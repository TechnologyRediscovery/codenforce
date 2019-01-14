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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ObjectNotFoundException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;

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
     * Deprecated as of the revision to use the glassfish container security
     * instead of our own internal code
     * @param loginName
     * @param loginPassword
     * @return
     * @throws ObjectNotFoundException
     * @throws IntegrationException 
     */
    private User getAuthenticatedUser(String loginName, String loginPassword) throws ObjectNotFoundException, IntegrationException{
        System.out.println("UserIntegrator.getAuthenticatedUser | attempting to get user for " + loginName);
        
        String query = "SELECT username, password, userid FROM login"
                + " WHERE username= ? AND password = ?;";
        
        ResultSet rs;
        Connection con;
        User newlyAuthenticatedUser = null;
        
        // login is successful if the result set has any rows in it
        // TODO: create value comparison check as a backup to avoid SQL injection risks
        try {
            con = getPostgresCon();
            PreparedStatement stmt = con.prepareStatement(query);
            
            stmt.setString(1, loginName);
            stmt.setString(2, loginPassword);
            
            rs = stmt.executeQuery();
            
            String retrievedUsername;
            String retrievedPassword;
            int authenticatedUserid;
            
            // ACCESS CONTROL: ONLY CREATE USER IF THE USER EXISTS IN THE SYSTEM
            if(rs.next()){
                
                retrievedUsername = rs.getString("username");
                retrievedPassword = rs.getString("password");
                
                // check again that there is a direct match between what was entered by
                // user and what was retrieved from the DB
                if(retrievedUsername.equals(loginName) && retrievedPassword.equals(loginPassword)){
                    authenticatedUserid = rs.getInt("userid");
                    newlyAuthenticatedUser = getUser(authenticatedUserid);
                    return newlyAuthenticatedUser;
                    
                }
            
            } else {
                throw new ObjectNotFoundException("No User found with those credentials. Try again, please.");
            }
            
   
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to authenticate a user due to a SQL error", ex);
        } 
        
        
        return null;
        
    }
    
    public void insertUser(User userToInsert) throws IntegrationException{
        Connection con = getPostgresCon();
        
        String query = "INSERT INTO public.login(\n" +
"            userrole, username, password, muni_municode, fname, lname, \n" +
"            worktitle, phonecell, phonehome, phonework, email, address_street, \n" +
"            address_city, address_zip, address_state, notes, activitystartdate, \n" +
"            activitystopdate, accesspermitted, userid)\n" +
"    VALUES (CAST (? AS role) , ?, ?, ?, ?, ?, \n" +
"            ?, ?, ?, ?, ?, ?, \n" +
"            ?, ?, ?, ?, ?, \n" +
"            ?, ?, ?);";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, userToInsert.getRoleType().toString());
            stmt.setString(2, userToInsert.getUsername());
            stmt.setString(3, userToInsert.getPassword());
            stmt.setInt(4, userToInsert.getMuniCode());
            stmt.setString(5, userToInsert.getFName());
            stmt.setString(6, userToInsert.getLName());
            
            stmt.setString(7,userToInsert.getWorkTitle());
            stmt.setString(8, userToInsert.getPhoneCell());
            stmt.setString(9, userToInsert.getPhoneHome());
            stmt.setString(10, userToInsert.getPhoneWork());
            stmt.setString(11, userToInsert.getEmail());
            stmt.setString(12, userToInsert.getAddress_street());
            
            stmt.setString(13, userToInsert.getAddress_city());
            stmt.setString(14, userToInsert.getAddress_zip());
            stmt.setString(15, userToInsert.getAddress_state());
            stmt.setString(16, userToInsert.getNotes());
            stmt.setTimestamp(17, java.sql.Timestamp
                    .valueOf(userToInsert.getActivityStartDate()));
            
            stmt.setTimestamp(18, java.sql.Timestamp
                    .valueOf(userToInsert.getActivityStopDate()));
            stmt.setBoolean(19, userToInsert.isSystemAccessPermitted());
            stmt.setInt(20, userToInsert.getUserID());
            
            System.out.println("UserIntegrator.insertUser | sql: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting new person", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        
    }
    
    public void updateUser(User userToUpdate) throws IntegrationException{
        Connection con = getPostgresCon();
        
        String query = "UPDATE public.login\n" +
            "   SET userrole= CAST (? as role), username=?, password=?, muni_municode=?, \n" +
            "       fname=?, lname=?, worktitle=?, phonecell=?, phonehome=?, phonework=?, \n" +
            "       email=?, address_street=?, address_city=?, address_zip=?, address_state=?, \n" +
            "       notes=?, activitystartdate=?, activitystopdate=?, accesspermitted=?\n" +
            " WHERE userid = ?";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, userToUpdate.getRoleType().toString());
            stmt.setString(2, userToUpdate.getUsername());
            stmt.setString(3, userToUpdate.getPassword());
            stmt.setInt(4, userToUpdate.getMuniCode());
            
            stmt.setString(5, userToUpdate.getFName());
            stmt.setString(6, userToUpdate.getLName());
            stmt.setString(7,userToUpdate.getWorkTitle());
            stmt.setString(8, userToUpdate.getPhoneCell());
            stmt.setString(9, userToUpdate.getPhoneHome());
            stmt.setString(10, userToUpdate.getPhoneWork());
            
            stmt.setString(11, userToUpdate.getEmail());
            stmt.setString(12, userToUpdate.getAddress_street());
            stmt.setString(13, userToUpdate.getAddress_city());
            stmt.setString(14, userToUpdate.getAddress_zip());
            stmt.setString(15, userToUpdate.getAddress_state());
            
            stmt.setString(16, userToUpdate.getNotes());
            stmt.setTimestamp(17, java.sql.Timestamp
                    .valueOf(userToUpdate.getActivityStartDate()));
            stmt.setTimestamp(18, java.sql.Timestamp
                    .valueOf(userToUpdate.getActivityStopDate()));
            stmt.setBoolean(19, userToUpdate.isSystemAccessPermitted());
            
            stmt.setInt(20, userToUpdate.getUserID());
            System.out.println("UserIntegrator.updateUser | sql: " + stmt.toString());
            
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting new person", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
            
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Note that the client method is responsible for moving the cursor on the 
     * result set object before passing it into this method
     * @param rs
     * @return
     * @throws IntegrationException 
     */
    private User generateUser(ResultSet rs) throws IntegrationException{
        User user = null;
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        CodeIntegrator ci = getCodeIntegrator();
        int userID;
        try {
            userID = rs.getInt("userid");
            // the central control mechanism for user access: only configurable
            // upon User object construction!
            user = new User(userID, RoleType.valueOf(rs.getString("userrole")));
            user.setSystemAccessPermitted(rs.getBoolean("accesspermitted"));
            
            user.setAuthMuis(getUserAuthMunis(userID));
            user.setUsername(rs.getString("username"));
            // passwords managed by Glassfish
            //user.setPassword(rs.getString("password"));
            user.setMuniCode(rs.getInt("muni_municode"));
            user.setMuni(mi.getMuniFromMuniCode(rs.getInt("muni_muniCode")));
            
            user.setFName(rs.getString("fname"));
            user.setLName(rs.getString("lname"));
            user.setWorkTitle(rs.getString("worktitle"));
            user.setPhoneCell(rs.getString("phonecell"));
            user.setPhoneHome(rs.getString("phonehome"));
            user.setPhoneWork(rs.getString("phonework"));
            
            user.setEmail(rs.getString("email"));
            user.setAddress_street(rs.getString("address_street"));
            user.setAddress_city(rs.getString("address_city"));
            user.setAddress_zip(rs.getString("address_zip"));
            user.setAddress_state(rs.getString("address_state"));
            
            user.setNotes(rs.getString("notes"));
            
            if(rs.getTimestamp("activitystartdate") != null){
                user.setActivityStartDate(rs.getTimestamp("activitystartdate").toLocalDateTime());
            }
            
            if(rs.getTimestamp("activitystopdate") != null ){
                user.setActivityStopDate(rs.getTimestamp("activitystopdate").toLocalDateTime());
                
            }
            
        } catch (SQLException ex) {
            throw new IntegrationException("Cannot create user", ex);
        }
        
        return user;
    }
    
    // program later--we generally dont want to allow folks to delete
    // users from the web client since they'll be integrated into tables
    // etc. They should mark the user as inactive and with an expiry date that
    // is in the past
    public void deleteUser(User userToDelete){
        
        // no guts for me!
        
    } 
    
    /**
     * Deprecated from auth version that didn't use Glassfish's authorization scheme
     * @param userID
     * @return
     * @throws IntegrationException 
     */
    public User getUser(int userID) throws IntegrationException{
        System.out.println("UserIntegrator.getUserByID");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        User newUser = new User();
        // broken query
        String query = "SELECT * from login where userid = ?;";
        
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
            throw new IntegrationException("Error inserting new person", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return newUser;
    }
    /**
     * Primary access point for the entire User system: Called during SessionInitializer actions
     * to create a new session
     * @param userName
     * @return the Fully-baked user object ready to be passed to and fro
     * @throws IntegrationException 
     */
   
    public User getUser(String userName) throws IntegrationException{
        
        System.out.println("UserIntegrator.getUserByID");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        User newUser = new User();
        String query = "SELECT * from login where username = ?;";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, userName);
            rs = stmt.executeQuery();
            while(rs.next()){
                newUser = generateUser(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error getting a new person", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return newUser;
    }
    
    /**
     * Users are permitted access to a set of municipalities which are all dumped
     * into a List by this method during the user lookup process.
     * @param uid
     * @return A liste of Municipalities to which the user should be granted data-related
     * access within their user type domain
     * @throws IntegrationException 
     */
    public ArrayList<Municipality> getUserAuthMunis(int uid) throws IntegrationException{
        System.out.println("UserIntegrator.getUserAuthMunis " + uid);
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT muni_municode FROM loginmuni WHERE userid = ?;";
        ArrayList<Municipality>muniList = new ArrayList<>();
        PreparedStatement stmt = null;
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, uid);
            System.out.println("UserIntegrator.getUserAuthMunis | stmt: " + stmt.toString());
            rs = stmt.executeQuery();
            while(rs.next()){
                muniList.add(mi.getMuniFromMuniCode(rs.getInt("muni_municode")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting new person", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return muniList;
    }
    
    public void setUserAuthMunis(User u, ArrayList<Municipality> munilist) throws IntegrationException{       
        Connection con = getPostgresCon();
        String query = "INSERT INTO loginmuni (\n" + 
                "userid, muni_municode)\n" +  "VALUES (?,?)";        
        int userId = u.getUserID();
        PreparedStatement stmt = null;
        
        // could try go inside the for loop? What are the ramifications?
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
        } // close finally
        
    }
    
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
     * For use by system administrators to manage user data
     * @return
     * @throws IntegrationException 
     */
    public ArrayList getCompleteUserList() throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        ArrayList<User> userList = new ArrayList();
        
        String query = "SELECT * from login;";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while(rs.next()){
                userList.add(generateUser(rs));
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
    
}
