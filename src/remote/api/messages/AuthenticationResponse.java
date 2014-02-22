package remote.api.messages;

import remote.api.exceptions.PacketException;
import remote.api.Message;
import remote.api.Packet;

public class AuthenticationResponse implements Message {
	private static final int LENGTH = 1;
	public static final int PACKET_SIZE = Packet.getPacketSize(LENGTH);

	@Override
	public Packet pack() throws PacketException {
		byte[] data = new byte[PACKET_SIZE];
		data[0] = Message.AUTHENTICATION_RESPONSE;
		return new Packet(data);
	}

	public static AuthenticationResponse unpack(byte[] data)
			throws PacketException {
		if (data.length != PACKET_SIZE) {
			throw new PacketException("Unexpected length", data);
		}
		return new AuthenticationResponse();
	}

	@Override
	public byte getType() {
		return Message.AUTHENTICATION_RESPONSE;
	}
}
