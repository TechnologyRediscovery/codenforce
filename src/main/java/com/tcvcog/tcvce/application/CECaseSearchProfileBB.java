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

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
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
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.PageModeEnum;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECase;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECaseList;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import com.tcvcog.tcvce.entities.search.SearchParamsCECase;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import java.io.Serializable;
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
//                    onModeUpdateInit();
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
     * @param ev
     */
     public void onModeUpdateInit(ActionEvent ev){
         // nothing to do here yet since the user is selected
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
    
    
    
    public String onViolationViewButtonChange(CodeViolation cv){
        getSessionBean().setSessCodeViolation(cv);
        return "ceCaseViolations";
        
    }
    
    public String onViolationAddButtonChange(){
        
        return "ceCaseViolations";
        
        
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

}
