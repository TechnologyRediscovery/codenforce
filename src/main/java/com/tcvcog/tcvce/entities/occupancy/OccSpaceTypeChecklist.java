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

/**
 *
 * @author sylvia
 */
public class OccSpaceTypeChecklist extends OccSpaceType {

    protected boolean overrideSpaceTypeRequired;
    protected boolean overrideSpaceTypeRequiredValue;
    protected boolean overrideSpaceTypeRequireAllSpaces;
    
    public OccSpaceTypeChecklist(OccSpaceType type){
        this.spaceTypeID = type.getSpaceTypeID();
        this.spaceTypeTitle = type.getSpaceTypeTitle();
        this.spaceTypeDescription = type.getSpaceTypeDescription();
        this.spaceList = type.getSpaceList();
       
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
    

    
    
    
}
