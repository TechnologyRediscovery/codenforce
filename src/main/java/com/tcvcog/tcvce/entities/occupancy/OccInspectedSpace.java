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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Subclass of Space: stores inspection-specific data about each space element
 * that is part of the super class. When a space is inspected, the ArrayList of CodeElements
 * in the Space are wrapped in an inspection blanket and added to the 
 * inspectedElementList which captures the compliance status, comp date, and notes.
 * 
 * @author Eric C. Darsow, Technology Rediscovery LLC 
 */
public class OccInspectedSpace extends OccSpace implements Serializable, Cloneable{

    
    private int inspectedSpaceID;
    private List<OccInspectedSpaceElement> inspectedElementList;
    private List<OccInspectedSpaceElement> visibleInspectedElementList;
    private OccLocationDescriptor location;
    private OccSpaceType type;
    
    private User lastInspectedBy;
    private LocalDateTime lastInspectedTS;
    
    private OccInspectableStatus status;
    
    
    public OccInspectedSpace(OccSpace spc){
        this.spaceID = spc.getSpaceID();
        this.occSpaceTypeID = spc.getOccSpaceTypeID();
        this.name = spc.getName();
        this.required = spc.isRequired();
        
        visibleInspectedElementList = new ArrayList<>();
    }
    
    /**
     *
     * @return
     */
    @Override
    public Object clone() {
        
        try { 
            OccInspectedSpace ois = (OccInspectedSpace) super.clone();
            return ois;
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }
    
    public List<CodeElement> getInspectedCodeElementsWithoutShell(){
        List<CodeElement> eleList = new ArrayList<>();
        if(inspectedElementList != null){
            Iterator<OccInspectedSpaceElement> iter = inspectedElementList.iterator();
            while(iter.hasNext()){
                eleList.add(iter.next());
            }
        }
        return eleList;
    }
    

    /**
     * @return the inspectedElementList
     */
    public List<OccInspectedSpaceElement> getInspectedElementList() {
        return inspectedElementList;
    }

    /**
     * @param inspectedElementList the inspectedElementList to set
     */
    public void setInspectedElementList(List<OccInspectedSpaceElement> inspectedElementList) {
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.inspectedElementList);
        hash = 53 * hash + Objects.hashCode(this.location);
        hash = 53 * hash + Objects.hashCode(this.type);
        hash = 53 * hash + Objects.hashCode(this.lastInspectedBy);
        hash = 53 * hash + Objects.hashCode(this.lastInspectedTS);
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
        final OccInspectedSpace other = (OccInspectedSpace) obj;
        if (!Objects.equals(this.inspectedElementList, other.inspectedElementList)) {
            return false;
        }
        if (!Objects.equals(this.location, other.location)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.lastInspectedBy, other.lastInspectedBy)) {
            return false;
        }
        if (!Objects.equals(this.lastInspectedTS, other.lastInspectedTS)) {
            return false;
        }
        return true;
    }

    /**
     * @return the inspectedSpaceID
     */
    public int getInspectedSpaceID() {
        return inspectedSpaceID;
    }

    /**
     * @param inspectedSpaceID the inspectedSpaceID to set
     */
    public void setInspectedSpaceID(int inspectedSpaceID) {
        this.inspectedSpaceID = inspectedSpaceID;
    }

    

    /**
     * @return the status
     */
    public OccInspectableStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(OccInspectableStatus status) {
        this.status = status;
    }

    /**
     * @return the visibleInspectedElementList
     */
    public List<OccInspectedSpaceElement> getVisibleInspectedElementList() {
        return visibleInspectedElementList;
    }

    /**
     * @param visibleInspectedElementList the visibleInspectedElementList to set
     */
    public void setVisibleInspectedElementList(List<OccInspectedSpaceElement> visibleInspectedElementList) {
        this.visibleInspectedElementList = visibleInspectedElementList;
    }

   
    
}
