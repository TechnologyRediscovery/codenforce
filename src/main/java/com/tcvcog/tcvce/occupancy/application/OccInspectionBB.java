/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.ChoiceCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CECaseEvent;
import com.tcvcog.tcvce.entities.Choice;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnitWithProp;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.ProposalOccPeriod;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.OccEvent;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpace;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpaceElement;
import com.tcvcog.tcvce.entities.occupancy.OccInspection;
import com.tcvcog.tcvce.entities.occupancy.OccLocationDescriptor;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPermit;
import com.tcvcog.tcvce.entities.occupancy.OccSpace;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceTypeInspectionDirective;
import com.tcvcog.tcvce.entities.reports.ReportConfigOccInspection;
import com.tcvcog.tcvce.entities.reports.ReportConfigOccPermit;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
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
    private List<OccEvent> filteredEventList;
    
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
    
    // reports
    private ReportConfigOccInspection reportConfigOccInspec;
    private OccPermit currentOccPermit;
    private ReportConfigOccPermit reportConfigOccPermit;
    
    // events 
    private OccEvent selectedEvent;
    private List<EventType> availableEventTypeList; 
    private EventType selectedEventType;
    private List<EventCategory> eventCategoryList;
    private EventCategory selectedEventCategory;
    private Person selectedPerson;
    
    /**
     * Creates a new instance of InspectionsBB
     */
    public OccInspectionBB() {
    }
    
    @PostConstruct
    public void initBean(){
        OccupancyIntegrator oi = getOccupancyIntegrator();
        PropertyIntegrator pi = getPropertyIntegrator();
        OccupancyCoordinator oc = getOccupancyCoordinator();
        
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
        
        availableEventTypeList = oc.getPermittedEventTypesForCECase(currentOccPeriod, getSessionBean().getSessionUser());
        
        

        
    }
    
    public void browseSpaceType(){
        if(selectedOccSpaceType != null){
            browseSpaceList = selectedOccSpaceType.getSpaceList();
            System.out.println("OccInspectionBB.browseSpaceType");
        }
        
    }
    
     public void makeChoice(Choice choice, Proposal p){
        ChoiceCoordinator cc = getChoiceCoordinator();
        try {
            if(p instanceof ProposalOccPeriod){
                currentOccPeriod = cc.processProposalEvaluation(    p, 
                                                                    choice, 
                                                                    currentOccPeriod, 
                                                                    getSessionBean().getSessionUser());
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "You just chose choice ID " + choice.getChoiceID() + " proposed in proposal ID " + p.getProposalID(), ""));
            }
            
        } catch (EventException | AuthorizationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
            ex.getMessage(), ""));
        }
    }    
    
    public void updateEventCategoryList(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        EventIntegrator ei = getEventIntegrator();
        try {
            eventCategoryList = ei.getEventCategoryList(selectedEventType);
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Unable to load event category choices, sorry!", ""));
            
        }
    }
    
    public void initializeEvent(EventCategory eventCat){
        
        
        
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
    
    
     public void hideEvent(OccEvent event){
        EventIntegrator ei = getEventIntegrator();
        event.setHidden(true);
        try {
            ei.updateEvent(event);
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_INFO,
                           "Success! event ID: " + event.getEventID() + " is now hidden", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not hide event, sorry; this is a system erro", ""));
        }
    }
    
    public void unHideEvent(CECaseEvent event){
        EventIntegrator ei = getEventIntegrator();
        event.setHidden(false);
        try {
            ei.updateEvent(event);
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_INFO,
                           "Success! Unhid event ID: " + event.getEventID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_ERROR,
                           "Could not unhide event, sorry; this is a system erro", ""));
        }
    }
  /**
     * Called when the user selects their own EventCategory to add to the case
     * and is a pass-through method to the initializeEvent method
     *
     * @param ev
     */
    public void initiateUserChosenEventCreation(ActionEvent ev) {
        initiateNewEvent();
    }

    public void initiateNewEvent() {

        if (getSelectedEventCategory() != null) {

            System.out.println("OccInspectionBB.initiateNewEvent | category: " + getSelectedEventCategory().getEventCategoryTitle());
            EventCoordinator ec = getEventCoordinator();
            try {
                selectedEvent = ec.getInitializedEvent(currentOccPeriod, getSelectedEventCategory());
                selectedEvent.setDateOfRecord(LocalDateTime.now());
                selectedEvent.setDiscloseToMunicipality(true);
                selectedEvent.setDiscloseToPublic(false);
            } catch (CaseLifecyleException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            }
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select an event category to create a new event.", ""));
        }
    }
    
    public void initializeOccInspectionReport(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        reportConfigOccInspec = oc.getOccInspectionReportConfigDefault( currentInspection, 
                                                                        currentOccPeriod, 
                                                                        getSessionBean().getSessionUser());
        
        
    }
    
    public String generateOccInspectionReport(){
        
        
        
        return "inspectionReport";
    }
    
    public void initializeOccPermit(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        currentOccPermit = oc.getOccPermitSkeleton(getSessionBean().getSessionUser());
        
        
        
    }
    

    public String generateOccPermit(OccPermit permit){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        currentOccPermit = permit;
        reportConfigOccPermit = oc.getOccPermitReportConfigDefault( currentOccPermit, 
                                                                    currentOccPeriod, 
                                                                    currentPropertyUnit, 
                                                                    getSessionBean().getSessionUser());
        getSessionBean().setReportConfigOccPermit(reportConfigOccPermit);
        
        return "occPermit";
    }
    
    
    /**
     * All Code Enforcement case events are funneled through this method which
     * has to carry out a number of checks based on the type of event being
     * created. The event is then passed to the attachNewEventToCECase on the
     * CaseCoordinator who will do some more checking about the event before
     * writing it to the DB
     *
     * @param ev unused
     * @throws ViolationException
     */
    public void attachEventToOccPeriod(ActionEvent ev) throws ViolationException {
        EventCoordinator ec = getEventCoordinator();
        CaseCoordinator cc = getCaseCoordinator();

        // category is already set from initialization sequence
        selectedEvent.setOccPeriodID(currentOccPeriod.getPeriodID());
        selectedEvent.setOwner(getSessionBean().getSessionUser());
//        try {
        
            // main entry point for handing the new event off to the CaseCoordinator
            // only the compliance events need to pass in another object--the violation
            // otherwise just the case and the event go to the coordinator
//            if (selectedEvent.getCategory().getEventType() == EventType.Compliance) {
//                selectedEvent.setEventID(cc.attachNewEventToCECase(currentCase, selectedEvent, selectedViolation));
//            } else {
//                selectedEvent.setEventID(cc.attachNewEventToCECase(currentCase, selectedEvent, null));
//            }
//            getFacesContext().addMessage(null,
//                    new FacesMessage(FacesMessage.SEVERITY_INFO,
//                            "Successfully logged event with an ID " + selectedEvent.getEventID(), ""));

            

//        } catch (IntegrationException ex) {
//            System.out.println(ex);
//            getFacesContext().addMessage(null,
//                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                            ex.getMessage(),
//                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
//        } catch (CaseLifecyleException ex) {
//            System.out.println(ex);
//            getFacesContext().addMessage(null,
//                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                            ex.getMessage(),
//                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
//        }

        // nullify the session's case so that the reload of currentCase
        // no the cecaseProfile.xhtml will trigger a new DB read
//        refreshCurrentCase();
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
         reloadCurrentInspection();
     }
     
     public void recordComplianceWithElement(OccInspectedSpaceElement inSpcEl){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.recordComplianceWithInspectedElement(    inSpcEl,
                                                        getSessionBean().getSessionUser(),
                                                        currentInspection);
            reloadCurrentInspection();
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
            oc.inspectWithoutCompliance(inSpcEl,
                                                        getSessionBean().getSessionUser(),
                                                        currentInspection);
            reloadCurrentInspection();
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
     
     public void inspectElementWithoutCompliance(OccInspectedSpaceElement inSpcEl){
         OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.inspectWithoutCompliance(inSpcEl,
                                        getSessionBean().getSessionUser(),
                                        currentInspection);
            reloadCurrentInspection();
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
     
     
     
     public void clearInspectionOfElement(OccInspectedSpaceElement inSpcEl){
         OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.clearInspectionOfElement(    inSpcEl,
                                            getSessionBean().getSessionUser(),
                                            currentInspection);
            reloadCurrentInspection();
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Inspection cleared for Space Element: " + inSpcEl.getInspectedSpaceElementID(), ""));
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

    /**
     * @return the filteredEventList
     */
    public List<OccEvent> getFilteredEventList() {
        return filteredEventList;
    }

    /**
     * @param filteredEventList the filteredEventList to set
     */
    public void setFilteredEventList(List<OccEvent> filteredEventList) {
        this.filteredEventList = filteredEventList;
    }

    /**
     * @return the selectedEventType
     */
    public EventType getSelectedEventType() {
        return selectedEventType;
    }

    /**
     * @param selectedEventType the selectedEventType to set
     */
    public void setSelectedEventType(EventType selectedEventType) {
        this.selectedEventType = selectedEventType;
    }

    /**
     * @return the availableEventTypeList
     */
    public List<EventType> getAvailableEventTypeList() {
        return availableEventTypeList;
    }

    /**
     * @param availableEventTypeList the availableEventTypeList to set
     */
    public void setAvailableEventTypeList(List<EventType> availableEventTypeList) {
        this.availableEventTypeList = availableEventTypeList;
    }

    /**
     * @return the eventCategoryList
     */
    public List<EventCategory> getEventCategoryList() {
        return eventCategoryList;
    }

    /**
     * @param eventCategoryList the eventCategoryList to set
     */
    public void setEventCategoryList(List<EventCategory> eventCategoryList) {
        this.eventCategoryList = eventCategoryList;
    }

    /**
     * @return the selectedEvent
     */
    public OccEvent getSelectedEvent() {
        return selectedEvent;
    }

    /**
     * @param selectedEvent the selectedEvent to set
     */
    public void setSelectedEvent(OccEvent selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    /**
     * @return the selectedEventCategory
     */
    public EventCategory getSelectedEventCategory() {
        return selectedEventCategory;
    }

    /**
     * @param selectedEventCategory the selectedEventCategory to set
     */
    public void setSelectedEventCategory(EventCategory selectedEventCategory) {
        this.selectedEventCategory = selectedEventCategory;
    }

    /**
     * @return the reportConfigOccInspec
     */
    public ReportConfigOccInspection getReportConfigOccInspec() {
        return reportConfigOccInspec;
    }

    /**
     * @return the reportConfigOccPermit
     */
    public ReportConfigOccPermit getReportConfigOccPermit() {
        return reportConfigOccPermit;
    }

    /**
     * @param reportConfigOccInspec the reportConfigOccInspec to set
     */
    public void setReportConfigOccInspec(ReportConfigOccInspection reportConfigOccInspec) {
        this.reportConfigOccInspec = reportConfigOccInspec;
    }

    /**
     * @param reportConfigOccPermit the reportConfigOccPermit to set
     */
    public void setReportConfigOccPermit(ReportConfigOccPermit reportConfigOccPermit) {
        this.reportConfigOccPermit = reportConfigOccPermit;
    }

    /**
     * @return the currentOccPermit
     */
    public OccPermit getCurrentOccPermit() {
        return currentOccPermit;
    }

    /**
     * @param currentOccPermit the currentOccPermit to set
     */
    public void setCurrentOccPermit(OccPermit currentOccPermit) {
        this.currentOccPermit = currentOccPermit;
    }

    /**
     * @return the selectedPerson
     */
    public Person getSelectedPerson() {
        return selectedPerson;
    }

    /**
     * @param selectedPerson the selectedPerson to set
     */
    public void setSelectedPerson(Person selectedPerson) {
        this.selectedPerson = selectedPerson;
    }
    
    
    
}
