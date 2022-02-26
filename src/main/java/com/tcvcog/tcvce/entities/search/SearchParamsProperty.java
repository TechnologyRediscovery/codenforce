/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.IntensityClass;
import com.tcvcog.tcvce.entities.PropertyUseType;
import java.util.HashMap;

/**
 * Represents all the fields you can search parcels by
 * Updated FEB 2022 for parcelization
 * 
 * @author marosco and ellen bascomb
 */
public  class       SearchParamsProperty 
        extends     SearchParams {
    
   public static final String MUNI_DBFIELD = "parcel.muni_municode";
    
   // filter #PROP-1
   private boolean lotblock_ctl;
   private String lotblock_val_num1;
   private String lotblock_val_letter;
   private String lotblock_val_num2;
   
   // filter #PROP-2
   private boolean bobSource_ctl;
   private BOBSource bobSource_val;
   
   // filter #PROP-3
   private boolean parcelid_ctl;
   private String parcelid_val;
   
   // filter #PROP-4
   private boolean addressBldgNum_ctl; 
   private String addressBldgNum_val;
   
   // filter #PROP-5
   private boolean addressStreetName_ctl;
   private String addressStreetName_val;
   
   // filter #PROP-6
   private boolean condition_ctl;
   private IntensityClass condition_intensityClass_val;
   
   // filter #PROP-7
   private boolean landbankprospect_ctl;
   private IntensityClass landbankprospect_intensityClass_val;
   
   // filter #PROP-8
   private boolean landbankheld_ctl;
   private boolean landbankheld_val;

   // filter #PROP-9
   private boolean nonaddressable_ctl;
   private boolean nonaddressable_val;
   
   // filter #PROP-10
   private boolean useType_ctl;
   private PropertyUseType useType_val;
   
   
   public SearchParamsPropertyDateFieldsEnum[] getDateFieldList(){
       SearchParamsPropertyDateFieldsEnum[] fields = SearchParamsPropertyDateFieldsEnum.values();
       return fields;
   }
   
   public SearchParamsPropertyUserFieldsEnum[] getUserFieldList(){
       SearchParamsPropertyUserFieldsEnum[] fields = SearchParamsPropertyUserFieldsEnum.values();
       return fields;
   }
   
    /**
     * @return the lotblock_ctl
     */
    public boolean isLotblock_ctl() {
        return lotblock_ctl;
    }

    /**
     * @return the lotblock_val_num1
     */
    public String getLotblock_val_num1() {
        return lotblock_val_num1;
    }

    /**
     * @return the parcelid_ctl
     */
    public boolean isParcelid_ctl() {
        return parcelid_ctl;
    }

    /**
     * @return the parcelid_val
     */
    public String getParcelid_val() {
        return parcelid_val;
    }

    /**
     * @return the addressBldgNum_ctl
     */
    public boolean isAddress_bldgNum_ctl() {
        return addressBldgNum_ctl;
    }

    /**
     * @return the addressBldgNum_val
     */
    public String getAddress_bldgNum_val() {
        return addressBldgNum_val;
    }


    
    /**
     * @param lotblock_ctl the lotblock_ctl to set
     */
    public void setLotblock_ctl(boolean lotblock_ctl) {
        this.lotblock_ctl = lotblock_ctl;
    }

    /**
     * @param lotblock_val the lotblock_val_num1 to set
     */
    public void setLotblock_val_num1(String lotblock_val) {
        this.lotblock_val_num1 = lotblock_val;
    }

    /**
     * @param parcelid_ctl the parcelid_ctl to set
     */
    public void setParcelid_ctl(boolean parcelid_ctl) {
        this.parcelid_ctl = parcelid_ctl;
    }

    /**
     * @param parcelid_val the parcelid_val to set
     */
    public void setParcelid_val(String parcelid_val) {
        this.parcelid_val = parcelid_val;
    }

    /**
     * @param address_ctl the addressBldgNum_ctl to set
     */
    public void setAddress_bldgNum_ctl(boolean address_ctl) {
        this.addressBldgNum_ctl = address_ctl;
    }

    /**
     * @param address_val the addressBldgNum_val to set
     */
    public void setAddress_bldgNum_val(String address_val) {
        this.addressBldgNum_val = address_val;
    }


    /**
     * @return the bobSource_ctl
     */
    public boolean isBobSource_ctl() {
        return bobSource_ctl;
    }


    /**
     * @return the condition_ctl
     */
    public boolean isCondition_ctl() {
        return condition_ctl;
    }


    /**
     * @return the landbankprospect_ctl
     */
    public boolean isLandbankprospect_ctl() {
        return landbankprospect_ctl;
    }


    /**
     * @return the landbankheld_ctl
     */
    public boolean isLandbankheld_ctl() {
        return landbankheld_ctl;
    }

    /**
     * @return the landbankheld_val
     */
    public boolean isLandbankheld_val() {
        return landbankheld_val;
    }

    /**
     * @return the nonaddressable_ctl
     */
    public boolean isNonaddressable_ctl() {
        return nonaddressable_ctl;
    }

    /**
     * @return the nonaddressable_val
     */
    public boolean isNonaddressable_val() {
        return nonaddressable_val;
    }

    
    /**
     * @return the useType_ctl
     */
    public boolean isUseType_ctl() {
        return useType_ctl;
    }


    /**
     * @param bobSource_ctl the bobSource_ctl to set
     */
    public void setBobSource_ctl(boolean bobSource_ctl) {
        this.bobSource_ctl = bobSource_ctl;
    }


    /**
     * @param condition_ctl the condition_ctl to set
     */
    public void setCondition_ctl(boolean condition_ctl) {
        this.condition_ctl = condition_ctl;
    }


    /**
     * @param landbankprospect_ctl the landbankprospect_ctl to set
     */
    public void setLandbankprospect_ctl(boolean landbankprospect_ctl) {
        this.landbankprospect_ctl = landbankprospect_ctl;
    }


    /**
     * @param landbankheld_ctl the landbankheld_ctl to set
     */
    public void setLandbankheld_ctl(boolean landbankheld_ctl) {
        this.landbankheld_ctl = landbankheld_ctl;
    }

    /**
     * @param landbankheld_val the landbankheld_val to set
     */
    public void setLandbankheld_val(boolean landbankheld_val) {
        this.landbankheld_val = landbankheld_val;
    }

    /**
     * @param nonaddressable_ctl the nonaddressable_ctl to set
     */
    public void setNonaddressable_ctl(boolean nonaddressable_ctl) {
        this.nonaddressable_ctl = nonaddressable_ctl;
    }

    /**
     * @param nonaddressable_val the nonaddressable_val to set
     */
    public void setNonaddressable_val(boolean nonaddressable_val) {
        this.nonaddressable_val = nonaddressable_val;
    }

   
    /**
     * @param useType_ctl the useType_ctl to set
     */
    public void setUseType_ctl(boolean useType_ctl) {
        this.useType_ctl = useType_ctl;
    }


  
   
    /**
     *
     * @return
     */
    public HashMap getParams(){
        HashMap m = new HashMap();
        m.put("Fil by Address Part:", this.isAddress_bldgNum_ctl());
        m.put("Fil by Bob Source:", this.isBobSource_ctl());
        m.put("Fil by Condition:", this.isCondition_ctl());
        m.put("Fil by Land Bank Held:", this.isLandbankheld_ctl());
        m.put("Fil by Land Bank Proipect:", this.isLandbankprospect_ctl());
        m.put("Fil by Lot and block:", this.isLotblock_ctl());
        m.put("Fil by Muni:", this.isMuni_ctl());
        m.put("Fil by Nonaddressable:", this.isNonaddressable_ctl());
        m.put("Fil by Object ID:", this.isBobID_ctl());
        m.put("Fil by Parcel ID:", this.isParcelid_ctl());
        m.put("Fil by Start End Date:", this.isDate_startEnd_ctl());
        m.put("Fil by Use Type:", this.isUseType_ctl());
        m.put("Fil by User Field:", this.isUser_ctl());
    
        return m;
        
}

    /**
     * @return the useType_val
     */
    public PropertyUseType getUseType_val() {
        return useType_val;
    }

    /**
     * @param useType_val the useType_val to set
     */
    public void setUseType_val(PropertyUseType useType_val) {
        this.useType_val = useType_val;
    }

    /**
     * @return the bobSource_val
     */
    public BOBSource getBobSource_val() {
        return bobSource_val;
    }

    /**
     * @param bobSource_val the bobSource_val to set
     */
    public void setBobSource_val(BOBSource bobSource_val) {
        this.bobSource_val = bobSource_val;
    }

  
    /**
     * @return the condition_intensityClass_val
     */
    public IntensityClass getCondition_intensityClass_val() {
        return condition_intensityClass_val;
    }

    /**
     * @param condition_intensityClass_val the condition_intensityClass_val to set
     */
    public void setCondition_intensityClass_val(IntensityClass condition_intensityClass_val) {
        this.condition_intensityClass_val = condition_intensityClass_val;
    }

    /**
     * @return the landbankprospect_intensityClass_val
     */
    public IntensityClass getLandbankprospect_intensityClass_val() {
        return landbankprospect_intensityClass_val;
    }

    /**
     * @param landbankprospect_intensityClass_val the landbankprospect_intensityClass_val to set
     */
    public void setLandbankprospect_intensityClass_val(IntensityClass landbankprospect_intensityClass_val) {
        this.landbankprospect_intensityClass_val = landbankprospect_intensityClass_val;
    }

  
    /**
     * @return the addressStreetName_ctl
     */
    public boolean isAddressStreetName_ctl() {
        return addressStreetName_ctl;
    }

    /**
     * @return the addressStreetName_val
     */
    public String getAddressStreetName_val() {
        return addressStreetName_val;
    }

    /**
     * @param addressStreetName_ctl the addressStreetName_ctl to set
     */
    public void setAddressStreetName_ctl(boolean addressStreetName_ctl) {
        this.addressStreetName_ctl = addressStreetName_ctl;
    }

    /**
     * @param addressStreetName_val the addressStreetName_val to set
     */
    public void setAddressStreetName_val(String addressStreetName_val) {
        this.addressStreetName_val = addressStreetName_val;
    }

    /**
     * @return the lotblock_val_letter
     */
    public String getLotblock_val_letter() {
        return lotblock_val_letter;
    }

    /**
     * @return the lotblock_val_num2
     */
    public String getLotblock_val_num2() {
        return lotblock_val_num2;
    }

    /**
     * @param lotblock_val_letter the lotblock_val_letter to set
     */
    public void setLotblock_val_letter(String lotblock_val_letter) {
        this.lotblock_val_letter = lotblock_val_letter;
    }

    /**
     * @param lotblock_val_num2 the lotblock_val_num2 to set
     */
    public void setLotblock_val_num2(String lotblock_val_num2) {
        this.lotblock_val_num2 = lotblock_val_num2;
    }

   
    
}
