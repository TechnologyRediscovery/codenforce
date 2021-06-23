/*
 * Copyright (C) 2021 Technology Rediscovery LLC
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
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventDomainEnum;
import com.tcvcog.tcvce.entities.IFace_EventHolder;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * The premier backing bean for universal events panel workflow.
 *
 * @author jurplel
 */
public class EventsBB extends BackingBeanUtils implements Serializable {

    private String formNoteText;

    private EventDomainEnum pageEventDomain;
    private IFace_EventHolder currentEventHolder;
    private List<EventCnF> eventList = new ArrayList<>();

    private ViewOptionsActiveHiddenListsEnum eventListFilterMode;

    // Single selected event for editing and viewing
    private EventCnF selectedEvent;

    public EventsBB() {}

    @PostConstruct
    public void initBean() {
        SessionBean sb = getSessionBean();
        EventCoordinator ec = getEventCoordinator();

        // Default events list filter mode
        setEventListFilterMode(ec.determineDefaultEventView(sb.getSessUser()));

        // Find event holder and setup event list
        updateEventHolder();
    }

    /**
     * Grabs the current page event type from the sessionBean and uses that to populate
     * the currentEventHolder object with something we can grab events from!
     */
    public void updateEventHolder() {
        SessionBean sb = getSessionBean();

        pageEventDomain = sb.getSessEventsPageEventDomainRequest();
        switch (pageEventDomain) {
            case CODE_ENFORCEMENT:
                currentEventHolder = sb.getSessCECase();
                break;
            case OCCUPANCY:
                currentEventHolder = sb.getSessOccPeriod();
                break;
            case UNIVERSAL:
                System.out.println("EventsBB reached universal case in updateEventHolder()--do something about this maybe?");
                break;
        }
        updateEventList();
    }

    /**
     * Repopulates eventList parameter with the latest and greatest events from
     * our special guest the event-containing object.
     * This method also filters the events according to eventListFilterMode
     */
    public void updateEventList() {
        if (currentEventHolder == null)
            return;

        eventList.clear();
        if (eventListFilterMode == null)
            eventList.addAll(currentEventHolder.getEventList());
        else
            eventList.addAll(currentEventHolder.getEventList(eventListFilterMode));

    }

    public void toggleEventActive() {
        EventCoordinator ec = getEventCoordinator();
        selectedEvent.setActive(!selectedEvent.isActive());
        try {
            ec.updateEvent(selectedEvent, getSessionBean().getSessUser());
        } catch (IntegrationException | BObStatusException | EventException ex) {
            System.out.println(ex);
            selectedEvent.setActive(!selectedEvent.isActive());
        }
    }

    public void newEvent() {
//        if (pageEventDomain == null)
//            return;
//
//        EventCoordinator ec = getEventCoordinator();
//
//        EventCnF newEvent;
//        try {
//            newEvent = ec.initEvent(currentEventHolder, null);
//        } catch (BObStatusException | EventException ex) {
//            System.out.println(ex);
//            return;
//        }
//
//        getSessionBean().setSessEvent(newEvent);
//        newEvent.setCeCaseID(currentCase.getBObID());
//
//
//        switch (pageEventDomain) {
//            case CODE_ENFORCEMENT:
//                currentEventHolder = sb.getSessCECase();
//                break;
//            case OCCUPANCY:
//                newEvent.setCeCaseID(currentEventHolder.getBObID());
//                break;
//            case UNIVERSAL:
//                System.out.println("EventsBB reached universal case in newEvent()--really seems like you *should* do something about this");
//                break;
//        }
//
//        selectedEvent = newEvent;
    }

    //
    // Getter and setter style methods that are not as straightforward
    // or do not point to a specific value start here.
    //

    public void clearFormNoteText(ActionEvent ev) {
        formNoteText = new String();
    }

    public int getEventListSize() {
        int size = 0;
        if (eventList != null)
            size = eventList.size();

        return size;
    }

    public List<ViewOptionsActiveHiddenListsEnum> getEventListFilterModes() {
        return Arrays.asList(ViewOptionsActiveHiddenListsEnum.values());
    }

    //
    // Mostly boring getters and setters start here.
    //

    public List<EventCnF> getEventList() {
        return eventList;
    }


    public ViewOptionsActiveHiddenListsEnum getEventListFilterMode() {
        return eventListFilterMode;
    }

    public void setEventListFilterMode(ViewOptionsActiveHiddenListsEnum eventListFilterMode) {
        this.eventListFilterMode = eventListFilterMode;
        updateEventList();
    }

    public EventCnF getSelectedEvent() {
        return selectedEvent;
    }

    public void setSelectedEvent(EventCnF selectedEvent) {
        this.selectedEvent = selectedEvent;
    }
}
