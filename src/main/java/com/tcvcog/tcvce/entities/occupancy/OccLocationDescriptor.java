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

import java.util.Objects;

/**
 *
 * @author sylvia
 */
public class OccLocationDescriptor {
    private int locationID;
    private String locationDescription;
    private int buildingFloorNo;

    /**
     * @return the locationID
     */
    public int getLocationID() {
        return locationID;
    }

    /**
     * @return the locationDescription
     */
    public String getLocationDescription() {
        return locationDescription;
    }

    /**
     * @param locationID the locationID to set
     */
    public void setLocationID(int locationID) {
        this.locationID = locationID;
    }

    /**
     * @param locationDescription the locationDescription to set
     */
    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

   

    /**
     * @return the buildingFloorNo
     */
    public int getBuildingFloorNo() {
        return buildingFloorNo;
    }

    /**
     * @param buildingFloorNo the buildingFloorNo to set
     */
    public void setBuildingFloorNo(int buildingFloorNo) {
        this.buildingFloorNo = buildingFloorNo;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.locationID;
        hash = 97 * hash + Objects.hashCode(this.locationDescription);
        hash = 97 * hash + this.buildingFloorNo;
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
        final OccLocationDescriptor other = (OccLocationDescriptor) obj;
        if (this.locationID != other.locationID) {
            return false;
        }
        if (this.buildingFloorNo != other.buildingFloorNo) {
            return false;
        }
        if (!Objects.equals(this.locationDescription, other.locationDescription)) {
            return false;
        }
        return true;
    }
    
    
    
}
