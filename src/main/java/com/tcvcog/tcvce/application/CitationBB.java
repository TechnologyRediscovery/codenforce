/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CitationCodeViolationLink;
import com.tcvcog.tcvce.entities.CitationDocketRecord;
import com.tcvcog.tcvce.entities.CitationFilingType;
import com.tcvcog.tcvce.entities.CitationStatus;
import com.tcvcog.tcvce.entities.CitationStatusLogEntry;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.CourtEntity;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.CourtEntityIntegrator;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.util.ArrayList;
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
        
    private List<User> userManagerOptionList;
    
    private String formNoteText;
    
    private List<CitationFilingType> citationFilingTypeList;

    private boolean issueCitationDisabled;
    private boolean updateCitationDisabled;
    private String formNoteCitationText;
    
    private List<CodeViolation> removedViolationList;
    private String citationEditEventDescription;
    
    private User citationIssuingOfficer;
    
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
         
        // Citation stuff
        
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        try {
            setCitationStatusList(cc.citation_getCitationStatusList());
            setCourtEntityList(cei.getCourtEntityList());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        setRemovedViolationList(new ArrayList<>());
        
        // set edit defaults
        citationStatusEditMode = false;
        citationDocketEditMode = false;
        citationInfoEditMode = false;
        
        
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
     * Listener for user requests to edit a citation's info
     */
   public void onCitationEditModeToggle(){
        citationInfoEditMode = !citationInfoEditMode;
        if(citationInfoEditMode){
            onCitationUpdateCommitButtonChange(null);
        }
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
     * @return page nav
     */
    public String onCitationAddCommitButtonChange() {
        System.out.println("CitationBB.IssueCitation");
        CaseCoordinator cc = getCaseCoordinator();
        if(getCurrentCitation() != null){

                Citation c = getCurrentCitation();
                
                try {
                    cc.citation_insertCitation(c,getSessionBean().getSessUser(), getCitationIssuingOfficer());

                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "New citation added to database!", ""));
                } catch (IntegrationException | BObStatusException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Unable to issue citation due to a database integration error", ""));
                    System.out.println(ex);
                    return "";
                }
                return "ceCaseProfile";
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to issue citation due to page object error", ""));
            return "";
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
        onCitationEditModeToggle();
    }
    
    /**
     * Listener for user requests to start the citation removal process
     * @param cit 
     */
    public void onCitationRemoveInitButtonChange(Citation cit){
        setCurrentCitation(cit);
    }
    
     /**
     * Listener for user requests to remove a citation
     *
     * @return
     */
    public void onCitationRemoveCommitButtonChange(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.citation_removeCitation(getCurrentCitation(), getSessionBean().getSessUser());
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
        setFormNoteCitationText("");
        
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
        mbp.setNewMessageContent(getFormNoteText());
        mbp.setHeader("Citation Note");
        mbp.setUser(getSessionBean().getSessUser());

        try {

            cc.citation_updateNotes(mbp, getCurrentCitation());
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
    
    /**
     * Listener for user requests to remove a violation from a citation
     * @param v 
     */
    public void onCitationViolationRemoveButtonChange(CitationCodeViolationLink v) {
        getCurrentCitation().getViolationList().remove(v);
        getRemovedViolationList().add(v);
    }

    /**
     * Listener for user requests to add a violation to a citation
     * @param v 
     */
    public void onCitationViolationRestoreButtonChange(CitationCodeViolationLink v) {
        getCurrentCitation().getViolationList().add(v);
        getRemovedViolationList().remove(v);
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
     * Listener for user requests to edit a citation's status record 
     */
   public void onCitationStatusLogEditModeToggle(){
        citationStatusEditMode = !citationStatusEditMode;
        // clicking done means save edits
        if(citationStatusEditMode){
            onStatusLogEntryEditCommitButtonChange(null);
        }
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
       currentCitationStatusLogEntry = cc.citation_getStatusLogEntrySkeleton();
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
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Created log entry with ID " + freshID, ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fatal error! Unable to insert new log entry.", ""));
        } 
        citationStatusEditMode = false;
       
   }
   
    
   /**
    * Listener for user requests to start the edit operation
    * @param ev 
    */
   public void onStatusLogEntryEditInitButtonChange(ActionEvent ev){
       // Nothing to do here yet
       
   }
   
    /**
     * Listener for user requests to commit edits to the current log entry
     * @param ev 
     */
    public void onStatusLogEntryEditCommitButtonChange(ActionEvent ev){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.citation_updateCitationStatusLogEntry(currentCitation, currentCitationStatusLogEntry, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Updated log entry ID: " + currentCitationStatusLogEntry.getLogEntryID(), ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
            
        }
        onCitationStatusLogEditModeToggle();
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
            sc.writeNotes(currentCitationDocket, getSessionBean().getSessUser());
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
     * Listener for user requests to edit a citation docket record info
     */
   public void onCitationDocketEditModeToggle(){
        citationDocketEditMode = !citationDocketEditMode;
        
        System.out.println("CitationBB.onCitationDocketEditModeToggle | citationDocketEditMode val is " + citationDocketEditMode);
   } 
    
   
   /**
    * Listener for commencement of docket creation process
    */
   public void onDocketAddInitButtonChange(){
       CaseCoordinator cc = getCaseCoordinator();
       currentCitationDocket = cc.citation_getCitationDocketRecordSkeleton();
       System.out.println("CitationBB.onDocketAddInitButtonChange");
       
   }
   
    /**
     * Listener for user requests to commit edits to the current docket
     * @param ev 
     */
    public void onDocketEditCommitButtonChange(ActionEvent ev){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.citation_updateDocketEntry(currentCitationDocket, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Updated log entry ID: " + currentCitationStatusLogEntry.getLogEntryID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
        onCitationDocketEditModeToggle();
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
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Added note to Docket ID " + currentCitationDocket.getDocketID(),""));
        } catch (IntegrationException | BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        } 
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
     * @return the userManagerOptionList
     */
    public List<User> getUserManagerOptionList() {
        return userManagerOptionList;
    }

    /**
     * @return the formNoteText
     */
    public String getFormNoteText() {
        return formNoteText;
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
     * @param userManagerOptionList the userManagerOptionList to set
     */
    public void setUserManagerOptionList(List<User> userManagerOptionList) {
        this.userManagerOptionList = userManagerOptionList;
    }

    /**
     * @param formNoteText the formNoteText to set
     */
    public void setFormNoteText(String formNoteText) {
        this.formNoteText = formNoteText;
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
    
   
}
