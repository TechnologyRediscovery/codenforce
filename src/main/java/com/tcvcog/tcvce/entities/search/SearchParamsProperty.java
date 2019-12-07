/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.CEActionRequestStatus;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.SearchParams;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sylvia
 */
public class SearchParamsProperty extends SearchParams implements Serializable {
    
   
    // The filter by date switch is on the superclass SearchParams
    // if that's swich is on, the integrator will look at this Enum value
    // to decide which field to use in the query against the db
    private SearchParamsPropertyDateFields dateField;
    
   private boolean filterByNullDateField;
   private SearchParamsPropertyDateFields nullDateField;
    
   private boolean filterByUserField;
   private SearchParamsPropertyUserFields userField;
   private User userFieldUser;
   
   private boolean filterByZip;
   private String zipCode;
    
   private boolean filterByLotAndBlock;
   private String logAndBlock;
   
   private boolean filterByBOBSource;
   private int bobSourceID;
   
   private boolean filterByParcelID;
   private String parcelID;
   
   private boolean filterByAddressPart; 
   private String addressPart;
   
   private boolean filterByCondition;
   private int conditionIntensityClassID;
   
   private boolean filterByLandBankPropspect;
   private int landBankPropsectIntensityClassID;
   
   private boolean filterByLandBankHeld;
   private boolean landBankHeld;

   private boolean filterByNonAddressable;
   private boolean nonAddressable;
   
   private boolean filterByUseType;
   private int useTypeID;
   
   private boolean filterByZoneClass;
   private String zoneClass;
   
   private boolean filterByAssessedValue;
   private int assessedValueMin;
   private int assessedValueMax;
   
   private boolean filterByYearBuilt;
   private int yearBuiltMin;
   private int yearBuiltMax;
   
   private HashMap allParams;
   
   private List<Enum> dateSearchOptions;
   private String dateToSearchProps;
   
   
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
     * @return the dateField
     */
    public SearchParamsPropertyDateFields getDateField() {
        return dateField;
    }

    /**
     * @return the filterByNullDateField
     */
    public boolean isFilterByNullDateField() {
        return filterByNullDateField;
    }

    /**
     * @return the nullDateField
     */
    public SearchParamsPropertyDateFields getNullDateField() {
        return nullDateField;
    }

    /**
     * @return the filterByUserField
     */
    public boolean isFilterByUserField() {
        return filterByUserField;
    }

    /**
     * @return the userField
     */
    public SearchParamsPropertyUserFields getUserField() {
        return userField;
    }

    /**
     * @return the filterByZip
     */
    public boolean isFilterByZip() {
        return filterByZip;
    }

    /**
     * @return the zipCode
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * @return the filterByBOBSource
     */
    public boolean isFilterByBOBSource() {
        return filterByBOBSource;
    }

    /**
     * @return the bobSourceID
     */
    public int getBobSourceID() {
        return bobSourceID;
    }

    /**
     * @return the filterByCondition
     */
    public boolean isFilterByCondition() {
        return filterByCondition;
    }

    /**
     * @return the conditionIntensityClassID
     */
    public int getConditionIntensityClassID() {
        return conditionIntensityClassID;
    }

    /**
     * @return the filterByLandBankPropspect
     */
    public boolean isFilterByLandBankPropspect() {
        return filterByLandBankPropspect;
    }

    /**
     * @return the landBankPropsectIntensityClassID
     */
    public int getLandBankPropsectIntensityClassID() {
        return landBankPropsectIntensityClassID;
    }

    /**
     * @return the filterByLandBankHeld
     */
    public boolean isFilterByLandBankHeld() {
        return filterByLandBankHeld;
    }

    /**
     * @return the landBankHeld
     */
    public boolean isLandBankHeld() {
        return landBankHeld;
    }

    /**
     * @return the filterByNonAddressable
     */
    public boolean isFilterByNonAddressable() {
        return filterByNonAddressable;
    }

    /**
     * @return the nonAddressable
     */
    public boolean isNonAddressable() {
        return nonAddressable;
    }

    
    /**
     * @return the filterByUseType
     */
    public boolean isFilterByUseType() {
        return filterByUseType;
    }

    /**
     * @return the useTypeID
     */
    public int getUseTypeID() {
        return useTypeID;
    }

    /**
     * @return the filterByZoneClass
     */
    public boolean isFilterByZoneClass() {
        return filterByZoneClass;
    }

    /**
     * @return the zoneClass
     */
    public String getZoneClass() {
        return zoneClass;
    }

    /**
     * @return the filterByAssessedValue
     */
    public boolean isFilterByAssessedValue() {
        return filterByAssessedValue;
    }

    /**
     * @return the assessedValueMin
     */
    public int getAssessedValueMin() {
        return assessedValueMin;
    }

    /**
     * @return the filterByYearBuilt
     */
    public boolean isFilterByYearBuilt() {
        return filterByYearBuilt;
    }

    /**
     * @return the yearBuiltMin
     */
    public int getYearBuiltMin() {
        return yearBuiltMin;
    }

    /**
     * @param dateField the dateField to set
     */
    public void setDateField(SearchParamsPropertyDateFields dateField) {
        this.dateField = dateField;
    }

    /**
     * @param filterByNullDateField the filterByNullDateField to set
     */
    public void setFilterByNullDateField(boolean filterByNullDateField) {
        this.filterByNullDateField = filterByNullDateField;
    }

    /**
     * @param nullDateField the nullDateField to set
     */
    public void setNullDateField(SearchParamsPropertyDateFields nullDateField) {
        this.nullDateField = nullDateField;
    }

    /**
     * @param filterByUserField the filterByUserField to set
     */
    public void setFilterByUserField(boolean filterByUserField) {
        this.filterByUserField = filterByUserField;
    }

    /**
     * @param userField the userField to set
     */
    public void setUserField(SearchParamsPropertyUserFields userField) {
        this.userField = userField;
    }

    /**
     * @param filterByZip the filterByZip to set
     */
    public void setFilterByZip(boolean filterByZip) {
        this.filterByZip = filterByZip;
    }

    /**
     * @param zipCode the zipCode to set
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    /**
     * @param filterByBOBSource the filterByBOBSource to set
     */
    public void setFilterByBOBSource(boolean filterByBOBSource) {
        this.filterByBOBSource = filterByBOBSource;
    }

    /**
     * @param bobSourceID the bobSourceID to set
     */
    public void setBobSourceID(int bobSourceID) {
        this.bobSourceID = bobSourceID;
    }

    /**
     * @param filterByCondition the filterByCondition to set
     */
    public void setFilterByCondition(boolean filterByCondition) {
        this.filterByCondition = filterByCondition;
    }

    /**
     * @param conditionIntensityClassID the conditionIntensityClassID to set
     */
    public void setConditionIntensityClassID(int conditionIntensityClassID) {
        this.conditionIntensityClassID = conditionIntensityClassID;
    }

    /**
     * @param filterByLandBankPropspect the filterByLandBankPropspect to set
     */
    public void setFilterByLandBankPropspect(boolean filterByLandBankPropspect) {
        this.filterByLandBankPropspect = filterByLandBankPropspect;
    }

    /**
     * @param landBankPropsectIntensityClassID the landBankPropsectIntensityClassID to set
     */
    public void setLandBankPropsectIntensityClassID(int landBankPropsectIntensityClassID) {
        this.landBankPropsectIntensityClassID = landBankPropsectIntensityClassID;
    }

    /**
     * @param filterByLandBankHeld the filterByLandBankHeld to set
     */
    public void setFilterByLandBankHeld(boolean filterByLandBankHeld) {
        this.filterByLandBankHeld = filterByLandBankHeld;
    }

    /**
     * @param landBankHeld the landBankHeld to set
     */
    public void setLandBankHeld(boolean landBankHeld) {
        this.landBankHeld = landBankHeld;
    }

    /**
     * @param filterByNonAddressable the filterByNonAddressable to set
     */
    public void setFilterByNonAddressable(boolean filterByNonAddressable) {
        this.filterByNonAddressable = filterByNonAddressable;
    }

    /**
     * @param nonAddressable the nonAddressable to set
     */
    public void setNonAddressable(boolean nonAddressable) {
        this.nonAddressable = nonAddressable;
    }

   
    /**
     * @param filterByUseType the filterByUseType to set
     */
    public void setFilterByUseType(boolean filterByUseType) {
        this.filterByUseType = filterByUseType;
    }

    /**
     * @param useTypeID the useTypeID to set
     */
    public void setUseTypeID(int useTypeID) {
        this.useTypeID = useTypeID;
    }

    /**
     * @param filterByZoneClass the filterByZoneClass to set
     */
    public void setFilterByZoneClass(boolean filterByZoneClass) {
        this.filterByZoneClass = filterByZoneClass;
    }

    /**
     * @param zoneClass the zoneClass to set
     */
    public void setZoneClass(String zoneClass) {
        this.zoneClass = zoneClass;
    }

    /**
     * @param filterByAssessedValue the filterByAssessedValue to set
     */
    public void setFilterByAssessedValue(boolean filterByAssessedValue) {
        this.filterByAssessedValue = filterByAssessedValue;
    }

    /**
     * @param assessedValueMin the assessedValueMin to set
     */
    public void setAssessedValueMin(int assessedValueMin) {
        this.assessedValueMin = assessedValueMin;
    }

    /**
     * @param filterByYearBuilt the filterByYearBuilt to set
     */
    public void setFilterByYearBuilt(boolean filterByYearBuilt) {
        this.filterByYearBuilt = filterByYearBuilt;
    }

    /**
     * @param yearBuiltMin the yearBuiltMin to set
     */
    public void setYearBuiltMin(int yearBuiltMin) {
        this.yearBuiltMin = yearBuiltMin;
    }

    /**
     * @return the assessedValueMax
     */
    public int getAssessedValueMax() {
        return assessedValueMax;
    }

    /**
     * @param assessedValueMax the assessedValueMax to set
     */
    public void setAssessedValueMax(int assessedValueMax) {
        this.assessedValueMax = assessedValueMax;
    }

    /**
     * @return the yearBuiltMax
     */
    public int getYearBuiltMax() {
        return yearBuiltMax;
    }

    /**
     * @param yearBuiltMax the yearBuiltMax to set
     */
    public void setYearBuiltMax(int yearBuiltMax) {
        this.yearBuiltMax = yearBuiltMax;
    }

    /**
     * @return the userFieldUser
     */
    public User getUserFieldUser() {
        return userFieldUser;
    }

    /**
     * @param userFieldUser the userFieldUser to set
     */
    public void setUserFieldUser(User userFieldUser) {
        this.userFieldUser = userFieldUser;
    }
   
    /**
     *
     * @return
     */
    public HashMap getParams(){
        HashMap m = new HashMap();
        m.put("Fil by Address Part:", this.isFilterByAddressPart());
        m.put("Fil by Assessed Value:", this.isFilterByAssessedValue());
        m.put("Fil by Bob Source:", this.isFilterByBOBSource());
        m.put("Fil by Condition:", this.isFilterByCondition());
        m.put("Fil by Land Bank Held:", this.isFilterByLandBankHeld());
        m.put("Fil by Land Bank Proipect:", this.isFilterByLandBankPropspect());
        m.put("Fil by Lot and block:", this.isFilterByLotAndBlock());
        m.put("Fil by Muni:", this.isFilterByMuni());
        m.put("Fil by Nonaddressable:", this.isFilterByNonAddressable());
        m.put("Fil by Null date field:", this.isFilterByNullDateField());
        m.put("Fil by Object ID:", this.isObjectID_filterBy());
        m.put("Fil by Parcel ID:", this.isFilterByParcelID());
        m.put("Fil by Start End Date:", this.isFilterByStartEndDate());
        m.put("Fil by Use Type:", this.isFilterByUseType());
        m.put("Fil by User Field:", this.isFilterByUserField());
        m.put("Fil by Year Built:", this.isFilterByYearBuilt());
        m.put("Fil by Zip:", this.isFilterByZip());
        m.put("Fil by Zone Class:", this.isFilterByZoneClass());
    
        return m;
        
}
    
    public HashMap getAllParams(){
        return this.allParams;
    }
           

    /**
     * @param allParams the allParams to set
     */
    public void setAllParams(HashMap allParams) {
        this.allParams = allParams;
    }

    /**
     * @return the dateSearchOptions
     */
    public List<Enum> getDateSearchOptions() {
        List<Enum> dateOptList = SearchParamsPropertyDateFields.ABANDONED_START.getAllTitles();
        dateSearchOptions = dateOptList;
        return dateSearchOptions;
    }

    /**
     * @param dateSearchOptions the dateSearchOptions to set
     */
    public void setDateSearchOptions(List<Enum> dateSearchOptions) {
        this.dateSearchOptions = dateSearchOptions;
    }

    /**
     * @return the dateToSearchProps
     */
    public String getDateToSearchProps() {
        return dateToSearchProps;
    }

    /**
     * @param dateToSearchProps the dateToSearchProps to set
     */
    public void setDateToSearchProps(String dateToSearchProps) {
        this.dateToSearchProps = dateToSearchProps;
    }
   
    
}
