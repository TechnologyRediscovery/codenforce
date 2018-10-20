/*
 * Copyright (C) 2018 Turtle Creek Valley
 * Council of Governments, PA
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
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.Municipality;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.faces.bean.ManagedProperty;

/**
 *
 * @author Eric C. Darsow
 */
public class MunicipalityIntegrator extends BackingBeanUtils implements Serializable {

    // this is a test injected bean -- not currently working as of 19-OCT-18
    @ManagedProperty(value="#{codeIntegrator}")
    private CodeIntegrator ci;
    
    private HashMap municipalityMap;
    /**
     * Creates a new instance of MunicipalityIntegrator
     */
    public MunicipalityIntegrator() {
        
        
    }
    
    public Map<String, Integer> getCaseCountsByPhase(int muniCode) throws IntegrationException{
        
        CasePhase[] phaseValuesArray = new CasePhase[8];
        phaseValuesArray[0] = CasePhase.PrelimInvestigationPending;
        phaseValuesArray[1] = CasePhase.NoticeDelivery;
        phaseValuesArray[2] = CasePhase.InitialComplianceTimeframe;
        phaseValuesArray[3] = CasePhase.SecondaryComplianceTimeframe;
        phaseValuesArray[4] = CasePhase.AwaitingHearingDate;
        phaseValuesArray[5] = CasePhase.HearingPreparation;
        phaseValuesArray[6] = CasePhase.InitialPostHearingComplianceTimeframe;
        phaseValuesArray[7] = CasePhase.SecondaryPostHearingComplianceTimeframe;
        //CasePhase[] phaseValuesArray = CasePhase.values();
        
        
        Map<String, Integer> caseCountMap = new LinkedHashMap<>();
        PreparedStatement stmt = null;
        Connection con = null;
        String query = "SELECT count(caseid) FROM cecase join property "
                + "ON property.propertyid = cecase.property_propertyid "
                + "WHERE property.municipality_municode = ? "
                + "AND casephase = CAST(? AS casephase) ;";
        ResultSet rs = null;
 
        try {
            con = getPostgresCon();
            for(CasePhase c: phaseValuesArray){
                stmt = con.prepareStatement(query);
                stmt.setInt(1, muniCode);
                String phaseString = c.toString();
                stmt.setString(2, phaseString);
                rs = stmt.executeQuery();
                while(rs.next()){
                    caseCountMap.put(phaseString, rs.getInt(1));
                }
            
            }
            
        } catch (SQLException ex) {
            System.out.println("MunicipalityIntegrator.getMuniFromMuniCode | " + ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.getCaseCountsByPhase", ex);
        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return caseCountMap;
        
        
    }
    
    public Municipality getMuniFromMuniCode(int muniCode) throws IntegrationException{
        Municipality muni = null;
        PreparedStatement stmt = null;
        Connection con = null;
        // note that muniCode is not returned in this query since it is specified in the WHERE
        String query = "SELECT municode, muniname, address_street, address_city, "
                + "address_state, address_zip, phone, "
                + "fax, email, managername, "
                + "managerphone, population, activeinprogram, defaultcodeset\n" +
                "FROM public.municipality WHERE municode = ?;";
        ResultSet rs = null;
 
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, muniCode);
            //System.out.println("MunicipalityIntegrator.getMuniFromMuniCode | query: " + stmt.toString());
            rs = stmt.executeQuery();
            while(rs.next()){
                muni = generateMuni(rs);
            }
            
            
        } catch (SQLException ex) {
            System.out.println("MunicipalityIntegrator.getMuniFromMuniCode | " + ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.getMuniFromMuniCode", ex);
        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return muni;
        
    }
    
    public Municipality generateMuni(ResultSet rs) throws SQLException{
                
        CodeIntegrator codeInt = getCodeIntegrator();
        Municipality muni = new Municipality();
        muni.setMuniCode(rs.getInt("municode"));
        muni.setMuniName(rs.getString("muniname"));
        muni.setAddress_street(rs.getString("address_street"));
        muni.setAddress_city(rs.getString("address_city"));

        muni.setAddress_state(rs.getString("address_state"));
        muni.setAddress_zip(rs.getString("address_zip"));
        muni.setPhone(rs.getString("phone"));

        muni.setFax(rs.getString("fax"));
        muni.setEmail(rs.getString("email"));
        muni.setManagerName(rs.getString("managername"));

        muni.setManagerPhone(rs.getString("managerphone"));
        muni.setPopulation(rs.getInt("population"));
        muni.setActiveInProgram(rs.getBoolean("activeinprogram"));             
        muni.setDefaultCodeSetID(rs.getInt("defaultcodeset"));
        
        return muni;
    }
    
    public void generateCompleteMuniNameIDMap() throws IntegrationException{
        HashMap<String, Integer> muniMap = new HashMap<>();
        
        Connection con = getPostgresCon();
        String query = "SELECT muniCode, muniName FROM municipality;";
        ResultSet rs = null;
        Statement stmt = null;
 
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while(rs.next()){
                muniMap.put(rs.getString("muniName"), rs.getInt("muniCode"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.generateCompleteMuniNameIDMap", ex);

        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        municipalityMap = muniMap;
    }
    
    //TODO: finish me
    public void updateMuni(Municipality muni){
        
        
    }
   

    /**
     * @return the municipalityMap
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public HashMap getMunicipalityMap() throws IntegrationException{
        generateCompleteMuniNameIDMap();
        return municipalityMap;
    }

    /**
     * @param municipalityMap the municipalityMap to set
     */
    public void setMunicipalityMap(HashMap municipalityMap) {
        this.municipalityMap = municipalityMap;
    }
    
    public ArrayList<Municipality> getCompleteMuniList() throws IntegrationException{
        ArrayList<Municipality> ll = new ArrayList<>();
        Municipality m;
        Connection con = getPostgresCon();
        String query = "SELECT muniCode, muniName FROM municipality;";
        ResultSet rs = null;
        Statement stmt = null;
 
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while(rs.next()){
                m = getMuniFromMuniCode(rs.getInt("muniCode"));
                System.out.println("MunicipalityIntegrator.getCompleteMuniList | adding muni: " + m.getMuniName());
                ll.add(m);
                
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.generateCompleteMuniNameIDMap", ex);

        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return ll;
    }

    /**
     * @return the ci
     */
    public CodeIntegrator getCi() {
        return ci;
    }

    /**
     * @param ci the ci to set
     */
    public void setCi(CodeIntegrator ci) {
        this.ci = ci;
    }
    
}
