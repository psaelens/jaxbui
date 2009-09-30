//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-34 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.07.28 at 04:16:42 PM CEST 
//


package generated.view;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import generated.PurchaseOrderType;

public class PurchaseOrderTypeView {

    PresentationModel presentationModel;
    protected JComponent shipToPanel;
    protected JComponent billToPanel;
    protected JComponent commentField;
    protected JComponent itemsField;
    protected JComponent orderDateField;

    public PurchaseOrderTypeView(PurchaseOrderType purchaseOrderType) {
        presentationModel = new PresentationModel(purchaseOrderType);
    }

    public PurchaseOrderTypeView() {
    }

    /**
     * Writes view contents to the given model.
     * 
     * @param model
     *     the object to write this editor's value to
     */
    public void updateModel(Object model) {
    }

    /**
     * Reads view contents from the given model.
     * 
     * @param model
     *     the object to read the values from
     */
    public void updateView(Object model) {
    }

    /**
     * Creates and configures the UI components.
     * 
     */
    protected void initComponents() {
        shipToPanel = new USAddressView().buildPanel();
        shipToPanel.setBorder(new TitledBorder("shipTo"));
        billToPanel = new USAddressView().buildPanel();
        billToPanel.setBorder(new TitledBorder("billTo"));
        commentField = new JTextField();
        itemsField = new JTextField();
        orderDateField = new JTextField();
    }

    /**
     * Builds and returns this editor's panel.
     * 
     */
    public JComponent buildPanel() {
        initComponents();
        FormLayout layout = (new FormLayout("right:pref, 3dlu, 150dlu:grow"));
        DefaultFormBuilder builder = (new DefaultFormBuilder(layout));
        builder.append(shipToPanel, builder.getColumnCount());
        builder.append(billToPanel, builder.getColumnCount());
        builder.append("comment", commentField);
        builder.append("items", itemsField);
        builder.append("orderDate", orderDateField);
        return builder.getPanel();
    }

}