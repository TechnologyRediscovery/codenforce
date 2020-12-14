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
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.entities.occupancy.OccChecklistTemplate;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpaceElement;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpace;
import com.tcvcog.tcvce.entities.occupancy.OccLocationDescriptor;
import com.tcvcog.tcvce.entities.occupancy.OccInspection;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccSpace;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceElement;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceType;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceTypeInspectionDirective;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class OccInspectionIntegrator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of ChecklistIntegrator
     */
    public OccInspectionIntegrator() {
    }

    public void updateChecklistTemplateMetadata(OccChecklistTemplate blueprint) throws IntegrationException {

        String query = "UPDATE public.occchecklist\n"
                + "   SET title=?, description=?, muni_municode=?, active=?, \n"
                + "       governingcodesource_sourceid=?\n"
                + " WHERE checklistid=?";
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

    // maybe don't need this one
    public void insertChecklistTemplateMetadata(OccChecklistTemplate bp) throws IntegrationException {

        String query = "INSERT INTO public.occchecklist(\n"
                + "            checklistid, title, description, muni_municode, active, governingcodesource_sourceid)\n"
                + "    VALUES (DEFAULT, ?, ?, ?, ?, ?);";
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
                stmt.setNull(5, java.sql.Types.NULL);
            }
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);

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
    }

    public void insertSpace(OccSpace space) throws IntegrationException {

        String query = "INSERT INTO public.occspace(\n"
                + "            spaceid, name, spacetype_id, required, description)\n"
                + "    VALUES (DEFAULT, ?, ?, ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, space.getName());
            stmt.setInt(2, space.getOccSpaceTypeID());
            stmt.setBoolean(3, space.isRequired());
            stmt.setString(4, space.getDescription());

            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert space", ex);

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

    public void updateSpace(OccSpace space) throws IntegrationException {

        String query = "UPDATE public.occspace\n"
                + "   SET name=?, spacetype_id=?, required=?, description=?\n"
                + " WHERE spaceid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, space.getName());
            stmt.setInt(2, space.getOccSpaceTypeID());
            stmt.setBoolean(3, space.isRequired());
            stmt.setString(4, space.getDescription());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot update space", ex);

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

    public OccSpace getOccSpace(int spaceID) throws IntegrationException {

        OccSpace space = null;
        String query = "SELECT spaceid, name, spacetype_id, required, description\n"
                + "  FROM public.occspace WHERE spaceid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, spaceID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                space = generateOccSpace(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Could not create sapce", ex);
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
        return space;
    }

    public OccSpaceType getOccSpaceType(int spaceTypeID) throws IntegrationException {

        OccSpaceType spaceType = null;
        String query = "SELECT spacetypeid, spacetitle, description, required\n"
                + "  FROM public.occspacetype WHERE spacetypeid=?;";
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
        return spaceType;
    }

    private OccSpace generateOccSpace(ResultSet rs) throws SQLException, IntegrationException {
        OccSpace space = new OccSpace();
        space.setSpaceID(rs.getInt("spaceid"));
        space.setName(rs.getString("name"));
        space.setOccSpaceTypeID(rs.getInt("spacetype_id"));
        space.setDescription(rs.getString("description"));
        space = populateSpaceWithCodeElements(space);
        return space;
    }

    /**
     * Takes in a OccSpace object, extracts its ID, then uses that to query for
     * all of the codeelements attached to that space. Used to compose a
     * checklist blueprint which can be converted into an implemented checklist
     *
     * @param spc the space Object to load up with elements. When passed in,
     * only the ID needs to be held in the object
     * @return a OccSpace object populated with CodeElement objects
     * @throws IntegrationException
     */
    private OccSpace populateSpaceWithCodeElements(OccSpace spc) throws IntegrationException {
        CodeIntegrator ci = getCodeIntegrator();
        String query = "SELECT spaceelementid, space_id, codeelement_id, required\n"
                + "  FROM public.occspaceelement WHERE space_id=?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<OccSpaceElement> eleList = new ArrayList<>();
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, spc.getSpaceID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                CodeElement ele = ci.getCodeElement(rs.getInt("codeelement_id"));
                int spcEleID = rs.getInt("spaceelementid");
                eleList.add(new OccSpaceElement(ele, spcEleID));
            }
            spc.setSpaceElementList(eleList);

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);

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
        return spc;
    }

    /**
     * Connects a OccSpace with Code Elements.
     *
     * @param spc the space to which the elements should be connected
     * @param elementsToAttach a list of CodeElements that should be inspected
     * in this space
     * @return
     * @throws IntegrationException
     */
    public void attachCodeElementsToSpace(OccSpace spc, List<CodeElement> elementsToAttach) throws IntegrationException {

        String sql = "INSERT INTO public.occspaceelement(\n"
                + " spaceelementid, space_id, codeelement_id)\n"
                + " VALUES (DEFAULT, ?, ?);";
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
                stmt.setInt(1, spc.getSpaceID());
                stmt.setInt(2, ce.getElementID());
                stmt.execute();
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);
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
    }

    /**
     * Connects a OccSpace with Code Elements.
     *
     * @param spc the space to which the elements should be connected
     * @param ele
     * @return
     * @throws IntegrationException
     */
    public int attachCodeElementToSpace(OccSpace spc, CodeElement ele) throws IntegrationException {

        if (spc == null || ele == null) {
            return 0;
        }

        int newlyLinkedSpaceCodeElementID = 0;
        String sql = "INSERT INTO public.occspaceelement(\n"
                + " spaceelementid, space_id, codeelement_id)\n"
                + " VALUES (DEFAULT, ?, ?);";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, spc.getSpaceID());
            stmt.setInt(2, ele.getElementID());
            stmt.execute();

            String retrievalQuery = "SELECT currval('spaceelement_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();

            while (rs.next()) {
                newlyLinkedSpaceCodeElementID = rs.getInt(1);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("", ex);
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
        return newlyLinkedSpaceCodeElementID;
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
        String query = "DELETE FROM public.occspaceelement\n"
                + " WHERE space_id = ? AND codeelement_id = ?;";
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
            String query = "DELETE FROM public.occspaceelement\n"
                    + " WHERE spaceelementid = ?;";

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
            throw new IntegrationException("Unable to delete the space and its associated"
                    + "elements: Probably because this space has been used in one or more"
                    + "occupancy inspectsion. It's here to stay!", ex);

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
    
    
    public List<OccChecklistTemplate> getChecklistTemplateList(Municipality m) throws IntegrationException{
        String checklistTableSELECT = "SELECT checklistid FROM public.occchecklist;";
        Connection con = getPostgresCon();
        List<OccChecklistTemplate> tempList = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            
            stmt = con.prepareStatement(checklistTableSELECT);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                tempList.add(getChecklistTemplate(rs.getInt("checklistid")));
                
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build a checklist blueprint from database", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return tempList;
        
    }
    
    

    public OccChecklistTemplate getChecklistTemplate(int checklistID) throws IntegrationException {

        String checklistTableSELECT = "SELECT checklistid, title, description, muni_municode, active, governingcodesource_sourceid\n"
                + "  FROM public.occchecklist WHERE checklistid=?;";
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

        chkList.setOccSpaceTypeTemplateList(getOccInspecTemplateSpaceTypeList(chkList.getInspectionChecklistID()));

        return chkList;
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

        // temp to close
        return inspSpaceList;
    }

    public OccInspectedSpace getInspectedSpace(int inspectedspaceID) throws IntegrationException {
        OccInspectedSpace inspectedSpace = null;
        if (inspectedspaceID == 0) {
            System.out.println("OccInspectionIntegrator.getInspectedSpace | called with spaceid=0");
            return inspectedSpace;
        }
        String querySpace = "SELECT inspectedspaceid, occspace_spaceid, occinspection_inspectionid, \n"
                + "       occlocationdescription_descid, addedtochecklistby_userid, addedtochecklistts \n"
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
            } else {
                System.out.println("OccInspectionIntegrator.getInspectedSpace | Failure: inspected space is null");
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate an inspected space, sorry!", ex);

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
        if (inspectedSpace != null) {
            // finally, combine our two objects by injecting the Element list into the InspectedSpace
            inspectedSpace.setInspectedElementList(inspectedEleList);
        }
        return inspectedSpace;
    }

    private OccInspectedSpace generateOccInspectedSpace(ResultSet rs) throws SQLException, IntegrationException {
        UserIntegrator ui = getUserIntegrator();

        OccInspectedSpace inSpace = new OccInspectedSpace(getOccSpace(rs.getInt("occspace_spaceid")));
        inSpace.setInspectedSpaceID(rs.getInt("inspectedspaceid"));
        inSpace.setLocation(getLocationDescriptor(rs.getInt("occlocationdescription_descid")));
        inSpace.setSpaceType(getSpaceType(inSpace.getOccSpaceTypeID()));
        inSpace.setAddedToChecklistBy(ui.getUser(rs.getInt("addedtochecklistby_userid")));
        inSpace.setInspectionID(rs.getInt("occinspection_inspectionid"));
        
        if (rs.getTimestamp("addedtochecklistts") != null) {
            inSpace.setAddedToChecklistTS(rs.getTimestamp("addedtochecklistts").toLocalDateTime());
        }

        return inSpace;

    }

    public OccInspectedSpaceElement getInspectedSpaceElement(int eleID) throws IntegrationException {
        String query_spaceIDs = "SELECT inspectedspaceelementid, notes, locationdescription_id, lastinspectedby_userid, \n"
                + "       lastinspectedts, compliancegrantedby_userid, compliancegrantedts, \n"
                + "       inspectedspace_inspectedspaceid, overriderequiredflagnotinspected_userid, \n"
                + "       spaceelement_elementid, occinspectedspaceelement.required, failureseverity_intensityclassid, "
                + "       occspaceelement.codeelement_id, occspaceelement.spaceelementid \n"
                + "  FROM public.occinspectedspaceelement INNER JOIN public.occspaceelement ON (spaceelement_elementid = spaceelementid)\n"
                + "  WHERE inspectedspaceelementid=?;";

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

        return ele;

    }

    private OccInspectedSpaceElement generateInspectedSpaceElement(ResultSet rs) throws SQLException, IntegrationException {
        CodeIntegrator ci = getCodeIntegrator();
        UserIntegrator ui = getUserIntegrator();

        OccInspectedSpaceElement inspectedEle
                = new OccInspectedSpaceElement(ci.getCodeElement(rs.getInt("codeelement_id")), rs.getInt("spaceelementid"));

        inspectedEle.setInspectedSpaceElementID(rs.getInt("inspectedspaceelementid"));

        inspectedEle.setNotes(rs.getString("notes"));
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
        return insertedLocDescID;
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
            stmt.setInt(1, spc.getSpaceID());
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

            stmt.setString(1, inspElement.getNotes());
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

            stmt.setString(1, inspElement.getNotes());

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
            stmt.setInt(1, inspSpace.getSpaceID());
            stmt.setInt(2, inspection.getInspectionID());
            stmt.setInt(3, inspSpace.getLocation().getLocationID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Could not update inspected space metadata", ex);

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
    }

    public void deleteInspectedSpace(OccInspectedSpace is) throws IntegrationException {
        String sqlDeleteInsSpaceElement = "DELETE FROM occinspectedspaceelement WHERE inspectedspace_inspectedspaceid=?;";
        String sqlDeleteInsSpace = "DELETE FROM occinspectedspace WHERE inspectedspaceid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            // first remove inspected space elements
            stmt = con.prepareStatement(sqlDeleteInsSpaceElement);
            stmt.setInt(1, is.getSpaceID());
            stmt.execute();

            // then remove the inspected space
            stmt = con.prepareStatement(sqlDeleteInsSpace);
            stmt.setInt(1, is.getSpaceID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete inspected space!", ex);
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

    public OccSpaceType getSpaceType(int spacetypeid) throws IntegrationException {
        String query = "SELECT spacetypeid, spacetitle, description, required\n"
                + " FROM public.occspacetype WHERE spacetypeid = ?;";
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
        return st;
    }

    public List<OccSpaceTypeInspectionDirective> getOccInspecTemplateSpaceTypeList(int checklistID) throws IntegrationException {

        String query = "    SELECT checklistspacetypeid, checklist_id, required, \n"
                + "       spacetype_typeid, overridespacetyperequired, overridespacetyperequiredvalue, \n"
                + "       overridespacetyperequireallspaces\n"
                + "  FROM public.occchecklistspacetype WHERE checklist_id=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<OccSpaceTypeInspectionDirective> typelist = new ArrayList<>();

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, checklistID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                typelist.add(generateOccSpaceTypeInspectionDirective(rs));

            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get space type", ex);

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

        return typelist;
    }

    private OccSpaceTypeInspectionDirective generateOccSpaceTypeInspectionDirective(ResultSet rs) throws IntegrationException, SQLException {

        // Now use the SpaceType to make a SpaceTypeTemplate that contains
        // variables for configuring SpacTypes when they are actually inspected
        OccSpaceTypeInspectionDirective directive = new OccSpaceTypeInspectionDirective(getOccSpaceType(rs.getInt("spacetype_typeid")));
        directive.setOverrideSpaceTypeRequired(rs.getBoolean("overridespacetyperequired"));
        directive.setOverrideSpaceTypeRequiredValue(rs.getBoolean("overridespacetyperequiredvalue"));
        directive.setOverrideSpaceTypeRequireAllSpaces(rs.getBoolean("overridespacetyperequireallspaces"));
        directive.setSpaceList(getOccSpaceList(directive.getSpaceTypeID()));
        return directive;
    }

    private OccSpaceType generateOccSpaceType(ResultSet rs) throws SQLException, IntegrationException {
        OccSpaceType type = new OccSpaceType();
        type.setSpaceTypeID(rs.getInt("spacetypeid"));
        type.setSpaceTypeTitle(rs.getString("spacetitle"));
        type.setSpaceTypeDescription(rs.getString("description"));
        type.setRequired(rs.getBoolean("required"));
        return type;
    }

    public List<OccSpace> getOccSpaceList(int spaceTypeID) throws IntegrationException {
        String query = "SELECT spaceid, name, spacetype_id, required, description\n"
                + "  FROM public.occspace WHERE spacetype_id=?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<OccSpace> spaceList = new ArrayList<>();

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, spaceTypeID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                spaceList.add(generateOccSpace(rs));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get space type", ex);

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

        return spaceList;
    }

    public void insertSpaceType(OccSpaceType spaceType) throws IntegrationException {
        String query = "INSERT INTO public.occspacetype(\n"
                + "         spacetypeid, spacetitle, description, required) \n"
                + "    VALUES (DEFAULT, ?, ?, ?)";

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
            throw new IntegrationException("Cannot delete space type--probably because another"
                    + "part of the database has a reference item.", ex);

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

    public void updateSpaceType(OccSpaceType spaceType) throws IntegrationException {
        String query = "UPDATE public.occspacetype\n"
                + "   SET spacetitle=?, description=?, required=?\n"
                + " WHERE spacetypeid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, spaceType.getSpaceTypeTitle());
            stmt.setString(2, spaceType.getSpaceTypeDescription());
            stmt.setBoolean(3, spaceType.isRequired());
            stmt.setInt(4, spaceType.getSpaceTypeID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update space type", ex);
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
        }
    }

    public List<OccInspection> getOccInspectionList(OccPeriod op) throws IntegrationException {

        List<OccInspection> inspecList = new ArrayList<>();

        String query = "SELECT inspectionid FROM occinspection WHERE occperiod_periodid=? ";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, op.getPeriodID());
            rs = stmt.executeQuery();

            while (rs.next()) {
                inspecList.add(getOccInspection(rs.getInt("inspectionid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrieve occinspectionlist", ex);

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

        return inspecList;

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

        return inspecList;

    }
    
    public OccInspection getOccInspection(int inspectionID) throws IntegrationException {
        OccupancyCoordinator oc = getOccupancyCoordinator();

        OccInspection inspection = null;

        String query = " SELECT inspectionid, occperiod_periodid, inspector_userid, publicaccesscc, \n"
                + "       enablepacc, notes, thirdpartyinspector_personid, thirdpartyinspectorapprovalts, \n"
                + "       thirdpartyinspectorapprovalby, passedinspection_userid, maxoccupantsallowed, \n"
                + "       numbedrooms, numbathrooms, passedinspectionts, occchecklist_checklistlistid, \n"
                + "       effectivedate, active, creationts \n"
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

        return oc.configureOccInspection(inspection);
    }

    private OccInspection generateOccInspection(ResultSet rs) throws IntegrationException, SQLException {
        OccInspection ins = new OccInspection();

        UserIntegrator ui = getUserIntegrator();
        PersonIntegrator pi = getPersonIntegrator();

        ins.setInspectionID(rs.getInt("inspectionid"));
        ins.setOccPeriodID(rs.getInt("occperiod_periodid"));
        ins.setInspector(ui.getUser(rs.getInt("inspector_userid")));
        ins.setPacc(rs.getInt("publicaccesscc"));

        ins.setEnablePacc(rs.getBoolean("enablepacc"));
        ins.setNotes(rs.getString("notes"));
        if (rs.getInt("thirdpartyinspector_personid") != 0) {
            ins.setThirdPartyInspector(pi.getPerson(rs.getInt("thirdpartyinspector_personid")));
        }
        if (rs.getTimestamp("thirdpartyinspectorapprovalts") != null) {
            ins.setThirdPartyInspectorApprovalTS(rs.getTimestamp("thirdpartyinspectorapprovalts").toLocalDateTime());
        }

        if (rs.getInt("thirdpartyinspectorapprovalby") != 0) {
            ins.setThirdPartyApprovalBy(ui.getUser(rs.getInt("thirdpartyinspectorapprovalby")));
        }
        if (rs.getInt("passedinspection_userid") != 0) {
            ins.setPassedInspectionCertifiedBy(ui.getUser(rs.getInt("passedinspection_userid")));
        }
        ins.setMaxOccupantsAllowed(rs.getInt("maxoccupantsallowed"));

        ins.setNumBedrooms(rs.getInt("numbedrooms"));
        ins.setNumBathrooms(rs.getInt("numbathrooms"));

        if (rs.getTimestamp("effectivedate") != null) {
            ins.setEffectiveDateOfRecord(rs.getTimestamp("effectivedate").toLocalDateTime());
        }

        // now set the big lists
        ins.setChecklistTemplate(getChecklistTemplate(rs.getInt("occchecklist_checklistlistid")));
        ins.setInspectedSpaceList(getInspectedSpaceList(ins.getInspectionID()));

        ins.setActive(rs.getBoolean("active"));

        if (rs.getTimestamp("creationts") != null) {
            ins.setCreationTS(rs.getTimestamp("creationts").toLocalDateTime());
        }

        return ins;
    }

    public void updateOccInspection(OccInspection occInsp) throws IntegrationException {
        String sql = "UPDATE public.occinspection\n"
                + "   SET inspector_userid=?, publicaccesscc=?, \n"
                + "       enablepacc=?, notes=?, thirdpartyinspector_personid=?, thirdpartyinspectorapprovalts=?, \n"
                + "       thirdpartyinspectorapprovalby=?, passedinspection_userid=?, maxoccupantsallowed=?, \n"
                + "       numbedrooms=?, numbathrooms=?, passedinspectionts=?, occchecklist_checklistlistid=?, \n"
                + "       effectivedate=?, active=?\n"
                + " WHERE inspectionid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, occInsp.getInspector().getUserID());
            stmt.setInt(2, occInsp.getPacc());

            stmt.setBoolean(3, occInsp.isEnablePacc());
            stmt.setString(4, occInsp.getNotes());
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
            if (occInsp.getPassedInspectionCertifiedBy() != null) {
                stmt.setInt(8, occInsp.getPassedInspectionCertifiedBy().getUserID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }

            stmt.setInt(9, occInsp.getMaxOccupantsAllowed());
            stmt.setInt(10, occInsp.getNumBedrooms());
            stmt.setInt(11, occInsp.getNumBathrooms());

            if (occInsp.getPassedInspectionTS() != null) {
                stmt.setTimestamp(12, java.sql.Timestamp.valueOf(occInsp.getPassedInspectionTS()));
            } else {
                stmt.setNull(12, java.sql.Types.NULL);
            }

            if (occInsp.getChecklistTemplate() != null) {
                stmt.setInt(13, occInsp.getChecklistTemplate().getInspectionChecklistID());
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }

            if (occInsp.getEffectiveDateOfRecord() != null) {
                stmt.setTimestamp(14, java.sql.Timestamp.valueOf(occInsp.getEffectiveDateOfRecord()));
            } else {
                stmt.setNull(14, java.sql.Types.NULL);
            }

            stmt.setBoolean(15, occInsp.isActive());

            stmt.setInt(16, occInsp.getInspectionID());

            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update occupancy inspection record", ex);
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

    public OccInspection insertOccInspection(OccInspection occInsp) throws IntegrationException {
        String query = "INSERT INTO public.occinspection(\n"
                + "            inspectionid, occperiod_periodid, inspector_userid, publicaccesscc, \n"
                + "            enablepacc, notes, thirdpartyinspector_personid, thirdpartyinspectorapprovalts, \n"
                + "            thirdpartyinspectorapprovalby, passedinspection_userid, maxoccupantsallowed, \n"
                + "            numbedrooms, numbathrooms, passedinspectionts, occchecklist_checklistlistid, \n"
                + "            effectivedate, active, creationts)\n"
                + "    VALUES (DEFAULT, ?, ?, ?, \n"
                + "            ?, ?, ?, ?, \n"
                + "            ?, ?, ?, \n"
                + "            ?, ?, ?, ?, \n"
                + "            ?, ?, now());";
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
            stmt.setString(5, occInsp.getNotes());
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
            if (occInsp.getPassedInspectionCertifiedBy() != null) {
                stmt.setInt(9, occInsp.getPassedInspectionCertifiedBy().getUserID());
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            stmt.setInt(10, occInsp.getMaxOccupantsAllowed());

            stmt.setInt(11, occInsp.getNumBedrooms());
            stmt.setInt(12, occInsp.getNumBathrooms());
            if (occInsp.getPassedInspectionTS() != null) {
                stmt.setTimestamp(13, java.sql.Timestamp.valueOf(occInsp.getPassedInspectionTS()));
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }
            if (occInsp.getChecklistTemplate() != null) {
                stmt.setInt(14, occInsp.getChecklistTemplate().getInspectionChecklistID());
            } else {
                stmt.setNull(14, java.sql.Types.NULL);
            }

            if (occInsp.getEffectiveDateOfRecord() != null) {
                stmt.setTimestamp(15, java.sql.Timestamp.valueOf(occInsp.getEffectiveDateOfRecord()));
            } else {
                stmt.setNull(15, java.sql.Types.NULL);
            }

            stmt.setBoolean(16, occInsp.isActive());

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
        }
        occInsp.setInspectionID(newInspectionID);
        return occInsp;
    }

    //xiaohong 
    //check
    public List<OccChecklistTemplate> getOccChecklistTemplatelist() throws IntegrationException {

        List<OccChecklistTemplate> checklistList = new ArrayList<>();

        String query = "SELECT checklistid FROM public.occchecklist ";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                checklistList.add(getChecklistTemplate(rs.getInt("checklistid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrieve occinspectionlist", ex);

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

        return checklistList;
    }

    public void insertOccChecklistSpaceType(int checklistid, OccSpaceTypeInspectionDirective ost) throws IntegrationException {
        String query = "INSERT INTO public.occchecklistspacetype(\n"
                + "     checklistspacetypeid, checklist_id, required, spacetype_typeid, \n"
                + "     overridespacetyperequired, overridespacetyperequiredvalue, overridespacetyperequireallspaces) \n"
                + "     VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, checklistid);
            stmt.setBoolean(2, ost.isRequired());
            stmt.setInt(3, ost.getSpaceTypeID());
            stmt.setBoolean(4, ost.isOverrideSpaceTypeRequired());
            stmt.setBoolean(5, ost.isOverrideSpaceTypeRequiredValue());
            stmt.setBoolean(6, ost.isOverrideSpaceTypeRequireAllSpaces());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert ChecklistSpaceType", ex);
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

    public int getLastInsertSpaceTypeid() {

        int lastInsertSpaceTypeid = 0;

        String query = "SELECT spacetypeid\n"
                + "  FROM public.occspacetype\n"
                + "  ORDER BY spacetypeid DESC\n"
                + "  LIMIT 1;";
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

        return lastInsertSpaceTypeid;
    }

    public void updateOccChecklistSpaceType(OccSpaceTypeInspectionDirective ost) throws IntegrationException {

        String query = "UPDATE public.occchecklistspacetype\n"
                + "     SET required=?\n"
                + "         overridespacetyperequired=?, overridespacetyperequiredvalue=?, \n"
                + "         overridespacetyperequireallspaces=?\n"
                + "     WHERE spacetype_typeid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setBoolean(1, ost.isRequired());
            stmt.setBoolean(2, ost.isOverrideSpaceTypeRequired());
            stmt.setBoolean(3, ost.isOverrideSpaceTypeRequiredValue());
            stmt.setBoolean(4, ost.isOverrideSpaceTypeRequireAllSpaces());
            stmt.setInt(5, ost.getSpaceTypeID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Could not update space", ex);

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
    }
    
    public void deleteOccChecklistSpaceType(OccSpaceType spaceType) throws IntegrationException {
        String query = "DELETE FROM public.occchecklistspacetype\n"
                + " WHERE spacetype_typeid= ?;";
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
    
    public void deleteSpace(OccSpace oc) throws IntegrationException {

        String query = "DELETE FROM public.occspace\n"
                + " WHERE spaceid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, oc.getSpaceID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete occupancy inspeciton--probably because another" + "part of the database has a reference item.", ex);
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

    public void updateOccSpace(OccSpace oc) throws IntegrationException {

        String query = "UPDATE public.occspace\n"
                + " SET spaceid=?, name=?, spacetype_id=?, required=?, description=?"
                + " WHERE spaceid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, oc.getSpaceID());
            stmt.setString(2, oc.getName());
            stmt.setInt(3, oc.getOccSpaceTypeID());
            stmt.setBoolean(4, oc.isRequired());
            stmt.setString(5, oc.getDescription());
            stmt.setInt(6, oc.getSpaceID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Could not update space", ex);

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
    }
    
    public void detachElement(int spaceid) throws IntegrationException {
        String query = "DELETE FROM public.occspaceelement\n"
                + " WHERE space_id= ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, spaceid);
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot detach element--probably because another"
                    + "part of the database has a reference item.", ex);

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
    
     public OccSpaceTypeInspectionDirective getOccInspecTemplateSpaceType(int spacetypeid) throws IntegrationException {

        String query = "    SELECT checklistspacetypeid, checklist_id, required, \n"
                + "       spacetype_typeid, overridespacetyperequired, overridespacetyperequiredvalue, \n"
                + "       overridespacetyperequireallspaces\n"
                + "  FROM public.occchecklistspacetype WHERE spacetype_typeid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        OccSpaceTypeInspectionDirective spacetype= null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, spacetypeid);
            rs = stmt.executeQuery();
            while (rs.next()) {
                spacetype = generateOccSpaceTypeInspectionDirective(rs);

            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get space type", ex);

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

        return spacetype;
    }


}
