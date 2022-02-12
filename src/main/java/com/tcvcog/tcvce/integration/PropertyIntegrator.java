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
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.ContactEmail;
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.HumanMailingAddressLink;
import com.tcvcog.tcvce.entities.HumanParcelLink;
import com.tcvcog.tcvce.entities.MailingAddress;
import com.tcvcog.tcvce.entities.MailingCityStateZip;
import com.tcvcog.tcvce.entities.MailingCityStateZipDefaultTypeEnum;
import com.tcvcog.tcvce.entities.MailingCityStateZipRecordTypeEnum;
import com.tcvcog.tcvce.entities.MailingStreet;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Parcel;
import com.tcvcog.tcvce.entities.ParcelInfo;
import com.tcvcog.tcvce.entities.ParcelMailingAddressLink;
import com.tcvcog.tcvce.entities.PropertyExtData;
import com.tcvcog.tcvce.entities.PropertyUseType;
import com.tcvcog.tcvce.entities.TaxStatus;
import com.tcvcog.tcvce.entities.search.SearchParamsMailingCityStateZip;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inserts, retrieves, updates, and deactivates property-related fields and tables
 * in the Database
 * @author Ellen Bascomb (Apartment 31Y)
 */
public class PropertyIntegrator extends BackingBeanUtils implements Serializable {

    final int MAX_RESULTS = 100;
    final String ACTIVE_FIELD = "property.active";
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
        PropertyCoordinator pc = getPropertyCoordinator();
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
                pi.setUnfitDateStart(rs.getTimestamp("unfitdatestart").toLocalDateTime());
            }
            
            if(rs.getTimestamp("unfitdatestop") != null){
                pi.setUnfitDateStop(rs.getTimestamp("unfitdatestop").toLocalDateTime());
            }
            
            if(rs.getInt("unfitby_userid") != 0){
                pi.setUnfitBy(ui.getUser(rs.getInt("unfitby_userid")));
            }
                
            if(rs.getTimestamp("abandoneddatestart") != null){
                pi.setAbandonedDateStart(rs.getTimestamp("abandoneddatestart").toLocalDateTime());
            }
            
            if(rs.getTimestamp("abandoneddatestop") != null){
                pi.setAbandonedDateStop(rs.getTimestamp("abandoneddatestop").toLocalDateTime());
            }
            
            if(rs.getInt("abandonedby_userid") != 0){
                pi.setAbandonedBy(ui.getUser(rs.getInt("abandonedby_userid")));
            }
            
            if(rs.getTimestamp("vacantdatestart") != null){
                pi.setVacantDateStart(rs.getTimestamp("vacantdatestart").toLocalDateTime());
            }
            
            if(rs.getTimestamp("vacantdatestop") != null){
                pi.setVacantDateStop(rs.getTimestamp("vacantdatestop").toLocalDateTime());
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
            pi.setActive(rs.getBoolean("active"));
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
            Statement st = con.createStatement();
            rs = st.executeQuery(idNumQuery);
            rs.next();
            freshID = rs.getInt("currval");

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
                        "       notes=?, muni_municode=?, \n" +
                        "       lotandblock=?\n" +
                        " WHERE parcelkey=?, ;";
            
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
            stmt.setString(6, pcl.getLotAndBlock());
            stmt.setInt(7, pcl.getParcelKey());
            
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert Parcel into DB, sorry!", ex);
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
     */
    public MailingAddress getMailingAddress(int addrID) throws IntegrationException, BObStatusException{
        
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
     * Extracts all mailing addresses associated with a given parcel
     * @param parcelID
     * @return the list of addresses
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public List<ParcelMailingAddressLink> getMailingAddressListByParcel(int parcelID) 
            throws IntegrationException, BObStatusException{
        
        List<ParcelMailingAddressLink> pmall = new ArrayList<>();
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        

        try {
            
            String s =  "SELECT mailingparcel_parcelid, mailingparcel_mailingid, source_sourceid, \n" +
                        "       createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, \n" +
                        "       deactivatedts, deactivatedby_userid, notes\n" +
                        "  FROM public.parcelmailingaddress " +
                        "  WHERE mailingparcel_parcelid = ?";
            
            stmt = con.prepareStatement(s);
            stmt.setInt(1, parcelID);

            rs = stmt.executeQuery();

            while (rs.next()) {
                // note that rs.next() is called and the cursor
                // is advanced to the first row in the rs
                pmall.add(generateParcelMailingAddressLink(rs));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Property Integrator ...", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return pmall;
    }
    
    /**
     * Populator of link object between a parcel and a mailing address
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
    private ParcelMailingAddressLink generateParcelMailingAddressLink(ResultSet rs) 
            throws SQLException, IntegrationException, BObStatusException{
        SystemIntegrator si = getSystemIntegrator();
        
        MailingAddress ma = getMailingAddress(rs.getInt("mailingparcel_mailingid"));
        ParcelMailingAddressLink pmal = new ParcelMailingAddressLink(ma);
        
        // populate nonstandard fields:
        if(rs.getInt("source_sourceid") != 0){
            pmal.setSource(si.getBOBSource(rs.getInt("source_sourceid")));
        }
        // populate standard fields with common method in SI
        si.populateTrackedLinkFields(pmal, rs);
        
        return pmal;
        
    }
    
    /**
     * Extracts all mailing addresses associated with a given human
     * @param humanID
     * @return the list of address objects
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public List<HumanMailingAddressLink> getMailingAddressListByHuman(int humanID) 
            throws IntegrationException, BObStatusException{
        
        List<HumanMailingAddressLink> hmal = new ArrayList<>();
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            
            String s =  "SELECT humanmailing_humanid, humanmailing_addressid, source_sourceid, \n" +
                        "       createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, \n" +
                        "       deactivatedts, deactivatedby_userid, notes, linkid, linkedobjectrole_lorid" +
                        "  FROM public.humanmailingaddress WHERE humanmailing_humanid=?";
            
            stmt = con.prepareStatement(s);
            stmt.setInt(1, humanID);

            rs = stmt.executeQuery();

            while (rs.next()) {
                hmal.add(generateHumanMailingAddressLink(rs));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator ...", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return hmal;
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
                mal.add(getMailingAddress(rs.getInt("addressid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator ...", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return mal;
    }
    
    
    
    /**
     * Populator of link object between a human and a mailing address
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
    private HumanMailingAddressLink generateHumanMailingAddressLink(ResultSet rs) 
            throws SQLException, IntegrationException, BObStatusException{
        SystemIntegrator si = getSystemIntegrator();
       
        MailingAddress ma = getMailingAddress(rs.getInt("humanmailing_addressid"));
        HumanMailingAddressLink hmal = new HumanMailingAddressLink(ma);
        hmal.setLinkID(rs.getInt("linkid"));
        
        hmal.setLinkRole(si.getLinkedObjectRole(rs.getInt("linkedobjectrole_lorid")));
        
        // populate nonstandard fields:
        if(rs.getInt("source_sourceid") != 0){
            hmal.setSource(si.getBOBSource(rs.getInt("source_sourceid")));
        }
        // populate standard fields with common method in SI
        si.populateTrackedLinkFields(hmal, rs);
        
        return hmal;
        
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
                        "            pobox, createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, \n" +
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
            Statement st = con.createStatement();
            rs = st.executeQuery(idNumQuery);
            rs.next();

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
          if(ms == null || ms.getStreetID() != 0 || ms.getCityStateZip() == null || ms.getCityStateZip().getCityStateZipID() == 0){
            throw new BObStatusException("Cannot insert street with null street or ID != 0, or zip of null or id = 0");
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

            String sql = "select count(propertyid) from property where municipality_municode = ?;";

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
                        "       verifiedsource_sourceid=?, source_sourceid=?, createdby=?, \n" +
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
            Statement st = con.createStatement();
            rs = st.executeQuery(idNumQuery);
            rs.next();
            freshID = rs.getInt("currval");

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
     * Hope to deprecate in deference to the new search system? ECD on 19JUL (2020?)
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
// TODO: Humanization fix
//                propList.add(generateProperty(rs));
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
      * 
     * Returns a full table dump of PropertyUseType entries
     * @deprecated  replaced by parcel system
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
     * @deprecated  replaced by parcel system,
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
                    stmt.setInt(++paramCounter, params.getPerson_val().getHumanID());
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
    
    
     public PropertyUnit getPropertyUnit(int propUnitID) throws IntegrationException, BObStatusException {
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

     
     

      private PropertyUnit generatePropertyUnit(ResultSet rs) throws SQLException, IntegrationException, BObStatusException {
        PropertyUnit pu = new PropertyUnit();
        UserIntegrator ui = new UserIntegrator();
        
        pu.setUnitID(rs.getInt("unitid"));
        pu.setUnitNumber(rs.getString("unitnumber"));
        pu.setParcelKey(rs.getInt("property_propertyid"));
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
            stmt.setInt(2, pu.getParcelKey());
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
            stmt.setInt(2, pu.getParcelKey());
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

    public List<PropertyUnit> getPropertyUnitList(Property p) throws IntegrationException, BObStatusException {
        List<PropertyUnit> unitList = new ArrayList();

        String query = "SELECT unitid FROM propertyunit WHERE property_propertyid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, p.getParcelKey());
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
    public PropertyUnitWithProp getPropertyUnitWithProp(int unitID) throws IntegrationException, BObStatusException{
        PropertyCoordinator pc = getPropertyCoordinator();
        
        PropertyUnitWithProp puwp = new PropertyUnitWithProp(getPropertyUnit(unitID));
        puwp.setProperty(pc.getProperty(puwp.getParcelKey()));
        
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

    public PropertyUnitChangeOrder getPropertyUnitChange(int unitChangeId) throws IntegrationException, BObStatusException {
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

    public PropertyUnitChangeOrder generatePropertyUnitChange(ResultSet rs) throws SQLException, IntegrationException, BObStatusException {
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

    public List<PropertyUnitChangeOrder> getPropertyUnitChangeList(int propertyUnitID) throws IntegrationException, BObStatusException {
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

    public List<PropertyUnitChangeOrder> getPropertyUnitChangeListAll(int propertyUnitID) throws IntegrationException, BObStatusException {
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
