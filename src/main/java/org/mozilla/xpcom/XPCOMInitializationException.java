package org.mozilla.xpcom;

public class XPCOMInitializationException extends RuntimeException {
	private static final long serialVersionUID = -7067350325909231055L;

	public XPCOMInitializationException(String paramString) {
		super(paramString);
	}

	public XPCOMInitializationException(Throwable paramThrowable) {
		super(paramThrowable);
	}

	public XPCOMInitializationException(String paramString,
			Throwable paramThrowable) {
		super(paramString, paramThrowable);
	}
}