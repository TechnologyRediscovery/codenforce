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
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.Property;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.ArrayList;

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
    
    public ArrayList getCECasesByProp(Property p) throws IntegrationException{
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
            throw new IntegrationException("Cannot search for person", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return caseList;
    }
    
    public ArrayList getOpenCECases(int muniCode) throws IntegrationException{
        
        ArrayList<CECase> caseList = new ArrayList();
        String query = "SELECT \n" +
            "  caseid, \n" +
            "  municipality.municode\n" +
            "FROM \n" +
            "  public.cecase, \n" +
            "  public.property, \n" +
            "  public.municipality\n" +
            "WHERE \n" +
            "  cecase.property_propertyid = property.propertyid AND\n" +
            "  property.municipality_municode = municipality.municode AND\n" +
            "  municipality.municode = ? AND casephase <> 'Closed'::casephase;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, muniCode);
            //System.out.println("CaseIntegrator.| sql: " + stmt.toString());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                caseList.add(getCECase(rs.getInt("caseid")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot search for person", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return caseList;
    }
    public ArrayList getCECaseHistory(int muniCode) throws IntegrationException{
        
        ArrayList<CECase> caseList = new ArrayList();
        String query = "SELECT \n" +
            "  caseid, \n" +
            "  municipality.municode\n" +
            "FROM \n" +
            "  public.cecase, \n" +
            "  public.property, \n" +
            "  public.municipality\n" +
            "WHERE \n" +
            "  cecase.property_propertyid = property.propertyid AND\n" +
            "  property.municipality_municode = municipality.municode AND\n" +
            "  municipality.municode = ? AND casephase = 'Closed'::casephase;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, muniCode);
            //System.out.println("CaseIntegrator.| sql: " + stmt.toString());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                caseList.add(getCECase(rs.getInt("caseid")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot search for person", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return caseList;
    }
    
    public CECase getCECase(int ceCaseID) throws IntegrationException{
        PropertyIntegrator pi = getPropertyIntegrator();
        UserIntegrator ui = getUserIntegrator();
        EventIntegrator ei = getEventIntegrator();
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        String query = "SELECT caseid, cecasepubliccc, property_propertyid, propertyunit_unitid, \n" +
            "       login_userid, casename, casephase, originationdate, closingdate, \n" +
            "       notes, creationtimestamp \n" +
            "  FROM public.cecase WHERE caseid = ?;";
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Connection con = null;
        CECase c = new CECase();
        
        try {
            
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ceCaseID);
            //System.out.println("CaseIntegrator.getCECase| sql: " + stmt.toString());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                c.setCaseID(rs.getInt("caseid"));
                c.setPublicControlCode(rs.getInt("cecasepubliccc"));
                c.setProperty(pi.getProperty(rs.getInt("property_propertyid")));
                c.setPropertyUnit(null); // change when units are integrated
                
                c.setUser(ui.getUser(rs.getInt("login_userid")));
                
                // big list additions here
                c.setEventList(ei.getEventsByCaseID(ceCaseID));
                c.setViolationList(cvi.getCodeViolations(ceCaseID));
                
                
                c.setCaseName(rs.getString("casename"));
                c.setCasePhase(CasePhase.valueOf(rs.getString("casephase")));
                
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
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot search for person", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return c;
    }
    

    public CECase insertNewCECase(CECase ceCase) throws IntegrationException{
        
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
            
            stmt.setInt(4, ceCase.getUser().getUserID());
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
 edit the data in connected tables, namely CodeViolation, EventCase, and Person
 Use calls to other add methods in this class for adding additional
 violations, events, and people to a CE case.
     * 
     * @param cecase the case to updated, with updated member variables
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void updateCECase(CECase cecase) throws IntegrationException{
        String query =  "UPDATE public.cecase\n" +
                        "   SET cecasepubliccc=?, \n" +
                        "       casename=?, originationdate=?, closingdate=?, notes=?\n" +
                        " WHERE caseid=?;";
        PreparedStatement stmt = null;
        Connection con = null;
        
        try {
            
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cecase.getPublicControlCode());
            stmt.setString(2, cecase.getCaseName());
            stmt.setTimestamp(3, java.sql.Timestamp
                    .valueOf(cecase.getOriginationDate()));
            if(cecase.getClosingDate() != null){
                stmt.setTimestamp(4, java.sql.Timestamp
                        .valueOf(cecase.getClosingDate()));
                
            }
            stmt.setString(5, cecase.getNotes());
            stmt.setInt(6, cecase.getCaseID());
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
    
    
    
}
