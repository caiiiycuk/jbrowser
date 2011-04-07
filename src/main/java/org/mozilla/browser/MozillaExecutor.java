package org.mozilla.browser;

import static org.mozilla.browser.XPCOMUtils.asyncProxy;
import static org.mozilla.browser.XPCOMUtils.getService;
import static org.mozilla.browser.XPCOMUtils.proxy;
import static org.mozilla.browser.impl.jna.Gtk.gtk;

import java.awt.Toolkit;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.browser.common.Platform;
import org.mozilla.browser.impl.CocoaUtils;
import org.mozilla.interfaces.nsIAppShell;
import org.mozilla.interfaces.nsIComponentManager;
import org.mozilla.interfaces.nsIRunnable;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsIThread;
import org.mozilla.interfaces.nsIThreadManager;
import org.mozilla.xpcom.Mozilla;


/**
 * Executor for running jobs on mozilla thread.
 */
public class MozillaExecutor {

    static Log log = LogFactory.getLog(MozillaExecutor.class);

    /**
     * The single executor for all mozilla jobs.
     */
    private static final MozillaExecutor singleton = new MozillaExecutor();

    /**
     * Thread where mozilla run.
     */
    private static Thread mozillaThread;
    private static boolean browserMangerInitilized;

    /**
     * Lock held until mozilla is successfully initialized
     */
    private final Semaphore initLock;


    private MozillaExecutor() {
        try {
        	MozillaExecutor.browserMangerInitilized = false;
            this.initLock = new Semaphore(1);
            this.initLock.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadLib(String libName, String libFileName, File libDir)
    {
        assert libName!=null && libFileName!=null;

        if (libDir==null) {
            //fallback to System.loadLibrary
            System.loadLibrary(libName);
            return;
        }

        File libFile = new File(libDir, libFileName);
        if (!libFile.exists()) {
            //fallback to System.loadLibrary
            System.loadLibrary(libName);
            return;
        }

        //use System.load to avoid requiring
        //java.library.path to be set
        System.load(libFile.getAbsolutePath());
    }

//    static X11.XErrorHandlerFunc x_error_handler = new X11.XErrorHandlerFunc() {
//        ////  //public int callback(Display display, XErrorEvent error_event) {
//        public int callback(Pointer display, Pointer error_event) {
//            System.err.println("Catched XError here !!!!!!!!!!!!! dderreree");
//            return 0;
//        }
//    };

    //private static int awtLocks = 0;

    private static class MozCallable<V> implements nsIRunnable, Runnable {

        //private final Throwable from=new Throwable();

        private final Runnable task1;
        private final Callable<V> task2;
        private V result;
        private Throwable error;

        /**
         * (non-Javadoc)
         * @param task
         */
        public MozCallable(Callable<V> task) {
            this.task1 = null;
            this.task2 = task;
        }
        /**
         * (non-Javadoc)
         * @param task
         */
        public MozCallable(Runnable task) {
            this.task1 = task;
            this.task2 = null;
        }

        public void run() {
            //execute the job on mozilla thread
            if (task1==null && task2==null) return;
            try {
                if(task1!=null) task1.run();
                if(task2!=null) result = task2.call();
            } catch (Throwable t) {
                error = t;
            }
        }

        public nsISupports queryInterface(String uuid) {
            return Mozilla.queryInterface(this, uuid);
        }
    }


    private void syncExec(Runnable task)
        throws MozillaRuntimeException
    {
        if (isMozillaThread()) {
            try {
                task.run();
            } catch (Throwable e) {
                throw new MozillaRuntimeException("wrapped exception from mozilla task", e); //$NON-NLS-1$
            }
        } else {
            MozCallable<Void> c = new MozCallable<Void>(task);
            nsIRunnable p = proxy(c, nsIRunnable.class);
            p.run();
            if (c.error!=null) throw new MozillaRuntimeException("wrapped exception from mozilla task", c.error); //$NON-NLS-1$
            //return c.result;
        }
    }

    private void syncExec(RunnableEx task)
        throws MozillaException
    {
        if (isMozillaThread()) {
            try {
                task.call();
            } catch (Throwable e) {
                throw new MozillaException("wrapped exception from mozilla task", e); //$NON-NLS-1$
            }
        } else {
            MozCallable<Void> c = new MozCallable<Void>(task);
            nsIRunnable p = proxy(c, nsIRunnable.class);
            p.run();
            if (c.error!=null) throw new MozillaException("wrapped exception from mozilla task", c.error); //$NON-NLS-1$
            //return c.result;
        }
    }

    private <V> V syncExec(Callable<V> task)
        throws MozillaException
    {
        if (isMozillaThread()) {
            try {
                return task.call();
            } catch (Throwable e) {
                throw new MozillaException("wrapped exception from mozilla task", e); //$NON-NLS-1$
            }
        } else {
            MozCallable<V> c = new MozCallable<V>(task);
            nsIRunnable p = proxy(c, nsIRunnable.class);
            p.run();
            if (c.error!=null) throw new MozillaException("wrapped exception from mozilla task", c.error); //$NON-NLS-1$
            return c.result;
        }
    }

    private <V> MozCallable<V> asyncExec(final Runnable task)
    {
        final MozillaRuntimeException from = new MozillaRuntimeException();
        MozCallable<V> c = new MozCallable<V>(new Runnable() { public void run() {
            try {
                task.run();
            } catch(Throwable t) {
                log.error("error in asynchronus mozilla task", t); //$NON-NLS-1$
                log.error("called from:", from); //$NON-NLS-1$
            }
        }});

        if (Platform.platform == Platform.OSX) {
            CocoaUtils.asyncExecOnAppKitThread(c);
        } else {
            nsIRunnable p = asyncProxy(c, nsIRunnable.class);
            p.run();
        }

        return c;
    }


//    /**
//     * Synchronously executes a task on Swing thread
//     * and spins the Mozilla thread while waiting for
//     * the Swing task to complete.
//     *
//     * @param task task
//     */
//    private void syncSwingExec(final Runnable task)
//        throws MozillaRuntimeException
//    {
//        assert MozillaExecutor.isMozillaThread();
//
//        //this task will execute always after enterModalEventLoop()
//        //becase we run in this method on mozilla thread, and so
//        //block other tasks until we reach nsIAppShell.run()
//        //in enterModalEventLoop()
//        final Runnable leaveTask = new Runnable() { public void run() {
//            exitModalEventLoop();
//        }};
//
//        final Throwable[] swingError = { null };
//        final Runnable swingTask = new Runnable() { public void run() {
//            try {
//                task.run();
//            } catch (Throwable t) {
//                swingError[0] = t;
//            }
//            try {
//                Thread.sleep(300);
//            } catch (InterruptedException e) {
//                log.error("wait interrupted", e);
//            }
//            //mozAsyncExec(leaveTask);
//            //leaveTask.run();
//        }};
//
//
//        SwingUtilities.invokeLater(swingTask);
//        //log.trace("<-- swing async leave");
//
////        enterModalEventLoop();
//
//        if (swingError[0]!= null) {
//            throw new MozillaRuntimeException(swingError[0]);
//        }
//        //log.trace("<-- swing sync leave");
//    }

    /**
     * Returns true, if the current thread is the mozilla thread,
     * that is a thread where (most of) the mozilla code runs.
     *
     * <p>On OSX, mozilla thread is the AppKit thread.
     *
     * @return true if current thread is mozilla thread
     */
    public static boolean isMozillaThread() {
        boolean b;

        Thread ct = Thread.currentThread();
        if (mozillaThread != null)
            b = ct==mozillaThread;
        else {
            String name = ct.getName();
            b =
                name.startsWith("AWT-EventQueue") || //$NON-NLS-1$
                name.startsWith("AWT-AppKit"); //$NON-NLS-1$
        }

        return b;
    }

    /**
     * Synchronously executes a task on mozilla thread.
     *
     * <p>An exception caught during task exception, is re-thrown
     * wrapped in a subclass of the RuntimeException, so its
     * explicit handling is not enforced.
     *
     * @param task task to be executed
     * @throws MozillaRuntimeException internal error if occurred
     */
    public static void mozSyncExec(Runnable task)
        throws MozillaRuntimeException
    {
        singleton.syncExec(task);
    }

    /**
     * Synchronously executes a task on mozilla thread.
     *
     * <p>An exception caught during task exception, is re-thrown
     * wrapped in a MozillaException, so its explicit
     * handling is enforced.
     *
     * @param task task to be executed
     * @throws MozillaException wrapped exception thrown from
     *   {@link RunnableEx#run()} or internal error if occurred
     */
    public static void mozSyncExec(RunnableEx task)
        throws MozillaException
    {
        singleton.syncExec(task);
    }

    /**
     * Synchronously executes a task with result
     * on mozilla thread.
     *
     * <p>An exception caught during task exception, is re-thrown
     * wrapped in a MozillaException, so its explicit
     * handling is enforced.
     *
     * @param task task to be executed
     * @param <V> type of return value
     * @return task return value
     * @throws MozillaException wrapped exception thrown from
     *   {@link Callable#call()} or internal error if occurred
     */
    public static <V> V mozSyncExec(Callable<V> task)
        throws MozillaException
    {
        return singleton.syncExec(task);
    }
    /**
     * Synchronously and quietly executes a task with result
     * on mozilla thread.
     *
     * <p>Possible exception is encapsulated into
     * {@link MozillaRuntimeException}, so that it does not have
     * to be handled explicitely in application logic.

     * @param task task to be executed
     * @param <V> type of return value
     * @return task return value
     * @throws MozillaRuntimeException wrapped exception thrown from
     *   {@link Callable#call()} or internal error if occurred.
     */
    public static <V> V mozSyncExecQuiet(Callable<V> task)
        throws MozillaRuntimeException
    {
        try {
            return singleton.syncExec(task);
        } catch (Exception e) {
            throw new MozillaRuntimeException(e);
        }
    }

    /**
     * Asynchronously executes a task on mozilla thread.
     *
     * @param task task to be executed
     * @return internal callable object , the task was wrapped into
     */
    public static MozCallable<Void> mozAsyncExec(Runnable task)
    {
        //FutureTask<Void> ft = new FutureTask<Void>(task, null);
        //return singleton.asyncExec(ft);
        //return ft;
        return singleton.asyncExec(task);
    }

    private static class PendingTask {
        final Runnable task;
        final Throwable postedFrom;

        /**
         * (non-Javadoc)
         * @param task
         * @param postedFrom
         */
        public PendingTask(Runnable task, Throwable postedFrom)
        {
            this.task = task;
            this.postedFrom = postedFrom;
        }
    }
    private static final List<PendingTask> postponedMozTaks = new LinkedList<PendingTask>();
    /**
     * Queues a task to be executed as soon as the mozilla is initialized.
     *
     * @param task task to be executed
     */
    public static void mozPostponableSyncExec(Runnable task)
    {
        if (browserMangerInitilized) {
            mozSyncExec(task);
        } else {
            PendingTask ptask = new PendingTask(task, new Throwable());
            postponedMozTaks.add(ptask);
        }
    }
   
    public static void runPostponedPreInitTasks()
    {
        for (PendingTask ptask : postponedMozTaks) {
            try {
                mozSyncExec(ptask.task);
            } catch(Throwable t) {
                log.error("error in postponed mozilla task", t); //$NON-NLS-1$
                log.error("called from:", ptask.postedFrom); //$NON-NLS-1$
            }
        }
    }

//    public static void mozSwingSyncExec(Runnable task)
//        throws MozillaRuntimeException
//    {
//        singleton.syncSwingExec(task);
//    }

    /**
     * Synchronously execute a task on swing thread.
     *
     * Possible exception is encapsulated into
     * {@link RuntimeException}, so that it has to
     * be handled explicitely in application logic.
     *
     * @param task task to be executed
     * @throws RuntimeException error if occurred
     */
    public static void swingSyncExec(final Runnable task)
        throws RuntimeException
    {
        final Throwable[] swingError = { null };
        final Runnable swingTask = new Runnable() { public void run() {
            try {
                task.run();
            } catch (Throwable t) {
                swingError[0] = t;
            }
        }};

        try {
            SwingUtilities.invokeAndWait(swingTask);
        } catch (Exception e) {
            throw new MozillaRuntimeException(e);
        }

        if (swingError[0]!= null) {
            throw new MozillaRuntimeException(swingError[0]);
        }
    }

    /**
     * Synchronously execute a task on swing thread.
     *
     * Possible exception is encapsulated into
     * {@link MozillaException}, so that it has to
     * be handled explicitely in application logic.
     *
     * @param task task to be executed
     * @throws Exception error if occurred
     */
    public static void swingSyncExec(final RunnableEx task)
        throws Exception
    {
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            final Throwable[] swingError = { null };
            final Runnable swingTask = new Runnable() { public void run() {
                try {
                    task.run();
                } catch (Throwable t) {
                    swingError[0] = t;
                }
            }};

            SwingUtilities.invokeAndWait(swingTask);

            if (swingError[0]!= null) {
                throw new Exception(swingError[0]);
            }
        }
    }

    /**
     * Asynchronously executes a task on swing thread.
     *
     * @param task task to be executed
     */
    public static void swingAsyncExec(Runnable task)
    {
        SwingUtilities.invokeLater(task);
    }

    private int nestingLevel = 0;
    private void enterModalEventLoop() {
        assert MozillaExecutor.isMozillaThread();

        if (Platform.platform!=Platform.OSX) {
            int currLevel = nestingLevel;
            nestingLevel++;

            nsIThreadManager tm = getService("@mozilla.org/thread-manager;1", nsIThreadManager.class); //$NON-NLS-1$
            nsIThread mt = tm.getMainThread();
            while (nestingLevel>currLevel) {
                mt.processNextEvent(true);
            }
            assert nestingLevel==currLevel;
        } else {
            CocoaUtils.runModal();
        }
    }

    private void exitModalEventLoop() {
        assert MozillaExecutor.isMozillaThread();

        if (Platform.platform!=Platform.OSX) {
            nestingLevel--;
        } else {
            CocoaUtils.stopModal();
        }
    }

    /**
     * Enters a nested event loop of a modal mozilla chrome window
     * @param chromeAdapter browser window
     */
    public static void mozEnterModalEventLoop() {
        singleton.enterModalEventLoop();
    }

    /**
     * leaves a nested event loop of a modal mozilla chrome window
     */
    public static void mozExitModalEventLoop() {
        singleton.exitModalEventLoop();
    }

    /**
     * Synchronously executes a task for mozilla initialization.
     */
    public static void mozInit(final File xulrunnerDir,
                                  final File nativeLibsDir,
                                  final RunnableEx task)
        throws MozillaException
    {
        if (Platform.platform==Platform.OSX) {
            //on OS X:
            // - we have to execute the initialization
            //   on the AppKit thread
            // - we do not need to spin the event loop
            //   using nsIAppShell.run(), because Swing
            //   will spin AppKit event loop, and therefore
            //   also the Mozilla event loop
            try {
                loadLib("cocoautils", "libcocoautils.jnilib", nativeLibsDir); //$NON-NLS-1$ //$NON-NLS-2$
                CocoaUtils.syncExecOnAppKitThread(task);
                mozillaThread = CocoaUtils.appkitThread;
                assert mozillaThread!=null;
            } catch (Throwable t) {
                throw new MozillaException(t);
            }
        } else {
            //on win32 and linux:
            // - we have to spin the event loop,
            //   using nsIAppShell.run()
            // - therefore we have to execute initialization
            //   on a new thread, that will end with a call
            //   to nsIAppShell.run() that won't return

            if (Platform.usingGTK2Toolkit()) {

                LookAndFeel laf = UIManager.getLookAndFeel();
                String jdkWithGtkLafFix =  "1.7"; //FIXME wait for the 1.7 fix //$NON-NLS-1$
                if (!"false".equals(System.getProperty("mozswing.gtklaf_check")) && //$NON-NLS-1$ //$NON-NLS-2$
                    "GTK".equals(laf.getID()) && //$NON-NLS-1$
                    !Platform.checkJavaVersion(jdkWithGtkLafFix, "")) //$NON-NLS-1$
                {
                    //known to deadlock if using native engine with the gtk l&f
                    //submitted a patch to jdk
                    throw new MozillaException(String.format("JDK version %s or newer is required if using the GTK look & feel", jdkWithGtkLafFix)); //$NON-NLS-1$
                }

                try {
                    //ensure we use XEmbedCanvasPeer, otherwise
                    //focus and key events are not forwarded
                    //to the gtk mozilla widget
                    Class.forName("sun.awt.X11.XEmbedCanvasPeer"); //$NON-NLS-1$
                    System.setProperty("sun.awt.xembedserver", "true"); //$NON-NLS-1$ //$NON-NLS-2$
                } catch (Throwable t) {
                    throw new MozillaException("jdk does not support XEMBED protocol", t); //$NON-NLS-1$
                }
            }

            //create lock for task completition
            final Semaphore taskLock = new Semaphore(1);
            try {
                taskLock.acquire();
            } catch (InterruptedException e) {
                log.error("wait interrupted", e); //$NON-NLS-1$
            }

            //create mozilla thread
            final Throwable taskException[] = new Throwable[1];
            mozillaThread = new Thread() { @Override public void run() {
                try {
                    if (Platform.usingGTK2Toolkit()) {
                        //ensure AWT library is loaded
                        //(needed for lockAWT/unlockAWT
                        Toolkit tk = Toolkit.getDefaultToolkit();
                        tk.sync();

                        //initialize GTK, because mozilla code
                        //in nsIBaseWindow.initWindow assumes that

                        //create two separate X11 connections
                        //Gdk.GdkLibrary.INSTANCE.gdk_init_check(Pointer.createConstant(0), Pointer.createConstant(0));
                        //Pointer dm = Gdk.GdkLibrary.INSTANCE.gdk_display_manager_get();
                        //Pointer d = Gdk.GdkLibrary.INSTANCE.gdk_display_open(Pointer.createConstant(0));
                        //Gdk.GdkLibrary.INSTANCE.gdk_display_manager_set_default_display(dm, d);

                        //loadLib("gtkutilsjni", "libgtkutilsjni.so", nativeLibsDir);
                        //GtkUtils.init();
                        //GtkUtils.setXErrorHandler();
                        gtk.gtk_init(null, null);
                        //X11.INSTANCE.XSetErrorHandler(x_error_handler);
                        gtk.gdk_error_trap_push();
//                        gtk.g_timeout_add(100, new Glib.GSourceFunc() {
//                            public boolean callback(Pointer data) {
//                                System.err.println("dddd");
//                                return false;
//                            }
//                        }, null);
                    }

                    task.run();
                } catch (Throwable t) {
                    taskException[0] = t;
                    return;
                } finally {
                    taskLock.release();
                }

                //spin the main event loop
                Mozilla moz = Mozilla.getInstance();
                nsIComponentManager componentManager = moz.getComponentManager();
                String NS_APPSHELL_CID = "{2d96b3df-c051-11d1-a827-0040959a28c9}"; //constant from mozilla/widget/public/nsWidgetsCID.h //$NON-NLS-1$
                nsIAppShell appShell = (nsIAppShell) componentManager.createInstance(NS_APPSHELL_CID, null, nsIAppShell.NS_IAPPSHELL_IID);
                //appShell.create(null, null);
                //appShell.spinup();
                appShell.run(); //spint mozilla event loop
            }};
            mozillaThread.setDaemon(true);
            mozillaThread.setName("Mozilla"); //$NON-NLS-1$
            mozillaThread.start();

            //wait for the initialization task to complete
            try {
                taskLock.acquire();
            } catch (InterruptedException e) {
                log.error("wait interrupted", e); //$NON-NLS-1$
            }
            //forward an exception, if occured
            if (taskException[0]!=null)
                throw new MozillaException(taskException[0]);
        }

        //now the worker can start processing task queue
        singleton.initLock.release();
    }

    public static void setBrowserMangerInitilized(boolean browserMangerInitilized) {
        MozillaExecutor.browserMangerInitilized = browserMangerInitilized;
    }
    
//  //used for debugging by a custom patch in javaxpcom
//  //see bug https://www.mozdev.org/bugs/show_bug.cgi?id=17849
//  public static void logCurrentThread(Object o) {
//      log.trace("thread="+Thread.currentThread()+" "+o);
//      log.trace("called from:", new Exception());
//  }


}
