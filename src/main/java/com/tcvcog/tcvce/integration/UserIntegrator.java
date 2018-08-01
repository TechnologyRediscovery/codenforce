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
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

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
    public User getAuthenticatedUser(String loginName, String loginPassword) throws ObjectNotFoundException, IntegrationException{
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
            stmt.setBoolean(19, userToInsert.isAccessPermitted());
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
            stmt.setBoolean(19, userToUpdate.isAccessPermitted());
            
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
        User user = new User();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        CodeIntegrator ci = getCodeIntegrator();
        try {
            
            user.setUserID(rs.getInt("userid"));
            user.setRoleType(RoleType.valueOf(rs.getString("userrole")));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
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
            user.setAccessPermitted(rs.getBoolean("accesspermitted"));
            
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
    
    public User getUser(int userID) throws IntegrationException{
        System.out.println("UserIntegrator.getUserByID");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        User newUser = new User();
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
            throw new IntegrationException("Error inserting new person", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return newUser;
    }
    
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
