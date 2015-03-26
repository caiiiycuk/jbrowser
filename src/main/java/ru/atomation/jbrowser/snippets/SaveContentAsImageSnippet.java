/**
 * No restrictions for this source.
 *
 * Author: CA>>>
 * Site: atomation.ru
 * Mail: Sashusik_EntXXI@Mail.ru
 */
package ru.atomation.jbrowser.snippets;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import org.apache.commons.io.FileUtils;

import ru.atomation.jbrowser.impl.JBrowserBuilder;
import ru.atomation.jbrowser.impl.JBrowserCanvas;
import ru.atomation.jbrowser.impl.JBrowserComponent;
import ru.atomation.jbrowser.impl.JComponentFactory;
import ru.atomation.jbrowser.interfaces.BrowserManager;

/**
 * Snippet creates JFrame window with JBrowser and opens
 * jbrowser web site. In menu you can save browser content as image
 * <hr>
 * Фрагмент кода создает окно, со встроенным браузером и открывает 
 * веб сайт jbrowser.
 * В меню вы можете сохранить содержимое браузера как изображение
 * 
 * @author caiiiycuk
 */
public class SaveContentAsImageSnippet {

    public static void main(String[] args) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize((int) (screenSize.getWidth() * 0.75f),
                (int) (screenSize.getHeight() * 0.75f));
        frame.setLocationRelativeTo(null);

        BrowserManager browserManager =
                new JBrowserBuilder().buildBrowserManager();

        JComponentFactory<Canvas> canvasFactory = browserManager.getComponentFactory(JBrowserCanvas.class);
        final JBrowserComponent<?> browser = canvasFactory.createBrowser();
        
        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("Images");
        MenuItem menuItem = new MenuItem("Get image");
        menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					File imageFile = new File("image-"
							+ System.currentTimeMillis() + ".png");
					byte[] image = browser.asImage();
					FileUtils.writeByteArrayToFile(imageFile, image);
					System.out.println("Image saved: "
							+ imageFile.getAbsoluteFile().toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
        
		menuBar.add(menu);
		menu.add(menuItem);
		frame.setMenuBar(menuBar);
        
        frame.getContentPane().add(browser.getComponent());
        frame.setVisible(true);

        browser.setUrl("http://code.google.com/p/jbrowser/");
    }
}
