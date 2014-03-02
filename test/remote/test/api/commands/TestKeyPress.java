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
import remote.api.commands.KeyPress;
import remote.api.commands.MouseMove;
import remote.api.exceptions.PacketException;

@RunWith(Parameterized.class)
public class TestKeyPress {
	private int keycode;

	private KeyPress kp;

	public TestKeyPress(int keycode) {
		this.keycode = keycode;
	}

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { 0 }, { Integer.MIN_VALUE },
				{ Integer.MAX_VALUE } });
	}

	@Before
	public void setUp() throws Exception {
		kp = new KeyPress(keycode);
	}

	@Test
	public void testWriteRead() throws PacketException {
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

	public void testGetLength() {
		assertEquals(KeyPress.LENGTH, kp.getLength());
	}

	@Test
	public void testGetType() {
		assertEquals(Command.KEY_PRESS, kp.getType());
	}

	@Test
	public void testGetButtons() {
		assertEquals(keycode, kp.getKeycode());
	}

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
