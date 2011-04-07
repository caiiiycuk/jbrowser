package ru.atomation.jbrowser.interfaces;

import java.awt.Component;

import ru.atomation.jbrowser.impl.JBrowserComponent;
import ru.atomation.jbrowser.impl.JBrowserFrame;

/**
 * Поставщик компонентов JBrowser для приложения // Factory for creating browsers
 *  
 * @author caiiiycuk
 *
 */
public interface ComponentFacotry<C extends Component> {

    /**
     * @param browserManager manager component for this factory
     */
    void setBrowserManager(BrowserManager browserManager);

    /**
     * Создать новый экземлпяр браузера (по умолчанию) // Create new JBrowser instance (default)
     * @return {@link JBrowser}
     */
    JBrowserComponent<? extends C> createBrowser();

    /**
     * Создать новый экземлпяр браузера (по умолчанию) // Create new JBrowser instance (default)
     * @param attachOnCreation if true Mozilla browser will auto create
     * @return {@link JBrowser}
     */
    JBrowserComponent<? extends C> createBrowser(boolean attachOnCreation);

    /**
     * Создат браузер // Create new JBrowser
     * @param attachOnCreation if true Mozilla browser will auto create
     * @param flags flags
     * @return
     */
    JBrowserComponent<? extends C> createBrowser(boolean attachOnCreation, long flags);

    /**
     * Создат браузер // Create new JBrowser
     * @param parent parent
     * @param attachOnCreation if true Mozilla browser will auto create
     * @param flags flags
     * @return
     */
    JBrowserComponent<? extends C> createBrowser(JBrowserComponent<?> parent, boolean attachOnCreation, long flags);

    /**
     * Used when mozila send call-back for creation new window, creating displayble browser
     * is important, for example if you use {@link JBrowserFrame} as {@link JBrowserComponent} you must
     * invoke method setVisible(true) before return {@link JBrowserFrame}
     * <hr>
     * Используется когда мозилла посылает запрос на создание нового окна, создание видимого
     * браузера важно, к примеру если вы используете {@link JBrowserFrame} как {@link JBrowserComponent} то
     * вы должны вызвать метод setVisible(true) прежде чем вернуть {@link JBrowserFrame}
     * @param parent
     * @param attachOnCreation
     * @param flags
     * @return
     */
    JBrowserComponent<? extends C> createDisplayableBrowser(JBrowserComponent<?> parent, boolean attachOnCreation, long flags);
}
