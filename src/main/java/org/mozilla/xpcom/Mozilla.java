package org.mozilla.xpcom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import org.mozilla.interfaces.nsIComponentManager;
import org.mozilla.interfaces.nsIComponentRegistrar;
import org.mozilla.interfaces.nsILocalFile;
import org.mozilla.interfaces.nsIServiceManager;
import org.mozilla.interfaces.nsISupports;

public class Mozilla implements IMozilla, IGRE, IXPCOM, IJavaXPCOMUtils,
		IXPCOMError {
	private static Mozilla mozillaInstance = new Mozilla();
	private static final String JAVAXPCOM_JAR = "javaxpcom.jar";
	private IMozilla mozilla = null;
	private IGRE gre = null;
	private IXPCOM xpcom = null;
	private IJavaXPCOMUtils jxutils = null;

	public static Mozilla getInstance() {
		return mozillaInstance;
	}

	public static File getGREPathWithProperties(
			GREVersionRange[] paramArrayOfGREVersionRange,
			Properties paramProperties) throws FileNotFoundException {
		File localFile = null;

		String str1 = System.getProperty("GRE_HOME");
		if (str1 != null) {
			try {
				localFile = new File(str1).getCanonicalFile();
			} catch (IOException localIOException) {
				throw new FileNotFoundException("cannot access GRE_HOME");
			}
			if (!localFile.exists()) {
				throw new FileNotFoundException("GRE_HOME doesn't exist");
			}
			return localFile;
		}

		str1 = System.getProperty("USE_LOCAL_GRE");
		if (str1 != null) {
			return null;
		}

		if (paramProperties == null) {
			paramProperties = new Properties();
		}
		paramProperties.setProperty("javaxpcom", "1");

		String str2 = System.getProperty("os.name").toLowerCase();
		if (str2.startsWith("mac os x"))
			localFile = getGREPathMacOSX(paramArrayOfGREVersionRange);
		else if (str2.startsWith("windows")) {
			localFile = getGREPathWindows(paramArrayOfGREVersionRange,
					paramProperties);
		} else {
			localFile = getGREPathUnix(paramArrayOfGREVersionRange,
					paramProperties);
		}

		if (localFile == null) {
			throw new FileNotFoundException("GRE not found");
		}

		return localFile;
	}

	private static File getGREPathMacOSX(
			GREVersionRange[] paramArrayOfGREVersionRange) {
		File localFile = findGREBundleFramework();
		if (localFile != null) {
			return localFile;
		}

		String str = System.getProperty("user.home");
		if (str != null) {
			localFile = findGREFramework(str, paramArrayOfGREVersionRange);
			if (localFile != null) {
				return localFile;
			}

		}

		return findGREFramework("", paramArrayOfGREVersionRange);
	}

	private static File findGREBundleFramework() {
		try {
			URL[] arrayOfURL = new URL[1];
			arrayOfURL[0] = new File("/System/Library/Java/").toURL();
			URLClassLoader localURLClassLoader = new URLClassLoader(arrayOfURL);
			Class localClass = Class.forName(
					"com.apple.cocoa.foundation.NSBundle", true,
					localURLClassLoader);

			Method localMethod1 = localClass.getMethod("mainBundle", null);
			Object localObject = localMethod1.invoke(null, null);

			if (localObject != null) {
				Method localMethod2 = localClass.getMethod(
						"privateFrameworksPath", null);

				String str = (String) localMethod2.invoke(localObject, null);

				if (str.length() != 0) {
					File localFile1 = new File(str, "XUL.framework");
					if (localFile1.isDirectory()) {
						File localFile2 = new File(localFile1, "libxpcom.dylib");
						if (localFile2.canRead()) {
							File localFile3 = localFile2.getCanonicalFile()
									.getParentFile();

							File localFile4 = new File(localFile3,
									"javaxpcom.jar");
							if (localFile4.canRead()) {
								return localFile3;
							}
						}
					}
				}
			}
		} catch (Exception localException) {
		}
		return null;
	}

	private static File findGREFramework(String paramString,
			GREVersionRange[] paramArrayOfGREVersionRange) {
		File localFile1 = new File(paramString
				+ "/Library/Frameworks/XUL.framework/Versions");

		if (!localFile1.exists()) {
			return null;
		}
		File[] arrayOfFile = localFile1.listFiles();
		for (int i = 0; i < arrayOfFile.length; i++) {
			if (checkVersion(arrayOfFile[i].getName(),
					paramArrayOfGREVersionRange)) {
				File localFile2 = new File(arrayOfFile[i], "libxpcom.dylib");

				File localFile3 = new File(arrayOfFile[i], "javaxpcom.jar");
				if ((localFile2.canRead()) && (localFile3.canRead())) {
					return arrayOfFile[i];
				}
			}
		}

		return null;
	}

	private static File getGREPathWindows(
			GREVersionRange[] paramArrayOfGREVersionRange,
			Properties paramProperties) {
		String str = "HKEY_CURRENT_USER\\Software\\mozilla.org\\GRE";
		File localFile = getGREPathFromRegKey(str, paramArrayOfGREVersionRange,
				paramProperties);
		if (localFile == null) {
			str = "HKEY_LOCAL_MACHINE\\Software\\mozilla.org\\GRE";
			localFile = getGREPathFromRegKey(str, paramArrayOfGREVersionRange,
					paramProperties);
		}

		return localFile;
	}

	private static File getGREPathFromRegKey(String paramString,
			GREVersionRange[] paramArrayOfGREVersionRange,
			Properties paramProperties) {
		File localFile1;
		try {
			localFile1 = File.createTempFile("jx_registry", null);
		} catch (IOException localIOException) {
			return null;
		}

		try {
			Process localProcess = Runtime.getRuntime().exec(
					"regedit /e \"" + localFile1.getPath() + "\" \""
							+ paramString + "\"");

			localProcess.waitFor();
		} catch (Exception localException) {
		}

		File localFile2 = null;
		if (localFile1.length() != 0L) {
			localFile2 = getGREPathFromRegistryFile(localFile1.getPath(),
					paramString, paramArrayOfGREVersionRange, paramProperties);
		}

		localFile1.delete();
		return localFile2;
	}

	private static File getGREPathFromRegistryFile(String paramString1,
			String paramString2, GREVersionRange[] paramArrayOfGREVersionRange,
			Properties paramProperties) {
		INIParser localINIParser;
		try {
			localINIParser = new INIParser(paramString1,
					Charset.forName("UTF-16"));
		} catch (Exception localException) {
			return null;
		}

		Iterator localIterator = localINIParser.getSections();
		while (localIterator.hasNext()) {
			String str1 = (String) localIterator.next();

			int i = paramString2.length();
			if (str1.length() <= i) {
				continue;
			}

			String str2 = str1.substring(i + 1);

			if (str2.indexOf('\\') != -1) {
				continue;
			}

			String str3 = localINIParser.getString(str1, "\"Version\"");
			if (str3 == null) {
				continue;
			}
			str3 = str3.substring(1, str3.length() - 1);
			if (!checkVersion(str3, paramArrayOfGREVersionRange))
				continue;
			Object localObject2;
			if (paramProperties != null) {
				int j = 1;
				Object localObject1 = paramProperties.propertyNames();
				while ((j != 0)
						&& (((Enumeration) localObject1).hasMoreElements())) {
					localObject2 = (String) ((Enumeration) localObject1)
							.nextElement();
					String str5 = localINIParser.getString(str1, "\""
							+ (String) localObject2 + "\"");
					if (str5 == null) {
						j = 0;
						continue;
					}

					String str6 = paramProperties
							.getProperty((String) localObject2);
					if (!str5.equals("\"" + str6 + "\"")) {
						j = 0;
					}
				}

				if (j == 0) {
					continue;
				}
			}
			String str4 = localINIParser.getString(str1, "\"GreHome\"");
			if (str4 == null)
				continue;
			str4 = str4.substring(1, str4.length() - 1);
			File localObject1 = new File(str4);
			if (((File) localObject1).exists()) {
				localObject2 = new File((File) localObject1, "xpcom.dll");
				if (((File) localObject2).canRead()) {
					return localObject1;
				}
			}

		}

		return (File) (File) null;
	}

	private static File getGREPathUnix(
			GREVersionRange[] paramArrayOfGREVersionRange,
			Properties paramProperties) {
		File localFile = null;

		String str = System.getProperty("MOZ_GRE_CONF");
		if (str != null) {
			localFile = getPathFromConfigFile(str, paramArrayOfGREVersionRange,
					paramProperties);
			if (localFile != null) {
				return localFile;
			}

		}

		str = System.getProperty("user.home");
		if (str != null) {
			localFile = getPathFromConfigFile(str + File.separator
					+ ".gre.config", paramArrayOfGREVersionRange,
					paramProperties);

			if (localFile != null) {
				return localFile;
			}

			localFile = getPathFromConfigDir(str + File.separator + ".gre.d",
					paramArrayOfGREVersionRange, paramProperties);

			if (localFile != null) {
				return localFile;
			}

		}

		localFile = getPathFromConfigFile("/etc/gre.conf",
				paramArrayOfGREVersionRange, paramProperties);
		if (localFile != null) {
			return localFile;
		}

		localFile = getPathFromConfigDir("/etc/gre.d",
				paramArrayOfGREVersionRange, paramProperties);
		return localFile;
	}

	private static File getPathFromConfigFile(String paramString,
			GREVersionRange[] paramArrayOfGREVersionRange,
			Properties paramProperties) {
		INIParser localINIParser;
		try {
			localINIParser = new INIParser(paramString);
		} catch (Exception localException) {
			return null;
		}

		Iterator localIterator = localINIParser.getSections();
		while (localIterator.hasNext()) {
			String str1 = (String) localIterator.next();

			if (!checkVersion(str1, paramArrayOfGREVersionRange))
				continue;
			Object localObject1;
			Object localObject2;
			if (paramProperties != null) {
				int i = 1;
				localObject1 = paramProperties.propertyNames();
				while ((i != 0)
						&& (((Enumeration) localObject1).hasMoreElements())) {
					localObject2 = (String) ((Enumeration) localObject1)
							.nextElement();
					String str3 = localINIParser.getString(str1,
							(String) localObject2);
					if (str3 == null) {
						i = 0;
						continue;
					}

					if (!str3.equals(paramProperties
							.getProperty((String) localObject2))) {
						i = 0;
					}
				}

				if (i == 0) {
					continue;
				}
			}
			String str2 = localINIParser.getString(str1, "GRE_PATH");
			if (str2 != null) {
				localObject1 = new File(str2);
				if (((File) localObject1).exists()) {
					localObject2 = new File((File) localObject1, "libxpcom.so");
					if (((File) localObject2).canRead()) {
						return (File) localObject1;
					}
				}
			}
		}

		return (File) (File) null;
	}

	private static File getPathFromConfigDir(String paramString,
			GREVersionRange[] paramArrayOfGREVersionRange,
			Properties paramProperties) {
		File localFile1 = new File(paramString);
		if (!localFile1.isDirectory()) {
			return null;
		}

		File localFile2 = null;
		File[] arrayOfFile = localFile1.listFiles();
		for (int i = 0; (i < arrayOfFile.length) && (localFile2 == null); i++) {
			if (!arrayOfFile[i].getName().endsWith(".conf")) {
				continue;
			}
			localFile2 = getPathFromConfigFile(arrayOfFile[i].getPath(),
					paramArrayOfGREVersionRange, paramProperties);
		}

		return localFile2;
	}

	private static boolean checkVersion(String paramString,
			GREVersionRange[] paramArrayOfGREVersionRange) {
		for (int i = 0; i < paramArrayOfGREVersionRange.length; i++) {
			if (paramArrayOfGREVersionRange[i].check(paramString)) {
				return true;
			}
		}
		return false;
	}

	public void initialize(File paramFile) throws XPCOMInitializationException {
		File localFile = new File(paramFile, "javaxpcom.jar");
		if (!localFile.exists()) {
			throw new XPCOMInitializationException(
					"Could not find javaxpcom.jar in " + paramFile);
		}

		URL[] arrayOfURL = new URL[1];
		try {
			arrayOfURL[0] = localFile.toURL();
		} catch (MalformedURLException localMalformedURLException) {
			throw new XPCOMInitializationException(localMalformedURLException);
		}
		URLClassLoader localURLClassLoader = new URLClassLoader(arrayOfURL,
				getClass().getClassLoader());
		try {
			Class localClass1 = Class.forName(
					"org.mozilla.xpcom.internal.MozillaImpl", true,
					localURLClassLoader);

			this.mozilla = (IMozilla) localClass1.newInstance();

			Class localClass2 = Class.forName(
					"org.mozilla.xpcom.internal.GREImpl", true,
					localURLClassLoader);

			this.gre = ((IGRE) localClass2.newInstance());

			Class localClass3 = Class.forName(
					"org.mozilla.xpcom.internal.XPCOMImpl", true,
					localURLClassLoader);

			this.xpcom = ((IXPCOM) localClass3.newInstance());

			Class localClass4 = Class.forName(
					"org.mozilla.xpcom.internal.JavaXPCOMMethods", true,
					localURLClassLoader);

			this.jxutils = ((IJavaXPCOMUtils) localClass4.newInstance());
		} catch (Exception localException) {
			throw new XPCOMInitializationException(
					"Could not load org.mozilla.xpcom.internal.* classes",
					localException);
		}

		this.mozilla.initialize(paramFile);
	}

	public void initEmbedding(File paramFile1, File paramFile2,
			IAppFileLocProvider paramIAppFileLocProvider) throws XPCOMException {
		try {
			this.gre.initEmbedding(paramFile1, paramFile2,
					paramIAppFileLocProvider);
		} catch (NullPointerException localNullPointerException) {
			throw new XPCOMInitializationException(
					"Must call Mozilla.getInstance().initialize() before using this method",
					localNullPointerException);
		}
	}

	public void termEmbedding() {
		try {
			this.gre.termEmbedding();
		} catch (NullPointerException localNullPointerException) {
			throw new XPCOMInitializationException(
					"Must call Mozilla.getInstance().initialize() before using this method",
					localNullPointerException);
		} finally {
			this.mozilla = null;
			this.gre = null;
			this.xpcom = null;
		}
	}

	public ProfileLock lockProfileDirectory(File paramFile)
			throws XPCOMException {
		try {
			return this.gre.lockProfileDirectory(paramFile);
		} catch (NullPointerException localNullPointerException) {
			throw new XPCOMInitializationException(
					"Must call Mozilla.getInstance().initialize() before using this method",
					localNullPointerException);
		}
	}

	public void notifyProfile() {
		try {
			this.gre.notifyProfile();
		} catch (NullPointerException localNullPointerException) {
			throw new XPCOMInitializationException(
					"Must call Mozilla.getInstance().initialize() before using this method",
					localNullPointerException);
		}
	}

	public nsIServiceManager initXPCOM(File paramFile,
			IAppFileLocProvider paramIAppFileLocProvider) throws XPCOMException {
		try {
			return this.xpcom.initXPCOM(paramFile, paramIAppFileLocProvider);
		} catch (NullPointerException localNullPointerException) {
			throw new XPCOMInitializationException(
					"Must call Mozilla.getInstance().initialize() before using this method",
					localNullPointerException);
		}
	}

	public void shutdownXPCOM(nsIServiceManager paramnsIServiceManager)
			throws XPCOMException {
		try {
			this.xpcom.shutdownXPCOM(paramnsIServiceManager);
		} catch (NullPointerException localNullPointerException) {
			throw new XPCOMInitializationException(
					"Must call Mozilla.getInstance().initialize() before using this method",
					localNullPointerException);
		} finally {
			this.mozilla = null;
			this.gre = null;
			this.xpcom = null;
		}
	}

	public nsIServiceManager getServiceManager() throws XPCOMException {
		try {
			return this.xpcom.getServiceManager();
		} catch (NullPointerException localNullPointerException) {
			throw new XPCOMInitializationException(
					"Must call Mozilla.getInstance().initialize() before using this method",
					localNullPointerException);
		}
	}

	public nsIComponentManager getComponentManager() throws XPCOMException {
		try {
			return this.xpcom.getComponentManager();
		} catch (NullPointerException localNullPointerException) {
			throw new XPCOMInitializationException(
					"Must call Mozilla.getInstance().initialize() before using this method",
					localNullPointerException);
		}
	}

	public nsIComponentRegistrar getComponentRegistrar() throws XPCOMException {
		try {
			return this.xpcom.getComponentRegistrar();
		} catch (NullPointerException localNullPointerException) {
			throw new XPCOMInitializationException(
					"Must call Mozilla.getInstance().initialize() before using this method",
					localNullPointerException);
		}
	}

	public nsILocalFile newLocalFile(String paramString, boolean paramBoolean)
			throws XPCOMException {
		try {
			return this.xpcom.newLocalFile(paramString, paramBoolean);
		} catch (NullPointerException localNullPointerException) {
			throw new XPCOMInitializationException(
					"Must call Mozilla.getInstance().initialize() before using this method",
					localNullPointerException);
		}
	}

	public static nsISupports queryInterface(nsISupports paramnsISupports,
			String paramString) {
		ArrayList localArrayList = new ArrayList();
		localArrayList.add(paramnsISupports.getClass());

		while (!localArrayList.isEmpty()) {
			Class localClass1 = (Class) localArrayList.remove(0);

			String str = localClass1.getName();
			if ((str.startsWith("java.")) || (str.startsWith("javax."))) {
				continue;
			}

			if ((localClass1.isInterface()) && (str.startsWith("org.mozilla"))) {
				String localObject = getInterfaceIID(localClass1);
				if ((localObject != null) && (paramString.equals(localObject))) {
					return paramnsISupports;
				}

			}

			Class<?>[] localObject = localClass1.getInterfaces();
			for (int i = 0; i < localObject.length; i++) {
				localArrayList.add(localObject[i]);
			}

			Class localClass2 = localClass1.getSuperclass();
			if (localClass2 != null) {
				localArrayList.add(localClass2);
			}
		}

		return (nsISupports) null;
	}

	public static String getInterfaceIID(Class paramClass) {
		StringBuffer localStringBuffer = new StringBuffer();
		String str1 = paramClass.getName();
		int i = str1.lastIndexOf(".");
		String str2 = i > 0 ? str1.substring(i + 1) : str1;

		if (str2.startsWith("ns")) {
			localStringBuffer.append("NS_");
			localStringBuffer.append(str2.substring(2).toUpperCase());
		} else {
			localStringBuffer.append(str2.toUpperCase());
		}
		localStringBuffer.append("_IID");
		String str3;
		try {
			Field localField = paramClass.getDeclaredField(localStringBuffer
					.toString());
			str3 = (String) localField.get(null);
		} catch (NoSuchFieldException localNoSuchFieldException) {
			str3 = null;
		} catch (IllegalAccessException localIllegalAccessException) {
			System.err.println("ERROR: Could not get field "
					+ localStringBuffer.toString());
			str3 = null;
		}

		return str3;
	}

	public long getNativeHandleFromAWT(Object paramObject) {
		try {
			return this.mozilla.getNativeHandleFromAWT(paramObject);
		} catch (NullPointerException localNullPointerException) {
			throw new XPCOMInitializationException(
					"Must call Mozilla.getInstance().initialize() before using this method",
					localNullPointerException);
		}
	}

	public long wrapJavaObject(Object paramObject, String paramString) {
		try {
			return this.jxutils.wrapJavaObject(paramObject, paramString);
		} catch (NullPointerException localNullPointerException) {
			throw new XPCOMInitializationException(
					"Must call Mozilla.getInstance().initialize() before using this method",
					localNullPointerException);
		}
	}

	public Object wrapXPCOMObject(long paramLong, String paramString) {
		try {
			return this.jxutils.wrapXPCOMObject(paramLong, paramString);
		} catch (NullPointerException localNullPointerException) {
			throw new XPCOMInitializationException(
					"Must call Mozilla.getInstance().initialize() before using this method",
					localNullPointerException);
		}
	}
}