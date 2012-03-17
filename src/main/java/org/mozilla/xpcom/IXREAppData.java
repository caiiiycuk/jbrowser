package org.mozilla.xpcom;

import java.io.File;

public abstract interface IXREAppData {
	public static final int NS_XRE_ENABLE_PROFILE_MIGRATOR = 2;
	public static final int NS_XRE_ENABLE_EXTENSION_MANAGER = 4;

	public abstract File getDirectory();

	public abstract String getVendor();

	public abstract String getName();

	public abstract String getVersion();

	public abstract String getBuildID();

	public abstract String getID();

	public abstract String getCopyright();

	public abstract int getFlags();
}