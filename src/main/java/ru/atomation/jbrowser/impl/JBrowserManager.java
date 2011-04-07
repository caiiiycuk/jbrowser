package ru.atomation.jbrowser.impl;

import static org.mozilla.browser.MozillaExecutor.mozInit;
import static org.mozilla.browser.common.Platform.Linux;
import static org.mozilla.browser.common.Platform.Solaris;
import static org.mozilla.browser.common.Platform.platform;
import static org.mozilla.browser.impl.jna.LibC.libc;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.browser.MozillaException;
import org.mozilla.browser.MozillaExecutor;
import org.mozilla.browser.MozillaRuntimeException;
import org.mozilla.browser.RunnableEx;
import org.mozilla.browser.XPCOMUtils;
import org.mozilla.browser.mt;
import org.mozilla.browser.common.Platform;
import org.mozilla.browser.common.XULRunnerFinder;
import org.mozilla.browser.impl.LocationProvider;
import org.mozilla.browser.impl.XREAppData;
import org.mozilla.dom.ThreadProxy;
import org.mozilla.interfaces.nsIExtensionManager;
import org.mozilla.interfaces.nsIPrefBranch;
import org.mozilla.interfaces.nsIWindowWatcher;
import org.mozilla.xpcom.Mozilla;

import ru.atomation.jbrowser.interfaces.BrowserConfig;
import ru.atomation.jbrowser.interfaces.BrowserManager;
import ru.atomation.jbrowser.interfaces.BrowserWindowCreator;
import ru.atomation.jbrowser.interfaces.ComponentFacotry;

/**
 * JBrowser manager
 */
public class JBrowserManager implements BrowserManager {

    protected static Log log = LogFactory.getLog(JBrowserManager.class);
    protected static JBrowserManager instance;
    protected JBrowserInitializationStatus status = JBrowserInitializationStatus.NONE;
    protected Throwable error = null;
    protected BrowserWindowCreator windowCreator;
    protected File xulrunnerPath;
    protected File profilePath;
    protected BrowserConfig browserConfig;

    JBrowserManager(JBrowserBuilder browserBuilder) {
        if (instance != null) {
            throw new IllegalStateException("BrowserManager can be only one");
        }

        instance = this;
        browserBuilder.getBrowserFactory().setBrowserManager(this);
        this.windowCreator = browserBuilder.getBrowserWindowCreator();
        this.windowCreator.setBrowserFactory(browserBuilder.getBrowserFactory());
        this.xulrunnerPath = browserBuilder.getXulRunnerPath();
        this.profilePath = browserBuilder.getProfilePath();
        initialize();
        this.browserConfig = new JBrowserConfig();
    }

    public static JBrowserManager getToolKit() {
        throw new UnsupportedOperationException("No more");
    }

    /**
     * Executes Mozilla (XULRunner) initialization.
     */
    protected void initialize() {
        if (status != JBrowserInitializationStatus.NONE) {
            return;
        }

        // set the all-permissions rights also for other classloaders,
        // for more details see
        // http://saloon.javaranch.com/cgi-bin/ubb/ultimatebb.cgi?ubb=get_topic&f=53&t=000106
        // this is needed when running under a secure classloader like the
        // webstart
        // environment.
        Policy.setPolicy(new Policy() {

            @Override
            public PermissionCollection getPermissions(CodeSource codesource) {
                Permissions perms = new Permissions();
                perms.add(new AllPermission());
                return (perms);
            }

            @Override
            public void refresh() {
            }
        });

        try {
            if (xulrunnerPath != null) {
                if (!XULRunnerFinder.isXULRunnerDir(xulrunnerPath)) {
                    // this isn't a valid home directory
                    log.error("invalid current xulrunner location " + xulrunnerPath.getAbsolutePath()); //$NON-NLS-1$
                    log.info("Continuing to search for other xulrunners"); //$NON-NLS-1$
                    xulrunnerPath = null;
                }
            }

            if (xulrunnerPath == null) {
                xulrunnerPath = XULRunnerFinder.findXULRunner();
            }

            if (xulrunnerPath == null) {
                throw new IOException(
                        mt.t("MozillaInitialization.Unable_to_resolve_XULRunner_home")); //$NON-NLS-1$
            }

            log.info("Using xul runner dir: " + xulrunnerPath.getAbsolutePath()); //$NON-NLS-1$
            final File foundXulrunnerDir = xulrunnerPath;

            // delete xpcom registry files for
            // safe upgrade to newer xulrunner version,
            // less confusions when adding own xpt components
            File componentsDir = new File(xulrunnerPath, "components"); //$NON-NLS-1$
            File compreg = new File(componentsDir, "compreg.dat"); //$NON-NLS-1$
            if (compreg.isFile()) {
                compreg.delete();
            }
            File xpti = new File(componentsDir, "xpti.dat"); //$NON-NLS-1$
            if (xpti.isFile()) {
                xpti.delete();
            }

            File nativeLibsDir = foundXulrunnerDir.getParentFile();
            if (Platform.platform == Platform.OSX && !new File(nativeLibsDir, "libcocoautils.jnilib").exists()) //$NON-NLS-1$
            {
                log.error("Unable to resolve location of MozSwing native libraries"); //$NON-NLS-1$
                log.info("Continuing assuming java.library.path is set"); //$NON-NLS-1$
                nativeLibsDir = null;
            }
            final File foundNativeLibsDir = nativeLibsDir;

            mozInit(foundXulrunnerDir, foundNativeLibsDir, new RunnableEx() {

                @Override
                public void run() throws Exception {

                    if (platform == Solaris || platform == Linux) {
                        // sighandlers are overridden only on XP_UNIX
                        int ret = libc.setenv(
                                "MOZ_DISABLE_SIG_HANDLER", "1", true); //$NON-NLS-1$ //$NON-NLS-2$
                        assert ret == 0;
                        // String val = libc.getenv("MOZ_DISABLE_SIG_HANDLER");
                        // assert val!=null && val.equals("1");
                    }

                    Mozilla moz = Mozilla.getInstance();
                    moz.initialize(foundXulrunnerDir);
                    LocationProvider locProvider = new LocationProvider(
                            foundXulrunnerDir, profilePath);
                    moz.initEmbedding(foundXulrunnerDir, foundXulrunnerDir,
                            locProvider, new XREAppData());

                    if (profilePath != null) {
                        moz.lockProfileDirectory(profilePath);
                        moz.notifyProfile();
                    }

                    nsIWindowWatcher winWatcher = XPCOMUtils.getService("@mozilla.org/embedcomp/window-watcher;1", nsIWindowWatcher.class); //$NON-NLS-1$
                    winWatcher.setWindowCreator(windowCreator);

                    // needed for open/save unknown content dialog,
                    // because otherwise code
                    nsIPrefBranch pref = XPCOMUtils.getService(
                            "@mozilla.org/preferences-service;1", nsIPrefBranch.class); //$NON-NLS-1$
                    // at nsHelperAppDlg.js:92 fails
                    // var autodownload =
                    // prefs.getBoolPref("browser.download.useDownloadDir");
                    pref.setBoolPref("browser.download.useDownloadDir", 0); //$NON-NLS-1$
                    // at nsDownloadManager.cpp:1272 fails
                    // rv = prefBranch->GetIntPref(NS_PREF_FOLDERLIST, &val);
                    // 0 = desktop
                    pref.setIntPref("browser.download.folderList", 0); //$NON-NLS-1$
                    // at nsDownloadManager.cpp:994 fails
                    // rv = pref->GetIntPref(PREF_BDM_QUITBEHAVIOR, &val);
                    // 2 = cancel downloads on quit
                    pref.setIntPref("browser.download.manager.quitBehavior", 2); //$NON-NLS-1$
                    // at nsDownloadManager.cpp:979 fails
                    // rv = pref->GetIntPref(PREF_BDM_RETENTION, &val);
                    // 1 = keep completed downloads
                    pref.setIntPref("browser.download.manager.retention", 1); //$NON-NLS-1$

                    // workaround known java plugin bug
                    // on win32 javaplugin runs in-process, therefore
                    // loading a webpage with a java applet inside
                    // a java-based mozilla embedding applications
                    // hangs the whole jvm
                    if (Platform.platform == Platform.Win32 || Platform.platform == Platform.OSX) {
                        pref.setBoolPref("security.enable_java", 0); //$NON-NLS-1$
                    }

                    // when page loading fails, display an error page,
                    // similar as firefox does
                    pref.setBoolPref("browser.xul.error_pages.enabled", 1); //$NON-NLS-1$

                    // disable various security warning dialogs
                    // such as when entering/leaving https site or
                    // submitting form
                    pref.setBoolPref("security.warn_entering_secure", 0); //$NON-NLS-1$
                    pref.setBoolPref("security.warn_entering_weak", 0); //$NON-NLS-1$
                    pref.setBoolPref("security.warn_leaving_secure", 0); //$NON-NLS-1$
                    pref.setBoolPref("security.warn_submit_insecure", 0); //$NON-NLS-1$
                    pref.setBoolPref("security.warn_viewing_mixed", 0); //$NON-NLS-1$

                    // nsIXULAppInfo appInfo =
                    // XPCOMUtils.getService("@mozilla.org/xre/app-info;1",
                    // nsIXULAppInfo.class);
                    // String ver = appInfo.getPlatformVersion();
                    // System.err.println("s="+ver);

                    nsIExtensionManager em = XPCOMUtils.getService(
                            "@mozilla.org/extensions/manager;1", nsIExtensionManager.class); //$NON-NLS-1$
                    em.start(null);

                    // configure mozdom4java
                    ThreadProxy.setSingleton(new ThreadProxy() {

                        @Override
                        public boolean isMozillaThread() {
                            return MozillaExecutor.isMozillaThread();
                        }

                        @Override
                        public void syncExec(Runnable task) {
                            MozillaExecutor.mozSyncExec(task);
                        }

                        @Override
                        public <V> V syncExec(Callable<V> task) {
                            try {
                                return MozillaExecutor.mozSyncExec(task);
                            } catch (MozillaException e) {
                                throw new MozillaRuntimeException(e);
                            }
                        }
                    });

                    status = JBrowserInitializationStatus.INITIALIZED;

                    // run possible pre-init tasks such
                    // as proxy settings, disable images
                    MozillaExecutor.runPostponedPreInitTasks();
                }
            });
        } catch (Exception e) {
            log.error("failed to initialize mozilla", e); //$NON-NLS-1$
            status = JBrowserInitializationStatus.FAILED;
            error = e;
        }
    }

    /**
     * Returns true, if mozilla was successfully initialized.
     *
     * @return true if mozilla was successfully initialized.
     */
    @Override
    public boolean isInitialized() {
        return status == JBrowserInitializationStatus.INITIALIZED;
    }

    /**
     * Returns exception that occurred during mozilla initialization or null.
     *
     * @return initialization exception
     */
    @Override
    public Throwable getError() {
        return error;
    }

    /**
     * Returns mozilla initialization status, one of NONE, INITIALIZED, FAILED.
     *
     * @return mozilla initialization status
     */
    @Override
    public JBrowserInitializationStatus getStatus() {
        return status;
    }

    /**
     * Add factory to process callback`s when mozilla wants to open a new window
     */
    @Override
    public void registerBrowserWindowCreator(ComponentFacotry<?> factory) {
        windowCreator.addInterceptor(factory);
    }

    @Override
    public void removeBrowserWindowCreator(ComponentFacotry<?> factory) {
        windowCreator.removeInterceptor(factory);
    }

    @Override
    public BrowserWindowCreator getWindowCreator() {
        return windowCreator;
    }

    @Override
    public String getManagerSummary() {
        StringBuilder builder = new StringBuilder(); //$NON-NLS-1$

        builder.append(String.format(mt.t("MozillaConfig.XULRunnerHome"), xulrunnerPath != null ? xulrunnerPath.getAbsolutePath() : mt.t("MozillaConfig.not_resolved"))); //$NON-NLS-1$ //$NON-NLS-2$

        builder.append(String.format(mt.t("MozillaConfig.Profile_directory"), profilePath != null ? profilePath.getAbsolutePath() : mt.t("MozillaConfig.not_used"))); //$NON-NLS-1$ //$NON-NLS-2$

        builder.append(String.format(mt.t("MozillaConfig.Platform"), Platform.platform)); //$NON-NLS-1$

        builder.append(String.format(mt.t("MozillaConfig.Java"), //$NON-NLS-1$
                System.getProperty("java.vm.version"), //$NON-NLS-1$
                System.getProperty("java.vm.vendor"))); //$NON-NLS-1$

        return builder.toString();
    }

    @Override
    public BrowserConfig getBrowserConfig() {
        return browserConfig;
    }

    @Override
    public <T extends Component> JComponentFactory<T> getComponentFactory(Class<? extends JBrowserComponent<T>> prototype) {
        return new JComponentFactory<T>(this, prototype);
    }

    @SuppressWarnings("unchecked")
	@Override
	public <T extends Component> ComponentFacotry<T> getDefaultFactory() {
		return (ComponentFacotry<T>) windowCreator.getBrowserFactory();
	}
    
    // /**
    // * Returns MozSwing's implementation of the
    // * WindowCreator XPCOM component.
    // *
    // * @return WindowCreator XPCOM component
    // */
    // public static WindowCreator getWinCreator() {
    // return winCreator;
    // }
}
