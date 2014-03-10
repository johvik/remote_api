package remote.test.api.messages;

import static org.junit.Assert.*;

import org.junit.Test;

import remote.api.exceptions.PacketException;
import remote.api.messages.AuthenticationResponse;
import remote.api.messages.Message;
import remote.api.messages.Ping;

/**
 * Test class for {@link AuthenticationResponse}.
 */
public class TestAuthenticationResponse {
	/**
	 * The authentication response constructed for the test.
	 */
	private AuthenticationResponse ar;

	/**
	 * Initializes the test
	 */
	public TestAuthenticationResponse() {
		ar = new AuthenticationResponse();
	}

	/**
	 * Test method for {@link AuthenticationResponse#pack()}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testPack() throws Exception {
		byte[] data = ar.pack().getData();
		assertEquals(Message.AUTHENTICATION_RESPONSE, data[0]);
	}

	/**
	 * Test method for {@link AuthenticationResponse#unpack(byte[])}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testUnpack() throws Exception {
		// Check that it throws when it has wrong length
		byte[] data = new byte[0];
		try {
			AuthenticationResponse.unpack(data);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Unexpected length", data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		// Correct length should not throw
		data = new byte[AuthenticationResponse.LENGTH];
		AuthenticationResponse.unpack(data);
	}

	/**
	 * Test method for {@link AuthenticationResponse#getType()}.
	 */
	@Test
	public void testGetType() {
		// Ensure it has the correct type
		assertEquals(Message.AUTHENTICATION_RESPONSE, ar.getType());
	}

	/**
	 * Test method for {@link AuthenticationResponse#compareTo(Message)}.
	 */
	@Test
	public void testCompareTo() {
		try {
			ar.compareTo(null);
			fail("Did not throw an exception");
		} catch (NullPointerException e) {
		}
		try {
			ar.compareTo(new Ping(false));
			fail("Did not throw an exception");
		} catch (ClassCastException e) {
		}

		// Compare to self
		assertEquals(0, ar.compareTo(ar));
	}
}
