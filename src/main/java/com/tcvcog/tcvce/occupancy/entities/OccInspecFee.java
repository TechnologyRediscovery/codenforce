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
import java.time.LocalDateTime;

/**
 *
 * @author Adam Gutonski
 */
public class OccInspecFee {
    private int occupancyInspectionFeeID;
    private Municipality muni;
    private String occupancyInspectionFeeName;
    private double occupancyInspectionFeeAmount;
    private LocalDateTime occupancyInspectionFeeEffDate;
    private LocalDateTime occupancyInspectionFeeExpDate;
    private String occupancyInspectionFeeNotes;

    /**
     * @return the occupancyInspectionFeeID
     */
    public int getOccupancyInspectionFeeID() {
        return occupancyInspectionFeeID;
    }

    /**
     * @param occupancyInspectionFeeID the occupancyInspectionFeeID to set
     */
    public void setOccupancyInspectionFeeID(int occupancyInspectionFeeID) {
        this.occupancyInspectionFeeID = occupancyInspectionFeeID;
    }

    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    /**
     * @return the occupancyInspectionFeeName
     */
    public String getOccupancyInspectionFeeName() {
        return occupancyInspectionFeeName;
    }

    /**
     * @param occupancyInspectionFeeName the occupancyInspectionFeeName to set
     */
    public void setOccupancyInspectionFeeName(String occupancyInspectionFeeName) {
        this.occupancyInspectionFeeName = occupancyInspectionFeeName;
    }

    /**
     * @return the occupancyInspectionFeeAmount
     */
    public double getOccupancyInspectionFeeAmount() {
        return occupancyInspectionFeeAmount;
    }

    /**
     * @param occupancyInspectionFeeAmount the occupancyInspectionFeeAmount to set
     */
    public void setOccupancyInspectionFeeAmount(double occupancyInspectionFeeAmount) {
        this.occupancyInspectionFeeAmount = occupancyInspectionFeeAmount;
    }

    /**
     * @return the occupancyInspectionFeeEffDate
     */
    public LocalDateTime getOccupancyInspectionFeeEffDate() {
        return occupancyInspectionFeeEffDate;
    }

    /**
     * @param occupancyInspectionFeeEffDate the occupancyInspectionFeeEffDate to set
     */
    public void setOccupancyInspectionFeeEffDate(LocalDateTime occupancyInspectionFeeEffDate) {
        this.occupancyInspectionFeeEffDate = occupancyInspectionFeeEffDate;
    }

    /**
     * @return the occupancyInspectionFeeExpDate
     */
    public LocalDateTime getOccupancyInspectionFeeExpDate() {
        return occupancyInspectionFeeExpDate;
    }

    /**
     * @param occupancyInspectionFeeExpDate the occupancyInspectionFeeExpDate to set
     */
    public void setOccupancyInspectionFeeExpDate(LocalDateTime occupancyInspectionFeeExpDate) {
        this.occupancyInspectionFeeExpDate = occupancyInspectionFeeExpDate;
    }

    /**
     * @return the occupancyInspectionFeeNotes
     */
    public String getOccupancyInspectionFeeNotes() {
        return occupancyInspectionFeeNotes;
    }

    /**
     * @param occupancyInspectionFeeNotes the occupancyInspectionFeeNotes to set
     */
    public void setOccupancyInspectionFeeNotes(String occupancyInspectionFeeNotes) {
        this.occupancyInspectionFeeNotes = occupancyInspectionFeeNotes;
    }
}
