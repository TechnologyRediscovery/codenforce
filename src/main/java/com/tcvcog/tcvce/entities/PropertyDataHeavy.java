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

import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author ellen bascomb
 */
public  class       PropertyDataHeavy 
        extends     Property 
        implements  IFace_CredentialSigned{
    
    
    private List<CECaseDataHeavy> ceCaseList;
    
    private List<PropertyUnitDataHeavy> unitWithListsList;
    
    private List<Person> personList;
    
    private List<CECaseDataHeavy> propInfoCaseList;
    
    private List<PropertyUnitChangeOrder> changeList;
    
    private List<Integer> blobList;
    
    private String credentialSignature;
    
    
    public PropertyDataHeavy(){
        
    }
    
    /**
     * Theoretically we store the credential signature of the user who
     * retrieves any data heavy bob, but for now, this is in holding
     * @param prop
     * @param cred 
     */
    public PropertyDataHeavy(Property prop, Credential cred){
        
        this.credentialSignature = cred.getSignature();
           
        this.propertyID = prop.getPropertyID();
        this.muni = prop.getMuni();
        this.muniCode = prop.getMuniCode();
        this.parID = prop.getParID();
        this.lotAndBlock = prop.getLotAndBlock();
        this.address = prop.getAddress();

        this.useType = prop.getUseType();      
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
        
        
        
        
    }
    

    public PropertyDataHeavy(Property prop){
        this.propertyID = prop.getPropertyID();
        this.muni = prop.getMuni();
        this.muniCode = prop.getMuniCode();
        this.parID = prop.getParID();
        this.lotAndBlock = prop.getLotAndBlock();
        this.address = prop.getAddress();

        this.useType = prop.getUseType();
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
        
        this.unitList = prop.getUnitList();
        
        
    }
    
    /**
     * @return the credentialSignature
     */
    @Override
    public String getCredentialSignature() {
        return credentialSignature;
    }

    public List<OccPeriod> assembleOccPeriodList(){
        List<OccPeriod> perList = new ArrayList<>();
        if(unitWithListsList != null && !unitWithListsList.isEmpty()){
            for(PropertyUnitDataHeavy pudh: unitWithListsList){
                if(pudh.getPeriodList() != null && !pudh.getPeriodList().isEmpty()){
                    perList.addAll(pudh.getPeriodList());
                }
            }
        }
        
        return perList;
        
    }
    
    /**
     * @return the ceCaseList
     */
    public List<CECaseDataHeavy> getCeCaseList() {
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
    public void setCeCaseList(List<CECaseDataHeavy> ceCaseList) {
        this.ceCaseList = ceCaseList;
    }


    /**
     * @param personList the personList to set
     */
    public void setPersonList(List<Person> personList) {
        this.personList = personList;
    }

    /**
     * @return the propInfoCaseList
     */
    public List getInfoCaseList() {
        return propInfoCaseList;
    }

    /**
     * @param propInfoCaseList the propInfoCaseList to set
     */
    public void setPropInfoCaseList(List<CECaseDataHeavy> propInfoCaseList) {
        this.propInfoCaseList = propInfoCaseList;
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
    public void setChangeList(List<PropertyUnitChangeOrder> changeList) {
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
    public List<PropertyUnitDataHeavy> getUnitWithListsList() {
        return unitWithListsList;
    }

    /**
     * @param unitWithListsList the unitWithListsList to set
     */
    public void setUnitWithListsList(List<PropertyUnitDataHeavy> unitWithListsList) {
        this.unitWithListsList = unitWithListsList;
    }


  
    
}
