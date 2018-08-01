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

import java.sql.*;
import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.occupancy.entities.OccupancyInspection;
import com.tcvcog.tcvce.occupancy.entities.OccInspecFee;
import com.tcvcog.tcvce.occupancy.entities.OccInspecStatus;
import com.tcvcog.tcvce.occupancy.entities.OccPermitApplication;
import com.tcvcog.tcvce.occupancy.entities.OccPermitApplicationReason;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * This integrator creates our PostGres connection for DB manipulation
 * @author Adam Gutonski
 */
public class OccupancyInspectionIntegrator extends BackingBeanUtils implements Serializable {
    
    public OccupancyInspectionIntegrator(){
        
    }
    
    public void insertOccPermitApplication(OccPermitApplication application){
        
    }
    
    public ArrayList<OccPermitApplicationReason> getOccPermitApplicationReasons(){
        // temp to close
        return new ArrayList();
    }
    
    public OccPermitApplication getOccPermitApplication(int applicationID){
        return new OccPermitApplication();
    }
    
    private OccPermitApplication generateApplication(ResultSet rs){
        OccPermitApplication app = new OccPermitApplication();
        
        return app;
    }
    
    public void updateOccPermitApplication(OccPermitApplication application){
        
    }
    
    public void deleteOccPermitApplication(OccPermitApplication application){
        
    }
    
    public ArrayList<OccInspecStatus> getOccInspecStatusList(){
        return new ArrayList();
    }
    
    public void updateOccInspecStatus(OccupancyInspection oi, OccInspecStatus updatedStatus){
        
        
    }
    
    public void insertOccupanyInspection(OccupancyInspection occupancyInspection) throws IntegrationException{
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
            if(occupancyInspection.getFirstInspectionDate() != null){
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(occupancyInspection.getFirstInspectionDate()));
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            stmt.setBoolean(4, occupancyInspection.isFirstInspectionPass());
            if(occupancyInspection.getSecondInspectionDate() != null){
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(occupancyInspection.getSecondInspectionDate()));
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            stmt.setBoolean(6, occupancyInspection.isSecondInspectionPass());
            stmt.setBoolean(8, occupancyInspection.isTotalFeePaid());
            stmt.setString(9, occupancyInspection.getOccupancyInspectionNotes());
            
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert OccupancyInspection", ex);
        
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} } 
        }
                
                
    }
    
    public void updateOccupancyInspection(OccupancyInspection occInspection) throws IntegrationException {
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
            if(occInspection.getFirstInspectionDate() != null){
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(occInspection.getFirstInspectionDate()));
                
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            stmt.setBoolean(4, occInspection.isFirstInspectionPass());
            //update second inspection date
            if(occInspection.getSecondInspectionDate() != null){
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(occInspection.getSecondInspectionDate()));
                
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            stmt.setBoolean(6, occInspection.isSecondInspectionPass());
            stmt.setBoolean(8, occInspection.isTotalFeePaid());
            stmt.setString(9, occInspection.getOccupancyInspectionNotes());
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
    
    public void deleteOccupancyInspection(OccupancyInspection occInspection) throws IntegrationException{
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
    
    private OccupancyInspection generateOccupancyInspection(ResultSet rs) throws IntegrationException{
        OccupancyInspection newInspection = new OccupancyInspection();
        
        try{
            newInspection.setInspectionID(rs.getInt("inspectionid"));
//            newInspection.setPropertyUnitID(rs.getInt("propertyunitid"));
//            newInspection.setLoginUserID(rs.getInt("login_userid"));
            java.sql.Timestamp stamp = rs.getTimestamp("firstinspectiondate");
            if(stamp != null){
                newInspection.setFirstInspectionDate(stamp.toLocalDateTime());
            } else {
                newInspection.setFirstInspectionDate(null);
            }
            newInspection.setFirstInspectionPass(rs.getBoolean("firstinspectionpass"));
            java.sql.Timestamp t = rs.getTimestamp("secondinspectiondate");
            if(t != null){
                newInspection.setSecondInspectionDate(t.toLocalDateTime());
            } else {
                newInspection.setSecondInspectionDate(null);
            }
            newInspection.setTotalFeePaid(rs.getBoolean("totalfeepaid"));
            newInspection.setOccupancyInspectionNotes(rs.getString("notes"));
            
        }catch (SQLException ex){
            System.out.println(ex.toString());
            throw new IntegrationException("Error generating occupancy inspection from ResultSet", ex);
        }
        return newInspection;
    }
    
    public OccupancyInspection getOccupancyInspection(int inspectionID){
        
        return new OccupancyInspection();
    }
    
    public ArrayList<OccupancyInspection> getOccupancyInspectionList(PropertyUnit pu) throws IntegrationException{
        String query = "SELECT inspectionid, propertyunitid, login_userid, firstinspectiondate, \n" +
                    "       firstinspectionpass, secondinspectiondate, secondinspectionpass, \n" +
                    "       resolved, totalfeepaid, notes\n" +
                    "  FROM public.occupancyinspection;";
    Connection con = getPostgresCon();
    ResultSet rs = null;
    PreparedStatement stmt = null;
    ArrayList<OccupancyInspection> occupancyInspectionList = new ArrayList();
    
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
    
     public void updateOccupancyInspectionFee(OccInspecFee oif) throws IntegrationException {
        String query = "UPDATE public.occinspectionfee\n" +
                    "   SET muni_municode=?, feename=?, feeamount=?, effectivedate=?, \n" +
                    "       expirydate=?, notes=? \n" +
                    " WHERE feeid=?;";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try{
            stmt = con.prepareStatement(query);
            stmt.setInt(1, oif.getMuni().getMuniCode());
            stmt.setString(2, oif.getOccupancyInspectionFeeName());
            stmt.setDouble(3, oif.getOccupancyInspectionFeeAmount());
            //update effective date
            if(oif.getOccupancyInspectionFeeEffDate() != null){
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(oif.getOccupancyInspectionFeeEffDate()));
                
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            //update expiry date
            if(oif.getOccupancyInspectionFeeExpDate() != null){
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(oif.getOccupancyInspectionFeeExpDate()));
                
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            stmt.setString(6, oif.getOccupancyInspectionFeeNotes());
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
    
    
    public ArrayList<OccInspecFee> getOccupancyInspectionFeeList() throws IntegrationException {
            String query = "SELECT feeid, muni_municode, feename, feeamount, effectivedate, expirydate, \n" +
                            "       notes\n" +
                            "  FROM public.occinspectionfee";
            Connection con = getPostgresCon();
            ResultSet rs = null;
            PreparedStatement stmt = null;
            ArrayList<OccInspecFee> occupancyInspectionFeeList = new ArrayList();
        
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
    
       
    public void insertOccupancyInspectionFee(OccInspecFee occupancyInspectionFee) throws IntegrationException {
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
            stmt.setString(2, occupancyInspectionFee.getOccupancyInspectionFeeName());
            stmt.setDouble(3, occupancyInspectionFee.getOccupancyInspectionFeeAmount());
            if(occupancyInspectionFee.getOccupancyInspectionFeeEffDate() != null){
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(occupancyInspectionFee.getOccupancyInspectionFeeEffDate()));
                
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            if(occupancyInspectionFee.getOccupancyInspectionFeeExpDate() != null){
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(occupancyInspectionFee.getOccupancyInspectionFeeExpDate()));
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            stmt.setString(6, occupancyInspectionFee.getOccupancyInspectionFeeNotes());
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
    
    public void deleteOccupancyInspectionFee(OccInspecFee oif) throws IntegrationException{
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
    
    
    private OccInspecFee generateOccupancyInspectionFee(ResultSet rs) throws IntegrationException {
        OccInspecFee newOif = new OccInspecFee();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
    
        try {
            newOif.setOccupancyInspectionFeeID(rs.getInt("feeid"));
            newOif.setMuni(mi.getMuniFromMuniCode(rs.getInt("muni_municode")));
            newOif.setOccupancyInspectionFeeName(rs.getString("feename"));
            newOif.setOccupancyInspectionFeeAmount(rs.getDouble("feeamount"));
            java.sql.Timestamp eff = rs.getTimestamp("effectivedate");
            //for effective date
            if(eff != null) {
                newOif.setOccupancyInspectionFeeEffDate(eff.toLocalDateTime());
            } else {
                newOif.setOccupancyInspectionFeeEffDate(null);
            }
            java.sql.Timestamp exp = rs.getTimestamp("expirydate");
            //for expiration date
            if(exp != null) {
                newOif.setOccupancyInspectionFeeExpDate(exp.toLocalDateTime());
            } else {
                newOif.setOccupancyInspectionFeeExpDate(null);
            }
            newOif.setOccupancyInspectionFeeNotes(rs.getString("notes"));
            
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error generation OccInspectionFee from result set", ex);
        }
        
        return newOif;
    }
    
}
