/*
 * Copyright (C) 2018 Adam Gutonski 
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
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.occupancy.entities.OccInspection;
import com.tcvcog.tcvce.entities.Fee;
import java.sql.Connection;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * 
 * 
 */
public class OccupancyInspectionIntegrator extends BackingBeanUtils implements Serializable {
    
    public OccupancyInspectionIntegrator(){
        
    }
    
    
    public void insertOccupanyInspection(OccInspection occupancyInspection) throws IntegrationException{
        String query = "INSERT INTO public.occupancyinspection(\n" +
                "   inspectionid, propertyunitid, login_userid, firstinspectiondate, "
            +   "firstinspectionpass, secondinspectiondate, secondinspectionpass, "
            +   "resolved, totalfeepaid, notes) \n" 
            +   "VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
//            stmt.setInt(1, occupancyInspection.getPropertyUnitID());
//            stmt.setInt(2, occupancyInspection.getLoginUserID());
            
            
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert OccupancyInspection", ex);
        
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} } 
        }
                
                
    }
    
    public void updateOccupancyInspection(OccInspection occInspection) throws IntegrationException {
        String query = "UPDATE public.occupancyinspection\n" +
                        "   SET propertyunitid=?, login_userid=?, firstinspectiondate=?, \n" +
                        "       firstinspectionpass=?, secondinspectiondate=?, secondinspectionpass=?, \n" +
                        "       resolved=?, totalfeepaid=?, notes=?\n" +
                        " WHERE inspectionid=?;";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try{
            stmt = con.prepareStatement(query);
//            stmt.setInt(1, occInspection.getPropertyUnitID());
//            stmt.setInt(2, occInspection.getLoginUserID());
            //update first inspection date
           
            stmt.setString(9, occInspection.getNotes());
            stmt.setInt(10, occInspection.getInspectionID());
            System.out.println("TRYING TO EXECUTE UPDATE METHOD");
            stmt.executeUpdate();
        } catch (SQLException ex){
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update occupancy inspection record", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
        
    }
    
    public void deleteOccupancyInspection(OccInspection occInspection) throws IntegrationException{
         String query = "DELETE FROM public.occupancyinspection\n" +
                        " WHERE inspectionid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, occInspection.getInspectionID());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete occupancy inspeciton--probably because another"
                    + "part of the database has a reference item.", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    private OccInspection generateOccupancyInspection(ResultSet rs) throws IntegrationException{
        OccInspection newInspection = new OccInspection();
        
        try{
            newInspection.setInspectionID(rs.getInt("inspectionid"));
//            newInspection.setPropertyUnitID(rs.getInt("propertyunitid"));
//            newInspection.setLoginUserID(rs.getInt("login_userid"));
           
            newInspection.setNotes(rs.getString("notes"));
            
        }catch (SQLException ex){
            System.out.println(ex.toString());
            throw new IntegrationException("Error generating occupancy inspection from ResultSet", ex);
        }
        return newInspection;
    }
    
    public OccInspection getOccupancyInspection(int inspectionID){
        
        return new OccInspection();
    }
    
    public ArrayList<OccInspection> getOccupancyInspectionList(PropertyUnit pu) throws IntegrationException{
        String query = "SELECT inspectionid, propertyunitid, login_userid, firstinspectiondate, \n" +
                    "       firstinspectionpass, secondinspectiondate, secondinspectionpass, \n" +
                    "       resolved, totalfeepaid, notes\n" +
                    "  FROM public.occupancyinspection;";
    Connection con = getPostgresCon();
    ResultSet rs = null;
    PreparedStatement stmt = null;
    ArrayList<OccInspection> occupancyInspectionList = new ArrayList();
    
    try{
        stmt = con.prepareStatement(query);
        rs = stmt.executeQuery();
        System.out.println("OccupancyInspectionIntegrator.getOccupancyInspectionList | SQL: " + stmt.toString());
        
        while(rs.next()){
            occupancyInspectionList.add(generateOccupancyInspection(rs));
        }
    } catch (SQLException ex){
        System.out.println(ex.toString());
        throw new IntegrationException("Cannot get occupancy inspection", ex);
    } finally {
        if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
    }
    return occupancyInspectionList;
    
    }
    
     public void updateOccupancyInspectionFee(Fee oif) throws IntegrationException {
        String query = "UPDATE public.occinspectionfee\n" +
                    "   SET muni_municode=?, feename=?, feeamount=?, effectivedate=?, \n" +
                    "       expirydate=?, notes=? \n" +
                    " WHERE feeid=?;";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try{
            stmt = con.prepareStatement(query);
            stmt.setInt(1, oif.getMuni().getMuniCode());
           
           
            stmt.setInt(7, oif.getOccupancyInspectionFeeID());
            stmt.executeUpdate();
        } catch (SQLException ex){
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update occupancy inspection fee", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
        
    }
    
    
    public ArrayList<Fee> getOccupancyInspectionFeeList() throws IntegrationException {
            String query = "SELECT feeid, muni_municode, feename, feeamount, effectivedate, expirydate, \n" +
                            "       notes\n" +
                            "  FROM public.occinspectionfee";
            Connection con = getPostgresCon();
            ResultSet rs = null;
            PreparedStatement stmt = null;
            ArrayList<Fee> occupancyInspectionFeeList = new ArrayList();
        
        try {
            stmt = con.prepareStatement(query);
            System.out.println("");
            rs = stmt.executeQuery();
            System.out.println("OccupancyInspectionFeeIntegrator.getOccupancyInspectionFeeList | SQL: " + stmt.toString());
            while(rs.next()){
                occupancyInspectionFeeList.add(generateOccupancyInspectionFee(rs));
            }
            
            } catch (SQLException ex) {
                System.out.println(ex.toString());
                throw new IntegrationException("Cannot get Occupancy Inspection Fee List", ex);
            } finally {
                if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
                if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
                if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
            }
            return occupancyInspectionFeeList;   
    }
    
       
    public void insertOccupancyInspectionFee(Fee occupancyInspectionFee) throws IntegrationException {
        String query = "INSERT INTO public.occinspectionfee(\n" +
                        "            feeid, muni_municode, feename, feeamount, effectivedate, expirydate, \n" +
                        "            notes)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, ?, \n" +
                        "            ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, occupancyInspectionFee.getMuni().getMuniCode());
            
            System.out.println("OccupancyInspectionFeeIntegrator.occupancyInspectionFeeIntegrator | sql: " + stmt.toString());
            System.out.println("TRYING TO EXECUTE INSERT METHOD");
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert Occupancy Inspection Fee", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    public void deleteOccupancyInspectionFee(Fee oif) throws IntegrationException{
         String query = "DELETE FROM public.occinspectionfee\n" +
                " WHERE feeid= ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, oif.getOccupancyInspectionFeeID());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete occupancy inspeciton fee--probably because another"
                    + "part of the database has a reference item.", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
    private Fee generateOccupancyInspectionFee(ResultSet rs) throws IntegrationException {
        Fee newOif = new Fee();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
    
        try {
            newOif.setOccupancyInspectionFeeID(rs.getInt("feeid"));
            newOif.setMuni(mi.getMuni(rs.getInt("muni_municode")));
           
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error generation OccInspectionFee from result set", ex);
        }
        
        return newOif;
    }
    
}
