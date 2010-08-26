/*
 * CollabNet Subversion Edge
 * Copyright (C) 2010, CollabNet Inc. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.collabnet.jmdns.browser;

import java.awt.Color;
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
		super();
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
	        label.setForeground(Color.BLUE);
			label.setIcon(((ServiceDescriptor) value).getImage()); 
			label.setIconTextGap(8);
		}
		return(label);
	}
}
