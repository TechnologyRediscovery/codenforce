/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.CEActionRequestIssueType;
import com.tcvcog.tcvce.entities.CEActionRequestStatus;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;

/**
 *
 * @author sylvia
 */
public  class       SearchParamsCEActionRequests 
        extends     SearchParams {
    
    public static final String DBFIELD = "property.municipality_municode";
    
    // filter CEAR-1
    private boolean requestStatus_ctl;
    private CEActionRequestStatus requestStatus_val;
    
    // filter CEAR-2
    private boolean issueType_ctl;
    private CEActionRequestIssueType issueType_val;
    
    // filter CEAR-3
    private boolean nonaddressable_ctl;
    private boolean nonaddressable_val;
    
    // filter CEAR-4
    private boolean urgent_ctl;
    private boolean urgent_val;
    
    // filter CEAR-5
    private boolean caseAttachment_ctl;
    private boolean caseAttachment_val;
    
    // filter CEAR-6
    private boolean cecase_ctl;
    private CECase cecase_val;
    
    // filter CEAR-7
    private boolean pacc_ctl;
    private boolean pacc_val;
    
    // filter CEAR-8
    private boolean requestorPerson_ctl;
    private Person requestorPerson_val;
    
    // filter CEAR-9
    private boolean property_ctl;
    private Property property_val;
    
    
    /**
     * @return the requestStatus_val
     */
    public CEActionRequestStatus getRequestStatus_val() {
        return requestStatus_val;
    }

    /**
     * @param requestStatus_val the requestStatus_val to set
     */
    public void setRequestStatus_val(CEActionRequestStatus requestStatus_val) {
        this.requestStatus_val = requestStatus_val;
    }

   
    /**
     * @return the nonaddressable_val
     */
    public boolean isNonaddressable_val() {
        return nonaddressable_val;
    }



    /**
     * @return the urgent_val
     */
    public boolean isUrgent_val() {
        return urgent_val;
    }

   
    /**
     * @param nonaddressable_val the nonaddressable_val to set
     */
    public void setNonaddressable_val(boolean nonaddressable_val) {
        this.nonaddressable_val = nonaddressable_val;
    }

    /**
     * @param urgent_val the urgent_val to set
     */
    public void setUrgent_val(boolean urgent_val) {
        this.urgent_val = urgent_val;
    }

    /**
     * @return the requestStatus_ctl
     */
    public boolean isRequestStatus_ctl() {
        return requestStatus_ctl;
    }

    /**
     * @return the nonaddressable_ctl
     */
    public boolean isNonaddressable_ctl() {
        return nonaddressable_ctl;
    }

    

    /**
     * @return the urgent_ctl
     */
    public boolean isUrgent_ctl() {
        return urgent_ctl;
    }

    /**
     * @return the caseAttachment_ctl
     */
    public boolean isCaseAttachment_ctl() {
        return caseAttachment_ctl;
    }


    /**
     * @param requestStatus_ctl the requestStatus_ctl to set
     */
    public void setRequestStatus_ctl(boolean requestStatus_ctl) {
        this.requestStatus_ctl = requestStatus_ctl;
    }


    /**
     * @param nonaddressable_ctl the nonaddressable_ctl to set
     */
    public void setNonaddressable_ctl(boolean nonaddressable_ctl) {
        this.nonaddressable_ctl = nonaddressable_ctl;
    }


    /**
     * @param urgent_ctl the urgent_ctl to set
     */
    public void setUrgent_ctl(boolean urgent_ctl) {
        this.urgent_ctl = urgent_ctl;
    }

    /**
     * @param caseAttachment_ctl the caseAttachment_ctl to set
     */
    public void setCaseAttachment_ctl(boolean caseAttachment_ctl) {
        this.caseAttachment_ctl = caseAttachment_ctl;
    }


    /**
     * @return the cecase_val
     */
    public CECase getCecase_val() {
        return cecase_val;
    }

    /**
     * @param cecase_val the cecase_val to set
     */
    public void setCecase_val(CECase cecase_val) {
        this.cecase_val = cecase_val;
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
     * @return the requestorPerson_ctl
     */
    public boolean isRequestorPerson_ctl() {
        return requestorPerson_ctl;
    }

    /**
     * @return the requestorPerson_val
     */
    public Person getRequestorPerson_val() {
        return requestorPerson_val;
    }

    /**
     * @param requestorPerson_ctl the requestorPerson_ctl to set
     */
    public void setRequestorPerson_ctl(boolean requestorPerson_ctl) {
        this.requestorPerson_ctl = requestorPerson_ctl;
    }

    /**
     * @param requestorPerson_val the requestorPerson_val to set
     */
    public void setRequestorPerson_val(Person requestorPerson_val) {
        this.requestorPerson_val = requestorPerson_val;
    }

    /**
     * @return the issueType_ctl
     */
    public boolean isIssueType_ctl() {
        return issueType_ctl;
    }

    /**
     * @return the issueType_val
     */
    public CEActionRequestIssueType getIssueType_val() {
        return issueType_val;
    }

    /**
     * @param issueType_ctl the issueType_ctl to set
     */
    public void setIssueType_ctl(boolean issueType_ctl) {
        this.issueType_ctl = issueType_ctl;
    }

    /**
     * @param issueType_val the issueType_val to set
     */
    public void setIssueType_val(CEActionRequestIssueType issueType_val) {
        this.issueType_val = issueType_val;
    }

    /**
     * @return the cecase_ctl
     */
    public boolean isCecase_ctl() {
        return cecase_ctl;
    }

    /**
     * @param cecase_ctl the cecase_ctl to set
     */
    public void setCecase_ctl(boolean cecase_ctl) {
        this.cecase_ctl = cecase_ctl;
    }

    /**
     * @return the caseAttachment_val
     */
    public boolean isCaseAttachment_val() {
        return caseAttachment_val;
    }

    /**
     * @param caseAttachment_val the caseAttachment_val to set
     */
    public void setCaseAttachment_val(boolean caseAttachment_val) {
        this.caseAttachment_val = caseAttachment_val;
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
     * @return the property_ctl
     */
    public boolean isProperty_ctl() {
        return property_ctl;
    }

    /**
     * @param property_ctl the property_ctl to set
     */
    public void setProperty_ctl(boolean property_ctl) {
        this.property_ctl = property_ctl;
    }
    
}
