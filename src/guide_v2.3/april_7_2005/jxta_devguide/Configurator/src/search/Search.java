
/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 *
 * Redistributions in source code form must reproduce the above copyright
 * and this condition.
 *
 * The contents of this file are subject to the Sun Project JXTA License
 * Version 1.1 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.jxta.org/jxta_license.html.
 */

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

import java.util.Enumeration;

/**
 *
 * @author james todd [gonzo at jxta dot org]
 */

public class Search {
    
    public static final int WAIT_INTERVAL = 50;
    
    static final int MAX = 15;
    
    private Connect connection = null;
    
    public static void main(String[] args) {
        Search s = new Search();
        
        s.process();
    }
    
    public Search() {
    }
    
    public Connect getConnection() {
        return this.connection;
    }
    
    public void discoverGroups() {
        discoverGroups(false);
    }
    
    public void discoverGroups(boolean verbose) {
        System.out.println("discovering groups");
        
        new Searcher(this.connection.getNetPeerGroup().getDiscoveryService(),
            Searcher.GROUP).search(verbose);
    }
    
    public void discoverPeers() {
        discoverPeers(false);
    }
    
    public void discoverPeers(boolean verbose) {
        System.out.println("discovering peers");
        
        new Searcher(this.connection.getNetPeerGroup().getDiscoveryService(),
            Searcher.PEER).search(verbose);
    }
    
    public void discoverPipes() {
        discoverPipes(false);
    }
    
    public void discoverPipes(boolean verbose) {
        System.out.println("discovering pipes");
        
        new Searcher(this.connection.getNetPeerGroup().getDiscoveryService(),
            Searcher.PIPE).search(verbose);
    }
        
    protected void process() {
        configure();
        
        System.out.println("waiting for connection");
        
        while (! this.connection.isConnected()) {
            try {
                Thread.sleep(WAIT_INTERVAL);
            } catch (InterruptedException ie) {}
        }
        
        discoverGroups();
        discoverPeers();
        discoverPipes();
    }
    
    protected void configure() {
        this.connection = new Connect();
        
        this.connection.process();
    }
}

class Searcher {
    
    public static final int GROUP = DiscoveryService.GROUP;
    public static final int PEER = DiscoveryService.PEER;
    public static final int PIPE = DiscoveryService.ADV;
    
    private DiscoveryService discovery = null;
    private int type = 0;
    
    public Searcher(DiscoveryService discovery, int type) {
        this.discovery = discovery;
        this.type = type;
    }
    
    public void search() {
        search(false);
    }
    
    public void search(final boolean verbose) {
        this.discovery.getRemoteAdvertisements(null, type, null, null, Search.MAX,
                new DiscoveryListener() {
            public void discoveryEvent(DiscoveryEvent de) {
                DiscoveryResponseMsg rm = de.getResponse();
                PeerAdvertisement pa = rm.getPeerAdvertisement();
                String pn = pa != null ? pa.getName() : "unknown";
                String msg = null;
                
                for (Enumeration a = rm.getAdvertisements(); a.hasMoreElements(); ) {
                    Object o = a.nextElement();
                    
                    if (type == DiscoveryService.GROUP) {
                        if (o instanceof PeerGroupAdvertisement) {
                            msg = "group: " + ((PeerGroupAdvertisement)o).getName();
                            
                            if (verbose) {
                                msg += "/" +
                                        ((PeerGroupAdvertisement)o).getPeerGroupID().toString();
                            }
                        }
                    } else if (type == DiscoveryService.PEER) {
                        if (o instanceof PeerAdvertisement) {
                            msg = "peer: " + ((PeerAdvertisement)o).getName();
                            
                            if (verbose) {
                                msg += "/" +
                                        ((PeerAdvertisement)o).getPeerID().toString();
                            }
                        }
                    } else if (type == DiscoveryService.ADV) {
                        if (o instanceof PipeAdvertisement) {
                            msg = "pipe: " + ((PipeAdvertisement)o).getName();
                            
                            if (verbose) {
                                msg += "/" +
                                        ((PipeAdvertisement)o).getPipeID().toString();
                            }
                        }
                    }
                    
                    if (msg != null) {
                        System.out.println("  peer \"" + pn + "\" " +msg);
                    }
                }
            }
        });
    }
}
