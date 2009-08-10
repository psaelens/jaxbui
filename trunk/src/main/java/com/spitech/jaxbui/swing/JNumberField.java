/**
 * 
 */
package com.spitech.jaxbui.swing;

import java.text.NumberFormat;

import javax.swing.JFormattedTextField;

public class JNumberField extends JFormattedTextField {

	public JNumberField() {
		super(NumberFormat.getNumberInstance());
	}
	
	public void setNumber(Number number) {
		if (number != null) {
			setText(number.toString());
		} else {
			setText(null);
		}
	}
}