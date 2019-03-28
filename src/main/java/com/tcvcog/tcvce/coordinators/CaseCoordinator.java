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
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.AccessKeyCard;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EventCECase;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.SearchParamsCEActionRequests;
import com.tcvcog.tcvce.entities.search.SearchParamsCECases;
import com.tcvcog.tcvce.entities.search.SearchParamsCEEvents;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.CitationIntegrator;
import com.tcvcog.tcvce.integration.CodeViolationIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
     * The temporarily hard-coded values for default search parameters for various
     * types of search Param objects
     * 
     * @param m
     * @return an search params object for CEAction requests with default values
     * which amount to requests that aren't attached to a case and were submitted
     * within the past 10 years
     */
    public SearchParamsCEActionRequests getDefaultSearchParamsCEActionRequests(Municipality m){
        
            System.out.println("CaseCoordinator.configureDefaultSearchParams "
                    + "| found actionrequest param object");
            
            SearchParamsCEActionRequests sps = new SearchParamsCEActionRequests();

            sps.setMuni(m);
            LocalDateTime pastTenYears = LocalDateTime.now().minusYears(10);
            sps.setStartDate(pastTenYears);
            
            // action requests cannot have a time stamp past the current datetime
            sps.setEndDate(LocalDateTime.now());

            sps.setUseAttachedToCase(true);
            sps.setAttachedToCase(false);
            sps.setUseMarkedUrgent(false);
            sps.setUseNotAtAddress(false);
            sps.setUseRequestStatus(false);
        
        return sps;
    }
    
    
    
     /**
     * Returns a SearchParams subclass for retrieving all open
     * cases in a given municipality. Open cases are defined as a 
     * case whose closing date is null.
     * @param m
     * @return a SearchParams subclass with mem vars ready to send
     * into the Integrator for case list retrieval
     */
    public SearchParamsCECases getDefaultSearchParamsCECase(Municipality m){
        SearchParamsCECases params = new SearchParamsCECases();
        
        // superclass 
        params.setFilterByMuni(true);
        params.setMuni(m);
        params.setFilterByObjectID(false);
        params.setLimitResultCountTo100(true);
        
        // subclass specific
        params.setUseIsOpen(true);
        params.setIsOpen(true);
        
        params.setDateToSearchCECases("Opening date of record");
        params.setUseCaseManager(false);
        
        params.setUseCasePhase(false);
        params.setUseCaseStage(false);
        params.setUseProperty(false);
        params.setUsePropertyInfoCase(false);
        params.setUseCaseManager(false);
        
        return params;
    }
    
    /**
     * Existed before queryCases() became the goto way to retrieve lists of cases
     * @deprecated 
     * @param m
     * @return
     * @throws IntegrationException 
     */
    public List<CECase> getOpenCECaseList(Municipality m) throws IntegrationException{
        CaseIntegrator ci = getCaseIntegrator();
        List<CECase> cList = ci.queryCECases(getDefaultSearchParamsCECase(m));
        return cList;
    }
    
    /**
     * Front door for querying cases in the DB
     * 
     * @param params pre-configured search parameters
     * @return
     * @throws IntegrationException 
     */
    public List<CECase> queryCECases(SearchParamsCECases params) throws IntegrationException{
        CaseIntegrator ci = getCaseIntegrator();
        return ci.queryCECases(params);
        
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
            sb.append(cear.getActionRequestorPerson().getFirstName());
            sb.append(" ");
            sb.append(cear.getActionRequestorPerson().getLastName());
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
            attachNewEvent(newCase, originationEvent);
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
     * @throws com.tcvcog.tcvce.domain.CaseLifecyleException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.ViolationException
     */
    public void attachNewEvent(CECase c, EventCECase e) 
            throws CaseLifecyleException, IntegrationException, ViolationException{
        EventType eventType = e.getCategory().getEventType();
         
        
        switch(eventType){
            case Action:
                processActionEvent(c, e);
                break;
            case Compliance:
                // deprecated--directly call attachNewComplianceEvent instead
                break;
            case Closing:
                processClosingEvent(c, e);
                break;
            default:
                processGeneralEvent(c, e);
        } // close switch
    } // close method
   
    /**
     * Core business logic method for recording compliance for CodeViolations
     * Checks for timeline fidelity before updating each violation.
     * If all is well, a call to updateViolation on the ViolationCoordinato is called.
     * After the violations have been marked with compliance, a review of the entire case
     * is conducted and if all violations on the case have a compliance date, 
     * the case phase is automatically changed to closed due to compliance
     * 
     * @param c the current case
     * @param e the Compliance event
     * @param viol
     * @throws ViolationException in the case of a malformed violation
     * @throws IntegrationException in the case of a DB error
     * @throws CaseLifecyleException in the case of date mismatch
     */
    public void attachNewComplianceEvent(CECase c, EventCECase e, CodeViolation viol) 
            throws ViolationException, IntegrationException, CaseLifecyleException{
        
        
        ViolationCoordinator vc = getViolationCoordinator();
        EventCoordinator ec = getEventCoordinator();
        
            viol.setActualComplianceDate(e.getDateOfRecord());
            vc.updateCodeViolation(viol);
        
        // first insert our nice compliance event for all selected violations
        attachNewEvent(c, e);
        // then look at the whole case and close if necessary
        checkForFullComplianceAndCloseCaseIfTriggered(c);
        
        
    } // close method
    
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
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        
        
        ArrayList caseViolationList = cvi.getCodeViolations(c);
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
                
                System.out.println("CaseCoordinator.processComplianceEvent | "
                    + "Found violation with a compliance date and toggled to true: " + cv.getActualComplianceDate());
                
            } else {
                complianceWithAllViolations = false;
                
                System.out.println("CaseCoordinator.processComplianceEvent | Found uncomplied violations, toggling to false and breaking out of while");
                
                break;
            }
            System.out.println("CaseCoordinator.processComplianceEvent | "
                    + "inside while loop for compliance check with all violations: " + complianceWithAllViolations);
        } // close while
        
        EventCECase complianceClosingEvent;
        
        if (complianceWithAllViolations){
            System.out.println("CaseCoordinator.processComplianceEvent | "
                    + "Inside clase closing if");
                   
            complianceClosingEvent = ec.getInitializedEvent(c, ei.getEventCategory(Integer.parseInt(getResourceBundle(
                Constants.EVENT_CATEGORY_BUNDLE).getString("closingAfterFullCompliance"))));
            attachNewEvent(c, complianceClosingEvent);
            
        } // close if
        
    }
    
     
    private void processClosingEvent(CECase c, EventCECase e) throws IntegrationException, CaseLifecyleException{
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
        ei.insertEvent(e);
        
    }
    
    
    
    
    /**
     * Main controller method for event-related life cycle events. Requires event to be
     * loaded up with a caseID and an eventType. No eventID is required since it
     * has not yet been logged into the db.
     * @param c code enforcement case
     * @param e event to process
     * @throws com.tcvcog.tcvce.domain.CaseLifecyleException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    private void processActionEvent(CECase c, EventCECase e) throws CaseLifecyleException, IntegrationException, ViolationException{
        
        EventCoordinator ec = getEventCoordinator();
        // insert the triggering action event
        attachNewEvent(c, e);
        //then pass event to check for phase changes
        checkForAndCarryOutCasePhaseChange(c, e);
        refreshCase(c);
    }
    
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
            ec.generateAndInsertPhaseChangeEvent(c, initialCasePhase); 

        } 
        
        refreshCase(c);
        
    }
    
    
    /**
     * A catch-the-rest method that simple adds the event to the case without
     * any additional logic or processing. Called by the default case in the
     * event delegator's switch method. Passes the duty of calling the integrator
     * to the insertEvent on the EventCoordinator
     * @param c the case to which the event should be attached
     * @param e the event to be attached
     * @throws IntegrationException thrown if the integrator cannot get the data
     * into the DB
     */
    private void processGeneralEvent(CECase c, EventCECase e) throws IntegrationException, CaseLifecyleException, ViolationException{
        EventCoordinator ec = getEventCoordinator();
        attachNewEvent(c, e);
        refreshCase(c);
        
    }
    
    /**
     * Utility method for determining which CasePhase follows any given case's CasePhase. 
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
    
    
    public ArrayList retrieveViolationList(CECase ceCase) throws IntegrationException{
        ArrayList<CodeViolation> al;
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        al = cvi.getCodeViolations(ceCase);
        return al;
    }
    
    public void resetNOVMailing(CECase cs, NoticeOfViolation nov) throws IntegrationException{
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        nov.setRequestToSend(false);
        nov.setLetterSentDate(null);
        nov.setLetterReturnedDate(null);
        cvi.updateViolationLetter(nov);
        
    }
    
    
    public void queueNoticeOfViolation(CECase c, NoticeOfViolation nov) 
            throws CaseLifecyleException, IntegrationException, EventException, ViolationException{
        
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        
        EventCoordinator evCoord = getEventCoordinator();
        
        // togglign this switch puts the notice in the queue for sending
        // flag violation letter as ready to send
        // this will also need to trigger a letter mailing process that hasn't been implemented as
        // of 2 March 2018
        if(nov.isRequestToSend() == false){
            nov.setRequestToSend(true);
        } else {
            throw new CaseLifecyleException("Notice is already queued for sending");
        }
        
        // new letters won't have a LocalDateTime object
        // so insert instead of update in this case
        if(nov.getInsertionTimeStamp() == null){
            cvi.insertNoticeOfViolation(c, nov);
            
        } else {
            cvi.updateViolationLetter(nov);
            
        }
        
        EventCECase noticeEvent = new EventCECase();
        EventCategory ec = new EventCategory();
        ec.setCategoryID(Integer.parseInt(getResourceBundle(
                Constants.EVENT_CATEGORY_BUNDLE).getString("noticeQueued")));
        noticeEvent.setCategory(ec);
        noticeEvent.setCaseID(c.getCaseID());
        noticeEvent.setDateOfRecord(LocalDateTime.now());
        
        String queuedNoticeEventNotes = getResourceBundle(Constants.MESSAGE_TEXT).getString("noticeQueuedEventDesc");
        noticeEvent.setDescription(queuedNoticeEventNotes);
        
        noticeEvent.setOwner(getFacesUser());
        noticeEvent.setActive(true);
        noticeEvent.setDiscloseToMunicipality(true);
        noticeEvent.setDiscloseToPublic(true);
        noticeEvent.setHidden(false);
        
        ArrayList<Person> al = new ArrayList();
        al.add(nov.getRecipient());
        noticeEvent.setEventPersons(al);
        
        attachNewEvent(c, noticeEvent);
        
        refreshCase(c);
        
    }
    
    public void refreshCase(CECase c) throws IntegrationException{
        System.out.println("CaseCoordinator.refreshCase");
        CaseIntegrator ci = getCaseIntegrator();
        
        getSessionBean().setcECase(ci.getCECase(c.getCaseID()));
        
    }
    
    public void markNoticeOfViolationAsSent(CECase ceCase, NoticeOfViolation nov) throws CaseLifecyleException, EventException, IntegrationException{
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        nov.setLetterSentDate(LocalDateTime.now());
        nov.setLetterSentDatePretty(getPrettyDate(LocalDateTime.now()));
        cvi.updateViolationLetter(nov);   
        //advanceToNextCasePhase(ceCase);
            
    }
    
    public void processReturnedNotice(CECase c, NoticeOfViolation nov) throws IntegrationException{
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        nov.setLetterReturnedDate(LocalDateTime.now());
        cvi.updateViolationLetter(nov);
        refreshCase(c);
    } 
    
    public void deleteNoticeOfViolation(NoticeOfViolation nov) throws CaseLifecyleException{
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();

        //cannot delete a letter that was already sent
        if(nov != null && nov.getLetterSentDate() != null){
            throw new CaseLifecyleException("Cannot delete a letter that has been sent");
        } else {
            try {
                cvi.deleteViolationLetter(nov);
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
       ArrayList<CodeViolation> al = new ArrayList<>();
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
}
