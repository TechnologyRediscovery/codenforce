/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.entities.occupancy.OccLocationDescriptor;
import com.tcvcog.tcvce.entities.occupancy.OccPermit;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Foundational entity for the system: Property
 * @author Eric Darsow
 */

public class Property implements Serializable{
    
    
    protected int propertyID;
    
    private PropertyStatus status;
    
    protected List<PropertyUnit> unitList;
    
    protected Municipality muni;
    protected int muniCode;
    protected String parID;
    protected String lotAndBlock;
    protected String address;
    
    protected String useGroup;
    protected String constructionType;
    protected String countyCode;
    
    protected String notes;
    protected String address_city;
    protected String address_state;
    protected String address_zip;
    
    protected String ownerCode;
    protected String propclass;
  
    protected LocalDateTime lastUpdatedTS;
    protected User lastUpdatedBy;
    protected OccLocationDescriptor  locationDescriptor;
    
    protected BOBSource bobSource;
    protected LocalDateTime unfitDateStart;
    protected LocalDateTime unfitDateStop;
    protected User unfitBy;
    
    protected LocalDateTime abandonedDateStart;
    protected LocalDateTime abandonedDateStop;
    protected User abandonedBy;
    protected LocalDateTime vacantDateStart;
    
    protected LocalDateTime vacantDateStop;
    protected User vacantBy;
    protected int conditionIntensityClassID;
    
    protected int landBankProspectIntensityClassID;
    protected boolean LandBankHeld;
    protected boolean active;
    protected boolean nonAddressable;
    
    // until we have an object
    protected int  useTypeID;
    protected String useTypeString;
    
    
    /**
     * Creates a new instance of Property
     */
    public Property() {
      
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
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    /**
     * @return the parID
     */
    public String getParID() {
        return parID;
    }

    /**
     * @param parID the parID to set
     */
    public void setParID(String parID) {
        this.parID = parID;
    }

    /**
     * @return the lotAndBlock
     */
    public String getLotAndBlock() {
        return lotAndBlock;
    }

    /**
     * @param lotAndBlock the lotAndBlock to set
     */
    public void setLotAndBlock(String lotAndBlock) {
        this.lotAndBlock = lotAndBlock;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the useTypeString
     */
    public String getUseTypeString() {
        return useTypeString;
    }

    /**
     * @param useTypeString the useTypeString to set
     */
    public void setUseTypeString(String useTypeString) {
        this.useTypeString = useTypeString;
    }

    /**
     * @return the useGroup
     */
    public String getUseGroup() {
        return useGroup;
    }

    /**
     * @param useGroup the useGroup to set
     */
    public void setUseGroup(String useGroup) {
        this.useGroup = useGroup;
    }

    /**
     * @return the constructionType
     */
    public String getConstructionType() {
        return constructionType;
    }

    /**
     * @param constructionType the constructionType to set
     */
    public void setConstructionType(String constructionType) {
        this.constructionType = constructionType;
    }

    /**
     * @return the countyCode
     */
    public String getCountyCode() {
        return countyCode;
    }

    /**
     * @param countyCode the countyCode to set
     */
    public void setCountyCode(String countyCode) {
        this.countyCode = countyCode;
    }

   

    /**
     * @return the muniCode
     */
    public int getMuniCode() {
        return muniCode;
    }

    /**
     * @param muniCode the muniCode to set
     */
    public void setMuniCode(int muniCode) {
        this.muniCode = muniCode;
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
     * @return the address_city
     */
    public String getAddress_city() {
        return address_city;
    }

    /**
     * @return the address_state
     */
    public String getAddress_state() {
        return address_state;
    }

    /**
     * @return the address_zip
     */
    public String getAddress_zip() {
        return address_zip;
    }

    /**
     * @param address_city the address_city to set
     */
    public void setAddress_city(String address_city) {
        this.address_city = address_city;
    }

    /**
     * @param address_state the address_state to set
     */
    public void setAddress_state(String address_state) {
        this.address_state = address_state;
    }

    /**
     * @param address_zip the address_zip to set
     */
    public void setAddress_zip(String address_zip) {
        this.address_zip = address_zip;
    }




    /**
     * @return the ownerCode
     */
    public String getOwnerCode() {
        return ownerCode;
    }

    /**
     * @return the propclass
     */
    public String getPropclass() {
        return propclass;
    }

    /**
     * @return the lastUpdatedTS
     */
    public LocalDateTime getLastUpdatedTS() {
        return lastUpdatedTS;
    }

    /**
     * @return the lastUpdatedBy
     */
    public User getLastUpdatedBy() {
        return lastUpdatedBy;
    }

   

    /**
     * @return the bobSource
     */
    public BOBSource getBobSource() {
        return bobSource;
    }

    /**
     * @return the unfitDateStart
     */
    public LocalDateTime getUnfitDateStart() {
        return unfitDateStart;
    }

    /**
     * @return the unfitDateStop
     */
    public LocalDateTime getUnfitDateStop() {
        return unfitDateStop;
    }

    /**
     * @return the unfitBy
     */
    public User getUnfitBy() {
        return unfitBy;
    }

    /**
     * @return the abandonedDateStart
     */
    public LocalDateTime getAbandonedDateStart() {
        return abandonedDateStart;
    }

    /**
     * @return the abandonedDateStop
     */
    public LocalDateTime getAbandonedDateStop() {
        return abandonedDateStop;
    }

    /**
     * @return the abandonedBy
     */
    public User getAbandonedBy() {
        return abandonedBy;
    }

    /**
     * @return the vacantDateStart
     */
    public LocalDateTime getVacantDateStart() {
        return vacantDateStart;
    }

    /**
     * @return the vacantDateStop
     */
    public LocalDateTime getVacantDateStop() {
        return vacantDateStop;
    }

    /**
     * @return the vacantBy
     */
    public User getVacantBy() {
        return vacantBy;
    }

    /**
     * @return the conditionIntensityClassID
     */
    public int getConditionIntensityClassID() {
        return conditionIntensityClassID;
    }

    /**
     * @return the landBankProspectIntensityClassID
     */
    public int getLandBankProspectIntensityClassID() {
        return landBankProspectIntensityClassID;
    }

    /**
     * @return the LandBankHeld
     */
    public boolean isLandBankHeld() {
        return LandBankHeld;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param ownerCode the ownerCode to set
     */
    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    /**
     * @param propclass the propclass to set
     */
    public void setPropclass(String propclass) {
        this.propclass = propclass;
    }

    /**
     * @param lastUpdatedTS the lastUpdatedTS to set
     */
    public void setLastUpdatedTS(LocalDateTime lastUpdatedTS) {
        this.lastUpdatedTS = lastUpdatedTS;
    }

    /**
     * @param lastUpdatedBy the lastUpdatedBy to set
     */
    public void setLastUpdatedBy(User lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    
    /**
     * @param bobSource the bobSource to set
     */
    public void setBobSource(BOBSource bobSource) {
        this.bobSource = bobSource;
    }

    /**
     * @param unfitDateStart the unfitDateStart to set
     */
    public void setUnfitDateStart(LocalDateTime unfitDateStart) {
        this.unfitDateStart = unfitDateStart;
    }

    /**
     * @param unfitDateStop the unfitDateStop to set
     */
    public void setUnfitDateStop(LocalDateTime unfitDateStop) {
        this.unfitDateStop = unfitDateStop;
    }

    /**
     * @param unfitBy the unfitBy to set
     */
    public void setUnfitBy(User unfitBy) {
        this.unfitBy = unfitBy;
    }

    /**
     * @param abandonedDateStart the abandonedDateStart to set
     */
    public void setAbandonedDateStart(LocalDateTime abandonedDateStart) {
        this.abandonedDateStart = abandonedDateStart;
    }

    /**
     * @param abandonedDateStop the abandonedDateStop to set
     */
    public void setAbandonedDateStop(LocalDateTime abandonedDateStop) {
        this.abandonedDateStop = abandonedDateStop;
    }

    /**
     * @param abandonedBy the abandonedBy to set
     */
    public void setAbandonedBy(User abandonedBy) {
        this.abandonedBy = abandonedBy;
    }

    /**
     * @param vacantDateStart the vacantDateStart to set
     */
    public void setVacantDateStart(LocalDateTime vacantDateStart) {
        this.vacantDateStart = vacantDateStart;
    }

    /**
     * @param vacantDateStop the vacantDateStop to set
     */
    public void setVacantDateStop(LocalDateTime vacantDateStop) {
        this.vacantDateStop = vacantDateStop;
    }

    /**
     * @param vacantBy the vacantBy to set
     */
    public void setVacantBy(User vacantBy) {
        this.vacantBy = vacantBy;
    }

    /**
     * @param conditionIntensityClassID the conditionIntensityClassID to set
     */
    public void setConditionIntensityClassID(int conditionIntensityClassID) {
        this.conditionIntensityClassID = conditionIntensityClassID;
    }

    /**
     * @param landBankProspectIntensityClassID the landBankProspectIntensityClassID to set
     */
    public void setLandBankProspectIntensityClassID(int landBankProspectIntensityClassID) {
        this.landBankProspectIntensityClassID = landBankProspectIntensityClassID;
    }

    /**
     * @param LandBankHeld the LandBankHeld to set
     */
    public void setLandBankHeld(boolean LandBankHeld) {
        this.LandBankHeld = LandBankHeld;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the nonAddressable
     */
    public boolean isNonAddressable() {
        return nonAddressable;
    }

    /**
     * @param nonAddressable the nonAddressable to set
     */
    public void setNonAddressable(boolean nonAddressable) {
        this.nonAddressable = nonAddressable;
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

    /**
     * @return the status
     */
    public PropertyStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(PropertyStatus status) {
        this.status = status;
    }

    /**
     * @return the useTypeID
     */
    public int getUseTypeID() {
        return useTypeID;
    }

    /**
     * @param useTypeID the useTypeID to set
     */
    public void setUseTypeID(int useTypeID) {
        this.useTypeID = useTypeID;
    }

    /**
     * @return the unitList
     */
    public List<PropertyUnit> getUnitList() {
        return unitList;
    }

    /**
     * @param unitList the unitList to set
     */
    public void setUnitList(List<PropertyUnit> unitList) {
        this.unitList = unitList;
    }

  
}
