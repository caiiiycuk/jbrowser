/**
 * No restrictions for this source.
 *
 * Author: CA>>>
 * Site: atomation.ru
 * Mail: Sashusik_EntXXI@Mail.ru
 */
package ru.atomation.jbrowser.impl;

import java.io.File;

import javax.swing.JFrame;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.browser.MozillaExecutor;

import ru.atomation.jbrowser.interfaces.BrowserManager;
import ru.atomation.jbrowser.interfaces.BrowserWindowCreator;
import ru.atomation.xulnative.XulExtractor;

/**
 * Entry point for coding of jbrowser aplication, prepare and create BrowserManager.
 * <hr>
 * Любая разработка приложения jbrowser должна начинатся с создания BrowserManager
 * класс предаставляет такие возможности
 * @author caiiiycuk
 */
public class JBrowserBuilder {

    private static Log logger = LogFactory.getLog(JBrowserBuilder.class);
    public static final String XULRUNNER_DEFAULT_DIR = "jbrowser";
    public static final String PROFILE_DEFAULT_DIR = "jbrowser/profile";
    private BrowserWindowCreator browserWindowCreator;
    private JComponentFactory<?> browserFactory;
    /**
     * directory with XULRunner binaries
     */
    private File xulRunnerPath;
    private File profilePath;

    public JBrowserBuilder() {
        browserWindowCreator = new JBrowserWindowCreator();
        browserFactory = new JComponentFactory<JFrame>(JBrowserFrame.class);
    }

    public BrowserWindowCreator getBrowserWindowCreator() {
        return browserWindowCreator;
    }

    public JBrowserBuilder setBrowserWindowCreator(BrowserWindowCreator browserWindowCreator) {
        this.browserWindowCreator = browserWindowCreator;
        return this;
    }

    /**
     * Set directory where xulrunner binaries are located.
     *
     * If set to null, jbrowser will use default xulrunner directory
     *
     * @param path directory with XULRunner binaries
     */
    public JBrowserBuilder setXulRunnerPath(File path) {
        xulRunnerPath = path.getAbsoluteFile();

        if (!isValidPath(xulRunnerPath.getPath())) {
            String oldPath = xulRunnerPath.getPath();
            xulRunnerPath = new File(new File(System.getProperty("java.io.tmpdir")), XULRUNNER_DEFAULT_DIR).getAbsoluteFile();
            logger.error("xullpath[" + oldPath + "] is invalid, using instead [" + xulRunnerPath.getPath() + "]");
        }

        return this;
	}
    
    /**
     * Returns directory, where xulrunner binaries are located.
     *
     * @return directory with XULRunner binaries
     */
    public File getXulRunnerPath() {
        if (xulRunnerPath == null) {
            setXulRunnerPath(new File(System.getProperty("user.dir"), XULRUNNER_DEFAULT_DIR).getAbsoluteFile());
        }

        File xulRunner = new File(xulRunnerPath, "xulrunner/xulrunner");

        if (!xulRunnerPath.exists()) {
            xulRunnerPath.mkdirs();
        }

        if (!(new File(xulRunner, "javaxpcom.jar")).exists()) {
            logger.info("Unpacking mozilla to: " + xulRunnerPath.getPath());
            new XulExtractor().extract(xulRunnerPath);
        }


        return xulRunner;
    }

    /**
     * Set directory where profile should be created.
     *
     * @param profilePath profile directory
     */
    public void setProfilePath(File profilePath) {
        this.profilePath = profilePath.getAbsoluteFile();
    }

    /**
     * Returns directory, where profile is created.
     *
     * @return profile directory
     */
    public File getProfilePath() {
        if (profilePath == null) {
            setProfilePath(new File(System.getProperty("user.dir"), PROFILE_DEFAULT_DIR).getAbsoluteFile());
        }

        if (!profilePath.exists()) {
            profilePath.mkdir();
        }

        return profilePath;
    }

    public BrowserManager buildBrowserManager() {
		JBrowserManager jBrowserManager = new JBrowserManager(this);
		MozillaExecutor.setBrowserMangerInitilized(true);
        return jBrowserManager;
    }

    /**
     * On nix platform xulrunner not work when locatet in directory with
     * non english charchers
     * @param path 
     * @return
     */
    protected boolean isValidPath(String path) {
        return path != null && path.length() > 0 && path.matches("^[\\p{ASCII}]*$");
    }

    public JBrowserBuilder setBrowserFactory(JComponentFactory<?> browserFactory) {
        this.browserFactory = browserFactory;
        return this;
    }
    
      
    public JComponentFactory<?> getBrowserFactory() {
        return browserFactory;
    }

}
