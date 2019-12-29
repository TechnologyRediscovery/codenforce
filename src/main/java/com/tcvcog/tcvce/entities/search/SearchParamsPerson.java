/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.RoleType;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores search parameters and switches turning each one on and off 
 * for queries involving Person objects
 * 
 * @author Sylvia Garland
 */
public  class   SearchParamsPerson 
        extends SearchParams {
    
        protected RoleType names_rtMin;
        private String name_first_val;
        private boolean name_first_ctl;
        
        private String name_last_val;
        private boolean name_last_ctl;
        private boolean name_compositeLNameOnly_ctl;
        
        
        protected RoleType phoneNumber_rtMin;
        private boolean phoneNumber_ctl;
        private String phoneNumber_val;
        
        protected RoleType email_rtMin;
        private String email_val;
        private boolean email_ctl;

        protected RoleType address_rtMin;
        private boolean address_streetNum_ctl;
        private String address_streetNum_val;
        private boolean city_ctl;
        private String city_val;
        private boolean zip_ctl;
        private String zip_val;
                
        protected RoleType personType_rtMin;
        private boolean personType_ctl;
        private List<PersonType> personType_val; 
        
        protected RoleType verified_rtMin;
        private boolean verified_ctl;
        private boolean verified_val;       
        
   public SearchParamsPerson(){
       personType_val = new ArrayList<>();
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
    public List<PersonType> getPersonType_val() {
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
    public void setPersonType_val(List<PersonType> personType_val) {
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
     * @return the city_val
     */
    public String getCity_val() {
        return city_val;
    }

    /**
     * @param city_val the city_val to set
     */
    public void setCity_val(String city_val) {
        this.city_val = city_val;
    }

    /**
     * @return the city_ctl
     */
    public boolean isCity_ctl() {
        return city_ctl;
    }

    /**
     * @param city_ctl the city_ctl to set
     */
    public void setCity_ctl(boolean city_ctl) {
        this.city_ctl = city_ctl;
    }

    /**
     * @return the zip_val
     */
    public String getZip_val() {
        return zip_val;
    }

    /**
     * @param zip_val the zip_val to set
     */
    public void setZip_val(String zip_val) {
        this.zip_val = zip_val;
    }

    /**
     * @return the zip_ctl
     */
    public boolean isZip_ctl() {
        return zip_ctl;
    }

    /**
     * @param zip_ctl the zip_ctl to set
     */
    public void setZip_ctl(boolean zip_ctl) {
        this.zip_ctl = zip_ctl;
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
    
}
