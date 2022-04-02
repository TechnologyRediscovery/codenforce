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

import com.tcvcog.tcvce.entities.occupancy.OccLocationDescriptor;
import com.tcvcog.tcvce.util.DateTimeUtil;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

/**
 * Roughly maps to a parcelunit record in the DB
 * @author ellen bascomb of apt 31y
 */
public class    PropertyUnit 
        extends PropertyUnitPublic {
    
    protected String notes;
    protected String rentalNotes;
    
    protected int conditionIntensityClassID;
    
    protected MailingAddress parcelMailing;
    protected BOBSource source;
    protected OccLocationDescriptor locationDescriptor;

    public PropertyUnit() {
    }

    public PropertyUnit(PropertyUnit input){
        unitID = input.getUnitID();
        parcelKey = input.getParcelKey();
        unitNumber = input.getUnitNumber();
        rentalIntentDateStart = input.rentalIntentDateStart;
        rentalIntentDateStop = input.rentalIntentDateStop;
        notes = input.getNotes();
        rentalNotes = input.getRentalNotes();
        conditionIntensityClassID = input.getConditionIntensityClassID();
        lastUpdatedTS = input.lastUpdatedTS;
        parcelMailing = input.parcelMailing;
        source = input.source;
        locationDescriptor = input.locationDescriptor;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + this.unitID;
        hash = 43 * hash + this.parcelKey;
        hash = 43 * hash + Objects.hashCode(this.unitNumber);
        hash = 43 * hash + Objects.hashCode(this.notes);
        hash = 43 * hash + Objects.hashCode(this.getRentalIntentDateStart());
        hash = 43 * hash + Objects.hashCode(this.getRentalIntentDateStop());
        hash = 43 * hash + Objects.hashCode(this.rentalNotes);
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
     * @return the rentalNotes
     */
    public String getRentalNotes() {
        return rentalNotes;
    }

 
    /**
     * @return the conditionIntensityClassID
     */
    public int getConditionIntensityClassID() {
        return conditionIntensityClassID;
    }

   


    /**
     * @param rentalNotes the rentalNotes to set
     */
    public void setRentalNotes(String rentalNotes) {
        this.rentalNotes = rentalNotes;
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
     * @return the parcelMailing
     */
    public MailingAddress getParcelMailing() {
        return parcelMailing;
    }

    /**
     * @param parcelMailing the parcelMailing to set
     */
    public void setParcelMailing(MailingAddress parcelMailing) {
        this.parcelMailing = parcelMailing;
    }

    /**
     * @return the source
     */
    public BOBSource getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(BOBSource source) {
        this.source = source;
    }

    /**
     * @return the locationDescriptor
     */
    public OccLocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    /**
     * @param locationDescriptor the locationDescriptor to set
     */
    public void setLocationDescriptor(OccLocationDescriptor locationDescriptor) {
        this.locationDescriptor = locationDescriptor;
    }

}