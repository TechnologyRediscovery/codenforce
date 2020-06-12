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

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventDomainEnum;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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
public class EventsBB extends BackingBeanUtils implements Serializable{
    
    
    private EventDomainEnum currentEventDomain;
    
    private OccPeriodDataHeavy currentOccPeriod;
    
    private CECaseDataHeavy currentCase;
    private OccPeriodDataHeavy currentPeriod;

    private EventCnF currentEvent;
    private EventCnF triggeringEventForProposal;
    
    private List<EventCategory> eventCategoryList;
    private EventCategory eventCategorySelected;
    
    private List<EventType> eventTypeList;
    private EventType eventTypeSelected;
    private Person selectedPerson;
    
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
    
    /**
     * Creates a new instance of EventsBB
     */
    public EventsBB() {
    }
    
     @PostConstruct
    public void initBean() {
        EventCoordinator ec = getEventCoordinator();
        
        SessionBean sb = getSessionBean();
        setCurrentOccPeriod(sb.getSessOccPeriod());
         eventsViewOptions = Arrays.asList(ViewOptionsActiveHiddenListsEnum.values());
        selectedEventView = ViewOptionsActiveHiddenListsEnum.VIEW_ALL;
        
           if(personCandidateList != null){
            personCandidateList = new ArrayList<>();
            personCandidateList.addAll(getSessionBean().getSessPersonList());
        }
        eventTypeListUserAllowed = ec.getPermittedEventTypesForOcc(getCurrentOccPeriod(), getSessionBean().getSessUser());
        try {
            eventCategoryListUserAllowed = ec.loadEventCategoryListUserAllowed(selectedEventType, getSessionBean().getSessUser());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        eventTypeListAll = new ArrayList();
        eventTypeListAll = ec.getEventTypesAll();
        
    }
    
    
     /**
     * Called when the user selects their own EventCategory to add to the case
     * and is a pass-through method to the initiateNewEvent method
     *
     * @param ev
     */
    public void initiateUserChosenEventCreation(ActionEvent ev) {
        initiateNewEvent();
    }
    
    
    
    public void events_initiateEventEdit(EventCnF ev){
        currentEvent = ev;
        System.out.println("OccInspectionBB.events_initiateEventEdit | current event: " + currentEvent.getEventID());
    }
    

    public void initiateNewEvent() {
        EventCoordinator ec = getEventCoordinator();
        
        EventCnF ev = null;
        if (getEventCategorySelected() != null) {

            try {
                
                ev = ec.initEvent(getCurrentCase(), getEventCategorySelected());
                ev.setDiscloseToMunicipality(true);
                ev.setDiscloseToPublic(false);
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
    public void attachEventToCase(ActionEvent ev) throws ViolationException {
        EventCoordinator ec = getEventCoordinator();
        CaseCoordinator cc = getCaseCoordinator();

        // category is already set from initialization sequence
        currentEvent.setCeCaseID(getCurrentCase().getCaseID());
        currentEvent.setOwner(getSessionBean().getSessUser());
        try {
        
         
            // main entry point for handing the new event off to the CaseCoordinator
            // only the compliance events need to pass in another object--the violation
            // otherwise just the case and the event go to the coordinator
            if (currentEvent.getCategory().getEventType() == EventType.Compliance) {
//                currentEvent.setEventID(cc.attachNewEventToCECase(getCurrentCase(), currentEvent, selectedViolation));
            } else {
                currentEvent.setEventID(cc.attachNewEventToCECase(getCurrentCase(), currentEvent, null));
            }
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully logged event with an ID " + currentEvent.getEventID(), ""));

            // now update the triggering event with the newly inserted event's ID
            // (We saved the triggering event when the take action button was clicked, before the event
            // add dialog was displayed and event-specific data is entered by the user
            if (getTriggeringEventForProposal() != null) {
//                triggeringEventForProposal.getEventProposalImplementation().setResponseEvent(selectedEvent);
//                triggeringEventForProposal.getEventProposalImplementation().setResponderActual(getSessionBean().getFacesUser());
//                ec.logResponseToActionRequest(triggeringEventForProposal);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Updated triggering event ID + "
                                + getTriggeringEventForProposal().getEventID()
                                + " with response info!", ""));
                // reset our holding var since we're done processing the event
                setTriggeringEventForProposal(null);
            }

        } catch (IntegrationException | BObStatusException | EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(),
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
        } 

        // nullify the session's case so that the reload of currentCase
        // no the cecaseProfile.xhtml will trigger a new DB read
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
    

   
    public void commitEventEdits(ActionEvent ev) {
        EventCoordinator ec = getEventCoordinator();
//        currentCase.getEventList().remove(selectedEvent);
        try {
            ec.editEvent(currentEvent);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Event udpated!", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please select one or more people to attach to this event",
                    "This is a non-user system-level error that must be fixed by your Sys Admin"));
        } catch (EventException ex) {
            Logger.getLogger(EventSearchBB.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void queueSelectedPerson(ActionEvent ev) {
        if (getSelectedPerson() != null) {
            currentEvent.getPersonList().add(getSelectedPerson());
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Please select one or more people to attach to this event",
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
        }
    }

    public void deQueuePersonFromEvent(Person p) {
        if (currentEvent.getPersonList() != null) {
            currentEvent.getPersonList().remove(p);
        }
    }

    public void editEvent(EventCnF ev) {
        currentEvent = ev;
    }

    /**
     * @return the currentCase
     */
    public CECaseDataHeavy getCurrentCase() {
        return currentCase;
    }

    /**
     * @return the currentPeriod
     */
    public OccPeriodDataHeavy getCurrentPeriod() {
        return currentPeriod;
    }

    /**
     * @return the eventCategoryList
     */
    public List<EventCategory> getEventCategoryList() {
        return eventCategoryList;
    }

    /**
     * @return the eventCategorySelected
     */
    public EventCategory getEventCategorySelected() {
        return eventCategorySelected;
    }

    /**
     * @return the eventTypeList
     */
    public List<EventType> getEventTypeList() {
        return eventTypeList;
    }

    /**
     * @return the eventTypeSelected
     */
    public EventType getEventTypeSelected() {
        return eventTypeSelected;
    }

    /**
     * @param currentCase the currentCase to set
     */
    public void setCurrentCase(CECaseDataHeavy currentCase) {
        this.currentCase = currentCase;
    }

    /**
     * @param currentPeriod the currentPeriod to set
     */
    public void setCurrentPeriod(OccPeriodDataHeavy currentPeriod) {
        this.currentPeriod = currentPeriod;
    }

    /**
     * @param eventCategoryList the eventCategoryList to set
     */
    public void setEventCategoryList(List<EventCategory> eventCategoryList) {
        this.eventCategoryList = eventCategoryList;
    }

    /**
     * @param eventCategorySelected the eventCategorySelected to set
     */
    public void setEventCategorySelected(EventCategory eventCategorySelected) {
        this.eventCategorySelected = eventCategorySelected;
    }

    /**
     * @param eventTypeList the eventTypeList to set
     */
    public void setEventTypeList(List<EventType> eventTypeList) {
        this.eventTypeList = eventTypeList;
    }

    /**
     * @param eventTypeSelected the eventTypeSelected to set
     */
    public void setEventTypeSelected(EventType eventTypeSelected) {
        this.eventTypeSelected = eventTypeSelected;
    }

    /**
     * @return the currentEvent
     */
    public EventCnF getCurrentEvent() {
        return currentEvent;
    }

    /**
     * @param currentEvent the currentEvent to set
     */
    public void setCurrentEvent(EventCnF currentEvent) {
        this.currentEvent = currentEvent;
    }

    /**
     * @return the triggeringEventForProposal
     */
    public EventCnF getTriggeringEventForProposal() {
        return triggeringEventForProposal;
    }

    /**
     * @param triggeringEventForProposal the triggeringEventForProposal to set
     */
    public void setTriggeringEventForProposal(EventCnF triggeringEventForProposal) {
        this.triggeringEventForProposal = triggeringEventForProposal;
    }

    /**
     * @return the selectedPerson
     */
    public Person getSelectedPerson() {
        return selectedPerson;
    }

    /**
     * @param selectedPerson the selectedPerson to set
     */
    public void setSelectedPerson(Person selectedPerson) {
        this.selectedPerson = selectedPerson;
    }

    /**
     * @return the currentEventDomain
     */
    public EventDomainEnum getCurrentEventDomain() {
        return currentEventDomain;
    }

    /**
     * @param currentEventDomain the currentEventDomain to set
     */
    public void setCurrentEventDomain(EventDomainEnum currentEventDomain) {
        this.currentEventDomain = currentEventDomain;
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
    
    

}
