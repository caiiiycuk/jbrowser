/**
 *
 */
package org.mozilla.browser.common;

import java.util.StringTokenizer;

public enum Platform {
    OSX("macosx"), Linux("linux"), Win32("win32"), Solaris("solaris"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    public static final Platform platform;
    public static final String arch;

    //http://lopica.sourceforge.net/os.html
    //mapping of known os.name:os.arch combinations to xulrunner location
    //  SunOS:x86 --> solaris-x86

    static {
        String osname = System.getProperty("os.name"); //$NON-NLS-1$
        if ("Mac OS X".equals(osname)) { //$NON-NLS-1$
            platform = Platform.OSX;
        } else if ("Linux".equals(osname)) { //$NON-NLS-1$
            platform = Platform.Linux;
        } else if ("SunOS".equals(osname)) { //$NON-NLS-1$
            platform = Platform.Solaris;
        } else {
            platform = Platform.Win32;
        }

        String osarch = System.getProperty("os.arch"); //$NON-NLS-1$
        String archTemp = osarch;
        for (String x : new String[]{"i386", "i486", "i586", "i686"}) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            if (x.equals(osarch)) {
                archTemp = "x86"; //$NON-NLS-1$
                break;
            }
        }
        arch = archTemp;
    }

    private final String libDir;

    private Platform(String libDir) {
        this.libDir = libDir;
    }

    public String libDir() {
        return libDir;
    }

    public String arch() {
        return arch;
    }

    public static boolean usingGTK2Toolkit() {
        return platform==Linux || platform==Solaris;
    }

    public static long getJavaVersion() {
        String v = System.getProperty("java.version"); //$NON-NLS-1$
        return jdkVersionToNumber(v);
    }

    public static boolean checkJavaVersion(String minVersion, String maxVersion) {
        long current = getJavaVersion();

        if (minVersion!=null && minVersion.length()>0) {
            long min = jdkVersionToNumber(minVersion);
            if (current<min) return false;
        }

        if (maxVersion!=null && maxVersion.length()>0) {
            long max = jdkVersionToNumber(maxVersion);
            if (current>max) return false;
        }

        return true;
    }

    private static long jdkVersionToNumber(String verStr) {
        try {
            String numStr;
            String buildStr;
            int idx = verStr.indexOf("_"); //$NON-NLS-1$
            if (idx>=0) {
                numStr = verStr.substring(0, idx);
                buildStr = verStr.substring(idx+1);
            } else {
                numStr = verStr;
                buildStr = ""; //$NON-NLS-1$
            }

            long num = 0;
            StringTokenizer st = new StringTokenizer(numStr, "."); //$NON-NLS-1$
            long shift = 100*100*100;
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                int n = Integer.parseInt(s);
                num += n*shift;
                shift /= 100;
            }

            if (buildStr.startsWith("b")) buildStr = buildStr.substring(1); //$NON-NLS-1$
            if (buildStr.length()>0) {
                int n = Integer.parseInt(buildStr);
                num += n;
            }

            return num;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Error parsing java version: "+verStr); //$NON-NLS-1$
        }
    }

}
