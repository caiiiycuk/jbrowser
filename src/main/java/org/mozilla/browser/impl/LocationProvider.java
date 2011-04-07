/*******************************************************************
 *
 * Licensed Materials - Property of IBM
 *
 * AJAX Toolkit Framework 6-28-496-8128
 *
 * (c) Copyright IBM Corp. 2006 All Rights Reserved.
 *
 * U.S. Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 *
 *******************************************************************/
package org.mozilla.browser.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.browser.common.Platform;
import org.mozilla.xpcom.IAppFileLocProvider;

public class LocationProvider implements IAppFileLocProvider {

    static Log log = LogFactory.getLog(LocationProvider.class);

    private File libXULPath;
    private File profile;
    private File history;

    public LocationProvider(File aBinPath, File aProfileDir)
    throws IOException {
        libXULPath = aBinPath;
        profile = aProfileDir;

        if (!libXULPath.exists() || !libXULPath.isDirectory()) {
            throw new FileNotFoundException("libxul directory specified is not valid: " //$NON-NLS-1$
                    + libXULPath.getAbsolutePath());
        }
        if (profile != null && (!profile.exists() || !profile.isDirectory())) {
            throw new FileNotFoundException("profile directory specified is not valid: " //$NON-NLS-1$
                    + profile.getAbsolutePath());
        }

        // create history file
        if (profile != null) {
            setupProfile();
        }
    }

    private void setupProfile() throws IOException {
        history = new File(profile, "history.dat"); //$NON-NLS-1$
        if (!history.exists()) {
            history.createNewFile();
        }
    }

    public File getFile(String aProp, boolean[] aPersistent) {
        File file = null;
        if (aProp.equals("GreD") || aProp.equals("GreComsD")) { //$NON-NLS-1$ //$NON-NLS-2$
//			file = new File(grePath);
            file = libXULPath;
            if (aProp.equals("GreComsD")) { //$NON-NLS-1$
                file = new File(file, "components"); //$NON-NLS-1$
            }
        }
        else if (aProp.equals("MozBinD") ||  //$NON-NLS-1$
            aProp.equals("CurProcD") || //$NON-NLS-1$
            aProp.equals("ComsD"))  //$NON-NLS-1$
        {
            file = libXULPath;
            if (aProp.equals("ComsD")) { //$NON-NLS-1$
                file = new File(file, "components"); //$NON-NLS-1$
            }
        }
        else if (aProp.equals("ProfD") ||
                aProp.equals("ProfDS") ||
                aProp.equals("ProfLD") ||
                aProp.equals("ProfLDS"))
        {
            return profile;
        }
        else if (aProp.equals("UAppData")) { //$NON-NLS-1$
            return profile;
        }
        else if (aProp.equals("UMimTyp") && profile!=null) {	//$NON-NLS-1$
            file = new File(profile, "mimeTypes.rdf"); //$NON-NLS-1$
        }
        else if (aProp.equals("UHist")) {	//$NON-NLS-1$
            return history;
        }
        else if (aProp.equals("XCurProcD")) { //$NON-NLS-1$
            if (profile!=null) {
                return profile;
            } else {
                String userDir = System.getProperty("user.dir"); //$NON-NLS-1$
                return new File(userDir);
            }
        }
//        else {
//            log.debug("LocationProvider::getFile() => unhandled property = " + aProp);
//        }

        return file;
    }

    public File[] getFiles(String aProp) {
        File[] files = null;
        if (aProp.equals("APluginsDL")) { //$NON-NLS-1$
            if(Platform.platform == Platform.OSX){
                log.debug("Adding global plugins for OSX"); //$NON-NLS-1$
                files = new File[2];
                files[0] = new File(libXULPath, "plugins"); //$NON-NLS-1$
                files[1] = new File("/Library/Internet Plug-Ins"); //$NON-NLS-1$
            } else {
                files = new File[1];
                files[0] = new File(libXULPath, "plugins"); //$NON-NLS-1$
            }
//        } else {
//            log.debug("LocationProvider::getFiles() => unhandled property = " + aProp);
        }

        return files;
    }

}
