package org.mozilla.browser;

import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.interfaces.nsIComponentManager;
import org.mozilla.interfaces.nsIComponentRegistrar;
import org.mozilla.interfaces.nsIFactory;
import org.mozilla.interfaces.nsIProxyObjectManager;
import org.mozilla.interfaces.nsIServiceManager;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsIThread;
import org.mozilla.interfaces.nsIThreadManager;
import org.mozilla.xpcom.IXPCOMError;
import org.mozilla.xpcom.Mozilla;
import org.mozilla.xpcom.XPCOMException;

/**
 * Utilities for creating and accessing of XPCOM components and services.
 *
 * <p>For understatning what is XPCOM, IDL interfaces, XPCOM objects,
 * services and components, see for example
 * <a href="http://www.mozilla.org/projects/xpcom/book/cxc/html/index.html">http://www.mozilla.org/projects/xpcom/book/cxc/html/index.html</a>.
 *
 * <p>Documentation of the XPCOM interfaces available in Mozilla can be
 * found at <a href="http://www.xulplanet.com">http://www.xulplanet.com</a>.
 */
@SuppressWarnings("unchecked") //$NON-NLS-1$
public class XPCOMUtils {

    static Log log = LogFactory.getLog(XPCOMUtils.class);

    private static String guessIID(Class c) {
        try {
            String name = c.getName();
            String baseName = c.getSimpleName();
            final String iidFieldName;
            if (name.startsWith("org.mozilla.interfaces.ns")) { //$NON-NLS-1$
                iidFieldName = String.format("NS_%s_IID", baseName.substring(2).toUpperCase()); //$NON-NLS-1$
            } else {
                iidFieldName = String.format("%s_IID", baseName.toUpperCase()); //$NON-NLS-1$
            }

            Field f = c.getDeclaredField(iidFieldName);
            String iid = (String) f.get(c);
            return iid;
        } catch (Throwable e) {
            log.error("failed to resolve IID of an XPCOM interface", e); //$NON-NLS-1$
            return null;
        }
    }

    private static String guessCID(Class c) {
        try {
            String baseName = c.getSimpleName();
            final String iidFieldName = String.format("%s_CID", baseName.toUpperCase()); //$NON-NLS-1$
            Field f = c.getDeclaredField(iidFieldName);
            String cid = (String) f.get(c);
            return cid;
        } catch (Throwable e) {
            log.error("failed to resolve CID of an XPCOM component", e); //$NON-NLS-1$
            return null;
        }
    }

    /**
     * Creates an XPCOM proxy for the given XPCOM object. Methods of
     * the proxy can be
     * then safely called from non-Mozilla threads.
     *
     * <p>This method creates a synchronous proxy. Therefore, calling
     * a method on the proxy will block the calling thread until
     * the method finishes.
     *
     * @param <T> - an XPCOM interface (org.mozilla.interfaces)
     * @param obj - XPCOM object
     * @param c - interface of the XPCOM proxy to be returned
     * @return synchronous XPCOM proxy
     */
    public static <T extends nsISupports> T proxy(nsISupports obj, Class<T> c) {
        try {
            Mozilla moz = Mozilla.getInstance();
            String iid = guessIID(c);
            nsIServiceManager sm = moz.getServiceManager();
            nsIThreadManager tm = (nsIThreadManager) sm.getServiceByContractID("@mozilla.org/thread-manager;1", nsIThreadManager.NS_ITHREADMANAGER_IID); //$NON-NLS-1$
            nsIThread mt = tm.getMainThread();
            nsIProxyObjectManager pm = (nsIProxyObjectManager) sm.getService("{eea90d41-b059-11d2-915e-c12b696c9333}", nsIProxyObjectManager.NS_IPROXYOBJECTMANAGER_IID); //$NON-NLS-1$

            T t = (T) pm.getProxyForObject(mt, iid, obj, nsIProxyObjectManager.INVOKE_SYNC);
            return t;
        } catch (Throwable e) {
            log.error("failed to create XPCOM proxy", e); //$NON-NLS-1$
            return null;
        }
    }

    /**
     * Creates an XPCOM proxy for the given XPCOM object. Methods of
     * the proxy can be then safely called from non-Mozilla threads.
     *
     * <p>This method creates an asynchronous proxy. Therefore, a call
     * of a method on the proxy will return immediately. The body of
     * the method will be executed later on the Mozilla thread.
     *
     * @param <T> - an XPCOM interface (org.mozilla.interfaces)
     * @param obj - XPCOM object
     * @param c - interface of the XPCOM proxy to be returned
     * @return asynchronous XPCOM proxy
     */
    public static <T extends nsISupports> T asyncProxy(nsISupports obj, Class<T> c) {
        try {
            Mozilla moz = Mozilla.getInstance();
            String iid = guessIID(c);
            nsIServiceManager sm = moz.getServiceManager();
            nsIThreadManager tm = (nsIThreadManager) sm.getServiceByContractID("@mozilla.org/thread-manager;1", nsIThreadManager.NS_ITHREADMANAGER_IID); //$NON-NLS-1$
            nsIThread mt = tm.getMainThread();
            nsIProxyObjectManager pm = (nsIProxyObjectManager) sm.getService("{eea90d41-b059-11d2-915e-c12b696c9333}", nsIProxyObjectManager.NS_IPROXYOBJECTMANAGER_IID); //$NON-NLS-1$

            T t = (T) pm.getProxyForObject(mt, iid, obj, nsIProxyObjectManager.INVOKE_ASYNC);
            return t;
        } catch (Throwable e) {
            log.error("failed to create XPCOM proxy", e); //$NON-NLS-1$
            return null;
        }
    }

    /**
     * Creates a new XPCOM object with the given contract ID. This method
     * is an equivalent of the <tt>do_CreateInstance()</tt> macro in mozilla
     * source code. For example,
     * <pre>
     *   create("@mozilla.org/timer;1", nsITimer.class)
     * </pre>
     * creates a new XPCOM timer object and returns its <tt>nsITimer</tt>
     * interface. Similarly,
     * <pre>
     *   create("@mozilla.org/timer;1", nsISupports.class)
     * </pre>
     * creates an XPCOM timer object and returns its <tt>nsISuppors</tt> interface.
     *
     * @param <T> - an XPCOM interface (org.mozilla.interfaces)
     * @param contractID - identifier of the XPCOM object to create
     * @param c - interface of the new XPCOM object to be returned
     * @return newly created XPCOM object
     */
    public static <T extends nsISupports> T create(String contractID, Class<T> c) {
        try {
            Mozilla moz = Mozilla.getInstance();
            String iid = guessIID(c);
            nsIComponentManager componentManager = moz.getComponentManager();

            T t = (T) componentManager.createInstanceByContractID(contractID, null, iid);

            return t;
        } catch (Throwable e) {
            log.error("failed to create XPCOM object", e); //$NON-NLS-1$
            return null;
        }
    }

    /**
     * Creates a new XPCOM object with the given contract ID. See
     * {@link XPCOMUtils#create(String, Class) create()}
     *
     *
     * for more details.
     *
     * <p>Moreover, this method can be called from non-Mozilla
     * threads and returns a <i>proxy</i> of the XPCOM object.
     * Methods of the <i>proxy</i> can be then safely called from
     * non-Mozilla threads.
     *
     * @param <T> - an XPCOM interface (org.mozilla.interfaces)
     * @param contractID - identifier of the XPCOM object to create
     * @param c - interface of the new XPCOM object to be returned
     * @return proxy of newly created XPCOM object
     */
    public static <T extends nsISupports> T createProxy(String contractID, Class<T> c) {
        try {
            Mozilla moz = Mozilla.getInstance();
            String iid = guessIID(c);
            nsIComponentManager componentManager = proxy(moz.getComponentManager(), nsIComponentManager.class);

            T t1 = (T) componentManager.createInstanceByContractID(contractID, null, iid);
            T t2 = proxy(t1, c);

            return t2;
        } catch (Throwable e) {
            log.error("failed to create XPCOM object", e); //$NON-NLS-1$
            return null;
        }
    }

    /**
     * Returns XPCOM service with the given contract ID. This method
     * is an equivalent of the <tt>do_GetService()</tt> macro in mozilla
     * source code. For example,
     * <pre>
     *   getService("@mozilla.org/cookiemanager;1", nsICookieManager.class)
     * </pre>
     * returns the <tt>nsICookieManager</tt> interface of the Mozilla's
     * cookie service. Similarly,
     * <pre>
     *   getService("@mozilla.org/cookiemanager;1", nsICookieService.class)
     * </pre>
     * returns the <tt>nsICookieService</tt> interface of the cookie service.
     *
     * @param <T> - an XPCOM interface (org.mozilla.interfaces)
     * @param contractID - identifier of the XPCOM service
     * @param c - interface of the XPCOM service to be returned
     * @return XPCOM service
     */
    public static <T extends nsISupports> T getService(String contractID, Class<T> c) {
        try {
            Mozilla moz = Mozilla.getInstance();
            String iid = guessIID(c);
            nsIServiceManager serviceManager = moz.getServiceManager();

            T t = (T) serviceManager.getServiceByContractID(contractID, iid);

            return t;
        } catch (Throwable e) {
            log.error("failed to create XPCOM service", e); //$NON-NLS-1$
            return null;
        }
    }

    /**
     * Returns XPCOM service with the given contract ID.
     * See
     * {@link XPCOMUtils#getService(String, Class) getService}
     * for more details.
     *
     * <p>Moreover, this method can be called from non-Mozilla
     * threads and returns a <i>proxy</i> of the XPCOM service.
     * Methods of the <i>proxy</i> can be then safely called from
     * non-Mozilla threads.
     *
     * @param <T> - an XPCOM interface (org.mozilla.interfaces)
     * @param contractID - identifier of the XPCOM service
     * @param c - interface of the XPCOM service to be returned
     * @return proxy of XPCOM service
     */
    public static <T extends nsISupports> T getServiceProxy(String contractID, Class<T> c) {
        try {
            Mozilla moz = Mozilla.getInstance();
            String iid = guessIID(c);
            nsIServiceManager serviceManager = proxy(moz.getServiceManager(), nsIServiceManager.class);

            T t1 = (T) serviceManager.getServiceByContractID(contractID, iid);
            T t2 = proxy(t1, c);

            return t2;
        } catch (Throwable e) {
            log.error("failed to create XPCOM service", e); //$NON-NLS-1$
            return null;
        }
    }

    /**
     * Returns a different interface of the given XPCOM object.
     * This method is an equivalent of the <tt>do_QueryInterface()</tt>
     * macro in mozilla source code.
     *
     * <p>For example, the Mozilla's cookie service implements the
     * following interfaces
     * <pre>
     *   nsICookieService
     *   nsICookieManager
     *   nsICookieManager2
     *   nsIObserver
     *   nsISupportsWeakReference
     * </pre>
     *
     * Obtaining another interfaces of an XPCOM object is done
     * using the following code snippet
     * <pre>
     *   nsICookieManager cm = ....
     *   nsIObserver os = qi(cm, nsIObserver.class);
     * </pre>
     *
     * @param <T> - an XPCOM interface (org.mozilla.interfaces)
     * @param obj - an XPCOM object
     * @param c - interface of the XPCOM object to be returned
     * @return XPCOM object
     */
    public static <T extends nsISupports> T qi(nsISupports obj, Class<T> c) {
        try {
            if (obj==null) return null;
            String iid = guessIID(c);
            T t = (T) obj.queryInterface(iid);
            return t;
        } catch (XPCOMException e) {
            //do not print an error if
            //obj does not implement the interface
            if (e.errorcode != IXPCOMError.NS_ERROR_NO_INTERFACE) {
                log.error("failed to query-interface an XPCOM object", e); //$NON-NLS-1$
            }
            return null;
        } catch (Throwable e) {
            log.error("failed to query-interface an XPCOM object", e); //$NON-NLS-1$
            return null;
        }
    }

    /**
     * Registers a new factory for the XPCOM objects and services.
     *
     * <p>For example, this code registers a new factory for the timer
     * XPCOM object.
     * <pre>
     *   nsIFactory myTimerFactory = ...
     *   register("@mozilla.org/timer;1", myTimerFactory);
     * </pre>
     *
     * <p>Note, some XPCOM objects (such as XPCOM services)
     * are singletons. Therefore, if a custom factory is registered
     * when the singleton already exists, the method
     * {@link nsIFactory#createInstance(nsISupports, String) createInstance()}
     * of the factory will be never called.
     *
     * @param contractID - identifier of the XPCOM objects this factory creates
     * @param factory - factory for XPCOM objects
     */
    public static void register(String contractID, nsIFactory factory) {
        Mozilla moz = Mozilla.getInstance();
        nsIComponentRegistrar componentRegistrar = moz.getComponentRegistrar();
        String name = factory.getClass().getSimpleName();
        String cid = guessCID(factory.getClass());
        componentRegistrar.registerFactory(cid, name, contractID, factory);
    }

}