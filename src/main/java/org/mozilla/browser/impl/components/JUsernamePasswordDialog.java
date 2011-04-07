package org.mozilla.browser.impl.components;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

import org.mozilla.browser.mt;

/**
 * Dialog for entering username+password
 * or password only
 */
public class JUsernamePasswordDialog extends JDialog
{

    private static final long serialVersionUID = 3492572931924246920L;

    private JTextField tfUsername;
    private JPasswordField tfPassword;
    private JCheckBox chkMessage;
    private JButton btOk, btCancel;

    private EventsHandler eventsHandler = new EventsHandler();

    private boolean retVal = false;

    public JUsernamePasswordDialog(Frame parent,
                                   String title,
                                   String message,
                                   String initialUsername,
                                   String initialPassword,
                                   boolean addCheck,
                                   String checkMessage,
                                   boolean initialCheckState)
    {
        super(parent, title, true);
        init(parent,
             message,
             true,
             initialUsername,
             initialPassword,
             addCheck,
             checkMessage,
             initialCheckState);
    }

    public JUsernamePasswordDialog(Frame parent,
                                   String title,
                                   String message,
                                   String initialPassword,
                                   boolean addCheck,
                                   String checkMessage,
                                   boolean initialCheckState)
    {
        super(parent, title, true);
        init(parent,
             message,
             false,
             "", //$NON-NLS-1$
             initialPassword,
             addCheck,
             checkMessage,
             initialCheckState);
    }

    private void init(Frame parent,
                      String text,
                      boolean addUsername,
                      String initialUsername,
                      String initialPassword,
                      boolean addCheck,
                      String checkMessage,
                      boolean initialCheck)
    {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel box = new JPanel();
        Border b = BorderFactory.createEmptyBorder(10,10,10,10);
        box.setBorder(b);
        box.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        int x = 0, y = 0;

        Insets topSpace = new Insets(7,0,0,0);
        Insets top2Space = new Insets(10,0,0,0);
        Insets top3Space = new Insets(20,0,0,0);

        c.weightx = 1.0;

        JPanel p1 = createMessagePanel(text);
        c.gridx = x++; c.gridy = y;
        c.insets = topSpace;
        box.add(p1, c);
        x = 0; y++;

        if (addUsername) {
            JPanel p2 = createUsernamePanel(initialUsername);
            c.gridx = x++; c.gridy = y;
            c.insets = topSpace;
            box.add(p2, c);
            x = 0; y++;
        }

        JPanel p2 = createPasswordPanel(initialPassword);
        c.gridx = x++; c.gridy = y;
        c.insets = topSpace;
        box.add(p2, c);
        x = 0; y++;

        if (addCheck) {
            JPanel p3 = createCheckPanel(checkMessage, initialCheck);
            c.gridx = x++; c.gridy = y;
            c.insets = top2Space;
            box.add(p3, c);
            x = 0; y++;
        }

        JPanel p4 = createButtonsPanel();
        c.gridx = x++; c.gridy = y;
        c.insets = top3Space;
        box.add(p4, c);
        x = 0; y++;

        setContentPane(box);
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);

        JRootPane rp = getRootPane();
        rp.setDefaultButton(btOk);


        if (addUsername) {
            tfUsername.grabFocus();
        } else {
            tfPassword.grabFocus();
        }
    }

    private JPanel createMessagePanel(String message)
    {
        JPanel box = new JPanel();
        box.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        int x = 0, y = 0;

        Insets nullSpace = new Insets(0,0,0,0);
        Insets leftSpace = new Insets(0,5,0,0);

        JLabel lb = new JLabel(UIManager.getIcon("OptionPane.questionIcon")); //$NON-NLS-1$
        c.gridx = x++; c.gridy = y;
        c.weightx = 0.0;
        c.insets = nullSpace;
        box.add(lb, c);

        JMultiLineLabel ml = new JMultiLineLabel();
        ml.setText(message);
        JScrollPane spMessage = new JScrollPane(ml);
        spMessage.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        spMessage.setBorder(null);
        c.gridx = x++; c.gridy = y;
        c.weightx = 1.0; c.weighty = 0.5;
        c.insets = leftSpace;
        box.add(spMessage, c);
        x = 0; y++;

        return box;
    }

    private JPanel createUsernamePanel(String username)
    {
        JPanel box = new JPanel();
        box.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        int x = 0, y = 0;

        Insets nullSpace = new Insets(0,0,0,0);
        Insets leftSpace = new Insets(0,5,0,0);

        JLabel lbUsername = new JLabel(mt.t("JUsernamePasswordDialog.Username")); //$NON-NLS-1$
        c.gridx = x++; c.gridy = y;
        c.weightx = 0.0;
        c.insets = nullSpace;
        box.add(lbUsername, c);

        tfUsername = new JTextField();
        tfUsername.setText(username);
        tfUsername.setSelectionStart(0);
        tfUsername.setSelectionEnd(username.length());
        Dimension d = tfUsername.getPreferredSize();
        d.width = 150;
        tfUsername.setPreferredSize(d);
        c.gridx = x++; c.gridy = y;
        c.weightx = 1.0; c.weighty = 0.5;
        c.insets = leftSpace;
        box.add(tfUsername, c);
        x = 0; y++;

        return box;
    }

    private JPanel createPasswordPanel(String password)
    {
        JPanel box = new JPanel();
        box.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        int x = 0, y = 0;

        Insets nullSpace = new Insets(0,0,0,0);
        Insets leftSpace = new Insets(0,5,0,0);

        JLabel lbPassword = new JLabel(mt.t("JUsernamePasswordDialog.Password")); //$NON-NLS-1$
        c.gridx = x++; c.gridy = y;
        c.weightx = 0.0;
        c.insets = nullSpace;
        box.add(lbPassword, c);

        tfPassword = new JPasswordField();
        tfPassword.setText(password);
        tfPassword.setSelectionStart(0);
        tfPassword.setSelectionEnd(password.length());
        Dimension d = tfPassword.getPreferredSize();
        d.width = 150;
        tfPassword.setPreferredSize(d);
        c.gridx = x++; c.gridy = y;
        c.weightx = 1.0; c.weighty = 0.5;
        c.insets = leftSpace;
        box.add(tfPassword, c);
        x = 0; y++;

        return box;
    }

    private JPanel createButtonsPanel() {
        JPanel box = new JPanel();
        box.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        int x = 0, y = 0;

        Insets nullSpace = new Insets(0,0,0,0);
        Insets leftSpace = new Insets(0,5,0,0);

        c.gridx = x++; c.gridy = y;
        c.weightx = 1.0;
        c.insets = nullSpace;
        box.add(Box.createHorizontalGlue(),c);

        btOk = new JButton(mt.t("JUsernamePasswordDialog.Ok")); //$NON-NLS-1$
        btOk.addActionListener(eventsHandler);
        Dimension d = btOk.getPreferredSize();
        d.width = 80;
        btOk.setPreferredSize(d);
        c.gridx = x++;  c.gridy = y;
        c.weightx = 0.0;
        c.insets = nullSpace;
        box.add(btOk, c);

        btCancel = new JButton(mt.t("JUsernamePasswordDialog.Cancel")); //$NON-NLS-1$
        btCancel.addActionListener(eventsHandler);
        d = btCancel.getPreferredSize();
        d.width = 80;
        btCancel.setPreferredSize(d);
        c.gridx = x++;  c.gridy = y;
        c.weightx = 0.0;
        c.insets = leftSpace;
        box.add(btCancel, c);

        c.gridx = x++; c.gridy = y;
        c.weightx = 1.0;
        c.insets = nullSpace;
        box.add(Box.createHorizontalGlue(),c);
        x = 0; y++;

        return box;
    }

    private JPanel createCheckPanel(String message, boolean checkState)
    {
        JPanel box = new JPanel();
        box.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        int x = 0, y = 0;

        Insets nullSpace = new Insets(0,0,0,0);

        chkMessage = new JCheckBox(message);
        chkMessage.setSelected(checkState);
        c.gridx = x++; c.gridy = y;
        c.weightx = 1.0;
        c.insets = nullSpace;
        box.add(chkMessage, c);

        return box;
    }

    class EventsHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == btOk) {
                retVal = true;
                dispose();
            } else if (e.getSource() == btCancel) {
                retVal = true;
                dispose();
            }
        }
    }

    public boolean run() {
        setVisible(true);
        return retVal;
    }

    public boolean getRetVal() {
        return retVal;
    }

    public String getUsername() {
        if (tfUsername!=null) return ""; //$NON-NLS-1$
        else return tfUsername.getText();
    }

    public String getPassword() {
        return new String(tfPassword.getPassword());
    }

    public boolean getCheckState() {
        return chkMessage.isSelected();
    }

}
