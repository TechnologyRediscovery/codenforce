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
import com.tcvcog.tcvce.application.IFace_pinnable;
import com.tcvcog.tcvce.application.interfaces.IFace_Loggable;
import com.tcvcog.tcvce.coordinators.MunicipalityCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CasePhaseEnum;
import com.tcvcog.tcvce.entities.IFace_noteHolder;
import com.tcvcog.tcvce.entities.IFace_trackedEntityLink;
import com.tcvcog.tcvce.entities.Icon;
import com.tcvcog.tcvce.entities.PropertyUseType;
import com.tcvcog.tcvce.entities.ImprovementSuggestion;
import com.tcvcog.tcvce.entities.IntensityClass;
import com.tcvcog.tcvce.entities.IntensitySchema;
import com.tcvcog.tcvce.entities.LinkedObjectRole;
import com.tcvcog.tcvce.entities.LinkedObjectSchemaEnum;
import com.tcvcog.tcvce.entities.ListChangeRequest;
import com.tcvcog.tcvce.entities.Manageable;
import com.tcvcog.tcvce.entities.ManagedSchemaEnum;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PrintStyle;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.TrackedEntity;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPermit;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;

/**
 * A catch all location for database operations against tables
 * that support Business objects across all sorts of subsystems
 * 
 * @author ellen bascomb of apt 31y
 */
public class SystemIntegrator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of SystemIntegrator
     */
    public SystemIntegrator() {
    }
    
    /**
     * The Universal Note Appending method
     * @param nh with the notes ready for insertion. NO PROCESSING OF THE NOTE
     * field is done by this method. Use the Universal note appending tools
     * of this class to do so and only pass it here when the new note and meta data
     * has been appended to the original notes and that whole blob written back 
     * into notes. I just call getNotes() and stick that sucker in!
     * @throws IntegrationException 
     */
    public void writeNotes(IFace_noteHolder nh) throws IntegrationException{
         if(nh != null && nh.getDBKey() != 0){
            
            
            StringBuilder sb = new StringBuilder();
            sb.append("UPDATE ");
            sb.append(nh.getDBTableName());
            sb.append(" SET notes=? WHERE ");
            sb.append(nh.getPKFieldName());
            sb.append("=?;");
            Connection con = getPostgresCon();
            PreparedStatement stmt = null;

            try {
                stmt = con.prepareStatement(sb.toString());
                stmt.setString(1, nh.getNotes());
                stmt.setInt(2, nh.getDBKey());
                
                stmt.executeUpdate();

            } catch (SQLException ex) {
                System.out.println(ex.toString());
                throw new IntegrationException("Tracked Entity has been deactivated", ex);

            } finally{
                 if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
                 if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            } // close finally
        }
        
        
    }
    
    /**
     * Writes an entry to the pinning tables for a given pinnable
     * @param pinnable 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void pinObject(IFace_pinnable pinnable) throws IntegrationException{
    
        if(pinnable == null || pinnable.getDBKey() == 0 || pinnable.getPinner() == null){
            throw new IntegrationException("Cannot pin object with null pinnable, user, or cecase");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(pinnable.getPinTableName());
        sb.append("(");
        sb.append(pinnable.getPinTableFKString());
        sb.append(", pinnedby_userid, createdts, deactivatedts) VALUES (?, ?, now(), NULL);");
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, pinnable.getPinner().getUserID());
            stmt.setInt(2, pinnable.getDBKey());

            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("SystemIntegrator.pinObject | Tracked Entity has been deactivated", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Deactivates the pinning entry for the given pinnable
     * @param pinnable 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void unPinObject(IFace_pinnable pinnable) throws IntegrationException{
        
        if(pinnable == null || pinnable.getDBKey() == 0 || pinnable.getPinner() == null){
            throw new IntegrationException("Cannot pin object with null pinnable, user, or cecase");
        }
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("UPDATE public.cecasepin\n");
        sb.append("   SET deactivatedts=now() WHERE ");
        sb.append(pinnable.getPinTableFKString());
        sb.append("=? AND pinnedby_userid=?;");
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, pinnable.getDBKey());
            stmt.setInt(2, pinnable.getPinner().getUserID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("SystemIntegrator.unPinObject: Database error: Pin record cannot be deactivated", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
    /**
     * Queries the appropriate pinning table to determine the 
     * current pinning status of the given pinnable
     * @param pinnable
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public boolean getPinnedStatus(IFace_pinnable pinnable) throws IntegrationException{
        boolean pinned = false;
        if(pinnable == null || pinnable.getPinner() == null){
            throw new IntegrationException("Cannot query pinned status with null pinable or null user");
        }
        

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT createdts ");
        sb.append(" FROM ");
        sb.append(pinnable.getPinTableName());
        sb.append(" WHERE ");
        sb.append(pinnable.getPinTableFKString());
        sb.append("=? AND pinnedby_userid=?;");

        try {
            stmt = con.prepareStatement(sb.toString());

            stmt.setInt(1, pinnable.getDBKey());
            stmt.setInt(2, pinnable.getPinner().getUserID());

            rs = stmt.executeQuery();

            while (rs.next()) { 
                System.out.println("SystemIntegrator.getPinnedStatus | Found record in " + pinnable.getPinTableName() + " for PK " + pinnable.getDBKey());
                pinned = true;
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("SystemIntegrator.getPinnedStatus | fatal integration error", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return pinned;
    }
    
    
    
    /**
     * Utility method for populating record tracking fields:
     * createdts
     * createdby_userid
     * lastupdatedts
     * lastupdatedby_userid
     * deactivatedts
     * deactivated_userid
     * @param te
     * @param rs
     * @param userIDOnly To avoid recursion, set to True to only inject UserIDs on 
     * creation, update, and deactivation 
     * @throws SQLException 
     */
    public void populateTrackedFields(TrackedEntity te, ResultSet rs, boolean userIDOnly) throws SQLException, IntegrationException, BObStatusException{
        UserCoordinator uc = getUserCoordinator();
        
        if(rs != null){
            
            if(rs.getTimestamp("createdts") != null){
                te.setCreatedTS(rs.getTimestamp("createdts").toLocalDateTime());                
            }
            if(rs.getInt("createdby_userid") != 0){
                if(userIDOnly){
                    te.setCreatedByUserID(rs.getInt("createdby_userid"));
                } else {
                    te.setCreatedBy(uc.user_getUser(rs.getInt("createdby_userid")));
                }
            }
            
            if(rs.getTimestamp("lastupdatedts") != null){
                te.setLastUpdatedTS(rs.getTimestamp("lastupdatedts").toLocalDateTime());
            }
            if(rs.getInt("lastupdatedby_userid") != 0){
                if(userIDOnly){
                    te.setLastUpdatedByUserID((rs.getInt("lastupdatedby_userid")));
                } else {
                    te.setLastUpdatedBy(uc.user_getUser(rs.getInt("lastupdatedby_userid")));
                }
            }
            
            if(rs.getTimestamp("deactivatedts") != null){
                te.setDeactivatedTS(rs.getTimestamp("deactivatedts").toLocalDateTime());
            }
            if(rs.getInt("deactivatedby_userid") != 0){
                if(userIDOnly){
                    te.setDeactivatedByUserID(rs.getInt("deactivatedby_userid"));
                } else {
                    te.setDeactivatedBy(uc.user_getUser(rs.getInt("deactivatedby_userid")));
                }
            }
        }
    }

    
    /**
     * Utility method for populating record tracking fields:
     * createdts
     * createdby_userid
     * lastupdatedts
     * lastupdatedby_userid
     * deactivatedts
     * deactivated_userid
     * @param te
     * @param rs
     * @throws SQLException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    protected void populateTrackedLinkFields(IFace_trackedEntityLink te, ResultSet rs) throws SQLException, IntegrationException, BObStatusException{
        UserIntegrator ui = getUserIntegrator();
        
        if(rs != null){
            
            if(rs.getTimestamp("createdts") != null){
                te.setLinkCreatedTS(rs.getTimestamp("createdts").toLocalDateTime());                
            }
            if(rs.getInt("createdby_userid") != 0){
                te.setLinkCreatedByUserID(rs.getInt("createdby_userid"));
            }
            
            if(rs.getTimestamp("lastupdatedts") != null){
                te.setLinkLastUpdatedTS(rs.getTimestamp("lastupdatedts").toLocalDateTime());
            }
            if(rs.getInt("lastupdatedby_userid") != 0){
                te.setLinkLastUpdatedByUserID(rs.getInt("lastupdatedby_userid"));
            }
            
            if(rs.getTimestamp("deactivatedts") != null){
                te.setLinkDeactivatedTS(rs.getTimestamp("deactivatedts").toLocalDateTime());
            }
            if(rs.getInt("deactivatedby_userid") != 0){
                te.setLinkDeactivatedByUserID(rs.getInt("deactivatedby_userid"));
            }
            if(rs.getInt("source_sourceid") != 0){
                te.setLinkSource(getBOBSource(rs.getInt("source_sourceid")));
            }
            te.setLinkNotes(rs.getString("notes"));
            
        }
    }
    
    
    /** Unified pathway for deactivating TrackedEntities
     * 
     * @param te
     * @param ua
     * @throws IntegrationException 
     */
    public void deactivateTrackedEntity(TrackedEntity te, UserAuthorized ua) throws IntegrationException{
        if(te != null && ua != null){
            
            
            StringBuilder sb = new StringBuilder();
            sb.append("UPDATE ");
            sb.append(te.getDBTableName());
            sb.append(" SET deactivatedts=now() AND deactivatedby_userid=? WHERE ");
            sb.append(te.getPKFieldName());
            sb.append("=?;");
            Connection con = getPostgresCon();
            PreparedStatement stmt = null;

            try {
                stmt = con.prepareStatement(sb.toString());
                stmt.setInt(1, ua.getUserID());
                stmt.setInt(2, te.getDBKey());
                
                stmt.executeUpdate();

            } catch (SQLException ex) {
                System.out.println(ex.toString());
                throw new IntegrationException("Tracked Entity has been deactivated", ex);

            } finally{
                 if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
                 if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            } // close finally
        }
    }
    
    
    /**
     * Reverses a deactivation action by writing NULL to the deactivatedts and deactivatedby_userid
     * fields of a TrackedEntity
     * 
     * @param te
     * @param ua
     * @throws IntegrationException 
     */
    public void reactivateTrackedEntity(TrackedEntity te, UserAuthorized ua) throws IntegrationException{
         if(te != null && ua != null){
            
            
            StringBuilder sb = new StringBuilder();
            sb.append("UPDATE ");
            sb.append(te.getDBTableName());
            sb.append(" SET deactivatedts=NULL AND deactivatedby_userid=NULL WHERE ");
            sb.append(te.getPKFieldName());
            sb.append("=?;");
            Connection con = getPostgresCon();
            PreparedStatement stmt = null;

            try {
                stmt = con.prepareStatement(sb.toString());
                stmt.setInt(1, te.getDBKey());
                
                stmt.executeUpdate();

            } catch (SQLException ex) {
                System.out.println(ex.toString());
                throw new IntegrationException("Tracked Entity has been deactivated", ex);

            } finally{
                 if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
                 if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            } // close finally
        }
    }
    
    /**
     * Retrieves a linked object role record from the DB
     * @param roleid
     * @return the new object or null if roleid is 0
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public LinkedObjectRole getLinkedObjectRole(int roleid) throws IntegrationException{
        LinkedObjectRole lor = null;
        if(roleid != 0){
            
            Connection con = getPostgresCon();
            ResultSet rs = null;
            PreparedStatement stmt = null;
            
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT lorid, lorschema, title, description,  \n" );
            sb.append(" createdts, deactivatedts, notes\n");
            sb.append("FROM public.linkedobjectrole WHERE lorid=?;");

            try {
                stmt = con.prepareStatement(sb.toString());
                stmt.setInt(1, roleid);
                
                rs = stmt.executeQuery();
                
                while (rs.next()) {
                    lor = generateLinkedObjectRole(rs);
                    
                }
            } catch (SQLException ex) {
                System.out.println(ex.toString());
                throw new IntegrationException("unable to get linked object role", ex);
            } finally {
                if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
                if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
                if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
            } // close finally
        }
        return lor;
    }
    
    
     /**
     * Utility method for checking database status
     * @return a string with all the patches and their TS
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public String getDatabasePatchRecord() throws IntegrationException{
            
            Connection con = getPostgresCon();
            ResultSet rs = null;
            PreparedStatement stmt = null;
            
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT patchnum, patchfilename, datepublished, patchauthor, notes\n" +
                        "  FROM public.dbpatch;");
            StringBuilder res = new StringBuilder();
            try {
                stmt = con.prepareStatement(sb.toString());
                
                rs = stmt.executeQuery();
                
                while (rs.next()) {
                    res.append(rs.getInt("patchnum"));
                    res.append(" | ");
                    
                }
            } catch (SQLException ex) {
                System.out.println(ex.toString());
                throw new IntegrationException("unable to get linked object role", ex);
            } finally {
                if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
                if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
                if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
            } // close finally
        return res.toString();
    }
    
    /**
     * Extracts all database IDs of roles by schema enum
     * @param lose
     * @return 
     */
    public List<Integer> getLinkedObjectRoleListBySchemaFamily(LinkedObjectSchemaEnum lose) throws IntegrationException, BObStatusException{
        if(lose == null){
            throw new BObStatusException("Cannot fetch roles given null enum");
            
        }
        List<Integer> idl = new ArrayList<>();

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT lorid FROM public.linkedobjectrole WHERE lorschema = CAST ( ? AS linkedobjectroleschema);");

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setString(1, lose.getRoleSChemaTypeString());

            rs = stmt.executeQuery();

            while (rs.next()) {
                idl.add(rs.getInt("lorid"));

            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to get linked object role ID list", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } 
        return idl;
    }
    
    /**
     * Populates common fields among LinkedObjectRole family
     * 
     * @param rs containing each common field in linked objects
     * @return the superclass ready to be injected into a subtype
     * @throws java.sql.SQLException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public LinkedObjectRole generateLinkedObjectRole(ResultSet rs) throws SQLException, IntegrationException{
        LinkedObjectRole lor = new LinkedObjectRole();
         if(rs != null){
            
            lor.setRoleID(rs.getInt("lorid"));
            
            lor.setTitle((rs.getString("title")));
            
            try{
                // this enum lookup makes all the code reuse possible since the
                // enum contains table identifiers that allows for dynamic generation
                // of the SQL necessary to retrieve, update, and deactivate human links to
                // any other object in the system the is a humanListHolder
                lor.setSchema(LinkedObjectSchemaEnum.valueOf(rs.getString("lorschema")));
            } catch (IllegalArgumentException ex){
                System.out.println("SystemIntgrator.generateLinkedObjectRole | Could not match Linked Object Role with Schema!");
                System.out.println(ex);
            }
            
            if(rs.getTimestamp("createdts") != null){
                lor.setCreatedTS(rs.getTimestamp("createdts").toLocalDateTime());                
            }
            lor.setDescription(rs.getString("description"));
            
          
            if(rs.getTimestamp("deactivatedts") != null){
                lor.setDeactivatedTS(rs.getTimestamp("deactivatedts").toLocalDateTime());
            }
            lor.setNotes(rs.getString("notes"));
        }
        return lor;
    }

    
    
    public Map<String, Integer> getPrintStyleMap() throws IntegrationException {
        Connection con = getPostgresCon();
        ResultSet rs = null;
        Map<String, Integer> styleMap = new HashMap<>();
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT styleid, description FROM printstyle;");

        try {
            stmt = con.prepareStatement(sb.toString());
            rs = stmt.executeQuery();
            while (rs.next()) {
                styleMap.put(rs.getString("description"), rs.getInt("styleid"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to generate printstyle", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return styleMap;
    }

    public PrintStyle getPrintStyle(int styleID) throws IntegrationException {
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PrintStyle style = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT styleid, description, headerimage_photodocid, headerheight, novtopmargin, \n"
                + "       novaddresseleftmargin, novaddressetopmargin, browserheadfootenabled, novtexttopmargin\n"
                + "  FROM public.printstyle WHERE styleid=?;");
        Icon i = null;

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, styleID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                style = generatePrintStyle(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to generate printstyle", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return style;

    }
    
    private PrintStyle generatePrintStyle(ResultSet rs) throws SQLException {
        PrintStyle style = new PrintStyle();

        style.setStyleID(rs.getInt("styleid"));

        style.setDescription(rs.getString("description"));
        style.setHeader_img_id(rs.getInt("headerimage_photodocid"));
        style.setHeader_height(rs.getInt("headerheight"));

        style.setNov_page_margin_top(rs.getInt("novtopmargin"));
        style.setNov_addressee_margin_left(rs.getInt("novaddresseleftmargin"));
        style.setNov_addressee_margin_top(rs.getInt("novaddressetopmargin"));
        style.setNov_text_margin_top(rs.getInt("novtexttopmargin"));

        return style;
    }
    
    
    
    
    
    
    
    
    
    
    /*------------------------------------------------------------------------------------
    
    Manageable Utils
    
    ------------------------------------------------------------------------------------*/
    
    /**
     * 
     * @param tableName Name of the target table to search
     * @param target    Name of the target column
     * @param targetID  Identifier of the target
     * @return int number of uses of targetID found in tableName on column target
     * @throws IntegrationException 
     */
    public int checkForUse(String tableName, String target, int targetID) throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(target);
        sb.append(" FROM ");
        sb.append(tableName);
        sb.append(" WHERE ");
        sb.append(target);
        sb.append("=?;");
        int uses = 0;

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, targetID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                uses++;     
                rs.getInt(target);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to locate " + target + ": " + targetID, ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return uses;
    }
    
    /**
     * Searches the DB for Foreign keys related to String search
     * @param search String to search for foreign keys
     * @return List<String> of tables with foreign keys to search
     * @throws IntegrationException 
     */
    public List<String> findForeignUseTables(String search) throws IntegrationException {
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        List<String> results = new ArrayList();
        sb.append("SELECT TABLE_NAME ");
        //sb.append("CONSTRAINT_NAME, ");
        //sb.append("CONSTRAINT_TYPE ");
        sb.append("FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS ");
        sb.append("WHERE CONSTRAINT_NAME LIKE '%").append(search).append("_fk'; ");
        
        try {
            stmt = con.prepareStatement(sb.toString());
            rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(rs.getString("TABLE_NAME"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate table_name", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return results;
    }
    
    /**
     * Checks a Manageable for use in the DB
     * @param m
     * @return int times used in the DB
     * @throws IntegrationException 
     */
    public int checkManagableForUse(Manageable m) throws IntegrationException { 
        ManagedSchemaEnum e = m.getMANAGEABLE_SCHEMA();
        
        int uses = 0;
        List<String> useTables = findForeignUseTables(e.getTARGET_TABLE_ID_FIELD());
        for(int x = 0; x < useTables.size(); x++){
            uses =+ checkForUse("public." + useTables.get(x), e.getFK_ID_FIELD(), m.getID());
            //System.out.println("Checked public." + useTables.get(x) + " for icon_iconid:" + i.getIconID());
        }
        return uses;
    }
    
    
    /*------------------------------------------------------------------------------------
    
    Icon
    
    ------------------------------------------------------------------------------------*/
    
    /**
     * 
     * @param iconID
     * @return Icon of the iconID
     * @throws IntegrationException 
     */
    public Icon getIcon(int iconID) throws IntegrationException {
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT iconid, name, styleclass, fontawesome, materialicons, deactivatedts ");
        sb.append("FROM public.icon WHERE iconid=?;");
        Icon i = null;

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, iconID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                i = new Icon();
                i.setID(rs.getInt("iconid"));
                i.setName(rs.getString("name"));
                i.setStyleClass(rs.getString("styleclass"));
                i.setFontAwesome(rs.getString("fontawesome"));
                i.setMaterialIcon(rs.getString("materialicons"));
                if(rs.getTimestamp("deactivatedts") == null){
                    i.setDeactivatedts(null);
                }else{
                    i.setDeactivatedts(rs.getTimestamp("deactivatedts").toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to generate icon", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return i;

    }
    
    public void deactivateIcon(Icon i) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE public.icon SET deactivatedts=?");
        sb.append("WHERE iconid=?");
        
        try{
            stmt = con.prepareStatement(sb.toString());
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, i.getID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to deactivate icon", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally     
    }

    public void updateIcon(Icon i) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE public.icon SET name=?, styleclass=?, fontawesome=?, materialicons=?, deactivatedts=? ");
        sb.append("WHERE iconid = ?;");

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setString(1, i.getName());
            stmt.setString(2, i.getStyleClass());
            stmt.setString(3, i.getFontAwesome());
            stmt.setString(4, i.getMaterialIcon());
            if(i.getDeactivatedts() == null){
                stmt.setTimestamp(5, null);
            }else{
                stmt.setTimestamp(5, Timestamp.valueOf(i.getDeactivatedts()));
            }
            stmt.setInt(6, i.getID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to update icon", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
    }

    public void insertIcon(Icon i) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO public.icon(");
        sb.append("iconid, name, styleclass, fontawesome, materialicons) ");
        sb.append("VALUES (DEFAULT, ?, ?, ?, ?);");

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setString(1, i.getName());
            stmt.setString(2, i.getStyleClass());
            stmt.setString(3, i.getFontAwesome());
            stmt.setString(4, i.getMaterialIcon());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to insert icon", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
    }

    /**
     * 
     * @return the Icon List active only
     * @throws IntegrationException 
     */
    public List<Icon> getIconList() throws IntegrationException{
        return getIconList(false);
    }
    
    /**
     * 
     * @param includeDeactivated
     * @return the Icon List deactivated Included base on includeDeactivated boolean
     * @throws IntegrationException 
     */
    public List<Icon> getIconList(boolean includeDeactivated) throws IntegrationException {
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        List<Icon> iList = new ArrayList<>();

        try {
            if(includeDeactivated){
                sb.append("SELECT iconid FROM public.icon;");
            }else{
                sb.append("SELECT iconid FROM public.icon WHERE deactivatedts IS NULL;");
            }
            stmt = con.prepareStatement(sb.toString());
            rs = stmt.executeQuery();
            while (rs.next()) {
                iList.add(getIcon(rs.getInt("iconid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate icon", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return iList;
    }
    
    /*------------------------------------------------------------------------------------
    
    Property Use Type
    
    ------------------------------------------------------------------------------------*/
    
    public int putCheckForUse(PropertyUseType p) throws IntegrationException {
        int uses = 0;
        List<String> useTables = findForeignUseTables("propertyusetypeid");
        for(int x = 0; x < useTables.size(); x++){
            uses =+ checkForUse("public." + useTables.get(x), "usetype_typeid", p.getTypeID());
            System.out.println("Checked public." + useTables.get(x) + " for  usetype_typeid" + p.getTypeID());
        }
        return uses;
    }
    
    public PropertyUseType getPut(int putID) throws IntegrationException {
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT propertyusetypeid, name, description, icon_iconid, zoneclass, deactivatedts ");
        sb.append("FROM public.propertyusetype WHERE propertyusetypeid=?;");
        PropertyUseType p = null;

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, putID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                p = new PropertyUseType();
                p.setTypeID(rs.getInt("propertyusetypeid"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setIcon(getIcon(rs.getInt("icon_iconid")));
                p.setZoneClass(rs.getString("zoneclass"));
                if(rs.getTimestamp("deactivatedts") == null){
                    p.setDeactivatedts(null);
                }else{
                    p.setDeactivatedts(rs.getTimestamp("deactivatedts").toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to generate put", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return p;
    }
    
    public void deactivatePut(PropertyUseType p) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE public.propertyusetype SET deactivatedts=?");
        sb.append("WHERE propertyusetypeid=?");
        
        try{
            stmt = con.prepareStatement(sb.toString());
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, p.getID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to deactivate put", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally 
    }
    
    public void updatePut(PropertyUseType p) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE public.propertyusetype SET name=?, description=?, icon_iconid=?, zoneclass=?, deactivatedts=? ");
        sb.append("WHERE propertyusetypeid = ?;");

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setString(1, p.getName());
            stmt.setString(2, p.getDescription());
            if(p.getIcon() == null){
                stmt.setNull(3, Types.INTEGER);
            }else{
                stmt.setInt(3, p.getIcon().getID());
            }
            stmt.setString(4, p.getZoneClass());
            if(p.getDeactivatedts() == null){
                stmt.setTimestamp(5, null);
            }else{
                stmt.setTimestamp(5, Timestamp.valueOf(p.getDeactivatedts()));
            }
            stmt.setInt(6, p.getTypeID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to update put", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
    }
    
    public void insertPut(PropertyUseType p) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO public.propertyusetype(");
        sb.append("propertyusetypeid, name, description, icon_iconid, zoneclass, deactivatedts) ");
        sb.append("VALUES (DEFAULT, ?, ?, ?, ?, ?);");

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setString(1, p.getName());
            stmt.setString(2, p.getDescription());
            stmt.setInt(3, p.getIcon().getID());
            stmt.setString(4, p.getZoneClass());
            stmt.setTimestamp(5, null);
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to insert PropertyUseType", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
    }
    
    public List<PropertyUseType> getPutList() throws IntegrationException {
        return getPutList(false);
    }
    
    public List<PropertyUseType> getPutList(boolean includeDeactivated) throws IntegrationException {
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        if(includeDeactivated){
            sb.append("SELECT propertyusetypeid FROM public.propertyusetype;");
        }else{
            sb.append("SELECT propertyusetypeid FROM public.propertyusetype WHERE deactivatedts IS NULL;");
        }
        List<PropertyUseType> putList = new ArrayList<>();
        try {
            stmt = con.prepareStatement(sb.toString());
            rs = stmt.executeQuery();
            while (rs.next()) {
                putList.add(getPut(rs.getInt("propertyusetypeid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate propertyUseType(List)", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return putList;
    }
    
    public void insertImprovementSuggestion(ImprovementSuggestion is) throws IntegrationException {

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        String s = " INSERT INTO public.improvementsuggestion(\n"
                + "            improvementid, improvementtypeid, improvementsuggestiontext, \n"
                + "            improvementreply, statusid, submitterid, submissiontimestamp)\n"
                + "    VALUES (DEFAULT, ?, ?, \n"
                + "            NULL, ?, ?, now());";

        try {
            stmt = con.prepareStatement(s);
            stmt.setInt(1, is.getImprovementTypeID());
            stmt.setString(2, is.getSuggestionText());
            // hard-coded status for expediency
            stmt.setInt(3, is.getStatusID());
            stmt.setInt(4, is.getSubmitter().getUserID());
            System.out.println("PersonIntegrator.getPersonListByPropertyID | sql: " + stmt.toString());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert improvement suggestion, sorry", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally
    }

    final String impSugQuery = "SELECT improvementid, improvementtypeid, improvementsuggestiontext, \n"
            + "       improvementreply, statusid, statustitle, typetitle, submitterid, submissiontimestamp\n"
            + "  FROM public.improvementsuggestion INNER JOIN improvementstatus USING (statusid)\n"
            + "  INNER JOIN improvementtype ON improvementtypeid = typeid;";

    public ArrayList<ImprovementSuggestion> getImprovementSuggestions() throws IntegrationException, BObStatusException {
        ArrayList<ImprovementSuggestion> impList = new ArrayList<>();

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(impSugQuery);
            rs = stmt.executeQuery();
            while (rs.next()) {
                impList.add(generateImprovementSuggestion(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build improvement suggestion due to an DB integration error", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return impList;
    }

    public ResultSet getImprovementSuggestionsRS() throws IntegrationException {
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(impSugQuery);
            rs = stmt.executeQuery();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to get improvement suggestion RS due to an DB integration error", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
//             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return rs;
    }

    private ImprovementSuggestion generateImprovementSuggestion(ResultSet rs) throws SQLException, IntegrationException, BObStatusException {
        UserCoordinator uc = getUserCoordinator();
        ImprovementSuggestion is = new ImprovementSuggestion();
        is.setSuggestionID(rs.getInt("improvementid"));
        is.setSubmitter(uc.user_getUser(rs.getInt("submitterid")));
        is.setImprovementTypeID(rs.getInt("improvementtypeid"));
        is.setImprovementTypeStr(rs.getString("typetitle"));
        is.setReply(rs.getString("improvementreply"));
        is.setStatusID(rs.getInt("statusid"));
        is.setStatusStr(rs.getString("statustitle"));
        is.setSuggestionText(rs.getString("improvementsuggestiontext"));
        is.setSubmissionTimeStamp(rs.getTimestamp("submissiontimestamp").toLocalDateTime());
        return is;
    }

    public void insertListChangeRequest(ListChangeRequest lcr) throws IntegrationException {

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        String s = " INSERT INTO public.listchangerequest(\n"
                + " changeid, changetext)\n"
                + " VALUES (DEFAULT, ?);";

        try {
            stmt = con.prepareStatement(s);
            stmt.setString(1, lcr.getChangeRequestText());
            System.out.println("PersonIntegrator.getPersonListByPropertyID | sql: " + stmt.toString());

            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.getPerson | Unable to retrieve person", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally
    }

    public HashMap<String, Integer> getSuggestionTypeMap() throws IntegrationException {
        HashMap<String, Integer> hm = new HashMap<>();

        String query = "SELECT typeid, typetitle, typedescription\n"
                + "  FROM public.improvementtype;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                hm.put(rs.getString("typetitle"), rs.getInt("typeid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build suggestion type map due to an DB integration error", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return hm;
    }

    /**
     * Returns all Source IDs in the bobsource table
     * @return
     * @throws IntegrationException 
     */
     public List<Integer> getBobSourceListComplete() throws IntegrationException{
          List<Integer> sidl = new ArrayList<>();
          BOBSource bs = null;
          
          String query =  " SELECT sourceid FROM public.bobsource WHERE active = TRUE;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                sidl.add(rs.getInt("sourceid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build bob source list complete due to an DB integration error", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return sidl;
     }
      
     /**
      * Getter for BOB source objects by id;
      * @param sourceID
      * @return
      * @throws IntegrationException
      * @throws BObStatusException 
      */
      public BOBSource getBOBSource(int sourceID) throws IntegrationException, BObStatusException{
          if(sourceID == 0){
              throw new BObStatusException("SystemIntegrator.getBObSource | cannot get bob source with ID = 0;");
          }
          BOBSource bs = null;
          
          String query =    "   SELECT sourceid, title, description, creator, muni_municode, userattributable, \n" +
                            "           active, notes\n" +
                            "           FROM public.bobsource WHERE sourceid = ?;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, sourceID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                bs = generateBOBSource(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to get bob source due to an DB integration error", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return bs;
    }

    private BOBSource generateBOBSource(ResultSet rs) throws SQLException, IntegrationException {
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        BOBSource bs = new BOBSource();
        bs.setSourceid(rs.getInt("sourceid"));
        bs.setTitle(rs.getString("title"));
        bs.setUserattributable(rs.getBoolean("userattributable"));
        bs.setDescription(rs.getString("description"));
        // beware of inifinte loops
        bs.setCreatorUserID(rs.getInt("creator"));
        bs.setMuni(mi.getMuni(rs.getInt("muni_municode")));
        bs.setActive(rs.getBoolean("active"));;
        bs.setNotes(rs.getString("notes"));
        return bs;

    }

    private IntensityClass generateIntensityClass(ResultSet rs) throws IntegrationException {

        IntensityClass intsty = new IntensityClass();
        MunicipalityIntegrator mi = new MunicipalityIntegrator();

        try {
            intsty.setClassID(rs.getInt("classid"));
            intsty.setTitle(rs.getString("title"));
            intsty.setMuni(mi.getMuni(rs.getInt("muni_municode")));
            intsty.setNumericRating(rs.getInt("numericrating"));
            intsty.setSchema(new IntensitySchema(rs.getString("schemaName")));
            intsty.setActive(rs.getBoolean("active"));
            intsty.setIcon(getIcon(rs.getInt("icon_iconid")));
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error generating Intensity from ResultSet", ex);
        }

        return intsty;

    }
    
    
    /**
     * Fetches intensity class by ID
     * @param intensityClassID
     * @return
     * @throws IntegrationException 
     */
    public IntensityClass getIntensityClass(int intensityClassID) throws IntegrationException{
        
        IntensityClass in = null;

        String query =  "SELECT classid, title, muni_municode, numericrating, schemaname, active, \n" +
                        "       icon_iconid\n" +
                        "  FROM public.intensityclass WHERE classid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, intensityClassID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                in = generateIntensityClass(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("SystemIntegrator.getIntensityClassList | Unable to get Intensity List", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return in;
    }
    
 
    /**
     * Utility adaptor method for legacy compatability
     * @param cat
     * @return
     * @throws IntegrationException 
     */
    public List<IntensityClass> getIntensityClassList(IntensitySchema cat) throws IntegrationException {
        
        return getIntensityClassList(cat.getLabel());
        
    }
    
    
    /**
     * A search-like method for intensity classes with any schema name like X
     * @param schemaLabel
     * @param cat
     * @return
     * @throws IntegrationException 
     */
    public List<IntensityClass> getIntensityClassList(String schemaLabel) throws IntegrationException {

        List<IntensityClass> inList = new ArrayList<>();

        String query = "SELECT classid, title, muni_municode, numericrating, schemaname, active, icon_iconid FROM intensityclass WHERE schemaname ILIKE ?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, schemaLabel);
            rs = stmt.executeQuery();
            while (rs.next()) {
                inList.add(generateIntensityClass(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("SystemIntegrator.getIntensityClassList | Unable to get Intensity List", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return inList;

    }

    public void updateIntensityClass(IntensityClass intsty) throws IntegrationException {

        String query = "UPDATE public.intensityclass\n"
                + "SET title=?, muni_municode=?, numericrating=?,\n"
                + "schemaname=?, active=?, icon_iconid=?\n"
                + "WHERE classid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, intsty.getTitle());
            stmt.setInt(2, intsty.getMuni().getMuniCode());
            stmt.setInt(3, intsty.getNumericRating());
            stmt.setString(4, intsty.getSchema().getLabel());
            stmt.setBoolean(5, intsty.isActive());
            stmt.setInt(6, intsty.getIcon().getID());
            stmt.setInt(7, intsty.getClassID());
            stmt.executeUpdate();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update Intensity", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        }

    }

    public void insertIntensityClass(IntensityClass intsty) throws IntegrationException {
        String query = "INSERT INTO public.intensityclass(classid, title, \n"
                + "muni_municode, numericrating, schemaname, \n"
                + "active, icon_iconid)\n"
                + "VALUES (DEFAULT, ?, ?, ?, ?, ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, intsty.getTitle());
            stmt.setInt(2, intsty.getMuni().getMuniCode());
            stmt.setInt(3, intsty.getNumericRating());
            stmt.setString(4, intsty.getSchema().getLabel());
            stmt.setBoolean(5, intsty.isActive());
            stmt.setInt(6, intsty.getIcon().getID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert Intensity Class", ex);

        } finally {
                if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }

    public void deleteIntensityClass(IntensityClass intsty) throws IntegrationException {
        String query = "DELETE FROM public.intensityclass\n"
                + "WHERE classid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, intsty.getClassID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete Intensity Class", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }
    
    public List<IntensitySchema> getIntensitySchemaList() throws IntegrationException {

        List<IntensitySchema> inList = new ArrayList<>();

        String query = "SELECT DISTINCT schemaname FROM intensityclass;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                inList.add(generateIntensitySchema(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("SystemIntegrator.getIntensitySchemaList | Unable to get Intensity Schema List", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return inList;

    }

    public IntensitySchema generateIntensitySchema(ResultSet rs) throws IntegrationException {
        
        String schemaName = "";

        try {
            schemaName = rs.getString("schemaname");
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error generating IntensitySchema from ResultSet", ex);
        }

        return new IntensitySchema(schemaName);

    }
    
    /**
     * Writes in a history record when a User accesses that object.
     * The Object's type will be checked against existing history
     * recording opportunities and create an appropriate entry in the
     * loginobjecthistory table.
     *
     * 
     *
     * @param u the User who viewed the object
     * @param ob any Object that's displayed in a data table or list in the system
     * @throws IntegrationException
     */
    public void logObjectView(User u, IFace_Loggable ob) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        StringBuilder insertSB = new StringBuilder();
        insertSB.append("INSERT INTO loginobjecthistory ");
        try {
            if (ob instanceof Person) {
                Person p = (Person) ob;
                
                insertSB.append("(login_userid, person_personid, entrytimestamp) VALUES (?, ?, DEFAULT); ");
                stmt = con.prepareStatement(insertSB.toString());
                
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, p.getHumanID());
                
                stmt.execute();
                
                System.out.println("SystemIntegrator.logObjectView: Person view logged id = " + p.getHumanID());
                
            } else if (ob instanceof Property) {
                Property p = (Property) ob;
                
                insertSB.append("(login_userid, property_propertyid, entrytimestamp) VALUES (?, ?, DEFAULT); ");
                stmt = con.prepareStatement(insertSB.toString());
                
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, p.getParcelKey());

                stmt.execute();

                System.out.println("SystemIntegrator.logObjectView: Property view logged id = " + p.getParcelKey());
            } else if (ob instanceof CECaseDataHeavy) {
                CECaseDataHeavy c = (CECaseDataHeavy) ob;
                
                insertSB.append("(login_userid, cecase_caseid, entrytimestamp) VALUES (?, ?, DEFAULT); ");
                stmt = con.prepareStatement(insertSB.toString());
                
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, c.getCaseID());

                stmt.execute();

                System.out.println("SystemIntegrator.logObjectView: Case view logged id = " + c.getCaseID());
            } else if (ob instanceof OccPeriod) {
                OccPeriod op = (OccPeriod) ob;

                insertSB.append("(login_userid, occperiod_periodid, entrytimestamp) VALUES (?, ?, DEFAULT); ");
                stmt = con.prepareStatement(insertSB.toString());

                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, op.getPeriodID());

                stmt.execute();
                System.out.println("SystemIntegrator.logObjectView: Occ Period logged id = " + op.getPeriodID() );
            }
            
            
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error writign object history: persons, properties, or cecases", ex);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */
                }
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
     * Writes in a history record when a User accesses that object.
     * The Object's type will be checked against existing history
     * recording opportunities and create an appropriate entry in the
     * loginobjecthistory table.
     *
     * Checks for duplicates in the table before inserting.
     * If duplicate object ID exists, update existing entry with the current
     * time stamp only.
     *
     * @deprecated 
     * @param u the User who viewed the object
     * @param ob any Object that's displayed in a data table or list in the system
     * @throws IntegrationException
     */
    public void logObjectView_OverwriteDate(User u, Object ob) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        StringBuilder selectSB = new StringBuilder();
        selectSB.append("SELECT historyentryid FROM loginobjecthistory " + "WHERE login_userid = ? ");
        StringBuilder insertSB = new StringBuilder();
        insertSB.append("INSERT INTO loginobjecthistory ");
        StringBuilder updateSB = new StringBuilder();
        updateSB.append("UPDATE loginobjecthistory SET entrytimestamp = now() " + "WHERE login_userid = ? ");
        try {
            if (ob instanceof Person) {
                Person p = (Person) ob;
                // prepare SELECT statement
                selectSB.append("AND person_personid = ? ");
                stmt = con.prepareStatement(selectSB.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, p.getHumanID());
                rs = stmt.executeQuery();
                if (rs.first()) {
                    // history entry with this user and person already exists
                    updateSB.append("AND person_personid = ? ");
                    stmt = con.prepareStatement(updateSB.toString());
                } else {
                    // pair not in history, do fresh insert
                    insertSB.append("(login_userid, person_personid, entrytimestamp) VALUES (?, ?, DEFAULT); ");
                    stmt = con.prepareStatement(insertSB.toString());
                }
                // each UPDATE and INSERT SQL structures take the params in this order
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, p.getHumanID());
                stmt.execute();
                System.out.println("SystemIntegrator.logObjectView: Person view logged id = " + p.getHumanID());
            } else if (ob instanceof Property) {
                Property p = (Property) ob;
                // prepare SELECT statement
                selectSB.append("AND property_propertyid = ? ");
                stmt = con.prepareStatement(selectSB.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, p.getParcelKey());
                rs = stmt.executeQuery();
                if (rs.first()) {
                    // history entry with this user and person already exists
                    updateSB.append("AND property_propertyid = ? ");
                    stmt = con.prepareStatement(updateSB.toString());
                } else {
                    // pair not in history, do fresh insert
                    insertSB.append("(login_userid, property_propertyid, entrytimestamp) VALUES (?, ?, DEFAULT); ");
                    stmt = con.prepareStatement(insertSB.toString());
                }
                // each UPDATE and INSERT SQL structures take the params in this order
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, p.getParcelKey());
                stmt.execute();
                System.out.println("SystemIntegrator.logObjectView: Property view logged id = " + p.getParcelKey());
            } else if (ob instanceof CECaseDataHeavy) {
                CECaseDataHeavy c = (CECaseDataHeavy) ob;
                // prepare SELECT statement
                selectSB.append("AND cecase_caseid = ? ");
                stmt = con.prepareStatement(selectSB.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, c.getCaseID());
                rs = stmt.executeQuery();
                if (rs.first()) {
                    // history entry with this user and person already exists
                    updateSB.append("AND cecase_caseid = ? ");
                    stmt = con.prepareStatement(updateSB.toString());
                } else {
                    // pair not in history, do fresh insert
                    insertSB.append("(login_userid, cecase_caseid, entrytimestamp) VALUES (?, ?, DEFAULT); ");
                    stmt = con.prepareStatement(insertSB.toString());
                }
                // each UPDATE and INSERT SQL structures take the params in this order
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, c.getCaseID());
                stmt.execute();
                System.out.println("SystemIntegrator.logObjectView: Case view logged id = " + c.getCaseID());
            } else if (ob instanceof OccPeriod) {
                OccPeriod op = (OccPeriod) ob;
                // prepare SELECT statement
                selectSB.append("AND occperiod_periodid = ? ");
                stmt = con.prepareStatement(selectSB.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, op.getPeriodID());
                rs = stmt.executeQuery();
                if (rs.first()) {
                    // history entry with this user and person already exists
                    updateSB.append("AND cecase_caseid = ? ");
                    stmt = con.prepareStatement(updateSB.toString());
                } else {
                    // pair not in history, do fresh insert
                    insertSB.append("(login_userid, cecase_caseid, entrytimestamp) VALUES (?, ?, DEFAULT); ");
                    stmt = con.prepareStatement(insertSB.toString());
                }
                // each UPDATE and INSERT SQL structures take the params in this order
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, op.getPeriodID());
                stmt.execute();
                System.out.println("SystemIntegrator.logObjectView: Occ Period logged id = " + op.getPeriodID() );
            }
            
            
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error writign object history: persons, properties, or cecases", ex);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */
                }
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
    
    //xiaohong add
    public ArrayList<PrintStyle> getPrintStyle() throws IntegrationException {

        String query = "SELECT styleid, description, headerimage_photodocid, headerheight, novtopmargin, \n"
                + "       novaddresseleftmargin, novaddressetopmargin, browserheadfootenabled, \n"
                + "       novtexttopmargin\n"
                + "  FROM public.printstyle;";

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<PrintStyle> styleList = new ArrayList<>();
        PrintStyle style = null;

        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                style = generatePrintStyle(rs);
                if(style != null){
                    styleList.add(style);
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to generate printstyle", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
        } // close finally
        return styleList;

    }
}
