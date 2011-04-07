package ru.atomation.jbrowser.abstracts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsIWebBrowserChrome;
import org.mozilla.xpcom.Mozilla;

import ru.atomation.jbrowser.impl.JBrowserComponent;
import ru.atomation.jbrowser.interfaces.BrowserWindowCreator;
import ru.atomation.jbrowser.interfaces.ComponentFacotry;
import ru.atomation.jbrowser.interfaces.NativeBrowser;

public abstract class AbstractBrowserWindowCreator implements BrowserWindowCreator {

    protected final List<ComponentFacotry<?>> interceptors;
    protected ComponentFacotry<?> browserFactory;
    protected boolean attachOnCreation;

    public AbstractBrowserWindowCreator() {
        interceptors = Collections.synchronizedList(new ArrayList<ComponentFacotry<?>>());
        attachOnCreation = false;
    }

    @Override
    public void addInterceptor(ComponentFacotry<?> factory) {
        synchronized (interceptors) {
            interceptors.add(factory);
        }
    }

    @Override
    public void removeInterceptor(ComponentFacotry<?> factory) {
        synchronized (interceptors) {
            interceptors.remove(factory);
        }
    }

    @Override
    public nsIWebBrowserChrome createChromeWindow(nsIWebBrowserChrome parent,
            long flags) {
        if (parent instanceof JBrowserComponent<?>) {
            synchronized (interceptors) {
                for (int i = interceptors.size() - 1; i > -1; i++) {
                    JBrowserComponent<?> created = interceptors.get(i).createBrowser((JBrowserComponent<?>) parent, attachOnCreation, flags);
                    if (created != null && created instanceof NativeBrowser) {
                        return (NativeBrowser) created;
                    }
                }
            }
        }

        return createWindow(parent, flags);
    }

    @Override
    public nsISupports queryInterface(String aIID) {
        return Mozilla.queryInterface(this, aIID);
    }

    @Override
    public void setBrowserFactory(ComponentFacotry<?> factory) {
        this.browserFactory = factory;
    }
    
    @Override
    public ComponentFacotry<?> getBrowserFactory() {
        return browserFactory;
    }

    public void setAttachOnCreation(boolean attachOnCreation) {
        this.attachOnCreation = attachOnCreation;
    }

}
