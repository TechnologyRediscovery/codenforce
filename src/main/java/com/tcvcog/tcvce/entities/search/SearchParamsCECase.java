/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.CasePhaseEnum;
import com.tcvcog.tcvce.entities.CaseStageEnum;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;

/**
 *
 * @author Ellen Bascomb
 */
public class        SearchParamsCECase 
        extends     SearchParams{
    
    public static final String MUNI_DBFIELD = "property.municipality_municode";
    
    // filter CECASE-1
    private boolean caseOpen_ctl;
    private boolean caseOpen_val;
    
    // filter CECASE-2
    private boolean property_ctl;
    private Property property_val;
    
    // filter CECASE-3
    private boolean propertyUnit_ctl;
    private PropertyUnit propertyUnit_val;
    
    // filter CECASE-4
    private boolean propInfoCase_ctl;
    private boolean propInfoCase_val;
    
    // filter CECASE-5
    private boolean personInfoCase_ctl;
    private boolean personInfoCase_val;
    
    // filter CECASE-6
    private boolean personInfoCaseID_ctl;
    private Person personInfoCaseID_val;
    
    // filter CECASE-7
    private boolean source_ctl;
    private BOBSource source_val;
    
    // filter CECASE-8
    private boolean pacc_ctl;
    private boolean pacc_val;

    // *******************************
    // ** JavaLand switches only!   **
    // *******************************
    
    // filter CECASE-9
    private boolean caseStage_ctl;
    private CaseStageEnum caseStage_val;
    
    // ****** END JAVALAND ONLY ********
    
    
   public SearchParamsCECase(){
       
   }
   
   
      
    public SearchParamsCECaseDateFieldsEnum[] getDateFieldList(){
       SearchParamsCECaseDateFieldsEnum[] fields = SearchParamsCECaseDateFieldsEnum.values();
       return fields;
   }
   
   public SearchParamsCECaseUserFieldsEnum[] getUserFieldList(){
       SearchParamsCECaseUserFieldsEnum[] fields = SearchParamsCECaseUserFieldsEnum.values();
       return fields;
   }
   
    /**
     * @return the property_ctl
     */
    public boolean isProperty_ctl() {
        return property_ctl;
    }

    /**
     * @return the property_val
     */
    public Property getProperty_val() {
        return property_val;
    }

    /**
     * @return the propInfoCase_ctl
     */
    public boolean isPropInfoCase_ctl() {
        return propInfoCase_ctl;
    }

    /**
     * @return the propInfoCase_val
     */
    public boolean isPropInfoCase_val() {
        return propInfoCase_val;
    }


    /**
     * @param property_ctl the property_ctl to set
     */
    public void setProperty_ctl(boolean property_ctl) {
        this.property_ctl = property_ctl;
    }

    /**
     * @param property_val the property_val to set
     */
    public void setProperty_val(Property property_val) {
        this.property_val = property_val;
    }

    /**
     * @param propInfoCase_ctl the propInfoCase_ctl to set
     */
    public void setPropInfoCase_ctl(boolean propInfoCase_ctl) {
        this.propInfoCase_ctl = propInfoCase_ctl;
    }

    /**
     * @param propInfoCase_val the propInfoCase_val to set
     */
    public void setPropInfoCase_val(boolean propInfoCase_val) {
        this.propInfoCase_val = propInfoCase_val;
    }


    /**
     * @return the propertyUnit_ctl
     */
    public boolean isPropertyUnit_ctl() {
        return propertyUnit_ctl;
    }

    /**
     * @return the propertyUnit_val
     */
    public PropertyUnit getPropertyUnit_val() {
        return propertyUnit_val;
    }

    /**
     * @return the personInfoCase_ctl
     */
    public boolean isPersonInfoCase_ctl() {
        return personInfoCase_ctl;
    }

    /**
     * @return the personInfoCase_val
     */
    public boolean isPersonInfoCase_val() {
        return personInfoCase_val;
    }

    /**
     * @return the source_ctl
     */
    public boolean isSource_ctl() {
        return source_ctl;
    }

    /**
     * @return the source_val
     */
    public BOBSource getSource_val() {
        return source_val;
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
     * @param propertyUnit_ctl the propertyUnit_ctl to set
     */
    public void setPropertyUnit_ctl(boolean propertyUnit_ctl) {
        this.propertyUnit_ctl = propertyUnit_ctl;
    }

    /**
     * @param propertyUnit_val the propertyUnit_val to set
     */
    public void setPropertyUnit_val(PropertyUnit propertyUnit_val) {
        this.propertyUnit_val = propertyUnit_val;
    }

    /**
     * @param personInfoCase_ctl the personInfoCase_ctl to set
     */
    public void setPersonInfoCase_ctl(boolean personInfoCase_ctl) {
        this.personInfoCase_ctl = personInfoCase_ctl;
    }

    /**
     * @param personInfoCase_val the personInfoCase_val to set
     */
    public void setPersonInfoCase_val(boolean personInfoCase_val) {
        this.personInfoCase_val = personInfoCase_val;
    }

    /**
     * @param source_ctl the source_ctl to set
     */
    public void setSource_ctl(boolean source_ctl) {
        this.source_ctl = source_ctl;
    }

    /**
     * @param source_val the source_val to set
     */
    public void setSource_val(BOBSource source_val) {
        this.source_val = source_val;
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
     * @return the caseStage_ctl
     */
    public boolean isCaseStage_ctl() {
        return caseStage_ctl;
    }

    /**
     * @return the caseStage_val
     */
    public CaseStageEnum getCaseStage_val() {
        return caseStage_val;
    }


    /**
     * @param caseStage_ctl the caseStage_ctl to set
     */
    public void setCaseStage_ctl(boolean caseStage_ctl) {
        this.caseStage_ctl = caseStage_ctl;
    }

    /**
     * @param caseStage_val the caseStage_val to set
     */
    public void setCaseStage_val(CaseStageEnum caseStage_val) {
        this.caseStage_val = caseStage_val;
    }

    /**
     * @return the caseOpen_ctl
     */
    public boolean isCaseOpen_ctl() {
        return caseOpen_ctl;
    }

    /**
     * @return the caseOpen_val
     */
    public boolean isCaseOpen_val() {
        return caseOpen_val;
    }

    /**
     * @param caseOpen_ctl the caseOpen_ctl to set
     */
    public void setCaseOpen_ctl(boolean caseOpen_ctl) {
        this.caseOpen_ctl = caseOpen_ctl;
    }

    /**
     * @param caseOpen_val the caseOpen_val to set
     */
    public void setCaseOpen_val(boolean caseOpen_val) {
        this.caseOpen_val = caseOpen_val;
    }

    /**
     * @return the personInfoCaseID_ctl
     */
    public boolean isPersonInfoCaseID_ctl() {
        return personInfoCaseID_ctl;
    }

    /**
     * @param personInfoCaseID_ctl the personInfoCaseID_ctl to set
     */
    public void setPersonInfoCaseID_ctl(boolean personInfoCaseID_ctl) {
        this.personInfoCaseID_ctl = personInfoCaseID_ctl;
    }

    /**
     * @return the personInfoCaseID_val
     */
    public Person getPersonInfoCaseID_val() {
        return personInfoCaseID_val;
    }

    /**
     * @param personInfoCaseID_val the personInfoCaseID_val to set
     */
    public void setPersonInfoCaseID_val(Person personInfoCaseID_val) {
        this.personInfoCaseID_val = personInfoCaseID_val;
    }

    
}
