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
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUseType;

/**
 *
 * @author Sylvia Garland
 */
public class        SearchParamsEvent 
        extends     SearchParams {
    
    public static final String MUNI_DBFIELD = "property.municipality_municode";
    
    // filter EVENT-1
    private boolean eventCat_ctl;
    private EventCategory eventCat_val;
    
    // filter EVENT-2
    private boolean eventType_ctl;
    private EventType eventType_val;
    
    // filter EVENT-3
    private boolean eventDomain_ctl;
    private EventDomainEnum eventDomain_val;
    
    // filter EVENT-4
    private boolean eventDomainPK_ctl;
    private int eventDomainPK_val;
  
    // filter EVENT-5
    private boolean person_ctl;
    private Person person_val;
    
    // filter EVENT-6
    private boolean discloseToMuni_ctl;
    private boolean discloseToMuni_val;
    
    // filter EVENT-7
    private boolean discloseToPublic_ctl;
    private boolean discloseToPublic_val;
    
    // filter EVENT-8
    private boolean property_ctl;
    private Property property_val;
    
    // filter EVENT-9
    private boolean propertyUseType_ctl;
    private PropertyUseType propertyUseType_val;
    
    // filter EVENT-10
    private boolean propertyLandBankHeld_ctl;
    private boolean propertyLandBankHeld_val;
    
    
   public SearchParamsEvent(){
       
   }
   
     public SearchParamsEventDateFieldsEnum[] getDateFieldList(){
       SearchParamsEventDateFieldsEnum[] fields = SearchParamsEventDateFieldsEnum.values();
       return fields;
   }
   
   public SearchParamsEventUserFieldsEnum[] getUserFieldList(){
       SearchParamsEventUserFieldsEnum[] fields = SearchParamsEventUserFieldsEnum.values();
       return fields;
   }
   
   
   public EventDomainEnum[] getEventDomainList(){
       EventDomainEnum[] domains = EventDomainEnum.values();
       return domains;
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

    /**
     * @return the eventDomainPK_ctl
     */
    public boolean isEventDomainPK_ctl() {
        return eventDomainPK_ctl;
    }

    /**
     * @param eventDomainPK_ctl the eventDomainPK_ctl to set
     */
    public void setEventDomainPK_ctl(boolean eventDomainPK_ctl) {
        this.eventDomainPK_ctl = eventDomainPK_ctl;
    }

    /**
     * @return the eventDomainPK_val
     */
    public int getEventDomainPK_val() {
        return eventDomainPK_val;
    }

    /**
     * @param eventDomainPK_val the eventDomainPK_val to set
     */
    public void setEventDomainPK_val(int eventDomainPK_val) {
        this.eventDomainPK_val = eventDomainPK_val;
    }

    /**
     * @return the property_ctl
     */
    public boolean isProperty_ctl() {
        return property_ctl;
    }

    /**
     * @param property_ctl the property_ctl to set
     */
    public void setProperty_ctl(boolean property_ctl) {
        this.property_ctl = property_ctl;
    }

    /**
     * @return the propertyUseType_ctl
     */
    public boolean isPropertyUseType_ctl() {
        return propertyUseType_ctl;
    }

    /**
     * @param propertyUseType_ctl the propertyUseType_ctl to set
     */
    public void setPropertyUseType_ctl(boolean propertyUseType_ctl) {
        this.propertyUseType_ctl = propertyUseType_ctl;
    }

    /**
     * @return the propertyLandBankHeld_ctl
     */
    public boolean isPropertyLandBankHeld_ctl() {
        return propertyLandBankHeld_ctl;
    }

    /**
     * @param propertyLandBankHeld_ctl the propertyLandBankHeld_ctl to set
     */
    public void setPropertyLandBankHeld_ctl(boolean propertyLandBankHeld_ctl) {
        this.propertyLandBankHeld_ctl = propertyLandBankHeld_ctl;
    }

    /**
     * @return the propertyLandBankHeld_val
     */
    public boolean isPropertyLandBankHeld_val() {
        return propertyLandBankHeld_val;
    }

    /**
     * @param propertyLandBankHeld_val the propertyLandBankHeld_val to set
     */
    public void setPropertyLandBankHeld_val(boolean propertyLandBankHeld_val) {
        this.propertyLandBankHeld_val = propertyLandBankHeld_val;
    }

    /**
     * @return the propertyUseType_val
     */
    public PropertyUseType getPropertyUseType_val() {
        return propertyUseType_val;
    }

    /**
     * @param propertyUseType_val the propertyUseType_val to set
     */
    public void setPropertyUseType_val(PropertyUseType propertyUseType_val) {
        this.propertyUseType_val = propertyUseType_val;
    }

    /**
     * @return the property_val
     */
    public Property getProperty_val() {
        return property_val;
    }

    /**
     * @param property_val the property_val to set
     */
    public void setProperty_val(Property property_val) {
        this.property_val = property_val;
    }

   

   
   

   
   

   
    
}
