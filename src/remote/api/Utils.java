package remote.api;

import java.util.Random;

public class Utils {
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

	public static byte[] generateRandom(int length) {
		// Generate a random key
		Random random = new Random();
		byte[] key = new byte[length];
		random.nextBytes(key);
		return key;
	}

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
