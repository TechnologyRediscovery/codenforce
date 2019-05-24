/*
 * Copyright (C) 2017 Turtle Creek Valley
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
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.PermissionsException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.search.QueryCEAR;
import com.tcvcog.tcvce.entities.search.QueryCEARTitle;
import com.tcvcog.tcvce.entities.search.SearchParamsCEActionRequests;
import com.tcvcog.tcvce.entities.search.SearchParamsCECases;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.CitationIntegrator;
import com.tcvcog.tcvce.integration.ViolationIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class CaseCoordinator extends BackingBeanUtils implements Serializable{

    final CasePhase initialCECasePphase = CasePhase.PrelimInvestigationPending;
    
    /**
     * Creates a new instance of CaseCoordinator
     */
    public CaseCoordinator() {
        
        
    
    }
    
    /**
     * Called at the very end of the CECase creation process by the CaseIntegrator
     * and simply checks for events that have a required eventcategory attached
     * and places a copy of the event in the Case's member variable.
     * 
     * This means that every time we refresh the case, the list is automatically
     * updated.
     * 
     * DESIGN NOTE: A competing possible location for this method would be on the
     * CECase object itself--in its getEventListActionRequest method
     * 
     * @param cse the CECase with a populated set of Events
     * @return the CECase with the action request list ready to roll
     * @throws com.tcvcog.tcvce.domain.CaseLifecyleException
     */
    public CECase configureCECase(CECase cse) throws CaseLifecyleException{
        
        setCaseStage(cse);
        cse.setShowHiddenEvents(false);
        cse.setShowInactiveEvents(false);
        
   
        
        // check to make sure we have empty lists on all of our list objects
        if(cse.getViolationList() == null){
            cse.setViolationList(new ArrayList<CodeViolation>());
        }
        
        cse.setEventListActionRequests(new ArrayList<EventCECase>());
        cse.setVisibleEventList(new ArrayList<EventCECase>());
        
        
        if(cse.getCitationList() == null){
            cse.setCitationList(new ArrayList<Citation>());
        }
        
        if(cse.getNoticeList() == null){
            cse.setNoticeList(new ArrayList<NoticeOfViolation>());
        }
        
        if(cse.getCeActionRequestList() == null){
            cse.setCeActionRequestList(new ArrayList<CEActionRequest>());
        }
        
        
        Collections.sort(cse.getNoticeList());
        Collections.reverse(cse.getNoticeList());
        Collections.sort(cse.getEventListActionRequests());
        Collections.sort(cse.getVisibleEventList());
        Collections.reverse(cse.getVisibleEventList()); 
        
        return cse;
    }
    
    public Icon getIconByCasePhase(CasePhase phase) throws IntegrationException{
        SystemIntegrator si = getSystemIntegrator();
        return si.getIcon(phase);
        
    }
    
    
    /**
     * Asks the SearchCoordinator for the appropriate 
     * 
     * @param m
     * @return an search params object for CEAction requests with default values
     * which amount to requests that aren't attached to a case and were submitted
     * within the past 10 years
     */
    public SearchParamsCEActionRequests getDefaultSearchParamsCEActionRequests(Municipality m){
        
        SearchCoordinator sc = getSearchCoordinator();
        return sc.generateParams_CEAR_Unprocessed(m);
    }
    
   
    public QueryCEAR generateInitialCEARList(User u, Municipality m) throws IntegrationException{
        SearchCoordinator sc = getSearchCoordinator();
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        
        return ceari.queryCEARs(sc.buildCEARQuery(QueryCEARTitle.UNPROCESSED, u, m));
        
    }
    
    
   
    
    /**
     * Front door for querying cases in the DB
     * 
     * @param params pre-configured search parameters
     * @return
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.CaseLifecyleException 
     */
    public List<CECase> queryCECases(SearchParamsCECases params) throws IntegrationException, CaseLifecyleException{
        CaseIntegrator ci = getCaseIntegrator();
        return ci.queryCECases(params);
        
    }
    
    public List<CECase> getUserCaseHistoryList(User u) throws IntegrationException, CaseLifecyleException{
        CaseIntegrator caseInt = getCaseIntegrator();
        return caseInt.getCECaseHistoryList(u);
        
    }
    
    
    
    
    
    
    public CECase getInitializedCECase(Property p, User u){
        CECase newCase = new CECase();
        
        int casePCC = getControlCodeFromTime();
        // caseID set by postgres sequence
        // timestamp set by postgres
        // no closing date, by design of case flow
        newCase.setPublicControlCode(casePCC);
        newCase.setProperty(p);
        newCase.setCaseManager(u);
        
        return newCase;
    }
    
    /**
     * Primary entry point for code enf cases. Two major pathways exist through this method:
     * - creating cases as a result of an action request submission
     * - creating cases from some other source than an action request
     * Depending on the source, an appropriately note-ified case origination event
     * is built and attached to the case that was just created.
     * 
     * @param newCase
     * @param u
     * @param cear
     * @throws IntegrationException
     * @throws CaseLifecyleException
     * @throws ViolationException 
     */
    public void insertNewCECase(CECase newCase, User u, CEActionRequest cear) throws IntegrationException, CaseLifecyleException, ViolationException{
        
        CECase insertedCase;
        
        CaseIntegrator ci = getCaseIntegrator();
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        EventCoordinator ec = getEventCoordinator();
        EventCategory originationCategory;
        EventCECase originationEvent;
        
        // set default status to prelim investigation pending
        newCase.setCasePhase(initialCECasePphase);
        
        // the integrator returns to us a CECase with the correct ID after it has
        // been written into the DB
        insertedCase = ci.insertNewCECase(newCase);
        newCase.setCaseID(insertedCase.getCaseID());

        // If we were passed in an action request, connect it to the new case we just made
        if(cear != null){
            ceari.connectActionRequestToCECase(cear.getRequestID(), insertedCase.getCaseID(), u.getUserID());
            originationCategory = ec.getInitiatlizedEventCategory(
                    Integer.parseInt(getResourceBundle(
                    Constants.EVENT_CATEGORY_BUNDLE).getString("originiationByActionRequest")));
            originationEvent = ec.getInitializedEvent(newCase, originationCategory);
            StringBuilder sb = new StringBuilder();
            sb.append("Case generated from the submission of a Code Enforcement Action Request");
            sb.append("<br/>");
            sb.append("ID#:");
            sb.append(cear.getRequestID());
            sb.append(" submitted by ");
            sb.append(cear.getRequestor().getFirstName());
            sb.append(" ");
            sb.append(cear.getRequestor().getLastName());
            sb.append(" on ");
            sb.append(getPrettyDate(cear.getDateOfRecord()));
            sb.append(" with a database timestamp of ");
            sb.append(getPrettyDate(cear.getSubmittedTimeStamp()));
            originationEvent.setNotes(sb.toString());
            
            
        } else {
            // since there's no action request, the assumed method is called "observation"
            originationCategory = ec.getInitiatlizedEventCategory(
                    Integer.parseInt(getResourceBundle(
                    Constants.EVENT_CATEGORY_BUNDLE).getString("originiationByObservation")));
            originationEvent = ec.getInitializedEvent(newCase, originationCategory);
            StringBuilder sb = new StringBuilder();
            sb.append("Case opened directly on property by code officer assigned to this event");
            originationEvent.setNotes(sb.toString());
            
        }
            originationEvent.setOwner(u);
            originationEvent.setCaseID(insertedCase.getCaseID());
            originationEvent.setDateOfRecord(LocalDateTime.now());
            attachNewEventToCECase(newCase, originationEvent, null);
    }
    
    /**
     * Called by the PIBCECaseBB when a public user wishes to add an event
     * to the case they are viewing online. This method stitches together the
     * message text, messenger name, and messenger phone number before
     * passing the info back to the EventCoordinator
     * @param caseID can be extracted from the public info bundle
     * @param msg the text of the message the user wants to add to the case 
     * @param messagerName the first and last name of the person submitting the message
     * Note that this submission info is not YET wired into the actual Person objects
     * in the system.
     * @param messagerPhone a simple String rendering of whatever the user types in. Length validation only.
     */
    public void attachPublicMessage(int caseID, String msg, String messagerName, String messagerPhone) throws IntegrationException{
        StringBuilder sb = new StringBuilder();
        sb.append("Case note added by ");
        sb.append(messagerName);
        sb.append(" with contact number: ");
        sb.append(messagerPhone);
        sb.append(": ");
        sb.append("<br/><br/>");
        sb.append(msg);
        
        EventCoordinator ec = getEventCoordinator();
        ec.attachPublicMessagToCECase(caseID, sb.toString());
        
        
    }
    
    /**
     * Called by the CaseManageBB when the user requests to change the case phase manually.
     * Note that this method is responsible for storing the case's phase before the change, 
     * updating the case Object itself, and then passing the updated CECase object
     * and the phase phase to the EventCoordinator which will take care of logging the event
     * 
     * @param c the case before its phase has been changed
     * @param newPhase the CasePhase to which we to change the case
     * @throws IntegrationException
     * @throws CaseLifecyleException 
     */
    public void manuallyChangeCasePhase(CECase c, CasePhase newPhase) throws IntegrationException, CaseLifecyleException, ViolationException{
        EventCoordinator ec = getEventCoordinator();
        CaseIntegrator ci = getCaseIntegrator();
        CasePhase pastPhase = c.getCasePhase();
        // this call to changeCasePhase requires that the case we pass in already has
        // its phase changed
        c.setCasePhase(newPhase);
        ci.changeCECasePhase(c);
        
        ec.generateAndInsertManualCasePhaseOverrideEvent(c, pastPhase);
        
    }
    
    
    /**
     * Primary event life cycle control method which is called
     * each time an event is added to a code enf case. The primary business
     * logic related to which events can be attached to a case at any
     * given case phase is implemented in this coordinator method.
     * 
     * Its core operation is to check case and event related qualities
     * and delegate further processing to event-type specific methods
     * also found in this coordinator
     * 
     * @param c the case to which the event should be added
     * @param e the event to add to the case also included in this call
     * @param viol the CodeViolation object associated with this event, can be null
     * @return 
     * @throws com.tcvcog.tcvce.domain.CaseLifecyleException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.ViolationException
     */
    public int attachNewEventToCECase(CECase c, EventCECase e, CodeViolation viol) 
            throws CaseLifecyleException, IntegrationException, ViolationException{
        EventType eventType = e.getCategory().getEventType();
        EventIntegrator ei = getEventIntegrator();
        int insertedEventID = 0;
        
        if(e.getCategory().getCasePhaseChangeRule() != null){
            evalulateCasePhaseChangeRule(c, e);
        }
        
        switch(eventType){
            case Action:
                System.out.println("CaseCoordinator.attachNewEventToCECase: action case");
                insertedEventID = ei.insertEvent(e);
                break;
            case Compliance:
                if(viol != null){
                    System.out.println("CaseCoordinator.attachNewEventToCECase: compliance inside if");
                    viol.setActualComplianceDate(e.getDateOfRecord());
                    insertedEventID = ei.insertEvent(e);
                    checkForFullComplianceAndCloseCaseIfTriggered(c);
                } else {
                    throw new CaseLifecyleException("no violation was included with this compliance event");
                }
                break;
            case Closing:
                System.out.println("CaseCoordinator.attachNewEventToCECase: closing case");
                insertedEventID = processClosingEvent(c, e);
                break;
            default:
                System.out.println("CaseCoordinator.attachNewEventToCECase: default case");
                e.setCaseID(c.getCaseID());
                insertedEventID = ei.insertEvent(e);
        } // close switch
        return insertedEventID;
    } // close method
    
    private boolean evalulateCasePhaseChangeRule(CECase cse, EventCECase event) 
            throws IntegrationException, CaseLifecyleException, ViolationException{
        
        CasePhaseChangeRule rule = event.getCategory().getCasePhaseChangeRule();
        boolean rulePasses = false;
        
        if(rule.getRequiredCurrentCasePhase() != null){
            if(rule.isTreatRequiredPhaseAsThreshold()){
                if (cse.getCasePhase().getOrder() >= rule.getRequiredCurrentCasePhase().getOrder()){
                    if(ruleSubcheck_forbiddenCasePhase(cse, rule)
                            &&
                        ruleSubcheck_requiredEventType(cse, rule)
                            &&
                        ruleSubcheck_forbiddenEventType(cse, rule)
                            &&
                        ruleSubcheck_requiredEventCategory(cse, rule)
                            &&
                        ruleSubcheck_forbiddenEventCategory(cse, rule)){
                            rulePasses = true;
                            implementPassedCasePhaseChangeRule(cse, rule);
                    }
                }
            } else if (cse.getCasePhase() == rule.getRequiredCurrentCasePhase()){
                if(ruleSubcheck_forbiddenCasePhase(cse, rule)
                            &&
                        ruleSubcheck_requiredEventType(cse, rule)
                            &&
                        ruleSubcheck_forbiddenEventType(cse, rule)
                            &&
                        ruleSubcheck_requiredEventCategory(cse, rule)
                            &&
                        ruleSubcheck_forbiddenEventCategory(cse, rule)){
                            rulePasses = true;
                            implementPassedCasePhaseChangeRule(cse, rule);
                }
            }
        }
        return rulePasses;
    }
    
    
    private boolean ruleSubcheck_forbiddenCasePhase(CECase cse, CasePhaseChangeRule rule){
        boolean subcheckPasses = true;
        
        if(rule.isTreatForbiddenPhaseAsThreshold()){
                if (cse.getCasePhase().getOrder() >= rule.getForbiddenCurrentCasePhase().getOrder()){
                    subcheckPasses = false;
                }
            } else if (cse.getCasePhase() == rule.getForbiddenCurrentCasePhase()){
                subcheckPasses = false;
            }
        return subcheckPasses;
    }
    
    
    private boolean ruleSubcheck_requiredEventType(CECase cse, CasePhaseChangeRule rule){
        boolean subcheckPasses = true;
        if(rule.getRequiredExtantEventType() != null){
            subcheckPasses = false;
            Iterator<EventCECase> iter = cse.getVisibleEventList().iterator();
            while(iter.hasNext()){
                EventCECase ev = iter.next();
                if(ev.getCategory().getEventType() == rule.getRequiredExtantEventType()){
                    subcheckPasses = true;
                }
            }
        }
        return subcheckPasses;
    }
   
    
    private boolean ruleSubcheck_forbiddenEventType(CECase cse, CasePhaseChangeRule rule){
        boolean subcheckPasses = true;
        Iterator<EventCECase> iter = cse.getVisibleEventList().iterator();
        while(iter.hasNext()){
            EventCECase ev = iter.next();
            if(ev.getCategory().getEventType() == rule.getRequiredExtantEventType()){
                subcheckPasses = false;
            }
        }
        return subcheckPasses;
    }
    
    private boolean ruleSubcheck_requiredEventCategory(CECase cse, CasePhaseChangeRule rule){
        boolean subcheckPasses = true;
        if(rule.getRequiredExtantEventCatID() != 0){
            subcheckPasses = false;
            Iterator<EventCECase> iter = cse.getVisibleEventList().iterator();
            while(iter.hasNext()){
                EventCECase ev = iter.next();
                if(ev.getCategory().getCategoryID() == rule.getRequiredExtantEventCatID()){
                    subcheckPasses = true;
                }
            }
        }
        return subcheckPasses;
    }
    
    private boolean ruleSubcheck_forbiddenEventCategory(CECase cse, CasePhaseChangeRule rule){
        boolean subcheckPasses = true;
        Iterator<EventCECase> iter = cse.getVisibleEventList().iterator();
        while(iter.hasNext()){
            EventCECase ev = iter.next();
            if(ev.getCategory().getCategoryID() == rule.getRequiredExtantEventCatID()){
                subcheckPasses = false;
            }
        }
        return subcheckPasses;
    }
    
    private void implementPassedCasePhaseChangeRule(CECase cse, CasePhaseChangeRule rule) 
            throws IntegrationException, CaseLifecyleException, ViolationException{
        CaseIntegrator ci = getCaseIntegrator();
        EventCoordinator ec = getEventCoordinator();
        EventCECase newEvent = null;
        
        CasePhase oldCP = cse.getCasePhase();
        cse.setCasePhase(rule.getTargetCasePhase());
        ci.changeCECasePhase(cse);
        ec.generateAndInsertPhaseChangeEvent(cse, oldCP, rule);
        if(rule.getTriggeredEventCategoryID() != 0){
            newEvent = ec.getInitializedEvent(cse, ec.getInitiatlizedEventCategory(rule.getTriggeredEventCategoryID()));
            if(rule.getTriggeredEventCategoryRequestedEventCatID() != 0){
                newEvent.setRequestedEventCat(ec.getInitiatlizedEventCategory(rule.getTriggeredEventCategoryRequestedEventCatID()));
            }
            attachNewEventToCECase(cse, newEvent, null);
            System.out.println("CaseCoordinator.implementPassedCasePhaseChangeRule "  + newEvent.getCategory().getEventCategoryTitle());
        }
        
    }
   
   
    
     /**
     * Implements business rules for determining which event types are allowed
     * to be attached to the given CECase based on the case's phase and the
     * user's permissions in the system.
     * 
     * Used for displaying the appropriate event types to the user on the
     * cecases.xhtml page
     * 
     * @param c the CECase on which the event would be attached
     * @param u the User doing the attaching
     * @return allowed EventTypes for attaching to the given case
     */
    public List<EventType> getPermittedEventTypesForCECase(CECase c, User u){
        List<EventType> typeList = new ArrayList<>();
        RoleType role = u.getRoleType();
        
        if(role == RoleType.EnforcementOfficial 
                || u.getRoleType() == RoleType.Developer){
            typeList.add(EventType.Action);
            typeList.add(EventType.Timeline);
        }
        
        if(role != RoleType.MuniReader){
            typeList.add(EventType.Communication);
            typeList.add(EventType.Meeting);
            typeList.add(EventType.Custom);
        }
        return typeList;
    }
    
    /**
     * A CECase's Stage is derived from its Phase based on the set of business
     * rules encoded in this method.
     * @param cs which needs its StageConfigured
     * @return the same CECas passed in with the CaseStage configured
     * @throws CaseLifecyleException 
     */
     public CECaseBaseClass setCaseStage(CECaseBaseClass cs) throws CaseLifecyleException {
        CaseStage stage;
        switch (cs.getCasePhase()) {
            case PrelimInvestigationPending:
                stage = CaseStage.Investigation;
                break;
            case NoticeDelivery:
                stage = CaseStage.Investigation;
                break;
        // Letter marked with a send date
            case InitialComplianceTimeframe:
                stage = CaseStage.Enforcement;
                break;
        // compliance inspection
            case SecondaryComplianceTimeframe:
                stage = CaseStage.Enforcement;
                break;
        // Filing of citation
            case AwaitingHearingDate:
                stage = CaseStage.Citation;
                break;
        // hearing date scheduled
            case HearingPreparation:
                stage = CaseStage.Citation;
                break;
        // hearing not resulting in a case closing
            case InitialPostHearingComplianceTimeframe:
                stage = CaseStage.Citation;
                break;
            case SecondaryPostHearingComplianceTimeframe:
                stage = CaseStage.Citation;
                break;
            case Closed:
                stage = CaseStage.Closed;
                // TODO deal with this later
                //                throw new CaseLifecyleException("Cannot advance a closed case to any other phase");
                break;
            case InactiveHolding:
                stage = CaseStage.Closed;
                break;
            default:
                stage = CaseStage.Closed;
        }
        cs.setCaseStage(stage);
        return cs;
    }
    
    
    
    /**
     * Iterates over all of a case's violations and checks for compliance. 
     * If all of the violations have a compliance date, the case is automatically
     * closed and a case closing event is generated and added to the case
     * 
     * @param c the case whose violations should be checked for compliance
     * @throws IntegrationException
     * @throws CaseLifecyleException
     * @throws ViolationException 
     */
    private void checkForFullComplianceAndCloseCaseIfTriggered(CECase c) 
            throws IntegrationException, CaseLifecyleException, ViolationException{
        
        EventCoordinator ec = getEventCoordinator();
        EventIntegrator ei = getEventIntegrator();
        ViolationIntegrator cvi = getCodeViolationIntegrator();
        
        List caseViolationList = cvi.getCodeViolations(c);
        boolean complianceWithAllViolations = false;
        ListIterator<CodeViolation> fullViolationLi = caseViolationList.listIterator();
        CodeViolation cv;
        
        while(fullViolationLi.hasNext())
        {
            cv = fullViolationLi.next();
            // if there are any outstanding code violations, toggle switch to 
            // false and exit the loop. Phase change will not occur
            if(cv.getActualComplianceDate() != null){
                complianceWithAllViolations = true;
            } else {
                complianceWithAllViolations = false;
                break;
            }
        } // close while
        
        EventCECase complianceClosingEvent;
        if (complianceWithAllViolations){
            complianceClosingEvent = ec.getInitializedEvent(c, ei.getEventCategory(Integer.parseInt(getResourceBundle(
                Constants.EVENT_CATEGORY_BUNDLE).getString("closingAfterFullCompliance"))));
            attachNewEventToCECase(c, complianceClosingEvent, null);
            
        } // close if
        
    }
     
    private int processClosingEvent(CECase c, EventCECase e) throws IntegrationException, CaseLifecyleException{
        CaseIntegrator ci = getCaseIntegrator();
        EventIntegrator ei = getEventIntegrator();
        
        CasePhase closedPhase = CasePhase.Closed;
        c.setCasePhase(closedPhase);
        ci.changeCECasePhase(c);
        
        c.setClosingDate(LocalDateTime.now());
        updateCoreCECaseData(c);
        // now load up the closing event before inserting it
        // we'll probably want to get this text from a resource file instead of
        // hardcoding it down here in the Java
        e.setDateOfRecord(LocalDateTime.now());
        e.setOwner(getFacesUser());
        e.setDescription(getResourceBundle(Constants.MESSAGE_TEXT).getString("automaticClosingEventDescription"));
        e.setNotes(getResourceBundle(Constants.MESSAGE_TEXT).getString("automaticClosingEventNotes"));
        e.setCaseID(c.getCaseID());
        return ei.insertEvent(e);
    }
    
    /**
     * @deprecated 
     * @param c
     * @param e
     * @throws CaseLifecyleException
     * @throws IntegrationException
     * @throws ViolationException 
     */
    private void checkForAndCarryOutCasePhaseChange(CECase c, EventCECase e) throws CaseLifecyleException, IntegrationException, ViolationException{
        
        CaseIntegrator ci = getCaseIntegrator();
        CasePhase initialCasePhase = c.getCasePhase();
        EventCoordinator ec = getEventCoordinator();
        // this value is used to compare to the category IDs listed in the resource bundle
        int evCatID = e.getCategory().getCategoryID();
        
        // check to see if the event triggers a case phase chage. 
        
        if(
            (initialCasePhase == CasePhase.PrelimInvestigationPending 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToNoticeDelivery")))

            ||

            (initialCasePhase == CasePhase.NoticeDelivery 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToInitialComplianceTimeframe")))

            ||

            (initialCasePhase == CasePhase.InitialComplianceTimeframe 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToSecondaryComplianceTimeframe")))

            ||

            (initialCasePhase == CasePhase.SecondaryComplianceTimeframe 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToAwaitingHearingDate")))

            ||

            (initialCasePhase == CasePhase.AwaitingHearingDate 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToHearingPreparation")))

            ||

            (initialCasePhase == CasePhase.HearingPreparation 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToInitialPostHearingComplianceTimeframe")))

            || 

            (initialCasePhase == CasePhase.InitialPostHearingComplianceTimeframe 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToSecondaryPostHearingComplianceTimeframe")))
        ){
            // write the phase change to the DB
            // we must ship the case to the integrator with the case phase updated
            // because the integrator does not implement any business logic
            c.setCasePhase(getNextCasePhase(c));
            ci.changeCECasePhase(c);
            
            // generate event for phase change and write
            ec.generateAndInsertPhaseChangeEvent(c, initialCasePhase, null); 

        } 
        
      
        
    }
    
    
    /**
     * Utility method for determining which CasePhase follows any given case's CasePhase.
     * 
     * @deprecated 
     * @param c the case whose set CasePhase will be read to determine the next CasePhase
     * @return the ONLY CasePhase to which any case can be changed to without a manual protocol
     * override request
     * @throws CaseLifecyleException thrown when no next CasePhase exists or the next
     * CasePhase cannot be determined
     */
    public CasePhase getNextCasePhase(CECase c) throws CaseLifecyleException{
        CasePhase currentPhase = c.getCasePhase();
        CasePhase nextPhaseInSequence = null;
        
        switch(currentPhase){
            
            case PrelimInvestigationPending:
                nextPhaseInSequence = CasePhase.NoticeDelivery;
                break;
                // conduct inital investigation
                // compose and deply notice of violation
            case NoticeDelivery:
                nextPhaseInSequence = CasePhase.InitialComplianceTimeframe;
                break;
                // Letter marked with a send date
            case InitialComplianceTimeframe:
                nextPhaseInSequence = CasePhase.SecondaryComplianceTimeframe;
                break;
                // compliance inspection
            case SecondaryComplianceTimeframe:
                nextPhaseInSequence = CasePhase.AwaitingHearingDate;
                break;
                // Filing of citation
            case AwaitingHearingDate:
                nextPhaseInSequence = CasePhase.HearingPreparation;
                break;
                // hearing date scheduled
            case HearingPreparation:
                nextPhaseInSequence = CasePhase.InitialPostHearingComplianceTimeframe;
                break;
                // hearing not resulting in a case closing
            case InitialPostHearingComplianceTimeframe:
                nextPhaseInSequence = CasePhase.SecondaryPostHearingComplianceTimeframe;
                break;
            
            case SecondaryPostHearingComplianceTimeframe:
                nextPhaseInSequence = CasePhase.HearingPreparation;
                break;
                
            case Closed:
                // TODO deal with this later
//                throw new CaseLifecyleException("Cannot advance a closed case to any other phase");
                break;
            case InactiveHolding:
                nextPhaseInSequence = CasePhase.InactiveHolding;
                break;
                
            default:
                nextPhaseInSequence = CasePhase.InactiveHolding;
        }
        
        return nextPhaseInSequence;
    }
    
    
   
    
    public void novResetMailing(NoticeOfViolation nov, User user) throws IntegrationException, PermissionsException{
        ViolationIntegrator cvi = getCodeViolationIntegrator();
        if(user.getKeyCard().isHasSysAdminPermissions()){
            cvi.novResetMailingFieldsToNull(nov);
        } else {
            throw new PermissionsException("User does not have sufficient acces righst to clear notice mailing fields");
        }
    }
    
        
    public NoticeOfViolation novGetNewNOVSkeleton(CECase cse, Municipality m){
        SystemIntegrator si = getSystemIntegrator();
        NoticeOfViolation nov = new NoticeOfViolation();
        nov.setViolationList(new ArrayList<CodeViolationDisplayable>());
        nov.setDateOfRecord(LocalDateTime.now());
        try {
            nov.setStyle(si.getPrintStyle(m.getDefaultNOVStyleID()));
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        
        
        // loop over unresolved violations on case and generate CodeViolationDisplayable obects
        Iterator<CodeViolation> iter = cse.getViolationListUnresolved().iterator();
        while(iter.hasNext()){
            CodeViolation cv = iter.next();
            CodeViolationDisplayable cvd = new CodeViolationDisplayable(cv);
            cvd.setIncludeHumanFriendlyText(false);
            cvd.setIncludeOrdinanceText(true);
            cvd.setIncludeViolationPhotos(false);
            nov.getViolationList().add(cvd);
        }
        
        return nov;
        
    }
    
    
    public void novLockAndQueue(CECase c, NoticeOfViolation nov, User user) 
            throws CaseLifecyleException, IntegrationException, EventException, ViolationException{
        
        ViolationIntegrator cvi = getCodeViolationIntegrator();
        EventCoordinator evCoord = getEventCoordinator();
        PersonCoordinator pc = getPersonCoordinator();
        PersonIntegrator pi = getPersonIntegrator();
        
        if(nov.getLockedAndqueuedTS() == null){
            int ghostID = pc.createChostPerson(nov.getRecipient(), user);
            nov.setRecipient(pi.getPerson(ghostID));
            nov.setLockedAndqueuedTS(LocalDateTime.now());
            nov.setLockedAndQueuedBy(user);
            cvi.novLockAndQueueForMailing(nov);
            System.out.println("CaseCoordinator.novLockAndQueue | NOV locked in integrator");
        } else {
            throw new CaseLifecyleException("Notice is already locked and queued for sending");
        }
        
        EventCECase noticeEvent = evCoord.getInitializedEvent(c, evCoord.getInitiatlizedEventCategory(Integer.parseInt(getResourceBundle(
                Constants.EVENT_CATEGORY_BUNDLE).getString("noticeQueued"))));
        String queuedNoticeEventNotes = getResourceBundle(Constants.MESSAGE_TEXT).getString("noticeQueuedEventDesc");
        noticeEvent.setDescription(queuedNoticeEventNotes);
        noticeEvent.setOwner(user);
        noticeEvent.setDiscloseToMunicipality(true);
        noticeEvent.setDiscloseToPublic(true);
        ArrayList<Person> al = new ArrayList();
        al.add(nov.getRecipient());
        noticeEvent.setPersonList(al);
        attachNewEventToCECase(c, noticeEvent, null);
    }
    
    public void refreshCase(CECase c) throws IntegrationException, CaseLifecyleException{
        System.out.println("CaseCoordinator.refreshCase");
        CaseIntegrator ci = getCaseIntegrator();
        
        getSessionBean().setcECase(ci.getCECase(c.getCaseID()));
    }
    
    public int novInsertNotice(NoticeOfViolation nov, CECase cse, User usr) throws IntegrationException{
        ViolationIntegrator vi = getCodeViolationIntegrator();
        System.out.println("CaseCoordinator.novInsertNotice");
        return vi.novInsert(cse, nov);
    }
    
    public void novUpdate(NoticeOfViolation nov) throws IntegrationException{
        ViolationIntegrator vi = getCodeViolationIntegrator();
        vi.novUpdate(nov);
    }
    
    public void novMarkAsSent(CECase ceCase, NoticeOfViolation nov, User user) throws CaseLifecyleException, EventException, IntegrationException{
        ViolationIntegrator cvi = getCodeViolationIntegrator();
        nov.setSentTS(LocalDateTime.now());
        nov.setSentBy(user);
        cvi.novUpdate(nov);   
    }
    
    public void novMarkAsReturned(CECase c, NoticeOfViolation nov, User user) throws IntegrationException, CaseLifecyleException{
        ViolationIntegrator cvi = getCodeViolationIntegrator();
        nov.setReturnedTS(LocalDateTime.now());
        nov.setReturnedBy(user);
        cvi.novUpdate(nov);
        refreshCase(c);
    } 
    
    public void novDelete(NoticeOfViolation nov) throws CaseLifecyleException{
        ViolationIntegrator cvi = getCodeViolationIntegrator();

        //cannot delete a letter that was already sent
        if(nov != null && nov.getSentTS() != null){
            throw new CaseLifecyleException("Cannot delete a letter that has been sent");
        } else {
            try {
                cvi.novDelete(nov);
            } catch (IntegrationException ex) {
                System.out.println(ex);
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Unable to delete notice of violation due to a database error", ""));
            }
        }
    }
    
   
   public Citation generateNewCitation(List<CodeViolation> violationList){
       Citation newCitation = new Citation();
       List<CodeViolation> al = new ArrayList<>();
       ListIterator<CodeViolation> li = violationList.listIterator();
       CodeViolation cv;
       
       StringBuilder notesBuilder = new StringBuilder();
       notesBuilder.append("Failure to comply with the following ordinances:\n");
       
       while(li.hasNext()){
           
           cv = li.next();
           System.out.println("CaseCoordinator.generateNewCitation | linked list item: " 
                   + cv.getDescription());
           
           // build a nice note section that lists the elements cited
           notesBuilder.append("* Chapter ");
           notesBuilder.append(cv.getCodeViolated().getCodeElement().getOrdchapterNo());
           notesBuilder.append(":");
           notesBuilder.append(cv.getCodeViolated().getCodeElement().getOrdchapterTitle());
           notesBuilder.append(", Section ");
           notesBuilder.append(cv.getCodeViolated().getCodeElement().getOrdSecNum());
           notesBuilder.append(":");
           notesBuilder.append(cv.getCodeViolated().getCodeElement().getOrdSecTitle());
           notesBuilder.append(", Subsection ");
           notesBuilder.append(cv.getCodeViolated().getCodeElement().getOrdSubSecNum());
           notesBuilder.append(": ");
           notesBuilder.append(cv.getCodeViolated().getCodeElement().getOrdSubSecTitle());
           notesBuilder.append("\n\n");
           
           al.add(cv);
           
       }
       newCitation.setViolationList(al);
       newCitation.setNotes(notesBuilder.toString());
       newCitation.setIsActive(true);
       
       return newCitation;
   }
   
   /**
    * Implements business logic before updating a CECase's core data (opening date,
    * closing date, etc.). If all is well, pass to integrator.
    * @param c the CECase to be updated
    * @throws CaseLifecyleException
    * @throws IntegrationException 
    */
   public void updateCoreCECaseData(CECase c) throws CaseLifecyleException, IntegrationException{
       CaseIntegrator ci = getCaseIntegrator();
       if(c.getClosingDate() != null){
            if(c.getClosingDate().isBefore(c.getOriginationDate())){
                throw new CaseLifecyleException("You cannot update a case's origination date to be after its closing date");
            }
       }
       ci.updateCECaseMetadata(c);
   }
   
   public void deleteCitation(Citation c) throws IntegrationException{
       CitationIntegrator citint = getCitationIntegrator();
       citint.deleteCitation(c);
              
   }
   
   public void issueCitation(Citation c) throws IntegrationException{
       CitationIntegrator citint = getCitationIntegrator();
       citint.insertCitation(c);
       
   }
   
   public void updateCitation(Citation c) throws IntegrationException{
       CitationIntegrator citint = getCitationIntegrator();
       citint.updateCitation(c);
       
   }
   
   /**
    * Factory method for our CEActionRequests - initializes the date as well
    * @return The CEActionRequest ready for populating with user values
    */
   public CEActionRequest getInititalizedCEActionRequest(){
       System.out.println("CaseCoordinator.getNewActionRequest");
       CEActionRequest cear = new CEActionRequest();
       // start by writing in the current date
       cear.setDateOfRecordUtilDate(
               java.util.Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
       
       return new CEActionRequest();
       
   }
   
   /**
    * Utility method for determining whether or not the panel of Code Enforcement request
    * routing buttons can be pressed. Used by the view for setting disabled properties on buttons
    * requests 
    * @param req the current CE Request
    * @param u current user
    * @return True if the current user can route the given ce request
    */
   public boolean determineCEActionRequestRoutingActionEnabledStatus(
                                                        CEActionRequest req,
                                                        User u ){
       if(req != null && u.getKeyCard() != null){
            if((
                    req.getRequestStatus().getStatusID() == 
                    Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                            .getString("actionRequestInitialStatusCode")))
                    && 
                    u.getKeyCard().isHasEnfOfficialPermissions()
                ){
                return true;
            }
        }
       return false;
   }
   
   public ReportConfigCECase getDefaultReportConfigCECase(CECase c){
        ReportConfigCECase rpt = new ReportConfigCECase();
        
        // general
        rpt.setIncludeCaseName(false);
        
        // events
        rpt.setIncludeEventNotes(true);
        rpt.setIncludeHiddenEvents(false);
        rpt.setIncludeInactiveEvents(false);
        rpt.setIncludeRequestedActionFields(false);
        rpt.setIncludeMunicipalityDiclosedEvents(true);
        rpt.setIncludeOfficerOnlyEvents(false);
        
        // notices of violation
        rpt.setIncludeAllNotices(false);
        rpt.setIncludeNoticeFullText(true);
        // violations
        rpt.setIncludeFullOrdinanceText(true);
        rpt.setIncludeViolationPhotos(true);
        
       return rpt;
   }
   
   public ReportConfigCECaseList getDefaultReportConfigCECaseList(){
       ReportConfigCECaseList listRpt = new ReportConfigCECaseList();
       listRpt.setIncludeListSummaryFigures(true);
       listRpt.setIncludeCaseNames(true);
       listRpt.setIncludeFullOwnerContactInfo(true);
       listRpt.setIncludeViolationList(true);
       listRpt.setIncludeEventSummaryByCase(false);
       return listRpt;
       
       
   }
   
   
   /**
    * Primary configuration mechanism for customizing report data from the 
    * ceCases.xhtml display. Called by the CECasesBB.
    * 
    * @param rptCse
    * @return
    * @throws IntegrationException 
    */
   public ReportConfigCECase transformCECaseForReport(ReportConfigCECase rptCse) throws IntegrationException, CaseLifecyleException{
       CaseIntegrator ci = getCaseIntegrator();
       // we actually get an entirely new object instead of editing the 
       // one we used throughout the ceCases.xhtml
       CECase c = ci.getCECase(rptCse.getCse().getCaseID());
       
       List<EventCECase> evList =  new ArrayList<>();
       Iterator<EventCECase> iter = c.getVisibleEventList().iterator();
       while(iter.hasNext()){
            EventCECase ev = iter.next();
            
            // toss out hidden events unless the user wants them
            if(ev.isHidden() && !rptCse.isIncludeHiddenEvents()) continue;
            // toss out inactive events unless user wants them
            if(!ev.isActive()&& !rptCse.isIncludeInactiveEvents()) continue;
            // toss out events only available internally to the muni users unless user wants them
            if(!ev.isDiscloseToMunicipality() && !rptCse.isIncludeMunicipalityDiclosedEvents()) continue;
            // toss out officer only events unless the user wants them
            if((!ev.isDiscloseToMunicipality() && !ev.isDiscloseToPublic()) 
                    && !rptCse.isIncludeOfficerOnlyEvents()) continue;
            evList.add(ev);
       }
       c.setVisibleEventList(evList);
       List<NoticeOfViolation> noticeList = new ArrayList<>();
       Iterator<NoticeOfViolation> iterNotice = c.getNoticeList().iterator();
       while(iterNotice.hasNext()){
           NoticeOfViolation nov = iterNotice.next();
           // skip unsent notices
           if(nov.getSentTS() == null) continue;
           // if the user dones't want all notices, skip returned notices
           if(!rptCse.isIncludeAllNotices() && nov.getReturnedTS() != null  ) continue;
           noticeList.add(nov);
       }
       c.setNoticeList(noticeList);
       return rptCse;
   }
   
   /**
    * Currently a pass-through method for object creation
    * @param ec
    * @return
    * @throws IntegrationException 
    */
   public CasePhaseChangeRule getCasePhaseChangeRule(EventCategory ec) throws IntegrationException{
       CaseIntegrator ci = getCaseIntegrator();
       return ci.getPhaseChangeRule(ec.getCasePhaseChangeRule().getRuleID());
       
   }
   
   
    
    public CodeViolation generateNewCodeViolation(CECase c, EnforcableCodeElement ece){
        CodeViolation v = new CodeViolation();
        
        System.out.println("ViolationCoordinator.generateNewCodeViolation | enfCodeElID:" + ece.getCodeSetElementID());
        
        v.setViolatedEnfElement(ece);
        v.setStipulatedComplianceDate(LocalDateTime.now()
                .plusDays(ece.getNormDaysToComply()));
        v.setPenalty(ece.getNormPenalty());
        v.setDateOfRecord(LocalDateTime.now());
        v.setCeCaseID(c.getCaseID());
        // control is passed back to the violationAddBB which stores this 
        // generated violation under teh activeCodeViolation in the session
        // which the ViolationAddBB then picks up and edits
        
        return v;
    }
    
    /**
     * Standard coordinator method which calls the integration method after 
     * checking businses rules. 
     * ALSO creates a corresponding timeline event to match the stipulated compliance
     * date on the violation that's added.
     * @param cv
     * @param cse
     * @return the database key assigned to the inserted violation
     * @throws IntegrationException
     * @throws ViolationException 
     * @throws com.tcvcog.tcvce.domain.CaseLifecyleException 
     */
    public int attachViolationToCaseAndInsertTimeFrameEvent(CodeViolation cv, CECase cse) throws IntegrationException, ViolationException, CaseLifecyleException{
        
        ViolationIntegrator vi = getCodeViolationIntegrator();
        EventCoordinator ec = getEventCoordinator();
        UserCoordinator uc = getUserCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        EventCECase tfEvent;
        int insertedViolationID;
        int eventID;
        StringBuilder sb = new StringBuilder();
        
        EventCategory eventCat = ec.getInitiatlizedEventCategory(
                                Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE)
                                .getString("complianceTimeframeExpiry")));
//        EventCategory eventCat = ec.getInitiatlizedEventCategory(113);
        tfEvent = ec.getInitializedEvent(cse, eventCat);
        tfEvent.setDateOfRecord(cv.getStipulatedComplianceDate());
        tfEvent.setOwner(cse.getCaseManager());
        tfEvent.setRequestActionByDefaultMuniCEO(true);
        eventCat = ec.getInitiatlizedEventCategory(
                Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE)
                .getString("propertyInspection")));
        tfEvent.setRequestedEventCat(eventCat);
        tfEvent.setActionRequestedBy(uc.getRobotUser());
        
        sb.append(getResourceBundle(Constants.MESSAGE_TEXT)
                        .getString("complianceTimeframeEndEventDesc"));
        sb.append("Case: ");
        sb.append(cse.getCaseName());
        sb.append(" at ");
        sb.append(cse.getProperty().getAddress());
        sb.append("(");
        sb.append(cse.getProperty().getMuni().getMuniName());
        sb.append(")");
        sb.append("; Violation: ");
        sb.append(cv.getViolatedEnfElement().getCodeElement().getHeaderString());
        tfEvent.setDescription(sb.toString());
        
        if(verifyCodeViolationAttributes(cse, cv)){
            eventID = cc.attachNewEventToCECase(cse, tfEvent, cv);
            cv.setComplianceTimeframeEventID(eventID);
            insertedViolationID = vi.insertCodeViolation(cv);
        } else {
            throw new ViolationException("Failed violation verification");
        }
        return insertedViolationID;
    }
    
    /**
     * Uses date fields on the populated CodeViolation to determine
     * a status string and icon for UI
     * Called by the integrator when creating a code violation
     * @param cv
     * @return the CodeViolation with correct icon and statusString
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public CodeViolation configureCodeViolation(CodeViolation cv) throws IntegrationException{
        SystemIntegrator si = getSystemIntegrator();
        if(cv.getActualComplianceDate() == null){
            // violation still within compliance timeframe
            if(cv.getDaysUntilStipulatedComplianceDate() >= 0){
                cv.setStatusString(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString("codeviolation_unresolved_withincomptimeframe_statusstring"));
                cv.setIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString("codeviolation_unresolved_withincomptimeframe_iconid"))));
                cv.setAgeLeadText(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString("codeviolation_unresolved_withincomptimeframe_ageleadtext"));
                
            // violation has not been cited, but is past compliance timeframe end date
            } else if(cv.getCitationIDList().isEmpty()) {
                cv.setStatusString(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString("codeviolation_unresolved_overdue_statusstring"));
                cv.setIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString("codeviolation_unresolved_overdue_iconid"))));
                cv.setAgeLeadText(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString("codeviolation_unresolved_overdue_ageleadtext"));
            // violation has been cited on at least one citation
            } else {
                cv.setStatusString(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString("codeviolation_unresolved_citation_statusstring"));
                cv.setIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString("codeviolation_unresolved_citation_iconid"))));
                cv.setAgeLeadText(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString("codeviolation_unresolved_citation_ageleadtext"));
            }
            // we have a resolved violation
        } else {
            cv.setStatusString(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                    .getString("codeviolation_resolved_statusstring"));
            cv.setIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                    .getString("codeviolation_resolved_iconid"))));
        }
        return cv;
    }
    
    
    private boolean verifyCodeViolationAttributes(CECase cse, CodeViolation cv) throws ViolationException{
        if(cse.getCasePhase() == CasePhase.Closed){
            throw new ViolationException("Cannot update code violations on closed cases!");
            
        }
        if(cv.getStipulatedComplianceDate().isBefore(cv.getDateOfRecord())){
            throw new ViolationException("Stipulated compliance date cannot be before the violation's date of record");
        }
        
        return true;
    }
    
    public void updateCodeViolation(CECase cse, CodeViolation cv, User u) throws ViolationException, IntegrationException{
        EventCoordinator ec = getEventCoordinator();
        ViolationIntegrator cvi = getCodeViolationIntegrator();
        EventIntegrator ei = getEventIntegrator();
        if(verifyCodeViolationAttributes(cse, cv)){
            cvi.updateCodeViolation(cv);
            
            // if we update the code violation, make sure to update any associated compliance timeframe events!
            if(cv.getCompTimeFrameComplianceEvent() != null || cv.getComplianceTimeframeEventID() != 0){
                int violTimeframeEventID;
                // cope with the condition that incoming code violations may only have the id
                // of the assocaited event and not the entire object
                if(cv.getCompTimeFrameComplianceEvent() != null){
                     violTimeframeEventID = cv.getCompTimeFrameComplianceEvent().getEventID();
                } else {
                    violTimeframeEventID = cv.getComplianceTimeframeEventID();
                }
                EventCECase tfEvent = ei.getEventCECase(violTimeframeEventID);
                tfEvent.setDateOfRecord(cv.getStipulatedComplianceDate());
                ec.editEvent(tfEvent, u);
                System.out.println("CaseCoordinator.updateCodeViolation | updated timeframe event ID: " + tfEvent.getEventID());
            }
            
            
        }
    }
    
    /**
     * CodeViolation should have the actual compliance date set from the user's 
     * event date of record
     * @param cv
     * @param u
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void recordCompliance(CodeViolation cv, User u) throws IntegrationException{
        
        ViolationIntegrator cvi = getCodeViolationIntegrator();
        EventIntegrator ei = getEventIntegrator();
        // update violation record for compliance
        cv.setComplianceUser(u);
        cvi.recordCompliance(cv);
                
        // inactivate timeframe expiry event
        if(cv.getCompTimeFrameComplianceEvent() != null || cv.getComplianceTimeframeEventID() != 0){
            int violTimeframeEventID;
            // cope with the condition that incoming code violations may only have the id
            // of the assocaited event and not the entire object
            if(cv.getCompTimeFrameComplianceEvent() != null){
                 violTimeframeEventID = cv.getCompTimeFrameComplianceEvent().getEventID();
            } else {
                violTimeframeEventID = cv.getComplianceTimeframeEventID();
            }
            System.out.println("ViolationCoordinator.recordCompliance | invalidating event id: " + violTimeframeEventID);
            ei.inactivateEvent(violTimeframeEventID);
        }
    }

    
    public void deleteViolation(CodeViolation cv) throws IntegrationException{
        ViolationIntegrator cvi = getCodeViolationIntegrator();
        cvi.deleteCodeViolation(cv);
    }
    
    public List getCodeViolations(CECase ceCase) throws IntegrationException{
        ViolationIntegrator cvi = getCodeViolationIntegrator();
        List al = cvi.getCodeViolations(ceCase);
        return al;
    }
    
   
} // close class
