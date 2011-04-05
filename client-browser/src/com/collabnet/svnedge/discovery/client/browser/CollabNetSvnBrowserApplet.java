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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.collabnet.svnedge.discovery.SvnEdgeBonjourClient;
import com.collabnet.svnedge.discovery.SvnEdgeServerInfo;
import com.collabnet.svnedge.discovery.SvnEdgeServersListener;
import com.collabnet.svnedge.discovery.client.browser.util.ResourceLoader;
import com.collabnet.svnedge.discovery.mdns.SvnEdgeServiceType;

public class CollabNetSvnBrowserApplet extends JApplet implements
        SvnEdgeServersListener, ListSelectionListener {

    private static final long serialVersionUID = -6654417233709753938L;

    private SvnEdgeBonjourClient csvnServersClient;
    // Vector headers;
    String type;
    DefaultListModel types;
    DefaultListModel services;
    JList serviceList;
    ImageIcon defaultServiceIcon;
    boolean isTeamForge;
    String hostUrl;

    /**
     * @param mDNS
     * @throws IOException
     */
    public CollabNetSvnBrowserApplet() throws IOException {
        this.csvnServersClient = SvnEdgeBonjourClient
                .makeInstance(SvnEdgeServiceType.CSVN);
    }

    public void init() {
        try {
            URL url = getDocumentBase();
            String urlString = url.getProtocol() + "://" + url.getAuthority();
            hostUrl = urlString;
            isTeamForge = isTeamForge(url);
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    createGUI();
                }
            });
        } catch (Exception e) {
            System.err.println("createGUI didn't successfully complete");
        }
    }

    private void createGUI() {
        Border border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        Container content = getContentPane();
        content.setLayout(new BorderLayout(5, 20));

        // service panel start
        services = new DefaultListModel();
        serviceList = new JList(services);
        serviceList.setBackground(Color.WHITE);
        serviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serviceList.setCellRenderer(new ServiceDescriptorRenderer());
        serviceList.addListSelectionListener(this);
        serviceList.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1
                        && e.getButton() == MouseEvent.BUTTON1) {
                    Object source = e.getSource();
                    if (source instanceof JList) {
                        JList jList = (JList) source;
                        int index = jList.locationToIndex(e.getPoint());
                        if (index >= 0) {
                            ListModel model = jList.getModel();
                            ServiceDescriptor sd = (ServiceDescriptor) model
                                    .getElementAt(index);
                            try {
                                URL url = new URL(isTeamForge ? 
                                        sd.getTeamForgeRegistrationUrl(hostUrl)
                                        : sd.getTeamForgeRegistrationUrl());
                                getAppletContext().showDocument(url, "_blank");
                            } catch (IOException exc) {
                                exc.printStackTrace(); // ignore
                            }
                        }
                    }
                }
            }
        });

        serviceList
                .addMouseMotionListener(new ServiceListMouseMotionListener());

        JPanel servicePanel = new JPanel();
        servicePanel.setBorder(border);
        servicePanel.setLayout(new BorderLayout());
        servicePanel.add("Center", new JScrollPane(serviceList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        content.add("Center", servicePanel);
        // service panel end

        // footer panel start
        JPanel footerPanel = new JPanel();
        footerPanel.setBorder(border);
        footerPanel.setLayout(new BorderLayout(5, 0));
        JLabel currentUrlLbl = new JLabel("Current Domain: ");
        footerPanel.add("West", currentUrlLbl);
        JLabel currentUrl = new JLabel(hostUrl);
        if ((isTeamForge)) {
            currentUrl.setIcon(ResourceLoader.Instance.getTeamForgeIcon());
            currentUrl.setIconTextGap(8);
        }

        footerPanel.add("Center", currentUrl);
        content.add("South", footerPanel);
        // footer panel end

        setLocation(100, 100);
        setSize(600, 300);

        this.csvnServersClient.addServersListener(this);
        this.setVisible(true);
    }

    public void csvnServerStopped(SvnEdgeServerInfo serverInfo) {
        final String name = serverInfo.getServiceName();
        final ServiceDescriptor tempSd = ServiceDescriptor.makeNew(serverInfo);
        System.out.println("Service REMOVE: " + name);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                services.removeElement(tempSd);
            }
        });
    }

    public void csvnServerIsRunning(SvnEdgeServerInfo serverInfo) {
        String aName = serverInfo.getServiceName();
        System.out.println("Service Resolve: " + aName + " -> "
                + serverInfo.getUrl());
        ServiceDescriptor tempSd = ServiceDescriptor.makeNew(serverInfo);
        int index = services.indexOf(tempSd);
        boolean insertNew = index < 0;
        if (insertNew) {
            final ServiceDescriptor sd = ServiceDescriptor.makeNew(
                    ResourceLoader.Instance.getCollabNetIcon(), serverInfo);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    insertSorted(services, sd);
                }
            });
        }

        else {
            services.setElementAt(tempSd, index);
            serviceList.repaint();
        }
    }

    void insertSorted(DefaultListModel model, String value) {
        for (int i = 0, n = model.getSize(); i < n; i++) {
            if (value.compareToIgnoreCase((String) model.elementAt(i)) < 0) {
                model.insertElementAt(value, i);
                return;
            }
        }
        model.addElement(value);
    }

    void insertSorted(DefaultListModel model, ServiceDescriptor value) {
        for (int i = 0, n = model.getSize(); i < n; i++) {
            if (value.compareTo(model.elementAt(i)) < 0) {
                model.insertElementAt(value, i);
                return;
            }
        }
        model.addElement(value);
    }

    /**
     * List selection changed.
     * 
     * @param e
     */
    public void valueChanged(ListSelectionEvent e) {
        // do nothing
    }

    public String toString() {
        return "CollabNet Subversion Server Browser";
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path, String descript) {
        URL imgURL = CollabNetSvnBrowserApplet.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, descript);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Determines if the given URL is a TeamForge URL
     * 
     * @param url
     * @return
     */
    private boolean isTeamForge(URL url) {
        String path = url.getPath();
        return path != null && path.startsWith("/sf/");
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) {
        try {
            new CollabNetSvnBrowserApplet();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
