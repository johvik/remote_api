package remote.test.api.commands;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import remote.api.commands.Command;
import remote.api.commands.MouseMove;
import remote.api.commands.MousePress;
import remote.api.exceptions.PacketException;

/**
 * Test class for {@link MousePress}.
 */
@RunWith(Parameterized.class)
public class TestMousePress {
	/**
	 * The buttons parameter.
	 */
	private int buttons;

	/**
	 * The mouse press constructed by the parameter.
	 */
	private MousePress mp;

	/**
	 * Constructs the mouse press from the parameter.
	 * 
	 * @param buttons
	 *            The buttons.
	 */
	public TestMousePress(int buttons) {
		this.buttons = buttons;
		mp = new MousePress(buttons);
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
			byte[] data = new byte[mp.getLength() + i];
			mp.write(data, i);
			MousePress read = MousePress.read(data, i);
			assertEquals(Command.MOUSE_PRESS, read.getType());
			assertEquals(buttons, read.getButtons());
			assertEquals(mp.getType(), read.getType());
			assertEquals(mp.getButtons(), read.getButtons());
		}
	}

	/**
	 * Test method for {@link MousePress#write(byte[], int)}.
	 */
	@Test
	public void testWrite() {
		byte[] data = new byte[mp.getLength()];
		int offset = 1;
		try {
			mp.write(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid write " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		data = new byte[0];
		offset = -mp.getLength();
		try {
			mp.write(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid write " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
	}

	/**
	 * Test method for {@link MousePress#read(byte[], int)}.
	 */
	@Test
	public void testRead() {
		byte[] data = new byte[mp.getLength()];
		int offset = 1;
		try {
			MousePress.read(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid read " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		data = new byte[0];
		offset = -mp.getLength();
		try {
			MousePress.read(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid read " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
	}

	/**
	 * Test method for {@link MousePress#getLength()}.
	 */
	public void testGetLength() {
		assertEquals(MousePress.LENGTH, mp.getLength());
	}

	/**
	 * Test method for {@link MousePress#getType()}.
	 */
	@Test
	public void testGetType() {
		assertEquals(Command.MOUSE_PRESS, mp.getType());
	}

	/**
	 * Test method for {@link MousePress#getButtons()}.
	 */
	@Test
	public void testGetButtons() {
		assertEquals(buttons, mp.getButtons());
	}

	/**
	 * Test method for {@link MousePress#compareTo(Command)}.
	 */
	@Test
	public void testCompareTo() {
		try {
			mp.compareTo(null);
			fail("Did not throw an exception");
		} catch (NullPointerException e) {
		}
		try {
			mp.compareTo(new MouseMove((short) 0, (short) 0));
			fail("Did not throw an exception");
		} catch (ClassCastException e) {
		}

		// Check against object with another buttons
		MousePress other = new MousePress(buttons - 1);
		assertNotEquals(0, mp.compareTo(other));

		// Compare to object with same parameters
		other = new MousePress(buttons);
		assertEquals(0, mp.compareTo(other));

		// Compare to self
		assertEquals(0, mp.compareTo(mp));
	}
}
