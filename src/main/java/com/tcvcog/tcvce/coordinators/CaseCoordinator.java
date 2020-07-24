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
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.occupancy.integration.PaymentIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
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
public class CaseCoordinator extends BackingBeanUtils implements Serializable {

    final CasePhaseEnum initialCECasePphase = CasePhaseEnum.PrelimInvestigationPending;

    /**
     * Creates a new instance of CaseCoordinator
     */
    public CaseCoordinator() {

    }

    @PostConstruct
    public void initBean() {

    }
    
    
    
    // *************************************************************************
    // *                     CODE ENF CASE GENERAL                             *
    // *************************************************************************
    
    
    /**
     * Called at the very end of the CECaseDataHeavy creation process by the
     * CaseIntegrator and simply checks for events that have a required
     * eventcategory attached and places a copy of the event in the Case's
     * member variable.
     *
     * This means that every time we refresh the case, the list is automatically
     * updated. DESIGN NOTE: A competing possible location for this method would
     * be on the CECaseDataHeavy object itself--in its getEventListActionRequest
     * method
     *
     * @param c
     * @param ua
     * @return the CECaseDataHeavy with the action request list ready to roll
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public CECaseDataHeavy assembleCECaseDataHeavy(CECase c, UserAuthorized ua)
            throws BObStatusException, IntegrationException, SearchException {
        
        Credential cred = null;
        if(ua != null){
            cred = ua.getKeyCard();
            
        }
        SearchCoordinator sc = getSearchCoordinator();
        WorkflowCoordinator wc = getWorkflowCoordinator();
        EventCoordinator ec = getEventCoordinator();
        PaymentIntegrator pi = getPaymentIntegrator();

        // Wrap our base class in the subclass wrapper--an odd design structure, indeed
        CECaseDataHeavy cse = new CECaseDataHeavy(c);

        try {
            
            

            // PROPOSAL LIST
            cse.setProposalList(wc.getProposalList(cse, cred));

            // EVENT RULE LIST
            cse.setEventRuleList(wc.rules_getEventRuleImpList(cse, cred));

            // CEAR LIST
            QueryCEAR qcear = sc.initQuery(QueryCEAREnum.ATTACHED_TO_CECASE, cred);
            qcear.getPrimaryParams().setCecase_ctl(true);
            qcear.getPrimaryParams().setCecase_val(c);

            cse.setCeActionRequestList(sc.runQuery(qcear).getBOBResultList());

        } catch (SearchException ex) {
            System.out.println(ex);
        }

        try {
            cse.setFeeList(pi.getFeeAssigned(c));

            cse.setPaymentListGeneral(pi.getPaymentList(c));
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return cse;
    }
    
    /**
     * Utility method for calling assembleCECaseDataHeavy() for each base CECase
     * in the given list
     * @param cseList
     * @param ua
     * @return 
     */
    public List<CECaseDataHeavy> assembleCECaseDataHeavyList(List<CECase> cseList, UserAuthorized ua){
        List<CECaseDataHeavy> cseDHList = new ArrayList<>();
        if(cseList != null && !cseList.isEmpty()){
            for(CECase cse: cseList){
                try {
                    cseDHList.add(assembleCECaseDataHeavy(cse, ua));
                } catch (BObStatusException | IntegrationException | SearchException ex) {
                    System.out.println("CaseCoordinator.assembleCECaseDataHeavy" + ex.toString());
                }
                
            }
        }
        return cseDHList;
        
    }

    /**
     * Utility for downcasting a list of CECasePropertyUnitDataHeavy to the base
     * class
     *
     * @param cspudhList
     * @return
     */
    public List<CECase> downcastCECasePropertyUnitHeavyList(List<CECasePropertyUnitHeavy> cspudhList) {
        List<CECase> cslist = new ArrayList<>();
        if (cspudhList != null && !cspudhList.isEmpty()) {
            for (CECasePropertyUnitHeavy c : cspudhList) {
                cslist.add((CECase) c);
            }

        }
        return cslist;
    }

    /**
     * Utility for assembling a list of data heavy cases from a list of base
     * class instances
     *
     * @param cseList
     * @param ua
     * @return
     */
    public List<CECaseDataHeavy> getCECaseDataHeavyList(List<CECase> cseList, UserAuthorized ua) {
        List<CECaseDataHeavy> heavyList = new ArrayList<>();
        for (CECase cse : cseList) {
            try {
                heavyList.add(assembleCECaseDataHeavy(cse, ua));
            } catch (BObStatusException | IntegrationException | SearchException ex) {
                System.out.println(ex);
            }
        }
        return heavyList;

    }

    /**
     * Primary pathway for retrieving the data-light superclass CECase.
     * Implements business logic.
     *
     * @param caseID
     * @return
     * @throws IntegrationException
     */
    public CECase getCECase(int caseID) throws IntegrationException {
        CaseIntegrator ci = getCaseIntegrator();
        CECase cse = null;
        try {
            cse = ci.getCECase(caseID);
            if (cse != null) {

                cse.setNoticeList(ci.novGetList(cse));
                Collections.sort(cse.getNoticeList());
                Collections.reverse(cse.getNoticeList());

                cse.setCitationList(ci.getCitations(cse));

                cse.setViolationList(ci.getCodeViolations(cse.getCaseID()));
                Collections.sort(cse.getViolationList());

                cse = configureCECaseStageAndPhase(cse);
            }
        } catch (BObStatusException ex) {
            System.out.println(ex);
        }

        return cse;

    }

    /**
     * Builds a property and propertyUnit heavy subclass of our CECase object
     *
     * @param cse
     * @param ua
     * @return the data-rich subclass with Property and possible PropertyUnit
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public CECasePropertyUnitHeavy assembleCECasePropertyUnitHeavy(CECase cse) throws IntegrationException, SearchException {
        PropertyCoordinator pc = getPropertyCoordinator();
        SearchCoordinator sc = getSearchCoordinator();
        EventCoordinator ec = getEventCoordinator();
        CECasePropertyUnitHeavy csepuh = null;
        if (cse != null) {
            csepuh = new CECasePropertyUnitHeavy(cse);
            csepuh.setProperty(pc.getProperty(cse.getPropertyID()));
            if (cse.getPropertyUnitID() != 0) {
                csepuh.setPropUnit(pc.getPropertyUnit(cse.getPropertyUnitID()));
            }

          
        }
        return csepuh;
    }
    
    /**
     * Utility method for converting a List of simple CECase objects into the
     * a case that contains information about its Property and any associated Units
     * @param cseList
     * @param ua
     * @return
     */
    public List<CECasePropertyUnitHeavy> assembleCECasePropertyUnitHeavyList(List<CECase> cseList) {
        
        List<CECasePropertyUnitHeavy> cspudhList = new ArrayList<>();
        
        if (cseList != null && !cseList.isEmpty()) {
            if(getSessionBean().getSessUser() !=null){
            
            for (CECase cse : cseList) {
                try {
                    cspudhList.add(assembleCECasePropertyUnitHeavy(cse));
                } catch (IntegrationException | SearchException ex){
                    System.out.println(ex);
                    
                }
            }
            } else{
                //This session must be public
                UserCoordinator uc = getUserCoordinator();
                for (CECase cse : cseList) {
                cspudhList.add(assembleCECasePropertyUnitHeavy(cse, uc.getPublicUserAuthorized().getMyCredential()));
            }
            }
            
            
        }
        
        
        return cspudhList;
    }
    
    /**
     * Asks the Integrator for an icon based on case phase
     * @param phase
     * @return
     * @throws IntegrationException 
     */
    public Icon getIconByCasePhase(CasePhaseEnum phase) throws IntegrationException{
        CaseIntegrator ci = getCaseIntegrator();
        return ci.getIcon(phase);
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

        if (cse.getCaseID() == 0) {
            throw new BObStatusException("cannot configure case with ID 0");

        }

        // First determine case stage, then dig around as needed in the case
        // data to determine the appropriate phase
        // case stage basically emerges from violation status assessment
        SystemIntegrator si = getSystemIntegrator();
        CaseStageEnum stage;

        int maxVStage;
        
        if(cse.isOpen()){
            maxVStage = determineMaxViolationStatus(cse.getViolationList());

            if (cse.getViolationList().isEmpty()) {
                // Open case, no violations yet: only one mapping
                cse.setCasePhase(CasePhaseEnum.PrelimInvestigationPending);

                // we have at least one violation attached  
            } else {

                // If we don't have a mailed notice, then we're in Notice Delivery phase
                if (!determineIfNoticeHasBeenMailed(cse)) {
                    cse.setCasePhase(CasePhaseEnum.NoticeDelivery);

                    // notice has been sent so we're in CaseStageEnum.Enforcement or beyond
                } else {
                    switch (maxVStage) {
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

        if (cse.getCasePhase() != null && cse.getCasePhase().getCaseStage() != null) {
            //now set the icon based on what phase we just assigned the case to
            cse.setCasePhaseIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                    .getString(cse.getCasePhase().getCaseStage().getIconPropertyLookup()))));
        }
        return cse;
    }

    /**
     * TODO: Finish logic
     *
     * @param cse
     * @return
     */
    public CECase determineAndSetPhase_stageCITATION(CECase cse) {

        
         return cse;
    } 
     
    /**
     * TODO: Finish my guts
     * @param cse
     * @return 
     */
    public boolean determineIfNoticeHasBeenMailed(CECase cse){
        return true;
    } 
    
   
       /**
     * TODO: Old logic but good method sig. This method should get the PropertyInfoCase
     * associated with the Muni's property
     * 
     * @param ua
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     */
    public CECase selectDefaultCECase(UserAuthorized ua) throws IntegrationException, BObStatusException {
            PropertyCoordinator pc = getPropertyCoordinator();
            MunicipalityCoordinator mc = getMuniCoordinator();
            CECase cse = null;
        try {
            MunicipalityDataHeavy mdh = mc.assembleMuniDataHeavy(ua.getKeyCard().getGoverningAuthPeriod().getMuni(), ua);
            PropertyDataHeavy pdh = mdh.getMuniPropertyDH();
            if(pdh.getPropInfoCaseList() != null && !pdh.getPropInfoCaseList().isEmpty()){
                cse = pdh.getPropInfoCaseList().get(0);
            }
            
        } catch (AuthorizationException | BObStatusException | EventException | IntegrationException ex) {
            System.out.println("CaseCoordinator: Exception selecting default CECase from MuniPropDH; using arbitrary case");
        }
        if(cse != null){
            return cse;
        }    
        return getCECase(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("arbitraryPlaceholderCaseID")));
    }
    
  
    /**
     * Asks the DB for a case history list and converts the list of IDs into
     * actual CECase objects
     * 
     * @param ua
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public List<CECase> getCECaseHistory(UserAuthorized ua) throws IntegrationException, BObStatusException{
        CaseIntegrator caseInt = getCaseIntegrator();
        List<CECase> cl = new ArrayList<>();
        List<Integer> cseidl = null;
        if (ua != null) {
            cseidl = caseInt.getCECaseHistoryList(ua.getMyCredential().getGoverningAuthPeriod().getUserID());
            if (!cseidl.isEmpty()) {
                for (Integer i : cseidl) {
                    cl.add(getCECase(i));
                }
            }
        }
        return cl;
    }
    
    /**
     * Factory method for creating new CECases
     * 
     * @param p
     * @param ua
     * @return 
     */
    public CECase initCECase(Property p, UserAuthorized ua){
        UserCoordinator uc = getUserCoordinator();
        CECase newCase = new CECase();
        

        int casePCC = generateControlCodeFromTime();
        // caseID set by postgres sequence
        // timestamp set by postgres
        // no closing date, by design of case flow
        newCase.setPublicControlCode(casePCC);
        newCase.setPropertyID(p.getPropertyID());
        newCase.setCaseManager(ua);

        return newCase;
    }

    /**
     * Primary entry point for inserting new code enf cases. Two major pathways
     * exist through this method: - creating cases as a result of an action
     * request submission - creating cases from some other source than an action
     * request Depending on the source, an appropriately note-ified case
     * origination event is built and attached to the case that was just
     * created.
     *
     * @param newCase
     * @param ua
     * @param cear
     * @throws IntegrationException
     * @throws BObStatusException
     * @throws ViolationException
     * @throws com.tcvcog.tcvce.domain.EventException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public void insertNewCECase(CECase newCase, UserAuthorized ua, CEActionRequest cear) throws IntegrationException, BObStatusException, ViolationException, EventException, SearchException{
        
        CaseIntegrator ci = getCaseIntegrator();
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        EventCoordinator ec = getEventCoordinator();
        EventCategory originationCategory;
        EventCnF originationEvent;
        UserCoordinator uc = getUserCoordinator();
        
        
        
        // the integrator returns to us a CECaseDataHeavy with the correct ID after it has
        // been written into the DB
        int freshID = ci.insertNewCECase(newCase);
        CECaseDataHeavy cedh = assembleCECaseDataHeavy(newCase, ua);
        

        // If we were passed in an action request, connect it to the new case we just made
        if(cear != null){
            ceari.connectActionRequestToCECase(cear.getRequestID(), freshID, ua.getUserID());
            originationCategory = ec.initEventCategory(
                    Integer.parseInt(getResourceBundle(
                            Constants.EVENT_CATEGORY_BUNDLE).getString("originiationByActionRequest")));
            originationEvent = ec.initEvent(cedh, originationCategory);
            StringBuilder sb = new StringBuilder();
            originationEvent.setNotes(sb.toString());
        } else {
            // since there's no action request, the assumed method is called "observation"
            originationCategory = ec.initEventCategory(
                    Integer.parseInt(getResourceBundle(
                            Constants.EVENT_CATEGORY_BUNDLE).getString("originiationByObservation")));
            originationEvent = ec.initEvent(cedh, originationCategory);
            StringBuilder sb = new StringBuilder();
            sb.append("Case opened directly on property by code officer assigned to this event");
            originationEvent.setNotes(generateCaseInitNoteFromCEAR(cear));

        }
            originationEvent.setUserCreator(uc.getUser(ua.getUserID()));
            
            cedh.setCaseID(freshID);
            
            ec.addEvent(originationEvent, cedh, ua);
    }
    
    private String generateCaseInitNoteFromCEAR(CEActionRequest cear){
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
        return sb.toString();
    }
    
   /**
    * Implements business logic before updating a CECaseDataHeavy's core data (opening date,
    * closing date, etc.). If all is well, pass to integrator.
    * @param c the CECaseDataHeavy to be updated
    * @throws BObStatusException
    * @throws IntegrationException 
    */
   public void updateCECaseMetadata(CECaseDataHeavy c) throws BObStatusException, IntegrationException{
       CaseIntegrator ci = getCaseIntegrator();
       if(c.getClosingDate() != null){
            if(c.getClosingDate().isBefore(c.getOriginationDate())){
                throw new BObStatusException("You cannot update a case's origination date to be after its closing date");
            }
       }
       ci.updateCECaseMetadata(c);
   }
  
    
    // *************************************************************************
    // *                     REPORTING                                         *
    // *************************************************************************
    
    
   /**
    * Factory method for creating new reports listing action requests
    * @param u
    * @param m
    * @return 
    */
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
     * Factory method for CECase reports with sensible initial settings
     * @param c
     * @return 
     */
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
   
   /**
    * Factory method for reports on a list of cases
    * @return 
    */
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
    * ceCases.xhtml display. The logic inside me makes sure that 
    * hidden events don't make it out to the report, etc. So the ReportConfig
    * object is ready for display and printing
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
       Iterator<EventCnF> iter = c.getEventListMaster().iterator();
       while(iter.hasNext()){
            EventCnF ev = iter.next();
            
            // toss out hidden events unless the user wants them
            if(ev.isHidden() && !rptCse.isIncludeHiddenEvents()) continue;
            // toss out inactive events unless user wants them
            if(!ev.isActive()&& !rptCse.isIncludeInactiveEvents()) continue;
            // toss out events only available internally to the muni users unless user wants them
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
   
 
 
     
    
    // *************************************************************************
    // *                     PUBLIC FACING                                     *
    // *************************************************************************
    
    /**
     * Called by the PIBCECaseBB when a public user wishes to add an event to
     * the case they are viewing online. This method stitches together the
     * message text, messenger name, and messenger phone number before passing
     * the info back to the EventCoordinator
     *
     * @param caseID can be extracted from the public info bundle
     * @param msg the text of the message the user wants to add to the case 
     * @param messagerName the first and last name of the person submitting the message
     * Note that this submission info is not YET wired into the actual Person objects
     * in the system.
     * 
     * @param messagerPhone a simple String rendering of whatever the user types in. Length validation only.
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.EventException
     */
    public void attachPublicMessage(int caseID, String msg, String messagerName, String messagerPhone) throws IntegrationException, BObStatusException, EventException {
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
    
     
    
    // *************************************************************************
    // *                     EVENTS                                            *
    // *************************************************************************
    
        
    /**
     * For inter-coordinator use only! Not called by backing beans.
     * Primary event life cycle control method which is called by EventCoordinator
     * each time an event is added to a code enf case. The primary business
     * logic related to which events can be attached to a case at any
     * given case phase is implemented in this coordinator method.
     * 
     * Its core operation is to check case and event related qualities
     * and delegate further processing to event-type specific methods
     * also found in this coordinator
     * 
     * @param evList
     * @param cse the case to which the event should be added
     * @param ev the event to add to the case also included in this call
     * @return a reference to the same incoming List of EventCnF objects that
     * is passed in. Any additional events that need to be attached should be
     * appended to the end of this list

     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.ViolationException
     * @throws com.tcvcog.tcvce.domain.EventException
     */
    protected List<EventCnF> addEvent_processForCECaseDomain(
                                    List<EventCnF> evList, 
                                    CECaseDataHeavy cse, 
                                    EventCnF ev) 
                            throws  BObStatusException, 
                                    IntegrationException, 
                                    ViolationException, 
                                    EventException{
        EventType eventType = ev.getCategory().getEventType();
        EventIntegrator ei = getEventIntegrator();
        if(evList == null || cse == null || ev == null){
            throw new BObStatusException("Null argument to addEvent_ceCaseDomain");
        }
        
        switch(eventType){
            case Action:
                break;
            case Compliance:
                // TODO: Finish me
                break;
            case Closing:
//                insertedEventID = processClosingEvent(cse, ev);
                break;
            default:
                ev.setCeCaseID(cse.getCaseID());
        } // close switch
        return evList;
    } // close method
    
    
 
    /**
     * Logic container for assessing if any additinal events, etc should be 
     * created as a result of an EventRule pass
     * @param cse
     * @param rule
     * @throws IntegrationException
     * @throws BObStatusException
     * @throws ViolationException 
     */
    protected void processCaseOnEventRulePass(CECaseDataHeavy cse, EventRuleAbstract rule) 
            throws IntegrationException, BObStatusException, ViolationException{

        CaseIntegrator ci = getCaseIntegrator();
        EventCoordinator ec = getEventCoordinator();
        EventCnF newEvent = null;

        CasePhaseEnum oldCP = cse.getCasePhase();
//        ec.generateAndInsertPhaseChangeEvent(cse, oldCP, rule);
//        if(rule.getTriggeredEventCategoryID() != 0){
//            newEvent = ec.initEvent(cse, ec.initEventCategory(rule.getTriggeredEventCategoryID()));
//            
//            addEvent_processForCECaseDomain(cse, newEvent, null);
//            System.out.println("CaseCoordinator.processCaseOnEventRulePass "  + newEvent.getCategory().getEventCategoryTitle());
//        }
//        
    }

    /**
     * Implements business rules for determining which event types are allowed
     * to be attached to the given CECaseDataHeavy based on the case's phase and the
     * user's permissions in the system.
     * 
     * Used for displaying the appropriate event types to the user on the
     * cecases.xhtml page
     *
     * @param c the CECaseDataHeavy on which the event would be attached
     * @param u the User doing the attaching
     * @return allowed EventTypes for attaching to the given case
     */
    public List<EventType> getPermittedEventTypesForCECase(CECaseDataHeavy c, User u) {
        List<EventType> typeList = new ArrayList<>();
        RoleType role = u.getRoleType();

        if (role == RoleType.EnforcementOfficial
                || u.getRoleType() == RoleType.Developer) {
            typeList.add(EventType.Action);
            typeList.add(EventType.Timeline);
        }

        if (role != RoleType.MuniReader) {
            typeList.add(EventType.Communication);
            typeList.add(EventType.Meeting);
            typeList.add(EventType.Custom);
        }
        return typeList;
    }
    
   
    
   
     /**
      * Logic intermediary event for creating events documenting a CECase's closure
      * 
      * @param c
      * @param e
      * @return
      * @throws IntegrationException
      * @throws BObStatusException 
      */
    private int processClosingEvent(CECaseDataHeavy c, EventCnF e) throws IntegrationException, BObStatusException{
        EventIntegrator ei = getEventIntegrator();
        
        CasePhaseEnum closedPhase = CasePhaseEnum.Closed;
        c.setCasePhase(closedPhase);

        c.setClosingDate(LocalDateTime.now());
        updateCECaseMetadata(c);
        // now load up the closing event before inserting it
        // we'll probably want to get this text from a resource file instead of
        // hardcoding it down HERE in the Java
        e.setUserCreator(getSessionBean().getSessUser());
        e.setDescription(getResourceBundle(Constants.MESSAGE_TEXT).getString("automaticClosingEventDescription"));
        e.setNotes(getResourceBundle(Constants.MESSAGE_TEXT).getString("automaticClosingEventNotes"));
        return ei.insertEvent(e);
    }
    
 
    
    
    // *************************************************************************
    // *                     NOTICES OF VIOLATION                              *
    // *************************************************************************
    
    
    /**
     * Sets mailing fields to null] Params changed to take in UserAuthorized
     * during corruption recovery
     *
     * @param nov
     * @param user
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.AuthorizationException
     */
    public void novResetMailing(NoticeOfViolation nov, UserAuthorized user) throws IntegrationException, AuthorizationException{
        CaseIntegrator ci = getCaseIntegrator();
        if(user.getMyCredential().isHasSysAdminPermissions()){
            ci.novResetMailingFieldsToNull(nov);
        } else {
            throw new AuthorizationException("User does not have sufficient acces righst to clear notice mailing fields");
        }
    }
    
        
    /**
     * Called when first creating a notice of violation
     * @param cse
     * @param mdh
     * @return
     * @throws SQLException
     * @throws AuthorizationException 
     */
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
        while (iter.hasNext()) {
            CodeViolation cv = iter.next();
            CodeViolationDisplayable cvd = new CodeViolationDisplayable(cv);
            cvd.setIncludeHumanFriendlyText(false);
            cvd.setIncludeOrdinanceText(true);
            cvd.setIncludeViolationPhotos(false);
            nov.getViolationList().add(cvd);
        }

        return nov;

    }
    
    /**
     * NOV is ready to send - And coordinate creating an event to document this
     * @param c
     * @param nov
     * @param ua
     * @throws BObStatusException
     * @throws IntegrationException
     * @throws EventException
     * @throws ViolationException 
     */
    public void novLockAndQueue(CECaseDataHeavy c, NoticeOfViolation nov, UserAuthorized ua) 
            throws BObStatusException, IntegrationException, EventException, ViolationException{
        
        CaseIntegrator ci = getCaseIntegrator();
        EventCoordinator evCoord = getEventCoordinator();
        PersonCoordinator pc = getPersonCoordinator();
        PersonIntegrator pi = getPersonIntegrator();
        
        if(nov.getLockedAndqueuedTS() == null){
            int ghostID = pc.createChostPerson(nov.getRecipient(), ua);
            nov.setRecipient(pi.getPerson(ghostID));
            nov.setLockedAndqueuedTS(LocalDateTime.now());
            nov.setLockedAndQueuedBy(ua);
            ci.novLockAndQueueForMailing(nov);
            System.out.println("CaseCoordinator.novLockAndQueue | NOV locked in integrator");
        } else {
            throw new BObStatusException("Notice is already locked and queued for sending");
        }
        EventCnF noticeEvent = evCoord.initEvent(c, evCoord.initEventCategory(Integer.parseInt(getResourceBundle(
                Constants.EVENT_CATEGORY_BUNDLE).getString("noticeQueued"))));
        String queuedNoticeEventNotes = getResourceBundle(Constants.MESSAGE_TEXT).getString("noticeQueuedEventDesc");
        noticeEvent.setDescription(queuedNoticeEventNotes);
        noticeEvent.setUserCreator(ua);
        ArrayList<Person> al = new ArrayList();
        al.add(nov.getRecipient());
        noticeEvent.setPersonList(al);
        evCoord.addEvent(noticeEvent, c, ua);
    }
    
    /**
     * Called when the NOV is ready to get written to the DB --but before queuing 
     * for sending
     * 
     * @param nov
     * @param cse
     * @param usr
     * @return
     * @throws IntegrationException 
     */
    public int novInsertNotice(NoticeOfViolation nov, CECaseDataHeavy cse, User usr) throws IntegrationException{
        CaseIntegrator ci = getCaseIntegrator();
        System.out.println("CaseCoordinator.novInsertNotice");
        return ci.novInsert(cse, nov);
    }
    
    /**
     * Logic pass-through for NOV updates
     * @param nov
     * @throws IntegrationException 
     */
    public void novUpdate(NoticeOfViolation nov) throws IntegrationException{
        CaseIntegrator ci = getCaseIntegrator();
        ci.novUpdateNotice(nov);
    }
    
    /**
     * Business logic intermediary method for marking a NOV as having already
     * been sent to the property
     * @param ceCase
     * @param nov
     * @param user
     * @throws BObStatusException
     * @throws EventException
     * @throws IntegrationException 
     */
    public void novMarkAsSent(CECaseDataHeavy ceCase, NoticeOfViolation nov, User user) throws BObStatusException, EventException, IntegrationException{
        CaseIntegrator ci = getCaseIntegrator();
        nov.setSentTS(LocalDateTime.now());
        nov.setSentBy(user);
        ci.novUpdateNotice(nov);   
    }
    
    /**
     * Logic container for configuring a NOV as returned by the postal service
     * 
     * @param c
     * @param nov
     * @param user
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public void novMarkAsReturned(CECaseDataHeavy c, NoticeOfViolation nov, User user) throws IntegrationException, BObStatusException{
        CaseIntegrator ci = getCaseIntegrator();
        nov.setReturnedTS(LocalDateTime.now());
        nov.setReturnedBy(user);
        ci.novUpdateNotice(nov);
    } 
    
    /**
     * Checks logic before deleting NOVs. It's rare to be able to delete something
     * but an unmailed NOV is a very low impact loss
     * @param nov
     * @throws BObStatusException 
     */
    public void novDelete(NoticeOfViolation nov) throws BObStatusException{
        CaseIntegrator ci = getCaseIntegrator();

        //cannot delete a letter that was already sent
        if (nov != null && nov.getSentTS() != null) {
            throw new BObStatusException("Cannot delete a letter that has been sent");
        } else {
            try {
                ci.novDelete(nov);
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to delete notice of violation due to a database error", ""));
            }
        }
    }
    
    
    
    // *************************************************************************
    // *                     CITATIONS                                         *
    // *************************************************************************
    
    
   /**
    * Called to create a new citation for a given List of CodeViolation objects
    * @param violationList
    * @return 
    */
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
    * Logic intermediary for recording that a Citation has been issued, meaning
    * sent to the proper magisterial authority
    * 
    * @param c
    * @throws IntegrationException 
    */
   public void issueCitation(Citation c) throws IntegrationException{
       CaseIntegrator ci = getCaseIntegrator();
       ci.insertCitation(c);
       
   }
   
   /**
    * Logic intermediary for updating fields on Citations in the DB
    * @param c
    * @throws IntegrationException 
    */
   public void updateCitation(Citation c) throws IntegrationException{
       CaseIntegrator ci = getCaseIntegrator();
       ci.updateCitation(c);
       
   }
   
   
//    --------------------------------------------------------------------------
//    ********************* CE Action Requests *********************************
//    --------------------------------------------------------------------------
    /**
     * Factory method for our CEActionRequests - initializes the date as well
     *
     * @return The CEActionRequest ready for populating with user values
     */
    public CEActionRequest getInititalizedCEActionRequest() {
        System.out.println("CaseCoordinator.getNewActionRequest");
        CEActionRequest cear = new CEActionRequest();
        // start by writing in the current date
        cear.setDateOfRecord(LocalDateTime.now());

        return new CEActionRequest();

    }

    /**
     * Business logic intermediary method for CEActionRequests. Calls the
     * CEAction Integrator
     *
     * @param cearid
     * @return
     * @throws IntegrationException
     */
    public CEActionRequest getCEActionRequest(int cearid) throws IntegrationException {
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        return ceari.getActionRequestByRequestID(cearid);
    }

    /**
     * Utility method for determining whether or not the panel of Code
     * Enforcement request routing buttons can be pressed. Used by the view for
     * setting disabled properties on buttons requests
     *
     * @param req the current CE Request
     * @param u current user
     * @return True if the current user can route the given ce request
     */
    public boolean determineCEActionRequestRoutingActionEnabledStatus(
            CEActionRequest req,
            UserAuthorized u) {
        if (req != null && u.getMyCredential() != null) {
            if ((req.getRequestStatus().getStatusID()
                    == Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                            .getString("actionRequestInitialStatusCode")))
                    && u.getMyCredential().isHasEnfOfficialPermissions()) {
                return true;
            }
        }
       return false;
   }
   
    public List<CEActionRequestIssueType> getIssueTypes(Municipality muni) throws IntegrationException{
        
        List<CEActionRequestIssueType> typeList = new ArrayList();
        
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        
        typeList = ceari.getRequestIssueTypeList(muni);
        
        return typeList;
        
    }
    
   
    // *************************************************************************
    // *                     VIOLATIONS                                        *
    // *************************************************************************
    
     
     /**
      * Violation status enum values have associated ordinal values
      * which this method looks at to determine the highest one, which
      * it returns and then goes on break
      * 
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
    private void checkForFullComplianceAndCloseCaseIfTriggered(CECaseDataHeavy c, UserAuthorized ua) 
            throws IntegrationException, BObStatusException, ViolationException, EventException{
        
        EventCoordinator ec = getEventCoordinator();
        EventIntegrator ei = getEventIntegrator();

        boolean complianceWithAllViolations = false;
        
        ListIterator<CodeViolation> fullViolationLi = c.getViolationList().listIterator();
        
        
        CodeViolation cv = null;
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
            ec.addEvent(complianceClosingEvent, c, ua);
            
        } // close if
        
    }
    
    /**
     * Factory method for starting the process of making a new CodeViolation
     * object
     * 
     * @param c
     * @param ece
     * @return 
     */
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
     * ALSO coordinates creating a corresponding proposal to match the stipulated compliance
     * date on the violation that's added.
     * @param cv
     * @param cse
     * @param ua
     * @return the database key assigned to the inserted violation
     * @throws IntegrationException
     * @throws ViolationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.EventException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public int attachViolationToCaseAndInsertTimeFrameEvent(CodeViolation cv, CECaseDataHeavy cse, UserAuthorized ua) 
            throws IntegrationException, ViolationException, BObStatusException, EventException, SearchException{
        
        CaseIntegrator ci = getCaseIntegrator();
        EventCoordinator ec = getEventCoordinator();
        UserCoordinator uc = getUserCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        EventCnF tfEvent;
        PaymentCoordinator pc = getPaymentCoordinator();
        int insertedViolationID;
        int eventID;
        StringBuilder sb = new StringBuilder();
        
        
        EventCategory eventCat = ec.initEventCategory(
                Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE)
                        .getString("complianceTimeframeExpiry")));
//        EventCategory eventCat = ec.initEventCategory(113);
        tfEvent = ec.initEvent(cse, eventCat);
        tfEvent.setTimeStart(cv.getStipulatedComplianceDate());
        tfEvent.setUserCreator(cse.getCaseManager());
        
        sb.append(getResourceBundle(Constants.MESSAGE_TEXT)
                .getString("complianceTimeframeEndEventDesc"));
        sb.append("Case: ");
        sb.append(cse.getCaseName());
        sb.append(" at ");
        sb.append(cc.assembleCECasePropertyUnitHeavy(cse).getProperty().getAddress());
        sb.append("(");
        sb.append(cc.assembleCECasePropertyUnitHeavy(cse).getProperty().getMuni().getMuniName());
        sb.append(")");
        sb.append("; Violation: ");
        sb.append(cv.getViolatedEnfElement().getCodeElement().getHeaderString());
        tfEvent.setDescription(sb.toString());
        
        if(verifyCodeViolationAttributes(cse, cv)){
            insertedViolationID = ci.insertCodeViolation(cv);
        } else {
            throw new ViolationException("Failed violation verification");
        }

        pc.insertAutoAssignedFees(cse, cv);
        return insertedViolationID;
    }
    
    public CodeViolation getCodeViolation(int vid) throws IntegrationException{
        CaseIntegrator ci = getCaseIntegrator();
        CodeViolation cv = ci.getCodeViolation(vid);
        return configureCodeViolation(cv);
    }
    
    /**
     * Uses date fields on the populated CodeViolation to determine a status
     * string and icon for UI Called by the integrator when creating a code
     * violation
     *
     * @param cv
     * @return the CodeViolation with correct icon and status
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public CodeViolation configureCodeViolation(CodeViolation cv) throws IntegrationException {
        SystemIntegrator si = getSystemIntegrator();
        if (cv.getActualComplianceDate() == null) {
            // violation still within compliance timeframe
            if (cv.getDaysUntilStipulatedComplianceDate() >= 0) {

                cv.setStatus(ViolationStatusEnum.UNRESOLVED_WITHINCOMPTIMEFRAME);
                cv.setIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString(cv.getStatus().getIconPropertyName()))));

                // violation has NOT been cited, but is past compliance timeframe end date
            } else if (cv.getCitationIDList().isEmpty()) {

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
    
    
    /**
     * Internal logic container for checking status of BObs before allowing updating
     * of the underlying CodeViolation record
     * @param cse
     * @param cv
     * @return
     * @throws ViolationException 
     */
    private boolean verifyCodeViolationAttributes(CECaseDataHeavy cse, CodeViolation cv) throws ViolationException{
        if(cse.getCasePhase() == CasePhaseEnum.Closed){
            throw new ViolationException("Cannot update code violations on closed cases!");

        }
        if (cv.getStipulatedComplianceDate().isBefore(cv.getDateOfRecord())) {
            throw new ViolationException("Stipulated compliance date cannot be before the violation's date of record");
        }

        return true;
    }
    
    /**
     * Access point for updating a CodeViolation record
     * @param cse
     * @param cv
     * @param u
     * @throws ViolationException
     * @throws IntegrationException
     * @throws EventException 
     */
    public void updateCodeViolation(    CECaseDataHeavy cse, 
                                        CodeViolation cv, 
                                        UserAuthorized u) 
                            throws      ViolationException, 
                                        IntegrationException, 
                                        EventException{
        
        EventCoordinator ec = getEventCoordinator();
        CaseIntegrator ci = getCaseIntegrator();
        EventIntegrator ei = getEventIntegrator();
        
        if(verifyCodeViolationAttributes(cse, cv)){
            ci.updateCodeViolation(cv);
        }
    
    } // close method
    
    /**
     * Attempts to deactivate a code violation, but will thow an Exception
     * if the CodeViolation has been used in a notice or in a citation 
     * @param cv
     * @param ua 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void deactivateCodeViolation(CodeViolation cv, UserAuthorized ua) throws BObStatusException, IntegrationException{
        CaseIntegrator ci = getCaseIntegrator();
        
        if(cv.getCitationIDList() != null && !cv.getCitationIDList().isEmpty()){
            throw new BObStatusException("Cannot deactivate a violation that has been used in a citation");
        }
        
        if(cv.getNoticeIDList() != null && !cv.getNoticeIDList().isEmpty()){
            throw new BObStatusException("Cannot deactivate a violation that has been used in a notice");
        }
        
        cv.setActive(false);
        ci.updateCodeViolation(cv);
        
    }

    /**
     * CodeViolation should have the actual compliance date set from the user's
     * event date of record
     *
     * @param cv
     * @param u
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void recordCompliance(CodeViolation cv, UserAuthorized u) throws IntegrationException{
        
        CaseIntegrator ci = getCaseIntegrator();
        
        // update violation record for compliance
        cv.setComplianceUser(u);
        cv.setComplianceTimeStamp(LocalDateTime.now());
        cv.setLastUpdatedUser(u);
        cv.setLastUpdatedTS(LocalDateTime.now());
        
        ci.updateCodeViolation(cv);
    }

    /**
     * Utility method for grabbing a list of CodeViolations given a CECase
     * @param ceCase
     * @return
     * @throws IntegrationException 
     */
    public List<CodeViolation> getCodeViolations(CECaseDataHeavy ceCase) throws IntegrationException{
        CaseIntegrator ci = getCaseIntegrator();
        List<CodeViolation> al = ci.getCodeViolations(ceCase);
        return al;
    }
    
} // close class
