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
    
    private Event triggeringEvent;
    private Event responseEvent;
    
    private boolean currentUserCanTakeAction;
    private EventCategory requestedEventCategory;
    private boolean requestedEventIDRequired;
    private User actionRequestedBy;
    private User responderIntended;
    private User responderActual;
    private LocalDateTime responseTimestamp;
    private String responseTimePrettyDate;
    private String responseNotes;
    private boolean actionRequestRejected;

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
     * @return the requestedEventIDRequired
     */
    public boolean isRequestedEventIDRequired() {
        return requestedEventIDRequired;
    }

    /**
     * @return the responderActual
     */
    public User getResponderActual() {
        return responderActual;
    }

    /**
     * @return the responseTimestamp
     */
    public LocalDateTime getResponseTimestamp() {
        return responseTimestamp;
    }

    /**
     * @param requestedEventIDRequired the requestedEventIDRequired to set
     */
    public void setRequestedEventIDRequired(boolean requestedEventIDRequired) {
        this.requestedEventIDRequired = requestedEventIDRequired;
    }

    /**
     * @param responderActual the responderActual to set
     */
    public void setResponderActual(User responderActual) {
        this.responderActual = responderActual;
    }

    /**
     * @param responseTimestamp the responseTimestamp to set
     */
    public void setResponseTimestamp(LocalDateTime responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
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
     * @return the responseNotes
     */
    public String getResponseNotes() {
        return responseNotes;
    }

    /**
     * @param responseNotes the responseNotes to set
     */
    public void setResponseNotes(String responseNotes) {
        this.responseNotes = responseNotes;
    }

    /**
     * @return the responseTimePrettyDate
     */
    public String getResponseTimePrettyDate() {
        String pretty = getPrettyDate(responseTimestamp);
        responseTimePrettyDate = pretty;
        return responseTimePrettyDate;
    }

    /**
     * @param responseTimePrettyDate the responseTimePrettyDate to set
     */
    public void setResponseTimePrettyDate(String responseTimePrettyDate) {
        this.responseTimePrettyDate = responseTimePrettyDate;
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
     * @return the currentUserCanTakeAction
     */
    public boolean isCurrentUserCanTakeAction() {
        return currentUserCanTakeAction;
    }

    /**
     * @param currentUserCanTakeAction the currentUserCanTakeAction to set
     */
    public void setCurrentUserCanTakeAction(boolean currentUserCanTakeAction) {
        this.currentUserCanTakeAction = currentUserCanTakeAction;
    }

    /**
     * @return the actionRequestedBy
     */
    public User getActionRequestedBy() {
        return actionRequestedBy;
    }

    /**
     * @param actionRequestedBy the actionRequestedBy to set
     */
    public void setActionRequestedBy(User actionRequestedBy) {
        this.actionRequestedBy = actionRequestedBy;
    }

    /**
     * @return the responderIntended
     */
    public User getResponderIntended() {
        return responderIntended;
    }

    /**
     * @param responderIntended the responderIntended to set
     */
    public void setResponderIntended(User responderIntended) {
        this.responderIntended = responderIntended;
    }

    /**
     * @return the actionRequestRejected
     */
    public boolean isActionRequestRejected() {
        return actionRequestRejected;
    }

    /**
     * @param actionRequestRejected the actionRequestRejected to set
     */
    public void setActionRequestRejected(boolean actionRequestRejected) {
        this.actionRequestRejected = actionRequestRejected;
    }

    /**
     * @return the requestedEventCategory
     */
    public EventCategory getRequestedEventCategory() {
        return requestedEventCategory;
    }

    /**
     * @param requestedEventCategory the requestedEventCategory to set
     */
    public void setRequestedEventCategory(EventCategory requestedEventCategory) {
        this.requestedEventCategory = requestedEventCategory;
    }

    /**
     * @return the triggeringEvent
     */
    public Event getTriggeringEvent() {
        return triggeringEvent;
    }

    /**
     * @param triggeringEvent the triggeringEvent to set
     */
    public void setTriggeringEvent(Event triggeringEvent) {
        this.triggeringEvent = triggeringEvent;
    }

    /**
     * @return the responseEvent
     */
    public Event getResponseEvent() {
        return responseEvent;
    }

    /**
     * @param responseEvent the responseEvent to set
     */
    public void setResponseEvent(Event responseEvent) {
        this.responseEvent = responseEvent;
    }

   

  

    
}
