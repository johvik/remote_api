package remote.test.api.messages;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.startsWith;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import remote.api.Packet;
import remote.api.exceptions.PacketException;
import remote.api.messages.AuthenticationRequest;
import remote.api.messages.Message;
import remote.api.messages.Ping;
import remote.test.api.Misc;

/**
 * Test class for {@link AuthenticationRequest}.
 */
@RunWith(Parameterized.class)
public class TestAuthenticationRequest {
	/**
	 * The key parameter.
	 */
	private byte[] key;
	/**
	 * The iv parameter.
	 */
	private byte[] iv;
	/**
	 * The user parameter.
	 */
	private String user;
	/**
	 * The password parameter.
	 */
	private String password;

	/**
	 * Initializes the parameters.
	 * 
	 * @param key
	 *            The key.
	 * @param iv
	 *            The iv.
	 * @param user
	 *            The user.
	 * @param password
	 *            The password.
	 */
	public TestAuthenticationRequest(byte[] key, byte[] iv, String user,
			String password) {
		this.key = key;
		this.iv = iv;
		this.user = user;
		this.password = password;
	}

	/**
	 * Creates input parameters.
	 * 
	 * @return The parameters.
	 */
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays
				.asList(new Object[][] {
						{
								Misc.getSequence(1, Packet.BLOCK_KEY_SIZE),
								Misc.getSequence(Packet.BLOCK_SIZE + 1,
										Packet.BLOCK_SIZE), "", "" },
						{
								Misc.getSequence(-Packet.BLOCK_KEY_SIZE,
										Packet.BLOCK_KEY_SIZE),
								Misc.getSequence(1, Packet.BLOCK_SIZE), "USER",
								"PASSWORD" },
						{
								// Exactly MAX_LENGTH
								new byte[Packet.BLOCK_KEY_SIZE],
								new byte[Packet.BLOCK_SIZE],
								Misc.repeat('a', 100),
								// 245 = 1 + BLOCK_KEY_SIZE + BLOCK_SIZE + 2 +
								// 100 + x
								// => x = 142 - (BLOCK_KEY_SIZE + BLOCK_SIZE)
								Misc.repeat(
										'b',
										142 - (Packet.BLOCK_KEY_SIZE + Packet.BLOCK_SIZE)) } });
	}

	/**
	 * Test method for
	 * {@link AuthenticationRequest#AuthenticationRequest(byte[], byte[], String, String)}
	 * .
	 */
	@Test
	public void testAuthenticationRequest() {
		try {
			new AuthenticationRequest(null, iv, user, password);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Key is null", null);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		try {
			new AuthenticationRequest(key, null, user, password);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Iv is null", null);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		try {
			new AuthenticationRequest(key, iv, null, password);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException(
					"User or password is null", null);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		try {
			new AuthenticationRequest(key, iv, user, null);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException(
					"User or password is null", null);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		try {
			new AuthenticationRequest(new byte[0], iv, user, password);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Key has wrong length",
					new byte[0]);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		try {
			new AuthenticationRequest(key, new byte[0], user, password);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Iv has wrong length",
					new byte[0]);
			assertEquals(ex.getMessage(), e.getMessage());
		}
	}

	/**
	 * Test method for {@link AuthenticationRequest#pack()}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testPack() throws Exception {
		// Test by packing followed by unpacking
		AuthenticationRequest request1 = new AuthenticationRequest(key, iv,
				user, password);
		byte[] data = request1.pack().getData();
		AuthenticationRequest request2 = AuthenticationRequest.unpack(data);
		assertEquals(Message.AUTHENTICATION_REQUEST, data[0]);
		assertEquals(user, request1.getUser());
		assertEquals(password, request1.getPassword());
		assertArrayEquals(key, request1.getKey());
		assertArrayEquals(iv, request1.getIv());
		// Check that they are the same
		assertEquals(request1.getUser(), request2.getUser());
		assertEquals(request1.getPassword(), request2.getPassword());
		assertArrayEquals(request1.getKey(), request2.getKey());
		assertArrayEquals(request1.getIv(), request2.getIv());

		// Try to pack with too long lengths
		try {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < 30; i++) {
				buffer.append("too long");
			}
			AuthenticationRequest request3 = new AuthenticationRequest(key, iv,
					buffer.toString(), password);
			request3.pack();
			fail("Did not throw an exception");
		} catch (PacketException e) {
			assertThat(e.getMessage(), startsWith("Length sum too big "));
		}
	}

	/**
	 * Test method for {@link AuthenticationRequest#unpack(byte[])}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testUnpack() throws Exception {
		// Check that it throws when it is too short
		byte[] data = new byte[0];
		try {
			AuthenticationRequest.unpack(data);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Unexpected length", data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		// Check that it throws when it is too long
		data = new byte[AuthenticationRequest.MAX_LENGTH + 1];
		try {
			AuthenticationRequest.unpack(data);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Unexpected length", data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		// Correct length should not throw
		data = new byte[AuthenticationRequest.MAX_LENGTH];
		AuthenticationRequest request = AuthenticationRequest.unpack(data);
		assertArrayEquals(new byte[Packet.BLOCK_KEY_SIZE], request.getKey());
		assertArrayEquals(new byte[Packet.BLOCK_SIZE], request.getIv());
		assertEquals("", request.getUser());
		assertEquals("", request.getPassword());

		// Try to unpack with too long lengths
		try {
			data[Packet.BLOCK_KEY_SIZE + Packet.BLOCK_SIZE + 1] = (byte) 0xFF;
			AuthenticationRequest.unpack(data);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Length sum too big", data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
	}

	/**
	 * Test method for {@link AuthenticationRequest#getType()}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testGetType() throws Exception {
		// Ensure it has the correct type
		AuthenticationRequest request = new AuthenticationRequest(key, iv,
				user, password);
		assertEquals(Message.AUTHENTICATION_REQUEST, request.getType());
	}

	/**
	 * Test method for {@link AuthenticationRequest#compareTo(Message)}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testCompareTo() throws Exception {
		AuthenticationRequest request = new AuthenticationRequest(key, iv,
				user, password);
		try {
			request.compareTo(null);
			fail("Did not throw an exception");
		} catch (NullPointerException e) {
		}
		try {
			request.compareTo(new Ping(false));
			fail("Did not throw an exception");
		} catch (ClassCastException e) {
		}

		// Check against object with another key
		AuthenticationRequest other = new AuthenticationRequest(
				Misc.getSequence(500, Packet.BLOCK_KEY_SIZE), iv, user,
				password);
		assertArrayEquals(request.getIv(), other.getIv());
		assertEquals(request.getUser(), other.getUser());
		assertEquals(request.getPassword(), other.getPassword());
		assertNotEquals(0, request.compareTo(other));

		// Check against object with another iv
		other = new AuthenticationRequest(key, Misc.getSequence(500,
				Packet.BLOCK_SIZE), user, password);
		assertArrayEquals(request.getKey(), other.getKey());
		assertEquals(request.getUser(), other.getUser());
		assertEquals(request.getPassword(), other.getPassword());
		assertNotEquals(0, request.compareTo(other));

		// Check against object with another user
		other = new AuthenticationRequest(key, iv, user + "a", password);
		assertArrayEquals(request.getKey(), other.getKey());
		assertArrayEquals(request.getIv(), other.getIv());
		assertEquals(request.getPassword(), other.getPassword());
		assertNotEquals(0, request.compareTo(other));

		// Check against object with another password
		other = new AuthenticationRequest(key, iv, user, password + "a");
		assertArrayEquals(request.getKey(), other.getKey());
		assertArrayEquals(request.getIv(), other.getIv());
		assertEquals(request.getUser(), other.getUser());
		assertNotEquals(0, request.compareTo(other));

		// Compare to self
		assertEquals(0, request.compareTo(request));
	}
}
