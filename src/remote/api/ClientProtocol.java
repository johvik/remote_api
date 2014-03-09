package remote.api;

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

import remote.api.commands.Command;
import remote.api.exceptions.AuthenticationException;
import remote.api.exceptions.PacketException;
import remote.api.exceptions.ProtocolException;
import remote.api.messages.AuthenticationRequest;
import remote.api.messages.CommandRequest;
import remote.api.messages.Message;
import remote.api.messages.Ping;

/**
 * Client side of the protocol.
 */
public class ClientProtocol extends Protocol {
	/**
	 * Key for the block cipher.
	 */
	private byte[] key;

	/**
	 * Constructs a new client protocol.
	 * 
	 * @param publicKey
	 *            The public key for the secure algorithm.
	 * @param key
	 *            The block cipher key.
	 * @param output
	 *            The output stream of the client. This is used to respond and
	 *            send data to the server.
	 * @throws GeneralSecurityException
	 *             If it fails to initialize the secure cipher.
	 * @throws ProtocolException
	 *             If it fails to initialize the block cipher or arguments are
	 *             null.
	 */
	public ClientProtocol(PublicKey publicKey, byte[] key, OutputStream output)
			throws GeneralSecurityException, ProtocolException {
		super(publicKey, key, output);
		this.key = key;
	}

	/**
	 * Sends an authentication request to the server.
	 * 
	 * @param user
	 *            User to authenticate.
	 * @param password
	 *            Password for the user.
	 * @throws ProtocolException
	 *             If already authenticated.
	 * @throws PacketException
	 *             If it fails to pack or encrypt the data.
	 * @throws IOException
	 *             If it fails to send the data to the server.
	 */
	public synchronized void authenticate(String user, String password)
			throws ProtocolException, PacketException, IOException {
		if (authenticated) {
			throw new AuthenticationException("Already authenticated");
		}
		writeSecure(new AuthenticationRequest(key, user, password).pack());
	}

	/**
	 * Sends a command request to the server.
	 * 
	 * @param command
	 *            The command to request.
	 * @throws PacketException
	 *             If it fails to pack or encrypt the data.
	 * @throws IOException
	 *             If it fails to send the data to the server.
	 * @throws ProtocolException
	 *             If not authenticated.
	 */
	public synchronized void commandRequest(Command command)
			throws PacketException, IOException, ProtocolException {
		deliver(new CommandRequest(command));
	}

	@Override
	public synchronized void process(Packet packet) throws PacketException,
			IOException, ProtocolException {
		if (authenticated) {
			Message message = packet.decode(blockDecryptCipher);
			byte type = message.getType();
			switch (type) {
			case Message.PING:
				processPing((Ping) message);
				return;
			}
			throw new ProtocolException("Unexpected message type: " + type);
		} else {
			// Only accept authentication responses
			Message message = packet.decode(blockDecryptCipher);
			byte type = message.getType();
			switch (type) {
			case Message.AUTHENTICATION_RESPONSE:
				authenticated = true;
				return;
			}
			throw new ProtocolException("Unexpected message type: " + type);
		}
	}
}
