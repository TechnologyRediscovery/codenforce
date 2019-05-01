/*
 * Copyright (C) 2017 cedba
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
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author cedba
 */
public class User implements Serializable{

    private int userID;
    private RoleType roleType;
    private String username;
    private String password;
    
    private Person person;
    private int personID;
   
    
    private String notes;
    private LocalDateTime activityStartDate;
    private java.util.Date activityStartDateUtilDate;
    private LocalDateTime activityStopDate;
    private java.util.Date activityStopDateUtilDate;
    
    // permissions
    private boolean systemAccessPermitted;
    private AccessKeyCard keyCard;
    
    private boolean isEnforcementOfficial;
    private String badgeNumber;
    private String oriNumber;
    
    
    /**
     * Creates a new instance of User
     */
    public User() {
    }
    
    /**
     * Creates a new instance of User
     * @param id
     * @param rt
     */
    public User(int id, RoleType rt) {
        userID = id;
        roleType = rt;
    }
    
    public void setUserID(int uid){
        userID = uid;
    }
    
    
    
    

    /**
     * @return the userID
     */
    public int getUserID() {
        return userID;
    }

    /**
     * @return the roleType
     */
    public RoleType getRoleType() {
        return roleType;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
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
     * @return the activityStartDate
     */
    public LocalDateTime getActivityStartDate() {
        return activityStartDate;
    }

    /**
     * @param activityStartDate the activityStartDate to set
     */
    public void setActivityStartDate(LocalDateTime activityStartDate) {
        this.activityStartDate = activityStartDate;
    }

    /**
     * @return the activityStopDate
     */
    public LocalDateTime getActivityStopDate() {
        return activityStopDate;
    }

    /**
     * @param activityStopDate the activityStopDate to set
     */
    public void setActivityStopDate(LocalDateTime activityStopDate) {
        this.activityStopDate = activityStopDate;
    }

   

   
    
    
    // no setters for access permissions private variables!!

    /**
     * @return the keyCard
     */
    public AccessKeyCard getKeyCard() {
        return keyCard;
    }

    /**
     * @param keyCard the keyCard to set
     */
    public void setKeyCard(AccessKeyCard keyCard) {
        this.keyCard = keyCard;
    }

    /**
     * @return the systemAccessPermitted
     */
    public boolean isSystemAccessPermitted() {
        return systemAccessPermitted;
    }

    /**
     * @param systemAccessPermitted the systemAccessPermitted to set
     */
    public void setSystemAccessPermitted(boolean systemAccessPermitted) {
        this.systemAccessPermitted = systemAccessPermitted;
    }

    /**
     * @param roleType the roleType to set
     */
    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    /**
     * @return the person
     */
    public Person getPerson() {
        return person;
    }

    /**
     * @param person the person to set
     */
    public void setPerson(Person person) {
        this.person = person;
    }

    /**
     * @return the isEnforcementOfficial
     */
    public boolean isIsEnforcementOfficial() {
        return isEnforcementOfficial;
    }

    /**
     * @param isEnforcementOfficial the isEnforcementOfficial to set
     */
    public void setIsEnforcementOfficial(boolean isEnforcementOfficial) {
        this.isEnforcementOfficial = isEnforcementOfficial;
    }

    /**
     * @return the badgeNumber
     */
    public String getBadgeNumber() {
        return badgeNumber;
    }

    /**
     * @return the oriNumber
     */
    public String getOriNumber() {
        return oriNumber;
    }

    /**
     * @param badgeNumber the badgeNumber to set
     */
    public void setBadgeNumber(String badgeNumber) {
        this.badgeNumber = badgeNumber;
    }

    /**
     * @param oriNumber the oriNumber to set
     */
    public void setOriNumber(String oriNumber) {
        this.oriNumber = oriNumber;
    }

    /**
     * @return the activityStopDateUtilDate
     */
    public java.util.Date getActivityStopDateUtilDate() {
        if(getActivityStopDate() != null){
            activityStopDateUtilDate = java.util.Date.from(getActivityStopDate()
                    .atZone(ZoneId.systemDefault()).toInstant());
            
        }
        return activityStopDateUtilDate;
    }

    /**
     * @param activityStopDateUtilDate the activityStopDateUtilDate to set
     */
    public void setActivityStopDateUtilDate(java.util.Date activityStopDateUtilDate) {
        this.activityStopDateUtilDate = activityStopDateUtilDate;
        if(activityStopDateUtilDate != null){
            activityStopDate = activityStopDateUtilDate
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

    /**
     * @return the activityStartDateUtilDate
     */
    public java.util.Date getActivityStartDateUtilDate() {
        if(getActivityStartDate() != null){
            activityStartDateUtilDate = java.util.Date.from(getActivityStartDate()
                    .atZone(ZoneId.systemDefault()).toInstant());
        }
        return activityStartDateUtilDate;
    }

    /**
     * @param activityStartDateUtilDate the activityStartDateUtilDate to set
     */
    public void setActivityStartDateUtilDate(java.util.Date activityStartDateUtilDate) {
        this.activityStartDateUtilDate = activityStartDateUtilDate;
        if(activityStartDateUtilDate != null){
            activityStartDate = activityStartDateUtilDate
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

    /**
     * @return the personID
     */
    public int getPersonID() {
        if(person != null){
            personID = person.getPersonID();
        }
        return personID;
    }

    /**
     * @param personID the personID to set
     */
    public void setPersonID(int personID) {
        this.personID = personID;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + this.userID;
        hash = 43 * hash + Objects.hashCode(this.roleType);
        hash = 43 * hash + Objects.hashCode(this.username);
        hash = 43 * hash + Objects.hashCode(this.password);
        hash = 43 * hash + Objects.hashCode(this.person);
        hash = 43 * hash + this.personID;
        hash = 43 * hash + Objects.hashCode(this.notes);
        hash = 43 * hash + Objects.hashCode(this.activityStartDate);
        hash = 43 * hash + Objects.hashCode(this.activityStartDateUtilDate);
        hash = 43 * hash + Objects.hashCode(this.activityStopDate);
        hash = 43 * hash + Objects.hashCode(this.activityStopDateUtilDate);
        hash = 43 * hash + (this.systemAccessPermitted ? 1 : 0);
        hash = 43 * hash + Objects.hashCode(this.keyCard);
        hash = 43 * hash + (this.isEnforcementOfficial ? 1 : 0);
        hash = 43 * hash + Objects.hashCode(this.badgeNumber);
        hash = 43 * hash + Objects.hashCode(this.oriNumber);
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
        final User other = (User) obj;
        if (this.userID != other.userID) {
            return false;
        }
       
        return true;
    }
    
}
