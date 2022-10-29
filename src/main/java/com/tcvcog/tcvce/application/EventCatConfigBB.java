/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Icon;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.event.ActionEvent;


/**
 *
 * @author ellen bascomb of apt 31y
 */
public class EventCatConfigBB extends BackingBeanUtils implements Serializable{

    private EventCategory currentEventCategory;
    private List<EventCategory> eventCategoryList;
    private List<Icon> iconList;
    private boolean editModeEventCat; 
    private List<EventType> eventTypeList;
    
    
    public EventCatConfigBB() {
    }
    
    @PostConstruct
    public void initBean(){
        SystemIntegrator si = getSystemIntegrator();
        eventTypeList = Arrays.asList(EventType.values());
                
        refreshCurrentEventCatAndList();
        
        
        try {
            iconList = si.getIconList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    /**
     * Edit mode toggler for event categories
     * @param ev 
     */
    public void onToggleEventCatEditMode(ActionEvent ev){
        System.out.println("EventCatConfigBB.onToggleEventCatEditMode | incoming value: " + editModeEventCat);
        if(editModeEventCat){
            if(currentEventCategory != null){
                if(currentEventCategory.getCategoryID() == 0){
                    addNewEventCategoryCommit();
                } else {
                    editEventCategoryCommit();
                }
            } else {
                   getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Event Category page configuration error EC1: currentEventCategory null", 
                        ""));
            }
        } else {
            // nothing to do--toggle on edit mode below
        }
        
        editModeEventCat = !editModeEventCat;
        
    }
    
    /**
     * Listener for user requests to cancel the operation
     * @param ev 
     */
    public void onEventCatEditAbort(ActionEvent ev){
        editModeEventCat = false;
    }
    
    /**
     * Entry point for viewing an event category
     * @param cat 
     */
    public void viewEventCategory(EventCategory cat){
        currentEventCategory = cat;
    }
    
    /**
     * Starting point for editing an event category
     * @param cat 
     */
    public void editEventCategoryInit(EventCategory cat){
        System.out.println("EventCatConfigBB.editInit on catID "+ cat.getCategoryID());
        currentEventCategory = cat;
        editModeEventCat = true;
        
    }
    
    /**
     * INternal organ for sending updates to Coordinator
     */
    private void editEventCategoryCommit(){
       EventCoordinator ec = getEventCoordinator();
        
        try {
            ec.updateEventCategory(currentEventCategory);
            refreshCurrentEventCatAndList();
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Event category updated!", ""));
        } catch (IntegrationException ex) {
           getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to update Event Category in database, sorry.", 
                        "This must be corrected by the System Administrator"));
        }
        
    }
    
    
    
    /**
     * Starts the process of making a new event category
     * @param ev 
     */
    public void onAddEventCategoryInit(ActionEvent ev){
        EventCoordinator ec = getEventCoordinator();
        currentEventCategory = ec.getEventCategorySkeleton();
        editModeEventCat = true;
    }
    
    /**
     * Asks coordinator for fresh current and list
     */
    private void refreshCurrentEventCatAndList(){
        EventCoordinator ec = getEventCoordinator();
        try {
            if(currentEventCategory !=null && currentEventCategory.getCategoryID() != 0){
                    currentEventCategory = ec.getEventCategory(currentEventCategory.getCategoryID());
            }
            eventCategoryList = ec.getEventCategoryList();
            
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
    }
    
    private void addNewEventCategoryCommit(){
        EventCoordinator ec = getEventCoordinator();
        
        int freshID = 0;
        try {
            if(currentEventCategory != null){
                
                freshID = ec.addEventCategory(currentEventCategory);
                currentEventCategory.setCategoryID(freshID);
                refreshCurrentEventCatAndList();
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Event category added!", ""));
            }
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to add new Event Category to database, sorry.", 
                        "This must be corrected by the System Administrator"));
        }
    }
    
    
    
    

//  ******************  GETTERS AND SETTERS  ******************************
    
    /**
     * @return the currentEventCategory
     */
    public EventCategory getCurrentEventCategory() {
        return currentEventCategory;
    }

    /**
     * @return the eventCategoryList
     */
    public List<EventCategory> getEventCategoryList() {
        return eventCategoryList;
        
    }

    /**
     * @return the eventTypeList
     */
    public List<EventType> getEventTypeList() {
        
        
        return eventTypeList;
    }

 
    public void setCurrentEventCategory(EventCategory currentEventCategory) {
        this.currentEventCategory = currentEventCategory;
    }

    /**
     * @param eventCategoryList the eventCategoryList to set
     */
    public void setEventCategoryList(ArrayList<EventCategory> eventCategoryList) {
        
        
        this.eventCategoryList = eventCategoryList;
    }

    /**
     * @param eventTypeList the eventTypeList to set
     */
    public void setEventTypeList(List<EventType> eventTypeList) {
        this.eventTypeList = eventTypeList;
    }

   
    /**
     * @return the iconList
     */
    public List<Icon> getIconList() {
        return iconList;
    }

    /**
     * @param iconList the iconList to set
     */
    public void setIconList(List<Icon> iconList) {
        this.iconList = iconList;
    }

    /**
     * @return the editModeEventCat
     */
    public boolean isEditModeEventCat() {
        return editModeEventCat;
    }

    /**
     * @param editModeEventCat the editModeEventCat to set
     */
    public void setEditModeEventCat(boolean editModeEventCat) {
        this.editModeEventCat = editModeEventCat;
    }
    
}
