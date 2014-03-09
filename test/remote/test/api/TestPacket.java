package remote.test.api;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.junit.Test;

import remote.api.Packet;
import remote.api.Utils;
import remote.api.exceptions.PacketException;
import remote.api.messages.AuthenticationRequest;
import remote.api.messages.AuthenticationResponse;
import remote.api.messages.Message;
import remote.api.messages.Ping;

/**
 * Test class for {@link Packet}.
 */
public class TestPacket {
	/**
	 * Test method for {@link Packet#Packet(byte[])}.
	 * 
	 * @throws PacketException
	 *             If something went wrong.
	 */
	@Test
	public void testPacket() throws PacketException {
		try {
			new Packet(null);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Data is null", null);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		// Valid construction
		new Packet(new byte[] { 0 });
	}

	/**
	 * Test method for {@link Packet#read(byte[])} and
	 * {@link Packet#read(byte[], int, int)}.
	 * 
	 * @throws PacketException
	 *             If something went wrong.
	 */
	@Test
	public void testRead() throws PacketException {
		byte[] reference = Misc.getSequence(1, 8);
		byte[] buffer = new byte[10];
		System.arraycopy(reference, 0, buffer, 2, reference.length);

		// Test max length
		int length = Message.MAX_LENGTH + 1;
		buffer[0] = (byte) ((length >> 8) & 0xFF);
		buffer[1] = (byte) (length & 0xFF);
		try {
			Packet.read(buffer);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Message too long", buffer);
			assertEquals(ex.getMessage(), e.getMessage());
		}

		// Test not enough data
		length = 16;
		buffer[0] = (byte) ((length >> 8) & 0xFF);
		buffer[1] = (byte) (length & 0xFF);
		Packet packet = Packet.read(buffer);
		assertEquals(null, packet);

		// Test correct data
		length = 8;
		buffer[0] = (byte) ((length >> 8) & 0xFF);
		buffer[1] = (byte) (length & 0xFF);
		packet = Packet.read(buffer);
		assertArrayEquals(reference, packet.getData());

		// Test different offset and length
		for (int i = 0; i < 8; i++) {
			length = 8 - i;
			buffer[i] = (byte) ((length >> 8) & 0xFF);
			buffer[i + 1] = (byte) (length & 0xFF);
			packet = Packet.read(buffer, i, length + 2);
			assertArrayEquals(Misc.getSequence(i + 1, length), packet.getData());
		}

		// Test invalid offset and length
		try {
			Packet.read(buffer, 1, buffer.length);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Length less than: " + 1
					+ " + " + buffer.length, buffer);
			assertEquals(ex.getMessage(), e.getMessage());
		}

		// Test invalid offset and length
		try {
			Packet.read(buffer, 0, buffer.length + 1);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Length less than: " + 0
					+ " + " + (buffer.length + 1), buffer);
			assertEquals(ex.getMessage(), e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link Packet#write(javax.crypto.Cipher, java.io.OutputStream)}.
	 * 
	 * @throws PacketException
	 *             If something went wrong.
	 * @throws IOException
	 *             If something went wrong.
	 */
	@Test
	public void testWrite() throws PacketException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] data = Misc.getSequence(1, 8);
		byte[] reference = new byte[10];
		reference[1] = 8; // Length
		System.arraycopy(data, 0, reference, 2, data.length);

		Packet packet = new Packet(data, true);
		packet.write(null, output);
		assertArrayEquals(reference, output.toByteArray());
	}

	/**
	 * Test method for block encryption in {@link Packet}.
	 * 
	 * @throws GeneralSecurityException
	 *             If something went wrong.
	 * 
	 * @throws PacketException
	 *             If something went wrong.
	 * @throws IOException
	 *             If something went wrong.
	 */
	@Test
	public void testBlockEncryption() throws GeneralSecurityException,
			PacketException, IOException {
		// Write the data
		Ping ping = new Ping(true);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Packet packet = ping.pack();
		byte[] oldData = packet.getData();
		packet.write(Misc.blockEncrypt, output);

		byte[] outData = output.toByteArray();
		// Should not be the same after encrypting
		assertThat(oldData, not(equalTo(packet.getData())));

		// Read the data
		packet = Packet.read(outData);
		Message message = packet.decode(Misc.blockDecrypt);
		assertArrayEquals(packet.getData(), oldData);
		assertEquals(Ping.class, message.getClass());
		assertEquals(true, ((Ping) message).isRequest());
	}

	/**
	 * Test method for secure encryption in {@link Packet}.
	 * 
	 * @throws GeneralSecurityException
	 *             If something went wrong.
	 * 
	 * @throws PacketException
	 *             If something went wrong.
	 * @throws IOException
	 *             If something went wrong.
	 */
	@Test
	public void testSecureEncryption() throws GeneralSecurityException,
			PacketException, IOException {
		// Write the data
		byte[] key = Misc.getSequence(1, Packet.BLOCK_KEY_SIZE);
		String user = "user";
		String password = "password";
		AuthenticationRequest request = new AuthenticationRequest(key, user,
				password);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Packet packet = request.pack();
		byte[] oldData = packet.getData();
		packet.write(Misc.secureEncrypt, output);

		byte[] outData = output.toByteArray();
		// Should not be the same after encrypting
		assertThat(oldData, not(equalTo(packet.getData())));

		// Read the data
		packet = Packet.read(outData);
		Message message = packet.decode(Misc.secureDecrypt);
		assertArrayEquals(packet.getData(), oldData);
		assertEquals(AuthenticationRequest.class, message.getClass());
		assertArrayEquals(key, ((AuthenticationRequest) message).getKey());
		assertEquals(user, ((AuthenticationRequest) message).getUser());
		assertEquals(password, ((AuthenticationRequest) message).getPassword());
	}

	/**
	 * Test method for when encoding fails in {@link Packet}.
	 * 
	 * @throws IOException
	 *             If something went wrong.
	 */
	@Test
	public void testEncodeFail() throws IOException {
		byte[] data = new byte[1];
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			// Try with decryption cipher...
			new Packet(data).write(Misc.blockDecrypt, output);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException(
					"Failed to encrypt packet", data);
			assertThat(e.getMessage(), startsWith(ex.getMessage()));
		}
	}

	/**
	 * Test method for when decoding fails in {@link Packet}.
	 */
	@Test
	public void testDecodeFail() {
		byte[] data = new byte[1];
		try {
			new Packet(data, true).decode(Misc.blockDecrypt);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException(
					"Failed to decrypt packet", data);
			assertThat(e.getMessage(), startsWith(ex.getMessage()));
		}
	}

	/**
	 * Test method for {@link Packet#decode(javax.crypto.Cipher)}.
	 * 
	 * @throws PacketException
	 *             If something went wrong.
	 */
	@Test
	public void testDecodeAuthenticationRequest() throws PacketException {
		byte[] data = new byte[AuthenticationRequest.MAX_LENGTH];
		data[0] = Message.AUTHENTICATION_REQUEST;
		Message message = new Packet(data).decode(null);
		assertEquals(AuthenticationRequest.class, message.getClass());
		assertEquals(Message.AUTHENTICATION_REQUEST, message.getType());
	}

	/**
	 * Test method for {@link Packet#decode(javax.crypto.Cipher)}.
	 * 
	 * @throws PacketException
	 *             If something went wrong.
	 */
	@Test
	public void testDecodeAuthenticationResponse() throws PacketException {
		byte[] data = new byte[AuthenticationResponse.LENGTH];
		data[0] = Message.AUTHENTICATION_RESPONSE;
		Message message = new Packet(data).decode(null);
		assertEquals(AuthenticationResponse.class, message.getClass());
		assertEquals(Message.AUTHENTICATION_RESPONSE, message.getType());
	}

	/**
	 * Test method for {@link Packet#decode(javax.crypto.Cipher)}.
	 * 
	 * @throws PacketException
	 *             If something went wrong.
	 */
	@Test
	public void testDecodePing() throws PacketException {
		byte[] data = new byte[Ping.LENGTH];
		data[0] = Message.PING;
		Message message = new Packet(data).decode(null);
		assertEquals(Ping.class, message.getClass());
		assertEquals(Message.PING, message.getType());
	}

	/**
	 * Test method for {@link Packet#decode(javax.crypto.Cipher)}.
	 */
	@Test
	public void testDecodeUnknown() {
		int codes = 0; // Count number of correct codes
		byte[] data = new byte[8];
		for (int i = 0; i <= 0xFF; i++) {
			data[0] = (byte) i;
			try {
				new Packet(data).decode(null);
				codes++; // OK and correct length
			} catch (PacketException e) {
				String message = e.getMessage();
				if (message.indexOf("Unexpected length") == 0) {
					codes++; // OK but wrong length
					PacketException ex = new PacketException(
							"Unexpected length", data);
					assertEquals(ex.getMessage(), message);
				} else {
					// Not OK, wrong code
					PacketException ex = new PacketException("Unknown message",
							data);
					assertEquals(ex.getMessage(), message);
				}
			}
		}
		assertEquals(Message.USED_CODES, codes);
	}

	/**
	 * Test method for {@link Packet#length()}.
	 * 
	 * @throws PacketException
	 *             If something went wrong.
	 */
	@Test
	public void testLength() throws PacketException {
		int length = 8;
		byte[] data = new byte[length];
		Packet packet = new Packet(data);
		assertEquals(length, packet.length());
	}

	/**
	 * Test method for {@link Packet#getData()}.
	 * 
	 * @throws PacketException
	 *             If something went wrong.
	 */
	@Test
	public void testGetData() throws PacketException {
		int length = 8;
		byte[] data = Misc.getSequence(1, length);
		Packet packet = new Packet(data);
		assertArrayEquals(data, packet.getData());
	}

	/**
	 * Test method for {@link Packet#toString()}.
	 * 
	 * @throws PacketException
	 *             If something went wrong.
	 */
	@Test
	public void testToString() throws PacketException {
		int length = 8;
		byte[] data = Misc.getSequence(1, length);
		Packet packet = new Packet(data);
		assertEquals(Utils.toHex(data), packet.toString());
	}
}
