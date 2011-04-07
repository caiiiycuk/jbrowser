package org.mozilla.browser.impl;

import static org.mozilla.browser.MozillaExecutor.mozSyncExec;
import static org.mozilla.browser.XPCOMUtils.getService;
import static org.mozilla.browser.XPCOMUtils.qi;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.mozilla.browser.MozillaExecutor;
import org.mozilla.interfaces.nsIHttpChannel;
import org.mozilla.interfaces.nsIRequest;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsIThread;
import org.mozilla.interfaces.nsIThreadManager;
import org.mozilla.interfaces.nsIURI;
import org.mozilla.interfaces.nsIWebProgress;
import org.mozilla.interfaces.nsIWebProgressListener;
import org.mozilla.xpcom.Mozilla;
import org.mozilla.xpcom.XPCOMException;

import ru.atomation.jbrowser.impl.JBrowserComponent;

public abstract class BlockingURLLoader {

    protected final JBrowserComponent<?> component;
    private final Semaphore sem;

    private boolean loadFailed = false;

    private long loadTimeout = 30000;

    public BlockingURLLoader(JBrowserComponent<?> component)
    {
        this.component = component;
        this.sem = new Semaphore(2);
    }

    public boolean getLoadFailed() {
        return loadFailed;
    }

    private class GeckoAdapter implements nsIWebProgressListener {

        private int loadsNum = 0;

        public void started() {
            //log.debug(this, "load started");
            sem.release();
            loadsNum++;
        }

        public void stopped(nsIRequest aRequest,
                            long aStatus)
        {
            //log.debug(this, "load stopped");
            if (loadsNum>0) { //sometimes we get a load end without any load start
                if (aStatus!=0) {
                    loadFailed = true;
                } else {
                    nsIHttpChannel hc = qi(aRequest, nsIHttpChannel.class);
                    if (hc!=null) {
                        long s = hc.getResponseStatus();
                        if (s!=200) {
                            loadFailed = true;
                        }
                    }
                    /*
                    final long status;
                    if (qi(aRequest, nsIDataChannel.class)!=null) {
                        nsIDataChannel dc = qi(aRequest, nsIDataChannel.class);
                        status = dc.getStatus();
                    } else if (qi(aRequest, nsIFileChannel.class)!=null) {
                        nsIFileChannel fc = qi(aRequest, nsIFileChannel.class);
                        status = fc.getStatus();
                    } else if (qi(aRequest, nsIFTPChannel.class)!=null) {
                        nsIFTPChannel fc = qi(aRequest, nsIFTPChannel.class);
                        status = fc.getStatus();
                    } else if (qi(aRequest, nsIHttpChannel.class)!=null) {
                        nsIHttpChannel hc = qi(aRequest, nsIHttpChannel.class);
                        status = hc.getResponseStatus();
                    } else if (qi(aRequest, nsIInputStreamChannel.class)!=null) {
                        nsIInputStreamChannel ic = qi(aRequest, nsIInputStreamChannel.class);
                        status = ic.getStatus();
                    } else if (qi(aRequest, nsIJARChannel.class)!=null) {
                        nsIJARChannel jc = qi(aRequest, nsIJARChannel.class);
                        status = jc.getStatus();
                    } else {
                        status = 200; //HTTP OK
                    }
                     */
                }

                sem.release();
                loadsNum--;
            }
        }
        public void onStateChange(nsIWebProgress aWebProgress,
                                  nsIRequest aRequest,
                                  long aStateFlags,
                                  long aStatus)
        {
            if ((aStateFlags & nsIWebProgressListener.STATE_IS_NETWORK)!=0 &&
                (aStateFlags & nsIWebProgressListener.STATE_START)!=0) {
                started();
            }

            if ((aStateFlags & nsIWebProgressListener.STATE_IS_NETWORK)!=0 &&
                (aStateFlags & nsIWebProgressListener.STATE_STOP)!=0) {
                stopped(aRequest, aStatus);
            }
        }

        public void onLocationChange(nsIWebProgress arg0, nsIRequest arg1, nsIURI arg2) {}
        public void onProgressChange(nsIWebProgress arg0, nsIRequest arg1, int arg2, int arg3, int arg4, int arg5) {}
        public void onSecurityChange(nsIWebProgress arg0, nsIRequest arg1, long arg2) {}
        public void onStatusChange(nsIWebProgress arg0, nsIRequest arg1, long arg2, String arg3) {}


        public nsISupports queryInterface(String iid) {
            return Mozilla.queryInterface(this, iid);
        }
    }

    public void load() {
        final GeckoAdapter ga = new GeckoAdapter();
        mozSyncExec(new Runnable() {
            public void run() {
                if (!component.isBrowserExisist()) {
                    return;
                }

                component.getWebBrowser().
                addWebBrowserListener(ga, nsIWebProgressListener.NS_IWEBPROGRESSLISTENER_IID);
            }
        });

        sem.acquireUninterruptibly(2);

        onLoadStarted();
        mozSyncExec(new Runnable() {
            public void run() {
                try {
                    if (triggerURLLoading()) {
                        loadFailed = true;
                    }
                } catch (XPCOMException e) {
                    loadFailed = true;
                }
            }
        });

        if (!loadFailed) {
            if (!MozillaExecutor.isMozillaThread()) {
                //not on ui thread
                try {
                    if (!sem.tryAcquire(2, loadTimeout, TimeUnit.MILLISECONDS)) {
                        //log.debug("load timeout");
                    }
                } catch (InterruptedException e) {
                    //log.error(e);
                }
            } else {
                //run the mozilla ui thread while the
                //thread waiting for loading-end finishes
                long start = System.currentTimeMillis();
                nsIThreadManager tm = getService("@mozilla.org/thread-manager;1", nsIThreadManager.class); //$NON-NLS-1$
                nsIThread mt = tm.getMainThread();

                //process events until we're finished.
                while (!sem.tryAcquire(2))
                {
                    if (mt.hasPendingEvents()) mt.processNextEvent(false);
                    if (System.currentTimeMillis()-start>=loadTimeout) {
                        //log("load timeout");
                        break;
                    }
                }
            }
        }
        onLoadEnded();
        mozSyncExec(new Runnable() {
            public void run() {
                if (!component.isBrowserExisist()) {
                    return;
                }

                component.getWebBrowser().
                removeWebBrowserListener(ga, nsIWebProgressListener.NS_IWEBPROGRESSLISTENER_IID);
            }
        });
    }

    public void setLoadTimeout(long millis) {
        this.loadTimeout = millis;
    }

    public long getLoadTimeut() {
        return this.loadTimeout;
    }

    /**
     * start url loading, return true if failed
     */
    public abstract boolean triggerURLLoading();
    public abstract void onLoadStarted();
    public abstract void onLoadEnded();

}