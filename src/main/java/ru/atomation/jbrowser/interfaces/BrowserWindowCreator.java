package ru.atomation.jbrowser.interfaces;

import org.mozilla.interfaces.nsIWebBrowserChrome;
import org.mozilla.interfaces.nsIWindowCreator;

import ru.atomation.jbrowser.impl.JBrowserComponent;

/**
 * Interface to process callback`s when mozilla needs to open window
 * @author caiiiycuk
 *
 */
public interface BrowserWindowCreator extends nsIWindowCreator {

    /**
     * Add window open interceptor
     * @param factory
     */
    void addInterceptor(ComponentFacotry<?> factory);

    /**
     * remove window open interceptor
     * @param factory
     */
    void removeInterceptor(ComponentFacotry<?> factory);

    nsIWebBrowserChrome createWindow(nsIWebBrowserChrome parent, long flags);

    /**
     * Creates a mozilla browser and adds it into a Swing window.
     * The Swing window must be already created and realized!!
     * (i.e. after addNotify())
     */
    void attachBrowser(JBrowserComponent<?> browser, long chromeFlags);

    /**
     * Remove mozilla browser from Swing window, and then
     * destroys the browser. The Swing window will continue
     * to exist.
     */
    void detachBrowser(JBrowserComponent<?> browser);

    /**
     * @param factory set factory to produce new browser, rememver that factory
     * must produce dispalable windows, else you will have deadlocks
     */
    void setBrowserFactory(ComponentFacotry<?> factory);

    /**
     * @return get factory to produce new browser, rememver that factory
     * must produce dispalable windows, else you will have deadlocks
     */
	ComponentFacotry<?> getBrowserFactory();

}
