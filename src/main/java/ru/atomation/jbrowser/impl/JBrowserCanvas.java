package ru.atomation.jbrowser.impl;

import static org.mozilla.browser.MozillaExecutor.isMozillaThread;
import static org.mozilla.browser.MozillaExecutor.mozAsyncExec;
import static org.mozilla.browser.MozillaExecutor.mozSyncExec;
import static org.mozilla.browser.MozillaExecutor.mozSyncExecQuiet;
import static org.mozilla.browser.MozillaExecutor.swingAsyncExec;
import static org.mozilla.browser.XPCOMUtils.qi;
import static org.mozilla.browser.impl.jna.Gtk.gtk;
import static org.mozilla.xpcom.IXPCOMError.NS_ERROR_INVALID_ARG;
import static org.mozilla.xpcom.IXPCOMError.NS_OK;

import java.awt.AWTEvent;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.concurrent.Callable;

import javax.swing.SwingUtilities;

import net.sourceforge.iharder.Base64;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.browser.MozillaAutomation;
import org.mozilla.browser.MozillaExecutor;
import org.mozilla.browser.XPCOMUtils;
import org.mozilla.browser.common.Platform;
import org.mozilla.browser.impl.EventBuffer;
import org.mozilla.browser.impl.FocusWatcher;
import org.mozilla.browser.impl.components.JFakeTooltip;
import org.mozilla.browser.impl.jna.Gdk.GdkDisplay;
import org.mozilla.browser.impl.jna.Gtk;
import org.mozilla.browser.impl.jna.Gtk.GtkWindow;
import org.mozilla.browser.impl.jna.X11;
import org.mozilla.dom.NodeFactory;
import org.mozilla.interfaces.nsIBaseWindow;
import org.mozilla.interfaces.nsIClipboardCommands;
import org.mozilla.interfaces.nsIContextMenuListener;
import org.mozilla.interfaces.nsIDOMCanvasRenderingContext2D;
import org.mozilla.interfaces.nsIDOMDocument;
import org.mozilla.interfaces.nsIDOMElement;
import org.mozilla.interfaces.nsIDOMEvent;
import org.mozilla.interfaces.nsIDOMEventListener;
import org.mozilla.interfaces.nsIDOMEventTarget;
import org.mozilla.interfaces.nsIDOMHTMLAnchorElement;
import org.mozilla.interfaces.nsIDOMHTMLCanvasElement;
import org.mozilla.interfaces.nsIDOMNSHTMLElement;
import org.mozilla.interfaces.nsIDOMNode;
import org.mozilla.interfaces.nsIDOMWindow;
import org.mozilla.interfaces.nsIDOMWindow2;
import org.mozilla.interfaces.nsIDOMXULElement;
import org.mozilla.interfaces.nsIDocShell;
import org.mozilla.interfaces.nsIInterfaceRequestor;
import org.mozilla.interfaces.nsIJSContextStack;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsIURI;
import org.mozilla.interfaces.nsIWebBrowser;
import org.mozilla.interfaces.nsIWebBrowserFocus;
import org.mozilla.interfaces.nsIWebNavigation;
import org.mozilla.interfaces.nsIWebProgressListener;
import org.mozilla.xpcom.Mozilla;
import org.mozilla.xpcom.XPCOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.atomation.jbrowser.abstracts.AbstractJBrowserCanvas;
import ru.atomation.jbrowser.interfaces.BrowserManager;
import ru.atomation.jbrowser.interfaces.ScrollControl;

public class JBrowserCanvas extends AbstractJBrowserCanvas implements JBrowserComponent<Canvas>, nsIContextMenuListener {

    private static final long serialVersionUID = 2946773219993586110L;
    protected static Log log = LogFactory.getLog(JBrowserCanvas.class);
    
    protected boolean inModalLoop = false;
    protected long chromeFlags;
    protected boolean browserExsist;
    protected JFakeTooltip tooltip;
    protected BrowserManager browserManager;
    private Long mozHandle = null;
    private GtkWindow gtkPtr = null;
    private CanvasListener canvasListener;
    private WindowListener windowListener;
    private EventBuffer eventBuffer;
    private final MozMouseListener ml = new MozMouseListener();
    private Component lastFocusedCmp = null;
    private Runnable onCreatePeer;
    private Runnable onDestroyPeer;
    private boolean visibility;
    private String urlToOpen;
    private JBrowserProgressListener progressListener;

    protected nsIWebBrowser webBrowser;
	protected nsIInterfaceRequestor interfaceRequestor;
	protected nsIDocShell docShell;
	protected nsIClipboardCommands clipboardCommands;
	
	protected final ScrollControl scrollControl;
    
    public JBrowserCanvas(BrowserManager browserManager) {
        super();

        this.browserManager = browserManager;
        this.eventBuffer = new EventBuffer();
        this.browserExsist = false;
        this.scrollControl = new ScrollControlImpl(this);
    }

    private long createHandle() {
        assert isMozillaThread();
        Mozilla moz = Mozilla.getInstance();
        if (Platform.usingGTK2Toolkit()) {
            Toolkit.getDefaultToolkit().sync();
            // Mozilla assumes a top-level GTK window exists.
            //
            // So, we create one with GtkPlug widget. This
            // widget is hooked as a child of the AWT canvas
            // using the XEMBED protocol.
            //
            // The implementation of XEMBED/Server mode
            // for the AWT canvas is not fully implemented (in java6),
            // so we have to take care of handling resize/show/hide
            // events
            X11.Window awtID = new X11.Window((int) moz.getNativeHandleFromAWT(this));
            assert !awtID.isNull();
            if (Platform.platform == Platform.Solaris) {
                // XEmbed implementation on solaris seems to be broken
                gtkPtr = gtk.gtk_window_new(Gtk.GTK_WINDOW_POPUP);
                gtk.gtk_window_set_default_size(gtkPtr, 300, 300);
                gtk.gtk_window_set_title(gtkPtr, "Mozilla Wrapper Window"); //$NON-NLS-1$
            } else {
                // on Linux use XEmbed, it handles focus
                // and keyevent propagation
                gtkPtr = gtk.gtk_plug_new(awtID);
            }
            assert gtkPtr != null;
            Toolkit.getDefaultToolkit().sync();
            gtk.gtk_widget_set_usize(gtkPtr, getWidth(), getHeight());
            gtk.gtk_widget_show(gtkPtr);
            Toolkit.getDefaultToolkit().sync();
            if (Platform.platform == Platform.Solaris) {
                X11.Window gtkID = gtk.gdk_x11_drawable_get_xid(gtkPtr.window);
                GdkDisplay display = gtk.gdk_display_get_default();
                X11.Display xdisplay = gtk.gdk_x11_display_get_xdisplay(display);
                X11.INSTANCE.XReparentWindow(xdisplay, gtkID, awtID, 0, 0);
            }
            mozHandle = gtkPtr.getPeer();
        } else {
            long h = moz.getNativeHandleFromAWT(this);
            assert h != 0;
            mozHandle = h;
        }

        return mozHandle;
    }

    public void destroyHandle() {
//FIXME if you can (on some linux systems this code rise a jvm halt)
//        assert isMozillaThread();
//        if (Platform.usingGTK2Toolkit()) {
//            gtk.gtk_widget_destroy(gtkPtr);
//            gtkPtr = null;
//        }
//        mozHandle = 0l;
    }

    @Override
    public long getHandle() {
        if (mozHandle == null) {
            mozHandle = createHandle();
        }
        return mozHandle;
    }

    @Override
    public void addNotify() {
        log.trace("addNotify"); //$NON-NLS-1$
        super.addNotify();
        canvasListener = new CanvasListener();
        addComponentListener(canvasListener);

        Window win = SwingUtilities.getWindowAncestor(this);
        assert win != null;
        windowListener = new WindowListener(win);
        win.addComponentListener(windowListener);
        win.addWindowFocusListener(windowListener);
        win.addWindowListener(windowListener);

        setFocusable(true);

        if (onCreatePeer != null) {
            onCreatePeer.run();
        }

        browserExsist = true;
    }

    @Override
    public void removeNotify() {
        log.trace("removeNotify"); //$NON-NLS-1$

        if (onDestroyPeer != null) {
            onDestroyPeer.run();
        }

        if (canvasListener != null) {
            removeComponentListener(canvasListener);
            canvasListener = null;
        }
        if (windowListener != null) {
            windowListener.win.removeComponentListener(windowListener);
            windowListener.win.removeWindowFocusListener(windowListener);
            windowListener.win.removeWindowListener(windowListener);
            windowListener = null;
        }

        Runnable r = new Runnable() {

            @Override
            public void run() {
                destroyBrowserWindow();
            }
        };
        mozAsyncExec(r);

        super.removeNotify();
    }

    @Override
    public BrowserManager getBrowserManager() {
        return browserManager;
    }

    private class CanvasListener implements ComponentListener {

        @Override
        public void componentHidden(ComponentEvent e) {
            // never called, so we have to register
            // on window ancestor
        }

        @Override
        public void componentShown(ComponentEvent e) {
            // never called, so we have to register
            // on window ancestor
        }

        @Override
        public void componentResized(ComponentEvent e) {
            onResize();
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }
    }

    private class WindowListener extends WindowAdapter implements
            ComponentListener, WindowFocusListener {

        private final Window win;

        public WindowListener(Window win) {
            this.win = win;
        }

        @Override
        public void componentHidden(ComponentEvent e) {
            onHide();
        }

        @Override
        public void componentShown(ComponentEvent e) {
            onShow();
        }

        @Override
        public void componentResized(ComponentEvent e) {
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void windowGainedFocus(WindowEvent e) {
        }

        @Override
        public void windowLostFocus(WindowEvent e) {
        }
        Component lastFocusedCmpOnDeactivate = null;

        @Override
        public void windowActivated(WindowEvent e) {
            log.debug("window activated, lastWas: " + lastFocusedCmpOnDeactivate); //$NON-NLS-1$
            onFocusMovedTo(lastFocusedCmpOnDeactivate);
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
            log.debug("window deactivated"); //$NON-NLS-1$
            lastFocusedCmpOnDeactivate = lastFocusedCmp;
            onFocusMovedTo(null);
        }
    }

    private void onShow() {
        log.trace("onShow"); //$NON-NLS-1$
        // set the visibility on the thing
        if (!isBrowserExisist()) {
            eventBuffer.record("onShow"); //$NON-NLS-1$
            return;
        } else {
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    nsIBaseWindow baseWindow = qi(getWebBrowser(), nsIBaseWindow.class);
                    baseWindow.setVisibility(true);
                }
            };
            mozAsyncExec(r);
        }
    }

    private void onHide() {
        log.trace("onHide"); //$NON-NLS-1$
        if (!isBrowserExisist()) {
            eventBuffer.record("onHide"); //$NON-NLS-1$
            return;
        } else {
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    nsIBaseWindow baseWindow = qi(getWebBrowser(), nsIBaseWindow.class);
                    baseWindow.setVisibility(false);
                }
            };
            mozAsyncExec(r);
        }
    }

    private void onResize() {
        log.trace("onResize"); //$NON-NLS-1$
        if (!isBrowserExisist()) {
            eventBuffer.record("onResize"); //$NON-NLS-1$
            return;
        } else {
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    // if (Platform.platform == Platform.Win32)
                    // Toolkit.getDefaultToolkit().sync();
                    Rectangle rect = getBounds();
                    // sometimes (e.g. when opening javascript:
                    // the height is be negative
                    if (rect.isEmpty()) {
                        return;
                    }
                    nsIBaseWindow baseWindow = qi(getWebBrowser(), nsIBaseWindow.class);
                    baseWindow.setPositionAndSize(rect.x, rect.y, rect.width, rect.height, true);
                    if (Platform.usingGTK2Toolkit() && gtkPtr != null) {
                        gtk.gdk_window_resize(gtkPtr.window, rect.width,
                                rect.height);
                    }
                }
            };
            mozAsyncExec(r);
        }
    }

    @Override
    public void onBrowserAttached() {
        assert isBrowserExisist();
        
        //--adding listeners
        progressListener = new JBrowserProgressListener(this);
        getWebBrowser().addWebBrowserListener(progressListener, nsIWebProgressListener.NS_IWEBPROGRESSLISTENER_IID);

        // set ourselves as the parent uri content listener
        getWebBrowser().setParentURIContentListener(new JBrowserURIContentListener(this));

        // On osx, when user clicks on a textfield,
        // in the mozarea, mozilla paints focus
        // in that textfield.
        //
        // We are supposed to call activate(), but
        // I do not know a good way of listening
        // for that event.
        //
        // For example, GtkEmbed does this with hooking
        // on focus_in_event signal but this does not work
        // in our case because of using the GtkPlug or
        // XReparentWindow
        //
        // Also, there are events DOMFocusIn, DOMFocusOut, DOMActivate
        // but mozilla (xul1.8.1) does not send them correctly
        //
        // So, we listen for all mousedown events, and activate
        // mozarea on such event.
        // Activation on traversal keys (e.g. <Tab>) are
        // handled via onFocusMovedTo.
        if (hasFocus()) {
            // sync state
            onFocusMovedTo(this);
        }
        nsIWebBrowser brow = getWebBrowser();
        nsIDOMWindow2 win = qi(brow.getContentDOMWindow(), nsIDOMWindow2.class);
        nsIDOMEventTarget et = win.getWindowRoot();
        et.addEventListener("mousedown", ml, false); //$NON-NLS-1$
        // listen when focus moves to another swing component
        FocusWatcher.register(this);

        // sync with the enabled images flag
        if (!getBrowserManager().getBrowserConfig().isEnabledImages()) {
            getBrowserManager().getBrowserConfig().disableImages(this);
        }

        // synchronize with current visibility state
        boolean vis = isVisible();
        nsIBaseWindow baseWindow = qi(getWebBrowser(),
                nsIBaseWindow.class);
        baseWindow.setVisibility(vis);

        eventBuffer.replayOn(this);

        if (urlToOpen != null) {
            setUrl(urlToOpen);
        }

        fireOnBrowserAttached();
    }

    @Override
    public void onBrowserDetached() {
        webBrowser.removeWebBrowserListener(progressListener, nsIWebProgressListener.NS_IWEBPROGRESSLISTENER_IID);

        nsIWebBrowser brow = getWebBrowser();
        nsIDOMWindow2 win = qi(brow.getContentDOMWindow(), nsIDOMWindow2.class);
        nsIDOMEventTarget et = win.getWindowRoot();
        
        if (et != null) {
        	et.removeEventListener("mousedown", ml, false); //$NON-NLS-1$
        }
        
        FocusWatcher.unregister(this);

        browserExsist = false;
        destroyHandle();

        fireOnBrowserDetached();
    }

    private class MozMouseListener implements nsIDOMEventListener {

        @Override
        public void handleEvent(nsIDOMEvent ev) {
            log.debug("dom event " + ev.getType()); //$NON-NLS-1$
            swingAsyncExec(new Runnable() {

                @Override
                public void run() {
                    if (lastFocusedCmp != JBrowserCanvas.this) {
                        JBrowserCanvas.this.requestFocus();
                    }
                }
            });
        }

        @Override
        public nsISupports queryInterface(String uuid) {
            return Mozilla.queryInterface(this, uuid);
        }
    }

    public void onFocusMovedTo(Component cmp) {
        if (cmp == lastFocusedCmp) {
            return;
        }
        log.debug("focus moved to: " + //$NON-NLS-1$
                (lastFocusedCmp == null ? null : lastFocusedCmp.getClass().getSimpleName()) + " -> " + //$NON-NLS-1$
                (cmp == null ? null : cmp.getClass().getSimpleName()));

        if (cmp == this) {
            if (lastFocusedCmp != this && isBrowserExisist()) {
                Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        nsIWebBrowserFocus webBrowserFocus = qi(getWebBrowser(), nsIWebBrowserFocus.class);
                        webBrowserFocus.activate();
                        log.debug("-------mozilla activated"); //$NON-NLS-1$
                    }
                };
                mozAsyncExec(r);
            }
        } else {
            if (lastFocusedCmp == this && isBrowserExisist()) {
                Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        nsIWebBrowserFocus webBrowserFocus = qi(getWebBrowser(), nsIWebBrowserFocus.class);

                        if (Platform.platform == Platform.OSX) {
                            // this code does not work on win32
                            //
                            // if clicking away from a focuse <a> element, into
                            // a JTextfield, mozilla does not un-paint the the
                            // dotted rectangle, so do that manually
                            try {
                                log.debug("blurring"); //$NON-NLS-1$
                                nsIDOMElement el = webBrowserFocus.getFocusedElement();
                                log.debug("el=" + el); //$NON-NLS-1$
                                nsIDOMHTMLAnchorElement ael = qi(el,
                                        nsIDOMHTMLAnchorElement.class);
                                if (ael != null) {
                                    ael.blur();
                                    log.debug("-------link blurred"); //$NON-NLS-1$
                                }
                            } catch (XPCOMException e) {
                                // ignore
                            }
                        }

                        webBrowserFocus.deactivate();
                        log.debug("-------mozilla deactivated"); //$NON-NLS-1$

                    }
                };
                mozAsyncExec(r);
            }
        }

        lastFocusedCmp = cmp;
    }

    @Override
    public void onCreatePeer(Runnable action) {
        onCreatePeer = action;

        if (onCreatePeer == null && isDisplayable()) {
            action.run();
        }
    }

    @Override
    public void onDestroyPeer(Runnable action) {
        onDestroyPeer = action;
    }

    //--JBrowser interface
    @Override
    public Canvas getComponent() {
        return this;
    }

    @Override
    public boolean back() {
        if (!isBrowserExisist()) {
            return false;
        }
        mozAsyncExec(new Runnable() {

            @Override
            public void run() {
                nsIWebNavigation nav = qi(getWebBrowser(), nsIWebNavigation.class);
                nav.goBack();
            }
        });
        return true;
    }

    @Override
    public boolean disposeBrowser() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean forward() {
        if (!isBrowserExisist()) {
            return false;
        }
        mozAsyncExec(new Runnable() {

            @Override
            public void run() {
                nsIWebNavigation nav = qi(getWebBrowser(), nsIWebNavigation.class);
                nav.goForward();
            }
        });
        return true;
    }

    @Override
    public Document getDocument() {
        if (isBrowserExisist()) {
            nsIDOMDocument nsdoc = getWebBrowser().getContentDOMWindow().getDocument();
            return (Document) NodeFactory.getNodeInstance(nsdoc);
        }

        return null;
    }

    @Override
    public String getFavIcon() {
        try {
            Document document = getDocument();

            if (document == null) {
                return "";
            }

            NodeList head = getDocument().getElementsByTagName("head");

            if (head == null || head.getLength() == 0) {
                return "";
            }

            NodeList list = head.item(0).getChildNodes();
            if (list == null || list.getLength() == 0) {
                return "";
            }

            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);

                if ("link".equals(node.getNodeName()) &&
                        node.hasAttributes()) {

                    NamedNodeMap map = node.getAttributes();

                    if (map.getNamedItem("rel") != null &&
                            "shortcut icon".equals(map.getNamedItem("rel").getNodeValue().toLowerCase())) {
                        return map.getNamedItem("href").getNodeValue();
                    }
                }

            }
        } catch (Throwable ex) {
            return "";
        }

        return "";
    }

    @Override
    public String getTitle() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUrl() {
        if (!isBrowserExisist()) {
            return "";
        }

        return mozSyncExecQuiet(new Callable<String>() {

            @Override
            public String call() {
                nsIWebBrowser brow = getWebBrowser();
                nsIWebNavigation nav = qi(brow, nsIWebNavigation.class);
                nsIURI uri = nav.getCurrentURI();

                if (uri == null) {
                    return "";
                }
                return uri.getSpec();
            }
        });
    }

    @Override
    public boolean isBrowserExisist() {
        return browserExsist && webBrowser != null;
    }

    @Override
    public boolean refresh() {
        if (!isBrowserExisist()) {
            return false;
        }
        mozAsyncExec(new Runnable() {

            @Override
            public void run() {
                nsIWebNavigation nav = qi(getWebBrowser(), nsIWebNavigation.class);
                nav.reload(nsIWebNavigation.LOAD_FLAGS_NONE);
            }
        });
        return true;
    }

    @Override
    public boolean setText(final String content) {
        mozAsyncExec(new Runnable() { public void run() {
            MozillaAutomation.triggerLoadHTML(JBrowserCanvas.this, content, null);
        }});
        
		return true;
    }

    @Override
    public boolean setUrl(final String url) {
        if (isBrowserExisist()) {
            mozAsyncExec(new Runnable() {

                @Override
                public void run() {
                    nsIWebNavigation nav = qi(getWebBrowser(), nsIWebNavigation.class);
                    nav.loadURI(url, nsIWebNavigation.LOAD_FLAGS_NONE, null, null, null);
                }
            });
        } else {
            urlToOpen = url;
        }

        return true;
    }

    @Override
    public boolean stop() {
        if (!isBrowserExisist()) {
            return false;
        }
        mozAsyncExec(new Runnable() {

            @Override
            public void run() {
                nsIWebNavigation nav = qi(getWebBrowser(), nsIWebNavigation.class);
                nav.stop(nsIWebNavigation.STOP_ALL);
            }
        });
        return true;
    }

//--Native browser listner	
    @Override
    public void destroyBrowserWindow() {
        assert MozillaExecutor.isMozillaThread();
        log.trace("destroyBrowserWindow"); //$NON-NLS-1$
        if (inModalLoop) {
            exitModalEventLoop(NS_OK);
        }

        getBrowserManager().getWindowCreator().detachBrowser(this);
        fireCloseWindow();
        removeAllListeners();
    }

    @Override
    public void exitModalEventLoop(long arg0) {
        log.trace("exitModalEventLoop"); //$NON-NLS-1$
        MozillaExecutor.mozExitModalEventLoop();
        inModalLoop = false;
    }

    @Override
    public long getChromeFlags() {
        return chromeFlags;
    }

    @Override
    public nsIWebBrowser getWebBrowser() {
        return webBrowser;
    }

    @Override
    public boolean isWindowModal() {
        return inModalLoop;
    }

    @Override
    public void setChromeFlags(long chromeFlags) {
        this.chromeFlags = chromeFlags;
        log.trace(String.format("setChromeFlags %d", chromeFlags)); //$NON-NLS-1$
    }

    @Override
    public void setStatus(long statusType, final String status) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                fireSetStatus(status);
            }
        });
    }

    @Override
    public void setWebBrowser(nsIWebBrowser webBrowser) {
        this.webBrowser = webBrowser;
    }

    @Override
    public void showAsModal() {
        assert MozillaExecutor.isMozillaThread();
        log.trace("showAsModal"); //$NON-NLS-1$
        //create JS context, that will hold the 'arguments'
        //passed via JS code in some XUL files, e.g.
        //content/global/commonDialog.xul
        nsIJSContextStack stack = XPCOMUtils.getService("@mozilla.org/js/xpc/ContextStack;1", nsIJSContextStack.class); //$NON-NLS-1$
        stack.push(0);
        inModalLoop = true;
        MozillaExecutor.mozEnterModalEventLoop();
        long cx = stack.pop();
        assert cx == 0;
    }

    @Override
    public void sizeBrowserTo(final int aCX, final int aCY) {
        assert MozillaExecutor.isMozillaThread();
        log.trace(String.format("sizeBrowserTo %d %d", aCX, aCY)); //$NON-NLS-1$
        swingAsyncExec(new Runnable() {

            @Override
            public void run() {
                fireSetSize(aCX, aCY);
            }
        });
    }

    @Override
    public void focusNextElement() {
        //called when focus is leaving the gecko area and
        //we should set focus to next swing component
        log.trace("focusNextElement"); //$NON-NLS-1$
        swingAsyncExec(new Runnable() {

            @Override
            public void run() {
                transferFocus();
            }
        });
    }

    @Override
    public void focusPrevElement() {
        //called when focus is leaving the gecko area and
        //we should set focus to previous swing component
        log.trace("focusPrevElement"); //$NON-NLS-1$
        swingAsyncExec(new Runnable() {

            @Override
            public void run() {
                transferFocusBackward();
            }
        });
    }

    @Override
    public void getDimensions(long flags, int[] x, int[] y, int[] cx, int[] cy) {
        assert MozillaExecutor.isMozillaThread();
        log.trace("getDimension"); //$NON-NLS-1$

        nsIBaseWindow baseWindow = qi(webBrowser, nsIBaseWindow.class);
        if ((flags & DIM_FLAGS_POSITION) != 0 &&
                (flags & (DIM_FLAGS_SIZE_INNER | DIM_FLAGS_SIZE_OUTER)) != 0) {
            baseWindow.getPositionAndSize(x, y, cx, cy);
        } else if ((flags & DIM_FLAGS_POSITION) != 0) {
            baseWindow.getPosition(x, y);
        } else if ((flags & (DIM_FLAGS_SIZE_INNER | DIM_FLAGS_SIZE_OUTER)) != 0) {
            baseWindow.getSize(cx, cy);
        } else {
            throw new XPCOMException(NS_ERROR_INVALID_ARG);
        }
    }

    @Override
    public long getSiteWindow() {
        assert MozillaExecutor.isMozillaThread();
        return getHandle();
    }

    @Override
    public boolean getVisibility() {
        assert MozillaExecutor.isMozillaThread();
        log.trace("getVisibility=" + visibility); //$NON-NLS-1$

        // Work around the problem that sometimes the window
        // is already visible even though mVisibility isn't true
        // yet.
        return visibility ||
                (browserExsist && isDisplayable());//is this needed?
    }

    @Override
    public void setDimensions(long flags, int x, int y, int cx, int cy) {
        assert MozillaExecutor.isMozillaThread();
        log.trace(String.format("setDimension flag=%d %d,%d %dx%d\n", flags, x, y, cx, cy)); //$NON-NLS-1$

        nsIBaseWindow baseWindow = qi(webBrowser, nsIBaseWindow.class);
        if ((flags & DIM_FLAGS_POSITION) != 0 &&
                ((flags & (DIM_FLAGS_SIZE_INNER | DIM_FLAGS_SIZE_OUTER))) != 0) {
            baseWindow.setPositionAndSize(x, y, cx, cy, true);
        } else if ((flags & DIM_FLAGS_POSITION) != 0) {
            baseWindow.setPosition(x, y);
        } else if ((flags & (DIM_FLAGS_SIZE_INNER | DIM_FLAGS_SIZE_OUTER)) != 0) {
            baseWindow.setSize(cx, cy, true);
        } else {
            throw new XPCOMException(NS_ERROR_INVALID_ARG);
        }
    }

    @Override
    public void setFocus() {
        assert MozillaExecutor.isMozillaThread();
        nsIBaseWindow baseWindow = qi(webBrowser, nsIBaseWindow.class);
        baseWindow.setFocus();
    }

    @Override
    public void setTitle(final String title) {
        assert MozillaExecutor.isMozillaThread();
        log.trace("setTitle=" + title); //$NON-NLS-1$
        swingAsyncExec(new Runnable() {

            @Override
            public void run() {
                fireSetTitle(title);
            }
        });
    }

    @Override
    public void setVisibility(boolean aVisibility) {
        assert MozillaExecutor.isMozillaThread();
        log.trace("setVisibility=" + aVisibility); //$NON-NLS-1$

        // We always set the visibility so that if it's chrome and we finish
        // the load we know that we have to show the window.
        visibility = aVisibility;

        // if this is a chrome window and the chrome hasn't finished loading
        // yet then don't show the window yet.
        if (!browserExsist) {
            return;
        }

        swingAsyncExec(new Runnable() {

            @Override
            public void run() {
                fireSetVisible(visibility);
            }
        });
    }

    @Override
    public void onHideTooltip() {
        assert MozillaExecutor.isMozillaThread();
        log.trace("hide tip"); //$NON-NLS-1$
        swingAsyncExec(new Runnable() {

            @Override
            public void run() {
                if (tooltip == null) {
                    return;
                }
                tooltip.setVisible(false);
                tooltip.dispose();
                tooltip = null;
            }
        });
    }

    @Override
    public void onShowTooltip(final int XCoords, final int YCoords, final String tipText) {
        assert MozillaExecutor.isMozillaThread();
        log.trace(String.format("shot tooltip: %d,%d '%s'", XCoords, YCoords, tipText)); //$NON-NLS-1$
        swingAsyncExec(new Runnable() {

            @Override
            public void run() {
                if (tooltip == null) {
                    tooltip = new JFakeTooltip();
                }
                tooltip.setup(XCoords, YCoords, tipText);
                tooltip.setVisible(true);
            }
        });
    }
    
	@Override
	public void onShowContextMenu(long aContextFlags, nsIDOMEvent aEvent, nsIDOMNode aNode) {
		fireShowContextMenu(aContextFlags, aEvent, aNode);
	}

	@Override
    public nsISupports getInterface(String riid) {
        if (riid.equals(nsIDOMWindow.NS_IDOMWINDOW_IID)) {
            //otherwise when creating a new window the code
            //in nsWindowWatcher::OpenWindowJSInternal after calling
            //  mWindowCreator->CreateChromeWindow(...)
            //fails on the test
            //  nsCOMPtr<nsIDOMWindow> newWindow(do_GetInterface(newChrome));
            //  if (newWindow)...
            nsIDOMWindow contentDOMWindow = webBrowser.getContentDOMWindow();
            return contentDOMWindow;
        }
        
        return queryInterface(riid);
    }

    @Override
    public nsISupports queryInterface(String uuid) {
    	if (uuid.equals(nsIContextMenuListener.NS_ICONTEXTMENULISTENER_IID)) {
			return this;
    	}
    	
		return Mozilla.queryInterface(this, uuid);
    }

    @Override
    public void processEvent(AWTEvent arg0) {
        super.processEvent(arg0);
    }
    
    @Override
	public nsIDocShell getDocShell() {
		if (docShell == null) {
			docShell = (nsIDocShell) getInterfaceRequestor().getInterface(nsIDocShell.NS_IDOCSHELL_IID);
		}
		
		return docShell;
	}
	
    @Override
	public nsIClipboardCommands getClipboardCommands() {
		if (clipboardCommands == null) {
			clipboardCommands = XPCOMUtils.qi(getDocShell(), nsIClipboardCommands.class);
		}
		
		return clipboardCommands;
	}
    
    @Override
    public nsIInterfaceRequestor getInterfaceRequestor() {
		if (interfaceRequestor == null) {
			interfaceRequestor = XPCOMUtils.qi(getWebBrowser(), nsIInterfaceRequestor.class);
		}
		
		return interfaceRequestor;
    }

	@Override
	public byte[] asImage() {
		String b64data = mozSyncExecQuiet(new Callable<String>() {
			@Override
			public String call() throws Exception {
              //create a hidden browser window with <canvas> element
              nsIWebBrowser wb = getWebBrowser();
              nsIDOMDocument doc = wb.getContentDOMWindow().getDocument();
              nsIDOMElement elem = doc.createElementNS("http://www.w3.org/1999/xhtml", "html:canvas"); //$NON-NLS-1$ //$NON-NLS-2$
              nsIDOMHTMLCanvasElement canvas = qi(elem, nsIDOMHTMLCanvasElement.class);
              nsIDOMCanvasRenderingContext2D context = qi(canvas.getContext("2d"), nsIDOMCanvasRenderingContext2D.class); //$NON-NLS-1$

              nsIDOMWindow domWin = getWebBrowser().getContentDOMWindow();
              //find out size of the document to be rendered
              int w, h;
              nsIDOMNSHTMLElement nselem = qi(domWin.getDocument().getDocumentElement(), nsIDOMNSHTMLElement.class);
              if (nselem != null) {
                  w =
                          nselem.getOffsetWidth() > nselem.getScrollWidth() ? nselem.getOffsetWidth() : nselem.getScrollWidth();
                  h =
                          nselem.getOffsetHeight() > nselem.getScrollHeight() ? nselem.getOffsetHeight() : nselem.getScrollHeight();
              } else {
                  nsIDOMXULElement xulelem = qi(domWin.getDocument().getDocumentElement(), nsIDOMXULElement.class);
                  if (xulelem != null) {
                      try {
                          w = Integer.parseInt(xulelem.getWidth());
                          h = Integer.parseInt(xulelem.getHeight());
                      } catch (NumberFormatException e) {
                          w = h = 1000;
                      }
                  } else {
                      w = h = 1000;
                  }
              }
              if (h > 16384) {
                  h = 16384; //limits in canvas in code
              }
              //fit canvas size to the content and
              //render the document there
              canvas.setWidth(w);
              canvas.setHeight(h);
              context.drawWindow(domWin, 0, 0, w, h, "rgb(255,255,255)"); //$NON-NLS-1$

              //get content of the canvas as png image
              return canvas.toDataURLAs("image/png", ""); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
    	
        b64data = b64data.replaceAll("^data:image/png;base64,", ""); //$NON-NLS-1$ //$NON-NLS-2$
        
        try {
        	return Base64.decode(b64data);
        } catch (Exception e) {
        	// do noting return null
        }
        
        return null;
	}

	@Override
	public void sizeToContent() {
		mozSyncExec(new Runnable() {
			@Override
			public void run() {
				getWebBrowser().getContentDOMWindow().sizeToContent();
			}
		});
	}
	
	@Override
	public ScrollControl getScrollControl() {
		return scrollControl;
	}

}
