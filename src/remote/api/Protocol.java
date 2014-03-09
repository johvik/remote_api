package remote.api;

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import remote.api.exceptions.AuthenticationException;
import remote.api.exceptions.PacketException;
import remote.api.exceptions.ProtocolException;
import remote.api.messages.Message;
import remote.api.messages.Ping;

/**
 * A base class for the protocols.
 */
public abstract class Protocol {
	/**
	 * An interface to handle ping callbacks.
	 */
	public interface PingCallback {
		/**
		 * Executed when a ping response is received.
		 * 
		 * @param diff
		 *            The time difference in nanoseconds since the request.
		 */
		public void run(long diff);
	}

	/**
	 * State if ping has been requested.
	 */
	private boolean pingRequested;
	/**
	 * Start of the measured ping time.
	 */
	private long pingTime;
	/**
	 * The ping callback.
	 */
	private PingCallback pingCallback;
	/**
	 * The output stream used to send responses and data.
	 */
	private OutputStream output;
	/**
	 * State if the user is authenticated.
	 */
	protected boolean authenticated;
	/**
	 * The cipher used for block encryption.
	 */
	protected Cipher blockEncryptCipher;
	/**
	 * The cipher used for block decryption.
	 */
	protected Cipher blockDecryptCipher;
	/**
	 * The cipher used to encrypt or decrypt secure data.
	 */
	protected Cipher secureCipher;

	/**
	 * Constructs a new protocol.
	 * 
	 * @param output
	 *            The output stream.
	 * @throws ProtocolException
	 *             If output is null.
	 */
	private Protocol(OutputStream output) throws ProtocolException {
		if (output == null) {
			throw new ProtocolException("Output cannot be null");
		}
		pingRequested = false;
		pingTime = 0;
		this.output = output;
		authenticated = false;
	}

	/**
	 * Constructor for a client protocol.
	 * 
	 * @param publicKey
	 *            The public key for the secure algorithm.
	 * @param key
	 *            The key to use for the block cipher.
	 * @param output
	 *            The output stream.
	 * @throws GeneralSecurityException
	 *             If it fails to initialize the secure cipher.
	 * @throws ProtocolException
	 *             If it fails to initialize the block cipher, if the key is
	 *             invalid or if output is null.
	 */
	protected Protocol(PublicKey publicKey, byte[] key, OutputStream output)
			throws GeneralSecurityException, ProtocolException {
		this(output);
		if (key == null) {
			throw new InvalidKeyException("Key cannot be null");
		}
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
	 *            The private key for the secure algorithm.
	 * @param output
	 *            The output stream.
	 * @throws GeneralSecurityException
	 *             If it fails to initialize the secure cipher.
	 * @throws ProtocolException
	 *             If output is null.
	 */
	protected Protocol(PrivateKey privateKey, OutputStream output)
			throws GeneralSecurityException, ProtocolException {
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
	 *            The key to use for the block cipher.
	 * @throws ProtocolException
	 *             If it fails to initialize the cipher.
	 */
	protected void blockCipherInit(SecretKey secretKey)
			throws ProtocolException {
		try {
			blockDecryptCipher = Cipher.getInstance(Packet.BLOCK_CIPHER);
			blockDecryptCipher.init(Cipher.DECRYPT_MODE, secretKey);
			blockEncryptCipher = Cipher.getInstance(Packet.BLOCK_CIPHER);
			blockEncryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);
		} catch (GeneralSecurityException e) {
			throw new ProtocolException("Failed to set block cipher", e);
		}
	}

	/**
	 * Sends a ping request.
	 * 
	 * @param pingCallback
	 *            The callback to use when receiving the response, may be null
	 *            if response should be ignored.
	 * 
	 * @throws ProtocolException
	 *             If ping already has been requested.
	 * @throws PacketException
	 *             If it fails to deliver the packet.
	 * @throws IOException
	 *             If it fails to write to the output stream.
	 */
	public synchronized void ping(PingCallback pingCallback)
			throws ProtocolException, PacketException, IOException {
		if (pingRequested) {
			throw new ProtocolException("Ping already requested");
		}
		deliver(new Ping(true));
		// Measure time
		pingTime = System.nanoTime();
		pingRequested = true;
		this.pingCallback = pingCallback;
	}

	/**
	 * Handles a ping message.
	 * 
	 * @param ping
	 *            The ping message.
	 * @throws PacketException
	 *             If i fails to pack the response.
	 * @throws ProtocolException
	 *             If ping was not requested.
	 * @throws IOException
	 *             If it fails to write to the output stream.
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
	 *            The message to deliver.
	 * @throws PacketException
	 *             If it fails to pack the message.
	 * @throws IOException
	 *             If it fails to write to the output stream.
	 * @throws ProtocolException
	 *             If not authenticated.
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
	 *            The packet to write.
	 * @throws PacketException
	 *             If it fails to pack the packet.
	 * @throws IOException
	 *             If it fails to write to the output stream.
	 */
	protected void writeSecure(Packet packet) throws PacketException,
			IOException {
		packet.write(secureCipher, output);
	}

	/**
	 * Used to process an incoming packet.
	 * 
	 * @param packet
	 *            The packet to process.
	 * @throws PacketException
	 *             If it fails to handle the packet for some reason.
	 * @throws IOException
	 *             If it fails to write to the output stream.
	 * @throws ProtocolException
	 *             If the packet is unexpected for some reason.
	 */
	public abstract void process(Packet packet) throws PacketException,
			IOException, ProtocolException;
}
