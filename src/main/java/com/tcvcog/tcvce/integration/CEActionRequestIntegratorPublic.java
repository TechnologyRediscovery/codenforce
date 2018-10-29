/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.integration;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author sylvia
 */
public class CEActionRequestIntegratorPublic extends BackingBeanUtils implements Serializable {

    
    /**
     * Creates a new instance of CEActionRequestIntegratorPublic
     */
    public CEActionRequestIntegratorPublic() {
    }
    
    
    public CEActionRequest getCEActionRequestByControlCode(int controlCode) throws IntegrationException{
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
        sb.append(" WHERE requestpubliccc = ?;");
        
        // for degugging
        // System.out.println("Select Statement: ");
        // System.out.println(sb.toString());
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, controlCode);
            System.out.println("CEActionRequestorIntegrator.getActionRequestByControlCode | SQL: " + stmt.toString());
            // Retrieve action data from postgres
           rs = stmt.executeQuery();
           
           
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
    
    
    
    
}
