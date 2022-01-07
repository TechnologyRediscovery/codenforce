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
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.IntegrationException;
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
import java.util.logging.Level;
import java.util.logging.Logger;

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
     * OccInspection object. This method, in turn, calls the private
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

    public OccInspectedSpace getInspectedSpace(int inspectedspaceID) throws IntegrationException {
        OccInspectedSpace inspectedSpace = null;
        if (inspectedspaceID == 0) {
            System.out.println("OccInspectionIntegrator.getInspectedSpace | called with spaceid=0");
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
                System.out.println("inspectedSpace.getInspectedSpaceID(): " + inspectedSpace.getInspectedSpaceID());
                stmt.setInt(1, inspectedSpace.getInspectedSpaceID());
                rs = stmt.executeQuery();
                while (rs.next()) {
                    inspectedEleList.add(getInspectedSpaceElement(rs.getInt("inspectedspaceelementid")));
                }
            } else {
                System.out.println("OccInspectionIntegrator.getInspectedSpace | Failure: inspected space is null");
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

    private OccInspectedSpace generateOccInspectedSpace(ResultSet rs) throws SQLException, IntegrationException {
        OccInspectedSpace inSpace = null;
        try {
            UserIntegrator ui = getUserIntegrator();
            OccChecklistIntegrator oci = getOccChecklistIntegrator();
            
            inSpace = new OccInspectedSpace();
            inSpace.setInspectedSpaceID(rs.getInt("inspectedspaceid"));
            inSpace.setLocation(getLocationDescriptor(rs.getInt("occlocationdescription_descid")));
            
            inSpace.setType(oci.getOccSpaceType(rs.getInt("occchecklistspacetype_chklstspctypid")));
            
            inSpace.setAddedToChecklistBy(ui.getUser(rs.getInt("addedtochecklistby_userid")));
            inSpace.setInspectionID(rs.getInt("occinspection_inspectionid"));
            
            if (rs.getTimestamp("addedtochecklistts") != null) {
                inSpace.setAddedToChecklistTS(rs.getTimestamp("addedtochecklistts").toLocalDateTime());
            }
            
        } catch (BObStatusException ex) {
            System.out.println(ex);
        }
        return inSpace;

    }

    public OccInspectedSpaceElement getInspectedSpaceElement(int eleID) throws IntegrationException {
        String query_spaceIDs = "SELECT inspectedspaceelementid, notes, locationdescription_id, lastinspectedby_userid, \n"
                + "lastinspectedts, compliancegrantedby_userid, compliancegrantedts, \n"
                + "inspectedspace_inspectedspaceid, overriderequiredflagnotinspected_userid, \n"
                + "occchecklistspacetypeelement_elementid, occinspectedspaceelement.required, failureseverity_intensityclassid, migratetocecaseonfail, occchecklistspacetypeelement.codeelement_id \n"
                + "FROM public.occinspectedspaceelement INNER JOIN public.occchecklistspacetypeelement ON (occchecklistspacetypeelement_elementid = spaceelementid) \n"
                + "WHERE inspectedspaceelementid=?;";

        System.out.println("getInspectedSpaceElement grabbing OccInspectedSpaceElement w/ ID: " + eleID);

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
            throw new IntegrationException("Unable to inspected space, sorry!", ex);

        } finally {
              if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return ele;

    }

    private OccInspectedSpaceElement generateInspectedSpaceElement(ResultSet rs) throws SQLException, IntegrationException {
            CodeIntegrator ci = getCodeIntegrator();
            UserIntegrator ui = getUserIntegrator();
            BlobIntegrator bi = getBlobIntegrator();
            BlobCoordinator bc = getBlobCoordinator();
            OccInspectedSpaceElement inspectedEle = null;
        try {
            
            inspectedEle = new OccInspectedSpaceElement(ci.getCodeElement(rs.getInt("codeelement_id")));
            
            inspectedEle.setInspectedSpaceElementID(rs.getInt("occchecklistspacetypeelement_elementid"));
            
            inspectedEle.setInspectionNotes(rs.getString("notes"));
            inspectedEle.setLocation(getLocationDescriptor(rs.getInt("locationdescription_id")));
            inspectedEle.setLastInspectedBy(ui.getUser(rs.getInt("lastinspectedby_userid")));
            
            if (rs.getTimestamp("lastinspectedts") != null) {
                inspectedEle.setLastInspectedTS(rs.getTimestamp("lastinspectedts").toLocalDateTime());
            }
            inspectedEle.setComplianceGrantedBy(ui.getUser(rs.getInt("compliancegrantedby_userid")));
            if (rs.getTimestamp("compliancegrantedts") != null) {
                inspectedEle.setComplianceGrantedTS(rs.getTimestamp("compliancegrantedts").toLocalDateTime());
            }
            inspectedEle.setInspectedSpaceID(rs.getInt("inspectedspace_inspectedspaceid"));
            inspectedEle.setOverrideRequiredFlag_thisElementNotInspectedBy(ui.getUser(rs.getInt("overriderequiredflagnotinspected_userid")));
            
            inspectedEle.setRequired(rs.getBoolean("required"));
            inspectedEle.setFailureIntensityClassID(rs.getInt("failureseverity_intensityclassid"));
            
            inspectedEle.setMigrateToCaseOnFail(rs.getBoolean("migratetocecaseonfail"));
            
            try {
                List<Integer> idList = bi.photosAttachedToInspectedSpaceElement(inspectedEle.getInspectedSpaceElementID());
                inspectedEle.setBlobList(bc.getBlobLightList(idList));
            } catch(BlobException ex){
                throw new IntegrationException("An error occurred while trying to retrieve blobs for a OccInspectedSpaceElement", ex);
            }
            System.out.println("Returning element with ID of: " + rs.getInt("occchecklistspacetypeelement_elementid"));
        } catch(BObStatusException ex){
            System.out.println(ex);
        }
            return inspectedEle;
    }

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

    public List<OccLocationDescriptor> getLocationDescriptorsByInspection(OccInspection inspection) {
        //

        return new ArrayList();
    }

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

    public OccInspectedSpace recordCommencementOfSpaceInspection(OccInspectedSpace spc, OccInspection inspection)
            throws IntegrationException {

        String sql = "INSERT INTO public.occinspectedspace(\n"
                + "            inspectedspaceid, occspace_spaceid, occinspection_inspectionid, \n"
                + "            occlocationdescription_descid, addedtochecklistby_userid, addedtochecklistts)\n"
                + "    VALUES (DEFAULT, ?, ?, \n"
                + "            ?, ?, now());";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        int insertedInspSpaceID = 0;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, spc.getInspectedSpaceID());
            stmt.setInt(2, inspection.getInspectionID());
            if (spc.getLocation() != null) {
                stmt.setInt(3, spc.getLocation().getLocationID());
            } else {
                stmt.setInt(3, Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString("locationdescriptor_implyfromspacename")));
            }
            if (spc.getAddedToChecklistBy() != null) {
                stmt.setInt(4, spc.getAddedToChecklistBy().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
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
     * Remember: The db doesn't have a concept of an "inspected space", only an
     * inspectedSpaceElement so this method will iterate over the inspected
     * elements in the OccInspectedSpace, executing an insert statement for
     * each.
     *
     * @param inspection the current OccInspection
     * @param inspSpace an OccInspectedSpace that was NOT retrieved from the DB
     * @return the number of newly inserted spaces (mostly for info value only)
     * passed as the second input parameter having been written to DB and added
     * to the OccInspection's internal List of inspected elements.
     * @throws IntegrationException
     */
    public int recordInspectionOfSpaceElements(OccInspectedSpace inspSpace, OccInspection inspection) throws IntegrationException {
        int spaceInserts = 0;
        Iterator<OccInspectedSpaceElement> inspectedElementListIterator = inspSpace.getInspectedElementList().iterator();
        while (inspectedElementListIterator.hasNext()) {
            OccInspectedSpaceElement oie = inspectedElementListIterator.next();
            if (oie.getInspectedSpaceElementID() != 0) {
                updateInspectedSpaceElement(oie);
            } else {
                spaceInserts++;
                insertInspectedSpaceElement(oie, inspSpace);
            }
        }
        return spaceInserts;
    }
    
    private void insertInspectedSpaceElement(OccInspectedSpaceElement inspElement, OccInspectedSpace inSpace) throws IntegrationException{
        
        String sql =     "INSERT INTO public.occinspectedspaceelement(\n" +
                                "            inspectedspaceelementid, notes, locationdescription_id, lastinspectedby_userid, \n" +
                                "            lastinspectedts, compliancegrantedby_userid, compliancegrantedts, \n" +
                                "            inspectedspace_inspectedspaceid, overriderequiredflagnotinspected_userid, \n" +
                                "            spaceelement_elementid, required, failureseverity_intensityclassid)\n" +
                                "    VALUES (DEFAULT, ?, ?, ?, \n" +
                                "            ?, ?, ?, \n" +
                                "            ?, ?, \n" +
                                "            ?, ?, ?);";
        
        // single row formatting of query_icse
        // SELECT DISTINCT space_id FROM checklistspaceelement INNER JOIN spaceelement ON (spaceelement_id = spaceelementid) WHERE checklist_id = 1;
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
            
            stmt.setInt(9, inspElement.getSpaceElementID());

            stmt.setBoolean(10, inspElement.isRequired());

            // TODO: Finish severity intensity!
//                stmt.setInt(10, 0);
            stmt.setNull(11, java.sql.Types.NULL);
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
    }

    public void updateInspectedSpaceElement(OccInspectedSpaceElement inspElement) throws IntegrationException {
        String sql = "UPDATE public.occinspectedspaceelement\n"
                + "   SET notes=?, lastinspectedby_userid=?, lastinspectedts=?, compliancegrantedby_userid=?, \n"
                + "       compliancegrantedts=?, failureseverity_intensityclassid=? \n"
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

            // TODO: failure severity classes
            stmt.setNull(6, java.sql.Types.NULL);

            stmt.setInt(7, inspElement.getInspectedSpaceElementID());

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
    public void updateInspectedSpace(OccInspection inspection, OccInspectedSpace inspSpace) throws IntegrationException {

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

    public List<Integer> getOccInspectionList(OccPeriod op) throws IntegrationException {

        List<Integer> inspecIDList = new ArrayList<>();

        String query = "SELECT inspectionid FROM occinspection WHERE occperiod_periodid=? ";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, op.getPeriodID());
            rs = stmt.executeQuery();

            while (rs.next()) {
                inspecIDList.add(rs.getInt("inspectionid"));
                System.out.println("Found inspection ID: " + rs.getInt("inspectionid"));
                System.out.println("...with the period ID: " + op.getPeriodID());
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

    public List<OccInspection> getOccInspectionListByPACC(int pacc) throws IntegrationException {

        List<OccInspection> inspecList = new ArrayList<>();

        String query = "SELECT inspectionid FROM occinspection WHERE publicaccesscc=? ";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, pacc);
            rs = stmt.executeQuery();

            while (rs.next()) {
                inspecList.add(getOccInspection(rs.getInt("inspectionid")));
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
    
    public OccInspection getOccInspection(int inspectionID) throws IntegrationException {
        OccupancyCoordinator oc = getOccupancyCoordinator();

        OccInspection inspection = null;

        String query = " SELECT inspectionid, occperiod_periodid, inspector_userid, publicaccesscc, \n"
                + "       enablepacc, notespreinspection, thirdpartyinspector_personid, thirdpartyinspectorapprovalts, \n"
                + "       thirdpartyinspectorapprovalby, maxoccupantsallowed, numbedrooms, numbathrooms, \n"
                + "       occchecklist_checklistlistid, effectivedate, createdts, followupto_inspectionid, \n"
                + "       deactivatedts, deactivatedby_userid, timestart, timeend, \n"
                + "       createdby_userid, lastupdatedts, lastupdatedby_userid, determination_detid, \n"
                + "       determinationby_userid, determinationts, remarks, generalcomments, \n"
                + "       cause_causeid \n"
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

    private OccInspection generateOccInspection(ResultSet rs) throws IntegrationException, SQLException {
            OccInspection ins = new OccInspection();
            
            
            UserIntegrator ui = getUserIntegrator();
            PersonCoordinator pc = getPersonCoordinator();
        try {
            
            ins.setInspectionID(rs.getInt("inspectionid"));
            ins.setOccPeriodID(rs.getInt("occperiod_periodid"));
            ins.setInspector(ui.getUser(rs.getInt("inspector_userid")));
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
                ins.setThirdPartyApprovalBy(ui.getUser(rs.getInt("thirdpartyinspectorapprovalby")));
            }
            ins.setMaxOccupantsAllowed(rs.getInt("maxoccupantsallowed"));
            ins.setNumBedrooms(rs.getInt("numbedrooms"));
            ins.setNumBathrooms(rs.getInt("numbathrooms"));
            
            
            // now set the big lists
            ins.setChecklistTemplate(getOccChecklistIntegrator().getChecklistTemplate(rs.getInt("occchecklist_checklistlistid")));
            ins.setInspectedSpaceList(getInspectedSpaceList(ins.getInspectionID()));
            
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
                ins.setDeactivatedBy(ui.getUser(rs.getInt("deactivatedby_userid")));
            }
            if (rs.getTimestamp("timestart") != null) {
                ins.setTimeStart(rs.getTimestamp("timestart").toLocalDateTime());
            }
            if (rs.getTimestamp("timeend") != null) {
                ins.setTimeEnd(rs.getTimestamp("timeend").toLocalDateTime());
            }
            
            if (rs.getInt("createdby_userid") != 0) {
                ins.setCreatedBy(ui.getUser(rs.getInt("createdby_userid")));
            }
            if (rs.getTimestamp("lastupdatedts") != null) {
                ins.setLastUpdatedTS(rs.getTimestamp("lastupdatedts").toLocalDateTime());
            }
            if (rs.getInt("lastupdatedby_userid") != 0) {
                ins.setLastUpdatedBy(ui.getUser(rs.getInt("lastupdatedby_userid")));
            }
            if (rs.getInt("determination_detid") != 0) {
                ins.setDetermination(getDetermination(rs.getInt("determination_detid")));
            }
            
            if (rs.getInt("determinationby_userid") != 0) {
                ins.setLastUpdatedBy(ui.getUser(rs.getInt("determinationby_userid")));
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

    public void updateOccInspection(OccInspection occInsp) throws IntegrationException {
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
            stmt.setInt(1, occInsp.getInspector().getUserID());
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
                stmt.setInt(20, occInsp.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(20, java.sql.Types.NULL);
            }
            if (occInsp.getDetermination() != null) {
                stmt.setInt(21, occInsp.getDetermination().getDeterminationID());
            } else {
                stmt.setNull(21, java.sql.Types.NULL);
            }
            if (occInsp.getDeterminationBy() != null) {
                stmt.setInt(22, occInsp.getDeterminationBy().getUserID());
            } else {
                stmt.setNull(22, java.sql.Types.NULL);
            }

            if (occInsp.getDeterminationTS() != null) {
                stmt.setTimestamp(23, java.sql.Timestamp.valueOf(occInsp.getDeterminationTS()));
            } else {
                stmt.setNull(23, java.sql.Types.NULL);
            }
            stmt.setString(24, occInsp.getRemarks());
            stmt.setString(25, occInsp.getGeneralComments());
            if (occInsp.getCause() != null) {
                stmt.setInt(26, occInsp.getCause().getCauseID());
            } else {
                stmt.setNull(26, java.sql.Types.NULL);
            }

            stmt.setInt(28, occInsp.getInspectionID());

            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update occupancy inspection record", ex);
        } finally {
              if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
    }

    public void activateOccInspection(OccInspection ins) throws IntegrationException {

        String query = "UPDATE occinspection SET active=false WHERE occperiod_periodid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ins.getOccPeriodID());
            stmt.executeUpdate();

            // now turn the given inspection active
            query = "UPDATE occinspection SET active=true WHERE inspectionid=?;";
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ins.getInspectionID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot mark inspection as activate, sorries!", ex);
        } finally {
              if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             

        } // close finally
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

    public OccInspection insertOccInspection(OccInspection occInsp) throws IntegrationException {
        String query = "INSERT INTO public.occinspection(\n"
                + "            inspectionid, occperiod_periodid, inspector_userid, publicaccesscc, \n"
                + "            enablepacc, notespreinspection, thirdpartyinspector_personid, thirdpartyinspectorapprovalts, \n"
                + "            thirdpartyinspectorapprovalby, maxoccupantsallowed, numbedrooms, numbathrooms, \n"
                + "            occchecklist_checklistlistid, effectivedate, createdts, followupto_inspectionid, \n"
                + "            deactivatedts, deactivatedby_userid, timestart, timeend, \n"
                + "            createdby_userid, lastupdatedts, lastupdatedby_userid, determination_detid, \n"
                + "            determinationby_userid, determinationts, remarks, generalcomments, \n"
                + "            cause_causeid)\n"
                + "    VALUES (DEFAULT, ?, ?, ?, \n"
                + "            ?, ?, ?, ?, \n"
                + "            ?, ?, ?, ?, \n"
                + "            ?, ?, now(), ?, \n"
                + "            ?, ?, ?, ?, \n"
                + "            ?, ?, ?, ?, \n"
                + "            ?, ?, ?, ?, \n"
                + "            ?);";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        int newInspectionID = 0;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, occInsp.getOccPeriodID());
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
        occInsp.setInspectionID(newInspectionID);
        return occInsp;
    }

    public void detachSpacefromSpaceType(OccSpaceType ost) throws IntegrationException {

        String query = "DELETE FROM public.occspace\n"
                + " WHERE spacetype_id=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ost.getSpaceTypeID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot detach space from spacetype--probably because another" + "part of the database has a reference item.", ex);
        } finally {
              if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             

        } // close finally
    }
        
    public int determinationCheckForUse(OccInspectionDetermination d) throws IntegrationException {
        SystemIntegrator si = getSystemIntegrator();
        int uses = 0;
        List<String> useTables = si.findForeignUseTables("determination");
        for(int x = 0; x < useTables.size(); x++){
            uses =+ si.checkForUse("public." + useTables.get(x), "determination_detid", d.getDeterminationID());
            System.out.println("Checked public." + useTables.get(x) + " for  determination_detid" + d.getDeterminationID());
        };
        return uses;
    }
    
    public OccInspectionDetermination getDetermination(int determinationID) throws IntegrationException {
        EventIntegrator ei = new EventIntegrator();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT determinationid, title, description, notes, eventcat_catid, active ");
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
        sb.append("UPDATE public.occinspectiondetermination SET title=?, description=?, notes=?, eventcat_catid=?, active=? ");
        sb.append("WHERE determinationid = ?;");

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setString(1, d.getTitle());
            stmt.setString(2, d.getDescription());
            stmt.setString(3, d.getNotes());
            stmt.setInt(4, d.getEventCategory().getCategoryID());
            stmt.setBoolean(5, d.isActive());
            stmt.setInt(6, d.getDeterminationID());
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
        sb.append("determinationid, title, description, notes, eventcat_catid, active) ");
        sb.append("VALUES (DEFAULT, ?, ?, ?, ?, ?);");

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setString(1, d.getTitle());
            stmt.setString(2, d.getDescription());
            stmt.setString(3, d.getNotes());
            stmt.setInt(4, d.getEventCategory().getCategoryID());
            stmt.setBoolean(5, true);
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
    
    public List<OccInspectionRequirementAssigned> getOccRequirementAssignedList(OccInspection inspection){
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
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
        //query the assigned table by inspection number
        //
        
        
        
        
        
        
        
        return assignedList;
    }
    
    public OccInspectionRequirementAssigned getOccRequirementAssigned(int inspectionID, int requirementID){
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
        } catch (BObStatusException ex) {
            Logger.getLogger(OccInspectionIntegrator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
        
        
        
        
        
        
        
        return reqAssigned;
    }
    
    private OccInspectionRequirementAssigned generateOccRequirementAssigned(ResultSet rs) throws BObStatusException{
        OccInspectionRequirementAssigned reqAssigned = null;
        if(rs == null){
            return reqAssigned;
        }
        UserIntegrator ui = getUserIntegrator();
        try {            
            //check for null rs
            //if not null make a new instance of OccInspectionRequirementAssigned
            //in constructor call get requirement and pass in requirementid
            reqAssigned = new OccInspectionRequirementAssigned(getRequirement(rs.getInt("occrequirement_requirementid")));
            reqAssigned.setInspectionID(rs.getInt("occinspection_inspectionid"));
            
            reqAssigned.setAssignedBy(ui.getUser(rs.getInt("assignedby")));
            reqAssigned.setAssignedDate(rs.getTimestamp("assigneddate").toLocalDateTime());
            reqAssigned.setAssignedNotes(rs.getString("assignednotes"));
            
            if(rs.getTimestamp("fulfilleddate") != null){
                reqAssigned.setFulfilledBy(ui.getUser(rs.getInt("fulfilledby")));
                reqAssigned.setFulfilledDate(rs.getTimestamp("fulfilleddate").toLocalDateTime());
                reqAssigned.setFulfilledNotes(rs.getString("fulfillednotes"));
            }
            
            reqAssigned.setNotes(rs.getString("notes"));
            
        } catch (SQLException | IntegrationException ex) {
            Logger.getLogger(OccInspectionIntegrator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return reqAssigned;
    }

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

    private OccInspectionCause generateCause(ResultSet rs) throws SQLException {
        OccInspectionCause cause = new OccInspectionCause();

        cause.setCauseID(rs.getInt("causeid"));

        cause.setTitle(rs.getString("title"));
        cause.setDescription(rs.getString("description"));

        cause.setNotes(rs.getString("notes"));

        cause.setActive(rs.getBoolean("active"));

        return cause;
    }

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