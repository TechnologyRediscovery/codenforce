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
 * GNU General Public License for mo re details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.entities.reports.ReportConfigCECaseList;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECase;
import com.tcvcog.tcvce.entities.reports.ReportCEARList;
import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.search.QueryCEAR;
import com.tcvcog.tcvce.entities.search.QueryCEAREnum;
import com.tcvcog.tcvce.entities.search.QueryEvent;
import com.tcvcog.tcvce.entities.search.QueryEventEnum;
import com.tcvcog.tcvce.integration.*;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class CaseCoordinator extends BackingBeanUtils implements Serializable{

    final CasePhaseEnum initialCECasePphase = CasePhaseEnum.PrelimInvestigationPending;
    
    /**
     * Creates a new instance of CaseCoordinator
     */
    public CaseCoordinator() {
    
    }
    
    @PostConstruct
    public void initBean(){
        
    }
    
    /**
     * Called at the very end of the CECaseDataHeavy creation process by the CaseIntegrator
     * and simply checks for events that have a required eventcategory attached
     * and places a copy of the event in the Case's member variable.
     * 
     * This means that every time we refresh the case, the list is automatically
     * updated.
     * DESIGN NOTE: A competing possible location for this method would be on the
     * CECaseDataHeavy object itself--in its getEventListActionRequest method
     * 
     * @param c
     * @param cred
     * @return the CECaseDataHeavy with the action request list ready to roll
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public CECaseDataHeavy assembleCECaseDataHeavy(CECase c, Credential cred) throws BObStatusException, IntegrationException{
        SearchCoordinator sc = getSearchCoordinator();
        ChoiceCoordinator chc = getChoiceCoordinator();
        EventCoordinator ec = getEventCoordinator();
        
        // Wrap our base class in the subclass wrapper--an odd design structure, indeed
        CECaseDataHeavy cse = new CECaseDataHeavy(c);

        try {
            // EVENT LIST
            QueryEvent qe = sc.initQuery(QueryEventEnum.CECASE, cred);
            qe.getPrimaryParams().setBobID_ctl(true);
            qe.getPrimaryParams().setBobID_val(c.getCaseID());
            cse.setCompleteEventList(sc.runQuery(qe).getBOBResultList());
        
            // PROPOSAL LIST
            cse.setProposalList(chc.getProposalList(cse, cred));
            
            // EVENT RULE LIST
            cse.setEventRuleList(ec.rules_getEventRuleImpList(cse, cred));
            
            // CEAR LIST
            QueryCEAR qcear = sc.initQuery(QueryCEAREnum.ATTACHED_TO_CECASE, cred);
            qcear.getPrimaryParams().setCecase_ctl(true);
            qcear.getPrimaryParams().setCecase_val(c);
            
            cse.setCeActionRequestList(sc.runQuery(qcear).getBOBResultList());
            
        } catch (SearchException ex) {
            System.out.println(ex);
        }
        
        
        //TODO NADGIT - integrate Fee functionality
//        cse.setFeeList(new ArrayList<MoneyCECaseFeeAssigned>());
//        cse.setPaymentList(new ArrayList<MoneyCECaseFeePayment>());
//        
        cse.setShowHiddenEvents(false);
        cse.setShowInactiveEvents(false);
        
        
        Collections.sort(cse.getVisibleEventList());
        Collections.reverse(cse.getVisibleEventList()); 
        
        // optionally sorted events based on action
        // requests
        
        return cse;
    }
    
    public List<CECaseDataHeavy> getCECaseHeavyList(List<CECase> cseList, Credential cred){
        List<CECaseDataHeavy> heavyList = new ArrayList<>();
        for(CECase cse: cseList){
            try {
                heavyList.add(assembleCECaseDataHeavy(cse, cred));
            } catch (BObStatusException | IntegrationException ex) {
                System.out.println(ex);
            } 
        }
        return heavyList;
        
    }
    
    /**
     * Primary pathway for retrieving the CECaseDataHeavy data-light 
     * superclass CECase. Implements business logic.
     * @param caseID
     * @return
     * @throws IntegrationException 
     */
    public CECase getCECase(int caseID) throws IntegrationException{
        CaseIntegrator ci = getCaseIntegrator();
        ViolationIntegrator cvi = getCodeViolationIntegrator();
        CitationIntegrator citInt = getCitationIntegrator();
        
        CECase cse = null;
        try {
            cse = ci.getCECase(caseID);
            
            cse.setNoticeList(cvi.novGetList(cse));
            Collections.sort(cse.getNoticeList());
            Collections.reverse(cse.getNoticeList());
            
            cse.setCitationList(citInt.getCitations(cse));
            
            cse.setViolationList(cvi.getCodeViolations(cse.getCaseID()));
            Collections.sort(cse.getViolationList());
            
            cse = configureCECaseStageAndPhase(cse);
        } catch (BObStatusException ex) {
            System.out.println(ex);
        }
        
        return cse;
        
    }
    
    public Icon getIconByCasePhase(CasePhaseEnum phase) throws IntegrationException{
        SystemIntegrator si = getSystemIntegrator();
        return si.getIcon(phase);
        
    }
    
   
     /**
     * A CECaseDataHeavy's Stage is derived from its Phase based on the set of business
     * rules encoded in this method.
     * @param cse which needs its StageConfigured
     * @return the same CECas passed in with the CaseStageEnum configured
     * @throws BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
     private CECase configureCECaseStageAndPhase(CECase cse) throws BObStatusException, IntegrationException {
        
         if(cse.getCaseID() == 0){
             throw new BObStatusException("cannot configure case with ID 0");
             
         }
         
        // First determine case stage, then dig around as needed in the case
        // data to determine the appropriate phase
        
        // case stage basically emerges from violation status assessment
         
         
        SystemIntegrator si = getSystemIntegrator();
        CaseStageEnum stage;
        
        int maxVStage;
        
        if(determineIfCaseIsOpen(cse)){
            maxVStage = determineMaxViolationStatus(cse.getViolationList());

            if(cse.getViolationList().isEmpty()){
                // Open case, no violations yet: only one mapping
                cse.setCasePhase(CasePhaseEnum.PrelimInvestigationPending);
                
              // we have at least one violation attached  
            } else { 
                
                // If we don't have a mailed notice, then we're in Notice Delivery phase
                if(!determineIfNoticeHasBeenMailed(cse)){
                    cse.setCasePhase(CasePhaseEnum.NoticeDelivery);
                  
                // notice has been sent so we're in CaseStageEnum.Enforcement or beyond
                } else {
                    switch(maxVStage){
                        case 0:  // all violations resolved
                            cse.setCasePhase(CasePhaseEnum.Closed);
                            break;
                        case 1: // all violations within compliance window
                            cse.setCasePhase(CasePhaseEnum.InitialComplianceTimeframe);
                            break;
                        case 2: // one or more EXPIRED compliance timeframes
                            cse.setCasePhase(CasePhaseEnum.SecondaryPostHearingComplianceTimeframe);
                            break;
                        case 3: // at least 1 violation used in a citation that's attached to case
                            determineAndSetPhase_stageCITATION(cse);
                            break;
                        default: // unintentional dumping ground 
                            cse.setCasePhase(CasePhaseEnum.InactiveHolding);
                    }
                    
                }
                
            }
        } else { // we have a closed case
            
            cse.setCasePhase(CasePhaseEnum.Closed);
        }
        
        if(cse.getCasePhase() != null && cse.getCasePhase().getCaseStage() != null){
            //now set the icon based on what phase we just assigned the case to
            cse.setCasePhaseIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                    .getString(cse.getCasePhase().getCaseStage().getIconPropertyLookup()))));
        }
        return cse;
    }
     
     /**
      * TODO: Finish logic
      * @param cse
      * @return 
      */
    public CECase determineAndSetPhase_stageCITATION(CECase cse){
//        Iterator<EventCnF> iter = cse.getActiveEventList().iterator();
//        cse.setCasePhase(CasePhaseEnum.HearingPreparation);
//        while(iter.hasNext()){
//            EventCnF ev = iter.next();
//            if(ev.getCategory().getEventType() == EventType.Citation){
//                // FINISH
//            }
//        }
         return cse;
    } 
     
    public boolean determineIfNoticeHasBeenMailed(CECase cse){
        return true;
    } 
    
    public boolean determineIfCaseIsOpen(CECase cse){
         boolean isOpen = false;
         if(cse.getClosingDate() == null){
             isOpen = true;
         }
         return isOpen;
     }
     
     /**
      * Violation status enum values have associated ordinal values
      * which this method looks at to determine the highest one, which
      * it returns and then goes on break
      * @param vList
      * @return 
      */
     public int determineMaxViolationStatus(List<CodeViolation> vList){
         int maxStatus = -1;
         if(vList != null){
             for(CodeViolation cv: vList){
                 if(cv.getStatus().getOrder() > maxStatus){
                     maxStatus = cv.getStatus().getOrder();
                 }
             }
         }
         return maxStatus;
     }
    
    public ReportCEARList getInitializedReportConficCEARs(User u, Municipality m){
        ReportCEARList rpt = new ReportCEARList();
        rpt.setIncludePhotos(true);
        rpt.setPrintFullCEARQueue(false);
        rpt.setCreator(u);
        rpt.setMuni(m);
        rpt.setGenerationTimestamp(LocalDateTime.now());
        return rpt;
    }
    
    /**
     * Old logic but good method sig. This method should get the PropertyInfoCase
     * associated with the Muni's property
     * @param cred
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public CECase selectDefaultCECase(Credential cred) throws IntegrationException, BObStatusException{
        CaseIntegrator ci = getCaseIntegrator();
        return ci.getCECase( Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("arbitraryPlaceholderCaseID")));
    }
    
  
    
    public List<CECase> assembleCaseHistory(Credential cred) throws IntegrationException, BObStatusException{
        CaseIntegrator caseInt = getCaseIntegrator();
        List<CECase> cl = new ArrayList<>();
        List<Integer> cseidl = null;
        if(cred != null){
            cseidl = caseInt.getCECaseHistoryList(cred.getGoverningAuthPeriod().getUserID());
             if(!cseidl.isEmpty()){
                for(Integer i: cseidl){
                    cl.add(getCECase(i));
                }
            }
        }
        return cl;
    }
    
    public CECaseDataHeavy initCECase(Property p, User u){
        CECaseDataHeavy newCase = new CECaseDataHeavy();
        
        int casePCC = generateControlCodeFromTime();
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
     * @throws BObStatusException
     * @throws ViolationException 
     */
    public void insertNewCECase(CECase newCase, Credential cred, CEActionRequest cear) throws IntegrationException, BObStatusException, ViolationException, EventException{
        
        CaseIntegrator ci = getCaseIntegrator();
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        EventCoordinator ec = getEventCoordinator();
        EventCategory originationCategory;
        EventCnF originationEvent;
        UserCoordinator uc = getUserCoordinator();
        User us = uc.getUser(cred.getGoverningAuthPeriod().getUserID());
        
        
        
        // the integrator returns to us a CECaseDataHeavy with the correct ID after it has
        // been written into the DB
        int freshID = ci.insertNewCECase(newCase);
        CECaseDataHeavy cedh = assembleCECaseDataHeavy(newCase, cred);
        

        // If we were passed in an action request, connect it to the new case we just made
        if(cear != null){
            ceari.connectActionRequestToCECase(cear.getRequestID(), freshID,us.getUserID());
            originationCategory = ec.initEventCategory(
                    Integer.parseInt(getResourceBundle(
                    Constants.EVENT_CATEGORY_BUNDLE).getString("originiationByActionRequest")));
            originationEvent = ec.initEvent(cedh, originationCategory);
            StringBuilder sb = new StringBuilder();
            sb.append("Case generated from the submission of a Code Enforcement Action Request");
            sb.append("<br />");
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
            originationCategory = ec.initEventCategory(
            Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("originiationByObservation")));
            originationEvent = ec.initEvent(cedh, originationCategory);
            StringBuilder sb = new StringBuilder();
            sb.append("Case opened directly on property by code officer assigned to this event");
            originationEvent.setNotes(sb.toString());
            
        }
            originationEvent.setOwner(us);
            attachNewEventToCECase(assembleCECaseDataHeavy(getCECase(freshID), cred), originationEvent, null);
    }
    
    /**
     * Called by the PIBCECaseBB when a public user wishes to add an event
     * to the case they are viewing online. This method stitches together the
     * message text, messenger name, and messenger phone number before
     * passing the info back to the EventCoordinator
     * @param caseID can be extracted from the public info bundle
     * @param msg the text of the message the user wants to add to the case 
     * @param messagerName the first and last name of the person submitting the message
 Note that this submission info is not YET wired into the actual Person objects
 in the system.
     * @param messagerPhone a simple String rendering of whatever the user types in. Length validation only.
     */
    public void attachPublicMessage(int caseID, String msg, String messagerName, String messagerPhone) throws IntegrationException, BObStatusException, EventException{
        StringBuilder sb = new StringBuilder();
        sb.append("Case note added by ");
        sb.append(messagerName);
        sb.append(" with contact number: ");
        sb.append(messagerPhone);
        sb.append(": ");
        sb.append("<br /><br />");
        sb.append(msg);
        
        EventCoordinator ec = getEventCoordinator();
        ec.attachPublicMessagToCECase(caseID, sb.toString());
        
        
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
     * @param cse the case to which the event should be added
     * @param ev the event to add to the case also included in this call
     * @param viol the CodeViolation object associated with this event, can be null
     * @return 
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.ViolationException
     */
    public int attachNewEventToCECase(CECaseDataHeavy cse, EventCnF ev, CodeViolation viol) 
            throws BObStatusException, IntegrationException, ViolationException, EventException{
        EventType eventType = ev.getCategory().getEventType();
        EventIntegrator ei = getEventIntegrator();
        int insertedEventID = 0;
        
//        if(e.getCategory().getCasePhaseChangeRule() != null){
//            evalulateCasePhaseChangeRule(c, e);
//        }
        
        switch(eventType){
            case Action:
                System.out.println("CaseCoordinator.attachNewEventToCECase: action case");
                insertedEventID = ei.insertEvent(ev);
                break;
            case Compliance:
                if(viol != null){
                    System.out.println("CaseCoordinator.attachNewEventToCECase: compliance inside if");
                    viol.setActualComplianceDate(ev.getTimeStart());
                    insertedEventID = ei.insertEvent(ev);
                    checkForFullComplianceAndCloseCaseIfTriggered(cse);
                } else {
                    throw new BObStatusException("no violation was included with this compliance event");
                }
                break;
            case Closing:
                System.out.println("CaseCoordinator.attachNewEventToCECase: closing case");
                insertedEventID = processClosingEvent(cse, ev);
                break;
            default:
                System.out.println("CaseCoordinator.attachNewEventToCECase: default case");
                ev.setCeCaseID(cse.getCaseID());
                insertedEventID = ei.insertEvent(ev);
        } // close switch
        return insertedEventID;
    } // close method
    
    
    
    public void evaluateProposal(   Proposal proposal, 
                                    IFace_Proposable chosen, 
                                    CECaseDataHeavy ceCase, 
                                    UserAuthorized u) throws EventException, AuthorizationException, BObStatusException, IntegrationException, ViolationException{
        ChoiceCoordinator cc = getChoiceCoordinator();
        EventCoordinator ec = getEventCoordinator();
        EventIntegrator ei = getEventIntegrator();
        
        EventCnF propEvent = null;
        int insertedEventID = 0;
        if(cc.determineProposalEvaluatability(proposal, chosen, u)){
            // since we can evaluate this proposal with the chosen Proposable, configure members
            proposal.setResponderActual(u);
            proposal.setResponseTS(LocalDateTime.now());
            proposal.setChosenChoice(chosen);
            
            // ask the EventCoord for a nicely formed EventCnF, which we cast to EventCnF
            EventCnF csEv = ec.generateEventDocumentingProposalEvaluation(proposal, chosen, u);
            // insert the event and grab the new ID
            insertedEventID = attachNewEventToCECase(ceCase, csEv, null);
            // go get our new event by ID and inject it into our proposal before writing its evaluation to DB
            proposal.setResponseEvent(ec.getEvent(insertedEventID));
            cc.recordProposalEvaluation(proposal);
        } else {
            throw new BObStatusException("Unable to evaluate proposal due to business rule violation");
        }
    }
    
    
    protected void processCaseOnEventRulePass(CECaseDataHeavy cse, EventRuleAbstract rule) 
            throws IntegrationException, BObStatusException, ViolationException{
        CaseIntegrator ci = getCaseIntegrator();
        EventCoordinator ec = getEventCoordinator();
        EventCnF newEvent = null;
        
        CasePhaseEnum oldCP = cse.getCasePhase();
        ci.changeCECasePhase(cse);
//        ec.generateAndInsertPhaseChangeEvent(cse, oldCP, rule);
//        if(rule.getTriggeredEventCategoryID() != 0){
//            newEvent = ec.initEvent(cse, ec.initEventCategory(rule.getTriggeredEventCategoryID()));
//            
//            attachNewEventToCECase(cse, newEvent, null);
//            System.out.println("CaseCoordinator.processCaseOnEventRulePass "  + newEvent.getCategory().getEventCategoryTitle());
//        }
//        
    }
   
   
    
     /**
     * Implements business rules for determining which event types are allowed
 to be attached to the given CECaseDataHeavy based on the case's phase and the
 user's permissions in the system.
     * 
     * Used for displaying the appropriate event types to the user on the
     * cecases.xhtml page
     * 
     * @param c the CECaseDataHeavy on which the event would be attached
     * @param u the User doing the attaching
     * @return allowed EventTypes for attaching to the given case
     */
    public List<EventType> getPermittedEventTypesForCECase(CECaseDataHeavy c, User u){
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
     * @throws BObStatusException
     * @throws ViolationException 
     */
    private void checkForFullComplianceAndCloseCaseIfTriggered(CECaseDataHeavy c) 
            throws IntegrationException, BObStatusException, ViolationException, EventException{
        
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
        
        EventCnF complianceClosingEvent;
        if (complianceWithAllViolations){
            complianceClosingEvent = ec.initEvent(c, ei.getEventCategory(Integer.parseInt(getResourceBundle(
                Constants.EVENT_CATEGORY_BUNDLE).getString("closingAfterFullCompliance"))));
            attachNewEventToCECase(c, complianceClosingEvent, null);
            
        } // close if
        
    }
     
    private int processClosingEvent(CECaseDataHeavy c, EventCnF e) throws IntegrationException, BObStatusException{
        CaseIntegrator ci = getCaseIntegrator();
        EventIntegrator ei = getEventIntegrator();
        
        CasePhaseEnum closedPhase = CasePhaseEnum.Closed;
        c.setCasePhase(closedPhase);
        ci.changeCECasePhase(c);
        
        c.setClosingDate(LocalDateTime.now());
        updateCoreCECaseData(c);
        // now load up the closing event before inserting it
        // we'll probably want to get this text from a resource file instead of
        // hardcoding it down here in the Java
        e.setOwner(getSessionBean().getSessUser());
        e.setDescription(getResourceBundle(Constants.MESSAGE_TEXT).getString("automaticClosingEventDescription"));
        e.setNotes(getResourceBundle(Constants.MESSAGE_TEXT).getString("automaticClosingEventNotes"));
        return ei.insertEvent(e);
    }
    
 
    
    
    /**
     * Sets mailing fields to null]
     * Params changed to take in UserAuthorized during corruption recovery
     * @param nov
     * @param user
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.AuthorizationException
     */
    public void novResetMailing(NoticeOfViolation nov, UserAuthorized user) throws IntegrationException, AuthorizationException{
        ViolationIntegrator cvi = getCodeViolationIntegrator();
        if(user.getMyCredential().isHasSysAdminPermissions()){
            cvi.novResetMailingFieldsToNull(nov);
        } else {
            throw new AuthorizationException("User does not have sufficient acces righst to clear notice mailing fields");
        }
    }
    
        
    public NoticeOfViolation novGetNewNOVSkeleton(CECaseDataHeavy cse, MunicipalityDataHeavy mdh) throws SQLException, AuthorizationException{
        SystemIntegrator si = getSystemIntegrator();
        NoticeOfViolation nov = new NoticeOfViolation();
        nov.setViolationList(new ArrayList<CodeViolationDisplayable>());
        nov.setDateOfRecord(LocalDateTime.now());
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        
        try {
            nov.setStyle(si.getPrintStyle(mdh.getDefaultNOVStyleID()));
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
    
    
    public void novLockAndQueue(CECaseDataHeavy c, NoticeOfViolation nov, User user) 
            throws BObStatusException, IntegrationException, EventException, ViolationException{
        
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
            throw new BObStatusException("Notice is already locked and queued for sending");
        }
        
        EventCnF noticeEvent = evCoord.initEvent(c, evCoord.initEventCategory(Integer.parseInt(getResourceBundle(
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
    
    public int novInsertNotice(NoticeOfViolation nov, CECaseDataHeavy cse, User usr) throws IntegrationException{
        ViolationIntegrator vi = getCodeViolationIntegrator();
        System.out.println("CaseCoordinator.novInsertNotice");
        return vi.novInsert(cse, nov);
    }
    
    public void novUpdate(NoticeOfViolation nov) throws IntegrationException{
        ViolationIntegrator vi = getCodeViolationIntegrator();
        vi.novUpdateNotice(nov);
    }
    
    public void novMarkAsSent(CECaseDataHeavy ceCase, NoticeOfViolation nov, User user) throws BObStatusException, EventException, IntegrationException{
        ViolationIntegrator cvi = getCodeViolationIntegrator();
        nov.setSentTS(LocalDateTime.now());
        nov.setSentBy(user);
        cvi.novUpdateNotice(nov);   
    }
    
    public void novMarkAsReturned(CECaseDataHeavy c, NoticeOfViolation nov, User user) throws IntegrationException, BObStatusException{
        ViolationIntegrator cvi = getCodeViolationIntegrator();
        nov.setReturnedTS(LocalDateTime.now());
        nov.setReturnedBy(user);
        cvi.novUpdateNotice(nov);
    } 
    
    public void novDelete(NoticeOfViolation nov) throws BObStatusException{
        ViolationIntegrator cvi = getCodeViolationIntegrator();

        //cannot delete a letter that was already sent
        if(nov != null && nov.getSentTS() != null){
            throw new BObStatusException("Cannot delete a letter that has been sent");
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
    * Implements business logic before updating a CECaseDataHeavy's core data (opening date,
 closing date, etc.). If all is well, pass to integrator.
    * @param c the CECaseDataHeavy to be updated
    * @throws BObStatusException
    * @throws IntegrationException 
    */
   public void updateCoreCECaseData(CECaseDataHeavy c) throws BObStatusException, IntegrationException{
       CaseIntegrator ci = getCaseIntegrator();
       if(c.getClosingDate() != null){
            if(c.getClosingDate().isBefore(c.getOriginationDate())){
                throw new BObStatusException("You cannot update a case's origination date to be after its closing date");
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
   
   
//    --------------------------------------------------------------------------
//    ********************* CE Action Requests *********************************
//    --------------------------------------------------------------------------
    
   
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
    * Business logic intermediary method for CEActionRequests. Calls the CEAction
    * Integrator 
    * @param cearid
    * @return
    * @throws IntegrationException 
    */
   public CEActionRequest getCEActionRequest(int cearid) throws IntegrationException{
       CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
       return ceari.getActionRequestByRequestID(cearid);
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
                                                        UserAuthorized u ){
       if(req != null && u.getMyCredential() != null){
            if((
                    req.getRequestStatus().getStatusID() == 
                    Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                            .getString("actionRequestInitialStatusCode")))
                    && 
                    u.getMyCredential().isHasEnfOfficialPermissions()
                ){
                return true;
            }
        }
       return false;
   }
   
   public ReportConfigCECase getDefaultReportConfigCECase(CECaseDataHeavy c){
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
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
    */
   public ReportConfigCECase transformCECaseForReport(ReportConfigCECase rptCse) throws IntegrationException, BObStatusException{
       CaseIntegrator ci = getCaseIntegrator();
       // we actually get an entirely new object instead of editing the 
       // one we used throughout the ceCases.xhtml
       CECaseDataHeavy c = rptCse.getCse();
       
       List<EventCnF> evList =  new ArrayList<>();
       Iterator<EventCnF> iter = c.getVisibleEventList().iterator();
       while(iter.hasNext()){
            EventCnF ev = iter.next();
            
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
       rptCse.setEventListForReport(evList);
       
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
       rptCse.setNoticeListForReport(noticeList);
       return rptCse;
   }
   
 
   
    
    public CodeViolation generateNewCodeViolation(CECaseDataHeavy c, EnforcableCodeElement ece){
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
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public int attachViolationToCaseAndInsertTimeFrameEvent(CodeViolation cv, CECaseDataHeavy cse) throws IntegrationException, ViolationException, BObStatusException, EventException{
        
        ViolationIntegrator vi = getCodeViolationIntegrator();
        EventCoordinator ec = getEventCoordinator();
        UserCoordinator uc = getUserCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        EventCnF tfEvent;
        int insertedViolationID;
        int eventID;
        StringBuilder sb = new StringBuilder();
        
        EventCategory eventCat = ec.initEventCategory(
                                Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE)
                                .getString("complianceTimeframeExpiry")));
//        EventCategory eventCat = ec.initEventCategory(113);
        tfEvent = ec.initEvent(cse, eventCat);
        tfEvent.setTimeStart(cv.getStipulatedComplianceDate());
        tfEvent.setOwner(cse.getCaseManager());
        
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
     * 
     * @param cv
     * @return the CodeViolation with correct icon and status
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public CodeViolation configureCodeViolation(CodeViolation cv) throws IntegrationException{
        SystemIntegrator si = getSystemIntegrator();
        if(cv.getActualComplianceDate() == null){
            // violation still within compliance timeframe
            if(cv.getDaysUntilStipulatedComplianceDate() >= 0){
                
                cv.setStatus(ViolationStatusEnum.UNRESOLVED_WITHINCOMPTIMEFRAME);
                cv.setIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString(cv.getStatus().getIconPropertyName()))));
                
            // violation has NOT been cited, but is past compliance timeframe end date
            } else if(cv.getCitationIDList().isEmpty()) {
                
                cv.setStatus(ViolationStatusEnum.UNRESOLVED_EXPIREDCOMPLIANCETIMEFRAME);
                cv.setIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString(cv.getStatus().getIconPropertyName()))));
                
            // violation has been cited on at least one citation
            } else {
                cv.setStatus(ViolationStatusEnum.UNRESOLVED_CITED);
                cv.setIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString(cv.getStatus().getIconPropertyName()))));
            }
            // we have a resolved violation
        } else {
                cv.setStatus(ViolationStatusEnum.RESOLVED);
                cv.setIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString(cv.getStatus().getIconPropertyName()))));
        }
        return cv;
    }
    
    
    private boolean verifyCodeViolationAttributes(CECaseDataHeavy cse, CodeViolation cv) throws ViolationException{
        if(cse.getCasePhase() == CasePhaseEnum.Closed){
            throw new ViolationException("Cannot update code violations on closed cases!");
            
        }
        if(cv.getStipulatedComplianceDate().isBefore(cv.getDateOfRecord())){
            throw new ViolationException("Stipulated compliance date cannot be before the violation's date of record");
        }
        
        return true;
    }
    
    public void updateCodeViolation(CECaseDataHeavy cse, CodeViolation cv, UserAuthorized u) throws ViolationException, IntegrationException, EventException{
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
                EventCnF tfEvent = ei.getEvent(violTimeframeEventID);
                tfEvent.setTimeStart(cv.getStipulatedComplianceDate());
                ec.editEvent(tfEvent);
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
    public void recordCompliance(CodeViolation cv, UserAuthorized u) throws IntegrationException{
        
        ViolationIntegrator cvi = getCodeViolationIntegrator();
        EventCoordinator ec = getEventCoordinator();
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
            ec.deactivateEvent(ec.getEvent(violTimeframeEventID), u);
        }
    }

    
    public void deleteViolation(CodeViolation cv) throws IntegrationException{
        ViolationIntegrator cvi = getCodeViolationIntegrator();
        cvi.deleteCodeViolation(cv);
    }
    
    public List getCodeViolations(CECaseDataHeavy ceCase) throws IntegrationException{
        ViolationIntegrator cvi = getCodeViolationIntegrator();
        List al = cvi.getCodeViolations(ceCase);
        return al;
    }
    
 
   
} // close class
