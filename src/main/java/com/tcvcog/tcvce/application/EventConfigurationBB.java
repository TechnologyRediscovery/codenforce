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
import com.tcvcog.tcvce.integration.EventIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;


/**
 *
 * @author Eric C. Darsow
 */
public class EventConfigurationBB extends BackingBeanUtils implements Serializable{

    private EventCategory selectedEventCategory;
    private ArrayList<EventCategory> eventCategoryList;
     
    private EventType[] eventTypeList;
    
    private EventType formEventType;
    private String formEventCategoryTitle;
    private String formEventCategoryDescr;
    
    private boolean formUserdeployable;
    private boolean formMunideployable;
    private boolean formPublicdeployable;
    private boolean formRequiresviewconfirmation;
    private boolean formNotifycasemonitors;
    private boolean formCasephasechangetrigger;
    private boolean formHidable;
    
    private EventType newFormSelectedEventType;
    private String newFormEventCategoryTitle;
    private String newFormEventCategoryDescr;
    
    private boolean newFormUserdeployable;
    private boolean newFormMunideployable;
    private boolean newFormPublicdeployable;
    private boolean newFormRequiresviewconfirmation;
    private boolean newFormNotifycasemonitors;
    private boolean newFormCasephasechangetrigger;
    private boolean newFormHidable;
    
    public EventConfigurationBB() {
    }
    
  
    
    public void editSelectedEventCategory(ActionEvent e){
        if(getSelectedEventCategory() != null){
            setFormEventType(getSelectedEventCategory().getEventType());
            setFormEventCategoryTitle(getSelectedEventCategory().getEventCategoryTitle());
            setFormEventCategoryDescr(getSelectedEventCategory().getEventCategoryDesc());
            
            setFormUserdeployable(selectedEventCategory.isUserdeployable());
            setFormMunideployable(selectedEventCategory.isMunideployable());
            setFormPublicdeployable(selectedEventCategory.isPublicdeployable());
            setFormRequiresviewconfirmation(selectedEventCategory.isRequiresviewconfirmation());
            setFormNotifycasemonitors(selectedEventCategory.isNotifycasemonitors());
            setFormHidable(selectedEventCategory.isHidable());
            
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
       ec.setEventType(getFormEventType());
       ec.setEventCategoryTitle(getFormEventCategoryTitle());
       ec.setEventCategoryDesc(getFormEventCategoryDescr());
       
        ec.setUserdeployable(formUserdeployable);
        ec.setMunideployable(formMunideployable);
        ec.setPublicdeployable(formPublicdeployable);
        ec.setRequiresviewconfirmation(formRequiresviewconfirmation);
        ec.setNotifycasemonitors(formNotifycasemonitors);
        ec.setHidable(formHidable);
        
        try {
            ei.updateEventCategory(ec);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Event category updated!", ""));
            setFormEventCategoryTitle("");
            setFormEventCategoryDescr("");
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
        
        ec.setEventType(getNewFormSelectedEventType());
        ec.setEventCategoryTitle(getNewFormEventCategoryTitle());
        ec.setEventCategoryDesc(getNewFormEventCategoryDescr());
        
        ec.setUserdeployable(newFormUserdeployable);
        ec.setMunideployable(newFormMunideployable);
        ec.setPublicdeployable(newFormCasephasechangetrigger);
        ec.setRequiresviewconfirmation(newFormRequiresviewconfirmation);
        ec.setNotifycasemonitors(newFormNotifycasemonitors);
        ec.setHidable(newFormHidable);
        
        try {
            ei.insertEventCategory(ec);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Event category added!", ""));
            setNewFormEventCategoryTitle("");
            setNewFormEventCategoryDescr("");
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
    public ArrayList<EventCategory> getEventCategoryList() {
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
        if(eventCategoryList != null){
            return eventCategoryList;
            
        } else {
            eventCategoryList = new ArrayList();
            return eventCategoryList;
        }
        
    }

    /**
     * @return the eventTypeList
     */
    public EventType[] getEventTypeList() {
        eventTypeList = EventType.values();
        
        return eventTypeList;
    }

    /**
     * @return the formEventType
     */
    public EventType getFormEventType() {
        return formEventType;
    }

    /**
     * @return the formEventCategoryTitle
     */
    public String getFormEventCategoryTitle() {
        return formEventCategoryTitle;
    }

    /**
     * @return the formEventCategoryDescr
     */
    public String getFormEventCategoryDescr() {
        return formEventCategoryDescr;
    }

    /**
     * @param selectedEventCategory the selectedEventCategory to set
     */
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
        this.setEventTypeList(eventTypeList);
    }

    /**
     * @param formEventType the formEventType to set
     */
    public void setFormEventType(EventType formEventType) {
        this.formEventType = formEventType;
    }

    /**
     * @param formEventCategoryTitle the formEventCategoryTitle to set
     */
    public void setFormEventCategoryTitle(String formEventCategoryTitle) {
        this.formEventCategoryTitle = formEventCategoryTitle;
    }

    /**
     * @param formEventCategoryDescr the formEventCategoryDescr to set
     */
    public void setFormEventCategoryDescr(String formEventCategoryDescr) {
        this.formEventCategoryDescr = formEventCategoryDescr;
    }

    /**
     * @return the newFormSelectedEventType
     */
    public EventType getNewFormSelectedEventType() {
        return newFormSelectedEventType;
    }

    /**
     * @return the newFormEventCategoryTitle
     */
    public String getNewFormEventCategoryTitle() {
        return newFormEventCategoryTitle;
    }

    /**
     * @return the newFormEventCategoryDescr
     */
    public String getNewFormEventCategoryDescr() {
        return newFormEventCategoryDescr;
    }

    /**
     * @param newFormSelectedEventType the newFormSelectedEventType to set
     */
    public void setNewFormSelectedEventType(EventType newFormSelectedEventType) {
        this.newFormSelectedEventType = newFormSelectedEventType;
    }

    /**
     * @param newFormEventCategoryTitle the newFormEventCategoryTitle to set
     */
    public void setNewFormEventCategoryTitle(String newFormEventCategoryTitle) {
        this.newFormEventCategoryTitle = newFormEventCategoryTitle;
    }

    /**
     * @param newFormEventCategoryDescr the newFormEventCategoryDescr to set
     */
    public void setNewFormEventCategoryDescr(String newFormEventCategoryDescr) {
        this.newFormEventCategoryDescr = newFormEventCategoryDescr;
    }

    /**
     * @return the formUserdeployable
     */
    public boolean isFormUserdeployable() {
        return formUserdeployable;
    }

    /**
     * @return the formMunideployable
     */
    public boolean isFormMunideployable() {
        return formMunideployable;
    }

    /**
     * @return the formPublicdeployable
     */
    public boolean isFormPublicdeployable() {
        return formPublicdeployable;
    }

    /**
     * @return the formRequiresviewconfirmation
     */
    public boolean isFormRequiresviewconfirmation() {
        return formRequiresviewconfirmation;
    }

    /**
     * @return the formNotifycasemonitors
     */
    public boolean isFormNotifycasemonitors() {
        return formNotifycasemonitors;
    }

    /**
     * @return the formCasephasechangetrigger
     */
    public boolean isFormCasephasechangetrigger() {
        return formCasephasechangetrigger;
    }

    /**
     * @return the formHidable
     */
    public boolean isFormHidable() {
        return formHidable;
    }

    /**
     * @return the newFormUserdeployable
     */
    public boolean isNewFormUserdeployable() {
        return newFormUserdeployable;
    }

    /**
     * @return the newFormMunideployable
     */
    public boolean isNewFormMunideployable() {
        return newFormMunideployable;
    }

    /**
     * @return the newFormPublicdeployable
     */
    public boolean isNewFormPublicdeployable() {
        return newFormPublicdeployable;
    }

    /**
     * @return the newFormRequiresviewconfirmation
     */
    public boolean isNewFormRequiresviewconfirmation() {
        return newFormRequiresviewconfirmation;
    }

    /**
     * @return the newFormNotifycasemonitors
     */
    public boolean isNewFormNotifycasemonitors() {
        return newFormNotifycasemonitors;
    }

    /**
     * @return the newFormCasephasechangetrigger
     */
    public boolean isNewFormCasephasechangetrigger() {
        return newFormCasephasechangetrigger;
    }

    /**
     * @return the newFormHidable
     */
    public boolean isNewFormHidable() {
        return newFormHidable;
    }


    /**
     * @param formUserdeployable the formUserdeployable to set
     */
    public void setFormUserdeployable(boolean formUserdeployable) {
        this.formUserdeployable = formUserdeployable;
    }

    /**
     * @param formMunideployable the formMunideployable to set
     */
    public void setFormMunideployable(boolean formMunideployable) {
        this.formMunideployable = formMunideployable;
    }

    /**
     * @param formPublicdeployable the formPublicdeployable to set
     */
    public void setFormPublicdeployable(boolean formPublicdeployable) {
        this.formPublicdeployable = formPublicdeployable;
    }

    /**
     * @param formRequiresviewconfirmation the formRequiresviewconfirmation to set
     */
    public void setFormRequiresviewconfirmation(boolean formRequiresviewconfirmation) {
        this.formRequiresviewconfirmation = formRequiresviewconfirmation;
    }

    /**
     * @param formNotifycasemonitors the formNotifycasemonitors to set
     */
    public void setFormNotifycasemonitors(boolean formNotifycasemonitors) {
        this.formNotifycasemonitors = formNotifycasemonitors;
    }

    /**
     * @param formCasephasechangetrigger the formCasephasechangetrigger to set
     */
    public void setFormCasephasechangetrigger(boolean formCasephasechangetrigger) {
        this.formCasephasechangetrigger = formCasephasechangetrigger;
    }

    /**
     * @param formHidable the formHidable to set
     */
    public void setFormHidable(boolean formHidable) {
        this.formHidable = formHidable;
    }

    /**
     * @param newFormUserdeployable the newFormUserdeployable to set
     */
    public void setNewFormUserdeployable(boolean newFormUserdeployable) {
        this.newFormUserdeployable = newFormUserdeployable;
    }

    /**
     * @param newFormMunideployable the newFormMunideployable to set
     */
    public void setNewFormMunideployable(boolean newFormMunideployable) {
        this.newFormMunideployable = newFormMunideployable;
    }

    /**
     * @param newFormPublicdeployable the newFormPublicdeployable to set
     */
    public void setNewFormPublicdeployable(boolean newFormPublicdeployable) {
        this.newFormPublicdeployable = newFormPublicdeployable;
    }

    /**
     * @param newFormRequiresviewconfirmation the newFormRequiresviewconfirmation to set
     */
    public void setNewFormRequiresviewconfirmation(boolean newFormRequiresviewconfirmation) {
        this.newFormRequiresviewconfirmation = newFormRequiresviewconfirmation;
    }

    /**
     * @param newFormNotifycasemonitors the newFormNotifycasemonitors to set
     */
    public void setNewFormNotifycasemonitors(boolean newFormNotifycasemonitors) {
        this.newFormNotifycasemonitors = newFormNotifycasemonitors;
    }

    /**
     * @param newFormCasephasechangetrigger the newFormCasephasechangetrigger to set
     */
    public void setNewFormCasephasechangetrigger(boolean newFormCasephasechangetrigger) {
        this.newFormCasephasechangetrigger = newFormCasephasechangetrigger;
    }

    /**
     * @param newFormHidable the newFormHidable to set
     */
    public void setNewFormHidable(boolean newFormHidable) {
        this.newFormHidable = newFormHidable;
    }
    
}
