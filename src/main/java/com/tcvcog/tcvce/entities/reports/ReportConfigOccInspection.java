/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.reports;

import com.tcvcog.tcvce.entities.CECasePropertyUnitHeavy;
import com.tcvcog.tcvce.entities.Icon;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitWithProp;
import com.tcvcog.tcvce.entities.occupancy.FieldInspection;
import com.tcvcog.tcvce.entities.occupancy.OccInspectionViewOptions;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodPropertyUnitHeavy;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsOccChecklistItemsEnum;
import java.io.Serializable;

/**
 *
 * @author sylvia
 */
public class ReportConfigOccInspection 
        extends Report 
        implements Serializable{
    
    private FieldInspection inspection;
    
    private Property inspectedProperty;
    private PropertyUnit inspectedUnit;
    
    // The property and unit are extracted from one of these
    // based on the domain of the FIN
    private OccPeriodPropertyUnitHeavy occPeriod;
    private CECasePropertyUnitHeavy ceCase;
    
    private Person propertyOwner;
    private boolean includeOwnerInfo;
    private boolean includeOwnerPhones;
    private boolean includeOwnerEmails;
    private boolean includeOwnerAddresses;
    
    private boolean includeBedBathOccCounts;
    private Icon defaultItemIcon;
        
    private boolean includeParentObjectInfoHeader;
    
    private ViewOptionsOccChecklistItemsEnum viewSetting;
    
    private boolean includeInspectedSpaceLocation;

    private boolean includeOrdinanceInspectionTimestamps;
    private boolean includeFullOrdText;
    private boolean includeOrdinanceFindings;
    private boolean includeRemedyInfo;
    
    private boolean includePhotos_pass;
    private boolean includePhotos_fail;    
    private boolean includeOverallInspectionPhotos;
    private int unifiedPhotoWidth;
    
    private boolean includePhotoIDs;
    private boolean includePhotoTitles;
    private boolean includePhotoOriginalFileNames;
    private boolean includePhotoDescriptions;

    private boolean includeSignature;
    
    
   

    /**
     * @return the occPeriod
     */
    public OccPeriodPropertyUnitHeavy getOccPeriod() {
        return occPeriod;
    }

    /**
     * @return the includeParentObjectInfoHeader
     */
    public boolean isIncludeParentObjectInfoHeader() {
        return includeParentObjectInfoHeader;
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
     * @return the includeOrdinanceFindings
     */
    public boolean isIncludeOrdinanceFindings() {
        return includeOrdinanceFindings;
    }

    /**
     * @return the includeOrdinanceInspectionTimestamps
     */
    public boolean isIncludeOrdinanceInspectionTimestamps() {
        return includeOrdinanceInspectionTimestamps;
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
    public void setOccPeriod(OccPeriodPropertyUnitHeavy occPeriod) {
        this.occPeriod = occPeriod;
    }

    /**
     * @param includeParentObjectInfoHeader the includeParentObjectInfoHeader to set
     */
    public void setIncludeParentObjectInfoHeader(boolean includeParentObjectInfoHeader) {
        this.includeParentObjectInfoHeader = includeParentObjectInfoHeader;
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
     * @param includeOrdinanceFindings the includeOrdinanceFindings to set
     */
    public void setIncludeOrdinanceFindings(boolean includeOrdinanceFindings) {
        this.includeOrdinanceFindings = includeOrdinanceFindings;
    }

    /**
     * @param includeOrdinanceInspectionTimestamps the includeOrdinanceInspectionTimestamps to set
     */
    public void setIncludeOrdinanceInspectionTimestamps(boolean includeOrdinanceInspectionTimestamps) {
        this.includeOrdinanceInspectionTimestamps = includeOrdinanceInspectionTimestamps;
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
     * @return the inspection
     */
    public FieldInspection getInspection() {
        return inspection;
    }

    /**
     * @param inspection the inspection to set
     */
    public void setInspection(FieldInspection inspection) {
        this.inspection = inspection;
    }

    /**
     * @return the ceCase
     */
    public CECasePropertyUnitHeavy getCeCase() {
        return ceCase;
    }

    /**
     * @param ceCase the ceCase to set
     */
    public void setCeCase(CECasePropertyUnitHeavy ceCase) {
        this.ceCase = ceCase;
    }

    /**
     * @return the includeOverallInspectionPhotos
     */
    public boolean isIncludeOverallInspectionPhotos() {
        return includeOverallInspectionPhotos;
    }

    /**
     * @param includeOverallInspectionPhotos the includeOverallInspectionPhotos to set
     */
    public void setIncludeOverallInspectionPhotos(boolean includeOverallInspectionPhotos) {
        this.includeOverallInspectionPhotos = includeOverallInspectionPhotos;
    }

    /**
     * @return the includeBedBathOccCounts
     */
    public boolean isIncludeBedBathOccCounts() {
        return includeBedBathOccCounts;
    }

    /**
     * @param includeBedBathOccCounts the includeBedBathOccCounts to set
     */
    public void setIncludeBedBathOccCounts(boolean includeBedBathOccCounts) {
        this.includeBedBathOccCounts = includeBedBathOccCounts;
    }

    /**
     * @return the includePhotoIDs
     */
    public boolean isIncludePhotoIDs() {
        return includePhotoIDs;
    }

    /**
     * @return the includePhotoTitles
     */
    public boolean isIncludePhotoTitles() {
        return includePhotoTitles;
    }

    /**
     * @return the includePhotoOriginalFileNames
     */
    public boolean isIncludePhotoOriginalFileNames() {
        return includePhotoOriginalFileNames;
    }

    /**
     * @return the includePhotoDescriptions
     */
    public boolean isIncludePhotoDescriptions() {
        return includePhotoDescriptions;
    }

    /**
     * @param includePhotoIDs the includePhotoIDs to set
     */
    public void setIncludePhotoIDs(boolean includePhotoIDs) {
        this.includePhotoIDs = includePhotoIDs;
    }

    /**
     * @param includePhotoTitles the includePhotoTitles to set
     */
    public void setIncludePhotoTitles(boolean includePhotoTitles) {
        this.includePhotoTitles = includePhotoTitles;
    }

    /**
     * @param includePhotoOriginalFileNames the includePhotoOriginalFileNames to set
     */
    public void setIncludePhotoOriginalFileNames(boolean includePhotoOriginalFileNames) {
        this.includePhotoOriginalFileNames = includePhotoOriginalFileNames;
    }

    /**
     * @param includePhotoDescriptions the includePhotoDescriptions to set
     */
    public void setIncludePhotoDescriptions(boolean includePhotoDescriptions) {
        this.includePhotoDescriptions = includePhotoDescriptions;
    }

    /**
     * @return the unifiedPhotoWidth
     */
    public int getUnifiedPhotoWidth() {
        return unifiedPhotoWidth;
    }

    /**
     * @param unifiedPhotoWidth the unifiedPhotoWidth to set
     */
    public void setUnifiedPhotoWidth(int unifiedPhotoWidth) {
        this.unifiedPhotoWidth = unifiedPhotoWidth;
    }

    /**
     * @return the inspectedProperty
     */
    public Property getInspectedProperty() {
        return inspectedProperty;
    }

    /**
     * @param inspectedProperty the inspectedProperty to set
     */
    public void setInspectedProperty(Property inspectedProperty) {
        this.inspectedProperty = inspectedProperty;
    }

    /**
     * @return the inspectedUnit
     */
    public PropertyUnit getInspectedUnit() {
        return inspectedUnit;
    }

    /**
     * @param inspectedUnit the inspectedUnit to set
     */
    public void setInspectedUnit(PropertyUnit inspectedUnit) {
        this.inspectedUnit = inspectedUnit;
    }

    /**
     * @return the propertyOwner
     */
    public Person getPropertyOwner() {
        return propertyOwner;
    }

    /**
     * @param propertyOwner the propertyOwner to set
     */
    public void setPropertyOwner(Person propertyOwner) {
        this.propertyOwner = propertyOwner;
    }

    /**
     * @return the includeOwnerInfo
     */
    public boolean isIncludeOwnerInfo() {
        return includeOwnerInfo;
    }

    /**
     * @return the includeOwnerPhones
     */
    public boolean isIncludeOwnerPhones() {
        return includeOwnerPhones;
    }

    /**
     * @return the includeOwnerEmails
     */
    public boolean isIncludeOwnerEmails() {
        return includeOwnerEmails;
    }

    /**
     * @return the includeOwnerAddresses
     */
    public boolean isIncludeOwnerAddresses() {
        return includeOwnerAddresses;
    }

    /**
     * @param includeOwnerInfo the includeOwnerInfo to set
     */
    public void setIncludeOwnerInfo(boolean includeOwnerInfo) {
        this.includeOwnerInfo = includeOwnerInfo;
    }

    /**
     * @param includeOwnerPhones the includeOwnerPhones to set
     */
    public void setIncludeOwnerPhones(boolean includeOwnerPhones) {
        this.includeOwnerPhones = includeOwnerPhones;
    }

    /**
     * @param includeOwnerEmails the includeOwnerEmails to set
     */
    public void setIncludeOwnerEmails(boolean includeOwnerEmails) {
        this.includeOwnerEmails = includeOwnerEmails;
    }

    /**
     * @param includeOwnerAddresses the includeOwnerAddresses to set
     */
    public void setIncludeOwnerAddresses(boolean includeOwnerAddresses) {
        this.includeOwnerAddresses = includeOwnerAddresses;
    }

    /**
     * @return the includeInspectedSpaceLocation
     */
    public boolean isIncludeInspectedSpaceLocation() {
        return includeInspectedSpaceLocation;
    }

    /**
     * @param includeInspectedSpaceLocation the includeInspectedSpaceLocation to set
     */
    public void setIncludeInspectedSpaceLocation(boolean includeInspectedSpaceLocation) {
        this.includeInspectedSpaceLocation = includeInspectedSpaceLocation;
    }

    
}
