/**
 * 
 */
package com.spitech.jaxbui.swing;

import java.text.DateFormat;

import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatterFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.jgoodies.binding.formatter.EmptyDateFormatter;

public class JDateField extends JFormattedTextField {

	public JDateField() {
		super();
		DateFormat shortFormat = DateFormat
				.getDateInstance(DateFormat.SHORT);
		shortFormat.setLenient(false);

		JFormattedTextField.AbstractFormatter defaultFormatter = new EmptyDateFormatter(
				shortFormat);
		JFormattedTextField.AbstractFormatter displayFormatter = new EmptyDateFormatter();
		DefaultFormatterFactory formatterFactory = new DefaultFormatterFactory(
				defaultFormatter, displayFormatter);
		setFormatterFactory(formatterFactory);
	}
	
	public void setDate(XMLGregorianCalendar calendar) {
		if (calendar != null) {
			setValue(calendar.toGregorianCalendar().getTime());
		} else {
			setText(null);
		}
	}
}