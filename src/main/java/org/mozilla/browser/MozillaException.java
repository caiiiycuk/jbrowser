package org.mozilla.browser;

/**
 * Exception for errors in mozilla related code that
 * are meant to be handled by the application logic
 */
public class MozillaException extends  Exception {

    private static final long serialVersionUID = -4186780198088841453L;

    /**
     * Constructs a new mozilla exception with
     * <code>null</code> as its detail message and
     * <code>null</code> as cause.
     */
    public MozillaException() {
    }

    /**
     * Constructs a new mozilla exception
     * with <code>null</code> as cause.
     *
     * @param message - detail message
     */
    public MozillaException(String message) {
        super(message);
    }

    /**
     * Constructs a new mozilla exception.
     *
     * @param message - detail message
     * @param cause - cause (parent) exception
     */
    public MozillaException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new mozilla exception
     * with <code>null</code> as its detail message.
     *
     * @param cause - cause (parent) exception
     */
    public MozillaException(Throwable cause) {
        super(cause);
    }

}
