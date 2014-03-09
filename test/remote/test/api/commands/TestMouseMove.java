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
 * Test class for {@link MouseMove}.
 */
@RunWith(Parameterized.class)
public class TestMouseMove {
	/**
	 * The dx parameter.
	 */
	private short dx;
	/**
	 * The dy parameter.
	 */
	private short dy;

	/**
	 * The mouse move constructed by the parameters.
	 */
	private MouseMove mm;

	/**
	 * Constructs the mouse move from the parameters.
	 * 
	 * @param dx
	 *            The dx.
	 * @param dy
	 *            The dy.
	 */
	public TestMouseMove(short dx, short dy) {
		this.dx = dx;
		this.dy = dy;
		mm = new MouseMove(dx, dy);
	}

	/**
	 * Creates input parameters.
	 * 
	 * @return The parameters.
	 */
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { (short) 0, (short) 0 },
				{ (short) -30, (short) 15 },
				{ Short.MIN_VALUE, Short.MIN_VALUE },
				{ Short.MAX_VALUE, Short.MAX_VALUE } });
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
			byte[] data = new byte[mm.getLength() + i];
			mm.write(data, i);
			MouseMove read = MouseMove.read(data, i);
			assertEquals(Command.MOUSE_MOVE, read.getType());
			assertEquals(dx, read.getDx());
			assertEquals(dy, read.getDy());
			assertEquals(mm.getType(), read.getType());
			assertEquals(mm.getDx(), read.getDx());
			assertEquals(mm.getDy(), read.getDy());
		}
	}

	/**
	 * Test method for {@link MouseMove#write(byte[], int)}.
	 */
	@Test
	public void testWrite() {
		byte[] data = new byte[mm.getLength()];
		int offset = 1;
		try {
			mm.write(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid write " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		data = new byte[0];
		offset = -mm.getLength();
		try {
			mm.write(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid write " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
	}

	/**
	 * Test method for {@link MouseMove#read(byte[], int)}.
	 */
	@Test
	public void testRead() {
		byte[] data = new byte[mm.getLength()];
		int offset = 1;
		try {
			MouseMove.read(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid read " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		data = new byte[0];
		offset = -mm.getLength();
		try {
			MouseMove.read(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid read " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
	}

	/**
	 * Test method for {@link MouseMove#getLength()}.
	 */
	public void testGetLength() {
		assertEquals(MouseMove.LENGTH, mm.getLength());
	}

	/**
	 * Test method for {@link MouseMove#getType()}.
	 */
	@Test
	public void testGetType() {
		assertEquals(Command.MOUSE_MOVE, mm.getType());
	}

	/**
	 * Test method for {@link MouseMove#getDx()}.
	 */
	@Test
	public void testGetDx() {
		assertEquals(dx, mm.getDx());
	}

	/**
	 * Test method for {@link MouseMove#getDy()}.
	 */
	@Test
	public void testGetDy() {
		assertEquals(dy, mm.getDy());
	}

	/**
	 * Test method for {@link MouseMove#compareTo(Command)}.
	 */
	@Test
	public void testCompareTo() {
		try {
			mm.compareTo(null);
			fail("Did not throw an exception");
		} catch (NullPointerException e) {
		}
		try {
			mm.compareTo(new MousePress(0));
			fail("Did not throw an exception");
		} catch (ClassCastException e) {
		}

		// Check against object with another dx
		MouseMove other = new MouseMove((short) (dx - 1), dy);
		assertEquals(mm.getDy(), other.getDy());
		assertNotEquals(0, mm.compareTo(other));

		// Check against object with another dy
		other = new MouseMove(dx, (short) (dy - 1));
		assertEquals(mm.getDx(), other.getDx());
		assertNotEquals(0, mm.compareTo(other));

		// Compare to object with same parameters
		other = new MouseMove(dx, dy);
		assertEquals(0, mm.compareTo(other));

		// Compare to self
		assertEquals(0, mm.compareTo(mm));
	}
}
