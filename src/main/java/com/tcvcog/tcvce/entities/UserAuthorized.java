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
import java.util.Map;

/**
 *
 * @author sylvia
 */
public class UserAuthorized extends User{

    private UserAuthorizationPeriod governingAuthPeriod;
    private Map<Municipality, UserAuthorizationPeriod> muniAuthPerMap;
    private final AccessKeyCard keyCard;
    

    
    /**
     * This constructor is the only way of setting the internals of this
     * security-critical BOB, so all must be assembled by the coordinator
     * and then only read from
     * @param u
     * @param uap
     * @param akc 
     */
    public UserAuthorized(  User u, 
                            UserAuthorizationPeriod uap, 
                            AccessKeyCard akc){
        this.userID = u.getUserID();
        this.username = u.getUsername();
        this.person = u.getPerson();
        this.personID = u.getPersonID();
        this.notes = u.getNotes();
        this.badgeNumber = u.getBadgeNumber();
        this.oriNumber = u.getOriNumber();
        
        this.governingAuthPeriod = uap;
        this.keyCard = akc;
        
        this.active = u.isActive();
        this.createdByUserId = u.getCreatedByUserId();
        this.createdTS = u.getCreatedTS();
        this.noLoginVirtualUser = u.isNoLoginVirtualUser();
        this.pswdLastUpdated = u.getPswdLastUpdated();
        this.forcePasswordResetTS = u.getForcePasswordResetTS();
    }
    
    /**
     * Convenience method for accessing the governingAuthPeriod's
     * authorized role field
     * @return 
     */
    public RoleType getRoleType(){
        if(governingAuthPeriod != null){
            return governingAuthPeriod.getAuthorizedRole();
        } else {
            return null;
        }
    }

    /**
     * @return the governingAuthPeriod
     */
    public UserAuthorizationPeriod getGoverningAuthPeriod() {
        return governingAuthPeriod;
    }

    /**
     * @param governingAuthPeriod the governingAuthPeriod to set
     */
    public void setGoverningAuthPeriod(UserAuthorizationPeriod governingAuthPeriod) {
        this.governingAuthPeriod = governingAuthPeriod;
    }

    // no setters for access permissions private variables!!
    /**
     * @return the keyCard
     */
    public AccessKeyCard getKeyCard() {
        return keyCard;
    }

    

   

    /**
     * @param validAuthPeriodList the validAuthPeriodList to set
     */
    public void setValidAuthPeriodList(Map<Municipality, UserAuthorizationPeriod>validAuthPeriodList) {
        this.setMuniAuthPerMap(validAuthPeriodList);
    }

    /**
     * @return the muniAuthPerMap
     */
    public Map<Municipality, UserAuthorizationPeriod> getMuniAuthPerMap() {
        return muniAuthPerMap;
    }

    /**
     * @param muniAuthPerMap the muniAuthPerMap to set
     */
    public void setMuniAuthPerMap(Map<Municipality, UserAuthorizationPeriod> muniAuthPerMap) {
        this.muniAuthPerMap = muniAuthPerMap;
    }

    
}
