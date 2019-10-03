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

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @author sylvia
 */
public class UserAuthorizationPeriod extends EntityUtils implements Serializable {
    
  private int muniloginrecordid;
  
  private Municipality muni;
  private int userID;
  private boolean defaultmuni;
  
  private LocalDateTime accessgranteddatestart;
  private LocalDateTime accessgranteddatestop;
  
  private LocalDateTime recorddeactivatedTS;
  private RoleType authorizedRole;
  
  private LocalDateTime createdTS;

  private User createdBy;
  private String notes;
  
  
    /**
     * Locks in the new AuthPeriod to a muni to discourage
     * mischevious activities by downstream users
     * @param m 
     */
    public UserAuthorizationPeriod(Municipality m){
        muni = m;
    }

    public UserAuthorizationPeriod() {
    }
  
  
    /**
     * @return the userID
     */
    public int getUserID() {
        return userID;
    }

    /**
     * @return the defaultmuni
     */
    public boolean isDefaultmuni() {
        return defaultmuni;
    }

    /**
     * @return the accessgranteddatestart
     */
    public LocalDateTime getAccessgranteddatestart() {
        return accessgranteddatestart;
    }

    /**
     * @return the accessgranteddatestop
     */
    public LocalDateTime getAccessgranteddatestop() {
        return accessgranteddatestop;
    }

   

    /**
     * @return the recorddeactivatedTS
     */
    public LocalDateTime getRecorddeactivatedTS() {
        return recorddeactivatedTS;
    }

    /**
     * @return the authorizedRole
     */
    public RoleType getAuthorizedRole() {
        return authorizedRole;
    }

    /**
     * @return the muniloginrecordid
     */
    public int getMuniloginrecordid() {
        return muniloginrecordid;
    }

   
    /**
     * @param userID the userID to set
     */
    public void setUserID(int userID) {
        this.userID = userID;
    }

    /**
     * @param defaultmuni the defaultmuni to set
     */
    public void setDefaultmuni(boolean defaultmuni) {
        this.defaultmuni = defaultmuni;
    }

    /**
     * @param accessgranteddatestart the accessgranteddatestart to set
     */
    public void setAccessgranteddatestart(LocalDateTime accessgranteddatestart) {
        this.accessgranteddatestart = accessgranteddatestart;
    }

    /**
     * @param accessgranteddatestop the accessgranteddatestop to set
     */
    public void setAccessgranteddatestop(LocalDateTime accessgranteddatestop) {
        this.accessgranteddatestop = accessgranteddatestop;
    }

   
    /**
     * @param recorddeactivatedTS the recorddeactivatedTS to set
     */
    public void setRecorddeactivatedTS(LocalDateTime recorddeactivatedTS) {
        this.recorddeactivatedTS = recorddeactivatedTS;
    }

    /**
     * @param authorizedRole the authorizedRole to set
     */
    public void setAuthorizedRole(RoleType authorizedRole) {
        this.authorizedRole = authorizedRole;
    }

    /**
     * @param muniloginrecordid the muniloginrecordid to set
     */
    public void setMuniloginrecordid(int muniloginrecordid) {
        this.muniloginrecordid = muniloginrecordid;
    }

   
    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    /**
     * @return the createdTS
     */
    public LocalDateTime getCreatedTS() {
        return createdTS;
    }

   

    /**
     * @param createdTS the createdTS to set
     */
    public void setCreatedTS(LocalDateTime createdTS) {
        this.createdTS = createdTS;
    }

   
  

    /**
     * @return the createdBy
     */
    public User getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
}
