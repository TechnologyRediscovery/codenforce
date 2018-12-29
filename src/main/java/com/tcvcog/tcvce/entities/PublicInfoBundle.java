/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.time.LocalDateTime;

/**
 *
 * @author sylvia
 */
public abstract class PublicInfoBundle {

    private int pacc;
    private boolean showDetailsPageButton;
    private boolean showAddMessageButton;
    private boolean paccEnabled;
    private String paccStatusMessage;
    
    private String typeName;
    private String dateOfRecord; 
    
    // some info bundles like an action request may not be assocaited with a
    // specific Property object. Most are, however, and the String rep of its 
    // address will be stored here
    private boolean addressAssociated;
    private String propertyAddress;
    private Municipality muni;
    
    private String caseManagerName;
    private String caseManagerContact;

    
    @Override
    public abstract String toString();
    
    /**
     * @return the addressAssociated
     */
    public boolean isAddressAssociated() {
        return addressAssociated;
    }

    /**
     * @return the propertyAddress
     */
    public String getPropertyAddress() {
        return propertyAddress;
    }

    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    /**
     * @return the caseManagerName
     */
    public String getCaseManagerName() {
        return caseManagerName;
    }

    /**
     * @return the caseManagerContact
     */
    public String getCaseManagerContact() {
        return caseManagerContact;
    }

    /**
     * @param addressAssociated the addressAssociated to set
     */
    public void setAddressAssociated(boolean addressAssociated) {
        this.addressAssociated = addressAssociated;
    }

    /**
     * @param propertyAddress the propertyAddress to set
     */
    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }

    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    /**
     * @param caseManagerName the caseManagerName to set
     */
    public void setCaseManagerName(String caseManagerName) {
        this.caseManagerName = caseManagerName;
    }

    /**
     * @param caseManagerContact the caseManagerContact to set
     */
    public void setCaseManagerContact(String caseManagerContact) {
        this.caseManagerContact = caseManagerContact;
    }
    

    /**
     * @return the pacc
     */
    public int getPacc() {
        return pacc;
    }


    /**
     * @return the typeName
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * @return the dateOfRecord
     */
    public String getDateOfRecord() {
        return dateOfRecord;
    }

    /**
     * @param pacc the pacc to set
     */
    public void setPacc(int pacc) {
        this.pacc = pacc;
    }

    /**
     * @param typeName the typeName to set
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * @param dateOfRecord the dateOfRecord to set
     */
    public void setDateOfRecord(String dateOfRecord) {
        this.dateOfRecord = dateOfRecord;
    }

    /**
     * @return the paccEnabled
     */
    public boolean isPaccEnabled() {
        return paccEnabled;
    }

    /**
     * @param paccEnabled the paccEnabled to set
     */
    public void setPaccEnabled(boolean paccEnabled) {
        this.paccEnabled = paccEnabled;
    }

    /**
     * @return the paccStatusMessage
     */
    public String getPaccStatusMessage() {
        return paccStatusMessage;
    }

    /**
     * @param paccStatusMessage the paccStatusMessage to set
     */
    public void setPaccStatusMessage(String paccStatusMessage) {
        this.paccStatusMessage = paccStatusMessage;
    }

    /**
     * @return the showDetailsPageButton
     */
    public boolean isShowDetailsPageButton() {
        return showDetailsPageButton;
    }

    /**
     * @param showDetailsPageButton the showDetailsPageButton to set
     */
    public void setShowDetailsPageButton(boolean showDetailsPageButton) {
        this.showDetailsPageButton = showDetailsPageButton;
    }

    /**
     * @return the showAddMessageButton
     */
    public boolean isShowAddMessageButton() {
        return showAddMessageButton;
    }

    /**
     * @param showAddMessageButton the showAddMessageButton to set
     */
    public void setShowAddMessageButton(boolean showAddMessageButton) {
        this.showAddMessageButton = showAddMessageButton;
    }
    
    
}
