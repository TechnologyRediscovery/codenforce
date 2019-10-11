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
import java.util.List;

/**
 *
 * @author sylvia
 */
public class UserAuthPeriod extends EntityUtils implements Serializable {
    
  private int userAuthPeriodID;
  
  private Municipality muni;
  private int userID;

  private List<UserMuniAuthPeriodLogEntry> useLog;

  private LocalDateTime startDate;
  private LocalDateTime stopDate;
  
  private LocalDateTime recorddeactivatedTS;
  private RoleType role;
  
  private LocalDateTime createdTS;

  private int createdByUserID;
  private String notes;
  
  private int assignmentRank;
  
  
    /**
     * Locks in the new AuthPeriod to a muni to discourage
     * mischevious activities by downstream users
     * @param m 
     */
    public UserAuthPeriod(Municipality m){
        muni = m;
    }

    public UserAuthPeriod() {
    }
  
  
    /**
     * @return the userID
     */
    public int getUserID() {
        return userID;
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
     * @return the role
     */
    public RoleType getRole() {
        return role;
    }

    /**
     * @return the userAuthPeriodID
     */
    public int getUserAuthPeriodID() {
        return userAuthPeriodID;
    }

   
    /**
     * @param userID the userID to set
     */
    public void setUserID(int userID) {
        this.userID = userID;
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
     * @param role the role to set
     */
    public void setRole(RoleType role) {
        this.role = role;
    }

    /**
     * @param userAuthPeriodID the userAuthPeriodID to set
     */
    public void setUserAuthPeriodID(int userAuthPeriodID) {
        this.userAuthPeriodID = userAuthPeriodID;
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

    /**
     * @return the useLog
     */
    public List<UserMuniAuthPeriodLogEntry> getUseLog() {
        return useLog;
    }

    /**
     * @param useLog the useLog to set
     */
    public void setUseLog(List<UserMuniAuthPeriodLogEntry> useLog) {
        this.useLog = useLog;
    }

    /**
     * @return the createdByUserID
     */
    public int getCreatedByUserID() {
        return createdByUserID;
    }

    /**
     * @param createdByUserID the createdByUserID to set
     */
    public void setCreatedByUserID(int createdByUserID) {
        this.createdByUserID = createdByUserID;
    }

    /**
     * @return the assignmentRank
     */
    public int getAssignmentRank() {
        return assignmentRank;
    }

    /**
     * @param assignmentRank the assignmentRank to set
     */
    public void setAssignmentRank(int assignmentRank) {
        this.assignmentRank = assignmentRank;
    }
    
}
