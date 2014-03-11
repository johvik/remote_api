package remote.test.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * A class that can transfer data from the output stream to this input stream.
 */
public class MagicInputStream extends ByteArrayInputStream {
	/**
	 * The output stream.
	 */
	private ByteArrayOutputStream output;

	/**
	 * Constructs a new input stream.
	 * 
	 * @param output
	 *            The output stream to fetch data from.
	 */
	public MagicInputStream(ByteArrayOutputStream output) {
		super(new byte[0]);
		this.output = output;
	}

	/**
	 * Updates the input stream with new data from the output stream.
	 */
	public void update() {
		buf = output.toByteArray();
		output.reset();
		count = buf.length;
		pos = 0;
		mark = 0;
	}
}
