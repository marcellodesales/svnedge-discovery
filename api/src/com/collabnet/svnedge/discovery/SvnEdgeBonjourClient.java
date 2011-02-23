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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.apache.log4j.Logger;

import com.collabnet.svnedge.discovery.mdns.SvnEdgeServerEvent;
import com.collabnet.svnedge.discovery.mdns.SvnEdgeServiceType;

/**
 * The SvnEdge Bounjour Client is a general client to be used by any clients.
 * The mDNS service type is is described as "csvn" that is listened on a local
 * TCP network ("_csvn._tcp.local.").
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 * 
 */
public class SvnEdgeBonjourClient implements ServiceListener {

    private static final Logger log = Logger.getLogger(SvnEdgeBonjourClient.class);
    /**
     * The jMDNS client instance
     */
    private JmDNS jmdns;
    /**
     * The list of service listeners that are interested when a new service has
     * been added to the mDNS proxy.
     */
    private Queue<SvnEdgeServersListener> observers;

    /**
     * Constructs a new instance with the given ipAddress, hostname and type.
     * @param address is the ip address to be assigned to the jmDNS service.
     * @param hostname is the hostname to identify the service.
     * @param type is the type of the service to probe.
     * @throws IOException if there is no Internet connectivity.
     */
    private SvnEdgeBonjourClient(InetAddress address, String hostname,
            SvnEdgeServiceType type)  throws IOException {
        if (address != null) {
            if (hostname != null && !hostname.equals("")) {
                jmdns = JmDNS.create(address, hostname);
                log.debug("Initializing Discovery Client with address " + 
                        address + "and hostname " + hostname);

            } else {
                log.debug("Initializing Discovery Client with address " + 
                        address);
                jmdns = JmDNS.create(address);
            }
        } else {
            log.debug("Initializing Discovery Client");
            jmdns = JmDNS.create();
        }
        jmdns.addServiceListener(type.toString(), this);
        this.observers = new ConcurrentLinkedQueue<SvnEdgeServersListener>();
    }

    /**
     * Creates a new client to probe for the given type.
     * @param type is the service type. See getServiceTypes().
     * @return an instance of the SvnEdgeBonjourClient.
     * @throws IOException if any problem trying to start the service occurs.
     */
    public static SvnEdgeBonjourClient makeInstance(SvnEdgeServiceType type) 
        throws IOException {

        return new SvnEdgeBonjourClient(null, null, type);
    }

    /**
     * Creates a new client using the given ip address that will reply to the
     * given type.
     * @param ipAddress is the ip address to publish the service.
     * @param type is the service type. See getServiceTypes().
     * @return an instance of the SvnEdgeBonjourClient.
     * @throws IOException if any problem trying to start the service occurs.
     */
    public static SvnEdgeBonjourClient makeInstance(InetAddress ipAddress,
          SvnEdgeServiceType type) throws IOException {

        return new SvnEdgeBonjourClient(ipAddress, null, type);
    }

    /**
     * Creates a new client using the given ip address that will reply to the
     * given type.
     * @param ipAddress is the ip address to publish the service.
     * @param hostname is hostname chosen by the client, mostly used for 
     * debugging.
     * @param type is the service type. See getServiceTypes().
     * @return an instance of the SvnEdgeBonjourClient.
     * @throws IOException if any problem trying to start the service occurs.
     */
    public static SvnEdgeBonjourClient makeInstance(InetAddress ipAddress,
          String hostname, SvnEdgeServiceType type) throws IOException {

        return new SvnEdgeBonjourClient(ipAddress, hostname, type);
    }

    /**
     * The stop method forces the client to completely stop the jmDNS service.
     * @throws IOException in case the close operation fails.
     */
    public void stop() throws IOException {
    	log.debug("Stopping the jmDSN client instance.");
        if (jmdns != null) {
            jmdns.close();
            jmdns = null;
        }
    }

    /**
     * Adds a new observer/listener to the service.
     * 
     * @param newLis is new observer interested in the events of any Subversion
     * Edge server in the local network.
     */
    public synchronized void addServersListener(SvnEdgeServersListener newLis) {
        this.observers.add(newLis);
    }

    /**
     * @return The available Service types implemented.
     */
    public static SvnEdgeServiceType[] getServiceTypes() {
        return SvnEdgeServiceType.values();
    }

    /**
     * Shows the information about the given ServiceInfo instance in the
     * debug level, after showing a given description.
     * @param description is a description of where the given service info was
     * originated from.
     * @param info the jmDNS service Info.
     */
    private void debug(String description, ServiceInfo info) {
        StringBuilder builder = new StringBuilder();
        builder.append("ServiceInfo: Nice:");
        builder.append(info.getNiceTextString());
        builder.append(", Inetaddress: [");
        for (InetAddress inetAddr : info.getInetAddresses()) {
            builder.append(inetAddr);
            builder.append(" , ");
        }
        builder.replace(builder.length() - 3, builder.length(), "");
        builder.append("], Host Addresses:[");
        for (String hostAddr : info.getHostAddresses()) {
            builder.append(hostAddr);
            builder.append(" , ");
        }
        builder.replace(builder.length() - 3, builder.length(), "");
        builder.append("], Port:");
        builder.append(info.getPort());
        builder.append(", Server:");
        builder.append(info.getServer());
        builder.append(", Urls:[");
        for (String url : info.getURLs()) {
            builder.append(url);
            builder.append(" , ");
        }
        builder.replace(builder.length() - 3, builder.length(), "");
        builder.append("], Qualified Name:");
        builder.append(info.getQualifiedName());
        builder.append(", Type:");
        builder.append(info.getType());
        builder.append(", Priority:");
        builder.append(info.getPriority());
        builder.append(", Weight:");
        builder.append(info.getWeight());
        builder.append(", Domain:");
        builder.append(info.getDomain());
        builder.append(", Protocol:");
        builder.append(info.getProtocol());
        builder.append(", Application:");
        builder.append(info.getApplication());
        builder.append(", Name:");
        builder.append(info.getName());
        builder.append(", Key:");
        builder.append(info.getKey());
        builder.append(", Type with Subtype:");
        builder.append(info.getTypeWithSubtype());
        builder.append(", Properties:[");
        Enumeration<String> props = info.getPropertyNames();
        while (props.hasMoreElements()) {
            String propName = props.nextElement();
            String propValue = info.getPropertyString(propName);
            builder.append(propName);
            builder.append(": ");
            builder.append(propValue);
            builder.append(" , ");
        }
        builder.replace(builder.length() - 3, builder.length(), "");
        builder.append("]");
        log.debug(description);
        log.debug(builder.toString());
    }

    /* (non-Javadoc)
     * @see javax.jmdns.ServiceListener#serviceAdded(javax.jmdns.ServiceEvent)
     */
    public void serviceAdded(ServiceEvent addedEvent) {
        synchronized (this) {
            // when any bonjour client is added to the jmDNS client. Do nothing.
            debug("SvnEdge Server in cache running...", addedEvent.getInfo());
            // update the client cache
            final boolean persistent = false;
            this.jmdns.getServiceInfo(addedEvent.getInfo().getTypeWithSubtype(),
                    addedEvent.getInfo().getName(), persistent);
        }
    }

    /* (non-Javadoc)
     * @see javax.jmdns.ServiceListener#serviceRemoved(javax.jmdns.ServiceEvent)
     */
    public void serviceRemoved(ServiceEvent removedEvent) {
        synchronized (this) {
            debug("SvnEdge Server stopped...", removedEvent.getInfo());
            SvnEdgeServerInfo serverInfo = SvnEdgeServerInfo.makeNew(
                    removedEvent.getInfo(), SvnEdgeServerEvent.SERVER_SHUTDOWN);
               log.debug("Informing " + this.observers.size() + " observer(s)");
               for (SvnEdgeServersListener observer : this.observers) {
                   observer.csvnServerStopped(serverInfo);
               }
        }
    }

    /* (non-Javadoc)
     * @see javax.jmdns.ServiceListener#serviceResolved(javax.jmdns.ServiceEvent)
     */
    public void serviceResolved(ServiceEvent resolvedEvent) {
        synchronized(this) {
            debug("SvnEdge Server running...", resolvedEvent.getInfo());
            SvnEdgeServerInfo serverInfo = SvnEdgeServerInfo.makeNew(
                    resolvedEvent.getInfo(), SvnEdgeServerEvent.SERVER_RUNNING);

            log.debug("Informing " + this.observers.size() + " observer(s)...");
            for (SvnEdgeServersListener observer : this.observers) {
                observer.csvnServerIsRunning(serverInfo);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("%%%%%%%%% SvnEdge Bonjour Client %%%%%%%%%%%%%%");

        Enumeration<NetworkInterface> infs =
                NetworkInterface.getNetworkInterfaces();
        Map<Integer, InetAddress> ipAddresses =
                new HashMap<Integer, InetAddress>();
        int count = 0;
        while (infs.hasMoreElements()) {
            NetworkInterface inf = infs.nextElement();
            boolean isLoopback = false;
            List<InetAddress> validAddresses = new ArrayList<InetAddress>();
            Enumeration<InetAddress> availableIps = inf.getInetAddresses();
            while (availableIps.hasMoreElements()) {
                InetAddress ip = availableIps.nextElement();
                isLoopback |= ip.isLoopbackAddress();
                if (ip instanceof Inet4Address) {
                    validAddresses.add(ip);
                }
            }
            if (!isLoopback && !validAddresses.isEmpty()) {
                for (InetAddress ip : validAddresses) {
                    ipAddresses.put(++count, ip);
                }
            }
        }
        InetAddress selectedIp = null;
        if (ipAddresses.size() > 0) {
            for (Integer index : ipAddresses.keySet()) {
                if (ipAddresses.get(index) instanceof Inet4Address) {
                    System.out.println("# " + (index) + ") " +
                                       ipAddresses.get(index).getHostAddress());
                }
            }
            System.out.println("# A) ALL ");
            System.out.print("Which IP address you wanna use? ");
            InputStreamReader converter = new InputStreamReader(System.in);
            BufferedReader input = new BufferedReader(converter);
            String selected = input.readLine();
            if (selected.equalsIgnoreCase("q")) {
                System.out.println("Thanks for using the SvnEdge API Client");
                System.exit(0);
            } else {
                if (selected.equalsIgnoreCase("a")) {
                    selectedIp = null;
                } else {
                    selectedIp = ipAddresses.get(Integer.valueOf(selected));
                }
            }
        }

        System.out.println();
        System.out.println("%% Available SvnEdge Service Types:");
        int i = 0;

        // getServiceTypes() is just a proxy to SvnEdgeServiceType.values()
        int totalServices = SvnEdgeBonjourClient.getServiceTypes().length;
        Map<Integer, SvnEdgeServiceType> services =
                new HashMap<Integer, SvnEdgeServiceType>(totalServices);
        for (SvnEdgeServiceType serviceType : SvnEdgeServiceType.values()) {
            services.put(++i, serviceType);
        }

        String selected = null;
        SvnEdgeServiceType selectedType = null;
        do {
            for (Integer index : services.keySet()) {
                System.out.println("%% [" + (index) + "]: " +
                                   services.get(index));
            }
            System.out.print("Which service you wanna observe? ");
            InputStreamReader converter = new InputStreamReader(System.in);
            BufferedReader input = new BufferedReader(converter);
            selected = input.readLine();
            if (selected.equalsIgnoreCase("q")) {
                System.out.println("Thanks for using the SvnEdge API Client");
                System.exit(0);
            }
            try {
                int indexSelected = Integer.parseInt(selected);
                if (indexSelected > i || indexSelected < 1) {
                    System.err.println("You need to make a selection " +
                                       "between 1 and " + i);
                } else {
                    selectedType = services.get(indexSelected);
                }

            } catch (NumberFormatException e) {
                System.err.println("You can only enter numeric values " +
                                   "between 1 and " + i);
            }
        } while (selectedType == null);

        // creating the client with the selected service type.
        SvnEdgeBonjourClient client = SvnEdgeBonjourClient.makeInstance(
                selectedIp, selectedType);

        final BlockingQueue<SvnEdgeServerInfo> receivedEvents = 
            new LinkedBlockingQueue<SvnEdgeServerInfo>();

        // adding an observer for the selected service type.
        client.addServersListener(new SvnEdgeServersListener() {
            public void csvnServerStopped(SvnEdgeServerInfo serverInfo) {
                receivedEvents.offer(serverInfo);
            }

            public void csvnServerIsRunning(SvnEdgeServerInfo serverInfo) {
                receivedEvents.offer(serverInfo);
            }
        });

        // just wait for the packets
        while (true) {
            System.out.println();
            System.out.println("###> Observing from IP addresses ");
            System.out.println("###> Waiting for packets...");
            try {
                SvnEdgeServerInfo svnedgeServerInfo = receivedEvents.take();
                switch(svnedgeServerInfo.getEvent()) {
                case SERVER_RUNNING:
                    System.out.println("#### Found Server running ####");
                    System.out.println(svnedgeServerInfo);
                    break;
                case SERVER_SHUTDOWN:
                    System.out.println("#### Server stopped ####");
                    System.out.println(svnedgeServerInfo);
                    break;
                }

            } catch (InterruptedException packetWasReceived) {
                packetWasReceived.printStackTrace();
            }
        }
    }
}
