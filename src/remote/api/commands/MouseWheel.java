package remote.api.commands;

import remote.api.exceptions.PacketException;

/**
 * Command class for a mouse wheel.
 */
public class MouseWheel extends Command {
	/**
	 * Number of bytes needed when writing.
	 */
	public static final int LENGTH = 5;

	/**
	 * Number to turn.
	 */
	private int wheelAmt;

	/**
	 * Constructs a new mouse wheel.
	 * 
	 * @param wheelAmt
	 *            Absolute number to turn.
	 */
	public MouseWheel(int wheelAmt) {
		this.wheelAmt = wheelAmt;
	}

	/**
	 * Gets the wheel amount.
	 * 
	 * @return Number to turn.
	 */
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

	/**
	 * Attempts to read a MouseWheel from data.
	 * 
	 * @param data
	 *            The data to read.
	 * @param offset
	 *            Start offset in data.
	 * @return The read command.
	 * @throws PacketException
	 *             If offset or length of data makes the read impossible.
	 */
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
