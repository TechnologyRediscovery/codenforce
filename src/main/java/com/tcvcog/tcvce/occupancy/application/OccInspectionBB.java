/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.ChoiceCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.InspectionException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.occupancy.OccChecklistTemplate;
import com.tcvcog.tcvce.entities.occupancy.OccEvent;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpace;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpaceElement;
import com.tcvcog.tcvce.entities.occupancy.OccInspection;
import com.tcvcog.tcvce.entities.occupancy.OccInspectionStatusEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsOccChecklistItemsEnum;
import com.tcvcog.tcvce.entities.occupancy.OccLocationDescriptor;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
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
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsEventRulesEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsProposalsEnum;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    private OccPeriodDataHeavy currentOccPeriod;
    
    private OccInspection currentInspection;
    private PropertyUnitWithProp currentPropertyUnit;
    private Property currentProperty;
    
    private OccInspectedSpace currentInSpc;
    private OccInspectedSpaceElement currentInSpcEl;
    
    private OccLocationDescriptor currentLocation;
    private OccLocationDescriptor selectedLocation;
    private List<OccLocationDescriptor> workingLocationList;
    
    private boolean periodStartDateNull;
    private boolean periodEndDateNull;
    
    private List<OccChecklistTemplate> inspectionTemplateCandidateList;
    private OccChecklistTemplate selectedInspectionTemplate;
    
    private List<OccPeriodType> occPeriodTypeList;
    
    private List<OccInspectedSpace> visibleInspectedSpaceList;
    private boolean includeSpacesWithNoElements;
    
    private OccSpaceTypeInspectionDirective selectedOccSpaceType;
    private OccSpace selectedOccSpace;
    
    private List<OccSpace> spacesInTypeList;
    private List<OccSpaceElement> elementsInSpaceList;
    
    private List<OccInspectionStatusEnum> inspectedElementAddValueCandidateList;
    private OccInspectionStatusEnum selectedInspectedElementAddValue;
    private boolean markNewlyAddedSpacesWithCompliance;
    private boolean promptForSpaceLocationUponAdd;
    
    private List<User> inspectorCandidateList;
    private User selectedInspector;
    
    private String formNoteText;
    private String formProposalRejectionReason;
    
    private List<PropertyUnit> propertyUnitCandidateList;
    private PropertyUnit selectedPropertyUnit;
    private OccPeriodType selectedOccPeriodType;
    
    // proposals
    private ProposalOccPeriod currentProposal;
    private List<ViewOptionsProposalsEnum> proposalsViewOptions;
    private ViewOptionsProposalsEnum selectedProposalsViewOption;
    
    // rules
    private EventRuleAbstract currentEventRuleAbstract;
    private List<EventRuleSet> eventRuleSetList;
    private EventRuleSet selectedEventRuleSet;
    private int formEventRuleIDToAdd;
    private boolean includeEventRuleInCurrentOccPeriodTemplate;
    private List<ViewOptionsEventRulesEnum> rulesViewOptions;
    private ViewOptionsEventRulesEnum selectedRulesViewOption;
    
    // reports
    private ReportConfigOccInspection reportConfigOccInspec;
    private List<ViewOptionsOccChecklistItemsEnum> itemFilterOptions;
    
    private OccPermit currentOccPermit;
    private ReportConfigOccPermit reportConfigOccPermit;
    
    // events 
    private OccEvent currentEvent;
    private List<Event> filteredEventList;
    private List<ViewOptionsActiveHiddenListsEnum> eventsViewOptions;
    private ViewOptionsActiveHiddenListsEnum selectedEventView;
    private List<EventType> eventTypeListUserAllowed; 
    private List<EventType> eventTypeListAll;
    private EventType selectedEventType;
    private List<EventCategory> eventCategoryListUserAllowed;
    private List<EventCategory> eventCategoryListAllActive;
    private EventCategory selectedEventCategory;
    private List<Person> personCandidateList;
    private Person selectedPerson;
    
     // payments
    private List<Payment> paymentList;
    private List<Payment> filteredPaymentList;
    private Payment selectedPayment;
    
    //fees
    private List<MoneyOccPeriodFeeAssigned> feeList;
    private List<MoneyOccPeriodFeeAssigned> filteredFeeList;
    private MoneyOccPeriodFeeAssigned selectedFee;
    
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
        EventCoordinator ec = getEventCoordinator();
        
        // set our blank lists used only by elements on this page
        spacesInTypeList = new ArrayList<>();
        visibleInspectedSpaceList = new ArrayList<>();
        OccPeriod sessionPeriod = getSessionBean().getSessionOccPeriod();
        try {
            if(sessionPeriod != null){
                if(!(sessionPeriod instanceof OccPeriodDataHeavy) || currentOccPeriod == null){
                    currentOccPeriod = oi.generateOccPeriodDataHeavy(sessionPeriod);
                }
                if(currentOccPeriod.getConfiguredTS() == null){
                    currentOccPeriod = oc.configureOccPeriodDataHeavy(currentOccPeriod, getSessionBean().getSessionUser());
                }
                currentPropertyUnit = pi.getPropertyUnitWithProp(currentOccPeriod.getPropertyUnitID());
                currentInspection = currentOccPeriod.getGoverningInspection();
                // all inspected spaces are visible by default
                if(currentInspection != null){
                    currentInspection.setViewSetting(ViewOptionsOccChecklistItemsEnum.ALL_ITEMS);
                }
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
        } catch (IntegrationException | EventException| AuthorizationException |CaseLifecycleException ex) {
            System.out.println(ex);
        }
        
        
        // general setting of drop-down box lists
        try {
            eventTypeListUserAllowed = ec.getPermittedEventTypesForOcc(currentOccPeriod, getSessionBean().getSessionUser());
            eventTypeListAll = new ArrayList();
            eventTypeListAll = ec.getEventTypesAll();
            eventCategoryListAllActive = ec.getEventCategoryListActive();
            occPeriodTypeList = getSessionBean().getSessionMuni().getProfile().getOccPeriodTypeList();
            currentEventRuleAbstract = ec.rules_getInitializedEventRuleAbstract();
            eventRuleSetList = ec.rules_getEventRuleSetList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
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
        
        
        itemFilterOptions = Arrays.asList(ViewOptionsOccChecklistItemsEnum.values());
        inspectedElementAddValueCandidateList = Arrays.asList(OccInspectionStatusEnum.values());
        
        proposalsViewOptions = Arrays.asList(ViewOptionsProposalsEnum.values());
        selectedProposalsViewOption = ViewOptionsProposalsEnum.VIEW_ALL;
        
        eventsViewOptions = Arrays.asList(ViewOptionsActiveHiddenListsEnum.values());
        selectedEventView = ViewOptionsActiveHiddenListsEnum.VIEW_ALL;
        
        rulesViewOptions = Arrays.asList(ViewOptionsEventRulesEnum.values());
        selectedRulesViewOption = ViewOptionsEventRulesEnum.VIEW_ALL;
        
        
        try {
            inspectionTemplateCandidateList = oii.getChecklistTemplateList(getSessionBean().getSessionMuni());
            reportConfigOccInspec =
                    oc.getOccInspectionReportConfigDefault(
                            currentInspection,
                            currentOccPeriod,
                            getSessionBean().getSessionUser());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        periodEndDateNull = false;
        periodStartDateNull = false;
        
        feeList = currentOccPeriod.getFeeList();
        paymentList = currentOccPeriod.getPaymentList();
        
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

    // TODO: finish me
    public void deletePhoto(int photoID){
        
    }
    
    /**
     * Placeholder method for the action listener on the client side button
     * @param ev 
     */
    public void viewStaticChecklistTemplate(ActionEvent ev){
        // dialog will appear clientside on complete
    }
    
    public void checklistAction_addAllRequiredSpaceTypes(ActionEvent ev){
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
                                selectedInspectedElementAddValue,
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
    
    public void initializeSpaceAdd(ActionEvent ev){
        // does this need to be nothing?
        
    }
    
    public void checklistAction_activateOccInspection(OccInspection ins){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        if(getSessionBean().getSessionUser().getMyCredential().isHasEnfOfficialPermissions()){
            try {
                
                oc.activateOccInspection(ins);
                currentInspection = ins;
                reloadCurrentInspection();
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
            eventCategoryListUserAllowed = ei.getEventCategoryList(selectedEventType);
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
         System.out.println("OccInspectionBB.proposals_makeChoice");
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
        reloadCurrentOccPeriodDataHeavy();
    }    
    
    public void proposals_clearProposal(Proposal p){
        ChoiceCoordinator cc = getChoiceCoordinator();
         System.out.println("OccInspectionBB.clearChoice");
        try {
            cc.clearProposalEvaluation(p, getSessionBean().getSessionUser());
            
        } catch (CaseLifecycleException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
            ex.getMessage(), ""));
        }
        reloadCurrentOccPeriodDataHeavy();
    }    
    
    private void reloadCurrentInspection(){
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        try {
            if(currentInspection != null){
                currentInspection = oii.getOccInspection(currentInspection.getInspectionID());
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Reloaded inspection ID " + currentInspection.getInspectionID(), ""));
            }
        } catch (IntegrationException ex) {
            System.out.println(ex);
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Unable to reload inspection", ""));
        }
    }
    
    private void reloadCurrentOccPeriodDataHeavy(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            currentOccPeriod = oc.reloadOccPeriod(currentOccPeriod, getSessionBean().getSessionUser());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Reloaded occ period ID " + currentOccPeriod.getPeriodID(), ""));
        } catch (IntegrationException | EventException | AuthorizationException | CaseLifecycleException | ViolationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Unable to reload occ period", ""));
        }
    }
    
    public void markInspectionAsGoverning(OccInspection insp){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.activateOccInspection(insp);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Success! Activated inspection ID: " + insp.getInspectionID(), ""));
        } catch (IntegrationException ex) {
            
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
        }
    }
    
    public void checklistAction_addSpaceToChecklist(OccSpace space) {
        FacesContext fc = getFacesContext();
        String paramVal = fc.getExternalContext().getRequestParameterMap().get("occperiod-elementstatusonadd");
        System.out.println("OccInspectionBB.addSpaceToChecklist | param val: " + paramVal);
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.inspectionAction_commenceSpaceInspection(currentInspection,
                            getSessionBean().getSessionUser(),
                            space,
                            selectedInspectedElementAddValue,
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
        currentInspection.setViewSetting(ViewOptionsOccChecklistItemsEnum.FAILED_ITEMS_ONLY);
//        currentInspection.configureVisibleSpaceElementList();
    }
    
    public void filterChecklist_uninspectedItems(ActionEvent ev){
        currentInspection.setViewSetting(ViewOptionsOccChecklistItemsEnum.UNISPECTED_ITEMS_ONLY);
    }
    
    public void filterChecklist_allItems(ActionEvent ev){
        currentInspection.setViewSetting(ViewOptionsOccChecklistItemsEnum.ALL_ITEMS);
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
    
    public void events_initiateEventEdit(OccEvent ev){
        currentEvent = ev;
        System.out.println("OccInspectionBB.events_initiateEventEdit | current event: " + currentEvent.getEventID());
    }
    
    public void rules_initiateEventRuleCreate(ActionEvent ev){
        EventCoordinator ec = getEventCoordinator();
        currentEventRuleAbstract = ec.rules_getInitializedEventRuleAbstract();
    }
    
    public void rules_initiateEventRuleEdit(EventRuleAbstract era){
        currentEventRuleAbstract = era;
        
    }
    
    public void rules_commitEventRuleEdits(ActionEvent ev){
        EventCoordinator ec = getEventCoordinator();
        try {
            ec.rules_updateEventRuleAbstract(currentEventRuleAbstract);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Update of event rule successful!", ""));
            reloadCurrentOccPeriodDataHeavy();
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                ex.getMessage(), ""));
        }
    }
    
    public void rules_commitEventRuleCreate(ActionEvent ev){
        EventCoordinator ec = getEventCoordinator();
        int freshEventRuleID;
        try {
            freshEventRuleID = ec.rules_createEventRuleAbstract(currentEventRuleAbstract, 
                                                                currentOccPeriod, 
                                                                null, 
                                                                includeEventRuleInCurrentOccPeriodTemplate,
                                                                getSessionBean().getSessionUser());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "New event rule added with ID " + freshEventRuleID, ""));
            System.out.println("OccInspectionBB.commiteventRuleCreate");
            reloadCurrentOccPeriodDataHeavy();
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
        }
    }
    
    
    public void rules_addEventRuleByID(ActionEvent ev){
        EventCoordinator ec = getEventCoordinator();
        try {
            EventRuleAbstract era = ec.rules_getEventRuleAbstract(formEventRuleIDToAdd);
            ec.rules_attachEventRule(era, currentOccPeriod, getSessionBean().getSessionUser());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Success! added rule to occ period", ""));
        } catch (IntegrationException | CaseLifecycleException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
            
        }
    }
    
    public void rules_addEventRuleSet(EventRuleSet ers){
        EventCoordinator ec = getEventCoordinator();
        try {
            ec.rules_attachRuleSet(ers, currentOccPeriod, getSessionBean().getSessionUser());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Success! added rule set to occ period", ""));
        } catch (IntegrationException | CaseLifecycleException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
            
        }
        
    }
    
    public void events_loadEventCategories() throws IntegrationException{
        System.out.println("OccInspectionBB.loadEventCategories | selected type: " + selectedEventType);
        EventCoordinator ec = getEventCoordinator();
        eventCategoryListUserAllowed = ec.loadEventCategoryListUserAllowed(selectedEventType, getSessionBean().getSessionUser());
        
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
        // TODO: Finish my guts
        
    }
    
    public void checklistAction_implementNewInspectionChecklist(ActionEvent ev){
        if(selectedInspectionTemplate != null){
            OccupancyCoordinator oc = getOccupancyCoordinator();
            try {
                oc.activateOccInspection(oc.inspectionAction_commenceOccupancyInspection(null, selectedInspectionTemplate, currentOccPeriod, getSessionBean().getSessionUser()));
                reloadCurrentOccPeriodDataHeavy();
                reloadCurrentInspection();
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Success! Created new inspection.", ""));
            } catch (InspectionException | IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
            }
        } else {
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please select an inspection template!", ""));
        }
    }
    
    public void initiateNoteOnInspection(ActionEvent ev){
        formNoteText = null;
    }
    
    public void attachNoteToInspection(ActionEvent ev){
                 OccInspectionIntegrator oii = getOccInspectionIntegrator();
         if(currentInspection != null){
            StringBuilder sb = new StringBuilder();
            if(currentInspection.getNotes() !=null){
                sb.append(currentInspection.getNotes());
                sb.append("<br />****************<br />");
            }
            sb.append(formNoteText);
            currentInspection.setNotes(sb.toString());
           try {
               oii.updateOccInspection(currentInspection);
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
        
    }
    
    /**
     * Utility method called when the user begins inspection report process.
     * The report object is put in place on the bean during init()
     * @param ev 
     */
    public void reports_initializeOccInspectionReport(ActionEvent ev){
       // go ahead!
    }
    
    public String reports_generateOccInspectionReport(){
        reloadCurrentInspection();
        currentOccPeriod.setGoverningInspection(currentInspection);
        reportConfigOccInspec.setOccPeriod(currentOccPeriod);
        reportConfigOccInspec.setPropUnitWithProp(currentPropertyUnit);
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
     */
    public void events_attachNewEvent(ActionEvent ev) {
        OccupancyCoordinator oc = getOccupancyCoordinator();

        // category is already set from initialization sequence
        currentEvent.setOwner(getSessionBean().getSessionUser());
        try {
        
//             main entry point for handing the new event off to the CaseCoordinator
//             only the compliance events need to pass in another object--the violation
//             otherwise just the case and the event go to the coordinator
            oc.attachNewEventToOccPeriod(currentOccPeriod, currentEvent, getSessionBean().getSessionUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully logged event with an ID " + currentEvent.getEventID(), ""));

        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(),
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
        } 

//         nullify the session's case so that the reload of currentCase
//         no the cecaseProfile.xhtml will trigger a new DB read
        reloadCurrentOccPeriodDataHeavy();
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
     
     public void checklistAction_initiateNoteOnInspectedElement(OccInspectedSpaceElement spcEl){
         formNoteText = null;
         currentInSpcEl = spcEl;
         
     }
     
     public void checklistAction_addNoteToInspectedElement(){
         OccInspectionIntegrator oii = getOccInspectionIntegrator();
         if(currentInSpcEl != null){
            StringBuilder sb = new StringBuilder();
            if(currentInSpcEl.getNotes() !=null){
                sb.append(currentInSpcEl.getNotes());
                sb.append("****************<br />");
            }
            sb.append(formNoteText);
            currentInSpcEl.setNotes(sb.toString());
           try {
               oii.updateInspectedSpaceElement(currentInSpcEl);
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
        reloadCurrentOccPeriodDataHeavy();
     }
     
     /**
      * Placeholder method so the update button UI can call a method
      * @param ev 
      */
     public void initiatePropUnitUpdate(ActionEvent ev){
        // do nothing!
     }
     

     
     public void certifyOccPeriodField(ActionEvent ev){
         
         OccupancyCoordinator oc = getOccupancyCoordinator();
         FacesContext context = getFacesContext();
         String field = context.getExternalContext().getRequestParameterMap().get("fieldtocertify");
         String certifymode = context.getExternalContext().getRequestParameterMap().get("certifymode");
         
         System.out.println("OccInspectionBB.certifuyOccPeriodField | field: " + field + " | mode: " + certifymode);
         
         User u = getSessionBean().getSessionUser();
         LocalDateTime now = LocalDateTime.now();
         
         switch(field){
            case "authorization":
                currentOccPeriod.setAuthorizedBy(u);
                currentOccPeriod.setAuthorizedTS(now);
                if(certifymode.equals("withdraw")){
                    currentOccPeriod.setAuthorizedBy(null);
                    currentOccPeriod.setAuthorizedTS(null);
                }
                break;
            case "occperiodtype":
                currentOccPeriod.setPeriodTypeCertifiedBy(u);
                currentOccPeriod.setPeriodTypeCertifiedTS(now);
                if(certifymode.equals("withdraw")){
                    currentOccPeriod.setPeriodTypeCertifiedBy(null);
                    currentOccPeriod.setPeriodTypeCertifiedTS(null);
                }
                break;
            case "startdate":
                if(periodStartDateNull){
                    currentOccPeriod.setStartDate(null);
                }
                currentOccPeriod.setStartDateCertifiedBy(u);
                currentOccPeriod.setStartDateCertifiedTS(now);
                if(certifymode.equals("withdraw")){
                    currentOccPeriod.setStartDateCertifiedBy(null);
                    currentOccPeriod.setStartDateCertifiedTS(null);
                }
                break;
            case "enddate":
                if(periodEndDateNull){
                    currentOccPeriod.setEndDate(null);
                }
                currentOccPeriod.setEndDateCertifiedBy(u);
                currentOccPeriod.setEndDateCertifiedTS(now);
                if(certifymode.equals("withdraw")){
                    currentOccPeriod.setEndDateCertifiedBy(null);
                    currentOccPeriod.setEndDateCertifiedTS(null);
                }
                break;
            default:
                getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Error! Unable to certify field", ""));
         }
         
        try {
            oc.updateOccPeriod(currentOccPeriod, u);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Successfully udpated field status!", ""));
        } catch (IntegrationException ex) {
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
        }
        reloadCurrentOccPeriodDataHeavy();
         
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
        reloadCurrentOccPeriodDataHeavy();
     }
     
     public String editOccPeriodPayments(){
         getSessionBean().setSessionOccPeriod(currentOccPeriod);
         getSessionBean().setPaymentRedirTo("occPeriodPayments");
         
         return "payments";
     }
     
     public String editOnePayment(Payment thisPayment){
         getSessionBean().setSessionPayment(thisPayment);
         getSessionBean().setPaymentRedirTo("occPeriodPayments");
         
         return "payments";
     }
     
     public String editOccPeriodFees(){
         getSessionBean().setFeeManagementOccPeriod(currentOccPeriod);
         getSessionBean().setFeeRedirTo("occPeriodPayments");
         
         return "editFees";
     }
     
     
     public void editLocation(OccInspectedSpace inSpace){
         
     }
     
     /**
      * utility pass through method to be called when loading Occperiod advanced settings
      * @param ev 
      */
     public void updateOccPeriodInitialize(ActionEvent ev){
         
     }
     
     public void updateOccPeriodCommit(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.updateOccPeriod(currentOccPeriod, getSessionBean().getSessionUser());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Update successful on OccPeriod ID: " + currentOccPeriod.getPeriodID(), ""));
        } catch (IntegrationException ex) {
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
        }
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
    public void updateOccInspectionCommitChanges(ActionEvent e){
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
     * @return the inspectorCandidateList
     */
    public List<User> getInspectorCandidateList() {
        return inspectorCandidateList;
    }

    /**
     * @param inspectorCandidateList the inspectorCandidateList to set
     */
    public void setInspectorCandidateList(List<User> inspectorCandidateList) {
        this.inspectorCandidateList = inspectorCandidateList;
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
    public void setCurrentOccPeriod(OccPeriodDataHeavy currentOccPeriod) {
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
    public List<Event> getFilteredEventList() {
        return filteredEventList;
    }

    /**
     * @param filteredEventList the filteredEventList to set
     */
    public void setFilteredEventList(List<Event> filteredEventList) {
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
     * @return the eventTypeListUserAllowed
     */
    public List<EventType> getEventTypeListUserAllowed() {
        return eventTypeListUserAllowed;
    }

    /**
     * @param eventTypeListUserAllowed the eventTypeListUserAllowed to set
     */
    public void setEventTypeListUserAllowed(List<EventType> eventTypeListUserAllowed) {
        this.eventTypeListUserAllowed = eventTypeListUserAllowed;
    }

    /**
     * @return the eventCategoryListUserAllowed
     */
    public List<EventCategory> getEventCategoryListUserAllowed() {
        return eventCategoryListUserAllowed;
    }

    /**
     * @param eventCategoryListUserAllowed the eventCategoryListUserAllowed to set
     */
    public void setEventCategoryListUserAllowed(List<EventCategory> eventCategoryListUserAllowed) {
        this.eventCategoryListUserAllowed = eventCategoryListUserAllowed;
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
    public List<ViewOptionsOccChecklistItemsEnum> getItemFilterOptions() {
        return itemFilterOptions;
    }

    /**
     * @param itemFilterOptions the itemFilterOptions to set
     */
    public void setItemFilterOptions(List<ViewOptionsOccChecklistItemsEnum> itemFilterOptions) {
        this.itemFilterOptions = itemFilterOptions;
    }

    /**
     * @return the inspectionTemplateCandidateList
     */
    public List<OccChecklistTemplate> getInspectionTemplateCandidateList() {
        return inspectionTemplateCandidateList;
    }

    /**
     * @param inspectionTemplateCandidateList the inspectionTemplateCandidateList to set
     */
    public void setInspectionTemplateCandidateList(List<OccChecklistTemplate> inspectionTemplateCandidateList) {
        this.inspectionTemplateCandidateList = inspectionTemplateCandidateList;
    }

    /**
     * @return the selectedInspectionTemplate
     */
    public OccChecklistTemplate getSelectedInspectionTemplate() {
        return selectedInspectionTemplate;
    }

    /**
     * @param selectedInspectionTemplate the selectedInspectionTemplate to set
     */
    public void setSelectedInspectionTemplate(OccChecklistTemplate selectedInspectionTemplate) {
        this.selectedInspectionTemplate = selectedInspectionTemplate;
    }

    /**
     * @return the selectedInspectedElementAddValue
     */
    public OccInspectionStatusEnum getSelectedInspectedElementAddValue() {
        return selectedInspectedElementAddValue;
    }

    /**
     * @param selectedInspectedElementAddValue the selectedInspectedElementAddValue to set
     */
    public void setSelectedInspectedElementAddValue(OccInspectionStatusEnum selectedInspectedElementAddValue) {
        this.selectedInspectedElementAddValue = selectedInspectedElementAddValue;
    }

    /**
     * @return the inspectedElementAddValueCandidateList
     */
    public List<OccInspectionStatusEnum> getInspectedElementAddValueCandidateList() {
        return inspectedElementAddValueCandidateList;
    }

    /**
     * @param inspectedElementAddValueCandidateList the inspectedElementAddValueCandidateList to set
     */
    public void setInspectedElementAddValueCandidateList(List<OccInspectionStatusEnum> inspectedElementAddValueCandidateList) {
        this.inspectedElementAddValueCandidateList = inspectedElementAddValueCandidateList;
    }

    /**
     * @return the periodStartDateNull
     */
    public boolean isPeriodStartDateNull() {
        return periodStartDateNull;
    }

    /**
     * @return the periodEndDateNull
     */
    public boolean isPeriodEndDateNull() {
        return periodEndDateNull;
    }

    /**
     * @param periodStartDateNull the periodStartDateNull to set
     */
    public void setPeriodStartDateNull(boolean periodStartDateNull) {
        this.periodStartDateNull = periodStartDateNull;
    }

    /**
     * @param periodEndDateNull the periodEndDateNull to set
     */
    public void setPeriodEndDateNull(boolean periodEndDateNull) {
        this.periodEndDateNull = periodEndDateNull;
    }

    /**
     * @return the formEventRuleIDToAdd
     */
    public int getFormEventRuleIDToAdd() {
        return formEventRuleIDToAdd;
    }

    /**
     * @param formEventRuleIDToAdd the formEventRuleIDToAdd to set
     */
    public void setFormEventRuleIDToAdd(int formEventRuleIDToAdd) {
        this.formEventRuleIDToAdd = formEventRuleIDToAdd;
    }

    /**
     * @return the currentEventRuleAbstract
     */
    public EventRuleAbstract getCurrentEventRuleAbstract() {
        return currentEventRuleAbstract;
    }

    /**
     * @param currentEventRuleAbstract the currentEventRuleAbstract to set
     */
    public void setCurrentEventRuleAbstract(EventRuleAbstract currentEventRuleAbstract) {
        this.currentEventRuleAbstract = currentEventRuleAbstract;
    }

    /**
     * @return the includeEventRuleInCurrentOccPeriodTemplate
     */
    public boolean isIncludeEventRuleInCurrentOccPeriodTemplate() {
        return includeEventRuleInCurrentOccPeriodTemplate;
    }

    /**
     * @param includeEventRuleInCurrentOccPeriodTemplate the includeEventRuleInCurrentOccPeriodTemplate to set
     */
    public void setIncludeEventRuleInCurrentOccPeriodTemplate(boolean includeEventRuleInCurrentOccPeriodTemplate) {
        this.includeEventRuleInCurrentOccPeriodTemplate = includeEventRuleInCurrentOccPeriodTemplate;
    }

    /**
     * @return the eventCategoryListAllActive
     */
    public List<EventCategory> getEventCategoryListAllActive() {
        return eventCategoryListAllActive;
    }

    /**
     * @param eventCategoryListAllActive the eventCategoryListAllActive to set
     */
    public void setEventCategoryListAllActive(List<EventCategory> eventCategoryListAllActive) {
        this.eventCategoryListAllActive = eventCategoryListAllActive;
    }

    /**
     * @return the eventTypeListAll
     */
    public List<EventType> getEventTypeListAll() {
        return eventTypeListAll;
    }

    /**
     * @param eventTypeListAll the eventTypeListAll to set
     */
    public void setEventTypeListAll(List<EventType> eventTypeListAll) {
        this.eventTypeListAll = eventTypeListAll;
    }

    /**
     * @return the eventsViewOptions
     */
    public List<ViewOptionsActiveHiddenListsEnum> getEventsViewOptions() {
        return eventsViewOptions;
    }

    /**
     * @return the selectedEventView
     */
    public ViewOptionsActiveHiddenListsEnum getSelectedEventView() {
        return selectedEventView;
    }

    /**
     * @param selectedEventView the selectedEventView to set
     */
    public void setSelectedEventView(ViewOptionsActiveHiddenListsEnum selectedEventView) {
        this.selectedEventView = selectedEventView;
    }

    /**
     * @return the proposalsViewOptions
     */
    public List<ViewOptionsProposalsEnum> getProposalsViewOptions() {
        return proposalsViewOptions;
    }

    /**
     * @return the selectedProposalsViewOption
     */
    public ViewOptionsProposalsEnum getSelectedProposalsViewOption() {
        return selectedProposalsViewOption;
    }

    /**
     * @return the rulesViewOptions
     */
    public List<ViewOptionsEventRulesEnum> getRulesViewOptions() {
        return rulesViewOptions;
    }

    /**
     * @return the selectedRulesViewOption
     */
    public ViewOptionsEventRulesEnum getSelectedRulesViewOption() {
        return selectedRulesViewOption;
    }

    /**
     * @param proposalsViewOptions the proposalsViewOptions to set
     */
    public void setProposalsViewOptions(List<ViewOptionsProposalsEnum> proposalsViewOptions) {
        this.proposalsViewOptions = proposalsViewOptions;
    }

    /**
     * @param selectedProposalsViewOption the selectedProposalsViewOption to set
     */
    public void setSelectedProposalsViewOption(ViewOptionsProposalsEnum selectedProposalsViewOption) {
        this.selectedProposalsViewOption = selectedProposalsViewOption;
    }

    /**
     * @param rulesViewOptions the rulesViewOptions to set
     */
    public void setRulesViewOptions(List<ViewOptionsEventRulesEnum> rulesViewOptions) {
        this.rulesViewOptions = rulesViewOptions;
    }

    /**
     * @param selectedRulesViewOption the selectedRulesViewOption to set
     */
    public void setSelectedRulesViewOption(ViewOptionsEventRulesEnum selectedRulesViewOption) {
        this.selectedRulesViewOption = selectedRulesViewOption;
    }

    /**
     * @return the eventRuleSetList
     */
    public List<EventRuleSet> getEventRuleSetList() {
        return eventRuleSetList;
    }

    /**
     * @param eventRuleSetList the eventRuleSetList to set
     */
    public void setEventRuleSetList(List<EventRuleSet> eventRuleSetList) {
        this.eventRuleSetList = eventRuleSetList;
    }

    /**
     * @return the selectedEventRuleSet
     */
    public EventRuleSet getSelectedEventRuleSet() {
        return selectedEventRuleSet;
    }

    /**
     * @param selectedEventRuleSet the selectedEventRuleSet to set
     */
    public void setSelectedEventRuleSet(EventRuleSet selectedEventRuleSet) {
        this.selectedEventRuleSet = selectedEventRuleSet;
    }

    /**
     * @return the filteredPaymentList
     */
    public List<Payment> getFilteredPaymentList() {
        return filteredPaymentList;
    }

    /**
     * @return the selectedPayment
     */
    public Payment getSelectedPayment() {
        return selectedPayment;
    }

    /**
     * @return the filteredFeeList
     */
    public List<MoneyOccPeriodFeeAssigned> getFilteredFeeList() {
        return filteredFeeList;
    }

    /**
     * @return the selectedFee
     */
    public MoneyOccPeriodFeeAssigned getSelectedFee() {
        return selectedFee;
    }

    public List<Payment> getPaymentList() {
        return paymentList;
    }

    public void setPaymentList(List<Payment> paymentList) {
        this.paymentList = paymentList;
    }
    
    /**
     * @param filteredPaymentList the filteredPaymentList to set
     */
    public void setFilteredPaymentList(List<Payment> filteredPaymentList) {
        this.filteredPaymentList = filteredPaymentList;
    }

    /**
     * @param selectedPayment the selectedPayment to set
     */
    public void setSelectedPayment(Payment selectedPayment) {
        this.selectedPayment = selectedPayment;
    }

    public List<MoneyOccPeriodFeeAssigned> getFeeList() {
        return feeList;
    }

    public void setFeeList(List<MoneyOccPeriodFeeAssigned> feeList) {
        this.feeList = feeList;
    }
    
    /**
     * @param filteredFeeList the filteredFeeList to set
     */
    public void setFilteredFeeList(List<MoneyOccPeriodFeeAssigned> filteredFeeList) {
        this.filteredFeeList = filteredFeeList;
    }

    /**
     * @param selectedFee the selectedFee to set
     */
    public void setSelectedFee(MoneyOccPeriodFeeAssigned selectedFee) {
        this.selectedFee = selectedFee;
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