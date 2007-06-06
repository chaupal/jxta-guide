
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

import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.rendezvous.RendezVousService;
import net.jxta.rendezvous.RendezvousListener;
import net.jxta.rendezvous.RendezvousEvent;

import net.jxta.ext.config.AbstractConfigurator;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author james todd [gonzo at jxta dot org]
 */

public class Connect
        implements RendezvousListener {
    
    private static final int WAIT = 0;
    private static final int INTERVAL = 2 * 1000;
    
    private PeerGroup netPeerGroup = null;
    private RendezVousService rendezVous = null;
    private Timer timer = null;
    
    public static void main(String[] args) {
        Connect c = new Connect();
        
        c.process();
    }
    
    public Connect() {
        AbstractConfigurator.register(JxtaConfigurator.class);
        
        this.timer = getTimer(WAIT, INTERVAL, true);
    }
    
    public PeerGroup getNetPeerGroup() {
        return this.netPeerGroup;
    }
    
    public  boolean isConnected() {
        return this.rendezVous != null &&
                (this.rendezVous.isConnectedToRendezVous() ||
                this.rendezVous.isRendezVous());
    }
    
    public void rendezvousEvent(RendezvousEvent re) {
        String s = null;
        
        switch (re.getType()) {
            case RendezvousEvent.BECAMEEDGE:
                s = "became edge";
                break;
            case RendezvousEvent.BECAMERDV:
                s = "became rdv";
                break;
            case RendezvousEvent.CLIENTCONNECT:
                s = "client connect";
                break;
            case RendezvousEvent.CLIENTDISCONNECT:
                s = "client disconnect";
                break;
            case RendezvousEvent.CLIENTFAILED:
                s = "client failure";
                break;
            case RendezvousEvent.CLIENTRECONNECT:
                s = "client reconnect";
                break;
            case RendezvousEvent.RDVCONNECT:
                s = "rendezVous connect";
                break;
            case RendezvousEvent.RDVDISCONNECT:
                s = "rendezVous disconnect";
                break;
            case RendezvousEvent.RDVFAILED:
                s = "rendezVous faiure";
                break;
            case RendezvousEvent.RDVRECONNECT:
                s = "rendezVous reconnect";
                break;
            default:
                s = "unknown";
                break;
        }
        
        System.out.println("  event: " + s);
    }
        
    protected void process() {
        System.out.println("creating NPG");
        
        try {
            this.netPeerGroup = PeerGroupFactory.newNetPeerGroup();
        } catch (PeerGroupException pge) {
            pge.printStackTrace();
        }
        
        if (this.netPeerGroup != null) {
            this.rendezVous = this.netPeerGroup.getRendezVousService();
            
            if (this.rendezVous != null) {
                System.out.println("adding listener");
                
                this.rendezVous.addListener(this);
            }
        }
    }
    
    protected void configure() {
    }
    
    private Timer getTimer(int wait, final int interval, final boolean throttle) {
        Timer t = new Timer(true);
        
        t.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                System.out.println("connected: " + isConnected());
                
                if (throttle &&
                        isConnected()) {
                    throttleBack();
                }
            }
        }
        , wait, interval);
        
        return t;
    }
    
    private void throttleBack() {
        this.timer.cancel();
        
        this.timer = getTimer(WAIT, INTERVAL * 5, false);
    }
}
