/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author sylvia
 */
public  class       PropertyUnitPublic 
        extends     TrackedEntity
        implements  Serializable{
    
    final static String TABLE_NAME = "parcelunit";
    final static String PK_FIELD = "unitid";
    
    
    protected int unitID;
    protected int parcelKey;
    protected String unitNumber;
    
    protected LocalDate rentalIntentDateStart;
    protected LocalDate rentalIntentDateStop;
    

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
     * @return the parcelKey
     */
    public int getParcelKey() {
        return parcelKey;
    }

    /**
     * @param parcelKey the parcelKey to set
     */
    public void setParcelKey(int parcelKey) {
        this.parcelKey = parcelKey;
    }

    /**
     * @return the rentalIntentDateStop
     */
    public LocalDate getRentalIntentDateStop() {
        return rentalIntentDateStop;
    }

    /**
     * @param rentalIntentDateStop the rentalIntentDateStop to set
     */
    public void setRentalIntentDateStop(LocalDate rentalIntentDateStop) {
        this.rentalIntentDateStop = rentalIntentDateStop;
    }

    /**
     * @return the rentalIntentDateStart
     */
    public LocalDate getRentalIntentDateStart() {
        return rentalIntentDateStart;
    }

    /**
     * @param rentalIntentDateStart the rentalIntentDateStart to set
     */
    public void setRentalIntentDateStart(LocalDate rentalIntentDateStart) {
        this.rentalIntentDateStart = rentalIntentDateStart;
    }

    @Override
    public String getPKFieldName() {
        return PK_FIELD;
    }

    @Override
    public int getDBKey() {
        return unitID;
    }

    @Override
    public String getDBTableName() {
        return TABLE_NAME;
    }
    
}
