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
import java.util.Objects;

/**
 *
 * @author cedba
 */
public class User implements Serializable{

    protected int userID;
    protected RoleType roleType;
    protected String username;
    
    protected Person person;
    protected int personID;

    protected String notes;
    
    protected String badgeNumber;
    protected String oriNumber;
    
    protected boolean noLoginVirtualUser;
    
    protected int createdByUserId;
    protected LocalDateTime createdTS;
    protected LocalDateTime lastUpdatedTS;
    
    protected LocalDateTime deactivatedTS;
    protected int deactivatedByUserID;
    protected int homeMuniID;
    
    
    
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
    // no setters for access permissions private variables!!
    /**
     * @return the keyCard
     */
    /**
     * @param keyCard the keyCard to set
     */   
    
    

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

  

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + this.userID;
        hash = 43 * hash + Objects.hashCode(this.roleType);
        hash = 43 * hash + Objects.hashCode(this.username);
        hash = 43 * hash + Objects.hashCode(this.person);
        hash = 43 * hash + this.personID;
        hash = 43 * hash + Objects.hashCode(this.notes);
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

    /**
     * @return the pswdLastUpdated
     */

    /**
     * @return the active
     */
    public boolean isActive() {
        boolean active = true;
        if(deactivatedTS != null){
            active = false;
        }
        return active;
    }

    /**
     * @return the noLoginVirtualUser
     */
    public boolean isNoLoginVirtualUser() {
        return noLoginVirtualUser;
    }


    /**
     * @param noLoginVirtualUser the noLoginVirtualUser to set
     */
    public void setNoLoginVirtualUser(boolean noLoginVirtualUser) {
        this.noLoginVirtualUser = noLoginVirtualUser;
    }

    /**
     * @return the createdByUserId
     */
    public int getCreatedByUserId() {
        return createdByUserId;
    }

    /**
     * @return the createdTS
     */
    public LocalDateTime getCreatedTS() {
        return createdTS;
    }

    /**
     * @return the pswdLastUpdated
     */
    /**
     * @param createdByUserId the createdByUserId to set
     */
    public void setCreatedByUserId(int createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    /**
     * @param createdTS the createdTS to set
     */
    public void setCreatedTS(LocalDateTime createdTS) {
        this.createdTS = createdTS;
    }

    /**
     * @return the deactivatedTS
     */
    public LocalDateTime getDeactivatedTS() {
        return deactivatedTS;
    }

    /**
     * @return the deactivatedByUserID
     */
    public int getDeactivatedBy() {
        return deactivatedByUserID;
    }

    /**
     * @param deactivatedTS the deactivatedTS to set
     */
    public void setDeactivatedTS(LocalDateTime deactivatedTS) {
        this.deactivatedTS = deactivatedTS;
    }

    /**
     * @param userid
     */
    public void setDeactivatedBy(int userid) {
        this.deactivatedByUserID =userid;
    }

    /**
     * @return the lastUpdatedTS
     */
    public LocalDateTime getLastUpdatedTS() {
        return lastUpdatedTS;
    }

    /**
     * @param lastUpdatedTS the lastUpdatedTS to set
     */
    public void setLastUpdatedTS(LocalDateTime lastUpdatedTS) {
        this.lastUpdatedTS = lastUpdatedTS;
    }

    /**
     * @return the homeMuniID
     */
    public int getHomeMuniID() {
        return homeMuniID;
    }

    /**
     * @param homeMuniID the homeMuniID to set
     */
    public void setHomeMuniID(int homeMuniID) {
        this.homeMuniID = homeMuniID;
    }
    /**
     * @return the accessRecord
     */
    /**
     * @param accessRecord the accessRecord to set
     */
  

}
