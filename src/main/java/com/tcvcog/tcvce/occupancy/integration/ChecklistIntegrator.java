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
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.entities.occupancy.OccChecklistBlueprint;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedElement;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpace;
import com.tcvcog.tcvce.entities.occupancy.OccLocationDescriptor;
import com.tcvcog.tcvce.entities.occupancy.OccInspection;
import com.tcvcog.tcvce.entities.occupancy.OccSpace;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceType;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
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
    
   
    
    public void updateChecklistBlueprintMetadata(OccChecklistBlueprint blueprint) throws IntegrationException{
        
        String query =  "UPDATE public.occchecklist\n" +
                        "   SET title=?, description=?, muni_municode=?, active=?, \n" +
                        "       governingcodesource_sourceid=?\n" +
                        " WHERE checklistid=?";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, blueprint.getTitle());
            stmt.setString(2, blueprint.getDescription());
            stmt.setInt(3, blueprint.getMuni().getMuniCode());
            stmt.setBoolean(4, blueprint.isActive());
            stmt.setInt(5, blueprint.getGoverningCodeSource().getSourceID());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot update checklist metatdata!", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    // maybe don't need this one
    public void insertChecklistBlueprint(OccChecklistBlueprint bp) throws IntegrationException{
        
        String query = "INSERT INTO public.occchecklist(\n" +
"            checklistid, title, description, muni_municode, active, governingcodesource_sourceid)\n" +
"    VALUES (DEFAULT, ?, ?, ?, ?, ?);";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, bp.getTitle());
            stmt.setString(2, bp.getDescription());
            stmt.setInt(3, bp.getMuni().getMuniCode());
            stmt.setBoolean(4, bp.isActive());
            if(bp.getGoverningCodeSource() != null){
                stmt.setInt(5, bp.getGoverningCodeSource().getSourceID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            stmt.execute();

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
        
        String query = "INSERT INTO public.occspace(\n" +
                        "            spaceid, name, spacetype_id, required)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, s.getName());
            stmt.setInt(2, s.getSpaceType().getSpaceTypeID());
            stmt.setBoolean(3, s.isRequired());
            
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
        
        String query =  "UPDATE public.occspace\n" +
                        "   SET name=?, spacetype_id=?, required=?\n" +
                        " WHERE spaceid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setString(1, s.getName());
            stmt.setInt(2, s.getSpaceType().getSpaceTypeID());
            stmt.setBoolean(3, s.isRequired());
                        
            stmt.executeQuery();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public OccSpace getOccSpace(int spaceID) throws IntegrationException{
        
        OccSpace space = null;
        String query =  "SELECT spaceid, name, spacetype_id, required\n" +
                        "  FROM public.occspace WHERE spaceid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, spaceID);
            rs = stmt.executeQuery();

            while(rs.next()){
                space = generateOccSpace(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Could not create sapce", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return space;
    }
    
    private OccSpace generateOccSpace(ResultSet rs) throws SQLException, IntegrationException{
        OccSpace space = new OccSpace();
        space.setSpaceid(rs.getInt("spaceid"));
        space.setName(rs.getString("name"));
        space.setSpaceType(getSpaceType(rs.getInt("spacetype")));
        space = populateSpaceWithCodeElements(space);
        return space;
        
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
                        " FROM public.occspaceelement WHERE spaceelementid = ?;";
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
        
        String query =  "INSERT INTO public.occspaceelement(\n" +
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
        
        String query = "DELETE FROM public.occspaceelement\n" +
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
    public List<OccSpace> getSpaceList() throws IntegrationException{
        
        String query = "SELECT spaceid FROM public.space;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<OccSpace> spaceAL = new ArrayList();

        try {

            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();

            while(rs.next()){
                spaceAL.add(getOccSpace(rs.getInt("spaceid")));
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
            String query =  "DELETE FROM public.occspaceelement\n" +
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
                spaceList.add(getOccSpace(rs.getInt("space_id")));
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
        CodeIntegrator ci = getCodeIntegrator();
        
        
        bp.setInspectionChecklistID(rs.getInt("checklistid"));
        bp.setTitle(rs.getString("title"));
        bp.setDescription(rs.getString("description"));
        bp.setMuni(mi.getMuni(rs.getInt("muni_municode")));
        bp.setActive(rs.getBoolean("active"));
        bp.setGoverningCodeSource(ci.getCodeSource(rs.getInt("governingcodesource_sourceid")));
        
        
        
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
    public List<OccInspectedSpace> getInspectedSpaceList(int inspectionID) throws IntegrationException{
        ArrayList<OccInspectedSpace> inspSpaceList = new ArrayList<>();
        
        String query_spaceIDs =  "SELECT DISTINCT spaceid\n" +
            " FROM occinspectedchecklistspaceelement INNER JOIN occchecklistspaceelement ON (checklistspaceelementid = checklistspaceelement_id)\n" +
            "	INNER JOIN occspaceelement ON (spaceelement_id = spaceelementid)\n" +
            "	INNER JOIN occspace ON (space_id = spaceid)\n" +
            " WHERE occinspection_id=?;";
        
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
        String query =  "SELECT spacetypeid, spacetitle, description, required\n" +
                        " FROM public.offspacetype WHERE spacetypeid = ?;";
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
    
    private List<OccSpaceType> getSpaceTypeList(int inspectionID) throws IntegrationException{

        String query = "    SELECT checklistspacetypeid, checklist_id, spaceelement_id, required, \n" +
                        "       spacetype_typeid, overridespctyprequired, overridespctypeequiredvalue, \n" +
                        "       overridespctypeequiredallspacesreq\n" +
                        "  FROM public.occchecklistspacetype;";
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
                spaceTypeList.add(getSpaceType(rs.getInt("spacetype_typeid")));
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
            newSpaceType.setRequired(rs.getBoolean("required"));
            
            newSpaceType.setSpaceList(genOccSpaceList(newSpaceType.getSpaceTypeID()));
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error generating space type from ResultSet", ex);
        }
         return newSpaceType;   

    }
    
    private List<OccSpace> genOccSpaceList(int spaceTypeID) throws IntegrationException{
        String query = "SELECT spaceid FROM public.occspace WHERE spacetype_id=?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<OccSpace> spaceList = new ArrayList<>();

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, spaceTypeID);
            rs = stmt.executeQuery();
            System.out.println("SpaceTypeIntegrator.getSpaceTypeList| SQL: " + stmt.toString());

            while(rs.next()){
                spaceList.add(getOccSpace(rs.getInt("spaceid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get space type", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return spaceList;
    }
    
    public void insertSpaceType(OccSpaceType spaceType) throws IntegrationException{
        String query = "INSERT INTO public.occspacetype(\n" +
                "         spacetypeid, spacetitle, description, required) \n" +
                "    VALUES (DEFAULT, ?, ?, ?)";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, spaceType.getSpaceTypeTitle());
            stmt.setString(2, spaceType.getSpaceTypeDescription());
            stmt.setBoolean(3, spaceType.isRequired());
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
         String query = "DELETE FROM public.occspacetype\n" +
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
        String query = "UPDATE public.occspacetype\n" +
                    "   SET spacetitle=?, description=?, required=?\n" +
                    " WHERE spacetypeid=?;";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try{
            stmt = con.prepareStatement(query);
            stmt.setString(1, spaceType.getSpaceTypeTitle());
            stmt.setString(2, spaceType.getSpaceTypeDescription());
            stmt.setBoolean(3, spaceType.isRequired());
            stmt.setInt(4, spaceType.getSpaceTypeID());
            stmt.executeUpdate();
        } catch (SQLException ex){
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update space type", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
    }
    
     public OccInspection getOccInspection(int inspectionID) throws IntegrationException {
        
         OccInspection inspection = null;
         
         String query = " SELECT inspectionid, occperiod_periodid, inspector_userid, publicaccesscc, \n" +
                        "       enablepacc, notes, thirdpartyinspector_personid, thirdpartyinspectorapprovalts, \n" +
                        "       thirdpartyinspectorapprovalby, passedinspection_userid, maxoccupantsallowed, \n" +
                        "       numbedrooms, numbathrooms, passedinspectionts, occchecklist_checklistlistid, \n" +
                        "       effectivedate\n" +
                        "  FROM public.occinspection;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, inspectionID);
            rs = stmt.executeQuery();
            System.out.println("SpaceTypeIntegrator.getSpaceTypeList| SQL: " + stmt.toString());

            while(rs.next()){
                inspection = generateOccupancyInspection(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get space type", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return inspection;
    }

       private OccInspection generateOccupancyInspection(ResultSet rs) throws IntegrationException, SQLException {
        OccInspection ins = new OccInspection();
        
        UserIntegrator ui = getUserIntegrator();
        PersonIntegrator pi = getPersonIntegrator();
        
        ins.setInspectionID(rs.getInt("inspectionid"));
        ins.setOccPeriodID(rs.getInt("occperiod_periodid"));
        ins.setInspector(ui.getUser(rs.getInt("inspector_userid")));
        ins.setPacc(rs.getInt("publicaccesscc"));
        
        ins.setEnablePacc(rs.getBoolean("enablepacc"));
        ins.setNotes(rs.getString("notes"));
        if(rs.getInt("thirdpartyinspector_personid") != 0){
            ins.setThirdPartyInspector(pi.getPerson(rs.getInt("thirdpartyinspector_personid")));
        }
        if(rs.getTimestamp("thirdpartyinspectorapprovalts") != null){
            ins.setThirdPartyInspectorApprovalTS(rs.getTimestamp("thirdpartyinspectorapprovalts").toLocalDateTime());
        }
        
        if(rs.getInt("thirdpartyinspectorapprovalby") != 0){
            ins.setThirdPartyApprovalBy(ui.getUser(rs.getInt("thirdpartyinspectorapprovalby")));
        } 
        if(rs.getInt("passedinspection_userid") != 0){
            ins.setPassCertifiedBy(ui.getUser(rs.getInt("passedinspection_userid")));
        } 
        ins.setMaxOccupantsAllowed(rs.getInt("maxoccupantsallowed"));
        
        ins.setNumBedrooms(rs.getInt("numbedrooms"));
        ins.setNumBathrooms(rs.getInt("numbathrooms"));
        
        if(rs.getTimestamp("effectivedate") != null){
            ins.setEffectiveDate(rs.getTimestamp("effectivedate").toLocalDateTime());
        }
        
        // now set the big lists
        ins.setBlueprint(getChecklistBlueprint(rs.getInt("occchecklist_checklistlistid")));
        ins.setInspectedSpaceList(getInspectedSpaceList(ins.getInspectionID()));
        
        
        return ins;
    }
     
    public void updateOccupancyInspection(OccInspection occInspection) throws IntegrationException {
        String query = "UPDATE public.occinspection\n" 
                + "   SET propertyunitid=?, login_userid=?, firstinspectiondate=?, \n" 
                + "       firstinspectionpass=?, secondinspectiondate=?, secondinspectionpass=?, \n" 
                + "       resolved=?, totalfeepaid=?, notes=?\n" + " WHERE inspectionid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            //            stmt.setInt(1, occInspection.getPropertyUnitID());
            //            stmt.setInt(2, occInspection.getLoginUserID());
            //update first inspection date
            stmt.setString(9, occInspection.getNotes());
            stmt.setInt(10, occInspection.getInspectionID());
            System.out.println("TRYING TO EXECUTE UPDATE METHOD");
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update occupancy inspection record", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
    }

    public void deleteOccupancyInspection(OccInspection occInspection) throws IntegrationException {
        String query = "DELETE FROM public.occupancyinspection\n" + " WHERE inspectionid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, occInspection.getInspectionID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete occupancy inspeciton--probably because another" + "part of the database has a reference item.", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }

        } // close finally
    }


    public void insertOccupanyInspection(OccInspection occupancyInspection) throws IntegrationException {
        String query = "INSERT INTO public.occupancyinspection(\n" + "   inspectionid, propertyunitid, login_userid, firstinspectiondate, " + "firstinspectionpass, secondinspectiondate, secondinspectionpass, " + "resolved, totalfeepaid, notes) \n" + "VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            //            stmt.setInt(1, occupancyInspection.getPropertyUnitID());
            //            stmt.setInt(2, occupancyInspection.getLoginUserID());
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert OccupancyInspection", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
    }

  

   
}
