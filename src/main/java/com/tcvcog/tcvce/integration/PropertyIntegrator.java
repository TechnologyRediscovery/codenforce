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
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitChange;
import com.tcvcog.tcvce.entities.PropertyUnitDataHeavy;
import com.tcvcog.tcvce.entities.PropertyUnitWithProp;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.QueryProperty;
import com.tcvcog.tcvce.entities.search.SearchParamsProperty;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.Credential;
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
import java.util.Iterator;
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
            
            p.setLocationDescriptor(ci.getLocationDescriptor(rs.getInt("locationdescription")));
            
            p.setBobSource(si.getBOBSource(rs.getInt("bobsource_sourceid")));
            
            if(rs.getTimestamp("unfitdatestart") != null){
                p.setUnfitDateStart(rs.getTimestamp("unfitdatestart").toLocalDateTime());
            }
            if(rs.getTimestamp("unfitdatestop") != null){
                p.setUnfitDateStop(rs.getTimestamp("unfitdatestop").toLocalDateTime());
            }
            
            p.setUnfitBy(ui.getUser(rs.getInt("unfitby_userid")));
                
            if(rs.getTimestamp("abandoneddatestart") != null){
                p.setAbandonedDateStart(rs.getTimestamp("abandoneddatestart").toLocalDateTime());
            }
            if(rs.getTimestamp("abandoneddatestop") != null){
                p.setAbandonedDateStop(rs.getTimestamp("abandoneddatestop").toLocalDateTime());
            }
            p.setAbandonedBy(ui.getUser(rs.getInt("abandonedby_userid")));
            
            if(rs.getTimestamp("vacantdatestart") != null){
                p.setVacantDateStart(rs.getTimestamp("vacantdatestart").toLocalDateTime());
            }
            if(rs.getTimestamp("vacantdatestop") != null){
                p.setVacantDateStop(rs.getTimestamp("vacantdatestop").toLocalDateTime());
            }
            p.setVacantBy(ui.getUser(rs.getInt("vacantby_userid")));
            p.setConditionIntensityClassID(rs.getInt("condition_intensityclassid"));
            
            p.setLandBankProspectIntensityClassID(rs.getInt("landbankprospect_intensityclassid"));
            p.setLandBankHeld(rs.getBoolean("landbankheld"));
            p.setActive(rs.getBoolean("active"));
            p.setNonAddressable(rs.getBoolean("nonaddressable"));
            
            p.setUseTypeID(rs.getInt("usetype_typeid"));
            p.setUseTypeString(getPropertyUseTypeString(rs.getInt("usetype_typeid")));
            
            p.setUnitList(getPropertyUnitList(p));
            
            if(p.getUnitList() != null && p.getUnitList().isEmpty()){
                System.out.println("PropertyIntegrator.generateProperty | inserting new unit");
                PropertyUnit pu = pc.getNewPropertyUnit();
                pu.setPropertyID(p.getPropertyID());
                insertPropertyUnit(pu);
            }
            
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
            stmt.setInt(1, useTypeID);
            rs = stmt.executeQuery();
            
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
    private PropertyDataHeavy generatePropertyWithLists(ResultSet rs) throws IntegrationException {

        MunicipalityIntegrator mi = getMunicipalityIntegrator();

        PropertyDataHeavy p = new PropertyDataHeavy(generateProperty(rs));

        // finish me
        return p;
    }

    /**
     * First gen simple search method for properties
     * 
     * @deprecated 
     * @param houseNum
     * @param street
     * @return
     * @throws IntegrationException 
     */
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
            System.out.println("PropertyIntegrator.searchForProperties - with muni | sql: ");
            stmt = con.prepareStatement(query);
            stmt.setString(1, "%" + houseNum + "%" + street + "%");
            stmt.setInt(2, muniID);
            rs = stmt.executeQuery();
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

    public int insertProperty(Property prop) throws IntegrationException {
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
             if(prop.getLastUpdatedTS() != null){
                stmt.setTimestamp(14, java.sql.Timestamp.valueOf(prop.getLastUpdatedTS()));
            }
            stmt.setInt(15, prop.getLastUpdatedBy().getUserID());
            
            stmt.setInt(16, prop.getLocationDescriptor().getLocationID());
            stmt.setInt(17, prop.getBobSource().getSourceid());
            if(prop.getUnfitDateStart() != null){
                stmt.setTimestamp(18, java.sql.Timestamp.valueOf(prop.getUnfitDateStart()));
            }
            if(prop.getUnfitDateStop() != null){
                stmt.setTimestamp(19, java.sql.Timestamp.valueOf(prop.getUnfitDateStop()));
            }
            
            stmt.setInt(20, prop.getUnfitBy().getUserID());
            if(prop.getAbandonedDateStart()!= null){
                stmt.setTimestamp(21, java.sql.Timestamp.valueOf(prop.getAbandonedDateStart()));
            }
            if(prop.getAbandonedDateStop()!= null){
                stmt.setTimestamp(22, java.sql.Timestamp.valueOf(prop.getAbandonedDateStop()));
            }
            stmt.setInt(23, prop.getAbandonedBy().getUserID());
            
            if(prop.getVacantDateStart()!= null){
                stmt.setTimestamp(24, java.sql.Timestamp.valueOf(prop.getVacantDateStart()));
            }
            if(prop.getVacantDateStop()!= null){
                stmt.setTimestamp(25, java.sql.Timestamp.valueOf(prop.getVacantDateStop()));
            }
            stmt.setInt(26, prop.getVacantBy().getUserID());
            stmt.setInt(27, prop.getConditionIntensityClassID());
            
            stmt.setInt(28, prop.getLandBankProspectIntensityClassID());
            stmt.setBoolean(29, prop.isLandBankHeld());
            stmt.setBoolean(30, prop.isActive());
            stmt.setBoolean(31, prop.isNonAddressable());
            
            stmt.setInt(32, prop.getUseTypeID());

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
                        "   SET propertyid=?, municipality_municode=?, parid=?, lotandblock=?, \n" +
                        "       address=?, usegroup=?, constructiontype=?, countycode=?, notes=?, \n" +
                        "       addr_city=?, addr_state=?, addr_zip=?, ownercode=?, propclass=?, \n" +
                        "       lastupdated=?, lastupdatedby=?, locationdescription=?, bobsource_sourceid=?, \n" +
                        "       unfitdatestart=?, unfitdatestop=?, unfitby_userid=?, abandoneddatestart=?, \n" +
                        "       abandoneddatestop=?, abandonedby_userid=?, vacantdatestart=?, \n" +
                        "       vacantdatestop=?, vacantby_userid=?, condition_intensityclassid=?, \n" +
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
            if(prop.getLastUpdatedTS() != null){
                stmt.setTimestamp(14, java.sql.Timestamp.valueOf(prop.getLastUpdatedTS()));
            }
            stmt.setInt(15, prop.getLastUpdatedBy().getUserID());
            
            stmt.setInt(16, prop.getLocationDescriptor().getLocationID());
            stmt.setInt(17, prop.getBobSource().getSourceid());
            if(prop.getUnfitDateStart() != null){
                stmt.setTimestamp(18, java.sql.Timestamp.valueOf(prop.getUnfitDateStart()));
            }
            if(prop.getUnfitDateStop() != null){
                stmt.setTimestamp(19, java.sql.Timestamp.valueOf(prop.getUnfitDateStop()));
            }
            
            stmt.setInt(20, prop.getUnfitBy().getUserID());
            if(prop.getAbandonedDateStart()!= null){
                stmt.setTimestamp(21, java.sql.Timestamp.valueOf(prop.getAbandonedDateStart()));
            }
            if(prop.getAbandonedDateStop()!= null){
                stmt.setTimestamp(22, java.sql.Timestamp.valueOf(prop.getAbandonedDateStop()));
            }
            stmt.setInt(23, prop.getAbandonedBy().getUserID());
            
            if(prop.getVacantDateStart()!= null){
                stmt.setTimestamp(24, java.sql.Timestamp.valueOf(prop.getVacantDateStart()));
            }
            if(prop.getVacantDateStop()!= null){
                stmt.setTimestamp(25, java.sql.Timestamp.valueOf(prop.getVacantDateStop()));
            }
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
            throw new IntegrationException("Unable to update property", ex);
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

    public QueryProperty runQueryProperties(QueryProperty query) throws IntegrationException {
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
        //sb.append("	LEFT OUTER JOIN propertystatus ON (property.status_statusid = propertystatus.statusid)\n");
        sb.append("	WHERE propertyid IS NOT NULL ");

        System.out.println("PropertyIntegrator.searchForPropWParams");
        System.out.println(params.getParams());
        if (!params.isObjectID_filterBy()) {
            if (params.isFilterByMuni()) {
                sb.append("AND municipality_municode = ? "); // param 1
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

            if (params.isFilterByStartEndDate()) {
                sb.append(" AND ");
                sb.append(getDBDateField(params));
                sb.append(" BETWEEN ? AND ? "); // parm 2 and 3 without ID
            }

        } else {  //object ID filter
            sb.append("AND ");
            sb.append("propertyid=? "); // will be param 1 with ID search
        }
        sb.append(";");
        
        System.out.println(sb.toString());
        
        int paramCounter = 0;

        try {
            stmt = con.prepareStatement(sb.toString());

            if (!params.isObjectID_filterBy()) {
                if (params.isFilterByMuni()) {
                     stmt.setInt(++paramCounter, params.getMuni().getMuniCode());
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
                if (params.isFilterByStartEndDate()) {
                    stmt.setTimestamp(++paramCounter, params.getStartDateSQLDate());
                    stmt.setTimestamp(++paramCounter, params.getEndDateSQLDate());
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
            System.out.println(String.format("number of returned props = %d", counter));
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot search for code enf cases, sorry!", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        System.out.println("about to return proplist");
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
        PropertyCoordinator pc = getPropertyCoordinator();
        String query = "SELECT propertyid, municipality_municode, parid, lotandblock, address, \n" +
                        "       usegroup, constructiontype, countycode, notes, addr_city, addr_state, \n" +
                        "       addr_zip, ownercode, propclass, lastupdated, lastupdatedby, locationdescription, \n" +
                        "       bobsource_sourceid, unfitdatestart, unfitdatestop, unfitby_userid, \n" +
                        "       abandoneddatestart, abandoneddatestop, abandonedby_userid, vacantdatestart, \n" +
                        "       vacantdatestop, vacantby_userid, condition_intensityclassid, \n" +
                        "       landbankprospect_intensityclassid, landbankheld, active, nonaddressable, \n" +
                        "       usetype_typeid\n" +
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
     * 
     * @param propertyID
     * @param u
     * @return
     * @throws IntegrationException
     * @throws CaseLifecycleException
     * @throws EventException
     * @throws AuthorizationException 
     */
    public PropertyDataHeavy getPropertyDataHeavy(int propertyID) throws IntegrationException, CaseLifecycleException, EventException, AuthorizationException {
            PropertyCoordinator pc = getPropertyCoordinator();
            CaseIntegrator ci = getCaseIntegrator();
            PersonIntegrator pi = getPersonIntegrator();
            
            PropertyDataHeavy p = new PropertyDataHeavy(getProperty(propertyID));
   
            p.setCeCaseList(ci.getCECasesByProp(p));
            p.setUnitWithListsList(getPropertyUnitWithListsList(p.getUnitList()));
            p.setPersonList(pi.getPersonList(p));
            
        return pc.configurePropertyWithLists(p);
    }

    /**
     * Dumps all Property records for a given User and lets the caller sort through them
     * @param cred
     * @param u
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

        return propList;
    }
    
     public PropertyUnit getPropertyUnitByPropertyUnitID(int propUnitID) throws IntegrationException {
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
                unitList.add(getPropertyUnitByPropertyUnitID(rs.getInt("unitid")));
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
     * 
     * @param propUnitList
     * @return
     * @throws IntegrationException
     * @throws EventException
     */
    public List<PropertyUnitDataHeavy> getPropertyUnitWithListsList(List<PropertyUnit> propUnitList) throws IntegrationException, EventException, EventException, AuthorizationException, CaseLifecycleException{
        List<PropertyUnitDataHeavy> puwll = new ArrayList<>();
        Iterator<PropertyUnit> iter = propUnitList.iterator();
        while(iter.hasNext()){
            try {
                PropertyUnit pu = iter.next();
                puwll.add(getPropertyUnitWithLists(pu.getUnitID()));
            } catch (ViolationException ex) {
                System.out.println(ex);
            }
        }
        return puwll;
    }
    
    /**
     * Adaptor method for calling getPropertyUnitWithLists(int unitID) given a PropertyUnit object
     * 
     * @param pu
     * @param u
     * @return a PropertyUnit containing a list of OccPeriods, and more in the future
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.EventException 
     * @throws com.tcvcog.tcvce.domain.AuthorizationException 
     */
    public PropertyUnitDataHeavy getPropertyUnitWithList(PropertyUnit pu) throws IntegrationException, EventException, AuthorizationException, CaseLifecycleException{
        PropertyUnitDataHeavy puwl = null;
        try {
            puwl = getPropertyUnitWithLists(pu.getUnitID());
        } catch (ViolationException ex) {
            System.out.println(ex);
        }
        return puwl;
    }
    
    public PropertyUnitDataHeavy getPropertyUnitWithLists(int unitID) throws IntegrationException, EventException, AuthorizationException, CaseLifecycleException, ViolationException{
        OccupancyIntegrator oi = getOccupancyIntegrator();
        PropertyUnitDataHeavy puwl = new PropertyUnitDataHeavy(getPropertyUnitByPropertyUnitID(unitID));
        puwl.setPeriodList(oi.getOccPeriodList(unitID));
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
        
        PropertyUnitWithProp puwp = new PropertyUnitWithProp(getPropertyUnitByPropertyUnitID(unitID));
        puwp.setProperty(pi.getProperty(puwp.getPropertyID()));
        
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

        PropertyUnit skeleton = pi.getPropertyUnitByPropertyUnitID(uc.getUnitID());

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
