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
import com.tcvcog.tcvce.entities.EventCECase;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Eric Darsow
 */
public class EventManageBB extends BackingBeanUtils implements Serializable {
    
    // add currentEvent form fields
    private ArrayList<EventCategory> eventCategoryList;
    
    private ArrayList catComList;
    private ArrayList catActionList;
    private ArrayList catMeetingList;
    private ArrayList catCustomList;
    
    private EventCategory selectedEventCategory ;
    private EventType selectedEventType;
    private EventType[] userAdminEventTypeList;
    
    private EventCECase eventInProcess;
    
    private List<Person> personsToAdd;
    private Person selectedPerson;
    
    // constructor
    public EventManageBB(){
        
    }
    
    @PostConstruct
    public void initBean(){
        personsToAdd = new ArrayList<>();
        
    }
    
    
    public String startNewEvent(){
        
        if (selectedEventCategory != null){

            System.out.println("EventAddBB.startNewEvent | category: " + selectedEventCategory.getEventCategoryTitle());

            CECase c = getSessionBean().getcECase();
            EventCoordinator ec = getEventCoordinator();
            try {
                eventInProcess = ec.getInitializedEvent(c, selectedEventCategory);
            } catch (CaseLifecyleException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            }
            getSessionBean().setActiveEvent(eventInProcess);
        } else {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Please select an event category to create a new event." ,""));
        }
        return "eventAdd";
    }
    
    public void attachEventToCase(ActionEvent ev) throws ViolationException{
        
        //Event e = getSessionBean().getActiveEvent();
        CaseCoordinator cc = getCaseCoordinator();
        CECase ccase = getSessionBean().getcECase();
        
        // category is already set from initialization sequence
        eventInProcess.setCaseID(ccase.getCaseID());
        System.out.println("EventAddBB.addEvent | CaseID: " + eventInProcess.getCaseID());
        eventInProcess.setCreator(getSessionBean().getFacesUser());
//        e.setEventPersons(formSelectedPersons);
        
        // now check for persons to connect
        
        try {
            if(eventInProcess.getCategory().getEventType() == EventType.Compliance){
                cc.processComplianceEvent(ccase, eventInProcess, getSessionBean().getActiveCodeViolation());
            } else {
                cc.processCEEvent(ccase, eventInProcess);
            }
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
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            ex.getMessage(), 
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
        }

        // nullify the session's case so that the reload of currentCase
        // no the cecaseProfile.xhtml will trigger a new DB read
        getSessionBean().setcECase(null);
    }
    
    

   
    public void queueSelectedPerson(ActionEvent ev){
        System.out.println("EventAddBB.queueSelectedPerson | In listener method");
        if(selectedPerson != null){
            if(personsToAdd != null){
                personsToAdd.add(selectedPerson);
                System.out.println("EventManageBB.queueSelectedPerson | added person to List | list size: " + personsToAdd.size());
            }
            
        } else {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Please select one or more people to attach to this event", 
                    "This is a non-user system-level error that must be fixed by your Sys Admin"));
            
        }
    }
    
    public void deQueuePersonFromEvent(Person p){
        if(personsToAdd != null){
            personsToAdd.remove(p);
        }
    }
    
      public String editEvent(){
        EventCoordinator ec = getEventCoordinator();
        
       
        // now check for persons to connect
        
        try {
//            ec.updateEvent(e, formClearExistingViewConfirmation);
//            getFacesContext().addMessage(null,
//                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
//                            "Successfully updated event ID: " + e.getEventID() , ""));
            
            getSessionBean().refreshActiveCase();
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
     * @return the selectedCadidatePersons
     */
    public Person getSelectedPerson() {
        return selectedPerson;
    }

    /**
     * @param p
     */
    public void setSelectedPerson(Person p) {
        this.selectedPerson = p;
    }

    /**
     * @return the eventInProcess
     */
    public EventCECase getEventInProcess() {
        EventCECase evCECase = getSessionBean().getActiveEvent();
        if(evCECase != null){
            eventInProcess = evCECase;
        }
        return eventInProcess;
    }

    /**
     * @param eventInProcess the eventInProcess to set
     */
    public void setEventInProcess(EventCECase eventInProcess) {
        this.eventInProcess = eventInProcess;
    }

    /**
     * @return the personsToAdd
     */
    public List<Person> getPersonsToAdd() {
       
        return personsToAdd;
    }

    /**
     * @param personsToAdd the personsToAdd to set
     */
    public void setPersonsToAdd(List<Person> personsToAdd) {
        this.personsToAdd = personsToAdd;
    }

   
    
}
