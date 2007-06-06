
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

import net.jxta.document.AdvertisementFactory;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

import java.io.IOException;

/**
 *
 * @author james todd [gonzo at jxta dot org]
 */

public class Share {
    
    private static final String GROUP_NAME = "MyGroup";
    private static final String PIPE_NAME = "MyPipe";
    
    private Search search = null;
    private Connect connection = null;
    
    public static void main(String[] args) {
        Share s = new Share();
        
        s.process();
    }
    
    public Share() {
    }
    
    public void publishGroup(String name) {
        publishGroup(name, null);
    }
    
    // xxx: implement pwd protected groups
    public void publishGroup(String name, String password) {
        name = name != null ? name.trim() : null;
        
        if (name != null &&
                name.length() > 0) {
            ModuleImplAdvertisement ma = null;
            
            try {
                ma = this.connection.getNetPeerGroup().getAllPurposePeerGroupImplAdvertisement();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            if (ma != null) {
                try {
                    this.connection.getNetPeerGroup().getDiscoveryService().publish(ma);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                
                this.connection.getNetPeerGroup().getDiscoveryService().remotePublish(ma);
                
                PeerGroupAdvertisement pga =
                        (PeerGroupAdvertisement)AdvertisementFactory.newAdvertisement(
                        PeerGroupAdvertisement.getAdvertisementType());
                
                pga.setPeerGroupID(IDFactory.newPeerGroupID());
                pga.setName(name);
                pga.setDescription("all your base are belong to us");
                pga.setModuleSpecID(ma.getModuleSpecID());
                
                System.out.println("publishing group: " + name + "/"  +
                        pga.getPeerGroupID().toString());
                
                try {
                    this.connection.getNetPeerGroup().getDiscoveryService().publish(pga);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                
                this.connection.getNetPeerGroup().getDiscoveryService().remotePublish(pga);
            }
        }
    }
    
    public void publishPipe(String name) {
        name = name != null ? name.trim() : null;
        
        if (name != null &&
                name.length() > 0) {
            PipeAdvertisement pa = (PipeAdvertisement)AdvertisementFactory.
                    newAdvertisement(PipeAdvertisement.getAdvertisementType());
            
            pa.setPipeID((PipeID)IDFactory.newPipeID(this.connection.getNetPeerGroup().getPeerGroupID()));
            pa.setName(name);
            pa.setType(PipeService.PropagateType);
            
            System.out.println("publishing pipe: " + name + "/" + pa.getPipeID().toString());
            
            try {
                this.connection.getNetPeerGroup().getDiscoveryService().publish(pa);
            } catch (IOException ioe) {
            }
            
            this.connection.getNetPeerGroup().getDiscoveryService().remotePublish(pa);
        }
    }
        
    protected void process() {
        configure();
        
        System.out.println("waiting for connection");
        
        while (! this.connection.isConnected()) {
            try {
                Thread.sleep(Search.WAIT_INTERVAL);
            } catch (InterruptedException ie) {}
        }
        
        this.search.discoverGroups(true);
        
        publishGroup(GROUP_NAME);
        
        this.search.discoverGroups(true);
        
        this.search.discoverPipes(true);
        
        publishPipe(PIPE_NAME);
        
        this.search.discoverPipes(true);
    }
    
    protected void configure() {
        this.search = new Search();
        
        this.search.configure();
        this.connection = this.search.getConnection();
    }
}
