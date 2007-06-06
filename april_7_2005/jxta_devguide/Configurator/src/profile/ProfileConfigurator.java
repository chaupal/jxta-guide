
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

import net.jxta.ext.config.Configurator;
import net.jxta.ext.config.Profile;
import net.jxta.exception.ConfiguratorException;

/**
 *
 * @author james todd [gonzo at jxta dot org]
 */

public class ProfileConfigurator {
    
    private static final String PROFILE = "profile.xml";
    private static final String PRINCIPAL = "MyPeerPrincipal";
    private static final String PASSWORD = "MyPeerPassWord";
    
    public static void main(String[] args) {
        ProfileConfigurator pc = new ProfileConfigurator();
        
        pc.process();
    }
    
    public ProfileConfigurator() {
    }
    
    protected void process() {
        configure();
        
        System.out.println("create PlatformConfig with the following properties:");
        System.out.println("  profile = " + PROFILE );
        
        Profile p = new Profile(this.getClass().getResource(PROFILE));
        Configurator c = new Configurator(p);
        
        c.setSecurity(PRINCIPAL, PASSWORD);
        
        System.out.println("saving PlatformConfig to: " + c.getJXTAHome());
        
        try {
            c.save();
        } catch (ConfiguratorException ce) {
            ce.printStackTrace();
        }
    }
    
    protected void configure() {
    }
}
