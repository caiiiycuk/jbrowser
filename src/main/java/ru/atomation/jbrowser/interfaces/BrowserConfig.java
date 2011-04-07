/**
 * No restrictions for this source.
 *
 * Author: CA>>>
 * Site: atomation.ru
 * Mail: Sashusik_EntXXI@Mail.ru
 */
package ru.atomation.jbrowser.interfaces;

import ru.atomation.jbrowser.impl.JBrowserComponent;

/**
 * Functions for configuring the mozilla browser.
 * @author caiiiycuk
 */
public interface BrowserConfig {

    /**
     * Enable loading of images (in all windows).
     */
    void enableImages();

    /**
     * Disable loading of images (in all windows).
     */
    void disableImages();

    /**
     * Enable loading of images in the given window.
     *
     * @param jBrowserComponent where to enable loading of images
     */
    void enableImages(JBrowserComponent<?> jBrowserComponent);

    /**
     * Disable loading of images in the given window.
     *
     * @param jBrowserComponent where to disable loading of images
     */
    void disableImages(JBrowserComponent<?> jBrowserComponent);

    /**
     * Returns true, if loading of images in webpages is enabled.
     *
     * @return true if enabled loading of images in webpages
     */
    boolean isEnabledImages();

    /**
     * Enable Javascript execution (in all windows).
     */
    void enableJavascript();

    /**
     * Disable Javascript execution (in all windows).
     */
    void disableJavascript();

    /**
     * Enable Javascript execution in the given window.
     *
     * @param jBrowserComponent where to enable javascript
     */
    void enableJavascript(JBrowserComponent<?> jBrowserComponent);

    /**
     * Disable Javascript execution in the given window.
     *
     * @param jBrowserComponent where to disable javascript
     */
    void disableJavascript(JBrowserComponent<?> jBrowserComponent);

    /**
     * Returns true, if Javascript execution in webpages
     * is enabled.
     *
     * @return true if Javascript execution in webpages
     * is enabled
     */
    boolean isEnabledJavascript();

    /**
     * Configures browser to use proxy setting by defining parameters
     * for all proxy types. Use null, -1 if a particular proxy type
     * should not be set.
     *
     * @param httpHost host (ip address) for HTTP proxy
     * @param httpPort port for HTTP proxy
     * @param sslHost host (ip address) for HTTPS proxy
     * @param sslPort port for HTTPS proxy
     * @param ftpHost host (ip address) for FTP proxy
     * @param ftpPort port for FTP proxy
     * @param socksHost host (ip address) for SOCKS proxy
     * @param socksPort port for SOCKS proxy
     * @param noProxyFor list of host to bypass with proxies.
     *   The same format as in Firefox configuration dialog
     *   (comma separated)
     */
    void setManualProxy(final String httpHost, final int httpPort,
            final String sslHost, final int sslPort,
            final String ftpHost, final int ftpPort,
            final String socksHost, final int socksPort,
            final String noProxyFor);

    /**
     * Configures browser to use automatic proxy settings.
     * (usually located in a file called proxy.pac)
     *
     * @param configURL url of a proxy.pac file
     */
    void setAutomaticProxy(final String configURL);

    /**
     * Disables using of all proxy types (HTTP, HTTPS, SOCKS, FTP).
     */
    void disableProxy();

    /**
     * Cleans browser cache.
     */
    void cleanCache();

    /**
     * Cleans browser cookies.
     */
    void cleanCookies();
}
