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
package com.collabnet.svnedge.discovery.client.browser.util;

import java.util.Hashtable;

import javax.swing.ImageIcon;

/**
 * Utility class to load resources
 */
public class ResourceLoader {

    public static ResourceLoader Instance = new ResourceLoader();

    /**
     * Relative path to the resources for this archive
     */
    private final String RESOURCE_PATH = "/resources/";

    /**
     * Map to cache the loaded image resources
     */
    private Hashtable<String, ImageIcon> imageCache;

    private ResourceLoader() {
        super();
        imageCache = new Hashtable<String, ImageIcon>();
    }

    /**
     * Utility method to get CollabNet icon
     * 
     * @return CollabNet <code>javax.swing.ImageIcon</code>
     */
    public ImageIcon getCollabNetIcon() {
        return getImageIcon("collabnet16.gif", "CollabNet");
    }

    /**
     * Utility method to get TeamForge icon
     * 
     * @return TeamForge <code>javax.swing.ImageIcon</code>
     */
    public ImageIcon getTeamForgeIcon() {
        return getImageIcon("tf16.gif", "TeamForge");
    }

    /**
     * Find the resource with the given <code>name</code> and create
     * <code>javax.swing.ImageIcon</code> instance
     * 
     * @return <code>javax.swing.ImageIcon</code>
     */
    public ImageIcon getImageIcon(String name, String description) {
        if (name == null || name.length() == 0) {
            return null;
        }
        ImageIcon img = null;
        if (imageCache.containsKey(name)) {
            img = imageCache.get(name);
        } else {
            String imagePath = RESOURCE_PATH + name;
            java.net.URL imageUrl = getResource(imagePath);
            if (imageUrl != null) {
                img = new ImageIcon(imageUrl, description);
                imageCache.put(name, img);
            }
        }
        return img;
    }

    private java.net.URL getResource(String name) {
        java.net.URL resourceUrl = ResourceLoader.class.getResource(name);
        if (resourceUrl == null) {
            System.err.println("Couldn't find resource: " + name);
        }
        return resourceUrl;
    }
}
