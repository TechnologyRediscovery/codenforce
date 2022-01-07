/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.OccInspectionCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.occupancy.OccInspectionRequirement;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public class OccInspectionRequirementBB extends BackingBeanUtils implements Serializable{

    private List<OccInspectionRequirement> reqList;
    private OccInspectionRequirement currentRequirement;
    
    /**
     * Creates a new 
     * instance of PropertyUseTypeBB
     */
    public OccInspectionRequirementBB() {
        
        
    }
    
    @PostConstruct
    public void initBean(){
        refreshRequirementList();
    }
    
    public void refreshRequirementList(){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
            reqList = oic.getRequirementList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    public void editRequirement(OccInspectionRequirement r){
        currentRequirement = r;
    }
    
    public void commitUpdates(ActionEvent ev){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
            oic.updateRequirement(currentRequirement);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully updated Requirement", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update Requirement, sorry", ""));
        }
        refreshRequirementList();
    }
    
    public void commitInsert(ActionEvent ev){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
            oic.insertRequirement(currentRequirement);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Requirement inserted", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not insert Requirement, sorry", ""));
        }
        refreshRequirementList();
    }
    
    public void commitRemove(ActionEvent ev) {
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        if(currentRequirement.getRequirementID() > 0){             
            try {
                int uses = oic.requirementCheckForUse(currentRequirement);
                if(uses == 0){
                    oic.deactivateRequirement(currentRequirement);
                    getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Success! Requirement removed", ""));
                } else {
                    getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Requirement is in use " + uses + " times. Could not remove", ""));
                }
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Could not remove Requirement, sorry", ""));
            }
            refreshRequirementList();
        } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Invalid Requirement: " + currentRequirement.getRequirementID(), ""));
        }
    }
    
    public void createNewRequirement(){
        OccInspectionRequirement p = new OccInspectionRequirement();
        currentRequirement = p;
    }
    

    /**
     * @return the reqList
     */
    public List<OccInspectionRequirement> getReqList() {
        return reqList;
    }

    /**
     * @param reqList the reqList to set
     */
    public void setReqList(List<OccInspectionRequirement> reqList) {
        this.reqList = reqList;
    }

    /**
     * @return the currentRequirement
     */
    public OccInspectionRequirement getCurrentRequirement() {
        return currentRequirement;
    }

    /**
     * @param currentRequirement the currentRequirement to set
     */
    public void setCurrentRequirement(OccInspectionRequirement currentRequirement) {
        this.currentRequirement = currentRequirement;
    }
    
}
