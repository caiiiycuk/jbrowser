package ru.atomation.jbrowser.impl;

import java.net.MalformedURLException;
import java.net.URL;

import org.mozilla.interfaces.nsIDOMEvent;
import org.mozilla.interfaces.nsIDOMMouseEvent;
import org.mozilla.interfaces.nsIDOMNamedNodeMap;
import org.mozilla.interfaces.nsIDOMNode;

import ru.atomation.jbrowser.interfaces.BrowserClipboardManager;
import ru.atomation.jbrowser.interfaces.ContextMenuFlags.ContextMenuFlag;

public class ContextMenuObject {

	private final ContextMenuFlag contextMenuFlag;
	private final JBrowserComponent<?> browser;
	private final BrowserClipboardManager browserClipboardManager;
	
	private boolean link;
	private String linkUrl;
	
	private int absoluteX;
	private int absoluteY;
	
	
	public ContextMenuObject(JBrowserComponent<?> browser, ContextMenuFlag contextMenuFlag,
			BrowserClipboardManager browserClipboardManager, nsIDOMEvent aEvent,
			nsIDOMNode aNode) {
		super();
		this.browser = browser;
		this.contextMenuFlag = contextMenuFlag;
		this.browserClipboardManager = browserClipboardManager;
		
		getXY(aEvent);
		checkForLink(aNode);
	}
	
	
	private void getXY(nsIDOMEvent aEvent) {
		nsIDOMMouseEvent domMouseEvent = (nsIDOMMouseEvent) aEvent.queryInterface(nsIDOMMouseEvent.NS_IDOMMOUSEEVENT_IID);
		if (domMouseEvent != null) {
			absoluteX = domMouseEvent.getScreenX();
			absoluteY = domMouseEvent.getScreenY();
		} else {
			absoluteX = -1;
			absoluteY = -1;
		}
	}


	private void checkForLink(nsIDOMNode aNode) {
		linkUrl = null;
		
		link = aNode != null && aNode.getNodeName().toLowerCase().equals("a") && 
			(contextMenuFlag.equals(ContextMenuFlag.LINK) ||
			 contextMenuFlag.equals(ContextMenuFlag.IMAGE_LINK));
	
		if (link) {
			nsIDOMNamedNodeMap attributes = aNode.getAttributes();
			if (attributes != null) {
				for (int i=0; i<attributes.getLength(); i++) {
					nsIDOMNode item = attributes.item(i);
					if ("href".equals(item.getNodeName().toLowerCase())) {
						try {
							linkUrl = new URL(new URL(browser.getUrl()), item.getNodeValue()).toString();
						} catch (MalformedURLException e) {
							link = false;
							linkUrl = null;
						}
						return;
					}
				}
			}
			
			link = false;
		}
	}


	public boolean canCopyImageContents() {
		if (contextMenuFlag.equals(ContextMenuFlag.IMAGE) || contextMenuFlag.equals(ContextMenuFlag.IMAGE_LINK)) {
			return browserClipboardManager.canCopyImageContents();
		}
		
		return false;
	}
	
	public void copyImageContents() {
		browserClipboardManager.copyImageContents();
	}
	
	public boolean canCopyImageLocation() {
		if (contextMenuFlag.equals(ContextMenuFlag.IMAGE) || contextMenuFlag.equals(ContextMenuFlag.IMAGE_LINK)) {
			return browserClipboardManager.canCopyImageLocation();
		}
		
		return false;
	}
	
	public void copyImageLocation() {
		browserClipboardManager.copyImageLocation();
	}
	
	public boolean canCopyLinkLocation() {
		if (contextMenuFlag.equals(ContextMenuFlag.LINK) || contextMenuFlag.equals(ContextMenuFlag.IMAGE_LINK)) {
			return browserClipboardManager.canCopyLinkLocation();
		}
		
		return false;
	}
	
	public void copyLinkLocation() {
		browserClipboardManager.copyLinkLocation();
	}
	
	public boolean canCopySelection() {
		return browserClipboardManager.canCopySelection();
	}
	
	public void copySelection() {
		browserClipboardManager.copySelection();
	}
	
	public boolean canCutSelection() {
		return browserClipboardManager.canCutSelection();
	}
	
	public void cutSelection() {
		browserClipboardManager.cutSelection();
	}
	
	public boolean canPaste() {
		return browserClipboardManager.canPaste();
	}
	
	public void paste() {
		browserClipboardManager.paste();
	}
	
	@Override
	public String toString() {
		return 
			"canCopyImageContents#" +
				canCopyImageContents()+
			"\ncanCopyImageLocation#"+
				canCopyImageLocation()+
			"\ncanCopyLinkLocation#"+
				canCopyLinkLocation()+
			"\ncanCopySelection#"+
				canCopySelection()+
			"\ncanCutSelection()#"+
				canCutSelection()+
			"\ncanPaste#"+
				canPaste();
	}
	
	public ContextMenuFlag getContextMenuFlag() {
		return contextMenuFlag;
	}
	
	public boolean hasLink() {
		return link;
	}
	
	public String getLinkUrl() {
		return linkUrl;
	}
	
	public int getAbsoluteX() {
		return absoluteX;
	}
	
	public int getAbsoluteY() {
		return absoluteY;
	}
	
}
