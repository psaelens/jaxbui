package com.sun.tools.xjc.addon;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.beans.Model;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

public class PluginImpl extends Plugin {

	/**
	 * Customization namespace URI.
	 */
	public static final String NS = "http://www.spitech.com/xjc/plugin/jaxbui";

	private Map<JClass, JDefinedClass> definedClasses = new HashMap<JClass, JDefinedClass>();
	
	@Override
	public String getOptionName() {
		return "Xui";
	}

	@Override
	public String getUsage() {
		return "-Xui : ";
	}


	@Override
	public boolean run(Outline outline, Options options,
			ErrorHandler errorHandler) throws SAXException {

		JCodeModel codeModel = outline.getCodeModel();

		for (ClassOutline co : outline.getClasses()) {
			co.implClass._extends(Model.class);

			JDefinedClass _class;
			JPackage _package = co.implClass._package();
			String fullyqualifiedName = _package.name() + "." + "view."
					+ co.implClass.name() + "View";
			try {
				_class = codeModel._class(fullyqualifiedName);

			} catch (JClassAlreadyExistsException e) {
				e.printStackTrace();
				_class = codeModel._getClass(co.implClass.name() + "View");
			}
			definedClasses.put(co.implClass, _class);

			JFieldVar _presentationModel = _class.field(JMod.PRIVATE
					& JMod.FINAL, PresentationModel.class, "presentationModel");

			// Instance Creation
			// ****************************************************

			JMethod _constructor = _class.constructor(JMod.PUBLIC);
			JVar _param = _constructor.param(co.implClass,
					uncapitalize(co.implClass.name()));
			_constructor.body().assign(
					_presentationModel,
					JExpr._new(codeModel._ref(PresentationModel.class)).arg(
							_param));

			JMethod _constructor2 = _class.constructor(JMod.PUBLIC);

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

			Iterator<Entry<String, JFieldVar>> fieldsIterator = co.implClass
					.fields().entrySet().iterator();
			while (fieldsIterator.hasNext()) {
				Entry<String, JFieldVar> next = fieldsIterator.next();
				// System.out.println(next.getKey() + "|->" + next.getValue());
				String propertyName = next.getKey();

				// retrieve setter method
				JType paramType = next.getValue().type();
				JMethod setterMethod = co.implClass.getMethod("set"
						+ capitalize(propertyName), new JType[] { paramType });
				if (setterMethod != null) {
					addPropertyChangeSupport(setterMethod, propertyName);
				} else {
					System.err.println("setter method for property ["
							+ propertyName + "] of bean [" + co.ref.name()
							+ "] not found.");
				}

				/*
				 * Build UI Class
				 */
				if (definedClasses.containsKey(paramType)) {
					JFieldVar _panel = _class.field(JMod.PROTECTED,
							JComponent.class, propertyName + "Panel");
					_buildPanel.body().invoke(_builder, "append").arg(_panel)
							.arg(_builder.invoke("getColumnCount"));
					_initComponents.body().assign(
							_panel,
							JExpr._new(definedClasses.get(paramType)).invoke(
									"buildPanel"));
					_initComponents.body().invoke(_panel, "setBorder").arg(
							JExpr._new(codeModel._ref(TitledBorder.class)).arg(
									propertyName));
				} else {
					JFieldVar _field = _class.field(JMod.PROTECTED,
							JComponent.class, propertyName + "Field");
					_initComponents.body().assign(_field,
							JExpr._new(codeModel._ref(JTextField.class)));
					_buildPanel.body().invoke(_builder, "append").arg(
							propertyName).arg(_field);
				}
			}

			// for (JMethod method : co.implClass.methods()) {
			// if (codeModel.VOID == method.type()
			// && method.listParams().length == 1
			// && method.name().startsWith("set")) {
			//					
			//					
			// String propertyName = extractPropertyName(method);
			// JType paramType = method.listParamTypes()[0];
			// addPropertyChangeSupport(method, propertyName);
			//					
			// /*
			// * Build UI Class
			// */
			// if (definedClasses.containsKey(paramType)) {
			// JFieldVar _panel = _class.field(JMod.PROTECTED, JComponent.class,
			// propertyName + "Panel");
			// _buildPanel.body().invoke(_builder,
			// "append").arg(_panel).arg(_builder.invoke("getColumnCount"));
			// _initComponents.body().assign(_panel,
			// JExpr._new(definedClasses.get(paramType)).invoke("buildPanel"));
			// _initComponents.body().invoke(_panel,
			// "setBorder").arg(JExpr._new(
			// codeModel._ref(TitledBorder.class)).arg(propertyName));
			// } else {
			// JFieldVar _field = _class.field(JMod.PROTECTED, JComponent.class,
			// propertyName + "Field");
			// _initComponents.body().assign(_field,
			// JExpr._new(codeModel._ref(JTextField.class)));
			// _buildPanel.body().invoke(_builder,
			// "append").arg(propertyName).arg(_field);
			// }
			//					
			// }
			// }
			_buildPanel.body()._return(_builder.invoke("getPanel"));
		}
		try {
			codeModel.build(new File("."));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
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

	private String extractPropertyName(JMethod method) {
		String methodName = method.name();
		if (methodName.length() > 4) {
			return uncapitalize(methodName.substring(3));
		}
		System.err.println("can't extract property name from method name ["
				+ methodName + "] "
				+ "- method name should matches regular expression 'set.*'");
		return methodName;
	}

	private String capitalize(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	private String uncapitalize(String name) {
		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}

}
