/*
 *  Copyright (c) 2001 Sun Microsystems, Inc.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Sun Microsystems, Inc. for Project JXTA."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 *  not be used to endorse or promote products derived from this
 *  software without prior written permission. For written
 *  permission, please contact Project JXTA at http://www.jxta.org.
 *
 *  5. Products derived from this software may not be called "JXTA",
 *  nor may "JXTA" appear in their name, without prior written
 *  permission of Sun.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  =========================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of Project JXTA.  For more
 *  information on Project JXTA, please see
 *  <http://www.jxta.org/>.
 *
 *  This license is based on the BSD license adopted by the Apache Foundation.
 *
 *  $Id: JxtaSocketExample.java,v 1.1 2005/11/22 04:27:22 raygao Exp $
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.exception.PeerGroupException;
import net.jxta.impl.membership.pse.StringAuthenticator;
import net.jxta.impl.protocol.PlatformConfig;
import net.jxta.membership.InteractiveAuthenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaSocket;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;
import net.jxta.rendezvous.RendezVousService;


/**
 *  This tutorial illustrates the use JxtaSocket. It attempts to bind a
 *  JxtaSocket to an instance of JxtaServerSocket bound socket.adv. Once a
 *  connection is established, it reads in expected data from the remote side,
 *  and then sends 1824 64K chunks and measures data rate achieved
 *
 */

public class JxtaSocketExample implements RendezvousListener {

    private transient PeerGroup netPeerGroup = null;
    private transient PipeAdvertisement pipeAdv;
    private transient JxtaSocket socket;
    // number of iterations to send the payload
    private static int ITERATIONS = 1824;
    // payload size
    private static int payloadSize = 64 * 1024;
    private RendezVousService rendezvous;
    private boolean waitForRendezvous = false;
    private String rendezvousLock = "Rendezvous Lock";

    /**
     *  Interact with the server.
     *
     *@exception  IOException  if an io exception occurs
     */
    public void run() throws IOException {

        int bufsize = 1024;
        if (waitForRendezvous && !rendezvous.isConnectedToRendezVous()) {
            System.out.println("Waiting for Rendezvous Connection");
            try {
                synchronized(rendezvousLock) {
                    rendezvousLock.wait();
                }
            } catch (InterruptedException e) {
            }
        }
        System.out.println("Connecting to the server");
        socket = new JxtaSocket(netPeerGroup,
                                //no specific peerid
                                null,
                                pipeAdv,
                                //general TO: 30 seconds
                                30000,
                                // reliable connection
                                true);

        // Set buffer size to payload size
        socket.setOutputStreamBufferSize(65536);

        // The server initiates communication by sending a small data packet
        // and then awaits data from the client
        System.out.println("Reading in data");
        InputStream in = socket.getInputStream();
        byte[] inbuf = new byte[bufsize];
        int read = in.read(inbuf, 0, bufsize);
        System.out.println("received " + read + " bytes");

        // Server is awaiting this data
        // Send data and time it.
        System.out.println("Sending back " + payloadSize + " * " + ITERATIONS + " bytes");
        OutputStream out = socket.getOutputStream();
        byte[] payload = new byte[payloadSize];
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            out.write(payload, 0, payloadSize);
        }
        out.flush();
        // include close in timing since it may need to flush the
        // tail end of the stream.
        socket.close();
        long t1 = System.currentTimeMillis();
        System.out.println("Completed in :" + (t1 - t0) + " msec");
        System.out.println("Data Rate :" + ((long) 64 * ITERATIONS * 8000) / (t1 - t0) + " Kbit/sec");
    }


    /**
     *  Starts the NetPeerGroup, and logs in
     *
     *@exception  PeerGroupException  Description of the Exception
     */
    private void startJxta() throws PeerGroupException {
        System.setProperty("net.jxta.tls.principal", "client");
        System.setProperty("net.jxta.tls.password", "password");
        System.setProperty("JXTA_HOME", System.getProperty("JXTA_HOME", "client"));
        File home = new File(System.getProperty("JXTA_HOME", "client"));
        if (!configured(home)) {
            createConfig(home, "JxtaSocketExample", false);
        }

        // create, and Start the default jxta NetPeerGroup
        netPeerGroup = PeerGroupFactory.newNetPeerGroup();
        if (waitForRendezvous) {
            rendezvous = netPeerGroup.getRendezVousService();
            rendezvous.addListener(this);
        }
    }

    /**
     *  rendezvousEvent the rendezvous event
     *
     *@param  event   rendezvousEvent
     */
    public void rendezvousEvent(RendezvousEvent event) {
        if (event.getType() == event.RDVCONNECT ||
            event.getType() == event.RDVRECONNECT) {
            synchronized(rendezvousLock) {
                rendezvousLock.notify();
            }
        }
        }


    /**
     *  Establishes credentials with the specified peer group
     *
     *@param  group      PeerGroup
     *@param  principal  Principal
     *@param  password   password
     */
    public static void login(PeerGroup group, String principal, String password) {
        try {
            StringAuthenticator auth = null;
            MembershipService membership = group.getMembershipService();
            Credential cred = membership.getDefaultCredential();
            if (cred == null) {
                AuthenticationCredential authCred = new AuthenticationCredential(group, "StringAuthentication", null);
                try {
                    auth = (StringAuthenticator) membership.apply(authCred);
                } catch (Exception failed) {
                    ;
                }

                if (auth != null) {
                    auth.setAuth1_KeyStorePassword(password.toCharArray());
                    auth.setAuth2Identity(group.getPeerID());
                    auth.setAuth3_IdentityPassword(principal.toCharArray());
                    if (auth.isReadyForJoin()) {
                        membership.join(auth);
                    }
                }
            }

            cred = membership.getDefaultCredential();
            if (null == cred) {
                AuthenticationCredential authCred = new AuthenticationCredential(group, "InteractiveAuthentication", null);

                InteractiveAuthenticator iAuth = (InteractiveAuthenticator) membership.apply(authCred);

                if (iAuth.interact() && iAuth.isReadyForJoin()) {
                    membership.join(iAuth);
                }
            }
        } catch (Throwable e) {
            System.out.flush();
            // make sure output buffering doesn't wreck console display.
            System.err.println("Uncaught Throwable caught by 'main':");
            e.printStackTrace();
            System.exit(1);
            // make note that we abended
        }
        finally {
            System.err.flush();
            System.out.flush();
        }
    }

    /**
     *  returns a resource InputStream
     *
     *@param  resource         resource name
     *@return                  returns a resource InputStream
     *@exception  IOException  if an I/O error occurs
     */
    protected static InputStream getResourceInputStream(String resource) throws IOException {
        ClassLoader cl = JxtaSocketExample.class.getClassLoader();
        return cl.getResourceAsStream(resource);
    }
    /**
     *  Returns true if the node has been configured, otherwise false
     *
     *@param  home  node jxta home directory
     *@return       true if home/PlatformConfig exists
     */
    protected static boolean configured(File home) {
        File platformConfig = new File(home, "PlatformConfig");
        return platformConfig.exists();
    }
    /**
     *  Creates a PlatformConfig with peer name set to name
     *
     *@param  home  node jxta home directory
     *@param  name  node given name (can be hostname)
     */
    protected static void createConfig(File home, String name, boolean server) {
        try {
            String fname = null;
            if (server) {
                fname = "ServerPlatformConfig.master";
            } else {
                fname = "PlatformConfig.master";
            }
            InputStream is = getResourceInputStream(fname);
            home.mkdirs();
            PlatformConfig platformConfig = (PlatformConfig) AdvertisementFactory.newAdvertisement(MimeMediaType.XMLUTF8, is);
            is.close();
            platformConfig.setName(name);
            File newConfig = new File(home, "PlatformConfig");
            OutputStream op = new FileOutputStream(newConfig);
            StructuredDocument doc = (StructuredDocument) platformConfig.getDocument(MimeMediaType.XMLUTF8);
            doc.sendToStream(op);
            op.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     *  If the java property RDVWAIT set to true then this demo
     *  will wait until a rendezvous connection is established before 
     *  initiating a connection
     *
     *@param  args  none recognized.
     */
    public static void main(String args[]) {

        try {
            JxtaSocketExample socEx = new JxtaSocketExample();
            String value = System.getProperty("RDVWAIT", "false");
            socEx.waitForRendezvous = Boolean.valueOf(value).booleanValue();
            System.out.println("Starting JXTA");
            socEx.startJxta();
            System.out.println("reading in socket.adv");
            FileInputStream is = new FileInputStream("socket.adv");
            socEx.pipeAdv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(MimeMediaType.XMLUTF8, is);
            is.close();
            // run it once
            socEx.run();
            // run it again, to exclude any object initialization overhead
            socEx.run();
        } catch (Throwable e) {
            System.out.println("failed : " + e);
            e.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);
    }
}

