/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.IntensityClass;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PropertyUseType;
import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author marosco and ellen bascomb
 */
public  class       SearchParamsProperty 
        extends     SearchParams {
    
   public static final String MUNI_DBFIELD = "property.municipality_municode";
    
   // filter PROP-1
   private boolean zip_ctl;
   private String zip_val;
    
   // filter #PROP-2
   private boolean lotblock_ctl;
   private String lotblock_val;
   
   // filter #PROP-3
   private boolean bobSource_ctl;
   private BOBSource bobSource_val;
   
   // filter #PROP-4
   private boolean parcelid_ctl;
   private String parcelid_val;
   
   // filter #PROP-5
   private boolean address_ctl; 
   private String address_val;
   
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
   
   // filter #PROP-11
   private boolean zoneClass_ctl;
   private String zoneClass_val;
   
   // filter #PROP-12
   private boolean taxStatus_ctl;
   private int taxStatus_val;
   
   // filter #PROP-13
   private boolean propValue_ctl;
   private int propValue_min_val;
   private int propValue_max_val;
   
   // filter #PROP-14
   private boolean constructionYear_ctl;
   private int constructionYear_min_val;
   private int constructionYear_max_val;
   
   // filter #PROP-15 person
   private boolean person_ctl;
   private Person person_val;
   
   
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
     * @return the lotblock_val
     */
    public String getLotblock_val() {
        return lotblock_val;
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
     * @return the address_ctl
     */
    public boolean isAddress_ctl() {
        return address_ctl;
    }

    /**
     * @return the address_val
     */
    public String getAddress_val() {
        return address_val;
    }


    
    /**
     * @param lotblock_ctl the lotblock_ctl to set
     */
    public void setLotblock_ctl(boolean lotblock_ctl) {
        this.lotblock_ctl = lotblock_ctl;
    }

    /**
     * @param lotblock_val the lotblock_val to set
     */
    public void setLotblock_val(String lotblock_val) {
        this.lotblock_val = lotblock_val;
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
     * @param address_ctl the address_ctl to set
     */
    public void setAddress_ctl(boolean address_ctl) {
        this.address_ctl = address_ctl;
    }

    /**
     * @param address_val the address_val to set
     */
    public void setAddress_val(String address_val) {
        this.address_val = address_val;
    }


    /**
     * @return the zip_ctl
     */
    public boolean isZip_ctl() {
        return zip_ctl;
    }

    /**
     * @return the zip_val
     */
    public String getZip_val() {
        return zip_val;
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
     * @return the zoneClass_ctl
     */
    public boolean isZoneClass_ctl() {
        return zoneClass_ctl;
    }

    /**
     * @return the zoneClass_val
     */
    public String getZoneClass_val() {
        return zoneClass_val;
    }

    /**
     * @return the propValue_ctl
     */
    public boolean isPropValue_ctl() {
        return propValue_ctl;
    }

    /**
     * @return the propValue_min_val
     */
    public int getPropValue_min_val() {
        return propValue_min_val;
    }

    /**
     * @return the constructionYear_ctl
     */
    public boolean isConstructionYear_ctl() {
        return constructionYear_ctl;
    }

    /**
     * @return the constructionYear_min_val
     */
    public int getConstructionYear_min_val() {
        return constructionYear_min_val;
    }

    /**
     * @param zip_ctl the zip_ctl to set
     */
    public void setZip_ctl(boolean zip_ctl) {
        this.zip_ctl = zip_ctl;
    }

    /**
     * @param zip_val the zip_val to set
     */
    public void setZip_val(String zip_val) {
        this.zip_val = zip_val;
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
     * @param zoneClass_ctl the zoneClass_ctl to set
     */
    public void setZoneClass_ctl(boolean zoneClass_ctl) {
        this.zoneClass_ctl = zoneClass_ctl;
    }

    /**
     * @param zoneClass_val the zoneClass_val to set
     */
    public void setZoneClass_val(String zoneClass_val) {
        this.zoneClass_val = zoneClass_val;
    }

    /**
     * @param propValue_ctl the propValue_ctl to set
     */
    public void setPropValue_ctl(boolean propValue_ctl) {
        this.propValue_ctl = propValue_ctl;
    }

    /**
     * @param propValue_min_val the propValue_min_val to set
     */
    public void setPropValue_min_val(int propValue_min_val) {
        this.propValue_min_val = propValue_min_val;
    }

    /**
     * @param constructionYear_ctl the constructionYear_ctl to set
     */
    public void setConstructionYear_ctl(boolean constructionYear_ctl) {
        this.constructionYear_ctl = constructionYear_ctl;
    }

    /**
     * @param constructionYear_min_val the constructionYear_min_val to set
     */
    public void setConstructionYear_min_val(int constructionYear_min_val) {
        this.constructionYear_min_val = constructionYear_min_val;
    }

    /**
     * @return the propValue_max_val
     */
    public int getPropValue_max_val() {
        return propValue_max_val;
    }

    /**
     * @param propValue_max_val the propValue_max_val to set
     */
    public void setPropValue_max_val(int propValue_max_val) {
        this.propValue_max_val = propValue_max_val;
    }

    /**
     * @return the constructionYear_max_val
     */
    public int getConstructionYear_max_val() {
        return constructionYear_max_val;
    }

    /**
     * @param constructionYear_max_val the constructionYear_max_val to set
     */
    public void setConstructionYear_max_val(int constructionYear_max_val) {
        this.constructionYear_max_val = constructionYear_max_val;
    }

   
    /**
     *
     * @return
     */
    public HashMap getParams(){
        HashMap m = new HashMap();
        m.put("Fil by Address Part:", this.isAddress_ctl());
        m.put("Fil by Assessed Value:", this.isPropValue_ctl());
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
        m.put("Fil by Year Built:", this.isConstructionYear_ctl());
        m.put("Fil by Zip:", this.isZip_ctl());
        m.put("Fil by Zone Class:", this.isZoneClass_ctl());
    
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
     * @return the taxStatus_ctl
     */
    public boolean isTaxStatus_ctl() {
        return taxStatus_ctl;
    }

    /**
     * @param taxStatus_ctl the taxStatus_ctl to set
     */
    public void setTaxStatus_ctl(boolean taxStatus_ctl) {
        this.taxStatus_ctl = taxStatus_ctl;
    }

    /**
     * @return the taxStatus_val
     */
    public int getTaxStatus_val() {
        return taxStatus_val;
    }

    /**
     * @param taxStatus_val the taxStatus_val to set
     */
    public void setTaxStatus_val(int taxStatus_val) {
        this.taxStatus_val = taxStatus_val;
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
     * @return the person_val
     */
    public Person getPerson_val() {
        return person_val;
    }

    /**
     * @param person_val the person_val to set
     */
    public void setPerson_val(Person person_val) {
        this.person_val = person_val;
    }

    /**
     * @return the person_ctl
     */
    public boolean isPerson_ctl() {
        return person_ctl;
    }

    /**
     * @param person_ctl the person_ctl to set
     */
    public void setPerson_ctl(boolean person_ctl) {
        this.person_ctl = person_ctl;
    }

   
    
}
