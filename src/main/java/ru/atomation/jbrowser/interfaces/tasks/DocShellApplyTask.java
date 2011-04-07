/**
 * No restrictions for this source.
 *
 * Author: CA>>>
 * Site: atomation.ru
 * Mail: Sashusik_EntXXI@Mail.ru
 */
package ru.atomation.jbrowser.interfaces.tasks;

import org.mozilla.interfaces.nsIDocShell;

/**
 * @author caiiiycuk
 */
public interface DocShellApplyTask {

    /**
     * Function to call on a window
     *
     * @param ds docshell (window)
     */
    public void apply(nsIDocShell ds);
}
