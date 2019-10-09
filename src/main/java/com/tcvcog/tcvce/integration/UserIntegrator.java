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
import com.tcvcog.tcvce.entities.UserAuthorizationPeriod;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.UserListified;
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
        String query =  "   SELECT userid, username, notes, personlink, pswdlastupdated, \n" +
                        "       active, forcepasswordreset, createdby, createdts, nologinvirtualonly\n" +
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
     * Legacy auth organelle
     * 
     * @deprecated 
     * @param userID
     * @param m
     * @return
     * @throws IntegrationException
     * @throws AuthorizationException 
     */
    public UserAuthorized getUserAuthorized(int userID) throws IntegrationException, AuthorizationException{
        if(userID == 0){
            throw new AuthorizationException("UserIntegrator.getUserWithAccessData | incoming userID = 0");
        }
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        UserAuthorized newUser = null;
        // broken query
        String query =  "   SELECT userid, username, notes, personlink \n" +
                        "   FROM public.login WHERE userid = ?;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, userID);
            rs = stmt.executeQuery();
            while(rs.next()){
                // this method deprecated
//                newUser = generateUserWithAccessData(rs, m);
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
            user.setPerson(pi.getPerson(rs.getInt("personlink")));
            
            user.setActive(rs.getBoolean("active"));
            user.setCreatedByUserId(rs.getInt("createdby"));
            if(rs.getTimestamp("createdts") != null){
                user.setCreatedTS(rs.getTimestamp("createdts").toLocalDateTime());
            }
            user.setNoLoginVirtualUser(rs.getBoolean("nologinvirtualonly"));
            if(rs.getTimestamp("pswdlastupdated") != null){
                user.setPswdLastUpdated(rs.getTimestamp("pswdlastupdated").toLocalDateTime());
            }
            if(rs.getTimestamp("forcepasswordreset") != null){
                user.setForcePasswordResetTS(rs.getTimestamp("forcepasswordreset").toLocalDateTime());
            }
            
        } catch (SQLException ex) {
            throw new IntegrationException("Cannot create user", ex);
        }
        
        return user;
    }
    
 
    
    
    public List<UserAuthorizationPeriod> getUserAuthorizationPeriods(User u) throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        List<UserAuthorizationPeriod> perList = new ArrayList<>();
        // broken query
        String query = "SELECT muniauthperiodid FROM public.loginmuniauthperiod WHERE authuser_userid=?;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, u.getUserID());
            rs = stmt.executeQuery();
            while(rs.next()){
                perList.add(getUserAuthorizationPeriod(rs.getInt("muniauthperiodid")));
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
    
    private UserAuthorizationPeriod getUserAuthorizationPeriod(int periodID) throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        UserAuthorizationPeriod per = null;
        // broken query
        String query = "SELECT muniauthperiodid, muni_municode, authuser_userid, defaultmuni, \n" +
                        "       accessgranteddatestart, accessgranteddatestop, recorddeactivatedts, \n" +
                        "       authorizedrole, createdts, createdby_userid, notes\n" +
                        "  FROM public.loginmuniauthperiod WHERE muniauthperiodid=?;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, periodID);
            rs = stmt.executeQuery();
            while(rs.next()){
                per = generateUserAuthPeriod(rs);
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
    
    
    private UserAuthorizationPeriod generateUserAuthPeriod(ResultSet rs) throws SQLException, IntegrationException{
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        UserIntegrator ui = getUserIntegrator();
        UserAuthorizationPeriod rec = new UserAuthorizationPeriod();
        
        rec.setMunLoginRecordID(rs.getInt("muniauthperiodid"));
        
        rec.setMuni(mi.getMuni(rs.getInt("muni_municode")));
        rec.setUserID(rs.getInt("authuser_userid"));
        rec.setDefaultMuni(rs.getBoolean("defaultmuni"));
        
        if(rs.getTimestamp("accessgranteddatestart") != null){
            rec.setStartDate(rs.getTimestamp("accessgranteddatestart").toLocalDateTime());
        }
        if(rs.getTimestamp("accessgranteddatestop") != null){
            rec.setStopDate(rs.getTimestamp("accessgranteddatestop").toLocalDateTime());
        }
        if(rs.getTimestamp("recorddeactivatedts") != null){
            rec.setRecorddeactivatedTS(rs.getTimestamp("recorddeactivatedts").toLocalDateTime());
        }
        if(rs.getTimestamp("createdts") != null){
            rec.setCreatedTS(rs.getTimestamp("createdts").toLocalDateTime());
        }

        rec.setCreatedBy(ui.getUser(rs.getInt("createdby_userid")));
        rec.setAuthorizedRole(RoleType.valueOf(rs.getString("authorizedrole")));
        
        rec.setNotes(rs.getString("notes"));
        
        return rec;
    }
    

    
    public void insertNewUserAuthorizationPeriod(UserAuthorizationPeriod uap) throws IntegrationException{
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query =  "INSERT INTO public.loginmuniauthperiod(\n" +
                        "            muniauthperiodid, muni_municode, authuser_userid, defaultmuni, \n" +
                        "            accessgranteddatestart, accessgranteddatestop, recorddeactivatedts, \n" +
                        "            authorizedrole, createdts, createdby_userid, notes, supportassignedby)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            CAST (? as role), now(), ?, ?, ?);";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, uap.getMuni().getMuniCode());
            stmt.setInt(2, uap.getUserID());
            stmt.setBoolean(3, uap.isDefaultMuni());
            
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(uap.getStartDate()));
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(uap.getStopDate()));
            if(uap.getRecorddeactivatedTS() != null){
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(uap.getRecorddeactivatedTS())); 
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            stmt.setString(7, uap.getAuthorizedRole().toString());
            // created TS by postgres now()
            stmt.setInt(8, uap.getCreatedBy().getUserID());
            stmt.setString(9, uap.getNotes());
            
            // set support assigned to null until functionality implemented
            stmt.setNull(10, java.sql.Types.NULL);
            
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
                        "            active, forcepasswordreset, createdby, createdts, nologinvirtualonly)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, ?);";
        
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
            if(userToInsert.getForcePasswordResetTS() != null){
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(userToInsert.getForcePasswordResetTS()));
            }
            stmt.setInt(6, userToInsert.getCreatedByUserId());
            
            if(userToInsert.getCreatedTS() !=  null){
                stmt.setTimestamp(7, java.sql.Timestamp.valueOf(userToInsert.getCreatedTS()));
            }
            stmt.setBoolean(8, userToInsert.isNoLoginVirtualUser());
            
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
     * Yikes! The embedding of access logic in the SQL statement inside this here
     * integration method is death.
     * 
     * @deprecated 
     * @param userID
     * @return
     * @throws IntegrationException
     * @throws AuthorizationException 
     */
    public Municipality getUserDefaultMunicipality(int userID) throws IntegrationException, AuthorizationException{
        Connection con = getPostgresCon();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        ResultSet rs = null;
        String query = "SELECT muni_municode FROM public.munilogin "
                + "WHERE userid=? AND defaultmuni=? AND recorddeactivatedts IS NULL "
                + "             AND accessgranteddatestart < now() "
                + "             AND accessgranteddatestop > now() "
                + "             ORDER BY recordcreatedts DESC;";
        Municipality m = null;
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query, 
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setInt(1, userID);
            stmt.setBoolean(2, true);
            rs = stmt.executeQuery();
            while(rs.next()){
                m = mi.getMuni(rs.getInt("muni_municode"));
            }
//            if(!rs.first()){
//                stmt = con.prepareStatement(query);
//                stmt.setInt(1, userID);
//                stmt.setBoolean(2, true);
//                rs = stmt.executeQuery();
//                while(rs.next()){
//                    m = mi.getMuni(rs.getInt("muni_municode"));
//                }
//                
//            }
                
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error getting default muni", ex);
        } finally{
            if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        if(m == null){
            throw new AuthorizationException("Could not load a default muni");
        }
        return m;
    }
    
    /**
     * @throws com.tcvcog.tcvce.domain.AuthorizationException
     * @deprecated 
     * @param u
     * @param m
     * @return
     * @throws IntegrationException 
     */
    public boolean setDefaultMunicipality(User u, Municipality m) throws IntegrationException, AuthorizationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        boolean defaultSet = false;
        String queryTurnOffDefaults = "UPDATE munilogin SET defaultmuni = FALSE "
                + "WHERE USERID = ?;";
        String query = "UPDATE munilogin SET defaultmuni = TRUE "
                + "WHERE USERID = ? AND MUNI_MUNICODE = ?;";
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(queryTurnOffDefaults);
            stmt.setInt(1, u.getUserID());
            stmt.executeUpdate();
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, u.getUserID());
            stmt.setInt(2, m.getMuniCode());
            stmt.executeUpdate();
            
            if(getUserDefaultMunicipality(u.getUserID()).equals(m)){
                defaultSet = true;
            }
            
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error setting default muni for user " + u.getUserID(), ex);
        } finally{
            if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return defaultSet;
    }
    
      public void invalidateUserAuthRecord(UserAuthorizationPeriod uap) throws IntegrationException{
         Connection con = getPostgresCon();
        
         String query = "UPDATE loginmuniauthperiod SET recorddeactivatedts = now() WHERE muniauthperiodid=?";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, uap.getMunLoginRecordID());
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
                        "    active=?, forcepasswordreset=?, createdby=?, \n" +
                        "    createdts=?, nologinvirtualonly=?\n" +
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
            
            if(usr.getForcePasswordResetTS() != null){
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(usr.getForcePasswordResetTS()));
            }
            stmt.setInt(6, usr.getCreatedByUserId());
            if(usr.getCreatedTS() !=  null){
                stmt.setTimestamp(7, java.sql.Timestamp.valueOf(usr.getCreatedTS()));
            }
            stmt.setBoolean(8, usr.isNoLoginVirtualUser());
            
            stmt.setInt(9, usr.getUserID());
            
            stmt.executeUpdate();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting new person", ex);
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
    
    
    public UserListified getUserListified(User u) throws IntegrationException{
        UserListified uwl = new UserListified(u);
        uwl.setUserAuthPeriodList(getUserAuthorizationPeriods(u));
        return uwl;
        
        
    }
    
   
    
    
    /**
     * For use by system administrators to manage user data
     * @return
     * @throws IntegrationException 
     */
    public List<User> getCompleteActiveUserList() throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        ArrayList<User> userList = new ArrayList();
        
       String query =  "SELECT muniauthperiodid, authuser_userid\n" +
                        "FROM public.loginmuniauthperiod WHERE recorddeactivatedts IS NULL \n" +
                        "	AND accessgranteddatestart < now() \n" +
                        "	AND accessgranteddatestop > now();";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while(rs.next()){
                userList.add(getUser(rs.getInt("authuser_userid")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error fetching user list", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return userList;
        
    }
    
    /**
     * For use by system administrators to manage user data
     * @param muniCode
     * @return
     * @throws IntegrationException 
     */
    public List getCompleteActiveUserList(int muniCode) throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        ArrayList<User> userList = new ArrayList();
        
       String query =  "SELECT userid "
                + "FROM munilogin "
                + "WHERE muni_municode=? "
                + "AND recorddeactivatedts IS NULL "
                + "AND accessgranteddatestart < now() "
                + "AND accessgranteddatestop > now();";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, muniCode);
            
            rs = stmt.executeQuery();
            while(rs.next()){
                userList.add(getUser(rs.getInt("userid")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error fetching user list", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return userList;
        
    }
    
     
    /**
     * For attaching event requests to default code officers by muni
     * @param muniCode
     * @return
     * @throws IntegrationException 
     */
    public List<User> getActiveCodeOfficerList(int muniCode) throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        List<User> userList = new ArrayList<>();
        
        String query =  "SELECT userid "
                + "FROM munilogin "
                + "WHERE muni_municode=? "
                + "AND recorddeactivatedts IS NULL "
                + "AND accessgranteddatestart < now() "
                + "AND accessgranteddatestop > now() "
                + "AND codeofficerstartdate < now() "
                + "AND codeofficerstopdate > now();";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, muniCode);
            
            rs = stmt.executeQuery();
            
            while(rs.next()){
                userList.add(getUser(rs.getInt("userid")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error fetching user list", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return userList;
        
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
