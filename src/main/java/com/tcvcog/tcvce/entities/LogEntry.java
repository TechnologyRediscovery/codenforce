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
package com.tcvcog.tcvce.entities;

import java.time.LocalDateTime;

/**
 *
 * @author Eric C. Darsow
 */
public class LogEntry {
    
    private int logEntryID;
    private LocalDateTime timeOfEntry;
    private User user;
    private int sessionID;
    private String entryCategory;
    private String notes;

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
     * @return the sessionID
     */
    public int getSessionID() {
        return sessionID;
    }

    /**
     * @param sessionID the sessionID to set
     */
    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }

    /**
     * @return the entryCategory
     */
    public String getEntryCategory() {
        return entryCategory;
    }

    /**
     * @param entryCategory the entryCategory to set
     */
    public void setEntryCategory(String entryCategory) {
        this.entryCategory = entryCategory;
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
    
}
