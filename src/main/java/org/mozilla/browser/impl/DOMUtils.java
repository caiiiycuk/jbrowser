package org.mozilla.browser.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * utility functions for DOM manipulation
 */
public class DOMUtils
{

    public static void writeDOMToFile(Document doc, File f)
        throws IOException
    {
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(f));
            writeDOMToStream(doc, os);
        }
        finally {
            if (os!=null) os.close();
        }
    }

    public static String writeDOMToString(Document doc)
    {
        try {
            StringWriter w = new StringWriter();
            try {
                StreamResult sr = new StreamResult(w);
                writeDOMToSource(doc, sr,  "UTF-8"); //$NON-NLS-1$
                return w.toString();
            }
            finally {
                if (w!=null) w.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String writeDOMToString(Node n)
    {
        try {
            StringWriter w = new StringWriter();
            try {
                StreamResult sr = new StreamResult(w);
                writeDOMToSource(n, sr,  "UTF-8"); //$NON-NLS-1$
                return w.toString();
            } finally {
                if (w!=null) w.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeDOMToStream(Document doc,
                                        OutputStream os)
        throws IOException
    {
        writeDOMToStream(doc, os, "UTF-8"); //$NON-NLS-1$
    }

    public static void writeDOMToStream(Node n,
                                        OutputStream os)
        throws IOException
    {
        StreamResult sr = new StreamResult(os);
        writeDOMToSource(n, sr, "UTF-8"); //$NON-NLS-1$
    }

    public static void writeDOMToStream(Document doc,
                                        OutputStream os,
                                        String encoding)
        throws IOException
    {
        StreamResult sr = new StreamResult(os);
        writeDOMToSource(doc, sr, encoding);
    }

    public static void writeDOMToSource(Node n,
                                        StreamResult sr,
                                        String encoding)
        throws IOException
    {
        try {
            //create transformer
            TransformerFactory trf = TransformerFactory.newInstance();
            Transformer tr = trf.newTransformer();
            tr.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
            tr.setOutputProperty(OutputKeys.ENCODING, encoding);
            tr.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

            //serialize DOM
            tr.transform(new DOMSource(n), sr);
        }
        catch (TransformerException e) {
            //wrap to IOException
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }
     }

}
