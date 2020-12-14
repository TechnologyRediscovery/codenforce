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

import com.tcvcog.tcvce.application.interfaces.IFace_Loggable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public  class       EventCnF 
        extends     BOb
        implements  Comparable<EventCnF>,
                    IFace_Loggable{
    
    protected int eventID;
    protected EventCategory category;
    protected String description;
    
    /**
     * Indicates to clients which BOb identifier to use
     * Alternatively, one could have only one field for the ID
     * and use Domain to control what we do with that ID
     * But that seemed to likely lead to more confusion since the DB
     * has two columns, since each is keyed differently
     */
    protected EventDomainEnum domain;
    protected int ceCaseID;
    protected int occPeriodID;
    
    protected LocalDateTime timeStart;
    protected LocalDateTime timeEnd;
    
    protected User userCreator;
    protected LocalDateTime creationts;
    
    protected User lastUpdatedBy;
    protected LocalDateTime lastUpdatedTS;
    
    protected boolean active;
    
    /**
     * Only for use in JavaLand; no DB col for hiding
     */
    protected boolean hidden;
    protected String notes;
    
    protected List<Person> personList;
    
    public EventCnF(){
        
        
    }
    
    public EventCnF(EventCnF ev){
        
        this.eventID = ev.eventID;
        this.category = ev.category;
        this.description = ev.description;
        
        this.domain = ev.domain;
        this.ceCaseID = ev.ceCaseID;
        this.occPeriodID = ev.occPeriodID;
        
        this.timeStart = ev.timeStart;
        this.timeEnd = ev.timeEnd;
        
        this.userCreator = ev.userCreator;
        this.creationts = ev.creationts;
        
        this.lastUpdatedBy = ev.lastUpdatedBy;
        this.lastUpdatedTS = ev.lastUpdatedTS;
        
        this.active = ev.active;
        this.hidden = ev.hidden;
        
        this.notes = ev.notes;
        
        this.personList = ev.personList;
    }
    
    
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
     * @return the creationts
     */
    public LocalDateTime getCreationts() {
        return creationts;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the userCreator
     */
    public User getUserCreator() {
        return userCreator;
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
     * @param creationts the creationts to set
     */
    public void setCreationts(LocalDateTime creationts) {
        this.creationts = creationts;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param userCreator the userCreator to set
     */
    public void setUserCreator(User userCreator) {
        this.userCreator = userCreator;
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
    public int compareTo(EventCnF e) {
        int c = 0;
        if(this.timeStart != null && e.timeStart != null){
             c = this.timeStart.compareTo(e.timeStart);
        } else if(this.creationts != null && e.creationts != null){
             c = this.creationts.compareTo(e.creationts);
        } 
        return c;
        
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.eventID;
        hash = 97 * hash + Objects.hashCode(this.category);
        hash = 97 * hash + Objects.hashCode(this.creationts);
        hash = 97 * hash + Objects.hashCode(this.description);
        hash = 97 * hash + Objects.hashCode(this.userCreator);
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
        final EventCnF other = (EventCnF) obj;
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
        return convertUtilDate(timeStart);
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
        return convertUtilDate(timeEnd);
    }
    
    public String getTimeStartPretty(){
        return EntityUtils.getPrettyDate(timeStart);
        
    }

    public String getTimeEndPretty(){
        return EntityUtils.getPrettyDate(timeEnd);
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
        timeStart = convertUtilDate(tsud);
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
        timeEnd = convertUtilDate(teud);
    }

    /**
     * @return the lastUpdatedBy
     */
    public User getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    /**
     * @return the lastUpdatedTS
     */
    public LocalDateTime getLastUpdatedTS() {
        return lastUpdatedTS;
    }

    /**
     * @param lastUpdatedBy the lastUpdatedBy to set
     */
    public void setLastUpdatedBy(User lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    /**
     * @param lastUpdatedTS the lastUpdatedTS to set
     */
    public void setLastUpdatedTS(LocalDateTime lastUpdatedTS) {
        this.lastUpdatedTS = lastUpdatedTS;
    }
   
}