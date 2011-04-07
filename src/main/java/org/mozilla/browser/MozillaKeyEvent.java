package org.mozilla.browser;

import java.awt.Component;
import java.awt.event.KeyEvent;

import org.w3c.dom.Node;

/**
 * AWT key event extension.
 * Adds the DOM node reference.
 */
public class MozillaKeyEvent extends KeyEvent {

    private static final long serialVersionUID = -7780924825975544053L;

    private final Node sourceNode;

    /**
     * Creates a new mozilla key event.
     *
     * @param source see {@link KeyEvent}
     * @param sourceNode DOM node source
     * @param id see {@link KeyEvent}
     * @param when see {@link KeyEvent}
     * @param modifiers see {@link KeyEvent}
     * @param keyCode see {@link KeyEvent}
     * @param keyChar see {@link KeyEvent}
     */
    public MozillaKeyEvent(Component source,
                           Node sourceNode,
                           int id, long when,
                           int modifiers,
                           int keyCode, char keyChar)
    {
        super(source, id, when, modifiers, keyCode, keyChar);
        this.sourceNode = sourceNode;
    }

    /**
     * Returns DOM node this event was fired from.
     *
     * @return DOM node that fired the event
     */
    public Node getSourceNode() {
        return sourceNode;
    }

}
