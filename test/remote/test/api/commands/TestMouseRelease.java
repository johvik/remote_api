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
import remote.api.commands.MouseRelease;
import remote.api.exceptions.PacketException;

@RunWith(Parameterized.class)
public class TestMouseRelease {
	private int buttons;

	private MouseRelease mr;

	public TestMouseRelease(int buttons) {
		this.buttons = buttons;
	}

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { 0 }, { Integer.MIN_VALUE },
				{ Integer.MAX_VALUE } });
	}

	@Before
	public void setUp() throws Exception {
		mr = new MouseRelease(buttons);
	}

	@Test
	public void testWriteRead() throws PacketException {
		for (int i = 0; i < 10; i++) {
			byte[] data = new byte[mr.getLength() + i];
			mr.write(data, i);
			MouseRelease read = MouseRelease.read(data, i);
			assertEquals(Command.MOUSE_RELEASE, read.getType());
			assertEquals(buttons, read.getButtons());
			assertEquals(mr.getType(), read.getType());
			assertEquals(mr.getButtons(), read.getButtons());
		}
	}

	@Test
	public void testWrite() {
		byte[] data = new byte[mr.getLength()];
		int offset = 1;
		try {
			mr.write(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid write " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		data = new byte[0];
		offset = -mr.getLength();
		try {
			mr.write(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid write " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
	}

	@Test
	public void testRead() {
		byte[] data = new byte[mr.getLength()];
		int offset = 1;
		try {
			MouseRelease.read(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid read " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		data = new byte[0];
		offset = -mr.getLength();
		try {
			MouseRelease.read(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid read " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
	}

	public void testGetLength() {
		assertEquals(MouseRelease.LENGTH, mr.getLength());
	}

	@Test
	public void testGetType() {
		assertEquals(Command.MOUSE_RELEASE, mr.getType());
	}

	@Test
	public void testGetButtons() {
		assertEquals(buttons, mr.getButtons());
	}

	@Test
	public void testCompareTo() {
		try {
			mr.compareTo(null);
			fail("Did not throw an exception");
		} catch (NullPointerException e) {
		}
		try {
			mr.compareTo(new MouseMove((short) 0, (short) 0));
			fail("Did not throw an exception");
		} catch (ClassCastException e) {
		}

		// Check against object with another buttons
		MouseRelease other = new MouseRelease(buttons - 1);
		assertNotEquals(0, mr.compareTo(other));

		// Compare to object with same parameters
		other = new MouseRelease(buttons);
		assertEquals(0, mr.compareTo(other));

		// Compare to self
		assertEquals(0, mr.compareTo(mr));
	}
}
