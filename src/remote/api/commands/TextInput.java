package remote.api.commands;

import remote.api.exceptions.PacketException;

/**
 * Command class for text input.
 */
public class TextInput extends Command {
	/**
	 * Minimum number of bytes needed for the command.
	 */
	public static final int STATIC_LENGTH = 2;
	/**
	 * Maximum length of the text.
	 */
	public static final int MAX_TEXT_LENGTH = 0xFF;
	/**
	 * Text for the input.
	 */
	private String text;

	/**
	 * Constructs a new text input. Note that the text will be clipped of if
	 * longer than {@link TextInput#MAX_TEXT_LENGTH}.
	 * 
	 * @param text
	 *            The input text.
	 */
	public TextInput(String text) {
		this.text = text;
	}

	/**
	 * Retrieves the text of the command.
	 * 
	 * @return The text.
	 */
	public String getText() {
		return text;
	}

	@Override
	public int compareTo(Command o) {
		TextInput other = (TextInput) o;
		return text.compareTo(other.text);
	}

	@Override
	public void write(byte[] data, int offset) throws PacketException {
		int length = text.length();
		if (offset < 0 || data.length < STATIC_LENGTH + length + offset) {
			throw new PacketException("Invalid write " + offset, data);
		}
		data[offset] = TEXT_INPUT;
		data[offset + 1] = (byte) (length & 0xFF);
		int pos = offset + 2;
		for (int i = 0; i < length; i++) {
			data[pos++] = (byte) text.charAt(i);
		}
	}

	/**
	 * Attempts to read a TextInput from data.
	 * 
	 * @param data
	 *            The data to read.
	 * @param offset
	 *            Start offset in data.
	 * @return The read command.
	 * @throws PacketException
	 *             If offset or length of data makes the read impossible.
	 */
	public static TextInput read(byte[] data, int offset)
			throws PacketException {
		if (offset < 0 || data.length < STATIC_LENGTH + offset) {
			throw new PacketException("Invalid read " + offset, data);
		}
		int length = data[offset + 1] & 0xFF;
		if (data.length < STATIC_LENGTH + length + offset) {
			throw new PacketException("Invalid read " + offset, data);
		}
		char[] text = new char[length];
		int pos = offset + 2;
		for (int i = 0; i < length; i++) {
			text[i] = (char) data[pos++];
		}
		return new TextInput(new String(text));
	}

	@Override
	public int getLength() {
		return STATIC_LENGTH + Math.min(text.length(), MAX_TEXT_LENGTH);
	}

	@Override
	public byte getType() {
		return TEXT_INPUT;
	}
}
