package remote.api.messages;

import remote.api.exceptions.PacketException;
import remote.api.Message;
import remote.api.Packet;

public class AuthenticationRequest implements Message {
	private static final int LENGTH = 245; // 245 max size for 2048 bit RSA key
	public static final int PACKET_SIZE = LENGTH;
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
		byte[] data = new byte[PACKET_SIZE];

		int pos = 0;
		data[pos++] = Message.AUTHENTICATION_REQUEST;
		// Write key
		for (int i = 0; i < Packet.BLOCK_KEY_SIZE; i++, pos++) {
			data[pos] = key[i];
		}

		byte[] userBytes = user.getBytes();
		byte[] passwordBytes = password.getBytes();
		int userLength = userBytes.length;
		int passwordLength = passwordBytes.length;
		// Check length
		if (calculateSize(userLength, passwordLength) > PACKET_SIZE) {
			throw new PacketException("Length sum too big", data);
		}

		// Write lengths
		data[pos++] = (byte) (userLength & 0xFF);
		data[pos++] = (byte) (passwordLength & 0xFF);

		// Write user
		for (int i = 0; i < userLength; i++, pos++) {
			data[pos] = userBytes[i];
		}

		// Write password
		for (int i = 0; i < passwordLength; i++, pos++) {
			data[pos] = passwordBytes[i];
		}
		return new Packet(data, true);
	}

	public static AuthenticationRequest unpack(byte[] data)
			throws PacketException {
		if (data.length != PACKET_SIZE) {
			throw new PacketException("Unexpected length", data);
		}

		byte[] key = new byte[Packet.BLOCK_KEY_SIZE];
		int pos = 1; // skip type byte
		// First BLOCK_KEY_SIZE contains the key
		for (int i = 0; i < Packet.BLOCK_KEY_SIZE; i++, pos++) {
			key[i] = data[pos];
		}

		// One byte with user length
		int userLength = data[pos++] & 0xFF;
		// One byte with password length
		int passwordLength = data[pos++] & 0xFF;
		// Check length
		if (calculateSize(userLength, passwordLength) > PACKET_SIZE) {
			throw new PacketException("Length sum too big", data);
		}

		// Rest is user + password bytes
		byte[] user = new byte[userLength];
		// Read user
		for (int i = 0; i < userLength; i++, pos++) {
			user[i] = data[pos];
		}

		byte[] password = new byte[passwordLength];
		// Read password
		for (int i = 0; i < passwordLength; i++, pos++) {
			password[i] = data[pos];
		}

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
