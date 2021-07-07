/*
 * Copyright (C) 2021 Technology Rediscovery LLC
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
 * Represents a connection between a Citation and a CodeViolation
 * Note that the LinkedObjectRole member is sort of unnecesary here since
 * we have a separate status object. This is unique to the Citation-CV link
 * because its role changes over time as a case moves through the
 * court system.
 * 
 * @author Ellen Bascomb
 */
public class CitationCodeViolationLink 
        extends CodeViolation
        implements IFace_trackedEntityLink{
    
  
    
    protected int citationViolationID;
//    protected LinkedObjectRole linkRole;
    
    protected CitationViolationStatusEnum citVStatus;
    
    protected BOBSource linkSource;
    
    protected LocalDateTime linkCreatedTS;
    protected User linkCreatedBy;
    
    protected LocalDateTime linkLastUpdatedTS;
    protected User linkLastUpdatedBy;
    
    protected LocalDateTime linkDeactivatedTS;
    protected User linkDeactivatedBy;
    
    protected String linkNotes;

    
    public CitationCodeViolationLink(CodeViolation cv){
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
        this.dateOfRecordUtilDate = cv.dateOfRecordUtilDate;
        this.dateOfRecordPretty = cv.dateOfRecordPretty;
        this.creationTS = cv.creationTS;
        this.creationTSPretty = cv.creationTSPretty;
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
        this.blobList = cv.blobList;
        this.complianceTFExpiryPropID = cv.complianceTFExpiryPropID;
        this.complianceTFExpiryProp = cv.complianceTFExpiryProp;
        this.severityIntensity = cv.severityIntensity;
        this.lastUpdatedTS = cv.lastUpdatedTS;
        this.lastUpdatedUser = cv.lastUpdatedUser;
        
        
    }
    
    /**
     * @return the citVStatus
     */
    public CitationViolationStatusEnum getCitVStatus() {
        return citVStatus;
    }

    /**
     * @return the linkSource
     */
    @Override
    public BOBSource getLinkSource() {
        return linkSource;
    }

    /**
     * @return the linkCreatedTS
     */
    @Override
    public LocalDateTime getLinkCreatedTS() {
        return linkCreatedTS;
    }

    /**
     * @return the linkCreatedBy
     */
     @Override
    public User getLinkCreatedBy() {
        return linkCreatedBy;
    }

    /**
     * @return the linkLastUpdatedTS
     */
     @Override
    public LocalDateTime getLinkLastUpdatedTS() {
        return linkLastUpdatedTS;
    }

    /**
     * @return the linkLastUpdatedBy
     */
     @Override
    public User getLinkLastUpdatedBy() {
        return linkLastUpdatedBy;
    }

    /**
     * @return the linkDeactivatedTS
     */
     @Override
    public LocalDateTime getLinkDeactivatedTS() {
        return linkDeactivatedTS;
    }

    /**
     * @return the linkDeactivatedBy
     */
     @Override
    public User getLinkDeactivatedBy() {
        return linkDeactivatedBy;
    }

    /**
     * @return the linkNotes
     */
     @Override
    public String getLinkNotes() {
        return linkNotes;
    }

    /**
     * @param citVStatus the citVStatus to set
     */
    public void setCitVStatus(CitationViolationStatusEnum citVStatus) {
        this.citVStatus = citVStatus;
    }

    /**
     * @param linkSource the linkSource to set
     */
    public void setLinkSource(BOBSource linkSource) {
        this.linkSource = linkSource;
    }

    /**
     * @param linkCreatedTS the linkCreatedTS to set
     */
     @Override
    public void setLinkCreatedTS(LocalDateTime linkCreatedTS) {
        this.linkCreatedTS = linkCreatedTS;
    }

    /**
     * @param linkCreatedBy the linkCreatedBy to set
     */
     @Override
    public void setLinkCreatedBy(User linkCreatedBy) {
        this.linkCreatedBy = linkCreatedBy;
    }

    /**
     * @param linkLastUpdatedTS the linkLastUpdatedTS to set
     */
     @Override
    public void setLinkLastUpdatedTS(LocalDateTime linkLastUpdatedTS) {
        this.linkLastUpdatedTS = linkLastUpdatedTS;
    }

    /**
     * @param linkLastUpdatedBy the linkLastUpdatedBy to set
     */
     @Override
    public void setLinkLastUpdatedBy(User linkLastUpdatedBy) {
        this.linkLastUpdatedBy = linkLastUpdatedBy;
    }

    /**
     * @param linkDeactivatedTS the linkDeactivatedTS to set
     */
     @Override
    public void setLinkDeactivatedTS(LocalDateTime linkDeactivatedTS) {
        this.linkDeactivatedTS = linkDeactivatedTS;
    }

    /**
     * @param linkDeactivatedBy the linkDeactivatedBy to set
     */
     @Override
    public void setLinkDeactivatedBy(User linkDeactivatedBy) {
        this.linkDeactivatedBy = linkDeactivatedBy;
    }

    /**
     * @param linkNotes the linkNotes to set
     */
     @Override
    public void setLinkNotes(String linkNotes) {
        this.linkNotes = linkNotes;
    }
    
     @Override
    public boolean isLinkDeactivated() {
        return linkDeactivatedTS != null;
    }


    @Override
    public int getDBKey() {
        return citationViolationID;
    }

    /**
     * @return the citationViolationID
     */
    public int getCitationViolationID() {
        return citationViolationID;
    }

    /**
     * @param citationViolationID the citationViolationID to set
     */
    public void setCitationViolationID(int citationViolationID) {
        this.citationViolationID = citationViolationID;
    }

   

    @Override
    public LinkedObjectRole getLinkedObjectRole() {
        throw new UnsupportedOperationException("Not supported on citations; use status insetad."); //To change body of generated methods, choose Tools | Templates.
    }

    

    @Override
    public void setLinkedObjectRole(LinkedObjectRole lor) {
        throw new UnsupportedOperationException("Not supported on citations; use status insetad."); //To change body of generated methods, choose Tools | Templates.
//        linkRole = lor;
    }

    @Override
    public String getPKFieldName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDBTableName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    
}
