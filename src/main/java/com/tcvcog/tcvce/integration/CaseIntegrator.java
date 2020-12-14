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
import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.coordinators.PaymentCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.coordinators.WorkflowCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CasePhaseEnum;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CitationStatus;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.CodeViolationDisplayable;
import com.tcvcog.tcvce.entities.Icon;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.TextBlock;
import com.tcvcog.tcvce.entities.search.SearchParamsCECase;
import com.tcvcog.tcvce.entities.PrintStyle;
import com.tcvcog.tcvce.entities.Blob;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneId;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.faces.application.FacesMessage;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class CaseIntegrator extends BackingBeanUtils implements Serializable{
    
    final String ACTIVE_FIELD = "cecase.active";
    /**
     * Creates a new instance of CaseIntegrator
     */
    public CaseIntegrator() {
    }
   
    
    /**
     * Single focal point of serach method for Code Enforcement case using a SearchParam
     * subclass. Outsiders will use runQueryCECase or runQueryCECase
     * @param params
     * @return a list of CECase IDs
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public List<Integer> searchForCECases(SearchParamsCECase params) throws IntegrationException, BObStatusException{
        SearchCoordinator sc = getSearchCoordinator();
        List<Integer> cseidlst = new ArrayList<>();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        params.appendSQL("SELECT DISTINCT caseid \n");
        params.appendSQL("FROM public.cecase \n");
        params.appendSQL("INNER JOIN public.property ON (cecase.property_propertyid = property.propertyid) \n");
        params.appendSQL("WHERE caseid IS NOT NULL ");
        
        // *******************************
        // **         BOb ID            **
        // *******************************
         if (!params.isBobID_ctl()) {

            //*******************************
            // **   MUNI,DATES,USER,ACTIVE  **
            // *******************************
            params = (SearchParamsCECase) sc.assembleBObSearchSQL_muniDatesUserActive(
                                                                params, 
                                                                SearchParamsCECase.MUNI_DBFIELD,
                                                                ACTIVE_FIELD);
            
            // *******************************
            // **       1:OPEN/CLOSED       **
            // *******************************
            if (params.isCaseOpen_ctl()) {
                if(params.isCaseOpen_val()){
                    params.appendSQL("AND closingdate IS NULL ");
                } else {
                    params.appendSQL("AND closingdate IS NOT NULL ");
                }
            }
            
            // *******************************
            // **      2:PROPERTY           **
            // *******************************
             if (params.isProperty_ctl()) {
                if(params.getProperty_val()!= null){
                    params.appendSQL("AND property_propertyid=? ");
                } else {
                    params.setProperty_ctl(false);
                    params.appendToParamLog("PROPERTY: no Property object; prop filter disabled");
                }
            }
            
            // *******************************
            // **     3:PROPERTY UNIT       **
            // *******************************
             if (params.isPropertyUnit_ctl()) {
                if(params.getPropertyUnit_val()!= null){
                    params.appendSQL("AND propertyunit_unitid=? ");
                } else {
                    params.setPropertyUnit_ctl(false);
                    params.appendToParamLog("PROPERTY UNIT: no PropertyUnit object; propunit filter disabled");
                }
            }
            
            // *******************************
            // **    4:PROP INFO CASES      **
            // *******************************
            if (params.isPropInfoCase_ctl()) {
                if (params.isPropInfoCase_val()) {
                    params.appendSQL("AND propertyinfocase = TRUE ");
                } else {
                    params.appendSQL("AND propertyinfocase = FALSE ");
                }
            }
            
            // **********************************
            // ** 5. PERSONS INFO CASES BOOL   **
            // **********************************
            if (params.isPersonInfoCase_ctl()) {
                if (params.isPersonInfoCase_val()) {
                    params.appendSQL("AND personinfocase_personid IS NOT NULL ");
                } else {
                    params.appendSQL("AND personinfocase_personid IS NULL ");
                }
            }
            
            // ********************************
            // ** 6.PERSONS INFO CASES ID    **
            // ********************************
            if (params.isPersonInfoCaseID_ctl()) {
                if(params.getPersonInfoCaseID_val() != null){
                    params.appendSQL("AND personinfocase_personid=? ");
                } else {
                    params.setPersonInfoCaseID_ctl(false);
                    params.appendToParamLog("PERSONINFO: no Person object; Person Info case filter disabled");
                }
            }
            
            // *******************************
            // **     7.BOb SOURCE          **
            // *******************************
             if (params.isSource_ctl()) {
                if(params.getSource_val() != null){
                    params.appendSQL("AND bobsource_sourceid=? ");
                } else {
                    params.setSource_ctl(false);
                    params.appendToParamLog("SOURCE: no BOb source object; source filter disabled");
                }
            }
           
            // *******************************
            // **        9. PACC            **
            // *******************************
             if (params.isPacc_ctl()) {
                if(params.isPacc_val()){
                    params.appendSQL("AND paccenabled = TRUE ");
                } else {
                    params.appendSQL("AND paccenabled = TRUE ");
                }
            }
            
            
        } else {
            params.appendSQL("caseid = ? "); // will be param 1 with ID search
        }

        int paramCounter = 0;
            
        try {
            stmt = con.prepareStatement(params.extractRawSQL());

            if (!params.isBobID_ctl()) {
                if (params.isMuni_ctl()) {
                     stmt.setInt(++paramCounter, params.getMuni_val().getMuniCode());
                }
                
                if(params.isDate_startEnd_ctl()){
                    stmt.setTimestamp(++paramCounter, params.getDateStart_val_sql());
                    stmt.setTimestamp(++paramCounter, params.getDateEnd_val_sql());
                 }
                
                if (params.isUser_ctl()) {
                   stmt.setInt(++paramCounter, params.getUser_val().getUserID());
                }
                
                if (params.isProperty_ctl()) {
                    stmt.setInt(++paramCounter, params.getProperty_val().getPropertyID());
                }
                
                if (params.isPropertyUnit_ctl()) {
                    stmt.setInt(++paramCounter, params.getPropertyUnit_val().getUnitID());
                }
                
                 if (params.isPersonInfoCaseID_ctl()) {
                    stmt.setInt(++paramCounter, params.getPersonInfoCaseID_val().getPersonID());
                }
                 
                if(params.isSource_ctl()){
                    stmt.setInt(++paramCounter, params.getSource_val().getSourceid());
                }

            } else {
                stmt.setInt(++paramCounter, params.getBobID_val());
            }
            
            rs = stmt.executeQuery();

            int counter = 0;
            int maxResults;
            if (params.isLimitResultCount_ctl()) {
                maxResults = params.getLimitResultCount_val();
            } else {
                maxResults = Integer.MAX_VALUE;
            }
            while (rs.next() && counter < maxResults) {
                cseidlst.add(rs.getInt("caseid"));
                counter++;
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot search for code enf cases, sorry!", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return cseidlst;
        
    }
    
    
    /**
     * Generates a CECaseDataHeavy without the big, fat lists
     * @param ceCaseID
     * @return
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public CECase getCECase(int ceCaseID) throws IntegrationException, BObStatusException{
        String query = "SELECT caseid, cecasepubliccc, property_propertyid, propertyunit_unitid, \n" +
                        "       login_userid, casename, originationdate, closingdate, creationtimestamp, \n" +
                        "       notes, paccenabled, allowuplinkaccess, propertyinfocase, personinfocase_personid, \n" +
                        "       bobsource_sourceid, active, lastupdatedby_userid, lastupdatedts \n" +
                        "  FROM public.cecase WHERE caseid = ?;";
        ResultSet rs = null;
        CaseCoordinator cc = getCaseCoordinator();
        PreparedStatement stmt = null;
        Connection con = null;
        CECase c = null;
        
        try {
            
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ceCaseID);
            //System.out.println("CaseIntegrator.cecase_getCECase| sql: " + stmt.toString());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                c = generateCECase(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get cecase by id", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return c;
    }
    
    /**
     * Gets a CECase according to a Property ID
     * @param propID
     * @return
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public List<CECase> getCECasesByProp(int propID) throws IntegrationException, BObStatusException{
        String query = "SELECT caseid, cecasepubliccc, property_propertyid, propertyunit_unitid, \n" +
                        "       login_userid, casename, originationdate, closingdate, creationtimestamp, \n" +
                        "       notes, paccenabled, allowuplinkaccess, propertyinfocase, personinfocase_personid, \n" +
                        "       bobsource_sourceid, active, lastupdatedby_userid, lastupdatedts \n" +
                        "  FROM public.cecase WHERE property_propertyid = ?;";
        ResultSet rs = null;
        CaseCoordinator cc = getCaseCoordinator();
        PreparedStatement stmt = null;
        Connection con = null;
        List<CECase> cList = new ArrayList<>();
        
        try {
            
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, propID);
            //System.out.println("CaseIntegrator.cecase_getCECase| sql: " + stmt.toString());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                cList.add(generateCECase(rs));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get cecase by id", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return cList;
    }
    
    /**
     * Internal generator for CECase objects
     * @param rs with all DB fields included
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
     private CECase generateCECase(ResultSet rs) throws SQLException, IntegrationException{
        UserCoordinator uc = getUserCoordinator();
        SystemIntegrator si = getSystemIntegrator();
        
        CECase cse = new CECase();

        cse.setCaseID(rs.getInt("caseid"));
        cse.setPublicControlCode(rs.getInt("cecasepubliccc"));

        cse.setPropertyID(rs.getInt("property_propertyid"));
        cse.setPropertyUnitID(rs.getInt("propertyunit_unitid"));
        
        cse.setCaseManager(uc.user_getUser(rs.getInt("login_userid")));

        cse.setCaseName(rs.getString("casename"));
        
        if(rs.getTimestamp("originationdate") != null){
            cse.setOriginationDate(rs.getTimestamp("originationdate")
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }

        if(rs.getTimestamp("closingdate") != null){
            cse.setClosingDate(rs.getTimestamp("closingdate")
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        if(rs.getTimestamp("creationtimestamp") != null){
            cse.setCreationTimestamp(rs.getTimestamp("creationtimestamp")
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        
        cse.setNotes(rs.getString("notes"));
        cse.setPaccEnabled(rs.getBoolean("paccenabled"));
        cse.setAllowForwardLinkedPublicAccess(rs.getBoolean("allowuplinkaccess"));
        cse.setPropertyInfoCase(rs.getBoolean("propertyinfocase"));
        cse.setPersonInfoPersonID(rs.getInt("personinfocase_personid"));
        
        if(rs.getInt("bobsource_sourceid") != 0){
            cse.setSource(si.getBOBSource(rs.getInt("bobsource_sourceid")));
        }
        cse.setActive(rs.getBoolean("active"));
        
        if(rs.getInt("lastupdatedby_userid") != 0){
            cse.setLastUpdatedBy(uc.user_getUser(rs.getInt("lastupdatedby_userid")));
        }
        
        if(rs.getTimestamp("lastupdatedts") != null){
            cse.setLastUpdatedTS(rs.getTimestamp("lastupdatedts").toLocalDateTime());
        }
        

        return cse;
    }
    
     /**
      * First gen search method to be deprecated in Beta
      * @param pacc
      * @return
      * @throws IntegrationException
      * @throws BObStatusException 
      */
    public List<CECase> getCECasesByPACC(int pacc) throws IntegrationException, BObStatusException{
        
        ArrayList<CECase> caseList = new ArrayList();
        String query = "SELECT caseid FROM public.cecase WHERE cecasepubliccc = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, pacc);
            System.out.println("CaseIntegrator.getCECasesByPacc | sql: " + stmt.toString());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                caseList.add(getCECase(rs.getInt("caseid")));
                
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot search for cases by PACC, sorry", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return caseList;
    }
    
  
    /**
     * Insertion point for CECaseDataHeavy objects; must be called by Coordinator who checks 
     * logic before sending to the DB. This method only copies from the passed in CECaseDataHeavy
     * into the SQL INSERT
     * 
     * @param ceCase
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public int insertNewCECase(CECase ceCase) throws IntegrationException, BObStatusException{
        String query = "INSERT INTO public.cecase(\n" +
                        "            caseid, cecasepubliccc, property_propertyid, propertyunit_unitid, \n" +
                        "            login_userid, casename, originationdate, closingdate, creationtimestamp, \n" +
                        "            notes, paccenabled, allowuplinkaccess, propertyinfocase, personinfocase_personid, \n" +
                        "            bobsource_sourceid, active, lastupdatedby_userid, lastupdatedts)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, now(), \n" +
                        "            ?, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, now());";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int insertedCaseID = 0;
        Connection con = null;
        
        try {
            
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ceCase.getPublicControlCode());
            stmt.setInt(2, ceCase.getPropertyID());
            if(ceCase.getPropertyUnitID() != 0) {
                stmt.setInt(3, ceCase.getPropertyUnitID());
            } else { 
                stmt.setNull(3, java.sql.Types.NULL); 
            }
            
            if(ceCase.getCaseManager() != null){
                stmt.setInt(4, ceCase.getCaseManager().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            stmt.setString(5, ceCase.getCaseName());
            
            if(ceCase.getOriginationDate() != null){
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(ceCase.getOriginationDate()));
            } else {
                stmt.setNull(6, java.sql.Types.NULL);                
            }
            // closing date
            stmt.setNull(7, java.sql.Types.NULL); 
            
            // creation TS set by database with now()
            
            stmt.setString(8, ceCase.getNotes());
            stmt.setBoolean(9, ceCase.isPaccEnabled());
            stmt.setBoolean(10, ceCase.isAllowForwardLinkedPublicAccess());
            stmt.setBoolean(11, ceCase.isPropertyInfoCase());
            if(ceCase.getPersonInfoPersonID() != 0){
                stmt.setInt(12, ceCase.getPersonInfoPersonID());
            } else {
                stmt.setNull(12, java.sql.Types.NULL); 
                
            }
            
            if(ceCase.getSource() != null){
                stmt.setInt(13, ceCase.getSource().getSourceid());
            } else {
                stmt.setNull(13, java.sql.Types.NULL); 
            }
            
            stmt.setBoolean(14, ceCase.isActive());
            
            if(ceCase.getLastUpdatedBy() != null){
                stmt.setInt(15, ceCase.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(15, java.sql.Types.NULL); 
            }
            
            stmt.execute();
            
            String retrievalQuery = "SELECT currval('cecase_caseID_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                insertedCaseID = rs.getInt(1);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Case Integrator: FATAL INSERT error; apologies.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return insertedCaseID;
        
    }
    
    /**
     * Updates the values in the CECaseDataHeavy in the DB but does NOT
     * edit the data in connected tables, namely CodeViolation, EventCnF, and Person
     * Use calls to other add methods in this class for adding additional
     * violations, events, and people to a CE case.
     * 
     * @param ceCase the case to updated, with updated member variables
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void updateCECaseMetadata(CECase ceCase) throws IntegrationException{
        String query =  "UPDATE public.cecase\n" +
                        "   SET cecasepubliccc=?, property_propertyid=?, propertyunit_unitid=?, \n" + // 1-3
                        "       login_userid=?, casename=?, originationdate=?, closingdate=?, \n" + // 4-7
                        "       notes=?, paccenabled=?, allowuplinkaccess=?, \n" + // 5-7
                        "       propertyinfocase=?, personinfocase_personid=?, bobsource_sourceid=?, \n" + // 8-10
                        "       active=?, lastupdatedby_userid=?, lastupdatedts=now() \n" + // 11-12
                        "  WHERE caseid=?;";
        PreparedStatement stmt = null;
        Connection con = null;
        
        try {
            
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ceCase.getPublicControlCode());
            stmt.setInt(2, ceCase.getPropertyID());
            if(ceCase.getPropertyUnitID() != 0){
                stmt.setInt(3, ceCase.getPropertyUnitID());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(ceCase.getCaseManager() != null){
                stmt.setInt(4, ceCase.getCaseManager().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            if(ceCase.getCaseName() != null){
                stmt.setString(5, ceCase.getCaseName());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            if(ceCase.getOriginationDate() != null){
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(ceCase.getOriginationDate()));
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            if(ceCase.getClosingDate() != null){
                stmt.setTimestamp(7, java.sql.Timestamp.valueOf(ceCase.getClosingDate()));
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            stmt.setString(8, ceCase.getNotes());
            stmt.setBoolean(9, ceCase.isPaccEnabled());
            stmt.setBoolean(10, ceCase.isAllowForwardLinkedPublicAccess());
            
            stmt.setBoolean(11, ceCase.isPropertyInfoCase());
            if(ceCase.getPersonInfoPersonID() != 0){
                stmt.setInt(12, ceCase.getPersonInfoPersonID());
            } else {
                stmt.setNull(12, java.sql.Types.NULL);
            }
            if(ceCase.getSource() != null){
                stmt.setInt(13, ceCase.getSource().getSourceid());
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }
            stmt.setBoolean(14, ceCase.isActive());
            if(ceCase.getLastUpdatedBy() != null){
                stmt.setInt(15, ceCase.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(15, java.sql.Types.NULL);
            }
            
            stmt.setInt(16, ceCase.getCaseID());
            
            stmt.executeUpdate();
            
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot udpate case due to a database storage issue", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    
    
   
    /**
     * Updates only the notes field on cecase table
     * @param cse with the Notes field as you want it inserted 
     * 
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public void updateCECaseNotes(CECase cse) 
            throws IntegrationException, BObStatusException{
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        if(cse == null){
            throw new BObStatusException("cannot update notes on a null case");
        }
        
        try {
            String s = "UPDATE public.cecase SET notes=? WHERE caseid=?";
            stmt = con.prepareStatement(s);
            stmt.setString(1, cse.getNotes());
            stmt.setInt(2, cse.getCaseID());

            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update notes", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
   
    /**
     * Updates only the notes field on citation table
     
     * 
     * @param cit
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public void updateCitationNotes(Citation cit) 
            throws IntegrationException, BObStatusException{
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        if(cit == null){
            throw new BObStatusException("cannot update notes on a null citation");
        }
        
        try {
            String s = "UPDATE public.citation SET notes=? WHERE citationid=?";
            stmt = con.prepareStatement(s);
            stmt.setString(1, cit.getNotes());
            stmt.setInt(2, cit.getCitationID());

            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update notes", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
   
    /**
     * Updates only the notes field on noticeofviolation table
     
     * 
     * @param nov
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public void novUpdateNotes(NoticeOfViolation nov) 
            throws IntegrationException, BObStatusException{
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        if(nov == null){
            throw new BObStatusException("cannot update notes on a null notice");
        }
        
        try {
            String s = "UPDATE public.noticeofviolation SET notes=? WHERE noticeid=?";
            stmt = con.prepareStatement(s);
            stmt.setString(1, nov.getNotes());
            stmt.setInt(2, nov.getNoticeID());

            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update notes", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
   
 
    
   
    /**
     * Grabs caseids from the loginobjecthistory table
     * @param userID
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public List<Integer> getCECaseHistoryList(int userID) 
            throws IntegrationException, BObStatusException{
        List<Integer> cseidl = new ArrayList<>();
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String s = "SELECT cecase_caseid, entrytimestamp FROM loginobjecthistory "
                    + "WHERE login_userid = ? "
                    + "AND cecase_caseid IS NOT NULL "
                    + "ORDER BY entrytimestamp DESC;";
            stmt = con.prepareStatement(s);
            stmt.setInt(1, userID);

            rs = stmt.executeQuery();
            
            while (rs.next()) {
                cseidl.add(rs.getInt("cecase_caseid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate case history list", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return cseidl;
    }
      /**
     *
     * @param casephase
     * @return
     * @throws IntegrationException
     */
    public Icon getIcon(CasePhaseEnum casephase) throws IntegrationException {
        Connection con = getPostgresCon();
        SystemIntegrator si = getSystemIntegrator();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT iconid ");
        sb.append("FROM public.cecasestatusicon WHERE status=?::casephase;");
        Icon icon = null;

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setString(1, casephase.toString());
            rs = stmt.executeQuery();
            while (rs.next()) {
                icon = si.getIcon(rs.getInt("iconid"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate icon", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return icon;

    }
    
    
    
    // *************************************************************************
    // *            CODE VIOLATIONS                                            *
    // *************************************************************************
    

    /**
     * Creates a new record in the codeviolation table from a CodeViolation object
     * @param v
     * @return
     * @throws IntegrationException 
     */
    public int insertCodeViolation(CodeViolation v) throws IntegrationException {
        int lastID = 0;

        String query =  "INSERT INTO public.codeviolation(\n" +
                        "            violationid, codesetelement_elementid, cecase_caseid, dateofrecord, \n" +
                        "            entrytimestamp, stipulatedcompliancedate, actualcompliancedate, \n" +
                        "            penalty, description, notes, legacyimport, compliancetimestamp, \n" +
                        "            complianceuser, severity_classid, createdby, compliancetfexpiry_proposalid, \n" +
                        "            lastupdatedts, lastupdated_userid, active)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, \n" +
                        "            now(), ?, ?, \n" +
                        "            ?, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            now(), ?, TRUE);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);

            stmt.setInt(1, v.getViolatedEnfElement().getCodeSetElementID());
            stmt.setInt(2, v.getCeCaseID());
            if(v.getDateOfRecord() != null){
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(v.getDateOfRecord()));
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }

            // entryts stamped by PG's now()
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(v.getStipulatedComplianceDate()));
            // a smidge of business logic here! Whoopsies
            stmt.setNull(5, java.sql.Types.NULL); // actual compliance
            
            stmt.setDouble(6, v.getPenalty());
            stmt.setString(7, v.getDescription());
            stmt.setString(8, v.getNotes());
            stmt.setBoolean(9, v.isLeagacyImport());
            stmt.setNull(10, java.sql.Types.NULL); // compliance TS
            
            stmt.setNull(11, java.sql.Types.NULL); // compliance user
            if(v.getSeverityIntensity() != null){
                stmt.setInt(12, v.getSeverityIntensity().getClassID() );
            } else {
                stmt.setNull(12, java.sql.Types.NULL);
            }
            
            if(v.getCreatedBy() != null){
                stmt.setInt(13, v.getCreatedBy().getUserID());
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }

            if(v.getComplianceTFExpiryPropID() != 0){
                stmt.setInt(14, v.getComplianceTFExpiryPropID());
            } else {
                stmt.setNull(14, java.sql.Types.NULL);
            }
            
            if(v.getLastUpdatedUser() != null){
                stmt.setInt(15, v.getLastUpdatedUser().getUserID());
            } else {
                stmt.setNull(15, java.sql.Types.NULL);
            }
            
            
            
            stmt.execute();
            
            String idNumQuery = "SELECT currval('codeviolation_violationid_seq');";
            Statement s = con.createStatement();
            ResultSet rs;
            rs = s.executeQuery(idNumQuery);
            rs.next();
            lastID = rs.getInt(1);

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot insert code violation, sorry.", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return lastID;

    }
    
        /**
     * Pathway for updating CodeViolatio objects
     * @param v
     * @throws IntegrationException 
     */
    public void updateCodeViolation(CodeViolation v) throws IntegrationException {
        String query =  " UPDATE public.codeviolation\n" +
                        "   SET codesetelement_elementid=?, cecase_caseid=?, dateofrecord=?, \n" + // 1-3
                        "       stipulatedcompliancedate=?, \n" + // 4-5
                        "       penalty=?, description=?, legacyimport=?, \n" + // 6-8
                        "       severity_classid=?, compliancetfexpiry_proposalid=?, \n" + // 9-12
                        "       lastupdatedts=now(), lastupdated_userid=?, active=?,  nullifiedts=?, nullifiedby=? \n" + // 13-14
                        " WHERE violationid = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, v.getViolatedEnfElement().getCodeSetElementID());
            stmt.setInt(2, v.getCeCaseID());
            
            if(v.getDateOfRecord() != null){
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(v.getDateOfRecord()));
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(v.getStipulatedComplianceDate()!= null){
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(v.getStipulatedComplianceDate()));
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            stmt.setDouble(5, v.getPenalty());
            stmt.setString(6, v.getDescription());
            stmt.setBoolean(7, v.isLeagacyImport());
            
           
            if(v.getSeverityIntensity() != null){
                stmt.setInt(8, v.getSeverityIntensity().getClassID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            
            if(v.getComplianceTFExpiryProp() != null){
                stmt.setInt(9, v.getComplianceTFExpiryProp().getProposalID());
            } else if(v.getComplianceTFExpiryPropID() != 0){
                stmt.setInt(9, v.getComplianceTFExpiryPropID());
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            
            if(v.getLastUpdatedUser() != null){
                stmt.setInt(10, v.getLastUpdatedUser().getUserID());
            }else {
                stmt.setNull(10, java.sql.Types.NULL);
            }
            stmt.setBoolean(11, v.isActive());
            
            if(v.getNullifiedTS() != null){
                stmt.setTimestamp(12, java.sql.Timestamp.valueOf(v.getNullifiedTS()));
            } else {
                stmt.setNull(12, java.sql.Types.NULL);
            }
            
            if(v.getNullifiedUser() != null){
                stmt.setInt(13, v.getNullifiedUser().getUserID());
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }
            
            stmt.setInt(14, v.getViolationID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot update code violation, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }
    
    
    /**
     * Pathway for recording compliance data on a single code violation record
     * @param v
     * @throws IntegrationException 
     */
    public void updateCodeViolationCompliance(CodeViolation v) throws IntegrationException {
        String query =  " UPDATE public.codeviolation\n" +
                        "   SET actualcompliancedate=?, compliancenote=?, \n" + // 4-5
                        "       complianceuser=?, compliancetimestamp=now(), \n" + // 9-12
                        "       lastupdatedts=now(), lastupdated_userid=? \n" + // 13-14
                        " WHERE violationid = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            if(v.getActualComplianceDate() != null){
                stmt.setTimestamp(1, java.sql.Timestamp.valueOf(v.getActualComplianceDate()));
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            
            stmt.setString(2, v.getComplianceNote());
            
            if(v.getComplianceUser() != null){
                stmt.setInt(3, v.getComplianceUser().getUserID());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(v.getLastUpdatedUser() != null){
                stmt.setInt(4, v.getLastUpdatedUser().getUserID());
            }else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            stmt.setInt(5, v.getViolationID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Integration error: cannot record violation compliance, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }
    
    
    /**
     * Generator method for CodeViolation objects
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException ////////////////////////////
     */
    private CodeViolation generateCodeViolationFromRS(ResultSet rs) throws SQLException, IntegrationException {

        CodeViolation v = new CodeViolation();
        CodeIntegrator ci = getCodeIntegrator();
        UserIntegrator ui = getUserIntegrator();
        WorkflowCoordinator wc = getWorkflowCoordinator();
        
        v.setViolationID(rs.getInt("violationid"));
        v.setViolatedEnfElement(ci.getEnforcableCodeElement(rs.getInt("codesetelement_elementid")));
        
        if(rs.getString("createdby") != null){
            v.setCreatedBy(ui.getUser(rs.getInt("createdby")));
        }
        
        v.setCeCaseID(rs.getInt("cecase_caseid"));
        if(rs.getTimestamp("dateofrecord") != null){
            v.setDateOfRecord(rs.getTimestamp("dateofrecord").toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
        }

        if(rs.getTimestamp("entrytimestamp") != null){
            
        v.setCreationTS(rs.getTimestamp("entrytimestamp").toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        }

        if(rs.getTimestamp("stipulatedcompliancedate") != null){
            
        v.setStipulatedComplianceDate(rs.getTimestamp("stipulatedcompliancedate").toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
                
        if (!(rs.getTimestamp("actualcompliancedate") == null)) {
            v.setActualComplianceDate(rs.getTimestamp("actualcompliancedate").toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
        }

        v.setPenalty(rs.getDouble("penalty"));
        v.setDescription(rs.getString("description"));
        v.setNotes(rs.getString("notes"));
        v.setLeagacyImport(rs.getBoolean("legacyimport"));

        if(rs.getTimestamp("compliancetimestamp") != null){
            v.setComplianceTimeStamp(rs.getTimestamp("compliancetimestamp").toLocalDateTime());
            v.setComplianceUser(ui.getUser(rs.getInt("complianceUser")));
        }
        
        v.setComplianceNote(rs.getString("compliancenote"));
        
        if(rs.getInt("compliancetfexpiry_proposalid") != 0){
            v.setComplianceTFExpiryPropID(rs.getInt("compliancetfexpiry_proposalid"));
            v.setComplianceTFExpiryProp(wc.getProposal(rs.getInt("compliancetfexpiry_proposalid")));
        }
        
        v.setActive(rs.getBoolean("active"));
        
        if(rs.getTimestamp("nullifiedts") != null){
            v.setNullifiedTS(rs.getTimestamp("nullifiedts").toLocalDateTime());
        } else {
            v.setNullifiedTS(null);
        }

        if(rs.getInt("nullifiedby") != 0){
            v.setNullifiedUser(ui.getUser(rs.getInt("nullifiedby")));
        } 
        
        return v;
    }

    /**
     * Primary getter method for CodeViolation objects
     * @param violationID
     * @return
     * @throws IntegrationException 
     */
    public CodeViolation getCodeViolation(int violationID) throws IntegrationException {
        String query = "SELECT violationid, codesetelement_elementid, cecase_caseid, dateofrecord, \n" +
                        "       entrytimestamp, stipulatedcompliancedate, actualcompliancedate, \n" +
                        "       penalty, description, notes, legacyimport, compliancetimestamp, \n" +
                        "       complianceuser, severity_classid, createdby, compliancetfexpiry_proposalid, \n" +
                        "       lastupdatedts, lastupdated_userid, active, compliancenote, nullifiedts, nullifiedby \n" +
                        "  FROM public.codeviolation WHERE violationid = ?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        CodeViolation cv = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, violationID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                cv = generateCodeViolationFromRS(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch code violation by ID, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return cv;
    }

    /**
     * Queries for all violation attached to a specific CECase
     * @param caseID
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> getCodeViolations(int caseID) throws IntegrationException {
        String query = "SELECT violationid FROM codeviolation WHERE cecase_caseid = ?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<Integer> idl = new ArrayList();

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, caseID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                
                idl.add(rs.getInt("violationid"));

            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot fetch code violation by ID, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return idl;
       
    }
    
    /**
     * Utility adaptor method for retrieving multiple code violations given a CECaseDataHeavy
     * @param c
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> getCodeViolationIDList(CECaseDataHeavy c) throws IntegrationException {
        return getCodeViolations(c.getCaseID());
    }
    
    /**
     * TODO: NADGIT Fix the BlobList References
     * @param cv
     * @return
     * @throws IntegrationException 
     */
    public List<Blob> loadViolationPhotoList(CodeViolation cv) throws IntegrationException{
        List<Blob> vBlobList = new ArrayList<>();
        BlobCoordinator bc = getBlobCoordinator();
        
        String query = "SELECT photodoc_photodocid FROM public.codeviolationphotodoc WHERE codeviolation_violationid = ?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cv.getViolationID());
            rs = stmt.executeQuery();

//            while (rs.next()) {
//                vBlobList.add(bc.getBlob(rs.getInt("photodoc_photodocid")));
//            }
            
//            cv.setBlobIDList(blobList);

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot load photos on violation.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return vBlobList;
    }
       /**
     * Updates only the notes field on codeviolation table
     
     * 
     * @param viol
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public void updateCodeViolationNotes(CodeViolation viol) 
            throws IntegrationException, BObStatusException{
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        if(viol == null){
            throw new BObStatusException("cannot update notes on a null violation");
        }
        
        try {
            String s = "UPDATE public.codeviolation SET notes=? WHERE violationid=?";
            stmt = con.prepareStatement(s);
            stmt.setString(1, viol.getNotes());
            stmt.setInt(2, viol.getViolationID());

            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update notes", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    
    
    
    // *************************************************************************
    // *            NOTICES OF VIOLATION (NOV)                                 *
    // *************************************************************************
    
    
    /**
     * Writes a new Notice of Violation to the DB
     * @param c associated CECaseDataHeavy
     * @param notice the Notice to write
     * @return the id assigned the new notice by the database
     * @throws IntegrationException 
     */
    public int novInsert(CECaseDataHeavy c, NoticeOfViolation notice) throws IntegrationException {

        String query =  "INSERT INTO public.noticeofviolation(\n" +
                        "            noticeid, caseid, lettertextbeforeviolations, creationtimestamp, \n" +
                        "            dateofrecord, sentdate, returneddate, personid_recipient, lettertextafterviolations, \n" +
                        "            lockedandqueuedformailingdate, lockedandqueuedformailingby, sentby, \n" +
                        "            returnedby, notes, creationby, printstyle_styleid)\n" +
                        "    VALUES (DEFAULT, ?, ?, now(), \n" +
                        "            ?, NULL, NULL, ?, ?, \n" +
                        "            NULL, NULL, NULL, \n" +
                        "            NULL, ?, ?, ?);";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int insertedNOVId = 0;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setInt(1, c.getCaseID());
            stmt.setString(2, notice.getNoticeTextBeforeViolations());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(notice.getDateOfRecord()));
            stmt.setInt(4, notice.getRecipient().getPersonID());
            stmt.setString(5, notice.getNoticeTextAfterViolations());
            stmt.setString(6, notice.getNotes());
            stmt.setInt(7, notice.getCreationBy().getUserID());
            if(notice.getStyle() != null){
                stmt.setInt(8, notice.getStyle().getStyleID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
                    
            stmt.execute();
            
            String retrievalQuery = "SELECT currval('noticeofviolation_noticeid_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();
            while(rs.next()){
                insertedNOVId = rs.getInt(1);
            }
            
            notice.setNoticeID(insertedNOVId);
            System.out.println("ViolationIntegrator.novInsert | noticeid " + notice.getNoticeID());
            System.out.println("ViolationIntegrator.novInsert | retrievedid " + insertedNOVId);
            
            List<CodeViolationDisplayable> cvList = notice.getViolationList();
            Iterator<CodeViolationDisplayable> iter = cvList.iterator();
            while(iter.hasNext()){
                CodeViolationDisplayable cvd = iter.next();
                novConnectNoticeToCodeViolation(notice, cvd);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert notice of violation letter at this time, sorry.", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return insertedNOVId;

    } // close method
    
    /**
     * Creates link between a NOV and a CECase
     * @param nov
     * @param cv
     * @throws IntegrationException 
     */
    private void novConnectNoticeToCodeViolation(NoticeOfViolation nov, CodeViolationDisplayable cv) throws IntegrationException{
        String query =  "INSERT INTO public.noticeofviolationcodeviolation(\n" +
                        "            noticeofviolation_noticeid, codeviolation_violationid, includeordtext, \n" +
                        "            includehumanfriendlyordtext, includeviolationphoto)\n" +
                        "    VALUES (?, ?, ?, \n" +
                        "            ?, ?);";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            System.out.println("ViolationIntegrator.novConnectNoticeTOCodeViolation | noticeid " + nov.getNoticeID());
            
            stmt.setInt(1, nov.getNoticeID());
            stmt.setInt(2, cv.getViolationID());
            stmt.setBoolean(3, cv.isIncludeOrdinanceText());
            stmt.setBoolean(4, cv.isIncludeHumanFriendlyText());
            stmt.setBoolean(5, cv.isIncludeViolationPhotos());

            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to connect notice and violation in database", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
    /**
     * Utility method for calling novUpdateNoticeCodeViolation on each violation
     * in a NOV
     * @param nov
     * @throws IntegrationException 
     */
    public void novUpdateAllNoticeCodeViolations(NoticeOfViolation nov) throws IntegrationException{
            Iterator<CodeViolationDisplayable> iter = nov.getViolationList().iterator();
            CodeViolationDisplayable cvd = null;
            while(iter.hasNext()){
                novUpdateNoticeCodeViolation(iter.next(), nov);
            }
    }
    
    /**
     * Updates the connection between a code violation and a NOV
     * @param cv
     * @param nov
     * @throws IntegrationException 
     */
    private void novUpdateNoticeCodeViolation(CodeViolationDisplayable cv, NoticeOfViolation nov) throws IntegrationException{
        String query =  "UPDATE public.noticeofviolationcodeviolation\n" +
                        "   SET includeordtext=?, includehumanfriendlyordtext=?, includeviolationphoto=? \n" +
                        " WHERE noticeofviolation_noticeid=? AND codeviolation_violationid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setBoolean(1, cv.isIncludeOrdinanceText());
            stmt.setBoolean(2, cv.isIncludeHumanFriendlyText());
            stmt.setBoolean(3, cv.isIncludeViolationPhotos());
            stmt.setInt(4, nov.getNoticeID());
            stmt.setInt(5, cv.getViolationID());

            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update notice violation links", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Removes linkages between a NOV and its CodeViolations. We don't lose any data beyond
     * display params for the CV in the NOV
     * @param nov
     * @throws IntegrationException 
     */
    private void novClearNoticeCodeViolationConnections(NoticeOfViolation nov) throws IntegrationException{
         String query =     "DELETE FROM public.noticeofviolationcodeviolation\n" +
                            " WHERE noticeofviolation_noticeid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, nov.getNoticeID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to clear connections between NOVs and code violations", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    /**
     * When a NOV is locked, it cannot be edited because it's ready for mailing
     * @param nov
     * @throws IntegrationException 
     */
    public void novLockAndQueueForMailing(NoticeOfViolation nov) throws IntegrationException{
        String query =  "UPDATE public.noticeofviolation\n" +
                        "   SET lockedandqueuedformailingdate=?, lockedandqueuedformailingby=?, " +
                        "   personid_recipient=?" +
                        "   WHERE noticeid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(nov.getLockedAndqueuedTS()));
            stmt.setInt(2, nov.getLockedAndQueuedBy().getUserID());
            stmt.setInt(3, nov.getRecipient().getPersonID());
            stmt.setInt(4, nov.getNoticeID());

            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to write NOV lock to database", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Updates the NOV record with sender info
     * @param nov
     * @throws IntegrationException 
     */
    public void novRecordMailing(NoticeOfViolation nov) throws IntegrationException{
        String query =  "UPDATE public.noticeofviolation\n" +
                        "   SET sentdate=?, sentby=? " +
                        "  WHERE noticeid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(nov.getSentTS()));
            stmt.setInt(2, nov.getSentBy().getUserID());
            stmt.setInt(3, nov.getNoticeID());

            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to record NOV mailing in database", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    /**
     * Updates an NOV record to reflect the NOV being returned by the USPS
     * @param nov
     * @throws IntegrationException 
     */
    public void novRecordReturnedNotice(NoticeOfViolation nov) throws IntegrationException{
        String query =  "UPDATE public.noticeofviolation\n" +
                        "   SET returneddate=?, returnedby=? " +
                        "  WHERE noticeid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(nov.getReturnedTS()));
            stmt.setInt(2, nov.getReturnedBy().getUserID());
            stmt.setInt(3, nov.getNoticeID());

            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot record notice as returned in database", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    /**
     * Updates the notes field only on a NOV object
     * @param nov
     * @throws IntegrationException 
     */
    public void novUpdateNoticeNotes(NoticeOfViolation nov) throws IntegrationException{
        String query =  "UPDATE public.noticeofviolation\n" +
                        "   SET notes=? " +
                        "  WHERE noticeid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setString(1, nov.getNotes());
            stmt.setInt(2, nov.getNoticeID());

            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot record notice as returned in database", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
    /**
     * Updates a subset of NOV fields
     * @param notice
     * @throws IntegrationException 
     */
    public void novUpdateNotice(NoticeOfViolation notice) throws IntegrationException {
        String query = "UPDATE public.noticeofviolation\n"
                + "   SET lettertextbeforeviolations=?, \n" +
                "       dateofrecord=?, personid_recipient=?, \n" +
                "       lettertextafterviolations=?"
                + " WHERE noticeid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setString(1, notice.getNoticeTextBeforeViolations());
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(notice.getDateOfRecord()));
            stmt.setInt(3, notice.getRecipient().getPersonID());
            stmt.setString(4, notice.getNoticeTextAfterViolations());
            stmt.setInt(5, notice.getNoticeID());

            stmt.executeUpdate();
            
            novClearNoticeCodeViolationConnections(notice);
            
            List<CodeViolationDisplayable> cvList = notice.getViolationList();
            Iterator<CodeViolationDisplayable> iter = cvList.iterator();
            while(iter.hasNext()){
                CodeViolationDisplayable cvd = iter.next();
                novConnectNoticeToCodeViolation(notice, cvd);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot update notice of violation letter at this time, sorry.", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }

    /**
     * Actually deletes an NOV from the DB! Very rare indeed. The CaseCoordinator
     * will only allow this if the NOV has not been locked or printed
     * @param notice
     * @throws IntegrationException 
     */
    public void novDelete(NoticeOfViolation notice) throws IntegrationException {
        String query = "UPDATE noticeofviolation SET active=FALSE "
                + "  WHERE noticeid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, notice.getNoticeID());

            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot update notice of violation letter at this time, sorry.", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }
    
    /**
     * Updates an NOV record to reflect that no mailing actions have occurred
     * @param notice
     * @throws IntegrationException 
     */
     public void novResetMailingFieldsToNull(NoticeOfViolation notice) throws IntegrationException {
        String query = "UPDATE public.noticeofviolation\n" +
                        "   SET sentdate=NULL, sentby=NULL, returneddate=NULL, returnedby=NULL,\n" +
                        "       lockedandqueuedformailingdate=NULL, lockedandqueuedformailingby=NULL"
                        + "  WHERE noticeid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, notice.getNoticeID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot clear nov of mailing data.", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }
    
     public void novUpdateHeaderImage(PrintStyle ps, Blob blob) throws BObStatusException, IntegrationException{
         if(ps == null || blob == null){
             throw new BObStatusException("Cannot update header image with null print or blob");
                     
         }
         

        String query = "UPDATE public.printstyle\n" +
                        "   SET headerimage_photodocid=?" +
                        " WHERE styleid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blob.getBlobID());
            stmt.setInt(2, ps.getStyleID());
            
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot clear nov of mailing data.", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
         
         
     }
     
    /**
     * Primary getter for the NOV object
     * @param noticeID
     * @return
     * @throws IntegrationException 
     */
    public NoticeOfViolation novGet(int noticeID) throws IntegrationException {
        String query =  "SELECT noticeid, caseid, lettertextbeforeviolations, creationtimestamp, \n" +
                        "       dateofrecord, sentdate, returneddate, personid_recipient, lettertextafterviolations, \n" +
                        "       lockedandqueuedformailingdate, lockedandqueuedformailingby, sentby, \n" +
                        "       returnedby, notes, creationby, printstyle_styleid, active, followupevent_eventid \n" +
                        "  FROM public.noticeofviolation WHERE noticeid = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        NoticeOfViolation notice = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, noticeID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                notice = novGenerate(rs);

            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch code violation by ID, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return notice;
    }

    /**
     * Grabs all the NOVs for a given CECase
     * @param ceCase
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> novGetList(CECase ceCase) throws IntegrationException {
        String query = "SELECT noticeid FROM public.noticeofviolation WHERE caseid=? AND active=TRUE;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<Integer> idl = new ArrayList();

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ceCase.getCaseID());
            rs = stmt.executeQuery();

            while (rs.next()) {
                idl.add(rs.getInt("noticeid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch code violation by ID, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return idl;
    }
    /**
     * Grabs all the NOVs for a given CECase
     * @param cv
     * @return of Notice letter IDs
     * @throws IntegrationException 
     */
    public List<Integer> novGetNOVIDList(CodeViolation cv) throws IntegrationException {
        String query = "SELECT noticeofviolation_noticeid FROM public.noticeofviolationcodeviolation "
                + "WHERE codeviolation_violationid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<Integer> idl = new ArrayList<>();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cv.getViolationID());
            rs = stmt.executeQuery();

            while (rs.next()) {
                idl.add(rs.getInt("noticeofviolation_noticeid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch code violation by ID, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return idl;
    }

    /**
     * Internal generator method for NOVs
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
    private NoticeOfViolation novGenerate(ResultSet rs) throws SQLException, IntegrationException {
//SELECT noticeid, caseid, lettertextbeforeviolations, creationtimestamp, 
//       dateofrecord, sentdate, returneddate, personid_recipient, lettertextafterviolations, 
//       lockedandqueuedformailingdate, lockedandqueuedformailingby, sentby, 
//       returnedby, notes
//  FROM public.noticeofviolation;

        PersonIntegrator pi = getPersonIntegrator();
        UserIntegrator ui = getUserIntegrator();
        SystemIntegrator si = getSystemIntegrator();
        EventCoordinator ec = getEventCoordinator();
        
        // the magical moment of notice instantiation
        NoticeOfViolation notice = new NoticeOfViolation();

        notice.setNoticeID(rs.getInt("noticeid"));
        notice.setRecipient(pi.getPerson(rs.getInt("personid_recipient")));
        notice.setDateOfRecord(rs.getTimestamp("dateofrecord").toLocalDateTime());

        notice.setNoticeTextBeforeViolations(rs.getString("lettertextbeforeviolations"));
        populateCodeViolations(notice);
        notice.setNoticeTextAfterViolations(rs.getString("lettertextafterviolations"));

        notice.setCreationTS(rs.getTimestamp("creationtimestamp").toLocalDateTime());
        notice.setCreationBy(ui.getUser(rs.getInt("creationby")));
        
        if (rs.getTimestamp("lockedandqueuedformailingdate") != null) {
            notice.setLockedAndqueuedTS(rs.getTimestamp("lockedandqueuedformailingdate").toLocalDateTime());
            notice.setLockedAndQueuedBy(ui.getUser(rs.getInt("lockedandqueuedformailingby")));
        }
        
        if (rs.getTimestamp("sentdate") != null) {
            notice.setSentTS(rs.getTimestamp("sentdate").toLocalDateTime());
            notice.setSentBy(ui.getUser(rs.getInt("sentby")));
        } 
        
        if (rs.getTimestamp("returneddate") != null) {
            notice.setReturnedTS(rs.getTimestamp("returneddate").toLocalDateTime());
            notice.setReturnedBy(ui.getUser(rs.getInt("returnedby")));
        } 
        
        notice.setStyle(si.getPrintStyle(rs.getInt("printstyle_styleid")));
        
        notice.setNotes(rs.getString("notes"));
        if(rs.getInt("followupevent_eventid") != 0){
            notice.setFollowupEvent(ec.getEvent(rs.getInt("followupevent_eventid")));
        }
        
        notice.setActive(rs.getBoolean("active"));

        return notice;

    }
    
    /**
     * Looks up all of the CodeViolations assocaited with a given NOV
     * and extracts their display properties stored in the linking DB's linking table
     * @param nov
     * @return
     * @throws IntegrationException 
     */
    private NoticeOfViolation populateCodeViolations(NoticeOfViolation nov) throws IntegrationException{
        String query =  "  SELECT noticeofviolation_noticeid, codeviolation_violationid, includeordtext, \n" +
                        "       includehumanfriendlyordtext, includeviolationphoto\n" +
                        "  FROM public.noticeofviolationcodeviolation WHERE noticeofviolation_noticeid = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        CodeViolationDisplayable viol;
        List<CodeViolationDisplayable> codeViolationList = new ArrayList<>();

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, nov.getNoticeID() );
            rs = stmt.executeQuery();

            while (rs.next()) {
                viol = new CodeViolationDisplayable(getCodeViolation(rs.getInt("codeviolation_violationid")));
                viol.setIncludeOrdinanceText(rs.getBoolean("includeordtext"));
                viol.setIncludeHumanFriendlyText(rs.getBoolean("includehumanfriendlyordtext"));
                viol.setIncludeViolationPhotos(rs.getBoolean("includeviolationphoto"));
                codeViolationList.add(viol);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot fetch code violation by ID, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        nov.setViolationList(codeViolationList);
        
        return nov;
        
    }
    
    
    
    
     // ***********************************************************************
     // **                  TEXT BLOCKS                                      **
     // ***********************************************************************
     
   
   /**
    * Retrieves all text blocks from DB
    * @return
    * @throws IntegrationException 
    */
    public HashMap<String, Integer> getTextBlockCategoryMap() throws IntegrationException{
        String query =  "SELECT categoryid, categorytitle\n" +
                        "  FROM public.textblockcategory;";
        HashMap<String, Integer> categoryMap = new HashMap<>();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                categoryMap.put(rs.getString("categorytitle"), rs.getInt("categoryid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot fetch code violation by ID, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return categoryMap;
    }
    
    private TextBlock generateTextBlock(ResultSet rs) throws SQLException, IntegrationException{
        TextBlock tb = new TextBlock();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        
        tb.setBlockID(rs.getInt("blockid"));
        tb.setTextBlockCategoryID(rs.getInt("categoryid"));
        tb.setTextBlockCategoryTitle(rs.getString("categorytitle"));
        tb.setMuni(mi.getMuni(rs.getInt("muni_municode")));
        tb.setTextBlockName(rs.getString("blockname"));
        tb.setTextBlockText(rs.getString("blocktext"));
        tb.setPlacementOrder(rs.getInt("placementorderdefault"));
        tb.setInjectableTemplate(rs.getBoolean("injectabletemplate"));
        
        return tb;
    }
    
    /**
     * Extracts a single text block from the DB
     * @param blockID
     * @return
     * @throws IntegrationException 
     */
     public TextBlock getTextBlock(int blockID) throws IntegrationException{
         
        String query =  "SELECT blockid, blockcategory_catid, textblock.muni_municode,"
                + " blockname, blocktext, categoryid, categorytitle, placementorderdefault,"
                + " injectabletemplate \n" +
                        "  FROM public.textblock INNER JOIN public.textblockcategory " + 
                        "  ON textblockcategory.categoryid=textblock.blockcategory_catid\n" +
                        "  WHERE blockid=?;";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection con = null;
        TextBlock tb = null;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blockID);
            
            rs = stmt.executeQuery(); 
            
            while(rs.next()){
                tb = generateTextBlock(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Code Violation Integrator: cannot retrive text block by ID", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
          
         return tb;
     }
     
    /**
     * Extracts all text blocks by a given category
     * @param categoryID
     * @return
     * @throws IntegrationException 
     */
     public List<TextBlock> getTextBlocksByCategory(int catID) throws IntegrationException{
         
        String query =  "SELECT blockid " +
                        "  FROM public.textblock WHERE blockcategory_catid=?;";
        List<TextBlock> blockList = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection con = null;
        TextBlock tb = null;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, catID);
            
            rs = stmt.executeQuery(); 
            
            while(rs.next()){
                blockList.add(getTextBlock(rs.getInt("blockid")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Code Violation Integrator: cannot retrive text block by ID", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
          
         return blockList;
     }
     
     /**
      * Extracts all text blocks associated with a given Muni
      * @param m
      * @return
      * @throws IntegrationException 
      */
     public ArrayList<TextBlock> getTextBlocks(Municipality m) throws IntegrationException{
        String query =    "  SELECT blockid " +
                            "  FROM public.textblock INNER JOIN public.textblockcategory ON textblockcategory.categoryid=textblock.blockcategory_catid\n" +
                            "  WHERE textblock.muni_municode=?;";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection con = null;
        ArrayList<TextBlock> ll = new ArrayList();
        TextBlock tb = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, m.getMuniCode());
            
            rs = stmt.executeQuery(); 
            
            while(rs.next()){
                tb = getTextBlock(rs.getInt("blockid"));
                ll.add(tb);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Code Violation Integrator: cannot retrive text blocks by municipality", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
         return ll;
     }
     
       /**
      * Extracts all text blocks associated with a given Muni
      * @param m
      * @return
      * @throws IntegrationException 
      */
     public ArrayList<TextBlock> getTextBlockTemplates(Municipality m) throws IntegrationException{
        String query =    "  SELECT blockid " +
                            "  FROM public.textblock INNER JOIN public.textblockcategory ON textblockcategory.categoryid=textblock.blockcategory_catid\n" +
                            "  WHERE textblock.muni_municode=? AND textblock.injectabletemplate IS TRUE;";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection con = null;
        ArrayList<TextBlock> ll = new ArrayList();
        TextBlock tb = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, m.getMuniCode());
            
            rs = stmt.executeQuery(); 
            
            while(rs.next()){
                tb = getTextBlock(rs.getInt("blockid"));
                ll.add(tb);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Code Violation Integrator: cannot retrive text blocks by municipality", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
         return ll;
     }
     
     /**
      * Extracts a master list of all text blocks regardless of Muni
      * @return
      * @throws IntegrationException 
      */
     public List<TextBlock> getAllTextBlocks() throws IntegrationException{
        String query =    "  SELECT blockid \n" +
                          "  FROM public.textblock INNER JOIN public.textblockcategory "
                        + "  ON textblockcategory.categoryid=textblock.blockcategory_catid;";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection con = null;
        ArrayList<TextBlock> ll = new ArrayList<>();
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            
            rs = stmt.executeQuery(); 
            
            while(rs.next()){
                ll.add(getTextBlock(rs.getInt("blockid")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Code Violation Integrator: cannot retrive all textblocks", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
          
         return ll;
         
     }
     
     /**
      * Creates a new entry in the textblock table
      * @param tb
      * @throws IntegrationException 
      */
     public void insertTextBlock(TextBlock tb) throws IntegrationException{
        String query =  "INSERT INTO public.textblock(\n" +
                        " blockid, blockcategory_catid, muni_municode, blockname, "
                        + "blocktext, placementorderdefault, injectabletemplate)\n" +
                        " VALUES (DEFAULT, ?, ?, ?, ?, ?, ?);";
        PreparedStatement stmt = null;
        Connection con = null;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, tb.getTextBlockCategoryID());
            stmt.setInt(2, tb.getMuni().getMuniCode());
            stmt.setString(3, tb.getTextBlockName());
            stmt.setString(4, tb.getTextBlockText());
            stmt.setInt(5, tb.getPlacementOrder());
            stmt.setBoolean(6, tb.isInjectableTemplate());
            
            
            stmt.execute(); 
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Text Block Integration Module: cannot insert text block into DB, sorry", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
          
         
         
     }
     
     /**
      * Updates key fields on text blocks
      * @param tb
      * @throws IntegrationException 
      */
     public void updateTextBlock(TextBlock tb) throws IntegrationException{
        String query = "UPDATE public.textblock\n" +
                        "   SET blockcategory_catid=?, muni_municode=?, blockname=?, \n" +
                        "       blocktext=?,placementorderdefault=?, injectabletemplate=? \n" +
                        " WHERE blockid=?;";
        PreparedStatement stmt = null;
        Connection con = null;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, tb.getTextBlockCategoryID());
            stmt.setInt(2, tb.getMuni().getMuniCode());
            stmt.setString(3, tb.getTextBlockName());
            stmt.setString(4, tb.getTextBlockText());
            stmt.setInt(5, tb.getPlacementOrder());
            stmt.setBoolean(6, tb.isInjectableTemplate());
            stmt.setInt(7, tb.getBlockID());  
            
            stmt.execute(); 
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Text Block Integration Module: cannot insert text block into DB", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
          
         
     }
     
     /**
      * Nukes a text block from the DB--very rare--only allowed if not used in a NOV
      * @param tb
      * @throws IntegrationException 
      */
     public void deleteTextBlock(TextBlock tb) throws IntegrationException{
        String query = " DELETE FROM public.textblock\n" +
                        " WHERE blockid=?;";
        PreparedStatement stmt = null;
        Connection con = null;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, tb.getBlockID());
            
            stmt.execute(); 
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Text Block Integration Module: cannot delete text block into DB, "
                    + "probably because it has been used in a letter somewhere", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
          
     }
     
     
     // ***********************************************************************
     // **                  CITATIONS                                        **
     // ***********************************************************************
     
     /**
      * Creates a new record in the citation table and connects applicable
      * codeviolations; TODO: the violation connection should probably be separated out
      * 
      * @param citation
      * @throws IntegrationException 
      */
    public void insertCitation(Citation citation) throws IntegrationException{
        
        String queryCitationTable =  "INSERT INTO public.citation(\n" +
                        "            citationid, citationno, status_statusid, origin_courtentity_entityid, \n" +
                        "            login_userid, dateofrecord, transtimestamp, isactive, \n" +
                        "            notes)\n" +
                        "    VALUES (DEFAULT, ?, ?, \n" +
                        "            ?, ?, ?, now(), ?, \n" +
                        "            ?);";
        
        String queryCitationViolationTable = "INSERT INTO public.citationviolation(\n" +
                        "            citationviolationid, citation_citationid, codeviolation_violationid)\n" +
                        "    VALUES (DEFAULT, ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmtCID = null;
        ResultSet rs = null;
        
        try {
            stmt1 = con.prepareStatement(queryCitationTable);
            stmtCID = con.prepareStatement("SELECT currval('citation_citationid_seq');");
            stmt2 = con.prepareStatement(queryCitationViolationTable);
            
            stmt1.setString(1, citation.getCitationNo());
            stmt1.setInt(2, citation.getStatus().getStatusID());
            stmt1.setInt(3, citation.getOrigin_courtentity().getCourtEntityID());
            stmt1.setInt(4, citation.getUserOwner().getUserID());
            stmt1.setTimestamp(5, java.sql.Timestamp.valueOf(citation.getDateOfRecord()));
            stmt1.setBoolean(6, citation.isIsActive());
            stmt1.setString(7, citation.getNotes());
            
            stmt1.execute();
            
            rs = stmtCID.executeQuery();
            int lastCID = 0;
            while(rs.next()){
                 lastCID= rs.getInt(1);
            }
            
            ListIterator<CodeViolation> li = citation.getViolationList().listIterator();
            
            while(li.hasNext()){
                stmt2.setInt(1, lastCID);
                stmt2.setInt(2, (int) li.next().getViolationID());
                
                stmt2.execute();
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert citation into database, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt1 != null) { try { stmt1.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Extracts a list of citation ID values given a CodeViolation ID
     * @param violationID
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> getCitations(int violationID) throws IntegrationException{
        List<Integer> cList = new ArrayList<>();
        String query = "SELECT citation_citationid FROM public.citationviolation WHERE codeviolation_violationid = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, violationID);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                cList.add(rs.getInt("citation_citationid"));
                
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate citation list", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return cList;
    }
    
    /**
     * Extracts a single citation from the DB given a citation ID
     * @param id
     * @return
     * @throws IntegrationException 
     */
    public Citation getCitation(int id) throws IntegrationException{

        String query = "SELECT citationid, citationno, status_statusid, origin_courtentity_entityid, \n" +
                        "login_userid, dateofrecord, transtimestamp, isactive, notes, officialtext FROM public.citation WHERE citationid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Citation c = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                c = generateCitationFromRS(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch code violation by ID, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return c;
    }
    
    /**
     * Extracts a list of Citation IDs from the DB given a Property
     * @param prop
     * @return
     * @throws IntegrationException 
     */
    public List<Citation> getCitations(Property prop) throws IntegrationException{
        //this doesn't work anymore since citations don't know about cases, we have to go through citationViolation
        // codeviolations know about cases
        
        String query =  "SELECT citationid FROM public.citation 	INNER JOIN public.cecase ON cecase.caseid = citation.caseid \n" +
                        "INNER JOIN public.property ON cecase.property_propertyID = property.propertyID \n" +
                        "WHERE propertyID=?; ";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<Citation> citationList = new ArrayList();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, prop.getPropertyID());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                citationList.add(getCitation(rs.getInt("citationid")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch code violation by ID, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return citationList;
    }
    
    /**
     * Extracts citations from the DB given a CECase
     * @param ceCase
     * @return
     * @throws IntegrationException 
     */
    public List<Citation> getCitations(CECase ceCase) throws IntegrationException{
            
        String query =  "SELECT DISTINCT ON (citationID) citation.citationid, codeviolation.cecase_caseID FROM public.citationviolation 	\n" +
                        "	INNER JOIN public.citation ON citation.citationid = citationviolation.citation_citationid\n" +
                        "	INNER JOIN public.codeviolation on codeviolation.violationid = citationviolation.codeviolation_violationid\n" +
                        "	INNER JOIN public.cecase ON cecase.caseid = codeviolation.cecase_caseID\n" +
                        "	WHERE codeviolation.cecase_caseID=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<Citation> citationList = new ArrayList();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ceCase.getCaseID());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                citationList.add(getCitation(rs.getInt("citationid")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch code violation by ID, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return citationList;
    }
    
    /**
     * Extracts a list of CodeViolation IDs from DB given a citation ID
     * @param cid
     * @return
     * @throws IntegrationException 
     */
    private List<Integer> getCodeViolations(Citation cid) throws IntegrationException{
        
        String query =  "SELECT codeviolation_violationid FROM public.citationviolation 	\n" +
                        "	INNER JOIN public.citation ON citation.citationid = citationviolation.citation_citationid\n" +
                        "	INNER JOIN public.codeviolation on codeviolation.violationid = citationviolation.codeviolation_violationid\n" +
                        "	WHERE citation.citationid=?;";
        Connection con = getPostgresCon();
        CaseIntegrator ci = getCaseIntegrator();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<CodeViolation> violationList = new ArrayList<>();
        List<Integer> idl = new ArrayList<>();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cid.getCitationID());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                idl.add(rs.getInt("codeviolation_violationid"));
                
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch code violation by ID, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return idl;
    }
    
    /**
     * Internal generator method for creating a Citation from a ResultSet with 
     * all DB fields present
     * @param rs
     * @return
     * @throws IntegrationException 
     */
    private Citation generateCitationFromRS(ResultSet rs) throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        Citation cit = new Citation();
        try {
            cit.setCitationID(rs.getInt("citationID"));
            cit.setCitationNo(rs.getString("citationNo"));
            cit.setStatus(getCitationStatus(rs.getInt("status_statusid")));
            cit.setOrigin_courtentity(cei.getCourtEntity(rs.getInt("origin_courtentity_entityID")));
            cit.setUserOwner(ui.getUser(rs.getInt("login_userID")));
            cit.setDateOfRecord(rs.getTimestamp("dateOfRecord").toLocalDateTime());
            cit.setTimeStamp(rs.getTimestamp("transTimeStamp").toLocalDateTime());
            cit.setIsActive(rs.getBoolean("isActive"));
            cit.setNotes(rs.getString("notes"));
            
            cit.setOfficialText(rs.getString("officialtext"));
        } catch (SQLException | IntegrationException ex) {
            System.out.println(ex);
            throw new IntegrationException("Unable to build citation from RS", ex);
        }
        return cit;
    }
    
    /**
     * Updates a single record in the citation table
     * @param citation
     * @throws IntegrationException 
     */
    public void updateCitation(Citation citation) throws IntegrationException{
        String query =  "UPDATE public.citation\n" +
                        "   SET citationno=?, status_statusid=?, origin_courtentity_entityid=?, \n" +
                        "       login_userid=?, dateofrecord=?, transtimestamp=now(), isactive=?, \n" +
                        "       officialtext=? \n" +
                        " WHERE citationid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, citation.getCitationNo());
            stmt.setInt(2, citation.getStatus().getStatusID());
            stmt.setInt(3, citation.getOrigin_courtentity().getCourtEntityID());
            stmt.setInt(4, citation.getUserOwner().getUserID());
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(citation.getDateOfRecord()));
            stmt.setBoolean(6, citation.isIsActive());
            stmt.setString(7, citation.getOfficialText());
            stmt.setInt(8, citation.getCitationID());
            
            stmt.execute();
            
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_INFO, 
                       "Updated citation no." + citation.getCitationNo() , ""));

            
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update citation in the database, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Nukes a Citation from the DB! Dangerous!
     * @param citation
     * @throws IntegrationException 
     */
    public void deleteCitation(Citation citation) throws IntegrationException{
        String query =  "DELETE FROM public.citation\n" +
                        " WHERE citationid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, citation.getCitationID());
            stmt.execute();
            
            getFacesContext().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Citation has been deleted from system forever!", ""));
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to delete this citation in the database, sorry. "
                    + "Most likely reason: some other record in the system references this citation somehow, "
                    + "like a court case. As a result, this citation cannot be deleted.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Creates a CitationStatus given a statusid
     * @param statusID
     * @return
     * @throws IntegrationException 
     */
    public CitationStatus getCitationStatus(int statusID) throws IntegrationException{
            
        String query =  "SELECT statusid, statusname, description, icon_iconid, editsforbidden, \n" +
                        "       eventrule_ruleid "
                        + "FROM citationStatus WHERE statusid=?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        CitationStatus cs = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, statusID);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                cs = generateCitationStatus(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch code violation by ID, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return cs;
        
        
    }
    
    /**
     * Buyilds a complete list of possible citation statuses
     * @return
     * @throws IntegrationException 
     */
    public List<CitationStatus> getCitationStatusList() throws IntegrationException{
        String query =  "SELECT statusid FROM citationStatus;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<CitationStatus> csList = new ArrayList<>();
        
        try {
            stmt = con.prepareStatement(query);
            
            rs = stmt.executeQuery();
            
            while(rs.next()){
                csList.add(getCitationStatus(rs.getInt("statusid")));
                
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch code violation by ID, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return csList;
        
        
        
    }
    
    /**
     * Internal generator method for CitationStatus objects
     * @param rs
     * @return
     * @throws IntegrationException 
     */
    private CitationStatus generateCitationStatus(ResultSet rs) throws IntegrationException{
        CitationStatus cs = new CitationStatus();
        SystemIntegrator si = getSystemIntegrator();
        WorkflowIntegrator wi = getWorkflowIntegrator();
        try {
            cs.setStatusID(rs.getInt("statusid"));
            cs.setStatusTitle(rs.getString("statusname"));
            cs.setDescription(rs.getString("description"));
            cs.setIcon(si.getIcon(rs.getInt("icon_iconid")));
            cs.setNonStatusEditsForbidden(rs.getBoolean("editsforbidden"));
            cs.setPhaseChangeRule(wi.rules_getEventRuleAbstract(rs.getInt("eventrule_ruleid")));
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Cannot Generate citation status object, sorry", ex);
        }
        return cs;
    }
    
    /**
     * Creates a new record n the citationstatus table
     * @param cs
     * @throws IntegrationException 
     */
    public void insertCitationStatus(CitationStatus cs) throws IntegrationException{
        CaseIntegrator ci = getCaseIntegrator();
        
        String query =  "INSERT INTO public.citationstatus(\n" +
                        "            statusid, statusname, description, icon_iconid, editsforbidden, \n" +
                        "       eventrule_ruleid)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, cs.getStatusTitle());
            stmt.setString(2, cs.getDescription());
            stmt.setInt(3, cs.getIcon().getIconid());
            stmt.setBoolean(4, cs.isNonStatusEditsForbidden());
            if(cs.getPhaseChangeRule() != null){
                stmt.setInt(5, cs.getPhaseChangeRule().getRuleid());
                
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert citation into database, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    /**
     * Nukes a record from the citationstatus table
     * @param cs
     * @throws IntegrationException 
     */
    public void deleteCitationStatus(CitationStatus cs) throws IntegrationException{
        
        String query = "DELETE FROM public.citationstatus WHERE statusid=?";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cs.getStatusID());
            stmt.execute();
            
            getFacesContext().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Citation status has been deleted from system forever!", ""));

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert citation into database, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        
    }
    
    /**
     * Updates a single record in the citationstatus table
     * @param cs
     * @throws IntegrationException 
     */
    public void updateCitationStatus(CitationStatus cs) throws IntegrationException{
        
        String query =  "UPDATE public.citationstatus\n" +
                        "   SET statusname=?, description=?, icon_iconid=?, editsforbidden=?, eventrule_ruleid=?\n" +
                        " WHERE statusid=?;";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, cs.getStatusTitle());
            stmt.setString(2, cs.getDescription());
            stmt.setInt(3, cs.getIcon().getIconid());
            stmt.setBoolean(4, cs.isNonStatusEditsForbidden());
             if(cs.getPhaseChangeRule() != null){
                stmt.setInt(5, cs.getPhaseChangeRule().getRuleid());
                
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            stmt.execute();

            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert citation into database, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    } // close method
    
     
} // close CaseIntegrator