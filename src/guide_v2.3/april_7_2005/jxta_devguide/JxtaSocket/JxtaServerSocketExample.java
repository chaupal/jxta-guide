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
 *  $Id: JxtaServerSocketExample.java,v 1.1 2005/11/22 04:27:21 raygao Exp $
 */
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.net.Socket;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.exception.PeerGroupException;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.socket.JxtaServerSocket;
import net.jxta.protocol.PipeAdvertisement;

/**
 *  This tutorial illustrates the use JxtaServerSocket It creates a
 *  JxtaServerSocket with a back log of 10. it also blocks indefinitely, until a
 *  connection is established Once a connection is established, it sends the
 *  content of socket.adv and reads data from the remote side.
 *
 */

public class JxtaServerSocketExample {

    private transient PeerGroup netPeerGroup = null;
    private transient PipeAdvertisement pipeAdv;
    private transient JxtaServerSocket serverSocket;

    /**
     * Sends data over socket
     *
     *@param  socket  Description of the Parameter
     */
    private void sendAndReceiveData(Socket socket) {
        try {
            // get the socket output stream
            OutputStream out = socket.getOutputStream();
            // read a file into a buffer
            File file = new File("socket.adv");
            FileInputStream is = new FileInputStream(file);
            int size = 4096;
            byte[] buf = new byte[size];
            int read = is.read(buf, 0, size);

            // send some bytes over the socket (the socket adv is used, but that could
            // be anything. It's just a handshake.)
            out.write(buf, 0, read);
            out.flush();
            System.out.println(read + " bytes sent");
            InputStream in = socket.getInputStream();

            // this call should block until bits are avail.
            long total = 0;
            long start = System.currentTimeMillis();
            while (true) {
                read = in.read(buf, 0, size);
                if (read < 1) {
                    break;
                }
                total += read;
                //System.out.print(".");
                //System.out.flush();
            }
            System.out.println("");
            long elapsed = System.currentTimeMillis() - start;
            System.out.println("EOT. Received " + total + " bytes in " + elapsed + " ms. Throughput = " + ((total * 8000) / (1024 * elapsed)) + " Kbit/s.");
            socket.close();
            System.out.println("Closed connection. Ready for next connection.");
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    /**
     *  wait for msgs
     */
    public void run() {

        System.out.println("starting ServerSocket");
        while (true) {
            try {
                System.out.println("Calling accept");
                Socket socket = serverSocket.accept();
                // set reliable
                if (socket != null) {
                    System.out.println("socket created");
                    sendAndReceiveData(socket);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *  Starts jxta
     */
    private void startJxta() {
        try {
            System.setProperty("net.jxta.tls.principal", "server");
            System.setProperty("net.jxta.tls.password", "password");
            System.setProperty("JXTA_HOME", System.getProperty("JXTA_HOME", "server"));
            File home = new File(System.getProperty("JXTA_HOME", "server"));
            if (!JxtaSocketExample.configured(home)) {
                JxtaSocketExample.createConfig(home, "JxtaServerSocketExample", true);
            }

            // create, and Start the default jxta NetPeerGroup
            netPeerGroup = PeerGroupFactory.newNetPeerGroup();
            //JxtaSocketExample.login(netPeerGroup, "server", "password");
        } catch (PeerGroupException e) {
            // could not instantiate the group, print the stack and exit
            System.out.println("fatal error : group creation failure");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     *  main
     *
     *@param  args  command line args
     */
    public static void main(String args[]) {

        JxtaServerSocketExample socEx = new JxtaServerSocketExample();
        socEx.startJxta();
        System.out.println("Reading in socket.adv");
        try {
            FileInputStream is = new FileInputStream("socket.adv");
            socEx.pipeAdv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(MimeMediaType.XMLUTF8, is);
            is.close();
            socEx.serverSocket = new JxtaServerSocket(socEx.netPeerGroup, socEx.pipeAdv, 10);
            // block until a connection is available
            socEx.serverSocket.setSoTimeout(0);
        } catch (Exception e) {
            System.out.println("failed to read/parse pipe advertisement");
            e.printStackTrace();
            System.exit(-1);
        }
        socEx.run();
    }
}
