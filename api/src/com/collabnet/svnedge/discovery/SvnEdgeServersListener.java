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
package com.collabnet.svnedge.discovery;

/**
 * The SvnEdge Server Observer is a type interested when a server has started
 * or stopped. That is, when the server starts, it is resolved by the network.
 * When the server is stopped, it will not be available in the network.
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 *
 */
public interface SvnEdgeServersListener {

    /**
     * This event when an SvnEdge server is identified to be running.
     * @param serverInfo is the server information.
     */
    public void csvnServerIsRunning(SvnEdgeServerInfo serverInfo);
    /**
     * The event when an SvnEdge server stops and can't be contacted in the
     * network.
     * @param serverInfo
     */
    public void csvnServerStopped(SvnEdgeServerInfo serverInfo);
}
