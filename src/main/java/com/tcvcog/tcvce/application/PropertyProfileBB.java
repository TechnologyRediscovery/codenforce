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

import com.tcvcog.tcvce.coordinators.*;
import com.tcvcog.tcvce.domain.*;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPermitType;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.util.*;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveListsEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsProposalsEnum;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 * The resurrected version of the Property Profile BB
 * At one point, this was collaspsed into the PropertySearchBB
 * when it was though these would happen at the same time;
 * Upon the dialogification of search based on the 
 * main container, this Bean was brought back to life and once
 * again occupies its hallowed role as the premier backing 
 * bean for individual parcel/property management
 * 
 * @author Ellen Bascomb of Apartment 31Y
 */
public class PropertyProfileBB 
        extends BackingBeanUtils{

    private PropertyDataHeavy currentProperty;
    private boolean currentPropertyEditMode;
    private boolean currentParcelInfoEditMode;
    
    private List<PropertyUseType> putList;
    
    // UNIT STUFF
    protected boolean unitEditMode;
    private PropertyUnitDataHeavy currentPropertyUnit;
    
    
    private CECase currentCECase;
    private EventCategory ceCaseOriginationEventSelected;
    private List<EventCategory> ceCaseOriginiationEventCandidateList;
    private boolean ceCaseUnitAssociated;
    private PropertyUnit ceCaseUnitAssociation;
    private String formUnitNoteText;
    
    
    private OccPeriod currentOccPeriod;
    private List<OccPermitType> occPeriodTypeList;
    private List<EventCategory> occPeriodOriginiationEventCandidateList;
    
    private Municipality muniSelected;
    
    private ViewOptionsActiveHiddenListsEnum eventViewOptionSelected;
    private List<ViewOptionsActiveHiddenListsEnum> eventViewOptions;

    private PropertyUseType selectedPropertyUseType;

    private List<IntensityClass> conditionIntensityList;
    private List<IntensityClass> landBankProspectIntensityList;
    private List<BOBSource> sourceList;
    
    private List<BlobLight> broadViewPhotoCandidates;
    private BlobLight selectedBoadviewPhoto;

    private String formNoteText;

    private Person personSelected;
    private List<Person> personToAddList;
    private boolean personLinkUseID;
    private int humanIDToLink;

    private ViewOptionsProposalsEnum selectedPropViewOption;
    
    // BLOBS
    private BlobLight currentBlob;
    /**
     * Creates a new instance of PropertyCreateBB
     */
    public PropertyProfileBB() {
        
    }
    
     
    @PostConstruct
    public void initBean(){
        PropertyIntegrator pi = getPropertyIntegrator();
        PropertyCoordinator pc = getPropertyCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        currentProperty = getSessionBean().getSessProperty();
        if(currentProperty != null){
            System.out.println("PropertyProfileBB.initBean() | Current Property: " + currentProperty.getParcelKey());
        } else {
            System.out.println("PropertyProfileBB.initBean() | Current Property null ");
            
        }
        reloadCurrentPropertyDataHeavy();
         try {
             setPutList(pi.getPropertyUseTypeList());
         } catch (IntegrationException ex) {
             System.out.println(ex);
         }
         
        currentParcelInfoEditMode = false;
        currentPropertyEditMode = false;
        try {
            setConditionIntensityList(sc.getIntensitySchemaWithClasses(
                    getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("intensityschema_propertycondition"))
                    .getClassList());
            setLandBankProspectIntensityList(sc.getIntensitySchemaWithClasses(
                    getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("intensityschema_landbankprospect"))
                    .getClassList());
        } catch (IntegrationException  ex) {
            System.out.println(ex);
        }
         
        occPeriodTypeList = getSessionBean().getSessMuni().getProfile().getOccPermitTypeList();
        sourceList = sc.getBobSourceListComplete();
        personToAddList = new ArrayList<>();
        eventViewOptions = Arrays.asList(ViewOptionsActiveHiddenListsEnum.values());
        eventViewOptionSelected = ViewOptionsActiveHiddenListsEnum.VIEW_ACTIVE_NOTHIDDEN;
        getSessionBean().setSessHumanListRefreshedList(null);
        
    }
    
      /**
     * Final step in creating a new occ period
     * @return 
     */
    public String onAddOccperiodCommitButtonChange(){
        
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            int newID = oc.insertOccPeriod(currentOccPeriod, getSessionBean().getSessUser());
            getSessionBean().setSessOccPeriod(oc.assembleOccPeriodDataHeavy(oc.getOccPeriod(newID), getSessionBean().getSessUser()));
        } catch (IntegrationException | BObStatusException | EventException | SearchException ex) {
            System.out.println("PropertyProfileBB.onAddOccperiodCommitButtonChange | " + ex.getClass().getCanonicalName());
            System.out.println(ex.getMessage());
            System.out.println(ex);
            getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "Could not commit new permit file: " , ""));
            return "";
        }
        return "occPeriodWorkflow";
    }
    
    
    /**
     * Listener to go and see occ period page
     * @param op
     * @return 
     */
   public String exploreOccPeriod(OccPeriod op){
       OccupancyCoordinator oc = getOccupancyCoordinator();
       if(op != null){
           try {
               getSessionBean().setSessOccPeriod(oc.assembleOccPeriodDataHeavy(op, getSessionBean().getSessUser()));
           } catch (IntegrationException | BObStatusException | SearchException ex) {
               System.out.println(ex);
           }
       } else {
           return "";
       }
       
       return "occPeriodWorkflow";
   }
    
      /**
     * Called when the user initiates new occ period creation
     * @param pu 
     */
    public void onOccperiodCreateInitButtonChange(PropertyUnit pu){
        PropertyCoordinator pc = getPropertyCoordinator();
        OccupancyCoordinator oc = getOccupancyCoordinator();
        EventCoordinator ec = getEventCoordinator();
        try {
            currentOccPeriod = oc.getOccPeriodSkeleton(pu, getSessionBean().getSessUser());
        } catch (BObStatusException ex) {
            System.out.println(ex);
              getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to start occ period creation process, sorry. ", 
                        ""));
        }
        occPeriodOriginiationEventCandidateList = ec.determinePermittedEventCategories(EventType.Occupancy, getSessionBean().getSessUser());
        
        try {
            currentPropertyUnit = pc.getPropertyUnitDataHeavy(pu, getSessionBean().getSessUser());
        } catch (IntegrationException | AuthorizationException | BObStatusException | EventException  ex) {
            System.out.println(ex);
        } 
    }
    
    
    /**
     * Listener for user requests to start property creation 
     * process
     * @param ev 
     */
    public void initiatePropertyCreation(ActionEvent ev){
        
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            currentProperty = pc.assemblePropertyDataHeavy(pc.generatePropertySkeleton(getSessionBean().getSessMuni()),getSessionBean().getSessUser());
        } catch (IntegrationException | BObStatusException | SearchException | BlobException ex) {
            System.out.println(ex);
        } 
    }
    
    /**
     * Listener for user requests to start the broadview photo update process
     * @param ev 
     */
    public void onChangeBroadviewPhotoInitLinkClick(ActionEvent ev){
        BlobCoordinator bc = getBlobCoordinator();
        
        broadViewPhotoCandidates = bc.assembleBrowserViewableBlobs(currentProperty.getBlobList());
        try {
            broadViewPhotoCandidates.add(bc.getDefaultBroadviewPhoto());
        } catch (IntegrationException | BlobException ex) {
            System.out.println(ex);
        } 
    
    }
    
    
    /**
     * Listener for user requests to commit the new broadview photo
     * @param blight    
     */
    public void onChangeBroadviewPhotoCommitLinkClick(BlobLight blight){
        PropertyCoordinator pc = getPropertyCoordinator();
        selectedBoadviewPhoto = blight;
        if(selectedBoadviewPhoto != null){
            currentProperty.setBroadviewPhoto(selectedBoadviewPhoto);
            try {
                pc.updatePropertyDataHeavyBroadviewPhoto(currentProperty, getSessionBean().getSessUser());
                reloadCurrentPropertyDataHeavy();
            } catch (BObStatusException | IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                           "ERROR updating photo", ""));
            } 
        } else {
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Cannot update broadview photo to null", ""));
        }
        
        
    }
    
    /**
     * Listener for user requests to insert a new property
     * @param ev 
     */
    public void insertProp(ActionEvent ev){
        PropertyCoordinator pc = getPropertyCoordinator();
        UserAuthorized ua = getSessionBean().getSessUser();
        SystemCoordinator sc = getSystemCoordinator();
        int freshID = 0;
        try {
            freshID = pc.addParcel(currentProperty, ua);
            currentProperty = pc.getPropertyDataHeavy(freshID, getSessionBean().getSessUser()); 
            getSessionBean().setSessProperty(pc.assemblePropertyDataHeavy(currentProperty, ua));
            sc.logObjectView(ua, currentProperty);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Successfully inserted property with ID " + freshID
                                + ", which is now your 'active property'", ""));
            
            
        } catch (IntegrationException | BObStatusException | SearchException | AuthorizationException | EventException | BlobException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to insert property in database, sorry. ", 
                        "Please make sure all required fields are completed. "));
        }
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
            currentProperty = pc.assemblePropertyDataHeavy(currentProperty, getSessionBean().getSessUser());
             getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Reloaded property id " + currentProperty.getPropertyID(), ""));
        } catch (IntegrationException | BObStatusException | SearchException | BlobException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Fatal error reloading property; sorries!", ""));
        }
    }
    
    /**
     * Listener for user requests to toggle edit mode of parcel level 
     * data
     * @param ev 
     */
    public void onPropertyEditModeToggleButtonChange(ActionEvent ev){
        System.out.println("PropertyProfileBB.onPropertyEditModeToggleButtonChange | edit mode start:" + currentPropertyEditMode);
        if(currentPropertyEditMode && currentProperty != null){
            
            if(currentProperty.getParcelKey() == 0){
                // we have a new property 
                
                // this page will never have a new property. If we do, throw error
                System.out.println("PropertyProfileBB.onPropertyEditModeToggleButtonChange | found property without ID on property profile page!! ERROR!");
                
            } else {
                // we have an existing property
                onPropertyUpdateCommit();
            }
        }
        currentPropertyEditMode = !currentPropertyEditMode;
    }
    
    /**
     * Listener for user requests to abandon a parcel info edit operation
     * 
     * @param ev 
     */
    public void onPropertyEditModeOperationAbortButtonChange(ActionEvent ev){
        System.out.println("PropertyProfileBB.abortPropertyEditmode");
    }
    
    /**
     * Listener for user requests to abandon a parcel info edit operation
     * 
     * @param ev 
     */
    public void onParcelInfoEditModeOperationAbortButtonChange(ActionEvent ev){
        System.out.println("PropertyProfileBB.abortParcelInfoEditmode");
    }
    
    /**
     * Listener for user requests to toggle edit mode of a parcel info record
     * @param ev 
     */
    public void onParcelInfoEditModeToggleButtonChange(ActionEvent ev){
        System.out.println("PropertyProfileBB.onParcelInfoEditModeToggleButtonChange | edit mode start:" + currentParcelInfoEditMode);
        
        if(currentParcelInfoEditMode && currentProperty != null && currentProperty.getParcelInfo() != null){
            if(currentProperty.getParcelInfo().getParcelInfoID() == 0){
                 onParcelInfoAddCommit();
                  
            } else {
                onParcelInfoUpdateCommit();
            }
        }
        currentParcelInfoEditMode = !currentParcelInfoEditMode;
    }
   
    /**
     * Listener for user requests to commit property updates
     */
    public void onPropertyUpdateCommit() {
        PropertyCoordinator pc = getPropertyCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        try {
//            currentProperty.setAbandonedDateStart(pc.configureDateTime(currentProperty.getAbandonedDateStart().to));
            pc.updatePropertyDataHeavyBroadviewPhoto(currentProperty, getSessionBean().getSessUser());
            reloadCurrentPropertyDataHeavy();
            sc.logObjectView(getSessionBean().getSessUser(), currentProperty);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully updated property with ID " + getCurrentProperty().getParcelKey()
                            + ", which is now your 'active property'", ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update property, sorries! " + ex.toString(), ""));
        }
    }
    
    /**
     * Writes new parcel info record to the DB
     */
    public void onParcelInfoAddCommit(){
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            pc.insertParcelInfoRecord(currentProperty.getParcelInfo(), getSessionBean().getSessUser());
            reloadCurrentPropertyDataHeavy();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully inserted a new property info record", ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "FATAL: Could not insert parcel info! " + ex.toString(), ""));
        }
    }
    
    /**
     * Writes changes to current parcel info record to the DB
     */
    public void onParcelInfoUpdateCommit(){
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            pc.updateParcelInfoRecord(currentProperty.getParcelInfo(), getSessionBean().getSessUser());
            reloadCurrentPropertyDataHeavy();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully updated parcel info record ID: " + currentProperty.getParcelInfo().getParcelInfoID(), ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "FATAL: Could not update parcel info! " + ex.toString(), ""));
        }
    }
    
      /**
     * Special getter that checks the session for updates on just this person.
     * @return 
     */
    public List<MailingAddressLink> getCurrentPropertyMADLinkList(){
        if(currentProperty != null){
            List<MailingAddressLink> sessLinkList = getSessionBean().getSessMailingAddressLinkRefreshedList();
            if(sessLinkList != null){
                System.out.println("PropertyProfileBB.currentPropertyMADLinkList | sessLinkListSize: " + sessLinkList.size());
                currentProperty.setMailingAddressLinkList(sessLinkList);
                getSessionBean().setSessMailingAddressLinkRefreshedList(null);
            }
            return currentProperty.getMailingAddressLinkList();
        }
        return new ArrayList<>();
    }
    
    /**
     * Listener for when the user aborts a property add operation;
     *
     * @param ev
     */
    public void onDiscardNewPropertyDataButtonChange(ActionEvent ev) {

    }

    /**
     * Listener for user requests to explore the property info cases on this
     * property
     *
     * @return
     */
    public String onInfoCaseListButtonChange() throws BObStatusException {
        CaseCoordinator cc = getCaseCoordinator();

        if (currentProperty != null && currentProperty.getPropInfoCaseList() != null) {
            getSessionBean().setSessCECaseListWithDowncastAndLookup(currentProperty.getPropInfoCaseList());
        }
        return "ceCaseSearch";
    }
   
    
    
    /**
     * Special wrapper getter around the current property's human
     * link list that asks the session for a new link list
     * on table load that might occur during a link edit operation
     * @return the new human link list
     */
    public List<HumanLink> getManagedHumanLinkList(){
        List<HumanLink> hll = getSessionBean().getSessHumanListRefreshedList();
        if(hll != null){
            currentProperty.setHumanLinkList(hll);
            // clear our refreshed list
            getSessionBean().setSessHumanListRefreshedList(null);
        }
        return currentProperty.getHumanLinkList();
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

    /**
     * Listener for user requests to manage units on a property
     * @param ev 
     */
    public void onPropUnitExploreButtonChange(ActionEvent ev) {
        System.out.println("PropertyProfileBB.onPropUnitExploreButtonChange");
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
     * Listener for user requests to view an occ period
     * @param op
     * @return 
     */
    public String onViewOccPeriodButtonChange(OccPeriod op){
        if(op != null){
            getSessionBean().setSessOccPeriodFromPeriodBase(op);
            return "occPeriodWorkflow";
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
     * Listener for user requests to link a new address to the 
     * current parcel
     * 
     * @param ev 
     */
    public void onLinkNewAddressToParcelButtonChange(ActionEvent ev){
        System.out.println("PropertyProfileBB.linkNewAddressToParcel");
    }
    
    // ********************************************************
    // ********************* CECASE ***************************
    // ********************************************************
    
    /**
     * Listener for user requests to start the ce case creation process;
     * I also build a list of eligible event categories for origination
     */
    public void onCECaseAddInitButtonChange(){
        CaseCoordinator cc = getCaseCoordinator();
        EventCoordinator ec = getEventCoordinator();
        ceCaseOriginiationEventCandidateList = ec.determinePermittedEventCategories(EventType.Origination, getSessionBean().getSessUser());
        try {
            currentCECase = cc.cecase_initCECase(currentProperty, getSessionBean().getSessUser());
        } catch (IntegrationException ex) {
            System.out.println(ex);
            
        }
        
    }
    
     /**
     * Listener method for creation of a new case
     * @return routing string: case profile
     */
    public String onAddNewCaseCommitButtonChange(){
        CaseCoordinator cc = getCaseCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        // check to see if we have an action request that needs to be connected
        // to this new case
        CEActionRequest cear = getSessionBean().getSessCEAR();
        
        try {
            if(currentCECase == null){
                throw new BObStatusException("Cannot init new case with null case");
            }
            if(currentCECase.getNotes() != null){
                MessageBuilderParams mbp = new MessageBuilderParams(
                        null, 
                        "Case Origination Note", 
                        null, 
                        currentCECase.getNotes(), 
                        getSessionBean().getSessUser(), 
                        null);
                currentCECase.setNotes(sc.appendNoteBlock(mbp));
                        
            }
            
            if(ceCaseUnitAssociated && ceCaseUnitAssociation != null){
                currentCECase.setPropertyUnitID(ceCaseUnitAssociation.getUnitID());
            }
            
            int freshID = cc.cecase_insertNewCECase(
                    currentCECase, 
                    getSessionBean().getSessUser(),
                    ceCaseOriginationEventSelected,
                    cear);
            getSessionBean().setSessCECase(cc.cecase_assembleCECaseDataHeavy(cc.cecase_getCECase(freshID), getSessionBean().getSessUser()));
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully added case to property! Access the case from the list below.", ""));
            //Send them back to the last page!
            return "ceCaseProfile";
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Integration Module error: Unable to add case to current property.",
                            "Best try again or note the error and complain to Eric."));
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "A code enf action request was found in queue to be attached to this case. "
                            + "I couldn't do that, though, sorry..",
                            "Best try again or note the error and complain to Eric."));
            System.out.println(ex);

        } catch (ViolationException | EventException | SearchException ex) {
            System.out.println(ex);
        }

        //reload page on error
        return "";
    }
    
    // ********************************************************
    // *******************   UNIT TOOLS ***********************
    // ********************************************************
    
      /**
       * Listener for user requests to view or edit a property unit
       * 
       * @param pu 
       */
      public void onUnitViewEditLinkClick(PropertyUnit pu){
          PropertyCoordinator pc = getPropertyCoordinator();
        try {
            currentPropertyUnit = pc.getPropertyUnitDataHeavy(pu, getSessionBean().getSessUser());
        } catch (IntegrationException | AuthorizationException | BObStatusException | EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not load property unit with lists" + ex.getMessage(), ""));
        } 
      }

      /**
       * listener for user requests to start the unit add process
       * @param ev 
       */
      public void onUnitAddInitButtonChange(ActionEvent ev){
          PropertyCoordinator pc = getPropertyCoordinator();
        try {
            currentPropertyUnit = pc.getPropertyUnitDataHeavy(pc.getPropertyUnitSkeleton(currentProperty), getSessionBean().getSessUser());
            unitEditMode = true;
        } catch (IntegrationException | AuthorizationException | BObStatusException | EventException ex) {
            System.out.println(ex);
              getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fatal error starting unit creation: " + ex.getMessage(), ""));
        } 
      }
      
      /**
       * Listener for the press of the unit edit on/off mode
       * @param ev 
       */
      public void onUnitEditToggleButtonChange(ActionEvent ev){
          if(unitEditMode){
              if(currentPropertyUnit != null){
                  if(currentPropertyUnit.getUnitID() == 0){
                      onUnitAddCommit();
                  } else {
                      onUnitUpdateCommit();
                  }
                  reloadCurrentPropertyDataHeavy();
              }
          }
          unitEditMode = !unitEditMode;
      }
      
      /**
       * Listener for user request to abort the unit edit process
       * @param ev 
       */
      public void onUnitEditModeAbortButtonChange(ActionEvent ev){
          unitEditMode = !unitEditMode;
      }
      
      /**
       * Internal caller to the coordinator for unit adds
       */
      private void onUnitAddCommit(){
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            int freshID = pc.insertPropertyUnit(currentPropertyUnit, currentProperty, getSessionBean().getSessUser());
            if(freshID != 0){

                currentPropertyUnit = pc.getPropertyUnitDataHeavy(pc.getPropertyUnit(freshID), getSessionBean().getSessUser());

                getFacesContext().addMessage(null,
                      new FacesMessage(FacesMessage.SEVERITY_INFO,
                              "Unit add success!", ""));
            } else {
                throw new IntegrationException("could not get a non-zero New Unit ID");
                
            }
        } catch (BObStatusException | AuthorizationException | EventException | IntegrationException ex) {
            System.out.println(ex);
              getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fatal error on unit insert: " + ex.getMessage(), ""));
        }
      }
      
      /**
       * Listener for user requests to start the note on unit process
       * @param ev 
       */
      public void onUnitNoteAddInitButtonChange(ActionEvent ev){
          formUnitNoteText = "";
      }
      
      /**
       * Listener for user requests to commit a note to a unit
       * @param ev 
       */
      public void onUnitNoteAddCommitButtonChange(ActionEvent ev){
          SystemCoordinator sc = getSystemCoordinator();
          PropertyCoordinator pc = getPropertyCoordinator();
          MessageBuilderParams mbp = new MessageBuilderParams(currentPropertyUnit.getNotes(), null, null , formUnitNoteText, getSessionBean().getSessUser(), null);
          currentPropertyUnit.setNotes(sc.appendNoteBlock(mbp));
        try {
            pc.updatePropertyUnit(currentPropertyUnit, getSessionBean().getSessUser());
            currentPropertyUnit = pc.getPropertyUnitDataHeavy(currentPropertyUnit, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                  new FacesMessage(FacesMessage.SEVERITY_INFO,
                          "Unit note update succses! Great work!", ""));
        } catch (BObStatusException | AuthorizationException  | EventException | IntegrationException  ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                  new FacesMessage(FacesMessage.SEVERITY_ERROR,
                          "Unit  note write failiure.", ""));
        }
          
      }
      
      /**
       * Internal caller to the coordinator for unit updates
       */
      private void onUnitUpdateCommit(){
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            pc.updatePropertyUnit(currentPropertyUnit,getSessionBean().getSessUser());
            currentPropertyUnit = pc.getPropertyUnitDataHeavy(pc.getPropertyUnit(currentPropertyUnit.getUnitID()), getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                  new FacesMessage(FacesMessage.SEVERITY_INFO,
                          "Unit update success!", ""));
        } catch (BObStatusException | AuthorizationException | EventException | IntegrationException ex) {
            System.out.println(ex);
              getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fatal error on unit update: " + ex.getMessage(), ""));
        }
      }
    
    // ********************************************************
    // **********   GETTERS AND SETTERS ***********************
    // ********************************************************

    /**
     * The exception to the "no logic in getters and setters" rule: 
     * this getter monitors the object
     * reload trigger so we can see changes made by
     * multi-use components such as the person linker or blob linker
     * 
     * @return the currentProperty
     */
    public PropertyDataHeavy getCurrentProperty() {
       
        return currentProperty;
    }

    /**
     * @param currentProperty the currentProperty to set
     */
    public void setCurrentProperty(PropertyDataHeavy currentProperty) {
        this.currentProperty = currentProperty;
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
     * @return the occPeriodTypeList
     */
    public List<OccPermitType> getOccPeriodTypeList() {
        return occPeriodTypeList;
    }

   

    /**
     * @param occPeriodTypeList the occPeriodTypeList to set
     */
    public void setOccPeriodTypeList(List<OccPermitType> occPeriodTypeList) {
        this.occPeriodTypeList = occPeriodTypeList;
    }

    /**
     * @return the currentOccPeriod
     */
    public OccPeriod getCurrentOccPeriod() {
        return currentOccPeriod;
    }

    /**
     * @param currentOccPeriod the currentOccPeriod to set
     */
    public void setCurrentOccPeriod(OccPeriod currentOccPeriod) {
        this.currentOccPeriod = currentOccPeriod;
    }

    /**
     * @return the muniSelected
     */
    public Municipality getMuniSelected() {
        return muniSelected;
    }

    /**
     * @return the eventViewOptionSelected
     */
    public ViewOptionsActiveHiddenListsEnum getEventViewOptionSelected() {
        return eventViewOptionSelected;
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
     * @return the humanIDToLink
     */
    public int getHumanIDToLink() {
        return humanIDToLink;
    }

    /**
     * @return the selectedPropViewOption
     */
    public ViewOptionsProposalsEnum getSelectedPropViewOption() {
        return selectedPropViewOption;
    }

    /**
     * @return the currentBlob
     */
    public BlobLight getCurrentBlob() {
        return currentBlob;
    }

    /**
     * @param muniSelected the muniSelected to set
     */
    public void setMuniSelected(Municipality muniSelected) {
        this.muniSelected = muniSelected;
    }

    /**
     * @param eventViewOptionSelected the eventViewOptionSelected to set
     */
    public void setEventViewOptionSelected(ViewOptionsActiveHiddenListsEnum eventViewOptionSelected) {
        this.eventViewOptionSelected = eventViewOptionSelected;
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
     * @param humanIDToLink the humanIDToLink to set
     */
    public void setHumanIDToLink(int humanIDToLink) {
        this.humanIDToLink = humanIDToLink;
    }

    /**
     * @param selectedPropViewOption the selectedPropViewOption to set
     */
    public void setSelectedPropViewOption(ViewOptionsProposalsEnum selectedPropViewOption) {
        this.selectedPropViewOption = selectedPropViewOption;
    }

    /**
     * @param currentBlob the currentBlob to set
     */
    public void setCurrentBlob(BlobLight currentBlob) {
        this.currentBlob = currentBlob;
    }

    /**
     * @return the currentPropertyUnit
     */
    public PropertyUnitDataHeavy getCurrentPropertyUnit() {
        return currentPropertyUnit;
    }

    /**
     * @param currentPropertyUnit the currentPropertyUnit to set
     */
    public void setCurrentPropertyUnit(PropertyUnitDataHeavy currentPropertyUnit) {
        this.currentPropertyUnit = currentPropertyUnit;
    }

    /**
     * @return the currentPropertyEditMode
     */
    public boolean isCurrentPropertyEditMode() {
        return currentPropertyEditMode;
    }

    /**
     * @return the currentParcelInfoEditMode
     */
    public boolean isCurrentParcelInfoEditMode() {
        return currentParcelInfoEditMode;
    }

    /**
     * @param currentPropertyEditMode the currentPropertyEditMode to set
     */
    public void setCurrentPropertyEditMode(boolean currentPropertyEditMode) {
        this.currentPropertyEditMode = currentPropertyEditMode;
    }

    /**
     * @param currentParcelInfoEditMode the currentParcelInfoEditMode to set
     */
    public void setCurrentParcelInfoEditMode(boolean currentParcelInfoEditMode) {
        this.currentParcelInfoEditMode = currentParcelInfoEditMode;
    }

    /**
     * @return the currentCECase
     */
    public CECase getCurrentCECase() {
        return currentCECase;
    }

    /**
     * @return the ceCaseOriginiationEventCandidateList
     */
    public List<EventCategory> getCeCaseOriginiationEventCandidateList() {
        return ceCaseOriginiationEventCandidateList;
    }

    /**
     * @param currentCECase the currentCECase to set
     */
    public void setCurrentCECase(CECase currentCECase) {
        this.currentCECase = currentCECase;
    }

    /**
     * @param ceCaseOriginiationEventCandidateList the ceCaseOriginiationEventCandidateList to set
     */
    public void setCeCaseOriginiationEventCandidateList(List<EventCategory> ceCaseOriginiationEventCandidateList) {
        this.ceCaseOriginiationEventCandidateList = ceCaseOriginiationEventCandidateList;
    }

    /**
     * @return the ceCaseOriginationEventSelected
     */
    public EventCategory getCeCaseOriginationEventSelected() {
        return ceCaseOriginationEventSelected;
    }

    /**
     * @param ceCaseOriginationEventSelected the ceCaseOriginationEventSelected to set
     */
    public void setCeCaseOriginationEventSelected(EventCategory ceCaseOriginationEventSelected) {
        this.ceCaseOriginationEventSelected = ceCaseOriginationEventSelected;
    }

    /**
     * @return the ceCaseUnitAssociated
     */
    public boolean isCeCaseUnitAssociated() {
        return ceCaseUnitAssociated;
    }

    /**
     * @param ceCaseUnitAssociated the ceCaseUnitAssociated to set
     */
    public void setCeCaseUnitAssociated(boolean ceCaseUnitAssociated) {
        this.ceCaseUnitAssociated = ceCaseUnitAssociated;
    }

    /**
     * @return the ceCaseUnitAssociation
     */
    public PropertyUnit getCeCaseUnitAssociation() {
        return ceCaseUnitAssociation;
    }

    /**
     * @param ceCaseUnitAssociation the ceCaseUnitAssociation to set
     */
    public void setCeCaseUnitAssociation(PropertyUnit ceCaseUnitAssociation) {
        this.ceCaseUnitAssociation = ceCaseUnitAssociation;
    }

  

    /**
     * @return the occPeriodOriginiationEventCandidateList
     */
    public List<EventCategory> getOccPeriodOriginiationEventCandidateList() {
        return occPeriodOriginiationEventCandidateList;
    }

    /**
     * @param occPeriodOriginiationEventCandidateList the occPeriodOriginiationEventCandidateList to set
     */
    public void setOccPeriodOriginiationEventCandidateList(List<EventCategory> occPeriodOriginiationEventCandidateList) {
        this.occPeriodOriginiationEventCandidateList = occPeriodOriginiationEventCandidateList;
    }

    /**
     * @return the unitEditMode
     */
    public boolean isUnitEditMode() {
        return unitEditMode;
    }

    /**
     * @param unitEditMode the unitEditMode to set
     */
    public void setUnitEditMode(boolean unitEditMode) {
        this.unitEditMode = unitEditMode;
    }

    /**
     * @return the formUnitNoteText
     */
    public String getFormUnitNoteText() {
        return formUnitNoteText;
    }

    /**
     * @param formUnitNoteText the formUnitNoteText to set
     */
    public void setFormUnitNoteText(String formUnitNoteText) {
        this.formUnitNoteText = formUnitNoteText;
    }

    /**
     * @return the selectedBoadviewPhoto
     */
    public BlobLight getSelectedBoadviewPhoto() {
        return selectedBoadviewPhoto;
    }

    /**
     * @param selectedBoadviewPhoto the selectedBoadviewPhoto to set
     */
    public void setSelectedBoadviewPhoto(BlobLight selectedBoadviewPhoto) {
        this.selectedBoadviewPhoto = selectedBoadviewPhoto;
    }

    /**
     * @return the broadViewPhotoCandidates
     */
    public List<BlobLight> getBroadViewPhotoCandidates() {
        return broadViewPhotoCandidates;
    }

    /**
     * @param broadViewPhotoCandidates the broadViewPhotoCandidates to set
     */
    public void setBroadViewPhotoCandidates(List<BlobLight> broadViewPhotoCandidates) {
        this.broadViewPhotoCandidates = broadViewPhotoCandidates;
    }

  
    
}
