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
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.event.ActionEvent;

/**
 * The premier backing bean for universal events panel workflow.
 *
 * @author jurplel
 */
public class EventsBB extends BackingBeanUtils implements Serializable {
    private DomainEnum pageDomain;
    private boolean eventEditMode;
    
    private IFace_EventHolder currentEventHolder;
    private List<EventCnF> eventList = new ArrayList<>();
    private List<HumanLink> eventHumanLinkList;

    private ViewOptionsActiveHiddenListsEnum eventListFilterMode;

    private Map<EventType, List<EventCategory>> typeCategoryMap;
    
    // form logic stuff
    private String formNoteText;
    private long formEventDuration;

    // skeleton values used in the the new event form (subset of form logic stuff I guess)
    private EventCnF skeletonEvent;

    private boolean updateFieldsOnCategoryChange;

    private long skeletonDuration;
    private EventType skeletonType;

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
        typeCategoryMap = ec.assembleEventTypeCatMap_toEnact(pageDomain, currentEventHolder, getSessionBean().getSessUser());
    }


    /**
     * Grabs the current page event type from the sessionBean and uses that to populate
     * the currentEventHolder object with something we can grab events from!
     */
    public void updateEventHolder() {
        SessionBean sb = getSessionBean();

        pageDomain = sb.getSessEventsPageEventDomainRequest();
        switch (pageDomain) {
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
    
    /**
     * Retrieves a new copy of our current event
     */
    private void refreshCurrentEvent(){
        EventCoordinator ec = getEventCoordinator();
        if(currentEvent != null){
            try {
                currentEvent = ec.getEvent(currentEvent.getEventID());
                System.out.println("EventsBB.refreshCurrentEvent: Refreshed Event");
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
            updateEventList();
        }
    }
    
    
    /**
     * Listener to view current event
     * @param ev 
     */
    public void onViewEvent(EventCnF ev){
        currentEvent = ev;
    }

    /**
     * Listener for user requests to make an event either active or inactive
     */
    public void toggleEventActive() {
        EventCoordinator ec = getEventCoordinator();
        currentEvent.setActive(!currentEvent.isActive());
        try {
            ec.updateEvent(currentEvent, getSessionBean().getSessUser());
             getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Event ID " + currentEvent.getEventID() + " has been deactivated!", ""));
             refreshCurrentEvent();
        } catch (IntegrationException | BObStatusException | EventException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Could not deactivate event ID: " + currentEvent.getEventID() , ""));
             // in case we didn't get the update
             // toggle it back to what it was before the call
            currentEvent.setActive(!currentEvent.isActive());
        }
    }
    
    /**
     * Listener for when the user starts editing the person links
     * to this particular event
     */
    public void onManageEventPersonButtonChange(){
        System.out.println("EventsBB.onManageEventPersonButtonChange");
        
    }
    
    
    /**
     * Listener for user requests to start or end the event editing process
     * @param ev 
     */
    public void onEventEditModeToggleButtonPress(ActionEvent ev){
        System.out.println("EventsBB.onEventEditModeToggleButtonPress | edit mode: " + eventEditMode);
        if(eventEditMode){
            saveEventChanges();
            
        }
        eventEditMode = !eventEditMode;
    }
    
    /**
     * listener for user requests to cancel an even edit oepration
     * @param ev 
     */
    public void onEventEditCancelButtonChange(ActionEvent ev){
        System.out.println("EventBB.onEventEditCancelButtonChange");
        eventEditMode = !eventEditMode;
          getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Edit aborted--changes discarded", ""));
    
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
     * Listener for user requests to end the vent deactivation process.
     * @param ev 
     */
    public void onEventNukeCancelButtonChange(ActionEvent ev){
        System.out.println("EvntsBB.onEventNukeCancelButtonChange-Cancel!");
    }
  
    /**
     * listener for user requests to start the vent remove process
     * @param ev 
     */
    public void onEventRemoveInitButtonChange(ActionEvent ev){
        // do nothing yet
    }
    
    /**
     * Listener for user request to remove an event
     * @param ev 
     */
    public void onEventRemoveCommitButtonChange(ActionEvent ev){
        EventCoordinator ec = getEventCoordinator();
        try {
            ec.removeEvent(getCurrentEvent(), getSessionBean().getSessUser());
             getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Removed event ID " + getCurrentEvent().getEventID(), ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                ex.getMessage(), ""));
        }
        
    }
    
      
    /**
     * Listener for user requests to resurrect an event
     * @param ev 
     */
    public void onEventReactivateCommitButtonChange(ActionEvent ev){
        EventCoordinator ec = getEventCoordinator();
        try {
            currentEvent.setActive(true);
            ec.updateEvent(currentEvent, getSessionBean().getSessUser());
             getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Reactivated event ID " + getCurrentEvent().getEventID(), ""));
        } catch (IntegrationException | BObStatusException | EventException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                ex.getMessage(), ""));
        }
        
    }
    /**
     * Updates event's end time using a given duration from the start time
     */
    public void updateTimeEndFromDuration() {
        if (getCurrentEvent().getTimeStart() != null) {
            getCurrentEvent().setTimeEnd(getCurrentEvent().getTimeStart().plusMinutes(formEventDuration));
        }
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

    /**
     * Primary listener for creating a new event
     */
    public void createNewEvent() {
        if (pageDomain == null || currentEventHolder == null ||
                skeletonEvent == null || skeletonEvent.getCategory() == null) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Event must have a category ", ""));
            return;
        }

        EventCoordinator ec = getEventCoordinator();
        SessionBean sb = getSessionBean();

        // Add new event to database and to the event holder

        try {
            List<EventCnF> evlist = ec.addEvent(skeletonEvent, currentEventHolder, sb.getSessUser());

            if(evlist != null && !evlist.isEmpty()){
                for(EventCnF ev: evlist){
                    
                    getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully logged event with an ID of  " + ev.getEventID(), ""));

                }
            }
                
        } catch (BObStatusException | EventException | IntegrationException ex) {
            System.out.println("Failed to update new event with entered details:" + ex);
            return;
        }

        // Update session event holder variable so we can display new events

        switch (pageDomain) {
            case CODE_ENFORCEMENT:
                CECase ceCase = (CECase) currentEventHolder;
                sb.setSessCECase(ceCase);
                break;
            case OCCUPANCY:
                OccPeriod occPeriod = (OccPeriod) currentEventHolder;
                sb.setSessOccPeriodFromPeriodBase(occPeriod);
                break;
            case UNIVERSAL:
                System.out.println("EventsBB reached universal case in createNewEvent()--do something about this maybe?");
                break;
        }

        updateEventHolder();
    }

    public List<EventType> getListOfPotentialTypes() {
        List<EventType> keys = new ArrayList(typeCategoryMap.keySet());
        // Sort the keys so they are always in the same order
        keys.sort((EventType et1, EventType et2) -> et1.getLabel().compareTo(et2.getLabel()));

        // Set a default potentialType if there isn't already one
        if (skeletonType == null)
            skeletonType = keys.get(0);
        return keys;
    }

    public List<EventCategory> getListOfPotentialCategories() {
        if (skeletonEvent != null && skeletonType != null && typeCategoryMap.containsKey(skeletonType)) {
            List<EventCategory> eventCategories = typeCategoryMap.get(skeletonType);
            // Set a default potentialCategory if there isn't already one in this list
            if (!eventCategories.contains(skeletonEvent.getCategory())){
                skeletonEvent.setCategory(eventCategories.get(0));
                onCategoryChangeUpdateSkeletonEventMembers();
            }
            return eventCategories;
        } else {
            return new ArrayList();
        }
    }

    /**
     * Listener to start the whole event creation process!
     * @param holder
     */
    public void onEventAddInit(IFace_EventHolder holder) {
        currentEventHolder = holder;
        // Set potentialEvent to an empty event
        EventCoordinator ec = getEventCoordinator();

        try {
            skeletonEvent = ec.initEvent(currentEventHolder, null);
        } catch (BObStatusException | EventException ex) {
            System.out.println("Failed to initialize new event:" + ex);
            return;
        }

        // ...with this domain
        skeletonEvent.setDomain(pageDomain);

        // Set fields not included in potentialEvent to default values
        setUpdateFieldsOnCategoryChange(true);
        setSkeletonType(null);
        setSkeletonDuration(0);
    }


    /**
     * Sets fields of the skeleton event based on the default fields
     * of the skeleton event's category (if updateFieldsOnCategoryChange is set)
     */
    public void onCategoryChangeUpdateSkeletonEventMembers() {
        if (isUpdateFieldsOnCategoryChange() && skeletonEvent != null && getSkeletonEvent().getCategory() != null) {
            EventCategory category = skeletonEvent.getCategory();

            skeletonEvent.setTimeStart(LocalDateTime.now());
            skeletonDuration = category.getDefaultDurationMins();

            skeletonEvent.setDescription(category.getHostEventDescriptionSuggestedText());

            recalculateEndTime();
        }
    }
    /**
     * Sets end time of skeleton event based on the duration held in this class,
     * and the start time held in the skeleton event.
     */
    public void recalculateEndTime() {
        // Set end time of potential event to its start time + potential duration
        if (skeletonEvent == null || skeletonEvent.getTimeStart() == null){
            return;
        }

        skeletonEvent.setTimeEnd(skeletonEvent.getTimeStart().plusMinutes(skeletonDuration));
    }
    
    /**
     * Checks the session bean. if the session bean 
     * has queued an EventCnF for person list refresh, 
     * get a new copy of CurrentEvent with updated persons.
     * Then clear the session trigger.
     */
    private void checkForPersonListReloadTrigger(){
        if(currentEvent != null
                && getSessionBean().getSessHumanListRefreshedList() != null  ){
            
            // clear refresh trigger
            getSessionBean().setSessHumanListRefreshedList(null);
        }
    }
    
    
    
     
     /**
     * Listener for commencement of note writing process
     *
     * @param ev
     */
    public void onNoteInitButtonChange(ActionEvent ev) {
            formNoteText = new String();

    }

    /**
     * Listener for user requests to commit new note content to the current
     * Event
     *
     * @param ev
     */
    public void onNoteCommitButtonChange(ActionEvent ev) {
        SystemCoordinator sc = getSystemCoordinator();
        
        MessageBuilderParams mbp = new MessageBuilderParams();
        
        mbp.setCred(getSessionBean().getSessUser().getKeyCard());
        mbp.setExistingContent(currentEvent.getNotes());
        mbp.setNewMessageContent(getFormNoteText());
        mbp.setHeader("Event Note");
        mbp.setUser(getSessionBean().getSessUser());
        currentEvent.setNotes(sc.appendNoteBlock(mbp));
        
        try {
            sc.writeNotes(currentEvent, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succesfully appended note to event!", ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fatal error appending note; apologies!", ""));

        }
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
    
    /**
     * "Wrapper" member to avoid lots of calls to session bean to check for the trigger
     * event presence
     * @return the eventHumanLinkList
     */
    public List<HumanLink> getManagedEventHumanLinkList() {
        List<HumanLink> hll = getSessionBean().getSessHumanListRefreshedList();
        if(hll != null){
            currentEvent.setHumanLinkList(hll);
            // clear our refreshed list
            getSessionBean().setSessHumanListRefreshedList(null);
        }
        return currentEvent.getHumanLinkList();
       
    }
   
    /**
     * The primary getter
     * @return 
     */
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

    public EventCnF getSkeletonEvent() {
        return skeletonEvent;
    }

    public void setSkeletonEvent(EventCnF skeletonEvent) {
        this.skeletonEvent = skeletonEvent;
    }

    public long getSkeletonDuration() {
        return skeletonDuration;
    }

    public void setSkeletonDuration(long skeletonDuration) {
        this.skeletonDuration = skeletonDuration;
    }

    public EventType getSkeletonType() {
        return skeletonType;
    }

    public void setSkeletonType(EventType skeletonType) {
        this.skeletonType = skeletonType;
    }

    public boolean isUpdateFieldsOnCategoryChange() {
        return updateFieldsOnCategoryChange;
    }

    public void setUpdateFieldsOnCategoryChange(boolean updateFieldsOnCategoryChange) {
        this.updateFieldsOnCategoryChange = updateFieldsOnCategoryChange;
        onCategoryChangeUpdateSkeletonEventMembers();
    }

    /**
     * @return the eventEditMode
     */
    public boolean isEventEditMode() {
        return eventEditMode;
    }

    /**
     * @param eventEditMode the eventEditMode to set
     */
    public void setEventEditMode(boolean eventEditMode) {
        this.eventEditMode = eventEditMode;
    }

   

    /**
     * @param eventHumanLinkList the eventHumanLinkList to set
     */
    public void setEventHumanLinkList(List<HumanLink> eventHumanLinkList) {
        this.eventHumanLinkList = eventHumanLinkList;
    }
}
