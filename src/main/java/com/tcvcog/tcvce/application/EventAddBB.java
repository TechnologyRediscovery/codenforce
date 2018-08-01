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


import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
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
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Eric Darsow
 */
public class EventAddBB extends BackingBeanUtils implements Serializable {
    
    // add currentEvent form fields
    private ArrayList<EventCategory> eventCategoryList;
    
    private ArrayList catComList;
    private ArrayList catActionList;
    private ArrayList catMeetingList;
    private ArrayList catCustomList;
    
    private EventCategory selectedEventCategory;
    private String selectedEventCateogryDescription;
    private boolean selectedEventRequiresViewConfirmation;
    private boolean selectedEventNotifiesCaseMonitors;
    private EventType selectedEventType;
    private EventType[] userAdminEventTypeList;
    
    private CECase ceCase;
    private EventCase currentEvent;
    
    private String formEventDesc;
    private Date formEventDate;
    private boolean formDiscloseToMuni;
    private boolean formDiscloseToPublic;
    private boolean activeEvent;
    private String formEventNotes;
    private boolean formRequireViewConfirmation;
    
    private ArrayList<Person> candidatePersonList;
    private Person selectedCadidatePerson;
    private ArrayList<Person> formSelectedPersons;
    
    // constructor
    public EventAddBB(){
        
    }
    
    public String startNewEvent(){
        System.out.println("EventAddBB.startNewEvent | category: " + selectedEventCategory.getEventCategoryTitle());
        
        CECase c = getSessionBean().getActiveCase();
        EventCoordinator ec = getEventCoordinator();
        try {
            currentEvent = ec.getInitializedEvent(c, selectedEventCategory);
        } catch (CaseLifecyleException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            ex.getMessage(), ""));
            
            
        }
        getSessionBean().setActiveEvent(currentEvent);
        return "eventAdd";
    }
    
    public String addEvent() throws ViolationException{
        
        //Event e = getSessionBean().getActiveEvent();
        EventCase e = currentEvent;
        CaseCoordinator cc = getCaseCoordinator();
        
        // category is already set from initialization sequence
        e.setCaseID(getSessionBean().getActiveCase().getCaseID());
        System.out.println("EventAddBB.addEvent | CaseID: " + e.getCaseID());
        e.setEventDescription(formEventDesc);
        e.setActiveEvent(activeEvent);
        e.setEventOwnerUser(getFacesUser());
        e.setDateOfRecord(formEventDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        e.setDiscloseToMunicipality(formDiscloseToMuni);
        e.setDiscloseToPublic(formDiscloseToPublic);
        e.setRequiresViewConfirmation(formRequireViewConfirmation);
        e.setNotes(formEventDesc);
//        e.setEventPersons(formSelectedPersons);
        
        // now check for persons to connect
        
        try {
            cc.processCEEvent(ceCase, e);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Successfully logged event.", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            ex.getMessage(), 
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
        } catch (CaseLifecyleException ex) {
            Logger.getLogger(EventAddBB.class.getName()).log(Level.SEVERE, null, ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            ex.getMessage(), 
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
        }
        
        try {
            cc.refreshCase(ceCase);
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            ex.getMessage(), 
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
            
        }
        // get user back to the most logical case page
        switch(e.getCategory().getEventType()){
            case Compliance:
                return "caseViolations";
            default:
                return "caseProfile";
                
        }
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
         if(currentEvent == null){
            formDiscloseToMuni = true;
        } else {
            formDiscloseToMuni = currentEvent.isDiscloseToMunicipality();
        }
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
        if(currentEvent == null){
            activeEvent = true;
        } else {
            activeEvent = currentEvent.isActiveEvent();
        }
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
     * @return the candidatePersonList
     */
    public ArrayList<Person> getCandidatePersonList() {
        System.out.println("EventAddBB.getCandidatePersonList | inside method");
        PersonIntegrator pi = getPersonIntegrator();
        
        
        try {
            candidatePersonList = pi.getPersonList(getSessionBean().getActiveCase().getProperty());
        } catch (IntegrationException ex) {
            // do nothing
        }
        return candidatePersonList;
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
     * @param candidatePersonList the candidatePersonList to set
     */
    public void setCandidatePersonList(ArrayList<Person> candidatePersonList) {
        this.candidatePersonList = candidatePersonList;
    }

   

    /**
     * @return the ceCase
     */
    public CECase getCeCase() {
        
        ceCase = getSessionBean().getActiveCase();
        return ceCase;
    }

    /**
     * @param ceCase the ceCase to set
     */
    public void setCeCase(CECase ceCase) {
        
        this.ceCase = ceCase;
    }
    
    public void attachSelectedPerson(){
        System.out.println("EventAddBB.attachSelectedPersons | In listener method");
        if(selectedCadidatePerson != null){
            System.out.println("EventAddBB.attachSelectedPeople | AddingPerson:  " + selectedCadidatePerson);
            formSelectedPersons.add(selectedCadidatePerson);
            
        } else {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Please select one or more people to attach to this event", 
                    "This is a non-user system-level error that must be fixed by your Sys Admin"));
            
        }
    }

    /**
     * @return the formSelectedPersons
     */
    public ArrayList<Person> getFormSelectedPersons() {
        EventCoordinator ec = getEventCoordinator();
        if(formSelectedPersons == null){   
            System.out.println("EventAddBB.getFormSelectedPersons | getting empty LL");
            formSelectedPersons = ec.getEmptyEventPersonList();
        }
        System.out.println("EventAddBB.getFormSelectedPersons | ll size: " + formSelectedPersons.size());
        
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
        
        EventCase currentEvent = getSessionBean().getActiveEvent();
        this.currentEvent = currentEvent;
        return this.currentEvent;
    }

    /**
     * @param currentEvent the currentEvent to set
     */
    public void setCurrentEvent(EventCase currentEvent) {
        this.currentEvent = currentEvent;
    }

    /**
     * @return the eventCategoryList
     */
    public ArrayList getEventCategoryList() {
        EventIntegrator ei = getEventIntegrator();
        
        if(selectedEventType != null){
            
            try {
                eventCategoryList = ei.getEventCategoryList(selectedEventType);
            } catch (IntegrationException ex) {
                // do nothing
            }
        }
        return eventCategoryList;
    }

    /**
     * @param eventCategoryList the eventCategoryList to set
     */
    public void setEventCategoryList(ArrayList eventCategoryList) {
        this.eventCategoryList = eventCategoryList;
    }

   

    /**
     * @return the catComList
     */
    public ArrayList getCatComList() {
        EventIntegrator ei = getEventIntegrator();
        try {
            catComList = ei.getEventCategoryList(EventType.Communication);
        } catch (IntegrationException ex) {
            // do nothing
        }
        return catComList;
    }

   
    /**
     * @return the catActionList
     */
    public ArrayList getCatActionList() {
          EventIntegrator ei = getEventIntegrator();
        try {
            catActionList = ei.getEventCategoryList(EventType.Action);
        } catch (IntegrationException ex) {
            // do nothing
        }
        return catActionList;
    }

    
    /**
     * @return the catMeetingList
     */
    public ArrayList getCatMeetingList() {
                  EventIntegrator ei = getEventIntegrator();
        try {
            catMeetingList = ei.getEventCategoryList(EventType.Meeting);
        } catch (IntegrationException ex) {
            // do nothing
        }
        return catMeetingList;
    }

    /**
     * @param catComList the catComList to set
     */
    public void setCatComList(ArrayList catComList) {
        this.catComList = catComList;
    }

    /**
     * @param catActionList the catActionList to set
     */
    public void setCatActionList(ArrayList catActionList) {
        this.catActionList = catActionList;
    }


    /**
     * @param catMeetingList the catMeetingList to set
     */
    public void setCatMeetingList(ArrayList catMeetingList) {
        this.catMeetingList = catMeetingList;
    }

    /**
     * @return the selectedEventCategory
     */
    public EventCategory getSelectedEventCategory() {
        return selectedEventCategory;
    }

    /**
     * @param selectedEventCategory the selectedEventCategory to set
     */
    public void setSelectedEventCategory(EventCategory selectedEventCategory) {
        this.selectedEventCategory = selectedEventCategory;
    }

    /**
     * @return the catCustomList
     */
    public ArrayList getCatCustomList() {
        return catCustomList;
    }

    /**
     * @param catCustomList the catCustomList to set
     */
    public void setCatCustomList(ArrayList catCustomList) {
        this.catCustomList = catCustomList;
    }

    /**
     * @return the selectedEventType
     */
    public EventType getSelectedEventType() {
        return selectedEventType;
    }

    /**
     * @param selectedEventType the selectedEventType to set
     */
    public void setSelectedEventType(EventType selectedEventType) {
        this.selectedEventType = selectedEventType;
    }

    /**
     * @return the userAdminEventTypeList
     */
    public EventType[] getUserAdminEventTypeList() {
        EventCoordinator ec = getEventCoordinator();
        userAdminEventTypeList = ec.getUserAdmnisteredEventTypeList();
        return userAdminEventTypeList;
    }

    /**
     * @param userAdminEventTypeList the userAdminEventTypeList to set
     */
    public void setUserAdminEventTypeList(EventType[] userAdminEventTypeList) {
        this.userAdminEventTypeList = userAdminEventTypeList;
    }

    /**
     * @return the selectedEventCateogryDescription
     */
    public String getSelectedEventCateogryDescription() {
        if(selectedEventCategory != null){
            selectedEventCateogryDescription = selectedEventCategory.getEventCategoryDesc();
        }
        return selectedEventCateogryDescription;
    }

    /**
     * @param selectedEventCateogryDescription the selectedEventCateogryDescription to set
     */
    public void setSelectedEventCateogryDescription(String selectedEventCateogryDescription) {
        this.selectedEventCateogryDescription = selectedEventCateogryDescription;
    }

    /**
     * @return the selectedEventRequiresViewConfirmation
     */
    public boolean isSelectedEventRequiresViewConfirmation() {
        if(selectedEventCategory != null){
            selectedEventRequiresViewConfirmation = selectedEventCategory.isRequiresviewconfirmation();
        }
        return selectedEventRequiresViewConfirmation;
    }

    /**
     * @return the selectedEventNotifiesCaseMonitors
     */
    public boolean isSelectedEventNotifiesCaseMonitors() {
          if(selectedEventCategory != null){
            selectedEventNotifiesCaseMonitors = selectedEventCategory.isNotifycasemonitors();
        }
        return selectedEventNotifiesCaseMonitors;
    }

    /**
     * @param selectedEventRequiresViewConfirmation the selectedEventRequiresViewConfirmation to set
     */
    public void setSelectedEventRequiresViewConfirmation(boolean selectedEventRequiresViewConfirmation) {
        this.selectedEventRequiresViewConfirmation = selectedEventRequiresViewConfirmation;
    }

    /**
     * @param selectedEventNotifiesCaseMonitors the selectedEventNotifiesCaseMonitors to set
     */
    public void setSelectedEventNotifiesCaseMonitors(boolean selectedEventNotifiesCaseMonitors) {
        this.selectedEventNotifiesCaseMonitors = selectedEventNotifiesCaseMonitors;
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
     * @return the selectedCadidatePersons
     */
    public Person getSelectedCadidatePerson() {
        return selectedCadidatePerson;
    }

    /**
     * @param p
     */
    public void setSelectedCadidatePerson(Person p) {
        this.selectedCadidatePerson = p;
    }

   
    
    
    
}
