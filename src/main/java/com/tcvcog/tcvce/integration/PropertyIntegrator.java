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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyWithLists;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ArrayList;

/**
 *
 * @author Eric Darsow
 */
public class PropertyIntegrator extends BackingBeanUtils implements Serializable {
    

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
    private Property generatePropertyFromRS(ResultSet rs) throws IntegrationException{
        
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
       
        Property p = new Property();
        
        try{
            p.setPropertyID(rs.getInt("propertyid"));
            p.setMuni(mi.getMuniFromMuniCode(rs.getInt("municipality_muniCode")));
            p.setMuniCode(rs.getInt("municipality_muniCode"));
            p.setParID(rs.getString("parid"));
            
            p.setLotAndBlock(rs.getString("lotandblock"));
            p.setAddress(rs.getString("address"));
            p.setPropertyUseType(rs.getString("propertyusetype"));
            
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
            p.setMuni(mi.getMuniFromMuniCode(rs.getInt("municipality_muniCode")));
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
    
    public ArrayList<Property> searchForProperties(String addrPart) throws IntegrationException{
    
        String query = "select * from property WHERE address ILIKE '%" + addrPart + "%';";
        
        System.out.println("PropertyIntegrator.searchForPropertiesAddOnly - query: " + query);
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        Statement stmt = null;
            
        ArrayList<Property> propList = new ArrayList<>();
 
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while(rs.next()){
                propList.add(generatePropertyFromRS(rs));
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
    
    public ArrayList<Property> searchForProperties(String addrPart, int muniID) throws IntegrationException{
        
        
        
        String query = "SELECT * from property WHERE address ILIKE '%" + addrPart + "%' AND municipality_muniCode=" 
                + muniID + ";";
//        String query = "select * from property LEFT OUTER JOIN propertyusetype ON public.propertyusetype.propertyUseTypeID = public.property.propertyUseType_UseID "
//                + " WHERE address ILIKE '%" + addrPart + "%' AND municipality_muniCode=" 
//                + muniID + ";";
        System.out.println("PropertyIntegrator.searchForProperties - with muni | sql: " + query);
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        Statement stmt = null;
            
        ArrayList<Property> propList = new ArrayList<>();
 
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while(rs.next()){
                propList.add(generatePropertyFromRS(rs));
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
    
    // deprecated method after removing lookup table functionality for propertyusetype
    
//    public HashMap getPropertyUseTypesMap() throws IntegrationException{
//        
//        String query = "SELECT * FROM public.propertyusetype;";
//        
//        Connection con = getPostgresCon();
//        ResultSet rs = null;
//        Statement stmt = null;
//            
//        HashMap<String, Integer> propertyUseTypeMap = new HashMap();
// 
//        try {
//            stmt = con.createStatement();
//            rs = stmt.executeQuery(query);
//            System.out.println("PropertyIntegrator.getPropertyUseTypesMap | sql: " + query);
//            while(rs.next()){
//                propertyUseTypeMap.put(rs.getString("name"),rs.getInt("propertyUseTypeID"));
//            }
//        } catch (SQLException ex) {
//            System.out.println(ex.toString());
//            throw new IntegrationException("Unable to build propertyUseTypesMap", ex);
//        } finally{
//             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
//             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
//             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
//        } // close finally
//        
//        return propertyUseTypeMap;
//        
//    }
    
    public String updateProperty(Property propToUpdate) throws IntegrationException{
        String query = "UPDATE public.property\n" +
                "   SET parid=?, lotandblock=?, \n" +
                "       address=?, propertyUseType_UseID=?, rental=?, multiunit=?, \n" +
                "       usegroup=?, constructiontype=?, countycode=?, notes=?" +
                " WHERE propertyid = ?;";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
 
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, propToUpdate.getParID());
            stmt.setString(2, propToUpdate.getLotAndBlock());
            
            stmt.setString(3, propToUpdate.getAddress());
            stmt.setString(4, propToUpdate.getPropertyUseType());
            
            stmt.setString(7, propToUpdate.getUseGroup());
            stmt.setString(8, propToUpdate.getConstructionType());
            stmt.setString(9, propToUpdate.getCountyCode());
            stmt.setString(10, propToUpdate.getNotes());
            
            // figure out if we need to do changes in the list elements
            
            stmt.setInt(11, propToUpdate.getPropertyID());
            
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
                p = generatePropertyFromRS(rs);
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
    
    public PropertyWithLists getPropertyWithLists(int propID) throws IntegrationException{
        PropertyWithLists p = new PropertyWithLists();
         String query = "SELECT * from property WHERE propertyid = ?;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        CaseIntegrator ci = getCaseIntegrator();
        PersonIntegrator pi = getPersonIntegrator();
 
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, propID);
            rs = stmt.executeQuery();
            while(rs.next()){
                p = generatePropertyWithLists(rs);
                p.setPropertyCaseList(ci.getCECasesByProp(p));
                p.setPropertyUnitList(getPropertyUnitList(p));
                p.setPropertyPersonList(pi.getPersonList(p));
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
        pu.setThisProperty(getProperty(rs.getInt("property_propertyin")));
        pu.setPropertyUnitPeople(persInt.getPersonList(rs.getInt("property_propertyid")));
        
        
        return pu;
    }
    
    public void updatePropertyUnit(PropertyUnit pu) throws IntegrationException{
        
        
    }
    
    public void deletePropertyUnit(PropertyUnit pu){
        
       
    }
    
} // close class
