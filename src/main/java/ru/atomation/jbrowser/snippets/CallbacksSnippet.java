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

import ru.atomation.jbrowser.impl.JBrowserBuilder;
import ru.atomation.jbrowser.impl.JBrowserComponent;
import ru.atomation.jbrowser.impl.JBrowserFrame;
import ru.atomation.jbrowser.interfaces.BrowserAdapter;
import ru.atomation.jbrowser.interfaces.BrowserManager;

/**
 * Snippet explains how to invoke java code
 * 
 * @author caiiiycuk
 */
public class CallbacksSnippet {

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

		browser.setText("<html><body>" +
				"<div id='container'></div>" +
				"<button onclick=\"javascript: window.location = 'call:#syso';\">Invoke java code</button><br/>"+
				"</body></html>");

		browser.addBrowserListener(new BrowserAdapter() {
			@Override
			public boolean beforeOpen(String uri) {
				if (uri.startsWith("call:")) {
					System.out.println("uri");
					return false;
				}

				return super.beforeOpen(uri);
			}
		});
	}
}
