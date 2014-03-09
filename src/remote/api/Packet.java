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

/**
 * A class representing a data packet.
 */
public class Packet {
	/**
	 * The secure algorithm to use for authentication.
	 */
	public static final String SECURE_ALGORITHM = "RSA";
	/**
	 * Length of the secure algorithms key.
	 */
	public static final int SECURE_KEY_SIZE = 2048;
	/**
	 * The block cipher algorithm.
	 */
	public static final String BLOCK_CIPHER = "Blowfish";
	/**
	 * Length of the block algorithms key.
	 */
	public static final int BLOCK_KEY_SIZE = 8;

	/**
	 * Data of the packet.
	 */
	private byte[] data;
	/**
	 * Indicates if the packet is encrypted or decrypted.
	 */
	private boolean encrypted;

	/**
	 * Constructs a non encrypted packet.
	 * 
	 * @param data
	 *            The data for the packet.
	 * @throws PacketException
	 *             If data is null.
	 */
	public Packet(byte[] data) throws PacketException {
		this(data, false);
	}

	/**
	 * Constructs a packet.
	 * 
	 * @param data
	 *            The data for the packet.
	 * @param encrypted
	 *            True if the packet is encrypted.
	 * @throws PacketException
	 *             If data is null.
	 */
	public Packet(byte[] data, boolean encrypted) throws PacketException {
		if (data == null) {
			throw new PacketException("Data is null", data);
		}
		this.data = data;
		this.encrypted = encrypted;
	}

	/**
	 * Decrypts the packet if encrypted with the given cipher.
	 * 
	 * @param cipher
	 *            Cipher to use for decryption.
	 * @throws PacketException
	 *             If decryption fails.
	 */
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

	/**
	 * Encrypts the packet if decrypted with the given cipher.
	 * 
	 * @param cipher
	 *            Cipher to use for encryption.
	 * @throws PacketException
	 *             If encryption fails.
	 */
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
	 *            The data to read from.
	 * @param off
	 *            Start position in data.
	 * @param len
	 *            Length of the data.
	 * @return A packet or null if not enough data.
	 * @throws PacketException
	 *             If the offset or length is invalid.
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
	 * Same as {@link #read(byte[], int, int)} with offset as 0 and length as
	 * data.length.
	 * 
	 * @param data
	 *            The data to read from.
	 * @return A packet or null if not enough data.
	 * @throws PacketException
	 *             If the offset or length is invalid.
	 */
	public static Packet read(byte[] data) throws PacketException {
		return read(data, 0, data.length);
	}

	/**
	 * Writes the packet data to the output including size bytes and encrypts if
	 * needed.
	 * 
	 * @param cipher
	 *            The cipher to use for encryption.
	 * @param output
	 *            Output stream where data is written.
	 * @throws PacketException
	 *             If it fails to encrypt.
	 * @throws IOException
	 *             If it fails to write to the output.
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
	 *            The cipher used to decrypt the data.
	 * @return The transformed message.
	 * @throws PacketException
	 *             If it fails to decrypt or unpack the packet.
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

	/**
	 * Gets the length of the data in the packet.
	 * 
	 * @return Length of the data.
	 */
	public int length() {
		return data.length;
	}

	/**
	 * Returns the data of the packet.
	 * 
	 * @return The data.
	 */
	public byte[] getData() {
		return data;
	}

	@Override
	public String toString() {
		return Utils.toHex(data);
	}
}
