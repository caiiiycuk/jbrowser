/**
 * No restrictions for this source.
 *
 * Author: CA>>>
 * Site: atomation.ru
 * Mail: Sashusik_EntXXI@Mail.ru
 */
package ru.atomation.jbrowser.abstracts;

import static org.mozilla.interfaces.nsIWebBrowserChrome.CHROME_DEFAULT;

import java.awt.Component;

import ru.atomation.jbrowser.impl.JBrowserComponent;
import ru.atomation.jbrowser.impl.JComponentFactory;
import ru.atomation.jbrowser.interfaces.BrowserManager;
import ru.atomation.jbrowser.interfaces.ComponentFacotry;

/**
 * Абстракция над {@link JComponentFactory}
 * @author caiiiycuk
 */
public abstract class AbstractComponentFactory<C extends Component> implements ComponentFacotry<C> {

    protected BrowserManager browserManager;

    @Override
    public void setBrowserManager(BrowserManager browserManager) {
        this.browserManager = browserManager;
    }

    public BrowserManager getBrowserManager() {
        return browserManager;
    }

    @Override
    public JBrowserComponent<? extends C> createBrowser() {
        return createBrowser(true);
    }

    @Override
    public JBrowserComponent<? extends C> createBrowser(boolean attachOnCreation) {
        return createBrowser(attachOnCreation, CHROME_DEFAULT);
    }

    @Override
    public JBrowserComponent<? extends C> createBrowser(boolean attachOnCreation, long flags) {
        return createBrowser(null, attachOnCreation, flags);
    }

    @Override
    public JBrowserComponent<? extends C> createBrowser(JBrowserComponent<?> parent, boolean attachOnCreation, long flags) {
        return createBrowser(parent, attachOnCreation, flags, false);
    }

    @Override
    public JBrowserComponent<? extends C> createDisplayableBrowser(JBrowserComponent<?> parent, boolean attachOnCreation, long flags) {
        return createBrowser(parent, attachOnCreation, flags, true);
    }

    protected abstract JBrowserComponent<? extends C> createBrowser(JBrowserComponent<?> parent, boolean attachOnCreation, long flags, boolean displayable);
}
