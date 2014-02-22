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
								new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 },
								"a longer user than the other ones",
								"also the password is a lot longer than the other tests: "
										+ "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa "
										+ "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb "
										+ "ccccccccccccccccccccccccccccccccccccccccc" } });
	}

	@Test
	public void testConstructor() {
		try {
			new AuthenticationRequest(null, user, password);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			assertEquals(e.getMessage(), "Null input data null");
		}
		try {
			new AuthenticationRequest(key, null, password);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			assertEquals(e.getMessage(), "Null input data " + Utils.toHex(key));
		}
		try {
			new AuthenticationRequest(key, user, null);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			assertEquals(e.getMessage(), "Null input data " + Utils.toHex(key));
		}
		try {
			new AuthenticationRequest(new byte[0], user, password);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			assertEquals(e.getMessage(), "Wrong key length");
		}
	}

	@Test
	public void testPack() throws PacketException {
		// Test by packing followed by unpacking
		AuthenticationRequest request1 = new AuthenticationRequest(key, user,
				password);
		byte[] data = request1.pack().getData();
		AuthenticationRequest request2 = AuthenticationRequest.unpack(data);
		assertEquals(data[0], Message.AUTHENTICATION_REQUEST);
		assertEquals(request1.getUser(), user);
		assertEquals(request1.getPassword(), password);
		assertArrayEquals(request1.getKey(), key);
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
			assertEquals(e.getMessage(), "Unexpected length");
		}
		// Correct length should not throw
		data = new byte[AuthenticationRequest.PACKET_SIZE];
		AuthenticationRequest request = AuthenticationRequest.unpack(data);
		assertArrayEquals(request.getKey(),
				new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 });
		assertEquals(request.getUser(), "");
		assertEquals(request.getPassword(), "");

		// Try to unpack with too long lengths
		try {
			data[10] = (byte) 0xFF;
			AuthenticationRequest.unpack(data);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			assertEquals(e.getMessage(),
					"Length sum too big " + Utils.toHex(data));
		}
	}

	@Test
	public void testGetType() throws PacketException {
		// Ensure it has the correct type
		AuthenticationRequest request = new AuthenticationRequest(key, user,
				password);
		assertEquals(request.getType(), Message.AUTHENTICATION_REQUEST);
	}
}
