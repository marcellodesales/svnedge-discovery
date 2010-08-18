package com.collabnet.jmdns.browser;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JList;
import javax.swing.ListModel;

import com.collabnet.jmdns.util.BrowserLauncher2;

public class ServiceActionMouseListener extends MouseAdapter {
	@Override
	public void mousePressed(MouseEvent arg0) {
		if (arg0.getClickCount() == 2) {
			Object source = arg0.getSource();
			if (source instanceof JList) {
				JList jList = (JList) source;
				int index = jList.locationToIndex(arg0.getPoint());
				if (index >= 0) {
					ListModel model = jList.getModel();
					ServiceDescriptor sd = (ServiceDescriptor) model.getElementAt(index);
					try {
						BrowserLauncher2.openURL2(sd.toString());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
}
