package remote.api;

import java.io.IOException;
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

public class ServerProtocol extends Protocol {
	public interface AuthenticationCheck {
		public boolean check(String user, String password);
	}

	public interface CommandHandler {
		public void handle(Command command);
	}

	private AuthenticationCheck authentication;
	private CommandHandler commandHandler;

	public ServerProtocol(AuthenticationCheck authentication,
			CommandHandler commandHandler, PrivateKey privateKey,
			OutputStream output) throws GeneralSecurityException,
			ProtocolException {
		super(privateKey, output);
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
				break;
			case Message.COMMAND_REQUEST:
				commandHandler.handle(((CommandRequest) message).getCommand());
				break;
			default:
				throw new ProtocolException("Unexpected message type: " + type);
			}
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
					SecretKey secretKey = new SecretKeySpec(key,
							Packet.BLOCK_CIPHER);
					try {
						blockCipherInit(secretKey);
						authenticated = true;
						deliver(new AuthenticationResponse());
					} catch (GeneralSecurityException e) {
						throw new PacketException("Failed to set block cipher",
								key, e);
					}
				} else {
					throw new AuthenticationException("Bad login");
				}
				break;
			default:
				throw new ProtocolException("Unexpected message type: " + type);
			}
		}
	}
}
