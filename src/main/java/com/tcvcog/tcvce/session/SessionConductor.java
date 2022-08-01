/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.session;

import javax.annotation.PostConstruct;

/**
 * The Summer 2022 holder of Session-level methods that previously lived on the
 * dumping ground of the SessionBean. This class handles inter-subsystem issues 
 * like the current system domain (as of July2022 either CE or OCC) and user-level
 * inquiries and permissions. This class will collaborate with the SessionBean 
 * for the forseeable future until full SessionBean deprecation
 * 
 * @author Ellen Bascomb (of Apartment 31Y)
 */
public class SessionConductor {

    /**
     * Creates a new instance of SessionConductor
     */
    public SessionConductor() {
    }
    


    
    
    @PostConstruct
    public void initBean()  {
        System.out.println("SessionConductor.initBean");
        
        
    }
    
    
    
    
    
    
}
