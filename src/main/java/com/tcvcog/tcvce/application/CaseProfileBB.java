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
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EventCase;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.integration.CitationIntegrator;
import com.tcvcog.tcvce.integration.CodeViolationIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Eric C. Darsow
 */
public class CaseProfileBB extends BackingBeanUtils implements Serializable{

    private CECase currentCase;
    private CasePhase nextPhase;
    private CasePhase[] casePhaseList;
    private CasePhase selectedCasePhase;
    
    private EventCase eventForTriggeringCasePhaseAdvancement;
    
    private ArrayList<EventCase> eventList;
    private ArrayList<EventCase> filteredEventList;
    private EventCase selectedEvent;
    
    private ArrayList<CodeViolation> fullCaseViolationList;
    private ArrayList<CodeViolation> selectedViolations;
    
    private ArrayList<NoticeOfViolation> noticeList;
    private NoticeOfViolation selectedNotice;
    
    private ArrayList<Citation> citationList;
    private Citation selectedCitation;
    
    /**
     * Creates a new instance of CaseManageBB
     */
    public CaseProfileBB() {
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
        
        EventCoordinator ec = getEventCoordinator();
        System.out.println("CaseManageBB.recordCompliance | selectedViolations size: " + selectedViolations.size());
        if(!selectedViolations.isEmpty()){

            // generate event for compliance with selected violations
            EventCase e = ec.generateViolationComplianceEvent(selectedViolations);

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
        
        currentCase = getSessionBean().getActiveCase();

        System.out.println("CaseManageBB.createNewNoticeOfViolation | current case: " + currentCase);

        if(!fullCaseViolationList.isEmpty()){
            getSessionBean().setActiveViolationList(fullCaseViolationList);
            return "noticeOfViolationBuilder";
        }
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to generate new Notice of Violation, Sorry.", ""));
        return "";
    }
    
    public String createNewNoticeForSelectedViolations(){
        
        currentCase = getSessionBean().getActiveCase();
        
        System.out.println("CaseManageBB.createNewNoticeOfViolationForSelected | current case: " + currentCase);
        
        if(!fullCaseViolationList.isEmpty()){
            getSessionBean().setActiveViolationList(selectedViolations);
            return "noticeOfViolationBuilder";
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "You must select at least one violation from the list "
                                    + "to generate a letter from selected violations", ""));
            return "";
        }
    }
    
    
    public String createCitationForAllViolations(){
        System.out.println("CaseManageBB.createCitationForAllViolations | current case tostring: " 
                + currentCase);
//        if(!currentCase.getViolationList().isEmpty()){
        if(currentCase != null){
            
            CaseCoordinator cc = getCaseCoordinator();
            getSessionBean().setActiveCitation(cc.generateNewCitation(fullCaseViolationList));
            return "citationEdit";
        }
        getFacesContext().addMessage(null,
        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Please select a violation and try again", ""));
        return "";
    }
    
    public String createCitationFromSelected(){
        System.out.println("CaseManageBB.createCitationFromSelected");
        if(!selectedViolations.isEmpty()){
            
            CaseCoordinator cc = getCaseCoordinator();

            getSessionBean().setActiveCitation(cc.generateNewCitation(selectedViolations));

            return "citationEdit";
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
        // no logic needed in the backing bean
        // sinec we just forward to the selectElement page
        
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
     * @return the currentCase
     */
    public CECase getCurrentCase() {
        
        currentCase = getSessionBean().getActiveCase();
        if(currentCase != null){
            System.out.println("CaseManageBB.getCurrentCase | currentCase Info: " + currentCase.getCaseName());
            
        }
        return currentCase;
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
    public ArrayList<EventCase> getEventList() {
        eventList = currentCase.getEventList();
        return eventList;
    }

    /**
     * @return the selectedEvent
     */
    public EventCase getSelectedEvent() {
        return selectedEvent;
    }

    /**
     * @return the fullCaseViolationList
     */
    public ArrayList<CodeViolation> getFullCaseViolationList() {
        ViolationCoordinator vc = getViolationCoordinator();
        try {
            fullCaseViolationList = vc.getCodeViolations(currentCase);
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
    public ArrayList<CodeViolation> getSelectedViolations() {
        return selectedViolations;
    }

    /**
     * @param eventList the eventList to set
     */
    public void setEventList(ArrayList<EventCase> eventList) {
        this.eventList = eventList;
    }

    /**
     * @param selectedEvent the selectedEvent to set
     */
    public void setSelectedEvent(EventCase selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    /**
     * @param fullCaseViolationList the fullCaseViolationList to set
     */
    public void setFullCaseViolationList(ArrayList<CodeViolation> fullCaseViolationList) {
        this.fullCaseViolationList = fullCaseViolationList;
    }

    /**
     * @param selectedViolation the selectedViolation to set
     */
    public void setSelectedViolations(ArrayList<CodeViolation> selectedViolation) {
        this.selectedViolations = selectedViolation;
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
    public ArrayList<NoticeOfViolation> getNoticeList() {
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        
        try {
            noticeList = cvi.getNoticeOfViolationList(getSessionBean().getActiveCase());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return noticeList;
    }

    /**
     * @param noticeList the noticeList to set
     */
    public void setNoticeList(ArrayList<NoticeOfViolation> noticeList) {
        this.noticeList = noticeList;
    }

    /**
     * @return the citationList
     */
    public ArrayList<Citation> getCitationList() {
        CitationIntegrator ci = getCitationIntegrator();
        
        
        try {
            citationList = ci.getCitationsByCase(getSessionBean().getActiveCase());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        System.out.println("CaseManageBB.getCitationList | list size: " + citationList.size());
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
        this.citationList = citationList;
    }

    /**
     * @param selectedCitation the selectedCitation to set
     */
    public void setSelectedCitation(Citation selectedCitation) {
        this.selectedCitation = selectedCitation;
    }

   
    
    public String takeNextAction(){
        EventCase e = getEventForTriggeringCasePhaseAdvancement();
        
        getSessionBean().setActiveEvent(e);
        
        return "eventAdd";
    }

    /**
     * @return the eventForTriggeringCasePhaseAdvancement
     */
    public EventCase getEventForTriggeringCasePhaseAdvancement() {
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
    public void setEventForTriggeringCasePhaseAdvancement(EventCase eventForTriggeringCasePhaseAdvancement) {
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
        casePhaseList = CasePhase.values();
        return casePhaseList;
    }

    /**
     * @param casePhaseList the casePhaseList to set
     */
    public void setCasePhaseList(CasePhase[] casePhaseList) {
        this.casePhaseList = casePhaseList;
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
    public ArrayList<EventCase> getFilteredEventList() {
        return filteredEventList;
    }

    /**
     * @param filteredEventList the filteredEventList to set
     */
    public void setFilteredEventList(ArrayList<EventCase> filteredEventList) {
        this.filteredEventList = filteredEventList;
    }
}