package remote.api.exceptions;

/**
 * Protocol exception.
 */
public class ProtocolException extends Exception {
	/**
	 * Random UID.
	 */
	private static final long serialVersionUID = 2303375486057235648L;

	/**
	 * Constructs a new protocol exception.
	 * 
	 * @param message
	 *            Message of the exception.
	 */
	public ProtocolException(String message) {
		super(message);
	}

	/**
	 * Constructs a new protocol exception.
	 * 
	 * @param message
	 *            Message of the exception.
	 * @param t
	 *            The Throwable that triggered the exception.
	 */
	public ProtocolException(String message, Throwable t) {
		super(message + "\n" + t.getMessage(), t);
	}
}
