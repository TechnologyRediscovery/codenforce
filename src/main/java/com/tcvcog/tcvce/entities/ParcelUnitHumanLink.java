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
 * Encapsulates information about a Human and link fields
 * for use in composit ParcelUnits which contain a list of ParcelUnitHumanLink
 * objects 
 * @author Ellen Bascomb (31Y)
 */
public class ParcelUnitHumanLink
        extends Human 
        implements IFace_trackedEntityLink{
    
    protected int linkID;
    protected int parcelUnitID;
    protected HumanParcelRole role;
    
    protected LocalDateTime linkCreatedTS;
    protected User linkCreatedBy;
    
    protected LocalDateTime linkLastUpdatedTS;
    protected User linkLastUpdatedBy;
    
    protected LocalDateTime linkDeactivatedTS;
    protected User linkDeactivatedBy;
    
    protected String linkNotes;
    public ParcelUnitHumanLink(Human h){
        
        this.humanID = h.humanID;
        this.name = h.name;
        this.dob = h.dob;
        this.under18 = h.under18;
        this.jobTitle = h.jobTitle;
        this.businessEntity = h.businessEntity;
        this.multiHuman = h.multiHuman;
        this.source = h.source;
        this.deceasedDate = h.deceasedDate;
        this.deceasedBy = h.deceasedBy;
        this.cloneOfHumanID = h.cloneOfHumanID;
        this.notes = h.notes;
        
        this.createdTS = h.createdTS;
        this.createdBy = h.createdBy;
        this.lastUpdatedTS = h.lastUpdatedTS;
        this.lastupdatedBy = h.lastupdatedBy;
        this.deactivatedTS = h.deactivatedTS;
        this.deactivatedBy = h.deactivatedBy;
        
    }
    
    /**
     * @return the linkID
     */
    public int getLinkID() {
        return linkID;
    }

    /**
     * @return the parcelUnitID
     */
    public int getParcelUnitID() {
        return parcelUnitID;
    }

   

    /**
     * @param linkID the linkID to set
     */
    public void setLinkID(int linkID) {
        this.linkID = linkID;
    }

    /**
     * @param parcelUnitID the parcelUnitID to set
     */
    public void setParcelUnitID(int parcelUnitID) {
        this.parcelUnitID = parcelUnitID;
    }

  

    /**
     * @return the role
     */
    public HumanParcelRole getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(HumanParcelRole role) {
        this.role = role;
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

   
    
}
