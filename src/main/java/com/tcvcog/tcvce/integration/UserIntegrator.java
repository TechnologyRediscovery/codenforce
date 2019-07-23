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
import com.tcvcog.tcvce.entities.UserAccessRecord;
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
        String query =  "   SELECT userid, username, password, notes, "
                        + "     personlink \n" +
                        "   FROM public.login WHERE userid = ?;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, userID);
            rs = stmt.executeQuery();
            while(rs.next()){
                newUser = generateUser(rs, getUserDefaultMunicipality(rs.getInt("userid")));
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
    private User generateUser(ResultSet rs, Municipality muni) throws IntegrationException{
        User user = new User();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        PersonIntegrator pi = getPersonIntegrator();
        try {
            user.setUserID(rs.getInt("userid"));
            user.setUsername(rs.getString("username"));
            user.setNotes(rs.getString("notes"));
            
            user.setPerson(pi.getPerson(rs.getInt("personlink")));
            
            user.setAccessRecord(getUserAccessRecord(user, muni));
            
        } catch (SQLException ex) {
            throw new IntegrationException("Cannot create user", ex);
        }
        
        return user;
    }
    
    
    private UserAccessRecord getUserAccessRecord(User u, Municipality muni) throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        UserAccessRecord rec = null;
        // broken query
        String query = "SELECT muni_municode, userid, defaultmuni, accessgranteddatestart, accessgranteddatestop, \n" +
                        "       codeofficerstartdate, codeofficerstopdate, staffstartdate, staffstopdate, \n" +
                        "       sysadminstartdate, sysadminstopdate, supportstartdate, supportstopdate, \n" +
                        "       codeofficerassignmentorder, staffassignmentorder, sysadminassignmentorder, \n" +
                        "       supportassignmentorder, bypasscodeofficerassignmentorder, bypassstaffassignmentorder, \n" +
                        "       bypasssysadminassignmentorder, bypasssupportassignmentorder, \n" +
                        "       recorddeactivatedts, userrole, muniloginrecordid, recordcreatedts,  badgenumber, orinumber \n" +
                        "   FROM public.munilogin "
                + "         WHERE userid=? "
                + "             AND muni_municode=? "
                + "             AND recorddeactivatedts IS NULL "
                + "             AND accessgranteddatestart < now() "
                + "             AND accessgranteddatestop > now() "
                + "             ORDER BY recordcreatedts DESC;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, u.getUserID());
            stmt.setInt(2, muni.getMuniCode());
            rs = stmt.executeQuery();
            while(rs.next()){
                rec = generateUserAccessRecord(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error getting user access record", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return rec;
    }
    
    private UserAccessRecord generateUserAccessRecord(ResultSet rs) throws SQLException{
        UserAccessRecord rec = new UserAccessRecord();
        
        rec.setMuni_municode(rs.getInt("muni_municode"));
        rec.setMuni_municode(rs.getInt("userid"));
        rec.setDefaultmuni(rs.getBoolean("defaultmuni"));
        rec.setAccessgranteddatestart(rs.getTimestamp("accessgranteddatestart").toLocalDateTime());
        rec.setAccessgranteddatestop(rs.getTimestamp("accessgranteddatestop").toLocalDateTime());
        
        rec.setCodeofficerstartdate(rs.getTimestamp("codeofficerstartdate").toLocalDateTime());
        rec.setCodeofficerstopdate(rs.getTimestamp("codeofficerstopdate").toLocalDateTime());
        rec.setStaffstartdate(rs.getTimestamp("staffstartdate").toLocalDateTime());
        rec.setStaffstopdate(rs.getTimestamp("staffstopdate").toLocalDateTime());
        
        rec.setSysadminstartdate(rs.getTimestamp("sysadminstartdate").toLocalDateTime());
        rec.setSysadminstopdate(rs.getTimestamp("sysadminstopdate").toLocalDateTime());
        rec.setSupportstartdate(rs.getTimestamp("supportstartdate").toLocalDateTime());
        rec.setSupportstopdate(rs.getTimestamp("supportstopdate").toLocalDateTime());
        
        rec.setMuni_municode(rs.getInt("codeofficerassignmentorder"));
        rec.setMuni_municode(rs.getInt("staffassignmentorder"));
        rec.setMuni_municode(rs.getInt("sysadminassignmentorder"));
        
        rec.setMuni_municode(rs.getInt("supportassignmentorder"));
        rec.setMuni_municode(rs.getInt("bypasscodeofficerassignmentorder"));
        
        rec.setMuni_municode(rs.getInt("bypassstaffassignmentorder"));
        rec.setMuni_municode(rs.getInt("bypasssysadminassignmentorder"));
        rec.setMuni_municode(rs.getInt("bypasssupportassignmentorder"));
        
        rec.setRecordcreatedts(rs.getTimestamp("recorddeactivatedts").toLocalDateTime());
        rec.setRole(RoleType.valueOf("role"));
        rec.setMuniloginrecordid(rs.getInt("muniloginrecordid"));
        rec.setRecordcreatedts(rs.getTimestamp("recordcreatedts").toLocalDateTime());
        rec.setOrinumber(rs.getString("orinumber"));
        rec.setBadgenumber(rs.getString("badgenumber"));
        
        return rec;
    }
    
    
    
    public int insertUser(User userToInsert) throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query =  "INSERT INTO    public.login(\n" +
                        "               userid, username, password, notes, "
                    +   "               personlink)\n" +
                        "    VALUES     (DEFAULT, ?, ?, ?, "
                + "                     ?, ?, ?);";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, userToInsert.getUsername());
            StringBuilder sb = new StringBuilder();
            sb.append(generateControlCodeFromTime());
            sb.append(generateControlCodeFromTime());
            stmt.setString(2, sb.toString());
            stmt.setString(3, userToInsert.getNotes());
            if(userToInsert.getPerson() == null){
                stmt.setInt(10, userToInsert.getPersonID());
            } else {
                stmt.setInt(10, userToInsert.getPerson().getPersonID());
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
    
    public Municipality getUserDefaultMunicipality(int userID) throws IntegrationException{
        Connection con = getPostgresCon();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        ResultSet rs = null;
        String query = "SELECT muni_municode FROM public.loginmuni "
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
        return m;
    }
    
    /**
     * @deprecated 
     * @param u
     * @param m
     * @return
     * @throws IntegrationException 
     */
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
        
        String query =  "UPDATE public.login\n" +
                        "   SET username=?, notes=?, badgenumber=?, orinumber=?, \n" +
                        "       personlink=?\n" +
                        " WHERE userid=?;";
        
        PreparedStatement stmt = null;
        	
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, userToUpdate.getUsername());
            stmt.setString(2, userToUpdate.getNotes());
            stmt.setString(3, userToUpdate.getBadgeNumber());
            stmt.setString(4, userToUpdate.getOriNumber());
            
            stmt.setInt(5, userToUpdate.getPerson().getPersonID());
            
            stmt.setInt(6, userToUpdate.getUserID());
            
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting new person", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
   
    // program later--we generally dont want to allow folks to delete
    // users from the web client since they'll be integrated into tables
    // etc. They should mark the user as inactive and with an expiry date that
    // is in the past
    public void deleteUser(User userToDelete){
        
        // no guts for me!
        
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
     * For attaching event requests to default code officers by muni
     * @return
     * @throws IntegrationException 
     */
    public List<User> getActiveCodeOfficerList(int muniCode) throws IntegrationException{
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
