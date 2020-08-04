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
import java.util.ArrayList;
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
    
    private Credential myCredential;
   
    /**
     * Only contains valid auth periods
     */
    protected Map<Municipality, List<UserMuniAuthPeriod>> muniAuthPeriodsMap;
    
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
    public UserAuthorized(User u ){
        if(u != null){
            
            this.userID = u.getUserID();
            this.username = u.getUsername();
            this.person = u.getPerson();
            this.personID = u.getPersonID();
            this.notes = u.getNotes();
            this.badgeNumber = u.getBadgeNumber();
            this.oriNumber = u.getOriNumber();

            this.noLoginVirtualUser = u.isNoLoginVirtualUser();


            this.createdByUserId = u.getCreatedByUserId();
            this.createdTS = u.getCreatedTS();

            this.lastUpdatedTS = u.lastUpdatedTS;
            this.deactivatedByUserID = u.deactivatedByUserID;
            this.deactivatedTS = u.deactivatedTS;

        }
    }
    
    
    /**
     * INJECTION SITE FOR THE USER'S CREDENTIAL OBJECT
     * 
     * @param mc 
     * @throws com.tcvcog.tcvce.domain.AuthorizationException cannot set a credential
     * to null or one with an id that doesn't match the userid in AuthPeriod
     */
    public void setMyCredential(Credential mc) throws AuthorizationException {
         if(mc != null && mc.getGoverningAuthPeriod().getUserID() == userID){
            myCredential = mc;
        }
        else {
            throw new AuthorizationException("cannot set a credential to NULL or one without matching userID in AuthPeriod");
        }
        this.myCredential = mc;
    }

    
    
    /**
     * Convenience method for accessing the governingAuthPeriod's
     * authorized role field
     * @return 
     */
    public RoleType getRole(){
        if(getMyCredential() != null){
            if(getMyCredential().getGoverningAuthPeriod() != null){
                return getMyCredential().getGoverningAuthPeriod().getRole();
            } 
        } 
        return null;
    }
    
    /**
     * Convenience method for pulling out Muni keys from the authorized Muni:AuthPeriod map
     * @return 
     */
    public List<Municipality> getAuthMuniList(){
        if(muniAuthPeriodsMap != null){
            return new ArrayList<>(muniAuthPeriodsMap.keySet());
        } 
        return new ArrayList<>();
    }
    
    public List<UserMuniAuthPeriod> getMuniAuthPeriodListForCredMuni(){
        List<UserMuniAuthPeriod> umapList  = null;
        if(myCredential != null && myCredential.getGoverningAuthPeriod() != null){
            umapList = muniAuthPeriodsMap.get(myCredential.getGoverningAuthPeriod().getMuni());
        }
        return umapList;
        
    }
    
    
    /**
     * Preserved for legacy compatability. DO NOT DEPRECATE.
     * @return the UserAuthorized official access credential object for all system level checks
     */
    public Credential getKeyCard(){
        return getMyCredential();
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
     * @return the muniAuthPeriodsMap
     */
    public Map<Municipality, List<UserMuniAuthPeriod>> getMuniAuthPeriodsMap() {
        if(muniAuthPeriodsMap != null){
            System.out.println("UserAuthorized.getMuniAuthPeriodsMap SIZE: " + muniAuthPeriodsMap.keySet().size());
        }
        return muniAuthPeriodsMap;
    }

    /**
     * @param muniAuthPeriodsMap the muniAuthPeriodsMap to set
     */
    public void setMuniAuthPeriodsMap(Map<Municipality, List<UserMuniAuthPeriod>> muniAuthPeriodsMap) {
        this.muniAuthPeriodsMap = muniAuthPeriodsMap;
    }

    /**
     * @return the credential
     */
    public Credential getMyCredential() {
        return myCredential;
    }
    /**
     * @return the homeMuni
     */
    /**
     * @param homeMuni the homeMuni to set
     */
    /**
     * @return the accessRecord
     */
    /**
     * @param accessRecord the accessRecord to set
     */


    
}
