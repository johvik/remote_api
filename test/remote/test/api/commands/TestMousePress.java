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
import remote.api.commands.MousePress;
import remote.api.exceptions.PacketException;

@RunWith(Parameterized.class)
public class TestMousePress {
	private int buttons;

	private MousePress mp;

	public TestMousePress(int buttons) {
		this.buttons = buttons;
	}

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { 0 }, { Integer.MIN_VALUE },
				{ Integer.MAX_VALUE } });
	}

	@Before
	public void setUp() throws Exception {
		mp = new MousePress(buttons);
	}

	@Test
	public void testWriteRead() throws PacketException {
		for (int i = 0; i < 10; i++) {
			byte[] data = new byte[mp.getLength() + i];
			mp.write(data, i);
			MousePress read = MousePress.read(data, i);
			assertEquals(MousePress.MOUSE_PRESS, read.getType());
			assertEquals(buttons, read.getButtons());
			assertEquals(mp.getType(), read.getType());
			assertEquals(mp.getButtons(), read.getButtons());
		}
	}

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

	public void testGetLength() {
		assertEquals(MousePress.LENGTH, mp.getLength());
	}

	@Test
	public void testGetType() {
		assertEquals(MousePress.MOUSE_PRESS, mp.getType());
	}

	@Test
	public void testGetButtons() {
		assertEquals(buttons, mp.getButtons());
	}

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
