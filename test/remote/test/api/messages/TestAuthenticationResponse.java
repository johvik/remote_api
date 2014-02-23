package remote.test.api.messages;

import static org.junit.Assert.*;

import org.junit.Test;

import remote.api.Message;
import remote.api.exceptions.PacketException;
import remote.api.messages.AuthenticationResponse;

public class TestAuthenticationResponse {

	@Test
	public void testPack() throws PacketException {
		AuthenticationResponse response = new AuthenticationResponse();
		byte[] data = response.pack().getData();
		assertEquals(Message.AUTHENTICATION_RESPONSE, data[0]);
	}

	@Test
	public void testUnpack() throws PacketException {
		// Check that it throws when it has wrong length
		byte[] data = new byte[0];
		try {
			AuthenticationResponse.unpack(data);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			assertEquals("Unexpected length", e.getMessage());
		}
		// Correct length should not throw
		data = new byte[AuthenticationResponse.PACKET_SIZE];
		AuthenticationResponse.unpack(data);
	}

	@Test
	public void testGetType() {
		// Ensure it has the correct type
		AuthenticationResponse response = new AuthenticationResponse();
		assertEquals(Message.AUTHENTICATION_RESPONSE, response.getType());
	}

}
