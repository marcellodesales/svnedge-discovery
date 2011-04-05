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

import javax.swing.ImageIcon;

import com.collabnet.svnedge.discovery.SvnEdgeServerInfo;
import com.collabnet.svnedge.discovery.client.browser.util.ResourceLoader;

public class ServiceDescriptor implements Comparable<Object> {

    private SvnEdgeServerInfo svnServerInfo;

    private ServiceDescriptor() {
    }

    public static ServiceDescriptor makeNew(SvnEdgeServerInfo serverInfo) {
        return makeNew(null, serverInfo);
    }

    public static ServiceDescriptor makeNew(ImageIcon image,
            SvnEdgeServerInfo serverInfo) {
        ServiceDescriptor sd = new ServiceDescriptor();
        sd.svnServerInfo = serverInfo;
        return sd;
    }

    public ImageIcon getImage() {
        return this.svnServerInfo.isManagedByTeamForge() ? ResourceLoader.Instance
                .getTeamForgeIcon() : ResourceLoader.Instance
                .getCollabNetIcon();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServiceDescriptor other = (ServiceDescriptor) obj;
        if (svnServerInfo == null) {
            if (other.svnServerInfo != null)
                return false;
        } else if (!svnServerInfo.equals(other.svnServerInfo))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((svnServerInfo == null) ? 0 : svnServerInfo.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return svnServerInfo.getUrl();
    }

    public int compareTo(Object arg0) {
        if (arg0 instanceof ServiceDescriptor) {
            return this.svnServerInfo
                    .compareTo(((ServiceDescriptor) arg0).svnServerInfo);
        } else {
            return 0;
        }
    }

    /**
     * @param teamForgeUrl
     *            is the URL of the teamforge server hosting displaying the
     *            services.
     * @return the teamforge registration URL for the service descriptor.
     */
    public String getTeamForgeRegistrationUrl(String teamForgeUrl) {
        return this.svnServerInfo.getTeamForgeRegistrationUrl(teamForgeUrl);
    }

    /**
     * @return the teamforge registration URL for the service descriptor. If
     * the svnedge server of the service is already managed, then the regular
     * URL is returned.
     */
    public String getTeamForgeRegistrationUrl() {
        return this.svnServerInfo.getTeamForgeRegistrationUrl();
    }

}
