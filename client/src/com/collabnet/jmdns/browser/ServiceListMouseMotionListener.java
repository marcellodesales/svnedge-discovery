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

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JList;

public class ServiceListMouseMotionListener extends MouseMotionAdapter {

    /**
     * Use <code>java.awt.Cursor.HAND_CURSOR</code> if the mouse is over a
     * discovered server
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        Object source = e.getSource();
        if (source instanceof JList) {
            Cursor cursor = Cursor.getDefaultCursor();
            JList jList = (JList) source;
            int index = jList.locationToIndex(e.getPoint());
            if (index >= 0) {
                if (jList.getCellBounds(index, index).contains(e.getPoint())) {
                    cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                }
            }
            jList.setCursor(cursor);
        }
    }
}
