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
import com.tcvcog.tcvce.coordinators.MunicipalityCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.WorkflowCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.Directive;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventRuleAbstract;
import com.tcvcog.tcvce.entities.EventRuleSet;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 * BackingBean to serve the faces page for configuring Directives and their Choices
 * 
 * @author sylvia
 */
public class DirectiveChoiceConfigBB extends BackingBeanUtils implements Serializable{
    
    
    // New from Chen & Chen
    private String currentMode;
    
    private boolean currentDirectiveSelected;
    private List<Directive> directiveList;
    private Directive currentDirective;
    
    private OccPeriodDataHeavy currentOccPeriod;
    private EventRuleAbstract currentEventRuleAbstract;
    private List<EventRuleAbstract> eventRuleList;
    
    private List<EventType> eventTypeListAll;
    private List<EventCategory> eventCategoryListAllActive;
    
    private boolean includeEventRuleInCurrentOccPeriodTemplate;
    private int formEventRuleIDToAdd;
    
    // rules
    private List<EventRuleSet> eventRuleSetList;
    private EventRuleSet selectedEventRuleSet;
    
    
    /**
     * Creates a new instance of ChoiceProposalConfigBB
     */
    public DirectiveChoiceConfigBB() {
    }
    
        
    @PostConstruct
    public void initBean(){
        EventCoordinator ec = getEventCoordinator();
        WorkflowCoordinator wc = getWorkflowCoordinator();
        eventTypeListAll = ec.getEventTypesAll();
        eventCategoryListAllActive = ec.assembleEventCategoryListActiveOnly(ec.getEventCategoryList());
        
        try {
            setEventRuleSetList(wc.rules_getEventRuleSetList());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }

        // new from Chen & Chen
         //initialize default current mode : Lookup
        currentMode = "Lookup";
        //initialize default setting
        defaultSetting();

        
    }
    

    public String getCurrentMode() {
        return currentMode;
    }

    /**
     *
     * @param currentMode Lookup, Insert, Update, Remove
     * @throws IntegrationException
     * @throws AuthorizationException
     */
    public void setCurrentMode(String currentMode) throws IntegrationException, AuthorizationException {

        //store currentMode into tempCurMode as a temporary value, in case the currenMode equal null
        String tempCurMode = this.currentMode;
        //reset default setting every time the Mode has been selected 
        defaultSetting();
        //check the currentMode == null or not
        if (currentMode == null) {
            this.currentMode = tempCurMode;
        } else {
            this.currentMode = currentMode;
        }
        //show the current mode in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, this.currentMode + " Mode Selected", ""));

    }

    //check if current mode == Lookup
    public boolean getActiveLookupMode() {
        return "Lookup".equals(currentMode);
    }

    //check if current mode == Insert
    public boolean getActiveInsertMode() {
        return "Insert".equals(currentMode);
    }

    //check if current mode == Update
    public boolean getActiveUpdateMode() {
        return "Update".equals(currentMode);
    }

    //check if current mode == Remove
    public boolean getActiveRemoveMode() {
        return "Remove".equals(currentMode);
    }

    //Select button on side panel can only be used in either Lookup Mode or Update Mode
    public boolean getSelectedButtonActive() {
        return !("Lookup".equals(currentMode) || "Update".equals(currentMode));
    }

    /**
     * Initialize the whole page into default setting
     */
    public void defaultSetting() {
        //initialize default selecte button in list-column: false
        currentDirectiveSelected = false;
        //initialize default current basic muni list
        directiveList = fetchDirectiveList();
    }
    
    
    private List<Directive> fetchDirectiveList(){
        WorkflowCoordinator wc = getWorkflowCoordinator();
        List<Directive> dlist = new ArrayList<>();
        try {
            wc.getDirectiveListForConfig(getSessionBean().getSessUser());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return dlist;
        
    }

    public String onInsertButtonChange() {
        //show successfully inserting message in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully Insert Municipality", ""));
        //reminder: set muniManageSample in faces-config.xml
        //return to muniManage_sample.xhtml page
        return "muniManageSample";
    }

    public String onUpdateButtonChange() {
        //show successfully updating message in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully Update Municipality", ""));
        //reminder: set muniManageSample in faces-config.xml
        //return to muniManage_sample.xhtml page
        return "muniManageSample";
    }

    public String onRemoveButtonChange() {
        //show successfully updating message in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully Remove Municipality", ""));
        //reminder: set muniManageSample in faces-config.xml
        //return to muniManage_sample.xhtml page
        return "muniManageSample";
    }

    /**
     * Listener for clicks to the button on each row of the object table in the 
     * left column
     * @param dir 
     */
    public void onObjectSelectButtonChange(Directive dir){
        
          MunicipalityCoordinator mc = getMuniCoordinator();

        // "Select" button was selected
        if (currentDirectiveSelected == true) {
            directiveList = new ArrayList<>();
            directiveList.add(dir);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Selected Directive: " + dir.getTitle(), ""));
            // "Select" button wasn't selected
        } else {
            // reset page
            defaultSetting();
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Page reset" , ""));
        }

        
    }
    
    public void reloadCurrentOccPeriodDataHeavy(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            setCurrentOccPeriod(oc.assembleOccPeriodDataHeavy(getCurrentOccPeriod(), getSessionBean().getSessUser().getMyCredential()));
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Reloaded occ period ID " + getCurrentOccPeriod().getPeriodID(), ""));
        } catch (IntegrationException | BObStatusException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Unable to reload occ period", ""));
        }
        
    }
      
    public void rules_initiateEventRuleCreate(ActionEvent ev){
        WorkflowCoordinator wc = getWorkflowCoordinator();
        currentEventRuleAbstract = wc.rules_getInitializedEventRuleAbstract();
    }
    
    public void rules_initiateEventRuleEdit(EventRuleAbstract era){
        currentEventRuleAbstract = era;
        
    }
    
    public void rules_commitEventRuleEdits(ActionEvent ev){
        WorkflowCoordinator wc = getWorkflowCoordinator();
        try {
            wc.rules_updateEventRuleAbstract(currentEventRuleAbstract);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Update of event rule successful!", ""));
            reloadCurrentOccPeriodDataHeavy();
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
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
     * @return the currentOccPeriod
     */
    public OccPeriodDataHeavy getCurrentOccPeriod() {
        return currentOccPeriod;
    }

    /**
     * @param currentOccPeriod the currentOccPeriod to set
     */
    public void setCurrentOccPeriod(OccPeriodDataHeavy currentOccPeriod) {
        this.currentOccPeriod = currentOccPeriod;
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
     * @return the currentDirectiveSelected
     */
    public boolean isCurrentDirectiveSelected() {
        return currentDirectiveSelected;
    }

    /**
     * @return the directiveList
     */
    public List<Directive> getDirectiveList() {
        return directiveList;
    }

    /**
     * @param currentDirectiveSelected the currentDirectiveSelected to set
     */
    public void setCurrentDirectiveSelected(boolean currentDirectiveSelected) {
        this.currentDirectiveSelected = currentDirectiveSelected;
    }

    /**
     * @param directiveList the directiveList to set
     */
    public void setDirectiveList(List<Directive> directiveList) {
        this.directiveList = directiveList;
    }

    /**
     * @return the currentDirective
     */
    public Directive getCurrentDirective() {
        return currentDirective;
    }

    /**
     * @param currentDirective the currentDirective to set
     */
    public void setCurrentDirective(Directive currentDirective) {
        this.currentDirective = currentDirective;
    }
    
}
