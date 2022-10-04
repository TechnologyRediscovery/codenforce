/*
 * Copyright (C) 2022 Technology Rediscovery LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.IFace_inspectable;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.occupancy.FieldInspection;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.util.Constants;
import java.time.LocalDateTime;

/**
 * Holder of settings for configuring the reinspection
 * of a given inspection
 * 
 * @author Ellen Bascomb of Apartment 31Y
 */
public class FieldInspectionReInspectionConfig {
    
    private FieldInspection sourceInspection;
    private IFace_inspectable inspectable;
    private FieldInspection reInspection;
    
    private LocalDateTime reinspectionDate;
    private User reinspector;
    private UserAuthorized requestingUser;

    // Reinspection settings switches
    protected boolean maintainSameParentObject;
    protected CECase changeTargetToCECase;
    protected OccPeriod changeTargetToOccPeriod;
    
    protected boolean migrateInspectionFindings;    
    protected boolean literalFindingsCloneNoWrapper;    
    
    private boolean migrateInspectedSpacesWithoutAnyInspectedItems;
    private boolean migrateInspectedSpacesWithoutAnyFailedItems;
    
    private boolean migrateOnlyFailedElements;
    private boolean migrateUninspectedElements;
    
    private boolean migrateBlobsOnSourceInspection;
    private boolean migrateBlobsOnFailedItems;
    private boolean migrateBlobsOnPassedItems;
    
    
    
    private StringBuilder log;
    
    
    /**
     * Tacks the given string onto the train
     * and then an html break
     * @param str 
     * @param newline whether or not to append an html br
     */
    public void appendToReinspectionLog(String str, boolean newline){
        if(log == null){
            log = new StringBuilder();
        }
        log.append(str);
        if(newline){
            log.append(Constants.FMT_HTML_BREAK);
        }
    }
    
    /**
     * Asks the String train for itself
     * @return 
     */
    public String getLog(){
        if(log != null){
            return log.toString();
        } else {
            return "EMTPY LOG;";
        }
    }
    
    /**
     * @return the sourceInspection
     */
    public FieldInspection getSourceInspection() {
        return sourceInspection;
    }

    /**
     * @return the reInspection
     */
    public FieldInspection getReInspection() {
        return reInspection;
    }

    /**
     * @return the migrateInspectedSpacesWithoutAnyFailedItems
     */
    public boolean isMigrateInspectedSpacesWithoutAnyFailedItems() {
        return migrateInspectedSpacesWithoutAnyFailedItems;
    }

    /**
     * @return the reinspectionDate
     */
    public LocalDateTime getReinspectionDate() {
        return reinspectionDate;
    }

    /**
     * @param sourceInspection the sourceInspection to set
     */
    public void setSourceInspection(FieldInspection sourceInspection) {
        this.sourceInspection = sourceInspection;
    }

    /**
     * @param reInspection the reInspection to set
     */
    public void setReInspection(FieldInspection reInspection) {
        this.reInspection = reInspection;
    }

    /**
     * @param migrateInspectedSpacesWithoutAnyFailedItems the migrateInspectedSpacesWithoutAnyFailedItems to set
     */
    public void setMigrateInspectedSpacesWithoutAnyFailedItems(boolean migrateInspectedSpacesWithoutAnyFailedItems) {
        this.migrateInspectedSpacesWithoutAnyFailedItems = migrateInspectedSpacesWithoutAnyFailedItems;
    }

    /**
     * @param reinspectionDate the reinspectionDate to set
     */
    public void setReinspectionDate(LocalDateTime reinspectionDate) {
        this.reinspectionDate = reinspectionDate;
    }

    /**
     * @return the inspectable
     */
    public IFace_inspectable getInspectable() {
        return inspectable;
    }

    /**
     * @param inspectable the inspectable to set
     */
    public void setInspectable(IFace_inspectable inspectable) {
        this.inspectable = inspectable;
    }

    /**
     * @return the reinspector
     */
    public User getReinspector() {
        return reinspector;
    }

    /**
     * @return the requestingUser
     */
    public UserAuthorized getRequestingUser() {
        return requestingUser;
    }

    /**
     * @param reinspector the reinspector to set
     */
    public void setReinspector(User reinspector) {
        this.reinspector = reinspector;
    }

    /**
     * @param requestingUser the requestingUser to set
     */
    public void setRequestingUser(UserAuthorized requestingUser) {
        this.requestingUser = requestingUser;
    }

    /**
     * @return the migrateBlobsOnSourceInspection
     */
    public boolean isMigrateBlobsOnSourceInspection() {
        return migrateBlobsOnSourceInspection;
    }

    /**
     * @return the migrateBlobsOnFailedItems
     */
    public boolean isMigrateBlobsOnFailedItems() {
        return migrateBlobsOnFailedItems;
    }

    /**
     * @param migrateBlobsOnSourceInspection the migrateBlobsOnSourceInspection to set
     */
    public void setMigrateBlobsOnSourceInspection(boolean migrateBlobsOnSourceInspection) {
        this.migrateBlobsOnSourceInspection = migrateBlobsOnSourceInspection;
    }

    /**
     * @param migrateBlobsOnFailedItems the migrateBlobsOnFailedItems to set
     */
    public void setMigrateBlobsOnFailedItems(boolean migrateBlobsOnFailedItems) {
        this.migrateBlobsOnFailedItems = migrateBlobsOnFailedItems;
    }

    /**
     * @return the migrateOnlyFailedElements
     */
    public boolean isMigrateOnlyFailedElements() {
        return migrateOnlyFailedElements;
    }

    /**
     * @param migrateOnlyFailedElements the migrateOnlyFailedElements to set
     */
    public void setMigrateOnlyFailedElements(boolean migrateOnlyFailedElements) {
        this.migrateOnlyFailedElements = migrateOnlyFailedElements;
    }

    /**
     * @return the migrateBlobsOnPassedItems
     */
    public boolean isMigrateBlobsOnPassedItems() {
        return migrateBlobsOnPassedItems;
    }

    /**
     * @param migrateBlobsOnPassedItems the migrateBlobsOnPassedItems to set
     */
    public void setMigrateBlobsOnPassedItems(boolean migrateBlobsOnPassedItems) {
        this.migrateBlobsOnPassedItems = migrateBlobsOnPassedItems;
    }

    /**
     * @return the migrateUninspectedElements
     */
    public boolean isMigrateUninspectedElements() {
        return migrateUninspectedElements;
    }

    /**
     * @param migrateUninspectedElements the migrateUninspectedElements to set
     */
    public void setMigrateUninspectedElements(boolean migrateUninspectedElements) {
        this.migrateUninspectedElements = migrateUninspectedElements;
    }

    /**
     * @return the migrateInspectedSpacesWithoutAnyInspectedItems
     */
    public boolean isMigrateInspectedSpacesWithoutAnyInspectedItems() {
        return migrateInspectedSpacesWithoutAnyInspectedItems;
    }

    /**
     * @param migrateInspectedSpacesWithoutAnyInspectedItems the migrateInspectedSpacesWithoutAnyInspectedItems to set
     */
    public void setMigrateInspectedSpacesWithoutAnyInspectedItems(boolean migrateInspectedSpacesWithoutAnyInspectedItems) {
        this.migrateInspectedSpacesWithoutAnyInspectedItems = migrateInspectedSpacesWithoutAnyInspectedItems;
    }

    /**
     * @return the maintainSameParentObject
     */
    public boolean isMaintainSameParentObject() {
        return maintainSameParentObject;
    }

    /**
     * @return the changeTargetToCECase
     */
    public CECase getChangeTargetToCECase() {
        return changeTargetToCECase;
    }

    /**
     * @return the changeTargetToOccPeriod
     */
    public OccPeriod getChangeTargetToOccPeriod() {
        return changeTargetToOccPeriod;
    }

    /**
     * @return the migrateInspectionFindings
     */
    public boolean isMigrateInspectionFindings() {
        return migrateInspectionFindings;
    }

    /**
     * @return the literalFindingsCloneNoWrapper
     */
    public boolean isLiteralFindingsCloneNoWrapper() {
        return literalFindingsCloneNoWrapper;
    }

  

    /**
     * @param maintainSameParentObject the maintainSameParentObject to set
     */
    public void setMaintainSameParentObject(boolean maintainSameParentObject) {
        this.maintainSameParentObject = maintainSameParentObject;
    }

    /**
     * @param changeTargetToCECase the changeTargetToCECase to set
     */
    public void setChangeTargetToCECase(CECase changeTargetToCECase) {
        this.changeTargetToCECase = changeTargetToCECase;
    }

    /**
     * @param changeTargetToOccPeriod the changeTargetToOccPeriod to set
     */
    public void setChangeTargetToOccPeriod(OccPeriod changeTargetToOccPeriod) {
        this.changeTargetToOccPeriod = changeTargetToOccPeriod;
    }

    /**
     * @param migrateInspectionFindings the migrateInspectionFindings to set
     */
    public void setMigrateInspectionFindings(boolean migrateInspectionFindings) {
        this.migrateInspectionFindings = migrateInspectionFindings;
    }

    /**
     * @param literalFindingsCloneNoWrapper the literalFindingsCloneNoWrapper to set
     */
    public void setLiteralFindingsCloneNoWrapper(boolean literalFindingsCloneNoWrapper) {
        this.literalFindingsCloneNoWrapper = literalFindingsCloneNoWrapper;
    }

    
    
}
