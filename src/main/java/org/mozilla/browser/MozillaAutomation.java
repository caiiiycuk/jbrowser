package org.mozilla.browser;

import static org.mozilla.browser.MozillaExecutor.isMozillaThread;
import static org.mozilla.browser.MozillaExecutor.mozSyncExec;
import static org.mozilla.browser.MozillaExecutor.mozSyncExecQuiet;
import static org.mozilla.browser.XPCOMUtils.create;
import static org.mozilla.browser.XPCOMUtils.getService;
import static org.mozilla.browser.XPCOMUtils.qi;

import java.awt.Rectangle;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import net.sourceforge.iharder.Base64;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.browser.impl.BlockingURLLoader;
import org.mozilla.dom.DocumentImpl;
import org.mozilla.dom.ElementImpl;
import org.mozilla.dom.NodeFactory;
import org.mozilla.interfaces.nsIBoxObject;
import org.mozilla.interfaces.nsIDOMAbstractView;
import org.mozilla.interfaces.nsIDOMClientRect;
import org.mozilla.interfaces.nsIDOMClientRectList;
import org.mozilla.interfaces.nsIDOMDocument;
import org.mozilla.interfaces.nsIDOMDocumentEvent;
import org.mozilla.interfaces.nsIDOMDocumentView;
import org.mozilla.interfaces.nsIDOMElement;
import org.mozilla.interfaces.nsIDOMEvent;
import org.mozilla.interfaces.nsIDOMEventTarget;
import org.mozilla.interfaces.nsIDOMHTMLDocument;
import org.mozilla.interfaces.nsIDOMHTMLFrameElement;
import org.mozilla.interfaces.nsIDOMHTMLIFrameElement;
import org.mozilla.interfaces.nsIDOMKeyEvent;
import org.mozilla.interfaces.nsIDOMMouseEvent;
import org.mozilla.interfaces.nsIDOMNSDocument;
import org.mozilla.interfaces.nsIDOMNSElement;
import org.mozilla.interfaces.nsIDOMWindow;
import org.mozilla.interfaces.nsIDOMXPathEvaluator;
import org.mozilla.interfaces.nsIDOMXPathResult;
import org.mozilla.interfaces.nsIDocShell;
import org.mozilla.interfaces.nsIInterfaceRequestor;
import org.mozilla.interfaces.nsISimpleEnumerator;
import org.mozilla.interfaces.nsIStringInputStream;
import org.mozilla.interfaces.nsIURI;
import org.mozilla.interfaces.nsIWebBrowser;
import org.mozilla.interfaces.nsIWebBrowserChrome;
import org.mozilla.interfaces.nsIWebNavigation;
import org.mozilla.interfaces.nsIWindowWatcher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.atomation.jbrowser.impl.JBrowserComponent;

/**
 * Mozilla automation for junit test cases
 */
public class MozillaAutomation {

    static Log log = LogFactory.getLog(MozillaAutomation.class);

    /**
     * Loads an url into a mozilla window,
     * blocking until the load finishes or expires.
     */
    public static boolean blockingLoad(JBrowserComponent<?> component, String url) {
        return blockingLoad(component, url, null);
    }

    /**
     * Loads an url into a mozilla window.
     * Additionally, POST data can be specified in
     * the format param1=val1&param2=val2...
     *
     * The function waits while the URL is loading and
     * returns only when loading is finished.
     *
     * Returns true if the load failed
     * (page does not exist)
     */
    public static boolean blockingLoad(JBrowserComponent<?> component, final String url, final String postData) {
        if (isMozillaThread()) {
            throw new RuntimeException(); //broken atm when calling from mozilla thread
        }        //log.debug("Loading document "+url);

        final nsIStringInputStream postDataStream;
        if (postData != null) {
            try {
                postDataStream = create("@mozilla.org/io/string-input-stream;1", nsIStringInputStream.class); //$NON-NLS-1$
                String streamData = String.format(
                        "Content-Type: application/x-www-form-urlencoded\r\n" + //$NON-NLS-1$
                        "Content-Length: %d\r\n" + //$NON-NLS-1$
                        "\r\n" + //$NON-NLS-1$
                        "%s", //$NON-NLS-1$
                        postData.getBytes("UTF-8").length, //$NON-NLS-1$
                        postData);
                postDataStream.setData(streamData, -1);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } else {
            postDataStream = null;
        }

        BlockingURLLoader l = new BlockingURLLoader(component) {

            @Override
            public boolean triggerURLLoading() {
                if (!component.isBrowserExisist()) {
                    return true;
                }
                nsIWebNavigation nav = qi(component.getWebBrowser(), nsIWebNavigation.class);
                nav.loadURI(url, nsIWebNavigation.LOAD_FLAGS_NONE, null, postDataStream, null);
                return false;
            }

            @Override
            public void onLoadStarted() {
            }

            @Override
            public void onLoadEnded() {
            }
        };
        l.load();
        return l.getLoadFailed();
    }

    /**
     * Loads html content into a mozilla window.
     * Sets the given url as the origin of the content.
     */
    public static boolean blockingLoadHTML(final JBrowserComponent<?> component, final String content, final String asUrl) {
        if (isMozillaThread()) {
            throw new RuntimeException(); //broken atm when calling from mozilla thread
        }        //log.debug("Loading html content"+content);

        BlockingURLLoader l = new BlockingURLLoader(component) {

            @Override
            public boolean triggerURLLoading() {
                return triggerLoadHTML(component, content, asUrl);
            }

            @Override
            public void onLoadStarted() {
            }

            @Override
            public void onLoadEnded() {
            }
        };
        l.load();
        return l.getLoadFailed();
    }

    /**
     * Triggers loading of web page with the give content.
     * Set the given url as the origin of the content.
     *
     * Returns true if failed.
     */
    public static boolean triggerLoadHTML(JBrowserComponent<?> component, String content, String asUrl) {
        assert isMozillaThread();

        if (!component.isBrowserExisist()) {
            return true;
        }

        if (asUrl == null) {
            final String b64content;
            try {
                b64content = Base64.encodeBytes(content.getBytes("UTF-8")); //$NON-NLS-1$
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            final String dataURI = "data:text/html;base64," + b64content; //$NON-NLS-1$

            nsIWebBrowser brow = component.getWebBrowser();
            nsIWebNavigation nav = qi(brow, nsIWebNavigation.class);
            nav.loadURI(dataURI, nsIWebNavigation.LOAD_FLAGS_NONE, null, null, null);
            return false;
        } else {
            nsIURI uri = create("@mozilla.org/network/simple-uri;1", nsIURI.class); //$NON-NLS-1$
            uri.setSpec(asUrl);

            nsIStringInputStream is = create("@mozilla.org/io/string-input-stream;1", nsIStringInputStream.class); //$NON-NLS-1$
            is.setData(content, content.length());

            nsIWebBrowser webBrowser = component.getWebBrowser();
            nsIInterfaceRequestor ir = qi(webBrowser, nsIInterfaceRequestor.class);
            nsIDocShell docShell = (nsIDocShell) ir.getInterface(nsIDocShell.NS_IDOCSHELL_IID);//
            docShell.loadStream(is, uri, "text/html", "utf-8", null); //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }
    }

    /**
     * Load previous page in session history.
     * Method blocks until loading is completed.
     */
    public static boolean blockingBack(JBrowserComponent<?> component) {
        if (isMozillaThread()) {
            throw new RuntimeException(); //broken atm when calling from mozilla thread
        }        //log.debug("Go Back");
        BlockingURLLoader l = new BlockingURLLoader(component) {

            @Override
            public boolean triggerURLLoading() {
                if (!component.isBrowserExisist()) {
                    return true;
                }
                nsIWebNavigation nav = qi(component.getWebBrowser(), nsIWebNavigation.class);
                nav.goBack();
                return false;
            }

            @Override
            public void onLoadStarted() {
            }

            @Override
            public void onLoadEnded() {
            }
        };
        l.load();
        return l.getLoadFailed();
    }

    /**
     * Load next page in session history.
     * Method blocks until loading is completed.
     */
    public static boolean blockingForward(JBrowserComponent<?> component) {
        if (isMozillaThread()) {
            throw new RuntimeException(); //broken atm when calling from mozilla thread
        }        //log.debug("Go Back");
        BlockingURLLoader l = new BlockingURLLoader(component) {

            @Override
            public boolean triggerURLLoading() {
                if (!component.isBrowserExisist()) {
                    return true;
                }
                nsIWebNavigation nav = qi(component.getWebBrowser(), nsIWebNavigation.class);
                nav.goForward();
                return false;
            }

            @Override
            public void onLoadStarted() {
            }

            @Override
            public void onLoadEnded() {
            }
        };
        l.load();
        return l.getLoadFailed();
    }

    /**
     * Reload current page.
     * Method blocks until loading is completed.
     */
    public static boolean blockingReload(JBrowserComponent<?> component) {
        if (isMozillaThread()) {
            throw new RuntimeException(); //broken atm when calling from mozilla thread
        }        //log.debug("Go Back");
        BlockingURLLoader l = new BlockingURLLoader(component) {

            @Override
            public boolean triggerURLLoading() {
                if (!component.isBrowserExisist()) {
                    return true;
                }
                nsIWebNavigation nav = qi(component.getWebBrowser(), nsIWebNavigation.class);
                nav.reload(nsIWebNavigation.LOAD_FLAGS_NONE);
                return false;
            }

            @Override
            public void onLoadStarted() {
            }

            @Override
            public void onLoadEnded() {
            }
        };
        l.load();
        return l.getLoadFailed();
    }

    public static String getCurrentURI(JBrowserComponent<?> component) {
        assert isMozillaThread();

        nsIWebBrowser brow = component.getWebBrowser();
        nsIWebNavigation nav = qi(brow, nsIWebNavigation.class);
        nsIURI uri = nav.getCurrentURI();
        return uri.getSpec();
    }

    /**
     * Clicks on a DOM element given by id.
     * Returns true, if failed.
     */
    public static boolean click(JBrowserComponent<?> component, String id) {
        assert isMozillaThread();

        if (!component.isBrowserExisist()) {
            return true;
        }

        nsIWebBrowser brow = component.getWebBrowser();
        nsIDOMDocument doc = brow.getContentDOMWindow().getDocument();

        nsIDOMElement elem = doc.getElementById(id);
        if (elem == null) {
            return true;
        }

        return click(elem);
    }

    /**
     * Clicks on a DOM element.
     * Returns true, if failed.
     */
    public static boolean click(nsIDOMElement elem) {
        assert isMozillaThread();

        //based on http://developer.mozilla.org/en/docs/DOM:document.createEvent
        nsIDOMDocument doc = elem.getOwnerDocument();
        nsIDOMDocumentEvent evdoc = qi(doc, nsIDOMDocumentEvent.class);
        nsIDOMEvent ev = evdoc.createEvent("MouseEvents"); //$NON-NLS-1$
        nsIDOMMouseEvent mev = qi(ev, nsIDOMMouseEvent.class);
        nsIDOMDocumentView view = qi(doc, nsIDOMDocumentView.class);
        nsIDOMAbstractView aview = view.getDefaultView();
        mev.initMouseEvent("click", true, true, aview, 0, 0, 0, 0, 0, false, false, false, false, 0, null); //$NON-NLS-1$
        nsIDOMEventTarget evt = qi(elem, nsIDOMEventTarget.class);
        boolean canceled = !evt.dispatchEvent(mev);
        return canceled;
    }

    /**
     * Types text into a DOM element given by id.
     * Returns true, if failed.
     */
    public static boolean type(JBrowserComponent<?> component, String id, String text) {
        assert isMozillaThread();

        if (!component.isBrowserExisist()) {
            return true;
        }

        nsIWebBrowser brow = component.getWebBrowser();
        nsIDOMWindow domwin = brow.getContentDOMWindow();
        nsIDOMDocument doc = domwin.getDocument();

        nsIDOMElement elem = doc.getElementById(id);
        if (elem == null) {
            return true;
        }

        return type(elem, text);
    }

    /**
     * Simulates mousemove over a DOM element.
     * Returns true, if failed.
     */
    public static boolean mousemove(JBrowserComponent<?> component, String id, float relX, float relY) {
        assert MozillaExecutor.isMozillaThread();

        if (!component.isBrowserExisist()) {
            return true;
        }

        nsIWebBrowser brow = component.getWebBrowser();
        nsIDOMDocument doc = brow.getContentDOMWindow().getDocument();

        nsIDOMElement elem = doc.getElementById(id);
        if (elem == null) {
            return true;
        }

        return mousemove(elem, relX, relY);
    }

    /**
     * Simulates mousemove over a DOM element.
     * Returns true, if failed.
     */
    public static boolean mousemove(nsIDOMElement elem, float relX, float relY) {
        assert isMozillaThread();

        //based on http://developer.mozilla.org/en/docs/DOM:document.createEvent
        nsIDOMDocument doc = elem.getOwnerDocument();

        nsIDOMNSDocument nsdoc = qi(doc, nsIDOMNSDocument.class);
        nsIBoxObject box = nsdoc.getBoxObjectFor(elem);
        int sx = box.getScreenX();
        int sy = box.getScreenY();
        int h = box.getHeight();
        int w = box.getWidth();

        nsIDOMDocumentEvent evdoc = qi(doc, nsIDOMDocumentEvent.class);
        nsIDOMEvent ev = evdoc.createEvent("MouseEvents"); //$NON-NLS-1$
        nsIDOMMouseEvent mev = qi(ev, nsIDOMMouseEvent.class);
        nsIDOMDocumentView view = qi(doc, nsIDOMDocumentView.class);
        nsIDOMAbstractView aview = view.getDefaultView();

        int x = (int) (relX * w);
        int y = (int) (relY * h);
        if (x < 0 || x >= w) {
            x = 0;
        }
        if (y < 0 || y >= h) {
            y = 0;
        }

        //http://www.codingforums.com/showthread.php?t=21674
        mev.initMouseEvent("mousemove", true, true, aview, 0, //$NON-NLS-1$
                sx + x, sy + y,
                x, y,
                false, false, false, false, 0, null);
        nsIDOMEventTarget evt = qi(elem, nsIDOMEventTarget.class);
        boolean canceled = !evt.dispatchEvent(mev);
        return canceled;
    }

    /**
     * Types text into a DOM element.
     * Returns true, if failed.
     */
    public static boolean type(nsIDOMElement elem, String text) {
        assert isMozillaThread();

        //based on http://developer.mozilla.org/en/docs/DOM:event.initKeyEvent
        nsIDOMDocument doc = elem.getOwnerDocument();
        nsIDOMDocumentEvent evdoc = qi(doc, nsIDOMDocumentEvent.class);
        nsIDOMEvent ev = evdoc.createEvent("KeyboardEvent"); //$NON-NLS-1$
        nsIDOMKeyEvent mev = qi(ev, nsIDOMKeyEvent.class);
        nsIDOMDocumentView view = qi(doc, nsIDOMDocumentView.class);
        nsIDOMAbstractView aview = view.getDefaultView();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            mev.initKeyEvent("keypress", true, true, aview, false, false, false, false, 0, c); //$NON-NLS-1$
            nsIDOMEventTarget evt = qi(elem, nsIDOMEventTarget.class);
            boolean canceled = !evt.dispatchEvent(mev);
            if (canceled) {
                return canceled;
            }
        }
        return false;
    }

    /**
     * Wait at most waitMillis until a window with title winTitle is open
     */
    public static JBrowserComponent<?> waitForWindowWithTitle(String winTitle, int waitMillis) {
        //can't wait on mozilla thread
        assert !isMozillaThread();

        List<JBrowserComponent<?>> wins = waitForNumWindowsWithTitle(winTitle, 1, waitMillis);
        return wins.isEmpty() ? null : wins.get(0);
    }

    /**
     * Wait at most waitMillis until numWins windows with title winTitle are open
     */
    public static List<JBrowserComponent<?>> waitForNumWindowsWithTitle(final String winTitle, int numWins, int waitMillis) {
        //can't wait on mozilla thread
        assert !isMozillaThread();

        final List<JBrowserComponent<?>> wins = new LinkedList<JBrowserComponent<?>>();
        mozSyncExec(new Runnable() {

            public void run() {
                wins.clear();
                wins.addAll(findWindowsByTitle(winTitle));
            }
        });

        int waited = 0;
        while (wins.size() < numWins && waited < waitMillis) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                log.error("wait interrupted", e); //$NON-NLS-1$
            }
            waited += 300;

            mozSyncExec(new Runnable() {

                public void run() {
                    wins.clear();
                    wins.addAll(findWindowsByTitle(winTitle));
                }
            });
        }

        return wins;
    }

    public static JBrowserComponent<?> waitForNoWindowWithTitle(final String winTitle, int waitMillis) {
        //can't wait on mozilla thread
        assert !isMozillaThread();

        final List<JBrowserComponent<?>> wins = new LinkedList<JBrowserComponent<?>>();
        mozSyncExec(new Runnable() {

            public void run() {
                wins.clear();
                wins.addAll(findWindowsByTitle(winTitle));
            }
        });

        int waited = 0;
        while (!wins.isEmpty() && waited < waitMillis) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                log.error("wait interrupted", e); //$NON-NLS-1$
            }
            waited += 300;

            mozSyncExec(new Runnable() {

                public void run() {
                    wins.clear();
                    wins.addAll(findWindowsByTitle(winTitle));
                }
            });
        }

        return wins.isEmpty() ? null : wins.get(0);
    }

    /**
     * Wait at most waitMillis until a window containing
     * a node with nodeText is open
     */
    public static JBrowserComponent<?> waitForWindowWithNodeText(String nodeText, int waitMillis) {
        //can't wait on mozilla thread
        assert !isMozillaThread();

        List<JBrowserComponent<?>> wins = waitForNumWindowsWithNodeText(nodeText, 1, waitMillis);
        return wins.isEmpty() ? null : wins.get(0);
    }

    /**
     * Wait at most waitMillis until numWins windows containing
     * a node with nodeText are open
     */
    public static List<JBrowserComponent<?>> waitForNumWindowsWithNodeText(final String nodeText,
            int numWins,
            int waitMillis) {
        //can't wait on mozilla thread
        assert !isMozillaThread();

        final List<JBrowserComponent<?>> wins = new LinkedList<JBrowserComponent<?>>();
        mozSyncExec(new Runnable() {

            public void run() {
                wins.clear();
                wins.addAll(findWindowsByNodeText(nodeText));
            }
        });

        int waited = 0;
        while (wins.size() < numWins && waited < waitMillis) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                log.error("wait interrupted", e); //$NON-NLS-1$
            }
            waited += 300;

            mozSyncExec(new Runnable() {

                public void run() {
                    wins.clear();
                    wins.addAll(findWindowsByNodeText(nodeText));
                }
            });
        }

        return wins;
    }

    public static JBrowserComponent<?> waitForNoWindowWithNodeText(final String nodeText, int waitMillis) {
        //can't wait on mozilla thread
        assert !isMozillaThread();

        final List<JBrowserComponent<?>> wins = new LinkedList<JBrowserComponent<?>>();
        mozSyncExec(new Runnable() {

            public void run() {
                wins.clear();
                wins.addAll(findWindowsByNodeText(nodeText));
            }
        });

        int waited = 0;
        while (!wins.isEmpty() && waited < waitMillis) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                log.error("wait interrupted", e); //$NON-NLS-1$
            }
            waited += 300;

            mozSyncExec(new Runnable() {

                public void run() {
                    wins.clear();
                    wins.addAll(findWindowsByNodeText(nodeText));
                }
            });
        }

        return wins.isEmpty() ? null : wins.get(0);
    }

    public static void sleep(int waitMillis) {
        //can't wait on mozilla thread
        assert !isMozillaThread();
        try {
            Thread.sleep(waitMillis);
        } catch (InterruptedException e) {
            log.error("wait interrupted", e); //$NON-NLS-1$
        }
    }

    public static List<JBrowserComponent<?>> findWindowsByTitle(String winTitle) {
        assert isMozillaThread();

        List<JBrowserComponent<?>> wins = new LinkedList<JBrowserComponent<?>>();
        nsIWindowWatcher winWatcher = getService("@mozilla.org/embedcomp/window-watcher;1", nsIWindowWatcher.class); //$NON-NLS-1$
        nsISimpleEnumerator en = winWatcher.getWindowEnumerator();
        while (en.hasMoreElements()) {
            nsIDOMWindow domWin = qi(en.getNext(), nsIDOMWindow.class);
            if (domWin == null) {
                continue;
            }

            String title = null;
            nsIDOMHTMLDocument htmlDoc = qi(domWin.getDocument(), nsIDOMHTMLDocument.class);
            if (htmlDoc != null) {
                title = htmlDoc.getTitle();
            } else {
                nsIDOMNSDocument nsDoc = qi(domWin.getDocument(), nsIDOMNSDocument.class);
                if (nsDoc != null) {
                    title = nsDoc.getTitle();
                }
            }

            if (title != null && winTitle.equals(title)) {
                JBrowserComponent<?> win = findWindow(domWin);
                if (win != null) {
                    wins.add(win);
                }
            }
        }

        return wins;
    }

    public static List<JBrowserComponent<?>> findWindowsByNodeText(String nodeText) {
        assert isMozillaThread();

        List<JBrowserComponent<?>> wins = new LinkedList<JBrowserComponent<?>>();
        nsIWindowWatcher winWatcher = getService("@mozilla.org/embedcomp/window-watcher;1", nsIWindowWatcher.class); //$NON-NLS-1$
        nsISimpleEnumerator en = winWatcher.getWindowEnumerator();
        while (en.hasMoreElements()) {
            nsIDOMWindow domWin = qi(en.getNext(), nsIDOMWindow.class);
            if (domWin == null) {
                continue;
            }

            nsIDOMDocument doc = domWin.getDocument();
            nsIDOMXPathEvaluator eval = qi(doc, nsIDOMXPathEvaluator.class);
            nsIDOMXPathResult ret = qi(eval.evaluate("count(//*[text()='" + nodeText + "'])"/*[text()=='"+nodeText+"'"*/, doc, null, nsIDOMXPathResult.ANY_TYPE, null), nsIDOMXPathResult.class); //$NON-NLS-1$ //$NON-NLS-2$
            double count = ret.getNumberValue();

            if (count > 0d) {
                JBrowserComponent<?> win = findWindow(domWin);
                if (win != null) {
                    wins.add(win);
                }
            }
        }

        return wins;
    }

    public static JBrowserComponent<?> findWindowByName(String winName) {
        assert isMozillaThread();

        nsIWindowWatcher winWatcher = getService("@mozilla.org/embedcomp/window-watcher;1", nsIWindowWatcher.class); //$NON-NLS-1$
        nsIDOMWindow domWin = winWatcher.getWindowByName(winName, null);
        if (domWin == null) {
            return null;
        }
        return findWindow(domWin);
    }

    public static JBrowserComponent<?> findWindow(nsIDOMWindow domWin) {
        assert isMozillaThread();

        nsIWindowWatcher winWatcher = getService("@mozilla.org/embedcomp/window-watcher;1", nsIWindowWatcher.class); //$NON-NLS-1$
        nsIWebBrowserChrome chromeWin = winWatcher.getChromeForWindow(domWin);
        if (chromeWin == null) {
            return null;
        }
        return findWindow(chromeWin);
    }

    public static JBrowserComponent<?> findWindow(nsIWebBrowserChrome chromeWin) {
        assert isMozillaThread();

        if (chromeWin instanceof JBrowserComponent<?>) {
            return (JBrowserComponent<?>) chromeWin;
        }

        return null;
    }

    public static void dumpWindows() {
        assert isMozillaThread();

        List<String> winTitles = new LinkedList<String>();
        nsIWindowWatcher winWatcher = getService("@mozilla.org/embedcomp/window-watcher;1", nsIWindowWatcher.class); //$NON-NLS-1$
        nsISimpleEnumerator en = winWatcher.getWindowEnumerator();
        while (en.hasMoreElements()) {
            nsIDOMWindow domWin = qi(en.getNext(), nsIDOMWindow.class);
            if (domWin == null) {
                continue;
            }

            String title = null;
            nsIDOMHTMLDocument htmlDoc = qi(domWin.getDocument(), nsIDOMHTMLDocument.class);
            if (htmlDoc != null) {
                title = htmlDoc.getTitle();
            } else {
                nsIDOMNSDocument nsDoc = qi(domWin.getDocument(), nsIDOMNSDocument.class);
                if (nsDoc != null) {
                    title = nsDoc.getTitle();
                }
            }

            winTitles.add(title != null ? title : "<none>"); //$NON-NLS-1$
        }

        StringBuffer sb = new StringBuffer();
        for (String s : winTitles) {
            sb.append(", "); //$NON-NLS-1$
            sb.append(s);
        }
        log.debug(sb.toString());
    }

    public static List<JBrowserComponent<?>> getOpennedWindows() {
        assert isMozillaThread();

        List<JBrowserComponent<?>> wins = new LinkedList<JBrowserComponent<?>>();

        nsIWindowWatcher winWatcher = getService("@mozilla.org/embedcomp/window-watcher;1", nsIWindowWatcher.class); //$NON-NLS-1$
        nsISimpleEnumerator en = winWatcher.getWindowEnumerator();
        while (en.hasMoreElements()) {
            nsIDOMWindow domWin = qi(en.getNext(), nsIDOMWindow.class);
            if (domWin == null) {
                continue;
            }

            JBrowserComponent<?> win = findWindow(domWin);
            wins.add(win);
        }

        return wins;
    }

//    public static void processPending() {
//        processPending(0);
//    }
//    public static void processPending(int millis) {
//        long start = System.currentTimeMillis();
//        nsIEventQueueService eqs = getService("@mozilla.org/event-queue-service;1", nsIEventQueueService.class);
//        nsIEventQueue currentThreadQ = eqs.getSpecialEventQueue(nsIEventQueueService.CURRENT_THREAD_EVENT_QUEUE);
//        //process events until we're finished.
//        while (true) {
//            if (currentThreadQ.pendingEvents())
//                currentThreadQ.processPendingEvents();
//
//            if (System.currentTimeMillis()-start>=millis) {
//                break;
//            }
//        }
//    }
    public static Object executeJavascript(final JBrowserComponent<?> component, final String script) {
        final Object[] ret = new Object[1];
        mozSyncExec(new Runnable() {

            public void run() {
                if (!component.isBrowserExisist()) {
                    return;
                }
                nsIWebNavigation nav = qi(component.getWebBrowser(), nsIWebNavigation.class);
                nav.loadURI("javascript:void " + script, nsIWebNavigation.LOAD_FLAGS_NONE, null, null, null); //$NON-NLS-1$
            /*better implementation with return values
                ::Evaluate(nsIDOMWindow* aWindow, const nsAString& aCode, nsIVariant** _retval)
                {
                nsCOMPtr<nsIScriptGlobalObject> global(do_QueryInterface(aWindow));
                if (!global)
                return NS_ERROR_UNEXPECTED;

                nsIScriptContext* context = global->GetContext();
                if (!context)
                return NS_ERROR_UNEXPECTED;

                nsCOMPtr<nsIXPConnect> xpc(do_GetService(kXPCCID));
                if (!xpc)
                return NS_ERROR_UNEXPECTED;

                JSContext* cx = NS_STATIC_CAST(JSContext*, context->GetNativeContext());
                NS_ASSERTION(cx, "no context?");

                jsval rval = JSVAL_VOID;
                JSAutoTempValueRooter tvr(cx, 1, &rval);

                PRBool isUndefined;
                nsresult rv = context->EvaluateStringWithValue(aCode, nsnull, nsnull,
                nsnull, 0, nsnull, &rval,
                &isUndefined);
                if (NS_FAILED(rv))
                return rv;

                if (isUndefined || JSVAL_IS_NULL(rval)) {
                 *_retval = nsnull;
                } else {
                rv = xpc->JSToVariant(cx, rval, _retval);
                }

                return rv;
                }

                nsIWebBrowser brow = win.getChromeAdapter().getWebBrowser();
                nsIDOMWindow domWin = brow.getContentDOMWindow();
                nsIVariant variant = jseval(domWin, script);

                switch (v.getDataType();) {
                case VTYPE_INT8: return var.getAsInt8(v);
                case VTYPE_INT16: return var.getAsInt16(v);
                case VTYPE_INT32: return var.getAsInt32(v);
                case VTYPE_INT64: return var.getAsInt64(v);
                case VTYPE_UINT8: return var.getAsUint8(v);
                case VTYPE_UINT16: return var.getAsUint16(v);
                case VTYPE_UINT32: return var.getAsUint32(v);
                case VTYPE_UINT64: return var.getAsUint64(v);
                case VTYPE_FLOAT: return var.getAsFloat(v);
                case VTYPE_DOUBLE: return var.getAsDouble(v);
                case VTYPE_BOOL: return var.getAsBool(v);
                case VTYPE_CHAR: return var.getAsChar(v);
                case VTYPE_WCHAR: return var.getAsWChar(v);
                case VTYPE_VOID: return null;
                case VTYPE_ID: throw new MozillaRuntimeException("unsupported javascript return type");
                case VTYPE_DOMSTRING: return var.getAsDOMString(v);
                case VTYPE_CHAR_STR: return var.getAsAUTF8String(v); //conversion
                case VTYPE_WCHAR_STR: return var.getAsWString(v);
                case VTYPE_INTERFACE: throw new MozillaRuntimeException("unsupported javascript return type");
                case VTYPE_INTERFACE_IS: throw new MozillaRuntimeException("unsupported javascript return type");
                case VTYPE_ARRAY: throw new MozillaRuntimeException("unsupported javascript return type");
                case VTYPE_STRING_SIZE_IS: return var.getAsStringWithSize(v);
                case VTYPE_WSTRING_SIZE_IS: return var.getAsWStringWithSize(v);
                case VTYPE_UTF8STRING: return var.getAsAUTF8String(v);
                case VTYPE_CSTRING: return var.getAsACString(v);
                case VTYPE_ASTRING: return var.getAsAString(v);
                case VTYPE_EMPTY_ARRAY: return new Object[0];
                case VTYPE_EMPTY: return null;
                default: return null;
                }

                 */
            }
        });
        return ret[0];
    }

    /**
     * Mozilla renders each element as a sequence of frames.
     * (Do not confuse with the HTML <frame> element)
     *
     * <p>This function returns coordinates of the frames
     * that render the given element.
     *
     * @param e element
     * @return coordinates of element's frames
     */
    public static Rectangle[] getElementRects(final Element e) {
        return mozSyncExecQuiet(new Callable<Rectangle[]>() {

            public Rectangle[] call() {
                nsIDOMElement el = ((ElementImpl) e).getInstance();
                nsIDOMNSElement nsel = qi(el, nsIDOMNSElement.class);

                nsIDOMDocument doc = el.getOwnerDocument();
                nsIDOMNSDocument nsdoc = qi(doc, nsIDOMNSDocument.class);

                nsIBoxObject box = nsdoc.getBoxObjectFor(el);
                int x0 = box.getX();
                int y0 = box.getY();

                int scrollx = 0, scrolly = 0;

                nsIDOMClientRectList crl = nsel.getClientRects();
                int len = (int) crl.getLength();
                Rectangle[] rs = new Rectangle[len];
                for (int i = 0; i < len; i++) {
                    nsIDOMClientRect dr = crl.item(i);
                    int x = (int) dr.getLeft();
                    int y = (int) dr.getTop();
                    int w = (int) (dr.getRight() - dr.getLeft());
                    int h = (int) (dr.getBottom() - dr.getTop());

                    if (i == 0) {
                        scrollx = x0 - x;
                        scrolly = y0 - y;
                    }

                    Rectangle r = new Rectangle(x + scrollx, y + scrolly, w, h);
                    rs[i] = r;
                }
                return rs;
            }
        });
    }

    /**
     * Returns element with the x,y coordinates
     * from the given document or some of its
     * sub-documents.
     *
     * @param doc
     * @param x x coordinate
     * @param y y coordinate
     * @return element
     */
    public static Element getElementFromPoint(final Document doc,
            final int x, final int y) {
        return mozSyncExecQuiet(new Callable<Element>() {

            public Element call() {

                nsIDOMDocument currDoc = ((DocumentImpl) doc).getInstance();
                nsIDOMElement currEl = null;
                while (currDoc != null) {
                    nsIDOMNSDocument nsdoc = qi(currDoc, nsIDOMNSDocument.class);

                    nsIDOMElement el = nsdoc.elementFromPoint(x, y);
                    if (el == null) {
                        break; //not from nsdoc
                    }
                    currEl = el;

                    nsIDOMHTMLFrameElement fel = qi(el, nsIDOMHTMLFrameElement.class);
                    if (fel != null) {
                        currDoc = fel.getContentDocument();
                        continue;
                    }
                    nsIDOMHTMLIFrameElement iel = qi(el, nsIDOMHTMLIFrameElement.class);
                    if (iel != null) {
                        currDoc = iel.getContentDocument();
                        continue;
                    }
                    //not an (i)frame element
                    currDoc = null;
                }

                return (Element) NodeFactory.getNodeInstance(currEl);
            }
        });
    }
}
