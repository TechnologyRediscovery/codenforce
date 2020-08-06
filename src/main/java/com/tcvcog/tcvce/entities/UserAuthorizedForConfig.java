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
 * Represents a User ready to be configured but does not contain
 * a credential, so that object can't be used for any funny business
 * @author sylvia
 */
public class UserAuthorizedForConfig extends UserAuthorized{

    private List<UserMuniAuthPeriod> umapList;
    
     /**
     * This constructor is the only way of setting the internals of this
     * security-critical BOB, so all must be assembled by the coordinator
     * and then only read from
     * @param u
     */
    public UserAuthorizedForConfig(UserAuthorized u){
        super(u);
        if(u != null){
            this.muniAuthPeriodsMap = u.muniAuthPeriodsMap;
            this.pswdLastUpdated = u.pswdLastUpdated;
            this.forcePasswordResetTS = u.forcePasswordResetTS;
            this.lastUpdatedTS = u.lastUpdatedTS;
            this.deactivatedByUserID = u.deactivatedByUserID;
            this.deactivatedTS = u.deactivatedTS;
        }
    }

    /**
     * Security logic container to prohibit users from even starting
     * the deactivation process for users with Dev level UMAPs in list
     * @return 
     */
    public boolean allowUserDeactivation(){
        boolean allow = true;
        if(umapList != null){
            for(UserMuniAuthPeriod umap: umapList){
                if(umap.getRole() == RoleType.Developer){
                    allow = false;
                }
            }
        }
        
        return allow;
    }
    
    /**
     * @return the umapList
     */
    public List<UserMuniAuthPeriod> getUmapList() {
        return umapList;
    }

    /**
     * @param umapList the umapList to set
     */
    public void setUmapList(List<UserMuniAuthPeriod> umapList) {
        this.umapList = umapList;
    }
    
    
    
}
