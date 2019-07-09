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
package com.tcvcog.tcvce.occupancy.integration;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.occupancy.entities.OccChecklistBlueprint;
import com.tcvcog.tcvce.occupancy.entities.OccInspectedElement;
import com.tcvcog.tcvce.occupancy.entities.OccInspectedSpace;
import com.tcvcog.tcvce.occupancy.entities.OccLocationDescriptor;
import com.tcvcog.tcvce.occupancy.entities.OccInspection;
import com.tcvcog.tcvce.occupancy.entities.OccSpace;
import com.tcvcog.tcvce.occupancy.entities.OccSpaceType;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 *
 * @author Eric C. Darsow
 */
public class ChecklistIntegrator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of ChecklistIntegrator
     */
    public ChecklistIntegrator() {
    }
    
    public void createChecklistBlueprintMetadata(OccChecklistBlueprint bpMetaData) throws IntegrationException{
        
        String query = "INSERT INTO public.checklist(\n" +
                        " checklistid, title, description, muni_municode, active)\n" +
                        " VALUES (DEFAULT, ?, ?, ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, bpMetaData.getTitle());
            stmt.setString(2, bpMetaData.getDescription());
            stmt.setInt(3, bpMetaData.getMuni().getMuniCode());
            stmt.setBoolean(4, bpMetaData.isActive());
            System.out.println("ChecklistIntegrator.createChecklistBlueprintListing "
                    + "| stmt: " + stmt.toString());
            stmt.executeQuery();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Sylvia, the CogBot, is unable to insert Checklist Blueprint listing, sorry!", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public void updateChecklistBlueprintMetadata(OccChecklistBlueprint blueprint) throws IntegrationException{
        
        String query = "UPDATE public.checklist\n" +
                        " SET title=?, description=?, muni_municode=?, active=?\n" +
                        " WHERE checklistid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, blueprint.getTitle());
            stmt.setString(2, blueprint.getDescription());
            stmt.setInt(3, blueprint.getMuni().getMuniCode());
            stmt.setBoolean(4, blueprint.isActive());
            stmt.setInt(5, blueprint.getInspectionChecklistID());
            System.out.println("ChecklistIntegrator.updateChecklistBlueprintListing "
                    + "| stmt: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Sylvia, the CogBot, is unable to update checklist blueprint metadata, sorry!", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    // maybe don't need this one
    public void insertChecklistBlueprint(OccChecklistBlueprint blueprint) throws IntegrationException{
        
        String query = "";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while(rs.next()){

            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
    }
    
    public void insertSpaceMetaData(OccSpace s) throws IntegrationException{
        
        String query = "INSERT INTO public.space(\n" +
                        " spaceid, name, spacetype)\n" +
                        " VALUES (DEFAULT, ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, s.getName());
            stmt.setInt(2, s.getSpaceType().getSpaceTypeID());
            System.out.println("ChecklistIntegrator.insertSpace | stmt: " + stmt.toString());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public void updateSpaceMetaData(OccSpace s) throws IntegrationException{
        
        String query =  "UPDATE public.space\n" +
                        " SET name=?, spacetype=?\n" +
                        " WHERE spaceid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setString(1, s.getName());
            stmt.setInt(2, s.getSpaceType().getSpaceTypeID());
            System.out.println("ChecklistIntegrator.updateSpace | stmt: " + stmt.toString());
            stmt.executeQuery();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public OccSpace getSpaceWithElements(int spaceID) throws IntegrationException{
        
        String query =  "SELECT name, spacetype_id\n" +
                        "  FROM public.space WHERE spaceid = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        OccSpace s = new OccSpace();
        
        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, spaceID);
            rs = stmt.executeQuery();

            while(rs.next()){
                s.setSpaceid(spaceID);
                s.setName(rs.getString("name"));
                s.setSpaceType(getSpaceType(rs.getInt("spacetype")));
            }
            
            s = populateSpaceWithCodeElements(s);

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return s;
    }
    
     public OccInspectedSpace populateInspectedSpaceMetadata(OccInspectedSpace is) throws IntegrationException{
        
        String query =  "SELECT name, spacetype_id\n" +
                        "  FROM public.space WHERE spaceid = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, is.getSpaceid());
            rs = stmt.executeQuery();

            while(rs.next()){
                is.setName(rs.getString("name"));
                is.setSpaceType(getSpaceType(rs.getInt("spacetype")));
            }
            

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return is;
    }
    
    /**
     * Takes in a OccSpace object, extracts its ID, then uses that to query for
 all of the codeelements attached to that space. Used to compose a checklist
     * blueprint which can be converted into an implemented checklist
     * @param s the space Object to load up with elements. When passed in, only the ID
     * needs to be held in the object
     * @return a OccSpace object populated with CodeElement objects
     * @throws IntegrationException 
     */
    private OccSpace populateSpaceWithCodeElements(OccSpace s) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        String query =  "SELECT spaceelementid, space_id, codeelement_id\n" +
                        " FROM public.spaceelement WHERE spaceid = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<CodeElement> eleList = new ArrayList();
        
        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, s.getSpaceid());
            rs = stmt.executeQuery();
            while(rs.next()){
                eleList.add(ci.getCodeElement(rs.getInt("codeelement_id")));
            }
            
            s.setElementList(eleList);

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return s;
    }
    
    /**
     * Connects a OccSpace with Code Elements. 
     * @param s the space to which the elements should be connected
     * @param elementsToAttach a list of CodeElements that should be inspected in this space
     * @throws IntegrationException 
     */
    public void attachCodeElementsToSpace(OccSpace s, ArrayList<CodeElement> elementsToAttach) throws IntegrationException{
        
        String query =  "INSERT INTO public.spaceelement(\n" +
                        " spaceelementid, space_id, codeelement_id)\n" +
                        " VALUES (DEFAULT, ?, ?);";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ListIterator li = elementsToAttach.listIterator();
        CodeElement ce;
        try {
            // for each CodeElement in the list passed into the method, make an entry in the spaceelement table
            while(li.hasNext()){
                ce = (CodeElement) li.next();
                stmt = con.prepareStatement(query);
                stmt.setInt(1, s.getSpaceid());
                stmt.setInt(2, ce.getElementID());
                System.out.println("ChecklistIntegrator.attachCodeElementsToSpace | stmt: " + stmt.toString());
                stmt.execute();
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
    }
    
    /**
     * Removes the connection between a OccSpace and a Code Element. Handy for adjusting ChecklistBlueprints
     * @param s
     * @param elementToDetach
     * @throws IntegrationException 
     */
    public void detachCodeElementFromSpace(OccSpace s, CodeElement elementToDetach) throws IntegrationException{
        
        String query = "DELETE FROM public.spaceelement\n" +
                        " WHERE space_id = ? AND spaceelementid = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, s.getSpaceid());
            stmt.setInt(2, elementToDetach.getElementID());
            System.out.println("ChecklistIntegrator.dettachCodeElementsFromSpace | stmt: " + stmt.toString());
            stmt.executeQuery();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
    /**
     * Generates a complete list of spaces. We'll probably want to segment these
     * my municipality as the system develops so that the list length stays
     * manageable.
     * 
     * @return a fully-baked space list, meaning each space has its elements intact
     * @throws IntegrationException 
     */
    public ArrayList<OccSpace> getSpaceList() throws IntegrationException{
        
        String query = "SELECT spaceid FROM public.space;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<OccSpace> spaceAL = new ArrayList();

        try {

            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();

            while(rs.next()){
                spaceAL.add(getSpaceWithElements(rs.getInt("spaceid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return spaceAL;
        
    }
    
    /**
     * Deletes both the linked spaceelements and the space entry itself
     * @param s
     * @throws IntegrationException 
     */
    public void deleteSpace(OccSpace s) throws IntegrationException{
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            // start by removing space elements
            String query =  "DELETE FROM public.spaceelement\n" +
                        " WHERE spaceelementid = ?;";

            stmt = con.prepareStatement(query);
            stmt.setInt(1, s.getSpaceid());
            stmt.executeQuery();
            
            // now remove the space itself
            query = "DELETE FROM public.space WHERE spaceid = ?";
            stmt = con.prepareStatement(query);
            stmt.setInt(1, s.getSpaceid());
            stmt.executeQuery();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to delete the space and its associated"
                    + "elements: Probably because this space has been used in one or more"
                    + "occupancy inspectsion. It's here to stay!", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }    
    
    
    public OccChecklistBlueprint getChecklistBlueprint(int blueprintID) throws IntegrationException{
        
        String blueprintMetadataQuery = "SELECT checklistid, title, description, muni_municode, active\n" +
                                        "  FROM public.checklist WHERE checklistid = ?;";
        // retrieves a list of space ids which we can then feed into the space generator to get spaces 
        // with their elements to list in checklistblueprint
        String spaceIDQuery = "SELECT DISTINCT space_id "
                + "FROM checklistspaceelement INNER JOIN spaceelement ON (spaceelement_id = spaceelementid) "
                + "WHERE checklist_id = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        OccChecklistBlueprint bp = null;
        ArrayList<OccSpace> spaceList = new ArrayList<>();

        try {
            
            stmt = con.prepareStatement(blueprintMetadataQuery);
            stmt.setInt(1, blueprintID);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                bp = generateChecklistBlueprint(rs);
            }
            
            // now that we have a ChecklistBllueprint with its metadata, we can 
            // build its space list with code elements embedded inside!
            stmt = con.prepareStatement(spaceIDQuery);
            stmt.setInt(1, blueprintID);
            rs = stmt.executeQuery();
            
            // loop once for each distinct space ID in the checklsitspaceelement table
            while(rs.next()){
                spaceList.add(getSpaceWithElements(rs.getInt("space_id")));
            }
            
            if(bp != null){
                bp.setSpaceList(spaceList);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build a checklist blueprint from database", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return bp ;
    }
    
    /**
     * Utility method for populating a OccChecklistBlueprint with metadata
     * @param rs containing all metadata fields from the checklist table
     * @return half-baked OccChecklistBlueprint object (no element lists)
     */
    private OccChecklistBlueprint generateChecklistBlueprint(ResultSet rs) throws SQLException, IntegrationException{
        OccChecklistBlueprint bp = new OccChecklistBlueprint();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        
        bp.setInspectionChecklistID(rs.getInt("checklistid"));
        bp.setTitle(rs.getString("title"));
        bp.setDescription(rs.getString("description"));
        bp.setMuni(mi.getMuni(rs.getInt("muni_municode")));
        bp.setActive(rs.getBoolean("active"));
        
        return bp;
    }
    
    /**
     * Retrieves the system-wide list of ChecklistBlueprints. May need to be
     * queried by Muni with time
     * @param muni
     * @return
     * @throws IntegrationException 
     */
    public ArrayList<OccChecklistBlueprint> getCompleteChecklistBlueprintList(Municipality muni) throws IntegrationException{
        
        String query = "SELECT checklistid from checklist;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<OccChecklistBlueprint> bpList = new ArrayList<>();

        try {

            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();

            while(rs.next()){
                bpList.add(getChecklistBlueprint(rs.getInt("checklistid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to retrieve list of blueprints from DB", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return bpList;
    }
    
    /**
     * NOT DONE!!!!
     * TODO: Wire up the system for updated
     * @param blueprint
     * @throws IntegrationException 
     */
    public void updateChecklistBlueprintSpaceList(OccChecklistBlueprint blueprint) throws IntegrationException{
        
        String query = "";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();

            while(rs.next()){

            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
    }
    
    
    /**
     * Called by the OccupancyIntegrator during the construction of an OccInspection
 object. This method, in turn, calls the private getInspectedSpaceList method in this
     * class to populate the actual inspection data.
     * @param inspectionID
     * @return a fully-baked ImplementedChecklist
     * @throws IntegrationException 
     */
    public ArrayList<OccInspectedSpace> getInspectedSpaceList(int inspectionID) throws IntegrationException{
        ArrayList<OccInspectedSpace> inspSpaceList = new ArrayList<>();
        
        String query_spaceIDs =  "SELECT DISTINCT spaceid\n" +
            " FROM inspectedchecklistspaceelement INNER JOIN checklistspaceelement ON (checklistspaceelementid = checklistspaceelement_id)\n" +
            "	INNER JOIN spaceelement ON (spaceelement_id = spaceelementid)\n" +
            "	INNER JOIN space ON (space_id = spaceid)\n" +
            " WHERE occupancyinspection_id=?;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            // this gets us a list of all of the spaces that have been inspected for this
            // occupancy inspection
            stmt = con.prepareStatement(query_spaceIDs);
            stmt.setInt(1, inspectionID);
            rs = stmt.executeQuery();

            // chugg down the list of spaceids and fetch an Inspected space for each
            while(rs.next()){
                inspSpaceList.add(getInspectedSpace(inspectionID, rs.getInt("spaceid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate inspected space list, sorry!", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        // temp to close
        return inspSpaceList;
    }
    
    private OccInspectedSpace getInspectedSpace(int inspectionID, int spaceID) throws IntegrationException, SQLException{
        
        // this gets us a list of inspectedchecklistspaceelements
        String query_inspectedElementsBySpace = 
            "SELECT inspectedchecklistspaceelementid, spaceelementid, spaceid, codeelement_id, checklist_id, compliancedate, inspected, notes, locationdescription_id\n" +
            " FROM inspectedchecklistspaceelement INNER JOIN checklistspaceelement ON (checklistspaceelementid = checklistspaceelement_id)\n" +
            "	INNER JOIN spaceelement ON (spaceelement_id = spaceelementid)\n" +
            "	INNER JOIN space ON (space_id = spaceid)\n" +
            " WHERE occupancyinspection_id=? AND spaceid = ?;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        OccInspectedSpace is = new OccInspectedSpace();
        ArrayList<OccInspectedElement> insElementList = new ArrayList<>();

        try {
            
            // this is a hacky way of grabbing basic space data: this process should
            // use Polymorphism but that will have to wait until later versions
            is = populateInspectedSpaceMetadata(is);
            
            stmt = con.prepareStatement(query_inspectedElementsBySpace);
            stmt.setInt(1, inspectionID);
            stmt.setInt(2, spaceID);
            rs = stmt.executeQuery();

            // chugg down the list of spaceids and fetch an Inspected space for each
            while(rs.next()){
                insElementList.add(generateInspectedElement(rs));
                
                // a hacky solution to space locations: overwrite the space's location for each
                // inspected element--since they're all the same for each element
                is.setLocation(getLocation(rs.getInt("locationdescription_id")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate an inspected space, sorry!", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
    
        is.setInspectedElementList(insElementList);
        return is;
    }
    
    private OccInspectedElement generateInspectedElement(ResultSet rs) throws SQLException, IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        OccInspectedElement ie = new OccInspectedElement();
        
        ie.setId(rs.getInt("inspectedchecklistspaceelementid"));
        ie.setInspected(rs.getBoolean("inspected"));
        ie.setElement(ci.getCodeElement(rs.getInt("codeelement_id")));
        ie.setComplianceDate(rs.getTimestamp("compliancedate").toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        ie.setNotes(rs.getString("notes"));
        // note this is the same location object as on the InspectedSPace to allow
        // the severing of the OccInspectedElement from the space without losing
        // location specific information
        // an UN-elegant solution
//        ie.set(getLocation(rs.getInt("locationdescription_id")));
        
        return ie;
    }
    
    public OccLocationDescriptor getLocation(int locationID) throws IntegrationException{
         String query_spaceIDs =  "SELECT locationdescriptionid, description\n" +
                                  "  FROM public.locationdescription WHERE locationdescriptionid = ?;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        OccLocationDescriptor loc = null;
        try {

            // this gets us a list of all of the spaces that have been inspected for this
            // occupancy inspection
            stmt = con.prepareStatement(query_spaceIDs);
            stmt.setInt(1, locationID);
            rs = stmt.executeQuery();

            // chugg down the list of spaceids and fetch an Inspected space for each
            while(rs.next()){
                loc = new OccLocationDescriptor();
                loc.setLocationID(rs.getInt("locationdescriptionid"));
                loc.setLocationDescription(rs.getString("description"));
                
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate inspected space list, sorry!", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return loc;
    }
    
    
    /**
     * Inserts an OccInspectedSpace to the inspectedchecklistspaceelement table which has not 
 previously been inspected. The OccInspectedSpace object and its composed elements
 do not have an ID number generated from the DB yet, and these will come from the DB
 sequences ;
 
 Remember: The db doesn't have a concept of an "inspected space", only 
 an inspectedSpaceElement so this method will iterate over the inspected
 elements in the OccInspectedSpace, executing an insert statement for each.
     * 
     * @param oi the current OccInspection
     * @param is an OccInspectedSpace that was NOT retrieved from the DB
     * @throws IntegrationException 
     */
    public void insertNewlyInspectedSpace(OccInspection oi, OccInspectedSpace is) throws IntegrationException{
        
        String query_locationInsert =  "INSERT INTO public.locationdescription(\n" +
                                        "            locationdescriptionid, description)\n" +
                                        "    VALUES (DEFAULT, ?);";
        
        String query_locationID = "SELECT currval(‘locationdescription_id_seq')";
        
        String query_icse =     "INSERT INTO public.inspectedchecklistspaceelement(\n" +
                                "            inspectedchecklistspaceelementid, occupancyinspection_id, checklistspaceelement_id, \n" +
                                "            compliancedate, notes, locationdescription_id)\n" +
                                "    VALUES (DEFAULT, ?, ?, \n" +
                                "            ?, ?, ?);";
        
        // single row formatting of query_icse
        // SELECT DISTINCT space_id FROM checklistspaceelement INNER JOIN spaceelement ON (spaceelement_id = spaceelementid) WHERE checklist_id = 1;
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        ListIterator<OccInspectedElement> inspectedElementListIterator = is.getInspectedElementList().listIterator();
        OccInspectedElement ie;

        try {
            stmt = con.prepareStatement(query_locationInsert);
            stmt.setString(1, is.getLocation().getLocationDescription());
            stmt.execute();
            
            // now get the ID of our location insert for use in the big insert
            int locationDescriptorID = 0;
            stmt = con.prepareStatement(query_locationID);
            rs = stmt.executeQuery();
            while(rs.next()){
                 locationDescriptorID = rs.getInt(1);
            }

            // we have the parts we need for inserting into the inspectedchecklistspaceelement
            // for each inspected element, build and execute and insert
            stmt = con.prepareStatement(query_icse);
            while (inspectedElementListIterator.hasNext()) {
                ie = inspectedElementListIterator.next();
                stmt.setInt(1, oi.getInspectionID());
                stmt.setInt(2, ie.getId());
                if(ie.getComplianceDate() != null){
                    stmt.setTimestamp(3, java.sql.Timestamp.valueOf(ie.getComplianceDate()));
                } else {
                    stmt.setNull(3, java.sql.Types.NULL);
                }
                stmt.setString(4, ie.getNotes());
                stmt.setInt(5, locationDescriptorID);
                stmt.execute();
            }

            } catch (SQLException ex) {
                System.out.println(ex.toString());
                throw new IntegrationException("Unable to insert newly inspected space into DB, sorry!", ex);

            } finally{
                 if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
                 if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
                 if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
            } // close finally
    }
    
    /**
     * For updating values on InspectedSpaces which have previously been committed with 
     * insertNewlyInspectedSpace. 
     * @param oi
     * @param is
     * @throws IntegrationException thrown for standard SQL errors and this method being 
 given an OccInspectedSpace object whose constituent members lack ID numbers for the updates.
     */
    public void updateInspectedSpace(OccInspection oi, OccInspectedSpace is) throws IntegrationException{
        
        String query = "";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();

            while(rs.next()){

            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

    }
    
    public void deleteInspectedSpace(OccInspectedSpace is) throws IntegrationException{
        
        String query = "";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();

            while(rs.next()){

            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
    }
    
    public OccSpaceType getSpaceType(int spacetypeid) throws IntegrationException{
        String query =  "SELECT spacetypeid, spacetitle, description\n" +
                        " FROM public.spacetype WHERE spacetypeid = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        OccSpaceType st = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, spacetypeid);
            rs = stmt.executeQuery();

            while(rs.next()){
                st = generateSpaceType(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return st;
        
    }
    
    public ArrayList<OccSpaceType> getSpaceTypeList() throws IntegrationException{

        String query = "SELECT spacetypeid, spacetitle, description" +
                "  FROM public.spacetype;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<OccSpaceType> spaceTypeList = new ArrayList();

        try {

            stmt = con.prepareStatement(query);
            //stmt.setInt(1, catID);
            rs = stmt.executeQuery();
            System.out.println("SpaceTypeIntegrator.getSpaceTypeList| SQL: " + stmt.toString());

            while(rs.next()){
                //inspectableCodeElementList.add(getInspectionGuidelines(rs.getString("inspection_guidelines")));
                /* see project notes, I will have to figure out how to align the CodeElement
                property of the InspectableCodeElement to have it fit in to the rs (ResultSet)
                and get returned based on foreign key alignment between inspectablecodeleement
                and codeelements...
                */
                spaceTypeList.add(generateSpaceType(rs));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get space type", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return spaceTypeList;
    }
    
    private OccSpaceType generateSpaceType(ResultSet rs) throws IntegrationException{
        OccSpaceType newSpaceType = new OccSpaceType();


        try {
            newSpaceType.setSpaceTypeID(rs.getInt("spacetypeid"));
            newSpaceType.setSpaceTypeTitle(rs.getString("spacetitle"));
            newSpaceType.setSpaceTypeDescription(rs.getString("description"));
            //newIce.setHighImportance(rs.getBoolean("high_importance"));
            //newIce.setInspectableCodeElementId(rs.getInt("inspectablecodeelementid"));
            //java.sql.Timestamp s = rs.getTimestamp("date");
            //if(s != null){
            //    newIce.setIceDate(s.toLocalDateTime());
            //} else {
            //    newIce.setIceDate(null);
            //}
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error generating space type from ResultSet", ex);
        }
         return newSpaceType;   

    }
    
    public void insertSpaceType(OccSpaceType spaceType) throws IntegrationException{
        String query = "INSERT INTO public.spacetype(\n" +
                "         spacetypeid, spacetitle, description) \n" +
                "    VALUES (DEFAULT, ?, ?)";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            //stmt.setInt(1, inspectableCodeElement.getInspectableCodeElementId());
            //stmt.setInt(1, spaceType.getSpaceTypeID());
            stmt.setString(1, spaceType.getSpaceTypeTitle());
            stmt.setString(2, spaceType.getSpaceTypeDescription());
            /*Previously had date as an attribute of ICE entity... 
            note that the timestamp is set by a call to postgres's now()
            //stmt.setInt(4, exercise.getLift_id());
            if(inspectableCodeElement.getIceDate() != null){
                //keep in  mind that we note place holder "4"' because it  points to the
                //fourth "?" symbol in our query...skips DEFAULT 
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(inspectableCodeElement.getIceDate()));
                
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            */
            
            System.out.println("SpaceTypeIntegrator.insertSpaceType| sql: " + stmt.toString());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert SpaceType", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
      public void deleteSpaceType(OccSpaceType spaceType) throws IntegrationException {
         String query = "DELETE FROM public.spacetype\n" +
                        " WHERE spacetypeid= ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, spaceType.getSpaceTypeID());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete space type--probably because another"
                    + "part of the database has a reference item.", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
      
    public void updateSpaceType(OccSpaceType spaceType) throws IntegrationException {
        String query = "UPDATE public.spacetype\n" +
                    "   SET spacetitle=?, description=?\n" +
                    " WHERE spacetypeid=?;";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try{
            stmt = con.prepareStatement(query);
            stmt.setString(1, spaceType.getSpaceTypeTitle());
            stmt.setString(2, spaceType.getSpaceTypeDescription());
            stmt.setInt(3, spaceType.getSpaceTypeID());
            System.out.println("TRYING TO EXECUTE UPDATE METHOD");
            stmt.executeUpdate();
        } catch (SQLException ex){
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update space type", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
    }
}
