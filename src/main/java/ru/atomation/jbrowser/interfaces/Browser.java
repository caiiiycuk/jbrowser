package ru.atomation.jbrowser.interfaces;

import org.w3c.dom.Document;

/**
 * Интерфейс который реализует браузер по умолчанию // Interface of default browser
 * @author caiiiycuk
 *
 */
public interface Browser {

    /**
     * Действительно ли создан нативный браузер // Are native browser exists
     * @return
     */
    boolean isBrowserExisist();

    /**
     * Отобразить предыдущею станицу // Send back action to browser
     * @return
     */
    boolean back();

    /**
     * Отобразить следующею станицу // Send forward action to browser
     * @return
     */
    boolean forward();

    /**
     * Остановить загрузку текущей страницы // Stop loading for current page
     * @return
     */
    boolean stop();

    /**
     * Загрузить страницу заново // Reload current page
     * @return
     */
    boolean refresh();

    /**
     * Отобразить html страницу // Show html content
     * <pre>Example: browser.setText("<html>Hellow, World!</html>");</pre>
     * @param content html code
     * @return
     */
    boolean setText(String content);

    /**
     * Открыть адрресс // Load page by url
     * <pre>Example: browser.setUrl("http://google.com");</pre>
     * @param url page address
     * @return
     */
    boolean setUrl(String url);

    /**
     * Получить адресс текущей странички // Get url of current page
     * @return
     */
    String getUrl();

    /**
     * Получить иконку текущей страничик // Get favicon of current page
     * @return url address of favicon image
     */
    String getFavIcon();

    /**
     * Получить заголовок страницы // Get title of current page
     * @return
     */
    String getTitle();

    /**
     * Вернуть текущее содержание браузера как изображение // Return browser content as image
     * @return byte array with png image data or null if error
     */
    byte[] asImage();
    
    /**
     * @return true если браузер владеет фокусом клавиатуры // true if browser has keyboard focus
     */
    boolean isFocusOwner();

    /**
     * Запросить фокус клавиатруы // request for keqyboard focus
     */
    void requestFocus();

    /**
     * Принудительно высвободить все системные ресурсы занимаемые браузером, в обычных условиях вызывать не требуется // Release all system resources, normally not need to invoke manualy
     * @return
     */
    boolean disposeBrowser();

    /**
     * Add new listener, see {@link BrowserListener}
     * @param listener
     */
    void addBrowserListener(BrowserListener listener);

    /**
     * Remove listener, see {@link BrowserListener}
     * @param listener
     */
    void removeBrowserListener(BrowserListener listener);

    /**
     * get w3c document of loaded page
     * @return
     */
    Document getDocument();

}
