package remote.api.exceptions;

import remote.api.Utils;

public class CommandException extends Exception {
	private static final long serialVersionUID = 8817113489772171373L;

	public CommandException(String message, byte[] data, int offset) {
		super((message + " " + Utils.toHex(data)).trim() + " offset: " + offset);
	}
}
