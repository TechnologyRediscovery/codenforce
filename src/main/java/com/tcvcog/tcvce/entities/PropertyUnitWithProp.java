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

/**
 *
 * @author sylvia
 */
public class PropertyUnitWithProp extends PropertyUnit {

    protected Property property;

    public PropertyUnitWithProp(PropertyUnit prop){
        if(prop != null){

            this.unitID = prop.getUnitID();
            this.propertyID = prop.getPropertyID();
            this.unitNumber = prop.getUnitNumber();

            this.notes = prop.getNotes();
            this.otherKnownAddress = prop.getOtherKnownAddress();

            this.rentalIntentDateStart = prop.getRentalIntentDateStart();
            this.rentalIntentDateStop = prop.getRentalIntentDateStop();
            this.rentalIntentLastUpdatedBy = prop.getRentalIntentLastUpdatedBy();
            this.rentalNotes = prop.getRentalNotes();
            this.active = prop.isActive();
            this.conditionIntensityClassID = prop.getConditionIntensityClassID();
            this.lastUpdatedTS = prop.getLastUpdatedTS();
        }
    }
    
    /**
     * @return the property
     */
    public Property getProperty() {
        return property;
    }

    /**
     * @param property the property to set
     */
    public void setProperty(Property property) {
        this.property = property;
    }
    
    
    
}
