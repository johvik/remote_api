package remote.test.api;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;

import org.junit.Test;

import remote.api.Packet;
import remote.api.ServerProtocol;
import remote.api.Protocol.PingCallback;
import remote.api.ServerProtocol.AuthenticationCheck;
import remote.api.ServerProtocol.CommandHandler;
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

public class TestServerProtocol {
	private AuthenticationCheck authentication = new AuthenticationCheck() {
		@Override
		public boolean check(String user, String password) {
			return true;
		}
	};
	private AuthenticationCheck authenticationFail = new AuthenticationCheck() {
		@Override
		public boolean check(String user, String password) {
			return false;
		}
	};
	private boolean commandHandled = false;
	private CommandHandler commandHandler = new CommandHandler() {
		@Override
		public void handle(Command command) {
			commandHandled = true;
		}
	};
	private long pingDiff = -1;
	private PingCallback pingCallback = new PingCallback() {
		@Override
		public void run(long diff) {
			pingDiff = diff;
		}
	};

	@Test
	public void testServerProtocol() throws GeneralSecurityException,
			ProtocolException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			new ServerProtocol(null, commandHandler, Misc.privateKey, output);
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException(
					"Authentication check cannot be null");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		try {
			new ServerProtocol(authentication, null, Misc.privateKey, output);
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException(
					"Command handler cannot be null");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		try {
			new ServerProtocol(authentication, commandHandler, null, output);
			fail("Did not throw an exception");
		} catch (InvalidKeyException e) {
			// Skip checking this one
		}
		try {
			new ServerProtocol(authentication, commandHandler, Misc.privateKey,
					null);
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException(
					"Output cannot be null");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		// Correct
		new ServerProtocol(authentication, commandHandler, Misc.privateKey,
				output);
	}

	@Test
	public void testPing() throws GeneralSecurityException, ProtocolException,
			PacketException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ServerProtocol sp = new ServerProtocol(authentication, commandHandler,
				Misc.privateKey, output);
		// Authenticate
		sp.process(Misc.encryptSecure(new AuthenticationRequest(Misc.key, "",
				"").pack()));
		output.reset();

		// Send a ping
		sp.ping(null);
		// Check that it was written
		Packet p = Packet.read(output.toByteArray());
		Ping ping = (Ping) p.decode(Misc.blockDecryptCipher);
		assertEquals(0, ping.compareTo(new Ping(true)));

		// Try to ping twice (not allowed)
		try {
			sp.ping(null);
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException(
					"Ping already requested");
			assertEquals(ex.getMessage(), e.getMessage());
		}

		output.reset();
		// Fake response to requested ping
		sp.process(Misc.encryptBlock(new Ping(false).pack()));
		assertArrayEquals(new byte[0], output.toByteArray());

		// Process twice (not allowed)
		try {
			sp.process(Misc.encryptBlock(new Ping(false).pack()));
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
			sp.ping(pingCallback);
			// Check that it was written
			p = Packet.read(output.toByteArray());
			ping = (Ping) p.decode(Misc.blockDecryptCipher);
			assertEquals(0, ping.compareTo(new Ping(true)));
			// Check that the callback wasn't called before the response
			assertEquals(-1, pingDiff);

			// Respond
			sp.process(Misc.encryptBlock(new Ping(false).pack()));
			long diff = System.nanoTime() - start;
			assertThat(pingDiff, lessThanOrEqualTo(diff));
		}

		output.reset();
		// Respond to a request
		sp.process(Misc.encryptBlock(new Ping(true).pack()));
		// Check that it was written
		p = Packet.read(output.toByteArray());
		ping = (Ping) p.decode(Misc.blockDecryptCipher);
		assertEquals(0, ping.compareTo(new Ping(false)));
	}

	@Test
	public void testCommandRequest() throws GeneralSecurityException,
			ProtocolException, PacketException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ServerProtocol sp = new ServerProtocol(authentication, commandHandler,
				Misc.privateKey, output);
		// Authenticate
		sp.process(Misc.encryptSecure(new AuthenticationRequest(Misc.key, "",
				"").pack()));

		output.reset();
		assertEquals(false, commandHandled);
		// Send a command request
		sp.process(Misc.encryptBlock(new CommandRequest(new MouseMove(
				(short) 0, (short) 0)).pack()));
		assertArrayEquals(new byte[0], output.toByteArray());
		assertEquals(true, commandHandled);
		commandHandled = false;
	}

	@Test
	public void testNotAuthenticated() throws GeneralSecurityException,
			ProtocolException, PacketException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ServerProtocol sp = new ServerProtocol(authentication, commandHandler,
				Misc.privateKey, output);
		// Not authenticated
		try {
			sp.ping(null);
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
		ServerProtocol sp = new ServerProtocol(authenticationFail,
				commandHandler, Misc.privateKey, output);
		// Fail to authenticate
		try {
			sp.process(Misc.encryptSecure(new AuthenticationRequest(
					new byte[Packet.BLOCK_KEY_SIZE], "", "").pack()));
			fail("Did not throw an exception");
		} catch (AuthenticationException e) {
			AuthenticationException ex = new AuthenticationException(
					"Bad login");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		assertArrayEquals(new byte[0], output.toByteArray());

		// Try to process without authentication
		try {
			// No encryption on the packet
			sp.process(new Ping(true).pack());
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException(
					"Unexpected message type: " + Message.PING);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		assertArrayEquals(new byte[0], output.toByteArray());

		// Try to authenticate twice
		sp = new ServerProtocol(authentication, commandHandler,
				Misc.privateKey, output);
		sp.process(Misc.encryptSecure(new AuthenticationRequest(Misc.key, "",
				"").pack()));
		Packet p = Packet.read(output.toByteArray());
		AuthenticationResponse r = (AuthenticationResponse) p
				.decode(Misc.blockDecryptCipher);
		assertEquals(0, r.compareTo(new AuthenticationResponse()));

		output.reset();
		// Not allowed to do it twice
		try {
			// Block encryption to allow decode
			sp.process(Misc.encryptBlock(new AuthenticationRequest(
					new byte[Packet.BLOCK_KEY_SIZE], "", "").pack()));
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException(
					"Unexpected message type: "
							+ Message.AUTHENTICATION_REQUEST);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		assertArrayEquals(new byte[0], output.toByteArray());
	}
}
