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
 * Second attempt that turned out to be the wrong approach 
 * for encoding our linked objects: they must inherit their
 * parent object fields, not the tracking fields
 * @deprecated back to IFace_trackedEntityLink
 * @author sylvia
 */
public class LinkedEntity {
    
    protected LocalDateTime linkCreatedTS;
    protected User linkCreatedBy;
    
    protected LocalDateTime linkLastUpdatedTS;
    protected User linkLastUpdatedBy;
    
    protected LocalDateTime linkDeactivatedTS;
    protected User linkDeactivatedBy;
    
    protected String linkNotes;
    
    public boolean isLinkDeactivated() {
        return linkDeactivatedTS != null;
    }
    /**
     * @return the linkNotes
     */
    public String getLinkNotes() {
        return linkNotes;
    }

    /**
     * @param linkNotes the linkNotes to set
     */
    public void setLinkNotes(String linkNotes) {
        this.linkNotes = linkNotes;
    }

    /**
     * @return the linkCreatedTS
     */
    public LocalDateTime getLinkCreatedTS() {
        return linkCreatedTS;
    }

    /**
     * @return the linkCreatedBy
     */
    public User getLinkCreatedBy() {
        return linkCreatedBy;
    }

    /**
     * @return the linkLastUpdatedTS
     */
    public LocalDateTime getLinkLastUpdatedTS() {
        return linkLastUpdatedTS;
    }

    /**
     * @return the linkLastUpdatedBy
     */
    public User getLinkLastUpdatedBy() {
        return linkLastUpdatedBy;
    }

    /**
     * @return the linkDeactivatedTS
     */
    public LocalDateTime getLinkDeactivatedTS() {
        return linkDeactivatedTS;
    }

    /**
     * @return the linkDeactivatedBy
     */
    public User getLinkDeactivatedBy() {
        return linkDeactivatedBy;
    }

    /**
     * @param linkCreatedTS the linkCreatedTS to set
     */
    public void setLinkCreatedTS(LocalDateTime linkCreatedTS) {
        this.linkCreatedTS = linkCreatedTS;
    }

    /**
     * @param linkCreatedBy the linkCreatedBy to set
     */
    public void setLinkCreatedBy(User linkCreatedBy) {
        this.linkCreatedBy = linkCreatedBy;
    }

    /**
     * @param linkLastUpdatedTS the linkLastUpdatedTS to set
     */
    public void setLinkLastUpdatedTS(LocalDateTime linkLastUpdatedTS) {
        this.linkLastUpdatedTS = linkLastUpdatedTS;
    }

    /**
     * @param linkLastUpdatedBy the linkLastUpdatedBy to set
     */
    public void setLinkLastUpdatedBy(User linkLastUpdatedBy) {
        this.linkLastUpdatedBy = linkLastUpdatedBy;
    }

    /**
     * @param linkDeactivatedTS the linkDeactivatedTS to set
     */
    public void setLinkDeactivatedTS(LocalDateTime linkDeactivatedTS) {
        this.linkDeactivatedTS = linkDeactivatedTS;
    }

    /**
     * @param linkDeactivatedBy the linkDeactivatedBy to set
     */
    public void setLinkDeactivatedBy(User linkDeactivatedBy) {
        this.linkDeactivatedBy = linkDeactivatedBy;
    }
}
