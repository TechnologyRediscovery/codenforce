/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;

/**
 *
 * @author sylvia
 */
public class        PersonOccApplication 
        extends     Person 
        implements  Serializable{
    
    private int applicationID;
    private boolean applicant;
    private boolean preferredContact;
    private PersonType applicationPersonType;
    private boolean linkActive; //stores if this link is active in the database, not the person object itself

    public PersonOccApplication() {
    }
    
    public PersonOccApplication(Person p){
        this.personID = p.personID;

        this.personType = p.personType;
        this.applicationPersonType = p.personType;
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
     * @return the preferredContact
     */
    public boolean isPreferredContact() {
        return preferredContact;
    }

    /**
     * @return the applicationPersonType
     */
    public PersonType getApplicationPersonType() {
        return applicationPersonType;
    }

    @Override
    public PersonType getPersonType() {
        return personType;
    }

    @Override
    public void setPersonType(PersonType personType) {
        this.personType = personType;
    }
   
    /**
     * @param preferredContact the preferredContact to set
     */
    public void setPreferredContact(boolean preferredContact) {
        this.preferredContact = preferredContact;
    }

    /**
     * @param applicationPersonType the applicationPersonType to set
     */
    public void setApplicationPersonType(PersonType applicationPersonType) {
        this.applicationPersonType = applicationPersonType;
    }

    public boolean isApplicant() {
        return applicant;
    }

    public void setApplicant(boolean applicant) {
        this.applicant = applicant;
    }

    public boolean isLinkActive() {
        return linkActive;
    }

    public void setLinkActive(boolean linkActive) {
        this.linkActive = linkActive;
    }

    public int getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(int applicationID) {
        this.applicationID = applicationID;
    }
    
}
