package org.mozilla.browser.impl;

import org.mozilla.browser.RunnableEx;


/**
 * Utility class for executing code
 * on the AppKit thread
 */
public class CocoaUtils {

    public static Thread appkitThread;

    /**
     * Executes the given task on the AppKit thread.
     *
     * During execution of this method, the method
     * callbackOnAppKitThread() will be called on
     * the AppKit thread.
     */
    public static native void syncExecOnAppKitThread(RunnableEx task)
        throws Exception;

    /**
     * Executes the given task on the AppKit thread.
     *
     * During execution of this method, the method
     * callbackOnAppKitThread() will be called on
     * the AppKit thread.
     */
    public static native void asyncExecOnAppKitThread(Runnable task);

    /**
     * called back on the AppKit thread with the runnable
     * passed to syncExecOnAppKitThread()
     * @param task
     */
    @SuppressWarnings("unused") //called from JNI //$NON-NLS-1$
    private static void callbackOnAppKitThread(RunnableEx task)
        throws Exception
    {
        Thread ct = Thread.currentThread();
        assert "AWT-AppKit".equals(ct.getName()); //$NON-NLS-1$
        if (appkitThread==null) appkitThread = ct;

        task.run();
    }

    /**
     * called back on the AppKit thread with the runnable
     * passed to asyncExecOnAppKitThread()
     * @param task
     */
    @SuppressWarnings("unused") //called from JNI //$NON-NLS-1$
    private static void callbackOnAppKitThread(Runnable task)
    {
        Thread ct = Thread.currentThread();
        assert "AWT-AppKit".equals(ct.getName()); //$NON-NLS-1$
        if (appkitThread==null) appkitThread = ct;

        task.run();
    }

    /**
     * Spins AppKit event loop until the method
     * stopModal() is called;
     */
    public static native void runModal();
    /**
     * Stops spinning of the AppKit event loop
     * started in runModal()
     */
    public static native void stopModal();

}
