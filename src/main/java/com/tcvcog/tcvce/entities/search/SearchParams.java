/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.EntityUtils;
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
public class SearchParams extends EntityUtils implements Serializable{
    
     
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
    private String startDatePretty;
    private String endDatePretty;
    
    private boolean useRelativeDates;
    private int startDateRelativeDays;
    private int endDateRelativeDays;
    
    private boolean applyDateSearchToDateOfRecord;
    private boolean useEntryTimestamp;
    
    private boolean filterByObjectID;
    private int objectID;
    
    private boolean limitResultCountTo100;
    
    private boolean active_filterBy;
    private boolean active;
    
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
        return startDate;
    }

    /**
     * @return the endDate
     */
    public LocalDateTime getEndDate() {
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
        if(useRelativeDates){
            startDateSQLDate = java.sql.Timestamp.valueOf(LocalDateTime.now().plusDays(startDateRelativeDays));
            System.out.println("SearchParams.getStartDateSQLDate | usereldates:true | starddate str: " + startDateSQLDate.toString());
        } else {
            if(startDate != null){
                startDateSQLDate = java.sql.Timestamp.valueOf(getStartDate());
            }
        }
        return startDateSQLDate;
    }

    /**
     * @return the endDateSQLDate
     */
    public java.sql.Timestamp getEndDateSQLDate() {
        if(useRelativeDates){
            endDateSQLDate = java.sql.Timestamp.valueOf(LocalDateTime.now().plusDays(endDateRelativeDays));
            System.out.println("SearchParams.getEndDateSQLDate | usereldates:true | starddate str: " + endDateSQLDate.toString());
        } else {
            if(endDate != null){
                endDateSQLDate = java.sql.Timestamp.valueOf(getEndDate());
            }
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
     * @return the applyDateSearchToDateOfRecord
     */
    public boolean isApplyDateSearchToDateOfRecord() {
        return applyDateSearchToDateOfRecord;
    }

    /**
     * @param applyDateSearchToDateOfRecord the applyDateSearchToDateOfRecord to set
     */
    public void setApplyDateSearchToDateOfRecord(boolean applyDateSearchToDateOfRecord) {
        this.applyDateSearchToDateOfRecord = applyDateSearchToDateOfRecord;
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
        if(startDateSQLDate != null){
            LocalDateTime startDateLDT = startDateSQLDate.toLocalDateTime();
            startDatePretty = getPrettyDate(startDateLDT);
        }
        return startDatePretty;
    }

    /**
     * @return the endDatePretty
     */
    public String getEndDatePretty() {
        if(endDateSQLDate != null){
            LocalDateTime endDateLDT = endDateSQLDate.toLocalDateTime();
            endDatePretty = getPrettyDate(endDateLDT);
        }
        return endDatePretty;
    }

    /**
     * 
     * @param startDatePretty the startDatePretty to set
     */
    public void setStartDatePretty(String startDatePretty) {
        
        this.startDatePretty = startDatePretty;
    }

    /**
     * @param endDatePretty the endDatePretty to set
     */
    public void setEndDatePretty(String endDatePretty) {
        this.endDatePretty = endDatePretty;
    }

    /**
     * @return the active_filterBy
     */
    public boolean isActive_filterBy() {
        return active_filterBy;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active_filterBy the active_filterBy to set
     */
    public void setActive_filterBy(boolean active_filterBy) {
        this.active_filterBy = active_filterBy;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

   

   
    
}
