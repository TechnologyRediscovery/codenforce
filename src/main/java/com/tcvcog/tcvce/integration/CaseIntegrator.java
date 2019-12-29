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
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseBase;
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.EventRuleAbstract;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Fee;
import com.tcvcog.tcvce.entities.FeeAssigned;
import com.tcvcog.tcvce.entities.MoneyCECaseFeeAssigned;
import com.tcvcog.tcvce.entities.MoneyCECaseFeePayment;
import com.tcvcog.tcvce.entities.Payment;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import com.tcvcog.tcvce.entities.search.SearchParamsCECase;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eric C. Darsow
 */
public class CaseIntegrator extends BackingBeanUtils implements Serializable{

    /**
     * Creates a new instance of CaseIntegrator
     */
    public CaseIntegrator() {
    }
    
    
    /**
     * First gen search method before Query and SearchParam object system
     * came online
     * 
     * @deprecated 
     * @param p
     * @return
     * @throws IntegrationException
     * @throws CaseLifecycleException 
     */
    public ArrayList getCECasesByProp(Property p) 
            throws IntegrationException, CaseLifecycleException{
        ArrayList<CECase> caseList = new ArrayList();
        String query = "SELECT \n" +
            "  caseid\n" +
            "FROM \n" +
            "  public.cecase, \n" +
            "  public.property\n" +
            "WHERE \n" +
            "  cecase.property_propertyid = property.propertyid AND\n" +
            "  property.propertyid = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, p.getPropertyID());
            //System.out.println("CaseIntegrator.getCECasesByProp| sql: " + stmt.toString());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                caseList.add(getCECase(rs.getInt("caseid")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get cases by property", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return caseList;
    }
    
   
    
    /**
     * Internal serach method for Code Enforcement case using a SearchParam
     * subclass. Outsiders will use runQueryCECase or runQueryCECase
     * @param params
     * @return
     * @throws IntegrationException
     * @throws CaseLifecycleException 
     */
    public List<Integer> searchForCECases(SearchParamsCECase params) throws IntegrationException, CaseLifecycleException{
        List<Integer> cseidlst = new ArrayList<>();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        boolean notFirstCriteria = false;
        
        sb.append("SELECT caseid ");
        sb.append("FROM public.cecase INNER JOIN public.property ON (property_propertyid = propertyid) ");
        sb.append("WHERE caseid IS NOT NULL AND ");
        
         if (!params.isBobID_ctl()) {
            if (params.isMuni_ctl()) {
                sb.append("municipality_municode = ? "); // param 1
            }

            if (params.isDate_startEnd_ctl()){
                switch (params.getDateToSearchCECases()) {
                    case "Opening date of record":
                        sb.append("originationdate ");
                        break;
                    case "Database record timestamp":
                        sb.append("creationtimestamp ");
                        break;
                    case "Closing date": 
                        sb.append("closingdate ");
                        break;
                    default:
                        sb.append("originationdate ");
                        break;
                }
                sb.append("BETWEEN ? AND ? "); // parm 2 and 3 without ID
            }


            if (params.isUseCasePhase()) {
                if(params.getCasePhase() != null){
                    sb.append("casephase = ?::casephase ");
                }
            }

            if (params.isUseCaseStage() && !params.isUseCasePhase()) {
                List<CasePhase> phList = params.getCaseStageAsPhaseList();
                if(phList != null){
                    int listLen = phList.size();
                    sb.append("(");
                    for(CasePhase cp : phList){
                        sb.append("casephase = ?::casephase ");
                        if(listLen > 1){
                            sb.append("OR ");
                            listLen--;
                        } else {
                            sb.append(") ");
                        }
                    }
                }
            }

            if (params.isUseProperty()) {
                sb.append("property_propertyid = ? ");
            }
            if (params.isUseCaseManager()) {
                if(params.getCaseManagerUser() != null){
                    if(notFirstCriteria){sb.append("AND ");} else {notFirstCriteria = true;}
                    sb.append("login_userid = ? ");
                }
            }

            if (params.isUsePropertyInfoCase()) {
                if (params.isPropertyInfoCase()) {
                    sb.append("propertyinfocase = TRUE ");
                } else {
                    sb.append("propertyinfocase = FALSE ");
                }
            }
            if (params.isUseIsOpen()) {
                if (params.isIsOpen()) {
                    sb.append("closingdate IS NULL ");
                } else {
                    sb.append("closingdate IS NOT NULL ");
                }
            }
            
        } else {
            sb.append("caseid = ? "); // will be param 1 with ID search
        }

        int paramCounter = 0;
            
        try {
            stmt = con.prepareStatement(sb.toString());

            if (!params.isBobID_ctl()) {
                if (params.isMuni_ctl()) {
                    stmt.setInt(++paramCounter, params.getMuni_val().getMuniCode());
                }
                if (params.isDate_startEnd_ctl()) {
                    stmt.setTimestamp(++paramCounter, params.getStartDate_val_SQLDate());
                    stmt.setTimestamp(++paramCounter, params.getEndDate_val_SQLDate());
                }
                if (params.isUseCasePhase()) {
                    stmt.setString(++paramCounter, params.getCasePhase().name());
                }

                if (params.isUseCaseStage() && !params.isUseCasePhase()) {
                    List<CasePhase> phList = params.getCaseStageAsPhaseList();
                    if(phList != null){
                        for(CasePhase cp : phList){
                            stmt.setString(++paramCounter, cp.name());
                        }
                    }
                }

                if (params.isUseProperty()) {
                    if(params.getProperty() != null){
                        stmt.setInt(++paramCounter, params.getProperty().getPropertyID());
                    }
                }

                if (params.isUseCaseManager()) {
                    if(params.getCaseManagerUser() != null){
                        stmt.setInt(++paramCounter, params.getCaseManagerUser().getUserID());
                    }
                }
            } else {
                stmt.setInt(++paramCounter, params.getBobID_val());
            }

            rs = stmt.executeQuery();

            int counter = 0;
            int maxResults;
            if (params.isLimitResultCount_ctl()) {
                maxResults = 100;
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
     * Generates a CECase without the big, fat lists
     * @param ceCaseID
     * @return
     * @throws IntegrationException 
     */
    public CECaseBase getCECaseBase(int ceCaseID) throws IntegrationException, CaseLifecycleException{
        String query = "SELECT caseid, cecasepubliccc, property_propertyid, propertyunit_unitid, \n" +
            "            login_userid, casename, casephase, originationdate, closingdate, \n" +
            "            creationtimestamp, notes, paccenabled, allowuplinkaccess \n" +
            "  FROM public.cecase WHERE caseid = ?;";
        ResultSet rs = null;
        CaseCoordinator cc = getCaseCoordinator();
        PreparedStatement stmt = null;
        Connection con = null;
        CECaseBase c = null;
        
        try {
            
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ceCaseID);
            //System.out.println("CaseIntegrator.getCECase| sql: " + stmt.toString());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                c = generateCECaseNoLists(rs);
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
     * Primary retrieval method for CECase objects from the DB
     * @param ceCaseID
     * @return
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.CaseLifecycleException 
     */
    public CECase getCECase(int ceCaseID) throws IntegrationException, CaseLifecycleException{
        if(ceCaseID == 0){
            throw new IntegrationException("Cannot get a case with ID 0");
        } else {
            System.out.println("CaseIntegrator.getCECase | getting case with id: " + ceCaseID);
        }
        CaseCoordinator cc = getCaseCoordinator();
        String query = "SELECT caseid, cecasepubliccc, property_propertyid, propertyunit_unitid, \n" +
            "            login_userid, casename, casephase, originationdate, closingdate, \n" +
            "            creationtimestamp, notes, paccenabled, allowuplinkaccess \n" +
            "  FROM public.cecase WHERE caseid = ?;";
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Connection con = null;
        CECase cse = null;
        
        try {
            
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ceCaseID);
            //System.out.println("CaseIntegrator.getCECase| sql: " + stmt.toString());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                CECaseBase baseCase = generateCECaseNoLists(rs);
                cse = generateCECase(baseCase);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get cecase by id", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        // send the case to the coordinator for the setting of casephase and such before returning
        if(cse != null){
            return cc.configureCECase(cse);
        }
        else return cse;
    }
    
    /**
     * Internal populator for CECase objects with lots of data
     * 
     * @param caseBare
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
    private CECase generateCECase(CECaseBase caseBare) throws SQLException, IntegrationException{
        EventIntegrator ei = getEventIntegrator();
        CitationIntegrator ci = getCitationIntegrator();
        ViolationIntegrator cvi = getCodeViolationIntegrator();
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        
        // Wrap our base class in the subclass wrapper--an odd design structure, indeed
        CECase cse = new CECase(caseBare);

        // *** POPULATE LISTS OF EVENTS, NOTICES, CITATIONS, AND VIOLATIONS ***
        cse.setCompleteEventList(ei.getEventsByCaseID(cse.getCaseID()));
        cse.setNoticeList(cvi.novGetList(cse));
        cse.setCitationList(ci.getCitations(cse));
        cse.setViolationList(cvi.getCodeViolations(cse.getCaseID()));
        cse.setCeActionRequestList(ceari.getCEActionRequestListByCase(cse.getCaseID()));
        
        //TODO NADGIT - integrate Fee functionality
        cse.setFeeList(new ArrayList<MoneyCECaseFeeAssigned>());
        cse.setPaymentList(new ArrayList<MoneyCECaseFeePayment>());
        
        return cse;
    }
    
    /**
     * Internal populator for CECaseBase objects
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
     private CECaseBase generateCECaseNoLists(ResultSet rs) throws SQLException, IntegrationException{
        PropertyIntegrator pi = getPropertyIntegrator();
        UserIntegrator ui = getUserIntegrator();
        SystemIntegrator si = getSystemIntegrator();
        
        int ceCaseID = rs.getInt("caseid");
        if(ceCaseID == 0){
            throw new IntegrationException("cannot generate case with ID 0");
        }
        
        CECaseBase c = new CECaseBase();

        c.setCaseID(ceCaseID);
        c.setPublicControlCode(rs.getInt("cecasepubliccc"));
        c.setProperty(pi.getProperty(rs.getInt("property_propertyid")));
        c.setPropertyUnit(null); // change when units are integrated

        c.setCaseManager(ui.getUser(rs.getInt("login_userid")));

        c.setCaseName(rs.getString("casename"));
        
        
        // let business logic in coordinators set the icon
//        CasePhase cp = CasePhase.valueOf(rs.getString("casephase"));
//        c.setCasePhase(cp);
//        c.setCasePhaseIcon(si.getIcon(cp));

        c.setOriginationDate(rs.getTimestamp("originationdate")
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        c.setOriginiationDatePretty(getPrettyDate(c.getOriginationDate()));

        if(rs.getTimestamp("closingdate") != null){
            c.setClosingDate(rs.getTimestamp("closingdate")
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            c.setClosingDatePretty(getPrettyDate(c.getClosingDate()));

        }
        c.setNotes(rs.getString("notes"));
        if(rs.getTimestamp("creationtimestamp") != null){
            c.setCreationTimestamp(rs.getTimestamp("creationtimestamp")
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }

        c.setPaccEnabled(rs.getBoolean("paccenabled"));
        c.setAllowForwardLinkedPublicAccess(rs.getBoolean("allowuplinkaccess"));

        return c;
    }
    
     /**
      * First gen search method to be deprecated in Beta
      * @param pacc
      * @return
      * @throws IntegrationException
      * @throws CaseLifecycleException 
      */
    public List<CECase> getCECasesByPACC(int pacc) throws IntegrationException, CaseLifecycleException{
        
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
     * Insertion point for CECase objects; must be called by Coordinator who checks 
     * logic before sending to the DB. This method only copies from the passed in CECase
     * into the SQL INSERT
     * 
     * @param ceCase
     * @return
     * @throws IntegrationException
     * @throws CaseLifecycleException 
     */
    public CECase insertNewCECase(CECase ceCase) throws IntegrationException, CaseLifecycleException{
        
        String query = "INSERT INTO public.cecase(\n" +
                        "            caseid, cecasepubliccc, property_propertyid, propertyunit_unitid, \n" +
                        "            login_userid, casename, casephase, originationdate, closingdate, \n" +
                        "            notes, creationTimestamp) \n" +
                        "    VALUES (DEFAULT, ?, ?, ?, \n" +
                        "            ?, ?, CAST (? as casephase), ?, ?, \n" +
                        "            ?, now());";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int insertedCaseID = 0;
        CECase freshlyInsertedCase = null;
        Connection con = null;
        
        try {
            
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ceCase.getPublicControlCode());
            stmt.setInt(2, ceCase.getProperty().getPropertyID());
            if(ceCase.getPropertyUnit() != null) {
                stmt.setInt(3, ceCase.getPropertyUnit().getUnitID());
            } else { stmt.setNull(3, java.sql.Types.NULL); }
            
            stmt.setInt(4, ceCase.getCaseManager().getUserID());
            stmt.setString(5, ceCase.getCaseName());
            stmt.setString(6, ceCase.getCasePhase().toString());
            stmt.setTimestamp(7, java.sql.Timestamp
                    .valueOf(ceCase.getOriginationDate()));
            // closing date
            stmt.setNull(8, java.sql.Types.NULL); 
            
            stmt.setString(9, ceCase.getNotes());
            
            System.out.println("CaseIntegrator.insertNewCase| sql: " + stmt.toString());
            
            stmt.execute();
            
            String retrievalQuery = "SELECT currval('cecase_caseID_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                insertedCaseID = rs.getInt(1);
            }
            
            freshlyInsertedCase = getCECase(insertedCaseID);
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Case Integrator: cannot insert new case, sorry", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return freshlyInsertedCase;
        
    }
    
    /**
     * Updates the values in the CECase in the DB but does NOT
 edit the data in connected tables, namely CodeViolation, EventCECase, and Person
 Use calls to other add methods in this class for adding additional
 violations, events, and people to a CE case.
     * 
     * @param ceCase the case to updated, with updated member variables
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void updateCECaseMetadata(CECase ceCase) throws IntegrationException{
        String query =  "UPDATE public.cecase\n" +
                        "   SET cecasepubliccc=?, \n" +
                        "       casename=?, originationdate=?, closingdate=?, notes=?, \n" +
                        " paccenabled=?, allowuplinkaccess=? " +
                        " WHERE caseid=?;";
        PreparedStatement stmt = null;
        Connection con = null;
        
        try {
            
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ceCase.getPublicControlCode());
            stmt.setString(2, ceCase.getCaseName());
            stmt.setTimestamp(3, java.sql.Timestamp
                    .valueOf(ceCase.getOriginationDate()));
            if(ceCase.getClosingDate() != null){
                stmt.setTimestamp(4, java.sql.Timestamp
                        .valueOf(ceCase.getClosingDate()));
                
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            stmt.setString(5, ceCase.getNotes());
            stmt.setBoolean(6, ceCase.isPaccEnabled());
            stmt.setBoolean(7, ceCase.isAllowForwardLinkedPublicAccess());
            stmt.setInt(8, ceCase.getCaseID());
            stmt.execute();
            
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot udpate case due to a database storage issue", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    public void deleteCECase(CECase cecase){
        
        
    }
    
    /**
     * The calling method is responsible for setting the new case phase
     * This is just a plain old update operation. The CaseCoordinator is responsible
     * for creating a case phase change event for logging purposes
     * @param ceCase
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void changeCECasePhase(CECase ceCase) throws IntegrationException{
        String query = "UPDATE public.cecase\n" +
                    "   SET casephase= CAST (? AS casephase)\n" +
                    " WHERE caseid = ?;";
        Connection con = null;
        PreparedStatement stmt = null;

         try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setString(1, ceCase.getCasePhase().toString());
            stmt.setInt(2, ceCase.getCaseID());
            stmt.executeUpdate();
             
        } catch (SQLException ex) { 
             System.out.println(ex.toString());
             throw new IntegrationException("Error updating case phase", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public List<Integer> getCECaseHistoryList(int userID) 
            throws IntegrationException, CaseLifecycleException{
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
     
}