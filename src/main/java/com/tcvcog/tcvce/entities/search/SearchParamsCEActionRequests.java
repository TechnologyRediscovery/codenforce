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
    
    private boolean useRequestStatus;
    private CEActionRequestStatus requestStatus;
    
    private boolean useNotAtAddress;
    private boolean notAtAnAddress;
    
    private boolean useMarkedUrgent;
    private boolean markedUrgent;
    
    private boolean useAttachedToCase;
    private boolean attachedToCase;
    
    private boolean useRequestID;
    private int requestID;
    

    /**
     * @return the requestStatus
     */
    public CEActionRequestStatus getRequestStatus() {
        return requestStatus;
    }

    /**
     * @param requestStatus the requestStatus to set
     */
    public void setRequestStatus(CEActionRequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

   
    /**
     * @return the notAtAnAddress
     */
    public boolean isNotAtAnAddress() {
        return notAtAnAddress;
    }



    /**
     * @return the markedUrgent
     */
    public boolean isMarkedUrgent() {
        return markedUrgent;
    }

    /**
     * @return the attachedToCase
     */
    public boolean isAttachedToCase() {
        return attachedToCase;
    }

    /**
     * @return the requestID
     */
    public int getRequestID() {
        return requestID;
    }

   
    /**
     * @param notAtAnAddress the notAtAnAddress to set
     */
    public void setNotAtAnAddress(boolean notAtAnAddress) {
        this.notAtAnAddress = notAtAnAddress;
    }

    /**
     * @param markedUrgent the markedUrgent to set
     */
    public void setMarkedUrgent(boolean markedUrgent) {
        this.markedUrgent = markedUrgent;
    }

    /**
     * @param attachedToCase the attachedToCase to set
     */
    public void setAttachedToCase(boolean attachedToCase) {
        this.attachedToCase = attachedToCase;
    }

    /**
     * @param requestID the requestID to set
     */
    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }

    /**
     * @return the useRequestStatus
     */
    public boolean isUseRequestStatus() {
        return useRequestStatus;
    }

    /**
     * @return the useNotAtAddress
     */
    public boolean isUseNotAtAddress() {
        return useNotAtAddress;
    }

    

    /**
     * @return the useMarkedUrgent
     */
    public boolean isUseMarkedUrgent() {
        return useMarkedUrgent;
    }

    /**
     * @return the useAttachedToCase
     */
    public boolean isUseAttachedToCase() {
        return useAttachedToCase;
    }

    /**
     * @return the useRequestID
     */
    public boolean isUseRequestID() {
        return useRequestID;
    }

    /**
     * @param useRequestStatus the useRequestStatus to set
     */
    public void setUseRequestStatus(boolean useRequestStatus) {
        this.useRequestStatus = useRequestStatus;
    }


    /**
     * @param useNotAtAddress the useNotAtAddress to set
     */
    public void setUseNotAtAddress(boolean useNotAtAddress) {
        this.useNotAtAddress = useNotAtAddress;
    }


    /**
     * @param useMarkedUrgent the useMarkedUrgent to set
     */
    public void setUseMarkedUrgent(boolean useMarkedUrgent) {
        this.useMarkedUrgent = useMarkedUrgent;
    }

    /**
     * @param useAttachedToCase the useAttachedToCase to set
     */
    public void setUseAttachedToCase(boolean useAttachedToCase) {
        this.useAttachedToCase = useAttachedToCase;
    }

    /**
     * @param useRequestID the useRequestID to set
     */
    public void setUseRequestID(boolean useRequestID) {
        this.useRequestID = useRequestID;
    }
    
}
