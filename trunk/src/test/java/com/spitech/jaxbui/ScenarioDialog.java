package com.spitech.jaxbui;

import generated.project.Loading;
import generated.project.Scenario;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import sun.swing.SwingUtilities2;

import com.spitech.jaxbui.LoadingEditor.LoadingDialog.Closer;
import com.spitech.jaxbui.LoadingEditor.LoadingDialog.DisposeOnClose;

public class ScenarioDialog extends JDialog {
    private Scenario initialColor;
    private JComponent chooserPane;
    private JButton cancelButton;

    public ScenarioDialog(Dialog owner, String title, boolean modal,
        Component c, JComponent chooserPane,
        ActionListener okListener, ActionListener cancelListener)
        throws HeadlessException {
        super(owner, title, modal);
	initColorChooserDialog(c, chooserPane, okListener, cancelListener);
    }

    public ScenarioDialog(Frame owner, String title, boolean modal,
        Component c, JComponent chooserPane,
        ActionListener okListener, ActionListener cancelListener)
        throws HeadlessException {
        super(owner, title, modal);
	initColorChooserDialog(c, chooserPane, okListener, cancelListener);
    }
    
    public ScenarioDialog(Window owner, String title, boolean modal,
    		Component c, JComponent chooserPane,
    		ActionListener okListener, ActionListener cancelListener)
    throws HeadlessException {
    	super(owner, title);
    	initColorChooserDialog(c, chooserPane, okListener, cancelListener);
    }

    protected void initColorChooserDialog(Component c, JComponent chooserPane,
	ActionListener okListener, ActionListener cancelListener) {
        //setResizable(false);

        this.chooserPane = chooserPane;

	String okString = UIManager.getString("ColorChooser.okText");
	String cancelString = UIManager.getString("ColorChooser.cancelText");
	String resetString = UIManager.getString("ColorChooser.resetText");

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(chooserPane, BorderLayout.CENTER);

        /*
         * Create Lower button panel
         */
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton(okString);
	getRootPane().setDefaultButton(okButton);
        okButton.setActionCommand("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });
        if (okListener != null) {
            okButton.addActionListener(okListener);
        }
        buttonPane.add(okButton);

        cancelButton = new JButton(cancelString);

	// The following few lines are used to register esc to close the dialog
	Action cancelKeyAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
//                ((AbstractButton)e.getSource()).fireActionPerformed(e);
            }
        }; 
	KeyStroke cancelKeyStroke = KeyStroke.getKeyStroke((char)KeyEvent.VK_ESCAPE, false);
	InputMap inputMap = cancelButton.getInputMap(JComponent.
						     WHEN_IN_FOCUSED_WINDOW);
	ActionMap actionMap = cancelButton.getActionMap();
	if (inputMap != null && actionMap != null) {
	    inputMap.put(cancelKeyStroke, "cancel");
	    actionMap.put("cancel", cancelKeyAction);
	}
	// end esc handling

        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });
        if (cancelListener != null) {
            cancelButton.addActionListener(cancelListener);
        }
        buttonPane.add(cancelButton);

        JButton resetButton = new JButton(resetString);
        resetButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               reset();
           }
        });
        int mnemonic = SwingUtilities2.getUIDefaultsInt("ColorChooser.resetMnemonic", -1);
        if (mnemonic != -1) {
            resetButton.setMnemonic(mnemonic);
        }
        buttonPane.add(resetButton);
        contentPane.add(buttonPane, BorderLayout.SOUTH);

        if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations = 
            UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations) {
                getRootPane().setWindowDecorationStyle(JRootPane.COLOR_CHOOSER_DIALOG);
            }
        }
        applyComponentOrientation(((c == null) ? getRootPane() : c).getComponentOrientation());

        pack();
        setLocationRelativeTo(c);

        this.addWindowListener(new Closer());
        this.addComponentListener(new DisposeOnClose());
    }

    public void show() {
//        initialColor = chooserPane.getColor();
        super.show();
    }

    public void reset() {
//        chooserPane.setColor(initialColor);
    }

    class Closer extends WindowAdapter implements Serializable{
        public void windowClosing(WindowEvent e) {
            cancelButton.doClick(0);
            Window w = e.getWindow();
            w.hide();
        }
    }

    static class DisposeOnClose extends ComponentAdapter implements Serializable{
        public void componentHidden(ComponentEvent e) {
            Window w = (Window)e.getComponent();
            w.dispose();
        }
    }

}
