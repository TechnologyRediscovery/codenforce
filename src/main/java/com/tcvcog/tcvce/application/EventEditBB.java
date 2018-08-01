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
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.EventCase;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric Darsow
 */
public class EventEditBB extends BackingBeanUtils implements Serializable {
    
    // add event form fields
    private ArrayList<EventCategory> eventCategoryList;
    
  
    private EventCase event;
    
    private String formEventDesc;
    private Date formEventDate;
    private boolean formDiscloseToMuni;
    private boolean formDiscloseToPublic;
    private boolean activeEvent;
    private String formEventNotes;
    private boolean formRequireViewConfirmation;
    
    private ArrayList<Person> propertyPersonList;
    private ArrayList<Person> formSelectedPersons;
    
    
    
    // constructor
    public EventEditBB(){
        
    }
    

    public String editEvent(){
        EventCoordinator ec = getEventCoordinator();
        
        EventCase e = getSessionBean().getActiveEvent();
        
        // category is already set from initialization sequence
        e.setCaseID(getSessionBean().getActiveCase().getCaseID());
        e.setEventDescription(formEventDesc);
        e.setActiveEvent(activeEvent);
        e.setEventOwnerUser(getFacesUser());
        e.setDateOfRecord(formEventDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        e.setDiscloseToMunicipality(formDiscloseToMuni);
        e.setDiscloseToPublic(formDiscloseToPublic);
        e.setRequiresViewConfirmation(formRequireViewConfirmation);
        e.setNotes(formEventDesc);
        e.setEventPersons(propertyPersonList);
        
        // now check for persons to connect
        
        try {
            ec.updateEvent(e);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Successfully updated event.", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            ex.getMessage(), 
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
        } 
        
        
        
        return "caseProfile";
    }
    
    
    /**
     * @return the formEventDesc
     */
    public String getFormEventDesc() {
        return formEventDesc;
    }

    /**
     * @return the formEventDate
     */
    public Date getFormEventDate() {
        LocalDateTime current = LocalDateTime.now();
        formEventDate = java.util.Date.from(current.atZone(ZoneId.systemDefault()).toInstant());
        
        return formEventDate;
    }

    /**
     * @return the eventType
     */
   

    /**
     * @return the formDiscloseToMuni
     */
    public boolean isFormDiscloseToMuni() {
        return formDiscloseToMuni;
    }

    /**
     * @return the formDiscloseToPublic
     */
    public boolean isFormDiscloseToPublic() {
        return formDiscloseToPublic;
    }

    /**
     * @return the activeEvent
     */
    public boolean isActiveEvent() {
        return activeEvent;
    }

    /**
     * @return the formEventNotes
     */
    public String getFormEventNotes() {
        return formEventNotes;
    }

    /**
     * @return the propertyPersonList
     */
    public ArrayList<Person> getPropertyPersonList() {
        PersonIntegrator pi = getPersonIntegrator();
        
        
        try {
            propertyPersonList = pi.getPersonList(getSessionBean().getActiveProp());
        } catch (IntegrationException ex) {
            // do nothing
        }
        return propertyPersonList;
    }

    /**
     * @param formEventDesc the formEventDesc to set
     */
    public void setFormEventDesc(String formEventDesc) {
        this.formEventDesc = formEventDesc;
    }

    /**
     * @param formEventDate the formEventDate to set
     */
    public void setFormEventDate(Date formEventDate) {
        this.formEventDate = formEventDate;
    }

    
    /**
     * @param formDiscloseToMuni the formDiscloseToMuni to set
     */
    public void setFormDiscloseToMuni(boolean formDiscloseToMuni) {
        this.formDiscloseToMuni = formDiscloseToMuni;
    }

    /**
     * @param formDiscloseToPublic the formDiscloseToPublic to set
     */
    public void setFormDiscloseToPublic(boolean formDiscloseToPublic) {
        this.formDiscloseToPublic = formDiscloseToPublic;
    }

    /**
     * @param activeEvent the activeEvent to set
     */
    public void setActiveEvent(boolean activeEvent) {
        this.activeEvent = activeEvent;
    }

    /**
     * @param formEventNotes the formEventNotes to set
     */
    public void setFormEventNotes(String formEventNotes) {
        this.formEventNotes = formEventNotes;
    }

    /**
     * @param propertyPersonList the propertyPersonList to set
     */
    public void setPropertyPersonList(ArrayList<Person> propertyPersonList) {
        this.propertyPersonList = propertyPersonList;
    }

   

    /**
     * @return the formSelectedPersons
     */
    public ArrayList<Person> getFormSelectedPersons() {
        return formSelectedPersons;
    }

    /**
     * @param formSelectedPersons the formSelectedPersons to set
     */
    public void setFormSelectedPersons(ArrayList<Person> formSelectedPersons) {
        this.formSelectedPersons = formSelectedPersons;
    }

    /**
     * @return the event
     */
    public EventCase getEvent() {
        
        EventCase currentEvent = getSessionBean().getActiveEvent();
        event = currentEvent;
        return event;
    }

    /**
     * @param event the event to set
     */
    public void setEvent(EventCase event) {
        this.event = event;
    }

  
   

    /**
     * @return the formRequireViewConfirmation
     */
    public boolean isFormRequireViewConfirmation() {
        return formRequireViewConfirmation;
    }

    /**
     * @param formRequireViewConfirmation the formRequireViewConfirmation to set
     */
    public void setFormRequireViewConfirmation(boolean formRequireViewConfirmation) {
        this.formRequireViewConfirmation = formRequireViewConfirmation;
    }

   
    
    
    
}
