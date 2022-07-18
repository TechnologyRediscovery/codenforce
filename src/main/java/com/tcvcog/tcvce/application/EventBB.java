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
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

/**
 * The premier backing bean for universal events panel workflow.
 *
 * @author jurplel and Ellen Bascomb of Apartment 31Y
 */
public class EventBB extends BackingBeanUtils implements Serializable {
    private DomainEnum pageDomain;
    private boolean eventEditMode;
    private EventCnF currentEvent;
    
    private IFace_EventHolder currentEventHolder;
    private String eventListComponentForRefreshTrigger;

    private Map<EventType, List<EventCategory>> typeCategoryMap;
    
    // form logic stuff
    private String formNoteText;
    private long formEventDuration;

    // skeleton values used in the the new event form (subset of form logic stuff I guess)
    private EventCnF skeletonEvent;

    private boolean updateFieldsOnCategoryChange;

    private long skeletonDuration;
    private EventType skeletonType;

    public EventBB() {}

    @PostConstruct
    public void initBean() {
        SessionBean sb = getSessionBean();
        EventCoordinator ec = getEventCoordinator();

        // Find event holder and setup event list
        loadSessionEventHolder();

        // Populate map of event types and categories
        typeCategoryMap = ec.assembleEventTypeCatMap_toEnact(pageDomain, currentEventHolder, getSessionBean().getSessUser());
    }


    /**
     * Grabs the current page event type from the sessionBean and uses that to populate
     * the currentEventHolder object with something we can grab events from!
     */
    public void loadSessionEventHolder() {
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
                System.out.println("eventBB reached universal case in updateEventHolder()--do something about this maybe?");
                break;
        }
    }
    
    /**
     * Grabs a new set of events from the DB and sends it to the session bean
     * and the UI will trigger the event holder's management BB to get a new
     * copy of the session event list and display that to the user
     */
    public void refreshEventHolderListAndTriggerSessionReload(){
        EventCoordinator ec = getEventCoordinator();
        if(currentEventHolder != null){
            try {
                System.out.println("EventBB.refreshEventHolderListAndTriggerSessionReload | eventHolder ID: " + currentEventHolder.getBObID() );
                getSessionBean().setSessEventListForRefreshUptake(ec.getEventList(currentEventHolder));
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Upded session event holder", ""));
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        ex.getMessage(), ""));
                
            }
        }
    }

    /**
     * Retrieves a new copy of our current event
     */
    private void refreshCurrentEvent(){
        EventCoordinator ec = getEventCoordinator();
        if(currentEvent != null){
            try {
                currentEvent = ec.getEvent(currentEvent.getEventID());
                System.out.println("eventBB.refreshCurrentEvent: Refreshed Event");
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        
        }
    }
    
    
    /**
     * Listener to view current event
     * @param ev 
     */
    public void onViewEvent(EventCnF ev){
        currentEvent = ev;
        extractEventListComponentForRefresh();
        refreshCurrentEvent();
    }
    
    private void extractEventListComponentForRefresh(){
         
           eventListComponentForRefreshTrigger = 
                FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getRequestParameterMap()
                        .get("event-list-component-to-update");
           System.out.println("EventBB.extractEventListComponentForRefresh | " + eventListComponentForRefreshTrigger);
        
        
    }

   
    
    /**
     * Listener for when the user starts editing the person links
     * to this particular event
     */
    public void onManageEventPersonButtonChange(){
        System.out.println("eventBB.onManageEventPersonButtonChange");
        
    }
    
    
    /**
     * Listener for user requests to start or end the event editing process
     * @param ev 
     */
    public void onEventEditModeToggleButtonPress(ActionEvent ev){
        System.out.println("eventBB.onEventEditModeToggleButtonPress | edit mode: " + eventEditMode);
        if(eventEditMode){
            if(currentEvent != null && currentEvent.getEventID() != 0){
                
                eventUpdateCommit();
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Success: Event update ID:"+getCurrentEvent().getEventID(), ""));
            } else {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Mostly fatal error: null current event or evid=0: error code EV007", ""));
            }
            
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
    public void eventUpdateCommit() {
        EventCoordinator ec = getEventCoordinator();

        try {
            ec.updateEvent(currentEvent, getSessionBean().getSessUser());
            System.out.println("eventBB.saveEventChanges successful");
            refreshCurrentEvent();
            refreshEventHolderListAndTriggerSessionReload();
            // Set backup copy in case of failure if saving to database succeeds
//            lastSavedSelectedEvent = new EventCnF(currentEvent);
        } catch (IntegrationException | BObStatusException | EventException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
            System.out.println("eventBB.saveEventChanges failure");
            // don't turn off editing yet!
//            discardEventChanges();
        }
    }

    /**
     * This method will discard the changes to the current working event and
     * set its value to that of the event present in the last successful save.
     */
    public void discardEventChanges() {
        System.out.println("eventBB.discardEventChanges");
        eventEditMode = false;
    }
    
    /**
     * listener for user requests to start the vent remove process that 
     * applies only to the bean's current event. I won't just take
     * in any old random event.
     * @param ev 
     */
    public void onEventDeactivateInit(ActionEvent ev){
        if(currentEvent != null){
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Beginning deactivation of event ID: " + currentEvent.getEventID(), ""));
            System.out.println("eventBB.onEventDeactivate ID: " + currentEvent.getEventID());
            
        } else {
            System.out.println("eventBB.onEventDeactivate | current event null");
            
        }
    }
    
    /**
     * Listener for user request to remove an event
     * @param ev 
     */
    public void onEventDeactivateCommit(ActionEvent ev){
        EventCoordinator ec = getEventCoordinator();
        try {
            ec.deactivateEvent(getCurrentEvent(), getSessionBean().getSessUser());
            refreshCurrentEvent();
            refreshEventHolderListAndTriggerSessionReload();
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
     * Listener for user cancels to event deac
     * @param ev 
     */
    public void onEventDeactivateAbort(ActionEvent ev){
        System.out.println("EventBB.onEventDeactivateAbort");
    }
    
      
    /**
     * Listener for user requests to resurrect an event
     * @param ev 
     */
    public void onEventReactivateCommitButtonChange(ActionEvent ev){
        EventCoordinator ec = getEventCoordinator();
        try {
            ec.reactivateEvent(currentEvent, getSessionBean().getSessUser());
            refreshCurrentEvent();
            refreshEventHolderListAndTriggerSessionReload();
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


    //
    // New event stuff
    //

    /**
     * Primary listener for creating a new event
     */
    public void eventAddCommit() {
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

        refreshCurrentEvent();
        refreshEventHolderListAndTriggerSessionReload();
        
    }

    /**
     * Special getter for event reconfiguration and add
     * @return 
     */
    public List<EventType> getListOfPotentialTypes() {
        List<EventType> keys = new ArrayList(typeCategoryMap.keySet());
        // Sort the keys so they are always in the same order
        keys.sort((EventType et1, EventType et2) -> et1.getLabel().compareTo(et2.getLabel()));

        // Set a default potentialType if there isn't already one
        if (skeletonType == null)
            skeletonType = keys.get(0);
        return keys;
    }

    /**
     * Special getter for event config
     * @return 
     */
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
        try {
            if(currentEvent != null){

            mbp.setCred(getSessionBean().getSessUser().getKeyCard());
            mbp.setExistingContent(currentEvent.getNotes());
            mbp.setNewMessageContent(getFormNoteText());
            mbp.setHeader("Event Note");
            mbp.setUser(getSessionBean().getSessUser());
            currentEvent.setNotes(sc.appendNoteBlock(mbp));
            } else {
                throw new BObStatusException("No current event on which to write note");
            }
        
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

    public IFace_EventHolder getCurrentEventHolder() {
        return currentEventHolder;
    }

    
    /**
     * "Wrapper" member to avoid lots of calls to session bean to check for the trigger
     * event presence
     * @return the eventHumanLinkList
     */
    public List<HumanLink> getManagedEventHumanLinkList() {
        List<HumanLink> hll = getSessionBean().getSessHumanListRefreshedList();
        if(currentEvent != null){
            if(hll != null){
                currentEvent.setHumanLinkList(hll);
                // clear our refreshed list
                getSessionBean().setSessHumanListRefreshedList(null);
            }
            return currentEvent.getHumanLinkList();
        } else {
            return new ArrayList<>();
        }
       
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

//        this.lastSavedSelectedEvent = new EventCnF(this.currentEvent);
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
     * @return the eventListComponentForRefreshTrigger
     */
    public String getEventListComponentForRefreshTrigger() {
        return eventListComponentForRefreshTrigger;
    }

    /**
     * @param eventListComponentForRefreshTrigger the eventListComponentForRefreshTrigger to set
     */
    public void setEventListComponentForRefreshTrigger(String eventListComponentForRefreshTrigger) {
        this.eventListComponentForRefreshTrigger = eventListComponentForRefreshTrigger;
    }
}
