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
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.occupancy.entities.OccPermit;
import com.tcvcog.tcvce.occupancy.entities.OccPermitType;
import java.io.Serializable;
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
            newOpt.setMuni(mi.getMuniFromMuniCode(rs.getInt("muni_municode")));
            newOpt.setOccupancyPermitTypeName(rs.getString("typename"));
            newOpt.setOccupancyPermitTypeDescription(rs.getString("typedescription"));
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error generating OccPermitType from ResultSet", ex);
        }
        
         return newOpt;
    }
    
}
