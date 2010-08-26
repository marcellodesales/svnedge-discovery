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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
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

public class CollabNetSvnBrowser extends JFrame implements ServiceListener, ListSelectionListener 
{
	private static final long serialVersionUID = -6508836417205258085L;
	private static final String SERVICE_TYPE__CSVN= "_csvn._tcp.local.";

	JmDNS jmdns;
	// Vector headers;
	String type;
	DefaultListModel types;
	DefaultListModel services;
	JList serviceList;
	boolean showGui;
	ImageIcon defaultServiceIcon;

	/**
	 * @param mDNS
	 * @throws IOException
	 */
	CollabNetSvnBrowser(JmDNS mDNS) throws IOException
	{
		this(true, mDNS);
	}

	CollabNetSvnBrowser(boolean createGui, JmDNS mDNS) throws IOException
	{
		this(createGui, mDNS, null);
	}

	/**
	 * Constructor (initialize with given service types)
	 * @param mDNS
	 * @throws IOException
	 */
	CollabNetSvnBrowser(boolean createGui, JmDNS mDNS, String serviceType) throws IOException
	{
		super("CollabNet Subversion Server Discovery");
		this.jmdns = mDNS;
		this.showGui = createGui;
		if (showGui) {
			createGUI();
		}
     	this.type = serviceType == null || serviceType.length() == 0 ? SERVICE_TYPE__CSVN : serviceType;
		this.jmdns.addServiceListener(type,this);
		
		if (showGui) {
			this.setVisible(true);
		}
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
		JLabel imageLabel = new JLabel(createImageIcon("/resources/logo_collabnet.gif", "CollabNet"), JLabel.LEFT);
		imageLabel.setBorder(border);
		headerPanel.add("North", imageLabel);
		JTextArea  descLabel = new JTextArea();
		descLabel.setBorder(border);
		descLabel.setText("This application shows all of the CollabNet Subversion servers that are currently active on your local LAN subnet."
				+ " Servers are discovered using the Bonjour protocol. Click on any of the servers listed below to be taken to the web login screen for that server."
				+ "\n"
				+ "\n"
				+ "NOTE: This list will dynamically adjust as new servers are discovered or leave the network."
		);
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
		serviceList.addMouseMotionListener(new ServiceListMouseMotionListener());
		JPanel servicePanel = new JPanel();
		servicePanel.setBorder(border);
		servicePanel.setLayout(new BorderLayout());
		servicePanel.add("Center", 
				new JScrollPane(serviceList, 
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
				)
		);
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

	/**
	 * Add a service.
	 *
	 * @param event
	 */
	public void serviceAdded(ServiceEvent event)
	{
		final String name = event.getName();
		System.out.println("Service ADD: " + name);
	}

	/**
	 * Remove a service.
	 *
	 * @param event
	 */
	public void serviceRemoved(ServiceEvent event)
	{
		final String name = event.getName();
		final ServiceDescriptor tempSd = new ServiceDescriptor(name, null);
		System.out.println("Service REMOVE: " + name);
		if (showGui) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run()
				{
					services.removeElement(tempSd);
				}
			});
		}
	}

	/**
	 * Resolve a service.
	 *
	 * @param event
	 */
	public void serviceResolved(ServiceEvent event)
	{
		String aName = event.getName();
		ServiceInfo anInfo = event.getInfo();
		System.out.println("Service Resolve: " + aName + " -> " + anInfo.getURL());
		if (showGui) {
			ServiceDescriptor tempSd = new ServiceDescriptor(anInfo, false);
			int index = services.indexOf(tempSd);
			boolean insertNew = index < 0;
			if (insertNew) {
				final ServiceDescriptor sd = new ServiceDescriptor(getDefaultServiceIcon(), anInfo, false);
				SwingUtilities.invokeLater(new Runnable() {
					public void run()
					{
						insertSorted(services, sd);
					}
				});
			}

			else {
				ServiceDescriptor sd = (ServiceDescriptor) services.getElementAt(index);
				sd.setServiceUrl(anInfo.getURL());
				serviceList.repaint();
			}
		}
	}

	void insertSorted(DefaultListModel model, String value)
	{
		for (int i = 0, n = model.getSize(); i < n; i++)
		{
			if (value.compareToIgnoreCase((String) model.elementAt(i)) < 0)
			{
				model.insertElementAt(value, i);
				return;
			}
		}
		model.addElement(value);
	}

	void insertSorted(DefaultListModel model, ServiceDescriptor value)
	{
		for (int i = 0, n = model.getSize(); i < n; i++)
		{
			if (value.compareTo(model.elementAt(i)) < 0)
			{
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
	public void valueChanged(ListSelectionEvent e)
	{
		if (e.getSource() == serviceList)
		{
			/*
			String name = (String) serviceList.getSelectedValue();
			if (name == null)
			{
				info.setText("");
				return;
			}
			if (!e.getValueIsAdjusting())
			{
				System.out.println(this + " valueChanged() type:" + type + " name:" + name);
				System.out.flush();
				ServiceInfo service = jmdns.getServiceInfo(type, name);
				if (service == null)
				{
					info.setText("service not found");
				}
				else
				{
					jmdns.requestServiceInfo(type, name);
				}
			}
			else {
				ShowDetails(jmdns.getServiceInfo(type, name));
			}*/
		}
	}

	public String toString()
	{
		return "CollabNet Subversion Server Browser";
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path,
			String description) {
		java.net.URL imgURL = CollabNetSvnBrowser.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	private ImageIcon getDefaultServiceIcon() {
		return defaultServiceIcon == null ?
				(defaultServiceIcon = createImageIcon("/resources/collabnet16.gif", "CollabNet"))
				: defaultServiceIcon;
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		boolean showGui = true;
		String serviceType = null;

		if (args != null) {
			int argc = args.length;
			if ((argc > 0) && "-headless".equals(args[0])) {
				System.arraycopy(args, 1, args, 0, --argc);
				showGui = false;
			}
			if ((argc > 1) && "-servicetype".equals(args[0])) {
				serviceType = args[1];
				System.arraycopy(args, 2, args, 0, argc -= 2);
			}
		}
		try {
			new CollabNetSvnBrowser(showGui, JmDNS.create(), serviceType);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
