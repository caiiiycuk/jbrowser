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
import java.awt.event.FocusEvent;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.mozilla.interfaces.nsIRequest;
import org.mozilla.interfaces.nsIWebProgress;

import ru.atomation.jbrowser.impl.JBrowserBuilder;
import ru.atomation.jbrowser.impl.JBrowserComponent;
import ru.atomation.jbrowser.impl.JBrowserFrame;
import ru.atomation.jbrowser.impl.JComponentFactory;
import ru.atomation.jbrowser.interfaces.BrowserAdapter;
import ru.atomation.jbrowser.interfaces.BrowserListener;
import ru.atomation.jbrowser.interfaces.BrowserManager;

/**
 * How to use a {@link BrowserListener}
 * <hr>
 * Как использовать {@link BrowserListener}
 * @author caiiiycuk
 */
public class BrowserListenerSnippet {

    protected static class JExtendedBrowserFrame extends JBrowserFrame {

		private static final long serialVersionUID = -5068732146715074552L;
		protected JTextArea textArea;

        public JExtendedBrowserFrame(BrowserManager browserManager) {
            super(browserManager);

            getContentPane().removeAll();

            GroupLayout layout = new GroupLayout(getContentPane());
            getContentPane().setLayout(layout);

            final JProgressBar progressBar = new JProgressBar();
            final JLabel statusLabel = new JLabel("asdasdasd");
            textArea = new JTextArea();
            final JScrollPane decoratedArea = new JScrollPane(textArea);

            layout.setHorizontalGroup(
                    layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(getBrowserCanvas(), GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                            .addComponent(decoratedArea, 300, 300, 300))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(statusLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                            .addComponent(progressBar, 50, 50, 300)));

            layout.setVerticalGroup(
                    layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup()
                            .addComponent(getBrowserCanvas(), GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                            .addComponent(decoratedArea, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
                        .addGroup(
                            layout.createParallelGroup()
                                .addComponent(statusLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(progressBar, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

            addBrowserListener(new BrowserAdapter() {

                @Override
                public void onBrowserAttached() {
                    addLine("> browser attached");
                }

                @Override
                public void onBrowserDetached() {
                    addLine("> browser detached");
                }

                @Override
                public boolean beforeOpen(String uri) {
                    addLine("> request for open '"+uri+"', true");
                    return true;
                }

                @Override
                public boolean canHandleContent(String aContentType, boolean aIsContentPreferred, String[] aDesiredContentType) {
                    addLine("> canHandleContent '"+aContentType+"', false");
                    return false;
                }

                @Override
                public void focusGained(FocusEvent e) {
                    addLine("> focusGained");
                }

                @Override
                public void focusLost(FocusEvent e) {
                    addLine("> focusLost");
                }

                @Override
                public void onCloseWindow() {
                    addLine("> onCloseWindow");
                }

                @Override
                public void onEnableBackButton(boolean enabled) {
                    addLine("> enableBack, " + enabled);
                }

                @Override
                public void onEnableForwardButton(boolean enabled) {
                    addLine("> enableForward, " + enabled);
                }

                @Override
                public void onEnableReloadButton(boolean enabled) {
                    addLine("> enableReload, " + enabled);
                }

                @Override
                public void onEnableStopButton(boolean enabled) {
                    addLine("> enableStop, " + enabled);
                }

                @Override
                public void onLoadingStarted() {
                    addLine("> loadingStarted");
                }

                @Override
                public void onLoadingEnded() {
                   addLine("> loadingEnded");
                }

                @Override
                public void onSecurityChange(nsIWebProgress aWebProgress, nsIRequest aRequest, long aState) {
                   addLine("> onSecurityChange");
                }

                @Override
                public void onSetSize(int w, int h) {
                   addLine("> sizeChanged to "+w+":"+h);
                }

                @Override
                public void onSetTitle(String title) {
                   addLine("> setTitle to "+title);
                }

                @Override
                public void onSetUrlbarText(String url) {
                   addLine("> urlText "+url);
                }

                @Override
                public void onSetVisible(boolean visibility) {
                   addLine("> visibility changed to "+visibility);
                }

                @Override
                public void onSetStatus(String text) {
                    statusLabel.setText(text);
                }

                @Override
                public void onProgressChange(nsIWebProgress aWebProgress, nsIRequest aRequest, long aCurSelfProgress, long aMaxSelfProgress, long aCurTotalProgress, long aMaxTotalProgress) {
                    progressBar.setMaximum((int) aMaxTotalProgress);
                    progressBar.setValue((int) aCurTotalProgress);
                }
            });
        }

        protected void addLine(String lineToAdd) {
            textArea.setText(textArea.getText() + "\n" + lineToAdd);
        }

    }

    @SuppressWarnings("unchecked")
	public static void main(String[] args) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        BrowserManager browserManager =
                new JBrowserBuilder()
                .setBrowserFactory(new JComponentFactory<JFrame>(JExtendedBrowserFrame.class))
                .buildBrowserManager();

        JBrowserComponent<JFrame> browser =
                (JBrowserComponent<JFrame>) browserManager.getComponentFactory(JExtendedBrowserFrame.class).createBrowser();

        browser.getComponent().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        browser.getComponent().setSize((int) (screenSize.getWidth() * 0.75f),
                (int) (screenSize.getHeight() * 0.75f));
        browser.getComponent().setLocationRelativeTo(null);
        browser.getComponent().setVisible(true);

        browser.setUrl("http://code.google.com/p/jbrowser/");
    }
}
