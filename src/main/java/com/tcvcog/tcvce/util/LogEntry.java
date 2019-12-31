/*
 * Copyright (C) 2017 Turtle Creek Valley
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
package com.tcvcog.tcvce.util;

import com.tcvcog.tcvce.domain.ExceptionSeverityEnum;
import com.tcvcog.tcvce.entities.User;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class LogEntry implements Serializable{
    
    private int logEntryID;
    
    private int entryID;
    private LocalDateTime timeOfEntry;
    
    private User user;
    private String notes;
    private boolean err;
    
    private LogEntryCategory entryCategory;
    
    private String credSignature;
    private SubSysEnum subSys;
    private ExceptionSeverityEnum severity;
    

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
     * @return the timeOfEntry
     */
    public LocalDateTime getTimeOfEntry() {
        return timeOfEntry;
    }

    /**
     * @param timeOfEntry the timeOfEntry to set
     */
    public void setTimeOfEntry(LocalDateTime timeOfEntry) {
        this.timeOfEntry = timeOfEntry;
    }

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

  

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the credSignature
     */
    public String getCredSignature() {
        return credSignature;
    }

    /**
     * @param credSignature the credSignature to set
     */
    public void setCredSignature(String credSignature) {
        this.credSignature = credSignature;
    }

    /**
     * @return the entryCategory
     */
    public LogEntryCategory getEntryCategory() {
        return entryCategory;
    }

    /**
     * @param entryCategory the entryCategory to set
     */
    public void setEntryCategory(LogEntryCategory entryCategory) {
        this.entryCategory = entryCategory;
    }

    /**
     * @return the severity
     */
    public ExceptionSeverityEnum getSeverity() {
        return severity;
    }

    /**
     * @return the subSys
     */
    public SubSysEnum getSubSys() {
        return subSys;
    }

    /**
     * @param severity the severity to set
     */
    public void setSeverity(ExceptionSeverityEnum severity) {
        this.severity = severity;
    }

    /**
     * @param subSys the subSys to set
     */
    public void setSubSys(SubSysEnum subSys) {
        this.subSys = subSys;
    }

    /**
     * @return the err
     */
    public boolean isErr() {
        return err;
    }

    /**
     * @param err the err to set
     */
    public void setErr(boolean err) {
        this.err = err;
    }

    /**
     * @return the entryID
     */
    public int getEntryID() {
        return entryID;
    }

    /**
     * @param entryID the entryID to set
     */
    public void setEntryID(int entryID) {
        this.entryID = entryID;
    }
    
}
