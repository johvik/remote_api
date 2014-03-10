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
import remote.api.messages.TerminateRequest;

/**
 * Test class for {@link TerminateRequest}.
 */
@RunWith(Parameterized.class)
public class TestTerminateRequest {
	/**
	 * The shutdown parameter.
	 */
	private boolean shutdown;
	/**
	 * The terminate request constructed by the parameter.
	 */
	private TerminateRequest tr;

	/**
	 * Constructs the terminate request from the parameter.
	 * 
	 * @param shutdown
	 *            The shutdown.
	 */
	public TestTerminateRequest(boolean shutdown) {
		this.shutdown = shutdown;
		tr = new TerminateRequest(shutdown);
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
	 * Test method for {@link TerminateRequest#pack()}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testPack() throws Exception {
		// Test by packing followed by unpacking
		byte[] data = tr.pack().getData();
		TerminateRequest other = TerminateRequest.unpack(data);
		assertEquals(Message.TERMINATE_REQUESET, data[0]);
		assertEquals(shutdown, tr.isShutdown());
		assertEquals(Message.TERMINATE_REQUESET, tr.getType());
		// Check that they are the same
		assertEquals(tr.isShutdown(), other.isShutdown());
		assertEquals(tr.getType(), other.getType());
	}

	/**
	 * Test method for {@link TerminateRequest#unpack(byte[])}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testUnpack() throws Exception {
		// Check that it throws when it has wrong length
		byte[] data = new byte[0];
		try {
			TerminateRequest.unpack(data);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Unexpected length", data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		// Correct length should not throw
		data = new byte[TerminateRequest.LENGTH];
		data[1] = (byte) (shutdown ? 1 : 0);
		TerminateRequest r = TerminateRequest.unpack(data);
		assertEquals(shutdown, r.isShutdown());
	}

	/**
	 * Test method for {@link TerminateRequest#isShutdown()}.
	 */
	@Test
	public void testIsShutdown() {
		assertEquals(shutdown, tr.isShutdown());
	}

	/**
	 * Test method for {@link TerminateRequest#getType()}.
	 */
	@Test
	public void testGetType() {
		// Ensure it has the correct type
		assertEquals(Message.TERMINATE_REQUESET, tr.getType());
	}

	/**
	 * Test method for {@link TerminateRequest#compareTo(Message)}.
	 */
	@Test
	public void testCompareTo() {
		try {
			tr.compareTo(null);
			fail("Did not throw an exception");
		} catch (NullPointerException e) {
		}
		try {
			tr.compareTo(new AuthenticationResponse());
			fail("Did not throw an exception");
		} catch (ClassCastException e) {
		}

		// Check against object with another request
		TerminateRequest other = new TerminateRequest(!shutdown);
		assertNotEquals(0, tr.compareTo(other));

		// Compare to self
		assertEquals(0, tr.compareTo(tr));
	}
}
