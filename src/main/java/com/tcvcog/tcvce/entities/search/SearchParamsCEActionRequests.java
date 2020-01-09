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
public  class   SearchParamsCEActionRequests 
        extends SearchParams {
    
    private boolean requestStatus_ctl;
    private CEActionRequestStatus requestStatus_val;
    
    private boolean nonaddressable_ctl;
    private boolean nonaddressable_val;
    
    private boolean urgent_ctl;
    private boolean urgent_val;
    
    private boolean caseAttachment_ctl;
    private boolean caseAttachment_val;
    
    private boolean useRequestID;
    private int requestID;
    

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
     * @return the caseAttachment_val
     */
    public boolean isCaseAttachment_val() {
        return caseAttachment_val;
    }

    /**
     * @return the requestID
     */
    public int getRequestID() {
        return requestID;
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
     * @param caseAttachment_val the caseAttachment_val to set
     */
    public void setCaseAttachment_val(boolean caseAttachment_val) {
        this.caseAttachment_val = caseAttachment_val;
    }

    /**
     * @param requestID the requestID to set
     */
    public void setRequestID(int requestID) {
        this.requestID = requestID;
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
     * @return the useRequestID
     */
    public boolean isUseRequestID() {
        return useRequestID;
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
     * @param useRequestID the useRequestID to set
     */
    public void setUseRequestID(boolean useRequestID) {
        this.useRequestID = useRequestID;
    }
    
}
