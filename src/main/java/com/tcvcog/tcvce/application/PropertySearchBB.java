/*
 * Copyright (C) 2020 Technology Rediscovery LLC
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
import com.tcvcog.tcvce.coordinators.WorkflowCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.BlobTypeException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventRuleAbstract;
import com.tcvcog.tcvce.entities.IntensityClass;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.PageModeEnum;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.PropertyExtData;
import com.tcvcog.tcvce.entities.PropertyUseType;
import com.tcvcog.tcvce.entities.search.QueryProperty;
import com.tcvcog.tcvce.entities.search.QueryPropertyEnum;
import com.tcvcog.tcvce.entities.search.SearchParamsProperty;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsProposalsEnum;
import java.io.IOException;
import java.time.LocalDateTime;
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
 * Primary backing bean for the Property Search and profile master
 * collapsed page
 * @author sylvia
 */
public class PropertySearchBB extends BackingBeanUtils{
    
    
    private PropertyDataHeavy currentProperty;
    private Property freshProperty;
    private boolean currentPropertySelected;
    
    private List<Property> propListMaster;
    private List<Property> propListDisplayed;
    private boolean appendResultsToList;
    
    private SearchParamsProperty searchParamsSelected;
    private List<SearchParamsProperty> searchParamsCustomized;
    
    private QueryProperty querySelected;
    private List<QueryProperty> queryList;
    
    private List<PropertyUseType> putList;
    
//    Migration from PropertyProfileBB
    
     private Municipality muniSelected;
    
    private ViewOptionsActiveHiddenListsEnum eventViewOptionSelected;
    private List<ViewOptionsActiveHiddenListsEnum> eventViewOptions;

    private PropertyUseType selectedPropertyUseType;

    private List<IntensityClass> conditionIntensityList;
    private List<IntensityClass> landBankProspectIntensityList;
    private List<BOBSource> sourceList;

    private List<PropertyExtData> propExtDataListFiltered;

    private String formNoteText;

    private Person personSelected;
    private List<Person> personToAddList;
    private boolean personLinkUseID;
    private int personIDToLink;

    private ViewOptionsProposalsEnum selectedPropViewOption;
    
    
    // BLOBS
    
    private BlobLight currentBlob;
    
    
    /**
     * Creates a new instance of SearchBB
     */
    public PropertySearchBB() {
    }
    
    @PostConstruct
    public void initBean(){
        SearchCoordinator sc = getSearchCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        PropertyIntegrator pi = getPropertyIntegrator();
        
        propListMaster = new ArrayList<>();
        propListDisplayed = new ArrayList<>();
        if(getSessionBean().getSessPropertyList() != null && !getSessionBean().getSessPropertyList().isEmpty()){
            propListMaster.addAll(getSessionBean().getSessPropertyList());
            propListDisplayed.addAll(propListMaster);
        }
        appendResultsToList = false;
        
        
        try {
            // build a fresh copy of our session's property
            currentProperty = pc.assemblePropertyDataHeavy(getSessionBean().getSessProperty(),getSessionBean().getSessUser());
            // the list of avail queries is built by the SessionInitializer
            // and put on the SessionBean for us to get here
            queryList = getSessionBean().getQueryPropertyList();
            putList = pi.getPropertyUseTypeList();
        } catch (IntegrationException | BObStatusException | SearchException ex) {
            System.out.println(ex);
        }
    
//        querySelected = getSessionBean().getQueryProperty();
        
        if(querySelected == null && !queryList.isEmpty()){
            querySelected = queryList.get(0);
        }
        
        personToAddList = new ArrayList<>();
        eventViewOptions = Arrays.asList(ViewOptionsActiveHiddenListsEnum.values());
        eventViewOptionSelected = ViewOptionsActiveHiddenListsEnum.VIEW_ACTIVE_NOTHIDDEN;

        // setup search
        configureParameters();
        
    }
   
    
    /**
     * Sets up search parameters for properties
     */
    private void configureParameters(){
        SystemCoordinator sc = getSystemCoordinator();
        if(querySelected != null 
                && 
            querySelected.getParamsList() != null 
                && 
            !querySelected.getParamsList().isEmpty()){
            
            searchParamsSelected = querySelected.getParamsList().get(0);
        }
        
         setMuniSelected(getSessionBean().getSessMuni());
           setSourceList(sc.getBobSourceListComplete());
           
        try {
            setConditionIntensityList(sc.getIntensitySchemaWithClasses(
                    getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("intensityschema_propertycondition"))
                    .getClassList());
            setLandBankProspectIntensityList(sc.getIntensitySchemaWithClasses(
                    getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("intensityschema_landbankprospect"))
                    .getClassList());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    
    public void clearPropertyList(ActionEvent ev){
        propListMaster.clear();
    }
    
    
       
    /**
     * Demo of using the Search system to extract objects from the DB
     * in a controlled way
     * @return 
     */
    private List<Property> testPropertySearch(){
        SearchCoordinator sc = getSearchCoordinator();
        QueryProperty qp = sc.initQuery(QueryPropertyEnum.HOUSESTREETNUM, getSessionBean().getSessUser().getMyCredential());
        if(qp != null && !qp.getParamsList().isEmpty()){
            SearchParamsProperty spp = qp.getParamsList().get(0);
            spp.setAddress_ctl(true);
            spp.setAddress_val("101 Main");
            spp.setMuni_ctl(true);
            spp.setMuni_val(getSessionBean().getSessMuni());
            spp.setLimitResultCount_ctl(true);
            spp.setLimitResultCount_val(20);
        }
        
        try {
            // send assembled query with its configured parameter object
            // to runQuery, which will delegate the work to the searchForXXX methods
            // on the intergrators
            sc.runQuery(qp);
        } catch (SearchException ex) {
            System.out.println(ex);
        }
        List<Property> propList = null;

        if(qp != null && !qp.getBOBResultList().isEmpty()){
            // extract the actual business objects from the Query object as a list
            propList = qp.getBOBResultList();
        }
        
        return propList;
        
    }
    
    /**
     * Action listener for the user's request to run the query
     * @param event 
     */
    public void executeQuery(ActionEvent event){
        System.out.println("PropertySearchBB.executeQuery | querySelected: " + querySelected.getQueryTitle());
        
        SearchCoordinator sc = getSearchCoordinator();
        List<Property> pl;
        
        try {
            
            pl = sc.runQuery(querySelected).getBOBResultList();
            if(!appendResultsToList){
                propListMaster.clear();
            } 
            propListMaster.addAll(pl);
            if(!propListMaster.isEmpty()){
                getSessionBean().setSessPropertyList(propListMaster);
            }
            
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Your search completed with " + pl.size() + " results", ""));
            
        } catch (SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to complete search! ", ""));
        }
    }
    
    /**
     * Listener method for changes in the selected query;
     * Updates search params and UI updates based on this changed value
     */
    public void changeQuerySelected(){
        System.out.println("PropertySearchBB.changeQuerySelected | querySelected: " + querySelected.getQueryTitle());
        configureParameters();
        
    }
    
    
    
    /**
     * Event listener for resetting a query after it's run
     * @param event 
     */
    public void resetQuery(ActionEvent event){
        SearchCoordinator sc = getSearchCoordinator();
        try {
            //        querySelected = sc.initQuery(querySelected.getQueryName(), getSessionBean().getSessUser().getMyCredential());
            queryList = sc.buildQueryPropertyList(getSessionBean().getSessUser().getMyCredential());
            if(queryList != null && !queryList.isEmpty()){
                querySelected = queryList.get(0);
            }
            if(appendResultsToList == false){
                if(propListMaster != null && !propListMaster.isEmpty()){
                    propListMaster.clear();
                }
            }
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Query reset ", ""));
            
        } catch (IntegrationException ex) {
             System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to reset search due to error in search coordinator! ", ""));
        }
        configureParameters();
    }
    
    
    
    /**
     * Loads a data-heavy subclass of the selected property
     * @param prop 
     * @return  
     */
    public String exploreProperty(Property prop){
        PropertyCoordinator pc = getPropertyCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        
        try {
            getSessionBean().setSessProperty(pc.assemblePropertyDataHeavy(prop, getSessionBean().getSessUser()));
            getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO,
                                        "Managing property at " + prop.getAddress() , ""));
            sc.logObjectView(getSessionBean().getSessUser(), prop);
            return "propertyInfo";
        } catch (IntegrationException | BObStatusException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        ex.getMessage(), ""));
        } 
        return "";
        
    }
    
    /**
     * ********************************************************
     * ************* PROPERTY PROFILE/INFO METHODS ************
     * ********************************************************
     */
    
    /**
     * Utilty method for refreshing property
     */
    public void reloadCurrentPropertyDataHeavy(){
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            setCurrentProperty(pc.assemblePropertyDataHeavy(currentProperty, getSessionBean().getSessUser()));
             getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Reloaded property at " + currentProperty.getAddress(), ""));
        } catch (IntegrationException | BObStatusException | SearchException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Fatal error reloading property; apologies!", ""));
            
        }
        
    }

    /**
     * Listener for when the user aborts a property add operation;
     *
     * @param ev
     */
    public void onDiscardNewPropertyDataButtonChange(ActionEvent ev) {

    }

    /**
     * Listener for user requests to create a note event on this property
     *
     * @return redirection to the EventAdd page
     */
    public String onAddNoteEventButtonChange() {
        EventCoordinator ec = getEventCoordinator();

        try {
            EventCnF ev = ec.initEvent(currentProperty.getPropInfoCaseList().get(0),
                    ec.getEventCategory(Integer.parseInt(
                            getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                                    .getString("propertyinfoeventcatid"))));
            getSessionBean().setSessEvent(ev);
        } catch (IntegrationException | BObStatusException | EventException ex) {
            System.out.println(ex);
        }

        return "eventAdd";

    }
    
    
     /**
     * Listener for commencement of note writing process
     *
     * @param ev
     */
    public void onNoteInitButtonChange(ActionEvent ev) {
        setFormNoteText(new String());

    }

    /**
     * Listener for user requests to commit new note content to the current
     * Property
     *
     * @param ev
     */
    public void onNoteCommitButtonChange(ActionEvent ev) {
        SystemCoordinator sc = getSystemCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setCred(getSessionBean().getSessUser().getKeyCard());
        mbp.setExistingContent(currentProperty.getNotes());
        mbp.setNewMessageContent(getFormNoteText());
        mbp.setHeader("Property Note");
        mbp.setUser(getSessionBean().getSessUser());
        currentProperty.setNotes(sc.appendNoteBlock(mbp));
        try {
            currentProperty.setLastUpdatedTS(LocalDateTime.now());
            pc.editProperty(currentProperty, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succesfully appended note!", ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fatal error appending note; apologies!", ""));

        }

        reloadCurrentPropertyDataHeavy();

    }

    /**
     * Listener for user requests to explore the property info cases on this
     * property
     *
     * @return
     */
    public String onInfoCaseListButtonChange() {
        CaseCoordinator cc = getCaseCoordinator();

        if (currentProperty != null && currentProperty.getPropInfoCaseList() != null) {
            getSessionBean().setSessCECaseListWithDowncastAndLookup(currentProperty.getPropInfoCaseList());
        }

        return "ceCaseSearch";
    }

   
    /**
     * Listener for the user's commencement of the person link process
     *
     * @param ev
     */
    public void onPersonConnectInitButtonChange(ActionEvent ev) {

    }

    /**
     * Listener for the user signaling their desire to connect a person
     *
     * @return
     */
    public String onPersonConnectCommitButtonChange() {
        PropertyCoordinator pc = getPropertyCoordinator();
        PersonCoordinator persc = getPersonCoordinator();
        try {
            // based on the user's boolean button choice, either 
            // look up a person by ID or use the object
            if (isPersonLinkUseID() &&.getHumanID()ToLink() != 0) {
                Person checkPer = null;
                checkPer = persc.getPerson.getHumanID()ToLink());
                if (checkPer != null && checkPer.getHumanID() != 0) {
                    pc.connectPersonToProperty(currentProperty, checkPer);
                    getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Connected " + checkPer.getLastName() + " to property ID " + currentProperty.getParcelkey(), ""));
                } else {
                    getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Could not find a Person with ID " +.getHumanID()ToLink(), ""));
                    
                }

            } else {
                if (getPersonSelected() != null) {
                    pc.connectPersonToProperty(currentProperty, getPersonSelected());
                    getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Connected " + getPersonSelected().getLastName() + " to property ID " + currentProperty.getParcelkey(), ""));
                } else {
                    
                    getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Could not complete link to person, sorry!", ""));
                }
            }
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            
        }

        return "propertySearch";
    }
    
    /**
     * Listener for user requests to remove a link between property and person
     * @param p
     * @return 
     */
    public String onPersonConnectRemoveButtonChange(Person p){
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            pc.connectRemovePersonToProperty(currentProperty, p, getSessionBean().getSessUser());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Removed property-person link and created documentation note.", ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Could not remove link from property to person, sorry!", ""));
        }
        
        
        return "propertySearch";
    }

    

    /**
     * Listener for user requests to remove the currently selected ERA;
     * Delegates all work to internal, non-listener method.
     *
     * @return Empty string which prompts page reload without wiping bean memory
     */
    public String onRemoveButtonChange() {
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully Remove Municipality", ""));
        return "";
    }

    public String onPropUnitExploreButtonChange() {

        getSessionBean().setSessProperty(currentProperty);
        return "propertyUnits";
    }

    /**
     * Listener for user requests to view advanced search dialog
     *
     * @param ev
     */
    public void onAdvancedSearchButtonChange(ActionEvent ev) {

    }

   
    /**
     * Listener for user requests to start the update process
     */
    public void onPropertyUpdateInit() {
        // nothing to do here yet
    }
    /**
     * Loads a skeleton property into which we inject values from the form
     */
    public void onPropertyAddInit() {
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            setCurrentProperty(pc.assemblePropertyDataHeavy(pc.generatePropertySkeleton(getSessionBean().getSessMuni()),getSessionBean().getSessUser()));
        } catch (IntegrationException | BObStatusException | SearchException ex) {
            System.out.println(ex);
        }
    }

    /**
     * Liases with coordinator to insert a new property object
     * @return 
     */
    public String onPropertyAddCommit() {
        PropertyCoordinator pc = getPropertyCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        int newID;
        try {
            newID = pc.addProperty(currentProperty, getSessionBean().getSessUser());
            getSessionBean().setSessProperty(pc.getPropertyDataHeavy(newID, getSessionBean().getSessUser()));
            sc.logObjectView(getSessionBean().getSessUser(), currentProperty);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully added property with ID: " + currentProperty.getParcelkey()
                            + ", which is now your 'active property'", ""));
        } catch (AuthorizationException | BObStatusException | EventException | IntegrationException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update property, sorries!" + ex.getClass().toString(), ""));
        }
        return "propertySearch";
        

    }

    /**
     * Listener for user requests to commit property updates
     * @return 
     */
    public String onPropertyUpdateCommit() {
        PropertyCoordinator pc = getPropertyCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        try {
//            currentProperty.setAbandonedDateStart(pc.configureDateTime(currentProperty.getAbandonedDateStart().to));
            pc.editProperty(currentProperty, getSessionBean().getSessUser());
            getSessionBean().setSessProperty(currentProperty);
            sc.logObjectView(getSessionBean().getSessUser(), currentProperty);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully updated property with ID " + getCurrentProperty().getParcelkey()
                            + ", which is now your 'active property'", ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update property, sorries! " + ex.toString(), ""));
        }
        return "propertySearch";
        

    }

    /**
     * Listener for requests from the user to view a Person's profile
     *
     * @param p
     * @return
     */
    public String onViewPersonProfileButtonChange(Person p) {
        PersonCoordinator pc = getPersonCoordinator();
        if (p != null) {
            getSessionBean().getSessPersonList().add(0, p);
            try {
                getSessionBean().setSessPerson(pc.assemblePersonDataHeavy(p, getSessionBean().getSessUser().getKeyCard()));
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
            return "personInfo";

        }
        return "";

    }

    public String onCreateNewCaseButtonChange() {
        getSessionBean().setSessProperty(currentProperty);
        getSessionBean().getNavStack().pushPage("ceCaseWorkflow");
        return "addNewCase";

    }

    /**
     * Listener for requests to view an event
     *
     * @param ev
     * @return
     */
    public String onViewEventButtonChange(EventCnF ev) {
        if (ev != null) {
            getSessionBean().setSessEvent(ev);
            return "eventAddEdit";
        }
        return "";

    }

    /**
     * Listener for requests to view a CECase
     *
     * @param cse
     * @return
     */
    public String onViewCaseButtonChange(CECase cse) {
        CaseCoordinator cc = getCaseCoordinator();
        if (cse != null) {
            System.out.println("PropertyProfile.onViewCaseButtonChange: setting in session case ID " + cse.getCaseID());

            getSessionBean().setSessCECase(cse);
        }
        return "ceCaseProfile";
    }

    /**
     * Listener for requests to remove a potential new person link to the
     * current Property
     *
     * @param p
     */
    public void deQueuePersonFromEvent(Person p) {
        // TODO Finish my guts
    }
    
    // ********************************************************
    // *********************BLOBS******************************
    // ********************************************************
    
    
    /**
     * Listener for user requests to upload a file and attach to case
     *
     * @param ev
     */
    public void onBlobUploadCommitButtonChange(FileUploadEvent ev) {
        PropertyCoordinator pc = getPropertyCoordinator();
        
        try {
            BlobCoordinator blobc = getBlobCoordinator();
            
            Blob blob = blobc.generateBlobSkeleton(getSessionBean().getSessUser());
            blob.setBytes(ev.getFile().getContent());
            blob.setFilename(ev.getFile().getFileName());
            blob.setMuni(getSessionBean().getSessMuni());
            Blob freshBlob = pc.blob_property_storeAndAttachBlob(getSessionBean().getSessUser(), blob, currentProperty);
            // ship to coordinator for storage
            if(freshBlob != null){
                System.out.println("cecaseSearchProfileBB.onBlobUploadCommitButtonChange | fresh blob ID: " + freshBlob.getPhotoDocID());

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Successfully stored photo/doc with ID " + freshBlob.getPhotoDocID(), ""));

            } 
            reloadCurrentPropertyDataHeavy();
        } catch (IntegrationException | IOException | NoSuchElementException | BlobException | BlobTypeException | BObStatusException ex) {

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to save photo or doc due to a system error, sorry!", ""));
            System.out.println("cecaseSearchProfileBB.onBlobUploadCommitButtonChange | upload failed! " + ex);
            System.out.println(ex);
        } 
    }

     
    /**
     * Listener for user requests to start the blob update process
     * @param bl 
     */
  public void onBlobSelectButtonChange(BlobLight bl){
      
        setCurrentBlob(bl);
      System.out.println("CECaseSearchProfileBB.onBlobSelectButtonChange: current blob: " + getCurrentBlob().getPhotoDocID());
      
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
        System.out.println("PropertySearchBB.onBlobAddButtonChange");

    }
      /**
     * Listener for user requests to update the current blob
     * @param ev
     */
    public void onBlobUpdateMetadata(ActionEvent ev){
          BlobCoordinator bc = getBlobCoordinator();
        
        try{
            bc.updateBlobMetatdata(getCurrentBlob(), getSessionBean().getSessUser());
            reloadCurrentPropertyDataHeavy();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully updated blob title and description!", ""));
        } catch(IntegrationException ex){
            System.out.println("propertySearchProfile.updateBlobDescription() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while trying to update the description!", ""));
        }
    }
    
    
    
    /**
     * Listener for user requests to remove a blob property link
     * @param bl 
     */
    public void onBlobRemoveInitButtonChange(BlobLight bl){
        currentBlob = bl;
        
    }
    
    
    /**
     * Hands off link removal to coordinator
     * @param ev 
     */
    public void onBlobRemoveCommitButtonChange(ActionEvent ev){
        BlobCoordinator bc = getBlobCoordinator();
        
        try {
            bc.removePropBlobRecord(currentBlob, currentProperty);
            
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully removed link between photo and property", ""));
        } catch (BObStatusException ex) {
            System.out.println("manageBlobBB.removePropPhotoLink | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while trying to remove the link!", ""));
        }
        reloadCurrentPropertyDataHeavy();
        
    }

    

    
    
    
    

    /**
     * @return the searchParamsSelected
     */
    public SearchParamsProperty getSearchParamsSelected() {
        return searchParamsSelected;
    }

    /**
     * @return the querySelected
     */
    public QueryProperty getQuerySelected() {
        return querySelected;
    }

    /**
     * @return the queryList
     */
    public List<QueryProperty> getQueryList() {
        return queryList;
    }

    /**
     * @param searchParamsSelected the searchParamsSelected to set
     */
    public void setSearchParamsSelected(SearchParamsProperty searchParamsSelected) {
        this.searchParamsSelected = searchParamsSelected;
    }

    /**
     * @param querySelected the querySelected to set
     */
    public void setQuerySelected(QueryProperty querySelected) {
        this.querySelected = querySelected;
    }

    /**
     * @param queryList the queryList to set
     */
    public void setQueryList(List<QueryProperty> queryList) {
        this.queryList = queryList;
    }

    /**
     * @return the searchParamsCustomized
     */
    public List<SearchParamsProperty> getSearchParamsCustomized() {
        return searchParamsCustomized;
    }

    /**
     * @param searchParamsCustomized the searchParamsCustomized to set
     */
    public void setSearchParamsCustomized(List<SearchParamsProperty> searchParamsCustomized) {
        this.searchParamsCustomized = searchParamsCustomized;
    }

    /**
     * @return the propListMaster
     */
    public List<Property> getPropListMaster() {
        return propListMaster;
    }

    /**
     * @param propListMaster the propListMaster to set
     */
    public void setPropListMaster(List<Property> propListMaster) {
        this.propListMaster = propListMaster;
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
     * @return the putList
     */
    public List<PropertyUseType> getPutList() {
        return putList;
    }

    /**
     * @param putList the putList to set
     */
    public void setPutList(List<PropertyUseType> putList) {
        this.putList = putList;
    }

  
    /**
     * @return the currentProperty
     */
    public Property getCurrentProperty() {
        return currentProperty;
    }

    /**
     * @return the currentPropertySelected
     */
    public boolean isCurrentPropertySelected() {
        currentPropertySelected = currentProperty != null;
        return currentPropertySelected;
    }

    /**
     * @return the propListDisplayed
     */
    public List<Property> getPropListDisplayed() {
        return propListDisplayed;
    }

  
    /**
     * @param currentProperty the currentProperty to set
     */
    public void setCurrentProperty(PropertyDataHeavy currentProperty) {
        this.currentProperty = currentProperty;
    }

    /**
     * @param currentPropertySelected the currentPropertySelected to set
     */
    public void setCurrentPropertySelected(boolean currentPropertySelected) {
        this.currentPropertySelected = currentPropertySelected;
    }

    /**
     * @param propListDisplayed the propListDisplayed to set
     */
    public void setPropListDisplayed(List<Property> propListDisplayed) {
        this.propListDisplayed = propListDisplayed;
    }

    /**
     * @return the freshProperty
     */
    public Property getFreshProperty() {
        return freshProperty;
    }

    /**
     * @param freshProperty the freshProperty to set
     */
    public void setFreshProperty(Property freshProperty) {
        this.freshProperty = freshProperty;
    }

    /**
     * @return the muniSelected
     */
    public Municipality getMuniSelected() {
        return muniSelected;
    }

    /**
     * @param muniSelected the muniSelected to set
     */
    public void setMuniSelected(Municipality muniSelected) {
        this.muniSelected = muniSelected;
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
     * @return the eventViewOptions
     */
    public List<ViewOptionsActiveHiddenListsEnum> getEventViewOptions() {
        return eventViewOptions;
    }

    /**
     * @return the selectedPropertyUseType
     */
    public PropertyUseType getSelectedPropertyUseType() {
        return selectedPropertyUseType;
    }

    /**
     * @return the conditionIntensityList
     */
    public List<IntensityClass> getConditionIntensityList() {
        return conditionIntensityList;
    }

    /**
     * @return the landBankProspectIntensityList
     */
    public List<IntensityClass> getLandBankProspectIntensityList() {
        return landBankProspectIntensityList;
    }

    /**
     * @return the sourceList
     */
    public List<BOBSource> getSourceList() {
        return sourceList;
    }

    /**
     * @return the propExtDataListFiltered
     */
    public List<PropertyExtData> getPropExtDataListFiltered() {
        return propExtDataListFiltered;
    }

    /**
     * @return the formNoteText
     */
    public String getFormNoteText() {
        return formNoteText;
    }

    /**
     * @return the personSelected
     */
    public Person getPersonSelected() {
        return personSelected;
    }

    /**
     * @return the personToAddList
     */
    public List<Person> getPersonToAddList() {
        return personToAddList;
    }

    /**
     * @return the personLinkUseID
     */
    public boolean isPersonLinkUseID() {
        return personLinkUseID;
    }

    /**
     * @return the personIDToLink
     */
    public int.getHumanID()ToLink() {
        return personIDToLink;
    }

    /**
     * @return the selectedPropViewOption
     */
    public ViewOptionsProposalsEnum getSelectedPropViewOption() {
        return selectedPropViewOption;
    }

   

    /**
     * @param eventViewOptions the eventViewOptions to set
     */
    public void setEventViewOptions(List<ViewOptionsActiveHiddenListsEnum> eventViewOptions) {
        this.eventViewOptions = eventViewOptions;
    }

    /**
     * @param selectedPropertyUseType the selectedPropertyUseType to set
     */
    public void setSelectedPropertyUseType(PropertyUseType selectedPropertyUseType) {
        this.selectedPropertyUseType = selectedPropertyUseType;
    }

    /**
     * @param conditionIntensityList the conditionIntensityList to set
     */
    public void setConditionIntensityList(List<IntensityClass> conditionIntensityList) {
        this.conditionIntensityList = conditionIntensityList;
    }

    /**
     * @param landBankProspectIntensityList the landBankProspectIntensityList to set
     */
    public void setLandBankProspectIntensityList(List<IntensityClass> landBankProspectIntensityList) {
        this.landBankProspectIntensityList = landBankProspectIntensityList;
    }

    /**
     * @param sourceList the sourceList to set
     */
    public void setSourceList(List<BOBSource> sourceList) {
        this.sourceList = sourceList;
    }

    /**
     * @param propExtDataListFiltered the propExtDataListFiltered to set
     */
    public void setPropExtDataListFiltered(List<PropertyExtData> propExtDataListFiltered) {
        this.propExtDataListFiltered = propExtDataListFiltered;
    }

    /**
     * @param formNoteText the formNoteText to set
     */
    public void setFormNoteText(String formNoteText) {
        this.formNoteText = formNoteText;
    }

    /**
     * @param personSelected the personSelected to set
     */
    public void setPersonSelected(Person personSelected) {
        this.personSelected = personSelected;
    }

    /**
     * @param personToAddList the personToAddList to set
     */
    public void setPersonToAddList(List<Person> personToAddList) {
        this.personToAddList = personToAddList;
    }

    /**
     * @param personLinkUseID the personLinkUseID to set
     */
    public void setPersonLinkUseID(boolean personLinkUseID) {
        this.personLinkUseID = personLinkUseID;
    }

    /**
     * @param personIDToLink the personIDToLink to set
     */
    public void setPersonIDToLink(int personIDToLink) {
        this.personIDToLink = personIDToLink;
    }

    /**
     * @param selectedPropViewOption the selectedPropViewOption to set
     */
    public void setSelectedPropViewOption(ViewOptionsProposalsEnum selectedPropViewOption) {
        this.selectedPropViewOption = selectedPropViewOption;
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
    
    
    
    
}
