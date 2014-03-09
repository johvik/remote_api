package remote.api.exceptions;

import remote.api.Utils;

/**
 * Packet exception, mostly for Packet class.
 */
public class PacketException extends Exception {
	/**
	 * Random UID.
	 */
	private static final long serialVersionUID = 1111551034114570738L;

	/**
	 * Constructs a new packet exception.
	 * 
	 * @param message
	 *            Message of the exception.
	 * @param data
	 *            The data that caused the exception.
	 */
	public PacketException(String message, byte[] data) {
		super((message + " " + Utils.toHex(data)).trim());
	}

	/**
	 * Constructs a new packet exception.
	 * 
	 * @param message
	 *            Message of the exception.
	 * @param data
	 *            The data that caused the exception.
	 * @param t
	 *            The Throwable that triggered the exception.
	 */
	public PacketException(String message, byte[] data, Throwable t) {
		super((message + " " + Utils.toHex(data)).trim() + "\n"
				+ t.getMessage(), t);
	}
}