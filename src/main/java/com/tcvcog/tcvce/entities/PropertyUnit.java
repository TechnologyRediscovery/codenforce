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

import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPermit;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplication;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eric C. Darsow
 */
public class PropertyUnit {
    protected int unitID;
    protected int propertyID;
    protected String unitNumber;
    
    protected String notes;
    protected String otherKnownAddress;
    
    protected LocalDateTime rentalIntentDateStart;
    protected LocalDateTime rentalIntentDateStop;
    protected User rentalIntentLastUpdatedBy;
    protected String rentalNotes;
    protected boolean active;
    
    protected int conditionIntensityClassID;
    protected LocalDateTime lastUpdatedTS;
    

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
  
   
    /**
     * @return the rentalIntentDateStart
     */
    public LocalDateTime getRentalIntentDateStart() {
        return rentalIntentDateStart;
    }

    /**
     * @return the rentalIntentDateStop
     */
    public LocalDateTime getRentalIntentDateStop() {
        return rentalIntentDateStop;
    }

    /**
     * @return the rentalIntentLastUpdatedBy
     */
    public User getRentalIntentLastUpdatedBy() {
        return rentalIntentLastUpdatedBy;
    }

    /**
     * @return the rentalNotes
     */
    public String getRentalNotes() {
        return rentalNotes;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @return the conditionIntensityClassID
     */
    public int getConditionIntensityClassID() {
        return conditionIntensityClassID;
    }

    /**
     * @param rentalIntentDateStart the rentalIntentDateStart to set
     */
    public void setRentalIntentDateStart(LocalDateTime rentalIntentDateStart) {
        this.rentalIntentDateStart = rentalIntentDateStart;
    }

    /**
     * @param rentalIntentDateStop the rentalIntentDateStop to set
     */
    public void setRentalIntentDateStop(LocalDateTime rentalIntentDateStop) {
        this.rentalIntentDateStop = rentalIntentDateStop;
    }

    /**
     * @param rentalIntentLastUpdatedBy the rentalIntentLastUpdatedBy to set
     */
    public void setRentalIntentLastUpdatedBy(User rentalIntentLastUpdatedBy) {
        this.rentalIntentLastUpdatedBy = rentalIntentLastUpdatedBy;
    }

    /**
     * @param rentalNotes the rentalNotes to set
     */
    public void setRentalNotes(String rentalNotes) {
        this.rentalNotes = rentalNotes;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @param conditionIntensityClassID the conditionIntensityClassID to set
     */
    public void setConditionIntensityClassID(int conditionIntensityClassID) {
        this.conditionIntensityClassID = conditionIntensityClassID;
    }

    /**
     * @return the propertyID
     */
    public int getPropertyID() {
        return propertyID;
    }

    /**
     * @param propertyID the propertyID to set
     */
    public void setPropertyID(int propertyID) {
        this.propertyID = propertyID;
    }

    /**
     * @return the lastUpdatedTS
     */
    public LocalDateTime getLastUpdatedTS() {
        return lastUpdatedTS;
    }

    /**
     * @param lastUpdatedTS the lastUpdatedTS to set
     */
    public void setLastUpdatedTS(LocalDateTime lastUpdatedTS) {
        this.lastUpdatedTS = lastUpdatedTS;
    }
    /**
     * @return the periodList
     */
    /**
     * @param periodList the periodList to set
     */
 
    
    
}
