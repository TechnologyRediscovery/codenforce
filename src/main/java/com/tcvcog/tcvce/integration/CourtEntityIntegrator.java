/*
 * Copyright (C) 2018 Adam Gutonski
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
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CitationCodeViolationLink;
import com.tcvcog.tcvce.entities.CitationDocketRecord;
import com.tcvcog.tcvce.entities.CitationFilingType;
import com.tcvcog.tcvce.entities.CitationStatus;
import com.tcvcog.tcvce.entities.CitationStatusLogEntry;
import com.tcvcog.tcvce.entities.CourtEntity;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.ViolationStatusEnum;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;

/**
 *  Database integration methods for court related objects, the
 * two major families of which are CourtEntity objects and the entire
 * (happy) Citation family.
 * 
 * @author Adam Gutonski & Ellen Bascomb of Apartment 31Y
 */
public class CourtEntityIntegrator extends BackingBeanUtils implements Serializable {
    
    public CourtEntityIntegrator() {
        
    }
    
    public void updateCourtEntity(CourtEntity courtEntity) throws IntegrationException {
        String query =  "UPDATE public.courtentity\n" +
                        "   SET entityofficialnum=?, jurisdictionlevel=?, name=?, \n" +
                        "       address_street=?, address_city=?, address_zip=?, address_state=?, \n" +
                        "       county=?, phone=?, url=?, notes=?, judgename=?\n" +
                        " WHERE entityid=?;";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        System.out.println("CourtEntityIntegrator.updateCourtEntity | updated name: " + courtEntity.getCourtEntityName());
        
        try{
            stmt = con.prepareStatement(query);
            stmt.setString(1, courtEntity.getCourtEntityOfficialNum());
            stmt.setString(2, courtEntity.getJurisdictionLevel());
            stmt.setString(4, courtEntity.getCourtEntityName());
            
            stmt.setString(5, courtEntity.getAddressStreet());
            stmt.setString(6, courtEntity.getAddressCity());
            stmt.setString(7, courtEntity.getAddressZip());
            stmt.setString(8, courtEntity.getAddressState());
            
            stmt.setString(9, courtEntity.getAddressCounty());
            stmt.setString(10, courtEntity.getPhone());
            stmt.setString(11, courtEntity.getUrl());
            stmt.setString(12, courtEntity.getNotes());
            stmt.setString(13, courtEntity.getJudgeName());
            
            stmt.setInt(14, courtEntity.getCourtEntityID());
            System.out.println("CourtEntityIntegrator.updateCourtEntity | sql: " + stmt.toString());
            stmt.executeUpdate();
        } catch (SQLException ex){
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update Court Entity", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
        
    }
    
    /**
     * Retrieves all court entities
     * @return
     * @throws IntegrationException 
     */
    public List<CourtEntity> getCourtEntityList() throws IntegrationException {
            String query = "SELECT entityid FROM public.courtentity;";
            Connection con = getPostgresCon();
            ResultSet rs = null;
            PreparedStatement stmt = null;
            ArrayList<CourtEntity> ceList = new ArrayList();
        
        try {
            stmt = con.prepareStatement(query);
            System.out.println("");
            rs = stmt.executeQuery();
            while(rs.next()){
                ceList.add(getCourtEntity(rs.getInt("entityid")));
            }
            
            } catch (SQLException ex) {
                System.out.println(ex.toString());
                throw new IntegrationException("cannot generate court entity list", ex);
            } finally {
                if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
                if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
                if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
            }
            return ceList;   
    }
    
    /**
     * Retrieves court entities by municode
     * @param muniCode
     * @return
     * @throws IntegrationException 
     */
       public List<CourtEntity> getCourtEntityList(int muniCode) throws IntegrationException {
            String query = "SELECT courtentity_entityid FROM municourtentity WHERE muni_municode=?;";
            Connection con = getPostgresCon();
            ResultSet rs = null;
            PreparedStatement stmt = null;
            List<CourtEntity> ceList = new ArrayList();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, muniCode);
            rs = stmt.executeQuery();
            while(rs.next()){
                ceList.add(getCourtEntity(rs.getInt("courtentity_entityid")));
            }
            
            } catch (SQLException ex) {
                System.out.println(ex.toString());
                throw new IntegrationException("Cannot generate court entity list", ex);
            } finally {
                if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
                if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
                if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
            }
            if(ceList.isEmpty()){
                ceList = getCourtEntityList();
            }
        
            return ceList;   
    }
    
    
    
    public void insertCourtEntity(CourtEntity courtEntity) throws IntegrationException {
        String query =  "INSERT INTO public.courtentity(\n" +
                        "            entityid, entityofficialnum, jurisdictionlevel, name, address_street, \n" +
                        "            address_city, address_zip, address_state, county, phone, url, \n" +
                        "            notes, judgename)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, ?, ?, \n" +
                        "            ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, courtEntity.getCourtEntityOfficialNum());
            stmt.setString(2, courtEntity.getJurisdictionLevel());
            stmt.setString(4, courtEntity.getCourtEntityName());
            stmt.setString(5, courtEntity.getAddressStreet());
            stmt.setString(6, courtEntity.getAddressCity());
            
            stmt.setString(7, courtEntity.getAddressZip());
            stmt.setString(8, courtEntity.getAddressState());
            stmt.setString(9, courtEntity.getAddressCounty());
            stmt.setString(10, courtEntity.getPhone());
            stmt.setString(11, courtEntity.getUrl());
            
            stmt.setString(12, courtEntity.getNotes());
            stmt.setString(13, courtEntity.getJudgeName());
            
            System.out.println("CourtEntityIntegrator.courtEntityIntegrator | sql: ");
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert Court Entity", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    public CourtEntity getCourtEntity(int entityID) throws IntegrationException{
        
         String query = "SELECT entityid, entityofficialnum, jurisdictionlevel, name, address_street, \n" +
                        "       address_city, address_zip, address_state, county, phone, url, \n" +
                        "       notes, judgename\n" +
                        "  FROM public.courtentity WHERE entityID=?;";
            Connection con = getPostgresCon();
            ResultSet rs = null;
            PreparedStatement stmt = null;
            CourtEntity ce = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, entityID);
            rs = stmt.executeQuery();
            while(rs.next()){
                ce = generateCourtEntity(rs);
            }
            
            } catch (SQLException ex) {
                System.out.println(ex.toString());
                throw new IntegrationException("Cannot generate court entity due to SQL error", ex);
            } catch (IntegrationException ex) {
                throw new IntegrationException("Cannot generate court entity", ex);
        } finally {
                if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
                if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
                if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
            }
            return ce;   
        
        
    }
    
    public void deleteCourtEntity(CourtEntity courtEntity) throws IntegrationException {
        String query = "DELETE FROM public.courtentity\n" +
                        " WHERE entityid= ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, courtEntity.getCourtEntityID());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete court entity--probably because another"
                    + "part of the database has a reference item.", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    private CourtEntity generateCourtEntity(ResultSet rs) throws IntegrationException {
        CourtEntity newCourtEntity = new CourtEntity();
    
        try {
            newCourtEntity.setCourtEntityID(rs.getInt("entityid"));
            newCourtEntity.setCourtEntityOfficialNum(rs.getString("entityofficialnum"));
            newCourtEntity.setJurisdictionLevel(rs.getString("jurisdictionlevel"));
            newCourtEntity.setCourtEntityName(rs.getString("name"));
            newCourtEntity.setAddressStreet(rs.getString("address_street"));
            
            newCourtEntity.setAddressCity(rs.getString("address_city"));
            newCourtEntity.setAddressZip(rs.getString("address_zip"));
            newCourtEntity.setAddressState(rs.getString("address_state"));
            newCourtEntity.setAddressCounty(rs.getString("county"));
            newCourtEntity.setPhone(rs.getString("phone"));
            newCourtEntity.setUrl(rs.getString("url"));
            
            newCourtEntity.setNotes(rs.getString("notes"));
            newCourtEntity.setJudgeName(rs.getString("judgename"));
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error generating Court Entity from result set", ex);
        }
        
        return newCourtEntity;
    }
    
    
     
     // ***********************************************************************
     // **                  CITATIONS                                        **
     // ***********************************************************************
     
     /**
      * Creates a new record in the citation table and connects applicable
      * code violations; TODO: the violation connection should probably be separated out
      * 
      * @param citation
     * @return the new citation's record database primary key
      * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
      */
    public int insertCitation(Citation citation) throws IntegrationException, BObStatusException{

        // Updated for citation overhaul as aprt of humanization
        String insert =  "INSERT INTO public.citation(\n" +
                        "            citationid, citationno, origin_courtentity_entityid, \n" +
                        "            login_userid, dateofrecord, isactive, notes, \n" +
                        "            officialtext, createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, \n" +
                        "            deactivatedts, deactivatedby_userid, filingtype_typeid)\n" +
                        "    VALUES (DEFAULT, ?, ?, \n" + //1-2
                        "            ?, ?, TRUE, ?, \n" + //3-5
                        "            ?, now(), ?, now(), ?, \n" + // 6-8
                        "            NULL, NULL, ?);"; //9
        
        Connection con = getPostgresCon();
        PreparedStatement stmt1 = null;
        PreparedStatement stmtCID = null;
        ResultSet rs = null;
        
        int lastCID = 0;
        
        try {
            
            stmt1 = con.prepareStatement(insert);
            stmtCID = con.prepareStatement("SELECT currval('citation_citationid_seq');");
            
            stmt1.setString(1, citation.getCitationNo());
            
            if(citation.getOrigin_courtentity() != null){
                stmt1.setInt(2, citation.getOrigin_courtentity().getCourtEntityID());
            } else {
                stmt1.setNull(2, java.sql.Types.NULL);
            }
            
            if(citation.getFilingOfficer() != null){
                stmt1.setInt(3, citation.getFilingOfficer().getUserID());
            } else {
                stmt1.setNull(3, java.sql.Types.NULL);
            }
            
            if(citation.getDateOfRecord() != null){
                stmt1.setTimestamp(4, java.sql.Timestamp.valueOf(citation.getDateOfRecord()));
            } else {
                stmt1.setNull(4, java.sql.Types.NULL);
            }
            
            stmt1.setString(5, citation.getNotes());
            stmt1.setString(6, citation.getOfficialText());
            
            if(citation.getCreatedBy() != null){
                stmt1.setInt(7, citation.getCreatedBy().getUserID());
            } else {
                stmt1.setNull(7, java.sql.Types.NULL);
            }
            
            if(citation.getLastUpdatedBy()!= null){
                stmt1.setInt(8, citation.getLastUpdatedBy().getUserID());
            } else {
                stmt1.setNull(8, java.sql.Types.NULL);
            }
            
            if(citation.getFilingType() != null){
                stmt1.setInt(9, citation.getFilingType().getTypeID());
            } else {
                stmt1.setNull(9, java.sql.Types.NULL);
            }
            
            stmt1.execute();
            rs = stmtCID.executeQuery();
            
            while(rs.next()){
                 lastCID= rs.getInt(1);
            }
           
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert citation into database, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt1 != null) { try { stmt1.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return lastCID;
    }
    
    
    /**
     * Second half of the citation insert process: linking violations to their
     * parent citation
     * @param cit 
     */
    public void linkViolationsToCitation(Citation cit) throws IntegrationException{
        
        if(cit != null){
            

            String queryCitationViolationTable = "INSERT INTO public.citationviolation(\n" +
                            "            citationviolationid, citation_citationid, codeviolation_violationid, \n" +
                            "            createdts, lastupdatedts, status, createdby_userid, \n" +
                            "            lastupdatedby_userid, notes, linksource)\n" +
                            "    VALUES (DEFAULT, ?, ?, \n" +
                            "            now(), now(), ?, ?, \n" +
                            "            ?, ?, ?);";

            Connection con = getPostgresCon();
            PreparedStatement stmt = null;

            try {

                stmt = con.prepareStatement(queryCitationViolationTable);
                
                ListIterator<CitationCodeViolationLink> li = cit.getViolationList().listIterator();

                while(li.hasNext()){
                    CitationCodeViolationLink ccvl = li.next();
                    stmt.setInt(1, cit.getCitationID());
                    stmt.setInt(2, ccvl.getViolationID());
                    stmt.setString(3, ccvl.getCitVStatus().toString());
                    stmt.setInt(4, cit.getCreatedBy().getUserID());
                    stmt.setInt(5, cit.getLastUpdatedBy().getUserID());
                    stmt.setString(6, ccvl.getNotes());
                    if(ccvl.getLinkSource() != null){
                        stmt.setInt(7, ccvl.getLinkSource().getSourceid());
                    } else {
                        stmt.setNull(7, java.sql.Types.NULL);
                    }
                    
                    stmt.execute();
                } // close while over citaiton violations

            } catch (SQLException ex) {
                System.out.println(ex.toString());
                throw new IntegrationException("Unable to insert citation into database, sorry.", ex);

            } finally{
                 if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
                 if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            } // close finally
        } else {
            throw new IntegrationException("Cannot link violations to a citation NULL citation");
            
        }
        
    }

    
    /**
     * Extracts a list of citation ID values given a CodeViolation ID
     * @param violationID
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> getCitationIDsByViolation(int violationID) throws IntegrationException{
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
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public Citation getCitation(int id) throws IntegrationException, BObStatusException{

        String query = "SELECT citationid, citationno, origin_courtentity_entityid, \n" +
                        "       login_userid, dateofrecord, isactive, notes, \n" +
                        "       officialtext, createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, \n" +
                        "       deactivatedts, deactivatedby_userid, filingtype_typeid\n" +
                        "  FROM public.citation WHERE citationid=?;";
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
            throw new IntegrationException("CourtEntityIntegrator.getCitation(): cannot fetch citation, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return c;
    }
    
    /**
     * Retrieves a DocketNumber record from the DB
     * @param id
     * @return the fully-backed DocketREcord
     * @throws IntegrationException 
     */
    public CitationDocketRecord getCitationDocketRecord(int id) throws IntegrationException{

        String query =  "SELECT docketid, docketno, dateofrecord, courtentity_entityid, createdts, \n" +
                        "       createdby_userid, lastupdatedts, lastupdatedby_userid, deactivatedts, \n" +
                        "       deactivatedby_userid, notes, citation_citationid " +
                        "  FROM public.citationdocketno WHERE docketid = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        CitationDocketRecord cdr = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                cdr = generateCitationDocketRecord(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("CourtEntityIntegrator.getCitationDocketRecord: cannot fetch citationdocketrecord, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return cdr;
    }
    
    /**
     * Injects member values from a result set for a CitationDocketRecord
     * @param rs
     * @return 
     */
    private CitationDocketRecord generateCitationDocketRecord(ResultSet rs) throws SQLException, IntegrationException{
        SystemIntegrator si = getSystemIntegrator();
        
        CitationDocketRecord cdr = null;
        if(rs != null){
            try {
                cdr = new CitationDocketRecord();
                
                cdr.setDocketID(rs.getInt("docketid"));
                cdr.setDocketNumber(rs.getString("docketno"));
                if(rs.getTimestamp("dateofrecord") != null){
                    cdr.setDateOfRecord(rs.getTimestamp("dateofrecord").toLocalDateTime());
                }
                
                cdr.setCourtEntity(getCourtEntity(rs.getInt("courtentity_entityid")));
                cdr.setCitationID(rs.getInt("citation_citationid"));
                cdr.setNotes(rs.getString("notes"));
                
                si.populateTrackedFields(cdr, rs, false);
            } catch (BObStatusException ex) {
                throw new IntegrationException(ex.getMessage());
            }
        }
        
        return cdr;
    }
    
    /**
     * Extracts citation docket records by citation
     * @param cit
     * @return
     * @throws BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public List<CitationDocketRecord> getCitationDocketRecords(Citation cit) 
            throws BObStatusException, IntegrationException, IntegrationException{
        if(cit == null){
            throw new BObStatusException("Cannot get citation docket nos for null citation");
        }
        
        List<CitationDocketRecord> cdrl = new ArrayList<>();
        
        String query = "SELECT docketid FROM citationdocketno WHERE citation_citationid=? AND deactivatedts IS NULL ;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cit.getCitationID());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                cdrl.add(getCitationDocketRecord(rs.getInt("docketid")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot fetch dockets by citation, Sorry!", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return cdrl;
        
    }
    
    /**
     * Creates a new record in the citationdocketno table
     * @param cdr
     * @return the ID of the freshly inserted docket
     * @throws IntegrationException 
     */
    public int insertCitationDocket(CitationDocketRecord cdr) throws IntegrationException{
        if(cdr == null || cdr.getCitationID() == 0){
            throw new IntegrationException("Cannot insert citation docket with null docket record or citation ID of zero!");
        }
        
        String query =  "INSERT INTO public.citationdocketno(\n" +
                        "            docketid, docketno, dateofrecord, courtentity_entityid, createdts, \n" +
                        "            createdby_userid, lastupdatedts, lastupdatedby_userid, \n" +
                        "            citation_citationid)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, now(), \n" +
                        "            ?, now(), ?, \n" +
                        "            ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int freshDocketID = 0;
        
        try {
            stmt = con.prepareStatement(query);
            
            stmt.setString(1, cdr.getDocketNumber());
            
            if(cdr.getDateOfRecord() != null){
                stmt.setTimestamp(2, java.sql.Timestamp.valueOf(cdr.getDateOfRecord()));
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            
            if(cdr.getCourtEntity() != null){
                stmt.setInt(3, cdr.getCourtEntity().getCourtEntityID());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(cdr.getCreatedBy() != null){
                stmt.setInt(4, cdr.getCreatedBy().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            if(cdr.getLastUpdatedBy() != null){
                stmt.setInt(5, cdr.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            stmt.setInt(6, cdr.getCitationID());
            
            stmt.execute();
            
            String retrievalQuery = "SELECT currval('citationdocketno_docketid_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                 freshDocketID = rs.getInt(1);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert citation Docket into database, sorry.", ex);
            
        } finally{
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return freshDocketID;
    }
    
    /**
     * Updates a docket record
     * @param cdr
     * @throws IntegrationException 
     */
    public void updateCitationDocket(CitationDocketRecord cdr) throws IntegrationException{
        if(cdr == null || cdr.getCitationID() == 0){
            throw new IntegrationException("Cannot udate citation docket with null docket record or citation ID of zero!");
        }
        
        String query =  "UPDATE public.citationdocketno\n" +
                        "   SET docketno=?, dateofrecord=?, courtentity_entityid=?, \n" +
                        "       lastupdatedts=now(), lastupdatedby_userid=?, \n" +
                        "       citation_citationid=? \n" +
                        " WHERE docketid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = con.prepareStatement(query);
            
            stmt.setString(1, cdr.getDocketNumber());
            
            if(cdr.getDateOfRecord() != null){
                stmt.setTimestamp(2, java.sql.Timestamp.valueOf(cdr.getDateOfRecord()));
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            
            if(cdr.getCourtEntity() != null){
                stmt.setInt(3, cdr.getCourtEntity().getCourtEntityID());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(cdr.getLastUpdatedBy() != null){
                stmt.setInt(4, cdr.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            stmt.setInt(5, cdr.getCitationID());
            
            stmt.setInt(6, cdr.getDocketID());
            
            
            stmt.execute();
            
           
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert citation Docket into database, sorry.", ex);
            
        } finally{
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
    }
    
    /**
     * Sets the deactivation TS and deactivation UserID on a citation docket
     * 
     * @param cdr
     * @throws IntegrationException 
     */
    public void deactivateCitationDocket(CitationDocketRecord cdr) throws IntegrationException{
        if(cdr == null || cdr.getCitationID() == 0){
            throw new IntegrationException("Cannot insert citation docket with null docket record or citation ID of zero!");
        }
        
        String query =  "UPDATE public.citationdocketno\n" +
                        "   SET deactivatedts=now(), deactivatedby_userid=?, lastupdatedts=now(), lastupdatedby_userid=? \n" +
                        " WHERE docketid=?;";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = con.prepareStatement(query);
            
            if(cdr.getDeactivatedBy()!= null){
                stmt.setInt(1, cdr.getDeactivatedBy().getUserID());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            
            if(cdr.getLastUpdatedBy() != null){
                stmt.setInt(2, cdr.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            
            stmt.setInt(3, cdr.getDocketID());
            
            stmt.execute();
            
           
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to deactivate citation Docket in database, sorry.", ex);
            
        } finally{
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
    }
    
    
    /**
     * Utility method for retrieving a list of Citation objects
     * @param citIDList a list of citation IDs
     * @return list of Citations, will always return an instantiated List
     * @throws IntegrationException 
     */
    public List<Citation> getCitations(List<Integer> citIDList) throws IntegrationException, BObStatusException{
        
        List<Citation> citL = new ArrayList<>();
        if(citIDList != null && !citIDList.isEmpty()){
            for(Integer i: citIDList){
                citL.add(getCitation(i));
            }
        }
        return citL;
        
    }
    
    /**
     * Extracts a list of Citation IDs from the DB given a Property
     * @param prop
     * @return
     * @throws IntegrationException 
     */
    public List<Citation> getCitations(Property prop) throws IntegrationException, BObStatusException{
        //this doesn't work anymore since citations don't know about cases, we have to go through citationViolation
        // codeviolations know about cases
        
        String query =  "SELECT citationid FROM public.citation 	"
                        + "INNER JOIN public.cecase ON cecase.caseid = citation.caseid \n" +
                        "INNER JOIN public.property ON cecase.property_propertyID = property.propertyID \n" +
                        "WHERE propertyID=? AND citation.isactive = TRUE; ";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<Citation> citationList = new ArrayList();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, prop.getParcelKey());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                citationList.add(getCitation(rs.getInt("citationid")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch citations by property, sorries!", ex);
            
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
     * @return list of Citations IDs of citations that are active only by cecase
     * @throws IntegrationException 
     */
    public List<Integer> getCitations(CECase ceCase) throws IntegrationException{
        CaseCoordinator cc = getCaseCoordinator();
        String query =  "SELECT DISTINCT ON (citationID) citation.citationid, codeviolation.cecase_caseID FROM public.citationviolation 	\n" +
                        "	INNER JOIN public.citation ON citation.citationid = citationviolation.citation_citationid\n" +
                        "	INNER JOIN public.codeviolation on codeviolation.violationid = citationviolation.codeviolation_violationid\n" +
                        "	INNER JOIN public.cecase ON cecase.caseid = codeviolation.cecase_caseID\n" +
                        "	WHERE codeviolation.cecase_caseID=? AND citation.isactive=TRUE;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<Integer> citationIDList = new ArrayList();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ceCase.getCaseID());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                citationIDList.add(rs.getInt("citationid"));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("CourtEntityIntegrator.getCitations(CECase): cannot fetch citations by CECase, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return citationIDList;
    }
    
    /**
     * Extracts a list of CodeViolation IDs from DB given a citation ID
     * @param cid
     * @return
     * @throws IntegrationException 
     */
    public List<CitationCodeViolationLink> getCodeViolationsByCitation(Citation cid) throws IntegrationException{
        
        String query =  "SELECT citationviolationid, citation_citationid, codeviolation_violationid, \n" +
                        "       createdts, lastupdatedts, deactivatedts, status, createdby_userid, \n" +
                        "       lastupdatedby_userid, deactivatedby_userid, notes, source_sourceid "
                + "FROM citationviolation WHERE citation_citationid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<CitationCodeViolationLink> cvll = new ArrayList<>();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cid.getCitationID());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                cvll.add(generateCitationViolationLink(rs));
                
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch list of CitationViolationLinks by Citation, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return cvll;
    }
    
   
    /**
     * Generator of a subclass of CodeViolation: a code violation attached to a citation
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
    private CitationCodeViolationLink generateCitationViolationLink(ResultSet rs) throws SQLException, IntegrationException{
        try {
            CaseCoordinator cc = getCaseCoordinator();
            SystemIntegrator si = getSystemIntegrator();
            CitationCodeViolationLink cvl = new CitationCodeViolationLink(cc.violation_getCodeViolation(rs.getInt("codeviolation_violationid")));
            
            si.populateTrackedLinkFields(cvl, rs);
            cvl.setCitationViolationID(rs.getInt("citationviolationid"));
            if(rs.getString("status") != null){
                cvl.setStatus(ViolationStatusEnum.valueOf(rs.getString("status")));
            }
            
            return cvl;
        } catch (BObStatusException ex) {
            throw new IntegrationException(ex.getMessage());
        }
    }
    
    
    /**
     * Internal generator method for creating a Citation from a ResultSet with 
     * all DB fields present
     * @param rs
     * @return
     * @throws IntegrationException 
     */
    private Citation generateCitationFromRS(ResultSet rs) throws IntegrationException, BObStatusException{
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        SystemIntegrator si = getSystemIntegrator();
        UserCoordinator uc = getUserCoordinator();
        Citation cit = new Citation();
        try {
            cit.setCitationID(rs.getInt("citationid"));
            cit.setCitationNo(rs.getString("citationno"));
            
            // DEPRECATE CITATION STATUS single field
//            cit.setStatus(getCitationStatus(rs.getInt("status_statusid")));
            cit.setOrigin_courtentity(cei.getCourtEntity(rs.getInt("origin_courtentity_entityID")));
            
            if(rs.getTimestamp("dateOfRecord") != null){
                cit.setDateOfRecord(rs.getTimestamp("dateOfRecord").toLocalDateTime());
            }
            cit.setFilingOfficer(uc.user_getUser(rs.getInt("login_userid")));
            cit.setFilingType(getCitationFilingType(rs.getInt("filingtype_typeid")));
            cit.setNotes(rs.getString("notes"));
            cit.setOfficialText(rs.getString("officialtext"));
            si.populateTrackedFields(cit, rs, false);
            
        } catch (SQLException | IntegrationException ex) {
            System.out.println(ex);
            throw new IntegrationException("Unable to build citation from RS", ex);
        }
        return cit;
    }
    
    
    /**
     * Writes a new record into the citationcitationstatus table to track
     * the movement a given citation through the court process
     * 
     * @param cit to which the log entry should be attached
     * @param csle the populated lot entry
     * @return the fresh record ID
     */
    public int insertCitationStatusLogEntry(Citation cit, CitationStatusLogEntry csle) throws IntegrationException{
        
        if(cit == null || csle == null || csle.getStatus() == null){
            throw new IntegrationException("cannot insert a log entry with null citation or log entry!");
        }
        
        String query =  "INSERT INTO public.citationcitationstatus(\n" +
                        "            citationstatusid, citation_citationid, citationstatus_statusid, \n" +
                        "            dateofrecord, createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, courtentity_entityid )\n" +
                        "    VALUES (DEFAULT, ?, ?, \n" +
                        "            ?, now(), ?, now(), ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int freshLogID = 0;
        
        try {
            stmt = con.prepareStatement(query);
            
            stmt.setInt(1, cit.getCitationID());
            stmt.setInt(2, csle.getStatus().getStatusID());
            
            if(csle.getDateOfRecord() != null){
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(csle.getDateOfRecord()));
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(csle.getCreatedBy() != null){
                stmt.setInt(4, csle.getCreatedBy().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            
            if(csle.getLastUpdatedBy() != null){
                stmt.setInt(5, csle.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            if(csle.getCourtEntity() != null){
                stmt.setInt(6, csle.getCourtEntity().getCourtEntityID());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            stmt.execute();
            
            String retrievalQuery = "SELECT currval('citationcitationstatus_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                 freshLogID = rs.getInt(1);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert citation log entry into database, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return freshLogID;
        
    }
    
    
    /**
     * Writes a new record into the citationcitationstatus table to track
     * the movement a given citation through the court process
     * 
     * @param cit to which the log entry should be attached
     * @param csle the populated lot entry
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void updateStatusLogEntry(CitationStatusLogEntry csle) throws IntegrationException{
        
        if(csle == null || csle.getStatus() == null){
            throw new IntegrationException("cannot insert a log entry with null citation or log entry!");
        }
        
        String query =  "UPDATE public.citationcitationstatus\n" +
                        "   SET citationstatus_statusid=?, \n" +
                        "       dateofrecord=?, lastupdatedts=now(), \n" +
                        "       lastupdatedby_userid=?, courtentity_entityid=?\n" +
                        " WHERE citationstatusid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            
            stmt.setInt(1, csle.getStatus().getStatusID());
            
            if(csle.getDateOfRecord() != null){
                stmt.setTimestamp(2, java.sql.Timestamp.valueOf(csle.getDateOfRecord()));
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            
            if(csle.getLastUpdatedBy() != null){
                stmt.setInt(3, csle.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(csle.getCourtEntity() != null){
                stmt.setInt(4, csle.getCourtEntity().getCourtEntityID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            stmt.setInt(5, csle.getLogEntryID());
                
            stmt.executeUpdate();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert citation into database, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    /**
     * Deactivates a citation status log entry
     * @param csle
     * @throws IntegrationException 
     */
    public void deactivateCitationStatusLogEntry(CitationStatusLogEntry csle) throws IntegrationException{
          
        if(csle == null ){
            throw new IntegrationException("cannot deactuvate a null log entry!");
        }
        
        String query =  "UPDATE public.citationcitationstatus\n" +
                        "   SET  lastupdatedts=now(), lastupdatedby_userid=?"
                + "         deactivatedts=now(), deactivatedby_userid=? " +
                        " WHERE citationstatusid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            
            if(csle.getLastUpdatedBy() != null){
                stmt.setInt(1, csle.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            
            if(csle.getDeactivatedBy() != null){
                stmt.setInt(2, csle.getDeactivatedBy().getUserID());
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            
            stmt.setInt(3, csle.getLogEntryID());
                
            stmt.executeUpdate();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to deactivate citation log, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        
    }
    
    
    /**
     * Creates a log of citation statuses and their dates of progression
     * through the court system; 
     * @param cit
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public List<CitationStatusLogEntry> buildCitationStatusLog(Citation cit) throws IntegrationException{
           String query =  "SELECT citationstatusid, citation_citationid, citationstatus_statusid, \n" +
                            "       dateofrecord, createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, \n" +
                            "       deactivatedts, deactivatedby_userid, notes, citationcitationstatus.courtentity_entityid AS ccs_ceid, statusid, statusname, description, icon_iconid, editsforbidden, \n" +
                            "       eventrule_ruleid\n" +
                            "  FROM public.citationcitationstatus JOIN public.citationstatus ON citationcitationstatus.citationstatus_statusid = statusid\n" +
                            "  WHERE citation_citationid=? AND deactivatedts IS NULL ORDER BY dateofrecord ASC;	";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<CitationStatusLogEntry> statusLog = new ArrayList<>();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cit.getCitationID());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                statusLog.add(generateCitationStatusLogEntry(rs));
                
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate citation status log, sorry", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return statusLog;           
    }
    
    /**
     * Generator method for CitationStatusLog Entries
     * @param rs
     * @return a single log entry
     */
    private CitationStatusLogEntry generateCitationStatusLogEntry(ResultSet rs) throws SQLException, IntegrationException{
        try {
            SystemIntegrator si = getSystemIntegrator();
            
            CitationStatusLogEntry csle = new CitationStatusLogEntry();
            
            csle.setLogEntryID(rs.getInt("citationstatusid"));
            csle.setStatus(generateCitationStatus(rs));
            
            if(rs.getTimestamp("dateofrecord") != null){
                csle.setDateOfRecord(rs.getTimestamp("dateofrecord").toLocalDateTime());
            }
            
            csle.setNotes(rs.getString("notes"));
            
            csle.setCourtEntity(getCourtEntity(rs.getInt("ccs_ceid")));
            
            si.populateTrackedFields(csle, rs, false);
            
            return csle;
        } catch (BObStatusException ex) {
            throw new IntegrationException(ex.getMessage());
        }
        
    }
    
    
    
    /**
     * Updates a single record in the citation table
     * @param citation
     * @throws IntegrationException 
     */
    public void updateCitation(Citation citation) throws IntegrationException{
        String query =  "UPDATE public.citation\n" +
                        "   SET  origin_courtentity_entityid=?, \n" +
                        "       login_userid=?, dateofrecord=?, \n" +
                        "       officialtext=?, lastupdatedts=now(), \n" +
                        "       lastupdatedby_userid=?, \n" +
                        "       filingtype_typeid=?, citationno = ?\n" +
                        " WHERE citationid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt1 = null;
        
        try {
            stmt1 = con.prepareStatement(query);
            
            
            if(citation.getOrigin_courtentity() != null){
                stmt1.setInt(1, citation.getOrigin_courtentity().getCourtEntityID());
            } else {
                stmt1.setNull(1, java.sql.Types.NULL);
            }
            
            if(citation.getFilingOfficer() != null){
                stmt1.setInt(2, citation.getFilingOfficer().getUserID());
            } else {
                stmt1.setNull(2, java.sql.Types.NULL);
            }
            
            if(citation.getDateOfRecord() != null){
                stmt1.setTimestamp(3, java.sql.Timestamp.valueOf(citation.getDateOfRecord()));
            } else {
                stmt1.setNull(3, java.sql.Types.NULL);
            }
            
            stmt1.setString(4, citation.getOfficialText());
            
            if(citation.getLastUpdatedBy()!= null){
                stmt1.setInt(5, citation.getLastUpdatedBy().getUserID());
            } else {
                stmt1.setNull(5, java.sql.Types.NULL);
            }
            
            if(citation.getFilingType() != null){
                stmt1.setInt(6, citation.getFilingType().getTypeID());
            } else {
                stmt1.setNull(6, java.sql.Types.NULL);
            }
            
            stmt1.setString(7, citation.getCitationNo());
            
            
            stmt1.setInt(8, citation.getCitationID());
            
            stmt1.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update citation in the database, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt1 != null) { try { stmt1.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Nukes a Citation from the DB! Dangerous!
     * @param citation
     * @throws IntegrationException 
     */
    public void deactivateCitation(Citation citation) throws IntegrationException{
        String query =  "UPDATE public.citation\n" +
                        "   SET  deactivatedts=now(), deactivatedby_userid=?, lastupdatedts=now(), \n" +
                        "       lastupdatedby_userid=? \n" +
                        " WHERE citationid=?;";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            if(citation.getDeactivatedBy() != null){
                stmt.setInt(1, citation.getDeactivatedBy().getUserID());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            
            if(citation.getLastUpdatedBy() != null){
                stmt.setInt(2, citation.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            
            stmt.setInt(3, citation.getCitationID());
            
            stmt.execute();
            
            getFacesContext().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Citation has been deactivated!", ""));
            
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
     * Creates a CitationStatusLogEntry given a statusid
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
            throw new IntegrationException("CEI.getCiationStatus(statusID): cannot fetch CitationStatus, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return cs;
        
        
    }
    
    /**
     * Builds a complete list of possible citation statuses
     * @return
     * @throws IntegrationException 
     */
    public List<CitationStatus> getCitationStatusList() throws IntegrationException{
        String query =  "SELECT statusid FROM citationStatus;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<CitationStatus> csList = new ArrayList<>();
        
        try {
            stmt = con.prepareStatement(query);
            
            rs = stmt.executeQuery();
            
            while(rs.next()){
                csList.add(getCitationStatus(rs.getInt("statusid")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("CEI.getCitationStatusList(): cannot fetch complete CitationStatus List, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return csList;
        
        
        
    }
    
    /**
     * Internal generator method for CitationStatusLogEntry objects
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
            cs.setEditsForbidden(rs.getBoolean("editsforbidden"));
            cs.setEventRuleAbstract(wi.rules_getEventRuleAbstract(rs.getInt("eventrule_ruleid")));
        
        } catch (SQLException | BObStatusException ex) {
            
            System.out.println(ex);
            throw new IntegrationException("Cannot Generate citation status object, sorry", ex);
        }
        return cs;
    }
    
    /**
     * Creates a new record in the citationstatus table
     * NOTE that this is a utility method for creating a new citation status
     * that can be applied to any citation, NOT for logging a new status of
     * a particular citation. 
     * 
     * @param cs
     * @throws IntegrationException 
     */
    public void insertCitationStatus(CitationStatus cs) throws IntegrationException{
        
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
            stmt.setBoolean(4, cs.isEditsForbidden());
            if(cs.getEventRuleAbstract()!= null){
                stmt.setInt(5, cs.getEventRuleAbstract().getRuleid());
                
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
            stmt.setBoolean(4, cs.isEditsForbidden());
             if(cs.getEventRuleAbstract()!= null){
                stmt.setInt(5, cs.getEventRuleAbstract().getRuleid());
                
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
    
    
    /**
     * Extracts a record from citationfilingtype
     * @param typeID
     * @return the fully baked type object
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public CitationFilingType getCitationFilingType(int typeID) 
            throws IntegrationException, BObStatusException{
        String query =  "SELECT typeid, title, description, muni_municode, active\n" +
                        "  FROM public.citationfilingtype WHERE typeid=?; ";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        CitationFilingType cft = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, typeID);
            
            rs = stmt.executeQuery();
            
            while(rs.next()){
                cft = generateCitationFilingType(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("CEI.getCitationFilingType(typeid): cannot fetch CitationFiliingType, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
             
        return cft;
        
    }
    
    /**
     * Generates a CitationFilingType object from a RS
     * @param rs
     * @return
     * @throws BObStatusException 
     */
    private CitationFilingType generateCitationFilingType(ResultSet rs) throws BObStatusException, SQLException{
        if(rs == null){
            throw new BObStatusException("Cannot generate filing type with null RS");
        }
        CitationFilingType cft = new CitationFilingType();
        
        cft.setTypeID(rs.getInt("typeid"));
        cft.setTitle(rs.getString("title"));
        cft.setDescription(rs.getString("description"));
        return cft;
    }
    
    /**
     * Builds a complete list of filing types
     * @return
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public List<CitationFilingType> getCitationFilingTypes() 
            throws IntegrationException, BObStatusException{
        String query =  "SELECT typeid FROM public.citationfilingtype WHERE active = TRUE; ";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<CitationFilingType> cftl = new ArrayList<>();
        
        try {
            stmt = con.prepareStatement(query);
            
            rs = stmt.executeQuery();
            
            while(rs.next()){
                cftl.add(getCitationFilingType(rs.getInt("typeid")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch citation filing type, sorry.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
                
        return cftl;
    }
}
