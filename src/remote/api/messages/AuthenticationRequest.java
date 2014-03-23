package remote.api.messages;

import remote.api.exceptions.PacketException;
import remote.api.Packet;
import remote.api.Utils;

/**
 * A class representing an authentication request containing a key to be used
 * for the block cipher and a user and a password.
 */
public class AuthenticationRequest extends Message {
	/**
	 * The minimum length required if sending a empty user and password.
	 */
	private static final int MIN_LENGTH = calculateSize(0, 0);
	/**
	 * The maximum length of the message, limited by the secure algorithm.
	 */
	public static final int MAX_LENGTH = 245; // max size for 2048 bit RSA key

	/**
	 * Key for the block cipher.
	 */
	private byte[] key;
	/**
	 * Initialization vector for block cipher.
	 */
	private byte[] iv;
	/**
	 * Base64 encoded user to authenticate.
	 */
	private byte[] user;
	/**
	 * Base64 encoded password for the user.
	 */
	private byte[] password;

	/**
	 * Constructs a new authentication request.
	 * 
	 * @param key
	 *            Key to use for the block cipher.
	 * @param iv
	 *            Initialization vector for the block cipher.
	 * @param user
	 *            Base64 encoded user to authenticate.
	 * @param password
	 *            Base64 encoded password for the user.
	 * @throws PacketException
	 *             If any of the arguments is null or the key has wrong length.
	 */
	public AuthenticationRequest(byte[] key, byte[] iv, byte[] user,
			byte[] password) throws PacketException {
		if (key == null) {
			throw new PacketException("Key is null", key);
		}
		if (iv == null) {
			throw new PacketException("Iv is null", iv);
		}
		if (user == null || password == null) {
			throw new PacketException("User or password is null", null);
		}
		if (key.length != Packet.BLOCK_KEY_SIZE) {
			throw new PacketException("Key has wrong length", key);
		}
		if (iv.length != Packet.BLOCK_SIZE) {
			throw new PacketException("Iv has wrong length", iv);
		}
		this.key = key;
		this.iv = iv;
		this.user = user;
		this.password = password;
	}

	/**
	 * Calculates the size of a message.
	 * 
	 * @param user
	 *            Length of the user.
	 * @param password
	 *            Length of the password.
	 * @return The total length of the message.
	 */
	private static int calculateSize(int user, int password) {
		// One byte for type
		// BLOCK_KEY_SIZE for key
		// BLOCK_SIZE for iv
		// Two bytes for user + password length bytes
		// Length of user + password
		return 1 + Packet.BLOCK_KEY_SIZE + Packet.BLOCK_SIZE + 2 + user
				+ password;
	}

	@Override
	public Packet pack() throws PacketException {
		int userLength = user.length;
		int passwordLength = password.length;
		// Check length
		int size = calculateSize(userLength, passwordLength);
		if (size > MAX_LENGTH) {
			throw new PacketException("Length sum too big " + size, null);
		}
		byte[] data = new byte[size];

		int pos = 0;
		data[pos++] = AUTHENTICATION_REQUEST;
		// Write key
		System.arraycopy(key, 0, data, pos, Packet.BLOCK_KEY_SIZE);
		pos += Packet.BLOCK_KEY_SIZE;

		// Write iv
		System.arraycopy(iv, 0, data, pos, Packet.BLOCK_SIZE);
		pos += Packet.BLOCK_SIZE;

		// Write lengths
		data[pos++] = (byte) (userLength & 0xFF);
		data[pos++] = (byte) (passwordLength & 0xFF);

		// Write user
		System.arraycopy(user, 0, data, pos, userLength);
		pos += userLength;

		// Write password
		System.arraycopy(password, 0, data, pos, passwordLength);
		pos += passwordLength;

		return new Packet(data);
	}

	/**
	 * Attempts to read an authentication request from data.
	 * 
	 * @param data
	 *            The data to read from.
	 * @return The authentication request read.
	 * @throws PacketException
	 *             If the length is incorrect or if the user plus password
	 *             length is too large.
	 */
	public static AuthenticationRequest unpack(byte[] data)
			throws PacketException {
		int length = data.length;
		if (length < MIN_LENGTH || length > MAX_LENGTH) {
			throw new PacketException("Unexpected length", data);
		}

		byte[] key = new byte[Packet.BLOCK_KEY_SIZE];
		int pos = 1; // skip type byte
		// First BLOCK_KEY_SIZE contains the key
		System.arraycopy(data, pos, key, 0, Packet.BLOCK_KEY_SIZE);
		pos += Packet.BLOCK_KEY_SIZE;

		// Next BLOCK_SIZE contains the iv
		byte[] iv = new byte[Packet.BLOCK_SIZE];
		System.arraycopy(data, pos, iv, 0, Packet.BLOCK_SIZE);
		pos += Packet.BLOCK_SIZE;

		// One byte with user length
		int userLength = data[pos++] & 0xFF;
		// One byte with password length
		int passwordLength = data[pos++] & 0xFF;
		// Check length
		if (calculateSize(userLength, passwordLength) > MAX_LENGTH) {
			throw new PacketException("Length sum too big", data);
		}

		// Rest is user + password bytes
		byte[] user = new byte[userLength];
		// Read user
		System.arraycopy(data, pos, user, 0, userLength);
		pos += userLength;

		byte[] password = new byte[passwordLength];
		// Read password
		System.arraycopy(data, pos, password, 0, passwordLength);
		pos += passwordLength;

		return new AuthenticationRequest(key, iv, user, password);
	}

	@Override
	public byte getType() {
		return AUTHENTICATION_REQUEST;
	}

	/**
	 * Gets the block cipher key.
	 * 
	 * @return The key.
	 */
	public byte[] getKey() {
		return key;
	}

	/**
	 * Gets the initialization vector for the block cipher.
	 * 
	 * @return The iv.
	 */
	public byte[] getIv() {
		return iv;
	}

	/**
	 * Gets the user of the request.
	 * 
	 * @return The user.
	 */
	public byte[] getUser() {
		return user;
	}

	/**
	 * Gets the password of the request.
	 * 
	 * @return The password.
	 */
	public byte[] getPassword() {
		return password;
	}

	@Override
	public int compareTo(Message o) {
		AuthenticationRequest other = (AuthenticationRequest) o;
		int cmp = Utils.compare(key, other.key);
		if (cmp == 0) {
			cmp = Utils.compare(iv, other.iv);
			if (cmp == 0) {
				cmp = Utils.compare(user, other.user);
				if (cmp == 0) {
					cmp = Utils.compare(password, other.password);
				}
			}
		}
		return cmp;
	}
}
