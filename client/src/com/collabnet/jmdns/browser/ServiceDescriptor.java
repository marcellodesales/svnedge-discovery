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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.swing.ImageIcon;

import com.collabnet.svnedge.discovery.SvnEdgeServerInfo;
import com.collabnet.svnedge.discovery.mdns.SvnEdgeCsvnServiceKey;

public class ServiceDescriptor implements Comparable<Object> {

    private ImageIcon img;
    private SvnEdgeServerInfo svnServerInfo;

    private ServiceDescriptor() {
    }

    public static ServiceDescriptor makeNew(SvnEdgeServerInfo serverInfo) {
        return makeNew(null, serverInfo);
    }

    public static ServiceDescriptor makeNew(ImageIcon image,
            SvnEdgeServerInfo serverInfo) {
        ServiceDescriptor sd = new ServiceDescriptor();
        sd.img = image;
        sd.svnServerInfo = serverInfo;
        return sd;
    }

    public ImageIcon getImage() {
        return this.img;
    }

    private String getTeamForgePath() {
        return this.svnServerInfo
                .getPropertyValue(SvnEdgeCsvnServiceKey.TEAMFORGE_PATH);
    }

    public boolean supportsTeamForgeRegistration() {
        return this.getTeamForgePath() != null;
    }

    public String getTeamForgeRegistrationUrl(String teamForgeUrl) {
        String url = toString();
        if (supportsTeamForgeRegistration() && teamForgeUrl != null
                && teamForgeUrl.length() > 0) {
            try {
                String ctfUrlQuery = "ctfURL=";
                url += this.getTeamForgePath();
                if (!url.endsWith("?")) {
                    url += "?";
                }
                if (!url.toLowerCase().endsWith(ctfUrlQuery.toLowerCase())) {
                    url += ctfUrlQuery;
                }
                url += URLEncoder.encode(teamForgeUrl, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return url;
    }

    /**
     * Gets the Subversion Edge URL with canonical host name
     * 
     * @return String representation of Subversion Edge
     */
    public String getNiceUrl() {
        String url = this.svnServerInfo.getUrl().toString();
        if (this.svnServerInfo.getUrl() != null && url != null) {
            String hostAddress = this.svnServerInfo.getHostAddress();
            if (url.contains(hostAddress)) {
                try {
                    String cHostName = this.svnServerInfo.getInetAddress()
                            .getCanonicalHostName();
                    if (cHostName != null && cHostName.length() > 0) {
                        url = url.replaceFirst(hostAddress, cHostName);
                    }
                } catch (Exception exc) {
                    // continue to use original URL (with IP address)
                    System.err.println(exc);
                }
            }
        }
        return url;
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
        String url = getNiceUrl();
        return url == null ? "?" : url;
    }

    public int compareTo(Object arg0) {
        if (arg0 instanceof ServiceDescriptor) {
            return this.svnServerInfo
                    .compareTo(((ServiceDescriptor) arg0).svnServerInfo);
        } else {
            return 0;
        }
    }

}
