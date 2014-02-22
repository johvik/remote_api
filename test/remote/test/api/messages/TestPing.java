package remote.test.api.messages;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import remote.api.Message;
import remote.api.exceptions.PacketException;
import remote.api.messages.Ping;

@RunWith(Parameterized.class)
public class TestPing {
	private boolean request;

	public TestPing(boolean request) {
		this.request = request;
	}

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { true }, { false } });
	}

	@Test
	public void testPack() throws PacketException {
		// Test by packing followed by unpacking
		Ping ping1 = new Ping(request);
		byte[] data = ping1.pack().getData();
		Ping ping2 = Ping.unpack(data);
		assertEquals(data[0], Message.PING);
		assertEquals(ping1.isRequest(), request);
		assertEquals(ping1.getType(), Message.PING);
		// Check that they are the same
		assertEquals(ping1.isRequest(), ping2.isRequest());
		assertEquals(ping1.getType(), ping2.getType());
	}

	@Test
	public void testUnpack() throws PacketException {
		// Check that it throws when it has wrong length
		byte[] data = new byte[0];
		try {
			Ping.unpack(data);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			assertEquals(e.getMessage(), "Unexpected length");
		}
		// Correct length should not throw
		data = new byte[Ping.PACKET_SIZE];
		data[1] = (byte) (request ? 1 : 0);
		Ping ping = Ping.unpack(data);
		assertEquals(ping.isRequest(), request);
	}

	@Test
	public void testRequest() {
		Ping ping = new Ping(request);
		assertEquals(ping.isRequest(), request);
	}

	@Test
	public void testGetType() {
		// Ensure it has the correct type
		Ping ping = new Ping(request);
		assertEquals(ping.getType(), Message.PING);
	}
}
