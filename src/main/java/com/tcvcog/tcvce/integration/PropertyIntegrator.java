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
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PropertyStatus;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitChange;
import com.tcvcog.tcvce.entities.PropertyUnitWithLists;
import com.tcvcog.tcvce.entities.PropertyUnitWithProp;
import com.tcvcog.tcvce.entities.PropertyWithLists;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.search.QueryProperty;
import com.tcvcog.tcvce.entities.search.SearchParamsOccPeriod;
import com.tcvcog.tcvce.entities.search.SearchParamsProperty;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
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

    /**
     * Creates a new instance of PropertyIntegrator
     */
    public PropertyIntegrator() {

    }

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
            p.setLastUpdatedTS(rs.getTimestamp("latupdated").toLocalDateTime());
            
            p.setLocationDescriptor(ci.getLocationDescriptor(rs.getInt("locationdescription")));
            p.setBobSource(si.getBOBSource(rs.getInt("bobsource_sourceid")));
            p.setUnfitDateStart(rs.getTimestamp("unfitdatestart").toLocalDateTime());
            p.setUnfitDateStop(rs.getTimestamp("unfitdatestop").toLocalDateTime());
            
            p.setUnfitBy(ui.getUser(rs.getInt("unfitby_userid")));
            p.setAbandonedDateStart(rs.getTimestamp("abandoneddatestart").toLocalDateTime());
            p.setAbandonedDateStop(rs.getTimestamp("abandoneddatestop").toLocalDateTime());
            p.setAbandonedBy(ui.getUser(rs.getInt("abandonedby_userid")));
            
            p.setVacantDateStart(rs.getTimestamp("vacantdatestart").toLocalDateTime());
            p.setVacantDateStop(rs.getTimestamp("vacantdatestop").toLocalDateTime());
            p.setVacantBy(ui.getUser(rs.getInt("vacantby_userid")));
            p.setConditionIntensityClassID(rs.getInt("condition_intensityclassid"));
            
            p.setLandBankProspectIntensityClassID(rs.getInt("landbankprospect_intensityclassid"));
            p.setLandBankHeld(rs.getBoolean("landbankheld"));
            p.setActive(rs.getBoolean("active"));
            p.setNonAddressable(rs.getBoolean("nonaddressable"));
            
            p.setUseTypeID(rs.getInt("usetype_typeid"));
            p.setUseTypeString(getPropertyUseTypeString(rs.getInt("usetype_typeid")));
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error generating Property from ResultSet", ex);
        }
        return p;
    }
    
    public String getPropertyUseTypeString(int useTypeID) throws IntegrationException{
          String query = "SELECT propertyusetypeid, name, description, icon_iconid, zoneclass\n" +
                        "  FROM public.propertyusetype WHERE propertyusetypeid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        String useType = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.executeQuery();
            
            while (rs.next()) {
                useType = rs.getString("name");
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error searching for properties", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return useType;
    }

    /**
     * Utility method for property search methods whose individual SQL
     * statements implement various search features. These methods can send
     * properly configured (i.e. cursor positioned) ResultSet objects to this
     * method and get back a populated Property object
     *
     * @param rs
     * @return the fully baked Property with all fields set from DB data
     */
    private PropertyWithLists generatePropertyWithLists(ResultSet rs) throws IntegrationException {

        MunicipalityIntegrator mi = getMunicipalityIntegrator();

        PropertyWithLists p = new PropertyWithLists(generateProperty(rs));

        // finish me
        return p;
    }

    public List<Property> searchForProperties(String houseNum, String street) throws IntegrationException {

        String query = "select propertyid FROM property WHERE address ILIKE ?;";

        System.out.println("PropertyIntegrator.searchForPropertiesAddOnly - query: " + query);

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<Property> propList = new ArrayList<>();

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, "%" + houseNum + "%" + street + "%");
            System.out.println("PropertyIntegrator.searchForProperties | SQL: " + stmt.toString());
            rs = stmt.executeQuery();
            int counter = 0;
            while (rs.next() && counter <= MAX_RESULTS) {
                propList.add(getProperty(rs.getInt("propertyid")));
                counter++;
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return propList;
    }

    public List<Property> searchForProperties(String houseNum, String street, int muniID) throws IntegrationException {
        String query = "SELECT propertyid FROM property WHERE address ILIKE ? AND municipality_muniCode=?;";

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
                propList.add(getProperty(rs.getInt("propertyid")));
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

    public List<Property> searchForChangedProperties(String houseNum, String street, int muniID) throws IntegrationException {
        String query = "SELECT DISTINCT\n"
                + "	   propertyid, unit_unitid, municipality_municode, parid, lotandblock, address,\n"
                + "        propertyusetype, usegroup, constructiontype, countycode, apartmentno,\n"
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

    public List<Property> searchForChangedPropertiesAll(String houseNum, String street, int muniID) throws IntegrationException {
        String query = "SELECT DISTINCT\n"
                + "	   propertyid FROM \n"
                + "	property,\n"
                + "	propertyunitchange\n"
                + "	\n"
                + "WHERE \n"
                + "	address ILIKE ? AND municipality_muniCode=?  AND propertyid = property_propertyid;";

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
                propList.add(getProperty(rs.getInt("propertyid")));
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

    public int insertProperty(Property newProp) throws IntegrationException {
        String query = "INSERT INTO public.property(\n" +
                        "            propertyid, municipality_municode, parid, lotandblock, address, \n" +
                        "            usegroup, constructiontype, countycode, notes, addr_city, \n" +
                        "            addr_state, addr_zip, ownercode, propclass, lastupdated, lastupdatedby, \n" +
                        "            locationdescription, bobsource_sourceid, unfitdatestart, unfitdatestop, \n" +
                        "            unfitby_userid, abandoneddatestart, abandoneddatestop, abandonedby_userid, \n" +
                        "            vacantdatestart, vacantdatestop, vacantby_userid, condition_intensityclassid, \n" +
                        "            landbankprospect_intensityclassid, landbankheld, active, nonaddressable, \n" +
                        "            usetype_typeid)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, now(), ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?);"; // im so sorry 

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        int lastID;

        try {
            stmt = con.prepareStatement(query);

            stmt.setInt(1, newProp.getMuniCode());
            stmt.setString(2, newProp.getParID());
            stmt.setString(3, newProp.getLotAndBlock());
            stmt.setString(4, newProp.getAddress());
            
            stmt.setString(5, newProp.getUseGroup());
            stmt.setString(6, newProp.getConstructionType());
            stmt.setString(7, newProp.getCountyCode());
            stmt.setString(8, newProp.getNotes());
            stmt.setString(9, newProp.getAddress_city());
            
            stmt.setString(10, newProp.getAddress_state());
            stmt.setString(11, newProp.getAddress_zip());
            stmt.setString(12, newProp.getOwnerCode());
            stmt.setString(13, newProp.getPropclass());
            stmt.setTimestamp(14, java.sql.Timestamp.valueOf(newProp.getLastUpdatedTS()));
            stmt.setInt(15, newProp.getLastUpdatedBy().getUserID());
            
            stmt.setInt(16, newProp.getLocationDescriptor().getLocationID());
            stmt.setInt(17, newProp.getBobSource().getSourceid());
            stmt.setTimestamp(18, java.sql.Timestamp.valueOf(newProp.getUnfitDateStart()));
            stmt.setTimestamp(19, java.sql.Timestamp.valueOf(newProp.getUnfitDateStop()));
            
            stmt.setInt(20, newProp.getUnfitBy().getUserID());
            stmt.setTimestamp(21, java.sql.Timestamp.valueOf(newProp.getAbandonedDateStart()));
            stmt.setTimestamp(22, java.sql.Timestamp.valueOf(newProp.getAbandonedDateStop()));
            stmt.setInt(23, newProp.getAbandonedBy().getUserID());
            
            stmt.setTimestamp(24, java.sql.Timestamp.valueOf(newProp.getVacantDateStart()));
            stmt.setTimestamp(25, java.sql.Timestamp.valueOf(newProp.getVacantDateStop()));
            stmt.setInt(26, newProp.getVacantBy().getUserID());
            stmt.setInt(27, newProp.getConditionIntensityClassID());
            
            stmt.setInt(28, newProp.getLandBankProspectIntensityClassID());
            stmt.setBoolean(29, newProp.isLandBankHeld());
            stmt.setBoolean(30, newProp.isActive());
            stmt.setBoolean(31, newProp.isNonAddressable());
            
            stmt.setInt(32, newProp.getUseTypeID());

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

    public List<Property> getProperties(Person p) throws IntegrationException {
        String query = "SELECT property_propertyid FROM propertyperson WHERE person_personid = ?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<Property> pList = new ArrayList<>();

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, p.getPersonID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                pList.add(getProperty(rs.getInt("property_propertyid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getProperty | Unable to retrieve property by ID number", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return pList;

    }

    public String updateProperty(Property prop) throws IntegrationException {
        String query =  "UPDATE public.property\n" +
                        "   SET municipality_municode=?, parid=?, lotandblock=?, \n" +
                        "       address=?, usegroup=?, constructiontype=?, countycode=?, apartmentno=?, \n" +
                        "       notes=?, addr_city=?, addr_state=?, addr_zip=?, ownercode=?, \n" +
                        "       propclass=?, lastupdated=now(), lastupdatedby=?, locationdescription=?, \n" +
                        "       bobsource_sourceid=?, unfitdatestart=?, unfitdatestop=?, unfitby_userid=?, \n" +
                        "       abandoneddatestart=?, abandoneddatestop=?, abandonedby_userid=?, \n" +
                        "       vacantdatestart=?, vacantdatestop=?, vacantby_userid=?, condition_intensityclassid=?, \n" +
                        "       landbankprospect_intensityclassid=?, landbankheld=?, active=?, \n" +
                        "       nonaddressable=?, usetype_typeid=?\n" +
                        " WHERE propertyid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

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
            stmt.setTimestamp(14, java.sql.Timestamp.valueOf(prop.getLastUpdatedTS()));
            stmt.setInt(15, prop.getLastUpdatedBy().getUserID());
            
            stmt.setInt(16, prop.getLocationDescriptor().getLocationID());
            stmt.setInt(17, prop.getBobSource().getSourceid());
            stmt.setTimestamp(18, java.sql.Timestamp.valueOf(prop.getUnfitDateStart()));
            stmt.setTimestamp(19, java.sql.Timestamp.valueOf(prop.getUnfitDateStop()));
            
            stmt.setInt(20, prop.getUnfitBy().getUserID());
            stmt.setTimestamp(21, java.sql.Timestamp.valueOf(prop.getAbandonedDateStart()));
            stmt.setTimestamp(22, java.sql.Timestamp.valueOf(prop.getAbandonedDateStop()));
            stmt.setInt(23, prop.getAbandonedBy().getUserID());
            
            stmt.setTimestamp(24, java.sql.Timestamp.valueOf(prop.getVacantDateStart()));
            stmt.setTimestamp(25, java.sql.Timestamp.valueOf(prop.getVacantDateStop()));
            stmt.setInt(26, prop.getVacantBy().getUserID());
            stmt.setInt(27, prop.getConditionIntensityClassID());
            
            stmt.setInt(28, prop.getLandBankProspectIntensityClassID());
            stmt.setBoolean(29, prop.isLandBankHeld());
            stmt.setBoolean(30, prop.isActive());
            stmt.setBoolean(31, prop.isNonAddressable());
            
            stmt.setInt(32, prop.getUseTypeID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build propertyUseTypesMap", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             

        } // close finally

        return "propertyProfile";

    }
    
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

    public QueryProperty queryProperties(QueryProperty query) throws IntegrationException {
        List<SearchParamsProperty> pList = query.getParmsList();

        for (SearchParamsProperty sp : pList) {
            query.addToResults(searchForProperties(sp));
        }
        query.setExecutionTimestamp(LocalDateTime.now());
        query.setExecutedByIntegrator(true);
        return query;

    }

    public List<Property> searchForProperties(SearchParamsProperty params) throws IntegrationException {

        List<Property> propList = new ArrayList<>();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT DISTINCT propertyid\n");
        sb.append("	FROM property LEFT OUTER JOIN propertyexternaldata ON (property.propertyid = propertyexternaldata.property_propertyid)\n");
        sb.append("	LEFT OUTER JOIN propertyusetype ON (property.usetype_typeid = propertyusetype.propertyusetypeid)\n");
        sb.append("	LEFT OUTER JOIN propertystatus ON (property.status_statusid = propertystatus.statusid)\n");
        sb.append("	WHERE propertyid IS NOT NULL ");

        if (!params.isFilterByObjectID()) {
            if (params.isFilterByMuni()) {
                sb.append("municipality_municode = ? "); // param 1
            }

            if (params.isFilterByStartEndDate()) {
                sb.append(getDBDateField(params));
                sb.append(" ");
                sb.append("BETWEEN ? AND ? "); // parm 2 and 3 without ID
            }

            if (params.isFilterByNullDateField()) {
                sb.append("AND ");
                sb.append(getDBDateField(params));
                sb.append("IS NULL ");
            }

            if (params.isFilterByUserField()) {
                sb.append("AND ");
                sb.append(getDBUserField(params));
                sb.append("=? ");
            }

            if (params.isFilterByZip()) {
                sb.append("AND addr_zip=? ");
            }

            if (params.isFilterByLotAndBlock()) {
                sb.append("AND lotandblock=? ");
            }

            if (params.isFilterByBOBSource()) {
                sb.append("AND bobsource_sourceid=? ");
            }

            if (params.isFilterByParcelID()) {
                sb.append("AND parid=? ");
            }

            if (params.isFilterByAddressPart()) {
                sb.append("AND address ILIKE ? ");
            }

            if (params.isFilterByCondition()) {
                sb.append("AND condition_intensityclassid=?");
            }

            if (params.isFilterByLandBankPropspect()) {
                sb.append("AND landbankprospect_intensityclassid=?");
            }

            if (params.isFilterByLandBankHeld()) {
                sb.append("AND landbankheld= ");
                if (params.isActive()) {
                    sb.append("TRUE ");
                } else {
                    sb.append("FALSE ");
                }
            }

            if (params.isFilterByNonAddressable()) {
                sb.append("AND landbankheld= ");
                if (params.isActive()) {
                    sb.append("TRUE ");
                } else {
                    sb.append("FALSE ");
                }
            }

            if (params.isFilterByUseType()) {
                sb.append("AND property.usetype_typeid=?");
            }

            if (params.isFilterByZoneClass()) {
                sb.append("AND propertyusetype.zoneclass=?");
            }

            if (params.isFilterByAssessedValue()) {
                sb.append("AND propertyexternaldata.assessedlandvalue+propertyexternaldata.assessedlandvalue>? ");
                sb.append("AND propertyexternaldata.assessedlandvalue+propertyexternaldata.assessedlandvalue<? ");
            }

            if (params.isFilterByYearBuilt()) {
                sb.append("AND propertyexternaldata.yearbuilt>? ");
                sb.append("AND propertyexternaldata.yearbuilt<? ");
            }

            if (params.isActive_filterBy()) {
                sb.append("AND ");
                if (params.isActive()) {
                    sb.append("active=TRUE ");
                } else {
                    sb.append("active=FALSE ");
                }
            }

        } else {  //object ID filter
            sb.append("AND ");
            sb.append("propertyid=? "); // will be param 1 with ID search
        }

        int paramCounter = 0;

        try {
            stmt = con.prepareStatement(sb.toString());

            if (!params.isFilterByObjectID()) {
                if (params.isFilterByMuni()) {
                     stmt.setInt(++paramCounter, params.getMuni().getMuniCode());
                }
                if (params.isFilterByStartEndDate()) {
                    stmt.setTimestamp(++paramCounter, java.sql.Timestamp.valueOf(params.getStartDate()));
                    stmt.setTimestamp(++paramCounter, java.sql.Timestamp.valueOf(params.getEndDate()));
                }
                if (params.isFilterByUserField()) {
                   stmt.setInt(++paramCounter, params.getUserFieldUser().getUserID());
                }
                if (params.isFilterByZip()) {
                   stmt.setString(++paramCounter, params.getZipCode());
                }
                if (params.isFilterByLotAndBlock()) {
                   stmt.setString(++paramCounter, params.getLogAndBlock());
                }
                if (params.isFilterByBOBSource()) {
                    stmt.setInt(++paramCounter, params.getBobSourceID());
                }
                if (params.isFilterByParcelID()) {
                    stmt.setString(++paramCounter, params.getParcelID());
                }
                if (params.isFilterByAddressPart()) {
                    stmt.setString(++paramCounter, params.getAddressPart());
                }
                if (params.isFilterByCondition()) {
                    stmt.setInt(++paramCounter, params.getConditionIntensityClassID());
                }
                if (params.isFilterByLandBankPropspect()) {
                    stmt.setInt(++paramCounter, params.getLandBankPropsectIntensityClassID());
                }
                if (params.isFilterByUseType()) {
                    stmt.setInt(++paramCounter, params.getUseTypeID());
                }
                if (params.isFilterByZoneClass()) {
                    stmt.setString(++paramCounter, params.getZoneClass());
                }
                if (params.isFilterByAssessedValue()) {
                    stmt.setInt(++paramCounter, params.getAssessedValueMin());
                    stmt.setInt(++paramCounter, params.getAssessedValueMax());
                }
                if (params.isFilterByYearBuilt()) {
                    stmt.setInt(++paramCounter, params.getYearBuiltMin());
                    stmt.setInt(++paramCounter, params.getYearBuiltMax());
                }
            } else {
                stmt.setInt(++paramCounter, params.getObjectID());
            }
            rs = stmt.executeQuery();
            int counter = 0;
            int maxResults;
            if (params.isLimitResultCountTo100()) {
                maxResults = 100;
            } else {
                maxResults = Integer.MAX_VALUE;
            }
            while (rs.next() && counter < maxResults) {
                propList.add(getProperty(rs.getInt("propertyid")));
                counter++;
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot search for code enf cases, sorry!", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return propList;
    }

    private String getDBDateField(SearchParamsProperty params) {
        switch (params.getDateField()) {
            case ABANDONED_START:
                return "property.abandoneddatestart";
            case ABANDONED_STOP:
                return "property.abandoneddatestop";
            case EXTERNAL_DATA_LASTUPDATED:
                return "propertyexternaldata.lastupdated";
            case LAST_UPDATED:
                return "property.lastupdated";
            case UNFIT_START:
                return "property.unfitdatestart";
            case UNFIT_STOP:
                return "property.unfitdatestop";
            case VACANT_START:
                return "property.vacantdatestart";
            case VACANT_STOP:
                return "property.vacantdatestart";
            default:
                return "property.lastupdated";
        }
    }

    private String getDBUserField(SearchParamsProperty params) {
        switch (params.getUserField()) {
            case ABANDONED_BY:
                return "property.abandonedby_userid";
            case UNFIT_BY:
                return "unfitby_userid";
            case VACANT_BY:
                return "vacantby_userid";
            case PROPERTY_UPDATEDBY:
                return "property.lastupdatedby";
            default:
                return "property.lastupdatedby";
        }
    }

    public Property getProperty(int propertyID) throws IntegrationException {
        Property p = new Property();
        String query = "SELECT propertyid, municipality_municode, parid, lotandblock, address, \n" +
                        "       usegroup, constructiontype, countycode, apartmentno, notes, addr_city, \n" +
                        "       addr_state, addr_zip, ownercode, propclass, lastupdated, lastupdatedby, \n" +
                        "       locationdescription, bobsource_sourceid, unfitdatestart, unfitdatestop, \n" +
                        "       unfitby_userid, abandoneddatestart, abandoneddatestop, abandonedby_userid, \n" +
                        "       vacantdatestart, vacantdatestop, vacantby_userid, condition_intensityclassid, \n" +
                        "       landbankprospect_intensityclassid, landbankheld, active, nonaddressable, \n" +
                        "       status_statusid, usetype_typeid\n" +
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

        return p;

    } // close getProperty()

    public PropertyWithLists getPropertyWithLists(int propertyID) throws IntegrationException, CaseLifecyleException {
        
            CaseIntegrator ci = getCaseIntegrator();
            PersonIntegrator pi = getPersonIntegrator();
            PropertyWithLists p = new PropertyWithLists(getProperty(propertyID));
   
            p.setCeCaseList(ci.getCECasesByProp(p));
            p.setUnitList(getPropertyUnitList(p));
            p.setPersonList(pi.getPersonList(p));
        return p;
    }

    public List<Property> getPropertyHistoryList(User u) throws IntegrationException {
        List<Property> propList = new ArrayList<>();
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String s = "SELECT property_propertyid, entrytimestamp FROM loginobjecthistory "
                    + "WHERE login_userid = ? "
                    + "AND property_propertyid IS NOT NULL "
                    + "ORDER BY entrytimestamp DESC;";
            stmt = con.prepareStatement(s);
            stmt.setInt(1, u.getUserID());

            rs = stmt.executeQuery();
            int MAX_RES = 20;  //behold a MAGICAL number
            int iter = 0;

            // are we too cool for for-loops?
            while (rs.next() && iter < MAX_RES) {
                Property p = getProperty(rs.getInt("property_propertyid"));
                propList.add(p);
                iter++;
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate property history list", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return propList;
    }
    
     public PropertyUnit getPropertyUnit(int propId) throws IntegrationException {
        PropertyUnit pu = new PropertyUnit();
        String query =  "SELECT unitid, unitnumber, property_propertyid, otherknownaddress, notes, \n" +
                        "       rentalintentdatestart, rentalintentdatestop, rentalintentlastupdatedby_userid, \n" +
                        "       rentalnotes, active, condition_intensityclassid, lastupdatedts\n" +
                        "  FROM public.propertyunit;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, propId);
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
        
        pu.setRentalIntentDateStart(rs.getTimestamp("rentalintentdatestart").toLocalDateTime());
        pu.setRentalIntentDateStop(rs.getTimestamp("rentalintentdatestop").toLocalDateTime());
        pu.setRentalIntentLastUpdatedBy(ui.getUser(rs.getInt("rentalintentlastupdatedby_userid")));
        
        pu.setRentalNotes(rs.getString("rentalnotes"));
        pu.setActive(rs.getBoolean("active"));
        pu.setConditionIntensityClassID(rs.getInt("condition_intensityclassid"));
        pu.setLastUpdatedTS(rs.getTimestamp("lastupdatedts").toLocalDateTime());
        
        return pu;
    }
    
    public void insertPropertyUnit(PropertyUnit pu) throws IntegrationException {
        String query =  "INSERT INTO public.propertyunit(\n" +
                        "            unitid, unitnumber, property_propertyid, otherknownaddress, notes, \n" +
                        "            rentalintentdatestart, rentalintentdatestop, rentalintentlastupdatedby_userid, \n" +
                        "            rentalnotes, active, condition_intensityclassid, lastupdatedts)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?, ?, ?, now());";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, pu.getUnitNumber());
            stmt.setInt(2, pu.getPropertyID());
            stmt.setString(3, pu.getOtherKnownAddress());
            stmt.setString(4, pu.getNotes());
            
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(pu.getRentalIntentDateStart()));
            stmt.setTimestamp(6, java.sql.Timestamp.valueOf(pu.getRentalIntentDateStop()));
            stmt.setInt(7, pu.getRentalIntentLastUpdatedBy().getUserID());
            
            stmt.setString(8, pu.getRentalNotes());
            stmt.setBoolean(9, pu.isActive());
            stmt.setInt(10, pu.getConditionIntensityClassID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getProperty | Unable to retrieve property by ID number", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
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
            
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(pu.getRentalIntentDateStart()));
            stmt.setTimestamp(6, java.sql.Timestamp.valueOf(pu.getRentalIntentDateStop()));
            stmt.setInt(7, pu.getRentalIntentLastUpdatedBy().getUserID());
            
            stmt.setString(8, pu.getRentalNotes());
            stmt.setBoolean(9, pu.isActive());
            stmt.setInt(10, pu.getConditionIntensityClassID());

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
     */
    public PropertyUnitWithLists getPropertyUnitWithLists(PropertyUnit pu) throws IntegrationException{
        return getPropertyUnitWithLists(pu.getUnitID());
    }
    
    public PropertyUnitWithLists getPropertyUnitWithLists(int unitID) throws IntegrationException{
        OccupancyIntegrator oi = getOccupancyIntegrator();

        PropertyUnitWithLists puwl = new PropertyUnitWithLists(getPropertyUnit(unitID));
        puwl.setPeriodList(oi.getOccPeriodList(unitID));
        return puwl;
    }
    
    public PropertyUnitWithProp getPropertyUnitWithProp(int unitID, int propertyID) throws IntegrationException{
        PropertyIntegrator pi = getPropertyIntegrator();
        
        PropertyUnitWithProp puwp = new PropertyUnitWithProp(getPropertyUnit(unitID));
        puwp.setProperty(pi.getProperty(propertyID));
        
        return puwp;
    }
   
    
    /**
     * TODO: Refactor and fill in NADGIT
     * @param uc
     * @throws IntegrationException 
     */
    public void implementPropertyUnitChangeOrder(PropertyUnitChange uc) throws IntegrationException {
        String query = "UPDATE public.propertyunit\n"
                + "SET unitnumber=?, otherknownaddress=?, notes=?, rental=?, inactive=?\n"
                + "WHERE unitid = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        PropertyIntegrator pi = getPropertyIntegrator();

        PropertyUnit skeleton = pi.getPropertyUnit(uc.getUnitID());

        if (uc.getUnitNumber() != null) {
            skeleton.setUnitNumber(uc.getUnitNumber());
        }

        if (uc.getOtherKnownAddress() != null) {
            skeleton.setOtherKnownAddress("Updated");
        } else {
            skeleton.setOtherKnownAddress("Updated");
        }

        if (uc.getNotes() != null) {
            skeleton.setNotes(uc.getNotes());
        }
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, skeleton.getUnitNumber());
            stmt.setString(2, skeleton.getOtherKnownAddress());
            stmt.setString(3, skeleton.getNotes());

            stmt.setBoolean(5, uc.isRemoved());
            stmt.setInt(6, skeleton.getUnitID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update PropertyUnit", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            
        } // close finally

    }
    
    public void insertPropertyUnitChange(PropertyUnitChange uc) throws IntegrationException {
        String query = "INSERT INTO public.propertyunitchange(\n"
                + "            unitchangeid, unitnumber, unit_unitid, otherknownaddress, notes, \n"
                + "            rental, removed, added, changedon, approvedby, changedby, property_propertyid)\n"
                + "    VALUES (DEFAULT, ?, ?, ?, ?, \n"
                + "            ?, ?, ?, ?, ?, ?, ?);";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, uc.getUnitNumber());
            stmt.setInt(2, uc.getUnitID());
            stmt.setString(3, uc.getOtherKnownAddress());
            stmt.setString(4, uc.getNotes());
            stmt.setBoolean(5, uc.isRental());
            stmt.setBoolean(6, uc.isRemoved());
            stmt.setBoolean(7, uc.isAdded());
            stmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(9, uc.getApprovedBy().getUserID());
            stmt.setString(10, uc.getChangedBy());
            stmt.setInt(11, uc.getPropertyID());

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

    public PropertyUnitChange getPropertyUnitChange(int unitChangeId) throws IntegrationException {
        PropertyUnitChange uc = new PropertyUnitChange();
        String query = "SELECT unitchangeid, unitid, property_propertyid, property.address,\n"
                + "propertyunitchange.unitnumber, propertyunitchange.otherknownaddress,\n"
                + "propertyunitchange.notes, propertyunitchange.rental,\n"
                + "removed, added, changedon, approvedon, approvedby, inactive\n"
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

    public PropertyUnitChange generatePropertyUnitChange(ResultSet rs) throws SQLException, IntegrationException {
        PersonIntegrator persInt = getPersonIntegrator();
        UserIntegrator ui = getUserIntegrator();
        PropertyUnitChange uc = new PropertyUnitChange();
        uc.setUnitChangeID(rs.getInt("unitchangeid"));
        uc.setUnitID(rs.getInt("unit_unitid"));
        uc.setUnitNumber(rs.getString("unitnumber"));
        uc.setNotes(rs.getString("notes"));
        uc.setOtherKnownAddress(rs.getString("otherknownaddress"));
        uc.setRental(rs.getBoolean("rental"));
        uc.setAdded(rs.getBoolean("added"));
        uc.setRemoved(rs.getBoolean("removed"));
        uc.setChangedOn(rs.getTimestamp("changedon"));
        uc.setApprovedOn(rs.getTimestamp("approvedon"));
        uc.setApprovedBy(ui.getUser(rs.getInt("approvedby")));
        uc.setChangedBy(rs.getString("changedby"));
        uc.setPropertyID(rs.getInt("property_propertyid"));
        uc.setActive(rs.getBoolean("active"));
        return uc;
    }

    /**
     * TODO occbeta
     *
     * @param changeToUpdate
     * @throws IntegrationException
     */
    public void updatePropertyUnitChange(PropertyUnitChange changeToUpdate) throws IntegrationException {
        String query = "UPDATE public.propertyunitchange\n"
                + "SET unitnumber=?, unit_unitid=?, otherknownaddress=?, notes=?, rental=?,\n"
                + "removed=?, added=?, changedon=?, approvedon=?, approvedby=?, changedby=?, inactive=?\n"
                + "WHERE unitchangeid = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);

            stmt.setString(1, changeToUpdate.getUnitNumber());
            stmt.setInt(2, changeToUpdate.getUnitID());
            stmt.setString(3, changeToUpdate.getOtherKnownAddress());
            stmt.setString(4, changeToUpdate.getNotes());
            stmt.setBoolean(5, changeToUpdate.isRental());
            stmt.setBoolean(6, changeToUpdate.isRemoved());
            stmt.setBoolean(7, changeToUpdate.isAdded());
            stmt.setTimestamp(8, changeToUpdate.getChangedOn());
            stmt.setTimestamp(9, changeToUpdate.getApprovedOn());
            stmt.setInt(10, changeToUpdate.getApprovedBy().getUserID());
            stmt.setString(11, changeToUpdate.getChangedBy());
            stmt.setBoolean(12, changeToUpdate.isActive());
            stmt.setInt(13, changeToUpdate.getUnitChangeID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update PropertyUnitChange", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            
        } // close finally

    }

    public List<PropertyUnitChange> getPropertyUnitChangeList(Property property) throws IntegrationException {
        List<PropertyUnitChange> ucl = new ArrayList<>();
        String query = "SELECT \n"
                + "	unitchangeid, unit_unitid, propertyunitchange.property_propertyid, property.address,\n"
                + "	propertyunitchange.unitnumber, propertyunitchange.otherknownaddress,\n"
                + "	propertyunitchange.notes, propertyunitchange.rental,\n"
                + "	removed, added, changedon, approvedon, changedby, approvedby, inactive\n"
                + "FROM \n"
                + "	propertyunitchange \n"
                + "JOIN property ON propertyunitchange.property_propertyid = property.propertyid\n"
                + "WHERE propertyunitchange.property_propertyid=? AND propertyunitchange.inactive IS null AND propertyunitchange.approvedon IS null;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, property.getPropertyID());
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

    public List<PropertyUnitChange> getPropertyUnitChangeListAll(Property property) throws IntegrationException {
        List<PropertyUnitChange> ucl = new ArrayList<>();
        String query = "SELECT \n"
                + "	unitchangeid, unit_unitid, propertyunitchange.property_propertyid, property.address,\n"
                + "	propertyunitchange.unitnumber, propertyunitchange.otherknownaddress,\n"
                + "	propertyunitchange.notes, propertyunitchange.rental,\n"
                + "	removed, added, changedon, approvedon, changedby, approvedby, inactive\n"
                + "FROM \n"
                + "	propertyunitchange \n"
                + "JOIN property ON propertyunitchange.property_propertyid = property.propertyid\n"
                + "WHERE propertyunitchange.property_propertyid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, property.getPropertyID());
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



} // close class
