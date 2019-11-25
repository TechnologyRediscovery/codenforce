/*s
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

import java.time.LocalDateTime;

/**
 *
 * @author sylvia
 */
public class UserMuniAuthPeriodLogEntry {
    
    private int logBookEntryID;
    private int userMuniAuthPeriodID;
    private String category;
    private LocalDateTime entryTS;
    private LocalDateTime entryDateOfRecord;
    private int disputedByUserID;
    private LocalDateTime disputedts;
    private String notes;
    private String cookie_jsessionid;
    private String header_remoteaddr;
    private String header_useragent;
    private String header_dateraw;
    private LocalDateTime header_date;
    private String header_cachectl;
    private int audit_usersession_userid;
    private int audit_usercredential_userid;
    private int audit_muni_municode;    
  

    /**
     * @return the disputedts
     */
    public LocalDateTime getDisputedts() {
        return disputedts;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @return the cookie_jsessionid
     */
    public String getCookie_jsessionid() {
        return cookie_jsessionid;
    }

    /**
     * @return the header_remoteaddr
     */
    public String getHeader_remoteaddr() {
        return header_remoteaddr;
    }

    /**
     * @return the header_useragent
     */
    public String getHeader_useragent() {
        return header_useragent;
    }

    /**
     * @return the header_dateraw
     */
    public String getHeader_dateraw() {
        return header_dateraw;
    }

    /**
     * @return the header_date
     */
    public LocalDateTime getHeader_date() {
        return header_date;
    }

    /**
     * @return the header_cachectl
     */
    public String getHeader_cachectl() {
        return header_cachectl;
    }

    /**
     * @return the audit_usersession_userid
     */
    public int getAudit_usersession_userid() {
        return audit_usersession_userid;
    }

    /**
     * @return the audit_usercredential_userid
     */
    public int getAudit_usercredential_userid() {
        return audit_usercredential_userid;
    }

    /**
     * @return the audit_muni_municode
     */
    public int getAudit_muni_municode() {
        return audit_muni_municode;
    }

   
   
   
  
    /**
     * @param disputedts the disputedts to set
     */
    public void setDisputedts(LocalDateTime disputedts) {
        this.disputedts = disputedts;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @param cookie_jsessionid the cookie_jsessionid to set
     */
    public void setCookie_jsessionid(String cookie_jsessionid) {
        this.cookie_jsessionid = cookie_jsessionid;
    }

    /**
     * @param header_remoteaddr the header_remoteaddr to set
     */
    public void setHeader_remoteaddr(String header_remoteaddr) {
        this.header_remoteaddr = header_remoteaddr;
    }

    /**
     * @param header_useragent the header_useragent to set
     */
    public void setHeader_useragent(String header_useragent) {
        this.header_useragent = header_useragent;
    }

    /**
     * @param header_dateraw the header_dateraw to set
     */
    public void setHeader_dateraw(String header_dateraw) {
        this.header_dateraw = header_dateraw;
    }

    /**
     * @param header_date the header_date to set
     */
    public void setHeader_date(LocalDateTime header_date) {
        this.header_date = header_date;
    }

    /**
     * @param header_cachectl the header_cachectl to set
     */
    public void setHeader_cachectl(String header_cachectl) {
        this.header_cachectl = header_cachectl;
    }

    /**
     * @param audit_usersession_userid the audit_usersession_userid to set
     */
    public void setAudit_usersession_userid(int audit_usersession_userid) {
        this.audit_usersession_userid = audit_usersession_userid;
    }

    /**
     * @param audit_usercredential_userid the audit_usercredential_userid to set
     */
    public void setAudit_usercredential_userid(int audit_usercredential_userid) {
        this.audit_usercredential_userid = audit_usercredential_userid;
    }

    /**
     * @param audit_muni_municode the audit_muni_municode to set
     */
    public void setAudit_muni_municode(int audit_muni_municode) {
        this.audit_muni_municode = audit_muni_municode;
    }

    /**
     * @return the entryDateOfRecord
     */
    public LocalDateTime getEntryDateOfRecord() {
        return entryDateOfRecord;
    }

    /**
     * @param entryDateOfRecord the entryDateOfRecord to set
     */
    public void setEntryDateOfRecord(LocalDateTime entryDateOfRecord) {
        this.entryDateOfRecord = entryDateOfRecord;
    }

    /**
     * @return the entryTS
     */
    public LocalDateTime getEntryTS() {
        return entryTS;
    }

    /**
     * @param entryTS the entryTS to set
     */
    public void setEntryTS(LocalDateTime entryTS) {
        this.entryTS = entryTS;
    }

    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

 
    /**
     * @return the userMuniAuthPeriodID
     */
    public int getUserMuniAuthPeriodID() {
        return userMuniAuthPeriodID;
    }

    /**
     * @param userMuniAuthPeriodID the userMuniAuthPeriodID to set
     */
    public void setUserMuniAuthPeriodID(int userMuniAuthPeriodID) {
        this.userMuniAuthPeriodID = userMuniAuthPeriodID;
    }

    /**
     * @return the disputedByUserID
     */
    public int getDisputedByUserID() {
        return disputedByUserID;
    }

    /**
     * @param disputedByUserID the disputedByUserID to set
     */
    public void setDisputedByUserID(int disputedByUserID) {
        this.disputedByUserID = disputedByUserID;
    }

    /**
     * @return the logBookEntryID
     */
    public int getLogBookEntryID() {
        return logBookEntryID;
    }

    /**
     * @param logBookEntryID the logBookEntryID to set
     */
    public void setLogBookEntryID(int logBookEntryID) {
        this.logBookEntryID = logBookEntryID;
    }

    
}
