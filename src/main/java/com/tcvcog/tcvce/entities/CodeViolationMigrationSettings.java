/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.entities.occupancy.FieldInspection;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Holds user preferences for migrating failed
 sourceInspection items to a CE case
 * 
 * @author Ellen Bascomb of Apartment 31Y
 */
public class CodeViolationMigrationSettings {
    
    private CodeViolationMigrationPathwayEnum pathway;
    private List<CodeViolation> violationListToMigrate;
    private List<CodeViolation> violationListReadyForInsertion;
    
    private UserAuthorized userEnactingMigration;
    
    private FieldInspection sourceInspection;
    private CECase sourceCase;
    private OccPeriod sourceOccPeriod;
    
    private CECaseDataHeavy ceCaseParent;
    private MunicipalityDataHeavy muniDH;
    private Property prop;
    private PropertyUnit propUnit;
    
    private LocalDateTime violationDateOfRecord;
    private LocalDateTime unifiedStipComplianceDate;
    private boolean useInspectedElementFindingsAsViolationFindings;
    private boolean linkInspectedElementPhotoDocsToViolation;
    private String newCECaseName;
    private User newCECaseManager;
    private LocalDateTime newCaseDateOfRecord;

    /**
     * No arg Constructor
     */
    public CodeViolationMigrationSettings(){
        
    }
    
    
    /**
     * @return the newCaseDateOfRecord
     */
    public LocalDateTime getNewCaseDateOfRecord() {
        return newCaseDateOfRecord;
    }

    /**
     * @param newCaseDateOfRecord the newCaseDateOfRecord to set
     */
    public void setNewCaseDateOfRecord(LocalDateTime newCaseDateOfRecord) {
        this.newCaseDateOfRecord = newCaseDateOfRecord;
    }

    /**
     * @return the violationDateOfRecord
     */
    public LocalDateTime getViolationDateOfRecord() {
        return violationDateOfRecord;
    }

    /**
     * @return the ceCaseParent
     */
    public CECaseDataHeavy getCeCaseParent() {
        return ceCaseParent;
    }

    /**
     * @return the useInspectedElementFindingsAsViolationFindings
     */
    public boolean isUseInspectedElementFindingsAsViolationFindings() {
        return useInspectedElementFindingsAsViolationFindings;
    }

    /**
     * @return the linkInspectedElementPhotoDocsToViolation
     */
    public boolean isLinkInspectedElementPhotoDocsToViolation() {
        return linkInspectedElementPhotoDocsToViolation;
    }

    /**
     * @return the newCECaseName
     */
    public String getNewCECaseName() {
        return newCECaseName;
    }

    /**
     * @return the newCECaseManager
     */
    public User getNewCECaseManager() {
        return newCECaseManager;
    }

    /**
     * @param violationDateOfRecord the violationDateOfRecord to set
     */
    public void setViolationDateOfRecord(LocalDateTime violationDateOfRecord) {
        this.violationDateOfRecord = violationDateOfRecord;
    }

    /**
     * @param ceCaseParent the ceCaseParent to set
     */
    public void setCeCaseParent(CECaseDataHeavy ceCaseParent) {
        this.ceCaseParent = ceCaseParent;
    }

    /**
     * @param useInspectedElementFindingsAsViolationFindings the useInspectedElementFindingsAsViolationFindings to set
     */
    public void setUseInspectedElementFindingsAsViolationFindings(boolean useInspectedElementFindingsAsViolationFindings) {
        this.useInspectedElementFindingsAsViolationFindings = useInspectedElementFindingsAsViolationFindings;
    }

    /**
     * @param linkInspectedElementPhotoDocsToViolation the linkInspectedElementPhotoDocsToViolation to set
     */
    public void setLinkInspectedElementPhotoDocsToViolation(boolean linkInspectedElementPhotoDocsToViolation) {
        this.linkInspectedElementPhotoDocsToViolation = linkInspectedElementPhotoDocsToViolation;
    }

    /**
     * @param newCECaseName the newCECaseName to set
     */
    public void setNewCECaseName(String newCECaseName) {
        this.newCECaseName = newCECaseName;
    }

    /**
     * @param newCECaseManager the newCECaseManager to set
     */
    public void setNewCECaseManager(User newCECaseManager) {
        this.newCECaseManager = newCECaseManager;
    }

    /**
     * @return the sourceInspection
     */
    public FieldInspection getSourceInspection() {
        return sourceInspection;
    }

    /**
     * @return the muniDH
     */
    public MunicipalityDataHeavy getMuniDH() {
        return muniDH;
    }

    /**
     * @param sourceInspection the sourceInspection to set
     */
    public void setSourceInspection(FieldInspection sourceInspection) {
        this.sourceInspection = sourceInspection;
    }

    /**
     * @param muniDH the muniDH to set
     */
    public void setMuniDH(MunicipalityDataHeavy muniDH) {
        this.muniDH = muniDH;
    }

    /**
     * @return the prop
     */
    public Property getProp() {
        return prop;
    }

    /**
     * @return the propUnit
     */
    public PropertyUnit getPropUnit() {
        return propUnit;
    }

    /**
     * @param prop the prop to set
     */
    public void setProp(Property prop) {
        this.prop = prop;
    }

    /**
     * @param propUnit the propUnit to set
     */
    public void setPropUnit(PropertyUnit propUnit) {
        this.propUnit = propUnit;
    }

    /**
     * @return the violationListToMigrate
     */
    public List<CodeViolation> getViolationListToMigrate() {
        return violationListToMigrate;
    }

    /**
     * @param violationListToMigrate the violationListToMigrate to set
     */
    public void setViolationListToMigrate(List<CodeViolation> violationListToMigrate) {
        this.violationListToMigrate = violationListToMigrate;
    }

    /**
     * @return the pathway
     */
    public CodeViolationMigrationPathwayEnum getPathway() {
        return pathway;
    }

    /**
     * @param pathway the pathway to set
     */
    public void setPathway(CodeViolationMigrationPathwayEnum pathway) {
        this.pathway = pathway;
    }

    /**
     * @return the violationListReadyForInsertion
     */
    public List<CodeViolation> getViolationListReadyForInsertion() {
        return violationListReadyForInsertion;
    }

    /**
     * @param violationListReadyForInsertion the violationListReadyForInsertion to set
     */
    public void setViolationListReadyForInsertion(List<CodeViolation> violationListReadyForInsertion) {
        this.violationListReadyForInsertion = violationListReadyForInsertion;
    }

    /**
     * @return the userEnactingMigration
     */
    public UserAuthorized getUserEnactingMigration() {
        return userEnactingMigration;
    }

    /**
     * @param userEnactingMigration the userEnactingMigration to set
     */
    public void setUserEnactingMigration(UserAuthorized userEnactingMigration) {
        this.userEnactingMigration = userEnactingMigration;
    }

    /**
     * @return the unifiedStipComplianceDate
     */
    public LocalDateTime getUnifiedStipComplianceDate() {
        return unifiedStipComplianceDate;
    }

    /**
     * @param unifiedStipComplianceDate the unifiedStipComplianceDate to set
     */
    public void setUnifiedStipComplianceDate(LocalDateTime unifiedStipComplianceDate) {
        this.unifiedStipComplianceDate = unifiedStipComplianceDate;
    }

    /**
     * @return the sourceCase
     */
    public CECase getSourceCase() {
        return sourceCase;
    }

    /**
     * @param sourceCase the sourceCase to set
     */
    public void setSourceCase(CECase sourceCase) {
        this.sourceCase = sourceCase;
    }

    /**
     * @return the sourceOccPeriod
     */
    public OccPeriod getSourceOccPeriod() {
        return sourceOccPeriod;
    }

    /**
     * @param sourceOccPeriod the sourceOccPeriod to set
     */
    public void setSourceOccPeriod(OccPeriod sourceOccPeriod) {
        this.sourceOccPeriod = sourceOccPeriod;
    }
    
    
    
    
    
}
