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

import org.mozilla.browser.MozillaAutomation;
import org.mozilla.browser.MozillaExecutor;

import ru.atomation.jbrowser.impl.JBrowserBuilder;
import ru.atomation.jbrowser.impl.JBrowserComponent;
import ru.atomation.jbrowser.impl.JBrowserFrame;
import ru.atomation.jbrowser.interfaces.BrowserAdapter;
import ru.atomation.jbrowser.interfaces.BrowserManager;

/**
 * Snippet explains how to execute custom java script
 * 
 * @author caiiiycuk
 */
public class ExecuteJavaScriptSnippet {

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

		browser.setText("<html><body>You should see alert message</body></html>");

		browser.addBrowserListener(new BrowserAdapter() {

			@Override
			public void onLoadingEnded() {
				MozillaExecutor.mozAsyncExec(new Runnable() {
					
					@Override
					public void run() {
				        //
				        // Execute javascript
				        //
				        browser.setUrl("javascript: alert('Looking good');");
					}
				});
				

		        // 
		        // Or (same thing)
		        // 
		        MozillaAutomation.executeJavascript(browser, "alert('Looking good');");
			}
		});
	}
}
