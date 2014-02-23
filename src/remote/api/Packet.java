package remote.api;

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import remote.api.exceptions.PacketException;
import remote.api.messages.AuthenticationRequest;
import remote.api.messages.AuthenticationResponse;
import remote.api.messages.Ping;

public class Packet {
	public static final String SECURE_ALGORITHM = "RSA";
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
				e.printStackTrace();
				throw new PacketException("Failed to decrypt packet", data);
			}
		}
	}

	private void encrypt(Cipher cipher) throws PacketException {
		if (!encrypted) {
			try {
				data = cipher.doFinal(data);
				encrypted = true;
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
				throw new PacketException("Failed to encrypt packet", data);
			}
		}
	}

	/**
	 * Attempts to read a packet from the data.
	 * 
	 * @param data
	 * @return A packet or null if not enough data.
	 * @throws PacketException
	 */
	public static Packet read(byte[] data) throws PacketException {
		int length = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
		if (length > Message.MAX_LENGTH) {
			throw new PacketException("Message too long", data);
		} else if (data.length >= length + 2) {
			// Copy data to new array
			byte[] packetData = new byte[length];
			System.arraycopy(data, 2, packetData, 0, length);

			return new Packet(packetData, true);
		}
		// Not enough data available
		return null;
	}

	public void write(Cipher cipher, OutputStream output)
			throws PacketException, IOException {
		encrypt(cipher);
		int length = data.length;
		output.write((length >> 8) & 0xFF);
		output.write(length & 0xFF);
		output.write(data);
	}

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
