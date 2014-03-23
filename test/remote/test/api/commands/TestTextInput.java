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
import remote.api.commands.TextInput;
import remote.api.exceptions.PacketException;
import remote.test.api.Misc;

/**
 * Test class for {@link TextInput}.
 */
@RunWith(Parameterized.class)
public class TestTextInput {
	/**
	 * The text parameter.
	 */
	private byte[] text;

	/**
	 * The text input constructed by the parameter.
	 */
	private TextInput ti;

	/**
	 * Constructs the text input from the parameter.
	 * 
	 * @param text
	 *            The text.
	 */
	public TestTextInput(byte[] text) {
		this.text = text;
		ti = new TextInput(text);
	}

	/**
	 * Creates input parameters.
	 * 
	 * @return The parameters.
	 */
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { Misc.getSequence(1, 10) },
				{ new byte[0] },
				{ Misc.getSequence(0, TextInput.MAX_TEXT_LENGTH) } });
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
			byte[] data = new byte[ti.getLength() + i];
			ti.write(data, i);
			TextInput read = TextInput.read(data, i);
			assertEquals(Command.TEXT_INPUT, read.getType());
			assertArrayEquals(text, read.getText());
			assertEquals(ti.getType(), read.getType());
			assertArrayEquals(ti.getText(), read.getText());
		}
	}

	/**
	 * Test method for {@link TextInput#write(byte[], int)}.
	 */
	@Test
	public void testWrite() {
		byte[] data = new byte[ti.getLength()];
		int offset = 1;
		try {
			ti.write(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid write " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		data = new byte[0];
		offset = -ti.getLength();
		try {
			ti.write(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid write " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
	}

	/**
	 * Test method for {@link TextInput#read(byte[], int)}.
	 */
	@Test
	public void testRead() {
		byte[] data = new byte[ti.getLength()];
		int offset = 1;
		if (data.length > offset + 1) {
			// Avoid array index out of bounds
			// when text length is 0
			data[offset + 1] = (byte) 0xFF;
		}
		try {
			TextInput.read(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid read " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		data = new byte[0];
		offset = -ti.getLength();
		try {
			TextInput.read(data, offset);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid read " + offset,
					data);
			assertEquals(ex.getMessage(), e.getMessage());
		}
	}

	/**
	 * Test method for {@link TextInput#getLength()}.
	 */
	public void testGetLength() {
		assertEquals(text.length + TextInput.STATIC_LENGTH, ti.getLength());
	}

	/**
	 * Test method for {@link TextInput#getType()}.
	 */
	@Test
	public void testGetType() {
		assertEquals(Command.TEXT_INPUT, ti.getType());
	}

	/**
	 * Test method for {@link TextInput#getText()}.
	 */
	@Test
	public void testGetButtons() {
		assertArrayEquals(text, ti.getText());
	}

	/**
	 * Test method for {@link TextInput#compareTo(Command)}.
	 */
	@Test
	public void testCompareTo() {
		try {
			ti.compareTo(null);
			fail("Did not throw an exception");
		} catch (NullPointerException e) {
		}
		try {
			ti.compareTo(new MouseMove((short) 0, (short) 0));
			fail("Did not throw an exception");
		} catch (ClassCastException e) {
		}

		// Check against object with another text
		TextInput other = new TextInput(Misc.getSequence(-15, 2));
		assertNotEquals(0, ti.compareTo(other));

		// Compare to object with same parameters
		other = new TextInput(text);
		assertEquals(0, ti.compareTo(other));

		// Compare to self
		assertEquals(0, ti.compareTo(ti));
	}
}
