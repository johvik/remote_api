package remote.test.api.messages;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import remote.api.exceptions.PacketException;
import remote.api.messages.AuthenticationResponse;
import remote.api.messages.Message;
import remote.api.messages.Ping;

/**
 * Test class for {@link Ping}.
 */
@RunWith(Parameterized.class)
public class TestPing {
	/**
	 * The request parameter.
	 */
	private boolean request;

	/**
	 * Constructs the ping from the parameter.
	 * 
	 * @param request
	 *            The request.
	 */
	public TestPing(boolean request) {
		this.request = request;
	}

	/**
	 * Creates input parameters.
	 * 
	 * @return The parameters.
	 */
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { true }, { false } });
	}

	/**
	 * Test method for {@link Ping#pack()}.
	 * 
	 * @throws PacketException
	 *             If something went wrong.
	 */
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

	/**
	 * Test method for {@link Ping#unpack(byte[])}.
	 * 
	 * @throws PacketException
	 *             If something went wrong.
	 */
	@Test
	public void testUnpack() throws PacketException {
		// Check that it throws when it has wrong length
		byte[] data = new byte[0];
		try {
			Ping.unpack(data);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Unexpected length", data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		// Correct length should not throw
		data = new byte[Ping.LENGTH];
		data[1] = (byte) (request ? 1 : 0);
		Ping ping = Ping.unpack(data);
		assertEquals(request, ping.isRequest());
	}

	/**
	 * Test method for {@link Ping#isRequest()}.
	 */
	@Test
	public void testIsRequest() {
		Ping ping = new Ping(request);
		assertEquals(request, ping.isRequest());
	}

	/**
	 * Test method for {@link Ping#getType()}.
	 */
	@Test
	public void testGetType() {
		// Ensure it has the correct type
		Ping ping = new Ping(request);
		assertEquals(Message.PING, ping.getType());
	}

	/**
	 * Test method for {@link Ping#compareTo(Message)}.
	 */
	@Test
	public void testCompareTo() {
		Ping ping = new Ping(request);
		try {
			ping.compareTo(null);
			fail("Did not throw an exception");
		} catch (NullPointerException e) {
		}
		try {
			ping.compareTo(new AuthenticationResponse());
			fail("Did not throw an exception");
		} catch (ClassCastException e) {
		}

		// Check against object with another request
		Ping other = new Ping(!request);
		assertNotEquals(0, ping.compareTo(other));

		// Compare to self
		assertEquals(0, ping.compareTo(ping));
	}
}
