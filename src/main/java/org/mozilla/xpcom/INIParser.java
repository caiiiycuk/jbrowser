package org.mozilla.xpcom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

public class INIParser {
	private HashMap mSections;

	public INIParser(String paramString, Charset paramCharset)
			throws FileNotFoundException, IOException {
		initFromFile(new File(paramString), paramCharset);
	}

	public INIParser(String paramString) throws FileNotFoundException,
			IOException {
		initFromFile(new File(paramString), Charset.forName("UTF-8"));
	}

	public INIParser(File paramFile, Charset paramCharset)
			throws FileNotFoundException, IOException {
		initFromFile(paramFile, paramCharset);
	}

	public INIParser(File paramFile) throws FileNotFoundException, IOException {
		initFromFile(paramFile, Charset.forName("UTF-8"));
	}

	private void initFromFile(File paramFile, Charset paramCharset)
			throws FileNotFoundException, IOException {
		FileInputStream localFileInputStream = new FileInputStream(paramFile);
		InputStreamReader localInputStreamReader = new InputStreamReader(
				localFileInputStream, paramCharset);
		BufferedReader localBufferedReader = new BufferedReader(
				localInputStreamReader);

		this.mSections = new HashMap();
		String str1 = null;
		String str2;
		while ((str2 = localBufferedReader.readLine()) != null) {
			String str3 = str2.trim();
			if ((str3.length() == 0) || (str3.startsWith("#"))
					|| (str3.startsWith(";"))) {
				continue;
			}

			if (str2.startsWith("[")) {
				if ((!str3.endsWith("]"))
						|| (str3.indexOf("]") != str3.length() - 1)) {
					str1 = null;
					continue;
				}

				str1 = str3.substring(1, str3.length() - 1);
				continue;
			}

			if (str1 == null) {
				continue;
			}
			StringTokenizer localStringTokenizer = new StringTokenizer(str2,
					"=");
			if (localStringTokenizer.countTokens() != 2) {
				continue;
			}
			Properties localProperties = (Properties) this.mSections.get(str1);
			if (localProperties == null) {
				localProperties = new Properties();
				this.mSections.put(str1, localProperties);
			}
			localProperties.setProperty(localStringTokenizer.nextToken(),
					localStringTokenizer.nextToken());
		}

		localBufferedReader.close();
	}

	public Iterator getSections() {
		return this.mSections.keySet().iterator();
	}

	public Iterator getKeys(String paramString) {
		Properties localProperties = (Properties) this.mSections
				.get(paramString);
		if (localProperties == null) {
			return null;
		}

		final Enumeration<?> propertyNames = 
				localProperties.propertyNames();
		
		return new Iterator() {
			public boolean hasNext() {
				return propertyNames.hasMoreElements();
			}

			public Object next() {
				return propertyNames.nextElement();
			}

			public void remove() {
			}
		};
	}

	public String getString(String paramString1, String paramString2) {
		Properties localProperties = (Properties) this.mSections
				.get(paramString1);
		if (localProperties == null) {
			return null;
		}

		return localProperties.getProperty(paramString2);
	}
}