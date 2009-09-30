package com.spitech.jaxbui;

import javax.swing.JLabel;

import org.jdesktop.application.SingleFrameApplication;

/**
 * Unit test for simple App.
 */
public class AppTest extends SingleFrameApplication {
    
	// ---------------------------------------------------------------- Application Implementation
	@Override
	protected void startup() {
		show(new JLabel());
	}
	
    public static void main( String[] args )
    {
    	launch(AppTest.class, args);
    }
    
    
}
