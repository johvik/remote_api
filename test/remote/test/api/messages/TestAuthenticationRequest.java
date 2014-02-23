package remote.test.api.messages;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.startsWith;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import remote.api.Message;
import remote.api.Packet;
import remote.api.Utils;
import remote.api.exceptions.PacketException;
import remote.api.messages.AuthenticationRequest;

@RunWith(Parameterized.class)
public class TestAuthenticationRequest {
	private byte[] key;
	private String user;
	private String password;

	public TestAuthenticationRequest(byte[] key, String user, String password) {
		this.key = key;
		this.user = user;
		this.password = password;
	}

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays
				.asList(new Object[][] {
						{ new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 }, "", "" },
						{ new byte[] { -1, -2, -3, -4, -5, -6, -7, -8 },
								"USER", "PASSWORD" },
						{
								// Exactly MAX_LENGTH
								new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 },
								"a longer user than the other ones",
								"also the password is a lot longer than the other tests: "
										+ "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa "
										+ "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb "
										+ "ccccccccccccccccccccccccccccccccccccccccc" } });
	}

	@Test
	public void testAuthenticationRequest() {
		try {
			new AuthenticationRequest(null, user, password);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			assertEquals("Null input data null", e.getMessage());
		}
		try {
			new AuthenticationRequest(key, null, password);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			assertEquals("Null input data " + Utils.toHex(key), e.getMessage());
		}
		try {
			new AuthenticationRequest(key, user, null);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			assertEquals("Null input data " + Utils.toHex(key), e.getMessage());
		}
		try {
			new AuthenticationRequest(new byte[0], user, password);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			assertEquals("Wrong key length", e.getMessage());
		}
	}

	@Test
	public void testPack() throws PacketException {
		// Test by packing followed by unpacking
		AuthenticationRequest request1 = new AuthenticationRequest(key, user,
				password);
		byte[] data = request1.pack().getData();
		AuthenticationRequest request2 = AuthenticationRequest.unpack(data);
		assertEquals(Message.AUTHENTICATION_REQUEST, data[0]);
		assertEquals(user, request1.getUser());
		assertEquals(password, request1.getPassword());
		assertArrayEquals(key, request1.getKey());
		// Check that they are the same
		assertEquals(request1.getUser(), request2.getUser());
		assertEquals(request1.getPassword(), request2.getPassword());
		assertArrayEquals(request1.getKey(), request2.getKey());

		// Try to pack with too long lengths
		try {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < 30; i++) {
				buffer.append("too long");
			}
			AuthenticationRequest request3 = new AuthenticationRequest(key,
					buffer.toString(), password);
			request3.pack();
			fail("Did not throw an exception");
		} catch (PacketException e) {
			assertThat(e.getMessage(), startsWith("Length sum too big "));
		}
	}

	@Test
	public void testUnpack() throws PacketException {
		// Check that it throws when it has wrong length
		byte[] data = new byte[0];
		try {
			AuthenticationRequest.unpack(data);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			assertEquals("Unexpected length", e.getMessage());
		}
		// Correct length should not throw
		data = new byte[AuthenticationRequest.MAX_LENGTH];
		AuthenticationRequest request = AuthenticationRequest.unpack(data);
		assertArrayEquals(new byte[Packet.BLOCK_KEY_SIZE], request.getKey());
		assertEquals("", request.getUser());
		assertEquals("", request.getPassword());

		// Try to unpack with too long lengths
		try {
			data[10] = (byte) 0xFF;
			AuthenticationRequest.unpack(data);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			assertEquals("Length sum too big " + Utils.toHex(data),
					e.getMessage());
		}
	}

	@Test
	public void testGetType() throws PacketException {
		// Ensure it has the correct type
		AuthenticationRequest request = new AuthenticationRequest(key, user,
				password);
		assertEquals(Message.AUTHENTICATION_REQUEST, request.getType());
	}
}
