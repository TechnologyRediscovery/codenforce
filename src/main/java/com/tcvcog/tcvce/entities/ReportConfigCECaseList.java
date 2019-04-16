/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;

/**
 *
 * @author sylvia
 */
public class ReportConfigCECaseList extends ReportConfig implements Serializable{
    
    private boolean includeListSummaryFigures;
    
    private boolean includeCaseNames;
    private boolean includeFullOwnerContactInfo;
    private boolean includeViolationList;
    private boolean includeEventSummaryByCase;
    
    private boolean includeExtendedPropertyDetails;

    /**
     * @return the includeListSummaryFigures
     */
    public boolean isIncludeListSummaryFigures() {
        return includeListSummaryFigures;
    }

    /**
     * @return the includeCaseNames
     */
    public boolean isIncludeCaseNames() {
        return includeCaseNames;
    }

    /**
     * @return the includeFullOwnerContactInfo
     */
    public boolean isIncludeFullOwnerContactInfo() {
        return includeFullOwnerContactInfo;
    }

    /**
     * @return the includeViolationList
     */
    public boolean isIncludeViolationList() {
        return includeViolationList;
    }

    /**
     * @return the includeEventSummaryByCase
     */
    public boolean isIncludeEventSummaryByCase() {
        return includeEventSummaryByCase;
    }

    /**
     * @param includeListSummaryFigures the includeListSummaryFigures to set
     */
    public void setIncludeListSummaryFigures(boolean includeListSummaryFigures) {
        this.includeListSummaryFigures = includeListSummaryFigures;
    }

    /**
     * @param includeCaseNames the includeCaseNames to set
     */
    public void setIncludeCaseNames(boolean includeCaseNames) {
        this.includeCaseNames = includeCaseNames;
    }

    /**
     * @param includeFullOwnerContactInfo the includeFullOwnerContactInfo to set
     */
    public void setIncludeFullOwnerContactInfo(boolean includeFullOwnerContactInfo) {
        this.includeFullOwnerContactInfo = includeFullOwnerContactInfo;
    }

    /**
     * @param includeViolationList the includeViolationList to set
     */
    public void setIncludeViolationList(boolean includeViolationList) {
        this.includeViolationList = includeViolationList;
    }

    /**
     * @param includeEventSummaryByCase the includeEventSummaryByCase to set
     */
    public void setIncludeEventSummaryByCase(boolean includeEventSummaryByCase) {
        this.includeEventSummaryByCase = includeEventSummaryByCase;
    }

    /**
     * @return the includeExtendedPropertyDetails
     */
    public boolean isIncludeExtendedPropertyDetails() {
        return includeExtendedPropertyDetails;
    }

    /**
     * @param includeExtendedPropertyDetails the includeExtendedPropertyDetails to set
     */
    public void setIncludeExtendedPropertyDetails(boolean includeExtendedPropertyDetails) {
        this.includeExtendedPropertyDetails = includeExtendedPropertyDetails;
    }
    
    
}
