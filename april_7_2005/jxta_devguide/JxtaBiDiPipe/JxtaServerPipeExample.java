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
 *  $Id: JxtaServerPipeExample.java,v 1.1 2005/11/22 04:27:18 raygao Exp $
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import net.jxta.membership.InteractiveAuthenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.util.JxtaBiDiPipe;
import net.jxta.util.JxtaServerPipe;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.impl.protocol.PlatformConfig;
import org.apache.log4j.Logger;


/**
 *  This example illustrates how to utilize the JxtaBiDiPipe Reads in pipe.adv
 *  and attempts to bind to a JxtaServerPipe
 */


public class JxtaServerPipeExample  {
    public static final int ITERATIONS = 100;
    private PeerGroup netPeerGroup = null;
    private PipeAdvertisement pipeAdv;
    private JxtaServerPipe serverPipe;
    private static final MimeMediaType MEDIA_TYPE = new MimeMediaType("application/bin");
    private final static Logger LOG = Logger.getLogger(JxtaServerPipeExample.class.getName());
    private final static String SenderMessage = "pipe_tutorial";

    /**
     *  main
     *
     * @param  args  command line args
     */
    public static void main(String args[]) {

        JxtaServerPipeExample eg = new JxtaServerPipeExample();
        eg.startJxta();
        System.out.println("Reading in pipe.adv");
        try {
            FileInputStream is = new FileInputStream("pipe.adv");
            eg.pipeAdv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(MimeMediaType.XMLUTF8, is);
            is.close();
            eg.serverPipe = new JxtaServerPipe(eg.netPeerGroup, eg.pipeAdv);
            // we want to block until a connection is established
            eg.serverPipe.setPipeTimeout(0);
        } catch (Exception e) {
            System.out.println("failed to bind to the JxtaServerPipe due to the following exception");
            e.printStackTrace();
            System.exit(-1);
        }
        // run on this thread
        eg.run();
    }


    private void sendTestMessages(JxtaBiDiPipe pipe) {
        try {
            for (int i =0; i<ITERATIONS; i++) {
            Message msg = new Message();
            String data = "Message #"+i;
            msg.addMessageElement(SenderMessage,
                                  new StringMessageElement(SenderMessage,
                                                           data,
                                                           null));
            System.out.println("Sending :"+data);
            pipe.sendMessage(msg);
            //Thread.sleep(100);
            }
        } catch (Exception ie) {
            ie.printStackTrace();
        }
    }
    /**
     * wait for msgs
     *
     */

    public void run() {

        System.out.println("Waiting for JxtaBidiPipe connections on JxtaServerPipe");
        while (true) {
            try {
                JxtaBiDiPipe bipipe = serverPipe.accept();
                if (bipipe != null ) {
                    System.out.println("JxtaBidiPipe accepted, sending 100 messages to the other end");
                    //Send a 100 messages
                    sendTestMessages(bipipe);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    /**
     * Starts jxta
     *
     */
    private void startJxta() {
        try {
            System.setProperty("net.jxta.tls.principal", "server");
            System.setProperty("net.jxta.tls.password", "password");
            System.setProperty("JXTA_HOME", System.getProperty("JXTA_HOME", "server"));
            File home = new File(System.getProperty("JXTA_HOME", "server"));
            if (!configured(home)) {
                createConfig(home, "JxtaServerPipeExample", true);
            }
            // create, and Start the default jxta NetPeerGroup
            netPeerGroup = PeerGroupFactory.newNetPeerGroup();
            JxtaBidiPipeExample.login(netPeerGroup, "server", "password");
            //netPeerGroup.startApp(null);
        } catch (PeerGroupException e) {
            // could not instantiate the group, print the stack and exit
            System.out.println("fatal error : group creation failure");
            e.printStackTrace();
            System.exit(1);
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
        ClassLoader cl = JxtaServerPipeExample.class.getClassLoader();
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
}

