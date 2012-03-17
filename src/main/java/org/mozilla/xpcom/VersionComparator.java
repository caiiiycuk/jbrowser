package org.mozilla.xpcom;

import java.util.StringTokenizer;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsIVersionComparator;

public class VersionComparator implements nsIVersionComparator {
	public nsISupports queryInterface(String paramString) {
		return Mozilla.queryInterface(this, paramString);
	}

	public int compare(String paramString1, String paramString2) {
		String str1 = paramString1;
		String str2 = paramString2;
		int i;
		do {
			VersionPart localVersionPart1 = new VersionPart(null);
			VersionPart localVersionPart2 = new VersionPart(null);
			str1 = parseVersionPart(str1, localVersionPart1);
			str2 = parseVersionPart(str2, localVersionPart2);

			i = compareVersionPart(localVersionPart1, localVersionPart2);
		} while ((i == 0) && ((str1 != null) || (str2 != null)));

		return i;
	}

	private static String parseVersionPart(String paramString,
			VersionPart paramVersionPart) {
		if ((paramString == null) || (paramString.length() == 0)) {
			return paramString;
		}

		StringTokenizer localStringTokenizer = new StringTokenizer(
				paramString.trim(), ".");
		String str1 = localStringTokenizer.nextToken();

		if (str1.equals("*")) {
			paramVersionPart.numA = 2147483647;
			paramVersionPart.strB = "";
		} else {
			VersionPartTokenizer localVersionPartTokenizer = new VersionPartTokenizer(
					str1);
			try {
				paramVersionPart.numA = Integer
						.parseInt(localVersionPartTokenizer.nextToken());
			} catch (NumberFormatException localNumberFormatException1) {
				paramVersionPart.numA = 0;
			}

			if (localVersionPartTokenizer.hasMoreElements()) {
				String str2 = localVersionPartTokenizer.nextToken();

				if (str2.charAt(0) == '+') {
					paramVersionPart.numA += 1;
					paramVersionPart.strB = "pre";
				} else {
					paramVersionPart.strB = str2;

					if (localVersionPartTokenizer.hasMoreTokens()) {
						try {
							paramVersionPart.numC = Integer
									.parseInt(localVersionPartTokenizer
											.nextToken());
						} catch (NumberFormatException localNumberFormatException2) {
							paramVersionPart.numC = 0;
						}
						if (localVersionPartTokenizer.hasMoreTokens()) {
							paramVersionPart.extraD = localVersionPartTokenizer
									.getRemainder();
						}
					}
				}
			}
		}

		if (localStringTokenizer.hasMoreTokens()) {
			return paramString.substring(str1.length() + 1);
		}
		return null;
	}

	private int compareVersionPart(VersionPart paramVersionPart1,
			VersionPart paramVersionPart2) {
		int i = compareInt(paramVersionPart1.numA, paramVersionPart2.numA);
		if (i != 0) {
			return i;
		}

		i = compareString(paramVersionPart1.strB, paramVersionPart2.strB);
		if (i != 0) {
			return i;
		}

		i = compareInt(paramVersionPart1.numC, paramVersionPart2.numC);
		if (i != 0) {
			return i;
		}

		return compareString(paramVersionPart1.extraD, paramVersionPart2.extraD);
	}

	private int compareInt(int paramInt1, int paramInt2) {
		return paramInt1 - paramInt2;
	}

	private int compareString(String paramString1, String paramString2) {
		if (paramString1 == null) {
			return paramString2 != null ? 1 : 0;
		}

		if (paramString2 == null) {
			return -1;
		}

		return paramString1.compareTo(paramString2);
	}

	private class VersionPart {
		int numA = 0;
		String strB;
		int numC = 0;
		String extraD;

		private VersionPart() {
		}

		VersionPart(VersionComparator arg2) {
			this();
		}
	}
}