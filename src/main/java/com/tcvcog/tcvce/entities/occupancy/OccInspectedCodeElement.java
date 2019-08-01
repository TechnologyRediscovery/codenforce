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
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.User;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 *
 * @author Eric C. Darsow
 */
public class OccInspectedCodeElement implements Serializable{
    
    private int inspectedElementID;
    private CodeElement element;
    
    private LocalDateTime lastInspectedTS;
    private User lastInspectedBy;
    
    private LocalDateTime complianceGrantedTS;
    private User complianceGrantedBy;
    
    private boolean required;
    private User overrideRequiredFlag_thisElementNotInspectedBy;
    private String notes;
    
    private OccLocationDescriptor location;
    
    private int failureIntensityClassID;

    /**
     * @return the inspectedElementID
     */
    public int getInspectedElementID() {
        return inspectedElementID;
    }

    /**
     * @return the complianceGrantedTS
     */
    public LocalDateTime getComplianceGrantedTS() {
        return complianceGrantedTS;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param inspectedElementID the inspectedElementID to set
     */
    public void setInspectedElementID(int inspectedElementID) {
        this.inspectedElementID = inspectedElementID;
    }

    /**
     * @param complianceGrantedTS the complianceGrantedTS to set
     */
    public void setComplianceGrantedTS(LocalDateTime complianceGrantedTS) {
        this.complianceGrantedTS = complianceGrantedTS;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
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
     * @return the lastInspectedTS
     */
    public LocalDateTime getLastInspectedTS() {
        return lastInspectedTS;
    }

    /**
     * @return the lastInspectedBy
     */
    public User getLastInspectedBy() {
        return lastInspectedBy;
    }

    /**
     * @return the complianceGrantedBy
     */
    public User getComplianceGrantedBy() {
        return complianceGrantedBy;
    }

    /**
     * @param lastInspectedTS the lastInspectedTS to set
     */
    public void setLastInspectedTS(LocalDateTime lastInspectedTS) {
        this.lastInspectedTS = lastInspectedTS;
    }

    /**
     * @param lastInspectedBy the lastInspectedBy to set
     */
    public void setLastInspectedBy(User lastInspectedBy) {
        this.lastInspectedBy = lastInspectedBy;
    }

    /**
     * @param complianceGrantedBy the complianceGrantedBy to set
     */
    public void setComplianceGrantedBy(User complianceGrantedBy) {
        this.complianceGrantedBy = complianceGrantedBy;
    }

    /**
     * @return the location
     */
    public OccLocationDescriptor getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(OccLocationDescriptor location) {
        this.location = location;
    }

    /**
     * @return the required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @return the overrideRequiredFlag_thisElementNotInspectedBy
     */
    public User getOverrideRequiredFlag_thisElementNotInspectedBy() {
        return overrideRequiredFlag_thisElementNotInspectedBy;
    }

    /**
     * @param required the required to set
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @param overrideRequiredFlag_thisElementNotInspectedBy the overrideRequiredFlag_thisElementNotInspectedBy to set
     */
    public void setOverrideRequiredFlag_thisElementNotInspectedBy(User overrideRequiredFlag_thisElementNotInspectedBy) {
        this.overrideRequiredFlag_thisElementNotInspectedBy = overrideRequiredFlag_thisElementNotInspectedBy;
    }

    /**
     * @return the failureIntensityClassID
     */
    public int getFailureIntensityClassID() {
        return failureIntensityClassID;
    }

    /**
     * @param failureIntensityClassID the failureIntensityClassID to set
     */
    public void setFailureIntensityClassID(int failureIntensityClassID) {
        this.failureIntensityClassID = failureIntensityClassID;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.inspectedElementID;
        hash = 97 * hash + Objects.hashCode(this.element);
        hash = 97 * hash + Objects.hashCode(this.lastInspectedTS);
        hash = 97 * hash + Objects.hashCode(this.lastInspectedBy);
        hash = 97 * hash + Objects.hashCode(this.complianceGrantedTS);
        hash = 97 * hash + Objects.hashCode(this.complianceGrantedBy);
        hash = 97 * hash + (this.required ? 1 : 0);
        hash = 97 * hash + Objects.hashCode(this.overrideRequiredFlag_thisElementNotInspectedBy);
        hash = 97 * hash + Objects.hashCode(this.notes);
        hash = 97 * hash + Objects.hashCode(this.location);
        hash = 97 * hash + this.failureIntensityClassID;
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
        final OccInspectedCodeElement other = (OccInspectedCodeElement) obj;
        if (this.inspectedElementID != other.inspectedElementID) {
            return false;
        }
        if (this.required != other.required) {
            return false;
        }
        if (this.failureIntensityClassID != other.failureIntensityClassID) {
            return false;
        }
        if (!Objects.equals(this.notes, other.notes)) {
            return false;
        }
        if (!Objects.equals(this.element, other.element)) {
            return false;
        }
        if (!Objects.equals(this.lastInspectedTS, other.lastInspectedTS)) {
            return false;
        }
        if (!Objects.equals(this.lastInspectedBy, other.lastInspectedBy)) {
            return false;
        }
        if (!Objects.equals(this.complianceGrantedTS, other.complianceGrantedTS)) {
            return false;
        }
        if (!Objects.equals(this.complianceGrantedBy, other.complianceGrantedBy)) {
            return false;
        }
        if (!Objects.equals(this.overrideRequiredFlag_thisElementNotInspectedBy, other.overrideRequiredFlag_thisElementNotInspectedBy)) {
            return false;
        }
        if (!Objects.equals(this.location, other.location)) {
            return false;
        }
        return true;
    }
    
    
    
}
