package org.mozilla.xpcom;

public abstract interface IJavaXPCOMUtils {
	public abstract long wrapJavaObject(Object paramObject, String paramString);

	public abstract Object wrapXPCOMObject(long paramLong, String paramString);
}