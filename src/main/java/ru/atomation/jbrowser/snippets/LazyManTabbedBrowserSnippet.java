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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import ru.atomation.jbrowser.impl.JBrowserBuilder;
import ru.atomation.jbrowser.impl.JBrowserCanvas;
import ru.atomation.jbrowser.impl.JBrowserComponent;
import ru.atomation.jbrowser.impl.JTabbedComponentFactory;
import ru.atomation.jbrowser.interfaces.BrowserAdapter;

/**
 * How to create custom tabbed browser
 * <hr>
 * Как создать свой браузер с табами
 * @author caiiiycuk
 */
public class LazyManTabbedBrowserSnippet {

    public static void main(String[] args) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize((int) (screenSize.getWidth() * 0.75f),
                (int) (screenSize.getHeight() * 0.75f));
        frame.setLocationRelativeTo(null);

        JTabbedPane tabContainer = new JTabbedPane();
        tabContainer.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        JTabbedComponentFactory<Canvas> canvasFactory =
                new JTabbedComponentFactory<Canvas>(tabContainer, JBrowserCanvas.class) {

                    @Override
                    protected JComponent createBrowserComponent(
							JBrowserComponent<? extends Canvas> browser) {
                        JPanel panel = new JPanel(new BorderLayout());
                        panel.add(browser.getComponent(), BorderLayout.CENTER);
                        return panel;
                    }

                    @Override
                    protected JComponent createTabComponent(
							JBrowserComponent<? extends Canvas> browser,
							JComponent browserComponent) {
                        final JLabel label = new JLabel("Loading please wait");

                        label.addMouseListener(new MouseAdapter() {

                            @Override
                            public void mouseClicked(MouseEvent evt) {
                                int index = getTabContainer().indexOfTabComponent(label);
                                if (index != -1) {
                                    if (evt.getButton() != MouseEvent.BUTTON1) {
                                        getTabContainer().remove(index);
                                    } else {
                                        getTabContainer().setSelectedIndex(index);
                                    }
                                }
                            }
                        });

                        browser.addBrowserListener(new BrowserAdapter() {

                            @Override
                            public void onSetTitle(String title) {
                                label.setText(title);
                            }

                            @Override
                            public void onCloseWindow() {
                                int index = getTabContainer().indexOfTabComponent(label);
                                if (index != -1) {
                                    getTabContainer().remove(index);
                                }
                            }
                        });

                        label.setPreferredSize(new Dimension(100, 15));
                        return label;
                    }

                    @Override
                    protected void tabCreated(int index, JComponent browserComponent, JComponent tabComponent) {
                        if (index != -1) {
                            getTabContainer().setSelectedIndex(index);
                        }
                    }

                };

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
