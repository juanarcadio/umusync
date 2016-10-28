package umu.sakai.umusync.api.secure;


/**
 * Exception thrown when an permission is needed
 *
 */
public class PermissionException
		extends RuntimeException {

	private static final long serialVersionUID = -7368551002524893216L;
	private Object [] arguments;

	/**
	 * Constructor.
	 */
	public PermissionException() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param message describing the exception
	 */
	public PermissionException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param message describing the exception
	 * @param ex the wrapped exception
	 */
	public PermissionException(String message, Throwable ex) {
		super(message, ex);
	}

	/**
	 * Constructor.
	 * 
	 * @param ex the wrapped exception
	 */
	public PermissionException(Throwable ex) {
		super(ex);
	}

	public void setArguments(Object [] args) { this.arguments = args; }
	public Object[] getArguments() { return arguments; }

}