package remote.test.api;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

import remote.api.Packet;
import remote.api.PacketScanner;
import remote.api.exceptions.PacketException;
import remote.api.messages.Ping;

/**
 * Test class for {@link PacketScanner}.
 */
public class TestPacketScanner {
	/**
	 * Test method for {@link PacketScanner#PacketScanner(java.io.InputStream)}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testPacketScanner() throws Exception {
		try {
			new PacketScanner(null);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			PacketException ex = new PacketException("Input stream is null",
					null);
			assertEquals(ex.getMessage(), e.getMessage());
		}
		// Valid construction
		new PacketScanner(new ByteArrayInputStream(new byte[0]));
	}

	/**
	 * Test method for {@link PacketScanner#nextPacket()}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testNextPacket() throws Exception {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		MagicInputStream input = new MagicInputStream(output);
		PacketScanner ps = new PacketScanner(input);

		// Write packet with size 0
		output.write(new byte[2]);
		input.update();
		Packet p = ps.nextPacket();
		assertArrayEquals(new byte[0], p.getData());

		// Write a Ping
		Ping ping = new Ping(true);
		ping.pack().write(Misc.blockEncrypt, output);
		input.update();
		p = ps.nextPacket();
		assertEquals(0, ping.compareTo(p.decode(Misc.blockDecrypt)));

		// Write enough data to cause it to wrap
		int count = (PacketScanner.BUFFER_SIZE / Ping.LENGTH) + 1;
		for (int i = 0; i < count; i++) {
			ping.pack().write(Misc.blockEncrypt, output);
		}
		// Read the written data
		input.update();
		for (int i = 0; i < count; i++) {
			p = ps.nextPacket();
			assertEquals(0, ping.compareTo(p.decode(Misc.blockDecrypt)));
		}

		// Check that it returns null when reading -1
		p = ps.nextPacket();
		assertEquals(null, p);
		input.close();
		output.close();
	}
}
