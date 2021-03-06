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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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

public class CollabNetSvnBrowser extends JFrame implements
        SvnEdgeServersListener, ListSelectionListener {

    private static final long serialVersionUID = -6508836417205258085L;

    SvnEdgeBonjourClient svnEdgeServersClient;
    // Vector headers;
    String type;
    DefaultListModel types;
    DefaultListModel services;
    JList serviceList;
    boolean showGui;
    ImageIcon defaultServiceIcon;

    /**
     * Constructor (initialize with given service types)
     * 
     * @param mDNS
     * @throws IOException
     */
    CollabNetSvnBrowser(boolean createGui, SvnEdgeServiceType serviceType)
            throws IOException {
        super("CollabNet Subversion Server Discovery");

        this.showGui = createGui;
        if (showGui) {
            createGUI();
            this.setVisible(true);
        }
        this.svnEdgeServersClient = SvnEdgeBonjourClient
                .makeInstance(serviceType);
        this.svnEdgeServersClient.addServersListener(this);
    }

    private void createGUI() {
        Color bg = new Color(230, 230, 230);
        Border border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        Container content = getContentPane();
        content.setLayout(new BorderLayout(5, 20));

        // header panel start
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setLayout(new BorderLayout());
        JLabel imageLabel = new JLabel(ResourceLoader.Instance.getImageIcon(
                "logo_collabnet.gif", "CollabNet"), JLabel.LEFT);
        imageLabel.setBorder(border);
        headerPanel.add("North", imageLabel);
        JTextArea descLabel = new JTextArea();
        descLabel.setBorder(border);
        descLabel.setText("This application shows all of the CollabNet "
                + "Subversion servers that are currently active on "
                + "your local LAN subnet. Servers are discovered "
                + "using the Bonjour protocol. Click on any of the "
                + "servers listed below to be taken to the web login "
                + "screen for that server." + "\n\n"
                + "NOTE: This list will dynamically adjust as new "
                + "servers are discovered or leave the network.");
        descLabel.setEditable(false);
        descLabel.setLineWrap(true);
        descLabel.setWrapStyleWord(true);
        descLabel.setMinimumSize(descLabel.getPreferredSize());
        headerPanel.add("Center", descLabel);
        content.add("North", headerPanel);
        // header panel end

        // service panel start
        services = new DefaultListModel();
        serviceList = new JList(services);
        serviceList.setBackground(bg);
        serviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serviceList.setCellRenderer(new ServiceDescriptorRenderer());
        serviceList.addListSelectionListener(this);
        serviceList.addMouseListener(new ServiceActionMouseListener());
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
        footerPanel.setLayout(new BorderLayout());

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                System.exit(ABORT);
            }
        });
        footerPanel.add("East", closeButton);
        content.add("South", footerPanel);

        // footer panel end

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(100, 100);
        setSize(600, 400);
    }

    public void csvnServerStopped(SvnEdgeServerInfo serverInfo) {
        final ServiceDescriptor tempSd = ServiceDescriptor.makeNew(serverInfo);
        if (showGui) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    services.removeElement(tempSd);
                }
            });
        }
    }

    public void csvnServerIsRunning(SvnEdgeServerInfo serverInfo) {
        if (showGui) {
            ServiceDescriptor tempSd = ServiceDescriptor.makeNew(serverInfo);
            int index = services.indexOf(tempSd);
            boolean insertNew = index < 0;
            if (insertNew) {
                final ServiceDescriptor sd = ServiceDescriptor.makeNew(
                        getDefaultServiceIcon(), serverInfo);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        insertSorted(services, sd);
                    }
                });
            }

            else {
                services.add(index, tempSd);
                serviceList.repaint();
            }
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
        // do nothing.
    }

    public String toString() {
        return "CollabNet Subversion Server Browser";
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path, String descrip) {
        java.net.URL imgURL = CollabNetSvnBrowser.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, descrip);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    private ImageIcon getDefaultServiceIcon() {
        return defaultServiceIcon == null ? (defaultServiceIcon = ResourceLoader.Instance
                .getCollabNetIcon()) : defaultServiceIcon;
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) {
        boolean showGui = true;

        SvnEdgeServiceType serviceType = SvnEdgeServiceType.CSVN;
        if (args != null) {
            int argc = args.length;
            if ((argc > 0) && "-headless".equals(args[0])) {
                System.arraycopy(args, 1, args, 0, --argc);
                showGui = false;
            }
            if ((argc > 1) && "-servicetype".equals(args[0])) {
                serviceType = SvnEdgeServiceType.retrieveByType(args[1]);
                System.arraycopy(args, 2, args, 0, argc -= 2);
            }
        }
        try {
            new CollabNetSvnBrowser(showGui, serviceType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
