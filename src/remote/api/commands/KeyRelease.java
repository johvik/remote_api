package remote.api.commands;

import remote.api.exceptions.PacketException;

/**
 * Class for a key release.
 */
public class KeyRelease extends Command {
	/**
	 * Number of bytes needed when writing.
	 */
	public static final int LENGTH = 5;

	/**
	 * Key code of the KeyRelease.
	 */
	private int keycode;

	/**
	 * Constructs a new KeyRelease.
	 * 
	 * @param keycode
	 *            Key code of the release.
	 */
	public KeyRelease(int keycode) {
		this.keycode = keycode;
	}

	/**
	 * Retrieves the key code of the command.
	 * 
	 * @return The key code.
	 */
	public int getKeycode() {
		return keycode;
	}

	@Override
	public int compareTo(Command o) {
		KeyRelease other = (KeyRelease) o;
		return Integer.valueOf(keycode).compareTo(other.keycode);
	}

	@Override
	public void write(byte[] data, int offset) throws PacketException {
		if (offset < 0 || data.length < LENGTH + offset) {
			throw new PacketException("Invalid write " + offset, data);
		}
		data[offset] = KEY_RELEASE;
		data[offset + 1] = (byte) ((keycode >> 24) & 0xFF);
		data[offset + 2] = (byte) ((keycode >> 16) & 0xFF);
		data[offset + 3] = (byte) ((keycode >> 8) & 0xFF);
		data[offset + 4] = (byte) (keycode & 0xFF);
	}

	/**
	 * Attempts to read a KeyRelease from data.
	 * 
	 * @param data
	 *            The data to read.
	 * @param offset
	 *            Start offset in data.
	 * @return The read command.
	 * @throws PacketException
	 *             If offset or length of data makes the read impossible.
	 */
	public static KeyRelease read(byte[] data, int offset)
			throws PacketException {
		if (offset < 0 || data.length < LENGTH + offset) {
			throw new PacketException("Invalid read " + offset, data);
		}
		// First byte is type
		int buttons = ((data[offset + 1] & 0xFF) << 24)
				| ((data[offset + 2] & 0xFF) << 16)
				| ((data[offset + 3] & 0xFF) << 8) | (data[offset + 4] & 0xFF);
		return new KeyRelease(buttons);
	}

	@Override
	public int getLength() {
		return LENGTH;
	}

	@Override
	public byte getType() {
		return KEY_RELEASE;
	}
}
