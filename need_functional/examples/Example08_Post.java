package org.mozilla.browser.examples;

import org.mozilla.browser.MozillaAutomation;
import org.mozilla.browser.MozillaWindow;

/**
 * Submit form using POST method
 */
public class Example08_Post {
    public static void main(String[] args) throws Exception {
        MozillaWindow win = new MozillaWindow();
        win.setSize(500, 600);
        win.setVisible(true);

        boolean failed =
            MozillaAutomation.
            blockingLoad(win,
                     //url
                     "http://search.yahoo.com/search", //$NON-NLS-1$
                     //POST data
                     "p=mozswing&ei=UTF-8"); //$NON-NLS-1$

        if (failed)
            System.err.println("load failed"); //$NON-NLS-1$
        else
            System.err.println("load succeeded"); //$NON-NLS-1$
    }
}
