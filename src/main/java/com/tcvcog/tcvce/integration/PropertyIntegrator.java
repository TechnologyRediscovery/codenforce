/*
 * Copyright (C) 2017 Turtle Creek Valley Council of Governments
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

import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitChangeOrder;
import com.tcvcog.tcvce.entities.PropertyUnitDataHeavy;
import com.tcvcog.tcvce.entities.PropertyUnitWithProp;
import com.tcvcog.tcvce.entities.search.SearchParamsProperty;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.PropertyExtData;
import com.tcvcog.tcvce.entities.PropertyUseType;
import com.tcvcog.tcvce.entities.TaxStatus;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
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
 *
 * @author Eric Darsow
 */
public class PropertyIntegrator extends BackingBeanUtils implements Serializable {

    final int MAX_RESULTS = 100;
    final String ACTIVE_FIELD = "property.active";

    /**
     * Creates a new instance of PropertyIntegrator
     */
    public PropertyIntegrator() {

    }
    
    
    public Property getProperty(int propertyID) throws IntegrationException {
        Property p = new Property();
        PropertyCoordinator pc = getPropertyCoordinator();
        String query = "SELECT propertyid, municipality_municode, parid, lotandblock, address, \n" +
                        "       usegroup, constructiontype, countycode, notes, addr_city, addr_state, \n" +
                        "       addr_zip, ownercode, propclass, lastupdated, lastupdatedby, locationdescription, \n" +
                        "       bobsource_sourceid, unfitdatestart, unfitdatestop, unfitby_userid, \n" +
                        "       abandoneddatestart, abandoneddatestop, abandonedby_userid, vacantdatestart, \n" +
                        "       vacantdatestop, vacantby_userid, condition_intensityclassid, \n" +
                        "       landbankprospect_intensityclassid, landbankheld, active, nonaddressable, \n" +
                        "       usetype_typeid, creationts \n" +
                        "  FROM public.property WHERE propertyid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, propertyID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                p = generateProperty(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getProperty | Unable to retrieve property by ID number", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return pc.configureProperty(p);

    } // close getProperty()
  
    
    

    /**
     * Utility method for property search methods whose individual SQL
     * statements implement various search features. These methods can send
     * properly configured (i.e. cursor positioned) ResultSet objects to this
     * method and get back a populated Property object
     *
     * @param rs
     * @return the fully baked Property with all fields set from DB data
     */
    private Property generateProperty(ResultSet rs) throws IntegrationException {

        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        OccInspectionIntegrator ci = getOccInspectionIntegrator();
        SystemIntegrator si = getSystemIntegrator();
        UserIntegrator ui = getUserIntegrator();
        PropertyCoordinator pc = getPropertyCoordinator();

        Property p = new Property();

        try {
            p.setPropertyID(rs.getInt("propertyid"));
            p.setMuni(mi.getMuni(rs.getInt("municipality_muniCode")));
            p.setMuniCode(rs.getInt("municipality_muniCode")); // for legacy compat
            
            p.setParID(rs.getString("parid"));
            p.setLotAndBlock(rs.getString("lotandblock"));
            p.setAddress(rs.getString("address"));

            p.setUseGroup(rs.getString("usegroup"));
            p.setConstructionType(rs.getString("constructiontype"));
            p.setCountyCode(rs.getString("countycode"));
            p.setNotes(rs.getString("notes"));
            p.setAddress_city(rs.getString("addr_city"));
            
            p.setAddress_state(rs.getString("addr_state"));
            p.setAddress_state(rs.getString("addr_zip"));
            p.setOwnerCode(rs.getString("ownercode"));  // for legacy compat
            p.setPropclass(rs.getString("propclass"));
            
            if(rs.getTimestamp("lastupdated") != null){
                p.setLastUpdatedTS(rs.getTimestamp("lastupdated").toLocalDateTime());
            }
            
            if(rs.getInt("locationdescription") != 0){
                p.setLocationDescriptor(ci.getLocationDescriptor(rs.getInt("locationdescription")));
            }
            
            if(rs.getInt("bobsource_sourceid") != 0){
                p.setBobSource(si.getBOBSource(rs.getInt("bobsource_sourceid")));
            }
            
            if(rs.getTimestamp("unfitdatestart") != null){
                p.setUnfitDateStart(rs.getTimestamp("unfitdatestart").toLocalDateTime());
            }
            
            if(rs.getTimestamp("unfitdatestop") != null){
                p.setUnfitDateStop(rs.getTimestamp("unfitdatestop").toLocalDateTime());
            }
            
            if(rs.getInt("unfitby_userid") != 0){
                p.setUnfitBy(ui.getUser(rs.getInt("unfitby_userid")));
            }
                
            if(rs.getTimestamp("abandoneddatestart") != null){
                p.setAbandonedDateStart(rs.getTimestamp("abandoneddatestart").toLocalDateTime());
            }
            
            if(rs.getTimestamp("abandoneddatestop") != null){
                p.setAbandonedDateStop(rs.getTimestamp("abandoneddatestop").toLocalDateTime());
            }
            
            if(rs.getInt("abandonedby_userid") != 0){
                p.setAbandonedBy(ui.getUser(rs.getInt("abandonedby_userid")));
            }
            
            if(rs.getTimestamp("vacantdatestart") != null){
                p.setVacantDateStart(rs.getTimestamp("vacantdatestart").toLocalDateTime());
            }
            
            if(rs.getTimestamp("vacantdatestop") != null){
                p.setVacantDateStop(rs.getTimestamp("vacantdatestop").toLocalDateTime());
            }
            
            if(rs.getTimestamp("creationts") != null){
                p.setCreationTS(rs.getTimestamp("creationts").toLocalDateTime());
            }
            
            if(rs.getInt("vacantby_userid") != 0){
                p.setVacantBy(ui.getUser(rs.getInt("vacantby_userid")));
            }
            
            if(rs.getInt("condition_intensityclassid") != 0){
                p.setCondition(si.getIntensityClass(rs.getInt("condition_intensityclassid")));
            }
            
            if(rs.getInt("landbankprospect_intensityclassid") != 0){
                p.setLandBankProspect(si.getIntensityClass(rs.getInt("landbankprospect_intensityclassid")));
            }
            
            p.setLandBankHeld(rs.getBoolean("landbankheld"));
            p.setActive(rs.getBoolean("active"));
            p.setNonAddressable(rs.getBoolean("nonaddressable"));
            
            if(rs.getInt("usetype_typeid") != 0){
                p.setUseType(getPropertyUseType(rs.getInt("usetype_typeid")));
            }
            
            p.setUnitList(getPropertyUnitList(p));
            
            if(p.getUnitList() != null && p.getUnitList().isEmpty()){
                System.out.println("PropertyIntegrator.generateProperty | inserting new unit");
                PropertyUnit pu = pc.initPropertyUnit(p);
                pu.setPropertyID(p.getPropertyID());
                insertPropertyUnit(pu);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error generating Property from ResultSet", ex);
        }
        return p;
    }
    

    /**
     * Hope to deprecate in deference to the new search system? ECD on 19JUL
     * @param houseNum
     * @param street
     * @param muniID
     * @return
     * @throws IntegrationException 
     */
    public List<Property> searchForChangedProperties(String houseNum, String street, int muniID) throws IntegrationException {
        String query = "SELECT DISTINCT\n"
                + "	   propertyid, unit_unitid, municipality_municode, parid, lotandblock, address,\n"
                + "        propertyusetype, usegroup, constructiontype, countycode, \n"
                + "        property.notes, addr_city, addr_state, addr_zip, ownercode, propclass,\n"
                + "        lastupdated, lastupdatedby, locationdescription, datasource,\n"
                + "        containsrentalunits, vacant \n"
                + "FROM \n"
                + "	property,\n"
                + "	propertyunitchange\n"
                + "	\n"
                + "WHERE \n"
                + "	address ILIKE ? AND municipality_muniCode=?  AND propertyid = property_propertyid AND propertyunitchange.inactive is null AND propertyunitchange.approvedon IS null;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        List<Property> propList = new ArrayList<>();

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, "%" + houseNum + "%" + street + "%");
            stmt.setInt(2, muniID);
            rs = stmt.executeQuery();
            System.out.println("PropertyIntegrator.searchForProperties - with muni | sql: " + stmt.toString());
            int counter = 0;
            while (rs.next() && counter <= MAX_RESULTS) {
                propList.add(generateProperty(rs));
                counter++;
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error searching for properties", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return propList;

    }


    /**
     * Injects a property into the property table
     * @param prop
     * @return
     * @throws IntegrationException 
     */
    public int insertProperty(Property prop) throws IntegrationException {
        String query = "INSERT INTO public.property(\n" +
                        "            propertyid, municipality_municode, parid, lotandblock, address, \n" +
                        "            usegroup, constructiontype, countycode, notes, addr_city, \n" +
                        "            addr_state, addr_zip, ownercode, propclass, lastupdated, lastupdatedby, \n" +
                        "            locationdescription, bobsource_sourceid, unfitdatestart, unfitdatestop, \n" +
                        "            unfitby_userid, abandoneddatestart, abandoneddatestop, abandonedby_userid, \n" +
                        "            vacantdatestart, vacantdatestop, vacantby_userid, condition_intensityclassid, \n" +
                        "            landbankprospect_intensityclassid, landbankheld, active, nonaddressable, \n" +
                        "            usetype_typeid, creationts)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, \n" + // 1-4
                        "            ?, ?, ?, ?, ?, \n" + // 5-9
                        "            ?, ?, ?, ?, now(), ?, \n" + // 10-14
                        "            ?, ?, ?, ?, \n" + // 15-18
                        "            ?, ?, ?, ?, \n" + // 19-22
                        "            ?, ?, ?, ?, \n" + // 23-26
                        "            ?, ?, ?, ?, \n" + // 27-30
                        "            ?, now());"; // im so sorry  // 31

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        int lastID;

        try {
            stmt = con.prepareStatement(query);

            stmt.setInt(1, prop.getMuniCode());
            stmt.setString(2, prop.getParID());
            stmt.setString(3, prop.getLotAndBlock());
            stmt.setString(4, prop.getAddress());
            
            stmt.setString(5, prop.getUseGroup());
            stmt.setString(6, prop.getConstructionType());
            stmt.setString(7, prop.getCountyCode());
            stmt.setString(8, prop.getNotes());
            stmt.setString(9, prop.getAddress_city());
            
            stmt.setString(10, prop.getAddress_state());
            stmt.setString(11, prop.getAddress_zip());
            stmt.setString(12, prop.getOwnerCode());
            stmt.setString(13, prop.getPropclass());
            if(prop.getLastUpdatedBy() != null){
                stmt.setInt(14, prop.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(14, java.sql.Types.NULL);
            }
            
            // line break
            if(prop.getLocationDescriptor() != null){
                stmt.setInt(15, prop.getLocationDescriptor().getLocationID());
            } else {
                stmt.setNull(15, java.sql.Types.NULL);
            }
            if(prop.getBobSource()!= null){
                stmt.setInt(16, prop.getBobSource().getSourceid());
            } else {
                stmt.setNull(16, java.sql.Types.NULL);
            }
            if(prop.getUnfitDateStart() != null){
                stmt.setTimestamp(17, java.sql.Timestamp.valueOf(prop.getUnfitDateStart()));
            } else {
                stmt.setNull(17, java.sql.Types.NULL);
            }
            if(prop.getUnfitDateStop() != null){
                stmt.setTimestamp(18, java.sql.Timestamp.valueOf(prop.getUnfitDateStop()));
            } else {
                stmt.setNull(18, java.sql.Types.NULL);
            }
            
            
            if(prop.getUnfitBy() != null){
                stmt.setInt(19, prop.getUnfitBy().getUserID());
            } else {
                stmt.setNull(19, java.sql.Types.NULL);
            }
            
            if(prop.getAbandonedDateStart()!= null){
                stmt.setTimestamp(20, java.sql.Timestamp.valueOf(prop.getAbandonedDateStart()));
            }else {
                stmt.setNull(20, java.sql.Types.NULL);
            }
            
            if(prop.getAbandonedDateStop()!= null){
                stmt.setTimestamp(21, java.sql.Timestamp.valueOf(prop.getAbandonedDateStop()));
            }else {
                stmt.setNull(21, java.sql.Types.NULL);
            }
            
            if(prop.getAbandonedBy()!= null){
                stmt.setInt(22, prop.getAbandonedBy().getUserID());
            } else {
                stmt.setNull(22, java.sql.Types.NULL);
            }
            
            if(prop.getVacantDateStart()!= null){
                stmt.setTimestamp(23, java.sql.Timestamp.valueOf(prop.getVacantDateStart()));
            }else {
                stmt.setNull(23, java.sql.Types.NULL);
            }
            if(prop.getVacantDateStop()!= null){
                stmt.setTimestamp(24, java.sql.Timestamp.valueOf(prop.getVacantDateStop()));
            }else {
                stmt.setNull(24, java.sql.Types.NULL);
            }
            if(prop.getVacantBy()!= null){
                stmt.setInt(25, prop.getVacantBy().getUserID());
            } else {
                stmt.setNull(25, java.sql.Types.NULL);
            }

            if(prop.getCondition()!= null){
                stmt.setInt(26, prop.getCondition().getClassID());
            } else {
                stmt.setNull(26, java.sql.Types.NULL);
            }
            

            if(prop.getLandBankProspect()!= null){
                stmt.setInt(27, prop.getLandBankProspect().getClassID());
            } else {
                stmt.setNull(27, java.sql.Types.NULL);
            }
            
            stmt.setBoolean(28, prop.isLandBankHeld());
            stmt.setBoolean(29, prop.isActive());
            stmt.setBoolean(30, prop.isNonAddressable());
            
            if(prop.getUseType() != null){
                stmt.setInt(31, prop.getUseType().getTypeID());
            } else {
                stmt.setNull(31, java.sql.Types.NULL);
            }

            stmt.execute();

            // grab the newly inserted propertyid
            String idNumQuery = "SELECT currval('propertyid_seq');";
            Statement s = con.createStatement();
            ResultSet rs;
            rs = s.executeQuery(idNumQuery);
            rs.next();
            lastID = rs.getInt(1);

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error inserting property. ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            
        } // close finally
    return lastID;

    }


    public String updateProperty(Property prop) throws IntegrationException {
        String query =  "UPDATE public.property\n" +
                        "   SET municipality_municode=?, parid=?, lotandblock=?, \n" +  // 1-3
                        "       address=?, usegroup=?, constructiontype=?, countycode=?, notes=?, \n" + // 4-8
                        "       addr_city=?, addr_state=?, addr_zip=?, ownercode=?, propclass=?, \n" + // 9-13
                        "       lastupdated=?, lastupdatedby=?, " + // 14-15
                        "       locationdescription=?, bobsource_sourceid=?, unfitdatestart=?, " + // 16-18
                        "       unfitdatestop=?, unfitby_userid=?, abandoneddatestart=?, \n" + // 19-21
                        "       abandoneddatestop=?, abandonedby_userid=?, vacantdatestart=?, \n" + // 22-24
                        "       vacantdatestop=?, vacantby_userid=?, condition_intensityclassid=?, \n" + // 25-27
                        "       landbankprospect_intensityclassid=?, landbankheld=?, active=?, \n" + // 28-30
                        "       nonaddressable=?, usetype_typeid=? \n" + // 31 -33
                        " WHERE propertyid=?;"; // 34

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, prop.getMuniCode());
            stmt.setString(2, prop.getParID());
            stmt.setString(3, prop.getLotAndBlock());
            
            // new SQL line
            stmt.setString(4, prop.getAddress());
            stmt.setString(5, prop.getUseGroup());
            stmt.setString(6, prop.getConstructionType());
            stmt.setString(7, prop.getCountyCode());
            stmt.setString(8, prop.getNotes());
            
            // new SQL line
            stmt.setString(9, prop.getAddress_city());
            stmt.setString(10, prop.getAddress_state());
            stmt.setString(11, prop.getAddress_zip());
            stmt.setString(12, prop.getOwnerCode());
            stmt.setString(13, prop.getPropclass());
            
            // new SQL line
            if(prop.getLastUpdatedTS() != null){
                stmt.setTimestamp(14, java.sql.Timestamp.valueOf(prop.getLastUpdatedTS()));
            } else {
                stmt.setTimestamp(14, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            }
            
             if(prop.getLastUpdatedBy() != null){
                stmt.setInt(15, prop.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(15, java.sql.Types.NULL);
            }
            
            // new SQL line
            if(prop.getLocationDescriptor() != null){
                stmt.setInt(16, prop.getLocationDescriptor().getLocationID());
            } else {
                stmt.setNull(16, java.sql.Types.NULL);
            }
            
            if(prop.getBobSource()!= null){
                stmt.setInt(17, prop.getBobSource().getSourceid());
            } else {
                stmt.setNull(17, java.sql.Types.NULL);
            }
            
            if(prop.getUnfitDateStart() != null){
                stmt.setTimestamp(18, java.sql.Timestamp.valueOf(prop.getUnfitDateStart()));
                System.out.println("***************************************");
                System.out.print(java.sql.Timestamp.valueOf(prop.getUnfitDateStart()));
                System.out.println(java.sql.Timestamp.valueOf(prop.getUnfitDateStart()).getClass());
            } else {
                stmt.setNull(18, java.sql.Types.NULL);
            }
            
            // new SQL line
            if(prop.getUnfitDateStop() != null){
                stmt.setTimestamp(19, java.sql.Timestamp.valueOf(prop.getUnfitDateStop()));
            } else {
                stmt.setNull(19, java.sql.Types.NULL);
            }
            
            if(prop.getUnfitBy() != null){
                stmt.setInt(20, prop.getUnfitBy().getUserID());
            } else {
                stmt.setNull(20, java.sql.Types.NULL);
            } 
            
            if(prop.getAbandonedDateStart()!= null){
                stmt.setTimestamp(21, java.sql.Timestamp.valueOf(prop.getAbandonedDateStart()));
            } else {
                stmt.setNull(21, java.sql.Types.NULL);
            } 
            
            // new SQL line
            
            if(prop.getAbandonedDateStop()!= null){
                stmt.setTimestamp(22, java.sql.Timestamp.valueOf(prop.getAbandonedDateStop()));
            } else {
                stmt.setNull(22, java.sql.Types.NULL);
            } 
            
            if(prop.getAbandonedBy()!= null){
                stmt.setInt(23, prop.getAbandonedBy().getUserID());
            } else {
                stmt.setNull(23, java.sql.Types.NULL);
            }
            
            if(prop.getVacantDateStart()!= null){
                stmt.setTimestamp(24, java.sql.Timestamp.valueOf(prop.getVacantDateStart()));
            } else {
                stmt.setNull(24, java.sql.Types.NULL);
            } 
            
            
            // new SQL line
            if(prop.getVacantDateStop()!= null){
                stmt.setTimestamp(25, java.sql.Timestamp.valueOf(prop.getVacantDateStop()));
            } else {
                stmt.setNull(25, java.sql.Types.NULL);
            } 
            
            if(prop.getVacantBy()!= null){
                stmt.setInt(26, prop.getVacantBy().getUserID());
            } else {
                stmt.setNull(26, java.sql.Types.NULL);
            }

            if(prop.getCondition()!= null){
                stmt.setInt(27, prop.getCondition().getClassID());
            } else {
                stmt.setNull(27, java.sql.Types.NULL);
            }
            

            // new SQL line
            if(prop.getLandBankProspect()!= null){
                stmt.setInt(28, prop.getLandBankProspect().getClassID());
            } else {
                stmt.setNull(28, java.sql.Types.NULL);
            }
            
            stmt.setBoolean(29, prop.isLandBankHeld());
            stmt.setBoolean(30, prop.isActive());
            
            // new SQL line
            stmt.setBoolean(31, prop.isNonAddressable());
            
            if(prop.getUseType() != null){
                stmt.setInt(32, prop.getUseType().getTypeID());
            } else {
                stmt.setNull(32, java.sql.Types.NULL);
            }
            
            stmt.setInt(33, prop.getPropertyID());
            
            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update property", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            System.out.println("Property ID = " + prop.getPropertyID());
            System.out.println("Connection closed and update hopefully saved.");
        } // close finally
        return "propertyProfile";
    }
    
    
     /**
     * Returns a full table dump of PropertyUseType entries
     * @param p the property containing the new note value. Client must append note properly
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void updatePropertyNoteFieldOnly(Property p) throws IntegrationException{
        String query = "UPDATE public.property SET notes=? WHERE propertyid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, p.getNotes());
            stmt.setInt(2, p.getPropertyID());
            
            stmt.executeUpdate();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error searching for properties", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    /**
     * Delete equivalent: marks active to false on property with given ID
     * @param propID
     * @throws IntegrationException 
     */
    public void inactivateProperty(int propID) throws IntegrationException{
      String query =  "UPDATE public.property\n" +
                        "   SET active=? WHERE propertyid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setBoolean(1, false);
            stmt.setInt(2, propID);
            
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getProperty | Unable to retrieve property by ID number", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }

   

    /**
     * Single entry point for searches against the property table. Since 
     * my return type is Integer, It's the job of
     * the caller to iterate over the result list and make real objects
     * 
     * @param params
     * @return List of property IDs
     * @throws IntegrationException 
     */
    public List<Integer> searchForProperties(SearchParamsProperty params) throws IntegrationException {
        SearchCoordinator sc = getSearchCoordinator();
        
        List<Integer> propIDList = new ArrayList<>();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        params.appendSQL("SELECT DISTINCT propertyid ");
        params.appendSQL("FROM property LEFT OUTER JOIN propertyexternaldata ON (property.propertyid = propertyexternaldata.property_propertyid) \n");
        
        if(params.isPerson_ctl()){
            params.appendSQL("LEFT OUTER JOIN propertyperson ON (property.propertyid = propertyperson.property_propertyid) \n");
        }
        
        params.appendSQL("WHERE propertyid IS NOT NULL ");
        
        // **********************************
        // **    FILTER COM-4 OBJECT ID     **
        // ***********************************
         if (!params.isBobID_ctl()) {
           
            //******************************************************************
           // **   FILTERS COM-1, COM-2, COM-3, COM-6 MUNI,DATES,USER,ACTIVE  **
           // ******************************************************************
             params = (SearchParamsProperty) sc.assembleBObSearchSQL_muniDatesUserActive(
                                                                        params, 
                                                                        SearchParamsProperty.MUNI_DBFIELD,
                                                                        ACTIVE_FIELD);

            //**************************************
           // **   FILTER PROP-1   ZIP            **
           // **************************************
            if (params.isZip_ctl()) {
                params.appendSQL("AND addr_zip ILIKE ? ");
            }

            //***************************************
           // **   FILTER PROP-2:  LOT AND BLOCK   **
           // ***************************************
            if (params.isLotblock_ctl()) {
                params.appendSQL("AND lotandblock LIKE ? ");
            }

            //*****************************************
           // **   FILTER PROP-3: BOB SOURCE         **
           // *****************************************
            if (params.isBobSource_ctl()) {
                if(params.getBobSource_val() != null){
                    params.appendSQL("AND bobsource_sourceid=? ");
                } else {
                    params.setBobSource_ctl(false);
                    params.appendToParamLog("SOURCE: no BOb source object; source filter disabled");
                }
            }

            //****************************
           // **   4:PARCEL ID          **
           // ****************************
            if (params.isParcelid_ctl()) {
                params.appendSQL("AND parid ILIKE ? ");
            }

           // *****************************
           // **       5: ADDRESS        **
           // *****************************
            if (params.isAddress_ctl()) {
                params.appendSQL("AND address ILIKE ? ");
            }

           // ****************************
           // **       6:CONDITION      **
           // ****************************
            if (params.isCondition_ctl()) {
                if(params.getCondition_intensityClass_val() != null){
                    params.appendSQL("AND condition_intensityclassid=? ");
                } else {
                    params.setCondition_ctl(false);
                    params.appendToParamLog("CONDITION: no condition object; condition filter disabled; |");
                }
            }

           // ****************************
           // ** 7:LAND BANK PROSPECT   **
           // ****************************
            if (params.isLandbankprospect_ctl()) {
                if(params.getLandbankprospect_intensityClass_val() != null){
                    params.appendSQL("AND landbankprospect_intensityclassid=? ");
                } else {
                    params.setLandbankprospect_ctl(false);
                    params.appendToParamLog("LANDBANKPROSPECT: No intensity object; land bank prospect disabled; |");
                }
            }

           // ****************************
           // ** 8:LAND BANK HELD  **
           // ****************************
            if (params.isLandbankheld_ctl()) {
                params.appendSQL("AND landbankheld= ");
                if (params.isActive_val()) {
                    params.appendSQL("TRUE ");
                } else {
                    params.appendSQL("FALSE ");
                }
            }

           // ****************************
           // ** 9:NONADDRESSABLE       **
           // ****************************
            if (params.isNonaddressable_ctl()) {
                params.appendSQL("AND nonaddressable= ");
                if (params.isActive_val()) {
                    params.appendSQL("TRUE ");
                } else {
                    params.appendSQL("FALSE ");
                }
            }

           // ****************************
           // **    10: USE TYPE        **
           // ****************************
            if (params.isUseType_ctl()) {
                if(params.getUseType_val() != null){
                    params.appendSQL("AND property.usetype_typeid=? ");
                } else {
                    params.setUseType_ctl(false);
                    params.appendToParamLog("USE TYPE: No intensity object found; use type filter disabled; | " );
                }
            }

           // ****************************
           // **     11:ZONE            **
           // ****************************
            if (params.isZoneClass_ctl()) {
                params.appendSQL("AND propertyusetype.zoneclass ILIKE ? ");
            }
            
           // ****************************
           // **     12:TAX STATUS      **
           // ****************************
            if(params.isTaxStatus_ctl()){
                params.appendSQL("AND taxstatus_taxstatusid=?");
            }

           // ****************************
           // **     13:PROP VALUE      **
           // ****************************
            if (params.isPropValue_ctl()) {
                params.appendSQL("AND propertyexternaldata.assessedlandvalue+propertyexternaldata.assessedlandvalue>? ");
                params.appendSQL("AND propertyexternaldata.assessedlandvalue+propertyexternaldata.assessedlandvalue<? ");
            }

           // ****************************
           // **   14:CONSTRUCTION YEAR   **
           // ****************************
            if (params.isConstructionYear_ctl()) {
                params.appendSQL("AND propertyexternaldata.yearbuilt>? ");
                params.appendSQL("AND propertyexternaldata.yearbuilt<? ");
            }

           // ****************************
           // **   15:PERSON            **
           // ****************************
            if (params.isPerson_ctl()) {
                if(params.getPerson_val() != null){
                    params.appendSQL("AND propertyperson.person_personid=? ");
                } else {
                    params.setPerson_ctl(false);
                    params.appendToParamLog("PERSON: No object found; filter disabled; | " );
                }
            }
           
        // ****************************
        // ** COM-4  OBJECT ID       **
        // **************************** 
        } else {
            params.appendSQL("AND propertyid=? "); // will be param 2 with ID search
        }
        params.appendSQL(";");
        int paramCounter = 0;
        StringBuilder str = null;

        try {
            stmt = con.prepareStatement(params.extractRawSQL());

            if (!params.isBobID_ctl()) {
                
                if (params.isMuni_ctl()) {
                     stmt.setInt(++paramCounter, params.getMuni_val().getMuniCode());
                }
                
                if(params.isDate_startEnd_ctl()){
                    stmt.setTimestamp(++paramCounter, params.getDateStart_val_sql());
                    stmt.setTimestamp(++paramCounter, params.getDateEnd_val_sql());
                 }
                
                if (params.isUser_ctl()) {
                   stmt.setInt(++paramCounter, params.getUser_val().getUserID());
                }
                
                if (params.isZip_ctl()) {
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getZoneClass_val());
                    str.append("%");
                    stmt.setString(++paramCounter, str.toString());
                }
                
                if (params.isLotblock_ctl()) {
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getLotblock_val());
                    str.append("%");
                    stmt.setString(++paramCounter, str.toString());
                }
                if (params.isBobSource_ctl()) {
                    stmt.setInt(++paramCounter, params.getBobSource_val().getSourceid());
                }
                if (params.isParcelid_ctl()) {
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getParcelid_val());
                    str.append("%");
                    stmt.setString(++paramCounter, str.toString());
                    
                }
                if (params.isAddress_ctl()) {
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getAddress_val());
                    str.append("%");
                    stmt.setString(++paramCounter, str.toString());
                }
                if (params.isCondition_ctl()) {
                    stmt.setInt(++paramCounter, params.getCondition_intensityClass_val().getClassID());
                }
                if (params.isLandbankprospect_ctl()) {
                    stmt.setInt(++paramCounter, params.getLandbankprospect_intensityClass_val().getClassID());
                }
                if (params.isUseType_ctl()) {
                    stmt.setInt(++paramCounter, params.getUseType_val().getTypeID());
                }
                if (params.isZoneClass_ctl()) {
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getZip_val());
                    str.append("%");
                    stmt.setString(++paramCounter, str.toString());
                }
                
                if(params.isTaxStatus_ctl()){
                    stmt.setInt(++paramCounter, params.getTaxStatus_val());
                }
                if (params.isPropValue_ctl()) {
                    stmt.setInt(++paramCounter, params.getPropValue_min_val());
                    stmt.setInt(++paramCounter, params.getPropValue_max_val());
                }
                if (params.isConstructionYear_ctl()) {
                    stmt.setInt(++paramCounter, params.getConstructionYear_min_val());
                    stmt.setInt(++paramCounter, params.getConstructionYear_max_val());
                }
                if (params.isDate_startEnd_ctl()) {
                    stmt.setTimestamp(++paramCounter, params.getDateStart_val_sql());
                    stmt.setTimestamp(++paramCounter, params.getDateEnd_val_sql());
                }
                if (params.isPerson_ctl()){
                    stmt.setInt(++paramCounter, params.getPerson_val().getPersonID());
                }
            } else {
                stmt.setInt(++paramCounter, params.getBobID_val());
            }
            
            params.appendToParamLog("Property Integrator SQL before execution: ");
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
                propIDList.add(rs.getInt("propertyid"));
                counter++;
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot search for properties, sorry!", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return propIDList;
    }


   

    /**
     * Dumps all Property records for a given User and lets the caller sort through them
     * @param cred
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> getPropertyHistoryList(Credential cred) throws IntegrationException {
        List<Integer> propList = new ArrayList<>();
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String s = "SELECT property_propertyid, entrytimestamp FROM loginobjecthistory "
                    + "WHERE login_userid = ? "
                    + "AND property_propertyid IS NOT NULL "
                    + "ORDER BY entrytimestamp DESC;";
            stmt = con.prepareStatement(s);
            stmt.setInt(1, cred.getGoverningAuthPeriod().getUserID());

            rs = stmt.executeQuery();

            // are we too cool for for-loops?
            while (rs.next()) {
                propList.add((rs.getInt("property_propertyid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate property history list", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        System.out.println("PropertyIntegrator.getPropertyHistoryList | returning list with size " + propList.size());
        return propList;
    }
    
    
    
    // *************************************************************************
    // **               PROPERTY UNIT STUFF                                   **
    // *************************************************************************
    
    
     public PropertyUnit getPropertyUnit(int propUnitID) throws IntegrationException {
        PropertyUnit pu = null;
        String query =  "SELECT unitid, unitnumber, property_propertyid, otherknownaddress, notes, \n" +
                        "       rentalintentdatestart, rentalintentdatestop, rentalintentlastupdatedby_userid, \n" +
                        "       rentalnotes, active, condition_intensityclassid, lastupdatedts\n" +
                        "  FROM public.propertyunit WHERE unitid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, propUnitID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                pu = generatePropertyUnit(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("PropertyIntegrator.getPropertyUnit | Unable to get property unit, ", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }

        return pu;
    }

     
     

      private PropertyUnit generatePropertyUnit(ResultSet rs) throws SQLException, IntegrationException {
        PropertyUnit pu = new PropertyUnit();
        UserIntegrator ui = new UserIntegrator();
        
        pu.setUnitID(rs.getInt("unitid"));
        pu.setUnitNumber(rs.getString("unitnumber"));
        pu.setPropertyID(rs.getInt("property_propertyid"));
        pu.setOtherKnownAddress(rs.getString("otherknownaddress"));
        pu.setNotes(rs.getString("notes"));
        
        if(rs.getTimestamp("rentalintentdatestart") != null){
            pu.setRentalIntentDateStart(rs.getTimestamp("rentalintentdatestart").toLocalDateTime());
        }
        if(rs.getTimestamp("rentalintentdatestop") != null){
            pu.setRentalIntentDateStop(rs.getTimestamp("rentalintentdatestop").toLocalDateTime());
        }
        pu.setRentalIntentLastUpdatedBy(ui.getUser(rs.getInt("rentalintentlastupdatedby_userid")));
        
        pu.setRentalNotes(rs.getString("rentalnotes"));
        pu.setActive(rs.getBoolean("active"));
        pu.setConditionIntensityClassID(rs.getInt("condition_intensityclassid"));
        if(rs.getTimestamp("lastupdatedts") != null){
            pu.setLastUpdatedTS(rs.getTimestamp("lastupdatedts").toLocalDateTime());
        }
        
        return pu;
    }
    
    public int insertPropertyUnit(PropertyUnit pu) throws IntegrationException {
        String query =  "INSERT INTO public.propertyunit(\n" +
                        "            unitid, unitnumber, property_propertyid, otherknownaddress, notes, \n" +
                        "            rentalintentdatestart, rentalintentdatestop, rentalintentlastupdatedby_userid, \n" +
                        "            rentalnotes, active, condition_intensityclassid, lastupdatedts)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?, ?, ?, now());";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        int lastID = 0;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, pu.getUnitNumber());
            stmt.setInt(2, pu.getPropertyID());
            stmt.setString(3, pu.getOtherKnownAddress());
            stmt.setString(4, pu.getNotes());
            
            if(pu.getRentalIntentDateStart() != null){
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(pu.getRentalIntentDateStart()));
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            if(pu.getRentalIntentDateStop() != null){
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(pu.getRentalIntentDateStop()));
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            if(pu.getRentalIntentLastUpdatedBy() != null){
                stmt.setInt(7, pu.getRentalIntentLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            stmt.setString(8, pu.getRentalNotes());
            stmt.setBoolean(9, pu.isActive());

            if(pu.getConditionIntensityClassID() != 0){
                stmt.setInt(10, pu.getConditionIntensityClassID());
            } else {
                stmt.setNull(10, java.sql.Types.NULL);
            }
            
            stmt.execute();
            
            // grab the newly inserted propertyid
            String idNumQuery = "SELECT currval('propertunit_unitid_seq');";
            Statement s = con.createStatement();
            ResultSet rs;
            rs = s.executeQuery(idNumQuery);
            rs.next();
            lastID = rs.getInt(1);
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getProperty | Unable to retrieve property by ID number", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return lastID;
    }
    
     public void updatePropertyUnit(PropertyUnit pu) throws IntegrationException {
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        String sql =  "UPDATE public.propertyunit\n" +
                        "   SET unitnumber=?, property_propertyid=?, otherknownaddress=?, \n" +
                        "       notes=?, rentalintentdatestart=?, rentalintentdatestop=?, rentalintentlastupdatedby_userid=?, \n" +
                        "       rentalnotes=?, active=?, condition_intensityclassid=?, lastupdatedts=now() \n" +
                        " WHERE unitid=?;";

        try {
            stmt = con.prepareStatement(sql);
            stmt.setString(1, pu.getUnitNumber());
            stmt.setInt(2, pu.getPropertyID());
            stmt.setString(3, pu.getOtherKnownAddress());
            stmt.setString(4, pu.getNotes());
            
             if(pu.getRentalIntentDateStart() != null){
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(pu.getRentalIntentDateStart()));
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            if(pu.getRentalIntentDateStop() != null){
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(pu.getRentalIntentDateStop()));
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            if(pu.getRentalIntentLastUpdatedBy() != null){
                stmt.setInt(7, pu.getRentalIntentLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            stmt.setString(8, pu.getRentalNotes());
            stmt.setBoolean(9, pu.isActive());
            if(pu.getConditionIntensityClassID() != 0){
                stmt.setInt(10, pu.getConditionIntensityClassID());
            } else {
                stmt.setNull(10, java.sql.Types.NULL);
            }
            
            stmt.setInt(11, pu.getUnitID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate property history list", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }

    public List<PropertyUnit> getPropertyUnitList(Property p) throws IntegrationException {
        List<PropertyUnit> unitList = new ArrayList();

        String query = "SELECT unitid FROM propertyunit WHERE property_propertyid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, p.getPropertyID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                unitList.add(getPropertyUnit(rs.getInt("unitid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build property unit list due to an DB integration error", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return unitList;
    }

  
    
    /**
     * Adaptor method for calling getPropertyUnitWithLists(int unitID) given a PropertyUnit object
     * 
     * @param pu
     * @return a PropertyUnit containing a list of OccPeriods, and more in the future
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.EventException 
     * @throws com.tcvcog.tcvce.domain.AuthorizationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public PropertyUnitDataHeavy getPropertyUnitWithLists(PropertyUnit pu) throws IntegrationException, EventException, AuthorizationException, BObStatusException{
        PropertyUnitDataHeavy puwl = null;
        try {
            puwl = getPropertyUnitWithLists(pu.getUnitID());
        } catch (ViolationException ex) {
            System.out.println(ex);
        }
        return puwl;
    }
    
    public PropertyUnitDataHeavy getPropertyUnitWithLists(int unitID) throws IntegrationException, EventException, AuthorizationException, BObStatusException, ViolationException{
        OccupancyIntegrator oi = getOccupancyIntegrator();
        PropertyUnitDataHeavy puwl = new PropertyUnitDataHeavy(getPropertyUnit(unitID));
        puwl.setPeriodList(oi.getOccPeriodList(unitID));
        puwl.setChangeOrderList(getPropertyUnitChangeListAll(unitID));
        return puwl;
    }
    
    /**
     * Handy utility method for grabbing a PropertyUnit with its own embedded Property
     * for use in Inspection stuff and reports.
     * 
     * @param unitID
     * @return
     * @throws IntegrationException 
     */
    public PropertyUnitWithProp getPropertyUnitWithProp(int unitID) throws IntegrationException{
        PropertyIntegrator pi = getPropertyIntegrator();
        
        PropertyUnitWithProp puwp = new PropertyUnitWithProp(getPropertyUnit(unitID));
        puwp.setProperty(pi.getProperty(puwp.getPropertyID()));
        
        return puwp;
    }
    
    public void insertPropertyUnitChange(PropertyUnitChangeOrder uc) throws IntegrationException {
        String query = "INSERT INTO public.propertyunitchange(\n"
                + "            unitchangeid, unitnumber, propertyunit_unitid, otherknownaddress, notes, \n"
                + "            rentalnotes, removed, added, changedby_personid)\n"
                + "    VALUES (DEFAULT, ?, ?, ?, ?, \n"
                + "            ?, ?, ?, ?);";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, uc.getUnitNumber());
            stmt.setInt(2, uc.getUnitID());
            stmt.setString(3, uc.getOtherKnownAddress());
            stmt.setString(4, uc.getNotes());
            stmt.setString(5, uc.getRentalNotes());
            stmt.setBoolean(6, uc.isRemoved());
            stmt.setBoolean(7, uc.isAdded());
            stmt.setInt(8, uc.getChangedBy());

            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.insertPropertyUnitChange | Error inserting property unit change order", ex);
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

    public PropertyUnitChangeOrder getPropertyUnitChange(int unitChangeId) throws IntegrationException {
        PropertyUnitChangeOrder uc = new PropertyUnitChangeOrder();
        String query = "SELECT unitchangeid, propertyunit_unitid,\n"
                + "propertyunitchange.unitnumber, propertyunitchange.otherknownaddress,\n"
                + "propertyunitchange.notes, propertyunitchange.rentalnotes,\n"
                + "removed, added, changedby_personid, approvedondate, approvedby_userid, inactive\n"
                + "FROM propertyunitchange JOIN propertyunit ON propertyunitchange.unit_unitid = propertyunit.unitid\n"
                + "JOIN property ON propertyunit.property_propertyid = property.propertyid;"
                + " WHERE unitchangeid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, unitChangeId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                uc = generatePropertyUnitChange(rs);
            }
        } catch (SQLException ex) {

            System.out.println(ex);
            throw new IntegrationException("PropertyIntegrator.getPropertyUnitChange | Unable to get property unit, ", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }

        return uc;
    }

    public PropertyUnitChangeOrder generatePropertyUnitChange(ResultSet rs) throws SQLException, IntegrationException {
        UserIntegrator ui = getUserIntegrator();
        PropertyUnitChangeOrder uc = new PropertyUnitChangeOrder();
        uc.setUnitChangeID(rs.getInt("unitchangeid"));
        uc.setUnitID(rs.getInt("propertyunit_unitid"));
        uc.setUnitNumber(rs.getString("unitnumber"));
        uc.setNotes(rs.getString("notes"));
        uc.setOtherKnownAddress(rs.getString("otherknownaddress"));
        uc.setRentalNotes(rs.getString("rentalnotes"));
        uc.setAdded(rs.getBoolean("added"));
        uc.setRemoved(rs.getBoolean("removed"));
        uc.setApprovedOn(rs.getTimestamp("approvedondate"));
        uc.setApprovedBy(ui.getUser(rs.getInt("approvedby_userid")));
        uc.setChangedBy(rs.getInt("changedby_personid"));
        uc.setActive(rs.getBoolean("active"));
        return uc;
    }

    /**
     * 
     * @param changeToUpdate
     * @throws IntegrationException
     */
    public void updatePropertyUnitChange(PropertyUnitChangeOrder changeToUpdate) throws IntegrationException {
        String query = "UPDATE public.propertyunitchange\n"
                + "SET unitnumber=?, propertyunit_unitid=?, otherknownaddress=?, notes=?, rentalnotes=?,\n"
                + "removed=?, added=?, approvedondate=?, approvedby_userid=?, changedby_personid=?, active=?\n"
                + "WHERE unitchangeid = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);

            stmt.setString(1, changeToUpdate.getUnitNumber());
            stmt.setInt(2, changeToUpdate.getUnitID());
            stmt.setString(3, changeToUpdate.getOtherKnownAddress());
            stmt.setString(4, changeToUpdate.getNotes());
            stmt.setString(5, changeToUpdate.getRentalNotes());
            stmt.setBoolean(6, changeToUpdate.isRemoved());
            stmt.setBoolean(7, changeToUpdate.isAdded());
            stmt.setTimestamp(8, changeToUpdate.getApprovedOn());
            if(changeToUpdate.getApprovedBy() !=null){
            stmt.setInt(9, changeToUpdate.getApprovedBy().getUserID());
            } else{
            stmt.setNull(9, java.sql.Types.NULL);
            }
            stmt.setInt(10, changeToUpdate.getChangedBy());
            stmt.setBoolean(11, changeToUpdate.isActive());
            stmt.setInt(12, changeToUpdate.getUnitChangeID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update PropertyUnitChange", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            
        } // close finally

    }

    public List<PropertyUnitChangeOrder> getPropertyUnitChangeList(int propertyUnitID) throws IntegrationException {
        List<PropertyUnitChangeOrder> ucl = new ArrayList<>();
        String query = "SELECT\n"
                + "unitchangeid, propertyunit_unitid,\n"
                + "propertyunitchange.unitnumber, propertyunitchange.otherknownaddress,\n"
                + "propertyunitchange.notes, propertyunitchange.rentalnotes,\n"
                + "removed, added, entryts, approvedondate, changedby_personid, approvedby_userid, propertyunitchange.active\n"
                + "FROM\n"
                + "propertyunitchange\n"
                + "JOIN propertyunit ON propertyunitchange.propertyunit_unitid = propertyunit.unitid\n"
                + "WHERE unitid=? AND propertyunitchange.active = true AND propertyunitchange.approvedondate IS null;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, propertyUnitID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                ucl.add(generatePropertyUnitChange(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getPropertyUnitChangeList | Unable to get property unit change, ", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }

        return ucl;
    }

    public List<PropertyUnitChangeOrder> getPropertyUnitChangeListAll(int propertyUnitID) throws IntegrationException {
        List<PropertyUnitChangeOrder> ucl = new ArrayList<>();
        String query = "SELECT\n"
                + "unitchangeid, propertyunit_unitid,\n"
                + "propertyunitchange.unitnumber, propertyunitchange.otherknownaddress,\n"
                + "propertyunitchange.notes, propertyunitchange.rentalnotes,\n"
                + "removed, added, entryts, approvedondate, changedby_personid, approvedby_userid, propertyunitchange.active\n"
                + "FROM\n"
                + "propertyunitchange\n"
                + "JOIN propertyunit ON propertyunitchange.propertyunit_unitid = propertyunit.unitid\n"
                + "WHERE unitid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, propertyUnitID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                ucl.add(generatePropertyUnitChange(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getPropertyUnitChangeListAll | Unable to get property unit change, ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }

        return ucl;
    }

 // *************************************************************************
    // **               PROPERTY USE TYPE STUFF                               **
    // *************************************************************************
    
    /**
     * Builds a PropertyUseType object given a PK from the propertyusetype table
     * @param useTypeID
     * @return
     * @throws IntegrationException 
     */
    public PropertyUseType getPropertyUseType(int useTypeID) throws IntegrationException{
        String query = "SELECT propertyusetypeid, name, description, icon_iconid, zoneclass\n" +
                        "  FROM public.propertyusetype WHERE propertyusetypeid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        PropertyUseType put = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, useTypeID);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                put = generatePropertyUseType(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error searching for properties", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return put;
    }
    
    
    public void updatePropertyUseType(PropertyUseType put) throws IntegrationException{
        String query =  "UPDATE public.propertyusetype\n" +
                        "   SET name=?, description=?, icon_iconid=?, zoneclass=?\n" +
                        " WHERE propertyusetypeid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, put.getName());
            stmt.setString(2, put.getDescription());
            if(put.getIcon() != null){
                stmt.setInt(3, put.getIcon().getIconid());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            stmt.setString(4, put.getZoneClass());
            

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update property use type", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    
    public void insertPropertyUseType(PropertyUseType put) throws IntegrationException{
        String query =  "INSERT INTO public.propertyusetype(\n" +
"            propertyusetypeid, name, description, icon_iconid, zoneclass)\n" +
"    VALUES (DEFAULT, ?, ?, ?, ?);";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, put.getName());
            stmt.setString(2, put.getDescription());
            if(put.getIcon() != null){
                stmt.setInt(3, put.getIcon().getIconid());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            stmt.setString(4, put.getZoneClass());
            
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert new prop use type", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    /**
     * Returns a full table dump of PropertyUseType entries
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public List<PropertyUseType> getPropertyUseTypeList() throws IntegrationException{
        String query = "SELECT propertyusetypeid, name, description, icon_iconid, zoneclass\n" +
                        "  FROM public.propertyusetype;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        List<PropertyUseType> putList = new ArrayList<>();
        
        try {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                putList.add(generatePropertyUseType(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error searching for properties", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return putList;
        
    }
    
    
    
    // *************************************************************************
    // **                 EXTERNAL DATA                                       **
    // *************************************************************************
    
    /**
    
    /**
     * Returns a full table dump of PropertyUseType entries
     * @param propID
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public List<Integer> getPropertyExternalDataRecordIDs(int propID) throws IntegrationException{
        String query = "SELECT extdataid FROM public.propertyexternaldata WHERE property_propertyid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        List<Integer> extList = new ArrayList<>();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, propID);
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                extList.add(rs.getInt("extdataid"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error searching for properties", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return extList;
        
    }
    
    /**
     * Returns a full table dump of PropertyUseType entries
     * @param recordID
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public PropertyExtData getPropertyExternalDataRecord(int recordID) throws IntegrationException{
        String query = "SELECT extdataid, property_propertyid, ownername, ownerphone, address_street, \n" +
                        "       address_citystatezip, address_city, address_state, address_zip, \n" +
                        "       saleprice, saleyear, assessedlandvalue, assessedbuildingvalue, \n" +
                        "       assessmentyear, usecode, yearbuilt, livingarea, condition, notes, \n" +
                        "       lastupdated, taxcode, taxstatus_taxstatusid \n" +
                        "  FROM public.propertyexternaldata WHERE extdataid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        PropertyExtData pxd = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, recordID);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                pxd = generateExternalDataRecord(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error searching for properties", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return pxd;
        
    }
    
    private PropertyExtData generateExternalDataRecord(ResultSet rs) throws SQLException, IntegrationException{
        PropertyExtData bundle = new PropertyExtData();
        
        bundle.setExtdataid(rs.getInt("extdataid"));
        bundle.setProperty_propertyid(rs.getInt("property_propertyid"));
        bundle.setOwnername(rs.getString("ownername"));
        bundle.setOwnerphone(rs.getString("ownerphone"));
        bundle.setAddress_street(rs.getString("address_street"));
        
        bundle.setAddress_citystatezip(rs.getString("address_citystatezip"));
        bundle.setAddress_city(rs.getString("address_city"));
        bundle.setAddress_state(rs.getString("address_state"));
        bundle.setAddress_zip(rs.getString("address_zip"));
        
        bundle.setSaleprice(rs.getDouble("saleprice"));
        bundle.setSaleyear(rs.getInt("saleyear"));
        bundle.setAssessedlandvalue(rs.getDouble("assessedlandvalue"));
        bundle.setAssessedbuildingvalue(rs.getDouble("assessedbuildingvalue"));
        
        bundle.setAssessmentyear(rs.getInt("assessmentyear"));
        bundle.setUsecode(rs.getString("usecode"));
        bundle.setYearbuilt(rs.getInt("yearbuilt"));
        bundle.setLivingarea(rs.getInt("livingarea"));
        bundle.setCondition(rs.getString("condition"));
        
        bundle.setNotes(rs.getString("notes"));
        if(rs.getTimestamp("lastupdated") != null){
            bundle.setLastUpdatedTS(rs.getTimestamp("lastupdated").toLocalDateTime());
        }
        bundle.setTaxStatus(getTaxStatus(rs.getInt("taxstatus_taxstatusid")));
        
        return bundle;
        
    }
    
    /**
     * Retrieves a single tax status record from the db table: taxstatus
     * @param taxStatusID
     * @return
     * @throws IntegrationException 
     */
    public TaxStatus getTaxStatus(int taxStatusID) throws IntegrationException{
           String query =   "SELECT taxstatusid, year, paidstatus, tax, penalty, interest, total, \n" +
                            "       datepaid FROM public.taxstatus WHERE taxstatusid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        TaxStatus ts = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, taxStatusID);
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                ts=generateTaxStatus(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error searching for properties", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return ts;
    }
    
    /**
     * Generator method for tax status objects
     * @param rs
     * @return
     * @throws SQLException 
     */
    private TaxStatus generateTaxStatus(ResultSet rs) throws SQLException{
        TaxStatus ts = new TaxStatus();
        ts.setTaxStatusID(rs.getInt("taxstatusid"));
        ts.setYear(rs.getInt("year"));
        ts.setPaidStatus(rs.getString("paidstatus"));
        ts.setTax(rs.getDouble("tax"));
        ts.setPenalty(rs.getDouble("penalty"));
        ts.setInterest(rs.getDouble("interest"));
        ts.setTotal(rs.getDouble("total"));
        if(rs.getDate("datepaid") != null){
            ts.setDatePaid(rs.getDate("datepaid").toLocalDate().toString());
        }
        return ts;
    }
    

    /**
     * Internal generator method which extracts column values from a ResultSet
     * populated from the propertyusetype table.
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
    private PropertyUseType generatePropertyUseType(ResultSet rs) throws SQLException, IntegrationException{
        SystemIntegrator si = getSystemIntegrator();
        PropertyUseType put = new PropertyUseType();

        put.setTypeID(rs.getInt("propertyusetypeid"));
        put.setName(rs.getString("name"));
        put.setDescription(rs.getString("description"));
        put.setIcon(si.getIcon(rs.getInt("icon_iconid")));
        put.setZoneClass(rs.getString("zoneclass"));

        return put;
    }

   
   

} // close class
