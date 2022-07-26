/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;

/**
 * Tools for pinning and unpinning objects, as of July 2022 this occured on the
 * dashboard
 * 
 * @author Ellen Bascomb of Apartment 31Y
 */
public class PinningBB extends BackingBeanUtils{

    /**
     * Creates a new instance of PinningBB
     */
    public PinningBB() {
    }
    
    
    /**
     * Pins an unpinned instance of IFace_pinnable and unpins a pinned
     * instance of IFace_pinnable
     * @param pinnable 
     */
    public void togglePin(IFace_pinnable pinnable){
        SystemCoordinator sc = getSystemCoordinator();
        try {
            sc.togglePinning(pinnable, getSessionBean().getSessUser());
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_ERROR,
                           "Fatal error: unable to toggle pinned status in the database, sorry.",""));
        }
    }
}
