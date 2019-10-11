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

import com.tcvcog.tcvce.domain.AuthorizationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sylvia
 */
public class UserAuthorized extends User{
    
    /**
     * Remember that the notion of a Credential only exists in Java land, since a User 
     * is never "in" the database, doing stuff.
     */
    private UserAuthCredential credential;

    private Map<Municipality, UserAuthPeriod> muniAuthPeriodMap;
    
    protected LocalDateTime pswdLastUpdated;
    protected LocalDateTime forcePasswordResetTS;

    
    /**
     * This constructor is the only way of setting the internals of this
     * security-critical BOB, so all must be assembled by the coordinator
     * and then only read from
     * @param u
     * @param uap
     * @param akc 
     */
    public UserAuthorized(  User u ){
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
     * Convenience method for accessing the governingAuthPeriod's
     * authorized role field
     * @return 
     */
    public RoleType getRoleType(){
        if(credential != null){
            if(credential.getGoverningAuthPeriod() != null){
                return credential.getGoverningAuthPeriod().getRole();
            } 
        } 
        return null;
    }


    public void setCredential(UserAuthCredential cr) throws AuthorizationException{
        if(cr != null && cr.getGoverningAuthPeriod().getUserID() == userID){
            credential = cr;
        }
        else {
            throw new AuthorizationException("cannot set a credential to NULL or one without matching userID in AuthPeriod");
        }
    }
    /**
     * @return the credential
     */
    public UserAuthCredential getCredential() {
        return credential;
    }

    /**
     * @param validAuthPeriodList the validAuthPeriodList to set
     */
    public void setValidAuthPeriodList(Map<Municipality, UserAuthPeriod>validAuthPeriodList) {
        this.setMuniAuthPeriodMap(validAuthPeriodList);
    }

    /**
     * @return the muniAuthPeriodMap
     */
    public Map<Municipality, UserAuthPeriod> getMuniAuthPeriodMap() {
        return muniAuthPeriodMap;
    }

    /**
     * @param muniAuthPeriodMap the muniAuthPeriodMap to set
     */
    public void setMuniAuthPeriodMap(Map<Municipality, UserAuthPeriod> muniAuthPeriodMap) {
        this.muniAuthPeriodMap = muniAuthPeriodMap;
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
     * @return the accessRecord
     */
    /**
     * @param accessRecord the accessRecord to set
     */

    
}
