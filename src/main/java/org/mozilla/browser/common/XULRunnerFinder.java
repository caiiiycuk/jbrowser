package org.mozilla.browser.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class XULRunnerFinder {
    static Log log = LogFactory.getLog(XULRunnerFinder.class);

    public static interface FindMethod{
        public File find(String version);
    }

    public static class EnvironmentFinder implements FindMethod {

        private static final String MOZSWING_XULRUNNER_HOME = "MOZSWING_XULRUNNER_HOME"; //$NON-NLS-1$

        public File find(String version) {
            String xuldirEnv = System.getenv(MOZSWING_XULRUNNER_HOME);
            if (xuldirEnv!=null && xuldirEnv.length()>0) {
                File f = new File(xuldirEnv);
                if (checkXULRunner(f, version,
                                   "environment variable: " + //$NON-NLS-1$
                                   MOZSWING_XULRUNNER_HOME + "=" + xuldirEnv)){ //$NON-NLS-1$
                    return f;
                }
            }

            return null;
        }
    }

    public static boolean checkXULRunner(File f, String version, String source) {
        if (!isXULRunnerDir(f)){
            log.warn("Invalid mozswing xulrunner " + source); //$NON-NLS-1$
            return false;
        }

        if (!checkVersion(f, version)) {
            // TODO this should probably be a multiple line log message
            //  but I don't know how to do that.
            log.warn("Mismatched mozswing xulrunner version " + source + //$NON-NLS-1$
                    " requested version: " + version); //$NON-NLS-1$
            return false;
        }

        return true;
    }

    public static class JavaPropertyFinder implements FindMethod {

        private static final String MOZSWING_XULRUNNER_HOME = "mozswing.xulrunner.home"; //$NON-NLS-1$

        public File find(String version) {
            // -Dmozswing.xulrunner.home=... (java property)
            String xuldirProp = System.getProperty(MOZSWING_XULRUNNER_HOME);
            if (xuldirProp!=null && xuldirProp.length()>0) {
                File f = new File(xuldirProp);

                if (checkXULRunner(f, version,
                                   "java property: " + //$NON-NLS-1$
                                   MOZSWING_XULRUNNER_HOME + "=" + xuldirProp)) { //$NON-NLS-1$
                    return f;
                }
            }

            return null;
        }
    }

    public static class JavaLibraryPathFinder implements FindMethod {

        public File find(String version) {
            // -Djava.library.path=... (java property)
            String nativelibdirProp = System.getProperty("java.library.path"); //$NON-NLS-1$
            if (nativelibdirProp!=null && nativelibdirProp.length()>0) {
                StringTokenizer st = new StringTokenizer(nativelibdirProp, File.pathSeparator);
                while(st.hasMoreTokens()){
                    String nativeLibraryDir = st.nextToken();
                    File f = new File(nativeLibraryDir, "xulrunner"); //$NON-NLS-1$

                    // It is pretty likely that this is going to fail so we don't use
                    // the checkXULRunner method which would print out a lot of warnings.
                    if (isXULRunnerDir(f) &&
                        checkVersion(f, version))
                    {
                        return f;
                    }
                }
            }

            return null;
        }
    }

    public static class JavaPreferencesFinder implements FindMethod {

        public static final String PREFERENCE_XULRUNNER_INSTALL_DIRS = "/org/mozdev/mozswing/xulrunner-versions"; //$NON-NLS-1$

        /**
         * Search for a /org/mozdev/mozswing preferences
         * The format should be:
         *  /org/mozdev/mozswing/xulrunner-versions
         *    [VERSION-A]
         *      1=[XULHOMEDIR]
         *      2=[XULHOMEDIR]
         *    [VERSION-B]
         *      1=[XULHOMEDIR]
         *      2=[XULHOMEDIR]
         *
         * @see org.mozilla.browser.common.XULRunnerFinder.FindMethod#find(String)
         */
        public File find(String version) {
            if (!isLaunchedFromWebstart()) return null;

            Preferences node =
                Preferences.systemRoot().node(PREFERENCE_XULRUNNER_INSTALL_DIRS);

			log.debug("Looking for version " + version + " in preference: " + PREFERENCE_XULRUNNER_INSTALL_DIRS); //$NON-NLS-1$ //$NON-NLS-2$
            if(version == null){
                // try every version
                try {
                    String[] childrenNames = node.childrenNames();
                    for (String string : childrenNames) {
                        Preferences versionNode = node.node(string);
                        return find(versionNode, version);
                    }
                } catch (BackingStoreException e) {
                    e.printStackTrace();
                }
            } else {
                Preferences versionNode = node.node(version);
                return find(versionNode, version);
            }

            return null;
        }

        /**
         * Go through the entries in this versionNode and return the first one that
         * is a valid xulrunner directory and has a matching version.
         *
         * @param versionNode
         * @param version
         * @return
         */
        public File find(Preferences versionNode, String version){
            String[] keys;
            try {
                keys = versionNode.keys();
                for (String key : keys) {
                    String directory = versionNode.get(key, null);
                    File f = new File(directory);

                    if (checkXULRunner(f, version,
                                       "java preference: " + //$NON-NLS-1$
                                        versionNode.absolutePath() + "/" + key + "=" + //$NON-NLS-1$ //$NON-NLS-2$
                                        directory))
                    {
                        return f;
                    }
                }
            } catch (BackingStoreException e) {
                log.warn("Problem looking up mozswing xulrunner home in preferences", e); //$NON-NLS-1$
            }

            return null;
        }

    }

    public static class JNLPConfigFinder implements FindMethod {

        public File find(String version) {
            // search for webstart installation
            if (isLaunchedFromWebstart()) {
                System.err.println("Looking for webstart muffin"); //$NON-NLS-1$
                try {
                    // FIXME this probably needs to be done even if this finder is
                    //  not used.  Because the JavaXPCOM code probably tries to load
                    //  in classes.
                    //set the all-permissions rights also for other classloaders,
                    //for more details see
                    //http://saloon.javaranch.com/cgi-bin/ubb/ultimatebb.cgi?ubb=get_topic&f=53&t=000106
                    Policy.setPolicy( new Policy() {
                        public PermissionCollection getPermissions(CodeSource codesource) {
                            Permissions perms = new Permissions();
                            perms.add(new AllPermission());
                            return(perms);
                        }
                        public void refresh(){}
                    });

                    System.err.println("Added All Permissions Policy"); //$NON-NLS-1$

                    // FIXME scytacki: based on the jnlp spec as far as I understand it, the
                    //  jar files referenced by an installer extension are not supposed to be
                    //  on the classpath of the main jnlp.  So the JNLPConfig class would not be
                    //  found if this was enforced by a particular webstart client.
                    //JNLPConfig class is distributed only in a jarfile
                    //that is part of the webstart deployment
                    Class<?> jnlpConfigClazz = Class.forName("org.mozilla.browser.jnlp.JNLPConfig"); //$NON-NLS-1$
                    Method getNativeDir = jnlpConfigClazz.getMethod("getNativeDir", (Class[]) null); //$NON-NLS-1$

                    System.err.println("Calling getNativeDir method"); //$NON-NLS-1$
                    String jnlpDir = (String) getNativeDir.invoke(null, (Object[]) null);
                    System.err.println("found jnlp config muffin: " + jnlpDir); //$NON-NLS-1$
                    if (jnlpDir!=null && jnlpDir.length()>0) {
                        String rel = "native/${OS}/xulrunner"; //$NON-NLS-1$
                        rel = rel.replace("${OS}", Platform.platform.libDir()); //$NON-NLS-1$

                        File f = new File(jnlpDir, rel);
                        if(checkXULRunner(f, version, "webstart muffin")){ //$NON-NLS-1$
                            return f;
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed searching JNLP location", e); //$NON-NLS-1$
                    return null;
                }
            }

            return null;
        }
    }

    public static class JarFolderAndParentsFinder implements FindMethod {

        public File find(String version) {
            // heuristic to search in neighbourhood dirs
            String[] reldirs = new String[] {
                "native/${OS}/xulrunner", //$NON-NLS-1$
                "native/${OS}-${ARCH}/xulrunner", //$NON-NLS-1$
                "xulrunner-build/build/compile/mozilla/dist/bin" //$NON-NLS-1$
            };
            try {
                //get jar location
                URL u = XULRunnerFinder.class.getResource(""); //$NON-NLS-1$

                String uproto = u.getProtocol();
                if ("jar".equals(uproto)) { //$NON-NLS-1$
                    //extract inner url
                    String upath = u.getPath();
                    upath = upath.substring(0, upath.indexOf('!'));
                    u = new URL(upath);
                    uproto = u.getProtocol();
                }
                File base = null;
                if ("file".equals(uproto)) { //$NON-NLS-1$
                    base = new File(u.toURI());
                }

                //try resolve relative dirs
                while (base!=null) {
                    for (String rel : reldirs) {
                        rel = rel.replace("${OS}", Platform.platform.libDir()); //$NON-NLS-1$
                        rel = rel.replace("${ARCH}", Platform.platform.arch()); //$NON-NLS-1$
                        File f = new File(base, rel);

                        // it is pretty likely this is going to fail we shouldn't
                        // use checkXULRunner which will print out warning messages.
                        if (!isXULRunnerDir(f)) {
                            continue;
                        }

                        if (!checkVersion(f, version)){
                            continue;
                        }

                        // If we are here then this directory is good.
                        return f;
                    }
                    base = base.getParentFile();
                }
            } catch (Exception e) {
                log.error("failed to locate xulrunner", e); //$NON-NLS-1$
            }

            return null;
        }
    }

    /**
     * Look for the xulrunner, if the MozillaConfig.getXULRunnerHome is set then
     * that will be used first.
     *
     * On OSX this will be called in the gui thread which should not be blocked.
     * So for example you should not call JOptionPane methods which block.
     *
     * @return
     */
    public static File findXULRunner() {
        // TODO This should be extensible so an application using mozswing
        //   can add its own ways of finding XULRunner
        FindMethod [] findMethods = {
            new EnvironmentFinder(),
            new JavaPropertyFinder(),
            new JavaLibraryPathFinder(),
            new JavaPreferencesFinder(),
            new JNLPConfigFinder(),
            new JarFolderAndParentsFinder(),
        };

        return findXULRunner(findMethods, null);
    }

    public static File findXULRunner(FindMethod [] findMethods, String version) {
        for(FindMethod findMethod: findMethods){
    		log.debug("Looking for xulrunner home with: " + findMethod.getClass().getName()); //$NON-NLS-1$
            File file = findMethod.find(version);
            if (file != null){
                // we found a XUL directory
    			log.debug("Found xulrunner home: " + file); //$NON-NLS-1$
                return file;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked") //$NON-NLS-1$
    private static boolean isLaunchedFromWebstart() {
        try {
            Class smClass = Class.forName( "javax.jnlp.ServiceManager"); //$NON-NLS-1$
            if (smClass==null) return false;
            Method lookup = smClass.getMethod("lookup", String.class); //$NON-NLS-1$
            if (lookup==null) return false;
            Object bs = lookup.invoke(null, "javax.jnlp.BasicService"); //$NON-NLS-1$
            return bs!=null;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isXULRunnerDir(File f1) {
        File f2;
        File f3;
        switch (Platform.platform) {
        case Linux:
        case Solaris:
            f2 = new File(f1, "libxul.so"); //$NON-NLS-1$
            f3 = new File(f1, "libjavaxpcomglue.so"); //$NON-NLS-1$
            break;
        case OSX:
            //libxul.dylib is not there
            f2 = new File(f1, "xulrunner"); //$NON-NLS-1$
            f3 = new File(f1, "libjavaxpcomglue.jnilib"); //$NON-NLS-1$
            break;
        case Win32:
        default:
            f2 = new File(f1, "xul.dll"); //$NON-NLS-1$
            f3 = new File(f1, "javaxpcomglue.dll"); //$NON-NLS-1$
            break;
        }

        return
            f1.isDirectory() &&
            f1.canRead() &&
            f2.isFile() &&
            f2.canRead() &&
            f3.isFile() &&
            f3.canRead();
    }

    /**
     * Currently this uses a version.properties file located in the xulrunner home
     * directory.  That file will probably only be there if this xulrunner has been
     * installed by mozswing.  This method could be expanded to execute the xulrunner
     * binary in the folder and have it print out the version.
     *
     * @param xulrunnerHome
     * @return
     */
    public static String getVersion(File xulrunnerHome) {
        try {
            File xulrunnerVersionFile = new File(xulrunnerHome, "version.properties"); //$NON-NLS-1$
            FileInputStream xulrunnerVersionIS = new FileInputStream(xulrunnerVersionFile);
            Properties versionProps = new Properties();
            versionProps.load(xulrunnerVersionIS);
            return versionProps.getProperty("version"); //$NON-NLS-1$
        } catch (IOException e) {
            log.warn("Cannot determine version of: " + xulrunnerHome); //$NON-NLS-1$
            return null;
        }
    }

    /**
     * If version is null this will return true.
     *
     * This method is intended to be used as the different findMethods look for a valid
     * xulrunner.  If they don't care about the version they will just pass in null.
     *
     * @param xulrunnerHome
     * @param version
     * @return
     */
    public static boolean checkVersion(File xulrunnerHome, String version)
    {
        if(version == null){
            return true;
        }

        String homeVersion = getVersion(xulrunnerHome);
        if(homeVersion == null){
            return false;
        }

        return homeVersion.equals(version);
    }

}
