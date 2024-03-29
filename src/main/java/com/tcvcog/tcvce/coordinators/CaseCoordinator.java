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
import com.tcvcog.tcvce.application.LegendItem;
import com.tcvcog.tcvce.application.interfaces.IFace_EventRuleGoverned;
import com.tcvcog.tcvce.domain.*;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.occupancy.FieldInspection;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpaceElement;
import com.tcvcog.tcvce.entities.reports.ReportCECaseListCatEnum;
import com.tcvcog.tcvce.entities.reports.ReportCECaseListStreetCECaseContainer;
import com.tcvcog.tcvce.entities.search.*;
import com.tcvcog.tcvce.integration.*;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.DateTimeUtil;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveListsEnum;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.hbar.HorizontalBarChartModel;
import org.primefaces.model.charts.optionconfig.title.Title;
import org.primefaces.model.charts.optionconfig.tooltip.Tooltip;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class CaseCoordinator extends BackingBeanUtils implements Serializable {

    /**
     * Where cases start
     */
    final CasePhaseEnum initialCECasePphase = CasePhaseEnum.PrelimInvestigationPending;

    final static int GRIDSQAURES_COLS_1 = 12;
    final static int GRIDSQAURES_COLS_2 = 6;
    final static int GRIDSQAURES_COLS_3 = 4;
    final static int GRIDSQAURES_COLS_4 = 3;
    /**
     * Unspecified days to comply in an ordinance will mean draw from this
     * member TODO: make a municipality setting on a muniprofile
     */
    final int FALLBACK_DAYSTOCOMPLY = 30;

    /**
     * "Give them a bit more time"
     */
    public final static int DEFAULT_EXTENSIONDAYS = 14;
    final static String ADDRESSLESS_PROPERTY_STREET_NAME = "Not attached to a street";

    final static int ARBITRARILY_LONG_TIME_YEARS = 113; // arbitrary magic number of years into the future (thanks WWALK)
    
    public final static RoleType MIN_RANK_TO_ISSUE_CITATION = RoleType.EnforcementOfficial;

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
     * @param c MUST BE A CURRENT VERSION OF A CASE!! Designed to be called with cecase_getCECase(int) on this class!!!
     * @param ua
     * @param profile
     * @return the CECaseDataHeavy with the action request list ready to roll
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public CECaseDataHeavy cecase_assembleCECaseDataHeavy(CECase c, UserAuthorized ua)
            throws BObStatusException,
            IntegrationException,
            SearchException {

        Credential cred = null;
        if (ua != null && c != null && c.getCaseID() != 0) {
            cred = ua.getKeyCard();

        } else {
            throw new BObStatusException("CaseCoordinator.cecase_assembleCECaseDataHeavy | Cannot construct cecaseDH with null case input or case ID = 0");
        }
        SearchCoordinator sc = getSearchCoordinator();
        WorkflowCoordinator wc = getWorkflowCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        BlobCoordinator bc = getBlobCoordinator();
        PersonCoordinator persc = getPersonCoordinator();
        OccInspectionCoordinator oic = getOccInspectionCoordinator();

        // Wrap our base class in the subclass wrapper--an odd design structure, indeed
//        CECaseDataHeavy cse = new CECaseDataHeavy(cecase_getCECase(c.getCaseID()));
        CECaseDataHeavy cse = new CECaseDataHeavy(c);

        try {

            cse.setProperty(pc.getProperty(c.getParcelKey()));
            cse.setPropertyUnit(pc.getPropertyUnit(c.getPropertyUnitID()));

            // PROPOSAL LIST
//            cse.setProposalList(wc.getProposalList(cse, cred));

            // EVENT RULE LIST
//            cse.setEventRuleList(wc.rules_getEventRuleImpList(cse, cred));

            // Human list
            cse.setHumanLinkList(persc.getHumanLinkList(cse));

            // Inspection list
            cse.setInspectionList(oic.getOccInspectionList(cse));

            // CEAR LIST
//            QueryCEAR qcear = sc.initQuery(QueryCEAREnum.ATTACHED_TO_CECASE, cred);
//            qcear.getPrimaryParams().setCecase_ctl(true);
//            qcear.getPrimaryParams().setCecase_val(c);
//
//            cse.setCeActionRequestList(sc.runQuery(qcear).getBOBResultList());

            // BLOB
            cse.setBlobList(bc.getBlobLightList(cse));

        } catch (BlobException | BObStatusException ex) {
            System.out.println(ex);
        }

        // Skip payment stuff for now--i totally broke this stuff
//        try {
//            cse.setFeeList(pi.getFeeAssigned(c));
//
//            cse.setPaymentListGeneral(pi.getPaymentList(c));
//        } catch (IntegrationException ex) {
//            System.out.println(ex);
//        }
        return cse;
    }

    /**
     * Utility method for calling cecase_assembleCECaseDataHeavy() for each base
     * CECase in the given list
     *
     * @param cseList
     * @param ua
     * @return
     */
    public List<CECaseDataHeavy> cecase_assembleCECaseDataHeavyList(List<CECase> cseList, UserAuthorized ua) {
        List<CECaseDataHeavy> cseDHList = new ArrayList<>();
        if (cseList != null && !cseList.isEmpty()) {
            for (CECase cse : cseList) {
                try {
                    cseDHList.add(cecase_assembleCECaseDataHeavy(cecase_getCECase(cse.getCaseID(), ua), ua));
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
    public List<CECase> cecase_downcastCECasePropertyUnitHeavyList(List<CECasePropertyUnitHeavy> cspudhList) {
        List<CECase> cslist = new ArrayList<>();
        if (cspudhList != null && !cspudhList.isEmpty()) {
            for (CECasePropertyUnitHeavy c : cspudhList) {
                cslist.add((CECase) c);
            }

        }
        return cslist;
    }

  

    /**
     * Manages the closing of a case prior to full violation compliance. Will
     * nullify any existing violations on the CECase and attach a close case
     * event based on the inputted value
     *
     * @param cse the case to force close
     * @param closeEventCat the EventCategory associated with the closure reason
     * @param ua the user doing the force closing
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.EventException
     */
    public void cecase_forceclose(CECaseDataHeavy cse, EventCategory closeEventCat, UserAuthorized ua) throws BObStatusException, IntegrationException, EventException {
        CaseIntegrator ci = getCaseIntegrator();
        EventCoordinator ec = getEventCoordinator();
        if (cse == null) {
            throw new BObStatusException("Cannot close a null case");
        }
//        if(cse.getClosingDate() != null){
//            throw new BObStatusException("Cannot force close an already closed case");
//        }
        if (closeEventCat == null) {
            throw new BObStatusException("Cannot close case with null closing event");
        }

        if (closeEventCat.getEventType() != EventType.Closing) {
            throw new BObStatusException("Cannot close case with an event that's not of type: Closing");
        }

        // Nullify violations on case
        if (cse.getViolationList() != null && !cse.getViolationList().isEmpty()) {
            for (CodeViolation cv : cse.getViolationList()) {
                if (cv.getActualComplianceDate() == null) {
                    violation_NullifyCodeViolation(cv, ua);
                }
            }
        }

//        cse.setClosingDate(LocalDateTime.now());
        ci.updateCECaseMetadata(cse);
        events_processClosingEvent(cse, ec.initEvent(cse, closeEventCat), ua);

    }

    /**
     * Primary pathway for retrieving the data-light superclass CECase.
     * Implements business logic.
     *
     * @param caseID
     * @param ua
     * @return
     * @throws IntegrationException
     */
    public CECase cecase_getCECase(int caseID, UserAuthorized ua)
            throws IntegrationException {
        CaseIntegrator ci = getCaseIntegrator();
        EventCoordinator ec = getEventCoordinator();
        SystemIntegrator si = getSystemIntegrator();

        CECase cse = null;
        try {
            cse = ci.getCECase(caseID);
            if (cse != null) {

                // Inject all cases with lists of their violations, notices, citations, 
                // and events, which will be used to determine case phase
                cse.setNoticeList(nov_getNoticeOfViolationList(ci.novGetList(cse)));
                Collections.sort(cse.getNoticeList());
                Collections.reverse(cse.getNoticeList());

                cse.setCitationList(citation_getCitationList(cse));

                cse.setViolationList(violation_getCodeViolations(ci.getCodeViolations(cse)));
                Collections.sort(cse.getViolationList());

                cse.setEventList(ec.getEventList(cse));
                Collections.sort(cse.getEventList());
                Collections.reverse(cse.getEventList());

                // configure origination & closing events
                cse.setOriginationEvent(ec.findMostRecentEventByType(cse.getEventList(), EventType.Origination));
                cse.setClosingEvent(ec.findMostRecentEventByType(cse.getEventList(), EventType.Closing));
                
                boolean rl = cecase_auditCECase(cse, getSessionBean().getSessUser());
                
                if(rl){
                    System.out.println("CaseCoordinator.cecase_getCECase | reloading due to audit trigger = true");
                    cse = ci.getCECase(caseID);
                }
                cse.setPinner(ua);
                cse.setPinned(si.getPinnedStatus(cse));
                
                cecase_configureMostRecentPastEvent(cse);
                cecase_configureCECasePriority(cse, ua.getGoverningMuniProfile());
                
        if (cse.getPriority() != null) {
            
            //now set the icon based on what phase we just assigned the case to
            cse.setPriorityIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                    .getString(cse.getPriority().getIconPropertyLookup()))));
        }
                
                // Replaced with priorities!
//                cecase_configureCECaseStageAndPhase(cse);
            }
        } catch (BObStatusException | BlobException ex) {
            System.out.println(ex);
        }
        return cse;
    }
    
    
    /**
     * Big important logic container for checking all sorts of attributes about
     * a CECase and rectifying the situation as possible, creating events and notes 
     * along the way
     * 
     * This method is 
     * 
     * @param cse at any stage, or phase, even before a DB write!
     * @param ua 
     */
    private boolean cecase_auditCECase(CECase cse, UserAuthorized ua) throws BObStatusException{
       
        if(cse == null || ua == null){
           throw new BObStatusException("Cannot audit null case or null user");
       }
        boolean reloadTrigger = false;
        StringBuilder auditLog = new StringBuilder();
        auditLog.append("AUDIT LOG for CECase ID: ");
        auditLog.append(cse.getCaseID());
        auditLog.append(Constants.FMT_HTML_BREAK);
        
        
       if(cse.getCaseManager() == null){
           auditLog.append("*NO case manager found");
           throw new BObStatusException(auditLog.toString());
       }
       
        if (cse.getOriginationDate() == null) {
            cse.setOriginationDate(LocalDateTime.now());
        }
        
        if (cse.getParcelKey() == 0) {
            throw new BObStatusException("Cases must have a nonzero property id");
        }
        
       // for cases that aren't just getting written to DB
    /*
       
       if(cse.getCaseID() != 0){
           try {

                // origination event
               if(cse.getOriginationEvent() == null){
                   auditLog.append("Creating generic opening event;");
                   cecase_auditcase_attachGenericOriginationEvent(cse, ua);
                  // turn off to avoid cycles
//                   cse = cecase_getCECase(cse.getCaseID());
                    reloadTrigger = true;
               }

               if(cse.getClosingDate() != null && cse.getClosingEvent() == null){
                   auditLog.append("Creating generic closingevent;");
                   cecase_auditcase_attachGenericClosingEvent(cse, ua);
                  // turn off to avoid cycles
//                   cse = cecase_getCECase(cse.getCaseID());
                    reloadTrigger= true;
               }
           } catch (BObStatusException | EventException | IntegrationException e) {
               auditLog.append("Could not add new origination or closing event");
               throw new BObStatusException(auditLog.toString());
           }
           
           
           // closing and opening date
            if (cse.getClosingDate() != null && cse.getOriginationDate() != null) {
                if (cse.getClosingDate().isBefore(cse.getOriginationDate())) {
                    auditLog.append("Cases cannot close before they open");
                    throw new BObStatusException(auditLog.toString());
                }
            }
       }
    */
       return reloadTrigger;
    }
    
    
    
    /**
     * Used during the case audit process to add an origination event to a case
     * that doesn't have one
     * @param cse
     * @param ua 
     */
    private void cecase_auditcase_attachGenericOriginationEvent(CECase cse, UserAuthorized ua) throws IntegrationException, BObStatusException, EventException{
        EventCoordinator ec = getEventCoordinator();
        UserCoordinator uc = getUserCoordinator();
        EventCategory origCat = ec.getEventCategory(Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE)
                                                    .getString("cecase_genericoriginationeventcatid")));
        
        EventCnF origEv = ec.initEvent(cse, origCat);
        origEv.setCreatedBy(uc.user_getUserRobot());
        origEv.setTimeStart(cse.getOriginationDate());
        origEv.setTimeEnd(origEv.getTimeStart().plusMinutes(origCat.getDefaultDurationMins()));
        origEv.setDescription(origCat.getEventCategoryDesc());
        ec.addEvent(origEv, cse, ua);
        
    }
    
    
    /**
     * Used during the case audit process to add a closing event to a case
     * that doesn't have one
     * @param cse
     * @param ua 
     */
    private void cecase_auditcase_attachGenericClosingEvent(CECase cse, UserAuthorized ua) throws IntegrationException, BObStatusException, EventException{
        EventCoordinator ec = getEventCoordinator();
        UserCoordinator uc = getUserCoordinator();
        EventCategory origCat = ec.getEventCategory(Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE)
                                                            .getString("cecase_genericclosingeventcatid")));
        
        EventCnF origEv = ec.initEvent(cse, origCat);
        origEv.setCreatedBy(uc.user_getUserRobot());
        origEv.setTimeStart(cse.getOriginationDate());
        origEv.setTimeEnd(origEv.getTimeStart().plusMinutes(origCat.getDefaultDurationMins()));
        origEv.setDescription(origCat.getEventCategoryDesc());
        ec.addEvent(origEv, cse, ua);
        
    }
    
    
    
    
    
    
    /**
     * Determines a case's most recent past event for use in priority jazz
     * 
     * @param cse 
     */
    private void cecase_configureMostRecentPastEvent(CECase cse){
        if(cse != null && cse.getEventList() != null && !cse.getEventList().isEmpty()){
            for(EventCnF ev : cse.getEventList()){
                if(ev.getTimeStart() != null){
                    if(ev.getTimeStart().isBefore(LocalDateTime.now())){
                        cse.setMostRecentPastEvent(ev);
                        cse.setDaysSinceLastEvent(DateTimeUtil.getTimePeriodAsDays(ev.getTimeStart(), LocalDateTime.now()));
                        break;
                    }
                }    
            }
        }
    }
    

    /**
     * Builds a property and propertyUnit heavy subclass of our CECase object
     *
     * @param cse
     * @return the data-rich subclass with Property and possible PropertyUnit
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public CECasePropertyUnitHeavy cecase_assembleCECasePropertyUnitHeavy(CECase cse) throws IntegrationException, SearchException, BObStatusException, BlobException {
        PropertyCoordinator pc = getPropertyCoordinator();

        CECasePropertyUnitHeavy csepuh = null;

        if (cse != null) {
            csepuh = new CECasePropertyUnitHeavy(cse);
            csepuh.setProperty(pc.getProperty(cse.getParcelKey()));
            if (cse.getPropertyUnitID() != 0) {
                csepuh.setPropUnit(pc.getPropertyUnit(cse.getPropertyUnitID()));
            }

        }
        return csepuh;
    }

    /**
     * Utility method for converting a List of simple CECase objects into the a
     * case that contains information about its Property and any associated
     * Units
     *
     * @param cseList
     * @return
     */
    public List<CECasePropertyUnitHeavy> cecase_assembleCECasePropertyUnitHeavyList(List<CECase> cseList) throws BObStatusException {

        List<CECasePropertyUnitHeavy> cspudhList = new ArrayList<>();

        if (cseList != null && !cseList.isEmpty()) {
            if (getSessionBean().getSessUser() != null) {

                for (CECase cse : cseList) {
                    try {
                        cspudhList.add(cecase_assembleCECasePropertyUnitHeavy(cse));
                    } catch (IntegrationException | SearchException | BObStatusException | BlobException  ex) {
                        System.out.println(ex);

                    }
                }
            } else {
                //This session must be public
                UserCoordinator uc = getUserCoordinator();
                for (CECase cse : cseList) {
                    try {
                        cspudhList.add(cecase_assembleCECasePropertyUnitHeavy(cse));
                    } catch (IntegrationException | SearchException  | BObStatusException | BlobException ex) {
                        System.out.println(ex);
                    }
                }
            }

        }

        return cspudhList;
    }

    /**
     * Utility method for converting a List of simple CECase objects into the a
     * case that contains information about its Property and any associated
     * Units
     *
     * @param cseList
     * @return
     */
    public List<CECasePropertyUnitHeavy> cecase_refreshCECasePropertyUnitHeavyList(List<CECasePropertyUnitHeavy> cseList) throws BObStatusException {

        List<CECasePropertyUnitHeavy> cspudhList = new ArrayList<>();

        if (cseList != null && !cseList.isEmpty()) {
            if (getSessionBean().getSessUser() != null) {

                for (CECase cse : cseList) {
                    try {
                        cspudhList.add(cecase_assembleCECasePropertyUnitHeavy(cse));
                    } catch (IntegrationException | SearchException | BObStatusException | BlobException ex) {
                        System.out.println(ex);

                    }
                }
            } else {
                //This session must be public
                UserCoordinator uc = getUserCoordinator();
                for (CECase cse : cseList) {
                    try {
                        cspudhList.add(cecase_assembleCECasePropertyUnitHeavy(cse));
                    } catch (IntegrationException | SearchException | BObStatusException | BlobException ex) {
                        System.out.println(ex);
                    }
                }
            }

        }

        return cspudhList;
    }

    /**
     * Asks the Integrator for an icon based on case phase
     *
     * @param phase
     * @return
     * @throws IntegrationException
     */
    public Icon cecase_getIconByCasePhase(CasePhaseEnum phase) throws IntegrationException {
        CaseIntegrator ci = getCaseIntegrator();
        return ci.getIcon(phase);
    }

    
    /**
     * Primary logic block for interrogating a case to assign it a priority Enum 
     * and write a meaningful log
     * @param cse
     * @return 
     */
    private CECase cecase_configureCECasePriority(CECase cse, MuniProfile profile) throws BObStatusException, IntegrationException{
        if(cse == null || profile == null){
            throw new BObStatusException("Cannot assign priority to null");
        }
        
        PropertyCoordinator pc = getPropertyCoordinator();
        Parcel parcel = pc.getProperty(cse.getParcelKey());
        
        
        // Get all letters that have been sent
        List<NoticeOfViolation> novSentList = cse.assembleNoticeList(ViewOptionsActiveListsEnum.VIEW_ACTIVE);
        // Get all violations that are active
        List<CodeViolation> codeViolActiveList = cse.assembleViolationList(ViewOptionsActiveListsEnum.VIEW_ACTIVE);
        
       cse.logPriorityAssignmentMessage("Assigning priority to CECase ID: ", false);
       cse.logPriorityAssignmentMessage(String.valueOf(cse.getCaseID()), true);
        
        if (cse.getPersonInfoPersonID() != 0 ) {
            cse.logPriorityAssignmentMessage("Found non-zero person info ID or prop info case TRUE;", true);
            cse.logPriorityAssignmentMessage("Assigning priorirty: CONTAINER.", true);
            cse.setPriority(PriorityEnum.CONTAINER);
            
        } else if (cse.getClosingDate() != null){
            cse.logPriorityAssignmentMessage("Case has a closing date;", true);
            cse.logPriorityAssignmentMessage("Assigning priority: CLOSED.", true);
            cse.setPriority(PriorityEnum.CLOSED);
            
        } else if (parcel != null && parcel.getParcelInfo() != null && parcel.getParcelInfo().isAbandoned()){
            cse.logPriorityAssignmentMessage("Case is not closed but is attached to a parcel inside abandonment window;", true);
            cse.logPriorityAssignmentMessage("This is an overriding priority and the logic is ignoring all citation and violation statuses;", true);
            cse.logPriorityAssignmentMessage("Assigning priority: ABANDONED STALL.", true);
            cse.setPriority(PriorityEnum.ABANDONMENT_STALL);
            
        } else if (!cecase_buildCitationListForPhaseAssignment(cse).isEmpty()) {
            cse.logPriorityAssignmentMessage("Found at least one citation;", true);
            cse.logPriorityAssignmentMessage("Assigning priority: CITATION.", true);
            cse.setPriority(PriorityEnum.CITATION);
            
        } else if (cse.getViolationList() == null || cse.getViolationList().isEmpty()) {
            cse.logPriorityAssignmentMessage("Found zero violations but I'm not a container case;", true);
            cse.logPriorityAssignmentMessage("Assigning priority: OPENING.", true);
            cse.setPriority(PriorityEnum.OPENING);
            // we have at least one violation
            // Let's figure out if we've been sitting open beyond allotted buffer
        } else if (codeViolActiveList != null && !codeViolActiveList.isEmpty()) {
            cse.logPriorityAssignmentMessage("Found ", false);
            cse.logPriorityAssignmentMessage(String.valueOf(codeViolActiveList.size()), false);
            cse.logPriorityAssignmentMessage(" active violations on case;", true);
            // see if we have at least one letter
            if(novSentList != null && !novSentList.isEmpty()){
                cse.logPriorityAssignmentMessage("Found ", false);
                cse.logPriorityAssignmentMessage(String.valueOf(novSentList.size()), false);
                cse.logPriorityAssignmentMessage(" sent letters on case;", true);
                
                NoticeOfViolation mostRecentLetter = novSentList.get(0);
                // bug fix for letters without type
                if(mostRecentLetter != null && mostRecentLetter.getNovType() != null){

                    cse.logPriorityAssignmentMessage("Using letter follow up window of : ", false);
                    cse.logPriorityAssignmentMessage(String.valueOf(mostRecentLetter.getNovType().getFollowUpWindowDays()), false);
                    cse.logPriorityAssignmentMessage(" days for letters of type: ", false);
                    cse.logPriorityAssignmentMessage(mostRecentLetter.getNovType().getTitle(), false);
                    cse.logPriorityAssignmentMessage(";", true);

                    long daysSinceLatestLetter = DateTimeUtil.getTimePeriodAsDays(mostRecentLetter.getDateOfRecord(), LocalDateTime.now());

                    cse.logPriorityAssignmentMessage("Most recent letter was marked as sent ", false);
                    cse.logPriorityAssignmentMessage(String.valueOf(daysSinceLatestLetter), false);
                    cse.logPriorityAssignmentMessage(" days ago on ", false);
                    cse.logPriorityAssignmentMessage(getPrettyDateNoTime(mostRecentLetter.getDateOfRecord()), false);
                    cse.logPriorityAssignmentMessage(" (Letter ID: ", false);
                    cse.logPriorityAssignmentMessage(String.valueOf(mostRecentLetter.getNoticeID()), false);
                    cse.logPriorityAssignmentMessage(")", true);


                    if(daysSinceLatestLetter < 0){
                        cse.logPriorityAssignmentMessage("Most recent letter was sent in the future? Hark!", true);
                        cse.setPriority(PriorityEnum.UNKNOWN);
    //                    return cse;
                    } else if(daysSinceLatestLetter <= mostRecentLetter.getNovType().getFollowUpWindowDays()) {
                        cse.logPriorityAssignmentMessage("Case is inside letter type appeals/waiting window;", true);
                        cse.logPriorityAssignmentMessage("Assigning priority: MONITORING", true);
                        cse.setPriority(PriorityEnum.MONITORING);
    //                    return cse;
                        // if we're within administrative buffer, then we're yellow
                    } else if(daysSinceLatestLetter > mostRecentLetter.getNovType().getFollowUpWindowDays() 
                            && daysSinceLatestLetter <= DateTimeUtil.getTimePeriodAsDays(mostRecentLetter.getDateOfRecord(), mostRecentLetter.getDateOfRecord().plusDays(profile.getPriorityParamDeadlineAdministrativeBufferDays())) ){
                        cse.logPriorityAssignmentMessage("Letter appeals/waiting period has expired, but inside administrative buffer;", true);
                        cse.logPriorityAssignmentMessage("Assigning priority: ACTION REQUIRED", true);
                        cse.setPriority(PriorityEnum.ACTION_REQUIRED);
    //                    return cse;

                    } else {
                        cse.logPriorityAssignmentMessage("Letter appeals/waiting period has expired, and you're OUTSIDE administrative buffer;", true);
                        cse.logPriorityAssignmentMessage("Assigning priority: ACTION PAST DUE", true);
                        cse.setPriority(PriorityEnum.ACTION_PASTDUE);
    //                    return cse;
                    }
                } else {
                    cse.logPriorityAssignmentMessage("Object status error (Fatal): Most recent letter is null or type is null;", false);
                    cse.setPriority(PriorityEnum.UNKNOWN);
                    
                }// close we have a most recent NOV and not null type

            } else {
                // we have No letters sent, so we need to know are we yellow or red?
                cse.logPriorityAssignmentMessage("Found zero sent letters;", true);
                CodeViolation earliestV = codeViolActiveList.get(codeViolActiveList.size() - 1);
                long daysSinceEarliestActiveViolation = DateTimeUtil.getTimePeriodAsDays(earliestV.getDateOfRecord(), LocalDateTime.now());
                cse.logPriorityAssignmentMessage("Using violation-letter buffer: ", false);
                cse.logPriorityAssignmentMessage(String.valueOf(profile.getPriorityParamLetterSendBufferDays()), false);
                cse.logPriorityAssignmentMessage(" days; ", true);
                
                cse.logPriorityAssignmentMessage("Earliest code violation's date of record is ", false);
                cse.logPriorityAssignmentMessage(String.valueOf(daysSinceEarliestActiveViolation), false);
                cse.logPriorityAssignmentMessage(" days ago (Violaion ID: ", false);
                cse.logPriorityAssignmentMessage(String.valueOf(earliestV.getViolationID()), false);
                cse.logPriorityAssignmentMessage(", DOR: ", true);
                cse.logPriorityAssignmentMessage(getPrettyDate(earliestV.getDateOfRecord()), false);
                cse.logPriorityAssignmentMessage(");", true);
                
                if(daysSinceEarliestActiveViolation <= profile.getPriorityParamLetterSendBufferDays()){
                    cse.logPriorityAssignmentMessage("Assigning  priority: ACTION REQUIRED (Yellow).", true);
                    cse.setPriority(PriorityEnum.ACTION_REQUIRED);
                } else {
                    cse.logPriorityAssignmentMessage("Assigning  priority: PAST DUE ACTION (Red).", true);
                    cse.setPriority(PriorityEnum.ACTION_PASTDUE);
                }
//                return cse;
            } // close if/then based on NOV list size
            
            // Check stip comp dates and see if we need to adjust priority as assigned by letters
            LocalDateTime earliestStipCompDate = violation_determineEarliestStipCompDate(codeViolActiveList);
            if(earliestStipCompDate != null){
                long daysToEarliestStipComp = DateTimeUtil.getTimePeriodAsDays(LocalDateTime.now(), earliestStipCompDate);
                
                cse.logPriorityAssignmentMessage("Determined earliest stipulated compliance date is ", false);
                cse.logPriorityAssignmentMessage(getPrettyDate(earliestStipCompDate), false);
                
                cse.logPriorityAssignmentMessage(", which is ", false);
                cse.logPriorityAssignmentMessage(String.valueOf(daysToEarliestStipComp), false);
                if(daysToEarliestStipComp >= 0){
                    cse.logPriorityAssignmentMessage(" days away;", true);
                    cse.logPriorityAssignmentMessage("Hence: we are inside a stip comp window;", true);
                    cse.logPriorityAssignmentMessage("Governing object in this muni is set to ", false);
                    if(profile.isPrioritizeLetterFollowUpBuffer()){
                        cse.logPriorityAssignmentMessage("Letters/NOVs;", true);
                        cse.logPriorityAssignmentMessage("Priority assigned by Letters/NOVs will not be altered", true);
                    } else {
                        cse.logPriorityAssignmentMessage("Stipulated compliance dates;", true);
                        cse.logPriorityAssignmentMessage("Letter/NOV-based priority assignment will be overriden;", true);
                        cse.logPriorityAssignmentMessage("Assigning override priority: MONITORING;", true);
                        cse.setPriority(PriorityEnum.MONITORING);
                        return cse;
                    }
                    
                  // okay, our stip comp date is in the past, so we need to check governing object and then check for buffers  
                  // we are either yellow or red, depending on the administrative buffer
                } else {
                    PriorityEnum priorityStipComp;
                    cse.logPriorityAssignmentMessage(" days ago;", true);
                    long stipCompPositivePast = -1l * daysToEarliestStipComp;
                    if(DateTimeUtil.getTimePeriodAsDays(earliestStipCompDate, LocalDateTime.now()) <= profile.getPriorityParamDeadlineAdministrativeBufferDays()){
                        cse.logPriorityAssignmentMessage("We are INSIDE administrative buffer following the most recent violation stipulated compliance date;", true);
                        cse.logPriorityAssignmentMessage("Priority as governed by stipulated compliance dates is ACTION REQUIRED/Yellow;", true);
                        priorityStipComp = PriorityEnum.ACTION_REQUIRED;
                        if(cse.getPriority() != null && cse.getPriority() != priorityStipComp){
                            cse.logPriorityAssignmentMessage("Priority conflict between letter/NOV rules and stipulated compliance date rules!;", true);
                            if(profile.isPrioritizeLetterFollowUpBuffer()){
                                cse.logPriorityAssignmentMessage("Priority assigned by letter/NOV rules will NOT be altered;", true);
                            } else {
                                cse.logPriorityAssignmentMessage("Priority assigned by letter/NOV rules will be overriden by stipulated compliance date;", true);
                                cse.logPriorityAssignmentMessage("Assignign Priority ACTION REQUIRED/Yellow;", true);
                                cse.setPriority(priorityStipComp);
                            }
                        } else {
                            cse.logPriorityAssignmentMessage("Priority agreement between letters/NOVs and stip comp date;", true);
                        }
                        
                    } else {
                        cse.logPriorityAssignmentMessage("We are OUTSIDE administrative buffer following the most recent violation stipulated compliance date;", true);
                        cse.logPriorityAssignmentMessage("Priority as governed by stipulated compliance dates is ACTION PAST DUE/Red;", true);
                        priorityStipComp = PriorityEnum.ACTION_PASTDUE;
                        if(cse.getPriority() != null && cse.getPriority() != priorityStipComp){
                            cse.logPriorityAssignmentMessage("Priority conflict between letter/NOV rules and stipulated compliance date rules!;", true);
                            if(profile.isPrioritizeLetterFollowUpBuffer()){
                                cse.logPriorityAssignmentMessage("Priority assigned by letter/NOV rules will NOT be altered;", true);
                            } else {
                                cse.logPriorityAssignmentMessage("Priority assigned by letter/NOV rules will be overriden by stipulated compliance date;", true);
                                cse.logPriorityAssignmentMessage("Assignign Priority ACTION PAST DUE/RED;", true);
                                cse.setPriority(priorityStipComp);
                            }
                        } else {
                            cse.logPriorityAssignmentMessage("Priority agreement between letters/NOVs and stip comp date;", true);
                        }
                    }
                }
            } else {
                cse.logPriorityAssignmentMessage("Violation status anomaly: Violation list is not null but no stip comp date could be determined;", true);
            }
        // we have no violations 
        } else {
            cse.logPriorityAssignmentMessage("Cannot determine priority;", true);
            cse.logPriorityAssignmentMessage("Assigning priority: UNKNOWN", true);
            cse.setPriority(PriorityEnum.UNKNOWN);
        }
        // If we didn't assign anything, then we don't know what to do.
        if(cse.getPriority() == null){
            cse.logPriorityAssignmentMessage("Cannot determine priority;", true);
            cse.logPriorityAssignmentMessage("Assigning priority: UNKNOWN", true);
            cse.setPriority(PriorityEnum.UNKNOWN);
        }
        
        // Now check for red or yellow or anbandoned stall and then see if we have a green buffer 
        // if so, take those steps
        if(profile.isPriorityAllowEventCategoryGreenBuffers()){
            cse.logPriorityAssignmentMessage("Muni profile has allowed use of event category green priority buffers...checking;", true);
            if(cse.getPriority() == PriorityEnum.ABANDONMENT_STALL 
                    || cse.getPriority() == PriorityEnum.ACTION_REQUIRED 
                    || cse.getPriority() == PriorityEnum.ACTION_PASTDUE){
                cse.logPriorityAssignmentMessage("Found assigned priority elegible for event category green buffer override;", true);
                if(cse.getEventList() != null && !cse.getEventList().isEmpty()){
                    for(EventCnF ev: cse.getEventList()){
                        // check for events in the pasts
                        if(ev.getTimeStart() != null && ev.getTimeStart().isBefore(LocalDateTime.now())){
                            if(ev.getCategory()!= null && ev.getCategory().getGreenBufferDays() != 0){
                                if(ev.getCategory().getGreenBufferDays() <= DateTimeUtil.getTimePeriodAsDays(ev.getTimeStart(), LocalDateTime.now())){
                                    cse.logPriorityAssignmentMessage("Found event (ID:", false);
                                    cse.logPriorityAssignmentMessage(String.valueOf(ev.getEventID()), false);
                                    cse.logPriorityAssignmentMessage(") with a green buffer of ", false);
                                    cse.logPriorityAssignmentMessage(String.valueOf(ev.getCategory().getGreenBufferDays()), false);
                                    cse.logPriorityAssignmentMessage("days, which has triggered a priority green override;", true);
                                    cse.logPriorityAssignmentMessage("Assigning Priority: GREEN OVERRIDE BUFFER", true);
                                    cse.setPriority(PriorityEnum.MON_OVERRIDE);
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    cse.logPriorityAssignmentMessage("No events were found to trigger a green override;", true);
                }
            } else {
                cse.logPriorityAssignmentMessage("Case priority is not eligible for green override;", true);
            }
        } else {
            cse.logPriorityAssignmentMessage("Muni profile has disallowed use of event category green priority buffers;", true);
        }
        
        
        return cse;
    }
    
    
    
    
    /**
     * A CECaseDataHeavy's Stage is derived from its Phase based on the set of
     * business rules encoded in this method.
     * @deprecated replaced in July 2022 with CasePriority
     * @param cse which needs its StageConfigured
     * @return the same CECas passed in with the CaseStageEnum configured
     * @throws BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    private CECase cecase_configureCECaseStageAndPhase(CECase cse) throws BObStatusException, IntegrationException {

        if (cse == null || cse.getCaseID() == 0) {
            throw new BObStatusException("cannot configure null case or one with ID 0");

        }

        // First determine case stage, then dig around as needed in the case
        // data to determine the appropriate phase
        // case stage basically emerges from violation status assessment
        SystemIntegrator si = getSystemIntegrator();
        CECaseStatus statusBundle = new CECaseStatus();

        if (cse.getPersonInfoPersonID() != 0 || cse.isPropertyInfoCase()) {
            statusBundle.setPhase(CasePhaseEnum.Container);
        } else if (cse.getClosingDate() != null) {
            statusBundle.setPhase(CasePhaseEnum.Closed);
            // jump right to court-based phase assignment if we have at least 1 elegible citation
        } else if (!cecase_buildCitationListForPhaseAssignment(cse).isEmpty()) {
            // Use catch all for citation at any stage of the process
            statusBundle.setPhase(CasePhaseEnum.Cited);
            // TODO: fix complex logic on citation phase stuff
//            statusBundle.setPhase(cecase_determineAndSetPhase_stageCITATION(cse));

        } else {
            // find overriding factors to have a closed 
            if (cse.getViolationList() != null && cse.getViolationList().isEmpty()) {
                // Open case, no violations yet: only one mapping
                statusBundle.setPhase(CasePhaseEnum.PrelimInvestigationPending);
                // we have at least one violation attached  
            } else {
                // If we don't have a mailed notice, then we're in Notice Delivery phase
                if (!cecase_determineIfNoticeHasBeenMailed(cse)) {
                    statusBundle.setPhase(CasePhaseEnum.IssueNotice);
                    // notice has been sent so we're in CaseStageEnum.Enforcement or beyond
                } else {
                    int maxVStage = violation_determineMaxViolationStatus(cse.getViolationList());
                    switch (maxVStage) {
                        case 0:  // all violations resolved
                            statusBundle.setPhase(CasePhaseEnum.FinalReview);
                            break;
                        case 1: // all violations within compliance window
                            statusBundle.setPhase(CasePhaseEnum.InsideComplianceWindow);
                            break;
                        case 2: // one or more EXPIRED compliance timeframes but no citations
                            statusBundle.setPhase(CasePhaseEnum.TimeframeExpiredNotCited);
                            break;
                        // we shouldn't hit this...
                        case 3: // at least 1 violation used in a citation that's attached to case
                            statusBundle.setPhase(cecase_determineAndSetPhase_stageCITATION(cse));
                            //                            cecase_determineAndSetPhase_stageCITATION(cse);
                            break;
                        default: // unintentional dumping ground 
                            statusBundle.setPhase(CasePhaseEnum.InactiveHolding);
                    } // close violation and citation block 
                } // close sent-notice-dependent block
            } // close violation-present-only block
        } // close non-container, non-closed block

        cse.setStatusBundle(statusBundle);

        if (cse.getStatusBundle() != null && cse.getStatusBundle().getPhase() != null) {
            //now set the icon based on what phase we just assigned the case to
            statusBundle.setPhaseIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                    .getString(cse.getStatusBundle().getPhase().getCaseStage().getIconPropertyLookup()))));
        }

        return cse;
    }

    /**
     * Utility method for culling out inactive citations, deactivated, etc.
     *
     * @param cse
     * @return
     */
    private List<Citation> cecase_buildCitationListForPhaseAssignment(CECase cse) {
        List<Citation> citList = new ArrayList<>();
        if (cse == null || cse.getCitationList() == null || cse.getCitationList().isEmpty()) {
            return citList;
        } else {
            for (Citation cit : cse.getCitationList()) {
                // TODO: fix me to work with docket status
                citList.add(cit);
            }
        }

        return citList;

    }

    /**
     * Logic container for case phase assignment for cases with at least 1
     * citation
     *
     * Assesses the list of events and citations on the case to determine the
     * appropriate post-hearing related case phase
     *
     * NOTE AS OF 29-NOV-2020: Let's just do: citation status, minus event
     * processing
     *
     * @param cse
     * @return
     */
    private CasePhaseEnum cecase_determineAndSetPhase_stageCITATION(CECase cse) {
        CasePhaseEnum courtPhase = null;

        int catIDCitationIssued = Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE).getString("court_citationissued"));
        int catIDHearingSched = Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE).getString("court_scheduledhearing"));
        int catIDHearingAttended = Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE).getString("court_hearingattended"));

        List<EventCnF> courtEvList = cecase_getEventListSubset(cse.getEventList(), EventType.Court);
        if (courtEvList == null || courtEvList.isEmpty()) {
            courtPhase = CasePhaseEnum.TimeframeExpiredNotCited;
            cse.logStatusNote("COURT: GAP IN EVENT RECORDING! No court events found, but We should have citation issued events; falling back to " + courtPhase.getLabel());
            return courtPhase;
        }

        // if we have a citation but no hearing scheduled, we're AwaitingAHearingDate
        LocalDateTime rightNow = LocalDateTime.now();
        LocalDateTime rightMostScheduledHearingDate = determineRightmostDateByEventCategoryID(courtEvList, catIDHearingSched);
        LocalDateTime rightMostHearingDate = determineRightmostDateByEventCategoryID(courtEvList, catIDHearingAttended);
        if (rightMostScheduledHearingDate == null) {
            courtPhase = CasePhaseEnum.AwaitingHearingDate;
            cse.logStatusNote("COURT: No hearing scheduled; assigned " + courtPhase.getLabel());
        } else {
            // if a hearing is scheduled in the future, we're HEARING PREP
            if (rightMostScheduledHearingDate.isAfter(rightNow)) {
                courtPhase = CasePhaseEnum.HearingPreparation;
                cse.logStatusNote("COURT: Found hearing schduled in the future; assigned " + courtPhase.getLabel());
                // if we've attended court and violations are still in the window, we're at InsideCourt....
            } else if (rightMostHearingDate.isBefore(rightNow)) {

                int maxVStage = violation_determineMaxViolationStatus(cse.getViolationList());
                switch (maxVStage) {
                    case 0:  // all violations resolved
                        courtPhase = CasePhaseEnum.Closed;
                        break;
                    case 1: // all violations within compliance window
                        courtPhase = CasePhaseEnum.InsideCourtOrderedComplianceTimeframe;
                        break;
                    case 2: // one or more EXPIRED compliance timeframes but no citations
                        courtPhase = CasePhaseEnum.CourtOrderedComplainceTimeframeExpired;
                        break;
                    // we shouldn't hit this...
                    default: // unintentional dumping ground 
                        courtPhase = CasePhaseEnum.CourtOrderedComplainceTimeframeExpired;
                } // close violation and citation block 
            }
        }
        return courtPhase;
    }

    /**
     * Utility method for extracting an event subset
     *
     * @param evList
     * @param et
     * @return
     */
    private List<EventCnF> cecase_getEventListSubset(List<EventCnF> evList, EventType et) {
        // get the events we want
        List<EventCnF> selectedEvents;
        selectedEvents = new ArrayList<>();
        if (evList != null && !evList.isEmpty()) {
            for (EventCnF ev : evList) {
                if (ev.getCategory().getEventType() == et) {
                    selectedEvents.add(ev);
                }
            }
        }

        return selectedEvents;
    }

    /**
     * Utility method for searching through a list of events and finding the
     * "latest in time" (i.e. right-most) event of a certain category
     *
     * @param evList complete event list to search
     * @param catID the category ID by which to create a comparison subset
     * @return if no event of this type is found, null is returned
     */
    private LocalDateTime determineRightmostDateByEventCategoryID(List<EventCnF> evList, int catID) {
        LocalDateTime highestDate = null;
        if (evList != null && !evList.isEmpty()) {
            for (EventCnF ev : evList) {
                if (ev.getCategory().getCategoryID() == catID && ev.getTimeStart() != null) {
                    if (highestDate == null) {
                        highestDate = ev.getTimeStart();
                    } else if (highestDate.isBefore(ev.getTimeStart())) {
                        highestDate = ev.getTimeStart();
                    }
                }
            }
        }

        return highestDate;
    }

    /**
     * TODO: Finish my guts
     *
     * @param cse
     * @return
     */
    public boolean cecase_determineIfNoticeHasBeenMailed(CECase cse) {
        return true;
    }

    /**
     * TODO: Old logic but good method sig. This method should get the
     * PropertyInfoCase associated with the Muni's property
     *
     * @param ua
     * @param profile necesary for case priority assignment
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     */
    public CECase cecase_selectDefaultCECase(UserAuthorized ua) throws IntegrationException, BObStatusException {
        PropertyCoordinator pc = getPropertyCoordinator();
        MunicipalityCoordinator mc = getMuniCoordinator();
        CECase cse = null;
        try {
            MunicipalityDataHeavy mdh = mc.assembleMuniDataHeavy(ua.getKeyCard().getGoverningAuthPeriod().getMuni(), ua);
            PropertyDataHeavy pdh = mdh.getMuniPropertyDH();
            if (pdh.getPropInfoCaseList() != null && !pdh.getPropInfoCaseList().isEmpty()) {
                cse = pdh.getPropInfoCaseList().get(0);
            }

        } catch (AuthorizationException | BObStatusException | EventException | IntegrationException | BlobException ex) {
            System.out.println("CaseCoordinator: Exception selecting default CECase from MuniPropDH; using arbitrary case");
        }
        if (cse != null) {
            return cse;
        }
        return cecase_getCECase(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("arbitraryPlaceholderCaseID")), ua);
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
    public List<CECase> cecase_getCECaseHistory(UserAuthorized ua) throws IntegrationException, BObStatusException {
        CaseIntegrator caseInt = getCaseIntegrator();
        List<CECase> cl = new ArrayList<>();
        if (ua != null) {
            List<Integer> cseidl = caseInt.getCECaseHistoryList(ua.getMyCredential().getGoverningAuthPeriod().getUserID());
            if (!cseidl.isEmpty()) {
                for (Integer i : cseidl) {
                    cl.add(cecase_getCECase(i, ua));
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
     * @throws com.tcvcog.tcvce.domain.IntegrationException If an error occurs
     * while generating a control code
     */
    public CECase cecase_initCECase(Property p, UserAuthorized ua) throws IntegrationException {
        CECase newCase = new CECase();

        // removed inputted muni here
        int casePCC = generateControlCodeFromTime(0);
        // caseID set by postgres sequence
        // timestamp set by postgres
        // no closing date, by design of case flow
        newCase.setPublicControlCode(casePCC);
        newCase.setParcelKey(p.getParcelKey());
        // TURN OFF MANAGER AUTO INSERT
        // APR 2022 to debug manager issues
//        if (ua.getKeyCard().getGoverningAuthPeriod().getOathTS() != null) {
//            newCase.setCaseManager(ua);
//        }
        newCase.setOriginationDate(LocalDateTime.now());

        return newCase;
    }

    /**
     * Logic intermediary for determining of a case can be deactivated
     *
     * @param cse
     * @throws IntegrationException
     * @throws BObStatusException
     */
    public void cecase_deactivateCase(CECaseDataHeavy cse) throws IntegrationException, BObStatusException {
        CaseIntegrator ci = getCaseIntegrator();
        if (cse != null) {
            if (!cse.isPropertyInfoCase()) {
                if (cse.getViolationList() == null || cse.getViolationList().isEmpty()) {
                    cse.setActive(false);
                    ci.updateCECaseMetadata(cse);
                } else {
                    throw new BObStatusException("Cannot deactivate a case with 1 or more violations attached");
                }
            }
        }
    }

    /**
     * Primary entry point for inserting new code enf cases. Two major pathways
     * exist through this method: - creating cases as a result of an action
     * request submission - creating cases from some other source than an action
     * request Depending on the source, an appropriately note-ified case
     * origination event is built and attached to the case that was just
     * created.
     *
     * @param inputCase
     * @param ua
     * @param origEventCat
     * @param cear
     * @param profile
     * @return the ID of the freshly inserted case
     * @throws IntegrationException
     * @throws BObStatusException
     * @throws ViolationException
     * @throws com.tcvcog.tcvce.domain.EventException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public int cecase_insertNewCECase(CECase inputCase,
            UserAuthorized ua,
            EventCategory origEventCat,
            CEActionRequest cear)
            throws  IntegrationException,
                    BObStatusException,
                    ViolationException,
                    EventException,
                    SearchException {

        CaseIntegrator ci = getCaseIntegrator();
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        EventCoordinator ec = getEventCoordinator();
        EventCategory originationCategory;
        EventCnF originationEvent;
        UserCoordinator uc = getUserCoordinator();

        if(inputCase == null){
            throw new BObStatusException("Cannot insert new case with null input case");
        }
        inputCase.setActive(true);
        inputCase.setLastUpdatedBy(ua);

        if (inputCase.getCaseManager() == null) {
            throw new BObStatusException("CeCase Must have a non-null manager");
            
        }

        cecase_auditCECase(inputCase, ua);

        // the integrator returns to us a CECaseDataHeavy with the correct ID after it has
        // been written into the DB
        int freshID = ci.insertNewCECase(inputCase);
        inputCase = cecase_getCECase(freshID, ua);
        CECaseDataHeavy cedh = cecase_assembleCECaseDataHeavy(inputCase, ua);

        // If we were passed in an action request, connect it to the new case we just made
        if (cear != null) {
            ceari.connectActionRequestToCECase(cear.getRequestID(), freshID, ua.getUserID());
            originationCategory = ec.initEventCategory(
                    Integer.parseInt(getResourceBundle(
                            Constants.EVENT_CATEGORY_BUNDLE).getString("originiationByActionRequest")));
            originationEvent = ec.initEvent(cedh, originationCategory);
            StringBuilder sb = new StringBuilder();
            originationEvent.setNotes(sb.toString());
        } else if (inputCase.isPropertyInfoCase()) {
            // This is a property info case, it originated to store info
            originationCategory = ec.initEventCategory(
                    Integer.parseInt(getResourceBundle(
                            Constants.EVENT_CATEGORY_BUNDLE).getString("originationAsPropertyInfoCase")));
            originationEvent = ec.initEvent(cedh, originationCategory);
            StringBuilder sb = new StringBuilder();
            sb.append("This case was created to contain information pertaining to a property");
        } else if (origEventCat != null) {
            originationCategory = origEventCat;
            originationEvent = ec.initEvent(cedh, originationCategory);

        } else {

            // since there's no action request, the assumed method is called "observation"
            originationCategory = ec.initEventCategory(
                    Integer.parseInt(getResourceBundle(
                            Constants.EVENT_CATEGORY_BUNDLE).getString("originiationByObservation")));
            originationEvent = ec.initEvent(cedh, originationCategory);
            StringBuilder sb = new StringBuilder();
            sb.append("Case opened directly on property by code officer assigned to this event");
//            originationEvent.setNotes(generateCaseInitNoteFromCEAR(cear));

        }
        originationEvent.setCreatedBy(uc.user_getUser(ua.getUserID()));
        originationEvent.setTimeStart(cedh.getOriginationDate());
        // logic in our event coordinator will set the end time based on the event cat
        ec.addEvent(originationEvent, cedh, ua);

        return freshID;
    }

    /**
     * Checks for violation status and closes case
     *
     * @param cse
     * @param ua
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void cecase_closeCase(CECase cse, UserAuthorized ua) throws BObStatusException, IntegrationException {
        CaseIntegrator ci = getCaseIntegrator();
        if (cse == null || ua == null) {
            throw new BObStatusException("Cannot close a case with null Case or User");
        }
        cse.setClosingDate(LocalDateTime.now());
        cse.setLastUpdatedBy(ua);

        ci.updateCECaseMetadata(cse);

    }
    
        /**
     * Utility for getting cecase list from a list of IDs
     * @param idl
     * @return
     * @throws IntegrationException
     */
    public List<CECase> getCaseListFromIDList(List<Integer> idl, UserAuthorized ua) throws IntegrationException{
        List<CECase> clist = new ArrayList<>();
        if(idl != null && !idl.isEmpty()){
            for(Integer i: idl){
                clist.add(cecase_getCECase(i, ua));
            }
        }
        return clist;
        
        
    }
    

    /**
     * Logic passthrough for parcel info cases
     * @param pcl
     * @return
     * @deprecated events connected directly to parcels instead as of JULY 2022
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public List<Integer> getPropertyInfoCaseIDList(Parcel pcl) throws IntegrationException, BObStatusException{
        CaseIntegrator ci = getCaseIntegrator();
        // Temporary work around--only return the first and most recent one
        List<Integer> infoCaseIDList = ci.getParcelInfoCaseIDList(pcl);
        List<Integer> abbreviatedList = new ArrayList<>();
        if(pcl == null){
            return abbreviatedList;
        }
        if(infoCaseIDList != null && !infoCaseIDList.isEmpty()){
            System.out.println("CaseCoordinator.getPropertyInfoCaseIDList | ParcelID : " + pcl.getParcelKey());
            System.out.println("CaseCoordinator.getPropertyInfoCaseIDList | Found raw list of size: " + infoCaseIDList.size());
            abbreviatedList.add(infoCaseIDList.get(0));
        }
        return abbreviatedList;
    }
    
    private String cecase_generateCaseInitNoteFromCEAR(CEActionRequest cear) {
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
        sb.append(DateTimeUtil.getPrettyDate(cear.getDateOfRecord()));
        sb.append(" with a database timestamp of ");
        sb.append(DateTimeUtil.getPrettyDate(cear.getSubmittedTimeStamp()));
        return sb.toString();
    }

  
    /**
     * Implements business logic before updating a CECaseDataHeavy's core data
     * (opening date, closing date, etc.). If all is well, pass to integrator.
     *
     * @param c the CECaseDataHeavy to be updated
     * @param ua the user doing the updating
     * @throws BObStatusException
     * @throws IntegrationException
     */
    public void cecase_updateCECaseMetadata(CECaseDataHeavy c, UserAuthorized ua) throws BObStatusException, IntegrationException {
        CaseIntegrator ci = getCaseIntegrator();
        if (ua == null || c == null) {
            throw new BObStatusException("Cannot update case with null case or user");
        }
        // the supplied user is the updator
        c.setLastUpdatedBy(ua);
        // Logic check for changes to the case closing date
        if (c.getClosingDate() != null) {
            if (c.getClosingDate().isBefore(c.getOriginationDate())) {
                throw new BObStatusException("You cannot update a case's origination date to be after its closing date");
            }
        }
        ci.updateCECaseMetadata(c);
    }
    
    /**
     * Changes a CeCase's origination event category 
     * @param cse
     * @param revisedEventCategory 
     * @param ua 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     * @throws com.tcvcog.tcvce.domain.EventException 
     */
    public void cecase_checkForAndUpdateCaseOriginationEventCategory(CECaseDataHeavy cse, EventCategory revisedEventCategory, UserAuthorized ua) 
            throws IntegrationException, EventException, BObStatusException{
        EventCoordinator ec = getEventCoordinator();
        if(revisedEventCategory != null && cse.getOriginationEvent() != null){
            // If we have an update, do the db write
            if(revisedEventCategory.getCategoryID() != cse.getOriginationEvent().getCategory().getCategoryID() ){
                System.out.println("CaseCoordinator.cecase_checkForAndUpdateCaseOriginationEventCategory");
                ec.updateEventCategoryMaintainType(cse.getOriginationEvent(), revisedEventCategory, ua);
                cecase_originationEventCategoryChangeNoteComposeAndWrite(cse, revisedEventCategory, ua);
            }
        }
    }
    
    /**
     * Writes and inserts a note about an origination event category update
     * @param cse
     * @param revisedEventCategory
     * @param ua
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    private void cecase_originationEventCategoryChangeNoteComposeAndWrite(CECaseDataHeavy cse, EventCategory revisedEventCategory, UserAuthorized ua) throws BObStatusException, IntegrationException{
                StringBuilder sb = new StringBuilder();
                sb.append("Case origination event category changed from ");
                sb.append(cse.getOriginationEvent().getCategory().getEventCategoryTitle());
                sb.append("(Category ID:");
                sb.append(cse.getOriginationEvent().getCategory().getCategoryID());
                sb.append(") to  ");
                sb.append(revisedEventCategory.getEventCategoryTitle());
                sb.append("(Category ID:");
                sb.append(revisedEventCategory.getCategoryID());
                sb.append(").");
                MessageBuilderParams mbp = new MessageBuilderParams(cse.getNotes(), "Origination event category change", null, sb.toString(), ua, null);
                cecase_updateCECaseNotes(mbp, cse);
    }

    /**
     * Updates only the notes field on CECase
     *
     * @param mbp
     * @param c the CECaseDataHeavy to be updated
     * @throws BObStatusException
     * @throws IntegrationException
     */
    public void cecase_updateCECaseNotes(MessageBuilderParams mbp, CECase c) throws BObStatusException, IntegrationException {
        CaseIntegrator ci = getCaseIntegrator();
        SystemCoordinator sc = getSystemCoordinator();
        if (c == null || mbp == null) {
            throw new BObStatusException("Cannot append if notes, case, or user are null");
        }

        c.setNotes(sc.appendNoteBlock(mbp));
        c.setLastUpdatedBy(mbp.getUser());
        ci.updateCECaseNotes(c);
    }

    // *************************************************************************
    // *                     REPORTING                                         *
    // *************************************************************************
    /**
     * Factory method for creating new reports listing action requests
     *
     * @param u
     * @param m
     * @return
     */
    public ReportCEARList report_getInitializedReportConficCEARs(User u, Municipality m) {
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
     *
     * @param c
     * @return
     */
    public ReportConfigCECase report_getDefaultReportConfigCECase(CECaseDataHeavy c) {
        ReportConfigCECase rpt = new ReportConfigCECase();

        rpt.setMaximumOutputForUserRank(false);
        
        // general
        rpt.setIncludeCaseName(true);

        // events
        rpt.setIncludeOfficerOnlyEvents(true);
        rpt.setIncludeMunicipalityDiclosedEvents(true);
        rpt.setIncludeHiddenEvents(false);
        rpt.setIncludeEventNotes(true);
        rpt.setIncludeInactiveEvents(false);
        rpt.setIncludeEventMetadata(true); 
        rpt.setIncludeEventPersonLinks(true);
        
        // notices of violation
        rpt.setIncludeNoticeFullText(true);
        rpt.setIncludeAllNotices(true);

        // CEARS and $$
        rpt.setIncludeActionRequsts(true);
        rpt.setIncludeActionRequestPhotos(true);
        rpt.setCearColumnCount(2);
        rpt.setIncludeFeeAndPaymentInfo(true);
        
        // FIRs
        rpt.setIncludeFieldInspectionReports(true);
        rpt.setIncludeFieldInspectionReportsWithPhotos(true);
        
        // violations
        rpt.setIncludeFullOrdinanceText(true);
        rpt.setIncludeViolationPhotos(true);
        rpt.setViolationPhotoColumnCount(2);
        rpt.setIncludeViolationNotes(true);
        
        // photos and blobs
        rpt.setIncludePropertyBroadviewPhoto(true);
        rpt.setIncludeCECasePhotoPool(true);
        rpt.setIncludeDocDownloadLinks(true);
        rpt.setCeCasePhotoPoolColumnCount(2);
        
        //perons
        rpt.setIncludeAssociatedPersons(true);
        rpt.setIncludeAssociatedPersonsOnParentProperty(true);
        rpt.setIncludePersonAddrPhoneEmail(true);

        return rpt;
    }

    /**
     * Factory method for reports on a list of cases
     *
     * @return
     */
    public ReportConfigCECaseList report_getDefaultReportConfigCECaseList() {
        ReportConfigCECaseList rptList = new ReportConfigCECaseList();
       rptList.setIncludeSectionReportSummary(true);
        rptList.setIncludeSectionReportSummary_openAsOfSOR(true);
        rptList.setIncludeSectionReportSummary_openAsOfEOR(true);
        rptList.setIncludeSectionReportSummary_casesOpened(true);
        rptList.setIncludeSectionReportSummary_casesClosed(true);
        rptList.setIncludeSectionReportSummary_newViolations(true);
        rptList.setIncludeSectionReportSummary_compViol(true);
        rptList.setIncludeSectionReportSummary_eventsLogged(true);
        rptList.setIncludeSectionReportSummary_eventsTotalTime(true);
        rptList.setIncludeSectionReportSummary_closurePieChart(true);
        rptList.setIncludeSectionReportSummary_ceCaseStatusPie(true);
        rptList.setIncludeSectionCodeViolationStatus(true);
        rptList.setIncludeSectionCodeViolationStatus_compliance(true);
        rptList.setIncludeSectionCodeViolationStatus_withinWindow(true);
        rptList.setIncludeSectionCodeViolationStatus_expiredWindow(true);
        rptList.setIncludeSectionCodeViolationStatus_cited(true);
        rptList.setIncludeSectionCitations(true);
        rptList.setIncludeSectionCitations_citationsAnyStage(true);
        rptList.setIncludeSectionCitations_pieChart(true);
        rptList.setIncludeSectionStreetLevelSummary(true);
        rptList.setIncludeSectionCaseByCaseDetail(true);
        rptList.setIncludeSectionCaseByCaseDetail_fullOwnerContactInfo(true);
        rptList.setIncludeSectionCaseByCaseDetail_caseNames(true);
        rptList.setIncludeSectionCaseByCaseDetail_violationList(true);
        rptList.setIncludeSectionCaseByCaseDetail_extendedPropertyDetails(true);
        rptList.setIncludeSectionCaseByCaseDetail_eventSummary(true);

        
        return rptList;

    }

    /**
     * Prepares a report of municipal case activity for a given time period
     *
     * @param rpt the report config object with dates set for searching
     * @param ua the user requesting the report
     * @return the configured report
     * @throws SearchException
     */
    public ReportConfigCECaseList report_buildCECaseListReport(ReportConfigCECaseList rpt, UserAuthorized ua) throws SearchException {
        SearchCoordinator sc = getSearchCoordinator();
        if (rpt == null || ua == null) {
            return null;
        }
        SearchParamsCECase spcse;
          
        // Phase 1: Cases open as of report START date 
        // Akin to "beginning inventory"
        QueryCECase queryOpenAsOfSOR = sc.initQuery(QueryCECaseEnum.OPEN_ASOFGIVENDATE, ua.getKeyCard());
        // query audit setup
        if (queryOpenAsOfSOR.getParamsList() != null && queryOpenAsOfSOR.getParamsList().size() == 2) {
            // setup params
            spcse = queryOpenAsOfSOR.getPrimaryParams();

            // client field injection of END date value: cases open as of START 
            if (spcse.getDateRuleList() != null && spcse.getDateRuleList().size() == 2) {
                // COORDINATOR injects date field SearchParamsCECaseDateFieldsEnum.ORIGINATION_DOFRECORD
                // Corodinator sets date start val to JAN 1 1970
                spcse.getDateRuleList().get(0).setDate_end_val(rpt.getDate_start_val());
                spcse.getDateRuleList().get(1).setDate_start_val(rpt.getDate_start_val().plusDays(1));
                // search coord injected now() as the end date
            }

            // client field injection: cases open as of EOR and not closed, even up to today
            if (queryOpenAsOfSOR.getParamsList() != null && !queryOpenAsOfSOR.getParamsList().isEmpty()) {
                SearchParamsCECase sponc = queryOpenAsOfSOR.getParamsList().get(1);
                sponc.getDateRuleList().get(0).setDate_end_val(rpt.getDate_start_val());
            }
        }

        List<CECase> casesOpenAsOfSOR = sc.runQuery(queryOpenAsOfSOR, ua).getBOBResultList();
        System.out.println("Query: " + queryOpenAsOfSOR.getQueryLog());

        if (casesOpenAsOfSOR != null && !casesOpenAsOfSOR.isEmpty()) {
            System.out.println("CaseCoordinator.assembleCECaseListReport: OPEN AS OF START OR REPORT");
            for (CECase cse : casesOpenAsOfSOR) {
                System.out.print("CID: " + cse.getCaseID());
                System.out.println("| name: " + cse.getCaseName());
            }
        }

        rpt.setCaseListOpenAsOfDateStart(cecase_assembleCECaseDataHeavyList(casesOpenAsOfSOR, ua));
        
        // Phase 2: Cases opened during reporting period
        QueryCECase query_opened = sc.initQuery(QueryCECaseEnum.OPENED_INDATERANGE, ua.getKeyCard());
        spcse = query_opened.getPrimaryParams();
        spcse.setDate_startEnd_ctl(true);
        spcse.setDate_field(SearchParamsCECaseDateFieldsEnum.ORIGINATION_DOFRECORD);
        spcse.setDate_start_val(rpt.getDate_start_val());
        spcse.setDate_end_val(rpt.getDate_end_val());
        rpt.setCaseListOpenedInDateRange(cecase_assembleCECaseDataHeavyList(sc.runQuery(query_opened, ua).getBOBResultList(), ua));

        if (rpt.getCaseListOpenedInDateRange() != null) {
            System.out.println("CaseCoordinator.report_buildCECaseListReport: Opened List size " + rpt.getCaseListOpenedInDateRange().size());
        }
        
        // Phase 3: Cases closed during reporting period
        QueryCECase query_closed = sc.initQuery(QueryCECaseEnum.CLOSED_CASES, ua.getKeyCard());
        spcse = query_closed.getPrimaryParams();
        spcse.setDate_field(SearchParamsCECaseDateFieldsEnum.CLOSE);
        spcse.setDate_startEnd_ctl(true);
        spcse.setDate_start_val(rpt.getDate_start_val());
        spcse.setDate_end_val(rpt.getDate_end_val());
        rpt.setCaseListClosedInDateRange(cecase_assembleCECaseDataHeavyList(sc.runQuery(query_closed, ua).getBOBResultList(), ua));
        if (rpt.getCaseListClosedInDateRange() != null) {
            System.out.println("CaseCoordinator.report_buildCECaseListReport: Current List size " + rpt.getCaseListClosedInDateRange().size());
        }

        // Phase 4: Cases open as of end of reporting period
        // AKIN to "Ending Inventory"
        QueryCECase queryOpenedAsOfEndDate = sc.initQuery(QueryCECaseEnum.OPEN_ASOFENDDATE, ua.getKeyCard());

        if (queryOpenedAsOfEndDate.getParamsList() != null && queryOpenedAsOfEndDate.getParamsList().size() == 2) {

            // setup params
            spcse = queryOpenedAsOfEndDate.getPrimaryParams();

            // client field injection: cases open as of EOR but closed between EOR+1 and now()
            // meaning they were still open as of EOR
            if (spcse.getDateRuleList() != null && spcse.getDateRuleList().size() == 2) {
                spcse.getDateRuleList().get(0).setDate_end_val(rpt.getDate_end_val());
                // we have to make this adjustment since we still need to think of a case as open
                // if it has since closed and therefore has a NOT NULL closing date
                // The costs of getting a computer to think it's not now()
                spcse.getDateRuleList().get(1).setDate_start_val(rpt.getDate_end_val().plusDays(1));
                System.out.println("CaseCoordinator.report_buildCECaseListReport " + spcse.getDateRuleList());
            }

            // client field injection: cases open as of EOR and not closed, even today
            if (queryOpenedAsOfEndDate.getParamsList() != null && !queryOpenedAsOfEndDate.getParamsList().isEmpty()) {
                SearchParamsCECase sponc = queryOpenedAsOfEndDate.getParamsList().get(1);
                sponc.getDateRuleList().get(0).setDate_end_val(rpt.getDate_end_val());
                System.out.println("CaseCoordinator.report_buildCECaseListReport " + sponc.getDateRuleList());
            }
        }

        List<CECase> casesOpenAsOfEOR = sc.runQuery(queryOpenedAsOfEndDate, ua).getBOBResultList();
        System.out.println("Query: " + queryOpenedAsOfEndDate.getQueryLog());

        if (casesOpenAsOfEOR != null && !casesOpenAsOfEOR.isEmpty()) {
            System.out.println("CaseCoordinator.assembleCECaseListReport: OPEN AS OF END OF REPORT CASE LIST");
            for (CECase cse : casesOpenAsOfEOR) {
                System.out.print("CID: " + cse.getCaseID());
                System.out.println("| name: " + cse.getCaseName());
            }
        }

        rpt.setCaseListOpenAsOfDateEnd(cecase_assembleCECaseDataHeavyList(casesOpenAsOfEOR, ua));
        
      
        
      // AVERAGE CASE AGE  
        if (rpt.getCaseListOpenAsOfDateEnd() != null && !rpt.getCaseListOpenAsOfDateEnd().isEmpty()) {
            System.out.println("CaseCoordinator.report_buildCECaseListReport: Opened List size " + rpt.getCaseListOpenAsOfDateEnd().size());
            // compute average case age for opened as of date list
            long sumOfAges = 0l;
            for (CECase cse : rpt.getCaseListOpenAsOfDateEnd()) {
                sumOfAges += cse.getCaseAgeAsOf(rpt.getDate_end_val());
            }
            double avg = (double) sumOfAges / (double) rpt.getCaseListOpenAsOfDateEnd().size();
            DecimalFormat df = new DecimalFormat("###.#");
            rpt.setAverageAgeOfCasesOpenAsOfReportEndDate(df.format(avg));
        }

      

        // BUILD BY STREET
        rpt = report_ceCaseList_organizeByStreet(rpt);

        // EVENTS
        QueryEvent query_ev = sc.initQuery(QueryEventEnum.MUNI_MONTHYACTIVITY, ua.getKeyCard());
        SearchParamsEvent spev = query_ev.getPrimaryParams();
        spev.setDate_startEnd_ctl(true);
        spev.setDate_start_val(rpt.getDate_start_val());
        spev.setDate_end_val(rpt.getDate_end_val());
        rpt.setEventList(sc.runQuery(query_ev, ua).getBOBResultList());

        // VIOLATIONS
        
        
        QueryCodeViolation qcodeVDORInPeriod = sc.initQuery(QueryCodeViolationEnum.LOGGED_IN_DATE_RANGE, ua.getKeyCard());
        SearchParamsCodeViolation params = qcodeVDORInPeriod.getPrimaryParams();
        params.setDate_relativeDates_ctl(false);
        params.setDate_start_val(rpt.getDate_start_val());
        params.setDate_end_val(rpt.getDate_end_val());
        rpt.setViolationsLoggedDateRange(sc.runQuery(qcodeVDORInPeriod).getResults());
        
        System.out.println("CaseCoordinator.report_buildCECaseListReport: Violation DOR in range query log" + qcodeVDORInPeriod.getQueryLog());
        
        
        QueryCodeViolation qcv = sc.initQuery(QueryCodeViolationEnum.COMP_PAST30, ua.getKeyCard());
        params = qcv.getPrimaryParams();
        params.setDate_relativeDates_ctl(false);
        params.setDate_start_val(rpt.getDate_start_val().minusYears(ARBITRARILY_LONG_TIME_YEARS));
        params.setDate_end_val(rpt.getDate_end_val());
        rpt.setViolationsAccumulatedCompliance(sc.runQuery(qcv).getResults());
        
        qcv = sc.initQuery(QueryCodeViolationEnum.COMP_PAST30, ua.getKeyCard());
        params = qcv.getPrimaryParams();
        params.setDate_relativeDates_ctl(false);
        params.setDate_start_val(rpt.getDate_start_val());
        params.setDate_end_val(rpt.getDate_end_val());
        rpt.setViolationsLoggedComplianceDateRange(sc.runQuery(qcv).getResults());
        
        
        qcv = sc.initQuery(QueryCodeViolationEnum.VIOLATIONS_INSIDE_COMPLIANCE_WINDOW, ua.getKeyCard());
        params = qcv.getPrimaryParams();
        params.setDate_relativeDates_ctl(false);
        // a violation that's in the compliance window as of END of report, 
        // will only be grabbed by asking if the the stip comp date is AFTER
        // the end of the report, and into the future some arbitrary number of days
        // That's why we're injecting the END date of the report as the START
        // date of our violation stip comp date query
        params.setDate_start_val(rpt.getDate_end_val());
        params.setDate_end_val(rpt.getDate_end_val().plusYears(ARBITRARILY_LONG_TIME_YEARS)); // arbitrary magic number of years into the future (thanks WWALK)
        rpt.setViolationsWithinStipCompWindow(sc.runQuery(qcv).getResults());
        
        qcv = sc.initQuery(QueryCodeViolationEnum.COMPLIANCE_DUE_EXPIRED_NOTCITED, ua.getKeyCard());
        params = qcv.getPrimaryParams();
        params.setDate_relativeDates_ctl(false);
        // since the violation is still active, we'll get expired ones
        // by capturing only the ones whose stip comp date is anytime in the past up to the report end date 
        params.setDate_start_val(rpt.getDate_end_val().minusYears(ARBITRARILY_LONG_TIME_YEARS));
        params.setDate_end_val(rpt.getDate_end_val()); 
        rpt.setViolationsOutsideCompWindowNOTCited(sc.runQuery(qcv).getResults());
        
        qcv = sc.initQuery(QueryCodeViolationEnum.NOCOMP_CITED_ANYTIME, ua.getKeyCard());
        params = qcv.getPrimaryParams();
        params.setDate_relativeDates_ctl(false);
        
        
        // this is an accumulated figure whose only bound is the repot end date
        params.setDate_start_val(rpt.getDate_end_val().minusYears(ARBITRARILY_LONG_TIME_YEARS));
        params.setDate_end_val(rpt.getDate_end_val());
        rpt.setViolationsNoComplianceButCited(sc.runQuery(qcv).getResults());
//        
//        initBarViolationsPastMonth(rpt);
//        initPieEnforcement(rpt);
        
        // deprecated as of AUG 2022 to use discrete stats
        // initPieViols(rpt);
        
        initPieCitation(rpt);
        initPieClosure(rpt);

        return rpt;

    }

    /**
     * Internal logic for taking apart case lists and turning them into
     * street-categorized maps
     *
     * @param rptCseList
     * @return the inputted report config object with its street container list
     * assembled
     */
    private ReportConfigCECaseList report_ceCaseList_organizeByStreet(ReportConfigCECaseList rptCseList) {

        if (rptCseList != null) {
            Map<String, ReportCECaseListStreetCECaseContainer> streetCaseMap = new HashMap<>();
            List<ReportCECaseListCatEnum> reportListsList = Arrays.asList(ReportCECaseListCatEnum.values());
            for (ReportCECaseListCatEnum enm : reportListsList) {
                List<CECaseDataHeavy> cseList = null;
                switch (enm) {
                    case CLOSED:
                        cseList = rptCseList.getCaseListClosedInDateRange();
                        break;
                    case CONTINUING:
                        cseList = rptCseList.getCaseListOpenAsOfDateEnd();
                        break;
                    case OPENED:
                        cseList = rptCseList.getCaseListOpenedInDateRange();
                        break;
                }

                if (cseList != null && !cseList.isEmpty()) {
                    ReportCECaseListStreetCECaseContainer streetCaseContainer = null;

                    for (CECaseDataHeavy cse : cseList) {
                        // check for an address, and create a generic one if there isn't one
                        if (cse.getProperty().getPrimaryAddressLink() == null) {
                            PropertyCoordinator pc = getPropertyCoordinator();
                            MailingAddressLink madLink = pc.getMailingAddressLinkSkeleton(pc.getMailingAddressSkeleton());
                            madLink.getStreet().setName(ADDRESSLESS_PROPERTY_STREET_NAME);
                            List<MailingAddressLink> madLinkList = new ArrayList<>();
                            madLinkList.add(madLink);
                            cse.getProperty().setMailingAddressLinkList(madLinkList);
                        }
                        // start with streets we don't currently have a case list for
                        if (!streetCaseMap.containsKey(cse.getProperty().getPrimaryAddressLink().getStreet().getName())) {
                            streetCaseContainer = new ReportCECaseListStreetCECaseContainer();
                            streetCaseContainer.setStreetName(cse.getProperty().getPrimaryAddressLink().getStreet().getName());
                            switch (enm) {
                                case CLOSED:
                                    streetCaseContainer.getCaseClosedList().add(cse);
                                    break;
                                case CONTINUING:
                                    streetCaseContainer.getCaseContinuingList().add(cse);
                                    break;
                                case OPENED:
                                    streetCaseContainer.getCaseOpenedList().add(cse);
                                    break;
                            } // close switch
                            // process cases on streets already in our mapping
                        } else {
                            streetCaseContainer = streetCaseMap.get(cse.getProperty().getPrimaryAddressLink().getStreet().getName());
                            switch (enm) {
                                case CLOSED:
                                    streetCaseContainer.getCaseClosedList().add(cse);
                                    break;
                                case CONTINUING:
                                    streetCaseContainer.getCaseContinuingList().add(cse);
                                    break;
                                case OPENED:
                                    streetCaseContainer.getCaseOpenedList().add(cse);
                                    break;
                            } // close switch
                        } // close if/else
                        // Write new or updated Street Case continaer to our map
                        streetCaseMap.put(cse.getProperty().getPrimaryAddressLink().getStreet().getName(), streetCaseContainer);
                    } // close for over case list
                } // close null/emtpy check
            } // close for over enum vals

            report_ceCaseList_removeDupsByStreet(streetCaseMap);
            rptCseList.setStreetSCC(streetCaseMap);
            rptCseList.assembleStreetList();

        } // close param null check

        return rptCseList;

    }

    /**
     * Removes cases which were opened and closed in the same period and leaves
     * them in the Closed group
     *
     * @param rpt
     */
    private void report_ceCaseList_removeDupsByStreet(Map<String, ReportCECaseListStreetCECaseContainer> streetCaseMap) {
        for (String rclsc : streetCaseMap.keySet()) {
            ReportCECaseListStreetCECaseContainer cont = streetCaseMap.get(rclsc);
            for (CECaseDataHeavy cse : cont.getCaseOpenedList()) {
                if (cont.getCaseClosedList().contains(cse)) {
                    System.out.println("CaseCoordinator.removeDupes: removing case ID " + cse.getCaseID());
                    cont.getCaseClosedList().remove(cse);
                }
            }
        }
    }

    /**
     * Builds a violation pie chart
     *
     * @deprecated  in favor of discrete statistics since this pie 
     * was asserted to be misleading to report readers
     * @param rpt
     */
    private void initPieViols(ReportConfigCECaseList rpt) {
        CaseCoordinator cc = getCaseCoordinator();
        rpt.setPieViol(new PieChartModel());
        ChartData pieData = new ChartData();

        PieChartDataSet dataSet = new PieChartDataSet();
        List<Number> violationCounts = new ArrayList<>();

        List<CodeViolation> vl = new ArrayList<>();
        for (CECaseDataHeavy cdh : rpt.assembleFullCaseLiset()) {
            vl.addAll(cdh.assembleViolationList(ViewOptionsActiveListsEnum.VIEW_ACTIVE));
        }

        // build our count map
        Map<CodeViolationStatusEnum, Integer> violMap = new HashMap<>();
        violMap.put(CodeViolationStatusEnum.UNRESOLVED_WITHINCOMPTIMEFRAME, 0);
        violMap.put(CodeViolationStatusEnum.UNRESOLVED_EXPIREDCOMPLIANCETIMEFRAME, 0);
        violMap.put(CodeViolationStatusEnum.RESOLVED, 0);
        violMap.put(CodeViolationStatusEnum.UNRESOLVED_CITED, 0);
        violMap.put(CodeViolationStatusEnum.NULLIFIED, 0);
        violMap.put(CodeViolationStatusEnum.TRANSFERRED, 0);
        

        for (CodeViolation cv : vl) {
            CodeViolationStatusEnum vs = cv.getStatus();
            Integer catCount = violMap.get(vs);
            catCount += 1;
            violMap.put(cv.getStatus(), catCount);
        }

        rpt.setPieViolStatMap(violMap);
        rpt.setPieViolCompCount(violMap.get(CodeViolationStatusEnum.RESOLVED));
        Set<CodeViolationStatusEnum> keySet = violMap.keySet();
        List<String> pieColors = new ArrayList<>();
        List<LegendItem> legend = new ArrayList<>();

        SystemCoordinator sc = getSystemCoordinator();

        for (CodeViolationStatusEnum vse : keySet) {
            Integer i = violMap.get(vse);
            violationCounts.add(i);
            String color = sc.generateRandomRGBColor();
            pieColors.add(color);
            LegendItem li = new LegendItem();
            li.setColorRGBString(color);
            li.setValue(i);
            li.setTitle(vse.getLabel());
            legend.add(li);
        }

        rpt.setPieViolLegend(legend);
        dataSet.setData(violationCounts);
        dataSet.setBackgroundColor(pieColors);
        pieData.addChartDataSet(dataSet);
        rpt.getPieViol().setData(pieData);
    }

    /**
     * Builds a pie chart about case enforcement status
     *
     * @param rpt
     * @deprecated use priority instead
     */
    private void initPieEnforcement(ReportConfigCECaseList rpt) {

        if (rpt != null) {

            rpt.setPieEnforcement(new PieChartModel());
            ChartData pieData = new ChartData();

            PieChartDataSet dataSet = new PieChartDataSet();
            List<Number> enfStatusNumList = new ArrayList<>();

            Map<String, Integer> statMap = new HashMap<>();

            statMap.put(CaseStageEnum.Investigation.getLabel(), 0);
            statMap.put(CasePhaseEnum.InsideComplianceWindow.getLabel(), 0);
            statMap.put(CasePhaseEnum.TimeframeExpiredNotCited.getLabel(), 0);
            statMap.put(CaseStageEnum.Citation.getLabel(), 0);

            for (CECaseDataHeavy cse : rpt.assembleNonClosedCaseList()) {
                if (cse.getStatusBundle().getPhase().getStage() == CaseStageEnum.Investigation) {
                    Integer statCount = statMap.get(CaseStageEnum.Investigation.getLabel());
                    statCount += 1;
                    statMap.put(CaseStageEnum.Investigation.getLabel(), statCount);

                }
                if (cse.getStatusBundle().getPhase().getCaseStage() == CaseStageEnum.Enforcement) {
                    if (cse.getStatusBundle().getPhase() == CasePhaseEnum.InsideComplianceWindow) {
                        Integer statCount = statMap.get(CasePhaseEnum.InsideComplianceWindow.getLabel());
                        statCount += 1;
                        statMap.put(CasePhaseEnum.InsideComplianceWindow.getLabel(), statCount);
                    }
                    if (cse.getStatusBundle().getPhase() == CasePhaseEnum.TimeframeExpiredNotCited) {
                        Integer statCount = statMap.get(CasePhaseEnum.TimeframeExpiredNotCited.getLabel());
                        statCount += 1;
                        statMap.put(CasePhaseEnum.TimeframeExpiredNotCited.getLabel(), statCount);
                    }
                }

                if (cse.getStatusBundle().getPhase().getStage() == CaseStageEnum.Citation) {
                    Integer statCount = statMap.get(CaseStageEnum.Citation.getLabel());
                    statCount += 1;
                    statMap.put(CaseStageEnum.Citation.getLabel(), statCount);
                }
            }

            Set<String> keySet = statMap.keySet();

            List<String> pieColors = new ArrayList<>();
            List<LegendItem> legend = new ArrayList<>();

            SystemCoordinator sc = getSystemCoordinator();

            for (String k : keySet) {
                Integer i = statMap.get(k);
                enfStatusNumList.add(i);
                String color = sc.generateRandomRGBColor();
                pieColors.add(color);
                LegendItem li = new LegendItem();
                li.setColorRGBString(color);
                li.setValue(i);
                li.setTitle(k);
                legend.add(li);

            }
            rpt.setPieEnforcementLegend(legend);

            dataSet.setBackgroundColor(pieColors);
            dataSet.setData(enfStatusNumList);
            pieData.addChartDataSet(dataSet);

            rpt.getPieEnforcement().setData(pieData);
        }
    }

    /**
     * Builds a pie chart about citations
     *
     * @param rpt
     */
    private void initPieCitation(ReportConfigCECaseList rpt) {
        if (rpt != null) {

            rpt.setPieCitation(new PieChartModel());
            ChartData pieData = new ChartData();

            PieChartDataSet dataSet = new PieChartDataSet();
            List<Number> statusCounts = new ArrayList<>();

            List<CECaseDataHeavy> nccl = rpt.assembleNonClosedCaseList();
            List<Citation> citl = new ArrayList<>();
            for (CECaseDataHeavy cse : nccl) {
                citl.addAll(cse.getCitationList());
            }

            rpt.setCitationList(citl);

            Map<String, Integer> citStatusMap = new HashMap<>();

            // COUNT BY Citation status
            if (!citl.isEmpty()) {
                for (Citation cit : citl) {
                    if (cit != null && cit.getMostRecentStatusLogEntry() != null && cit.getMostRecentStatusLogEntry().getStatus() != null) {
                        String statTitle = cit.getMostRecentStatusLogEntry().getStatus().getStatusTitle();
                        if (citStatusMap.containsKey(statTitle)) {
                            Integer statCount = citStatusMap.get(statTitle) + 1;
                            citStatusMap.put(statTitle, statCount);
                        } else {
                            citStatusMap.put(statTitle, 1);
                        }
                    }
                }
            }

            Set<String> keySet = citStatusMap.keySet();

            List<String> pieColors = new ArrayList<>();
            List<LegendItem> legend = new ArrayList<>();

            SystemCoordinator sc = getSystemCoordinator();

            for (String k : keySet) {
                Integer i = citStatusMap.get(k);
                statusCounts.add(i);
                String color = sc.generateRandomRGBColor();
                pieColors.add(color);
                LegendItem li = new LegendItem();
                li.setColorRGBString(color);
                li.setValue(i);
                li.setTitle(k);
                legend.add(li);

            }
            rpt.setPieCitationLegend(legend);
            dataSet.setData(statusCounts);
            dataSet.setBackgroundColor(pieColors);
            pieData.addChartDataSet(dataSet);
            rpt.getPieCitation().setData(pieData);
        }
    }

    /**
     * Builds a pie chart about case closures
     *
     * @param rpt
     */
    private void initPieClosure(ReportConfigCECaseList rpt) {
        rpt.setPieClosure(new PieChartModel());
        ChartData pieData = new ChartData();

        PieChartDataSet dataSet = new PieChartDataSet();
        List<Number> closingEventCountList = new ArrayList<>();

        List<EventCnF> evList = new ArrayList<>();

        // GET ONLY CLOSING EVENTS
        if (rpt.getCaseListClosedInDateRange() != null && !rpt.getCaseListClosedInDateRange().isEmpty()) {
            for (CECaseDataHeavy cse : rpt.getCaseListClosedInDateRange()) {
                List<EventCnF> evl = cse.getEventList(ViewOptionsActiveHiddenListsEnum.VIEW_ALL);
                if (!evl.isEmpty()) {
                    for (EventCnF ev : evl) {
                        if (ev.getCategory().getEventType() == EventType.Closing) {
                            evList.add(ev);
                            break; // only add the first closing event encountered
                        }
                    }
                }
            }
        }

        Map<String, Integer> closeEventMap = new HashMap<>();

        // COUNT BY CLOSE EVENT CATEGORY TITLE
        if (!evList.isEmpty()) {
            for (EventCnF ce : evList) {
                if (closeEventMap.containsKey(ce.getCategory().getEventCategoryTitle())) {
                    Integer catCount = closeEventMap.get(ce.getCategory().getEventCategoryTitle()) + 1;
                    closeEventMap.put(ce.getCategory().getEventCategoryTitle(), catCount);
                } else {
                    closeEventMap.put(ce.getCategory().getEventCategoryTitle(), 1);
                }
            }
        }

        Set<String> keySet = closeEventMap.keySet();

        List<String> pieColors = new ArrayList<>();
        List<LegendItem> legend = new ArrayList<>();

        SystemCoordinator sc = getSystemCoordinator();

        for (String k : keySet) {
            Integer i = closeEventMap.get(k);
            closingEventCountList.add(i);
            String color = sc.generateRandomRGBColor();
            pieColors.add(color);
            LegendItem li = new LegendItem();
            li.setColorRGBString(color);
            li.setValue(i);
            li.setTitle(k);
            legend.add(li);

        }

        rpt.setPieClosureLegend(legend);

        // Inject into our pie
        dataSet.setData(closingEventCountList);
        dataSet.setBackgroundColor(pieColors);
        pieData.addChartDataSet(dataSet);
        rpt.getPieClosure().setData(pieData);
    }

    /**
     * In-process logic for violations: Horizontal cannot be stacked?
     *
     * @param rpt
     */
    private void initBarViolationsPastMonth(ReportConfigCECaseList rpt) {

        HorizontalBarChartModel hbcm = new HorizontalBarChartModel();
        ChartData barData = new ChartData();

        BarChartDataSet dsNewViols = new BarChartDataSet();

        dsNewViols.setLabel("New Violations");
        dsNewViols.setBackgroundColor("rgb(255,9,122)");
        List<Number> dsNewViolsVals = new ArrayList<>();
//          dsNewViolsVals.add(rpt.getViolationsLoggedNOVDateRange().size());
        dsNewViolsVals.add(6);
        dsNewViols.setData(dsNewViolsVals);

        BarChartDataSet dsCompliance = new BarChartDataSet();
        dsCompliance.setLabel("Compliance");
        dsCompliance.setBackgroundColor("rgb(60,9,122)");
        List<Number> dsComplianceVals = new ArrayList<>();
//          dsComplianceVals.add(rpt.getViolationsLoggedComplianceDateRange().size());
        dsComplianceVals.add(3);
        dsCompliance.setData(dsComplianceVals);

        BarChartDataSet dsCited = new BarChartDataSet();
        dsCited.setLabel("Citation");
        dsCited.setBackgroundColor("rgb(255,9,3)");
        List<Number> dsCitedVals = new ArrayList<>();
//          dsCitedVals.add(rpt.getViolationsCitedDateRange().size());
        dsCitedVals.add(12);
        dsCited.setData(dsCitedVals);

        barData.addChartDataSet(dsNewViols);
        barData.addChartDataSet(dsCompliance);
        barData.addChartDataSet(dsCited);

        List<String> labels = new ArrayList<>();
        labels.add("Reporting Period");
        barData.setLabels(labels);

        hbcm.setData(barData);

        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setStacked(true);
        cScales.addXAxesData(linearAxes);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);

        Title title = new Title();
        title.setDisplay(true);
        title.setText("Violation Statistics");
        options.setTitle(title);

        Tooltip tooltip = new Tooltip();
        tooltip.setMode("index");
        tooltip.setIntersect(false);
        options.setTooltip(tooltip);

        hbcm.setOptions(options);

        rpt.setBarViolationsReport(hbcm);

    }

    /**
     * Primary configuration mechanism for customizing report data from the
     * ceCases.xhtml display. The logic inside me makes sure that hidden events
     * don't make it out to the report, etc. So the ReportConfig object is ready
     * for display and printing
     *
     * @param rptCse
     * @param ua
     * @return
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public ReportConfigCECase report_prepareCECaseReport(ReportConfigCECase rptCse, UserAuthorized ua) throws IntegrationException, BObStatusException, SearchException, BlobException {
        PropertyCoordinator pc = getPropertyCoordinator();
        PersonCoordinator persC = getPersonCoordinator();
        
        if(rptCse == null || rptCse.getCse() == null){
            throw new BObStatusException("Cannot generate report with null config or case inside that config");
        }
        // we actually get an entirely new object instead of editing the 
        // one we used throughout the ceCases.xhtml
        CECaseDataHeavy c = rptCse.getCse();
        
        if(c.getManager() != null){
            rptCse.setCaseManagerPerson(persC.getPerson(c.getManager().getHuman()));
        }
        
        rptCse.setPropDH(pc.assemblePropertyDataHeavy(rptCse.getCse().getProperty(), ua));
        
        List<EventCnF> evList = new ArrayList<>();
        Iterator<EventCnF> iter = c.getEventList(ViewOptionsActiveHiddenListsEnum.VIEW_ALL).iterator();
        while (iter.hasNext()) {
            EventCnF ev = iter.next();

            // toss out hidden events unless the user wants them
            if (ev.isHidden() && !rptCse.isIncludeHiddenEvents()) {
                continue;
            }
            // toss out inactive events unless user wants them
            if (!ev.isActive() && !rptCse.isIncludeInactiveEvents()) {
                continue;
            }
            // toss out events only available internally to the muni users unless user wants them
            evList.add(ev);
        }
        rptCse.setEventListForReport(evList);

        List<NoticeOfViolation> noticeList = new ArrayList<>();
        Iterator<NoticeOfViolation> iterNotice = c.getNoticeList().iterator();
        while (iterNotice.hasNext()) {
            NoticeOfViolation nov = iterNotice.next();
            // skip unsent notices
            if (nov.getSentTS() == null) {
                continue;
            }
            // if the user dones't want all notices, skip returned notices
            if (!rptCse.isIncludeAllNotices() && nov.getReturnedTS() != null) {
                continue;
            }
            noticeList.add(nov);
        }
        rptCse.setNoticeListForReport(noticeList);
        
        // convert columns to CSS grid size
        rptCse.setCeCasePhotoPoolGridSquares(report_ceCase_setGridSquaresFromColCount(rptCse.getCeCasePhotoPoolColumnCount()));
        rptCse.setViolationPhotoGridSquares(report_ceCase_setGridSquaresFromColCount(rptCse.getViolationPhotoColumnCount()));
        rptCse.setCearPhotoGridSquares(report_ceCase_setGridSquaresFromColCount(rptCse.getCearColumnCount()));
        
        return rptCse;
    }
    
    /**
     * Translates column counts to grid squares for CSS display (divides 12 by col count)
     */
    private int report_ceCase_setGridSquaresFromColCount(int colCount){
        switch(colCount){
            case 1: 
                return GRIDSQAURES_COLS_1;
            case 2:
                return GRIDSQAURES_COLS_2;
            case 3:
                return GRIDSQAURES_COLS_3;
            case 4:
                return GRIDSQAURES_COLS_4;
            default:
                return GRIDSQAURES_COLS_2;
        }
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
     * @param messagerName the first and last name of the person submitting the
     * message Note that this submission info is not YET wired into the actual
     * Person objects in the system.
     *
     * @param messagerPhone a simple String rendering of whatever the user types
     * in. Length validation only.
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.EventException
     */
    public void public_attachPublicMessage(int caseID, String msg, String messagerName, String messagerPhone) throws IntegrationException, BObStatusException, EventException {
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
     * For inter-coordinator use only! Not called by backing beans. Primary
     * event life cycle control method which is called by EventCoordinator each
     * time an event is added to a code enf case. The primary business logic
     * related to which events can be attached to a case at any given case phase
     * is implemented in this coordinator method.
     *
     * Its core operation is to check case and event related qualities and
     * delegate further processing to event-type specific methods also found in
     * this coordinator
     *
     * @param evList
     * @param cse the case to which the event should be added
     * @param ev the event to add to the case also included in this call
     * @return a reference to the same incoming List of EventCnF objects that is
     * passed in. Any additional events that need to be attached should be
     * appended to the end of this list
     *
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.ViolationException
     * @throws com.tcvcog.tcvce.domain.EventException
     */
    protected List<EventCnF> events_addEvent_processForCECaseDomain(
            List<EventCnF> evList,
            CECaseDataHeavy cse,
            EventCnF ev)
            throws BObStatusException,
            IntegrationException,
            ViolationException,
            EventException {
        EventType eventType = ev.getCategory().getEventType();
        EventIntegrator ei = getEventIntegrator();
        if (evList == null || cse == null || ev == null) {
            throw new BObStatusException("Null argument to addEvent_ceCaseDomain");
        }

        switch (eventType) {
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
     *
     * @param cse
     * @param rule
     * @throws IntegrationException
     * @throws BObStatusException
     * @throws ViolationException
     */
    protected void events_processCaseOnEventRulePass(CECaseDataHeavy cse, EventRuleAbstract rule)
            throws IntegrationException, BObStatusException, ViolationException {

        CaseIntegrator ci = getCaseIntegrator();
        EventCoordinator ec = getEventCoordinator();
        EventCnF newEvent = null;

//        CasePhaseEnum oldCP = cse.getCasePhase();
//        ec.generateAndInsertPhaseChangeEvent(cse, oldCP, rule);
//        if(rule.getTriggeredEventCategoryID() != 0){
//            newEvent = ec.initEvent(cse, ec.initEventCategory(rule.getTriggeredEventCategoryID()));
//            
//            events_addEvent_processForCECaseDomain(cse, newEvent, null);
//            System.out.println("CaseCoordinator.events_processCaseOnEventRulePass "  + newEvent.getCategory().getEventCategoryTitle());
//        }
//        
    }

    /**
     * Implements business rules for determining which event types are allowed
     * to be attached to the given CECaseDataHeavy based on the case's phase and
     * the user's permissions in the system.
     *
     * Used for displaying the appropriate event types to the user on the
     * cecases.xhtml page
     *
     * @param c the CECaseDataHeavy on which the event would be attached
     * @param u the User doing the attaching
     * @return allowed EventTypes for attaching to the given case
     */
    public List<EventType> events_getPermittedEventTypesForCECase(CECaseDataHeavy c, User u) {
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
     * Logic intermediary event for creating events documenting a CECase's
     * closure
     *
     * @param c
     * @param e
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     */
    private int events_processClosingEvent(CECaseDataHeavy c, EventCnF e, UserAuthorized ua) throws IntegrationException, BObStatusException {
        EventIntegrator ei = getEventIntegrator();

        CasePhaseEnum closedPhase = CasePhaseEnum.Closed;
//        c.setCasePhase(closedPhase);

        if (c.getClosingDate() == null) {
            c.setClosingDate(LocalDateTime.now());
            cecase_updateCECaseMetadata(c, ua);
        }
        // now load up the closing event before inserting it
        // we'll probably want to get this text from a resource file instead of
        // hardcoding it down here in the Java
        e.setCreatedBy(getSessionBean().getSessUser());
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
    public void nov_ResetMailing(NoticeOfViolation nov, UserAuthorized user) throws IntegrationException, AuthorizationException {
        CaseIntegrator ci = getCaseIntegrator();
        if (user.getMyCredential().isHasSysAdminPermissions()) {
            ci.novResetMailingFieldsToNull(nov);
        } else {
            throw new AuthorizationException("User does not have sufficient acces righst to clear notice mailing fields");
        }
    }

    /**
     *
     * @param noticeID
     * @return
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public NoticeOfViolation nov_getNoticeOfViolation(int noticeID)
            throws IntegrationException, BObStatusException, BlobException {
        EventCoordinator ec = getEventCoordinator();
        CaseIntegrator ci = getCaseIntegrator();
        NoticeOfViolation nov = ci.novGet(noticeID);
        if (nov != null) {
            if (nov.getNotifyingOfficer() == null && nov.getCreationBy() != null) {
                nov.setNotifyingOfficer(nov.getCreationBy());
            }
            nov.setEmittedEvents(ec.getEmittedEvents(nov));
        }

        return nov;
    }

    /**
     * Utility method for extracting a list of NOV ID's from the db
     *
     * @param idl
     * @return
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.BlobException
     */
    public List<NoticeOfViolation> nov_getNoticeOfViolationList(List<Integer> idl)
            throws IntegrationException, BObStatusException, BlobException {
        List<NoticeOfViolation> novl = new ArrayList<>();
        if (idl != null && !idl.isEmpty()) {
            for (Integer i : idl) {
                novl.add(nov_getNoticeOfViolation(i));
            }
        }
        return novl;
    }

    /**
     * Called when first creating a notice of violation
     *
     * @param cse
     * @param mdh
     * @param ua
     * @return
     * @throws AuthorizationException
     */
    public NoticeOfViolation nov_GetNewNOVSkeleton(CECaseDataHeavy cse, MunicipalityDataHeavy mdh, UserAuthorized ua) throws AuthorizationException {
        SystemIntegrator si = getSystemIntegrator();
        NoticeOfViolation nov = new NoticeOfViolation();

        nov.setViolationList(new ArrayList<>());
        nov.setDateOfRecord(LocalDateTime.now());
        nov.setBlocksBeforeViolations(new ArrayList<>());
        nov.setBlocksAfterViolations(new ArrayList<>());

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
            // start with sensible default values
            cvd.setIncludeHumanFriendlyText(false);
            cvd.setIncludeOrdinanceText(true);
            cvd.setIncludeViolationPhotos(false);
            nov.getViolationList().add(cvd);
        }

        nov.setCreationBy(ua);
        nov.setNotifyingOfficer(ua);

        return nov;
    }

    /**
     * NOV is ready to send - And coordinate creating an event to document this
     *
     * @param c
     * @param nov
     * @param ua
     * @throws BObStatusException
     * @throws IntegrationException
     * @throws EventException
     * @throws ViolationException
     */
    public void nov_LockAndQueue(CECaseDataHeavy c,
            NoticeOfViolation nov,
            UserAuthorized ua)
            throws BObStatusException, IntegrationException, EventException, ViolationException {

        PersonCoordinator pc = getPersonCoordinator();
        
        if (c == null || nov == null || ua == null || nov.getRecipient() == null || nov.getRecipientMailingAddress() == null) {
            throw new BObStatusException("Cannot lock notice with null case, nov, or recipient Person or mailing or user");
        }
        
        if(nov.getNotifyingOfficerPerson() != null){
            nov.setNotifyingOfficerPerson(pc.getPersonByHumanID(nov.getNotifyingOfficer().getHumanID()));
        }
        
        if (nov.getNotifyingOfficerPerson().getEmailList() == null
                || nov.getNotifyingOfficerPerson().getEmailList().isEmpty()
                || nov.getNotifyingOfficerPerson().getPhoneList() == null
                || nov.getNotifyingOfficerPerson().getPhoneList().isEmpty()) {
            throw new BObStatusException("Cannot lock notice with null notifying officer person or empty address list inside said person");
        }

        CaseIntegrator ci = getCaseIntegrator();

        if (nov.getLockedAndqueuedTS() == null) {
            nov.setLockedAndqueuedTS(LocalDateTime.now());
            nov.setLockedAndQueuedBy(ua);

            // Pull out values of chosen Human and associated mailing address 
            // and inject into permament NOV fields for record keeping
            nov.setFixedAddrXferTS(LocalDateTime.now());
            nov.setFixedRecipientName(nov.getRecipient().getName());

            nov.setFixedRecipientBldgNo(nov.getRecipientMailingAddress().getBuildingNo());
            nov.setFixedRecipientStreet(nov.getRecipientMailingAddress().getStreet().getName());
            nov.setFixedRecipientCity(nov.getRecipientMailingAddress().getStreet().getCityStateZip().getCity());
            nov.setFixedRecipientState(nov.getRecipientMailingAddress().getStreet().getCityStateZip().getState());
            nov.setFixedRecipientZip(nov.getRecipientMailingAddress().getStreet().getCityStateZip().getZipCode());

            nov.setFixedNotifyingOfficerName(nov.getNotifyingOfficerPerson().getName());
            nov.setFixedNotifyingOfficerTitle(nov.getNotifyingOfficerPerson().getJobTitle());
            nov.setFixedNotifyingOfficerPhone(nov.getNotifyingOfficerPerson().getPhoneList().get(0).getPhoneNumber());
            nov.setFixedNotifyingOfficerEmail(nov.getNotifyingOfficerPerson().getEmailList().get(0).getEmailaddress());
            nov.setFixedNotifyingOfficerSignaturePhotoDocID(nov.getNotifyingOfficer().getSignatureBlobID());

            ci.novLockAndQueueForMailing(nov);

            System.out.println("CaseCoordinator.novLockAndQueue | NOV locked in integrator");

        } else {
            throw new BObStatusException("Notice is already locked and queued for sending");
        }
    }

    /**
     * Create a new text block skeleton with injectrable set to true
     *
     * @param muni
     * @return
     */
    public TextBlock nov_getTemplateBlockSekeleton(Municipality muni) {
        TextBlock tb = new TextBlock();
        tb.setMuni(muni);
        tb.setInjectableTemplate(true);
//        tb.setTextBlockCategoryID(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
//                .getString("nov_textblocktemplate_categoryid")));
        tb.setTextBlockText("inject violations with: ***VIOLATIONS***");

        return tb;

    }
    
    /**
     * Extracts all available NOV Types by Muni
     * @param muni
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public List<NoticeOfViolationType> nov_getNOVTypeList(Municipality muni) throws IntegrationException, BlobException{
        CaseIntegrator ci = getCaseIntegrator();
        return nov_getNOVTypeListByID(ci.novGetTypeList(muni));
        
    }
    
    /**
     * Basic getter for NOV types
     * @param typeID
     * @return
     * @throws IntegrationException 
     */
    public NoticeOfViolationType nov_getNOVType(int typeID) throws IntegrationException, BlobException{
        CaseIntegrator ci = getCaseIntegrator();
        if(typeID == 0){
            return null;
        }
        return ci.novGetType(typeID);
        
    }
    
    /**
     * Internal organ for getting NOVTypes by a list of IDs
     * @param idl
     * @return
     * @throws IntegrationException 
     */
    public List<NoticeOfViolationType> nov_getNOVTypeListByID(List<Integer> idl) throws IntegrationException, BlobException{
        List<NoticeOfViolationType> typeList = new ArrayList<>();
        if(idl != null && !idl.isEmpty()){
            CaseIntegrator ci = getCaseIntegrator();
            for(Integer i: idl){
                typeList.add(ci.novGetType(i));
            }
        }
        return typeList;
    }

    /**
     * Called when the NOV is ready to get written to the DB --but before
     * queuing for sending
     *
     * @param nov
     * @param cse
     * @param usr
     * @return
     * @throws IntegrationException
     */
    public int nov_InsertNotice(NoticeOfViolation nov, CECaseDataHeavy cse, User usr) throws IntegrationException, BObStatusException {
        if(nov == null || cse == null || usr == null){
            throw new BObStatusException("cannot insert notice with null notice, notice type, case, or user ");
        }
        if(nov.getNovType() == null){
            throw new BObStatusException("cannot insert notice with null notice type");
        }
        CaseIntegrator ci = getCaseIntegrator();
        nov.setCreationBy(usr);
        
        System.out.println("CaseCoordinator.novInsertNotice");
        return ci.novInsert(cse, nov);
    }

    /**
     * Logic pass-through for NOV updates
     *
     * @param nov
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public void nov_update(NoticeOfViolation nov) throws IntegrationException, BObStatusException {
        CaseIntegrator ci = getCaseIntegrator();
        if (nov != null) {
            if (nov.getLockedAndqueuedTS() == null) {
                ci.novUpdateNotice(nov);
            } else {
                throw new BObStatusException("Cannot update a locked notice.");
            }

        }
    }

    /**
     * Business logic intermediary method for marking a NOV as having already
     * been sent to the property
     *
     * @param ceCase
     * @param nov
     * @param user
     * @throws BObStatusException
     * @throws EventException
     * @throws IntegrationException
     */
    public void nov_markAsSent(CECaseDataHeavy ceCase, NoticeOfViolation nov, UserAuthorized user) throws BObStatusException, EventException, IntegrationException {

        CaseIntegrator ci = getCaseIntegrator();
        EventCoordinator ec = getEventCoordinator();

        nov.setSentTS(LocalDateTime.now());
        nov.setSentBy(user);
        ci.novRecordMailing(nov);

        ec.processEventEmitter(nov, EventEmissionEnum.NOTICE_OF_VIOLATION_SENT, user, ceCase);
//        nov_logNOVSentEvent(ceCase, nov, user);

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
    public void nov_markAsReturned(CECaseDataHeavy c, NoticeOfViolation nov, User user) throws IntegrationException, BObStatusException {
        CaseIntegrator ci = getCaseIntegrator();
        nov.setReturnedTS(LocalDateTime.now());
        nov.setReturnedBy(user);
        ci.novRecordReturnedNotice(nov);
    }

    /**
     * Checks logic before deleting NOVs. It's rare to be able to delete
     * something but an unmailed NOV is a very low impact loss
     *
     * @param nov
     * @throws BObStatusException
     */
    public void nov_delete(NoticeOfViolation nov) throws BObStatusException {
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
    
    /**
     * Factory for NOVViolationType objects (e.g. Notice of Violation, Request for Compliance)
     * @return 
     */
    public NoticeOfViolationType nov_getNoticeOfViolationTypeSkeleton(){
        return new NoticeOfViolationType();
    }
    

    /**
     * @param ps
     * @param blob
     * @return the PrintStyle with the header image set
     * @throws IntegrationException
     * @throws BObStatusException
     */
    public PrintStyle nov_updateStyleHeaderImage(PrintStyle ps, BlobLight blob) throws IntegrationException, BObStatusException {
        CaseIntegrator ci = getCaseIntegrator();
        if (ps == null || blob == null) {
            throw new BObStatusException("Cannot update header image with null style or blob");

        }

        ps.setHeader_img_id(blob.getPhotoDocID());
        ci.novUpdateHeaderImage(ps);

        return ps;

    }

    /**
     * Logic container for creating a NOV from a given template/category ID
     *
     * @param nov
     * @param categoryID
     * @return with text blocks loaded up into before and after based on default
     * sort order
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public NoticeOfViolation nov_assembleNOVFromBlocks(NoticeOfViolation nov, int categoryID) throws IntegrationException {
        CaseIntegrator ci = getCaseIntegrator();
        List<TextBlock> blockList = new ArrayList<>();

        if (categoryID != 0) {
            // TODO: revisit this logic
//            blockList.addAll(ci.getTextBlocksByCategory(categoryID));
        }

        if (!blockList.isEmpty()) {
            for (TextBlock tb : blockList) {
                if (tb.getPlacementOrder() < 0) {
                    nov.getBlocksBeforeViolations().add(tb);
                    Collections.sort(nov.getBlocksBeforeViolations());
                } else if (tb.getPlacementOrder() > 0) {
                    nov.getBlocksAfterViolations().add(tb);
                    Collections.sort(nov.getBlocksAfterViolations());
                }
            }

        }
        return nov;
    }

   
    /**
     * Tool for building an NOV from a template block
     *
     * @param nov
     * @param temp
     * @param cse
     * @return
     * @throws BObStatusException
     */
    public NoticeOfViolation nov_assembleNOVFromTemplate(NoticeOfViolation nov, TextBlock temp, CECase cse) throws BObStatusException {

        if (nov == null || nov.getNovType() == null || temp == null || cse == null) {
            throw new BObStatusException("Cannot assemble a notice from template with null NOV, template or case");
        }

        String template = temp.getTextBlockText();
        int startOfInjectionPoint = template.indexOf(Constants.NOV_VIOLATIONS_INJECTION_POINT);
        System.out.println("CaseCoor.nov_assembleNOVFromTemplate: Injection point found starts at: " + startOfInjectionPoint);
        // If the injection point is not found, put the entire template block as text before Violations
        if (startOfInjectionPoint != -1) {
            nov.setNoticeTextBeforeViolations(template.substring(0, startOfInjectionPoint));
            nov.setNoticeTextAfterViolations(template.substring(startOfInjectionPoint + Constants.NOV_VIOLATIONS_INJECTION_POINT.length()));
        } else {
            nov.setNoticeTextBeforeViolations(template);
        }

        nov.setIncludeStipulatedCompDate(nov.getNovType().isIncludeStipCompDate());
        
        return nov;
    }

    /**
     * Updates only the notes field on Notice of Violation
     *
     * @param mbp
     * @param nov
     * @throws BObStatusException
     * @throws IntegrationException
     */
    public void nov_updateNotes(MessageBuilderParams mbp, NoticeOfViolation nov) throws BObStatusException, IntegrationException {
        CaseIntegrator ci = getCaseIntegrator();
        SystemCoordinator sc = getSystemCoordinator();
        if (nov == null || mbp == null) {
            throw new BObStatusException("Cannot append if notes, case, or user are null");
        }

        nov.setNotes(sc.appendNoteBlock(mbp));

        ci.novUpdateNotes(nov);
    }
    
    /**
     * Inserts an NOV type
     * @param novt
     * @return
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public int nov_insertNOVType(NoticeOfViolationType novt) throws BObStatusException, IntegrationException{
        if(novt == null){
            throw new BObStatusException("Cannot insert new NOV Type with null NOV type");
            
        }
        CaseIntegrator ci = getCaseIntegrator();
        return ci.novInsertNOVType(novt);
    }
    
    /**
     * Updates an NOV type
     * @param novt
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public void nov_updateNOVType(NoticeOfViolationType novt) throws BObStatusException, IntegrationException{
        if(novt == null){
            throw new BObStatusException("Cannot update null NOV type");
        }
        CaseIntegrator ci = getCaseIntegrator();
        ci.novUpdateNOVType(novt);
    }

    /**
     * Deactivation point for NOV types
     * @param novt 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void nov_deactivateNOVType(NoticeOfViolationType novt) throws BObStatusException, IntegrationException{
        if(novt == null){
            throw new BObStatusException("Cannot deactivate null NOV type");
        }
        CaseIntegrator ci = getCaseIntegrator();
        ci.novUpdateNOVType(novt);
        
    }
    
    
    // *************************************************************************
    // *                     BLOBS                                             *
    // *************************************************************************
    /**
     * Primary pathway for inserting blobs for attachment to a CECase Liaises
     * with the BlobCoordinator and Integrator as needed
     *
     * @param ua
     * @param blob
     * @param cse
     * @return
     * @throws BlobException
     * @throws IOException
     * @throws IntegrationException
     * @throws BlobTypeException
     */
    public Blob blob_ceCase_attachBlob(UserAuthorized ua, Blob blob, CECase cse)
            throws BlobException, IOException, IntegrationException, BlobTypeException {
        BlobCoordinator bc = getBlobCoordinator();
        BlobIntegrator bi = getBlobIntegrator();

        throw new BlobException("Failed to atttach blob to case; code must be updated for IFafce_BlobHolder");
        // TODO: update for new code reuse methods!!!

//        Blob freshBlob = bc.storeBlob(blob);
//        bi.linkBlobToCECase(blob, cse);
//        return null;
    }

    // *************************************************************************
    // *                     CITATIONS                                         *
    // *************************************************************************
    /**
     * Logic container for assembling a list of citations by a CECase
     *
     * @param cse
     * @return fully-baked citation objects
     * @throws IntegrationException
     */
    private List<Citation> citation_getCitationList(CECase cse) throws IntegrationException, BObStatusException, BlobException {
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        List<Citation> citList = new ArrayList<>();
        if (cse != null) {
            List<Integer> citIDList = cei.getCitations(cse);
            if (citIDList != null && !citIDList.isEmpty()) {
                for (Integer i : citIDList) {
                    citList.add(citation_getCitation(i));
                }
            }
        }
        return citList;
    }

    /**
     * Logic passthrough for acquiring a list of all possible citation statuses
     * that can be attached to a CitationStatusLogEntry
     *
     * @return sorted status list
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public List<CitationStatus> citation_getCitationStatusList() throws IntegrationException {
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        List<CitationStatus> statusList = cei.getCitationStatusList();
        if (statusList != null && !statusList.isEmpty()) {
            Collections.sort(statusList);
        }
        return statusList;

    }

    /**
     * Logic pass through for citation filing types (e.g. private criminal
     * complaint vs. non-traffic)
     *
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     */
    public List<CitationFilingType> citation_getCitationFilingTypeList() throws IntegrationException, BObStatusException {
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        return cei.getCitationFilingTypes();

    }

    /**
     * Getter for Citation objects
     *
     * @param citationID
     * @return
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public Citation citation_getCitation(int citationID) throws IntegrationException, BObStatusException, BlobException {
        if (citationID == 0) {
            throw new BObStatusException("Cannot get citation with an ID of 0");

        }
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        Citation cit = cei.getCitation(citationID);

        return citation_configureCitation(cit);

    }

    /**
     * Logic method for getting citations all set up
     *
     * @param cit
     * @return
     */
    private Citation citation_configureCitation(Citation cit) throws IntegrationException, BObStatusException, BlobException {
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        PersonCoordinator pc = getPersonCoordinator();
        BlobCoordinator bc = getBlobCoordinator();

        cit.setViolationList(cei.getCodeViolationsByCitation(cit));
        cit.setStatusLog(citation_getCitationStatusLog(cit));
        cit.setHumanLinkList(pc.getHumanLinkList(cit));
        cit.setDocketNos(cei.getCitationDocketRecords(cit));
        cit.setBlobList(bc.getBlobLightList(cit));
        // TODO: populate citation with events and blobs--but this can cycle, so be careful

        return cit;

    }

    /**
     * Initializes a Citation object with a default start status
     *
     * @param creator
     * @param cse
     * @param citingOfficer
     * @return
     * @throws BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public Citation citation_getCitationSkeleton(CECase cse, User creator, User citingOfficer) throws BObStatusException, IntegrationException {
        // turned off before we have a method that can check for a user's permissions
        // when they are NOT the session's authorized user
//        if (!creator.getKeyCard().isHasEnfOfficialPermissions()) {
//            throw new BObStatusException("Users must have enforcement office permissions to create a citation");
//        }
        Citation cit = new Citation();

        cit.setCreatedBy(creator);
        cit.setFilingOfficer(citingOfficer);
        cit.setCitationNo(generateInternalCitationID(cse));
        cit.setDateOfRecord(LocalDateTime.now());
        cit.setOrigin_courtentity(getSessionBean().getSessMuni().getCourtEntities().get(0));

        List<CodeViolation> cvlst = new ArrayList<>();

        // As long as the violation does not have a complianec date, add it to the list
        // for user approval
        if (cse.getViolationList() != null && !cse.getViolationList().isEmpty()) {
            for (CodeViolation v : cse.getViolationList()) {
                if (v.getActualComplianceDate() == null && v.getStipulatedComplianceDate().isBefore(LocalDateTime.now())) {
                    cvlst.add(v);
                    System.out.println("CaseCoordinator.citation_getCitationSkeleton: Adding ViolID: " + v.getViolationID());
                }
            }
        }
        cit.setViolationList(buildCitationCodeViolationLinkSkeletonList(cvlst));
        return cit;
    }

    /**
     * Generates a temporary internal citation number using the format SEAN
     * GRAMZ prefers: CIT-BLDGNO-DATE
     *
     * @param cse
     * @return the proposed internal ID
     */
    private String generateInternalCitationID(CECase cse) throws BObStatusException, IntegrationException {
        if (cse == null) {
            throw new BObStatusException("cannot generate citation no with null case");
        }
        PropertyCoordinator pc = getPropertyCoordinator();

        Property p = pc.getProperty(cse.getParcelKey());
        StringBuilder sb = new StringBuilder();
        sb.append("PCC-");
        sb.append(p.getAddress().getBuildingNo());
        LocalDateTime ldt = LocalDateTime.now();
        sb.append("-");
        sb.append(ldt.getMonthValue());
        sb.append("-");
        sb.append(ldt.getDayOfMonth());
        sb.append("-");
        sb.append(ldt.getYear());
        return sb.toString();

    }

    /**
     * Convenience method for injecting a code violation into a
     * CitationCodeViolation for skeletons to play with in their bellies
     *
     * @param cvl
     * @return
     */
    private List<CitationCodeViolationLink> buildCitationCodeViolationLinkSkeletonList(List<CodeViolation> cvl) {
        List<CitationCodeViolationLink> ccvl = new ArrayList<>();
        if (cvl != null) {
            for (CodeViolation cv : cvl) {
                ccvl.add(new CitationCodeViolationLink((cv)));
            }
        }

        return ccvl;

    }

    /**
     * Logic intermediary for recording that a Citation has been issued, meaning
     * sent to the proper magisterial authority
     *
     * @param c
     * @param creator
     * @return
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public int citation_insertCitation(Citation c,
            UserAuthorized creator)
            throws IntegrationException,
            BObStatusException {
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        SystemCoordinator sc = getSystemCoordinator();
        int freshID = 0;
        if (c == null
                || creator == null
                || c.getFilingOfficer() == null
                || c.getViolationList() == null) {
            throw new BObStatusException("Cannot issue citation with null citation, creator, issuing officer, or violation list.");
        }
        if (c.getViolationList().isEmpty()) {
            throw new BObStatusException("Citations must cite at least one code violation;");
        }

        c.setCreatedBy(creator);
        c.setCreatedTS(LocalDateTime.now());
        c.setLastUpdatedBy(creator);
        c.setLastUpdatedTS(LocalDateTime.now());

        // Create CitationCodeViolationLink objects to wrap each incoming CodeViolation
        // with metadata
        if (c.getViolationList() != null && !c.getViolationList().isEmpty()) {
            for (CitationCodeViolationLink cv : c.getViolationList()) {
                cv.setLinkCreatedByUserID(creator.getUserID());
                cv.setLinkLastUpdatedByUserID(creator.getUserID());
                CitationViolationStatusEnum cvse = getDefaultCitationViolationStatusEnumVal();
                StringBuilder sb = new StringBuilder();
                sb.append("Citation-Violation status initially set to: ");
                sb.append(cvse.getLabel());
                sb.append(" on ");
                sb.append(LocalDate.now().toString());
                cv.setNotes(sb.toString());
                cv.setCitVStatus(cvse);

                cv.setLinkSource(sc.getBObSource(Integer.parseInt(
                        getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("bobsource_internalhuman"))));

            }
        }
        freshID = cei.insertCitation(c);
        c.setCitationID(freshID);
        cei.insertCitationCodeViolationLink(c);

        return freshID;

    }

    /**
     * Logic container for returning the default status of a code violation
     * attached to a single citation
     *
     * @return
     */
    private CitationViolationStatusEnum getDefaultCitationViolationStatusEnumVal() {
        return CitationViolationStatusEnum.AWAITING_PLEA;

    }

    /* **********************************************
    /* ********** CITATION STATUS LOGS **************
    /* **********************************************
    
    
    /**
     * Factory method for citation status log entry objects
     * @return the factoried object
     */
    public CitationStatusLogEntry citation_getStatusLogEntrySkeleton(Citation cit) {
        CitationStatusLogEntry csle = new CitationStatusLogEntry();

        csle.setDateOfRecord(LocalDateTime.now());
        if (cit != null && cit.getOrigin_courtentity() != null) {
            csle.setCourtEntity(cit.getOrigin_courtentity());
        }
        return csle;
    }

    /**
     * Builds a complete citation status log
     *
     * @param cit
     * @return
     */
    public List<CitationStatusLogEntry> citation_getCitationStatusLog(Citation cit) throws BObStatusException, IntegrationException {
        if (cit == null) {
            throw new BObStatusException("Cannot build citationlog with null citation");
        }

        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        List<Integer> idl = cei.citation_getStatusLog(cit);
        List<CitationStatusLogEntry> log = new ArrayList<>();
        if (idl != null && !idl.isEmpty()) {
            for (Integer i : idl) {
                log.add(citation_getCitationStatusLogEntry(i));
            }
        }
        Collections.sort(log);
        Collections.reverse(log);
        return log;
    }

    /**
     * Extracts a single citation status log entry from the db
     *
     * @param logid
     * @return
     */
    public CitationStatusLogEntry citation_getCitationStatusLogEntry(int logid) throws IntegrationException {
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        return cei.getCitationStatusLogEntry(logid);

    }

    /**
     * Entry point for making a new citation status log entry on a particular
     * citation.
     *
     * @param cit to which the log entry shall be attached
     * @param csle the log entry to link to the given citation
     * @param ua doing the logging
     * @return the ID of the fresh record in the citationcitationstatus table
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public int citation_insertCitationStatusLogEntry(Citation cit,
            CitationStatusLogEntry csle,
            UserAuthorized ua)
            throws BObStatusException, IntegrationException {
        if (cit == null
                || csle == null
                || ua == null
                || csle.getStatus() == null) {
            throw new BObStatusException("Cannot make a log entry with null citation, log entry, or user authorized");

        }
        CourtEntityIntegrator cei = getCourtEntityIntegrator();

        csle.setCreatedBy(ua);
        csle.setLastUpdatedBy(ua);
        cit.setLastUpdatedBy(ua);

        return cei.insertCitationStatusLogEntry(cit, csle);

    }

    /**
     * Updates a citation status log entry
     *
     * @param cit
     * @param csle
     * @param ua
     * @throws BObStatusException for any null inputs
     * @throws IntegrationException
     */
    public void citation_updateCitationStatusLogEntry(Citation cit,
            CitationStatusLogEntry csle,
            UserAuthorized ua)
            throws BObStatusException,
            IntegrationException {

        if (cit == null || csle == null || ua == null || csle.getStatus() == null) {
            throw new BObStatusException("Cannot update citation status with null citation, log entry or its status, or user");
        }

        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        csle.setLastUpdatedBy(ua);

        cei.updateStatusLogEntry(csle);
    }

    /**
     * Deactivates a citation log entry
     *
     * @param csle
     * @param ua doing the deactivating
     */
    public void citation_deactivateCitationStatusLogEntry(CitationStatusLogEntry csle, UserAuthorized ua) throws IntegrationException {
        CourtEntityIntegrator cei = getCourtEntityIntegrator();

        csle.setDeactivatedBy(ua);
        csle.setLastUpdatedBy(ua);
        // database sets timestamps!!

        cei.deactivateCitationStatusLogEntry(csle);

    }

    /* **********************************************/
 /* **************** DOCKETS  ********************/
 /* **********************************************/
    /**
     * Factory method for citation dockets Sets the DOR to now and the origin
     * court entity to the head of the muni list
     *
     * @param cit
     * @return
     */
    public CitationDocketRecord citation_getCitationDocketRecordSkeleton(Citation cit) {
        CitationDocketRecord cdr = new CitationDocketRecord();
        if (cit != null && cit.getCitationID() != 0) {
            cdr.setCitationID(cit.getCitationID());
            cdr.setDateOfRecord(LocalDateTime.now());
            if (getSessionBean().getSessMuni().getCourtEntities() != null && !getSessionBean().getSessMuni().getCourtEntities().isEmpty()) {
                cdr.setCourtEntity(getSessionBean().getSessMuni().getCourtEntities().get(0));
            }
            return cdr;
        }
        return null;
    }

    public CitationDocketRecord citation_getCitationDocketRecord(int docketID) throws BObStatusException, IntegrationException {
        if (docketID == 0) {
            throw new BObStatusException("cannot get docket from ID = 0");
        }
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        return cei.getCitationDocketRecord(docketID);

    }

    /**
     * Logic intermediary for dockets
     *
     * @param cdr
     * @param ua
     * @return ID of fresh docket
     */
    public int citation_insertDocketEntry(CitationDocketRecord cdr, UserAuthorized ua) throws IntegrationException {
        int freshID = 0;
        if (cdr != null && ua != null) {
            CourtEntityIntegrator cei = getCourtEntityIntegrator();
            cdr.setCreatedBy(ua);
            cdr.setLastUpdatedBy(ua);
            freshID = cei.insertCitationDocket(cdr);
        }
        return freshID;

    }

    /**
     * Logic pass through for updates to dockets
     *
     * @param cdr
     * @param ua
     * @throws IntegrationException
     */
    public void citation_updateDocketEntry(CitationDocketRecord cdr, UserAuthorized ua) throws IntegrationException {
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        if (cdr != null && ua != null) {
            cdr.setLastUpdatedBy(ua);
            cei.updateCitationDocket(cdr);
        }

    }

    /**
     * Logic intermediary for deactivation of citation Dockets
     *
     * @param cdr
     * @param ua
     * @throws IntegrationException
     */
    public void citation_deactivateDocketEntry(CitationDocketRecord cdr, UserAuthorized ua) throws IntegrationException {
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        if (cdr != null && ua != null) {
            cdr.setDeactivatedBy(ua);
            cdr.setLastUpdatedBy(ua);
            cei.deactivateCitationDocket(cdr);
        }
    }

    /**
     * Logic intermediary for updating fields on Citations in the DB
     *
     * @param c
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public void citation_updateCitation(Citation c) throws IntegrationException, BObStatusException {
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        cei.updateCitation(c);

    }

    /**
     * Updates a record in the citationviolation table
     *
     * @param ccvl
     * @param updateNotes
     * @param ua
     * @throws BObStatusException
     * @throws IntegrationException
     */
    public void citation_updateCitationCodeViolationLink(CitationCodeViolationLink ccvl, String updateNotes, UserAuthorized ua) throws BObStatusException, IntegrationException {
        if (ccvl == null || ccvl.getCitationViolationID() == 0 || ua == null) {
            throw new BObStatusException("Cannot update codeviolationcitation link with null inputs");
        }
        SystemCoordinator sc = getSystemCoordinator();
        CourtEntityIntegrator cei = getCourtEntityIntegrator();

        StringBuilder freshNote = new StringBuilder();
        freshNote.append("Citation-Violation link ID ");
        freshNote.append(ccvl.getCitationViolationID());
        freshNote.append(" status updated to ");
        freshNote.append(ccvl.getCitVStatus().getLabel());
        freshNote.append("<br />");
        freshNote.append("With notes: ");
        freshNote.append("<br />");
        freshNote.append(updateNotes);

        MessageBuilderParams mbp = new MessageBuilderParams(ccvl.getLinkNotes(),
                null, null, freshNote.toString(), getSessionBean().getSessUser(), null);

        ccvl.setLinkNotes(sc.appendNoteBlock(mbp));
        ccvl.setLinkLastUpdatedByUserID(ua.getUserID());
        cei.updateCitationCodeViolationLink(ccvl);

    }

    /**
     * Deactivates a record in the citationviolation table
     *
     * @param ccvl
     * @param ua
     * @throws BObStatusException
     * @throws IntegrationException
     */
    public void citation_deactivateCitationCodeViolationLink(CitationCodeViolationLink ccvl, UserAuthorized ua) throws BObStatusException, IntegrationException {
        if (ccvl == null || ccvl.getCitationViolationID() == 0 || ua == null) {
            throw new BObStatusException("Cannot update codeviolationcitation link with null inputs");
        }

        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        ccvl.setLinkCreatedByUserID(ua.getUserID());
        ccvl.setLinkDeactivatedByUserID(ua.getUserID());
        cei.deactivateCitationCodeViolationLink(ccvl);

    }

    /**
     * Logic intermediary for updating fields on Citations in the DB and
     * deactivating all associated objects: citation dockets, log records, blob
     * links, person links, and codeviolation links, and event links
     *
     * @param c
     * @param ua
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public void citation_removeCitation(Citation c, UserAuthorized ua) throws IntegrationException, BObStatusException, AuthorizationException {
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        UserCoordinator uc = getUserCoordinator();
        PersonCoordinator pc = getPersonCoordinator();
        BlobCoordinator bc = getBlobCoordinator();

        if (c != null && ua != null) {
            uc.auth_verifyUserAuthorizedRank_MeetOrExceed_SECURITYCRITICAL(ua, MIN_RANK_TO_ISSUE_CITATION);
            c.setDeactivatedBy(ua);
            cei.deactivateCitation(c);
            System.out.println("CaseCoordinator.citation_removeCitation | deactivated citation ID: " + c.getCecaseID());
            // deactivate associated objects: log entries
            if (c.getStatusLog() != null && !c.getStatusLog().isEmpty()) {
                for (CitationStatusLogEntry csle : c.getStatusLog()) {
                    citation_deactivateCitationStatusLogEntry(csle, ua);
                    System.out.println("CaseCoordinator.citation_removeCitation | deactivated status log ID: " + csle.getLogEntryID());
                }
            }
            // deactivate associated objects: dockets
            if (c.getDocketNos() != null && !c.getDocketNos().isEmpty()) {
                for (CitationDocketRecord cdr : c.getDocketNos()) {
                    citation_deactivateDocketEntry(cdr, ua);
                    System.out.println("CaseCoordinator.citation_removeCitation | deactivated docket ID: " + cdr.getDocketID());
                }
            }
            // deactivate associated objects: citationviolations
            if (c.getViolationList() != null && !c.getViolationList().isEmpty()) {
                for (CitationCodeViolationLink ccvl : c.getViolationList()) {
                    citation_deactivateCitationCodeViolationLink(ccvl, ua);
                    System.out.println("CaseCoordinator.citation_removeCitation | deactivated citation violation link: " + ccvl.getCitationViolationID());
                }
            }
            // deactivate associated objects: personlinks
            if (c.getHumanLinkList() != null && !c.getHumanLinkList().isEmpty()) {
                for (HumanLink hl : c.getHumanLinkList()) {
                    pc.deactivateHumanLink(hl, ua);
                    System.out.println("CaseCoordinator.citation_removeCitation | deactivated human link link: " + hl.getLinkID());
                }
            }
            // deactivate associated objects: bloblinks
            if (c.getBlobList() != null && !c.getBlobList().isEmpty()) {
                for (BlobLight bl : c.getBlobList()) {
                    bc.deleteLinksToPhotoDocRecord(bl, BlobLinkEnum.CITATION);
                    System.out.println("CaseCoordinator.citation_removeCitation | deactivated links to blob light: " + bl.getPhotoDocID());
                }
            }

            // deactivate associated objects: related events
            // Not sure until we write the events!
        }
    }

    /**
     * Updates only the notes field on Citation
     *
     * @param mbp
     * @param cit
     * @throws BObStatusException
     * @throws IntegrationException
     */
    public void citation_updateNotes(MessageBuilderParams mbp, Citation cit) throws BObStatusException, IntegrationException {
        CaseIntegrator ci = getCaseIntegrator();
        SystemCoordinator sc = getSystemCoordinator();
        if (cit == null || mbp == null) {
            throw new BObStatusException("Cannot append if notes, case, or user are null");
        }

        cit.setNotes(sc.appendNoteBlock(mbp));

        ci.updateCitationNotes(cit);
    }

//    --------------------------------------------------------------------------
//    ********************* CE Action Requests *********************************
//    --------------------------------------------------------------------------
    /**
     * Factory method for our CEActionRequests - initializes the date as well
     *
     * @return The CEActionRequest ready for populating with user values
     */
    public CEActionRequest cear_getInititalizedCEActionRequest() {
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
    public CEActionRequest cear_getCEActionRequest(int cearid) throws IntegrationException, BObStatusException {
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
    public boolean cear_determineCEActionRequestRoutingActionEnabledStatus(
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

    public List<CEActionRequestIssueType> cear_getIssueTypes(Municipality muni) throws IntegrationException {

        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();

        List<CEActionRequestIssueType> typeList = ceari.getRequestIssueTypeList(muni);

        return typeList;

    }

    public List<CEActionRequestIssueType> cear_getIssueTypes() throws IntegrationException {

        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();

        List<CEActionRequestIssueType> typeList = ceari.getRequestIssueTypeList();

        return typeList;

    }

    // *************************************************************************
    // *                     VIOLATIONS                                        *
    // *************************************************************************
    
    /**
     * Utility method for iterating over a given list of violations
     * and determining the soonest stip comp date
     * 
     * @param vlist
     * @return perhaps a non-null date if at least one violation is in the inputted list
     * and has a stip comp date
     */
    private LocalDateTime violation_determineEarliestStipCompDate(List<CodeViolation> vlist){
        LocalDateTime earliesSCD = null;
        
        if(vlist != null && !vlist.isEmpty()){
            for(CodeViolation cv: vlist){
                if(cv.getStipulatedComplianceDate() != null){
                    if(earliesSCD == null){
                        earliesSCD = cv.getStipulatedComplianceDate();
                    } else {
                        if(earliesSCD.isAfter(cv.getStipulatedComplianceDate())){
                            earliesSCD = cv.getStipulatedComplianceDate();
                        }
                    }
                }
            }
        }
        return earliesSCD;
    }
    
    
    
    /**
     * Violation status enum values have associated ordinal values which this
     * method looks at to determine the highest one, which it returns and then
     * goes on break
     *
     * @param vList
     * @return
     */
    public int violation_determineMaxViolationStatus(List<CodeViolation> vList) {
        int maxStatus = -1;
        if (vList != null) {
            for (CodeViolation cv : vList) {
                if (cv.getStatus().getOrder() > maxStatus) {
                    maxStatus = cv.getStatus().getOrder();
                }
            }
        }
        return maxStatus;
    }

    /**
     * Iterates over all of a case's violations and checks for compliance. If
     * all of the violations have a compliance date, the case is automatically
     * closed and a case closing event is generated and added to the case
     *
     * @param c the case whose violations should be checked for compliance
     * @param ua
     * @throws IntegrationException
     * @throws BObStatusException
     * @throws ViolationException
     * @throws com.tcvcog.tcvce.domain.EventException
     */
    public void violation_checkForFullComplianceAndCloseCaseIfTriggered(CECaseDataHeavy c, UserAuthorized ua)
            throws IntegrationException, BObStatusException, ViolationException, EventException {

        EventCoordinator ec = getEventCoordinator();
        EventIntegrator ei = getEventIntegrator();

        boolean complianceWithAllViolations = false;

        ListIterator<CodeViolation> fullViolationLi = c.getViolationList().listIterator();

        CodeViolation cv = null;
        while (fullViolationLi.hasNext()) {
            cv = fullViolationLi.next();
            // if there are any outstanding code violations, toggle switch to 
            // false and exit the loop. Phase change will not occur
            if (cv.getActualComplianceDate() != null) {
                complianceWithAllViolations = true;
            } else {
                complianceWithAllViolations = false;
                break;
            }
        } // close while

        EventCnF complianceClosingEvent;

        if (complianceWithAllViolations) {
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
     * @return
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public CodeViolation violation_getCodeViolationSkeleton(CECaseDataHeavy c) throws BObStatusException, IntegrationException {
        CodeViolation v = new CodeViolation();
        SystemCoordinator sc = getSystemCoordinator();
         
        v.setSeverityIntensity(sc.getIntensityClass(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("defaultviolationseverity"))));
        v.setDateOfRecord(LocalDateTime.now());
        if (c != null) {
            v.setCeCaseID(c.getCaseID());
        } else {
            throw new BObStatusException("Cannot attach violation to null case");
        }

        // control is passed back to the violationAddBB which stores this 
        // generated violation under teh activeCodeViolation in the session
        // which the ViolationAddBB then picks up and edits
        return v;
    }

    /**
     * Standard coordinator method which calls the integration method after
     * checking business rules. ASLO attaches blobs if present
     *
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
    public int violation_attachViolationToCase(CodeViolation cv, CECase cse, UserAuthorized ua)
            throws IntegrationException, ViolationException, BObStatusException, EventException, SearchException {

        if (cv == null || cse == null || ua == null) {
            throw new BObStatusException("Cannot attach violation with null viol, case, or user");
        }

        CaseIntegrator ci = getCaseIntegrator();
        PaymentCoordinator pc = getPaymentCoordinator();
        BlobCoordinator blobc = getBlobCoordinator();
        
        int insertedViolationID;

        violation_verifyCodeViolationAttributes(cse, cv);
        if (cv.getLastUpdatedUser() == null) {
            cv.setLastUpdatedUser(ua);
        }
        if (cv.getCreatedBy() == null) {
            cv.setCreatedBy(ua);
        }
        
        insertedViolationID = ci.insertCodeViolation(cv);
        pc.insertAutoAssignedFees(cse, cv);
        // deal with blobs
        if(cv.getBlobList() != null && !cv.getBlobList().isEmpty()){
            System.out.println("CaseCoordinator.violation_attachViolationToCase | linking blob to violation on viol insert! size: " + cv.getBlobList().size());
            // ordinarily a violation wouldn't have blobs on it from a fresh case attachment
            // but it might from an inspection!
            cv.setViolationID(insertedViolationID);
            blobc.linkBlobHolderToBlobList(cv, cv.getBlobList() );
        } else {
            System.out.println("CaseCoordinator.violation_attachViolationToCase | Found null or empty blob list on violation attachment" );
        }
        
        return insertedViolationID;
    }

    /**
     *
     * @param vid
     * @return
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public CodeViolation violation_getCodeViolation(int vid)
            throws IntegrationException, BObStatusException {
        
        CaseIntegrator ci = getCaseIntegrator();
        CodeViolation cv = ci.getCodeViolation(vid);
        
        try {
            cv = violation_configureCodeViolation(cv);
        } catch (BlobException ex) {
            System.out.println(ex);
            throw new BObStatusException("Could not retrieve blobs on Violations");
        }
        
        return cv;
    }

    /**
     * Unused as of March 2022--nice idea though
     *
     * @param vid
     * @return
     * @throws ViolationException
     * @throws IntegrationException
     * @throws SearchException
     * @throws BObStatusException
     */
    public CodeViolationPropCECaseHeavy violation_getCodeViolationPropCECaseHeavy(int vid) throws ViolationException, IntegrationException, SearchException, BObStatusException {
        PropertyCoordinator pc = getPropertyCoordinator();
        CodeViolationPropCECaseHeavy cvpcdh = null;
        if (vid != 0) {
            CodeViolation cv = violation_getCodeViolation(vid);
            cvpcdh = new CodeViolationPropCECaseHeavy(cv);

        } else {
            throw new ViolationException("Cannot construct a CodeViolationPropCECaseHeavy with violation ID of 0");
        }

        return cvpcdh;

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
    private CodeViolation violation_configureCodeViolation(CodeViolation cv) throws IntegrationException, BObStatusException, BlobException {
        SystemIntegrator si = getSystemIntegrator();
        CaseIntegrator ci = getCaseIntegrator();
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        BlobCoordinator bc = getBlobCoordinator();

        
        cv.setBlobList(bc.getBlobLightList(cv));
        cv.setCitationIDList(cei.getCitationIDsByViolation(cv.getViolationID()));
        cv.setNoticeIDList(ci.novGetNOVIDList(cv));

        // nullification overrides all
        if (cv.getNullifiedTS() != null) {
            cv.setStatus(CodeViolationStatusEnum.NULLIFIED);
            cv.setIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                    .getString(cv.getStatus().getIconPropertyName()))));

        }  else if(cv.getTransferredTS() != null){
            cv.setStatus(CodeViolationStatusEnum.TRANSFERRED);
            cv.setIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                    .getString(cv.getStatus().getIconPropertyName()))));
            
        } else if (cv.getActualComplianceDate() == null) {
            // violation still within compliance timeframe
            if (cv.getDaysUntilStipulatedComplianceDate() >= 0) {

                cv.setStatus(CodeViolationStatusEnum.UNRESOLVED_WITHINCOMPTIMEFRAME);
                cv.setIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString(cv.getStatus().getIconPropertyName()))));

                // violation has NOT been cited, but is past compliance timeframe end date
            } else if (cv.getCitationIDList().isEmpty()) {

                cv.setStatus(CodeViolationStatusEnum.UNRESOLVED_EXPIREDCOMPLIANCETIMEFRAME);
                cv.setIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString(cv.getStatus().getIconPropertyName()))));

                // violation has been cited on at least one citation
            } else {
                cv.setStatus(CodeViolationStatusEnum.UNRESOLVED_CITED);
                cv.setIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString(cv.getStatus().getIconPropertyName()))));
            }
            // we have a resolved violation
        } else {
            cv.setStatus(CodeViolationStatusEnum.RESOLVED);
            cv.setIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                    .getString(cv.getStatus().getIconPropertyName()))));
        }
        return cv;
    }

    /**
     * Internal logic container for checking status of BObs before allowing
     * updating of the underlying CodeViolation record
     *
     * @param cse
     * @param cv
     * @return
     * @throws ViolationException
     */
    private void violation_verifyCodeViolationAttributes(CECase cse, CodeViolation cv) throws ViolationException {
        if (cse == null || cv == null) {
            throw new ViolationException("Cannot verify code violation attributes with null case or violation");
        }
        if (cv.getCeCaseID() != cse.getCaseID()) {
            throw new ViolationException("Violation is not mapped to parent case ID");
        }

        if (cse.getPriority() != null && !cse.getPriority().isQualifiesAsOpen()) {
            throw new ViolationException("Cannot update code violations on closed cases!");
        }
        if (cse.getStatusBundle() != null && cse.getStatusBundle().getPhase() == CasePhaseEnum.Closed) {
            throw new ViolationException("Cannot update code violations on closed cases!");
        }
        if (cv.getStipulatedComplianceDate().isBefore(cv.getDateOfRecord())) {
            throw new ViolationException("Stipulated compliance date cannot be before the violation's date of record");
        }

    }

    /**
     * Asks the given field inspection for its failed ordinances that are slated
     * for migration and injects them into CodeViolations for the user to tweak.
     * When the user is done, they should all get injected into the
     * CodeViolationMigratingSettings object and sent to the method in this
     * class which will conduct the actual migration
     *
     * @param fin from which the failed items are to be extracted
     * @return a list, perhaps with the CodeViolations that correspond to each
     * of the failed items
     * @throws BObStatusException with null inputs or insufficient inputs
     *
     */
    public List<CodeViolation> violation_buildViolationListFromFailedInspectionItems(FieldInspection fin)
            throws BObStatusException {

        if (fin == null) {
            throw new BObStatusException("Cannot build violation list from null field inspection");
        }

        List<CodeViolation> cvl = new ArrayList<>();
        System.out.println("CaseCoordinator.violation_buildViolationListFromFailedInspectionItems | current FIN ID: " + fin.getInspectionID());
        List<OccInspectedSpaceElement> ecel = fin.extractFailedItemsForCECaseMigration();
        System.out.println("CaseCoordinator.violation_buildViolationListFromFailedInspectionItems | violated ecel size: " + ecel.size());
        if (!ecel.isEmpty()) {
            for (OccInspectedSpaceElement oise : ecel) {
                CodeViolation cv = new CodeViolation();
                cv.setViolatedEnfElement((EnforcableCodeElement) oise);
                cv.setDescription(oise.getInspectionNotes());
                cv.setSeverityIntensity(oise.getFaillureSeverity());
                cv.setCreatedBy(oise.getLastInspectedBy());
                cv.setLastUpdatedUser(oise.getLastInspectedBy());
                cv.setPenalty(oise.getNormPenalty());
                cv.setBlobList(oise.getBlobList());
                cvl.add(cv); // method will make new violation
            }
        }
        return cvl;
    }
    
    /**
     * Iterates over a list of cecases and only returns those which can receive
     * new violations during the migration process either from another CECase
     * or from a field inspection attached to either a cecase or an occ period
     * @param cecaseList of possible migration targets
     * @param currentCase If not null, I won't add this case to the list
     * @return a list, perhaps containing one or more eligible cases
     */
    public List<CECasePropertyUnitHeavy> cecase_assembleListOfOpenCases(List<CECasePropertyUnitHeavy> cecaseList, CECaseDataHeavy currentCase){
        List<CECasePropertyUnitHeavy> cl = new ArrayList<>();
        if(cecaseList != null && !cecaseList.isEmpty()){
            for(CECasePropertyUnitHeavy cse: cecaseList){
                if(cse.getClosingDate() != null){
                    continue;
                }
                // removed to add all cases
//                if(currentCase != null){
//                    if(currentCase.getCaseID() == cse.getCaseID()){
//                        continue;
//                    }
//                }
                cl.add(cse);
            }
        }
        return cl;
    }

    /**
     * Generator method for violation migration settings, with sensible defaults
     * applied before returning
     *
     * @return
     */
    public CodeViolationMigrationSettings violation_migration_getCodeViolationMigrationSettingsSkeleton() {
        CodeViolationMigrationSettings cvms = new CodeViolationMigrationSettings();
        cvms.setViolationListReadyForInsertion(new ArrayList<>());
        cvms.setViolationListSuccessfullyMigrated(new ArrayList<>());
        cvms.setLinkInspectedElementPhotoDocsToViolation(true);
        cvms.setUseInspectedElementFindingsAsViolationFindings(true);
        cvms.setMigrateWithoutMarkingSourceViolsAsTransferred(false);
        cvms.appendToMigrationLog("Init complete");

        return cvms;
    }

    /**
     * Iterates over a list of case violations and extracts those without
     * compliance, or nullification
     *
     * @param cse
     * @return a list, perhaps containing some violations
     */
    public List<CodeViolation> violation_migration_assembleViolationsForMigrationFromCECase(CECase cse) throws ViolationException {
        if (cse == null) {
            throw new ViolationException("Cannot extract violations from null case");
        }
        List<CodeViolation> vtxferl = new ArrayList<>();
        if (cse.getViolationList() != null && !cse.getViolationList().isEmpty()) {
            for (CodeViolation cv : cse.getViolationList()) {
                if (cv.getActualComplianceDate() == null
                        && cv.getNullifiedTS() == null) {
                    vtxferl.add(cv);
                }
            }
        }
        return vtxferl;
    }

    /**
     * The primary entry way for migrating code violations from all sorts of
     * places to either a new or existing code enforcement case
     *
     * @param cvms containing the migration pathway and all the necessary
     * trimmings
     * @param ua
     * @return a list, perhaps with one or more IDs of the new Code Violations
     * written to the DB
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public CodeViolationMigrationSettings violation_migration_migrateViolations(CodeViolationMigrationSettings cvms, UserAuthorized ua)
            throws BObStatusException {
        if (cvms == null) {
            throw new BObStatusException("Cannot migrate code violations with null settings or null pathway in that settings or violationlist or empty list");
        }

        List<Integer> freshViolationList = new ArrayList<>();

        try {
            // setup basic configuration stuff
            violation_migration_auditMigrationSettings(cvms);
            violation_migration_configureTargetCase(cvms, ua);
            violation_migration_assembleFreshViolations(cvms);

            
            // then write the new violations
            for (CodeViolation cv : cvms.getViolationListReadyForInsertion()) {
                freshViolationList.add(violation_attachViolationToCase(cv, cvms.getCeCaseParent(), cvms.getUserEnactingMigration()));
            }

            if(!cvms.isMigrateWithoutMarkingSourceViolsAsTransferred()){
                violation_migration_recordTransferOfTransferrables(cvms);
            } else {
                cvms.appendToMigrationLog("User indiciated NOT to mark source violations as transferrred; skipping this step.");
            }
            
            cvms.getViolationListSuccessfullyMigrated().addAll(violation_getCodeViolations(freshViolationList));
        } catch (ViolationException | BObStatusException | EventException | IntegrationException | SearchException | BlobException ex) {
            System.out.println(ex);
            String exname = "Exception caught: " + ex.getClass().getName();
            cvms.appendToMigrationLog(exname);
            cvms.appendToMigrationLog(ex.getMessage());
        }

        return cvms;
    }

    /**
     * Iterates over a list of instances of Transferrable and records their
     * movement in the DB
     *
     * @param trables
     * @throws ViolationException
     */
    private void violation_migration_recordTransferOfTransferrables(CodeViolationMigrationSettings cvms) throws ViolationException, IntegrationException {
        if (cvms == null) {
            throw new ViolationException("Cannot mark transferrables as transferred with null input");
        }
        CaseIntegrator ci = getCaseIntegrator();
        List<IFace_transferrable> trlist = cvms.getTransferrableList();
        if(trlist != null || !trlist.isEmpty()){

            for (IFace_transferrable trable : cvms.getTransferrableList()) {
                trable.setTransferredBy(cvms.getUserEnactingMigration());
                trable.setTransferredTS(LocalDateTime.now());
                trable.setTransferredToCECaseID(cvms.getCeCaseParent().getCaseID());

                ci.updateTransferrable(trable);
                StringBuilder sb = new StringBuilder();
                sb.append("Recorded transfer of transferrable in table: ");
                sb.append(trable.getTransferEnum().getTargetTableID());
                sb.append(" With PK:  ");
                sb.append(trable.getDBKey());
                System.out.println("CaseCoordinator.violation_migration_recordTransferOfTransferrables : " + sb.toString());
                cvms.appendToMigrationLog(sb.toString());
            }
            
        } else {
            throw new ViolationException("Cannot transfer with empty list of transferrables");
        }

    }

    /**
     * Internal logic checker for code violation migration settings
     *
     * @param cvms
     * @throws BObStatusException
     */
    private void violation_migration_auditMigrationSettings(CodeViolationMigrationSettings cvms) throws ViolationException {

        if (cvms.getPathway() == null) {
            throw new ViolationException("Audit failure: Null pathway");
        }
        
        if (cvms.getViolationListToMigrate() == null) {
            throw new ViolationException("Audit failure: Null violation list to migrate");
        }
        
        if (cvms.getViolationListToMigrate().isEmpty()) {
            throw new ViolationException("Audit failure: Empty violation list to migrate");
        }
        
        if (cvms.getUserEnactingMigration() == null) {
            throw new ViolationException("Cannot migrate with null user authorized doing migration");
        }
    }

    /**
     * Internal logic organ for examining the migration settings object and
     * building a new case if needed
     *
     * @param cvms not null
     */
    private void violation_migration_configureTargetCase(CodeViolationMigrationSettings cvms, UserAuthorized ua)
            throws ViolationException, IntegrationException, BObStatusException, SearchException, BlobException {
        if (cvms == null) {
            throw new ViolationException("Cannot configure case with null cvms");
        }

        if (cvms.getPathway().isUseExistingCase()) {
            if (cvms.getCeCaseParent() == null) {
                throw new ViolationException("Cannot migrate to an existing case with null case on settings");
            }
            // We need a new case
        } else {
            if (cvms.getProp() == null) {
                throw new ViolationException("Cannot make new case for violations with null property");
            }
            CECase targetCase = cecase_initCECase(cvms.getProp(), cvms.getUserEnactingMigration());
            if (cvms.getNewCaseDateOfRecord() != null) {
                targetCase.setOriginationDate(cvms.getNewCaseDateOfRecord());
            } else {
                targetCase.setOriginationDate(LocalDateTime.now());
            }
            targetCase.setCaseName(cvms.getNewCECaseName());
            targetCase.setCaseManager(cvms.getNewCECaseManager());

            try {
                targetCase = cecase_getCECase(cecase_insertNewCECase(targetCase, cvms.getUserEnactingMigration(), cvms.getNewCECaseOriginationEventCategory(), null), ua);
            } catch (EventException ex) {
                System.out.println(ex);
                cvms.appendToMigrationLog("FATAL: Could not insert new CECase due to event exception.");
            }

            cvms.setCeCaseParent(cecase_assembleCECasePropertyUnitHeavy(targetCase));
        }
    }

    /**
     * Internal organ for actually setting up each new violation from the list
     * of violations to migrate
     *
     * @param cvms Caller is responsible for having audited this
     */
    private void violation_migration_assembleFreshViolations(CodeViolationMigrationSettings cvms) throws ViolationException {
        if (cvms.getViolationListReadyForInsertion() == null) {
            cvms.setViolationListReadyForInsertion(new ArrayList<>());
        }
        for (CodeViolation cv : cvms.getViolationListToMigrate()) {
            StringBuilder sb = new StringBuilder("Preparing to migrate violation ID: ");
            sb.append(cv.getViolationID());
            cvms.appendToMigrationLog(sb.toString());

            CodeViolation targetViol = new CodeViolation();

            targetViol.setCeCaseID(cvms.getCeCaseParent().getCaseID());
            targetViol.setViolatedEnfElement(cv.getViolatedEnfElement());

            if (cvms.getViolationDateOfRecord() != null) {
                targetViol.setDateOfRecord(cvms.getViolationDateOfRecord());
            } else {
                targetViol.setDateOfRecord(LocalDateTime.now());
            }

            targetViol.setStipulatedComplianceDate(cvms.getUnifiedStipComplianceDate());

            targetViol.setDescription(cv.getDescription());
            if(targetViol.getDescription() == null){
                targetViol.setDescription("[no description]");
            }
            targetViol.setPenalty(cv.getPenalty());

            // deal with blobs
            if (cvms.isLinkInspectedElementPhotoDocsToViolation()) {
                if(cv.getBlobList() != null && !cv.getBlobList().isEmpty()){
                    System.out.println("CaseCoordinator.violation_migration_assembleFreshViolations | incoming code violation blob list size: " + cv.getBlobList().size());
                    targetViol.setBlobList(cv.getBlobList());
                    
                } else {
                    System.out.println("CaseCoordinator.violation_migration_assembleFreshViolations | null or empty incoming cv blob list");
                }
            }
            targetViol.setSeverityIntensity(cv.getSeverityIntensity());
            targetViol.setActive(true);
            targetViol.setCreatedBy(cvms.getUserEnactingMigration());
            targetViol.setLastUpdatedUser(cvms.getUserEnactingMigration());

            targetViol.setNotes(violation_migration_buildTargetViolationNotes(cv, cvms));

            // finally, add our new violation to the list for insertion
            cvms.getViolationListReadyForInsertion().add(targetViol);

        }
    }

    /**
     * Internal logic organ for determining the target violation stip comp date
     *
     * @param cv
     * @param cvms
     * @return
     */
    private LocalDateTime violation_migration_determineTargetViolStipCompDate(CodeViolation cv, CodeViolationMigrationSettings cvms) throws ViolationException {
        if (cv == null || cvms == null) {
            throw new ViolationException("Cannot determine new stip date with null cv or settings object");
        }
        LocalDateTime targetVStipDate;
        if (cv.getStipulatedComplianceDate() != null) {
            targetVStipDate = cv.getStipulatedComplianceDate();
        } else {
            if (cvms.getUnifiedStipComplianceDate() != null) {
                targetVStipDate = cvms.getUnifiedStipComplianceDate();
            } else {
                if (cv.getViolatedEnfElement().getNormDaysToComply() != 0) {
                    targetVStipDate = LocalDateTime.now().plusDays(cv.getViolatedEnfElement().getNormDaysToComply());
                } else {
                    targetVStipDate = LocalDateTime.now().plusDays(FALLBACK_DAYSTOCOMPLY);
                }
            }
        }
        return targetVStipDate;
    }

    private static final String VIOLMIG_INJECTION_MARKER_NOW = "[NOW]";
    private static final String VIOLMIG_INJECTION_MARKER_FIELDINSPECTIONID = "[FINID]";
    private static final String VIOLMIG_INJECTION_MARKER_SOURCECECASEID = "[CASEID]";
    private static final String VIOLMIG_INJECTION_MARKER_SOURCEOCCPERIOD = "[PERIODID]";

    /**
     * Internal organ for building a nice, descriptive note on the violations
     * being migrated so we know where they came from
     * TODO: Get me working!
     *
     * @param cv yet to be inserted that needs the note
     * @param cvms with all the goodies
     */
    private String violation_migration_buildTargetViolationNotes(CodeViolation cv, CodeViolationMigrationSettings cvms) throws ViolationException {
        if (cv == null || cvms == null) {
            throw new ViolationException("Cannot determine new stip date with null cv or settings object");
        }

        String vnote = cvms.getPathway().getViolationNoteInjectableString();

        String now = LocalDateTime.now().toString();
        vnote = vnote.replace(VIOLMIG_INJECTION_MARKER_NOW, now);

        if (cvms.getSourceInspection() != null) {
            String insID = String.valueOf(cvms.getSourceInspection().getInspectionID());
            if (insID != null) {
                vnote = vnote.replace(VIOLMIG_INJECTION_MARKER_FIELDINSPECTIONID, insID);
            }
        }
        if (cvms.getSourceCase() != null) {
            String scaseID = String.valueOf(cvms.getSourceCase().getCaseID());
            if (scaseID != null) {
                vnote = vnote.replace(VIOLMIG_INJECTION_MARKER_SOURCECECASEID, scaseID);
            }
        }
        if (cvms.getSourceOccPeriod() != null) {
            String speriodID = String.valueOf(cvms.getSourceOccPeriod().getPeriodID());
            if (speriodID != null) {
                vnote = vnote.replace(VIOLMIG_INJECTION_MARKER_SOURCEOCCPERIOD, speriodID);
            }
        }
        String ln = "Violation Note: " + vnote;
        // BROKEN as of 21 SEP 2022
        // Placeholder injection
//        cvms.appendToMigrationLog(ln);
        cvms.appendToMigrationLog("This violation was migrated from a field inspection on this case, a different case, a permit file!");

        return vnote;
    }

    /**
     * Logic holder for injecting a code element into a code violation and
     * setting sensible default values based on preferences by muni
     *
     * @param cv can be null
     * @param ece to be injected
     * @return the CodeViolation with injected ordinance
     * @throws BObStatusException
     */
    public CodeViolation violation_injectOrdinance(CodeViolation cv,
            EnforcableCodeElement ece)
            throws BObStatusException, IntegrationException {

        if (ece != null) {

            if (cv == null) {
                cv = new CodeViolation();
            }

            int daysInFuture;
            if (ece.getNormDaysToComply() != 0) {
                daysInFuture = ece.getNormDaysToComply();
            } else {
                daysInFuture = FALLBACK_DAYSTOCOMPLY;
            }
            cv.setViolatedEnfElement(ece);
            
            cv.setDescription(ece.getDefaultViolationDescription());
            cv.setStipulatedComplianceDate(LocalDateTime.now().plusDays(daysInFuture));
            cv.setDateOfRecord(LocalDateTime.now());
            cv.setPenalty(ece.getNormPenalty());
            if(cv.getSeverityIntensity() == null){
                SystemCoordinator sc = getSystemCoordinator();
                cv.setSeverityIntensity(sc.getIntensityClass(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("defaultviolationseverity"))));
            }

        } else {
            throw new BObStatusException("Cannot inject null ordinance or cannot inject into null code violation");
        }
        return cv;
    }

    /**
     * Access point for updating a CodeViolation record
     *
     * @param cse
     * @param cv
     * @param u
     * @throws ViolationException
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public void violation_updateCodeViolation(CECaseDataHeavy cse,
            CodeViolation cv,
            UserAuthorized u)
            throws ViolationException,
            IntegrationException,
            BObStatusException {

        EventCoordinator ec = getEventCoordinator();
        CaseIntegrator ci = getCaseIntegrator();
        EventIntegrator ei = getEventIntegrator();

        if (cse == null || cv == null || u == null) {
            throw new BObStatusException("Cannot update a code violation given a null case, violation, or user");
        }

        cv.setLastUpdatedUser(u);
        ci.updateCodeViolation(cv);

    } // close method

    /**
     * Updates only the notes field on violation. This method takes care of
     * pulling out existing notes and prepending the new notes
     *
     * @param mbp
     * @param viol
     * @throws BObStatusException
     * @throws IntegrationException
     */
    public void violation_updateNotes(MessageBuilderParams mbp, CodeViolation viol) throws BObStatusException, IntegrationException {
        CaseIntegrator ci = getCaseIntegrator();
        SystemCoordinator sc = getSystemCoordinator();
        if (viol == null || mbp == null) {
            throw new BObStatusException("Cannot append if notes, case, or user are null");
        }

        viol.setNotes(sc.appendNoteBlock(mbp));

        ci.updateCodeViolationNotes(viol);
    }

    /**
     * Attempts to deactivate a code violation, but will thow an Exception if
     * the CodeViolation has been used in a notice or in a citation
     *
     * @param cv
     * @param ua
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void violation_deactivateCodeViolation(CodeViolation cv, UserAuthorized ua) throws BObStatusException, IntegrationException {
        CaseIntegrator ci = getCaseIntegrator();

        if (cv.getCitationIDList() != null && !cv.getCitationIDList().isEmpty()) {
            throw new BObStatusException("Cannot deactivate a violation that has been used in a citation");
        }

        if (cv.getNoticeIDList() != null && !cv.getNoticeIDList().isEmpty()) {
            throw new BObStatusException("Cannot deactivate a violation that has been used in a notice");
        }

        cv.setActive(false);
        ci.updateCodeViolation(cv);

    }

    /**
     * Attempts to deactivate a code violation, but will thow an Exception if
     * the CodeViolation has been used in a notice or in a citation
     *
     * @param cv
     * @param ua
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void violation_NullifyCodeViolation(CodeViolation cv, UserAuthorized ua) throws BObStatusException, IntegrationException {
        CaseIntegrator ci = getCaseIntegrator();
//
//        if (cv.getCitationIDList() != null && !cv.getCitationIDList().isEmpty()) {
//            throw new BObStatusException("Cannot deactivate a violation that has been used in a citation");
//        }
        // inject appropriate values into CV before updating
        if (cv != null && ua != null) {
            // cannot nullify a violation for which compliance has been achieved
            if (cv.getActualComplianceDate() == null) {
                cv.setNullifiedTS(LocalDateTime.now());
                cv.setNullifiedUser(ua);
                System.out.println("CaseCoordinator.violation_NullifyCodeViolation: ready to nullify in integrator");
                ci.updateCodeViolation(cv);
            } else {
                throw new BObStatusException("Cannot nullify a violation that has compliance marked");

            }
        } else {
            throw new BObStatusException("Cannot nullify a violation with null CV or User");
        }
    }

    /**
     * Listener for user requests to reactivate a nullified violation
     *
     * @param cv
     * @param cse
     * @param ua
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public void violation_reactivateViolation(CodeViolation cv, CECase cse, UserAuthorized ua) throws BObStatusException, IntegrationException {
        if (cv == null || cv.getNullifiedTS() == null || cse == null || ua == null) {
            throw new BObStatusException("Cannot reactivate with null violation, null nullified ts, null case, or null user");
        }
        CaseIntegrator ci = getCaseIntegrator();
        cv.setNullifiedTS(null);
        cv.setNullifiedUser(null);
        ci.updateCodeViolation(cv);

    }

    /**
     * Wires up a given violation's findings to be the default findings for
     * violations of the given enforcable code element inside the CodeViolation
     * passed in TODO: Add option of checking that user's permissions and
     * updating all those muni's code sets as well
     *
     * @param cv
     * @param ua
     * @param cascadeToAuthCodebooks triggers the calling of an internal method
     * for updating all code books that are default in any muni in which the
     * UserAuthorized has enforcement official permissions (or better)
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public void violation_makeFindingsDefaultInCodebook(CodeViolation cv, UserAuthorized ua, boolean cascadeToAuthCodebooks) throws BObStatusException, IntegrationException {

        if (cv == null || ua == null) {
            throw new BObStatusException("Cannot update default findings with null violation, codeset, or user");
        }

        CodeCoordinator codeCoor = getCodeCoordinator();
        codeCoor.updateEnfCodeElementMakeFindingsDefault(cv.getDescription(), cv.getViolatedEnfElement(), ua);

    }
    
    /**
     * respond to a batch stip comp date request
     * @param cvlist
     * @param cse
     * @param ua
     * @param revStipCompDate
     * @throws ViolationException 
     */
    public void violation_extendStipulatedComplianceDateBatch(List<CodeViolation> cvlist, CECaseDataHeavy cse, UserAuthorized ua, LocalDateTime revStipCompDate) 
            throws BObStatusException, ViolationException, IntegrationException {
        if(cvlist == null 
                || cvlist.isEmpty() 
                || cse == null 
                || ua == null 
                || revStipCompDate == null 
                || revStipCompDate.isBefore(LocalDateTime.now())){
            throw new ViolationException("Cannot update batch with null list, case, user, newdate, or date in the past");
        }
        for(CodeViolation cv: cvlist){
            violation_extendStipulatedComplianceDate(cv, "Stip comp update", cse, ua);
        }
        
    }

  

    /**
     * Logic gateway for updates to a code violation's stipulated compliance
     * date
     *
     * @param cv with an updated stip date only. For other violation changes, see 
     * methods prefixed with violation_ in this Coordinator
     * @param extEvDesc
     * @param cse on which the code violation for update should be connected. Verification 
     * will occur
     * @param ua
     * @throws com.tcvcog.tcvce.domain.BObStatusException this violation doesn't go with this case
     * @throws com.tcvcog.tcvce.domain.ViolationException If stip comp date on or before origination date
     * @throws com.tcvcog.tcvce.domain.IntegrationException I can't write to the db
     */
    public void violation_extendStipulatedComplianceDate(CodeViolation cv, String extEvDesc, CECaseDataHeavy cse, UserAuthorized ua)
            throws BObStatusException, ViolationException, IntegrationException {

        if (cv == null || cse == null || ua == null) {
            throw new BObStatusException("Cannot extend compliance date given a null violation, case, or user");
        }

        if (cv.getStipulatedComplianceDate() == null) {
            throw new BObStatusException("Cannot extend a null stipulated compliance date");
        }

        if(cv.getStipulatedComplianceDate().isBefore(cv.getDateOfRecord())){
            throw new BObStatusException("Cannot update a violation to have a stip comp date before the violation's date of record.");
        }
        
        violation_updateCodeViolation(cse, cv, ua);
        prepareAndLogCodeViolationStipCompDateUpdateEvent(cv, extEvDesc, cse, ua);
    }
    
    /**
     * Logic and coordination block for creating a case event associated with the extension of a stip 
     * comp date.
     * @param cv
     * @param cse
     * @param ua
     * @return 
     */
    private int prepareAndLogCodeViolationStipCompDateUpdateEvent(CodeViolation cv, String extEvDesc, CECaseDataHeavy cse, UserAuthorized ua) throws BObStatusException, IntegrationException{
        
        System.out.println("CaseCoordinator.violation_extendStipulatedComplianceDate | done on cv " + cv.getViolationID());
        // now generate a note
        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setUser(ua);
        mbp.setHeader("Stipulated compliance date extended to: ");
        StringBuilder sb = new StringBuilder();
        sb.append("Previous stipulated compliance date of has been changed to ");
        sb.append(DateTimeUtil.getPrettyDate(cv.getStipulatedComplianceDate()));
        sb.append(".");

        mbp.setNewMessageContent(sb.toString());
        violation_updateNotes(mbp, cv);
//            
        return 0;
    }

    /**
     * CodeViolation should have the actual compliance date set from the user's
     * event date of record
     *
     * @param cse
     * @param cv
     * @param u
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.ViolationException
     * @throws com.tcvcog.tcvce.domain.EventException
     */
    public void violation_recordCompliance(CECaseDataHeavy cse, CodeViolation cv, UserAuthorized u)
            throws IntegrationException, BObStatusException, ViolationException, EventException {

        if (cse == null || cv == null || u == null) {
            throw new BObStatusException("Cannot record compliance on null case, viol, or user");
        }
        if (cv.getActualComplianceDate() == null) {
            throw new ViolationException("Cannot set compliance with null actual compliance date");
        }

        CaseIntegrator ci = getCaseIntegrator();

        // update violation record for compliance
        cv.setComplianceUser(u);
        cv.setLastUpdatedUser(u);

        ci.updateCodeViolationCompliance(cv);
        // Based on user feedback, we are suspending auto-closing of case
        // upon all violation coming into compliance
        // as of 2-DEC-2021

//        violation_checkForFullComplianceAndCloseCaseIfTriggered(cse, u);
    }

    /**
     *
     * @param cvIDList
     * @return
     * @throws IntegrationException
     */
    public List<CodeViolation> violation_getCodeViolations(List<Integer> cvIDList) throws IntegrationException, BObStatusException {
        List<CodeViolation> vl = new ArrayList<>();

        if (cvIDList != null && !cvIDList.isEmpty()) {
            for (Integer i : cvIDList) {
                vl.add(violation_getCodeViolation(i));
            }
        }

        return vl;

    }

} // close class
