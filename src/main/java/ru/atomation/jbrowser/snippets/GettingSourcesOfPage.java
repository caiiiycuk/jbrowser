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
import java.awt.Toolkit;
import java.io.StringWriter;

import javax.swing.JFrame;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import ru.atomation.jbrowser.impl.JBrowserBuilder;
import ru.atomation.jbrowser.impl.JBrowserCanvas;
import ru.atomation.jbrowser.impl.JBrowserComponent;
import ru.atomation.jbrowser.impl.JComponentFactory;
import ru.atomation.jbrowser.interfaces.BrowserAdapter;
import ru.atomation.jbrowser.interfaces.BrowserManager;

/**
 * Snippet shows how to obtain source code of a page
 * <hr>
 * Фрагмент кода показывает как получить исходный код страницы
 * 
 * @author caiiiycuk
 */
public class GettingSourcesOfPage {

	public static void main(String[] args) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize((int) (screenSize.getWidth() * 0.75f),
				(int) (screenSize.getHeight() * 0.75f));
		frame.setLocationRelativeTo(null);

		BrowserManager browserManager = new JBrowserBuilder()
				.buildBrowserManager();

		JComponentFactory<Canvas> canvasFactory = browserManager
				.getComponentFactory(JBrowserCanvas.class);
		final JBrowserComponent<?> browser = canvasFactory.createBrowser();

        frame.getContentPane().add(browser.getComponent());
        frame.setVisible(true);
        
		browser.setUrl("http://google.com");

		browser.addBrowserListener(new BrowserAdapter() {
			@Override
			public void onLoadingEnded() {
				StringWriter sw = new StringWriter();
				try {
					Document doc = browser.getDocument();
					DOMSource domSource = new DOMSource(doc);
					StreamResult result = new StreamResult(sw);
					TransformerFactory tf = TransformerFactory.newInstance();
					Transformer transformer = tf.newTransformer();
					transformer.transform(domSource, result);

				} catch (TransformerConfigurationException e) {
					e.printStackTrace();
				} catch (TransformerException e) {
					e.printStackTrace();
				} catch (TransformerFactoryConfigurationError e) {
					e.printStackTrace();
				}
				
				System.out.println(sw.getBuffer().toString());
			}
		});

	}
}
