package com.sun.tools.xjc.addon;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.jgoodies.binding.adapter.AbstractTableAdapter;
import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.list.IndirectListModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.spitech.uiskeleton.component.JDateField;
import com.spitech.uiskeleton.component.JNumberField;
import com.spitech.uiskeleton.view.editor.AbstractEditor;
import com.sun.codemodel.JArray;
import com.sun.codemodel.JCase;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JSwitch;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CTypeInfo;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

public class SwingViewRenderer {
	
	private Map<JType, Class<? extends JComponent>> componentsMap = new HashMap<JType, Class<? extends JComponent>>();
	private Map<Class<? extends JComponent>, String> componentSettersMap = new HashMap<Class<? extends JComponent>, String>();
	private Map<Class<? extends JComponent>, String> componentGettersMap = new HashMap<Class<? extends JComponent>, String>();

	private final ViewGenerator viewGenerator;
	
	private final Outline outline;

	private final ClassOutline classOutline;
	
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
		this.classOutline = outline.getClazz(bean);
		this.codeModel = outline.getCodeModel();
		
		/*
		 * !! static ?
		 */
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
		componentsMap.put(codeModel._ref(XMLGregorianCalendar.class), JDateField.class);
		componentSettersMap.put(JDateField.class, "setDate");
		componentGettersMap.put(JDateField.class, "getDate");
		
		
		try {
//			System.out.println("OwnerPackage:" + bean.getOwnerPackage().name());
//			System.out.println("subPackage:" + bean.getOwnerPackage().subPackage("view2").name());
			String fullyqualifiedName = bean.getOwnerPackage().subPackage(
					"view").name()
					+ ".editor." + bean.shortName + "Editor";
			_class = codeModel._class(fullyqualifiedName);
		} catch (JClassAlreadyExistsException e) {
			// it's OK for this to collide.
			_class = e.getExistingClass();
		}

		_class._extends(AbstractEditor.class);
		
		
		viewGenerator.registerEditor(classOutline.implRef, _class);
		
		JMethod _constructor = _class.constructor(JMod.PUBLIC);
		_constructor.body().invoke("super").arg("");
		
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
						classOutline.implClass,
						StringUtils.uncapitalize(bean.shortName),
						JExpr.cast(classOutline.implClass,
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

	public void end() {
		_buildPanel.body()._return(_builder.invoke("getPanel"));
	}

	public void handleSimpleType(CPropertyInfo prop) {
//		int i = 0;
		if (prop.ref().isEmpty()) {
			System.err.println("unhandled property [" + prop.displayName() + "] - no type info");
		} else if (prop.ref().size() > 1) {
			System.err.println("unhandled property [" + prop.displayName() + "] - more than one type info");
		} else {
			String name = prop.getName(false);
			for (CTypeInfo typeInfo : prop.ref()) {
//			String name = prop.getName(false) + "$" + (++i);
				JType type = typeInfo.toType(outline, Aspect.EXPOSED);
				Class<? extends JComponent> componentClass = componentsMap.get(type);
				if (componentClass == null) {
					System.err.println("component not found for type [" + type + "]");
				} else {
					JType _componentRef = codeModel._ref(componentClass);
					JFieldVar _field = _class.field(JMod.PROTECTED,
							_componentRef, name + "Field");
					_initComponents.body().assign(
							_field,
							JExpr._new(_componentRef));
					JMethod getterMethod = getMethod(classOutline.implClass, "(?i)(?:get|is)" + name, type, new JType[0]);
					JMethod setterMethod = getMethod(classOutline.implClass, "(?i)set" + name, codeModel.VOID, new JType[]{type});
					if (getterMethod != null) {
						_updateView.body().invoke(_field, componentSettersMap.get(componentClass)).arg(JExpr.invoke(_modelForUpdateView, getterMethod));
					}
					if (setterMethod != null) {
//						_updateModel.body().invoke(_modelForUpdateView, setterMethod).arg(JExpr.invoke(_field, componentGettersMap.get(componentClass)));
					}
					
					_buildPanel.body().invoke(_builder, "append").arg(
							name).arg(_field);
				}
			}
		}
	}

	public void handleComplexType(CPropertyInfo prop) {
//		int i = 0;
		if (prop.ref().isEmpty()) {
			System.err.println("unhandled property [" + prop.displayName() + "] - no type info");
		} else if (prop.ref().size() > 1) {
			System.err.println("unhandled property [" + prop.displayName() + "] - more than one type info");
		} else {
//			if (prop.kind() == PropertyKind.ELEMENT) {
//				CElementPropertyInfo element = (CElementPropertyInfo)prop;
//				for (CTypeRef type : element.getTypes()) {
//					debug(type);
//				}
//			}
			String name = prop.getName(false);
			for (CTypeInfo typeInfo : prop.ref()) {
	//			final String name = prop.getName(false) + "$" + (++i);
				CClassInfo bean = outline.getModel().beans().get(typeInfo);
				if (CBuiltinLeafInfo.LEAVES.containsValue(typeInfo)) {
					// BUILT-IN Types
					handleSimpleType(prop);
					return;
				}
				if (bean == null) {
					System.out.println("CClassInfo (CPropertyInfo = " + ToStringBuilder.reflectionToString(prop, ToStringStyle.MULTI_LINE_STYLE) + ") not found for CTypeInfo " + ToStringBuilder.reflectionToString(typeInfo, ToStringStyle.MULTI_LINE_STYLE));
					return;
				}
				SwingViewRenderer viewRenderer = viewGenerator.getViewRenderer(bean);
				JFieldVar _viewVar = _class.field(JMod.PROTECTED,
						viewRenderer._class, name + "View");
				JVar _panel = _buildPanel.body().decl(codeModel._ref(JComponent.class), name + "Panel", JExpr.invoke(_viewVar, "buildPanel"));
				_buildPanel.body().invoke(_builder, "append").arg(_panel)
				.arg(_builder.invoke("getColumnCount"));
				_buildPanel.body().invoke(_panel, "setBorder").arg(
						JExpr._new(codeModel._ref(TitledBorder.class)).arg(
								name));
				_initComponents.body().assign(
						_viewVar,
						JExpr._new(viewRenderer._class));
				JMethod getterMethod = getMethod(classOutline.implClass, "(?i)(?:get|is)" + name, typeInfo.toType(outline, Aspect.EXPOSED), new JType[0]);
				if (getterMethod != null) {
					_updateView.body().invoke(_viewVar, "updateView").arg(JExpr.invoke(_modelForUpdateView, getterMethod));
				} else {
					System.err.println("getter method for property ["
							+ name + "] of bean [" + classOutline.ref.name()
							+ "] not found.");
				}
			}
		}
	}

	private void debug(Object o) {
		System.out.println(toString(o));
	}

	private String toString(Object o) {
		return ToStringBuilder.reflectionToString(o, ToStringStyle.MULTI_LINE_STYLE);
	}
	
	
	public void handleCollection(CPropertyInfo prop) {
		if (prop.ref().isEmpty()) {
			System.err.println("unhandled property [" + prop.displayName()
					+ "] - no type info");
		} else if (prop.ref().size() > 1) {
			System.err.println("unhandled property [" + prop.displayName()
					+ "] - more than one type info");
		} else {
			String propertyName = prop.getName(false);
			for (CTypeInfo typeInfo : prop.ref()) {
				JType paramType = typeInfo.toType(outline, Aspect.EXPOSED);
				System.out.println("type=" + paramType);
//				if (((JClass) paramType).isParameterized()) {
					JType type = paramType;//((JClass) paramType).getTypeParameters().get(0);
					if (componentsMap.containsKey(type)) {
						// Collection of Default Data Type
						JFieldVar _field = _class.field(JMod.PROTECTED,
								JList.class, propertyName + "List");
						_initComponents.body().assign(_field,
								JExpr._new(codeModel._ref(JList.class)));
						_buildPanel.body().invoke(_builder, "append").arg(
								JExpr._new(codeModel._ref(JScrollPane.class))
										.arg(_field)).arg(
								_builder.invoke("getColumnCount"));
						JClass listT = codeModel.ref(List.class).narrow(type.boxify());
						JMethod getterMethod = getMethod(
								classOutline.implClass, "(?i)(?:get|is)"
										+ propertyName, listT, new JType[0]);
						if (getterMethod != null) {
							_updateView
									.body()
									.invoke(_field, "setModel")
									.arg(
											JExpr
													._new(
															codeModel
																	.ref(IndirectListModel.class).narrow(type.boxify()))
													.arg(
															JExpr
																	.invoke(
																			_modelForUpdateView,
																			getterMethod)));
						} else {
							System.err.println("[handleCollection] getter method for property ["
									+ propertyName + "] of bean ["
									+ classOutline.ref.name() + "] not found.");
						}
					} else if (outline.getModel().beans().containsKey(
							typeInfo)) {
						// Collection of Generic Type
						SwingViewRenderer viewRenderer = viewGenerator
								.getViewRenderer(outline.getModel().beans()
										.get(typeInfo));
						JDefinedClass _tableAdapter = viewRenderer
								.generateTableAdapter();
						JFieldVar _field = _class.field(JMod.PROTECTED,
								JTable.class, propertyName + "Table");
						_initComponents.body().assign(
								_field,
								JExpr._new(codeModel._ref(JTable.class)).arg(
										JExpr._new(_tableAdapter)));
						_buildPanel.body().invoke(_builder, "append").arg(
								JExpr._new(codeModel._ref(JScrollPane.class))
										.arg(_field)).arg(
								_builder.invoke("getColumnCount"));
						JClass listT = codeModel.ref(List.class).narrow(type.boxify());
						JMethod getterMethod = getMethod(
								classOutline.implClass, "(?i)(?:get|is)"
										+ propertyName, listT, new JType[0]);
						if (getterMethod != null) {
							_updateView.body().invoke(_field, "setModel").arg(
									JExpr._new(_tableAdapter).arg(
											JExpr.invoke(_modelForUpdateView,
													getterMethod)));
						} else {
							System.err.println("getter method for property ["
									+ propertyName + "] of bean ["
									+ classOutline.ref.name() + "] not found.");
						}
					} else {
						System.out.println("unhandle property ["
								+ prop.displayName() + "] - " + typeInfo + " not found.");
					}
//				} else {
//					System.out.println("unhandle property ["
//							+ prop.displayName() + "] - TODO handle not parametrized type");
//				}
			}
		}
	}
	
	public void handleEnumeration(CPropertyInfo prop) {
		if (prop.ref().isEmpty()) {
			System.err.println("unhandled property [" + prop.displayName() + "] - no type info");
		} else if (prop.ref().size() > 1) {
			System.err.println("unhandled property [" + prop.displayName() + "] - more than one type info");
		} else {
			String propertyName = prop.getName(false);
			for (CTypeInfo typeInfo : prop.ref()) {
				JType type = typeInfo.toType(outline, Aspect.EXPOSED);
				JFieldVar _field = _class.field(JMod.PROTECTED,
						JComboBox.class, propertyName + "Field");
				_initComponents.body().assign(
						_field,
						JExpr._new(codeModel._ref(JComboBox.class)).arg(
								((JDefinedClass) type)
								.staticInvoke("values")));
				_buildPanel.body().invoke(_builder, "append").arg(
						propertyName).arg(_field);
				JMethod getterMethod = getMethod(classOutline.implClass, "(?i)(?:get|is)" + propertyName, type, new JType[0]);
				if (getterMethod != null) {
					_updateView.body().invoke(_field, "setSelectedItem").arg(JExpr.invoke(_modelForUpdateView, getterMethod));
				} else {
					System.err.println("getter method for property ["
							+ propertyName + "] of bean [" + classOutline.ref.name()
							+ "] not found.");
				}
			}
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

            if ((m.type().equals(returnType) 
            		|| (returnType != null && m.type().equals(returnType.unboxify()))) 
            		&& m.hasSignature(argTypes))
                return m;
        }
        System.err.println("method not found definedClass=" + (definedClass != null ? definedClass.name() : null) 
        		+ " regex=" + regex 
        		+ " returnType=" + (returnType != null ? returnType.name() : null) 
				+ " argTypes=" + (argTypes != null ? Arrays.asList(argTypes) : null));
        return null;
    }

//	 scenarioTable.addMouseListener(new MouseAdapter() {
//	@Override
//	public void mouseClicked(MouseEvent e) {
//		if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
//			JTable table = (JTable)e.getComponent();
//			if (table.getSelectedRowCount() == 1) {
//				/*
//				 * select scenario
//				 */
//				ScenarioTableAdapter tableModel = (ScenarioTableAdapter) table.getModel();
//				Scenario scenario = tableModel.getScenario(table.getSelectedRow());
//				
//				/*
//				 * create view with the selected scenario
//				 */
//				JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(table));
//				ScenarioView scenarioView = new ScenarioView();
//				dialog.getContentPane().add(scenarioView.buildPanel());
//				scenarioView.updateView(scenario);
//				dialog.pack();
//				dialog.setVisible(true);
//			}
//		}
//	}
//});
    
    JDefinedClass generateNode() {
    	JCodeModel codeModel = outline.getCodeModel();
    	JDefinedClass implClass = classOutline.implClass;
    	JDefinedClass _class;
    	
    	JPackage _package = implClass._package();
    	String fullyqualifiedName = _package.name() + "." + "node."
    	+ implClass.name() + "Node";
    	try {
    		_class = codeModel._class(fullyqualifiedName);
    	} catch (JClassAlreadyExistsException e) {
    		// ok, just retrieve existing class
    		_class = e.getExistingClass();
    	}
    	
    	
    	_class._extends(AbstractTableAdapter.class);

    	return _class;
    }
    
    JDefinedClass generateTableAdapter() {
		JCodeModel codeModel = outline.getCodeModel();
		JDefinedClass implClass = classOutline.implClass;

			System.out.println("** Generate Table Adapter for ["
					+ implClass + "]");

			JDefinedClass _class;
			JPackage _package = implClass._package();
			String fullyqualifiedName = _package.name() + "." + "adapter."
					+ implClass.name() + "TableAdapter";
			try {
				_class = codeModel._class(fullyqualifiedName);
			} catch (JClassAlreadyExistsException e) {
				// ok, just retrieve existing class
				_class = e.getExistingClass();
			}
			

			_class._extends(AbstractTableAdapter.class);
			
	

			// Instance Creation
			// ****************************************************

			JArray _columnNames = JExpr.newArray(codeModel._ref(String.class));
			JFieldVar _field = _class.field(JMod.PRIVATE | JMod.STATIC 
					| JMod.FINAL, codeModel._ref(String[].class), "COLUMN_NAMES", _columnNames);
			
			JMethod _constructor = _class.constructor(JMod.PUBLIC);
			
			JVar _param = _constructor.param(codeModel.ref(List.class).narrow(implClass), classOutline.target.shortName);
			_constructor.body().invoke("super")
				.arg(JExpr._new(codeModel._ref(ArrayListModel.class)).arg(_param))
				.arg(_field);
			
			JMethod _constructor2 = _class.constructor(JMod.PUBLIC);
			_constructor2.body().invoke("super")
			.arg(JExpr._new(codeModel._ref(ArrayListModel.class)))
			.arg(_field);
			
			// Implements table adapter
			// *****************************************
			JType OBJECT = codeModel.ref(Object.class);
			
			JMethod _getValueAt = _class.method(JMod.PUBLIC, OBJECT,
					"getValueAt");
			JVar _rowIndex = _getValueAt.param(int.class, "rowIndex");
			JVar _columnIndex = _getValueAt.param(int.class, "columnIndex");
			_getValueAt.javadoc().add(
					"TODO javadoc.");
			_getValueAt.javadoc().addParam(_rowIndex).add(
					"index of the row");
			_getValueAt.javadoc().addParam(_columnIndex).add(
					"index of the column");
			
			JVar _row = _getValueAt.body().decl(implClass,  classOutline.target.shortName, 
					JExpr.cast(implClass, JExpr._this().invoke("getRow").arg(_rowIndex)));

			JSwitch _switch = _getValueAt.body()._switch(_columnIndex);
			int i = 0;
			for (Entry<String, JFieldVar> entry : implClass.fields().entrySet()) {
				_columnNames.add(JExpr.lit(entry.getKey()));
				JCase _case = _switch._case(JExpr.lit(i++));
				_case.body()._return(_row.invoke(getMethod(implClass, "(?i)(?:get|is)" +  entry.getKey(), entry.getValue().type(), new JType[0])));
			}
			_switch._default().body()._return(JExpr._null());
			
			return _class;
	}
}
