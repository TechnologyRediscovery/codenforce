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
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import com.tcvcog.tcvce.application.interfaces.IFace_ActivatableBOB;
import com.tcvcog.tcvce.util.DateTimeUtil;

/**
 * Behold the base class of the EventCnF family!
 * @author ellen bascomb of apt 31y
 */
public  class       EventCnF 
        extends     TrackedEntity
        implements  Comparable<EventCnF>,
                    IFace_Loggable,
                    IFace_noteHolder,
                    IFace_ActivatableBOB,
                    IFace_humanListHolder{
    
    final static LinkedObjectSchemaEnum HUMAN_LINK_SCHEMA_ENUM = LinkedObjectSchemaEnum.EventHuman;
    final static String EVENT_FRIENDLY_NAME = "Event";
    final static String EVENT_TABLE_NAME = "event";
    final static String EVENT_PK_FIELD = "eventid";
    
    
    protected int eventID;
    protected EventCategory category;
    protected String description;
    
    
    protected List<HumanLink> humanLinkList;
    
    /**
     * Indicates to clients which BOb identifier to use
     * Alternatively, one could have only one field for the ID
     * and use Domain to control what we do with that ID
     * But that seemed to likely lead to more confusion since the DB
     * has two columns, since each is keyed differently
     */
    protected DomainEnum domain;
    protected int ceCaseID;
    protected int occPeriodID;
    
    protected LocalDateTime timeStart;
    protected LocalDateTime timeEnd;
    
    protected boolean active;
    protected String notes;
    
    /**
     * Only for use in JavaLand; no DB col for hiding
     */
    protected boolean hidden;
    protected long duration;
    
    
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
        
        this.lastUpdatedBy = ev.lastUpdatedBy;
        this.lastUpdatedTS = ev.lastUpdatedTS;
        
        this.active = ev.active;

        this.hidden = ev.hidden;
        this.notes = ev.notes;
        
        
        
        
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
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Special: I read for the presence of deac ts
     * @return the active
     */
    @Override
    public boolean isActive() {
        return deactivatedTS == null;
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
    @Override
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
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
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
    @Override
    public void setNotes(String notes) {
        this.notes = notes;
    }

  
    @Override
    public int compareTo(EventCnF e) {
        int c = 0;
        if(this.timeStart != null && e.timeStart != null){
             c = this.timeStart.compareTo(e.timeStart);
        } else if(this.createdTS != null && e.createdTS != null){
             c = this.createdTS.compareTo(e.createdTS);
        } 
        return c;
        
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.eventID;
        hash = 97 * hash + Objects.hashCode(this.category);
        hash = 97 * hash + Objects.hashCode(this.description);
        hash = 97 * hash + (this.active ? 1 : 0);
        hash = 97 * hash + (this.hidden ? 1 : 0);
        hash = 97 * hash + Objects.hashCode(this.notes);
        return hash;
    }

    /**
     * Uses only the event ID for equality checking! Not all fields
     * within the event itself, so this method should not be used to
     * figure out if, say, an event object has had its description changed
     * or not
     * @param obj
     * @return 
     */
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
    public DomainEnum getDomain() {
        return domain;
    }

    /**
     * @param domain the domain to set
     */
    public void setDomain(DomainEnum domain) {
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
     * @return the timeEnd
     */
    public LocalDateTime getTimeEnd() {
        
        return timeEnd;
    }

   
    
    public String getTimeStartPretty(){
        return DateTimeUtil.getPrettyDate(timeStart);
        
    }

    public String getTimeEndPretty(){
        return DateTimeUtil.getPrettyDate(timeEnd);
    }
    
    /**
     * @param timeStart the timeStart to set
     */
    public void setTimeStart(LocalDateTime timeStart) {
        this.timeStart = timeStart;
    }

   

    /**
     * @param timeEnd the timeEnd to set
     */
    public void setTimeEnd(LocalDateTime timeEnd) {
        this.timeEnd = timeEnd;
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
   
    
    @Override
    public List<HumanLink> getHumanLinkList() {
        return humanLinkList;
    }

    @Override
    public void setHumanLinkList(List<HumanLink> hll) {
        humanLinkList = hll;
    }

    @Override
    public LinkedObjectSchemaEnum getHUMAN_LINK_SCHEMA_ENUM() {
        return HUMAN_LINK_SCHEMA_ENUM;
    }



    @Override
    public int getHostPK() {
        return eventID;
    }

    @Override
    public String getNoteHolderFriendlyName() {
        return EVENT_FRIENDLY_NAME;
    }

    @Override
    public String getPKFieldName() {
        return EVENT_PK_FIELD;
    }

    @Override
    public int getDBKey() {
        return this.eventID;
    }

    @Override
    public String getDBTableName() {
        return EVENT_TABLE_NAME;
    }

    /**
     * @return the duration
     */
    public long getDuration() {
       
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    
}