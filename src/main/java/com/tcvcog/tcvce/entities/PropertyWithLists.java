/*
 * Copyright (C) 2018 Turtle Creek Valley
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

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eric C. Darsow
 */
public class PropertyWithLists extends Property implements Serializable{
    
    // cases store code enforcement data
    private List<CECase> ceCaseList;
    // property units store occupancy data
    private List<PropertyUnitWithLists> unitWithListsList;
    // both are connected to Person objects all over the place
    private List<Person> personList;
    private List<CECase> infoCaseList;
    private List<PropertyUnitChange> changeList;
    private List<Integer> blobList;
    
    public PropertyWithLists(){
        
    }

    public PropertyWithLists(Property prop){
        this.propertyID = prop.getPropertyID();
        this.muni = prop.getMuni();
        this.muniCode = prop.getMuniCode();
        this.parID = prop.getParID();
        this.lotAndBlock = prop.getLotAndBlock();
        this.address = prop.getAddress();

        this.useTypeString = prop.getUseTypeString();
        this.useGroup = prop.getUseGroup();
        this.constructionType = prop.getConstructionType();
        this.countyCode = prop.getCountyCode();

        this.notes = prop.getNotes();
        this.address_city = prop.getAddress_city();
        this.address_state = prop.getAddress_state();
        this.address_zip = prop.address_zip;

        this.ownerCode = prop.getOwnerCode();
        this.propclass = prop.getPropclass();

        this.lastUpdatedTS = prop.getLastUpdatedTS();
        this.lastUpdatedBy = prop.getLastUpdatedBy();
        this.locationDescriptor = prop.getLocationDescriptor();

        this.bobSource = prop.getBobSource();
        this.unfitDateStart = prop.getUnfitDateStart();
        this.unfitDateStop = prop.getUnfitDateStop();
        this.unfitBy = prop.getUnfitBy();

        this.abandonedDateStart = prop.getAbandonedDateStart();
        this.abandonedDateStop = prop.getAbandonedDateStop();
        this.abandonedBy = prop.getAbandonedBy();
        this.vacantDateStart = prop.getVacantDateStart();

        this.vacantDateStop = prop.getVacantDateStop();
        this.vacantBy = prop.getVacantBy();
        this.conditionIntensityClassID = prop.getConditionIntensityClassID();

        this.landBankProspectIntensityClassID = prop.getLandBankProspectIntensityClassID();
        this.LandBankHeld = prop.isLandBankHeld();
        this.active = prop.isActive();
        this.nonAddressable = prop.isNonAddressable();
        
        this.useTypeID = prop.getUseTypeID();
        this.useTypeString = prop.getUseTypeString();
        this.unitList = prop.getUnitList();
        
        
    }
    
    
    /**
     * @return the ceCaseList
     */
    public List<CECase> getCeCaseList() {
        return ceCaseList;
    }


    /**
     * @return the personList
     */
    public List<Person> getPersonList() {
        return personList;
    }

    /**
     * @param ceCaseList the ceCaseList to set
     */
    public void setCeCaseList(List<CECase> ceCaseList) {
        this.ceCaseList = ceCaseList;
    }


    /**
     * @param personList the personList to set
     */
    public void setPersonList(List<Person> personList) {
        this.personList = personList;
    }

    /**
     * @return the infoCaseList
     */
    public List getInfoCaseList() {
        return infoCaseList;
    }

    /**
     * @param infoCaseList the infoCaseList to set
     */
    public void setInfoCaseList(List<CECase> infoCaseList) {
        this.infoCaseList = infoCaseList;
    }

    /**
     * @return the changeList
     */
    public List getChangeList() {
        return changeList;
    }

    /**
     * @param changeList the changeList to set
     */
    public void setChangeList(List<PropertyUnitChange> changeList) {
        this.changeList = changeList;
    }

    /**
     * @return the blobList
     */
    public List getBlobList() {
        return blobList;
    }

    /**
     * @param blobList the blobList to set
     */
    public void setBlobList(List<Integer> blobList) {
        this.blobList = blobList;
    }

    /**
     * @return the unitWithListsList
     */
    public List<PropertyUnitWithLists> getUnitWithListsList() {
        return unitWithListsList;
    }

    /**
     * @param unitWithListsList the unitWithListsList to set
     */
    public void setUnitWithListsList(List<PropertyUnitWithLists> unitWithListsList) {
        this.unitWithListsList = unitWithListsList;
    }
    
}
