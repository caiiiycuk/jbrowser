package org.mozilla.browser.examples;

import org.mozilla.browser.MozillaWindow;

/**
 * Create a hidden browser window
 */
public class Example02_CreateHiddenWindow {
    public static void main(String[] args) throws Exception {
        final MozillaWindow win = new MozillaWindow();
        //initalize window to attach mozilla window,
        //but do not make the window visible
        win.addNotify();

        win.load("about:"); //$NON-NLS-1$

        //uncomment this to see the mozilla window
        //win.setVisible(true);
    }
}
