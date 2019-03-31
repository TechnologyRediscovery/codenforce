/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
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
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.occupancy.entities.OccPermit;
import com.tcvcog.tcvce.occupancy.entities.OccPermitApplication;
import com.tcvcog.tcvce.occupancy.entities.OccPermitApplicationReason;
import com.tcvcog.tcvce.occupancy.entities.OccPermitType;
import java.io.Serializable;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Eric C. Darsow
 */
public class OccupancyPermitIntegrator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of OccupancyPermitIntegrator
     */
    public OccupancyPermitIntegrator() {
    }
    
    public OccPermit getOccupancyPermit(int permitID) throws IntegrationException{
        OccPermit op = null;
        
        String query =  "SELECT permitid, referenceno, occinspec_inspectionid, permittype, dateissued, \n" +
                        " dateexpires, issuedunder, specialconditions, notes\n" +
                        " FROM public.occupancypermit WHERE permitid=?;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
 
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, permitID);
            rs = stmt.executeQuery();
            while(rs.next()){
                op = generateOccupancyPermit(rs);
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
    
    public OccPermit generateOccupancyPermit(ResultSet rs) throws SQLException, IntegrationException{
        OccupancyInspectionIntegrator ii = getOccupancyInspectionIntegrator();
        CodeIntegrator ci = getCodeIntegrator();
        
        OccPermit op = new OccPermit();
        
        op.setPermitID(rs.getInt("permitid"));
        op.setReferenceNo(rs.getString("referenceno"));
//        op.setInspection(ii.getOccupancyInspection(rs.getInt("occinspec_inspectionid")));
        op.setDateIssued(rs.getTimestamp("dateissued").toLocalDateTime());
        op.setDateExpires(rs.getTimestamp("dateexpires").toLocalDateTime());
//        op.setIssuingCodeSource(ci.getCodeSource(rs.getInt("issuedunder")));
        op.setSpecialConditions(rs.getString("specialconditions"));
        op.setNotes(rs.getString("notes"));
        
        return op;
    }
    
    public ArrayList<OccPermit> getOccupancyPermitList(PropertyUnit pu){
        return new ArrayList();
    }
    
    public ArrayList<OccPermit> getOccupancyPermitList(Property p) throws IntegrationException{
        
        ArrayList<OccPermit> permitList = new ArrayList();
        String query =  " ";
        
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
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return new ArrayList();
        
    }
    
    
    
    public void updateOccupancyPermitType(OccPermitType opt) throws IntegrationException {
        String query = "UPDATE public.occpermittype\n" +
                    "   SET typename=?, typedescription=?\n" +
                    " WHERE typeid=?;";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try{
            stmt = con.prepareStatement(query);
            //stmt.setInt(1, opt.getOccupancyPermitTypeID());
            //stmt.setInt(2, opt.getOccupancyPermitTypeMuniCodeID());
            stmt.setString(1, opt.getOccupancyPermitTypeName());
            stmt.setString(2, opt.getOccupancyPermitTypeDescription());
            stmt.setInt(3, opt.getOccupancyPermitTypeID());
            stmt.executeUpdate();
        } catch (SQLException ex){
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update occupancy permit type", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
        
    }
    
    public void deleteOccupancyPermitType(OccPermitType opt) throws IntegrationException{
         String query = "DELETE FROM public.occpermittype\n" +
                        " WHERE typeid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, opt.getOccupancyPermitTypeID());
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
    
    
    public ArrayList<OccPermitType> getOccupancyPermitTypeList() throws IntegrationException{
        String query = "SELECT typeid, muni_municode, typename, typedescription\n" +
                       "  FROM public.occpermittype";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<OccPermitType> occupancyPermitTypeList = new ArrayList();
        
        try {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            System.out.println("OccupancyPermitTypeIntegrator.getOccupancyPermitTypeList | SQL: " + stmt.toString());
            while(rs.next()){
                occupancyPermitTypeList.add(generateOccupancyPermitType(rs));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get OccupancyPermitType", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        return occupancyPermitTypeList;        
    }
    
    
    public void insertOccupancyPermitType(OccPermitType occupancyPermitType) throws IntegrationException{
        String query = "INSERT INTO public.occpermittype(\n" +
                    "  typeid, muni_municode, typename, typedescription)\n" +
                    "  VALUES (DEFAULT, ?, ?, ?)";
    
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try{
            stmt = con.prepareStatement(query);
            //stmt.setInt(1, occupancyPermitType.getOccupancyPermitTypeMuniCodeID());
            stmt.setInt(1, occupancyPermitType.getMuni().getMuniCode());
            stmt.setString(2, occupancyPermitType.getOccupancyPermitTypeName());
            stmt.setString(3, occupancyPermitType.getOccupancyPermitTypeDescription());
            System.out.println("OccupancyPermitTypeIntegrator.occupancyPermitTypeIntegrator | sql: " + stmt.toString());
            stmt.execute();
        } catch (SQLException ex){
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert OccupancyPermitType ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
        
    }
    
    
    
    private OccPermitType generateOccupancyPermitType(ResultSet rs) throws IntegrationException{
        OccPermitType newOpt = new OccPermitType();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        
        try{
            newOpt.setOccupancyPermitTypeID(rs.getInt("typeid"));
            newOpt.setMuni(mi.getMuni(rs.getInt("muni_municode")));
            newOpt.setOccupancyPermitTypeName(rs.getString("typename"));
            newOpt.setOccupancyPermitTypeDescription(rs.getString("typedescription"));
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error generating OccPermitType from ResultSet", ex);
        }
        
         return newOpt;
    }
    
    public void insertOccPermitApplication(OccPermitApplication application) throws IntegrationException{
        String query = "INSERT INTO public.occupancypermitapplication(applicationid, multiunit, "
                + "reason_reasonid, submissiontimestamp, currentowner_personid, "
                + "contactperson_personid, newowner_personid, "
                + "submitternotes, internalnotes, propertyunitid) "
                + "VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        
        Connection con = null;
        PreparedStatement stmt = null;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setBoolean(1, application.isMultiUnit());
            stmt.setInt(2, application.getReason().getId());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(application.getSubmissionDate()));
            stmt.setInt(4, application.getCurrentOwner().getPersonID());
            stmt.setInt(5, application.getContactPerson().getPersonID());
            stmt.setInt(6, application.getNewOwner().getPersonID());
            stmt.setString(7, application.getSubmissionNotes());
            stmt.setString(8, application.getInternalNotes());
            stmt.setString(9, String.valueOf(application.getApplicationProperty().getPropertyID()));
            
        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyInspectionIntegraton.insertOccPermitApplication"
                    + "| IntegrationError: unable to insert occupancy permit application ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
    }
    
    public ArrayList<OccPermitApplicationReason> getOccPermitApplicationReasons() throws IntegrationException{
        OccPermitApplicationReason reason = null;
        ArrayList<OccPermitApplicationReason> reasons = new ArrayList<>();
        String query = "SELECT reasonid, reasontitle, reasondescription, activereason "
                + "FROM public.occpermitapplicationreason;";
        
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
        String query = "SELECT applicationid, multiunit, reason_reasonid, submissiontimestamp, "
                + "currentowner_personid, contactperson_personid, "
                + "newowner_personid, occupancyinspection_id, submitternotes, internalnotes, "
                + "propertyunitid\n"
                + "FROM occupancypermitapplication\n"
                + "WHERE occupancypermitapplication.applicationid = ?;";
        
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
            occpermitapp.setMultiUnit(rs.getBoolean("multiunit"));
            occpermitapp.setSubmissionDate(rs.getTimestamp("submissiontimestamp").toLocalDateTime());
            occpermitapp.setCurrentOwner(pi.getPerson(rs.getInt("currentowner_personid")));
            occpermitapp.setContactPerson(pi.getPerson(rs.getInt("contactperson_personid")));
            occpermitapp.setNewOccupants(pi.getOccPermitAppPersons(rs.getInt("applicationid")));
            occpermitapp.setNewOwner(pi.getPerson(rs.getInt("newowner_personid")));
            occpermitapp.setSubmissionNotes(rs.getString("submitternotes"));
            occpermitapp.setInternalNotes(rs.getString("internalNotes"));
//            TODO 2/12/2019: Remove this if Property will be used in OccPermitApplication instead of propertyUnitId as member variable.
//            occpermitapp.setPropertyUnitId(rs.getString("propertyunitid"));
            occpermitapp.setApplicationProperty(propint.getProperty(Integer.parseInt(rs.getString("propertyunitid"))));            
      
        } catch (SQLException ex) {
            throw new IntegrationException("OccupancyInspectionIntegrator.generateOccPermitApplication | "
                    + "IntegrationException: Unable to generate occupancy permit application ", ex);
        }
        return occpermitapp;
    }
    
    public void updateOccPermitApplication(OccPermitApplication application) throws IntegrationException{
        String query = "UPDATE public.occupancypermitapplication"
                + "SET multiunit=?, reason_reasonid=?, submissiontimestamp=?, "
                + "currentowner_personid=?, contactperson_personid=?, "
                + "newowner_personid=?, submitternotes=?, internalnotes=?, propertyunitid=?"
                + "WHERE occupancypermitapplication.applicationid = ?;";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setBoolean(1,application.isMultiUnit());
            stmt.setInt(2,application.getReason().getId());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(application.getSubmissionDate()));
            stmt.setInt(4,application.getCurrentOwner().getPersonID());
            stmt.setInt(5,application.getContactPerson().getPersonID());
            stmt.setInt(6,application.getNewOwner().getPersonID());
            stmt.setString(7,application.getSubmissionNotes());
            stmt.setString(8,application.getInternalNotes());
            stmt.setString(9,String.valueOf(application.getApplicationProperty().getPropertyID()));
            stmt.setInt(10,application.getId());           
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
        // leave blank for now. Do we need? DP 2/4/2019
    }
    
    public OccPermitApplicationReason getOccPermitApplicationReason(int reasonId) throws IntegrationException{
        OccPermitApplicationReason occpermitappreason = null;
        String query = "SELECT reasonid, reasontitle, reasondescription, activereason\n "
                + "FROM public.occpermitapplicationreason\n"
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
        } catch(SQLException ex) {
            throw new IntegrationException("OccupancyPermitIntegrator.generateOccPermitApplicationReason | "
                    + "Integration Error: Unable to generate occupancy permit application reason ", ex);
        }
        
        return occpermitappreason;
    }
    
    public ArrayList<PersonType> getRequiredPersonTypes (OccPermitApplicationReason occPermitApplicationReason) throws IntegrationException{
        ArrayList<PersonType> requiredPersonTypes = null;
        int reasonId = occPermitApplicationReason.getId();
        String query = "SELECT requiredpersontypes FROM public.occpermitapplicationreason "
                + "WHERE reasonid = ?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, reasonId);
            rs = stmt.executeQuery();            
            while (rs.next()){
                requiredPersonTypes = generateRequiredPersonTypes(rs);
            }            
        } catch(SQLException ex) {
            throw new IntegrationException("OccupancyPermitIntegrator.getRequiredPersonTypes | "
                    + "IntegrationError: Unable to get required person types ", ex);
        } finally{
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
            throw new IntegrationException("OccupancyPermitIntegrator.generateRequiredPersonTypes | "
                    + "IntegrationError: Unable to generate required person types ", ex);
        } finally {
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }
        
        for (String personType:convertedPersonTypes){
            PersonType requiredPersonType = PersonType.valueOf(personType);
            requiredPersonTypes.add(requiredPersonType);
        }         
        return requiredPersonTypes;        
    }
    
}
