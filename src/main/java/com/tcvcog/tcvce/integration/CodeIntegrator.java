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
import com.tcvcog.tcvce.coordinators.MunicipalityCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import java.io.Serializable;
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.CodeElementGuideEntry;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.occupancy.integration.PaymentIntegrator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.faces.application.FacesMessage;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class CodeIntegrator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of CodeIntegrator
     */
    public CodeIntegrator() {
        System.out.println("CodeIntegrator.CodeIntegrator");
        
    }
    
    
    // *************************************************************
    // *********************CODE SOURCES****************************
    // *************************************************************
    
    
    
    

    /**
     * Utility method for use by methods in this Integrator which allows 
     * easy filling of code-related objects with fully-baked CodeSource objects
     * Note that client methods must manage cursor positins on the ResultSet 
     * passed in.
     *     
     * 
     * @param rs
     * 
     * @param source Clients are responsible for instantiating an emtpy CodeSource
     * and passing a reference into this method
     * 
     * @return a fully-baked CodeSource from the database
     * 
     * @throws SQLException 
     */
    private CodeSource populateCodeSourceMetatdataFromRS(ResultSet rs, CodeSource source) throws SQLException{
        
        
        source.setSourceID(rs.getInt(1));
        source.setSourceName(rs.getString(2));
        source.setSourceYear(rs.getInt(3));
        source.setSourceDescription(rs.getString(4));
        source.setIsActive(rs.getBoolean(5));
        source.setUrl(rs.getString(6));
        source.setSourceNotes(rs.getString(7));
        return source;
        
    }
    
    /**
     * Retrieves code source information given a sourceID
     * @param sourceID the unique ID number of the code source in the DB
     * @return the CodeSource object built from the DB read
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public CodeSource getCodeSource(int sourceID) throws IntegrationException{
        CodeSource source = new CodeSource();

        String query = "SELECT sourceid, name, year, "
                + "description, isactive, url, "
                + "notes\n" 
                + " FROM public.codesource WHERE sourceid = ?;";
        
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, sourceID);
            rs = stmt.executeQuery();
            while(rs.next()){
                populateCodeSourceMetatdataFromRS(rs, source);

            }
            
        } catch (SQLException ex) { 
             System.out.println(ex.toString());
             throw new IntegrationException("Exception in CodeIntegrator", ex);
        } finally{
            if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return source;
    }
    
    /**
     * Takes in a CodeSource object and inserts into the DB
     * @param sourceToInsert
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void insertCodeSource(CodeSource sourceToInsert) throws IntegrationException{
        String query = "INSERT INTO public.codesource(\n" +
            "sourceid, name, year, description, isactive, url, notes)\n" +
            "VALUES (DEFAULT, ?, ?, ?, ?, ?, ?);";

        // create sql statement up here
        Connection con = null;
        PreparedStatement stmt = null;

         try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setString(1, sourceToInsert.getSourceName());
            stmt.setInt(2, sourceToInsert.getSourceYear());
            stmt.setString(3, sourceToInsert.getSourceDescription());
            stmt.setBoolean(4, sourceToInsert.isIsActive());
            stmt.setString(5, sourceToInsert.getUrl());
            stmt.setString(6, sourceToInsert.getSourceNotes());
            stmt.executeUpdate();
            System.out.println("CodeIntegrator.insertCodeSource: executed update with SQL - " + stmt.toString());
             
        } catch (SQLException ex) { 
             System.out.println(ex.toString());
             throw new IntegrationException("Cannot insert code source");
             
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    } 
    
    /**
     * Updates fields in the DB for a given CodeSource objects
     * @param sourceToUpdate
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void updateCodeSource(CodeSource sourceToUpdate) throws IntegrationException{
         String query = "UPDATE public.codesource\n" +
                "   SET name=?, year=?, description=?, isactive=?, url=?, \n" +
                "       notes=?\n" +
                " WHERE sourceid=?;";

        // create sql statement up here
        Connection con = null;
        PreparedStatement stmt = null;

         try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setString(1, sourceToUpdate.getSourceName());
            stmt.setInt(2, sourceToUpdate.getSourceYear());
            stmt.setString(3, sourceToUpdate.getSourceDescription());
            stmt.setBoolean(4, sourceToUpdate.isIsActive());
            stmt.setString(5, sourceToUpdate.getUrl());
            stmt.setString(6, sourceToUpdate.getSourceNotes());
            stmt.setInt(7, sourceToUpdate.getSourceID());
            stmt.execute();
             
        } catch (SQLException ex) { 
             System.out.println(ex.toString());
             throw new IntegrationException("cannot update code source");
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Toggles the active isactive flag on the passed code source
     * @param source the CodeSource to remove from the DB
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void deactivateCodeSource(CodeSource source) throws IntegrationException{
        if(source == null) return;
        
        String query = "UPDATE public.codesource\n" +
                "   SET isactive=FALSE WHERE sourceid=?;";

        // create sql statement up here
        Connection con = null;
        PreparedStatement stmt = null;

         try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
           
            stmt.setInt(1, source.getSourceID());
            stmt.execute();
             
        } catch (SQLException ex) { 
             System.out.println(ex.toString());
             throw new IntegrationException("Cannot deactivate code source");
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Utility method for converting a ArrayList of CodeSource objects
     * into a map with the name of the CodeSource as the key and the 
     * CodeSource object as the value. Designed for selectItem components
     * that the view likes to eat for breakfast.
     * @return fully-baked hasmap of CodeSource names and CodeSource objects
     * @throws IntegrationException 
     */
    public HashMap getCodeSourceMap() throws IntegrationException{
        ArrayList<CodeSource> csList = getCompleteCodeSourceList();
        HashMap<String, Integer> csMap = new HashMap();
        String stringKey;
        ListIterator li = csList.listIterator();
        while(li.hasNext()){
            CodeSource cs = (CodeSource) li.next();
             stringKey = cs.getSourceName() + "(" + String.valueOf(cs.getSourceYear()) + ")";
            csMap.put(stringKey, cs.getSourceID());
        }
        return csMap;
    }
    
    /**
     * Retrieves all existing code sources from the DB and builds CodeSource
     * objects for each one, populating all the fields
     * @return all code sources in the DB as CodeSource objects
     * @throws IntegrationException 
     */
    public ArrayList getCompleteCodeSourceList() throws IntegrationException{
        String query = "SELECT sourceid, name, year, "
              + "description, isactive, url, "
              + "notes\n" 
              + " FROM public.codesource WHERE isactive=TRUE;";
        
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<CodeSource> codeSources = new ArrayList();
        CodeSource source;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while(rs.next()){
                source = populateCodeSourceMetatdataFromRS(rs, new CodeSource());
                if(source != null){
                    // figure out about this possible dereferencing warning
                    codeSources.add(source);
                }
            }
            
        } catch (SQLException ex) { 
             System.out.println(ex.toString());
             throw new IntegrationException("Exception in CodeIntegrator", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return codeSources;
    }
    
    
    
    
    
    // *************************************************************
    // **************CODE ELEMENTS (ORDINANCES)*********************
    // *************************************************************
   
    
    
        /**
     * Core method which grabs data related to a single CodeElement and populates
     * a CodeElement object which is returned to the client.
     * 
     * @param elementID
     * @return the loaded up CodeElement with database data
     * @throws IntegrationException 
     */
    public CodeElement getCodeElement(int elementID) throws IntegrationException{
        CodeElement newCodeElement = null;
        PreparedStatement stmt = null;
        Connection con = getPostgresCon();
        // note that muniCode is not returned in this query since it is specified in the WHERE
        String query = "SELECT elementid, codesource_sourceid, ordchapterno, ordchaptertitle, \n" +
                        "       ordsecnum, ordsectitle, ordsubsecnum, ordsubsectitle, ordtechnicaltext, \n" +
                        "       ordhumanfriendlytext, resourceurl, guideentryid, notes, legacyid, \n" +
                        "       ordsubsubsecnum, useinjectedvalues, lastupdatedts, \n" +
                        "       createdby_userid, lastupdatedby_userid, deactivatedts, deactivatedby_userid, \n" +
                        "       createdts\n" +
                        "  FROM public.codeelement WHERE elementid=?;";
        ResultSet rs = null;
 
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, elementID);
            rs = stmt.executeQuery();
            
            // this query will only return 1 row since the WHERE clause selects from an PK column
            while(rs.next()){
                newCodeElement = generateCodeElement(rs);
                 
            }
        } catch (SQLException ex) {
            System.out.println("CodeIntegrator.getCodeElementByElementID | " + ex.toString());
            throw new IntegrationException("Exception in CodeIntegrator.getCodeElementByElementID", ex);
        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) {System.out.println(e.toString());} }
        } // close finally
        return newCodeElement;
        
    }
    
    
    /**
     * Internal Utility method for loading up a CodeElement object given
     * a result set. Used by all sorts of CodeElement related methods
     * that are getting data for many code elements back in a single result set.
     * Note that the client method is responsible for manging ResultSet cursor
     * position 
     * @param rs With cursor positioned at the row to extract and populate from
     * @param e an empty CodeElement. Client class is responsible for instantiation
     * @return a populated CodeElement extracted from that row in the ResultSet
     */
    private CodeElement generateCodeElement(ResultSet rs) throws SQLException, IntegrationException{
        if(rs == null) return null;
        CodeElement e = new CodeElement();
        UserIntegrator ui = getUserIntegrator();

        // to ease the eyes, line spacing corresponds to the field spacing in CodeElement

        e.setElementID(rs.getInt("elementid"));

        e.setGuideEntryID(rs.getInt("guideentryid"));
        if(rs.getInt("guideentryid") != 0){
            e.setGuideEntry(getCodeElementGuideEntry(rs.getInt("guideentryid")));
        } else {
            e.setGuideEntry(null);
        }

        e.setSource(getCodeSource(rs.getInt("codesource_sourceid")));

        e.setOrdchapterNo(rs.getInt("ordchapterno"));

        e.setOrdchapterTitle(rs.getString("ordchaptertitle"));
        e.setOrdSecNum(rs.getString("ordsecnum"));
        e.setOrdSecTitle(rs.getString("ordsectitle"));

        e.setOrdSubSecNum(rs.getString("ordsubsecnum"));
        e.setOrdSubSecTitle(rs.getString("ordsubsectitle"));
        e.setOrdSubSecNum(rs.getString("ordsubsecnum"));

        e.setOrdSubSubSecNum(rs.getString("ordsubsubsecnum"));

        e.setOrdTechnicalText(rs.getString("ordtechnicaltext"));

        e.setOrdHumanFriendlyText(rs.getString("ordhumanfriendlytext"));
        e.setUseInjectedValues(rs.getBoolean("useinjectedvalues"));

        e.setResourceURL(rs.getString("resourceurl"));


        if(rs.getTimestamp("createdts") != null){
            e.setCreatedTS(rs.getTimestamp("createdts").toLocalDateTime());                
        }
        if(rs.getInt("createdby_userid") != 0){
            e.setCreatedBy(ui.getUser(rs.getInt("createdby_userid")));
        }

        if(rs.getTimestamp("lastupdatedts") != null){
            e.setLastUpdatedTS(rs.getTimestamp("lastupdatedts").toLocalDateTime());
        }
        if(rs.getInt("lastupdatedby_userid") != 0){
            e.setLastupdatedBy(ui.getUser(rs.getInt("lastupdatedby_userid")));
        }

        if(rs.getTimestamp("deactivatedts") != null){
            e.setDeactivatedTS(rs.getTimestamp("deactivatedts").toLocalDateTime());
        }
        if(rs.getInt("deactivatedby_userid") != 0){
            e.setDeactivatedBy(ui.getUser(rs.getInt("deactivatedby_userid")));
        }

                
                
                
        return e;
    }
    
  
    
    /**
     * Key method for returning a fully-assembled link of code elements by source ID.
     * Each code element returned by a single call to this method contains its own 
 instance of a CodeSource object whose field values are identical.
 
 NOTE these are CodeElement that can become EnforcableCodeElement when they are
 added to a codset (which is, in turn, associated with a single muni)
     * 
     * @param sourceID the CodeSource id used in the WHERE clause of the embedded SQL statment
     * @return Fully-baked CodeElement objects in a ArrayList
     * @throws IntegrationException Caught by backing beans and converted into
     * user messages
     */
    public List<CodeElement> getCodeElements(int sourceID) throws IntegrationException{
        String query = "SELECT elementid from codeelement where codesource_sourceID = ? AND deactivatedts IS NULL;";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<CodeElement> elementList = new ArrayList();
         try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            
            stmt.setInt(1, sourceID);
            
            rs = stmt.executeQuery();
            
            while(rs.next()){
                elementList.add(getCodeElement(rs.getInt("elementid")));
            }
             
        } catch (SQLException ex) { 
             System.out.println(ex.toString());
             throw new IntegrationException("Error Retrieving code element list", ex);
        } finally{
            if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
         return elementList;
    } // close getCodeElements
    
    
    
    public int insertCodeElement(CodeElement element) throws IntegrationException{
         String query = "INSERT INTO public.codeelement(\n" +
                        "            elementid, codesource_sourceid, ordchapterno, ordchaptertitle, \n" +
                        "            ordsecnum, ordsectitle, ordsubsecnum, ordsubsectitle, ordtechnicaltext, \n" +
                        "            ordhumanfriendlytext, resourceurl, guideentryid, notes, legacyid, \n" +
                        "            ordsubsubsecnum, useinjectedvalues, lastupdatedts, \n" +
                        "            createdby_userid, lastupdatedby_userid, \n" +
                        "            createdts)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, ?, \n" +
                        "            ?, ?, now(), \n" +
                        "            ?, ?, \n" +
                        "            now());";

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int freshID = 0;

         try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            
            if(element.getSource() != null){
                stmt.setInt(1, element.getSource().getSourceID());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            stmt.setInt(2, element.getOrdchapterNo());
            stmt.setString(3, element.getOrdchapterTitle());
            
            stmt.setString(4, element.getOrdSecNum());
            stmt.setString(5, element.getOrdSecTitle());
            stmt.setString(6, element.getOrdSubSecNum());
            stmt.setString(7, element.getOrdSubSecTitle());
            stmt.setString(8, element.getOrdTechnicalText());
            
            stmt.setString(9, element.getOrdHumanFriendlyText());
            stmt.setString(10, element.getResourceURL());
            if(element.getGuideEntryID() != 0){
                stmt.setInt(11, element.getGuideEntryID());
            } else {
                stmt.setNull(11, java.sql.Types.NULL);
            }
            stmt.setString(12, element.getNotes());
            stmt.setInt(13, element.getLegacyID());
            
            stmt.setString(14, element.getOrdSubSubSecNum());
            stmt.setBoolean(15, element.isUseInjectedValues());
            
            if(element.getCreatedBy() != null){
                stmt.setInt(16, element.getCreatedBy().getUserID());
            } else{
                stmt.setNull(16, java.sql.Types.NULL);
            }
            
            if(element.getLastupdatedBy() != null){
                stmt.setInt(17, element.getLastupdatedBy().getUserID());
            } else{
                stmt.setNull(17, java.sql.Types.NULL);
            }
            
            stmt.execute();
            
            String retrievalQuery = "SELECT currval('codeelement_elementid_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                freshID = rs.getInt(1);
            }
            
             
        } catch (SQLException ex) { 
             System.out.println(ex.toString());
             throw new IntegrationException("Error inserting code element", ex);
        } finally{
            if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
         return freshID;
    }
    
    /**
     * Updates a record in the codeelement table
     * @param element
     * @throws IntegrationException 
     */
    public void updateCodeElement(CodeElement element) throws IntegrationException{
          String query = "UPDATE public.codeelement\n" +
                            "   SET codesource_sourceid=?, ordchapterno=?, ordchaptertitle=?, \n" +
                            "       ordsecnum=?, ordsectitle=?, ordsubsecnum=?, ordsubsectitle=?, \n" +
                            "       ordtechnicaltext=?, ordhumanfriendlytext=?, resourceurl=?, guideentryid=?, \n" +
                            "       notes=?, legacyid=?, ordsubsubsecnum=?, useinjectedvalues=?, \n" +
                            "       lastupdatedts=now(), lastupdatedby_userid=? \n" +
                            " WHERE elementid=?;";

        Connection con = null;
        PreparedStatement stmt = null;

         try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
           
            
            // no source changes on element update
            //stmt.setInt(2, element.getSource().getSourceID());
            
            if(element.getSource() != null){
                stmt.setInt(1, element.getSource().getSourceID());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            stmt.setInt(2, element.getOrdchapterNo());
            stmt.setString(3, element.getOrdchapterTitle());
            
            stmt.setString(4, element.getOrdSecNum());
            stmt.setString(5, element.getOrdSecTitle());
            stmt.setString(6, element.getOrdSubSecNum());
            stmt.setString(7, element.getOrdSubSecTitle());
            
            stmt.setString(8, element.getOrdTechnicalText());
            stmt.setString(9, element.getOrdHumanFriendlyText());
            stmt.setString(10, element.getResourceURL());
            if(element.getGuideEntryID() != 0){
                stmt.setInt(11, element.getGuideEntryID());
            } else {
                stmt.setNull(11, java.sql.Types.NULL);
            }
            stmt.setString(12, element.getNotes());
            stmt.setInt(13, element.getLegacyID());
            stmt.setString(14, element.getOrdSubSubSecNum());
            stmt.setBoolean(15, element.isUseInjectedValues());
                        
            if(element.getLastupdatedBy() != null){
                stmt.setInt(16, element.getLastupdatedBy().getUserID());
            } else{
                stmt.setNull(16, java.sql.Types.NULL);
            }
            
            stmt.setInt(17, element.getElementID());
            
            
            stmt.executeUpdate();
            
             
        } catch (SQLException ex) { 
             System.out.println(ex.toString());
             throw new IntegrationException("Error inserting code element", ex);
        } finally{
            if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Sets the deactivationts in the codeelement table to signal deletion
     * 
     * @param element
     * @throws IntegrationException 
     */
    public void deactivateCodeElement(CodeElement element) throws IntegrationException{
        String query =  "UPDATE codeelement SET deactivatedts = now(), deactivatedby_userid=? WHERE elementid=?;";
        Connection con = null;
        PreparedStatement stmt = null;

         try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            if(element.getDeactivatedBy() != null){
                stmt.setInt(1, element.getDeactivatedBy().getUserID());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            stmt.setInt(2, element.getElementID());
            stmt.execute();
             
        } catch (SQLException ex) { 
             System.out.println(ex.toString());
             throw new IntegrationException("Unable to deactivate code element--"
                     + "probably because it has been used somewhere in the system. It's here to stay.", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
    // *************************************************************
    // ******CODE SETS and CODE SET ELEMENTS  (Code Books)**********
    // *************************************************************
   
     
    /**
     * Updates a record in the codeset table, which declares a schema
     * for organizing enforcable code elements
     * @param set
     * @throws IntegrationException 
     */
    public void updateCodeSetMetadata(CodeSet set) throws IntegrationException{
        if(set == null) throw  new IntegrationException("Cannot update a null code set");
        
        String query = "UPDATE public.codeset\n" +
            "SET name=?, description=?, municipality_municode=? WHERE codeSetid=?;";
        Connection con = null;
        PreparedStatement stmt = null;

         try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setString(1, set.getCodeSetName());
            stmt.setString(2, set.getCodeSetDescription());
            if(set.getMuni() != null){
                stmt.setInt(3, set.getMuni().getMuniCode());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            stmt.setInt(4, set.getCodeSetID());
            stmt.executeUpdate();
             
        } catch (SQLException ex) { 
             System.out.println(ex.toString());
             throw new IntegrationException("Error updating code set", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        
    }
    
    /**
     * Retrieves a code set from the DB
     * @param setID
     * @return
     * @throws IntegrationException 
     */
    public CodeSet getCodeSetBySetID(int setID) throws IntegrationException{
         String query = "SELECT codesetid, name, description, municipality_municode, active  \n" +
                        "FROM public.codeset WHERE codesetid = ?";
        
         //System.out.println("CodeIntegrator.getCodeSets | MuniCode: "+ muniCode);
        
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        CodeSet cs = null;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, setID);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                cs = populateCodeSetFromRS(rs);
            }
            
        } catch (SQLException ex) { 
             System.out.println("CodeIntegrator.getCodeSetBySetID | " + ex.toString());
             throw new IntegrationException("Exception in CodeSetIntegrator", ex);
        } finally{
            if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
            if (con != null) { try { con.close(); } catch (SQLException e) {System.out.println("getCodeSetById | " + e.toString());} }
        } // close finally
        return cs;
        
    }
    
    /**
     * Builds a mapping of municipalities to code sets
     * @return
     * @throws IntegrationException 
     */
    public HashMap<Municipality, CodeSet> getMuniDefaultCodeSetMap() throws IntegrationException{
        HashMap<Municipality, CodeSet> muniSetMap = new HashMap<>();
        MunicipalityCoordinator mc = getMuniCoordinator();
        
        String query = "SELECT municode, defaultcodeset FROM public.municipality;";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                muniSetMap.put(mc.getMuni(rs.getInt("municode")), getCodeSetBySetID(rs.getInt("defaultcodeset")));
            }
            
        } catch (SQLException ex) { 
             System.out.println("CodeIntegrator.getCodeSetBySetID | " + ex.toString());
             throw new IntegrationException("Exception in CodeSetIntegrator", ex);
        } finally{
            if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return muniSetMap;
    }
    
   
    /**
     * Genenerator of CodeSet objects from a codeset record
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
    private CodeSet populateCodeSetFromRS(ResultSet rs) throws SQLException, IntegrationException{
        CodeSet set = new CodeSet();
        MunicipalityIntegrator muniInt = getMunicipalityIntegrator();
        
        set.setCodeSetID(rs.getInt("codesetid"));
        set.setCodeSetName(rs.getString("name"));
        set.setCodeSetDescription(rs.getString("description"));
        // the key call: grab a list of all enforcable code elements in this set (large)
        set.setEnfCodeElementList(getEnforcableCodeElementList(rs.getInt("codesetid")));
        
        set.setMuni(muniInt.getMuni(rs.getInt("municipality_municode")));
        set.setActive(rs.getBoolean("active"));

        return set;
        
    }
    
    
    
    /**
     * Extracts all code sets from the codeset table
     * @return
     * @throws IntegrationException 
     */
    public ArrayList getCodeSets() throws IntegrationException {
        String query = "SELECT codesetid \n"
                + "  FROM public.codeset WHERE active=TRUE;";

        //System.out.println("CodeIntegrator.getCodeSets | MuniCode: "+ muniCode);
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<CodeSet> codeSetList = new ArrayList();

        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                codeSetList.add(getCodeSetBySetID(rs.getInt("codesetid")));
            }

        } catch (SQLException ex) {
            System.out.println("CodeIntegrator.getCodeSetByMuniCode | " + ex.toString());
            throw new IntegrationException("Exception in CodeSetIntegrator", ex);
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
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
        } // close finally
        return codeSetList;
    }
    
    /**
     * Gets all codesets (codebooks) for a given municipality
     * @param muniCode
     * @return
     * @throws IntegrationException 
     */
    public ArrayList getCodeSets(int muniCode) throws IntegrationException{
         String query = "SELECT codesetid \n" +
                        "FROM public.codeset WHERE municipality_municode = ?";
        
         //System.out.println("CodeIntegrator.getCodeSets | MuniCode: "+ muniCode);
        
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<CodeSet> codeSetList = new ArrayList();
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, muniCode);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                codeSetList.add(getCodeSetBySetID(rs.getInt("codesetid")));
            }
            
        } catch (SQLException ex) { 
             System.out.println("CodeIntegrator.getCodeSetByMuniCode | " + ex.toString());
             throw new IntegrationException("Exception in CodeSetIntegrator", ex);
        } finally{
            if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return codeSetList;
    }
    
    
     /**
     * Creates and populates single EnforcableCodeElement object based on giving ID number. 
     * 
     * @param codeSetElementID
     * @return the fully-baked EnforcableCodeElement
     * @throws IntegrationException 
     */
    public EnforcableCodeElement getEnforcableCodeElement(int codeSetElementID) throws IntegrationException{
        EnforcableCodeElement newEce = null;
        PreparedStatement stmt = null;
        Connection con = null;
        String query = "SELECT codesetelementid, codeset_codesetid, codelement_elementid, elementmaxpenalty, \n" +
                " elementminpenalty, elementnormpenalty, penaltynotes, normdaystocomply, \n" +
                " daystocomplynotes, munispecificnotes, defaultviolationdescription, createdts, createdby_userid, \n" +
                " lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid \n" +
                " FROM public.codesetelement WHERE codesetelementid=?;";
        ResultSet rs = null;
 
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, codeSetElementID);
            rs = stmt.executeQuery();
            while(rs.next()){
                newEce = generateEnforcableCodeElement(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println("MunicipalityIntegrator.getMuniFromMuniCode | " + ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.getMuniFromMuniCode", ex);
        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        return newEce;
    }
    
  /**
   * Returns a ArrayList of fully-baked EnforcableCodeElement objects based on a search with
   * setID. Handy for then populating data tables of codes, such as in creating
   * a codeviolation. This involves a rather complicated object composition process
   * that draws on several other methods in this class for retrieving from the database
   * 
   * @param setID
   * @return all CodeElement objects kicked out by postgres with that setID
   * @throws com.tcvcog.tcvce.domain.IntegrationException
   */
    public ArrayList getEnforcableCodeElementList(int setID) throws IntegrationException{
        PreparedStatement stmt = null;
        Connection con = null;
        String query = "SELECT codesetelementid " +
                " FROM public.codesetelement WHERE codeset_codesetid=? AND deactivatedts IS NULL;";
        ResultSet rs = null;
        ArrayList<EnforcableCodeElement> eceList = new ArrayList();
 
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, setID);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                
                eceList.add(getEnforcableCodeElement(rs.getInt("codesetelementid")));
                
            }
        } catch (SQLException ex) {
            System.out.println("MunicipalityIntegrator.getMuniFromMuniCode | " + ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.getMuniFromMuniCode", ex);
        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { System.out.println("getEnforcableCodeElementList | " + e.toString());} }
        } // close finally
        
        return eceList;
    }
    
    /**
     * Generator of EnforcableCodeElement objects given a RS
     * @param rs with all fields SELECTed
     * @return the populated object
     * @throws SQLException
     * @throws IntegrationException 
     */
    private EnforcableCodeElement generateEnforcableCodeElement(ResultSet rs) throws SQLException, IntegrationException{
        
        PaymentIntegrator pi = getPaymentIntegrator();
        UserIntegrator ui = getUserIntegrator();
        CodeElement ele = getCodeElement(rs.getInt("codelement_elementid"));
        
        EnforcableCodeElement newEce = new EnforcableCodeElement(ele);
        
        newEce.setCodeSetID(rs.getInt("codeset_codesetid"));
        newEce.setCodeSetElementID(rs.getInt("codesetelementid"));
        
        newEce.setMaxPenalty(rs.getInt("elementmaxpenalty"));
        newEce.setMinPenalty(rs.getInt("elementminpenalty"));
        newEce.setNormPenalty(rs.getInt("elementnormpenalty"));
        newEce.setPenaltyNotes(rs.getString("penaltynotes"));
        newEce.setNormDaysToComply(rs.getInt("normdaystocomply"));
        newEce.setDaysToComplyNotes(rs.getString("daystocomplynotes"));
        newEce.setMuniSpecificNotes(rs.getString("munispecificnotes"));
        newEce.setFeeList(pi.getFeeList(newEce));
        newEce.setDefaultViolationDescription(rs.getString("defaultviolationdescription"));
        
        if(rs.getTimestamp("createdts") != null){
            newEce.setEceCreatedTS(rs.getTimestamp("createdts").toLocalDateTime());                
        }
        if(rs.getInt("createdby_userid") != 0){
            newEce.setEceCreatedBy(ui.getUser(rs.getInt("createdby_userid")));
        }

        if(rs.getTimestamp("lastupdatedts") != null){
            newEce.setEceLastUpdatedTS(rs.getTimestamp("lastupdatedts").toLocalDateTime());
        }
        if(rs.getInt("lastupdatedby_userid") != 0){
            newEce.setEceLastupdatedBy(ui.getUser(rs.getInt("lastupdatedby_userid")));
        }

        if(rs.getTimestamp("deactivatedts") != null){
            newEce.setEceDeactivatedTS(rs.getTimestamp("deactivatedts").toLocalDateTime());
        }
        if(rs.getInt("deactivatedby_userid") != 0){
            newEce.setEceDeactivatedBy(ui.getUser(rs.getInt("deactivatedby_userid")));
        }

        return newEce;
    }
    
    /**
     * Creates what we call an EnforcableCodeElement, which means
 we find an existing code element and add muni-specific 
 enforcement data to that element and store it in the DB
 
 This operation adds an entry to table codesetelement
 and uses the ID of the codeSet and CodeElement to make
 the many-to-many links in the database.
     * 
     * @param ece
     * @return  
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public int insertEnforcableCodeElementToCodeSet(EnforcableCodeElement ece) throws IntegrationException{
        PreparedStatement stmt = null;
        Connection con = null;
        String query = "INSERT INTO public.codesetelement(\n" +
                    "codesetelementid, codeset_codesetid, codelement_elementid, elementmaxpenalty, \n" +
                    "elementminpenalty, elementnormpenalty, penaltynotes, normdaystocomply, \n" +
                    "daystocomplynotes, munispecificnotes, defaultviolationdescription,"
                    + "createdts, createdby_userid, lastupdatedts, lastupdatedby_userid)\n" +
                    " VALUES (DEFAULT, ?, ?, ?, \n" +
                    "?, ?, ?, ?, \n" +
                    "?, ?, ?,"
                    + "now(), ?, now(), ?);";
        ResultSet rs = null;
        int freshECEID = 0;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ece.getCodeSetID());
            stmt.setInt(2, ece.getElementID() );
            stmt.setDouble(3, ece.getMaxPenalty());
            
            stmt.setDouble(4, ece.getMinPenalty());
            stmt.setDouble(5, ece.getNormPenalty());
            stmt.setString(6, ece.getPenaltyNotes());
            stmt.setInt(7, ece.getNormDaysToComply());
            
            stmt.setString(8, ece.getDaysToComplyNotes());
            stmt.setString(9, ece.getMuniSpecificNotes());
            stmt.setString(10, ece.getDefaultViolationDescription());
            
            if(ece.getEceCreatedBy() != null){
                stmt.setInt(11, ece.getEceCreatedBy().getUserID());
            } else {
                stmt.setNull(11, java.sql.Types.NULL);
            }

            if(ece.getEceLastupdatedBy() != null){
                stmt.setInt(12, ece.getEceLastupdatedBy().getUserID());
            } else {
                stmt.setNull(12, java.sql.Types.NULL);
            }
            
            
            stmt.execute();
            
            String retrievalQuery = "SELECT currval('codesetelement_elementid_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                freshECEID = rs.getInt(1);
            }
            
            
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Exception in CodeIntegrator.addEnforcableCodeElementToCodeSet", ex);
        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return freshECEID;
    }
    
    /**
     * Updates a record in the codesetelement table
     * 
     * @param ece
     * @throws IntegrationException 
     */
    public void updateEnforcableCodeElement(EnforcableCodeElement ece) throws IntegrationException{
        PreparedStatement stmt = null;
        Connection con = null;
        String query =  "UPDATE public.codesetelement\n" +
                        "   SET elementmaxpenalty=?, elementminpenalty=?, elementnormpenalty=?, \n" +
                        "       penaltynotes=?, normdaystocomply=?, daystocomplynotes=?, munispecificnotes=?, defaultviolationdescription=?, \n" +
                        "       lastupdatedts=now(), lastupdatedby_userid=? \n" +
                        " WHERE codesetelementid=?;";
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setDouble(1, ece.getMaxPenalty());
            stmt.setDouble(2, ece.getMinPenalty());
            stmt.setDouble(3, ece.getNormPenalty());
            
            stmt.setString(4, ece.getPenaltyNotes());
            stmt.setInt(5, ece.getNormDaysToComply());
            stmt.setString(6, ece.getDaysToComplyNotes());
            stmt.setString(7, ece.getMuniSpecificNotes());
            stmt.setString(8, ece.getDefaultViolationDescription());
            
            if(ece.getEceLastUpdatedTS() != null){
                stmt.setInt(9, ece.getEceLastupdatedBy().getUserID());
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            
            stmt.setInt(10, ece.getCodeSetElementID());
            System.out.println("CodeIntegrator.updateEnforcableCodeElement | ece update: " + stmt.toString());
            
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update enforcable code element data", ex);
        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    /**
     * Deactivates a codesetelement by setting its deactivatedts field
     * @param ece
     * @throws IntegrationException 
     */
    public void deactivateEnforcableCodeElement(EnforcableCodeElement ece ) throws IntegrationException{
        if(ece == null){
            throw new IntegrationException("Cannot nuke a null ECE!");
        } 
        PreparedStatement stmt = null;
        Connection con = null;
        String query =  "UPDATE public.codesetelement SET deactivatedts = now(), deactivatedby_userid=? \n" +
                        " WHERE codesetelementid = ?;";
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            
            if(ece.getEceDeactivatedBy() != null){
                stmt.setInt(1, ece.getEceDeactivatedBy().getUserID());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            
            stmt.setInt(2, ece.getCodeSetElementID());
            
            System.out.println("CodeIntegratator.deacECE: eceid: " + ece.getCodeSetElementID() );
            System.out.println("CodeIntegratator.deacECE: stmt: " + stmt.getParameterMetaData());
            stmt.executeUpdate();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to delete enforcable code element: "
                    + "it is probably used somewhere in the system and is here to stay!", ex);
        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Adds a code set listing which is then a container for enforcable code set elements
     * @param codeSetToInsert
     * @return the code set list (each code set does not have the list of enfcodeelements)
     * @throws IntegrationException 
     */
    public int insertCodeSetMetadata(CodeSet codeSetToInsert) throws IntegrationException{
        if(codeSetToInsert ==  null){
            throw new IntegrationException("cannot insert a null set");
            
        }
        PreparedStatement stmt = null;
        Connection con = null;
        // note that muniCode is not returned in this query since it is specified in the WHERE
        String query = "INSERT INTO public.codeset(\n" +
                "codesetid, name, description, municipality_municode, active)\n" +
                "VALUES (DEFAULT, ?, ?, ?, TRUE);";
        
        ResultSet rs = null;
        int freshID = 0;
 
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setString(1, codeSetToInsert.getCodeSetName());
            stmt.setString(2, codeSetToInsert.getCodeSetDescription());
            if(codeSetToInsert.getMuni() != null){
                stmt.setInt(3, codeSetToInsert.getMuni().getMuniCode());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            stmt.execute();
            
            
            String retrievalQuery = "SELECT currval('codeset_codesetid_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                freshID = rs.getInt(1);
            }
            
        } catch (SQLException ex) {
            System.out.println("MunicipalityIntegrator.getMuniFromMuniCode | " + ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.getMuniFromMuniCode", ex);
        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return freshID;
    }
    
    /**
     * Deactivates a code set record
     * @param set
     * @throws BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
   public void deactivateCodeSet(CodeSet set) throws BObStatusException, IntegrationException{
        if(set == null) throw new BObStatusException("Cannot deactivate null code set");
        
        PreparedStatement stmt = null;
        Connection con = null;
        // note that muniCode is not returned in this query since it is specified in the WHERE
        String query = "UPDATE codeset SET active=FALSE WHERE codesetid=?";
        
 
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, set.getCodeSetID());
            stmt.executeUpdate();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Exception in MunicipalityIntegrator.getMuniFromMuniCode", ex);
        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
       
       
   }
  
    
    // *************************************************************
    // ********************* CODE GUIDE ****************************
    // *************************************************************
   
    
    
    public void insertCodeElementGuideEntry(CodeElementGuideEntry cege) throws IntegrationException{
        String query =  "INSERT INTO public.codeelementguide(\n" +
                        "            guideentryid, category, subcategory, description, enforcementguidelines, \n" +
                        "            inspectionguidelines, priority)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
                        "            ?, ?);";
        Connection con = null;
        PreparedStatement stmt = null;

         try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setString(1, cege.getCategory());
            stmt.setString(2, cege.getSubCategory());
            stmt.setString(3, cege.getDescription());
            stmt.setString(4, cege.getEnforcementGuidelines());
            stmt.setString(5, cege.getInspectionGuidelines());
            stmt.setBoolean(6, cege.isPriority());
            
            System.out.println("CodeElementGuideBB.insertCodeElementGuideEntry | stmt: " + stmt.toString());
            
            stmt.execute();
             
        } catch (SQLException ex) { 
             System.out.println(ex.toString());
             throw new IntegrationException("Error inserting code element type", ex);
        } finally{
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            
        } // close finally
    }
    
    public void updateCodeElementGuideEntry(CodeElementGuideEntry cege) throws IntegrationException{
        String query =  "UPDATE public.codeelementguide\n" +
                        "   SET category=?, subcategory=?, description=?, enforcementguidelines=?, \n" +
                        "       inspectionguidelines=?, priority=?\n" +
                        " WHERE guideentryid=?;";
        Connection con = null;
        PreparedStatement stmt = null;

         try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setString(1, cege.getCategory());
            stmt.setString(2, cege.getSubCategory());
            stmt.setString(3, cege.getDescription());
            stmt.setString(4, cege.getEnforcementGuidelines());
            stmt.setString(5, cege.getInspectionGuidelines());
            stmt.setBoolean(6, cege.isPriority());
            stmt.setInt(7, cege.getGuideEntryID());
             System.out.println("CodeIntegrator.updateCodeElementGuideEntry | stmt: " + stmt.toString());
            stmt.execute();
             
        } catch (SQLException ex) { 
             System.out.println(ex.toString());
             throw new IntegrationException("Error updating code element guide entry", ex);
        } finally{
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
   
    
    public void deleteCodeElementGuideEntry(CodeElementGuideEntry ge) throws IntegrationException{
        String query =  "DELETE FROM public.codeelementguide\n" +
                        " WHERE guidenetryid=?;";
        Connection con = null;
        PreparedStatement stmt = null;

         try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ge.getGuideEntryID());
            stmt.execute();
             
        } catch (SQLException ex) { 
             System.out.println(ex.toString());
             throw new IntegrationException("Error deleting code element guide entry, "
                     + "probably because it has been connected to some other business object. No delete for you!", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    
    public CodeElementGuideEntry getCodeElementGuideEntry(int entryid) throws IntegrationException{
        String query =  "SELECT guideentryid, category, subcategory, description, enforcementguidelines, \n" +
                        " inspectionguidelines, priority\n" +
                        " FROM public.codeelementguide WHERE guideentryid = ?;";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        CodeElementGuideEntry cege = null;
         try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, entryid);
            rs = stmt.executeQuery();
            while(rs.next()){
                cege = generateCodeElementGuideEntry(rs);
            }
             
        } catch (SQLException ex) { 
             System.out.println(ex.toString());
             throw new IntegrationException("Error retrieving code element guide entry by ID", ex);
        } finally{
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
         
         return cege;
    }
    
    /**
     * Retrieves the entire code guide!
     * @return the full code guide for browsing
     * @throws IntegrationException 
     */
    public ArrayList<CodeElementGuideEntry> getCodeElementGuideEntries() throws IntegrationException{
        String query =  "SELECT guideentryid, category, subcategory, description, enforcementguidelines, \n" +
                        "       inspectionguidelines, priority\n" +
                        "  FROM public.codeelementguide;";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<CodeElementGuideEntry> cegelist = new ArrayList();

         try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while(rs.next()){
                cegelist.add(generateCodeElementGuideEntry(rs));
                System.out.println("CodeIntegrator.getCodeElementGuideEntries | retrieved Entry");
                
            }
             
        } catch (SQLException ex) { 
             System.out.println(ex.toString());
             throw new IntegrationException("Error generating code element guide entry list", ex);
        } finally{
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return cegelist;
    }
    
    private CodeElementGuideEntry generateCodeElementGuideEntry(ResultSet rs) throws SQLException{
        CodeElementGuideEntry cege = new CodeElementGuideEntry();
        cege.setGuideEntryID(rs.getInt("guideentryid"));
        cege.setCategory(rs.getString("category"));
        cege.setSubCategory(rs.getString("subcategory"));
        cege.setDescription(rs.getString("description"));
        cege.setEnforcementGuidelines(rs.getString("enforcementguidelines"));
        cege.setInspectionGuidelines(rs.getString("inspectionguidelines"));
        cege.setPriority(rs.getBoolean("priority"));
        return cege;
    }
    
    public void linkElementToCodeGuideEntry(CodeElement element, int codeGuideEntryID) throws IntegrationException{
        String query =  "update codeelement set guideentryid = ? where elementid = ?;";
        Connection con = null;
        PreparedStatement stmt = null;

         try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, codeGuideEntryID);
            stmt.setInt(2, element.getElementID());
            stmt.execute();
             
        } catch (SQLException ex) { 
             System.out.println(ex.toString());
             throw new IntegrationException("Unable to link element id " + element.getElementID() 
                     + " to guide entry with ID of " + codeGuideEntryID 
                     + ". Make sure your guide entry ID exists in the CodeGuide.", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
 
    
} // close class
