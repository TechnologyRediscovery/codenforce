/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.session;

import jakarta.annotation.PostConstruct;

/**
 *
 * @author Ellen Bascomb (of Aparment 31Y)
 */
public class SessionCodeConductor {

    /**
     * Creates a new instance of SessionCodeConductor
     */
    public SessionCodeConductor() {
    }
    
    
    
    @PostConstruct
    public void initBean()  {
        System.out.println("SessionCodeConductor.initBean");
        
        
    }
    
    
}
