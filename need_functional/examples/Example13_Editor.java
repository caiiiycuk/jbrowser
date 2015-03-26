package org.mozilla.browser.examples;

import static org.mozilla.browser.XPCOMUtils.qi;

import org.mozilla.browser.IMozillaWindow;
import org.mozilla.browser.MozillaAutomation;
import org.mozilla.browser.MozillaExecutor;
import org.mozilla.browser.MozillaWindow;
import org.mozilla.browser.XPCOMUtils;
import org.mozilla.interfaces.nsICommandManager;
import org.mozilla.interfaces.nsIDOMElement;
import org.mozilla.interfaces.nsIDOMEvent;
import org.mozilla.interfaces.nsIDOMEventListener;
import org.mozilla.interfaces.nsIDOMEventTarget;
import org.mozilla.interfaces.nsIDOMWindow;
import org.mozilla.interfaces.nsIDOMWindow2;
import org.mozilla.interfaces.nsIEditingSession;
import org.mozilla.interfaces.nsIEditor;
import org.mozilla.interfaces.nsIEditorStyleSheets;
import org.mozilla.interfaces.nsIHTMLEditor;
import org.mozilla.interfaces.nsIHTMLInlineTableEditor;
import org.mozilla.interfaces.nsIHTMLObjectResizer;
import org.mozilla.interfaces.nsIInterfaceRequestor;
import org.mozilla.interfaces.nsISelection;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsIWebBrowser;
import org.mozilla.xpcom.Mozilla;

/**
 * Working with the HTML Editor
 */
public class Example13_Editor {

    public static void main(String[] args) {
        final MozillaWindow win = new MozillaWindow();
        win.setSize(500, 600);
        win.setVisible(true);
        MozillaAutomation.blockingLoad(win, "about:license"); //$NON-NLS-1$

        initializeHTMLEditor(win);
        modifyHTMLContent(win);
        addClickListener(win);
    }

    public static void initializeHTMLEditor(final IMozillaWindow win) {
        MozillaExecutor.mozSyncExec(new Runnable() { public void run() {
            nsIWebBrowser webBrowser = win.getChromeAdapter().getWebBrowser();
            nsIInterfaceRequestor ir = qi(webBrowser, nsIInterfaceRequestor.class);

            nsIDOMWindow domWin = webBrowser.getContentDOMWindow();

            //start editing session
            nsIEditingSession editorSession = (nsIEditingSession) ir.getInterface(nsIEditingSession.NS_IEDITINGSESSION_IID);
            editorSession.makeWindowEditable(domWin, "html", false, true, true); //$NON-NLS-1$

            //get the generic editor
            nsIEditor editor = editorSession.getEditorForWindow(domWin);
            assert editor!=null;

            //get the HTML editor
            final nsIHTMLEditor htmlEditor = qi(editor, nsIHTMLEditor.class);
            assert htmlEditor!=null;
            htmlEditor.ignoreSpuriousDragEvent(true);
            htmlEditor.setReturnInParagraphCreatesNewParagraph(true);

            final nsIHTMLObjectResizer resizer = XPCOMUtils.qi(htmlEditor, nsIHTMLObjectResizer.class);
            assert resizer!=null;
            resizer.setObjectResizingEnabled(true);
            resizer.refreshResizers();

            nsIHTMLInlineTableEditor tableEditor = XPCOMUtils.qi(htmlEditor, nsIHTMLInlineTableEditor.class);
            assert tableEditor!=null;
        }});
    }

    public static void modifyHTMLContent(final IMozillaWindow win) {
        MozillaExecutor.mozSyncExec(new Runnable() { public void run() {
            nsIWebBrowser webBrowser = win.getChromeAdapter().getWebBrowser();
            nsIInterfaceRequestor ir = qi(webBrowser, nsIInterfaceRequestor.class);

            nsIDOMWindow domWin = webBrowser.getContentDOMWindow();
            nsIEditingSession editorSession = (nsIEditingSession) ir.getInterface(nsIEditingSession.NS_IEDITINGSESSION_IID);
            nsIEditor editor = editorSession.getEditorForWindow(domWin);
            assert editor!=null;

            //select an element in the editor
            nsISelection sel = editor.getSelection();
            nsIDOMElement e = domWin.getDocument().getElementById("section-1"); //$NON-NLS-1$
            sel.collapse(e, 0);

            //start to modify the HTML content
            nsICommandManager cm = (nsICommandManager)  ir.getInterface(nsICommandManager.NS_ICOMMANDMANAGER_IID);
            for (int i=0;i<50;i++) {
                cm.doCommand("cmd_insertHR", null, domWin); //$NON-NLS-1$
            }
        }});
    }

    static final String kNormalStyleSheet = "chrome://editor/content/EditorContent.css"; //$NON-NLS-1$
    static final String kAllTagsStyleSheet = "chrome://editor/content/EditorAllTags.css"; //$NON-NLS-1$
    static final String kParagraphMarksStyleSheet = "chrome://editor/content/EditorParagraphMarks.css"; //$NON-NLS-1$
    static final String kContentEditableStyleSheet = "resource://gre/res/contenteditable.css"; //$NON-NLS-1$

    public static void addClickListener(final IMozillaWindow win) {
        MozillaExecutor.mozSyncExec(new Runnable() { public void run() {
            nsIWebBrowser webBrowser = win.getChromeAdapter().getWebBrowser();
            nsIInterfaceRequestor ir = qi(webBrowser, nsIInterfaceRequestor.class);

            nsIDOMWindow2 domWin = qi(webBrowser.getContentDOMWindow(), nsIDOMWindow2.class);
            nsIEditingSession editorSession = (nsIEditingSession) ir.getInterface(nsIEditingSession.NS_IEDITINGSESSION_IID);
            nsIEditor editor = editorSession.getEditorForWindow(domWin);
            assert editor!=null;

            nsIEditorStyleSheets es = qi(editor, nsIEditorStyleSheets.class);
            es.addOverrideStyleSheet(kContentEditableStyleSheet);
            //es.addOverrideStyleSheet(kNormalStyleSheet);  // not in chrome/
            //es.addOverrideStyleSheet(kAllTagsStyleSheet); // not in chrome/
            //es.enableStyleSheet(kAllTagsStyleSheet, false);

            final nsIHTMLObjectResizer resizer = XPCOMUtils.qi(editor, nsIHTMLObjectResizer.class);
            assert resizer!=null;
            resizer.setObjectResizingEnabled(true);

            nsIDOMEventTarget et = domWin.getWindowRoot();
            et.addEventListener("click", new nsIDOMEventListener() { //$NON-NLS-1$
                public void handleEvent(nsIDOMEvent event) {

                    nsIDOMElement el = qi(event.getTarget(), nsIDOMElement.class);
                    if (el==null) return;

                    //show resize handle
                    resizer.hideResizers();
                    resizer.showResizers(el);
                }
                public nsISupports queryInterface(String uuid) {
                    return Mozilla.queryInterface(this, uuid);
                }
            }, true);
        }});
    }

}
