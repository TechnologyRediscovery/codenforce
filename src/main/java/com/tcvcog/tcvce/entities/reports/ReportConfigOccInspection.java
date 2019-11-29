/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.reports;

import com.tcvcog.tcvce.entities.Icon;
import com.tcvcog.tcvce.entities.PropertyUnitWithProp;
import com.tcvcog.tcvce.entities.occupancy.OccInspectionViewOptions;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsOccChecklistItemsEnum;
import java.io.Serializable;

/**
 *
 * @author sylvia
 */
public class ReportConfigOccInspection 
        extends Report 
        implements Serializable{
    
    private OccPeriod occPeriod;
    private PropertyUnitWithProp propUnitWithProp;
    
    private Icon defaultItemIcon;
        
    private boolean includeOccPeriodInfoHeader;
    
    private ViewOptionsOccChecklistItemsEnum viewSetting;
    
    private boolean includePhotos_pass;
    private boolean includePhotos_fail;
    
    private boolean includeFullOrdText;
    private boolean includeElementNotes;
    
    private boolean includeElementLastInspectedInfo;
    private boolean includeElementComplianceInfo;
   
    private boolean includeRemedyInfo;
    private boolean includeSignature;

    /**
     * @return the occPeriod
     */
    public OccPeriod getOccPeriod() {
        return occPeriod;
    }

    /**
     * @return the includeOccPeriodInfoHeader
     */
    public boolean isIncludeOccPeriodInfoHeader() {
        return includeOccPeriodInfoHeader;
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
     * @return the includeRemedyInfo
     */
    public boolean isIncludeRemedyInfo() {
        return includeRemedyInfo;
    }

    /**
     * @return the includeSignature
     */
    public boolean isIncludeSignature() {
        return includeSignature;
    }

    /**
     * @param occPeriod the occPeriod to set
     */
    public void setOccPeriod(OccPeriod occPeriod) {
        this.occPeriod = occPeriod;
    }

    /**
     * @param includeOccPeriodInfoHeader the includeOccPeriodInfoHeader to set
     */
    public void setIncludeOccPeriodInfoHeader(boolean includeOccPeriodInfoHeader) {
        this.includeOccPeriodInfoHeader = includeOccPeriodInfoHeader;
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
     * @param includeRemedyInfo the includeRemedyInfo to set
     */
    public void setIncludeRemedyInfo(boolean includeRemedyInfo) {
        this.includeRemedyInfo = includeRemedyInfo;
    }

    /**
     * @param includeSignature the includeSignature to set
     */
    public void setIncludeSignature(boolean includeSignature) {
        this.includeSignature = includeSignature;
    }

    /**
     * @return the viewSetting
     */
    public ViewOptionsOccChecklistItemsEnum getViewSetting() {
        return viewSetting;
    }

    /**
     * @param viewSetting the viewSetting to set
     */
    public void setViewSetting(ViewOptionsOccChecklistItemsEnum viewSetting) {
        this.viewSetting = viewSetting;
    }

    /**
     * @return the defaultItemIcon
     */
    public Icon getDefaultItemIcon() {
        return defaultItemIcon;
    }

    /**
     * @param defaultItemIcon the defaultItemIcon to set
     */
    public void setDefaultItemIcon(Icon defaultItemIcon) {
        this.defaultItemIcon = defaultItemIcon;
    }

    /**
     * @return the propUnitWithProp
     */
    public PropertyUnitWithProp getPropUnitWithProp() {
        return propUnitWithProp;
    }

    /**
     * @param propUnitWithProp the propUnitWithProp to set
     */
    public void setPropUnitWithProp(PropertyUnitWithProp propUnitWithProp) {
        this.propUnitWithProp = propUnitWithProp;
    }
}
