package remote.api;

import java.util.Random;

/**
 * A class containing various utilities.
 */
public class Utils {
	/**
	 * Converts the data to a hex string.
	 * 
	 * @param data
	 *            The data to convert.
	 * @return The converted data e.g: [0x00, 0xFF] becomes: 00 FF. Or null if
	 *         data is null.
	 */
	public static String toHex(byte[] data) {
		if (data == null) {
			return "null";
		}
		StringBuffer sb = new StringBuffer();
		for (byte b : data) {
			String hex = Integer.toHexString(0xFF & b);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
			sb.append(' ');
		}
		return sb.toString().toUpperCase().trim();
	}

	/**
	 * Generates a random byte array with given length.
	 * 
	 * @param length
	 *            Length of the array.
	 * @return A random byte array with length length.
	 */
	public static byte[] generateRandom(int length) {
		// Generate a random key
		Random random = new Random();
		byte[] key = new byte[length];
		random.nextBytes(key);
		return key;
	}

	/**
	 * Compares two byte arrays.
	 * 
	 * @param b1
	 *            First array to compare.
	 * @param b2
	 *            Second array to compare.
	 * @return 0 if they are equal or -1/1 if not equal.
	 */
	public static int compare(byte[] b1, byte[] b2) {
		if (b1 == null) {
			if (b2 == null) {
				return 0;
			}
			return -1;
		} else if (b2 == null) {
			return 1;
		} else {
			// Arrays are not null
			int length = b1.length;
			int cmp = Integer.valueOf(length).compareTo(b2.length);
			// Compare the byte arrays
			// Same length if cmp == 0
			for (int i = 0; cmp == 0 && i < length; i++) {
				cmp = Byte.valueOf(b1[i]).compareTo(b2[i]);
			}
			return cmp;
		}
	}
}
