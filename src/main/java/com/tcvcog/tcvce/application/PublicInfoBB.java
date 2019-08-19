/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.PublicInfoCoordinator;
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.PublicInfoBundle;
import com.tcvcog.tcvce.entities.PublicInfoBundleCECase;
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
    private String publicMessage;
    
    
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
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unable to search for info bundles, sorry!", "This is a system error."));
        } catch (CaseLifecycleException ex) {
            System.out.println(ex);
        }
        
    }
    
    public String viewPACCRecordDetails(PublicInfoBundle pib){
        if(pib instanceof PublicInfoBundleCECase){
            PublicInfoBundleCECase pibCase = (PublicInfoBundleCECase) pib;
            getSessionBean().setPibCECase(pibCase);
            return "publicInfoCECase";
            
        }
        return "";
    }
    
    public void attachMessage(ActionEvent ev){
        PublicInfoCoordinator pic = getPublicInfoCoordinator();
        try {
            pic.attachMessageToBundle(selectedBundle, publicMessage);
            getFacesContext().addMessage(null,
                  new FacesMessage(FacesMessage.SEVERITY_INFO,
                          "Public case note added", ""));

        } catch (IntegrationException ex) {
            System.out.println(ex);
              getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to attach messages at this time, sorry!", 
                            "This is a system error and has been logged for debugging."));
        
        }
        publicMessage = null;
        
        
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
     * @param sb
     */
    public void setSelectedBundle(PublicInfoBundle sb) {
        System.out.println("PublicInfoBB.setSelectedBundle | Bundle type: " + sb.getTypeName());
        this.selectedBundle = sb;
    }

    /**
     * @return the publicMessage
     */
    public String getPublicMessage() {
        return publicMessage;
    }

    /**
     * @param publicMessage the publicMessage to set
     */
    public void setPublicMessage(String publicMessage) {
        this.publicMessage = publicMessage;
    }
    
    
    public String goToIntensityManage(){
        
        return "intensityManage";
    }
    
    
}
