package org.mozilla.browser.impl;

import static org.mozilla.browser.MozillaExecutor.swingAsyncExec;
import static org.mozilla.browser.XPCOMUtils.qi;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.browser.MozillaExecutor;
import org.mozilla.browser.MozillaKeyEvent;
import org.mozilla.browser.MozillaMouseEvent;
import org.mozilla.dom.NodeFactory;
import org.mozilla.interfaces.nsIDOMEvent;
import org.mozilla.interfaces.nsIDOMEventListener;
import org.mozilla.interfaces.nsIDOMKeyEvent;
import org.mozilla.interfaces.nsIDOMMouseEvent;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.xpcom.Mozilla;
import org.w3c.dom.Node;

import ru.atomation.jbrowser.impl.JBrowserComponent;

/**
 * Listener for loading events in mozilla browser
 */
public class DOMAdapter implements nsIDOMEventListener {

    static Log log = LogFactory.getLog(DOMAdapter.class);
    public static String[] hookedEvents = new String[]{
        "click", "mousedown", "mouseup", "mouseover", "mousemove", "mouseout", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        "keydown", "keyup", "keypress" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    };
    private final JBrowserComponent<?> browserComponent;

    public DOMAdapter(JBrowserComponent<?> browserComponent) {
        this.browserComponent = browserComponent;
    }

    public void handleEvent(nsIDOMEvent event) {
        assert MozillaExecutor.isMozillaThread();
        //log.debug("DOM event: "+event.getType());

        nsIDOMMouseEvent nsmev = qi(event, nsIDOMMouseEvent.class);
        if (nsmev != null) {
            fireAWTEvent(nsmev);
        } else {
            nsIDOMKeyEvent nskev = qi(event, nsIDOMKeyEvent.class);
            if (nskev != null) {
                fireAWTEvent(nskev);
            }
        }
    }

    private void fireAWTEvent(nsIDOMMouseEvent nsmev) {
        final Component source = browserComponent.getComponent();

        final int id;
        String type = nsmev.getType();
        if (type.equals("click")) { //$NON-NLS-1$
            id = MouseEvent.MOUSE_CLICKED;
        } else if (type.equals("mousedown")) { //$NON-NLS-1$
            id = MouseEvent.MOUSE_PRESSED;
        } else if (type.equals("mouseup")) { //$NON-NLS-1$
            id = MouseEvent.MOUSE_RELEASED;
        } else if (type.equals("mouseover")) { //$NON-NLS-1$
            id = MouseEvent.MOUSE_ENTERED;
        } else if (type.equals("mousemove")) { //$NON-NLS-1$
            id = MouseEvent.MOUSE_MOVED;
        } else if (type.equals("mouseout")) { //$NON-NLS-1$
            id = MouseEvent.MOUSE_EXITED;
        } else {
            //unknown
            log.error("Unknown mouse event type " + type); //$NON-NLS-1$
            return;
        }

        final Node sourceNode = NodeFactory.getNodeInstance(nsmev.getTarget());
        final long when = System.currentTimeMillis();

        int mods = 0;
        if (nsmev.getShiftKey()) {
            mods |= MouseEvent.SHIFT_DOWN_MASK;
        }
        if (nsmev.getCtrlKey()) {
            mods |= MouseEvent.CTRL_DOWN_MASK;
        }
        if (nsmev.getMetaKey()) {
            mods |= MouseEvent.META_DOWN_MASK;
        }
        if (nsmev.getAltKey()) {
            mods |= MouseEvent.ALT_DOWN_MASK;
        }
        if (nsmev.getButton() == 0) {
            mods |= MouseEvent.BUTTON1_DOWN_MASK; //left
        }
        if (nsmev.getButton() == 1) {
            mods |= MouseEvent.BUTTON2_DOWN_MASK; //middle
        }
        if (nsmev.getButton() == 2) {
            mods |= MouseEvent.BUTTON3_DOWN_MASK; //right
        }
        final int modifiers = mods;

        final int x = nsmev.getClientX();
        final int y = nsmev.getClientY();

        final int clickCount = "click".equals(nsmev.getType()) ? 1 : 0; //$NON-NLS-1$

        final int button;
        if (nsmev.getButton() == 0) {
            button = MouseEvent.BUTTON1; //left
        } else if (nsmev.getButton() == 1) {
            button = MouseEvent.BUTTON2; //middle
        } else if (nsmev.getButton() == 2) {
            button = MouseEvent.BUTTON3; //right
        } else {
            button = MouseEvent.NOBUTTON;
        }

        swingAsyncExec(new Runnable() {

            public void run() {
                try {
                    MozillaMouseEvent ev = new MozillaMouseEvent(source, sourceNode, id, when, modifiers, x, y, clickCount, false, button);
                    JBrowserComponent<?> component = (JBrowserComponent<?>) ev.getSource();
                    component.processEvent(ev);
                } catch (Exception e) {
                    log.error("failed to dispatch awt event", e); //$NON-NLS-1$
                }
            }
        });
    }

    private void fireAWTEvent(nsIDOMKeyEvent nskev) {
        final Component source = browserComponent.getComponent();

        final int id;
        String type = nskev.getType();
        if (type.equals("keydown")) { //$NON-NLS-1$
            id = KeyEvent.KEY_PRESSED;
        } else if (type.equals("keyup")) { //$NON-NLS-1$
            id = KeyEvent.KEY_RELEASED;
        } else if (type.equals("keypress")) { //$NON-NLS-1$
            id = KeyEvent.KEY_TYPED;
        } else {
            //unknown
            log.error("Unknown key event type " + type); //$NON-NLS-1$
            return;
        }

        final Node sourceNode = NodeFactory.getNodeInstance(nskev.getTarget());
        final long when = System.currentTimeMillis();

        int mods = 0;
        if (nskev.getShiftKey()) {
            mods |= KeyEvent.SHIFT_DOWN_MASK;
        }
        if (nskev.getCtrlKey()) {
            mods |= KeyEvent.CTRL_DOWN_MASK;
        }
        if (nskev.getMetaKey()) {
            mods |= KeyEvent.META_DOWN_MASK;
        }
        if (nskev.getAltKey()) {
            mods |= KeyEvent.ALT_DOWN_MASK;
        }
        final int modifiers = mods;

        //nsIDOMNSUIEvent nskev2 = qi(nskev, nsIDOMNSUIEvent.class);
        //long which = nskev2.getWhich();
        final long mozKeyCode = nskev.getKeyCode();
        final long mozCharCode = nskev.getCharCode();
        log.debug("moz keycode=" + mozKeyCode + " charcode=" + mozCharCode); //$NON-NLS-1$ //$NON-NLS-2$

        final int awtKeyCode;
        if (id != KeyEvent.KEY_TYPED) {
            awtKeyCode = (int) mozKeyCode;
        } else {
            awtKeyCode = KeyEvent.VK_UNDEFINED;
        }

        final char awtKeyChar1;
        if (mozCharCode != 0) {
            awtKeyChar1 = (char) mozCharCode;
        } else {
            awtKeyChar1 = 0;
        }

        //send awt event
        swingAsyncExec(new Runnable() {

            public void run() {
                try {
                    MozillaKeyEvent ev = new MozillaKeyEvent(source, sourceNode, id, when, modifiers, awtKeyCode, awtKeyChar1);

                    //mozilla event has either keyCode!=0 xor keyChar!=0
                    //awt events have keyCode!=0 and keyChar!=0 for
                    //printable chars
                    if (awtKeyChar1 == 0 && !ev.isActionKey()) {
                        char awtKeyChar2 = (char) awtKeyCode;

                        if (Character.isLetter(awtKeyChar2) &&
                                (modifiers & KeyEvent.SHIFT_DOWN_MASK) == 0) {
                            awtKeyChar2 = Character.toLowerCase(awtKeyChar2);
                        }

                        ev.setKeyChar(awtKeyChar2);
                    }
                    log.debug("awt id=" + id + " keycode=" + ev.getKeyCode() + " keychar='" + ev.getKeyChar() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

                    JBrowserComponent<?> component = (JBrowserComponent<?>) ev.getSource();
                    component.processEvent(ev);
                } catch (Exception e) {
                    log.error("failed to dispatch awt event", e); //$NON-NLS-1$
                }
            }
        });
    }

    public nsISupports queryInterface(String uuid) {
        return Mozilla.queryInterface(this, uuid);
    }
}
