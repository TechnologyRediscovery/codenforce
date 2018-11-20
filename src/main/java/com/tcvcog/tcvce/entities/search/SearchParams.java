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
    
    
    private Municipality muni;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private java.util.Date startDateUtilDate;
    private java.util.Date endDateUtilDate;
    private java.sql.Timestamp startDateSQLDate;
    private java.sql.Timestamp endDateSQLDate;
    
    
    
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
        
        startDateUtilDate = java.util.Date.from(getStartDate().atZone(ZoneId.systemDefault()).toInstant());
        return startDateUtilDate;
    }

    /**
     * @param startDateUtilDate the startDateUtilDate to set
     */
    public void setStartDateUtilDate(java.util.Date startDateUtilDate) {
        this.startDateUtilDate = startDateUtilDate;
        startDate = startDateUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * @return the endDateUtilDate
     */
    public java.util.Date getEndDateUtilDate() {
        endDateUtilDate = java.util.Date.from(getEndDate().atZone(ZoneId.systemDefault()).toInstant());
        return endDateUtilDate;
    }

    /**
     * @param endDateUtilDate the endDateUtilDate to set
     */
    public void setEndDateUtilDate(java.util.Date endDateUtilDate) {
        this.endDateUtilDate = endDateUtilDate;
        endDate = endDateUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
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
        startDateSQLDate = java.sql.Timestamp.valueOf(getStartDate());
        return startDateSQLDate;
    }

    /**
     * @return the endDateSQLDate
     */
    public java.sql.Timestamp getEndDateSQLDate() {
        endDateSQLDate = java.sql.Timestamp.valueOf(getEndDate());
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

    

   
    
}
