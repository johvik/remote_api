package remote.api.commands;

import remote.api.exceptions.PacketException;

public class KeyRelease extends Command {
	public static final int LENGTH = 5;
	private int keycode;

	public KeyRelease(int keycode) {
		this.keycode = keycode;
	}

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
