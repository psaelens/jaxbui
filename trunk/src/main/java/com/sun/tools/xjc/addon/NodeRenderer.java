package com.sun.tools.xjc.addon;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.jdesktop.application.Action;

import com.spitech.uiskeleton.model.node.AbstractTreeNode;
import com.spitech.uiskeleton.model.node.NavigationNode;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
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
import com.sun.tools.xjc.model.CValuePropertyInfo;
import com.sun.tools.xjc.model.CClassInfoParent.Visitor;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

class NodeRenderer implements CPropertyVisitor<String> {
	
	private static final Logger LOG = Logger.getLogger(NodeRenderer.class);

//	private Map<JType, Class<? extends JComponent>> componentsMap = new HashMap<JType, Class<? extends JComponent>>();
//	private Map<Class<? extends JComponent>, String> componentSettersMap = new HashMap<Class<? extends JComponent>, String>();
//	private Map<Class<? extends JComponent>, String> componentGettersMap = new HashMap<Class<? extends JComponent>, String>();

	private final JClass DEFAULT_TREE_MODEL;
	private final JClass NAVIGATION_NODE;
	
	private final ViewGenerator viewGenerator;

	private final Outline outline;

	private final ClassOutline classOutline;

	private final JCodeModel codeModel;

	private JDefinedClass _class;
	private JMethod _constructor;
	private JMethod _getName;
	private JMethod _getIcon;
	private JMethod _getUserObject;
	private JMethod _remove;
	
	private JVar _associatedModel;
	private JVar _modelForRemove;

	
	NodeRenderer(ViewGenerator viewGenerator, Outline outline, CClassInfo bean) {
		this.viewGenerator = viewGenerator;
		this.outline = outline;
		this.classOutline = outline.getClazz(bean);
		this.codeModel = outline.getCodeModel();

		DEFAULT_TREE_MODEL = codeModel.ref(DefaultTreeModel.class);
		NAVIGATION_NODE = codeModel.ref(NavigationNode.class);
		
		JDefinedClass implClass = classOutline.implClass;

		JPackage _package = implClass._package();
		String fullyqualifiedName = _package.name() + ".model." + "node."
				+ implClass.name() + "Node";
		try {
			_class = codeModel._class(fullyqualifiedName);
		} catch (JClassAlreadyExistsException e) {
			// ok, just retrieve existing class
			_class = e.getExistingClass();
		}

		_class._extends(AbstractTreeNode.class);
	}

	void generateBody() {
		LOG.debug("shortName:" + classOutline.target.shortName);
		
		JDefinedClass implClass = classOutline.implClass;

		_constructor = _class.constructor(JMod.PUBLIC);
		JVar _parent = _constructor.param(NavigationNode.class, "parent");
		_associatedModel = _constructor.param(implClass, StringUtils
				.uncapitalize(classOutline.target.shortName));
		_constructor.body().invoke("super").arg(_parent).arg(_associatedModel);

		_getName = _class.method(JMod.PUBLIC, String.class, "getName");
		_getName.body()._return(JExpr.lit(classOutline.target.shortName));

		_getIcon = _class.method(JMod.PUBLIC, Icon.class, "getIcon");
		_getIcon.body()._return(JExpr._null());

		_getUserObject = _class.method(JMod.PUBLIC, classOutline.implRef, "get"
				+ classOutline.target.shortName);
		_getUserObject.body()._return(
				JExpr.cast(classOutline.implRef, JExpr.invoke("getModel")));

		_remove = _class.method(JMod.PUBLIC, codeModel.VOID, "childRemoved");
		_remove.annotate(Override.class);
		JVar _node = _remove.param(AbstractTreeNode.class, "node");
		
		JVar _navigationNode = _remove.body().decl(
				NAVIGATION_NODE, "navigationNode",
				JExpr.cast(NAVIGATION_NODE, _node));
		_modelForRemove = _remove.body().decl(codeModel.ref(Object.class),
				"model", _navigationNode.invoke("getModel"));


		List<CPropertyInfo> properties = classOutline.target.getProperties();
		classOutline.target.accept(new Visitor<String>() {
			public String onBean(CClassInfo info) {
				LOG.debug("fullName:" + info.fullName());
				return null;
			};
			
			public String onElement(com.sun.tools.xjc.model.CElementInfo info) {
				LOG.debug("fullName:" + info.fullName());
				return null;
			};
			
			public String onPackage(JPackage jpackage) {
				LOG.debug("package:" + jpackage);
				return null;
			};
		});
		for (CPropertyInfo propertyInfo : properties) {
			propertyInfo.accept(this);
		}
	}

	@Override
	public String onAttribute(CAttributePropertyInfo p) {
		LOG.debug(p.displayName());
		return null;
	}

	@Override
	public String onElement(CElementPropertyInfo p) {
		LOG.debug(p.displayName());
		// Expected : only one element in List of TypeInfos that this property references
		for (CNonElement typeInfo : p.ref()) {
			if (CBuiltinLeafInfo.LEAVES.containsValue(typeInfo)) {
				// built-in types are not represented by a Node
				continue;
			}
			JType type = typeInfo.toType(outline, Aspect.EXPOSED);
			JMethod _add = _class.method(JMod.PUBLIC, codeModel.VOID, "add"
					+ p.getName(true));
			JVar _event = _add.param(ActionEvent.class, "event");
			JAnnotationUse annotate = _add.annotate(Action.class);

			JVar _treeModel = _add.body().decl(
					DEFAULT_TREE_MODEL,
					"treeModel",
					JExpr.cast(DEFAULT_TREE_MODEL, JExpr.invoke(_event,
					"getSource")));
			if (p.isCollection()) {
				JInvocation listExpr = JExpr.invoke(JExpr
						.invoke(_getUserObject), getter(p.getName(true),
						codeModel.ref(List.class).narrow((JClass) type)));
				// add body
				JVar _toAdd = _add.body().decl(type, p.getName(false),
						JExpr._new(type));
				_add.body().invoke(listExpr, "add").arg(_toAdd);
				NodeRenderer childNodeRenderer = viewGenerator
						.getNodeRenderer(outline.getModel().beans().get(
								typeInfo));
				if (childNodeRenderer == null) {
					System.err
							.println("child node renderer not found for property ["
									+ p.displayName() + "]");
				} else {
					_add.body().invoke("add").arg(
							JExpr._new(childNodeRenderer._class).arg(
									JExpr._this()).arg(_toAdd));
				}

				// remove body
				JBlock _then = _remove.body()._if(
						_modelForRemove._instanceof(type))._then();
				_then.invoke(listExpr, "remove").arg(_modelForRemove);
				_add.body().invoke(_treeModel, "nodeStructureChanged").arg(
						JExpr._this());
			} else {
				// enabled feature support
				String enabledPropertyValue = p.getName(false) + "Enabled";
				annotate.param("enabledProperty", enabledPropertyValue);
				
				JFieldVar _enabledProperty = _class.field(JMod.PRIVATE,
						codeModel.BOOLEAN, enabledPropertyValue);
				_constructor.body().assign(_enabledProperty, JExpr._null().eq(JExpr.invoke(_associatedModel, getter(p.getName(true), type))));
				JMethod _get = _class.method(JMod.PUBLIC, codeModel.BOOLEAN,
						"is" + StringUtils.capitalize(enabledPropertyValue));
				_get.body()._return(_enabledProperty);
				JMethod _set = _class.method(JMod.PUBLIC, codeModel.VOID, "set"
						+ StringUtils.capitalize(enabledPropertyValue));
				JVar _newValue = _set.param(codeModel.BOOLEAN,
						enabledPropertyValue);
				JVar _oldValue = _set.body().decl(codeModel.BOOLEAN, "oldValue",
						JExpr.invoke(_get));
				_set.body().assign(JExpr._this().ref(_enabledProperty), _newValue);
				_set.body().invoke("firePropertyChange").arg(
						enabledPropertyValue).arg(_oldValue).arg(_enabledProperty);
				// add body
				JVar _toAdd = _add.body().decl(type, p.getName(false),
						JExpr._new(type));
				_add.body().invoke(JExpr.invoke(_getUserObject),
						setter(p.getName(true), type)).arg(_toAdd);
				NodeRenderer childNodeRenderer = viewGenerator
						.getNodeRenderer(outline.getModel().beans().get(
								typeInfo));
				if (childNodeRenderer == null) {
					System.err
							.println("child node renderer not found for property ["
									+ p.displayName() + "]");
				} else {
					_add.body().invoke("add").arg(
							JExpr._new(childNodeRenderer._class).arg(
									JExpr._this()).arg(_toAdd));
				}
				_add.body().invoke(_set).arg(JExpr.FALSE);
				_add.body().invoke(_treeModel, "nodeStructureChanged").arg(
						JExpr._this());
				// remove body
				JBlock _then = _remove.body()._if(
						_modelForRemove._instanceof(type))._then();
				_then.invoke(JExpr.invoke(_getUserObject),
						setter(p.getName(true), type)).arg(JExpr._null());
				_then.invoke(_set).arg(JExpr.TRUE);
			}
		}
		return null;
	}

	@Override
	public String onReference(CReferencePropertyInfo p) {
		LOG.debug(p.displayName());
		return null;
	}

	@Override
	public String onValue(CValuePropertyInfo p) {
		LOG.debug(p.displayName());
		return null;
	}

	private JMethod getter(String name, JType type) {
		return getMethod(classOutline.implClass, "(?i)(?:get|is)" + name, type,
				new JType[0]);
	}

	private JMethod setter(String name, JType type) {
		return getMethod(classOutline.implClass, "(?i)set" + name,
				codeModel.VOID, new JType[] { type });
	}

	/**
	 * Looks for a method that has the specified method signature and return it.
	 * 
	 * @return null if not found.
	 */
	public JMethod getMethod(JDefinedClass definedClass, String regex,
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

	String toString(Object o) {
		return ToStringBuilder.reflectionToString(o,
				ToStringStyle.MULTI_LINE_STYLE);
	}
}
