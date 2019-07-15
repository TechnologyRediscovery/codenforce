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

/**
 *
 * @author Eric C. Darsow
 */
public class OccInspectedSpaceElement implements Serializable{
    
    private int inspectedElementID;
    private CodeElement element;
    
    private LocalDateTime lastInspectedTS;
    private User lastInspectedBy;
    
    private LocalDateTime complianceGrantedTS;
    private User complianceGrantedBy;
    
    private boolean required;
    private User overrideRequiredFlag_eleNotInspected;
    private String notes;
    
    private OccLocationDescriptor location;

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
     * @return the overrideRequiredFlag_eleNotInspected
     */
    public User getOverrideRequiredFlag_eleNotInspected() {
        return overrideRequiredFlag_eleNotInspected;
    }

    /**
     * @param required the required to set
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @param overrideRequiredFlag_eleNotInspected the overrideRequiredFlag_eleNotInspected to set
     */
    public void setOverrideRequiredFlag_eleNotInspected(User overrideRequiredFlag_eleNotInspected) {
        this.overrideRequiredFlag_eleNotInspected = overrideRequiredFlag_eleNotInspected;
    }
}