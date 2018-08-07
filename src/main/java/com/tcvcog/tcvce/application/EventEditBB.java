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
import com.tcvcog.tcvce.entities.EventCase;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric Darsow
 */
public class EventEditBB extends BackingBeanUtils implements Serializable {
    
    // add currentEvent form fields
    private EventCase currentEvent;
    
    private String formEventDesc;
    private Date formEventDate;
    private boolean formDiscloseToMuni;
    private boolean formDiscloseToPublic;
    private boolean activeEvent;
    private String formEventNotes;
    private boolean formRequireViewConfirmation;
    private boolean formClearExistingViewConfirmation;
    
    private ArrayList<Person> propertyPersonList;
    private ArrayList<Person> formSelectedPersons;
    
    private boolean disableClearViewConfirmation;
    
    
    
    // constructor
    public EventEditBB(){
        
    }
    

    public String editEvent(){
        EventCoordinator ec = getEventCoordinator();
        
        EventCase e = currentEvent;
        
        // category is already set from initialization sequence
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
            ec.updateEvent(e, formClearExistingViewConfirmation);
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
        formEventDesc = currentEvent.getEventDescription();
        return formEventDesc;
    }

    /**
     * @return the formEventDate
     */
    public Date getFormEventDate() {
        formEventDate = java.util.Date.from(currentEvent.getDateOfRecord().atZone(ZoneId.systemDefault()).toInstant());
        return formEventDate;
    }

    /**
     * @return the eventType
     */
   

    /**
     * @return the formDiscloseToMuni
     */
    public boolean isFormDiscloseToMuni() {
        formDiscloseToMuni = currentEvent.isDiscloseToMunicipality();
        return formDiscloseToMuni;
    }

    /**
     * @return the formDiscloseToPublic
     */
    public boolean isFormDiscloseToPublic() {
        formDiscloseToPublic = currentEvent.isDiscloseToPublic();
        return formDiscloseToPublic;
    }

    /**
     * @return the activeEvent
     */
    public boolean isActiveEvent() {
        activeEvent = currentEvent.isActiveEvent();
        return activeEvent;
    }

    /**
     * @return the formEventNotes
     */
    public String getFormEventNotes() {
        formEventNotes = currentEvent.getNotes();
        return formEventNotes;
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
     * @return the currentEvent
     */
    public EventCase getCurrentEvent() {
        
        currentEvent = getSessionBean().getActiveEvent();
        return this.currentEvent;
    }

    /**
     * @param currentEvent the currentEvent to set
     */
    public void setCurrentEvent(EventCase currentEvent) {
        this.currentEvent = currentEvent;
    }

  
   

    /**
     * @return the formRequireViewConfirmation
     */
    public boolean isFormRequireViewConfirmation() {
        formRequireViewConfirmation = currentEvent.isRequiresViewConfirmation();
        return formRequireViewConfirmation;
    }

    /**
     * @param formRequireViewConfirmation the formRequireViewConfirmation to set
     */
    public void setFormRequireViewConfirmation(boolean formRequireViewConfirmation) {
        this.formRequireViewConfirmation = formRequireViewConfirmation;
    }

    /**
     * @return the disableClearViewConfirmation
     */
    public boolean isDisableClearViewConfirmation() {
        disableClearViewConfirmation = !(currentEvent.getViewConfirmedBy() != null);
        return disableClearViewConfirmation;
    }

    /**
     * @param disableClearViewConfirmation the disableClearViewConfirmation to set
     */
    public void setDisableClearViewConfirmation(boolean disableClearViewConfirmation) {
        this.disableClearViewConfirmation = disableClearViewConfirmation;
    }

    /**
     * @return the formClearExistingViewConfirmation
     */
    public boolean isFormClearExistingViewConfirmation() {
        return formClearExistingViewConfirmation;
    }

    /**
     * @param formClearExistingViewConfirmation the formClearExistingViewConfirmation to set
     */
    public void setFormClearExistingViewConfirmation(boolean formClearExistingViewConfirmation) {
        this.formClearExistingViewConfirmation = formClearExistingViewConfirmation;
    }

   
    
}
