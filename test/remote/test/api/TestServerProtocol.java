package remote.test.api;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;

import org.junit.Test;

import remote.api.Message;
import remote.api.Packet;
import remote.api.ServerProtocol;
import remote.api.Protocol.PingCallback;
import remote.api.ServerProtocol.AuthenticationCheck;
import remote.api.exceptions.AuthenticationException;
import remote.api.exceptions.PacketException;
import remote.api.exceptions.ProtocolException;
import remote.api.messages.AuthenticationRequest;
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
			new ServerProtocol(null, Misc.privateKey, output);
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			assertEquals("Authentication check cannot be null", e.getMessage());
		}
		try {
			new ServerProtocol(authentication, null, output);
			fail("Did not throw an exception");
		} catch (InvalidKeyException e) {
			// Skip checking this one
		}
		try {
			new ServerProtocol(authentication, Misc.privateKey, null);
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			assertEquals("Output cannot be null", e.getMessage());
		}
	}

	@Test
	public void testPing() throws GeneralSecurityException, ProtocolException,
			PacketException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ServerProtocol sp = new ServerProtocol(authentication, Misc.privateKey,
				output);
		// Authenticate
		sp.process(new AuthenticationRequest(new byte[Packet.BLOCK_KEY_SIZE],
				"", "").pack());

		// Send a ping
		sp.ping(null);

		// Try to ping twice (not allowed)
		try {
			sp.ping(null);
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			assertEquals("Ping already requested", e.getMessage());
		}
		// Respond to requested ping
		sp.process(new Ping(false).pack());

		// Process twice (not allowed)
		try {
			sp.process(new Ping(false).pack());
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			assertEquals("Ping not requested", e.getMessage());
		}

		// Measure the ping time a couple of times
		for (int i = 0; i < 5; i++) {
			pingDiff = -1;
			long start = System.nanoTime();
			// Request with callback
			sp.ping(pingCallback);
			// Check that the callback wasn't called before the response
			assertEquals(-1, pingDiff);

			// Respond
			sp.process(new Ping(false).pack());
			long diff = System.nanoTime() - start;
			assertThat(pingDiff, lessThanOrEqualTo(diff));
		}

		// Fake respond to a request
		sp.process(new Ping(true).pack());
	}

	@Test
	public void testNotAuthenticated() throws GeneralSecurityException,
			ProtocolException, PacketException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ServerProtocol sp = new ServerProtocol(authentication, Misc.privateKey,
				output);
		// Not authenticated
		try {
			sp.ping(null);
			fail("Did not throw an exception");
		} catch (AuthenticationException e) {
			assertEquals("Expecting authentication", e.getMessage());
		}
	}

	@Test
	public void testProcess() throws GeneralSecurityException,
			ProtocolException, PacketException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ServerProtocol sp = new ServerProtocol(authenticationFail,
				Misc.privateKey, output);
		// Fail to authenticate
		try {
			sp.process(new AuthenticationRequest(
					new byte[Packet.BLOCK_KEY_SIZE], "", "").pack());
			fail("Did not throw an exception");
		} catch (AuthenticationException e) {
			assertEquals("Bad login", e.getMessage());
		}

		// Try to process without authentication
		try {
			sp.process(new Ping(true).pack());
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			assertEquals("Unexpected message type: " + Message.PING,
					e.getMessage());
		}

		// Try to authenticate twice
		sp = new ServerProtocol(authentication, Misc.privateKey, output);
		sp.process(new AuthenticationRequest(new byte[Packet.BLOCK_KEY_SIZE],
				"", "").pack());
		// Not allowed to do it twice
		try {
			sp.process(new AuthenticationRequest(
					new byte[Packet.BLOCK_KEY_SIZE], "", "").pack());
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			assertEquals("Unexpected message type: "
					+ Message.AUTHENTICATION_REQUEST, e.getMessage());
		}
	}
}
