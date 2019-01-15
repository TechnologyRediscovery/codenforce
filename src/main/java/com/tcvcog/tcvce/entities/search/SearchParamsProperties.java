/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.CEActionRequestStatus;
import com.tcvcog.tcvce.entities.search.SearchParams;
import java.io.Serializable;

/**
 *
 * @author sylvia
 */
public class SearchParamsProperties extends SearchParams implements Serializable {
    
   private boolean filterByLotAndBlock;
   private String logAndBlock;
   
   private boolean filterByParcelID;
   private String parcelID;
   
   private boolean filterByAddressPart; 
   private String addressPart;
   
   private boolean filterByStreetPart;
   private String streetPart;
   
   private boolean filterByCECaseStartEndDate;
   private boolean filterByCECaseStatus;
   
   private boolean filterByRental;
   private boolean isContainsRentalUnits;
   
   private boolean filterByVacant;
   private boolean isVacant;
   
   private boolean filterByUnits;
   private int minUnits;
   private int maxUnits;
   
   private boolean filterBySource;
   private int sourceID;
   
   private boolean filterByPropertyUseType;
   private String propertyUseType;
   
   private boolean filterByPerson;
   private int personID;

    /**
     * @return the filterByLotAndBlock
     */
    public boolean isFilterByLotAndBlock() {
        return filterByLotAndBlock;
    }

    /**
     * @return the logAndBlock
     */
    public String getLogAndBlock() {
        return logAndBlock;
    }

    /**
     * @return the filterByParcelID
     */
    public boolean isFilterByParcelID() {
        return filterByParcelID;
    }

    /**
     * @return the parcelID
     */
    public String getParcelID() {
        return parcelID;
    }

    /**
     * @return the filterByAddressPart
     */
    public boolean isFilterByAddressPart() {
        return filterByAddressPart;
    }

    /**
     * @return the addressPart
     */
    public String getAddressPart() {
        return addressPart;
    }

    /**
     * @return the filterByStreetPart
     */
    public boolean isFilterByStreetPart() {
        return filterByStreetPart;
    }

    /**
     * @return the streetPart
     */
    public String getStreetPart() {
        return streetPart;
    }

    /**
     * @return the filterByCECaseStartEndDate
     */
    public boolean isFilterByCECaseStartEndDate() {
        return filterByCECaseStartEndDate;
    }

    /**
     * @return the filterByCECaseStatus
     */
    public boolean isFilterByCECaseStatus() {
        return filterByCECaseStatus;
    }

    /**
     * @return the filterByRental
     */
    public boolean isFilterByRental() {
        return filterByRental;
    }

    /**
     * @return the isContainsRentalUnits
     */
    public boolean isIsContainsRentalUnits() {
        return isContainsRentalUnits;
    }

    /**
     * @return the filterByVacant
     */
    public boolean isFilterByVacant() {
        return filterByVacant;
    }

    /**
     * @return the isVacant
     */
    public boolean isIsVacant() {
        return isVacant;
    }

    /**
     * @return the filterByUnits
     */
    public boolean isFilterByUnits() {
        return filterByUnits;
    }

    /**
     * @return the minUnits
     */
    public int getMinUnits() {
        return minUnits;
    }

    /**
     * @return the maxUnits
     */
    public int getMaxUnits() {
        return maxUnits;
    }

    /**
     * @return the filterBySource
     */
    public boolean isFilterBySource() {
        return filterBySource;
    }

    /**
     * @return the sourceID
     */
    public int getSourceID() {
        return sourceID;
    }

    /**
     * @return the filterByPropertyUseType
     */
    public boolean isFilterByPropertyUseType() {
        return filterByPropertyUseType;
    }

    /**
     * @return the propertyUseType
     */
    public String getPropertyUseType() {
        return propertyUseType;
    }

    /**
     * @return the filterByPerson
     */
    public boolean isFilterByPerson() {
        return filterByPerson;
    }

    /**
     * @return the personID
     */
    public int getPersonID() {
        return personID;
    }

    /**
     * @param filterByLotAndBlock the filterByLotAndBlock to set
     */
    public void setFilterByLotAndBlock(boolean filterByLotAndBlock) {
        this.filterByLotAndBlock = filterByLotAndBlock;
    }

    /**
     * @param logAndBlock the logAndBlock to set
     */
    public void setLogAndBlock(String logAndBlock) {
        this.logAndBlock = logAndBlock;
    }

    /**
     * @param filterByParcelID the filterByParcelID to set
     */
    public void setFilterByParcelID(boolean filterByParcelID) {
        this.filterByParcelID = filterByParcelID;
    }

    /**
     * @param parcelID the parcelID to set
     */
    public void setParcelID(String parcelID) {
        this.parcelID = parcelID;
    }

    /**
     * @param filterByAddressPart the filterByAddressPart to set
     */
    public void setFilterByAddressPart(boolean filterByAddressPart) {
        this.filterByAddressPart = filterByAddressPart;
    }

    /**
     * @param addressPart the addressPart to set
     */
    public void setAddressPart(String addressPart) {
        this.addressPart = addressPart;
    }

    /**
     * @param filterByStreetPart the filterByStreetPart to set
     */
    public void setFilterByStreetPart(boolean filterByStreetPart) {
        this.filterByStreetPart = filterByStreetPart;
    }

    /**
     * @param streetPart the streetPart to set
     */
    public void setStreetPart(String streetPart) {
        this.streetPart = streetPart;
    }

    /**
     * @param filterByCECaseStartEndDate the filterByCECaseStartEndDate to set
     */
    public void setFilterByCECaseStartEndDate(boolean filterByCECaseStartEndDate) {
        this.filterByCECaseStartEndDate = filterByCECaseStartEndDate;
    }

    /**
     * @param filterByCECaseStatus the filterByCECaseStatus to set
     */
    public void setFilterByCECaseStatus(boolean filterByCECaseStatus) {
        this.filterByCECaseStatus = filterByCECaseStatus;
    }

    /**
     * @param filterByRental the filterByRental to set
     */
    public void setFilterByRental(boolean filterByRental) {
        this.filterByRental = filterByRental;
    }

    /**
     * @param isContainsRentalUnits the isContainsRentalUnits to set
     */
    public void setIsContainsRentalUnits(boolean isContainsRentalUnits) {
        this.isContainsRentalUnits = isContainsRentalUnits;
    }

    /**
     * @param filterByVacant the filterByVacant to set
     */
    public void setFilterByVacant(boolean filterByVacant) {
        this.filterByVacant = filterByVacant;
    }

    /**
     * @param isVacant the isVacant to set
     */
    public void setIsVacant(boolean isVacant) {
        this.isVacant = isVacant;
    }

    /**
     * @param filterByUnits the filterByUnits to set
     */
    public void setFilterByUnits(boolean filterByUnits) {
        this.filterByUnits = filterByUnits;
    }

    /**
     * @param minUnits the minUnits to set
     */
    public void setMinUnits(int minUnits) {
        this.minUnits = minUnits;
    }

    /**
     * @param maxUnits the maxUnits to set
     */
    public void setMaxUnits(int maxUnits) {
        this.maxUnits = maxUnits;
    }

    /**
     * @param filterBySource the filterBySource to set
     */
    public void setFilterBySource(boolean filterBySource) {
        this.filterBySource = filterBySource;
    }

    /**
     * @param sourceID the sourceID to set
     */
    public void setSourceID(int sourceID) {
        this.sourceID = sourceID;
    }

    /**
     * @param filterByPropertyUseType the filterByPropertyUseType to set
     */
    public void setFilterByPropertyUseType(boolean filterByPropertyUseType) {
        this.filterByPropertyUseType = filterByPropertyUseType;
    }

    /**
     * @param propertyUseType the propertyUseType to set
     */
    public void setPropertyUseType(String propertyUseType) {
        this.propertyUseType = propertyUseType;
    }

    /**
     * @param filterByPerson the filterByPerson to set
     */
    public void setFilterByPerson(boolean filterByPerson) {
        this.filterByPerson = filterByPerson;
    }

    /**
     * @param personID the personID to set
     */
    public void setPersonID(int personID) {
        this.personID = personID;
    }
   
    
}
