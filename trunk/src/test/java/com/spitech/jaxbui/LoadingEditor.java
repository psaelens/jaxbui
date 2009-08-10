package com.spitech.jaxbui;

/* 
 * ColorEditor.java (compiles with releases 1.3 and 1.4) is used by 
 * TableDialogEditDemo.java.
 */

import generated.project.Loading;
import generated.project.view.LoadingView;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.AbstractButton;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;

import sun.swing.SwingUtilities2;

public class LoadingEditor extends AbstractCellEditor
                         implements TableCellEditor,
			            ActionListener {
    
	Loading currentLoading;
    JButton button;
    LoadingView loadingView;
    JDialog dialog;
    protected static final String EDIT = "edit";

    public LoadingEditor() {
        //Set up the editor (from the table's point of view),
        //which is a button.
        //This button brings up the color chooser dialog,
        //which is the editor from the user's point of view.
        button = new JButton();
        button.setActionCommand(EDIT);
        button.addActionListener(this);
        button.setBorderPainted(false);

        //Set up the dialog that the button brings up.
        loadingView = new LoadingView();
        dialog = createDialog(button,
                                        "Edit Loading",
                                        true,  //modal
                                        loadingView.buildPanel(),
                                        this,  //OK button handler
                                        null); //no CANCEL button handler
    }
    
    public static JDialog createDialog(Component c, String title, boolean modal,
    		JComponent chooserPane, ActionListener okListener,
            ActionListener cancelListener) throws HeadlessException {
            Window window = SwingUtilities.getWindowAncestor(c);
            LoadingDialog dialog;
            if (window instanceof Frame) {
                dialog = new LoadingDialog((Frame)window, title, modal, c, chooserPane,
    					    okListener, cancelListener);
            } else {
                dialog = new LoadingDialog((Dialog)window, title, modal, c, chooserPane,
    					    okListener, cancelListener);
            }
    	return dialog;
        }
    
    static class LoadingDialog extends JDialog {
        private Loading initialColor;
        private JComponent chooserPane;
        private JButton cancelButton;

        public LoadingDialog(Dialog owner, String title, boolean modal,
            Component c, JComponent chooserPane,
            ActionListener okListener, ActionListener cancelListener)
            throws HeadlessException {
            super(owner, title, modal);
    	initColorChooserDialog(c, chooserPane, okListener, cancelListener);
        }

        public LoadingDialog(Frame owner, String title, boolean modal,
            Component c, JComponent chooserPane,
            ActionListener okListener, ActionListener cancelListener)
            throws HeadlessException {
            super(owner, title, modal);
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
//                    ((AbstractButton)e.getSource()).fireActionPerformed(e);
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
//            initialColor = chooserPane.getColor();
            super.show();
        }

        public void reset() {
//            chooserPane.setColor(initialColor);
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

    /**
     * Handles events from the editor button and from
     * the dialog's OK button.
     */
    public void actionPerformed(ActionEvent e) {
        if (EDIT.equals(e.getActionCommand())) {
            //The user has clicked the cell, so
            //bring up the dialog.
//            button.setBackground(currentLoading);
//            loadingView.setColor(currentLoading);
        	loadingView.updateView(currentLoading);
            dialog.setVisible(true);

            //Make the renderer reappear.
            fireEditingStopped();

        } else { //User pressed dialog's "OK" button.
//            currentLoading = loadingView.getColor();
        }
    }

    //Implement the one CellEditor method that AbstractCellEditor doesn't.
    public Object getCellEditorValue() {
        return currentLoading;
    }

    //Implement the one method defined by TableCellEditor.
    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column) {
        currentLoading = (Loading)value;
        return button;
    }
}

