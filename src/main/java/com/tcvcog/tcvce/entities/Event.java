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
import java.time.ZoneId;

/**
 *
 * @author Eric C. Darsow
 */
public class Event extends EntityUtils implements Serializable {
    
    private int eventID;
    private EventCategory category;
    
    private LocalDateTime dateOfRecord;
    private String dateOfRecordPretty;
    private java.util.Date dateOfRecordUtilDate;
    private LocalDateTime timestamp;
    private String timestampPretty;
    
    private String description;
    private User creator;
    private User assignedTo;
    private boolean discloseToMunicipality;
    private boolean discloseToPublic;
    private boolean active;
    private boolean hidden;
    private String notes;
    
    private boolean requiresViewConfirmation;
    private User viewConfRequestedBy;
    private boolean currentUserCanConfirm;
    private boolean viewConfirmed;
    private User viewConfirmedBy;
    private LocalDateTime viewConfirmedAt;
    private String viewConfAtPrettyDate;
    private String viewNotes;

    /**
     * @return the eventID
     */
    public int getEventID() {
        return eventID;
    }

    /**
     * @return the category
     */
    public EventCategory getCategory() {
        return category;
    }

    /**
     * @return the dateOfRecord
     */
    public LocalDateTime getDateOfRecord() {
        return dateOfRecord;
    }

    /**
     * @return the dateOfRecordPretty
     */
    public String getDateOfRecordPretty() {
        String pretty = getPrettyDate(dateOfRecord);
        dateOfRecordPretty = pretty;
        return dateOfRecordPretty;
    }

    /**
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the creator
     */
    public User getCreator() {
        return creator;
    }

    /**
     * @return the discloseToMunicipality
     */
    public boolean isDiscloseToMunicipality() {
        return discloseToMunicipality;
    }

    /**
     * @return the discloseToPublic
     */
    public boolean isDiscloseToPublic() {
        return discloseToPublic;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @return the hidden
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param eventID the eventID to set
     */
    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(EventCategory category) {
        this.category = category;
    }

    /**
     * @param dateOfRecord the dateOfRecord to set
     */
    public void setDateOfRecord(LocalDateTime dateOfRecord) {
        this.dateOfRecord = dateOfRecord;
    }

    
    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param creator the creator to set
     */
    public void setCreator(User creator) {
        this.creator = creator;
    }

    /**
     * @param discloseToMunicipality the discloseToMunicipality to set
     */
    public void setDiscloseToMunicipality(boolean discloseToMunicipality) {
        this.discloseToMunicipality = discloseToMunicipality;
    }

    /**
     * @param discloseToPublic the discloseToPublic to set
     */
    public void setDiscloseToPublic(boolean discloseToPublic) {
        this.discloseToPublic = discloseToPublic;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @param hidden the hidden to set
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the requiresViewConfirmation
     */
    public boolean isRequiresViewConfirmation() {
        return requiresViewConfirmation;
    }

    /**
     * @return the viewConfirmedBy
     */
    public User getViewConfirmedBy() {
        return viewConfirmedBy;
    }

    /**
     * @return the viewConfirmedAt
     */
    public LocalDateTime getViewConfirmedAt() {
        return viewConfirmedAt;
    }

    /**
     * @param requiresViewConfirmation the requiresViewConfirmation to set
     */
    public void setRequiresViewConfirmation(boolean requiresViewConfirmation) {
        this.requiresViewConfirmation = requiresViewConfirmation;
    }

    /**
     * @param viewConfirmedBy the viewConfirmedBy to set
     */
    public void setViewConfirmedBy(User viewConfirmedBy) {
        this.viewConfirmedBy = viewConfirmedBy;
    }

    /**
     * @param viewConfirmedAt the viewConfirmedAt to set
     */
    public void setViewConfirmedAt(LocalDateTime viewConfirmedAt) {
        this.viewConfirmedAt = viewConfirmedAt;
    }

    /**
     * @return the viewConfirmed
     */
    public boolean isViewConfirmed() {
        return viewConfirmed;
    }

    /**
     * @param viewConfirmed the viewConfirmed to set
     */
    public void setViewConfirmed(boolean viewConfirmed) {
        this.viewConfirmed = viewConfirmed;
    }

    /**
     * @return the dateOfRecordUtilDate
     */
    public java.util.Date getDateOfRecordUtilDate() {
        if(dateOfRecord != null){
            dateOfRecordUtilDate = java.util.Date.from(
                    this.dateOfRecord.atZone(ZoneId.systemDefault()).toInstant());
        }
        return dateOfRecordUtilDate;
    }

    /**
     * @param dateOfRecordUtilDate the dateOfRecordUtilDate to set
     */
    public void setDateOfRecordUtilDate(java.util.Date dateOfRecordUtilDate) {
        this.dateOfRecordUtilDate = dateOfRecordUtilDate;
        if(dateOfRecordUtilDate != null){
            dateOfRecord = this.dateOfRecordUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

    /**
     * @return the assignedTo
     */
    public User getAssignedTo() {
        return assignedTo;
    }

    /**
     * @param assignedTo the assignedTo to set
     */
    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    /**
     * @return the viewNotes
     */
    public String getViewNotes() {
        return viewNotes;
    }

    /**
     * @param viewNotes the viewNotes to set
     */
    public void setViewNotes(String viewNotes) {
        this.viewNotes = viewNotes;
    }

    /**
     * @return the viewConfAtPrettyDate
     */
    public String getViewConfAtPrettyDate() {
        String pretty = getPrettyDate(viewConfirmedAt);
        viewConfAtPrettyDate = pretty;
        return viewConfAtPrettyDate;
    }

    /**
     * @param viewConfAtPrettyDate the viewConfAtPrettyDate to set
     */
    public void setViewConfAtPrettyDate(String viewConfAtPrettyDate) {
        this.viewConfAtPrettyDate = viewConfAtPrettyDate;
    }

    /**
     * @return the timestampPretty
     */
    public String getTimestampPretty() {
        String s = getPrettyDate(timestamp);
        timestampPretty = s;
        return timestampPretty;
    }

    /**
     * @param timestampPretty the timestampPretty to set
     */
    public void setTimestampPretty(String timestampPretty) {
        this.timestampPretty = timestampPretty;
    }

    /**
     * @return the currentUserCanConfirm
     */
    public boolean isCurrentUserCanConfirm() {
        return currentUserCanConfirm;
    }

    /**
     * @param currentUserCanConfirm the currentUserCanConfirm to set
     */
    public void setCurrentUserCanConfirm(boolean currentUserCanConfirm) {
        this.currentUserCanConfirm = currentUserCanConfirm;
    }

    /**
     * @return the viewConfRequestedBy
     */
    public User getViewConfRequestedBy() {
        return viewConfRequestedBy;
    }

    /**
     * @param viewConfRequestedBy the viewConfRequestedBy to set
     */
    public void setViewConfRequestedBy(User viewConfRequestedBy) {
        this.viewConfRequestedBy = viewConfRequestedBy;
    }

   

  

    
}
