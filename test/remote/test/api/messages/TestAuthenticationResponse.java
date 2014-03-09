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
	 * Test method for {@link AuthenticationResponse#pack()}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testPack() throws Exception {
		AuthenticationResponse response = new AuthenticationResponse();
		byte[] data = response.pack().getData();
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
		AuthenticationResponse response = new AuthenticationResponse();
		assertEquals(Message.AUTHENTICATION_RESPONSE, response.getType());
	}

	/**
	 * Test method for {@link AuthenticationResponse#compareTo(Message)}.
	 */
	@Test
	public void testCompareTo() {
		AuthenticationResponse response = new AuthenticationResponse();
		try {
			response.compareTo(null);
			fail("Did not throw an exception");
		} catch (NullPointerException e) {
		}
		try {
			response.compareTo(new Ping(false));
			fail("Did not throw an exception");
		} catch (ClassCastException e) {
		}

		// Compare to self
		assertEquals(0, response.compareTo(response));
	}
}
