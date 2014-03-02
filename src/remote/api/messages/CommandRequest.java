package remote.api.messages;

import remote.api.Packet;
import remote.api.commands.Command;
import remote.api.commands.MouseMove;
import remote.api.exceptions.PacketException;

public class CommandRequest extends Message {
	public static final int STATIC_LENGTH = 1;
	private Command command;

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

	public static CommandRequest unpack(byte[] data) throws PacketException {
		if (data.length <= STATIC_LENGTH) {
			throw new PacketException("Unexpected length", data);
		}
		byte type = data[1];
		switch (type) {
		case Command.MOUSE_MOVE:
			return new CommandRequest(MouseMove.read(data, STATIC_LENGTH));
		default:
			throw new PacketException("Unknown command message", data);
		}
	}

	@Override
	public byte getType() {
		return COMMAND_REQUEST;
	}

	public Command getCommand() {
		return command;
	}

	@Override
	public int compareTo(Message o) {
		CommandRequest other = (CommandRequest) o;
		return command.compareTo(other.getCommand());
	}
}
