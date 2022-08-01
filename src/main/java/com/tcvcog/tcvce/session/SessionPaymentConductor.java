/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.session;

import javax.annotation.PostConstruct;

/**
 * Home of Money object family specific non-business session facilitation logic 
 * to organized inter-subsystem operations on Money objects and their entire family
 * 
 * @author Ellen Bascomb (of Aparment 31Y)
 */
public class SessionPaymentConductor {

    /**
     * Creates a new instance of SessionPaymentConductor
     */
    public SessionPaymentConductor() {
    }
    
    
    @PostConstruct
    public void initBean()  {
        System.out.println("SessionPaymentConductor.initBean");
        
        
    }
    
    
}
