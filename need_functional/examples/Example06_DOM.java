package org.mozilla.browser.examples;

import org.mozilla.browser.MozillaAutomation;
import org.mozilla.browser.MozillaWindow;
import org.mozilla.browser.impl.DOMUtils;
import org.w3c.dom.Document;

/**
 * Load a document and dump its DOM tree
 */
public class Example06_DOM {
    public static void main(String[] args) throws Exception {
        final MozillaWindow win = new MozillaWindow();
        win.addNotify();

        MozillaAutomation.blockingLoad(win, "about:"); //$NON-NLS-1$

        Document doc = win.getDocument();
        DOMUtils.writeDOMToStream(doc, System.out);

        System.exit(0);
    }
}
