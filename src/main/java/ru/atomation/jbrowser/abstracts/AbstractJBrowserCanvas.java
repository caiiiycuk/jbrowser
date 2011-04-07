package ru.atomation.jbrowser.abstracts;

import java.awt.Canvas;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;

import org.mozilla.interfaces.nsIDOMEvent;
import org.mozilla.interfaces.nsIDOMNode;
import org.mozilla.interfaces.nsIRequest;
import org.mozilla.interfaces.nsIWebProgress;

import ru.atomation.jbrowser.interfaces.Browser;
import ru.atomation.jbrowser.interfaces.BrowserListener;
import ru.atomation.jbrowser.interfaces.JBrowserComponentEvents;

public abstract class AbstractJBrowserCanvas extends Canvas implements Browser,
		JBrowserComponentEvents, FocusListener {

	private static final long serialVersionUID = 2993200030693743785L;
	private final BrowserListener[] EMPTY_ARRAY = new BrowserListener[0];
	private final List<BrowserListener> browserListeners;

	public AbstractJBrowserCanvas() {
		browserListeners = Collections
				.synchronizedList(new ArrayList<BrowserListener>());
		addFocusListener(this);
	}

	@Override
	public void addBrowserListener(BrowserListener listener) {
		synchronized (browserListeners) {
			browserListeners.add(listener);
		}
	}

	@Override
	public void removeBrowserListener(BrowserListener listener) {
		synchronized (browserListeners) {
			browserListeners.remove(listener);
		}
	}

	protected void removeAllListeners() {
		synchronized (browserListeners) {
			browserListeners.clear();
		}
	}

	/**
	 * Request to set title for the browser window. Callback from mozilla
	 * embedding code.
	 * 
	 * <p>
	 * See {@link JFrame#setTitle(String)}
	 * 
	 * @param title
	 *            window title
	 */
	@Override
	public void fireSetTitle(String title) {
		for (BrowserListener l : getBrowserListeners()) {
			l.onSetTitle(title);
		}
	}

	/**
	 * Request to show/hide the browser window Callback from mozilla embedding
	 * code.
	 * 
	 * <p>
	 * See {@link JFrame#setVisible(boolean)}
	 * 
	 * @param visibility
	 *            window visibility
	 */
	@Override
	public void fireSetVisible(boolean visibility) {
		for (BrowserListener l : getBrowserListeners()) {
			l.onSetVisible(visibility);
		}
	}

	/**
	 * Request to resize the browser window. Callback from mozilla embedding
	 * code.
	 * 
	 * <p>
	 * See {@link JFrame#setSize(int, int)}
	 * 
	 * @param w
	 *            requested window width
	 * @param h
	 *            requested window height
	 */
	@Override
	public void fireSetSize(int w, int h) {
		for (BrowserListener l : getBrowserListeners()) {
			l.onSetSize(w, h);
		}
	}

	/**
	 * Request to show text in statusbar. Callback from mozilla embedding code.
	 * 
	 * @param text
	 *            text for statusbar
	 */
	@Override
	public void fireSetStatus(String text) {
		for (BrowserListener l : getBrowserListeners()) {
			l.onSetStatus(text);
		}
	}

	/**
	 * Request to set text in urlbar. Callback from mozilla embedding code.
	 * 
	 * @param url
	 *            text to show in urlbar
	 */
	@Override
	public void fireSetUrlbarText(String url) {
		for (BrowserListener l : getBrowserListeners()) {
			l.onSetUrlbarText(url);
		}
	}

	/**
	 * Request to enable/disable the forward button. Callback from mozilla
	 * embedding code.
	 * 
	 * @param enabled
	 *            true to enable the button, otherwise false
	 */
	@Override
	public void fireEnableForwardButton(boolean enabled) {
		for (BrowserListener l : getBrowserListeners()) {
			l.onEnableForwardButton(enabled);
		}
	}

	/**
	 * Enable/disable the back button. Callback from mozilla embedding code.
	 * 
	 * @param enabled
	 *            true to enable the button, otherwise false
	 */
	@Override
	public void fireEnableBackButton(boolean enabled) {
		for (BrowserListener l : getBrowserListeners()) {
			l.onEnableBackButton(enabled);
		}
	}

	/**
	 * Enable/disable the stop button. Callback from mozilla embedding code.
	 * 
	 * @param enabled
	 *            true to enable the button, otherwise false
	 */
	@Override
	public void fireEnableStopButton(boolean enabled) {
		for (BrowserListener l : getBrowserListeners()) {
			l.onEnableStopButton(enabled);
		}
	}

	/**
	 * Enable/disable the reload button. Callback from mozilla embedding code.
	 * 
	 * @param enabled
	 *            true to enable the button, otherwise false
	 */
	@Override
	public void fireEnableReloadButton(boolean enabled) {
		for (BrowserListener l : getBrowserListeners()) {
			l.onEnableReloadButton(enabled);
		}
	}

	/**
	 * Notification when document loading starts Callback from mozilla embedding
	 * code.
	 */
	@Override
	public void fireLoadingStarted() {
		for (BrowserListener l : getBrowserListeners()) {
			l.onLoadingStarted();
		}
	}

	/**
	 * Notification when document loading end. Callback from mozilla embedding
	 * code.
	 */
	@Override
	public void fireLoadingEnded() {
		for (BrowserListener l : getBrowserListeners()) {
			l.onLoadingEnded();
		}
	}

	/**
	 * Request to close the browser window. Callback from mozilla embedding
	 * code.
	 */
	@Override
	public void fireCloseWindow() {
		for (BrowserListener l : getBrowserListeners()) {
			l.onCloseWindow();
		}
	}

	/**
	 * Notification when browser want open page
	 * 
	 * @param uri
	 *            uri of page to open
	 * @return true if page can be opend; false when open must be aborted
	 */
	@Override
	public boolean fireBeforeOpen(String uri) {
		for (BrowserListener l : getBrowserListeners()) {
			if (!l.beforeOpen(uri)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Invoked when browser gains the keyboard focus.
	 * 
	 * @param e
	 */
	@Override
	public void fireFocusGained(FocusEvent e) {
		for (BrowserListener l : getBrowserListeners()) {
			l.focusGained(e);
		}
	}

	/**
	 * Invoked when browser gains the keyboard focus.
	 * 
	 * @param e
	 */
	@Override
	public void fireFocusLost(FocusEvent e) {
		for (BrowserListener l : getBrowserListeners()) {
			l.focusLost(e);
		}
	}

	@Override
	public void fireProgressChange(nsIWebProgress aWebProgress,
			nsIRequest aRequest, long aCurSelfProgress, long aMaxSelfProgress,
			long aCurTotalProgress, long aMaxTotalProgress) {
		for (BrowserListener l : getBrowserListeners()) {
			l.onProgressChange(aWebProgress, aRequest, aCurSelfProgress,
					aMaxSelfProgress, aCurTotalProgress, aMaxTotalProgress);
		}
	}

	@Override
	public void fireSecurityChange(nsIWebProgress aWebProgress,
			nsIRequest aRequest, long aState) {
		for (BrowserListener l : getBrowserListeners()) {
			l.onSecurityChange(aWebProgress, aRequest, aState);
		}
	}

	@Override
	public boolean canHandleContent(String aContentType,
			boolean aIsContentPreferred, String[] aDesiredContentType) {
		for (BrowserListener l : getBrowserListeners()) {
			if (l.canHandleContent(aContentType, aIsContentPreferred,
					aDesiredContentType)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void fireOnBrowserAttached() {
		for (BrowserListener l : getBrowserListeners()) {
			l.onBrowserAttached();
		}
	}

	@Override
	public void fireOnBrowserDetached() {
		for (BrowserListener l : getBrowserListeners()) {
			l.onBrowserDetached();
		}
	}
	
	/**
	 * Invoked when a component gains the keyboard focus.
	 */
	public void focusGained(FocusEvent e) {
		for (BrowserListener l : getBrowserListeners()) {
			l.focusGained(e);
		}
	}

	/**
	 * Invoked when a component loses the keyboard focus.
	 */
	public void focusLost(FocusEvent e) {
		for (BrowserListener l : getBrowserListeners()) {
			l.focusLost(e);
		}
	}

	protected void fireShowContextMenu(long aContextFlags, nsIDOMEvent aEvent,
			nsIDOMNode aNode) {
		for (BrowserListener l : getBrowserListeners()) {
			l.showContextMenu(aContextFlags, aEvent, aNode);
		}
	}
    
    protected BrowserListener[] getBrowserListeners() {
    	synchronized (browserListeners) {
    		return browserListeners.toArray(EMPTY_ARRAY);
    	}
    }
	
}
