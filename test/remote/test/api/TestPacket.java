package remote.test.api;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.util.Random;

import org.junit.Test;

import remote.api.Packet;
import remote.api.exceptions.PacketException;

public class TestPacket {

	@Test
	public void testPacket() {
		try {
			new Packet(null);
			fail("Did not throw an exception");
		} catch (PacketException e) {
			assertEquals("Data is null null", e.getMessage());
		}
		try {
			// Does not match block size
			new Packet(new byte[] { 0 });
			fail("Did not throw an exception");
		} catch (PacketException e) {
			assertEquals("Invalid data length 00", e.getMessage());
		}
	}

	@Test
	public void testGetPacketSize() {
		// Test some numbers
		for (int i = 0; i < 200; i++) {
			int size = Packet.getPacketSize(i);
			assertThat(size, greaterThanOrEqualTo(i));
		}
		// Check some random numbers
		Random random = new Random();
		for (int i = 0; i < 100; i++) {
			int num = random.nextInt(Integer.MAX_VALUE);
			int size = Packet.getPacketSize(num);
			assertThat(size, greaterThanOrEqualTo(num));
		}
	}

	@Test
	public void testRead() {
		// TODO
	}

	@Test
	public void testWrite() {
		// TODO
	}

	@Test
	public void testDecode() {
		// TODO
	}

	@Test
	public void testLength() {
		// TODO
	}

	@Test
	public void testGetData() {
		// TODO
	}

	@Test
	public void testToString() {
		// TODO
	}
}
