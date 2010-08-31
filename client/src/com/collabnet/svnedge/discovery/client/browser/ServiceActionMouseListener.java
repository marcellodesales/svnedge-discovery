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
package com.collabnet.svnedge.discovery.client.browser;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JList;
import javax.swing.ListModel;


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
                    ServiceDescriptor sd = (ServiceDescriptor) model
                            .getElementAt(index);
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
