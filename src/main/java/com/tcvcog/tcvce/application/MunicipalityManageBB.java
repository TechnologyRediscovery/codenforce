/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;

/**
 *
 * @author sylvia
 */
public class MunicipalityManageBB extends BackingBeanUtils implements Serializable {

    private Municipality currentMuni;
    
    
    /**
     * Creates a new instance of MunicipalityManageBB
     */
    public MunicipalityManageBB() {
    }
    
    public String updateMuni(){
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        try {
            mi.updateMuni(currentMuni);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Successfully updated municipality info!", ""));
            getSessionBean().setActiveMuni(mi.getMuniFromMuniCode(currentMuni.getMuniCode()));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Unable to update municipality info due to a system error", 
                        "This could be because the code set"
                        + "ID or the code source ID you entered is not an "
                        + "actual database record ID. Check in the "
                        + "\"municipal code\" section to verify ID numbers"));
        }
        
        return "";
        
        
    }

    /**
     * @return the currentMuni
     */
    public Municipality getCurrentMuni() {
        currentMuni = getSessionBean().getActiveMuni();
        return currentMuni;
    }

    /**
     * @param currentMuni the currentMuni to set
     */
    public void setCurrentMuni(Municipality currentMuni) {
        this.currentMuni = currentMuni;
    }
    
}
