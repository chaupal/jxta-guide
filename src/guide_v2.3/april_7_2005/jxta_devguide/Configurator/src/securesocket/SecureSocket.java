
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

import net.jxta.endpoint.MessageTransport;
import net.jxta.document.AdvertisementFactory;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaServerSocket;
import net.jxta.socket.JxtaSocket;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author james todd [gonzo at jxta dot org]
 */

public class SecureSocket {
    
    public static final int WAIT_INTERVAL = 50;
    
    protected static final String PING = "ping";
    protected static final String PONG = "pong";
    
    private static final String PIPE_NAME = "JXTA HOL :: SecureSocket";
    private static final String PIPE_TYPE = PipeService.UnicastType;
//    private static final String PIPE_TYPE = PipeService.UnicastSecureType;
    private static final String JXTA_TLS_TRANSPORT = "jxtatls";
    
    private Connect connection = null;
    private JxtaAuthenticator authenticator = null;
    private boolean isServerReady = false;
    
    public static void main(String[] args) {
        SecureSocket s = new SecureSocket();
        
        s.process();
    }
        
    protected void process() {
        configure();
        
        while (! this.connection.isConnected()) {
            try {
                Thread.sleep(WAIT_INTERVAL);
            } catch (InterruptedException ie) {
            }
        }
        
        JxtaAuthenticator a = new JxtaAuthenticator();
        PeerGroup npg = this.connection.getNetPeerGroup();
        PeerGroup cpg = getTlsPeerGroup(npg);
        
        if (cpg != null) {
            if (! a.isAuthenticated(cpg)) {
                System.out.println("authenticating against: " +
                    cpg.getPeerGroupName());
            
                a.authenticate(cpg);
            }
        
            System.out.println("is authenticated: " +
                a.isAuthenticated(cpg));
            
            PipeAdvertisement pa = generatePipeAdvertisement(npg);
            
            importCertificate();
            initiateServer(npg, pa);
            
            while (! isServerReady) {
                try {
                    Thread.sleep(WAIT_INTERVAL);
                } catch (InterruptedException ie) {
                }
            }
            
            initiateSocket(npg, pa);
        } else {
            System.out.println("uable to find tls group");
        }
    }
    
    protected void configure() {
        this.connection = new Connect();
        
        this.connection.process();
    }
    
    private PeerGroup getTlsPeerGroup(PeerGroup pg) {
        MessageTransport tls =
            pg.getEndpointService().getMessageTransport(JXTA_TLS_TRANSPORT);
            
        return tls != null ? tls.getEndpointService().getGroup() : null;
    }
    
    private PipeAdvertisement generatePipeAdvertisement(PeerGroup pg) {
        PipeAdvertisement pa = (PipeAdvertisement)AdvertisementFactory.
            newAdvertisement(PipeAdvertisement.getAdvertisementType());
        
        pa.setPipeID((PipeID)IDFactory.newPipeID(pg.getPeerGroupID()));
        pa.setName(PIPE_NAME);
        pa.setType(PIPE_TYPE);
        
        return pa;
    }
    
    // xxx: wip
    private void importCertificate() {
    }
    
    private void initiateServer(final PeerGroup pg,
        final PipeAdvertisement padv) {
        new Thread(new Runnable() {
            public void run() {
                ServerSocket ss = null;

                try {
                    ss = new JxtaServerSocket(pg, padv);
                } catch (IOException ioe) {
                }

                while (ss != null) {
                    isServerReady = true;
                    
                    try {
                        handle(ss.accept());
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        }).start();
    }
    
    private void initiateSocket(PeerGroup pg, PipeAdvertisement padv) {
        try{
            Socket s = new JxtaSocket(pg, padv);

            out(s.getOutputStream(), PING, "client");
            in(s.getInputStream(), PONG, "client");
                        
            s.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    private void in(InputStream is, String msg, String prefix)
    throws IOException {
        if (msg != null) {
            int c = -1;
            StringBuffer sb = new StringBuffer();

            while ((c = is.read()) > -1) {
                sb.append((char)c);

                if (sb.toString().indexOf(msg) > -1) {
                    System.out.println(prefix + " read: " + msg);
                    
                    break;
                }
            }
        }
    }
    
    private void out(OutputStream os, String msg, String prefix)
    throws IOException {
        if (msg != null) {
            System.out.println(prefix + " write: " + msg);

            os.write(msg.getBytes());
            os.flush();
        }
    }
    
    private void handle(final Socket s) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    in(s.getInputStream(), PING, "server");
                    out(s.getOutputStream(), PONG, "server");

                    s.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }).start();
    }
}