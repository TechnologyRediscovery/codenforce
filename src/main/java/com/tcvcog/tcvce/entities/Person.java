/*
 * Copyright (C) 2017 Turtle Creek Valley Council of Governements
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

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model object representing a person in the system. A Person has a type
 * coordinated through the enum @PersonType. Contains getters and setters for
 * database fields related to a Person, stored in the person table. 
 * 
 * @author Eric Darsow
 */
public class Person implements Serializable{
    
    private int personID;
    
    private PersonType personType;
    private Municipality muni;
    private int muniCode;
    
    private int sourceID;
    private String sourceTitle;
    private User creator;
    
    // for backwards compatability
    
    private String firstName;
    private String lastName;
    
    // frist, middle initial, and last all in lastName
    private boolean compositeLastName;
    private boolean businessEntity;
    
    private String jobTitle;
    
    private String phoneCell;
    private String phoneHome;
    private String phoneWork;
    
    private String email;
    private String address_street;
    private String address_city;
    
    private String address_zip;
    private String address_state;
    private boolean addressOfResidence;
    
    private String mailing_address_street;
    private String mailing_address_city;
    private String mailing_address_zip;
    
    private String mailing_address_state;
    // postgres defaults this to true
    private boolean mailingSameAsResidence;
    private String notes;
    
    private LocalDateTime lastUpdated;
    
    private LocalDateTime expiryDate;
    private String expiryNotes;
    private boolean active;
    
    /**
     * Tenancy tracking
     */
    private boolean under18;
    private User verifiedBy;
    

    /**
     * @return the personID
     */
    public int getPersonID() {
        return personID;
    }

    /**
     * @param personID the personID to set
     */
    public void setPersonID(int personID) {
        this.personID = personID;
    }

  
    /**
     * @return the phoneCell
     */
    public String getPhoneCell() {
        return phoneCell;
    }

    /**
     * @param phoneCell the phoneCell to set
     */
    public void setPhoneCell(String phoneCell) {
        this.phoneCell = phoneCell;
    }

    /**
     * @return the phoneHome
     */
    public String getPhoneHome() {
        return phoneHome;
    }

    /**
     * @param phoneHome the phoneHome to set
     */
    public void setPhoneHome(String phoneHome) {
        this.phoneHome = phoneHome;
    }

    /**
     * @return the phoneWork
     */
    public String getPhoneWork() {
        return phoneWork;
    }

    /**
     * @param phoneWork the phoneWork to set
     */
    public void setPhoneWork(String phoneWork) {
        this.phoneWork = phoneWork;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the address_street
     */
    public String getAddress_street() {
        return address_street;
    }

    /**
     * @param address_street the address_street to set
     */
    public void setAddress_street(String address_street) {
        this.address_street = address_street;
    }

    /**
     * @return the address_city
     */
    public String getAddress_city() {
        return address_city;
    }

    /**
     * @param address_city the address_city to set
     */
    public void setAddress_city(String address_city) {
        this.address_city = address_city;
    }

    /**
     * @return the address_zip
     */
    public String getAddress_zip() {
        return address_zip;
    }

    /**
     * @param address_zip the address_zip to set
     */
    public void setAddress_zip(String address_zip) {
        this.address_zip = address_zip;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the personType
     */
    public PersonType getPersonType() {
        return personType;
    }

    /**
     * @param personType the personType to set
     */
    public void setPersonType(PersonType personType) {
        this.personType = personType;
    }

    /**
     * @return the lastUpdated
     */
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    /**
     * @param lastUpdated the lastUpdated to set
     */
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * @return the address_state
     */
    public String getAddress_state() {
        return address_state;
    }

    /**
     * @param address_state the address_state to set
     */
    public void setAddress_state(String address_state) {
        this.address_state = address_state;
    }

    /**
     * @return the jobTitle
     */
    public String getJobTitle() {
        return jobTitle;
    }

    /**
     * @param jobTitle the jobTitle to set
     */
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    /**
     * @return the expiryDate
     */
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    /**
     * @param expiryDate the expiryDate to set
     */
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the under18
     */
    public boolean isUnder18() {
        return under18;
    }

    /**
     * @param under18 the under18 to set
     */
    public void setUnder18(boolean under18) {
        this.under18 = under18;
    }

    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the muniCode
     */
    public int getMuniCode() {
        muniCode = muni.getMuniCode();
        return muniCode;
    }

    /**
     * @param muniCode the muniCode to set
     */
    public void setMuniCode(int muniCode) {
        this.muniCode = muniCode;
    }
    
    @Override
    public String toString(){
        return this.firstName + this.lastName;
    }

    /**
     * @return the sourceID
     */
    public int getSourceID() {
        return sourceID;
    }

    /**
     * @return the sourceTitle
     */
    public String getSourceTitle() {
        return sourceTitle;
    }

    /**
     * @return the creator
     */
    public User getCreator() {
        return creator;
    }

    /**
     * @return the businessEntity
     */
    public boolean isBusinessEntity() {
        return businessEntity;
    }

    /**
     * @return the addressOfResidence
     */
    public boolean isAddressOfResidence() {
        return addressOfResidence;
    }

    /**
     * @return the mailing_address_street
     */
    public String getMailing_address_street() {
        return mailing_address_street;
    }

    /**
     * @return the mailing_address_city
     */
    public String getMailing_address_city() {
        return mailing_address_city;
    }

    /**
     * @return the mailing_address_zip
     */
    public String getMailing_address_zip() {
        return mailing_address_zip;
    }

    /**
     * @return the mailing_address_state
     */
    public String getMailing_address_state() {
        return mailing_address_state;
    }

    /**
     * @return the mailingSameAsResidence
     */
    public boolean isMailingSameAsResidence() {
        return mailingSameAsResidence;
    }

    /**
     * @return the expiryNotes
     */
    public String getExpiryNotes() {
        return expiryNotes;
    }

    

    /**
     * @param sourceID the sourceID to set
     */
    public void setSourceID(int sourceID) {
        this.sourceID = sourceID;
    }

    /**
     * @param sourceTitle the sourceTitle to set
     */
    public void setSourceTitle(String sourceTitle) {
        this.sourceTitle = sourceTitle;
    }

    /**
     * @param creator the creator to set
     */
    public void setCreator(User creator) {
        this.creator = creator;
    }

    /**
     * @param businessEntity the businessEntity to set
     */
    public void setBusinessEntity(boolean businessEntity) {
        this.businessEntity = businessEntity;
    }

    /**
     * @param addressOfResidence the addressOfResidence to set
     */
    public void setAddressOfResidence(boolean addressOfResidence) {
        this.addressOfResidence = addressOfResidence;
    }

    /**
     * @param mailing_address_street the mailing_address_street to set
     */
    public void setMailing_address_street(String mailing_address_street) {
        this.mailing_address_street = mailing_address_street;
    }

    /**
     * @param mailing_address_city the mailing_address_city to set
     */
    public void setMailing_address_city(String mailing_address_city) {
        this.mailing_address_city = mailing_address_city;
    }

    /**
     * @param mailing_address_zip the mailing_address_zip to set
     */
    public void setMailing_address_zip(String mailing_address_zip) {
        this.mailing_address_zip = mailing_address_zip;
    }

    /**
     * @param mailing_address_state the mailing_address_state to set
     */
    public void setMailing_address_state(String mailing_address_state) {
        this.mailing_address_state = mailing_address_state;
    }

    /**
     * @param mailingSameAsResidence the mailingSameAsResidence to set
     */
    public void setMailingSameAsResidence(boolean mailingSameAsResidence) {
        this.mailingSameAsResidence = mailingSameAsResidence;
    }

    /**
     * @param expiryNotes the expiryNotes to set
     */
    public void setExpiryNotes(String expiryNotes) {
        this.expiryNotes = expiryNotes;
    }

    /**
     * @return the compositeLastName
     */
    public boolean isCompositeLastName() {
        return compositeLastName;
    }

    /**
     * @param compositeLastName the compositeLastName to set
     */
    public void setCompositeLastName(boolean compositeLastName) {
        this.compositeLastName = compositeLastName;
    }

    /**
     * @return the verifiedBy
     */
    public User getVerifiedBy() {
        return verifiedBy;
    }

    /**
     * @param verifiedBy the verifiedBy to set
     */
    public void setVerifiedBy(User verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

}
