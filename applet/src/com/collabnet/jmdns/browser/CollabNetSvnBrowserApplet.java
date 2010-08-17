package com.collabnet.jmdns.browser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
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

public class CollabNetSvnBrowserApplet extends JApplet implements ServiceListener, ListSelectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6654417233709753938L;

	private static final String SERVICE_TYPE__CSVN= "_csvn._tcp.local.";

	JmDNS jmdns;
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
	public CollabNetSvnBrowserApplet() throws IOException
	{
		this(JmDNS.create());
	}

	/**
	 * @param mDNS
	 * @throws IOException
	 */
	CollabNetSvnBrowserApplet(JmDNS mDNS) throws IOException
	{
		this(mDNS, SERVICE_TYPE__CSVN);
	}

	/**
	 * Constructor (initialize with given service types)
	 * @param mDNS
	 * @throws IOException
	 */
	CollabNetSvnBrowserApplet(JmDNS mDNS, String serviceType) throws IOException
	{
		this.jmdns = mDNS;
     	this.type = serviceType == null || serviceType.length() == 0 ? SERVICE_TYPE__CSVN : serviceType;
	}

	public void init() {
		try {
			URL url = getDocumentBase();
			String urlString =url.getProtocol() +  "://" + url.getAuthority(); 
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
							ServiceDescriptor sd = (ServiceDescriptor) model.getElementAt(index);
							try {
								URL url = new URL(
										isTeamForge ?
												sd.GetTeamForgeRegistrationUrl(hostUrl)
												: sd.toString());
								getAppletContext().showDocument(url, "_blank");
							} catch (IOException exc) {
								exc.printStackTrace(); // ignore
							}
						}
					}
				}
			}
		});
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
		footerPanel.setLayout(new BorderLayout(5,0));
		JLabel currentUrlLbl = new JLabel("Current Domain: ");
		footerPanel.add("West", currentUrlLbl);
		JLabel currentUrl = new JLabel(hostUrl);
		if ( (isTeamForge)) {
			currentUrl.setIcon(createImageIcon("/resources/tf16.gif", "TeamForge"));
			currentUrl.setIconTextGap(8);
		}

		footerPanel.add("Center", currentUrl);
		content.add("South", footerPanel);
		// footer panel end
		
		setLocation(100, 100);
		setSize(600, 300);
		
		this.jmdns.addServiceListener(type,this);
    	this.setVisible(true);
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
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				services.removeElement(tempSd);
			}
		});
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
		ServiceDescriptor tempSd = new ServiceDescriptor(anInfo, isTeamForge);
		int index = services.indexOf(tempSd);
		boolean insertNew = index < 0;
		if (insertNew) {
			final ServiceDescriptor sd = new ServiceDescriptor(getDefaultServiceIcon(), anInfo, isTeamForge);
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
		java.net.URL imgURL = CollabNetSvnBrowserApplet.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	/**
	 * Determines if the given URL is a TeamForge URL
	 * @param url
	 * @return
	 */
	private boolean isTeamForge(URL url) {
	
		String path = url.getPath();
		return path != null
			&& path.startsWith("/sf/");
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
		try {
			new CollabNetSvnBrowserApplet();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
