package org.mozilla.browser.examples;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.mozilla.browser.MozillaAutomation;
import org.mozilla.browser.MozillaWindow;

/**
 * Listen for key and mouse events inside mozilla area.
 *
 * Implementation listens for mozilla DOM events,
 * wraps them into AWT events and forwards from mozilla
 * thread into AWT's event dispatching thread.
 *
 * Optionally, it is possible to listen directly
 * for mouse/key events on mozilla thread.
 * To do this, register a nsIDOMWindowLister in
 * nsIDOMDocument or nsIDOMWindow2
 */
public class Example07_AWTEvents {
    public static void main(String[] args) throws Exception {
        MozillaWindow win = new MozillaWindow();
        win.setSize(500, 600);
        win.setVisible(true);

        MozillaAutomation.blockingLoad(win, "about:"); //$NON-NLS-1$

        win.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                System.err.println("received "+e); //$NON-NLS-1$
            }
            public void mouseEntered(MouseEvent e) {
                System.err.println("received "+e); //$NON-NLS-1$
            }
            public void mouseExited(MouseEvent e) {
                System.err.println("received "+e); //$NON-NLS-1$
            }
            public void mousePressed(MouseEvent e) {
                System.err.println("received "+e); //$NON-NLS-1$
            }
            public void mouseReleased(MouseEvent e) {
                System.err.println("received "+e); //$NON-NLS-1$
            }
        });

        win.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                System.err.println("received "+e); //$NON-NLS-1$
            }
            public void keyReleased(KeyEvent e) {
                System.err.println("received "+e); //$NON-NLS-1$
            }
            public void keyTyped(KeyEvent e) {
                System.err.println("received "+e); //$NON-NLS-1$
            }
        });

    }
}
