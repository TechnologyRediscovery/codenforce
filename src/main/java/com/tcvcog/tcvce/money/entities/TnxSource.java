/*
 * Copyright (C) 2018 Adam Gutonski
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
package com.tcvcog.tcvce.money.entities;

import com.tcvcog.tcvce.entities.EntityUtils;
import com.tcvcog.tcvce.entities.EventCategory;
import java.util.Objects;

/**
 *
 * @author Adam Gutonski & Major refactor by Ellen Bascomb for June 2022 launch
 */
public class TnxSource extends EntityUtils {
    
    protected int sourceID;
    protected String title;
    protected String description;
    protected String notes;
    protected boolean humanAssignable;
    protected EventCategory eventCategory;
    protected TnxTypeEnum applicableTnxType;
    protected boolean active;
    
    /**
     * @return the sourceID
     */
    public int getSourceID() {
        return sourceID;
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
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @return the humanAssignable
     */
    public boolean isHumanAssignable() {
        return humanAssignable;
    }

    /**
     * @return the eventCategory
     */
    public EventCategory getEventCategory() {
        return eventCategory;
    }

    /**
     * @return the applicableTnxType
     */
    public TnxTypeEnum getApplicableTnxType() {
        return applicableTnxType;
    }

    /**
     * @param sourceID the sourceID to set
     */
    public void setSourceID(int sourceID) {
        this.sourceID = sourceID;
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
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @param humanAssignable the humanAssignable to set
     */
    public void setHumanAssignable(boolean humanAssignable) {
        this.humanAssignable = humanAssignable;
    }

    /**
     * @param eventCategory the eventCategory to set
     */
    public void setEventCategory(EventCategory eventCategory) {
        this.eventCategory = eventCategory;
    }

    /**
     * @param applicableTnxType the applicableTnxType to set
     */
    public void setApplicableTnxType(TnxTypeEnum applicableTnxType) {
        this.applicableTnxType = applicableTnxType;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    
}
