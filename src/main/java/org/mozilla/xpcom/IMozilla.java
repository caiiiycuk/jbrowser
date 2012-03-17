package org.mozilla.xpcom;

import java.io.File;

public abstract interface IMozilla {
	public abstract void initialize(File paramFile)
			throws XPCOMInitializationException;

	public abstract long getNativeHandleFromAWT(Object paramObject);
}