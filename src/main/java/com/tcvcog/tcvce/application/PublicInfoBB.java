/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.PublicInfoCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.PublicInfoBundle;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public class PublicInfoBB extends BackingBeanUtils implements Serializable{

    private List<PublicInfoBundle> publicInfoBundleList;
    private PublicInfoBundle selectedBundle;
    private int submittedPACC;
    
    
    /**
     * Creates a new instance of CEPublicAccessBB
     */
    public PublicInfoBB() {
    }
    
    public void submitPACC(ActionEvent ae){
        PublicInfoCoordinator pic = getPublicInfoCoordinator();
        try {
            publicInfoBundleList = pic.getPublicInfoBundles(submittedPACC);
            if(!publicInfoBundleList.isEmpty()){
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Retrieved " + String.valueOf(publicInfoBundleList.size()) + " bundles!",""));
            } else {
                
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "No info bundles found for this control code",""));
                publicInfoBundleList = null;
            }
        } catch (IntegrationException ex) {
              getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to search for info bundles, sorry!", "This is a system error."));
        }
        
    }
    
    public void attachMessage(PublicInfoBundle pib){
        
    }

    /**
     * @return the publicInfoBundleList
     */
    public List<PublicInfoBundle> getPublicInfoBundleList() {
        return publicInfoBundleList;
    }

    /**
     * @return the submittedPACC
     */
    public int getSubmittedPACC() {
        return submittedPACC;
    }

    /**
     * @param publicInfoBundleList the publicInfoBundleList to set
     */
    public void setPublicInfoBundleList(List<PublicInfoBundle> publicInfoBundleList) {
        this.publicInfoBundleList = publicInfoBundleList;
    }

    /**
     * @param submittedPACC the submittedPACC to set
     */
    public void setSubmittedPACC(int submittedPACC) {
        this.submittedPACC = submittedPACC;
    }

    /**
     * @return the selectedBundle
     */
    public PublicInfoBundle getSelectedBundle() {
        return selectedBundle;
    }

    /**
     * @param selectedBundle the selectedBundle to set
     */
    public void setSelectedBundle(PublicInfoBundle selectedBundle) {
        this.selectedBundle = selectedBundle;
    }
    
    
    
}
