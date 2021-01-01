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

import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.BlobTypeException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CECasePropertyUnitHeavy;
import com.tcvcog.tcvce.entities.CaseStageEnum;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.IntensityClass;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.PageModeEnum;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECase;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECaseList;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import com.tcvcog.tcvce.entities.search.SearchParamsCECase;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveListsEnum;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import org.primefaces.event.FileUploadEvent;

/**
 *
 * @author sylvia
 */
public class CECaseSearchProfileBB
        extends BackingBeanUtils
        implements Serializable {

    
    private PageModeEnum currentMode;
    private List<PageModeEnum> pageModes;

    private CECaseDataHeavy currentCase;
    private boolean currentCaseSelected;

    private int formNOVFollowupDays;
    private List<CECasePropertyUnitHeavy> caseList;
    private List<CECasePropertyUnitHeavy> filteredCaseList;
    
    private SearchParamsCECase searchParamsSelected;
    private List<QueryCECase> queryList;
    private QueryCECase querySelected;
    private boolean appendResultsToList;
    
    private List<EventCategory> closingEventCategoryList;
    private EventCategory closingEventCategorySelected;
    
    private ViewOptionsActiveHiddenListsEnum eventViewOptionSelected;
    private List<ViewOptionsActiveHiddenListsEnum> eventViewOptions;
    
    private List<User> userManagerOptionList;
    private List<BOBSource> bobSourceOptionList;
    
    private String formNoteText;
    
    private List<Property> propListForSearch;
    private CaseStageEnum[] caseStageList;

    private ReportConfigCECase reportCECase;
    private ReportConfigCECaseList reportCECaseList;

    
    
    
    
    /*******************************************************
     *              Violation collapse fields
     *              FROM ViolationBB
    /*******************************************************/
    
    private CodeViolation currentViolation;
    private List<ViewOptionsActiveListsEnum> viewOptionList;
    private ViewOptionsActiveListsEnum selectedViewOption;
    
    private List<IntensityClass> severityList;
    
    private String formNoteTextViolation;
    private List<EnforcableCodeElement> filteredElementList;
    private CodeSet currentCodeSet;

    private boolean extendStipCompUsingDate;
    private java.util.Date extendedStipCompDate;
    private int extendedStipCompDaysFromToday;

    
    
    
    /**
     * Creates a new instance of CECaseSearchBB
     */
    public CECaseSearchProfileBB() {
    }

    /**
     * Sets up primary case list and aux lists for editing, and searching
     * This is a hybrid bean that coordinates both CECase search and 
     * list viewing functions
     */
    @PostConstruct
    public void initBean() {
        CaseCoordinator cc = getCaseCoordinator();
        SearchCoordinator sc = getSearchCoordinator();
        UserCoordinator uc = getUserCoordinator();
        SystemCoordinator sysCor = getSystemCoordinator();
        EventCoordinator ec = getEventCoordinator();
        

        SessionBean sb = getSessionBean();
        queryList = sc.buildQueryCECaseList(getSessionBean().getSessUser().getMyCredential());
        querySelected = getSessionBean().getQueryCECase();

        configureParameters();

        caseList = new ArrayList<>();
        caseList.addAll(cc.cecase_refreshCECasePropertyUnitHeavyList(sb.getSessCECaseList()));
        CECase cseTemp = getSessionBean().getSessCECase();
        try {
            if(cseTemp == null){
                if(caseList != null && !caseList.isEmpty()){
                    cseTemp = caseList.get(0);
                } else {
                    cseTemp = cc.cecase_selectDefaultCECase(sb.getSessUser());
                }
            }
            currentCase = cc.cecase_assembleCECaseDataHeavy(cseTemp, getSessionBean().getSessUser());
            System.out.println("CECaseSearchProfile.initBean(): current case ID: " + currentCase.getCaseID());
            closingEventCategoryList = ec.getEventCategeryList(EventType.Closing);
            
        } catch (IntegrationException | SearchException | BObStatusException ex) {
            System.out.println(ex);
        }


        propListForSearch = sb.getSessPropertyList();
        caseStageList = CaseStageEnum.values();
        
        eventViewOptions = Arrays.asList(ViewOptionsActiveHiddenListsEnum.values());
        setEventViewOptionSelected(ViewOptionsActiveHiddenListsEnum.VIEW_ACTIVE_NOTHIDDEN);

        userManagerOptionList = uc.user_assembleUserListForSearch(getSessionBean().getSessUser());
        bobSourceOptionList = sysCor.getBobSourceListComplete();

        ReportConfigCECaseList listRpt = getSessionBean().getReportConfigCECaseList();
        
        if (listRpt != null) {
            reportCECaseList = listRpt;
        } else {
            reportCECaseList = cc.report_getDefaultReportConfigCECaseList();
        }
        
        
        ReportConfigCECase rpt = getSessionBean().getReportConfigCECase();
        
        if (rpt != null) {
            rpt.setTitle("Code Enforcement Case Summary");
            reportCECase = rpt;
        }
        
        pageModes = new ArrayList<>();
        pageModes.add(PageModeEnum.LOOKUP);
        pageModes.add(PageModeEnum.INSERT);
        pageModes.add(PageModeEnum.UPDATE);
        pageModes.add(PageModeEnum.REMOVE);
        if(getSessionBean().getCeCaseSearchProfilePageModeRequest() != null){
            setCurrentMode(getSessionBean().getCeCaseSearchProfilePageModeRequest());
        } 
        setCurrentMode(pageModes.get(0));
     
        // NOV
        formNOVFollowupDays = getSessionBean().getSessMuni().getProfile().getNovDefaultDaysForFollowup();
        if(formNOVFollowupDays == 0){
            formNOVFollowupDays = 20;
        }
        
        
        // VIOLATION STUFF FROM VIOLATIONBB
        
         currentViolation = getSessionBean().getSessCodeViolation();
            if (currentViolation == null) {
                if (currentCase != null && !currentCase.getViolationList().isEmpty()) {
                    currentViolation = currentCase.getViolationList().get(0);
                }
            }
        try{
            
            severityList = sysCor.getIntensitySchemaWithClasses(
                    getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("intensityschema_violationseverity"))
                    .getClassList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));

        }
        
        filteredElementList = null;
        extendStipCompUsingDate = true;
        currentCodeSet = getSessionBean().getSessMuni().getCodeSet();

    }

    /**
     * Loads the first parameter bundle of a selected query object
     */
    private void configureParameters() {
        if (querySelected != null
                && querySelected.getParamsList() != null
                && !querySelected.getParamsList().isEmpty()) {

            searchParamsSelected = querySelected.getParamsList().get(0);

        } else {
            searchParamsSelected = null;
        }
    }
    
    /**
     * Listener for requests to reload the current CECaseDataHeavy
     * @param ev 
     */
    public void refreshCurrentCase(ActionEvent ev){
        reloadCase();
    }
    
    public void reloadCase(){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            currentCase = cc.cecase_assembleCECaseDataHeavy(currentCase, getSessionBean().getSessUser());
        } catch (BObStatusException  | IntegrationException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Could not refresh current case", ""));
        }
        
    }
    
     /**
     * Responds to the user clicking one of the page modes: LOOKUP, ADD, UPDATE, REMOVE
     * @param mode     
     */
    public void setCurrentMode(PageModeEnum mode) {

        //store currentMode into tempCurMode as a temporary value, in case the currenMode equal null
        PageModeEnum tempCurMode = this.currentMode;
        //reset default setting every time the Mode has been selected 
//        loadDefaultPageConfig();
        //check the currentMode == null or not
        if (mode == null) {
            this.currentMode = tempCurMode;
        } else {
            this.currentMode = mode;
            System.out.println("CECaseSearchProfileBB.setCurrentMode: " + currentMode.getTitle());
            switch(currentMode){
                case LOOKUP:
                    onModeLookupInit();
                    break;
                case INSERT:
                    onModeInsertInit();
                    break;
                case UPDATE:
                    onModeUpdateInit();
                    break;
                case REMOVE:
                    onModeRemoveInit();
                    break;
                default:
                    break;
                    
            }
        }
    }
    
    
    //check if current mode == Lookup
    public boolean getActiveLookupMode() {
        // hard-wired on since there's always a property loaded
        return PageModeEnum.LOOKUP.equals(currentMode) ;
    }

    /**
     * Provide UI elements a boolean true if the mode is UPDATE
     * @return 
     */
    public boolean getActiveUpdateMode(){
        return PageModeEnum.UPDATE.equals(currentMode);
    }


    //check if current mode == Insert
    public boolean getActiveInsertUpdateMode() {
        return PageModeEnum.INSERT.equals(currentMode) || PageModeEnum.UPDATE.equals(currentMode);
    }

    //check if current mode == Insert
    public boolean getActiveInsertMode() {
        return PageModeEnum.INSERT.equals(currentMode);
    }

    //check if current mode == Remove
    public boolean getActiveRemoveMode() {
        return PageModeEnum.REMOVE.equals(currentMode);
    }

    /**
     * Listener for user requests to start case updates
     * @param ev 
     */
    public void onCaseSettingsInitButtonChnage(ActionEvent ev){
        // nothing to do yet
    }

    
    /**
     * Primary listener method which copies a reference to the selected 
     * user from the list and sets it on the selected user perch
     * @param cse
     * @return 
     */
    public String onObjetViewButtonChange(CECase cse){
        CaseCoordinator cc = getCaseCoordinator();
        
        if(cse != null){
            try {
                currentCase = cc.cecase_assembleCECaseDataHeavy(cse, getSessionBean().getSessUser());
                getSessionBean().setSessCECase(currentCase);
                getSessionBean().setSessProperty(currentCase.getPropertyID());
            } catch (IntegrationException | BObStatusException | SearchException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null, 
                      new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                      "Unable to assemble case data heavy " + ex.getMessage(), ""));
            }
        }
        System.out.println("CECaseSearchProfileBB.currentCase: " + currentCase.getCaseID());
        return "";
    }
     
 
    /**
     * Internal logic container for changes to page mode: Lookup
     */
    private void onModeLookupInit(){
    }
    
   
    
    /**
     * Internal logic container for beginning the user creation change process
     * Delegated from the mode button router
     */
    public void onModeInsertInit(){
    }
    
    /**
     * Listener for user requests to open new case at current property
     * @return 
     */
    public String onCaseOpenButtonChange(){
        getSessionBean().getNavStack().pushPage("ceCaseSearchProfile");
        return "caseAdd";
        
    }
    
    /**
     * Listener for beginning of update process
     */
     public void onModeUpdateInit(){
         // nothign to do here yet since the user is selected
     }
     
     /**
      * Listener for user requests to commit updates
      * @return 
      */
     public String onCaseUpdateButtonChange(){
         CaseCoordinator cc = getCaseCoordinator();
        
        try {
            cc.cecase_updateCECaseMetadata(currentCase);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Case metadata updated", ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Could not update case metadata, sorry! This error must be corrected by an administrator", ""));
            
        }
         
         return "ceCaseSearchProfile";
    }
     
    
     /**
      * Listener for the start of the case remove process
      */
     public void onModeRemoveInit(){
       
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
        return "ceCaseSearchProfile";
        
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
        return "ceCaseSearchProfile";
        
    }
    
      /**
     * Listener for commencement of note writing process
     *
     * @param ev
     */
    public void onNoteInitButtonChange(ActionEvent ev) {
        formNoteText = new String();

    }

    
    
    /**
     * Listener for user requests to commit new note content to the current
     * Property
     *
     * @param ev
     * @return 
     */
    public String onNoteCommitButtonChange(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();
        
        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setCred(getSessionBean().getSessUser().getKeyCard());
        mbp.setExistingContent(currentCase.getNotes());
        mbp.setNewMessageContent(formNoteText);
        mbp.setHeader("Case Note");
        mbp.setUser(getSessionBean().getSessUser());
       
        try {
            
            cc.cecase_updateCECaseNotes(mbp, currentCase);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succesfully appended note!", ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fatal error appending note; apologies!", ""));

        }

        return "ceCaseSearchProfile";

    }
    
    public String onProposalViewButtonChange(Proposal prop){
        
        return "workflow";
        
    }
    
    public String onProposalsListButtonChange(){
        return "workflow";
        
    }
    
    public String onEventViewButtonChange(EventCnF ev){
        getSessionBean().setSessEvent(ev);
        return "events";
        
    }
    
    public String onEventAddButtonChange(){
        EventCoordinator ec = getEventCoordinator();
        try {
            getSessionBean().setSessEvent(ec.initEvent(currentCase, null));
        } catch (BObStatusException | EventException ex) {
            System.out.println(ex);
        } 
        return "events";
        
        
    }
    
    public String onCEARViewButtonChange(CEActionRequest cear){
        getSessionBean().setSessCEAR(cear);
        return "cEActionRequests";
        
    }
    
    
    

    
    
    
    
    public String onNOVViewButtonChange(NoticeOfViolation nov){
        getSessionBean().setSessNotice(nov);
        return "ceCaseNotices";
        
    }
    
    public void onHowToNextStepButtonChange(ActionEvent ev){
        System.out.println("ceCaseSearchProfileBB.onHowToNextStepButtonChange");
        
    }
    
    
    
    
    
    public String onCitationViewButtonChange(Citation cit){
        getSessionBean().setSessCitation(cit);
        return "ceCaseCitations";
        
        
    }
    
    public String onCitationAddButtonChange(){
        
        return "ceCaseCitations";
    }
    
    public String onBlobViewButtonChange(Blob blob){
        return "blobs";
        
    }
    
    public String onBlobAddButtonChange(){
        
        return "blobs";
    }
    

    /**
     * Listener for requests to go view the property profile of a property associated
     * with the given case
     * @return 
     */
    public String exploreProperty(){
        try {
            getSessionBean().setSessProperty(currentCase.getPropertyID());
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
     * Listener for user requests to build a code enforcement case
     * 
     * @return 
     */
    public String generateReportCECase() {
        System.out.println("CECaseSearchProfileBB.generateReportCECase");
        CaseCoordinator cc = getCaseCoordinator();

        getReportCECase().setCse(currentCase);

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
        reportCECase = cc.report_getDefaultReportConfigCECase(currentCase);
        System.out.println("CaseProfileBB.prepareReportCECase | reportConfigOb: " + getReportCECase());

    }
    
    
    /**
     * Responder to the query button on the UI
     *
     * @param ev
     */
    public void executeQuery(ActionEvent ev) {
        System.out.println("CaseProfileBB.executeQuery: Listener");
        executeQuery();
    }

    /**
     * Logic block for executing selected query
     */
    public void executeQuery() {
        System.out.println("CECaseSearchBB.executeQuery");
        SearchCoordinator sc = getSearchCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        int listSize = 0;

        if (!appendResultsToList) {
            caseList.clear();
        }

        try {
            caseList.addAll(sc.runQuery(getQuerySelected()).getResults());
            if (caseList != null) {
                listSize = getCaseList().size();
            }
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Your query completed with " + listSize + " results", ""));
        } catch (SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not query the database, sorry.", ""));
        }
    }

    /**
     * Convenience method for accessing the size of the CECase List
     *
     * @return
     */
    public int getCaseListSize() {
        int s = 0;
        if (caseList != null && !caseList.isEmpty()) {
            s = caseList.size();
        }
        return s;

    }

    /**
     * Listener method for requests to load case history
     *
     * @param ev
     */
    public void loadCECaseHistory(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            caseList.addAll(cc.cecase_assembleCECasePropertyUnitHeavyList(cc.cecase_getCECaseHistory(getSessionBean().getSessUser())));
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Case history loaded", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not load case history, sorry.", ""));
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
    }

    /**
     * Listener method for requests to clear the search results list
     *
     * @param ev
     */
    public void clearCECaseList(ActionEvent ev) {
        if (caseList != null) {
            caseList.clear();
        }
    }

    /**
     * Listener method for changes in selected query objects
     *
     */
    public void changeQuerySelected() {
        configureParameters();
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "New query loaded!", ""));
    }

    /**
     * Action listener for query resets
     *
     * @param ev
     */
    public void resetQuery(ActionEvent ev) {
        SearchCoordinator sc = getSearchCoordinator();
        queryList = sc.buildQueryCECaseList(getSessionBean().getSessUser().getMyCredential());
        if (queryList != null && !queryList.isEmpty()) {
            querySelected = queryList.get(0);
        }
        configureParameters();
    }
    
    /**
     * 
     * @param ev 
     */
    public void prepareReportCECaseList(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();

        if (reportCECaseList == null) {
            reportCECaseList = cc.report_getDefaultReportConfigCECaseList();
        }
        reportCECaseList.setTitle("Code Enforcement Activity Report");
        reportCECaseList.setDate_start_val(LocalDateTime.now().minusDays(30));
        reportCECaseList.setDate_end_val(LocalDateTime.now());
        System.out.println("CaseProfileBB.prepareCaseListReport");

    }

    /**
     * 
     * @return 
     */
    public String generateReportCECaseList() {
        CaseCoordinator cc = getCaseCoordinator();
        reportCECaseList.setCreator(getSessionBean().getSessUser());
        reportCECaseList.setMuni(getSessionBean().getSessMuni());
        reportCECaseList.setGenerationTimestamp(LocalDateTime.now());

        try {
            reportCECaseList = cc.report_buildCECaseListReport(reportCECaseList, getSessionBean().getSessUser());
        } catch (SearchException ex) {
            System.out.println(ex);
            
        }
        getSessionBean().setReportConfigCECaseList(reportCECaseList);
        getSessionBean().setReportConfigCECase(null);
        getSessionBean().setSessReport(reportCECaseList);

        return "reportCECaseList";

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
    public void onViolationComplianceInitButtonChange(CodeViolation viol){
        currentViolation = viol;
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
    public void onViolationNullifyButtonChange(CodeViolation viol){
        currentViolation = viol;
    }
    
    
   
     /**
     * Listener for user selection of a violation from the code set violation
     * table
     *
     * @param ece
     */
    public void onViolationSelectElementButtonChange(EnforcableCodeElement ece) {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            setCurrentViolation(cc.violation_injectOrdinance(currentCase, getCurrentViolation(), ece, null));
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
    }
    
    /**
     * Listener for user requests to start the violation add process
     * @param ev
     */
    public void onViolationAddInit(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();
        System.out.println("violationBB.OnModeInsertInit");

        try {
            currentViolation = cc.violation_getCodeViolationSkeleton(currentCase);
        } catch (BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }

    }
    
      /**
     * Listener for commencement of extending stip comp date
     *
     * @param ev
     */
    public void onViolationExtendStipCompDateInitButtonChange(ActionEvent ev) {
        setExtendedStipCompDaysFromToday(CaseCoordinator.DEFAULT_EXTENSIONDAYS);
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
            if (isExtendStipCompUsingDate() && getExtendedStipCompDate() != null) {
                LocalDateTime freshDate = getExtendedStipCompDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                if (freshDate.isBefore(LocalDateTime.now())) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Stipulated compliance dates must be in the future!", ""));
                } else {
                    secBetween = freshDate.toEpochSecond(ZoneOffset.of("-4")) - LocalDateTime.now().toEpochSecond(ZoneOffset.of("-4"));
                    // divide by num seconds in a day
                    long daysBetween = secBetween / (24 * 60 * 60);
                    cc.violation_extendStipulatedComplianceDate(getCurrentViolation(), daysBetween, currentCase, getSessionBean().getSessUser());
                }
            } else {
                cc.violation_extendStipulatedComplianceDate(getCurrentViolation(), getExtendedStipCompDaysFromToday(), currentCase, getSessionBean().getSessUser());
            }
        } catch (BObStatusException | IntegrationException | ViolationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.toString(), ""));
        } 
        getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "Stipulated compliance dates is now: " + getPrettyDate(getCurrentViolation().getStipulatedComplianceDate()), ""));
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
     * Listener for user requests to commit a violation compliance event
     *
     * @return 
     */
    public String onViolationRecordComplianceCommitButtonChange() {
        EventCoordinator ec = getEventCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        
            // build event details package
            EventCnF e = null;
            try {
                
//                cc.violation_recordCompliance(currentViolation, getSessionBean().getSessUser());
                   getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Compliance recorded", ""));
                
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
            } catch (IntegrationException ex) {
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
     * @param ev
     */
    public void onViolationNotesInitButtonChange(ActionEvent ev) {
        formNoteTextViolation = null;

    }

    /**
     * Listener for user requests to commit new note content to the current
     * object
     *
     * @return
     */
    public String onNoteCommitButtonChange() {
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
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fatal error appending note; apologies!", ""));
            return "";
        }
        return "ceCaseProfile";
    }

  

    /**
     * Listener
     *
     * @param ev
     */
    public void handlePhotoUpload(FileUploadEvent ev) {
        // NADGIT TODO
//        CaseCoordinator cc = getCaseCoordinator();
//        if (ev == null) {
//            System.out.println("ViolationAddBB.handlePhotoUpload | event: null");
//            return;
//        }
//        if (this.getCurrentViolation().getBlobIDList() == null) {
//            this.getCurrentViolation().setBlobIDList(new ArrayList<Integer>());
//        }
//        if (this.blobList == null) {
//            this.blobList = new ArrayList<>();
//        }
//        
//        try {
//            BlobCoordinator blobc = getBlobCoordinator();
//            
//            Blob blob = blobc.getNewBlob();
//            blob.setBytes(ev.getFile().getContents());
//            blob.setFilename(ev.getFile().getFileName());
//            blob.setMunicode(getSessionBean().getSessMuni().getMuniCode());
//            this.getCurrentViolation().getBlobIDList().add(blobc.storeBlob(blob).getBlobID());
//            this.getBlobList().add(blob);
//        } catch (IntegrationException | IOException | ClassNotFoundException | NoSuchElementException ex) {
//            System.out.println("ViolationAddBB.handlePhotoUpload | upload failed! " + ex);
//        } catch (BlobException ex) {
//            System.out.println("ViolationAddBB.handlePhotoUpload | upload failed! " + ex);
//            getFacesContext().addMessage(null,
//                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                            ex.getMessage(), ""));
//        }
    }

    /**
     * Responds to user reqeusts to commit a new code violation to the CECase
     *
     * @return
     */
    public String onViolationAddCommitButtonChange() {

        CaseCoordinator cc = getCaseCoordinator();

        try {
            cc.violation_attachViolationToCase(getCurrentViolation(), currentCase, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Violation attached to case.", ""));
            getSessionBean().getSessionBean().setSessCodeViolation(getCurrentViolation());
            System.out.println("ViolationBB.onViolationAddCommmitButtonChange | completed violation process");
        } catch (IntegrationException | SearchException | BObStatusException | EventException | ViolationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
            return "";
        }
        return "ceCaseProfile";

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
    
    public String onViolationNullifyCommitButtonChange(){
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
     * Listener for user request to remove photo on violation
     *
     * @param photoid
     * @return
     */
    public String onPhotoRemoveButtonChange(int photoid) {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.violation_removeLinkBlobToCodeViolation(getCurrentViolation(), photoid);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Blob removed with ID " + photoid, ""));
        } catch (BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Cannot remove photo yet: unsupported operation", ""));
            
        } 

        // do something here
        return "ceCaseProfile";

    }
    
    
    /**
     * TODO: NADIT review
     * @param blob 
     */
    public void onPhotoUpdateDescription(Blob blob){
        BlobCoordinator bc = getBlobCoordinator();
        try {
            bc.updateBlobFilename(blob);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully updated photo description", ""));
            
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Cannot update photo description", ""));
            
        } catch (IOException | BlobTypeException | ClassNotFoundException ex) {
            System.out.println(ex);
        } 
        
    }

    public String photosConfirm() {
        /*  TODO: this obviously
        
        if(this.currentViolation == null){
            this.currentViolation = getSessionBean().getSessionCodeViolation();
        }
        if(this.getPhotoList() == null  ||  this.getPhotoList().isEmpty()){
            getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                "No uploaded photos to commit.", 
                                "Use the 'Return to case home without commiting photos' button bellow if you have no photos to upload."));
            return "";
        }
        
        ImageServices is = getImageServices();
        
        for(Photograph photo : this.getPhotoList()){
            
            try { 
                // commit and link
                is.commitPhotograph(photo.getPhotoID());
                is.linkPhotoToCodeViolation(photo.getPhotoID(), currentViolation.getViolationID());
                
            } catch (IntegrationException ex) {
                System.out.println(ex.toString());
                    getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                "INTEGRATION ERROR: Unable write request into the database, our apologies!", 
                                "Please call your municipal office and report your concern by phone."));
                    return "";
            }
        }
         */
        return "ceCaseProfile";
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
     * @return the caseList
     */
    public List<CECasePropertyUnitHeavy> getCaseList() {
        return caseList;
    }

    /**
     * @return the filteredCaseList
     */
    public List<CECasePropertyUnitHeavy> getFilteredCaseList() {
        return filteredCaseList;
    }

    /**
     * @return the searchParamsSelected
     */
    public SearchParamsCECase getSearchParamsSelected() {
        return searchParamsSelected;
    }

    /**
     * @return the queryList
     */
    public List<QueryCECase> getQueryList() {
        return queryList;
    }

    /**
     * @return the querySelected
     */
    public QueryCECase getQuerySelected() {
        return querySelected;
    }

    /**
     * @return the reportCECaseList
     */
    public ReportConfigCECaseList getReportCECaseList() {
        return reportCECaseList;
    }

    /**
     * @param currentCase the currentCase to set
     */
    public void setCurrentCase(CECaseDataHeavy currentCase) {
        this.currentCase = currentCase;
    }

    /**
     * @param caseList the caseList to set
     */
    public void setCaseList(List<CECasePropertyUnitHeavy> caseList) {
        this.caseList = caseList;
    }

    /**
     * @param filteredCaseList the filteredCaseList to set
     */
    public void setFilteredCaseList(List<CECasePropertyUnitHeavy> filteredCaseList) {
        this.filteredCaseList = filteredCaseList;
    }

    /**
     * @param searchParamsSelected the searchParamsSelected to set
     */
    public void setSearchParamsSelected(SearchParamsCECase searchParamsSelected) {
        this.searchParamsSelected = searchParamsSelected;
    }

    /**
     * @param queryList the queryList to set
     */
    public void setQueryList(List<QueryCECase> queryList) {
        this.queryList = queryList;
    }

    /**
     * @param querySelected the querySelected to set
     */
    public void setQuerySelected(QueryCECase querySelected) {
        this.querySelected = querySelected;
    }

    /**
     * @param reportCECaseList the reportCECaseList to set
     */
    public void setReportCECaseList(ReportConfigCECaseList reportCECaseList) {
        this.reportCECaseList = reportCECaseList;
    }

    /**
     * @return the appendResultsToList
     */
    public boolean isAppendResultsToList() {
        return appendResultsToList;
    }

    /**
     * @param appendResultsToList the appendResultsToList to set
     */
    public void setAppendResultsToList(boolean appendResultsToList) {
        this.appendResultsToList = appendResultsToList;
    }

    /**
     * @return the propListForSearch
     */
    public List<Property> getPropListForSearch() {
        return propListForSearch;
    }

    /**
     * @param propListForSearch the propListForSearch to set
     */
    public void setPropListForSearch(List<Property> propListForSearch) {
        this.propListForSearch = propListForSearch;
    }

    /**
     * @return the caseStageList
     */
    public CaseStageEnum[] getCaseStageList() {
        return caseStageList;
    }

    /**
     * @param caseStageList the caseStageList to set
     */
    public void setCaseStageList(CaseStageEnum[] caseStageList) {
        this.caseStageList = caseStageList;
    }

    /**
     * @return the currentMode
     */
    public PageModeEnum getCurrentMode() {
        return currentMode;
    }

    /**
     * @return the pageModes
     */
    public List<PageModeEnum> getPageModes() {
        return pageModes;
    }

    /**
     * @return the currentCaseSelected
     */
    public boolean isCurrentCaseSelected() {
        return currentCaseSelected;
    }

  

    /**
     * @param pageModes the pageModes to set
     */
    public void setPageModes(List<PageModeEnum> pageModes) {
        this.pageModes = pageModes;
    }

    /**
     * @param currentCaseSelected the currentCaseSelected to set
     */
    public void setCurrentCaseSelected(boolean currentCaseSelected) {
        this.currentCaseSelected = currentCaseSelected;
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
        return closingEventCategoryList;
    }

    /**
     * @param closingEventList the closingEventList to set
     */
    public void setClosingEventList(List<EventCategory> closingEventList) {
        this.closingEventCategoryList = closingEventList;
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
     * @return the extendStipCompUsingDate
     */
    public boolean isExtendStipCompUsingDate() {
        return extendStipCompUsingDate;
    }

    /**
     * @return the extendedStipCompDate
     */
    public java.util.Date getExtendedStipCompDate() {
        return extendedStipCompDate;
    }

    /**
     * @return the extendedStipCompDaysFromToday
     */
    public int getExtendedStipCompDaysFromToday() {
        return extendedStipCompDaysFromToday;
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
     * @param extendStipCompUsingDate the extendStipCompUsingDate to set
     */
    public void setExtendStipCompUsingDate(boolean extendStipCompUsingDate) {
        this.extendStipCompUsingDate = extendStipCompUsingDate;
    }

    /**
     * @param extendedStipCompDate the extendedStipCompDate to set
     */
    public void setExtendedStipCompDate(java.util.Date extendedStipCompDate) {
        this.extendedStipCompDate = extendedStipCompDate;
    }

    /**
     * @param extendedStipCompDaysFromToday the extendedStipCompDaysFromToday to set
     */
    public void setExtendedStipCompDaysFromToday(int extendedStipCompDaysFromToday) {
        this.extendedStipCompDaysFromToday = extendedStipCompDaysFromToday;
    }

}
