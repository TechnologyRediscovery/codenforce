/*
 * Copyright (C) 2017 Eric C. Darsow
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
import com.tcvcog.tcvce.entities.CEActionRequest;
import java.sql.*;
import java.io.Serializable;
import java.util.ArrayList;
/**
 *
 * @author Eric C. Darsow
 */
public class CEActionRequestIntegrator extends BackingBeanUtils implements Serializable {

    //Connection integratorConn;
    /**
     * Creates a new instance of CEActionRequestIntegrator
     */
    public CEActionRequestIntegrator() {
    }
    
    public void submitCEActionRequest(CEActionRequest actionRequest) throws IntegrationException{
        int controlCode = 0;
        
        StringBuilder qbuilder = new StringBuilder();
        qbuilder.append("INSERT INTO public.ceactionrequest(\n" +
"            requestid, requestpubliccc, muni_municode, property_propertyid, \n" +
"            issuetype_issuetypeid, actrequestor_requestorid, cecase_caseid, \n" +
"            submittedtimestamp, dateofrecord, notataddress, addressofconcern, \n" +
"            requestdescription, isurgent, anonymityrequested, coginternalnotes, \n" +
"            muniinternalnotes, publicexternalnotes)\n" +
"    VALUES (DEFAULT, ?, ?, ?, \n" +
"            ?, ?, ?, \n" +
"            now(), ?, ?, ?, \n" + 
"            ?, ?, ?, ?, \n" +
"            ?, ?);");
        
        Connection con = null;
        PreparedStatement stmt = null;
        
        try {
            // start by inserting a person and getting his/her/their new ID
            con = getPostgresCon();
            stmt = con.prepareStatement(qbuilder.toString());
            
            controlCode = actionRequest.getRequestPublicCC();
            stmt.setInt(1, controlCode);
            stmt.setInt(2, actionRequest.getMuniCode());
            
            if(actionRequest.isIsAtKnownAddress()){
                stmt.setInt(3, actionRequest.getRequestProperty().getPropertyID());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            stmt.setInt(4, actionRequest.getIssueType_issueTypeID());
            stmt.setInt(5, actionRequest.getPersonID());
            // case ID is null since the request hasn't been assigned to a case yet
            stmt.setNull(6, java.sql.Types.NULL); // 0 is the int version of null
            
            // time stamp of entry is created with postegres's now()
            stmt.setTimestamp(7, java.sql.Timestamp.valueOf(actionRequest.getDateOfRecord()));
            stmt.setBoolean(8, actionRequest.isIsAtKnownAddress());
            stmt.setString(9, actionRequest.getAddressOfConcern());

            stmt.setString(10, actionRequest.getRequestDescription());
            stmt.setBoolean(11, actionRequest.isIsUrgent());
            stmt.setBoolean(12, actionRequest.isAnonymitiyRequested());
            stmt.setString(13, actionRequest.getCogInternalNotes());
            
            stmt.setString(14, actionRequest.getMuniInternalNotes());
            stmt.setString(15, actionRequest.getPublicExternalNotes());
            
            System.out.println("CEActionRequestIntegrator.submitCEActionRequest | sql: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException ex) { 
            System.out.println(ex);
            throw new IntegrationException("Integration Error: Problem inserting new Code Enforcement Action Request", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    } 
    
    private StringBuilder getSelectActionRequestSQLStatement(int requestID){
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("SELECT requestid, requestpubliccc, public.ceactionrequest.muni_municode AS muni_municode, \n" +
"	property_propertyid, issuetype_issuetypeid, actrequestor_requestorid, submittedtimestamp, \n" +
"	dateofrecord, addressofconcern, \n" +
"	notataddress, requestdescription, isurgent, anonymityRequested, \n" +
"	cecase_caseid, coginternalnotes, \n" +
"	muniinternalnotes, publicexternalnotes,\n" +
"	actionRqstIssueType.typeName AS typename\n" +
"	FROM public.ceactionrequest \n" +
"		INNER JOIN actionrqstissuetype ON ceactionrequest.issuetype_issuetypeid = actionRqstIssueType.issuetypeid");
        sb.append(" WHERE requestID = ");
        sb.append(String.valueOf(requestID));
        sb.append(";");
        
        return sb;
        
    } // close method
    
    private CEActionRequest generateActionRequestFromRS(ResultSet rs) throws SQLException, IntegrationException{
            
            // create the action request object
           CEActionRequest actionRequest = new CEActionRequest();
           MunicipalityIntegrator mi = getMunicipalityIntegrator();
           PersonIntegrator pi = getPersonIntegrator();
           PropertyIntegrator propI= getPropertyIntegrator();
            
           actionRequest.setRequestID(rs.getInt("requestid"));
           actionRequest.setRequestPublicCC(rs.getInt("requestPubliccc"));
           actionRequest.setMuni(mi.getMuniFromMuniCode(rs.getInt("muni_municode")));
           actionRequest.setRequestProperty(propI.getProperty(rs.getInt("property_propertyID")));
           actionRequest.setActionRequestorPerson(pi.getPerson(rs.getInt("actrequestor_requestorid")));
           
           actionRequest.setIssueType_issueTypeID(rs.getInt("issuetype_issuetypeid"));
           actionRequest.setIssueTypeString(rs.getString("typename")); // field from joined table
           actionRequest.setSubmittedTimeStamp(rs.getTimestamp("submittedtimestamp").toLocalDateTime());
           actionRequest.setDateOfRecord(rs.getTimestamp("dateofrecord").toLocalDateTime());
           actionRequest.setFormattedSubmittedTimeStamp(getPrettyDate(actionRequest.getDateOfRecord()));
           
           actionRequest.setDaysSinceDateOfRecord(getDaysSince(actionRequest.getDateOfRecord()));
           actionRequest.setAddressOfConcern(rs.getString("addressofconcern"));
           actionRequest.setIsAtKnownAddress(rs.getBoolean("notataddress"));
           
           actionRequest.setRequestDescription(rs.getString("requestDescription"));
           actionRequest.setIsUrgent(rs.getBoolean("isurgent"));
           
           System.out.println("CEActionRequestIntegrator.generateActinRequestFromRS | "
                   + "cecase_caseid from DB with null input: " + rs.getInt("cecase_caseid"));
           actionRequest.setCaseID(rs.getInt("cecase_caseid"));
           actionRequest.setAnonymitiyRequested(rs.getBoolean("anonymityRequested"));
           
           actionRequest.setCogInternalNotes(rs.getString("coginternalnotes"));
           
           actionRequest.setMuniInternalNotes(rs.getString("muniinternalnotes"));
           actionRequest.setPublicExternalNotes(rs.getString("publicexternalnotes"));
           System.out.println("CEActionRequestIntegrator.generateActionRequestFromRS | Generated request: " + actionRequest.getRequestID());
           return actionRequest;
    }
    
    
    public CEActionRequest getActionRequestByRequestID(int requestID) throws IntegrationException{
        CEActionRequest newActionRequest = null;
        
        StringBuilder sb = getSelectActionRequestSQLStatement(requestID);
        
        // for degugging
        // System.out.println("Select Statement: ");
        // System.out.println(sb.toString());
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = getPostgresCon();
            stmt = con.createStatement();
            
            System.out.println("CEActionRequestorIntegrator.getActionRequest | SQL: " + sb.toString());
            // Retrieve action data from postgres
           rs = stmt.executeQuery(sb.toString());
           
           
           // loop through the result set and reat an action request from each
           while(rs.next()){
               newActionRequest = generateActionRequestFromRS(rs);
                System.out.println("CEActionRequestorIntegrator.getActionRequest | Retrieved Request: " + newActionRequest.getRequestID());
         
           }
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("CEActionRequestorIntegrator.getActionRequest | Integration Error: Unable to retrieve action request", ex);
        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        return newActionRequest;
    } // close getActionRequest
    
    public CEActionRequest getActionRequestByControlCode(int controlCode) throws IntegrationException{
        CEActionRequest newActionRequest = null;
        
       StringBuilder sb = new StringBuilder();
        
        sb.append("SELECT requestid, requestpubliccc, public.ceactionrequest.muni_municode AS muni_municode, \n" +
"	property_propertyid, issuetype_issuetypeid, actrequestor_requestorid, submittedtimestamp, \n" +
"	dateofrecord, addressofconcern, \n" +
"	notataddress, requestdescription, isurgent, anonymityRequested, \n" +
"	cecase_caseid, coginternalnotes, \n" +
"	muniinternalnotes, publicexternalnotes,\n" +
"	actionRqstIssueType.typeName AS typename\n" +
"	FROM public.ceactionrequest \n" +
"		INNER JOIN actionrqstissuetype ON ceactionrequest.issuetype_issuetypeid = actionRqstIssueType.issuetypeid");
        sb.append(" WHERE requestpubliccc = ");
        sb.append(String.valueOf(controlCode));
        sb.append(";");
        
        // for degugging
        // System.out.println("Select Statement: ");
        // System.out.println(sb.toString());
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = getPostgresCon();
            stmt = con.createStatement();
            
            System.out.println("CEActionRequestorIntegrator.getActionRequestByControlCode | SQL: " + sb.toString());
            // Retrieve action data from postgres
           rs = stmt.executeQuery(sb.toString());
           
           
           // loop through the result set and reat an action request from each
           while(rs.next()){
               newActionRequest = generateActionRequestFromRS(rs);
                System.out.println("CEActionRequestorIntegrator.getActionRequestByControlCode | Retrieved Request: " + newActionRequest.getRequestID());
         
           }
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("CEActionRequestorIntegrator.getActionRequestByControlCode | Integration Error: Unable to retrieve action request", ex);
        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        return newActionRequest;
    } // close getActionRequest
    
    
    public ArrayList getCEActionRequestList(int muniCode) throws IntegrationException{
        ArrayList<CEActionRequest> requestList = new ArrayList();
        int requestID;
        String query = "SELECT requestid, requestpubliccc FROM public.ceactionrequest "
                + "WHERE muni_municode = ?;"; 
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Connection con = getPostgresCon();
        CEActionRequest cear;
        
         try {
           stmt = con.prepareStatement(query);
           stmt.setInt(1, muniCode);
           rs = stmt.executeQuery();
           while(rs.next()){
               requestID = rs.getInt("requestid");
               cear = getActionRequestByRequestID(requestID);
               requestList.add(cear);
           } // close while
               
         } catch (SQLException ex) {
                System.out.println(ex);
                throw new IntegrationException("Integration Error: Problem retrieving and generating action request list", ex);
        } finally {
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             
        }// close try/catch
         
         return requestList;
    } // close method
    
    
} // close class
