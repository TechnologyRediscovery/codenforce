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
import com.tcvcog.tcvce.util.LogEntry;
import com.tcvcog.tcvce.entities.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    
    
    public int writeLogEntry(LogEntry le){
        Connection con = getPostgresCon();
        int freshLogEntry = 0;
        
        String query = "INSERT INTO public.log(\n" +
                        "            logentryid, timeofentry, user_userid, notes, error, category, \n" +
                        "            credsig, subsys, severity)\n" +
                        "    VALUES (DEFAULT, now(), ?, ?, ?, ?, \n" +
                        "            ?, ?, ?);";

        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            
            if(le.getUser() != null){
                stmt.setInt(1, le.getUser().getUserID());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            if(le.getNotes() != null){
                stmt.setString(2, le.getNotes());
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            stmt.setBoolean(3, le.getEntryCategory().isError());
            stmt.setString(4, le.getEntryCategory().getTitle());
            
            // second line of insert vals
            if(le.getCredSignature() != null){
                stmt.setString(5, le.getCredSignature());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            if(le.getSubSys() != null){
                stmt.setString(6, le.getSubSys().getSubSysID_Roman());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            if(le.getSeverity()!= null){
                stmt.setString(7, le.getSeverity().toString());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
        
              // grab the newly inserted propertyid
            String idNumQuery = "SELECT currval('coglog_logeentryid_seq');";
            Statement s = con.createStatement();
            ResultSet rs;
            rs = s.executeQuery(idNumQuery);
            rs.next();
            freshLogEntry = rs.getInt(1);
            
        } catch (SQLException ex) {
            System.out.println(ex);
             
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        return freshLogEntry;
    }
    
    
    /**
     * First gen log attempt
     * 
     * @deprecated 
     * @param uid
     * @param sid
     * @param cat
     * @param notes
     * @param error
     * @param reqview
     * @throws IntegrationException 
     */
    public void makeLogEntry(int uid, String sid, int cat, String notes, 
            boolean error, boolean reqview) throws IntegrationException{
        Connection con = getPostgresCon();
        String query = "INSERT INTO public.log(\n" +
            "            logentryid, timeofentry, user_userid, category, notes, \n" +
            "            error, reqview, viewed)\n" +
            "    VALUES (DEFAULT, now(), ?, ?, ?, \n" +
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
