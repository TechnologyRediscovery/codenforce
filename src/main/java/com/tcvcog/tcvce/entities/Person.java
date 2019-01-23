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
import java.util.Objects;

/**
 * Model object representing a person in the system. A Person has a type
 * coordinated through the enum @PersonType. Contains getters and setters for
 * database fields related to a Person, stored in the person table. 
 * 
 * @author Eric Darsow
 */
public class Person extends EntityUtils implements Serializable{
    
    private int personID;
    
    private PersonType personType;
    private Municipality muni;
    
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
    private String addressStreet;
    private String addressCity;
    
    private String addressZip;
    private String addressState;
    private boolean addressOfResidence;
    
    private String mailingAddressStreet;
    private String mailingAddressCity;
    private String mailingAddressZip;
    
    private String mailingAddressState;
    // postgres defaults this to true
    private boolean mailingSameAsResidence;
    private String notes;
    
    private LocalDateTime lastUpdated;
    private String lastUpdatedPretty;
    
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
     * @return the addressStreet
     */
    public String getAddressStreet() {
        return addressStreet;
    }

    /**
     * @param addressStreet the addressStreet to set
     */
    public void setAddressStreet(String addressStreet) {
        this.addressStreet = addressStreet;
    }

    /**
     * @return the addressCity
     */
    public String getAddressCity() {
        return addressCity;
    }

    /**
     * @param addressCity the addressCity to set
     */
    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    /**
     * @return the addressZip
     */
    public String getAddressZip() {
        return addressZip;
    }

    /**
     * @param addressZip the addressZip to set
     */
    public void setAddressZip(String addressZip) {
        this.addressZip = addressZip;
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
     * @return the addressState
     */
    public String getAddressState() {
        return addressState;
    }

    /**
     * @param addressState the addressState to set
     */
    public void setAddressState(String addressState) {
        this.addressState = addressState;
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
     * @return the mailingAddressStreet
     */
    public String getMailingAddressStreet() {
        return mailingAddressStreet;
    }

    /**
     * @return the mailingAddressCity
     */
    public String getMailingAddressCity() {
        return mailingAddressCity;
    }

    /**
     * @return the mailingAddressZip
     */
    public String getMailingAddressZip() {
        return mailingAddressZip;
    }

    /**
     * @return the mailingAddressState
     */
    public String getMailingAddressState() {
        return mailingAddressState;
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
     * @param mailingAddressStreet the mailingAddressStreet to set
     */
    public void setMailingAddressStreet(String mailingAddressStreet) {
        this.mailingAddressStreet = mailingAddressStreet;
    }

    /**
     * @param mailingAddressCity the mailingAddressCity to set
     */
    public void setMailingAddressCity(String mailingAddressCity) {
        this.mailingAddressCity = mailingAddressCity;
    }

    /**
     * @param mailingAddressZip the mailingAddressZip to set
     */
    public void setMailingAddressZip(String mailingAddressZip) {
        this.mailingAddressZip = mailingAddressZip;
    }

    /**
     * @param mailingAddressState the mailingAddressState to set
     */
    public void setMailingAddressState(String mailingAddressState) {
        this.mailingAddressState = mailingAddressState;
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.personID;
        hash = 79 * hash + Objects.hashCode(this.personType);
        hash = 79 * hash + Objects.hashCode(this.muni);
        hash = 79 * hash + this.sourceID;
        hash = 79 * hash + Objects.hashCode(this.sourceTitle);
        hash = 79 * hash + Objects.hashCode(this.creator);
        hash = 79 * hash + Objects.hashCode(this.firstName);
        hash = 79 * hash + Objects.hashCode(this.lastName);
        hash = 79 * hash + (this.compositeLastName ? 1 : 0);
        hash = 79 * hash + (this.businessEntity ? 1 : 0);
        hash = 79 * hash + Objects.hashCode(this.jobTitle);
        hash = 79 * hash + Objects.hashCode(this.phoneCell);
        hash = 79 * hash + Objects.hashCode(this.phoneHome);
        hash = 79 * hash + Objects.hashCode(this.phoneWork);
        hash = 79 * hash + Objects.hashCode(this.email);
        hash = 79 * hash + Objects.hashCode(this.addressStreet);
        hash = 79 * hash + Objects.hashCode(this.addressCity);
        hash = 79 * hash + Objects.hashCode(this.addressZip);
        hash = 79 * hash + Objects.hashCode(this.addressState);
        hash = 79 * hash + (this.addressOfResidence ? 1 : 0);
        hash = 79 * hash + Objects.hashCode(this.mailingAddressStreet);
        hash = 79 * hash + Objects.hashCode(this.mailingAddressCity);
        hash = 79 * hash + Objects.hashCode(this.mailingAddressZip);
        hash = 79 * hash + Objects.hashCode(this.mailingAddressState);
        hash = 79 * hash + (this.mailingSameAsResidence ? 1 : 0);
        hash = 79 * hash + Objects.hashCode(this.notes);
        hash = 79 * hash + Objects.hashCode(this.lastUpdated);
        hash = 79 * hash + Objects.hashCode(this.expiryDate);
        hash = 79 * hash + Objects.hashCode(this.expiryNotes);
        hash = 79 * hash + (this.active ? 1 : 0);
        hash = 79 * hash + (this.under18 ? 1 : 0);
        hash = 79 * hash + Objects.hashCode(this.verifiedBy);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Person other = (Person) obj;
        if (this.personID != other.personID) {
            return false;
        }
        if (this.sourceID != other.sourceID) {
            return false;
        }
        if (this.compositeLastName != other.compositeLastName) {
            return false;
        }
        if (this.businessEntity != other.businessEntity) {
            return false;
        }
        if (this.addressOfResidence != other.addressOfResidence) {
            return false;
        }
        if (this.mailingSameAsResidence != other.mailingSameAsResidence) {
            return false;
        }
        if (this.active != other.active) {
            return false;
        }
        if (this.under18 != other.under18) {
            return false;
        }
        if (!Objects.equals(this.sourceTitle, other.sourceTitle)) {
            return false;
        }
        if (!Objects.equals(this.firstName, other.firstName)) {
            return false;
        }
        if (!Objects.equals(this.lastName, other.lastName)) {
            return false;
        }
        if (!Objects.equals(this.jobTitle, other.jobTitle)) {
            return false;
        }
        if (!Objects.equals(this.phoneCell, other.phoneCell)) {
            return false;
        }
        if (!Objects.equals(this.phoneHome, other.phoneHome)) {
            return false;
        }
        if (!Objects.equals(this.phoneWork, other.phoneWork)) {
            return false;
        }
        if (!Objects.equals(this.email, other.email)) {
            return false;
        }
        if (!Objects.equals(this.addressStreet, other.addressStreet)) {
            return false;
        }
        if (!Objects.equals(this.addressCity, other.addressCity)) {
            return false;
        }
        if (!Objects.equals(this.addressZip, other.addressZip)) {
            return false;
        }
        if (!Objects.equals(this.addressState, other.addressState)) {
            return false;
        }
        if (!Objects.equals(this.mailingAddressStreet, other.mailingAddressStreet)) {
            return false;
        }
        if (!Objects.equals(this.mailingAddressCity, other.mailingAddressCity)) {
            return false;
        }
        if (!Objects.equals(this.mailingAddressZip, other.mailingAddressZip)) {
            return false;
        }
        if (!Objects.equals(this.mailingAddressState, other.mailingAddressState)) {
            return false;
        }
        if (!Objects.equals(this.notes, other.notes)) {
            return false;
        }
        if (!Objects.equals(this.expiryNotes, other.expiryNotes)) {
            return false;
        }
        if (this.personType != other.personType) {
            return false;
        }
        if (!Objects.equals(this.muni, other.muni)) {
            return false;
        }
        if (!Objects.equals(this.creator, other.creator)) {
            return false;
        }
        if (!Objects.equals(this.lastUpdated, other.lastUpdated)) {
            return false;
        }
        if (!Objects.equals(this.expiryDate, other.expiryDate)) {
            return false;
        }
        if (!Objects.equals(this.verifiedBy, other.verifiedBy)) {
            return false;
        }
        return true;
    }

    /**
     * @return the lastUpdatedPretty
     */
    public String getLastUpdatedPretty() {
        String s = getPrettyDate(lastUpdated);
        return s;
    }

    /**
     * @param lastUpdatedPretty the lastUpdatedPretty to set
     */
    public void setLastUpdatedPretty(String lastUpdatedPretty) {
        this.lastUpdatedPretty = lastUpdatedPretty;
    }
    

}
