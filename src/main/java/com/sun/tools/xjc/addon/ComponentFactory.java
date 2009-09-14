package com.sun.tools.xjc.addon;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.xml.datatype.XMLGregorianCalendar;

import com.spitech.uiskeleton.component.JDateField;
import com.spitech.uiskeleton.component.JNumberField;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;

class ComponentFactory {

	private Map<JType, Class<? extends JComponent>> componentsMap = new HashMap<JType, Class<? extends JComponent>>();
	private Map<Class<? extends JComponent>, String> componentSettersMap = new HashMap<Class<? extends JComponent>, String>();
	private Map<Class<? extends JComponent>, String> componentGettersMap = new HashMap<Class<? extends JComponent>, String>();

	
	ComponentFactory(JCodeModel codeModel) {
		componentsMap.put(codeModel._ref(String.class), JTextField.class);
		componentSettersMap.put(JTextField.class, "setText");
		componentGettersMap.put(JTextField.class, "getText");
		componentsMap.put(codeModel._ref(Number.class), JNumberField.class);
		componentsMap.put(codeModel._ref(Byte.class), JNumberField.class);
		componentsMap.put(codeModel._ref(Integer.class), JNumberField.class);
		componentsMap.put(codeModel._ref(BigInteger.class), JNumberField.class);
		componentsMap.put(codeModel._ref(BigDecimal.class), JNumberField.class);
		componentSettersMap.put(JNumberField.class, "setNumber");
		componentGettersMap.put(JNumberField.class, "getNumber");
		componentsMap.put(codeModel._ref(Boolean.class), JCheckBox.class);
		componentSettersMap.put(JCheckBox.class, "setSelected");
		componentGettersMap.put(JCheckBox.class, "isSelected");
		componentsMap.put(codeModel._ref(XMLGregorianCalendar.class),
				JDateField.class);
		componentSettersMap.put(JDateField.class, "setDate");
		componentGettersMap.put(JDateField.class, "getDate");
	}


	public Class<? extends JComponent> getComponent(JType type) {
		return componentsMap.get(type);
	}


	public String getSetter(Class<? extends JComponent> componentClass) {
		return componentSettersMap.get(componentClass);
	}


	public String getGetter(Class<? extends JComponent> componentClass) {
		return componentGettersMap.get(componentClass);
	}

	public boolean contains(JType type) {
		return componentsMap.containsKey(type);
	}
}
