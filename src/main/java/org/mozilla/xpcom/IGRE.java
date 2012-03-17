package org.mozilla.xpcom;

import java.io.File;

public abstract interface IGRE {
	public abstract void initEmbedding(File paramFile1, File paramFile2,
			IAppFileLocProvider paramIAppFileLocProvider) throws XPCOMException;

	public abstract void termEmbedding();

	public abstract ProfileLock lockProfileDirectory(File paramFile)
			throws XPCOMException;

	public abstract void notifyProfile();
}