package com.sun.tools.xjc.addon;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import javax.xml.datatype.XMLGregorianCalendar;

import com.jgoodies.binding.formatter.EmptyDateFormatter;
import com.jgoodies.binding.formatter.EmptyNumberFormatter;
import com.sun.codemodel.JClass;
import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.CTypeInfo;

public class FormatterFactory {

	private Map<CBuiltinLeafInfo, Class<?>> map = new HashMap<CBuiltinLeafInfo, Class<?>>();
	
	public FormatterFactory() {
		// String <-> JTextField
		// Boolean <-> JCheckBox
		map.put(CBuiltinLeafInfo.INT, Integer.class);
		map.put(CBuiltinLeafInfo.LONG, Long.class);
		map.put(CBuiltinLeafInfo.BYTE, Byte.class);
		map.put(CBuiltinLeafInfo.SHORT, Short.class);
		map.put(CBuiltinLeafInfo.FLOAT, Short.class);
		map.put(CBuiltinLeafInfo.DOUBLE, Short.class);
		
		// XMLGregorianCalendar
		map.put(CBuiltinLeafInfo.CALENDAR, XMLGregorianCalendar.class);
		
	}
	
	private AbstractFormatterFactory createFormatterFactory(Class<?> valueClass) {
		if (Integer.class == valueClass || Long.class == valueClass
				|| Byte.class == valueClass || Short.class == valueClass 
				|| Float.class == valueClass || Double.class == valueClass) {
			NumberFormatter defaultFormat = new NumberFormatter(getNumberFormat(valueClass));
			defaultFormat.setValueClass(valueClass);
			return new DefaultFormatterFactory(defaultFormat, // default
					defaultFormat, // display
					defaultFormat, // edit
					new EmptyNumberFormatter() // null
			);
		} else if (XMLGregorianCalendar.class == valueClass) {
	        DateFormat shortFormat = DateFormat.getDateInstance(DateFormat.SHORT);
	        shortFormat.setLenient(false);
	        
	        JFormattedTextField.AbstractFormatter defaultFormatter = 
	            new EmptyDateFormatter(shortFormat);
	        JFormattedTextField.AbstractFormatter displayFormatter = 
	            new EmptyDateFormatter();
	        
            return new DefaultFormatterFactory(
            		defaultFormatter, 
            		displayFormatter);
		}
		
		
		return new DefaultFormatterFactory();
	}
	

	private NumberFormat getNumberFormat(Class<?> valueClass) {
		if (Integer.class == valueClass || Long.class == valueClass
				|| Byte.class == valueClass || Short.class == valueClass) {
			return NumberFormat.getIntegerInstance();
		}
		return NumberFormat.getNumberInstance();
	}
	
	
	public AbstractFormatterFactory getFactory(CTypeInfo typeInfo) {
		
		return null;
	}
}
