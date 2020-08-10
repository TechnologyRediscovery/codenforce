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
package com.tcvcog.tcvce.integration;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CourtEntity;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Adam Gutonski
 */
public class CourtEntityIntegrator extends BackingBeanUtils implements Serializable {
    
    public CourtEntityIntegrator() {
        
    }
    
    public void updateCourtEntity(CourtEntity courtEntity) throws IntegrationException {
        String query =  "UPDATE public.courtentity\n" +
                        "   SET entityofficialnum=?, jurisdictionlevel=?, name=?, \n" +
                        "       address_street=?, address_city=?, address_zip=?, address_state=?, \n" +
                        "       county=?, phone=?, url=?, notes=?, judgename=?\n" +
                        " WHERE entityid=?;";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        System.out.println("CourtEntityIntegrator.updateCourtEntity | updated name: " + courtEntity.getCourtEntityName());
        
        try{
            stmt = con.prepareStatement(query);
            stmt.setString(1, courtEntity.getCourtEntityOfficialNum());
            stmt.setString(2, courtEntity.getJurisdictionLevel());
            stmt.setString(4, courtEntity.getCourtEntityName());
            
            stmt.setString(5, courtEntity.getAddressStreet());
            stmt.setString(6, courtEntity.getAddressCity());
            stmt.setString(7, courtEntity.getAddressZip());
            stmt.setString(8, courtEntity.getAddressState());
            
            stmt.setString(9, courtEntity.getAddressCounty());
            stmt.setString(10, courtEntity.getPhone());
            stmt.setString(11, courtEntity.getUrl());
            stmt.setString(12, courtEntity.getNotes());
            stmt.setString(13, courtEntity.getJudgeName());
            
            stmt.setInt(14, courtEntity.getCourtEntityID());
            System.out.println("CourtEntityIntegrator.updateCourtEntity | sql: " + stmt.toString());
            stmt.executeUpdate();
        } catch (SQLException ex){
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update Court Entity", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
        
    }
    
    /**
     * Retrieves all court entities
     * @return
     * @throws IntegrationException 
     */
    public List<CourtEntity> getCourtEntityList() throws IntegrationException {
            String query = "SELECT entityid FROM public.courtentity;";
            Connection con = getPostgresCon();
            ResultSet rs = null;
            PreparedStatement stmt = null;
            ArrayList<CourtEntity> ceList = new ArrayList();
        
        try {
            stmt = con.prepareStatement(query);
            System.out.println("");
            rs = stmt.executeQuery();
            while(rs.next()){
                ceList.add(getCourtEntity(rs.getInt("entityid")));
            }
            
            } catch (SQLException ex) {
                System.out.println(ex.toString());
                throw new IntegrationException("cannot generate court entity list", ex);
            } finally {
                if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
                if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
                if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
            }
            return ceList;   
    }
    
    /**
     * Retrieves court entities by municode
     * @param muniCode
     * @return
     * @throws IntegrationException 
     */
       public List<CourtEntity> getCourtEntityList(int muniCode) throws IntegrationException {
            String query = "SELECT courtentity_entityid FROM municourtentity WHERE muni_municode=?;";
            Connection con = getPostgresCon();
            ResultSet rs = null;
            PreparedStatement stmt = null;
            List<CourtEntity> ceList = new ArrayList();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, muniCode);
            rs = stmt.executeQuery();
            while(rs.next()){
                ceList.add(getCourtEntity(rs.getInt("courtentity_entityid")));
            }
            
            } catch (SQLException ex) {
                System.out.println(ex.toString());
                throw new IntegrationException("Cannot generate court entity list", ex);
            } finally {
                if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
                if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
                if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
            }
            if(ceList.isEmpty()){
                ceList = getCourtEntityList();
            }
        
            return ceList;   
    }
    
    
    
    public void insertCourtEntity(CourtEntity courtEntity) throws IntegrationException {
        String query =  "INSERT INTO public.courtentity(\n" +
                        "            entityid, entityofficialnum, jurisdictionlevel, name, address_street, \n" +
                        "            address_city, address_zip, address_state, county, phone, url, \n" +
                        "            notes, judgename)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, ?, ?, \n" +
                        "            ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, courtEntity.getCourtEntityOfficialNum());
            stmt.setString(2, courtEntity.getJurisdictionLevel());
            stmt.setString(4, courtEntity.getCourtEntityName());
            stmt.setString(5, courtEntity.getAddressStreet());
            stmt.setString(6, courtEntity.getAddressCity());
            
            stmt.setString(7, courtEntity.getAddressZip());
            stmt.setString(8, courtEntity.getAddressState());
            stmt.setString(9, courtEntity.getAddressCounty());
            stmt.setString(10, courtEntity.getPhone());
            stmt.setString(11, courtEntity.getUrl());
            
            stmt.setString(12, courtEntity.getNotes());
            stmt.setString(13, courtEntity.getJudgeName());
            
            System.out.println("CourtEntityIntegrator.courtEntityIntegrator | sql: ");
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert Court Entity", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    public CourtEntity getCourtEntity(int entityID) throws IntegrationException{
        
         String query = "SELECT entityid, entityofficialnum, jurisdictionlevel, name, address_street, \n" +
                        "       address_city, address_zip, address_state, county, phone, url, \n" +
                        "       notes, judgename\n" +
                        "  FROM public.courtentity WHERE entityID=?;";
            Connection con = getPostgresCon();
            ResultSet rs = null;
            PreparedStatement stmt = null;
            CourtEntity ce = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, entityID);
            rs = stmt.executeQuery();
            while(rs.next()){
                ce = generateCourtEntity(rs);
            }
            
            } catch (SQLException ex) {
                System.out.println(ex.toString());
                throw new IntegrationException("Cannot generate court entity due to SQL error", ex);
            } catch (IntegrationException ex) {
                throw new IntegrationException("Cannot generate court entity", ex);
        } finally {
                if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
                if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
                if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
            }
            return ce;   
        
        
    }
    
    public void deleteCourtEntity(CourtEntity courtEntity) throws IntegrationException {
        String query = "DELETE FROM public.courtentity\n" +
                        " WHERE entityid= ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, courtEntity.getCourtEntityID());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete court entity--probably because another"
                    + "part of the database has a reference item.", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    private CourtEntity generateCourtEntity(ResultSet rs) throws IntegrationException {
        CourtEntity newCourtEntity = new CourtEntity();
    
        try {
            newCourtEntity.setCourtEntityID(rs.getInt("entityid"));
            newCourtEntity.setCourtEntityOfficialNum(rs.getString("entityofficialnum"));
            newCourtEntity.setJurisdictionLevel(rs.getString("jurisdictionlevel"));
            newCourtEntity.setCourtEntityName(rs.getString("name"));
            newCourtEntity.setAddressStreet(rs.getString("address_street"));
            
            newCourtEntity.setAddressCity(rs.getString("address_city"));
            newCourtEntity.setAddressZip(rs.getString("address_zip"));
            newCourtEntity.setAddressState(rs.getString("address_state"));
            newCourtEntity.setAddressCounty(rs.getString("county"));
            newCourtEntity.setPhone(rs.getString("phone"));
            newCourtEntity.setUrl(rs.getString("url"));
            
            newCourtEntity.setNotes(rs.getString("notes"));
            newCourtEntity.setJudgeName(rs.getString("judgename"));
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error generating Court Entity from result set", ex);
        }
        
        return newCourtEntity;
    }
    
}
