package remote.api.commands;

public abstract class Command {
	// Number of type codes used.
	public static final int USED_CODES = 1;

	public static final byte MOUSE_MOVE = 0;

	/**
	 * Write the command bytes into data. Note that the first byte is reserved
	 * for the packet type.
	 * 
	 * @param data
	 */
	public abstract void write(byte[] data);

	/**
	 * Gets the type byte of the command. This has to be unique across commands.
	 * 
	 * @return A byte identifying the command.
	 */
	public abstract byte getType();
}
