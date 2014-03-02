package remote.test.api;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;

import org.junit.Test;

import remote.api.ClientProtocol;
import remote.api.Packet;
import remote.api.Protocol.PingCallback;
import remote.api.commands.Command;
import remote.api.commands.MouseMove;
import remote.api.exceptions.AuthenticationException;
import remote.api.exceptions.PacketException;
import remote.api.exceptions.ProtocolException;
import remote.api.messages.AuthenticationRequest;
import remote.api.messages.AuthenticationResponse;
import remote.api.messages.CommandRequest;
import remote.api.messages.Message;
import remote.api.messages.Ping;

public class TestClientProtocol {
	private long pingDiff = -1;
	private PingCallback pingCallback = new PingCallback() {
		@Override
		public void run(long diff) {
			pingDiff = diff;
		}
	};

	@Test
	public void testClientProtocol() throws GeneralSecurityException,
			ProtocolException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			new ClientProtocol(null, Misc.key, output);
			fail("Did not throw an exception");
		} catch (InvalidKeyException e) {
			// Skip checking this one
		}
		try {
			new ClientProtocol(Misc.publicKey, null, output);
			fail("Did not throw an exception");
		} catch (InvalidKeyException e) {
			InvalidKeyException ex = new InvalidKeyException(
					"Key cannot be null");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		try {
			// Wrong key size
			new ClientProtocol(Misc.publicKey,
					new byte[Packet.BLOCK_KEY_SIZE - 1], output);
			fail("Did not throw an exception");
		} catch (InvalidKeyException e) {
			InvalidKeyException ex = new InvalidKeyException(
					"Key has wrong length");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		try {
			new ClientProtocol(Misc.publicKey, Misc.key, null);
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException(
					"Output cannot be null");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		// Correct
		new ClientProtocol(Misc.publicKey, Misc.key, output);
	}

	@Test
	public void testNotAuthenticated() throws GeneralSecurityException,
			ProtocolException, PacketException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ClientProtocol cp = new ClientProtocol(Misc.publicKey, Misc.key, output);
		// Not authenticated
		try {
			cp.ping(null);
			fail("Did not throw an exception");
		} catch (AuthenticationException e) {
			AuthenticationException ex = new AuthenticationException(
					"Expecting authentication");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		assertArrayEquals(new byte[0], output.toByteArray());
	}

	@Test
	public void testProcess() throws GeneralSecurityException,
			ProtocolException, PacketException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ClientProtocol cp = new ClientProtocol(Misc.publicKey, Misc.key, output);

		// Try to process without authentication
		try {
			cp.process(new Ping(true).pack());
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException(
					"Unexpected message type: " + Message.PING);
			assertEquals(ex.getMessage(), e.getMessage());
		}

		// Try to authenticate twice
		cp.process(new AuthenticationResponse().pack());
		// Not allowed to do it twice
		try {
			cp.process(new AuthenticationResponse().pack());
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException(
					"Unexpected message type: "
							+ Message.AUTHENTICATION_RESPONSE);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		assertArrayEquals(new byte[0], output.toByteArray());
	}

	@Test
	public void testAuthenticate() throws GeneralSecurityException,
			ProtocolException, PacketException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ClientProtocol cp = new ClientProtocol(Misc.publicKey, Misc.key, output);

		// Send authenticate
		String user = "user";
		String password = "password";
		cp.authenticate(user, password);

		Packet p = Packet.read(output.toByteArray());
		AuthenticationRequest r = (AuthenticationRequest) p
				.decode(Misc.secureDecrypt);
		assertArrayEquals(Misc.key, r.getKey());
		assertEquals(user, r.getUser());
		assertEquals(password, r.getPassword());

		output.reset();
		// Authenticate
		cp.process(new AuthenticationResponse().pack());

		// Should not work to authenticate twice
		try {
			cp.authenticate(user, password);
		} catch (AuthenticationException e) {
			AuthenticationException ex = new AuthenticationException(
					"Already authenticated");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		assertArrayEquals(new byte[0], output.toByteArray());
	}

	@Test
	public void testCommandRequest() throws GeneralSecurityException,
			ProtocolException, PacketException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ClientProtocol cp = new ClientProtocol(Misc.publicKey, Misc.key, output);
		// Authenticate
		cp.process(new AuthenticationResponse().pack());

		// Send a mouse move
		Command command = new MouseMove((short) 1, (short) -1);
		cp.commandRequest(command);
		Packet p = Packet.read(output.toByteArray());
		CommandRequest r = (CommandRequest) p.decode(Misc.blockDecryptCipher);
		assertEquals(0, command.compareTo(r.getCommand()));
	}

	@Test
	public void testPing() throws GeneralSecurityException, ProtocolException,
			PacketException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ClientProtocol cp = new ClientProtocol(Misc.publicKey, Misc.key, output);
		// Authenticate
		cp.process(new AuthenticationResponse().pack());

		// Send a ping
		cp.ping(null);
		// Check that it was written
		Packet p = Packet.read(output.toByteArray());
		Ping ping = (Ping) p.decode(Misc.blockDecryptCipher);
		assertEquals(0, ping.compareTo(new Ping(true)));

		// Try to ping twice (not allowed)
		try {
			cp.ping(null);
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException(
					"Ping already requested");
			assertEquals(ex.getMessage(), e.getMessage());
		}

		output.reset();
		// Fake response to requested ping
		cp.process(new Ping(false).pack());
		assertArrayEquals(new byte[0], output.toByteArray());

		// Process twice (not allowed)
		try {
			cp.process(new Ping(false).pack());
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException("Ping not requested");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		assertArrayEquals(new byte[0], output.toByteArray());

		// Measure the ping time a couple of times
		for (int i = 0; i < 5; i++) {
			pingDiff = -1;
			long start = System.nanoTime();
			// Request with callback
			cp.ping(pingCallback);
			// Check that it was written
			p = Packet.read(output.toByteArray());
			ping = (Ping) p.decode(Misc.blockDecryptCipher);
			assertEquals(0, ping.compareTo(new Ping(true)));
			// Check that the callback wasn't called before the response
			assertEquals(-1, pingDiff);

			// Fake response
			cp.process(new Ping(false).pack());
			long diff = System.nanoTime() - start;
			assertThat(pingDiff, lessThanOrEqualTo(diff));
		}

		output.reset();
		// Respond to a request
		cp.process(new Ping(true).pack());
		// Check that it was written
		p = Packet.read(output.toByteArray());
		ping = (Ping) p.decode(Misc.blockDecryptCipher);
		assertEquals(0, ping.compareTo(new Ping(false)));
	}
}
