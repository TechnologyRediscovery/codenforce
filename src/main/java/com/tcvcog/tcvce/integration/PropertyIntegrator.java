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
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyWithLists;
import com.tcvcog.tcvce.entities.User;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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
     * Utility method for property search methods whose individual
     * SQL statements implement various search features. These methods
     * can send properly configured (i.e. cursor positioned) ResultSet
     * objects to this method and get back a populated Property object
     * 
     * @param rs
     * @return the fully baked Property with all fields set from DB data
     */
    private Property generateProperty(ResultSet rs) throws IntegrationException{
        
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
       
        Property p = new Property();
        
        try{
            p.setPropertyID(rs.getInt("propertyid"));
            p.setMuni(mi.getMuni(rs.getInt("municipality_muniCode")));
            p.setMuniCode(rs.getInt("municipality_muniCode"));
            p.setParID(rs.getString("parid"));
            
            p.setLotAndBlock(rs.getString("lotandblock"));
            p.setAddress(rs.getString("address"));
            p.setPropertyUseType(rs.getString("propertyusetype"));
            
            p.setUseGroup(rs.getString("usegroup"));
            p.setConstructionType(rs.getString("constructiontype"));
            p.setCountyCode(rs.getString("countycode"));
            p.setNotes(rs.getString("notes"));
        } catch (SQLException ex){
            System.out.println(ex);
            throw new IntegrationException("Error generating Property from ResultSet", ex);
        }
        return p;
    }
    
    /** 
     * Utility method for property search methods whose individual
     * SQL statements implement various search features. These methods
     * can send properly configured (i.e. cursor positioned) ResultSet
     * objects to this method and get back a populated Property object
     * 
     * @param rs
     * @return the fully baked Property with all fields set from DB data
     */
    private PropertyWithLists generatePropertyWithLists(ResultSet rs) throws IntegrationException{
        
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
       
        PropertyWithLists p = new PropertyWithLists();
        
        try{
            p.setPropertyID(rs.getInt("propertyid"));
            p.setMuni(mi.getMuni(rs.getInt("municipality_muniCode")));
            p.setMuniCode(rs.getInt("municipality_muniCode"));
            p.setParID(rs.getString("parid"));
            
            p.setLotAndBlock(rs.getString("lotandblock"));
            p.setAddress(rs.getString("address"));
            p.setPropertyUseType(rs.getString("propertyusetype"));  //use type name
            
            p.setUseGroup(rs.getString("usegroup"));
            p.setConstructionType(rs.getString("constructiontype"));
            p.setCountyCode(rs.getString("countycode"));
            p.setNotes(rs.getString("notes"));
            
           
            //p.setNotes(rs.getString("notes"));
            
            
        } catch (SQLException ex){
            System.out.println(ex);
            throw new IntegrationException("Error generating Property from ResultSet", ex);
        }
        return p;
    }
    
    public ArrayList<Property> searchForProperties(String houseNum, String street) throws IntegrationException{
    
        String query = "select propertyid, municipality_municode, parid, lotandblock, address, \n" +
"       propertyusetype, usegroup, constructiontype, countycode, apartmentno, \n" +
"       notes, addr_city, addr_state, addr_zip, ownercode, propclass, \n" +
"       lastupdated, lastupdatedby, locationdescription, datasource, \n" +
"       containsrentalunits, vacant FROM property WHERE address ILIKE ?;";
        
        System.out.println("PropertyIntegrator.searchForPropertiesAddOnly - query: " + query);
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
            
        ArrayList<Property> propList = new ArrayList<>();
 
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, "%" + houseNum + "%" + street + "%");
            System.out.println("PropertyIntegrator.searchForProperties | SQL: " + stmt.toString());
            rs = stmt.executeQuery();
            int counter = 0;
            while(rs.next() && counter <= MAX_RESULTS){
                propList.add(generateProperty(rs));
                counter++;
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return propList;
        
    }
    
    public ArrayList<Property> searchForProperties(String houseNum, String street, int muniID) throws IntegrationException{
        String query = "SELECT propertyid, municipality_municode, parid, lotandblock, address, \n" +
"       propertyusetype, usegroup, constructiontype, countycode, apartmentno, \n" +
"       notes, addr_city, addr_state, addr_zip, ownercode, propclass, \n" +
"       lastupdated, lastupdatedby, locationdescription, datasource, \n" +
"       containsrentalunits, vacant FROM property WHERE address ILIKE ? AND municipality_muniCode=?;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
            
        ArrayList<Property> propList = new ArrayList<>();
 
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, "%" + houseNum + "%" + street + "%");
            stmt.setInt(2, muniID);
            rs = stmt.executeQuery();
            System.out.println("PropertyIntegrator.searchForProperties - with muni | sql: " + stmt.toString());
            int counter = 0;
            while(rs.next() && counter <= MAX_RESULTS){
                propList.add(generateProperty(rs));
                counter++;
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error searching for properties", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return propList;
        
    }
    
    public int insertProperty(Property newProp) throws IntegrationException{
        int propID;
        // Validation would be dope
        
        String query = "INSERT INTO public.property(\n" +
                "       propertyid, \n" + // DEFAULT
                "       municipality_municode, \n" +
                "       parid, lotandblock, \n" +
                "       address, propertyusetype, \n" +
                "       usegroup, constructiontype, countycode, notes, \n" +
                "       containsrentalunits, multiunit, vacant, \n" +
                "       lastupdatedby, lastupdated )\n" +
                "   VALUES ( DEFAULT,\n" +
                "       ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; // im so sorry 
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            
            stmt.setInt(1, newProp.getMuniCode());
            
            stmt.setString(2, newProp.getParID());
            stmt.setString(3, newProp.getLotAndBlock());
            
            stmt.setString(4, newProp.getAddress());
            stmt.setString(5, newProp.getPropertyUseType());
            
            stmt.setString(6, newProp.getUseGroup());
            stmt.setString(7, newProp.getConstructionType());
            stmt.setString(8, newProp.getCountyCode());
            stmt.setString(9, newProp.getNotes());
            
            stmt.setBoolean(10, newProp.isRental());  // containsrentalunits=?
            stmt.setBoolean(11, newProp.isMultiUnit());  // multiunit=?
            stmt.setBoolean(12, newProp.isVacant());  // vacant=?
            
            stmt.setInt(13, getSessionBean().getFacesUser().getUserID());
            stmt.setTimestamp(14, Timestamp.valueOf(LocalDateTime.now()));
            
            // TODO: add event to dumby tracker case on this property to track who/when of changes
            
            // figure out if we need to do changes in the list elements
            
            stmt.execute();
            
            // grab the newly inserted propertyid
            String idNumQuery = "SELECT currval('propertyid_seq');";
            Statement s = con.createStatement();
            ResultSet rs;
            int lastID;
            rs = s.executeQuery(idNumQuery);
            rs.next();
            lastID = rs.getInt(1);
            return lastID;
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error inserting property. ", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        
    }
    
    public List<Property> getProperties(Person p) throws IntegrationException{
         String query = "SELECT property_propertyid FROM propertyperson WHERE person_personid = ?;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<Property> pList = new ArrayList<>();
 
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, p.getPersonID());
            rs = stmt.executeQuery();
            while(rs.next()){
                pList.add(getProperty(rs.getInt("property_propertyid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getProperty | Unable to retrieve property by ID number", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return pList;
        
    }
  
    public String updateProperty(Property propToUpdate) throws IntegrationException{
        String query = "UPDATE public.property\n" +
                "   SET parid=?, lotandblock=?, \n" +
                "       address=?, propertyusetype=?, \n" +
                "       usegroup=?, constructiontype=?, countycode=?, notes=?, \n" +
                "       containsrentalunits=?, vacant=?, \n" +
                "       lastupdatedby=?, lastupdated=?" +
                " WHERE propertyid = ?;";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
 
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, propToUpdate.getParID());
            stmt.setString(2, propToUpdate.getLotAndBlock());
            
            stmt.setString(3, propToUpdate.getAddress());
            stmt.setString(4, propToUpdate.getPropertyUseType());
            
            stmt.setString(5, propToUpdate.getUseGroup());
            stmt.setString(6, propToUpdate.getConstructionType());
            stmt.setString(7, propToUpdate.getCountyCode());
            stmt.setString(8, propToUpdate.getNotes());
            
            stmt.setBoolean(9, propToUpdate.isRental());  // containsrentalunits=?
            stmt.setBoolean(10, propToUpdate.isVacant());  // vacant=?
            
            stmt.setInt(11, getSessionBean().getFacesUser().getUserID());
            stmt.setTimestamp(12, Timestamp.valueOf(LocalDateTime.now()));
            
            // TODO: add event to dumby tracker case on this property to track who/when of changes
            // figure out if we need to do changes in the list elements
            
            stmt.setInt(13, propToUpdate.getPropertyID());
            
            stmt.executeUpdate();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build propertyUseTypesMap", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return "propertyProfile";
        
    }
    
    
     public Property getProperty(int propertyID) throws IntegrationException{
        Property p = new Property();
        // not needed after converting property use type to a simple string without a lookup table
//         String query = "SELECT * from property LEFT OUTER JOIN propertyusetype ON public.propertyusetype.propertyUseTypeID = public.property.propertyusetype_useid "
//                + " WHERE propertyid = ?;";
         String query = "SELECT * from property WHERE propertyid = ?;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
 
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, propertyID);
            rs = stmt.executeQuery();
            while(rs.next()){
                p = generateProperty(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getProperty | Unable to retrieve property by ID number", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return p;
        
    } // close getProperty()
    
    public PropertyWithLists getPropertyWithLists(int propertyID) throws IntegrationException, CaseLifecyleException{
        PropertyWithLists p = new PropertyWithLists();
        String query = "SELECT * from property WHERE propertyid = ?;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        CaseIntegrator ci = getCaseIntegrator();
        PersonIntegrator pi = getPersonIntegrator();
 
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, propertyID);
            rs = stmt.executeQuery();
            while(rs.next()){
                p = generatePropertyWithLists(rs);
                p.setCeCaseList(ci.getCECasesByProp(p));
                p.setUnitList(getPropertyUnitList(p));
                p.setPersonList(pi.getPersonList(p));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getProperty | Unable to retrieve property by ID number", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return p;
        
    }
    
    public List<Property> getPropertyHistoryList(User u) throws IntegrationException{
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
    
    public void insertPropertyUnit(PropertyUnit pu) throws IntegrationException{
         String query = "INSERT INTO public.propertyunit(\n" +
                        "            unitid, unitnumber, property_propertyid, otherknownaddress, notes, \n" +
                        "            rental)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
                        "            ?);";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
 
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, pu.getUnitNumber());
            stmt.setInt(2, pu.getThisProperty().getPropertyID());
            stmt.setString(3, pu.getOtherKnownAddress());
            stmt.setString(4, pu.getNotes());
            stmt.setBoolean(5, pu.isRental());
            
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getProperty | Unable to retrieve property by ID number", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    
    public ArrayList<PropertyUnit> getPropertyUnitList(Property p) throws IntegrationException{
        ArrayList<PropertyUnit> unitList = new ArrayList();
        
         String query = "SELECT unitid, unitnumber, property_propertyid, otherknownaddress, propertyunit.notes, \n" +
                        "       rental\n" +
                        "  FROM propertyunit JOIN property ON propertyunit.property_propertyID = property.propertyid\n" +
                        "  WHERE property.propertyid=?;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
 
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, p.getPropertyID());
            rs = stmt.executeQuery();
            while(rs.next()){
                unitList.add(generatePropertyUnit(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build property unit list due to an DB integration error", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return unitList;
    }
    
    private PropertyUnit generatePropertyUnit(ResultSet rs) throws SQLException, IntegrationException{
        PersonIntegrator persInt = getPersonIntegrator();
        PropertyUnit pu = new PropertyUnit();
        pu.setUnitID(rs.getInt("unitid"));
        pu.setUnitNumber(rs.getString("unitnumber"));
        pu.setNotes(rs.getString("notes"));
        pu.setOtherKnownAddress(rs.getString("otherknownaddress"));
        pu.setRental(rs.getBoolean("rental"));
        pu.setThisProperty(getProperty(rs.getInt("property_propertyid")));
        pu.setPropertyUnitPeople(persInt.getPersonList(rs.getInt("property_propertyid")));
        return pu;
    }
    
    public void updatePropertyUnit(PropertyUnit pu) throws IntegrationException{
        
        
    }
    
    public void deletePropertyUnit(PropertyUnit pu){
        
       
    }
    
    public PropertyWithLists getNewPropertyWithLists(){
        return new PropertyWithLists();
    }
    
} // close class
