package remote.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import remote.api.commands.Command;
import remote.api.exceptions.AuthenticationException;
import remote.api.exceptions.PacketException;
import remote.api.exceptions.ProtocolException;
import remote.api.messages.AuthenticationRequest;
import remote.api.messages.AuthenticationResponse;
import remote.api.messages.CommandRequest;
import remote.api.messages.Message;
import remote.api.messages.Ping;
import remote.api.messages.TerminateRequest;

/**
 * Server side of the protocol.
 */
public class ServerProtocol extends Protocol {
	/**
	 * Interface to handle important events.
	 */
	public interface Handler {
		/**
		 * Checks if the user with password is allowed to authenticate.
		 * 
		 * @param user
		 *            The base64 encoded user.
		 * @param password
		 *            The base64 encoded password.
		 * @return True if user and password is allowed to authenticate.
		 */
		public boolean authentication(byte[] user, byte[] password);

		/**
		 * Handles the command.
		 * 
		 * @param command
		 *            The command to handle.
		 */
		public void command(Command command);

		/**
		 * Handles the termination.
		 * 
		 * @param shutdown
		 *            If it should shutdown as well.
		 */
		public void terminate(boolean shutdown);
	}

	/**
	 * Interface that handles important state changes in the protocol.
	 */
	public interface ConnectionHandler {
		/**
		 * Callback when the client has successfully authenticated.
		 */
		public void onAuthenticated();
	}

	/**
	 * The handler.
	 */
	private Handler handler;

	/**
	 * The connection state handler.
	 */
	private ConnectionHandler connectionHandler;

	/**
	 * Constructs a new server protocol.
	 * 
	 * @param handler
	 *            The handler.
	 * @param connectionHandler
	 *            The connection state handler.
	 * @param privateKey
	 *            The private key of the secure algorithm.
	 * @param input
	 *            The input stream of the server. This is used to receive data
	 *            from the client.
	 * @param output
	 *            The output stream of the server. This is used to respond and
	 *            send data to the client.
	 * @throws GeneralSecurityException
	 *             If it fails to initialize the secure cipher.
	 * @throws ProtocolException
	 *             If arguments is null.
	 * @throws PacketException
	 *             See {@link PacketScanner#PacketScanner(InputStream)}
	 */
	public ServerProtocol(Handler handler, ConnectionHandler connectionHandler,
			PrivateKey privateKey, InputStream input, OutputStream output)
			throws GeneralSecurityException, ProtocolException, PacketException {
		super(privateKey, input, output);
		if (handler == null) {
			throw new ProtocolException("Handler cannot be null");
		}
		if (connectionHandler == null) {
			throw new ProtocolException("Connection handler cannot be null");
		}
		this.handler = handler;
		this.connectionHandler = connectionHandler;
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
			case Message.COMMAND_REQUEST:
				handler.command(((CommandRequest) message).getCommand());
				return;
			case Message.TERMINATE_REQUESET:
				handler.terminate(((TerminateRequest) message).isShutdown());
				return;
			}
			throw new ProtocolException("Unexpected message type: " + type);
		} else {
			// Only accept authentication requests
			Message message = packet.decode(secureCipher);
			byte type = message.getType();
			switch (type) {
			case Message.AUTHENTICATION_REQUEST:
				AuthenticationRequest authentication = (AuthenticationRequest) message;
				// Check if user is allowed
				if (handler.authentication(authentication.getUser(),
						authentication.getPassword())) {
					// Set the block key
					byte[] key = authentication.getKey();
					// Change initialization vector
					iv = authentication.getIv();
					SecretKey secretKey = new SecretKeySpec(key,
							Packet.BLOCK_CIPHER_NAME);
					// Initialize the block cipher
					blockCipherInit(secretKey);
					authenticated = true;
					deliver(new AuthenticationResponse());
					connectionHandler.onAuthenticated();
				} else {
					throw new AuthenticationException("Bad login");
				}
				return;
			}
			throw new ProtocolException("Unexpected message type: " + type);
		}
	}
}
