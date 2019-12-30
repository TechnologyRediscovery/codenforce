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
 * @author ellen bascomb of apt 31y
 */
public  class       Event 
        extends     BOb
        implements  Comparable<Event> {
    
    protected int eventID;
    protected EventCategory category;
    
    protected EventDomainEnum domain;
    
    protected int ceCaseID;
    protected int occPeriodID;
    
    protected LocalDateTime timeStart;
    protected java.util.Date timeStartUtilDate;
    protected LocalDateTime timeEnd;
    protected java.util.Date timeEndUtilDate;
    
    protected LocalDateTime timestamp;
    
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
     * @return the timestampPretty
     */
    public String getTimestampPretty() {
        String s = EntityUtils.getPrettyDate(timestamp);
        return s;
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
        int c = this.timeStart.compareTo(e.timeStart);
        return c;
        
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.eventID;
        hash = 97 * hash + Objects.hashCode(this.category);
        hash = 97 * hash + Objects.hashCode(this.timestamp);
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
    public EventDomainEnum getDomain() {
        return domain;
    }

    /**
     * @param domain the domain to set
     */
    public void setDomain(EventDomainEnum domain) {
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

    /**
     * @return the timeStart
     */
    public LocalDateTime getTimeStart() {
        return timeStart;
    }

    /**
     * @return the timeStartUtilDate
     */
    public java.util.Date getTimeStartUtilDate() {
         if(timeStart != null){
            timeStartUtilDate = java.util.Date.from(
                    this.timeStart.atZone(ZoneId.systemDefault()).toInstant());
        }
        return timeStartUtilDate;
    }

    /**
     * @return the timeEnd
     */
    public LocalDateTime getTimeEnd() {
        
        return timeEnd;
    }

    /**
     * @return the timeEndUtilDate
     */
    public java.util.Date getTimeEndUtilDate() {
         if(timeEnd != null){
            timeEndUtilDate = java.util.Date.from(
                    this.timeEnd.atZone(ZoneId.systemDefault()).toInstant());
        }
        return timeEndUtilDate;
    }

    /**
     * @param timeStart the timeStart to set
     */
    public void setTimeStart(LocalDateTime timeStart) {
        this.timeStart = timeStart;
    }

    /**
     * @param tsud
     */
    public void setTimeStartUtilDate(java.util.Date tsud) {
        this.timeStartUtilDate = tsud;
        if(tsud != null){
            timeStart = this.timeStartUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

    /**
     * @param timeEnd the timeEnd to set
     */
    public void setTimeEnd(LocalDateTime timeEnd) {
        this.timeEnd = timeEnd;
    }

    /**
     * @param teud
     */
    public void setTimeEndUtilDate(java.util.Date teud) {
        this.timeEndUtilDate = teud;
        if(teud != null){
            timeEnd = this.timeEndUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }


    

    
}
