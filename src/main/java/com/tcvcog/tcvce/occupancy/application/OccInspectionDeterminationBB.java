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
import com.tcvcog.tcvce.entities.occupancy.OccInspectionDetermination;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public class OccInspectionDeterminationBB extends BackingBeanUtils implements Serializable{

    private List<OccInspectionDetermination> detList;
    private OccInspectionDetermination currentDetermination;
    
    /**
     * Creates a new 
     * instance of PropertyUseTypeBB
     */
    public OccInspectionDeterminationBB() {
        
        
    }
    
    @PostConstruct
    public void initBean(){
        refreshDeterminationList();
    }
    
    public void refreshDeterminationList(){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
            detList = oic.getDeterminationList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    public void editDetermination(OccInspectionDetermination d){
        currentDetermination = d;
    }
    
    public void commitUpdates(ActionEvent ev){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
            oic.updateDetermination(currentDetermination);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully updated Determination", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update Determination, sorry", ""));
        }
        refreshDeterminationList();
    }
    
    public void commitInsert(ActionEvent ev){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
            oic.insertDetermination(currentDetermination);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Determination inserted", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not insert Determination, sorry", ""));
        }
        refreshDeterminationList();
    }
    
    public void commitRemove(ActionEvent ev) {
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        if(currentDetermination.getDeterminationID() > 0){             
            try {
                int uses = oic.determinationCheckForUse(currentDetermination);
                if(uses == 0){
                    oic.deactivateDetermination(currentDetermination);
                    getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Success! Determination removed", ""));
                } else {
                    getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Determination is in use " + uses + " times. Could not remove", ""));
                }
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Could not remove Determination, sorry", ""));
            }
            refreshDeterminationList();
        } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Invalid Determination: " + currentDetermination.getDeterminationID(), ""));
        }
    }
    
    public void createNewDetermination(){
        OccInspectionDetermination p = new OccInspectionDetermination();
        p.setEventCategory(new EventCategory());
        currentDetermination = p;
    }
    

    /**
     * @return the detList
     */
    public List<OccInspectionDetermination> getDetList() {
        return detList;
    }

    /**
     * @param detList the detList to set
     */
    public void setDetList(List<OccInspectionDetermination> detList) {
        this.detList = detList;
    }

    /**
     * @return the currentDetermination
     */
    public OccInspectionDetermination getCurrentDetermination() {
        return currentDetermination;
    }

    /**
     * @param currentDetermination the currentDetermination to set
     */
    public void setCurrentDetermination(OccInspectionDetermination currentDetermination) {
        this.currentDetermination = currentDetermination;
    }
    
}
