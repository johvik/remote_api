package remote.api.commands;

import remote.api.exceptions.CommandException;

public class MouseMove extends Command {
	public static final int LENGTH = 5;

	private short dx;
	private short dy;

	public MouseMove(short dx, short dy) {
		this.dx = dx;
		this.dy = dy;
	}

	@Override
	public void write(byte[] data, int offset) throws CommandException {
		if (offset < 0 || data.length < LENGTH + offset) {
			throw new CommandException("Invalid write", data, offset);
		}
		data[offset] = MOUSE_MOVE;
		data[offset + 1] = (byte) ((dx >> 8) & 0xFF);
		data[offset + 2] = (byte) (dx & 0xFF);
		data[offset + 3] = (byte) ((dy >> 8) & 0xFF);
		data[offset + 4] = (byte) (dy & 0xFF);
	}

	public static MouseMove read(byte[] data, int offset)
			throws CommandException {
		if (offset < 0 || data.length < LENGTH + offset) {
			throw new CommandException("Invalid read", data, offset);
		}
		// First byte is type
		short dx = (short) (((data[offset + 1] & 0xFF) << 8) | (data[offset + 2] & 0xFF));
		short dy = (short) (((data[offset + 3] & 0xFF) << 8) | (data[offset + 4] & 0xFF));
		return new MouseMove(dx, dy);
	}

	@Override
	public byte getType() {
		return MOUSE_MOVE;
	}

	public short getDx() {
		return dx;
	}

	public short getDy() {
		return dy;
	}
}
