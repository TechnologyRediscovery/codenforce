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


import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.ViolationCoordinator;
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.EventCECase;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.SearchParamsCECases;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.CitationIntegrator;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.CodeViolationIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Eric C. Darsow
 */
public class CaseProfileBB extends BackingBeanUtils implements Serializable{

    // deprecated member for use by CaseProfile.xhtml
    private CECase currentCase;
    private CasePhase nextPhase;
    private CasePhase[] casePhaseList;
    private CasePhase selectedCasePhase; 
    
    private List<CECase> caseList;
    private ArrayList<CECase> filteredCaseList;    
    private SearchParamsCECases ceCaseSearchParams;
    
    
    private ArrayList<CECase> filteredCaseHistoryList;
    private CECase selectedCase;
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
    private NoticeOfViolation selectedNotice;
    
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
    
    public void manageCECase(CECase c){
        // replace any session case with this one
        getSessionBean().setcECase(null);
        selectedCase = c;
    }
    
    
    
    
    public void changePACCAccess(){
        System.out.println("CEActionRequestsBB.changePACCAccess");
        CaseIntegrator ci = getCaseIntegrator();
        
        try {
            
            ci.updateCECaseMetadata(currentCase);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Done! Public access status is now: " + String.valueOf(selectedCase.isPaccEnabled()) +
                                " and action request forward linking is statusnow: " + String.valueOf(selectedCase.isAllowForwardLinkedPublicAccess())
                    + " for case ID: " + selectedCase.getCaseID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR
                    , "Unable to add change public access code status"
                    , getResourceBundle(Constants.MESSAGE_BUNDLE).getString("systemLevelError")));
        }
    }
    
    
    public String editEvent(EventCECase ev){
        getSessionBean().setActiveEvent(ev);
        return "eventEdit";
    }
    
    /**
     * 
     * @return 
     */
    public String overrideCasePhase(){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.manuallyChangeCasePhase(currentCase, selectedCasePhase);
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
    
    public String reloadPage(){
        return "";
    }
    
    
    
    public String recordCompliance() throws IntegrationException{
        CaseCoordinator cc = getCaseCoordinator();
        RoleType u = getFacesUser().getRoleType(); 
        
        EventCoordinator ec = getEventCoordinator();
        if(!selectedViolations.isEmpty()){

            // generate event for compliance with selected violations
            EventCECase e = ec.generateViolationComplianceEvent(selectedViolations);

            // when event is submitted, send violation list to c
            getSessionBean().setActiveEvent(e);
            getSessionBean().setActiveViolationList(selectedViolations);
            return "eventAdd";
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Please select a violation and try again", ""));
            return "";
        }
    }
    public String recordCompliance(CodeViolation cv) throws IntegrationException{
        CaseCoordinator cc = getCaseCoordinator();
        RoleType u = getFacesUser().getRoleType(); 
        
        EventCoordinator ec = getEventCoordinator();
            // generate event for compliance with selected violations
            EventCECase e = ec.generateViolationComplianceEvent(selectedViolations);

            // when event is submitted, send violation list to c
            getSessionBean().setActiveEvent(e);
            getSessionBean().setActiveViolationList(selectedViolations);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Please select a violation and try again", ""));
            return "eventAdd";
    }
    
    public String editViolation(){
        ArrayList<CodeViolation> ll = new ArrayList();
        
        if(!selectedViolations.isEmpty()){
            
            getSessionBean().setActiveCodeViolation(selectedViolations.get(0));
            return "violationEdit";
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Please select a violation and try again", ""));
            return "";
        }
    }
    
    public String createNewNoticeForAllViolations(){
        
        currentCase = getSessionBean().getcECase();

        System.out.println("CaseManageBB.createNewNoticeOfViolation | current case: " + currentCase);

        if(!fullCaseViolationList.isEmpty()){
            if(vListHasNoComplianceDates(fullCaseViolationList)){
                
            getSessionBean().setActiveViolationList(fullCaseViolationList);
            return "noticeOfViolationBuilder";
            } else {
                getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Yikes! You may not create a notice for violation "
                                + "with a compliance date!", ""));
                return "";
            }
        }
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to generate new Notice of Violation, Sorry.", ""));
        return "";
    }
    
    public String createNewNoticeForSelectedViolations(){
        
        currentCase = getSessionBean().getcECase();
        
        System.out.println("CaseManageBB.createNewNoticeOfViolationForSelected | current case: " + currentCase);
        
        if(!selectedViolations.isEmpty()){
            if(vListHasNoComplianceDates(selectedViolations)){
            getSessionBean().setActiveViolationList(selectedViolations);
            return "noticeOfViolationBuilder";
            } else {
                getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Yikes! You may not create a notice for a violation "
                                + "with a compliance date!", ""));
                return "";
            }
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "You must select at least one violation from the list "
                                    + "to generate a letter from selected violations", ""));
            return "";
        }
    }
    
    private boolean vListHasNoComplianceDates(List<CodeViolation> vList){
        Iterator<CodeViolation> it = vList.iterator();
        CodeViolation cv;
        boolean noComplianceDates = true;
        while(it.hasNext()){
            cv = it.next();
            if(cv.getActualComplianceDate() != null){
                noComplianceDates = false;
                break;
            }
        }
        return noComplianceDates;
    }
    
    
    public String createCitationForAllViolations(){
        System.out.println("CaseManageBB.createCitationForAllViolations | current case tostring: " 
                + currentCase);
//        if(!currentCase.getViolationList().isEmpty()){
        if(currentCase != null){
            if(vListHasNoComplianceDates(selectedViolations)){
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
    
    public String createCitationFromSelected(){
        System.out.println("CaseManageBB.createCitationFromSelected");
        if(!selectedViolations.isEmpty()){
            if(vListHasNoComplianceDates(selectedViolations)){
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
    
    public String updateCitation(){
        if(selectedCitation != null){
            
            getSessionBean().setActiveCitation(selectedCitation);
            return "citationEdit";
        }
        getFacesContext().addMessage(null,
        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Please select a citation and try again", ""));
        return "";
    }
    
    public String deleteCitation(){
        if(selectedCitation != null){
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
    

    public void deleteViolation(ActionEvent e){
        if(selectedViolations != null){
            if(selectedViolations.size() == 1){
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
    
    public String addViolation(){
        getSessionBean().setcECase(selectedCase);
        
        
        return "violationSelectElement";
    }
    
    public String editSelectedEvent(){

        if(selectedEvent != null){
            
            getSessionBean().setActiveEvent(selectedEvent);
            return "eventEdit";
        } else {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Please select an event to edit and try again", ""));
            return "";

            }
    }
    
    public String editNoticeOfViolation(){
        
        getSessionBean().setActiveNotice(selectedNotice);
        
        return "noticeOfViolationEditor";
    }
    
    
    public void deleteNoticeOfViolation(ActionEvent event){
        CaseCoordinator caseCoord = getCaseCoordinator();
        
        getSessionBean().setActiveNotice(selectedNotice);
        try {
            
            caseCoord.deleteNoticeOfViolation(selectedNotice);
            caseCoord.refreshCase(currentCase);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Notice no. " + selectedNotice.getNoticeID() + " has been nuked forever", ""));
            
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
    
    
    public String markNoticeOfViolationAsSent(){
        CaseCoordinator caseCoord = getCaseCoordinator();
        try {
            
            if(selectedNotice.getLetterSentDate() == null 
                    && selectedNotice.isRequestToSend() == true){
                caseCoord.markNoticeOfViolationAsSent(currentCase, selectedNotice);
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
        return "caseNotices";
    }
    
    public String markNoticeOfViolationAsReturned(){
        CaseCoordinator caseCoord = getCaseCoordinator();
        
        try {
        // check to make sure that the nootice has both been sent and not
        // marked as retuned
            if(selectedNotice.getLetterSentDate() != null 
                    && selectedNotice.getLetterReturnedDate() == null){

                caseCoord.processReturnedNotice(currentCase, selectedNotice);
                caseCoord.refreshCase(currentCase);

                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Notice no. " + selectedNotice.getNoticeID() 
                                + " has been marked as returned on today's date", ""));

            } else {
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Notice no. " + selectedNotice.getNoticeID() 
                        + " has either NOT been queued for sending "
                        + "(and therefore cant be returned) or has already been marked as returned", ""));
            }
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to mark notice as returned", ""));
        }
        return "caseNotices";
    }
    
    /**
     * @deprecated leftover from previous heavy case profile page
     * @return the currentCase
     */
    public CECase getCurrentCase() {
        
        currentCase = getSessionBean().getcECase();
        if(currentCase != null){
            System.out.println("CaseManageBB.getCurrentCase | currentCase Info: " + currentCase.getCaseName());
            
        }
        return currentCase;
    }

    /**
     * Used in the violation table to set the clicked violation row
     * as the system's current violation. At this stage, this feature
     * is used by the updateViolationsCodebookLink method only
     * 
     * Note that the member variable that this "setter" sets is
     * also the memvar that setSel3ectedViolation sets
     * @param cv the code violation clicked in the table
     */
    public void setActiveViolation(CodeViolation cv){
        selectedViolation = cv;
    }
    
    public void updateViolationsCodeBookLink(ActionEvent ae){
        CaseIntegrator casei = getCaseIntegrator();
        try {
            CodeViolationIntegrator cvi = getCodeViolationIntegrator();
            CodeIntegrator ci = getCodeIntegrator();
            EnforcableCodeElement ece = ci.getEnforcableCodeElement(newViolationCodeBookEleID);
            if(ece != null){
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
     * @param eventList the eventList to set
     */
    public void setEventList(ArrayList<EventCECase> eventList) {
        this.setEventList(eventList);
    }

    /**
     * @param selectedEvent the selectedEvent to set
     */
    public void setSelectedEvent(EventCECase selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    /**
     * @param fullCaseViolationList the fullCaseViolationList to set
     */
    public void setFullCaseViolationList(ArrayList<CodeViolation> fullCaseViolationList) {
        this.setFullCaseViolationList(fullCaseViolationList);
    }

    /**
     * @param selectedViolation the selectedViolation to set
     */
    public void setSelectedViolations(ArrayList<CodeViolation> selectedViolation) {
        this.setSelectedViolations(selectedViolation);
    }

    /**
     * @return the selectedNotice
     */
    public NoticeOfViolation getSelectedNotice() {
        return selectedNotice;
    }

    /**
     * @param selectedNotice the selectedNotice to set
     */
    public void setSelectedNotice(NoticeOfViolation selectedNotice) {
        this.selectedNotice = selectedNotice;
    }

    /**
     * @return the noticeList
     */
    public List<NoticeOfViolation> getNoticeList() {
        setNoticeList(getSessionBean().getcECase().getNoticeList());
        return noticeList;
    }

    /**
     * @param noticeList the noticeList to set
     */
    public void setNoticeList(ArrayList<NoticeOfViolation> noticeList) {
        this.setNoticeList(noticeList);
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
     * @param citationList the citationList to set
     */
    public void setCitationList(ArrayList<Citation> citationList) {
        this.setCitationList(citationList);
    }

    /**
     * @param selectedCitation the selectedCitation to set
     */
    public void setSelectedCitation(Citation selectedCitation) {
        this.selectedCitation = selectedCitation;
    }

   
    
    public String takeNextAction(){
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
     * @param eventForTriggeringCasePhaseAdvancement the eventForTriggeringCasePhaseAdvancement to set
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
        setCasePhaseList(CasePhase.values());
        return casePhaseList;
    }

    /**
     * @param casePhaseList the casePhaseList to set
     */
    public void setCasePhaseList(CasePhase[] casePhaseList) {
        this.setCasePhaseList(casePhaseList);
    }

    /**
     * @return the selectedCasePhase
     */
    public CasePhase getSelectedCasePhase() {
        return selectedCasePhase;
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
     * @param filteredEventList the filteredEventList to set
     */
    public void setFilteredEventList(ArrayList<EventCECase> filteredEventList) {
        this.setFilteredEventList(filteredEventList);
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
        if(caseList == null){
            ceCaseSearchParams.setMuni(getSessionBean().getActiveMuni());
            try {
                System.out.println("CaseProfileBB.getCaseList | getting list for : " + getSessionBean().getActiveMuni().getMuniName());
                caseList = ci.getCECases(ceCaseSearchParams);
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        }
        
        if(caseList == null){
            caseList = new ArrayList<>();
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
     * @return the selectedCase
     */
    public CECase getSelectedCase() {
        CECase sessionCase = getSessionBean().getcECase();
        if(sessionCase != null){
            selectedCase = sessionCase;
        }
        
        return selectedCase;
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
     * @param selectedCase the selectedCase to set
     */
    public void setSelectedCase(CECase selectedCase) {
        this.selectedCase = selectedCase;
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
     * @return the ceCaseSearchParams
     */
    public SearchParamsCECases getCeCaseSearchParams() {
        SearchCoordinator sc = getSearchCoordinator();
        if(ceCaseSearchParams == null){
            ceCaseSearchParams = sc.getDefaultSearchParamsCECase();
        }
        return ceCaseSearchParams;
    }

    /**
     * @param ceCaseSearchParams the ceCaseSearchParams to set
     */
    public void setCeCaseSearchParams(SearchParamsCECases ceCaseSearchParams) {
        this.ceCaseSearchParams = ceCaseSearchParams;
    }
}