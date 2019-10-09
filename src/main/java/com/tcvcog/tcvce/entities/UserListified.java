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

import java.util.List;

/**
 *
 * @author sylvia
 */
public class UserListified extends User{
    
    private List<UserAuthorizationPeriod> userAuthPeriodList;

     /**
     * This constructor is the only way of setting the internals of this
     * security-critical BOB, so all must be assembled by the coordinator
     * and then only read from
     * @param u
     * @param uap
     * @param akc 
     */
    public UserListified(  User u){
        this.userID = u.getUserID();
        this.username = u.getUsername();
        this.person = u.getPerson();
        this.personID = u.getPersonID();
        this.notes = u.getNotes();
        this.badgeNumber = u.getBadgeNumber();
        this.oriNumber = u.getOriNumber();
        
        this.active = u.isActive();
        this.createdByUserId = u.getCreatedByUserId();
        this.createdTS = u.getCreatedTS();
        this.noLoginVirtualUser = u.isNoLoginVirtualUser();
        this.pswdLastUpdated = u.getPswdLastUpdated();
        this.forcePasswordResetTS = u.getForcePasswordResetTS();
    }
    
    /**
     * @return the userAuthPeriodList
     */
    public List<UserAuthorizationPeriod> getUserAuthPeriodList() {
        return userAuthPeriodList;
    }

    /**
     * @param userAuthPeriodList the userAuthPeriodList to set
     */
    public void setUserAuthPeriodList(List<UserAuthorizationPeriod> userAuthPeriodList) {
        this.userAuthPeriodList = userAuthPeriodList;
    }
            
            
    
    
}
