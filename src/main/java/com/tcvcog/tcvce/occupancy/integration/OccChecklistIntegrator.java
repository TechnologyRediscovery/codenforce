/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.occupancy.integration;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
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
    public List<Integer> getOccChecklistTemplateList(Municipality muni) throws IntegrationException {
        List<Integer> checklistIDList = new ArrayList<>();
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
                checklistIDList.add(rs.getInt("checklistid"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrieve occinspectionlist", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return checklistIDList;
    }
    
    
    

    /**
     * Connects an OccSpaceTypeChecklistified to a List of CodeElements. 
     * Note that the 
     *
     * @param tpe
     * @param ecel a list of CodeElements that should be inspected
     * in this space
     * @throws IntegrationException
     */
    public void attachCodeElementsToSpaceTypeInChecklist(OccSpaceTypeChecklistified tpe, List<EnforcableCodeElement> ecel) throws IntegrationException {
        String sql =    "INSERT INTO public.occchecklistspacetypeelement(\n" +
                        "            spaceelementid, codesetelement_seteleid, required, checklistspacetype_typeid, notes)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?);";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ListIterator li = ecel.listIterator();
        EnforcableCodeElement ece;
        try {
            // for each CodeElement in the list passed into the method, make an entry in the spaceelement table
            while (li.hasNext()) {
                ece = (EnforcableCodeElement) li.next();
                stmt = con.prepareStatement(sql);
                
                stmt.setInt(1, ece.getCodeSetElementID());
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
        String query = "SELECT checklistspacetypeid FROM public.occchecklistspacetype WHERE checklist_id=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<Integer> ostidl = new ArrayList<>();
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, checklistID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                ostidl.add(rs.getInt("checklistspacetypeid"));
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

    /**
     * Removes a space type from a checklist
     * 
     * @param ostchk
     * @throws IntegrationException 
     */
    public void deleteOccSpaceTypeChecklistified(OccSpaceTypeChecklistified ostchk) throws IntegrationException {
        String query = "DELETE FROM public.occchecklistspacetype\n" 
                + " WHERE checklistspacetypeid = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ostchk.getChecklistSpaceTypeID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete space type checklistified--probably because another" + "part of the database has a reference item.", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    /**
     * Removes a space type from a checklist
     * 
     * @param spaceType
     * @throws IntegrationException 
     */
    public void deleteSpaceType(OccSpaceType spaceType) throws IntegrationException {
        String query = "DELETE FROM public.occspacetype\n" 
                + " WHERE spacetypeid= ?;";
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
        String checklistTableSELECT = "SELECT checklistid, title, description, muni_municode, active, governingcodesource_sourceid, createdts \n" + 
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
        if(rs.getTimestamp("createdts") != null){
            chkList.setCreatedTS(rs.getTimestamp("createdts").toLocalDateTime());
        } 
        
        return chkList;
    }

    
    
    /**
     * Extracts a record from occspacetype, with no inspection meta data attached
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
        return tpe;
    }    
    
    
    /**
     * Extracts all space types from the DB
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> getOccSpaceTypeIDListComplete() throws IntegrationException{
        List<Integer> idl = new ArrayList<>();
        
        String query = "SELECT spacetypeid, spacetitle, description\n" +
                    "  FROM public.occspacetype;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                idl.add(rs.getInt("spacetypeid"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Could not create sapce", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return idl;
    }

    
      
    /**
     * Updates a record in the occspacetype by ID
     * @param ost to update
     * @throws IntegrationException 
     */
    public void updateSpaceType(OccSpaceType ost) throws IntegrationException {
        String query = "UPDATE public.occspacetype\n" +
                        "   SET spacetitle=?, description=?\n" +
                        " WHERE spacetypeid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, ost.getSpaceTypeTitle());
            stmt.setString(2, ost.getSpaceTypeDescription());
            
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
     * Updates a record in the occchecklistspacetype by ID
     * @param ose
     * @throws IntegrationException 
     */
    public void updateOccSpaceElement(OccSpaceElement ose) throws IntegrationException {
        if(ose == null){
            throw new IntegrationException("Cannot update null OccSpaceElement");
        }
        String query = "UPDATE public.occchecklistspacetypeelement\n" +
                        "   SET required=? WHERE spaceelementid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setBoolean(1, ose.isRequiredForInspection());
            stmt.setInt(2, ose.getOccChecklistSpaceTypeElementID());
            
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to OccSpaceElement", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        }
    }

  
    /**
     * Creates a new record in the occspacetype--bare bones type
     * @param spaceType
     * @throws IntegrationException 
     */
    public int insertSpaceType(OccSpaceType spaceType) throws IntegrationException {
        String query = "INSERT INTO public.occspacetype(\n" +
                        "            spacetypeid, spacetitle, description)\n" +
                        "    VALUES (DEFAULT, ?, ?);";

        Connection con = getPostgresCon();
         ResultSet rs = null;
         int freshID = 0;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, spaceType.getSpaceTypeTitle());
            stmt.setString(2, spaceType.getSpaceTypeDescription());
            stmt.execute();
            
            
            String retrievalQuery = "SELECT currval('spacetype_spacetypeid_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();

            while (rs.next()) {
                freshID = rs.getInt(1);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert SpaceType", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
        return freshID;
    }

  
  
 /**
     * Extracts all space types from the DB
     * @param ostchk
     * @return
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public void verifyUniqueChecklistSpaceTypeLink(OccSpaceTypeChecklistified ostchk) throws IntegrationException, BObStatusException{
        List<Integer> idl = new ArrayList<>();
        
        String query = "SELECT checklistspacetypeid \n" +
                        "  FROM public.occchecklistspacetype WHERE checklist_id=? AND spacetype_typeid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ostchk.getChecklistParentID());
            stmt.setInt(2, ostchk.getSpaceTypeID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Cannot link space type to checklist: Duplicate Mapping. ");
                sb.append("Checklist ID ");
                sb.append(ostchk.getChecklistParentID());
                sb.append(" is already linked to space type ID ");
                sb.append(ostchk.getSpaceTypeID());
                throw new BObStatusException(sb.toString());
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Could not create sapce", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
    }


    /**
     * Inserts a record into the occchecklistspacetype table but does not
     * process its OccSpaceElement list in its belly.
     * @param ost
     * @throws IntegrationException 
     */
    public void insertOccChecklistSpaceTypeChecklistified(OccSpaceTypeChecklistified ost) throws IntegrationException {
        String query = "INSERT INTO public.occchecklistspacetype(\n" 
                + "     checklistspacetypeid, checklist_id, required, spacetype_typeid, notes) \n" 
                + "     VALUES (DEFAULT, ?, ?, ?, ?)";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            
            stmt.setInt(1, ost.getChecklistParentID());
            stmt.setBoolean(2, ost.isRequired());
            stmt.setInt(3, ost.getSpaceTypeID());
            stmt.setString(4, ost.getNotes());
            
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
     * TODO: adapt to new
     *
     * @param spc
     * @param elementToDetach
     * @throws IntegrationException
     */
    public void detachOccSpaceElementFromOccSpaceTypeChecklistified(OccSpaceElement elementToDetach) throws IntegrationException {
        String query = "DELETE FROM public.occchecklistspacetypeelement\n" 
                + " WHERE checklistspacetype_typeid = ? AND codeelement_id = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, elementToDetach.getParentSpaceTypeID());
            stmt.setInt(2, elementToDetach.getElementID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("OccChecklistIntegrator.detachCodeElementFromOccSpaceTypeChecklistified | Could not remove link", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
    }

    /**
     * Creates a record in occchecklist
     * Updated for 21-JUL'21
     *
     * @param bp
     * @throws IntegrationException
     */
    public int insertChecklistTemplateMetadata(OccChecklistTemplate bp) throws IntegrationException {
        String query = "INSERT INTO public.occchecklist(\n" 
                + "            checklistid, title, description, muni_municode, active, governingcodesource_sourceid, createdts)\n" 
                + "    VALUES (DEFAULT, ?, ?, ?, ?, ?, now());";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        int freshID = 0;
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
            
            String retrievalQuery = "SELECT currval('checklist_checklistid_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();

            while (rs.next()) {
                freshID = rs.getInt(1);
            }
            
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return freshID;
    }

    

    /**
     * Updates a record in occchecklist
     * @param oct
     * @throws IntegrationException
     */
    public void updateChecklistTemplateMetadata(OccChecklistTemplate oct) throws IntegrationException {
        String query = "UPDATE public.occchecklist\n" 
                + "   SET title=?, description=?, muni_municode=?, active=?, \n" 
                + "       governingcodesource_sourceid=?\n" 
                + " WHERE checklistid=?";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, oct.getTitle());
            stmt.setString(2, oct.getDescription());
            stmt.setInt(3, oct.getMuni().getMuniCode());
            stmt.setBoolean(4, oct.isActive());
            stmt.setInt(5, oct.getGoverningCodeSource().getSourceID());
            stmt.setInt(6, oct.getInspectionChecklistID());
            
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot update checklist metatdata!", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
    }

 
    /**
     * Asks DB for the ID of the most recently assigned ID
     * @param occInspectionIntegrator
     * @return 
     */
    public int getLastInsertSpaceTypeid(OccInspectionIntegrator occInspectionIntegrator) {
        int lastInsertSpaceTypeid = 0;
        String query =  "SELECT spacetypeid\n" 
                    +   "  FROM public.occchecklistspacetype\n" 
                    +   "  ORDER BY spacetypeid DESC\n" 
                    +   "  LIMIT 1;";
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
     * Takes in a OccSpace type ID, then uses that to query for
     * all of the codeelements attached to that space. Used to compose a
     * checklist blueprint which can be converted into an implemented checklist
     * 
     * @param ocstid the checklistspacetypeID
     * @return a OccSpaceChecklistified object populated with CodeElement objects
     * @throws IntegrationException
     */
    public OccSpaceTypeChecklistified getOccSpaceTypeChecklistified(int ocstid) throws IntegrationException {
        
         String query = "SELECT 	occchecklistspacetypeelement.spaceelementid, \n" + //inc
                        "	occchecklistspacetypeelement.codesetelement_seteleid, \n" + //inc
                        "	occchecklistspacetypeelement.required AS ocsterequired, \n" + //inc
                        "	occchecklistspacetypeelement.checklistspacetype_typeid, \n" + // join field
                        "	occchecklistspacetypeelement.notes AS ocstenotes,\n" + //inc
                        "	occchecklistspacetype.checklistspacetypeid, \n" + //inc
                        "	occchecklistspacetype.checklist_id, \n" + //inc
                        "	occchecklistspacetype.required AS ocstrequired, \n" + //inc
                        "	occchecklistspacetype.spacetype_typeid, \n" + // join field
                        "	occchecklistspacetype.notes AS ocstnotes \n" + //inc
                        "FROM    public.occchecklistspacetype \n" +
                        "	LEFT OUTER JOIN public.occchecklistspacetypeelement ON (occchecklistspacetypeelement.checklistspacetype_typeid=occchecklistspacetype.checklistspacetypeid) " +
                        "WHERE 	occchecklistspacetype.checklistspacetypeid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<OccSpaceElement> eleList = new ArrayList<>();
        OccSpaceTypeChecklistified ostc = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ocstid);
            rs = stmt.executeQuery();
            boolean containerBuilt = false;
            while (rs.next()) {
                if(!containerBuilt){
                    ostc = new OccSpaceTypeChecklistified(getOccSpaceType(rs.getInt("spacetype_typeid")));
                    ostc.setChecklistSpaceTypeID(rs.getInt("checklistspacetypeid"));
                    ostc.setRequired(rs.getBoolean("ocstrequired"));
                    ostc.setChecklistParentID(rs.getInt("checklist_id"));
                    ostc.setNotes(rs.getString("ocstnotes"));
                    containerBuilt = true;
                }
                
                // Because of this yucky left outer join, we can get back a row to correspond
                // to a type but a type that has no elements to inspect. So this was yielding
                // an occspacelement without any guts and erroring out the insert.
                if(rs.getInt("spaceelementid") != 0){
                    // this call to the code coordinator is basically an antipattern--but for this level of 
                    // complexity of object creation, we'll try this for testing
                    int seteleid = rs.getInt("codesetelement_seteleid");
                    OccSpaceElement ele = new OccSpaceElement(getCodeCoordinator().getEnforcableCodeElement(seteleid));
                    ele.setRequiredForInspection(rs.getBoolean("ocsterequired"));
                    ele.setOccChecklistSpaceTypeElementID(rs.getInt("spaceelementid"));
                    ele.setNotes(rs.getString("ocstenotes"));
                    ele.setParentSpaceTypeID(rs.getInt("checklistspacetype_typeid"));
                    eleList.add(new OccSpaceElement(ele));
                }
            }
            //inject our list of elements
            if(ostc !=null){
                ostc.setCodeElementList(eleList);
            }
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
