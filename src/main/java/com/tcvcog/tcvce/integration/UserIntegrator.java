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
package com.tcvcog.tcvce.integration;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.MunicipalityCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriodLogEntry;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriod;
import com.tcvcog.tcvce.entities.UserAuthorized;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class UserIntegrator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of UserIntegrator
     */
    public UserIntegrator() {
        
    }
    
   
    /**
     *
     * @param userID
     * @return
     * @throws IntegrationException 
     */
    public User getUser(int userID) throws IntegrationException{
        
        if(userID == 0){
            return null;
        }
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        User newUser = null;
        // broken query
        String query =  "   SELECT userid, username, notes, personlink, \n" +
                        "       createdby, createdts, nologinvirtualonly, \n" +
                        "       deactivatedts, deactivated_userid, lastupdatedts, homemuni \n" +
                        "  FROM public.login WHERE userid = ?;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, userID);
            rs = stmt.executeQuery();
            while(rs.next()){
                newUser = generateUser(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error getting user", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return newUser;
    }
    
    
      /**
     * Extracts complete table dump of the login table
     * 
     * @return IDs of all users in the login table
     * @throws IntegrationException 
     */
    public List<Integer> getUserListComplete() throws IntegrationException{
        
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        // broken query
        String query =  "SELECT userid FROM public.login;";
        
        List<Integer> idl = new ArrayList<>();
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while(rs.next()){
                idl.add(rs.getInt("userid"));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error getting user", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        System.out.println("UserIntegrator.getUserListComplete: Returning list size "+ idl.toString());
        return idl;
    }
    
    
    
     /**
     * Note that the client method is responsible for moving the cursor on the 
     * result set object before passing it into this method     * 
     * @param rs
     * @return
     * @throws IntegrationException 
     */
    private User generateUser(ResultSet rs) throws IntegrationException, SQLException{
        User user = new User();
        PersonIntegrator pi = getPersonIntegrator();
        MunicipalityCoordinator mc = getMuniCoordinator();
        
        
            // line 1 of SELECT
            user.setUserID(rs.getInt("userid"));
            user.setUsername(rs.getString("username"));
            user.setNotes(rs.getString("notes"));
            user.setPersonID(rs.getInt("personlink"));
            if(rs.getInt("personlink") != 0){
                user.setPerson(pi.getPerson(rs.getInt("personlink")));
            }
            // line 2 of SELECT
            user.setCreatedByUserId(rs.getInt("createdby"));
            if(rs.getTimestamp("createdts") != null){
                user.setCreatedTS(rs.getTimestamp("createdts").toLocalDateTime());
            }
            user.setNoLoginVirtualUser(rs.getBoolean("nologinvirtualonly"));
            
            // line 3 of SELECT
            if(rs.getTimestamp("deactivatedts") != null){
                user.setDeactivatedTS(rs.getTimestamp("deactivatedts").toLocalDateTime());   
            }
            user.setDeactivatedBy(rs.getInt("deactivated_userid"));
            if(rs.getTimestamp("lastupdatedts") != null){
                user.setLastUpdatedTS(rs.getTimestamp("lastupdatedts").toLocalDateTime());
            }
            
            if(rs.getInt("homemuni") != 0){
                user.setHomeMuniID(rs.getInt("homemuni"));
            }
            
            
        
        return user;
    }
    
  
 
    
    
    /**
     *
     * Creates sekeletonized UserAuthorized
     * 
     * @param usr
     * @return null returned with null input
     * @throws IntegrationException 
     */
    public UserAuthorized getUserAuthorizedNoAuthPeriods(User usr) throws IntegrationException{
        if(usr == null){
            System.out.println("UserIntegrator.getUANoUMAPs: null User passed in");
            return null;
        }
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        // broken query
        String query =  " SELECT userid, pswdlastupdated, forcepasswordreset, createdby, createdts " +
                        " FROM public.login WHERE userid = ?;";
        
        UserAuthorized ua = null;
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, usr.getUserID());
            rs = stmt.executeQuery();
            while(rs.next()){
                ua = new UserAuthorized(getUser(usr.getUserID()));
                ua = generateUserAuthorized(ua, rs);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error generating UserAuthorized", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return ua;
    }
    
    /**
     * Generator method for UserAuthorized objects
     * @param ua
     * @param rs
     * @return
     * @throws SQLException 
     */
    private UserAuthorized generateUserAuthorized(UserAuthorized ua, ResultSet rs) throws SQLException{
        
            if(rs.getTimestamp("pswdlastupdated") != null){
                ua.setPswdLastUpdated(rs.getTimestamp("pswdlastupdated").toLocalDateTime());
            }
            if(rs.getTimestamp("forcepasswordreset") != null){
                ua.setForcePasswordResetTS(rs.getTimestamp("forcepasswordreset").toLocalDateTime());
            }
            ua.setCreatedByUserId(rs.getInt("createdby"));
            if(rs.getTimestamp("createdts") != null){
                ua.setCreatedTS(rs.getTimestamp("createdts").toLocalDateTime());
            }
            
            
        
        return ua;
        
    }
    
   
    
    /**
     * Provides a complete list of records by User in the table loginmuniauthperiod.
     * Client is responsible for validating the UMAP object by the UserCoordinator
     * @param userID
     * @return
     * @throws IntegrationException 
     */
    public List<UserMuniAuthPeriod> getUserMuniAuthPeriodsRaw(int userID) throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        List<UserMuniAuthPeriod> perList = null;
        // broken query
        String query = "SELECT muniauthperiodid FROM public.loginmuniauthperiod WHERE authuser_userid=?;";
        
        PreparedStatement stmt = null;
        if(userID != 0){
            perList = new ArrayList<>();
            try {
                stmt = con.prepareStatement(query);
                stmt.setInt(1, userID);
                rs = stmt.executeQuery();
                while(rs.next()){
                    perList.add(getUserMuniAuthPeriod(rs.getInt("muniauthperiodid")));
                }

            } catch (SQLException ex) {
                System.out.println(ex);
                throw new IntegrationException("Error getting user access record", ex);
            } finally{
                 if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
                 if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
                 if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            } // close finally
        }
        
        return perList;
        
    }
    
    /**
     * For use by system administrators to manage user data. Raw means that even 
     * expired or invalidated periods are STILL included
     * @param m
     * @return
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.AuthorizationException 
     */
    public List<UserMuniAuthPeriod> getUserMuniAuthPeriodsRaw(Municipality m) throws IntegrationException, AuthorizationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        List<UserMuniAuthPeriod> umapList = new ArrayList<>();
        
       String query =  "SELECT muniauthperiodid " +
                        "FROM public.loginmuniauthperiod WHERE muni_municode=?;";
        
        PreparedStatement stmt = null;
        
        try {
                 
            stmt = con.prepareStatement(query);
            stmt.setInt(1, m.getMuniCode());
            rs = stmt.executeQuery();
            while(rs.next()){
                umapList.add(getUserMuniAuthPeriod(rs.getInt("muniauthperiodid")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error fetching user list", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return umapList;
        
    }
    
    public UserMuniAuthPeriod getUserMuniAuthPeriod(int periodID) throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        UserMuniAuthPeriod per = null;
        // broken query
        String query = "SELECT muniauthperiodid, muni_municode, authuser_userid, accessgranteddatestart, \n" +
                        "       accessgranteddatestop, recorddeactivatedts, authorizedrole, createdts, \n" +
                        "       createdby_userid, notes, supportassignedby, assignmentrank\n" +
                        "  FROM public.loginmuniauthperiod WHERE muniauthperiodid=?;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, periodID);
            rs = stmt.executeQuery();
            while(rs.next()){
                per = generateUserMuniAuthPeriod(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error getting user access record", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return per;
        
        
    }
    
    
    private UserMuniAuthPeriod generateUserMuniAuthPeriod(ResultSet rs) throws SQLException, IntegrationException{
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        UserMuniAuthPeriod per = new UserMuniAuthPeriod(mi.getMuni(rs.getInt("muni_municode")));
        
        per.setUserMuniAuthPeriodID(rs.getInt("muniauthperiodid"));
        
        per.setPeriodActivityLogBook(getMuniAuthPeriodLogEntryList(per));
        
        per.setUserID(rs.getInt("authuser_userid"));
        
        if(rs.getTimestamp("accessgranteddatestart") != null){
            per.setStartDate(rs.getTimestamp("accessgranteddatestart").toLocalDateTime());
        }
        if(rs.getTimestamp("accessgranteddatestop") != null){
            per.setStopDate(rs.getTimestamp("accessgranteddatestop").toLocalDateTime());
        }
        if(rs.getTimestamp("recorddeactivatedts") != null){
            per.setRecorddeactivatedTS(rs.getTimestamp("recorddeactivatedts").toLocalDateTime());
        }
        per.setRole(RoleType.valueOf(rs.getString("authorizedrole")));
        
        if(rs.getTimestamp("createdts") != null){
            per.setCreatedTS(rs.getTimestamp("createdts").toLocalDateTime());
        }
        per.setCreatedByUserID(rs.getInt("createdby_userid"));
        
        per.setNotes(rs.getString("notes"));
        // do support stuff later
        per.setAssignmentRelativeOrder(rs.getInt("assignmentrank"));
        
        return per;
    }
    
    
    public void insertUserMuniAuthPeriodLogEntry(UserMuniAuthPeriodLogEntry entry) throws IntegrationException, AuthorizationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        UserMuniAuthPeriod per = null;
        // broken query
        String query =  "INSERT INTO public.loginmuniauthperiodlog(\n" +
                        "            authperiodlogentryid, authperiod_periodid, category, entryts, \n" +
                        "            entrydateofrecord, disputedby_userid, disputedts, notes, cookie_jsessionid, \n" +
                        "            header_remoteaddr, header_useragent, header_dateraw, header_date, \n" +
                        "            header_cachectl, audit_usersession_userid, audit_usercredential_userid, \n" +
                        "            audit_muni_municode)\n" +
                        "    VALUES (DEFAULT, ?, ?, now(), \n" +
                        "            now(), ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, NULL, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?);";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            
            stmt.setInt(1, entry.getUserMuniAuthPeriodID());
            stmt.setString(2, entry.getCategory());
            // entry ts and dateofrecord set by PG's now()
            
            if(entry.getDisputedByUserID()== 0 && entry.getDisputedts() == null){
                stmt.setNull(3, java.sql.Types.NULL);
                stmt.setNull(4, java.sql.Types.NULL);
            } else {
                throw new AuthorizationException("Cannot insert a CredentialLogEntry with not null disputed fields!");
            }
            stmt.setString(5, entry.getNotes());
            stmt.setString(6, entry.getCookie_jsessionid());
            
            stmt.setString(7, entry.getHeader_remoteaddr());
            stmt.setString(8, entry.getHeader_useragent());
            stmt.setString(9,  entry.getHeader_dateraw());
            
            //header date java type set to NULL in SQL
            stmt.setString(10, entry.getHeader_cachectl());
            stmt.setInt(11, entry.getAudit_usersession_userid());
            stmt.setInt(12, entry.getAudit_usercredential_userid());
            
            stmt.setInt(13, entry.getAudit_muni_municode());
            
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error getting user access record", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public List<UserMuniAuthPeriodLogEntry> getMuniAuthPeriodLogEntryList(UserMuniAuthPeriod uap) throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        List<UserMuniAuthPeriodLogEntry> uacleList = new ArrayList<>();
        // broken query
        String query = "SELECT authperiodlogentryid" +
                        "  FROM public.loginmuniauthperiodlog WHERE authperiod_periodid=?;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, uap.getUserMuniAuthPeriodID());
            rs = stmt.executeQuery();
            while(rs.next()){
                uacleList.add(getUserMuniAuthPeriodLogEntry(rs.getInt("authperiodlogentryid")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error getting user access record", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

        return uacleList;
    }
    
    
    public UserMuniAuthPeriodLogEntry getUserMuniAuthPeriodLogEntry(int logEntryID) throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        UserMuniAuthPeriodLogEntry uacle = null;
        // broken query
        String query = "SELECT authperiodlogentryid, authperiod_periodid, category, entryts, \n" +
                        "       entrydateofrecord, disputedby_userid, disputedts, notes, cookie_jsessionid, \n" +
                        "       header_remoteaddr, header_useragent, header_dateraw, header_date, \n" +
                        "       header_cachectl, audit_usersession_userid, audit_usercredential_userid, \n" +
                        "       audit_muni_municode\n" +
                        "  FROM public.loginmuniauthperiodlog WHERE authperiodlogentryid=?;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, logEntryID);
            rs = stmt.executeQuery();
            while(rs.next()){
                uacle = generateMuniAuthPeriodLogEntry(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error getting user access record", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

        return uacle;
        
    }
    
    /**
     * Generator method for objects representing an entry in the user auth log
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
    private UserMuniAuthPeriodLogEntry generateMuniAuthPeriodLogEntry(ResultSet rs) throws SQLException, IntegrationException{
        UserMuniAuthPeriodLogEntry uacle = new UserMuniAuthPeriodLogEntry();
        
        uacle.setLogBookEntryID(rs.getInt("authperiodlogentryid"));
        uacle.setUserMuniAuthPeriodID(rs.getInt("authperiod_periodid"));
        uacle.setCategory(rs.getString("category"));
        
        if(rs.getTimestamp("entrydateofrecord") != null){
            uacle.setEntryTS(rs.getTimestamp("entrydateofrecord").toLocalDateTime());
        }
        uacle.setDisputedByUserID(rs.getInt("disputedby_userid"));
        if(rs.getTimestamp("disputedts") != null){
            uacle.setDisputedts(rs.getTimestamp("disputedts").toLocalDateTime());
        }
        uacle.setNotes(rs.getString("notes"));
        uacle.setCookie_jsessionid(rs.getString("cookie_jsessionid"));
        
        uacle.setHeader_remoteaddr(rs.getString("header_remoteaddr"));
        uacle.setHeader_useragent(rs.getString("header_useragent"));
        uacle.setHeader_dateraw(rs.getString("header_dateraw"));
        if(rs.getTimestamp("header_date") != null){
            uacle.setHeader_date(rs.getTimestamp("header_date").toLocalDateTime());
        } else {
            uacle.setHeader_date(null);
        }
        
        uacle.setHeader_cachectl(rs.getString("header_cachectl"));
        uacle.setAudit_usersession_userid(rs.getInt("audit_usersession_userid"));
        uacle.setAudit_usercredential_userid(rs.getInt("audit_usercredential_userid"));
        
        uacle.setAudit_muni_municode(rs.getInt("audit_muni_municode"));
        
        return uacle;
    }
    
    /**
     * Remember that the notion of a Credential only exists in Java land, since a User 
     * is never "in" the database, doing stuff.
     * @param uacle
     * @throws IntegrationException 
     */
    public void updateUserMuniAuthPeriodLogEntry(UserMuniAuthPeriodLogEntry uacle) throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        UserMuniAuthPeriod per = null;
        // broken query
        String query = "UPDATE public.loginmuniauthperiodlog\n" +
                        "   SET category=?, entrydateofrecord=?, disputedby_userid=?, disputedts=?, notes=?, \n" +
                        " WHERE authperiodlogentryid=?;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, uacle.getCategory());
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(uacle.getEntryDateOfRecord()));
            stmt.setInt(3, uacle.getDisputedByUserID());
            if(uacle.getDisputedts() != null){
                stmt.setTimestamp(4,  java.sql.Timestamp.valueOf(uacle.getDisputedts()));
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            stmt.setString(5, uacle.getNotes());
            
            stmt.setInt(6, uacle.getUserMuniAuthPeriodID());
            stmt.executeUpdate();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error getting user access record", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    

    /**
     * Insertion point for records in the loginmuniauthperiod table
     * @param uap
     * @throws IntegrationException 
     */
    public void insertNewUserAuthorizationPeriod(UserMuniAuthPeriod uap) throws IntegrationException{
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        
        String query = "INSERT INTO public.loginmuniauthperiod(\n" +
                        "            muniauthperiodid, muni_municode, authuser_userid, accessgranteddatestart, \n" +
                        "            accessgranteddatestop, recorddeactivatedts, authorizedrole, createdts, \n" +
                        "            createdby_userid, notes, supportassignedby, assignmentrank)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, \n" +
                        "            ?, NULL, CAST (? AS role), now(), \n" +
                        "            ?, ?, ?, ?);";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, uap.getMuni().getMuniCode());
            stmt.setInt(2, uap.getUserID());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(uap.getStartDate()));
            
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(uap.getStopDate()));
            
            stmt.setString(5, uap.getRole().toString());
            // created TS by postgres now()
            
            stmt.setInt(6, uap.getCreatedByUserID());
            stmt.setString(7, uap.getNotes());
            // set support assigned to null until functionality implemented
            stmt.setNull(8, java.sql.Types.NULL);
            stmt.setInt(9, uap.getAssignmentRelativeOrder());
            
            stmt.execute();
            
            updateUserLastUpdatedTS(uap.getUserID());
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting new authorization period", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
              if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
    }
    
    /**
     * Insertion point for records in the login table; NOTE that this is a rather 
     * sparse isnert since several login fields like password, etc., are managed 
     * by separate methods in this class and the UserCoordinator
     * @param userToInsert
     * @return
     * @throws IntegrationException 
     */
    public int insertUser(User userToInsert) throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query =  "INSERT INTO public.login(\n" +
                        "            userid, username, password, notes, personlink, pswdlastupdated, \n" +
                        "            forcepasswordreset, createdby, createdts, nologinvirtualonly, \n" +
                        "            deactivatedts, deactivated_userid, lastupdatedts, userrole, homemuni)\n" +
                        "    VALUES (DEFAULT, ?, NULL, ?, ?, NULL, \n" +
                        "            NULL, ?, now(), ?, \n" +
                        "            NULL, NULL, now(), 'User'::role, ?);";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, userToInsert.getUsername());
            stmt.setString(2, userToInsert.getNotes());
            
            if(userToInsert.getPerson() == null){
                stmt.setInt(3, userToInsert.getPersonID());
            } else {
                stmt.setInt(3, userToInsert.getPerson().getPersonID());
            }
            
            if(userToInsert.getCreatedByUserId() != 0){
                stmt.setInt(4, userToInsert.getCreatedByUserId());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            // created ts from db's now()
            stmt.setBoolean(5, userToInsert.isNoLoginVirtualUser());
            
            if(userToInsert.getHomeMuniID() != 0){
                stmt.setInt(6, userToInsert.getHomeMuniID());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            
            stmt.execute();
            
            String idNumQuery = "SELECT currval('login_userid_seq');";
            Statement s = con.createStatement();
            rs = s.executeQuery(idNumQuery);
            rs.next();
            int newID = rs.getInt("currval");
            return newID;
            
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting new user", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
              if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
    }
    
    
    /**
     * Insertion point for records in the login table; NOTE that this is a rather 
     * sparse isnert since several login fields like password, etc., are managed 
     * by separate methods in this class and the UserCoordinator
     * @param muni
     * @return
     * @throws IntegrationException 
     */
    public List<User> getUsersByHomeMuni(Municipality muni) throws IntegrationException{
        UserCoordinator uc = getUserCoordinator();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query =  "SELECT userid FROM public.login WHERE homemuni = ?;";
        List<User> usrList = new ArrayList<>();
        if(muni == null){
            return usrList;
        }
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, muni.getMuniCode());
            stmt.execute();
            
            rs = stmt.executeQuery();
            while(rs.next()){
                usrList.add(uc.user_getUser(rs.getInt("userid")));
            }
            
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting new user", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
              if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return usrList;
    }
    
    /**
     * Invalidating an Authorization period is effectively deleting that record. This action
     * cannot be undone, not even by developers.
     * 
     * @param uap
     * @throws IntegrationException 
     */
    public void invalidateUserAuthRecord(UserMuniAuthPeriod uap) throws IntegrationException{
         Connection con = getPostgresCon();
        
         String query =     "UPDATE loginmuniauthperiod SET recorddeactivatedts = now(), notes=? "
                        +   "WHERE muniauthperiodid=?;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, uap.getNotes());
            stmt.setInt(2, uap.getUserMuniAuthPeriodID());
            
            stmt.executeUpdate();
            
            // any update to a User's UMAP is also an update to that user, so stamp it!
            updateUserLastUpdatedTS(uap.getUserID());
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error updating password", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
    /**
     * As of September 2019, this method will hash incoming passwords with MD5 before writing to the 
     * table. Wildfly's config file specifies that it will digest any submitted password with MD5
     * and then compare. I wanted to use the crypto() library in posgres but that fucntion returns 
     * a true/false and knows how to do its own comparison--but that doesn't work with the current postgres setup
     * 
     * @param user
     * @param psswd
     * @throws IntegrationException 
     */
    public void setUserPassword_SECURITYCRITICAL(User user, String psswd) throws IntegrationException{
         Connection con = getPostgresCon();
        
         String query = "UPDATE public.login\n" +
            "   SET password = encode(digest(?, 'md5'), 'base64') WHERE userid = ?";
        
         String updateQuery = "UPDATE public.login\n" +
            "   SET pswdlastupdated = now() WHERE userid = ?";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, psswd);
            stmt.setInt(2, user.getUserID());
            
            stmt.executeUpdate();
            
            stmt = con.prepareStatement(updateQuery);
            stmt.setInt(1, user.getUserID());
            
            stmt.executeUpdate();
            
            updateUserLastUpdatedTS(user.getUserID());
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error updating password", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Utility method for marking a User as last updated now() since
     * there are a number of methods that change a single field, like username,
     * or forcing a password reset
     * @param userID
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void updateUserLastUpdatedTS(int userID) throws IntegrationException{
         Connection con = getPostgresCon();
        
         String query = "UPDATE public.login \n" +
            "   SET lastupdatedts = now() WHERE userid = ?";
        
        PreparedStatement stmt = null;
        if(userID != 0){
            try {
                stmt = con.prepareStatement(query);
                stmt.setInt(1, userID);

                stmt.executeUpdate();

            } catch (SQLException ex) {
                System.out.println(ex);
                throw new IntegrationException("Error updating user's lastupdated timestamp", ex);
            } finally{
                 if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
                 if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            } // close finally
        }
        
    }
    
    
    
    /**
     * Updates fields on login table record; most login fields are managed by independent
     * methods for security thoroughness
     * @param usr
     * @throws IntegrationException 
     */
    public void updateUser(User usr) throws IntegrationException{
        Connection con = getPostgresCon();
        System.out.println("UserIntegrator.updateUser");
        String query =  "UPDATE public.login\n" +
                        "   SET notes=?, personlink=?, \n" +
                        "    nologinvirtualonly=?, deactivatedts=?, deactivated_userid=?, lastupdatedts=now(), homemuni=? \n" +
                        " WHERE userid=?;";
        
        PreparedStatement stmt = null;
        	
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, usr.getNotes());
    
            // check both the object and ID person link fields
            // without a Person object, use the raw ID
            if(usr.getPerson() == null){
                if(usr.getPersonID() != 0){
                    stmt.setInt(2, usr.getPersonID());
                } else {
                    stmt.setNull(2, java.sql.Types.NULL);
                }
            } else { // we've got a person object
                if(usr.getPerson().getPersonID() != 0){ // make sure it's not a new Person
                    stmt.setInt(2, usr.getPerson().getPersonID());
                } else {
                    stmt.setNull(2, java.sql.Types.NULL);
                }
            }
            
            stmt.setBoolean(3, usr.isNoLoginVirtualUser());
            
            if(usr.getDeactivatedTS() != null){
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(usr.getDeactivatedTS()));
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            if(usr.getDeactivatedBy() != 0){
                stmt.setInt(5, usr.getDeactivatedBy());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            if(usr.getHomeMuniID() != 0){
                stmt.setInt(6, usr.getHomeMuniID());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            stmt.setInt(7, usr.getUserID());
            
            stmt.executeUpdate();
            
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("UserIntegrator.updateUser:Error updating User", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
   
    /**
     * Forces the passed in user to reset their password on next login
     * @param usr
     * @throws IntegrationException 
     */
    public void forcePasswordReset(User usr) throws IntegrationException{
        Connection con = getPostgresCon();
        String query =  "UPDATE public.login\n" +
                        "   SET forcepasswordreset=now() " +
                        " WHERE userid=?;";
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, usr.getUserID());
            
            stmt.executeUpdate();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error updating userAuthEntry person", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Primary access point for the entire User system: Called during SessionInitializer actions
     * to create a new session
     * @param userName
     * @return the Fully-baked user object ready to be passed to and fro
     * @throws IntegrationException 
     */   
    public int getUserID(String userName) throws IntegrationException{
        
        System.out.println("UserIntegrator.getUser");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        int userID = 0;
        String query = "SELECT userid FROM login WHERE username = ?;";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, userName);
            rs = stmt.executeQuery();
            while(rs.next()){
                userID = rs.getInt("userid");
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("userint.getuser", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return userID;
    }
    
    /**
     * Utility method for extracting USER ids of all users who aren't deactivated
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> getSystemUserIDList() throws IntegrationException{

        List<Integer> idlst = new ArrayList<>();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        int userID = 0;
        String query = "SELECT userid FROM login WHERE deactivatedts IS NULL ;";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while(rs.next()){
                idlst.add(rs.getInt("userid"));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Userintegrator.getSystemUserIDList", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return idlst;
    }
    

    
}
