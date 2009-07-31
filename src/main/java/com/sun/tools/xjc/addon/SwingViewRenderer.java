package com.sun.tools.xjc.addon;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.xml.datatype.XMLGregorianCalendar;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.addon.PluginImpl.JDateField;
import com.sun.tools.xjc.addon.PluginImpl.JNumberField;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

public class SwingViewRenderer {
	
	private Map<JType, Class<? extends JComponent>> componentsMap = new HashMap<JType, Class<? extends JComponent>>();
	private Map<Class<? extends JComponent>, String> componentSettersMap = new HashMap<Class<? extends JComponent>, String>();

	private final ViewGenerator viewGenerator;
	
	private final Outline outline;

	private final JCodeModel codeModel;
	
	JDefinedClass _class;
	JMethod _initComponents;
	JMethod _updateView;
	JMethod _updateModel;
	JMethod _buildPanel;
	JVar _builder;
	JVar _modelForUpdateView;
	
	SwingViewRenderer(ViewGenerator viewGenerator, Outline outline, CClassInfo bean) {
		this.viewGenerator = viewGenerator;
		this.outline = outline;
		this.codeModel = outline.getCodeModel();
		
		/*
		 * !! static ?
		 */
		componentsMap.put(codeModel._ref(String.class), JTextField.class);
		componentSettersMap.put(JTextField.class, "setText");
		componentsMap.put(codeModel._ref(Number.class), JNumberField.class);
		componentsMap.put(codeModel._ref(Integer.class), JNumberField.class);
		componentsMap.put(codeModel._ref(BigInteger.class), JNumberField.class);
		componentsMap.put(codeModel._ref(BigDecimal.class), JNumberField.class);
		componentSettersMap.put(JNumberField.class, "setNumber");
		componentsMap.put(codeModel._ref(Boolean.class), JCheckBox.class);
		componentSettersMap.put(JCheckBox.class, "setSelected");
		componentsMap.put(codeModel._ref(XMLGregorianCalendar.class), JDateField.class);
		componentSettersMap.put(JDateField.class, "setDate");
		
		
		try {
			String fullyqualifiedName = bean.getOwnerPackage().subPackage(
					"view2").name()
					+ "." + bean.getName() + "View";
			_class = codeModel._class(fullyqualifiedName);
		} catch (JClassAlreadyExistsException e) {
			// it's OK for this to collide.
			_class = e.getExistingClass();
		}

		_updateModel = _class.method(JMod.PUBLIC, codeModel.VOID,
				"updateModel");
		JVar _updateModelParam1 = _updateModel.param(Object.class, "model");
		_updateModel.javadoc().add("Writes view contents to the given model.");
		_updateModel.javadoc().addParam(_updateModelParam1).add(
				"the object to write this editor's value to");

		_updateView = _class.method(JMod.PUBLIC, codeModel.VOID,
				"updateView");
		JVar _updateViewParam1 = _updateView.param(Object.class, "model");
		_updateView.javadoc().add("Reads view contents from the given model.");
		_updateView.javadoc().addParam(_updateViewParam1).add(
				"the object to read the values from");
		_updateView.body()._if(_updateViewParam1.eq(JExpr._null()))._then()
				._return();
		_modelForUpdateView = _updateView.body()
				.decl(
						outline.getClazz(bean).implClass,
						bean.shortName,
						JExpr.cast(outline.getClazz(bean).implClass,
								_updateViewParam1));

		_initComponents = _class.method(JMod.PROTECTED, codeModel.VOID,
				"initComponents");
		_initComponents.javadoc().add(
				"Creates and configures the UI components.");

		_buildPanel = _class.method(JMod.PUBLIC, JComponent.class,
				"buildPanel");
		_buildPanel.javadoc().add("Builds and returns this editor's panel.");
		_buildPanel.body().invoke(_initComponents);
		_buildPanel
				.body()
				.decl(
						codeModel._ref(FormLayout.class),
						"layout",
						JExpr
								.direct("new FormLayout(\"right:pref, 3dlu, 150dlu:grow\")"));
		_builder = _buildPanel.body().decl(
				codeModel._ref(DefaultFormBuilder.class), "builder",
				JExpr.direct("new DefaultFormBuilder(layout)"));
	}

	public void handleSimpleType(ClassOutline cc, CPropertyInfo prop) {
		// Handle Default Java Data Type
		Class<? extends JComponent> componentClass = componentsMap
				.get(prop.baseType.boxify());
		JType _componentRef = codeModel._ref(componentClass);
		JFieldVar _field = _class.field(JMod.PROTECTED,
				_componentRef, prop.getName(false) + "Field");
		_initComponents.body().assign(
				_field,
				JExpr._new(_componentRef));
		JMethod getterMethod = getMethod(cc.implClass, "(?i)(?:get|is)" + prop.getName(false), prop.baseType, new JType[0]);
		if (getterMethod != null) {
			_updateView.body().invoke(_field, componentSettersMap.get(componentClass)).arg(JExpr.invoke(_modelForUpdateView, getterMethod));
		} else {
			System.err.println("setter method for property ["
					+ prop.getName(false) + "] of bean [" + cc.ref.name()
					+ "] not found.");
		}
		_buildPanel.body().invoke(_builder, "append").arg(
				prop.getName(false)).arg(_field);
	}
	
	public void handleComplexType(ClassOutline cc, CPropertyInfo prop) {
		SwingViewRenderer viewRenderer = viewGenerator.getViewRenderer(outline.getModel().getClassInfo(prop.getAdapter().adapterType));
		JFieldVar _viewVar = _class.field(JMod.PROTECTED,
				viewRenderer._class, prop.getName(false) + "View");
		JVar _panel = _buildPanel.body().decl(codeModel._ref(JComponent.class), prop.getName(false) + "Panel", JExpr.invoke(_viewVar, "buildPanel"));
		_buildPanel.body().invoke(_builder, "append").arg(_panel)
		.arg(_builder.invoke("getColumnCount"));
		_buildPanel.body().invoke(_panel, "setBorder").arg(
				JExpr._new(codeModel._ref(TitledBorder.class)).arg(
						prop.getName(false)));
		_initComponents.body().assign(
				_viewVar,
				JExpr._new(viewRenderer._class));
		JMethod getterMethod = getMethod(cc.implClass, "(?i)(?:get|is)" + prop.getName(false), prop.baseType, new JType[0]);
		if (getterMethod != null) {
			_updateView.body().invoke(_viewVar, "updateView").arg(JExpr.invoke(_modelForUpdateView, getterMethod));
		} else {
			System.err.println("setter method for property ["
					+ prop.getName(false) + "] of bean [" + cc.ref.name()
					+ "] not found.");
		}
	}
	
	/**
     * Looks for a method that has the specified method signature
     * and return it.
     * 
     * @return
     *      null if not found.
     */
    public JMethod getMethod(JDefinedClass definedClass, String regex, JType returnType, JType[] argTypes) {
        for (JMethod m : definedClass.methods()) {
            if (!m.name().matches(regex))
                continue;

            if (m.type().equals(returnType) && m.hasSignature(argTypes))
                return m;
        }
        return null;
    }


}
