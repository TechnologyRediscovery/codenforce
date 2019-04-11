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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Eric C. Darsow
 */
public class CodeViolation extends EntityUtils implements Serializable{
    private int violationID;
    private EnforcableCodeElement violatedEnfElement;
    private int ceCaseID;
    private int citationID;
    
    private String statusString;

   
    
    private double penalty;
    private String description;
    private String notes;
    
    private LocalDateTime dateOfCitation;
    private String dateOfCitationPretty;
    private List<Integer> citationIDList;
    private String citationsStringList;
    
    private LocalDateTime dateOfRecord;
    private String dateOfRecordPretty;
    private LocalDateTime entryTimeStamp;
    private String entryTimeStampPretty;
    
    private long daysUntilStipulatedComplianceDate;
    private LocalDateTime stipulatedComplianceDate;
    private String stipulatedComplianceDatePretty;
    
    private LocalDateTime actualComplianceDate;
    private String actualComplianceDatePretty;
    
    private boolean leagacyImport;
    private LocalDateTime complianceTimeStamp;
    private User complianceUser;
    private EventCECase compTimeFrameComplianceEvent;
    private int complianceTimeframeEventID;
    
    private List<Integer> photoList;

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
     * @return the citationID
     */
    public int getCitationID() {
        return citationID;
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
     * @param citationID the citationID to set
     */
    public void setCitationID(int citationID) {
        this.citationID = citationID;
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
     * @return the entryTimeStamp
     */
    public LocalDateTime getEntryTimeStamp() {
        return entryTimeStamp;
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
     * @param entryTimeStamp the entryTimeStamp to set
     */
    public void setEntryTimeStamp(LocalDateTime entryTimeStamp) {
        this.entryTimeStamp = entryTimeStamp;
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
     * @return the citationsStringList
     */
    public String getCitationsStringList() {
        StringBuilder sb = new StringBuilder();
        Iterator<Integer> it;
        
        if(!citationIDList.isEmpty()){
            sb.append("Citation IDs: ");
            it = citationIDList.iterator();
            while(it.hasNext()){
                Integer i = it.next();
                sb.append(String.valueOf(i));
                if(it.hasNext()){
                    sb.append(", ");
                }
            }
            citationsStringList = sb.toString();
        } else citationsStringList = "";
        return citationsStringList;
    }

    /**
     * @param citationsStringList the citationsStringList to set
     */
    public void setCitationsStringList(String citationsStringList) {
        this.citationsStringList = citationsStringList;
    }

    /**
     * @return the daysUntilStipulatedComplianceDate
     */
    public long getDaysUntilStipulatedComplianceDate() {
        daysUntilStipulatedComplianceDate = 
                getTimePeriodAsDays(LocalDateTime.now(), stipulatedComplianceDate);
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
        dateOfCitationPretty = getPrettyDate(dateOfCitation);
        return dateOfCitationPretty;
    }

    /**
     * @return the dateOfRecordPretty
     */
    public String getDateOfRecordPretty() {
        dateOfRecordPretty = getPrettyDate(dateOfRecord);
        return dateOfRecordPretty;
    }

    /**
     * @return the entryTimeStampPretty
     */
    public String getEntryTimeStampPretty() {
        entryTimeStampPretty = getPrettyDate(entryTimeStamp);
        
        return entryTimeStampPretty;
    }

    /**
     * @return the stipulatedComplianceDatePretty
     */
    public String getStipulatedComplianceDatePretty() {
        stipulatedComplianceDatePretty = getPrettyDate(stipulatedComplianceDate);
        return stipulatedComplianceDatePretty;
    }

    /**
     * @return the actualComplianceDatePretty
     */
    public String getActualComplianceDatePretty() {
        actualComplianceDatePretty = getPrettyDate(actualComplianceDate);
        
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
     * @param entryTimeStampPretty the entryTimeStampPretty to set
     */
    public void setEntryTimeStampPretty(String entryTimeStampPretty) {
        this.entryTimeStampPretty = entryTimeStampPretty;
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
     * @return the photoList
     */
    public List<Integer> getPhotoList() {
        return photoList;
    }

    /**
     * @param photoList the photoList to set
     */
    public void setPhotoList(List<Integer> photoList) {
        this.photoList = photoList;
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
    public EventCECase getCompTimeFrameComplianceEvent() {
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
    public void setCompTimeFrameComplianceEvent(EventCECase compTimeFrameComplianceEvent) {
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
    
    private String getStrElement(){
        StringBuilder sb = new StringBuilder();
        sb.append(violatedEnfElement.getCodeElement().getOrdSubSecNum());
        sb.append(": ");
        sb.append(violatedEnfElement.getCodeElement().getOrdSubSecTitle());
        return sb.toString();
        
    }
    
    /**
     * @return the statusString
     */
    public String getStatusString() {
        StringBuilder sb = new StringBuilder();
        if(actualComplianceDate == null){
            sb.append("Unresolved");
            sb.append("; Compliance due on: ");
            sb.append(getStipulatedComplianceDatePretty());
            
            
        } else {
            sb.append("Compliance achieved on ");
            sb.append(getActualComplianceDatePretty());
        }
        statusString = sb.toString();
        return statusString;
    }

    /**
     * @param statusString the statusString to set
     */
    public void setStatusString(String statusString) {
        
        this.statusString = statusString;
    }
   
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.violationID;
        hash = 53 * hash + Objects.hashCode(this.violatedEnfElement);
        hash = 53 * hash + this.ceCaseID;
        hash = 53 * hash + this.citationID;
        hash = 53 * hash + (int) (Double.doubleToLongBits(this.penalty) ^ (Double.doubleToLongBits(this.penalty) >>> 32));
        hash = 53 * hash + Objects.hashCode(this.description);
        hash = 53 * hash + Objects.hashCode(this.notes);
        hash = 53 * hash + Objects.hashCode(this.dateOfCitation);
        hash = 53 * hash + Objects.hashCode(this.dateOfCitationPretty);
        hash = 53 * hash + Objects.hashCode(this.citationIDList);
        hash = 53 * hash + Objects.hashCode(this.citationsStringList);
        hash = 53 * hash + Objects.hashCode(this.dateOfRecord);
        hash = 53 * hash + Objects.hashCode(this.dateOfRecordPretty);
        hash = 53 * hash + Objects.hashCode(this.entryTimeStamp);
        hash = 53 * hash + Objects.hashCode(this.entryTimeStampPretty);
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
        hash = 53 * hash + Objects.hashCode(this.photoList);
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

}
