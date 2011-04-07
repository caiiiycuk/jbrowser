package org.mozilla.browser.impl.components;

import java.util.Vector;

import javax.swing.JLabel;

/**
 * Implements a JLabel-like component, that is capable
 * to display several lines of text
 */
public class JMultiLineLabel extends JLabel
{

    private static final long serialVersionUID = 1L;

    public void setText(String s) {
        if (s == null) s = ""; //$NON-NLS-1$
        String lines[] = breakupLines(s);
        StringBuffer sb = new StringBuffer();
        sb.append("<html>"); //$NON-NLS-1$
        for (int i=0; i<lines.length; i++) {
            if (i>0) sb.append("<br>"); //$NON-NLS-1$
            sb.append(lines[i]);
        }
        sb.append("</html>"); //$NON-NLS-1$
        super.setText(sb.toString());
    }

    private static String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$
    private static int LINE_SEPARATOR_LEN = LINE_SEPARATOR.length();

    @SuppressWarnings("unchecked") //$NON-NLS-1$
    public static String[] breakupLines(String text) {
        int len = text.length();
        if (len == 0) return new String[] {""}; //$NON-NLS-1$
        else {
            Vector data = new Vector(10);
            int start=0;
            int i=0;
            while (i<len) {
                if (text.startsWith(LINE_SEPARATOR,i)) {
                    data.addElement(text.substring(start,i));
                    start=i+LINE_SEPARATOR_LEN;
                    i=start;
                }
                else if (text.charAt(i)=='\n') {
                    data.addElement(text.substring(start,i));
                    start=i+1;
                    i=start;
                }
                else { i++; }
            }

            if (start != len) {
                data.addElement(text.substring(start));
            }

            int numlines = data.size();
            String lines[] = new String[numlines];
            data.copyInto(lines);
            return lines;
        }
    }

}
