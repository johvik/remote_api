package remote.test.api;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.Test;

import remote.api.Utils;

/**
 * Test class for {@link Utils}.
 */
public class TestUtils {
	/**
	 * Test method for {@link Utils#Utils()}.
	 */
	@Test
	public void testUtils() {
		new Utils();
	}

	/**
	 * Test method for {@link Utils#toHex(byte[])}.
	 */
	@Test
	public void testToHex() {
		byte[] data = new byte[] { 0x01, 0x23, 0x45, 0x67, (byte) 0x89,
				(byte) 0xAB, (byte) 0xCD, (byte) 0xEF };
		assertEquals("01 23 45 67 89 AB CD EF", Utils.toHex(data));
		assertEquals("00", Utils.toHex(new byte[] { 0 }));
		assertEquals("", Utils.toHex(new byte[] {}));
		assertEquals("null", Utils.toHex(null));
		// Test a longer array
		int size = 8192;
		String hex = Utils.toHex(new byte[size]);
		assertEquals(size * 3 - 1, hex.length());
		assertThat(hex, startsWith("00 00 00 00 00 00 00 00"));
	}

	/**
	 * Test method for {@link Utils#generateRandom(int)}.
	 */
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

	/**
	 * Test method for {@link Utils#compare(byte[], byte[])}.
	 */
	@Test
	public void testCompare() {
		// Same array
		assertEquals(0,
				Utils.compare(Misc.getSequence(1, 10), Misc.getSequence(1, 10)));
		assertEquals(0, Utils.compare(new byte[0], new byte[0]));

		// Different lengths
		assertEquals(-1, Utils.compare(new byte[0], new byte[1]));
		assertEquals(1, Utils.compare(new byte[1], new byte[0]));

		// Less than
		assertEquals(-1,
				Utils.compare(new byte[] { 1, 0 }, new byte[] { 1, 1 }));
		assertEquals(-1, Utils.compare(new byte[] { 0 }, new byte[] { 1 }));

		// Greater than
		assertEquals(1, Utils.compare(new byte[] { 1, 1 }, new byte[] { 1, 0 }));
		assertEquals(1, Utils.compare(new byte[] { 1 }, new byte[] { 0 }));

		// Different null
		assertEquals(0, Utils.compare(null, null));
		assertEquals(-1, Utils.compare(null, new byte[0]));
		assertEquals(1, Utils.compare(new byte[0], null));
	}
}
