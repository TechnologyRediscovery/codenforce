/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.MailingCityStateZipDefaultTypeEnum;
import com.tcvcog.tcvce.entities.MailingCityStateZipRecordTypeEnum;

/**
 * Coagulates instructions for querying the special 
 * CityStateZip table!!
 * 
 * @author Ellen Bascomb
 */
public class        SearchParamsMailingCityStateZip 
        extends     SearchParams{
    
    
    private boolean zip_ctl;
    private String zip_val;
    
    private boolean state_ctl;
    private String state_val;
    
    private boolean city_ctl;
    private String city_val;
    
    private boolean recordType_ctl;
    private MailingCityStateZipRecordTypeEnum recordType_val;
    
    private boolean defaultType_ctl;
    private MailingCityStateZipDefaultTypeEnum defaultType_val;
    
    private boolean defaultCity_ctl;
    private String defaultCity_val;

    /**
     * @return the zip_ctl
     */
    public boolean isZip_ctl() {
        return zip_ctl;
    }

    /**
     * @return the zip_val
     */
    public String getZip_val() {
        return zip_val;
    }

    /**
     * @return the state_ctl
     */
    public boolean isState_ctl() {
        return state_ctl;
    }

    /**
     * @return the state_val
     */
    public String getState_val() {
        return state_val;
    }

    /**
     * @return the city_ctl
     */
    public boolean isCity_ctl() {
        return city_ctl;
    }

    /**
     * @return the city_val
     */
    public String getCity_val() {
        return city_val;
    }

    /**
     * @return the recordType_ctl
     */
    public boolean isRecordType_ctl() {
        return recordType_ctl;
    }

    /**
     * @return the recordType_val
     */
    public MailingCityStateZipRecordTypeEnum getRecordType_val() {
        return recordType_val;
    }

    /**
     * @return the defaultType_ctl
     */
    public boolean isDefaultType_ctl() {
        return defaultType_ctl;
    }

    /**
     * @return the defaultType_val
     */
    public MailingCityStateZipDefaultTypeEnum getDefaultType_val() {
        return defaultType_val;
    }

    /**
     * @return the defaultCity_ctl
     */
    public boolean isDefaultCity_ctl() {
        return defaultCity_ctl;
    }

    /**
     * @return the defaultCity_val
     */
    public String getDefaultCity_val() {
        return defaultCity_val;
    }

    /**
     * @param zip_ctl the zip_ctl to set
     */
    public void setZip_ctl(boolean zip_ctl) {
        this.zip_ctl = zip_ctl;
    }

    /**
     * @param zip_val the zip_val to set
     */
    public void setZip_val(String zip_val) {
        this.zip_val = zip_val;
    }

    /**
     * @param state_ctl the state_ctl to set
     */
    public void setState_ctl(boolean state_ctl) {
        this.state_ctl = state_ctl;
    }

    /**
     * @param state_val the state_val to set
     */
    public void setState_val(String state_val) {
        this.state_val = state_val;
    }

    /**
     * @param city_ctl the city_ctl to set
     */
    public void setCity_ctl(boolean city_ctl) {
        this.city_ctl = city_ctl;
    }

    /**
     * @param city_val the city_val to set
     */
    public void setCity_val(String city_val) {
        this.city_val = city_val;
    }

    /**
     * @param recordType_ctl the recordType_ctl to set
     */
    public void setRecordType_ctl(boolean recordType_ctl) {
        this.recordType_ctl = recordType_ctl;
    }

    /**
     * @param recordType_val the recordType_val to set
     */
    public void setRecordType_val(MailingCityStateZipRecordTypeEnum recordType_val) {
        this.recordType_val = recordType_val;
    }

    /**
     * @param defaultType_ctl the defaultType_ctl to set
     */
    public void setDefaultType_ctl(boolean defaultType_ctl) {
        this.defaultType_ctl = defaultType_ctl;
    }

    /**
     * @param defaultType_val the defaultType_val to set
     */
    public void setDefaultType_val(MailingCityStateZipDefaultTypeEnum defaultType_val) {
        this.defaultType_val = defaultType_val;
    }

    /**
     * @param defaultCity_ctl the defaultCity_ctl to set
     */
    public void setDefaultCity_ctl(boolean defaultCity_ctl) {
        this.defaultCity_ctl = defaultCity_ctl;
    }

    /**
     * @param defaultCity_val the defaultCity_val to set
     */
    public void setDefaultCity_val(String defaultCity_val) {
        this.defaultCity_val = defaultCity_val;
    }
  
    
    
}
