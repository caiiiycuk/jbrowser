package org.mozilla.browser.impl;

import org.mozilla.interfaces.nsIFactory;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.xpcom.Mozilla;

public class PromptServiceFactory implements nsIFactory {

    public static final String PROMPTSERVICEFACTORY_CID = "{eeb4e53d-5cec-47ac-ad0b-db7d0a980894}"; //$NON-NLS-1$

    public PromptServiceFactory() {
    }

    public nsISupports queryInterface(String aIID) {
        return Mozilla.queryInterface(this, aIID);
    }

    public nsISupports createInstance(nsISupports aOuter, String iid) {
        PromptService promptService = new PromptService();
        return promptService;
    }

    public void lockFactory(boolean lock) {
    }
}