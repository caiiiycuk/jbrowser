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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import ru.atomation.jbrowser.impl.JBrowserCanvas;
import ru.atomation.jbrowser.impl.JBrowserComponent;
import ru.atomation.jbrowser.impl.JTabbedComponentFactory;
import ru.atomation.jbrowser.interfaces.BrowserAdapter;

/**
 * Defaut implementation for abstarct JTabbedComponentFacotry, used for snippets
 * @author caiiiycuk
 */
public class DefaultTabbedComponentFactory extends JTabbedComponentFactory<Canvas> {

    public DefaultTabbedComponentFactory(JTabbedPane tabContainer) {
        super(tabContainer, JBrowserCanvas.class);
        tabContainer.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    @Override
    protected JComponent createBrowserComponent(JBrowserComponent<? extends Canvas> browser) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(browser.getComponent(), BorderLayout.CENTER);
        return panel;
    }

    @Override
    protected JComponent createTabComponent(JBrowserComponent<? extends Canvas> browser, JComponent browserComponent) {
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

}
