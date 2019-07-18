/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

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
    protected Municipality muni;
    protected int muniCode;
    protected String parID;
    protected String lotAndBlock;
    protected String address;
    
    protected String propertyUseType;
    protected String useGroup;
    protected String constructionType;
    protected String countyCode;
    protected String apartmentno;
    
    protected String notes;
    protected String address_city;
    protected String address_state;
    protected String address_zip;
    
    protected String ownerCode;
    protected String propclass;
  
    protected LocalDateTime lastupdated;
    protected User lastupdatedby;
    protected String locationdescription;
    
    protected BOBSource datasource;
    protected LocalDateTime unfitdatestart;
    protected LocalDateTime unfitdatestop;
    protected User unfitby;
    
    protected LocalDateTime abandoneddatestart;
    protected LocalDateTime abandoneddatestop;
    protected User abandonedby;
    protected LocalDateTime vacantdatestart;
    
    protected LocalDateTime vacantdatestop;
    protected User vacantbu_userid;
    protected int condition_intensityclassid;
    
    protected int landbankprospect_intensityclassid;
    protected boolean landbankheld;
    protected boolean active;
    protected boolean nonAddressable;
    
    
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
     * @return the propertyUseType
     */
    public String getPropertyUseType() {
        return propertyUseType;
    }

    /**
     * @param propertyUseType the propertyUseType to set
     */
    public void setPropertyUseType(String propertyUseType) {
        this.propertyUseType = propertyUseType;
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
     * @return the apartmentno
     */
    public String getApartmentno() {
        return apartmentno;
    }

    /**
     * @param apartmentno the apartmentno to set
     */
    public void setApartmentno(String apartmentno) {
        this.apartmentno = apartmentno;
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
     * @return the lastupdated
     */
    public LocalDateTime getLastupdated() {
        return lastupdated;
    }

    /**
     * @return the lastupdatedby
     */
    public User getLastupdatedby() {
        return lastupdatedby;
    }

    /**
     * @return the locationdescription
     */
    public String getLocationdescription() {
        return locationdescription;
    }

    /**
     * @return the datasource
     */
    public BOBSource getDatasource() {
        return datasource;
    }

    /**
     * @return the unfitdatestart
     */
    public LocalDateTime getUnfitdatestart() {
        return unfitdatestart;
    }

    /**
     * @return the unfitdatestop
     */
    public LocalDateTime getUnfitdatestop() {
        return unfitdatestop;
    }

    /**
     * @return the unfitby
     */
    public User getUnfitby() {
        return unfitby;
    }

    /**
     * @return the abandoneddatestart
     */
    public LocalDateTime getAbandoneddatestart() {
        return abandoneddatestart;
    }

    /**
     * @return the abandoneddatestop
     */
    public LocalDateTime getAbandoneddatestop() {
        return abandoneddatestop;
    }

    /**
     * @return the abandonedby
     */
    public User getAbandonedby() {
        return abandonedby;
    }

    /**
     * @return the vacantdatestart
     */
    public LocalDateTime getVacantdatestart() {
        return vacantdatestart;
    }

    /**
     * @return the vacantdatestop
     */
    public LocalDateTime getVacantdatestop() {
        return vacantdatestop;
    }

    /**
     * @return the vacantbu_userid
     */
    public User getVacantbu_userid() {
        return vacantbu_userid;
    }

    /**
     * @return the condition_intensityclassid
     */
    public int getCondition_intensityclassid() {
        return condition_intensityclassid;
    }

    /**
     * @return the landbankprospect_intensityclassid
     */
    public int getLandbankprospect_intensityclassid() {
        return landbankprospect_intensityclassid;
    }

    /**
     * @return the landbankheld
     */
    public boolean isLandbankheld() {
        return landbankheld;
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
     * @param lastupdated the lastupdated to set
     */
    public void setLastupdated(LocalDateTime lastupdated) {
        this.lastupdated = lastupdated;
    }

    /**
     * @param lastupdatedby the lastupdatedby to set
     */
    public void setLastupdatedby(User lastupdatedby) {
        this.lastupdatedby = lastupdatedby;
    }

    /**
     * @param locationdescription the locationdescription to set
     */
    public void setLocationdescription(String locationdescription) {
        this.locationdescription = locationdescription;
    }

    /**
     * @param datasource the datasource to set
     */
    public void setDatasource(BOBSource datasource) {
        this.datasource = datasource;
    }

    /**
     * @param unfitdatestart the unfitdatestart to set
     */
    public void setUnfitdatestart(LocalDateTime unfitdatestart) {
        this.unfitdatestart = unfitdatestart;
    }

    /**
     * @param unfitdatestop the unfitdatestop to set
     */
    public void setUnfitdatestop(LocalDateTime unfitdatestop) {
        this.unfitdatestop = unfitdatestop;
    }

    /**
     * @param unfitby the unfitby to set
     */
    public void setUnfitby(User unfitby) {
        this.unfitby = unfitby;
    }

    /**
     * @param abandoneddatestart the abandoneddatestart to set
     */
    public void setAbandoneddatestart(LocalDateTime abandoneddatestart) {
        this.abandoneddatestart = abandoneddatestart;
    }

    /**
     * @param abandoneddatestop the abandoneddatestop to set
     */
    public void setAbandoneddatestop(LocalDateTime abandoneddatestop) {
        this.abandoneddatestop = abandoneddatestop;
    }

    /**
     * @param abandonedby the abandonedby to set
     */
    public void setAbandonedby(User abandonedby) {
        this.abandonedby = abandonedby;
    }

    /**
     * @param vacantdatestart the vacantdatestart to set
     */
    public void setVacantdatestart(LocalDateTime vacantdatestart) {
        this.vacantdatestart = vacantdatestart;
    }

    /**
     * @param vacantdatestop the vacantdatestop to set
     */
    public void setVacantdatestop(LocalDateTime vacantdatestop) {
        this.vacantdatestop = vacantdatestop;
    }

    /**
     * @param vacantbu_userid the vacantbu_userid to set
     */
    public void setVacantbu_userid(User vacantbu_userid) {
        this.vacantbu_userid = vacantbu_userid;
    }

    /**
     * @param condition_intensityclassid the condition_intensityclassid to set
     */
    public void setCondition_intensityclassid(int condition_intensityclassid) {
        this.condition_intensityclassid = condition_intensityclassid;
    }

    /**
     * @param landbankprospect_intensityclassid the landbankprospect_intensityclassid to set
     */
    public void setLandbankprospect_intensityclassid(int landbankprospect_intensityclassid) {
        this.landbankprospect_intensityclassid = landbankprospect_intensityclassid;
    }

    /**
     * @param landbankheld the landbankheld to set
     */
    public void setLandbankheld(boolean landbankheld) {
        this.landbankheld = landbankheld;
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

  
}
