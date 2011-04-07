/**
 * No restrictions for this source.
 *
 * Author: CA>>>
 * Site: atomation.ru
 * Mail: Sashusik_EntXXI@Mail.ru
 */

package ru.atomation.jbrowser.interfaces;

import java.awt.event.FocusEvent;

import org.mozilla.interfaces.nsIDOMEvent;
import org.mozilla.interfaces.nsIDOMNode;
import org.mozilla.interfaces.nsIRequest;
import org.mozilla.interfaces.nsIWebProgress;

/**
 * Adapter for {@link BrowserListener}
 * @author caiiiycuk
 */
public class BrowserAdapter implements BrowserListener {

    @Override
    public void onSetTitle(String title) {
    }

    @Override
    public void onSetVisible(boolean visibility) {
    }

    @Override
    public void onSetSize(int w, int h) {
    }

    @Override
    public void onSetStatus(String text) {
    }

    @Override
    public void onSetUrlbarText(String url) {
    }

    @Override
    public void onEnableForwardButton(boolean enabled) {
    }

    @Override
    public void onEnableBackButton(boolean enabled) {
    }

    @Override
    public void onEnableStopButton(boolean enabled) {
    }

    @Override
    public void onEnableReloadButton(boolean enabled) {
    }

    @Override
    public void onLoadingStarted() {
    }

    @Override
    public void onLoadingEnded() {
    }

    @Override
    public void onCloseWindow() {
    }

    @Override
    public boolean beforeOpen(String uri) {
        return true;
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
    }


    @Override
    public void onSecurityChange(nsIWebProgress aWebProgress, nsIRequest aRequest, long aState) {
    }

    @Override
    public boolean canHandleContent(String aContentType, boolean aIsContentPreferred, String[] aDesiredContentType) {
        return false;
    }

    @Override
    public void onBrowserAttached() {
    }

    @Override
    public void onBrowserDetached() {
    }

	@Override
	public void onProgressChange(nsIWebProgress aWebProgress,
			nsIRequest aRequest, long aCurSelfProgress, long aMaxSelfProgress,
			long aCurTotalProgress, long aMaxTotalProgress) {
		
	}

	@Override
	public void showContextMenu(long aContextFlags, nsIDOMEvent aEvent,
			nsIDOMNode aNode) {
	}

}
