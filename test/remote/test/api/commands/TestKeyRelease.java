package remote.test.api.commands;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import remote.api.commands.Command;
import remote.api.commands.KeyRelease;
import remote.api.commands.MouseMove;
import remote.api.exceptions.PacketException;

/**
 * Test class for {@link KeyRelease}.
 */
@RunWith(Parameterized.class)
public class TestKeyRelease {
	/**
	 * The key code parameter.
	 */
	private int keycode;

	/**
	 * The key release constructed by the parameter.
	 */
	private KeyRelease kr;

	/**
	 * Constructs the key release from the parameter.
	 * 
	 * @param keycode
	 *            The key code.
	 */
	public TestKeyRelease(int keycode) {
		this.keycode = keycode;
		kr = new KeyRelease(keycode);
	}

	/**
	 * Creates input parameters.
	 * 
	 * @return The parameters.
	 */
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { 0 }, { Integer.MIN_VALUE },
				{ Integer.MAX_VALUE } });
	}

	/**
	 * Tests writing and reading the command.
	 * 
	 * @throws PacketException
	 *             If something went wrong.
	 */
	@Test
	public void testWriteRead() throws PacketException {
		for (int i = 0; i < 10; i++) {
			byte[] data = new byte[kr.getLength() + i];
			kr.write(data, i);
			KeyRelease read = KeyRelease.read(data, i);
			assertEquals(Command.KEY_RELEASE, read.getType());
			assertEquals(keycode, read.getKeycode());
			assertEquals(kr.getType(), read.getType());
			assertEquals(kr.getKeycode(), read.getKeycode());
		}
	}

	/**
	 * Test method for {@link KeyRelease#write(byte[], int)}.
	 */
	@Test
	public void testWrite() {
		byte[] data = new byte[kr.getLength()];
		int offset = 1;
		try {
			kr.write(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid write " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		data = new byte[0];
		offset = -kr.getLength();
		try {
			kr.write(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid write " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
	}

	/**
	 * Test method for {@link KeyRelease#read(byte[], int)}.
	 */
	@Test
	public void testRead() {
		byte[] data = new byte[kr.getLength()];
		int offset = 1;
		try {
			KeyRelease.read(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid read " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		data = new byte[0];
		offset = -kr.getLength();
		try {
			KeyRelease.read(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid read " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
	}

	/**
	 * Test method for {@link KeyRelease#getLength()}.
	 */
	public void testGetLength() {
		assertEquals(KeyRelease.LENGTH, kr.getLength());
	}

	/**
	 * Test method for {@link KeyRelease#getType()}.
	 */
	@Test
	public void testGetType() {
		assertEquals(Command.KEY_RELEASE, kr.getType());
	}

	/**
	 * Test method for {@link KeyRelease#getKeycode()}.
	 */
	@Test
	public void testGetKeycode() {
		assertEquals(keycode, kr.getKeycode());
	}

	/**
	 * Test method for {@link KeyRelease#compareTo(Command)}.
	 */
	@Test
	public void testCompareTo() {
		try {
			kr.compareTo(null);
			fail("Did not throw an exception");
		} catch (NullPointerException e) {
		}
		try {
			kr.compareTo(new MouseMove((short) 0, (short) 0));
			fail("Did not throw an exception");
		} catch (ClassCastException e) {
		}

		// Check against object with another key code
		KeyRelease other = new KeyRelease(keycode - 1);
		assertNotEquals(0, kr.compareTo(other));

		// Compare to object with same parameters
		other = new KeyRelease(keycode);
		assertEquals(0, kr.compareTo(other));

		// Compare to self
		assertEquals(0, kr.compareTo(kr));
	}
}
