/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.entities.occupancy.OccPermit;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Foundational entity for the system: Property
 * @author Eric Darsow
 */

public class Property implements Serializable{
    
    private int propertyID;
    private Municipality muni;
    private int muniCode;
    private String parID;
    
    private String lotAndBlock;
    private String address;
    private String address_city;
    private String address_state;
    private String address_zip;
    
    private List<PropertyUnit> unitList;
    
    private String propertyUseType;
    
    private String useGroup;
    private String constructionType;
    private String countyCode;
    private String apartmentno;
    
    private List<CECase> infoCaseList;
    
    private boolean vacant;
    
    private String notes;
    
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
     * @return the vacant
     */
    public boolean isVacant() {
        return vacant;
    }

    /**
     * @param vacant the vacant to set
     */
    public void setVacant(boolean vacant) {
        this.vacant = vacant;
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

    /**
     * @return the infoCaseList
     */
    public List<CECase> getInfoCaseList() {
        return infoCaseList;
    }

    /**
     * @param infoCaseList the infoCaseList to set
     */
    public void setInfoCaseList(List<CECase> infoCaseList) {
        this.infoCaseList = infoCaseList;
    }
  
}
