package remote.api;

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import remote.api.exceptions.AuthenticationException;
import remote.api.exceptions.PacketException;
import remote.api.exceptions.ProtocolException;
import remote.api.messages.AuthenticationRequest;
import remote.api.messages.AuthenticationResponse;
import remote.api.messages.Ping;

public class ServerProtocol extends Protocol {
	public interface AuthenticationCheck {
		public boolean check(String user, String password);
	}

	private AuthenticationCheck authentication;

	public ServerProtocol(AuthenticationCheck authentication,
			PrivateKey privateKey, OutputStream output)
			throws GeneralSecurityException {
		super(privateKey, output);
		this.authentication = authentication;
	}

	@Override
	public void process(Packet packet) throws PacketException, IOException,
			ProtocolException {
		if (authenticated) {
			Message message = packet.decode(blockDecryptCipher);
			byte type = message.getType();
			switch (type) {
			case Message.PING:
				processPing((Ping) message);
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
						e.printStackTrace();
						throw new PacketException("Failed to set block cipher",
								key);
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
