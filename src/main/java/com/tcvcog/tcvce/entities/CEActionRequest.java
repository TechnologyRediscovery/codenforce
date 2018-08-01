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

import java.time.LocalDateTime;

/**
 * Models the entity: code enforcement action request.This object blends data from several database tables to create
 a conceptual model of a single request.
 
 The jsf page for submitting a request act on an CEActionRequest object
 and will edit the values of member varibles
 on this object and can then ask for the request to be read into the DB.
 
 A reference to an CEActionRequest object will be attached to the 
 Visit object such that both public and logged in users will have access
 to an CEActionRequest to do with as they please (print, etc.)
 * @author Eric Darsow
 */
public class CEActionRequest {
    
    // requests no longer have a status--remove when fully updated
    // for the full case model
    //private RequestStatus requestStatus;
    
    private int requestID;
    private int requestPublicCC;

    private Municipality muni;
    private int personID;
    private Person actionRequestorPerson;

    private Property requestProperty;
   
    private int issueType_issueTypeID;
    private String issueTypeString; //populated from linked table
    
    private int muniCode;
    private CECase linkedCase; // probably only use caseID
    private int caseID;
    
    private java.time.LocalDateTime submittedTimeStamp;
    private String formattedSubmittedTimeStamp;
    
    private java.time.LocalDateTime dateOfRecord;
    private long daysSinceDateOfRecord;

    private boolean isAtKnownAddress;
    private String addressOfConcern;
    private String nonAddressDescription;
    
    private String requestDescription;
    private boolean isUrgent;
    private boolean anonymitiyRequested;
    
    private String cogInternalNotes;
    private String muniInternalNotes;
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
     * @return the muniInternalNotes
     */
    public String getMuniInternalNotes() {
        return muniInternalNotes;
    }

    /**
     * @param muniInternalNotes the muniInternalNotes to set
     */
    public void setMuniInternalNotes(String muniInternalNotes) {
        this.muniInternalNotes = muniInternalNotes;
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
     * @return the linkedCase
     */
    public CECase getLinkedCase() {
        return linkedCase;
    }

    /**
     * @param linkedCase the linkedCase to set
     */
    public void setLinkedCase(CECase linkedCase) {
        this.linkedCase = linkedCase;
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
     * @return the nonAddressDescription
     */
    public String getNonAddressDescription() {
        return nonAddressDescription;
    }

    /**
     * @param nonAddressDescription the nonAddressDescription to set
     */
    public void setNonAddressDescription(String nonAddressDescription) {
        this.nonAddressDescription = nonAddressDescription;
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

   
}