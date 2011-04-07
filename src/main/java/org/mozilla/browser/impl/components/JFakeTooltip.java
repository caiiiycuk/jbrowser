package org.mozilla.browser.impl.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;


public class JFakeTooltip extends JDialog {

    private static final long serialVersionUID = -5265135238377781454L;

    private JLabel tooltipLabel;

    public JFakeTooltip() {
    	init();
	}

    public void init() {
        setDefaultLookAndFeelDecorated(false);
        setUndecorated(true);
        setFocusable(false);
        setFocusableWindowState(false);
        //alwaysonTop paints tooltip also
        //above other windows
        //setAlwaysOnTop(true);

        JPanel box = new JPanel();
        Border border = UIManager.getBorder("ToolTip.border"); //$NON-NLS-1$
        box.setBorder(border);
        box.setLayout(new BorderLayout());

        tooltipLabel = new JLabel(""); //$NON-NLS-1$
        tooltipLabel.setHorizontalAlignment(SwingConstants.CENTER);

        Color fg = UIManager.getColor("ToolTip.foreground"); //$NON-NLS-1$
        Color bg = UIManager.getColor("ToolTip.background"); //$NON-NLS-1$
        Font f = UIManager.getFont("ToolTip.font"); //$NON-NLS-1$
        tooltipLabel.setOpaque(true);
        tooltipLabel.setForeground(fg);
        tooltipLabel.setBackground(bg);
        tooltipLabel.setFont(f);
        //add space at the sides
        Border b = BorderFactory.createEmptyBorder(0,3,0,3);
        tooltipLabel.setBorder(b);
        box.add(tooltipLabel, BorderLayout.CENTER);

        setContentPane(box);
    }

    public void setup(int x, int y, String aText)
    {
        tooltipLabel.setText(aText);

        //start with coordinates relative to mozilla canvas
        Point loc = new Point(x,y);
        //relocate coordinates relative to parent window
        SwingUtilities.convertPointToScreen(loc, getParent());
        //add size of the cursor
        loc.translate(15, 15);
        setLocation(loc);

        pack();
    }

    public JLabel getTooltipLabel() {
        return tooltipLabel;
    }

}
