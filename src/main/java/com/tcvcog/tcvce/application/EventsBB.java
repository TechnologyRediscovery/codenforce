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
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public class EventsBB extends BackingBeanUtils implements Serializable{

    private CECaseDataHeavy currentCase;
    private OccPeriodDataHeavy currentPeriod;
    
    private List<EventCategory> eventCategoryList;
    private EventCategory eventCategorySelected;
    
    private List<EventType> eventTypeList;
    private EventType eventTypeSelected;
    
    
    /**
     * Creates a new instance of EventsBB
     */
    public EventsBB() {
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
        EventCoordinator ec = getEventCoordinator();
        
        EventCnF ev = null;
        if (eventCategorySelected != null) {

            try {
                
                ev = ec.initEvent(currentCase, eventCategorySelected);
                ev.setDiscloseToMunicipality(true);
                ev.setDiscloseToPublic(false);
            } catch (BObStatusException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            } catch (EventException ex) {
                Logger.getLogger(EventSearchBB.class.getName()).log(Level.SEVERE, null, ex);
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
        getSelectedEvent().setCeCaseID(getCurrentCase().getCaseID());
        getSelectedEvent().setOwner(getSessionBean().getSessUser());
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

   
    public void commitEventEdits(ActionEvent ev) {
        EventCoordinator ec = getEventCoordinator();
//        currentCase.getEventList().remove(selectedEvent);
        try {
            ec.editEvent(getSelectedEvent());
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

    public void editEvent(EventCnF ev) {
        selectedEvent = ev;
    }
    
    

}
