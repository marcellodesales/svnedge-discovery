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
package com.collabnet.svnedge.discovery.mdns;

/**
 * The keys for the CSVN service. See SvnEdgeServiceType.CSVN.
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 * 
 */
public enum SvnEdgeCsvnServiceKey implements SvnEdgeServiceKey {

    CONTEXT_PATH("path"),
    TEAMFORGE_PATH("tfpath");

    /**
     * The key that is transported in the network.
     */
    private String key;

    private SvnEdgeCsvnServiceKey(String keyValue) {
        this.key = keyValue;
    }

    @Override
    public String toString() {
        return this.key;
    }
}
