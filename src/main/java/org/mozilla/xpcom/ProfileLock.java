package org.mozilla.xpcom;

public class ProfileLock {
	private long lock = 0L;

	public ProfileLock(long paramLong) {
		this.lock = paramLong;
	}

	public void release() {
		releaseNative(this.lock);
		this.lock = 0L;
	}

	private native void releaseNative(long paramLong);

	public boolean isValid() {
		return this.lock != 0L;
	}

	protected void finalize() throws Throwable {
		release();
		super.finalize();
	}
}