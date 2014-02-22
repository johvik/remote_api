package remote.api;

import remote.api.exceptions.PacketException;

public interface Message {
	public static final byte AUTHENTICATION_REQUEST = 0;
	public static final byte AUTHENTICATION_RESPONSE = 1;
	public static final byte PING = 2;

	/**
	 * Packet will throw exception if this length is exceeded.
	 */
	public static final int MAX_LENGTH = 256;

	/**
	 * Transforms the message into a packet.
	 * 
	 * @return A packet containing the information from the message.
	 * @throws PacketException
	 *             If packing the message fails.
	 */
	public Packet pack() throws PacketException;

	/**
	 * Gets the type byte of the message. This has to be unique across messages.
	 * 
	 * @return A byte identifying the message.
	 */
	public byte getType();
}
