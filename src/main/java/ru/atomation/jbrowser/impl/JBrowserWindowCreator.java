package ru.atomation.jbrowser.impl;

import static org.mozilla.browser.MozillaExecutor.isMozillaThread;
import static org.mozilla.browser.XPCOMUtils.create;
import static org.mozilla.browser.XPCOMUtils.qi;
import static org.mozilla.interfaces.nsIWebBrowserChrome.CHROME_ALL;
import static org.mozilla.interfaces.nsIWebBrowserChrome.CHROME_DEFAULT;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mozilla.browser.impl.DOMAdapter;
import org.mozilla.interfaces.nsIBaseWindow;
import org.mozilla.interfaces.nsIDOMEventTarget;
import org.mozilla.interfaces.nsIDOMWindow2;
import org.mozilla.interfaces.nsIDocShellTreeItem;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsIWebBrowser;
import org.mozilla.interfaces.nsIWebBrowserChrome;
import org.mozilla.xpcom.Mozilla;

import ru.atomation.jbrowser.abstracts.AbstractBrowserWindowCreator;
import ru.atomation.jbrowser.interfaces.NativeBrowser;

/**
 * based on embedding/browser/gtk/EmbedWindowCreator.cpp, 
 * based on mozswing WindowCreator
 */
public class JBrowserWindowCreator extends AbstractBrowserWindowCreator {

    protected Map<JBrowserComponent<?>, DOMAdapter> domAdapters;
    protected Set<NativeBrowser> attachedBrowsers;
    
    
    JBrowserWindowCreator() {
        domAdapters = new HashMap<JBrowserComponent<?>, DOMAdapter>();
        attachedBrowsers = Collections.synchronizedSet(new HashSet<NativeBrowser>());
    }

    @Override
    public nsISupports queryInterface(String aIID) {
        return Mozilla.queryInterface(this, aIID);
    }

    /**
     * Callback when mozilla wants to open a new window
     */
    @Override
    public nsIWebBrowserChrome createWindow(nsIWebBrowserChrome parent,
            long flags) {
        if (getBrowserFactory() == null) {
            return parent;
        }

        //we disable attaching on creation for fixing deadlocks
        JBrowserComponent<?> browser;
        if (parent instanceof JBrowserComponent<?>) {
            browser = getBrowserFactory().createDisplayableBrowser((JBrowserComponent<?>) parent, false, flags);
        } else {
            browser = getBrowserFactory().createDisplayableBrowser(null, false, flags);
        }

        //wait for peer creation
        while (!browser.getComponent().isDisplayable()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //ignoring
                }
        }

        //and now attaching browser
        browser.getBrowserManager().getWindowCreator().attachBrowser(browser, flags);
        return (NativeBrowser) browser;
    }

    /**
     * Creates a mozilla browser and adds it into a Swing window.
     * The Swing window must be already created and realized!!
     * (i.e. after addNotify())
     */
    @Override
    public void attachBrowser(JBrowserComponent<?> browserComponent, long chromeFlags) {
        assert isMozillaThread();

        if (!browserComponent.getComponent().isDisplayable()) {
        	return;
        }
        
        if (!(browserComponent instanceof NativeBrowser)) {
            throw new IllegalStateException("Browsr component must implements NativeBrowser inteface");
        }

        NativeBrowser browser = (NativeBrowser) browserComponent;
        
        synchronized (attachedBrowsers) {
        	if (attachedBrowsers.contains(browser)) {
        		return;
        	}
        	
        	attachedBrowsers.add(browser);
		}
        
        if (chromeFlags == CHROME_DEFAULT) {
            chromeFlags = CHROME_ALL;
        }

        //create new webbrowser component
        nsIWebBrowser webBrowser = create("@mozilla.org/embedding/browser/nsWebBrowser;1", nsIWebBrowser.class); //$NON-NLS-1$

        //create and set our embedding implementation
        browser.setWebBrowser(webBrowser);
        browser.setChromeFlags(chromeFlags);
        webBrowser.setContainerWindow(browser);

        //set flag whether displaying html content or chrome content
        boolean isChromeContent =
                (browser.getChromeFlags() & nsIWebBrowserChrome.CHROME_OPENAS_CHROME) != 0;

        nsIDocShellTreeItem item = qi(webBrowser, nsIDocShellTreeItem.class);
        item.setItemType(
                isChromeContent ? nsIDocShellTreeItem.typeChromeWrapper : nsIDocShellTreeItem.typeContentWrapper);

        //get dimension of the mozilla area. Usually, the widget does not
        //have the correct bounds at this stage yet, so use defaults
        Rectangle dim = browserComponent.getComponent().getBounds();
        nsIBaseWindow baseWindow = qi(webBrowser, nsIBaseWindow.class);
        //create the native mozilla area
        baseWindow.initWindow(browserComponent.getHandle(), 0, 0, 0, dim.width, dim.height);
        baseWindow.create();

        // bind the dom listener
        nsIDOMWindow2 domWin = qi(webBrowser.getContentDOMWindow(), nsIDOMWindow2.class);
        nsIDOMEventTarget et = domWin.getWindowRoot();

        DOMAdapter domAdapter = new DOMAdapter(browserComponent);
        for (String ev : DOMAdapter.hookedEvents) {
            et.addEventListener(ev, domAdapter, false);
        }
        //-put dom-adapter to map
        domAdapters.put(browserComponent, domAdapter);

        browser.onBrowserAttached();
    }

    /**
     * Remove mozilla browser from Swing window, and then
     * destroys the browser. The Swing window will continue
     * to exist.
     */
    @Override
    public void detachBrowser(JBrowserComponent<?> browserComponent) {
        assert isMozillaThread();

        if (!(browserComponent instanceof NativeBrowser)) {
            throw new IllegalStateException("Browsr component must implements NativeBrowser inteface");
        }

        NativeBrowser browser = (NativeBrowser) browserComponent;

        synchronized (attachedBrowsers) {
        	if (!attachedBrowsers.contains(browser)) {
        		return;
        	}
        	
        	attachedBrowsers.remove(browser);
		}

        nsIWebBrowser webBrowser = browser.getWebBrowser();

        DOMAdapter domAdapter = domAdapters.get(browserComponent);

        if (domAdapter != null) {
            nsIDOMWindow2 domWin = qi(webBrowser.getContentDOMWindow(), nsIDOMWindow2.class);
            nsIDOMEventTarget et = domWin.getWindowRoot();
            if (et != null) {
	            for (String ev : DOMAdapter.hookedEvents) {
	                et.removeEventListener(ev, domAdapter, false);
	            }
            }
        }

        webBrowser.setParentURIContentListener(null);

        browserComponent.onBrowserDetached();
        nsIBaseWindow baseWindow = qi(webBrowser, nsIBaseWindow.class);
        baseWindow.destroy();

        webBrowser.setContainerWindow(null);
    }
}
