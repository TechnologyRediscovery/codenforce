/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.occupancy.OccInspection;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 * Primary backing bean for the inspections.xhtml page which is the central
 management point for all occupancy inspection related tasks including:\
 * 
 Reviewing, editing (generally processing) occupancy applications
 Initiating all occupancy inspection related events such as starting a new 
  occupancy inspection, editing an existing one, checking on the status of one
 Initiating the creation of an occupancy permit based on a successful inspection
 
 Classes with similar functions for different core business objects:
 CaseProfileBB.java
 PersonsBB.java
 CEActionRequestsBB.java
  
 Design considerations:
 The primary methods on this bean are to manage the querying for and displaying
 occupancy inspection objects, which contain all sorts of goodies
 
 The convention in the family of backing beans that do the same kind of work
 is to maintain as a member variable a List of the main business object, 
 in this case, an OccInspection
 
 and a member variable named something like selectedXXXX or currentXXX which
 is loaded when the user clicks on a row button on the left column's data table
 display and then used to populate all of the object-specific fields in the
 right -side management page.
 *  
 * You may want separate backing beans to manage tasks related to occupancy inspections
 to keep this bean mostly about querying, displaying and selecting our core business
 object of the OccInspection
 * 
 * 
 * @author mced ghost
 */
public class OccInspectionBB extends BackingBeanUtils implements Serializable {

    private OccInspection currentInspection;
    
    /**
     * Creates a new instance of InspectionsBB
     */
    public OccInspectionBB() {
    }
    
    /**
     * Called when the user clicks a command button inside the row of the
 OccInspection table to manage it
     * @param ins 
     */
    public void manageInspection(OccInspection ins){
        setCurrentInspection(ins);
        
    }
     /**
      * Edits the currentInspection 
      * @param e 
      */
     public void editOccupancyInspection(ActionEvent e){
         
    }
    
     /**
      * We can only delete one that was JUST made - OK if this doesn't get implemented
      * until the end
      * @param e 
      */
    public void deleteSelectedOccupancyInspection(ActionEvent e){
        OccupancyIntegrator oii = getOccupancyIntegrator();
            
    }

    /**
     * Happens in a dialog in inspections.xhtml
     * @param e 
     */
    public void commitOccupancyInspectionUpdates(ActionEvent e){
        OccupancyIntegrator oii = getOccupancyIntegrator();
        OccInspectionIntegrator ci = getOccInspectionIntegrator();
        
        try{
            ci.updateOccInspection(currentInspection);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Occupancy Inspection Record updated!", ""));
        } catch (IntegrationException ex){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to update occupancy inspection record in database.",
                    "This must be corrected by the System Administrator"));
        }
    }
    
   


  

    /**
     * @return the currentInspection
     */
    public OccInspection getCurrentInspection() {
        return currentInspection;
    }

  

    /**
     * @param currentInspection the currentInspection to set
     */
    public void setCurrentInspection(OccInspection currentInspection) {
        this.currentInspection = currentInspection;
    }
    
    
    
}
