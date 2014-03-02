package remote.api.commands;

import remote.api.exceptions.PacketException;

public class MouseRelease extends Command {
	public static final int LENGTH = 5;
	private int buttons;

	public MouseRelease(int buttons) {
		this.buttons = buttons;
	}

	public int getButtons() {
		return buttons;
	}

	@Override
	public int compareTo(Command o) {
		MouseRelease other = (MouseRelease) o;
		return Integer.valueOf(buttons).compareTo(other.buttons);
	}

	@Override
	public void write(byte[] data, int offset) throws PacketException {
		if (offset < 0 || data.length < LENGTH + offset) {
			throw new PacketException("Invalid write " + offset, data);
		}
		data[offset] = MOUSE_RELEASE;
		data[offset + 1] = (byte) ((buttons >> 24) & 0xFF);
		data[offset + 2] = (byte) ((buttons >> 16) & 0xFF);
		data[offset + 3] = (byte) ((buttons >> 8) & 0xFF);
		data[offset + 4] = (byte) (buttons & 0xFF);
	}

	public static MouseRelease read(byte[] data, int offset)
			throws PacketException {
		if (offset < 0 || data.length < LENGTH + offset) {
			throw new PacketException("Invalid read " + offset, data);
		}
		// First byte is type
		int buttons = ((data[offset + 1] & 0xFF) << 24)
				| ((data[offset + 2] & 0xFF) << 16)
				| ((data[offset + 3] & 0xFF) << 8) | (data[offset + 4] & 0xFF);
		return new MouseRelease(buttons);
	}

	@Override
	public int getLength() {
		return LENGTH;
	}

	@Override
	public byte getType() {
		return MOUSE_RELEASE;
	}
}
