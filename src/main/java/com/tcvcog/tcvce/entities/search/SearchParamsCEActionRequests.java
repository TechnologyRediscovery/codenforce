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
public class SearchParamsCEActionRequests extends SearchParams implements Serializable {
    
    private boolean useRequestStatus;
    private CEActionRequestStatus requestStatus;
    
    private boolean useDescriptionContains;
    private String descriptionContains;
    
    private boolean useLastNameOfRequestor;
    private String lastNameOfRequestor;
    
    private boolean useNotAtAddress;
    private boolean notAtAnAddress;
    
    private boolean useDescribedLocation;
    private boolean describedLocation;
    
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
     * @return the descriptionContains
     */
    public String getDescriptionContains() {
        return descriptionContains;
    }

    /**
     * @param descriptionContains the descriptionContains to set
     */
    public void setDescriptionContains(String descriptionContains) {
        this.descriptionContains = descriptionContains;
    }

    /**
     * @return the lastNameOfRequestor
     */
    public String getLastNameOfRequestor() {
        return lastNameOfRequestor;
    }

    /**
     * @return the notAtAnAddress
     */
    public boolean isNotAtAnAddress() {
        return notAtAnAddress;
    }

    /**
     * @return the describedLocation
     */
    public boolean isDescribedLocation() {
        return describedLocation;
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
     * @param lastNameOfRequestor the lastNameOfRequestor to set
     */
    public void setLastNameOfRequestor(String lastNameOfRequestor) {
        this.lastNameOfRequestor = lastNameOfRequestor;
    }

    /**
     * @param notAtAnAddress the notAtAnAddress to set
     */
    public void setNotAtAnAddress(boolean notAtAnAddress) {
        this.notAtAnAddress = notAtAnAddress;
    }

    /**
     * @param describedLocation the describedLocation to set
     */
    public void setDescribedLocation(boolean describedLocation) {
        this.describedLocation = describedLocation;
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
     * @return the useDescriptionContains
     */
    public boolean isUseDescriptionContains() {
        return useDescriptionContains;
    }

    /**
     * @return the useLastNameOfRequestor
     */
    public boolean isUseLastNameOfRequestor() {
        return useLastNameOfRequestor;
    }

    /**
     * @return the useNotAtAddress
     */
    public boolean isUseNotAtAddress() {
        return useNotAtAddress;
    }

    /**
     * @return the useDescribedLocation
     */
    public boolean isUseDescribedLocation() {
        return useDescribedLocation;
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
     * @param useDescriptionContains the useDescriptionContains to set
     */
    public void setUseDescriptionContains(boolean useDescriptionContains) {
        this.useDescriptionContains = useDescriptionContains;
    }

    /**
     * @param useLastNameOfRequestor the useLastNameOfRequestor to set
     */
    public void setUseLastNameOfRequestor(boolean useLastNameOfRequestor) {
        this.useLastNameOfRequestor = useLastNameOfRequestor;
    }

    /**
     * @param useNotAtAddress the useNotAtAddress to set
     */
    public void setUseNotAtAddress(boolean useNotAtAddress) {
        this.useNotAtAddress = useNotAtAddress;
    }

    /**
     * @param useDescribedLocation the useDescribedLocation to set
     */
    public void setUseDescribedLocation(boolean useDescribedLocation) {
        this.useDescribedLocation = useDescribedLocation;
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
