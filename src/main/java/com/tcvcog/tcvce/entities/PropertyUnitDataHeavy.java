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
import com.tcvcog.tcvce.entities.occupancy.OccPeriodPropertyUnitHeavy;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sylvia
 */
public class        PropertyUnitDataHeavy 
        extends     PropertyUnit
        implements  IFace_CredentialSigned,
                    IFace_humanListHolder{
    final static LinkedObjectSchemaEnum HUMAN_LINK_SCHEMA_ENUM = LinkedObjectSchemaEnum.ParcelUnitHuman;

    private List<OccPeriod> periodList;
    
    protected List<HumanLink> humanLinkList;
     
    
    private List<PropertyUnitChangeOrder> changeOrderList;
    
    private String credentialSignature;
    
    
    /**
     * Pre-Credential Requiring constructor
     * @param prop 
     */
    public PropertyUnitDataHeavy(PropertyUnit prop){
        if(prop != null){

            this.unitID = prop.getUnitID();
            this.parcelKey = prop.getParcelKey();
            this.unitNumber = prop.getUnitNumber();

            this.notes = prop.getNotes();

            this.rentalIntentDateStart = prop.getRentalIntentDateStart();
            this.rentalIntentDateStop = prop.getRentalIntentDateStop();
            this.rentalNotes = prop.getRentalNotes();
            this.conditionIntensityClassID = prop.getConditionIntensityClassID();
            this.lastUpdatedTS = prop.getLastUpdatedTS();
            this.source = prop.source;

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

    
    @Override
    public List<HumanLink> getHumanLinkList() {
        return humanLinkList;
    }

    @Override
    public void setHumanLinkList(List<HumanLink> hll) {
        humanLinkList = hll;
    }

    @Override
    public LinkedObjectSchemaEnum getHUMAN_LINK_SCHEMA_ENUM() {
        return HUMAN_LINK_SCHEMA_ENUM;
    }


    @Override
    public int getHostPK() {
        return parcelKey;
    }
    
    
}
