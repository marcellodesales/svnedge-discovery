package com.collabnet.svnedge.discovery.mdns;

/**
 * Defines the event that was captured by jmDNS.
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 *
 */
public enum SvnEdgeServerEvent {

    /**
     * When the server has started.
     */
    SERVER_RUNNING,
    /**
     * When the server has just stopped.
     */
    SERVER_SHUTDOWN
}
