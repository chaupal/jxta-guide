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
 *  $Id: JxtaBidiPipeExample.java,v 1.1 2005/11/22 04:27:17 raygao Exp $
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.util.Date;
import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.Messenger;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.exception.PeerGroupException;
import net.jxta.impl.membership.pse.StringAuthenticator;
import net.jxta.membership.InteractiveAuthenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.util.JxtaBiDiPipe;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;
import net.jxta.rendezvous.RendezVousService;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *  This example illustrates how to utilize the JxtaBiDiPipe Reads in pipe.adv
 *  and attempts to bind to a JxtaServerPipe
 */

public class JxtaBidiPipeExample implements PipeMsgListener, RendezvousListener {

    private PeerGroup netPeerGroup = null;
    private PipeAdvertisement pipeAdv;
    private JxtaBiDiPipe pipe;
    private RendezVousService rendezvous;
    private final static String SenderMessage = "pipe_tutorial";
    private final static String completeLock = "completeLock";
    private int count = 0;

    private final static Logger LOG = Logger.getLogger(JxtaBidiPipeExample.class.getName());

    /**
     *  Starts jxta
     */
    private void startJxta() {
        try {
            System.setProperty("net.jxta.tls.principal", "client");
            System.setProperty("net.jxta.tls.password", "password");
            System.setProperty("JXTA_HOME", System.getProperty("JXTA_HOME", "client"));
            File home = new File(System.getProperty("JXTA_HOME", "client"));
            if (!JxtaServerPipeExample.configured(home)) {
                JxtaServerPipeExample.createConfig(home, "JxtaBidiPipeExample", false);
            }

            // create, and Start the default jxta NetPeerGroup
            netPeerGroup = PeerGroupFactory.newNetPeerGroup();
            rendezvous = netPeerGroup.getRendezVousService();
            login(netPeerGroup, "client", "password");
            netPeerGroup.startApp(null);
        } catch (PeerGroupException e) {
            // could not instantiate the group, print the stack and exit
            System.out.println("fatal error : group creation failure");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void login(PeerGroup group, String principal, String password) {
        try {
            StringAuthenticator auth = null;
            MembershipService membership = group.getMembershipService();
            Credential cred = membership.getDefaultCredential();
            if (cred == null) {
                AuthenticationCredential authCred = new AuthenticationCredential(group, "StringAuthentication", null);
                try {
                    auth = (StringAuthenticator) membership.apply(authCred);
                } catch(Exception failed) {
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
        } catch(Throwable e) {
            System.out.flush(); // make sure output buffering doesn't wreck console display.
            System.err.println("Uncaught Throwable caught by 'main':");
            e.printStackTrace();
            System.exit(1);         // make note that we abended
        } finally {
            System.err.flush();
            System.out.flush();
        }
    }
    /**
     *  when we get a message, print out the message on the console
     *
     *@param  event  message event
     */
    public void pipeMsgEvent(PipeMsgEvent event) {

        Message msg = null;
        try {
            // grab the message from the event
            msg = event.getMessage();
            if (msg == null) {
                if (LOG.isEnabledFor(Level.DEBUG)) {
                    LOG.debug("Received an empty message, returning");
                }
                return;
            }
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Received a response");
            }
            // get the message element named SenderMessage
            MessageElement msgElement = msg.getMessageElement(SenderMessage, SenderMessage);
            // Get message
            if (msgElement.toString() == null) {
                System.out.println("null msg received");
            } else {
                Date date = new Date(System.currentTimeMillis());
                System.out.println("Message  :"+ msgElement.toString());
                count ++;
            }
            if (count >= JxtaServerPipeExample.ITERATIONS) {
                synchronized(completeLock) {
                    completeLock.notify();
                }
            }
        } catch (Exception e) {
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug(e);
            }
            return;
        }
    }
    /**
     *  rendezvousEvent the rendezvous event
     *
     *@param  event   rendezvousEvent
     */
    public synchronized void rendezvousEvent(RendezvousEvent event) {
        if (event.getType() == event.RDVCONNECT ||
            event.getType() == event.RDVRECONNECT ) {
            notify();
        }
    }
    /**
     * awaits a rendezvous connection
     */
    private synchronized void waitForRendezvousConncection() {
        if (!rendezvous.isConnectedToRendezVous()) {
            System.out.println("Waiting for Rendezvous Connection");
            try {
                wait();
                System.out.println("Connected to Rendezvous");
            } catch (InterruptedException e) {
                // got our notification
            }
        }
    }

    private void waitUntilCompleted() {
        try {
            synchronized(completeLock) {
                completeLock.wait();
            }
            System.out.println("Done.");
        } catch (InterruptedException e) {
            System.out.println("Interrupted.");
        }
    }

    /**
     *  main
     *
     *@param  args  command line args
     */
    public static void main(String args[]) {

        JxtaBidiPipeExample eg = new JxtaBidiPipeExample();
        eg.startJxta();
        System.out.println("reading in pipe.adv");
        try {
            FileInputStream is = new FileInputStream("pipe.adv");
            eg.pipeAdv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(MimeMediaType.XMLUTF8, is);
            is.close();
            System.out.println("creating the BiDi pipe");
            eg.pipe = new JxtaBiDiPipe();
            eg.pipe.setReliable(true);
            eg.waitForRendezvousConncection();
            System.out.println("Attempting to establish a connection");
            eg.pipe.connect(eg.netPeerGroup,
                            null,
                            eg.pipeAdv,
                            180000,
                            // register as a message listener
                            eg);
            //at this point we need to keep references around until data xchange
            //is complete
            eg.waitUntilCompleted();
            System.exit(0);
        } catch (Exception e) {
            System.out.println("failed to bind the JxtaBiDiPipe due to the following exception");
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
}

