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

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.LogEntry;
import com.tcvcog.tcvce.entities.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 *
 * @author Eric Darsow
 */
public class LogIntegrator extends BackingBeanUtils {

    /**
     * Creates a new instance of LogIntegrator
     */
    public LogIntegrator() {
    }
    
    public void makeLogEntry(int uid, String sid, int cat, String notes, 
            boolean error, boolean reqview) throws IntegrationException{
        Connection con = getPostgresCon();
        String query = "INSERT INTO public.genlog(\n" +
            "            logentryid, timeofentry, user_userid, sessionid, category, notes, \n" +
            "            error, reqview, viewed)\n" +
            "    VALUES (DEFAULT, now(), ?, ?, ?, ?, \n" +
            "            ?, ?, FALSE);";

        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, uid);
            stmt.setString(2, sid);
            stmt.setInt(3, cat);
            stmt.setString(4, notes);
            stmt.setBoolean(5, error);
            stmt.setBoolean(6, reqview);
            stmt.execute();
            
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error creating log ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
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
