package com.sun.tools.xjc.addon;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultFormatterFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.jgoodies.binding.adapter.AbstractTableAdapter;
import com.jgoodies.binding.beans.Model;
import com.jgoodies.binding.formatter.EmptyDateFormatter;
import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.list.ListHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.codemodel.JArray;
import com.sun.codemodel.JBlock;
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
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.Outline;

public class PluginImpl extends Plugin {

	/**
	 * Customization namespace URI.
	 */
	public static final String NS = "http://www.spitech.com/xjc/plugin/jaxbui";

	private Map<JClass, JDefinedClass> definedClasses = new HashMap<JClass, JDefinedClass>();

	private Map<JType, Class<? extends JComponent>> componentsMap = new HashMap<JType, Class<? extends JComponent>>();
	private Map<Class<? extends JComponent>, String> componentSettersMap = new HashMap<Class<? extends JComponent>, String>();

	@Override
	public String getOptionName() {
		return "Xui";
	}

	@Override
	public String getUsage() {
		return "-Xui : ";
	}

	public static class JNumberField extends JFormattedTextField {

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

	public static class JDateField extends JFormattedTextField {

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
	

	@Override
	public boolean run(Outline outline, Options options,
			ErrorHandler errorHandler) throws SAXException {

		new ViewGenerator(outline, errorHandler);
		
		JCodeModel codeModel = outline.getCodeModel();

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

		for (ClassOutline classOutline : outline.getClasses()) {
			createView(classOutline, outline, options, errorHandler);
		}
		
		return true;
	}

	private JDefinedClass createTableAdapter(ClassOutline classOutline,
			Outline outline, Options options, ErrorHandler errorHandler) {
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
			JVar _param = _constructor.param(codeModel.ref(List.class).narrow(implClass), uncapitalize(implClass
					.name()));
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
			
			JVar _row = _getValueAt.body().decl(implClass, uncapitalize(implClass.name()), 
					JExpr.cast(implClass, JExpr._this().invoke("getRow").arg(_rowIndex)));

			JSwitch _switch = _getValueAt.body()._switch(_columnIndex);
			int i = 0;
			for (Entry<String, JFieldVar> entry : implClass.fields().entrySet()) {
				_columnNames.add(JExpr.lit(entry.getKey()));
				JCase _case = _switch._case(JExpr.lit(i++));
				_case.body()._return(_row.invoke(getMethod(implClass, "(?i)(?:get|is)" + capitalize(entry.getKey()), entry.getValue().type(), new JType[0])));
			}
			_switch._default().body()._return(JExpr._null());
			
			return _class;
	}
	
	private JDefinedClass createView(ClassOutline classOutline,
			Outline outline, Options options, ErrorHandler errorHandler) {

		JCodeModel codeModel = outline.getCodeModel();
		JDefinedClass implClass = classOutline.implClass;

		if (definedClasses.containsKey(implClass)) {
			System.out.println("** View Already Generated for ["
					+ classOutline.implClass + "]");
			return definedClasses.get(implClass);
		} else {
			System.out.println("** Generate View for ["
					+ classOutline.implClass + "]");
			addPropertyChangeSupport(implClass);

			JDefinedClass _class;
			JPackage _package = implClass._package();
			String fullyqualifiedName = _package.name() + "." + "view."
					+ implClass.name() + "View";
			try {
				_class = codeModel._class(fullyqualifiedName);

			} catch (JClassAlreadyExistsException e) {
				e.printStackTrace();
				_class = codeModel._getClass(implClass.name() + "View");
			}
			definedClasses.put(implClass, _class);

//			JFieldVar _presentationModel = _class.field(JMod.PRIVATE
//					| JMod.FINAL, PresentationModel.class, "presentationModel");

			// Instance Creation
			// ****************************************************

			_class.constructor(JMod.PUBLIC);
			
//			JMethod _constructor = _class.constructor(JMod.PUBLIC);
//			JVar _param = _constructor.param(implClass, uncapitalize(implClass
//					.name()));
//			_constructor.body().assign(
//					_presentationModel,
//					JExpr._new(codeModel._ref(PresentationModel.class)).arg(
//							_param));


			// Storing and Restoring Models
			// *****************************************

			JMethod _updateModel = _class.method(JMod.PUBLIC, codeModel.VOID,
					"updateModel");
			JVar _updateModelParam1 = _updateModel.param(Object.class, "model");
			_updateModel.javadoc().add(
					"Writes view contents to the given model.");
			_updateModel.javadoc().addParam(_updateModelParam1).add(
					"the object to write this editor's value to");

			JMethod _updateView = _class.method(JMod.PUBLIC, codeModel.VOID,
					"updateView");
			JVar _updateViewParam1 = _updateView.param(Object.class, "model");
			_updateView.javadoc().add(
					"Reads view contents from the given model.");
			_updateView.javadoc().addParam(_updateViewParam1).add(
					"the object to read the values from");
			_updateView.body()._if(_updateViewParam1.eq(JExpr._null()))._then()._return();
			JVar _modelForUpdateView = _updateView.body().decl(implClass, uncapitalize(implClass.name()), JExpr.cast(implClass, _updateViewParam1));
			
			// Component Creation and Configuration
			// **********************************

			JMethod _initComponents = _class.method(JMod.PROTECTED,
					codeModel.VOID, "initComponents");
			_initComponents.javadoc().add(
					"Creates and configures the UI components.");

			// Building
			// *************************************************************

			JMethod _buildPanel = _class.method(JMod.PUBLIC, JComponent.class,
					"buildPanel");
			_buildPanel.javadoc()
					.add("Builds and returns this editor's panel.");
			_buildPanel.body().invoke(_initComponents);
			_buildPanel
					.body()
					.decl(
							codeModel._ref(FormLayout.class),
							"layout",
							JExpr
									.direct("new FormLayout(\"right:pref, 3dlu, 150dlu:grow\")"));
			JVar _builder = _buildPanel.body().decl(
					codeModel._ref(DefaultFormBuilder.class), "builder",
					JExpr.direct("new DefaultFormBuilder(layout)"));

			Iterator<Entry<String, JFieldVar>> fieldsIterator = implClass
					.fields().entrySet().iterator();
			while (fieldsIterator.hasNext()) {
				Entry<String, JFieldVar> next = fieldsIterator.next();
				// System.out.println(next.getKey() + "|->" + next.getValue());
				String propertyName = next.getKey();

				// retrieve setter method
				JFieldVar param = next.getValue();
				JType paramType = param.type();
								
				JMethod setterMethod = getMethod(implClass, "(?i)set" + capitalize(propertyName), codeModel.VOID, new JType[] { paramType });
				if (setterMethod != null) {
					addPropertyChangeSupport(setterMethod, propertyName);
				} else {
					System.err.println("setter method for property ["
							+ propertyName + "] of bean [" + classOutline.ref.name()
							+ "] not found.");
				}
				JMethod getterMethod = getMethod(implClass, "(?i)(?:get|is)" + capitalize(propertyName), paramType, new JType[0]);
				if (getterMethod == null) {
					System.err.println("setter method for property ["
							+ propertyName + "] of bean [" + classOutline.ref.name()
							+ "] not found.");
				}

				/*
				 * Build UI Class
				 */
				if (componentsMap.containsKey(paramType.boxify())) { 
					// Handle Default Java Data Type
					Class<? extends JComponent> componentClass = componentsMap
							.get(paramType.boxify());
					JType _componentRef = codeModel._ref(componentClass);
					JFieldVar _field = _class.field(JMod.PROTECTED,
							_componentRef, propertyName + "Field");
					_initComponents.body().assign(
							_field,
							JExpr._new(_componentRef));
					_updateView.body().invoke(_field, componentSettersMap.get(componentClass)).arg(JExpr.invoke(_modelForUpdateView, getterMethod));
					_buildPanel.body().invoke(_builder, "append").arg(
							propertyName).arg(_field);
				} else if (contains(outline.getClasses(), paramType)){
					// Handle Generated Type
					JDefinedClass _view;
					if (definedClasses.containsKey(paramType)) {
						//view has already been generated
						_view = definedClasses.get(paramType);
					} else {
						//view has not been generated
						_view = createView(getClassOutline(outline.getClasses(), paramType), outline, options, errorHandler);
					}
					JFieldVar _viewVar = _class.field(JMod.PROTECTED,
							_view, propertyName + "View");
					JVar _panel = _buildPanel.body().decl(codeModel._ref(JComponent.class), propertyName + "Panel", JExpr.invoke(_viewVar, "buildPanel"));
					_buildPanel.body().invoke(_builder, "append").arg(_panel)
					.arg(_builder.invoke("getColumnCount"));
					_buildPanel.body().invoke(_panel, "setBorder").arg(
							JExpr._new(codeModel._ref(TitledBorder.class)).arg(
									propertyName));
					_initComponents.body().assign(
							_viewVar,
							JExpr._new(_view));
					_updateView.body().invoke(_viewVar, "updateView").arg(JExpr.invoke(_modelForUpdateView, getterMethod));
				} else if (isEnum(outline, paramType)) { 
					// Handle Enumeration
					JFieldVar _field = _class.field(JMod.PROTECTED,
							JComboBox.class, propertyName + "Field");
					_initComponents.body().assign(
							_field,
							JExpr._new(codeModel._ref(JComboBox.class)).arg(
									((JDefinedClass) paramType)
											.staticInvoke("values")));
					_buildPanel.body().invoke(_builder, "append").arg(
							propertyName).arg(_field);
					_updateView.body().invoke(_field, "setSelectedItem").arg(JExpr.invoke(_modelForUpdateView, getterMethod));
				} else if (isCollection(paramType, codeModel.ref(Collection.class))){
					// Handle Parametrized Collection
					if (((JClass)paramType).isParameterized()) {
						if (componentsMap.containsKey(((JClass)paramType).getTypeParameters().get(0))) {
							// Collection of Default Data Type
							JFieldVar _field = _class.field(JMod.PROTECTED,
									JList.class, propertyName + "List");
							_initComponents.body().assign(
									_field,
									JExpr._new(codeModel._ref(JList.class)));
							_buildPanel.body().invoke(_builder, "append").arg(JExpr._new(codeModel._ref(JScrollPane.class)).arg(_field))
							.arg(_builder.invoke("getColumnCount"));
							_updateView.body().invoke(_field, "setModel").arg(JExpr._new(codeModel._ref(ListHolder.class)).arg(JExpr.invoke(_modelForUpdateView, getterMethod)));
						} else if (contains(outline.getClasses(), ((JClass)paramType).getTypeParameters().get(0))) {
							// Collection of Generic Type
							ClassOutline paramClassOutline = getClassOutline(outline.getClasses(), ((JClass)paramType).getTypeParameters().get(0));
							if (paramClassOutline != null) {
								JDefinedClass _tableAdapter = createTableAdapter(paramClassOutline, outline, options, errorHandler);
								JFieldVar _field = _class.field(JMod.PROTECTED,
										JTable.class, propertyName + "Table");
								_initComponents.body().assign(
										_field,
										JExpr._new(codeModel._ref(JTable.class)).arg(JExpr._new(_tableAdapter)));
								_buildPanel.body().invoke(_builder, "append").arg(JExpr._new(codeModel._ref(JScrollPane.class)).arg(_field))
								.arg(_builder.invoke("getColumnCount"));
								_updateView.body().invoke(_field, "setModel").arg(JExpr._new(_tableAdapter).arg(JExpr.invoke(_modelForUpdateView, getterMethod)));
//								 scenarioTable.addMouseListener(new MouseAdapter() {
//							        	@Override
//							        	public void mouseClicked(MouseEvent e) {
//							        		if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
//							        			JTable table = (JTable)e.getComponent();
//							        			if (table.getSelectedRowCount() == 1) {
//							        				/*
//							        				 * select scenario
//							        				 */
//							        				ScenarioTableAdapter tableModel = (ScenarioTableAdapter) table.getModel();
//							        				Scenario scenario = tableModel.getScenario(table.getSelectedRow());
//							        				
//							        				/*
//							        				 * create view with the selected scenario
//							        				 */
//							        				JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(table));
//							        				ScenarioView scenarioView = new ScenarioView();
//							        				dialog.getContentPane().add(scenarioView.buildPanel());
//							        				scenarioView.updateView(scenario);
//							        				dialog.pack();
//							        				dialog.setVisible(true);
//							        			}
//							        		}
//							        	}
//							        });
							} else {
								System.out.println("unhandle type [" + paramType
										+ "] for bean [" + classOutline.ref.name() + "]");
							}
						} else {
							System.out.println("unhandle type [" + paramType
									+ "] for bean [" + classOutline.ref.name() + "]");
						}
					}
				} else {
					System.out.println("unhandle type [" + paramType
							+ "] for bean [" + classOutline.ref.name() + "]");
					// System.out.println("definedClasses:" + definedClasses);
				}
			}
			_buildPanel.body()._return(_builder.invoke("getPanel"));
			return _class;
		}
	}
	
    private boolean isCollection(JType paramType, JClass collectionClass) {
		return collectionClass.isAssignableFrom(paramType.boxify());
	}

	private ClassOutline getClassOutline(Collection<? extends ClassOutline> classes, JType type) {
    	for (ClassOutline classOutline : classes) {
    		if (classOutline.ref.equals(type))
    			return classOutline;
    	}
    	System.out.println("classOutline not found for [" + type + "]");
		return null;
	}

	private boolean contains(Collection<? extends ClassOutline> classes, JType type) {
    	for (ClassOutline classOutline : classes) {
			if (classOutline.ref.equals(type))
				return true;
		}
		return false;
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

	private boolean isEnum(Outline outline, JType type) {
		Iterator<EnumOutline> iterator = outline.getEnums().iterator();
		while (iterator.hasNext()) {
			EnumOutline enumOutline = (EnumOutline) iterator.next();
			if (type.equals(enumOutline.clazz)) {
				return true;
			}
		}
		return false;
	}

	private void addPropertyChangeSupport(JDefinedClass clazz) {
		clazz._extends(Model.class);
	}

	private void addPropertyChangeSupport(JMethod method, String propertyName) {
		/*
		 * Add Java Bound Properties Feature
		 */
		JBlock body = method.body();
		int oldPos = body.pos();
		body.pos(oldPos - 1); // insert before oldPos
		JType paramType = method.listParamTypes()[0];
		JVar param = method.listParams()[0];
		JVar oldValueVar = body.decl(paramType, "oldValue", param);
		body.pos(oldPos + 1); // insert after
		body.invoke("firePropertyChange").arg(propertyName).arg(oldValueVar)
				.arg(param);
	}

	private String capitalize(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	private String uncapitalize(String name) {
		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}

	public static void main(String[] args) {
		System.out.println("setUsPrice".matches("(?i)(?:set|is)USPrice"));
	}
	
}
