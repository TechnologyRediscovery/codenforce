/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a set of humans associated with an Occupancy Application
 * Refactored from extending Person to Human
 * 
 * TODO: Jurplel please review this and bake into our occ system
 * 
 * @author Ellen Bascomb of Apartment 31Y
 */
public class        OccApplicationHumanLink 
        extends     Human 
        implements  IFace_trackedEntityLink{
    
    final static LinkedObjectRoleSchemaEnum ROLE_SCHEMA = LinkedObjectRoleSchemaEnum.OCCAPPLICATIONHUMAN;
    
    private int applicationID;
    private boolean applicant;
    private boolean preferredContact;
    private PersonType applicationPersonType;
    private boolean linkActive; //stores if this link is active in the database, not the person object itself

    protected BOBSource linkSource;

    /**
     * @return the preferredContact
     */
    public boolean isPreferredContact() {
        return preferredContact;
    }

    /**
     * @return the applicationPersonType
     */
    public PersonType getApplicationPersonType() {
        return applicationPersonType;
    }

   
    /**
     * @param preferredContact the preferredContact to set
     */
    public void setPreferredContact(boolean preferredContact) {
        this.preferredContact = preferredContact;
    }

    /**
     * @param applicationPersonType the applicationPersonType to set
     */
    public void setApplicationPersonType(PersonType applicationPersonType) {
        this.applicationPersonType = applicationPersonType;
    }

    public boolean isApplicant() {
        return applicant;
    }

    public void setApplicant(boolean applicant) {
        this.applicant = applicant;
    }

    public boolean isLinkActive() {
        return linkActive;
    }

    public void setLinkActive(boolean linkActive) {
        this.linkActive = linkActive;
    }

    public int getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(int applicationID) {
        this.applicationID = applicationID;
    }

    @Override
    public String getPKFieldName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDBTableName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LocalDateTime getLinkCreatedTS() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLinkCreatedTS(LocalDateTime ts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public User getLinkCreatedBy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLinkCreatedBy(User usr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LocalDateTime getLinkLastUpdatedTS() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLinkLastUpdatedTS(LocalDateTime ts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public User getLinkLastUpdatedBy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLinkLastUpdatedBy(User usr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LocalDateTime getLinkDeactivatedTS() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLinkDeactivatedTS(LocalDateTime ts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLinkDeactivatedBy(User usr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public User getLinkDeactivatedBy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isLinkDeactivated() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLinkNotes(String n) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getLinkNotes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LinkedObjectRole getLinkedObjectRole() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LinkedObjectRoleSchemaEnum getLinkedObjectRoleSchemaEnum() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the linkSource
     */
    public BOBSource getLinkSource() {
        return linkSource;
    }

    /**
     * @param linkSource the linkSource to set
     */
    public void setLinkSource(BOBSource linkSource) {
        this.linkSource = linkSource;
    }
    
}
