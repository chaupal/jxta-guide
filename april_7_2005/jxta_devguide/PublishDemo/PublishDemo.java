/*
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *       Sun Microsystems, Inc. for Project JXTA."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact Project JXTA at http://www.jxta.org.
 *
 * 5. Products derived from this software may not be called "JXTA",
 *    nor may "JXTA" appear in their name, without prior written
 *    permission of Sun.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see
 * <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache Foundation.
 *
 */

import java.util.Enumeration;

import net.jxta.discovery.DiscoveryService;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.ModuleImplAdvertisement;

public class PublishDemo   {

    static PeerGroup myGroup = null;
    private DiscoveryService discoSvc;

    public static void main(String args[]) {
        PublishDemo myapp = new PublishDemo();
        System.out.println ("Starting PublishDemo ....");
        myapp.startJxta();
        myapp.groupsInLocalCache();
        myapp.createGroup();
        myapp.groupsInLocalCache();
        System.exit(0);
    }

    private void startJxta() {
        try {
            // create, and start the default jxta NetPeerGroup
            myGroup = PeerGroupFactory.newNetPeerGroup();
        } catch (PeerGroupException e) {
            // could not instantiate the group, print the stack and exit
            System.out.println("fatal error : group creation failure");
            e.printStackTrace();
            System.exit(1);
        }
        // obtain the the discovery service
        discoSvc = myGroup.getDiscoveryService();
    }

    // print all peer groups found in the local cache
    private void groupsInLocalCache() {

        System.out.println("--- local cache (Peer Groups)  ---");

        try {
            PeerGroupAdvertisement adv = null;
            Enumeration en = discoSvc.getLocalAdvertisements(discoSvc.GROUP, null, null);
            if (en != null) {
                while (en.hasMoreElements()) {
                    adv = (PeerGroupAdvertisement) en.nextElement();
                    System.out.println( adv.getName() + ", group ID = " +
                                        adv.getPeerGroupID().toString());
                }
            }
        } catch (Exception e) {}

        System.out.println("--- end local cache ---");
    }

    // create and publish a new peer group
    private void createGroup() {
        PeerGroupAdvertisement adv;

        System.out.println("Creating a new group advertisement");

        try {
            // create a new all purpose peergroup.
            ModuleImplAdvertisement implAdv =
                myGroup.getAllPurposePeerGroupImplAdvertisement();

            PeerGroup pg = myGroup.newGroup(null,                // Assign new group ID
                                            implAdv,              // The implem. adv
                                            "PubTest",            // The name
                                            "testing group adv"); // Helpful descr.

            // print the name of the group and the peer group ID
            adv = pg.getPeerGroupAdvertisement();
            PeerGroupID GID = adv.getPeerGroupID();
            System.out.println("  Group = " +adv.getName() +
                               "\n  Group ID = " + GID.toString());

        } catch (Exception eee) {
            System.out.println("Group creation failed with " + eee.toString());
            return;
        }

        try {
            // publish this advertisement
            //(send out to other peers and rendezvous peer)
            discoSvc.remotePublish(adv);
            System.out.println("Group published successfully.");
        } catch (Exception e) {
            System.out.println("Error publishing group advertisement");
            e.printStackTrace();
            return;
        }
    }
}
