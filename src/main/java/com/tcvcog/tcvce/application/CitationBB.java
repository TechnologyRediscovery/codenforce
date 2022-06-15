/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CitationCodeViolationLink;
import com.tcvcog.tcvce.entities.CitationDocketRecord;
import com.tcvcog.tcvce.entities.CitationFilingType;
import com.tcvcog.tcvce.entities.CitationStatus;
import com.tcvcog.tcvce.entities.CitationStatusLogEntry;
import com.tcvcog.tcvce.entities.CitationViolationStatusEnum;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.CourtEntity;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.HumanLink;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.CourtEntityIntegrator;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 * Manages citation operations that are attached to CECases
 * @author sylvia
 */
public class CitationBB extends BackingBeanUtils {

    private Citation currentCitation;
    
    private List<CitationFilingType> citationFilingTypeList;

    private boolean issueCitationDisabled;
    private boolean updateCitationDisabled;
    private String formNoteCitationText;
    private User citationIssuingOfficer;
    
    // CITATION - VIOLATION LINKS
    private List<CodeViolation> removedViolationList;
    private String citationEditEventDescription;
    private CitationCodeViolationLink currentCitationViolationLink;
    private String formCitationViolationLinkNotes;
    private List<CitationViolationStatusEnum> citationViolationStatusEnumList;
    
    
    
    //    DOCKETS 
    private boolean citationDocketEditMode;
    private CitationDocketRecord currentCitationDocket;
    private List<CourtEntity> courtEntityList;
    private String docketNotesFormText;
    
    //    STATUS LOGS
    private boolean citationStatusEditMode;
    private CitationStatusLogEntry currentCitationStatusLogEntry;
    private List<CitationStatus> citationStatusList;
    private String statusNotesFormText;
    
    private boolean citationInfoEditMode;
    
    /**
     * Creates a new instance of CitationBB
     */
    public CitationBB() {
    }
    
    
    @PostConstruct
    public void initBean()  {
        System.out.println("CitationBB.initBean()");
        CaseCoordinator cc = getCaseCoordinator();
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        UserCoordinator uc = getUserCoordinator();
        // Citation stuff
        
        
        try {
            citationStatusList = cc.citation_getCitationStatusList();
            courtEntityList = cei.getCourtEntityList();
            citationFilingTypeList = cc.citation_getCitationFilingTypeList();
            
        } catch (IntegrationException | BObStatusException  ex) {
            System.out.println(ex);
        }
        
        setRemovedViolationList(new ArrayList<>());
        
        // set edit defaults
        citationStatusEditMode = false;
        citationDocketEditMode = false;
        citationInfoEditMode = false;
        citationViolationStatusEnumList = Arrays.asList(CitationViolationStatusEnum.values());
        
    }
    
    public void onCitationViewButtonChange(Citation cit){
        currentCitation = cit;
    }
    
    /**
     * Generic cancellation event listener
     * @param ev 
     */
    public void onOperationCancelButtonChange(ActionEvent ev){
        System.out.println("CitationBB.onOperationCancelButtonChange");
        
    }
    
    
    /*******************************************************
    /*******************************************************
     **              Citations GENERAL                    **
    /*******************************************************/
    /*******************************************************/
    
    /**
     * Special getter wrapper for citation blobs that responds to
     * the bl ob tools update field
     * @return 
     */
    public List<BlobLight> getCitationBlobsAutoUpdated(){
       List<BlobLight> sessBlobListForUpdate = getSessionBean().getSessBlobLightListForRefreshUptake();
        if(sessBlobListForUpdate != null && currentCitation != null){
            System.out.println("CECaseSearchProfileBB.getBlobLightListFromCECase | found non-null session blob list for uptake: " + sessBlobListForUpdate.size());
            getCurrentCitation().setBlobList(sessBlobListForUpdate);
            // clear session since we have the new list
            getSessionBean().setSessBlobLightListForRefreshUptake(null);
            return sessBlobListForUpdate;
        } else {
            if(currentCitation.getBlobList() != null){
                return currentCitation.getBlobList();
            } else {
                return new ArrayList<>();
            }
        }
        
    }
    
    /**
     * Listener for requests to refresh the current citation
     */
    public void refreshCurrentCitation(){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            currentCitation = cc.citation_getCitation(currentCitation.getCitationID());
        } catch (IntegrationException | BObStatusException | BlobException ex) {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
        
    }
    
    
    /**
     * Internal method for telling the session bean that
     * the cecase profile should refresh its cecase
     */
    private void triggerParentCECaseReload(){
        getSessionBean().setSessCECaseRefreshTrigger(LocalDateTime.now());
    }
    
    /**
     * Listener for user requests to edit a citation's info
     */
   public void onCitationEditModeToggle(){
        if(citationInfoEditMode){
            onCitationUpdateCommitButtonChange(null);
            refreshCurrentCitation();
        }
        citationInfoEditMode = !citationInfoEditMode;
       System.out.println("CitationBB.onCitationEditModeToggle: End of method citationInfoEditMode is: " + citationInfoEditMode);
   } 
   
   /**
    * listener for user requests to abort edits of citation info
    * @param ev 
    */
   public void onCitationEditAbortButtonChange(ActionEvent ev){
       citationInfoEditMode = false;
       
   }
    
   /**
    * listener for user requests to start a new citation
    * @param ev 
    */
    public void onCitationAddInitButtonChange(ActionEvent ev){
          CaseCoordinator cc = getCaseCoordinator();
        
        try {
            currentCitation = cc.citation_getCitationSkeleton(getSessionBean().getSessCECase(), getSessionBean().getSessUser(), citationIssuingOfficer );
           
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
            
        }
        
    }
    
      /**
     * Listener for user requests to issue a citation
     *
     * @param ev
     */
    public void onCitationAddCommitButtonChange(ActionEvent ev) {
        System.out.println("CitationBB.onCitationAddCommitButtonChange");
        CaseCoordinator cc = getCaseCoordinator();
        if(currentCitation != null){
            try {
                cc.citation_insertCitation(currentCitation, getSessionBean().getSessUser());
                refreshCurrentCitation();
                triggerParentCECaseReload();
                onStatusLogEntryAddInitButtonChange(null);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "New citation added to database!", ""));
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Please make a citation status log entry now.", ""));
            } catch (IntegrationException | BObStatusException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to issue citation due to a database integration error", ""));
                System.out.println(ex);
            }
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to issue citation due to page object error", ""));
        }
    }
    
     /**
     * Listener for user requests to update the current citation.
     * Only accessible from a citation profile, so no need for
     * the Citation to come as an input param
     */
    public void onCitationUpdateInitButtonChange(){
        
        // TODO: Check logic on citation to see for allowable updates
        
    }
    
      /**
     * Listener for user requests to commit
     * @param ev citation updates
     *
     */
    public void onCitationUpdateCommitButtonChange(ActionEvent ev) {
        System.out.println("CitationBB.updateCitation");
        CaseCoordinator cc = getCaseCoordinator();

        try {
            cc.citation_updateCitation(getCurrentCitation());
        } catch (IntegrationException | BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
            System.out.println(ex);
            
        }
    }
    
    /**
     * Listener for user requests to start the citation removal process
     * @param ev
     */
    public void onCitationRemoveInitButtonChange(ActionEvent ev){
        System.out.println("CitationBB.onCitationRemoveInitButtonChange");
    }
    
     /**
     * Listener for user requests to remove a citation
     *
     * @param ev
     */
    public void onCitationRemoveCommitButtonChange(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.citation_removeCitation(currentCitation, getSessionBean().getSessUser());
            refreshCurrentCitation();
            triggerParentCECaseReload();
                    
        } catch (IntegrationException | BObStatusException | AuthorizationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }

    }
    
    /**
     * Listener for user requests to start the note process
     * for a specific citation
     */
    public void onCitationNoteInitButtonChange(){
        formNoteCitationText = "";
        
    }
    
    /**
     * Listener for user requests to complete the citation
     * note add operation
     * @param ev 
     */
    public void onCitationNoteCommitButtonChange(ActionEvent ev){
        
         CaseCoordinator cc = getCaseCoordinator();

        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setCred(getSessionBean().getSessUser().getKeyCard());
        mbp.setExistingContent(getCurrentCitation().getNotes());
        mbp.setNewMessageContent(formNoteCitationText);
        mbp.setHeader("Citation Note");
        mbp.setUser(getSessionBean().getSessUser());

        try {

            cc.citation_updateNotes(mbp, getCurrentCitation());
            refreshCurrentCitation();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succesfully appended note!", ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fatal error appending note; apologies!", ""));
        }
        
    }
    
    
     
   
    
    /* **********************************************/
    /* ********** CITATION VIOLATION TOODL **********/
    /* **********************************************/
    
    /**
     * Listener for user requests to remove a violation from a citation
     * @param v 
     */
    public void onCitationViolationRemoveButtonChange(CitationCodeViolationLink v) {
        System.out.println("CitationBB.onCitationViolationRemoveButtonChange | CITV ID: " + v.getCitationViolationID());
        getCurrentCitation().getViolationList().remove(v);
        getRemovedViolationList().add(v);
    }

    /**
     * Listener for user requests to add a violation to a citation
     * @param v 
     */
    public void onCitationViolationRestoreButtonChange(CitationCodeViolationLink v) {
        System.out.println("CitationBB.onCitationViolationRestoreButtonChange | CITV ID: " + v.getCitationViolationID());
        getCurrentCitation().getViolationList().add(v);
        getRemovedViolationList().remove(v);
    }
    
    
    
    /**
     * Listener for user requests to start the status update for a citation
     * violation 
     * @param ccvl 
     */
    public void onCitationViolationStatusUpdateInit(CitationCodeViolationLink ccvl){
        currentCitationViolationLink = ccvl;
        formCitationViolationLinkNotes = "";
        
    }
    
    /**
     * Listener for users who are done with updating a citation violation status
     * and I'll automatically append the update and notes to the notes column
     * @param ev 
     */
    public void onCitationViolationStatusUpdateCommit(ActionEvent ev){
        SystemCoordinator sc = getSystemCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        if(currentCitationViolationLink != null){
            try {
                cc.citation_updateCitationCodeViolationLink(currentCitationViolationLink, formCitationViolationLinkNotes, getSessionBean().getSessUser());
                refreshCurrentCitation();
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Updated citation violation status!", ""));
            } catch (BObStatusException | IntegrationException ex) {
                System.out.println(ex);
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fatal error updating citation-violation link", ""));
            } 
        }
    }
    
   /**
    * Listener to view the history of the citation violation link
    * @param ccvl 
    */
    public void onCitationViolationView(CitationCodeViolationLink ccvl){
        currentCitationViolationLink = ccvl;
        
    }
   
     
   
    
    /* **********************************************/
    /* ********** CITATION STATUS LOGS **************/
    /* **********************************************/
    
    /**
     * Listener for user requests to view the citation status log entry
     * @param csle 
     */
    public void onCitationStatusLogEntryViewButtonChange(CitationStatusLogEntry csle){
        System.out.println("CitationBB.onCitationStatusLogEntryViewButtonChange | Viewing CSLE ID " + csle.getLogEntryID());
        currentCitationStatusLogEntry = csle;
    }
    
    /**
     * Listener for user requests to view the citation status log entry
     * @param csle 
     */
    public void onCitationStatusLogEntryEditButtonChange(CitationStatusLogEntry csle){
        System.out.println("CitationBB.onCitationStatusLogEntryEditButtonChange | Viewing CSLE ID " + csle.getLogEntryID());
        citationStatusEditMode = true;
        currentCitationStatusLogEntry = csle;
    }
    
    
    /**
     * Listener for user requests to edit a citation's status record 
     */
   public void onCitationStatusLogEditModeToggle(){
        // clicking done means save edits
        if(citationStatusEditMode){
            if(currentCitationStatusLogEntry != null){
                if(currentCitationStatusLogEntry.getLogEntryID() == 0){
                    onStatusLogEntryAddCommitButtonChange(null);
                    System.out.println("citationBB.onStatusLogEntryAddCommitButtonChange | called on status edit mode toggle");
                } else {
                    onStatusLogEntryEditCommitButtonChange(null);
                    System.out.println("citationBB.onStatusLogEntryEditCommitButtonChange | called on status edit mode toggle");
                }
                refreshCurrentCitation();
                triggerParentCECaseReload();
            }
        }
        citationStatusEditMode = !citationStatusEditMode;
        System.out.println("CitationBB.onCitationStatusLogEditModeToggle | citationStatusEditMode val is " + citationStatusEditMode);
   } 
   
   /**
    * Listener for methods to abort the edit process
    */
   public void onCitationStatusLogEditAbortListener(){
       citationStatusEditMode = false;
   }
   
  
   /**
    * Listener for user requests to start the status log entry process
    * @param ev 
    */
   public void onStatusLogEntryAddInitButtonChange(ActionEvent ev){
       CaseCoordinator cc = getCaseCoordinator();
       currentCitationStatusLogEntry = cc.citation_getStatusLogEntrySkeleton(currentCitation);
       citationStatusEditMode = true;
   }
   
   
   
   /**
    * Listener for user requests to finalize their new log entry
    * @param ev 
    */
   public void onStatusLogEntryAddCommitButtonChange(ActionEvent ev){
       CaseCoordinator cc = getCaseCoordinator();
       
        try {
            int freshID = cc.citation_insertCitationStatusLogEntry(currentCitation, currentCitationStatusLogEntry, getSessionBean().getSessUser());
            currentCitationStatusLogEntry =cc.citation_getCitationStatusLogEntry(freshID);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Created log entry with ID " + freshID, ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fatal error! Unable to insert new log entry.", ""));
        } 
       
   }
   
       
    /**
     * Listener for user requests to commit edits to the current log entry
     * @param ev 
     */
    public void onStatusLogEntryEditCommitButtonChange(ActionEvent ev){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.citation_updateCitationStatusLogEntry(currentCitation, currentCitationStatusLogEntry, getSessionBean().getSessUser());
            currentCitationStatusLogEntry = cc.citation_getCitationStatusLogEntry(currentCitationStatusLogEntry.getLogEntryID());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Updated log entry ID: " + currentCitationStatusLogEntry.getLogEntryID(), ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
            
        }
    }
    
    /**
     * Listener for user requests to start the log entry removal process
     * @param ev 
     */
    public void onStatusLogEntryRemoveInitButtonChange(ActionEvent ev){
        // Nothing to do here yet
        
        
    }
    
    /**
     * Listener for user requests to finalize the log entry removal process
     * @param ev 
     */
    public void onStatuslogEntryRemoveCommitButtonChange(ActionEvent ev){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.citation_deactivateCitationStatusLogEntry(currentCitationStatusLogEntry, getSessionBean().getSessUser());
            refreshCurrentCitation();
            triggerParentCECaseReload();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                           "Success! Removed citation status log entry ID " + currentCitationStatusLogEntry.getLogEntryID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
        
    }
    
    /**
     * listener for user requests to start to add note to a status log entry
     */
    public void onStatusLogNoteInitButtonChange(){
        statusNotesFormText = "";
        
        
    }
    
    /**
     * Listener for user requests to complete note process for status logs
     */
    public void onStatusLogNoteCommitButtonChange(){
        SystemCoordinator sc = getSystemCoordinator();
        
        MessageBuilderParams mbp = new MessageBuilderParams();
        
        mbp.setCred(getSessionBean().getSessUser().getKeyCard());
        mbp.setUser(getSessionBean().getSessUser());
        mbp.setNewMessageContent(statusNotesFormText);
        mbp.setExistingContent(currentCitationStatusLogEntry.getNotes());
        
        currentCitationStatusLogEntry.setNotes(sc.appendNoteBlock(mbp));
        try {
            sc.writeNotes(currentCitationStatusLogEntry, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Added note to Status Log ID " + currentCitationStatusLogEntry.getLogEntryID(),""));
        } catch (IntegrationException | BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        } 
    }
    
    
    
    /* **********************************************/
    /* **************** DOCKETS  ********************/
    /* **********************************************/
    
    /**
     * Listener to view a docket
     * @param cdr 
     */
    public void onDocketViewButtonChange(CitationDocketRecord cdr){
        currentCitationDocket = cdr;
    }
    
    
    /**
     * Listener to view a docket
     * @param cdr 
     */
    public void onDocketEditButtonChange(CitationDocketRecord cdr){
        currentCitationDocket = cdr;
        citationDocketEditMode = true;
    }
    
    
    /**
     * Listener for user requests to edit a citation docket record info
     */
   public void onCitationDocketEditModeToggle(){
       if(citationDocketEditMode){
           if(currentCitationDocket != null && currentCitationDocket.getDocketID()==0){
               onDocketAddCommitButtonChange();
           } else {
               onDocketEditCommitButtonChange(null);
               
           }
           refreshCurrentCitation();
       }
        citationDocketEditMode = !citationDocketEditMode;
        
        System.out.println("CitationBB.onCitationDocketEditModeToggle | citationDocketEditMode val is " + citationDocketEditMode);
   } 
   
   /**
    * Internal refresher of dockets!
    * @throws BObStatusException
    * @throws IntegrationException 
    */
   private void refreshCurrentDocket() throws BObStatusException, IntegrationException{
       CaseCoordinator cc = getCaseCoordinator();
       currentCitationDocket = cc.citation_getCitationDocketRecord(currentCitationDocket.getHostPK());
   }
    
   
   /**
    * Listener for commencement of docket creation process
     * @param ev
    */
   public void onDocketAddInitButtonChange(ActionEvent ev){
       CaseCoordinator cc = getCaseCoordinator();
       currentCitationDocket = cc.citation_getCitationDocketRecordSkeleton(currentCitation);
       System.out.println("CitationBB.onDocketAddInitButtonChange");
       citationDocketEditMode = true;
       
   }
   
   /**
    * Initiates the writing of a new docket to a citation
    */
   public void onDocketAddCommitButtonChange(){
       CaseCoordinator cc = getCaseCoordinator();
        try {
            int freshID = cc.citation_insertDocketEntry(currentCitationDocket, getSessionBean().getSessUser());
            currentCitationDocket = cc.citation_getCitationDocketRecord(freshID);
            
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Docket added to citation.", ""));
        } catch (IntegrationException | BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
   }
   
    /**
     * Listener for user requests to commit edits to the current docket
     * @param ev 
     */
    public void onDocketEditCommitButtonChange(ActionEvent ev){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.citation_updateDocketEntry(currentCitationDocket, getSessionBean().getSessUser());
            refreshCurrentDocket();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Updated docket Number: " + currentCitationDocket.getDocketNumber(), ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
        citationDocketEditMode = false;
        System.out.println("CitationBB.onDocketEditCommitButtonChange");
    }
    
    /**
     * Commences the user's docket removal process
     */
    public void onDocketRemoveInitButtonChange(){
        // nothing to do here yet
    }
    
    /**
     * Listener for user requests to confirm their removal of a docket
     */
    public void onDocketRemoveCommitButtonChange(){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.citation_deactivateDocketEntry(currentCitationDocket, getSessionBean().getSessUser());
            refreshCurrentCitation();
            triggerParentCECaseReload();
            
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Removed Docket ID: " + currentCitationDocket.getDocketID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to remove the Docket ID: " + currentCitationDocket.getDocketID(), ""));
        }
    }
    
      /**
     * listener for user requests to start to add note to a status log entry
     */
    public void onDocketNoteInitButtonChange(){
        docketNotesFormText = "";
        
        
    }
    
    /**
     * Listener for user requests to complete note process for status logs
     */
    public void onDocketNoteCommitButtonChange(){
        SystemCoordinator sc = getSystemCoordinator();
        
        MessageBuilderParams mbp = new MessageBuilderParams();
        
        mbp.setCred(getSessionBean().getSessUser().getKeyCard());
        mbp.setUser(getSessionBean().getSessUser());
        mbp.setNewMessageContent(docketNotesFormText);
        mbp.setExistingContent(currentCitationDocket.getNotes());
        
        currentCitationDocket.setNotes(sc.appendNoteBlock(mbp));
        try {
            sc.writeNotes(currentCitationDocket, getSessionBean().getSessUser());
            refreshCurrentCitation();
            refreshCurrentDocket();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Added note to Docket ID " + currentCitationDocket.getDocketID(),""));
        } catch (IntegrationException | BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        } 
    }
    
    /**
     * Listener for users to stop the docket add or update operation
     * @param ev 
     */
    public void onDocketAddEditOperationAbortButtonChange(ActionEvent ev){
        citationDocketEditMode = false;
        System.out.println("citationBB.onDocketAddEditOperationAbortButtonChange");
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Operation aborted!" + currentCitationDocket.getDocketID(),""));
    }
    
    
    
    /**
     * removes a link between a citaiton human and a docket
     * NOTE that users can only link a person to a docket
     * if they are also linked to the citation first
     * And citations can only contain links to persons
     * who are already attached to the property (or will be
     * mapped to that property at the time of citation linking)
     * @param hl 
     */
    public void onDocketPersonRemoveButtonChange(HumanLink hl){
        PersonCoordinator pc = getPersonCoordinator();
//        IS this the correct call?
//        pc.deactivateLinkedHuman(currentCitation, hl, ua);
        
    }
    
    
    
    /* ******************************************** */
    /* *************misc citation stuff *********** */
    /* ******************************************** */
    
    /**
     * Listener for user requests to commit changes to citation status
     * @deprecated  replaced with individual record log entries
     * @return
     */
    public String onCitationUpdateStatusCommitButtonChange() {
        System.out.println("CitationBB.updateCitationStatus");
        CaseCoordinator cc = getCaseCoordinator();

        try {
            cc.citation_updateCitation(getCurrentCitation());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success: Updated citation status!", ""));
        } catch (IntegrationException | BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
            System.out.println(ex);
            return "";
        }
        return "ceCaseProfile";
    }

  
   
    /**
     * @return the currentCitation
     */
    public Citation getCurrentCitation() {
        return currentCitation;
    }

    /**
     * @return the citationStatusList
     */
    public List<CitationStatus> getCitationStatusList() {
        return citationStatusList;
    }

    /**
     * @return the courtEntityList
     */
    public List<CourtEntity> getCourtEntityList() {
        return courtEntityList;
    }

    /**
     * @return the citationFilingTypeList
     */
    public List<CitationFilingType> getCitationFilingTypeList() {
        return citationFilingTypeList;
    }

    /**
     * @return the issueCitationDisabled
     */
    public boolean isIssueCitationDisabled() {
        return issueCitationDisabled;
    }

    /**
     * @return the updateCitationDisabled
     */
    public boolean isUpdateCitationDisabled() {
        return updateCitationDisabled;
    }

    /**
     * @return the formNoteCitationText
     */
    public String getFormNoteCitationText() {
        return formNoteCitationText;
    }

    /**
     * @return the removedViolationList
     */
    public List<CodeViolation> getRemovedViolationList() {
        return removedViolationList;
    }

    /**
     * @return the citationEditEventDescription
     */
    public String getCitationEditEventDescription() {
        return citationEditEventDescription;
    }

    /**
     * @return the citationIssuingOfficer
     */
    public User getCitationIssuingOfficer() {
        return citationIssuingOfficer;
    }

    /**
     * @return the currentCitationStatusLogEntry
     */
    public CitationStatusLogEntry getCurrentCitationStatusLogEntry() {
        return currentCitationStatusLogEntry;
    }

    /**
     * @return the citationInfoEditMode
     */
    public boolean isCitationInfoEditMode() {
        return citationInfoEditMode;
    }

    /**
     * @return the citationDocketEditMode
     */
    public boolean isCitationDocketEditMode() {
        return citationDocketEditMode;
    }

    /**
     * @return the citationStatusEditMode
     */
    public boolean isCitationStatusEditMode() {
        return citationStatusEditMode;
    }

   
    /**
     * @param currentCitation the currentCitation to set
     */
    public void setCurrentCitation(Citation currentCitation) {
        this.currentCitation = currentCitation;
    }

    /**
     * @param citationStatusList the citationStatusList to set
     */
    public void setCitationStatusList(List<CitationStatus> citationStatusList) {
        this.citationStatusList = citationStatusList;
    }

    /**
     * @param courtEntityList the courtEntityList to set
     */
    public void setCourtEntityList(List<CourtEntity> courtEntityList) {
        this.courtEntityList = courtEntityList;
    }

    /**
     * @param citationFilingTypeList the citationFilingTypeList to set
     */
    public void setCitationFilingTypeList(List<CitationFilingType> citationFilingTypeList) {
        this.citationFilingTypeList = citationFilingTypeList;
    }

    /**
     * @param issueCitationDisabled the issueCitationDisabled to set
     */
    public void setIssueCitationDisabled(boolean issueCitationDisabled) {
        this.issueCitationDisabled = issueCitationDisabled;
    }

    /**
     * @param updateCitationDisabled the updateCitationDisabled to set
     */
    public void setUpdateCitationDisabled(boolean updateCitationDisabled) {
        this.updateCitationDisabled = updateCitationDisabled;
    }

    /**
     * @param formNoteCitationText the formNoteCitationText to set
     */
    public void setFormNoteCitationText(String formNoteCitationText) {
        this.formNoteCitationText = formNoteCitationText;
    }

    /**
     * @param removedViolationList the removedViolationList to set
     */
    public void setRemovedViolationList(List<CodeViolation> removedViolationList) {
        this.removedViolationList = removedViolationList;
    }

    /**
     * @param citationEditEventDescription the citationEditEventDescription to set
     */
    public void setCitationEditEventDescription(String citationEditEventDescription) {
        this.citationEditEventDescription = citationEditEventDescription;
    }

    /**
     * @param citationIssuingOfficer the citationIssuingOfficer to set
     */
    public void setCitationIssuingOfficer(User citationIssuingOfficer) {
        this.citationIssuingOfficer = citationIssuingOfficer;
    }

    /**
     * @param currentCitationStatusLogEntry the currentCitationStatusLogEntry to set
     */
    public void setCurrentCitationStatusLogEntry(CitationStatusLogEntry currentCitationStatusLogEntry) {
        this.currentCitationStatusLogEntry = currentCitationStatusLogEntry;
    }

    /**
     * @param citationInfoEditMode the citationInfoEditMode to set
     */
    public void setCitationInfoEditMode(boolean citationInfoEditMode) {
        this.citationInfoEditMode = citationInfoEditMode;
    }

    /**
     * @param citationDocketEditMode the citationDocketEditMode to set
     */
    public void setCitationDocketEditMode(boolean citationDocketEditMode) {
        this.citationDocketEditMode = citationDocketEditMode;
    }

    /**
     * @param citationStatusEditMode the citationStatusEditMode to set
     */
    public void setCitationStatusEditMode(boolean citationStatusEditMode) {
        this.citationStatusEditMode = citationStatusEditMode;
    }

    /**
     * @return the currentCitationDocket
     */
    public CitationDocketRecord getCurrentCitationDocket() {
        return currentCitationDocket;
    }

    /**
     * @param currentCitationDocket the currentCitationDocket to set
     */
    public void setCurrentCitationDocket(CitationDocketRecord currentCitationDocket) {
        this.currentCitationDocket = currentCitationDocket;
    }

    /**
     * @return the docketNotesFormText
     */
    public String getDocketNotesFormText() {
        return docketNotesFormText;
    }

    /**
     * @return the statusNotesFormText
     */
    public String getStatusNotesFormText() {
        return statusNotesFormText;
    }

    /**
     * @param docketNotesFormText the docketNotesFormText to set
     */
    public void setDocketNotesFormText(String docketNotesFormText) {
        this.docketNotesFormText = docketNotesFormText;
    }

    /**
     * @param statusNotesFormText the statusNotesFormText to set
     */
    public void setStatusNotesFormText(String statusNotesFormText) {
        this.statusNotesFormText = statusNotesFormText;
    }

    /**
     * @return the currentCitationViolationLink
     */
    public CitationCodeViolationLink getCurrentCitationViolationLink() {
        return currentCitationViolationLink;
    }

    /**
     * @param currentCitationViolationLink the currentCitationViolationLink to set
     */
    public void setCurrentCitationViolationLink(CitationCodeViolationLink currentCitationViolationLink) {
        this.currentCitationViolationLink = currentCitationViolationLink;
    }

    /**
     * @return the formCitationViolationLinkNotes
     */
    public String getFormCitationViolationLinkNotes() {
        return formCitationViolationLinkNotes;
    }

    /**
     * @param formCitationViolationLinkNotes the formCitationViolationLinkNotes to set
     */
    public void setFormCitationViolationLinkNotes(String formCitationViolationLinkNotes) {
        this.formCitationViolationLinkNotes = formCitationViolationLinkNotes;
    }

    /**
     * @return the citationViolationStatusEnumList
     */
    public List<CitationViolationStatusEnum> getCitationViolationStatusEnumList() {
        return citationViolationStatusEnumList;
    }

    /**
     * @param citationViolationStatusEnumList the citationViolationStatusEnumList to set
     */
    public void setCitationViolationStatusEnumList(List<CitationViolationStatusEnum> citationViolationStatusEnumList) {
        this.citationViolationStatusEnumList = citationViolationStatusEnumList;
    }

    
  
    
   
}
