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

import com.tcvcog.tcvce.entities.EntityUtils;
import java.io.Serializable;
import java.util.List;

/**
 * Mapped to fields of the spacetype table
 * @author EC Darsow
 */
public class OccSpaceType 
        extends EntityUtils 
        implements Serializable{
    protected int spaceTypeID;
    protected String spaceTypeTitle;
    protected String spaceTypeDescription;
    
    public OccSpaceType(OccSpaceType ost){
        this.spaceTypeID = ost.spaceTypeID;
        this.spaceTypeTitle = ost.spaceTypeTitle;
        this.spaceTypeDescription = ost.spaceTypeDescription;
    }
    
    public OccSpaceType(){
        
    }

    /**
     * @return the spaceTypeID
     */
    public int getSpaceTypeID() {
        return spaceTypeID;
    }

    /**
     * @param spaceTypeID the spaceTypeID to set
     */
    public void setSpaceTypeID(int spaceTypeID) {
        this.spaceTypeID = spaceTypeID;
    }

    /**
     * @return the spaceTypeTitle
     */
    public String getSpaceTypeTitle() {
        return spaceTypeTitle;
    }

    /**
     * @param spaceTypeTitle the spaceTypeTitle to set
     */
    public void setSpaceTypeTitle(String spaceTypeTitle) {
        this.spaceTypeTitle = spaceTypeTitle;
    }

    /**
     * @return the spaceTypeDescription
     */
    public String getSpaceTypeDescription() {
        return spaceTypeDescription;
    }

    /**
     * @param spaceTypeDescription the spaceTypeDescription to set
     */
    public void setSpaceTypeDescription(String spaceTypeDescription) {
        this.spaceTypeDescription = spaceTypeDescription;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + this.spaceTypeID;
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
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
        final OccSpaceType other = (OccSpaceType) obj;
        if (this.spaceTypeID != other.spaceTypeID) {
            return false;
        }
        return true;
    }

  
}
