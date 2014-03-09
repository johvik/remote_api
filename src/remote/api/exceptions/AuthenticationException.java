package remote.api.exceptions;

/**
 * Authentication exception, to be used when authentication fails.
 */
public class AuthenticationException extends ProtocolException {
	/**
	 * Random UID.
	 */
	private static final long serialVersionUID = 3577363556401414633L;

	/**
	 * Constructs a new authentication exception.
	 * 
	 * @param message
	 *            Message of the exception.
	 */
	public AuthenticationException(String message) {
		super(message);
	}
}
