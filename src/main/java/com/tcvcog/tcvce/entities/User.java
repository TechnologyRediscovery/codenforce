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

/**
 *
 * @author cedba
 */
public class User implements Serializable{

    private int userID;
    private RoleType roleType;
    private String username;
    private String password;
    private List<Municipality> authMunis;
    
    // To be deprecated 
    private Municipality muni;
    
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
     * Creates a new instance of User
     * @param munis The municipality objects for the munis this user
     * can search and manipulate data within
     */
    public User(LinkedList<Municipality> munis) {
        authMunis = munis;
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

   

   
    

    /**
     * @return the authMunis
     */
    public List<Municipality> getAuthMunis() {
        return authMunis;
    }

    /**
     * 
     * @param ml 
     */
    public void setAuthMuis(List<Municipality> ml) {
        authMunis = ml;
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
    
}
