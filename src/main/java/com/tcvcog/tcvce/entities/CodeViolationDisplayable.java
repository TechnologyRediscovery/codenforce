/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author sylvia
 */
public class CodeViolationDisplayable extends CodeViolation {
    
    public CodeViolationDisplayable(){
        
    }
    
    public CodeViolationDisplayable(CodeViolation cv){
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
        this.dateOfCitation = cv.dateOfCitation;
        this.dateOfCitationPretty = cv.dateOfCitationPretty;
        this.citationIDList = cv.citationIDList;
        this.citationListAsString = cv.citationListAsString;
        this.noticeIDList = cv.noticeIDList;
        this.noticeIDListAsString = cv.noticeIDListAsString;
        this.dateOfRecord = cv.dateOfRecord;
        this.dateOfRecordUtilDate = cv.dateOfRecordUtilDate;
        this.dateOfRecordPretty = cv.dateOfRecordPretty;
        this.creationTS = cv.creationTS;
        this.creationTSPretty = cv.creationTSPretty;
        this.createdBy = cv.createdBy;
        this.stipulatedComplianceDate = cv.stipulatedComplianceDate;
        this.actualComplianceDate = cv.actualComplianceDate;
        this.leagacyImport = cv.leagacyImport;
        this.blobIDList = cv.blobIDList;
        this.photoList = cv.photoList;
        this.complianceTimeStamp = cv.complianceTimeStamp;
        this.complianceUser = cv.complianceUser;
        this.complianceTFExpiryPropID = cv.complianceTFExpiryPropID;
        this.complianceTFExpiryProp = cv.complianceTFExpiryProp;
        this.severityIntensityClassID = cv.severityIntensityClassID;
        this.lastUpdatedTS = cv.lastUpdatedTS;
        this.lastUpdatedUser = cv.lastUpdatedUser;

    }

    private boolean includeOrdinanceText;
    private boolean includeHumanFriendlyText;
    private boolean includeViolationPhotos = true;

    /**
     * @return the includeOrdinanceText
     */
    public boolean isIncludeOrdinanceText() {
        return includeOrdinanceText;
    }

    /**
     * @param includeOrdinanceText the includeOrdinanceText to set
     */
    public void setIncludeOrdinanceText(boolean includeOrdinanceText) {
        this.includeOrdinanceText = includeOrdinanceText;
    }

    /**
     * @return the includeHumanFriendlyText
     */
    public boolean isIncludeHumanFriendlyText() {
        return includeHumanFriendlyText;
    }

    /**
     * @return the includeViolationPhotos
     */
    public boolean isIncludeViolationPhotos() {
        return includeViolationPhotos;
    }

    /**
     * @param includeHumanFriendlyText the includeHumanFriendlyText to set
     */
    public void setIncludeHumanFriendlyText(boolean includeHumanFriendlyText) {
        this.includeHumanFriendlyText = includeHumanFriendlyText;
    }

    /**
     * @param includeViolationPhotos the includeViolationPhotos to set
     */
    public void setIncludeViolationPhotos(boolean includeViolationPhotos) {
        this.includeViolationPhotos = includeViolationPhotos;
    }
    
}
