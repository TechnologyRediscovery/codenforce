/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.reports;

import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.search.Query;
import com.tcvcog.tcvce.entities.search.QueryBacked;
import com.tcvcog.tcvce.entities.search.QueryCEAR;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author sylvia
 */
public class ReportConfigOccInspection 
        extends Report 
        implements Serializable{
    
    private OccPeriod reportPeriod;
    
    private boolean includeOccPeriodInfoHeader;
    
    private boolean includeElements_notInspected;
    private boolean includeElements_pass;
    private boolean includeElements_fail;
    
    private boolean includePhotos_pass;
    private boolean includePhotos_fail;
    
    private boolean separateElementsBySpace;
    private boolean includeFullOrdText;
    private boolean includeElementNotes;
    
    private boolean includeElementLastInspectedInfo;
    private boolean includeElementComplianceInfo;
   
    private boolean includeFullInpsectedSpaceList;
    private boolean includeNextStepText;
    private boolean includeSignature;

    /**
     * @return the reportPeriod
     */
    public OccPeriod getReportPeriod() {
        return reportPeriod;
    }

    /**
     * @return the includeOccPeriodInfoHeader
     */
    public boolean isIncludeOccPeriodInfoHeader() {
        return includeOccPeriodInfoHeader;
    }

    /**
     * @return the includeElements_notInspected
     */
    public boolean isIncludeElements_notInspected() {
        return includeElements_notInspected;
    }

    /**
     * @return the includeElements_pass
     */
    public boolean isIncludeElements_pass() {
        return includeElements_pass;
    }

    /**
     * @return the includeElements_fail
     */
    public boolean isIncludeElements_fail() {
        return includeElements_fail;
    }

    /**
     * @return the includePhotos_pass
     */
    public boolean isIncludePhotos_pass() {
        return includePhotos_pass;
    }

    /**
     * @return the includePhotos_fail
     */
    public boolean isIncludePhotos_fail() {
        return includePhotos_fail;
    }

    /**
     * @return the separateElementsBySpace
     */
    public boolean isSeparateElementsBySpace() {
        return separateElementsBySpace;
    }

    /**
     * @return the includeFullOrdText
     */
    public boolean isIncludeFullOrdText() {
        return includeFullOrdText;
    }

    /**
     * @return the includeElementNotes
     */
    public boolean isIncludeElementNotes() {
        return includeElementNotes;
    }

    /**
     * @return the includeElementLastInspectedInfo
     */
    public boolean isIncludeElementLastInspectedInfo() {
        return includeElementLastInspectedInfo;
    }

    /**
     * @return the includeElementComplianceInfo
     */
    public boolean isIncludeElementComplianceInfo() {
        return includeElementComplianceInfo;
    }

    /**
     * @return the includeFullInpsectedSpaceList
     */
    public boolean isIncludeFullInpsectedSpaceList() {
        return includeFullInpsectedSpaceList;
    }

    /**
     * @return the includeNextStepText
     */
    public boolean isIncludeNextStepText() {
        return includeNextStepText;
    }

    /**
     * @return the includeSignature
     */
    public boolean isIncludeSignature() {
        return includeSignature;
    }

    /**
     * @param reportPeriod the reportPeriod to set
     */
    public void setReportPeriod(OccPeriod reportPeriod) {
        this.reportPeriod = reportPeriod;
    }

    /**
     * @param includeOccPeriodInfoHeader the includeOccPeriodInfoHeader to set
     */
    public void setIncludeOccPeriodInfoHeader(boolean includeOccPeriodInfoHeader) {
        this.includeOccPeriodInfoHeader = includeOccPeriodInfoHeader;
    }

    /**
     * @param includeElements_notInspected the includeElements_notInspected to set
     */
    public void setIncludeElements_notInspected(boolean includeElements_notInspected) {
        this.includeElements_notInspected = includeElements_notInspected;
    }

    /**
     * @param includeElements_pass the includeElements_pass to set
     */
    public void setIncludeElements_pass(boolean includeElements_pass) {
        this.includeElements_pass = includeElements_pass;
    }

    /**
     * @param includeElements_fail the includeElements_fail to set
     */
    public void setIncludeElements_fail(boolean includeElements_fail) {
        this.includeElements_fail = includeElements_fail;
    }

    /**
     * @param includePhotos_pass the includePhotos_pass to set
     */
    public void setIncludePhotos_pass(boolean includePhotos_pass) {
        this.includePhotos_pass = includePhotos_pass;
    }

    /**
     * @param includePhotos_fail the includePhotos_fail to set
     */
    public void setIncludePhotos_fail(boolean includePhotos_fail) {
        this.includePhotos_fail = includePhotos_fail;
    }

    /**
     * @param separateElementsBySpace the separateElementsBySpace to set
     */
    public void setSeparateElementsBySpace(boolean separateElementsBySpace) {
        this.separateElementsBySpace = separateElementsBySpace;
    }

    /**
     * @param includeFullOrdText the includeFullOrdText to set
     */
    public void setIncludeFullOrdText(boolean includeFullOrdText) {
        this.includeFullOrdText = includeFullOrdText;
    }

    /**
     * @param includeElementNotes the includeElementNotes to set
     */
    public void setIncludeElementNotes(boolean includeElementNotes) {
        this.includeElementNotes = includeElementNotes;
    }

    /**
     * @param includeElementLastInspectedInfo the includeElementLastInspectedInfo to set
     */
    public void setIncludeElementLastInspectedInfo(boolean includeElementLastInspectedInfo) {
        this.includeElementLastInspectedInfo = includeElementLastInspectedInfo;
    }

    /**
     * @param includeElementComplianceInfo the includeElementComplianceInfo to set
     */
    public void setIncludeElementComplianceInfo(boolean includeElementComplianceInfo) {
        this.includeElementComplianceInfo = includeElementComplianceInfo;
    }

    /**
     * @param includeFullInpsectedSpaceList the includeFullInpsectedSpaceList to set
     */
    public void setIncludeFullInpsectedSpaceList(boolean includeFullInpsectedSpaceList) {
        this.includeFullInpsectedSpaceList = includeFullInpsectedSpaceList;
    }

    /**
     * @param includeNextStepText the includeNextStepText to set
     */
    public void setIncludeNextStepText(boolean includeNextStepText) {
        this.includeNextStepText = includeNextStepText;
    }

    /**
     * @param includeSignature the includeSignature to set
     */
    public void setIncludeSignature(boolean includeSignature) {
        this.includeSignature = includeSignature;
    }
}
