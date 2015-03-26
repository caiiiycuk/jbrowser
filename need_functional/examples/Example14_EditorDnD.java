package org.mozilla.browser.examples;

import static org.mozilla.browser.XPCOMUtils.qi;
import static org.mozilla.browser.examples.Example13_Editor.initializeHTMLEditor;

import java.util.Arrays;

import org.mozilla.browser.IMozillaWindow;
import org.mozilla.browser.MozillaAutomation;
import org.mozilla.browser.MozillaExecutor;
import org.mozilla.browser.MozillaWindow;
import org.mozilla.browser.XPCOMUtils;
import org.mozilla.interfaces.nsIClipboardDragDropHooks;
import org.mozilla.interfaces.nsICommandManager;
import org.mozilla.interfaces.nsICommandParams;
import org.mozilla.interfaces.nsIDOMDocument;
import org.mozilla.interfaces.nsIDOMElement;
import org.mozilla.interfaces.nsIDOMEvent;
import org.mozilla.interfaces.nsIDOMNode;
import org.mozilla.interfaces.nsIDOMText;
import org.mozilla.interfaces.nsIDOMWindow;
import org.mozilla.interfaces.nsIDragSession;
import org.mozilla.interfaces.nsIEditingSession;
import org.mozilla.interfaces.nsIEditor;
import org.mozilla.interfaces.nsIFormatConverter;
import org.mozilla.interfaces.nsIInterfaceRequestor;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsISupportsArray;
import org.mozilla.interfaces.nsISupportsCString;
import org.mozilla.interfaces.nsISupportsString;
import org.mozilla.interfaces.nsITransferable;
import org.mozilla.interfaces.nsIWebBrowser;
import org.mozilla.xpcom.Mozilla;

/**
 * Overriding drag and drop in HTML Editor
 */
public class Example14_EditorDnD {

    public static void main(String[] args) {
        final MozillaWindow win = new MozillaWindow();
        win.setSize(500, 600);
        win.setVisible(true);
        MozillaAutomation.blockingLoad(win, "about:"); //$NON-NLS-1$

        initializeHTMLEditor(win);
        installDnDHook(win);

        //now try to drop some text on an <A> link
        //in the mozswing window, e.g. from WordPad
    }

    public static void installDnDHook(final IMozillaWindow win) {
        MozillaExecutor.mozSyncExec(new Runnable() { public void run() {
            nsIWebBrowser webBrowser = win.getChromeAdapter().getWebBrowser();
            nsIInterfaceRequestor ir = qi(webBrowser, nsIInterfaceRequestor.class);

            nsIDOMWindow domWin = webBrowser.getContentDOMWindow();
            nsIEditingSession editorSession = (nsIEditingSession) ir.getInterface(nsIEditingSession.NS_IEDITINGSESSION_IID);
            nsIEditor editor = editorSession.getEditorForWindow(domWin);
            assert editor!=null;

            nsICommandManager cm = (nsICommandManager)  ir.getInterface(nsICommandManager.NS_ICOMMANDMANAGER_IID);
            nsICommandParams p = XPCOMUtils.create("@mozilla.org/embedcomp/command-params;1", nsICommandParams.class); //$NON-NLS-1$
            p.setISupportsValue("addhook", dndHook); //$NON-NLS-1$
            cm.doCommand("cmd_clipboardDragDropHook", p, domWin); //$NON-NLS-1$
        }});
    }


    static class DndHook implements nsIClipboardDragDropHooks {

        private static final String TEXT_UNICODE = "text/unicode"; //$NON-NLS-1$

        public boolean allowDrop(nsIDOMEvent event, nsIDragSession session) {
            if (!session.isDataFlavorSupported(TEXT_UNICODE)) return false;

            nsIDOMNode target = qi(event.getTarget(), nsIDOMNode.class);
            if (target==null) return false;

            //allow drop only on <A> elements
            String name = target.getLocalName();
            if (name==null || !name.equalsIgnoreCase("a")) return false; //$NON-NLS-1$

            return true;
        }
        public boolean allowStartDrag(nsIDOMEvent event) {
            return false;
        }
        public boolean onCopyOrDrag(nsIDOMEvent event, nsITransferable trans) {
            return false;
        }
        public boolean onPasteOrDrop(nsIDOMEvent event, nsITransferable trans) {
            nsIFormatConverter fc = trans.getConverter();
            assert fc==null;
            trans.setConverter(new FormatConverter());

            nsISupports[] aData = new nsISupports[] { null };
            long[] aDataLen = new long[] { 0 };
            trans.getTransferData("text/unicode", aData, aDataLen); //$NON-NLS-1$
            nsISupportsString ss = qi(aData[0], nsISupportsString.class);
            String s = ss.getData();

            //handle the drop, here...

            //in this example we know the target is <a>
            nsIDOMElement target = qi(event.getTarget(), nsIDOMElement.class);
            if (target==null) return true; //do default action

            nsIDOMElement targetParent = qi(target.getParentNode(), nsIDOMElement.class);
            if (targetParent==null) return true; //do default action

            nsIDOMDocument doc = targetParent.getOwnerDocument();

            nsIDOMElement e = doc.createElement("a"); //$NON-NLS-1$
            e.setAttribute("href", "about:"); //$NON-NLS-1$ //$NON-NLS-2$
            nsIDOMText t = doc.createTextNode(s);
            e.appendChild(t);
            targetParent.appendChild(e);

            return false; //cancel default action
        }
        public nsISupports queryInterface(String uuid) {
            return Mozilla.queryInterface(this, uuid);
        }
    }

    static class FormatConverter implements nsIFormatConverter {

        private static final String[] fromMimes = new String[] {
            "application/x-moz-nativehtml", //$NON-NLS-1$
            "text/html", //$NON-NLS-1$
            "text/unicode", //$NON-NLS-1$
        };

        private static final String[] toMimes = new String[] {
            "text/unicode", //$NON-NLS-1$
        };

        public boolean canConvert(String fromDataFlavor, String toDataFlavor) {
            return
                Arrays.asList(fromMimes).contains(fromDataFlavor) &&
                Arrays.asList(toMimes).contains(toDataFlavor);
        }
        public void convert(String fromDataFlavor, nsISupports fromData,
                long dataLen, String toDataFlavor, nsISupports[] toData,
                long[] dataToLen)
        {
            String d = ""; //$NON-NLS-1$

            nsISupportsCString s1 = qi(fromData, nsISupportsCString.class);
            if (s1!=null) d = s1.getData();
            else {
                nsISupportsString s2 = qi(fromData, nsISupportsString.class);
                if (s2!=null) d = s2.getData();
            }

            nsISupportsString s = XPCOMUtils.create("@mozilla.org/supports-string;1", nsISupportsString.class); //$NON-NLS-1$
            s.setData(d);
            toData[0] = s;
            dataToLen[0] = d.length();
        }
        public nsISupportsArray getInputDataFlavors() {
            nsISupportsArray a = XPCOMUtils.create("@mozilla.org/supports-array;1", nsISupportsArray.class); //$NON-NLS-1$
            for (String mime : fromMimes) {
                nsISupportsCString s = XPCOMUtils.create("@mozilla.org/supports-cstring;1", nsISupportsCString.class); //$NON-NLS-1$
                s.setData(mime);
                a.appendElement(s);
            }
            return a;
        }
        public nsISupportsArray getOutputDataFlavors() {
            nsISupportsArray a = XPCOMUtils.create("@mozilla.org/supports-array;1", nsISupportsArray.class); //$NON-NLS-1$
            for (String mime : toMimes) {
                nsISupportsCString s = XPCOMUtils.create("@mozilla.org/supports-cstring;1", nsISupportsCString.class); //$NON-NLS-1$
                s.setData(mime);
                a.appendElement(s);
            }
            return a;
        }
        public nsISupports queryInterface(String uuid) {
            return Mozilla.queryInterface(this, uuid);
        }
    }

    static DndHook dndHook = new DndHook();
}
