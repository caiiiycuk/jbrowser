/**
 * No restrictions for this source.
 *
 * Author: CA>>>
 * Site: atomation.ru
 * Mail: Sashusik_EntXXI@Mail.ru
 */
package ru.atomation.jbrowser.impl;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

/**
 * Abstract factory for creation browser`s in JTabbedPane
 * <hr>
 * Абстракатная фактори для создания бразуреов в JTabbedPane
 * 
 * <p> See also: <p> {@link LazyManTabbedBrowserSnippet}
 * @author caiiiycuk
 */
public abstract class JTabbedComponentFactory<C extends Component> extends JComponentFactory<C> {

    protected final JTabbedPane tabContainer;

    public JTabbedComponentFactory(JTabbedPane tabContainer, Class<? extends JBrowserComponent<? extends C>> prototype) {
        super(prototype);
        this.tabContainer = tabContainer;
    }

    @Override
	public JBrowserComponent<? extends C> createBrowser(JBrowserComponent<?> parent, boolean attachOnCreation, long flags, boolean displayable) {
        JBrowserComponent<? extends C> browser = super.createBrowser(parent, attachOnCreation, flags, displayable);

        //i don`t know why swing request JPanel, Canvas work wrong
        JComponent browserComponent = createBrowserComponent(browser);
        JComponent tabComponent = createTabComponent(browser, browserComponent);

        tabContainer.addTab("", browserComponent);

        if (tabComponent != null) {
            tabContainer.setTabComponentAt(tabContainer.getTabCount() - 1,
                    tabComponent);
        }

        tabCreated(tabContainer.getTabCount() - 1, browserComponent, tabComponent);

        return browser;
    }

    /**
     * Creating tab component for browser (usually JPanel)
     * @param browser browser for embedding in component
     * @return JComponent
     */
    protected abstract JComponent createBrowserComponent(JBrowserComponent<? extends C> browser);

    /**
     * Create tab component
     * @param browser
     * @param browserComponent (from createBrowserComponent)
     * @return may be null
     */
    protected abstract JComponent createTabComponent(JBrowserComponent<? extends C> browser, JComponent browserComponent);

    /**
     * Post processor for created tabs
     * @param index - index of created tab
     * @param browserComponent (from createBrowserComponent)
     * @param tabComponent (from createTabComponent)
     */
    protected abstract void tabCreated(int index, JComponent browserComponent, JComponent tabComponent);

    public JTabbedPane getTabContainer() {
        return tabContainer;
    }

}
