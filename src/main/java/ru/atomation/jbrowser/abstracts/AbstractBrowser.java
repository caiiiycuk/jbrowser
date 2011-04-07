package ru.atomation.jbrowser.abstracts;

import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;

import ru.atomation.jbrowser.interfaces.Browser;
import ru.atomation.jbrowser.interfaces.BrowserListener;

public abstract class AbstractBrowser implements Browser {

	private final List<BrowserListener> browserListeners;
	
	public AbstractBrowser() {
		browserListeners = Collections.synchronizedList(new ArrayList<BrowserListener>());
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
	
    /**
     * Request to set title for the browser window.
     * Callback from mozilla embedding code.
     *
     * <p>See {@link JFrame#setTitle(String)}
     *
     * @param title window title
     */
    protected void fireSetTitle(String title) {
    	synchronized (browserListeners) {
			for (BrowserListener l: browserListeners) {
				l.onSetTitle(title);
			}
		}
    }
    
    /**
     * Request to show/hide the browser window
     * Callback from mozilla embedding code.
     *
     * <p>See {@link JFrame#setVisible(boolean)}
     *
     * @param visibility window visibility
     */
    protected void fireSetVisible(boolean visibility) {
    	synchronized (browserListeners) {
			for (BrowserListener l: browserListeners) {
				l.onSetVisible(visibility);
			}
		}
    }
    
    /**
     * Request to resize the browser window.
     * Callback from mozilla embedding code.
     *
     * <p>See {@link JFrame#setSize(int, int)}
     *
     * @param w requested window width
     * @param h requested window height
     */
    protected void fireSetSize(int w, int h) {
    	synchronized (browserListeners) {
			for (BrowserListener l: browserListeners) {
				l.onSetSize(w, h);
			}
		}
    }
    
    /**
     * Request to show text in statusbar.
     * Callback from mozilla embedding code.
     *
     * @param text text for statusbar
     */
    protected void fireSetStatus(String text) {
    	synchronized (browserListeners) {
			for (BrowserListener l: browserListeners) {
				l.onSetStatus(text);
			}
		}
    }
    
    /**
     * Request to set text in urlbar.
     * Callback from mozilla embedding code.
     *
     * @param url text to show in urlbar
     */
    protected void fireSetUrlbarText(String url) {
    	synchronized (browserListeners) {
			for (BrowserListener l: browserListeners) {
				l.onSetUrlbarText(url);
			}
		}
    }
    
    /**
     * Request to enable/disable the forward button.
     * Callback from mozilla embedding code.
     *
     * @param enabled true to enable the button,
     *   otherwise false
     */
    protected void fireEnableForwardButton(boolean enabled) {
    	synchronized (browserListeners) {
			for (BrowserListener l: browserListeners) {
				l.onEnableForwardButton(enabled);
			}
		}
    }
    
    /**
     * Enable/disable the back button.
     * Callback from mozilla embedding code.
     *
     * @param enabled true to enable the button,
     *   otherwise false
     */
    
    protected void fireEnableBackButton(boolean enabled) {
    	synchronized (browserListeners) {
			for (BrowserListener l: browserListeners) {
				l.onEnableBackButton(enabled);
			}
		}
    }
    /**
     * Enable/disable the stop button.
     * Callback from mozilla embedding code.
     *
     * @param enabled true to enable the button,
     *   otherwise false
     */
    
    protected void fireEnableStopButton(boolean enabled) {
    	synchronized (browserListeners) {
			for (BrowserListener l: browserListeners) {
				l.onEnableStopButton(enabled);
			}
		}
    }
  
    /**
     * Enable/disable the reload button.
     * Callback from mozilla embedding code.
     *
     * @param enabled true to enable the button,
     *   otherwise false
     */
    protected void fireEnableReloadButton(boolean enabled) {
    	synchronized (browserListeners) {
			for (BrowserListener l: browserListeners) {
				l.onEnableReloadButton(enabled);
			}
		}
    }

    /**
     * Notification when document loading starts
     * Callback from mozilla embedding code.
     */
    protected void fireLoadingStarted() {
    	synchronized (browserListeners) {
			for (BrowserListener l: browserListeners) {
				l.onLoadingStarted();
			}
		}
    }
    
    /**
     * Notification when document loading end.
     * Callback from mozilla embedding code.
     */
    protected void fireLoadingEnded() {
    	synchronized (browserListeners) {
			for (BrowserListener l: browserListeners) {
				l.onLoadingEnded();
			}
		}
    }
    
    /**
     * Request to close the browser window.
     * Callback from mozilla embedding code.
     */
    protected void fireCloseWindow() {
    	synchronized (browserListeners) {
			for (BrowserListener l: browserListeners) {
				l.onCloseWindow();
			}
		}
    }

	/**
	 * Notification when browser want open page
	 * @param uri uri of page to open
	 * @return true if page can be opend; false when open must be aborted
	 */
    protected boolean fireBeforeOpen(String uri) {
    	synchronized (browserListeners) {
			for (BrowserListener l: browserListeners) {
				if (!l.beforeOpen(uri)) {
					return false;
				}
			}
		}
    	
    	return true;
    }
    
    /**
     * Invoked when browser gains the keyboard focus.
     * @param e
     */
    protected void fireFocusGained(FocusEvent e) {
    	synchronized (browserListeners) {
			for (BrowserListener l: browserListeners) {
				l.focusGained(e);
			}
		}
    }
	
	/**
	 * Invoked when browser gains the keyboard focus.
	 * @param e
	 */
    protected void fireFocusLost(FocusEvent e) {
    	synchronized (browserListeners) {
			for (BrowserListener l: browserListeners) {
				l.focusLost(e);
			}
		}
    }

}
