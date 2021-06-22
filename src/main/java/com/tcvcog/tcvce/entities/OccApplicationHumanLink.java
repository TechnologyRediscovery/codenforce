/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;

/**
 * Represents a set of humans associated with an Occupancy Application
 * Refactored from extending Person to Human
 * 
 * TODO: Jurplel please review this and bake into our occ system
 * 
 * @author Ellen Bascomb of Apartment 31Y
 */
public class        OccApplicationHumanLink 
        extends     Human {
    
    private int applicationID;
    private boolean applicant;
    private boolean preferredContact;
    private PersonType applicationPersonType;
    private boolean linkActive; //stores if this link is active in the database, not the person object itself


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
    
}
