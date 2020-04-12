/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.application.SessionBean;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public  class OccPeriodEventsBB 
        extends BackingBeanUtils{
    
    private OccPeriodDataHeavy currentOccPeriod;
    
     // events 
    private EventCnF currentEvent;
    private List<EventCnF> filteredEventList;
    private List<ViewOptionsActiveHiddenListsEnum> eventsViewOptions;
    private ViewOptionsActiveHiddenListsEnum selectedEventView;
    private List<EventType> eventTypeListUserAllowed; 
    private List<EventType> eventTypeListAll;
    private EventType selectedEventType;
    private List<EventCategory> eventCategoryListUserAllowed;
    private List<EventCategory> eventCategoryListAllActive;
    private EventCategory selectedEventCategory;
    private List<Person> personCandidateList;
    private Person selectedPerson;
     
    @PostConstruct
    public void initBean() {
        EventCoordinator ec = getEventCoordinator();
        
        SessionBean sb = getSessionBean();
        currentOccPeriod = sb.getSessOccPeriod();
         eventsViewOptions = Arrays.asList(ViewOptionsActiveHiddenListsEnum.values());
        selectedEventView = ViewOptionsActiveHiddenListsEnum.VIEW_ALL;
        
           if(personCandidateList != null){
            personCandidateList = new ArrayList<>();
            personCandidateList.addAll(getSessionBean().getSessPersonList());
        }
        eventTypeListUserAllowed = ec.getPermittedEventTypesForOcc(currentOccPeriod, getSessionBean().getSessUser());
        eventTypeListAll = new ArrayList();
        eventTypeListAll = ec.getEventTypesAll();
        
    }
    
    public void reloadCurrentOccPeriodDataHeavy(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            currentOccPeriod = oc.assembleOccPeriodDataHeavy(currentOccPeriod, getSessionBean().getSessUser().getMyCredential());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Reloaded occ period ID " + currentOccPeriod.getPeriodID(), ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Unable to reload occ period", ""));
        }
        
    }
    /**
     * Creates a new instance of OccPeriodEvents
     */
    public OccPeriodEventsBB() {
    }

     /**
     * Called when the user selects their own EventCategory to add to the case
 and is a pass-through method to the initEvent method
     *
     * @param ev
     */
    public void events_initializeEvent(ActionEvent ev) {
        events_initiateNewEvent();
    }

    
    public void events_loadEventCategories() throws IntegrationException{
        System.out.println("OccInspectionBB.loadEventCategories | selected type: " + selectedEventType);
        EventCoordinator ec = getEventCoordinator();
        eventCategoryListUserAllowed = ec.loadEventCategoryListUserAllowed(selectedEventType, getSessionBean().getSessUser());
        
    }
    
    public void events_commitEventEdits(ActionEvent ev){
        EventCoordinator ec = getEventCoordinator();
        try {
            ec.editEvent(currentEvent);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Successfully updated event!", ""));
        } catch (IntegrationException | EventException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
        }
    }
    
    
    
    /**
     * Initialization of event process
     */
    public void events_initiateNewEvent() {

        if (getSelectedEventCategory() != null) {
            System.out.println("OccInspectionBB.initiateNewEvent | category: " + getSelectedEventCategory().getEventCategoryTitle());
            EventCoordinator ec = getEventCoordinator();
            try {
                currentEvent = ec.initEvent(currentOccPeriod, getSelectedEventCategory());
                currentEvent.setTimeStart(LocalDateTime.now());
                currentEvent.setTimeEnd(currentEvent.getTimeStart().plusMinutes(currentEvent.getCategory().getDefaultdurationmins()));
                currentEvent.setDiscloseToMunicipality(true);
                currentEvent.setDiscloseToPublic(false);
            } catch (BObStatusException | EventException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            } 
        } else {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Please select an event category to create a new event.", ""));
        }
    }
    
    public void events_initiateEventEdit(EventCnF ev){
        currentEvent = ev;
        System.out.println("OccInspectionBB.events_initiateEventEdit | current event: " + currentEvent.getEventID());
    }
    
       /**
     * All Code Enforcement case events are funneled through this method which
     * has to carry out a number of checks based on the type of event being
     * created. The event is then passed to the attachNewEventToCECase on the
     * CaseCoordinator who will do some more checking about the event before
     * writing it to the DB
     *
     * @param ev unused
     * @throws ViolationException
     */
    public void events_attachNewEvent(ActionEvent ev) {
        OccupancyCoordinator oc = getOccupancyCoordinator();

        // category is already set from initialization sequence
        currentEvent.setOwner(getSessionBean().getSessUser());
        try {
        
//             main entry point for handing the new event off to the CaseCoordinator
//             only the compliance events need to pass in another object--the violation
//             otherwise just the case and the event go to the coordinator
            oc.attachNewEventToOccPeriod(currentOccPeriod, currentEvent, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully logged event with an ID " + currentEvent.getEventID(), ""));

        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(),
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
        } 

//         nullify the session's case so that the reload of currentCase
//         no the cecaseProfile.xhtml will trigger a new DB read
        reloadCurrentOccPeriodDataHeavy();
    }
     
    
    public void updateEventCategoryList(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        EventIntegrator ei = getEventIntegrator();
        try {
            eventCategoryListUserAllowed = ei.getEventCategoryList(selectedEventType);
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Unable to load event category choices, sorry!", ""));
        }
    }
    
     public void hideEvent(EventCnF event){
        EventIntegrator ei = getEventIntegrator();
        event.setHidden(true);
        try {
            ei.updateEvent(event);
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_INFO,
                           "Success! event ID: " + event.getEventID() + " is now hidden", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not hide event, sorry; this is a system erro", ""));
        }
    }
    
    public void unHideEvent(EventCnF event){
        EventIntegrator ei = getEventIntegrator();
        event.setHidden(false);
        try {
            ei.updateEvent(event);
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_INFO,
                           "Success! Unhid event ID: " + event.getEventID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_ERROR,
                           "Could not unhide event, sorry; this is a system erro", ""));
        }
    }
    
    public void events_queuePerson(ActionEvent ev){
        if(currentEvent != null){
            currentEvent.getPersonList().add(selectedPerson);
        }
    }
    
    public void events_deQueuePersonFromEvent(Person p){
        currentEvent.getPersonList().remove(p);
    }
    
    
    
    /**
     * @return the currentOccPeriod
     */
    public OccPeriodDataHeavy getCurrentOccPeriod() {
        return currentOccPeriod;
    }

    /**
     * @return the currentEvent
     */
    public EventCnF getCurrentEvent() {
        return currentEvent;
    }

    /**
     * @return the filteredEventList
     */
    public List<EventCnF> getFilteredEventList() {
        return filteredEventList;
    }

    /**
     * @return the eventsViewOptions
     */
    public List<ViewOptionsActiveHiddenListsEnum> getEventsViewOptions() {
        return eventsViewOptions;
    }

    /**
     * @return the selectedEventView
     */
    public ViewOptionsActiveHiddenListsEnum getSelectedEventView() {
        return selectedEventView;
    }

    /**
     * @return the eventTypeListUserAllowed
     */
    public List<EventType> getEventTypeListUserAllowed() {
        return eventTypeListUserAllowed;
    }

    /**
     * @return the eventTypeListAll
     */
    public List<EventType> getEventTypeListAll() {
        return eventTypeListAll;
    }

    /**
     * @return the selectedEventType
     */
    public EventType getSelectedEventType() {
        return selectedEventType;
    }

    /**
     * @return the eventCategoryListUserAllowed
     */
    public List<EventCategory> getEventCategoryListUserAllowed() {
        return eventCategoryListUserAllowed;
    }

    /**
     * @return the eventCategoryListAllActive
     */
    public List<EventCategory> getEventCategoryListAllActive() {
        return eventCategoryListAllActive;
    }

    /**
     * @return the selectedEventCategory
     */
    public EventCategory getSelectedEventCategory() {
        return selectedEventCategory;
    }

    /**
     * @return the personCandidateList
     */
    public List<Person> getPersonCandidateList() {
        return personCandidateList;
    }

    /**
     * @return the selectedPerson
     */
    public Person getSelectedPerson() {
        return selectedPerson;
    }

    /**
     * @param currentOccPeriod the currentOccPeriod to set
     */
    public void setCurrentOccPeriod(OccPeriodDataHeavy currentOccPeriod) {
        this.currentOccPeriod = currentOccPeriod;
    }

    /**
     * @param currentEvent the currentEvent to set
     */
    public void setCurrentEvent(EventCnF currentEvent) {
        this.currentEvent = currentEvent;
    }

    /**
     * @param filteredEventList the filteredEventList to set
     */
    public void setFilteredEventList(List<EventCnF> filteredEventList) {
        this.filteredEventList = filteredEventList;
    }

    /**
     * @param eventsViewOptions the eventsViewOptions to set
     */
    public void setEventsViewOptions(List<ViewOptionsActiveHiddenListsEnum> eventsViewOptions) {
        this.eventsViewOptions = eventsViewOptions;
    }

    /**
     * @param selectedEventView the selectedEventView to set
     */
    public void setSelectedEventView(ViewOptionsActiveHiddenListsEnum selectedEventView) {
        this.selectedEventView = selectedEventView;
    }

    /**
     * @param eventTypeListUserAllowed the eventTypeListUserAllowed to set
     */
    public void setEventTypeListUserAllowed(List<EventType> eventTypeListUserAllowed) {
        this.eventTypeListUserAllowed = eventTypeListUserAllowed;
    }

    /**
     * @param eventTypeListAll the eventTypeListAll to set
     */
    public void setEventTypeListAll(List<EventType> eventTypeListAll) {
        this.eventTypeListAll = eventTypeListAll;
    }

    /**
     * @param selectedEventType the selectedEventType to set
     */
    public void setSelectedEventType(EventType selectedEventType) {
        this.selectedEventType = selectedEventType;
    }

    /**
     * @param eventCategoryListUserAllowed the eventCategoryListUserAllowed to set
     */
    public void setEventCategoryListUserAllowed(List<EventCategory> eventCategoryListUserAllowed) {
        this.eventCategoryListUserAllowed = eventCategoryListUserAllowed;
    }

    /**
     * @param eventCategoryListAllActive the eventCategoryListAllActive to set
     */
    public void setEventCategoryListAllActive(List<EventCategory> eventCategoryListAllActive) {
        this.eventCategoryListAllActive = eventCategoryListAllActive;
    }

    /**
     * @param selectedEventCategory the selectedEventCategory to set
     */
    public void setSelectedEventCategory(EventCategory selectedEventCategory) {
        this.selectedEventCategory = selectedEventCategory;
    }

    /**
     * @param personCandidateList the personCandidateList to set
     */
    public void setPersonCandidateList(List<Person> personCandidateList) {
        this.personCandidateList = personCandidateList;
    }

    /**
     * @param selectedPerson the selectedPerson to set
     */
    public void setSelectedPerson(Person selectedPerson) {
        this.selectedPerson = selectedPerson;
    }
    
}
