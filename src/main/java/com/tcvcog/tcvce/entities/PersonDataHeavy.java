/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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

import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodPropertyUnitHeavy;
import java.util.List;

/**
 *
 * @author sylvia
 */
public  class PersonDataHeavy 
        extends Person 
        implements IFace_CredentialSigned{
    
    private List<CECasePropertyUnitHeavy> caseList;
    private List<OccPeriodPropertyUnitHeavy> periodList;
    private List<Property> propertyList;
    private List<EventCnFPropUnitCasePeriodHeavy> eventList;
    
    private String credentialSignature;
    
    public PersonDataHeavy(){
        
    }
    
    /**
     * @return the credentialSignature
     */
    @Override
    public String getCredentialSignature() {
        return credentialSignature;
    }

    
    
    public PersonDataHeavy(Person p, Credential cred){
        
        if(cred != null){
            credentialSignature = cred.getSignature();
        }
        
        this.personID = p.personID;

        this.personType = p.personType;
        this.muniCode = p.muniCode;
        this. muniName = p.muniName;
        this.muni = p.muni;

        this.source = p.source;
        this.creatorUserID = p.creatorUserID;
        this.creationTimeStamp = p.creationTimeStamp;

        // for backwards compatability

        this.firstName = p.firstName;
        this.lastName = p.lastName;

        // frist, middle initial, and last all in lastName
        this.compositeLastName = p.compositeLastName;
        this.businessEntity = p.businessEntity;

        this. jobTitle = p.jobTitle;

        this. phoneCell = p.phoneCell;
        this. phoneHome = p.phoneHome;
        this. phoneWork = p.phoneWork;

        this. email = p.email;
        this. addressStreet = p.addressStreet;
        this. addressCity = p.addressCity;

        this. addressZip = p.addressZip;
        this. addressState = p.addressState;

        this.useSeparateMailingAddress = p.useSeparateMailingAddress;
        this.mailingAddressStreet = p.mailingAddressStreet;
        this.mailingAddressThirdLine = p.mailingAddressThirdLine;
        this.mailingAddressCity = p.mailingAddressCity;
        this.mailingAddressZip = p.mailingAddressZip;

        this.mailingAddressState = p.mailingAddressState;

        this.notes = p.notes;

        this.lastUpdated = p.lastUpdated;
        this.lastUpdatedPretty = p.lastUpdatedPretty;

        this.canExpire = p.canExpire;
        this.expiryDate = p.expiryDate;
        this.expireString = p.expireString;
        this.expiryDateUtilDate = p.expiryDateUtilDate;
        this.expiryNotes = p.expiryNotes;
        this.active = p.active;
        this.linkedUserID = p.linkedUserID;

        /**
         * Tenancy tracking
         */
        this.under18 = p.under18;
        this.verifiedByUserID = p.verifiedByUserID;

        this.referencePerson = p.referencePerson;

        this.ghostCreatedDate = p.ghostCreatedDate;
        this.ghostCreatedDatePretty = p.ghostCreatedDatePretty;
        this.ghostOf = p.ghostOf;
        this.ghostCreatedByUserID = p.ghostCreatedByUserID;

        this.cloneCreatedDate = p.cloneCreatedDate;
        this.cloneCreatedDatePretty = p.cloneCreatedDatePretty;
        this.cloneOf = p.cloneOf;
        this.cloneCreatedByUserID = p.cloneCreatedByUserID;

        this.ghostsList = p.ghostsList;
        this.cloneList = p.cloneList;
        this.mergedList = p.mergedList;
        
    }
    
    /**
     * Dead on arrival method to follow pattern of other BObs whose previous
     * DataHeavy versions did not require Credentials to Instantiate
     * 
     * @deprecated 
     * @param p to be injected into the superclass members
     * 
     * 
     */
     public PersonDataHeavy(Person p){
        
        
        this.personID = p.personID;

        this.personType = p.personType;
        this.muniCode = p.muniCode;
        this. muniName = p.muniName;

        this.source = p.source;
        this.creatorUserID = p.creatorUserID;
        this.creationTimeStamp = p.creationTimeStamp;

        // for backwards compatability

        this.firstName = p.firstName;
        this.lastName = p.lastName;

        // frist, middle initial, and last all in lastName
        this.compositeLastName = p.compositeLastName;
        this.businessEntity = p.businessEntity;

        this. jobTitle = p.jobTitle;

        this. phoneCell = p.phoneCell;
        this. phoneHome = p.phoneHome;
        this. phoneWork = p.phoneWork;

        this. email = p.email;
        this. addressStreet = p.addressStreet;
        this. addressCity = p.addressCity;

        this. addressZip = p.addressZip;
        this. addressState = p.addressState;

        this.useSeparateMailingAddress = p.useSeparateMailingAddress;
        this.mailingAddressStreet = p.mailingAddressStreet;
        this.mailingAddressThirdLine = p.mailingAddressThirdLine;
        this.mailingAddressCity = p.mailingAddressCity;
        this.mailingAddressZip = p.mailingAddressZip;

        this.mailingAddressState = p.mailingAddressState;

        this.notes = p.notes;

        this.lastUpdated = p.lastUpdated;
        this.lastUpdatedPretty = p.lastUpdatedPretty;

        this.canExpire = p.canExpire;
        this.expiryDate = p.expiryDate;
        this.expireString = p.expireString;
        this.expiryDateUtilDate = p.expiryDateUtilDate;
        this.expiryNotes = p.expiryNotes;
        this.active = p.active;
        this.linkedUserID = p.linkedUserID;

        /**
         * Tenancy tracking
         */
        this.under18 = p.under18;
        this.verifiedByUserID = p.verifiedByUserID;

        this.referencePerson = p.referencePerson;

        this.ghostCreatedDate = p.ghostCreatedDate;
        this.ghostCreatedDatePretty = p.ghostCreatedDatePretty;
        this.ghostOf = p.ghostOf;
        this.ghostCreatedByUserID = p.ghostCreatedByUserID;

        this.cloneCreatedDate = p.cloneCreatedDate;
        this.cloneCreatedDatePretty = p.cloneCreatedDatePretty;
        this.cloneOf = p.cloneOf;
        this.cloneCreatedByUserID = p.cloneCreatedByUserID;

        this.ghostsList = p.ghostsList;
        this.cloneList = p.cloneList;
        this.mergedList = p.mergedList;
        
    }

    /**
     * @return the caseList
     */
    public List<CECasePropertyUnitHeavy> getCaseList() {
        return caseList;
    }

    /**
     * @return the periodList
     */
    public List<OccPeriodPropertyUnitHeavy> getPeriodList() {
        return periodList;
    }

    /**
     * @return the propertyList
     */
    public List<Property> getPropertyList() {
        return propertyList;
    }

    /**
     * @return the eventList
     */
    public List<EventCnFPropUnitCasePeriodHeavy> getEventList() {
        return eventList;
    }

    /**
     * @param caseList the caseList to set
     */
    public void setCaseList(List<CECasePropertyUnitHeavy> caseList) {
        this.caseList = caseList;
    }

    /**
     * @param periodList the periodList to set
     */
    public void setPeriodList(List<OccPeriodPropertyUnitHeavy> periodList) {
        this.periodList = periodList;
    }

    /**
     * @param propertyList the propertyList to set
     */
    public void setPropertyList(List<Property> propertyList) {
        this.propertyList = propertyList;
    }

    /**
     * @param eventList the eventList to set
     */
    public void setEventList(List<EventCnFPropUnitCasePeriodHeavy> eventList) {
        this.eventList = eventList;
    }

 
   
    
}
