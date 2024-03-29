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
import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.coordinators.OccInspectionCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.IFace_inspectable;
import com.tcvcog.tcvce.entities.occupancy.*;
import com.tcvcog.tcvce.integration.*;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The master Inspection integrator for methods 
 * used during the actual inspection process.
 * See the ChecklistIntegrator for methods used to configure a checklist
 * 
 * @author ellen bascomb of apt 31y
 */
public class OccInspectionIntegrator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of ChecklistIntegrator
     */
    public OccInspectionIntegrator() {
    }



    /**
     * Called by the OccupancyIntegrator during the construction of an
 FieldInspection object. This method, in turn, calls the private
     * getInspectedSpaceList method in this class to populate the actual
     * inspection data.
     *
     * @param inspectionID
     * @return a fully-baked ImplementedChecklist
     * @throws IntegrationException
     */
    public List<OccInspectedSpace> getInspectedSpaceList(int inspectionID) throws IntegrationException {
        List<OccInspectedSpace> inspSpaceList = new ArrayList<>();
        String query = "SELECT inspectedspaceid FROM public.occinspectedspace WHERE occinspection_inspectionid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {

            // this gets us a list of all of the spaces that have been inspected for this
            // occupancy inspection
            stmt = con.prepareStatement(query);
            stmt.setInt(1, inspectionID);
            rs = stmt.executeQuery();

            // chugg down the list of spaceids and fetch an Inspected space for each
            while (rs.next()) {
                inspSpaceList.add(getInspectedSpace(rs.getInt("inspectedspaceid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate inspected space list, sorry!", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        // temp to close
        return inspSpaceList;
    }

    /**
     * Extracts an inspected space from DB and injects elements
     * @param inspectedspaceID
     * @return
     * @throws IntegrationException 
     */
    public OccInspectedSpace getInspectedSpace(int inspectedspaceID) throws IntegrationException {
        OccInspectedSpace inspectedSpace = null;
        if (inspectedspaceID == 0) {
            return inspectedSpace;
        }
        String querySpace = "SELECT inspectedspaceid, occinspection_inspectionid, \n"
                + "       occlocationdescription_descid, addedtochecklistby_userid, addedtochecklistts, occchecklistspacetype_chklstspctypid \n"
                + "  FROM public.occinspectedspace WHERE inspectedspaceid=?;";
        String queryElements
                = "SELECT occinspectedspaceelement.inspectedspaceelementid\n"
                + "     FROM occinspectedspaceelement INNER JOIN occinspectedspace ON (occinspectedspaceelement.inspectedspace_inspectedspaceid = occinspectedspace.inspectedspaceid)\n"
                + "     WHERE occinspectedspace.inspectedspaceid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<OccInspectedSpaceElement> inspectedEleList = new ArrayList<>();

        try {
            stmt = con.prepareStatement(querySpace);
            stmt.setInt(1, inspectedspaceID);
            rs = stmt.executeQuery();

            // create our subclass OccInspectedSpace by passing in the superclass
            // OccSpace, which we make right away
            while (rs.next()) {
                inspectedSpace = generateOccInspectedSpace(rs);
            }
            stmt = con.prepareStatement(queryElements);
            if (inspectedSpace != null) {
                stmt.setInt(1, inspectedSpace.getInspectedSpaceID());
                rs = stmt.executeQuery();
                while (rs.next()) {
                    inspectedEleList.add(getInspectedSpaceElement(rs.getInt("inspectedspaceelementid")));
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate an inspected space, sorry!", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        if (inspectedSpace != null) {
            // finally, combine our two objects by injecting the Element list into the InspectedSpace
            inspectedSpace.setInspectedElementList(inspectedEleList);
        }
        return inspectedSpace;
    }

    /**
     * GEnerator for OccInspectedSpace
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
    private OccInspectedSpace generateOccInspectedSpace(ResultSet rs) throws SQLException, IntegrationException {
        UserCoordinator uc = getUserCoordinator();
        OccChecklistIntegrator oci = getOccChecklistIntegrator();
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        OccInspectedSpace inSpace = null;
        try {
            
            inSpace = new OccInspectedSpace();
            inSpace.setInspectedSpaceID(rs.getInt("inspectedspaceid"));
            inSpace.setLocation(getLocationDescriptor(rs.getInt("occlocationdescription_descid")));
            
            inSpace.setType(oci.getOccSpaceTypeChecklistified(rs.getInt("occchecklistspacetype_chklstspctypid")));
            
            inSpace.setAddedToChecklistBy(uc.user_getUser(rs.getInt("addedtochecklistby_userid")));
            inSpace.setInspectionID(rs.getInt("occinspection_inspectionid"));
            
            if (rs.getTimestamp("addedtochecklistts") != null) {
                inSpace.setAddedToChecklistTS(rs.getTimestamp("addedtochecklistts").toLocalDateTime());
            }
            
        } catch (BObStatusException ex) {
            System.out.println(ex);
        }
        return inSpace;

    }

    /**
     * Extracts an inspected element and all its meta data from the DB
     * @param eleID
     * @return
     * @throws IntegrationException 
     */
    public OccInspectedSpaceElement getInspectedSpaceElement(int eleID) throws IntegrationException {
        String query_spaceIDs = "SELECT inspectedspaceelementid, "
                + "occinspectedspaceelement.notes AS oisenotes, "
                + "locationdescription_id, "
                + "lastinspectedby_userid, \n"
                + "lastinspectedts, "
                + "compliancegrantedby_userid, "
                + "compliancegrantedts, \n"
                + "inspectedspace_inspectedspaceid, "
                + "overriderequiredflagnotinspected_userid, \n"
                + "occchecklistspacetypeelement_elementid, "
                + "failureseverity_intensityclassid, "
                + "migratetocecaseonfail, "
                + "occinspection_inspectionid, "
                + "occchecklistspacetypeelement.codesetelement_seteleid, transferredts, transferredby_userid, transferredtocecase_caseid  \n"
                + "FROM public.occinspectedspaceelement "
                + "INNER JOIN public.occchecklistspacetypeelement ON (occchecklistspacetypeelement_elementid = spaceelementid) "
                + "INNER JOIN public.occinspectedspace on (occinspectedspace.inspectedspaceid = occinspectedspaceelement.inspectedspace_inspectedspaceid) "
                + "WHERE inspectedspaceelementid=?;";


        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        OccInspectedSpaceElement ele = null;
        try {

            // this gets us a list of all of the spaces that have been inspected for this
            // occupancy inspection
            stmt = con.prepareStatement(query_spaceIDs);
            stmt.setInt(1, eleID);
            rs = stmt.executeQuery();

            // chugg down the list of spaceids and fetch an Inspected space for each
            while (rs.next()) {
                ele = generateInspectedSpaceElement(rs);

            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("getInspectedSpaceElement | Unable to get inspected space, sorry!", ex);

        } finally {
              if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return ele;

    }

    /**
     * GEnerator for inspected space elements
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
    private OccInspectedSpaceElement generateInspectedSpaceElement(ResultSet rs) throws SQLException, IntegrationException {
            CodeIntegrator ci = getCodeIntegrator();
            UserCoordinator uc = getUserCoordinator();
            CaseIntegrator cseint = getCaseIntegrator();
            BlobCoordinator bc = getBlobCoordinator();
            SystemIntegrator si = getSystemIntegrator();
            OccInspectedSpaceElement inspectedEle = null;
        try {
            
            inspectedEle = new OccInspectedSpaceElement(ci.getEnforcableCodeElement(rs.getInt("codesetelement_seteleid")));
            
            inspectedEle.setInspectedSpaceElementID(rs.getInt("inspectedspaceelementid"));
            inspectedEle.setOccChecklistSpaceTypeElementID(rs.getInt("occchecklistspacetypeelement_elementid"));
            
            inspectedEle.setInspectionNotes(rs.getString("oisenotes"));
            inspectedEle.setLocation(getLocationDescriptor(rs.getInt("locationdescription_id")));
            inspectedEle.setLastInspectedBy(uc.user_getUser(rs.getInt("lastinspectedby_userid")));
            
            if (rs.getTimestamp("lastinspectedts") != null) {
                inspectedEle.setLastInspectedTS(rs.getTimestamp("lastinspectedts").toLocalDateTime());
            }
            inspectedEle.setComplianceGrantedBy(uc.user_getUser(rs.getInt("compliancegrantedby_userid")));
            if (rs.getTimestamp("compliancegrantedts") != null) {
                inspectedEle.setComplianceGrantedTS(rs.getTimestamp("compliancegrantedts").toLocalDateTime());
            }
            inspectedEle.setInspectedSpaceID(rs.getInt("inspectedspace_inspectedspaceid"));
            inspectedEle.setOverrideRequiredFlag_thisElementNotInspectedBy(uc.user_getUser(rs.getInt("overriderequiredflagnotinspected_userid")));
            
            inspectedEle.setFaillureSeverity(si.getIntensityClass(rs.getInt("failureseverity_intensityclassid")));
            
            inspectedEle.setMigrateToCaseOnFail(rs.getBoolean("migratetocecaseonfail"));
            
            inspectedEle.setBlobList(bc.getBlobLightList(inspectedEle));
            inspectedEle.setOccInspectionID(rs.getInt("occinspection_inspectionid"));
            cseint.populateTransferrableFields(inspectedEle, rs);
            
        } catch(BObStatusException | BlobException ex){
            System.out.println(ex);
        }
            return inspectedEle;
    }

    /**
     * Creates a new record in the occlocationdescriptor table
     * @param locDesc
     * @return
     * @throws IntegrationException 
     */
    public int insertLocationDescriptor(OccLocationDescriptor locDesc) throws IntegrationException {
        String sql = "INSERT INTO public.occlocationdescriptor(\n"
                + "            locationdescriptionid, description, buildingfloorno)\n"
                + "    VALUES (DEFAULT, ?, ?);";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        int insertedLocDescID = 0;
        try {

            stmt = con.prepareStatement(sql);
            stmt.setString(1, locDesc.getLocationDescription());
            stmt.setInt(2, locDesc.getBuildingFloorNo());
            stmt.executeUpdate();

            String retrievalQuery = "SELECT currval('locationdescription_id_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();

            while (rs.next()) {
                insertedLocDescID = rs.getInt(1);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate location description, sorry!", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return insertedLocDescID;
    }

    /**
     * TODO: Finish me
     * @param inspection
     * @return 
     */
    public List<OccLocationDescriptor> getLocationDescriptorsByInspection(FieldInspection inspection) {
        //

        return new ArrayList();
    }

    /**
     * Extracts a location descriptor from the DB
     * @param descriptorID
     * @return
     * @throws IntegrationException 
     */
    public OccLocationDescriptor getLocationDescriptor(int descriptorID) throws IntegrationException {
        String query_spaceIDs = "SELECT locationdescriptionid, description, buildingfloorno\n"
                + "  FROM public.occlocationdescriptor WHERE locationdescriptionid = ?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        OccLocationDescriptor loc = null;
        try {

            // this gets us a list of all of the spaces that have been inspected for this
            // occupancy inspection
            stmt = con.prepareStatement(query_spaceIDs);
            stmt.setInt(1, descriptorID);
            rs = stmt.executeQuery();

            // chugg down the list of spaceids and fetch an Inspected space for each
            while (rs.next()) {
                loc = new OccLocationDescriptor();
                loc.setLocationID(rs.getInt("locationdescriptionid"));
                loc.setLocationDescription(rs.getString("description"));
                loc.setBuildingFloorNo(rs.getInt("buildingfloorno"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to get location description, sorry!", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return loc;
    }

    /**
     * Updates a record in the occlocationdescriptor table
     * @param locDesc
     * @throws IntegrationException 
     */
    public void updateLocationDescriptor(OccLocationDescriptor locDesc) throws IntegrationException {
        String sql = "UPDATE public.occlocationdescriptor\n"
                + "   SET description=?, buildingfloorno=?\n"
                + " WHERE locationdescriptionid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {

            stmt = con.prepareStatement(sql);

            stmt.setString(1, locDesc.getLocationDescription());
            stmt.setInt(2, locDesc.getBuildingFloorNo());
            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate location description, sorry!", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
    }

    /**
     * Creates a record in the occinspectedspace table which connects a space type 
     * with an inspeection and records who added it and when. NOTE that I don't
     * write individual element data; use my siblings for that instead
     * 
     * @param spc
     * @param inspection
     * @return
     * @throws IntegrationException 
     */
    public OccInspectedSpace recordCommencementOfSpaceInspection(OccInspectedSpace spc, FieldInspection inspection)
            throws IntegrationException {

        String sql =    "INSERT INTO public.occinspectedspace(\n" +
                        "            inspectedspaceid, occinspection_inspectionid, occlocationdescription_descid, \n" +
                        "            addedtochecklistby_userid, addedtochecklistts, occchecklistspacetype_chklstspctypid)\n" +
                        "    VALUES (DEFAULT, ?, ?, \n" +
                        "            ?, now(), ?);";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        int insertedInspSpaceID = 0;
        try {
            stmt = con.prepareStatement(sql);
            
            stmt.setInt(1, inspection.getInspectionID());
            
            if (spc.getLocation() != null) {
                stmt.setInt(2, spc.getLocation().getLocationID());
            } else {
                stmt.setInt(2, Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString("locationdescriptor_implyfromspacename")));
            }
            
            if(spc.getAddedToChecklistBy() != null){
                stmt.setInt(3, spc.getAddedToChecklistBy().getUserID());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(spc.getType() != null){
                stmt.setInt(4, spc.getType().getChecklistSpaceTypeID());
            } else {
                throw new IntegrationException("Cannot inspect space with null type!");
            }
            
            
            stmt.execute();

            String retrievalQuery = "SELECT currval('occinspectedspace_pk_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();

            while (rs.next()) {
                insertedInspSpaceID = rs.getInt(1);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert newly inspected space, sorry!", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        spc.setInspectedSpaceID(insertedInspSpaceID);

        return spc;

    }

    /**
     * Inserts an OccInspectedSpace to the inspectedchecklistspaceelement table
     * which has not previously been inspected. The OccInspectedSpace object and
     * its composed elements do not have an ID number generated from the DB yet,
     * and these will come from the DB sequences ;
     *
     *
     * @param inspection the current FieldInspection
     * @param inspSpace an OccInspectedSpace that was NOT retrieved from the DB
     * @return the number of newly inserted space elements (mostly for info value only)
 passed as the second input parameter having been written to DB and added
 to the FieldInspection's internal List of inspected elements.
     * @throws IntegrationException
     */
    public int recordInspectionOfSpaceElements(OccInspectedSpace inspSpace, FieldInspection inspection) throws IntegrationException {
        int spaceElementInserts = 0;
        Iterator<OccInspectedSpaceElement> inspectedElementListIterator = inspSpace.getInspectedElementList().iterator();
        while (inspectedElementListIterator.hasNext()) {
            OccInspectedSpaceElement oie = inspectedElementListIterator.next();
            if (oie.getInspectedSpaceElementID() != 0) {
                updateInspectedSpaceElement(oie);
            } else {
                spaceElementInserts++;
                insertInspectedSpaceElement(oie, inspSpace);
            }
        }
        return spaceElementInserts;
    }
    
    /**
     * Massive creator of records in the all-important occinspectedspaceelement
     * table 
     * @param inspElement
     * @param inSpace
     * @throws IntegrationException 
     */
    private int insertInspectedSpaceElement(OccInspectedSpaceElement inspElement, OccInspectedSpace inSpace) throws IntegrationException{
        
        String sql =     "INSERT INTO public.occinspectedspaceelement(\n" +
                        "            inspectedspaceelementid, notes, locationdescription_id, lastinspectedby_userid, \n" +
                        "            lastinspectedts, compliancegrantedby_userid, compliancegrantedts, \n" +
                        "            inspectedspace_inspectedspaceid, overriderequiredflagnotinspected_userid, \n" +
                        "            occchecklistspacetypeelement_elementid, failureseverity_intensityclassid, \n" +
                        "            migratetocecaseonfail)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?, ?, \n" +
                        "            ?, ?, \n" +
                        "            ?);";
        
        // single row formatting of query_icse
        // SELECT DISTINCT space_id FROM checklistspaceelement INNER JOIN spaceelement ON (spaceelement_id = spaceelementid) WHERE checklist_id = 1;
        
        if(inspElement == null || inspElement.getInspectedSpaceElementID() != 0){
            throw new IntegrationException("Cannot insert inspected element with nonzero ID or null");
        }
        
        int newlyInspectedSpaceElement = 0;
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            // we have the parts we need for inserting into the inspectedchecklistspaceelement
            // for each inspected element, build and execute and insert
            stmt = con.prepareStatement(sql);

            stmt.setString(1, inspElement.getInspectionNotes());
            int locID;
            if (inSpace.getLocation() != null) {
                if (inSpace.getLocation().getLocationID() == 0) {
                    locID = insertLocationDescriptor(inSpace.getLocation());
                } else {
                    locID = inSpace.getLocation().getLocationID();
                }
            } else {
                locID = getLocationDescriptor(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString("locationdescriptor_implyfromspacename"))).getLocationID();
            }
            stmt.setInt(2, locID);
            if(inspElement.getLastInspectedBy() != null
                    && inspElement.getLastInspectedTS() != null){
                stmt.setInt(3, inspElement.getLastInspectedBy().getUserID());
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(inspElement.getLastInspectedTS()));
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
                stmt.setNull(4, java.sql.Types.NULL);
                
            }


            if(inspElement.getComplianceGrantedBy() != null 
                    && inspElement.getComplianceGrantedTS() != null){
                stmt.setInt(5, inspElement.getComplianceGrantedBy().getUserID());
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(inspElement.getComplianceGrantedTS()));
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
                stmt.setNull(6, java.sql.Types.NULL);
            }

            stmt.setInt(7, inSpace.getInspectedSpaceID());

            if(inspElement.getOverrideRequiredFlag_thisElementNotInspectedBy() != null){
                stmt.setInt(8, inspElement.getOverrideRequiredFlag_thisElementNotInspectedBy().getUserID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            
            stmt.setInt(9, inspElement.getOccChecklistSpaceTypeElementID());

            if(inspElement.getFaillureSeverity()!= null){
                stmt.setInt(10, inspElement.getFaillureSeverity().getClassID());
            } else {
                stmt.setNull(10,java.sql.Types.NULL);
            }
            stmt.setBoolean(11, inspElement.isMigrateToCaseOnFail());

            stmt.execute();
            
            System.out.println("OccInspectectionIntegrator.insertInspectedSpaceElements | inspectedElement inserted!");

            String retrievalQuery = "SELECT currval('inspectedspacetypeelement_inspectedstelid_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();

            while (rs.next()) {
                newlyInspectedSpaceElement = rs.getInt(1);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert newly inspected space into DB, sorry!", ex);

        } finally {
               if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return newlyInspectedSpaceElement;
    }

    /**
     * Updates a single record in the occinspectedspaceelement table
     * @param inspElement with members ready for extraction and insertion into DB
     * @throws IntegrationException 
     */
    public void updateInspectedSpaceElement(OccInspectedSpaceElement inspElement) throws IntegrationException {
        String sql = "UPDATE public.occinspectedspaceelement\n"
                + "   SET notes=?, lastinspectedby_userid=?, lastinspectedts=?, compliancegrantedby_userid=?, \n"
                + "       compliancegrantedts=?, failureseverity_intensityclassid=?, migratetocecaseonfail=? \n"
                + " WHERE inspectedspaceelementid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(sql);

            stmt.setString(1, inspElement.getInspectionNotes());

            if (inspElement.getLastInspectedBy() != null) {
                stmt.setInt(2, inspElement.getLastInspectedBy().getUserID());
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }

            if (inspElement.getLastInspectedTS() != null) {
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(inspElement.getLastInspectedTS()));
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            if (inspElement.getComplianceGrantedBy() != null) {
                stmt.setInt(4, inspElement.getComplianceGrantedBy().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }

            if (inspElement.getComplianceGrantedTS() != null) {
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(inspElement.getComplianceGrantedTS()));
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            if(inspElement.getFaillureSeverity()!= null){
                stmt.setInt(6, inspElement.getFaillureSeverity().getClassID());
            } else {
                stmt.setNull(6,java.sql.Types.NULL);
            }
            
            stmt.setBoolean(7, inspElement.isMigrateToCaseOnFail());

            stmt.setInt(8, inspElement.getInspectedSpaceElementID());

            stmt.executeUpdate();
            System.out.println("OccInspectionIntegrator.updatedInspectedCodeElement | completed updated on ineleid: " + inspElement.getInspectedSpaceElementID());
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update inspected space element in the database, sorry!", ex);

        } finally {
              if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
    }

    /**
     * For updating values on InspectedSpaces which have previously been
     * committed with recordInspectionOfSpaceElements.
     *
     * @param inspection
     * @param inspSpace
     * @throws IntegrationException thrown for standard SQL errors and this
     * method being given an OccInspectedSpace object whose constituent members
     * lack ID numbers for the updates.
     */
    public void updateInspectedSpace(FieldInspection inspection, OccInspectedSpace inspSpace) throws IntegrationException {

        String query = "UPDATE public.occinspectedspace\n"
                + "   SET occspace_spaceid=?, occinspection_inspectionid=?, \n"
                + "       occlocationdescription_descid=? \n"
                + " WHERE inspectedspaceid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, inspSpace.getInspectedSpaceID());
            stmt.setInt(2, inspection.getInspectionID());
            stmt.setInt(3, inspSpace.getLocation().getLocationID());
            stmt.setInt(4, inspSpace.getInspectedSpaceID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Could not update inspected space metadata", ex);

        } finally {
              if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
    }

    
    /**
     * Removes records from both the occinspectedspaceelement (first)
     * and then the parent occinspectedspace table
     * @param is
     * @throws IntegrationException 
     */
    public void deleteInspectedSpace(OccInspectedSpace is) throws IntegrationException {
        String sqlDeleteInsSpaceElement = "DELETE FROM occinspectedspaceelement WHERE inspectedspace_inspectedspaceid=?;";
        String sqlDeleteInsSpace = "DELETE FROM occinspectedspace WHERE inspectedspaceid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            // first remove inspected space elements
            stmt = con.prepareStatement(sqlDeleteInsSpaceElement);
            stmt.setInt(1, is.getInspectedSpaceID());
            stmt.execute();

            // then remove the inspected space
            stmt = con.prepareStatement(sqlDeleteInsSpace);
            stmt.setInt(1, is.getInspectedSpaceID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete inspected space!", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
    }

    /**
     * Deletes a single record in the occinspectedspaceelement table
     * @param ele
     * @throws IntegrationException 
     */
    public void deleteInspectedSpaceElement(OccInspectedSpaceElement ele) throws IntegrationException {
        String sqlDeleteInsSpaceElement = "DELETE FROM occinspectedspaceelement WHERE inspectedspaceelementid =?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            // first remove inspected space elements
            stmt = con.prepareStatement(sqlDeleteInsSpaceElement);
            stmt.setInt(1, ele.getInspectedSpaceElementID());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete inspected element!", ex);
        } finally {
              if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }

    /**
     * Builds an ID list of all inspections by OccPeriod or CECase
     * which have not been deactivated.
     * @param inspectable
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> getOccInspectionList(IFace_inspectable inspectable) throws IntegrationException, BObStatusException {

        List<Integer> inspecIDList = new ArrayList<>();
        
        if(inspectable == null){
            throw new BObStatusException("Cannot retrieve inspections from null inspectable");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT inspectionid FROM occinspection WHERE ");
        sb.append(inspectable.getDomainEnum().getDbField());
        sb.append("=? AND deactivatedts IS NULL;");
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, inspectable.getHostPK());
            rs = stmt.executeQuery();

            while (rs.next()) {
                inspecIDList.add(rs.getInt("inspectionid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrieve occinspectionlist", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return inspecIDList;

    }

    /**
     * Extracts IDs of inspections by pacc
     * @param pacc
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> getOccInspectionListByPACC(int pacc) throws IntegrationException {

        List<Integer> inspecList = new ArrayList<>();

        String query = "SELECT inspectionid FROM occinspection WHERE publicaccesscc=? ";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, pacc);
            rs = stmt.executeQuery();

            while (rs.next()) {
                inspecList.add(rs.getInt("inspectionid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrieve occinspectionlist", ex);

        } finally {
              if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return inspecList;

    }
    
    /**
     * Extracts a single occ inspection from the DB; Note the Coordinator injects it with
     * all sorts of lists, most notably the occinspectedspace list which contains the elements
     * and their inspection status
     * @param inspectionID
     * @return
     * @throws IntegrationException 
     */
    public FieldInspection getOccInspection(int inspectionID) throws IntegrationException {
        OccupancyCoordinator oc = getOccupancyCoordinator();

        FieldInspection inspection = null;

        String query = " SELECT inspectionid, occperiod_periodid, inspector_userid, publicaccesscc, \n"
                + "       enablepacc, notespreinspection, thirdpartyinspector_personid, thirdpartyinspectorapprovalts, \n"
                + "       thirdpartyinspectorapprovalby, maxoccupantsallowed, numbedrooms, numbathrooms, \n"
                + "       occchecklist_checklistlistid, effectivedate, createdts, followupto_inspectionid, \n"
                + "       deactivatedts, deactivatedby_userid, timestart, timeend, \n"
                + "       createdby_userid, lastupdatedts, lastupdatedby_userid, determination_detid, \n"
                + "       determinationby_userid, determinationts, remarks, generalcomments, \n"
                + "       cause_causeid, cecase_caseid \n"
                + "  FROM public.occinspection WHERE inspectionid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, inspectionID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                inspection = generateOccInspection(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get space type", ex);

        } finally {
              if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return inspection;
    }

    /**
     * Generator of FieldInspection objects
     * @param rs
     * @return
     * @throws IntegrationException
     * @throws SQLException 
     */
    private FieldInspection generateOccInspection(ResultSet rs) throws IntegrationException, SQLException {
            FieldInspection ins = new FieldInspection();
            UserCoordinator uc = getUserCoordinator();
            PersonCoordinator pc = getPersonCoordinator();
        try {
            
            ins.setInspectionID(rs.getInt("inspectionid"));
            ins.setOccPeriodID(rs.getInt("occperiod_periodid"));
            ins.setCecaseID(rs.getInt("cecase_caseid"));
            ins.setInspector(uc.user_getUser(rs.getInt("inspector_userid")));
            ins.setPacc(rs.getInt("publicaccesscc"));
            
            ins.setEnablePacc(rs.getBoolean("enablepacc"));
            ins.setNotesPreInspection(rs.getString("notespreinspection"));
            if (rs.getInt("thirdpartyinspector_personid") != 0) {
                ins.setThirdPartyInspector(pc.getPerson(pc.getHuman(rs.getInt("thirdpartyinspector_personid"))));
            }
            if (rs.getTimestamp("thirdpartyinspectorapprovalts") != null) {
                ins.setThirdPartyInspectorApprovalTS(rs.getTimestamp("thirdpartyinspectorapprovalts").toLocalDateTime());
            }
            
            if (rs.getInt("thirdpartyinspectorapprovalby") != 0) {
                ins.setThirdPartyApprovalBy(uc.user_getUser(rs.getInt("thirdpartyinspectorapprovalby")));
            }
            ins.setMaxOccupantsAllowed(rs.getInt("maxoccupantsallowed"));
            ins.setNumBedrooms(rs.getInt("numbedrooms"));
            ins.setNumBathrooms(rs.getInt("numbathrooms"));
            
            ins.setChecklistTemplateID(rs.getInt("occchecklist_checklistlistid"));
            
            
            if (rs.getTimestamp("effectivedate") != null) {
                ins.setEffectiveDateOfRecord(rs.getTimestamp("effectivedate").toLocalDateTime());
            }
            if (rs.getTimestamp("createdts") != null) {
                ins.setCreationTS(rs.getTimestamp("createdts").toLocalDateTime());
            }
            ins.setFollowUpToInspectionID(rs.getInt("followupto_inspectionid"));
            
            if (rs.getTimestamp("deactivatedts") != null) {
                ins.setDeactivatedTS(rs.getTimestamp("deactivatedts").toLocalDateTime());
            }
            if (rs.getInt("deactivatedby_userid") != 0) {
                ins.setDeactivatedBy(uc.user_getUser(rs.getInt("deactivatedby_userid")));
            }
            if (rs.getTimestamp("timestart") != null) {
                ins.setTimeStart(rs.getTimestamp("timestart").toLocalDateTime());
            }
            if (rs.getTimestamp("timeend") != null) {
                ins.setTimeEnd(rs.getTimestamp("timeend").toLocalDateTime());
            }
            
            if (rs.getInt("createdby_userid") != 0) {
                ins.setCreatedBy(uc.user_getUser(rs.getInt("createdby_userid")));
            }
            if (rs.getTimestamp("lastupdatedts") != null) {
                ins.setLastUpdatedTS(rs.getTimestamp("lastupdatedts").toLocalDateTime());
            }
            if (rs.getInt("lastupdatedby_userid") != 0) {
                ins.setLastUpdatedBy(uc.user_getUser(rs.getInt("lastupdatedby_userid")));
            }
            if (rs.getInt("determination_detid") != 0) {
                ins.setDetermination(getDetermination(rs.getInt("determination_detid")));
            }
            
            if (rs.getInt("determinationby_userid") != 0) {
                ins.setLastUpdatedBy(uc.user_getUser(rs.getInt("determinationby_userid")));
            }
            if (rs.getTimestamp("determinationts") != null) {
                ins.setDeterminationTS(rs.getTimestamp("determinationts").toLocalDateTime());
            }
            ins.setRemarks(rs.getString("remarks"));
            ins.setGeneralComments(rs.getString("generalcomments"));
            
            if (rs.getInt("cause_causeid") != 0) {
                ins.setCause(getCause(rs.getInt("cause_causeid")));
            }
            
        } catch (BObStatusException ex) {
            System.out.println(ex);
        }
        return ins;
    }

    /**
     * Updates a single record in the occinspection table
     * @param occInsp
     * @throws IntegrationException 
     */
    public void updateOccInspection(FieldInspection occInsp) throws IntegrationException {
        String sql = "UPDATE public.occinspection\n"
                + "   SET inspector_userid=?, publicaccesscc=?, \n"
                + "       enablepacc=?, notespreinspection=?, thirdpartyinspector_personid=?, thirdpartyinspectorapprovalts=?, \n"
                + "       thirdpartyinspectorapprovalby=?, maxoccupantsallowed=?, numbedrooms=?, numbathrooms=?, \n"
                + "       occchecklist_checklistlistid=?, effectivedate=?, followupto_inspectionid=?, \n"
                + "       deactivatedts=?, deactivatedby_userid=?, timestart=?, timeend=?, \n"
                + "       lastupdatedts=now(), lastupdatedby_userid=?, determination_detid=?, determinationby_userid=?, \n"
                + "       determinationts=?, remarks=?, generalcomments=?, cause_causeid=?\n"
                + " WHERE inspectionid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sql);
            if(occInsp.getInspector() != null){
                stmt.setInt(1, occInsp.getInspector().getUserID());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            stmt.setInt(2, occInsp.getPacc());

            stmt.setBoolean(3, occInsp.isEnablePacc());
            stmt.setString(4, occInsp.getNotesPreInspection());
            if (occInsp.getThirdPartyInspector() != null) {
                stmt.setInt(5, occInsp.getThirdPartyInspector().getPersonID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            if (occInsp.getThirdPartyInspectorApprovalTS() != null) {
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(occInsp.getThirdPartyInspectorApprovalTS()));
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }

            if (occInsp.getThirdPartyApprovalBy() != null) {
                stmt.setInt(7, occInsp.getThirdPartyApprovalBy().getUserID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            stmt.setInt(8, occInsp.getMaxOccupantsAllowed());
            stmt.setInt(9, occInsp.getNumBedrooms());
            stmt.setInt(10, occInsp.getNumBathrooms());

            if (occInsp.getChecklistTemplate() != null) {
                stmt.setInt(11, occInsp.getChecklistTemplate().getInspectionChecklistID());
            } else {
                stmt.setNull(11, java.sql.Types.NULL);
            }
            if (occInsp.getEffectiveDateOfRecord() != null) {
                stmt.setTimestamp(12, java.sql.Timestamp.valueOf(occInsp.getEffectiveDateOfRecord()));
            } else {
                stmt.setNull(12, java.sql.Types.NULL);
            }
            if (occInsp.getFollowUpToInspectionID() != 0) {
                stmt.setInt(13, occInsp.getFollowUpToInspectionID());
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }

            if (occInsp.getDeactivatedTS() != null) {
                stmt.setTimestamp(14, java.sql.Timestamp.valueOf(occInsp.getDeactivatedTS()));
            } else {
                stmt.setNull(14, java.sql.Types.NULL);
            }
            if (occInsp.getDeactivatedBy() != null) {
                stmt.setInt(15, occInsp.getDeactivatedBy().getUserID());
            } else {
                stmt.setNull(15, java.sql.Types.NULL);
            }
            if (occInsp.getTimeStart() != null) {
                stmt.setTimestamp(16, java.sql.Timestamp.valueOf(occInsp.getTimeStart()));
            } else {
                stmt.setNull(16, java.sql.Types.NULL);
            }
            if (occInsp.getTimeEnd() != null) {
                stmt.setTimestamp(17, java.sql.Timestamp.valueOf(occInsp.getTimeEnd()));
            } else {
                stmt.setNull(17, java.sql.Types.NULL);
            }

           
            if (occInsp.getLastUpdatedBy() != null) {
                stmt.setInt(18, occInsp.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(18, java.sql.Types.NULL);
            }
            if (occInsp.getDetermination() != null) {
                stmt.setInt(19, occInsp.getDetermination().getDeterminationID());
            } else {
                stmt.setNull(19, java.sql.Types.NULL);
            }
            if (occInsp.getDeterminationBy() != null) {
                stmt.setInt(20, occInsp.getDeterminationBy().getUserID());
            } else {
                stmt.setNull(20, java.sql.Types.NULL);
            }

            if (occInsp.getDeterminationTS() != null) {
                stmt.setTimestamp(21, java.sql.Timestamp.valueOf(occInsp.getDeterminationTS()));
            } else {
                stmt.setNull(21, java.sql.Types.NULL);
            }
            stmt.setString(22, occInsp.getRemarks());
            stmt.setString(23, occInsp.getGeneralComments());
            if (occInsp.getCause() != null) {
                stmt.setInt(24, occInsp.getCause().getCauseID());
            } else {
                stmt.setNull(24, java.sql.Types.NULL);
            }

            stmt.setInt(25, occInsp.getInspectionID());

            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update occupancy inspection record", ex);
        } finally {
              if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
    }

    /**
     * Sets the deactivation fields on the given FieldInspection
     * @param ins
     * @throws IntegrationException 
     */
    public void deactivateOccInspection(FieldInspection ins) throws IntegrationException {

        String query = "UPDATE occinspection SET deactivatedts=now() AND deactivatedby_userid=? WHERE inspectionid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            if(ins.getDeactivatedBy() != null){
                stmt.setInt(1, ins.getDeactivatedBy().getUserID());
            } else {
                throw new IntegrationException("Cannot deactivate occ inspection with null deac by User");
            }
            stmt.setInt(2, ins.getInspectionID());
                    
            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot mark inspection as deactivated, sorries!", ex);
        } finally {
              if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             

        } // close finally
    }

   
    /**
     * Creates a new record in the occinspection table
     * @param occInsp
     * @return
     * @throws IntegrationException 
     */
    public int insertOccInspection(FieldInspection occInsp) throws IntegrationException {
        String query = "INSERT INTO public.occinspection(\n"
                + "            inspectionid, occperiod_periodid, inspector_userid, publicaccesscc, \n"
                + "            enablepacc, notespreinspection, thirdpartyinspector_personid, thirdpartyinspectorapprovalts, \n"
                + "            thirdpartyinspectorapprovalby, maxoccupantsallowed, numbedrooms, numbathrooms, \n"
                + "            occchecklist_checklistlistid, effectivedate, createdts, followupto_inspectionid, \n"
                + "            deactivatedts, deactivatedby_userid, timestart, timeend, \n"
                + "            createdby_userid, lastupdatedts, lastupdatedby_userid, determination_detid, \n"
                + "            determinationby_userid, determinationts, remarks, generalcomments, \n"
                + "            cause_causeid, cecase_caseid)\n"
                + "    VALUES (DEFAULT, ?, ?, ?, \n"
                + "            ?, ?, ?, ?, \n"
                + "            ?, ?, ?, ?, \n"
                + "            ?, ?, now(), ?, \n"
                + "            ?, ?, ?, ?, \n"
                + "            ?, ?, ?, ?, \n"
                + "            ?, ?, ?, ?, \n"
                + "            ?, ?);";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        int newInspectionID = 0;
        try {
            stmt = con.prepareStatement(query);
            if(occInsp.getDomainEnum() == null){
                throw new IntegrationException("cannot write inspection with null domain");
            }
            switch(occInsp.getDomainEnum()){
                case OCCUPANCY:
                    stmt.setInt(1, occInsp.getOccPeriodID());
                    stmt.setNull(28, java.sql.Types.NULL);
                    break;
                case CODE_ENFORCEMENT:
                    stmt.setInt(28, occInsp.getCecaseID());
                    stmt.setNull(1, java.sql.Types.NULL);
                    break;
            }
            stmt.setInt(2, occInsp.getInspector().getUserID());
            stmt.setInt(3, occInsp.getPacc());

            stmt.setBoolean(4, occInsp.isEnablePacc());
            stmt.setString(5, occInsp.getNotesPreInspection());
            if (occInsp.getThirdPartyInspector() != null) {
                stmt.setInt(6, occInsp.getThirdPartyInspector().getPersonID());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            if (occInsp.getThirdPartyInspectorApprovalTS() != null) {
                stmt.setTimestamp(7, java.sql.Timestamp.valueOf(occInsp.getThirdPartyInspectorApprovalTS()));
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }

            if (occInsp.getThirdPartyApprovalBy() != null) {
                stmt.setInt(8, occInsp.getThirdPartyApprovalBy().getUserID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            stmt.setInt(9, occInsp.getMaxOccupantsAllowed());
            stmt.setInt(10, occInsp.getNumBedrooms());
            stmt.setInt(11, occInsp.getNumBathrooms());
            
            if (occInsp.getChecklistTemplate() != null) {
                stmt.setInt(12, occInsp.getChecklistTemplate().getInspectionChecklistID());
            } else {
                stmt.setNull(12, java.sql.Types.NULL);
            }
            if (occInsp.getEffectiveDateOfRecord() != null) {
                stmt.setTimestamp(13, java.sql.Timestamp.valueOf(occInsp.getEffectiveDateOfRecord()));
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }
            if (occInsp.getFollowUpToInspectionID() != 0) {
                stmt.setInt(14, occInsp.getFollowUpToInspectionID());
            } else {
                stmt.setNull(14, java.sql.Types.NULL);
            }
            if (occInsp.getDeactivatedTS() != null) {
                stmt.setTimestamp(15, java.sql.Timestamp.valueOf(occInsp.getDeactivatedTS()));
            } else {
                stmt.setNull(15, java.sql.Types.NULL);
            }
            if (occInsp.getDeactivatedBy() != null) {
                stmt.setInt(16, occInsp.getDeactivatedBy().getUserID());
            } else {
                stmt.setNull(16, java.sql.Types.NULL);
            }
            if (occInsp.getTimeStart() != null) {
                stmt.setTimestamp(17, java.sql.Timestamp.valueOf(occInsp.getTimeStart()));
            } else {
                stmt.setNull(17, java.sql.Types.NULL);
            }
            if (occInsp.getTimeEnd() != null) {
                stmt.setTimestamp(18, java.sql.Timestamp.valueOf(occInsp.getTimeEnd()));
            } else {
                stmt.setNull(18, java.sql.Types.NULL);
            }

            if (occInsp.getCreatedBy() != null) {
                stmt.setInt(19, occInsp.getCreatedBy().getUserID());
            } else {
                stmt.setNull(19, java.sql.Types.NULL);
            }
            if (occInsp.getLastUpdatedTS() != null) {
                stmt.setTimestamp(20, java.sql.Timestamp.valueOf(occInsp.getLastUpdatedTS()));
            } else {
                stmt.setNull(20, java.sql.Types.NULL);
            }
            if (occInsp.getLastUpdatedBy() != null) {
                stmt.setInt(21, occInsp.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(21, java.sql.Types.NULL);
            }
            if (occInsp.getDetermination() != null) {
                stmt.setInt(22, occInsp.getDetermination().getDeterminationID());
            } else {
                stmt.setNull(22, java.sql.Types.NULL);
            }

            if (occInsp.getDeterminationBy() != null) {
                stmt.setInt(23, occInsp.getDeterminationBy().getUserID());
            } else {
                stmt.setNull(23, java.sql.Types.NULL);
            }
            if (occInsp.getDeterminationTS() != null) {
                stmt.setTimestamp(24, java.sql.Timestamp.valueOf(occInsp.getDeterminationTS()));
            } else {
                stmt.setNull(24, java.sql.Types.NULL);
            }
            stmt.setString(25, occInsp.getRemarks());
            stmt.setString(26, occInsp.getGeneralComments());

            if (occInsp.getCause() != null) {
                stmt.setInt(27, occInsp.getCause().getCauseID());
            } else {
                stmt.setNull(27, java.sql.Types.NULL);
            }

            stmt.execute();
            String retrievalQuery = "SELECT currval('occupancyinspectionid_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();

            while (rs.next()) {
                newInspectionID = rs.getInt(1);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert OccupancyInspection", ex);
        } finally {
              if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        
        return newInspectionID;
    }
    
    /**
     * Looks for any dispatches by inspection
     * @param fin
     * @return the ID of at maximum one dipsatch; 0 for no dispatches
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public int getOccInspectionDispatchByInspection(FieldInspection fin) throws IntegrationException{
        if(fin == null){
            throw new IntegrationException("Cannot fetch dispatches by null field inspection");
        }
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT dispatchid FROM occinspectiondispatch WHERE inspection_inspectionid=? AND deactivatedts IS NULL;");
        int did = 0;

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, fin.getInspectionID());

            rs = stmt.executeQuery();
            while (rs.next()) {
                did = rs.getInt("dispatchid");
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable find dispatches by inspection", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return did;
    }
    
    /**
     * Extracts a dispatch from the DB by ID
     * @param dispatchID
     * @return the fully-baked inspection dispatch
     */
    public OccInspectionDispatch getOccInspectionDispatch(int dispatchID) throws IntegrationException, BObStatusException{
        if(dispatchID == 0){
            return null;
        }
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT dispatchid, createdby_userid, createdts, dispatchnotes, "
                + "inspection_inspectionid, retrievalts, retrievedby_userid, synchronizationts, synchronizationnotes, "
                + "deactivatedts, deactivatedby_userid, lastupdatedts, lastupdatedby_userid\n" 
                + "	FROM public.occinspectiondispatch WHERE dispatchid=?;");
        OccInspectionDispatch dispatch = null;

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, dispatchID);

            rs = stmt.executeQuery();
            while (rs.next()) {
                dispatch = generateOccInspectionDispatch(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to generate dispatch", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return dispatch;
        
    }
    
    /**
     * Internal generator for dispatches
     * @param rs
     * @return 
     */
    private OccInspectionDispatch generateOccInspectionDispatch(ResultSet rs) throws SQLException, IntegrationException, BObStatusException{
        if(rs == null){
            return null;
        }
        
        UserCoordinator uc = getUserCoordinator();
        SystemIntegrator si = getSystemIntegrator();
        
        OccInspectionDispatch dispatch = new OccInspectionDispatch();
        dispatch.setDispatchID(rs.getInt("dispatchid"));
        dispatch.setDispatchNotes(rs.getString("dispatchnotes"));
        dispatch.setInspectionID(rs.getInt("inspection_inspectionid"));
        if(rs.getTimestamp("retrievalts") != null){
            dispatch.setRetrievalTS(rs.getTimestamp("retrievalts").toLocalDateTime());
        }
        if(rs.getInt("retrievedby_userid") != 0){
            dispatch.setRetrievedBy(uc.user_getUser(rs.getInt("retrievedby_userid")));   
        }
        if(rs.getTimestamp("synchronizationts") != null){
            dispatch.setSynchronizationTS(rs.getTimestamp("synchronizationts").toLocalDateTime());
        }
        dispatch.setSynchronizationNotes(rs.getString("synchronizationnotes"));
        si.populateTrackedFields(dispatch, rs, false);
        return dispatch;
        
    }

    
    /**
     * Writes a given dispatch to the DB
     * @param oid
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public int insertOccInspectionDispatch(OccInspectionDispatch oid) throws IntegrationException{
        if(oid == null){
            throw new IntegrationException("Cannot insert a null dispatch");
        }
        
        String query = "INSERT INTO public.occinspectiondispatch(\n" +
                        "	dispatchid, createdby_userid, createdts, dispatchnotes, \n" +
                        "	inspection_inspectionid, retrievalts, retrievedby_userid, \n" +
                        "	synchronizationts, synchronizationnotes, deactivatedts, deactivatedby_userid, \n" +
                        "	lastupdatedts, lastupdatedby_userid)\n" +
                        "      VALUES (DEFAULT, ?, now(), ?, "
                                    + "?, ?, ?, "
                                    + "?, ?, NULL, NULL, "
                                    + "now(), ?);";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        int freshDispatchID = 0;
        try {
            stmt = con.prepareStatement(query);
      
            if(oid.getCreatedBy() != null){
                stmt.setInt(1, oid.getCreatedBy().getCreatedByUserId());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            stmt.setString(2, oid.getDispatchNotes());

            stmt.setInt(3, oid.getInspectionID());
            if(oid.getRetrievalTS() != null){
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(oid.getRetrievalTS()));
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            if(oid.getRetrievedBy()!= null){
                stmt.setInt(5, oid.getRetrievedBy().getUserID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }

            if(oid.getSynchronizationTS() != null){
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(oid.getSynchronizationTS()));
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            stmt.setString(7, oid.getSynchronizationNotes());
            if(oid.getLastUpdatedBy() != null){
                stmt.setInt(8, oid.getLastUpdatedByUserID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            
            stmt.execute();
            
            String retrievalQuery = "SELECT currval('occinspectiondispatch_dispatchid_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();

            while (rs.next()) {
                freshDispatchID = rs.getInt(1);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert OccInspectionDispatch", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        
        return freshDispatchID;
        
    }
    
    /**
     * Updates a record of the occinspectiondispatch table
     * @param oid
     * @throws IntegrationException 
     */
    public void updateOccInspectionDispatch(OccInspectionDispatch oid) throws IntegrationException{
        if(oid == null){
            throw new IntegrationException("Cannot update a null dispatch");
        }
        
        String query = "UPDATE public.occinspectiondispatch\n" +
                        "	SET dispatchnotes=?, inspection_inspectionid=?, \n" +
                        "	retrievalts=?, retrievedby_userid=?, synchronizationts=?, \n" +
                        "	synchronizationnotes=?, \n" +
                        "	deactivatedts=?, deactivatedby_userid=?, lastupdatedts=now(), \n" +
                        "	lastupdatedby_userid=?\n" +
                        "	WHERE dispatchid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
      
            stmt.setString(1, oid.getDispatchNotes());

            stmt.setInt(2, oid.getInspectionID());
            if(oid.getRetrievalTS() != null){
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(oid.getRetrievalTS()));
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            if(oid.getRetrievedBy()!= null){
                stmt.setInt(4, oid.getRetrievedBy().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }

            if(oid.getSynchronizationTS() != null){
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(oid.getSynchronizationTS()));
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            stmt.setString(6, oid.getSynchronizationNotes());

            if(oid.getDeactivatedTS() != null){
                stmt.setTimestamp(7, java.sql.Timestamp.valueOf(oid.getDeactivatedTS()));
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            if(oid.getDeactivatedBy() != null){
                stmt.setInt(8, oid.getDeactivatedByUserID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            
            if(oid.getLastUpdatedBy() != null){
                stmt.setInt(9, oid.getLastUpdatedByUserID());
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            stmt.setInt(10, oid.getDispatchID());
            
            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot update OccInspectionDispatch", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        
        
    }
  
    /**
     * See if a determination has been used in the DB and if not, it can be deleted
     * @param d
     * @return
     * @throws IntegrationException 
     */
    public int determinationCheckForUse(OccInspectionDetermination d) throws IntegrationException {
        SystemIntegrator si = getSystemIntegrator();
        int uses = 0;
        List<String> useTables = si.findForeignUseTables("determination");
        for(int x = 0; x < useTables.size(); x++){
            uses =+ si.checkForUse("public." + useTables.get(x), "determination_detid", d.getDeterminationID());
            System.out.println("Checked public." + useTables.get(x) + " for  determination_detid" + d.getDeterminationID());
        }
        return uses;
    }
    
    /**
     * Create a determination object from the db
     * @param determinationID
     * @return
     * @throws IntegrationException 
     */
    public OccInspectionDetermination getDetermination(int determinationID) throws IntegrationException {
        EventIntegrator ei = new EventIntegrator();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT determinationid, title, description, notes, eventcat_catid, active, qualifiesaspassed ");
        sb.append("FROM public.occinspectiondetermination WHERE determinationid=?;");
        OccInspectionDetermination d = null;

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, determinationID);

            rs = stmt.executeQuery();
            while (rs.next()) {

                d = new OccInspectionDetermination();
                d.setDeterminationID(rs.getInt("determinationid"));
                d.setTitle(rs.getString("title"));
                d.setDescription(rs.getString("description"));
                d.setNotes(rs.getString("notes"));
                if (rs.getInt("eventcat_catid") != 0) {
                    d.setEventCategory(ei.getEventCategory(rs.getInt("eventcat_catid")));
                }
                d.setActive(rs.getBoolean("active"));
                d.setQualifiesAsPassed(rs.getBoolean("qualifiesaspassed"));

            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to generate determination", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return d;
    }

    /**
     * Deactivates an occ inspection determination 
     * @param d
     * @throws IntegrationException 
     */
    public void deactivateDetermination(OccInspectionDetermination d) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE public.occinspectiondetermination SET active=false ");
        sb.append("WHERE determinationid=?");
        
        try{
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, d.getDeterminationID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to deactivate determination", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }


    public void updateDetermination(OccInspectionDetermination d) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE public.occinspectiondetermination SET title=?, description=?, notes=?, eventcat_catid=?, active=?, qualifiesaspassed=? ");
        sb.append("WHERE determinationid = ?;");

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setString(1, d.getTitle());
            stmt.setString(2, d.getDescription());
            stmt.setString(3, d.getNotes());
            stmt.setInt(4, d.getEventCategory().getCategoryID());
            stmt.setBoolean(5, d.isActive());
            stmt.setBoolean(6, d.isQualifiesAsPassed());
            stmt.setInt(7, d.getDeterminationID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to update determination", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
    }

    public void insertDetermination(OccInspectionDetermination d) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO public.occinspectiondetermination(");
        sb.append("determinationid, title, description, notes, eventcat_catid, active, qualifiesaspassed) ");
        sb.append("VALUES (DEFAULT, ?, ?, ?, ?, ?, ?);");

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setString(1, d.getTitle());
            stmt.setString(2, d.getDescription());
            stmt.setString(3, d.getNotes());
            stmt.setInt(4, d.getEventCategory().getCategoryID());
            stmt.setBoolean(5, true);
            stmt.setBoolean(6, d.isQualifiesAsPassed());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to insert determination", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
    }

   
    public List<OccInspectionDetermination> getDeterminationList() throws IntegrationException {

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT determinationid FROM public.occinspectiondetermination WHERE active=true;");
        List<OccInspectionDetermination> detList = new ArrayList<>();
        try {
            stmt = con.prepareStatement(sb.toString());
            rs = stmt.executeQuery();
            while (rs.next()) {
                detList.add(getDetermination(rs.getInt("determinationid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate OccInspectionDetermination(List)", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return detList;
    }
        
    public int requirementCheckForUse(OccInspectionRequirement r) throws IntegrationException {
        SystemIntegrator si = getSystemIntegrator();
        int uses = 0;
        List<String> useTables = si.findForeignUseTables("requirementid");
        for(int x = 0; x < useTables.size(); x++){
            uses =+ si.checkForUse("public." + useTables.get(x), "requirement_requirementid", r.getRequirementID());
            System.out.println("Checked public." + useTables.get(x) + " for  requirement_requirementid" + r.getRequirementID());
        };
        return uses;
    }
    
    public OccInspectionRequirement getRequirement(int requirementID) throws IntegrationException {
        EventIntegrator ei = new EventIntegrator();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT requirementid, title, description, active ");
        sb.append("FROM public.occinspectionrequirement WHERE requirementid=?;");
        OccInspectionRequirement r = null;

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, requirementID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                r = new OccInspectionRequirement();
                r.setRequirementID(rs.getInt("requirementid"));
                r.setTitle(rs.getString("title"));
                r.setDescription(rs.getString("description"));
                r.setActive(rs.getBoolean("active"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to generate requirement", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return r;
    }

    public void deactivateRequirement(OccInspectionRequirement r) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE public.occinspectionrequirement SET active=false ");
        sb.append("WHERE requirementid=?");
        
        try{
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, r.getRequirementID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to deactivate requirement", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }

    public void updateRequirement(OccInspectionRequirement r) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE public.occinspectionrequirement SET title=?, description=?, active=? ");
        sb.append("WHERE requirementid = ?;");

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setString(1, r.getTitle());
            stmt.setString(2, r.getDescription());
            stmt.setBoolean(3, r.isActive());
            stmt.setInt(4, r.getRequirementID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to update requirement", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
    }


    public void insertRequirement(OccInspectionRequirement r) throws IntegrationException {

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO public.occinspectionrequirement(");
        sb.append("requirementid, title, description, active) ");
        sb.append("VALUES (DEFAULT, ?, ?, ?);");

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setString(1, r.getTitle());
            stmt.setString(2, r.getDescription());
            stmt.setBoolean(3, true);
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to insert requirement", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
    }

   
    public List<OccInspectionRequirement> getRequirementList() throws IntegrationException {

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT requirementid FROM public.occinspectionrequirement WHERE active=true;");
        List<OccInspectionRequirement> reqList = new ArrayList<>();
        try {
            stmt = con.prepareStatement(sb.toString());
            rs = stmt.executeQuery();
            while (rs.next()) {
                reqList.add(getRequirement(rs.getInt("requirementid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate OccInspectionRequirement(List)", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return reqList;
    }
    
    public List<OccInspectionRequirementAssigned> getOccRequirementAssignedList(FieldInspection inspection) 
            throws IntegrationException, BObStatusException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT occrequirement_requirementid FROM public.occinspectionrequirementassigned ");
        sb.append("WHERE occinspection_inspectionid = ?;");
        List<OccInspectionRequirementAssigned> assignedList = new ArrayList<>();
        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, inspection.getInspectionID());
            rs = stmt.executeQuery();
            while(rs.next()){
                assignedList.add(getOccRequirementAssigned(inspection.getInspectionID(), rs.getInt("occrequirement_requirementid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get OccRequiremetnAssignedList");
            
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
        //query the assigned table by inspection number
        //
        
        
        
        
        
        
        
        return assignedList;
    }
    
    public OccInspectionRequirementAssigned getOccRequirementAssigned(int inspectionID, int requirementID) 
            throws IntegrationException, BObStatusException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM public.occinspectionrequirementassigned ");
        sb.append("WHERE occinspection_inspectionid = ? ");
        sb.append("AND occrequirement_requirementid = ?;");
        OccInspectionRequirementAssigned reqAssigned = null;
        
        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, inspectionID);
            rs = stmt.executeQuery();
            reqAssigned = generateOccRequirementAssigned(rs);
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot get occ requirement assigned");
            
        
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
        
        
        
        
        
        
        
        return reqAssigned;
    }
    
    private OccInspectionRequirementAssigned generateOccRequirementAssigned(ResultSet rs) throws BObStatusException, IntegrationException{
        OccInspectionRequirementAssigned reqAssigned = null;
        if(rs == null){
            return reqAssigned;
        }
        UserCoordinator uc = getUserCoordinator();
        try {            
            //check for null rs
            //if not null make a new instance of OccInspectionRequirementAssigned
            //in constructor call get requirement and pass in requirementid
            reqAssigned = new OccInspectionRequirementAssigned(getRequirement(rs.getInt("occrequirement_requirementid")));
            reqAssigned.setInspectionID(rs.getInt("occinspection_inspectionid"));
            
            reqAssigned.setAssignedBy(uc.user_getUser(rs.getInt("assignedby")));
            reqAssigned.setAssignedDate(rs.getTimestamp("assigneddate").toLocalDateTime());
            reqAssigned.setAssignedNotes(rs.getString("assignednotes"));
            
            if(rs.getTimestamp("fulfilleddate") != null){
                reqAssigned.setFulfilledBy(uc.user_getUser(rs.getInt("fulfilledby")));
                reqAssigned.setFulfilledDate(rs.getTimestamp("fulfilleddate").toLocalDateTime());
                reqAssigned.setFulfilledNotes(rs.getString("fulfillednotes"));
            }
            
            reqAssigned.setNotes(rs.getString("notes"));
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("cannot generate occrequirementassigned");
            
            
        }
        
        
        return reqAssigned;
    }

    /**
     * Extracts and builds an OccInspectionCause object from the DB
     * @param causeID
     * @return
     * @throws IntegrationException 
     */
    public OccInspectionCause getCause(int causeID) throws IntegrationException {
        OccInspectionCause cause = null;

        String query = " SELECT causeid, title, description, notes, active \n"
                + "  FROM public.occinspectioncause WHERE causeid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, causeID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                cause = generateCause(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get OccInspectionCause", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return cause;
    }
    
       /**
     * Retrieves a list of IDs of all active OccInspectionCause records
     * @return the IDs of the occ inspection causes to fetch
     */
    public List<Integer> getCauseListActiveOnly() throws IntegrationException{
         String query = " SELECT causeid \n"
                + "  FROM public.occinspectioncause WHERE active = TRUE;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<Integer> cidl = new ArrayList<>();

        try {

            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                cidl.add(rs.getInt("causeid"));
                               
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get occ inspection cause list", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return cidl;
        
        
    }

    /**
     * Generator of OccInspectionCause objects
     * @param rs
     * @return
     * @throws SQLException 
     */
    private OccInspectionCause generateCause(ResultSet rs) throws SQLException {
        OccInspectionCause cause = new OccInspectionCause();

        cause.setCauseID(rs.getInt("causeid"));

        cause.setTitle(rs.getString("title"));
        cause.setDescription(rs.getString("description"));

        cause.setNotes(rs.getString("notes"));

        cause.setActive(rs.getBoolean("active"));

        return cause;
    }

    /**
     * Updates a record in the occinspectioncause table
     * @param cause
     * @throws IntegrationException 
     */
    public void updateCause(OccInspectionCause cause) throws IntegrationException {
        String sql = "UPDATE public.occinspectioncause\n"
                + "   SET title=?, description=?, notes=?, active=? \n"
                + " WHERE causeid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setString(1, cause.getTitle());
            stmt.setString(2, cause.getDescription());

            stmt.setString(3, cause.getNotes());

            stmt.setBoolean(4, cause.isActive());

            stmt.setInt(5, cause.getCauseID());

            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update occinspectioncause record", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
    }

    /**
     * Removes a record from the occinspectioncause table
     * @param cause
     * @throws IntegrationException 
     */
    public void deleteCause(OccInspectionCause cause) throws IntegrationException {
        String query = "DELETE FROM public.occinspectioncause\n" + " WHERE causeid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cause.getCauseID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete occ inspection cause--probably because another" + "part of the database has a reference item.", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }

        } // close finally
    }

    /**
     * Creates a new record in the occinspectioncause table
     * @param cause
     * @return
     * @throws IntegrationException 
     */
    public OccInspectionCause insertCause(OccInspectionCause cause) throws IntegrationException {
        String query = "INSERT INTO public.occinspectioncause(\n"
                + "            causeid, title, description, notes, active)\n"
                + "    VALUES (DEFAULT, ?, ?, ?, ?);";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        int newCauseID = 0;
        try {
            stmt = con.prepareStatement(query);

            stmt.setString(1, cause.getTitle());
            stmt.setString(2, cause.getDescription());

            stmt.setString(3, cause.getNotes());

            stmt.setBoolean(4, cause.isActive());

            stmt.execute();
            String retrievalQuery = "SELECT currval('occinspectioncause_causeid_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();

            while (rs.next()) {
                newCauseID = rs.getInt(1);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert OccInspectionCause", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        cause.setCauseID(newCauseID);
        return cause;
    }
}