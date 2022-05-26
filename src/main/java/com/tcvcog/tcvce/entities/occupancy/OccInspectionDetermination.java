/*
 * Copyright (C) 2021 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.EventCategory;

public class OccInspectionDetermination {

    private int determinationID;

    private String title;
    private String description;

    private String notes;
    private boolean qualifiesAsPassed;

    // This could be the ID? depends if the object is too heavy with it's icon and stuff
    private EventCategory eventCategory;

    private boolean active;

    public OccInspectionDetermination() {}

    public OccInspectionDetermination(OccInspectionDetermination occInspectionDetermination) {
        this.determinationID = occInspectionDetermination.getDeterminationID();
        this.title = occInspectionDetermination.getTitle();
        this.description = occInspectionDetermination.getDescription();
        this.notes = occInspectionDetermination.getNotes();
        this.eventCategory = occInspectionDetermination.getEventCategory();
        this.active = occInspectionDetermination.isActive();
    }

    public int getDeterminationID() {
        return determinationID;
    }

    public void setDeterminationID(int determinationID) {
        this.determinationID = determinationID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public EventCategory getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(EventCategory eventCategory) {
        this.eventCategory = eventCategory;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the qualifiesAsPassed
     */
    public boolean isQualifiesAsPassed() {
        return qualifiesAsPassed;
    }

    /**
     * @param qualifiesAsPassed the qualifiesAsPassed to set
     */
    public void setQualifiesAsPassed(boolean qualifiesAsPassed) {
        this.qualifiesAsPassed = qualifiesAsPassed;
    }
}
