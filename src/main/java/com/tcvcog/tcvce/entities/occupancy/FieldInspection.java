/*
 * Copyright (C) 2021 Technology Rediscovery LLC
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

import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.entities.BOb;
import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.entities.BlobLinkEnum;
import com.tcvcog.tcvce.entities.DomainEnum;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.IFace_BlobHolder;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.OccInspectionRequirementAssigned;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsOccChecklistItemsEnum;

import java.time.LocalDateTime;
import java.util.*;

/**
 * This whole class represents a "clipboard," where each item in the inspectedSpaceList
 * represents a sheet of paper listing all the code elements that were inspected in that particular space
 * and of that particular type with its own injected location descriptor.
 *
 * Every sheet of paper i.e. an element in the inspectedSpaceList can only be of one space type--that space type
 * lives inside of the OccInspectedSpace, and that space type was birthed from an OccSpaceTypeInspectionDirective.
 *
 * It is this object that gets passed to the "inspectionaction" methods on the occ coordinator to house the logic for
 * setting the right member variables on OccInspectedSpace and their OccInspectedSpaceElement classes.
 *
 * @author Adam Gutonski and Sylvia
 */
public  class       FieldInspection 
        extends     BOb 
        implements  Comparable<FieldInspection>,
                    IFace_BlobHolder{
    
    private final static BlobLinkEnum BLOB_LINK_ENUM = BlobLinkEnum.FIELD_INSPECTION;
    private final static BlobLinkEnum BLOP_UPSPTREAM_POOL = BlobLinkEnum.OCC_PERIOD;
    
    private int inspectionID;

    /** ID for inspection that this inspection is a follow up to (if any) **/
    private int followUpToInspectionID;

    private User inspector;

    private User createdBy;
    private LocalDateTime creationTS;

    private User lastUpdatedBy;
    private LocalDateTime lastUpdatedTS;

    private User deactivatedBy;
    private LocalDateTime deactivatedTS;

    private LocalDateTime timeStart;
    private LocalDateTime timeEnd;

    /** ID for the OccPeriod that this FieldInspection is for **/
    private DomainEnum domainEnum;
    private int occPeriodID;
    private int cecaseID;
    
    // This template object provides us the raw lists of uninspected
    // space types, from which we extract a list of Spaces and their CodeElements
    private int checklistTemplateID;
    private OccChecklistTemplate checklistTemplate;
    
    private List<OccInspectedSpace> inspectedSpaceList;
    private List<OccInspectedSpace> inspectedSpaceListVisible;
    private ViewOptionsOccChecklistItemsEnum viewSetting;
    private boolean includeEmptySpaces;
    
    private List<BlobLight> blobList;
    
    private int pacc;
    private boolean enablePacc;
    
    private boolean readyForPassedCertification;
    
    private LocalDateTime effectiveDateOfRecord;
    
    private int maxOccupantsAllowed;
    private int numBedrooms;
    private int numBathrooms;
    
    private Person thirdPartyInspector;
    private LocalDateTime thirdPartyInspectorApprovalTS;
    private User thirdPartyApprovalBy;

    private String notesPreInspection;

    private OccInspectionDetermination determination;
    private User determinationBy;
    private LocalDateTime determinationTS;


    private String remarks;
    private String generalComments;

    private OccInspectionCause cause;
    
    private List<OccInspectionRequirementAssigned> requirementList;

    /**
     * No Arg constructor for FieldInspections:
     * I setup lists
     */
    public FieldInspection() {

        inspectedSpaceList = new ArrayList<>();
        inspectedSpaceListVisible = new ArrayList<>();
        viewSetting = ViewOptionsOccChecklistItemsEnum.ALL_ITEMS;
    }
    
    
    /** 
     * Special getter for subsets of elements
     * @return the inspectedSpaceListVisible
     */
    public List<OccInspectedSpace> getInspectedSpaceListVisible() {
        configureVisibleSpaceElementList(null);

        return inspectedSpaceListVisible;
    }


    /**
     * Uses the Inspection's view setting (the given one if not null)
     * to build a list of visible inspected spaces based on 
     * @param viewEnum if not null, I become this inspection's default view setting
     */
    public void configureVisibleSpaceElementList(ViewOptionsOccChecklistItemsEnum viewEnum) {
        if(viewEnum != null){
            viewSetting = viewEnum;
        }
        inspectedSpaceListVisible.clear();
        for(Iterator<OccInspectedSpace> it = inspectedSpaceList.iterator(); it.hasNext(); ){
            OccInspectedSpace ois = it.next(); 
            ois.setViewSetting(viewSetting);
            ois.configureVisibleElementList();
            if(!ois.getInspectedElementListVisible().isEmpty()
                    || (ois.getInspectedElementListVisible().isEmpty() && includeEmptySpaces)){
                inspectedSpaceListVisible.add(ois);
            }
        } // close for over inspectedspaces
    }
    
     /**
     * I extract all failed elements who have a true flag on their migrate to CE case
     * in their belly
     * @param fin
     * @return a list, perhaps with eces to include in a case
     * @throws BObStatusException 
     */
    public List<EnforcableCodeElement> extractFailedItemsForCECaseMigration() 
            throws BObStatusException{
        
        List<EnforcableCodeElement> eceList = new ArrayList<>();
        if(inspectedSpaceList != null && !inspectedSpaceList.isEmpty()){
            for(OccInspectedSpace ois: inspectedSpaceList){
                for(OccInspectedSpaceElement oise: ois.getElementListFail()){
                    if(oise.isMigrateToCaseOnFail()){
                        eceList.add(oise);
                    }
                }
            }
        } 
        return eceList;
    }

    /**
     * Asks each inspected space for its unique location descriptors and
     * builds a nice list
     * @return a list, perhaps containing one or more unique location descriptors
     */
    public List<OccLocationDescriptor> getAllUniqueLocationDescriptors() {
        Set<OccLocationDescriptor> locationDescriptors = new HashSet();

        for (OccInspectedSpace inspectedSpace : inspectedSpaceList) {
            locationDescriptors.add(inspectedSpace.getLocation());
            locationDescriptors.addAll(inspectedSpace.getAllUniqueLocationDescriptors());
        }

        List<OccLocationDescriptor> locationDescriptorList = new ArrayList();
        locationDescriptorList.addAll(locationDescriptors);
        return locationDescriptorList;
    }

    /**
     * @return the size of the inspectedSpaceList
     */
    public int getInspectedSpaceListSize() {
        int size = 0;
        if (inspectedSpaceList != null)
            size = inspectedSpaceList.size();

        return size;
    }
    
    
    

    // ********************************************************
    // **************** GETTERS AND SETTERS *******************
    // ********************************************************
    
    
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
    public String getNotesPreInspection() {
        return notesPreInspection;
    }

    /**
     * @param notesPreInspection the notes to set
     */
    public void setNotesPreInspection(String notesPreInspection) {
        this.notesPreInspection = notesPreInspection;
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
     * @return the effectiveDateOfRecord
     */
    public LocalDateTime getEffectiveDateOfRecord() {
        return effectiveDateOfRecord;
    }

    /**
     * @param effectiveDateOfRecord the effectiveDateOfRecord to set
     */
    public void setEffectiveDateOfRecord(LocalDateTime effectiveDateOfRecord) {
        this.effectiveDateOfRecord = effectiveDateOfRecord;
    }

    // These are generated by intellij--sorry for the format but it does what you expect

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldInspection that = (FieldInspection) o;
        return inspectionID == that.inspectionID && followUpToInspectionID == that.followUpToInspectionID && occPeriodID == that.occPeriodID && includeEmptySpaces == that.includeEmptySpaces && pacc == that.pacc && enablePacc == that.enablePacc && readyForPassedCertification == that.readyForPassedCertification && maxOccupantsAllowed == that.maxOccupantsAllowed && numBedrooms == that.numBedrooms && numBathrooms == that.numBathrooms && Objects.equals(inspector, that.inspector) && Objects.equals(createdBy, that.createdBy) && Objects.equals(creationTS, that.creationTS) && Objects.equals(lastUpdatedBy, that.lastUpdatedBy) && Objects.equals(lastUpdatedTS, that.lastUpdatedTS) && Objects.equals(checklistTemplate, that.checklistTemplate) && Objects.equals(inspectedSpaceList, that.inspectedSpaceList) && Objects.equals(inspectedSpaceListVisible, that.inspectedSpaceListVisible) && viewSetting == that.viewSetting && Objects.equals(effectiveDateOfRecord, that.effectiveDateOfRecord) && Objects.equals(thirdPartyInspector, that.thirdPartyInspector) && Objects.equals(thirdPartyInspectorApprovalTS, that.thirdPartyInspectorApprovalTS) && Objects.equals(thirdPartyApprovalBy, that.thirdPartyApprovalBy) && Objects.equals(notesPreInspection, that.notesPreInspection) && Objects.equals(determination, that.determination) && Objects.equals(determinationBy, that.determinationBy) && Objects.equals(determinationTS, that.determinationTS) && Objects.equals(remarks, that.remarks) && Objects.equals(generalComments, that.generalComments) && Objects.equals(cause, that.cause);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inspectionID, followUpToInspectionID, inspector, createdBy, creationTS, lastUpdatedBy, lastUpdatedTS, occPeriodID, checklistTemplate, inspectedSpaceList, inspectedSpaceListVisible, viewSetting, includeEmptySpaces, pacc, enablePacc, readyForPassedCertification, effectiveDateOfRecord, maxOccupantsAllowed, numBedrooms, numBathrooms, thirdPartyInspector, thirdPartyInspectorApprovalTS, thirdPartyApprovalBy, notesPreInspection, determination, determinationBy, determinationTS, remarks, generalComments, cause);
    }

    /**
     * Utility method for determining which date to use for comparison.
     * Note that inspections that haven't been approved probably won't have an
     * effective date of record so we should just use the creation timestamp
     * @param ins
     * @return the selected date for comparison
     */
    private LocalDateTime getDateForComparison(FieldInspection ins){
        if(ins.getEffectiveDateOfRecord() == null){
            return ins.getCreationTS();
        } else {
            return ins.getEffectiveDateOfRecord();
        }
    }
    

    @Override
    public int compareTo(FieldInspection ins) {
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
    public boolean isIncludeEmptySpaces() {
        return includeEmptySpaces;
    }

    /**
     * @param includeEmptySpaces the includeEmtpySpaces to set
     */
    public void setIncludeEmptySpaces(boolean includeEmptySpaces) {
        this.includeEmptySpaces = includeEmptySpaces;
    }

    public int getFollowUpToInspectionID() {
        return followUpToInspectionID;
    }

    public void setFollowUpToInspectionID(int followUpToInspectionID) {
        this.followUpToInspectionID = followUpToInspectionID;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(User lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public LocalDateTime getLastUpdatedTS() {
        return lastUpdatedTS;
    }

    public void setLastUpdatedTS(LocalDateTime lastUpdatedTS) {
        this.lastUpdatedTS = lastUpdatedTS;
    }

    public User getDeactivatedBy() {
        return deactivatedBy;
    }

    public void setDeactivatedBy(User deactivatedBy) {
        this.deactivatedBy = deactivatedBy;
    }

    public LocalDateTime getDeactivatedTS() {
        return deactivatedTS;
    }

    public void setDeactivatedTS(LocalDateTime deactivatedTS) {
        this.deactivatedTS = deactivatedTS;
    }

    public OccInspectionDetermination getDetermination() {
        return determination;
    }

    public void setDetermination(OccInspectionDetermination determination) {
        this.determination = determination;
    }

    public User getDeterminationBy() {
        return determinationBy;
    }

    public void setDeterminationBy(User determinationBy) {
        this.determinationBy = determinationBy;
    }

    public LocalDateTime getDeterminationTS() {
        return determinationTS;
    }

    public void setDeterminationTS(LocalDateTime determinationTS) {
        this.determinationTS = determinationTS;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getGeneralComments() {
        return generalComments;
    }

    public void setGeneralComments(String generalComments) {
        this.generalComments = generalComments;
    }

    public OccInspectionCause getCause() {
        return cause;
    }

    public void setCause(OccInspectionCause cause) {
        this.cause = cause;
    }

    public LocalDateTime getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(LocalDateTime timeStart) {
        this.timeStart = timeStart;
    }

    public LocalDateTime getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(LocalDateTime timeEnd) {
        this.timeEnd = timeEnd;
    }

    /**
     * @return the checklistTemplateID
     */
    public int getChecklistTemplateID() {
        return checklistTemplateID;
    }

    /**
     * @param checklistTemplateID the checklistTemplateID to set
     */
    public void setChecklistTemplateID(int checklistTemplateID) {
        this.checklistTemplateID = checklistTemplateID;
    }

    @Override
    public void setBlobList(List<BlobLight> bl) {
        this.blobList = bl;
    }

    @Override
    public List<BlobLight> getBlobList() {
        return blobList;
    }

    @Override
    public BlobLinkEnum getBlobLinkEnum() {
        return BLOB_LINK_ENUM;
    }

    @Override
    public int getParentObjectID() {
        return inspectionID;
    }

    @Override
    public BlobLinkEnum getBlobUpstreamPoolEnum() {
        return BLOP_UPSPTREAM_POOL;
    }

    @Override
    public int getBlobUpstreamPoolEnumPoolFeederID() {
        return occPeriodID;
    }

    /**
     * @return the cecaseID
     */
    public int getCecaseID() {
        return cecaseID;
    }

    /**
     * @param cecaseID the cecaseID to set
     */
    public void setCecaseID(int cecaseID) {
        this.cecaseID = cecaseID;
    }

    /**
     * @return the domainEnum
     */
    public DomainEnum getDomainEnum() {
        return domainEnum;
    }

    /**
     * @param domainEnum the domainEnum to set
     */
    public void setDomainEnum(DomainEnum domainEnum) {
        this.domainEnum = domainEnum;
    }
}
