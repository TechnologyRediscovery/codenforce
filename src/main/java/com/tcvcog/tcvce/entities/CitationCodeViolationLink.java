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
package com.tcvcog.tcvce.entities;

import java.time.LocalDateTime;

/**
 * Represents a connection between a Citation and a CodeViolation
 * Note that the LinkedObjectRole member is sort of unnecesary here since
 * we have a separate status object. This is unique to the Citation-CV link
 * because its role changes over time as a case moves through the
 * court system.
 * 
 * @author Ellen Bascomb
 */
public class CitationCodeViolationLink 
        extends CodeViolation
        implements IFace_trackedEntityLink{
    
    final static LinkedObjectRoleSchemaEnum ROLE_SCHEMA = LinkedObjectRoleSchemaEnum.CITATIONCODEVIOLATION;
    
    protected int citationViolationID;
    protected LinkedObjectRole linkRole;
    
    protected CitationViolationStatusEnum citVStatus;
    
    protected BOBSource linkSource;
    
    protected LocalDateTime linkCreatedTS;
    protected User linkCreatedBy;
    
    protected LocalDateTime linkLastUpdatedTS;
    protected User linkLastUpdatedBy;
    
    protected LocalDateTime linkDeactivatedTS;
    protected User linkDeactivatedBy;
    
    protected String linkNotes;

    /**
     * @return the citVStatus
     */
    public CitationViolationStatusEnum getCitVStatus() {
        return citVStatus;
    }

    /**
     * @return the linkSource
     */
    public BOBSource getLinkSource() {
        return linkSource;
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
     * @param citVStatus the citVStatus to set
     */
    public void setCitVStatus(CitationViolationStatusEnum citVStatus) {
        this.citVStatus = citVStatus;
    }

    /**
     * @param linkSource the linkSource to set
     */
    public void setLinkSource(BOBSource linkSource) {
        this.linkSource = linkSource;
    }

    /**
     * @param linkCreatedTS the linkCreatedTS to set
     */
     @Override
    public void setLinkCreatedTS(LocalDateTime linkCreatedTS) {
        this.linkCreatedTS = linkCreatedTS;
    }

    /**
     * @param linkCreatedBy the linkCreatedBy to set
     */
     @Override
    public void setLinkCreatedBy(User linkCreatedBy) {
        this.linkCreatedBy = linkCreatedBy;
    }

    /**
     * @param linkLastUpdatedTS the linkLastUpdatedTS to set
     */
     @Override
    public void setLinkLastUpdatedTS(LocalDateTime linkLastUpdatedTS) {
        this.linkLastUpdatedTS = linkLastUpdatedTS;
    }

    /**
     * @param linkLastUpdatedBy the linkLastUpdatedBy to set
     */
     @Override
    public void setLinkLastUpdatedBy(User linkLastUpdatedBy) {
        this.linkLastUpdatedBy = linkLastUpdatedBy;
    }

    /**
     * @param linkDeactivatedTS the linkDeactivatedTS to set
     */
     @Override
    public void setLinkDeactivatedTS(LocalDateTime linkDeactivatedTS) {
        this.linkDeactivatedTS = linkDeactivatedTS;
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

    @Override
    public String getPKFieldName() {
        return ROLE_SCHEMA.getLinkedTablePKField();
    }

    @Override
    public int getDBKey() {
        return citationViolationID;
    }

    @Override
    public String getDBTableName() {
       return ROLE_SCHEMA.getLinkedTableName();
    }

    /**
     * @return the citationViolationID
     */
    public int getCitationViolationID() {
        return citationViolationID;
    }

    /**
     * @param citationViolationID the citationViolationID to set
     */
    public void setCitationViolationID(int citationViolationID) {
        this.citationViolationID = citationViolationID;
    }

    /**
     * @param linkRole the linkRole to set
     */
    public void setLinkRole(LinkedObjectRole linkRole) {
        this.linkRole = linkRole;
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
