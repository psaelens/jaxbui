//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-34 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.07.28 at 04:16:42 PM CEST 
//


package generated.view;

import javax.swing.JComponent;
import javax.swing.JTextField;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ItemView {

    PresentationModel presentationModel;
    protected JComponent productNameField;
    protected JComponent quantityField;
    protected JComponent usPriceField;
    protected JComponent commentField;
    protected JComponent shipDateField;
    protected JComponent partNumField;

    public ItemView(generated.Items.Item item) {
        presentationModel = new PresentationModel(item);
    }

    public ItemView() {
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
        productNameField = new JTextField();
        quantityField = new JTextField();
        usPriceField = new JTextField();
        commentField = new JTextField();
        shipDateField = new JTextField();
        partNumField = new JTextField();
    }

    /**
     * Builds and returns this editor's panel.
     * 
     */
    public JComponent buildPanel() {
        initComponents();
        FormLayout layout = (new FormLayout("right:pref, 3dlu, 150dlu:grow"));
        DefaultFormBuilder builder = (new DefaultFormBuilder(layout));
        builder.append("productName", productNameField);
        builder.append("quantity", quantityField);
        builder.append("usPrice", usPriceField);
        builder.append("comment", commentField);
        builder.append("shipDate", shipDateField);
        builder.append("partNum", partNumField);
        return builder.getPanel();
    }

}
