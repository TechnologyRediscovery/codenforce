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
        this.ceCaseID = cv.ceCaseID;
        this.penalty = cv.penalty;
        this.daysUntilStipulatedComplianceDate = cv.daysUntilStipulatedComplianceDate;
        this.leagacyImport = cv.leagacyImport;
        this.complianceTimeframeEventID = cv.complianceTimeframeEventID;

        this.violatedEnfElement = cv.violatedEnfElement;
        this.icon = cv.icon;
        this.blobIDList = cv.blobIDList;
        this.citationIDList = cv.citationIDList;
        this.complianceUser = cv.complianceUser;
        this.compTimeFrameComplianceEvent = cv.compTimeFrameComplianceEvent;

        this.ageLeadText = cv.ageLeadText;
        this.statusString = cv.statusString;
        this.description = cv.description;
        this.notes = cv.notes;
        this.citationListAsString = cv.citationListAsString;

        this.stipulatedComplianceDate = cv.stipulatedComplianceDate;
        this.dateOfRecord = cv.dateOfRecord;
        this.creationTS = cv.creationTS;
        this.createdBy = cv.createdBy;
        
        this.actualComplianceDate = cv.actualComplianceDate;
        this.complianceTimeStamp = cv.complianceTimeStamp;

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
