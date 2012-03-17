package org.mozilla.xpcom;

public class GREVersionRange {
	private String lower;
	private boolean lowerInclusive;
	private String upper;
	private boolean upperInclusive;

	public GREVersionRange(String paramString1, boolean paramBoolean1,
			String paramString2, boolean paramBoolean2) {
		this.lower = paramString1;
		this.lowerInclusive = paramBoolean1;
		this.upper = paramString2;
		this.upperInclusive = paramBoolean2;
	}

	public boolean check(String paramString) {
		VersionComparator localVersionComparator = new VersionComparator();
		int i = localVersionComparator.compare(paramString, this.lower);
		if (i < 0) {
			return false;
		}

		if ((i == 0) && (!this.lowerInclusive)) {
			return false;
		}

		i = localVersionComparator.compare(paramString, this.upper);
		if (i > 0) {
			return false;
		}

		return (i != 0) || (this.upperInclusive);
	}
}