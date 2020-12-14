/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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

import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author sylvia
 */
public class MuniProfile {
    
    private int profileID;
    private String title;
    private String description;
    private LocalDateTime lastupdatedTS;
    private User lastupdatedUser;
    private String notes;
    private int continuousoccupancybufferdays;
    private int minimumuserranktodeclarerentalintent;
    
    private int novDefaultDaysForFollowup;
    
    private EventRuleSet eventRuleSetCE;
    private List<OccPeriodType> occPeriodTypeList;

    /**
     * @return the profileID
     */
    public int getProfileID() {
        return profileID;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the lastupdatedTS
     */
    public LocalDateTime getLastupdatedTS() {
        return lastupdatedTS;
    }

    /**
     * @return the lastupdatedUser
     */
    public User getLastupdatedUser() {
        return lastupdatedUser;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @return the continuousoccupancybufferdays
     */
    public int getContinuousoccupancybufferdays() {
        return continuousoccupancybufferdays;
    }

    /**
     * @return the minimumuserranktodeclarerentalintent
     */
    public int getMinimumuserranktodeclarerentalintent() {
        return minimumuserranktodeclarerentalintent;
    }

    /**
     * @param profileID the profileID to set
     */
    public void setProfileID(int profileID) {
        this.profileID = profileID;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param lastupdatedTS the lastupdatedTS to set
     */
    public void setLastupdatedTS(LocalDateTime lastupdatedTS) {
        this.lastupdatedTS = lastupdatedTS;
    }

    /**
     * @param lastupdatedUser the lastupdatedUser to set
     */
    public void setLastupdatedUser(User lastupdatedUser) {
        this.lastupdatedUser = lastupdatedUser;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @param continuousoccupancybufferdays the continuousoccupancybufferdays to set
     */
    public void setContinuousoccupancybufferdays(int continuousoccupancybufferdays) {
        this.continuousoccupancybufferdays = continuousoccupancybufferdays;
    }

    /**
     * @param minimumuserranktodeclarerentalintent the minimumuserranktodeclarerentalintent to set
     */
    public void setMinimumuserranktodeclarerentalintent(int minimumuserranktodeclarerentalintent) {
        this.minimumuserranktodeclarerentalintent = minimumuserranktodeclarerentalintent;
    }

    /**
     * @return the eventRuleSetCE
     */
    public EventRuleSet getEventRuleSetCE() {
        return eventRuleSetCE;
    }

    /**
     * @param eventRuleSetCE the eventRuleSetCE to set
     */
    public void setEventRuleSetCE(EventRuleSet eventRuleSetCE) {
        this.eventRuleSetCE = eventRuleSetCE;
    }

    /**
     * @return the occPeriodTypeList
     */
    public List<OccPeriodType> getOccPeriodTypeList() {
        return occPeriodTypeList;
    }

    /**
     * @param occPeriodTypeList the occPeriodTypeList to set
     */
    public void setOccPeriodTypeList(List<OccPeriodType> occPeriodTypeList) {
        this.occPeriodTypeList = occPeriodTypeList;
    }
    
     @Override
    public int hashCode() {
        int hash = 15;
        hash = 500 * hash + this.profileID;
        hash = 500 * hash + Objects.hashCode(this.title);
        hash = 500 * hash + Objects.hashCode(this.description);
        hash = 500 * hash + Objects.hashCode(this.notes);
        hash = 500 * hash + this.continuousoccupancybufferdays;
        hash = 500 * hash + this.minimumuserranktodeclarerentalintent;
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
        final MuniProfile other = (MuniProfile) obj;
        if (this.profileID != other.profileID) {
            return false;
        }
        if (this.continuousoccupancybufferdays != other.continuousoccupancybufferdays) {
            return false;
        }
        if (this.minimumuserranktodeclarerentalintent != other.minimumuserranktodeclarerentalintent) {
            return false;
        }
        if (!Objects.equals(this.title, other.title)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.notes, other.notes)) {
            return false;
        }
        return true;
    }

    /**
     * @return the novDefaultDaysForFollowup
     */
    public int getNovDefaultDaysForFollowup() {
        return novDefaultDaysForFollowup;
    }

    /**
     * @param novDefaultDaysForFollowup the novDefaultDaysForFollowup to set
     */
    public void setNovDefaultDaysForFollowup(int novDefaultDaysForFollowup) {
        this.novDefaultDaysForFollowup = novDefaultDaysForFollowup;
    }
}
