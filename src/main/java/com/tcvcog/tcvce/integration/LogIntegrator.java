 /*
 * Copyright (C) 2017 Turtle Creek Valley
 * Council of Governments, PA
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
package com.tcvcog.tcvce.integration;

import com.tcvcog.tcvce.entities.LogEntry;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 *
 * @author Eric Darsow
 */
public class LogIntegrator {

    /**
     * Creates a new instance of LogIntegrator
     */
    public LogIntegrator() {
    }
    
    public int insertLogEvent(LogEntry entry){
        return 0;
        
    }
    
    public ArrayList getLogEventsInWindow(LocalDateTime start, LocalDateTime end){
        ArrayList logList = null;
        
        return logList;
    }
    
    public LogEntry getLogEntryByLogID(int logID){
        LogEntry logEntry = null;
        return logEntry;
    }
    
    
    
}
