package remote.api;

import java.io.IOException;
import java.io.InputStream;

import remote.api.exceptions.PacketException;
import remote.api.messages.Message;

/**
 * A class that scans an input stream for packets.
 */
public class PacketScanner {
	/**
	 * The size of the read buffer.
	 */
	public static final int BUFFER_SIZE = 1024;
	/**
	 * The read buffer.
	 */
	private byte[] buffer;
	/**
	 * The input stream.
	 */
	private InputStream input;
	/**
	 * Position in the buffer.
	 */
	private int bufferOffset;
	/**
	 * Bytes left in the buffer.
	 */
	private int bufferAvailable;
	/**
	 * Scan position in the buffer.
	 */
	private int scanOffset;
	/**
	 * Bytes left for the scanner.
	 */
	private int scanAvailable;

	/**
	 * Constructs a new packet scanner.
	 * 
	 * @param input
	 *            The input stream to read from.
	 * @throws PacketException
	 *             If input is null.
	 */
	public PacketScanner(InputStream input) throws PacketException {
		if (input == null) {
			throw new PacketException("Input stream is null", null);
		}
		this.input = input;
		buffer = new byte[BUFFER_SIZE];
		bufferOffset = 0;
		bufferAvailable = buffer.length;
		scanOffset = 0;
		scanAvailable = 0;
	}

	/**
	 * Blocks until the next packet is available from the input stream.
	 * 
	 * @return The next packet or null if -1 is read.
	 * @throws IOException
	 *             If it fails while reading data.
	 * @throws PacketException
	 *             If it fails while reading the packet.
	 */
	public Packet nextPacket() throws IOException, PacketException {
		while (true) {
			// Check for complete packet in the buffer
			Packet packet = checkForPacket();
			if (packet != null) {
				return packet;
			}

			// Check if it is impossible to fill buffer with more data
			// (This is impossible due to Message length check in Packet.read)
			// if (bufferAvailable <= 0) {
			// throw new PacketException("Buffer overflow", buffer);
			// }

			// Read more data into the buffer
			int read = input.read(buffer, bufferOffset, bufferAvailable);
			if (read == -1) {
				return null;
			}
			bufferOffset += read;
			bufferAvailable -= read;
			scanAvailable += read;
		}
	}

	/**
	 * Checks for a packet in the buffer.
	 * 
	 * @return The packet if available otherwise null
	 * @throws PacketException
	 *             If it fails while reading the packet.
	 */
	private Packet checkForPacket() throws PacketException {
		Packet packet = Packet.read(buffer, scanOffset, scanAvailable);
		if (packet != null) {
			// Consume
			int consumed = packet.length() + 2; // 2 bytes for length
			scanOffset += consumed;
			scanAvailable -= consumed;
			if (scanAvailable == 0) {
				// Reset buffer
				bufferOffset = 0;
				bufferAvailable = buffer.length;
				scanOffset = 0;
			} else if (bufferAvailable < Message.MAX_LENGTH) {
				// Move remaining data to start of buffer
				System.arraycopy(buffer, scanOffset, buffer, 0, scanAvailable);
				bufferOffset = scanAvailable;
				bufferAvailable = buffer.length - scanAvailable;
				scanOffset = 0;
			}
		}
		return packet;
	}
}
