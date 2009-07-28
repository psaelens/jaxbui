package com.spitech.jaxbui;

import generated.Items;
import generated.PurchaseOrderType;
import generated.view.PurchaseOrderTypeView;

import javax.swing.JFrame;

public class PurchaseOrderApp {

	/**
	 * @param args
	 */
    public static void main(String[] args) {
		JFrame frame = new JFrame(".:Purchase Order Editor");
		frame.getContentPane().add(new PurchaseOrderTypeView(new PurchaseOrderType()).buildPanel());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
    

}
