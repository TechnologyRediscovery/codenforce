/*
 * Copyright (C) 2020 Technology Rediscovery LLC
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

import java.time.LocalDateTime;

/**
 * Represents a Mailing address associated with a given parcel
 * 
 * @author Ellen Bascomb of Apartment 31Y
 */
public class ParcelMailingAddressLink
        extends MailingAddress
        implements IFace_trackedEntityLink{

    final static LinkedObjectSchemaEnum ROLE_SCHEMA = LinkedObjectSchemaEnum.PARCELMAILINGADDRESS;
    
    protected LinkedObjectRole linkRole;
    protected BOBSource linkSource;
    
    protected LocalDateTime linkCreatedTS;
    protected int linkCreatedBy;
    
    protected LocalDateTime linkLastUpdatedTS;
    protected int linkLastUpdatedBy;
    
    protected LocalDateTime linkDeactivatedTS;
    protected int linkDeactivatedBy;
    
    protected String linkNotes;
    
    public ParcelMailingAddressLink(MailingAddress ma){
        
        this.addressID = ma.addressID;
        this.buildingNo = ma.buildingNo;
        this.street = ma.street;
        this.poBox = ma.poBox;
        this.verifiedTS = ma.verifiedTS;
        this.source = ma.source;
        this.notes = ma.notes;

        // Abstract TrackedEntity Class Fields
        this.createdTS = ma.createdTS;
        this.createdBy = ma.createdBy;
        this.lastUpdatedTS = ma.lastUpdatedTS;
        this.lastUpdatedBy = ma.lastUpdatedBy;
        this.deactivatedTS = ma.deactivatedTS;
        this.deactivatedBy = ma.deactivatedBy;
        
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

    /**
     * @return the linkCreatedTS
     */
    @Override
    public LocalDateTime getLinkCreatedTS() {
        return linkCreatedTS;
    }

    /**
     * @return the linkCreatedBy
     */
    @Override
    public int getLinkCreatedByUserID() {
        return linkCreatedBy;
    }

    /**
     * @return the linkLastUpdatedTS
     */
    @Override
    public LocalDateTime getLinkLastUpdatedTS() {
        return linkLastUpdatedTS;
    }

    /**
     * @return the linkLastUpdatedBy
     */
    @Override
    public int getLinkLastUpdatedByUserID() {
        return linkLastUpdatedBy;
    }

    /**
     * @return the linkDeactivatedTS
     */
    @Override
    public LocalDateTime getLinkDeactivatedTS() {
        return linkDeactivatedTS;
    }

    /**
     * @return the linkDeactivatedBy
     */
    @Override
    public int getLinkDeactivatedByUserID() {
        return linkDeactivatedBy;
    }

    /**
     * @return the linkNotes
     */
    @Override
    public String getLinkNotes() {
        return linkNotes;
    }


    /**
     * @param linkCreatedBy the linkCreatedBy to set
     */
    @Override
    public void setLinkCreatedByUserID(int linkCreatedBy) {
        this.linkCreatedBy = linkCreatedBy;
    }


    /**
     * @param linkLastUpdatedBy the linkLastUpdatedBy to set
     */
    @Override
    public void setLinkLastUpdatedByUserID(int linkLastUpdatedBy) {
        this.linkLastUpdatedBy = linkLastUpdatedBy;
    }


    /**
     * @param linkDeactivatedBy the linkDeactivatedBy to set
     */
    @Override
    public void setLinkDeactivatedByUserID(int linkDeactivatedBy) {
        this.linkDeactivatedBy = linkDeactivatedBy;
    }

    /**
     * @param linkNotes the linkNotes to set
     */
    @Override
    public void setLinkNotes(String linkNotes) {
        this.linkNotes = linkNotes;
    }

    @Override
    public boolean isLinkDeactivated() {
        return linkDeactivatedTS != null;
    }

    
    
    
    
    
    /**
     * @param linkCreatedTS the linkCreatedTS to set
     */
    @Override
    public void setLinkCreatedTS(LocalDateTime linkCreatedTS) {
        this.linkCreatedTS = linkCreatedTS;
    }

    /**
     * @param linkLastUpdatedTS the linkLastUpdatedTS to set
     */
    @Override
    public void setLinkLastUpdatedTS(LocalDateTime linkLastUpdatedTS) {
        this.linkLastUpdatedTS = linkLastUpdatedTS;
    }

    /**
     * @param linkDeactivatedTS the linkDeactivatedTS to set
     */
    @Override
    public void setLinkDeactivatedTS(LocalDateTime linkDeactivatedTS) {
        this.linkDeactivatedTS = linkDeactivatedTS;
    }

    @Override
    public String getPKFieldName() {
        return ROLE_SCHEMA.getLinkedTablePKField();
    }

    @Override
    public String getDBTableName() {
        return ROLE_SCHEMA.getLinkingTableName();
    }

    @Override
    public LinkedObjectRole getLinkedObjectRole() {
        return linkRole;
    }

    
    public LinkedObjectSchemaEnum getLinkedObjectRoleSchemaEnum() {
        return ROLE_SCHEMA;
    }

    /**
     * @param linkRole the linkRole to set
     */
    public void setLinkRole(LinkedObjectRole linkRole) {
        this.linkRole = linkRole;
    }

    @Override
    public void setLinkedObjectRole(LinkedObjectRole lor) {
        linkRole = lor;
    }

   
}
