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
package com.tcvcog.tcvce.entities.occupancy;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author sylvia
 */
public class OccSpaceTypeInspectionDirective extends OccSpaceType {

    protected boolean overrideSpaceTypeRequired;
    protected boolean overrideSpaceTypeRequiredValue;
    protected boolean overrideSpaceTypeRequireAllSpaces;
    protected List<OccSpace> spaceList;
    
     //xiaohong add
    protected boolean selected;

    public OccSpaceTypeInspectionDirective(OccSpaceType type){
        this.spaceTypeID = type.getSpaceTypeID();
        this.spaceTypeTitle = type.getSpaceTypeTitle();
        this.spaceTypeDescription = type.getSpaceTypeDescription();
        this.required = type.isRequired();
       
    }
    

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * @return the overrideSpaceTypeRequired
     */
    public boolean isOverrideSpaceTypeRequired() {
        return overrideSpaceTypeRequired;
    }

    /**
     * @return the overrideSpaceTypeRequiredValue
     */
    public boolean isOverrideSpaceTypeRequiredValue() {
        return overrideSpaceTypeRequiredValue;
    }

    /**
     * @return the overrideSpaceTypeRequireAllSpaces
     */
    public boolean isOverrideSpaceTypeRequireAllSpaces() {
        return overrideSpaceTypeRequireAllSpaces;
    }

    /**
     * @param overrideSpaceTypeRequired the overrideSpaceTypeRequired to set
     */
    public void setOverrideSpaceTypeRequired(boolean overrideSpaceTypeRequired) {
        this.overrideSpaceTypeRequired = overrideSpaceTypeRequired;
    }

    /**
     * @param overrideSpaceTypeRequiredValue the overrideSpaceTypeRequiredValue to set
     */
    public void setOverrideSpaceTypeRequiredValue(boolean overrideSpaceTypeRequiredValue) {
        this.overrideSpaceTypeRequiredValue = overrideSpaceTypeRequiredValue;
    }

    /**
     * @param overrideSpaceTypeRequireAllSpaces the overrideSpaceTypeRequireAllSpaces to set
     */
    public void setOverrideSpaceTypeRequireAllSpaces(boolean overrideSpaceTypeRequireAllSpaces) {
        this.overrideSpaceTypeRequireAllSpaces = overrideSpaceTypeRequireAllSpaces;
    }

    /**
     * @return the spaceList
     */
    public List<OccSpace> getSpaceList() {
        return spaceList;
    }

    /**
     * @param spaceList the spaceList to set
     */
    public void setSpaceList(List<OccSpace> spaceList) {
        this.spaceList = spaceList;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.overrideSpaceTypeRequired ? 1 : 0);
        hash = 97 * hash + (this.overrideSpaceTypeRequiredValue ? 1 : 0);
        hash = 97 * hash + (this.overrideSpaceTypeRequireAllSpaces ? 1 : 0);
        hash = 97 * hash + Objects.hashCode(this.spaceList);
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
        final OccSpaceTypeInspectionDirective other = (OccSpaceTypeInspectionDirective) obj;
        if (this.overrideSpaceTypeRequired != other.overrideSpaceTypeRequired) {
            return false;
        }
        if (this.overrideSpaceTypeRequiredValue != other.overrideSpaceTypeRequiredValue) {
            return false;
        }
        if (this.overrideSpaceTypeRequireAllSpaces != other.overrideSpaceTypeRequireAllSpaces) {
            return false;
        }
        if (!Objects.equals(this.spaceList, other.spaceList)) {
            return false;
        }
        return true;
    }
    

    
    
    
}
