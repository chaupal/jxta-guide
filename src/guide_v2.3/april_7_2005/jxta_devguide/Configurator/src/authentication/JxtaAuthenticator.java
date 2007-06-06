
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

import net.jxta.credential.AuthenticationCredential;
import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ProtocolNotSupportedException;
import net.jxta.impl.membership.pse.StringAuthenticator;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;

/**
 *
 * @author james todd [gonzo at jxta dot org]
 */

public class JxtaAuthenticator {
    
    public static final int WAIT_INTERVAL = 50;
    
    private static final String AUTHENTICATION = "StringAuthentication";
    
    private Connect connection = null;
    
    public static void main(String[] args) {
        JxtaAuthenticator a = new JxtaAuthenticator();
        
        a.process();
    }
    
    public JxtaAuthenticator() {
    }
    
    public boolean isAuthenticated(PeerGroup pg) {
        boolean isAuthenticated = false;
        
        try {
            isAuthenticated =
                (pg.getMembershipService().getDefaultCredential() != null);
        } catch (PeerGroupException pge) {
        }
        
        return isAuthenticated;
    }
    
    public boolean authenticate(PeerGroup pg) {
        if (! isAuthenticated(pg)) {
            MembershipService ms = pg.getMembershipService();
            AuthenticationCredential ac = new AuthenticationCredential(pg,
                AUTHENTICATION, null);
            Authenticator a = null;
            
            try {
                a = ms.apply(ac);
            } catch (ProtocolNotSupportedException pnse) {
            } catch (PeerGroupException pge) {
            }
            
            if (a == null) {
                try {
                    a = ms.apply(ac);
                } catch (ProtocolNotSupportedException pnse) {
                } catch (PeerGroupException pge) {
                }
            }

            ((StringAuthenticator)a).setAuth1_KeyStorePassword(JxtaConfigurator.PASSWORD);
            ((StringAuthenticator)a).setAuth2Identity(pg.getPeerID());
            ((StringAuthenticator)a).setAuth3_IdentityPassword(JxtaConfigurator.PASSWORD);
            
            if (a != null &&
                a.isReadyForJoin()) {
                try {
                    ms.join(a);
                } catch (PeerGroupException pge) {
                    pge.printStackTrace();
                }
            }
        }
        
        return isAuthenticated(pg);
    }
        
    protected void process() {
        configure();
        
        System.out.println("waiting for connection");
        
        while (! this.connection.isConnected()) {
            try {
                Thread.sleep(WAIT_INTERVAL);
            } catch (InterruptedException ie) {
            }
        }
        
        boolean isAuthenticated = isAuthenticated(this.connection.getNetPeerGroup());
        
        System.out.println("is authenticated[" +
            this.connection.getNetPeerGroup().getPeerGroupName() + "]: " +
            isAuthenticated(this.connection.getNetPeerGroup()));
        
        if (! isAuthenticated) {
            authenticate(this.connection.getNetPeerGroup());
        }
        
        System.out.println("is authenticated[" +
            this.connection.getNetPeerGroup().getPeerGroupName() + "]: " +
            isAuthenticated(this.connection.getNetPeerGroup()));
    }
    
    protected void configure() {
        this.connection = new Connect();
        
        this.connection.process();
    }
}
