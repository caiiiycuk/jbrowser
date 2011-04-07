package ru.atomation.jbrowser.interfaces;

import org.mozilla.interfaces.nsIContextMenuListener;


public class ContextMenuFlags {

	public enum ContextMenuFlag {
		NONE("NONE", nsIContextMenuListener.CONTEXT_NONE),
		TEXT("TEXT", nsIContextMenuListener.CONTEXT_TEXT),
		DOCUMENT("DOCUMENT", nsIContextMenuListener.CONTEXT_DOCUMENT),
		IMAGE("IMAGE", nsIContextMenuListener.CONTEXT_IMAGE),
		LINK("LINK", nsIContextMenuListener.CONTEXT_LINK),
		IMAGE_LINK("IMAGE_LINK", nsIContextMenuListener.CONTEXT_IMAGE | nsIContextMenuListener.CONTEXT_LINK);
		
		private final String name;
		private final long uid;
		
		private ContextMenuFlag(String name, long uid) {
			this.name = name;
			this.uid = uid;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public long toLong() {
			return uid;
		}
	}
	
	
	public static ContextMenuFlag fromLong(long aContextFlag) {
		if (aContextFlag == nsIContextMenuListener.CONTEXT_TEXT) {
			return ContextMenuFlag.TEXT;
		}
		
		if (aContextFlag == nsIContextMenuListener.CONTEXT_DOCUMENT) {
			return ContextMenuFlag.DOCUMENT;
		}
		
		if (aContextFlag == nsIContextMenuListener.CONTEXT_IMAGE) {
			return ContextMenuFlag.IMAGE;
		}
		
		if (aContextFlag == nsIContextMenuListener.CONTEXT_LINK) {
			return ContextMenuFlag.LINK;
		}
		
		if (aContextFlag == (nsIContextMenuListener.CONTEXT_IMAGE | nsIContextMenuListener.CONTEXT_LINK)) {
			return ContextMenuFlag.IMAGE_LINK;
		}
		
		return ContextMenuFlag.NONE;
	}
}
