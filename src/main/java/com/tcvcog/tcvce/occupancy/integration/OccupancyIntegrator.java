/*
 * Copyright (C) Technology Rediscovery LLC
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
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.PaymentCoordinator;
import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.Human;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.HumanLink;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PublicInfoBundleOccPermitApplication;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import com.tcvcog.tcvce.entities.occupancy.OccPermit;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplication;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplicationReason;
import com.tcvcog.tcvce.entities.occupancy.OccPermitType;
import com.tcvcog.tcvce.entities.occupancy.OccAppPersonRequirement;
import com.tcvcog.tcvce.entities.occupancy.OccApplicationStatusEnum;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.search.SearchParamsOccPeriod;
import com.tcvcog.tcvce.entities.search.SearchParamsOccPermit;
import java.io.Serializable;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Integration methods that return populated business objects with all their
 composite elements all nicely situated on shelves and in Lists

 High-level object families produced here include: OccPeriod OccPermitType
 OccPermit OccPermitApplication
 *
 * @author ellen bascomb of apt 31y
 */
public class OccupancyIntegrator extends BackingBeanUtils implements Serializable {

    
    final String ACTIVE_FIELD_OCCPERIOD = "occperiod.deactivatedts";
    final String ACTIVE_FIELD_OCCPERMIT = "occpermit.deactivatedts";
    
    /**
     * Creates a new instance of OccupancyIntegrator
     */
    public OccupancyIntegrator() {
    }

    
   
    
    /**
     * Extracts all active occ periods from the DB using a unitID
     * @param unitID
     * @return
     * @throws IntegrationException
     * @throws EventException
     * @throws AuthorizationException
     * @throws BObStatusException
     * @throws ViolationException 
     */
    public List<Integer> getOccPeriodIDListByUnitID(int unitID) throws IntegrationException, EventException, AuthorizationException, BObStatusException, ViolationException {
        List<Integer> opIDList = new ArrayList<>();
        String query = "SELECT periodid FROM public.occperiod WHERE parcelunit_unitid=? AND deactivatedts IS NULL;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, unitID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                opIDList.add(rs.getInt("periodid"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build occ period", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return opIDList;
    }

    /**
     * Primary entry point for searches against the occ permit table
     * @param spop
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
  public List<Integer> searchForOccPermits(SearchParamsOccPermit spop) throws IntegrationException{
        
        
        if(spop == null){
            throw new IntegrationException("cannot search with null params");
            
        }
        List<Integer> permitIDList = new ArrayList<>();
        
        SearchCoordinator sc = getSearchCoordinator();
        
        spop.appendSQL("SELECT permitid FROM occpermit\n");
        spop.appendSQL("INNER JOIN occperiod ON (occperiod.periodid = occpermit.occperiod_periodid)\n");
        spop.appendSQL("INNER JOIN parcelunit ON (occperiod.parcelunit_unitid = parcelunit.unitid)\n");
        spop.appendSQL("INNER JOIN parcel ON (parcelunit.parcel_parcelkey = parcel.parcelkey)\n" );
        spop.appendSQL("INNER JOIN municipality ON (parcel.muni_municode = municipality.municode)\n" );
        spop.appendSQL("WHERE permitid IS NOT NULL ");                        
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            if (!spop.isBobID_ctl()) {
           
                spop = (SearchParamsOccPermit) sc.assembleBObSearchSQL_muniDatesUserActive(spop, 
                                                            SearchParamsOccPermit.MUNI_DBFIELD,
                                                            ACTIVE_FIELD_OCCPERMIT);
                if(spop.isDraft_ctl()){
                    if(spop.isDraft_val()){
                        spop.appendSQL("AND finalizedts IS NULL ");
                    } else {
                        spop.appendSQL("AND finalizedts IS NOT NULL ");
                    }
                }
            } else {
                spop.appendSQL("AND permitid=?");
            }
            
            
            spop.appendSQL(";");
            
            
            
            stmt = con.prepareStatement(spop.extractRawSQL());
            
            int paramCounter = 0;

            if (!spop.isBobID_ctl()) {
                if (spop.isMuni_ctl()) {
                     stmt.setInt(++paramCounter, spop.getMuni_val().getMuniCode());
                }
                
                if(spop.isDate_startEnd_ctl()){
                    stmt.setTimestamp(++paramCounter, spop.getDateStart_val_sql());
                    stmt.setTimestamp(++paramCounter, spop.getDateEnd_val_sql());
                 }
                
                if (spop.isUser_ctl()) {
                   stmt.setInt(++paramCounter, spop.getUser_val().getUserID());
                }
                
            } else {
                stmt.setInt(++paramCounter, spop.getBobID_val());
            }

            rs = stmt.executeQuery();

            int counter = 0;
            int maxResults;
            if (spop.isLimitResultCount_ctl()) {
                maxResults = spop.getLimitResultCount_val();
            } else {
                maxResults = Integer.MAX_VALUE;
            }
            while (rs.next() && counter < maxResults) {
                permitIDList.add(rs.getInt("permitid"));
                counter++;
            }
            
           
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to search for occ permits", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return permitIDList;
      
  }

    /**
     * Single point of entry for queries against the OccPeriod table
     * @param params
     * @return 
     */
    public List<Integer> searchForOccPeriods(SearchParamsOccPeriod params) {
        SearchCoordinator sc = getSearchCoordinator();
        
        List<Integer> periodList = new ArrayList<>();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        params.appendSQL("SELECT DISTINCT occperiod.periodid ");
        params.appendSQL("FROM occperiod \n");
        params.appendSQL("INNER JOIN parcelunit ON (occperiod.parcelunit_unitid = parcelunit.unitid) \n");
        params.appendSQL("INNER JOIN parcel ON (parcelunit.parcel_parcelkey = parcel.parcelkey) \n ");
        params.appendSQL("LEFT OUTER JOIN occinspection ON (occinspection.occperiod_periodid = periodid) \n");
        params.appendSQL("LEFT OUTER JOIN occpermit ON (occpermit.occperiod_periodid = periodid) \n ");
        params.appendSQL("LEFT OUTER JOIN humanoccperiod ON (occperiod.periodid = humanoccperiod.occperiod_periodid) \n");
        params.appendSQL("WHERE occperiod.periodid IS NOT NULL ");

        
        if (!params.isBobID_ctl()) {
           // *******************************
           // **   MUNI,DATES,USER,ACTIVE  **
           // *******************************
            params = (SearchParamsOccPeriod) sc.assembleBObSearchSQL_muniDatesUserActive(params, 
                                                            SearchParamsOccPeriod.MUNI_DBFIELD,
                                                            ACTIVE_FIELD_OCCPERIOD);
            
           // *******************************
            // **        PROPERTY           **
            // *******************************
             if (params.isProperty_ctl()) {
                if(params.getProperty_val()!= null){
                    params.appendSQL("AND parcel_parcelkey=? ");
                } else {
                    params.setProperty_ctl(false);
                    params.appendToParamLog("Parcel: no parcel object; prop filter disabled");
                }
            }
            
            // *******************************
            // **       PROPERTY UNIT       **
            // *******************************
             if (params.isPropertyUnit_ctl()) {
                if(params.getPropertyUnit_val()!= null){
                    params.appendSQL("AND parcelunit_unitid=? ");
                } else {
                    params.setPropertyUnit_ctl(false);
                    params.appendToParamLog("parcel UNIT: no PropertyUnit object; propunit filter disabled");
                }
            }
             
         

            
            // *******************************
            // **       PERMITS             **
            // *******************************
            if (params.isPermitIssuance_ctl()) {
                if (params.isPermitIssuance_val()) {
                    params.appendSQL("occpermit.dateissued IS NOT NULL ");
                } else {
                    params.appendSQL("occpermit.dateissued IS NULL ");
                }
            }

            
            // *******************************
            // **    PASSED INSPECTIONS     **
            // *******************************
            if (params.isInspectionPassed_ctl()) {
                params.appendSQL("AND occinspection.passedinspectionts ");
                if (params.isInspectionPassed_val()) {
                    params.appendSQL("IS NOT NULL ");
                } else {
                    params.appendSQL("IS NULL ");
                }
            }

            // *******************************
            // **    THIRD PARTY            **
            // *******************************
            if (params.isThirdPartyInspector_ctl()) {
                params.appendSQL("AND occinspection.thirdpartyinspector_personid ");
                if (params.isThirdPartyInspector_registered_val()) {
                    params.appendSQL("IS NOT NULL ");
                } else {
                    params.appendSQL("IS NULL ");
                }

                params.appendSQL("AND occinspection.thirdpartyinspector_personid ");
                if (params.isThirdPartyInspector_approved_val()) {
                    params.appendSQL("IS NOT NULL ");
                } else {
                    params.appendSQL("IS NULL ");
                }
            }

           // *******************************
            // **          7:PACC          **
            // *******************************
            if (params.isPacc_ctl()) {
                if(params.isPacc_val()){
                    params.appendSQL("AND paccenabled = TRUE ");
                } else {
                    params.appendSQL("AND paccenabled = FALSE ");
                }
            }
             
            // *******************************
            // **       8:PERSON            **
            // *******************************
            if (params.isPerson_ctl()) {
                if(params.getPerson_val()!= null){
                    params.appendSQL("AND occperiodperson.person_personid=? ");
                } else {
                    params.setPropertyUnit_ctl(false);
                    params.appendToParamLog("PERSON: no Person object found; person filter disabled");
                }
            }
             
             


        } else {
            params.appendSQL("AND occperiod.periodid=? "); // will be param 1 with ID search
        }
        
        params.appendSQL(";");

        int paramCounter = 0;

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
                
                if (params.isProperty_ctl()) {
                    stmt.setInt(++paramCounter, params.getProperty_val().getParcelKey());
                }

                if (params.isPropertyUnit_ctl()) {
                    stmt.setInt(++paramCounter, params.getPropertyUnit_val().getUnitID());
                }
                // filter OCC-8
                if (params.isPerson_ctl()) {
                    stmt.setInt(++paramCounter, params.getPerson_val().getHumanID());
                }

            } else {
                stmt.setInt(++paramCounter, params.getBobID_val());
            }

            rs = stmt.executeQuery();

            int counter = 0;
            int maxResults;
            if (params.isLimitResultCount_ctl()) {
                maxResults = params.getLimitResultCount_val();
            } else {
                maxResults = Integer.MAX_VALUE;
            }
            while (rs.next() && counter < maxResults) {
                periodList.add(rs.getInt("periodid"));
                counter++;
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return periodList;
    }

    /**
     * Primary extraction point for an occupancy period by ID
     * @param periodid
     * @return
     * @throws IntegrationException
     * @throws EventException
     * @throws AuthorizationException
     * @throws BObStatusException
     * @throws ViolationException 
     */
    public OccPeriod getOccPeriod(int periodid) throws IntegrationException, 
                                                                EventException, 
                                                                AuthorizationException, 
                                                                BObStatusException, 
                                                                ViolationException {
        OccPeriod op = null;
        OccupancyCoordinator oc = getOccupancyCoordinator();
        String query = "SELECT periodid, source_sourceid, parcelunit_unitid, createdts, startdate, startdatecertifiedby_userid, \n"
                + "       startdatecertifiedts, enddate, enddatecertifiedby_userid, enddatecterifiedts, \n"
                + "       manager_userid, authorizationts, authorizedby_userid, overrideperiodtypeconfig, \n"
                + "       notes, createdby_userid, lastupdatedby_userid, lastupdatedts, deactivatedts, deactivatedby_userid \n"
                + "  FROM public.occperiod WHERE periodid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, periodid);
            rs = stmt.executeQuery();
            while (rs.next()) {
                op = generateOccPeriod(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build occ period", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return op;
    }

    /**
     * Generator for occupancy period objects
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    private OccPeriod generateOccPeriod(ResultSet rs) throws SQLException, IntegrationException, BObStatusException {
        SystemIntegrator si = getSystemIntegrator();
        UserCoordinator uc = getUserCoordinator();

        OccPeriod op = new OccPeriod();

        op.setPeriodID(rs.getInt("periodid"));

        if(rs.getInt("source_sourceid") != 0){
            op.setSource(si.getBOBSource(rs.getInt("source_sourceid")));
        }
        op.setPropertyUnitID(rs.getInt("parcelunit_unitid"));

        if(rs.getTimestamp("startdate") != null){
            op.setStartDate(rs.getTimestamp("startdate").toLocalDateTime());
        }
            
        op.setStartDateCertifiedBy(uc.user_getUser(rs.getInt("startdatecertifiedby_userid")));
        if(rs.getTimestamp("startdatecertifiedts") != null){
            op.setStartDateCertifiedTS(rs.getTimestamp("startdatecertifiedts").toLocalDateTime());
        }

        if(rs.getTimestamp("enddate") != null){
            op.setEndDate(rs.getTimestamp("enddate").toLocalDateTime());
        }
        op.setEndDateCertifiedBy(uc.user_getUser(rs.getInt("enddatecertifiedby_userid")));
        if(rs.getTimestamp("enddatecterifiedts") != null){
            op.setEndDateCertifiedTS(rs.getTimestamp("enddatecterifiedts").toLocalDateTime());
        }

        op.setManager(uc.user_getUser(rs.getInt("manager_userid")));

        if(rs.getTimestamp("authorizationts") != null){
            op.setAuthorizedTS(rs.getTimestamp("authorizationts").toLocalDateTime());
        }
        op.setAuthorizedBy(uc.user_getUser(rs.getInt("authorizedby_userid")));

        op.setOverrideTypeConfig(rs.getBoolean("overrideperiodtypeconfig"));
        op.setNotes(rs.getString("notes"));

        si.populateTrackedFields(op, rs, false);

        return op;
    }
    
    /**
     * Grabs all occ period IDs from the loginobjecthistory table
     * @param userID
     * @return
     * @throws IntegrationException 
     */
 public List<Integer> getOccPeriodHistoryList(int userID) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Integer> al = new ArrayList<>();

        try {
            String s = "SELECT occperiod_periodid, entrytimestamp FROM loginobjecthistory "
                    + "WHERE login_userid = ? "
                    + "AND occperiod_periodid IS NOT NULL "
                    + "ORDER BY entrytimestamp DESC;";
            stmt = con.prepareStatement(s);
            stmt.setInt(1, userID);

            rs = stmt.executeQuery();
            while (rs.next()) {
                al.add(rs.getInt("occperiod_periodid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.getPerson | Unable to retrieve person", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        System.out.println("OccIntegrator Retrieved period of size: " + al.size());
        return al;

    }  
 
 
 // **********************************************************
 // ******************** OCCUPANCY PERMITS *******************
 // **********************************************************
 

 /**
  * Extracts a single occ period by ID
  * @param permitID
  * @return
  * @throws IntegrationException
  * @throws BObStatusException 
  */
    public OccPermit getOccPermit(int permitID) throws IntegrationException, BObStatusException {
        OccPermit op = null;
        String query = "SELECT permitid, occperiod_periodid, referenceno, staticpermitadditionaltext, \n" +
                        "       notes, finalizedts, finalizedby_userid, statictitle, staticmuniaddress, \n" +
                        "       staticpropertyinfo, staticownerseller, staticcolumnlink, staticbuyertenant, \n" +
                        "       staticproposeduse, staticusecode, staticpropclass, staticdateofapplication, \n" +
                        "       staticinitialinspection, staticreinspectiondate, staticfinalinspection, \n" +
                        "       staticdateofissue, staticofficername, staticissuedundercodesourceid, \n" +
                        "       staticstipulations, staticcomments, staticmanager, statictenants, \n" +
                        "       staticleaseterm, staticleasestatus, staticpaymentstatus, staticnotice, \n" +
                        "       createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, \n" +
                        "       deactivatedts, deactivatedby_userid, staticconstructiontype, nullifiedts, " +
                        "       nullifiedby_userid, staticdateexpiry, permittype_typeid, staticsignature_photodocid  \n" +
                        "  FROM public.occpermit WHERE permitid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, permitID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                op = generateOccPermit(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to get occ permit due to integration error", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return op;

    }

    /**
     * Generator for occ permits
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    private OccPermit generateOccPermit(ResultSet rs) throws SQLException, IntegrationException, BObStatusException {
        UserCoordinator uc = getUserCoordinator();
        OccPermit permit = new OccPermit();
        SystemIntegrator si = getSystemIntegrator();

        permit.setPermitID(rs.getInt("permitid"));
        permit.setPeriodID(rs.getInt("occperiod_periodid"));
        permit.setReferenceNo(rs.getString("referenceno"));
        
        permit.setPermitType(getOccPermitType(rs.getInt("permittype_typeid")));

        permit.setPermitAdditionalText(rs.getString("staticpermitadditionaltext"));
        permit.setNotes(rs.getString("notes"));

        if(rs.getTimestamp("finalizedts") != null){
            permit.setFinalizedts(rs.getTimestamp("finalizedts").toLocalDateTime());
        }
        permit.setFinalizedBy(uc.user_getUser(rs.getInt("finalizedby_userid")));
       
        permit.setStatictitle(rs.getString("statictitle"));
        permit.setStaticmuniaddress(rs.getString("staticmuniaddress"));
        permit.setStaticpropertyinfo(rs.getString("staticpropertyinfo"));
        permit.setStaticownerseller(rs.getString("staticownerseller"));
        
        permit.setStaticcolumnlink(rs.getString("staticcolumnlink"));
        permit.setStaticbuyertenant(rs.getString("staticbuyertenant"));
        permit.setStaticproposeduse(rs.getString("staticproposeduse"));
        permit.setStaticusecode(rs.getString("staticusecode"));
        permit.setStaticpropclass(rs.getString("staticpropclass"));
        if(rs.getTimestamp("staticdateofapplication") != null){
            permit.setStaticdateofapplication(rs.getTimestamp("staticdateofapplication").toLocalDateTime());
        }
        if(rs.getTimestamp("staticinitialinspection") != null){
            permit.setStaticinitialinspection(rs.getTimestamp("staticinitialinspection").toLocalDateTime());
        }
        if(rs.getTimestamp("staticreinspectiondate") != null){
            permit.setStaticreinspectiondate(rs.getTimestamp("staticreinspectiondate").toLocalDateTime());
        }
        if(rs.getTimestamp("staticfinalinspection") != null){
            permit.setStaticfinalinspection(rs.getTimestamp("staticfinalinspection").toLocalDateTime());
        }
        if(rs.getTimestamp("staticdateofissue") != null){
            permit.setStaticdateofissue(rs.getTimestamp("staticdateofissue").toLocalDateTime());
        }
        if(rs.getTimestamp("staticdateexpiry") != null){
            permit.setStaticdateofexpiry(rs.getTimestamp("staticdateexpiry").toLocalDateTime());
        }
        
        permit.setStaticofficername(rs.getString("staticofficername"));
        permit.setStaticissuedundercodesourceid(rs.getString("staticissuedundercodesourceid"));
        permit.setStaticstipulations(rs.getString("staticstipulations"));
        permit.setStaticcomments(rs.getString("staticcomments"));
        permit.setStaticmanager(rs.getString("staticmanager"));
        
        permit.setStatictenants(rs.getString("statictenants"));
        permit.setStaticleaseterm(rs.getString("staticleaseterm"));
        permit.setStaticleasestatus(rs.getString("staticleasestatus"));
        permit.setStaticpaymentstatus(rs.getString("staticpaymentstatus"));
        permit.setStaticnotice(rs.getString("staticnotice"));
        permit.setStaticconstructiontype(rs.getString("staticconstructiontype"));
        
        if(rs.getTimestamp("nullifiedts") != null){
            permit.setNullifiedTS(rs.getTimestamp("nullifiedts").toLocalDateTime());
        }
        if(rs.getInt("nullifiedby_userid") != 0){
            permit.setNullifiedBy(uc.user_getUser(rs.getInt("nullifiedby_userid")));
        }
        
        permit.setStaticOfficerSignaturePhotoDocID(rs.getInt("staticsignature_photodocid"));
        
        si.populateTrackedFields(permit, rs, true);
        
        return permit;
    }

    /**
     * Extracts permits by occ period 
     * @param period
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public List<Integer> getOccPermitIDList(OccPeriod period) throws IntegrationException, BObStatusException {
        List<Integer> permitIDList = new ArrayList<>();
        String query = "SELECT permitid FROM public.occpermit WHERE occperiod_periodid=? AND deactivatedts IS NULL;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, period.getPeriodID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                permitIDList.add(rs.getInt("permitid"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build occ permit ID list", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return permitIDList;
    }

    /**
     * Entry point for OccPermit objects into the database. 
     * Only called by coordinator. BUT remember, 
     *  you can only write to the metadata fields here. Actual permit static fields
     *  are written specially through the occPermitPopulateStaticFieldsFromDynamicFields
     * @param permit
     * @return ID of the skeleton. 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public int insertOccPermit(OccPermit permit) throws BObStatusException, IntegrationException{
        if(permit == null){
            throw new BObStatusException("Cannot insert null occ permit");
        }
        
        String query = "INSERT INTO public.occpermit(\n" +
                         "            permitid, occperiod_periodid, referenceno,  notes, createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, permittype_typeid)\n" +
                         "    VALUES (DEFAULT, ?, NULL, ?, now(), ?, now(), ?, ?);"; 
        ResultSet rs = null;
        Connection con = null;
        PreparedStatement stmt = null;
        int freshPermitID = 0;

        PaymentCoordinator pc = getPaymentCoordinator();
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, permit.getPeriodID());
            stmt.setString(2, permit.getNotes());
            if(permit.getCreatedBy() != null){
                stmt.setInt(3, permit.getCreatedBy().getUserID());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            // on creation, the last updator is the creator
            if(permit.getCreatedBy() != null){
                stmt.setInt(4, permit.getCreatedBy().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            if(permit.getPermitType() != null){
                stmt.setInt(5, permit.getPermitType().getTypeID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            stmt.execute();

            String lastIDNumSQL = "SELECT currval('occupancypermit_permitid_seq'::regclass)";

            stmt = con.prepareStatement(lastIDNumSQL);

            rs = stmt.executeQuery();

            while (rs.next()) {
                freshPermitID = rs.getInt("currval");
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("OccupancyIntegrator.insertOccPermit"
                    + "| IntegrationError: unable to insert occupancy permit", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        return freshPermitID;
    }
    
    
    /**
     * Asks DB for the count of finalized permits by muni
     * @param muni
     * @return 
     */
    public int countFinalizedPermitsByMuni(Municipality muni) throws BObStatusException, IntegrationException{
        if(muni == null){
            throw new BObStatusException("Cannot get permit count by muni with null muni");
        }
        
        String query = "SELECT count(permitid) AS pcount\n" +
                        "FROM occpermit INNER JOIN occperiod ON (occperiod.periodid = occpermit.occperiod_periodid)\n" +
                        "INNER JOIN parcelunit ON (occperiod.parcelunit_unitid = parcelunit.unitid)\n" +
                        "INNER JOIN parcel ON (parcelunit.parcel_parcelkey = parcel.parcelkey)\n" +
                        "WHERE parcel.muni_municode=? AND finalizedts IS NOT NULL;"; 
        ResultSet rs = null;
        Connection con = null;
        PreparedStatement stmt = null;
        int finalizedPermitCount = 0;

        PaymentCoordinator pc = getPaymentCoordinator();
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, muni.getMuniCode());
            
            rs = stmt.executeQuery();

            while (rs.next()) {
                finalizedPermitCount = rs.getInt("pcount");
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("OccupancyIntegrator.countFinalizePermitsByMuni");
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        return finalizedPermitCount;
        
        
        
        
    }
    
    /**
     * Updates an occ permit's metadata, including notes and deactivationts and decativatedby_userid
     * @param permit
     * @throws IntegrationException 
     */
    public void updateOccPermit(OccPermit permit) throws IntegrationException {
        String query = "UPDATE public.occpermit\n" +
                        "   SET referenceno=?,  \n" +
                        "       notes=?, finalizedts=?, finalizedby_userid=?, lastupdatedts=now(), \n" +
                        "       lastupdatedby_userid=?, deactivatedts=?, deactivatedby_userid=?, occperiod_periodid=?, permittype_typeid=?\n" +
                        " WHERE permitid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            if(permit.getReferenceNo() != null){
                stmt.setString(1, permit.getReferenceNo());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            stmt.setString(2, permit.getNotes());
            
            if(permit.getFinalizedts() != null){
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(permit.getFinalizedts()));
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            if(permit.getFinalizedBy() != null){
                stmt.setInt(4, permit.getFinalizedBy().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            if(permit.getLastUpdatedBy() != null){
                stmt.setInt(5, permit.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            if(permit.getDeactivatedTS() != null){
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(permit.getDeactivatedTS()));
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            if(permit.getDeactivatedBy() != null){
                stmt.setInt(7, permit.getDeactivatedBy().getUserID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            stmt.setInt(8, permit.getPeriodID());
            
            if(permit.getPermitType() != null){
                stmt.setInt(9, permit.getPermitType().getTypeID());
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            
            stmt.setInt(10, permit.getPermitID());
            
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("OccupancyIntegrator.updateOccpermit | Unable to update occupancy permit metadata", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
    }
    
     
    /**
     * Nullifed an occ permit
     * @param permit
     * @throws IntegrationException 
     */
    public void nullifyOccupancyPermit(OccPermit permit) throws IntegrationException {
        String query = "UPDATE public.occpermit\n" +
                        "   SET nullifiedts=now(), nullifiedby_userid=?, lastupdatedts=now(), \n" +
                        "       lastupdatedby_userid=? \n" +
                        " WHERE permitid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            if(permit.getNullifiedBy() != null){
                stmt.setInt(1, permit.getNullifiedBy().getUserID());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            
            if(permit.getLastUpdatedBy() != null){
                stmt.setInt(2, permit.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            
            stmt.setInt(3, permit.getPermitID());
            
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("OccupancyIntegrator.updateOccpermit | Unable to update occupancy permit metadata", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
    }
    
    
    /**
     * I update the special permit static fields on an occ permit
     * @param permit     
     * @throws IntegrationException 
     */
    public void updateOccPermitStaticFields(OccPermit permit) throws IntegrationException {
        String query = "UPDATE public.occpermit\n" +
                        "   SET staticpermitadditionaltext=?, statictitle=?, \n" +
                        "       staticmuniaddress=?, staticpropertyinfo=?, staticownerseller=?, \n" +
                        "       staticcolumnlink=?, staticbuyertenant=?, staticproposeduse=?, \n" +
                        "       staticusecode=?, staticpropclass=?, staticdateofapplication=?, \n" +
                        "       staticinitialinspection=?, staticreinspectiondate=?, staticfinalinspection=?, \n" +
                        "       staticdateofissue=?, staticofficername=?, staticissuedundercodesourceid=?, \n" +
                        "       staticstipulations=?, staticcomments=?, staticmanager=?, statictenants=?, \n" +
                        "       staticleaseterm=?, staticleasestatus=?, staticpaymentstatus=?, \n" +
                        "       staticnotice=?, lastupdatedts=now(), \n" +
                        "       lastupdatedby_userid=?, staticconstructiontype=?, staticdateexpiry=?, staticsignature_photodocid=?  " +
                        " WHERE permitid = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setString(1, permit.getPermitAdditionalText());
            stmt.setString(2, permit.getStatictitle());
            
            stmt.setString(3, permit.getStaticmuniaddress());
            stmt.setString(4, permit.getStaticpropertyinfo());
            stmt.setString(5, permit.getStaticownerseller());
            
            stmt.setString(6, permit.getStaticcolumnlink());
            stmt.setString(7, permit.getStaticbuyertenant());
            stmt.setString(8, permit.getStaticproposeduse());
            
            stmt.setString(9, permit.getStaticusecode());
            stmt.setString(10, permit.getStaticpropclass());
            if(permit.getStaticdateofapplication() != null){
                stmt.setTimestamp(11, java.sql.Timestamp.valueOf(permit.getStaticdateofapplication()));
            } else {
                stmt.setNull(11, java.sql.Types.NULL);
            }
            
            if(permit.getStaticinitialinspection() != null){
                stmt.setTimestamp(12, java.sql.Timestamp.valueOf(permit.getStaticinitialinspection()));
            } else {
                stmt.setNull(12, java.sql.Types.NULL);
            }
            if(permit.getStaticreinspectiondate() != null){
                stmt.setTimestamp(13, java.sql.Timestamp.valueOf(permit.getStaticreinspectiondate()));
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }
            if(permit.getStaticfinalinspection() != null){
                stmt.setTimestamp(14, java.sql.Timestamp.valueOf(permit.getStaticfinalinspection()));
            } else {
                stmt.setNull(14, java.sql.Types.NULL);
            }
            
            if(permit.getStaticdateofissue() != null){
                stmt.setTimestamp(15, java.sql.Timestamp.valueOf(permit.getStaticdateofissue()));
            } else {
                stmt.setNull(15, java.sql.Types.NULL);
            }
            stmt.setString(16, permit.getStaticofficername());
            stmt.setString(17, permit.getStaticissuedundercodesourceid());
            
            stmt.setString(18, permit.getStaticstipulations());
            stmt.setString(19, permit.getStaticcomments());
            stmt.setString(20, permit.getStaticmanager());
            stmt.setString(21, permit.getStatictenants());
            
            stmt.setString(22, permit.getStaticleaseterm());
            stmt.setString(23, permit.getStaticleasestatus());
            stmt.setString(24, permit.getStaticpaymentstatus());
            
            stmt.setString(25, permit.getStaticnotice());
            if(permit.getLastUpdatedBy() != null){
                stmt.setInt(26, permit.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(26, java.sql.Types.NULL);
            }
            stmt.setString(27, permit.getStaticconstructiontype());
            if(permit.getStaticdateofexpiry() != null){
                stmt.setTimestamp(28, java.sql.Timestamp.valueOf(permit.getStaticdateofexpiry()));
            } else {
                stmt.setNull(28, java.sql.Types.NULL);
            }
            if(permit.getStaticOfficerSignaturePhotoDocID() != 0){
                stmt.setInt(29, permit.getStaticOfficerSignaturePhotoDocID());
            } else {
                stmt.setNull(29, java.sql.Types.NULL);
            }
            stmt.setInt(30, permit.getPermitID());
            

            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("OccupancyIntegrator.updateOccpermitStaticFields | Unable to update occupancy permit static fields", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
    }
    
   
    
    //**************************************************************
    //**************************** OCC PERIOD TYPE STUFF ***********
    //**************************************************************
    
    
    
    
    
    
    
    /**
     * Updates a record in occ permit type
     * @param permitType
     * @throws IntegrationException 
     */
    public void updateOccPermitType(OccPermitType permitType) throws IntegrationException {
        String query = "UPDATE public.occpermittype\n" +
                        "   SET muni_municode=?, title=?, authorizeduses=?, description=?, \n" +
                        "       userassignable=?, permittable=?, requireinspectionpass=?, requireleaselink=?, \n" +
                        "       active=?, allowthirdpartyinspection=?, commercial=?, defaultpermitvalidityperioddays=?, \n" +
                        "       eventruleset_setid=?, permittitle=?, permittitlesub=?, expires=?, \n" +
                        "       requiremanager=?, requiretenant=?, requirezerobalance=?\n" +
                        " WHERE typeid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, permitType.getMuni().getMuniCode());
            stmt.setString(2, permitType.getTitle());
            stmt.setString(3, permitType.getAuthorizeduses());
            stmt.setString(4, permitType.getDescription());
            stmt.setBoolean(5, permitType.isUserassignable());

            stmt.setBoolean(6, permitType.isPermittable());
            stmt.setBoolean(7, permitType.isPassedInspectionRequired());

            stmt.setBoolean(8, permitType.isRequireLeaseLink());
            stmt.setBoolean(9, permitType.isActive());
            stmt.setBoolean(10, permitType.isAllowthirdpartyinspection());
            
            stmt.setBoolean(11, permitType.isCommercial());

            stmt.setInt(12, permitType.getDefaultValidityPeriodDays());
            if(permitType.getBaseRuleSetID() != 0){
                stmt.setInt(13, permitType.getBaseRuleSetID());
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }
            
            stmt.setString(14, permitType.getPermitTitle());
            stmt.setString(15, permitType.getPermitTitleSub());
            stmt.setBoolean(16, permitType.isExpires());
            stmt.setBoolean(17, permitType.isRequireManager());
            stmt.setBoolean(18, permitType.isRequireTenant());
            stmt.setBoolean(19, permitType.isRequireZeroBalance());
            
            stmt.setInt(20, permitType.getTypeID());
            
            

            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update occupancy permit type", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
    }

    /**
     * Removes an occ period type
     * @param opt
     * @throws IntegrationException 
     */
    public void deleteOccPeriodType(OccPermitType opt) throws IntegrationException {
        String query = "DELETE FROM public.occpermittype\n"
                + " WHERE typeid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
//            stmt.setInt(1, opt.getOccupancyPermitTypeID());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete occupancy permit type--probably because another"
                    + "part of the database has a reference item.", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
    }

    /**
     * Primary extraction point for occ period types
     * @param typeid
     * @return  null if typeid == 0
     * @throws IntegrationException 
     */
    public OccPermitType getOccPermitType(int typeid) throws IntegrationException {
        if(typeid == 0){
            return null;
        }
        OccPermitType tpe = null;
        String query = "SELECT typeid, muni_municode, title, authorizeduses, description, userassignable, \n" +
                        "       permittable, requireinspectionpass, requireleaselink, active, \n" +
                        "       allowthirdpartyinspection, commercial, defaultpermitvalidityperioddays, \n" +
                        "       eventruleset_setid, permittitle, permittitlesub, expires, requiremanager, \n" +
                        "       requiretenant, requirezerobalance\n" +
                        "  FROM public.occpermittype WHERE typeid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, typeid);
            rs = stmt.executeQuery();
            while (rs.next()) {
                tpe = generateOccPermitType(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to occ period type due to an DB integration error", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return tpe;
    }

    /**
     * Extracts occ period types by muniprofile
     * @param muniProfileID
     * @return
     * @throws IntegrationException 
     */
    public List<OccPermitType> getOccPermitTypeList(int muniProfileID) throws IntegrationException {
        List<OccPermitType> typeList = new ArrayList<>();
        String query = "SELECT occperiodtype_typeid\n"
                + "  FROM public.muniprofileoccperiodtype WHERE muniprofile_profileid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, muniProfileID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                typeList.add(getOccPermitType(rs.getInt("occperiodtype_typeid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build occupancy period type list", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return typeList;
    }

    /**
     * Extracts all OccPeriodTypes in DB for configuration
     * @return
     * @throws IntegrationException 
     */
    public List<OccPermitType> getCompleteOccPermitTypeList() throws IntegrationException {
        List<OccPermitType> occPeriodTypeList = new ArrayList<>();
        String query = "SELECT muniprofile_profileid, occperiodtype_typeid\n"
                + "  FROM public.muniprofileoccperiodtype;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                occPeriodTypeList.add(getOccPermitType(rs.getInt("occperiodtype_typeid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get OccupancyPermitType list, sorry", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        return occPeriodTypeList;
    }

    /**
     * Writes a new record in the occperiodtype table
     * @param permitType
     * @throws IntegrationException 
     */
    public void insertOccPermitType(OccPermitType permitType) throws IntegrationException {
        String query = "INSERT INTO public.occpermittype(\n" +
"            typeid, muni_municode, title, authorizeduses, description, userassignable, \n" +
"            permittable, requireinspectionpass, requireleaselink, active, \n" +
"            allowthirdpartyinspection, commercial, defaultpermitvalidityperioddays, \n" +
"            eventruleset_setid, permittitle, permittitlesub, expires, requiremanager, \n" +
"            requiretenant, requirezerobalance)\n" +
"    VALUES (DEFAULT, ?, ?, ?, ?, ?, \n" +
"            ?, ?, ?, ?, \n" +
"            ?, ?, ?, \n" +
"            ?, ?, ?, ?, ?, \n" +
"            ?, ?);";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, permitType.getMuni().getMuniCode());
            stmt.setString(2, permitType.getTitle());
            stmt.setString(3, permitType.getAuthorizeduses());
            stmt.setString(4, permitType.getDescription());
            stmt.setBoolean(5, permitType.isUserassignable());

            stmt.setBoolean(6, permitType.isPermittable());
            stmt.setBoolean(7, permitType.isPassedInspectionRequired());

            stmt.setBoolean(8, permitType.isRequireLeaseLink());
            stmt.setBoolean(9, permitType.isActive());
            stmt.setBoolean(10, permitType.isAllowthirdpartyinspection());
            
            stmt.setBoolean(11, permitType.isCommercial());

            stmt.setInt(12, permitType.getDefaultValidityPeriodDays());
            if(permitType.getBaseRuleSetID() != 0){
                stmt.setInt(13, permitType.getBaseRuleSetID());
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }
            
            stmt.setString(14, permitType.getPermitTitle());
            stmt.setString(15, permitType.getPermitTitleSub());
            stmt.setBoolean(16, permitType.isExpires());
            stmt.setBoolean(17, permitType.isRequireManager());
            stmt.setBoolean(18, permitType.isRequireTenant());
            stmt.setBoolean(19, permitType.isRequireZeroBalance());
            

            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert OccupancyPermitType ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
    }

    /**
     * Insertion point for occupancy period objects
     * @param period
     * @return
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public int insertOccPeriod(OccPeriod period) throws IntegrationException, BObStatusException {
        if(period == null || period.getCreatedBy() == null || period.getPropertyUnitID() == 0){
            throw new BObStatusException("cannot insert occ period with null period, type, or creator, or parcel unit with ID == 0");
        }
        
        String query = " INSERT INTO public.occperiod(\n"
                + "            periodid, source_sourceid, parcelunit_unitid, createdts, \n"
                + "            startdate, enddate, manager_userid, \n"
                + "            notes, createdby_userid, lastupdatedby_userid, lastupdatedts)\n"
                + "    VALUES (DEFAULT, ?, ?, now(),  \n"
                + "            ?, ?, ?,"
                + "            ?, ?, ?, now());";
        ResultSet rs = null;
        Connection con = null;
        PreparedStatement stmt = null;
        int newPeriodId = 0;

        PaymentCoordinator pc = getPaymentCoordinator();
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);

            // PARAMS LINE 1
            if(period.getSource() != null){
                stmt.setInt(1, period.getSource().getSourceid());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            
            stmt.setInt(2, period.getPropertyUnitID());
            
            if (period.getStartDate() != null) {
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(period.getStartDate()));
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }

            if (period.getEndDate() != null) {
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(period.getEndDate()));
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }

            if (period.getManager() != null) {
                stmt.setInt(5, period.getManager().getUserID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            stmt.setString(6, period.getNotes());
            
            if (period.getCreatedBy() != null) {
                stmt.setInt(7, period.getCreatedBy().getUserID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }

            if (period.getLastUpdatedBy() != null) {
                stmt.setInt(8, period.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }

            stmt.execute();

            String lastIDNumSQL = "SELECT currval('occperiodid_seq'::regclass);";

            stmt = con.prepareStatement(lastIDNumSQL);

            rs = stmt.executeQuery();

            while (rs.next()) {
                newPeriodId = rs.getInt("currval");
            }
            
            
        // TODO: fix this with fee/payment update    
//        pc.insertAutoAssignedFees(period);

        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.insertOccPeriod"
                    + "| IntegrationError: unable to insert occupancy period", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        return newPeriodId;
    }
    
    

    /**
     * Updates an occ period 
     * @param period
     * @throws IntegrationException 
     */
    public void updateOccPeriod(OccPeriod period) throws IntegrationException {
        String query = "UPDATE public.occperiod\n"
                + "   SET source_sourceid=?, parcelunit_unitid=?,\n"
                + "       startdate=?, \n"
                + "       startdatecertifiedby_userid=?, startdatecertifiedts=?, enddate=?, \n"
                + "       enddatecertifiedby_userid=?, enddatecterifiedts=?, manager_userid=?, \n"
                + "       authorizationts=?, authorizedby_userid=?, overrideperiodtypeconfig=?, \n"
                + "       notes=?, lastupdatedby_userid=?, lastupdatedts=now() \n"
                + " WHERE periodid=?;";
        ResultSet rs = null;
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);

            // PARAMS LINE 1
            if(period.getSource() != null){
                stmt.setInt(1, period.getSource().getSourceid());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            stmt.setInt(2, period.getPropertyUnitID());
            // timestamp set to now()

            if (period.getStartDate() != null) {
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(period.getStartDate()));
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            // PARAMS LINE 3
            if (period.getStartDateCertifiedBy() != null) {
                stmt.setInt(4, period.getStartDateCertifiedBy().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }

            if (period.getStartDateCertifiedTS() != null) {
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(period.getStartDateCertifiedTS()));
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }

            if (period.getEndDate() != null) {
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(period.getEndDate()));
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            if (period.getEndDateCertifiedBy() != null) {
                stmt.setInt(7, period.getEndDateCertifiedBy().getUserID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }

            if (period.getEndDateCertifiedTS() != null) {
                stmt.setTimestamp(8, java.sql.Timestamp.valueOf(period.getEndDateCertifiedTS()));
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }

            // PARAMS LINE 4
            if (period.getManager() != null) {
                stmt.setInt(9, period.getManager().getUserID());
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            if (period.getAuthorizedTS() != null) {
                stmt.setTimestamp(10, java.sql.Timestamp.valueOf(period.getAuthorizedTS()));
            } else {
                stmt.setNull(10, java.sql.Types.NULL);
            }
            if (period.getAuthorizedBy() != null) {
                stmt.setInt(11, period.getAuthorizedBy().getUserID());
            } else {
                stmt.setNull(11, java.sql.Types.NULL);
            }
            stmt.setBoolean(12, period.isOverrideTypeConfig());

            // PARAMS LINE 5
            stmt.setString(13, period.getNotes());
           

            if(period.getLastUpdatedBy() != null){
                stmt.setInt(14, period.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(14, java.sql.Types.NULL);
            }

            stmt.setInt(15, period.getPeriodID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Integration error: Unable to update occ period. This is a fatal error that should be reported.", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }

    }
 
    /**
     * Generator for occ period types
     * @param rs
     * @return
     * @throws IntegrationException 
     */
    private OccPermitType generateOccPermitType(ResultSet rs) throws IntegrationException {
        OccPermitType opt = new OccPermitType();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        PaymentIntegrator pi = getPaymentIntegrator();
        
        try {
            opt.setTypeID(rs.getInt("typeid"));
            opt.setMuni(mi.getMuni(rs.getInt("muni_municode")));
            opt.setTitle(rs.getString("title"));
            opt.setAuthorizeduses(rs.getString("authorizeduses"));
            opt.setDescription(rs.getString("description"));
            opt.setUserassignable(rs.getBoolean("userassignable"));
            
            opt.setPermittable(rs.getBoolean("permittable"));
            opt.setPassedInspectionRequired(rs.getBoolean("requireinspectionpass"));
            
            opt.setRequireLeaseLink(rs.getBoolean("requireleaselink"));
            opt.setActive(rs.getBoolean("active"));
            opt.setAllowthirdpartyinspection(rs.getBoolean("allowthirdpartyinspection"));
//            opt.setOptionalpersontypeList(generateOptionalPersonTypes(rs));

//            opt.setRequiredPersontypeList(generateRequiredPersonTypes(rs));
            opt.setCommercial(rs.getBoolean("commercial"));
            
            opt.setDefaultValidityPeriodDays(rs.getInt("defaultpermitvalidityperioddays"));
            
            
            opt.setBaseRuleSetID(rs.getInt("eventruleset_setid"));
            opt.setPermitTitle(rs.getString("permittitle"));
            opt.setPermitTitleSub(rs.getString("permittitlesub"));
            opt.setExpires(rs.getBoolean("expires"));
            
            opt.setRequireManager(rs.getBoolean("requiremanager"));
            opt.setRequireTenant(rs.getBoolean("requiretenant"));
            opt.setRequireZeroBalance(rs.getBoolean("requirezerobalance"));

            // ** Deac during CHARGES UPGRADE
//            opt.setPermittedFees(pi.getFeeList(opt));
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error generating OccPermitType from ResultSet", ex);
        }

        return opt;
    }

    /**
     * Insertion point for occ period applications
     * @param application
     * @return
     * @throws IntegrationException 
     */
    public int insertOccPermitApplication(OccPermitApplication application) throws IntegrationException {
        String query = "INSERT INTO public.occpermitapplication(applicationid,  "
                + "reason_reasonid, submissiontimestamp, "
                + "submitternotes, internalnotes, propertyunitid, "
                + "declaredtotaladults, declaredtotalyouth, rentalintent, "
                + "occperiod_periodid, externalnotes, status, "
                + "applicationpubliccc, paccenabled, allowuplinkaccess) "
                + "VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::occapplicationstatus, ?, ?, ?) "
                + "RETURNING applicationid;";

        Connection con = null;
        PreparedStatement stmt = null;
        int applicationId;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, application.getReason().getId());
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(application.getSubmissionDate()));
            stmt.setString(3, application.getSubmissionNotes());
            stmt.setString(4, application.getInternalNotes());
            stmt.setString(5, String.valueOf(application.getApplicationPropertyUnit().getUnitID()));
            
            int adults = 0;
            int youth = 0;
                    
                    for(Human h : application.getAttachedPersons()){
                        
                        if(h.isUnder18()){
                            youth++;
                        } else {
                            adults++;
                        }
                        
                    }
            
            stmt.setInt(6, adults);
            stmt.setInt(7, youth);
            stmt.setBoolean(8, false);
            stmt.setInt(9, application.getConnectedPeriod().getPeriodID());
            stmt.setString(10, application.getExternalPublicNotes());
            stmt.setString(11, OccApplicationStatusEnum.Waiting.name());
            stmt.setInt(12, application.getPublicControlCode());
            
            application.setPaccEnabled(true); //We want to make sure they default to visible
            
            stmt.setBoolean(13, application.isPaccEnabled());
            stmt.setBoolean(14, application.isUplinkAccess());
                    
            stmt.execute();
            ResultSet inserted_application = stmt.getResultSet();
            inserted_application.next();
            applicationId = inserted_application.getInt(1);

        } catch (SQLException ex) {
            System.out.println("OccupancyIntegrator.insertOccPermitApplication() | ERROR:" + ex);
            throw new IntegrationException("OccupancyIntegrator.insertOccPermitApplication"
                    + "| IntegrationError: unable to insert occupancy permit application ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            
        }

        return applicationId;
    }

    /**
     * Extracts all occ permit application reasons
     * @return
     * @throws IntegrationException 
     */
    public List<OccPermitApplicationReason> getOccPermitApplicationReasons() throws IntegrationException {

        OccPermitApplicationReason reason = null;
        List<OccPermitApplicationReason> reasons = new ArrayList<>();
        String query = "SELECT reasonid, reasontitle, reasondescription, activereason, humanfriendlydescription, periodtypeproposal_periodid "
                + "FROM public.occpermitapplicationreason "
                + "WHERE activereason = 'true';";

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                reason = generateOccPermitApplicationReason(rs);
                reasons.add(reason);
            }

        } catch (SQLException ex) {
            System.out.println("OccupancyIntegrator.getOccPermitApplicationReasons() | ERROR: " + ex);
            throw new IntegrationException("OccupancyInspectionIntegrator.getOccPermitApplicationReasons "
                    + "| IntegrationException: Unable to get occupancy permit application reasons ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        return reasons;
    }

    /**
     * Extracts a single occ permit application by ID
     * @param applicationID
     * @return
     * @throws IntegrationException
     * @throws EventException
     * @throws AuthorizationException
     * @throws BObStatusException
     * @throws ViolationException
     */
    public OccPermitApplication getOccPermitApplication(int applicationID) throws IntegrationException, EventException, AuthorizationException, BObStatusException, ViolationException, ViolationException {
        OccPermitApplication occpermitapp = null;
        String query = "   SELECT applicationid, reason_reasonid, submissiontimestamp, \n"
                + "       submitternotes, internalnotes, propertyunitid, declaredtotaladults, \n"
                + "       declaredtotalyouth, occperiod_periodid, rentalintent, status, externalnotes,\n"
                + "       applicationpubliccc, paccenabled, allowuplinkaccess\n"
                + "  FROM public.occpermitapplication WHERE applicationid=?;";

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, applicationID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                occpermitapp = generateOccPermitApplication(rs);
            }
        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyInspectionIntegrator.getOccPermitApplication | "
                    + "IntegrationException: Unable to retrieve occupancy permit application ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        return occpermitapp;
    }

    
    /**
     * Generator for occ permit applications
     * @param rs
     * @return
     * @throws IntegrationException
     * @throws EventException
     * @throws AuthorizationException
     * @throws BObStatusException
     * @throws ViolationException 
     */
    private OccPermitApplication generateOccPermitApplication(ResultSet rs) throws IntegrationException, EventException, AuthorizationException, BObStatusException, ViolationException {
        OccPermitApplication occpermitapp = new OccPermitApplication();
        PersonIntegrator pi = getPersonIntegrator();
        PropertyIntegrator propint = getPropertyIntegrator();
        try {
            occpermitapp.setId(rs.getInt("applicationid"));
            occpermitapp.setReason(getOccPermitApplicationReason(rs.getInt("reason_reasonid")));
            occpermitapp.setSubmissionDate(rs.getTimestamp("submissiontimestamp").toLocalDateTime());
            occpermitapp.setSubmissionNotes(rs.getString("submitternotes"));
            occpermitapp.setInternalNotes(rs.getString("internalNotes"));
            occpermitapp.setApplicationPropertyUnit(propint.getPropertyUnit(Integer.parseInt(rs.getString("propertyunitid"))));
            occpermitapp.setConnectedPeriod(getOccPeriod(rs.getInt("occperiod_periodid")));
            occpermitapp.setStatus(OccApplicationStatusEnum.valueOf(rs.getString("status")));
            occpermitapp.setExternalPublicNotes(rs.getString("externalnotes"));
            occpermitapp.setPublicControlCode(rs.getInt("applicationpubliccc"));
            occpermitapp.setPaccEnabled(rs.getBoolean("paccenabled"));
            occpermitapp.setUplinkAccess(rs.getBoolean("allowuplinkaccess"));
            
            if(occpermitapp.getConnectedPeriod() != null)
            {
            
                occpermitapp.setAttachedPersons(new ArrayList<>());
                
                for (HumanLink skeleton : pi.getPersonOccApplicationList(occpermitapp)) {

    //  ----->  TODO: Update for Humanization/Parcelization <------
//                    if (skeleton.isApplicant()) {
//                        occpermitapp.setApplicantPerson(skeleton);
//                    }
//
//                    if(skeleton.isPreferredContact()){
//                        occpermitapp.setPreferredContact(skeleton);
//                    }

                    occpermitapp.getAttachedPersons().add(skeleton);

                }
            }
            
        } catch (SQLException ex) {
            System.out.println("OccupancyIntegrator.generateOccPermitApplication() | ERROR: " + ex);
            throw new IntegrationException("OccupancyInspectionIntegrator.generateOccPermitApplication | "
                    + "IntegrationException: Unable to generate occupancy permit application ", ex);
        }
        return occpermitapp;
    }

    /**
     * Gets all OccPermitApplications. Use with care.
     * @return
     * @throws IntegrationException
     * @throws EventException
     * @throws AuthorizationException
     * @throws BObStatusException
     * @throws ViolationException 
     */
    public List<OccPermitApplication> getOccPermitApplicationList() throws IntegrationException, EventException, AuthorizationException, BObStatusException, ViolationException {
        List<OccPermitApplication> occpermitappList = new ArrayList<>();
        String query = "SELECT applicationid \n"
                + "  FROM public.occpermitapplication;";

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                occpermitappList.add(getOccPermitApplication(rs.getInt("applicationid")));
            }
        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.getOccPermitApplicationList", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        return occpermitappList;
    }
    
    /**
     * Gets all occ permit applications by period
     * @param op
     * @return
     * @throws IntegrationException
     * @throws EventException
     * @throws AuthorizationException
     * @throws BObStatusException
     * @throws ViolationException 
     */
    public List<OccPermitApplication> getOccPermitApplicationList(OccPeriod op) throws IntegrationException, EventException, AuthorizationException, BObStatusException, ViolationException {
        List<OccPermitApplication> occpermitappList = new ArrayList<>();
        String query = "SELECT occpermitapp_applicationid\n"
                + "  FROM public.occperiodpermitapplication WHERE occperiod_periodid = ?;   ";

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, op.getPeriodID());
            rs = stmt.executeQuery();

            while (rs.next()) {
                occpermitappList.add(getOccPermitApplication(rs.getInt("occpermitapp_applicationid")));
            }
        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.getOccPermitApplicationList", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        return occpermitappList;
    }

    /**
     * Extracts an occ period application by integer public control code
     * @param pacc
     * @return
     * @throws IntegrationException
     * @throws EventException
     * @throws AuthorizationException
     * @throws BObStatusException
     * @throws ViolationException 
     */
    public List<OccPermitApplication> getOccPermitApplicationListByControlCode(int pacc) throws IntegrationException, EventException, AuthorizationException, BObStatusException, ViolationException {
        List<OccPermitApplication> occpermitappList = new ArrayList<>();
        String query = "SELECT applicationid\n"
                + "  FROM public.occpermitapplication WHERE applicationpubliccc = ?;   ";

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, pacc);
            rs = stmt.executeQuery();

            while (rs.next()) {
                occpermitappList.add(getOccPermitApplication(rs.getInt("applicationid")));
            }
        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.getOccPermitApplicationListByControlCode() | ERROR: " + ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        return occpermitappList;
    }
    
    /**
     * Changes PACC access by permit
     * @param permit
     * @throws IntegrationException 
     */
    public void updatePACCAccess(OccPermitApplication permit) throws IntegrationException {

        String q = "UPDATE occpermitapplication SET paccenabled = ? WHERE applicationid = ?;";

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(q);
            stmt.setBoolean(1, permit.isPaccEnabled());
            stmt.setInt(2, permit.getId());
            // Retrieve action data from postgres
            stmt.executeUpdate();

            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("OccupancyIntegrator.updatePACCAccess | Integration Error: Unable to update OccPermitApplication", ex);
        } finally {
            
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }
    
    /**
     * Attaches a message to the OccPermitApplication inside the PublicInfoBundle, 
     * uses PACC to find application
     * @param application
     * @param message
     * @throws IntegrationException 
     */
    public void attachMessageToOccPermitApplication(PublicInfoBundleOccPermitApplication application, String message) throws IntegrationException {
        String query = "UPDATE public.occpermitapplication\n"
                + "SET externalnotes=? WHERE occpermitapplication.applicationpubliccc = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, message);
            stmt.setInt(2, application.getPacc());
            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println("OccupancyIntegrator.attachMessageToOccPermitApplication() | ERROR: " + ex);
            throw new IntegrationException("OccupancyInspectionIntegrator.attachMessageToOccPermitApplication"
                    + " | IntegrationException: Unable to attach message to occupancy permit application ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            
        }

    }
    
    /**
     * Updates a record in the occpermitapplication table
     * @param application
     * @throws IntegrationException 
     */
    public void updateOccPermitApplication(OccPermitApplication application) throws IntegrationException {
        String query = "UPDATE public.occpermitapplication "
                + "SET reason_reasonid=?, submissiontimestamp=?, "
                + "submitternotes=?, internalnotes=?, propertyunitid=?, externalnotes=?, status=?::occapplicationstatus, "
                + "applicationpubliccc=?, paccenabled=?, allowuplinkaccess=? "
                + "WHERE occpermitapplication.applicationid = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, application.getReason().getId());
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(application.getSubmissionDate()));
            stmt.setString(3, application.getSubmissionNotes());
            stmt.setString(4, application.getInternalNotes());
            stmt.setString(5, String.valueOf(application.getApplicationPropertyUnit().getUnitID()));
            stmt.setString(6, application.getExternalPublicNotes());
            stmt.setString(7, application.getStatus().name());
            stmt.setInt(8, application.getPublicControlCode());
            stmt.setBoolean(9, application.isPaccEnabled());
            stmt.setBoolean(10, application.isUplinkAccess());
            stmt.setInt(11, application.getId());
            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println("OccupancyIntegrator.updateOccPermitApplication() | ERROR: " + ex);
            throw new IntegrationException("OccupancyInspectionIntegrator.updateOccPermitApplication"
                    + " | IntegrationException: Unable to update occupancy permit application ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            
        }

    }

    /**
     * Doesn't do anything yet.
     * @param application 
     */
    public void deleteOccPermitApplication(OccPermitApplication application) {
    }

    /**
     * Extracts a single occ permit application reason by ID
     * @param reasonId
     * @return
     * @throws IntegrationException 
     */
    public OccPermitApplicationReason getOccPermitApplicationReason(int reasonId) throws IntegrationException {
        OccPermitApplicationReason occpermitappreason = null;

        String query = "SELECT reasonid, reasontitle, reasondescription, activereason, "
                + "humanfriendlydescription, periodtypeproposal_periodid\n "
                + "FROM public.occpermitapplicationreason \n"
                + "WHERE reasonid = ?;";

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, reasonId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                occpermitappreason = generateOccPermitApplicationReason(rs);
            }

        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyInspectionIntegrator.getOccPermitApplicationReason | "
                    + "IntegrationException: Unable to get occupancy permit application reason ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }

        return occpermitappreason;
    }

    /**
     * Generator for occpermit application reasons
     * @param rs
     * @return
     * @throws IntegrationException 
     */
    private OccPermitApplicationReason generateOccPermitApplicationReason(ResultSet rs) throws IntegrationException {
        OccPermitApplicationReason occpermitappreason = new OccPermitApplicationReason();

        try {
            occpermitappreason.setId(rs.getInt("reasonid"));
            occpermitappreason.setTitle(rs.getString("reasontitle"));
            occpermitappreason.setDescription(rs.getString("reasondescription"));
            occpermitappreason.setActive(rs.getBoolean("activereason"));
            occpermitappreason.setHumanFriendlyDescription(rs.getString("humanfriendlydescription"));
            occpermitappreason.setPersonsRequirement(getPersonsRequirement(rs.getInt("reasonid")));
            occpermitappreason.setProposalPeriodType(getOccPermitType(rs.getInt("periodtypeproposal_periodid")));
        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.generateOccPermitApplicationReason | "
                    + "Integration Error: Unable to generate occupancy permit application reason ", ex);
        }

        return occpermitappreason;
    }

    /**
     * Extracts and builds an occ application person requirement
     * @deprecated  with humanization there are no longer person types
     * @param reasonId
     * @return
     * @throws IntegrationException 
     */
    public OccAppPersonRequirement getPersonsRequirement(int reasonId) throws IntegrationException {
        OccAppPersonRequirement personsRequirement = null;
        String query = "SELECT reasonid, humanfriendlydescription FROM public.occpermitapplicationreason "
                + "WHERE reasonid = ?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, reasonId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                personsRequirement = generatePersonsRequirement(rs);
            }
        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.getPersonsRequirement | "
                    + "IntegrationError: Unable to get PersonsRequirement ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }

        return personsRequirement;
    }

    /**
     * Generator for Occ application person requriements
     * @deprecated  with humanization there are no longer person types
     * @param rs
     * @return
     * @throws IntegrationException 
     */
    private OccAppPersonRequirement generatePersonsRequirement(ResultSet rs) throws IntegrationException {
        OccAppPersonRequirement personsRequirement = new OccAppPersonRequirement();

        try {
            personsRequirement.setRequirementSatisfied(false);
            personsRequirement.setRequirementExplanation(rs.getString("humanfriendlydescription"));
            personsRequirement.setRequiredPersonTypes(getRequiredPersonTypes(rs.getInt("reasonid")));
            personsRequirement.setOptionalPersonTypes(getOptionalPersonTypes(rs.getInt("reasonid")));
        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.generatePersonsRequirement | "
                    + "IntegrationError: Unable to generate PersonsRequirement. ", ex);
        }

        return personsRequirement;
    }

    /**
     * Extracts person types
     * @deprecated  with humanization there are no longer person types
     * @param reasonId
     * @return
     * @throws IntegrationException 
     */
    public List<PersonType> getRequiredPersonTypes(int reasonId) throws IntegrationException {
        List<PersonType> requiredPersonTypes = null;
        String query = "SELECT requiredpersontypes FROM public.occpermitapplicationreason "
                + "WHERE reasonid = ?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, reasonId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                requiredPersonTypes = generateRequiredPersonTypes(rs);
            }
        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.getRequiredPersonTypes | "
                    + "IntegrationError: Unable to get required person types ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }

        return requiredPersonTypes;
    }

    /**
     * Generator of required person types
     * @deprecated  with humanization there are no longer person types
     * @param rs
     * @return
     * @throws IntegrationException 
     */
    private List<PersonType> generateRequiredPersonTypes(ResultSet rs) throws IntegrationException {
        List<PersonType> requiredPersonTypes = new ArrayList<>();
        String[] convertedPersonTypes = null;

        try {

            Array personTypes = rs.getArray("requiredpersontypes");
            convertedPersonTypes = (String[]) personTypes.getArray();

        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.generateRequiredPersonTypes | "
                    + "IntegrationError: Unable to generate required person types ", ex);
        }
        for (String personType : convertedPersonTypes) {
            PersonType requiredPersonType = PersonType.valueOf(personType);
            requiredPersonTypes.add(requiredPersonType);
        }
        return requiredPersonTypes;
    }

    /**
     * Extracts optional person types
     * @deprecated  with humanization there are no longer person types
     * @param reasonId
     * @return
     * @throws IntegrationException 
     */
    public List<PersonType> getOptionalPersonTypes(int reasonId) throws IntegrationException {
        List<PersonType> optionalPersonTypes = null;
        String query = "SELECT optionalpersontypes FROM public.occpermitapplicationreason "
                + "WHERE reasonid = ?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, reasonId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                optionalPersonTypes = generateOptionalPersonTypes(rs);
            }
        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.getOptionalPersonTypes | "
                    + "IntegrationError: Unable to get optional person types. ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }

        return optionalPersonTypes;
    }

    /**
     * Generator for optional person types
     * @deprecated  with humanization there are no longer person types
     * @param rs
     * @return
     * @throws IntegrationException 
     */
    private List<PersonType> generateOptionalPersonTypes(ResultSet rs) throws IntegrationException {
        List<PersonType> optionalPersonTypes = new ArrayList<>();
        String[] convertedPersonTypes = null;

        try {
            java.sql.Array persTypesSQLArray = rs.getArray("optionalpersontypes");
            if (persTypesSQLArray != null) {
                convertedPersonTypes = (String[]) persTypesSQLArray.getArray();
            }

        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.generateOptionalPersonTypes | "
                    + "IntegrationError: Unable to generate optional person types. ", ex);
        }
        if (convertedPersonTypes != null) {
            for (String personType : convertedPersonTypes) {
                PersonType optionalPersonType = PersonType.valueOf(personType);
                optionalPersonTypes.add(optionalPersonType);
            }
        }
        return optionalPersonTypes;
    }
    
    /**
     * Inserts a person into the occpermitapplicationperson table in the
     * database.
     * @deprecated  with humanization there are no longer person types
     * @param person
     * @param applicationID
     * @throws IntegrationException
     */
    public void insertOccApplicationPerson(HumanLink person, int applicationID) throws IntegrationException {

        String query = "INSERT INTO public.occpermitapplicationperson(occpermitapplication_applicationid, "
                + "person_personid, applicant, preferredcontact, active, applicationpersontype)\n"
                + "VALUES (?, ?, ?, ?, true, CAST (? AS persontype));";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, applicationID);
            stmt.setInt(2, person.getHumanID());

//  ----->  TODO: Update for Humanization/Parcelization <------
//            stmt.setBoolean(3, person.isApplicant());
//            stmt.setBoolean(4, person.isPreferredContact());
//            stmt.setString(5, person.getApplicationPersonType().name());
            
            stmt.execute();
            } catch (SQLException ex) {
                System.out.println("OccupancyIntegrator.insertOccApplicationPerson() | ERROR: "+ ex);
                throw new IntegrationException("OccupancyIntegrator.insertOccApplicationPerson"
                        + " | IntegrationException: Unable to insert occupancy permit application ", ex);
            } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { } }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { } }
            }
    }
    
    public void updatePersonOccPeriod(HumanLink input, OccPermitApplication app) throws IntegrationException{
        Connection con = getPostgresCon();
        String query = "UPDATE occpermitapplicationperson "
                + "SET applicant = ?, preferredcontact = ?, applicationpersontype = ?::persontype, active = ? "
                + "WHERE person_personid = ? AND occpermitapplication_applicationid = ?;";

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);

//  ----->  TODO: Update for Humanization/Parcelization <------
//            stmt.setBoolean(1, input.isApplicant());
//            stmt.setBoolean(2, input.isPreferredContact());
//            stmt.setString(3, input.getApplicationPersonType().name());
//            stmt.setBoolean(4, input.isLinkActive());
            stmt.setInt(5, input.getHumanID());
            stmt.setInt(6, app.getId());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update person-occperiod link");
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
}
