/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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

import com.tcvcog.tcvce.coordinators.*;
import com.tcvcog.tcvce.domain.*;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.occupancy.FieldInspection;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECase;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECaseList;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import com.tcvcog.tcvce.entities.search.SearchParamsCECase;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.util.*;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveListsEnum;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import org.primefaces.event.FileUploadEvent;

/**
 * Primary backing bean for the Code Enforcement case 
 * @author sylvia
 */
public class CECaseBB
        extends BackingBeanUtils
        implements Serializable {
    

    private CECaseDataHeavy currentCase;
    private PropertyDataHeavy currentCasePropDataHeavy;
    private boolean editModeCurrentCase;

    private int formNOVFollowupDays;
    
    private List<EventCategory> closingEventCategoryList;
    private EventCategory closingEventCategorySelected;
    protected int eventPersonIDForLookup;
    
    private ViewOptionsActiveHiddenListsEnum eventViewOptionSelected;
    private List<ViewOptionsActiveHiddenListsEnum> eventViewOptions;
    
    private List<User> userManagerOptionList;
    private List<BOBSource> bobSourceOptionList;
    
    private String formNoteText;
    private String formViolationStipCompDateExtReason;
    private ReportConfigCECase reportCECase;
    
    /*******************************************************
     *              Violation collapse fields
     *              FROM ViolationBB
    /*******************************************************/
    
    private CodeViolation currentViolation;
    private boolean editModeCurrentViolation;
    
    private List<ViewOptionsActiveListsEnum> viewOptionList;
    private ViewOptionsActiveListsEnum selectedViewOption;
    
    private List<IntensityClass> severityList;
    private LocalDateTime complianceDateForm;
    
    private String formNoteTextViolation;
    private List<EnforcableCodeElement> filteredElementList;
    private List<EnforcableCodeElement> selectedElementList;
    private List<CodeViolation> selectedViolationList;

    private CodeSet currentCodeSet;

    private boolean formExtendStipCompUsingDate;
    private int formExtendedStipCompDaysFromToday;
    
    private boolean formMakeFindingsDefault;

    
    /*******************************************************
     *              BLOB Jazz
    /*******************************************************/
    
    private BlobLight currentBlob;
    private List<BlobLight> blobList;
    
    
    /**
     * Creates a new instance of CECaseSearchBB
     */
    public CECaseBB() {
    }

    /**
     * Sets up primary case list and aux lists for editing, and searching
     * This is a hybrid bean that coordinates both CECase search and 
     * list viewing functions
     */
    @PostConstruct
    public void initBean()  {
        CaseCoordinator cc = getCaseCoordinator();
        UserCoordinator uc = getUserCoordinator();
        SystemCoordinator sysCor = getSystemCoordinator();
        try {
            currentCase = cc.cecase_assembleCECaseDataHeavy(getSessionBean().getSessCECase(), getSessionBean().getSessUser());
            userManagerOptionList = uc.user_assembleUserListForSearch(getSessionBean().getSessUser());
            bobSourceOptionList = sysCor.getBobSourceListComplete();
            severityList = sysCor.getIntensitySchemaWithClasses(
                    getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("intensityschema_violationseverity"))
                    .getClassList();

        } catch (IntegrationException | BObStatusException | SearchException  ex) {
            System.out.println(ex);
        }
        
        
        formNOVFollowupDays = getSessionBean().getSessMuni().getProfile().getNovDefaultDaysForFollowup();
        if(formNOVFollowupDays == 0){
            formNOVFollowupDays = 20;
        }
        
        // VIOLATION STUFF FROM VIOLATIONBB
        currentViolation = getSessionBean().getSessCodeViolation();
        
        if (currentViolation == null) {
            if (currentCase != null && currentCase.getViolationList() != null && !currentCase.getViolationList().isEmpty()) {
                currentViolation = currentCase.getViolationList().get(0);
            }
        }
        
        formMakeFindingsDefault = false;
        filteredElementList = null;
        formExtendStipCompUsingDate = true;
        
    }
    
    /**
     * Utility method for reloading the property data heavy version of this
     * case's property
     */
    private void refreshCasePropertyDataHeavy(){
        PropertyCoordinator pc = getPropertyCoordinator();
        if(currentCase != null){
            try {
                currentCasePropDataHeavy = pc.assemblePropertyDataHeavy(currentCase.getProperty(), getSessionBean().getSessUser());
            } catch (IntegrationException | BObStatusException | SearchException ex) {
                  getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Fatal error loading property data heavy", ""));
            
            }
        }
    }
    
    /**
     * Sets the session event
     * @param ev 
     */
    public void injectSessionEvent(EventCnF ev){
        if(ev != null){
            getSessionBean().setSessEvent(ev);
        }
        
    }

   
    
    /**
     * Listener for requests to reload the current CECaseDataHeavy
     * @return  
     */
    public String refreshCurrentCase(){
        return "ceCaseProfile";
        
    }
    
    /**
     * Special getter that tells this bean to check the session for a refresh 
     * trigger timestamp.
     * @return 
     */
    public LocalDateTime getReloadCECaseTrigger(){
        LocalDateTime trigger = getSessionBean().getSessCECaseRefreshTrigger();
        if(trigger != null){
            reloadCurrentCase();
            System.out.println("CECaseSearchProfileBB.getReloadCECaseTrigger: "+ getPrettyDateNoTime(trigger));
            trigger = null;
            getSessionBean().setSessCECaseRefreshTrigger(trigger);
        }
        return trigger;
        
    }
    
    /**
     * provides the ID of the component for accessory UIs to refresh so the CE case is
     * refreshed. Updating this component will call getReloadCECaseTrigger which, if not null
     * triggers a reload.
     * @return 
     */
    public String getReloadCECaseComponentIDToUpdate(){
        return "case-refresh-trigger-form";
    }
    
    
    /**
     * Gets a new cecase data heavy
     */
    public void reloadCurrentCase(){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            currentCase = cc.cecase_assembleCECaseDataHeavy(currentCase, getSessionBean().getSessUser());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Refreshed case!", ""));
        } catch (BObStatusException  | IntegrationException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Could not refresh current case", ""));
        }
        
    }
    
    public void reloadCaseListener(ActionEvent ev){
        reloadCurrentCase();
    }
    
  
    /**
     * Listener for user requests to start case updates or end them
     * @param ev 
     */
    public void onToggleCECaseEditButtonChange(ActionEvent ev){
        CaseCoordinator cc = getCaseCoordinator();
        if(editModeCurrentCase){
            try {
                cc.cecase_updateCECaseMetadata(currentCase, getSessionBean().getSessUser());
            } catch (BObStatusException | IntegrationException ex) {
                System.out.println(ex);
            } 
        }
    }

    
   
     
    
    /**
     * Listener for user requests to open new case at current property
     * @return 
     */
    public String onCaseOpenButtonChange(){
        getSessionBean().getNavStack().pushPage("ceCaseProfile");
        return "caseAdd";
        
    }
    

    
    
    
    
    
    /**
      * Funnel for all updateXXX methods on cases
      * The caller is responsible for updating notes to 
      * document the field changes, value by value
      * @param cse the case with updated fields
      */
     private void updateCaseMetatData(){
          CaseCoordinator cc = getCaseCoordinator();
        
        try {
            cc.cecase_updateCECaseMetadata(currentCase, getSessionBean().getSessUser());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Case metadata updated", ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Could not update case metadata, sorry! This error must be corrected by an administrator", ""));
            
        }
         
     }
     
    /**
     * Listener for user requests to deactivate a cecase
     * @return 
     */
    public String onCaseRemoveCommitButtonChange(){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.cecase_deactivateCase(currentCase);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Case marked as inactive.", ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                ex.getMessage(), ""));
            
        }
        return "ceCaseProfile";
        
    }
    
    /**
     * Listener for user requests to start the case force close operation
     * @param ev 
     */
    public void onCaseForceCloseInitButtonChange(ActionEvent ev){
        // nothing to do yet
        
    }
    
    /**
     * Listener for user requests to deactivate a cecase
     * @return 
     */
    public String onCaseForceCloseCommitButtonChnage(){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.cecase_forceclose(currentCase, closingEventCategorySelected, getSessionBean().getSessUser());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Case closed and unresolved violations nullified.", ""));
        } catch (IntegrationException | BObStatusException | EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                ex.getMessage(), ""));
            
        }
        return "ceCaseProfile";
        
    }
    
    /**
     * Listener for user requests to begin operation
     * @param ev 
     */
    public void onCaseRenameInitButtonChange(ActionEvent ev){
        
        
    }
    
    /**
     * User requests to commit rename operation
     * @return reload case profile page
     */
    public String onCaseRenameCommitButtonChange(){
        return "ceCaseProfile";
        
    }
    
    
    /**
     * Listener for user requests to begin operation
     * @param ev 
     */
    public void onCaseChangeManagerInitButtonChange(ActionEvent ev){
        
        
    }
    
    /**
     * User requests to commit change manager operation
     * @return reload case profile page
     */
    public String onCaseChangeManagerCommitButtonChange(){
        return "ceCaseProfile";
        
    }
    
    
    /**
     * Listener for user requests to begin operation
     * @param ev 
     */
    public void onCaseUpdateDORInitButtonChange(ActionEvent ev){
        
        
    }
    
    /**
     * User requests to commit update DOR operation
     * @return reload case profile page
     */
    public String onCaseUpdateDORCommitButtonChange(){
        updateCaseMetatData(currentCase);
        return "ceCaseProfile";
        
    }
    
    
    
      /**
     * Listener for commencement of note writing process
     *
     * @param ev
     */
    public void onCaseNoteInitButtonChange(ActionEvent ev) {
        formNoteText = new String();

    }

    
    public void onHowToNextStepButtonChange(ActionEvent ev){
        System.out.println("ceCaseSearchProfileBB.onHowToNextStepButtonChange");
        
    }
    
    
    
    
    
    
    /*******************************************************
    /*******************************************************
     **              BLOBS                               **
    /*******************************************************/
    /*******************************************************/
    
    /**
     * Special getter wrapper around the CECase blob list
     * that checks the session for a new blob list
     * that may have been injected by the BlobUtilitiesBB
     * 
     * @return the CECases's updated blob list
     */
    public List<BlobLight> getManagedBlobLightListFromCECase(){
        List<BlobLight> sessBlobListForUpdate = getSessionBean().getSessBlobLightListForRefreshUptake();
        if(sessBlobListForUpdate != null && currentCase != null){
            System.out.println("CECaseSearchProfileBB.getBlobLightListFromCECase | found non-null session blob list for uptake: " + sessBlobListForUpdate.size());
            getCurrentCase().setBlobList(sessBlobListForUpdate);
            // clear session since we have the new list
            getSessionBean().setSessBlobLightListForRefreshUptake(null);
            return sessBlobListForUpdate;
        } else {
            if(currentCase.getBlobList() != null){
                return currentCase.getBlobList();
            } else {
                return new ArrayList<>();
            }
        }
    }
    
    
   
    
    

    /**
     * Listener for requests to go view the property profile of a property associated
     * with the given case
     * @return 
     */
    public String exploreProperty(){
        try {
            getSessionBean().setSessProperty(currentCase.getParcelKey());
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Could not load property data heavy; reloaded page", ""));
            return "";
        }
        return "propertyInfo";
        
    }
    
   
    
  
  
    
    /**
     * Catch all for cancellation requests
     * @param ev 
     */
    public void onOperationCancelButtonChange(ActionEvent ev){
        //  nothing to do here yet
    }
    
    
    
    
    
    /*******************************************************
    /*******************************************************
     **              Violation processing                 **
    /*******************************************************/
    /*******************************************************/
    
    
    /**
     * Listener for start of violation update operation
     * @param viol 
     */
    public void onViolationUpdateInitButtonChange(CodeViolation viol){
        currentViolation = viol;
    }
    
    /**
     * Listener for user requests to start the compliance recording operation
     * @param viol 
     */
    public void onViolationRecordComplianceInitButtonChange(CodeViolation viol){
        currentViolation = viol;
        // set default compliance date of today
    }
    
    /**
     * Listener for user requests to record compliance from the violation
     * details dialog (not the violation table, in which case the violation
     * object is accepted as a parameter
     * @param ev 
     */
    public void onViolationRecordComplianceInitButtonChange(ActionEvent ev){
        System.out.println("CeCaseSearchProfileBB.onViolationRecordComplianceInitButtonChange | from dialog");
    }
    
  
    
    /**
     * Listener for user requests to start nuke operation
     * @param viol 
     */
    public void onViolationNukeButtonChange(CodeViolation viol){
        currentViolation = viol;
    }
    
    /**
     * Listener for user requests to start nullify operation
     * @param viol 
     */
    public void onViolationNullifyInitButtonChange(CodeViolation viol){
        currentViolation = viol;
    }
    
    /**
     * Listener for user requests to start nullify operation
     * @param ev
     */
    public void onViolationNullifyInitButtonChange(ActionEvent ev){
        // nothing to do here since we got to this point 
        // having viewed the violation so currentViolation is correct
    }
    
    
   
     /**
     * Listener for user selection of a violation from the code set violation
     * table
     *
     * @param ece
     */
     private CodeViolation injectOrdinanceIntoViolation(EnforcableCodeElement ece) throws BObStatusException {

       
        CaseCoordinator cc = getCaseCoordinator();
        CodeViolation cv = null;
        cv = cc.violation_injectOrdinance(cc.violation_getCodeViolationSkeleton(currentCase), ece);
       
        return cv;
    }
    
    /**
    * Listener for user indication that ordinances have been selected
     * and we're ready to configure the violation of those ordinances
     * Iterates over each of the selected elements and injects the ordinance
     * and builds a violation list for configuration
     * 
    */
    public void onOrdinanceSelectionCompleteButtonChange(){
        if(!selectedElementList.isEmpty()){
            for(EnforcableCodeElement ece: selectedElementList){
                try{
                    System.out.println("onOrdinanceSelectionCompleteButtonChange | visiting eceID: " + ece.getCodeSetElementID());
                    CodeViolation cv = injectOrdinanceIntoViolation(ece);
                    System.out.println("onOrdinanceSelectionCompleteButtonChange | Adding Code Violation to list, cseID: " + cv.getViolatedEnfElement().getCodeSetElementID());
                    selectedViolationList.add(cv);
                } catch (BObStatusException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    ex.getMessage(), ""));
                }
            }
        } else {
             getFacesContext().addMessage(null,
                     new FacesMessage(FacesMessage.SEVERITY_ERROR,

                            "At least one ordinance is required!", ""));
        }
    }
    
    /**
     * Responds to user requests to commit a new code violation to the CECase
     *
     * @return
    */
    public String onViolationAddCommitButtonChange() {


        CaseCoordinator cc = getCaseCoordinator();
        UserAuthorized ua = getSessionBean().getSessUser();
        
        // Iterate over all the violations and add them to our case
        if(!selectedViolationList.isEmpty()){
            for(CodeViolation cv: selectedViolationList){
                try {
                    cc.violation_attachViolationToCase(cv, currentCase, ua);
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "Success! Violation attached to case.", ""));
                    // Removed for batch processing
                    //            getSessionBean().getSessionBean().setSessCodeViolation(currentViolation);
                    System.out.println("CECaseSearchProfileBB.onViolationAddCommmitButtonChange | attached violation to case");
                    if(cv.isMakeFindingsDefault()){
                        makeViolationFindingsDefault(cv);
                    }
                } catch (IntegrationException | SearchException | BObStatusException | EventException | ViolationException ex) {
                    System.out.println(ex);
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    ex.getMessage(), ""));
                }
            }
        }
        
        return "ceCaseProfile";
    }
    
      /**
     * Listener for user requests to remove a violation from the list
     * of ECEs turned into violations, prior to their attachment to the case
     * @param cv 
     */
    public void onViolationRemoveFromBatchButtonChange(CodeViolation cv){
        if(cv != null && !selectedViolationList.isEmpty()){
            selectedViolationList.remove(cv);
            System.out.println("Removed Violation ECE ID from batch: " + cv.getViolatedEnfElement().getCodeSetElementID());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Removed Ordiance from Batch-Code Set Element ID: " + cv.getViolatedEnfElement().getCodeSetElementID(), ""));
        }
    }
    
     /**
     * Listener for user requests to start the violation add process
     * 
     * @param ev
     */
    public void onViolationAddInit(ActionEvent ev) {
        // setup our lists
        selectedElementList = new ArrayList<>();
        selectedViolationList = new ArrayList<>();
        System.out.println("OnViolationAddInit: Selected Element list Size: " + selectedElementList.size());
    }
    
    
    
      /**
     * Listener for commencement of extending stip comp date
     *
     * @param ev
     */
    public void onViolationExtendStipCompDateInitButtonChange(ActionEvent ev) {
        setFormExtendedStipCompDaysFromToday(CaseCoordinator.DEFAULT_EXTENSIONDAYS);
    }

    /**
     * Listener for requests to commit extension of stip comp date
     *
     * @return
     */
    public String onViolationExtendStipCompDateCommitButtonChange() {
        CaseCoordinator cc = getCaseCoordinator();
        long secBetween;
        try {
            if (formExtendStipCompUsingDate) {
                if (currentViolation.getStipulatedComplianceDate().isBefore(LocalDateTime.now())) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Stipulated compliance dates must be in the future!", ""));
                } else {
                    secBetween = currentViolation.getStipulatedComplianceDate().toEpochSecond(ZoneOffset.of("-4")) - LocalDateTime.now().toEpochSecond(ZoneOffset.of("-4"));
                    // divide by num seconds in a day
                    long daysBetween = secBetween / (24 * 60 * 60);
                    
                    cc.violation_extendStipulatedComplianceDate(getCurrentViolation(), formViolationStipCompDateExtReason, currentCase, getSessionBean().getSessUser());
                }
            } else {
                // no math to do
                cc.violation_extendStipulatedComplianceDate(getCurrentViolation(), formViolationStipCompDateExtReason, currentCase, getSessionBean().getSessUser());
            }
        } catch (BObStatusException | IntegrationException | ViolationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.toString(), ""));
        } 
        getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "Stipulated compliance dates is now: " + DateTimeUtil.getPrettyDate(getCurrentViolation().getStipulatedComplianceDate()), ""));
        return "ceCaseProfile";

    }

    /**
     * Listener for user reqeusts to commit updates to a codeViolation
     *
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     */
    public String onViolationUpdateCommitButtonChange() throws IntegrationException, BObStatusException {
        CaseCoordinator cc = getCaseCoordinator();
        EventCoordinator eventCoordinator = getEventCoordinator();
        SystemCoordinator sc = getSystemCoordinator();

        EventCategory ec = eventCoordinator.initEventCategory(
                Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE).getString("updateViolationEventCategoryID")));

        try {

            cc.violation_updateCodeViolation(currentCase, getCurrentViolation(), getSessionBean().getSessUser());

            // if update succeeds without throwing an error, then generate an
            // update violation event
            // TODO: Rewire this to work with new event processing cycle
//             eventCoordinator.generateAndInsertCodeViolationUpdateEvent(getCurrentCase(), currentViolation, event);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Violation updated and notice event generated", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to edit violation in the database",
                            "This is a system-level error that msut be corrected by an administrator, Sorry!"));

        } catch (ViolationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            ex.getMessage(), "Please revise the stipulated compliance date"));

        }

        return "ceCaseProfile";
    }

     /**
     * Listener for user requests to start the update stip date operation
     * @param ev 
     */
    public void onViolationUpdateDORInitButtonChange(ActionEvent ev){
        System.out.println("CECaseSearchProfileBB.onViolationUpdateDORInitButtonChange");
        // nothing to do here yet
        
    }
    
    /**
     * Listener for user reqeusts to commit updates to a codeViolation
     *
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     */
    public String onViolationUpdateDORCommitButtonChange() throws IntegrationException, BObStatusException {
        CaseCoordinator cc = getCaseCoordinator();
        EventCoordinator eventCoordinator = getEventCoordinator();
        SystemCoordinator sc = getSystemCoordinator();

        EventCategory ec = eventCoordinator.initEventCategory(
                Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE).getString("updateViolationEventCategoryID")));

        try {

            cc.violation_updateCodeViolation(currentCase, getCurrentViolation(), getSessionBean().getSessUser());

            // if update succeeds without throwing an error, then generate an
            // update violation event
            // TODO: Rewire this to work with new event processing cycle
//             eventCoordinator.generateAndInsertCodeViolationUpdateEvent(getCurrentCase(), currentViolation, event);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Violation updated and notice event generated", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to edit violation in the database",
                            "This is a system-level error that msut be corrected by an administrator, Sorry!"));

        } catch (ViolationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            ex.getMessage(), "Please revise the stipulated compliance date"));

        }

        return "ceCaseProfile";
    }
    
    /**
     * Listener for user requests to start the update stip date operation
     * @param ev 
     */
    public void onViolationUpdateStipDateInitButtonChange(ActionEvent ev){
        System.out.println("CECaseSearchProfileBB.onViolationUpdateStipDateInitButtonChange");
        // nothing to do here yet
        
    }
    
    /**
     * Listener for user reqeusts to commit updates to a codeViolation
     *
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     */
    public String onViolationUpdateStipDateCommitButtonChange() throws IntegrationException, BObStatusException {
        CaseCoordinator cc = getCaseCoordinator();
        EventCoordinator eventCoordinator = getEventCoordinator();
        SystemCoordinator sc = getSystemCoordinator();

        EventCategory ec = eventCoordinator.initEventCategory(
                Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE).getString("updateViolationEventCategoryID")));

        try {

            cc.violation_updateCodeViolation(currentCase, getCurrentViolation(), getSessionBean().getSessUser());

            // if update succeeds without throwing an error, then generate an
            // update violation event
            // TODO: Rewire this to work with new event processing cycle
//             eventCoordinator.generateAndInsertCodeViolationUpdateEvent(getCurrentCase(), currentViolation, event);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Violation updated and notice event generated", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to edit violation in the database",
                            "This is a system-level error that msut be corrected by an administrator, Sorry!"));

        } catch (ViolationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            ex.getMessage(), "Please revise the stipulated compliance date"));

        }

        return "ceCaseProfile";
    }
    
    
    /**
     * Listener for user requests to start the update penalty and severity operation
     * @param ev 
     */
    public void onViolationUpdatePenaltySeverityInitButtonChange(ActionEvent ev){
        System.out.println("CECaseSearchProfileBB.onViolationUpdatePenalitySeverityInitButtonChange");
        // nothing to do here yet
        
    }
    
    
    /**
     * Listener for user reqeusts to commit updates to a codeViolation
     *
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     */
    public String onViolationUpdatePenaltySeverityCommitButtonChange() throws IntegrationException, BObStatusException {
        CaseCoordinator cc = getCaseCoordinator();
        EventCoordinator eventCoordinator = getEventCoordinator();
        SystemCoordinator sc = getSystemCoordinator();

        EventCategory ec = eventCoordinator.initEventCategory(
                Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE).getString("updateViolationEventCategoryID")));

        try {

            cc.violation_updateCodeViolation(currentCase, getCurrentViolation(), getSessionBean().getSessUser());

            // if update succeeds without throwing an error, then generate an
            // update violation event
            // TODO: Rewire this to work with new event processing cycle
//             eventCoordinator.generateAndInsertCodeViolationUpdateEvent(getCurrentCase(), currentViolation, event);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Violation updated and notice event generated", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to edit violation in the database",
                            "This is a system-level error that msut be corrected by an administrator, Sorry!"));

        } catch (ViolationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            ex.getMessage(), "Please revise the stipulated compliance date"));

        }

        return "ceCaseProfile";
    }

    
    
    
    /**
     * Listener for user requests to start the update findings operation
     * @param ev 
     */
    public void onViolationUpdateFindingsInitButtonChange(ActionEvent ev){
        System.out.println("CECaseSearchProfileBB.onViolationUpdateFindingsInitButtonChange");
        // nothing to do here yet
        
    }
    
    /**
     * Listener for user reqeusts to commit updates to a codeViolation
     *
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     */
    public String onViolationUpdateFindingsCommitButtonChange() throws IntegrationException, BObStatusException {
        CaseCoordinator cc = getCaseCoordinator();
        EventCoordinator eventCoordinator = getEventCoordinator();
        SystemCoordinator sc = getSystemCoordinator();

        EventCategory ec = eventCoordinator.initEventCategory(
                Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE).getString("updateViolationEventCategoryID")));

        try {

            cc.violation_updateCodeViolation(currentCase, getCurrentViolation(), getSessionBean().getSessUser());

            // if update succeeds without throwing an error, then generate an
            // update violation event
            // TODO: Rewire this to work with new event processing cycle
//             eventCoordinator.generateAndInsertCodeViolationUpdateEvent(getCurrentCase(), currentViolation, event);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Violation updated and notice event generated", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to edit violation in the database",
                            "This is a system-level error that msut be corrected by an administrator, Sorry!"));

        } catch (ViolationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            ex.getMessage(), "Please revise the stipulated compliance date"));

        }
        
        if(formMakeFindingsDefault){
            makeViolationFindingsDefault(currentViolation);
        }

        return "ceCaseProfile";
    }
    
    private void makeViolationFindingsDefault(CodeViolation cv){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.violation_makeFindingsDefaultInCodebook(cv, getSessionBean().getSessUser() , false);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Updated default violation findings",
                            ""));
        } catch (BObStatusException | IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to make violation findings default " + ex.getMessage(),
                            ""));
            System.out.println(ex);
        } 
    }

    
    
    
    /**
     * Listener for user requests to commit a violation compliance event
     *
     * @return 
     */
    public String onViolationRecordComplianceCommitButtonChange() {
        EventCoordinator ec = getEventCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        if(currentViolation != null){
            currentViolation.setActualComplianceDate(complianceDateForm);
        }
        // build event details package
        EventCnF e = null;
        try {
            // Delegate the heavy lifting to the coordinator
            cc.violation_recordCompliance(currentCase, currentViolation, getSessionBean().getSessUser());
               getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Compliance recorded for Ordinacnce " + currentViolation.getViolationID(), ""));

            // ************ TODO: Finish me with events ******************//
            // ************ TODO: Finish me with events ******************//
            e = ec.generateViolationComplianceEvent(getCurrentViolation());
            e.setUserCreator(getSessionBean().getSessUser());
            e.setTimeStart(LocalDateTime.now());

            // ************ TODO: Finish me with events ******************//
            // ************ TODO: Finish me with events ******************//

               getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Compliance event attached to case", ""));
        } catch (IntegrationException | BObStatusException | ViolationException | EventException ex) {
            System.out.println(ex);
               getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        ex.toString(), ""));
               return "";
        }


        return "ceCaseProfile";
        

            
        // the user is then shown the add event dialog, and when the
        // event is added to the case, the CaseCoordinator will
        // set the date of record on the violation to match that chosen
        // for the event
//        selectedEvent = e;
    }

    /**
     * Listener for commencement of note writing process
     *
     * @param cv
     */
    public void onViolationNotesInitButtonChange(CodeViolation cv) {
        currentViolation = cv;
        formNoteTextViolation = null;

    }

    /**
     * Listener for user requests to commit new note content to the current
     * violation
     *
     * @param ev
     */
    public void onViolationNoteCommitButtonChange(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();

        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setCred(getSessionBean().getSessUser().getKeyCard());
        mbp.setExistingContent(getCurrentViolation().getNotes());
        mbp.setNewMessageContent(getFormNoteText());
        mbp.setHeader("Violation Note");
        mbp.setUser(getSessionBean().getSessUser());

        try {

            cc.violation_updateNotes(mbp, getCurrentViolation());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succesfully appended note!", ""));
            currentViolation = cc.violation_getCodeViolation(currentViolation.getViolationID());
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fatal error appending note; apologies!", ""));
        }
    }

  

  
    
    /**
     * Listener for user requests to abort violation add process
     * @return 
     */
    public String onViolationAddAbortButtonChange(){
        return "";  //reload our current page with dialogs closed
        
    }

    /**
     * Listener for user requests to remove a violation from a case
     *
     * @return
     */
    public String onViolationRemoveCommitButtonChange() {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.violation_deactivateCodeViolation(getCurrentViolation(), getSessionBean().getSessUser());
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), null));
            return "";

        }
        return "ceCaseProfile";

    }
    
    /**
     * Listener for user requests to nullify a violation
     * @return 
     */
    public String onViolationNullifyCommitButtonChange(){
        CaseCoordinator cc = getCaseCoordinator();
         try {
            cc.violation_NullifyCodeViolation(currentViolation, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Violation nullified.", ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), null));
            return "";

        }
        return "ceCaseProfile";
    }
    
   
    /**
     * Listener for user requests to reactivate a violation
     * @return 
     */
    public String onViolationReactivateButtonChange(){
        CaseCoordinator cc = getCaseCoordinator();
         try {
            cc.violation_reactivateViolation(currentViolation, currentCase, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Violation reactivated.", ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), null));
            return "";

        }
        return "ceCaseProfile";
    }
    
   
  
    
    
    
     /**
     * Listener for user requests to build a code enforcement case
     * 
     * @return 
     */
    public String generateReportCECase() {
        System.out.println("CECaseSearchProfileBB.generateReportCECase");
        CaseCoordinator cc = getCaseCoordinator();

        getReportCECase().setCreator(getSessionBean().getSessUser());
        getReportCECase().setMuni(getSessionBean().getSessMuni());
        getReportCECase().setGenerationTimestamp(LocalDateTime.now());

        try {
            setReportCECase(cc.report_transformCECaseForReport(getReportCECase()));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Could not generate report, sorry!", ""));
        }

        getSessionBean().setReportConfigCECase(getReportCECase());
        // this is for use by the report header to have a super class with only
        // the basic info. reportingBB exposes it to the faces page
        getSessionBean().setSessReport(getReportCECase());
        // force our reportingBB to choose the right bundle
        getSessionBean().setReportConfigCECaseList(null);

        return "reportCECase";
    }

  /**
     * Listener for the report initiation process
     * @param ev 
     */
    public void prepareReportCECase(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();
        setReportCECase(cc.report_getDefaultReportConfigCECase(currentCase));
        System.out.println("CaseProfileBB.prepareReportCECase | reportConfigOb: " + getReportCECase());

    }
    
    
    
    
    
    /*******************************************************
    /*******************************************************
     **              GETTERS AND SETTERS                  **
    /*******************************************************/
    /*******************************************************/
    
    
    

    /**
     * @return the currentCase
     */
    public CECaseDataHeavy getCurrentCase() {
        return currentCase;
    }

   
    
    /**
     * @param currentCase the currentCase to set
     */
    public void setCurrentCase(CECaseDataHeavy currentCase) {
        this.currentCase = currentCase;
    }

   
    

    /**
     * @return the eventViewOptions
     */
    public List<ViewOptionsActiveHiddenListsEnum> getEventViewOptions() {
        return eventViewOptions;
    }

    /**
     * @param eventViewOptions the eventViewOptions to set
     */
    public void setEventViewOptions(List<ViewOptionsActiveHiddenListsEnum> eventViewOptions) {
        this.eventViewOptions = eventViewOptions;
    }

    /**
     * @return the eventViewOptionSelected
     */
    public ViewOptionsActiveHiddenListsEnum getEventViewOptionSelected() {
        return eventViewOptionSelected;
    }

    /**
     * @param eventViewOptionSelected the eventViewOptionSelected to set
     */
    public void setEventViewOptionSelected(ViewOptionsActiveHiddenListsEnum eventViewOptionSelected) {
        this.eventViewOptionSelected = eventViewOptionSelected;
    }

    /**
     * @return the userManagerOptionList
     */
    public List<User> getUserManagerOptionList() {
        return userManagerOptionList;
    }

    /**
     * @param userManagerOptionList the userManagerOptionList to set
     */
    public void setUserManagerOptionList(List<User> userManagerOptionList) {
        this.userManagerOptionList = userManagerOptionList;
    }

    /**
     * @return the bobSourceOptionList
     */
    public List<BOBSource> getBobSourceOptionList() {
        return bobSourceOptionList;
    }

    /**
     * @param bobSourceOptionList the bobSourceOptionList to set
     */
    public void setBobSourceOptionList(List<BOBSource> bobSourceOptionList) {
        this.bobSourceOptionList = bobSourceOptionList;
    }

    /**
     * @return the formNoteText
     */
    public String getFormNoteText() {
        return formNoteText;
    }

    /**
     * @param formNoteText the formNoteText to set
     */
    public void setFormNoteText(String formNoteText) {
        this.formNoteText = formNoteText;
    }

    /**
     * @return the closingEventList
     */
    public List<EventCategory> getClosingEventList() {
        return getClosingEventCategoryList();
    }

    /**
     * @param closingEventList the closingEventList to set
     */
    public void setClosingEventList(List<EventCategory> closingEventList) {
        this.setClosingEventCategoryList(closingEventList);
    }

    /**
     * @return the closingEventCategorySelected
     */
    public EventCategory getClosingEventCategorySelected() {
        return closingEventCategorySelected;
    }

    /**
     * @param closingEventCategorySelected the closingEventCategorySelected to set
     */
    public void setClosingEventCategorySelected(EventCategory closingEventCategorySelected) {
        this.closingEventCategorySelected = closingEventCategorySelected;
    }

   
    /**
     * @return the formNOVFollowupDays
     */
    public int getFormNOVFollowupDays() {
        return formNOVFollowupDays;
    }

    /**
     * @param formNOVFollowupDays the formNOVFollowupDays to set
     */
    public void setFormNOVFollowupDays(int formNOVFollowupDays) {
        this.formNOVFollowupDays = formNOVFollowupDays;
    }

    /**
     * @return the currentViolation
     */
    public CodeViolation getCurrentViolation() {
        return currentViolation;
    }

    /**
     * @return the viewOptionList
     */
    public List<ViewOptionsActiveListsEnum> getViewOptionList() {
        return viewOptionList;
    }

    /**
     * @return the selectedViewOption
     */
    public ViewOptionsActiveListsEnum getSelectedViewOption() {
        return selectedViewOption;
    }

    /**
     * @return the severityList
     */
    public List<IntensityClass> getSeverityList() {
        return severityList;
    }

    /**
     * @return the formNoteTextViolation
     */
    public String getFormNoteTextViolation() {
        return formNoteTextViolation;
    }

    /**
     * @return the filteredElementList
     */
    public List<EnforcableCodeElement> getFilteredElementList() {
        return filteredElementList;
    }

    /**
     * @return the currentCodeSet
     */
    public CodeSet getCurrentCodeSet() {
        return currentCodeSet;
    }

    /**
     * @return the formExtendStipCompUsingDate
     */
    public boolean isFormExtendStipCompUsingDate() {
        return formExtendStipCompUsingDate;
    }

   

    /**
     * @return the formExtendedStipCompDaysFromToday
     */
    public int getFormExtendedStipCompDaysFromToday() {
        return formExtendedStipCompDaysFromToday;
    }

    /**
     * @param currentViolation the currentViolation to set
     */
    public void setCurrentViolation(CodeViolation currentViolation) {
        this.currentViolation = currentViolation;
    }

    /**
     * @param viewOptionList the viewOptionList to set
     */
    public void setViewOptionList(List<ViewOptionsActiveListsEnum> viewOptionList) {
        this.viewOptionList = viewOptionList;
    }

    /**
     * @param selectedViewOption the selectedViewOption to set
     */
    public void setSelectedViewOption(ViewOptionsActiveListsEnum selectedViewOption) {
        this.selectedViewOption = selectedViewOption;
    }

    /**
     * @param severityList the severityList to set
     */
    public void setSeverityList(List<IntensityClass> severityList) {
        this.severityList = severityList;
    }

    /**
     * @param formNoteTextViolation the formNoteTextViolation to set
     */
    public void setFormNoteTextViolation(String formNoteTextViolation) {
        this.formNoteTextViolation = formNoteTextViolation;
    }

    /**
     * @param filteredElementList the filteredElementList to set
     */
    public void setFilteredElementList(List<EnforcableCodeElement> filteredElementList) {
        this.filteredElementList = filteredElementList;
    }

    /**
     * @param currentCodeSet the currentCodeSet to set
     */
    public void setCurrentCodeSet(CodeSet currentCodeSet) {
        this.currentCodeSet = currentCodeSet;
    }

    /**
     * @param formExtendStipCompUsingDate the formExtendStipCompUsingDate to set
     */
    public void setFormExtendStipCompUsingDate(boolean formExtendStipCompUsingDate) {
        this.formExtendStipCompUsingDate = formExtendStipCompUsingDate;
    }

   

    /**
     * @param formExtendedStipCompDaysFromToday the formExtendedStipCompDaysFromToday to set
     */
    public void setFormExtendedStipCompDaysFromToday(int formExtendedStipCompDaysFromToday) {
        this.formExtendedStipCompDaysFromToday = formExtendedStipCompDaysFromToday;
    }

    /**
     * @return the complianceDateForm
     */
    public LocalDateTime getComplianceDateForm() {
        return complianceDateForm;
    }

    /**
     * @param complianceDateForm the complianceDateForm to set
     */
    public void setComplianceDateForm(LocalDateTime complianceDateForm) {
        this.complianceDateForm = complianceDateForm;
    }

    /**
     * @return the formMakeFindingsDefault
     */
    public boolean isFormMakeFindingsDefault() {
        return formMakeFindingsDefault;
    }

    /**
     * @param formMakeFindingsDefault the formMakeFindingsDefault to set
     */
    public void setFormMakeFindingsDefault(boolean formMakeFindingsDefault) {
        this.formMakeFindingsDefault = formMakeFindingsDefault;
    }

   

    /**
     * @return the closingEventCategoryList
     */
    public List<EventCategory> getClosingEventCategoryList() {
        return closingEventCategoryList;
    }

    
    /**
     * @param closingEventCategoryList the closingEventCategoryList to set
     */
    public void setClosingEventCategoryList(List<EventCategory> closingEventCategoryList) {
        this.closingEventCategoryList = closingEventCategoryList;
    }

 

    /**
     * @return the currentCasePropDataHeavy
     */
    public PropertyDataHeavy getCurrentCasePropDataHeavy() {
        return currentCasePropDataHeavy;
    }

    /**
     * @param currentCasePropDataHeavy the currentCasePropDataHeavy to set
     */
    public void setCurrentCasePropDataHeavy(PropertyDataHeavy currentCasePropDataHeavy) {
        this.currentCasePropDataHeavy = currentCasePropDataHeavy;
    }

    
    /**
     * @return the blobList
     */
    public List<BlobLight> getBlobList() {
        return blobList;
    }

    /**
     * @param blobList the blobList to set
     */
    public void setBlobList(List<BlobLight> blobList) {
        this.blobList = blobList;
    }

    /**
     * @return the currentBlob
     */
    public BlobLight getCurrentBlob() {
        return currentBlob;
    }

    /**
     * @param currentBlob the currentBlob to set
     */
    public void setCurrentBlob(BlobLight currentBlob) {
        this.currentBlob = currentBlob;
    }

   
    /**
     * @return the eventPersonIDForLookup
     */
    public int getEventPersonIDForLookup() {
        return eventPersonIDForLookup;
    }

    /**
     * @param eventPersonIDForLookup the eventPersonIDForLookup to set
     */
    public void setEventPersonIDForLookup(int eventPersonIDForLookup) {
        this.eventPersonIDForLookup = eventPersonIDForLookup;
    }

    /**
     * @return the selectedElementList
     */
    public List<EnforcableCodeElement> getSelectedElementList() {
        return selectedElementList;
    }

    /**
     * @return the selectedViolationList
     */
    public List<CodeViolation> getSelectedViolationList() {
        return selectedViolationList;
    }

    /**
     * @param selectedElementList the selectedElementList to set
     */
    public void setSelectedElementList(List<EnforcableCodeElement> selectedElementList) {
        this.selectedElementList = selectedElementList;
    }

    /**
     * @param selectedViolationList the selectedViolationList to set
     */
    public void setSelectedViolationList(List<CodeViolation> selectedViolationList) {
        this.selectedViolationList = selectedViolationList;
    }

    
    /**
     * @return the formViolationStipCompDateExtReason
     */
    public String getFormViolationStipCompDateExtReason() {
        return formViolationStipCompDateExtReason;
    }

    /**
     * @param formViolationStipCompDateExtReason the formViolationStipCompDateExtReason to set
     */
    public void setFormViolationStipCompDateExtReason(String formViolationStipCompDateExtReason) {
        this.formViolationStipCompDateExtReason = formViolationStipCompDateExtReason;
    }

    /**
     * @return the editModeCurrentViolation
     */
    public boolean isEditModeCurrentViolation() {
        return editModeCurrentViolation;
    }

    /**
     * @param editModeCurrentViolation the editModeCurrentViolation to set
     */
    public void setEditModeCurrentViolation(boolean editModeCurrentViolation) {
        this.editModeCurrentViolation = editModeCurrentViolation;
    }

    /**
     * @return the reportCECase
     */
    public ReportConfigCECase getReportCECase() {
        return reportCECase;
    }

    /**
     * @param reportCECase the reportCECase to set
     */
    public void setReportCECase(ReportConfigCECase reportCECase) {
        this.reportCECase = reportCECase;
    }

    /**
     * @return the editModeCurrentCase
     */
    public boolean isEditModeCurrentCase() {
        return editModeCurrentCase;
    }

    /**
     * @param editModeCurrentCase the editModeCurrentCase to set
     */
    public void setEditModeCurrentCase(boolean editModeCurrentCase) {
        this.editModeCurrentCase = editModeCurrentCase;
    }

   

}
