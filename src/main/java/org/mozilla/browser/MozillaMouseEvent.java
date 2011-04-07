package org.mozilla.browser;

import java.awt.Component;
import java.awt.event.MouseEvent;

import org.w3c.dom.Node;

/**
 * AWT key event extension.
 * Adds the DOM node reference.
 */
public class MozillaMouseEvent extends MouseEvent {

    private static final long serialVersionUID = 918127862264798409L;

    private final Node sourceNode;

    /**
     * Creates a new mozilla mouse event.
     *
     * @param source as for {@link MouseEvent}
     * @param sourceNode DOM node source
     * @param id as for {@link MouseEvent}
     * @param when as for {@link MouseEvent}
     * @param modifiers as for {@link MouseEvent}
     * @param x as for {@link MouseEvent}
     * @param y as for {@link MouseEvent}
     * @param clickCount as for {@link MouseEvent}
     * @param popupTrigger as for {@link MouseEvent}
     * @param button as for {@link MouseEvent}
     */
    public MozillaMouseEvent(Component source,
                             Node sourceNode,
                             int id,
                             long when,
                             int modifiers,
                             int x, int y,
                             int clickCount,
                             boolean popupTrigger,
                             int button)
    {
        super(source, id, when, modifiers,
              x, y,
              clickCount,
              popupTrigger,
              button);
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
