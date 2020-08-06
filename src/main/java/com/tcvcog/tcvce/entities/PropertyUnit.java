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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class    PropertyUnit 
        extends PropertyUnitPublic {
    
    protected String notes;
    protected String rentalNotes;
    protected boolean active;
    
    protected int conditionIntensityClassID;
    protected LocalDateTime lastUpdatedTS;

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + this.unitID;
        hash = 43 * hash + this.propertyID;
        hash = 43 * hash + Objects.hashCode(this.unitNumber);
        hash = 43 * hash + Objects.hashCode(this.notes);
        hash = 43 * hash + Objects.hashCode(this.otherKnownAddress);
        hash = 43 * hash + Objects.hashCode(this.rentalIntentDateStart);
        hash = 43 * hash + Objects.hashCode(this.rentalIntentDateStop);
        hash = 43 * hash + Objects.hashCode(this.rentalIntentLastUpdatedBy);
        hash = 43 * hash + Objects.hashCode(this.rentalNotes);
        hash = 43 * hash + (this.active ? 1 : 0);
        hash = 43 * hash + this.conditionIntensityClassID;
        hash = 43 * hash + Objects.hashCode(this.lastUpdatedTS);
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
        final PropertyUnit other = (PropertyUnit) obj;
        if (this.unitID != other.unitID) {
            return false;
        }
        if (this.propertyID != other.propertyID) {
            return false;
        }
        if (this.active != other.active) {
            return false;
        }
        if (this.conditionIntensityClassID != other.conditionIntensityClassID) {
            return false;
        }
        if (!Objects.equals(this.unitNumber, other.unitNumber)) {
            return false;
        }
        if (!Objects.equals(this.notes, other.notes)) {
            return false;
        }
        if (!Objects.equals(this.otherKnownAddress, other.otherKnownAddress)) {
            return false;
        }
        if (!Objects.equals(this.rentalNotes, other.rentalNotes)) {
            return false;
        }
        if (!Objects.equals(this.rentalIntentDateStart, other.rentalIntentDateStart)) {
            return false;
        }
        if (!Objects.equals(this.rentalIntentDateStop, other.rentalIntentDateStop)) {
            return false;
        }
        if (!Objects.equals(this.rentalIntentLastUpdatedBy, other.rentalIntentLastUpdatedBy)) {
            return false;
        }
        if (!Objects.equals(this.lastUpdatedTS, other.lastUpdatedTS)) {
            return false;
        }
        return true;
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
     * @return the rentalIntentDateStart
     */
    public Date getRentalIntentDateStartUtil() {
        Date rentalIntentDateStartUtil = null;
        if(rentalIntentDateStart != null){
            rentalIntentDateStartUtil = Date.from(rentalIntentDateStart.atZone(ZoneId.systemDefault()).toInstant());
        }
        
        return rentalIntentDateStartUtil;
    }

    /**
     * @return the rentalIntentDateStop
     */
    public Date getRentalIntentDateStopUtil() {
        Date rentalIntentDateStopUtil = null;
        if(rentalIntentDateStop != null){
            rentalIntentDateStopUtil = Date.from(rentalIntentDateStop.atZone(ZoneId.systemDefault()).toInstant());
        }
        
        return rentalIntentDateStopUtil;
        
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
     * @param rentalIntentDateStart the rentalIntentDateStart to set
     */
    public void setRentalIntentDateStartUtil(Date rentalIntentDateStart) {
        if(rentalIntentDateStart != null){
            this.rentalIntentDateStart = rentalIntentDateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } else {
            this.rentalIntentDateStart = null;
        }
        
    }

    /**
     * @param rentalIntentDateStop the rentalIntentDateStop to set
     */
    public void setRentalIntentDateStopUtil(Date rentalIntentDateStop) {
        if(rentalIntentDateStop != null){
        this.rentalIntentDateStop = rentalIntentDateStop.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } else {
            this.rentalIntentDateStop = null;
        }
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
