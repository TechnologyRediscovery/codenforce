/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.session;

import jakarta.annotation.PostConstruct;

/**
 * Home of CECase object family specific non-business session facilitation logic 
 * to organized inter-subsystem operations on CECAse objects and their entire family
 * 
 * @author Ellen Bascomb (of Aparment 31Y)
 */
public class SessionCECaseConductor {

    /**
     * Creates a new instance of SessionCECaseConductor
     */
    public SessionCECaseConductor() {
    }
    
    @PostConstruct
    public void initBean()  {
        System.out.println("SessionCECaseConductor.initBean");
        
        
    }
    
}
