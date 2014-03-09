package remote.api.messages;

import remote.api.exceptions.PacketException;
import remote.api.Packet;

/**
 * A class for a ping message.
 */
public class Ping extends Message {
	/**
	 * Number of bytes needed by the message.
	 */
	public static final int LENGTH = 2;

	/**
	 * Indicates if its a request or a response.
	 */
	private boolean request;

	/**
	 * Constructs a new ping.
	 * 
	 * @param request
	 *            True if it is a request.
	 */
	public Ping(boolean request) {
		this.request = request;
	}

	@Override
	public Packet pack() throws PacketException {
		byte[] data = new byte[LENGTH];
		data[0] = PING;
		data[1] = (byte) (request ? 1 : 0);
		return new Packet(data);
	}

	/**
	 * Attempts to read a ping from data.
	 * 
	 * @param data
	 *            The data to read from.
	 * @return The ping read.
	 * @throws PacketException
	 *             If the length is incorrect.
	 */
	public static Ping unpack(byte[] data) throws PacketException {
		if (data.length != LENGTH) {
			throw new PacketException("Unexpected length", data);
		}
		return new Ping(data[1] == 1);
	}

	@Override
	public byte getType() {
		return PING;
	}

	/**
	 * Check if it is a request or a response.
	 * 
	 * @return True if a response, false if a request.
	 */
	public boolean isRequest() {
		return request;
	}

	@Override
	public int compareTo(Message o) {
		Ping other = (Ping) o;
		return Boolean.valueOf(request).compareTo(other.request);
	}
}
