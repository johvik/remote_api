package remote.api.messages;

import remote.api.exceptions.PacketException;
import remote.api.Packet;

/**
 * A class for a terminate request message.
 */
public class TerminateRequest extends Message {
	/**
	 * Number of bytes needed by the message.
	 */
	public static final int LENGTH = 2;

	/**
	 * Indicates if its a request for shutdown as well.
	 */
	private boolean shutdown;

	/**
	 * Constructs a new terminate request.
	 * 
	 * @param shutdown
	 *            True if it should request to shutdown.
	 */
	public TerminateRequest(boolean shutdown) {
		this.shutdown = shutdown;
	}

	@Override
	public Packet pack() throws PacketException {
		byte[] data = new byte[LENGTH];
		data[0] = TERMINATE_REQUESET;
		data[1] = (byte) (shutdown ? 1 : 0);
		return new Packet(data);
	}

	/**
	 * Attempts to read a terminate request from data.
	 * 
	 * @param data
	 *            The data to read from.
	 * @return The terminate request read.
	 * @throws PacketException
	 *             If the length is incorrect.
	 */
	public static TerminateRequest unpack(byte[] data) throws PacketException {
		if (data.length != LENGTH) {
			throw new PacketException("Unexpected length", data);
		}
		return new TerminateRequest(data[1] == 1);
	}

	@Override
	public byte getType() {
		return TERMINATE_REQUESET;
	}

	/**
	 * Check if shutdown is requested.
	 * 
	 * @return True if shutdown is requested.
	 */
	public boolean isShutdown() {
		return shutdown;
	}

	@Override
	public int compareTo(Message o) {
		TerminateRequest other = (TerminateRequest) o;
		return Boolean.valueOf(shutdown).compareTo(other.shutdown);
	}
}
