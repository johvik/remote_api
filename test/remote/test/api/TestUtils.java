package remote.test.api;

import static org.junit.Assert.*;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsEqual.equalTo;

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

	@Test
	public void testGenerateRandom() {
		// Check that the length works
		for (int i = 0; i < 10; i++) {
			assertEquals(i, Utils.generateRandom(i).length);
		}
		// Compare two keys
		// Normally they should not match...
		int length = 100;
		byte[] data1 = Utils.generateRandom(length);
		byte[] data2 = Utils.generateRandom(length);
		// If tried up to 100 times it is highly unlikely and must be a fault
		for (int i = 0; equalTo(data1).matches(data2) && i < 100; i++) {
			data2 = Utils.generateRandom(length);
		}
		assertEquals(length, data1.length);
		assertEquals(data1.length, data2.length);
		assertThat(data1, not(equalTo(data2)));
	}
}
