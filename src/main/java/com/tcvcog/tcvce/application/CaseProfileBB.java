/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.ChoiceCoordinator;
import com.tcvcog.tcvce.coordinators.DataCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;    
import com.tcvcog.tcvce.domain.PermissionsException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECase;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECaseList;
import com.tcvcog.tcvce.entities.search.Query;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import com.tcvcog.tcvce.entities.search.SearchParamsCECase;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.ViolationIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import org.primefaces.model.chart.DonutChartModel;

/**
 *
 * @author Eric C. Darsow
 */
public class CaseProfileBB extends BackingBeanUtils implements Serializable {

    // deprecated member for use by CaseProfile.xhtml
    private CECase currentCase;
    private CasePhase nextPhase;
    private CasePhase[] casePhaseList;
    private CasePhase selectedCasePhase;
    private CaseStage[] caseStageArray;

    private DonutChartModel violationDonut;
    
    private List<CECase> caseList;
    private List<CECase> filteredCaseList;
    private SearchParamsCECase searchParams;
    
    private List<QueryCECase> queryList;
    private QueryCECase selectedCECaseQuery;
    private Query selectedBOBQuery;
    
    private List<CECase> filteredCaseHistoryList;
    private List<CECaseEvent> recentEventList;
    private List<Person> muniPeopleList;

    private CECaseEvent eventForTriggeringCasePhaseAdvancement;
    private CECaseEvent triggeringEventForProposal;

    private List<CECaseEvent> filteredEventList;
    private CECaseEvent selectedEvent;

    private boolean allowedToClearActionResponse;

    private List<CodeViolation> selectedViolations;
    private CodeViolation selectedViolation;
    private int newViolationCodeBookEleID;

    private List<Citation> citationList;
    private Citation selectedCitation;

    private HashMap<CasePhase, String> imageFilenameMap;
    private String phaseDiagramImageFilename;

    // add currentEvent form fields
    private List<EventCategory> eventCategoryList;

    private EventCategory selectedEventCategory;
    private EventType selectedEventType;
    private List<EventType> availableEventList;

    private Person selectedPerson;

    private boolean includeActionRequest;
    private List<EventCategory> availableActionsToRequest;

    private String styleClassStatusIcon;

    private String styleClassInvestigation;
    private String styleClassEnforcement;
    private String sytleClassCitation;
    private String sytleClassClosed;
    private String styleClassActionRequestIcon;

//    reports
    private ReportConfigCECase reportCECase;
    private ReportConfigCECaseList reportCECaseList;

    /**
     * Creates a new instance of CaseManageBB
     */
    public CaseProfileBB() {
       
    }

    
    /**
     * Configures the cECases.xhtml page by loading our case Queues
     */
    @PostConstruct
    public void initBean() {
        SearchCoordinator sc = getSearchCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        CaseIntegrator ci = getCaseIntegrator();
        
        queryList = sc.buildQueryCECaseList(getSessionBean().getSessionMuni(), getSessionBean().getSessionUser());
        selectedCECaseQuery = getSessionBean().getQueryCECase();
        searchParams = selectedCECaseQuery.getSearchParamsList().get(0);
        if(!selectedCECaseQuery.isExecutedByIntegrator()){
            try {
                sc.runQuery(selectedCECaseQuery);
            } catch (IntegrationException | CaseLifecycleException ex) {
                System.out.println(ex);
            }
        }
        
        caseList = selectedCECaseQuery.getResults();
        currentCase = getSessionBean().getSessionCECase();
        
//        if (currentCase == null && !caseList.isEmpty()) {
//            currentCase = caseList.get(0);
//        } else if (currentCase == null){
//            currentCase = getSessionBean().getSessionCECase();
//        }
        
//        generateViolationDonut();
//        refreshCurrentCase();

        ReportConfigCECase rpt = getSessionBean().getReportConfigCECase();
        if (rpt != null) {
            rpt.setTitle("Code Enforcement Case Summary");
            reportCECase = rpt;
        }

        ReportConfigCECaseList listRpt = getSessionBean().getReportConfigCECaseList();
        if (listRpt != null) {
            reportCECaseList = listRpt;
        } else {
            reportCECaseList = cc.getDefaultReportConfigCECaseList();
        }
    }
    
    public String viewCasePropertyProfile(){
        getSessionBean().getSessionPropertyList().add(0, currentCase.getProperty());
        positionCurrentCaseAtHeadOfQueue();
        return "properties";
    }

   private void generateViolationDonut(){
       DataCoordinator dc = getDataCoordinator();
       if(caseList != null && caseList.size() >= 1){
           DonutChartModel d = new DonutChartModel();
           d.addCircle(dc.computeViolationFrequencyStringMap(caseList));
           
           d.setTitle("Violation frequency: all listed cases");
           d.setLegendPosition("s");
           d.setShowDataLabels(true);
           d.setShowDatatip(true);
           d.setSeriesColors(sytleClassClosed);
            violationDonut = d;
          
       }
   }
   
   public void updateCase(ActionEvent ev){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.updateCoreCECaseData(currentCase);
            cc.refreshCase(currentCase);
        } catch (CaseLifecycleException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        ex.getMessage(),
                        "Please try again with a revised origination date"));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        ex.getMessage(),
                        "This issue must be corrected by a system administrator, sorry"));
        }
        
        getFacesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Case details updated!",""));
        
    }

    
    
    
    private void positionCurrentCaseAtHeadOfQueue(){
        getSessionBean().getSessionCECaseList().remove(currentCase);
        getSessionBean().getSessionCECaseList().add(0, currentCase);
        getSessionBean().setSessionCECase(currentCase);
    }

   
    
    public void hideEvent(CECaseEvent event){
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

    
    public void deletePhoto(int blobID){
        // TODO: remove entry from linker table for deleted photos
        for(Integer pid : this.selectedViolation.getBlobIDList()){
            if(pid.compareTo(blobID) == 0){
                this.selectedViolation.getBlobIDList().remove(pid);
                break;
            }
        }
        BlobCoordinator blobc = getBlobCoordinator();
        try {
            blobc.deleteBlob(blobID);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    
    public void executeQuery(){
        SearchCoordinator sc = getSearchCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        int listSize = 0;
        
        try {
            caseList = sc.runQuery(selectedCECaseQuery).getResults();
            if (caseList != null) {
                listSize = caseList.size();
            }
            generateViolationDonut();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Your query completed with " + listSize + " results", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not query the database, sorry.", ""));
        } catch (CaseLifecycleException ex) {
            System.out.println(ex);
        }
    }
    
    

    /**
     * Responder to the query button on the UI
     *
     * @param ev
     */
    public void executeQuery(ActionEvent ev) {
        System.out.println("CaseProfileBB.executeQuery");
        executeQuery();
    }
    
    public void refreshCaseList(ActionEvent ev){
        caseList = null;
    }
    

    public String generateReportCECase() {
        CaseCoordinator cc = getCaseCoordinator();
        positionCurrentCaseAtHeadOfQueue();

        reportCECase.setCse(currentCase);

        reportCECase.setCreator(getSessionBean().getSessionUser());
        reportCECase.setMuni(getSessionBean().getSessionMuni());
        reportCECase.setGenerationTimestamp(LocalDateTime.now());

        try {
            reportCECase = cc.transformCECaseForReport(reportCECase);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        } catch (CaseLifecycleException ex) {
            System.out.println(ex);
        }

        getSessionBean().setReportConfigCECase(reportCECase);
        // this is for use by the report header to have a super class with only
        // the basic info. reportingBB exposes it to the faces page
        getSessionBean().setSessionReport(reportCECase);
        // force our reportingBB to choose the right bundle
        getSessionBean().setReportConfigCECaseList(null);
        getSessionBean().setQueryCECase(selectedCECaseQuery);

        return "reportCECase";
    }

    public void prepareReportCECaseList(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();

        if (reportCECaseList == null) {
            reportCECaseList = cc.getDefaultReportConfigCECaseList();
        }
        System.out.println("CaseProfileBB.prepareCaseListReport");

    }
    
    
    

    public String generateReportCECaseList() {
        reportCECaseList.setCreator(getSessionBean().getSessionUser());
        reportCECaseList.setMuni(getSessionBean().getSessionMuni());
        reportCECaseList.setGenerationTimestamp(LocalDateTime.now());
        
        getSessionBean().setReportConfigCECaseList(reportCECaseList);
        getSessionBean().setReportConfigCECase(null);
        getSessionBean().setSessionCECaseList(caseList);
        getSessionBean().setSessionReport(reportCECaseList);
        getSessionBean().setQueryCECase(selectedCECaseQuery);
        
        return "reportCECaseList";

    }

    public void prepareReportCECase(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();
        reportCECase = cc.getDefaultReportConfigCECase(currentCase);
        System.out.println("CaseProfileBB.prepareReportCECase | reportConfigOb: " + reportCECase);

    }
    

    public void rejectRequestedEvent(CECaseEvent ev) {
        selectedEvent = ev;
//        rejectedEventListIndex = currentCase.getEventProposalList().indexOf(ev);
    }

    /**
     * TODO Overhaul Proposals
     * Called when the user clicks the take requested action button
     *
     * @param ev
     */
    public void initiateNewRequestedEvent(CECaseEvent ev) {
        //selectedEventCategory = ev.getRequestedEventCat();
        
        triggeringEventForProposal = ev;
        initiateNewEvent();
    }

    /**
     * Called when the user selects their own EventCategory to add to the case
     * and is a pass-through method to the initiateNewEvent method
     *
     * @param ev
     */
    public void initiateUserChosenEventCreation(ActionEvent ev) {
        initiateNewEvent();
    }

    public void initiateNewEvent() {

        if (selectedEventCategory != null) {

            System.out.println("EventAddBB.startNewEvent | category: " + selectedEventCategory.getEventCategoryTitle());
            EventCoordinator ec = getEventCoordinator();
            try {
                selectedEvent = ec.getInitializedEvent(currentCase, selectedEventCategory);
                selectedEvent.setDateOfRecord(LocalDateTime.now());
                selectedEvent.setDiscloseToMunicipality(true);
                selectedEvent.setDiscloseToPublic(false);
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
    public void attachEventToCase(ActionEvent ev) throws ViolationException {
        EventCoordinator ec = getEventCoordinator();
        CaseCoordinator cc = getCaseCoordinator();

        // category is already set from initialization sequence
        selectedEvent.setCaseID(currentCase.getCaseID());
        selectedEvent.setOwner(getSessionBean().getSessionUser());
        try {
        
         
            // main entry point for handing the new event off to the CaseCoordinator
            // only the compliance events need to pass in another object--the violation
            // otherwise just the case and the event go to the coordinator
            if (selectedEvent.getCategory().getEventType() == EventType.Compliance) {
                selectedEvent.setEventID(cc.attachNewEventToCECase(currentCase, selectedEvent, selectedViolation));
            } else {
                selectedEvent.setEventID(cc.attachNewEventToCECase(currentCase, selectedEvent, null));
            }
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully logged event with an ID " + selectedEvent.getEventID(), ""));

            // now update the triggering event with the newly inserted event's ID
            // (We saved the triggering event when the take action button was clicked, before the event
            // add dialog was displayed and event-specific data is entered by the user
            if (triggeringEventForProposal != null) {
//                triggeringEventForProposal.getEventProposalImplementation().setResponseEvent(selectedEvent);
//                triggeringEventForProposal.getEventProposalImplementation().setResponderActual(getSessionBean().getFacesUser());
//                ec.logResponseToActionRequest(triggeringEventForProposal);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Updated triggering event ID + "
                                + triggeringEventForProposal.getEventID()
                                + " with response info!", ""));
                // reset our holding var since we're done processing the event
                triggeringEventForProposal = null;
            }

        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(),
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
        } catch (CaseLifecycleException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(),
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
        }

        // nullify the session's case so that the reload of currentCase
        // no the cecaseProfile.xhtml will trigger a new DB read
        refreshCurrentCase();
    }

    /**
     * Called by pages that aren't ceCases.xhtml to bring up the proper case for
     * viewing
     *
     * @param ev
     * @return
     */
    public String jumpToCasesToEditCEEvent(EventCECaseCasePropBundle ev) {
        CaseIntegrator ci = getCaseIntegrator();
        caseList = getSessionBean().getSessionCECaseList();
        List<Property> propList = getSessionBean().getSessionPropertyList();
        if (caseList != null) {
            caseList.add(1, caseList.remove(0));
            try {
                caseList.add(0, ci.generateCECase(ev.getEventCaseBare()));
            } catch (SQLException | IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Unable to migrate from events to cases",
                        "This is a non-user system-level error that must be fixed by your Sys Admin, sorry"));
            }
        }
        if (propList != null) {
            propList.add(1, propList.remove(0));
            propList.add(0, ev.getEventCaseBare().getProperty());
        }
        return "ceCases";
    }

    public void commitEventEdits(ActionEvent ev) {
        EventCoordinator ec = getEventCoordinator();
//        currentCase.assembleEventList().remove(selectedEvent);
        try {
            ec.editEvent(selectedEvent, getSessionBean().getSessionUser());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Event udpated!", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please select one or more people to attach to this event",
                    "This is a non-user system-level error that must be fixed by your Sys Admin"));
        }
        refreshCurrentCase();

    }

  

    public void queueSelectedPerson(ActionEvent ev) {
        if (selectedPerson != null) {
            selectedEvent.getPersonList().add(selectedPerson);
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Please select one or more people to attach to this event",
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
        }
    }

    public void deQueuePersonFromEvent(Person p) {
        if (selectedEvent.getPersonList() != null) {
            selectedEvent.getPersonList().remove(p);
        }
    }

    public void editEvent(CECaseEvent ev) {
        selectedEvent = ev;
    }

    /**
     * Primary injection point for setting the case which will be displayed in
     * the right column (the manage object column) on cECases.xhtml
     *
     * @param c the case to be managed--comes from the data table row button
     */
    public void manageCECase(CECase c) {
        UserIntegrator ui = getUserIntegrator();
        System.out.println("CaseProfileBB.manageCECase | caseid: " + c.getCaseID());
        try {
            ui.logObjectView(getSessionBean().getSessionUser(), c);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        currentCase = c;
        getSessionBean().setSessionCECase(currentCase);
        getSessionBean().setSessionProperty(c.getProperty());
    }
    
    public void testButton(ActionEvent ev){
        System.out.println("button");
    }

    public void changePACCAccess() {
        System.out.println("CEActionRequestsBB.changePACCAccess");
        CaseIntegrator ci = getCaseIntegrator();

        try {
            ci.updateCECaseMetadata(currentCase);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Done! Public access status is now: " + String.valueOf(currentCase.isPaccEnabled())
                    + " and action request forward linking is statusnow: " + String.valueOf(currentCase.isAllowForwardLinkedPublicAccess())
                    + " for case ID: " + currentCase.getCaseID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to add change public access code status",
                    getResourceBundle(Constants.MESSAGE_TEXT).getString("systemLevelError")));
        }
    }

    public void overrideCasePhase(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();
        CaseIntegrator ci = getCaseIntegrator();
        try {
            cc.manuallyChangeCasePhase(currentCase, getSelectedCasePhase());
            currentCase = ci.getCECase(currentCase.getCaseID());
            updateCaseInCaseList(currentCase);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Updated case phase; please refresh case", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to write case phase changes to DB",
                            "This error must be corrected by a system administrator, sorry"));
        } catch (CaseLifecycleException | ViolationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to change case phase due to a case lifecycle exception",
                            "Please check with your system administrator"));

        }
    }

    /**
     * Designed to update the case phase or name in the master case list in
     * ceCases.xhtml when a case is updated. Prevents rebuilding all the cases
     * in the entire list, which could be massive
     *
     * @param c
     */
    private void updateCaseInCaseList(CECase c) {
        CaseIntegrator ci = getCaseIntegrator();
        Iterator<CECase> it = caseList.iterator();
        CECase localCase;
        int idx = 0;
        while (it.hasNext()) {
            localCase = it.next();
            if (localCase.getCaseID() == c.getCaseID()) {
                try {
                    caseList.set(idx, ci.getCECase(c.getCaseID()));
                } catch (IntegrationException | CaseLifecycleException ex) {
                    System.out.println(ex);
                    getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
                }
            } // end if
            idx++;
        } // end while
    }

    /**
     * Pass through method for clicks of the refresh case data button on
     * cECases.xhtml
     *
     * @param ev
     */
    public void refreshCurrentCase(ActionEvent ev) {
        refreshCurrentCase();
        
    }

    public void refreshCurrentCase() {
        CaseIntegrator ci = getCaseIntegrator();
        System.out.println("CaseProfileBB.refreshCurrentCase | Refreshing case ID " + currentCase.getCaseID());
        try {
            currentCase = ci.getCECase(currentCase.getCaseID());
        } catch (IntegrationException ex) {
            System.out.println("CaseProfileBB.refreshCurrentCase | integration ex" + ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        } catch (CaseLifecycleException ex) {
            System.out.println("CaseProfileBB.refreshCurrentCase | lifecycle ex" + ex);
        }

    }

    public void initiatePhaseOverride(ActionEvent ev) {
        System.out.println("CaseProfileBB.initiatePhaseOverride");
        // do nothing
    }
    
    
    public void removeViolation(CodeViolation cv){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.deleteViolation(cv);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Violation id " + cv.getViolationID() + " removed from case!", ""));
            
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
            
        }
        refreshCurrentCase();
        
        
    }

    public void recordCompliance(CodeViolation cv) {
        EventCoordinator ec = getEventCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        selectedViolation = cv;
        // build event details package
        CECaseEvent e = null;
        try {
            selectedViolation.setComplianceUser(getSessionBean().getSessionUser());
            e = ec.generateViolationComplianceEvent(selectedViolation);
            e.setOwner(getSessionBean().getSessionUser());
            e.setDateOfRecord(LocalDateTime.now());
            cv.setActualComplianceDate(LocalDateTime.now());
            cc.recordCompliance(cv, getSessionBean().getSessionUser());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        // the user is then shown the add event dialog, and when the
        // event is added to the case, the CaseCoordinator will
        // set the date of record on the violation to match that chosen
        // for the event
        selectedEvent = e;

    }

//    Procedural vs. OO code
    public String editViolation(CodeViolation cv) {
            getSessionBean().setSessionCodeViolation(cv);
            positionCurrentCaseAtHeadOfQueue();
            return "violationEdit";
    }
 
    public String createNewNotice() throws SQLException {
        NoticeOfViolation nov;
        CaseCoordinator cc = getCaseCoordinator();
            if (!currentCase.getViolationListUnresolved().isEmpty()) {
                getSessionBean().getSessionPropertyList().add(0, currentCase.getProperty());
                getSessionBean().setSessionProperty(currentCase.getProperty());
                positionCurrentCaseAtHeadOfQueue();
                nov = cc.novGetNewNOVSkeleton(currentCase, getSessionBean().getSessionMuni());
                nov.setCreationBy(getSessionBean().getSessionUser());
                getSessionBean().setSessionNotice(nov);
                return "noticeOfViolationBuilder";
            } else {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No unresolved violations exist for building a letter", ""));
            }
        return "";
    }

    public void resetNotice(NoticeOfViolation nov) {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.novResetMailing(nov, getSessionBean().getSessionUser());
            refreshCurrentCase();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Notice mailing status has been reset", ""));
        } catch (IntegrationException | PermissionsException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        }
    }

    public String createNewCitation() {
        System.out.println("CaseProfileBB.createNewCitation  | current case tostring: "
                + currentCase);
        getSessionBean().setSessionCitation(null);
        positionCurrentCaseAtHeadOfQueue();
        return "citationEdit";
    }

    public String updateCitation(Citation cit) {
        getSessionBean().setSessionCitation(cit);
        positionCurrentCaseAtHeadOfQueue();
        return "citationEdit";
    }

    public String deleteCitation() {
        if (selectedCitation != null) {
            CaseCoordinator cc = getCaseCoordinator();
            try {
                cc.deleteCitation(selectedCitation);
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to delete citation, sorry: "
                                + "probably because it is linked to another DB entity", ""));
                System.out.println(ex);
            }
        }
        return "";
    }

    public void deleteViolation(ActionEvent e) {
        CaseCoordinator cc = getCaseCoordinator();

        try {
            cc.deleteViolation(selectedViolations.get(0));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Unable to delete selected violation",
                        "It has probably been added to a Notice of Violation"
                                + "already and cannot be removed."));
        }
    }

    public String addViolation() {
        getSessionBean().setSessionCECase(currentCase);
        return "violationSelectElement";
    }

    public String printNotice(NoticeOfViolation nov) {
        getSessionBean().setSessionNotice(nov);
        positionCurrentCaseAtHeadOfQueue();
        return "noticeOfViolationPrint";
    }

    public String editNoticeOfViolation(NoticeOfViolation nov) {
        getSessionBean().setSessionNotice(nov);
        positionCurrentCaseAtHeadOfQueue();
        return "noticeOfViolationEditor";
    }

    public void lockNoticeAndQueueForMailing(NoticeOfViolation nov) {
        CaseCoordinator caseCoord = getCaseCoordinator();
        
        try {
            caseCoord.novLockAndQueue(currentCase, nov, getSessionBean().getSessionUser());
        } catch (CaseLifecycleException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        } catch (EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "The automatic event generation associated with this action has thrown an error. "
                            + "Please create an event manually which logs this letter being queued for mailing", ""));

        } catch (ViolationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Unable to queue notice of violatio. "
                            + "Please create an event manually which logs this letter being queued for mailing", ""));
        }
    }

    public void deleteSelectedEvent() {

    }

    public void deleteNoticeOfViolation(NoticeOfViolation nov) {
        CaseCoordinator caseCoord = getCaseCoordinator();
        try {
            caseCoord.novDelete(nov);
            caseCoord.refreshCase(currentCase);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Notice no. " + nov.getNoticeID() + " has been nuked forever", ""));
        } catch (CaseLifecycleException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to delete this notice of violation, "
                            + "probably because it has been sent already", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));

        }
    }

    public void markNoticeOfViolationAsSent(NoticeOfViolation nov) {
        CaseCoordinator caseCoord = getCaseCoordinator();
        try {
            caseCoord.novMarkAsSent(currentCase, nov, getSessionBean().getSessionUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Marked notice as sent and added event to case",
                            ""));
        } catch (CaseLifecycleException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,ex.getMessage(),""));
        }catch (EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to generate case event to log phase change",
                            "Note that because this message is being displayed, the phase change"
                            + "has probably succeeded"));
        }
    }

    public void markNoticeOfViolationAsReturned(NoticeOfViolation nov) {
        CaseCoordinator caseCoord = getCaseCoordinator();
        try {
            caseCoord.novMarkAsReturned(currentCase, nov, getSessionBean().getSessionUser());
            caseCoord.refreshCase(currentCase);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Notice no. " + nov.getNoticeID()
                            + " has been marked as returned on today's date", ""));
        } catch (IntegrationException | CaseLifecycleException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        }
    }

    /**
     * @return the currentCase
     */
    public CECase getCurrentCase() {
        return currentCase;
    }

    /**
     * Used in the violation table to set the clicked violation row as the
     * system's current violation. At this stage, this feature is used by the
     * updateViolationsCodebookLink method only
     *
     * Note that the member variable that this "setter" sets is also the memvar
     * that setSel3ectedViolation sets
     *
     * @param cv the code violation clicked in the table
     */
    public void setActiveViolation(CodeViolation cv) {
        selectedViolation = cv;
    }

    public void updateViolationsCodeBookLink(ActionEvent ae) throws CaseLifecycleException {
        CaseIntegrator casei = getCaseIntegrator();
        try {
            ViolationIntegrator cvi = getCodeViolationIntegrator();
            CodeIntegrator ci = getCodeIntegrator();
            EnforcableCodeElement ece = ci.getEnforcableCodeElement(newViolationCodeBookEleID);
            if (ece != null) {
                selectedViolation.setViolatedEnfElement(ece);
                cvi.updateCodeViolation(selectedViolation);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Success: Updated Violation with new CodeBook linking", ""));
                currentCase = casei.getCECase(currentCase.getCaseID());
            } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to find CodeBook Entry by this ID, sorry. Please try again.", ""));
            }
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }

    }


  
    
    public void makeChoice(Choice choice, Proposal p){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            if(p instanceof ProposalCECase){
                cc.evaluateProposal(p, choice, currentCase, getSessionBean().getSessionUser());
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "You just chose choice ID " + choice.getChoiceID() + " proposed in proposal ID " + p.getProposalID(), ""));
            }
            
        } catch (EventException | AuthorizationException | CaseLifecycleException | IntegrationException | ViolationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
            ex.getMessage(), ""));
        } 
    }    
    
    /**
     * @param currentCase the currentCase to set
     */
    public void setCurrentCase(CECase currentCase) {
        this.currentCase = currentCase;
    }

    /**
     * @return the selectedEvent
     */
    public CECaseEvent getSelectedEvent() {
        return selectedEvent;
    }

    /**
     * @return the selectedViolation
     */
    public List<CodeViolation> getSelectedViolations() {
        return selectedViolations;
    }

    /**
     * @param selectedEvent the selectedEvent to set
     */
    public void setSelectedEvent(CECaseEvent selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    /**
     * @param svs
     */
    public void setSelectedViolations(List<CodeViolation> svs) {
        selectedViolations = svs;
    }

    /**
     * @return the citationList
     */
    public List<Citation> getCitationList() {

        setCitationList(getSessionBean().getSessionCECase().getCitationList());
        return citationList;
    }

    /**
     * @return the selectedCitation
     */
    public Citation getSelectedCitation() {
        return selectedCitation;
    }

    /**
     * @param cl
     */
    public void setCitationList(List<Citation> cl) {
        citationList = cl;
    }

    /**
     * @param selectedCitation the selectedCitation to set
     */
    public void setSelectedCitation(Citation selectedCitation) {
        this.selectedCitation = selectedCitation;
    }

    public String takeNextAction() {
        CECaseEvent e = getEventForTriggeringCasePhaseAdvancement();
        return "eventAdd";
    }

    /**
     * @return the eventForTriggeringCasePhaseAdvancement
     */
    public CECaseEvent getEventForTriggeringCasePhaseAdvancement() {
        EventCoordinator ec = getEventCoordinator();

        try {
            eventForTriggeringCasePhaseAdvancement = ec.getActionEventForCaseAdvancement(currentCase);
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error connecting to DB. This must be corrected by a system administrator", ""));

        } catch (CaseLifecycleException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error generating next action event for advancing the case phase", ""));
        }
        return eventForTriggeringCasePhaseAdvancement;
    }

    /**
     * @param eventForTriggeringCasePhaseAdvancement the
     * eventForTriggeringCasePhaseAdvancement to set
     */
    public void setEventForTriggeringCasePhaseAdvancement(CECaseEvent eventForTriggeringCasePhaseAdvancement) {
        this.eventForTriggeringCasePhaseAdvancement = eventForTriggeringCasePhaseAdvancement;
    }

    /**
     * @return the nextPhase
     */
    public CasePhase getNextPhase() {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            nextPhase = cc.getNextCasePhase(currentCase);
        } catch (CaseLifecycleException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error generating next case phase, sorry!", ""));

        }
        return nextPhase;
    }

    /**
     * @param nextPhase the nextPhase to set
     */
    public void setNextPhase(CasePhase nextPhase) {
        this.nextPhase = nextPhase;
    }

    /**
     * @return the casePhaseList
     */
    public CasePhase[] getCasePhaseList() {
        casePhaseList = (CasePhase.values());
        return casePhaseList;
    }

    /**
     * @return the currentCase's phase
     */
    public CasePhase getCurrentCasePhase() {
        return currentCase.getCasePhase();
    }

    /**
     * @param selectedCasePhase the selectedCasePhase to set
     */
    public void setSelectedCasePhase(CasePhase selectedCasePhase) {
        this.selectedCasePhase = selectedCasePhase;
    }

    /**
     * @return the filteredEventList
     */
    public List<CECaseEvent> getFilteredEventList() {
        return filteredEventList;
    }

    /**
     * @param fel
     */
    public void setFilteredEventList(List<CECaseEvent> fel) {
        filteredEventList = fel;
    }

    /**
     * @return the selectedViolation
     */
    public CodeViolation getSelectedViolation() {
        return selectedViolation;
    }

    /**
     * @param selectedViolation the selectedViolation to set
     */
    public void setSelectedViolation(CodeViolation selectedViolation) {
        this.selectedViolation = selectedViolation;
    }

    /**
     * @return the newViolationCodeBookEleID
     */
    public int getNewViolationCodeBookEleID() {
        return newViolationCodeBookEleID;
    }

    /**
     * @param newViolationCodeBookEleID the newViolationCodeBookEleID to set
     */
    public void setNewViolationCodeBookEleID(int newViolationCodeBookEleID) {
        this.newViolationCodeBookEleID = newViolationCodeBookEleID;
    }

    /**
     * @return the phaseDiagramImageFilename
     */
    public String getPhaseDiagramImageFilename() {
        phaseDiagramImageFilename = getImageFilenameMap().get(currentCase.getCasePhase());
        return phaseDiagramImageFilename;
    }

    /**
     * @param phaseDiagramImageFilename the phaseDiagramImageFilename to set
     */
    public void setPhaseDiagramImageFilename(String phaseDiagramImageFilename) {
        this.phaseDiagramImageFilename = phaseDiagramImageFilename;
    }

    /**
     * @return the caseList
     */
     public List<CECase> getCaseList() {

        return caseList;
    }

    /**
     * @return the filteredCaseList
     */
    public List<CECase> getFilteredCaseList() {
        return filteredCaseList;
    }

    /**
     * @return the filteredCaseHistoryList
     */
    public List<CECase> getFilteredCaseHistoryList() {
        return filteredCaseHistoryList;
    }

    /**
     * @return the recentEventList
     */
    public List<CECaseEvent> getRecentEventList() {
        return recentEventList;
    }

    /**
     * @return the muniPeopleList
     */
    public List<Person> getMuniPeopleList() {
        return muniPeopleList;
    }

    /**
     *
     * @return the imageFilenameMap
     */
    public HashMap<CasePhase, String> getImageFilenameMap() {
        return imageFilenameMap;
    }

    /**
     * @param caseList the caseList to set
     */
    public void setCaseList(List<CECase> caseList) {
        this.caseList = caseList;
    }

    /**
     * @param filteredCaseList the filteredCaseList to set
     */
    public void setFilteredCaseList(List<CECase> filteredCaseList) {
        this.filteredCaseList = filteredCaseList;
    }

    /**
     * @param filteredCaseHistoryList the filteredCaseHistoryList to set
     */
    public void setFilteredCaseHistoryList(List<CECase> filteredCaseHistoryList) {
        this.filteredCaseHistoryList = filteredCaseHistoryList;
    }

    /**
     * @param recentEventList the recentEventList to set
     */
    public void setRecentEventList(List<CECaseEvent> recentEventList) {
        this.recentEventList = recentEventList;
    }

    /**
     * @param muniPeopleList the muniPeopleList to set
     */
    public void setMuniPeopleList(List<Person> muniPeopleList) {
        this.muniPeopleList = muniPeopleList;
    }

   

    /**
     * @param imageFilenameMap the imageFilenameMap to set
     */
    public void setImageFilenameMap(HashMap<CasePhase, String> imageFilenameMap) {
        this.imageFilenameMap = imageFilenameMap;
    }

    /**
     * @return the searchParams
     */
    public SearchParamsCECase getSearchParams() {

        return searchParams;
    }

    /**
     * @param searchParams the searchParams to set
     */
    public void setSearchParams(SearchParamsCECase searchParams) {
        this.searchParams = searchParams;
    }

    /**
     * @return the selectedCasePhase
     */
    public CasePhase getSelectedCasePhase() {
        return selectedCasePhase;
    }

    /**
     * @return the caseStageArray
     */
    public CaseStage[] getCaseStageArray() {
        caseStageArray = CaseStage.values();
        return caseStageArray;
    }

    /**
     * @param caseStageArray the caseStageArray to set
     */
    public void setCaseStageArray(CaseStage[] caseStageArray) {
        this.caseStageArray = caseStageArray;
    }

    /**
     * @return the eventCategoryList
     */
    public List<EventCategory> getEventCategoryList() {
        EventIntegrator ei = getEventIntegrator();

        if (selectedEventType != null) {

            try {
                eventCategoryList = ei.getEventCategoryList(selectedEventType);
            } catch (IntegrationException ex) {
                // do nothing
            }
        }
        return eventCategoryList;
    }

    /**
     * @return the selectedEventCategory
     */
    public EventCategory getSelectedEventCategory() {
        return selectedEventCategory;
    }

    /**
     * @return the selectedEventType
     */
    public EventType getSelectedEventType() {
        return selectedEventType;
    }

    /**
     * @return the availableEventList
     */
    public List<EventType> getAvailableEventTypeList() {
        EventCoordinator ec = getEventCoordinator();
        availableEventList = ec.getPermittedEventTypesForCECase(currentCase,
                getSessionBean().getSessionUser());
        return availableEventList;
    }

    /**
     * @return the selectedPerson
     */
    public Person getSelectedPerson() {
        return selectedPerson;
    }

    /**
     * @param eventCategoryList the eventCategoryList to set
     */
    public void setEventCategoryList(List<EventCategory> eventCategoryList) {
        this.eventCategoryList = eventCategoryList;
    }

    /**
     * @param selectedEventCategory the selectedEventCategory to set
     */
    public void setSelectedEventCategory(EventCategory selectedEventCategory) {
        this.selectedEventCategory = selectedEventCategory;
    }

    /**
     * @param selectedEventType the selectedEventType to set
     */
    public void setSelectedEventType(EventType selectedEventType) {
        this.selectedEventType = selectedEventType;
    }

    /**
     * @param availableEventList the availableEventList to set
     */
    public void setAvailableEventList(List<EventType> availableEventList) {
        this.availableEventList = availableEventList;
    }

    /**
     * @param selectedPerson the selectedPerson to set
     */
    public void setSelectedPerson(Person selectedPerson) {
        this.selectedPerson = selectedPerson;
    }

    /**
     * @return the includeActionRequest
     */
    public boolean isIncludeActionRequest() {

        return includeActionRequest;
    }

    /**
     * @param includeActionRequest the includeActionRequest to set
     */
    public void setIncludeActionRequest(boolean includeActionRequest) {
        this.includeActionRequest = includeActionRequest;
    }

    /**
     * @return the availableActionsToRequest
     */
    public List<EventCategory> getAvailableActionsToRequest() {
        EventIntegrator ei = getEventIntegrator();
        try {
            availableActionsToRequest = ei.getRequestableEventCategories();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return availableActionsToRequest;
    }

    /**
     * @param availableActionToRequest the availableActionsToRequest to set
     */
    public void setAvailableActionToRequest(List<EventCategory> availableActionToRequest) {
        this.availableActionsToRequest = availableActionToRequest;
    }

    /**
     * @return the allowedToClearActionResponse
     */
    public boolean isAllowedToClearActionResponse() {
        allowedToClearActionResponse = false;
//        if (selectedEvent != null) {
//            if (selectedEvent.getEventProposalImplementation() != null
//                    && ((getSessionBean().getFacesUser().getUserID() == selectedEvent.getEventProposalImplementation().getResponderActual().getUserID())
//                    || getSessionBean().getFacesUser().getKeyCard().isHasSysAdminPermissions())) {
//                allowedToClearActionResponse = true;
//            }
//        }
        return allowedToClearActionResponse;
    }

    /**
     * @param allowedToClearActionResponse the allowedToClearActionResponse to
     * set
     */
    public void setAllowedToClearActionResponse(boolean allowedToClearActionResponse) {
        this.allowedToClearActionResponse = allowedToClearActionResponse;
    }

    /**
     * @return the triggeringEventForProposal
     */
    public CECaseEvent getTriggeringEventForProposal() {
        return triggeringEventForProposal;
    }

    /**
     * @param triggeringEventForProposal the
 triggeringEventForProposal to set
     */
    public void setTriggeringEventForProposal(CECaseEvent triggeringEventForProposal) {
        this.triggeringEventForProposal = triggeringEventForProposal;
    }

    

    /**
     * @return the styleClassInvestigation
     */
    public String getStyleClassInvestigation() {
        String style = null;
        
       
        
        if (currentCase.getCasePhase().getCaseStage() == CaseStage.Investigation) {
            style = currentCase.getCasePhaseIcon().getStyleClass();
            
        } else {
            style = Constants.STYLE_CLASS_INACTIVE_CASE_PHASE;
        }
        styleClassInvestigation = style;
        return styleClassInvestigation;
    }

    /**
     * @return the styleClassEnforcement
     */
    public String getStyleClassEnforcement() {
        String style = null;
        if (currentCase.getCasePhase().getCaseStage() == CaseStage.Enforcement) {
            style = currentCase.getCasePhaseIcon().getStyleClass();
            
        } else {
            style = Constants.STYLE_CLASS_INACTIVE_CASE_PHASE;
        }
        styleClassEnforcement = style;
        return styleClassEnforcement;
    }

    /**
     * @return the sytleClassCitation
     */
    public String getSytleClassCitation() {
        String style = null;
        if (currentCase.getCasePhase().getCaseStage() == CaseStage.Citation) {
            style = currentCase.getCasePhaseIcon().getStyleClass();
            
        } else {
            style = Constants.STYLE_CLASS_INACTIVE_CASE_PHASE;
        }
        sytleClassCitation = style;
        return sytleClassCitation;
    }

    /**
     * @return the sytleClassClosed
     */
    public String getSytleClassClosed() {
        String style = null;
        if (currentCase.getCasePhase().getCaseStage() == CaseStage.Closed) {
            style = currentCase.getCasePhaseIcon().getStyleClass();
            
        } else {
            style = Constants.STYLE_CLASS_INACTIVE_CASE_PHASE;
        }
        sytleClassClosed = style;
        return sytleClassClosed;
    }

    /**
     * @param styleClassInvestigation the styleClassInvestigation to set
     */
    public void setStyleClassInvestigation(String styleClassInvestigation) {
        this.styleClassInvestigation = styleClassInvestigation;
    }

    /**
     * @param styleClassEnforcement the styleClassEnforcement to set
     */
    public void setStyleClassEnforcement(String styleClassEnforcement) {
        this.styleClassEnforcement = styleClassEnforcement;
    }

    /**
     * @param sytleClassCitation the sytleClassCitation to set
     */
    public void setSytleClassCitation(String sytleClassCitation) {
        this.sytleClassCitation = sytleClassCitation;
    }

    /**
     * @param sytleClassClosed the sytleClassClosed to set
     */
    public void setSytleClassClosed(String sytleClassClosed) {
        this.sytleClassClosed = sytleClassClosed;
    }

    /**
     * @return the styleClassStatusIcon
     */
    public String getStyleClassStatusIcon() {
        if(currentCase != null && currentCase.getCasePhaseIcon() != null){
            styleClassStatusIcon = currentCase.getCasePhaseIcon().getStyleClass();
        }
        return styleClassStatusIcon;
    }

    /**
     * @param styleClassStatusIcon the styleClassStatusIcon to set
     */
    public void setStyleClassStatusIcon(String styleClassStatusIcon) {
        this.styleClassStatusIcon = styleClassStatusIcon;
    }

    /**
     * @return the styleClassActionRequestIcon
     */
    public String getStyleClassActionRequestIcon() {
        return styleClassActionRequestIcon;
    }

    /**
     * @param styleClassActionRequestIcon the styleClassActionRequestIcon to set
     */
    public void setStyleClassActionRequestIcon(String styleClassActionRequestIcon) {
        this.styleClassActionRequestIcon = styleClassActionRequestIcon;
    }

    /**
     * @return the reportCECase
     */
    public ReportConfigCECase getReportCECase() {
        return reportCECase;
    }

    /**
     * @param reportCECase the reportCECase to set
     */
    public void setReportCECase(ReportConfigCECase reportCECase) {
        this.reportCECase = reportCECase;
    }

    /**
     * @return the reportCECaseList
     */
    public ReportConfigCECaseList getReportCECaseList() {
        return reportCECaseList;
    }

    /**
     * @param reportCECaseList the reportCECaseList to set
     */
    public void setReportCECaseList(ReportConfigCECaseList reportCECaseList) {
        this.reportCECaseList = reportCECaseList;
    }

    
    /**
     * @return the selectedCECaseQuery
     */
    public QueryCECase getSelectedCECaseQuery() {
        if(selectedBOBQuery instanceof Query){
            selectedCECaseQuery = (QueryCECase) selectedBOBQuery;
        }
        return selectedCECaseQuery;
    }

    /**
     * @param selectedCECaseQuery the selectedCECaseQuery to set
     */
    public void setSelectedCECaseQuery(QueryCECase selectedCECaseQuery) {
        
        
        this.selectedCECaseQuery = selectedCECaseQuery;
    }

    /**
     * @return the queryList
     */
    public List<QueryCECase> getQueryList() {
        return queryList;
    }

    /**
     * @param queryList the queryList to set
     */
    public void setQueryList(List<QueryCECase> queryList) {
        this.queryList = queryList;
    }

    /**
     * @return the selectedBOBQuery
     */
    public Query getSelectedBOBQuery() {
        return selectedBOBQuery;
    }

    /**
     * @param selectedBOBQuery the selectedBOBQuery to set
     */
    public void setSelectedBOBQuery(Query selectedBOBQuery) {
        this.selectedBOBQuery = selectedBOBQuery;
    }

    /**
     * @return the violationDonut
     */
    public DonutChartModel getViolationDonut() {
        return violationDonut;
    }

    /**
     * @param violationDonut the violationDonut to set
     */
    public void setViolationDonut(DonutChartModel violationDonut) {
        this.violationDonut = violationDonut;
    }

}
