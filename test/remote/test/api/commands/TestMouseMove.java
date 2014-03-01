package remote.test.api.commands;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import remote.api.commands.MouseMove;
import remote.api.exceptions.PacketException;

@RunWith(Parameterized.class)
public class TestMouseMove {
	private short dx;
	private short dy;

	private MouseMove mm;

	public TestMouseMove(short dx, short dy) {
		this.dx = dx;
		this.dy = dy;
	}

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { (short) 0, (short) 0 },
				{ (short) -30, (short) 15 },
				{ Short.MIN_VALUE, Short.MIN_VALUE },
				{ Short.MAX_VALUE, Short.MAX_VALUE } });
	}

	@Before
	public void setUp() throws Exception {
		mm = new MouseMove(dx, dy);
	}

	@Test
	public void testWriteRead() throws PacketException {
		for (int i = 0; i < 10; i++) {
			byte[] data = new byte[MouseMove.LENGTH + i];
			mm.write(data, i);
			MouseMove read = MouseMove.read(data, i);
			assertEquals(MouseMove.MOUSE_MOVE, read.getType());
			assertEquals(dx, read.getDx());
			assertEquals(dy, read.getDy());
			assertEquals(mm.getType(), read.getType());
			assertEquals(mm.getDx(), read.getDx());
			assertEquals(mm.getDy(), read.getDy());
		}
	}

	@Test
	public void testWrite() {
		byte[] data = new byte[MouseMove.LENGTH];
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
		offset = -MouseMove.LENGTH;
		try {
			mm.write(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid write " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
	}

	@Test
	public void testRead() {
		byte[] data = new byte[MouseMove.LENGTH];
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
		offset = -MouseMove.LENGTH;
		try {
			MouseMove.read(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid read " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
	}

	public void testGetLength() {
		assertEquals(MouseMove.LENGTH, mm.getLength());
	}

	@Test
	public void testGetType() {
		assertEquals(MouseMove.MOUSE_MOVE, mm.getType());
	}

	@Test
	public void testGetDx() {
		assertEquals(dx, mm.getDx());
	}

	@Test
	public void testGetDy() {
		assertEquals(dy, mm.getDy());
	}

	@Test
	public void testCompareTo() {
		// Check against object with another dx
		MouseMove other = new MouseMove((short) (dx - 1), dy);
		assertNotEquals(0, mm.compareTo(other));

		// Check against object with another dy
		other = new MouseMove(dx, (short) (dy - 1));
		assertNotEquals(0, mm.compareTo(other));

		// Compare to object with same parameters
		other = new MouseMove(dx, dy);
		assertEquals(0, mm.compareTo(other));

		// Compare to self
		assertEquals(0, mm.compareTo(mm));
	}
}
