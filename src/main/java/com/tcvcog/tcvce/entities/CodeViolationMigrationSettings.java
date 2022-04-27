/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.entities.occupancy.FieldInspection;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds user preferences for migrating failed
 sourceInspection items to a CE case
 * 
 * @author Ellen Bascomb of Apartment 31Y
 */

public class CodeViolationMigrationSettings {
    
    final static String HTML_BREAK = "<br />";
    final static String SEP = ": ";
    
    private CodeViolationMigrationPathwayEnum pathway;
    private List<CodeViolation> violationListToMigrate;
    private List<CodeViolation> violationListReadyForInsertion;
    private List<CodeViolation> violationListSuccessfullyMigrated;
    
    private UserAuthorized userEnactingMigration;
    
    private FieldInspection sourceInspection;
    private List<IFace_transferrable> transferrableList;
    private CECase sourceCase;
    private OccPeriod sourceOccPeriod;
    
    private CECasePropertyUnitHeavy ceCaseParent;
    private MunicipalityDataHeavy muniDH;
    
    private LocalDateTime violationDateOfRecord;
    private LocalDateTime unifiedStipComplianceDate;
    private boolean useInspectedElementFindingsAsViolationFindings;
    private boolean linkInspectedElementPhotoDocsToViolation;
    private boolean migrateWithoutMarkingSourceViolsAsTransferred;
    
    private Property prop;
    private PropertyUnit newCasePropUnit;
    private String newCECaseName;
    private boolean newCECaseUnitAssociated;
    private User newCECaseManager;
    private EventCategory newCECaseOriginationEventCategory;
    private LocalDateTime newCaseDateOfRecord;
    
    private StringBuilder migrationLogSB;
    
    
    /**
     * Asks the internal StringBuilder for its string
     * @return the migrationLog
     */
    public String getMigrationLog() {
        if(migrationLogSB != null){
            return migrationLogSB.toString();
        } else {
            return "[Empty Log]";
        }
    }

    /**
     * Appends input to the internal StringBuilder, 
     * and adds HTML breaks and timestamps 
     * @param logData
     */
    public void appendToMigrationLog(String logData) {
        int nano = LocalDateTime.now().getNano();
        if(migrationLogSB == null){
            migrationLogSB = new StringBuilder();
        } 
        migrationLogSB.append(nano);
        migrationLogSB.append(SEP);
        migrationLogSB.append(logData);
        migrationLogSB.append(HTML_BREAK);
    }
  
    

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
    public CECasePropertyUnitHeavy getCeCaseParent() {
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
    public void setCeCaseParent(CECasePropertyUnitHeavy ceCaseParent) {
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
     * @return the newCasePropUnit
     */
    public PropertyUnit getNewCasePropUnit() {
        return newCasePropUnit;
    }

    /**
     * @param prop the prop to set
     */
    public void setProp(Property prop) {
        this.prop = prop;
    }

    /**
     * @param newCasePropUnit the newCasePropUnit to set
     */
    public void setNewCasePropUnit(PropertyUnit newCasePropUnit) {
        this.newCasePropUnit = newCasePropUnit;
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

    /**
     * Casts all the code violations to transferrables
     * @return the transferrableList
     */
    public List<IFace_transferrable> getTransferrableList() {
       List<IFace_transferrable> trl = new ArrayList<>(); 
       if(violationListToMigrate != null && !violationListToMigrate.isEmpty()){
           for(CodeViolation cv: violationListToMigrate){
               trl.add((IFace_transferrable) cv);
           }
       }
        
        return trl;
     
        
    }

    /**
     * @param transferrableList the transferrableList to set
     */
    public void setTransferrableList(List<IFace_transferrable> transferrableList) {
        this.transferrableList = transferrableList;
    }

    /**
     * @return the newCECaseOriginationEventCategory
     */
    public EventCategory getNewCECaseOriginationEventCategory() {
        return newCECaseOriginationEventCategory;
    }

    /**
     * @param newCECaseOriginationEventCategory the newCECaseOriginationEventCategory to set
     */
    public void setNewCECaseOriginationEventCategory(EventCategory newCECaseOriginationEventCategory) {
        this.newCECaseOriginationEventCategory = newCECaseOriginationEventCategory;
    }

    /**
     * @return the newCECaseUnitAssociated
     */
    public boolean isNewCECaseUnitAssociated() {
        return newCECaseUnitAssociated;
    }

    /**
     * @param newCECaseUnitAssociated the newCECaseUnitAssociated to set
     */
    public void setNewCECaseUnitAssociated(boolean newCECaseUnitAssociated) {
        this.newCECaseUnitAssociated = newCECaseUnitAssociated;
    }

    /**
     * @return the violationListSuccessfullyMigrated
     */
    public List<CodeViolation> getViolationListSuccessfullyMigrated() {
        return violationListSuccessfullyMigrated;
    }

    /**
     * @param violationListSuccessfullyMigrated the violationListSuccessfullyMigrated to set
     */
    public void setViolationListSuccessfullyMigrated(List<CodeViolation> violationListSuccessfullyMigrated) {
        this.violationListSuccessfullyMigrated = violationListSuccessfullyMigrated;
    }

    /**
     * @return the migrateWithoutMarkingSourceViolsAsTransferred
     */
    public boolean isMigrateWithoutMarkingSourceViolsAsTransferred() {
        return migrateWithoutMarkingSourceViolsAsTransferred;
    }

    /**
     * @param migrateWithoutMarkingSourceViolsAsTransferred the migrateWithoutMarkingSourceViolsAsTransferred to set
     */
    public void setMigrateWithoutMarkingSourceViolsAsTransferred(boolean migrateWithoutMarkingSourceViolsAsTransferred) {
        this.migrateWithoutMarkingSourceViolsAsTransferred = migrateWithoutMarkingSourceViolsAsTransferred;
    }

 
    
    
    
}
