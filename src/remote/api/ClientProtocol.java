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

public class ClientProtocol extends Protocol {
	private byte[] key;

	public ClientProtocol(PublicKey publicKey, byte[] key, OutputStream output)
			throws GeneralSecurityException, ProtocolException {
		super(publicKey, key, output);
		this.key = key;
	}

	public synchronized void authenticate(String user, String password)
			throws ProtocolException, PacketException, IOException {
		if (authenticated) {
			throw new AuthenticationException("Already authenticated");
		}
		writeSecure(new AuthenticationRequest(key, user, password).pack());
	}

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
			Message message = packet.decode(secureCipher);
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
