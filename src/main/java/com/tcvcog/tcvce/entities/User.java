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

/**
 *
 * @author cedba
 */
public class User implements Serializable{

    private int userID;
    private RoleType roleType;
    private String username;
    // note password is not stored in the business object!
    private String password;
    private int muniCode;
    private Municipality muni;
    private String fName;
    private String lName;
    private String workTitle;
    private String phoneCell;
    private String phoneHome;
    private String phoneWork;
    private String email;
    private String address_street;
    private String address_city;
    private String address_zip;
    private String address_state;
    private String notes;
    private LocalDateTime activityStartDate;
    private LocalDateTime activityStopDate;
    private boolean accessPermitted;
    
    /**
     * Creates a new instance of User
     */
    public User() {
    }


    /**
     * @return the userID
     */
    public int getUserID() {
        return userID;
    }

    /**
     * @param userID the userID to set
     */
    public void setUserID(int userID) {
        this.userID = userID;
    }

    /**
     * @return the roleType
     */
    public RoleType getRoleType() {
        return roleType;
    }

    /**
     * @param roleType the roleType to set
     */
    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
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
     * @return the fName
     */
    public String getFName() {
        return fName;
    }

    /**
     * @param fName the fName to set
     */
    public void setFName(String fName) {
        this.fName = fName;
    }

    /**
     * @return the lName
     */
    public String getLName() {
        return lName;
    }

    /**
     * @param lName the lName to set
     */
    public void setLName(String lName) {
        this.lName = lName;
    }

    /**
     * @return the workTitle
     */
    public String getWorkTitle() {
        return workTitle;
    }

    /**
     * @param workTitle the workTitle to set
     */
    public void setWorkTitle(String workTitle) {
        this.workTitle = workTitle;
    }

    /**
     * @return the phoneCell
     */
    public String getPhoneCell() {
        return phoneCell;
    }

    /**
     * @param phoneCell the phoneCell to set
     */
    public void setPhoneCell(String phoneCell) {
        this.phoneCell = phoneCell;
    }

    /**
     * @return the phoneHome
     */
    public String getPhoneHome() {
        return phoneHome;
    }

    /**
     * @param phoneHome the phoneHome to set
     */
    public void setPhoneHome(String phoneHome) {
        this.phoneHome = phoneHome;
    }

    /**
     * @return the phoneWork
     */
    public String getPhoneWork() {
        return phoneWork;
    }

    /**
     * @param phoneWork the phoneWork to set
     */
    public void setPhoneWork(String phoneWork) {
        this.phoneWork = phoneWork;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the address_street
     */
    public String getAddress_street() {
        return address_street;
    }

    /**
     * @param address_street the address_street to set
     */
    public void setAddress_street(String address_street) {
        this.address_street = address_street;
    }

    /**
     * @return the address_city
     */
    public String getAddress_city() {
        return address_city;
    }

    /**
     * @param address_city the address_city to set
     */
    public void setAddress_city(String address_city) {
        this.address_city = address_city;
    }

    /**
     * @return the address_zip
     */
    public String getAddress_zip() {
        return address_zip;
    }

    /**
     * @param address_zip the address_zip to set
     */
    public void setAddress_zip(String address_zip) {
        this.address_zip = address_zip;
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
     * @return the accessPermitted
     */
    public boolean isAccessPermitted() {
        return accessPermitted;
    }

    /**
     * @param accessPermitted the accessPermitted to set
     */
    public void setAccessPermitted(boolean accessPermitted) {
        this.accessPermitted = accessPermitted;
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

    /**
     * @return the muniCode
     */
    public int getMuniCode() {
        return muniCode;
    }

    /**
     * @param muniCode the muniCode to set
     */
    public void setMuniCode(int muniCode) {
        this.muniCode = muniCode;
    }

    /**
     * @return the address_state
     */
    public String getAddress_state() {
        return address_state;
    }

    /**
     * @param address_state the address_state to set
     */
    public void setAddress_state(String address_state) {
        this.address_state = address_state;
    }

    
    
    
}
