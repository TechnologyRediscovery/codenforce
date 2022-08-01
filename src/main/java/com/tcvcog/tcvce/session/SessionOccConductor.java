/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.session;

import javax.annotation.PostConstruct;

/**
 * Home of Occupancy Period (to users, these are now called Permit Files) 
 * object family specific non-business session facilitation logic 
 * to organized inter-subsystem operations on OccPeriod objects and their entire family
 * 
 * @author Ellen Bascomb (of Aparment 31Y)
 */
public class SessionOccConductor {

    /**
     * Creates a new instance of SessionOccConductor
     */
    public SessionOccConductor() {
    }

    
    
    @PostConstruct
    public void initBean()  {
        System.out.println("SessionOccConductor.initBean");
        
        
    }
    
    
}
