package org.mozilla.browser;

/**
 * Exception for runtime errors in mozilla related code.
 *
 * Because MozillaRuntimeException is a subclass of
 * {@link RuntimeException}, it does not have to
 * be handled explicitely.
 */
public class MozillaRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -236477723372982615L;

    /**
     * Constructs a new mozilla runtime exception with
     * <code>null</code> as its detail message and
     * <code>null</code> as cause.
     */
    public MozillaRuntimeException() {
    }

    /**
     * Constructs a new mozilla runtime exception
     * with <code>null</code> as cause.
     *
     * @param message - detail message
     */
    public MozillaRuntimeException(String message) {
        super(message);
    }

    /**
     * Constructs a new mozilla runtime exception.
     *
     * @param message - detail message
     * @param cause - cause (parent) exception
     */
    public MozillaRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new mozilla runtime exception
     * with <code>null</code> as its detail message.
     *
     * @param cause - cause (parent) exception
     */
    public MozillaRuntimeException(Throwable cause) {
        super(cause);
    }

}
