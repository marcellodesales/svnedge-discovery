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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

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
            } else {
                jmdns = JmDNS.create(address);
            }
        } else {
            jmdns = JmDNS.create();
        }
        jmdns.addServiceListener(type.toString(), this);
        this.observers = new ConcurrentLinkedQueue<SvnEdgeServersListener>();
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
     */
    public void stop() {
        jmdns.unregisterAllServices();
        jmdns.close();
        jmdns = null;
    }

    /**
     * Adds a new observer/listener to the service.
     * 
     * @param newLis
     *            is new observer interested in the events of any Subversion
     *            Edge server in the local network.
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

    private void debug(ServiceInfo info) {
        System.out.println("================ INFO =========");
        System.out.println("Nice: " + info.getNiceTextString());
        System.out.println("Hostname: " + info.getHostAddress());
        System.out.println("Name: " + info.getName());
        System.out.println("Type: " + info.getType());
        System.out.println("Qualified Name: " + info.getQualifiedName());
        System.out.println("Port: " + info.getPort());
        System.out.println("Priority: " + info.getPriority());
        System.out.println("Server: " + info.getServer());
        System.out.println("Text Strings: " + info.getTextString());
        System.out.println("URL: " + info.getURL());
        System.out.println("Weight: " + info.getWeight());
        System.out.println("Inetaddress: " + info.getAddress());
        System.out.println("Bytes: " + info.getTextBytes());

        Enumeration<String> props = info.getPropertyNames();
        while (props.hasMoreElements()) {
            String propName = props.nextElement();
            String propValue = info.getPropertyString(propName);
            System.out.println(propName + ": " + propValue);
        }
    }

    @Override
    public void serviceAdded(ServiceEvent addedEvent) {
        // when any bonjour client is added to the jmDNS client. Do nothing.
        this.notifyPacketArrival();
    }

    @Override
    public void serviceRemoved(ServiceEvent removedEvent) {
        SvnEdgeServerInfo serverInfo = SvnEdgeServerInfo.makeNew(removedEvent
                .getInfo());
        for (SvnEdgeServersListener observer : this.observers) {
            observer.csvnServerStopped(serverInfo);
        }
        // debug(removedEvent.getInfo());
        this.notifyPacketArrival();
    }

    @Override
    public void serviceResolved(ServiceEvent resolvedEvent) {
        SvnEdgeServerInfo serverInfo = SvnEdgeServerInfo.makeNew(resolvedEvent
                .getInfo());
        for (SvnEdgeServersListener observer : this.observers) {
            observer.csvnServerIsRunning(serverInfo);
        }
        // debug(resolvedEvent.getInfo());
        this.notifyPacketArrival();
    }

    private synchronized void waitForPacket() throws InterruptedException {
        this.wait();
    }

    private synchronized void notifyPacketArrival() {
        this.notifyAll();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("%%%%%%%%% SvnEdge Bonjour Client %%%%%%%%%%%%%%");
        Enumeration<NetworkInterface> infs = NetworkInterface.getNetworkInterfaces();
        Map<Integer, InetAddress> ipAddresses = new HashMap<Integer, InetAddress>();
        int count = 0;
        while(infs.hasMoreElements()) {
            NetworkInterface inf = infs.nextElement();
            if (!inf.isLoopback()) {
                Enumeration<InetAddress> availableIps = inf.getInetAddresses();
                while (availableIps.hasMoreElements()) {
                    InetAddress ip = availableIps.nextElement();
                    if (ip instanceof Inet4Address) {
                        ipAddresses.put(++count, ip);
                    }
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
                System.out.println("%% [" + (index) + "]: "
                        + services.get(index));
            }
            System.out.print("Which service you wanna " + "observe? ");
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
                    System.err.println("You need to make a selection "
                            + "between 1 and " + i);
                } else {
                    selectedType = services.get(indexSelected);
                }

            } catch (NumberFormatException e) {
                System.err.println("You can only enter numeric values "
                        + "between 1 and " + i);
            }
        } while (selectedType == null);

        // creating the client with the selected service type.
        SvnEdgeBonjourClient client = SvnEdgeBonjourClient
                .makeInstance(selectedIp, selectedType);

        // adding an observer for the selected service type.
        client.addServersListener(new SvnEdgeServersListener() {
            @Override
            public void csvnServerStopped(SvnEdgeServerInfo serverInfo) {
                System.out.println("#### Server stopped ####");
                System.out.println(serverInfo);
                System.out.println();
            }

            @Override
            public void csvnServerIsRunning(SvnEdgeServerInfo serverInfo) {
                System.out.println("#### Found Server running ####");
                System.out.println(serverInfo);
                System.out.println();
            }
        });

        // just wait for the packets
        while (true) {
            System.out.println();
            System.out.println("###> Observing from IP addresses ");
            System.out.println("###> Waiting for packets...");
            try {
                client.waitForPacket();
            } catch (InterruptedException packetWasReceived) {
                // do nothing as a new packet was broadcasted
            }
        }
    }
}
