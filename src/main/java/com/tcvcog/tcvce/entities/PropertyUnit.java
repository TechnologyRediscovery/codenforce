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

import com.tcvcog.tcvce.occupancy.entities.OccPermit;
import java.util.ArrayList;

/**
 *
 * @author Eric C. Darsow
 */
public class PropertyUnit {
    
    private int unitID;
    private String unitNumber;
    private String notes;
    private String otherKnownAddress;
    private boolean rental;
    private Property thisProperty;
    private ArrayList<Person> propertyUnitPeople;
    private ArrayList<OccPermit> occupancyPermitList;
    

    /**
     * @return the unitID
     */
    public int getUnitID() {
        return unitID;
    }

    /**
     * @param unitID the unitID to set
     */
    public void setUnitID(int unitID) {
        this.unitID = unitID;
    }

    /**
     * @return the unitNumber
     */
    public String getUnitNumber() {
        return unitNumber;
    }

    /**
     * @param unitNumber the unitNumber to set
     */
    public void setUnitNumber(String unitNumber) {
        this.unitNumber = unitNumber;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the propertyUnitPeople
     */
    public ArrayList<Person> getPropertyUnitPeople() {
        return propertyUnitPeople;
    }

    /**
     * @param propertyUnitPeople the propertyUnitPeople to set
     */
    public void setPropertyUnitPeople(ArrayList<Person> propertyUnitPeople) {
        this.propertyUnitPeople = propertyUnitPeople;
    }

    /**
     * @return the otherKnownAddress
     */
    public String getOtherKnownAddress() {
        return otherKnownAddress;
    }

    /**
     * @param otherKnownAddress the otherKnownAddress to set
     */
    public void setOtherKnownAddress(String otherKnownAddress) {
        this.otherKnownAddress = otherKnownAddress;
    }

    /**
     * @return the rental
     */
    public boolean isRental() {
        return rental;
    }

    /**
     * @param rental the rental to set
     */
    public void setRental(boolean rental) {
        this.rental = rental;
    }

    /**
     * @return the occupancyPermitList
     */
    public ArrayList<OccPermit> getOccupancyPermitList() {
        return occupancyPermitList;
    }

    /**
     * @param occupancyPermitList the occupancyPermitList to set
     */
    public void setOccupancyPermitList(ArrayList<OccPermit> occupancyPermitList) {
        this.occupancyPermitList = occupancyPermitList;
    }

    /**
     * @return the thisProperty
     */
    public Property getThisProperty() {
        return thisProperty;
    }

    /**
     * @param thisProperty the thisProperty to set
     */
    public void setThisProperty(Property thisProperty) {
        this.thisProperty = thisProperty;
    }
    
}
