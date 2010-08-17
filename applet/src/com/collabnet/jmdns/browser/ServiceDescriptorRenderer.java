package com.collabnet.jmdns.browser;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

public class ServiceDescriptorRenderer extends DefaultListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1048671867146583463L;

	public ServiceDescriptorRenderer() {
	}

	public Component getListCellRendererComponent(JList list,
			Object value,
			int index,
			boolean isSelected,
			boolean hasFocus) {
		JLabel label =
			(JLabel)super.getListCellRendererComponent(list,
					value,
					index,
					isSelected,
					hasFocus);
		if (value instanceof ServiceDescriptor) {
			label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
			label.setIcon(((ServiceDescriptor) value).getImage()); 
			label.setIconTextGap(8);
		}
		return(label);
	}
}
