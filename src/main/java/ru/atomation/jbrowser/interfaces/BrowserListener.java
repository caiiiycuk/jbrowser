package ru.atomation.jbrowser.interfaces;

import java.awt.event.FocusEvent;

import javax.swing.JFrame;

import org.mozilla.interfaces.nsIDOMEvent;
import org.mozilla.interfaces.nsIDOMNode;
import org.mozilla.interfaces.nsIRequest;
import org.mozilla.interfaces.nsIWebProgress;

/**
 * BrowserListener interface
 * @author caiiiycuk
 */
public interface BrowserListener {

    /**
     * Request to set title for the browser window.
     * Callback from mozilla embedding code.
     *
     * <p>See {@link JFrame#setTitle(String)}
     *
     * @param title window title
     */
    public void onSetTitle(String title);

    /**
     * Request to show/hide the browser window
     * Callback from mozilla embedding code.
     *
     * <p>See {@link JFrame#setVisible(boolean)}
     *
     * @param visibility window visibility
     */
    public void onSetVisible(boolean visibility);

    /**
     * Request to resize the browser window.
     * Callback from mozilla embedding code.
     *
     * <p>See {@link JFrame#setSize(int, int)}
     *
     * @param w requested window width
     * @param h requested window height
     */
    public void onSetSize(int w, int h);

    /**
     * Request to show text in statusbar.
     * Callback from mozilla embedding code.
     *
     * @param text text for statusbar
     */
    public void onSetStatus(String text);

    /**
     * Request to set text in urlbar.
     * Callback from mozilla embedding code.
     *
     * @param url text to show in urlbar
     */
    public void onSetUrlbarText(String url);

    /**
     * Request to enable/disable the forward button.
     * Callback from mozilla embedding code.
     *
     * @param enabled true to enable the button,
     *   otherwise false
     */
    public void onEnableForwardButton(boolean enabled);

    /**
     * Enable/disable the back button.
     * Callback from mozilla embedding code.
     *
     * @param enabled true to enable the button,
     *   otherwise false
     */
    public void onEnableBackButton(boolean enabled);

    /**
     * Enable/disable the stop button.
     * Callback from mozilla embedding code.
     *
     * @param enabled true to enable the button,
     *   otherwise false
     */
    public void onEnableStopButton(boolean enabled);

    /**
     * Enable/disable the reload button.
     * Callback from mozilla embedding code.
     *
     * @param enabled true to enable the button,
     *   otherwise false
     */
    public void onEnableReloadButton(boolean enabled);

    /**
     * Notification when document loading starts
     * Callback from mozilla embedding code.
     */
    public void onLoadingStarted();

    /**
     * Notification when document loading end.
     * Callback from mozilla embedding code.
     */
    public void onLoadingEnded();

    /**
     * Request to close the browser window.
     * Callback from mozilla embedding code.
     */
    public void onCloseWindow();

//    /**
//     * Notification, when mozilla browser is being added
//     * into the given swing window.
//     *
//     * @param chromeAdapter mozilla browser being added
//     *   into swing window/panel
//     * @param parentChromeAdapter mozilla browser of parent
//     * window, see {@link IMozillaWindow#getParentWindow()}
//     */
//    public void onAttachBrowser();
//   
//    /**
//     * Notification when mozilla browser is being removed from swing window.
//     */
//    public void onDetachBrowser();
    /**
     * Notification when browser want open page
     * @param uri uri of page to open
     * @return true if page can be opend; false when open must be aborted
     */
    public boolean beforeOpen(String uri);

    /**
     * Invoked when browser gains the keyboard focus.
     * @param e
     */
    public void focusGained(FocusEvent e);

    /**
     * Invoked when browser gains the keyboard focus.
     * @param e
     */
    public void focusLost(FocusEvent e);

    /**
     * Notification that the progress has changed for one of the requests associated with aWebProgress. Progress totals are reset to zero when all requests in aWebProgress complete (corresponding to onStateChange being called with aStateFlags including the STATE_STOP and STATE_IS_WINDOW flags).
     * <p>Parameters
     * <li> aWebProgress - The nsIWebProgress instance that fired the notification. </li>
     * <li> aRequest - The nsIRequest that has new progress. </li>
     * <li> aCurSelfProgress - The current progress for aRequest. </li>
     * <li> aMaxSelfProgress - The maximum progress for aRequest. </li>
     * <li> aCurTotalProgress - The current progress for all requests associated with aWebProgress. </li>
     * <li> aMaxTotalProgress - The total progress for all requests associated with aWebProgress. </li>
     * <p>NOTE: If any progress value is unknown, or if its value would exceed the maximum value of type long, then its value is replaced with -1.
     * <p>NOTE: If the object also implements nsIWebProgressListener2 and the caller knows about that interface, this function will not be called. Instead, nsIWebProgressListener2::onProgressChange64 will be called.
     */
    void onProgressChange(nsIWebProgress aWebProgress, nsIRequest aRequest, long aCurSelfProgress, long aMaxSelfProgress, long aCurTotalProgress, long aMaxTotalProgress);

    /**
     * Called when the location of the window being watched changes. This is not when a load is requested, but rather once it is verified that the load is going to occur in the given window. For instance, a load that starts in a window might send progress and status messages for the new site, but it will not send the onLocationChange until we are sure that we are loading this new page here.
     * <p>Parameters
     * <li>aWebProgress - The nsIWebProgress instance that fired the notification.
     * <li>aRequest - The associated nsIRequest. This may be null in some cases.
     * <li>aLocation - The URI of the location that is being loaded.
     */
    void onSecurityChange(nsIWebProgress aWebProgress, nsIRequest aRequest, long aState);

    /**
     * Intercepter for nsIURIContentListener::canHandleContent, default(empty) implemntation
     * must return false;
     */
    boolean canHandleContent(String aContentType, boolean aIsContentPreferred, String[] aDesiredContentType);

    /**
     * Call`s after mozilla embded in component
     */
    void onBrowserAttached();

    /**
     * Call`s after mozilla destroys browser
     */
    void onBrowserDetached();

	/**
	 * Context menu action listener
	 * @param aContextFlags
	 * @param aEvent
	 * @param aNode
	 */
    void showContextMenu(long aContextFlags, nsIDOMEvent aEvent,
			nsIDOMNode aNode);
}
