/**
 * No restrictions for this source.
 *
 * Author: CA>>>
 * Site: atomation.ru
 * Mail: Sashusik_EntXXI@Mail.ru
 */
package ru.atomation.jbrowser.impl;

import static org.mozilla.browser.MozillaExecutor.mozPostponableSyncExec;
import static org.mozilla.browser.MozillaExecutor.mozSyncExec;
import static org.mozilla.browser.XPCOMUtils.getService;

import org.mozilla.browser.MozillaExecutor;
import org.mozilla.browser.XPCOMUtils;
import org.mozilla.interfaces.nsICache;
import org.mozilla.interfaces.nsICacheService;
import org.mozilla.interfaces.nsICookieManager;
import org.mozilla.interfaces.nsIDOMWindowInternal;
import org.mozilla.interfaces.nsIDocShell;
import org.mozilla.interfaces.nsIInterfaceRequestor;
import org.mozilla.interfaces.nsIPrefBranch;
import org.mozilla.interfaces.nsISimpleEnumerator;
import org.mozilla.interfaces.nsIWebBrowser;
import org.mozilla.interfaces.nsIWebBrowserChrome;
import org.mozilla.interfaces.nsIWindowWatcher;

import ru.atomation.jbrowser.interfaces.BrowserConfig;
import ru.atomation.jbrowser.interfaces.tasks.DocShellApplyTask;

/**
 * Standard implemenation for {@link BrowserConfig}
 * @author caiiiycuk
 */
public class JBrowserConfig implements BrowserConfig {

    private boolean enabledJavascript;
    private boolean enabledImages;

    JBrowserConfig() {
        enabledJavascript = true;
        enabledImages = true;

        //read proxy setting from java properties
        String httpHost = System.getProperty("proxy.http.host", ""); //$NON-NLS-1$ //$NON-NLS-2$
        int httpPort = parseInt(System.getProperty("proxy.http.port", "")); //$NON-NLS-1$ //$NON-NLS-2$
        String sslHost = System.getProperty("proxy.https.host", ""); //$NON-NLS-1$ //$NON-NLS-2$
        int sslPort = parseInt(System.getProperty("proxy.https.port", "")); //$NON-NLS-1$ //$NON-NLS-2$
        String ftpHost = System.getProperty("proxy.https.host", ""); //$NON-NLS-1$ //$NON-NLS-2$
        int ftpPort = parseInt(System.getProperty("proxy.https.port", "")); //$NON-NLS-1$ //$NON-NLS-2$
        String socksHost = System.getProperty("proxy.https.host", ""); //$NON-NLS-1$ //$NON-NLS-2$
        int socksPort = parseInt(System.getProperty("proxy.https.port", "")); //$NON-NLS-1$ //$NON-NLS-2$
        String noProxyFor = System.getProperty("proxy.bypass.list", ""); //$NON-NLS-1$ //$NON-NLS-2$
        setManualProxy(httpHost, httpPort,
                sslHost, sslPort,
                ftpHost, ftpPort,
                socksHost, socksPort,
                noProxyFor);
    }

    @Override
    public void enableImages() {
        mozPostponableSyncExec(new Runnable() {

            @Override
            public void run() {
                /*
                normally should be sufficient to set a preference,
                but content-blocker extension is not part of xulrunner build

                //http://kb.mozillazine.org/Permissions.default.image
                // 1-Accept, 2-Deny, 3-dontAcceptForeign
                nsIPrefBranch pref = getService("@mozilla.org/preferences-service;1", nsIPrefBranch.class);
                pref.setIntPref("permissions.default.image", 1);
                 */

                enabledImages = true;
                //enable images in existing windows
                applyForAllWindows(new DocShellApplyTask() {

                    @Override
                    public void apply(nsIDocShell ds) {
                        ds.setAllowImages(true);
                    }
                });
            }
        });
    }

    @Override
    public void disableImages() {
        mozPostponableSyncExec(new Runnable() {

            @Override
            public void run() {
                /*
                normally should be sufficient to set a preference,
                but content-blocker extension is not part of xulrunner build

                //http://kb.mozillazine.org/Permissions.default.image
                // 1-Accept, 2-Deny, 3-dontAcceptForeign
                nsIPrefBranch pref = getService("@mozilla.org/preferences-service;1", nsIPrefBranch.class);
                pref.setIntPref("permissions.default.image", 2);
                 */

                enabledImages = false;

                //disable images in existing windows
                applyForAllWindows(new DocShellApplyTask() {

                    @Override
                    public void apply(nsIDocShell ds) {
                        ds.setAllowImages(false);
                    }
                });
            }
        });
    }

    @Override
    public void enableImages(final JBrowserComponent<?> jBrowserComponent) {
        mozSyncExec(new Runnable() {

            @Override
            public void run() {
                if (!(jBrowserComponent instanceof nsIWebBrowserChrome)) {
                    throw new IllegalArgumentException("This jbrowser component does not support native configuration");
                }

                nsIWebBrowser webBrowser = ((nsIWebBrowserChrome) jBrowserComponent).getWebBrowser();
                nsIInterfaceRequestor ir = XPCOMUtils.qi(webBrowser, nsIInterfaceRequestor.class);
                nsIDocShell docShell = (nsIDocShell) ir.getInterface(nsIDocShell.NS_IDOCSHELL_IID);
                docShell.setAllowImages(false);
            }
        });
    }

    @Override
    public void disableImages(final JBrowserComponent<?> jBrowserComponent) {
        mozSyncExec(new Runnable() {

            @Override
            public void run() {
                if (!(jBrowserComponent instanceof nsIWebBrowserChrome)) {
                    throw new IllegalArgumentException("This jbrowser component does not support native configuration");
                }


                nsIWebBrowser webBrowser = ((nsIWebBrowserChrome) jBrowserComponent).getWebBrowser();
                nsIInterfaceRequestor ir = XPCOMUtils.qi(webBrowser, nsIInterfaceRequestor.class);
                nsIDocShell docShell = (nsIDocShell) ir.getInterface(nsIDocShell.NS_IDOCSHELL_IID);
                docShell.setAllowImages(false);
            }
        });
    }

    @Override
    public boolean isEnabledImages() {
        return enabledImages;
    }

    @Override
    public void enableJavascript() {
        mozPostponableSyncExec(new Runnable() {

            @Override
            public void run() {
                enabledJavascript = true;

                //enable images in existing windows
                applyForAllWindows(new DocShellApplyTask() {

                    @Override
                    public void apply(nsIDocShell ds) {
                        ds.setAllowJavascript(true);
                    }
                });
            }
        });
    }

    @Override
    public void disableJavascript() {
        mozPostponableSyncExec(new Runnable() {

            @Override
            public void run() {
                enabledJavascript = false;

                //disable images in existing windows
                applyForAllWindows(new DocShellApplyTask() {

                    @Override
                    public void apply(nsIDocShell ds) {
                        ds.setAllowJavascript(false);
                    }
                });
            }
        });
    }

    @Override
    public void enableJavascript(final JBrowserComponent<?> jBrowserComponent) {
        mozSyncExec(new Runnable() {

            @Override
            public void run() {
                if (!(jBrowserComponent instanceof nsIWebBrowserChrome)) {
                    throw new IllegalArgumentException("This jbrowser component does not support native configuration");
                }


                nsIWebBrowser webBrowser = ((nsIWebBrowserChrome) jBrowserComponent).getWebBrowser();
                nsIInterfaceRequestor ir = XPCOMUtils.qi(webBrowser, nsIInterfaceRequestor.class);
                nsIDocShell docShell = (nsIDocShell) ir.getInterface(nsIDocShell.NS_IDOCSHELL_IID);
                docShell.setAllowJavascript(false);
            }
        });
    }

    @Override
    public void disableJavascript(final JBrowserComponent<?> jBrowserComponent) {
        mozSyncExec(new Runnable() {

            @Override
            public void run() {
                if (!(jBrowserComponent instanceof nsIWebBrowserChrome)) {
                    throw new IllegalArgumentException("This jbrowser component does not support native configuration");
                }


                nsIWebBrowser webBrowser = ((nsIWebBrowserChrome) jBrowserComponent).getWebBrowser();
                nsIInterfaceRequestor ir = XPCOMUtils.qi(webBrowser, nsIInterfaceRequestor.class);
                nsIDocShell docShell = (nsIDocShell) ir.getInterface(nsIDocShell.NS_IDOCSHELL_IID);
                docShell.setAllowJavascript(false);
            }
        });
    }

    @Override
    public boolean isEnabledJavascript() {
        return enabledJavascript;
    }

    @Override
    public void setManualProxy(
            final String httpHost,
            final int httpPort,
            final String sslHost,
            final int sslPort,
            final String ftpHost,
            final int ftpPort,
            final String socksHost,
            final int socksPort,
            final String noProxyFor) {
        mozPostponableSyncExec(new Runnable() {

            @Override
            public void run() {
                nsIPrefBranch pref = getService("@mozilla.org/preferences-service;1", nsIPrefBranch.class); //$NON-NLS-1$

                //switch to manual configuration
                pref.setIntPref("network.proxy.type", 1); //$NON-NLS-1$

                boolean validConfig = false;
                //http proxy
                if (httpHost != null && httpHost.length() > 0 && httpPort > 0) {
                    pref.setCharPref("network.proxy.http", httpHost); //$NON-NLS-1$
                    pref.setIntPref("network.proxy.http_port", httpPort); //$NON-NLS-1$
                    validConfig = true;
                } else {
                    pref.setCharPref("network.proxy.http", ""); //$NON-NLS-1$ //$NON-NLS-2$
                    pref.setIntPref("network.proxy.http_port", 0); //$NON-NLS-1$
                }

                //ssl proxy
                if (sslHost != null && sslHost.length() > 0 && sslPort > 0) {
                    pref.setCharPref("network.proxy.ssl", sslHost); //$NON-NLS-1$
                    pref.setIntPref("network.proxy.ssl_port", sslPort); //$NON-NLS-1$
                    validConfig = true;
                } else {
                    pref.setCharPref("network.proxy.ssl", ""); //$NON-NLS-1$ //$NON-NLS-2$
                    pref.setIntPref("network.proxy.ssl_port", 0); //$NON-NLS-1$
                }

                //ftp proxy
                if (ftpHost != null && ftpHost.length() > 0 && ftpPort > 0) {
                    pref.setCharPref("network.proxy.ftp", ftpHost); //$NON-NLS-1$
                    pref.setIntPref("network.proxy.ftp_port", ftpPort); //$NON-NLS-1$
                    validConfig = true;
                } else {
                    pref.setCharPref("network.proxy.ftp", ""); //$NON-NLS-1$ //$NON-NLS-2$
                    pref.setIntPref("network.proxy.ftp_port", 0); //$NON-NLS-1$
                }

                //socks proxy
                if (socksHost != null && socksHost.length() > 0 && socksPort > 0) {
                    pref.setCharPref("network.proxy.socks", socksHost); //$NON-NLS-1$
                    pref.setIntPref("network.proxy.socks_port", socksPort); //$NON-NLS-1$
                    validConfig = true;
                } else {
                    pref.setCharPref("network.proxy.socks", ""); //$NON-NLS-1$ //$NON-NLS-2$
                    pref.setIntPref("network.proxy.socks_port", 0); //$NON-NLS-1$
                }

                //no proxy for
                if (noProxyFor != null && noProxyFor.length() > 0) {
                    pref.setCharPref("network.proxy.no_proxies_on", noProxyFor); //$NON-NLS-1$
                } else {
                    pref.setCharPref("network.proxy.no_proxies_on", "localhost, 127.0.0.1"); //$NON-NLS-1$ //$NON-NLS-2$
                }

                if (!validConfig) {
                    //reset proxy type
                    pref.setIntPref("network.proxy.type", 0); //$NON-NLS-1$
                }
            }
        });
    }

    @Override
    public void setAutomaticProxy(final String configURL) {
        mozPostponableSyncExec(new Runnable() {

            @Override
            public void run() {
                nsIPrefBranch pref = getService("@mozilla.org/preferences-service;1", nsIPrefBranch.class); //$NON-NLS-1$

                //switch to automatic configuration
                pref.setIntPref("network.proxy.type", 2); //$NON-NLS-1$

                boolean validConfig = false;
                if (configURL != null && configURL.length() > 0) {
                    pref.setCharPref("network.proxy.autoconfig_url", configURL); //$NON-NLS-1$
                    validConfig = true;
                } else {
                    pref.setCharPref("network.proxy.autoconfig_url", ""); //$NON-NLS-1$ //$NON-NLS-2$
                }

                if (!validConfig) {
                    //reset proxy type
                    pref.setIntPref("network.proxy.type", 0); //$NON-NLS-1$
                }
            }
        });
    }

    @Override
    public void disableProxy() {
        mozPostponableSyncExec(new Runnable() {

            @Override
            public void run() {
                nsIPrefBranch pref = getService("@mozilla.org/preferences-service;1", nsIPrefBranch.class); //$NON-NLS-1$

                //switch to direct connection
                pref.setIntPref("network.proxy.type", 0); //$NON-NLS-1$
            }
        });
    }

    @Override
    public void cleanCache() {
        mozPostponableSyncExec(new Runnable() {

            @Override
            public void run() {
                nsICacheService cache = getService("@mozilla.org/network/cache-service;1", nsICacheService.class); //$NON-NLS-1$
                cache.evictEntries(nsICache.STORE_ANYWHERE);
            }
        });
    }

    @Override
    public void cleanCookies() {
        mozPostponableSyncExec(new Runnable() {

            @Override
            public void run() {
                nsICookieManager cookieManager = getService("@mozilla.org/cookiemanager;1", nsICookieManager.class); //$NON-NLS-1$
                cookieManager.removeAll();
            }
        });
    }

    private void applyForAllWindows(DocShellApplyTask t) {
        assert MozillaExecutor.isMozillaThread();
        nsIWindowWatcher ww = XPCOMUtils.getService("@mozilla.org/embedcomp/window-watcher;1", nsIWindowWatcher.class); //$NON-NLS-1$

        nsISimpleEnumerator winEn = ww.getWindowEnumerator();
        while (winEn.hasMoreElements()) {
            nsIDOMWindowInternal domWin = XPCOMUtils.qi(winEn.getNext(), nsIDOMWindowInternal.class);
            nsIWebBrowserChrome chrome = ww.getChromeForWindow(domWin);
            nsIWebBrowser webBrowser = chrome.getWebBrowser();
            nsIInterfaceRequestor ir = XPCOMUtils.qi(webBrowser, nsIInterfaceRequestor.class);
            nsIDocShell docShell = (nsIDocShell) ir.getInterface(nsIDocShell.NS_IDOCSHELL_IID);
            t.apply(docShell);
        }
    }

    private int parseInt(String s) {
        if (s.length() == 0) {
            return -1;
        }
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return -1;
        }
    }
}
