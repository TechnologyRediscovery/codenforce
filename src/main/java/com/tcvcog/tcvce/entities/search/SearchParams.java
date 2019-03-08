/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.Municipality;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Encapsulates municipality restrictions and start/end
 * dates for searching. Types for all three commonly used date
 * formats system wide are included in the params object
 * and the conversions are made automatically based on the
 * LocalDateTime value, allowing objects on the front end and 
 * integration end to just grab the date Type they need and go!
 * @author Sylvia Garland
 */
public class SearchParams implements Serializable{
    
    
    private String searchName;
    private String searchDescription;
    private boolean filterByMuni;
    private Municipality muni;
    
    private boolean filterByStartEndDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    private java.util.Date startDateUtilDate;
    private java.util.Date endDateUtilDate;
    private java.sql.Timestamp startDateSQLDate;
    private java.sql.Timestamp endDateSQLDate;
    
    private boolean useRelativeDates;
    private int startDateRelativeDays;
    private int endDateRelativeDays;
    
    private boolean useDateOfRecord;
    private boolean useEntryTimestamp;
    
    
    private boolean filterByObjectID;
    private int objectID;
    
    private boolean limitResultCountTo100;
    
    
   public SearchParams(){
       
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
     * @return the startDateUtilDate
     */
    public java.util.Date getStartDateUtilDate() {
        if(startDate != null){
            startDateUtilDate = java.util.Date.from(getStartDate().atZone(ZoneId.systemDefault()).toInstant());
        }
        return startDateUtilDate;
    }

    /**
     * @param startDateUtilDate the startDateUtilDate to set
     */
    public void setStartDateUtilDate(java.util.Date startDateUtilDate) {
        this.startDateUtilDate = startDateUtilDate;
        if(startDateUtilDate != null){
            startDate = startDateUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

    /**
     * @return the endDateUtilDate
     */
    public java.util.Date getEndDateUtilDate() {
        if(endDate != null){
            endDateUtilDate = java.util.Date.from(getEndDate().atZone(ZoneId.systemDefault()).toInstant());
        }
        return endDateUtilDate;
    }

    /**
     * @param endDateUtilDate the endDateUtilDate to set
     */
    public void setEndDateUtilDate(java.util.Date endDateUtilDate) {
        this.endDateUtilDate = endDateUtilDate;
        if(endDateUtilDate != null){
            endDate = endDateUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

    /**
     * @return the startDate
     */
    public LocalDateTime getStartDate() {
        if(useRelativeDates){
            startDate = LocalDateTime.now().plusDays(startDateRelativeDays);
        }
        
        return startDate;
    }

    /**
     * @return the endDate
     */
    public LocalDateTime getEndDate() {
        if(useRelativeDates){
            endDate = LocalDateTime.now().plusDays(endDateRelativeDays);
        }
        return endDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    /**
     * @return the startDateSQLDate
     */
    public java.sql.Timestamp getStartDateSQLDate() {
        if(startDate != null){
            startDateSQLDate = java.sql.Timestamp.valueOf(getStartDate());
        }
        return startDateSQLDate;
    }

    /**
     * @return the endDateSQLDate
     */
    public java.sql.Timestamp getEndDateSQLDate() {
        if(endDate != null){
            endDateSQLDate = java.sql.Timestamp.valueOf(getEndDate());
        }
        return endDateSQLDate;
    }

    /**
     * @param startDateSQLDate the startDateSQLDate to set
     */
    public void setStartDateSQLDate(java.sql.Timestamp startDateSQLDate) {
        this.startDateSQLDate = startDateSQLDate;
    }

    /**
     * @param endDateSQLDate the endDateSQLDate to set
     */
    public void setEndDateSQLDate(java.sql.Timestamp endDateSQLDate) {
        this.endDateSQLDate = endDateSQLDate;
    }

    /**
     * @return the filterByStartEndDate
     */
    public boolean isFilterByStartEndDate() {
        return filterByStartEndDate;
    }

    /**
     * @param filterByStartEndDate the filterByStartEndDate to set
     */
    public void setFilterByStartEndDate(boolean filterByStartEndDate) {
        this.filterByStartEndDate = filterByStartEndDate;
    }

    /**
     * @return the limitResultCountTo100
     */
    public boolean isLimitResultCountTo100() {
        return limitResultCountTo100;
    }

    /**
     * @param limitResultCountTo100 the limitResultCountTo100 to set
     */
    public void setLimitResultCountTo100(boolean limitResultCountTo100) {
        this.limitResultCountTo100 = limitResultCountTo100;
    }

    /**
     * @return the objectID
     */
    public int getObjectID() {
        return objectID;
    }

    /**
     * @param objectID the objectID to set
     */
    public void setObjectID(int objectID) {
        this.objectID = objectID;
    }

    /**
     * @return the filterByObjectID
     */
    public boolean isFilterByObjectID() {
        return filterByObjectID;
    }

    /**
     * @param filterByObjectID the filterByObjectID to set
     */
    public void setFilterByObjectID(boolean filterByObjectID) {
        this.filterByObjectID = filterByObjectID;
    }

    /**
     * @return the searchName
     */
    public String getSearchName() {
        return searchName;
    }

    /**
     * @param searchName the searchName to set
     */
    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    /**
     * @return the searchDescription
     */
    public String getSearchDescription() {
        return searchDescription;
    }

    /**
     * @param searchDescription the searchDescription to set
     */
    public void setSearchDescription(String searchDescription) {
        this.searchDescription = searchDescription;
    }

    /**
     * @return the filterByMuni
     */
    public boolean isFilterByMuni() {
        return filterByMuni;
    }

    /**
     * @param filterByMuni the filterByMuni to set
     */
    public void setFilterByMuni(boolean filterByMuni) {
        this.filterByMuni = filterByMuni;
    }

    /**
     * @return the useRelativeDates
     */
    public boolean isUseRelativeDates() {
        return useRelativeDates;
    }

    /**
     * @return the startDateRelativeDays
     */
    public int getStartDateRelativeDays() {
        return startDateRelativeDays;
    }

    /**
     * @return the endDateRelativeDays
     */
    public int getEndDateRelativeDays() {
        return endDateRelativeDays;
    }

    /**
     * @param useRelativeDates the useRelativeDates to set
     */
    public void setUseRelativeDates(boolean useRelativeDates) {
        this.useRelativeDates = useRelativeDates;
    }

    /**
     * @param startDateRelativeDays the startDateRelativeDays to set
     */
    public void setStartDateRelativeDays(int startDateRelativeDays) {
        this.startDateRelativeDays = startDateRelativeDays;
    }

    /**
     * @param endDateRelativeDays the endDateRelativeDays to set
     */
    public void setEndDateRelativeDays(int endDateRelativeDays) {
        this.endDateRelativeDays = endDateRelativeDays;
    }

    /**
     * @return the useEntryTimestamp
     */
    public boolean isUseEntryTimestamp() {
        return useEntryTimestamp;
    }

    /**
     * @param useEntryTimestamp the useEntryTimestamp to set
     */
    public void setUseEntryTimestamp(boolean useEntryTimestamp) {
        this.useEntryTimestamp = useEntryTimestamp;
    }

    /**
     * @return the useDateOfRecord
     */
    public boolean isUseDateOfRecord() {
        return useDateOfRecord;
    }

    /**
     * @param useDateOfRecord the useDateOfRecord to set
     */
    public void setUseDateOfRecord(boolean useDateOfRecord) {
        this.useDateOfRecord = useDateOfRecord;
    }

    

   
    
}
