package org.mozilla.browser.examples;

import static org.mozilla.browser.MozillaExecutor.swingAsyncExec;
import static org.mozilla.browser.XPCOMUtils.qi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.TransferHandler;

import net.sourceforge.iharder.Base64;

import org.mozilla.browser.MozillaExecutor;
import org.mozilla.browser.MozillaPanel;
import org.mozilla.browser.XPCOMUtils;
import org.mozilla.dom.NodeFactory;
import org.mozilla.interfaces.nsIDOMEvent;
import org.mozilla.interfaces.nsIDOMEventListener;
import org.mozilla.interfaces.nsIDOMEventTarget;
import org.mozilla.interfaces.nsIDOMWindow2;
import org.mozilla.interfaces.nsIDragService;
import org.mozilla.interfaces.nsIDragSession;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsISupportsString;
import org.mozilla.interfaces.nsITransferable;
import org.mozilla.interfaces.nsIWebBrowser;
import org.mozilla.xpcom.Mozilla;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Drag and drop from/to Mozilla browser
 */
public class Example11_DnD {

    public static void main(String[] args) {
        //Create and set up the window.
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 800);

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        //drop a color into the mozilla area
        JColorChooser b = new JColorChooser();
        b.setColor(Color.RED);
        b.setDragEnabled(true);
        b.setTransferHandler(new Base64TransferHandler());
        b.setMinimumSize(new Dimension(500,100));
        sp.add(b);

        final MozillaPanel moz = new MozillaPanel();
        moz.load("about:"); //$NON-NLS-1$
        moz.setMinimumSize(new Dimension(500,350));
        moz.setPreferredSize(new Dimension(500,650));

        sp.add(moz);

        sp.setDividerLocation(0.7d);
        frame.getContentPane().add(sp, BorderLayout.CENTER);
        frame.setVisible(true);

        //install dnd listener
        MozillaExecutor.mozSyncExec(new Runnable() { public void run() {
            nsIWebBrowser brow = moz.getChromeAdapter().getWebBrowser();
            nsIDOMWindow2 win = qi(brow.getContentDOMWindow(), nsIDOMWindow2.class);
            nsIDOMEventTarget et = win.getWindowRoot();
            MozDnDListener dndl = new MozDnDListener();
            et.addEventListener("dragdrop", dndl, true); //$NON-NLS-1$
        }});
    }

    public static void onDrop(Node source, Node target, Object data) {
        if (data instanceof Color) {
            Color c = (Color) data;
            Document doc = target.getOwnerDocument();
            Element e1 = doc.createElement("table"); //$NON-NLS-1$
            Element e2 = doc.createElement("tr"); //$NON-NLS-1$
            Element e3 = doc.createElement("td"); //$NON-NLS-1$
            Text t = doc.createTextNode("dnd example"); //$NON-NLS-1$

            int i = c.getRed()*256*256+c.getGreen()*256+c.getBlue();
            String s = Integer.toHexString(i);
            e3.setAttribute("bgcolor", "#"+s); //$NON-NLS-1$ //$NON-NLS-2$

            e3.appendChild(t);
            e2.appendChild(e3);
            e1.appendChild(e2);
            target.getParentNode().appendChild(e1);

        }
    }

    private static class MozDnDListener implements nsIDOMEventListener {
        public void handleEvent(nsIDOMEvent ev) {
            System.err.println("dom event "+ev.getType()); //$NON-NLS-1$
            nsIDragService ds = XPCOMUtils.getService("@mozilla.org/widget/dragservice;1", nsIDragService.class); //$NON-NLS-1$
            nsIDragSession dragSession = ds.getCurrentSession();

            String f = "text/unicode"; //$NON-NLS-1$
            if (dragSession.isDataFlavorSupported(f)) {
                nsITransferable t = XPCOMUtils.create("@mozilla.org/widget/transferable;1", nsITransferable.class); //$NON-NLS-1$
                t.addDataFlavor(f);

                dragSession.getData(t, 0);
                nsISupports[] aData = new nsISupports[] { null };
                long[] aDataLen = new long[] { 0 };
                t.getTransferData(f, aData, aDataLen);
                nsISupportsString ss = qi(aData[0], nsISupportsString.class);

                String s = ss.getData();
                Object o;
                try {
                    //try if the content is in base64 encoding
                    o = Base64.decodeToObject(s);
                } catch (Exception e) {
                    o = s;
                }
                final Object data = o;

                final Node target = NodeFactory.getNodeInstance(ev.getTarget());
                final Node source = NodeFactory.getNodeInstance(dragSession.getSourceNode());

                //send awt event
                swingAsyncExec(new Runnable() { public void run() {
                    onDrop(source, target, data);
                }});

                ev.stopPropagation();
            }
        }
        public nsISupports queryInterface(String uuid) {
            return Mozilla.queryInterface(this, uuid);
        }
    }

    /**
     * Adds support for text/unicode support by encoding
     * the data object into base64 for each transferable
     */
    @SuppressWarnings("serial")
	public static class Base64TransferHandler extends TransferHandler {

        public Base64TransferHandler() {
            super("color"); //$NON-NLS-1$
        }

        public Transferable createTransferable(JComponent comp) {
            Transferable t = super.createTransferable(comp);
            return new Base64Transferable(t);
        }
    }

    /**
     * Adds support for text/unicode support by encoding
     * the data object into base64
     */
    public static class Base64Transferable implements Transferable {

        private final Transferable t;

        public Base64Transferable(Transferable t) {
            this.t = t;
        }

        public Object getTransferData(DataFlavor flavor)
            throws
                UnsupportedFlavorException,
                IOException
        {
            if (flavor!=null &&
                flavor.equals(DataFlavor.stringFlavor) &&
                !t.isDataFlavorSupported(DataFlavor.stringFlavor))
            {
                //if text/unicode dataflavor is not supported
                //get the data
                DataFlavor[] dfs = t.getTransferDataFlavors();
                Object d = t.getTransferData(dfs[0]);

                //serialize to byte stream
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream os = new ObjectOutputStream(bos);
                os.writeObject(d);
                os.close();
                bos.close();

                //encode into string with base64
                return Base64.encodeBytes(bos.toByteArray());
            } else {
                return t.getTransferData(flavor);
            }
        }
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if (flavor==null) return false;
            if (flavor.equals(DataFlavor.stringFlavor)) return true;
            return t.isDataFlavorSupported(flavor);
        }
        public DataFlavor[] getTransferDataFlavors() {
            if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return t.getTransferDataFlavors();
            } else {
                DataFlavor[] fs = t.getTransferDataFlavors();

                DataFlavor[] allfs = new DataFlavor[fs.length+1];
                allfs[0] = DataFlavor.stringFlavor;
                System.arraycopy(fs, 0, allfs, 1, fs.length);
                return allfs;
            }
        }
    }


}
