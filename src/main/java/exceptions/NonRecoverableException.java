package exceptions;

public class NonRecoverableException extends Exception {

	public NonRecoverableException(String message, Exception e) {
		super(message,e);
	}

}
