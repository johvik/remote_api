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
	public void write(byte[] data) throws CommandException {
		if (data.length != LENGTH + 1) {
			throw new CommandException("Unexpected length", data);
		}
		data[1] = MOUSE_MOVE;
		data[2] = (byte) ((dx >> 8) & 0xFF);
		data[3] = (byte) (dx & 0xFF);
		data[4] = (byte) ((dy >> 8) & 0xFF);
		data[5] = (byte) (dy & 0xFF);
	}

	public static MouseMove read(byte[] data) throws CommandException {
		if (data.length != LENGTH + 1) {
			throw new CommandException("Unexpected length", data);
		}
		short dx = (short) (((data[2] & 0xFF) << 8) | (data[3] & 0xFF));
		short dy = (short) (((data[4] & 0xFF) << 8) | (data[5] & 0xFF));
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
