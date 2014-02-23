package remote.api.messages;

import remote.api.exceptions.PacketException;
import remote.api.Message;
import remote.api.Packet;

public class Ping implements Message {
	public static final int LENGTH = 2;
	private boolean request;

	public Ping(boolean request) {
		this.request = request;
	}

	@Override
	public Packet pack() throws PacketException {
		byte[] data = new byte[LENGTH];
		data[0] = Message.PING;
		data[1] = (byte) (request ? 1 : 0);
		return new Packet(data);
	}

	public static Ping unpack(byte[] data) throws PacketException {
		if (data.length != LENGTH) {
			throw new PacketException("Unexpected length", data);
		}
		return new Ping(data[1] == 1);
	}

	@Override
	public byte getType() {
		return Message.PING;
	}

	/**
	 * Check if it is a request or a response.
	 * 
	 * @return True if a response, false if a request.
	 */
	public boolean isRequest() {
		return request;
	}
}
