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
 * The SvnEdgeServiceType enumarates the Bonjour/mDNS service types related
 * to the SvnEdge service "collabnetsvn". Each service type contains a list
 * of associated SvnEdgeServiceKeys related to the service and used by SvnEdge.
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 *
 */
public enum SvnEdgeServiceType {

    /**
     * The service type _csvn._tcp.local.
     */
    CSVN("_csvn._tcp.local."),
    /**
     * The service type _http._tcp.local.
     */
    HTTP("_http._tcp.local.");

    /**
     * The type of the service.
     */
    private String mDnsType;

    /**
     * Creates a new instance of the SvnEdgeServiceType with the given type and
     * associated name.
     * 
     * @param mDNSType
     *            is the bonjour type, which includes an identifier followed by
     *            the transport protocol.
     */
    private SvnEdgeServiceType(String mDNSType) {
        this.mDnsType = mDNSType;
    }

    /**
     * @return the bonjour service type.
     */
    public String getType() {
        return this.mDnsType;
    }

    @Override
    public String toString() {
        return this.mDnsType;
    }

    /**
     * @return an array with the list of the keys required by the type.
     */
    public SvnEdgeServiceKey[] getRequiredKeys() {
        switch (this) {
        case CSVN:
            return SvnEdgeCsvnServiceKey.values();
        case HTTP:
            return SvnEdgeHttpServiceKey.values();
        default:
            return new SvnEdgeServiceKey[] {};
        }
    }

    /**
     * @param mDNSType
     *            is one of the mDNS type for SvnEdge.
     * @return an instance related to the mDNS service type.
     */
    public static SvnEdgeServiceType retrieveByType(String mDNSType) {
        for (SvnEdgeServiceType t : SvnEdgeServiceType.values()) {
            if (t.toString().equals(mDNSType)) {
                return t;
            }
        }
        return null;
    }
}
