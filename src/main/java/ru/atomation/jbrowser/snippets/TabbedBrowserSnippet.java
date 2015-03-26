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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import ru.atomation.jbrowser.impl.JBrowserBuilder;
import ru.atomation.jbrowser.impl.JBrowserCanvas;
import ru.atomation.jbrowser.impl.JBrowserComponent;
import ru.atomation.jbrowser.impl.JComponentFactory;
import ru.atomation.jbrowser.interfaces.BrowserAdapter;

/**
 * How to create custom tabbed browser
 * <hr>
 * Как создать свой браузер с табами
 *
 * <p> See also: <p> {@link LazyManTabbedBrowserSnippet}
 * @author caiiiycuk
 */
public class TabbedBrowserSnippet {

    @SuppressWarnings("serial")
	protected static class CloasableTab extends JPanel {
        private JLabel label;
        private JButton close;
        private Runnable closeAction;

        public CloasableTab(Runnable closeAction) {
            super();

            this.closeAction = closeAction;
            
            setOpaque(false);

            label = new JLabel();
            close = new JButton("X");

            close.setBorder(new EmptyBorder(0,0,0,0));
            close.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    if (CloasableTab.this.closeAction != null) {
                        CloasableTab.this.closeAction.run();
                    }
                }
            });

            GroupLayout groupLayout = new GroupLayout(this);
            setLayout(groupLayout);

            groupLayout.setHorizontalGroup(groupLayout.createSequentialGroup()
                    .addComponent(label, 100, 100, 100)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(close, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));

            groupLayout.setVerticalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(label)
                    .addComponent(close));
        }

        public JLabel getLabel() {
            return label;
        }

    }

    protected static class TabbedComponentFactory extends JComponentFactory<Canvas> {

        protected final JTabbedPane tabContainer;

        public TabbedComponentFactory(JTabbedPane tabContainer) {
            super(JBrowserCanvas.class);
            this.tabContainer = tabContainer;
        }

		@Override
		@SuppressWarnings("unchecked")
		public JBrowserComponent<Canvas> createBrowser(JBrowserComponent<?> parent, boolean attachOnCreation, long flags, boolean displayable) {
            JBrowserComponent<Canvas> browser = (JBrowserComponent<Canvas>) super.createBrowser(parent, attachOnCreation, flags, displayable);

            //i don`t know why swing request JPanel, Canvas work wrong
            final JPanel browserPanel = createBrowserPanel(browser);
            browser.addBrowserListener(new BrowserAdapter() {
                @Override
                public void onSetTitle(String title) {
                    int index = tabContainer.indexOfComponent(browserPanel);
                    CloasableTab tabComponent = (CloasableTab) tabContainer.getTabComponentAt(index);
                    tabComponent.getLabel().setText(title);
                }

                @Override
                public void onCloseWindow() {
                    int index = tabContainer.indexOfComponent(browserPanel);
                    if (index != -1) {
                        tabContainer.remove(index);
                    }

                    if (tabContainer.getTabCount() == 0) {
                        System.exit(0);
                    }
                }
            });

            CloasableTab cloasableTab = new CloasableTab(
                    new Runnable() {

                        @Override
                        public void run() {
                            int index = tabContainer.indexOfComponent(browserPanel);
                            tabContainer.remove(index);
                        }
                    });

            tabContainer.addTab("New tab", browserPanel);
            tabContainer.setTabComponentAt(tabContainer.getTabCount() -1,
                    cloasableTab);
            tabContainer.setSelectedIndex(tabContainer.getTabCount() -1);
            return browser;
        }

        protected JPanel createBrowserPanel(JBrowserComponent<?> browser) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(browser.getComponent(), BorderLayout.CENTER);
            return panel;
        }
    }

    public static void main(String[] args) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize((int) (screenSize.getWidth() * 0.75f),
                (int) (screenSize.getHeight() * 0.75f));
        frame.setLocationRelativeTo(null);

        JTabbedPane tabContainer = new JTabbedPane();
        tabContainer.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        JComponentFactory<Canvas> canvasFactory = new TabbedComponentFactory(tabContainer);

        new JBrowserBuilder().setBrowserFactory(canvasFactory).buildBrowserManager();

        frame.getContentPane().add(tabContainer, BorderLayout.CENTER);
        frame.setVisible(true);

        JBrowserComponent<?> browser1 = canvasFactory.createBrowser();
        browser1.setUrl("http://www.google.com/search?q=Java+Browser");

        JBrowserComponent<?> browser2 = canvasFactory.createBrowser();
        browser2.setUrl("http://yandex.ru/yandsearch?text=Java Browser");

        JBrowserComponent<?> browser3 = canvasFactory.createBrowser();
        browser3.setUrl("http://atomation.ru");
    }
}
