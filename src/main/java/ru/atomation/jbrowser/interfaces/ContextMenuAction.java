package ru.atomation.jbrowser.interfaces;

import ru.atomation.jbrowser.impl.ContextMenuObject;


public interface ContextMenuAction {
	
    public void onShowContextMenu(ContextMenuObject contextMenuClipboard, Object[] rawData);

}
