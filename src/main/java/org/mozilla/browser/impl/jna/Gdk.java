package org.mozilla.browser.impl.jna;

import org.mozilla.browser.impl.jna.Gtk.GBaseMapped;
import org.mozilla.browser.impl.jna.Gtk.GBaseStruct;
import org.mozilla.browser.impl.jna.X11.Display;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface Gdk extends Library {

    public static class GdkRectangle extends GBaseStruct {
        public int x;
        public int y;
        public int width;
        public int height;
    }

    public static class GdkDisplay extends GBaseMapped {
        public static final GdkDisplay None = null;
        public GdkDisplay() { }
        public GdkDisplay(Integer id) { super(id); }
        public GdkDisplay(int id) { super(id); }
        public Object fromNative(Object nativeValue, FromNativeContext context) {
            return new GdkDisplay((Integer)nativeValue);
        }
      }

      public static class GdkWindow extends GBaseMapped {
          public static final GdkWindow None = null;
          public GdkWindow() { }
          public GdkWindow(Integer id) { super(id); }
          public GdkWindow(int id) { super(id); }
          public Object fromNative(Object nativeValue, FromNativeContext context) {
              return new GdkWindow((Integer)nativeValue);
          }
      }

    boolean gdk_init_check(Pointer argc, Pointer argv);
    Pointer gdk_display_manager_get();
    Pointer gdk_display_manager_get_default_display (Pointer display_manager);
    void gdk_display_manager_set_default_display (Pointer display_manager, Pointer display);
    Pointer gdk_display_open(Pointer display_name);

    X11.Window gdk_x11_drawable_get_xid(GdkWindow window);
    Display gdk_x11_display_get_xdisplay(GdkDisplay display);
    GdkDisplay gdk_display_get_default ();
    void gdk_window_resize(GdkWindow window, int width, int height);

    void gdk_error_trap_push();

    void gdk_threads_init();
    void gdk_threads_enter();
    void gdk_threads_leave();
}
