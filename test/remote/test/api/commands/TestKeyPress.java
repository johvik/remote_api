package remote.test.api.commands;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import remote.api.commands.Command;
import remote.api.commands.KeyPress;
import remote.api.commands.MouseMove;
import remote.api.exceptions.PacketException;

/**
 * Test class for {@link KeyPress}.
 */
@RunWith(Parameterized.class)
public class TestKeyPress {
	/**
	 * The key code parameter.
	 */
	private int keycode;

	/**
	 * The key press constructed by the parameter.
	 */
	private KeyPress kp;

	/**
	 * Constructs the key press from the parameter.
	 * 
	 * @param keycode
	 *            The key code.
	 */
	public TestKeyPress(int keycode) {
		this.keycode = keycode;
		kp = new KeyPress(keycode);
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
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testWriteRead() throws Exception {
		for (int i = 0; i < 10; i++) {
			byte[] data = new byte[kp.getLength() + i];
			kp.write(data, i);
			KeyPress read = KeyPress.read(data, i);
			assertEquals(Command.KEY_PRESS, read.getType());
			assertEquals(keycode, read.getKeycode());
			assertEquals(kp.getType(), read.getType());
			assertEquals(kp.getKeycode(), read.getKeycode());
		}
	}

	/**
	 * Test method for {@link KeyPress#write(byte[], int)}.
	 */
	@Test
	public void testWrite() {
		byte[] data = new byte[kp.getLength()];
		int offset = 1;
		try {
			kp.write(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid write " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		data = new byte[0];
		offset = -kp.getLength();
		try {
			kp.write(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid write " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
	}

	/**
	 * Test method for {@link KeyPress#read(byte[], int)}.
	 */
	@Test
	public void testRead() {
		byte[] data = new byte[kp.getLength()];
		int offset = 1;
		try {
			KeyPress.read(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid read " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		data = new byte[0];
		offset = -kp.getLength();
		try {
			KeyPress.read(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid read " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
	}

	/**
	 * Test method for {@link KeyPress#getLength()}.
	 */
	public void testGetLength() {
		assertEquals(KeyPress.LENGTH, kp.getLength());
	}

	/**
	 * Test method for {@link KeyPress#getType()}.
	 */
	@Test
	public void testGetType() {
		assertEquals(Command.KEY_PRESS, kp.getType());
	}

	/**
	 * Test method for {@link KeyPress#getKeycode()}.
	 */
	@Test
	public void testGetKeycode() {
		assertEquals(keycode, kp.getKeycode());
	}

	/**
	 * Test method for {@link KeyPress#compareTo(Command)}.
	 */
	@Test
	public void testCompareTo() {
		try {
			kp.compareTo(null);
			fail("Did not throw an exception");
		} catch (NullPointerException e) {
		}
		try {
			kp.compareTo(new MouseMove((short) 0, (short) 0));
			fail("Did not throw an exception");
		} catch (ClassCastException e) {
		}

		// Check against object with another key code
		KeyPress other = new KeyPress(keycode - 1);
		assertNotEquals(0, kp.compareTo(other));

		// Compare to object with same parameters
		other = new KeyPress(keycode);
		assertEquals(0, kp.compareTo(other));

		// Compare to self
		assertEquals(0, kp.compareTo(kp));
	}
}
