package remote.api;

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
}
