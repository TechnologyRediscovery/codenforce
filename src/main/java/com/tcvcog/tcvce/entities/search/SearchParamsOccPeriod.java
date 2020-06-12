/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import java.io.Serializable;

/**
 *
 * @author sylvia
 */
public class        SearchParamsOccPeriod 
        extends     SearchParams {
    
    public static final String MUNI_DBFIELD = "property.municipality_municode";
    
    // filter OCC-1
    private boolean property_ctl;
    private Property property_val;
    
    // filter OCC-2
    private boolean propertyUnit_ctl;
    private PropertyUnit propertyUnit_val;
    
    // filter OCC-3
    private boolean periodType_ctl;
    private OccPeriodType periodType_val;
    
    // filter OCC-4
    private boolean permitIssuance_ctl;
    private boolean permitIssuance_val;
    
    // filter OCC-5
    private boolean inspectionPassed_ctl;
    private boolean inspectionPassed_val;
    
    // filter OCC-6
    private boolean thirdPartyInspector_ctl;
    private boolean thirdPartyInspector_registered_val;
    private boolean thirdPartyInspector_approved_val;
    
    // filter OCC-7
    private boolean pacc_ctl;
    private boolean pacc_val;
    
    // filter OCC-8
    private boolean person_ctl;
    private Person person_val;
    
    
    public SearchParamsOccPeriodDateFieldsEnum[] getDateFieldList(){
       SearchParamsOccPeriodDateFieldsEnum[] fields = SearchParamsOccPeriodDateFieldsEnum.values();
       return fields;
   }
   
   public SearchParamsOccPeriodUserFieldsEnum[] getUserFieldList(){
       SearchParamsOccPeriodUserFieldsEnum[] fields = SearchParamsOccPeriodUserFieldsEnum.values();
       return fields;
   }
    
    
    /**
     * @return the periodType_ctl
     */
    public boolean isPeriodType_ctl() {
        return periodType_ctl;
    }

    /**
     * @return the periodType_val
     */
    public OccPeriodType getPeriodType_val() {
        return periodType_val;
    }

    /**
     * @param periodType_ctl the periodType_ctl to set
     */
    public void setPeriodType_ctl(boolean periodType_ctl) {
        this.periodType_ctl = periodType_ctl;
    }

    /**
     * @param periodType_val the periodType_val to set
     */
    public void setPeriodType_val(OccPeriodType periodType_val) {
        this.periodType_val = periodType_val;
    }

    /**
     * @return the permitIssuance_ctl
     */
    public boolean isPermitIssuance_ctl() {
        return permitIssuance_ctl;
    }

    /**
     * @return the permitIssuance_val
     */
    public boolean isPermitIssuance_val() {
        return permitIssuance_val;
    }

    /**
     * @return the inspectionPassed_ctl
     */
    public boolean isInspectionPassed_ctl() {
        return inspectionPassed_ctl;
    }

    /**
     * @return the inspectionPassed_val
     */
    public boolean isInspectionPassed_val() {
        return inspectionPassed_val;
    }

    /**
     * @return the thirdPartyInspector_ctl
     */
    public boolean isThirdPartyInspector_ctl() {
        return thirdPartyInspector_ctl;
    }

    /**
     * @return the thirdPartyInspector_registered_val
     */
    public boolean isThirdPartyInspector_registered_val() {
        return thirdPartyInspector_registered_val;
    }

    /**
     * @return the thirdPartyInspector_approved_val
     */
    public boolean isThirdPartyInspector_approved_val() {
        return thirdPartyInspector_approved_val;
    }

    /**
     * @return the pacc_ctl
     */
    public boolean isPacc_ctl() {
        return pacc_ctl;
    }

    /**
     * @return the pacc_val
     */
    public boolean isPacc_val() {
        return pacc_val;
    }


    /**
     * @param permitIssuance_ctl the permitIssuance_ctl to set
     */
    public void setPermitIssuance_ctl(boolean permitIssuance_ctl) {
        this.permitIssuance_ctl = permitIssuance_ctl;
    }

    /**
     * @param permitIssuance_val the permitIssuance_val to set
     */
    public void setPermitIssuance_val(boolean permitIssuance_val) {
        this.permitIssuance_val = permitIssuance_val;
    }

    /**
     * @param inspectionPassed_ctl the inspectionPassed_ctl to set
     */
    public void setInspectionPassed_ctl(boolean inspectionPassed_ctl) {
        this.inspectionPassed_ctl = inspectionPassed_ctl;
    }

    /**
     * @param inspectionPassed_val the inspectionPassed_val to set
     */
    public void setInspectionPassed_val(boolean inspectionPassed_val) {
        this.inspectionPassed_val = inspectionPassed_val;
    }

    /**
     * @param thirdPartyInspector_ctl the thirdPartyInspector_ctl to set
     */
    public void setThirdPartyInspector_ctl(boolean thirdPartyInspector_ctl) {
        this.thirdPartyInspector_ctl = thirdPartyInspector_ctl;
    }

    /**
     * @param thirdPartyInspector_registered_val the thirdPartyInspector_registered_val to set
     */
    public void setThirdPartyInspector_registered_val(boolean thirdPartyInspector_registered_val) {
        this.thirdPartyInspector_registered_val = thirdPartyInspector_registered_val;
    }

    /**
     * @param thirdPartyInspector_approved_val the thirdPartyInspector_approved_val to set
     */
    public void setThirdPartyInspector_approved_val(boolean thirdPartyInspector_approved_val) {
        this.thirdPartyInspector_approved_val = thirdPartyInspector_approved_val;
    }

    /**
     * @param pacc_ctl the pacc_ctl to set
     */
    public void setPacc_ctl(boolean pacc_ctl) {
        this.pacc_ctl = pacc_ctl;
    }

    /**
     * @param pacc_val the pacc_val to set
     */
    public void setPacc_val(boolean pacc_val) {
        this.pacc_val = pacc_val;
    }


    /**
     * @return the propertyUnit_ctl
     */
    public boolean isPropertyUnit_ctl() {
        return propertyUnit_ctl;
    }


    /**
     * @return the property_ctl
     */
    public boolean isProperty_ctl() {
        return property_ctl;
    }


    /**
     * @param propertyUnit_ctl the propertyUnit_ctl to set
     */
    public void setPropertyUnit_ctl(boolean propertyUnit_ctl) {
        this.propertyUnit_ctl = propertyUnit_ctl;
    }

    /**
     * @param property_ctl the property_ctl to set
     */
    public void setProperty_ctl(boolean property_ctl) {
        this.property_ctl = property_ctl;
    }

    /**
     * @return the property_val
     */
    public Property getProperty_val() {
        return property_val;
    }

    /**
     * @param property_val the property_val to set
     */
    public void setProperty_val(Property property_val) {
        this.property_val = property_val;
    }

    /**
     * @return the propertyUnit_val
     */
    public PropertyUnit getPropertyUnit_val() {
        return propertyUnit_val;
    }

    /**
     * @param propertyUnit_val the propertyUnit_val to set
     */
    public void setPropertyUnit_val(PropertyUnit propertyUnit_val) {
        this.propertyUnit_val = propertyUnit_val;
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

   
    
}
