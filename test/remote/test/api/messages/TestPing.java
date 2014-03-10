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
	 * The ping constructed by the parameter.
	 */
	private Ping p;

	/**
	 * Constructs the ping from the parameter.
	 * 
	 * @param request
	 *            The request.
	 */
	public TestPing(boolean request) {
		this.request = request;
		p = new Ping(request);
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
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testPack() throws Exception {
		// Test by packing followed by unpacking
		byte[] data = p.pack().getData();
		Ping other = Ping.unpack(data);
		assertEquals(Message.PING, data[0]);
		assertEquals(request, p.isRequest());
		assertEquals(Message.PING, p.getType());
		// Check that they are the same
		assertEquals(p.isRequest(), other.isRequest());
		assertEquals(p.getType(), other.getType());
	}

	/**
	 * Test method for {@link Ping#unpack(byte[])}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testUnpack() throws Exception {
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
		assertEquals(request, p.isRequest());
	}

	/**
	 * Test method for {@link Ping#getType()}.
	 */
	@Test
	public void testGetType() {
		// Ensure it has the correct type
		assertEquals(Message.PING, p.getType());
	}

	/**
	 * Test method for {@link Ping#compareTo(Message)}.
	 */
	@Test
	public void testCompareTo() {
		try {
			p.compareTo(null);
			fail("Did not throw an exception");
		} catch (NullPointerException e) {
		}
		try {
			p.compareTo(new AuthenticationResponse());
			fail("Did not throw an exception");
		} catch (ClassCastException e) {
		}

		// Check against object with another request
		Ping other = new Ping(!request);
		assertNotEquals(0, p.compareTo(other));

		// Compare to self
		assertEquals(0, p.compareTo(p));
	}
}
