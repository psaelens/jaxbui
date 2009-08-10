package com.spitech.jaxbui;

import generated.project.view.ProjectView;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class ProjectApp {

	/**
	 * @param args
	 */
    public static void main(String[] args) {
    	final ProjectView view = new ProjectView();
    	final JFrame frame = new JFrame(".:Project Editor");
    	
		JMenuBar menubar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(new AbstractAction("open") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(".");
				int returnVal = fc.showOpenDialog(frame);

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            //This is where a real application would open the file.
		            System.out.println("Opening: " + file.getName() + ".");
					try {
						JAXBContext context = JAXBContext.newInstance("generated.project");
						Unmarshaller u = context.createUnmarshaller();
						Object o =  u.unmarshal(file);
						view.updateView(o);
						
			            Marshaller m = context.createMarshaller();
			            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			            m.marshal(o, System.out);
					} catch (JAXBException e1) {
						e1.printStackTrace();
					}
		        } else {
		        	 System.out.println("Open command cancelled by user.");
		        }
			
			}
		});
		menubar.add(fileMenu);
		frame.setJMenuBar(menubar);
		frame.getContentPane().add(view.buildPanel());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
    

}
