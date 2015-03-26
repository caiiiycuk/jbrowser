package org.mozilla.browser.examples;

import java.awt.Dimension;
import java.awt.Image;

import org.mozilla.browser.IMozillaWindow;
import org.mozilla.browser.IMozillaWindowFactory;
import org.mozilla.browser.MozillaAutomation;
import org.mozilla.browser.impl.components.JImageButton;

import ru.atomation.jbrowser.impl.JBrowserWindowCreator;

/**
 * Customize creation of popup windows
 */
public class Example15_WindowFactory {

    public static void main(String[] args) {
        throw new UnsupportedOperationException("Not implemented yet");
//
//        //load an icon
//        final Image img = JImageButton.createImageIcon("reloadNormal.png").getImage(); //$NON-NLS-1$
//
//        //set factory for creating new popup windows.
//        //It must be set before creating the first MozillaWindow
//        IMozillaWindowFactory f = new IMozillaWindowFactory() {
//            public IMozillaWindow create(boolean attachBrowser) {
//                final MozillaWindow w = new MozillaWindow(attachBrowser, null, null, ResizingMode.IGNORE_MOZILLA_RESIZE);
//                w.setIconImage(img);
//        		w.setSize(new Dimension(400, 400));
//                return w;
//            }
//        };
//        JBrowserWindowCreator.setWindowFactory(f);
//
//        //use the icon for the main window
//        MozillaWindow win = new MozillaWindow();
//        win.setIconImage(img);
//        win.setSize(500, 600);
//        win.setVisible(true);
//        MozillaAutomation.blockingLoad(win, "about:"); //$NON-NLS-1$
//
//        //open a new popup window. It will have
//        //the same icon
//        win.load("javascript:alert('dialog with the same icon')"); //$NON-NLS-1$
    }
}
