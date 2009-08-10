package com.spitech.jaxbui;

import generated.po.Items;

import java.util.List;

import com.jgoodies.binding.adapter.AbstractTableAdapter;
import com.jgoodies.binding.list.ArrayListModel;

public class ItemTableAdapter extends AbstractTableAdapter {


	private static final String[] COLUMN_NAMES =
		{"productName", "quantity", "usPrice", "comment", "shipDate", "partNum"};
	
	public ItemTableAdapter(List<Items.Item> items) {
		super(new ArrayListModel(items), COLUMN_NAMES);
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Items.Item item = (Items.Item)getRow(rowIndex);
		switch (columnIndex) {
		case 0:
			return item.getProductName();
		case 1:
			return item.getQuantity();
		case 2:
			return item.getUSPrice();
		case 3:
			return item.getComment();
		case 4:
			return item.getShipDate();
		case 5:
			return item.getPartNum();
		default:
			return null;
		}
	}

	
}
