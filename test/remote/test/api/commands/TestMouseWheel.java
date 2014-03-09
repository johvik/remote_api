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
import remote.api.commands.MouseWheel;
import remote.api.exceptions.PacketException;

/**
 * Test class for {@link MouseWheel}.
 */
@RunWith(Parameterized.class)
public class TestMouseWheel {
	/**
	 * The wheelAmt parameter.
	 */
	private int wheelAmt;

	/**
	 * The mouse wheel constructed by the parameter.
	 */
	private MouseWheel mw;

	/**
	 * Constructs the mouse wheel from the parameter.
	 * 
	 * @param wheelAmt
	 *            The wheelAmt.
	 */
	public TestMouseWheel(int wheelAmt) {
		this.wheelAmt = wheelAmt;
		mw = new MouseWheel(wheelAmt);
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
			byte[] data = new byte[mw.getLength() + i];
			mw.write(data, i);
			MouseWheel read = MouseWheel.read(data, i);
			assertEquals(Command.MOUSE_WHEEL, read.getType());
			assertEquals(wheelAmt, read.getWheelAmt());
			assertEquals(mw.getType(), read.getType());
			assertEquals(mw.getWheelAmt(), read.getWheelAmt());
		}
	}

	/**
	 * Test method for {@link MouseWheel#write(byte[], int)}.
	 */
	@Test
	public void testWrite() {
		byte[] data = new byte[mw.getLength()];
		int offset = 1;
		try {
			mw.write(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid write " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		data = new byte[0];
		offset = -mw.getLength();
		try {
			mw.write(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid write " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
	}

	/**
	 * Test method for {@link MouseWheel#read(byte[], int)}.
	 */
	@Test
	public void testRead() {
		byte[] data = new byte[mw.getLength()];
		int offset = 1;
		try {
			MouseWheel.read(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid read " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		data = new byte[0];
		offset = -mw.getLength();
		try {
			MouseWheel.read(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid read " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
	}

	/**
	 * Test method for {@link MouseWheel#getLength()}.
	 */
	public void testGetLength() {
		assertEquals(MouseWheel.LENGTH, mw.getLength());
	}

	/**
	 * Test method for {@link MouseWheel#getType()}.
	 */
	@Test
	public void testGetType() {
		assertEquals(Command.MOUSE_WHEEL, mw.getType());
	}

	/**
	 * Test method for {@link MouseWheel#getWheelAmt()}.
	 */
	@Test
	public void testGetWheelAmt() {
		assertEquals(wheelAmt, mw.getWheelAmt());
	}

	/**
	 * Test method for {@link MouseWheel#compareTo(Command)}.
	 */
	@Test
	public void testCompareTo() {
		try {
			mw.compareTo(null);
			fail("Did not throw an exception");
		} catch (NullPointerException e) {
		}
		try {
			mw.compareTo(new MouseMove((short) 0, (short) 0));
			fail("Did not throw an exception");
		} catch (ClassCastException e) {
		}

		// Check against object with another wheel amount
		MouseWheel other = new MouseWheel(wheelAmt - 1);
		assertNotEquals(0, mw.compareTo(other));

		// Compare to object with same parameters
		other = new MouseWheel(wheelAmt);
		assertEquals(0, mw.compareTo(other));

		// Compare to self
		assertEquals(0, mw.compareTo(mw));
	}
}
