package ru.atomation.jbrowser.impl;

import java.util.concurrent.Callable;

import org.mozilla.browser.MozillaExecutor;

import ru.atomation.jbrowser.interfaces.ScrollControl;

class ScrollControlImpl implements ScrollControl {

	private final JBrowserCanvas browser;
	
	public ScrollControlImpl(JBrowserCanvas browser) {
		this.browser = browser;
	}

	@Override
	public int getScrollX() {
		return MozillaExecutor.mozSyncExecQuiet(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return browser.getWebBrowser().getContentDOMWindow().getScrollX();
			}
		});
	}

	@Override
	public int getScrollY() {
		return MozillaExecutor.mozSyncExecQuiet(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return browser.getWebBrowser().getContentDOMWindow().getScrollY();
			}
		});
	}

	@Override
	public void setScrollbarVisibile(final boolean visible) {
		MozillaExecutor.mozSyncExec(new Runnable() {
			@Override
			public void run() {
				browser.getWebBrowser().getContentDOMWindow().getScrollbars().setVisible(visible);
			}
		});
	}

	@Override
	public void scrollBy(final int xScrollDif, final int yScrollDif) {
		MozillaExecutor.mozSyncExec(new Runnable() {
			@Override
			public void run() {
				browser.getWebBrowser().getContentDOMWindow().scrollBy(xScrollDif, yScrollDif);
			}
		});
	}

	@Override
	public void scrollByLines(final int numLines) {
		MozillaExecutor.mozSyncExec(new Runnable() {
			@Override
			public void run() {
				browser.getWebBrowser().getContentDOMWindow().scrollByLines(numLines);
			}
		});
	}

	@Override
	public void scrollByPages(final int numPages) {
		MozillaExecutor.mozSyncExec(new Runnable() {
			@Override
			public void run() {
				browser.getWebBrowser().getContentDOMWindow().scrollByPages(numPages);
			}
		});
	}

	@Override
	public void scrollTo(final int xScroll, final int yScroll) {
		MozillaExecutor.mozSyncExec(new Runnable() {
			@Override
			public void run() {
				browser.getWebBrowser().getContentDOMWindow().scrollTo(xScroll, yScroll);
			}
		});
	}

	@Override
	public boolean isScrollbarVisible() {
		return MozillaExecutor.mozSyncExecQuiet(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return browser.getWebBrowser().getContentDOMWindow().getScrollbars().getVisible();
			}
		});
	}

}
