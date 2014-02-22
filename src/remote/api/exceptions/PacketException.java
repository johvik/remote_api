package remote.api.exceptions;

import remote.api.Utils;

public class PacketException extends Exception {
	private static final long serialVersionUID = 1111551034114570738L;

	public PacketException(String message, byte[] data) {
		super((message + " " + Utils.toHex(data)).trim());
	}
}