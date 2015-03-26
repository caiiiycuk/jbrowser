/**
 * No restrictions for this source.
 *
 * Author: CA>>>
 * Site: atomation.ru
 * Mail: Sashusik_EntXXI@Mail.ru
 */
package ru.atomation.jbrowser.snippets.helpers;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import ru.atomation.jbrowser.impl.JBrowserBuilder;
import ru.atomation.jbrowser.impl.JBrowserComponent;
import ru.atomation.jbrowser.impl.JTabbedComponentFactory;

/**
 * How to create custom tabbed browser
 * <hr>
 * Как создать свой браузер с табами
 * @author caiiiycuk
 */
public class DefaultTabbedComponentSnippet {

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

        JBrowserComponent<?> browser1 = canvasFactory.createBrowser();
        browser1.setUrl("http://www.google.com/search?q=Java+Browser");

        JBrowserComponent<?> browser2 = canvasFactory.createBrowser();
        browser2.setUrl("http://yandex.ru/yandsearch?text=Java Browser");

        JBrowserComponent<?> browser3 = canvasFactory.createBrowser();
        browser3.setUrl("http://code.google.com/p/jbrowser/");
    }
}
