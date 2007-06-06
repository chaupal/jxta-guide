
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
import net.jxta.exception.ConfiguratorException;

/**
 *
 * @author james todd [gonzo at jxta dot org]
 */

public class SimpleConfigurator {
    
    private static final String NAME = "MyPeerName";
    private static final String DESCRIPTION = "MyPeerDescription";
    private static final String PRINCIPAL = "MyPeerPrincipal";
    private static final String PASSWORD = "MyPeerPassWord";
    
    public static void main(String[] args) {
        SimpleConfigurator sc = new SimpleConfigurator();
        
        sc.process();
    }
    
    public SimpleConfigurator() {
    }
    
    protected void process() {
        configure();
        
        System.out.println("create PlatformConfig with the following properties:");
        System.out.println("  name = " + NAME );
        System.out.println("  description = " + DESCRIPTION);
        System.out.println("  principal = " + PRINCIPAL);
        System.out.println("  password = " + PASSWORD);
        
        Configurator c = new Configurator(NAME, DESCRIPTION, PRINCIPAL, PASSWORD);
        
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
