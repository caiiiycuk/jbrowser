/**
 * No restrictions for this source.
 *
 * Author: CA>>>
 * Site: atomation.ru
 * Mail: Sashusik_EntXXI@Mail.ru
 */
package ru.atomation.jbrowser.impl;

import static org.mozilla.browser.XPCOMUtils.qi;

import javax.swing.SwingUtilities;

import org.mozilla.browser.MozillaExecutor;
import org.mozilla.interfaces.nsIDOMWindow;
import org.mozilla.interfaces.nsIDOMWindowInternal;
import org.mozilla.interfaces.nsIRequest;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsIURI;
import org.mozilla.interfaces.nsIWebNavigation;
import org.mozilla.interfaces.nsIWebProgress;
import org.mozilla.interfaces.nsIWebProgressListener;
import org.mozilla.interfaces.nsIWebProgressListener2;
import org.mozilla.xpcom.Mozilla;

import ru.atomation.jbrowser.interfaces.JBrowserComponentEvents;

/**
 * @see https://developer.mozilla.org/en/nsIWebProgressListener
 * @author caiiiycuk
 */
public class JBrowserProgressListener implements nsIWebProgressListener, nsIWebProgressListener2 {

    protected JBrowserComponent<?> browserComponent;
    protected JBrowserComponentEvents events;

    public JBrowserProgressListener(JBrowserComponent<?> browserComponent) {
        this.browserComponent = browserComponent;

        if (!(browserComponent instanceof JBrowserComponentEvents)) {
            throw new IllegalStateException("JBrowserComponent must implements JBrowserComponentEvents interface");
        }

        this.events = (JBrowserComponentEvents) browserComponent;
    }



    @Override
    public void onStateChange(nsIWebProgress aWebProgress, nsIRequest aRequest, long aStateFlags, long aStatus) {
        assert MozillaExecutor.isMozillaThread();
//        if (mozCanvas.webBrowser==null) return;

        // if we've got the start flag, emit the signal
        if ((aStateFlags & nsIWebProgressListener.STATE_IS_NETWORK) != 0 &&
                (aStateFlags & nsIWebProgressListener.STATE_START) != 0) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    events.fireEnableStopButton(true);
                    events.fireLoadingStarted();
                }
            });
        }

        if ((aStateFlags & nsIWebProgressListener.STATE_IS_NETWORK) != 0 &&
                (aStateFlags & nsIWebProgressListener.STATE_STOP) != 0) {

            nsIWebNavigation nav = qi(browserComponent.getWebBrowser(), nsIWebNavigation.class);
            final boolean isFwd = nav.getCanGoForward();
            final boolean isBack = nav.getCanGoBack();

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    events.fireEnableForwardButton(isFwd);
                    events.fireEnableBackButton(isBack);
                    events.fireEnableStopButton(false);
                    events.fireEnableReloadButton(true);
                    events.fireLoadingEnded();
                }
            });

            contentFinishedLoading();
        }
    }

    @Override
    public void onLocationChange(nsIWebProgress aWebProgress, nsIRequest aRequest, nsIURI aLocation) {
        assert MozillaExecutor.isMozillaThread();

        final String uri = aLocation.getSpec();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                events.fireSetUrlbarText(uri);
            }
        });
    }

    @Override
    public void onStatusChange(nsIWebProgress aWebProgress, nsIRequest aRequest, long aStatus, final String aMessage) {
        assert MozillaExecutor.isMozillaThread();


        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                events.fireSetStatus(aMessage);
            }
        });
    }

    @Override
    public void onProgressChange(nsIWebProgress aWebProgress, nsIRequest aRequest, int aCurSelfProgress, int aMaxSelfProgress, int aCurTotalProgress, int aMaxTotalProgress) {
        events.fireProgressChange(aWebProgress, aRequest, aCurSelfProgress, aMaxSelfProgress, aCurTotalProgress, aMaxTotalProgress);
    }
    
	@Override
	public void onProgressChange64(nsIWebProgress aWebProgress, nsIRequest aRequest, long aCurSelfProgress, long aMaxSelfProgress, long aCurTotalProgress, long aMaxTotalProgress) {
		events.fireProgressChange(aWebProgress, aRequest, aCurSelfProgress, aMaxSelfProgress, aCurTotalProgress, aMaxTotalProgress);
	}



	@Override
	public boolean onRefreshAttempted(nsIWebProgress arg0, nsIURI arg1,
			int arg2, boolean arg3) {
		return false;
	}

    @Override
    public void onSecurityChange(nsIWebProgress aWebProgress, nsIRequest aRequest, long aState) {
        events.fireSecurityChange(aWebProgress, aRequest, aState);
    }

    @Override
    public nsISupports queryInterface(String uuid) {
        return Mozilla.queryInterface(this, uuid);
    }

    protected void contentFinishedLoading() {
        if (browserComponent.isBrowserExisist()) {
            // get the content DOM window for that web browser
            nsIDOMWindow domWindow = browserComponent.getWebBrowser().getContentDOMWindow();
            if (domWindow == null) {
                //NS_WARNING("no dom window in content finished loading\n");
                return;
            }

            // resize the content
            domWindow.sizeToContent();

            //if the JFrame size if forced in Example15, the content
            //is not resized to cover the whole window. Force to resize
            //it with the +1, -1 trick
            nsIDOMWindowInternal wini = qi(domWindow, nsIDOMWindowInternal.class);
            wini.resizeBy(1, 0);
            wini.resizeBy(-1, 0);

            // and since we're done loading show the window, assuming that the
            // visibility flag has been set.
            boolean visibility = browserComponent.getVisibility();
            if (visibility) {
                browserComponent.setVisibility(true);
            }
        }
    }

}
