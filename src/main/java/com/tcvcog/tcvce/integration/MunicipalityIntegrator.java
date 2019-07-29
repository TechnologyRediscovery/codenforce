/*
 * Copyright (C) 2018 Turtle Creek Valley
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
import com.tcvcog.tcvce.entities.MuniProfile;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.MunicipalityComplete;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Eric C. Darsow
 */
public class MunicipalityIntegrator extends BackingBeanUtils implements Serializable {

    
    /**
     * Creates a new instance of MunicipalityIntegrator
     */
    public MunicipalityIntegrator() {
    }
    
  
    
    public Municipality getMuni(int muniCode) throws IntegrationException, SQLException{
          PreparedStatement stmt = null;
        Municipality muni = null;
        Connection con = null;
        // note that muniCode is not returned in this query since it is specified in the WHERE
        String query =  "    SELECT municode, muniname, address_street, address_city, address_state, \n" +
                        "       address_zip, phone, fax, email, population, activeinprogram, \n" +
                        "       defaultcodeset, occpermitissuingsource_sourceid, novprintstyle_styleid, \n" +
                        "       profile_profileid, enablecodeenforcement, enableoccupancy, enablepublicceactionreqsub, \n" +
                        "       enablepublicceactionreqinfo, enablepublicoccpermitapp, enablepublicoccinspectodo, \n" +
                        "       munimanager_userid, office_propertyid, notes, lastupdatedts, \n" +
                        "       lastupdated_userid, primarystaffcontact_userid\n" +
                        "  FROM public.municipality WHERE municode=?;";
        ResultSet rs = null;
 
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, muniCode);
            rs = stmt.executeQuery();
            while(rs.next()){
                muni = generateMuni(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println("MunicipalityIntegrator.getMuni | " + ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.getMuni", ex);
        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return muni;
        
    }
    
    public MunicipalityComplete getMuniComplete(int muniCode) throws IntegrationException{
        PreparedStatement stmt = null;
        MunicipalityComplete muniComplete = null;
        Connection con = null;
        // note that muniCode is not returned in this query since it is specified in the WHERE
        String query =  "    SELECT municode, muniname, address_street, address_city, address_state, \n" +
                        "       address_zip, phone, fax, email, population, activeinprogram, \n" +
                        "       defaultcodeset, occpermitissuingsource_sourceid, novprintstyle_styleid, \n" +
                        "       profile_profileid, enablecodeenforcement, enableoccupancy, enablepublicceactionreqsub, \n" +
                        "       enablepublicceactionreqinfo, enablepublicoccpermitapp, enablepublicoccinspectodo, \n" +
                        "       munimanager_userid, office_propertyid, notes, lastupdatedts, \n" +
                        "       lastupdated_userid, primarystaffcontact_userid\n" +
                        "  FROM public.municipality WHERE municode=?;";
        ResultSet rs = null;
 
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, muniCode);
            rs = stmt.executeQuery();
            while(rs.next()){
                muniComplete = generateMuniComplete(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println("MunicipalityIntegrator.getMuni | " + ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.getMuni", ex);
        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return muniComplete;
    }
    
    
    private Municipality generateMuni(ResultSet rs) throws SQLException{
        Municipality muni = new Municipality();
        
        muni.setMuniCode(rs.getInt("municode"));
        muni.setMuniName(rs.getString("muniname"));
        
        
        return muni;
    }
    
    private MunicipalityComplete generateMuniComplete(ResultSet rs) throws SQLException, IntegrationException{
        SystemIntegrator si = getSystemIntegrator();
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        UserIntegrator ui = getUserIntegrator();
        CodeIntegrator ci = getCodeIntegrator();
        PropertyIntegrator pi = getPropertyIntegrator();
        
        MunicipalityComplete muni = new MunicipalityComplete(generateMuni(rs));
        
        muni.setAddress_street(rs.getString("address_street"));
        muni.setAddress_city(rs.getString("address_city"));
        muni.setAddress_state(rs.getString("address_state"));
        
        muni.setAddress_zip(rs.getString("address_zip"));
        muni.setPhone(rs.getString("phone"));
        muni.setFax(rs.getString("fax"));
        muni.setEmail(rs.getString("email"));
        muni.setPopulation(rs.getInt("population"));
        muni.setActiveInProgram(rs.getBoolean("activeinprogram"));             
        
        muni.setCodeSet(ci.getCodeSetBySetID(rs.getInt("defaultcodeset")));
        muni.setIssuingCodeSource(ci.getCodeSource(rs.getInt("occpermitissuingsource_sourceid")));
        muni.setDefaultNOVStyleID(rs.getInt("novprintstyle_styleid"));
        
        muni.setProfile(getMuniProfile(rs.getInt("profile_profileid")));
        muni.setEnableCodeEnforcement(rs.getBoolean("enablecodeenforcement"));
        muni.setEnableOccupancy(rs.getBoolean("enableoccupancy"));
        muni.setEnablePublicCEActionRequestSubmissions(rs.getBoolean("enablepublicceactionreqsub"));
        
        muni.setEnablePublicCEActionRequestInfo(rs.getBoolean("enablepublicceactionreqinfo"));
        muni.setEnablePublicOccPermitApp(rs.getBoolean("enablepublicoccpermitapp"));
        muni.setEnablePublicOccInspectionTODOs(rs.getBoolean("enablepublicoccinspectodo"));

        muni.setMuniManager(ui.getUser(rs.getInt("munimanager_userid")));
        muni.setMuniOfficePropertyId(rs.getInt("office_propertyid"));
        muni.setNotes(rs.getString("notes"));
        
        if(rs.getTimestamp("lastupdatedts") != null){
            muni.setLastUpdatedTS(rs.getTimestamp("lastupdatedts").toLocalDateTime());
        }
        
        muni.setLastUpdaetdBy(ui.getUser(rs.getInt("lastupdated_userid")));
        muni.setPrimaryStaffContact(ui.getUser(rs.getInt("primarystaffcontact_userid")));
        
        muni.setCodeOfficers(ui.getActiveCodeOfficerList(muni.getMuniCode()));
        muni.setCourtEntities(cei.getCourtEntityList(muni.getMuniCode()));
        
        return muni;
    }
    
    private MuniProfile getMuniProfile(int profileID) throws IntegrationException{
        MuniProfile mp = null;
        PreparedStatement stmt = null;
        Connection con = null;
        // note that muniCode is not returned in this query since it is specified in the WHERE
        String query =  "SELECT profileid, title, description, lastupdatedts, lastupdatedby_userid, \n" +
                        "       notes, continuousoccupancybufferdays, minimumuserranktodeclarerentalintent\n" +
                        "  FROM public.muniprofile WHERE profileid=?;";
        ResultSet rs = null;
 
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, profileID);
            rs = stmt.executeQuery();
            while(rs.next()){
                mp = generateMuniProfile(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println("MunicipalityIntegrator.getMuni | " + ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.getMuni", ex);
        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return mp;
    }
    
    private MuniProfile generateMuniProfile(ResultSet rs) 
            throws SQLException, IntegrationException{
        MuniProfile mp = new MuniProfile();
        EventIntegrator ei = getEventIntegrator();
        UserIntegrator ui = getUserIntegrator();
        OccupancyIntegrator oi = getOccupancyIntegrator();
        
        mp.setProfileID(rs.getInt("profileid"));
        mp.setTitle(rs.getString("title"));
        mp.setDescription(rs.getString("description"));
        mp.setLastupdatedTS(rs.getTimestamp("lastupdatedts").toLocalDateTime());
        mp.setLastupdatedUser(ui.getUser(rs.getInt("lastupdatedby_userid")));
        mp.setNotes(rs.getString("notes"));
        mp.setContinuousoccupancybufferdays(rs.getInt("continuousoccupancybufferdays"));
        mp.setMinimumuserranktodeclarerentalintent(rs.getInt("minimumuserranktodeclarerentalintent"));
        
        mp.setEventRuleSetCE(ei.getEventRuleSet(rs.getInt("profileid")));
        mp.setOccPeriodTypeList(oi.getOccPeriodTypeList(rs.getInt("profileid")));
        if(mp.getOccPeriodTypeList() == null){
            mp.setOccPeriodTypeList(new ArrayList<OccPeriodType>());
        }
        
        return mp;
        
    }
    
    public List<Municipality> getMuniList() throws IntegrationException{
        List<Municipality> mList = new ArrayList<>();
        String query = "SELECT municode FROM municipality;";
        ResultSet rs = null;
        Statement stmt = null;
        Connection con = getPostgresCon();
 
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while(rs.next()){
                mList.add(getMuni(rs.getInt("municode")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.getMuniList", ex);

        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return mList;
        
    }
   
    
    public void updateMuniComplete(MunicipalityComplete muni) throws IntegrationException{
        
        Connection con = null;
        String query =  "UPDATE public.municipality\n" +
                        "   SET muniname=?, address_street=?, address_city=?, address_state=?, \n" +
                        "       address_zip=?, phone=?, fax=?, email=?, population=?, activeinprogram=?, \n" +
                        "       defaultcodeset=?, occpermitissuingsource_sourceid=?, novprintstyle_styleid=?, \n" +
                        "       profile_profileid=?, enablecodeenforcement=?, enableoccupancy=?, \n" +
                        "       enablepublicceactionreqsub=?, enablepublicceactionreqinfo=?, \n" +
                        "       enablepublicoccpermitapp=?, enablepublicoccinspectodo=?, munimanager_userid=?, \n" +
                        "       office_propertyid=?, notes=?, lastupdatedts=now(), lastupdated_userid=?, \n" +
                        "       primarystaffcontact_userid=?\n" +
                        " WHERE municode=?;";
        ResultSet rs = null;
        PreparedStatement stmt = null;
 
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setString(1, muni.getMuniName());
            stmt.setString(2, muni.getAddress_street());
            stmt.setString(3, muni.getAddress_city());
            stmt.setString(4, muni.getAddress_state());
            
            stmt.setString(5, muni.getAddress_zip());
            stmt.setString(6, muni.getPhone());
            stmt.setString(7, muni.getFax());
            stmt.setString(8, muni.getEmail());
            stmt.setInt(9, muni.getPopulation());
            stmt.setBoolean(10, muni.isActiveInProgram());
            
            stmt.setInt(11, muni.getCodeSet().getCodeSetID());
            stmt.setInt(12, muni.getIssuingCodeSource().getSourceID());
            stmt.setInt(13, muni.getDefaultNOVStyleID());
            
            stmt.setInt(14, muni.getProfile().getProfileID());
            stmt.setBoolean(15, muni.isEnableCodeEnforcement());
            stmt.setBoolean(16, muni.isEnableOccupancy());
            
            stmt.setBoolean(17, muni.isEnablePublicOccPermitApp());
            stmt.setBoolean(18, muni.isEnablePublicOccInspectionTODOs());
            stmt.setInt(19, muni.getMuniManager().getUserID());
            
            stmt.setInt(20, muni.getMuniOfficePropertyId());
            stmt.setString(21, muni.getNotes());
            // lastupdatedts=now()
            stmt.setInt(22, muni.getLastUpdaetdBy().getUserID());
            
            stmt.setInt(23, muni.getPrimaryStaffContact().getUserID());
            
            stmt.setInt(24, muni.getMuniCode());
            stmt.executeUpdate();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.updateMuni", ex);

        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    public Map<Integer, String> getMunicipalityMap() throws IntegrationException{
        Map<Integer, String> muniMap = null;
            
        muniMap = new HashMap<>();

        Connection con = getPostgresCon();

        String query = "SELECT muniCode, muniName FROM municipality;";
        ResultSet rs = null;
        Statement stmt = null;

        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            
            while(rs.next()){
                muniMap.put(rs.getInt("muniCode"),rs.getString("muniName"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.generateCompleteMuniNameIDMap", ex);

        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return muniMap;
    }
    
    
    public HashMap<String, Integer> generateCompleteMuniNameIDMap() throws IntegrationException{
        HashMap<String, Integer> muniMap = new HashMap<>();
       
        Connection con = getPostgresCon();
        
        String query = "SELECT muniCode, muniName FROM municipality;";
        ResultSet rs = null;
        Statement stmt = null;
 
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while(rs.next()){
                muniMap.put(rs.getString("muniName"), rs.getInt("muniCode"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.generateCompleteMuniNameIDMap", ex);

        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return muniMap;
    }
    
      /**
     * @return the municipalityMap
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public HashMap<String, Integer> getMunicipalityStringIDMap() throws IntegrationException{
        return generateCompleteMuniNameIDMap();
    }

    /**
     * Users are permitted access to a set of municipalities which are all dumped
     * into a List by this method during the user lookup process.
     * @param uid
     * @return A list of Municipalities to which the user should be granted data-related
     * access within their user type domain
     * @throws IntegrationException
     */
    public List<Municipality> getUserAuthMunis(int uid) throws IntegrationException {
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT DISTINCT muni_municode \n" +
                        "FROM munilogin\n" +
                        "WHERE userid = ? \n" +
                        "AND recorddeactivatedts IS NULL\n" +
                        "AND accessgranteddatestart < now()\n" +
                        "AND accessgranteddatestop > now();";
        List<Municipality> muniList = new ArrayList<>();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, uid);
            rs = stmt.executeQuery();
            while (rs.next()) {
                muniList.add(getMuni(rs.getInt("muni_municode")));
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("UserIntegrator.getUserAuthMunis | Error getting user-auth-munis", ex);
        } finally {
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        return muniList;
    }
    
}
