/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.User;
import java.time.LocalDateTime;

/**
 *
 * @author Mike-Faux
 */
public class OccInspectionRequirementAssigned extends OccInspectionRequirement {
    private int inspectionID;
    
    private User assignedBy;
    private LocalDateTime assignedDate;
    private String assignedNotes;
    
    private User fulfilledBy;
    private LocalDateTime fulfilledDate;
    private String fulfilledNotes;
    
    private String notes;
    
    
    public OccInspectionRequirementAssigned(OccInspectionRequirement occInspectionRequirement){
        super(occInspectionRequirement);
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
     * @return the assignedBy
     */
    public User getAssignedBy() {
        return assignedBy;
    }

    /**
     * @param assignedBy the assignedBy to set
     */
    public void setAssignedBy(User assignedBy) {
        this.assignedBy = assignedBy;
    }

    /**
     * @return the assignedDate
     */
    public LocalDateTime getAssignedDate() {
        return assignedDate;
    }

    /**
     * @param assignedDate the assignedDate to set
     */
    public void setAssignedDate(LocalDateTime assignedDate) {
        this.assignedDate = assignedDate;
    }

    /**
     * @return the assignedNotes
     */
    public String getAssignedNotes() {
        return assignedNotes;
    }

    /**
     * @param assignedNotes the assignedNotes to set
     */
    public void setAssignedNotes(String assignedNotes) {
        this.assignedNotes = assignedNotes;
    }

    /**
     * @return the fulfilledBy
     */
    public User getFulfilledBy() {
        return fulfilledBy;
    }

    /**
     * @param fulfilledBy the fulfilledBy to set
     */
    public void setFulfilledBy(User fulfilledBy) {
        this.fulfilledBy = fulfilledBy;
    }

    /**
     * @return the fulfilledDate
     */
    public LocalDateTime getFulfilledDate() {
        return fulfilledDate;
    }

    /**
     * @param fulfilledDate the fulfilledDate to set
     */
    public void setFulfilledDate(LocalDateTime fulfilledDate) {
        this.fulfilledDate = fulfilledDate;
    }

    /**
     * @return the fulfilledNotes
     */
    public String getFulfilledNotes() {
        return fulfilledNotes;
    }

    /**
     * @param fulfilledNotes the fulfilledNotes to set
     */
    public void setFulfilledNotes(String fulfilledNotes) {
        this.fulfilledNotes = fulfilledNotes;
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
}
