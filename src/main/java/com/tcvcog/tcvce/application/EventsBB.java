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

import com.tcvcog.tcvce.application.interfaces.IFace_ActivatableBOB;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.util.DateTimeUtil;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

/**
 * The premier backing bean for universal events panel workflow.
 *
 * @author jurplel
 */
public class EventsBB extends BackingBeanUtils implements Serializable {
    private EventDomainEnum pageEventDomain;

    private IFace_EventHolder currentEventHolder;
    private IFace_ActivatableBOB currentEventHolderBOB;
    private List<EventCnF> eventList = new ArrayList<>();

    private ViewOptionsActiveHiddenListsEnum eventListFilterMode;

    private Map<EventType, List<EventCategory>> typeCategoryMap;

    // form logic stuff
    private String formNoteText;
    private long formEventDuration;

    // potential values used in the the new event form (subset of form logic stuff I guess)
    private boolean updateFieldsOnCategoryChange;

    private LocalDateTime potentialTimeStart;
    private long potentialDuration;

    private EventType potentialType;
    private EventCategory potentialCategory;
    private String potentialDescription;

    // Single event that is currently used for editing/viewing
    // and also a copy of its last saved state to restore on edit cancel
    private EventCnF lastSavedSelectedEvent;
    private EventCnF currentEvent;

    public EventsBB() {}

    @PostConstruct
    public void initBean() {
        SessionBean sb = getSessionBean();
        EventCoordinator ec = getEventCoordinator();

        // Default events list filter mode
        setEventListFilterMode(ec.determineDefaultEventView(sb.getSessUser()));

        // Find event holder and setup event list
        updateEventHolder();

        // Populate map of event types and categories
        typeCategoryMap = ec.assembleEventTypeCatMap_toEnact(pageEventDomain, currentEventHolder, getSessionBean().getSessUser());
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
                currentEventHolderBOB = sb.getSessCECase();
                break;
            case OCCUPANCY:
                currentEventHolder = sb.getSessOccPeriod();
                currentEventHolderBOB = sb.getSessOccPeriod();
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
        currentEvent.setActive(!currentEvent.isActive());
        try {
            ec.updateEvent(currentEvent, getSessionBean().getSessUser());
        } catch (IntegrationException | BObStatusException | EventException ex) {
            System.out.println(ex);
            currentEvent.setActive(!currentEvent.isActive());
        }
    }

    /**
     * This method attempts to update the database entry for the selectedEvent.
     * It will fail in certain conditions, in which case the selectedEvent is returned to
     * a backup made before any current unsaved changes.
     */
    public void saveEventChanges() {
        EventCoordinator ec = getEventCoordinator();

        try {
            ec.updateEvent(currentEvent, getSessionBean().getSessUser());

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Save successful on event ID: " + currentEvent.getEventID(), ""));
            System.out.println("EventsBB.saveEventChanges successful");

            // Set backup copy in case of failure if saving to database succeeds
            lastSavedSelectedEvent = new EventCnF(currentEvent);
        } catch (IntegrationException | BObStatusException | EventException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
            System.out.println("EventsBB.saveEventChanges failure");

            // Restore working copy of occ period to last working one if saving to database fails.
            discardEventChanges();
        }
    }

    /**
     * This method will discard the changes to the current working event and
     * set its value to that of the event present in the last successful save.
     */
    public void discardEventChanges() {
        System.out.println("EventsBB.discardEventChanges");

        setCurrentEvent(new EventCnF(lastSavedSelectedEvent));
    }

    /**
     * Updates event's end time using a given duration from the start time
     */
    public void updateTimeEndFromDuration() {
        if (getCurrentEvent().getTimeStart() != null) {
            getCurrentEvent().setTimeEnd(getCurrentEvent().getTimeStart().plusMinutes(formEventDuration));
        }
    }

    public void clearFormNoteText() {
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
    // New event stuff
    //

    // Some of this stuff I feel could be moved up the chain for sure (e.g. to addEvent)
    // Also maybe missing some faces messages here for failure states i guess
    public void createNewEvent() {
        System.out.println("called?");
        if (pageEventDomain == null || currentEventHolder == null || potentialCategory == null) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Event must have a category ", ""));
            return;
        }


        // Create the event and add it to the database
        EventCoordinator ec = getEventCoordinator();

        EventCnF newEvent;
        try {
            newEvent = ec.initEvent(currentEventHolder, potentialCategory);
        } catch (BObStatusException | EventException ex) {
            System.out.println("Failed to initialize new event:" + ex);
            return;
        }

        newEvent.setDomain(pageEventDomain);

        newEvent.setTimeStart(getPotentialTimeStart());
        newEvent.setTimeEnd(getPotentialTimeEnd());

        newEvent.setDescription(potentialDescription);

        try {
            ec.addEvent(newEvent, currentEventHolder, getSessionBean().getSessUser());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Successfully logged event with an ID " + newEvent.getEventID() + " ", ""));
        } catch (BObStatusException | EventException | IntegrationException ex) {
            System.out.println("Failed to update new event with entered details:" + ex);
            return;
        }

        // Reload current event holder in session bean so we can get the new events from it!
        // This is kinda slow and should ultimately be replaced with something more elegant,
        // maybe on the session bean's side...
        try {
            getSessionBean().activateSessionObject(currentEventHolderBOB);
        } catch (BObStatusException ex) {
            System.out.println("Failed to activate session object:" + ex);
        }

        updateEventHolder();
    }

    public List<EventType> getListOfPotentialTypes() {
        List<EventType> keys = new ArrayList(typeCategoryMap.keySet());
        // Sort the keys so they are always in the same order
        keys.sort((EventType et1, EventType et2) -> et1.getLabel().compareTo(et2.getLabel()));

        // Set a default potentialType if there isn't already one
        if (potentialType == null)
            potentialType = keys.get(0);
        return keys;
    }

    public List<EventCategory> getListOfPotentialCategories() {
        if (potentialType != null && typeCategoryMap.containsKey(potentialType)) {
            List<EventCategory> eventCategories = typeCategoryMap.get(potentialType);
            // Set a default potentialCategory if there isn't already one in this list
            if (!eventCategories.contains(potentialCategory))
                potentialCategory = eventCategories.get(0);
            return eventCategories;
        } else {
            return new ArrayList();
        }
    }

    public void resetPotentialEvent() {
        setUpdateFieldsOnCategoryChange(true);
        setPotentialTimeStart(LocalDateTime.now());
        setPotentialDuration(0);

        setPotentialType(null);
        setPotentialCategory(null);
        setPotentialDescription("");
    }

    public LocalDateTime getPotentialTimeEnd() {
        if (getPotentialTimeStart() == null)
            return null;

        return getPotentialTimeStart().plusMinutes(potentialDuration);
    }

    public java.util.Date getPotentialTimeEndUtilDate() {
        return DateTimeUtil.convertUtilDate(getPotentialTimeEnd());
    }

    //
    // Mostly boring getters and setters start here.
    //

    public List<EventCnF> getEventList() {
        return eventList;
    }

    public IFace_EventHolder getCurrentEventHolder() {
        return currentEventHolder;
    }

    public ViewOptionsActiveHiddenListsEnum getEventListFilterMode() {
        return eventListFilterMode;
    }

    // Not completely boring
    public void setEventListFilterMode(ViewOptionsActiveHiddenListsEnum eventListFilterMode) {
        this.eventListFilterMode = eventListFilterMode;
        updateEventList();
    }

    public EventCnF getCurrentEvent() {
        return currentEvent;
    }

    // Not boring
    public void setCurrentEvent(EventCnF currentEvent) {
        this.currentEvent = currentEvent;
        // Calculate duration for forms
        if (currentEvent.getTimeStart() != null && currentEvent.getTimeEnd() != null)
            formEventDuration = ChronoUnit.MINUTES.between(currentEvent.getTimeStart(), currentEvent.getTimeEnd());

        this.lastSavedSelectedEvent = new EventCnF(this.currentEvent);
    }

    public String getFormNoteText() {
        return formNoteText;
    }

    public void setFormNoteText(String formNoteText) {
        this.formNoteText = formNoteText;
    }

    public long getFormEventDuration() {
        return formEventDuration;
    }

    public void setFormEventDuration(long formEventDuration) {
        this.formEventDuration = formEventDuration;
    }

    public LocalDateTime getPotentialTimeStart() {
        return potentialTimeStart;
    }

    public java.util.Date getPotentialTimeStartUtilDate() {
        return DateTimeUtil.convertUtilDate(potentialTimeStart);
    }

    public void setPotentialTimeStart(LocalDateTime potentialTimeStart) {
        this.potentialTimeStart = potentialTimeStart;
    }

    public void setPotentialTimeStartUtilDate(java.util.Date potentialTimeStart) {
        this.potentialTimeStart = DateTimeUtil.convertUtilDate(potentialTimeStart);
    }

    public long getPotentialDuration() {
        return potentialDuration;
    }

    public void setPotentialDuration(long potentialDuration) {
        this.potentialDuration = potentialDuration;
    }

    public EventType getPotentialType() {
        return potentialType;
    }

    public void setPotentialType(EventType potentialType) {
        this.potentialType = potentialType;
    }

    public EventCategory getPotentialCategory() {
        return potentialCategory;
    }

    public void setPotentialCategory(EventCategory potentialCategory) {
        this.potentialCategory = potentialCategory;
        if (isUpdateFieldsOnCategoryChange() && getPotentialCategory() != null) {
            setPotentialTimeStart(LocalDateTime.now());
            setPotentialDuration(getPotentialCategory().getDefaultDurationMins());
            setPotentialDescription(getPotentialCategory().getHostEventDescriptionSuggestedText());
        }
    }

    public boolean isUpdateFieldsOnCategoryChange() {
        return updateFieldsOnCategoryChange;
    }

    public void setUpdateFieldsOnCategoryChange(boolean updateFieldsOnCategoryChange) {
        this.updateFieldsOnCategoryChange = updateFieldsOnCategoryChange;
    }

    public String getPotentialDescription() {
        return potentialDescription;
    }

    public void setPotentialDescription(String potentialDescription) {
        this.potentialDescription = potentialDescription;
    }
}
