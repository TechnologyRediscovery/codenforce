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

/**
 * Models the entity: Property Unit Change Order.Public users compile lists of
 * the units in their property while applying for occupancy.Before applying any
 * changes to our records, they await approval as one of these entities.
 *
 * @author Nathan Dietz
 */
public class PropertyUnitChangeOrder extends ChangeOrder {

    private int unitChangeID;
    private int unitID;
    private String unitNumber;
    private String otherKnownAddress;
    private String notes;
    private String rentalNotes;
    
    public PropertyUnitChangeOrder() {
    }

    public PropertyUnitChangeOrder(PropertyUnit input) {
        unitID = input.getUnitID();
        unitNumber = input.getUnitNumber();
        otherKnownAddress = input.getOtherKnownAddress();
        notes = input.getNotes();
        rentalNotes = input.getRentalNotes();
    }
    
    
    
    /**
     * This constructor compares a proposed property unit against the original property unit.
     * If there is a difference between the original and the proposed, the value of the proposed is saved.
     * @param original
     * @param proposed 
     */
    public PropertyUnitChangeOrder(PropertyUnit original, PropertyUnit proposed){
        
        unitID = proposed.getUnitID();

        //check each field for changes
        if (!compareStrings(original.getUnitNumber(), proposed.getUnitNumber())) {
            unitNumber = proposed.getUnitNumber();
        }

        if (!compareStrings(original.getOtherKnownAddress(), proposed.getOtherKnownAddress())) {
            otherKnownAddress = proposed.getOtherKnownAddress();
        }

        if (!compareStrings(original.getNotes(), proposed.getNotes())) {
            notes = proposed.getNotes();
        }

        if (!compareStrings(original.getRentalNotes(), proposed.getRentalNotes())) {
            rentalNotes = proposed.getRentalNotes();
        }
        
    }
    
     public PropertyUnit toPropertyUnit() {
        
        PropertyUnit skeleton = new PropertyUnit();
        
        skeleton.setUnitNumber(unitNumber);
        
        skeleton.setNotes(notes);
        
        skeleton.setOtherKnownAddress(otherKnownAddress);
        
        return skeleton;
        
    }
    
    /**
     * Detects if the unit has actually been changed.
     * @return 
     */
    @Override
    public boolean changedOccured() {

        if(unitNumber!=null){
           return true; 
        }
        
        if (otherKnownAddress != null) {
            return true;
        }

        if (notes != null) {
            return true;
        }
        if (rentalNotes != null) {
           return true;
        }
        //If none of the above apply, atleast check if it has been added or removed.
        
        return added || removed; 

    }
    
    public int getUnitChangeID() {
        return unitChangeID;
    }

    public void setUnitChangeID(int unitChangeID) {
        this.unitChangeID = unitChangeID;
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

    public String getRentalNotes() {
        return rentalNotes;
    }

    public void setRentalNotes(String rental) {

        rentalNotes = rental;
    }

    public String newOrRemoved(){
        
        if(removed == true)
        {
            return "Removed";
        }
        
        if(added == true) {    
            return "Added";
        }
            
            return  "Edited";
        
    }
    
}
