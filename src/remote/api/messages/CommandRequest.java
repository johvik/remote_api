package remote.api.messages;

import remote.api.Packet;
import remote.api.commands.Command;
import remote.api.commands.KeyPress;
import remote.api.commands.KeyRelease;
import remote.api.commands.MouseMove;
import remote.api.commands.MousePress;
import remote.api.commands.MouseRelease;
import remote.api.commands.MouseWheel;
import remote.api.commands.TextInput;
import remote.api.exceptions.PacketException;

/**
 * A class for sending commands to the server.
 */
public class CommandRequest extends Message {
	/**
	 * Static length of a message. This is the type byte of the command request.
	 */
	public static final int STATIC_LENGTH = 1;

	/**
	 * Command of the message.
	 */
	private Command command;

	/**
	 * Constructs a new command request.
	 * 
	 * @param command
	 *            The command of the message.
	 * @throws PacketException
	 *             If the command is null.
	 */
	public CommandRequest(Command command) throws PacketException {
		if (command == null) {
			throw new PacketException("Command is null", null);
		}
		this.command = command;
	}

	@Override
	public Packet pack() throws PacketException {
		byte[] data = new byte[STATIC_LENGTH + command.getLength()];
		data[0] = COMMAND_REQUEST;
		command.write(data, STATIC_LENGTH);
		return new Packet(data);
	}

	/**
	 * Attempts to read a command request from data.
	 * 
	 * @param data
	 *            The data to read from.
	 * @return The command request read.
	 * @throws PacketException
	 *             If the length is incorrect or an invalid command was sent.
	 */
	public static CommandRequest unpack(byte[] data) throws PacketException {
		if (data.length <= STATIC_LENGTH) {
			throw new PacketException("Unexpected length", data);
		}
		byte type = data[1];
		switch (type) {
		case Command.MOUSE_MOVE:
			return new CommandRequest(MouseMove.read(data, STATIC_LENGTH));
		case Command.MOUSE_PRESS:
			return new CommandRequest(MousePress.read(data, STATIC_LENGTH));
		case Command.MOUSE_RELEASE:
			return new CommandRequest(MouseRelease.read(data, STATIC_LENGTH));
		case Command.MOUSE_WHEEL:
			return new CommandRequest(MouseWheel.read(data, STATIC_LENGTH));
		case Command.KEY_PRESS:
			return new CommandRequest(KeyPress.read(data, STATIC_LENGTH));
		case Command.KEY_RELEASE:
			return new CommandRequest(KeyRelease.read(data, STATIC_LENGTH));
		case Command.TEXT_INPUT:
			return new CommandRequest(TextInput.read(data, STATIC_LENGTH));
		}
		throw new PacketException("Unknown command message", data);
	}

	@Override
	public byte getType() {
		return COMMAND_REQUEST;
	}

	/**
	 * Gets the command of the request.
	 * 
	 * @return The command.
	 */
	public Command getCommand() {
		return command;
	}

	@Override
	public int compareTo(Message o) {
		CommandRequest other = (CommandRequest) o;
		Command otherCommand = other.command;
		// "Class" check
		int cmp = Byte.valueOf(command.getType()).compareTo(
				otherCommand.getType());
		if (cmp == 0) {
			cmp = command.compareTo(otherCommand);
		}
		return cmp;
	}
}
