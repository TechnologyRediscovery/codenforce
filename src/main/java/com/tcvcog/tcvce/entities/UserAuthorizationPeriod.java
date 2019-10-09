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
    
  private int munLoginRecordID;
  
  private Municipality muni;
  private int userID;
  private boolean defaultMuni;
  
  private LocalDateTime startDate;
  private LocalDateTime stopDate;
  
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
     * @return the defaultMuni
     */
    public boolean isDefaultMuni() {
        return defaultMuni;
    }

    /**
     * @return the startDate
     */
    public LocalDateTime getStartDate() {
        return startDate;
    }

    /**
     * @return the stopDate
     */
    public LocalDateTime getStopDate() {
        return stopDate;
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
     * @return the munLoginRecordID
     */
    public int getMunLoginRecordID() {
        return munLoginRecordID;
    }

   
    /**
     * @param userID the userID to set
     */
    public void setUserID(int userID) {
        this.userID = userID;
    }

    /**
     * @param defaultMuni the defaultMuni to set
     */
    public void setDefaultMuni(boolean defaultMuni) {
        this.defaultMuni = defaultMuni;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    /**
     * @param stopDate the stopDate to set
     */
    public void setStopDate(LocalDateTime stopDate) {
        this.stopDate = stopDate;
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
     * @param munLoginRecordID the munLoginRecordID to set
     */
    public void setMunLoginRecordID(int munLoginRecordID) {
        this.munLoginRecordID = munLoginRecordID;
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
