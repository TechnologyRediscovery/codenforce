/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.session;

import jakarta.annotation.PostConstruct;

/**
 ** Home of Property object family specific non-business session facilitation logic 
 * to organized inter-subsystem operations on Parcel and Property objects and their entire
 * 
 * @author Ellen Bascomb (of Aparment 31Y)
 */
public class SessionPropertyConductor {

    /**
     * Creates a new instance of SessionPropertyConductor
     */
    public SessionPropertyConductor() {
    }
    
    @PostConstruct
    public void initBean()  {
        System.out.println("SessionPropertyConductor.initBean");
        
        
    }
    
}
