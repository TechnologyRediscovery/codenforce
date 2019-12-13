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
public class UserAccessRecord  implements Serializable {
    
  private int muni_municode;
  private int userid;
  private boolean defaultmuni;
  
  private RoleType role;
  
  private LocalDateTime accessgranteddatestart;
  private LocalDateTime accessgranteddatestop;
  
  private LocalDateTime codeofficerstartdate;
  private LocalDateTime codeofficerstopdate;
  
  private LocalDateTime staffstartdate;
  private LocalDateTime staffstopdate;
  
  private LocalDateTime sysadminstartdate;
  private LocalDateTime sysadminstopdate;
  
  private LocalDateTime supportstartdate;
  private LocalDateTime supportstopdate;
  
  private int codeofficerassignmentorder;
  private int staffassignmentorder;
  private int sysadminassignmentorder;
  private int supportassignmentorder;
  
  private int bypasscodeofficerassignmentorder;
  private int bypassstaffassignmentorder;
  private int bypasssysadminassignmentorder;
  private int bypasssupportassignmentorder;
  
  private LocalDateTime recorddeactivatedts;
  
  private int muniloginrecordid;
  
  private LocalDateTime recordcreatedts;
  
  private String orinumber;
  private String badgenumber;

  private int defaultCECaseID;
    /**
     * @return the muni_municode
     */
    public int getMuni_municode() {
        return muni_municode;
    }

    /**
     * @return the userid
     */
    public int getUserid() {
        return userid;
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
     * @return the codeofficerstartdate
     */
    public LocalDateTime getCodeofficerstartdate() {
        return codeofficerstartdate;
    }

    /**
     * @return the codeofficerstopdate
     */
    public LocalDateTime getCodeofficerstopdate() {
        return codeofficerstopdate;
    }

    /**
     * @return the staffstartdate
     */
    public LocalDateTime getStaffstartdate() {
        return staffstartdate;
    }

    /**
     * @return the staffstopdate
     */
    public LocalDateTime getStaffstopdate() {
        return staffstopdate;
    }

    /**
     * @return the sysadminstartdate
     */
    public LocalDateTime getSysadminstartdate() {
        return sysadminstartdate;
    }

    /**
     * @return the sysadminstopdate
     */
    public LocalDateTime getSysadminstopdate() {
        return sysadminstopdate;
    }

    /**
     * @return the supportstartdate
     */
    public LocalDateTime getSupportstartdate() {
        return supportstartdate;
    }

    /**
     * @return the supportstopdate
     */
    public LocalDateTime getSupportstopdate() {
        return supportstopdate;
    }

    /**
     * @return the codeofficerassignmentorder
     */
    public int getCodeofficerassignmentorder() {
        return codeofficerassignmentorder;
    }

    /**
     * @return the staffassignmentorder
     */
    public int getStaffassignmentorder() {
        return staffassignmentorder;
    }

    /**
     * @return the sysadminassignmentorder
     */
    public int getSysadminassignmentorder() {
        return sysadminassignmentorder;
    }

    /**
     * @return the supportassignmentorder
     */
    public int getSupportassignmentorder() {
        return supportassignmentorder;
    }

    /**
     * @return the bypasscodeofficerassignmentorder
     */
    public int getBypasscodeofficerassignmentorder() {
        return bypasscodeofficerassignmentorder;
    }

    /**
     * @return the bypassstaffassignmentorder
     */
    public int getBypassstaffassignmentorder() {
        return bypassstaffassignmentorder;
    }

    /**
     * @return the bypasssysadminassignmentorder
     */
    public int getBypasssysadminassignmentorder() {
        return bypasssysadminassignmentorder;
    }

    /**
     * @return the bypasssupportassignmentorder
     */
    public int getBypasssupportassignmentorder() {
        return bypasssupportassignmentorder;
    }

    /**
     * @return the recorddeactivatedts
     */
    public LocalDateTime getRecorddeactivatedts() {
        return recorddeactivatedts;
    }

    /**
     * @return the role
     */
    public RoleType getRole() {
        return role;
    }

    /**
     * @return the muniloginrecordid
     */
    public int getMuniloginrecordid() {
        return muniloginrecordid;
    }

    /**
     * @return the recordcreatedts
     */
    public LocalDateTime getRecordcreatedts() {
        return recordcreatedts;
    }

    /**
     * @param muni_municode the muni_municode to set
     */
    public void setMuni_municode(int muni_municode) {
        this.muni_municode = muni_municode;
    }

    /**
     * @param userid the userid to set
     */
    public void setUserid(int userid) {
        this.userid = userid;
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
     * @param codeofficerstartdate the codeofficerstartdate to set
     */
    public void setCodeofficerstartdate(LocalDateTime codeofficerstartdate) {
        this.codeofficerstartdate = codeofficerstartdate;
    }

    /**
     * @param codeofficerstopdate the codeofficerstopdate to set
     */
    public void setCodeofficerstopdate(LocalDateTime codeofficerstopdate) {
        this.codeofficerstopdate = codeofficerstopdate;
    }

    /**
     * @param staffstartdate the staffstartdate to set
     */
    public void setStaffstartdate(LocalDateTime staffstartdate) {
        this.staffstartdate = staffstartdate;
    }

    /**
     * @param staffstopdate the staffstopdate to set
     */
    public void setStaffstopdate(LocalDateTime staffstopdate) {
        this.staffstopdate = staffstopdate;
    }

    /**
     * @param sysadminstartdate the sysadminstartdate to set
     */
    public void setSysadminstartdate(LocalDateTime sysadminstartdate) {
        this.sysadminstartdate = sysadminstartdate;
    }

    /**
     * @param sysadminstopdate the sysadminstopdate to set
     */
    public void setSysadminstopdate(LocalDateTime sysadminstopdate) {
        this.sysadminstopdate = sysadminstopdate;
    }

    /**
     * @param supportstartdate the supportstartdate to set
     */
    public void setSupportstartdate(LocalDateTime supportstartdate) {
        this.supportstartdate = supportstartdate;
    }

    /**
     * @param supportstopdate the supportstopdate to set
     */
    public void setSupportstopdate(LocalDateTime supportstopdate) {
        this.supportstopdate = supportstopdate;
    }

    /**
     * @param codeofficerassignmentorder the codeofficerassignmentorder to set
     */
    public void setCodeofficerassignmentorder(int codeofficerassignmentorder) {
        this.codeofficerassignmentorder = codeofficerassignmentorder;
    }

    /**
     * @param staffassignmentorder the staffassignmentorder to set
     */
    public void setStaffassignmentorder(int staffassignmentorder) {
        this.staffassignmentorder = staffassignmentorder;
    }

    /**
     * @param sysadminassignmentorder the sysadminassignmentorder to set
     */
    public void setSysadminassignmentorder(int sysadminassignmentorder) {
        this.sysadminassignmentorder = sysadminassignmentorder;
    }

    /**
     * @param supportassignmentorder the supportassignmentorder to set
     */
    public void setSupportassignmentorder(int supportassignmentorder) {
        this.supportassignmentorder = supportassignmentorder;
    }

    /**
     * @param bypasscodeofficerassignmentorder the bypasscodeofficerassignmentorder to set
     */
    public void setBypasscodeofficerassignmentorder(int bypasscodeofficerassignmentorder) {
        this.bypasscodeofficerassignmentorder = bypasscodeofficerassignmentorder;
    }

    /**
     * @param bypassstaffassignmentorder the bypassstaffassignmentorder to set
     */
    public void setBypassstaffassignmentorder(int bypassstaffassignmentorder) {
        this.bypassstaffassignmentorder = bypassstaffassignmentorder;
    }

    /**
     * @param bypasssysadminassignmentorder the bypasssysadminassignmentorder to set
     */
    public void setBypasssysadminassignmentorder(int bypasssysadminassignmentorder) {
        this.bypasssysadminassignmentorder = bypasssysadminassignmentorder;
    }

    /**
     * @param bypasssupportassignmentorder the bypasssupportassignmentorder to set
     */
    public void setBypasssupportassignmentorder(int bypasssupportassignmentorder) {
        this.bypasssupportassignmentorder = bypasssupportassignmentorder;
    }

    /**
     * @param recorddeactivatedts the recorddeactivatedts to set
     */
    public void setRecorddeactivatedts(LocalDateTime recorddeactivatedts) {
        this.recorddeactivatedts = recorddeactivatedts;
    }

    /**
     * @param role the role to set
     */
    public void setRole(RoleType role) {
        this.role = role;
    }

    /**
     * @param muniloginrecordid the muniloginrecordid to set
     */
    public void setMuniloginrecordid(int muniloginrecordid) {
        this.muniloginrecordid = muniloginrecordid;
    }

    /**
     * @param recordcreatedts the recordcreatedts to set
     */
    public void setRecordcreatedts(LocalDateTime recordcreatedts) {
        this.recordcreatedts = recordcreatedts;
    }

    /**
     * @return the orinumber
     */
    public String getOrinumber() {
        return orinumber;
    }

    /**
     * @return the badgenumber
     */
    public String getBadgenumber() {
        return badgenumber;
    }

    /**
     * @param orinumber the orinumber to set
     */
    public void setOrinumber(String orinumber) {
        this.orinumber = orinumber;
    }

    /**
     * @param badgenumber the badgenumber to set
     */
    public void setBadgenumber(String badgenumber) {
        this.badgenumber = badgenumber;
    }

    /**
     * @return the defaultCECaseID
     */
    public int getDefaultCECaseID() {
        return defaultCECaseID;
    }

    /**
     * @param defaultCECaseID the defaultCECaseID to set
     */
    public void setDefaultCECaseID(int defaultCECaseID) {
        this.defaultCECaseID = defaultCECaseID;
    }
    
}
