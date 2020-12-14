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

import com.tcvcog.tcvce.application.interfaces.IFace_Loggable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Model object representing a person in the system. A Person has a type
 coordinated through the enum @PersonType. Contains getters and setters for
 database fields related to a Person, stored in the person table. 
 * 
 * @author Eric Darsow
 */
public  class       Person 
        extends     BOb
        implements  IFace_Loggable{
    
    protected int personID;
    
    protected PersonType personType;
    /**
     * Used this to avoid cycles in Municipality creation
     * but now with a super and subclass of muni, we don't need them
     * @deprecated 
     */
    
    protected int muniCode;
    protected Municipality muni;
    /**
     * Used this to avoid cycles in Municipality creation
     * but now with a super and subclass of muni, we don't need them
     * @deprecated 
     */
    protected String muniName;
    protected BOBSource source;
    
    /**
     * We don't use a User object here due to cycling
     */
    protected int creatorUserID;
    protected LocalDateTime creationTimeStamp;
    
    // for backwards compatability
    
    protected String firstName;
    protected String lastName;
    
    // first, middle initial, and last all in lastName
    protected boolean compositeLastName;
    protected boolean businessEntity;
    
    protected String jobTitle;
    
    protected String phoneCell;
    protected String phoneHome;
    protected String phoneWork;
    
    protected String email;
    protected String addressStreet;
    protected String addressCity;
    
    protected String addressZip;
    protected String addressState;
    
    protected boolean useSeparateMailingAddress;
    protected String mailingAddressStreet;
    protected String mailingAddressThirdLine;
    protected String mailingAddressCity;
    protected String mailingAddressZip;
    
    protected String mailingAddressState;
    
    protected String notes;
    
    protected LocalDateTime lastUpdated;
    protected String lastUpdatedPretty;
    
    protected boolean canExpire;
    protected LocalDateTime expiryDate;
    protected String expireString;
    protected java.util.Date expiryDateUtilDate;
    protected String expiryNotes;
    protected boolean active;
    protected int linkedUserID;
    
    /**
     * Tenancy tracking
     */
    protected boolean under18;
    protected int verifiedByUserID;
    
    protected boolean referencePerson;
    
    protected LocalDateTime ghostCreatedDate;
    protected String ghostCreatedDatePretty;
    protected int ghostOf;
    protected int ghostCreatedByUserID;
    
    protected LocalDateTime cloneCreatedDate;
    protected String cloneCreatedDatePretty;
    protected int cloneOf;
    protected int cloneCreatedByUserID;
    
    protected ArrayList<Integer> ghostsList;
    protected ArrayList<Integer> cloneList;
    protected ArrayList<Integer> mergedList;

    public Person() {
    }

    /**
     * Method for cloning Person objects
     * 
     * @param input The person we would like to clone
     */
    public Person(Person input) {
        personID = input.getPersonID(); 
        personType = input.getPersonType();
        muniCode = input.getMuniCode();
        muniName = input.getMuniName();
        source = input.getSource();
        creatorUserID = input.getCreatorUserID();
        creationTimeStamp = input.getCreationTimeStamp();
        firstName = input.getFirstName();
        lastName = input.getLastName();
        compositeLastName = input.isCompositeLastName();
        businessEntity = input.isBusinessEntity();
        jobTitle = input.getJobTitle();
        phoneCell = input.getPhoneCell();
        phoneHome = input.getPhoneHome();
        phoneWork = input.getPhoneWork();
        email = input.getEmail();
        addressStreet = input.getAddressStreet();
        addressCity = input.getAddressCity();
        addressZip = input.getAddressZip();
        addressState = input.getAddressState();
        useSeparateMailingAddress = input.isUseSeparateMailingAddress();
        mailingAddressStreet = input.getMailingAddressStreet();
        mailingAddressThirdLine = input.getMailingAddressThirdLine();
        mailingAddressCity = input.getMailingAddressCity();
        mailingAddressZip = input.getMailingAddressZip();
        mailingAddressState = input.getMailingAddressState();
        notes = input.getNotes();
        lastUpdated = input.getLastUpdated();
        lastUpdatedPretty = input.getLastUpdatedPretty();
        canExpire = input.isCanExpire();
        expiryDate = input.getExpiryDate();
        expireString = input.getExpireString();
        expiryDateUtilDate = input.getExpiryDateUtilDate();
        expiryNotes = input.getExpiryNotes();
        active = input.isActive();
        linkedUserID = input.getLinkedUserID();
        under18 = input.isUnder18();
        verifiedByUserID = input.getVerifiedByUserID();
        referencePerson = input.isReferencePerson();
        ghostCreatedDate = input.getGhostCreatedDate();
        ghostCreatedDatePretty = input.getGhostCreatedDatePretty();
        ghostOf = input.getGhostOf();
        ghostCreatedByUserID = input.getGhostCreatedByUserID();
        cloneCreatedDate = input.getCloneCreatedDate();
        cloneCreatedDatePretty = input.getCloneCreatedDatePretty();
        cloneOf = input.getCloneOf();
        cloneCreatedByUserID = input.getCloneCreatedByUserID();
        ghostsList = input.getGhostsList();
        cloneList = input.getCloneList();
        mergedList = input.getMergedList();
    }
    
    
    

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
     * @return the businessEntity
     */
    public boolean isBusinessEntity() {
        return businessEntity;
    }

    /**
     * @return the useSeparateMailingAddress
     */
    public boolean isUseSeparateMailingAddress() {
        return useSeparateMailingAddress;
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
     * @return the expiryNotes
     */
    public String getExpiryNotes() {
        return expiryNotes;
    }

    


    /**
     * @param businessEntity the businessEntity to set
     */
    public void setBusinessEntity(boolean businessEntity) {
        this.businessEntity = businessEntity;
    }

    /**
     * @param useSeparateMailingAddress the useSeparateMailingAddress to set
     */
    public void setUseSeparateMailingAddress(boolean useSeparateMailingAddress) {
        this.useSeparateMailingAddress = useSeparateMailingAddress;
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

   

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.personID;
        hash = 79 * hash + Objects.hashCode(this.personType);
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
        hash = 79 * hash + (this.useSeparateMailingAddress ? 1 : 0);
        hash = 79 * hash + Objects.hashCode(this.mailingAddressStreet);
        hash = 79 * hash + Objects.hashCode(this.mailingAddressCity);
        hash = 79 * hash + Objects.hashCode(this.mailingAddressZip);
        hash = 79 * hash + Objects.hashCode(this.mailingAddressState);
        hash = 79 * hash + Objects.hashCode(this.notes);
        hash = 79 * hash + Objects.hashCode(this.lastUpdated);
        hash = 79 * hash + Objects.hashCode(this.expiryDate);
        hash = 79 * hash + Objects.hashCode(this.expiryNotes);
        hash = 79 * hash + (this.active ? 1 : 0);
        hash = 79 * hash + (this.under18 ? 1 : 0);
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
       
        return true;
    }


    /**
     * @return the canExpire
     */
    public boolean isCanExpire() {
        return canExpire;
    }

    /**
     * @param canExpire the canExpire to set
     */
    public void setCanExpire(boolean canExpire) {
        this.canExpire = canExpire;
    }

    

    /**
     * @return the creationTimeStamp
     */
    public LocalDateTime getCreationTimeStamp() {
        return creationTimeStamp;
    }

    /**
     * @param creationTimeStamp the creationTimeStamp to set
     */
    public void setCreationTimeStamp(LocalDateTime creationTimeStamp) {
        this.creationTimeStamp = creationTimeStamp;
    }

    /**
     * @return the expiryDateUtilDate
     */
    public java.util.Date getExpiryDateUtilDate() {
        expiryDateUtilDate = convertUtilDate(expiryDate);
        return expiryDateUtilDate;
    }

    /**
     * @param edut
     */
    public void setExpiryDateUtilDate(java.util.Date edut) {
        expiryDateUtilDate = edut;
        expiryDate = convertUtilDate(edut);
    }

    /**
     * @return the expireString
     */
    public String getExpireString() {
        expireString = EntityUtils.getPrettyDate(expiryDate);
        return expireString;
        
    }

    /**
     * @param expireString the expireString to set
     */
    public void setExpireString(String expireString) {
        this.expireString = expireString;
    }

    /**
     * @return the lastUpdatedPretty
     */
    public String getLastUpdatedPretty() {
        lastUpdatedPretty = EntityUtils.getPrettyDate(lastUpdated);
        return lastUpdatedPretty;
    }

    /**
     * @param lastUpdatedPretty the lastUpdatedPretty to set
     */
    public void setLastUpdatedPretty(String lastUpdatedPretty) {
        this.lastUpdatedPretty = lastUpdatedPretty;
    }

  
   

    /**
     * @return the verifiedByUserID
     */
    public int getVerifiedByUserID() {
        return verifiedByUserID;
    }

    /**
     * @param verifiedByUserID the verifiedByUserID to set
     */
    public void setVerifiedByUserID(int verifiedByUserID) {
        this.verifiedByUserID = verifiedByUserID;
    }

    /**
     * @return the linkedUserID
     */
    public int getLinkedUserID() {
        return linkedUserID;
    }

    /**
     * @param linkedUserID the linkedUserID to set
     */
    public void setLinkedUserID(int linkedUserID) {
        this.linkedUserID = linkedUserID;
    }

    /**
     * @return the creatorUserID
     */
    public int getCreatorUserID() {
        return creatorUserID;
    }

    /**
     * @param creatorUserID the creatorUserID to set
     */
    public void setCreatorUserID(int creatorUserID) {
        this.creatorUserID = creatorUserID;
    }

    /**
     * @return the muniName
     */
    public String getMuniName() {
        if(this.muni != null){
            muniName = muni.getMuniName();
        }
        return muniName;
    }

    /**
     * @param muniName the muniName to set
     */
    public void setMuniName(String muniName) {
        this.muniName = muniName;
    }

    /**
     * @return the muniCode
     */
    public int getMuniCode() {
        if(this.muni != null){
            muniCode = muni.muniCode;
        }
        return muniCode;
    }

    /**
     * @param muniCode the muniCode to set
     */
    public void setMuniCode(int muniCode) {
        this.muniCode = muniCode;
    }

    /**
     * @return the ghostCreatedDate
     */
    public LocalDateTime getGhostCreatedDate() {
        return ghostCreatedDate;
    }

    /**
     * @return the ghostCreatedDatePretty
     */
    public String getGhostCreatedDatePretty() {
        return ghostCreatedDatePretty;
    }

    /**
     * @return the ghostOf
     */
    public int getGhostOf() {
        return ghostOf;
    }

    /**
     * @return the ghostCreatedByUserID
     */
    public int getGhostCreatedByUserID() {
        return ghostCreatedByUserID;
    }

    /**
     * @return the cloneCreatedDate
     */
    public LocalDateTime getCloneCreatedDate() {
        return cloneCreatedDate;
    }

    /**
     * @return the cloneCreatedDatePretty
     */
    public String getCloneCreatedDatePretty() {
        return cloneCreatedDatePretty;
    }

    /**
     * @return the cloneOf
     */
    public int getCloneOf() {
        return cloneOf;
    }

    /**
     * @return the cloneCreatedByUserID
     */
    public int getCloneCreatedByUserID() {
        return cloneCreatedByUserID;
    }

    /**
     * @param ghostCreatedDate the ghostCreatedDate to set
     */
    public void setGhostCreatedDate(LocalDateTime ghostCreatedDate) {
        this.ghostCreatedDate = ghostCreatedDate;
    }

    /**
     * @param ghostCreatedDatePretty the ghostCreatedDatePretty to set
     */
    public void setGhostCreatedDatePretty(String ghostCreatedDatePretty) {
        this.ghostCreatedDatePretty = ghostCreatedDatePretty;
    }

    /**
     * @param ghostOf the ghostOf to set
     */
    public void setGhostOf(int ghostOf) {
        this.ghostOf = ghostOf;
    }

    /**
     * @param ghostCreatedByUserID the ghostCreatedByUserID to set
     */
    public void setGhostCreatedByUserID(int ghostCreatedByUserID) {
        this.ghostCreatedByUserID = ghostCreatedByUserID;
    }

    /**
     * @param cloneCreatedDate the cloneCreatedDate to set
     */
    public void setCloneCreatedDate(LocalDateTime cloneCreatedDate) {
        this.cloneCreatedDate = cloneCreatedDate;
    }

    /**
     * @param cloneCreatedDatePretty the cloneCreatedDatePretty to set
     */
    public void setCloneCreatedDatePretty(String cloneCreatedDatePretty) {
        this.cloneCreatedDatePretty = cloneCreatedDatePretty;
    }

    /**
     * @param cloneOf the cloneOf to set
     */
    public void setCloneOf(int cloneOf) {
        this.cloneOf = cloneOf;
    }

    /**
     * @param cloneCreatedByUserID the cloneCreatedByUserID to set
     */
    public void setCloneCreatedByUserID(int cloneCreatedByUserID) {
        this.cloneCreatedByUserID = cloneCreatedByUserID;
    }

    /**
     * @return the mailingAddressThirdLine
     */
    public String getMailingAddressThirdLine() {
        return mailingAddressThirdLine;
    }

    /**
     * @param mailingAddressThirdLine the mailingAddressThirdLine to set
     */
    public void setMailingAddressThirdLine(String mailingAddressThirdLine) {
        this.mailingAddressThirdLine = mailingAddressThirdLine;
    }

    /**
     * @return the referencePerson
     */
    public boolean isReferencePerson() {
        return referencePerson;
    }

    /**
     * @param referencePerson the referencePerson to set
     */
    public void setReferencePerson(boolean referencePerson) {
        this.referencePerson = referencePerson;
    }

    /**
     * @return the ghostsList
     */
    public ArrayList<Integer> getGhostsList() {
        return ghostsList;
    }

    /**
     * @return the cloneList
     */
    public ArrayList<Integer> getCloneList() {
        return cloneList;
    }

    /**
     * @return the mergedList
     */
    public ArrayList<Integer> getMergedList() {
        return mergedList;
    }

    /**
     * @param ghostsList the ghostsList to set
     */
    public void setGhostsList(ArrayList<Integer> ghostsList) {
        this.ghostsList = ghostsList;
    }

    /**
     * @param cloneList the cloneList to set
     */
    public void setCloneList(ArrayList<Integer> cloneList) {
        this.cloneList = cloneList;
    }

    /**
     * @param mergedList the mergedList to set
     */
    public void setMergedList(ArrayList<Integer> mergedList) {
        this.mergedList = mergedList;
    }

    
    /**
     * @return the source
     */
    public BOBSource getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(BOBSource source) {
        this.source = source;
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
        if(muni != null){
            this.muniCode = muni.getMuniCode();
            this.muniName = muni.getMuniName();
        }
        this.muni = muni;
    }

   

}
