package org.mozilla.browser.examples;

import org.mozilla.browser.MozillaWindow;

/**
 * Load web page from HTML string
 */
public class Example04_LoadHTML {
    public static void main(String[] args) throws Exception {
        MozillaWindow win = new MozillaWindow();
        win.setSize(500, 600);
        win.setVisible(true);

        String s1 =
            "<html>" + //$NON-NLS-1$
            "<h3>HTML content</h3>" + //$NON-NLS-1$
            "<li><a href=\"about:\">about:</a>" + //$NON-NLS-1$
            "<li><a href=\"about:config\">about:config</a>" + //$NON-NLS-1$
            "</html>"; //$NON-NLS-1$
        win.loadHTML(s1);
    }
}
