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
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CEActionRequestStatus;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.PublicInfoBundleCEActionRequest;
import com.tcvcog.tcvce.entities.search.SearchParams;
import com.tcvcog.tcvce.entities.search.SearchParamsCEActionRequests;
import com.tcvcog.tcvce.util.Constants;
import java.sql.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public void attachMessageToCEActionRequest(PublicInfoBundleCEActionRequest request, String message) throws IntegrationException {
        String q = "UPDATE public.ceactionrequest\n"
                + "   SET publicexternalnotes = ? WHERE requestid = ?;";

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
            stmt.setInt(2, request.getRequestID());
            System.out.println("CEActionRequestorIntegrator.attachMessageToCEActionRequest | statement: " + stmt.toString());
            // Retrieve action data from postgres
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("CEActionRequestorIntegrator.getActionRequest | Integration Error: Unable to retrieve action request", ex);
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
            throw new IntegrationException("CEActionRequestorIntegrator.getActionRequest | Integration Error: Unable to retrieve action request", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
    }

    public List<CEActionRequest> getCEActionRequestByControlCode(int controlCode) throws IntegrationException {
        CEActionRequest newActionRequest = null;

        List<CEActionRequest> requestList = new ArrayList<>();
        String q = "SELECT requestid, requestpubliccc, public.ceactionrequest.muni_municode AS muni_municode, \n"
                + "	property_propertyid, issuetype_issuetypeid, actrequestor_requestorid, submittedtimestamp, \n"
                + "	dateofrecord, addressofconcern, \n"
                + "	notataddress, requestdescription, isurgent, anonymityRequested, \n"
                + "	cecase_caseid, coginternalnotes, status_id, \n"
                + "	muniinternalnotes, publicexternalnotes,\n"
                + "	actionRqstIssueType.typeName AS typename\n"
                + "	FROM public.ceactionrequest \n"
                + "		INNER JOIN actionrqstissuetype ON ceactionrequest.issuetype_issuetypeid = actionRqstIssueType.issuetypeid"
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
            System.out.println("CEActionRequestorIntegrator.getActionRequestByControlCode | SQL: " + stmt.toString());
            // Retrieve action data from postgres
            rs = stmt.executeQuery();

            // loop through the result set and reat an action request from each
            while (rs.next()) {
                requestList.add(generateActionRequestFromRS(rs));

            }
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("CEActionRequestorIntegrator.getActionRequestByControlCode | Integration Error: Unable to retrieve action request", ex);
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
                + "            muniinternalnotes, publicexternalnotes, status_id )\n"
                + "    VALUES (DEFAULT, ?, ?, ?, \n"
                + "            ?, ?, ?, \n"
                + "            now(), ?, ?, ?, \n"
                + "            ?, ?, ?, ?, \n"
                + "            ?, ?, ?);");

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
            System.out.println("CEActionRequetIntegrator.submitCEActionRequest | description: " + actionRequest.getRequestDescription());
            stmt.setString(10, actionRequest.getRequestDescription());
            stmt.setBoolean(11, actionRequest.isIsUrgent());
            stmt.setBoolean(12, actionRequest.isAnonymitiyRequested());
            stmt.setString(13, actionRequest.getCogInternalNotes());

            stmt.setString(14, actionRequest.getMuniNotes());
            stmt.setString(15, actionRequest.getPublicExternalNotes());
            stmt.setInt(16, Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("actionRequestInitialStatusCode")));

            System.out.println("CEActionRequestIntegrator.submitCEActionRequest | sql: " + stmt.toString());
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

        actionRequest.setRequestID(rs.getInt("requestid"));
        actionRequest.setRequestPublicCC(rs.getInt("requestPubliccc"));
        actionRequest.setMuni(mi.getMuniFromMuniCode(rs.getInt("muni_municode")));
        actionRequest.setIsAtKnownAddress(rs.getBoolean("notataddress"));
        actionRequest.setRequestProperty(propI.getProperty(rs.getInt("property_propertyID")));
        actionRequest.setActionRequestorPerson(pi.getPerson(rs.getInt("actrequestor_requestorid")));

        actionRequest.setIssueType_issueTypeID(rs.getInt("issuetype_issuetypeid"));
        actionRequest.setIssueTypeString(rs.getString("typename")); // field from joined table
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
        }
        actionRequest.setCaseAttachmentUser(ui.getUser(rs.getInt("caseattachment_userid")));
        
        
        actionRequest.setAnonymitiyRequested(rs.getBoolean("anonymityRequested"));

        actionRequest.setCogInternalNotes(rs.getString("coginternalnotes"));

        actionRequest.setMuniNotes(rs.getString("muniinternalnotes"));
        actionRequest.setPublicExternalNotes(rs.getString("publicexternalnotes"));
        return actionRequest;
    }

    public void connectActionRequestToCECase(int actionRequestID, int cecaseID, int userid)
            throws CaseLifecyleException, IntegrationException {
        CECase cecase = null;

        CaseIntegrator ci = getCaseIntegrator();
        try {
            cecase = ci.getCECase(cecaseID);
        } catch (IntegrationException ex) {
            throw new CaseLifecyleException("Cannot find a CECase to which the action request can be connected");
        }
        if (cecase == null) {
            throw new CaseLifecyleException("Case returned has ID of zero");
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
                + "	actionRqstIssueType.typeName AS typename, paccenabled, caseattachmenttimestamp, caseattachment_userid \n"
                + "	FROM public.ceactionrequest \n"
                + "		INNER JOIN actionrqstissuetype ON ceactionrequest.issuetype_issuetypeid = actionRqstIssueType.issuetypeid");
        sb.append(" WHERE requestID = ?;");

//        
//        
//        sb.append("SELECT requestid, requestpubliccc, public.ceactionrequest.muni_municode AS muni_municode, property_propertyid, \n" +
//                "       issuetype_issuetypeid, actrequestor_requestorid, cecase_caseid, \n" +
//                "       submittedtimestamp, dateofrecord, notataddress, addressofconcern, \n" +
//                "       requestdescription, isurgent, anonymityrequested, coginternalnotes, \n" +
//                "       muniinternalnotes, publicexternalnotes, status_id, caseattachmenttimestamp, \n" +
//                "       paccenabled, caseattachment_userid\n"
//                + "	FROM public.ceactionrequest INNER JOIN actionrqstissuetype ON ceactionrequest.issuetype_issuetypeid = actionRqstIssueType.issuetypeid ");
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
            System.out.println("CEActionRequestorIntegrator.getActionRequest | statement: " + stmt.toString());
            // Retrieve action data from postgres
            rs = stmt.executeQuery();

            // loop through the result set and reat an action request from each
            while (rs.next()) {
                newActionRequest = generateActionRequestFromRS(rs);
                System.out.println("CEActionRequestorIntegrator.getActionRequest | Retrieved Request: " + newActionRequest.getRequestID());

            }
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("CEActionRequestorIntegrator.getActionRequest | Integration Error: Unable to retrieve action request", ex);
        } finally {
            
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return newActionRequest;
    } // close getActionRequest

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
            throw new IntegrationException("CEActionRequestorIntegrator.getActionRequest | Integration Error: Unable to update action request", ex);
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
            stmt.setInt(1, req.getActionRequestorPerson().getPersonID());
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

    public ArrayList getCEActionRequestList(int muniCode) throws IntegrationException {
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
            while (rs.next()) {
                requestID = rs.getInt("requestid");
                cear = getActionRequestByRequestID(requestID);
                requestList.add(cear);
            } // close while

        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Integration Error: Problem retrieving and generating action request list", ex);
        } finally {
            
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }

        }// close try/catch

        return requestList;
    } // close method

    /**
     * Primary retrieval method for Code Enforcement Action Requests. Implements
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
    public List<CEActionRequest> getCEActionRequestList(SearchParamsCEActionRequests params) throws IntegrationException {
        List<CEActionRequest> list = new ArrayList();
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT requestid FROM public.ceactionrequest ");
        sb.append("WHERE muni_municode = ? "); // param 1

        // as long as this isn't an ID only search, do the normal SQL building process
         if (!params.isUseRequestID()) {
            sb.append("AND submittedtimestamp BETWEEN ? AND ? "); // parm 2 and 3 without ID
            
            if(params.isUseRequestStatus()){
                sb.append("AND status_id = ? "); // param 4 without ID search
            } // close request status
            
            if(params.isUseNotAtAddress()){
                if(params.isNotAtAnAddress()){
                    sb.append("AND notataddress = TRUE ");
                } else {
                    sb.append("AND notataddress = FALSE ");
                }
            } // close not at address
            
            if(params.isUseMarkedUrgent()){
                if(params.isMarkedUrgent()){
                    sb.append("AND isurgent = TRUE ");
                } else {
                    sb.append("AND isurgent = FALSE ");
                }
            } // close urgent

            if (params.isUseAttachedToCase()) {
                if (params.isAttachedToCase()) {
                    sb.append("AND cecase_caseid IS NOT NULL ");
                } else {
                    sb.append("AND cecase_caseid IS NULL ");
                }
            } // close attached to case

        } else {
            sb.append("AND requestid = ? "); // will be param 2 with ID search
        }
        sb.append(";");

        ResultSet rs = null;
        PreparedStatement stmt = null;
        Connection con = getPostgresCon();
        CEActionRequest cear;

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, params.getMuni().getMuniCode());
            // as long as we're not searching by ID only
            if (!params.isUseRequestID()) {
                stmt.setTimestamp(2, params.getStartDateSQLDate());
                stmt.setTimestamp(3, params.getEndDateSQLDate());
                
                if(params.isUseRequestStatus()){
                    stmt.setInt(4, params.getRequestStatus().getStatusID());
                }

            } else {
                stmt.setInt(2, params.getRequestID());
            }

            System.out.println("CEActionRequestIntegrator.getCEActionRequestList | stmt: " + stmt.toString());
            rs = stmt.executeQuery();
            while (rs.next()) {
                cear = getActionRequestByRequestID(rs.getInt(1));
                list.add(cear);
            } // close while

        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Integration Error: Problem retrieving and generating action request list", ex);
        } finally {
            
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }

        }// close try/catch

        return list;
    } // close method

    public List getCEActionRequestListByCase(int ceCaseID) throws IntegrationException {
        ArrayList<CEActionRequest> requestList = new ArrayList();
        int requestID;
        String query = "SELECT requestid FROM public.ceactionrequest "
                + "WHERE cecase_caseid = ?;";
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Connection con = getPostgresCon();
        CEActionRequest cear;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ceCaseID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                requestID = rs.getInt("requestid");
                cear = getActionRequestByRequestID(requestID);
                requestList.add(cear);
            } // close while

        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Integration Error: Problem retrieving and generating action request list", ex);
        } finally {
            
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }

        }// close try/catch

        return requestList;
    } // close method
    
    
    /**
     * @return the violationMap
     */
    public HashMap<String, Integer> getViolationMap() {
        
        
       HashMap<String, Integer> violationMap = new HashMap<>();
        
        Connection con = getPostgresCon();
        String query = "SELECT issueTypeID, typeName FROM public.actionRqstIssueType;";
        ResultSet rs;
 
        try {
            Statement stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while(rs.next()){
                violationMap.put(rs.getString("typeName"), rs.getInt("issueTypeID"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
        } // end try/catch
        return violationMap;
    }

    

} // close class
