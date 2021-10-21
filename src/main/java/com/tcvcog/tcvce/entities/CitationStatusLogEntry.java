/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
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
 * Represents a state of a Citation beginning on a given date
 * A list of these represents the Citation's journey through the
 * court system
 * 
 * @author ellen bascomb of apt 31y
 */
public class    CitationStatusLogEntry 
        extends TrackedEntity
        implements IFace_noteHolder{
    
    final static String CITATION_STATUS_TABLE = "citationcitationstatus";
    final static String CITATION_STATUS_PKFIELD = "citationstatusid";
    
    private int logEntryID;
    private int citationID;
    private CitationStatus status;

    private LocalDateTime dateOfRecord;
    
    private String notes;
    
    private CourtEntity courtEntity;
    
    private boolean nonStatusEditsForbidden;
    private EventRuleAbstract phaseChangeRule;

    /**
     * @return the nonStatusEditsForbidden
     */
    public boolean isNonStatusEditsForbidden() {
        return nonStatusEditsForbidden;
    }

    /**
     * @param nonStatusEditsForbidden the nonStatusEditsForbidden to set
     */
    public void setNonStatusEditsForbidden(boolean nonStatusEditsForbidden) {
        this.nonStatusEditsForbidden = nonStatusEditsForbidden;
    }

    /**
     * @return the phaseChangeRule
     */
    public EventRuleAbstract getPhaseChangeRule() {
        return phaseChangeRule;
    }

    /**
     * @param phaseChangeRule the phaseChangeRule to set
     */
    public void setPhaseChangeRule(EventRuleAbstract phaseChangeRule) {
        this.phaseChangeRule = phaseChangeRule;
    }


    /**
     * @return the dateOfRecord
     */
    public LocalDateTime getDateOfRecord() {
        return dateOfRecord;
    }

    /**
     * @return the notes
     */
    @Override
    public String getNotes() {
        return notes;
    }

   
    /**
     * @param dateOfRecord the dateOfRecord to set
     */
    public void setDateOfRecord(LocalDateTime dateOfRecord) {
        this.dateOfRecord = dateOfRecord;
    }

    /**
     * @param notes the notes to set
     */
    @Override
    public void setNotes(String notes) {
        this.notes = notes;
    }

   

    @Override
    public String getPKFieldName() {
        return CITATION_STATUS_PKFIELD;
    }

    @Override
    public int getDBKey() {
        int id = 0;
        if(status != null){
            return status.getStatusID();
        }
        return id;
    }

    @Override
    public String getDBTableName() {
        return CITATION_STATUS_TABLE;
    }

    /**
     * @return the status
     */
    public CitationStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(CitationStatus status) {
        this.status = status;
    }

    /**
     * @return the citationID
     */
    public int getCitationID() {
        return citationID;
    }

    /**
     * @param citationID the citationID to set
     */
    public void setCitationID(int citationID) {
        this.citationID = citationID;
    }

    /**
     * @return the logEntryID
     */
    public int getLogEntryID() {
        return logEntryID;
    }

    /**
     * @param logEntryID the logEntryID to set
     */
    public void setLogEntryID(int logEntryID) {
        this.logEntryID = logEntryID;
    }

    /**
     * @return the courtEntity
     */
    public CourtEntity getCourtEntity() {
        return courtEntity;
    }

    /**
     * @param courtEntity the courtEntity to set
     */
    public void setCourtEntity(CourtEntity courtEntity) {
        this.courtEntity = courtEntity;
    }
    
}
