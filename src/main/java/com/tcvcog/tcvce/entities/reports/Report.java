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
import java.util.List;

/**
 *
 * @author sylvia
 */
public abstract class Report
        extends EntityUtils 
        implements Serializable {
    
    private String title;
    private LocalDateTime generationTimestamp;
    private String generationTimestampPretty;
    private User creator;
    private Municipality muni;
    private String notes;
    private boolean sortInRevChrono;
    
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
            generationTimestampPretty = getPrettyDate(generationTimestamp);
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

   

    
}
