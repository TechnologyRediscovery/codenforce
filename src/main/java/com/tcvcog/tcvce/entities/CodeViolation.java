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
 
import com.tcvcog.tcvce.util.DateTimeUtil;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * The premier container representing a violation of an ordinance
 * @author ellen bascomb of apt 31y
 */
public  class       CodeViolation  
        extends     BOb
        implements  Serializable,
                    IFace_BlobHolder,
                    Comparable<CodeViolation> {
    
    static final BlobLinkEnum VIOLATION_BLOB_LINK_ENUM = BlobLinkEnum.CODE_VIOLATION;
    static final BlobLinkEnum VIOLATION_BLOB_LINK_ENUM_UPSTREAM_POOL = BlobLinkEnum.CE_CASE;
    
    protected int violationID;
    protected EnforcableCodeElement violatedEnfElement;
    protected int ceCaseID;
       
    protected ViolationStatusEnum status;
    protected boolean active;
    protected Icon icon;
    protected String ageLeadText;
    
    protected double penalty;
    protected String description;
    protected String notes;
 
    protected LocalDateTime dateOfRecord;
    protected LocalDateTime creationTS;
    
    protected User createdBy;
    
    protected boolean allowHostCaseUpdate;
    protected boolean allowOrdinanceUpdates;
    protected boolean allowDORUpdate;
    protected boolean allowStipCompDateUpdate;
    
    protected LocalDateTime dateOfCitation;
    protected List<Integer> citationIDList;
    protected List<Integer> noticeIDList;
    
    protected boolean makeFindingsDefault;
    
    // compliance fields
    protected LocalDateTime stipulatedComplianceDate;
    protected LocalDateTime actualComplianceDate;
    protected LocalDateTime complianceTimeStamp;
    protected User complianceUser;
    protected String complianceNote;
    
    // nullification fields
    
    protected LocalDateTime nullifiedTS;
    protected User nullifiedUser;
    
    protected boolean leagacyImport;
    
    protected List<BlobLight> blobList;
    
    protected int complianceTFExpiryPropID;
    protected Proposal complianceTFExpiryProp;
    
    protected IntensityClass severityIntensity;
    
    protected LocalDateTime lastUpdatedTS;
    protected User lastUpdatedUser;
    
    public CodeViolation(){
        
    }
    
    public CodeViolation(CodeViolation cv){
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
        this.blobList = cv.blobList;
        this.complianceTFExpiryPropID = cv.complianceTFExpiryPropID;
        this.complianceTFExpiryProp = cv.complianceTFExpiryProp;
        this.severityIntensity = cv.severityIntensity;
        this.lastUpdatedTS = cv.lastUpdatedTS;
        this.lastUpdatedUser = cv.lastUpdatedUser;
    }
    
     /**
     * @return the daysUntilStipulatedComplianceDate
     */
    public long getDaysUntilStipulatedComplianceDate() {
        return DateTimeUtil.getTimePeriodAsDays(LocalDateTime.now(), stipulatedComplianceDate);
    }
    
     @Override
    public int compareTo(CodeViolation o) {
        if(o == null){
            throw new NullPointerException("Cannot compare to Null");
        }
        if(this.getDaysUntilStipulatedComplianceDate() < o.getDaysUntilStipulatedComplianceDate()){
            return -1;
        }
        if(this.getDaysUntilStipulatedComplianceDate() == o.getDaysUntilStipulatedComplianceDate()){
            return 0;
        }
        return 1;
    }
    
    /**
     * @return the violationID
     */
    public int getViolationID() {
        return violationID;
    }

    /**
     * @param violationID the violationID to set
     */
    public void setViolationID(int violationID) {
        this.violationID = violationID;
    }

    /**
     * @return the codeViolated
     */
    public EnforcableCodeElement getCodeViolated() {
        return getViolatedEnfElement();
    }

    /**
     * @param codeViolated the codeViolated to set
     */
    public void setCodeViolated(EnforcableCodeElement codeViolated) {
        this.setViolatedEnfElement(codeViolated);
    }

    /**
     * @return the stipulatedComplianceDate
     */
    public LocalDateTime getStipulatedComplianceDate() {
        return stipulatedComplianceDate;
    }

    /**
     * @param stipulatedComplianceDate the stipulatedComplianceDate to set
     */
    public void setStipulatedComplianceDate(LocalDateTime stipulatedComplianceDate) {
        this.stipulatedComplianceDate = stipulatedComplianceDate;
    }

    /**
     * @return the actualComplianceDate
     */
    public LocalDateTime getActualComplianceDate() {
        return actualComplianceDate;
    }

    /**
     * @param actualComplianceDate the actualComplianceDate to set
     */
    public void setActualComplianceDate(LocalDateTime actualComplianceDate) {
        this.actualComplianceDate = actualComplianceDate;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the violatedEnfElement
     */
    public EnforcableCodeElement getViolatedEnfElement() {
        return violatedEnfElement;
    }

    /**
     * @return the ceCaseID
     */
    public int getCeCaseID() {
        return ceCaseID;
    }

    /**
     * @return the penalty
     */
    public double getPenalty() {
        return penalty;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param violatedEnfElement the violatedEnfElement to set
     */
    public void setViolatedEnfElement(EnforcableCodeElement violatedEnfElement) {
        this.violatedEnfElement = violatedEnfElement;
    }

    /**
     * @param ceCaseID the ceCaseID to set
     */
    public void setCeCaseID(int ceCaseID) {
        this.ceCaseID = ceCaseID;
    }


    /**
     * @param penalty the penalty to set
     */
    public void setPenalty(double penalty) {
        this.penalty = penalty;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the dateOfCitation
     */
    public LocalDateTime getDateOfCitation() {
        return dateOfCitation;
    }

    /**
     * @return the dateOfRecord
     */
    public LocalDateTime getDateOfRecord() {
        return dateOfRecord;
    }

    /**
     * @return the creationTS
     */
    public LocalDateTime getCreationTS() {
        return creationTS;
    }

    /**
     * @param dateOfCitation the dateOfCitation to set
     */
    public void setDateOfCitation(LocalDateTime dateOfCitation) {
        this.dateOfCitation = dateOfCitation;
    }

    /**
     * @param dateOfRecord the dateOfRecord to set
     */
    public void setDateOfRecord(LocalDateTime dateOfRecord) {
        this.dateOfRecord = dateOfRecord;
    }

    /**
     * @param creationTS the creationTS to set
     */
    public void setCreationTS(LocalDateTime creationTS) {
        this.creationTS = creationTS;
    }

    /**
     * @return the citationIDList
     */
    public List<Integer> getCitationIDList() {
        return citationIDList;
    }

    /**
     * @param citationIDList the citationIDList to set
     */
    public void setCitationIDList(List<Integer> citationIDList) {
        this.citationIDList = citationIDList;
    }

    /**
     * @return the citationListAsString
     */
    public String getCitationListAsString() {
        String s = "Inject during configuration";
        return s;
    }

   

   

    /**
     * @return the dateOfCitationPretty
     */
    public String getDateOfCitationPretty() {
        String s = DateTimeUtil.getPrettyDate(dateOfCitation);
        return s;
    }

    
    /**
     * @return the stipulatedComplianceDatePretty
     */
    public String getStipulatedComplianceDatePretty() {
        return DateTimeUtil.getPrettyDate(stipulatedComplianceDate);
    }

    /**
     * @return the actualComplianceDatePretty
     */
    public String getActualComplianceDatePretty() {
        return DateTimeUtil.getPrettyDate(actualComplianceDate);
    }


 
    /**
     * @return the leagacyImport
     */
    public boolean isLeagacyImport() {
        return leagacyImport;
    }

    /**
     * @return the complianceTimeStamp
     */
    public LocalDateTime getComplianceTimeStamp() {
        return complianceTimeStamp;
    }

    /**
     * @return the complianceUser
     */
    public User getComplianceUser() {
        return complianceUser;
    }

   

    /**
     * @param leagacyImport the leagacyImport to set
     */
    public void setLeagacyImport(boolean leagacyImport) {
        this.leagacyImport = leagacyImport;
    }

    /**
     * @param complianceTimeStamp the complianceTimeStamp to set
     */
    public void setComplianceTimeStamp(LocalDateTime complianceTimeStamp) {
        this.complianceTimeStamp = complianceTimeStamp;
    }

    /**
     * @param complianceUser the complianceUser to set
     */
    public void setComplianceUser(User complianceUser) {
        this.complianceUser = complianceUser;
    }

    
    
    /**
     * Violations can print themselves on a single line
     * @return 
     */
    @Override
    public String toString(){
        return getStrElement();
    }
    
    protected String getStrElement(){
        StringBuilder sb = new StringBuilder();
        sb.append("Dont use toString()!");
        return sb.toString();
        
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + this.violationID;
        hash = 73 * hash + Objects.hashCode(this.violatedEnfElement);
        hash = 73 * hash + this.ceCaseID;
        hash = 73 * hash + Objects.hashCode(this.status);
        hash = 73 * hash + (this.active ? 1 : 0);
        hash = 73 * hash + Objects.hashCode(this.icon);
        hash = 73 * hash + Objects.hashCode(this.ageLeadText);
        hash = 73 * hash + (int) (Double.doubleToLongBits(this.penalty) ^ (Double.doubleToLongBits(this.penalty) >>> 32));
        hash = 73 * hash + Objects.hashCode(this.description);
        hash = 73 * hash + Objects.hashCode(this.notes);
        hash = 73 * hash + Objects.hashCode(this.dateOfRecord);
        hash = 73 * hash + Objects.hashCode(this.creationTS);
        hash = 73 * hash + Objects.hashCode(this.createdBy);
        hash = 73 * hash + (this.allowHostCaseUpdate ? 1 : 0);
        hash = 73 * hash + (this.allowOrdinanceUpdates ? 1 : 0);
        hash = 73 * hash + (this.allowDORUpdate ? 1 : 0);
        hash = 73 * hash + (this.allowStipCompDateUpdate ? 1 : 0);
        hash = 73 * hash + Objects.hashCode(this.dateOfCitation);
        hash = 73 * hash + Objects.hashCode(this.citationIDList);
        hash = 73 * hash + Objects.hashCode(this.noticeIDList);
        hash = 73 * hash + Objects.hashCode(this.stipulatedComplianceDate);
        hash = 73 * hash + Objects.hashCode(this.actualComplianceDate);
        hash = 73 * hash + Objects.hashCode(this.complianceTimeStamp);
        hash = 73 * hash + Objects.hashCode(this.complianceUser);
        hash = 73 * hash + Objects.hashCode(this.complianceNote);
        hash = 73 * hash + Objects.hashCode(this.nullifiedTS);
        hash = 73 * hash + Objects.hashCode(this.nullifiedUser);
        hash = 73 * hash + (this.leagacyImport ? 1 : 0);
        hash = 73 * hash + Objects.hashCode(this.blobList);
        hash = 73 * hash + this.complianceTFExpiryPropID;
        hash = 73 * hash + Objects.hashCode(this.complianceTFExpiryProp);
        hash = 73 * hash + Objects.hashCode(this.severityIntensity);
        hash = 73 * hash + Objects.hashCode(this.lastUpdatedTS);
        hash = 73 * hash + Objects.hashCode(this.lastUpdatedUser);
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
        final CodeViolation other = (CodeViolation) obj;
        if (this.violationID != other.violationID) {
            return false;
        }
        if(other.getCodeViolated().getCodeSetElementID() != this.getCodeViolated().getCodeSetElementID()){
            return false;
        }

        return true;
    }

    /**
     * @return the icon
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    /**
     * @return the ageLeadText
     */
    public String getAgeLeadText() {
        return ageLeadText;
    }

    /**
     * @param ageLeadText the ageLeadText to set
     */
    public void setAgeLeadText(String ageLeadText) {
        this.ageLeadText = ageLeadText;
    }

    /**
     * @return the createdBy
     */
    public User getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return the noticeIDList
     */
    public List<Integer> getNoticeIDList() {
        return noticeIDList;
    }

    /**
     * @return the noticeIDListAsString
     */
    public String getNoticeIDListAsString() {
        String s = "TODO: Wire up to coordinator";
        return s;
    }

   
    /**
     * @param noticeIDList the noticeIDList to set
     */
    public void setNoticeIDList(List<Integer> noticeIDList) {
        this.noticeIDList = noticeIDList;
    }


  

   
    /**
     * @return the status
     */
    public ViolationStatusEnum getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(ViolationStatusEnum status) {
        this.status = status;
    }

    /**
     * @return the severityIntensity
     */
    public IntensityClass getSeverityIntensity() {
        return severityIntensity;
    }

    /**
     * @param severityIntensityClassID the severityIntensity to set
     */
    public void setSeverityIntensity(IntensityClass severityIntensityClassID) {
        this.severityIntensity = severityIntensityClassID;
    }

    /**
     * @return the complianceTFExpiryPropID
     */
    public int getComplianceTFExpiryPropID() {
        return complianceTFExpiryPropID;
    }

    /**
     * @return the complianceTFExpiryProp
     */
    public Proposal getComplianceTFExpiryProp() {
        return complianceTFExpiryProp;
    }

    /**
     * @param complianceTFExpiryPropID the complianceTFExpiryPropID to set
     */
    public void setComplianceTFExpiryPropID(int complianceTFExpiryPropID) {
        this.complianceTFExpiryPropID = complianceTFExpiryPropID;
    }

    /**
     * @param complianceTFExpiryProp the complianceTFExpiryProp to set
     */
    public void setComplianceTFExpiryProp(Proposal complianceTFExpiryProp) {
        this.complianceTFExpiryProp = complianceTFExpiryProp;
    }

    /**
     * @return the lastUpdatedTS
     */
    public LocalDateTime getLastUpdatedTS() {
        return lastUpdatedTS;
    }

    /**
     * @return the lastUpdatedUser
     */
    public User getLastUpdatedUser() {
        return lastUpdatedUser;
    }

    /**
     * @param lastUpdatedTS the lastUpdatedTS to set
     */
    public void setLastUpdatedTS(LocalDateTime lastUpdatedTS) {
        this.lastUpdatedTS = lastUpdatedTS;
    }

    /**
     * @param lastUpdatedUser the lastUpdatedUser to set
     */
    public void setLastUpdatedUser(User lastUpdatedUser) {
        this.lastUpdatedUser = lastUpdatedUser;
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
     * @return the allowHostCaseUpdate
     */
    public boolean isAllowHostCaseUpdate() {
        return allowHostCaseUpdate;
    }

    /**
     * @return the allowOrdinanceUpdates
     */
    public boolean isAllowOrdinanceUpdates() {
        return allowOrdinanceUpdates;
    }

    /**
     * @return the allowDORUpdate
     */
    public boolean isAllowDORUpdate() {
        return allowDORUpdate;
    }

    /**
     * @return the allowStipCompDateUpdate
     */
    public boolean isAllowStipCompDateUpdate() {
        return allowStipCompDateUpdate;
    }

    /**
     * @param allowHostCaseUpdate the allowHostCaseUpdate to set
     */
    public void setAllowHostCaseUpdate(boolean allowHostCaseUpdate) {
        this.allowHostCaseUpdate = allowHostCaseUpdate;
    }

    /**
     * @param allowOrdinanceUpdates the allowOrdinanceUpdates to set
     */
    public void setAllowOrdinanceUpdates(boolean allowOrdinanceUpdates) {
        this.allowOrdinanceUpdates = allowOrdinanceUpdates;
    }

    /**
     * @param allowDORUpdate the allowDORUpdate to set
     */
    public void setAllowDORUpdate(boolean allowDORUpdate) {
        this.allowDORUpdate = allowDORUpdate;
    }

    /**
     * @param allowStipCompDateUpdate the allowStipCompDateUpdate to set
     */
    public void setAllowStipCompDateUpdate(boolean allowStipCompDateUpdate) {
        this.allowStipCompDateUpdate = allowStipCompDateUpdate;
    }

    /**
     * @return the complianceNote
     */
    public String getComplianceNote() {
        return complianceNote;
    }

    /**
     * @param complianceNote the complianceNote to set
     */
    public void setComplianceNote(String complianceNote) {
        this.complianceNote = complianceNote;
    }

    /**
     * @return the blobList
     */
    public List<BlobLight> getBlobList() {
        return blobList;
    }

    /**
     * @param blobList the blobList to set
     */
    public void setBlobList(List<BlobLight> blobList) {
        this.blobList = blobList;
    }

    /**
     * @return the nullifiedTS
     */
    public LocalDateTime getNullifiedTS() {
        return nullifiedTS;
    }

    /**
     * @return the nullifiedUser
     */
    public User getNullifiedUser() {
        return nullifiedUser;
    }

    /**
     * @param nullifiedTS the nullifiedTS to set
     */
    public void setNullifiedTS(LocalDateTime nullifiedTS) {
        this.nullifiedTS = nullifiedTS;
    }

    /**
     * @param nullifiedUser the nullifiedUser to set
     */
    public void setNullifiedUser(User nullifiedUser) {
        this.nullifiedUser = nullifiedUser;
    }

    /**
     * @return the makeFindingsDefault
     */
    public boolean isMakeFindingsDefault() {
        return makeFindingsDefault;
    }

    /**
     * @param makeFindingsDefault the makeFindingsDefault to set
     */
    public void setMakeFindingsDefault(boolean makeFindingsDefault) {
        this.makeFindingsDefault = makeFindingsDefault;
    }

    @Override
    public BlobLinkEnum getBlobLinkEnum() {
        return VIOLATION_BLOB_LINK_ENUM;
    }

    @Override
    public int getParentObjectID() {
        return violationID;
    }

    @Override
    public BlobLinkEnum getBlobUpstreamPoolEnum() {
        return VIOLATION_BLOB_LINK_ENUM_UPSTREAM_POOL;
    }

    @Override
    public int getBlobUpstreamPoolEnumPoolFeederID() {
        return ceCaseID;
    }

   

}
