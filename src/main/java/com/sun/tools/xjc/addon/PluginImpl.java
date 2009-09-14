package com.sun.tools.xjc.addon;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.jgoodies.binding.beans.Model;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.Outline;

public class PluginImpl extends Plugin {

	/**
	 * Customization namespace URI.
	 */
	public static final String NS = "http://www.spitech.com/xjc/plugin/jaxbui";

	public PluginImpl() {
		BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("[%-5p] %M(%c{1}:%L) %m%n")));
	}
	
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

		new ViewGenerator(outline, errorHandler);
		
		return true;
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
	
}
