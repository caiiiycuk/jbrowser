package ru.atomation.jbrowser.interfaces;

public interface BrowserClipboardManager {

	boolean canCopyImageContents();

	boolean canCopyImageLocation();

	boolean canCopyLinkLocation();

	boolean canCopySelection();

	boolean canCutSelection();

	boolean canPaste();

	void copyImageContents();

	void copyImageLocation();

	void copyLinkLocation();

	void copySelection();

	void cutSelection();

	void paste();

}
