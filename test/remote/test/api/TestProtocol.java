package remote.test.api;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.junit.Test;

import remote.api.Packet;
import remote.api.Protocol.PingCallback;
import remote.api.ServerProtocol;
import remote.api.ServerProtocol.AuthenticationCheck;
import remote.api.exceptions.AuthenticationException;
import remote.api.exceptions.PacketException;
import remote.api.exceptions.ProtocolException;
import remote.api.messages.AuthenticationRequest;
import remote.api.messages.Ping;

public class TestProtocol {
	private long pingDiff = -1;
	private PingCallback pingCallback = new PingCallback() {
		@Override
		public void run(long diff) {
			pingDiff = diff;
		}
	};

	@Test
	public void testPing() throws GeneralSecurityException, ProtocolException,
			PacketException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ServerProtocol sp = new ServerProtocol(new AuthenticationCheck() {
			@Override
			public boolean check(String user, String password) {
				return true;
			}
		}, Keys.privateKey, output);

		// Try without authenticating
		try {
			sp.ping(null);
			fail("Did not throw an exception");
		} catch (AuthenticationException e) {
			assertEquals("Expecting authentication", e.getMessage());
		}

		// Authenticate
		sp.process(new AuthenticationRequest(new byte[Packet.BLOCK_KEY_SIZE],
				"", "").pack());
		// Now ping should work
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
		} catch (ProtocolException e) {
			assertEquals("Ping not requested", e.getMessage());
		}

		// Request with callback
		sp.ping(pingCallback);

		assertEquals(-1, pingDiff);
		// Respond
		sp.process(new Ping(false).pack());
		assertThat(pingDiff, greaterThanOrEqualTo(0L));

		// Fake respond to a request
		sp.process(new Ping(true).pack());
	}
}
