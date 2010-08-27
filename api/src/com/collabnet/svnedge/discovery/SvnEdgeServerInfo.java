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

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.jmdns.ServiceInfo;

import com.collabnet.svnedge.discovery.mdns.SvnEdgeServiceKey;
import com.collabnet.svnedge.discovery.mdns.SvnEdgeServiceType;

/**
 * This class describes an SVN service provided by an instance of CollabNet
 * Subversion Edge server published using mDNS.
 * 
 * @author Marcello de Sales(mdesales@collab.net)
 */
public final class SvnEdgeServerInfo implements Comparable<SvnEdgeServerInfo> {

    /**
     * The name of the service given by the mDNS name resolution. The default
     * name for svn edge is "collabnetsvn". If other servers are added on the
     * same host in a different port, the names are added indexes
     * "collabnetsvn (2)".
     */
    private String serviceName;
    /**
     * This is the inet address instance of the service. Any information
     * regarding the IP address and hostname.
     */
    private InetAddress ipAddress;
    /**
     * The port where the web console is running.
     */
    private int port;
    /**
     * The published URL provided by the service.
     */
    private URL url;
    /**
     * The map with the keys and values retrieved from the mDNS packet.
     */
    private Map<SvnEdgeServiceKey, String> keyValues =
            new HashMap<SvnEdgeServiceKey, String>();

    /**
     * Factory method that builds a new SvnEdge Service Info from a given
     * service info from mDNS.
     * 
     * @param serviceInfo
     *            is an instance of a service info captured from jmDNS.
     * @return a new instance of the SvnEdge Service Info with the captured
     *         values from the service.
     */
    public static SvnEdgeServerInfo makeNew(ServiceInfo serviceInfo) {
        SvnEdgeServerInfo newInst = new SvnEdgeServerInfo();
        newInst.serviceName = serviceInfo.getName();
        newInst.ipAddress = serviceInfo.getInetAddress();
        newInst.port = serviceInfo.getPort();
        try {
            newInst.url = new URL(serviceInfo.getURL());
        } catch (MalformedURLException e) {
            try {
                newInst.url =
                        new URL("http://" + newInst.getHostname() + ":" +
                                newInst.port);
            } catch (MalformedURLException e1) {
            }
        }
        SvnEdgeServiceType type =
                SvnEdgeServiceType.retrieveByType(serviceInfo.getType());
        for (SvnEdgeServiceKey key : type.getRequiredKeys()) {
            newInst.keyValues.put(key,
                    serviceInfo.getPropertyString(key.toString()));
        }
        return newInst;
    }

    @Override
    public String toString() {
        StringBuilder propsBuilder = new StringBuilder();
        propsBuilder.append(" Keys: ");
        for (SvnEdgeServiceKey key : this.keyValues.keySet()) {
            propsBuilder.append("[" + key + "]=");
            propsBuilder.append(this.keyValues.get(key));
            propsBuilder.append(" ");
        }
        return "SvnEdgeServerInfo: name=" + serviceName + ", url=" + getUrl() +
               propsBuilder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((serviceName == null) ? 0 : 
            serviceName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SvnEdgeServerInfo other = (SvnEdgeServerInfo) obj;
        if (this.serviceName == null) {
            if (other.serviceName != null)
                return false;
        } else if (!serviceName.equals(other.serviceName))
            return false;
        return true;
    }

    /**
     * @return the name of the bonjour service associated.
     */
    public String getServiceName() {
        return this.serviceName;
    }

    /**
     * @return the IP address of the service.
     */
    public String getHostAddress() {
        return this.ipAddress.getHostAddress();
    }

    /**
     * @return The hostname of the server where the application is running.
     */
    public String getHostname() {
        return this.ipAddress.getHostName();
    }

    /**
     * @return the port where the service is running.
     */
    public int getPort() {
        return port;
    }

    /**
     * @return is the URL of the SvnEdge web console.
     */
    public URL getUrl() {
        return url;
    }

    /**
     * @param key is the service key.
     * @return the text representation for the given key. 
     */
    public String getPropertyValue(SvnEdgeServiceKey key) {
        return this.keyValues.get(key);
    }

    @Override
    public int compareTo(SvnEdgeServerInfo other) {
        return this.getUrl().toString().compareTo(other.getUrl().toString());
    }
}
