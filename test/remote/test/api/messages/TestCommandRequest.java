package remote.test.api.messages;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import remote.api.commands.Command;
import remote.api.commands.MouseMove;
import remote.api.commands.MousePress;
import remote.api.exceptions.PacketException;
import remote.api.messages.CommandRequest;
import remote.api.messages.Message;
import remote.api.messages.Ping;

@RunWith(Parameterized.class)
public class TestCommandRequest {
	private Command command;
	private CommandRequest request;

	public TestCommandRequest(Command command) throws PacketException {
		this.command = command;
		request = new CommandRequest(command);
	}

	@Parameters
	public static Collection<Object[]> data() {
		List<Object[]> list = Arrays.asList(new Object[][] {
				{ new MouseMove((short) -1, (short) 100) },
				{ new MousePress(0x12345678) } });
		assertEquals(Command.USED_CODES, list.size());
		return list;
	}

	@Test
	public void testPack() throws PacketException {
		// Test by packing followed by unpacking
		byte[] data = request.pack().getData();
		CommandRequest r = CommandRequest.unpack(data);
		assertEquals(Message.COMMAND_REQUEST, data[0]);
		assertEquals(0, command.compareTo(request.getCommand()));
		assertEquals(Message.COMMAND_REQUEST, request.getType());
		// Check that they are the same
		assertEquals(request.getType(), r.getType());
		assertEquals(0, command.compareTo(r.getCommand()));

		// Test packing an artificial command that always throws
		try {
			new CommandRequest(new Command() {
				@Override
				public int compareTo(Command o) {
					return 0;
				}

				@Override
				public void write(byte[] data, int offset)
						throws PacketException {
					throw new PacketException("Expected exception", null);
				}

				@Override
				public byte getType() {
					return 0;
				}

				@Override
				public int getLength() {
					return 0;
				}
			}).pack();
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Expected exception", null);
			assertEquals(ex.getMessage(), e.getMessage());
		}
	}

	@Test
	public void testGetType() throws PacketException {
		// Ensure it has the correct type
		assertEquals(Message.COMMAND_REQUEST, request.getType());
	}

	@Test
	public void testCommandRequest() throws PacketException {
		try {
			new CommandRequest(null);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Command is null", null);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		new CommandRequest(command);
	}

	@Test
	public void testUnpack() throws PacketException {
		// Check that it throws when it has wrong static length
		byte[] data = new byte[0];
		try {
			CommandRequest.unpack(data);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Unexpected length", data);
			assertEquals(ex.getMessage(), e.getMessage());
		}

		// Check that it throws when it has wrong length
		data = new byte[CommandRequest.STATIC_LENGTH + 1];
		try {
			CommandRequest.unpack(data);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Invalid read "
					+ CommandRequest.STATIC_LENGTH, data);
			assertEquals(ex.getMessage(), e.getMessage());
		}

		// Correct length should not throw
		data = new byte[CommandRequest.STATIC_LENGTH + command.getLength()];
		data[1] = command.getType();
		command.write(data, CommandRequest.STATIC_LENGTH);
		CommandRequest r = CommandRequest.unpack(data);
		assertEquals(0, command.compareTo(r.getCommand()));
	}

	@Test
	public void testGetCommand() {
		assertEquals(command.getClass(), request.getCommand().getClass());
		assertEquals(command.getLength(), request.getCommand().getLength());
		assertEquals(command.getType(), request.getCommand().getType());
		assertEquals(0, command.compareTo(request.getCommand()));
	}

	@Test
	public void testUnknownCommandTypes() {
		int codes = 0; // Count number of correct codes
		byte[] data = new byte[8];
		for (int i = 0; i <= 0xFF; i++) {
			data[1] = (byte) i;
			try {
				CommandRequest.unpack(data);
				codes++; // OK and correct length
			} catch (PacketException e) {
				String message = e.getMessage();
				if (message.indexOf("Invalid read") == 0) {
					codes++; // OK but wrong length
					PacketException ex = new PacketException("Invalid read "
							+ CommandRequest.STATIC_LENGTH, data);
					assertEquals(ex.getMessage(), message);
				} else {
					// Not OK, wrong code
					PacketException ex = new PacketException(
							"Unknown command message", data);
					assertEquals(ex.getMessage(), message);
				}
			}
		}
		assertEquals(Command.USED_CODES, codes);
	}

	@Test
	public void testCompareTo() throws PacketException {
		try {
			request.compareTo(null);
			fail("Did not throw an exception");
		} catch (NullPointerException e) {
		}
		try {
			request.compareTo(new Ping(false));
			fail("Did not throw an exception");
		} catch (ClassCastException e) {
		}

		// Check against object with another command
		CommandRequest other = new CommandRequest(new MouseMove((short) 0,
				(short) 0));
		assertNotEquals(0, request.compareTo(other));

		// Compare to self
		assertEquals(0, request.compareTo(request));
	}
}
