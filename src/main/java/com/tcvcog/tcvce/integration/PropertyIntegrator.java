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

import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.coordinators.*;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.search.SearchParamsMailingCityStateZip;
import com.tcvcog.tcvce.entities.search.SearchParamsProperty;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import java.io.Serializable;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Inserts, retrieves, updates, and deactivates property-related fields and tables
 * in the Database
 * @author Ellen Bascomb (Apartment 31Y)
 */
public class PropertyIntegrator extends BackingBeanUtils implements Serializable {

    final int MAX_RESULTS = 100;
    final String PARCEL_ACTIVE_FIELD = "parcel.deactivatedts";
    final String HUMAN_PARCEL_ROLE_TABLE_NAME = "humanparcelrole";
    final String HUMAN_MAILING_ROLE_TABLE_NAME = "humanmailingrole";
    

    /**
     * Creates a new instance of PropertyIntegrator
     */
    public PropertyIntegrator() {
        
    }
    
    /**
     * Extracts a Parcel from the DB
     * @param parcelkey
     * @return
     * @throws IntegrationException 
     */
    public Parcel getParcel(int parcelkey) throws IntegrationException{
        
        Parcel p = null;
        String query =  "SELECT parcelkey, muni_municode, parcelidcnty, source_sourceid, createdts, createdby_userid, \n" +
                        "       lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, \n" +
                        "       notes, lotandblock \n" +
                        "  FROM public.parcel WHERE parcelkey=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, parcelkey);
            rs = stmt.executeQuery();
            while (rs.next()) {
                p = generateParcel(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getParcel| Unable to retrieve parcel by key", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return p;
    }
    
    /**
     * Populates fields on a Parcel object
     * @param rs retrieved from the DB with all columns SELECTed
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
    private Parcel generateParcel(ResultSet rs) throws SQLException, IntegrationException{
        try {
            SystemIntegrator si = getSystemIntegrator();
            MunicipalityIntegrator mi = getMunicipalityIntegrator();
            if(rs == null){
                return  null;
            }
            
            Parcel parcel = new Parcel();
            
            parcel.setParcelKey(rs.getInt("parcelkey"));
            parcel.setCountyParcelID(rs.getString("parcelidcnty"));
            parcel.setMuni(mi.getMuni(rs.getInt("muni_municode")));
            if(rs.getInt("source_sourceid") != 0){
                parcel.setSource(si.getBOBSource(rs.getInt("source_sourceid")));
            }
            parcel.setLotAndBlock(rs.getString("lotandblock"));
            si.populateTrackedFields(parcel, rs, false);
            return parcel;
        } catch (BObStatusException ex) {
            throw new IntegrationException(ex.getMessage());
            
        }
        
    }
    
    /**
     * Extracts a Parcel from the DB
     * @param countyParcelID the County Parcel ID
     * @return fully-baked parcel
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public Parcel getParcelByParID(int countyParcelID) throws IntegrationException{
        
        Parcel p = null;
        PropertyCoordinator pc = getPropertyCoordinator();
        String query = "SELECT parcelkey, muni_municode, parcelidcnty, source_sourceid, createdts, createdby_userid, \n" +
                        "       lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, \n" +
                        "       notes, lotandblock \n" +
                        "  FROM public.parcel WHERE parcelidcnty=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, countyParcelID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                p = generateParcel(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getParcel | Unable to retrieve parcel by county Parcel ID", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return p;
    }
    
    
    /**
     * 
     * @param parcel
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
      public List<Integer> getParcelInfoByParcel(Parcel parcel) throws IntegrationException, BObStatusException {
        if(parcel == null){
            throw new BObStatusException("Cannot get a parcel info ID with null Parcel");
        }
        
        String query =  "SELECT parcelinfoid FROM public.parcelinfo WHERE parcel_parcelkey = ? AND deactivatedts IS NULL ORDER BY lastupdatedts DESC;";
        List<Integer> infoIDL = new ArrayList<>();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, parcel.getParcelKey());
            rs = stmt.executeQuery();
            while (rs.next()) {
                infoIDL.add(rs.getInt("parcelinfoid"));
                
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getProperty | Unable to retrieve property by ID number", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return infoIDL;

    } // close getProperty()
    
    
    
    /**
     * Extracts property from DB
     * @param infoRecordID
     * @return
     * @throws IntegrationException 
     */
    public ParcelInfo getParcelInfo(int infoRecordID) throws IntegrationException, BObStatusException {
        ParcelInfo pi = null;
        
        PropertyCoordinator pc = getPropertyCoordinator();
        String query =  "SELECT parcelinfoid, parcel_parcelkey, usegroup, constructiontype, countycode, \n" +
                        "       notes, ownercode, propclass, locationdescription, bobsource_sourceid, \n" +
                        "       unfitdatestart, unfitdatestop, unfitby_userid, abandoneddatestart, \n" +
                        "       abandoneddatestop, abandonedby_userid, vacantdatestart, vacantdatestop, \n" +
                        "       vacantby_userid, condition_intensityclassid, landbankprospect_intensityclassid, \n" +
                        "       landbankheld, nonaddressable, usetype_typeid, createdts, createdby_userid, \n" +
                        "       lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid\n" +
                        "   FROM public.parcelinfo WHERE parcelinfoid = ?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, infoRecordID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                pi = generateParcelInfo(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getProperty | Unable to retrieve property by ID number", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return pi;

    } // close getProperty()
    
    
    /**
     * writes a new record to the parcelinfo table
     * @param info
     * @return 
     */
    public int insertParcelInfo(ParcelInfo info) throws BObStatusException, IntegrationException{
        if(info == null){
            throw new BObStatusException("Cannot insert parcel info with null info input!");
        }
            
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int freshID = 0;

        try {
            
            String s =  "INSERT INTO public.parcelinfo(\n" +
                        "            parcelinfoid, parcel_parcelkey, usegroup, constructiontype, countycode, \n" + // 1-4
                        "            notes, ownercode, propclass, locationdescription, bobsource_sourceid, \n" + // 5-9
                        "            unfitdatestart, unfitdatestop, unfitby_userid, abandoneddatestart, \n" + // 10-13
                        "            abandoneddatestop, abandonedby_userid, vacantdatestart, vacantdatestop, \n" + // 14-17
                        "            vacantby_userid, condition_intensityclassid, landbankprospect_intensityclassid, \n" + // 18-20
                        "            landbankheld, nonaddressable, usetype_typeid, createdts, createdby_userid, \n" + //21-24
                        "            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid)\n" + // 25
                        "    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?, ?, ?, now(), ?, \n" +
                        "            now(), ?, NULL, NULL);";
            
            stmt = con.prepareStatement(s);
            
            stmt.setInt(1, info.getParcelInternalID());
            stmt.setString(2, info.getUseGroup());
            stmt.setString(3, info.getConstructionType());
            stmt.setString(4, info.getCountyCode());
            
            stmt.setString(5, info.getNotes());
            stmt.setString(6, info.getOwnerCode());
            stmt.setString(7, info.getPropClass());
            if(info.getLocationDescriptor() != null){
                stmt.setInt(8, info.getLocationDescriptor().getLocationID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            if(info.getBobSource() != null){
                stmt.setInt(9, info.getBobSource().getSourceid());
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
                    
            if(info.getUnfitDateStart() != null){
                stmt.setTimestamp(10, java.sql.Timestamp.valueOf(info.getUnfitDateStart().atStartOfDay()));
            } else {
                stmt.setNull(10, java.sql.Types.NULL);
            }
            if(info.getUnfitDateStop() != null){
                stmt.setTimestamp(11, java.sql.Timestamp.valueOf(info.getUnfitDateStop().atStartOfDay()));
            } else {
                stmt.setNull(11, java.sql.Types.NULL);
            }
            if(info.getUnfitBy() != null){
                stmt.setInt(12, info.getUnfitBy().getUserID());
            } else {
                stmt.setNull(12, java.sql.Types.NULL);
            }
                   
                    
            if(info.getAbandonedDateStart() != null){
                stmt.setTimestamp(13, java.sql.Timestamp.valueOf(info.getAbandonedDateStart().atStartOfDay()));
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }
            if(info.getAbandonedDateStop() != null){
                stmt.setTimestamp(14, java.sql.Timestamp.valueOf(info.getAbandonedDateStop().atStartOfDay()));
            } else {
                stmt.setNull(14, java.sql.Types.NULL);
            }
            if(info.getAbandonedBy() != null){
                stmt.setInt(15, info.getAbandonedBy().getUserID());
            } else {
                stmt.setNull(15, java.sql.Types.NULL);
            }
                   
            if(info.getVacantDateStart() != null){
                stmt.setTimestamp(16, java.sql.Timestamp.valueOf(info.getVacantDateStart().atStartOfDay()));
            } else {
                stmt.setNull(16, java.sql.Types.NULL);
            }
            if(info.getVacantDateStop() != null){
                stmt.setTimestamp(17, java.sql.Timestamp.valueOf(info.getVacantDateStop().atStartOfDay()));
            } else {
                stmt.setNull(17, java.sql.Types.NULL);
            }
            if(info.getVacantBy() != null){
                stmt.setInt(18, info.getVacantBy().getUserID());
            } else {
                stmt.setNull(18, java.sql.Types.NULL);
            }
                   
            
            if(info.getCondition() != null){
                stmt.setInt(19, info.getCondition().getClassID());
            } else {
                stmt.setNull(19, java.sql.Types.NULL);
            }
            if(info.getLandBankProspect() != null){
                stmt.setInt(20, info.getLandBankProspect().getClassID());
            } else {
                stmt.setInt(20, java.sql.Types.NULL);
            }
            
            stmt.setBoolean(21, info.isLandBankHeld());
            stmt.setBoolean(22, info.isNonAddressable());
            
            if(info.getUseType() != null){
                stmt.setInt(23, info.getUseType().getTypeID());
            } else {
                stmt.setNull(23, java.sql.Types.NULL);
            }
            
            if(info.getCreatedBy() != null){
                stmt.setInt(24, info.getCreatedBy().getUserID());
            } else {
                stmt.setNull(24, java.sql.Types.NULL);
            }
            
            
            if(info.getLastUpdatedBy() != null){
                stmt.setInt(25, info.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(25, java.sql.Types.NULL);
            }
            stmt.execute();

            String idNumQuery = "SELECT currval('parcelinfo_infoid_seq');";
            PreparedStatement st = con.prepareStatement(idNumQuery);
            rs = st.executeQuery();
            while(rs.next()){
                freshID = rs.getInt("currval");
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert Parcel into DB, sorry!", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return freshID;
        
    }
    
    
    /**
     * Updates a record in the parcelinfo table
     * @param info
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public void updateParcelInfo(ParcelInfo info) throws BObStatusException, IntegrationException{
        if(info == null){
            throw new BObStatusException("Cannot update parcel info with null info input!");
        }
           Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int freshID = 0;

        try {
            
            String s =  "UPDATE public.parcelinfo\n" +
                        "   SET parcel_parcelkey=?, usegroup=?, constructiontype=?, \n" +
                        "       countycode=?, notes=?, ownercode=?, propclass=?, locationdescription=?, \n" +
                        "       bobsource_sourceid=?, unfitdatestart=?, unfitdatestop=?, unfitby_userid=?, \n" +
                        "       abandoneddatestart=?, abandoneddatestop=?, abandonedby_userid=?, \n" +
                        "       vacantdatestart=?, vacantdatestop=?, vacantby_userid=?, condition_intensityclassid=?, \n" +
                        "       landbankprospect_intensityclassid=?, landbankheld=?, nonaddressable=?, \n" +
                        "       usetype_typeid=?, lastupdatedts=now(), \n" +
                        "       lastupdatedby_userid=?, deactivatedts=?, deactivatedby_userid=?\n" +
                        " WHERE parcelinfoid=?;";
            
            stmt = con.prepareStatement(s);
            
            stmt.setInt(1, info.getParcelInternalID());
            stmt.setString(2, info.getUseGroup());
            stmt.setString(3, info.getConstructionType());
            stmt.setString(4, info.getCountyCode());
            
            stmt.setString(5, info.getNotes());
            stmt.setString(6, info.getOwnerCode());
            stmt.setString(7, info.getPropClass());
            if(info.getLocationDescriptor() != null){
                stmt.setInt(8, info.getLocationDescriptor().getLocationID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            if(info.getBobSource() != null){
                stmt.setInt(9, info.getBobSource().getSourceid());
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
                    
            if(info.getUnfitDateStart() != null){
                
                stmt.setTimestamp(10, java.sql.Timestamp.valueOf(info.getUnfitDateStart().atStartOfDay()));
            } else {
                stmt.setNull(10, java.sql.Types.NULL);
            }
            if(info.getUnfitDateStop() != null){
                stmt.setTimestamp(11, java.sql.Timestamp.valueOf(info.getUnfitDateStop().atStartOfDay()));
            } else {
                stmt.setNull(11, java.sql.Types.NULL);
            }
            if(info.getUnfitBy() != null){
                stmt.setInt(12, info.getUnfitBy().getUserID());
            } else {
                stmt.setNull(12, java.sql.Types.NULL);
            }
                   
                    
            if(info.getAbandonedDateStart() != null){
                stmt.setTimestamp(13, java.sql.Timestamp.valueOf(info.getAbandonedDateStart().atStartOfDay()));
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }
            if(info.getAbandonedDateStop() != null){
                stmt.setTimestamp(14, java.sql.Timestamp.valueOf(info.getAbandonedDateStop().atStartOfDay()));
            } else {
                stmt.setNull(14, java.sql.Types.NULL);
            }
            if(info.getAbandonedBy() != null){
                stmt.setInt(15, info.getAbandonedBy().getUserID());
            } else {
                stmt.setNull(15, java.sql.Types.NULL);
            }
                   
            if(info.getVacantDateStart() != null){
                stmt.setTimestamp(16, java.sql.Timestamp.valueOf(info.getVacantDateStart().atStartOfDay()));
            } else {
                stmt.setNull(16, java.sql.Types.NULL);
            }
            if(info.getVacantDateStop() != null){
                stmt.setTimestamp(17, java.sql.Timestamp.valueOf(info.getVacantDateStop().atStartOfDay()));
            } else {
                stmt.setNull(17, java.sql.Types.NULL);
            }
            if(info.getVacantBy() != null){
                stmt.setInt(18, info.getVacantBy().getUserID());
            } else {
                stmt.setNull(18, java.sql.Types.NULL);
            }
            
            if(info.getCondition() != null){
                stmt.setInt(19, info.getCondition().getClassID());
            } else {
                stmt.setNull(19, java.sql.Types.NULL);
            }
            if(info.getLandBankProspect() != null){
                stmt.setInt(20, info.getLandBankProspect().getClassID());
            } else {
                stmt.setInt(20, java.sql.Types.NULL);
            }
            
            stmt.setBoolean(21, info.isLandBankHeld());
            stmt.setBoolean(22, info.isNonAddressable());
            
            if(info.getUseType() != null){
                stmt.setInt(23, info.getUseType().getTypeID());
            } else {
                stmt.setNull(23, java.sql.Types.NULL);
            }
            
            if(info.getLastUpdatedBy() != null){
                stmt.setInt(24, info.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(24, java.sql.Types.NULL);
            }
            
            if(info.getDeactivatedBy() != null){
                stmt.setInt(25, info.getDeactivatedBy().getUserID());
            } else {
                stmt.setNull(25, java.sql.Types.NULL);
            }
            
            if(info.getDeactivatedTS() != null){
                stmt.setTimestamp(26, java.sql.Timestamp.valueOf(info.getDeactivatedTS()));
            } else {
                stmt.setNull(26, java.sql.Types.NULL);
            }
            
            stmt.setInt(27, info.getParcelInfoID());
            stmt.execute();
            
        } catch (SQLException ex){
            throw new IntegrationException("cannot update parcel info");
            
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }    
        }
    }
    
    
    
  
     /**
     * Utility method for property search methods whose individual SQL
     * statements implement various search features. These methods can send
     * properly configured (i.e. cursor positioned) ResultSet objects to this
     * method and get back a populated Property object
     * @param rs
     * @return the fully baked Property with all fields set from DB data
     */
    private ParcelInfo generateParcelInfo(ResultSet rs) throws IntegrationException, BObStatusException {

        OccInspectionIntegrator ci = getOccInspectionIntegrator();
        SystemIntegrator si = getSystemIntegrator();
        UserIntegrator ui = getUserIntegrator();
        

        ParcelInfo pi = new ParcelInfo();

        try {
            pi.setParcelInfoID(rs.getInt("parcelinfoid"));
            pi.setParcelInternalID(rs.getInt("parcel_parcelkey"));
            pi.setUseGroup(rs.getString("usegroup"));
            
            pi.setConstructionType(rs.getString("constructiontype"));
            pi.setCountyCode(rs.getString("countycode"));
            pi.setOwnerCode(rs.getString("ownercode"));  // for legacy compat
            pi.setPropClass(rs.getString("propclass"));
            
            if(rs.getInt("locationdescription") != 0){
                pi.setLocationDescriptor(ci.getLocationDescriptor(rs.getInt("locationdescription")));
            }
            
            if(rs.getInt("bobsource_sourceid") != 0){
                pi.setBobSource(si.getBOBSource(rs.getInt("bobsource_sourceid")));
            }
            
            if(rs.getTimestamp("unfitdatestart") != null){
                pi.setUnfitDateStart(LocalDate.from(rs.getTimestamp("unfitdatestart").toLocalDateTime()));
            }
            
            if(rs.getTimestamp("unfitdatestop") != null){
                pi.setUnfitDateStop(LocalDate.from(rs.getTimestamp("unfitdatestop").toLocalDateTime()));
            }
            
            if(rs.getInt("unfitby_userid") != 0){
                pi.setUnfitBy(ui.getUser(rs.getInt("unfitby_userid")));
            }
                
            if(rs.getTimestamp("abandoneddatestart") != null){
                pi.setAbandonedDateStart(LocalDate.from(rs.getTimestamp("abandoneddatestart").toLocalDateTime()));
            }
            
            if(rs.getTimestamp("abandoneddatestop") != null){
                pi.setAbandonedDateStop(LocalDate.from(rs.getTimestamp("abandoneddatestop").toLocalDateTime()));
            }
            
            if(rs.getInt("abandonedby_userid") != 0){
                pi.setAbandonedBy(ui.getUser(rs.getInt("abandonedby_userid")));
            }
            
            if(rs.getTimestamp("vacantdatestart") != null){
                pi.setVacantDateStart(LocalDate.from(rs.getTimestamp("vacantdatestart").toLocalDateTime()));
            }
            
            if(rs.getTimestamp("vacantdatestop") != null){
                pi.setVacantDateStop(LocalDate.from(rs.getTimestamp("vacantdatestop").toLocalDateTime()));
            }
            
            if(rs.getInt("vacantby_userid") != 0){
                pi.setVacantBy(ui.getUser(rs.getInt("vacantby_userid")));
            }
            
            if(rs.getInt("condition_intensityclassid") != 0){
                pi.setCondition(si.getIntensityClass(rs.getInt("condition_intensityclassid")));
            }
            
            if(rs.getInt("landbankprospect_intensityclassid") != 0){
                pi.setLandBankProspect(si.getIntensityClass(rs.getInt("landbankprospect_intensityclassid")));
            }
            
            pi.setLandBankHeld(rs.getBoolean("landbankheld"));
            pi.setNonAddressable(rs.getBoolean("nonaddressable"));
            
            if(rs.getInt("usetype_typeid") != 0){
                pi.setUseType(getPropertyUseType(rs.getInt("usetype_typeid")));
            }
            
            si.populateTrackedFields(pi, rs, false);
           
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error generating Property from ResultSet", ex);
        }
        return pi;
    }
    
    /**
     * Creates a new record in the parcel table
     * @param pcl
     * @return
     * @throws IntegrationException 
     */
    public int insertParcel(Parcel pcl) throws IntegrationException{
          
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int freshID = 0;

        try {
            
            String s =  "INSERT INTO public.parcel(\n" +
                        "            parcelkey, parcelidcnty, source_sourceid, createdts, createdby_userid, \n" + // 1-3
                        "            lastupdatedts, lastupdatedby_userid, \n" + // 4
                        "            notes, muni_municode, lotandblock)\n" + // 5-7
                        "    VALUES (DEFAULT, ?, ?, now(), ?, \n" +
                        "            now(), ?, \n" +
                        "            ?, ?, ?);";
            
            stmt = con.prepareStatement(s);
            
            stmt.setString(1, pcl.getCountyParcelID());
            
            if(pcl.getSource() != null){
                stmt.setInt(2, pcl.getSource().getSourceid());
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            
            if(pcl.getCreatedBy() != null){
                stmt.setInt(3, pcl.getCreatedBy().getUserID());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(pcl.getLastUpdatedBy() != null){
                stmt.setInt(4, pcl.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            stmt.setString(5, pcl.getNotes());
            
            if(pcl.getMuni() != null){
                stmt.setInt(6, pcl.getMuni().getMuniCode());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            stmt.setString(7, pcl.getLotAndBlock());
            
            stmt.execute();

            String idNumQuery = "SELECT currval('parcel_parcelkey_seq');";
            stmt = con.prepareStatement(idNumQuery);
            rs = stmt.executeQuery();
            while(rs.next()){
                freshID = rs.getInt("currval");
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert Parcel into DB, sorry!", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return freshID;
        
        
    }
    
       /**
     * Creates a new record in the parcel table
     * @param pcl
     * @throws IntegrationException 
     */
    public void updateParcel(Parcel pcl) throws IntegrationException{
          
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            
            String s =  "UPDATE public.parcel\n" +
                        "   SET parcelidcnty=?, source_sourceid=?, \n" +
                        "       lastupdatedts=now(), lastupdatedby_userid=?, \n" +
                        "       notes=?, muni_municode=? " +
                        " WHERE parcelkey=?;";
            
            stmt = con.prepareStatement(s);
            
            stmt.setString(1, pcl.getCountyParcelID());
            
            if(pcl.getSource() != null){
                stmt.setInt(2, pcl.getSource().getSourceid());
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
         
            if(pcl.getLastUpdatedBy() != null){
                stmt.setInt(3, pcl.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            stmt.setString(4, pcl.getNotes());
            
            if(pcl.getMuni() != null){
                stmt.setInt(5, pcl.getMuni().getMuniCode());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            stmt.setInt(6, pcl.getParcelKey());
            
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to updaet Parcel info in DB, sorry!", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
    }
    
    
    
    
    
    
    /**
     * *************************************************************************
     * ******************** MAILING ADDRESS CENTRAL !!**************************
     * *************************************************************************
     */
    
    /**
     * Extracts a record from the master city/state/zip table
     * @param cszid
     * @return
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */ 
    public MailingCityStateZip getMailingCityStateZip(int cszid) throws IntegrationException, BObStatusException{
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        MailingCityStateZip mcsz = null;

        try {
            
            String s =  "SELECT id, zip_code, sid, state_abbr, city, list_type_id, list_type, \n" +
                        "       default_state, default_city, default_type\n" +
                        "  FROM public.mailingcitystatezip WHERE id=?;";
            
            stmt = con.prepareStatement(s);
            stmt.setInt(1, cszid);

            rs = stmt.executeQuery();

            while (rs.next()) {
                mcsz = generateMailingCityStateZip(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getMailingAddress", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return mcsz;
        
    }
    
    /**
     * Injects fields on a city/state/zip object
     * @param rs
     * @return
     * @throws BObStatusException 
     */
    private MailingCityStateZip generateMailingCityStateZip(ResultSet rs) throws BObStatusException, SQLException{
        if(rs == null){
            throw new BObStatusException("Cannot generate city/state/zip with null RS!");
        }
        
        MailingCityStateZip mcsz = new MailingCityStateZip();
        mcsz.setCityStateZipID(rs.getInt("id"));
        mcsz.setCity(rs.getString("city"));
        mcsz.setState(rs.getString("state_abbr"));
        mcsz.setStateID(rs.getInt("sid"));
        mcsz.setZipCode(rs.getString("zip_code"));
        
// Couldn't get enum matching to work
//        mcsz.setDefaultType(MailingCityStateZipDefaultTypeEnum.valueOf(rs.getString("default_type")));
//        mcsz.setRecordType(MailingCityStateZipRecordTypeEnum.valueOf(rs.getString("list_type")));

        mcsz.setDefaultTypeString(rs.getString("default_type"));
        mcsz.setRecordTypeString(rs.getString("list_type"));
        mcsz.setDefaultCity(rs.getString("default_city"));
        
        return mcsz;
    }
    
    /**
     * Queries the municitystatezip table for official ZIPs
     * @param muni
     * @return a list of CityStateZip IDs
     * @throws IntegrationException
     * @throws BObStatusException 
     */
     public List<Integer> getMailingCityStateZipListByMuni(Municipality muni) throws IntegrationException, BObStatusException{
        if(muni == null){
            throw new BObStatusException("cannot get zips with null muni");
            
        }
         
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Integer> idl = new ArrayList<>();
        try {
            
            String s =  "SELECT muni_municode, citystatezip_id\n" +
                        "  FROM public.municitystatezip WHERE muni_municode=?;";
            
            stmt = con.prepareStatement(s);
            stmt.setInt(1, muni.getMuniCode());

            rs = stmt.executeQuery();

            while (rs.next()) {
                idl.add(rs.getInt("citystatezip_id"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getMailingAddress", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return idl;
        
    }
    
    
    /**
     * Queries the master city state zip table by zip code
     * @param zip the five-digit zip
     * @return a list of matching records that are default or accepted, not not accepted
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public List<MailingCityStateZip> queryMailingCityStateZipByZip(String zip) 
            throws BObStatusException, IntegrationException{
        
        if(zip == null){
            throw new BObStatusException("Cannot query for zip codes with null zip.");
        }
          
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<MailingCityStateZip> mcszl = new ArrayList<>();

        try {
            
            String s =  "SELECT id " +
                        "  FROM public.mailingcitystatezip WHERE zip_code=?;";
            
            stmt = con.prepareStatement(s);
            stmt.setString(1, zip);

            rs = stmt.executeQuery();

            while (rs.next()) {
                mcszl.add(getMailingCityStateZip(rs.getInt("id")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.queryMCSZByZip", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return mcszl; 
    }
     
    /**
     * Extracts a Street record from the DB and creates an injected object
     * @param streetID
     * @return the fully-baked street
     * @throws IntegrationException
     * @throws BObStatusException 
     */
     public MailingStreet getMailingStreet(int streetID) throws IntegrationException, BObStatusException{
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        MailingStreet ms = null;

        try {
            
            String s =  "SELECT streetid, name, namevariantsarr, citystatezip_cszipid, notes, \n" +
                        "       pobox, createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, \n" +
                        "       deactivatedts, deactivatedby_userid\n" +
                        "  FROM public.mailingstreet WHERE streetid=?;";
            
            stmt = con.prepareStatement(s);
            stmt.setInt(1, streetID);

            rs = stmt.executeQuery();

            while (rs.next()) {
                ms = generateMailingStreet(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getMailingAddress", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return ms;
        
    }
     
     /**
      * Internal generator method for creating a Street object from a resultset
      * @param rs
      * @return 
      */
     private MailingStreet generateMailingStreet(ResultSet rs) throws BObStatusException, SQLException, IntegrationException{
         SystemIntegrator si = getSystemIntegrator();
         if(rs == null){
             throw new BObStatusException("Cannot generate a street with null RS");
         }
         
         MailingStreet ms = new MailingStreet();
         ms.setStreetID(rs.getInt("streetid"));
         ms.setName(rs.getString("name"));
         ms.setCityStateZip(getMailingCityStateZip(rs.getInt("citystatezip_cszipid")));
         ms.setNotes(rs.getString("notes"));
         si.populateTrackedFields(ms, rs, true);
         return ms;
     }
    
     
    /**
     * Extracts a record from the mailingaddress table
     * @param addrID record key
     * @return populated Objectified mailingaddress
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public MailingAddress getMailingAddress(int addrID) throws IntegrationException, BObStatusException{
        if(addrID == 0){
            throw new BObStatusException("cannot fetch address of ID 0!");
            
        }
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        MailingAddress ma = null;

        try {
            
            String s =  "SELECT addressid, bldgno, street_streetid, verifiedts, verifiedby_userid, \n" +
                        "       verifiedsource_sourceid, source_sourceid, createdts, createdby_userid, \n" +
                        "       lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, \n" +
                        "       notes\n" +
                        "  FROM public.mailingaddress WHERE addressid=?;";
            
            stmt = con.prepareStatement(s);
            stmt.setInt(1, addrID);

            rs = stmt.executeQuery();

            while (rs.next()) {
                ma = generateMailingAddress(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getMailingAddress", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return ma;
        
    }
    
    /**
     * Internal mailingaddress population method
     * @param rs with all fields SELECTed
     * @return the populated object
     */
    private MailingAddress generateMailingAddress(ResultSet rs) throws SQLException, IntegrationException, BObStatusException{
        SystemIntegrator si = getSystemIntegrator();
        UserCoordinator uc = getUserCoordinator();
        if(rs == null){
            throw new BObStatusException("Cannot generate a mailing address with null RS!");
        }
        
        MailingAddress ma = new MailingAddress();
        
        ma.setAddressID(rs.getInt("addressid"));
        ma.setBuildingNo(rs.getString("bldgno"));
        ma.setStreet(getMailingStreet(rs.getInt("street_streetid")));
        ma.setVerifiedBy(uc.user_getUser(rs.getInt("verifiedby_userid")));
        
        if(rs.getTimestamp("verifiedts") != null){
            ma.setVerifiedTS(rs.getTimestamp("verifiedts").toLocalDateTime());
        }
        
        if(rs.getInt("source_sourceid") != 0){
            ma.setSource(si.getBOBSource(rs.getInt("source_sourceid")));
        }
        
        ma.setNotes(rs.getString("notes"));
        si.populateTrackedFields(ma, rs, true);
        
        return ma;
        
    }
    
   
 
      /**
     * Extracts all mailing addresses associated with a given human
     
     * @param madHolder
     * @return the list of address objects
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public MailingAddressLink getMailingAddressLink(IFace_addressListHolder madHolder, int linkID) 
            throws IntegrationException, BObStatusException{
        
        if(madHolder == null || madHolder.getLinkedObjectSchemaEnum() == null){
            throw new BObStatusException("Cannot get addresses by interface with null holder or its schema enum");
        }
        
        MailingAddressLink madLink = null;
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT source_sourceid, createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, \n");
            sb.append("deactivatedts, deactivatedby_userid, notes, linkid, linkedobjectrole_lorid, priority, \n");
            sb.append(madHolder.getLinkedObjectSchemaEnum().getLINKED_OBJECT_FK_FIELD());
            sb.append(", ");
            sb.append(madHolder.getLinkedObjectSchemaEnum().getTargetTableFKField());
            sb.append(" FROM ");
            sb.append(madHolder.getLinkedObjectSchemaEnum().getLinkingTableName());
            sb.append(" WHERE ");
            sb.append(madHolder.getLinkedObjectSchemaEnum().getLinkingTablePKField());
            sb.append("=?;");
            
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, linkID);

            rs = stmt.executeQuery();

            while (rs.next()) {
                madLink = generateMailingAddressLink(rs, madHolder);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator ...", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return madLink;
    }
    
    
    
      /**
     * Extracts all mailing addresses associated with a given human
     
     * @param madHolder
     * @return the list of address objects
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public List<MailingAddressLink> getMailingAddressLinks(IFace_addressListHolder madHolder) 
            throws IntegrationException, BObStatusException{
        
        if(madHolder == null || madHolder.getLinkedObjectSchemaEnum() == null){
            throw new BObStatusException("Cannot get addresses by interface with null holder or its schema enum");
        }
        
        List<MailingAddressLink> madLinkList = new ArrayList<>();
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT source_sourceid, createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, \n");
            sb.append("deactivatedts, deactivatedby_userid, notes, linkid, linkedobjectrole_lorid, priority, \n");
            sb.append(madHolder.getLinkedObjectSchemaEnum().getLINKED_OBJECT_FK_FIELD());
            sb.append(", ");
            sb.append(madHolder.getLinkedObjectSchemaEnum().getTargetTableFKField());
            sb.append(" FROM ");
            sb.append(madHolder.getLinkedObjectSchemaEnum().getLinkingTableName());
            sb.append(" WHERE ");
            sb.append(madHolder.getLinkedObjectSchemaEnum().getTargetTableFKField());
            sb.append("=? AND deactivatedts IS NULL;");
            
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, madHolder.getTargetObjectPK());

            rs = stmt.executeQuery();

            while (rs.next()) {
                madLinkList.add(generateMailingAddressLink(rs, madHolder));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator ...", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return madLinkList;
    }
    
    
    /**
     * Writes a new record in a table that links a mailing address to a target
     * @param alh
     * @param madLink
     * @return
     * @throws BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public int linkMailingAddress(IFace_addressListHolder alh, MailingAddressLink madLink) throws BObStatusException, IntegrationException{
        if(alh == null 
                || madLink == null 
                || madLink.getLinkedObjectRole() == null 
                || madLink.getLinkedObjectRole().getSchema() == null){
            throw new BObStatusException("Cannot link a mailing address with null link, or role, or schema enum"); 
        }        
          
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int freshID = 0;

        try {
            
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO public.");
            sb.append(madLink.getLinkedObjectRole().getSchema().getLinkingTableName());
            sb.append(" (");
            sb.append(madLink.getLinkedObjectRole().getSchema().getTargetTableFKField());
            sb.append(",");
            sb.append(madLink.getLinkedObjectRole().getSchema().getLINKED_OBJECT_FK_FIELD());
            sb.append(", source_sourceid, createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, " );
            sb.append("            deactivatedts, deactivatedby_userid, notes, linkid, linkedobjectrole_lorid," );
            sb.append("            priority)" );
            sb.append("    VALUES (?, ?, ?," );
            sb.append("            now(), ?, now(), ?," );
            sb.append("            NULL, NULL, ?, DEFAULT, ?," );
            sb.append("            ?);");
            stmt = con.prepareStatement(sb.toString());
            System.out.println("PropertyIntegrator.writeLink: " + sb.toString());
            stmt.setInt(1, alh.getTargetObjectPK());
            stmt.setInt(2, madLink.getAddressID());
            
            if(madLink.getSource() != null){
                stmt.setInt(3, madLink.getSource().getSourceid());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(madLink.getLinkCreatedByUserID() != 0){
                stmt.setInt(4, madLink.getLinkCreatedByUserID());
            }else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            
            if(madLink.getLinkLastUpdatedByUserID() != 0){
                stmt.setInt(5, madLink.getLinkLastUpdatedByUserID());
            }else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            stmt.setString(6, madLink.getNotes());
            
            if(madLink.getLinkedObjectRole() != null){
                stmt.setInt(7, madLink.getLinkedObjectRole().getRoleID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            stmt.setInt(8, madLink.getPriority());
                       
            
            stmt.execute();
            
            sb = new StringBuilder("SELECT currval('");
            sb.append(madLink.getLinkedObjectRole().getSchema().getLinkingTableSequenceID());
            sb.append("');");
            stmt = con.prepareStatement(sb.toString());
            rs = stmt.executeQuery();
            

            while (rs.next()) {
                freshID = rs.getInt("currval");
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.linkAddress: Unable to write mailing link!", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return freshID;
        
    }
    
    
    /**
     * Updates a small subset of fields on a mailing address link
     * @param madLink
     * @throws BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void updateMailingAddressLink(MailingAddressLink madLink) throws BObStatusException, IntegrationException{
        if(madLink == null || madLink.getLinkID() == 0){
            throw new BObStatusException("Cannot update a link with null link or ID = 0");
        }
        
        
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE public.");
        sb.append(madLink.getLinkedObjectRoleSchemaEnum().getLinkingTableName());
        sb.append(" SET source_sourceid=?, lastupdatedts=now(), lastupdatedby_userid=?, deactivatedts=?, deactivatedby_userid=?,");
        sb.append("notes=?, linkedobjectrole_lorid=?, priority=? ");
        sb.append(" WHERE ");
        sb.append(madLink.getLinkedObjectRole().getSchema().getLinkingTablePKField());
        sb.append("=?;");

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(sb.toString());
               
            if(madLink.getSource() != null){
                stmt.setInt(1, madLink.getSource().getSourceid());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            
            if(madLink.getLinkLastUpdatedByUserID() != 0){
                stmt.setInt(2, madLink.getLinkLastUpdatedByUserID());
            }else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            if(madLink.getDeactivatedTS() != null){
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(madLink.getDeactivatedTS()));
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(madLink.getLinkDeactivatedByUserID() != 0){
                stmt.setInt(4, madLink.getLinkDeactivatedByUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            
            stmt.setString(5, madLink.getNotes());
            
            if(madLink.getLinkedObjectRole() != null){
                stmt.setInt(6, madLink.getLinkedObjectRole().getRoleID());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            stmt.setInt(7, madLink.getPriority());
            
            stmt.setInt(8, madLink.getLinkID());
            
            stmt.executeUpdate();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error updating address link", ex);
            
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        
        
    }
    
    
    
    
   
    
     /**
     * Extracts all mailing addresses associated with a given street
     * @param street
     * @return the list of address objects
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public List<MailingAddress> getMailingAddressListByStreet(MailingStreet street) 
            throws IntegrationException, BObStatusException{
        
        if(street == null || street.getStreetID() == 0){
            throw new BObStatusException("Cannot query addresses by street with null street or streetid = 0");
        }
        PropertyCoordinator pc = getPropertyCoordinator();
        
        List<MailingAddress> mal = new ArrayList<>();
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String s =  "SELECT addressid FROM public.mailingaddress WHERE street_streetid=? AND deactivatedts IS NULL;";
            
            stmt = con.prepareStatement(s);
            stmt.setInt(1, street.getStreetID());

            rs = stmt.executeQuery();

            while (rs.next()) {
                mal.add(pc.getMailingAddress(rs.getInt("addressid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getMailingAddressListByStreet: Just kidding! I Cannot fetch mailing by street.", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return mal;
    }
    
    
      /**
     * Populator of links to mailing addresses
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
    private MailingAddressLink generateMailingAddressLink(ResultSet rs, IFace_addressListHolder adlh) 
            throws SQLException, IntegrationException, BObStatusException{
        SystemIntegrator si = getSystemIntegrator();
        PropertyCoordinator pc = getPropertyCoordinator();
        
        MailingAddress ma = pc.getMailingAddress(rs.getInt(adlh.getLinkedObjectSchemaEnum().getLINKED_OBJECT_FK_FIELD()));
        MailingAddressLink madLink = new MailingAddressLink(ma);
        madLink.setLinkID(rs.getInt("linkid"));
        
        madLink.setLinkRole(si.getLinkedObjectRole(rs.getInt("linkedobjectrole_lorid")));
        madLink.setTargetObjectPK(adlh.getTargetObjectPK());
        // populate nonstandard fields:
        if(rs.getInt("source_sourceid") != 0){
            madLink.setSource(si.getBOBSource(rs.getInt("source_sourceid")));
        }
        // populate standard fields with common method in SI
        si.populateTrackedLinkFields(madLink, rs);
        
        return madLink;
        
    }
    
   
    
    
    /**
     * Creates a new record in the mailingstreet table
     * @param ms to insert, not null with ID = 0
     * @return the fresh ID
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public int insertMailingStreet(MailingStreet ms) throws BObStatusException, IntegrationException{
        
        if(ms == null || ms.getStreetID() != 0 || ms.getCityStateZip() == null || ms.getCityStateZip().getCityStateZipID() == 0){
            throw new BObStatusException("Cannot insert street with null street or ID != 0, or zip of null or id = 0");
        }
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int freshID = 0;

        try {
            
            String s =  "INSERT INTO public.mailingstreet(\n" +
                        "            streetid, name, namevariantsarr, citystatezip_cszipid, notes, \n" +
                        "            pobox, createdts, createdby_userid, lastupdatedts, lastupdatedby_userid) \n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
                        "            ?, now(), ?, now(), ?);";
            
            stmt = con.prepareStatement(s);
            
            stmt.setString(1, ms.getName());
            // don't do anything with name variants yet
            stmt.setNull(2, java.sql.Types.NULL);
            stmt.setInt(3, ms.getCityStateZip().getCityStateZipID());
            stmt.setString(4, ms.getNotes());
            
            stmt.setBoolean(5, ms.isPoBox());
            if(ms.getCreatedBy() != null){
                stmt.setInt(6, ms.getCreatedBy().getUserID());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            if(ms.getLastUpdatedBy() != null){
                stmt.setInt(7, ms.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }

            stmt.execute();
            
            
            String idNumQuery = "SELECT currval('mailingstreet_streetid_seq');";
            PreparedStatement st = con.prepareStatement(idNumQuery);
            rs = st.executeQuery();
            

            while (rs.next()) {
                freshID = rs.getInt("currval");
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.insertMailingStreet", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return freshID;
    }
    
    
    /**
     * Updates a record in the mailingstreet table
     * @param ms must be not null and ID != 0
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void updateMailingStreet(MailingStreet ms) throws BObStatusException, IntegrationException{
          if(ms == null || ms.getStreetID() == 0 || ms.getCityStateZip() == null || ms.getCityStateZip().getCityStateZipID() == 0){
            throw new BObStatusException("Cannot insert street with null street or ID == 0, or zip of null or id = 0");
        }
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            
            String s =  "UPDATE public.mailingstreet\n" +
                        "   SET name=?, namevariantsarr=?, citystatezip_cszipid=?, \n" +
                        "       notes=?, pobox=?, lastupdatedts=now(), \n" +
                        "       lastupdatedby_userid=?, deactivatedts=?, deactivatedby_userid=?\n" +
                        " WHERE streetid=?;";
            
            
            stmt = con.prepareStatement(s);
            
            stmt.setString(1, ms.getName());
            // don't do anything with name variants yet
            stmt.setNull(2, java.sql.Types.NULL);
            stmt.setInt(3, ms.getCityStateZip().getCityStateZipID());
            stmt.setString(4, ms.getNotes());
            
            stmt.setBoolean(5, ms.isPoBox());
           
            if(ms.getLastUpdatedBy() != null){
                stmt.setInt(6, ms.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            if(ms.getDeactivatedBy() != null){
                stmt.setTimestamp(7, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            if(ms.getDeactivatedBy() != null){
                stmt.setInt(8, ms.getDeactivatedBy().getUserID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            
            stmt.setInt(9, ms.getStreetID());
            
            stmt.executeUpdate();
            
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.updateMailingStreet", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
    /**
     * Extracts streets by a Street String and a mailing zip
     * If both are null, you'll get all streets.
     * 
     * @param street returns all streets in zip if null
     * @param csz returns matching streets across all zips if null
     * @return matching Streets
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public List<MailingStreet> searchForStreetsByNameAndZip(String street, MailingCityStateZip csz) 
            throws IntegrationException, BObStatusException{
        
        List<MailingStreet> streetList = new ArrayList<>();
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT streetid FROM mailingstreet ");
            sb.append(" WHERE streetid IS NOT NULL AND deactivatedts IS NULL ");
            
            if(street != null){
                sb.append("AND name ILIKE ? ");
            }
            
            if(csz != null){
                sb.append(" AND citystatezip_cszipid = ?");
            }
            sb.append(";");
            
            stmt = con.prepareStatement(sb.toString());
            int paramCounter = 0;
            
            if(street != null){
                StringBuilder strB = new StringBuilder();
                strB.append("%");
                strB.append(street);
                strB.append("%");
                stmt.setString(++paramCounter, street);
            }
            
            if(csz != null){
                stmt.setInt(++paramCounter, csz.getCityStateZipID());
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                streetList.add(getMailingStreet(rs.getInt("streetid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator ...", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return streetList;
    }
    
    
      /**
     * 
     * Primary entry point for queries against the mailingcitystatezip
     * table. 
     * 
     * @param params
     * @return List MailingCityState records
     * @throws IntegrationException 
     */
    public List<Integer> searchForMailingCityStateZip(SearchParamsMailingCityStateZip params) throws IntegrationException {
        
        List<Integer> msszl = new ArrayList<>();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        params.appendSQL("SELECT mailingcitystatezip.id FROM public.mailingcitystatezip ");
        
        params.appendSQL("WHERE id IS NOT NULL ");
        
        // **********************************
        // **    FILTER COM-4 OBJECT ID     **
        // ***********************************
         if (!params.isBobID_ctl()) {
            //**************************************
           // **   FILTER CSZ-1   ZIP            **
           // **************************************
            if (params.isZip_ctl()) {
                params.appendSQL("AND zip_code ILIKE ? ");
            }
            //***************************************
           // **   FILTER CSZ-2:  STATE **
           // ***************************************
            if (params.isState_ctl()) {
                params.appendSQL("AND state_abbr ILIKE ? ");
            }

            //***************************************
           // **   FILTER CSZ-3:  CITY **
           // ***************************************
            if (params.isCity_ctl()) {
                params.appendSQL("AND city ILIKE ? ");
            }
            //***************************************
           // **   FILTER CSZ-4:  RECORD TYPE **
           // ***************************************
            if (params.isRecordType_ctl()) {
                params.appendSQL("AND list_type=? ");
            }
            //***************************************
           // **   FILTER CSZ-5:  DEFAULT TYPE **
           // ***************************************
            if (params.isDefaultType_ctl()) {
                params.appendSQL("AND default_type=?");
            }
            //***************************************
           // **   FILTER CSZ-6:  DEFAULT CITY**
           // ***************************************
            if (params.isDefaultCity_ctl()) {
                params.appendSQL("AND default_city ILIKE ?");
            }
        // ****************************
        // ** COM-4  OBJECT ID       **
        // **************************** 
        } else {
            params.appendSQL("AND id=? "); // will be param 2 with ID search
        }
        params.appendSQL(";");
        int paramCounter = 0;
        StringBuilder str = null;

        try {
            stmt = con.prepareStatement(params.extractRawSQL());

            if (!params.isBobID_ctl()) {
                
                if (params.isZip_ctl()) {
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getZip_val());
                    str.append("%");
                     stmt.setString(++paramCounter, str.toString());
                }
                
                if (params.isState_ctl()) {
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getState_val());
                    str.append("%");
                    stmt.setString(++paramCounter, str.toString());
                }
                
                if (params.isCity_ctl()) {
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getCity_val());
                    str.append("%");
                    stmt.setString(++paramCounter, str.toString());
                }
                
                if (params.isRecordType_ctl()) {
                     stmt.setString(++paramCounter, params.getRecordType_val().getTypeDBString());
                }
                
                if (params.isDefaultType_ctl()) {
                     stmt.setString(++paramCounter, params.getDefaultType_val().getTypeDBString());
                }
                
                if (params.isDefaultCity_ctl()) {
                  str = new StringBuilder();
                  str.append("%");
                  str.append(params.getDefaultCity_val());
                  str.append("%");
                  stmt.setString(++paramCounter, str.toString());
                }
            } else {
                stmt.setInt(++paramCounter, params.getBobID_val());
            }
            
            params.appendToParamLog("City State Zip query before execution: ");
            params.appendToParamLog(stmt.toString());
            System.out.println("PropertyIntegrator.searchForMailingCityStateZip | SQL " + params.extractRawSQL());
            rs = stmt.executeQuery();
            
            int counter = 0;
            int maxResults;
            if (params.isLimitResultCount_ctl()) {
                maxResults = 100;
            } else {
                maxResults = Integer.MAX_VALUE;
            }
            while (rs.next() && counter < maxResults) {
                msszl.add(rs.getInt("id"));
                counter++;
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot search for city/state/zip, sorry!", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return msszl;
    }

    
    
    
//    ***********************************************
//    ************** END OF MAILING!!! ************** 
//    ***********************************************
    
  
    
    /**
     * Hacky utility method for counting properties by municode
     * @param muniCode
     * @return 0 or the count of properties in a given municipality
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public int getPropertyCount(int muniCode) throws IntegrationException{
        int cnt = 0;
        if(muniCode != 0){

            String sql = "select count(parcelkey) from parcel where muni_municode = ?;";

            Connection con = getPostgresCon();
            ResultSet rs = null;
            PreparedStatement stmt = null;

            try {
                stmt = con.prepareStatement(sql);
                stmt.setInt(1, muniCode);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    cnt = rs.getInt("count");
                }
            } catch (SQLException ex) {
                System.out.println(ex.toString());
                throw new IntegrationException("Unable to count properties by municode", ex);
            } finally {
                 if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
                 if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
                 if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
            } // close finally
        }

        return cnt;
    }
    
    
  
    /**
     * Updates a record in the mailingaddress table; I can also deactivate records
     * @param addr with fields as they are to be udpated
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void updateMailingAddress(MailingAddress addr) throws BObStatusException, IntegrationException{
        if(addr == null || addr.getStreet() == null || addr.getStreet().getStreetID() == 0 || addr.getStreet().getCityStateZip() == null){
            throw new BObStatusException("Cannot update a null adress, or street, or street ID = 0, or CSZ null");
        }
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ContactEmail em = null;

        try {
            
            String s =  "UPDATE public.mailingaddress\n" +
                        "   SET bldgno=?, street_streetid=?, verifiedts=?, verifiedby_userid=?, \n" +
                        "       verifiedsource_sourceid=?, source_sourceid=?, createdby_userid=?, \n" +
                        "       lastupdatedts=now(), lastupdatedby_userid=?, \n" +
                        "       notes=?, deactivatedts=?, deactivatedby_userid=? " +
                        "       WHERE addressid=?;";
            
            stmt = con.prepareStatement(s);
            
            stmt.setString(1, addr.getBuildingNo());
            
            if(addr.getStreet() != null){
                stmt.setInt(2, addr.getStreet().getStreetID());
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            
            if(addr.getVerifiedTS() != null){
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(addr.getVerifiedTS()));
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(addr.getVerifiedBy() != null){
                stmt.setInt(4, addr.getVerifiedBy().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            if(addr.getVerifiedSource() != null){
                stmt.setInt(5, addr.getVerifiedSource().getSourceid());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            if(addr.getSource() != null){
                stmt.setInt(6, addr.getSource().getSourceid());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            if(addr.getCreatedBy() != null){
                stmt.setInt(7, addr.getCreatedBy().getUserID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            if(addr.getLastUpdatedBy() != null){
                stmt.setInt(8, addr.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            
            stmt.setString(9, addr.getNotes());
            
            if(addr.getDeactivatedTS() != null){
                stmt.setTimestamp(10, java.sql.Timestamp.valueOf(addr.getDeactivatedTS()));
            } else {
                stmt.setNull(10, java.sql.Types.NULL);
            }
            
            if(addr.getDeactivatedBy() != null){
                stmt.setInt(11, addr.getDeactivatedBy().getUserID());
            } else {
                stmt.setNull(11, java.sql.Types.NULL);
            }
            stmt.setInt(12, addr.getAddressID());

            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator ...", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Creates a new record in the mailingaddress table
     * @param addr
     * @return the ID of the freshly inserted address
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public int insertMailingAddress(MailingAddress addr) throws IntegrationException, BObStatusException{
        
        if(addr == null || addr.getStreet() == null || addr.getStreet().getStreetID() == 0 || addr.getStreet().getCityStateZip() == null){
            throw new BObStatusException("Cannot update a null adress, or street, or street ID = 0, or CSZ null");
        }
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int freshID = 0;

        try {
            
            String s =  "INSERT INTO public.mailingaddress(\n" +
                        "            addressid, bldgno, street_streetid, verifiedts, verifiedby_userid, \n" +
                        "            verifiedsource_sourceid, source_sourceid, createdts, createdby_userid, \n" +
                        "            lastupdatedts, lastupdatedby_userid, \n" +
                        "            notes)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
                        "            ?, ?, now(), ?, \n" +
                        "            now(), ?,  \n" +
                        "            ?);";
            
            stmt = con.prepareStatement(s);
            
           stmt.setString(1, addr.getBuildingNo());
            
            if(addr.getStreet() != null){
                stmt.setInt(2, addr.getStreet().getStreetID());
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            
            if(addr.getVerifiedTS() != null){
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(addr.getVerifiedTS()));
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(addr.getVerifiedBy() != null){
                stmt.setInt(4, addr.getVerifiedBy().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            if(addr.getVerifiedSource() != null){
                stmt.setInt(5, addr.getVerifiedSource().getSourceid());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            if(addr.getSource() != null){
                stmt.setInt(6, addr.getSource().getSourceid());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            if(addr.getCreatedBy() != null){
                stmt.setInt(7, addr.getCreatedBy().getUserID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            if(addr.getLastUpdatedBy() != null){
                stmt.setInt(8, addr.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            
            stmt.setString(9, addr.getNotes());
            
            stmt.execute();

            String idNumQuery = "SELECT currval('mailingaddress_addressid_seq');";
            PreparedStatement st = con.prepareStatement(idNumQuery);
            rs = st.executeQuery();
            while(rs.next()){
                freshID = rs.getInt("currval");
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator ...", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return freshID;
        
    }
    
     /**
      * 
     * Returns a full table dump of PropertyUseType entries
     * @deprecated  replaced by parcel system
     * @param p the property containing the new note value. Client must append note properly
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void updatePropertyNoteFieldOnly(Property p) throws IntegrationException{
        String query = "UPDATE public.parcel SET notes=? WHERE parcelkey=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, p.getNotes());
            stmt.setInt(2, p.getParcelKey());
            
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
     * @param parcel     
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public void deactivateParcel(Parcel parcel) throws IntegrationException, BObStatusException{
      String query =  "UPDATE public.parcel\n" +
                        "   SET deactivatedts=now(), deactivatedby=? WHERE parcelkey=?;";

        if (parcel == null || parcel.getDeactivatedBy() == null){
            throw new BObStatusException("cannot deac parcel with null parcel input or null deac by");
        }
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, parcel.getDeactivatedBy().getUserID());
            stmt.setInt(2, parcel.getParcelKey());
            
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.deactivatedParcel | could not deac parcel", ex);
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

        params.appendSQL("SELECT DISTINCT parcel.parcelkey ");
        params.appendSQL("FROM parcel LEFT OUTER JOIN parcelinfo  ");
        params.appendSQL("ON (parcel.parcelkey = parcelinfo.parcel_parcelkey) ");
        params.appendSQL("LEFT OUTER JOIN parcelmailingaddress  ");
        params.appendSQL("ON (parcel.parcelkey = parcelmailingaddress.parcel_parcelkey) ");
        params.appendSQL("LEFT OUTER JOIN mailingaddress ");
        params.appendSQL("ON (parcelmailingaddress.mailingaddress_addressid = mailingaddress.addressid) ");
        params.appendSQL("LEFT OUTER JOIN mailingstreet ");
        params.appendSQL("ON (mailingaddress.street_streetid = mailingstreet.streetid) ");
        params.appendSQL("WHERE parcel.deactivatedts IS NULL ");
        params.appendSQL("AND parcelinfo.deactivatedts IS NULL ");
        params.appendSQL("AND parcelmailingaddress.deactivatedts IS NULL ");
        params.appendSQL("AND mailingaddress.deactivatedts IS NULL ");
        params.appendSQL("AND mailingstreet.deactivatedts IS NULL ");
		
        
        // **********************************
        // **    FILTER COM-4 OBJECT ID     **
        // ***********************************
         if (!params.isBobID_ctl()) {
           
            //******************************************************************
           // **   FILTERS COM-1, COM-2, COM-3, COM-6 MUNI,DATES,USER,ACTIVE  **
           // ******************************************************************
             params = (SearchParamsProperty) sc.assembleBObSearchSQL_muniDatesUserActive(params, 
                                                                        SearchParamsProperty.MUNI_DBFIELD,
                                                                        PARCEL_ACTIVE_FIELD);

            //***************************************
           // **   FILTER PROP-1:  LOT AND BLOCK   **
           // ***************************************
            if (params.isLotblock_ctl()) {
                params.appendSQL("AND lotandblock ILIKE ? ");
            }

            //*****************************************
           // **   FILTER PROP-2: Parcel BOB SOURCE         **
           // *****************************************
            if (params.isBobSource_ctl()) {
                if(params.getBobSource_val() != null){
                    params.appendSQL("AND parcel.source_sourceid=? ");
                } else {
                    params.setBobSource_ctl(false);
                    params.appendToParamLog("SOURCE: no BOb source object; source filter disabled");
                }
            }

            //****************************
           // **   3:PARCEL ID          **
           // ****************************
            if (params.isParcelid_ctl()) {
                params.appendSQL("AND parcel.parcelidcnty ILIKE ? ");
            }

           // *****************************
           // **       4: ADDRESS-BLDG        **
           // *****************************
            if (params.isAddress_bldgNum_ctl()) {
                params.appendSQL("AND mailingaddress.bldgno ILIKE ? ");
            }

            // *****************************
           // **       5: ADDRESS-STREET **
           // *****************************
            if (params.isAddressStreetName_ctl()) {
                params.appendSQL("AND mailingstreet.name ILIKE ? ");
            }

           // ****************************
           // **       6:CONDITION      **
           // ****************************
            if (params.isCondition_ctl()) {
                if(params.getCondition_intensityClass_val() != null){
                    params.appendSQL("AND parcelinfo.condition_intensityclassid=? ");
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
                    params.appendSQL("AND parcelinfo.landbankprospect_intensityclassid=? ");
                } else {
                    params.setLandbankprospect_ctl(false);
                    params.appendToParamLog("LANDBANKPROSPECT: No intensity object; land bank prospect disabled; |");
                }
            }

           // ****************************
           // ** 8:LAND BANK HELD  **
           // ****************************
            if (params.isLandbankheld_ctl()) {
                params.appendSQL("AND parcelinfo.landbankheld= ");
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
                params.appendSQL("AND parcelinfo.nonaddressable= ");
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
                    params.appendSQL("AND parcelinfo.usetype_typeid=? ");
                } else {
                    params.setUseType_ctl(false);
                    params.appendToParamLog("USE TYPE: No intensity object found; use type filter disabled; | " );
                }
            }

          
           
        // ****************************
        // ** COM-4  OBJECT ID       **
        // **************************** 
        } else {
            params.appendSQL("AND parcel.parcelkey=? "); // will be param 2 with ID search
        }
        params.appendSQL(";");
        int paramCounter = 0;
        StringBuilder str = null;

        try {
            stmt = con.prepareStatement(params.extractRawSQL());

            if (!params.isBobID_ctl()) {
                // common field
                if (params.isMuni_ctl()) {
                     stmt.setInt(++paramCounter, params.getMuni_val().getMuniCode());
                }
                
                // common field
                if(params.isDate_startEnd_ctl()){
                    stmt.setTimestamp(++paramCounter, params.getDateStart_val_sql());
                    stmt.setTimestamp(++paramCounter, params.getDateEnd_val_sql());
                 }
                
                // common field
                if (params.isUser_ctl()) {
                   stmt.setInt(++paramCounter, params.getUser_val().getUserID());
                }
                
                // prop param #1: LOB
                                
                if (params.isLotblock_ctl()) {
                    str = new StringBuilder();
                    str.append("%");
                    if(params.getLotblock_val_num1() != null){
                        str.append(params.getLotblock_val_num1());
                        str.append("%");
                    }
                    if(params.getLotblock_val_letter()!= null){
                        str.append(params.getLotblock_val_letter());
                        str.append("%");
                    }
                    if(params.getLotblock_val_num2() != null){
                        str.append(params.getLotblock_val_num2());
                        str.append("%");
                    }
                    stmt.setString(++paramCounter, str.toString());
                }
                // prop param #2: bobsource
                if (params.isBobSource_ctl()) {
                    stmt.setInt(++paramCounter, params.getBobSource_val().getSourceid());
                }
                // prop param #3: parcelid
                if (params.isParcelid_ctl()) {
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getParcelid_val());
                    str.append("%");
                    stmt.setString(++paramCounter, str.toString());
                    
                }
                // prop param #4: bldg number
                if (params.isAddress_bldgNum_ctl()) {
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getAddress_bldgNum_val());
                    str.append("%");
                    stmt.setString(++paramCounter, str.toString());
                }
                
                // prop param #5: street name
                if (params.isAddressStreetName_ctl()) {
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getAddressStreetName_val());
                    str.append("%");
                    stmt.setString(++paramCounter, str.toString());
                }
                // prop param #6: condition
                if (params.isCondition_ctl()) {
                    stmt.setInt(++paramCounter, params.getCondition_intensityClass_val().getClassID());
                }
                // prop param #7: land bank prospect
                if (params.isLandbankprospect_ctl()) {
                    stmt.setInt(++paramCounter, params.getLandbankprospect_intensityClass_val().getClassID());
                }
                
                // conditions 8- land bank held is boolean
                // conditions 9- nonaddressable is boolean
                
                // prop param #10: street name
                if (params.isUseType_ctl()) {
                    stmt.setInt(++paramCounter, params.getUseType_val().getTypeID());
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
                propIDList.add(rs.getInt("parcelkey"));
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
    
    
    /**
     * Getter for property unit objects
     * @param propUnitID
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
     public PropertyUnit getPropertyUnit(int propUnitID) throws IntegrationException, BObStatusException {
        PropertyUnit pu = null;
        String query =  "SELECT unitid, unitnumber, parcel_parcelkey, rentalintentdatestart, \n" +
                        "       rentalintentdatestop, rentalnotes, condition_intensityclassid, \n" +
                        "       source_sourceid, createdts, createdby_userid, lastupdatedts, \n" +
                        "       lastupdatedby_userid, deactivatedts, deactivatedby_userid, notes, \n" +
                        "       location_occlocationdescriptor, address_parcelmailingid\n" +
                        "  FROM public.parcelunit WHERE unitid=?;";

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

     
     
        /**
         * Generator method for property unit objects
         * @param rs
         * @return
         * @throws SQLException
         * @throws IntegrationException
         * @throws BObStatusException 
         * 
         */
      private PropertyUnit generatePropertyUnit(ResultSet rs) throws SQLException, IntegrationException, BObStatusException {
        PropertyUnit pu = new PropertyUnit();
        SystemCoordinator sc = getSystemCoordinator();
        SystemIntegrator si = getSystemIntegrator();
        PropertyCoordinator pc = getPropertyCoordinator();
        OccupancyCoordinator oc = getOccupancyCoordinator();
        
        pu.setUnitID(rs.getInt("unitid"));
        pu.setUnitNumber(rs.getString("unitnumber"));
        pu.setParcelKey(rs.getInt("parcel_parcelkey"));
        
        if(rs.getTimestamp("rentalintentdatestart") != null){
            pu.setRentalIntentDateStart(LocalDate.from(rs.getTimestamp("rentalintentdatestart").toLocalDateTime()));
        }
        if(rs.getTimestamp("rentalintentdatestop") != null){
            pu.setRentalIntentDateStop(LocalDate.from(rs.getTimestamp("rentalintentdatestop").toLocalDateTime()));
        }
        pu.setRentalNotes(rs.getString("rentalnotes"));
        
        if(rs.getInt("condition_intensityclassid") != 0){
            pu.setConditionIntensityClassID(rs.getInt("condition_intensityclassid"));
        }
        if(rs.getInt("source_sourceid") != 0){
            pu.setSource(sc.getBObSource(rs.getInt("source_sourceid")));
        }
        pu.setNotes(rs.getString("notes"));
        
        if(rs.getInt("address_parcelmailingid") != 0){
            pu.setParcelMailing(pc.getMailingAddress(rs.getInt("address_parcelmailingid")));
        }
        
        if(rs.getInt("location_occlocationdescriptor") != 0){
            pu.setLocationDescriptor(oc.getOccLocationDescriptor(rs.getInt("location_occlocationdescriptor")));
        }
        
        si.populateTrackedFields(pu, rs, false);
        
        return pu;
    }
    
    
      
    
    /**
     * Pathway for inserting a new property unit
     * @param pu
     * @return
     * @throws IntegrationException 
     */
    public int insertPropertyUnit(PropertyUnit pu) throws IntegrationException {
        String query =  "INSERT INTO public.parcelunit(\n" +
                        "            unitid, unitnumber, parcel_parcelkey, rentalintentdatestart, \n" +
                        "            rentalintentdatestop, rentalnotes, condition_intensityclassid, \n" +
                        "            source_sourceid, createdts, createdby_userid, lastupdatedts, \n" +
                        "            lastupdatedby_userid, deactivatedts, deactivatedby_userid, notes, \n" +
                        "            location_occlocationdescriptor, address_parcelmailingid)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?, now(), ?, now(), \n" +
                        "            ?, NULL, NULL, ?, \n" +
                        "            ?, ?);";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        int lastID = 0;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, pu.getUnitNumber());
            stmt.setInt(2, pu.getParcelKey());
    
            if(pu.getRentalIntentDateStart() != null){
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(pu.getRentalIntentDateStart().atStartOfDay()));
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            if(pu.getRentalIntentDateStop() != null){
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(pu.getRentalIntentDateStop().atStartOfDay()));
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }

            stmt.setString(5, pu.getRentalNotes());

            if(pu.getConditionIntensityClassID() != 0){
                stmt.setInt(6, pu.getConditionIntensityClassID());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            if(pu.getSource() != null){
                stmt.setInt(7, pu.getSource().getSourceid());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            if(pu.getCreatedBy() != null){
                stmt.setInt(8, pu.getCreatedBy().getUserID());
            }else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
                   
            
            if(pu.getLastUpdatedBy() != null){
                stmt.setInt(9, pu.getLastUpdatedBy().getUserID());
            }else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            
            stmt.setString(10, pu.getNotes());
            
            if(pu.getLocationDescriptor() != null){
                stmt.setInt(11, pu.getLocationDescriptor().getLocationID());
            } else {
                stmt.setNull(11, java.sql.Types.NULL);
            }
                   
            if(pu.getParcelMailing() != null){
                stmt.setInt(12, pu.getParcelMailing().getAddressID());
            } else {
                stmt.setNull(12, java.sql.Types.NULL);
            }
            
            stmt.execute();
            
            // grab the newly inserted propertyid
            String idNumQuery = "SELECT currval('parcelunit_unitid_seq');";
            stmt = con.prepareStatement(idNumQuery);
            ResultSet rs;
            rs = stmt.executeQuery();
            while(rs.next()){
                lastID = rs.getInt(1);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.propertyUnit | Unable to retrieve parcelunit", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return lastID;
    }
    
    /**
     * Pathway for updating a property unit 
     * @param pu
     * @throws IntegrationException 
     */
     public void updatePropertyUnit(PropertyUnit pu) throws IntegrationException {
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        String sql =  "UPDATE public.parcelunit\n" +
                        "   SET unitnumber=?, parcel_parcelkey=?, rentalintentdatestart=?, \n" +
                        "       rentalintentdatestop=?, rentalnotes=?, condition_intensityclassid=?, \n" +
                        "       source_sourceid=?, lastupdatedts=?, \n" +
                        "       lastupdatedby_userid=?, deactivatedts=?, deactivatedby_userid=?, \n" +
                        "       notes=?, location_occlocationdescriptor=?, address_parcelmailingid=?\n" +
                        " WHERE unitid=?;";

        try {
            stmt = con.prepareStatement(sql);
              stmt.setString(1, pu.getUnitNumber());
            stmt.setInt(2, pu.getParcelKey());
    
            if(pu.getRentalIntentDateStart() != null){
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(pu.getRentalIntentDateStart().atStartOfDay()));
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            if(pu.getRentalIntentDateStop() != null){
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(pu.getRentalIntentDateStop().atStartOfDay()));
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }

            stmt.setString(5, pu.getRentalNotes());

            if(pu.getConditionIntensityClassID() != 0){
                stmt.setInt(6, pu.getConditionIntensityClassID());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            if(pu.getSource() != null){
                stmt.setInt(7, pu.getSource().getSourceid());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            if(pu.getCreatedBy() != null){
                stmt.setInt(8, pu.getCreatedBy().getUserID());
            }else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
                   
            
            if(pu.getLastUpdatedBy() != null){
                stmt.setInt(9, pu.getLastUpdatedBy().getUserID());
            }else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            
            if(pu.getDeactivatedTS() != null){
                stmt.setTimestamp(10, java.sql.Timestamp.valueOf(pu.getDeactivatedTS()));
            }else {
                stmt.setNull(10, java.sql.Types.NULL);
            }
            
            if(pu.getDeactivatedBy() != null){
                stmt.setInt(11, pu.getDeactivatedBy().getUserID());
            }else {
                stmt.setNull(11, java.sql.Types.NULL);
            }
            
            stmt.setString(12, pu.getNotes());
            
            if(pu.getLocationDescriptor() != null){
                stmt.setInt(13, pu.getLocationDescriptor().getLocationID());
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }
                   
            if(pu.getParcelMailing() != null){
                stmt.setInt(14, pu.getParcelMailing().getAddressID());
            } else {
                stmt.setNull(14, java.sql.Types.NULL);
            }
            stmt.setInt(15, pu.getUnitID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update property unit", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }

     /**
      * Getter for parcel units
      * @param p
      * @return
      * @throws IntegrationException
      * @throws BObStatusException 
      */
    public List<Integer> getPropertyUnitList(Property p) throws IntegrationException, BObStatusException {
        List<Integer> uidl = new ArrayList();

        String query = "SELECT unitid FROM parcelunit WHERE parcel_parcelkey=? AND deactivatedts IS NULL;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, p.getParcelKey());
            rs = stmt.executeQuery();
            while (rs.next()) {
                uidl.add(rs.getInt("unitid"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build property unit list due to an DB integration error", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return uidl;
    }

  
    
    /**
     * Adaptor method for calling getPropertyUnitDataHeavy(int unitID) given a PropertyUnit object
     * 
     * @param pu
     * @return a PropertyUnit containing a list of OccPeriods, and more in the future
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.EventException 
     * @throws com.tcvcog.tcvce.domain.AuthorizationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public PropertyUnitDataHeavy getPropertyDataHeavy(PropertyUnit pu) throws IntegrationException, EventException, AuthorizationException, BObStatusException{
        PropertyUnitDataHeavy puwl = null;
        try {
            puwl = getPropertyUnitDataHeavy(pu.getUnitID());
        } catch (ViolationException ex) {
            System.out.println(ex);
        }
        return puwl;
    }
    
    /**
     * Getter for property unit with occ periods inside of its belly, along 
     * with persons and blobs
     * @param unitID
     * @return
     * @throws IntegrationException
     * @throws EventException
     * @throws AuthorizationException
     * @throws BObStatusException
     * @throws ViolationException 
     */
    public PropertyUnitDataHeavy getPropertyUnitDataHeavy(int unitID) throws IntegrationException, EventException, AuthorizationException, BObStatusException, ViolationException{
        OccupancyIntegrator oi = getOccupancyIntegrator();
        PersonCoordinator pc = getPersonCoordinator();
        
        PropertyUnitDataHeavy pudh = new PropertyUnitDataHeavy(getPropertyUnit(unitID));
        pudh.setPeriodList(oi.getOccPeriodList(unitID));
        pudh.setHumanLinkList(pc.assembleLinkedHumanLinks(pudh));
        
        
        // Disabled for parcelization and simplification of unit editing process
//        puwl.setChangeOrderList(getPropertyUnitChangeListAll(unitID));
        return pudh;
    }
    
    /**
     * Handy utility method for grabbing a PropertyUnit with its own embedded Property
     * for use in Inspection stuff and reports.
     * 
     * @param unitID
     * @return
     * @throws IntegrationException 
     */
    public PropertyUnitWithProp getPropertyUnitWithProp(int unitID) throws IntegrationException, BObStatusException{
        PropertyCoordinator pc = getPropertyCoordinator();
        
        PropertyUnitWithProp puwp = new PropertyUnitWithProp(getPropertyUnit(unitID));
        puwp.setProperty(pc.getProperty(puwp.getParcelKey()));
        
        return puwp;
    }
    
    public void insertPropertyUnitChange(PropertyUnitChangeOrder uc) throws IntegrationException {
        String query = "INSERT INTO public.propertyunitchange(\n"
                + "            unitchangeid, unitnumber, parcelunit_unitid, otherknownaddress, notes, \n"
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

    public PropertyUnitChangeOrder getPropertyUnitChange(int unitChangeId) throws IntegrationException, BObStatusException {
        PropertyUnitChangeOrder uc = new PropertyUnitChangeOrder();
        String query = "SELECT unitchangeid, parcelunit_unitid,\n"
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

    public PropertyUnitChangeOrder generatePropertyUnitChange(ResultSet rs) throws SQLException, IntegrationException, BObStatusException {
        UserIntegrator ui = getUserIntegrator();
        PropertyUnitChangeOrder uc = new PropertyUnitChangeOrder();
        uc.setUnitChangeID(rs.getInt("unitchangeid"));
        uc.setUnitID(rs.getInt("parcelunit_unitid"));
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
                + "SET unitnumber=?, parcelunit_unitid=?, otherknownaddress=?, notes=?, rentalnotes=?,\n"
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

    public List<PropertyUnitChangeOrder> getPropertyUnitChangeList(int propertyUnitID) throws IntegrationException, BObStatusException {
        List<PropertyUnitChangeOrder> ucl = new ArrayList<>();
        String query = "SELECT\n"
                + "unitchangeid, parcelunit_unitid,\n"
                + "propertyunitchange.unitnumber, propertyunitchange.otherknownaddress,\n"
                + "propertyunitchange.notes, propertyunitchange.rentalnotes,\n"
                + "removed, added, entryts, approvedondate, changedby_personid, approvedby_userid, propertyunitchange.active\n"
                + "FROM\n"
                + "propertyunitchange\n"
                + "JOIN propertyunit ON propertyunitchange.parcelunit_unitid = propertyunit.unitid\n"
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

    public List<PropertyUnitChangeOrder> getPropertyUnitChangeListAll(int propertyUnitID) throws IntegrationException, BObStatusException {
        List<PropertyUnitChangeOrder> ucl = new ArrayList<>();
        String query = "SELECT\n"
                + "unitchangeid, parcelunit_unitid,\n"
                + "propertyunitchange.unitnumber, propertyunitchange.otherknownaddress,\n"
                + "propertyunitchange.notes, propertyunitchange.rentalnotes,\n"
                + "removed, added, entryts, approvedondate, changedby_personid, approvedby_userid, propertyunitchange.active\n"
                + "FROM\n"
                + "propertyunitchange\n"
                + "JOIN propertyunit ON propertyunitchange.parcelunit_unitid = propertyunit.unitid\n"
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
     * Returns a full table dump of PropertyUseType entries\
     * @deprecated replaced by parcelinfo
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
