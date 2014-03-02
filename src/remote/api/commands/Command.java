package remote.api.commands;

import remote.api.exceptions.PacketException;

public abstract class Command implements Comparable<Command> {
	// Number of type codes used.
	public static final int USED_CODES = 5;

	public static final byte MOUSE_MOVE = 0;
	public static final byte MOUSE_PRESS = 1;
	public static final byte MOUSE_RELEASE = 2;
	public static final byte MOUSE_WHEEL = 3;
	public static final byte KEY_PRESS = 4;

	/**
	 * Write the command bytes into data. Note that the first byte is reserved
	 * for the packet type.
	 * 
	 * @param data
	 * @param offset
	 * @throws PacketException
	 */
	public abstract void write(byte[] data, int offset) throws PacketException;

	/**
	 * Calculates the length of the command.
	 * 
	 * @return Number of bytes required for the command.
	 */
	public abstract int getLength();

	/**
	 * Gets the type byte of the command. This has to be unique across commands.
	 * 
	 * @return A byte identifying the command.
	 */
	public abstract byte getType();
}
