/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.IntensityClass;
import com.tcvcog.tcvce.entities.Property;

/**
 * Umbrella container for all the switches that will be used to build a 
 * valid SQL query
 * 
 * @author Ellen Bascomb
 */
public class        SearchParamsCodeViolation 
        extends     SearchParams{
    
    public static final String DBFIELD_MUNI = "parcel.muni_municode";
    public static final String DBFIELD_ACTIVE = "codeviolation.active";
    
    
    // CV param set 1
    private boolean property_ctl;
    private Property property_val;
    
    // CV param set 2
    private boolean cecase_ctl;
    private CECase cecase_val;
   
    
    
    // CV param set 3
    /**
     * This will check if the violation has been attached to a citation
     * whose citation status is not allowing edits, which is a hacky
     * way to use an existing field to control which citations we see
     * 
     * TODO: connect to citation table to allow citation status
     */
    private boolean cited_ctl;
    private boolean cited_val;
    
    // CV param set 4
    private boolean legacyImport_ctl;
    private boolean legacyImport_val;
    
    // CV param set 5
    private boolean severity_ctl;
    private IntensityClass severity_val;
    
    // CV param set 6
    private boolean source_ctl;
    private BOBSource source_val;
    
    // CV param set 7
    private boolean noticeMailed_ctl;
    private boolean noticeMailed_val;
    
    // CV param set 8
    private boolean transferred_ctl;
    private boolean transferred_val;
    
    // CV param set 9
    private boolean compliance_ctl;
    private boolean compliance_val;
    
    // CV param set 10
    private boolean nullified_ctl;
    private boolean nullified_val;
    
    
   public SearchParamsCodeViolation(){
       
   }
   
   
      
    public SearchParamsCodeViolationDateFieldsEnum[] getDateFieldList(){
       SearchParamsCodeViolationDateFieldsEnum[] fields = SearchParamsCodeViolationDateFieldsEnum.values();
       return fields;
   }
   
   public SearchParamsCodeViolationUserFieldsEnum[] getUserFieldList(){
       SearchParamsCodeViolationUserFieldsEnum[] fields = SearchParamsCodeViolationUserFieldsEnum.values();
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
     * @return the cecase_ctl
     */
    public boolean isCecase_ctl() {
        return cecase_ctl;
    }

    /**
     * @return the cecase_val
     */
    public CECase getCecase_val() {
        return cecase_val;
    }

    /**
     * @return the cited_ctl
     */
    public boolean isCited_ctl() {
        return cited_ctl;
    }

    /**
     * @return the cited_val
     */
    public boolean isCited_val() {
        return cited_val;
    }

    /**
     * @return the legacyImport_ctl
     */
    public boolean isLegacyImport_ctl() {
        return legacyImport_ctl;
    }

    /**
     * @return the legacyImport_val
     */
    public boolean isLegacyImport_val() {
        return legacyImport_val;
    }

    /**
     * @return the severity_ctl
     */
    public boolean isSeverity_ctl() {
        return severity_ctl;
    }

    /**
     * @return the severity_val
     */
    public IntensityClass getSeverity_val() {
        return severity_val;
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
     * @param cecase_ctl the cecase_ctl to set
     */
    public void setCecase_ctl(boolean cecase_ctl) {
        this.cecase_ctl = cecase_ctl;
    }

    /**
     * @param cecase_val the cecase_val to set
     */
    public void setCecase_val(CECase cecase_val) {
        this.cecase_val = cecase_val;
    }

    /**
     * @param cited_ctl the cited_ctl to set
     */
    public void setCited_ctl(boolean cited_ctl) {
        this.cited_ctl = cited_ctl;
    }

    /**
     * @param cited_val the cited_val to set
     */
    public void setCited_val(boolean cited_val) {
        this.cited_val = cited_val;
    }

    /**
     * @param legacyImport_ctl the legacyImport_ctl to set
     */
    public void setLegacyImport_ctl(boolean legacyImport_ctl) {
        this.legacyImport_ctl = legacyImport_ctl;
    }

    /**
     * @param legacyImport_val the legacyImport_val to set
     */
    public void setLegacyImport_val(boolean legacyImport_val) {
        this.legacyImport_val = legacyImport_val;
    }

    /**
     * @param severity_ctl the severity_ctl to set
     */
    public void setSeverity_ctl(boolean severity_ctl) {
        this.severity_ctl = severity_ctl;
    }

    /**
     * @param severity_val the severity_val to set
     */
    public void setSeverity_val(IntensityClass severity_val) {
        this.severity_val = severity_val;
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
     * @return the noticeMailed_ctl
     */
    public boolean isNoticeMailed_ctl() {
        return noticeMailed_ctl;
    }

    /**
     * @return the noticeMailed_val
     */
    public boolean isNoticeMailed_val() {
        return noticeMailed_val;
    }

    /**
     * @param noticeMailed_ctl the noticeMailed_ctl to set
     */
    public void setNoticeMailed_ctl(boolean noticeMailed_ctl) {
        this.noticeMailed_ctl = noticeMailed_ctl;
    }

    /**
     * @param noticeMailed_val the noticeMailed_val to set
     */
    public void setNoticeMailed_val(boolean noticeMailed_val) {
        this.noticeMailed_val = noticeMailed_val;
    }

    /**
     * @return the transferred_ctl
     */
    public boolean isTransferred_ctl() {
        return transferred_ctl;
    }

    /**
     * @return the transferred_val
     */
    public boolean isTransferred_val() {
        return transferred_val;
    }

    /**
     * @return the compliance_ctl
     */
    public boolean isCompliance_ctl() {
        return compliance_ctl;
    }

    /**
     * @return the compliance_val
     */
    public boolean isCompliance_val() {
        return compliance_val;
    }

    /**
     * @return the nullified_ctl
     */
    public boolean isNullified_ctl() {
        return nullified_ctl;
    }

    /**
     * @return the nullified_val
     */
    public boolean isNullified_val() {
        return nullified_val;
    }

    /**
     * @param transferred_ctl the transferred_ctl to set
     */
    public void setTransferred_ctl(boolean transferred_ctl) {
        this.transferred_ctl = transferred_ctl;
    }

    /**
     * @param transferred_val the transferred_val to set
     */
    public void setTransferred_val(boolean transferred_val) {
        this.transferred_val = transferred_val;
    }

    /**
     * @param compliance_ctl the compliance_ctl to set
     */
    public void setCompliance_ctl(boolean compliance_ctl) {
        this.compliance_ctl = compliance_ctl;
    }

    /**
     * @param compliance_val the compliance_val to set
     */
    public void setCompliance_val(boolean compliance_val) {
        this.compliance_val = compliance_val;
    }

    /**
     * @param nullified_ctl the nullified_ctl to set
     */
    public void setNullified_ctl(boolean nullified_ctl) {
        this.nullified_ctl = nullified_ctl;
    }

    /**
     * @param nullified_val the nullified_val to set
     */
    public void setNullified_val(boolean nullified_val) {
        this.nullified_val = nullified_val;
    }
   
  

    
}
