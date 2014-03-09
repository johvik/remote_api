package remote.test.api;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
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

/**
 * Test class for {@link ServerProtocol}.
 */
public class TestServerProtocol {
	/**
	 * The authentication check that always passes.
	 */
	private AuthenticationCheck authentication = new AuthenticationCheck() {
		@Override
		public boolean check(String user, String password) {
			return true;
		}
	};
	/**
	 * The authentication check that always fails.
	 */
	private AuthenticationCheck authenticationFail = new AuthenticationCheck() {
		@Override
		public boolean check(String user, String password) {
			return false;
		}
	};
	/**
	 * State to see if command has been handled.
	 */
	private boolean commandHandled = false;
	/**
	 * The command handler.
	 */
	private CommandHandler commandHandler = new CommandHandler() {
		@Override
		public void handle(Command command) {
			commandHandled = true;
		}
	};
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
	 * {@link ServerProtocol#ServerProtocol(AuthenticationCheck, CommandHandler, java.security.PrivateKey, java.io.InputStream, java.io.OutputStream)}
	 * .
	 * 
	 * @throws GeneralSecurityException
	 *             If something went wrong.
	 * @throws ProtocolException
	 *             If something went wrong.
	 * @throws PacketException
	 *             If something went wrong.
	 */
	@Test
	public void testServerProtocol() throws GeneralSecurityException,
			ProtocolException, PacketException {
		ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			new ServerProtocol(null, commandHandler, Misc.privateKey, input,
					output);
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException(
					"Authentication check cannot be null");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		try {
			new ServerProtocol(authentication, null, Misc.privateKey, input,
					output);
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException(
					"Command handler cannot be null");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		try {
			new ServerProtocol(authentication, commandHandler, null, input,
					output);
			fail("Did not throw an exception");
		} catch (InvalidKeyException e) {
			// Skip checking this one
		}
		try {
			new ServerProtocol(authentication, commandHandler, Misc.privateKey,
					null, output);
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException("Input cannot be null");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		try {
			new ServerProtocol(authentication, commandHandler, Misc.privateKey,
					input, null);
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException(
					"Output cannot be null");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		// Correct
		new ServerProtocol(authentication, commandHandler, Misc.privateKey,
				input, output);
	}

	/**
	 * Test method for {@link ServerProtocol#ping(PingCallback)}.
	 * 
	 * @throws GeneralSecurityException
	 *             If something went wrong.
	 * @throws ProtocolException
	 *             If something went wrong.
	 * @throws PacketException
	 *             If something went wrong.
	 * @throws IOException
	 *             If something went wrong.
	 */
	@Test
	public void testPing() throws GeneralSecurityException, ProtocolException,
			PacketException, IOException {
		ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ServerProtocol sp = new ServerProtocol(authentication, commandHandler,
				Misc.privateKey, input, output);
		// Authenticate
		sp.process(Misc.encryptSecure(new AuthenticationRequest(Misc.key, "",
				"").pack()));
		output.reset();

		// Send a ping
		sp.ping(null);
		// Check that it was written
		Packet p = Packet.read(output.toByteArray());
		Ping ping = (Ping) p.decode(Misc.blockDecrypt);
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
			ping = (Ping) p.decode(Misc.blockDecrypt);
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
		ping = (Ping) p.decode(Misc.blockDecrypt);
		assertEquals(0, ping.compareTo(new Ping(false)));
	}

	/**
	 * Test method for handling command requests in {@link ServerProtocol}.
	 * 
	 * @throws GeneralSecurityException
	 *             If something went wrong.
	 * @throws ProtocolException
	 *             If something went wrong.
	 * @throws PacketException
	 *             If something went wrong.
	 * @throws IOException
	 *             If something went wrong.
	 */
	@Test
	public void testCommandRequest() throws GeneralSecurityException,
			ProtocolException, PacketException, IOException {
		ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ServerProtocol sp = new ServerProtocol(authentication, commandHandler,
				Misc.privateKey, input, output);
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

	/**
	 * Test method for authentication in {@link ServerProtocol}.
	 * 
	 * @throws GeneralSecurityException
	 *             If something went wrong.
	 * @throws ProtocolException
	 *             If something went wrong.
	 * @throws PacketException
	 *             If something went wrong.
	 * @throws IOException
	 *             If something went wrong.
	 */
	@Test
	public void testNotAuthenticated() throws GeneralSecurityException,
			ProtocolException, PacketException, IOException {
		ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ServerProtocol sp = new ServerProtocol(authentication, commandHandler,
				Misc.privateKey, input, output);
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

	/**
	 * Test method for {@link ServerProtocol#process(Packet)}.
	 * 
	 * @throws GeneralSecurityException
	 *             If something went wrong.
	 * @throws ProtocolException
	 *             If something went wrong.
	 * @throws PacketException
	 *             If something went wrong.
	 * @throws IOException
	 *             If something went wrong.
	 */
	@Test
	public void testProcess() throws GeneralSecurityException,
			ProtocolException, PacketException, IOException {
		ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ServerProtocol sp = new ServerProtocol(authenticationFail,
				commandHandler, Misc.privateKey, input, output);
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
				Misc.privateKey, input, output);
		sp.process(Misc.encryptSecure(new AuthenticationRequest(Misc.key, "",
				"").pack()));
		Packet p = Packet.read(output.toByteArray());
		AuthenticationResponse r = (AuthenticationResponse) p
				.decode(Misc.blockDecrypt);
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

	/**
	 * Test method for {@link ServerProtocol#nextPacket()}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test(timeout = 1000)
	public void testNextPacket() throws Exception {
		ByteArrayOutputStream tmp = new ByteArrayOutputStream();
		AuthenticationRequest r = new AuthenticationRequest(Misc.key, "user",
				"password");
		r.pack().write(Misc.secureEncrypt, tmp);

		ByteArrayInputStream input = new ByteArrayInputStream(tmp.toByteArray());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ServerProtocol sp = new ServerProtocol(authentication, commandHandler,
				Misc.privateKey, input, output);

		// Makes no sense to test more than one scenario, since it is a wrapper
		// of the PacketScanner class
		Packet p = sp.nextPacket();
		assertEquals(0, r.compareTo(p.decode(Misc.secureDecrypt)));
	}
}
