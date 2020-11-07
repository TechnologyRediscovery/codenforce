package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.IntensityClass;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.PageModeEnum;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.PropertyExtData;
import com.tcvcog.tcvce.entities.PropertyUseType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsProposalsEnum;
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

/*
 * Copyright (C) 2018 Technology Rediscovery LLC
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
/**
 *
 * @author ellen bascomb of apt 31y
 */
public class PropertyProfileBB extends BackingBeanUtils implements Serializable {

    private PageModeEnum currentMode;
    private List<PageModeEnum> pageModes;

    private PropertyDataHeavy currentProperty;
    private boolean currentPropertySelected;

    private Municipality muniSelected;
    
    private ViewOptionsActiveHiddenListsEnum eventViewOptionSelected;
    private List<ViewOptionsActiveHiddenListsEnum> eventViewOptions;

    private List<PropertyUseType> putList;
    private PropertyUseType selectedPropertyUseType;

    private List<IntensityClass> conditionIntensityList;
    protected List<IntensityClass> landBankProspectIntensityList;
    private List<BOBSource> sourceList;

    private List<PropertyExtData> propExtDataListFiltered;

    private String formNoteText;

    private Person personSelected;
    private List<Person> personToAddList;
    private boolean personLinkUseID;
    private int personIDToLink;

    private ViewOptionsProposalsEnum selectedPropViewOption;

    /**
     * Creates a new instance of PropertyProfileBB
     */
    public PropertyProfileBB() {
    }

    @PostConstruct
    public void initBean() {
        
        PropertyIntegrator pi = getPropertyIntegrator();
        PropertyCoordinator pc = getPropertyCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        pageModes = new ArrayList<>();
        pageModes.add(PageModeEnum.LOOKUP);
        pageModes.add(PageModeEnum.INSERT);
        pageModes.add(PageModeEnum.UPDATE);
        pageModes.add(PageModeEnum.REMOVE);

        if (getSessionBean().isStartPropInfoPageWithAdd()) {
            currentMode = PageModeEnum.INSERT;
        } else {
            currentMode = PageModeEnum.LOOKUP;
        }

        personToAddList = new ArrayList<>();
        eventViewOptions = Arrays.asList(ViewOptionsActiveHiddenListsEnum.values());
        eventViewOptionSelected = ViewOptionsActiveHiddenListsEnum.VIEW_ACTIVE_NOTHIDDEN;

        // use same pathway as clicking the button
        setCurrentMode(currentMode);

        try {
            currentProperty = getSessionBean().getSessProperty();
            if(currentProperty != null){
                currentProperty = pc.assemblePropertyDataHeavy(currentProperty, getSessionBean().getSessUser());
                System.out.println("PropertyProfileBB.initBean(): reassembled pdh");
            }
            currentPropertySelected = true;
            muniSelected = getSessionBean().getSessMuni();

            putList = pi.getPropertyUseTypeList();
            sourceList = sc.getBobSourceListComplete();
            conditionIntensityList = sc.getIntensitySchemaWithClasses(
                    getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("intensityschema_propertycondition"))
                    .getClassList();
            landBankProspectIntensityList = sc.getIntensitySchemaWithClasses(
                    getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("intensityschema_landbankprospect"))
                    .getClassList();
        } catch (IntegrationException | BObStatusException | SearchException ex) {
            System.out.println(ex);
        }
    }

    /**
     * Getter for currentMode
     *
     * @return
     */
    public PageModeEnum getCurrentMode() {
        return currentMode;
    }

    /**
     * Responds to the user clicking one of the page modes: LOOKUP, ADD, UPDATE,
     * REMOVE
     *
     * @param mode
     */
    public void setCurrentMode(PageModeEnum mode) {

        //store currentMode into tempCurMode as a temporary value, in case the currenMode equal null
        PageModeEnum tempCurMode = this.currentMode;
        //reset default setting every time the Mode has been selected 
//        loadDefaultPageConfig();
        //check the currentMode == null or not
        if (currentMode == null) {
            this.currentMode = tempCurMode;
        } else {
            this.currentMode = mode;
            switch (currentMode) {
                case LOOKUP:
                    initPropertyLookup();
                    break;
                case INSERT:
                    initiatePropertyAdd();
                    break;
                case UPDATE:
                    initiatePropertyUpdate();
                    break;
                case REMOVE:
                    initiatePropertyRemove();
                    break;
                default:
                    break;

            }
        }
        if (currentMode != null) {
            //show the current mode in p:messages box
//            getFacesContext().addMessage(null,
//                    new FacesMessage(FacesMessage.SEVERITY_INFO,
//                            this.currentMode.getTitle() + " Mode Selected", ""));
        }

        if (currentProperty != null) {
            currentPropertySelected = true;
        }

    }

    //check if current mode == Lookup
    public boolean getActiveLookupMode() {
        // hard-wired on since there's always a property loaded
        return PageModeEnum.LOOKUP.equals(currentMode);
    }

    //check if current mode == Lookup
    public boolean getActiveViewMode() {
        return PageModeEnum.VIEW.equals(currentMode) || PageModeEnum.LOOKUP.equals(currentMode);

    }

    //check if current mode == Insert
    public boolean getActiveInsertUpdateMode() {
        return PageModeEnum.INSERT.equals(currentMode) || PageModeEnum.UPDATE.equals(currentMode);
    }

    //check if current mode == Remove
    public boolean getActiveRemoveMode() {
        return PageModeEnum.REMOVE.equals(currentMode);
    }

    /**
     * Initialize the whole page into default setting
     */
    public void loadDefaultPageConfig() {
        System.out.println("EventRuleConfigBB.loadDefaultPageConfig()");

        currentPropertySelected = true;
        //initialize default current basic muni list
    }
    
    /**
     * Utilty method for refreshing property
     */
    public void reloadCurrentPropertyDataHeavy(){
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            currentProperty = pc.assemblePropertyDataHeavy(currentProperty, getSessionBean().getSessUser());
             getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
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
     * Action listener for user request to view a full property record from Also
     * acts as a property reload tool the list
     *
     * @param prop
     */
    public void onExplorePropertyButtonChange(Property prop) {
        PropertyCoordinator pc = getPropertyCoordinator();
        if (prop != null) {
            try {
                currentProperty = pc.getPropertyDataHeavy(prop.getPropertyID(), getSessionBean().getSessUser());
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Loaded property record for " + prop.getAddress(), ""));
                getSessionBean().setSessProperty(currentProperty);
            } catch (AuthorizationException | BObStatusException | EventException | IntegrationException | SearchException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Fatal error loading property; apologies!", ""));
            }
        }
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
     */
    public void onNoteCommitButtonChange(ActionEvent ev) {
        SystemCoordinator sc = getSystemCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setCred(getSessionBean().getSessUser().getKeyCard());
        mbp.setExistingContent(currentProperty.getNotes());
        mbp.setNewMessageContent(formNoteText);
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
     * Listener for button clicks when user is ready to insert a new EventRule.
     * Delegates all work to internal method
     *
     * @return Empty string which prompts page reload without wiping bean memory
     */
    public String onInsertButtonChange() {
        //show successfully inserting message in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully Insert Municipality", ""));
        //reminder: set muniManageSample in faces-config.xml
        //return to muniManage_sample.xhtml page
        commitPropertyAdd();
        return "";
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
            if (personLinkUseID && personIDToLink != 0) {
                Person checkPer = null;
                checkPer = persc.getPerson(personIDToLink);
                if (checkPer != null && checkPer.getPersonID() != 0) {
                    pc.connectPersonToProperty(currentProperty, checkPer);
                    getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Connected " + checkPer.getLastName() + " to property ID " + currentProperty.getPropertyID(), ""));
                } else {
                    getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Could not find a Person with ID " + personIDToLink, ""));
                    
                }

            } else {
                if (personSelected != null) {
                    pc.connectPersonToProperty(currentProperty, personSelected);
                    getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Connected " + personSelected.getLastName() + " to property ID " + currentProperty.getPropertyID(), ""));
                } else {
                    
                    getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Could not complete link to person, sorry!", ""));
                }
            }
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            
        }

        return "propertyInfo";
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
        
        
        return "propertyInfo";
    }

    /**
     * Listener for user requests to submit object updates; Delegates all work
     * to internal method
     *
     * @return Empty string which prompts page reload without wiping bean memory
     */
    public String onUpdateButtonChange() {
        //show successfully updating message in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully Update Municipality", ""));
        //reminder: set muniManageSample in faces-config.xml
        //return to muniManage_sample.xhtml page
        commitPropertyUpdates();

        return "";
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

    private void initPropertyLookup() {

    }

    private void initiatePropertyView() {

    }

    /**
     * Internal logic container at commencement of property update operation
     */
    private void initiatePropertyUpdate() {

        // do nothing
    }

    /**
     * Internal logic container for property remove operations
     */
    private void initiatePropertyRemove() {

    }

    /**
     * Loads a skeleton property into which we inject values from the form
     */
    public void initiatePropertyAdd() {
        PropertyCoordinator pc = getPropertyCoordinator();
        currentProperty = pc.initPropertyDataHeavy(getSessionBean().getSessMuni());
    }

    /**
     * Liases with coordinator to insert a new property object
     */
    private void commitPropertyAdd() {
        PropertyCoordinator pc = getPropertyCoordinator();
        int newID = 0;
        try {
            newID = pc.addProperty(currentProperty, getSessionBean().getSessUser());
            getSessionBean().setSessProperty(pc.getPropertyDataHeavy(newID, getSessionBean().getSessUser()));
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully added property with ID: " + currentProperty.getPropertyID()
                            + ", which is now your 'active property'", ""));
        } catch (AuthorizationException | BObStatusException | EventException | IntegrationException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update property, sorries!" + ex.getClass().toString(), ""));
        }

    }

    private void commitPropertyUpdates() {
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
//            currentProperty.setAbandonedDateStart(pc.configureDateTime(currentProperty.getAbandonedDateStart().to));
            pc.editProperty(currentProperty, getSessionBean().getSessUser());
            currentProperty = pc.getPropertyDataHeavy(currentProperty.getPropertyID(), getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully updated property with ID " + getCurrentProperty().getPropertyID()
                            + ", which is now your 'active property'", ""));
        } catch (AuthorizationException | BObStatusException | EventException | IntegrationException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update property, sorries! " + ex.toString(), ""));
        }

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
            getSessionBean().setSessPerson(pc.assemblePersonDataHeavy(p, getSessionBean().getSessUser().getKeyCard()));
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

            try {
                getSessionBean().setSessCECase(cc.cecase_assembleCECaseDataHeavy(cse, getSessionBean().getSessUser()));
            } catch (BObStatusException | IntegrationException | SearchException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Could not load CE Case for viewing, sorry! " + ex.toString(), ""));
            }
        }
        return "ceCaseSearchProfile";
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

    /**
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
     * @return the pageModes
     */
    public List<PageModeEnum> getPageModes() {
        return pageModes;
    }

    /**
     * @return the currentPropertySelected
     */
    public boolean isCurrentPropertySelected() {
        return currentPropertySelected;
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
     * @return the sourceList
     */
    public List<BOBSource> getSourceList() {
        return sourceList;
    }

    /**
     * @param pageModes the pageModes to set
     */
    public void setPageModes(List<PageModeEnum> pageModes) {
        this.pageModes = pageModes;
    }

    /**
     * @param currentPropertySelected the currentPropertySelected to set
     */
    public void setCurrentPropertySelected(boolean currentPropertySelected) {
        this.currentPropertySelected = currentPropertySelected;
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
     * @param sourceList the sourceList to set
     */
    public void setSourceList(List<BOBSource> sourceList) {
        this.sourceList = sourceList;
    }

    /**
     * @return the propExtDataListFiltered
     */
    public List<PropertyExtData> getPropExtDataListFiltered() {
        return propExtDataListFiltered;
    }

    /**
     * @param propExtDataListFiltered the propExtDataListFiltered to set
     */
    public void setPropExtDataListFiltered(List<PropertyExtData> propExtDataListFiltered) {
        this.propExtDataListFiltered = propExtDataListFiltered;
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
     * @return the personToAddList
     */
    public List<Person> getPersonToAddList() {
        return personToAddList;
    }

    /**
     * @param personToAddList the personToAddList to set
     */
    public void setPersonToAddList(List<Person> personToAddList) {
        this.personToAddList = personToAddList;
    }

    /**
     * @return the personSelected
     */
    public Person getPersonSelected() {
        return personSelected;
    }

    /**
     * @param personSelected the personSelected to set
     */
    public void setPersonSelected(Person personSelected) {
        this.personSelected = personSelected;
    }

    /**
     * @return the selectedPropViewOption
     */
    public ViewOptionsProposalsEnum getSelectedPropViewOption() {
        return selectedPropViewOption;
    }

    /**
     * @param selectedPropViewOption the selectedPropViewOption to set
     */
    public void setSelectedPropViewOption(ViewOptionsProposalsEnum selectedPropViewOption) {
        this.selectedPropViewOption = selectedPropViewOption;
    }

    /**
     * @return the landBankProspectIntensityList
     */
    public List<IntensityClass> getLandBankProspectIntensityList() {
        return landBankProspectIntensityList;
    }

    /**
     * @param landBankProspectIntensityList the landBankProspectIntensityList to
     * set
     */
    public void setLandBankProspectIntensityList(List<IntensityClass> landBankProspectIntensityList) {
        this.landBankProspectIntensityList = landBankProspectIntensityList;
    }

    /**
     * @return the personLinkUseID
     */
    public boolean isPersonLinkUseID() {
        return personLinkUseID;
    }

    /**
     * @param personLinkUseID the personLinkUseID to set
     */
    public void setPersonLinkUseID(boolean personLinkUseID) {
        this.personLinkUseID = personLinkUseID;
    }

    /**
     * @return the personIDToLink
     */
    public int getPersonIDToLink() {
        return personIDToLink;
    }

    /**
     * @param personIDToLink the personIDToLink to set
     */
    public void setPersonIDToLink(int personIDToLink) {
        this.personIDToLink = personIDToLink;
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


}
