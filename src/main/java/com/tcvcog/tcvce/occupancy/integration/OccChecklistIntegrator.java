/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.occupancy.integration;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.occupancy.OccChecklistTemplate;
import com.tcvcog.tcvce.entities.occupancy.OccSpace;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceElement;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceType;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceTypeChecklistified;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 *  Container for methods used in creating an occupancy checklist
 * which is implemented during an occupancy inspection 
 * 
 * @author Ellen Bascomb of Apartment 31Y
 */
public class OccChecklistIntegrator extends BackingBeanUtils{

    /**
     * Creates a new instance of OccChecklistIntegrator
     */
    public OccChecklistIntegrator() {
    }


    /**
     * Extracts checklist templates by municipality
     * @param muni for record filtering; pass null to extract ALL checklists for management purposes, 
     * including deactivated ones
     * @return an instance of List, perhaps with checklists
     * @throws IntegrationException 
     */
    public List<OccChecklistTemplate> getOccChecklistTemplateList(Municipality muni) throws IntegrationException {
        List<OccChecklistTemplate> checklistList = new ArrayList<>();
        StringBuilder sb = new StringBuilder(); 
        sb.append("SELECT checklistid FROM public.occchecklist ");
        if(muni == null){
            sb.append(";"); // get ALL checklist templates
        } else {
            sb.append("WHERE muni_municode=? AND active = TRUE;");
            
        }
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sb.toString());
            if(muni != null){
                stmt.setInt(1, muni.getMuniCode()); 
            } else {
                // no need to inject
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                checklistList.add(getChecklistTemplate(rs.getInt("checklistid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrieve occinspectionlist", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return checklistList;
    }

    /**
     * Connects an OccSpaceTypeChecklistified to a List of CodeElements. 
     * Note that the 
     *
     * @param tpe
     * @param elementsToAttach a list of CodeElements that should be inspected
     * in this space
     * @throws IntegrationException
     */
    public void attachCodeElementsToSpaceTypeInChecklist(OccSpaceTypeChecklistified tpe, List<CodeElement> elementsToAttach) throws IntegrationException {
        String sql =    "INSERT INTO public.occchecklistspacetypeelement(\n" +
                        "            spaceelementid, codeelement_id, required, checklistspacetype_typeid, notes)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?);";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ListIterator li = elementsToAttach.listIterator();
        CodeElement ce;
        try {
            // for each CodeElement in the list passed into the method, make an entry in the spaceelement table
            while (li.hasNext()) {
                ce = (CodeElement) li.next();
                stmt = con.prepareStatement(sql);
                
                stmt.setInt(1, ce.getElementID());
                stmt.setBoolean(2, tpe.isRequired());
                stmt.setInt(3, tpe.getSpaceTypeID());
                stmt.setString(4, tpe.getNotes());
                
                stmt.execute();
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
    }

   

    /**
     * Builds a list of OccSpaceType IDs from records in the occchecklistspacetype using a checklist ID
     * table
     * @param checklistID
     * @return the list of fully-baked objects
     * @throws IntegrationException 
     */
    public List<Integer> getOccSpaceTypeChecklistifiedIDListByChecklist(int checklistID) throws IntegrationException {
        String query = "SELECT spacetype_typeid FROM public.occchecklistspacetype WHERE checklist_id=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<Integer> ostidl = new ArrayList<>();
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, checklistID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                ostidl.add(rs.getInt("spacetype_typeid"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("getOccSpaceTypeChecklistifiedIDListByChecklist | Error building list of space type IDs by checklist!", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return ostidl;
    }

    public void deleteOccChecklistSpaceType(OccSpaceType spaceType) throws IntegrationException {
        String query = "DELETE FROM public.occchecklistspacetype\n" + " WHERE spacetype_typeid= ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, spaceType.getSpaceTypeID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete space type--probably because another" + "part of the database has a reference item.", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }

    /**
     * Retrieves and builds a bare bones checklist, with title and such, but no
     * space types or elements--see the config methods on the OccInspectionCoordinator
     * @param checklistID
     * @return
     * @throws IntegrationException 
     */
    public OccChecklistTemplate getChecklistTemplate(int checklistID) throws IntegrationException {
        String checklistTableSELECT = "SELECT checklistid, title, description, muni_municode, active, governingcodesource_sourceid\n" + 
                "  FROM public.occchecklist WHERE checklistid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        OccChecklistTemplate template = null;
        try {
            stmt = con.prepareStatement(checklistTableSELECT);
            stmt.setInt(1, checklistID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                template = generateChecklistTemplate(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build a checklist blueprint from database", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
        return template;
    }
    
    
    /**
     * Extracts a record from occspacetype
     *
     * @param spaceTypeID
     * @return
     * @throws IntegrationException
     */
    public OccSpaceType getOccSpaceType(int spaceTypeID) throws IntegrationException {
        OccSpaceType spaceType = null;
        String query = "SELECT spacetypeid, spacetitle, description\n" +
                    "  FROM public.occspacetype WHERE spacetypeid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, spaceTypeID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                spaceType = generateOccSpaceType(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Could not create sapce", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return spaceType;
    }

    
    /**
     * Updates a record in the occchecklistspacetype by ID
     * @param ostc
     * @throws IntegrationException 
     */
    public void updateSpaceTypeChecklistified(OccSpaceTypeChecklistified ostc) throws IntegrationException {
        String query = "UPDATE public.occchecklistspacetype\n" +
                        "   SET checklist_id=?, required=?, spacetype_typeid=?, \n" +
                        "       notes=?\n" +
                        " WHERE checklistspacetypeid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ostc.getChecklistParentID());
            stmt.setBoolean(2, ostc.isRequired());
            stmt.setInt(3, ostc.getSpaceTypeID());
            stmt.setString(4, ostc.getNotes());
            stmt.setInt(5, ostc.getChecklistSpaceTypeID());
            
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update space type", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        }
    }

  
    /**
     * Creates a new record in the occspacetype
     * @param spaceType
     * @throws IntegrationException 
     */
    public void insertSpaceType(OccSpaceType spaceType) throws IntegrationException {
        String query = "INSERT INTO public.occspacetype(\n" +
                        "            spacetypeid, spacetitle, description)\n" +
                        "    VALUES (DEFAULT, ?, ?);";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, spaceType.getSpaceTypeTitle());
            stmt.setString(2, spaceType.getSpaceTypeDescription());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert SpaceType", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally

    }

    /**
     * Generator for OccSpaceType objects
     * @param rs for a single record with all fields SELECTed
     * @return the fully-baked object
     * @throws SQLException
     * @throws IntegrationException 
     */
    private OccSpaceType generateOccSpaceType(ResultSet rs) throws SQLException, IntegrationException {
        OccSpaceType tpe = new OccSpaceType();
        tpe.setSpaceTypeID(rs.getInt("spacetypeid"));
        tpe.setSpaceTypeTitle(rs.getString("spacetitle"));
        tpe.setSpaceTypeDescription(rs.getString("description"));
        // also other useless fields are present
        return tpe;
    }    
    
    public void deleteSpaceType(OccSpaceType spaceType) throws IntegrationException {
        String query = "DELETE FROM public.occchecklistspacetype\n" + " WHERE spacetypeid= ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, spaceType.getSpaceTypeID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete space type--probably because another" + "part of the database has a reference item.", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
    }

    /**
     * Deletes both the linked spaceelements and the space entry itself
     *
     * @param s
     * @throws IntegrationException
     */
    public void deleteSpaceAndElementLinks(OccSpace s) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            // start by removing space elements
            String query = "DELETE FROM public.occspaceelement\n" + " WHERE spaceelementid = ?;";
            stmt = con.prepareStatement(query);
            stmt.setInt(1, s.getSpaceID());
            stmt.executeQuery();
            // now remove the space itself
            query = "DELETE FROM public.space WHERE spaceid = ?";
            stmt = con.prepareStatement(query);
            stmt.setInt(1, s.getSpaceID());
            stmt.executeQuery();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to delete the space and its associated" + "elements: Probably because this space has been used in one or more" + "occupancy inspectsion. It's here to stay!", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
    }


    public void insertOccChecklistSpaceType(int checklistid, OccSpaceType ost) throws IntegrationException {
        String query = "INSERT INTO public.occchecklistspacetype(\n" + "     checklistspacetypeid, checklist_id, required, spacetype_typeid, \n" + "     overridespacetyperequired, overridespacetyperequiredvalue, overridespacetyperequireallspaces) \n" + "     VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, checklistid);
            stmt.setInt(2, ost.getSpaceTypeID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert ChecklistSpaceType", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
    }

    /**
     * Removes the connection between a OccSpace and a Code Element. Handy for
     * adjusting ChecklistBlueprints
     *
     * @param spc
     * @param elementToDetach
     * @throws IntegrationException
     */
    public void detachCodeElementFromSpace(OccSpace spc, CodeElement elementToDetach) throws IntegrationException {
        String query = "DELETE FROM public.occspaceelement\n" + " WHERE space_id = ? AND codeelement_id = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, spc.getSpaceID());
            stmt.setInt(2, elementToDetach.getElementID());
            System.out.println("ChecklistIntegrator.dettachCodeElementsFromSpace | stmt: " + stmt.toString());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
    }

    /**
     * Utility method for populating a OccChecklistTemplate with metadata
     *
     * @param rs containing all metadata fields from the checklist table
     * @return half-baked OccChecklistTemplate object (no element lists)
     */
    private OccChecklistTemplate generateChecklistTemplate(ResultSet rs) throws SQLException, IntegrationException {
        OccChecklistTemplate chkList = new OccChecklistTemplate();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        CodeIntegrator ci = getCodeIntegrator();
        chkList.setInspectionChecklistID(rs.getInt("checklistid"));
        chkList.setTitle(rs.getString("title"));
        chkList.setDescription(rs.getString("description"));
        chkList.setMuni(mi.getMuni(rs.getInt("muni_municode")));
        chkList.setActive(rs.getBoolean("active"));
        chkList.setGoverningCodeSource(ci.getCodeSource(rs.getInt("governingcodesource_sourceid")));
        return chkList;
    }

    /**
     * Creates a record in occchecklist
     * Updated for 21-JUL'21
     *
     * @param bp
     * @throws IntegrationException
     */
    public void insertChecklistTemplateMetadata(OccChecklistTemplate bp) throws IntegrationException {
        String query = "INSERT INTO public.occchecklist(\n" 
                + "            checklistid, title, description, muni_municode, active, governingcodesource_sourceid, createdts)\n" 
                + "    VALUES (DEFAULT, ?, ?, ?, ?, ?, now());";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, bp.getTitle());
            stmt.setString(2, bp.getDescription());
            stmt.setInt(3, bp.getMuni().getMuniCode());
            stmt.setBoolean(4, bp.isActive());
            if (bp.getGoverningCodeSource() != null) {
                stmt.setInt(5, bp.getGoverningCodeSource().getSourceID());
            } else {
                stmt.setNull(5, Types.NULL);
            }
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
    }

    public OccSpaceType getSpaceType(int spacetypeid) throws IntegrationException {
        String query =      "   SELECT checklistspacetypeid, checklist_id, required, spacetype_typeid, \n" +
                            "       notes\n" +
                            "  FROM public.occchecklistspacetype WHERE spacetypeid = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        OccSpaceType st = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, spacetypeid);
            rs = stmt.executeQuery();
            while (rs.next()) {
                st = generateOccSpaceType(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Could not get space type", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return st;
    }

    public void detachElement(int spaceid) throws IntegrationException {
        String query = "DELETE FROM public.occspaceelement\n" + " WHERE space_id= ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, spaceid);
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot detach element--probably because another" + "part of the database has a reference item.", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
    }

    /**
     * Updates a record in occchecklist
     * @param blueprint
     * @throws IntegrationException
     */
    public void updateChecklistTemplateMetadata(OccChecklistTemplate blueprint) throws IntegrationException {
        String query = "UPDATE public.occchecklist\n" 
                + "   SET title=?, description=?, muni_municode=?, active=?, \n" 
                + "       governingcodesource_sourceid=?\n" + " WHERE checklistid=?";
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
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
    }

 
    public int getLastInsertSpaceTypeid(OccInspectionIntegrator occInspectionIntegrator) {
        int lastInsertSpaceTypeid = 0;
        String query = "SELECT spacetypeid\n" + "  FROM public.occchecklistspacetype\n" + "  ORDER BY spacetypeid DESC\n" + "  LIMIT 1;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                lastInsertSpaceTypeid = rs.getInt("spacetypeid");
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return lastInsertSpaceTypeid;
    }

    /**
     * Takes in a OccSpace object, extracts its ID, then uses that to query for
     * all of the codeelements attached to that space. Used to compose a
     * checklist blueprint which can be converted into an implemented checklist
     * 
     * @param ocstid the checklistspacetypeID
     * @return a OccSpaceChecklistified object populated with CodeElement objects
     * @throws IntegrationException
     */
    public OccSpaceTypeChecklistified getOccSpaceTypeChecklistified(int ocstid) throws IntegrationException {
        
        String query = "SELECT 	occchecklistspacetypeelement.spaceelementid, \n" + //inc
                        "	occchecklistspacetypeelement.codeelement_id, \n" + //inc
                        "	occchecklistspacetypeelement.required, \n" + //inc
                        "	occchecklistspacetypeelement.checklistspacetype_typeid, \n" + // join field
                        "	occchecklistspacetypeelement.notes,\n" + //inc
                        "	occchecklistspacetype.checklistspacetypeid, \n" + //inc
                        "	occchecklistspacetype.checklist_id, \n" + //inc
                        "	occchecklistspacetype.required, \n" + //inc
                        "	occchecklistspacetype.spacetype_typeid, \n" + // join field
                        "	occchecklistspacetype.notes,\n" + //inc
                        "	occspacetype.spacetypeid, \n" + //inc
                        "	occspacetype.spacetitle, \n" + //inc
                        "	occspacetype.description\n" + //inc
                        "FROM	public.occchecklistspacetypeelement \n" +
                        "	INNER JOIN public.occchecklistspacetype ON (occchecklistspacetypeelement.checklistspacetype_typeid=occchecklistspacetype.checklistspacetypeid)\n" +
                        "	INNER JOIN public.occspacetype ON (occchecklistspacetype.spacetype_typeid = occspacetype.spacetypeid)\n" +
                        "WHERE 	occchecklistspacetype.checklistspacetypeid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<OccSpaceElement> eleList = new ArrayList<>();
        OccSpaceTypeChecklistified ostc = new OccSpaceTypeChecklistified();
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ocstid);
            rs = stmt.executeQuery();
            boolean containerBuilt = false;
            while (rs.next()) {
                if(!containerBuilt){
                    ostc.setChecklistSpaceTypeID(rs.getInt("occchecklistspacetype.checklistspacetypeid"));
                    ostc.setSpaceTypeID(rs.getInt("occspacetype.spacetypeid"));
                    ostc.setSpaceTypeTitle("occspacetype.spacetitle");
                    ostc.setSpaceTypeDescription("occspacetype.description");
                    ostc.setRequired(rs.getBoolean("occchecklistspacetype.required"));
                    ostc.setChecklistParentID(rs.getInt("occchecklistspacetype.checklist_id"));
                    ostc.setNotes(rs.getString("occchecklistspacetype.notes"));
                }
                // this call to the code coordinator is basically an antipattern--but for this level of 
                // complexity of object creation, we'll try this for testing
                OccSpaceElement ele = new OccSpaceElement(getCodeCoordinator().getCodeElement(rs.getInt("occchecklistspacetypeelement.spaceelementid")));
                ele.setRequiredForInspection(rs.getBoolean("occchecklistspacetypeelement.required"));
                ele.setSpaceElementID(rs.getInt("occchecklistspacetypeelement.spaceelementid"));
                ele.setNotes(rs.getString("occchecklistspacetypeelement.notes"));
                eleList.add(new OccSpaceElement(ele));
            }
            //inject our list of elements
            ostc.setCodeElementList(eleList);
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("OccChecklistIntegrator.getOccSpaceTypeChecklistified | Could not built fancy OccSpaceTypeChecklistified!", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return ostc;
    }
    
}
