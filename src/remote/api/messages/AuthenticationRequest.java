package remote.api.messages;

import remote.api.exceptions.PacketException;
import remote.api.Message;
import remote.api.Packet;

public class AuthenticationRequest implements Message {
	private static final int MIN_LENGTH = calculateSize(0, 0);
	public static final int MAX_LENGTH = 245; // max size for 2048 bit RSA key
	private byte[] key;
	private String user;
	private String password;

	public AuthenticationRequest(byte[] key, String user, String password)
			throws PacketException {
		if (key == null || user == null || password == null) {
			throw new PacketException("Null input data", key);
		}
		if (key.length != Packet.BLOCK_KEY_SIZE) {
			throw new PacketException("Wrong key length", key);
		}
		this.key = key;
		this.user = user;
		this.password = password;
	}

	private static int calculateSize(int user, int password) {
		// One byte for type
		// BLOCK_KEY_SIZE for key
		// Two bytes for user + password length bytes
		// Length of user + password
		return 1 + Packet.BLOCK_KEY_SIZE + 2 + user + password;
	}

	@Override
	public Packet pack() throws PacketException {
		byte[] userBytes = user.getBytes();
		byte[] passwordBytes = password.getBytes();
		int userLength = userBytes.length;
		int passwordLength = passwordBytes.length;
		// Check length
		int size = calculateSize(userLength, passwordLength);
		if (size > MAX_LENGTH) {
			throw new PacketException("Length sum too big " + size, null);
		}
		byte[] data = new byte[size];

		int pos = 0;
		data[pos++] = Message.AUTHENTICATION_REQUEST;
		// Write key
		System.arraycopy(key, 0, data, pos, Packet.BLOCK_KEY_SIZE);
		pos += Packet.BLOCK_KEY_SIZE;

		// Write lengths
		data[pos++] = (byte) (userLength & 0xFF);
		data[pos++] = (byte) (passwordLength & 0xFF);

		// Write user
		System.arraycopy(userBytes, 0, data, pos, userLength);
		pos += userLength;

		// Write password
		System.arraycopy(passwordBytes, 0, data, pos, passwordLength);
		pos += passwordLength;

		return new Packet(data);
	}

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

		String userString = new String(user);
		String passwordString = new String(password);
		return new AuthenticationRequest(key, userString, passwordString);
	}

	@Override
	public byte getType() {
		return Message.AUTHENTICATION_REQUEST;
	}

	public byte[] getKey() {
		return key;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}
}
