package remote.test.api;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
	 * Executor to run in another thread.
	 */
	private ExecutorService es = Executors.newFixedThreadPool(1);

	/**
	 * Writes data in another thread.
	 * 
	 * @param output
	 *            The destination.
	 * @param data
	 *            The data to write.
	 */
	private void write(final PipedOutputStream output, final byte[] data) {
		es.execute(new Runnable() {
			@Override
			public void run() {
				try {
					output.write(data);
					output.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Writes data in another thread.
	 * 
	 * @param output
	 *            The destination.
	 * @param packet
	 *            The data to write.
	 */
	private void write(final PipedOutputStream output, final Packet packet) {
		write(output, packet, 1);
	}

	/**
	 * Writes data in another thread.
	 * 
	 * @param output
	 *            The destination.
	 * @param packet
	 *            The data to write.
	 * @param times
	 *            Number of times to write the data.
	 */
	private void write(final PipedOutputStream output, final Packet packet,
			final int times) {
		es.execute(new Runnable() {
			@Override
			public void run() {
				try {
					for (int i = 0; i < times; i++) {
						packet.write(Misc.blockEncrypt, output);
					}
					output.flush();
				} catch (PacketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

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
	@Test(timeout = 10000)
	public void testNextPacket() throws Exception {
		PipedOutputStream output = new PipedOutputStream();
		PipedInputStream input = new PipedInputStream(output);
		PacketScanner ps = new PacketScanner(input);

		// Write packet with size 0
		write(output, new byte[2]);
		Packet p = ps.nextPacket();
		assertArrayEquals(new byte[0], p.getData());

		// Write a Ping
		Ping ping = new Ping(true);
		write(output, ping.pack());
		p = ps.nextPacket();
		assertEquals(0, ping.compareTo(p.decode(Misc.blockDecrypt)));

		// Write enough data to cause it to wrap
		int count = (PacketScanner.BUFFER_SIZE / Ping.LENGTH) + 1;
		write(output, ping.pack(), count);
		for (int i = 0; i < count; i++) {
			p = ps.nextPacket();
			assertEquals(0, ping.compareTo(p.decode(Misc.blockDecrypt)));
		}

		// Make sure write thread has stopped
		es.shutdown();
		assertEquals(true,
				es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS));

		// Check that it returns null when reading -1
		output.close();
		p = ps.nextPacket();
		assertEquals(null, p);
		input.close();
	}
}
