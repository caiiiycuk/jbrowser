package ru.atomation.jbrowser.impl;

import static org.mozilla.browser.MozillaExecutor.mozAsyncExec;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.atomation.jbrowser.abstracts.AbstractComponentFactory;
import ru.atomation.jbrowser.interfaces.BrowserManager;
import ru.atomation.jbrowser.interfaces.ComponentFacotry;

/**
 * Фактори создает браузер встроенный в canvas // Factory create browser embedded in canvas 
 * @author caiiiycuk
 */
public class JComponentFactory<T extends Component> extends AbstractComponentFactory<T> implements ComponentFacotry<T> {

    private static Log logger = LogFactory.getLog(JBrowserCanvas.class);
    protected final Class<? extends JBrowserComponent<? extends T>> prototype;

    public JComponentFactory(Class<? extends JBrowserComponent<? extends T>> prototype) {
        this(null, prototype);
    }

    public JComponentFactory(BrowserManager browserManager, Class<? extends JBrowserComponent<? extends T>> prototype) {
        setBrowserManager(browserManager);
        this.prototype = prototype;
    }

    @Override
    public JBrowserComponent<? extends T> createBrowser(JBrowserComponent<?> parent, boolean attachOnCreation, final long flags, boolean displayable) {
        final JBrowserComponent<? extends T> instance;
        try {
            Constructor<? extends JBrowserComponent<? extends T>> constructor = prototype.getConstructor(BrowserManager.class);
            instance = constructor.newInstance(browserManager);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return null;
        }

        if (attachOnCreation) {
            instance.onCreatePeer(new Runnable() {

                @Override
                public void run() {
                    if (browserManager.isInitialized()) {
                        Runnable r = new Runnable() {

                            @Override
                            public void run() {
                                browserManager.getWindowCreator().attachBrowser(instance, flags);
                            }
                        };
                        mozAsyncExec(r);
                    }
                }
            });
        }

        instance.onDestroyPeer(new Runnable() {

            @Override
            public void run() {
                if (browserManager.isInitialized()) {
                    Runnable r = new Runnable() {

                        @Override
                        public void run() {
                            browserManager.getWindowCreator().detachBrowser(instance);
                        }
                    };
                    mozAsyncExec(r);
                }
            }
        });

        if (displayable) {
            instance.getComponent().setSize(320, 240);
            instance.getComponent().setVisible(true);
//            if (instance.getComponent() instanceof Window) {
//                ((Window) instance.getComponent()).setLocationRelativeTo(null);
//            }
        }

        return instance;
    }

    protected Dimension getPrefferedSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        return new Dimension(
                (int) (screenSize.getWidth() * 0.75f),
                (int) (screenSize.getHeight() * 0.75f));
    }
}
