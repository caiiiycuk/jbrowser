package org.mozilla.browser.examples;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;

import org.mozilla.browser.MozillaPanel;

/**
 * Mozilla browser embedded into a JPanel
 */
public class Example09_CreatePanel {

    public static void main(String[] args) {
        //Create and set up the window.
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu"); //$NON-NLS-1$
        menuBar.add(menu);
        for (int i=0; i<15; i++) {
          JMenuItem menuItem = new JMenuItem("menu item "+i); //$NON-NLS-1$
          menu.add(menuItem);
        }
        frame.setJMenuBar(menuBar);
        //force heavyweight menu to overlay the mozilla panel
        menu.getPopupMenu().setLightWeightPopupEnabled(false);

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        JButton b = new JButton("Click me"); //$NON-NLS-1$
        b.setMinimumSize(new Dimension(500,100));
        sp.add(b);

        final MozillaPanel moz = new MozillaPanel();
        moz.load("about:"); //$NON-NLS-1$
        moz.setMinimumSize(new Dimension(500,200));
        sp.add(moz);

        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String[] urls = new String[] {
                    "about:", //$NON-NLS-1$
                    "http://www.yahoo.com", //$NON-NLS-1$
                    "http://www.google.com", //$NON-NLS-1$
                    "http://www.msn.com" //$NON-NLS-1$
                };
                int idx = (int) (Math.random()*urls.length);
                moz.load(urls[idx]);
            }
        });

        sp.setDividerLocation(0.2d);
        frame.getContentPane().add(sp, BorderLayout.CENTER);
        frame.setVisible(true);
    }

}
