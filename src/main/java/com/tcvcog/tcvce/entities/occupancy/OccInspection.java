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

import com.tcvcog.tcvce.entities.Payment;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Adam Gutonski and Sylvia
 */
public class OccInspection {
    
    private int inspectionID;
    private User inspector;
    private int occPeriodID;
    
    // This template object provides us the raw lists of uninspected
    // space types, from which we extract a list of Spaces and their CodeElements
    private OccChecklistTemplate checklistTemplate;
    private List<OccInspectedSpace> inspectedSpaceList;
    
    private int pacc;
    private boolean enablePacc;
    
    private User passCertifiedBy;
    private LocalDateTime passTS;
    private LocalDateTime effectiveDate;
    
    private int maxOccupantsAllowed;
    private int numBedrooms;
    private int numBathrooms;
    
    private Person thirdPartyInspector;
    private LocalDateTime thirdPartyInspectorApprovalTS;
    private User thirdPartyApprovalBy;
    
    

    private String notes; 
    
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
     * @return the passCertifiedBy
     */
    public User getPassCertifiedBy() {
        return passCertifiedBy;
    }

    /**
     * @param passCertifiedBy the passCertifiedBy to set
     */
    public void setPassCertifiedBy(User passCertifiedBy) {
        this.passCertifiedBy = passCertifiedBy;
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
     * @return the passTS
     */
    public LocalDateTime getPassTS() {
        return passTS;
    }

    /**
     * @param passTS the passTS to set
     */
    public void setPassTS(LocalDateTime passTS) {
        this.passTS = passTS;
    }

    /**
     * @return the effectiveDate
     */
    public LocalDateTime getEffectiveDate() {
        return effectiveDate;
    }

    /**
     * @param effectiveDate the effectiveDate to set
     */
    public void setEffectiveDate(LocalDateTime effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    
}
