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

import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.ViolationCoordinator;
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.CaseStage;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.EventCECase;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.search.SearchParamsCECases;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.CodeViolationIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

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

    private List<CECase> caseList;
    private ArrayList<CECase> filteredCaseList;
    private SearchParamsCECases searchParams;

    private ArrayList<CECase> filteredCaseHistoryList;
    private ArrayList<EventCECase> recentEventList;
    private ArrayList<Person> muniPeopleList;

    private EventCECase eventForTriggeringCasePhaseAdvancement;

    private List<EventCECase> eventList;
    private List<EventCECase> filteredEventList;
    private EventCECase selectedEvent;

    private List<CodeViolation> fullCaseViolationList;
    private List<CodeViolation> selectedViolations;
    private CodeViolation selectedViolation;
    private int newViolationCodeBookEleID;

    private List<NoticeOfViolation> noticeList;
    

    private List<Citation> citationList;
    private Citation selectedCitation;

    private HashMap<CasePhase, String> imageFilenameMap;
    private String phaseDiagramImageFilename;

    /**
     * Creates a new instance of CaseManageBB
     */
    public CaseProfileBB() {
        //TODO: move somewhere else! This is too hackey. Needs a resource bundle for
        // changing image IDs without recompiling!
        imageFilenameMap = new HashMap<>();
        imageFilenameMap.put(CasePhase.PrelimInvestigationPending, "stage1_prelim.svg");
        imageFilenameMap.put(CasePhase.NoticeDelivery, "stage1_notice.svg");
        imageFilenameMap.put(CasePhase.InitialComplianceTimeframe, "stage2_initial.svg");
        imageFilenameMap.put(CasePhase.SecondaryComplianceTimeframe, "stage2_secondary.svg");
        imageFilenameMap.put(CasePhase.AwaitingHearingDate, "stage3_awaiting.svg");
        imageFilenameMap.put(CasePhase.HearingPreparation, "stage3_prep.svg");
        imageFilenameMap.put(CasePhase.InitialPostHearingComplianceTimeframe, "stage3_postHearing.svg");
        imageFilenameMap.put(CasePhase.SecondaryPostHearingComplianceTimeframe, "stage3_postHearing.svg");
        imageFilenameMap.put(CasePhase.Closed, "stage3_closed.svg");
    }
    
    @PostConstruct
    public void initBean(){
        CaseCoordinator cc = getCaseCoordinator();
        searchParams = cc.getDefaultSearchParamsCECase(getSessionBean().getActiveMuni());
        List<CECase> retrievedCaseList = getSessionBean().getcECaseQueue();
        if(retrievedCaseList != null){
            currentCase = retrievedCaseList.get(0);
            caseList = retrievedCaseList;
        }
    }

    
    public void executeQuery(ActionEvent ev){
        System.out.println("CaseProfileBB.executeQuery");
        CaseCoordinator cc = getCaseCoordinator();
        int listSize = 0;
        try {
            caseList = cc.queryCECases(searchParams);
            if(caseList != null){
                listSize = caseList.size();
            }
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Your query completed with " + listSize + " results", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not query the database, sorry.", ""));
        }
        
    }
    
    
/**
 * Primary injection point for setting the case which will be displayed in the right
 * column (the manage object column) on cECases.xhtml
 * @param c the case to be managed--comes from the data table row button
 */
    public void manageCECase(CECase c) {
        UserIntegrator ui = getUserIntegrator();
        getSessionBean().setcECase(c);
        try {
            ui.logObjectView(getSessionBean().getFacesUser(), c);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        currentCase = c;
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

  

    /**
     *
     * @return
     */
    public String overrideCasePhase() {
        CaseCoordinator cc = getCaseCoordinator();
        CaseIntegrator ci = getCaseIntegrator();
        try {
            cc.manuallyChangeCasePhase(currentCase, getSelectedCasePhase());
            currentCase = ci.getCECase(currentCase.getCaseID());
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to write case phase changes to DB",
                            "This error must be corrected by a system administrator, sorry"));
        } catch (CaseLifecyleException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to change case phase due to a case lifecycle exception",
                            "Please check with your system administrator"));

        }
        
        return "";
    }

    public String reloadPage() {
        return "";
    }
    
    public void refreshCurrentCase(ActionEvent ev){
        CaseIntegrator ci = getCaseIntegrator();
        try {
            currentCase = ci.getCECase(currentCase.getCaseID());
            System.out.println("CaseProfileBB.refreshCurrentCase");
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
    }

    public void recordCompliance(CodeViolation cv) throws IntegrationException {
        EventCoordinator ec = getEventCoordinator();
        // build event details 
        EventCECase e = ec.generateViolationComplianceEvent(cv);
        // put our violation on its session shelf for the eventAddBB
        getSessionBean().setActiveCodeViolation(cv);
        // put our event on its session shelf for the eventAddBB
        getSessionBean().setActiveEvent(e);
        getSessionBean().setcECase(currentCase);
    }

    public String editViolation() {
        ArrayList<CodeViolation> ll = new ArrayList();

        if (!selectedViolations.isEmpty()) {

            getSessionBean().setActiveCodeViolation(selectedViolations.get(0));
            return "violationEdit";
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a violation and try again", ""));
            return "";
        }
    }

    public String createNewNotice() {

        List<CodeViolation> retrievedList = currentCase.getViolationList();
        
        if (retrievedList != null) {
            if (retrievedList.size() > 0) {
                getSessionBean().setViolationQueue(retrievedList);
                getSessionBean().setActiveProp(currentCase.getProperty());
                getSessionBean().setcECase(currentCase);
            }
            return "noticeOfViolationBuilder";

        } else {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Yikes! No outstanding violations exist for building a letter"
                    + "with a compliance date!", ""));
        }

        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Unable to generate new Notice of Violation, Sorry.", ""));
        return "";
    }

    private boolean checkViolationListForNoComplianceDates(List<CodeViolation> vList) {
        Iterator<CodeViolation> it = vList.iterator();
        CodeViolation cv;
        boolean noComplianceDates = true;
        while (it.hasNext()) {
            cv = it.next();
            if (cv.getActualComplianceDate() != null) {
                noComplianceDates = false;
                break;
            }
        }
        return noComplianceDates;
    }

    public void resetNotice(NoticeOfViolation nov){
        CaseCoordinator cc = getCaseCoordinator();
        CaseIntegrator ci = getCaseIntegrator();
        try {
            cc.resetNOVMailing(getSessionBean().getcECase(), nov);
            //reset case
            getSessionBean().setcECase(ci.getCECase(currentCase.getCaseID()));
            getFacesContext().addMessage(null,
                     new FacesMessage(FacesMessage.SEVERITY_INFO,
                             "Notice mailing status has been reset", ""));
        } catch (IntegrationException ex) {
               getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Yikes! Could not reset the violation's mailing status. Sorry!", 
                                "This is a system-level error"));
        }
    }
   

    public String createCitationForAllViolations() {
        System.out.println("CaseManageBB.createCitationForAllViolations | current case tostring: "
                + currentCase);
//        if(!currentCase.getViolationList().isEmpty()){
        if (currentCase != null) {
            if (checkViolationListForNoComplianceDates(selectedViolations)) {
                CaseCoordinator cc = getCaseCoordinator();
                getSessionBean().setActiveCitation(cc.generateNewCitation(fullCaseViolationList));
                return "citationEdit";
            } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Yikes! You may not cite a violation with a compliance date!", ""));
                return "";
            }
        }
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Please select a violation and try again", ""));
        return "";
    }

    public String createCitationFromSelected() {
        System.out.println("CaseManageBB.createCitationFromSelected");
        if (!selectedViolations.isEmpty()) {
            if (checkViolationListForNoComplianceDates(selectedViolations)) {
                CaseCoordinator cc = getCaseCoordinator();
                getSessionBean().setActiveCitation(cc.generateNewCitation(selectedViolations));
                return "citationEdit";
            } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Yikes! You may not cite a violation with a compliance date!", ""));
                return "";
            }
        }
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Please select a violation and try again", ""));
        return "";
    }

    public String updateCitation() {
        if (selectedCitation != null) {

            getSessionBean().setActiveCitation(selectedCitation);
            return "citationEdit";
        }
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Please select a citation and try again", ""));
        return "";
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
        if (selectedViolations != null) {
            if (selectedViolations.size() == 1) {
                ViolationCoordinator vc = getViolationCoordinator();
                try {
                    vc.deleteViolation(selectedViolations.get(0));
                } catch (IntegrationException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Unable to delete selected violation",
                                    "Check if the violation has been referenced in a citation."
                                    + "If so, and you still wish to delete, you must remove"
                                    + "the citation first, then delete the violation."));
                }
            } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Don't get wreckless, now! You may only delete one violation at a time!", ""));

            }
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a violation and try again", ""));
        }
    }

    public String addViolation() {
        getSessionBean().setcECase(currentCase);

        return "violationSelectElement";
    }

    
    public String printNotice(NoticeOfViolation nov){
        System.out.println("CaseProfileBB.printNotice");
        getSessionBean().setActiveNotice(nov);
        return "noticeOfViolationPrint";
    }

    public String editNoticeOfViolation(NoticeOfViolation nov) {
        getSessionBean().setActiveNotice(nov);
        return "noticeOfViolationEditor";
    }
    
    
    public void queueNotice(NoticeOfViolation nov){
        System.out.println("CaseProfileBB.QueueNotice");
        CaseCoordinator caseCoord = getCaseCoordinator();
        
//        CECase ceCase = getSessionBean().getcECase();

        
//        NoticeOfViolation notice = caseCoord.generateNoticeSkeleton(ceCase);
        
        try {
            
            caseCoord.queueNoticeOfViolation(currentCase, nov);
            
        } catch (CaseLifecyleException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to deploy notice due to a business process corruption hazard. "
                                + "Please make a notice event to discuss with Eric and Team", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to update case phase due to a database connectivity error",
                        "this issue must be corrected by a system administrator, sorry"));
        } catch (EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, 
                        "The automatic event generation associated with this action has thrown an error. "
                                + "Please create an event manually which logs this letter being queued for mailing", ""));
            
        }
        
        
    }
    

    public void deleteNoticeOfViolation(NoticeOfViolation nov) {
        CaseCoordinator caseCoord = getCaseCoordinator();

        getSessionBean().setActiveNotice(nov);
        try {

            caseCoord.deleteNoticeOfViolation(nov);
            caseCoord.refreshCase(currentCase);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Notice no. " + nov.getNoticeID() + " has been nuked forever", ""));
            caseCoord.refreshCase(currentCase);

        } catch (CaseLifecyleException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to delete this notice of violation, "
                            + "probably because it has been sent already", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to refresh case, Sorry. Please try reselecting the case from the case listing", ""));

        }
    }

    public void markNoticeOfViolationAsSent(NoticeOfViolation nov) {
        CaseCoordinator caseCoord = getCaseCoordinator();
        try {
                if (nov.getLetterSentDate() == null
                        && nov.isRequestToSend() == true) {
                    caseCoord.markNoticeOfViolationAsSent(currentCase, nov);
                    caseCoord.refreshCase(currentCase);
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "Marked notice as sent and added event to case",
                                    ""));

                } else {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN,
                                    "Oops! The letter you selected has either "
                                    + "NOT been queued for sending or has ALREADY been marked as sent",
                                    ""));
                }
            

        } catch (CaseLifecyleException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.toString(), "This must be corrected by a "
                            + "system administrator, sorry"));

        } catch (EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to generate case event to log phase change",
                            "Note that because this message is being displayed, the phase change"
                            + "has probably succeeded"));

        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to mark selected notice as sent",
                            ""));

        } // close try/cathc section
    }

    public void markNoticeOfViolationAsReturned(NoticeOfViolation nov) {
        CaseCoordinator caseCoord = getCaseCoordinator();

        try {
            // check to make sure that the nootice has both been sent and not
            // marked as retuned
            if (nov.getLetterSentDate() != null
                    && nov.getLetterReturnedDate() == null) {

                caseCoord.processReturnedNotice(currentCase, nov);
                caseCoord.refreshCase(currentCase);

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Notice no. " + nov.getNoticeID()
                                + " has been marked as returned on today's date", ""));

            } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Notice no. " + nov.getNoticeID()
                                + " has either NOT been queued for sending "
                                + "(and therefore cant be returned) or has already been marked as returned", ""));
            }
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to mark notice as returned", ""));
        }
    }

    /**
     * @return the currentCase
     */
    public CECase getCurrentCase() {
        CaseIntegrator caseint = getCaseIntegrator();
        CECase sessionCase = getSessionBean().getcECase();
        if (sessionCase != null) {
            currentCase = sessionCase;
        } else {
            if (currentCase != null) {
                try {
                    // most desirably, we've got a current case, so reload it
                    currentCase = caseint.getCECase(currentCase.getCaseID());
                } catch (IntegrationException ex) {
                    System.out.println(ex);
                }
            } else {
                try {
                    // otherwise, get an arbitrary case
                    currentCase = caseint.getCECase(
                            Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("arbitraryPlaceholderCaseID")));
                } catch (IntegrationException ex) {
                    System.out.println(ex);
                }
            }

        }
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

    public void updateViolationsCodeBookLink(ActionEvent ae) {
        CaseIntegrator casei = getCaseIntegrator();
        try {
            CodeViolationIntegrator cvi = getCodeViolationIntegrator();
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
            Logger.getLogger(CaseProfileBB.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * @param currentCase the currentCase to set
     */
    public void setCurrentCase(CECase currentCase) {
        this.currentCase = currentCase;
    }

    /**
     * @return the eventList
     */
    public List<EventCECase> getEventList() {
        setEventList(currentCase.getEventList());
        return eventList;
    }

    /**
     * @return the selectedEvent
     */
    public EventCECase getSelectedEvent() {
        return selectedEvent;
    }

    /**
     * @return the fullCaseViolationList
     */
    public List<CodeViolation> getFullCaseViolationList() {
        ViolationCoordinator vc = getViolationCoordinator();
        try {
            setFullCaseViolationList((List<CodeViolation>) vc.getCodeViolations(currentCase));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to load code violation list",
                            "This is a system-level error that msut be corrected by an administrator, Sorry!"));

        }
        return fullCaseViolationList;
    }

    /**
     * @return the selectedViolation
     */
    public List<CodeViolation> getSelectedViolations() {
        return selectedViolations;
    }

    /**
     * @param el
     */
    public void setEventList(ArrayList<EventCECase> el) {
        eventList = el;
    }

    /**
     * @param selectedEvent the selectedEvent to set
     */
    public void setSelectedEvent(EventCECase selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    /**
     * @param fcvll
     */
    public void setFullCaseViolationList(ArrayList<CodeViolation> fcvll) {
        fullCaseViolationList = fcvll;
    }

    /**
     * @param svs
     */
    public void setSelectedViolations(ArrayList<CodeViolation> svs) {
        selectedViolations = svs;
    }

   

    /**
     * @return the noticeList
     */
    public List<NoticeOfViolation> getNoticeList() {
        setNoticeList(getSessionBean().getcECase().getNoticeList());
        return noticeList;
    }

    /**
     * @param nl
     */
    public void setNoticeList(ArrayList<NoticeOfViolation> nl) {
        noticeList = nl;
    }

    /**
     * @return the citationList
     */
    public List<Citation> getCitationList() {

        setCitationList(getSessionBean().getcECase().getCitationList());
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
    public void setCitationList(ArrayList<Citation> cl) {
        citationList = cl;
    }

    /**
     * @param selectedCitation the selectedCitation to set
     */
    public void setSelectedCitation(Citation selectedCitation) {
        this.selectedCitation = selectedCitation;
    }

    public String takeNextAction() {
        EventCECase e = getEventForTriggeringCasePhaseAdvancement();

        getSessionBean().setActiveEvent(e);

        return "eventAdd";
    }

    /**
     * @return the eventForTriggeringCasePhaseAdvancement
     */
    public EventCECase getEventForTriggeringCasePhaseAdvancement() {
        EventCoordinator ec = getEventCoordinator();

        try {
            eventForTriggeringCasePhaseAdvancement = ec.getActionEventForCaseAdvancement(currentCase);
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error connecting to DB. This must be corrected by a system administrator", ""));

        } catch (CaseLifecyleException ex) {
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
    public void setEventForTriggeringCasePhaseAdvancement(EventCECase eventForTriggeringCasePhaseAdvancement) {
        this.eventForTriggeringCasePhaseAdvancement = eventForTriggeringCasePhaseAdvancement;
    }

    /**
     * @return the nextPhase
     */
    public CasePhase getNextPhase() {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            nextPhase = cc.getNextCasePhase(currentCase);
        } catch (CaseLifecyleException ex) {
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
    public List<EventCECase> getFilteredEventList() {
        return filteredEventList;
    }

    /**
     * @param fel
     */
    public void setFilteredEventList(ArrayList<EventCECase> fel) {
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
//        List<CECase> sessionList = getSessionBean().getcECaseList();
        CaseIntegrator ci = getCaseIntegrator();
        if (caseList == null) {
            searchParams.setMuni(getSessionBean().getActiveMuni());
            try {
                System.out.println("CaseProfileBB.getCaseList | getting list for : " + getSessionBean().getActiveMuni().getMuniName());
                caseList = ci.getCECases(searchParams);
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        }


        return caseList;
    }

    /**
     * @return the filteredCaseList
     */
    public ArrayList<CECase> getFilteredCaseList() {
        return filteredCaseList;
    }

    /**
     * @return the filteredCaseHistoryList
     */
    public ArrayList<CECase> getFilteredCaseHistoryList() {
        return filteredCaseHistoryList;
    }

    /**
     * @return the recentEventList
     */
    public ArrayList<EventCECase> getRecentEventList() {
        return recentEventList;
    }

    /**
     * @return the muniPeopleList
     */
    public ArrayList<Person> getMuniPeopleList() {
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
    public void setCaseList(ArrayList<CECase> caseList) {
        this.caseList = caseList;
    }

    /**
     * @param filteredCaseList the filteredCaseList to set
     */
    public void setFilteredCaseList(ArrayList<CECase> filteredCaseList) {
        this.filteredCaseList = filteredCaseList;
    }

    /**
     * @param filteredCaseHistoryList the filteredCaseHistoryList to set
     */
    public void setFilteredCaseHistoryList(ArrayList<CECase> filteredCaseHistoryList) {
        this.filteredCaseHistoryList = filteredCaseHistoryList;
    }

    /**
     * @param recentEventList the recentEventList to set
     */
    public void setRecentEventList(ArrayList<EventCECase> recentEventList) {
        this.recentEventList = recentEventList;
    }

    /**
     * @param muniPeopleList the muniPeopleList to set
     */
    public void setMuniPeopleList(ArrayList<Person> muniPeopleList) {
        this.muniPeopleList = muniPeopleList;
    }

    /**
     * @param eventList the eventList to set
     */
    public void setEventList(List<EventCECase> eventList) {
        this.eventList = eventList;
    }

    /**
     * @param filteredEventList the filteredEventList to set
     */
    public void setFilteredEventList(List<EventCECase> filteredEventList) {
        this.filteredEventList = filteredEventList;
    }

    /**
     * @param fullCaseViolationList the fullCaseViolationList to set
     */
    public void setFullCaseViolationList(List<CodeViolation> fullCaseViolationList) {
        this.fullCaseViolationList = fullCaseViolationList;
    }

    /**
     * @param selectedViolations the selectedViolations to set
     */
    public void setSelectedViolations(List<CodeViolation> selectedViolations) {
        this.selectedViolations = selectedViolations;
    }

    /**
     * @param noticeList the noticeList to set
     */
    public void setNoticeList(List<NoticeOfViolation> noticeList) {
        this.noticeList = noticeList;
    }

    /**
     * @param citationList the citationList to set
     */
    public void setCitationList(List<Citation> citationList) {
        this.citationList = citationList;
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
    public SearchParamsCECases getSearchParams() {
        
        return searchParams;
    }

    /**
     * @param searchParams the searchParams to set
     */
    public void setSearchParams(SearchParamsCECases searchParams) {
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
}
