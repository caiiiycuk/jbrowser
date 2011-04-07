package org.mozilla.browser;

import java.util.concurrent.Callable;

/**
 * Interface similar to {@link Runnable}, but allows
 * to throw an exception from run() method.
 */
public abstract class RunnableEx implements Callable<Void>
{
    /**
     * Method to be executed.
     *
     * @throws Exception - exception that can be thrown
     */
    public abstract void run()
        throws Exception;

    /**
     * Internal method.
     * Use {@link RunnableEx#run()} instead.
     * @return always null
     * @throws Exception - exception from execution
     */
    public final Void call() throws Exception {
        run();
        return null;
    }

}
