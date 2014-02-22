package remote.api.exceptions;

public class AuthenticationException extends ProtocolException {
	private static final long serialVersionUID = 3577363556401414633L;

	public AuthenticationException(String message) {
		super(message);
	}
}
