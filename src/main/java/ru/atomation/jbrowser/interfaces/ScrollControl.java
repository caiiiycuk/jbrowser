package ru.atomation.jbrowser.interfaces;

/**
 * @author caiiiycuk
 */
public interface ScrollControl {
	
    /**
     * Current scroll position in pixels
     */
	int getScrollX();
    
    /**
     * Current scroll position in pixels
     */
	int getScrollY();
    
    /**
     * Change visibility of scrollbars
     * @param visible
     */
    void setScrollbarVisibile(boolean visible);
    
    /**
     * @return true if scrollbars are visible
     */
    boolean isScrollbarVisible();
    
    /**
     * Scrolls the window by a given number of pixels relative to the current scroll position.
     */
    void scrollBy(int xScrollDif, int yScrollDif);
    
    /**
     * Scrolls the window by the specified number of lines.
     */
    void scrollByLines(int numLines);
    
    /**
     * Scrolls the window by the specified number of pages. 
     */
    void scrollByPages(int numPages);
    
    /**
     * Scrolls the window to an absolute pixel offset.
     */
    void scrollTo(int xScroll, int yScroll);

}
