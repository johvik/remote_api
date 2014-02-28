package remote.api.messages;

import remote.api.exceptions.PacketException;
import remote.api.Packet;

public class AuthenticationResponse extends Message {
	public static final int LENGTH = 1;

	@Override
	public Packet pack() throws PacketException {
		byte[] data = new byte[LENGTH];
		data[0] = AUTHENTICATION_RESPONSE;
		return new Packet(data);
	}

	public static AuthenticationResponse unpack(byte[] data)
			throws PacketException {
		if (data.length != LENGTH) {
			throw new PacketException("Unexpected length", data);
		}
		return new AuthenticationResponse();
	}

	@Override
	public byte getType() {
		return AUTHENTICATION_RESPONSE;
	}
}
