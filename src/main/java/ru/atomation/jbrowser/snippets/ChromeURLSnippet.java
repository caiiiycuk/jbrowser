/**
 * No restrictions for this source.
 *
 * Author: CA>>>
 * Site: atomation.ru
 * Mail: Sashusik_EntXXI@Mail.ru
 */
package ru.atomation.jbrowser.snippets;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import ru.atomation.jbrowser.impl.JBrowserBuilder;
import ru.atomation.jbrowser.impl.JBrowserComponent;
import ru.atomation.jbrowser.impl.JTabbedComponentFactory;
import ru.atomation.jbrowser.interfaces.ChromePages;
import ru.atomation.jbrowser.snippets.helpers.DefaultTabbedComponentFactory;

/**
 * How to use a chrome urls {@link ChromePages}
 * <hr>
 * Как использовать chrome urls {@link ChromePages}
 * @author caiiiycuk
 */
public class ChromeURLSnippet {

    public static void main(String[] args) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize((int) (screenSize.getWidth() * 0.75f),
                (int) (screenSize.getHeight() * 0.75f));
        frame.setLocationRelativeTo(null);

        JTabbedPane tabContainer = new JTabbedPane();

        JTabbedComponentFactory<Canvas> canvasFactory =
                new DefaultTabbedComponentFactory(tabContainer);

        new JBrowserBuilder().setBrowserFactory(canvasFactory).buildBrowserManager();

        frame.getContentPane().add(tabContainer, BorderLayout.CENTER);
        frame.setVisible(true);

        JBrowserComponent<?> browser;
//--open all known chrome pages
        browser = canvasFactory.createBrowser();
        browser.setUrl(ChromePages.Addons);

        browser = canvasFactory.createBrowser();
        browser.setUrl(ChromePages.Downloads);

        browser = canvasFactory.createBrowser();
        browser.setUrl(ChromePages.ErrorConsole);


        browser = canvasFactory.createBrowser();
        browser.setUrl(ChromePages.SavedPasswords);


        browser = canvasFactory.createBrowser();
        browser.setUrl(ChromePages.CharacterEncodingCustomizeList);
        browser = canvasFactory.createBrowser();
        browser.setUrl(ChromePages.ToolbarsCustomize);

        browser = canvasFactory.createBrowser();
        browser.setUrl(ChromePages.FindDialog);
        browser = canvasFactory.createBrowser();
        browser.setUrl(ChromePages.FilePrint);
        browser = canvasFactory.createBrowser();
        browser.setUrl(ChromePages.FilePageSetup);
        browser = canvasFactory.createBrowser();
        browser.setUrl(ChromePages.FilePrintPreview);

        browser = canvasFactory.createBrowser();
        browser.setUrl(ChromePages.Config);

        browser = canvasFactory.createBrowser();
        browser.setUrl(ChromePages.Certificates);
    }
}
