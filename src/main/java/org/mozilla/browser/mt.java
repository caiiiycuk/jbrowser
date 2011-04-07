package org.mozilla.browser;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Message translation
 */
public class mt {
    private static final String BUNDLE_NAME = "org.mozilla.browser.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(BUNDLE_NAME);

    private mt() {
    }

    /**
     * message translation
     *
     * @param key text to translate
     * @return translated text
     */
    public static String t(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
