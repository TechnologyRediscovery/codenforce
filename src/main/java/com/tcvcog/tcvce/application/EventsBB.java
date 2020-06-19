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

import com.tcvcog.tcvce.application.interfaces.IFace_EventRuleGoverned;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.MunicipalityCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
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
 * The cornerstone backing bean for the grand unified event (GUE) subsystem:
 * displays and allows the user to manipulate EventCnF objects for either
 * CECase objects or OccPeriod objects--our two workflow-enabled BObs
 * @author sylvia
 */
public class EventsBB extends BackingBeanUtils implements Serializable{
    
    private EventDomainEnum currentEventDomain;
    
    private IFace_EventRuleGoverned currentERGBOb;
    
    private EventCnF currentEvent;
    
    private List<EventCnF> eventList;
    private List<EventCnF> filteredEventList;
    
    private List<ViewOptionsActiveHiddenListsEnum> eventsViewOptionsCandidates;
    private ViewOptionsActiveHiddenListsEnum selectedEventView;
    
    private List<EventType> eventTypeCandidates;
    private EventType eventTypeSelected;
    
    private List<EventCategory> eventCategoryCandidates;
    private EventCategory eventCategorySelected;
    
    // used during event edits
    private List<Person> personCandidates;
    private Person personSelected;
    
    // unknown use
    private EventCnF triggeringEventForProposal;
    
    
  
    /**
     * Creates a new instance of EventsBB
     */
    public EventsBB() {
    }
    
    /**
     * We've got to setup this bean to work with either event domain: OccPeriod
     * or CECase, which we'll get by asking the session bean what it is set to. If
     * the SessionBean isn't clear on the matter, implement some basic logic 
     * to choose and tell the user
     * 
     * We'll also setup possible new events based on the status of the 
     * active workflow-enabled BOb
     * 
     */
    @PostConstruct
    public void initBean() {
        EventCoordinator ec = getEventCoordinator();
        MunicipalityCoordinator mc = getMuniCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        SessionBean sb = getSessionBean();
        
        eventsViewOptionsCandidates = Arrays.asList(ViewOptionsActiveHiddenListsEnum.values());
        
        selectedEventView = ec.determineDefaultEventView(sb.getSessUser());
        
        eventList = new ArrayList<>();
        
        // Based on who sent us to this page, we'll load in either the CECase or OccPeriod
        // on the SessionBean
        EventDomainEnum domain = sb.getSessEventDomain();
        switch(domain){
            case CODE_ENFORCEMENT:
                currentERGBOb = sb.getSessCECase();
                // in this case our ERGBob is a CECase
                eventList.addAll(currentERGBOb.assembleEventList(selectedEventView));
                break;
            case OCCUPANCY:
                currentERGBOb = sb.getSessOccPeriod();
                // in this case our ERGBOb is an OccPeriod
                eventList.addAll(currentERGBOb.assembleEventList(selectedEventView));
                break;
            case UNIVERSAL:
                // We'll just grab all the events on the session and load up
                // the muni property info
                eventList.addAll(sb.getSessEventList());
                // ask the Prop Coor to figure out a sensible ERG when we're viewing
                // an arbitrary event list
                currentERGBOb = (IFace_EventRuleGoverned) pc.determineGoverningPropertyInfoCase(sb.getSessMuni().getMuniPropertyDH());
                break;
            // "Shouldn't happen"
            default:
                eventList.addAll(sb.getSessEventList());
                currentERGBOb = (IFace_EventRuleGoverned) pc.determineGoverningPropertyInfoCase(sb.getSessMuni().getMuniPropertyDH());
                
        }
        
        personCandidates = new ArrayList<>();
        personCandidates.addAll(getSessionBean().getSessPersonList());
        
        eventTypeCandidates = ec.determinePermittedEventTypes(domain, currentERGBOb, sb.getSessUser());
        eventCategoryCandidates = new ArrayList<>();
        if(eventTypeCandidates != null && !eventTypeCandidates.isEmpty()){
            eventTypeSelected = eventTypeCandidates.get(0);
            eventCategoryCandidates = ec.determinePermittedEventCategories(eventTypeSelected, sb.getSessUser());
        }
        

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
    
    
    /**
     * Listener method for starting event edits
     * @param ev 
     */
    public void initiateEventEdit(EventCnF ev){
        currentEvent = ev;
    }
    

    /**
     * Logic container for setting up new event which will be displayed 
     * in the overlay window for the User
     */
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
        currentEvent.setUserCreator(getSessionBean().getSessUser());
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
    
    
  
    /**
     * Listener method for changes in EventType selected by User
     */
    public void refreshAvailableEventCategories(){
        EventCoordinator ec = getEventCoordinator();
        if(eventTypeSelected != null){
            eventCategoryCandidates.clear();
            eventCategoryCandidates.addAll(ec.determinePermittedEventCategories(eventTypeSelected, getSessionBean().getSessUser()));
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
    

    /**
     * Listener pass through method for finalizing event edits
     * @param ev 
     */
    public void finalizeEventUpdateListener(ActionEvent ev){
        finalizeEventUpdate();
    }
    
    /**
     * Event update processing
     */
    public void finalizeEventUpdate() {
        EventCoordinator ec = getEventCoordinator();
        try {
            ec.editEvent(currentEvent);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Event udpated!", ""));
        } catch (IntegrationException | EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(),
                    ""));
        } 

    }

    public void queueSelectedPerson(ActionEvent ev) {
        if (getPersonSelected() != null) {
            currentEvent.getPersonList().add(getPersonSelected());
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
     * @return the eventCategoryCandidates
     */
    public List<EventCategory> getEventCategoryCandidates() {
        return eventCategoryCandidates;
    }

    /**
     * @return the eventCategorySelected
     */
    public EventCategory getEventCategorySelected() {
        return eventCategorySelected;
    }

    /**
     * @return the eventTypeCandidates
     */
    public List<EventType> getEventTypeCandidates() {
        return eventTypeCandidates;
    }

    /**
     * @return the eventTypeSelected
     */
    public EventType getEventTypeSelected() {
        return eventTypeSelected;
    }

    /**
     * @param eventCategoryCandidates the eventCategoryCandidates to set
     */
    public void setEventCategoryCandidates(List<EventCategory> eventCategoryCandidates) {
        this.eventCategoryCandidates = eventCategoryCandidates;
    }

    /**
     * @param eventCategorySelected the eventCategorySelected to set
     */
    public void setEventCategorySelected(EventCategory eventCategorySelected) {
        this.eventCategorySelected = eventCategorySelected;
    }

    /**
     * @param eventTypeCandidates the eventTypeCandidates to set
     */
    public void setEventTypeCandidates(List<EventType> eventTypeCandidates) {
        this.eventTypeCandidates = eventTypeCandidates;
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
     * @return the personSelected
     */
    public Person getPersonSelected() {
        return personSelected;
    }

    /**
     * @param personSelected the personSelected to set
     */
    public void setPersonSelected(Person personSelected) {
        this.personSelected = personSelected;
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
     * @return the filteredEventList
     */
    public List<EventCnF> getFilteredEventList() {
        return filteredEventList;
    }

    /**
     * @return the eventsViewOptionsCandidates
     */
    public List<ViewOptionsActiveHiddenListsEnum> getEventsViewOptionsCandidates() {
        return eventsViewOptionsCandidates;
    }

    /**
     * @return the selectedEventView
     */
    public ViewOptionsActiveHiddenListsEnum getSelectedEventView() {
        return selectedEventView;
    }


    /**
     * @return the personCandidates
     */
    public List<Person> getPersonCandidates() {
        return personCandidates;
    }

    /**
     * @param filteredEventList the filteredEventList to set
     */
    public void setFilteredEventList(List<EventCnF> filteredEventList) {
        this.filteredEventList = filteredEventList;
    }

    /**
     * @param eventsViewOptionsCandidates the eventsViewOptionsCandidates to set
     */
    public void setEventsViewOptionsCandidates(List<ViewOptionsActiveHiddenListsEnum> eventsViewOptionsCandidates) {
        this.eventsViewOptionsCandidates = eventsViewOptionsCandidates;
    }

    /**
     * @param selectedEventView the selectedEventView to set
     */
    public void setSelectedEventView(ViewOptionsActiveHiddenListsEnum selectedEventView) {
        this.selectedEventView = selectedEventView;
    }


    /**
     * @param personCandidates the personCandidates to set
     */
    public void setPersonCandidates(List<Person> personCandidates) {
        this.personCandidates = personCandidates;
    }

    /**
     * @return the currentERGBOb
     */
    public IFace_EventRuleGoverned getCurrentERGBOb() {
        return currentERGBOb;
    }

    /**
     * @param currentERGBOb the currentERGBOb to set
     */
    public void setCurrentERGBOb(IFace_EventRuleGoverned currentERGBOb) {
        this.currentERGBOb = currentERGBOb;
    }

    /**
     * @return the eventList
     */
    public List<EventCnF> getEventList() {
        return eventList;
    }

    /**
     * @param eventList the eventList to set
     */
    public void setEventList(List<EventCnF> eventList) {
        this.eventList = eventList;
    }
    
    

}
