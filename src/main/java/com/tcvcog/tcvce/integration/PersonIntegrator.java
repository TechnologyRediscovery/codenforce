 /*
 * Copyright (C) 2017 ellen bascomb of apt 31y
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
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.ContactEmail;
import com.tcvcog.tcvce.entities.ContactPhone;
import com.tcvcog.tcvce.entities.ContactPhoneType;
import com.tcvcog.tcvce.entities.Human;
import com.tcvcog.tcvce.entities.HumanLink;
import com.tcvcog.tcvce.entities.IFace_humanListHolder;
import com.tcvcog.tcvce.entities.LinkedObjectSchemaEnum;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonChangeOrder;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.SearchParamsPerson;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplication;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Connects Person objects to the data store
 *
 * @author ellen bascomb of apt 31y
 */
public class PersonIntegrator extends BackingBeanUtils implements Serializable {

    final String PERSON_ACTIVE_FIELD = "human.deactivatedts";
    /**
     * Creates a new instance of PersonIntegrator
     */
    public PersonIntegrator() {
    }
    
    
   
    
    /**
     * Asks the inputted param for its linking table name and fetches
     * related humans, building them into a nice little list
     * @param hlh
     * @return a list, not empty if there are linked humans
     * @throws IntegrationException 
     */
    public List<HumanLink> getHumanLinks(IFace_humanListHolder hlh) throws IntegrationException{
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        if(hlh == null){
            throw new IntegrationException("Cannot get linked humans with null list holder object!!");
        }
        
        List<HumanLink> linkedHumans = new ArrayList<>();

        try {
            
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT linkid, human_humanid, linkedobjectrole_lorid, \n");
            sb.append("createdts, createdby_userid, lastupdatedts, lastupdatedby_userid,");
            sb.append("deactivatedts, deactivatedby_userid, notes, source_sourceid  ");
            sb.append("FROM ");
            sb.append(hlh.getHUMAN_LINK_SCHEMA_ENUM().getLinkingTableName());
            sb.append(" WHERE ");
            sb.append(hlh.getHUMAN_LINK_SCHEMA_ENUM().getTargetTableFKField());
            sb.append("=?");
            sb.append(" AND deactivatedts IS NULL;");
            
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, hlh.getHostPK());

            rs = stmt.executeQuery();

            while (rs.next()) {
                HumanLink hl = generateHumanLink(rs, getHuman(rs.getInt("human_humanid")));
                hl.setParentObjectID(hlh.getHostPK());
                
                linkedHumans.add(hl);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.getHumanLinkIDList()| Unable to retrieve person", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return linkedHumans;
        
    }
    
    
    /**
     * Builds a list of linked humans based on an Enum and a human
     * @param lose
     * @param hum
     * @return the HumanLinks associated with the given human in the object family of the Enum
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public List<HumanLink> getHumanLinksByLinkedObjectEnum(LinkedObjectSchemaEnum lose, Human hum) throws IntegrationException{
         Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        if(lose == null || hum == null){
            throw new IntegrationException("Cannot get linked humans with null enum or human");
        }
        
        List<HumanLink> linkedHumans = new ArrayList<>();

        try {
            
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT linkid, human_humanid, linkedobjectrole_lorid, \n");
            sb.append("createdts, createdby_userid, lastupdatedts, lastupdatedby_userid,");
            sb.append("deactivatedts, deactivatedby_userid, notes, source_sourceid,  ");
            sb.append(lose.getTargetTableFKField());
            sb.append(" FROM ");
            sb.append(lose.getLinkingTableName());
            sb.append(" WHERE ");
            sb.append("human_humanid");
            sb.append("=?");
            sb.append(" AND deactivatedts IS NULL;");
            
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, hum.getHumanID());

            rs = stmt.executeQuery();

            while (rs.next()) {
                HumanLink hl = generateHumanLink(rs, getHuman(rs.getInt("human_humanid")));
                hl.setParentObjectID(rs.getInt(lose.getTargetTableFKField()));
                linkedHumans.add(hl);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.getHumanLinkIDList()| Unable to retrieve person", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return linkedHumans;
    }
    
    
    
    
    /**
     * Looks up a Human given a human and creates a returns a new instance
     * of Human with all the available information loaded about that person
     *
     * @param humanID
     * @return a Human object with all of the available data loaded
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public Human getHuman(int humanID) throws IntegrationException {

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        Human h = null;

        try {
            
            String s =  "SELECT humanid, name, dob, under18, jobtitle, businessentity, \n" +
                        "       multihuman, source_sourceid, deceaseddate, deceasedby_userid, \n" +
                        "       cloneof_humanid, createdts, createdby_userid, lastupdatedts, \n" +
                        "       lastupdatedby_userid, deactivatedts, deactivatedby_userid, notes\n" +
                        "  FROM public.human WHERE humanid=?;";
            
            stmt = con.prepareStatement(s);
            stmt.setInt(1, humanID);

            rs = stmt.executeQuery();

            while (rs.next()) {
                // note that rs.next() is called and the cursor
                // is advanced to the first row in the rs
                h = generateHuman(rs);
            }

        } catch (SQLException | BObStatusException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.getHuman | Unable to retrieve human", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return h;
    }
    
    /**
     * Populates the member fields on a Human object from a resulset
     * @param rs with all columns SELECTed
     * @return the fully-baked Human
     * @throws SQLException
     * @throws IntegrationException 
     */
    private Human generateHuman(ResultSet rs) throws SQLException, IntegrationException, BObStatusException{
        SystemIntegrator si = getSystemIntegrator();
        UserCoordinator uc = getUserCoordinator();
        Human h = new Human();
        
        h.setHumanID(rs.getInt("humanid"));
        h.setName(rs.getString("name"));
        if(rs.getDate("dob") != null){
            h.setDob(rs.getDate("dob").toLocalDate());
        }
        h.setUnder18(rs.getBoolean("under18"));
        
        h.setJobTitle(rs.getString("jobTitle"));
        h.setBusinessEntity(rs.getBoolean("businessentity"));
        h.setMultiHuman(rs.getBoolean("multihuman"));
        if(rs.getInt("source_sourceid") != 0){
            si.getBOBSource(rs.getInt("source_sourceid"));
        }
        
        h.setNotes(rs.getString("notes"));
        if(rs.getDate("deceaseddate") != null){
            h.setDeceasedDate(rs.getDate("deceaseddate").toLocalDate());
        }
        if(rs.getInt("deceasedby_userid") != 0){
            h.setDeceasedBy(uc.user_getUser(rs.getInt("deceasedby_userid")));
        }
        
        h.setCloneOfHumanID(rs.getInt("cloneof_humanid"));
        
        // Ship to our SystemIntegrator for standard fields
        si.populateTrackedFields(h, rs, true);
        return h;
    }
    
    /**
     * Generator for HumanLink objects which are Humans connected to a BOb
     * with connection metadata attached as a subclass, including
     * the link source and the linked object role
     * 
     * @param rs containing fields for the standard linked tables
     * @param h the human whose linked metadata is desired
     * @return the HumanLink which is a human with link metadata attached
     * @throws SQLException
     * @throws IntegrationException 
     */
    public HumanLink generateHumanLink(ResultSet rs, Human h) throws SQLException, IntegrationException{
        SystemIntegrator si = getSystemIntegrator();
        HumanLink hl = null;
        
        if(rs != null && h != null){
            try {
                hl = new HumanLink(h);
                si.populateTrackedLinkFields(hl, rs);
                hl.setLinkID(rs.getInt("linkid"));
                hl.setNotes(rs.getString("notes"));
                hl.setLinkRole(si.getLinkedObjectRole(rs.getInt("linkedobjectrole_lorid")));
            } catch (BObStatusException ex) {
                throw new IntegrationException(ex.getMessage());
            }
        }
        return hl;
    }

    /**
     * Creates a record in the appropriate linking table for a given human
     * to a given human list holder object =
     * @param humanziedBOb the object to which the human shall be linked
     * @param hl the human to link
     * @return the link ID of the freshly created link, 0 for incomplete links
     * @throws IntegrationException 
     */
    public int insertHumanLink(IFace_humanListHolder humanziedBOb, HumanLink hl) throws IntegrationException, BObStatusException{
        if (humanziedBOb == null || hl == null || humanziedBOb.getHostPK() == 0){
            throw new IntegrationException("Cannot link a human and a list holder with null inputs or host PK of 0");
        }

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        int linkid = 0;
        
        try {
            
            StringBuilder sb = new StringBuilder();
            
            sb.append("INSERT INTO ");
            sb.append(humanziedBOb.getHUMAN_LINK_SCHEMA_ENUM().getLinkingTableName());
            sb.append(" (linkid, human_humanid,");
            sb.append(humanziedBOb.getHUMAN_LINK_SCHEMA_ENUM().getTargetTableFKField());
            sb.append(", createdts, createdby_userid, lastupdatedts, lastupdatedby_userid,");
            sb.append(" linkedobjectrole_lorid, source_sourceid)");
            sb.append(" VALUES (DEFAULT, ?, ?, now(), ?, now(), ?, ?, ?);");
                        
            stmt = con.prepareStatement(sb.toString());
            
            stmt.setInt(1, hl.getHumanID());
            stmt.setInt(2, humanziedBOb.getHostPK());
            if(hl.getLinkCreatedByUserID() != 0){
                stmt.setInt(3, hl.getLinkCreatedByUserID() );
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(hl.getLinkLastUpdatedByUserID() != 0){
                stmt.setInt(4, hl.getLinkLastUpdatedByUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            if(hl.getLinkedObjectRole() != null){
                stmt.setInt(5, hl.getLinkedObjectRole().getRoleID());
            } else  { 
                throw new BObStatusException("PersonIntegrator.insertHumanLink | Cannot link human with null LinkedObjectRole");
            }
            
            if(hl.getLinkSource() != null){
                stmt.setInt(6, hl.getLinkSource().getSourceid());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }

            stmt.execute();

            sb = new StringBuilder();
            sb.append("SELECT currval('");
            sb.append(humanziedBOb.getHUMAN_LINK_SCHEMA_ENUM().getLinkingTableSequenceID());
            sb.append("');");
            
            stmt = con.prepareStatement(sb.toString());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                linkid = rs.getInt(1);
            } 
            System.out.println("PersonIntegrator.insertHumanLink | linked human new LinkID = " + linkid);

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.insertHumanLink| Unable to create human link", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return linkid;
    }
    
   /**
     * Updates a record in the appropriate linking table for a given human
     * to a given human list holder object =
     * @param humanziedBOb the object to which the human shall be linked
     * @param hl the human to link
     * @throws IntegrationException 
     */
    public void updateHumanLink(IFace_humanListHolder humanziedBOb, HumanLink hl) throws IntegrationException{
        if (humanziedBOb == null || hl == null){
            throw new IntegrationException("Cannot link a human and a list holder with null inputs");
        }

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            
            StringBuilder sb = new StringBuilder();
            
            sb.append("UPDATE ");
            sb.append(humanziedBOb.getHUMAN_LINK_SCHEMA_ENUM().getLinkingTableName());
            sb.append(" SET source_sourceid=?, lastupdatedts=now(), lastupdatedby_userid=?, ");
            sb.append(" linkedobjectrole_lorid=? WHERE ");
            sb.append(humanziedBOb.getHUMAN_LINK_SCHEMA_ENUM().getLinkingTablePKField());
            sb.append(" = ?;");
                        
            stmt = con.prepareStatement(sb.toString());
            
            if(hl.getSource() != null){
                stmt.setInt(1, hl.getSource().getSourceid());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            
            if(hl.getLastUpdatedBy()!= null){
                stmt.setInt(2, hl.getLinkLastUpdatedByUserID() );
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            
            if(hl.getLinkedObjectRole() != null){
                stmt.setInt(3, hl.getLinkedObjectRole().getRoleID());
            } else  { 
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            stmt.setInt(4, hl.getLinkID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.updateHumanLink | Unable to update link", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    
   /**
     * Updates a record in the appropriate linking table for a given human
     * to a given human list holder object =
     * @param hl the human to link
     * @throws IntegrationException 
     */
    public void deactivateHumanLink(HumanLink hl) throws IntegrationException{
        if (hl == null 
                || hl.getLinkedObjectRole() == null 
                || hl.getLinkedObjectRole().getSchema() == null  
                || hl.getParentObjectID() == 0){
            throw new IntegrationException("Cannot deactivate a human link with null link, schemaenum, or parent object ID == 0");
        }

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            
            StringBuilder sb = new StringBuilder();
            
            sb.append("UPDATE ");
            sb.append(hl.getLinkedObjectRole().getSchema().getLinkingTableName());
            sb.append(" SET lastupdatedts=now(), lastupdatedby_userid=?, ");
            sb.append(" deactivatedts=now(), deactivatedby_userid=? WHERE ");
            sb.append(hl.getLinkedObjectRole().getSchema().getLinkingTablePKField());
            sb.append(" = ?;");
                        
            stmt = con.prepareStatement(sb.toString());
            
            
            if(hl.getLinkLastUpdatedByUserID() != 0){
                stmt.setInt(1, hl.getLinkLastUpdatedByUserID());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            
            if(hl.getLinkDeactivatedByUserID() != 0){
                stmt.setInt(2, hl.getLinkDeactivatedByUserID());
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            
            stmt.setInt(3, hl.getLinkID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.deactivateHumanLink", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    /**
     * Writes the value of the given HumanLink's notes over the existing notes
     * @param hl
     * @throws BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void updateHumanLinkNotes(HumanLink hl) throws BObStatusException, IntegrationException{
        if(hl == null || hl.getLinkedObjectRole() == null || hl.getLinkedObjectRole().getSchema() == null){
            throw new BObStatusException("Cannot write notes with null link or null LinkObjectSchemaEnum");            
        }
        
        
           Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            
            StringBuilder sb = new StringBuilder();
            
            sb.append("UPDATE ");
            sb.append(hl.getLinkedObjectRole().getSchema().getLinkingTableName());
            sb.append(" SET lastupdatedts=now(), lastupdatedby_userid=?, ");
            sb.append(" notes=? WHERE ");
            sb.append(hl.getLinkedObjectRole().getSchema().getLinkingTablePKField());
            sb.append(" = ?;");
                        
            stmt = con.prepareStatement(sb.toString());
            
            
            if(hl.getLastUpdatedBy()!= null){
                stmt.setInt(1, hl.getLinkLastUpdatedByUserID());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            
            stmt.setString(2, hl.getNotes());
            
            stmt.setInt(3, hl.getLinkID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.updateHumanLinkNotes", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    
    
    /**
     * Updates values for a given record in the human table
     * @param h containing the new values
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void updateHuman(Human h) throws IntegrationException{
        
        String selectQuery =  "UPDATE public.human\n" +
                                "   SET name=?, dob=?, under18=?, jobtitle=?, businessentity=?, \n" + // 1-5
                                "       multihuman=?, source_sourceid=?, deceaseddate=?, deceasedby_userid=?, \n" + // 6-9
                                "       cloneof_humanid=?, lastupdatedts=now(), \n" + // 10
                                "       lastupdatedby_userid=?, \n" + // 11
                                "       notes=?\n" + //12
                                " WHERE humanid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(selectQuery);
            // first SQL row
            stmt.setString(1, h.getName());
            if(h.getDob() != null){
                stmt.setDate(2, java.sql.Date.valueOf(h.getDob()));
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            stmt.setBoolean(3, h.isUnder18());
            stmt.setString(4, h.getJobTitle());
            stmt.setBoolean(5, h.isBusinessEntity());
            
            // second SQL row
            stmt.setBoolean(6, h.isMultiHuman());
            if(h.getSource() != null){
                stmt.setInt(7, h.getSource().getSourceid());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            if(h.getDeceasedDate() != null){
                stmt.setDate(8, java.sql.Date.valueOf(h.getDeceasedDate()));
                if(h.getDeceasedBy() != null){
                    stmt.setInt(9, h.getDeceasedBy().getUserID());
                } else {
                    stmt.setNull(9, java.sql.Types.NULL);
                }
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
                stmt.setNull(9, java.sql.Types.NULL);
            }
            
            // third sql row
            if(h.getCloneOfHumanID() != 0){
                stmt.setInt(10, h.getCloneOfHumanID());
            } else {
                stmt.setNull(10, java.sql.Types.NULL);
            }
            
            // fourth sql row
            if(h.getLastUpdatedBy() != null){
                stmt.setInt(11, h.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(11, java.sql.Types.NULL);
            }
            
            stmt.setString(12, h.getNotes());
            
            stmt.setInt(13, h.getHumanID());
            
            
            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.updateHuman: Unable to UpdatePerson", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    /**
     * Creates a record in the human table
     * @param h fully populated Human
     * @return the ID of the freshly inserted human record
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public int insertHuman(Human h) throws IntegrationException{
        int freshHumanID = 0;
        
        String query =    "INSERT INTO public.human(\n" +
                                "           humanid, name, dob, under18, jobtitle, businessentity, multihuman, \n" +
                                "            source_sourceid, deceaseddate, deceasedby_userid, cloneof_humanid, \n" +
                                "            createdts, createdby_userid)\n" +
                                "    VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, \n" +
                                "            ?, ?, ?, ?, \n" +
                                "            now(), ?);";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = con.prepareStatement(query);
        
            // first SQL row
            // DEFAULT ID
            stmt.setString(1, h.getName());
            if(h.getDob() != null){
                stmt.setDate(2, java.sql.Date.valueOf(h.getDob()));
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            stmt.setBoolean(3, h.isUnder18());
            stmt.setString(4, h.getJobTitle());
            stmt.setBoolean(5, h.isBusinessEntity());
            stmt.setBoolean(6, h.isMultiHuman());
            
            // second SQL row
            if(h.getSource() != null){
                stmt.setInt(7, h.getSource().getSourceid());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            if(h.getDeceasedDate() != null){
                stmt.setDate(8, java.sql.Date.valueOf(h.getDeceasedDate()));
                if(h.getDeceasedBy() != null){
                    stmt.setInt(9, h.getDeceasedBy().getUserID());
                } else {
                    stmt.setNull(9, java.sql.Types.NULL);
                }
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
                stmt.setNull(9, java.sql.Types.NULL);
            }
            
            if(h.getCloneOfHumanID() != 0){
                stmt.setInt(10, h.getCloneOfHumanID());
            } else {
                stmt.setNull(10, java.sql.Types.NULL);
            }
            
            // third sql row
            // creation timestamp set by now()
            if(h.getCreatedBy() != null){
                stmt.setInt(11, h.getCreatedBy().getUserID());
            } else {
                stmt.setNull(11, java.sql.Types.NULL);
            }
            
            stmt.execute();
             
            String retrievalQuery = "SELECT currval('human_humanid_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                freshHumanID = rs.getInt(1);
            }
            

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.insertHuman: Unable to Insert new person", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return freshHumanID;
    }
    
    
    /**
     * Retrieves an email record
     * @param emailID
     * @return the retrieved Email address
     * @throws IntegrationException 
     */
    public ContactEmail getContactEmail(int emailID) throws IntegrationException {

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ContactEmail em = null;

        try {
            
            String s =  "SELECT emailid, human_humanid, emailaddress, bouncets, createdts, createdby_userid, \n" +
                        "       lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, \n" +
                        "       notes, priority \n" +
                        "  FROM public.contactemail WHERE emailid=?;";
            
            stmt = con.prepareStatement(s);
            stmt.setInt(1, emailID);

            rs = stmt.executeQuery();

            while (rs.next()) {
                // note that rs.next() is called and the cursor
                // is advanced to the first row in the rs
                em = generateContactEmail(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator ...", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return em;
    }
    
    /**
     * Retrieves all contactphone records by humanid
     * @param humanID of the human to populate
     * @return of all Contacts associated with the given human
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public List<ContactPhone> getContactPhoneList(int humanID) throws IntegrationException{
        List<ContactPhone> phoneList = new ArrayList<>();
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            
            String s =  "SELECT phoneid FROM contactphone WHERE human_humanid=? AND deactivatedts IS NULL; ";
            
            stmt = con.prepareStatement(s);
            stmt.setInt(1, humanID);

            rs = stmt.executeQuery();

            while (rs.next()) {
                // note that rs.next() is called and the cursor
                // is advanced to the first row in the rs
                phoneList.add(getContactPhone(rs.getInt("phoneid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator ...", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return phoneList;
        
        
    }
    
    /**
     * Retrieves all contactemail records by humanid
     * @param humanID of the human to search
     * @return of all contacts associated with the given human
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public List<ContactEmail> getContactEmailList(int humanID) throws IntegrationException{
        List<ContactEmail> emailList = new ArrayList<>();
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            
            String s =  "SELECT emailid FROM contactemail WHERE human_humanid=? AND deactivatedts IS NULL; ";
            
            stmt = con.prepareStatement(s);
            stmt.setInt(1, humanID);

            rs = stmt.executeQuery();

            while (rs.next()) {
                // note that rs.next() is called and the cursor
                // is advanced to the first row in the rs
                emailList.add(getContactEmail(rs.getInt("emailid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator ...", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        
        return emailList;
    }
    
    
    
    /**
     * Populates fields on a ContactEmail
     * @param rs with all fields SELECTed
     * @return the populated ContactEmail object
     */
    private ContactEmail generateContactEmail(ResultSet rs) throws SQLException, IntegrationException{
        try {
            SystemIntegrator si = getSystemIntegrator();
            ContactEmail em = new ContactEmail();
            
            em.setEmailID(rs.getInt("emailid"));
            em.setHumanID(rs.getInt("human_humanid"));
            em.setEmailaddress(rs.getString("emailaddress"));
            
            if(rs.getTimestamp("bouncets") != null){
                em.setBounceTS(rs.getTimestamp("bouncets").toLocalDateTime());
            }
            em.setNotes(rs.getString("notes"));
            em.setPriority(rs.getInt("priority"));
            si.populateTrackedFields(em, rs, true);
            
            return em;
        } catch (BObStatusException ex) {
            throw new IntegrationException(ex.getMessage());
            
        }
    }
    
    
    /**
     * Updates a record in the contactemail table
     * NOTE: bounces are recorded by a dedicated method in this Integrator
     * NOTE: notes are recorded in the SystemIntegrator's updateNotes method
     * @param em with all fields ready for insertion
     * @throws IntegrationException 
     */
    public void updateContactEmail(ContactEmail em) throws IntegrationException, BObStatusException {
        if(em == null){
            throw new BObStatusException("Cannot update a null emailaddress");
        }
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            
            String s =  "UPDATE public.contactemail\n" +
                        "   SET human_humanid=?, emailaddress=?, \n" +
                        "       lastupdatedts=now(), lastupdatedby_userid=?, "
                    +   "       deactivatedts=?, deactivatedby_userid=?, bouncets=?, priority=?  \n" +
                        " WHERE emailid=?;";
            
            stmt = con.prepareStatement(s);
            if(em.getHumanID() != 0){
                stmt.setInt(1, em.getHumanID());
            }
            stmt.setString(2, em.getEmailaddress());
            if(em.getLastUpdatedBy() != null){
                stmt.setInt(3, em.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(em.getDeactivatedTS()!= null){
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(em.getDeactivatedTS()));
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
             if(em.getDeactivatedBy()!= null){
                stmt.setInt(5, em.getDeactivatedBy().getUserID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
             if(em.getBounceTS() != null){
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(em.getBounceTS()));
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
             
            
            stmt.setInt(7, em.getPriority());

            stmt.setInt(8, em.getEmailID());
            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator ...", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    
 
    
    /**
     * Creates a new record in the contactemail table
     * @param em with fields ready for insertion
     * @return the id of the freshly inserted record
     * @throws IntegrationException 
     */
    public int insertContactEmail(ContactEmail em) throws IntegrationException {

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int lastID = 0;

        try {
            
            String s =  "INSERT INTO public.contactemail(\n" +
                        "            emailid, human_humanid, emailaddress, createdts, createdby_userid, \n" +
                        "            lastupdatedts, lastupdatedby_userid, priority) \n" +
                        "    VALUES (DEFAULT, ?, ?, now(), ?, \n" +
                        "            now(), ?, ?);";
            
            stmt = con.prepareStatement(s);
             if(em.getHumanID() != 0){
                stmt.setInt(1, em.getHumanID());
            }
            stmt.setString(2, em.getEmailaddress());
            if(em.getCreatedBy() != null){
                stmt.setInt(3, em.getCreatedBy().getUserID());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            if(em.getLastUpdatedBy() != null){
                stmt.setInt(4, em.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            stmt.setInt(5, em.getPriority());
            

            stmt.execute();
            
            String idNumQuery = "SELECT currval('contactemail_emailid_seq');";
            Statement st = con.createStatement();
            rs = st.executeQuery(idNumQuery);
            rs.next();
            lastID = rs.getInt("currval");

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.insertContactEmail", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return lastID;
    }
    
    /**
     * Extracts a record from the contactphone table
     * @param phoneID
     * @return
     * @throws IntegrationException 
     */
    public ContactPhone getContactPhone(int phoneID) throws IntegrationException {

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ContactPhone phone = null;

        try {
            
            String s =  "SELECT phoneid, human_humanid, phonenumber, phoneext, phonetype_typeid, \n" +
                        "       disconnectts, disconnect_userid, createdts, createdby_userid, \n" +
                        "       lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, \n" +
                        "       notes, priority \n" +
                        "  FROM public.contactphone WHERE phoneid=?;";
            
            stmt = con.prepareStatement(s);
            stmt.setInt(1, phoneID);

            rs = stmt.executeQuery();

            while (rs.next()) {
                // note that rs.next() is called and the cursor
                // is advanced to the first row in the rs
                phone = generateContactPhone(rs);
                
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator ...", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return phone;
    }
    
    
    /**
     * POpulates fields of a ContactPhone Object 
     * @param rs with all fields SELECTed
     * @return the populated object
     */
    private ContactPhone generateContactPhone(ResultSet rs) throws SQLException, IntegrationException{
        try {
            ContactPhone phone = new ContactPhone();
            UserCoordinator uc = getUserCoordinator();
            SystemIntegrator si = getSystemIntegrator();
            
            phone.setPhoneID(rs.getInt("phoneid"));
            phone.setHumanID(rs.getInt("human_humanid"));
            phone.setPhoneNumber(rs.getString("phonenumber"));
            phone.setExtension(rs.getInt("phoneext"));
            
            phone.setPhoneType(getContactPhoneType(rs.getInt("phonetype_typeid")));
            
            if(rs.getTimestamp("disconnectts") != null){
                phone.setDisconnectTS(rs.getTimestamp("disconnectts").toLocalDateTime());
            }
            phone.setDisconnectRecordedBy(uc.user_getUser(rs.getInt("disconnect_userid")));
            phone.setNotes(rs.getString("notes"));
            phone.setPriority(rs.getInt("priority"));
            
            si.populateTrackedFields(phone, rs, true);
            
            return phone;
        } catch (BObStatusException ex) {
            throw new IntegrationException(ex.getMessage());
        }
        
    }
    
    /**
     * Updates a record in the contactphone table
     * @param phone
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public void updateContactPhone(ContactPhone phone) throws IntegrationException, BObStatusException {

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        

        try {
            
            String s =  "UPDATE public.contactphone\n" +
                        "   SET human_humanid=?, phonenumber=?, phoneext=?, phonetype_typeid=?, \n" +
                        "       disconnectts=?, disconnect_userid=?, \n" +
                        "       lastupdatedts=now(), lastupdatedby_userid=?, deactivatedts=?, deactivatedby_userid=?, priority=? \n" +
                        " WHERE phoneid=? ;";
            
            stmt = con.prepareStatement(s);
            if(phone.getHumanID() != 0){
                stmt.setInt(1, phone.getHumanID() );
            } else {
                throw new BObStatusException("Cannot update a phone contact with 0 as humanID link");
            }
            
            stmt.setString(2, phone.getPhoneNumber());
            // don't set extension as zero
            // do not support edge case of a phone with an actual extension of 0
            if(phone.getExtension() != 0){
                stmt.setInt(3, phone.getExtension());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(phone.getPhoneType() != null){
                stmt.setInt(4, phone.getPhoneType().getPhoneTypeID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            if(phone.getDisconnectTS() != null){
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(phone.getDisconnectTS()));
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            if(phone.getDisconnectRecordedBy() != null){
                stmt.setInt(6, phone.getDisconnectRecordedBy().getUserID());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            if(phone.getLastUpdatedBy() != null){
                stmt.setInt(7, phone.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            if(phone.getDeactivatedTS()!= null){
                stmt.setTimestamp(8, java.sql.Timestamp.valueOf(phone.getDeactivatedTS()));
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            
            if(phone.getDeactivatedBy()!= null){
                stmt.setInt(9, phone.getDeactivatedBy().getUserID());
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            
            stmt.setInt(10, phone.getPriority());
            
            
            stmt.setInt(11, phone.getPhoneID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator ...", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
    
    /**
     * Inserts a record in the contactphone table
     * @param phone the ContactPhone object with fields ready for insertion
     * @return the ID of the freshly inserted phone object
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public int insertContactPhone(ContactPhone phone) throws IntegrationException, BObStatusException {

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int lastID = 0;

        try {
            
            String s =  "INSERT INTO public.contactphone(\n" +
                        "            phoneid, human_humanid, phonenumber, phoneext, phonetype_typeid, \n" +
                        "            createdts, createdby_userid, \n" +
                        "            lastupdatedts, lastupdatedby_userid, priority )\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
                        "            now(), ?, \n" +
                        "            now(), ?, ?);";
            
            stmt = con.prepareStatement(s);
            if(phone.getHumanID() != 0){
                stmt.setInt(1, phone.getHumanID() );
            } else {
                throw new BObStatusException("Cannot insert a phone contact with 0 as humanID link");
            }
            
            stmt.setString(2, phone.getPhoneNumber());
            // don't set extension as zero
            // do not support edge case of a phone with an actual extension of 0
            if(phone.getExtension() != 0){
                stmt.setInt(3, phone.getExtension());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(phone.getPhoneType() != null){
                stmt.setInt(4, phone.getPhoneType().getPhoneTypeID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            if(phone.getCreatedBy() != null){
                stmt.setInt(5, phone.getCreatedBy().getUserID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            if(phone.getLastUpdatedBy() != null){
                stmt.setInt(6, phone.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            stmt.setInt(7, phone.getPriority());
            

            stmt.execute();
            
            String idNumQuery = "SELECT currval('contactphone_phoneid_seq');";
            Statement st = con.createStatement();
            rs = st.executeQuery(idNumQuery);
            rs.next();
            lastID = rs.getInt("currval");


        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator ...", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return lastID;
    }
    
    
    
    /**
     * Extracts a record from the contactphonetype table
     * @param phoneTypeID
     * @return the record Objectified
     * @throws IntegrationException 
     */
    public ContactPhoneType getContactPhoneType(int phoneTypeID) throws IntegrationException {

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ContactPhoneType cpt = null;

        try {
            
            String s = "SELECT phonetypeid, title, createdts, deactivatedts\n" +
                        "  FROM public.contactphonetype WHERE phonetypeid=?;";
            
            stmt = con.prepareStatement(s);
            stmt.setInt(1, phoneTypeID);

            rs = stmt.executeQuery();

            while (rs.next()) {
                cpt = generateContactPhoneType(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator ...", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return cpt;
    }
    
    
    
    /**
     * Extracts all active records from the contactphonetype table
     * @param phoneTypeID
     * @return the record Objectified
     * @throws IntegrationException 
     */
    public List<ContactPhoneType> getContactPhoneTypeList() throws IntegrationException {

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<ContactPhoneType> cptl = new ArrayList<>();

        try {
            
            String s = "SELECT phonetypeid, title, createdts, deactivatedts\n" +
                       "  FROM public.contactphonetype WHERE deactivatedts IS NULL;";
            
            stmt = con.prepareStatement(s);
            
            rs = stmt.executeQuery();

            while (rs.next()) {
                cptl.add(generateContactPhoneType(rs));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator ...", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return cptl;
    }
    
    
    
    
    /**
     * Populates fields on a ConatctPhoneType object
     * @param rs with all fields SELECTed
     * @return the Objectified phone type
     */
    private ContactPhoneType generateContactPhoneType(ResultSet rs) throws SQLException{
        ContactPhoneType cpt = new ContactPhoneType();
        
        cpt.setPhoneTypeID(rs.getInt("phonetypeid"));
        cpt.setTitle(rs.getString("title"));
        if(rs.getTimestamp("createdts") != null){
            cpt.setCreatedTS(rs.getTimestamp("createdts").toLocalDateTime());
        }
        if(rs.getTimestamp("deactivatedts") != null){
            cpt.setDeactivatedTS(rs.getTimestamp("deactivatedts").toLocalDateTime());
        }
        return cpt;
        
    }
    
    
    /**
     * Updates a record in the contactphonetype table
     * @param cpt
     * @throws IntegrationException 
     */
    public void updateContactPhoneType(ContactPhoneType cpt) throws IntegrationException {

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            
            String s =  "UPDATE public.contactphonetype\n" +
                        "   SET title=?, deactivatedts=?\n" +
                        " WHERE phonetypeid=?;";
            
            stmt = con.prepareStatement(s);
            stmt.setString(1, cpt.getTitle() );
            if(cpt.getDeactivatedTS() != null){
                stmt.setTimestamp(2, java.sql.Timestamp.valueOf(cpt.getDeactivatedTS()));
            }
            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator ...", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
    
    /**
     * Creates a new record in the contactphonetypetable
     * @param cpt
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void insertContactPhoneType(ContactPhoneType cpt) throws IntegrationException {

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            
            String s =  "INSERT INTO public.contactphonetype(\n" +
                        "            phonetypeid, title, createdts)\n" +
                        "    VALUES (DEFAULT, ?, now());";
            
            stmt = con.prepareStatement(s);
            
            stmt.setString(1, cpt.getTitle());
            
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator ...", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
    
    
    /**
     * We have a special type of Person which are those who have been attached to an application
     * or OccPeriod, and this method fetches those connected to an OccPeriod
     * 
     * @param application
     * @return
     * @throws IntegrationException 
     */
    public List<HumanLink> getPersonOccApplicationList(OccPermitApplication application) throws IntegrationException{
        List<HumanLink> personList = new ArrayList<>();
        String selectQuery =  "SELECT person_personid, occpermitapplication_applicationid, applicant, preferredcontact, \n" +
                                "   applicationpersontype, active\n" +
                                "   FROM public.occpermitapplicationperson WHERE occpermitapplication_applicationid=? AND active=true;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(selectQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setInt(1, application.getId());
            rs = stmt.executeQuery();
            while(rs.next()){
                personList.add(generatePersonOccPeriod(rs));
                
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to get persons connected to OccPermitApplication", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return personList;
    }
    
    
     public List<Integer> getPersonsMappedToAUser() throws IntegrationException{
        List<Integer> humanidl = new ArrayList<>();
        String selectQuery =  "SELECT DISTINCT personlink FROM public.login;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(selectQuery);
            rs = stmt.executeQuery();
            while(rs.next()){
                humanidl.add(rs.getInt("personlink"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to get persons connected to users", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return humanidl;
    }
    
    /**
     * We have a special type of Person which are those who have been attached to an application
     * or OccPeriod, and this method fetches those connected to an OccApplication
     * 
     * @param personID
     * @param applicationID
     * @return
     * @throws IntegrationException 
     */
    public HumanLink getPersonOccApplication(int personID, int applicationID) throws IntegrationException{
        String selectQuery =  "SELECT person_personid, applicant, preferredcontact, \n" +
                                "   applicationpersontype, active\n" +
                                "   FROM public.occpermitapplicationperson "
                              + "WHERE occpermitapplication_applicationid=? AND person_personID=?;";
        HumanLink output = null;
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(selectQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setInt(1, applicationID);
            stmt.setInt(2, personID);
            rs = stmt.executeQuery();
            while(rs.next()){
                output = generatePersonOccPeriod(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to get persons connected to OccPermitApplication", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return output;
        
    }
    
    /**
     * Gets a list that includes inactive links
     * 
     * @param application
     * @return
     * @throws IntegrationException 
     */
    public List<HumanLink> getPersonOccApplicationListWithInactive(OccPermitApplication application) throws IntegrationException{
        List<HumanLink> personList = new ArrayList<>();
        String selectQuery =  "SELECT person_personid, occpermitapplication_applicationid, applicant, preferredcontact, \n" +
                                "   applicationpersontype, active\n" +
                                "   FROM public.occpermitapplicationperson WHERE occpermitapplication_applicationid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(selectQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setInt(1, application.getId());
            rs = stmt.executeQuery();
            while(rs.next()){
                personList.add(generatePersonOccPeriod(rs));
                
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to get persons connected to OccPermitApplication", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return personList;
    }
    
    private HumanLink generatePersonOccPeriod(ResultSet rs) throws SQLException, IntegrationException{
        HumanLink pop = new HumanLink(getHuman(rs.getInt("person_personid")));
        // TODO: fix this with Occ

//        pop.setApplicationID(rs.getInt("occpermitapplication_applicationid"));
//        pop.setApplicant(rs.getBoolean("applicant"));
//        pop.setPreferredContact(rs.getBoolean("preferredcontact"));
//        pop.setApplicationPersonType(PersonType.valueOf(rs.getString("applicationpersontype")));
//        pop.setLinkActive(rs.getBoolean("active"));
        return pop;        
    }   

    public List<Integer> getPersonHistory(int userID) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Integer> al = new ArrayList<>();

        try {
            String s = "SELECT person_personid, entrytimestamp FROM loginobjecthistory "
                    + "WHERE login_userid = ? "
                    + "AND person_personid IS NOT NULL "
                    + "ORDER BY entrytimestamp DESC;";
            stmt = con.prepareStatement(s);
            stmt.setInt(1, userID);

            rs = stmt.executeQuery();
            while (rs.next()) {
                al.add(rs.getInt("person_personid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.getPerson | Unable to retrieve person", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        System.out.println("PersonIntegrator Retrieved history of size: " + al.size());
        return al;

    }

    /**
     * Calls native database method to make a Ghost from a given Person--which
     * is a copy of a person at a given time to be used in recreating official documents
     * with addresses and such, like Citations, NOVs, etc.
     * @param p of which you would like to create a Ghost
     * @param u doing the connecting
     * @return the database identifier of the sent in Person's very own ghost
     * @throws IntegrationException 
     */
    public int createGhost(Person p, User u) throws IntegrationException, BObStatusException {
        if(p == null || u == null){
            throw new BObStatusException("Cannot make ghost with null peson or U");
            
        }
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int newGhostID = 0;

        try {
            String s = "select createghostperson(p.*, ? ) from person AS p where personid = ?;";
            stmt = con.prepareStatement(s);
            stmt.setInt(1, u.getUserID());
            stmt.setInt(2, p.getHumanID());

            rs = stmt.executeQuery();
            
            while (rs.next()) {
                newGhostID = rs.getInt("createghostperson");
                System.out.println("PersonIntegrator.createGhostPerson | new ghost ID: " + newGhostID );
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.getGhost | Unable create ghost person", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return newGhostID;

    }

    /**
     * Calls database native function to make a clone of a person, which is an exact copy of 
     * a Person at a given point in time that is used to safely edit person info 
     * without allowing certain levels of users access to the primary Person record
     * from which there is no recovery of core info
     * 
     * Ghosts are friendly and invited; clones are an unedesirable manifestation
     * of the modern biotechnological era
     * 
     * @param p of which you would like to create a clone
     * @param u doing the creating of clone
     * @return the database identifer of the inputted Person's clone
     * @throws IntegrationException 
     */
    public int createClone(Person p, User u) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int newGhostID = 0;

        try {
            String s = "select createcloneperson(p.*, ? ) from person AS p where personid = ?;";
            stmt = con.prepareStatement(s);
            stmt.setInt(1, u.getUserID());
            stmt.setInt(2, p.getHumanID());

            rs = stmt.executeQuery();
            
            while (rs.next()) {
                newGhostID = rs.getInt("createcloneperson");
                System.out.println("PersonIntegrator.createClone| new clone ID: " + newGhostID );
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.createClone | Unable to make Clone person", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return newGhostID;

    }
    
   

    /**
     * Writes new value to the given person's note field. It's the Caller's job to 
     * correctly append new notes to old ones, and in this case the caller 
     * should always be the PersonCoordinator
     * @param p
     * @throws IntegrationException 
     */
    public void updatePersonNotes(Person p) throws IntegrationException {
        Connection con = getPostgresCon();
        String query = "UPDATE person SET notes = ? WHERE personid = ?;";

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, p.getNotes() );
            stmt.setInt(2, p.getHumanID());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to add note to person");
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
  
    
    /**
     * Single point of entry for all queries against the person table, one SearchParamsPerson
     * at a time
     * @param params
     * @return the PKs of person records selected by the SQL
     * @throws IntegrationException 
     */
    public List<Integer> searchForPersons(SearchParamsPerson params) throws IntegrationException {
        SearchCoordinator sc = getSearchCoordinator();
        List<Integer> persIDList = new ArrayList();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        params.appendSQL    ("SELECT DISTINCT humanid FROM public.human\n");
        params.appendSQL    ("LEFT OUTER JOIN public.contactemail ON (human.humanid = contactemail.human_humanid)\n");
        params.appendSQL    ("LEFT OUTER JOIN public.contactphone ON (human.humanid = contactphone.human_humanid)\n");
        params.appendSQL    ("WHERE humanid IS NOT NULL \n");
        
        
        // ***********************************
        // **    FILTER COM-4 OBJECT ID     **
        // ***********************************
        if (!params.isBobID_ctl()){
            
            
            //******************************************************************
           // **   FILTERS COM-1, COM-2, COM-3, COM-6 MUNI,DATES,USER,ACTIVE  **
           // ******************************************************************
            params = (SearchParamsPerson) sc.assembleBObSearchSQL_muniDatesUserActive(params, 
                                                                    null,
                                                                    PERSON_ACTIVE_FIELD);
            

            // ***********************************
            // **    FILTER PERS-1              **
            // ***********************************
            if (params.isName_ctl()){
                params.appendSQL("AND name ILIKE ? ");
            }
            
            // ***********************************
            // **    FILTER PERS-2              **
            // ***********************************
            if (params.isUnder18_ctl()){
                params.appendSQL("AND under19= ");
                if(params.isUnder18_val()){
                    params.appendSQL("TRUE ");
                } else {
                    params.appendSQL("FALSE ");
                }
            }

            // ***********************************
            // **    FILTER PERS-3              **
            // ***********************************
            
            if(params.isBusinessEntity_ctl()){
                params.appendSQL("AND businessentity=");
                if(params.isBusinessEntity_val()){
                    params.appendSQL("TRUE ");
                } else {
                    params.appendSQL("FALSE ");
                }
            }
          
            // ***********************************
            // **    FILTER PERS-4              **
            // ***********************************
            
            if(params.isMultiHuman_ctl()){
                params.appendSQL("AND multihuman=");
                if(params.isMultiHuman_val()){
                    params.appendSQL("TRUE ");
                } else {
                    params.appendSQL("FALSE ");
                }
            }
            
            // ***********************************
            // **    FILTER PERS-5              **
            // ***********************************
            if (params.isJobTitle_ctl()){
                params.appendSQL("AND jobtitle ILIKE ? ");
            }
            
            // ***********************************
            // **    FILTER PERS-6              **
            // ***********************************
            if (params.isPhoneNumber_ctl()){
                params.appendSQL("AND contactphone.phonenumber ILIKE ? ");
            }
            
            // ***********************************
            // **    FILTER PERS-7              **
            // ***********************************
            if (params.isEmail_ctl()){
                params.appendSQL("AND contactemail.emailaddress ILIKE ? ");
            }
            
            // ***********************************
            // **    FILTER PERS-8              **
            // ***********************************
       
             if (params.isSource_ctl()) {
                if(params.getSource_val() != null){
                    params.appendSQL("AND bobsource_sourceid=? ");
                } else {
                    params.setSource_ctl(false);
                    params.appendToParamLog("SOURCE: no BOb source object; source filter disabled");
                }
            }
            
             
        
        } else {
            params.appendSQL("AND human.humanid=? ");
        }
        params.appendSQL(";");
        
        int paramCounter = 0;
        StringBuilder str = null;
        
        try {
            stmt = con.prepareStatement(params.extractRawSQL());
            
            // filter COM-4
            if (!params.isBobID_ctl()){
                if (params.isMuni_ctl()) {
                     stmt.setInt(++paramCounter, params.getMuni_val().getMuniCode());
                }
                // filter COM-2
                if(params.isDate_startEnd_ctl()){
                    stmt.setTimestamp(++paramCounter, params.getDateStart_val_sql());
                    stmt.setTimestamp(++paramCounter, params.getDateEnd_val_sql());
                 }
                // filter COM-3
                if (params.isUser_ctl()) {
                   stmt.setInt(++paramCounter, params.getUser_val().getUserID());
                }
                
                // filter PERS-1
                if (params.isName_ctl()){
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getName_val());
                    str.append("%");
                    stmt.setString(++paramCounter, str.toString());
                }
                
                // filter PERS-2 - boolean
                // filter PERS-3 - boolean
                // filter PERS-4 - boolean
                
                // filter PERS-5
                if (params.isJobTitle_ctl()){
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getJobTitle_val());
                    str.append("%");
                    stmt.setString(++paramCounter, str.toString());
                }
                
                
                // filter PERS-6
                if (params.isPhoneNumber_ctl()){
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getPhoneNumber_val());
                    str.append("%");
                }
                
                // filter PERS-7
                if (params.isEmail_ctl()){
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getEmail_val());
                    str.append("%");
                    stmt.setString(++paramCounter, str.toString());
                }
            
                // filter PERS-8
                if (params.isSource_ctl()) {
                     stmt.setInt(++paramCounter, params.getSource_val().getSourceid());
                }
                
            } else {
                stmt.setInt(++paramCounter, params.getBobID_val());
            }
            
            params.appendToParamLog("PersonIntegrator SQL before execution: ");
            params.appendToParamLog(stmt.toString());
            
            rs = stmt.executeQuery();
            
            int counter = 0;
            int maxResults;
            if (params.isLimitResultCount_ctl()) {
                maxResults = 100;
            } else {
                maxResults = Integer.MAX_VALUE;
            }
            while (rs.next() && counter < maxResults) {
                persIDList.add(rs.getInt("humanid"));
                counter++;
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.queryPersons | Unable to search for "
                    + "persons, ", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }        
       
        return persIDList;
    }
    
    /**
     * TODO: JURPLEL needs to adapt this to humans
     * @param rs
     * @return 
     */
    public PersonChangeOrder generatePersonChangeOrder(ResultSet rs){
        PersonChangeOrder skeleton = new PersonChangeOrder();
        UserCoordinator uc = getUserCoordinator();
        
        try{
        skeleton.setPersonChangeID(rs.getInt("personchangeid"));
        skeleton.setPersonID(rs.getInt("person_personid"));
        skeleton.setFirstName(rs.getString("firstname"));
        skeleton.setLastName(rs.getString("lastname"));
        skeleton.setCompositeLastName(rs.getBoolean("compositelastname"));
        skeleton.setPhoneCell(rs.getString("phonecell"));
        skeleton.setPhoneHome(rs.getString("phonehome"));
        skeleton.setPhoneWork(rs.getString("phonework"));
        skeleton.setEmail(rs.getString("email"));
        skeleton.setAddressStreet(rs.getString("addressstreet"));
        skeleton.setAddressCity(rs.getString("addresscity"));
        skeleton.setAddressZip(rs.getString("addresszip"));
        skeleton.setAddressState(rs.getString("addressstate"));
        skeleton.setUseSeparateMailingAddress(rs.getString("useseparatemailingaddress"));
        skeleton.setMailingAddressStreet(rs.getString("mailingaddressstreet"));
        skeleton.setMailingAddressThirdLine(rs.getString("mailingaddresthirdline"));
        skeleton.setMailingAddressCity(rs.getString("mailingaddresscity"));
        skeleton.setMailingAddressZip(rs.getString("mailingaddresszip"));
        skeleton.setMailingAddressState(rs.getString("mailingaddressstate"));
        
        skeleton.setRemoved(rs.getBoolean("removed"));
        skeleton.setAdded(rs.getBoolean("added"));
        skeleton.setChangedOn(rs.getTimestamp("entryts"));
        skeleton.setApprovedOn(rs.getTimestamp("approvedondate"));
        skeleton.setApprovedBy(uc.user_getUser(rs.getInt("approvedby_userid")));
        skeleton.setChangedBy(rs.getInt("changedby_personid"));
        skeleton.setActive(rs.getBoolean("active"));
        
        } catch(SQLException | IntegrationException | BObStatusException ex){
            System.out.println("PersonIntegrator.generatePersonChangeOrder() | ERROR: " + ex);
        }
        
        return skeleton;
        
    }
    
    
    /**
     * TODO: JURPLEL needs to adapt to public occ application
     * @param order
     * @throws IntegrationException 
     */
    public void insertPersonChangeOrder(PersonChangeOrder order) throws IntegrationException{
        
        String query =  "INSERT INTO public.personchange(\n" +
"            personchangeid, person_personid, firstname, lastname, compositelastname, \n" +
"            phonecell, phonehome, phonework, email, addressstreet, addresscity, \n" +
"            addresszip, addressstate, useseparatemailingaddress, mailingaddressstreet, \n" +
"            mailingaddresthirdline, mailingaddresscity, mailingaddresszip, \n" +
"            mailingaddressstate, removed, added, entryts, \n" +
"            changedby_personid)\n" +
"    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
"            ?, ?, ?, ?, ?, ?, \n" +
"            ?, ?, ?, ?, \n" +
"            ?, ?, ?, \n" +
"            ?, ?, ?, ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
//            stmt.setInt(1, order.getHumanID());
            stmt.setString(2,order.getFirstName());
            stmt.setString(3, order.getLastName());
            stmt.setBoolean(4, order.isCompositeLastName());
            stmt.setString(5, order.getPhoneCell());
            stmt.setString(6, order.getPhoneHome());
            stmt.setString(7, order.getPhoneWork());
            stmt.setString(8, order.getEmail());
            stmt.setString(9, order.getAddressStreet());
            stmt.setString(10, order.getAddressCity());
            stmt.setString(11, order.getAddressZip());
            stmt.setString(12, order.getAddressState());
            stmt.setBoolean(13, order.isUseSeparateMailingAddress());
            stmt.setString(14, order.getMailingAddressStreet());
            stmt.setString(15, order.getMailingAddressThirdLine());
            stmt.setString(16, order.getMailingAddressCity());
            stmt.setString(17, order.getMailingAddressZip());
            stmt.setString(18, order.getMailingAddressState());
            stmt.setBoolean(19, order.isRemoved());
            stmt.setBoolean(20, order.isAdded());
            stmt.setTimestamp(21, Timestamp.valueOf(LocalDateTime.now()));
//            stmt.setInt(22, order.getHumanID());

            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert person change order ", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    /**
     * TODO: JURPLEL needs to adapt this to humans on the public side
     * @param order
     * @throws IntegrationException 
     */
    public void updatePersonChangeOrder(PersonChangeOrder order) throws IntegrationException{
        
        String query =  "UPDATE public.personchange\n" +
"   SET person_personid=?, firstname=?, lastname=?, \n" +
"       compositelastname=?, phonecell=?, phonehome=?, phonework=?, email=?, \n" +
"       addressstreet=?, addresscity=?, addresszip=?, addressstate=?, \n" +
"       useseparatemailingaddress=?, mailingaddressstreet=?, mailingaddresthirdline=?, \n" +
"       mailingaddresscity=?, mailingaddresszip=?, mailingaddressstate=?, \n" +
"       removed=?, added=?, approvedondate=?, approvedby_userid=?, \n"
                + "changedby_personid=?, active=?\n" +
" WHERE personchangeid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
//            stmt.setInt(1, order.getHumanID());
            stmt.setString(2, order.getFirstName());
            stmt.setString(3, order.getLastName());
            stmt.setBoolean(4, order.isCompositeLastName());
            stmt.setString(5, order.getPhoneCell());
            stmt.setString(6, order.getPhoneHome());
            stmt.setString(7, order.getPhoneWork());
            stmt.setString(8, order.getEmail());
            stmt.setString(9, order.getAddressStreet());
            stmt.setString(10, order.getAddressCity());
            stmt.setString(11, order.getAddressZip());
            stmt.setString(12, order.getAddressState());
            stmt.setBoolean(13, order.isUseSeparateMailingAddress());
            stmt.setString(14, order.getMailingAddressStreet());
            stmt.setString(15, order.getMailingAddressThirdLine());
            stmt.setString(16, order.getMailingAddressCity());
            stmt.setString(17, order.getMailingAddressZip());
            stmt.setString(18, order.getMailingAddressState());
            stmt.setBoolean(19, order.isRemoved());
            stmt.setBoolean(20, order.isAdded());
            stmt.setTimestamp(21, order.getApprovedOn());
            if (order.getApprovedBy() != null) {
                stmt.setInt(22, order.getApprovedBy().getUserID());
            } else {
                stmt.setNull(22, java.sql.Types.NULL);
            }
            stmt.setInt(23, order.getChangedBy());
            stmt.setBoolean(24, order.isActive());
            stmt.setInt(25, order.getPersonChangeID());
            
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update person change order ", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    /**
     * TODO: JURPLEL needs to adapt this
     * @param personID
     * @return
     * @throws IntegrationException 
     */
    public List<PersonChangeOrder> getPersonChangeOrderList(int personID) throws IntegrationException{
        
        String query =  "SELECT personchangeid, person_personid, firstname, lastname, compositelastname, \n" +
                        "       phonecell, phonehome, phonework, email, addressstreet, addresscity, \n" +
                        "       addresszip, addressstate, useseparatemailingaddress, mailingaddressstreet, \n" +
                        "       mailingaddresthirdline, mailingaddresscity, mailingaddresszip, \n" +
                        "       mailingaddressstate, removed, added, entryts, approvedondate, \n" +
                        "       approvedby_userid, changedby_userid, changedby_personid, active\n" +
                        "  FROM public.personchange\n" +
                        "  where person_personid = ? AND active = true AND approvedon IS NULL;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        List<PersonChangeOrder> orderList = new ArrayList<>();

        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, personID);
            
            ResultSet rs = stmt.executeQuery();

            while(rs.next()){
                orderList.add(generatePersonChangeOrder(rs));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to get person change order list", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return orderList;
        
    }
    
    /**
     * TODO: JURPLEL: Adapt to humans
     * @param personID
     * @return
     * @throws IntegrationException 
     */
    public List<PersonChangeOrder> getPersonChangeOrderListAll(int personID) throws IntegrationException{
        
        String query =  "SELECT personchangeid, person_personid, firstname, lastname, compositelastname, \n" +
"       phonecell, phonehome, phonework, email, addressstreet, addresscity, \n" +
"       addresszip, addressstate, useseparatemailingaddress, mailingaddressstreet, \n" +
"       mailingaddresthirdline, mailingaddresscity, mailingaddresszip, \n" +
"       mailingaddressstate, removed, added, entryts, approvedondate, \n" +
"       approvedby_userid, changedby_userid, changedby_personid, active\n" +
"  FROM public.personchange\n" +
"  where person_personid = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        List<PersonChangeOrder> orderList = new ArrayList<>();

        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, personID);
            
            ResultSet rs = stmt.executeQuery();

            while(rs.next()){
                orderList.add(generatePersonChangeOrder(rs));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to get person change order list", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return orderList;
        
    }
    
} // close class