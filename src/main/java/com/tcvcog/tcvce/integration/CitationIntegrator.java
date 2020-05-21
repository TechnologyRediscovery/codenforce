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
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CitationStatus;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.Property;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.faces.application.FacesMessage;

/**
 *
 * @author ellen bascomb of apt 31y
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
    
    public Citation getCitation(int id) throws IntegrationException{

        String query = "SELECT citationid, citationno, status_statusid, origin_courtentity_entityid, \n" +
                        "login_userid, dateofrecord, transtimestamp, isactive, notes FROM public.citation WHERE citationid=?;";
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
    
    public List<Citation> getCitations(Property prop) throws IntegrationException{
        //this doesn't work anymore since citations don't know about cases, we have to go through citationViolation
        // codeviolations know about cases
        
        String query =  "SELECT citationid, citationno, status_statusid, origin_courtentity_entityid, \n" +
                        "login_userid, dateofrecord, transtimestamp, isactive, notes " +
                        "FROM public.citation 	INNER JOIN public.cecase ON cecase.caseid = citation.caseid \n" +
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
    
    private List<CodeViolation> getCodeViolations(Citation cid) throws IntegrationException{
        
        String query =  "SELECT codeviolation_violationid FROM public.citationviolation 	\n" +
                        "	INNER JOIN public.citation ON citation.citationid = citationviolation.citation_citationid\n" +
                        "	INNER JOIN public.codeviolation on codeviolation.violationid = citationviolation.codeviolation_violationid\n" +
                        "	WHERE citation.citationid=?;";
        Connection con = getPostgresCon();
        ViolationIntegrator cvi = getCodeViolationIntegrator();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<CodeViolation> violationList = new ArrayList<>();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cid.getCitationID());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                violationList.add(cvi.getCodeViolation(rs.getInt("codeviolation_violationid")));
                
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
            cit.setViolationList(getCodeViolations(cit));
        } catch (SQLException | IntegrationException ex) {
            System.out.println(ex);
            throw new IntegrationException("Unable to build citation from RS", ex);
        }
        return cit;
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
            stmt.setInt(2, citation.getStatus().getStatusID());
            stmt.setInt(3, citation.getOrigin_courtentity().getCourtEntityID());
            stmt.setInt(4, citation.getUserOwner().getUserID());
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(citation.getDateOfRecord()));
            stmt.setBoolean(6, citation.isIsActive());
            stmt.setString(7, citation.getNotes());
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
    
    private CitationStatus generateCitationStatus(ResultSet rs) throws IntegrationException{
        CitationStatus cs = new CitationStatus();
        SystemIntegrator si = getSystemIntegrator();
        EventIntegrator ei = getEventIntegrator();
        CaseIntegrator ci = getCaseIntegrator();
        try {
            cs.setStatusID(rs.getInt("statusid"));
            cs.setStatusTitle(rs.getString("statusname"));
            cs.setDescription(rs.getString("description"));
            cs.setIcon(si.getIcon(rs.getInt("icon_iconid")));
            cs.setEditsAllowed(rs.getBoolean("editsforbidden"));
            cs.setPhaseChangeRule(ei.rules_getEventRuleAbstract(rs.getInt("eventrule_ruleid"), this));
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Cannot Generate citation status object, sorry", ex);
        }
        return cs;
    }
    
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
            stmt.setBoolean(4, cs.isEditsAllowed());
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
            stmt.setBoolean(4, cs.isEditsAllowed());
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
} // close class
