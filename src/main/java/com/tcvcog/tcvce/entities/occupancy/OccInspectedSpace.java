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

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.entities.User;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Subclass of Space: stores inspection-specific data about each space element
 * that is part of the super class. When a space is inspected, the ArrayList of CodeElements
 * in the Space are wrapped in an inspection blanket and added to the 
 * inspectedElementList which captures the compliance status, comp date, and notes.
 * 
 * @author Eric C. Darsow, Technology Rediscovery LLC 
 */
public class OccInspectedSpace extends OccSpace implements Serializable{
    
    private List<OccInspectedCodeElement> inspectedElementList;
    private OccLocationDescriptor location;
    private OccSpaceType type;
    
    private User lastInspectedBy;
    private LocalDateTime lastInspectedTS;
    
    public OccInspectedSpace(OccSpace spc){
        this.spaceid = spc.getSpaceid();
        this.occSpaceTypeID = spc.getOccSpaceTypeID();
        this.name = spc.getName();
        this.required = spc.isRequired();
        this.elementList = spc.getElementList();
        
        inspectedElementList = new ArrayList<>();
    }
    
    public void addElementToInspectedList(OccInspectedCodeElement ele){
        inspectedElementList.add(ele);
        
    }

    /**
     * @return the inspectedElementList
     */
    public List<OccInspectedCodeElement> getInspectedElementList() {
        return inspectedElementList;
    }

    /**
     * @param inspectedElementList the inspectedElementList to set
     */
    public void setInspectedElementList(List<OccInspectedCodeElement> inspectedElementList) {
        this.inspectedElementList = inspectedElementList;
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
     * @return the spaceType
     */
    public OccSpaceType getSpaceType() {
        return type;
    }

  

    /**
     * @param spaceType the spaceType to set
     */
    public void setSpaceType(OccSpaceType spaceType) {
        this.type = spaceType;
    }

   

    /**
     * @return the type
     */
    public OccSpaceType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(OccSpaceType type) {
        this.type = type;
    }

    /**
     * @return the lastInspectedBy
     */
    public User getLastInspectedBy() {
        return lastInspectedBy;
    }

    /**
     * @return the lastInspectedTS
     */
    public LocalDateTime getLastInspectedTS() {
        return lastInspectedTS;
    }

    /**
     * @param lastInspectedBy the lastInspectedBy to set
     */
    public void setLastInspectedBy(User lastInspectedBy) {
        this.lastInspectedBy = lastInspectedBy;
    }

    /**
     * @param lastInspectedTS the lastInspectedTS to set
     */
    public void setLastInspectedTS(LocalDateTime lastInspectedTS) {
        this.lastInspectedTS = lastInspectedTS;
    }
    
}
