/*
 * Copyright (C) 2017 ellen bascomb of apt 31y
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
import java.util.List;
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
public class CEActionRequest extends BOb implements Serializable{

    /**
     * @return the blobIDList
     */
    public List<Integer> getBlobIDList() {
        return blobIDList;
    }

    /**
     * @param blobIDList the blobIDList to set
     */
    public void setBlobIDList(List<Integer> blobIDList) {
        this.blobIDList = blobIDList;
    }
    
    // requests no longer have a status--remove when fully updated
    // for the full case model
    //private RequestStatus requestStatus;
    
    private int requestID;
    private int requestPublicCC;
    private boolean paccEnabled;
    private CEActionRequestStatus requestStatus;

    private Municipality muni;
    private Person requestor;

    private Property requestProperty;
    
    private CEActionRequestIssueType issue;
    
    private int muniCode;
    
    private int caseID;
    private java.time.LocalDateTime caseAttachmentTimeStamp;
    private User caseAttachmentUser;
    
    private java.time.LocalDateTime submittedTimeStamp;
    private String formattedSubmittedTimeStamp;
    
    private java.time.LocalDateTime dateOfRecord;
    private long daysSinceDateOfRecord;
    
    private boolean isAtKnownAddress;
    private String addressOfConcern;
    
    private String requestDescription;
    private boolean isUrgent;
    private boolean anonymitiyRequested;
    
    private String cogInternalNotes;
    private String muniNotes;
    private String publicExternalNotes;
    
    private boolean active;
    // end threes
    
    /**
     * A VERY hacky way to deal with print formatting in Chrome
     */
    private boolean insertPageBreakBefore = true;
    
    // these are populated on the lookup when the linked
    // tables with the String values are selected
    
    // list of blob id's associated with this request
    private List<Integer> blobIDList;
    
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
        return !isAtKnownAddress;
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
     * @return the requestor
     */
    public Person getRequestor() {
        return requestor;
    }

    /**
     * @param requestor the requestor to set
     */
    public void setRequestor(Person requestor) {
        this.requestor = requestor;
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
    public void event(int caseID) {
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
    
    public void setCaseID(int i){
        this.caseID = i;
    }

    /**
     * @return the dateOfRecordUtilDate
     */
    public java.util.Date getDateOfRecordUtilDate() {        
        return convertUtilDate(dateOfRecord);
    }

    /**
     * @param dateOfRecordUtilDate the dateOfRecordUtilDate to set
     */
    public void setDateOfRecordUtilDate(java.util.Date dateOfRecordUtilDate) {
        dateOfRecord = convertUtilDate(dateOfRecordUtilDate);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + this.requestID;
        hash = 73 * hash + this.requestPublicCC;
        hash = 73 * hash + Objects.hashCode(this.requestStatus);
        hash = 73 * hash + Objects.hashCode(this.muni);
        hash = 73 * hash + Objects.hashCode(this.requestor);
        hash = 73 * hash + Objects.hashCode(this.requestProperty);
        hash = 73 * hash + this.muniCode;
        hash = 73 * hash + this.caseID;
        hash = 73 * hash + Objects.hashCode(this.submittedTimeStamp);
        hash = 73 * hash + Objects.hashCode(this.formattedSubmittedTimeStamp);
        hash = 73 * hash + Objects.hashCode(this.dateOfRecord);
        hash = 73 * hash + (int) (this.daysSinceDateOfRecord ^ (this.daysSinceDateOfRecord >>> 32));
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
        if (!Objects.equals(this.requestor, other.requestor)) {
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
        return true;
    }

    /**
     * @return the paccEnabled
     */
    public boolean isPaccEnabled() {
        return paccEnabled;
    }

    /**
     * @param paccEnabled the paccEnabled to set
     */
    public void setPaccEnabled(boolean paccEnabled) {
        this.paccEnabled = paccEnabled;
    }

    /**
     * @return the caseAttachmentUser
     */
    public User getCaseAttachmentUser() {
        return caseAttachmentUser;
    }

    /**
     * @param caseAttachmentUser the caseAttachmentUser to set
     */
    public void setCaseAttachmentUser(User caseAttachmentUser) {
        this.caseAttachmentUser = caseAttachmentUser;
    }

    /**
     * @return the caseAttachmentTimeStamp
     */
    public java.time.LocalDateTime getCaseAttachmentTimeStamp() {
        return caseAttachmentTimeStamp;
    }

    /**
     * @param caseAttachmentTimeStamp the caseAttachmentTimeStamp to set
     */
    public void setCaseAttachmentTimeStamp(java.time.LocalDateTime caseAttachmentTimeStamp) {
        this.caseAttachmentTimeStamp = caseAttachmentTimeStamp;
    }

    /**
     * @return the insertPageBreakBefore
     */
    public boolean isInsertPageBreakBefore() {
        return insertPageBreakBefore;
    }

    /**
     * @param insertPageBreakBefore the insertPageBreakBefore to set
     */
    public void setInsertPageBreakBefore(boolean insertPageBreakBefore) {
        this.insertPageBreakBefore = insertPageBreakBefore;
    }

    /**
     * @return the issue
     */
    public CEActionRequestIssueType getIssue() {
        return issue;
    }

    /**
     * @param issue the issue to set
     */
    public void setIssue(CEActionRequestIssueType issue) {
        this.issue = issue;
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

}