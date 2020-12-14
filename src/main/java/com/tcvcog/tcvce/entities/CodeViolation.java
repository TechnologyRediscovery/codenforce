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
 
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public  class       CodeViolation  
        extends BOb
        implements  Serializable,
                    Comparable<CodeViolation> {
    
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
    protected java.util.Date dateOfRecordUtilDate;
    protected String dateOfRecordPretty;
    
    protected LocalDateTime creationTS;
    protected String creationTSPretty;
    protected User createdBy;
    
    protected boolean allowHostCaseUpdate;
    protected boolean allowOrdinanceUpdates;
    protected boolean allowDORUpdate;
    protected boolean allowStipCompDateUpdate;
    
    protected LocalDateTime dateOfCitation;
    protected List<Integer> citationIDList;
    protected List<Integer> noticeIDList;
    
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
    protected List<Photograph> photoList;
    protected List<Integer> blobIDList;
    protected List<Integer> photoIDList;
    
    protected List<Blob> blobList;
    
    protected int complianceTFExpiryPropID;
    protected Proposal complianceTFExpiryProp;
    
    protected IntensityClass severityIntensity;
    
    protected LocalDateTime lastUpdatedTS;
    protected User lastUpdatedUser;
    
    
    
     /**
     * @return the daysUntilStipulatedComplianceDate
     */
    public long getDaysUntilStipulatedComplianceDate() {
        return EntityUtils.getTimePeriodAsDays(LocalDateTime.now(), stipulatedComplianceDate);
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
        String s = EntityUtils.getPrettyDate(dateOfCitation);
        return s;
    }

    /**
     * @return the dateOfRecordPretty
     */
    public String getDateOfRecordPretty() {
        dateOfRecordPretty = EntityUtils.getPrettyDate(dateOfRecord);
        return dateOfRecordPretty;
    }

    /**
     * @return the creationTSPretty
     */
    public String getCreationTSPretty() {
        creationTSPretty = EntityUtils.getPrettyDate(creationTS);
        
        return creationTSPretty;
    }

    /**
     * @return the stipulatedComplianceDatePretty
     */
    public String getStipulatedComplianceDatePretty() {
        return EntityUtils.getPrettyDate(stipulatedComplianceDate);
    }

    /**
     * @return the actualComplianceDatePretty
     */
    public String getActualComplianceDatePretty() {
        return EntityUtils.getPrettyDate(actualComplianceDate);
    }


    /**
     * @param dateOfRecordPretty the dateOfRecordPretty to set
     */
    public void setDateOfRecordPretty(String dateOfRecordPretty) {
        this.dateOfRecordPretty = dateOfRecordPretty;
    }

    /**
     * @param creationTSPretty the creationTSPretty to set
     */
    public void setCreationTSPretty(String creationTSPretty) {
        this.creationTSPretty = creationTSPretty;
    }

    

    /**
     * @return the list of blobIDs associated with this Violation
     */
    public List<Integer> getBlobIDList() {
        return this.blobIDList;
    }

    /**
     * @param blobIDList the blobIDList to set
     */
    public void setBlobIDList(List<Integer> blobIDList) {
        this.blobIDList = blobIDList;
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
        sb.append("Done use toString()!");
        return sb.toString();
        
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.violationID;
        hash = 53 * hash + Objects.hashCode(this.violatedEnfElement);
        hash = 53 * hash + this.ceCaseID;
        hash = 53 * hash + (int) (Double.doubleToLongBits(this.penalty) ^ (Double.doubleToLongBits(this.penalty) >>> 32));
        hash = 53 * hash + Objects.hashCode(this.description);
        hash = 53 * hash + Objects.hashCode(this.notes);
        hash = 53 * hash + Objects.hashCode(this.dateOfCitation);
        hash = 53 * hash + Objects.hashCode(this.citationIDList);
        hash = 53 * hash + Objects.hashCode(this.dateOfRecord);
        hash = 53 * hash + Objects.hashCode(this.dateOfRecordPretty);
        hash = 53 * hash + Objects.hashCode(this.creationTS);
        hash = 53 * hash + Objects.hashCode(this.creationTSPretty);
        hash = 53 * hash + Objects.hashCode(this.stipulatedComplianceDate);
        hash = 53 * hash + Objects.hashCode(this.actualComplianceDate);
        hash = 53 * hash + (this.leagacyImport ? 1 : 0);
        hash = 53 * hash + Objects.hashCode(this.complianceTimeStamp);
        hash = 53 * hash + Objects.hashCode(this.complianceUser);
        hash = 53 * hash + Objects.hashCode(this.blobIDList);
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
     * @return the dateOfRecordUtilDate
     */
    public java.util.Date getDateOfRecordUtilDate() {
        return convertUtilDate(dateOfRecord);
    }

    /**
     * @return the stipulatedComplianceDateUtilDate
     */
    public java.util.Date getStipulatedComplianceDateUtilDate() {
        return convertUtilDate(stipulatedComplianceDate);
    }

    /**
     * @return the actualComplianceDateUtilDate
     */
    public java.util.Date getActualComplianceDateUtilDate() {
        return convertUtilDate(actualComplianceDate);
    }

    /**
     * @param noticeIDList the noticeIDList to set
     */
    public void setNoticeIDList(List<Integer> noticeIDList) {
        this.noticeIDList = noticeIDList;
    }


    /**
     * @param dateOfRecordUtilDate the dateOfRecordUtilDate to set
     */
    public void setDateOfRecordUtilDate(java.util.Date dateOfRecordUtilDate) {
        this.dateOfRecordUtilDate = dateOfRecordUtilDate;
        dateOfRecord = convertUtilDate(dateOfRecordUtilDate);
    }

    /**
     * @param stipulatedComplianceDateUtilDate the stipulatedComplianceDateUtilDate to set
     */
    public void setStipulatedComplianceDateUtilDate(java.util.Date stipulatedComplianceDateUtilDate) {
        stipulatedComplianceDate = convertUtilDate(stipulatedComplianceDateUtilDate);
    }

    /**
     * @param actualComplianceDateUtilDate the actualComplianceDateUtilDate to set
     */
    public void setActualComplianceDateUtilDate(java.util.Date actualComplianceDateUtilDate) {
        actualComplianceDate = convertUtilDate(actualComplianceDateUtilDate);
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
     * @return the photoIDList
     */
    public List<Integer> getPhotoIDList() {
        return photoIDList;
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
     * @param photoIDList the photoIDList to set
     */
    public void setPhotoIDList(List<Integer> photoIDList) {
        this.photoIDList = photoIDList;
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
     * @return the photoList
     */
    public List<Photograph> getPhotoList() {
        return photoList;
    }

    /**
     * @param photoList the photoList to set
     */
    public void setPhotoList(List<Photograph> photoList) {
        this.photoList = photoList;
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
    public List<Blob> getBlobList() {
        return blobList;
    }

    /**
     * @param blobList the blobList to set
     */
    public void setBlobList(List<Blob> blobList) {
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

   

}
