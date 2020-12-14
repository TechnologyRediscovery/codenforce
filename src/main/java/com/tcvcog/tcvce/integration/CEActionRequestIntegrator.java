/*
 * Copyright (C) 2017 ellen bascomb of apt 31y
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
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.MunicipalityCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CEActionRequestIssueType;
import com.tcvcog.tcvce.entities.CEActionRequestStatus;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.PublicInfoBundleCEActionRequest;
import com.tcvcog.tcvce.entities.search.SearchParamsCEActionRequests;
import com.tcvcog.tcvce.util.Constants;
import java.sql.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class CEActionRequestIntegrator extends BackingBeanUtils implements Serializable {

    
    final String ACTIVE_FIELD = "ceactionrequest.active";
    
    
    //Connection integratorConn;
    /**
     * Creates a new instance of CEActionRequestIntegrator
     */
    public CEActionRequestIntegrator() {
    }
    
    /**
     * Attaches a message to the CEActionRequest inside the PublicInfoBundle, 
     * uses PACC to find request
     * @param request
     * @param message
     * @throws IntegrationException 
     */
    public void attachMessageToCEActionRequest(PublicInfoBundleCEActionRequest request, String message) throws IntegrationException {
        String q = "UPDATE public.ceactionrequest\n"
                + "   SET publicexternalnotes = ? WHERE requestpubliccc = ?;";

        // for degugging
        // System.out.println("Select Statement: ");
        // System.out.println(sb.toString());
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(q);
            stmt.setString(1, message);
            stmt.setInt(2, request.getPacc());
            System.out.println("CEActionRequestorIntegrator.attachMessageToCEActionRequest | statement: " + stmt.toString());
            // Retrieve action data from postgres
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("CEActionRequestorIntegrator.attachMessageToCEActionRequest | Integration Error: Unable to retrieve action request", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
    }
    
    public void updateActionRequestNotes(CEActionRequest request) throws IntegrationException {
        String q = "UPDATE public.ceactionrequest "
                + "SET coginternalnotes = ?, "
                + "muniinternalnotes = ?,"
                + "publicexternalnotes = ? "
                + "WHERE requestid = ?;";

        // for degugging
        // System.out.println("Select Statement: ");
        // System.out.println(sb.toString());
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(q);
            stmt.setString(1, request.getCogInternalNotes());
            stmt.setString(2, request.getMuniNotes());
            stmt.setString(3, request.getPublicExternalNotes());
            stmt.setInt(4, request.getRequestID());
            System.out.println("CEActionRequestorIntegrator.attachMessageToCEActionRequest | statement: " + stmt.toString());
            // Retrieve action data from postgres
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("CEActionRequestorIntegrator.updateActionRequest | Integration Error: Unable to retrieve action request", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
    }

    public List<CEActionRequest> getCEActionRequestByControlCode(int controlCode) throws IntegrationException {
        List<CEActionRequest> requestList = new ArrayList<>();
        String q = "SELECT requestid, requestpubliccc, muni_municode, \n"
                + "	property_propertyid, issuetype_issuetypeid, actrequestor_requestorid, submittedtimestamp, \n"
                + "	dateofrecord, addressofconcern, notataddress, \n"
                + "	requestdescription, isurgent, anonymityRequested, \n"
                + "	cecase_caseid, coginternalnotes, status_id, caseattachmenttimestamp, \n"
                + "	muniinternalnotes, publicexternalnotes, paccenabled, caseattachment_userid, active, \n"
                + "	usersubmitter_userid\n"
                + "	FROM public.ceactionrequest \n"
                + " WHERE requestpubliccc= ?;";

        // for degugging
        // System.out.println("Select Statement: ");
        // System.out.println(sb.toString());
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(q);
            stmt.setInt(1, controlCode);
            System.out.println("CEActionRequestorIntegrator.getCEActionRequestByControlCode | SQL: " + stmt.toString());
            // Retrieve action data from postgres
            rs = stmt.executeQuery();

            // loop through the result set and reat an action request from each
            while (rs.next()) {
                requestList.add(generateActionRequestFromRS(rs));

            }
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("CEActionRequestorIntegrator.getCEActionRequestByControlCode | Integration Error: Unable to retrieve action request", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return requestList;
    } // close getActionRequest

    public int submitCEActionRequest(CEActionRequest actionRequest) throws IntegrationException {
        int controlCode;

        StringBuilder qbuilder = new StringBuilder();
        qbuilder.append("INSERT INTO public.ceactionrequest(\n"
                + "            requestid, requestpubliccc, muni_municode, property_propertyid, \n"
                + "            issuetype_issuetypeid, actrequestor_requestorid, cecase_caseid, \n"
                + "            submittedtimestamp, dateofrecord, notataddress, addressofconcern, \n"
                + "            requestdescription, isurgent, anonymityrequested, coginternalnotes, \n"
                + "            muniinternalnotes, publicexternalnotes, status_id, active )\n"
                + "    VALUES (DEFAULT, ?, ?, ?, \n"
                + "            ?, ?, ?, \n"
                + "            now(), ?, ?, ?, \n"
                + "            ?, ?, ?, ?, \n"
                + "            ?, ?, ?, ?);");

        Connection con = null;
        PreparedStatement stmt = null;

        try {
            // start by inserting a person and getting his/her/their new ID
            con = getPostgresCon();
            stmt = con.prepareStatement(qbuilder.toString());

            controlCode = actionRequest.getRequestPublicCC();
            stmt.setInt(1, controlCode);
            stmt.setInt(2, actionRequest.getMuni().getMuniCode());

            if (actionRequest.isIsAtKnownAddress()) {
                stmt.setInt(3, actionRequest.getRequestProperty().getPropertyID());
                actionRequest.setAddressOfConcern(actionRequest.getRequestProperty().getAddress());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }

            stmt.setInt(4, actionRequest.getIssue().getIssueTypeID());
            stmt.setInt(5, actionRequest.getRequestor().getPersonID());
            // case ID is null since the request hasn't been assigned to a case yet
            stmt.setNull(6, java.sql.Types.NULL); // 0 is the int version of null

            // time stamp of entry is created with postegres's now()
            stmt.setTimestamp(7, java.sql.Timestamp.valueOf(actionRequest.getDateOfRecord()));
            stmt.setBoolean(8, actionRequest.isIsAtKnownAddress());
            stmt.setString(9, actionRequest.getAddressOfConcern());
            System.out.println("CEActionRequetIntegrator.submitCEActionRequest | description: " + actionRequest.getRequestDescription());
            stmt.setString(10, actionRequest.getRequestDescription());
            stmt.setBoolean(11, actionRequest.isIsUrgent());
            stmt.setBoolean(12, actionRequest.isAnonymitiyRequested());
            stmt.setString(13, actionRequest.getCogInternalNotes());

            stmt.setString(14, actionRequest.getMuniNotes());
            stmt.setString(15, actionRequest.getPublicExternalNotes());
            stmt.setInt(16, Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("actionRequestInitialStatusCode")));
            stmt.setBoolean(17, actionRequest.isActive());
            

            stmt.execute();
            
            // grab the ID of the most recently inserted CEaction request to send
            // back to the caller, who uses it to look up that request again and
            // display the control code. We only want to display control codes
            // of requests that actually made it into the DB to avoid 
            // the user trying to look up a code for a request that doesn't exist
            String idNumQuery = "SELECT currval('ceactionrequest_requestid_seq');";
            Statement s = con.createStatement();
            ResultSet rs;
            int lastID;
            rs = s.executeQuery(idNumQuery);
            rs.next();
            lastID = rs.getInt(1);
            return lastID;

        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Integration Error: Problem inserting new Code Enforcement Action Request", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }

    private CEActionRequest generateActionRequestFromRS(ResultSet rs) throws SQLException, IntegrationException {

        // create the action request object
        CEActionRequest actionRequest = new CEActionRequest();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        PersonIntegrator pi = getPersonIntegrator();
        PropertyIntegrator propI = getPropertyIntegrator();
        UserIntegrator ui = getUserIntegrator();
        
        actionRequest.setRequestStatus(getRequestStatus(rs.getInt("status_id")));
        actionRequest.setPaccEnabled(rs.getBoolean("paccenabled"));

        actionRequest.setIssue(getRequestIssueType(rs.getInt("issuetype_issuetypeid")));
        
        actionRequest.setRequestID(rs.getInt("requestid"));
        actionRequest.setRequestPublicCC(rs.getInt("requestPubliccc"));
        actionRequest.setMuni(mi.getMuni(rs.getInt("muni_municode")));
        actionRequest.setIsAtKnownAddress(rs.getBoolean("notataddress"));
        actionRequest.setRequestProperty(propI.getProperty(rs.getInt("property_propertyID")));
        actionRequest.setRequestor(pi.getPerson(rs.getInt("actrequestor_requestorid")));

        actionRequest.setSubmittedTimeStamp(rs.getTimestamp("submittedtimestamp").toLocalDateTime());
        actionRequest.setDateOfRecord(rs.getTimestamp("dateofrecord").toLocalDateTime());
        actionRequest.setFormattedSubmittedTimeStamp(getPrettyDate(actionRequest.getDateOfRecord()));

        actionRequest.setDaysSinceDateOfRecord(getDaysSince(actionRequest.getDateOfRecord()));
        actionRequest.setAddressOfConcern(rs.getString("addressofconcern"));

        actionRequest.setRequestDescription(rs.getString("requestDescription"));
        actionRequest.setIsUrgent(rs.getBoolean("isurgent"));

        actionRequest.setCaseID(rs.getInt("cecase_caseid"));
        
        java.sql.Timestamp ts = rs.getTimestamp("caseattachmenttimestamp");
        if(ts != null){
            actionRequest.setCaseAttachmentTimeStamp(ts.toLocalDateTime());
            actionRequest.setCaseAttachmentUser(ui.getUser(rs.getInt("caseattachment_userid")));
        }
        
        actionRequest.setAnonymitiyRequested(rs.getBoolean("anonymityRequested"));

        actionRequest.setCogInternalNotes(rs.getString("coginternalnotes"));

        actionRequest.setMuniNotes(rs.getString("muniinternalnotes"));
        actionRequest.setPublicExternalNotes(rs.getString("publicexternalnotes"));
        actionRequest.setActive(rs.getBoolean("active"));
        
        return actionRequest;
    }

    public void connectActionRequestToCECase(int actionRequestID, int cecaseID, int userid)
            throws BObStatusException, IntegrationException {
        CaseCoordinator cc = getCaseCoordinator();
        CECase cecase = null;
        
        
        try {
            cecase = cc.cecase_getCECase(cecaseID);
        } catch (IntegrationException ex) {
            throw new BObStatusException("Cannot find a CECase to which the action request can be connected");
        }
        if (cecase == null) {
            throw new BObStatusException("Case returned has ID of zero");
        }

        String q = "UPDATE ceactionrequest SET cecase_caseid =?, "
                + "caseattachment_userid=?, caseattachmenttimestamp=now()  "
                + "WHERE requestID =  ?;";

        Connection con = null;
        PreparedStatement stmt = null;

        try {
            // start by inserting a person and getting his/her/their new ID
            con = getPostgresCon();
            stmt = con.prepareStatement(q);
            stmt.setInt(1, cecaseID);
            stmt.setInt(2, userid);
            stmt.setInt(3, actionRequestID);
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Integration Error: Problem connecting action request to cecase", ex);
        } finally {
            
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }

    public CEActionRequest getActionRequestByRequestID(int requestID) throws IntegrationException {
        CEActionRequest newActionRequest = null;
        StringBuilder sb = new StringBuilder();

        
        sb.append("SELECT requestid, requestpubliccc, public.ceactionrequest.muni_municode AS muni_municode, \n"
                + "	property_propertyid, issuetype_issuetypeid, actrequestor_requestorid, submittedtimestamp, \n"
                + "	dateofrecord, addressofconcern, status_id, \n"
                + "	notataddress, requestdescription, isurgent, anonymityRequested, \n"
                + "	cecase_caseid, coginternalnotes, \n"
                + "	muniinternalnotes, publicexternalnotes,\n"
                + "	ceactionrequestissuetype.typeName AS typename, paccenabled, caseattachmenttimestamp, caseattachment_userid, ceactionrequest.active \n"
                + "FROM public.ceactionrequest \n"
                + "     INNER JOIN ceactionrequestissuetype ON ceactionrequest.issuetype_issuetypeid = ceactionrequestissuetype.issuetypeid ");
        sb.append("WHERE requestid = ?;");

//        
//        
//        sb.append("SELECT requestid, requestpubliccc, public.ceactionrequest.muni_municode AS muni_municode, property_propertyid, \n" +
//                "       issuetype_issuetypeid, actrequestor_requestorid, cecase_caseid, \n" +
//                "       submittedtimestamp, dateofrecord, notataddress, addressofconcern, \n" +
//                "       requestdescription, isurgent, anonymityrequested, coginternalnotes, \n" +
//                "       muniinternalnotes, publicexternalnotes, status_id, caseattachmenttimestamp, \n" +
//                "       paccenabled, caseattachment_userid\n"
//                + "	FROM public.ceactionrequest INNER JOIN ceactionrequestissuetype ON ceactionrequest.issuetype_issuetypeid = ceactionrequestissuetype.issuetypeid ");
//        sb.append(" WHERE requestid = ?;");

        // for degugging
        // System.out.println("Select Statement: ");
//         System.out.println(sb.toString());
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, requestID);
            // Retrieve action data from postgres
            rs = stmt.executeQuery();

            // loop through the result set and reat an action request from each
            while (rs.next()) {
                newActionRequest = generateActionRequestFromRS(rs);

            }
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("CEActionRequestorIntegrator.getActionRequest | Integration Error: Unable to retrieve action request", ex);
        } finally {
            
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        newActionRequest = populatePhotodocIDs(newActionRequest);
        
        return newActionRequest;
    } // close getActionRequest

    
    
    private CEActionRequest populatePhotodocIDs(CEActionRequest cear) throws IntegrationException{
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT photodoc_photodocid FROM public.ceactionrequestphotodoc");
        sb.append(" WHERE ceactionrequest_requestid = ?;");
        
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, cear.getRequestID());
            // Retrieve action data from postgres
            rs = stmt.executeQuery();
            // loop through the result set and each to the request
            cear.setBlobIDList(new ArrayList<Integer>());
            while (rs.next()) {
                cear.getBlobIDList().add(rs.getInt("photodoc_photodocid"));
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("CEActionRequestorIntegrator.getActionRequest | Integration Error: Unable to retrieve photos on request", ex);
        } finally {
            
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return cear;
    }
    
    /**
     * Updates the status of the passed in CEActionRequest. 
     * @param req The status of the inputted Request must be that to which you'd
     * like the DB to reflect
     * @throws IntegrationException 
     */
    public void updateActionRequestStatus(CEActionRequest req) throws IntegrationException {

        String q = "UPDATE ceactionrequest SET status_id = ? WHERE requestid = ?;";

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(q);
            stmt.setInt(1, req.getRequestStatus().getStatusID());
            stmt.setInt(2, req.getRequestID());
            System.out.println("CEActionRequestorIntegrator.updateActionRequestStatus | statement: " + stmt.toString());
            // Retrieve action data from postgres
            stmt.executeUpdate();

            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("CEActionRequestorIntegrator.getActionRequest | Integration Error: Unable to update action request", ex);
        } finally {
            
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }
    
    public void updatePACCAccess(CEActionRequest req) throws IntegrationException {

        String q = "UPDATE ceactionrequest SET paccenabled = ? WHERE requestid = ?;";

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(q);
            stmt.setBoolean(1, req.isPaccEnabled());
            stmt.setInt(2, req.getRequestID());
            // Retrieve action data from postgres
            stmt.executeUpdate();

            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("CEActionRequestIntegrator.updatePACCAccess | Integration Error: Unable to update action request", ex);
        } finally {
            
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }
    
    public void updateActionRequestProperty(CEActionRequest req) throws IntegrationException {

        String q = "UPDATE ceactionrequest SET property_propertyid = ? WHERE requestid = ?;";

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(q);
            stmt.setInt(1, req.getRequestProperty().getPropertyID());
            stmt.setInt(2, req.getRequestID());
            System.out.println("CEActionRequestorIntegrator.updateActionRequestProperty | statement: " + stmt.toString());
            // Retrieve action data from postgres
            stmt.executeUpdate();

            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Integration Error: Unable to update action request", ex);
        } finally {
            
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }
    
    
    public void updateActionRequestor(CEActionRequest req) throws IntegrationException {

        String q = "UPDATE ceactionrequest SET actrequestor_requestorid = ? WHERE requestid = ?;";

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(q);
            stmt.setInt(1, req.getRequestor().getPersonID());
            stmt.setInt(2, req.getRequestID());
            System.out.println("CEActionRequestorIntegrator.updateActionRequestor| statement: " + stmt.toString());
            // Retrieve action data from postgres
            stmt.executeUpdate();

            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Integration Error: Unable to update action request", ex);
        } finally {
            
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }

    public CEActionRequestStatus getRequestStatus(int statusID) throws IntegrationException {

        CEActionRequestStatus status = null;
        String query = "SELECT statusid, title, description\n"
                + "  FROM public.ceactionrequeststatus WHERE statusid = ?;";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1,statusID);
            rs = stmt.executeQuery();

            // loop through the result set and reat an action request from each
            while (rs.next()) {
                status = generateCEActionRequestStatus(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("CEActionRequestorIntegrator.getActionRequestByControlCode | Integration Error: Unable to retrieve action request", ex);
        } finally {
            
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return status;
    }

    public CEActionRequestStatus generateCEActionRequestStatus(ResultSet rs) throws IntegrationException {
        CEActionRequestStatus arqs = new CEActionRequestStatus();
        try {
            arqs.setStatusID(rs.getInt("statusid"));
            arqs.setStatusTitle(rs.getString("title"));
            arqs.setDescription(rs.getString("description"));
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Cannot Generate CEActionRequestStatus object", ex);
        }
        return arqs;
    }
    
    
    public List<CEActionRequestStatus> getRequestStatusList() throws IntegrationException {

        List<CEActionRequestStatus> statusList = new ArrayList();
        CEActionRequestStatus status;
        String query = "SELECT statusid, title, description\n"
                + "  FROM public.ceactionrequeststatus;";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();

            // loop through the result set and reat an action request from each
            while (rs.next()) {
                status = generateCEActionRequestStatus(rs);
                statusList.add(status);
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("CEActionRequestorIntegrator.getActionRequestByControlCode | Integration Error: Unable to retrieve action request", ex);
        } finally {
            
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return statusList;
    }
    
    
    
    public CEActionRequestIssueType getRequestIssueType(int issueID) throws IntegrationException {

        CEActionRequestIssueType tpe = null;
        String query = "SELECT issuetypeid, typename, typedescription, muni_municode, notes, \n" +
                        "       intensity_classid, active\n" +
                        "  FROM public.ceactionrequestissuetype\n"
                         + " WHERE issuetypeid = ?;";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, issueID);
            rs = stmt.executeQuery();

            // loop through the result set and reat an action request from each
            while (rs.next()) {
                tpe = generateCEActionRequestIssueType(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("CEActionRequestorIntegrator.getActionRequestByControlCode | Integration Error: Unable to retrieve action request", ex);
        } finally {
            
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return tpe;
    }

    public CEActionRequestIssueType generateCEActionRequestIssueType(ResultSet rs) throws IntegrationException {
        CEActionRequestIssueType tpe = new CEActionRequestIssueType();
        MunicipalityCoordinator mc = getMuniCoordinator();
        SystemIntegrator si = getSystemIntegrator();
        
        try {
            tpe.setIssueTypeID(rs.getInt("issuetypeid"));
            tpe.setName(rs.getString("typename"));
            tpe.setDescription((rs.getString("typedescription")));
            tpe.setMuni(mc.getMuni(rs.getInt("muni_municode")));
            tpe.setNotes(rs.getString("notes"));
            tpe.setIntensityClass(si.getIntensityClass(rs.getInt("intensity_classid")));
            tpe.setActive(rs.getBoolean("active"));
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Cannot Generate CEActionRequestStatus object", ex);
        }
        return tpe;
    }
    
    
    /**
     * Generates a list of CEActionRequestIssueType objects
     * @param muni restrict results to given muni; when null, all issue types are returned
     * @return the list of sub-BObs
     * @throws IntegrationException 
     */
    public List<CEActionRequestIssueType> getRequestIssueTypeList(Municipality muni) throws IntegrationException {

        List<CEActionRequestIssueType> typeList = new ArrayList();
        
        StringBuilder sb = new StringBuilder();
        sb.append( "SELECT issuetypeid, typename, typedescription, muni_municode, notes, intensity_classid, active \n");
        sb.append(" FROM public.ceactionrequestissuetype ");
        if(muni != null){
            sb.append(" WHERE muni_municode = ?;");
        } else {
            sb.append(";");
        }
        
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(sb.toString());
            if(muni != null){
                stmt.setInt(1, muni.getMuniCode());
            } 

            rs = stmt.executeQuery();

            // loop through the result set and reat an action request from each
            while (rs.next()) {
                typeList.add(generateCEActionRequestIssueType(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("CEActionRequestorIntegrator.getActionRequestByControlCode | Integration Error: Unable to retrieve action request", ex);
        } finally {
            
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return typeList;
    }
    
    /**
     * Generates a list of CEActionRequestIssueType objects
     * @param muni restrict results to given muni; when null, all issue types are returned
     * @return the list of sub-BObs
     * @throws IntegrationException 
     */
    public List<CEActionRequestIssueType> getRequestIssueTypeList() throws IntegrationException {

        List<CEActionRequestIssueType> typeList = new ArrayList();
        
        StringBuilder sb = new StringBuilder();
        sb.append( "SELECT issuetypeid, typename, typedescription, muni_municode, notes, intensity_classid, active \n");
        sb.append(" FROM public.ceactionrequestissuetype; ");
        
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(sb.toString());

            rs = stmt.executeQuery();

            // loop through the result set and reat an action request from each
            while (rs.next()) {
                typeList.add(generateCEActionRequestIssueType(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("CEActionRequestorIntegrator.getActionRequestByControlCode | Integration Error: Unable to retrieve action request", ex);
        } finally {
            
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return typeList;
    }
    
    
    
    
    
    
    /**
     * Called by the SearchCoordinator's queryCEAR method
     * implements
     * a multi-stage SQL statement building process based on the settings on the
     * SearchParams object passed into this method.
     *
     * @param params A SearchParamsCEActionRequests object with all of the
     * levers nicely setup for searching
     * 
     * @return a List of all the action requests that meet the search criteria.
     * NOTE that no user will be allowed to search outside of its municipality,
     * including when using a straight ID search.
     * 
     * @throws IntegrationException
     */
    public List<Integer> searchForCEActionRequests(SearchParamsCEActionRequests params) throws IntegrationException {
        SearchCoordinator sc = getSearchCoordinator();
        List<Integer> cearidlst = new ArrayList();
        
        params.appendSQL("SELECT DISTINCT requestid FROM public.ceactionrequest \n");
        params.appendSQL("LEFT OUTER JOIN public.property ON (ceactionrequest.property_propertyid = property.propertyid) \n");
        params.appendSQL("WHERE requestid IS NOT NULL \n"); 

        // ****************************
        // **         OBJECT ID      **
        // ****************************
         if (!params.isBobID_ctl()) {
             
            
            // ***********************************
            // **     MUNI,DATES,USER,ACTIVE    **
            // *********************************** 
           
            params = (SearchParamsCEActionRequests) sc.assembleBObSearchSQL_muniDatesUserActive(
                                                                            params, 
                                                                            SearchParamsCEActionRequests.DBFIELD,
                                                                            ACTIVE_FIELD);
            
            // ****************************
            // **   1.REQUEST STATUS     **
            // **************************** 
            if(params.isRequestStatus_ctl()){
                if(params.getRequestStatus_val() != null){
                    params.appendSQL("AND status_id = ? "); // param 4 without ID search
                } else {
                    params.setRequestStatus_ctl(false);
                    params.appendToParamLog("REQUEST STATUS: found null CEActionRequestStatus; status filter turned off; | ");
                }
            }
            
            // ****************************
            // **   2/ISSUE TYPE         **
            // **************************** 
            if(params.isIssueType_ctl()){
                if(params.getIssueType_val() != null){
                    params.appendSQL("AND issuetype_issuetypeid=? "); // param 4 without ID search
                } else {
                    params.setIssueType_ctl(false);
                    params.appendToParamLog("ISSUE TYPE: found null CEActionRequestIssueType; issue type filter turned off; | ");
                }
            }
            
            // ****************************
            // **   3/ADDRESSABILITY     **
            // **************************** 
            if(params.isNonaddressable_ctl()){
                if(params.isNonaddressable_val()){
                    params.appendSQL("AND notataddress = TRUE ");
                } else {
                    params.appendSQL("AND notataddress = FALSE ");
                }
            } 
            
            // ****************************
            // **       4.URGENCY        **
            // **************************** 
            if(params.isUrgent_ctl()){
                if(params.isUrgent_val()){
                    params.appendSQL("AND isurgent = TRUE ");
                } else {
                    params.appendSQL("AND isurgent = FALSE ");
                }
            } 

            // ****************************
            // **     5/CASE CNXN        **
            // **************************** 
            if (params.isCaseAttachment_ctl()) {
                if (params.isCaseAttachment_val()) {
                    params.appendSQL("AND cecase_caseid IS NOT NULL ");
                } else {
                    params.appendSQL("AND cecase_caseid IS NULL ");
                }
            }
            
            // ****************************
            // **    6/CECASE            **
            // **************************** 
            
            if (params.isCecase_ctl()) {
                if(params.getCecase_val() != null){
                    params.appendSQL("AND cecase_caseid=? ");
                } else {
                    params.setCecase_ctl(false);
                    params.appendToParamLog("CECASE ID: no CECase found; case id filter turned off; | ");
                }
            }
            
            // ****************************
            // **    7/PUBLIC ACCESS     **
            // **************************** 
            if (params.isPacc_ctl()) {
                if(params.isPacc_val()){
                    params.appendSQL("AND paccenabled = TRUE ");
                } else {
                    params.appendSQL("AND paccenabled = FALSE");
                }
            }
                
            // *******************************
            // **   8: REQUESTING PERSON    **
            // ******************************* 
            
            if (params.isRequestorPerson_ctl()) {
                if(params.getRequestorPerson_val() != null){
                    params.appendSQL("AND actrequestor_requestorid=? ");
                } else {
                    params.setRequestorPerson_ctl(false);
                    params.appendToParamLog("REQUESTING PERSON: no Person object found; person filter turned off; | ");
                }
            }
                
            // *******************************
            // **   9: Property             **
            // ******************************* 
            
            if (params.isProperty_ctl()) {
                if(params.getProperty_val() != null){
                    params.appendSQL("AND property.propertyid=? ");
                } else {
                    params.setRequestorPerson_ctl(false);
                    params.appendToParamLog("PROPERTY: no Property object found; filter turned off; | ");
                }
            }

        // ****************************
        // **      OBJECT ID         **
        // **************************** 
        } else {
            params.appendSQL("AND requestid=? "); // will be param 2 with ID search
        }
        params.appendSQL(";");

        ResultSet rs = null;
        PreparedStatement stmt = null;
        Connection con = getPostgresCon();
        int paramCounter = 0;

        try {
            stmt = con.prepareStatement(params.extractRawSQL());
            
            
            // as long as we're not searching by ID only
            if (!params.isBobID_ctl()) {
                 if(params.isMuni_ctl()){
                    stmt.setInt(++paramCounter, params.getMuni_val().getMuniCode());
                 }
                
                 if(params.isDate_startEnd_ctl()){
                    stmt.setTimestamp(++paramCounter, params.getDateStart_val_sql());
                    stmt.setTimestamp(++paramCounter, params.getDateEnd_val_sql());
                 }
                 
                if (params.isUser_ctl()) {
                    stmt.setInt(++paramCounter, params.getUser_val().getUserID());
                }
                
                if(params.isRequestStatus_ctl()){
                    stmt.setInt(++paramCounter, params.getRequestStatus_val().getStatusID());
                }
                
                if(params.isIssueType_ctl()){
                    stmt.setInt(++paramCounter, params.getIssueType_val().getIssueTypeID());
                }
                
                if(params.isCecase_ctl()){
                    stmt.setInt(++paramCounter, params.getCecase_val().getCaseID());
                }
                
                if(params.isRequestorPerson_ctl()){
                    stmt.setInt(++paramCounter, params.getRequestorPerson_val().getPersonID());
                }
                
                if(params.isProperty_ctl()){
                    stmt.setInt(++paramCounter, params.getProperty_val().getPropertyID());
                }
                

            } else {
                stmt.setInt(++paramCounter, params.getBobID_val());
            }

            params.appendToParamLog("CEActionRequestIntegrator SQL before execution: ");
            params.appendToParamLog(stmt.toString());
            
            rs = stmt.executeQuery();
            
            int counter = 0;
            int maxResults;
            if (params.isLimitResultCount_ctl()) {
                maxResults = params.getLimitResultCount_val();
            } else {
                maxResults = Integer.MAX_VALUE;
            }
            while (rs.next() && counter < maxResults) {
                cearidlst.add(rs.getInt("requestid"));
                counter++;
            }


        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Integration Error: Problem retrieving and generating action request list", ex);
        } finally {
            
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }

        }// close try/catch

        return cearidlst;
    } // close method


} // close class
