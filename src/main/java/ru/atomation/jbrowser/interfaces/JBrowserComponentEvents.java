/**
 * No restrictions for this source.
 *
 * Author: CA>>>
 * Site: atomation.ru
 * Mail: Sashusik_EntXXI@Mail.ru
 */
package ru.atomation.jbrowser.interfaces;

import java.awt.event.FocusEvent;

import javax.swing.JFrame;

import org.mozilla.interfaces.nsIRequest;
import org.mozilla.interfaces.nsIWebProgress;

/**
 * @author caiiiycuk
 */
public interface JBrowserComponentEvents {

    /**
     * Request to set title for the browser window.
     * Callback from mozilla embedding code.
     *
     * <p>See {@link JFrame#setTitle(String)}
     *
     * @param title window title
     */
    void fireSetTitle(String title);

    /**
     * Request to show/hide the browser window
     * Callback from mozilla embedding code.
     *
     * <p>See {@link JFrame#setVisible(boolean)}
     *
     * @param visibility window visibility
     */
    void fireSetVisible(boolean visibility);

    /**
     * Request to resize the browser window.
     * Callback from mozilla embedding code.
     *
     * <p>See {@link JFrame#setSize(int, int)}
     *
     * @param w requested window width
     * @param h requested window height
     */
    void fireSetSize(int w, int h);

    /**
     * Request to show text in statusbar.
     * Callback from mozilla embedding code.
     *
     * @param text text for statusbar
     */
    void fireSetStatus(String text);

    /**
     * Request to set text in urlbar.
     * Callback from mozilla embedding code.
     *
     * @param url text to show in urlbar
     */
    void fireSetUrlbarText(String url);

    /**
     * Request to enable/disable the forward button.
     * Callback from mozilla embedding code.
     *
     * @param enabled true to enable the button,
     *   otherwise false
     */
    void fireEnableForwardButton(boolean enabled);

    /**
     * Enable/disable the back button.
     * Callback from mozilla embedding code.
     *
     * @param enabled true to enable the button,
     *   otherwise false
     */
    void fireEnableBackButton(boolean enabled);

    /**
     * Enable/disable the stop button.
     * Callback from mozilla embedding code.
     *
     * @param enabled true to enable the button,
     *   otherwise false
     */
    void fireEnableStopButton(boolean enabled);

    /**
     * Enable/disable the reload button.
     * Callback from mozilla embedding code.
     *
     * @param enabled true to enable the button,
     *   otherwise false
     */
    void fireEnableReloadButton(boolean enabled);

    /**
     * Notification when document loading starts
     * Callback from mozilla embedding code.
     */
    void fireLoadingStarted();

    /**
     * Notification when document loading end.
     * Callback from mozilla embedding code.
     */
    void fireLoadingEnded();

    /**
     * Request to close the browser window.
     * Callback from mozilla embedding code.
     */
    void fireCloseWindow();

    /**
     * Notification when browser want open page
     * @param uri uri of page to open
     * @return true if page can be opend; false when open must be aborted
     */
    boolean fireBeforeOpen(String uri);

    /**
     * Invoked when browser gains the keyboard focus.
     * @param e
     */
    void fireFocusGained(FocusEvent e);

    /**
     * Invoked when browser gains the keyboard focus.
     * @param e
     */
    void fireFocusLost(FocusEvent e);

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
    void fireProgressChange(nsIWebProgress aWebProgress, nsIRequest aRequest, long aCurSelfProgress, long aMaxSelfProgress, long aCurTotalProgress, long aMaxTotalProgress);

    /**
     * Called when the location of the window being watched changes. This is not when a load is requested, but rather once it is verified that the load is going to occur in the given window. For instance, a load that starts in a window might send progress and status messages for the new site, but it will not send the onLocationChange until we are sure that we are loading this new page here.
     * <p>Parameters
     * <li>aWebProgress - The nsIWebProgress instance that fired the notification.
     * <li>aRequest - The associated nsIRequest. This may be null in some cases.
     * <li>aLocation - The URI of the location that is being loaded.
     */
    void fireSecurityChange(nsIWebProgress aWebProgress, nsIRequest aRequest, long aState);

    boolean canHandleContent(String aContentType, boolean aIsContentPreferred, String[] aDesiredContentType);

    void fireOnBrowserAttached();

    void fireOnBrowserDetached();
}
