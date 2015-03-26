package org.mozilla.browser.examples;

import static org.mozilla.browser.XPCOMUtils.qi;

import org.mozilla.browser.MozillaExecutor;
import org.mozilla.browser.MozillaWindow;
import org.mozilla.interfaces.nsIDOMEvent;
import org.mozilla.interfaces.nsIDOMEventListener;
import org.mozilla.interfaces.nsIDOMEventTarget;
import org.mozilla.interfaces.nsIDOMWindow;
import org.mozilla.interfaces.nsIDOMWindow2;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsIWebBrowser;
import org.mozilla.xpcom.Mozilla;

/**
 * Block mouse and key events from the user
 */
public class Example12_BlockEvents {

    public static void main(String[] args) {
        final MozillaWindow win = new MozillaWindow();
        win.setSize(500, 600);
        win.load("about:"); //$NON-NLS-1$
        win.setVisible(true);

        MozillaExecutor.mozSyncExec(new Runnable() { public void run() {
            nsIWebBrowser webBrowser = win.getChromeAdapter().getWebBrowser();

            nsIDOMWindow domWin = webBrowser.getContentDOMWindow();
            nsIDOMWindow2 domWin2 = qi(domWin, nsIDOMWindow2.class);
            nsIDOMEventTarget et = domWin2.getWindowRoot();

            MyDOMListener l = new MyDOMListener();
            for (String ev : new String[] {
                    "click", //$NON-NLS-1$
                    "mousemove", //$NON-NLS-1$
                    "mousedown", //$NON-NLS-1$
                    "mouseup", //$NON-NLS-1$
                    "keydown", //$NON-NLS-1$
                    "keyup", //$NON-NLS-1$
                    "keypress", //$NON-NLS-1$
                })
            {
                et.addEventListener(ev, l, true);
            }

        }});
    }

    private static class MyDOMListener implements nsIDOMEventListener {
        public void handleEvent(nsIDOMEvent event) {
            System.err.println("event "+event.getType()); //$NON-NLS-1$

            if (event.getCancelable()) {
                System.err.println("canceling event "+event.getType()); //$NON-NLS-1$
                event.preventDefault();
            }

            event.stopPropagation();
        }
        public nsISupports queryInterface(String uuid) {
            return Mozilla.queryInterface(this, uuid);
        }
    }

}
