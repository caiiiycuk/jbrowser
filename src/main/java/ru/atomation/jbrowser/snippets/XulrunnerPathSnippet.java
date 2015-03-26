/**
 * No restrictions for this source.
 *
 * Author: CA>>>
 * Site: atomation.ru
 * Mail: Sashusik_EntXXI@Mail.ru
 */
package ru.atomation.jbrowser.snippets;

import java.io.File;

import javax.swing.JFrame;

import ru.atomation.jbrowser.impl.JBrowserBuilder;
import ru.atomation.jbrowser.impl.JBrowserCanvas;
import ru.atomation.jbrowser.impl.JBrowserComponent;
import ru.atomation.jbrowser.interfaces.BrowserManager;


/**
 * Snippet show how to configure xulrunner working directory. If you dont use
 * xulrunner-*.jar (native providers), then you must manually install xulrunner
 * in specified working directory.
 * <hr>
 * Фрагмент показывает как сконфигурировать рабочую директорию для xulrunner. Если
 * вы не используется xulrunner-*.jar, тогда вы должны самостоятельно установить
 * xulrunner в сконфигурированную директорию.
 * @author caiiiycuk
 */
public class XulrunnerPathSnippet {

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 480);
        frame.setLocationRelativeTo(null);

        BrowserManager browserManager =
                new JBrowserBuilder().setXulRunnerPath(new File("Specify your folder"))
                .buildBrowserManager();

        JBrowserComponent<?> browser = browserManager.getComponentFactory(JBrowserCanvas.class).createBrowser();
        frame.getContentPane().add(browser.getComponent());
        frame.setVisible(true);

        browser.setUrl("http://code.google.com/p/jbrowser/");
    }
}
