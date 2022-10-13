/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.occupancy;

import java.util.List;

/**
 * Wrapper around an OccSpaceType to contain the checklist related metadata
 * @author Ellen Bascomb of Apartment 31Y
 */
public class OccSpaceTypeChecklistified
        extends OccSpaceType{
    
    private int checklistSpaceTypeID;
    private int checklistParentID; // populated for auditing
    private boolean required;
    private String notes;
    private List<OccSpaceElement> codeElementList;

    public OccSpaceTypeChecklistified(OccSpaceType ost){
        super(ost);
        
    }
    
    public OccSpaceTypeChecklistified(){
        
    }
    
    
    /**
     * @return the codeElementList
     */
    public List<OccSpaceElement> getCodeElementList() {
        return codeElementList;
    }

    /**
     * @param codeElementList the codeElementList to set
     */
    public void setCodeElementList(List<OccSpaceElement> codeElementList) {
        this.codeElementList = codeElementList;
    }

    /**
     * @return the required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param required the required to set
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the checklistSpaceTypeID
     */
    public int getChecklistSpaceTypeID() {
        return checklistSpaceTypeID;
    }

    /**
     * @param checklistSpaceTypeID the checklistSpaceTypeID to set
     */
    public void setChecklistSpaceTypeID(int checklistSpaceTypeID) {
        this.checklistSpaceTypeID = checklistSpaceTypeID;
    }

    /**
     * @return the checklistParentID
     */
    public int getChecklistParentID() {
        return checklistParentID;
    }

    /**
     * @param checklistParentID the checklistParentID to set
     */
    public void setChecklistParentID(int checklistParentID) {
        this.checklistParentID = checklistParentID;
    }
    
}
