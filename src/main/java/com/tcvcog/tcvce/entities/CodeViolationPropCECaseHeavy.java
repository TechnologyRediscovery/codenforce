/*
 * Copyright (C) 2017 Turtle Creek Valley
Council of Governments, PA
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
 * Data Heavy subclass of CodeViolation for allowing violations to roam
 * free, untethered from the metal constrains of their host CECase
 * 
 * @author ellen bascomb of apt 31y
 */
public  class       CodeViolationPropCECaseHeavy  
        extends     CodeViolation {
    
   private String muniName;
   private int muniCode;
    
   private String propertyAddress;
   private int propertyID;
   
   private String ceCaseName;
       
   
   public CodeViolationPropCECaseHeavy(CodeViolation cv){
       
    this.violationID = cv.violationID;
    this.violatedEnfElement = cv.violatedEnfElement;
    this.ceCaseID = cv.ceCaseID;
    this.status = cv.status;
    this.active = cv.active;
    this.icon = cv.icon;
    this.ageLeadText = cv.ageLeadText;
    this.penalty = cv.penalty;
    this.description = cv.description;
    this.notes = cv.notes;
    this.dateOfRecord = cv.dateOfRecord;
    this.creationTS = cv.creationTS;
    this.createdBy = cv.createdBy;
    this.allowHostCaseUpdate = cv.allowHostCaseUpdate;
    this.allowOrdinanceUpdates = cv.allowOrdinanceUpdates;
    this.allowDORUpdate = cv.allowDORUpdate;
    this.allowStipCompDateUpdate = cv.allowStipCompDateUpdate;
    this.dateOfCitation = cv.dateOfCitation;
    this.citationIDList = cv.citationIDList;
    this.noticeIDList = cv.noticeIDList;
    this.stipulatedComplianceDate = cv.stipulatedComplianceDate;
    this.actualComplianceDate = cv.actualComplianceDate;
    this.complianceTimeStamp = cv.complianceTimeStamp;
    this.complianceUser = cv.complianceUser;
    this.complianceNote = cv.complianceNote;
    this.nullifiedTS = cv.nullifiedTS;
    this.nullifiedUser = cv.nullifiedUser;
    this.leagacyImport = cv.leagacyImport;
    this.makeFindingsDefault = cv.makeFindingsDefault;
    // TODO: address from NADGIT
//    this.photoList = cv.photoList;
//    this.blobIDList = cv.blobIDList;
//    this.photoIDList = cv.photoIDList;
    this.blobList = cv.blobList;
    this.complianceTFExpiryPropID = cv.complianceTFExpiryPropID;
    this.complianceTFExpiryProp = cv.complianceTFExpiryProp;
    this.severityIntensity = cv.severityIntensity;
    this.lastUpdatedTS = cv.lastUpdatedTS;
    this.lastUpdatedUser = cv.lastUpdatedUser;
       
   }

    /**
     * @return the muniName
     */
    public String getMuniName() {
        return muniName;
    }

    /**
     * @return the muniCode
     */
    public int getMuniCode() {
        return muniCode;
    }

    /**
     * @return the propertyAddress
     */
    public String getPropertyAddress() {
        return propertyAddress;
    }

    /**
     * @return the propertyID
     */
    public int getPropertyID() {
        return propertyID;
    }

    /**
     * @return the ceCaseName
     */
    public String getCeCaseName() {
        return ceCaseName;
    }

    /**
     * @param muniName the muniName to set
     */
    public void setMuniName(String muniName) {
        this.muniName = muniName;
    }

    /**
     * @param muniCode the muniCode to set
     */
    public void setMuniCode(int muniCode) {
        this.muniCode = muniCode;
    }

    /**
     * @param propertyAddress the propertyAddress to set
     */
    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }

    /**
     * @param propertyID the propertyID to set
     */
    public void setPropertyID(int propertyID) {
        this.propertyID = propertyID;
    }

    /**
     * @param ceCaseName the ceCaseName to set
     */
    public void setCeCaseName(String ceCaseName) {
        this.ceCaseName = ceCaseName;
    }

    

   

}
