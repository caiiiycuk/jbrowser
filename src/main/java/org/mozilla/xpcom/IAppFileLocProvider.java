package org.mozilla.xpcom;

import java.io.File;

public abstract interface IAppFileLocProvider {
	public abstract File getFile(String paramString,
			boolean[] paramArrayOfBoolean);

	public abstract File[] getFiles(String paramString);
}