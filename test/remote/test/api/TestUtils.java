package remote.test.api;

import static org.junit.Assert.*;

import org.junit.Test;

import remote.api.Utils;

public class TestUtils {

	@Test
	public void testToHex() {
		byte[] data = new byte[] { 0x01, 0x23, 0x45, 0x67, (byte) 0x89,
				(byte) 0xAB, (byte) 0xCD, (byte) 0xEF };
		assertEquals("01 23 45 67 89 AB CD EF", Utils.toHex(data));
		assertEquals("00", Utils.toHex(new byte[] { 0 }));
		assertEquals("", Utils.toHex(new byte[] {}));
		assertEquals("null", Utils.toHex(null));
	}
}
