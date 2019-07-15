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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.PropertyUnit;
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
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.integration.ChoiceIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
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
 * High-level object families produced here include:
 * OccPeriod
 * OccPeriodType
 * OccPermit
 * OccPermitApplication
 * 
 * @author Eric C. Darsow
 */
public class OccupancyIntegrator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of OccupancyIntegrator
     */
    public OccupancyIntegrator() {
    }
    
    public List<OccPeriod> getOccPeriodList(PropertyUnit unit) throws IntegrationException{
        List<OccPeriod> opList = new ArrayList<>();
        String query =  "SELECT periodid FROM public.occperiod WHERE propertyunit_unitid=?;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
 
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, unit.getUnitID());
            rs = stmt.executeQuery();
            while(rs.next()){
                opList.add(getOccPeriod(rs.getInt("periodid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build occ period", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return opList;
    }
    
    
    public OccPeriod getOccPeriod(int periodid) throws IntegrationException{
        OccPeriod op = null;
        String query =  "SELECT periodid, source_sourceid, propertyunit_unitid, createdts, type_typeid, \n" +
                        "       typecertifiedby_userid, typecertifiedts, startdate, startdatecertifiedby_userid, \n" +
                        "       startdatecertifiedts, enddate, enddatecertifiedby_userid, enddatecterifiedts, \n" +
                        "       manager_userid, authorizationts, authorizedby_userid, overrideperiodtypeconfig, \n" +
                        "       notes, createdby_userid\n" +
                        "  FROM public.occperiod WHERE periodid=?;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
 
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, periodid);
            rs = stmt.executeQuery();
            while(rs.next()){
                op = generateOccPeriod(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build occ period", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return op;
    }
    
    
    private OccPeriod generateOccPeriod(ResultSet rs) throws SQLException, IntegrationException{
        ChecklistIntegrator ci = getChecklistIntegrator();
        PersonIntegrator pi = getPersonIntegrator();
        SystemIntegrator si = getSystemIntegrator();
        UserIntegrator ui = getUserIntegrator();
        EventIntegrator ei = getEventIntegrator();
        ChoiceIntegrator choiceInt = getChoiceIntegrator();
        
        OccPeriod op = new OccPeriod();
        
        op.setPeriodID(rs.getInt("periodid"));
        
        op.setCreatedBy(ui.getUser(rs.getInt("createdby_userid")));
               
        op.setSource(si.getBOBSource(rs.getInt("source_sourceid")));
        op.setPropertyUnitID(rs.getInt("propertyunit_unitid"));
        op.setCreatedTS(rs.getTimestamp("createdts").toLocalDateTime());
        
        op.setType(getOccPeriodType(rs.getInt("type_typeid")));
        op.setPeriodTypeCertifiedBy(ui.getUser("typecertifiedby_userid"));
        op.setPeriodTypeCertifiedTS(rs.getTimestamp("typecertifiedts").toLocalDateTime());
        
        op.setStartDate(rs.getTimestamp("startdate").toLocalDateTime());
        op.setStartDateCertifiedBy(ui.getUser(rs.getInt("startdatecertifiedby_userid")));
        op.setStartDateCertifiedTS(rs.getTimestamp("startdatecertifiedts").toLocalDateTime());
        
        op.setEndDate(rs.getTimestamp("enddate").toLocalDateTime());
        op.setEndDateCertifiedBy(ui.getUser(rs.getInt("enddatecertifiedby_userid")));
        op.setEndDateCertifiedTS(rs.getTimestamp("enddatecterifiedts").toLocalDateTime());
        
        op.setManager(ui.getUser(rs.getInt("manager_userid")));
        
        op.setAuthorizedTS(rs.getTimestamp("authorizationts").toLocalDateTime());
        op.setAuthorizedBy(ui.getUser(rs.getInt("authorizedby_userid")));
        
        op.setOverrideTypeConfig(rs.getBoolean("overrideperiodtypeconfig"));
        op.setNotes(rs.getString("notes"));
        
        // now get all the lists from their respective integrators
        // this is the Java version of table joins in SQL; we're doing them interatively
        // in our integrators for each BOB
        op.setApplicationList(getOccPermitApplicationList(op));
        op.setPersonList(pi.getPersonList(op));
        
        op.setEventList(ei.getOccEvents(op.getPeriodID()));
        op.setEventProposalList(choiceInt.getProposalList(op));
        op.setInspectionList(ci.getOccInspectionList(op));

        op.setPermitList(getOccPermitList(op));
        op.setBlobIDList(getBlobList(op));
        return op;
    }
    
    public void updateOccPeriod(OccPeriod op){
        
    }
    
    public OccPermit getOccPermit(int permitID) throws IntegrationException{
        OccPermit op = null;
        String query =  "SELECT permitid, occperiod_periodid, referenceno, issuedto_personid, \n" +
                        "       issuedby_userid, dateissued, permitadditionaltext, notes\n" +
                        "  FROM public.occpermit WHERE permitid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, permitID);
            rs = stmt.executeQuery();
            while(rs.next()){
                op = generateOccPermit(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build property unit list due to an DB integration error", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return op;
        
    }
    
    private OccPermit generateOccPermit(ResultSet rs) throws SQLException, IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        OccPermit permit = new OccPermit();
        PersonIntegrator pi = getPersonIntegrator();
        
        permit.setPermitID(rs.getInt("permitid"));
        permit.setPeriodID(rs.getInt("occperiod_periodid"));
        permit.setReferenceNo(rs.getString("referenceno"));
        
        if(rs.getTimestamp("dateissued") != null){
            permit.setDateIssued(rs.getTimestamp("dateissued").toLocalDateTime());
        }
        
        permit.setIssuedBy(ui.getUser(rs.getInt("issuedby_userid")));
        permit.setIssuedTo(pi.getPerson(rs.getInt("issuedto_personid")));
        
        permit.setPermitAdditionalText(rs.getString("permitadditionaltext"));
        permit.setNotes(rs.getString("notes"));
        
        return permit;
    }
    
    public List<OccPermit> getOccPermitList(OccPeriod period) throws IntegrationException{
        List<OccPermit> permitList = new ArrayList<>();
        String query =  "SELECT permitid FROM public.occpermit WHERE occperiod_periodid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, period.getPeriodID());
            rs = stmt.executeQuery();
            while(rs.next()){
                permitList.add(getOccPermit(rs.getInt("permitid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build occ permit list", ex);
        } finally{
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
    public List<Integer> getBlobList(OccPeriod period) throws IntegrationException{
        List<Integer> blobIDList = new ArrayList<>();
        String query =  "SELECT photodoc_photodocid, occperiod_periodid\n" +
                        "  FROM public.occperiodphotodoc WHERE occperiod_periodid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, period.getPeriodID());
            rs = stmt.executeQuery();
            while(rs.next()){
                blobIDList.add(rs.getInt("photodoc_photodocid"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build occ permit list", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return blobIDList;
        
        
    }
    
    public void updateOccPeriodType(OccPeriodType opt) throws IntegrationException {
        String query = "UPDATE public.occpermittype\n" +
                    "   SET typename=?, typedescription=?\n" +
                    " WHERE typeid=?;";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try{
            stmt = con.prepareStatement(query);
            //stmt.setInt(1, opt.getOccupancyPermitTypeID());
            //stmt.setInt(2, opt.getOccupancyPermitTypeMuniCodeID());
            
            stmt.executeUpdate();
        } catch (SQLException ex){
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update occupancy permit type", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
    }
    
    public void deleteOccPeriodType(OccPeriodType opt) throws IntegrationException{
         String query = "DELETE FROM public.occpermittype\n" +
                        " WHERE typeid=?;";
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

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
    public OccPeriodType getOccPeriodType(int typeid) throws IntegrationException{
        OccPeriodType tpe = null;
        String query =  "SELECT typeid, muni_municode, title, authorizeduses, description, userassignable, \n" +
                        "       permittable, startdaterequired, enddaterequired, passedinspectionrequired, \n" +
                        "       rentalcompatible, active, allowthirdpartyinspection, optionalpersontypes, \n" +
                        "       requiredpersontypes, commercial, requirepersontypeentrycheck, \n" +
                        "       defaultpermitvalidityperioddays, occchecklist_checklistlistid, \n" +
                        "       asynchronousinspectionvalidityperiod, defaultinspectionvalidityperiod, \n" +
                        "       eventruleset_setid\n" +
                        "  FROM public.occperiodtype WHERE typeid=?;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, typeid);
            rs = stmt.executeQuery();
            while(rs.next()){
                tpe = generateOccPeriodType(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build property unit list due to an DB integration error", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return tpe;
    }
    
    public List<OccPeriodType> getOccPeriodTypeList(int muniProfileID){
        return new ArrayList<>();
    }
    
    
    public List<OccPeriodType> getCompleteOccPeriodTypeList(int profileID) throws IntegrationException{
        List<OccPeriodType> occPeriodTypeList = new ArrayList<>();
        String query = "SELECT muniprofile_profileid, occperiodtype_typeid\n" +
                        "  FROM public.muniprofileoccperiodtype WHERE muniprofile_profileid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, profileID);
            rs = stmt.executeQuery();
            while(rs.next()){
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
    
    
    public void insertOccPeriodType(OccPeriodType periodType) throws IntegrationException{
        String query = "INSERT INTO public.occperiodtype(\n" +
                        "            typeid, muni_municode, title, authorizeduses, description, userassignable, \n" +
                        "            permittable, startdaterequired, enddaterequired, passedinspectionrequired, \n" +
                        "            rentalcompatible, active, allowthirdpartyinspection, optionalpersontypes, \n" +
                        "            requiredpersontypes, commercial, requirepersontypeentrycheck, \n" +
                        "            defaultvalidityperioddays)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?);";
    
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try{
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
            
            stmt.execute();
        } catch (SQLException ex){
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert OccupancyPermitType ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
        
    }
    
    
    
    private OccPeriodType generateOccPeriodType(ResultSet rs) throws IntegrationException{
        OccPeriodType opt = new OccPeriodType();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        
        try{
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
            opt.setOptionalpersontypeList(generateOptionalPersonTypes(rs));
            
            opt.setOptionalpersontypeList(generateOptionalPersonTypes(rs));
            opt.setCommercial(rs.getBoolean("commercial"));
            opt.setRequirepersontypeentrycheck(rs.getBoolean("requirepersontypeentrycheck"));
            opt.setDefaultValidityPeriodDays(rs.getInt("defaultvalidityperioddays"));
            
            // wire up when nathan is done
            // opt.setFeeList(fee);
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error generating OccPermitType from ResultSet", ex);
        }
        
         return opt;
    }
    
    public void insertOccPermitApplication(OccPermitApplication application) throws IntegrationException{
        String query = "    INSERT INTO public.occpermitapplication(\n" +
                        "            applicationid, reason_reasonid, submissiontimestamp, submitternotes, \n" +
                        "            internalnotes, propertyunitid, declaredtotaladults, declaredtotalyouth, \n" +
                        "            occperiod_periodid)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?);";
        
        Connection con = null;
        PreparedStatement stmt = null;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            
            stmt.setInt(1, application.getReason().getId());
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(application.getSubmissionDate()));
            stmt.setInt(4, application.getApplicantPerson().getPersonID());
            stmt.setString(5, application.getSubmissionNotes());
            stmt.setString(6, application.getInternalNotes());
            stmt.setString(7, String.valueOf(application.getApplicationPropertyUnit().getUnitID()));
            stmt.execute();
            
        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.insertOccPermitApplication"
                    + "| IntegrationError: unable to insert occupancy permit application ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
    }
    
    
    public int insertOccPermitApplicationAndReturnId(OccPermitApplication application) throws IntegrationException {
                String query = "INSERT INTO public.occupancypermitapplication(applicationid,  "
                    + "reason_reasonid, submissiontimestamp, "
                    + "submitternotes, internalnotes, propertyunitid, "
                    + "person_personid, rental) "
                    + "VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?) "
                    + "RETURNING applicationid;";
        
        Connection con = null;
        PreparedStatement stmt = null;
        int applicationId;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(2, application.getReason().getId());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(application.getSubmissionDate()));
            stmt.setString(4, application.getSubmissionNotes());
            stmt.setString(5, application.getInternalNotes());
            stmt.setString(6, String.valueOf(application.getApplicationPropertyUnit().getUnitID()));
            stmt.setInt(7, application.getApplicantPerson().getPersonID());
            stmt.setBoolean(8, application.getApplicationPropertyUnit().isRental());
            stmt.execute();
            ResultSet inserted_application = stmt.getResultSet();
            inserted_application.next();
            applicationId = inserted_application.getInt(1);
            
        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.insertOccPermitApplicationAndReturnId"
                    + "| IntegrationError: unable to insert occupancy permit application ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }        

        return applicationId;
    }
    
    public List<OccPermitApplicationReason> getOccPermitApplicationReasons() throws IntegrationException{
        
        OccPermitApplicationReason reason = null;
        ArrayList<OccPermitApplicationReason> reasons = new ArrayList<>();
                String query = "SELECT reasonid, reasontitle, reasondescription, activereason, humanfriendlydescription "
                + "FROM public.occpermitapplicationreason "
                + "WHERE activereason = 'true';";
        
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try{
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while(rs.next()){
                reason = generateOccPermitApplicationReason(rs);
                reasons.add(reason);
            }            
            
        } catch(SQLException ex) {
            throw new IntegrationException("OccupancyInspectionIntegrator.getOccPermitApplicationReasons "
                    + "| IntegrationException: Unable to get occupancy permit application reasons ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        return reasons;
    }
    
    public OccPermitApplication getOccPermitApplication(int applicationID) throws IntegrationException {
        OccPermitApplication occpermitapp = null;     
        String query =  "   SELECT applicationid, reason_reasonid, submissiontimestamp, occupancyinspection_id, \n" +
                        "       submitternotes, internalnotes, propertyunitid, declaredtotaladults, \n" +
                        "       declaredtotalyouth\n" +
                        "  FROM public.occpermitapplication WHERE applicationid=?;";
        
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, applicationID);
            rs= stmt.executeQuery();
            
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
    
    private OccPermitApplication generateOccPermitApplication(ResultSet rs) throws IntegrationException {
        OccPermitApplication occpermitapp = new OccPermitApplication();
        PersonIntegrator pi = getPersonIntegrator();
        PropertyIntegrator propint = getPropertyIntegrator();
        try {
            occpermitapp.setId(rs.getInt("applicationid"));
            occpermitapp.setReason(getOccPermitApplicationReason(rs.getInt("reasonid")));
            occpermitapp.setSubmissionDate(rs.getTimestamp("submissiontimestamp").toLocalDateTime());
            occpermitapp.setSubmissionNotes(rs.getString("submitternotes"));
            occpermitapp.setInternalNotes(rs.getString("internalNotes"));
            occpermitapp.setApplicationPropertyUnit(propint.getPropertyUnit(rs.getInt("propertyunitid")));           
      
        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyInspectionIntegrator.generateOccPermitApplication | "
                    + "IntegrationException: Unable to generate occupancy permit application ", ex);
        }
        return occpermitapp;
    }
    
    public List<OccPermitApplication> getOccPermitApplicationList(OccPeriod op) throws IntegrationException{
        List<OccPermitApplication> occpermitappList = new ArrayList<>();
        String query =  "SELECT occpermitapp_applicationid\n" +
                        "  FROM public.occperiodpermitapplication WHERE occperiod_periodid = ?;   ";
        
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, op.getPeriodID());
            rs= stmt.executeQuery();
            
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
    
    public void updateOccPermitApplication(OccPermitApplication application) throws IntegrationException{
        String query = "UPDATE public.occupancypermitapplication"
                + "SET multiunit=?, reason_reasonid=?, submissiontimestamp=?, "
                + "submitternotes=?, internalnotes=?, propertyunitid=?"
                + "WHERE occupancypermitapplication.applicationid = ?;";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(2,application.getReason().getId());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(application.getSubmissionDate()));
            stmt.setString(4,application.getSubmissionNotes());
            stmt.setString(5,application.getInternalNotes());
            stmt.setString(6,String.valueOf(application.getApplicationPropertyUnit().getUnitID()));
            stmt.setInt(7,application.getId());           
            stmt.executeUpdate();
            
        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyInspectionIntegrator.updateOccPermitApplication"
                    + " | IntegrationException: Unable to update occupancy permit application ", ex);
        } finally{
            if (con != null) { try { con.close();} catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
        
    }
    
    public void deleteOccPermitApplication(OccPermitApplication application){
    }
    
    public OccPermitApplicationReason getOccPermitApplicationReason(int reasonId) throws IntegrationException{
        OccPermitApplicationReason occpermitappreason = null;
        
        String query = "SELECT reasonid, reasontitle, reasondescription, activereason, "
                + "humanfriendlydescription\n "
                + "FROM public.occpermitapplicationreason \n"
                + "WHERE reasonid = ?;";
        
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, reasonId);
            rs= stmt.executeQuery();
            
            while (rs.next()) {
                occpermitappreason = generateOccPermitApplicationReason(rs);
            }
            
        } catch(SQLException ex) {
            throw new IntegrationException("OccupancyInspectionIntegrator.getOccPermitApplicationReason | "
                    + "IntegrationException: Unable to get occupancy permit application reason ", ex);            
        } finally{
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        
        return occpermitappreason;
    }
    
    public OccPermitApplicationReason generateOccPermitApplicationReason(ResultSet rs) throws IntegrationException{
        OccPermitApplicationReason occpermitappreason = new OccPermitApplicationReason();        
        
        try {
            occpermitappreason.setId(rs.getInt("reasonid"));
            occpermitappreason.setTitle(rs.getString("reasontitle"));
            occpermitappreason.setDescription(rs.getString("reasondescription"));
            occpermitappreason.setActive(rs.getBoolean("activereason"));
            occpermitappreason.setHumanFriendlyDescription(rs.getString("humanfriendlydescription"));
            occpermitappreason.setPersonsRequirement(getPersonsRequirement(rs.getInt("reasonid")));
        } catch(SQLException ex) {
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
            while (rs.next()){
                personsRequirement = generatePersonsRequirement(rs);
            }
        } catch(SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.getPersonsRequirement | "
                    + "IntegrationError: Unable to get PersonsRequirement ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        
        return personsRequirement;
    }
    
    public OccAppPersonRequirement generatePersonsRequirement(ResultSet rs) throws IntegrationException{
        OccAppPersonRequirement personsRequirement = new OccAppPersonRequirement();
        
        try {
            personsRequirement.setRequirementSatisfied(false);
            personsRequirement.setRequirementExplanation(rs.getString("humanfriendlydescription"));
            personsRequirement.setRequiredPersonTypes(getRequiredPersonTypes(rs.getInt("reasonid")));
            personsRequirement.setOptionalPersonTypes(getOptionalPersonTypes(rs.getInt("reasonid")));
        } catch(SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.generatePersonsRequirement | "
                    + "IntegrationError: Unable to generate PersonsRequirement. ", ex);
        }
        
        return personsRequirement;
    }
    
    public ArrayList<PersonType> getRequiredPersonTypes (int reasonId) throws IntegrationException{
        ArrayList<PersonType> requiredPersonTypes = null;
        String query = "SELECT requiredpersontypes FROM public.occpermitapplicationreason "
                + "WHERE reasonid = ?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, reasonId);
            rs = stmt.executeQuery();            
            while (rs.next()){
                requiredPersonTypes = generateRequiredPersonTypes(rs);
            }            
        } catch(SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.getRequiredPersonTypes | "
                    + "IntegrationError: Unable to get required person types ", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        
        return requiredPersonTypes;
    }
    
    public ArrayList<PersonType> generateRequiredPersonTypes (ResultSet rs) throws IntegrationException{
        ArrayList<PersonType> requiredPersonTypes = new ArrayList<>();
        String[] convertedPersonTypes = null;
        
        try {
            
            Array personTypes = rs.getArray("requiredpersontypes");
            convertedPersonTypes = (String[]) personTypes.getArray();
            
        } catch(SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.generateRequiredPersonTypes | "
                    + "IntegrationError: Unable to generate required person types ", ex);
        }         
        for (String personType:convertedPersonTypes){
            PersonType requiredPersonType = PersonType.valueOf(personType);
            requiredPersonTypes.add(requiredPersonType);
        }         
        return requiredPersonTypes;        
    }
    
    public ArrayList<PersonType> getOptionalPersonTypes (int reasonId) throws IntegrationException{
        ArrayList<PersonType> optionalPersonTypes = null;
        String query = "SELECT optionalpersontypes FROM public.occpermitapplicationreason "
                + "WHERE reasonid = ?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, reasonId);
            rs = stmt.executeQuery();            
            while (rs.next()){
                optionalPersonTypes = generateOptionalPersonTypes(rs);
            }            
        } catch(SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.getOptionalPersonTypes | "
                    + "IntegrationError: Unable to get optional person types. ", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        
        return optionalPersonTypes;
    }
    
    public ArrayList<PersonType> generateOptionalPersonTypes (ResultSet rs) throws IntegrationException{
        ArrayList<PersonType> optionalPersonTypes = new ArrayList<>();
        String[] convertedPersonTypes = null;
        
        try {
            java.sql.Array persTypesSQLArray = rs.getArray("optionalpersontypes");
            if (persTypesSQLArray != null){ 
            convertedPersonTypes = (String[]) persTypesSQLArray.getArray();
            }
            
        } catch(SQLException ex) {
            throw new IntegrationException("OccupancyIntegrator.generateOptionalPersonTypes | "
                    + "IntegrationError: Unable to generate optional person types. ", ex);
        }
        if (convertedPersonTypes != null){
            for (String personType:convertedPersonTypes){
                PersonType optionalPersonType = PersonType.valueOf(personType);
                optionalPersonTypes.add(optionalPersonType);
            }
        }         
        return optionalPersonTypes;        
    }
    
    /**
     * Inserts a person into the occpermitapplicationperson table in the database. The default value
     * for the applicant column is false, and that column will be set to true when the applicant 
     * person is the same as a person within the OccPermitApplication's attachedPersons variable. 
     * The boolean for the preferred contact is set similarly.
     * @param application
     * @throws IntegrationException 
     */
    public void insertOccPeriodPersons (OccPermitApplication application) throws IntegrationException{
        
        String query = "INSERT INTO public.occperiodperson(period_periodid, "
                + "person_personid, applicant, preferredcontact, applicationpersontype)\n"
                + "VALUES (?, ?, ?, ?, CAST (? AS persontype));";                

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        List<Person> applicationPersons = application.getAttachedPersons();
        for (Person person:applicationPersons){           
            try {
                stmt = con.prepareStatement(query);
                stmt.setInt(1, application.getId());
                stmt.setInt(2, person.getPersonID());

                /* If the person at this step of the for-loop is the applicantPerson on the 
                OccPermitApplication, set applicant column to true*/
                if (application.getApplicantPerson() != null && application.getApplicantPerson().equals(person)){                
                    stmt.setBoolean(3, true);
                } else {
                    stmt.setBoolean(3, false);
                }

                /* If the person at this step of the for-loop is the preferredContact on the 
                OccPermitApplication, set preferredcontact column to true */
                if (application.getPreferredContact() != null && application.getPreferredContact().equals(person)) {
                    stmt.setBoolean(4, true);
                } else {
                    stmt.setBoolean(4, false);
                }                
                stmt.setString(5, person.getPersonType().getLabel());
                stmt.execute();
            } catch(SQLException ex) {
                throw new IntegrationException("OccupancyIntegrator.insertOccPermitPersons"
                        + " | IntegrationException: Unable to update occupancy permit application ", ex);
            }
        }   
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
