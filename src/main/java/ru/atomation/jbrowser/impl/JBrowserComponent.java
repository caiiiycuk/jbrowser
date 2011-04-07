package ru.atomation.jbrowser.impl;

import java.awt.AWTEvent;
import java.awt.Component;

import ru.atomation.jbrowser.interfaces.Browser;
import ru.atomation.jbrowser.interfaces.BrowserManager;
import ru.atomation.jbrowser.interfaces.DisplayableComponent;
import ru.atomation.jbrowser.interfaces.NativeBrowser;

/**
 * Браузер встроенный в компонент Swing // Browser embedded in swing component
 * @author caiiiycuk
 */
public interface JBrowserComponent<T extends Component> extends DisplayableComponent, Browser, NativeBrowser {

    /**
     * @return See {@link java.awt.Component}
     */
    T getComponent();

    /**
     * @return Handle of awt component
     */
    long getHandle();

    /**
     * @return {@link BrowserManager} which control thiw browser component
     */
    BrowserManager getBrowserManager();

    /**
     * delegate of java.awt.Component.processEvent
     * @param ev
     */
    void processEvent(AWTEvent ev);

}
