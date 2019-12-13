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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Eric C. Darsow
 */
public class CodeViolation  implements Serializable{
    protected int violationID;
    protected EnforcableCodeElement violatedEnfElement;
    protected int ceCaseID;
       
    protected ViolationStatusEnum status;
    protected Icon icon;
    protected String ageLeadText;
    
    protected double penalty;
    protected String description;
    protected String notes;
    
    protected LocalDateTime dateOfCitation;
    protected String dateOfCitationPretty;
    
    protected List<Integer> citationIDList;
    protected String citationListAsString;
    
    protected List<Integer> noticeIDList;
    protected String noticeIDListAsString;
 
    protected LocalDateTime dateOfRecord;
    protected java.util.Date dateOfRecordUtilDate;
    protected String dateOfRecordPretty;
    
    protected LocalDateTime creationTS;
    protected String creationTSPretty;
    
    protected User createdBy;
    
    protected long daysUntilStipulatedComplianceDate;
    protected LocalDateTime stipulatedComplianceDate;
    protected java.util.Date stipulatedComplianceDateUtilDate;
    protected String stipulatedComplianceDatePretty;
    
    protected LocalDateTime actualComplianceDate;
    protected java.util.Date actualComplianceDateUtilDate;
    protected String actualComplianceDatePretty;
    
    protected boolean leagacyImport;
    
    protected List<Integer> blobIDList;
    
    protected LocalDateTime complianceTimeStamp;
    protected User complianceUser;
    protected CECaseEvent compTimeFrameComplianceEvent;
    protected int complianceTimeframeEventID;
    
    protected List<Integer> photoList;
    
    protected List<Fee> feeList;
    
    private int severityIntensityClassID;
    
    
    
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
       
        return citationListAsString;
    }

    /**
     * @param citationListAsString the citationListAsString to set
     */
    public void setCitationListAsString(String citationListAsString) {
        this.citationListAsString = citationListAsString;
    }

    /**
     * @return the daysUntilStipulatedComplianceDate
     */
    public long getDaysUntilStipulatedComplianceDate() {
        daysUntilStipulatedComplianceDate = 
                EntityUtils.getTimePeriodAsDays(LocalDateTime.now(), stipulatedComplianceDate);
        return daysUntilStipulatedComplianceDate;
    }

    /**
     * @param daysUntilStipulatedComplianceDate the daysUntilStipulatedComplianceDate to set
     */
    public void setDaysUntilStipulatedComplianceDate(long daysUntilStipulatedComplianceDate) {
        this.daysUntilStipulatedComplianceDate = daysUntilStipulatedComplianceDate;
    }

    /**
     * @return the dateOfCitationPretty
     */
    public String getDateOfCitationPretty() {
        dateOfCitationPretty = EntityUtils.getPrettyDate(dateOfCitation);
        return dateOfCitationPretty;
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
        stipulatedComplianceDatePretty = EntityUtils.getPrettyDate(stipulatedComplianceDate);
        return stipulatedComplianceDatePretty;
    }

    /**
     * @return the actualComplianceDatePretty
     */
    public String getActualComplianceDatePretty() {
        actualComplianceDatePretty = EntityUtils.getPrettyDate(actualComplianceDate);
        
        return actualComplianceDatePretty;
    }

    /**
     * @param dateOfCitationPretty the dateOfCitationPretty to set
     */
    public void setDateOfCitationPretty(String dateOfCitationPretty) {
        this.dateOfCitationPretty = dateOfCitationPretty;
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
     * @param stipulatedComplianceDatePretty the stipulatedComplianceDatePretty to set
     */
    public void setStipulatedComplianceDatePretty(String stipulatedComplianceDatePretty) {
        this.stipulatedComplianceDatePretty = stipulatedComplianceDatePretty;
    }

    /**
     * @param actualComplianceDatePretty the actualComplianceDatePretty to set
     */
    public void setActualComplianceDatePretty(String actualComplianceDatePretty) {
        this.actualComplianceDatePretty = actualComplianceDatePretty;
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
     * @return the compTimeFrameComplianceEvent
     */
    public CECaseEvent getCompTimeFrameComplianceEvent() {
        return compTimeFrameComplianceEvent;
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
     * @param compTimeFrameComplianceEvent the compTimeFrameComplianceEvent to set
     */
    public void setCompTimeFrameComplianceEvent(CECaseEvent compTimeFrameComplianceEvent) {
        this.compTimeFrameComplianceEvent = compTimeFrameComplianceEvent;
    }

    /**
     * @return the complianceTimeframeEventID
     */
    public int getComplianceTimeframeEventID() {
        return complianceTimeframeEventID;
    }

    /**
     * @param complianceTimeframeEventID the complianceTimeframeEventID to set
     */
    public void setComplianceTimeframeEventID(int complianceTimeframeEventID) {
        this.complianceTimeframeEventID = complianceTimeframeEventID;
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
        sb.append(violatedEnfElement.getCodeElement().getOrdSubSecNum());
        sb.append(": ");
        sb.append(violatedEnfElement.getCodeElement().getOrdSubSecTitle());
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
        hash = 53 * hash + Objects.hashCode(this.dateOfCitationPretty);
        hash = 53 * hash + Objects.hashCode(this.citationIDList);
        hash = 53 * hash + Objects.hashCode(this.citationListAsString);
        hash = 53 * hash + Objects.hashCode(this.dateOfRecord);
        hash = 53 * hash + Objects.hashCode(this.dateOfRecordPretty);
        hash = 53 * hash + Objects.hashCode(this.creationTS);
        hash = 53 * hash + Objects.hashCode(this.creationTSPretty);
        hash = 53 * hash + (int) (this.daysUntilStipulatedComplianceDate ^ (this.daysUntilStipulatedComplianceDate >>> 32));
        hash = 53 * hash + Objects.hashCode(this.stipulatedComplianceDate);
        hash = 53 * hash + Objects.hashCode(this.stipulatedComplianceDatePretty);
        hash = 53 * hash + Objects.hashCode(this.actualComplianceDate);
        hash = 53 * hash + Objects.hashCode(this.actualComplianceDatePretty);
        hash = 53 * hash + (this.leagacyImport ? 1 : 0);
        hash = 53 * hash + Objects.hashCode(this.complianceTimeStamp);
        hash = 53 * hash + Objects.hashCode(this.complianceUser);
        hash = 53 * hash + Objects.hashCode(this.compTimeFrameComplianceEvent);
        hash = 53 * hash + this.complianceTimeframeEventID;
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
        if (!Objects.equals(this.actualComplianceDatePretty, other.actualComplianceDatePretty)) {
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
        return noticeIDListAsString;
    }

    /**
     * @return the dateOfRecordUtilDate
     */
    public java.util.Date getDateOfRecordUtilDate() {
        if(dateOfRecord != null){
            dateOfRecordUtilDate 
                    = java.util.Date.from(dateOfRecord.atZone(ZoneId.systemDefault()).toInstant());
        }
        return dateOfRecordUtilDate;
    }

    /**
     * @return the stipulatedComplianceDateUtilDate
     */
    public java.util.Date getStipulatedComplianceDateUtilDate() {
        if(stipulatedComplianceDate != null){
            stipulatedComplianceDateUtilDate 
                    = java.util.Date.from(stipulatedComplianceDate.atZone(ZoneId.systemDefault()).toInstant());
        }
        return stipulatedComplianceDateUtilDate;
    }

    /**
     * @return the actualComplianceDateUtilDate
     */
    public java.util.Date getActualComplianceDateUtilDate() {
        if(actualComplianceDate != null){
            actualComplianceDateUtilDate 
                    = java.util.Date.from(actualComplianceDate.atZone(ZoneId.systemDefault()).toInstant());
        }
        return actualComplianceDateUtilDate;
    }

    /**
     * @param noticeIDList the noticeIDList to set
     */
    public void setNoticeIDList(List<Integer> noticeIDList) {
        this.noticeIDList = noticeIDList;
    }

    /**
     * @param noticeIDListAsString the noticeIDListAsString to set
     */
    public void setNoticeIDListAsString(String noticeIDListAsString) {
        this.noticeIDListAsString = noticeIDListAsString;
    }

    /**
     * @param dateOfRecordUtilDate the dateOfRecordUtilDate to set
     */
    public void setDateOfRecordUtilDate(java.util.Date dateOfRecordUtilDate) {
        this.dateOfRecordUtilDate = dateOfRecordUtilDate;
        if(dateOfRecordUtilDate != null){
            dateOfRecord = dateOfRecordUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

    /**
     * @param stipulatedComplianceDateUtilDate the stipulatedComplianceDateUtilDate to set
     */
    public void setStipulatedComplianceDateUtilDate(java.util.Date stipulatedComplianceDateUtilDate) {
            
        this.stipulatedComplianceDateUtilDate = stipulatedComplianceDateUtilDate;
        if(stipulatedComplianceDateUtilDate != null){
            stipulatedComplianceDate = stipulatedComplianceDateUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            
        }
    }

    /**
     * @param actualComplianceDateUtilDate the actualComplianceDateUtilDate to set
     */
    public void setActualComplianceDateUtilDate(java.util.Date actualComplianceDateUtilDate) {
        
        this.actualComplianceDateUtilDate = actualComplianceDateUtilDate;
        
        if(actualComplianceDateUtilDate != null){
            actualComplianceDate = actualComplianceDateUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        
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
     * @return the severityIntensityClassID
     */
    public int getSeverityIntensityClassID() {
        return severityIntensityClassID;
    }

    /**
     * @param severityIntensityClassID the severityIntensityClassID to set
     */
    public void setSeverityIntensityClassID(int severityIntensityClassID) {
        this.severityIntensityClassID = severityIntensityClassID;
    }

}
