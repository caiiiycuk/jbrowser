/**
 * No restrictions for this source.
 *
 * Author: CA>>>
 * Site: atomation.ru
 * Mail: Sashusik_EntXXI@Mail.ru
 */
package ru.atomation.jbrowser.impl;

import java.awt.AWTEvent;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.mozilla.interfaces.nsIClipboardCommands;
import org.mozilla.interfaces.nsIDocShell;
import org.mozilla.interfaces.nsIInterfaceRequestor;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsIWebBrowser;
import org.w3c.dom.Document;

import ru.atomation.jbrowser.interfaces.BrowserAdapter;
import ru.atomation.jbrowser.interfaces.BrowserListener;
import ru.atomation.jbrowser.interfaces.BrowserManager;
import ru.atomation.jbrowser.interfaces.ScrollControl;

/**
 * Browser embeded in JFrame / Браузер встроенный в окно
 * @author caiiiycuk
 */
public class JBrowserFrame extends JFrame implements JBrowserComponent<JFrame> {

	private static final long serialVersionUID = -8107666478456286031L;
	protected JBrowserCanvas browserCanvas;
    protected BrowserManager browserManager;
    protected boolean autoResize;

    public JBrowserFrame(BrowserManager browserManager) {
        super();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        this.autoResize = true;
        
        this.browserManager = browserManager;
        this.browserCanvas = new JBrowserCanvas(browserManager);
        this.browserCanvas.addBrowserListener(new BrowserAdapter() {

            @Override
            public void onCloseWindow() {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        dispose();
                    }
                });
            }

            @Override
            public void onSetSize(final int w, final int h) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                    	if (autoResize) {
                    		browserCanvas.setPreferredSize(new Dimension(w, h));
                    		setSize(getPreferredSize());
                    	}
                    }
                });
            }

            @Override
            public void onSetTitle(final String title) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        setTitle(title);
                    }
                });
            }
        });
        this.getContentPane().add(browserCanvas);
    }

    @Override
    public void onCreatePeer(Runnable action) {
        browserCanvas.onCreatePeer(action);
    }

    @Override
    public void onDestroyPeer(Runnable action) {
        browserCanvas.onDestroyPeer(action);
    }

    @Override
    public JFrame getComponent() {
        return this;
    }

    @Override
    public long getHandle() {
        return browserCanvas.getHandle();
    }

    @Override
    public BrowserManager getBrowserManager() {
        return browserManager;
    }

    @Override
    public boolean isBrowserExisist() {
        return browserCanvas.isBrowserExisist();
    }

    @Override
    public boolean back() {
        return browserCanvas.back();
    }

    @Override
    public boolean forward() {
        return browserCanvas.forward();
    }

    @Override
    public boolean stop() {
        return browserCanvas.stop();
    }

    @Override
    public boolean refresh() {
        return browserCanvas.refresh();
    }

    @Override
    public boolean setText(String content) {
        return browserCanvas.setText(content);
    }

    @Override
    public boolean setUrl(String url) {
        return browserCanvas.setUrl(url);
    }

    @Override
    public String getUrl() {
        return browserCanvas.getUrl();
    }

    @Override
    public String getFavIcon() {
        return browserCanvas.getFavIcon();
    }

    @Override
    public boolean disposeBrowser() {
        return browserCanvas.disposeBrowser();
    }

    @Override
    public void addBrowserListener(BrowserListener listener) {
        browserCanvas.addBrowserListener(listener);
    }

    @Override
    public void removeBrowserListener(BrowserListener listener) {
        browserCanvas.removeBrowserListener(listener);
    }

    @Override
    public void onBrowserAttached() {
        browserCanvas.onBrowserAttached();
    }

    @Override
    public void onBrowserDetached() {
        browserCanvas.onBrowserDetached();
    }

    @Override
    public void setStatus(long arg0, String arg1) {
        browserCanvas.setStatus(arg0, arg1);
    }

    @Override
    public nsIWebBrowser getWebBrowser() {
        return browserCanvas.getWebBrowser();
    }

    @Override
    public void setWebBrowser(nsIWebBrowser arg0) {
        browserCanvas.setWebBrowser(arg0);
    }

    @Override
    public long getChromeFlags() {
        return browserCanvas.getChromeFlags();
    }

    @Override
    public void setChromeFlags(long arg0) {
        browserCanvas.setChromeFlags(arg0);
    }

    @Override
    public void destroyBrowserWindow() {
        browserCanvas.destroyBrowserWindow();
    }

    @Override
    public void sizeBrowserTo(int arg0, int arg1) {
        browserCanvas.sizeBrowserTo(arg0, arg1);
    }

    @Override
    public void showAsModal() {
        browserCanvas.showAsModal();
    }

    @Override
    public boolean isWindowModal() {
        return browserCanvas.isWindowModal();
    }

    @Override
    public void exitModalEventLoop(long arg0) {
        browserCanvas.exitModalEventLoop(arg0);
    }

    @Override
    public nsISupports queryInterface(String arg0) {
        return browserCanvas.queryInterface(arg0);
    }

    @Override
    public void focusNextElement() {
        browserCanvas.focusNextElement();
    }

    @Override
    public void focusPrevElement() {
        browserCanvas.focusPrevElement();
    }

    @Override
    public void setDimensions(long arg0, int arg1, int arg2, int arg3, int arg4) {
        browserCanvas.setDimensions(arg0, arg4, arg4, arg4, arg4);
    }

    @Override
    public void getDimensions(long arg0, int[] arg1, int[] arg2, int[] arg3, int[] arg4) {
        browserCanvas.getDimensions(arg0, arg4, arg4, arg4, arg4);
    }

    @Override
    public void setFocus() {
        browserCanvas.setFocus();
    }

    @Override
    public boolean getVisibility() {
        return browserCanvas.getVisibility();
    }

    @Override
    public void setVisibility(boolean arg0) {
        browserCanvas.setVisibility(arg0);
    }

    @Override
    public long getSiteWindow() {
        return browserCanvas.getSiteWindow();
    }

    @Override
    public nsISupports getInterface(String arg0) {
        return browserCanvas.getInterface(arg0);
    }

    @Override
    public void onShowTooltip(int arg0, int arg1, String arg2) {
        browserCanvas.onShowTooltip(arg1, arg1, arg2);
    }

    @Override
    public void onHideTooltip() {
        browserCanvas.onHideTooltip();
    }

    @Override
    public void processEvent(AWTEvent arg0) {
        super.processEvent(arg0);
    }

    @Override
    public Document getDocument() {
        return browserCanvas.getDocument();
    }

    public JBrowserCanvas getBrowserCanvas() {
        return browserCanvas;
    }
    
    /**
     * Allow mozilla to modify frame size
     * @param autoResize
     */
    public void setAutoResize(boolean autoResize) {
		this.autoResize = autoResize;
	}
    
    /**
     * Is mozilla can modify frame size
     * @return
     */
    public boolean isAutoResize() {
		return autoResize;
	}

	@Override
	public nsIClipboardCommands getClipboardCommands() {
		return browserCanvas.getClipboardCommands();
	}

	@Override
	public nsIDocShell getDocShell() {
		return browserCanvas.getDocShell();
	}

	@Override
	public nsIInterfaceRequestor getInterfaceRequestor() {
		return browserCanvas.getInterfaceRequestor();
	}

	@Override
	public byte[] asImage() {
		return browserCanvas.asImage();
	}

	@Override
	public void sizeToContent() {
		browserCanvas.sizeToContent();
	}
	
	@Override
	public ScrollControl getScrollControl() {
		return browserCanvas.getScrollControl();
	}

}
