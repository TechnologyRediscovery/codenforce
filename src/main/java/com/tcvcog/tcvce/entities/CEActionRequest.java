/*
 * Copyright (C) 2017 Eric C. Darsow
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Models the entity: code enforcement action request.This object blends data from several database tables to create
 a conceptual model of a single request.
 
 The JSF page for submitting a request act on an CEActionRequest object
 and will edit the values of member variables
 on this object and can then ask for the request to be read into the DB.
 
 A reference to an CEActionRequest object will be attached to the 
 Visit object such that both public and logged in users will have access
 to an CEActionRequest to do with as they please (print, etc.)
 * @author Eric Darsow
 */
public class CEActionRequest implements Serializable{
    
    // requests no longer have a status--remove when fully updated
    // for the full case model
    //private RequestStatus requestStatus;
    
    private int requestID;
    private int requestPublicCC;
    private CEActionRequestStatus requestStatus;

    private Municipality muni;
    private int personID;
    private Person actionRequestorPerson;

    private Property requestProperty;
   
    private int issueType_issueTypeID;
    private String issueTypeString; //populated from linked table
    
    private int muniCode;
    private int caseID;
    
    private java.time.LocalDateTime submittedTimeStamp;
    private String formattedSubmittedTimeStamp;
    
    private java.time.LocalDateTime dateOfRecord;
    private long daysSinceDateOfRecord;
    
    private java.util.Date dateOfRecordUtilDate;

    private boolean isAtKnownAddress;
    private String addressOfConcern;
    
    private String requestDescription;
    private boolean isUrgent;
    private boolean anonymitiyRequested;
    
    private String cogInternalNotes;
    private String muniNotes;
    private String publicExternalNotes;
    // end threes
    
    // these are populated on the lookup when the linked
    // tables with the String values are selected
    

    /**
     * Creates a new instance of ActionRequest
     */
    public CEActionRequest() {
    }

    /**
     * @return the requestID
     */
    public int getRequestID() {
        return requestID;
    }

    /**
     * @param requestID the requestID to set
     */
    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }

    /**
     * @return the issueType_issueTypeID
     */
    public int getIssueType_issueTypeID() {
        return issueType_issueTypeID;
    }

    /**
     * @param issueType_issueTypeID the issueType_issueTypeID to set
     */
    public void setIssueType_issueTypeID(int issueType_issueTypeID) {
        this.issueType_issueTypeID = issueType_issueTypeID;
    }

    /**
     * @return the issueTypeString
     */
    public String getIssueTypeString() {
        return issueTypeString;
    }

    /**
     * @param issueTypeString the issueTypeString to set
     */
    public void setIssueTypeString(String issueTypeString) {
        this.issueTypeString = issueTypeString;
    }


    /**
     * @return the addressOfConcern
     */
    public String getAddressOfConcern() {
        return addressOfConcern;
    }

    /**
     * @param addressOfConcern the addressOfConcern to set
     */
    public void setAddressOfConcern(String addressOfConcern) {
        this.addressOfConcern = addressOfConcern;
    }

    /**
     * @return the isAtKnownAddress
     */
    public boolean isIsAtKnownAddress() {
        return isAtKnownAddress;
    }

    /**
     * @param isAtKnownAddress the isAtKnownAddress to set
     */
    public void setIsAtKnownAddress(boolean isAtKnownAddress) {
        this.isAtKnownAddress = isAtKnownAddress;
    }
    
    public boolean getNotAtAddress(){
        return isAtKnownAddress;
    }

  

    /**
     * @return the requestDescription
     */
    public String getRequestDescription() {
        return requestDescription;
    }

    /**
     * @param requestDescription the requestDescription to set
     */
    public void setRequestDescription(String requestDescription) {
        this.requestDescription = requestDescription;
    }

    /**
     * @return the isUrgent
     */
    public boolean isIsUrgent() {
        return isUrgent;
    }

    /**
     * @param isUrgent the isUrgent to set
     */
    public void setIsUrgent(boolean isUrgent) {
        this.isUrgent = isUrgent;
    }

    /**
     * @return the dateOfRecord
     */
    public LocalDateTime getDateOfRecord() {
        return dateOfRecord;
    }

    /**
     * @param dateOfRecord the dateOfRecord to set
     */
    public void setDateOfRecord(LocalDateTime dateOfRecord) {
        this.dateOfRecord = dateOfRecord;
    }

    
    /**
     * @return the cogInternalNotes
     */
    public String getCogInternalNotes() {
        return cogInternalNotes;
    }

    /**
     * @param cogInternalNotes the cogInternalNotes to set
     */
    public void setCogInternalNotes(String cogInternalNotes) {
        this.cogInternalNotes = cogInternalNotes;
    }

    /**
     * @return the muniNotes
     */
    public String getMuniNotes() {
        return muniNotes;
    }

    /**
     * @param muniNotes the muniNotes to set
     */
    public void setMuniNotes(String muniNotes) {
        this.muniNotes = muniNotes;
    }

    /**
     * @return the publicExternalNotes
     */
    public String getPublicExternalNotes() {
        return publicExternalNotes;
    }

    /**
     * @param publicExternalNotes the publicExternalNotes to set
     */
    public void setPublicExternalNotes(String publicExternalNotes) {
        this.publicExternalNotes = publicExternalNotes;
    }

  
    /**
     * @return the requestPublicCC
     */
    public int getRequestPublicCC() {
        return requestPublicCC;
    }

    /**
     * @param requestPublicCC the requestPublicCC to set
     */
    public void setRequestPublicCC(int requestPublicCC) {
        this.requestPublicCC = requestPublicCC;
    }

    /**
     * @return the submittedTimeStamp
     */
    public LocalDateTime getSubmittedTimeStamp() {
        return submittedTimeStamp;
    }
    
    

    /**
     * @param submittedTimeStamp the submittedTimeStamp to set
     */
    public void setSubmittedTimeStamp(LocalDateTime submittedTimeStamp) {
        this.submittedTimeStamp = submittedTimeStamp;
    }

    /**
     * @return the actionRequestorPerson
     */
    public Person getActionRequestorPerson() {
        return actionRequestorPerson;
    }

    /**
     * @param actionRequestorPerson the actionRequestorPerson to set
     */
    public void setActionRequestorPerson(Person actionRequestorPerson) {
        this.actionRequestorPerson = actionRequestorPerson;
    }

    /**
     * @return the requestProperty
     */
    public Property getRequestProperty() {
        return requestProperty;
    }

    /**
     * @param requestProperty the requestProperty to set
     */
    public void setRequestProperty(Property requestProperty) {
        this.requestProperty = requestProperty;
    }




    /**
     * @return the anonymitiyRequested
     */
    public boolean isAnonymitiyRequested() {
        return anonymitiyRequested;
    }

    /**
     * @param anonymitiyRequested the anonymitiyRequested to set
     */
    public void setAnonymitiyRequested(boolean anonymitiyRequested) {
        this.anonymitiyRequested = anonymitiyRequested;
    }

    /**
     * @return the muniCode
     */
    public int getMuniCode() {
        return muniCode;
    }

    /**
     * @param muniCode the muniCode to set
     */
    public void setMuniCode(int muniCode) {
        this.muniCode = muniCode;
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
     * @return the caseID
     */
    public int getCaseID() {
        return caseID;
    }

    /**
     * @param caseID the caseID to set
     */
    public void setCaseID(int caseID) {
        this.caseID = caseID;
    }

    /**
     * @return the formattedSubmittedTimeStamp
     */
    public String getFormattedSubmittedTimeStamp() {
        
        return formattedSubmittedTimeStamp;
    }

    /**
     * @param formattedSubmittedTimeStamp the formattedSubmittedTimeStamp to set
     */
    public void setFormattedSubmittedTimeStamp(String formattedSubmittedTimeStamp) {
        this.formattedSubmittedTimeStamp = formattedSubmittedTimeStamp;
    }

    /**
     * @return the daysSinceDateOfRecord
     */
    public long getDaysSinceDateOfRecord() {
        return daysSinceDateOfRecord;
    }

    /**
     * @param daysSinceDateOfRecord the daysSinceDateOfRecord to set
     */
    public void setDaysSinceDateOfRecord(long daysSinceDateOfRecord) {
        this.daysSinceDateOfRecord = daysSinceDateOfRecord;
    }

    /**
     * @return the requestStatus
     */
    public CEActionRequestStatus getRequestStatus() {
        return requestStatus;
    }

    /**
     * @param requestStatus the requestStatus to set
     */
    public void setRequestStatus(CEActionRequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    /**
     * @return the dateOfRecordUtilDate
     */
    public java.util.Date getDateOfRecordUtilDate() {
        return dateOfRecordUtilDate;
    }

    /**
     * @param dateOfRecordUtilDate the dateOfRecordUtilDate to set
     */
    public void setDateOfRecordUtilDate(java.util.Date dateOfRecordUtilDate) {
        this.dateOfRecordUtilDate = dateOfRecordUtilDate;
        dateOfRecord = dateOfRecordUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + this.requestID;
        hash = 73 * hash + this.requestPublicCC;
        hash = 73 * hash + Objects.hashCode(this.requestStatus);
        hash = 73 * hash + Objects.hashCode(this.muni);
        hash = 73 * hash + this.personID;
        hash = 73 * hash + Objects.hashCode(this.actionRequestorPerson);
        hash = 73 * hash + Objects.hashCode(this.requestProperty);
        hash = 73 * hash + this.issueType_issueTypeID;
        hash = 73 * hash + Objects.hashCode(this.issueTypeString);
        hash = 73 * hash + this.muniCode;
        hash = 73 * hash + this.caseID;
        hash = 73 * hash + Objects.hashCode(this.submittedTimeStamp);
        hash = 73 * hash + Objects.hashCode(this.formattedSubmittedTimeStamp);
        hash = 73 * hash + Objects.hashCode(this.dateOfRecord);
        hash = 73 * hash + (int) (this.daysSinceDateOfRecord ^ (this.daysSinceDateOfRecord >>> 32));
        hash = 73 * hash + Objects.hashCode(this.dateOfRecordUtilDate);
        hash = 73 * hash + (this.isAtKnownAddress ? 1 : 0);
        hash = 73 * hash + Objects.hashCode(this.addressOfConcern);
        hash = 73 * hash + Objects.hashCode(this.requestDescription);
        hash = 73 * hash + (this.isUrgent ? 1 : 0);
        hash = 73 * hash + (this.anonymitiyRequested ? 1 : 0);
        hash = 73 * hash + Objects.hashCode(this.cogInternalNotes);
        hash = 73 * hash + Objects.hashCode(this.muniNotes);
        hash = 73 * hash + Objects.hashCode(this.publicExternalNotes);
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
        final CEActionRequest other = (CEActionRequest) obj;
        if (this.requestID != other.requestID) {
            return false;
        }
        if (this.requestPublicCC != other.requestPublicCC) {
            return false;
        }
        if (this.personID != other.personID) {
            return false;
        }
        if (this.issueType_issueTypeID != other.issueType_issueTypeID) {
            return false;
        }
        if (this.muniCode != other.muniCode) {
            return false;
        }
        if (this.caseID != other.caseID) {
            return false;
        }
        if (this.daysSinceDateOfRecord != other.daysSinceDateOfRecord) {
            return false;
        }
        if (this.isAtKnownAddress != other.isAtKnownAddress) {
            return false;
        }
        if (this.isUrgent != other.isUrgent) {
            return false;
        }
        if (this.anonymitiyRequested != other.anonymitiyRequested) {
            return false;
        }
        if (!Objects.equals(this.issueTypeString, other.issueTypeString)) {
            return false;
        }
        if (!Objects.equals(this.formattedSubmittedTimeStamp, other.formattedSubmittedTimeStamp)) {
            return false;
        }
        if (!Objects.equals(this.addressOfConcern, other.addressOfConcern)) {
            return false;
        }
        if (!Objects.equals(this.requestDescription, other.requestDescription)) {
            return false;
        }
        if (!Objects.equals(this.cogInternalNotes, other.cogInternalNotes)) {
            return false;
        }
        if (!Objects.equals(this.muniNotes, other.muniNotes)) {
            return false;
        }
        if (!Objects.equals(this.publicExternalNotes, other.publicExternalNotes)) {
            return false;
        }
        if (!Objects.equals(this.requestStatus, other.requestStatus)) {
            return false;
        }
        if (!Objects.equals(this.muni, other.muni)) {
            return false;
        }
        if (!Objects.equals(this.actionRequestorPerson, other.actionRequestorPerson)) {
            return false;
        }
        if (!Objects.equals(this.requestProperty, other.requestProperty)) {
            return false;
        }
        if (!Objects.equals(this.submittedTimeStamp, other.submittedTimeStamp)) {
            return false;
        }
        if (!Objects.equals(this.dateOfRecord, other.dateOfRecord)) {
            return false;
        }
        if (!Objects.equals(this.dateOfRecordUtilDate, other.dateOfRecordUtilDate)) {
            return false;
        }
        return true;
    }
    
    

   
}