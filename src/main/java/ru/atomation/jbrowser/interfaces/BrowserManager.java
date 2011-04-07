package ru.atomation.jbrowser.interfaces;

import java.awt.Component;

import ru.atomation.jbrowser.impl.JBrowserComponent;
import ru.atomation.jbrowser.impl.JBrowserInitializationStatus;
import ru.atomation.jbrowser.impl.JComponentFactory;

/**
 * JBrowser manager
 */
public interface BrowserManager {

    /**
     * Returns true, if jbrowser was successfully initialized.
     *
     * @return true if jbrowser was successfully initialized.
     */
    boolean isInitialized();

    /**
     * Returns exception that occurred during jbrowser initialization or null.
     *
     * @return initialization exception
     */
    Throwable getError();

    /**
     * Returns jbrowser initialization status, one of NONE, INITIALIZED, FAILED.
     *
     * @return jbrowser initialization status
     */
    JBrowserInitializationStatus getStatus();

    /**
     * Add factory to process callback`s when mozilla wants to open a new window
     */
    void registerBrowserWindowCreator(ComponentFacotry<?> factory);

    /**
     * Remove factory (apposite for registerBrowserWindowCreator)
     */
    void removeBrowserWindowCreator(ComponentFacotry<?> factory);

    /**
     * @return {@link BrowserWindowCreator}
     */
    BrowserWindowCreator getWindowCreator();

    /**
     * Returns a short XULRunner, java, platform configuration summary.
     * Used e.g. in error reports.
     *
     * @return short summary of XULRunner, Java, platform configuration
     */
    String getManagerSummary();

    /**
     * @return see {@link BrowserConfig}
     */
    BrowserConfig getBrowserConfig();

    /**
     * get component factory
     * @param <T> component for embedding
     * @param prototype JBrowserComponent prototype
     * @return factory for JBrowserComponent<T>
     */
    <T extends Component> JComponentFactory<T> getComponentFactory(Class<? extends JBrowserComponent<T>> prototype);
    
    /**
     * get default browser factory
     * @return factory for JBrowserComponent<T>
     */
    <T extends Component> ComponentFacotry<T> getDefaultFactory();

}
