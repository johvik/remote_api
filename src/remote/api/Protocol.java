package remote.api;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.management.openmbean.InvalidKeyException;

import remote.api.exceptions.AuthenticationException;
import remote.api.exceptions.PacketException;
import remote.api.exceptions.ProtocolException;
import remote.api.messages.Ping;

public abstract class Protocol {
	public interface PingCallback {
		/**
		 * Executed when a ping response is received.
		 * 
		 * @param diff
		 *            The time difference in nanoseconds since the request.
		 */
		public void run(long diff);
	}

	private boolean pingRequested;
	private long pingTime;
	private PingCallback pingCallback;
	private DataOutputStream output;
	protected boolean authenticated;
	protected Cipher blockEncryptCipher;
	protected Cipher blockDecryptCipher;
	protected Cipher secureCipher;

	private Protocol(OutputStream output) {
		pingRequested = false;
		pingTime = 0;
		this.output = new DataOutputStream(output);
		authenticated = false;
	}

	/**
	 * Constructor for a client protocol.
	 * 
	 * @param publicKey
	 * @param key
	 * @param output
	 * @throws GeneralSecurityException
	 */
	protected Protocol(PublicKey publicKey, byte[] key, OutputStream output)
			throws GeneralSecurityException {
		this(output);
		if (key.length != Packet.BLOCK_KEY_SIZE) {
			throw new InvalidKeyException("Key has wrong length");
		}
		SecretKey secretKey = new SecretKeySpec(key, Packet.BLOCK_CIPHER);
		// Initialize ciphers
		blockCipherInit(secretKey);
		secureCipher = Cipher.getInstance(Packet.SECURE_ALGORITHM);
		secureCipher.init(Cipher.ENCRYPT_MODE, publicKey);
	}

	/**
	 * Constructor for a server protocol.
	 * 
	 * @param privateKey
	 * @param output
	 * @throws GeneralSecurityException
	 */
	protected Protocol(PrivateKey privateKey, OutputStream output)
			throws GeneralSecurityException {
		this(output);
		blockDecryptCipher = null;
		blockEncryptCipher = null;
		secureCipher = Cipher.getInstance(Packet.SECURE_ALGORITHM);
		secureCipher.init(Cipher.DECRYPT_MODE, privateKey);
	}

	/**
	 * Initializes the block cipher given a key.
	 * 
	 * @param secretKey
	 * @throws GeneralSecurityException
	 */
	protected void blockCipherInit(SecretKey secretKey)
			throws GeneralSecurityException {
		blockDecryptCipher = Cipher.getInstance(Packet.BLOCK_CIPHER);
		blockDecryptCipher.init(Cipher.DECRYPT_MODE, secretKey);
		blockEncryptCipher = Cipher.getInstance(Packet.BLOCK_CIPHER);
		blockEncryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);
	}

	/**
	 * Sends a ping request
	 * 
	 * @param pingCallback
	 * 
	 * @throws ProtocolException
	 * @throws PacketException
	 * @throws IOException
	 */
	public void ping(PingCallback pingCallback) throws ProtocolException,
			PacketException, IOException {
		if (pingRequested) {
			throw new ProtocolException("Ping already requested");
		}
		pingRequested = true;
		this.pingCallback = pingCallback;
		// Measure time
		pingTime = System.nanoTime();
		deliver(new Ping(true));
	}

	/**
	 * Handles a ping message.
	 * 
	 * @param ping
	 * @throws PacketException
	 * @throws ProtocolException
	 * @throws IOException
	 */
	protected void processPing(Ping ping) throws PacketException,
			ProtocolException, IOException {
		if (ping.isRequest()) {
			// Respond with a pong!
			deliver(new Ping(false));
		} else {
			if (!pingRequested) {
				throw new ProtocolException("Ping not requested");
			}
			pingRequested = false;
			long diff = System.nanoTime() - pingTime;
			// Run the callback
			if (pingCallback != null) {
				pingCallback.run(diff);
			}
		}
	}

	/**
	 * Delivers a block cipher message, checks if authenticated.
	 * 
	 * @param message
	 * @throws PacketException
	 * @throws IOException
	 * @throws ProtocolException
	 */
	protected void deliver(Message message) throws PacketException,
			IOException, ProtocolException {
		if (!authenticated) {
			throw new AuthenticationException("Expecting authentication");
		}
		message.pack().write(blockEncryptCipher, output);
	}

	/**
	 * Writes a secure packet without any checks.
	 * 
	 * @param packet
	 * @throws PacketException
	 * @throws IOException
	 */
	protected void writeSecure(Packet packet) throws PacketException,
			IOException {
		packet.write(secureCipher, output);
	}

	/**
	 * Used to process an incoming packet.
	 * 
	 * @param packet
	 * @throws PacketException
	 * @throws IOException
	 * @throws ProtocolException
	 */
	public abstract void process(Packet packet) throws PacketException,
			IOException, ProtocolException;
}
