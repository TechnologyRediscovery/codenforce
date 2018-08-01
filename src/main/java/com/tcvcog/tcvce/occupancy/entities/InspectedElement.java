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
package com.tcvcog.tcvce.occupancy.entities;

import com.tcvcog.tcvce.entities.CodeElement;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @author Eric C. Darsow
 */
public class InspectedElement implements Serializable{
    private int id;
    private boolean inspected;
    private CodeElement element;
    private LocalDateTime complianceDate;
    private String notes;
    private LocationDescriptor location;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the complianceDate
     */
    public LocalDateTime getComplianceDate() {
        return complianceDate;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @param complianceDate the complianceDate to set
     */
    public void setComplianceDate(LocalDateTime complianceDate) {
        this.complianceDate = complianceDate;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the inspected
     */
    public boolean isInspected() {
        return inspected;
    }

    /**
     * @param inspected the inspected to set
     */
    public void setInspected(boolean inspected) {
        this.inspected = inspected;
    }

    /**
     * @return the element
     */
    public CodeElement getElement() {
        return element;
    }

    /**
     * @param element the element to set
     */
    public void setElement(CodeElement element) {
        this.element = element;
    }

    /**
     * @return the location
     */
    public LocationDescriptor getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(LocationDescriptor location) {
        this.location = location;
    }
}
