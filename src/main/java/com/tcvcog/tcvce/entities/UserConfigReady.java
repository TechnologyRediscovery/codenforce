/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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
import java.util.List;

/**
 *
 * @author sylvia
 */
public class UserConfigReady extends User{
    
    private List<UserAuthPeriod> userFullAuthPeriodList;
    
    protected LocalDateTime pswdLastUpdated;
    protected LocalDateTime forcePasswordResetTS;
    
    protected int createdByUserId;
    protected LocalDateTime createdTS;

     /**
     * This constructor is the only way of setting the internals of this
     * security-critical BOB, so all must be assembled by the coordinator
     * and then only read from
     * @param u
     * @param uap
     * @param akc 
     */
    public UserConfigReady(  User u){
        this.userID = u.getUserID();
        this.username = u.getUsername();
        this.person = u.getPerson();
        this.personID = u.getPersonID();
        this.notes = u.getNotes();
        this.badgeNumber = u.getBadgeNumber();
        this.oriNumber = u.getOriNumber();
        
        this.active = u.isActive();
        this.noLoginVirtualUser = u.isNoLoginVirtualUser();
    }
    
    /**
     * @return the userFullAuthPeriodList
     */
    public List<UserAuthPeriod> getUserFullAuthPeriodList() {
        return userFullAuthPeriodList;
    }

    /**
     * @param userFullAuthPeriodList the userFullAuthPeriodList to set
     */
    public void setUserFullAuthPeriodList(List<UserAuthPeriod> userFullAuthPeriodList) {
        this.userFullAuthPeriodList = userFullAuthPeriodList;
    }

    /**
     * @return the pswdLastUpdated
     */
    public LocalDateTime getPswdLastUpdated() {
        return pswdLastUpdated;
    }

    /**
     * @param pswdLastUpdated the pswdLastUpdated to set
     */
    public void setPswdLastUpdated(LocalDateTime pswdLastUpdated) {
        this.pswdLastUpdated = pswdLastUpdated;
    }

    /**
     * @return the forcePasswordResetTS
     */
    public LocalDateTime getForcePasswordResetTS() {
        return forcePasswordResetTS;
    }

    /**
     * @param forcePasswordResetTS the forcePasswordResetTS to set
     */
    public void setForcePasswordResetTS(LocalDateTime forcePasswordResetTS) {
        this.forcePasswordResetTS = forcePasswordResetTS;
    }

    /**
     * @return the createdByUserId
     */
    public int getCreatedByUserId() {
        return createdByUserId;
    }

    /**
     * @return the createdTS
     */
    public LocalDateTime getCreatedTS() {
        return createdTS;
    }
    /**
     * @return the pswdLastUpdated
     */

    /**
     * @param createdByUserId the createdByUserId to set
     */
    public void setCreatedByUserId(int createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    /**
     * @param createdTS the createdTS to set
     */
    public void setCreatedTS(LocalDateTime createdTS) {
        this.createdTS = createdTS;
    }
    /**
     * @return the accessRecord
     */
    /**
     * @param accessRecord the accessRecord to set
     */
            
            
    
    
}
