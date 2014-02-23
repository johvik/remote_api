package remote.test.api;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.junit.Test;

import remote.api.Packet;
import remote.api.Protocol.PingCallback;
import remote.api.ServerProtocol;
import remote.api.ServerProtocol.AuthenticationCheck;
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
}
