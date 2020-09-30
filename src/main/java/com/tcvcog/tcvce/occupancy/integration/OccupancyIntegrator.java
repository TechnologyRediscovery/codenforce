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
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonOccApplication;
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
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import com.tcvcog.tcvce.entities.occupancy.OccAppPersonRequirement;
import com.tcvcog.tcvce.entities.occupancy.OccApplicationStatusEnum;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.search.SearchParamsOccPeriod;
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
 * composite elements all nicely situated on shelves and in Lists
 *
 * High-level object families produced here include: OccPeriod OccPeriodType
 * OccPermit OccPermitApplication
 *
 * @author ellen bascomb of apt 31y
 */
public class OccupancyIntegrator extends BackingBeanUtils implements Serializable {

    
    final String ACTIVE_FIELD = "occperiod.active";
    
    /**
     * Creates a new instance of OccupancyIntegrator
     */
    public OccupancyIntegrator() {
    }

    
    public List<OccPeriod> getOccPeriodList(PropertyUnit pu, UserAuthorized u) 
            throws  IntegrationException, 
                    AuthorizationException, 
                    EventException, 
                    BObStatusException, 
                    ViolationException {
        return getOccPeriodList(pu.getUnitID());
    }
    
    public List<OccPeriod> getOccPeriodList(int unitID) throws IntegrationException, EventException, AuthorizationException, BObStatusException, ViolationException {
        List<OccPeriod> opList = new ArrayList<>();
        String query = "SELECT periodid FROM public.occperiod WHERE propertyunit_unitid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, unitID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                opList.add(getOccPeriod(rs.getInt("periodid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build occ period", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return opList;
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
        params.appendSQL("FROM occperiod INNER JOIN occperiodtype ON (type_typeid = typeid) \n");
        params.appendSQL("INNER JOIN propertyunit ON (occperiod.propertyunit_unitid = unitid) \n");
        params.appendSQL("INNER JOIN property ON (propertyunit.property_propertyid = property.propertyid) \n ");
        params.appendSQL("LEFT OUTER JOIN occinspection ON (occinspection.occperiod_periodid = periodid) \n");
        params.appendSQL("LEFT OUTER JOIN occpermit ON (occpermit.occperiod_periodid = periodid) \n ");
        params.appendSQL("LEFT OUTER JOIN occperiodperson ON (occperiod.periodid = occperiodperson.person_personid) \n");
        params.appendSQL("WHERE occperiod.periodid IS NOT NULL ");

        
        if (!params.isBobID_ctl()) {
           // *******************************
           // **   MUNI,DATES,USER,ACTIVE  **
           // *******************************
            params = (SearchParamsOccPeriod) sc.assembleBObSearchSQL_muniDatesUserActive(
                                                            params, 
                                                            SearchParamsOccPeriod.MUNI_DBFIELD,
                                                            ACTIVE_FIELD);
            
           // *******************************
            // **        PROPERTY           **
            // *******************************
             if (params.isProperty_ctl()) {
                if(params.getProperty_val()!= null){
                    params.appendSQL("AND property_propertyid=? ");
                } else {
                    params.setProperty_ctl(false);
                    params.appendToParamLog("PROPERTY: no Property object; prop filter disabled");
                }
            }
            
            // *******************************
            // **       PROPERTY UNIT       **
            // *******************************
             if (params.isPropertyUnit_ctl()) {
                if(params.getPropertyUnit_val()!= null){
                    params.appendSQL("AND propertyunit_unitid=? ");
                } else {
                    params.setPropertyUnit_ctl(false);
                    params.appendToParamLog("PROPERTY UNIT: no PropertyUnit object; propunit filter disabled");
                }
            }
             
            // *******************************
            // **       PERIOD TYPE         **
            // *******************************
            if (params.isPeriodType_ctl()) {
                if(params.getPeriodType_val() != null){
                    params.appendSQL("AND type_typeid=? ");
                } else {
                    params.setPeriodType_ctl(false);
                    params.appendToParamLog("PERIOD TYPE: no type object found; type filter disabled");
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
                    stmt.setInt(++paramCounter, params.getProperty_val().getPropertyID());
                }

                if (params.isPropertyUnit_ctl()) {
                    stmt.setInt(++paramCounter, params.getPropertyUnit_val().getUnitID());
                }

                if (params.isPeriodType_ctl()) {
                    stmt.setInt(++paramCounter, params.getPeriodType_val().getTypeID());
                }

                // filter OCC-8
                if (params.isPerson_ctl()) {
                    stmt.setInt(++paramCounter, params.getPerson_val().getPersonID());
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


    public OccPeriod getOccPeriod(int periodid) throws IntegrationException, 
                                                                EventException, 
                                                                AuthorizationException, 
                                                                BObStatusException, 
                                                                ViolationException {
        OccPeriod op = null;
        OccupancyCoordinator oc = getOccupancyCoordinator();
        String query = "SELECT periodid, source_sourceid, propertyunit_unitid, createdts, type_typeid, \n"
                + "       typecertifiedby_userid, typecertifiedts, startdate, startdatecertifiedby_userid, \n"
                + "       startdatecertifiedts, enddate, enddatecertifiedby_userid, enddatecterifiedts, \n"
                + "       manager_userid, authorizationts, authorizedby_userid, overrideperiodtypeconfig, \n"
                + "       notes, createdby_userid, active \n"
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
        return oc.configureOccPeriod(op);
    }

    private OccPeriod generateOccPeriod(ResultSet rs) throws SQLException, IntegrationException {
        SystemIntegrator si = getSystemIntegrator();
        UserIntegrator ui = getUserIntegrator();

        OccPeriod op = new OccPeriod();

        op.setPeriodID(rs.getInt("periodid"));

        op.setCreatedBy(ui.getUser(rs.getInt("createdby_userid")));

        op.setSource(si.getBOBSource(rs.getInt("source_sourceid")));
        op.setPropertyUnitID(rs.getInt("propertyunit_unitid"));
        if(rs.getTimestamp("createdts") != null){
            op.setCreatedTS(rs.getTimestamp("createdts").toLocalDateTime());
        }

        op.setType(getOccPeriodType(rs.getInt("type_typeid")));
        op.setPeriodTypeCertifiedBy(ui.getUser(rs.getInt("typecertifiedby_userid")));
        if(rs.getTimestamp("typecertifiedts") != null){
            op.setPeriodTypeCertifiedTS(rs.getTimestamp("typecertifiedts").toLocalDateTime());
        }

        if(rs.getTimestamp("startdate") != null){
            op.setStartDate(rs.getTimestamp("startdate").toLocalDateTime());
        }
            
        op.setStartDateCertifiedBy(ui.getUser(rs.getInt("startdatecertifiedby_userid")));
        if(rs.getTimestamp("startdatecertifiedts") != null){
            op.setStartDateCertifiedTS(rs.getTimestamp("startdatecertifiedts").toLocalDateTime());
        }

        if(rs.getTimestamp("enddate") != null){
            op.setEndDate(rs.getTimestamp("enddate").toLocalDateTime());
        }
        op.setEndDateCertifiedBy(ui.getUser(rs.getInt("enddatecertifiedby_userid")));
        if(rs.getTimestamp("enddatecterifiedts") != null){
            op.setEndDateCertifiedTS(rs.getTimestamp("enddatecterifiedts").toLocalDateTime());
        }

        op.setManager(ui.getUser(rs.getInt("manager_userid")));

        if(rs.getTimestamp("authorizationts") != null){
            op.setAuthorizedTS(rs.getTimestamp("authorizationts").toLocalDateTime());
        }
        op.setAuthorizedBy(ui.getUser(rs.getInt("authorizedby_userid")));

        op.setOverrideTypeConfig(rs.getBoolean("overrideperiodtypeconfig"));
        op.setNotes(rs.getString("notes"));

        
        op.setActive(rs.getBoolean("active"));

        return op;
    }
    
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

    public OccPermit getOccPermit(int permitID) throws IntegrationException {
        OccPermit op = null;
        String query = "SELECT permitid, occperiod_periodid, referenceno, issuedto_personid, \n"
                + "       issuedby_userid, dateissued, permitadditionaltext, notes\n"
                + "  FROM public.occpermit WHERE permitid=?;";
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
            throw new IntegrationException("Unable to build property unit list due to an DB integration error", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return op;

    }

    private OccPermit generateOccPermit(ResultSet rs) throws SQLException, IntegrationException {
        UserIntegrator ui = getUserIntegrator();
        OccPermit permit = new OccPermit();
        PersonIntegrator pi = getPersonIntegrator();

        permit.setPermitID(rs.getInt("permitid"));
        permit.setPeriodID(rs.getInt("occperiod_periodid"));
        permit.setReferenceNo(rs.getString("referenceno"));

        if (rs.getTimestamp("dateissued") != null) {
            permit.setDateIssued(rs.getTimestamp("dateissued").toLocalDateTime());
        }

        permit.setIssuedBy(ui.getUser(rs.getInt("issuedby_userid")));
        permit.setIssuedTo(pi.getPerson(rs.getInt("issuedto_personid")));

        permit.setPermitAdditionalText(rs.getString("permitadditionaltext"));
        permit.setNotes(rs.getString("notes"));

        return permit;
    }

    public List<OccPermit> getOccPermitList(OccPeriod period) throws IntegrationException {
        List<OccPermit> permitList = new ArrayList<>();
        String query = "SELECT permitid FROM public.occpermit WHERE occperiod_periodid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, period.getPeriodID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                permitList.add(getOccPermit(rs.getInt("permitid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build occ permit list", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return permitList;
    }

    /**
     * Gets an Integer-coded list of blobs
     *
     * @param period
     * @return
     * @throws IntegrationException
     */
    public List<Integer> getBlobList(OccPeriod period) throws IntegrationException {
        List<Integer> blobIDList = new ArrayList<>();
        String query = "SELECT photodoc_photodocid, occperiod_periodid\n"
                + "  FROM public.occperiodphotodoc WHERE occperiod_periodid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, period.getPeriodID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                blobIDList.add(rs.getInt("photodoc_photodocid"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build occ permit list", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return blobIDList;

    }

    /**
     * TODO: Finish me!
     * @param opt
     * @throws IntegrationException 
     */
    public void updateOccPeriodType(OccPeriodType opt) throws IntegrationException {
        String query = "UPDATE public.occpermittype\n"
                + "   SET typename=?, typedescription=?\n"
                + " WHERE typeid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            //stmt.setInt(1, opt.getOccupancyPermitTypeID());
            //stmt.setInt(2, opt.getOccupancyPermitTypeMuniCodeID());

            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update occupancy permit type", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
    }

    public void deleteOccPeriodType(OccPeriodType opt) throws IntegrationException {
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

    public OccPeriodType getOccPeriodType(int typeid) throws IntegrationException {
        OccPeriodType tpe = null;
        String query = "SELECT typeid, muni_municode, title, authorizeduses, description, userassignable, \n" +
                        "       permittable, startdaterequired, enddaterequired, passedinspectionrequired, \n" +
                        "       rentalcompatible, active, allowthirdpartyinspection, optionalpersontypes, \n" +
                        "       requiredpersontypes, commercial, requirepersontypeentrycheck, \n" +
                        "       defaultpermitvalidityperioddays, occchecklist_checklistlistid, \n" +
                        "       asynchronousinspectionvalidityperiod, defaultinspectionvalidityperiod, \n" +
                        "       eventruleset_setid, inspectable, permittitle, permittitlesub\n" +
                        "  FROM public.occperiodtype WHERE typeid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, typeid);
            rs = stmt.executeQuery();
            while (rs.next()) {
                tpe = generateOccPeriodType(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build property unit list due to an DB integration error", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return tpe;
    }

    public List<OccPeriodType> getOccPeriodTypeList(int muniProfileID) throws IntegrationException {
        List<OccPeriodType> typeList = new ArrayList<>();
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
                typeList.add(getOccPeriodType(rs.getInt("occperiodtype_typeid")));
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

    public List<OccPeriodType> getCompleteOccPeriodTypeList() throws IntegrationException {
        List<OccPeriodType> occPeriodTypeList = new ArrayList<>();
        String query = "SELECT muniprofile_profileid, occperiodtype_typeid\n"
                + "  FROM public.muniprofileoccperiodtype;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                occPeriodTypeList.add(getOccPeriodType(rs.getInt("occperiodtype_typeid")));
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

    public void insertOccPeriodType(OccPeriodType periodType) throws IntegrationException {
        String query = "INSERT INTO public.occperiodtype(\n" +
                        "            typeid, muni_municode, title, authorizeduses, description, userassignable, \n" +
                        "            permittable, startdaterequired, enddaterequired, passedinspectionrequired, \n" +
                        "            rentalcompatible, active, allowthirdpartyinspection, optionalpersontypes, \n" +
                        "            requiredpersontypes, commercial, requirepersontypeentrycheck, \n" +
                        "            defaultpermitvalidityperioddays, occchecklist_checklistlistid, \n" +
                        "            asynchronousinspectionvalidityperiod, defaultinspectionvalidityperiod, \n" +
                        "            eventruleset_setid, inspectable, permittitle, permittitlesub)\n" +
                        "    VALUES (?, ?, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?, ?, \n" +
                        "            ?, ?, \n" +
                        "            ?, ?, ?, ?);";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, periodType.getMuni().getMuniCode());
            stmt.setString(2, periodType.getTitle());
            stmt.setString(3, periodType.getAuthorizeduses());
            stmt.setString(4, periodType.getDescription());
            stmt.setBoolean(5, periodType.isUserassignable());

            stmt.setBoolean(6, periodType.isPermittable());
            stmt.setBoolean(7, periodType.isStartdaterequired());
            stmt.setBoolean(8, periodType.isEnddaterequired());
            stmt.setBoolean(8, periodType.isPassedInspectionRequired());

            stmt.setBoolean(9, periodType.isRentalcompatible());
            stmt.setBoolean(10, periodType.isActive());
            stmt.setBoolean(11, periodType.isAllowthirdpartyinspection());
            stmt.setArray(12, con.createArrayOf("integer", periodType.getOptionalpersontypeList().toArray()));

            stmt.setArray(13, con.createArrayOf("integer", periodType.getRequiredPersontypeList().toArray()));
            stmt.setBoolean(14, periodType.isCommercial());
            stmt.setBoolean(15, periodType.isRequirepersontypeentrycheck());

            stmt.setInt(16, periodType.getDefaultValidityPeriodDays());
            stmt.setString(17, periodType.getPermitTitle());
            stmt.setString(18, periodType.getPermitTitleSub());

            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert OccupancyPermitType ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
    }

    public int insertOccPeriod(OccPeriod period) throws IntegrationException {
        String query = " INSERT INTO public.occperiod(\n"
                + "            periodid, source_sourceid, propertyunit_unitid, createdts, type_typeid, \n"
                + "            typecertifiedby_userid, typecertifiedts, startdate, startdatecertifiedby_userid, \n"
                + "            startdatecertifiedts, enddate, enddatecertifiedby_userid, enddatecterifiedts, \n"
                + "            manager_userid, authorizationts, authorizedby_userid, overrideperiodtypeconfig, \n"
                + "            notes, createdby_userid, active)\n"
                + "    VALUES (DEFAULT, ?, ?, now(), ?, \n"
                + "            ?, ?, ?, ?, \n"
                + "            ?, ?, ?, ?, \n"
                + "            ?, ?, ?, ?, \n"
                + "            ?, ?, ?);";
        ResultSet rs = null;
        Connection con = null;
        PreparedStatement stmt = null;
        int newPeriodId = 0;

        PaymentCoordinator pc = getPaymentCoordinator();
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);

            // PARAMS LINE 1
            stmt.setInt(1, 10);
            stmt.setInt(2, period.getPropertyUnitID());
            // timestamp set to now()
            stmt.setInt(3, period.getType().getTypeID());

            // PARAMS LINE 2
            if (period.getPeriodTypeCertifiedBy() != null) {
                stmt.setInt(4, period.getPeriodTypeCertifiedBy().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            if (period.getPeriodTypeCertifiedTS() != null) {
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(period.getPeriodTypeCertifiedTS()));
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            if (period.getStartDate() != null) {
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(period.getStartDate()));
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            // PARAMS LINE 3
            if (period.getStartDateCertifiedBy() != null) {
                stmt.setInt(7, period.getStartDateCertifiedBy().getUserID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }

            if (period.getStartDateCertifiedTS() != null) {
                stmt.setTimestamp(8, java.sql.Timestamp.valueOf(period.getStartDateCertifiedTS()));
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }

            if (period.getEndDate() != null) {
                stmt.setTimestamp(9, java.sql.Timestamp.valueOf(period.getEndDate()));
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            if (period.getEndDateCertifiedBy() != null) {
                stmt.setInt(10, period.getEndDateCertifiedBy().getUserID());
            } else {
                stmt.setNull(10, java.sql.Types.NULL);
            }

            if (period.getEndDateCertifiedTS() != null) {
                stmt.setTimestamp(11, java.sql.Timestamp.valueOf(period.getEndDateCertifiedTS()));
            } else {
                stmt.setNull(11, java.sql.Types.NULL);
            }

            // PARAMS LINE 4
            if (period.getManager() != null) {
                stmt.setInt(12, period.getManager().getUserID());
            } else {
                stmt.setNull(12, java.sql.Types.NULL);
            }
            if (period.getAuthorizedTS() != null) {
                stmt.setTimestamp(13, java.sql.Timestamp.valueOf(period.getAuthorizedTS()));
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }
            if (period.getAuthorizedBy() != null) {
                stmt.setInt(14, period.getAuthorizedBy().getUserID());
            } else {
                stmt.setNull(14, java.sql.Types.NULL);
            }
            stmt.setBoolean(15, period.isOverrideTypeConfig());

            // PARAMS LINE 5
            stmt.setString(16, period.getNotes());
            if (period.getCreatedBy() != null) {
                stmt.setInt(17, period.getCreatedBy().getUserID());
            } else {
                stmt.setNull(17, java.sql.Types.NULL);
            }
            
            stmt.setBoolean(18, period.isActive());

            stmt.execute();

            String lastIDNumSQL = "SELECT currval('occperiodid_seq'::regclass)";

            stmt = con.prepareStatement(lastIDNumSQL);

            rs = stmt.executeQuery();

            while (rs.next()) {
                newPeriodId = rs.getInt("currval");
            }
            
        pc.insertAutoAssignedFees(period);

        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.insertOccPermitApplication"
                    + "| IntegrationError: unable to insert occupancy permit application ", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        return newPeriodId;
    }

    public void updateOccPeriod(OccPeriod period) throws IntegrationException {
        String query = "UPDATE public.occperiod\n"
                + "   SET source_sourceid=?, propertyunit_unitid=?,\n"
                + "       type_typeid=?, typecertifiedby_userid=?, typecertifiedts=?, startdate=?, \n"
                + "       startdatecertifiedby_userid=?, startdatecertifiedts=?, enddate=?, \n"
                + "       enddatecertifiedby_userid=?, enddatecterifiedts=?, manager_userid=?, \n"
                + "       authorizationts=?, authorizedby_userid=?, overrideperiodtypeconfig=?, \n"
                + "       notes=?, createdby_userid=?, active=? \n"
                + " WHERE periodid=?;";
        ResultSet rs = null;
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);

            // PARAMS LINE 1
            stmt.setInt(1, period.getSource().getSourceid());
            stmt.setInt(2, period.getPropertyUnitID());
            // timestamp set to now()
            stmt.setInt(3, period.getType().getTypeID());

            // PARAMS LINE 2
            if (period.getPeriodTypeCertifiedBy() != null) {
                stmt.setInt(4, period.getPeriodTypeCertifiedBy().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            if (period.getPeriodTypeCertifiedTS() != null) {
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(period.getPeriodTypeCertifiedTS()));
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            if (period.getStartDate() != null) {
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(period.getStartDate()));
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            // PARAMS LINE 3
            if (period.getStartDateCertifiedBy() != null) {
                stmt.setInt(7, period.getStartDateCertifiedBy().getUserID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }

            if (period.getStartDateCertifiedTS() != null) {
                stmt.setTimestamp(8, java.sql.Timestamp.valueOf(period.getStartDateCertifiedTS()));
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }

            if (period.getEndDate() != null) {
                stmt.setTimestamp(9, java.sql.Timestamp.valueOf(period.getEndDate()));
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            if (period.getEndDateCertifiedBy() != null) {
                stmt.setInt(10, period.getEndDateCertifiedBy().getUserID());
            } else {
                stmt.setNull(10, java.sql.Types.NULL);
            }

            if (period.getEndDateCertifiedTS() != null) {
                stmt.setTimestamp(11, java.sql.Timestamp.valueOf(period.getEndDateCertifiedTS()));
            } else {
                stmt.setNull(11, java.sql.Types.NULL);
            }

            // PARAMS LINE 4
            if (period.getManager() != null) {
                stmt.setInt(12, period.getManager().getUserID());
            } else {
                stmt.setNull(12, java.sql.Types.NULL);
            }
            if (period.getAuthorizedTS() != null) {
                stmt.setTimestamp(13, java.sql.Timestamp.valueOf(period.getAuthorizedTS()));
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }
            if (period.getAuthorizedBy() != null) {
                stmt.setInt(14, period.getAuthorizedBy().getUserID());
            } else {
                stmt.setNull(14, java.sql.Types.NULL);
            }
            stmt.setBoolean(15, period.isOverrideTypeConfig());

            // PARAMS LINE 5
            stmt.setString(16, period.getNotes());
            if (period.getCreatedBy() != null) {
                stmt.setInt(17, period.getCreatedBy().getUserID());
            } else {
                stmt.setNull(17, java.sql.Types.NULL);
            }
            
            stmt.setInt(18, period.getPeriodID());
            
            stmt.setBoolean(19, period.isActive());

            stmt.executeUpdate();

           

        } catch (SQLException ex) {
            throw new IntegrationException("Integration error: Unable to update occ period. This is a fatal error that should be reported.", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }

    }
 
    private OccPeriodType generateOccPeriodType(ResultSet rs) throws IntegrationException {
        OccPeriodType opt = new OccPeriodType();
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
            opt.setStartdaterequired(rs.getBoolean("startdaterequired"));
            opt.setEnddaterequired(rs.getBoolean("enddaterequired"));
            opt.setPassedInspectionRequired(rs.getBoolean("passedinspectionrequired"));
            
            opt.setRentalcompatible(rs.getBoolean("rentalcompatible"));
            opt.setActive(rs.getBoolean("active"));
            opt.setAllowthirdpartyinspection(rs.getBoolean("allowthirdpartyinspection"));
//            opt.setOptionalpersontypeList(generateOptionalPersonTypes(rs));

//            opt.setRequiredPersontypeList(generateRequiredPersonTypes(rs));
            opt.setCommercial(rs.getBoolean("commercial"));
            opt.setRequirepersontypeentrycheck(rs.getBoolean("requirepersontypeentrycheck"));
            
            opt.setDefaultValidityPeriodDays(rs.getInt("defaultpermitvalidityperioddays"));
            opt.setChecklistID(rs.getInt("occchecklist_checklistlistid"));
            
            opt.setAsynchronousValidityPeriod((rs.getBoolean("asynchronousinspectionvalidityperiod")));
            opt.setDefaultValidityPeriodDays((rs.getInt("defaultinspectionvalidityperiod")));
            
            opt.setBaseRuleSetID(rs.getInt("eventruleset_setid"));
            opt.setInspectable(rs.getBoolean("inspectable"));
            opt.setPermitTitle(rs.getString("permittitle"));
            opt.setPermitTitleSub(rs.getString("permittitlesub"));

            opt.setPermittedFees(pi.getFeeList(opt));
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error generating OccPermitType from ResultSet", ex);
        }

        return opt;
    }

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
                    
                    for(Person p : application.getAttachedPersons()){
                        
                        if(p.isUnder18()){
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
            
                occpermitapp.setAttachedPersons(new ArrayList<PersonOccApplication>());
                
                for (PersonOccApplication skeleton : pi.getPersonOccApplicationList(occpermitapp)) {

                    if (skeleton.isApplicant()){
                        occpermitapp.setApplicantPerson(skeleton);
                    }

                    if(skeleton.isPreferredContact()){
                        occpermitapp.setPreferredContact(skeleton);
                    }

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

    private OccPermitApplicationReason generateOccPermitApplicationReason(ResultSet rs) throws IntegrationException {
        OccPermitApplicationReason occpermitappreason = new OccPermitApplicationReason();

        try {
            occpermitappreason.setId(rs.getInt("reasonid"));
            occpermitappreason.setTitle(rs.getString("reasontitle"));
            occpermitappreason.setDescription(rs.getString("reasondescription"));
            occpermitappreason.setActive(rs.getBoolean("activereason"));
            occpermitappreason.setHumanFriendlyDescription(rs.getString("humanfriendlydescription"));
            occpermitappreason.setPersonsRequirement(getPersonsRequirement(rs.getInt("reasonid")));
            occpermitappreason.setProposalPeriodType(getOccPeriodType(rs.getInt("periodtypeproposal_periodid")));
        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.generateOccPermitApplicationReason | "
                    + "Integration Error: Unable to generate occupancy permit application reason ", ex);
        }

        return occpermitappreason;
    }

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
     *
     * @param person
     * @param applicationID
     * @throws IntegrationException
     */
    public void insertOccApplicationPerson(PersonOccApplication person, int applicationID) throws IntegrationException {

        String query = "INSERT INTO public.occpermitapplicationperson(occpermitapplication_applicationid, "
                + "person_personid, applicant, preferredcontact, active, applicationpersontype)\n"
                + "VALUES (?, ?, ?, ?, true, CAST (? AS persontype));";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, applicationID);
            stmt.setInt(2, person.getPersonID());

            stmt.setBoolean(3, person.isApplicant());
            stmt.setBoolean(4, person.isPreferredContact());
            stmt.setString(5, person.getApplicationPersonType().name());
            
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
    
    public void updatePersonOccPeriod(PersonOccApplication input, OccPermitApplication app) throws IntegrationException{
        Connection con = getPostgresCon();
        String query = "UPDATE occpermitapplicationperson "
                + "SET applicant = ?, preferredcontact = ?, applicationpersontype = ?::persontype, active = ? "
                + "WHERE person_personid = ? AND occpermitapplication_applicationid = ?;";

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setBoolean(1, input.isApplicant());
            stmt.setBoolean(2, input.isPreferredContact());
            stmt.setString(3, input.getApplicationPersonType().name());
            stmt.setBoolean(4, input.isLinkActive());
            stmt.setInt(5, input.getPersonID());
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
    
    /*
    
     OccPeriodType tpe = null;
        ArrayList<OccPermit> permitList = new ArrayList();
        String query =  "SELECT typeid, muni_municode, title, authorizeduses, description, userassignable, \n" +
                        "       permittable, startdaterequired, enddaterequired, completedinspectionrequired, \n" +
                        "       rentalcompatible, active, allowthirdpartyinspection, optionalpersontypes, \n" +
                        "       requiredpersontypes, commercial, fee_feeid, requirepersontypeentrycheck\n" +
                        "  FROM public.occperiodtype; ";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
 
        try {
            
            stmt = con.prepareStatement(query);
//            stmt.setInt(1, permitID);
            rs = stmt.executeQuery();
            while(rs.next()){
                //permitList.add(generateOccupancyPermit(rs.getInt("permitid")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build property unit list due to an DB integration error", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { } }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) {  } }
        } // close finally
        return new ArrayList();
        
    **/
}
