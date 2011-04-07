package org.mozilla.browser.impl;

import java.awt.Frame;

import org.mozilla.browser.mt;
import org.mozilla.browser.impl.components.JUsernamePasswordDialog;
import org.mozilla.interfaces.nsIDOMWindow;
import org.mozilla.interfaces.nsIPromptService;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.xpcom.Mozilla;

/**
 * Re-implementation of a mozilla service
 * for handling displaying of various message
 * dialogs in the swing way.
 *
 * http://developer.mozilla.org/en/docs/nsIPromptService
 * http://www.xulplanet.com/references/xpcomref/ifaces/nsIPromptService.html
 */
public class PromptService implements nsIPromptService {

    public void alert(nsIDOMWindow arg0, String arg1, String arg2) {
    }

    public void alertCheck(nsIDOMWindow arg0, String arg1, String arg2, String arg3, boolean[] arg4) {
    }

    public boolean confirm(nsIDOMWindow arg0, String arg1, String arg2) {
        return false;
    }

    public boolean confirmCheck(nsIDOMWindow arg0, String arg1, String arg2, String arg3, boolean[] arg4) {
        return false;
    }

    public int confirmEx(nsIDOMWindow arg0, String arg1, String arg2, long arg3, String arg4, String arg5, String arg6, String arg7, boolean[] arg8) {
        return 0;
    }

    public boolean prompt(nsIDOMWindow arg0, String arg1, String arg2, String[] arg3, String arg4, boolean[] arg5) {
        return false;
    }

    public boolean promptPassword(nsIDOMWindow parent, String dialogTitle, String text, String[] password, String checkMsg, boolean[] checkState) {
        Frame f = null; //FIXME
        JUsernamePasswordDialog dialog =
            new JUsernamePasswordDialog(f,
                                        dialogTitle.length()==0 ? dialogTitle : mt.t("PromptService.Title1"), //$NON-NLS-1$
                                        text,
                                        password[0],
                                        checkMsg!=null,
                                        checkMsg,
                                        checkState[0]);
        boolean ret = dialog.run();
        checkState[0] = dialog.getCheckState();
        password[0] = dialog.getPassword();

        //return true for OK, and false for Cancel
        return ret;
    }

    public boolean promptUsernameAndPassword(nsIDOMWindow parent, String dialogTitle, String text, String[] username, String[] password, String checkMsg, boolean[] checkState)
    {
        Frame f = null; //FIXME
        JUsernamePasswordDialog dialog =
            new JUsernamePasswordDialog(f,
                                        dialogTitle.length()==0 ? dialogTitle : mt.t("PromptService.Title1"), //$NON-NLS-1$
                                        text,
                                        username[0],
                                        password[0],
                                        checkMsg!=null,
                                        checkMsg,
                                        checkState[0]);
        boolean ret = dialog.run();
        checkState[0] = dialog.getCheckState();
        username[0] = dialog.getUsername();
        password[0] = dialog.getPassword();

        //return true for OK, and false for Cancel
        return ret;
    }

    public boolean select(nsIDOMWindow arg0, String arg1, String arg2, long arg3, String[] arg4, int[] arg5) {
        return false;
    }

    public nsISupports queryInterface(String iid) {
        return Mozilla.queryInterface(this, iid);
    }

}
