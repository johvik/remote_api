package remote.api.exceptions;

public class ProtocolException extends Exception {
	private static final long serialVersionUID = 2303375486057235648L;

	public ProtocolException(String message) {
		super(message);
	}

	public ProtocolException(String message, Throwable t) {
		super(message + "\n" + t.getMessage(), t);
	}
}
