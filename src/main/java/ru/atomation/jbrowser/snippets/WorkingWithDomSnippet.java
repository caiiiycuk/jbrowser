/**
 * No restrictions for this source.
 *
 * Author: CA>>>
 * Site: atomation.ru
 * Mail: Sashusik_EntXXI@Mail.ru
 */
package ru.atomation.jbrowser.snippets;

import java.awt.Dimension;

import javax.swing.JFrame;

import org.mozilla.browser.MozillaExecutor;
import org.mozilla.interfaces.nsIDOMDocument;
import org.mozilla.interfaces.nsIDOMElement;

import ru.atomation.jbrowser.impl.JBrowserBuilder;
import ru.atomation.jbrowser.impl.JBrowserComponent;
import ru.atomation.jbrowser.impl.JBrowserFrame;
import ru.atomation.jbrowser.interfaces.BrowserAdapter;
import ru.atomation.jbrowser.interfaces.BrowserManager;

/**
 * Snippet explains how to change dom from java code
 * 
 * @author caiiiycuk
 */
public class WorkingWithDomSnippet {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		BrowserManager browserManager = new JBrowserBuilder()
				.buildBrowserManager();

		final JBrowserComponent<JFrame> browser = (JBrowserComponent<JFrame>) browserManager
				.getComponentFactory(JBrowserFrame.class).createBrowser();

		browser.getComponent().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		browser.getComponent().setMinimumSize(new Dimension(320, 240));
		browser.getComponent().setLocationRelativeTo(null);
		browser.getComponent().setVisible(true);

		browser.setText("<html><body><div id='container'></div></body></html>");

		browser.addBrowserListener(new BrowserAdapter() {

			@Override
			public void onLoadingEnded() {
				MozillaExecutor.mozAsyncExec(new Runnable() {
					
					@Override
					public void run() {
						nsIDOMDocument document = browser.getWebBrowser()
								.getContentDOMWindow().getDocument();

						nsIDOMElement containerDiv = document.getElementById("container");
						
						for (int i=0; i<10; ++i) {
							nsIDOMElement createdDiv = document.createElement("div");
							createdDiv.appendChild(document.createTextNode("Div ID#"+i));
							containerDiv.appendChild(createdDiv);
						}						
					}
				});
			}
		});
	}
}
