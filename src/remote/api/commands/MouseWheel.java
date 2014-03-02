package remote.api.commands;

import remote.api.exceptions.PacketException;

public class MouseWheel extends Command {
	public static final int LENGTH = 5;
	private int wheelAmt;

	public MouseWheel(int wheelAmt) {
		this.wheelAmt = wheelAmt;
	}

	public int getWheelAmt() {
		return wheelAmt;
	}

	@Override
	public int compareTo(Command o) {
		MouseWheel other = (MouseWheel) o;
		return Integer.valueOf(wheelAmt).compareTo(other.wheelAmt);
	}

	@Override
	public void write(byte[] data, int offset) throws PacketException {
		if (offset < 0 || data.length < LENGTH + offset) {
			throw new PacketException("Invalid write " + offset, data);
		}
		data[offset] = MOUSE_WHEEL;
		data[offset + 1] = (byte) ((wheelAmt >> 24) & 0xFF);
		data[offset + 2] = (byte) ((wheelAmt >> 16) & 0xFF);
		data[offset + 3] = (byte) ((wheelAmt >> 8) & 0xFF);
		data[offset + 4] = (byte) (wheelAmt & 0xFF);
	}

	public static MouseWheel read(byte[] data, int offset)
			throws PacketException {
		if (offset < 0 || data.length < LENGTH + offset) {
			throw new PacketException("Invalid read " + offset, data);
		}
		// First byte is type
		int buttons = ((data[offset + 1] & 0xFF) << 24)
				| ((data[offset + 2] & 0xFF) << 16)
				| ((data[offset + 3] & 0xFF) << 8) | (data[offset + 4] & 0xFF);
		return new MouseWheel(buttons);
	}

	@Override
	public int getLength() {
		return LENGTH;
	}

	@Override
	public byte getType() {
		return MOUSE_WHEEL;
	}
}
