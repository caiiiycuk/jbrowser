package org.mozilla.browser.impl.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class Glib {

    public static Glib glib = new Glib();

    private Glib() {}

    public void g_thread_init(Pointer vtable) {
    	Gthread.gthread.g_thread_init(vtable);
    }

//    public static interface GSourceFunc extends Callback {
//        boolean callback(Pointer data);
//    }
//    int g_idle_add(GSourceFunc func, Pointer data);
//    int g_timeout_add(int interval, GSourceFunc func, Pointer data);

    private interface Gthread extends Library {
    	Gthread gthread = (Gthread)
    		Native.loadLibrary("gthread-2.0", Gthread.class); //$NON-NLS-1$

        void g_thread_init(Pointer vtable);
    }

}
