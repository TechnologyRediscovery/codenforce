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

public class OccInspectionCause {

    private int causeID;

    private String title;
    private String description;

    private String notes;

    private boolean active;

    public OccInspectionCause() {}

    public OccInspectionCause(OccInspectionCause occInspectionCause) {
        this.causeID = occInspectionCause.getCauseID();
        this.title = occInspectionCause.getTitle();
        this.description = occInspectionCause.getDescription();
        this.notes = occInspectionCause.getNotes();
        this.active = occInspectionCause.isActive();
    }

    public int getCauseID() {
        return causeID;
    }

    public void setCauseID(int causeID) {
        this.causeID = causeID;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
