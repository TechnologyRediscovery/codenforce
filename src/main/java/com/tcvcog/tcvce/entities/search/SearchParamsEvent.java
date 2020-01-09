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
    
    
    private boolean eventCat_ctl;
    private EventCategory eventCat_val;
    
    private boolean eventType_ctl;
    private EventType eventType_val;
    
    private boolean eventDomain_ctl;
    private EventDomainEnum eventDomain_val;
    private int eventDomainID_val;
  
    private boolean person_ctl;
    private Person person_val;
    
    private boolean discloseToMuni_ctl;
    private boolean discloseToMuni_val;
    
    private boolean discloseToPublic_ctl;
    private boolean discloseToPublic_val;
    
    
   public SearchParamsEvent(){
       
   }

    /**
     * @return the eventCat_ctl
     */
    public boolean isEventCat_ctl() {
        return eventCat_ctl;
    }

    /**
     * @return the eventCat_val
     */
    public EventCategory getEventCat_val() {
        return eventCat_val;
    }

    /**
     * @return the person_ctl
     */
    public boolean isPerson_ctl() {
        return person_ctl;
    }


    /**
     * @param eventCat_ctl the eventCat_ctl to set
     */
    public void setEventCat_ctl(boolean eventCat_ctl) {
        this.eventCat_ctl = eventCat_ctl;
    }

    /**
     * @param eventCat_val the eventCat_val to set
     */
    public void setEventCat_val(EventCategory eventCat_val) {
        this.eventCat_val = eventCat_val;
    }

    /**
     * @param person_ctl the person_ctl to set
     */
    public void setPerson_ctl(boolean person_ctl) {
        this.person_ctl = person_ctl;
    }

    /**
     * @return the eventType_ctl
     */
    public boolean isEventType_ctl() {
        return eventType_ctl;
    }

    /**
     * @param eventType_ctl the eventType_ctl to set
     */
    public void setEventType_ctl(boolean eventType_ctl) {
        this.eventType_ctl = eventType_ctl;
    }

   

    /**
     * @return the eventType_val
     */
    public EventType getEventType_val() {
        return eventType_val;
    }

    /**
     * @param eventType_val the eventType_val to set
     */
    public void setEventType_val(EventType eventType_val) {
        this.eventType_val = eventType_val;
    }

    /**
     * @return the person_val
     */
    public Person getPerson_val() {
        return person_val;
    }

    /**
     * @param person_val the person_val to set
     */
    public void setPerson_val(Person person_val) {
        this.person_val = person_val;
    }

   
    /**
     * @return the eventDomain_ctl
     */
    public boolean isEventDomain_ctl() {
        return eventDomain_ctl;
    }

    /**
     * @param eventDomain_ctl the eventDomain_ctl to set
     */
    public void setEventDomain_ctl(boolean eventDomain_ctl) {
        this.eventDomain_ctl = eventDomain_ctl;
    }

    /**
     * @return the eventDomain_val
     */
    public EventDomainEnum getEventDomain_val() {
        return eventDomain_val;
    }

    /**
     * @param eventDomain_val the eventDomain_val to set
     */
    public void setEventDomain_val(EventDomainEnum eventDomain_val) {
        this.eventDomain_val = eventDomain_val;
    }

    /**
     * @return the eventDomainID_val
     */
    public int getEventDomainID_val() {
        return eventDomainID_val;
    }

    /**
     * @param eventDomainID_val the eventDomainID_val to set
     */
    public void setEventDomainID_val(int eventDomainID_val) {
        this.eventDomainID_val = eventDomainID_val;
    }

    /**
     * @return the discloseToMuni_ctl
     */
    public boolean isDiscloseToMuni_ctl() {
        return discloseToMuni_ctl;
    }

    /**
     * @return the discloseToMuni_val
     */
    public boolean isDiscloseToMuni_val() {
        return discloseToMuni_val;
    }

    /**
     * @return the discloseToPublic_ctl
     */
    public boolean isDiscloseToPublic_ctl() {
        return discloseToPublic_ctl;
    }

    /**
     * @return the discloseToPublic_val
     */
    public boolean isDiscloseToPublic_val() {
        return discloseToPublic_val;
    }

    /**
     * @param discloseToMuni_ctl the discloseToMuni_ctl to set
     */
    public void setDiscloseToMuni_ctl(boolean discloseToMuni_ctl) {
        this.discloseToMuni_ctl = discloseToMuni_ctl;
    }

    /**
     * @param discloseToMuni_val the discloseToMuni_val to set
     */
    public void setDiscloseToMuni_val(boolean discloseToMuni_val) {
        this.discloseToMuni_val = discloseToMuni_val;
    }

    /**
     * @param discloseToPublic_ctl the discloseToPublic_ctl to set
     */
    public void setDiscloseToPublic_ctl(boolean discloseToPublic_ctl) {
        this.discloseToPublic_ctl = discloseToPublic_ctl;
    }

    /**
     * @param discloseToPublic_val the discloseToPublic_val to set
     */
    public void setDiscloseToPublic_val(boolean discloseToPublic_val) {
        this.discloseToPublic_val = discloseToPublic_val;
    }

   

   
   

   
   

   
    
}
