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
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
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
    
   
    
    public int insertUser(User userToInsert) throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "INSERT INTO public.login(\n" +
            "            userid, userrole, username, notes, activitystartdate, \n" +
            "            activitystopdate, accesspermitted, enforcementofficial, badgenumber, \n" +
            "       orinumber, personlink, password)\n" +
            "    VALUES (DEFAULT, CAST (? AS role), ?, ?, ?, ?, \n" +
            "            ?, ?, ?, ?, \n" +
            "            ?, ?, ?);";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, userToInsert.getRoleType().toString());
            stmt.setString(2, userToInsert.getUsername());
            
            stmt.setString(3, userToInsert.getNotes());
            stmt.setTimestamp(4, java.sql.Timestamp
                    .valueOf(userToInsert.getActivityStartDate()));
            
            stmt.setTimestamp(5, java.sql.Timestamp
                    .valueOf(userToInsert.getActivityStopDate()));
            stmt.setBoolean(6, userToInsert.isSystemAccessPermitted());
            
            stmt.setBoolean(7, userToInsert.isIsEnforcementOfficial());
            stmt.setString(8, userToInsert.getBadgeNumber());
            stmt.setString(9, userToInsert.getOriNumber());
            if(userToInsert.getPerson() == null){
                stmt.setInt(10, userToInsert.getPersonID());
            } else {
                stmt.setInt(10, userToInsert.getPerson().getPersonID());
            }
            
            stmt.setString(11, userToInsert.getPassword());
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
    
    public Municipality getDefaultMunicipality(User u) throws IntegrationException{
        Connection con = getPostgresCon();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        ResultSet rs = null;
        String query = "SELECT muni_municode FROM public.loginmuni "
                + "WHERE userid=? AND defaultmuni=?;";
        Municipality m = null;
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query, 
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setInt(1, u.getUserID());
            stmt.setBoolean(2, true);
            rs = stmt.executeQuery();
            while(rs.next()){
                m = mi.getMuni(rs.getInt("muni_municode"));
            }
            if(!rs.first()){
                stmt = con.prepareStatement(query);
                stmt.setInt(1, u.getUserID());
                stmt.setBoolean(2, true);
                rs = stmt.executeQuery();
                while(rs.next()){
                    m = mi.getMuni(rs.getInt("muni_municode"));
                }
                
            }
                
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error getting default muni", ex);
        } finally{
            if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return m;
    }
    
    public boolean setDefaultMunicipality(User u, Municipality m) throws IntegrationException{
        Connection con = getPostgresCon();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        ResultSet rs = null;
        boolean defaultSet = false;
        String query = "UPDATE loginmuni SET defaultmuni = TRUE "
                + "WHERE USERID = ? AND MUNI_MUNICODE = ?;";
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, u.getUserID());
            stmt.setInt(2, m.getMuniCode());
            stmt.executeUpdate();
            
            if(getDefaultMunicipality(u).equals(m)){
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
    
    public void setUserPassword(User user, String psswd) throws IntegrationException{
         Connection con = getPostgresCon();
        
         String query = "UPDATE public.login\n" +
            "   SET password= ? WHERE userid = ?";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, psswd);
            stmt.setInt(2, user.getUserID());
            
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error updating password", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public void updateUser(User userToUpdate) throws IntegrationException{
        Connection con = getPostgresCon();
        
        String query = "UPDATE public.login\n" +
            "   SET userrole= CAST (? as role), username=?, \n" +
            "       notes=?, activitystartdate=?, activitystopdate=?, accesspermitted=?, "
                +" enforcementofficial=?, badgenumber=?, orinumber=?, personlink=? "  +
            " WHERE userid = ?";
        
        PreparedStatement stmt = null;
        	
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, userToUpdate.getRoleType().toString());
            stmt.setString(2, userToUpdate.getUsername());
            
            stmt.setString(3, userToUpdate.getNotes());
            stmt.setTimestamp(4, java.sql.Timestamp
                    .valueOf(userToUpdate.getActivityStartDate()));
            stmt.setTimestamp(5, java.sql.Timestamp
                    .valueOf(userToUpdate.getActivityStopDate()));
            stmt.setBoolean(6, userToUpdate.isSystemAccessPermitted());
            stmt.setBoolean(7, userToUpdate.isIsEnforcementOfficial());
            stmt.setString(8, userToUpdate.getBadgeNumber());
            stmt.setString(9, userToUpdate.getOriNumber());
            stmt.setInt(10, userToUpdate.getPerson().getPersonID());
            
            stmt.setInt(11, userToUpdate.getUserID());
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
     * result set object before passing it into this method     * 
     * @param rs
     * @return
     * @throws IntegrationException 
     */
    private User generateUser(ResultSet rs) throws IntegrationException{
        User user = new User();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        PersonIntegrator pi = getPersonIntegrator();
        try {
            user.setUserID(rs.getInt("userid"));
            // the central control mechanism for user access: only configurable
            // upon User object construction!
            user.setRoleType(RoleType.valueOf(rs.getString("userrole")));
            user.setSystemAccessPermitted(rs.getBoolean("accesspermitted"));
//            user.setAuthMuis(getUserAuthMunis(user.getUserID()));
            
            user.setUsername(rs.getString("username"));
            // passwords managed by Glassfish
            //user.setPassword(rs.getString("password"));
            user.setNotes(rs.getString("notes"));
            
            if(rs.getTimestamp("activitystartdate") != null){
                user.setActivityStartDate(rs.getTimestamp("activitystartdate").toLocalDateTime());
            }
            if(rs.getTimestamp("activitystopdate") != null ){
                user.setActivityStopDate(rs.getTimestamp("activitystopdate").toLocalDateTime());
            }
            user.setIsEnforcementOfficial(rs.getBoolean("enforcementofficial"));
            user.setBadgeNumber(rs.getString("badgenumber"));
            user.setOriNumber(rs.getString("orinumber"));
            
            user.setPerson(pi.getPerson(rs.getInt("personlink")));
            
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
     * Deprecated from auth version that didn't use Glassfish's authorization scheme     * 
     * @param userID
     * @return
     * @throws IntegrationException 
     */
    public User getUser(int userID) throws IntegrationException{
        System.out.println("UserIntegrator.getUser | userid: " + userID);
        Connection con = getPostgresCon();
        ResultSet rs = null;
        User newUser = new User();
        // broken query
        String query = "SELECT userid, userrole, username, muni_municode, notes, activitystartdate, \n" +
                        "       activitystopdate, accesspermitted, enforcementofficial, badgenumber, \n" +
                        "       orinumber, personlink "
                + "FROM login where userid = ?;";
        
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
     * Primary access point for the entire User system: Called during SessionInitializer actions
     * to create a new session
     * @param userName
     * @return the Fully-baked user object ready to be passed to and fro
     * @throws IntegrationException 
     */   
    public User getUser(String userName) throws IntegrationException{
        
        System.out.println("UserIntegrator.getUser");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        User newUser = null;
        String query = "SELECT userid FROM login WHERE username = ?;";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, userName);
            rs = stmt.executeQuery();
            while(rs.next()){
                newUser = getUser(rs.getInt("userid"));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("userint.getuser", ex);
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
     * @return A list of Municipalities to which the user should be granted data-related
     * access within their user type domain
     * @throws IntegrationException 
     */
    public List<Municipality> getUserAuthMunis(int uid) throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT muni_municode FROM loginmuni WHERE userid = ?;";
        List<Municipality>muniList = new ArrayList<>();
        PreparedStatement stmt = null;
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, uid);
            rs = stmt.executeQuery();
            while(rs.next()){
                muniList.add(mi.getMuni(rs.getInt("muni_municode")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("UserIntegrator.getUserAuthMunis | Error getting user-auth-munis", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return muniList;
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
     * For use by system administrators to manage user data
     * @return
     * @throws IntegrationException 
     */
    public List getCompleteActiveUserList() throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        ArrayList<User> userList = new ArrayList();
        
        String query =  "SELECT userid FROM login WHERE accesspermitted = TRUE;";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
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
     * @return
     * @throws IntegrationException 
     */
    public List<User> getActiveCodeOfficerList() throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        List<User> userList = new ArrayList<>();
        
        String query =  "SELECT userid FROM login WHERE enforcementofficial=TRUE AND accesspermitted=TRUE;";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
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
                stmt.setInt(1, p.getPropertyID());
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
