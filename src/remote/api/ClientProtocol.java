package remote.api;

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

import remote.api.exceptions.AuthenticationException;
import remote.api.exceptions.PacketException;
import remote.api.exceptions.ProtocolException;
import remote.api.messages.AuthenticationRequest;

public class ClientProtocol extends Protocol {
	private byte[] key;

	public ClientProtocol(PublicKey publicKey, byte[] key, OutputStream output)
			throws GeneralSecurityException, ProtocolException {
		super(publicKey, key, output);
		this.key = key;
	}

	public void authenticate(String user, String password)
			throws ProtocolException, PacketException, IOException {
		if (authenticated) {
			throw new AuthenticationException("Already authenticated");
		}
		writeSecure(new AuthenticationRequest(key, user, password).pack());
	}

	@Override
	public void process(Packet packet) throws PacketException, IOException,
			ProtocolException {
		// TODO Auto-generated method stub
	}
}
