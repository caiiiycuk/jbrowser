package org.mozilla.browser.examples;

import org.mozilla.browser.MozillaWindow;

/**
 * Create a browser window
 */
public class Example01_CreateWindow {
    public static void main(String[] args) throws Exception {
        MozillaWindow win = new MozillaWindow();
        win.setSize(500, 600);
        win.load("about:"); //$NON-NLS-1$
        win.setVisible(true);
    }
}
