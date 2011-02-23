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
import java.util.Set;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.apache.log4j.Logger;

import com.collabnet.svnedge.discovery.mdns.SvnEdgeCsvnServiceKey;
import com.collabnet.svnedge.discovery.mdns.SvnEdgeHttpServiceKey;
import com.collabnet.svnedge.discovery.mdns.SvnEdgeServiceKey;
import com.collabnet.svnedge.discovery.mdns.SvnEdgeServiceType;

/**
 * Publishes the services using the jmDSN framework to publish multicast packets
 * for DNS services. It follows the Bonjour protocol.
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 *
 */
public final class SvnEdgeBonjourRegister {

    private static final Logger log = Logger.getLogger(SvnEdgeBonjourRegister.class);
    /**
     * The name of the bonjour service published by SvnEdge servers.
     */
    public static final String SERVICE_NAME = "collabnetsvn";
    /**
     * the private instance of the jmDNS.
     */
    private JmDNS jmdns;
    /**
     * The jmDNS service info acquired during the registration. This is needed
     * to be used to handle the unregistration process.
     */
    private ServiceInfo serverRegistrationInfo;

    /**
     * Singleton Constructor.
     * @throws IOException 
     */
    private SvnEdgeBonjourRegister(InetAddress ipAddress) throws IOException {
        log.debug("Registering the SvnEdge server at " + 
                ipAddress.getHostAddress());
        this.jmdns = JmDNS.create(ipAddress);
    }

    /**
     * @param ipAddress is the given ipAddress with to publish the jmDNS 
     * services. In case the ipAddress is site-local, it throws the IOException.
     * @return the singleton instance of this class for publishing services.
     * @throws IOException in case the given ipAddress is site-local.
     */
    public static SvnEdgeBonjourRegister getInstance(InetAddress ipAddress) 
            throws IOException {
        return new SvnEdgeBonjourRegister(ipAddress);
    }

    /**
     * Non-blocking method call to publish the given service type as 
     * {@link SvnEdgeBonjourRegister.SERVICE_NAME}. The service will be
     * bound to the given ipAddress and port number, which will be associated 
     * with the given number of parameters.
     * Once the method returns, jmDNS will be broadcasting multicast packets
     * with the service described above.
     * @param ipAddress is the ipAddress selected.
     * @param port is the given port number of the service.
     * @param serviceName is the name of the service.
     * @param type is the service type.
     * @param params is the map of keys and values for the service.
     * @throws IllegalArgumentException in case the given params do not contain
     * a required key for the given service type.
     * @throws IOException in case any failure to start the service occurs
     * with the jmDSN service. It usually happens if there is no Internet
     * connectivity.
     */
    public void registerService(int port, SvnEdgeServiceType type, 
                Map<SvnEdgeServiceKey, String> params) throws 
                        IllegalArgumentException, IOException {

        Map<String, String> serviceParams = getValidParams(type, params);

        final String serviceType = type.toString();

        log.debug("Registering service type " + type);
        if (type.equals(SvnEdgeServiceType.CSVN)) {
            // register _csvn._tcp.local, with priority = weight = 0
            serverRegistrationInfo = ServiceInfo.create(serviceType, SERVICE_NAME,
                    port, 0, 0, serviceParams);

        } else if (type.equals(SvnEdgeServiceType.HTTP)) {
            //register _http._tcp.local
            String serviceVal = SvnEdgeHttpServiceKey.PATH.toString() + "=" + 
                serviceParams.get(SvnEdgeHttpServiceKey.PATH.toString());
            serverRegistrationInfo = ServiceInfo.create(serviceType, SERVICE_NAME, 
                    port, serviceVal);
        }

        //register _csvn._tcp.local
        jmdns.registerService(serverRegistrationInfo);
        log.debug("Registered the server: " + serverRegistrationInfo);
    }

    /**
     * @param type is the service type.
     * @param params is the provided parameters.
     * @return the updated props map for the jmDNS service.
     * @throws IllegalArgumentException in case at least one required key
     * for the given service type is not provided.
     */
    private Map<String, String> getValidParams(SvnEdgeServiceType type,
                           Map<SvnEdgeServiceKey, String> params) 
        throws IllegalArgumentException {

        Map<String, String> updatedProps = new HashMap<String, String>(
                params.size());
        Set<SvnEdgeServiceKey> providedKeys = params.keySet();
        for (SvnEdgeServiceKey requiredKey : type.getRequiredKeys()) {
            if (!providedKeys.contains(requiredKey)) {
                throw new IllegalArgumentException("The parameter " + 
                        requiredKey +" is require by the service type " +type);
            }
            updatedProps.put(requiredKey.toString(), params.get(requiredKey));
        }
        return updatedProps;
    }

    /**
     * Closing the connection with the mDNS service.
     * @throws IOException in case the close operation fails.
     * 
     * @see javax.jmdns.JmDNS#close()
     */
    public void close() throws IOException {
        log.debug("Unregistering this server...");
        if (jmdns != null) {
            jmdns.unregisterService(serverRegistrationInfo);
            jmdns.close();
            jmdns = null;
        }
    }

    /**
     * Unregister all the services CSVN and HTTP
     * 
     * @see javax.jmdns.JmDNS#unregisterAllServices()
     */
    public void unregisterServices() {
        jmdns.unregisterAllServices();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("%%%%%%%%% SvnEdge Bonjour Register %%%%%%%%%%%%%%");
        Enumeration<NetworkInterface> infs = 
            NetworkInterface.getNetworkInterfaces();
        Map<Integer, InetAddress> ipAddresses = 
            new HashMap<Integer, InetAddress>();
        int count = 0;
        while(infs.hasMoreElements()) {
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
            System.out.print("Which IP address you wanna use? ");
            InputStreamReader converter = new InputStreamReader(System.in);
            BufferedReader input = new BufferedReader(converter);
            String selected = input.readLine();
            if (selected.equalsIgnoreCase("q")) {
                System.out.println("Thanks for using the SvnEdge Discovery API");
                System.exit(0);
            } else {
                selectedIp = ipAddresses.get(Integer.valueOf(selected));
            }
        }

        int portNumber = -1;
        while (portNumber == -1) {
            System.out.print("Which port number? ");
            InputStreamReader converter = new InputStreamReader(System.in);
            BufferedReader input = new BufferedReader(converter);
            String selected = input.readLine();
            try {
                portNumber = Integer.parseInt(selected);
                if (portNumber < 1) {
                    portNumber = -1;
                    throw new Exception("The number must be greater than 0");
                }

            } catch (Exception nfe) {
                System.out.print(nfe.getMessage());
                continue;
            }
        }

        System.out.println("Registering the service at " + 
                selectedIp.getHostAddress() + ":" + portNumber);
        Map<SvnEdgeServiceKey, String> props = 
            new HashMap<SvnEdgeServiceKey, String>();
        props.put(SvnEdgeCsvnServiceKey.TEAMFORGE_PATH, "/integration");
        props.put(SvnEdgeCsvnServiceKey.CONTEXT_PATH, "/csvn");

        SvnEdgeBonjourRegister r = SvnEdgeBonjourRegister.getInstance(
                selectedIp);
        r.registerService(portNumber, SvnEdgeServiceType.CSVN, props);

        System.out.print("Stop? ");
        InputStreamReader converter = new InputStreamReader(System.in);
        BufferedReader input = new BufferedReader(converter);
        String selected = input.readLine();
        System.out.println("Stopping the service...");
        r.close();
        System.out.println("Service should be stopped...");
    }
    
}
