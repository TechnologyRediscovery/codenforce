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

/**
 *
 * @author Mike-Faux
 */
public class OccInspectionRequirement {
    
    private int  requirementID;
    
    private String title;
    private String description;
    
    private boolean active;
    
    public OccInspectionRequirement() {
        
    }
    
    public OccInspectionRequirement(OccInspectionRequirement occInspectionRequirement) {
        this.requirementID = occInspectionRequirement.getRequirementID();
        this.title = occInspectionRequirement.getTitle();
        this.description = occInspectionRequirement.getDescription();
        this.active = occInspectionRequirement.isActive();
    }

    /**
     * @return the requirementID
     */
    public int getRequirementID() {
        return requirementID;
    }

    /**
     * @param requirementID the requirementID to set
     */
    public void setRequirementID(int requirementID) {
        this.requirementID = requirementID;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
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
