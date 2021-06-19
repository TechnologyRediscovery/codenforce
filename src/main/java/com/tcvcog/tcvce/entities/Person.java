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

import com.tcvcog.tcvce.application.interfaces.IFace_ActivatableBOB;
import com.tcvcog.tcvce.application.interfaces.IFace_Loggable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

/**
 * Model object representing a person in the system. A Person has a type
 coordinated through the enum @PersonType. Contains getters and setters for
 database fields related to a Person, stored in the person table. 
 * 
 * @author Eric Darsow
 */
public  class       Person 
        extends     Human
        implements  IFace_Loggable{
    
    protected List<ContactEmail> emailList;
    protected List<ContactPhone> phoneList;
    protected List<MailingAddress> addressList;

    public Person() {
    }

    /**
     * Method for cloning Person objects
     * 
     * @param input The person we would like to clone
     */
    public Person(Person input) {
       
    }

    /**
     * @return the emailList
     */
    public List<ContactEmail> getEmailList() {
        return emailList;
    }

    /**
     * @return the phoneList
     */
    public List<ContactPhone> getPhoneList() {
        return phoneList;
    }

    /**
     * @return the addressList
     */
    public List<MailingAddress> getAddressList() {
        return addressList;
    }

    /**
     * @param emailList the emailList to set
     */
    public void setEmailList(List<ContactEmail> emailList) {
        this.emailList = emailList;
    }

    /**
     * @param phoneList the phoneList to set
     */
    public void setPhoneList(List<ContactPhone> phoneList) {
        this.phoneList = phoneList;
    }

    /**
     * @param addressList the addressList to set
     */
    public void setAddressList(List<MailingAddress> addressList) {
        this.addressList = addressList;
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

    

}
