package remote.api.messages;

import remote.api.exceptions.PacketException;
import remote.api.Packet;

/**
 * A class for authentication response, sent by the server after the client has
 * requested a successful authentication.
 */
public class AuthenticationResponse extends Message {
	/**
	 * Number of bytes needed by the message.
	 */
	public static final int LENGTH = 1;

	@Override
	public Packet pack() throws PacketException {
		byte[] data = new byte[LENGTH];
		data[0] = AUTHENTICATION_RESPONSE;
		return new Packet(data);
	}

	/**
	 * Attempts to read an authentication response from data.
	 * 
	 * @param data
	 *            The data to read from.
	 * @return The authentication response read.
	 * @throws PacketException
	 *             If the length is incorrect.
	 */
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

	@Override
	public int compareTo(Message o) {
		AuthenticationResponse other = (AuthenticationResponse) o;
		other.getType(); // Dummy to create null pointer exception
		return 0;
	}
}
