/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.CEActionRequestStatus;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import com.tcvcog.tcvce.entities.search.SearchParams;
import java.io.Serializable;

/**
 *
 * @author sylvia
 */
public class SearchParamsOccPeriod extends SearchParams implements Serializable {
    
    private boolean propertyUnit_filterBy;
    private int propertyUnit_unitID;
    
    private boolean property_filterBy;
    private int property_propertyid;
    
    // The filter by date switch is on the superclass SearchParams
    // if that's swich is on, the integrator will look at this Enum value
    // to decide which field to use in the query against the db
    private SearchParamsOccPeriodDateFields dateField;
    
    private boolean nullDateField_filterBy;
    private SearchParamsOccPeriodDateFields nullDateField_EnumValue;
    
    private boolean occPeriodType_filterBy;
    private OccPeriodType occPeriodType_type;
    
    private boolean userField_filter;
    private SearchParamsOccPeriodUserFields userField_enumValue;
    private User userFieldUser;
    
    private boolean overrideTypeConfig_filterBy;
    private boolean overrideTypeConfig_switch_overridesTypeConfig;
    
    private boolean permitIssuance_filterBy;
    private boolean permitIssuance_switch_permitIssued;
    
    private boolean inspectionPassed_filterBy;
    private boolean inspectionPassed_switch_passedInspection;
    
    private boolean thirdPartyInspector_filterBy;
    private boolean thirdPartyInspector_switch_thirdPartyRegistered;
    private boolean thirdPartyInspector_switch_thirdPartyApproval;
    
    private boolean paccEnabled_filterBy;
    private boolean paccEnabled_switch_paccIsEnabled;
    
    private boolean checklistImplemented_filterBy;
    private int checklistImplemented_checklistID;

    /**
     * @return the dateField
     */
    public SearchParamsOccPeriodDateFields getDateField() {
        return dateField;
    }

    /**
     * @return the nullDateField_filterBy
     */
    public boolean isNullDateField_filterBy() {
        return nullDateField_filterBy;
    }

    /**
     * @return the nullDateField_EnumValue
     */
    public SearchParamsOccPeriodDateFields getNullDateField_EnumValue() {
        return nullDateField_EnumValue;
    }

    /**
     * @return the occPeriodType_filterBy
     */
    public boolean isOccPeriodType_filterBy() {
        return occPeriodType_filterBy;
    }

    /**
     * @return the occPeriodType_type
     */
    public OccPeriodType getOccPeriodType_type() {
        return occPeriodType_type;
    }

    /**
     * @return the userField_filter
     */
    public boolean isUserField_filter() {
        return userField_filter;
    }

    /**
     * @return the userField_enumValue
     */
    public SearchParamsOccPeriodUserFields getUserField_enumValue() {
        return userField_enumValue;
    }

   
    /**
     * @param dateField the dateField to set
     */
    public void setDateField(SearchParamsOccPeriodDateFields dateField) {
        this.dateField = dateField;
    }

    /**
     * @param nullDateField_filterBy the nullDateField_filterBy to set
     */
    public void setNullDateField_filterBy(boolean nullDateField_filterBy) {
        this.nullDateField_filterBy = nullDateField_filterBy;
    }

    /**
     * @param nullDateField_EnumValue the nullDateField_EnumValue to set
     */
    public void setNullDateField_EnumValue(SearchParamsOccPeriodDateFields nullDateField_EnumValue) {
        this.nullDateField_EnumValue = nullDateField_EnumValue;
    }

    /**
     * @param occPeriodType_filterBy the occPeriodType_filterBy to set
     */
    public void setOccPeriodType_filterBy(boolean occPeriodType_filterBy) {
        this.occPeriodType_filterBy = occPeriodType_filterBy;
    }

    /**
     * @param occPeriodType_type the occPeriodType_type to set
     */
    public void setOccPeriodType_type(OccPeriodType occPeriodType_type) {
        this.occPeriodType_type = occPeriodType_type;
    }

    /**
     * @param userField_filter the userField_filter to set
     */
    public void setUserField_filter(boolean userField_filter) {
        this.userField_filter = userField_filter;
    }

    /**
     * @param userField_enumValue the userField_enumValue to set
     */
    public void setUserField_enumValue(SearchParamsOccPeriodUserFields userField_enumValue) {
        this.userField_enumValue = userField_enumValue;
    }

    /**
     * @return the overrideTypeConfig_filterBy
     */
    public boolean isOverrideTypeConfig_filterBy() {
        return overrideTypeConfig_filterBy;
    }

    /**
     * @return the overrideTypeConfig_switch_overridesTypeConfig
     */
    public boolean isOverrideTypeConfig_switch_overridesTypeConfig() {
        return overrideTypeConfig_switch_overridesTypeConfig;
    }

    /**
     * @param overrideTypeConfig_filterBy the overrideTypeConfig_filterBy to set
     */
    public void setOverrideTypeConfig_filterBy(boolean overrideTypeConfig_filterBy) {
        this.overrideTypeConfig_filterBy = overrideTypeConfig_filterBy;
    }

    /**
     * @param overrideTypeConfig_switch_overridesTypeConfig the overrideTypeConfig_switch_overridesTypeConfig to set
     */
    public void setOverrideTypeConfig_switch_overridesTypeConfig(boolean overrideTypeConfig_switch_overridesTypeConfig) {
        this.overrideTypeConfig_switch_overridesTypeConfig = overrideTypeConfig_switch_overridesTypeConfig;
    }

    /**
     * @return the permitIssuance_filterBy
     */
    public boolean isPermitIssuance_filterBy() {
        return permitIssuance_filterBy;
    }

    /**
     * @return the permitIssuance_switch_permitIssued
     */
    public boolean isPermitIssuance_switch_permitIssued() {
        return permitIssuance_switch_permitIssued;
    }

    /**
     * @return the inspectionPassed_filterBy
     */
    public boolean isInspectionPassed_filterBy() {
        return inspectionPassed_filterBy;
    }

    /**
     * @return the inspectionPassed_switch_passedInspection
     */
    public boolean isInspectionPassed_switch_passedInspection() {
        return inspectionPassed_switch_passedInspection;
    }

    /**
     * @return the thirdPartyInspector_filterBy
     */
    public boolean isThirdPartyInspector_filterBy() {
        return thirdPartyInspector_filterBy;
    }

    /**
     * @return the thirdPartyInspector_switch_thirdPartyRegistered
     */
    public boolean isThirdPartyInspector_switch_thirdPartyRegistered() {
        return thirdPartyInspector_switch_thirdPartyRegistered;
    }

    /**
     * @return the thirdPartyInspector_switch_thirdPartyApproval
     */
    public boolean isThirdPartyInspector_switch_thirdPartyApproval() {
        return thirdPartyInspector_switch_thirdPartyApproval;
    }

    /**
     * @return the paccEnabled_filterBy
     */
    public boolean isPaccEnabled_filterBy() {
        return paccEnabled_filterBy;
    }

    /**
     * @return the paccEnabled_switch_paccIsEnabled
     */
    public boolean isPaccEnabled_switch_paccIsEnabled() {
        return paccEnabled_switch_paccIsEnabled;
    }

    /**
     * @return the checklistImplemented_filterBy
     */
    public boolean isChecklistImplemented_filterBy() {
        return checklistImplemented_filterBy;
    }

    /**
     * @return the checklistImplemented_checklistID
     */
    public int getChecklistImplemented_checklistID() {
        return checklistImplemented_checklistID;
    }

    /**
     * @param permitIssuance_filterBy the permitIssuance_filterBy to set
     */
    public void setPermitIssuance_filterBy(boolean permitIssuance_filterBy) {
        this.permitIssuance_filterBy = permitIssuance_filterBy;
    }

    /**
     * @param permitIssuance_switch_permitIssued the permitIssuance_switch_permitIssued to set
     */
    public void setPermitIssuance_switch_permitIssued(boolean permitIssuance_switch_permitIssued) {
        this.permitIssuance_switch_permitIssued = permitIssuance_switch_permitIssued;
    }

    /**
     * @param inspectionPassed_filterBy the inspectionPassed_filterBy to set
     */
    public void setInspectionPassed_filterBy(boolean inspectionPassed_filterBy) {
        this.inspectionPassed_filterBy = inspectionPassed_filterBy;
    }

    /**
     * @param inspectionPassed_switch_passedInspection the inspectionPassed_switch_passedInspection to set
     */
    public void setInspectionPassed_switch_passedInspection(boolean inspectionPassed_switch_passedInspection) {
        this.inspectionPassed_switch_passedInspection = inspectionPassed_switch_passedInspection;
    }

    /**
     * @param thirdPartyInspector_filterBy the thirdPartyInspector_filterBy to set
     */
    public void setThirdPartyInspector_filterBy(boolean thirdPartyInspector_filterBy) {
        this.thirdPartyInspector_filterBy = thirdPartyInspector_filterBy;
    }

    /**
     * @param thirdPartyInspector_switch_thirdPartyRegistered the thirdPartyInspector_switch_thirdPartyRegistered to set
     */
    public void setThirdPartyInspector_switch_thirdPartyRegistered(boolean thirdPartyInspector_switch_thirdPartyRegistered) {
        this.thirdPartyInspector_switch_thirdPartyRegistered = thirdPartyInspector_switch_thirdPartyRegistered;
    }

    /**
     * @param thirdPartyInspector_switch_thirdPartyApproval the thirdPartyInspector_switch_thirdPartyApproval to set
     */
    public void setThirdPartyInspector_switch_thirdPartyApproval(boolean thirdPartyInspector_switch_thirdPartyApproval) {
        this.thirdPartyInspector_switch_thirdPartyApproval = thirdPartyInspector_switch_thirdPartyApproval;
    }

    /**
     * @param paccEnabled_filterBy the paccEnabled_filterBy to set
     */
    public void setPaccEnabled_filterBy(boolean paccEnabled_filterBy) {
        this.paccEnabled_filterBy = paccEnabled_filterBy;
    }

    /**
     * @param paccEnabled_switch_paccIsEnabled the paccEnabled_switch_paccIsEnabled to set
     */
    public void setPaccEnabled_switch_paccIsEnabled(boolean paccEnabled_switch_paccIsEnabled) {
        this.paccEnabled_switch_paccIsEnabled = paccEnabled_switch_paccIsEnabled;
    }

    /**
     * @param checklistImplemented_filterBy the checklistImplemented_filterBy to set
     */
    public void setChecklistImplemented_filterBy(boolean checklistImplemented_filterBy) {
        this.checklistImplemented_filterBy = checklistImplemented_filterBy;
    }

    /**
     * @param checklistImplemented_checklistID the checklistImplemented_checklistID to set
     */
    public void setChecklistImplemented_checklistID(int checklistImplemented_checklistID) {
        this.checklistImplemented_checklistID = checklistImplemented_checklistID;
    }

    /**
     * @return the propertyUnit_filterBy
     */
    public boolean isPropertyUnit_filterBy() {
        return propertyUnit_filterBy;
    }

    /**
     * @return the propertyUnit_unitID
     */
    public int getPropertyUnit_unitID() {
        return propertyUnit_unitID;
    }

    /**
     * @return the property_filterBy
     */
    public boolean isProperty_filterBy() {
        return property_filterBy;
    }

    /**
     * @return the property_propertyid
     */
    public int getProperty_propertyid() {
        return property_propertyid;
    }

    /**
     * @param propertyUnit_filterBy the propertyUnit_filterBy to set
     */
    public void setPropertyUnit_filterBy(boolean propertyUnit_filterBy) {
        this.propertyUnit_filterBy = propertyUnit_filterBy;
    }

    /**
     * @param propertyUnit_unitID the propertyUnit_unitID to set
     */
    public void setPropertyUnit_unitID(int propertyUnit_unitID) {
        this.propertyUnit_unitID = propertyUnit_unitID;
    }

    /**
     * @param property_filterBy the property_filterBy to set
     */
    public void setProperty_filterBy(boolean property_filterBy) {
        this.property_filterBy = property_filterBy;
    }

    /**
     * @param property_propertyid the property_propertyid to set
     */
    public void setProperty_propertyid(int property_propertyid) {
        this.property_propertyid = property_propertyid;
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

   
    
}
