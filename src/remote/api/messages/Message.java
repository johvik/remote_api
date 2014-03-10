package remote.api.messages;

import remote.api.Packet;
import remote.api.exceptions.PacketException;

/**
 * A class that represents a message sent between the client and the server.
 */
public abstract class Message implements Comparable<Message> {
	/**
	 * Number of type codes used for messages. This number has to be increased
	 * when adding new messages.
	 */
	public static final int USED_CODES = 5;

	/**
	 * Type code for authentication request.
	 */
	public static final byte AUTHENTICATION_REQUEST = 0;
	/**
	 * Type code for authentication response.
	 */
	public static final byte AUTHENTICATION_RESPONSE = 1;
	/**
	 * Type code for ping.
	 */
	public static final byte PING = 2;
	/**
	 * Type code for command request.
	 */
	public static final byte COMMAND_REQUEST = 3;
	/**
	 * Type code for terminate request.
	 */
	public static final byte TERMINATE_REQUESET = 4;

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
	public abstract Packet pack() throws PacketException;

	/**
	 * Gets the type byte of the message. This has to be unique across messages.
	 * 
	 * @return A byte identifying the message.
	 */
	public abstract byte getType();
}
