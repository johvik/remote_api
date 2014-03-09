package remote.api.commands;

import remote.api.exceptions.PacketException;

/**
 * A class that represent a command sent from the client to be executed on the
 * server.
 */
public abstract class Command implements Comparable<Command> {
	/**
	 * Number of type codes used for commands. This number has to be increased
	 * when adding new commands.
	 */
	public static final int USED_CODES = 6;

	/**
	 * Type code for mouse move.
	 */
	public static final byte MOUSE_MOVE = 0;
	/**
	 * Type code for mouse press.
	 */
	public static final byte MOUSE_PRESS = 1;
	/**
	 * Type code for mouse release.
	 */
	public static final byte MOUSE_RELEASE = 2;
	/**
	 * Type code for mouse wheel.
	 */
	public static final byte MOUSE_WHEEL = 3;
	/**
	 * Type code for key press.
	 */
	public static final byte KEY_PRESS = 4;
	/**
	 * Type code for key release.
	 */
	public static final byte KEY_RELEASE = 5;

	/**
	 * Write the command bytes into data. Note that the first byte is reserved
	 * for the packet type.
	 * 
	 * @param data
	 *            Destination of the write.
	 * @param offset
	 *            Start offset in data.
	 * @throws PacketException
	 *             If offset or length makes a write impossible.
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
