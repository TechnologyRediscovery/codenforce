/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.CaseStage;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.User;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ellen Bascomb
 */
public class        SearchParamsCECase 
        extends     SearchParams {
    
    
    /**
     * Required by superclass to know which field to search. 
     * Superclass which has the _ctl and _val components
     */
    private SearchParamsCECaseDateFieldsEnum date_field;
    
    /**
     * Required by superclass which has the _ctl and _val components
     */
    private SearchParamsCECaseUserFieldsEnum user_field;
    
    private boolean open_ctl;
    private boolean open_val;
    
    private boolean property_ctl;
    private Property property_val;
    
    private boolean propertyUnit_ctl;
    private PropertyUnit propertyUnit_val;
    
    private boolean propInfoCase_ctl;
    private boolean propInfoCase_val;
    
    private boolean personInfoCase_ctl;
    private boolean personInfoCase_val;
    
    private boolean source_ctl;
    private BOBSource source_val;
    
    private boolean pacc_ctl;
    private boolean pacc_val;
    
    
    
    
   public SearchParamsCECase(){
       
   }

    /**
     * @return the open_ctl
     */
    public boolean isOpen_ctl() {
        return open_ctl;
    }

    /**
     * @return the open_val
     */
    public boolean isOpen_val() {
        return open_val;
    }

    /**
     * @param open_ctl the open_ctl to set
     */
    public void setOpen_ctl(boolean open_ctl) {
        this.open_ctl = open_ctl;
    }

    /**
     * @param open_val the open_val to set
     */
    public void setOpen_val(boolean open_val) {
        this.open_val = open_val;
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
     * @return the date_field
     */
    public SearchParamsCECaseDateFieldsEnum getDate_field() {
        return date_field;
    }

    /**
     * @param date_field the date_field to set
     */
    public void setDate_field(SearchParamsCECaseDateFieldsEnum date_field) {
        this.date_field = date_field;
    }

    /**
     * @return the user_field
     */
    public SearchParamsCECaseUserFieldsEnum getUser_field() {
        return user_field;
    }

    /**
     * @param user_field the user_field to set
     */
    public void setUser_field(SearchParamsCECaseUserFieldsEnum user_field) {
        this.user_field = user_field;
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

    
}
