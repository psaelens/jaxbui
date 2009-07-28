package com.spitech.tools.xjc.addon.ui_generator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.Outline;

public class SwingGenerator {

	Outline outline;
	Options options;
	ErrorHandler errorHandler;
	
	JCodeModel codeModel;
	
	JDefinedClass _class;
	
	public SwingGenerator(Outline outline, Options options, ErrorHandler errorHandler)
	throws SAXException {
		this.outline = outline;
		this.options = options;
		this.errorHandler = errorHandler;
		
		this.codeModel = outline.getCodeModel();
	}
	
	public void createClass(){}
	
}
