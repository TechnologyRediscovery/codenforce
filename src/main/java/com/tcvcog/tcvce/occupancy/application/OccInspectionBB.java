/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitWithLists;
import com.tcvcog.tcvce.entities.PropertyUnitWithProp;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpace;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpaceElement;
import com.tcvcog.tcvce.entities.occupancy.OccInspection;
import com.tcvcog.tcvce.entities.occupancy.OccLocationDescriptor;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccSpace;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceType;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceTypeInspectionDirective;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
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
    private OccPeriod currentOccPeriod;
    private PropertyUnitWithProp currentPropertyUnit;
    private Property currentProperty;
    
    private OccInspectedSpace currentInSpc;
    private OccInspectedSpaceElement currentInSpcEl;
    
    private OccSpaceTypeInspectionDirective selectedOccSpaceType;
    private List<OccSpace> browseSpaceList;
    private List<User> inspectorPossibilityList;
    private User selectedInspector;
    
    private String formNoteText;
    private List<OccLocationDescriptor> locationList;
    
    
    
    /**
     * Creates a new instance of InspectionsBB
     */
    public OccInspectionBB() {
    }
    
    @PostConstruct
    public void initBean(){
        OccupancyIntegrator oi = getOccupancyIntegrator();
        PropertyIntegrator pi = getPropertyIntegrator();
        
        browseSpaceList = new ArrayList<>();
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        if(currentInspection == null){
            if(getSessionBean().getSessionOccInspection() != null){
                currentInspection = getSessionBean().getSessionOccInspection();
                try {
                    currentInspection = oii.getOccInspection(currentInspection.getInspectionID());
                } catch (IntegrationException ex) {
                    System.out.println(ex);
                }
            }
        }
        try {
            currentOccPeriod = oi.getOccPeriod(currentInspection.getOccPeriodID());
            currentPropertyUnit = pi.getPropertyUnitWithProp(currentOccPeriod.getPropertyUnitID());
        } catch (IntegrationException ex) {
            Logger.getLogger(OccInspectionBB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void browseSpaceType(){
        if(selectedOccSpaceType != null){
            browseSpaceList = selectedOccSpaceType.getSpaceList();
            System.out.println("OccInspectionBB.browseSpaceType");
        }
        
    }
    
    
    
    private void reloadCurrentInspection(){
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        try {
            currentInspection = oii.getOccInspection(currentInspection.getInspectionID());
        } catch (IntegrationException ex) {
            System.out.println(ex);
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Unable to reload inspection", ""));
            
        }
        
    }
    
    public void addSpaceToChecklist(OccSpace space) {
        OccupancyIntegrator oi = getOccupancyIntegrator();
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.inspectionAction_commenceSpaceInspection(currentInspection,
                            getSessionBean().getSessionUser(),
                            space,
                            null);
            
        System.out.println("OccInspectionBB.addSpaceToChecklist | space name: " + space.getName());
        reloadCurrentInspection();
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Space added to checklist!", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Unable to add space to checklist", ""));
        }
        
    }
    
     
    /**
     * Called when the user clicks a command button inside the row of the
 OccInspection table to manage it
     * @param ev
     */
    public void beginInspectionMetadataEdit(ActionEvent ev){
        
        
    }
    
    
    
    
     /**
      * Edits the currentInspection 
      * @param e 
      */
     public void editOccupancyInspectionMetadata(ActionEvent e){
         
    }
     
     public void removeSpaceFromChecklist(OccInspectedSpace spc){
         OccupancyCoordinator oc = getOccupancyCoordinator();
         oc.removeSpaceFromChecklist(spc, getSessionBean().getSessionUser(), currentInspection);
     }
     
     public void recordComplianceWithElement(OccInspectedSpaceElement inSpcEl){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.recordComplianceWithInspectedElement(    inSpcEl,
                                                        getSessionBean().getSessionUser(),
                                                        currentInspection);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Compliance recorded for Space Element: " + inSpcEl.getInspectedSpaceElementID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                ex.getMessage() + inSpcEl.getInspectedSpaceElementID(), ""));
        }
     }
     
     public void removeComlianceWithElement(OccInspectedSpaceElement inSpcEl){
         OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.removeComplianceWithInspectedElement(    inSpcEl,
                                                        getSessionBean().getSessionUser(),
                                                        currentInspection);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Compliance removed for Space Element: " + inSpcEl.getInspectedSpaceElementID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                ex.getMessage() + inSpcEl.getInspectedSpaceElementID(), ""));
        }
     }
     
     public void editLocation(OccInspectedSpace inSpace){
         
         
         
     }
     
     
     
     
     
     
     
     public void addNoteToInspectedElement(OccInspectedSpaceElement spcEl){
         
         
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

    /**
     * @return the selectedOccSpaceType
     */
    public OccSpaceTypeInspectionDirective getSelectedOccSpaceType() {
        return selectedOccSpaceType;
    }

    /**
     * @param selectedOccSpaceType the selectedOccSpaceType to set
     */
    public void setSelectedOccSpaceType(OccSpaceTypeInspectionDirective selectedOccSpaceType) {
        this.selectedOccSpaceType = selectedOccSpaceType;
    }

    /**
     * @return the browseSpaceList
     */
    public List<OccSpace> getBrowseSpaceList() {
        
        return browseSpaceList;
    }

    /**
     * @param browseSpaceList the browseSpaceList to set
     */
    public void setBrowseSpaceList(List<OccSpace> browseSpaceList) {
        this.browseSpaceList = browseSpaceList;
    }

    /**
     * @return the formNoteText
     */
    public String getFormNoteText() {
        return formNoteText;
    }

    /**
     * @param formNoteText the formNoteText to set
     */
    public void setFormNoteText(String formNoteText) {
        this.formNoteText = formNoteText;
    }

    /**
     * @return the locationList
     */
    public List<OccLocationDescriptor> getLocationList() {
        return locationList;
    }

    /**
     * @param locationList the locationList to set
     */
    public void setLocationList(List<OccLocationDescriptor> locationList) {
        this.locationList = locationList;
    }

    /**
     * @return the currentInSpcEl
     */
    public OccInspectedSpaceElement getCurrentInSpcEl() {
        return currentInSpcEl;
    }

    /**
     * @param currentInSpcEl the currentInSpcEl to set
     */
    public void setCurrentInSpcEl(OccInspectedSpaceElement currentInSpcEl) {
        this.currentInSpcEl = currentInSpcEl;
    }

    /**
     * @return the currentInSpc
     */
    public OccInspectedSpace getCurrentInSpc() {
        return currentInSpc;
    }

    /**
     * @param currentInSpc the currentInSpc to set
     */
    public void setCurrentInSpc(OccInspectedSpace currentInSpc) {
        this.currentInSpc = currentInSpc;
    }

    /**
     * @return the inspectorPossibilityList
     */
    public List<User> getInspectorPossibilityList() {
        return inspectorPossibilityList;
    }

    /**
     * @param inspectorPossibilityList the inspectorPossibilityList to set
     */
    public void setInspectorPossibilityList(List<User> inspectorPossibilityList) {
        this.inspectorPossibilityList = inspectorPossibilityList;
    }

    /**
     * @return the selectedInspector
     */
    public User getSelectedInspector() {
        return selectedInspector;
    }

    /**
     * @param selectedInspector the selectedInspector to set
     */
    public void setSelectedInspector(User selectedInspector) {
        this.selectedInspector = selectedInspector;
    }

    /**
     * @return the currentOccPeriod
     */
    public OccPeriod getCurrentOccPeriod() {
        return currentOccPeriod;
    }

    /**
     * @return the currentPropertyUnit
     */
    public PropertyUnitWithProp getCurrentPropertyUnit() {
        return currentPropertyUnit;
    }

    /**
     * @return the currentProperty
     */
    public Property getCurrentProperty() {
        return currentProperty;
    }

    /**
     * @param currentOccPeriod the currentOccPeriod to set
     */
    public void setCurrentOccPeriod(OccPeriod currentOccPeriod) {
        this.currentOccPeriod = currentOccPeriod;
    }

    /**
     * @param currentPropertyUnit the currentPropertyUnit to set
     */
    public void setCurrentPropertyUnit(PropertyUnitWithProp currentPropertyUnit) {
        this.currentPropertyUnit = currentPropertyUnit;
    }

    /**
     * @param currentProperty the currentProperty to set
     */
    public void setCurrentProperty(Property currentProperty) {
        this.currentProperty = currentProperty;
    }
    
    
    
}
