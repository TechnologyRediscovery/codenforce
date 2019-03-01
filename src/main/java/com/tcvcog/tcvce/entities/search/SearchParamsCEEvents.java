/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Person;
import java.io.Serializable;

/**
 *
 * @author Sylvia Garland
 */
public class SearchParamsCEEvents extends SearchParams implements Serializable{
    
    private boolean filterByEventCategory;
    private EventCategory eventCategory;
    
    private boolean filterByEventType;
    private EventType evtType;
    
    private boolean filterByCaseID;
    private int caseId;
    
    private boolean filterByEventOwner;
    private int ownerUserID;   
  
    private boolean filterByPerson;
    private Person person;
    
    private boolean filterByActive;
    private boolean isActive;
    
    private boolean filterByRequiresViewConfirmation;
    private boolean isViewConfirmationRequired;
    
    private boolean filterByHidden;
    private boolean isHidden;
    
    private boolean filterByViewed;
    private boolean isViewed;
    
    
    private boolean filterByViewConfirmedAtDateRange;
    
   public SearchParamsCEEvents(){
       
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
    public int getOwnerUserID() {
        return ownerUserID;
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
     * @return the filterByRequiresViewConfirmation
     */
    public boolean isFilterByRequiresViewConfirmation() {
        return filterByRequiresViewConfirmation;
    }

    /**
     * @return the isViewConfirmationRequired
     */
    public boolean isIsViewConfirmationRequired() {
        return isViewConfirmationRequired;
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
     * @return the filterByViewConfirmedAtDateRange
     */
    public boolean isFilterByViewConfirmedAtDateRange() {
        return filterByViewConfirmedAtDateRange;
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
    public void setOwnerUserID(int ownerUserID) {
        this.ownerUserID = ownerUserID;
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
     * @param filterByRequiresViewConfirmation the filterByRequiresViewConfirmation to set
     */
    public void setFilterByRequiresViewConfirmation(boolean filterByRequiresViewConfirmation) {
        this.filterByRequiresViewConfirmation = filterByRequiresViewConfirmation;
    }

    /**
     * @param isViewConfirmationRequired the isViewConfirmationRequired to set
     */
    public void setIsViewConfirmationRequired(boolean isViewConfirmationRequired) {
        this.isViewConfirmationRequired = isViewConfirmationRequired;
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
     * @param filterByViewConfirmedAtDateRange the filterByViewConfirmedAtDateRange to set
     */
    public void setFilterByViewConfirmedAtDateRange(boolean filterByViewConfirmedAtDateRange) {
        this.filterByViewConfirmedAtDateRange = filterByViewConfirmedAtDateRange;
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
     * @return the filterByViewed
     */
    public boolean isFilterByViewed() {
        return filterByViewed;
    }

    /**
     * @return the isViewed
     */
    public boolean isIsViewed() {
        return isViewed;
    }

    /**
     * @param filterByViewed the filterByViewed to set
     */
    public void setFilterByViewed(boolean filterByViewed) {
        this.filterByViewed = filterByViewed;
    }

    /**
     * @param isViewed the isViewed to set
     */
    public void setIsViewed(boolean isViewed) {
        this.isViewed = isViewed;
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
   
   
   
   

   
   

   
    
}
