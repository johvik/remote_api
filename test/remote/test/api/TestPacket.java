package remote.test.api;

import static org.junit.Assert.*;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;

import remote.api.Message;
import remote.api.Packet;
import remote.api.Utils;
import remote.api.exceptions.PacketException;
import remote.api.messages.AuthenticationRequest;
import remote.api.messages.AuthenticationResponse;
import remote.api.messages.Ping;

public class TestPacket {

	@Test
	public void testPacket() throws PacketException {
		try {
			new Packet(null);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			assertEquals("Data is null null", e.getMessage());
		}
		// Valid construction
		new Packet(new byte[] { 0 });
	}

	@Test
	public void testRead() throws PacketException {
		byte[] reference = new byte[8];
		for (int i = 0; i < reference.length; i++) {
			reference[i] = (byte) (i + 1);
		}
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
			assertEquals("Message too long " + Utils.toHex(buffer),
					e.getMessage());
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
	}

	@Test
	public void testWrite() throws PacketException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] data = new byte[8];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) (i + 1);
		}
		byte[] reference = new byte[10];
		reference[1] = 8; // Length
		System.arraycopy(data, 0, reference, 2, data.length);

		Packet packet = new Packet(data, true);
		packet.write(null, output);
		assertArrayEquals(reference, output.toByteArray());
	}

	@Test
	public void testBlockEncryption() throws GeneralSecurityException,
			PacketException, IOException {
		byte[] key = new byte[Packet.BLOCK_KEY_SIZE];
		for (int i = 0; i < Packet.BLOCK_KEY_SIZE; i++) {
			key[i] = (byte) (i + 1);
		}
		SecretKey secretKey = new SecretKeySpec(key, Packet.BLOCK_CIPHER);
		Cipher blockDecryptCipher = Cipher.getInstance(Packet.BLOCK_CIPHER);
		blockDecryptCipher.init(Cipher.DECRYPT_MODE, secretKey);
		Cipher blockEncryptCipher = Cipher.getInstance(Packet.BLOCK_CIPHER);
		blockEncryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);

		// Write the data
		Ping ping = new Ping(true);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Packet packet = ping.pack();
		byte[] oldData = packet.getData();
		packet.write(blockEncryptCipher, output);

		byte[] outData = output.toByteArray();
		// Should not be the same after encrypting
		assertThat(oldData, not(equalTo(packet.getData())));

		// Read the data
		packet = Packet.read(outData);
		Message message = packet.decode(blockDecryptCipher);
		assertArrayEquals(packet.getData(), oldData);
		assertEquals(Ping.class, message.getClass());
		assertEquals(true, ((Ping) message).isRequest());
	}

	@Test
	public void testSecureEncryption() {
		// TODO
	}

	@Test
	public void testDecodeAuthenticationRequest() throws PacketException {
		byte[] data = new byte[AuthenticationRequest.LENGTH];
		data[0] = Message.AUTHENTICATION_REQUEST;
		Message message = new Packet(data).decode(null);
		assertEquals(AuthenticationRequest.class, message.getClass());
		assertEquals(Message.AUTHENTICATION_REQUEST, message.getType());
	}

	@Test
	public void testDecodeAuthenticationResponse() throws PacketException {
		byte[] data = new byte[AuthenticationResponse.LENGTH];
		data[0] = Message.AUTHENTICATION_RESPONSE;
		Message message = new Packet(data).decode(null);
		assertEquals(AuthenticationResponse.class, message.getClass());
		assertEquals(Message.AUTHENTICATION_RESPONSE, message.getType());
	}

	@Test
	public void testDecodePing() throws PacketException {
		byte[] data = new byte[Ping.LENGTH];
		data[0] = Message.PING;
		Message message = new Packet(data).decode(null);
		assertEquals(Ping.class, message.getClass());
		assertEquals(Message.PING, message.getType());
	}

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
					assertEquals("Unexpected length " + Utils.toHex(data),
							e.getMessage());
				} else {
					// Not OK, wrong code
					assertEquals("Unknown message " + Utils.toHex(data),
							e.getMessage());
				}
			}
		}
		assertEquals(Message.USED_CODES, codes);
	}

	@Test
	public void testLength() throws PacketException {
		int length = 8;
		byte[] data = new byte[length];
		Packet packet = new Packet(data);
		assertEquals(length, packet.length());
	}

	@Test
	public void testGetData() throws PacketException {
		int length = 8;
		byte[] data = new byte[length];
		for (int i = 0; i < length; i++) {
			data[i] = (byte) (i + 1);
		}
		Packet packet = new Packet(data);
		assertArrayEquals(data, packet.getData());
	}

	@Test
	public void testToString() throws PacketException {
		int length = 8;
		byte[] data = new byte[length];
		for (int i = 0; i < length; i++) {
			data[i] = (byte) (i + 1);
		}
		Packet packet = new Packet(data);
		assertEquals(Utils.toHex(data), packet.toString());
	}
}
