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
import com.tcvcog.tcvce.util.*;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveListsEnum;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 * Primary backing bean for the Code Enforcement case 
 * @author sylvia
 */
public class CECaseBB
        extends BackingBeanUtils
        implements Serializable {
    

    private CECaseDataHeavy currentCase;
    private boolean editModeCurrentCase;
    private boolean editModeCurrentCaseManager;
    private boolean editModeCurrentCloseCase;
    private boolean editModeCurrentCaseRecord;

    private int formNOVFollowupDays;
    private boolean formCurrentCaseUnitAssociated;
    private PropertyUnit formSelectedUnit;
    
    
    private List<User> userManagerOptionList;
    private List<BOBSource> bobSourceOptionList;
    
    private String formNoteText;
    private String formViolationStipCompDateExtReason;
    private ReportConfigCECase reportCECase;
    
    /*******************************************************
     *              EVENTS
    /*******************************************************/
    private List<EventCnF> managedEventList;
    private List<EventCategory> closingEventCategoryList;
    private List<EventCategory> originationEventCategoryList;
    private EventCategory formCECaseOriginationEventCat;
    private EventCategory closingEventCategorySelected;
    protected int eventPersonIDForLookup;
    
   
    
    /*******************************************************
     *              Violation collapse fields
     *              FROM ViolationBB
    /*******************************************************/
    
    private CodeViolation currentViolation;
    private boolean editModeCurrentViolation;
    
    private List<ViewOptionsActiveListsEnum> viewOptionList;
    private ViewOptionsActiveListsEnum selectedViewOption;
    
    private List<IntensityClass> severityList;
    
    private String formNoteTextViolation;
    private LocalDateTime complianceDateForm;
    private List<EnforcableCodeElement> filteredElementList;
    private List<EnforcableCodeElement> selectedElementList;
    private List<CodeViolation> selectedViolationList;
    private List<CodeViolation> selectedViolationListForBatchStipCompDateUpdate;

    private CodeSet currentCodeSet;

    private boolean formExtendStipCompUsingDate;
    private int formExtendedStipCompDaysFromToday;
    private String formExtensionEventDescr;
    

    
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
        System.out.println("CECaseBB.initBean();");
        CaseCoordinator cc = getCaseCoordinator();
        EventCoordinator ec = getEventCoordinator();
        UserCoordinator uc = getUserCoordinator();
        SystemCoordinator sysCor = getSystemCoordinator();
        
        try {
            configureCurrentCase(getSessionBean().getSessCECase());
            
            userManagerOptionList = uc.user_assembleUserListForSearch(getSessionBean().getSessUser());
            bobSourceOptionList = sysCor.getBobSourceListComplete();
            severityList = sysCor.getIntensitySchemaWithClasses(
                    getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("intensityschema_violationseverity"))
                    .getClassList();
            originationEventCategoryList = ec.getEventCategeryList(EventType.Origination);
            closingEventCategoryList = ec.getEventCategeryList(EventType.Closing);
            
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
        getSessionBean().setSessHumanListRefreshedList(null);
        
        filteredElementList = null;
        formExtendStipCompUsingDate = true;
        
        toggleAllEditModesToFalse();
        
    }
    
    /**
     * Turns all page edit modes to false
     */
    private void toggleAllEditModesToFalse(){
        
        editModeCurrentCase = false;
        editModeCurrentCaseManager = false;
        editModeCurrentCaseRecord = false;
        editModeCurrentViolation = false;
        editModeCurrentCloseCase = false;
    }
    
    /**
     * takes in a base case and sets it as the bean's current case
     * @param cse
     * @throws BObStatusException
     * @throws IntegrationException
     * @throws IntegrationException
     * @throws SearchException 
     */
    private void configureCurrentCase(CECase cse) throws BObStatusException, IntegrationException, IntegrationException, SearchException{
        CaseCoordinator cc = getCaseCoordinator();
        currentCase = cc.cecase_assembleCECaseDataHeavy(cc.cecase_getCECase(cse.getCaseID(), getSessionBean().getSessUser()),getSessionBean().getSessUser());
    }
    
 
    
    /**
     * Sets the session event
     * @param ev 
     */
    public void injectSessionEvent(EventCnF ev){
        if(ev != null){
            getSessionEventConductor().setSessEvent(ev);
        }
        
    }

   
    

    
    
    /*******************************************************
     *              Case Details form listeners
    /*******************************************************/
    
    
    
    /**
     * Listener for user requests to start case updates or end them
     * @param ev 
     */
    public void onToggleCECaseEditButtonChange(ActionEvent ev){
        CaseCoordinator cc = getCaseCoordinator();
        System.out.println("CECaseBB.onToggleCECaseEditButtonChange | top of method case edit mode: " + editModeCurrentCase);
        
        // if we don't have a case, then don't do anything.
        if(currentCase == null){
            return;
        }
        if(editModeCurrentCase){
            // We're writing edits
            if(formCurrentCaseUnitAssociated && formSelectedUnit != null){
                currentCase.setPropertyUnitID(formSelectedUnit.getUnitID());
            }
            try {
                System.out.println("CECaseBB.onToggleCECaseEditButtonChange | sending meta data " + currentCase.getCaseName());
                cc.cecase_updateCECaseMetadata(currentCase, getSessionBean().getSessUser());
                cc.cecase_checkForAndUpdateCaseOriginationEventCategory(currentCase, formCECaseOriginationEventCat, getSessionBean().getSessUser());
                reloadCurrentCase();
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Case update success!", ""));
                 
            } catch (BObStatusException | IntegrationException | EventException ex) {
                System.out.println(ex);
                  getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Fatal error on case update!", ""));
            } 
        // prepare the form for EDITS
        } else {
            if(currentCase.getPropertyUnit() != null){
                formSelectedUnit = currentCase.getPropertyUnit();
                formCurrentCaseUnitAssociated = true;
            } else {
                formCurrentCaseUnitAssociated = false;
            }
            
            if(currentCase.getOriginationEvent() != null){
                formCECaseOriginationEventCat = currentCase.getOriginationEvent().getCategory();
            }
            
        }
        editModeCurrentCase = !editModeCurrentCase;
    }

    /**
     * Listener for user requests to abort case record update
     * @param ev 
     */
    public void onEditCECaseAbort(ActionEvent ev){
        editModeCurrentCase = false;
        System.out.println("CECaseBB.onEditCECaseAbort | edit mode edit case: " + editModeCurrentCase);
    }
    
    
    
    
    /**
     * Listener for toggling edit mode on and then saving manager edits
     * @param ev 
     */
    public void onToggleCECaseManagerEdit(ActionEvent ev){
        
        CaseCoordinator cc = getCaseCoordinator();
        if(editModeCurrentCaseManager){
            try {
                cc.cecase_updateCECaseMetadata(currentCase, getSessionBean().getSessUser());
                reloadCurrentCase();
                  getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Manager edit complete", ""));
            } catch (BObStatusException | IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Fatal error updating case manager. Please log a ticket.", ""));
            } 
        }
        editModeCurrentCaseManager = !editModeCurrentCaseManager;
    }
    
    
    
    /**
     * Listener for cancellation of manager edit
     * @param ev 
     */
    public void onEditManagerAbort(ActionEvent ev){
        
        editModeCurrentCaseManager = false;
        System.out.println("CECaseBB.onEditManagerAbort | edit mode edit manager: " + editModeCurrentCaseManager);
         getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Aborted: Manager edit operation.", ""));
    }
    
    
    /**
     * Listener for toggling edit mode on and then saving manager edits
     * @param ev 
     */
    public void onToggleEditCECaseRecordStatus(ActionEvent ev){
        
        CaseCoordinator cc = getCaseCoordinator();
        if(editModeCurrentCaseRecord){
            try {
                cc.cecase_updateCECaseMetadata(currentCase, getSessionBean().getSessUser());
                reloadCurrentCase();
                  getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Record edit complete", ""));
            } catch (BObStatusException | IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Fatal error udating case manager. Please log a ticket.", ""));
            } 
        }
        editModeCurrentCaseRecord = !editModeCurrentCaseRecord;
    }
    
    
    
    /**
     * Listener for cancellation of manager edit
     * @param ev 
     */
    public void onEditRecordAbort(ActionEvent ev){
        
        editModeCurrentCaseRecord = false;
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Aborted: Case record edit operation.", ""));
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
            System.out.println("ceCaseBB.getReloadCECaseTrigger: "+ getPrettyDateNoTime(trigger));
            trigger = null;
            getSessionBean().setSessCECaseRefreshTrigger(trigger);
        }
        return trigger;
        
    }
    
    /**
     * provides the ID of the component for accessory UIs to refresh so the CE case is
     * refreshed. Updating this component will call getReloadCECaseTrigger which, if not null
     * triggers a reload.
     * 
     * As of May 2022, I think this is defunct. Use managed fields instead
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
            currentCase = cc.cecase_assembleCECaseDataHeavy(cc.cecase_getCECase(currentCase.getCaseID(), getSessionBean().getSessUser()), getSessionBean().getSessUser());
            getSessionBean().setSessCECase(currentCase);
            getSessionBean().setNoteholderRefreshTimestampTrigger(LocalDateTime.now());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Refreshed case!", ""));
        } catch (BObStatusException  | IntegrationException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Could not refresh current case", ""));
        }
        
    }
    
    /**
     * Listener for UI requests to directly reload case
     * @param ev 
     */
    public void reloadCaseListener(ActionEvent ev){
        reloadCurrentCase();
    }
    
  
   
    /**
     * Listener to start case deac process
     * @param ev 
     */
     public void onCaseRemoveInitButtonChange(ActionEvent ev){
         System.out.println("CECaseBB.onCaseRemoveInitButtonChange");
         
         
     }
    /**
     * Listener for user requests to deactivate a cecase
     */
    public void onCaseRemoveCommitButtonChange(ActionEvent ev){
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
        
        
    }
    
    /**
     * Listener for user requests to start the case force close operation
     * @param ev 
     */
    public void onCaseCloseInitButtonChange(ActionEvent ev){
        editModeCurrentCloseCase = true;
        if(currentCase != null){
            currentCase.setClosingDate(LocalDateTime.now());
        }
        System.out.println("CECaseBB.onCaseCloseInitButtonChange | edit mode case close: " + editModeCurrentCloseCase);
        
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
     * Listener for user requests to abort the case closure operation
     * @param ev 
     */
    public void onCaseCloseAbortLinkClick(ActionEvent ev){
        editModeCurrentCase = false;
        
        
    }
    
    public void onCaseRepenInitLinkClikc(ActionEvent ev){
        
        
        
    }
    
   
    
    public void onHowToNextStepButtonChange(ActionEvent ev){
        System.out.println("ceCaseBB.onHowToNextStepButtonChange");
        
    }
    
    
    /**
     * Special listener for field inspection lists from the CECase
     * I check the session bean for an updated list each call
     * @return a list, perhaps with some freshly written inspections
     */
    public List<FieldInspection> getManagedCECaseFieldInspectionList(){
        List<FieldInspection> finlist = getSessionBean().getSessFieldInspectionListForRefresh();
        if(finlist != null){
            currentCase.setInspectionList(finlist);
            getSessionBean().setSessFieldInspectionListForRefresh(null);
            reloadCurrentCase();
        } else {
            return currentCase.getInspectionList();
        }
        return finlist;
        
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
        System.out.println("ceCaseBB.getBlobLightListFromCECase ");
        List<BlobLight> sessBlobListForUpdate = getSessionBean().getSessBlobLightListForRefreshUptake();
        if(sessBlobListForUpdate != null && currentCase != null){
            System.out.println("ceCaseBB.getBlobLightListFromCECase | found non-null session blob list for uptake: " + sessBlobListForUpdate.size());
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
        } catch (IntegrationException | BObStatusException | BlobException ex) {
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
   
    
    /**
     * Listener for users to view a violation
     * @param cv 
     */
    public void onViolationView(CodeViolation cv){
        currentViolation = cv;
    }
    
    /**
     * User toggling violation edit mode
     * @param ev 
     */
    public void onToggleViolationUpdateButtonPress(ActionEvent ev){
        System.out.println("CECaseBB.onToggleViolationUpdateButtonPress | mode in " + editModeCurrentViolation);
        if(editModeCurrentViolation){
            try {
                onViolationUpdateCommitButtonChange();
            } catch (IntegrationException | BObStatusException ex) {
                System.out.println(ex);
            } 
        } else {
            
        }
        editModeCurrentViolation = !editModeCurrentViolation;
    }
    
    /**
     * Listener for user abort requests
     * @param ev 
     */
    public void onViolationUpdateAbort(ActionEvent ev){
        editModeCurrentViolation = false;   
        System.out.println("CECaseBB.onViolationUpdateAbort | edit mode is now: " + editModeCurrentViolation);
    }
    
     /**
     * Listener for user reqeusts to commit updates to a codeViolation
     *
     * @throws IntegrationException
     * @throws BObStatusException
     */
    public void onViolationUpdateCommitButtonChange() throws IntegrationException, BObStatusException {
        CaseCoordinator cc = getCaseCoordinator();
        EventCoordinator eventCoordinator = getEventCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        CodeCoordinator codec = getCodeCoordinator();
        EventCategory ec = eventCoordinator.initEventCategory(
                Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE).getString("updateViolationEventCategoryID")));

        try {
            if(currentCase != null && currentViolation != null){
                cc.violation_updateCodeViolation(currentCase, currentViolation, getSessionBean().getSessUser());
                if(currentViolation.isMakeFindingsDefault()){
                    makeViolationFindingsDefault(currentViolation);
                      getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Default findings for " + currentViolation.getViolatedEnfElement().getHeaderString() + " are now " + currentViolation.getViolatedEnfElement().getDefaultViolationDescription(), ""));
                    System.out.println("CECaseBB.onViolationUpdateCommitButton | " 
                            + "Default findings for " + currentViolation.getViolatedEnfElement().getHeaderString() 
                            + " are now " + currentViolation.getViolatedEnfElement().getDefaultViolationDescription());
                    
                }
                // if update succeeds without throwing an error, then generate an
                // update violation event
                // TODO: Rewire this to work with new event processing cycle
    //             eventCoordinator.generateAndInsertCodeViolationUpdateEvent(getCurrentCase(), currentViolation, event);
                reloadCurrentCase();
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Success! Violation updated, ID: " + currentViolation.getViolationID(), ""));
            }
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
    }

    
    
    /**
     * Listener for user requests to start the compliance recording operation
     * @param viol 
     */
    public void onViolationRecordComplianceInitButtonChange(CodeViolation viol){
        System.out.println("CeCaseSearchProfileBB.onViolationRecordComplianceInitButtonChange | from table");
        currentViolation = viol;
        currentViolation.setComplianceTimeStamp(LocalDateTime.now());
       
    }
    
    /**
     * Listener for user requests to record compliance from the violation
     * details dialog (not the violation table, in which case the violation
     * object is accepted as a parameter
     * @param ev 
     */
    public void onViolationRecordComplianceInitButtonChange(ActionEvent ev){
        onViolationRecordComplianceInitButtonChange(currentViolation);
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
        try {
            cv = cc.violation_injectOrdinance(cc.violation_getCodeViolationSkeleton(currentCase), ece);
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    ex.getMessage(), ""));
        }
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
     * @param ev
    */
    public void onViolationAddCommitButtonChange(ActionEvent ev) {


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
                    System.out.println("ceCaseBB.onViolationAddCommmitButtonChange | attached violation to case");
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
            reloadCurrentCase();
        }
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
     * @param ev
     */
    public void onViolationExtendStipCompDateCommitButtonChange(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            if (!formExtendStipCompUsingDate) {
                    currentViolation.setStipulatedComplianceDate(LocalDateTime.now().plusDays(formExtendedStipCompDaysFromToday));
            } 
            cc.violation_extendStipulatedComplianceDate(currentViolation, formViolationStipCompDateExtReason, currentCase, getSessionBean().getSessUser());
        } catch (BObStatusException | IntegrationException | ViolationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.toString(), ""));
        } 
        getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "Stipulated compliance dates is now: " + DateTimeUtil.getPrettyDate(getCurrentViolation().getStipulatedComplianceDate()), ""));
        reloadCurrentCase();

    }
    
    

   
     /**
     * Listener for user requests to start the update stip date operation
     * @param ev 
     */
    public void onViolationUpdateDORInitButtonChange(ActionEvent ev){
        System.out.println("ceCaseBB.onViolationUpdateDORInitButtonChange");
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
     * Listener for user requests to start the update stip date operation from a single violation
     * 
     * @param ev 
     */
    public void onViolationUpdateStipDateInitButtonChange(ActionEvent ev){
        System.out.println("ceCaseBB.onViolationUpdateStipDateInitButtonChange");
        // nothing to do here yet
        initViolationUpdateStipCompDateBatch();
        if(currentViolation != null){
            currentViolation.setQueuedForStipCompExtDate(true);
        }
    }
    
    /**
     * Responds to requests from the dashboard for updates stip comp dates on a case
     * @param cse 
     */
    public void onViolationUpdateStipCompDateBatch(CECase cse){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            configureCurrentCase(cse);
        } catch (BObStatusException | IntegrationException | SearchException ex) {
            System.out.println(ex);
        } 
        initViolationUpdateStipCompDateBatch();
        
    }
    
    
    
    
    /**
     * Creates list of violations eligible for stip comp date extensions
     */
    private void initViolationUpdateStipCompDateBatch(){
        selectedViolationListForBatchStipCompDateUpdate = new ArrayList<>();
        List<CodeViolation> eleVList = currentCase.getViolationListUnresolved();
        if(currentCase != null && eleVList != null && !eleVList.isEmpty() ){
            selectedViolationListForBatchStipCompDateUpdate.addAll(eleVList);
        }
        // turn off all queue flags from previous clicks
        for(CodeViolation cv: selectedViolationListForBatchStipCompDateUpdate){
            cv.setQueuedForStipCompExtDate(false);
        }
    }
    
    
    
    
    
    
    /**
     * Listener for user requests to commit updates to a codeViolation
     *
     * @param ev
     * @throws IntegrationException
     * @throws BObStatusException
     */
    public void onViolationUpdateStipDateCommitButtonChange(ActionEvent ev) throws IntegrationException, BObStatusException {
        CaseCoordinator cc = getCaseCoordinator();
        EventCoordinator eventCoordinator = getEventCoordinator();

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
            reloadCurrentCase();
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

    }
    
    
    /**
     * Internal tool for updating default findings on a violation
     * 
     * @param cv 
     */
    private void makeViolationFindingsDefault(CodeViolation cv){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.violation_makeFindingsDefaultInCodebook(cv, getSessionBean().getSessUser() , false);
            getSessionBean().refreshSessionCodeBook();
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
     * @param ev
     */
    public void onViolationRecordComplianceCommitButtonChange(ActionEvent ev) {
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
            reloadCurrentCase();
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Compliance recorded for Ordinacnce " + currentViolation.getViolationID(), ""));

            // ************ TODO: Finish me with events ******************//
            // ************ TODO: Finish me with events ******************//
            e = ec.generateViolationComplianceEvent(currentViolation);
            e.setCreatedBy(getSessionBean().getSessUser());
            e.setTimeStart(LocalDateTime.now());

            // ************ TODO: Finish me with events ******************//
            // ************ TODO: Finish me with events ******************//

//               getFacesContext().addMessage(null,
//                new FacesMessage(FacesMessage.SEVERITY_INFO,
//                        "Compliance event attached to case", ""));
        } catch (IntegrationException | BObStatusException | ViolationException | EventException ex) {
            System.out.println(ex);
               getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        ex.toString(), ""));
        }

    }

    /**
     * reloads only the current violation, not the whole case
     */
    private void reloadCurrentViolationList(){
        CaseCoordinator cc = getCaseCoordinator();
        if(currentViolation != null){
            try {
                currentViolation = cc.violation_getCodeViolation(currentViolation.getViolationID());
                reloadCurrentCase();
            } catch (IntegrationException | BObStatusException ex) {
                System.out.println(ex);
            } 
        }
    }
    
    
    /**
     * Listener for commencement of note writing process
     *
     * @param ev
     */
    public void onViolationNotesInitButtonChange(ActionEvent ev) {
        formNoteTextViolation = "";

    }
    
    
   
    
    
    
    /**
     * Special wrapper getter method for blobs on a code violation that checks session
     * for updates on each getter call
     * 
     * @return 
     */
    public List<BlobLight> getManagedViolationBlobList(){
        List<BlobLight> blist = getSessionBean().getSessBlobLightListForRefreshUptake();
        if(currentViolation != null){
            if(blist != null){
                System.out.println("CECaseBB.getManagedViolationBlobList | found refreshed BLOB list for violation ID " + currentViolation.getViolationID());
                currentViolation.setBlobList(blist);
                getSessionBean().setSessBlobLightListForRefreshUptake(null);
            }
            return currentViolation.getBlobList();
        }
        return new ArrayList<>();
    }

    /**
     * Listener for user requests to commit new note content to the current
     * violation
     *
     * @param ev
     */
    public void onViolationNoteCommitButtonChange(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();
        if(currentViolation == null){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fatal error appending note: no violation on cecaseBB!", ""));
            return;
        }
        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setCred(getSessionBean().getSessUser().getKeyCard());
        mbp.setExistingContent(currentViolation.getNotes());
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
     * @param ev
     */
    public void onViolationAddAbortButtonChange(ActionEvent ev){
        System.out.println("CECaseBB.onViolationAddAbortButtonChange");  //reload our current page with dialogs closed
        
    }

    /**
     * Listener for user requests to remove a violation from a case
     *
     * @param ev
     */
    public void onViolationRemoveCommitButtonChange(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.violation_deactivateCodeViolation(getCurrentViolation(), getSessionBean().getSessUser());
            reloadCurrentCase();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Violation deactivated with ID: " + currentViolation.getViolationID(), null));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), null));
        }
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
        System.out.println("ceCaseBB.generateReportCECase");
        CaseCoordinator cc = getCaseCoordinator();

        reportCECase.setCreator(getSessionBean().getSessUser());
        reportCECase.setMuni(getSessionBean().getSessMuni());
        reportCECase.setGenerationTimestamp(LocalDateTime.now());
        reportCECase.setCse(currentCase);
        
        try {
            setReportCECase(cc.report_prepareCECaseReport(reportCECase, getSessionBean().getSessUser()));
        } catch (IntegrationException | BObStatusException | SearchException | BlobException ex) {
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
    
    
    
    
    /**
     * Special getter wrapper around the cease's human link list that can be managed
     * by a shared UI for person link management and search
     * @return the current Case's human link list
     */
    public List<HumanLink> getManagedCECaseHumanLinkList(){
        List<HumanLink> hll = getSessionBean().getSessHumanListRefreshedList();
        if(hll != null){
            currentCase.setHumanLinkList(hll);
            getSessionBean().setSessHumanListRefreshedList(null);
        }
        return currentCase.getHumanLinkList();
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

    /**
     * @return the formExtensionEventDescr
     */
    public String getFormExtensionEventDescr() {
        return formExtensionEventDescr;
    }

    /**
     * @param formExtensionEventDescr the formExtensionEventDescr to set
     */
    public void setFormExtensionEventDescr(String formExtensionEventDescr) {
        this.formExtensionEventDescr = formExtensionEventDescr;
    }

    /**
     * @return the editModeCurrentCaseManager
     */
    public boolean isEditModeCurrentCaseManager() {
        return editModeCurrentCaseManager;
    }



    /**
     * @return the editModeCurrentCloseCase
     */
    public boolean isEditModeCurrentCloseCase() {
        return editModeCurrentCloseCase;
    }

    /**
     * @return the editModeCurrentCaseRecord
     */
    public boolean isEditModeCurrentCaseRecord() {
        return editModeCurrentCaseRecord;
    }

    /**
     * @param editModeCurrentCaseManager the editModeCurrentCaseManager to set
     */
    public void setEditModeCurrentCaseManager(boolean editModeCurrentCaseManager) {
        this.editModeCurrentCaseManager = editModeCurrentCaseManager;
    }

   
    /**
     * @param editModeCurrentCloseCase the editModeCurrentCloseCase to set
     */
    public void setEditModeCurrentCloseCase(boolean editModeCurrentCloseCase) {
        this.editModeCurrentCloseCase = editModeCurrentCloseCase;
    }

    /**
     * @param editModeCurrentCaseRecord the editModeCurrentCaseRecord to set
     */
    public void setEditModeCurrentCaseRecord(boolean editModeCurrentCaseRecord) {
        this.editModeCurrentCaseRecord = editModeCurrentCaseRecord;
    }

    /**
     * @return the formCurrentCaseUnitAssociated
     */
    public boolean getFormCurrentCaseUnitAssociated() {
        return formCurrentCaseUnitAssociated;
    }

    /**
     * @param formCurrentCaseUnitAssociated the formCurrentCaseUnitAssociated to set
     */
    public void setFormCurrentCaseUnitAssociated(boolean formCurrentCaseUnitAssociated) {
        this.formCurrentCaseUnitAssociated = formCurrentCaseUnitAssociated;
    }

    /**
     * @return the formSelectedUnit
     */
    public PropertyUnit getFormSelectedUnit() {
        return formSelectedUnit;
    }

    /**
     * @param formSelectedUnit the formSelectedUnit to set
     */
    public void setFormSelectedUnit(PropertyUnit formSelectedUnit) {
        this.formSelectedUnit = formSelectedUnit;
    }

    /**
     * @return the originationEventCategoryList
     */
    public List<EventCategory> getOriginationEventCategoryList() {
        return originationEventCategoryList;
    }

    /**
     * @param originationEventCategoryList the originationEventCategoryList to set
     */
    public void setOriginationEventCategoryList(List<EventCategory> originationEventCategoryList) {
        this.originationEventCategoryList = originationEventCategoryList;
    }

    /**
     * @return the formCECaseOriginationEventCat
     */
    public EventCategory getFormCECaseOriginationEventCat() {
        return formCECaseOriginationEventCat;
    }

    /**
     * @param formCECaseOriginationEventCat the formCECaseOriginationEventCat to set
     */
    public void setFormCECaseOriginationEventCat(EventCategory formCECaseOriginationEventCat) {
        this.formCECaseOriginationEventCat = formCECaseOriginationEventCat;
    }

    /**
     * @return the selectedViolationListForBatchStipCompDateUpdate
     */
    public List<CodeViolation> getSelectedViolationListForBatchStipCompDateUpdate() {
        return selectedViolationListForBatchStipCompDateUpdate;
    }

    /**
     * @param selectedViolationListForBatchStipCompDateUpdate the selectedViolationListForBatchStipCompDateUpdate to set
     */
    public void setSelectedViolationListForBatchStipCompDateUpdate(List<CodeViolation> selectedViolationListForBatchStipCompDateUpdate) {
        this.selectedViolationListForBatchStipCompDateUpdate = selectedViolationListForBatchStipCompDateUpdate;
    }

   

}
