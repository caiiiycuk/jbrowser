/**
 * No restrictions for this source.
 *
 * Author: CA>>>
 * Site: atomation.ru
 * Mail: Sashusik_EntXXI@Mail.ru
 */
package ru.atomation.jbrowser.snippets;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import ru.atomation.jbrowser.impl.JBrowserBuilder;
import ru.atomation.jbrowser.impl.JBrowserComponent;
import ru.atomation.jbrowser.impl.JBrowserFrame;
import ru.atomation.jbrowser.interfaces.BrowserManager;

/**
 * Snippet creates JFrame window with JBrowser and opens
 * jbrowser web site
 * <hr>
 * Фрагмент кода создает окно, со встроенным браузером и открывает
 * веб сайт jbrowser
 * 
 * @author caiiiycuk
 */
public class LazyManGettingStartedSnippet {

    @SuppressWarnings("unchecked")
	public static void main(String[] args) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        BrowserManager browserManager =
                new JBrowserBuilder().buildBrowserManager();

        final JBrowserComponent<JFrame> browser =
                (JBrowserComponent<JFrame>) browserManager.getComponentFactory(JBrowserFrame.class).createBrowser();

        browser.getComponent().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        browser.getComponent().setSize((int) (screenSize.getWidth() * 0.75f),
                (int) (screenSize.getHeight() * 0.75f));
        browser.getComponent().setLocationRelativeTo(null);
        browser.getComponent().setVisible(true);

        browser.setUrl("http://code.google.com/p/jbrowser/");
    }
}
