package org.mozilla.browser.impl;

import java.io.File;

import org.mozilla.browser.mt;
import org.mozilla.xpcom.IXREAppData;

/**
 * Describes entry point for XUL Application
 * (content of the application.ini file)
 *
 * http://developer.mozilla.org/en/docs/XUL_Application_Packaging
 */
public class XREAppData implements IXREAppData {

    public String getVendor() {
        return mt.t("XREAppData.Vendor"); //$NON-NLS-1$
    }

    public String getName() {
        return mt.t("XREAppData.Name"); //$NON-NLS-1$
    }

    public int getFlags() {
        return 0;
    }

    public File getDirectory() {
        return null;
    }

    public String getID() {
        return mt.t("XREAppData.ID"); //$NON-NLS-1$
    }

    public String getVersion() {
        return mt.t("XREAppData.Version"); //$NON-NLS-1$
    }

    public String getBuildID() {
        return mt.t("XREAppData.BuildID"); //$NON-NLS-1$
    }

    public String getCopyright() {
        return mt.t("XREAppData.Copyright"); //$NON-NLS-1$
    }

}
