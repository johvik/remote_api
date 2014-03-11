package remote.test.api;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;

import org.junit.Test;

import remote.api.Packet;
import remote.api.ServerProtocol;
import remote.api.Protocol.PingCallback;
import remote.api.ServerProtocol.Handler;
import remote.api.commands.Command;
import remote.api.commands.MouseMove;
import remote.api.exceptions.AuthenticationException;
import remote.api.exceptions.ProtocolException;
import remote.api.messages.AuthenticationRequest;
import remote.api.messages.AuthenticationResponse;
import remote.api.messages.CommandRequest;
import remote.api.messages.Message;
import remote.api.messages.Ping;
import remote.api.messages.TerminateRequest;

/**
 * Test class for {@link ServerProtocol}.
 */
public class TestServerProtocol {
	/**
	 * State to force authentication to fail.
	 */
	private boolean authenticationFail = false;
	/**
	 * State to see if command has been handled.
	 */
	private Command command = null;
	/**
	 * State to see if terminate has been handled.
	 */
	private Boolean shutdown = null;
	/**
	 * The handler.
	 */
	private Handler handler = new Handler() {
		@Override
		public boolean authentication(String user, String password) {
			return !authenticationFail;
		}

		@Override
		public void command(Command command) {
			TestServerProtocol.this.command = command;
		}

		@Override
		public void terminate(boolean shutdown) {
			TestServerProtocol.this.shutdown = shutdown;
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
	 * {@link ServerProtocol#ServerProtocol(Handler, java.security.PrivateKey, java.io.InputStream, java.io.OutputStream)}
	 * .
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testServerProtocol() throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			new ServerProtocol(null, Misc.privateKey, input, output);
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException(
					"Handler cannot be null");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		try {
			new ServerProtocol(handler, null, input, output);
			fail("Did not throw an exception");
		} catch (InvalidKeyException e) {
			// Skip checking this one
		}
		try {
			new ServerProtocol(handler, Misc.privateKey, null, output);
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException("Input cannot be null");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		try {
			new ServerProtocol(handler, Misc.privateKey, input, null);
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException(
					"Output cannot be null");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		// Correct
		new ServerProtocol(handler, Misc.privateKey, input, output);
	}

	/**
	 * Test method for {@link ServerProtocol#ping(PingCallback)}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testPing() throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ServerProtocol sp = new ServerProtocol(handler, Misc.privateKey, input,
				output);
		// Authenticate
		sp.process(Misc.encryptSecure(new AuthenticationRequest(Misc.key,
				Misc.iv, "", "").pack()));
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
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testCommandRequest() throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ServerProtocol sp = new ServerProtocol(handler, Misc.privateKey, input,
				output);
		// Authenticate
		sp.process(Misc.encryptSecure(new AuthenticationRequest(Misc.key,
				Misc.iv, "", "").pack()));

		output.reset();
		assertEquals(null, command);
		// Send a command request
		MouseMove mm = new MouseMove((short) 0, (short) 0);
		sp.process(Misc.encryptBlock(new CommandRequest(mm).pack()));
		assertArrayEquals(new byte[0], output.toByteArray());
		assertEquals(0, mm.compareTo(command));
		command = null;
	}

	/**
	 * Test method for handling terminate requests in {@link ServerProtocol}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testTerminateRequest() throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ServerProtocol sp = new ServerProtocol(handler, Misc.privateKey, input,
				output);
		// Authenticate
		sp.process(Misc.encryptSecure(new AuthenticationRequest(Misc.key,
				Misc.iv, "", "").pack()));

		output.reset();
		assertEquals(null, shutdown);
		// Send a terminate request
		boolean s = false;
		sp.process(Misc.encryptBlock(new TerminateRequest(s).pack()));
		assertArrayEquals(new byte[0], output.toByteArray());
		assertEquals(s, shutdown);
		shutdown = null;
	}

	/**
	 * Test method for authentication in {@link ServerProtocol}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testNotAuthenticated() throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ServerProtocol sp = new ServerProtocol(handler, Misc.privateKey, input,
				output);
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
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testProcess() throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		authenticationFail = true;
		ServerProtocol sp = new ServerProtocol(handler, Misc.privateKey, input,
				output);
		// Fail to authenticate
		try {
			sp.process(Misc.encryptSecure(new AuthenticationRequest(Misc.key,
					Misc.iv, "", "").pack()));
			fail("Did not throw an exception");
		} catch (AuthenticationException e) {
			AuthenticationException ex = new AuthenticationException(
					"Bad login");
			assertEquals(ex.getMessage(), e.getMessage());
		}
		assertArrayEquals(new byte[0], output.toByteArray());
		authenticationFail = false;

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
		sp = new ServerProtocol(handler, Misc.privateKey, input, output);
		sp.process(Misc.encryptSecure(new AuthenticationRequest(Misc.key,
				Misc.iv, "", "").pack()));
		Packet p = Packet.read(output.toByteArray());
		AuthenticationResponse r = (AuthenticationResponse) p
				.decode(Misc.blockDecrypt);
		assertEquals(0, r.compareTo(new AuthenticationResponse()));

		output.reset();
		// Not allowed to do it twice
		try {
			// Block encryption to allow decode
			sp.process(Misc.encryptBlock(new AuthenticationRequest(Misc.key,
					Misc.iv, "", "").pack()));
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
	@Test
	public void testNextPacket() throws Exception {
		ByteArrayOutputStream tmp = new ByteArrayOutputStream();
		AuthenticationRequest r = new AuthenticationRequest(Misc.key, Misc.iv,
				"user", "password");
		r.pack().write(Misc.secureEncrypt, tmp);

		ByteArrayInputStream input = new ByteArrayInputStream(tmp.toByteArray());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ServerProtocol sp = new ServerProtocol(handler, Misc.privateKey, input,
				output);

		// Makes no sense to test more than one scenario, since it is a wrapper
		// of the PacketScanner class
		Packet p = sp.nextPacket();
		assertEquals(0, r.compareTo(p.decode(Misc.secureDecrypt)));
	}
}
