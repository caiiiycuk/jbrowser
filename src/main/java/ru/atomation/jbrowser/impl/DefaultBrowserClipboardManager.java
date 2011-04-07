package ru.atomation.jbrowser.impl;

import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.browser.MozillaExecutor;
import org.mozilla.interfaces.nsIClipboardCommands;

import ru.atomation.jbrowser.interfaces.Browser;
import ru.atomation.jbrowser.interfaces.BrowserAdapter;
import ru.atomation.jbrowser.interfaces.BrowserClipboardManager;

public class DefaultBrowserClipboardManager implements BrowserClipboardManager {

    protected static Log logger = LogFactory.getLog(DefaultBrowserClipboardManager.class);
	
	private final Browser browser;
	private final nsIClipboardCommands nsIClipboardCommands;
	private final List<Runnable> delayed;
	
	public DefaultBrowserClipboardManager(Browser browser, nsIClipboardCommands nsIClipboardCommands) {
		this.browser = browser;
		this.nsIClipboardCommands = nsIClipboardCommands;
		this.delayed = Collections.synchronizedList(new ArrayList<Runnable>());
		
		this.browser.addBrowserListener(new BrowserAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				synchronized (delayed) {
					for (Runnable r: delayed) {
						r.run();
					}
					
					delayed.clear();
				}
			}
		});
	}
	
	/**
	 * Вернет false если не активен
	 */
	@Override
	public boolean canCopyImageContents() {
		try {
			return nsIClipboardCommands.canCopyImageContents();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return false;
	}

	@Override
	public boolean canCopyImageLocation() {
		try {
			return nsIClipboardCommands.canCopyImageLocation();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	
		return false;
	}

	@Override
	public boolean canCopyLinkLocation() {
		try {
			return nsIClipboardCommands.canCopyLinkLocation();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	
		return false;
	}

	@Override
	public boolean canCopySelection() {
		try {
			return nsIClipboardCommands.canCopySelection();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	
		return false;
	}

	@Override
	public boolean canCutSelection() {
		try {
			return nsIClipboardCommands.canCutSelection();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	
		return false;
	}

	@Override
	public boolean canPaste() {
		try {
			return nsIClipboardCommands.canPaste();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	
		return false;
	}

	@Override
	public void copyImageContents() {
		invokeInMozillaThread(new Runnable() {
			@Override
			public void run() {
				if (canCopyImageContents()) {
					try {
						nsIClipboardCommands.copyImageContents();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		});
	}

	@Override
	public void copyImageLocation() {
		invokeInMozillaThread(new Runnable() {
			@Override
			public void run() {
				if (canCopyImageLocation()) {
					try {
						nsIClipboardCommands.copyImageLocation();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		});
	}

	@Override
	public void copyLinkLocation() {
		invokeInMozillaThread(new Runnable() {
			@Override
			public void run() {
				if (canCopyLinkLocation()) {
					try {
						nsIClipboardCommands.copyLinkLocation();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		});
	}

	@Override
	public void copySelection() {
		invokeInMozillaThread(new Runnable() {
			@Override
			public void run() {
				if (canCopySelection()) {
					try{
						nsIClipboardCommands.copySelection();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		});
	}

	@Override
	public void cutSelection() {
		invokeInMozillaThread(new Runnable() {
			@Override
			public void run() {
				if (canCutSelection()) {
					try {
						nsIClipboardCommands.cutSelection();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		});
	}

	@Override
	public void paste() {
		invokeInMozillaThread(new Runnable() {
			@Override
			public void run() {
				if (canPaste()) {
					try {
						nsIClipboardCommands.paste();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		});
	}

	protected void invokeInMozillaThread(final Runnable run) {
		if (browser.isFocusOwner()) {
			MozillaExecutor.mozAsyncExec(run);
		} else {
			synchronized (delayed) {
				delayed.add(new Runnable() {
					@Override
					public void run() {
						MozillaExecutor.mozAsyncExec(run);
					}
				});
			}
			
			browser.requestFocus();
		}
	}
	
}
