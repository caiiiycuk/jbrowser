package org.mozilla.browser.examples;

import org.mozilla.browser.MozillaAutomation;
import org.mozilla.browser.MozillaWindow;

/**
 * Load a web page blocking while loading is finished
 */
public class Example05_BlockingLoad {
    public static void main(String[] args) throws Exception {
        MozillaWindow win = new MozillaWindow();
        win.setSize(500, 600);
        win.setVisible(true);

        System.err.println("loading started"); //$NON-NLS-1$
        MozillaAutomation.blockingLoad(win, "http://www.yahoo.com"); //$NON-NLS-1$
        System.err.println("loading finished"); //$NON-NLS-1$
    }
}
