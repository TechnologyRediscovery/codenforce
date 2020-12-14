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
import java.util.Objects;

/**
 *
 * @author sylvia
 */
public class UserMuniAuthPeriod extends BOb implements Serializable, Comparable<UserMuniAuthPeriod> {

    private int userMuniAuthPeriodID;

    private Municipality muni;
    private int userID;

    private RoleType role;
    
    private List<UserMuniAuthPeriodLogEntry> periodActivityLogBook;

    /**
     * Timestamped business logic for UMAP validity is implemented.
     * Works in tandem with the member validatedTS: if this is a valid UMAP,
     * both this and validatedTS will have the same value. An invalid UAMP
     * will be timestamped when valuated but denied a validatedTS.
     * 
     * For Javaland only since validity is based on the current date/time
     */
    private LocalDateTime validityEvaluatedTS;
    
    /**
     * Security signaling timestamp upon validation by the UserCoordinator
     * For Javaland only since validity is based on the current date/time
     */
    private LocalDateTime validatedTS;

    private LocalDateTime startDate;
    private LocalDateTime stopDate;

    private LocalDateTime recorddeactivatedTS;
    

    private LocalDateTime createdTS;

    private int createdByUserID;
    private String notes;

    private int assignmentRelativeOrder;
    
    /**
     * Half-baked attempt at some security on BOBs
     * @return 
     */
    public boolean isValid(){
        boolean v = false;
        if(validityEvaluatedTS != null 
                &&
            validatedTS != null
                &&
            validityEvaluatedTS.equals(validatedTS)){
            v = true;
        }
        return v;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.userMuniAuthPeriodID;
        hash = 67 * hash + Objects.hashCode(this.muni);
        hash = 67 * hash + this.userID;
        hash = 67 * hash + Objects.hashCode(this.periodActivityLogBook);
        hash = 67 * hash + Objects.hashCode(this.startDate);
        hash = 67 * hash + Objects.hashCode(this.stopDate);
        hash = 67 * hash + Objects.hashCode(this.recorddeactivatedTS);
        hash = 67 * hash + Objects.hashCode(this.role);
        hash = 67 * hash + Objects.hashCode(this.createdTS);
        hash = 67 * hash + this.createdByUserID;
        hash = 67 * hash + Objects.hashCode(this.notes);
        hash = 67 * hash + this.assignmentRelativeOrder;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserMuniAuthPeriod other = (UserMuniAuthPeriod) obj;
        if (this.userMuniAuthPeriodID != other.userMuniAuthPeriodID) {
            return false;
        }
//        if (this.userID != other.userID) {
//            return false;
//        }
//        if (this.createdByUserID != other.createdByUserID) {
//            return false;
//        }
//        if (this.assignmentRelativeOrder != other.assignmentRelativeOrder) {
//            return false;
//        }
//        if (!Objects.equals(this.notes, other.notes)) {
//            return false;
//        }
//        if (!Objects.equals(this.muni, other.muni)) {
//            return false;
//        }
//        if (!Objects.equals(this.periodActivityLogBook, other.periodActivityLogBook)) {
//            return false;
//        }
//        if (!Objects.equals(this.startDate, other.startDate)) {
//            return false;
//        }
//        if (!Objects.equals(this.stopDate, other.stopDate)) {
//            return false;
//        }
//        if (!Objects.equals(this.recorddeactivatedTS, other.recorddeactivatedTS)) {
//            return false;
//        }
//        if (this.role != other.role) {
//            return false;
//        }
//        if (!Objects.equals(this.createdTS, other.createdTS)) {
//            return false;
//        }
        return true;
    }
    
  
    /**
     * Orders UMAP for session creation by choosing the highest role rank 
     * in the list. Tied rank valid UMAPs are assigned based on assingment order
     * @param o
     * @return 
     */
    @Override
    public int compareTo(UserMuniAuthPeriod o) {
        if(this.role.getRank() > o.getRole().getRank()){
            return 1;
        } else if(this.role.getRank() == o.getRole().getRank()){
            if(this.assignmentRelativeOrder > o.getAssignmentRelativeOrder()){
                return 1;
            } else if(this.getAssignmentRelativeOrder() == o.getAssignmentRelativeOrder()){
                return this.getCreatedTS().compareTo(o.getCreatedTS());
            } else {
                return -1;
            }
        } else {
            return 0;
        }
    }
  
    /**
     * Locks in the new AuthPeriod to a muni to discourage
     * mischevious activities by downstream users
     * @param m 
     */
    public UserMuniAuthPeriod(Municipality m){
        muni = m;
    }

    public UserMuniAuthPeriod() {
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
     * @return the userMuniAuthPeriodID
     */
    public int getUserMuniAuthPeriodID() {
        return userMuniAuthPeriodID;
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
     * @param userMuniAuthPeriodID the userMuniAuthPeriodID to set
     */
    public void setUserMuniAuthPeriodID(int userMuniAuthPeriodID) {
        this.userMuniAuthPeriodID = userMuniAuthPeriodID;
    }

   
    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    
    public void setMuni(Municipality m){
        muni = m;
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
     * @return the periodActivityLogBook
     */
    public List<UserMuniAuthPeriodLogEntry> getPeriodActivityLogBook() {
        return periodActivityLogBook;
    }

    /**
     * @param periodActivityLogBook the periodActivityLogBook to set
     */
    public void setPeriodActivityLogBook(List<UserMuniAuthPeriodLogEntry> periodActivityLogBook) {
        this.periodActivityLogBook = periodActivityLogBook;
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
     * @return the assignmentRelativeOrder
     */
    public int getAssignmentRelativeOrder() {
        return assignmentRelativeOrder;
    }

    /**
     * @param assignmentRelativeOrder the assignmentRelativeOrder to set
     */
    public void setAssignmentRelativeOrder(int assignmentRelativeOrder) {
        this.assignmentRelativeOrder = assignmentRelativeOrder;
    }

    /**
     * @return the validatedTS
     */
    public LocalDateTime getValidatedTS() {
        return validatedTS;
    }

    /**
     * @param validatedTS the validatedTS to set
     */
    public void setValidatedTS(LocalDateTime validatedTS) {
        this.validatedTS = validatedTS;
    }

    /**
     * @return the validityEvaluatedTS
     */
    public LocalDateTime getValidityEvaluatedTS() {
        return validityEvaluatedTS;
    }

    /**
     * @param validityEvaluatedTS the validityEvaluatedTS to set
     */
    public void setValidityEvaluatedTS(LocalDateTime validityEvaluatedTS) {
        this.validityEvaluatedTS = validityEvaluatedTS;
    }

    /**
     * @return the stopDateUtilDate
     */
    public java.util.Date getStopDateUtilDate() {
        return convertUtilDate(stopDate);
    }

    /**
     * @param stopDateUtilDate the stopDateUtilDate to set
     */
    public void setStopDateUtilDate(java.util.Date stopDateUtilDate) {
        stopDate = convertUtilDate(stopDateUtilDate);
    }

    /**
     * @return the startDateUtilDate
     */
    public java.util.Date getStartDateUtilDate() {
        return convertUtilDate(startDate);
    }

    /**
     * @param startDateUtilDate the startDateUtilDate to set
     */
    public void setStartDateUtilDate(java.util.Date startDateUtilDate) {
        startDate = convertUtilDate(startDateUtilDate);
    }
    
}