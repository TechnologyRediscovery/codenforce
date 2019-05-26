/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.User;
import java.io.Serializable;

/**
 *
 * @author Sylvia Garland
 */
public class SearchParamsEventCECase extends SearchParams implements Serializable{
    
    private boolean useRespondedAtDateRange;
    
    private boolean filterByEventCategory;
    private EventCategory eventCategory;
    
    private boolean filterByEventType;
    private EventType evtType;
    
    
    private boolean filterByCaseID;
    private int caseId;
    
    private boolean filterByEventOwner;
    private User user;
  
    private boolean filterByPerson;
    private Person person;
    
    private boolean filterByActive;
    private boolean isActive;
    
    private boolean filterByHidden;
    private boolean isHidden;
    
    // main control for follow-up event request parameter activation
    private boolean filterByrequestsAction;
    private boolean requestsAction;
    
    // these search parameters are only valid for events that request
    // a follow-up action
    
    private boolean filterByHasResponseEvent;
    private boolean hasResponseEvent;

    // use the EventCategory and EventType fields above to query the 
    // event category or type of the requested follow-up event
    private boolean filterByRequestedResponseEventCat;
    
    // we have a User type memvar called user. These switches can
    // determine which db field the user is queried against
    private boolean filterByRequestor;
    private boolean filterByResponderIntended;
    private boolean filterByResponderActual;

    private boolean filterByRejectedRequest;
    private boolean rejectedRequest;
    
    
    
   public SearchParamsEventCECase(){
       
   }

    /**
     * @return the filterByEventCategory
     */
    public boolean isFilterByEventCategory() {
        return filterByEventCategory;
    }

    /**
     * @return the eventCategory
     */
    public EventCategory getEventCategory() {
        return eventCategory;
    }

    /**
     * @return the filterByCaseID
     */
    public boolean isFilterByCaseID() {
        return filterByCaseID;
    }

    /**
     * @return the caseId
     */
    public int getCaseId() {
        return caseId;
    }

    /**
     * @return the filterByEventOwner
     */
    public boolean isFilterByEventOwner() {
        return filterByEventOwner;
    }

    /**
     * @return the ownerUserID
     */
    public User getUser() {
        return user;
    }

    /**
     * @return the filterByActive
     */
    public boolean isFilterByActive() {
        return filterByActive;
    }

    /**
     * @return the isActive
     */
    public boolean isIsActive() {
        return isActive;
    }

    /**
     * @return the filterByrequestsAction
     */
    public boolean isFilterByrequestsAction() {
        return filterByrequestsAction;
    }

    /**
     * @return the requestsAction
     */
    public boolean isRequestsAction() {
        return requestsAction;
    }

    /**
     * @return the filterByHidden
     */
    public boolean isFilterByHidden() {
        return filterByHidden;
    }

    /**
     * @return the isHidden
     */
    public boolean isIsHidden() {
        return isHidden;
    }

    /**
     * @return the filterByPerson
     */
    public boolean isFilterByPerson() {
        return filterByPerson;
    }

    
    /**
     * @return the useRespondedAtDateRange
     */
    public boolean isUseRespondedAtDateRange() {
        return useRespondedAtDateRange;
    }

    /**
     * @param filterByEventCategory the filterByEventCategory to set
     */
    public void setFilterByEventCategory(boolean filterByEventCategory) {
        this.filterByEventCategory = filterByEventCategory;
    }

    /**
     * @param eventCategory the eventCategory to set
     */
    public void setEventCategory(EventCategory eventCategory) {
        this.eventCategory = eventCategory;
    }

    /**
     * @param filterByCaseID the filterByCaseID to set
     */
    public void setFilterByCaseID(boolean filterByCaseID) {
        this.filterByCaseID = filterByCaseID;
    }

    /**
     * @param caseId the caseId to set
     */
    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    /**
     * @param filterByEventOwner the filterByEventOwner to set
     */
    public void setFilterByEventOwner(boolean filterByEventOwner) {
        this.filterByEventOwner = filterByEventOwner;
    }

    /**
     * @param ownerUserID the ownerUserID to set
     */
    public void setOwnerUserID(User ownerUserID) {
        this.setUser(ownerUserID);
    }

    /**
     * @param filterByActive the filterByActive to set
     */
    public void setFilterByActive(boolean filterByActive) {
        this.filterByActive = filterByActive;
    }

    /**
     * @param isActive the isActive to set
     */
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * @param filterByrequestsAction the filterByrequestsAction to set
     */
    public void setFilterByrequestsAction(boolean filterByrequestsAction) {
        this.filterByrequestsAction = filterByrequestsAction;
    }

    /**
     * @param requestsAction the requestsAction to set
     */
    public void setRequestsAction(boolean requestsAction) {
        this.requestsAction = requestsAction;
    }

    /**
     * @param filterByHidden the filterByHidden to set
     */
    public void setFilterByHidden(boolean filterByHidden) {
        this.filterByHidden = filterByHidden;
    }

    /**
     * @param isHidden the isHidden to set
     */
    public void setIsHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    /**
     * @param filterByPerson the filterByPerson to set
     */
    public void setFilterByPerson(boolean filterByPerson) {
        this.filterByPerson = filterByPerson;
    }

   
    /**
     * @param useRespondedAtDateRange the useRespondedAtDateRange to set
     */
    public void setUseRespondedAtDateRange(boolean useRespondedAtDateRange) {
        this.useRespondedAtDateRange = useRespondedAtDateRange;
    }

    /**
     * @return the filterByEventType
     */
    public boolean isFilterByEventType() {
        return filterByEventType;
    }

    /**
     * @param filterByEventType the filterByEventType to set
     */
    public void setFilterByEventType(boolean filterByEventType) {
        this.filterByEventType = filterByEventType;
    }

    /**
     * @return the filterByHasResponseEvent
     */
    public boolean isFilterByHasResponseEvent() {
        return filterByHasResponseEvent;
    }

    /**
     * @return the hasResponseEvent
     */
    public boolean isHasResponseEvent() {
        return hasResponseEvent;
    }

    /**
     * @param filterByHasResponseEvent the filterByHasResponseEvent to set
     */
    public void setFilterByHasResponseEvent(boolean filterByHasResponseEvent) {
        this.filterByHasResponseEvent = filterByHasResponseEvent;
    }

    /**
     * @param hasResponseEvent the hasResponseEvent to set
     */
    public void setHasResponseEvent(boolean hasResponseEvent) {
        this.hasResponseEvent = hasResponseEvent;
    }

    /**
     * @return the evtType
     */
    public EventType getEvtType() {
        return evtType;
    }

    /**
     * @param evtType the evtType to set
     */
    public void setEvtType(EventType evtType) {
        this.evtType = evtType;
    }

    /**
     * @return the person
     */
    public Person getPerson() {
        return person;
    }

    /**
     * @param person the person to set
     */
    public void setPerson(Person person) {
        this.person = person;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

   

    /**
     * @return the filterByRejectedRequest
     */
    public boolean isFilterByRejectedRequest() {
        return filterByRejectedRequest;
    }

    /**
     * @param filterByRejectedRequest the filterByRejectedRequest to set
     */
    public void setFilterByRejectedRequest(boolean filterByRejectedRequest) {
        this.filterByRejectedRequest = filterByRejectedRequest;
    }

    /**
     * @return the rejectedRequest
     */
    public boolean isRejectedRequest() {
        return rejectedRequest;
    }

    /**
     * @param rejectedRequest the rejectedRequest to set
     */
    public void setRejectedRequest(boolean rejectedRequest) {
        this.rejectedRequest = rejectedRequest;
    }

    /**
     * @return the filterByRequestedResponseEventCat
     */
    public boolean isFilterByRequestedResponseEventCat() {
        return filterByRequestedResponseEventCat;
    }

    
    

    /**
     * @return the filterByRequestor
     */
    public boolean isFilterByRequestor() {
        return filterByRequestor;
    }

    /**
     * @return the filterByResponderIntended
     */
    public boolean isFilterByResponderIntended() {
        return filterByResponderIntended;
    }

    /**
     * @return the filterByResponderActual
     */
    public boolean isFilterByResponderActual() {
        return filterByResponderActual;
    }

    /**
     * @param filterByRequestedResponseEventCat the filterByRequestedResponseEventCat to set
     */
    public void setFilterByRequestedResponseEventCat(boolean filterByRequestedResponseEventCat) {
        this.filterByRequestedResponseEventCat = filterByRequestedResponseEventCat;
    }


    /**
     * @param filterByRequestor the filterByRequestor to set
     */
    public void setFilterByRequestor(boolean filterByRequestor) {
        this.filterByRequestor = filterByRequestor;
    }

    /**
     * @param filterByResponderIntended the filterByResponderIntended to set
     */
    public void setFilterByResponderIntended(boolean filterByResponderIntended) {
        this.filterByResponderIntended = filterByResponderIntended;
    }

    /**
     * @param filterByResponderActual the filterByResponderActual to set
     */
    public void setFilterByResponderActual(boolean filterByResponderActual) {
        this.filterByResponderActual = filterByResponderActual;
    }
   
   
   
   

   
   

   
    
}
