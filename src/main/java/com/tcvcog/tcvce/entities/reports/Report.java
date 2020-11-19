/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.reports;

import com.tcvcog.tcvce.entities.EntityUtils;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.Query;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 *
 * @author sylvia
 */
public abstract class Report
         
        implements Serializable {
    
    private String title;
    private LocalDateTime generationTimestamp;
    private String generationTimestampPretty;
    private User creator;
    private Municipality muni;
    private LocalDateTime date_start_val;
    private LocalDateTime date_end_val;
    private String notes;
    private boolean sortInRevChrono;
    
    
    /**
     * @return the startDate_val_utilDate
     */
    public java.util.Date getStartDate_val_utilDate() {
        if(date_start_val != null){
            return java.util.Date.from(getDate_start_val().atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    /**
     * @param startDate_val_utilDate the startDate_val_utilDate to set
     */
    public void setStartDate_val_utilDate(java.util.Date startDate_val_utilDate) {
        if(startDate_val_utilDate != null){
            date_start_val = startDate_val_utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

    /**
     * @return the endDate_val_utilDate
     */
    public java.util.Date getEndDate_val_utilDate() {
        if(date_end_val != null){
            return java.util.Date.from(getDate_end_val().atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    /**
     * @param endDate_val_utilDate the endDate_val_utilDate to set
     */
    public void setEndDate_val_utilDate(java.util.Date endDate_val_utilDate) {
        if(endDate_val_utilDate != null){
            date_end_val = endDate_val_utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }
     /**
     * @return the startDate_val_SQLDate
     */
    public java.sql.Timestamp getDateStart_val_sql() {
            if(date_start_val != null){
                return java.sql.Timestamp.valueOf(getDate_start_val());
            }
        return java.sql.Timestamp.valueOf(LocalDateTime.of(2100, 1, 1, 0, 0));
    }

    /**
     * @return the endDate_val_SQLDate
     */
    public java.sql.Timestamp getDateEnd_val_sql() {
            if(date_end_val != null){
                return java.sql.Timestamp.valueOf(getDate_end_val());
            }
        // the EPOCH!
        return java.sql.Timestamp.valueOf(LocalDateTime.of(1970, 1, 1, 0, 0));
    }


    /**
     *
     * @return
     */
   
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the generationTimestamp
     */
    public LocalDateTime getGenerationTimestamp() {
        return generationTimestamp;
    }

    /**
     * @return the creator
     */
    public User getCreator() {
        return creator;
    }

    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

   

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param generationTimestamp the generationTimestamp to set
     */
    public void setGenerationTimestamp(LocalDateTime generationTimestamp) {
        this.generationTimestamp = generationTimestamp;
    }

    /**
     * @param creator the creator to set
     */
    public void setCreator(User creator) {
        this.creator = creator;
    }

    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the generationTimestampPretty
     */
    public String getGenerationTimestampPretty() {
        if(generationTimestamp != null){
            generationTimestampPretty = EntityUtils.getPrettyDate(generationTimestamp);
        }
        return generationTimestampPretty;
    }

    /**
     * @param generationTimestampPretty the generationTimestampPretty to set
     */
    public void setGenerationTimestampPretty(String generationTimestampPretty) {
        this.generationTimestampPretty = generationTimestampPretty;
    }

    /**
     * @return the sortInRevChrono
     */
    public boolean isSortInRevChrono() {
        return sortInRevChrono;
    }

    /**
     * @param sortInRevChrono the sortInRevChrono to set
     */
    public void setSortInRevChrono(boolean sortInRevChrono) {
        this.sortInRevChrono = sortInRevChrono;
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

   

    
}
