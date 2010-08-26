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

import javax.jmdns.ServiceInfo;
import javax.swing.ImageIcon;

public class ServiceDescriptor implements Comparable<Object>{

	private String serviceName;
	private String serviceUrl;
	private ImageIcon img;
	private String teamForgePath;

	ServiceDescriptor(String serviceName, String url) {
		this.serviceName = serviceName;
		setServiceUrl(url);
	}

	ServiceDescriptor(ImageIcon image, ServiceInfo info, boolean isTeamForge) {
		serviceName = info.getName();
		img = image;
		String svcUrl = info.getURL();
		teamForgePath = null;
		if (isTeamForge) {
			String tfPath = info.getPropertyString("tfpath");
			if (tfPath != null && tfPath.length() > 0) {
				teamForgePath = tfPath;
			}
		}
		setServiceUrl(svcUrl);
	}

	ServiceDescriptor(ServiceInfo info, boolean isTeamForge) {
		this (null, info, isTeamForge);
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public String getServiceName() {
		return serviceName;
	}
	
	public ImageIcon getImage() {
	
		return img;
	}
	
	public boolean SupportsTeamForgeRegistration() {
		return teamForgePath != null;
	}
	
	public String GetTeamForgeRegistrationUrl(String teamForgeUrl) {
		String url = toString();
		if (SupportsTeamForgeRegistration() && teamForgeUrl != null && teamForgeUrl.length() > 0) {
			try {
				String ctfUrlQuery = "ctfURL=";
				url += teamForgePath;
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
	
	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof ServiceDescriptor) {
			return getServiceName().equals(((ServiceDescriptor) arg0).getServiceName());
		}
		else if (arg0 instanceof ServiceInfo) {
			return getServiceName().equals(((ServiceInfo) arg0).getName());
		}
		else if (arg0 instanceof String) {
			return getServiceName().equals((String) arg0);
		}
		return super.equals(arg0);
	}

	@Override
	public int hashCode() {
		return getServiceName().hashCode();
	}

	@Override
	public String toString() {
		return getServiceUrl() == null ? "?" : getServiceUrl();
	}

	public int compareTo(Object arg0) {
		if (arg0 instanceof ServiceDescriptor) {
			return getServiceName().compareToIgnoreCase(((ServiceDescriptor) arg0).getServiceName());
		}
		else if (arg0 instanceof ServiceInfo) {
			return getServiceName().compareToIgnoreCase(((ServiceInfo) arg0).getName());
		}
		else if (arg0 instanceof String) {
			return getServiceName().compareToIgnoreCase((String) arg0);
		}
		return 0;
	}
}
