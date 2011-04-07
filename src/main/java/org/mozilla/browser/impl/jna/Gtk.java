package org.mozilla.browser.impl.jna;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Native;
import com.sun.jna.NativeMapped;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public interface Gtk extends Gdk {

    Gtk gtk = (Gtk)
        Native.loadLibrary("gtk-x11-2.0", Gtk.class); //$NON-NLS-1$

    int GTK_WINDOW_TOPLEVEL = 0;
    int GTK_WINDOW_POPUP = 1;

    public static class GBaseStruct extends Structure {
        public long getPeer() {
            String s = getPointer().toString();
            s = s.replace("native@0x", ""); //$NON-NLS-1$ //$NON-NLS-2$
            return Long.valueOf(s, 16);
        }
    }

    public static class GBaseMapped implements NativeMapped {
        private Integer id = new Integer(0);
        public GBaseMapped() { }
        public GBaseMapped(Integer id) { this.id = id==null ? 0 : id; }
        public GBaseMapped(int id) { this(new Integer(id)); }
      public Object fromNative(Object nativeValue, FromNativeContext context) {
          return new GBaseMapped((Integer)nativeValue);
      }
      public Class<Integer> nativeType() {
          return Integer.class;
      }
      public Object toNative() {
          return id;
      }
      public boolean isNull() {
          return id==null || id.intValue()==0;
      }
      public int getPeer() {
          return id;
      }
    }

    public static class GInitiallyUnowned extends GBaseStruct {
        public byte x0, x1, x2, x3;
        public byte x4, x5, x6, x7;
        public byte x8, x9, x10, x11;
    }

    public static class GtkObject extends GBaseStruct {
        public GInitiallyUnowned parent_instance;

        /**
         * 32 bits of flags. GtkObject only uses 4 of these bits and
         * GtkWidget uses the rest. This is done because structs are
         * aligned on 4 or 8 byte boundaries. If a new bitfield were
         * used in GtkWidget much space would be wasted.
         */
        public int flags;
    }

    /**
     * A requisition is a desired amount of space which a
     * widget may request.
     */
    public static class GtkRequisition extends GBaseStruct {
        public int width;
        public int height;
    }

    public static class GtkAllocation extends GdkRectangle {
    }

    public static class GtkWidget extends GBaseStruct {
        /**
         * The object structure needs to be the first
         * element in the widget structure in order for
         * the object mechanism to work correctly. This
         * allows a GtkWidget pointer to be cast to a
         * GtkObject pointer.
         */
        public GtkObject object;

        /**
         * 16 bits of internally used private flags.
         * this will be packed into the same 4 byte alignment frame that
         * state and saved_state go. we therefore don't waste any new
         * space on this.
         */
        public short private_flags;

        /**
         * The state of the widget. There are actually only
         * 5 widget states (defined in "gtkenums.h").
         */
        public byte state;

        /**
         * The saved state of the widget. When a widget's state
         * is changed to GTK_STATE_INSENSITIVE via
         * "gtk_widget_set_state" or "gtk_widget_set_sensitive"
         * the old state is kept around in this field. The state
         * will be restored once the widget gets sensitive again.
         */
        public byte saved_state;

        /**
         * The widget's name. If the widget does not have a name
         * (the name is NULL), then its name (as returned by
         * "gtk_widget_get_name") is its class's name.
         * Among other things, the widget name is used to determine
         * the style to use for a widget.
         */
        public String name;

        /*< public >*/

        /**
         * The style for the widget. The style contains the
         * colors the widget should be drawn in for each state
         * along with graphics contexts used to draw with and
         * the font to use for text.
         */
        public Pointer style;

        /**
         * The widget's desired size.
         */
        public GtkRequisition requisition;

        /**
         * The widget's allocated size.
         */
        public GtkAllocation allocation;

        /**
         * The widget's window or its parent window if it does
         * not have a window. (Which will be indicated by the
         * GTK_NO_WINDOW flag being set).
         */
        public GdkWindow window;

        /**
         * The widget's parent.
         */
        public Pointer parent;
    }

    public static class GtkWindow extends GtkWidget {
//        public static final GtkWindow None = null;
//        public GtkWindow() { }
//        public GtkWindow(Integer id) { super(id); }
//        public GtkWindow(int id) { super(id); }
//        public Object fromNative(Object nativeValue, FromNativeContext context) {
//            if (isNone(nativeValue))
//                return None;
//            return new GtkWindow((Integer)nativeValue);
//        }
    }

    public static class GtkPlug extends GtkWindow {
//        public GtkPlug() { }
//        public GtkPlug(Integer id) { super(id); }
//        public GtkPlug(int id) { super(id); }
//        public Object fromNative(Object nativeValue, FromNativeContext context) {
//            return new GtkPlug((Integer)nativeValue);
//        }
    }

    void gtk_init(IntByReference argc, PointerByReference argv);

    //GtkWidget
    void gtk_widget_show(GtkWidget widget);
    void gtk_widget_destroy(GtkWidget widget);
    void gtk_widget_set_usize(GtkWidget widget, int width, int height);
    void gtk_widget_set_size_request(GtkWidget widget, int width, int height);

    //GtkWindow
    GtkWindow gtk_window_new(int type);
    void gtk_window_set_default_size(GtkWindow window, int width, int height);
    void gtk_window_set_title(GtkWindow window, String title);
    void gtk_window_resize(GtkWindow window, int width, int height);

    //GtkPlug
    GtkPlug gtk_plug_new(X11.Window socket_id);

    int gtk_events_pending();
    int gtk_main_iteration();
}
