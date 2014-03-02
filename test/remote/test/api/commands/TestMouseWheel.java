package remote.test.api.commands;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import remote.api.commands.Command;
import remote.api.commands.MouseMove;
import remote.api.commands.MouseWheel;
import remote.api.exceptions.PacketException;

@RunWith(Parameterized.class)
public class TestMouseWheel {
	private int wheelAmt;

	private MouseWheel mw;

	public TestMouseWheel(int wheelAmt) {
		this.wheelAmt = wheelAmt;
	}

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { 0 }, { Integer.MIN_VALUE },
				{ Integer.MAX_VALUE } });
	}

	@Before
	public void setUp() throws Exception {
		mw = new MouseWheel(wheelAmt);
	}

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

	public void testGetLength() {
		assertEquals(MouseWheel.LENGTH, mw.getLength());
	}

	@Test
	public void testGetType() {
		assertEquals(Command.MOUSE_WHEEL, mw.getType());
	}

	@Test
	public void testGetButtons() {
		assertEquals(wheelAmt, mw.getWheelAmt());
	}

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

		// Check against object with another buttons
		MouseWheel other = new MouseWheel(wheelAmt - 1);
		assertNotEquals(0, mw.compareTo(other));

		// Compare to object with same parameters
		other = new MouseWheel(wheelAmt);
		assertEquals(0, mw.compareTo(other));

		// Compare to self
		assertEquals(0, mw.compareTo(mw));
	}
}
