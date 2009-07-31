package com.sun.tools.xjc.addon;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.util.CodeModelClassFactory;

public class ViewGenerator {

	private final Set<JType> simpleTypes;
	
	/** all {@link ClassOutline}s keyed by their {@link ClassOutline#target}. */
	private final Map<CClassInfo, SwingViewRenderer> viewRenderers = new HashMap<CClassInfo, SwingViewRenderer>();

	private final Outline outline;

	/** the model object which we are processing. */
	private final Model model;

	private final JCodeModel codeModel;

	private final ErrorHandler errorHandler;

	/** Simplifies class/interface creation and collision detection. */
	private final CodeModelClassFactory classFactory;

	public ViewGenerator(Outline outline, ErrorHandler errorReceiver) {
		super();
		this.outline = outline;
		this.model = outline.getModel();
		this.codeModel = outline.getCodeModel();
		this.errorHandler = errorReceiver;
		this.classFactory = outline.getClassFactory();

		simpleTypes = new HashSet<JType>();
		simpleTypes.add(codeModel._ref(String.class));
		simpleTypes.add(codeModel._ref(BigInteger.class));
		simpleTypes.add(codeModel._ref(int.class));
		simpleTypes.add(codeModel._ref(Integer.class));
		simpleTypes.add(codeModel._ref(long.class));
		simpleTypes.add(codeModel._ref(Long.class));
		simpleTypes.add(codeModel._ref(short.class));
		simpleTypes.add(codeModel._ref(Short.class));
		simpleTypes.add(codeModel._ref(BigDecimal.class));
		simpleTypes.add(codeModel._ref(float.class));
		simpleTypes.add(codeModel._ref(Float.class));
		simpleTypes.add(codeModel._ref(double.class));
		simpleTypes.add(codeModel._ref(Double.class));
		simpleTypes.add(codeModel._ref(boolean.class));
		simpleTypes.add(codeModel._ref(Boolean.class));
		simpleTypes.add(codeModel._ref(byte.class));
		simpleTypes.add(codeModel._ref(Byte.class));
		simpleTypes.add(codeModel._ref(XMLGregorianCalendar.class));
		simpleTypes.add(codeModel._ref(Duration.class));
		
		// create the class definitions for all the beans first.
		// this should also fill in PackageContext#getClasses
		for (CClassInfo bean : model.beans().values())
			getViewRenderer(bean);

		// fill in implementation classes
		for (ClassOutline co : outline.getClasses())
			generateViewBody(co);
	}

	public SwingViewRenderer getViewRenderer(CClassInfo bean) {
		System.out.println("ViewGenerator.getViewRenderer(" + bean + ")");
		SwingViewRenderer r = viewRenderers.get(bean);
		if (r == null)
			viewRenderers.put(bean, r = generateViewRenderer(bean));
		return r;
	}

	/**
	 * Generates the minimum {@link JDefinedClass} skeleton without filling in
	 * its body.
	 */
	private SwingViewRenderer generateViewRenderer(CClassInfo bean) {
		return new SwingViewRenderer(this, outline, bean);
	}

	/**
	 * Generates the body of a class.
	 * 
	 */
	private void generateViewBody(ClassOutline cc) {
		CClassInfo target = cc.target;

		for (CPropertyInfo prop : target.getProperties()) {
			generateFieldDecl(cc, prop);
		}

		if (target.declaresAttributeWildcard()) {
//			generateAttributeWildcard(cc);
		}

	}

	private void generateFieldDecl(ClassOutline cc, CPropertyInfo prop) {
		SwingViewRenderer viewRenderer = getViewRenderer(cc.target);
		if (isSimpleType(prop.baseType)) {
			viewRenderer.handleSimpleType(cc, prop);
		} else if (isGeneratedType(prop.baseType)) {
			viewRenderer.handleComplexType(cc, prop);
		}
	}
	
	private boolean isSimpleType(JType type) {
		return type.isPrimitive() || simpleTypes.contains(type);
	}
	
	private boolean isGeneratedType(JType type) {
    	for (ClassOutline classOutline : outline.getClasses()) {
			if (classOutline.ref.equals(type))
				return true;
		}
		return false;
	}
}
