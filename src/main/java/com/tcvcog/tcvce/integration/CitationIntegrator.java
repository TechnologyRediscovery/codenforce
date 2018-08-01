/*
 * Copyright (C) 2018 Turtle Creek Valley
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
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CitationStatus;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.Property;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class CitationIntegrator extends BackingBeanUtils implements Serializable{
    
    
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
            stmt1.setInt(2, citation.getStatus().getCitationStatusID());
            stmt1.setInt(3, citation.getOrigin_courtentity().getCourtEntityID());
            stmt1.setInt(4, citation.getUserOwner().getUserID());
            stmt1.setTimestamp(5, java.sql.Timestamp.valueOf(citation.getDateOfRecord()));
            stmt1.setBoolean(6, citation.isIsActive());
            stmt1.setString(7, citation.getNotes());
            
            System.out.println("CitationIntegrator.insertCitation| citation insert sql: " + stmt1.toString());
            stmt1.execute();
            
            rs = stmtCID.executeQuery();
            int lastCID = 0;
            while(rs.next()){
                 lastCID= rs.getInt(1);
            }
            System.out.println("CitationIntegrator.insertCitation | last citation ID: " + lastCID);
            ListIterator<CodeViolation> li = citation.getViolationList().listIterator();
            
            while(li.hasNext()){
                stmt2.setInt(1, lastCID);
                stmt2.setInt(2, (int) li.next().getViolationID());
                System.out.println("CitationIntegreator.insertCitation | citationViolation insert SQL: " + stmt2.toString());
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
    
    public Citation getCitationByID(int id) throws IntegrationException{

        String query = "SELECT citationid, citationno, status_statusid, origin_courtentity_entityid, \n" +
"       login_userid, dateofrecord, transtimestamp, isactive, notes FROM public.citation WHERE citationid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Citation c = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, id);
            System.out.println("Code.getEventCategory| sql: " + stmt.toString());
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
    
    public ArrayList<Citation> getCitationsByProperty(Property prop) throws IntegrationException{
        //this doesn't work anymore since citations don't know about cases, we have to go through citationViolation
        // codeviolations know about cases
        
        String query =  "SELECT citationid, citationno, status_statusid, origin_courtentity_entityid, \n" +
"       login_userid, dateofrecord, transtimestamp, isactive, notes " +
                        "FROM public.citation 	INNER JOIN public.cecase ON cecase.caseid = citation.caseid\n" +
                        "                               INNER JOIN public.property ON cecase.property_propertyID = property.propertyID\n" +
                        "WHERE propertyID=?; ";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<Citation> citationList = new ArrayList();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, prop.getPropertyID());
            System.out.println("CitationIntegrator.getCitationsByProperty| sql: " + stmt.toString());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                citationList.add(generateCitationFromRS(rs));
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
    
    public ArrayList<Citation> getCitationsByCase(CECase ceCase) throws IntegrationException{
            
        String query =  "SELECT DISTINCT ON (citationID) citation.citationid, codeviolation.cecase_caseID FROM public.citationviolation 	\n" +
                        "	INNER JOIN public.citation ON citation.citationid = citationviolation.citation_citationid\n" +
                        "	INNER JOIN public.codeviolation on codeviolation.violationid = citationviolation.codeviolation_violationid\n" +
                        "	INNER JOIN public.cecase ON cecase.caseid = codeviolation.cecase_caseID\n" +
                        "	WHERE codeviolation.cecase_caseID=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<Citation> citationList = new ArrayList();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ceCase.getCaseID());
            System.out.println("CitationIntegrator.getCitationsByCase| sql: " + stmt.toString());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                citationList.add(getCitationByID(rs.getInt("citationid")));
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
    
    private ArrayList<CodeViolation> getCodeViolationsByCitation(Citation cid) throws IntegrationException{
        
        String query =  "SELECT codeviolation.violationid FROM public.citationviolation 	\n" +
                        "	INNER JOIN public.citation ON citation.citationid = citationviolation.citation_citationid\n" +
                        "	INNER JOIN public.codeviolation on codeviolation.violationid = citationviolation.codeviolation_violationid\n" +
                        "	WHERE citation.citationid=?;";
        Connection con = getPostgresCon();
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<CodeViolation> violationList = new ArrayList<>();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cid.getCitationID());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                violationList.add(cvi.getCodeViolationByViolationID(rs.getInt("violationid")));
                
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch code violation by ID, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return violationList;
    }
    
    
    private Citation generateCitationFromRS(ResultSet rs) throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        Citation c = new Citation();
        try {
            c.setCitationID(rs.getInt("citationID"));
            c.setCitationNo(rs.getString("citationNo"));
            c.setStatus(getCitationStatus(rs.getInt("status_statusid")));
            c.setOrigin_courtentity(cei.getCourtEntity(rs.getInt("origin_courtentity_entityID")));
            c.setUserOwner(ui.getUser(rs.getInt("login_userID")));
            c.setDateOfRecord(rs.getTimestamp("dateOfRecord").toLocalDateTime());
            c.setTimeStamp(rs.getTimestamp("transTimeStamp").toLocalDateTime());
            c.setIsActive(rs.getBoolean("isActive"));
            c.setNotes(rs.getString("notes"));
            c.setViolationList(getCodeViolationsByCitation(c));
        } catch (SQLException | IntegrationException ex) {
            System.out.println(ex);
            throw new IntegrationException("Unable to build citation from RS", ex);
        }
        return c;
    }
    
    public void updateCitation(Citation citation) throws IntegrationException{
        String query =  "UPDATE public.citation\n" +
                        "   SET citationno=?, status_statusid=?, origin_courtentity_entityid=?, \n" +
                        "       login_userid=?, dateofrecord=?, transtimestamp=now(), isactive=?, \n" +
                        "       notes=?\n" +
                        " WHERE citationid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, citation.getCitationNo());
            stmt.setInt(2, citation.getStatus().getCitationStatusID());
            stmt.setInt(3, citation.getOrigin_courtentity().getCourtEntityID());
            stmt.setInt(4, citation.getUserOwner().getUserID());
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(citation.getDateOfRecord()));
            stmt.setBoolean(6, citation.isIsActive());
            stmt.setString(7, citation.getNotes());
            stmt.setInt(8, citation.getCitationID());
            
            
            System.out.println("CitationIntegrator.updateCitation | sql: " + stmt.toString());
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
    
    public void deleteCitation(Citation citation) throws IntegrationException{
        String query =  "DELETE FROM public.citation\n" +
                        " WHERE citationid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, citation.getCitationID());
            
            System.out.println("CitationIntegrator.updateCitation | sql: " + stmt.toString());
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
    
    public CitationStatus getCitationStatus(int statusID) throws IntegrationException{
            
        String query =  "SELECT statusid, statusname, description from citationStatus where statusid=?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        CitationStatus cs = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, statusID);
            System.out.println("CitationIntegrator.getCitationStatus| sql: " + stmt.toString());
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
    
    public ArrayList<CitationStatus> getFullCitationStatusList() throws IntegrationException{
        String query =  "SELECT statusid, statusname, description from citationStatus;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<CitationStatus> csList = new ArrayList<>();
        
        try {
            stmt = con.prepareStatement(query);
            System.out.println("CitationIntegrator.getFullCitationStatusList | sql: " + stmt.toString());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                csList.add(generateCitationStatus(rs));
                
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
    
    public CitationStatus generateCitationStatus(ResultSet rs) throws IntegrationException{
        CitationStatus cs = new CitationStatus();
        try {
            cs.setCitationStatusID(rs.getInt("statusid"));
            cs.setStatusTitle(rs.getString("statusname"));
            cs.setDescription(rs.getString("description"));
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Cannot Generate citation status object, sorry", ex);
        }
        return cs;
    }
    
    public void insertCitationStatus(CitationStatus cs) throws IntegrationException{
        
        
        String query =  "INSERT INTO public.citationstatus(\n" +
                        "            statusid, statusname, description)\n" +
                        "    VALUES (DEFAULT, ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, cs.getStatusTitle());
            stmt.setString(2, cs.getDescription());
            
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert citation into database, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    public void deleteCitationStatus(CitationStatus cs) throws IntegrationException{
        
        String query = "DELETE FROM public.citationstatus WHERE statusid=?";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cs.getCitationStatusID());
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
    
    public void updateCitationStatus(CitationStatus cs) throws IntegrationException{
        
        String query =  "UPDATE public.citationstatus\n" +
                        "   SET statusname=?, description=?\n" +
                        " WHERE statusid=?;";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, cs.getStatusTitle());
            stmt.setString(1, cs.getDescription());
            
            stmt.execute();
            
            
            getFacesContext().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Citation no. " + cs.getCitationStatusID() + " has been updated", ""));

            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert citation into database, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    } // close method
} // close class
