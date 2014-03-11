package remote.test.api;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;

import org.junit.Test;

import remote.api.ClientProtocol;
import remote.api.Packet;
import remote.api.Protocol.PingCallback;
import remote.api.commands.Command;
import remote.api.commands.MouseMove;
import remote.api.exceptions.AuthenticationException;
import remote.api.exceptions.ProtocolException;
import remote.api.messages.AuthenticationRequest;
import remote.api.messages.AuthenticationResponse;
import remote.api.messages.CommandRequest;
import remote.api.messages.Message;
import remote.api.messages.Ping;

/**
 * Test class for {@link ClientProtocol}.
 */
public class TestClientProtocol {
	/**
	 * Stores the ping callback results.
	 */
	private long pingDiff = -1;
	/**
	 * The ping callback.
	 */
	private PingCallback pingCallback = new PingCallback() {
		@Override
		public void run(long diff) {
			pingDiff = diff;
		}
	};

	/**
	 * Test method for
	 * {@link ClientProtocol#ClientProtocol(java.security.PublicKey, byte[], byte[], java.io.InputStream, java.io.OutputStream)}
	 * .
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testClientProtocol() throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			new ClientProtocol(null, Misc.key, Misc.iv, input, output);
			fail("Did not throw an exception");
		} catch (InvalidKeyException e) {
			// Skip checking this one
		}
		try {
			new ClientProtocol(Misc.publicKey, null, Misc.iv, input, output);
			fail("Did not throw an exception");
		} catch (InvalidKeyException e) {
			InvalidKeyException ex = new InvalidKeyException(
					"Key cannot be null");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		try {
			new ClientProtocol(Misc.publicKey, Misc.key, null, input, output);
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException("Iv cannot be null");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		try {
			// Wrong key size
			new ClientProtocol(Misc.publicKey,
					new byte[Packet.BLOCK_KEY_SIZE - 1], Misc.iv, input,
					output);
			fail("Did not throw an exception");
		} catch (InvalidKeyException e) {
			InvalidKeyException ex = new InvalidKeyException(
					"Key has wrong length");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		try {
			// Wrong iv size
			new ClientProtocol(Misc.publicKey, Misc.key,
					new byte[Packet.BLOCK_SIZE - 1], input, output);
			fail("Did not throw an exception");
		} catch (InvalidKeyException e) {
			InvalidKeyException ex = new InvalidKeyException(
					"Iv has wrong length");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		try {
			new ClientProtocol(Misc.publicKey, Misc.key, Misc.iv, null, output);
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException("Input cannot be null");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		try {
			new ClientProtocol(Misc.publicKey, Misc.key, Misc.iv, input, null);
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException(
					"Output cannot be null");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		// Correct
		new ClientProtocol(Misc.publicKey, Misc.key, Misc.iv, input, output);
		new ClientProtocol(Misc.publicKey, input, output);
	}

	/**
	 * Test method for {@link ClientProtocol#authenticate(String, String)}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testNotAuthenticated() throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ClientProtocol cp = new ClientProtocol(Misc.publicKey, Misc.key,
				Misc.iv, input, output);
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

	/**
	 * Test method for {@link ClientProtocol#process(Packet)}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testProcess() throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ClientProtocol cp = new ClientProtocol(Misc.publicKey, Misc.key,
				Misc.iv, input, output);

		// Try to process without authentication
		try {
			cp.process(Misc.encryptBlock(new Ping(true).pack()));
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException(
					"Unexpected message type: " + Message.PING);
			assertEquals(ex.getMessage(), e.getMessage());
		}

		// Try to authenticate twice
		cp.process(Misc.encryptBlock(new AuthenticationResponse().pack()));
		// Not allowed to do it twice
		try {
			cp.process(Misc.encryptBlock(new AuthenticationResponse().pack()));
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException(
					"Unexpected message type: "
							+ Message.AUTHENTICATION_RESPONSE);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		assertArrayEquals(new byte[0], output.toByteArray());
	}

	/**
	 * Test method for {@link ClientProtocol#authenticate(String, String)}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testAuthenticate() throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ClientProtocol cp = new ClientProtocol(Misc.publicKey, Misc.key,
				Misc.iv, input, output);

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
		cp.process(Misc.encryptBlock(new AuthenticationResponse().pack()));

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

	/**
	 * Test method for {@link ClientProtocol#commandRequest(Command)}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testCommandRequest() throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ClientProtocol cp = new ClientProtocol(Misc.publicKey, Misc.key,
				Misc.iv, input, output);
		// Authenticate
		cp.process(Misc.encryptBlock(new AuthenticationResponse().pack()));

		// Send a mouse move
		Command command = new MouseMove((short) 1, (short) -1);
		cp.commandRequest(command);
		Packet p = Packet.read(output.toByteArray());
		CommandRequest r = (CommandRequest) p.decode(Misc.blockDecrypt);
		assertEquals(0, command.compareTo(r.getCommand()));
	}

	/**
	 * Test method for {@link ClientProtocol#ping(PingCallback)}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testPing() throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ClientProtocol cp = new ClientProtocol(Misc.publicKey, Misc.key,
				Misc.iv, input, output);
		// Authenticate
		cp.process(Misc.encryptBlock(new AuthenticationResponse().pack()));

		// Send a ping
		cp.ping(null);
		// Check that it was written
		Packet p = Packet.read(output.toByteArray());
		Ping ping = (Ping) p.decode(Misc.blockDecrypt);
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
		cp.process(Misc.encryptBlock(new Ping(false).pack()));
		assertArrayEquals(new byte[0], output.toByteArray());

		// Process twice (not allowed)
		try {
			cp.process(Misc.encryptBlock(new Ping(false).pack()));
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
			ping = (Ping) p.decode(Misc.blockDecrypt);
			assertEquals(0, ping.compareTo(new Ping(true)));
			// Check that the callback wasn't called before the response
			assertEquals(-1, pingDiff);

			// Fake response
			cp.process(Misc.encryptBlock(new Ping(false).pack()));
			long diff = System.nanoTime() - start;
			assertThat(pingDiff, lessThanOrEqualTo(diff));
		}

		output.reset();
		// Respond to a request
		cp.process(Misc.encryptBlock(new Ping(true).pack()));
		// Check that it was written
		p = Packet.read(output.toByteArray());
		ping = (Ping) p.decode(Misc.blockDecrypt);
		assertEquals(0, ping.compareTo(new Ping(false)));
	}

	/**
	 * Test method for {@link ClientProtocol#nextPacket()}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testNextPacket() throws Exception {
		ByteArrayOutputStream tmp = new ByteArrayOutputStream();
		AuthenticationResponse r = new AuthenticationResponse();
		r.pack().write(Misc.blockEncrypt, tmp);

		ByteArrayInputStream input = new ByteArrayInputStream(tmp.toByteArray());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ClientProtocol cp = new ClientProtocol(Misc.publicKey, Misc.key,
				Misc.iv, input, output);

		// Makes no sense to test more than one scenario, since it is a wrapper
		// of the PacketScanner class
		Packet p = cp.nextPacket();
		assertEquals(0, r.compareTo(p.decode(Misc.blockDecrypt)));
	}
}
