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

/**
 * Server side of the protocol.
 */
public class ServerProtocol extends Protocol {
	/**
	 * Interface to check authentication.
	 */
	public interface AuthenticationCheck {
		/**
		 * Checks if the user with password is allowed to authenticate.
		 * 
		 * @param user
		 *            The user.
		 * @param password
		 *            The password.
		 * @return True if user and password is allowed to authenticate.
		 */
		public boolean check(String user, String password);
	}

	/**
	 * Interface to handle commands.
	 */
	public interface CommandHandler {
		/**
		 * Handles the command.
		 * 
		 * @param command
		 *            The command to handle.
		 */
		public void handle(Command command);
	}

	/**
	 * The authentication checker.
	 */
	private AuthenticationCheck authentication;
	/**
	 * The command handler.
	 */
	private CommandHandler commandHandler;

	/**
	 * Constructs a new server protocol.
	 * 
	 * @param authentication
	 *            The authentication checker.
	 * @param commandHandler
	 *            The command handler.
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
	public ServerProtocol(AuthenticationCheck authentication,
			CommandHandler commandHandler, PrivateKey privateKey,
			InputStream input, OutputStream output)
			throws GeneralSecurityException, ProtocolException, PacketException {
		super(privateKey, input, output);
		if (authentication == null) {
			throw new ProtocolException("Authentication check cannot be null");
		}
		if (commandHandler == null) {
			throw new ProtocolException("Command handler cannot be null");
		}
		this.authentication = authentication;
		this.commandHandler = commandHandler;
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
				commandHandler.handle(((CommandRequest) message).getCommand());
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
				if (this.authentication.check(authentication.getUser(),
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
				} else {
					throw new AuthenticationException("Bad login");
				}
				return;
			}
			throw new ProtocolException("Unexpected message type: " + type);
		}
	}
}
