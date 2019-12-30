/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventDomainEnum;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.User;
import java.io.Serializable;

/**
 *
 * @author Sylvia Garland
 */
public class        SearchParamsEvent 
        extends     SearchParams 
        implements  Serializable{
    
    private boolean useRespondedAtDateRange;
    
    private boolean filterByEventCategory;
    private EventCategory eventCategory;
    
    private boolean filterByEventType;
    private EventType evtType;
    
    protected boolean filterByEventDomain;
    protected EventDomainEnum domain;
    
    private boolean filterByCaseID;
    private int caseId;
    
    private boolean filterByEventOwner;
    private int userID;
  
    private boolean filterByPerson;
    private Person person;
    
    private boolean filterByActive;
    private boolean isActive;
    
    private boolean filterByHidden;
    private boolean isHidden;
    
   public SearchParamsEvent(){
       
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
     * @return the filterByActive
     */
    public boolean isActive_ctl() {
        return filterByActive;
    }

    /**
     * @return the isActive
     */
    public boolean isIsActive() {
        return isActive;
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
     * @param filterByActive the filterByActive to set
     */
    public void setActive_ctl(boolean filterByActive) {
        this.filterByActive = filterByActive;
    }

    /**
     * @param isActive the isActive to set
     */
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
  

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
     * @return the userID
     */
    public int getUserID() {
        return userID;
    }

    /**
     * @param userID the userID to set
     */
    public void setUserID(int userID) {
        this.userID = userID;
    }

    /**
     * @return the filterByEventDomain
     */
    public boolean isFilterByEventDomain() {
        return filterByEventDomain;
    }

    /**
     * @param filterByEventDomain the filterByEventDomain to set
     */
    public void setFilterByEventDomain(boolean filterByEventDomain) {
        this.filterByEventDomain = filterByEventDomain;
    }

    /**
     * @return the domain
     */
    public EventDomainEnum getDomain() {
        return domain;
    }

    /**
     * @param domain the domain to set
     */
    public void setDomain(EventDomainEnum domain) {
        this.domain = domain;
    }

   

   
   

   
   

   
    
}