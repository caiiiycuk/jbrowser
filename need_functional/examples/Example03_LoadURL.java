package org.mozilla.browser.examples;

import org.mozilla.browser.MozillaWindow;

/**
 * Load a url
 */
public class Example03_LoadURL {
    public static void main(String[] args) throws Exception {
        MozillaWindow win = new MozillaWindow();
        win.setSize(500, 600);
        win.setVisible(true);

        win.load("about:"); //$NON-NLS-1$
    }
}
