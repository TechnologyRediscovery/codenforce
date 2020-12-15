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
import com.tcvcog.tcvce.coordinators.MunicipalityCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.MuniProfile;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.MunicipalityDataHeavy;
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
 * @author ellen bascomb of apt 31y
 */
public class MunicipalityIntegrator extends BackingBeanUtils implements Serializable {

    
    /**
     * Creates a new instance of MunicipalityIntegrator
     */
    public MunicipalityIntegrator() {
    }
    
  
    
    public Municipality getMuni(int muniCode) throws IntegrationException {
          PreparedStatement stmt = null;
        Municipality muni = null;
        Connection con = null;
        // note that muniCode is not returned in this query since it is specified in the WHERE
        String query =  " SELECT municode, muniname FROM public.municipality WHERE municode=?;";
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
    
    public MunicipalityDataHeavy getMunDataHeavy(int muniCode) throws IntegrationException, AuthorizationException{
        PreparedStatement stmt = null;
        MunicipalityDataHeavy muniComplete = null;
        Connection con = null;
        // note that muniCode is not returned in this query since it is specified in the WHERE
        String query =  "    SELECT municode, muniname, address_street, address_city, address_state, \n" +
                        "       address_zip, phone, fax, email, population, activeinprogram, \n" +
                        "       defaultcodeset, occpermitissuingsource_sourceid, novprintstyle_styleid, \n" +
                        "       profile_profileid, enablecodeenforcement, enableoccupancy, enablepublicceactionreqsub, \n" +
                        "       enablepublicceactionreqinfo, enablepublicoccpermitapp, enablepublicoccinspectodo, \n" +
                        "       munimanager_userid, office_propertyid, notes, lastupdatedts, \n" +
                        "       lastupdated_userid, primarystaffcontact_userid, defaultoccperiod    \n" +
                        "  FROM public.municipality"
                      + "  WHERE municode=? AND activeinprogram = true;";
        ResultSet rs = null;
 
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, muniCode);
            rs = stmt.executeQuery();
            while(rs.next()){
                muniComplete = generateMuniDataHeavy(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println("MunicipalityIntegrator.getMuni | " + ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.getMuni", ex);
        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { System.out.println("getMuni | " + e.toString());} }
        } // close finally
        
        return muniComplete;
    }
    
    
    private Municipality generateMuni(ResultSet rs) throws SQLException{
        Municipality muni = new Municipality();
        
        muni.setMuniCode(rs.getInt("municode"));
        muni.setMuniName(rs.getString("muniname"));
        
        
        return muni;
    }
    
    private MunicipalityDataHeavy generateMuniDataHeavy(ResultSet rs) throws SQLException, IntegrationException, AuthorizationException{
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        UserIntegrator ui = getUserIntegrator();
        CodeIntegrator ci = getCodeIntegrator();
        
        MunicipalityDataHeavy mdh = new MunicipalityDataHeavy(generateMuni(rs));
        
        mdh.setAddress_street(rs.getString("address_street"));
        mdh.setAddress_city(rs.getString("address_city"));
        mdh.setAddress_state(rs.getString("address_state"));
        
        mdh.setAddress_zip(rs.getString("address_zip"));
        mdh.setPhone(rs.getString("phone"));
        mdh.setFax(rs.getString("fax"));
        mdh.setEmail(rs.getString("email"));
        mdh.setPopulation(rs.getInt("population"));
        mdh.setActiveInProgram(rs.getBoolean("activeinprogram"));             
        
        mdh.setCodeSet(ci.getCodeSetBySetID(rs.getInt("defaultcodeset")));
        mdh.setIssuingCodeSource(ci.getCodeSource(rs.getInt("occpermitissuingsource_sourceid")));
        mdh.setDefaultNOVStyleID(rs.getInt("novprintstyle_styleid"));
        
        mdh.setProfile(getMuniProfile(rs.getInt("profile_profileid")));
        mdh.setEnableCodeEnforcement(rs.getBoolean("enablecodeenforcement"));
        mdh.setEnableOccupancy(rs.getBoolean("enableoccupancy"));
        mdh.setEnablePublicCEActionRequestSubmissions(rs.getBoolean("enablepublicceactionreqsub"));
        
        mdh.setEnablePublicCEActionRequestInfo(rs.getBoolean("enablepublicceactionreqinfo"));
        mdh.setEnablePublicOccPermitApp(rs.getBoolean("enablepublicoccpermitapp"));
        mdh.setEnablePublicOccInspectionTODOs(rs.getBoolean("enablepublicoccinspectodo"));

        mdh.setMuniManager(ui.getUser(rs.getInt("munimanager_userid")));
        mdh.setMuniOfficePropertyId(rs.getInt("office_propertyid"));
        mdh.setDefaultOccPeriodID(rs.getInt("defaultoccperiod"));
        
        
        mdh.setNotes(rs.getString("notes"));
        
        if(rs.getTimestamp("lastupdatedts") != null){
            mdh.setLastUpdatedTS(rs.getTimestamp("lastupdatedts").toLocalDateTime());
        }
        
        mdh.setLastUpdatedBy(ui.getUser(rs.getInt("lastupdated_userid")));
        mdh.setPrimaryStaffContact(ui.getUser(rs.getInt("primarystaffcontact_userid")));
        
        
        return mdh;
    }
    
    private MuniProfile getMuniProfile(int profileID) throws IntegrationException{
        MuniProfile mp = null;
        PreparedStatement stmt = null;
        Connection con = null;
        // note that muniCode is not returned in this query since it is specified in the WHERE
        String query =  "SELECT profileid, title, description, lastupdatedts, lastupdatedby_userid, \n" +
                        "       notes, continuousoccupancybufferdays, minimumuserranktodeclarerentalintent, novfollowupdefaultdays \n" +
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
        WorkflowIntegrator wi = getWorkflowIntegrator();
        
        mp.setProfileID(rs.getInt("profileid"));
        mp.setTitle(rs.getString("title"));
        mp.setDescription(rs.getString("description"));
        mp.setLastupdatedTS(rs.getTimestamp("lastupdatedts").toLocalDateTime());
        mp.setLastupdatedUser(ui.getUser(rs.getInt("lastupdatedby_userid")));
        mp.setNotes(rs.getString("notes"));
        mp.setContinuousoccupancybufferdays(rs.getInt("continuousoccupancybufferdays"));
        mp.setMinimumuserranktodeclarerentalintent(rs.getInt("minimumuserranktodeclarerentalintent"));
        mp.setNovDefaultDaysForFollowup(rs.getInt("novfollowupdefaultdays"));
        
        mp.setEventRuleSetCE(wi.rules_getEventRuleSet(rs.getInt("profileid")));
        mp.setOccPeriodTypeList(oi.getOccPeriodTypeList(rs.getInt("profileid")));
        if(mp.getOccPeriodTypeList() == null){
            mp.setOccPeriodTypeList(new ArrayList<OccPeriodType>());
        }
        
        return mp;
        
    }
    
    public List<Municipality> getMuniList() throws IntegrationException{
        List<Municipality> mList = new ArrayList<>();
        String query = "SELECT municode FROM municipality WHERE activeinprogram = true;";
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
           if (con != null) { try { con.close(); } catch (SQLException e) { System.out.println("MunicipalityIntegrator.getMuniList | " + e.toString());} }
        } // close finally
        
        return mList;
        
    }
   
    
    public void updateMuniDataHeavy(MunicipalityDataHeavy muni) throws IntegrationException{
        
        Connection con = null;
        String query =  "UPDATE public.municipality\n" +
                        "   SET muniname=?, address_street=?, address_city=?, address_state=?, \n" +
                        "       address_zip=?, phone=?, fax=?, email=?, population=?, activeinprogram=?, \n" +
                        "       defaultcodeset=?, occpermitissuingsource_sourceid=?, novprintstyle_styleid=?, \n" +
                        "       profile_profileid=?, enablecodeenforcement=?, enableoccupancy=?, \n" +
                        "       enablepublicceactionreqsub=?, enablepublicceactionreqinfo=?, \n" +
                        "       enablepublicoccpermitapp=?, enablepublicoccinspectodo=?, munimanager_userid=?, \n" +
                        "       office_propertyid=?, notes=?, lastupdatedts=now(), lastupdated_userid=?, \n" +
                        "       primarystaffcontact_userid=?, defaultoccperiod=?\n" +
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

            stmt.setBoolean(17, muni.isEnablePublicCEActionRequestSubmissions());
            stmt.setBoolean(18, muni.isEnablePublicCEActionRequestInfo());

            stmt.setBoolean(19, muni.isEnablePublicOccPermitApp());
            stmt.setBoolean(20, muni.isEnablePublicOccInspectionTODOs());
            stmt.setInt(21, muni.getMuniManager().getUserID());

            stmt.setInt(22, muni.getMuniOfficePropertyId());
            
            stmt.setString(23, muni.getNotes());
            // lastupdatedts=now()
            stmt.setInt(24, muni.getLastUpdatedBy().getUserID());

            stmt.setInt(25, muni.getPrimaryStaffContact().getUserID());
            
            stmt.setInt(26, muni.getDefaultOccPeriodID());
            
            stmt.setInt(27, muni.getMuniCode());

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
     * @deprecated 
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
    
    //xiaohong add
    public ArrayList<MuniProfile> getMuniProfileList() throws IntegrationException {

        String query = "SELECT profileid FROM public.muniprofile;";

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<MuniProfile> muniProfileList = new ArrayList<>();
        MuniProfile muniProfile;

        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                muniProfile = getMuniProfile(rs.getInt("profileid"));
                
                if (muniProfile != null) {
                    muniProfileList.add(muniProfile);
                }

            }

        } catch (SQLException ex) {
            System.out.println("MunicipalityIntegrator.getMuni | " + ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.getMuni", ex);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {/* ignored */ }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally

        return muniProfileList;
    }
    
    //xiaohong add
    public void insertMuniDataHeavy(MunicipalityDataHeavy muni) {

        String query = "INSERT INTO public.municipality(\n"
                + "            municode, muniname, address_street, address_city, address_state, \n"
                + "            address_zip, phone, fax, email, population, activeinprogram, \n"
                + "            defaultcodeset, occpermitissuingsource_sourceid, novprintstyle_styleid, \n"
                + "            profile_profileid, enablecodeenforcement, enableoccupancy, enablepublicceactionreqsub, \n"
                + "            enablepublicceactionreqinfo, enablepublicoccpermitapp, enablepublicoccinspectodo, \n"
                + "            munimanager_userid, office_propertyid, notes, lastupdatedts, \n"
                + "            lastupdated_userid, primarystaffcontact_userid, defaultoccperiod)\n"
                + "    VALUES (?, ?, ?, ?, ?, \n"
                + "            ?, ?, ?, ?, ?, ?, \n"
                + "            ?, ?, ?, \n"
                + "            ?, ?, ?, ?, \n"
                + "            ?, ?, ?, \n"
                + "            ?, ?, ?, now(), \n"
                + "            ?, ?, ?);";

        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, muni.getMuniCode());
            stmt.setString(2, muni.getMuniName());
            stmt.setString(3, muni.getAddress_street());
            stmt.setString(4, muni.getAddress_city());
            stmt.setString(5, muni.getAddress_state());

            stmt.setString(6, muni.getAddress_zip());
            stmt.setString(7, muni.getPhone());
            stmt.setString(8, muni.getFax());
            stmt.setString(9, muni.getEmail());
            stmt.setInt(10, muni.getPopulation());
            stmt.setBoolean(11, muni.isActiveInProgram());

            stmt.setInt(12, muni.getCodeSet().getCodeSetID());
            stmt.setInt(13, muni.getIssuingCodeSource().getSourceID());
            stmt.setInt(14, muni.getDefaultNOVStyleID());

            stmt.setInt(15, muni.getProfile().getProfileID());
            stmt.setBoolean(16, muni.isEnableCodeEnforcement());
            stmt.setBoolean(17, muni.isEnableOccupancy());
            stmt.setBoolean(18, muni.isEnablePublicCEActionRequestSubmissions());

            stmt.setBoolean(19, muni.isEnablePublicCEActionRequestInfo());
            stmt.setBoolean(20, muni.isEnablePublicOccPermitApp());
            stmt.setBoolean(21, muni.isEnablePublicOccInspectionTODOs());

            stmt.setInt(22, muni.getMuniManager().getUserID());
            stmt.setInt(23, muni.getMuniOfficePropertyId());
            stmt.setString(24, muni.getNotes());

            // lastupdatedts=now()
            stmt.setInt(25, muni.getLastUpdatedBy().getUserID());
            stmt.setInt(26, muni.getPrimaryStaffContact().getUserID());
            stmt.setInt(27, muni.getDefaultOccPeriodID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());

        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {/* ignored */ }
            }

            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally
    }
    
     /**
     * Sets the defaultcodeset field on the muni table
     * @param set
     * @param muni
     * @throws BObStatusException 
     */
    public void mapCodeSetAsMuniDefault(CodeSet set, Municipality muni) throws BObStatusException, IntegrationException{
        
        if(set == null || muni == null){
            throw new BObStatusException("Cannot link set and muni with null set or muni!");
            
        }
        
        String query = "UPDATE municipality SET defaultcodeset=? WHERE municode=?; ";
        Connection con = null;
        PreparedStatement stmt = null;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, set.getCodeSetID());
            stmt.setInt(2, muni.getMuniCode());
            stmt.executeUpdate();
            
        } catch (SQLException ex) { 
             System.out.println(ex);
             throw new IntegrationException("Database exception making code set active in given muni", ex);
        } finally{
            if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        
        
        
    }
    
    
}
