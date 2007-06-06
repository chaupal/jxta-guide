
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

import net.jxta.ext.config.AbstractConfigurator;
import net.jxta.ext.config.Configurator;

import net.jxta.exception.ConfiguratorException;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.impl.protocol.PlatformConfig;

/**
 *
 * @author james todd [gonzo at jxta dot org]
 */

public class JxtaConfigurator
    extends AbstractConfigurator {
    
    public static String NAME = "jxta";
    public static String CREDENTIAL = NAME;
    public static String PASSWORD = NAME;
    
    public static void main(String[] args) {
        process();
    }
    
    static {
        addResource(PROFILE_KEY, "profile.xml");
    }
    
    public PlatformConfig createPlatformConfig(Configurator configurator)
    throws ConfiguratorException {
        System.out.println("saving PlatformConfig to: " +
            configurator.getJXTAHome());
        
        configurator.setName(NAME);
        configurator.setSecurity(CREDENTIAL, PASSWORD);

        return configurator.getPlatformConfig();
    }
    
    public PlatformConfig updatePlatformConfig(Configurator configurator)
    throws ConfiguratorException {
        System.out.println("update PlatformConfig to: " +
            configurator.getJXTAHome());
                
        return configurator.getPlatformConfig();
    }
    
    protected static void process() {
        AbstractConfigurator.register(JxtaConfigurator.class);
        
        try {
            PeerGroupFactory.newNetPeerGroup();
        } catch (PeerGroupException pge) {
        }
    }

    protected void configure() {
    }
}
