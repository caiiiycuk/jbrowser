package ru.atomation.jbrowser.interfaces;

import org.mozilla.interfaces.nsIClipboardCommands;
import org.mozilla.interfaces.nsIDocShell;
import org.mozilla.interfaces.nsIEmbeddingSiteWindow;
import org.mozilla.interfaces.nsIInterfaceRequestor;
import org.mozilla.interfaces.nsITooltipListener;
import org.mozilla.interfaces.nsIWebBrowserChrome;
import org.mozilla.interfaces.nsIWebBrowserChromeFocus;

/**
 * Mozilla interfaces of default browser implementation
 * @author caiiiycuk
 *
 */
public interface NativeBrowser extends 
	nsIWebBrowserChrome,
	nsIWebBrowserChromeFocus,
	nsIEmbeddingSiteWindow,
	nsIInterfaceRequestor,
	nsITooltipListener {

    /**
     * Called after browser creation  
     */
    void onBrowserAttached();

    /**
     * Called on destroying browser
     */
    void onBrowserDetached();
    
    /**
     * {@link nsIInterfaceRequestor}
     * @return
     */
    nsIInterfaceRequestor getInterfaceRequestor();

    /**
     * {@link nsIDocShell}
     * @return
     */
	nsIDocShell getDocShell();

    /**
     * {@link nsIClipboardCommands}
     * @return
     */
	nsIClipboardCommands getClipboardCommands();
    
}
