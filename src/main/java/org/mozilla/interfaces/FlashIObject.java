package org.mozilla.interfaces;

/**
 * An IDL interface for the Flash plugin.
 */
public interface FlashIObject extends nsISupports {

      String FLASHIOBJECT_IID = "{42b1d5a4-6c2b-11d6-8063-0005029bc257}"; //$NON-NLS-1$

      FlashIObject evaluate(String s);

}