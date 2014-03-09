package remote.api.commands;

import remote.api.exceptions.PacketException;

/**
 * Command class for a mouse move.
 */
public class MouseMove extends Command {
	/**
	 * Number of bytes needed when writing.
	 */
	public static final int LENGTH = 5;

	/**
	 * Relative amount to move in the x-axis.
	 */
	private short dx;
	/**
	 * Relative amount to move in the y-axis.
	 */
	private short dy;

	/**
	 * Constructs a new mouse move.
	 * 
	 * @param dx
	 *            Relative number of pixels to move in the x-axis.
	 * @param dy
	 *            Relative number of pixels to move in the y-axis.
	 */
	public MouseMove(short dx, short dy) {
		this.dx = dx;
		this.dy = dy;
	}

	@Override
	public void write(byte[] data, int offset) throws PacketException {
		if (offset < 0 || data.length < LENGTH + offset) {
			throw new PacketException("Invalid write " + offset, data);
		}
		data[offset] = MOUSE_MOVE;
		data[offset + 1] = (byte) ((dx >> 8) & 0xFF);
		data[offset + 2] = (byte) (dx & 0xFF);
		data[offset + 3] = (byte) ((dy >> 8) & 0xFF);
		data[offset + 4] = (byte) (dy & 0xFF);
	}

	/**
	 * Attempts to read a MouseMove from data.
	 * 
	 * @param data
	 *            The data to read.
	 * @param offset
	 *            Start offset in data.
	 * @return The read command.
	 * @throws PacketException
	 *             If offset or length of data makes the read impossible.
	 */
	public static MouseMove read(byte[] data, int offset)
			throws PacketException {
		if (offset < 0 || data.length < LENGTH + offset) {
			throw new PacketException("Invalid read " + offset, data);
		}
		// First byte is type
		short dx = (short) (((data[offset + 1] & 0xFF) << 8) | (data[offset + 2] & 0xFF));
		short dy = (short) (((data[offset + 3] & 0xFF) << 8) | (data[offset + 4] & 0xFF));
		return new MouseMove(dx, dy);
	}

	@Override
	public int getLength() {
		return LENGTH;
	}

	@Override
	public byte getType() {
		return MOUSE_MOVE;
	}

	/**
	 * Relative amount to move in the x-axis.
	 * 
	 * @return The dx value.
	 */
	public short getDx() {
		return dx;
	}

	/**
	 * Relative amount to move in the y-axis.
	 * 
	 * @return The dy value.
	 */
	public short getDy() {
		return dy;
	}

	@Override
	public int compareTo(Command o) {
		MouseMove other = (MouseMove) o;
		int cmp = Short.valueOf(dx).compareTo(other.dx);
		if (cmp == 0) {
			cmp = Short.valueOf(dy).compareTo(other.dy);
		}
		return cmp;
	}
}
