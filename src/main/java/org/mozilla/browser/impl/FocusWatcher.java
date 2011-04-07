package org.mozilla.browser.impl;

import static java.awt.event.FocusEvent.FOCUS_GAINED;
import static org.mozilla.browser.MozillaExecutor.swingAsyncExec;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusEvent;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.atomation.jbrowser.impl.JBrowserCanvas;

public class FocusWatcher {

    static Log log = LogFactory.getLog(FocusWatcher.class);

    private static Map<Window, List<JBrowserCanvas>> xx = new HashMap<Window, List<JBrowserCanvas>>();

    public static void register(final JBrowserCanvas c) {
        swingAsyncExec(new Runnable() { public void run() {
            Window win = SwingUtilities.getWindowAncestor(c);
            assert win!=null;

            boolean wasEmpty = xx.isEmpty();

            List<JBrowserCanvas> cs = xx.get(win);
            if (cs==null) {
                cs = new LinkedList<JBrowserCanvas>();
                xx.put(win, cs);
            }
            cs.add(c);

            if (wasEmpty) {
                Toolkit.getDefaultToolkit().
                addAWTEventListener(gfl,
                                    FocusEvent.FOCUS_EVENT_MASK
                                    | WindowEvent.WINDOW_FOCUS_EVENT_MASK);
            }
        }});
    }

    public static void unregister(final JBrowserCanvas c) {
        swingAsyncExec(new Runnable() { public void run() {
            //search window the canvas was registered in
            for (Window win : xx.keySet()) {
                List<JBrowserCanvas> cs = xx.get(win);
                assert cs!=null;
                if (cs.remove(c)) {
                    if (cs.isEmpty()) {
                        xx.remove(win);
                    }
                    break;
                }
            }

            if (xx.isEmpty()) {
                Toolkit.getDefaultToolkit().
                removeAWTEventListener(gfl);
            }
        }});
    }

    private static GlobalFocusListener gfl = new GlobalFocusListener();

    static class GlobalFocusListener implements AWTEventListener {
        public void eventDispatched(AWTEvent ev) {
            log.debug(ev.getSource().getClass().getSimpleName()+ " "+ev); //$NON-NLS-1$
            if (!(ev instanceof FocusEvent)) return;

            FocusEvent fev = (FocusEvent) ev;

            if (fev.getID()==FOCUS_GAINED &&
                (ev.getSource() instanceof Component))
            {
                Component cmp = (Component) ev.getSource();

                if (cmp instanceof JBrowserCanvas) {
                    JBrowserCanvas c = (JBrowserCanvas) cmp;
                    c.onFocusMovedTo(cmp);
                } else {
                    //notify one of the canvas in window
                    //to deactivate gecko
                    Window win = SwingUtilities.getWindowAncestor(cmp);
                    if (win!=null) {
                        List<JBrowserCanvas> cs = xx.get(win);
                        if (cs!=null && !cs.isEmpty()) {
                            JBrowserCanvas c = (JBrowserCanvas) cs.get(0);
                            c.onFocusMovedTo(cmp);
                        }
                    }
                }
            }

        }
    }

}
