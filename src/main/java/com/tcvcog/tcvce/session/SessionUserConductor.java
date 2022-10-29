/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.session;

import jakarta.annotation.PostConstruct;

/**
 * Home of User object family specific non-business session facilitation logic 
 * to organized inter-subsystem operations on User objects and their entire family
 * 
 * @author Ellen Bascomb (of Aparment 31Y)
 */
public class SessionUserConductor {

    /**
     * Creates a new instance of SessionUserConductor
     */
    public SessionUserConductor() {
    }
    
    @PostConstruct
    public void initBean()  {
        System.out.println("SessionUserConductor.initBean");
        
        
    }
    
}
