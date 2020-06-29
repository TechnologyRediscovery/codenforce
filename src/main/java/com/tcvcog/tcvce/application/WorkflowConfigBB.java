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
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.WorkflowCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventRuleAbstract;
import com.tcvcog.tcvce.entities.EventRuleSet;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import java.io.Serializable;
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
public class WorkflowConfigBB extends BackingBeanUtils implements Serializable{
    
    
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
    public WorkflowConfigBB() {
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
    
    public void rules_commitEventRuleCreate(ActionEvent ev){
        WorkflowCoordinator wc = getWorkflowCoordinator();
        int freshEventRuleID;
        try {
            freshEventRuleID = wc.rules_createEventRuleAbstract(currentEventRuleAbstract, getCurrentOccPeriod(), 
                                                                null, isIncludeEventRuleInCurrentOccPeriodTemplate(),
                                                                getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "New event rule added with ID " + freshEventRuleID, ""));
            System.out.println("OccInspectionBB.commiteventRuleCreate");
            reloadCurrentOccPeriodDataHeavy();
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
        }
    }
    
    
    public void rules_addEventRuleByID(ActionEvent ev){
        WorkflowCoordinator wc = getWorkflowCoordinator();
        try {
            EventRuleAbstract era = wc.rules_getEventRuleAbstract(getFormEventRuleIDToAdd());
            wc.rules_attachEventRule(era, getCurrentOccPeriod(), getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Success! added rule to occ period", ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
            
        } 
    }
    
    public void rules_addEventRuleSet(EventRuleSet ers){
        WorkflowCoordinator wc = getWorkflowCoordinator();
        try {
            wc.rules_attachRuleSet(ers, getCurrentOccPeriod(), getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Success! added rule set to occ period", ""));
        } catch (IntegrationException | BObStatusException ex) {
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
    
}
