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
import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.BlobTypeException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CECasePropertyUnitHeavy;
import com.tcvcog.tcvce.entities.CaseStageEnum;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CitationCodeViolationLink;
import com.tcvcog.tcvce.entities.CitationFilingType;
import com.tcvcog.tcvce.entities.CitationStatus;
import com.tcvcog.tcvce.entities.CitationStatusLogEntry;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.CourtEntity;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventCnFPropUnitCasePeriodHeavy;
import com.tcvcog.tcvce.entities.DomainEnum;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Human;
import com.tcvcog.tcvce.entities.HumanLink;
import com.tcvcog.tcvce.entities.IntensityClass;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.PageModeEnum;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonDataHeavy;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECase;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECaseList;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import com.tcvcog.tcvce.entities.search.SearchParamsCECase;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.CourtEntityIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.DateTimeUtil;
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
import java.util.Map;
import java.util.NoSuchElementException;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import org.primefaces.event.FileUploadEvent;

/**
 * Primary backing bean for the Code Enforcement case 
 * @author sylvia
 */
public class CECaseSearchProfileBB
        extends BackingBeanUtils
        implements Serializable {
    
    private PageModeEnum currentMode;
    private List<PageModeEnum> pageModes;

    private CECaseDataHeavy currentCase;
    private PropertyDataHeavy currentCasePropDataHeavy;
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
    protected int eventPersonIDForLookup;
    
    private ViewOptionsActiveHiddenListsEnum eventViewOptionSelected;
    private List<ViewOptionsActiveHiddenListsEnum> eventViewOptions;
    
    private List<User> userManagerOptionList;
    private List<BOBSource> bobSourceOptionList;
    
    private String formNoteText;

    private List<Property> propListForSearch;
    private CaseStageEnum[] caseStageList;

    private ReportConfigCECase reportCECase;
    private ReportConfigCECaseList reportCECaseList;

    
    //NOV
  
    
    
    /*******************************************************
     *              Violation collapse fields
     *              FROM ViolationBB
    /*******************************************************/
    
    private CodeViolation currentViolation;
    private List<ViewOptionsActiveListsEnum> viewOptionList;
    private ViewOptionsActiveListsEnum selectedViewOption;
    
    private List<IntensityClass> severityList;
    private java.util.Date complianceDateForm;
    
    private String formNoteTextViolation;
    private List<EnforcableCodeElement> filteredElementList;
    private CodeSet currentCodeSet;

    private boolean extendStipCompUsingDate;
    private java.util.Date extendedStipCompDate;
    private int extendedStipCompDaysFromToday;
    
    private boolean formMakeFindingsDefault;

    
    
    /*******************************************************
     *              Event collapse fields
     *              FROM EventsBB
    /*******************************************************/
    
    private EventCnF currentEvent;
    
    private Map<EventType, List<EventCategory>> typeCatMap;
    private boolean updateNewEventFieldsWithCatChange;
    
    private List<EventType> eventTypeCandidates;
    private EventType eventTypeSelected;
    
    private List<EventCategory> eventCategoryCandidates;
    private EventCategory eventCategorySelected;
    
    private int eventDurationFormField;
    
    private List<Human> eventHumanCandidates;
    private int eventHumanIDForLookup;
    private Human eventHumanSelected;
    
    private String formEventNoteText;
    
    
    /*******************************************************
     *              Person utilities
    /*******************************************************/
    
    private Person workingPerson;
    
    
    
  
    
    
    
    /*******************************************************
     *              BLOB Jazz
    /*******************************************************/
    
    private BlobLight currentBlob;
    private List<BlobLight> blobList;
    
    
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
    public void initBean()  {
        CaseCoordinator cc = getCaseCoordinator();
        SearchCoordinator sc = getSearchCoordinator();
        UserCoordinator uc = getUserCoordinator();
        SystemCoordinator sysCor = getSystemCoordinator();
        EventCoordinator ec = getEventCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        

        SessionBean sb = getSessionBean();
        queryList = sc.buildQueryCECaseList(getSessionBean().getSessUser().getMyCredential());
        querySelected = getSessionBean().getQueryCECase();

        configureParameters();

        try {
        caseList = new ArrayList<>();
        caseList.addAll(cc.cecase_refreshCECasePropertyUnitHeavyList(sb.getSessCECaseList()));
        CECaseDataHeavy cseTemp = getSessionBean().getSessCECase();
            
            // TODO: edie
            // deal with event page load issues later
//            if(cseTemp == null){
//                if(caseList != null && !caseList.isEmpty()){
//                    cseTemp = caseList.get(0);
//                } else {
//                    cseTemp = cc.cecase_selectDefaultCECase(sb.getSessUser());
//                }
//            }
//            currentCase = cseTemp;
            // request scoped bean: don't reload ergbob
            currentCase = cc.cecase_assembleCECaseDataHeavy(cseTemp, getSessionBean().getSessUser());
            if(currentCase.getEventList() != null && !currentCase.getEventList().isEmpty()){
                injectSessionEvent(currentCase.getEventList().get(0));
            }
           
            
            System.out.println("CECaseSearchProfileBB.initBean(): current case ID: " + currentCase.getCaseID());
            setClosingEventCategoryList(ec.getEventCategeryList(EventType.Closing));
            

        propListForSearch = sb.getSessPropertyList();
        caseStageList = CaseStageEnum.values();
        
        eventViewOptions = Arrays.asList(ViewOptionsActiveHiddenListsEnum.values());
        setEventViewOptionSelected(ViewOptionsActiveHiddenListsEnum.VIEW_ACTIVE_NOTHIDDEN);

        userManagerOptionList = uc.user_assembleUserListForSearch(getSessionBean().getSessUser());
        bobSourceOptionList = sysCor.getBobSourceListComplete();

        } catch (IntegrationException | BObStatusException | SearchException  ex) {
            System.out.println(ex);
        }
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
        
        refreshCasePropertyDataHeavy();
        
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
                if (currentCase != null && currentCase.getViolationList() != null && !currentCase.getViolationList().isEmpty()) {
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
        
        formMakeFindingsDefault = false;
        
        filteredElementList = null;
        extendStipCompUsingDate = true;
        currentCodeSet = getSessionBean().getSessMuni().getCodeSet();

        // EVENT STUFF
        setTypeCatMap(ec.assembleEventTypeCatMap_toEnact(DomainEnum.CODE_ENFORCEMENT, currentCase, getSessionBean().getSessUser()));
        eventTypeCandidates = new ArrayList<>(getTypeCatMap().keySet());
        eventCategoryCandidates = new ArrayList<>();
        if(eventTypeCandidates != null && !eventTypeCandidates.isEmpty()){
            eventTypeSelected = getEventTypeCandidates().get(0);
            eventCategoryCandidates.addAll(getTypeCatMap().get(eventTypeSelected));
        }
        eventHumanCandidates = new ArrayList<>();
        
        updateNewEventFieldsWithCatChange = true;
       
        
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
     * Listener for user requests to view a citation
     * @param cit 
     */
    public void onCitationViewButtonChange(Citation cit){
        getSessionBean().setSessCitation(cit);
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
     * @return  
     */
    public String refreshCurrentCase(){
        return "ceCaseProfile";
        
    }
    
    public void reloadCase(){
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
        reloadCase();
    }
    
     /**
     * Responds to the user clicking one of the page modes: LOOKUP, ADD, UPDATE, REMOVE
     * @deprecated no longer used during page collapse and simplification DEC/JAN '21
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
                getSessionBean().setSessProperty(currentCase.getParcelKey());
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
        getSessionBean().getNavStack().pushPage("ceCaseProfile");
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
        
         updateCaseMetatData(currentCase);
         return "ceCaseProfile";
    }
     
     /**
      * Funnel for all updateXXX methods on cases
      * The caller is responsible for updating notes to 
      * document the field changes, value by value
      * @param cse the case with updated fields
      */
     private void updateCaseMetatData(CECase cse){
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
        updateCaseMetatData(currentCase);
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
        updateCaseMetatData(currentCase);
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

    
    public String onProposalViewButtonChange(Proposal prop){
        return "workflow";
    }
    
    public String onProposalsListButtonChange(){
        return "workflow";
    }
    
    public void onEventViewButtonChange(EventCnF ev){
        if(ev != null){
            System.out.println("CECaseSearchProfileBB.onEventViewButtonChange: Event ID: " + ev.getEventID());
            getSessionBean().setSessEvent(ev);
            currentEvent = ev;
        }
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
    
    
    
    
    
    
    /*******************************************************
    /*******************************************************
     **              BLOBS                               **
    /*******************************************************/
    /*******************************************************/
    
    
    
    
    /**
     * Listener for user requests to start the blob update process
     * @param bl 
     */
  public void onBlobSelectButtonChange(BlobLight bl){
      
      currentBlob = bl;
      System.out.println("CECaseSearchProfileBB.onBlobSelectButtonChange: current blob: " + currentBlob.getPhotoDocID());
      
  }
    
    
    public String onBlobViewButtonChange(Blob blob){
        return "blobs";
        
    }

    /**
     * Listener for user requests to start a file upload
     * @param ev
     */
    public void onBlobAddButtonChange(ActionEvent ev){
        // nothing to do here yet
        System.out.println("CECaseSearchProfileBB.onBlobAddButtonChange");

    }
      /**
     * Listener for user requests to update the current blob
     * @param ev
     */
    public void onBlobUpdateMetadata(ActionEvent ev){
          BlobCoordinator bc = getBlobCoordinator();
        
        try{
            bc.updateBlobMetatdata(currentBlob, getSessionBean().getSessUser());
            reloadCaseListener(ev);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully updated blob title and description!", ""));
        } catch(IntegrationException ex){
            System.out.println("manageBlobBB.updateBlobDescription() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while trying to update the description!", ""));
        }
    }

    public String onBlobConfirm() {
        
      
        if(currentViolation.getBlobList() == null  ||  currentViolation.getBlobList().isEmpty()){
            getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                "No uploaded pdfs or photos to commit.", 
                                "Use the 'Return to case home without commiting photos' button bellow if you have no photos to upload."));
            return "";
        }
        
        BlobIntegrator bi = getBlobIntegrator();
        
        for(BlobLight photo : currentViolation.getBlobList()){
            
            try { 
                // commit and link
                
                bi.commitPhotograph(photo.getPhotoDocID());
                bi.linkBlobToViolation(photo.getPhotoDocID(), currentViolation.getViolationID());
                
            } catch (IntegrationException ex) {
                System.out.println(ex.toString());
                    getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                "INTEGRATION ERROR: Unable write request into the database, our apologies!", 
                                "Please call your municipal office and report your concern by phone."));
                    return "";
            }
        }
        
        return "ceCaseProfile";

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
     * Listener for the report initiation process
     * @return 
     */
    public String prepareReportCECaseListCustomQuery() {
        getSessionBean().setSessCECaseList(caseList);
        
//       rpt.setCaseListCustomQueryExport();
        return "ceCaseListExport";
        
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
        prepareComplianceDateForm();
    }
    
    /**
     * Listener for user requests to record compliance from the violation
     * details dialog (not the violation table, in which case the violation
     * object is accepted as a parameter
     * @param ev 
     */
    public void onViolationRecordComplianceInitButtonChange(ActionEvent ev){
        System.out.println("CeCaseSearchProfileBB.onViolationRecordComplianceInitButtonChange | from dialog");
        prepareComplianceDateForm();
    }
    
    private void prepareComplianceDateForm(){
        complianceDateForm = java.util.Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        
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
    public void onViolationSelectElementButtonChange(EnforcableCodeElement ece) {
        System.out.println("CECaseSearchProfileBB.onViolationSelectElementButtonChange: CSEID: " + ece.getCodeSetElementID());
        CaseCoordinator cc = getCaseCoordinator();
        try {
            currentViolation = cc.violation_injectOrdinance(currentCase, getCurrentViolation(), ece, null);
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
            currentViolation.setActualComplianceDateUtilDate(complianceDateForm);
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
     * @param ev
     */
    public void onViolationNotesInitButtonChange(ActionEvent ev) {
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
     * Responds to user requests to commit a new code violation to the CECase
     *
     * @return
     */
    public String onViolationAddCommitButtonChange() {

        CaseCoordinator cc = getCaseCoordinator();

        try {
            cc.violation_attachViolationToCase(currentViolation, currentCase, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Violation attached to case.", ""));
            getSessionBean().getSessionBean().setSessCodeViolation(currentViolation);
            System.out.println("ViolationBB.onViolationAddCommmitButtonChange | completed violation process");
        } catch (IntegrationException | SearchException | BObStatusException | EventException | ViolationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
            return "";
        }
        
        if(formMakeFindingsDefault){
            makeViolationFindingsDefault(currentViolation);
        }
        return "ceCaseProfile";

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
    
    
    /********************************************** /
    /***********BLOBS****************************** /
    /********************************************** /



    /**
     * Listener for user requests to upload a file and attach to case
     *
     * @param ev
     */
    public void onBlobUploadCommitButtonChange(FileUploadEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();
        if (this.blobList == null) {
            this.blobList = new ArrayList<>();
        }
        
        try {
            BlobCoordinator blobc = getBlobCoordinator();
            
            Blob blob = blobc.generateBlobSkeleton(getSessionBean().getSessUser());
            blob.setBytes(ev.getFile().getContent());
            blob.setFilename(ev.getFile().getFileName());
            blob.setMuni(getSessionBean().getSessMuni());
            Blob freshBlob = cc.blob_ceCase_attachBlob(getSessionBean().getSessUser(), blob, currentCase);
            // ship to coordinator for storage
            if(freshBlob != null){
                System.out.println("cecaseSearchProfileBB.onBlobUploadCommitButtonChange | fresh blob ID: " + freshBlob.getPhotoDocID());
            } 
            
            this.currentCase.getBlobList().add(freshBlob);
            this.getBlobList().add(blob);
        } catch (IntegrationException | IOException  | BlobException | BlobTypeException ex) {
            System.out.println("cecaseSearchProfileBB.onBlobUploadCommitButtonChange | upload failed! " + ex);
            System.out.println(ex);
        } 
    }

    /**
     * Listener for user request to remove photo on violation
     *     
     * @param bl
     */
    public void onBlobRemoveInitButtonChange(BlobLight bl) {
        currentBlob = bl;
        
    }
    
    
    /**
     * Removes link between blob and cecase
     * @param ev
     */
    public void onBlobRemoveCommitButtonChange(ActionEvent ev){
        BlobCoordinator bc = getBlobCoordinator();
        try {
            bc.removeCECaseBlobRecord(currentBlob, currentCase);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Blob link removed.", ""));
        } catch (BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Operation: Failed! Blob link not removed.", ""));
            

        }
        
        reloadCase();
        
    }
    
    
  
    
    
    
    /*******************************************************
    /*******************************************************
     **              EVENTS!!                             **
    /*******************************************************/
    /*******************************************************/
      
    public void onEventAddInitButtonChange(){
        EventCoordinator ec = getEventCoordinator();
        try {
            EventCnF ev = ec.initEvent(currentCase, null);
            getSessionBean().setSessEvent(ev);
            ev.setCeCaseID(currentCase.getBObID());
            currentEvent = ev;
        } catch (BObStatusException | EventException ex) {
            System.out.println(ex);
        } 
    }
    
      public void hideEvent(EventCnF event){
        EventIntegrator ei = getEventIntegrator();
        event.setHidden(true);
        try {
            ei.updateEvent(event);
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_INFO,
                           "Success! event ID: " + event.getEventID() + " is now hidden", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not hide event, sorry; this is a system erro", ""));
        }
    }
    
    public void unHideEvent(EventCnF event){
        EventIntegrator ei = getEventIntegrator();
        event.setHidden(false);
        try {
            ei.updateEvent(event);
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_INFO,
                           "Success! Unhid event ID: " + event.getEventID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_ERROR,
                           "Could not unhide event, sorry; this is a system erro", ""));
        }
    }
    
    
      /**
     * Actionlistener Called when the user selects their own EventCategory to add to the case
     * and is a pass-through method to the initiateNewEvent method
     *
     * @param ev
     */
    public void initiateUserChosenEventCreation(ActionEvent ev) {
        initiateNewEvent();
    }
    
    
    /**
     * Listener method for starting event edits
     * @param ev 
     */
    public void initiateEventEdit(EventCnFPropUnitCasePeriodHeavy ev){
        setCurrentEvent(ev);
    }
      /**
     * Logic container for setting up new event which will be displayed 
     * in the overlay window for the User
     */
    private void initiateNewEvent() {
        System.out.println("EventsBB.initiateNewEvent");
        EventCoordinator ec = getEventCoordinator();
        
        EventCnF ev = null;
            
        try {
            if(getCurrentEvent() == null){

                ev = ec.initEvent(currentCase, null);
//                ev.setCategory(eventCategorySelected);
                ev.setCeCaseID(currentCase.getBObID());
                ev.setUserCreator(getSessionBean().getSessUser());
                currentEvent = ec.assembleEventCnFPropUnitCasePeriodHeavy(ev);

            }
                
        } catch (BObStatusException | EventException | IntegrationException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        }
    }

    public void onEventRemoveInitButtonChange(ActionEvent ev){
        // do nothing yet
    }
    
    public String onEventRemoveCommitButtonChange(){
        EventCoordinator ec = getEventCoordinator();
        try {
            ec.removeEvent(getCurrentEvent(), getSessionBean().getSessUser());
             getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Removed event ID " + getCurrentEvent().getEventID(), ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                ex.getMessage(), ""));
             return "";
        }
        
        return "ceCaseProfile";
        
    }

    
    public String onEventReactivateCommitButtonChange(){
        EventCoordinator ec = getEventCoordinator();
        try {
            currentEvent.setActive(true);
            ec.updateEvent(currentEvent, getSessionBean().getSessUser());
             getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Reactivated event ID " + getCurrentEvent().getEventID(), ""));
        } catch (IntegrationException | BObStatusException | EventException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                ex.getMessage(), ""));
             return "";
        }
        
        return "ceCaseProfile";
        
    }
    
    /**
     * All Code Enforcement case events are funneled through this method which
     * has to carry out a number of checks based on the type of event being
     * created. The event is then passed to the addEvent_processForCECaseDomain on the
     * Event Coordinator who will do some more checking about the event before
     * writing it to the DB
     *
     * @return 
     */
    public String onEventAddCommitButtonChange() {
        System.out.println("EventsBB.onEventAddCommitButtonChange");
        EventCoordinator ec = getEventCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        
        List<EventCnFPropUnitCasePeriodHeavy> evDoneList;
            
        // category is already set from initialization sequence

        try {
            getCurrentEvent().setCategory(getEventCategorySelected());
            getCurrentEvent().setDomain(DomainEnum.CODE_ENFORCEMENT);
            if(getCurrentEvent().getDomain() == null && getCurrentEvent().getDomain() == DomainEnum.UNIVERSAL){
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Event must have a domain that's not universal", ""));
                return "";
            }
            if(getEventCategorySelected() == null){
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Event must have a category ", ""));
                return "";
            } else {
                List<EventCnF> evSimpleList = ec.addEvent(getCurrentEvent(), currentCase, getSessionBean().getSessUser());
                evDoneList = ec.assembleEventCnFPropUnitCasePeriodHeavyList(evSimpleList);

                if(evDoneList != null && !evDoneList.isEmpty()){
                    for(EventCnF evt: evDoneList){

                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO,
                                        "Successfully logged event with an ID " + evt.getEventID() + " ", ""));
                    }
                    setCurrentEvent(evDoneList.get(0));
                    sc.logObjectView(getSessionBean().getSessUser(), getCurrentEvent());
                }
                getSessionBean().setSessEvent(getCurrentEvent());
                
                return "ceCaseProfile";
            }
    
        } catch (IntegrationException | BObStatusException | EventException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(),
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
            return "";
        } 

    }
    
      /**
     * Listener for user requests to update an event
     * @return 
     */
    public String onEventUpdateCommitButtonChange(){
        EventCoordinator ec = getEventCoordinator();
        try {
            ec.updateEvent(getCurrentEvent(), getSessionBean().getSessUser());
        } catch (IntegrationException | BObStatusException | EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(),
                            ""));
            return "";
        } 
        
        return "ceCaseProfile";
    }
    
    public void onPersonListCommitButtonChange(ActionEvent ev){
        
        // nothing to do on the back end
        
    }
    
   
    /**
     * Listener for user request to go and view a person in personProfile
     * @param p
     * @return 
     */
    public String onPersonViewButtonChange(Person p){
        
        getSessionBean().setSessPersonQueued(p);
        getSessionBean().setOnPageLoad_sessionSwitch_viewProfile(true);
        return "personSearch";
        
    }
    
    /**
     * Listener for user changes to the selected event type
     */
    public void onEventTypeMenuChange(){
        System.out.println("EventsBB.onEventTypeMenuChange");
        refreshAvailableEventCategories();
        
    }
  
    /**
     * Listener method for changes in EventType selected by User
     */
    public void refreshAvailableEventCategories(){
        
        if(getEventTypeSelected() != null){
            getEventCategoryCandidates().clear();
            getEventCategoryCandidates().addAll(getTypeCatMap().get(getEventTypeSelected()));
            if(!eventCategoryCandidates.isEmpty()){
                setEventCategorySelected(getEventCategoryCandidates().get(0));
            }
            System.out.println("EventsBB.refreshavailableEventCategories");
        }
    }
    /**
     * Listener for user changes to the event category list on event add
     */
    public void onEventCategoryMenuChange(){
        
        configureEventFieldsOnAddConfig();
    }
   
    /**
     * Sets current event field values to those suggested by the 
     * selected event category
     */
    private void configureEventFieldsOnAddConfig(){
        if(eventCategorySelected != null 
                && currentEvent != null 
                && updateNewEventFieldsWithCatChange){
            currentEvent.setTimeStart(LocalDateTime.now());
            setEventDurationFormField(getEventCategorySelected().getDefaultDurationMins());
            currentEvent.setTimeEnd(getCurrentEvent().getTimeStart().plusMinutes(getEventCategorySelected().getDefaultDurationMins()));
            currentEvent.setDescription(getEventCategorySelected().getHostEventDescriptionSuggestedText());
        }
    }
    
    /**
     * Listener for ajax updates to event time start
     * @param ev
     */
    public void onTimeStartChange(){
        System.out.println("CECaseSearchProfileBB.onTimeStartChange: " );
        if(currentEvent.getTimeStart() != null){
            currentEvent.setTimeEnd(getCurrentEvent().getTimeStart().plusMinutes(getEventDurationFormField()));
            
        }
    }
    
    /**
     * Listener for AJAX updates to event duration
     */
    public void onEventDurationChange(){
        System.out.println("CECaseSearchProfileBB.onEventDurtaionChange");
        if(getCurrentEvent().getTimeStart() != null){
            getCurrentEvent().setTimeEnd(getCurrentEvent().getTimeStart().plusMinutes(getEventDurationFormField()));
        }
    }
    

    /**
     * Listener pass through method for finalizing event edits
     * @param ev 
     */
    public void finalizeEventUpdateListener(ActionEvent ev){
        finalizeEventUpdate();
    }
    
    /**
     * Event update processing
     */
    public void finalizeEventUpdate() {
        EventCoordinator ec = getEventCoordinator();
        try {
            ec.updateEvent(getCurrentEvent(), getSessionBean().getSessUser());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Event udpated!", ""));
        } catch (IntegrationException | EventException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(),
                    ""));
        } 

    }

    /**
     * Listener method for adding the selected person to a queue
     * @param ev 
     */
    public void queueSelectedPerson(ActionEvent ev) {
        PersonCoordinator pc = getPersonCoordinator();
        if (eventHumanSelected != null) {
            getCurrentEvent().getPersonList().add(pc.createHumanLinkSkeleton(eventHumanSelected));
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Please select one or more people to attach to this event",
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
        }
    }

    public void deQueuePersonFromEvent(Person p) {
        if (getCurrentEvent().getPersonList() != null) {
            getCurrentEvent().getPersonList().remove(p);
        }
    }

   
      /**
     * Listener for commencement of note writing process
     *
     * @param ev
     */
    public void onEventNoteInitButtonChange(ActionEvent ev) {
        formEventNoteText = "";

    }

    /**
     * Listener for user requests to commit new note content to the current
     * object
     *
     * @return
     */
    public String onNoteCommitButtonChange() {
        EventCoordinator ec = getEventCoordinator();

        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setCred(getSessionBean().getSessUser().getKeyCard());
        mbp.setExistingContent(getCurrentEvent().getNotes());
        mbp.setNewMessageContent(getFormNoteText());
        mbp.setHeader("Notice of Violation Note");
        mbp.setUser(getSessionBean().getSessUser());

        try {

            ec.updateEventNotes(mbp, getCurrentEvent(), getSessionBean().getSessUser());
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

    
    
   
    
    /*******************************************************
    /*******************************************************
     **              Person management on cases           **
    /*******************************************************/
    /*******************************************************/
    
    /**
     * Listener for beginning of person add process
     * @param ev 
     */
    public void personCreateInit(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        workingPerson = pc.createPersonSkeleton(getSessionBean().getSessUser().getMyCredential().getGoverningAuthPeriod().getMuni());
    }

    /**
     * Action listener for creation of new person objectgs
     * @param ev
     */
    public void personCreateCommit(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
    
        try {
           Person per = pc.humanAdd(workingPerson, getSessionBean().getSessUser());
            PersonDataHeavy freshPerson = pc.assemblePersonDataHeavy(per,getSessionBean().getSessUser().getKeyCard());
            getSessionBean().setSessPerson(freshPerson);
            HumanLink hl = new HumanLink(freshPerson);
        
            Property property = currentCase.getProperty();
            
            pc.linkHuman(currentCase, hl, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_INFO, 
                     "Successfully added " + freshPerson.getFirstName() + " to the Database!" 
                         + " and connected to " + property.getAddress(), ""));
           } catch (IntegrationException | BObStatusException ex) {
               System.out.println(ex.toString());
                  getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                               "Unable to add new person to the database, my apologies!", ""));
        }
        //make sure the person list includes our fresh person
        refreshCasePropertyDataHeavy();
        
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

    /**
     * @return the complianceDateForm
     */
    public java.util.Date getComplianceDateForm() {
        return complianceDateForm;
    }

    /**
     * @param complianceDateForm the complianceDateForm to set
     */
    public void setComplianceDateForm(java.util.Date complianceDateForm) {
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
     * @return the currentEvent
     */
    public EventCnF getCurrentEvent() {
        return currentEvent;
    }

    /**
     * @return the eventTypeCandidates
     */
    public List<EventType> getEventTypeCandidates() {
        return eventTypeCandidates;
    }

    /**
     * @return the eventTypeSelected
     */
    public EventType getEventTypeSelected() {
        return eventTypeSelected;
    }

    /**
     * @return the eventCategoryCandidates
     */
    public List<EventCategory> getEventCategoryCandidates() {
        return eventCategoryCandidates;
    }

    /**
     * @return the eventCategorySelected
     */
    public EventCategory getEventCategorySelected() {
        return eventCategorySelected;
    }

    /**
     * @return the eventDurationFormField
     */
    public int getEventDurationFormField() {
        return eventDurationFormField;
    }

    /**
     * @return the eventPersonCandidates
     */
    public List<Human> getEventPersonCandidates() {
        return eventHumanCandidates;
    }

    /**
     * @return the eventHumanIDForLookup
     */
    public int getEventHumanIDForLookup() {
        return eventHumanIDForLookup;
    }

    /**
     * @return the eventPersonSelected
     */
    public Human getEventPersonSelected() {
        return eventHumanSelected;
    }

    /**
     * @return the formEventNoteText
     */
    public String getFormEventNoteText() {
        return formEventNoteText;
    }

    /**
     * @param currentEvent the currentEvent to set
     */
    public void setCurrentEvent(EventCnF currentEvent) {
        this.currentEvent = currentEvent;
    }

    /**
     * @param eventTypeCandidates the eventTypeCandidates to set
     */
    public void setEventTypeCandidates(List<EventType> eventTypeCandidates) {
        this.eventTypeCandidates = eventTypeCandidates;
    }

    /**
     * @param eventTypeSelected the eventTypeSelected to set
     */
    public void setEventTypeSelected(EventType eventTypeSelected) {
        this.eventTypeSelected = eventTypeSelected;
    }

    /**
     * @param eventCategoryCandidates the eventCategoryCandidates to set
     */
    public void setEventCategoryCandidates(List<EventCategory> eventCategoryCandidates) {
        this.eventCategoryCandidates = eventCategoryCandidates;
    }

    /**
     * @param eventCategorySelected the eventCategorySelected to set
     */
    public void setEventCategorySelected(EventCategory eventCategorySelected) {
        this.eventCategorySelected = eventCategorySelected;
    }

    /**
     * @param eventDurationFormField the eventDurationFormField to set
     */
    public void setEventDurationFormField(int eventDurationFormField) {
        this.eventDurationFormField = eventDurationFormField;
    }

    /**
     * @param eventPersonCandidates the eventPersonCandidates to set
     */
    public void setEventPersonCandidates(List<Human> eventPersonCandidates) {
        this.eventHumanCandidates = eventPersonCandidates;
    }

    /**
     * @param eventHumanIDForLookup the eventHumanIDForLookup to set
     */
    public void setEventHumanIDForLookup(int eventHumanIDForLookup) {
        this.eventHumanIDForLookup = eventHumanIDForLookup;
    }

    /**
     * @param eventPersonSelected the eventPersonSelected to set
     */
    public void setEventPersonSelected(Person eventPersonSelected) {
        this.eventHumanSelected = eventPersonSelected;
    }

    /**
     * @param formEventNoteText the formEventNoteText to set
     */
    public void setFormEventNoteText(String formEventNoteText) {
        this.formEventNoteText = formEventNoteText;
    }

    /**
     * @return the closingEventCategoryList
     */
    public List<EventCategory> getClosingEventCategoryList() {
        return closingEventCategoryList;
    }

    /**
     * @return the typeCatMap
     */
    public Map<EventType, List<EventCategory>> getTypeCatMap() {
        return typeCatMap;
    }

    /**
     * @return the updateNewEventFieldsWithCatChange
     */
    public boolean isUpdateNewEventFieldsWithCatChange() {
        return updateNewEventFieldsWithCatChange;
    }

    /**
     * @param closingEventCategoryList the closingEventCategoryList to set
     */
    public void setClosingEventCategoryList(List<EventCategory> closingEventCategoryList) {
        this.closingEventCategoryList = closingEventCategoryList;
    }

    /**
     * @param typeCatMap the typeCatMap to set
     */
    public void setTypeCatMap(Map<EventType, List<EventCategory>> typeCatMap) {
        this.typeCatMap = typeCatMap;
    }

    /**
     * @param updateNewEventFieldsWithCatChange the updateNewEventFieldsWithCatChange to set
     */
    public void setUpdateNewEventFieldsWithCatChange(boolean updateNewEventFieldsWithCatChange) {
        this.updateNewEventFieldsWithCatChange = updateNewEventFieldsWithCatChange;
    }

    /**
     * @return the workingPerson
     */
    public Person getWorkingPerson() {
        return workingPerson;
    }

    /**
     * @param workingPerson the workingPerson to set
     */
    public void setWorkingPerson(Person workingPerson) {
        this.workingPerson = workingPerson;
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

   

}
