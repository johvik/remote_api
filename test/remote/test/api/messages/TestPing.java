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
		assertEquals(Message.PING, data[0]);
		assertEquals(request, ping1.isRequest());
		assertEquals(Message.PING, ping1.getType());
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
			assertEquals("Unexpected length", e.getMessage());
		}
		// Correct length should not throw
		data = new byte[Ping.LENGTH];
		data[1] = (byte) (request ? 1 : 0);
		Ping ping = Ping.unpack(data);
		assertEquals(request, ping.isRequest());
	}

	@Test
	public void testIsRequest() {
		Ping ping = new Ping(request);
		assertEquals(request, ping.isRequest());
	}

	@Test
	public void testGetType() {
		// Ensure it has the correct type
		Ping ping = new Ping(request);
		assertEquals(Message.PING, ping.getType());
	}
}
