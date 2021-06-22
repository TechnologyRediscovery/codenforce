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
        extends TrackedEntity {
    
    private int statusID;
    private String statusTitle;
    private String description;
    private Icon icon;

    private LocalDateTime dateOfRecord;
    
    private String notes;
    
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
     * @return the statusID
     */
    public int getStatusID() {
        return statusID;
    }

    /**
     * @return the statusTitle
     */
    public String getStatusTitle() {
        return statusTitle;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the icon
     */
    public Icon getIcon() {
        return icon;
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
    public String getNotes() {
        return notes;
    }

    /**
     * @param statusID the statusID to set
     */
    public void setStatusID(int statusID) {
        this.statusID = statusID;
    }

    /**
     * @param statusTitle the statusTitle to set
     */
    public void setStatusTitle(String statusTitle) {
        this.statusTitle = statusTitle;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
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
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
}
