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
package com.tcvcog.tcvce.entities;

/**
 * Encapsulates a ZIP code and its associated city and state
 * @author sylvia
 */
public class MailingCityStateZip {
    
    private int cityStateZipID;
    private String zipCode;
    private int stateID;
    private String state;
    private String city;
    
    private MailingCityStateZipRecordTypeEnum recordType;
    private MailingCityStateZipDefaultTypeEnum defaultType;
    private String recordTypeString;
    private String defaultTypeString;
    
    private String defaultCity;

    /**
     * @return the cityStateZipID
     */
    public int getCityStateZipID() {
        return cityStateZipID;
    }

    /**
     * @param cityStateZipID the cityStateZipID to set
     */
    public void setCityStateZipID(int cityStateZipID) {
        this.cityStateZipID = cityStateZipID;
    }

    /**
     * @return the zipCode
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * @return the stateID
     */
    public int getStateID() {
        return stateID;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param zipCode the zipCode to set
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    /**
     * @param stateID the stateID to set
     */
    public void setStateID(int stateID) {
        this.stateID = stateID;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the recordType
     */
    public MailingCityStateZipRecordTypeEnum getRecordType() {
        return recordType;
    }

    /**
     * @return the defaultType
     */
    public MailingCityStateZipDefaultTypeEnum getDefaultType() {
        return defaultType;
    }

    /**
     * @return the defaultCity
     */
    public String getDefaultCity() {
        return defaultCity;
    }

    /**
     * @param recordType the recordType to set
     */
    public void setRecordType(MailingCityStateZipRecordTypeEnum recordType) {
        this.recordType = recordType;
    }

    /**
     * @param defaultType the defaultType to set
     */
    public void setDefaultType(MailingCityStateZipDefaultTypeEnum defaultType) {
        this.defaultType = defaultType;
    }

    /**
     * @param defaultCity the defaultCity to set
     */
    public void setDefaultCity(String defaultCity) {
        this.defaultCity = defaultCity;
    }

    /**
     * @return the recordTypeString
     */
    public String getRecordTypeString() {
        return recordTypeString;
    }

    /**
     * @return the defaultTypeString
     */
    public String getDefaultTypeString() {
        return defaultTypeString;
    }

    /**
     * @param recordTypeString the recordTypeString to set
     */
    public void setRecordTypeString(String recordTypeString) {
        this.recordTypeString = recordTypeString;
    }

    /**
     * @param defaultTypeString the defaultTypeString to set
     */
    public void setDefaultTypeString(String defaultTypeString) {
        this.defaultTypeString = defaultTypeString;
    }
    
    
    
}
