/*
 * Copyright (C) 2018 Technology Rediscovery, LLC.
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
package com.tcvcog.tcvce.entities;

/**
 *
 * @author Nathan Dietz
 */
public class MoneyOccPeriodFeeAssigned extends FeeAssigned {
   
   private int OccPerAssignedFeeID;
   private int occPeriodID;
   private int occPeriodTypeID;
   
   public MoneyOccPeriodFeeAssigned() {
    
}

    public int getOccPeriodID() {
        return occPeriodID;
    }

    public void setOccPeriodID(int occPeriodID) {
        this.occPeriodID = occPeriodID;
    }

    public int getOccPeriodTypeID() {
        return occPeriodTypeID;
    }

    public void setOccPeriodTypeID(int occPeriodTypeID) {
        this.occPeriodTypeID = occPeriodTypeID;
    }

    public int getOccPerAssignedFeeID() {
        return OccPerAssignedFeeID;
    }

    public void setOccPerAssignedFeeID(int OccPerAssignedFeeID) {
        this.OccPerAssignedFeeID = OccPerAssignedFeeID;
    }

    
   
}
