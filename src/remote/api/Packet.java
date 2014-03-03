package remote.api;

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import remote.api.exceptions.PacketException;
import remote.api.messages.AuthenticationRequest;
import remote.api.messages.AuthenticationResponse;
import remote.api.messages.CommandRequest;
import remote.api.messages.Message;
import remote.api.messages.Ping;

public class Packet {
	public static final String SECURE_ALGORITHM = "RSA";
	public static final int SECURE_KEY_SIZE = 2048;
	public static final String BLOCK_CIPHER = "Blowfish";
	public static final int BLOCK_KEY_SIZE = 8;

	private byte[] data;
	private boolean encrypted;

	/**
	 * Constructs a non encrypted.
	 * 
	 * @param data
	 * @throws PacketException
	 */
	public Packet(byte[] data) throws PacketException {
		this(data, false);
	}

	/**
	 * Constructs a packet.
	 * 
	 * @param data
	 * @param encrypted
	 * @throws PacketException
	 */
	public Packet(byte[] data, boolean encrypted) throws PacketException {
		if (data == null) {
			throw new PacketException("Data is null", data);
		}
		this.data = data;
		this.encrypted = encrypted;
	}

	private void decrypt(Cipher cipher) throws PacketException {
		if (encrypted) {
			try {
				data = cipher.doFinal(data);
				encrypted = false;
			} catch (GeneralSecurityException e) {
				throw new PacketException("Failed to decrypt packet", data, e);
			}
		}
	}

	private void encrypt(Cipher cipher) throws PacketException {
		if (!encrypted) {
			try {
				data = cipher.doFinal(data);
				encrypted = true;
			} catch (GeneralSecurityException e) {
				throw new PacketException("Failed to encrypt packet", data, e);
			}
		}
	}

	/**
	 * Attempts to read a packet from the data.
	 * 
	 * @param data
	 * @param off
	 * @param len
	 * @return A packet or null if not enough data.
	 * @throws PacketException
	 */
	public static Packet read(byte[] data, int off, int len)
			throws PacketException {
		int packetLength = ((data[off] & 0xFF) << 8) | (data[off + 1] & 0xFF);
		if (packetLength > Message.MAX_LENGTH) {
			throw new PacketException("Message too long", data);
		} else if (data.length < off + len) {
			throw new PacketException("Length less than: " + off + " + " + len,
					data);
		} else if (len >= packetLength + 2) {
			// Copy data to new array
			byte[] packetData = new byte[packetLength];
			System.arraycopy(data, 2 + off, packetData, 0, packetLength);

			return new Packet(packetData, true);
		}
		// Not enough data available
		return null;
	}

	/**
	 * Same as read with offset as 0 and length as data.length.
	 * 
	 * @param data
	 * @return A packet or null if not enough data.
	 * @throws PacketException
	 */
	public static Packet read(byte[] data) throws PacketException {
		return read(data, 0, data.length);
	}

	/**
	 * Writes the packet data to the output including size bytes and encrypts if
	 * needed.
	 * 
	 * @param cipher
	 * @param output
	 * @throws PacketException
	 * @throws IOException
	 */
	public void write(Cipher cipher, OutputStream output)
			throws PacketException, IOException {
		encrypt(cipher);
		int length = data.length;
		output.write((length >> 8) & 0xFF);
		output.write(length & 0xFF);
		output.write(data);
	}

	/**
	 * Transforms the packet to a message and decrypts if needed.
	 * 
	 * @param cipher
	 * @return The transformed message
	 * @throws PacketException
	 */
	public Message decode(Cipher cipher) throws PacketException {
		decrypt(cipher);
		byte type = data[0];
		switch (type) {
		case Message.AUTHENTICATION_REQUEST:
			return AuthenticationRequest.unpack(data);
		case Message.AUTHENTICATION_RESPONSE:
			return AuthenticationResponse.unpack(data);
		case Message.PING:
			return Ping.unpack(data);
		case Message.COMMAND_REQUEST:
			return CommandRequest.unpack(data);
		}
		throw new PacketException("Unknown message", data);
	}

	public int length() {
		return data.length;
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public String toString() {
		return Utils.toHex(data);
	}
}
