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

/**
 *
 * @author Eric C. Darsow
 */
public class Event extends EntityUtils implements Serializable {
    
    private int eventID;
    private EventCategory category;
    private LocalDateTime dateOfRecord;
    private String prettyDateOfRecord;
    private LocalDateTime eventTimeStamp;
    private java.util.Date dateOfRecordUtilDate;
    private String eventDescription;
    private User eventOwnerUser;
    private boolean discloseToMunicipality;
    private boolean discloseToPublic;
    private boolean activeEvent;
    private boolean hidden;
    private String notes;
    private boolean requiresViewConfirmation;
    // this boolean is not mapped into DB--only for using switches by user
    private boolean viewConfirmed;
    private User viewConfirmedBy;
    private LocalDateTime viewConfirmedAt;

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
     * @return the prettyDateOfRecord
     */
    public String getPrettyDateOfRecord() {
        return prettyDateOfRecord;
    }

    /**
     * @return the eventTimeStamp
     */
    public LocalDateTime getEventTimeStamp() {
        return eventTimeStamp;
    }

    /**
     * @return the eventDescription
     */
    public String getEventDescription() {
        return eventDescription;
    }

    /**
     * @return the eventOwnerUser
     */
    public User getEventOwnerUser() {
        return eventOwnerUser;
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
     * @return the activeEvent
     */
    public boolean isActiveEvent() {
        return activeEvent;
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
     * @param prettyDateOfRecord the prettyDateOfRecord to set
     */
    public void setPrettyDateOfRecord(String prettyDateOfRecord) {
        this.prettyDateOfRecord = prettyDateOfRecord;
    }

    /**
     * @param eventTimeStamp the eventTimeStamp to set
     */
    public void setEventTimeStamp(LocalDateTime eventTimeStamp) {
        this.eventTimeStamp = eventTimeStamp;
    }

    /**
     * @param eventDescription the eventDescription to set
     */
    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    /**
     * @param eventOwnerUser the eventOwnerUser to set
     */
    public void setEventOwnerUser(User eventOwnerUser) {
        this.eventOwnerUser = eventOwnerUser;
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
     * @param activeEvent the activeEvent to set
     */
    public void setActiveEvent(boolean activeEvent) {
        this.activeEvent = activeEvent;
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
        return dateOfRecordUtilDate;
    }

    /**
     * @param dateOfRecordUtilDate the dateOfRecordUtilDate to set
     */
    public void setDateOfRecordUtilDate(java.util.Date dateOfRecordUtilDate) {
        this.dateOfRecordUtilDate = dateOfRecordUtilDate;
    }

    
}
