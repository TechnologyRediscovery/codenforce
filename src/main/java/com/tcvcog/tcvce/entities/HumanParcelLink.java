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
 *  Linking object reprsenting a connection between a human and parcel;
 * Manifest as Parcel objects stored in a list by a Human derivative
 * @author sylvia
 */
public class HumanParcelLink 
        extends Parcel
        implements IFace_trackedEntityLink{
    
    final static LinkedObjectRoleSchemaEnum ROLE_SCHEMA = LinkedObjectRoleSchemaEnum.PARCELHUMAN;
    
    protected int linkID;
    protected int humanID;
    
    protected LinkedObjectRole linkRole;
    
    protected BOBSource linkSource;
    
    protected LocalDateTime linkCreatedTS;
    protected User linkCreatedBy;
    
    protected LocalDateTime linkLastUpdatedTS;
    protected User linkLastUpdatedBy;
    
    protected LocalDateTime linkDeactivatedTS;
    protected User linkDeactivatedBy;
    
    protected String linkNotes;

    /**
     * @return the linkID
     */
    public int getLinkID() {
        return linkID;
    }

   

    /**
     * @return the linkSource
     */
    public BOBSource getLinkSource() {
        return linkSource;
    }

   

    /**
     * @param linkID the linkID to set
     */
    public void setLinkID(int linkID) {
        this.linkID = linkID;
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
    public User getLinkCreatedBy() {
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
    public User getLinkLastUpdatedBy() {
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
    public User getLinkDeactivatedBy() {
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
    public void setLinkCreatedBy(User linkCreatedBy) {
        this.linkCreatedBy = linkCreatedBy;
    }

    /**
     * @param linkLastUpdatedBy the linkLastUpdatedBy to set
     */
    @Override
    public void setLinkLastUpdatedBy(User linkLastUpdatedBy) {
        this.linkLastUpdatedBy = linkLastUpdatedBy;
    }

    /**
     * @param linkDeactivatedBy the linkDeactivatedBy to set
     */
    @Override
    public void setLinkDeactivatedBy(User linkDeactivatedBy) {
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
     * @return the humanID
     */
    public int getHumanID() {
        return humanID;
    }

    /**
     * @param humanID the humanID to set
     */
    public void setHumanID(int humanID) {
        this.humanID = humanID;
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
        return ROLE_SCHEMA.getLinkedTableName();
    }

    @Override
    public LinkedObjectRole getLinkedObjectRole() {
        return linkRole;
    }

    @Override
    public LinkedObjectRoleSchemaEnum getLinkedObjectRoleSchemaEnum() {
        return ROLE_SCHEMA;
    }

   
}
