/*
 * Copyright (C) 2018 Adam Gutonski 
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
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.BOb;
import com.tcvcog.tcvce.entities.EntityUtils;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsOccChecklistItemsEnum;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Adam Gutonski and Sylvia
 */
public class OccInspection extends BOb implements Comparable<OccInspection> {
    
    private int inspectionID;
    private User inspector;
    private int occPeriodID;
    
    private boolean active;
    
    // This template object provides us the raw lists of uninspected
    // space types, from which we extract a list of Spaces and their CodeElements
    private OccChecklistTemplate checklistTemplate;
    
    private List<OccInspectedSpace> inspectedSpaceList;
    private List<OccInspectedSpace> inspectedSpaceListVisible;
    private ViewOptionsOccChecklistItemsEnum viewSetting;
    private boolean includeEmtpySpaces;
    
    private int pacc;
    private boolean enablePacc;
    
    private boolean readyForPassedCertification;
    private User passedInspectionCertifiedBy;
    private LocalDateTime passedInspectionTS;
    
    private LocalDateTime effectiveDateOfRecord;
    protected java.util.Date effectiveDateOfRecordUtilDate;
    
    private LocalDateTime creationTS;
    
    private int maxOccupantsAllowed;
    private int numBedrooms;
    private int numBathrooms;
    
    private Person thirdPartyInspector;
    private LocalDateTime thirdPartyInspectorApprovalTS;
    private User thirdPartyApprovalBy;

    private String notes; 
    
    public OccInspection(){
        inspectedSpaceList = new ArrayList<>();
        inspectedSpaceListVisible = new ArrayList<>();
        viewSetting = ViewOptionsOccChecklistItemsEnum.ALL_ITEMS;
    }
    
    public void addSpaceToInspectedSpaces(OccInspectedSpace spc){
        inspectedSpaceList.add(spc);
    }
    
    public void configureVisibleSpaceElementList(){
        inspectedSpaceListVisible.clear();
        for(Iterator<OccInspectedSpace> it = inspectedSpaceList.iterator(); it.hasNext(); ){
            OccInspectedSpace ois = it.next(); 
            ois.setViewSetting(viewSetting);
            ois.configureVisibleElementList();
            if(!ois.getInspectedElementListVisible().isEmpty()
                    || (ois.getInspectedElementListVisible().isEmpty() && includeEmtpySpaces)){
                inspectedSpaceListVisible.add(ois);
            }
        } // close for over inspectedspaces
    }
    
    /**
     * @return the inspectionID
     */
    public int getInspectionID() {
        return inspectionID;
    }

    /**
     * @param inspectionID the inspectionID to set
     */
    public void setInspectionID(int inspectionID) {
        this.inspectionID = inspectionID;
    }


    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the inspector
     */
    public User getInspector() {
        return inspector;
    }

    /**
     * @param inspector the inspector to set
     */
    public void setInspector(User inspector) {
        this.inspector = inspector;
    }


    /**
     * @return the pacc
     */
    public int getPacc() {
        return pacc;
    }

    /**
     * @return the enablePacc
     */
    public boolean isEnablePacc() {
        return enablePacc;
    }


    /**
     * @param pacc the pacc to set
     */
    public void setPacc(int pacc) {
        this.pacc = pacc;
    }

    /**
     * @param enablePacc the enablePacc to set
     */
    public void setEnablePacc(boolean enablePacc) {
        this.enablePacc = enablePacc;
    }

    public List<OccInspectedSpace> getInspectedSpaceList() {
        return inspectedSpaceList;
    }

   

    /**
     * @return the checklistTemplate
     */
    public OccChecklistTemplate getChecklistTemplate() {
        return checklistTemplate;
    }

    /**
     * @param checklistTemplate the checklistTemplate to set
     */
    public void setChecklistTemplate(OccChecklistTemplate checklistTemplate) {
        this.checklistTemplate = checklistTemplate;
    }

    /**
     * @return the occPeriodID
     */
    public int getOccPeriodID() {
        return occPeriodID;
    }

    /**
     * @param occPeriodID the occPeriodID to set
     */
    public void setOccPeriodID(int occPeriodID) {
        this.occPeriodID = occPeriodID;
    }

    /**
     * @return the passedInspectionCertifiedBy
     */
    public User getPassedInspectionCertifiedBy() {
        return passedInspectionCertifiedBy;
    }

    /**
     * @param passedInspectionCertifiedBy the passedInspectionCertifiedBy to set
     */
    public void setPassedInspectionCertifiedBy(User passedInspectionCertifiedBy) {
        this.passedInspectionCertifiedBy = passedInspectionCertifiedBy;
    }

    /**
     * @return the maxOccupantsAllowed
     */
    public int getMaxOccupantsAllowed() {
        return maxOccupantsAllowed;
    }

    /**
     * @return the numBedrooms
     */
    public int getNumBedrooms() {
        return numBedrooms;
    }

    /**
     * @return the numBathrooms
     */
    public int getNumBathrooms() {
        return numBathrooms;
    }

    /**
     * @param maxOccupantsAllowed the maxOccupantsAllowed to set
     */
    public void setMaxOccupantsAllowed(int maxOccupantsAllowed) {
        this.maxOccupantsAllowed = maxOccupantsAllowed;
    }

    /**
     * @param numBedrooms the numBedrooms to set
     */
    public void setNumBedrooms(int numBedrooms) {
        this.numBedrooms = numBedrooms;
    }

    /**
     * @param numBathrooms the numBathrooms to set
     */
    public void setNumBathrooms(int numBathrooms) {
        this.numBathrooms = numBathrooms;
    }

    /**
     * @return the thirdPartyInspector
     */
    public Person getThirdPartyInspector() {
        return thirdPartyInspector;
    }

    /**
     * @return the thirdPartyInspectorApprovalTS
     */
    public LocalDateTime getThirdPartyInspectorApprovalTS() {
        return thirdPartyInspectorApprovalTS;
    }

    /**
     * @return the thirdPartyApprovalBy
     */
    public User getThirdPartyApprovalBy() {
        return thirdPartyApprovalBy;
    }

    /**
     * @param thirdPartyInspector the thirdPartyInspector to set
     */
    public void setThirdPartyInspector(Person thirdPartyInspector) {
        this.thirdPartyInspector = thirdPartyInspector;
    }

    /**
     * @param thirdPartyInspectorApprovalTS the thirdPartyInspectorApprovalTS to set
     */
    public void setThirdPartyInspectorApprovalTS(LocalDateTime thirdPartyInspectorApprovalTS) {
        this.thirdPartyInspectorApprovalTS = thirdPartyInspectorApprovalTS;
    }

    /**
     * @param thirdPartyApprovalBy the thirdPartyApprovalBy to set
     */
    public void setThirdPartyApprovalBy(User thirdPartyApprovalBy) {
        this.thirdPartyApprovalBy = thirdPartyApprovalBy;
    }

    /**
     * @param inspectedSpaceList the inspectedSpaceList to set
     */
    public void setInspectedSpaceList(List<OccInspectedSpace> inspectedSpaceList) {
        this.inspectedSpaceList = inspectedSpaceList;
    }

    /**
     * @return the passedInspectionTS
     */
    public LocalDateTime getPassedInspectionTS() {
        return passedInspectionTS;
    }
    
    public String getPassedInspectionTSPretty(){
        return EntityUtils.getPrettyDate(passedInspectionTS);
    }

    /**
     * @param passedInspectionTS the passedInspectionTS to set
     */
    public void setPassedInspectionTS(LocalDateTime passedInspectionTS) {
        this.passedInspectionTS = passedInspectionTS;
    }

    /**
     * @return the effectiveDateOfRecord
     */
    public LocalDateTime getEffectiveDateOfRecord() {
        return effectiveDateOfRecord;
    }
    
    public String getEffectiveDateOfRecordPretty(){
        return EntityUtils.getPrettyDate(effectiveDateOfRecord);
    }

    /**
     * @param effectiveDateOfRecord the effectiveDateOfRecord to set
     */
    public void setEffectiveDateOfRecord(LocalDateTime effectiveDateOfRecord) {
        this.effectiveDateOfRecord = effectiveDateOfRecord;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.inspectionID;
        hash = 53 * hash + Objects.hashCode(this.inspector);
        hash = 53 * hash + this.occPeriodID;
        hash = 53 * hash + Objects.hashCode(this.checklistTemplate);
        hash = 53 * hash + Objects.hashCode(this.inspectedSpaceList);
        hash = 53 * hash + this.pacc;
        hash = 53 * hash + (this.enablePacc ? 1 : 0);
        hash = 53 * hash + Objects.hashCode(this.passedInspectionCertifiedBy);
        hash = 53 * hash + Objects.hashCode(this.passedInspectionTS);
        hash = 53 * hash + Objects.hashCode(this.effectiveDateOfRecord);
        hash = 53 * hash + this.maxOccupantsAllowed;
        hash = 53 * hash + this.numBedrooms;
        hash = 53 * hash + this.numBathrooms;
        hash = 53 * hash + Objects.hashCode(this.thirdPartyInspector);
        hash = 53 * hash + Objects.hashCode(this.thirdPartyInspectorApprovalTS);
        hash = 53 * hash + Objects.hashCode(this.thirdPartyApprovalBy);
        hash = 53 * hash + Objects.hashCode(this.notes);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OccInspection other = (OccInspection) obj;
        if (this.inspectionID != other.inspectionID) {
            return false;
        }
        if (this.occPeriodID != other.occPeriodID) {
            return false;
        }
        if (this.pacc != other.pacc) {
            return false;
        }
        if (this.enablePacc != other.enablePacc) {
            return false;
        }
        if (this.maxOccupantsAllowed != other.maxOccupantsAllowed) {
            return false;
        }
        if (this.numBedrooms != other.numBedrooms) {
            return false;
        }
        if (this.numBathrooms != other.numBathrooms) {
            return false;
        }
        if (!Objects.equals(this.notes, other.notes)) {
            return false;
        }
        if (!Objects.equals(this.inspector, other.inspector)) {
            return false;
        }
        if (!Objects.equals(this.checklistTemplate, other.checklistTemplate)) {
            return false;
        }
        if (!Objects.equals(this.inspectedSpaceList, other.inspectedSpaceList)) {
            return false;
        }
        if (!Objects.equals(this.passedInspectionCertifiedBy, other.passedInspectionCertifiedBy)) {
            return false;
        }
        if (!Objects.equals(this.passedInspectionTS, other.passedInspectionTS)) {
            return false;
        }
        if (!Objects.equals(this.effectiveDateOfRecord, other.effectiveDateOfRecord)) {
            return false;
        }
        if (!Objects.equals(this.thirdPartyInspector, other.thirdPartyInspector)) {
            return false;
        }
        if (!Objects.equals(this.thirdPartyInspectorApprovalTS, other.thirdPartyInspectorApprovalTS)) {
            return false;
        }
        if (!Objects.equals(this.thirdPartyApprovalBy, other.thirdPartyApprovalBy)) {
            return false;
        }
        return true;
    }

    /**
     * @return the effectiveDateOfRecordUtilDate
     */
    public java.util.Date getEffectiveDateOfRecordUtilDate() {
        effectiveDateOfRecordUtilDate = convertUtilDate(effectiveDateOfRecord);
        
        return effectiveDateOfRecordUtilDate;
    }

    /**
     * @param effectiveDateOfRecordUtilDate the effectiveDateOfRecordUtilDate to set
     */
    public void setEffectiveDateOfRecordUtilDate(java.util.Date effectiveDateOfRecordUtilDate) {
        this.effectiveDateOfRecordUtilDate = effectiveDateOfRecordUtilDate;
        effectiveDateOfRecord = convertUtilDate(effectiveDateOfRecordUtilDate);
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * Utility method for determining which date to use for comparison.
     * Note that inspections that haven't been approved probably won't have an
     * effective date of record so we should just use the creation timestamp
     * @param ins
     * @return the selected date for comparison
     */
    private LocalDateTime getDateForComparison(OccInspection ins){
        if(ins.getEffectiveDateOfRecord() == null){
            return ins.getCreationTS();
        } else {
            return ins.getEffectiveDateOfRecord();
        }
    }
    

    @Override
    public int compareTo(OccInspection ins) {
        int compRes = getDateForComparison(this).compareTo(getDateForComparison(ins));

//        if(getDateForComparison(this).isBefore(getDateForComparison(ins))){
//            return 1;
//        } else if (getDateForComparison(this).isAfter(getDateForComparison(ins))){
//            return -1;
//        } else {
//            return 0;
//        }
        return compRes;
    }

    /**
     * @return the creationTS
     */
    public LocalDateTime getCreationTS() {
        return creationTS;
    }

    /**
     * @param creationTS the creationTS to set
     */
    public void setCreationTS(LocalDateTime creationTS) {
        this.creationTS = creationTS;
    }

    /**
     * @return the inspectedSpaceListVisible
     */
    public List<OccInspectedSpace> getInspectedSpaceListVisible() {
        configureVisibleSpaceElementList();

        return inspectedSpaceListVisible;
    }

    /**
     * @param inspectedSpaceListVisible the inspectedSpaceListVisible to set
     */
    public void setInspectedSpaceListVisible(List<OccInspectedSpace> inspectedSpaceListVisible) {
        this.inspectedSpaceListVisible = inspectedSpaceListVisible;
    }

    /**
     * @return the readyForPassedCertification
     */
    public boolean isReadyForPassedCertification() {
        return readyForPassedCertification;
    }

    /**
     * @param readyForPassedCertification the readyForPassedCertification to set
     */
    public void setReadyForPassedCertification(boolean readyForPassedCertification) {
        this.readyForPassedCertification = readyForPassedCertification;
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
     * @return the includeEmtpySpaces
     */
    public boolean isIncludeEmtpySpaces() {
        return includeEmtpySpaces;
    }

    /**
     * @param includeEmtpySpaces the includeEmtpySpaces to set
     */
    public void setIncludeEmtpySpaces(boolean includeEmtpySpaces) {
        this.includeEmtpySpaces = includeEmtpySpaces;
    }

    
}
