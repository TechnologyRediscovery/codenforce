/*
 * Copyright (C) 2018 Adam Gutonski
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

import com.tcvcog.tcvce.entities.Municipality;


/**
 *
 * @author Adam Gutonski
 */
public class OccPermitType {
    
    private int occupancyPermitTypeID;
    private Municipality muni;
    private String occupancyPermitTypeName;
    private String occupancyPermitTypeDescription;

    /**
     * @return the occupancyPermitTypeID
     */
    public int getOccupancyPermitTypeID() {
        return occupancyPermitTypeID;
    }

    /**
     * @param occupancyPermitTypeID the occupancyPermitTypeID to set
     */
    public void setOccupancyPermitTypeID(int occupancyPermitTypeID) {
        this.occupancyPermitTypeID = occupancyPermitTypeID;
    }

    /**
     * @return the muniCodeID
     */
    public Municipality getMuni() {
        return muni;
    }

    /**
     * @param occupancyPermitTypeMuniCodeID the muniCodeID to set
     */
    public void setMuni(Municipality occupancyPermitTypeMuniCodeID) {
        this.muni = occupancyPermitTypeMuniCodeID;
    }

    /**
     * @return the occupancyPermitTypeName
     */
    public String getOccupancyPermitTypeName() {
        return occupancyPermitTypeName;
    }

    /**
     * @param occupancyPermitTypeName the occupancyPermitTypeName to set
     */
    public void setOccupancyPermitTypeName(String occupancyPermitTypeName) {
        this.occupancyPermitTypeName = occupancyPermitTypeName;
    }

    /**
     * @return the occupancyPermitTypeDescription
     */
    public String getOccupancyPermitTypeDescription() {
        return occupancyPermitTypeDescription;
    }

    /**
     * @param occupancyPermitTypeDescription the occupancyPermitTypeDescription to set
     */
    public void setOccupancyPermitTypeDescription(String occupancyPermitTypeDescription) {
        this.occupancyPermitTypeDescription = occupancyPermitTypeDescription;
    }
    
}
