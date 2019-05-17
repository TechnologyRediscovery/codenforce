/*
 * Copyright (C) 2017 Turtle Creek Valley
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
package com.tcvcog.tcvce.integration;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.CodeViolationDisplayable;
import com.tcvcog.tcvce.entities.EventCECase;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.TextBlock;
import com.tcvcog.tcvce.entities.Municipality;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Connects the postgres database to java land in all directions.
 * @author Ms. L. Darsow
 */
public class ViolationIntegrator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of ViolationIntegrator
     */
    public ViolationIntegrator() {
    }

    public int insertCodeViolation(CodeViolation v) throws IntegrationException {
        int lastID = 0;

        String query = "INSERT INTO public.codeviolation("
                + "            violationid, codesetelement_elementid, cecase_caseid, "
                + "            dateofrecord, entrytimestamp, stipulatedcompliancedate,"
                + "            actualcompliancdate, penalty, description, notes, "
                + "             legacyimport, compliancetimestamp, " 
                + "            complianceuser, compliancetfevent)"
                + "    VALUES (DEFAULT, ?, ?,"
                + "             ?, now(), ?,"
                + "            NULL, ?, ?, ?, ?, NULL, NULL, ? );";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            //stmt.setInt(1, v.getViolationID());
            stmt.setInt(1, v.getViolatedEnfElement().getCodeSetElementID());
            stmt.setInt(2, v.getCeCaseID());
            //stmt.setString(3, v.getCitationID());

            //stmt.setTimestamp(4, java.sql.Timestamp.valueOf(v.getDateOfCitation()));
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(v.getDateOfRecord()));
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(v.getStipulatedComplianceDate()));

            //stmt.setTimestamp(7, java.sql.Timestamp.valueOf(v.getActualComplianceDate()));
            stmt.setDouble(5, v.getPenalty());
            stmt.setString(6, v.getDescription());
            stmt.setString(7, v.getNotes());
            stmt.setBoolean(8, v.isLeagacyImport());
            stmt.setInt(9, v.getComplianceTimeframeEventID());
            stmt.execute();
            
            String idNumQuery = "SELECT currval('codeviolation_violationid_seq');";
            Statement s = con.createStatement();
            ResultSet rs;
            rs = s.executeQuery(idNumQuery);
            rs.next();
            lastID = rs.getInt(1);

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot insert code violation, sorry.", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return lastID;

    }
    
    
    /**
     * Writes a new Notice of Violation to the DB
     * @param c associated CECase
     * @param notice the Notice to write
     * @return the id assigned the new notice by the database
     * @throws IntegrationException 
     */
    public int novInsert(CECase c, NoticeOfViolation notice) throws IntegrationException {

        String query =  "INSERT INTO public.noticeofviolation(\n" +
                        "            noticeid, caseid, lettertextbeforeviolations, creationtimestamp, \n" +
                        "            dateofrecord, sentdate, returneddate, personid_recipient, lettertextafterviolations, \n" +
                        "            lockedandqueuedformailingdate, lockedandqueuedformailingby, sentby, \n" +
                        "            returnedby, notes, creationby, printstyle_styleid)\n" +
                        "    VALUES (DEFAULT, ?, ?, now(), \n" +
                        "            ?, NULL, NULL, ?, ?, \n" +
                        "            NULL, NULL, NULL, \n" +
                        "            NULL, ?, ?, ?);";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int insertedNOVId = 0;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setInt(1, c.getCaseID());
            stmt.setString(2, notice.getNoticeTextBeforeViolations());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(notice.getDateOfRecord()));
            stmt.setInt(4, notice.getRecipient().getPersonID());
            stmt.setString(5, notice.getNoticeTextAfterViolations());
            stmt.setString(6, notice.getNotes());
            stmt.setInt(7, notice.getCreationBy().getUserID());
            if(notice.getStyle() != null){
                stmt.setInt(8, notice.getStyle().getStyleID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
                    
            stmt.execute();
            
            String retrievalQuery = "SELECT currval('noticeofviolation_noticeid_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();
            while(rs.next()){
                insertedNOVId = rs.getInt(1);
            }
            
            notice.setNoticeID(insertedNOVId);
            System.out.println("ViolationIntegrator.novInsert | noticeid " + notice.getNoticeID());
            System.out.println("ViolationIntegrator.novInsert | retrievedid " + insertedNOVId);
            
            List<CodeViolationDisplayable> cvList = notice.getViolationList();
            Iterator<CodeViolationDisplayable> iter = cvList.iterator();
            while(iter.hasNext()){
                CodeViolationDisplayable cvd = iter.next();
                novConnectNoticeToCodeViolation(notice, cvd);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert notice of violation letter at this time, sorry.", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return insertedNOVId;

    } // close method
    
    private void novConnectNoticeToCodeViolation(NoticeOfViolation nov, CodeViolationDisplayable cv) throws IntegrationException{
        String query =  "INSERT INTO public.noticeofviolationcodeviolation(\n" +
                        "            noticeofviolation_noticeid, codeviolation_violationid, includeordtext, \n" +
                        "            includehumanfriendlyordtext, includeviolationphoto)\n" +
                        "    VALUES (?, ?, ?, \n" +
                        "            ?, ?);";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            System.out.println("ViolationIntegrator.novConnectNoticeTOCodeViolation | noticeid " + nov.getNoticeID());
            
            stmt.setInt(1, nov.getNoticeID());
            stmt.setInt(2, cv.getViolationID());
            stmt.setBoolean(3, cv.isIncludeOrdinanceText());
            stmt.setBoolean(4, cv.isIncludeHumanFriendlyText());
            stmt.setBoolean(5, cv.isIncludeViolationPhotos());

            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to connect notice and violation in database", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
    
    public void novUpdateAllNoticeCodeViolations(NoticeOfViolation nov) throws IntegrationException{
            Iterator<CodeViolationDisplayable> iter = nov.getViolationList().iterator();
            CodeViolationDisplayable cvd = null;
            while(iter.hasNext()){
                novUpdateNoticeCodeViolation(iter.next(), nov);
            }
    }
    
    
    private void novUpdateNoticeCodeViolation(CodeViolationDisplayable cv, NoticeOfViolation nov) throws IntegrationException{
        String query =  "UPDATE public.noticeofviolationcodeviolation\n" +
                        "   SET includeordtext=?, includehumanfriendlyordtext=?, includeviolationphoto=? \n" +
                        " WHERE noticeofviolation_noticeid=? AND codeviolation_violationid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setBoolean(1, cv.isIncludeOrdinanceText());
            stmt.setBoolean(2, cv.isIncludeHumanFriendlyText());
            stmt.setBoolean(3, cv.isIncludeViolationPhotos());
            stmt.setInt(4, nov.getNoticeID());
            stmt.setInt(5, cv.getViolationID());

            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update notice violation links", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    private void novClearNoticeCodeViolationConnections(NoticeOfViolation nov) throws IntegrationException{
         String query =     "DELETE FROM public.noticeofviolationcodeviolation\n" +
                            " WHERE noticeofviolation_noticeid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, nov.getNoticeID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to clear connections between NOVs and code violations", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    

    public void novLockAndQueueForMailing(NoticeOfViolation nov) throws IntegrationException{
        String query =  "UPDATE public.noticeofviolation\n" +
                        "   SET lockedandqueuedformailingdate=?, lockedandqueuedformailingby=?, " +
                        "   personid_recipient=?" +
                        "   WHERE noticeid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(nov.getLockedAndqueuedTS()));
            stmt.setInt(2, nov.getLockedAndQueuedBy().getUserID());
            stmt.setInt(3, nov.getRecipient().getPersonID());
            stmt.setInt(4, nov.getNoticeID());

            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to write NOV lock to database", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public void novRecordMailing(NoticeOfViolation nov) throws IntegrationException{
        String query =  "UPDATE public.noticeofviolation\n" +
                        "   SET sentdate=?, sentby=? " +
                        "  WHERE noticeid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(nov.getSentTS()));
            stmt.setInt(2, nov.getSentBy().getUserID());
            stmt.setInt(3, nov.getNoticeID());

            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to record NOV mailing in database", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    public void novRecordReturnedNotice(NoticeOfViolation nov) throws IntegrationException{
        String query =  "UPDATE public.noticeofviolation\n" +
                        "   SET returneddate=? returnedby=? " +
                        "  WHERE noticeid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(nov.getReturnedTS()));
            stmt.setInt(2, nov.getReturnedBy().getUserID());
            stmt.setInt(3, nov.getNoticeID());

            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot record notice as returned in database", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    public void novUpdateNoticeNotes(NoticeOfViolation nov) throws IntegrationException{
        String query =  "UPDATE public.noticeofviolation\n" +
                        "   SET notes=? " +
                        "  WHERE noticeid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setString(1, nov.getNotes());
            stmt.setInt(2, nov.getNoticeID());

            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot record notice as returned in database", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
    public void novUpdate(NoticeOfViolation notice) throws IntegrationException {
        String query = "UPDATE public.noticeofviolation\n"
                + "   SET lettertextbeforeviolations=?, \n" +
                "       dateofrecord=?, personid_recipient=?, \n" +
                "       lettertextafterviolations=?"
                + " WHERE noticeid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setString(1, notice.getNoticeTextBeforeViolations());
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(notice.getDateOfRecord()));
            stmt.setInt(3, notice.getRecipient().getPersonID());
            stmt.setString(4, notice.getNoticeTextAfterViolations());
            stmt.setInt(5, notice.getNoticeID());

            stmt.executeUpdate();
            
            novClearNoticeCodeViolationConnections(notice);
            
            List<CodeViolationDisplayable> cvList = notice.getViolationList();
            Iterator<CodeViolationDisplayable> iter = cvList.iterator();
            while(iter.hasNext()){
                CodeViolationDisplayable cvd = iter.next();
                novConnectNoticeToCodeViolation(notice, cvd);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot update notice of violation letter at this time, sorry.", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }

    public void novDelete(NoticeOfViolation notice) throws IntegrationException {
        String query = "DELETE FROM public.noticeofviolation\n"
                + "  WHERE noticeid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, notice.getNoticeID());

            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot update notice of violation letter at this time, sorry.", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }
    
     public void novResetMailingFieldsToNull(NoticeOfViolation notice) throws IntegrationException {
        String query = "UPDATE public.noticeofviolation\n" +
                        "   SET sentdate=NULL, sentby=NULL, returneddate=NULL, returnedby=NULL,\n" +
                        "       lockedandqueuedformailingdate=NULL, lockedandqueuedformailingby=NULL"
                        + "  WHERE noticeid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, notice.getNoticeID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot clear nov of mailing data.", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }

    public NoticeOfViolation novGet(int noticeID) throws IntegrationException {
        String query =  "SELECT noticeid, caseid, lettertextbeforeviolations, creationtimestamp, \n" +
                        "       dateofrecord, sentdate, returneddate, personid_recipient, lettertextafterviolations, \n" +
                        "       lockedandqueuedformailingdate, lockedandqueuedformailingby, sentby, \n" +
                        "       returnedby, notes, creationby, printstyle_styleid \n" +
                        "  FROM public.noticeofviolation WHERE noticeid = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        NoticeOfViolation notice = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, noticeID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                notice = novGenerate(rs);

            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch code violation by ID, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return notice;
    }

    public List<NoticeOfViolation> novGetList(CECase ceCase) throws IntegrationException {
        String query = "SELECT noticeid FROM public.noticeofviolation WHERE caseid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<NoticeOfViolation> al = new ArrayList();

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ceCase.getCaseID());
            rs = stmt.executeQuery();

            while (rs.next()) {
                al.add(novGet(rs.getInt("noticeid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch code violation by ID, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return al;
    }

    private NoticeOfViolation novGenerate(ResultSet rs) throws SQLException, IntegrationException {
//SELECT noticeid, caseid, lettertextbeforeviolations, creationtimestamp, 
//       dateofrecord, sentdate, returneddate, personid_recipient, lettertextafterviolations, 
//       lockedandqueuedformailingdate, lockedandqueuedformailingby, sentby, 
//       returnedby, notes
//  FROM public.noticeofviolation;

        PersonIntegrator pi = getPersonIntegrator();
        UserIntegrator ui = getUserIntegrator();
        SystemIntegrator si = getSystemIntegrator();
        
        
        // the magical moment of notice instantiation
        NoticeOfViolation notice = new NoticeOfViolation();

        notice.setNoticeID(rs.getInt("noticeid"));
        notice.setRecipient(pi.getPerson(rs.getInt("personid_recipient")));
        notice.setDateOfRecord(rs.getTimestamp("dateofrecord").toLocalDateTime());

        notice.setNoticeTextBeforeViolations(rs.getString("lettertextbeforeviolations"));
        populateCodeViolations(notice);
        notice.setNoticeTextAfterViolations(rs.getString("lettertextafterviolations"));

        notice.setCreationTS(rs.getTimestamp("creationtimestamp").toLocalDateTime());
        notice.setCreationBy(ui.getUser(rs.getInt("creationby")));
        
        if (rs.getTimestamp("lockedandqueuedformailingdate") != null) {
            notice.setLockedAndqueuedTS(rs.getTimestamp("lockedandqueuedformailingdate").toLocalDateTime());
            notice.setLockedAndQueuedBy(ui.getUser(rs.getInt("lockedandqueuedformailingby")));
        }
        
        if (rs.getTimestamp("sentdate") != null) {
            notice.setSentTS(rs.getTimestamp("sentdate").toLocalDateTime());
            notice.setSentBy(ui.getUser(rs.getInt("sentby")));
        } 
        
        if (rs.getTimestamp("returneddate") != null) {
            notice.setReturnedTS(rs.getTimestamp("returneddate").toLocalDateTime());
            notice.setReturnedBy(ui.getUser(rs.getInt("returnedby")));
        } 
        
        notice.setStyle(si.getPrintStyle(rs.getInt("printstyle_styleid")));
        
        notice.setNotes(rs.getString("notes"));

        return notice;

    }
    
    private NoticeOfViolation populateCodeViolations(NoticeOfViolation nov) throws IntegrationException{
        String query =  "  SELECT noticeofviolation_noticeid, codeviolation_violationid, includeordtext, \n" +
                        "       includehumanfriendlyordtext, includeviolationphoto\n" +
                        "  FROM public.noticeofviolationcodeviolation WHERE noticeofviolation_noticeid = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        CodeViolationDisplayable viol;
        List<CodeViolationDisplayable> codeViolationList = new ArrayList<>();

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, nov.getNoticeID() );
            rs = stmt.executeQuery();

            while (rs.next()) {
                viol = new CodeViolationDisplayable(getCodeViolation(rs.getInt("codeviolation_violationid")));
                viol.setIncludeOrdinanceText(rs.getBoolean("includeordtext"));
                viol.setIncludeHumanFriendlyText(rs.getBoolean("includehumanfriendlyordtext"));
                viol.setIncludeViolationPhotos(rs.getBoolean("includeviolationphoto"));
                codeViolationList.add(viol);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot fetch code violation by ID, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        nov.setViolationList(codeViolationList);
        
        return nov;
        
    }
    
    

    public void updateCodeViolation(CodeViolation v) throws IntegrationException {
        String query = "UPDATE public.codeviolation\n"
                + "   SET codesetelement_elementid=?, cecase_caseid=?, \n"
                + "       dateofrecord=?, stipulatedcompliancedate=?, \n"
                + "       penalty=?, description=?, notes=? "
                + " WHERE violationid = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, v.getViolatedEnfElement().getCodeSetElementID());
            stmt.setInt(2, v.getCeCaseID());

            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(v.getDateOfRecord()));
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(v.getStipulatedComplianceDate()));

            stmt.setDouble(5, v.getPenalty());
            stmt.setString((6), v.getDescription());
            stmt.setString(7, v.getNotes());
            stmt.setInt(8, v.getViolationID());

            System.out.println("CodeViolationIntegrator.updateViolation | stmt: " + stmt.toString());

            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch code violation by ID, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }
    public void recordCompliance(CodeViolation v) throws IntegrationException {
        String query = "UPDATE public.codeviolation\n"
                +   "   SET actualcompliancdate=?, compliancetimestamp=now(), \n" 
                +   "       complianceuser=? "
                +   "   WHERE violationid = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(v.getActualComplianceDate()));
            stmt.setInt(2, v.getComplianceUser().getUserID());
            stmt.setInt(3, v.getViolationID());

            System.out.println("CodeViolationIntegrator.recordCompliance");

            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot complete compliance certification.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }

    public void deleteCodeViolation(CodeViolation violationToDelete) throws IntegrationException {
        String query = "DELETE FROM public.codeviolation\n"
                + " WHERE violationid = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, violationToDelete.getViolationID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete code violation--probably because"
                    + "other enetities in the system refer to it. Sorry!", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }

    private CodeViolation generateCodeViolationFromRS(ResultSet rs) throws SQLException, IntegrationException {

        CodeViolation v = new CodeViolation();
        CaseCoordinator cc = getCaseCoordinator();
        CodeIntegrator ci = getCodeIntegrator();
        CitationIntegrator citInt = getCitationIntegrator();
        UserIntegrator ui = getUserIntegrator();

        
        v.setViolationID(rs.getInt("violationid"));
        v.setViolatedEnfElement(ci.getEnforcableCodeElement(rs.getInt("codesetelement_elementid")));
        
        if(rs.getString("createdby") != null){
            v.setCreatedBy(ui.getUser(rs.getString("createdby")));
        }
        
        v.setCeCaseID(rs.getInt("cecase_caseid"));
        v.setDateOfRecord(rs.getTimestamp("dateofrecord").toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());

        v.setCreationTS(rs.getTimestamp("entrytimestamp").toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());

        v.setStipulatedComplianceDate(rs.getTimestamp("stipulatedcompliancedate").toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
                
                
        if (!(rs.getTimestamp("actualcompliancdate") == null)) {
            v.setActualComplianceDate(rs.getTimestamp("actualcompliancdate").toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());

        }

        v.setPenalty(rs.getDouble("penalty"));
        v.setDescription(rs.getString("description"));
        v.setNotes(rs.getString("notes"));
        v.setLeagacyImport(rs.getBoolean("legacyimport"));
        if(rs.getTimestamp("compliancetimestamp") != null){
            v.setComplianceTimeStamp(rs.getTimestamp("compliancetimestamp").toLocalDateTime());
            v.setComplianceUser(ui.getUser("complianceUser"));
            
        }
        
        v.setComplianceTimeframeEventID(rs.getInt("compliancetfevent"));
        
        v.setCitationIDList(citInt.getCitations(v.getViolationID()));
        cc.configureCodeViolation(v);
        return v;
    }

    public CodeViolation getCodeViolation(int violationID) throws IntegrationException {
        String query = "SELECT violationid, codesetelement_elementid, cecase_caseid, dateofrecord, \n"
                + "       entrytimestamp, stipulatedcompliancedate, actualcompliancdate, \n"
                + "       penalty, description, notes, legacyimport, compliancetimestamp, \n" +
"       complianceuser, compliancetfevent, severity_classid, createdby \n"
                + "  FROM public.codeviolation WHERE violationid = ?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        CodeViolation cv = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, violationID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                cv = generateCodeViolationFromRS(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch code violation by ID, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return cv;
    }

    public List<CodeViolation> getCodeViolations(int caseID) throws IntegrationException {
        String query = "SELECT violationid FROM codeviolation WHERE cecase_caseid = ?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<CodeViolation> cvList = new ArrayList();
        CodeViolation cv;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, caseID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                cv = getCodeViolation(rs.getInt("violationid"));
                loadViolationPhotoList(cv);
                cvList.add(cv);

            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot fetch code violation by ID, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return cvList;
       
    }
    

    public List<CodeViolation> getCodeViolations(CECase c) throws IntegrationException {
        return getCodeViolations(c.getCaseID());
    }
    
    public void loadViolationPhotoList(CodeViolation cv) throws IntegrationException{
        ArrayList<Integer> photoList = new ArrayList<>();
        
        String query = "SELECT photodoc_photodocid FROM public.codeviolationphotodoc WHERE codeviolation_violationid = ?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cv.getViolationID());
            rs = stmt.executeQuery();

            while (rs.next()) {
                photoList.add((Integer)rs.getInt(1));
            }
            
            cv.setPhotoList(photoList);

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot load photos on violation.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
    }
    
    
    public HashMap<String, Integer> getTextBlockCategoryMap() throws IntegrationException{
        String query =  "SELECT categoryid, categorytitle\n" +
                        "  FROM public.textblockcategory;";
        HashMap<String, Integer> categoryMap = new HashMap<>();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                categoryMap.put(rs.getString("categorytitle"), rs.getInt("categoryid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot fetch code violation by ID, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return categoryMap;
    }
    
    private TextBlock generateTextBlock(ResultSet rs) throws SQLException, IntegrationException{
        TextBlock tb = new TextBlock();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        
        tb.setBlockID(rs.getInt("blockid"));
        tb.setTextBlockCategoryID(rs.getInt("categoryid"));
        tb.setTextBlockCategoryTitle(rs.getString("categorytitle"));
        tb.setMuni(mi.getMuni(rs.getInt("muni_municode")));
        tb.setTextBlockName(rs.getString("blockname"));
        tb.setTextBlockText(rs.getString("blocktext"));

        return tb;
    }
    
     public TextBlock getTextBlock(int blockID) throws IntegrationException{
         
        String query =  "SELECT blockid, blockcategory_catid, muni_municode, blockname, blocktext, categoryid, categorytitle\n" +
                        "  FROM public.textblock INNER JOIN public.textblockcategory " + 
                        "  ON textblockcategory.categoryid=textblock.blockcategory_catid\n" +
                        "  WHERE blockid=?;";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection con = null;
        TextBlock tb = null;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blockID);
            
            rs = stmt.executeQuery(); 
            
            while(rs.next()){
                tb = generateTextBlock(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Code Violation Integrator: cannot retrive text block by ID", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
          
         return tb;
     }
     
     public ArrayList<TextBlock> getTextBlocks(Municipality m) throws IntegrationException{
        String query =    "  SELECT blockid " +
                            "  FROM public.textblock INNER JOIN public.textblockcategory ON textblockcategory.categoryid=textblock.blockcategory_catid\n" +
                            "  WHERE muni_municode=?;";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection con = null;
        ArrayList<TextBlock> ll = new ArrayList();
        TextBlock tb = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, m.getMuniCode());
            
            rs = stmt.executeQuery(); 
            
            while(rs.next()){
                tb = getTextBlock(rs.getInt("blockid"));
                ll.add(tb);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Code Violation Integrator: cannot retrive text blocks by municipality", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
         return ll;
     }
     
     public List<TextBlock> getAllTextBlocks() throws IntegrationException{
        String query =    "  SELECT blockid, blockcategory_catid, muni_municode, blockname, blocktext, categoryid, categorytitle\n" +
                          "  FROM public.textblock INNER JOIN public.textblockcategory "
                        + "  ON textblockcategory.categoryid=textblock.blockcategory_catid;";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection con = null;
        ArrayList<TextBlock> ll = new ArrayList<>();
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            
            rs = stmt.executeQuery(); 
            
            while(rs.next()){
                ll.add(generateTextBlock(rs));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Code Violation Integrator: cannot retrive all textblocks", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
          
         return ll;
         
     }
     
     public void insertTextBlock(TextBlock tb) throws IntegrationException{
        String query =  "INSERT INTO public.textblock(\n" +
                        " blockid, blockcategory_catid, muni_municode, blockname, blocktext)\n" +
                        " VALUES (DEFAULT, ?, ?, ?, ?);";
        PreparedStatement stmt = null;
        Connection con = null;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, tb.getTextBlockCategoryID());
            stmt.setInt(2, tb.getMuni().getMuniCode());
            stmt.setString(3, tb.getTextBlockName());
            stmt.setString(4, tb.getTextBlockText());
            
            
            stmt.execute(); 
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Text Block Integration Module: cannot insert text block into DB, sorry", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
          
         
         
     }
     
     public void updateTextBlock(TextBlock tb) throws IntegrationException{
        String query = "UPDATE public.textblock\n" +
                        "   SET blockcategory_catid=?, muni_municode=?, blockname=?, \n" +
                        "       blocktext=?\n" +
                        " WHERE blockid=?;";
        PreparedStatement stmt = null;
        Connection con = null;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, tb.getTextBlockCategoryID());
            stmt.setInt(2, tb.getMuni().getMuniCode());
            stmt.setString(3, tb.getTextBlockName());
            stmt.setString(4, tb.getTextBlockText());
            stmt.setInt(5, tb.getBlockID());  
            
            stmt.execute(); 
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Text Block Integration Module: cannot insert text block into DB", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
          
         
     }
     
     public void deleteTextBlock(TextBlock tb) throws IntegrationException{
        String query = " DELETE FROM public.textblock\n" +
                        " WHERE blockid=?;";
        PreparedStatement stmt = null;
        Connection con = null;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, tb.getBlockID());
            
            stmt.execute(); 
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Text Block Integration Module: cannot delete text block into DB, "
                    + "probably because it has been used in a letter somewhere", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
          
     }

} // close integrator
