package org.mozilla.xpcom;

import java.io.File;
import org.mozilla.interfaces.nsIComponentManager;
import org.mozilla.interfaces.nsIComponentRegistrar;
import org.mozilla.interfaces.nsILocalFile;
import org.mozilla.interfaces.nsIServiceManager;

public abstract interface IXPCOM {
	public abstract nsIServiceManager initXPCOM(File paramFile,
			IAppFileLocProvider paramIAppFileLocProvider) throws XPCOMException;

	public abstract void shutdownXPCOM(nsIServiceManager paramnsIServiceManager)
			throws XPCOMException;

	public abstract nsIServiceManager getServiceManager() throws XPCOMException;

	public abstract nsIComponentManager getComponentManager()
			throws XPCOMException;

	public abstract nsIComponentRegistrar getComponentRegistrar()
			throws XPCOMException;

	public abstract nsILocalFile newLocalFile(String paramString,
			boolean paramBoolean) throws XPCOMException;
}