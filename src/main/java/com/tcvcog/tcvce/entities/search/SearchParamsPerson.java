/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores search parameters and switches turning each one on and off 
 for queries involving Person objects
 * 
 * @author Sylvia Garland
 */
public  class       SearchParamsPerson 
        extends     SearchParams {
    
    public static final String MUNI_DBFIELD = "property.municipality_municode";
    
    // filter PERS-1
    protected RoleType names_rtMin;
    private boolean name_first_ctl;
    private String name_first_val;

    // filter PERS-2
    private boolean name_last_ctl;
    private String name_last_val;

    // filter PERS-3
    private boolean name_compositeLNameOnly_ctl;
    private boolean name_compositeLNameOnly_val;

    // filter PERS-4
    protected RoleType phoneNumber_rtMin;
    private boolean phoneNumber_ctl;
    private String phoneNumber_val;

    // filter PERS-5
    protected RoleType email_rtMin;
    private boolean email_ctl;
    private String email_val;

    // filter PERS-6
    protected RoleType address_rtMin;
    private boolean address_streetNum_ctl;
    private String address_streetNum_val;
    
    // filter PERS-7
    private boolean address_city_ctl;
    private String address_city_val;
    
    // filter PERS-8
    private boolean address_zip_ctl;
    private String address_zip_val;

    // filter PERS-9
    protected RoleType personType_rtMin;
    private boolean personType_ctl;
    private PersonType personType_val; 

    // filter PERS-10
    protected RoleType verified_rtMin;
    private boolean verified_ctl;
    private boolean verified_val;       

    // filter PERS-11
    private boolean source_ctl;
    private BOBSource source_val;
    
    // filter PERS-12
    private boolean property_ctl;
    private Property property_val;
    
    // filter PERS-13
    private boolean propertyUnit_ctl;
    private PropertyUnit propertyUnit_val;
    
    // filter PERS-14
    private boolean occPeriod_ctl;
    private OccPeriod occPeriod_val;
    
    // filter PERS-15
    private boolean event_ctl;
    private EventCnF event_Val;
    
    // filter PERS-16
    private boolean citation_ctl;
    private Citation citation_val;
    
    // filter PERS-17
    private boolean mergeTarget_ctl;
    private Person mergeTarget_val;
    
    // filter PERS-18
    private boolean muni_ctl;
    private Municipality muni_val;
        
   public SearchParamsPerson(){
       
   }

    /**
     * @return the name_first_val
     */
    public String getName_first_val() {
        return name_first_val;
    }

    /**
     * @return the name_first_ctl
     */
    public boolean isName_first_ctl() {
        return name_first_ctl;
    }

    /**
     * @return the name_last_val
     */
    public String getName_last_val() {
        return name_last_val;
    }

    /**
     * @return the name_last_ctl
     */
    public boolean isName_last_ctl() {
        return name_last_ctl;
    }

    /**
     * @return the name_compositeLNameOnly_ctl
     */
    public boolean isName_compositeLNameOnly_ctl() {
        return name_compositeLNameOnly_ctl;
    }

    /**
     * @return the personType_val
     */
    public PersonType getPersonType_val() {
        return personType_val;
    }

    /**
     * @return the personType_ctl
     */
    public boolean isPersonType_ctl() {
        return personType_ctl;
    }

    /**
     * @return the email_val
     */
    public String getEmail_val() {
        return email_val;
    }

    /**
     * @return the email_ctl
     */
    public boolean isEmail_ctl() {
        return email_ctl;
    }

    /**
     * @return the address_streetNum_val
     */
    public String getAddress_streetNum_val() {
        return address_streetNum_val;
    }

    /**
     * @return the address_streetNum_ctl
     */
    public boolean isAddress_streetNum_ctl() {
        return address_streetNum_ctl;
    }

    /**
     * @return the verified_ctl
     */
    public boolean isVerified_ctl() {
        return verified_ctl;
    }

    /**
     * @return the verified_val
     */
    public boolean isVerified_val() {
        return verified_val;
    }

    /**
     * @param name_first_val the name_first_val to set
     */
    public void setName_first_val(String name_first_val) {
        this.name_first_val = name_first_val;
    }

    /**
     * @param name_first_ctl the name_first_ctl to set
     */
    public void setName_first_ctl(boolean name_first_ctl) {
        this.name_first_ctl = name_first_ctl;
    }

    /**
     * @param name_last_val the name_last_val to set
     */
    public void setName_last_val(String name_last_val) {
        this.name_last_val = name_last_val;
    }

    /**
     * @param name_last_ctl the name_last_ctl to set
     */
    public void setName_last_ctl(boolean name_last_ctl) {
        this.name_last_ctl = name_last_ctl;
    }

    /**
     * @param name_compositeLNameOnly_ctl the name_compositeLNameOnly_ctl to set
     */
    public void setName_compositeLNameOnly_ctl(boolean name_compositeLNameOnly_ctl) {
        this.name_compositeLNameOnly_ctl = name_compositeLNameOnly_ctl;
    }

    /**
     * @param personType_val the personType_val to set
     */
    public void setPersonType_val(PersonType personType_val) {
        this.personType_val = personType_val;
    }

    /**
     * @param personType_ctl the personType_ctl to set
     */
    public void setPersonType_ctl(boolean personType_ctl) {
        this.personType_ctl = personType_ctl;
    }

    /**
     * @param email_val the email_val to set
     */
    public void setEmail_val(String email_val) {
        this.email_val = email_val;
    }

    /**
     * @param email_ctl the email_ctl to set
     */
    public void setEmail_ctl(boolean email_ctl) {
        this.email_ctl = email_ctl;
    }

    /**
     * @param address_streetNum_val the address_streetNum_val to set
     */
    public void setAddress_streetNum_val(String address_streetNum_val) {
        this.address_streetNum_val = address_streetNum_val;
    }

    /**
     * @param address_streetNum_ctl the address_streetNum_ctl to set
     */
    public void setAddress_streetNum_ctl(boolean address_streetNum_ctl) {
        this.address_streetNum_ctl = address_streetNum_ctl;
    }


    /**
     * @param verified_ctl the verified_ctl to set
     */
    public void setVerified_ctl(boolean verified_ctl) {
        this.verified_ctl = verified_ctl;
    }

    /**
     * @param verified_val the verified_val to set
     */
    public void setVerified_val(boolean verified_val) {
        this.verified_val = verified_val;
    }

    /**
     * @return the phoneNumber_val
     */
    public String getPhoneNumber_val() {
        return phoneNumber_val;
    }

    /**
     * @param phoneNumber_val the phoneNumber_val to set
     */
    public void setPhoneNumber_val(String phoneNumber_val) {
        this.phoneNumber_val = phoneNumber_val;
    }

    /**
     * @return the phoneNumber_ctl
     */
    public boolean isPhoneNumber_ctl() {
        return phoneNumber_ctl;
    }

    /**
     * @param phoneNumber_ctl the phoneNumber_ctl to set
     */
    public void setPhoneNumber_ctl(boolean phoneNumber_ctl) {
        this.phoneNumber_ctl = phoneNumber_ctl;
    }

    /**
     * @return the address_city_val
     */
    public String getAddress_city_val() {
        return address_city_val;
    }

    /**
     * @param address_city_val the address_city_val to set
     */
    public void setAddress_city_val(String address_city_val) {
        this.address_city_val = address_city_val;
    }

    /**
     * @return the address_city_ctl
     */
    public boolean isAddress_city_ctl() {
        return address_city_ctl;
    }

    /**
     * @param address_city_ctl the address_city_ctl to set
     */
    public void setAddress_city_ctl(boolean address_city_ctl) {
        this.address_city_ctl = address_city_ctl;
    }

    /**
     * @return the address_zip_val
     */
    public String getAddress_zip_val() {
        return address_zip_val;
    }

    /**
     * @param address_zip_val the address_zip_val to set
     */
    public void setAddress_zip_val(String address_zip_val) {
        this.address_zip_val = address_zip_val;
    }

    /**
     * @return the address_zip_ctl
     */
    public boolean isAddress_zip_ctl() {
        return address_zip_ctl;
    }

    /**
     * @param address_zip_ctl the address_zip_ctl to set
     */
    public void setAddress_zip_ctl(boolean address_zip_ctl) {
        this.address_zip_ctl = address_zip_ctl;
    }  

    /**
     * @return the names_rtMin
     */
    public RoleType getNames_rtMin() {
        return names_rtMin;
    }

    /**
     * @param names_rtMin the names_rtMin to set
     */
    public void setNames_rtMin(RoleType names_rtMin) {
        this.names_rtMin = names_rtMin;
    }

    /**
     * @return the phoneNumber_rtMin
     */
    public RoleType getPhoneNumber_rtMin() {
        return phoneNumber_rtMin;
    }

    /**
     * @param phoneNumber_rtMin the phoneNumber_rtMin to set
     */
    public void setPhoneNumber_rtMin(RoleType phoneNumber_rtMin) {
        this.phoneNumber_rtMin = phoneNumber_rtMin;
    }

    /**
     * @return the email_rtMin
     */
    public RoleType getEmail_rtMin() {
        return email_rtMin;
    }

    /**
     * @param email_rtMin the email_rtMin to set
     */
    public void setEmail_rtMin(RoleType email_rtMin) {
        this.email_rtMin = email_rtMin;
    }

    /**
     * @return the address_rtMin
     */
    public RoleType getAddress_rtMin() {
        return address_rtMin;
    }

    /**
     * @param address_rtMin the address_rtMin to set
     */
    public void setAddress_rtMin(RoleType address_rtMin) {
        this.address_rtMin = address_rtMin;
    }

    /**
     * @return the personType_rtMin
     */
    public RoleType getPersonType_rtMin() {
        return personType_rtMin;
    }

    /**
     * @param personType_rtMin the personType_rtMin to set
     */
    public void setPersonType_rtMin(RoleType personType_rtMin) {
        this.personType_rtMin = personType_rtMin;
    }


    /**
     * @return the verified_rtMin
     */
    public RoleType getVerified_rtMin() {
        return verified_rtMin;
    }

    /**
     * @param verified_rtMin the verified_rtMin to set
     */
    public void setVerified_rtMin(RoleType verified_rtMin) {
        this.verified_rtMin = verified_rtMin;
    }

    /**
     * @return the name_compositeLNameOnly_val
     */
    public boolean isName_compositeLNameOnly_val() {
        return name_compositeLNameOnly_val;
    }

    /**
     * @param name_compositeLNameOnly_val the name_compositeLNameOnly_val to set
     */
    public void setName_compositeLNameOnly_val(boolean name_compositeLNameOnly_val) {
        this.name_compositeLNameOnly_val = name_compositeLNameOnly_val;
    }

    /**
     * @return the source_ctl
     */
    public boolean isSource_ctl() {
        return source_ctl;
    }

    /**
     * @param source_ctl the source_ctl to set
     */
    public void setSource_ctl(boolean source_ctl) {
        this.source_ctl = source_ctl;
    }

    /**
     * @return the source_val
     */
    public BOBSource getSource_val() {
        return source_val;
    }

    /**
     * @param source_val the source_val to set
     */
    public void setSource_val(BOBSource source_val) {
        this.source_val = source_val;
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
     * @return the propertyUnit_ctl
     */
    public boolean isPropertyUnit_ctl() {
        return propertyUnit_ctl;
    }

    /**
     * @return the propertyUnit_val
     */
    public PropertyUnit getPropertyUnit_val() {
        return propertyUnit_val;
    }

    /**
     * @param propertyUnit_ctl the propertyUnit_ctl to set
     */
    public void setPropertyUnit_ctl(boolean propertyUnit_ctl) {
        this.propertyUnit_ctl = propertyUnit_ctl;
    }

    /**
     * @param propertyUnit_val the propertyUnit_val to set
     */
    public void setPropertyUnit_val(PropertyUnit propertyUnit_val) {
        this.propertyUnit_val = propertyUnit_val;
    }

    /**
     * @return the occPeriod_ctl
     */
    public boolean isOccPeriod_ctl() {
        return occPeriod_ctl;
    }

    /**
     * @return the occPeriod_val
     */
    public OccPeriod getOccPeriod_val() {
        return occPeriod_val;
    }

    /**
     * @return the event_ctl
     */
    public boolean isEvent_ctl() {
        return event_ctl;
    }

    /**
     * @return the event_Val
     */
    public EventCnF getEvent_Val() {
        return event_Val;
    }

    /**
     * @return the citation_ctl
     */
    public boolean isCitation_ctl() {
        return citation_ctl;
    }

    /**
     * @return the citation_val
     */
    public Citation getCitation_val() {
        return citation_val;
    }

    /**
     * @return the mergeTarget_ctl
     */
    public boolean isMergeTarget_ctl() {
        return mergeTarget_ctl;
    }

    /**
     * @return the mergeTarget_val
     */
    public Person getMergeTarget_val() {
        return mergeTarget_val;
    }

    /**
     * @param occPeriod_ctl the occPeriod_ctl to set
     */
    public void setOccPeriod_ctl(boolean occPeriod_ctl) {
        this.occPeriod_ctl = occPeriod_ctl;
    }

    /**
     * @param occPeriod_val the occPeriod_val to set
     */
    public void setOccPeriod_val(OccPeriod occPeriod_val) {
        this.occPeriod_val = occPeriod_val;
    }

    /**
     * @param event_ctl the event_ctl to set
     */
    public void setEvent_ctl(boolean event_ctl) {
        this.event_ctl = event_ctl;
    }

    /**
     * @param event_Val the event_Val to set
     */
    public void setEvent_Val(EventCnF event_Val) {
        this.event_Val = event_Val;
    }

    /**
     * @param citation_ctl the citation_ctl to set
     */
    public void setCitation_ctl(boolean citation_ctl) {
        this.citation_ctl = citation_ctl;
    }

    /**
     * @param citation_val the citation_val to set
     */
    public void setCitation_val(Citation citation_val) {
        this.citation_val = citation_val;
    }

    /**
     * @param mergeTarget_ctl the mergeTarget_ctl to set
     */
    public void setMergeTarget_ctl(boolean mergeTarget_ctl) {
        this.mergeTarget_ctl = mergeTarget_ctl;
    }

    /**
     * @param mergeTarget_val the mergeTarget_val to set
     */
    public void setMergeTarget_val(Person mergeTarget_val) {
        this.mergeTarget_val = mergeTarget_val;
    }

    /**
     * @return the muni_ctl
     */
    public boolean isMuni_ctl() {
        return muni_ctl;
    }

    /**
     * @param muni_ctl the muni_ctl to set
     */
    public void setMuni_ctl(boolean muni_ctl) {
        this.muni_ctl = muni_ctl;
    }

    /**
     * @return the muni_val
     */
    public Municipality getMuni_val() {
        return muni_val;
    }

    /**
     * @param muni_val the muni_val to set
     */
    public void setMuni_val(Municipality muni_val) {
        this.muni_val = muni_val;
    }
    
}
