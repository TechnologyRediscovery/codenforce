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

import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.WorkflowCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventRuleAbstract;
import com.tcvcog.tcvce.entities.EventRuleSet;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.PageModeEnum;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 * Model backing bean for our Beta release in July 2020 with the page mode
 * design implemented site-wide created by Chen&Chen
 * 
 * @author sylvia
 */
public class EventRuleConfigBB extends BackingBeanUtils implements Serializable{
    
    private PageModeEnum currentMode;
    private List<PageModeEnum> pageModes;
    
    private EventRuleAbstract currentEventRuleAbstract;
    private boolean currentRuleSelected;
    
    private List<EventRuleAbstract> eventRuleList;
    private List<EventRuleAbstract> eventRuleListFiltered;
    
    private List<EventType> eventTypeListAll;
    private List<EventCategory> eventCategoryListAllActive;
    
    private boolean includeEventRuleInCurrentOccPeriodTemplate;
    private int formEventRuleIDToAdd;
    
    // rule sets
    private List<EventRuleSet> eventRuleSetList;
    private EventRuleSet selectedEventRuleSet;
    
    
    /**
     * Creates a new instance of ChoiceProposalConfigBB
     */
    public EventRuleConfigBB() {
    }
    
    /**
     * Bean initializer which builds our rule list and sets up default page modes
     */    
    @PostConstruct
    public void initBean(){
        EventCoordinator ec = getEventCoordinator();
        try {
            eventRuleList = fetchRuleList();
            // start by displaying the entire list
            eventRuleListFiltered = new ArrayList<>();
            if(eventRuleList != null && !eventRuleList.isEmpty()){
                eventRuleListFiltered.addAll(eventRuleList);
            }
            eventTypeListAll = ec.getEventTypesAll();
            eventCategoryListAllActive = ec.assembleEventCategoryListActiveOnly(ec.getEventCategoryList());
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            ex.getMessage(), ""));
        }
        pageModes = getSessionBean().assemblePermittedPageModes();
        currentMode = PageModeEnum.LOOKUP;
        
    }

    /**
     * Getter for currentMode
     * @return 
     */
    public PageModeEnum getCurrentMode() {
        return currentMode;
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
        if (currentMode == null) {
            this.currentMode = tempCurMode;
        } else {
            this.currentMode = mode;
            switch(currentMode){
                case LOOKUP:
                    loadDefaultPageConfig();
                    break;
                case INSERT:
                    rules_initiateEventRuleCreate();
                    eventRuleListFiltered.clear();
                    break;
                case UPDATE:
                    if(currentEventRuleAbstract != null){
                        eventRuleListFiltered.clear();
                        eventRuleListFiltered.add(currentEventRuleAbstract);
                        rules_initiateEventRuleEdit(currentEventRuleAbstract);
                    }
                    break;
                case REMOVE:
//                    eventRuleListFiltered.clear();
                    if(currentEventRuleAbstract != null){
                    }
                    break;
                default:
                    break;
                    
            }
        }
        if(currentMode != null){
            //show the current mode in p:messages box
            getFacesContext().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                this.currentMode.getTitle() + " Mode Selected", ""));
        }

    }

    //check if current mode == Lookup
    public boolean getActiveLookupMode() {
        return PageModeEnum.LOOKUP.equals(currentMode);
    }

    //check if current mode == Insert
    public boolean getActiveInsertUpdateMode() {
        return PageModeEnum.INSERT.equals(currentMode) || PageModeEnum.UPDATE.equals(currentMode);
    }

    //check if current mode == Remove
    public boolean getActiveRemoveMode() {
        return PageModeEnum.REMOVE.equals(currentMode);
    }

    //Select button on side panel can only be used in either Lookup Mode or Update Mode
    public boolean getSelectedButtonActive() {
        return !(PageModeEnum.LOOKUP.equals(currentMode) || PageModeEnum.UPDATE.equals(currentMode));
    }

    /**
     * Initialize the whole page into default setting
     */
    public void loadDefaultPageConfig() {
        System.out.println("EventRuleConfigBB.loadDefaultPageConfig()");
        currentEventRuleAbstract = null;
        currentRuleSelected = false;
        //initialize default current basic muni list
        eventRuleListFiltered.clear();
        try {
            eventRuleListFiltered.addAll(fetchRuleList());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    
    /**
     * Utility method for extracting complete rule list from the Coordinator
     * @return
     * @throws IntegrationException 
     */
    private List<EventRuleAbstract> fetchRuleList() throws IntegrationException{
        WorkflowCoordinator wc = getWorkflowCoordinator();
        return wc.rules_getEventRuleAbstractListForConfig(getSessionBean().getSessUser());
        
    }
    
       /**
     * Listener for clicks to the button on each row of the object table in the 
     * left column
     * @param era
     */
    public void onObjectSelectButtonChange(EventRuleAbstract era){
        System.out.println("EventRuleConfigBB.onObjectSelectButtonChange");
        // "Select" button was selected
        if (currentRuleSelected && era != null) {
            
            eventRuleListFiltered.clear();
            eventRuleListFiltered.add(era);
            currentEventRuleAbstract = era;
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Selected Rule: " + era.getTitle(), ""));
            // "Select" button wasn't selected
            currentRuleSelected = true;
        } else {
            // reset page
            loadDefaultPageConfig();
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Page reset" , ""));
        }

        
    }
    

    /**
     * Listener for button clicks when user is ready to insert a new EventRule.
     * Delegates all work to internal method
     * @return Empty string which prompts page reload without wiping bean memory
     */
    public String onInsertButtonChange() {
        //show successfully inserting message in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully Insert Municipality", ""));
        //reminder: set muniManageSample in faces-config.xml
        //return to muniManage_sample.xhtml page
        rules_commitEventRuleCreate();
        return "";
    }

    /**
     * Listener for user requests to submit object updates;
     * Delegates all work to internal method
     * @return Empty string which prompts page reload without wiping bean memory
     */
    public String onUpdateButtonChange() {
        //show successfully updating message in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully Update Municipality", ""));
        //reminder: set muniManageSample in faces-config.xml
        //return to muniManage_sample.xhtml page
        rules_commitEventRuleEdits();
        return "";
    }

    /**
     * Listener for user requests to remove the currently selected ERA;
     * Delegates all work to internal, non-listener method.
     * @return Empty string which prompts page reload without wiping bean memory
     */
    public String onRemoveButtonChange() {
        //show successfully updating message in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully Remove Municipality", ""));
        //reminder: set muniManageSample in faces-config.xml
        //return to muniManage_sample.xhtml page
        rules_commitEventRuleRemove();
        return "";
    }

 
    /**
     * Internal method to set up a skeleton ERA for creation
     * and is called when the page mode is changed to insert
     */
    private void rules_initiateEventRuleCreate(){
        WorkflowCoordinator wc = getWorkflowCoordinator();
        currentEventRuleAbstract = wc.rules_getInitializedEventRuleAbstract();
    }
    
    
    /**
     * Internal method to setup the page for editing the requested ERA
     * @param era 
     */
    private void rules_initiateEventRuleEdit(EventRuleAbstract era){
        currentEventRuleAbstract = era;
        
    }
    
    
    /**
     * Internal method for finalizing event rule edits
     */
    private void rules_commitEventRuleEdits(){
        WorkflowCoordinator wc = getWorkflowCoordinator();
        try {
            wc.rules_updateEventRuleAbstract(currentEventRuleAbstract);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Update of event rule successful!", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                ex.getMessage(), ""));
        }
    }
    
    /**
     * Internal method for finalizing event rule creation 
     */
    public void rules_commitEventRuleCreate(){
        WorkflowCoordinator wc = getWorkflowCoordinator();
        int freshEventRuleID;
        try {
            freshEventRuleID = wc.rules_createEventRuleAbstract(currentEventRuleAbstract, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "New event rule added with ID " + freshEventRuleID, ""));
            System.out.println("OccInspectionBB.commiteventRuleCreate");
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
        }
    }
    
    /**
     * Internal method for finalizing event rule removal
     */
    public void rules_commitEventRuleRemove(){
        WorkflowCoordinator wc = getWorkflowCoordinator();
        try {
            wc.rules_removeEventRuleAbstract(currentEventRuleAbstract, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Event rule Updated with ID " + currentEventRuleAbstract.getRuleid(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
        }
    }
   

    /**
     * @return the currentEventRuleAbstract
     */
    public EventRuleAbstract getCurrentEventRuleAbstract() {
        return currentEventRuleAbstract;
    }

    /**
     * @param currentEventRuleAbstract the currentEventRuleAbstract to set
     */
    public void setCurrentEventRuleAbstract(EventRuleAbstract currentEventRuleAbstract) {
        this.currentEventRuleAbstract = currentEventRuleAbstract;
    }

    /**
     * @return the eventTypeListAll
     */
    public List<EventType> getEventTypeListAll() {
        return eventTypeListAll;
    }

    /**
     * @return the eventCategoryListAllActive
     */
    public List<EventCategory> getEventCategoryListAllActive() {
        return eventCategoryListAllActive;
    }

    /**
     * @param eventTypeListAll the eventTypeListAll to set
     */
    public void setEventTypeListAll(List<EventType> eventTypeListAll) {
        this.eventTypeListAll = eventTypeListAll;
    }

    /**
     * @param eventCategoryListAllActive the eventCategoryListAllActive to set
     */
    public void setEventCategoryListAllActive(List<EventCategory> eventCategoryListAllActive) {
        this.eventCategoryListAllActive = eventCategoryListAllActive;
    }

    /**
     * @return the eventRuleList
     */
    public List<EventRuleAbstract> getEventRuleList() {
        return eventRuleList;
    }

    /**
     * @param eventRuleList the eventRuleList to set
     */
    public void setEventRuleList(List<EventRuleAbstract> eventRuleList) {
        this.eventRuleList = eventRuleList;
    }


    /**
     * @return the includeEventRuleInCurrentOccPeriodTemplate
     */
    public boolean isIncludeEventRuleInCurrentOccPeriodTemplate() {
        return includeEventRuleInCurrentOccPeriodTemplate;
    }

    /**
     * @param includeEventRuleInCurrentOccPeriodTemplate the includeEventRuleInCurrentOccPeriodTemplate to set
     */
    public void setIncludeEventRuleInCurrentOccPeriodTemplate(boolean includeEventRuleInCurrentOccPeriodTemplate) {
        this.includeEventRuleInCurrentOccPeriodTemplate = includeEventRuleInCurrentOccPeriodTemplate;
    }

    /**
     * @return the formEventRuleIDToAdd
     */
    public int getFormEventRuleIDToAdd() {
        return formEventRuleIDToAdd;
    }

    /**
     * @param formEventRuleIDToAdd the formEventRuleIDToAdd to set
     */
    public void setFormEventRuleIDToAdd(int formEventRuleIDToAdd) {
        this.formEventRuleIDToAdd = formEventRuleIDToAdd;
    }

    /**
     * @return the eventRuleSetList
     */
    public List<EventRuleSet> getEventRuleSetList() {
        return eventRuleSetList;
    }

    /**
     * @return the selectedEventRuleSet
     */
    public EventRuleSet getSelectedEventRuleSet() {
        return selectedEventRuleSet;
    }

    /**
     * @param eventRuleSetList the eventRuleSetList to set
     */
    public void setEventRuleSetList(List<EventRuleSet> eventRuleSetList) {
        this.eventRuleSetList = eventRuleSetList;
    }

    /**
     * @param selectedEventRuleSet the selectedEventRuleSet to set
     */
    public void setSelectedEventRuleSet(EventRuleSet selectedEventRuleSet) {
        this.selectedEventRuleSet = selectedEventRuleSet;
    }

 
    /**
     * @return the currentRuleSelected
     */
    public boolean isCurrentRuleSelected() {
        
        return currentRuleSelected;
    }

    
    /**
     * @return the eventRuleListFiltered
     */
    public List<EventRuleAbstract> getEventRuleListFiltered() {
        return eventRuleListFiltered;
    }

    /**
     * @param eventRuleListFiltered the eventRuleListFiltered to set
     */
    public void setEventRuleListFiltered(List<EventRuleAbstract> eventRuleListFiltered) {
        this.eventRuleListFiltered = eventRuleListFiltered;
    }

    /**
     * @return the pageModes
     */
    public List<PageModeEnum> getPageModes() {
        return pageModes;
    }

    /**
     * @param pageModes the pageModes to set
     */
    public void setPageModes(List<PageModeEnum> pageModes) {
        this.pageModes = pageModes;
    }

    /**
     * @param currentRuleSelected the currentRuleSelected to set
     */
    public void setCurrentRuleSelected(boolean currentRuleSelected) {
        this.currentRuleSelected = currentRuleSelected;
    }

  
    
}
