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
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CECaseEvent;
import com.tcvcog.tcvce.entities.Choice;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitWithProp;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.ProposalOccPeriod;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.OccChecklistTemplate;
import com.tcvcog.tcvce.entities.occupancy.OccEvent;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpace;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpaceElement;
import com.tcvcog.tcvce.entities.occupancy.OccInspection;
import com.tcvcog.tcvce.entities.occupancy.OccInspectionViewOptions;
import com.tcvcog.tcvce.entities.occupancy.OccLocationDescriptor;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import com.tcvcog.tcvce.entities.occupancy.OccPermit;
import com.tcvcog.tcvce.entities.occupancy.OccSpace;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceElement;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceTypeInspectionDirective;
import com.tcvcog.tcvce.entities.reports.ReportConfigOccInspection;
import com.tcvcog.tcvce.entities.reports.ReportConfigOccPermit;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
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
    
    public static final String ADD_WITH_COMPLIANCE = "comp";
    public static final String ADD_AS_UNINSPECTED = "insp";

    private OccInspection currentInspection;
    private OccPeriod currentOccPeriod;
    private ProposalOccPeriod currentProposal;
    private PropertyUnitWithProp currentPropertyUnit;
    private Property currentProperty;
    
    private OccInspectedSpace currentInSpc;
    private OccInspectedSpaceElement currentInSpcEl;
    
    private OccLocationDescriptor currentLocation;
    private OccLocationDescriptor selectedLocation;
    private List<OccLocationDescriptor> workingLocationList;
    
    private List<OccPeriodType> occPeriodTypeList;
    private List<OccEvent> filteredEventList;
    
    private List<OccInspectedSpace> visibleInspectedSpaceList;
    private boolean includeSpacesWithNoElements;
    
    private OccSpaceTypeInspectionDirective selectedOccSpaceType;
    private OccSpace selectedOccSpace;
    
    private List<OccSpace> spacesInTypeList;
    private List<OccSpaceElement> elementsInSpaceList;
    
    private boolean markNewlyAddedSpacesWithCompliance;
    private boolean promptForSpaceLocationUponAdd;
    
    private List<User> inspectorPossibilityList;
    private User selectedInspector;
    
    private String formNoteText;
    private String formProposalRejectionReason;
    
    private List<PropertyUnit> propertyUnitCandidateList;
    private PropertyUnit selectedPropertyUnit;
    private OccPeriodType selectedOccPeriodType;
    
    // reports
    private ReportConfigOccInspection reportConfigOccInspec;
    private OccInspectionViewOptions[] itemFilterOptions;
    
    private OccPermit currentOccPermit;
    private ReportConfigOccPermit reportConfigOccPermit;
    
    // events 
    private OccEvent currentEvent;
    private List<EventType> availableEventTypeList; 
    private EventType selectedEventType;
    private List<EventCategory> eventCategoryList;
    private EventCategory selectedEventCategory;
    private List<Person> personCandidateList;
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
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        
        // set our blank lists used only by elements on this page
        spacesInTypeList = new ArrayList<>();
        visibleInspectedSpaceList = new ArrayList<>();
        
        try {
            if(getSessionBean().getSessionOccPeriod() != null){
                currentOccPeriod = oi.getOccPeriod(getSessionBean().getSessionOccPeriod().getPeriodID(), getSessionBean().getSessionUser());
                currentInspection = currentOccPeriod.determineGoverningOccInspection();
                currentPropertyUnit = pi.getPropertyUnitWithProp(currentOccPeriod.getPropertyUnitID());
                // all inspected spaces are visible by default
                currentInspection.setViewSetting(OccInspectionViewOptions.ALL_ITEMS);
            }
        
//            if(currentInspection == null){
//                if(getSessionBean().getSessionOccInspection() != null){
//                    currentInspection = getSessionBean().getSessionOccInspection();
                    // we don't really need to reload inspection from integrator
//                    try {
//                        currentInspection = oii.getOccInspection(currentInspection.getInspectionID());
//                    } catch (IntegrationException ex) {
//                        System.out.println(ex);
//                    }
//                currentOccPeriod = oi.getOccPeriod(currentInspection.getOccPeriodID(), getSessionBean().getSessionUser());
//                } else {
//                    currentOccPeriod = oi.getOccPeriod(getSessionBean().getSessionOccPeriod().getPeriodID(), getSessionBean().getSessionUser());
//                    currentInspection = oii.getOccInspection(currentOccPeriod.getInspectionList().get(0).getInspectionID());
//                    currentPropertyUnit = pi.getPropertyUnitWithProp(currentOccPeriod.getPropertyUnitID());
//                } 
//            }
            propertyUnitCandidateList = pi.getPropertyUnitList(getSessionBean().getSessionProperty());
        } catch (IntegrationException | EventException| AuthorizationException |CaseLifecycleException | ViolationException ex) {
            System.out.println(ex);
        }
        
        availableEventTypeList = oc.getPermittedEventTypes(currentOccPeriod, getSessionBean().getSessionUser());
        occPeriodTypeList = getSessionBean().getSessionMuni().getProfile().getOccPeriodTypeList();
        
        if(workingLocationList == null){
            workingLocationList = new ArrayList<>();
            try {
                workingLocationList.add(oii.getLocationDescriptor(
                        Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("locationdescriptor_implyfromspacename"))));
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        }
        
        if(personCandidateList != null){
            personCandidateList = new ArrayList<>();
            personCandidateList.addAll(getSessionBean().getSessionPersonList());
        }
        
        itemFilterOptions = OccInspectionViewOptions.values();
        
        try {
            reportConfigOccInspec =
                    oc.getOccInspectionReportConfigDefault(
                            currentInspection,
                            currentOccPeriod,
                            getSessionBean().getSessionUser());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    public void loadSpacesInType(){
        if(selectedOccSpaceType != null){
            spacesInTypeList = selectedOccSpaceType.getSpaceList();
            System.out.println("OccInspectionBB.loadSpacesInType");
        }
    }
    
    public void loadElementsInSpace(){
        if(selectedOccSpace != null){
            elementsInSpaceList = selectedOccSpace.getSpaceElementList();
            System.out.println("OccInspectionBB.loadElementsInSpace");
        }
    }
    
    /**
     * Placeholder method for the action listener on the client side button
     * @param ev 
     */
    public void viewStaticChecklistTemplate(ActionEvent ev){
        // dialog will appear clientside on complete
    }
    
    public void addAllRequiredSpaceTypes(ActionEvent ev){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        System.out.println("OccinspectionBB.addAllrequiredSpaceTypes");
          
        for(OccSpaceTypeInspectionDirective stid: currentInspection.getChecklistTemplate().getOccSpaceTypeTemplateList()){
            for(OccSpace spc: stid.getSpaceList()){
                if(spc.isRequired()){
                    try {
                        oc.inspectionAction_commenceSpaceInspection(    
                                currentInspection,
                                getSessionBean().getSessionUser(),
                                spc,
                                null);
                    } catch (IntegrationException ex) {
                        System.out.println(ex);
                    }
                }
            }
        }
        reloadCurrentInspection();
    }
    
    public void initiateOccLocationDescriptorDialog(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        currentLocation = oc.getOccLocationDescriptorSkeleton();
    }
    
    public void addNewLocationDescriptor(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        int freshLocID = 0;
        try {
            oc.addNewLocationDescriptor(currentLocation);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Created new location descriptor of ID " + freshLocID, ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                ex.getMessage(), ""));
        }
    }
    
    public void initiateOccLocationUpdate(OccLocationDescriptor old){
        currentLocation = old;
    }
    
    public void activateOccInspection(OccInspection ins){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        if(getSessionBean().getSessionUser().getKeyCard().isHasEnfOfficialPermissions()){
            try {
                
                oc.activateOccInspection(ins, selectedInspector);
                currentInspection = ins;
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Inspection ID " + ins.getInspectionID() + " is now your active inspection", ""));
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        ex.getMessage(), ""));
            }
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
    
    public void events_queuePerson(ActionEvent ev){
        if(currentEvent != null){
            currentEvent.getPersonList().add(selectedPerson);
        }
    }
    
    public void events_deQueuePersonFromEvent(Person p){
        currentEvent.getPersonList().remove(p);
    }
    
    public void proposals_initiateViewPropMetadata(ProposalOccPeriod p){
        System.out.println("OccInspectionBB.proposals_viewPropMetadata");
        currentProposal = p;
    }
    
    public void proposal_reject(Proposal p){
        ChoiceCoordinator choiceCoord = getChoiceCoordinator();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(p.getNotes());
            sb.append("\n*** Proposal Rejection Reason ***");
            sb.append(formNoteText);
            p.setNotes(sb.toString());
            
            choiceCoord.rejectProposal(p, currentOccPeriod, getSessionBean().getSessionUser());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
            "Proposal id " + p.getProposalID() + " has been rejected!", ""));
        } catch (IntegrationException | AuthorizationException | CaseLifecycleException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
            ex.getMessage(), ""));
        }
    }
    
    public void proposals_makeChoice(Choice choice, Proposal p){
        OccupancyCoordinator oc = getOccupancyCoordinator();
         System.out.println("OccInspectionBB.makeChoice");
        try {
            if(p instanceof ProposalOccPeriod){
                oc.evaluateProposal(    p, 
                                        choice, 
                                        currentOccPeriod, 
                                        getSessionBean().getSessionUser());
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "You just chose choice ID " + choice.getChoiceID() + " proposed in proposal ID " + p.getProposalID(), ""));
            }
            
        } catch (EventException | AuthorizationException | CaseLifecycleException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
            ex.getMessage(), ""));
        }
        reloadCurrentOccPeriod();
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
    
    private void reloadCurrentOccPeriod(){
        OccupancyIntegrator oi = getOccupancyIntegrator();
        try {
            oi.getOccPeriod(currentOccPeriod.getPeriodID(), getSessionBean().getSessionUser());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Reloaded occ period ID " + currentOccPeriod.getPeriodID(), ""));
        } catch (IntegrationException | EventException | AuthorizationException | CaseLifecycleException | ViolationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Unable to reload occ period", ""));
            
        }
    }
    
    public void addSpaceToChecklist(OccSpace space) {
        FacesContext fc = getFacesContext();
        String paramVal = fc.getExternalContext().getRequestParameterMap().get("occperiod-elementstatusonadd");
        System.out.println("OccInspectionBB.addSpaceToChecklist | param val: " + paramVal);
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.inspectionAction_commenceSpaceInspection(currentInspection,
                            getSessionBean().getSessionUser(),
                            space,
                            null);
            
        System.out.println("OccInspectionBB.addSpaceToChecklist | space name: " + space.getName());
        reloadCurrentInspection();
        selectedOccSpace = null;
        selectedOccSpaceType = null;
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
    
    public void filterChecklist_failedItems(ActionEvent ev){
        currentInspection.setViewSetting(OccInspectionViewOptions.FAILED_ITEMS_ONLY);
//        currentInspection.configureVisibleSpaceElementList();
    }
    
    public void filterChecklist_uninspectedItems(ActionEvent ev){
        currentInspection.setViewSetting(OccInspectionViewOptions.UNISPECTED_ITEMS_ONLY);
    }
    
    public void filterChecklist_allItems(ActionEvent ev){
        currentInspection.setViewSetting(OccInspectionViewOptions.ALL_ITEMS);
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
    public void events_initializeEvent(ActionEvent ev) {
        events_initiateNewEvent();
    }

    /**
     * Initialization of event process
     */
    public void events_initiateNewEvent() {

        if (getSelectedEventCategory() != null) {

            System.out.println("OccInspectionBB.initiateNewEvent | category: " + getSelectedEventCategory().getEventCategoryTitle());
            EventCoordinator ec = getEventCoordinator();
            try {
                currentEvent = ec.getInitializedEvent(currentOccPeriod, getSelectedEventCategory());
                currentEvent.setDateOfRecord(LocalDateTime.now());
                currentEvent.setDiscloseToMunicipality(true);
                currentEvent.setDiscloseToPublic(false);
            } catch (CaseLifecycleException ex) {
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
    
    public void initiateEventEdit(OccEvent ev){
        
    }
    
    public void events_commitEventEdits(ActionEvent ev){
        EventCoordinator ec = getEventCoordinator();
        try {
            ec.editEvent(currentEvent, getSessionBean().getSessionUser());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Successfully updated event!", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
        }
    }
    
    public void rejectProposal(){
        
    }
    
    public void initializeOccInspectionReport(ActionEvent ev){
       
    }
    
    public String reports_generateOccInspectionReport(){
        getSessionBean().setReportConfigInspection(reportConfigOccInspec);
        return "inspectionReport";
    }
    
    public void reports_initializeOccPermitReport(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        currentOccPermit = oc.getOccPermitSkeleton(getSessionBean().getSessionUser());
    }

    public String reports_generateOccPermit(OccPermit permit){
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
    public void events_attachNewEvent(ActionEvent ev) throws ViolationException {
        EventCoordinator ec = getEventCoordinator();
        CaseCoordinator cc = getCaseCoordinator();

        // category is already set from initialization sequence
        currentEvent.setOccPeriodID(currentOccPeriod.getPeriodID());
        currentEvent.setOwner(getSessionBean().getSessionUser());
//        try {
        
            // main entry point for handing the new event off to the CaseCoordinator
            // only the compliance events need to pass in another object--the violation
            // otherwise just the case and the event go to the coordinator
//            if (currentEvent.getCategory().getEventType() == EventType.Compliance) {
//                currentEvent.setEventID(cc.attachNewEventToCECase(currentCase, currentEvent, selectedViolation));
//            } else {
//                currentEvent.setEventID(cc.attachNewEventToCECase(currentCase, currentEvent, null));
//            }
//            getFacesContext().addMessage(null,
//                    new FacesMessage(FacesMessage.SEVERITY_INFO,
//                            "Successfully logged event with an ID " + currentEvent.getEventID(), ""));

            

//        } catch (IntegrationException ex) {
//            System.out.println(ex);
//            getFacesContext().addMessage(null,
//                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                            ex.getMessage(),
//                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
//        } catch (CaseLifecycleException ex) {
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
    public void checklistAction_beginInspectionMetadataEdit(ActionEvent ev){
        // do nothing since a dialog is brought up for the user
        
    }
    
     /**
      * Edits the currentInspection 
      * @param e 
      */
     public void checklistAction_editOccupancyInspectionMetadata(ActionEvent e){
         OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.updateOccInspection(currentInspection, getSessionBean().getSessionUser());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
     
     public void checklistAction_removeSpaceFromChecklist(OccInspectedSpace spc){
         OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.inspectionAction_removeSpaceFromChecklist(spc, getSessionBean().getSessionUser(), currentInspection);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Successfully removed InspectedSpace ID: " + spc.getInspectedSpaceID() , ""));
        } catch (IntegrationException ex) {
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
        }
         reloadCurrentInspection();
     }
     
     public void checklistAction_recordComplianceWithElement(OccInspectedSpaceElement inSpcEl){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.inspectionAction_recordComplianceWithInspectedElement(    inSpcEl,
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
     
     public void checklistAction_removeComplianceWithElement(OccInspectedSpaceElement inSpcEl){
         OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.inspectionAction_inspectWithoutCompliance(inSpcEl,
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
     
     public void checklistAction_inspectElementWithoutCompliance(OccInspectedSpaceElement inSpcEl){
         OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.inspectionAction_inspectWithoutCompliance(inSpcEl,
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
     
     public void checklistAction_clearInspectionOfElement(OccInspectedSpaceElement inSpcEl){
        System.out.println("OccInspectionBB.clearInspection");
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
     
     
     public void checklistAction_addNoteToInspectedElement(OccInspectedSpaceElement spcEl){
         OccInspectionIntegrator oii = getOccInspectionIntegrator();
         StringBuilder sb = new StringBuilder(spcEl.getNotes());
         sb.append(formNoteText);
         spcEl.setNotes(sb.toString());
        try {
            oii.updateInspectedSpaceElement(spcEl);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Success! Note added", ""));
        } catch (IntegrationException ex) {
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
        }
         reloadCurrentInspection();
     }
     
     public void checklistAction_recordComplianceForAllElements(ActionEvent ev){
            
     }
     
     public void checklistAction_certifyInspection(ActionEvent ev){
         OccupancyCoordinator oc = getOccupancyCoordinator();
         currentInspection.setPassedInspectionTS(LocalDateTime.now());
         currentInspection.setPassedInspectionCertifiedBy(getSessionBean().getSessionUser());
        try {
            oc.updateOccInspection(currentInspection, selectedInspector);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Success! Inspection certified as passed!", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Error! Unable to certify inspection as passed, sorry.", ""));
        }
        reloadCurrentOccPeriod();
     }
     
     /**
      * Placeholder method so the update button UI can call a method
      * @param ev 
      */
     public void initiatePropUnitUpdate(ActionEvent ev){
        // do nothing!
     }
     
     public void updateOccPeriodType(ActionEvent ev){
         
     }
     
     public void authorizeOccPeriod(ActionEvent ev){
         OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.authorizeOccPeriod(currentOccPeriod, getSessionBean().getSessionUser());
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_INFO,
               "Success! Occupancy period ID " + currentOccPeriod.getPeriodID() 
                       + " is now authorized and permits can be generated.", ""));
        } catch (AuthorizationException | CaseLifecycleException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_ERROR,
               ex.getMessage(), ""));
        }
     }
     
     public void updatePeriodPropUnit(){
         OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.updateOccPeriodPropUnit(currentOccPeriod, selectedPropertyUnit);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "The current occupancy period has been assigned to property unit ID " + selectedPropertyUnit.getUnitID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
        }
        reloadCurrentOccPeriod();
     }
     
     
     public void editLocation(OccInspectedSpace inSpace){
         
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
     * @return the spacesInTypeList
     */
    public List<OccSpace> getSpacesInTypeList() {
        
        return spacesInTypeList;
    }

    /**
     * @param spacesInTypeList the spacesInTypeList to set
     */
    public void setSpacesInTypeList(List<OccSpace> spacesInTypeList) {
        this.spacesInTypeList = spacesInTypeList;
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
     * @return the currentEvent
     */
    public OccEvent getCurrentEvent() {
        return currentEvent;
    }

    /**
     * @param currentEvent the currentEvent to set
     */
    public void setCurrentEvent(OccEvent currentEvent) {
        this.currentEvent = currentEvent;
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

    /**
     * @return the currentProposal
     */
    public ProposalOccPeriod getCurrentProposal() {
        return currentProposal;
    }

    /**
     * @param currentProposal the currentProposal to set
     */
    public void setCurrentProposal(ProposalOccPeriod currentProposal) {
        this.currentProposal = currentProposal;
    }

    /**
     * @return the occPeriodTypeList
     */
    public List<OccPeriodType> getOccPeriodTypeList() {
        return occPeriodTypeList;
    }

    /**
     * @param occPeriodTypeList the occPeriodTypeList to set
     */
    public void setOccPeriodTypeList(List<OccPeriodType> occPeriodTypeList) {
        this.occPeriodTypeList = occPeriodTypeList;
    }

    /**
     * @return the markNewlyAddedSpacesWithCompliance
     */
    public boolean isMarkNewlyAddedSpacesWithCompliance() {
        return markNewlyAddedSpacesWithCompliance;
    }

    /**
     * @return the promptForSpaceLocationUponAdd
     */
    public boolean isPromptForSpaceLocationUponAdd() {
        return promptForSpaceLocationUponAdd;
    }

    /**
     * @param markNewlyAddedSpacesWithCompliance the markNewlyAddedSpacesWithCompliance to set
     */
    public void setMarkNewlyAddedSpacesWithCompliance(boolean markNewlyAddedSpacesWithCompliance) {
        this.markNewlyAddedSpacesWithCompliance = markNewlyAddedSpacesWithCompliance;
    }

    /**
     * @param promptForSpaceLocationUponAdd the promptForSpaceLocationUponAdd to set
     */
    public void setPromptForSpaceLocationUponAdd(boolean promptForSpaceLocationUponAdd) {
        this.promptForSpaceLocationUponAdd = promptForSpaceLocationUponAdd;
    }

    /**
     * @return the selectedOccSpace
     */
    public OccSpace getSelectedOccSpace() {
        return selectedOccSpace;
    }

    /**
     * @param selectedOccSpace the selectedOccSpace to set
     */
    public void setSelectedOccSpace(OccSpace selectedOccSpace) {
        this.selectedOccSpace = selectedOccSpace;
    }

    /**
     * @return the elementsInSpaceList
     */
    public List<OccSpaceElement> getElementsInSpaceList() {
        return elementsInSpaceList;
    }

    /**
     * @param elementsInSpaceList the elementsInSpaceList to set
     */
    public void setElementsInSpaceList(List<OccSpaceElement> elementsInSpaceList) {
        this.elementsInSpaceList = elementsInSpaceList;
    }

    /**
     * @return the currentLocation
     */
    public OccLocationDescriptor getCurrentLocation() {
        return currentLocation;
    }

    /**
     * @param currentLocation the currentLocation to set
     */
    public void setCurrentLocation(OccLocationDescriptor currentLocation) {
        this.currentLocation = currentLocation;
    }

    /**
     * @return the selectedLocation
     */
    public OccLocationDescriptor getSelectedLocation() {
        return selectedLocation;
    }

    /**
     * @param selectedLocation the selectedLocation to set
     */
    public void setSelectedLocation(OccLocationDescriptor selectedLocation) {
        this.selectedLocation = selectedLocation;
    }
    
    
    /**
     * @return the workingLocationList
     */
    public List<OccLocationDescriptor> getWorkingLocationList() {
        return workingLocationList;
    }

    /**
     * @param workingLocationList the workingLocationList to set
     */
    public void setWorkingLocationList(List<OccLocationDescriptor> workingLocationList) {
        this.workingLocationList = workingLocationList;
    }

    /**
     * @return the personCandidateList
     */
    public List<Person> getPersonCandidateList() {
        return personCandidateList;
    }

    /**
     * @param personCandidateList the personCandidateList to set
     */
    public void setPersonCandidateList(List<Person> personCandidateList) {
        this.personCandidateList = personCandidateList;
    }

    /**
     * @return the formProposalRejectionReason
     */
    public String getFormProposalRejectionReason() {
        return formProposalRejectionReason;
    }

    /**
     * @param formProposalRejectionReason the formProposalRejectionReason to set
     */
    public void setFormProposalRejectionReason(String formProposalRejectionReason) {
        this.formProposalRejectionReason = formProposalRejectionReason;
    }

    /**
     * @return the visibleInspectedSpaceList
     */
    public List<OccInspectedSpace> getVisibleInspectedSpaceList() {
        return visibleInspectedSpaceList;
    }

    /**
     * @param visibleInspectedSpaceList the visibleInspectedSpaceList to set
     */
    public void setVisibleInspectedSpaceList(List<OccInspectedSpace> visibleInspectedSpaceList) {
        this.visibleInspectedSpaceList = visibleInspectedSpaceList;
    }

    /**
     * @return the includeSpacesWithNoElements
     */
    public boolean isIncludeSpacesWithNoElements() {
        return includeSpacesWithNoElements;
    }

    /**
     * @param includeSpacesWithNoElements the includeSpacesWithNoElements to set
     */
    public void setIncludeSpacesWithNoElements(boolean includeSpacesWithNoElements) {
        this.includeSpacesWithNoElements = includeSpacesWithNoElements;
    }

    /**
     * @return the selectedPropertyUnit
     */
    public PropertyUnit getSelectedPropertyUnit() {
        return selectedPropertyUnit;
    }

    /**
     * @param selectedPropertyUnit the selectedPropertyUnit to set
     */
    public void setSelectedPropertyUnit(PropertyUnit selectedPropertyUnit) {
        this.selectedPropertyUnit = selectedPropertyUnit;
    }

    /**
     * @return the propertyUnitCandidateList
     */
    public List<PropertyUnit> getPropertyUnitCandidateList() {
        return propertyUnitCandidateList;
    }

    /**
     * @param propertyUnitCandidateList the propertyUnitCandidateList to set
     */
    public void setPropertyUnitCandidateList(List<PropertyUnit> propertyUnitCandidateList) {
        this.propertyUnitCandidateList = propertyUnitCandidateList;
    }

    /**
     * @return the selectedOccPeriodType
     */
    public OccPeriodType getSelectedOccPeriodType() {
        return selectedOccPeriodType;
    }

    /**
     * @param selectedOccPeriodType the selectedOccPeriodType to set
     */
    public void setSelectedOccPeriodType(OccPeriodType selectedOccPeriodType) {
        this.selectedOccPeriodType = selectedOccPeriodType;
    }

    /**
     * @return the itemFilterOptions
     */
    public OccInspectionViewOptions[] getItemFilterOptions() {
        return itemFilterOptions;
    }

    /**
     * @param itemFilterOptions the itemFilterOptions to set
     */
    public void setItemFilterOptions(OccInspectionViewOptions[] itemFilterOptions) {
        this.itemFilterOptions = itemFilterOptions;
    }

    
     
}

//
//OccInspectedSpace visibleSpace = null;
//        List<OccInspectedSpaceElement> visibleEleList = new ArrayList<>();
//        for(Iterator<OccInspectedSpace> it = currentInspection.getInspectedSpaceList().iterator(); it.hasNext(); ){
//            OccInspectedSpace ois = it.next();
//        
//            for(Iterator<OccInspectedSpaceElement> itEle = ois.getInspectedElementList().iterator(); itEle.hasNext(); ){
//                OccInspectedSpaceElement oise = itEle.next();
//                if(oise.getComplianceGrantedTS() == null 
//                        && oise.getLastInspectedTS() == null){
//                    // we found a failed item, so add it to our visible list
//                    visibleEleList.add(oise);
//                } 
//            } // close for over inspectedSpaceelements
//            
//            visibleSpace = (OccInspectedSpace) ois.clone();
//            if(visibleSpace != null){
//                visibleSpace.setInspectedElementList(visibleEleList);
//                // only add our cloned InspectedSpace to the visible list if there
//                // are some selected elements or the user wants to see empty spaces
//                if((visibleEleList.isEmpty() && includeSpacesWithNoElements) 
//                        || !visibleEleList.isEmpty() ){
//                    visibleInspectedSpaceList.add(visibleSpace);
//                } 
//            }
//        } // close for over inspectedspaces