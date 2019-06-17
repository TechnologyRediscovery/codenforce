/*
 * Copyright (C) 2017 Turtle Creek Valley
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
package com.tcvcog.tcvce.entities;

import java.time.LocalDate;

/**
 * Models the entity: Property Unit Change Order.Public users compile lists of
 * the units in their property while applying for occupancy.Before applying any
 * changes to our records, they await approval as one of these entities.
 *
 * @author Nathan Dietz
 */
public class PropertyUnitChange {

    private int unitChangeID;
    private int changedBy; //personID of the person who changed the unit.
    private int unitID;
    private int approvedBy; //personID of the user that approved the change (a backend operation)
    private String unitNumber;
    private String otherKnownAddress;
    private String notes;
    private boolean rental;
    private boolean boolChanged; //this stores if the rental variable was changed, not whether or not the unit is a rental.
    private boolean removed;
    private boolean added;
    private java.time.LocalDate changedOn;
    private java.time.LocalDate approvedOn; //If null, it has not been approved
    private PropertyUnit thisUnit;

    public int getUnitChangeID() {
        return unitChangeID;
    }

    public void setUnitChangeID(int unitChangeID) {
        this.unitChangeID = unitChangeID;
    }

    public int getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(int changedBy) {
        this.changedBy = changedBy;
    }

    public int getUnitID() {
        return unitID;
    }

    public void setUnitID(int unitID) {
        this.unitID = unitID;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public void setUnitNumber(String unitNumber) {
        this.unitNumber = unitNumber;
    }

    public int getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(int approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getOtherKnownAddress() {
        return otherKnownAddress;
    }

    public void setOtherKnownAddress(String otherKnownAddress) {
        this.otherKnownAddress = otherKnownAddress;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isRental() {
        return rental;
    }

    public void setRental(boolean rental) {

        boolChanged = true;
        this.rental = rental;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public LocalDate getChangedOn() {
        return changedOn;
    }

    public void setChangedOn(LocalDate changedOn) {
        this.changedOn = changedOn;
    }

    public LocalDate getApprovedOn() {
        return approvedOn;
    }

    public void setApprovedOn(LocalDate approvedOn) {
        this.approvedOn = approvedOn;
    }

    public boolean isBoolChanged() {
        return boolChanged;
    }

    public boolean isAdded() {
        return added;
    }

    public void setAdded(boolean added) {
        
        this.added = added;
    }

    
    
    /**
     * USE SPARINGLY.This variable is already managed by setRental.
     *
     * @param boolChanged
     */
    public void setBoolChanged(boolean boolChanged) {
        this.boolChanged = boolChanged;
    }
    
    /**
     * Detects if the unit has actually been changed.
     * @return 
     */
    public boolean changedOccured() {

        boolean temp = false;

        if (otherKnownAddress != null) {

            temp = true;
        }

        if (notes != null) {

            temp = true;
        }

        if (boolChanged == true) {

            temp = true;

        }
        
        return temp;

    }

    public PropertyUnit getThisUnit() {
        return thisUnit;
    }

    public void setThisUnit(PropertyUnit thisUnit) {
        this.thisUnit = thisUnit;
    }
    
    
    
}
