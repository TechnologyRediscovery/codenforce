    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.DomainEnum;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUseType;
import com.tcvcog.tcvce.entities.RoleType;

/**
 *
 * @author Sylvia Garland
 */
public class        SearchParamsEvent 
        extends     SearchParams {
    
    public static final String MUNI_DBFIELD = "parcel.muni_municode";
    
    // filter EVENT-1
    private boolean eventCat_ctl;
    private EventCategory eventCat_val;
    
    // filter EVENT-2
    private boolean eventType_ctl;
    private EventType eventType_val;
    
    // filter EVENT-3
    private boolean eventDomain_ctl;
    private DomainEnum eventDomain_val;
    
    // filter EVENT-4
    private boolean eventDomainPK_ctl;
    private int eventDomainPK_val;
  
    // filter EVENT-5
    private boolean person_ctl;
    private Person person_val;
    
    // filter EVENT-6: role floor enact
    private boolean rolefloor_enact_ctl;
    private RoleType roleFloor_enact_val;
    
    // filter EVENT-7: role floor view
    private boolean rolefloor_view_ctl;
    private RoleType roleFloor_view_val;
    
    // filter EVENT-8: role floor update
    private boolean rolefloor_update_ctl;
    private RoleType roleFloor_update_val;
    
    // filter EVENT-9
    private boolean property_ctl;
    private Property property_val;
    
    // filter EVENT-10
    private boolean notify_ctl;
    private boolean notify_val;
    
    
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
   
   
   public DomainEnum[] getEventDomainList(){
       DomainEnum[] domains = DomainEnum.values();
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
    public DomainEnum getEventDomain_val() {
        return eventDomain_val;
    }

    /**
     * @param eventDomain_val the eventDomain_val to set
     */
    public void setEventDomain_val(DomainEnum eventDomain_val) {
        this.eventDomain_val = eventDomain_val;
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

    /**
     * @return the rolefloor_enact_ctl
     */
    public boolean isRolefloor_enact_ctl() {
        return rolefloor_enact_ctl;
    }

    /**
     * @return the roleFloor_enact_val
     */
    public RoleType getRoleFloor_enact_val() {
        return roleFloor_enact_val;
    }

    /**
     * @return the rolefloor_view_ctl
     */
    public boolean isRolefloor_view_ctl() {
        return rolefloor_view_ctl;
    }

    /**
     * @return the roleFloor_view_val
     */
    public RoleType getRoleFloor_view_val() {
        return roleFloor_view_val;
    }

    /**
     * @return the rolefloor_update_ctl
     */
    public boolean isRolefloor_update_ctl() {
        return rolefloor_update_ctl;
    }

    /**
     * @return the roleFloor_update_val
     */
    public RoleType getRoleFloor_update_val() {
        return roleFloor_update_val;
    }

    /**
     * @return the notify_ctl
     */
    public boolean isNotify_ctl() {
        return notify_ctl;
    }

    /**
     * @return the notify_val
     */
    public boolean isNotify_val() {
        return notify_val;
    }

    /**
     * @param rolefloor_enact_ctl the rolefloor_enact_ctl to set
     */
    public void setRolefloor_enact_ctl(boolean rolefloor_enact_ctl) {
        this.rolefloor_enact_ctl = rolefloor_enact_ctl;
    }

    /**
     * @param roleFloor_enact_val the roleFloor_enact_val to set
     */
    public void setRoleFloor_enact_val(RoleType roleFloor_enact_val) {
        this.roleFloor_enact_val = roleFloor_enact_val;
    }

    /**
     * @param rolefloor_view_ctl the rolefloor_view_ctl to set
     */
    public void setRolefloor_view_ctl(boolean rolefloor_view_ctl) {
        this.rolefloor_view_ctl = rolefloor_view_ctl;
    }

    /**
     * @param roleFloor_view_val the roleFloor_view_val to set
     */
    public void setRoleFloor_view_val(RoleType roleFloor_view_val) {
        this.roleFloor_view_val = roleFloor_view_val;
    }

    /**
     * @param rolefloor_update_ctl the rolefloor_update_ctl to set
     */
    public void setRolefloor_update_ctl(boolean rolefloor_update_ctl) {
        this.rolefloor_update_ctl = rolefloor_update_ctl;
    }

    /**
     * @param roleFloor_update_val the roleFloor_update_val to set
     */
    public void setRoleFloor_update_val(RoleType roleFloor_update_val) {
        this.roleFloor_update_val = roleFloor_update_val;
    }

    /**
     * @param notify_ctl the notify_ctl to set
     */
    public void setNotify_ctl(boolean notify_ctl) {
        this.notify_ctl = notify_ctl;
    }

    /**
     * @param notify_val the notify_val to set
     */
    public void setNotify_val(boolean notify_val) {
        this.notify_val = notify_val;
    }

   

   
   

   
   

   
    
}
