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
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthCredential;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriodLogEntry;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriod;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.UserConfigReady;
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
 * @author Eric C. Darsow
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
                        "       active, nologinvirtualonly\n" +
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
     *
     * @param u
     * @return
     * @throws IntegrationException 
     */
    public UserAuthorized getUserAuthorizedSkel(User u) throws IntegrationException{
        
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        // broken query
        String query =  " SELECT userid, pswdlastupdated, forcepasswordreset, createdby, createdts " +
                        " FROM public.login WHERE userid = ?;";
        
        UserAuthorized ua = null;
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, u.getUserID());
            rs = stmt.executeQuery();
            while(rs.next()){
                ua = new UserAuthorized(u);
                if(rs.getTimestamp("pswdlastupdated") != null){
                    ua.setPswdLastUpdated(rs.getTimestamp("pswdlastupdated").toLocalDateTime());
                }
                if(rs.getTimestamp("forcepasswordreset") != null){
                    ua.setForcePasswordResetTS(rs.getTimestamp("forcepasswordreset").toLocalDateTime());
                }
                System.out.println("UserIntegrator.getAuthorizedUser | uaid: " + ua.getUserID());
                ua.setCreatedByUserId(rs.getInt("createdby"));
                if(rs.getTimestamp("createdts") != null){
                    ua.setCreatedTS(rs.getTimestamp("createdts").toLocalDateTime());
                }
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
     * Note that the client method is responsible for moving the cursor on the 
     * result set object before passing it into this method     * 
     * @param rs
     * @return
     * @throws IntegrationException 
     */
    private User generateUser(ResultSet rs) throws IntegrationException{
        User user = new User();
        PersonIntegrator pi = getPersonIntegrator();
        try {
            user.setUserID(rs.getInt("userid"));
            user.setUsername(rs.getString("username"));
            user.setNotes(rs.getString("notes"));
            user.setActive(rs.getBoolean("active"));
            user.setPerson(pi.getPerson(rs.getInt("personlink")));
            user.setNoLoginVirtualUser(rs.getBoolean("nologinvirtualonly"));
            
        } catch (SQLException ex) {
            throw new IntegrationException("Cannot create user", ex);
        }
        
        return user;
    }
    
  
 
    
    /**
     * Provides a complete list of records by User in the table loginmuniauthperiod.
     * Client is responsible for validating the UMAP object by the UserCoordinator
     * @param u
     * @return
     * @throws IntegrationException 
     */
    public List<UserMuniAuthPeriod> getUserMuniAuthPeriodsRaw(User u) throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        List<UserMuniAuthPeriod> perList = new ArrayList<>();
        // broken query
        String query = "SELECT muniauthperiodid FROM public.loginmuniauthperiod WHERE authuser_userid=?;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, u.getUserID());
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
        
        return perList;
        
    }
    
    /**
     * For use by system administrators to manage user data
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
    
    private UserMuniAuthPeriod getUserMuniAuthPeriod(int periodID) throws IntegrationException{
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
        UserMuniAuthPeriod per = new UserMuniAuthPeriod();
        
        per.setUserAuthPeriodID(rs.getInt("muniauthperiodid"));
        
        per.setMuni(mi.getMuni(rs.getInt("muni_municode")));
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
        per.setAssignmentRank(rs.getInt("assignmentrank"));
        
        
        return per;
    }
    
    
    public void insertUserMuniAuthPeriodLogEntry(UserMuniAuthPeriodLogEntry uacle) throws IntegrationException, AuthorizationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        UserMuniAuthPeriod per = null;
        // broken query
        String query =  "INSERT INTO public.loginmuniauthperiodlog(\n" +
                        "            authperiodlogentryid, authperiod_periodid, category, entryts, \n" +
                        "            entrydateofecord, disputedby_userid, disputedts, notes, cookie_jsessionid, \n" +
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
            
            stmt.setInt(1, uacle.getAuthPeriod().getUserAuthPeriodID());
            stmt.setString(2, uacle.getCategory());
            // entry ts and dateofrecord set by PG's now()
            
            if(uacle.getDisputedBy() == null && uacle.getDisputedts() == null){
                stmt.setNull(3, java.sql.Types.NULL);
                stmt.setNull(4, java.sql.Types.NULL);
            } else {
                throw new AuthorizationException("Cannot insert a CredentialLogEntry with not null disputed fields!");
            }
            stmt.setString(5, uacle.getNotes());
            stmt.setString(6, uacle.getCookie_jsessionid());
            
            stmt.setString(7, uacle.getHeader_remoteaddr());
            stmt.setString(8, uacle.getHeader_useragent());
            stmt.setString(9,  uacle.getHeader_dateraw());
            
            //header date java type set to NULL in SQL
            stmt.setString(10, uacle.getHeader_cachectl());
            stmt.setInt(11, uacle.getAudit_usersession_userid());
            stmt.setInt(12, uacle.getAudit_usercredential_userid());
            
            stmt.setInt(13, uacle.getAudit_muni_municode());
            
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
    
    public List<UserMuniAuthPeriodLogEntry> getMuniAuthPeriodLogEntrys(UserMuniAuthPeriod uap) throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        List<UserMuniAuthPeriodLogEntry> uacleList = new ArrayList<>();
        // broken query
        String query = "SELECT authperiodlogentryid" +
                        "  FROM public.loginmuniauthperiodlog WHERE authperiod_periodid=?;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, uap.getUserAuthPeriodID());
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
                        "       entrydateofecord, disputedby_userid, disputedts, notes, cookie_jsessionid, \n" +
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
    
    
    private UserMuniAuthPeriodLogEntry generateMuniAuthPeriodLogEntry(ResultSet rs) throws SQLException, IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        
        
        UserMuniAuthPeriodLogEntry uacle = new UserMuniAuthPeriodLogEntry();
        
//        SELECT authperiodlogentryid, authperiod_periodid, category, entryts, \n" +
//                        "       entrydateofecord, disputedby_userid, disputedts, notes, cookie_jsessionid, \n" +
//                        "       header_remoteaddr, header_useragent, header_dateraw, header_date, \n" +
//                        "       header_cachectl, audit_usersession_userid, audit_usercredential_userid, \n" +
//                        "       audit_muni_municode\n" +
//                        "  FROM public.loginmuniauthperiodlog WHERE authperiodlogentryid=?;";
        
        uacle.setAuthperiodlogentryID(rs.getInt("authperiodlogentryid"));
        uacle.setAuthPeriod(getUserMuniAuthPeriod(rs.getInt("authperiod_periodid")));
        uacle.setCategory(rs.getString("category"));
        
        if(rs.getTimestamp("entrydateofrecord") != null){
            uacle.setEntryTS(rs.getTimestamp("entrydateofrecord").toLocalDateTime());
        }
        uacle.setDisputedBy(ui.getUser(rs.getInt("disputedby_userid")));
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
                        "   SET category=?, entrydateofecord=?, disputedby_userid=?, disputedts=?, notes=?, \n" +
                        " WHERE authperiodlogentryid=?;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, uacle.getCategory());
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(uacle.getEntryDateOfRecord()));
            if(uacle.getDisputedBy() != null){
                stmt.setInt(3, uacle.getDisputedBy().getUserID());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            if(uacle.getDisputedts() != null){
                stmt.setTimestamp(4,  java.sql.Timestamp.valueOf(uacle.getDisputedts()));
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            stmt.setString(5, uacle.getNotes());
            
            stmt.setInt(6, uacle.getAuthperiodlogentryID());
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
    

    
    public void insertNewUserAuthorizationPeriod(UserMuniAuthPeriod uap) throws IntegrationException{
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        
        String query = "INSERT INTO public.loginmuniauthperiod(\n" +
                        "            muniauthperiodid, muni_municode, authuser_userid, accessgranteddatestart, \n" +
                        "            accessgranteddatestop, recorddeactivatedts, authorizedrole, createdts, \n" +
                        "            createdby_userid, notes, supportassignedby, assignmentrank)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, \n" +
                        "            ?, ?, CAST (? AS role), now(), \n" +
                        "            ?, ?, ?, ?);";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, uap.getMuni().getMuniCode());
            stmt.setInt(2, uap.getUserID());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(uap.getStartDate()));
            
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(uap.getStopDate()));
            if(uap.getRecorddeactivatedTS() != null){
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(uap.getRecorddeactivatedTS())); 
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            stmt.setString(6, uap.getRole().toString());
            // created TS by postgres now()
            
            stmt.setInt(7, uap.getCreatedByUserID());
            stmt.setString(8, uap.getNotes());
            // set support assigned to null until functionality implemented
            stmt.setNull(9, java.sql.Types.NULL);
            stmt.setInt(10, uap.getAssignmentRank());
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting new authorization period", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
              if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
    }
    
    
    public int insertUser(User userToInsert) throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query =  "INSERT INTO public.login(\n" +
                        "            userid, username, notes, personlink, \n" +
                        "            active, createdby, createdts, nologinvirtualonly \n" +
                        "    VALUES (DEFAULT, ?, ?, ?, \n" +
                        "            ?, ?, now(), ?);";
        
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
            
            stmt.setBoolean(4, userToInsert.isActive());
            
            stmt.setInt(5, userToInsert.getCreatedByUserId());
            
            // created ts from db's now()
            stmt.setBoolean(6, userToInsert.isNoLoginVirtualUser());
            
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
     * Invalidating an Authorization period is effectively deleting that record. This action
     * cannot be undone, not even by developers.
     * 
     * @param uap
     * @throws IntegrationException 
     */
    public void invalidateUserAuthRecord(UserMuniAuthPeriod uap) throws IntegrationException{
         Connection con = getPostgresCon();
        
         String query =     "UPDATE loginmuniauthperiod SET recorddeactivatedts = now(), notes=? "
                        +   "WHERE muniauthperiodid=?";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, uap.getUserAuthPeriodID());
            stmt.setString(2, uap.getNotes());
            
            stmt.executeUpdate();
            
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
    public void setUserPassword(User user, String psswd) throws IntegrationException{
         Connection con = getPostgresCon();
        
         String query = "UPDATE login SET password = encode(digest(?, 'md5'), 'base64') WHERE userid = ?;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, psswd);
            stmt.setInt(2, user.getUserID());
            
            stmt.executeUpdate();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error updating password", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public void updateUser(User usr) throws IntegrationException{
        Connection con = getPostgresCon();
        
        String query =  "UPDATE public.login\n" +
                        "   SET username=?, notes=?, personlink=?, \n" +
                        "    active=?, nologinvirtualonly=?\n" +
                        " WHERE userid=?;";
        
        PreparedStatement stmt = null;
        	
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, usr.getUsername());
            stmt.setString(2, usr.getNotes());
            
            if(usr.getPerson() == null){
                stmt.setInt(3, usr.getPersonID());
            } else {
                stmt.setInt(3, usr.getPerson().getPersonID());
            }
            
            stmt.setBoolean(4, usr.isActive());
            stmt.setBoolean(5, usr.isNoLoginVirtualUser());
            stmt.setInt(6, usr.getUserID());
            
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
     * TODO: Implement functionality for password reset
     * @param usr
     * @throws IntegrationException 
     */
    public void updateUserAuthorized(UserAuthorized usr) throws IntegrationException{
        Connection con = getPostgresCon();
        String query =  "UPDATE public.login\n" +
                        "   SET forcepasswordreset=? " +
                        " WHERE userid=?;";
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            if(usr.getForcePasswordResetTS() != null){
                stmt.setTimestamp(1, java.sql.Timestamp.valueOf(usr.getForcePasswordResetTS()));
            }
            
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
     * Primary access point for retrieving the numeric ID for a 
     * logged in User. Jboss only knows about user-entered names
     * : Called during SessionInitializer actions
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
     * Inserts user-municipality mappings into the loginmuni table. This effectively gives the user
     * "permissions" to view the data for the municipalities that are linked to their userid.     * 
     * @param u - A User
     * @param munilist - A list of municipalities to be mapped to User u
     * @throws IntegrationException 
     */
    public void setUserAuthMunis(User u, List<Municipality> munilist) throws IntegrationException{       
        Connection con = getPostgresCon();
        String query = "INSERT INTO loginmuni (\n" + 
                "userid, muni_municode)\n" +  "VALUES (?,?)";        
        int userId = u.getUserID();
        PreparedStatement stmt = null;
        
        try {                   
                stmt = con.prepareStatement(query);
                stmt.setInt(1,userId);
                for(Municipality muni: munilist){
                    System.out.println("UserIntegrator.setUserAuthMunis: " + muni.getMuniCode());
                    int municode = muni.getMuniCode();
                    stmt.setInt(2,municode);
                    stmt.execute();
                }
                
        } catch (SQLException ex) {
            System.out.println("UserIntegrator.setUserAuthMunis exception encountered." + ex);
            throw new IntegrationException("Error in mapping authorized municipality to user", ex);
        } finally {
            if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        }
        
    }
    
    /**
     * Removes a user-municipality mapping from the loginmuni table.     * 
     * @param u - A User
     * @param muni - A Municipality
     * @throws IntegrationException 
     */
    public void deleteUserAuthMuni(User u, Municipality muni) throws IntegrationException{
        Connection con = getPostgresCon();
        String query = "DELETE FROM loginmuni WHERE (userid, muni_municode) = (?,?)";
        
        int userId = u.getUserID();
        int municode = muni.getMuniCode();
        PreparedStatement stmt = null;
        
        try { 
            stmt = con.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setInt(2,municode);
            stmt.execute();
        }
        catch (SQLException ex) {
            System.out.println("UserIntegrator.deleteUserAuthMuni: Error deleting row from loginmuni");
            throw new IntegrationException();
        } finally {
            if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        }
    }        
    
  
    
   
    
    
    /**
     * Writes in a history record when a User accesses that object.
     * The Object's type will be checked against existing history 
     * recording opportunities and create an appropriate entry in the
     * loginobjecthistory table.
     * 
     * Checks for duplicates in the table before inserting. 
     * If duplicate object ID exists, update existing entry with the current
     * time stamp only.
     *  
     * @param u the User who viewed the object
     * @param ob any Object that's displayed in a data table or list in the system
     * @throws IntegrationException 
     */
    public void logObjectView(User u, Object ob) throws IntegrationException{
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        StringBuilder selectSB = new StringBuilder();
        selectSB.append("SELECT historyentryid FROM loginobjecthistory "
                + "WHERE login_userid = ? ");
        
        StringBuilder insertSB = new StringBuilder();
        insertSB.append("INSERT INTO loginobjecthistory ");
        
        StringBuilder updateSB = new StringBuilder();
        updateSB.append("UPDATE loginobjecthistory SET entrytimestamp = now() "
                + "WHERE login_userid = ? ");
        
        try {
            if(ob instanceof Person){
                Person p = (Person) ob;
                
                // prepare SELECT statement
                selectSB.append("AND person_personid = ? ");
                stmt = con.prepareStatement(selectSB.toString(),
                        ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, p.getPersonID());
                rs = stmt.executeQuery();
                
                if(rs.first()){ // history entry with this user and person already exists
                    updateSB.append("AND person_personid = ? ");
                    stmt = con.prepareStatement(updateSB.toString());
                    
                } else { // pair not in history, do fresh insert
                    insertSB.append("(login_userid, person_personid, entrytimestamp) VALUES (?, ?, DEFAULT); ");
                    stmt = con.prepareStatement(insertSB.toString());
                }

                // each UPDATE and INSERT SQL structures take the params in this order
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, p.getPersonID());
                stmt.execute();
                System.out.println("UserIntegrator.logObjectView: Person view logged id = " + p.getPersonID());
                
            } else if(ob instanceof Property){
                Property p = (Property) ob;
                // prepare SELECT statement
                selectSB.append("AND property_propertyid = ? ");
                stmt = con.prepareStatement(selectSB.toString(),
                        ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, p.getPropertyID());
                rs = stmt.executeQuery();
                
                if(rs.first()){ // history entry with this user and person already exists
                    updateSB.append("AND property_propertyid = ? ");
                    stmt = con.prepareStatement(updateSB.toString());
                    
                } else { // pair not in history, do fresh insert
                    insertSB.append("(login_userid, property_propertyid, entrytimestamp) VALUES (?, ?, DEFAULT); ");
                    stmt = con.prepareStatement(insertSB.toString());
                }

                // each UPDATE and INSERT SQL structures take the params in this order
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, p.getPropertyID());
                stmt.execute();
                System.out.println("UserIntegrator.logObjectView: Property view logged id = " + p.getPropertyID());
            
            } else if(ob instanceof CECase){
                CECase c = (CECase) ob;
                // prepare SELECT statement
                selectSB.append("AND cecase_caseid = ? ");
                stmt = con.prepareStatement(selectSB.toString(),
                        ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, c.getCaseID());
                rs = stmt.executeQuery();
                
                if(rs.first()){ // history entry with this user and person already exists
                    updateSB.append("AND cecase_caseid = ? ");
                    stmt = con.prepareStatement(updateSB.toString());
                    
                } else { // pair not in history, do fresh insert
                    insertSB.append("(login_userid, cecase_caseid, entrytimestamp) VALUES (?, ?, DEFAULT); ");
                    stmt = con.prepareStatement(insertSB.toString());
                }

                // each UPDATE and INSERT SQL structures take the params in this order
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, c.getCaseID());
                stmt.execute();
                System.out.println("UserIntegrator.logObjectView: Case view logged id = " + c.getCaseID());
            }

        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error writign object history: persons, properties, or cecases", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }

    
}
