/*
 * Copyright (C) 2018 Turtle Creek Valley
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
public class Citation 
        extends BOb
        implements Serializable {
    private int citationID;
    private String citationNo;
    private CitationStatus status;
    private boolean nonStausUpdatesAllowed;
    private CourtEntity origin_courtentity;
    
    private String officialText;
    
    private List<EventCnF> eventList;
    
    private CECase ceCaseNoLists;
    private User userOwner;
    
    private LocalDateTime dateOfRecord;
    private java.util.Date dateOfRecordUtilDate;
    private String dateOfRecordPretty;
    
    private LocalDateTime timeStamp;
    private String timeStampPretty;
    
    private boolean isActive;
    private String notes;
    
    // notice that to avoid cycles, the Citation is allowed to have actual CodeViolation
    // objects in its LinkedList but CodeViolation only gets the citation IDs which
    // it can use to look up a Citation if needs be
    private List<CodeViolation> violationList;

    /**
     * @return the citationID
     */
    public int getCitationID() {
        return citationID;
    }

    /**
     * @return the citationNo
     */
    public String getCitationNo() {
        return citationNo;
    }

   
    
    /**
     * @return the userOwner
     */
    public User getUserOwner() {
        return userOwner;
    }

    /**
     * @return the dateOfRecord
     */
    public LocalDateTime getDateOfRecord() {
        return dateOfRecord;
    }

    /**
     * @return the timeStamp
     */
    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    /**
     * @return the isActive
     */
    public boolean isIsActive() {
        return isActive;
    }

    /**
     * @return the notesO
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param citationID the citationID to set
     */
    public void setCitationID(int citationID) {
        this.citationID = citationID;
    }

    /**
     * @param citationNo the citationNo to set
     */
    public void setCitationNo(String citationNo) {
        this.citationNo = citationNo;
    }


    

    /**
     * @param userOwner the userOwner to set
     */
    public void setUserOwner(User userOwner) {
        this.userOwner = userOwner;
    }

    /**
     * @param dateOfRecord the dateOfRecord to set
     */
    public void setDateOfRecord(LocalDateTime dateOfRecord) {
        this.dateOfRecord = dateOfRecord;
    }

    /**
     * @param timeStamp the timeStamp to set
     */
    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * @param isActive the isActive to set
     */
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the violationList
     */
    public List<CodeViolation> getViolationList() {
        return violationList;
    }

    /**
     * @param violationList the violationList to set
     */
    public void setViolationList(List<CodeViolation> violationList) {
        this.violationList = violationList;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + this.citationID;
        hash = 71 * hash + Objects.hashCode(this.citationNo);
        hash = 71 * hash + Objects.hashCode(this.ceCaseNoLists);
        hash = 71 * hash + Objects.hashCode(this.userOwner);
        hash = 71 * hash + Objects.hashCode(this.dateOfRecord);
        hash = 71 * hash + Objects.hashCode(this.timeStamp);
        hash = 71 * hash + (this.isActive ? 1 : 0);
        hash = 71 * hash + Objects.hashCode(this.notes);
        hash = 71 * hash + Objects.hashCode(this.violationList);
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
        final Citation other = (Citation) obj;
        if (this.citationID != other.citationID) {
            return false;
        }
        if (this.getOrigin_courtentity() != other.getOrigin_courtentity()) {
            return false;
        }
        if (this.isActive != other.isActive) {
            return false;
        }
        if (!Objects.equals(this.citationNo, other.citationNo)) {
            return false;
        }
        if (!Objects.equals(this.notes, other.notes)) {
            return false;
        }
        if (!Objects.equals(this.ceCaseNoLists, other.ceCaseNoLists)) {
            return false;
        }
        if (!Objects.equals(this.userOwner, other.userOwner)) {
            return false;
        }
        if (!Objects.equals(this.dateOfRecord, other.dateOfRecord)) {
            return false;
        }
        if (!Objects.equals(this.timeStamp, other.timeStamp)) {
            return false;
        }
        if (!Objects.equals(this.violationList, other.violationList)) {
            return false;
        }
        return true;
    }

    /**
     * @return the status
     */
    public CitationStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(CitationStatus status) {
        this.status = status;
    }

    /**
     * @return the origin_courtentity
     */
    public CourtEntity getOrigin_courtentity() {
        return origin_courtentity;
    }

    /**
     * @param origin_courtentity the origin_courtentity to set
     */
    public void setOrigin_courtentity(CourtEntity origin_courtentity) {
        this.origin_courtentity = origin_courtentity;
    }

    /**
     * @return the ceCaseNoLists
     */
    public CECase getCeCaseNoLists() {
        return ceCaseNoLists;
    }

    /**
     * @param ceCaseNoLists the ceCaseNoLists to set
     */
    public void setCeCaseNoLists(CECase ceCaseNoLists) {
        this.ceCaseNoLists = ceCaseNoLists;
    }

    /**
     * @return the dateOfRecordUtilDate
     */
    public java.util.Date getDateOfRecordUtilDate() {
        dateOfRecordUtilDate = convertUtilDate(dateOfRecord);
        return dateOfRecordUtilDate;
    }

    /**
     * @param dateOfRecordUtilDate the dateOfRecordUtilDate to set
     */
    public void setDateOfRecordUtilDate(java.util.Date dateOfRecordUtilDate) {
        this.dateOfRecordUtilDate = dateOfRecordUtilDate;
        this.dateOfRecord = convertUtilDate(dateOfRecordUtilDate);
    }

    /**
     * @return the dateOfRecordPretty
     */
    public String getDateOfRecordPretty() {
        dateOfRecordPretty = EntityUtils.getPrettyDate(dateOfRecord);
        return dateOfRecordPretty;
    }

    /**
     * @return the timeStampPretty
     */
    public String getTimeStampPretty() {
        timeStampPretty = EntityUtils.getPrettyDate(timeStamp);
        return timeStampPretty;
    }

    /**
     * @return the eventList
     */
    public List<EventCnF> getEventList() {
        return eventList;
    }

    /**
     * @param eventList the eventList to set
     */
    public void setEventList(List<EventCnF> eventList) {
        this.eventList = eventList;
    }

    /**
     * @return the nonStausUpdatesAllowed
     */
    public boolean isNonStausUpdatesAllowed() {
        return nonStausUpdatesAllowed;
    }

    /**
     * @param nonStausUpdatesAllowed the nonStausUpdatesAllowed to set
     */
    public void setNonStausUpdatesAllowed(boolean nonStausUpdatesAllowed) {
        this.nonStausUpdatesAllowed = nonStausUpdatesAllowed;
    }

    /**
     * @return the officialText
     */
    public String getOfficialText() {
        return officialText;
    }

    /**
     * @param officialText the officialText to set
     */
    public void setOfficialText(String officialText) {
        this.officialText = officialText;
    }
    
    
    
    
}


/*


CREATE TABLE citation
(
    citationID                      INTEGER DEFAULT nextval('citation_citationID_seq') NOT NULL, 
    citationNo                      text, --collaboratively created with munis
    origin_courtentity     INTEGER NOT NULL, --fk
    cecase_caseID                   INTEGER NOT NULL, --fk
    login_userID                    INTEGER NOT NULL, --fk
    dateOfRecord                    TIMESTAMP WITH TIME ZONE NOT NULL,
    transTimeStamp                  TIMESTAMP WITH TIME ZONE NOT NULL,
    isActive                        boolean DEFAULT TRUE,
    notes                           text
    -- this is just a skeleton for a citation: more fields likely as system develops
) ;
*/
