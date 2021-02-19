/*
 * Copyright (C) Technology Rediscovery LLC. 2020
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
package com.tcvcog.tcvce.entities;

import java.io.Serializable;

/**
 * PublicInfoBundle is a wrapping that removes all information a public user should not know about a BOb.
 * e.g. A PublicInfoBundlePerson has the first two letters of the first and last
 * name, and only the beginning characters of the email address. This way a public user can
 * identify themselves, but malicious users can't use the website as an email.
 * search engine.
 * @author Nathan Dietz
 */
public abstract class PublicInfoBundle implements Serializable {

    /*
    pacc = Public Access Control Code. The public user uses this code to view/edit
    CEActionRequests, OccPermitApplications, etc.
    
    It is rare, but possible for multiple PublicInfoBundles to have the same 
    code if they are somehow related to one another.
    It is not that there is a PACC for each PublicInfoBundle
    Each public user gets 1 PACC for all the bundles they need!
    */
    private int pacc;
    
    
    private boolean showDetailsPageButton;
    private boolean showAddMessageButton;
    
    //If false, public users cannot view this bundle using its PACC
    private boolean paccEnabled;
    
    //A possible explanation as to why the user can't view the PACC
    //Typically automatically generated.
    private String paccStatusMessage;
    
    private String typeName; //The type of object the bundle is holding.
    private String dateOfRecord; 
    
    // some info bundles like an action request may not be associated with a
    // specific Property object. Most are, however, and the String rep of its 
    // address will be stored here
    private boolean addressAssociated;
    private String propertyAddress;
    private Municipality muni;
    
    private String caseManagerName;
    private String caseManagerContact;

    public PublicInfoBundle() {
    }
    
    @Override
    public String toString(){
        
        return this.getClass().getName();
        
    }
    /**
     * Takes a User object and uses its information to populate 
     * the caseManagerName and caseManagerContact fields.
     * Also checks for null pointers!
     * @param manager 
     */
    public void setCaseManager(User manager){
        if (manager != null && manager.getPerson() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(manager.getPerson().getFirstName());
            sb.append(" ");
            sb.append(manager.getPerson().getLastName());
            setCaseManagerName(sb.toString());
            setCaseManagerContact(manager.getPerson().getPhoneWork());
        }
    }
    
    /**
     * Takes a Property object and uses its information to populate 
     * the addressAssociated and propertyAddress fields.
     * Also checks for null pointers!
     * @param prop 
     */
    public void setAddress(Property prop){
        if (prop == null || prop.isNonAddressable()) {
                setAddressAssociated(false);
            } else {
                setAddressAssociated(true);
                setPropertyAddress(prop.getAddress());
            }
        
    }
    
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
