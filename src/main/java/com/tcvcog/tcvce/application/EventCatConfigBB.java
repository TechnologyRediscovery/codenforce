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

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Icon;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;


/**
 *
 * @author ellen bascomb of apt 31y
 */
public class EventCatConfigBB extends BackingBeanUtils implements Serializable{

    private EventCategory selectedEventCategory;
    private List<EventCategory> eventCategoryList;
    private List<Icon> iconList;
     
    private EventType[] eventTypeList;
    
    
    public EventCatConfigBB() {
    }
    
    @PostConstruct
    public void initBean(){
        
        if(eventCategoryList == null){
            try {
                EventIntegrator ei = getEventIntegrator();
                eventCategoryList = ei.getEventCategoryList();
                //return eventCategoryList;
            } catch (IntegrationException ex) {
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to load event category list", 
                            "This must be corrected by the System Administrator"));
            }
            
        } 
        
        SystemIntegrator si = getSystemIntegrator();
        try {
            iconList = si.getIconList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
  
    
    public void editSelectedEventCategory(ActionEvent e){
        if(getSelectedEventCategory() != null){
            
        } else {
           getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Please select an event category to update", ""));
        }
        
    }
    
    public void commitUpdates(ActionEvent e){
       EventIntegrator ei = getEventIntegrator();
       EventCategory ec = new EventCategory();
       
       ec.setCategoryID(getSelectedEventCategory().getCategoryID());
        
        try {
            ei.updateEventCategory(ec);
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
    
    public void addNewEventCategory(ActionEvent e){
        EventIntegrator ei = getEventIntegrator();
        EventCategory ec = new EventCategory();
        
        
        try {
            ei.insertEventCategory(ec);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Event category added!", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to add new Event Category to database, sorry.", 
                        "This must be corrected by the System Administrator"));
        }
        
    }
    
    public void deleteSelectedEventCategory(ActionEvent e){
        EventIntegrator ei = getEventIntegrator();
        if(getSelectedEventCategory() != null){
            try {
                ei.deleteEventCategory(getSelectedEventCategory());
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Event category deleted forever!", ""));
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to delete the category--probably because it is used "
                                    + "somewhere in the database. Sorry.", 
                            "This category will always be with us."));
            }
            
        } else {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Please select a category from the table to delete", ""));
        }
    }

    /**
     * @return the selectedEventCategory
     */
    public EventCategory getSelectedEventCategory() {
        return selectedEventCategory;
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
    public EventType[] getEventTypeList() {
        eventTypeList = EventType.values();
        
        return eventTypeList;
    }

 
    public void setSelectedEventCategory(EventCategory selectedEventCategory) {
        this.selectedEventCategory = selectedEventCategory;
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
    public void setEventTypeList(EventType[] eventTypeList) {
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
    
}
