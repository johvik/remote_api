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
	private static final int BLOCK_SIZE = 8;

	private byte[] data;
	private boolean encrypted;

	/**
	 * Constructs a non encrypted and expects a normal sized packet.
	 * 
	 * @param data
	 * @throws PacketException
	 */
	public Packet(byte[] data) throws PacketException {
		this(data, false, false);
	}

	/**
	 * Constructs a non encrypted packet with optional special length.
	 * 
	 * @param data
	 * @param specialLength
	 *            If packet is allowed to have a length that does not match the
	 *            block size
	 * @throws PacketException
	 */
	public Packet(byte[] data, boolean specialLength) throws PacketException {
		// TODO Is it better to let the encrypt throw an exception instead?
		this(data, specialLength, false);
	}

	public Packet(byte[] data, boolean specialLength, boolean encrypted)
			throws PacketException {
		if (data == null) {
			throw new PacketException("Data is null", data);
		} else if ((!specialLength || encrypted)
				&& (data.length % BLOCK_SIZE) != 0) {
			// Only non encrypted packets may have a special length
			throw new PacketException("Invalid data length", data);
		}
		this.data = data;
		this.encrypted = encrypted;
	}

	/**
	 * Calculates the expected packet size given a length.
	 * 
	 * @param length
	 * @return The size of a packet with the specified length including padding
	 *         to fill a block.
	 */
	public static int getPacketSize(int length) {
		int padding = length % BLOCK_SIZE;
		if (padding != 0) {
			padding = BLOCK_SIZE - padding;
		}
		return length + padding;
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
		} else if (length % BLOCK_SIZE != 0) {
			throw new PacketException("Bad message length", data);
		} else if (data.length >= length + 2) {
			// Copy data to new array
			byte[] packetData = new byte[length];
			System.arraycopy(data, 2, packetData, 0, length);

			return new Packet(packetData, false, true);
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
