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
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.EventCECase;
import com.tcvcog.tcvce.entities.EventCECaseCasePropBundle;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.integration.EventIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public class CECaseEventsBB
        extends     BackingBeanUtils
        implements  Serializable {

    private CECase currentCase;
    private ArrayList<EventCECase> recentEventList;
    
    
    private EventCECase eventForTriggeringCasePhaseAdvancement;
    
    private EventCECase triggeringEventForProposal;

    private List<EventCECase> filteredEventList;
    private EventCECase selectedEvent;
    private Person selectedPerson;

    
    private boolean allowedToClearActionResponse;
    private List<EventCategory> eventCategoryList;
    
    
    private EventCategory selectedEventCategory;
    private EventType selectedEventType;
    private List<EventType> availableEventTypeList;
    




    
    @PostConstruct
    public void initBean() {
        SessionBean sb = getSessionBean();
        setCurrentCase(sb.getSessionCECase());
       
    }
    /**
     * Creates a new instance of CECaseEventsBB
     */
    public CECaseEventsBB() {
    }
    
    
    public void rejectRequestedEvent(EventCECase ev) {
        setSelectedEvent(ev);
//        rejectedEventListIndex = currentCase.getEventProposalList().indexOf(ev);
    }
    
    
    public void hideEvent(EventCECase event){
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
    
    public void unHideEvent(EventCECase event){
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
    
    

    public void queueSelectedPerson(ActionEvent ev) {
        if (getSelectedPerson() != null) {
            selectedEvent.getPersonList().add(getSelectedPerson());
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Please select one or more people to attach to this event",
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
        }
    }

    public void deQueuePersonFromEvent(Person p) {
        if (selectedEvent.getPersonList() != null) {
            selectedEvent.getPersonList().remove(p);
        }
    }

    public void editEvent(EventCECase ev) {
        selectedEvent = ev;
    }
    
    
    /**
     * TODO Overhaul Proposals
     * Called when the user clicks the take requested action button
     *
     * @param ev
     */
    public void initiateNewRequestedEvent(EventCECase ev) {
        //selectedEventCategory = ev.getRequestedEventCat();
        
        setTriggeringEventForProposal(ev);
        initiateNewEvent();
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

    public void initiateNewEvent() {

        if (getSelectedEventCategory() != null) {

            System.out.println("EventAddBB.startNewEvent | category: " + getSelectedEventCategory().getEventCategoryTitle());
            EventCoordinator ec = getEventCoordinator();
            try {
                setSelectedEvent(ec.getInitializedEvent(getCurrentCase(), getSelectedEventCategory()));
                getSelectedEvent().setDateOfRecord(LocalDateTime.now());
                getSelectedEvent().setDiscloseToMunicipality(true);
                getSelectedEvent().setDiscloseToPublic(false);
            } catch (CaseLifecycleException ex) {
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
        getSelectedEvent().setCaseID(getCurrentCase().getCaseID());
        getSelectedEvent().setOwner(getSessionBean().getSessionUser());
        try {
        
         
            // main entry point for handing the new event off to the CaseCoordinator
            // only the compliance events need to pass in another object--the violation
            // otherwise just the case and the event go to the coordinator
            if (getSelectedEvent().getCategory().getEventType() == EventType.Compliance) {
//                getSelectedEvent().setEventID(cc.attachNewEventToCECase(getCurrentCase(), getSelectedEvent(), selectedViolation));
            } else {
                getSelectedEvent().setEventID(cc.attachNewEventToCECase(getCurrentCase(), getSelectedEvent(), null));
            }
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully logged event with an ID " + getSelectedEvent().getEventID(), ""));

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

        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(),
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
        } catch (CaseLifecycleException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(),
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
        }

        // nullify the session's case so that the reload of currentCase
        // no the cecaseProfile.xhtml will trigger a new DB read
    }

   
    public void commitEventEdits(ActionEvent ev) {
        EventCoordinator ec = getEventCoordinator();
//        currentCase.getEventList().remove(selectedEvent);
        try {
            ec.editEvent(getSelectedEvent(), getSessionBean().getSessionUser());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Event udpated!", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please select one or more people to attach to this event",
                    "This is a non-user system-level error that must be fixed by your Sys Admin"));
        }

    }

    /**
     * @return the currentCase
     */
    public CECase getCurrentCase() {
        return currentCase;
    }
    
    

    /**
     * @param currentCase the currentCase to set
     */
    public void setCurrentCase(CECase currentCase) {
        this.currentCase = currentCase;
    }

    /**
     * @return the recentEventList
     */
    public ArrayList<EventCECase> getRecentEventList() {
        return recentEventList;
    }

    /**
     * @return the eventForTriggeringCasePhaseAdvancement
     */
    public EventCECase getEventForTriggeringCasePhaseAdvancement() {
        return eventForTriggeringCasePhaseAdvancement;
    }

    /**
     * @return the triggeringEventForProposal
     */
    public EventCECase getTriggeringEventForProposal() {
        return triggeringEventForProposal;
    }

    /**
     * @return the filteredEventList
     */
    public List<EventCECase> getFilteredEventList() {
        return filteredEventList;
    }

    /**
     * @return the selectedEvent
     */
    public EventCECase getSelectedEvent() {
        return selectedEvent;
    }

    /**
     * @return the allowedToClearActionResponse
     */
    public boolean isAllowedToClearActionResponse() {
        return allowedToClearActionResponse;
    }

    /**
     * @return the eventCategoryList
     */
    public List<EventCategory> getEventCategoryList() {
       
        return eventCategoryList;
    }

    /**
     * @return the selectedEventCategory
     */
    public EventCategory getSelectedEventCategory() {
        return selectedEventCategory;
    }

    /**
     * @return the selectedEventType
     */
    public EventType getSelectedEventType() {
        return selectedEventType;
    }

    /**
     * @return the availableEventTypeList
     */
    public List<EventType> getAvailableEventTypeList() {
        
        return availableEventTypeList;
    }

    /**
     * @param recentEventList the recentEventList to set
     */
    public void setRecentEventList(ArrayList<EventCECase> recentEventList) {
        this.recentEventList = recentEventList;
    }

    /**
     * @param eventForTriggeringCasePhaseAdvancement the eventForTriggeringCasePhaseAdvancement to set
     */
    public void setEventForTriggeringCasePhaseAdvancement(EventCECase eventForTriggeringCasePhaseAdvancement) {
        this.eventForTriggeringCasePhaseAdvancement = eventForTriggeringCasePhaseAdvancement;
    }

    /**
     * @param triggeringEventForProposal the triggeringEventForProposal to set
     */
    public void setTriggeringEventForProposal(EventCECase triggeringEventForProposal) {
        this.triggeringEventForProposal = triggeringEventForProposal;
    }

    /**
     * @param filteredEventList the filteredEventList to set
     */
    public void setFilteredEventList(List<EventCECase> filteredEventList) {
        this.filteredEventList = filteredEventList;
    }

    /**
     * @param selectedEvent the selectedEvent to set
     */
    public void setSelectedEvent(EventCECase selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    /**
     * @param allowedToClearActionResponse the allowedToClearActionResponse to set
     */
    public void setAllowedToClearActionResponse(boolean allowedToClearActionResponse) {
        this.allowedToClearActionResponse = allowedToClearActionResponse;
    }

    /**
     * @param eventCategoryList the eventCategoryList to set
     */
    public void setEventCategoryList(List<EventCategory> eventCategoryList) {
        this.eventCategoryList = eventCategoryList;
    }

    /**
     * @param selectedEventCategory the selectedEventCategory to set
     */
    public void setSelectedEventCategory(EventCategory selectedEventCategory) {
        this.selectedEventCategory = selectedEventCategory;
    }

    /**
     * @param selectedEventType the selectedEventType to set
     */
    public void setSelectedEventType(EventType selectedEventType) {
        this.selectedEventType = selectedEventType;
    }

    /**
     * @param availableEventTypeList the availableEventTypeList to set
     */
    public void setAvailableEventTypeList(List<EventType> availableEventTypeList) {
        this.availableEventTypeList = availableEventTypeList;
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
    
}
