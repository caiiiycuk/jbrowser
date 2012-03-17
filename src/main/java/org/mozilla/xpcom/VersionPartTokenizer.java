package org.mozilla.xpcom;

import java.util.Enumeration;

class VersionPartTokenizer implements Enumeration {
	String part;

	public VersionPartTokenizer(String paramString) {
		this.part = paramString;
	}

	public boolean hasMoreElements() {
		return this.part.length() != 0;
	}

	public boolean hasMoreTokens() {
		return this.part.length() != 0;
	}

	public Object nextElement() {
		if (this.part.matches("[\\+\\-]?[0-9].*")) {
			int i = 0;
			if ((this.part.charAt(0) == '+') || (this.part.charAt(0) == '-')) {
				i = 1;
			}

			while ((i < this.part.length())
					&& (Character.isDigit(this.part.charAt(i)))) {
				i++;
			}

			String str = this.part.substring(0, i);
			this.part = this.part.substring(i);
			return str;
		}

		int i = 0;
		while ((i < this.part.length())
				&& (!Character.isDigit(this.part.charAt(i)))) {
			i++;
		}

		String str = this.part.substring(0, i);
		this.part = this.part.substring(i);
		return str;
	}

	public String nextToken() {
		return (String) nextElement();
	}

	public String getRemainder() {
		return this.part;
	}
}