/**
 * No restrictions for this source.
 *
 * Author: CA>>>
 * Site: atomation.ru
 * Mail: Sashusik_EntXXI@Mail.ru
 */
package ru.atomation.jbrowser.impl;

import static org.mozilla.browser.XPCOMUtils.getService;
import static org.mozilla.browser.XPCOMUtils.qi;

import org.mozilla.interfaces.nsIRequest;
import org.mozilla.interfaces.nsIStreamListener;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsIURI;
import org.mozilla.interfaces.nsIURIContentListener;
import org.mozilla.interfaces.nsIWebNavigation;
import org.mozilla.interfaces.nsIWebNavigationInfo;
import org.mozilla.xpcom.Mozilla;

import ru.atomation.jbrowser.interfaces.JBrowserComponentEvents;

/**
 *
 * @author caiiiycuk
 */
public class JBrowserURIContentListener implements nsIURIContentListener {

    protected final JBrowserComponent<?> component;
    protected final JBrowserComponentEvents events;
    protected nsIURIContentListener parent;
    private nsISupports cookie;

    /**
     * @param component must implemets {@link  JBrowserComponentEvents}
     * @param parent
     */
    public JBrowserURIContentListener(JBrowserComponent<?> component) {
        this.component = component;

        if (!(component instanceof JBrowserComponentEvents)) {
            throw new IllegalStateException("JBrowserComponent must implements JBrowserComponentEvents interface");
        }

        this.parent = component.getWebBrowser().getParentURIContentListener();
        this.events = (JBrowserComponentEvents) component;
    }

    @Override
    public boolean onStartURIOpen(nsIURI uri) {
        return !events.fireBeforeOpen(uri.getSpec());
    }

    @Override
    public boolean doContent(String arg0, boolean arg1, nsIRequest arg2, nsIStreamListener[] arg3) {
        if (parent != null) {
            return parent.doContent(arg0, arg1, arg2, arg3);
        }

        return false;
    }

    @Override
    public boolean isPreferred(String arg0, String[] arg1) {
        return canHandleContent(arg0, true, arg1);
    }

    @Override
    public boolean canHandleContent(String aContentType,
            boolean aIsContentPreferred,
            String[] aDesiredContentType) {

        boolean handled =
            events.canHandleContent(aContentType,
                aIsContentPreferred,
                aDesiredContentType);

        if (handled) {
            return true;
        }

        aDesiredContentType[0] = null;

        if (aContentType != null) {
            nsIWebNavigationInfo webNavInfo = getService("@mozilla.org/webnavigation-info;1", nsIWebNavigationInfo.class); //$NON-NLS-1$
            if (webNavInfo != null) {
                nsIWebNavigation webNav = qi(component.getWebBrowser(), nsIWebNavigation.class);
                long canHandle = webNavInfo.isTypeSupported(aContentType, webNav);
                return canHandle != nsIWebNavigationInfo.UNSUPPORTED;
            }
        }

        return false;
    }

    @Override
    public nsISupports getLoadCookie() {
        return cookie;
    }

    @Override
    public void setLoadCookie(nsISupports arg0) {
        this.cookie = arg0;
    }

    @Override
    public nsIURIContentListener getParentContentListener() {
        return parent;
    }

    @Override
    public void setParentContentListener(nsIURIContentListener arg0) {
        this.parent = arg0;
    }

    @Override
    public nsISupports queryInterface(String uuid) {
        return Mozilla.queryInterface(this, uuid);
    }
}
