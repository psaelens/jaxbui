package com.spitech.jaxbui;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import com.sun.codemodel.JPackage;

/**
 * Hello world!
 *
 */
public class App 
{
	
	public static class TracingEventQueue extends EventQueue {

//		   private TracingEventQueueThread tracingThread;

		   public TracingEventQueue() {
//		      this.tracingThread = new TracingEventQueueThread(500);
//		      this.tracingThread.start();
		   }

		   @Override
		   protected void dispatchEvent(AWTEvent event) {
//		      this.tracingThread.eventDispatched(event);
		      super.dispatchEvent(event);
//		      this.tracingThread.eventProcessed(event);
		   }
		}

	
    public static void main( String[] args )
    {
    	Toolkit.getDefaultToolkit().getSystemEventQueue().push(
    		    new TracingEventQueue());

        JButton hogEDT = new JButton("Hog EDT");
        hogEDT.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
              // simulate load
              try {
                 Thread.sleep(5000);
              } catch (InterruptedException ie) {
              }
           }
        });
        
        JTextField tf = new JTextField("some text") {
        	   @Override
        	   public void paint(Graphics g) {
        	      this.setBorder(new LineBorder(Color.red));
        	      super.paint(g);
        	   }
        	};


        JFrame frame = new JFrame("Debugging Swing");
        JPanel panel = new JPanel();
        panel.add(hogEDT);
        panel.add(tf);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
