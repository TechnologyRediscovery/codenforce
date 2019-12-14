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
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Eric C. Darsow
 */
public  class       Event 
        extends     BOb
        implements  Comparable<Event> {
    
    protected int eventID;
    protected EventCategory category;
    
    protected EventDomain domain;
    protected int ceCaseID;
    protected int occPeriodID;
    
    protected LocalDateTime dateOfRecord;
    protected String dateOfRecordPretty;
    protected java.util.Date dateOfRecordUtilDate;
    protected LocalDateTime timestamp;
    protected String timestampPretty;
    
    protected String description;
    protected User owner;
    
    protected boolean discloseToMunicipality; 
    protected boolean discloseToPublic;
    protected boolean active;
    protected boolean hidden;
    
    protected String notes;
    
    protected List<Person> personList;
    
    
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
        String pretty = EntityUtils.getPrettyDate(dateOfRecord);
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
     * @return the owner
     */
    public User getOwner() {
        return owner;
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
     * @param owner the owner to set
     */
    public void setOwner(User owner) {
        this.owner = owner;
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
     * @return the timestampPretty
     */
    public String getTimestampPretty() {
        String s = EntityUtils.getPrettyDate(timestamp);
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
     * @return the personList
     */
    public List<Person> getPersonList() {
        return personList;
    }

    /**
     * @param personList the personList to set
     */
    public void setPersonList(List<Person> personList) {
        this.personList = personList;
    }

    

  
    @Override
    public int compareTo(Event e) {
        int c = this.dateOfRecord.compareTo(e.getDateOfRecord());
        return c;
        
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.eventID;
        hash = 97 * hash + Objects.hashCode(this.category);
        hash = 97 * hash + Objects.hashCode(this.dateOfRecord);
        hash = 97 * hash + Objects.hashCode(this.dateOfRecordPretty);
        hash = 97 * hash + Objects.hashCode(this.dateOfRecordUtilDate);
        hash = 97 * hash + Objects.hashCode(this.timestamp);
        hash = 97 * hash + Objects.hashCode(this.timestampPretty);
        hash = 97 * hash + Objects.hashCode(this.description);
        hash = 97 * hash + Objects.hashCode(this.owner);
        hash = 97 * hash + (this.discloseToMunicipality ? 1 : 0);
        hash = 97 * hash + (this.discloseToPublic ? 1 : 0);
        hash = 97 * hash + (this.active ? 1 : 0);
        hash = 97 * hash + (this.hidden ? 1 : 0);
        hash = 97 * hash + Objects.hashCode(this.notes);
        hash = 97 * hash + Objects.hashCode(this.personList);
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
        final Event other = (Event) obj;
        if (this.eventID != other.eventID) {
            return false;
        }
        return true;
    }

    /**
     * @return the domain
     */
    public EventDomain getDomain() {
        return domain;
    }

    /**
     * @param domain the domain to set
     */
    public void setDomain(EventDomain domain) {
        this.domain = domain;
    }

    /**
     * @return the ceCaseID
     */
    public int getCeCaseID() {
        return ceCaseID;
    }

    /**
     * @return the occPeriodID
     */
    public int getOccPeriodID() {
        return occPeriodID;
    }

    /**
     * @param ceCaseID the ceCaseID to set
     */
    public void setCeCaseID(int ceCaseID) {
        this.ceCaseID = ceCaseID;
    }

    /**
     * @param occPeriodID the occPeriodID to set
     */
    public void setOccPeriodID(int occPeriodID) {
        this.occPeriodID = occPeriodID;
    }


    

    
}
