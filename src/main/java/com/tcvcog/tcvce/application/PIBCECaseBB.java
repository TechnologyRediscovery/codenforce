/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.PublicInfoBundleCECase;
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public class PIBCECaseBB extends BackingBeanUtils implements Serializable {
    
    private PublicInfoBundleCECase activePIBCECase;
    private String messagerName;
    private String messagerPhone;
    
    

    /**
     * Creates a new instance of PIBCECaseBB
     */
    public PIBCECaseBB() {
        
        
    }

    
    public void attachMessageToCase(ActionEvent ev){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.public_attachPublicMessage(activePIBCECase.getBundledCase().getCaseID(), messagerName, messagerName, messagerPhone);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Successfully added message to csae. Please re-search using your access control code to see your event appear in the list", ""));
        } catch (IntegrationException | BObStatusException | EventException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to attach a note to this case, sorries!", "This is a system error and must be corrected by an administrator."));
        }        
    }
    
    /**
     * @return the activePIBCECase
     */
    public PublicInfoBundleCECase getActivePIBCECase() {
        activePIBCECase = getSessionBean().getPibCECase();
        return activePIBCECase;
    }

    /**
     * @param activePIBCECase the activePIBCECase to set
     */
    public void setActivePIBCECase(PublicInfoBundleCECase activePIBCECase) {
        this.activePIBCECase = activePIBCECase;
    }

   

    /**
     * @return the messagerPhone
     */
    public String getMessagerPhone() {
        return messagerPhone;
    }

    

    /**
     * @param messagerPhone the messagerPhone to set
     */
    public void setMessagerPhone(String messagerPhone) {
        this.messagerPhone = messagerPhone;
    }

    /**
     * @return the messagerName
     */
    public String getMessagerName() {
        return messagerName;
    }

    /**
     * @param messagerName the messagerName to set
     */
    public void setMessagerName(String messagerName) {
        this.messagerName = messagerName;
    }
    
}
