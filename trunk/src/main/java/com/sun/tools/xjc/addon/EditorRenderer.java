package com.sun.tools.xjc.addon;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.jdesktop.application.Application;

import com.jgoodies.binding.list.IndirectListModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.spitech.uiskeleton.view.editor.AbstractEditor;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.model.CAttributePropertyInfo;
import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CNonElement;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CPropertyVisitor;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.model.CTypeInfo;
import com.sun.tools.xjc.model.CValuePropertyInfo;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.model.core.PropertyKind;

public class EditorRenderer implements CPropertyVisitor<String> {

	private static final Logger LOG = Logger.getLogger(EditorRenderer.class);
	
	private final ViewGenerator viewGenerator;

	private final Outline outline;

	private final ClassOutline classOutline;

	private final JCodeModel codeModel;

	private final ComponentFactory componentFactory;
	
	private JDefinedClass _class;
	private JMethod _initComponents;
	private JMethod _updateView;
	private JMethod _updateModel;
	private JMethod _buildPanel;
	private JVar _builder;
	private JVar _modelForUpdateView;
	private JVar _modelForUpdateModel;

	EditorRenderer(ViewGenerator viewGenerator, Outline outline, CClassInfo bean) {
		this.viewGenerator = viewGenerator;
		this.outline = outline;
		this.classOutline = outline.getClazz(bean);
		this.codeModel = outline.getCodeModel();
		this.componentFactory = new ComponentFactory(codeModel);
		
		try {
			JPackage _package = classOutline._package()._package().subPackage("view").subPackage("editor");
			_class = _package._class( bean.shortName + "Editor");
		} catch (JClassAlreadyExistsException e) {
			// it's OK for this to collide.
			_class = e.getExistingClass();
		}

		_class._extends(AbstractEditor.class);

		viewGenerator.registerEditor(classOutline.implRef, _class);
	}

	void generateBody() {
		JMethod _constructor = _class.constructor(JMod.PUBLIC);
		final JVar _app = _constructor.param(Application.class, "app");
		_constructor.body().invoke("super").arg(_app).arg("");

		_updateModel = createMethod(JMod.PUBLIC, codeModel.VOID, "updateModel");
		JVar _updateModelParam1 = _updateModel.param(Object.class, "model");
		_updateModel.javadoc().add("Writes view contents to the given model.");
		_updateModel.javadoc().addParam(_updateModelParam1).add(
				"the object to write this editor's value to");
		_updateModel.body()._if(_updateModelParam1.eq(JExpr._null()))._then()
		._return();
		_modelForUpdateModel = _updateModel.body().decl(classOutline.implClass,
				StringUtils.uncapitalize(classOutline.target.shortName),
				JExpr.cast(classOutline.implClass, _updateModelParam1));

		_updateView = createMethod(JMod.PUBLIC, codeModel.VOID, "updateView");
		JVar _updateViewParam1 = _updateView.param(Object.class, "model");
		_updateView.javadoc().add("Reads view contents from the given model.");
		_updateView.javadoc().addParam(_updateViewParam1).add(
				"the object to read the values from");
		_updateView.body().invoke("setTitleSuffix").arg(JExpr.lit(classOutline.target.shortName));
		_updateView.body()._if(_updateViewParam1.eq(JExpr._null()))._then()
				._return();
		_modelForUpdateView = _updateView.body().decl(classOutline.implClass,
				StringUtils.uncapitalize(classOutline.target.shortName),
				JExpr.cast(classOutline.implClass, _updateViewParam1));

		_initComponents = createMethod(JMod.PROTECTED, codeModel.VOID, "initComponents");
		_initComponents.javadoc().add("Creates and configures the UI components.");

		_buildPanel = createMethod(JMod.PROTECTED, JComponent.class, "buildPanel");
		_buildPanel.javadoc().add("Builds and returns this editor's panel.");
		_buildPanel.body().invoke(_initComponents);
		JType formLayoutType = codeModel._ref(FormLayout.class);
		JType defaultFormBuilderType = codeModel._ref(DefaultFormBuilder.class);
		JVar _layout = _buildPanel
				.body()
				.decl(
						formLayoutType,
						"layout",
						JExpr._new(formLayoutType).arg("right:pref, 3dlu, 150dlu:grow"));
		
		_builder = _buildPanel.body().decl(
				defaultFormBuilderType, "builder",
				JExpr._new(defaultFormBuilderType).arg(_layout));
		_buildPanel.body().invoke(_builder, "setDefaultDialogBorder");
		
		for (CPropertyInfo prop : classOutline.target.getProperties()) {
			prop.accept(this);
//			if (prop.isCollection()) {
//				handleCollection(prop);
//			} else if (isEnum(prop)) {
//				handleEnumeration(prop);
//			} else if (isAttribute(prop)) {
//				handleSimpleType(prop);
//			} else if (prop.kind() == PropertyKind.VALUE) {
//				handleSimpleType(prop);
//			} else if (prop.kind() == PropertyKind.ELEMENT) {
//				// handleComplexType(prop);
//			} else if (prop.kind() == PropertyKind.REFERENCE) {
//				// handleComplexType(prop);
//			} else if (prop.kind() == PropertyKind.MAP) {
//				// LOG.debug("TODO : Handle Map");
//			}
		}
		_buildPanel.body()._return(_builder.invoke("getPanel"));
	}

	@Override
	public String onAttribute(CAttributePropertyInfo p) {
		handleSimpleType(p);
		return null;
	}

	@Override
	public String onElement(CElementPropertyInfo p) {
		LOG.debug(p.displayName());
		for (CNonElement typeInfo : p.ref()) {
			if (CBuiltinLeafInfo.LEAVES.containsValue(typeInfo)) {
				handleSimpleType(p);
			} else {
				// don't manage complex type (= generated type)
				// complex type is managed by a tree view
			}
		}
		return null;
	}

	@Override
	public String onReference(CReferencePropertyInfo p) {
		return null;
	}

	@Override
	public String onValue(CValuePropertyInfo p) {
		handleSimpleType(p);
		return null;
	}
	
	private boolean isEnum(CPropertyInfo prop) {
		for (CTypeInfo typeInfo : prop.ref()) {
			if (isEnum(outline, typeInfo.toType(outline, Aspect.EXPOSED))) {
				return true;
			}
		}
		return false;
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

	private void handleSimpleType(CPropertyInfo prop) {
		// int i = 0;
		if (prop.ref().isEmpty()) {
			LOG.warn("unhandled property [" + prop.displayName()
					+ "] - no type info");
		} else if (prop.ref().size() > 1) {
			LOG.warn("unhandled property [" + prop.displayName()
					+ "] - more than one type info");
		} else {
			String name = prop.getName(false);
			for (CTypeInfo typeInfo : prop.ref()) {
				// String name = prop.getName(false) + "$" + (++i);
				
				
				JType type = typeInfo.toType(outline, Aspect.EXPOSED);
//				JClass jclass = (JClass)type;
//				jclass.
				Class<? extends JComponent> componentClass = componentFactory.getComponent(type);
				if (componentClass == null) {
					LOG.warn("component not found for type [" + type + "]");
				} else {
					JType _componentRef = codeModel._ref(componentClass);
					JFieldVar _field = _class.field(JMod.PROTECTED,
							_componentRef, name + "Field");
					_initComponents.body().assign(_field,
							JExpr._new(_componentRef));
					if (prop.isCollection()) {
						JClass listOfTypeClass = codeModel.ref(List.class).narrow(type.boxify());
						JMethod getterMethod = getter(name, listOfTypeClass);
						if (getterMethod != null) {
							_updateView.body().invoke(_field, componentFactory.getSetter(componentClass))
							.arg(codeModel.ref(StringUtils.class).staticInvoke("join").arg(
									JExpr.invoke(_modelForUpdateView,
											getterMethod)).arg(","));
							
							JInvocation getTypeInvocation = JExpr.invoke(_modelForUpdateModel, getterMethod);
							_updateModel.body().add(getTypeInvocation.invoke("clear"));
							JInvocation splitTextFieldInvocation = _field.invoke(componentFactory.getGetter(componentClass)).invoke("split").arg(",");
							JInvocation arraysAsListInvocation = codeModel.ref(Arrays.class).staticInvoke("asList").arg(splitTextFieldInvocation);
							_updateModel.body().add(getTypeInvocation.invoke("addAll").arg(arraysAsListInvocation));
						}
						
					} else {
						JMethod getterMethod = getter(name, type);
						JMethod setterMethod = setter(name, type);
						if (getterMethod != null) {
							if (type.unboxify() == codeModel.BOOLEAN) {
								JVar _selected = _updateView.body().decl(type, "selected", JExpr.invoke(_modelForUpdateView,
										getterMethod));
								_updateView.body()._if(_selected.eq(JExpr._null()))._then().assign(_selected, JExpr.FALSE);
								_updateView.body().invoke(_field, componentFactory.getSetter(componentClass))
								.arg(_selected);
							} else {
								_updateView.body().invoke(_field, componentFactory.getSetter(componentClass))
								.arg(
										JExpr.invoke(_modelForUpdateView,
												getterMethod));
							}
							
						}
						if (setterMethod != null) {
							JExpression arg;
//						if (type.unboxify() != type && type.unboxify() != codeModel.BOOLEAN) {
////							LOG.debug("unboxify:" + type.unboxify().binaryName());
//							arg = JExpr.invoke(JExpr.invoke(_field,
//									componentFactory.getGetter(componentClass)), type.unboxify().binaryName() + "Value");
//						} else {
							arg = JExpr.cast(type, 
									JExpr.invoke(_field,
											componentFactory.getGetter(componentClass))); 
//						}
							_updateModel.body().invoke(_modelForUpdateModel,
									setterMethod).arg(arg);
						}
					}

					_buildPanel.body().invoke(_builder, "append").arg(name)
							.arg(_field);
				}
			}
		}
	}

	private JMethod getter(String name, JType type) {
		return getMethod(classOutline.implClass,
				"(?i)(?:get|is)" + name, type, new JType[0]);
	}

	private JMethod setter(String name, JType type) {
		return getMethod(classOutline.implClass,
				"(?i)set" + name, codeModel.VOID,
				new JType[] { type });
	}

	private void debug(Object o) {
		LOG.debug(toString(o));
	}

	private String toString(Object o) {
		return ToStringBuilder.reflectionToString(o,
				ToStringStyle.MULTI_LINE_STYLE);
	}

	private void handleCollection(CPropertyInfo prop) {
		if (prop.ref().isEmpty()) {
			LOG.warn("unhandled property [" + prop.displayName()
					+ "] - no type info");
		} else if (prop.ref().size() > 1) {
			LOG.warn("unhandled property [" + prop.displayName()
					+ "] - more than one type info");
		} else {
			String propertyName = prop.getName(false);
			for (CTypeInfo typeInfo : prop.ref()) {
				JType paramType = typeInfo.toType(outline, Aspect.EXPOSED);
				JType type = paramType;
				if (componentFactory.contains(type)) {
					// Collection of Default Data Type
					JFieldVar _field = _class.field(JMod.PROTECTED,
							JList.class, propertyName + "List");
					_initComponents.body().assign(_field,
							JExpr._new(codeModel._ref(JList.class)));
					_buildPanel.body().invoke(_builder, "append").arg(
							JExpr._new(codeModel._ref(JScrollPane.class)).arg(
									_field)).arg(
							_builder.invoke("getColumnCount"));
					JClass listT = codeModel.ref(List.class).narrow(
							type.boxify());
					JMethod getterMethod = getMethod(classOutline.implClass,
							"(?i)(?:get|is)" + propertyName, listT,
							new JType[0]);
					if (getterMethod != null) {
						_updateView.body().invoke(_field, "setModel").arg(
								JExpr._new(codeModel.ref(IndirectListModel.class).narrow(
										type.boxify()))
										.arg(
												JExpr.invoke(
														_modelForUpdateView,
														getterMethod)));
					} else {
						System.err
								.println("[handleCollection] getter method for property ["
										+ propertyName
										+ "] of bean ["
										+ classOutline.ref.name()
										+ "] not found.");
					}
				}
			}
		}
	}

	public void handleEnumeration(CPropertyInfo prop) {
		if (prop.ref().isEmpty()) {
			LOG.warn("unhandled property [" + prop.displayName()
					+ "] - no type info");
		} else if (prop.ref().size() > 1) {
			LOG.warn("unhandled property [" + prop.displayName()
					+ "] - more than one type info");
		} else {
			String propertyName = prop.getName(false);
			for (CTypeInfo typeInfo : prop.ref()) {
				JType type = typeInfo.toType(outline, Aspect.EXPOSED);
				JFieldVar _field = _class.field(JMod.PROTECTED,
						JComboBox.class, propertyName + "Field");
				_initComponents.body().assign(
						_field,
						JExpr._new(codeModel._ref(JComboBox.class)).arg(
								((JDefinedClass) type).staticInvoke("values")));
				_buildPanel.body().invoke(_builder, "append").arg(propertyName)
						.arg(_field);
				JMethod getterMethod = getter(propertyName, type);
				if (getterMethod != null) {
					_updateView.body().invoke(_field, "setSelectedItem").arg(
							JExpr.invoke(_modelForUpdateView, getterMethod));
				} else {
					LOG.warn("getter method for property ["
							+ propertyName + "] of bean ["
							+ classOutline.ref.name() + "] not found.");
				}
			}
		}
	}
	
	JMethod createMethod(int mods, JType returnType, String name) {
		return _class.method(mods, returnType, name);
	}
	
	JMethod createMethod(int mods, Class<?> returnType, String name) {
		return _class.method(mods, returnType, name);
	}

	/**
	 * Looks for a method that has the specified method signature and return it.
	 * 
	 * @return null if not found.
	 */
	public static JMethod getMethod(JDefinedClass definedClass, String regex,
			JType returnType, JType[] argTypes) {
		for (JMethod m : definedClass.methods()) {
			if (!m.name().matches(regex))
				continue;

			if ((m.type().equals(returnType) || (returnType != null && m.type()
					.equals(returnType.unboxify())))
					&& m.hasSignature(argTypes))
				return m;
		}
		LOG.warn("method not found definedClass="
				+ (definedClass != null ? definedClass.name() : null)
				+ " regex=" + regex + " returnType="
				+ (returnType != null ? returnType.name() : null)
				+ " argTypes="
				+ (argTypes != null ? Arrays.asList(argTypes) : null));
		return null;
	}

}
