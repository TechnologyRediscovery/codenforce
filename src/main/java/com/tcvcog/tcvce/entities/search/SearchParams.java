/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.EntityUtils;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

/**
 * Encapsulates municipality restrictions and start/end
 * dates for searching. Types for all three commonly used date
 * formats system wide are included in the params object
 * and the conversions are made automatically based on the
 * LocalDateTime value, allowing objects on the front end and 
 * integration end to just grab the date Type they need and go!
 * @author Sylvia, no wait, Lauren
 */

public  class           SearchParams 
        implements      Serializable{
    
    private String filterName;
    private String filterDescription;
    
    private StringBuilder sql;
    
    // filter common COM-1: Municipality
    private RoleType muni_rtMin;
    private boolean muni_ctl;
    private Municipality muni_val;
    
    // set #2: Date
    // subclasses will have a DateEnum member to specify which date field
    private RoleType date_rtMin;
    private boolean date_startEnd_ctl;
    private IFace_dateFieldHolder date_field;
    private LocalDateTime date_start_val;
    private LocalDateTime date_end_val;

    private boolean date_relativeDates_ctl;
    private int date_relativeDates_start_val;
    private int date_realtiveDates_end_val;
    
    // set #3: User
    private RoleType user_rtMin;
    private boolean user_ctl;
    private IFace_userFieldHolder user_field;
    private User user_val;
    
    // set #4: BOb ID
    private RoleType bobID_rtMin;
    private boolean bobID_ctl;
    private int bobID_val;
    
    // set #5: Result count limit
    private RoleType limitResultCount_rtMin;
    private boolean limitResultCount_ctl;
    private int limitResultCount_val;
    
    // set #6: Active
    private RoleType active_rtMin;
    private boolean active_ctl;
    private boolean active_val;
    
    private StringBuilder log;
    
   public SearchParams(){
       sql = new StringBuilder();
       log = new StringBuilder();
   }
   
   
    @Override
   public String toString(){
       StringBuilder sb = new StringBuilder();
       sb.append(filterName);
       sb.append(" ");
       sb.append(filterDescription);
       return sb.toString();
   }
   
   
   
   public void appendSQL(String str){
       if(str != null){
            sql.append(str);
       }
   }
   
   public void clearSQL(){
       System.out.println("SearchParams.clearSQL");
       sql = new StringBuilder();
   }
   
   
   public String extractRawSQL(){
        return sql.toString();
   }
   
   public void appendToParamLog(String str){
       if(str != null){
           log.append(Constants.FMT_HTML_BREAK);
           log.append(Constants.FMT_SPLAT);
           log.append(str);
       }
   }
   
   public String getParamLog(){
        return log.toString();
   }
   
   public void clearParamLog(){
       log = new StringBuilder();
   }
   
   
   

    /**
     * @return the muni_rtMin
     */
    public RoleType getMuni_rtMin() {
        return muni_rtMin;
    }

    /**
     * @param muni_rtMin the muni_rtMin to set
     */
    public void setMuni_rtMin(RoleType muni_rtMin) {
        this.muni_rtMin = muni_rtMin;
    }
   
   
    
    /**
     * @return the muni_val
     */
    public Municipality getMuni_val() {
        return muni_val;
    }

    /**
     * @param muni_val the muni_val to set
     */
    public void setMuni_val(Municipality muni_val) {
        this.muni_val = muni_val;
    }

    /**
     * @return the startDate_val_utilDate
     */
    public java.util.Date getStartDate_val_utilDate() {
        return EntityUtils.convertUtilDate(date_start_val);
    }

    /**
     * @param startDate_val_utilDate the startDate_val_utilDate to set
     */
    public void setStartDate_val_utilDate(java.util.Date startDate_val_utilDate) {
        date_start_val = EntityUtils.convertUtilDate(startDate_val_utilDate);
    }

    /**
     * @return the endDate_val_utilDate
     */
    public java.util.Date getEndDate_val_utilDate() {
        return EntityUtils.convertUtilDate(date_end_val);
    }

    /**
     * @param endDate_val_utilDate the endDate_val_utilDate to set
     */
    public void setEndDate_val_utilDate(java.util.Date endDate_val_utilDate) {
        date_end_val = EntityUtils.convertUtilDate(endDate_val_utilDate);
    }

    /**
     * @return the date_start_val
     */
    public LocalDateTime getDate_start_val() {
        return date_start_val;
    }

    /**
     * @return the date_end_val
     */
    public LocalDateTime getDate_end_val() {
        return date_end_val;
    }

    /**
     * @return the date_start_val in Util date form.
     */
    public Date getDate_start_val_util() {
        return EntityUtils.convertUtilDate(date_start_val);
    }

    /**
     * @return the date_end_val in Util date form.
     */
    public Date getDate_end_val_util() {
        return EntityUtils.convertUtilDate(date_end_val);
    }
    
    /**
     * @param date_start_val the date_start_val to set
     */
    public void setDate_start_val(LocalDateTime date_start_val) {
        this.date_start_val = date_start_val;
    }

    /**
     * @param date_end_val the date_end_val to set
     */
    public void setDate_end_val(LocalDateTime date_end_val) {
        this.date_end_val = date_end_val;
    }
    
    /**
     * @param dateStartUtil the date_start_val to set in Util date form
     */
    public void setDate_start_val_util(Date dateStartUtil) {
        date_start_val = EntityUtils.convertUtilDate(dateStartUtil);
    }

    /**
     * @param dateEndUtil the date_end_val to set in Util date form
     */
    public void setDate_end_val_util(Date dateEndUtil) {
        date_end_val = EntityUtils.convertUtilDate(dateEndUtil);
    }

    /**
     * @return the startDate_val_SQLDate
     */
    public java.sql.Timestamp getDateStart_val_sql() {
        if(date_relativeDates_ctl){
            return java.sql.Timestamp.valueOf(LocalDateTime.now().plusDays(date_relativeDates_start_val));
        } else {
            if(date_start_val != null){
                return java.sql.Timestamp.valueOf(getDate_start_val());
            }
        }
        return java.sql.Timestamp.valueOf(LocalDateTime.of(2100, 1, 1, 0, 0));
    }

    /**
     * @return the endDate_val_SQLDate
     */
    public java.sql.Timestamp getDateEnd_val_sql() {
        if(date_relativeDates_ctl){
            return java.sql.Timestamp.valueOf(LocalDateTime.now().plusDays(date_realtiveDates_end_val));
        } else {
            if(date_end_val != null){
                return java.sql.Timestamp.valueOf(getDate_end_val());
            }
        }
        // the EPOCH!
        return java.sql.Timestamp.valueOf(LocalDateTime.of(1970, 1, 1, 0, 0));
    }

    /**
     * @return the date_startEnd_ctl
     */
    public boolean isDate_startEnd_ctl() {
        return date_startEnd_ctl;
    }

    /**
     * @param date_startEnd_ctl the date_startEnd_ctl to set
     */
    public void setDate_startEnd_ctl(boolean date_startEnd_ctl) {
        this.date_startEnd_ctl = date_startEnd_ctl;
    }

    /**
     * @return the limitResultCount_ctl
     */
    public boolean isLimitResultCount_ctl() {
        return limitResultCount_ctl;
    }

    /**
     * @param limitResultCount_ctl the limitResultCount_ctl to set
     */
    public void setLimitResultCount_ctl(boolean limitResultCount_ctl) {
        this.limitResultCount_ctl = limitResultCount_ctl;
    }

    /**
     * @return the bobID_val
     */
    public int getBobID_val() {
        return bobID_val;
    }

    /**
     * @param bobID_val the bobID_val to set
     */
    public void setBobID_val(int bobID_val) {
        this.bobID_val = bobID_val;
    }

    /**
     * @return the filterByObjectID
     */
    public boolean isBobID_ctl() {
        return bobID_ctl;
    }

    /**
     * @param bobID_ctl the filterByObjectID to set
     */
    public void setBobID_ctl(boolean bobID_ctl) {
        this.bobID_ctl = bobID_ctl;
    }

    /**
     * @return the filterName
     */
    public String getFilterName() {
        return filterName;
    }

    /**
     * @param filterName the filterName to set
     */
    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    /**
     * @return the filterDescription
     */
    public String getFilterDescription() {
        return filterDescription;
    }

    /**
     * @param filterDescription the filterDescription to set
     */
    public void setFilterDescription(String filterDescription) {
        this.filterDescription = filterDescription;
    }

    /**
     * @return the muni_ctl
     */
    public boolean isMuni_ctl() {
        return muni_ctl;
    }

    /**
     * @param muni_ctl the muni_ctl to set
     */
    public void setMuni_ctl(boolean muni_ctl) {
        this.muni_ctl = muni_ctl;
    }

    /**
     * @return the date_relativeDates_ctl
     */
    public boolean isDate_relativeDates_ctl() {
        return date_relativeDates_ctl;
    }

    /**
     * @return the date_relativeDates_start_val
     */
    public int getDate_relativeDates_start_val() {
        return date_relativeDates_start_val;
    }

    /**
     * @return the date_realtiveDates_end_val
     */
    public int getDate_realtiveDates_end_val() {
        return date_realtiveDates_end_val;
    }

    /**
     * @param date_relativeDates_ctl the date_relativeDates_ctl to set
     */
    public void setDate_relativeDates_ctl(boolean date_relativeDates_ctl) {
        this.date_relativeDates_ctl = date_relativeDates_ctl;
    }

    /**
     * @param date_relativeDates_start_val the date_relativeDates_start_val to set
     */
    public void setDate_relativeDates_start_val(int date_relativeDates_start_val) {
        this.date_relativeDates_start_val = date_relativeDates_start_val;
    }

    /**
     * @param date_realtiveDates_end_val the date_realtiveDates_end_val to set
     */
    public void setDate_realtiveDates_end_val(int date_realtiveDates_end_val) {
        this.date_realtiveDates_end_val = date_realtiveDates_end_val;
    }


    /**
     * Used for printing search params on reports: since the correct SQL timestamp
     * is the relevant field that we draw from for building the actual SQL query
     * and those getters take into account relative date preferences, we need
     * to first convert the SQL-compatible date back to LocalDateTime and then
     * make it pretty for printing
     * @return the startDatePretty
     */
    public String getStartDatePretty() {
        if(date_start_val != null){
            return EntityUtils.getPrettyDate(date_start_val);
        }
        return null;
    }

    /**
     * @return the endDatePretty
     */
    public String getEndDatePretty() {
        if(date_end_val != null){
            return EntityUtils.getPrettyDate(date_end_val);
        }
        return null;
    }

    /**
     * @return the active_ctl
     */
    public boolean isActive_ctl() {
        return active_ctl;
    }

    /**
     * @return the active_val
     */
    public boolean isActive_val() {
        return active_val;
    }

    /**
     * @param active_ctl the active_ctl to set
     */
    public void setActive_ctl(boolean active_ctl) {
        this.active_ctl = active_ctl;
    }

    /**
     * @param active_val the active_val to set
     */
    public void setActive_val(boolean active_val) {
        this.active_val = active_val;
    }

    /**
     * @return the date_rtMin
     */
    public RoleType getDate_rtMin() {
        return date_rtMin;
    }

    /**
     * @param date_rtMin the date_rtMin to set
     */
    public void setDate_rtMin(RoleType date_rtMin) {
        this.date_rtMin = date_rtMin;
    }

    /**
     * @return the bobID_rtMin
     */
    public RoleType getBobID_rtMin() {
        return bobID_rtMin;
    }

    /**
     * @param bobID_rtMin the bobID_rtMin to set
     */
    public void setBobID_rtMin(RoleType bobID_rtMin) {
        this.bobID_rtMin = bobID_rtMin;
    }

    /**
     * @return the limitResultCount_val
     */
    public int getLimitResultCount_val() {
        return limitResultCount_val;
    }

    /**
     * @param limitResultCount_val the limitResultCount_val to set
     */
    public void setLimitResultCount_val(int limitResultCount_val) {
        this.limitResultCount_val = limitResultCount_val;
    }

    /**
     * @return the limitResultCount_rtMin
     */
    public RoleType getLimitResultCount_rtMin() {
        return limitResultCount_rtMin;
    }

    /**
     * @param limitResultCount_rtMin the limitResultCount_rtMin to set
     */
    public void setLimitResultCount_rtMin(RoleType limitResultCount_rtMin) {
        this.limitResultCount_rtMin = limitResultCount_rtMin;
    }

    /**
     * @return the user_ctl
     */
    public boolean isUser_ctl() {
        return user_ctl;
    }

    /**
     * @param user_ctl the user_ctl to set
     */
    public void setUser_ctl(boolean user_ctl) {
        this.user_ctl = user_ctl;
    }

    /**
     * @return the user_val
     */
    public User getUser_val() {
        return user_val;
    }

    /**
     * @param user_val the user_val to set
     */
    public void setUser_val(User user_val) {
        this.user_val = user_val;
    }

    /**
     * @return the date_field
     */
    public IFace_dateFieldHolder getDate_field() {
        return date_field;
    }

    /**
     * @param date_field the date_field to set
     */
    public void setDate_field(IFace_dateFieldHolder date_field) {
        this.date_field = date_field;
    }

    /**
     * @return the user_field
     */
    public IFace_userFieldHolder getUser_field() {
        return user_field;
    }

    /**
     * @param user_field the user_field to set
     */
    public void setUser_field(IFace_userFieldHolder user_field) {
        this.user_field = user_field;
    }
    
    


    /**
     * @return the sql
     */
    public StringBuilder getSql() {
        return sql;
    }

    /**
     * @param sql the sql to set
     */
    public void setSql(StringBuilder sql) {
        this.sql = sql;
    }

    /**
     * @return the user_rtMin
     */
    public RoleType getUser_rtMin() {
        return user_rtMin;
    }

    /**
     * @param user_rtMin the user_rtMin to set
     */
    public void setUser_rtMin(RoleType user_rtMin) {
        this.user_rtMin = user_rtMin;
    }

    /**
     * @return the active_rtMin
     */
    public RoleType getActive_rtMin() {
        return active_rtMin;
    }

    /**
     * @param active_rtMin the active_rtMin to set
     */
    public void setActive_rtMin(RoleType active_rtMin) {
        this.active_rtMin = active_rtMin;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.filterName);
        hash = 71 * hash + Objects.hashCode(this.filterDescription);
        hash = 71 * hash + Objects.hashCode(this.sql);
        hash = 71 * hash + Objects.hashCode(this.muni_rtMin);
        hash = 71 * hash + (this.muni_ctl ? 1 : 0);
        hash = 71 * hash + Objects.hashCode(this.muni_val);
        hash = 71 * hash + Objects.hashCode(this.date_rtMin);
        hash = 71 * hash + (this.date_startEnd_ctl ? 1 : 0);
        hash = 71 * hash + Objects.hashCode(this.date_field);
        hash = 71 * hash + Objects.hashCode(this.date_start_val);
        hash = 71 * hash + Objects.hashCode(this.date_end_val);
        hash = 71 * hash + (this.date_relativeDates_ctl ? 1 : 0);
        hash = 71 * hash + this.date_relativeDates_start_val;
        hash = 71 * hash + this.date_realtiveDates_end_val;
        hash = 71 * hash + Objects.hashCode(this.user_rtMin);
        hash = 71 * hash + (this.user_ctl ? 1 : 0);
        hash = 71 * hash + Objects.hashCode(this.user_field);
        hash = 71 * hash + Objects.hashCode(this.user_val);
        hash = 71 * hash + Objects.hashCode(this.bobID_rtMin);
        hash = 71 * hash + (this.bobID_ctl ? 1 : 0);
        hash = 71 * hash + this.bobID_val;
        hash = 71 * hash + Objects.hashCode(this.limitResultCount_rtMin);
        hash = 71 * hash + (this.limitResultCount_ctl ? 1 : 0);
        hash = 71 * hash + this.limitResultCount_val;
        hash = 71 * hash + Objects.hashCode(this.active_rtMin);
        hash = 71 * hash + (this.active_ctl ? 1 : 0);
        hash = 71 * hash + (this.active_val ? 1 : 0);
        hash = 71 * hash + Objects.hashCode(this.log);
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
        final SearchParams other = (SearchParams) obj;
        if (this.muni_ctl != other.muni_ctl) {
            return false;
        }
        if (this.date_startEnd_ctl != other.date_startEnd_ctl) {
            return false;
        }
        if (this.date_relativeDates_ctl != other.date_relativeDates_ctl) {
            return false;
        }
        if (this.date_relativeDates_start_val != other.date_relativeDates_start_val) {
            return false;
        }
        if (this.date_realtiveDates_end_val != other.date_realtiveDates_end_val) {
            return false;
        }
        if (this.user_ctl != other.user_ctl) {
            return false;
        }
        if (this.bobID_ctl != other.bobID_ctl) {
            return false;
        }
        if (this.bobID_val != other.bobID_val) {
            return false;
        }
        if (this.limitResultCount_ctl != other.limitResultCount_ctl) {
            return false;
        }
        if (this.limitResultCount_val != other.limitResultCount_val) {
            return false;
        }
        if (this.active_ctl != other.active_ctl) {
            return false;
        }
        if (this.active_val != other.active_val) {
            return false;
        }
        if (!Objects.equals(this.filterName, other.filterName)) {
            return false;
        }
        if (!Objects.equals(this.filterDescription, other.filterDescription)) {
            return false;
        }
        if (!Objects.equals(this.sql, other.sql)) {
            return false;
        }
        if (this.muni_rtMin != other.muni_rtMin) {
            return false;
        }
        if (!Objects.equals(this.muni_val, other.muni_val)) {
            return false;
        }
        if (this.date_rtMin != other.date_rtMin) {
            return false;
        }
        if (!Objects.equals(this.date_field, other.date_field)) {
            return false;
        }
        if (!Objects.equals(this.date_start_val, other.date_start_val)) {
            return false;
        }
        if (!Objects.equals(this.date_end_val, other.date_end_val)) {
            return false;
        }
        if (this.user_rtMin != other.user_rtMin) {
            return false;
        }
        if (!Objects.equals(this.user_field, other.user_field)) {
            return false;
        }
        if (!Objects.equals(this.user_val, other.user_val)) {
            return false;
        }
        if (this.bobID_rtMin != other.bobID_rtMin) {
            return false;
        }
        if (this.limitResultCount_rtMin != other.limitResultCount_rtMin) {
            return false;
        }
        if (this.active_rtMin != other.active_rtMin) {
            return false;
        }
        if (!Objects.equals(this.log, other.log)) {
            return false;
        }
        return true;
    }
    
    

   
    
}
