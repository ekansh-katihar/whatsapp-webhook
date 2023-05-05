package exceptions;

public class NonRecoverableException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NonRecoverableException(String message, Exception e) {
		super(message,e);
	}

}
