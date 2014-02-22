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
		assertEquals(data[0], Message.AUTHENTICATION_RESPONSE);
	}

	@Test
	public void testUnpack() throws PacketException {
		// Check that it throws when it has wrong length
		byte[] data = new byte[0];
		try {
			AuthenticationResponse.unpack(data);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			assertEquals(e.getMessage(), "Unexpected length");
		}
		// Correct length should not throw
		data = new byte[AuthenticationResponse.PACKET_SIZE];
		AuthenticationResponse.unpack(data);
	}

	@Test
	public void testGetType() {
		// Ensure it has the correct type
		AuthenticationResponse response = new AuthenticationResponse();
		assertEquals(response.getType(), Message.AUTHENTICATION_RESPONSE);
	}

}
