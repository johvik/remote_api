package remote.test.api;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import remote.api.Packet;

/**
 * A helper class for the tests.
 */
public class Misc {
	/**
	 * The private key.
	 */
	public static PrivateKey privateKey;
	/**
	 * The public key.
	 */
	public static PublicKey publicKey;
	/**
	 * The key for the block cipher.
	 */
	public static byte[] key;
	/**
	 * The initialization vector.
	 */
	public static byte[] iv;
	/**
	 * The secret key for the block cipher.
	 */
	public static SecretKey secretKey;
	/**
	 * The block decrypt cipher.
	 */
	public static Cipher blockDecrypt;
	/**
	 * The block encrypt cipher.
	 */
	public static Cipher blockEncrypt;
	/**
	 * The secure decrypt cipher.
	 */
	public static Cipher secureDecrypt;
	/**
	 * The secure encrypt cipher.
	 */
	public static Cipher secureEncrypt;
	static {
		try {
			KeyFactory keyFactory = KeyFactory
					.getInstance(Packet.SECURE_ALGORITHM_NAME);
			privateKey = keyFactory
					.generatePrivate(new RSAPrivateKeySpec(
							new BigInteger(
									"20134254310111876361202866314108968204981698707023098174509848016538361340068154080221226903152716741691177544895582833095778498831876368737541275589258904991959335097305652778429500233652186048642106165566875887303812745872719282270778593126721035827645927529200997010332320430882912795400722363957922171201073586391455742845187637472867650716140231631789758124448338078779761585213985819898061474683944417595284592829909793640245683782387335764464247466037661435457674665761288297726118193971702941050422552088863500561512935220236008069590989430679869890388141102277549511231670670042034121251923449954590575254103"),
							new BigInteger(
									"8853148701565435419698536682693411916367206488552796437048541896830583620500541619691702331021435804893642725655065237977341791672462598500232341099770722876440669537803942751667041644157575802427934958468356233035292611773717923572898160094797138859655960657475702745727550511264673360469087940585859029364984095754560027236168774026732743313514867528520286228996392525537511686947441343349492286213543086953611056820752700243661783388280121987066338534742106447852719549747575472975528705262124836175510000154934521641303333163595350884568813004235721286039700123980275751721618293657791629183976790611356694462273")));
			publicKey = keyFactory
					.generatePublic(new RSAPublicKeySpec(
							new BigInteger(
									"20134254310111876361202866314108968204981698707023098174509848016538361340068154080221226903152716741691177544895582833095778498831876368737541275589258904991959335097305652778429500233652186048642106165566875887303812745872719282270778593126721035827645927529200997010332320430882912795400722363957922171201073586391455742845187637472867650716140231631789758124448338078779761585213985819898061474683944417595284592829909793640245683782387335764464247466037661435457674665761288297726118193971702941050422552088863500561512935220236008069590989430679869890388141102277549511231670670042034121251923449954590575254103"),
							new BigInteger("65537")));
			key = Misc.getSequence(1, Packet.BLOCK_KEY_SIZE);
			secretKey = new SecretKeySpec(key, Packet.BLOCK_CIPHER_NAME);

			iv = Misc.getSequence(1, Packet.BLOCK_SIZE);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			blockDecrypt = Cipher.getInstance(Packet.BLOCK_CIPHER);
			blockDecrypt.init(Cipher.DECRYPT_MODE, Misc.secretKey, ivSpec);
			blockEncrypt = Cipher.getInstance(Packet.BLOCK_CIPHER);
			blockEncrypt.init(Cipher.ENCRYPT_MODE, Misc.secretKey, ivSpec);

			secureDecrypt = Cipher.getInstance(Packet.SECURE_ALGORITHM);
			secureDecrypt.init(Cipher.DECRYPT_MODE, Misc.privateKey);
			secureEncrypt = Cipher.getInstance(Packet.SECURE_ALGORITHM);
			secureEncrypt.init(Cipher.ENCRYPT_MODE, Misc.publicKey);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generates a sequence starting at start and each element is increased by
	 * one. Note that it will wrap and start over at 0 when it wraps.
	 * 
	 * @param start
	 *            Number to start at.
	 * @param length
	 *            Length of the sequence.
	 * @return A byte array of size equal to length.
	 */
	public static byte[] getSequence(int start, int length) {
		byte[] sequence = new byte[length];
		for (int i = 0; i < length; i++) {
			sequence[i] = (byte) (i + start);
		}
		return sequence;
	}

	/**
	 * Creates a new string by repeating the input.
	 * 
	 * @param c
	 *            The char to repeat.
	 * @param times
	 *            The number of times to repeat.
	 * @return The repeated string.
	 */
	public static String repeat(char c, int times) {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < times; i++) {
			b.append(c);
		}
		return b.toString();
	}

	/**
	 * Encrypts a packet with the block cipher.
	 * 
	 * @param packet
	 *            The packet to encrypt.
	 * @return The encrypted packet.
	 * @throws Exception
	 *             If something went wrong.
	 */
	public static Packet encryptBlock(Packet packet) throws Exception {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		packet.write(blockEncrypt, output);
		// Remove first two bytes
		byte[] data = output.toByteArray();
		byte[] res = new byte[data.length - 2];
		System.arraycopy(data, 2, res, 0, res.length);

		return new Packet(res, true);
	}

	/**
	 * Encrypts a packet with the secure cipher.
	 * 
	 * @param packet
	 *            The packet to encrypt.
	 * @return The encrypted packet.
	 * @throws Exception
	 *             If something went wrong.
	 */
	public static Packet encryptSecure(Packet packet) throws Exception {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		packet.write(secureEncrypt, output);
		// Remove first two bytes
		byte[] data = output.toByteArray();
		byte[] res = new byte[data.length - 2];
		System.arraycopy(data, 2, res, 0, res.length);

		return new Packet(res, true);
	}
}
