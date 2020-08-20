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

import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sylvia
 */
public class        PropertyUnitDataHeavy 
        extends     PropertyUnit
        implements  IFace_CredentialSigned{

    private List<OccPeriod> periodList;
    
    private List<PropertyUnitChangeOrder> changeOrderList;
    
    private String credentialSignature;
    
    
    /**
     * Pre-Credential Requiring constructor
     * @param prop 
     */
    public PropertyUnitDataHeavy(PropertyUnit prop){
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

    public PropertyUnitDataHeavy() {
        periodList = new ArrayList<>();
    }
    
    
    @Override
    public String getCredentialSignature() {
        return credentialSignature;
    }


    /**
     * @return the periodList
     */
    public List<OccPeriod> getPeriodList() {
        return periodList;
    }

    /**
     * @param periodList the periodList to set
     */
    public void setPeriodList(List<OccPeriod> periodList) {
        this.periodList = periodList;
    }

    /**
     * @param credentialSignature the credentialSignature to set
     */
    public void setCredentialSignature(String credentialSignature) {
        this.credentialSignature = credentialSignature;
    }

    public List<PropertyUnitChangeOrder> getChangeOrderList() {
        return changeOrderList;
    }

    public void setChangeOrderList(List<PropertyUnitChangeOrder> changeOrderList) {
        this.changeOrderList = changeOrderList;
    }

}
