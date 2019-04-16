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
import com.tcvcog.tcvce.coordinators.ViolationCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CodeViolation;
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
import java.util.List;
import java.util.Set;

/**
 *
 * @author Eric C. Darsow
 */
public class CodeViolationIntegrator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of ViolationIntegrator
     */
    public CodeViolationIntegrator() {
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

    public void insertNoticeOfViolation(CECase c, NoticeOfViolation notice) throws IntegrationException {

        String query = "INSERT INTO public.noticeofviolation(\n"
                + " noticeid, caseid, personid_recipient, lettertext, insertiontimestamp, dateofrecord, \n"
                + " requesttosend, lettersenddate, letterReturnedDate)\n"
                + " VALUES (DEFAULT, ?, ?, ?, now(), ?, \n"
                + " ?, ?, ?);";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, c.getCaseID());
            stmt.setInt(2, notice.getRecipient().getPersonID());
            stmt.setString(3, notice.getNoticeText());
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(notice.getDateOfRecord()));
            stmt.setBoolean(5, notice.isRequestToSend());
            if(notice.getLetterSentDate() == null){
                stmt.setNull(6, java.sql.Types.NULL);
            } else {
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(notice.getLetterSentDate()));
            }
            
            if(notice.getLetterReturnedDate() == null){
                stmt.setNull(7, java.sql.Types.NULL);
            } else {
                stmt.setTimestamp(7, java.sql.Timestamp.valueOf(notice.getLetterReturnedDate()));   
            }

            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert notice of violation letter at this time, sorry.", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    } // close method

    public void updateViolationLetter(NoticeOfViolation notice) throws IntegrationException {
        String query = "UPDATE public.noticeofviolation\n"
                + "   SET personid_recipient=?, lettertext=?,  dateofrecord=?, \n"
                + "   requesttosend=?, lettersenddate=?, letterreturneddate=?\n"
                + " WHERE noticeid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, notice.getRecipient().getPersonID());
            stmt.setString(2, notice.getNoticeText());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(notice.getDateOfRecord()));
            stmt.setBoolean(4, notice.isRequestToSend());
            if (notice.getLetterSentDate() != null) {
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(notice.getLetterSentDate()));
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }

            if (notice.getLetterReturnedDate() != null) {
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(notice.getLetterReturnedDate()));
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
                
            }
            
            stmt.setInt(7, notice.getNoticeID());

            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot update notice of violation letter at this time, sorry.", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }

    public void deleteViolationLetter(NoticeOfViolation notice) throws IntegrationException {
        String query = "DELETE FROM public.noticeofviolation\n"
                + "  WHERE noticeid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, notice.getNoticeID());

            System.out.println("CodeViolationIntegrator.updateNoticeOfViolation| sql: " + stmt.toString());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot update notice of violation letter at this time, sorry.", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }

    public NoticeOfViolation getNoticeOfViolation(int noticeID) throws IntegrationException {
        String query = "SELECT noticeid, caseid, lettertext, insertiontimestamp, dateofrecord, \n" +
                        "       requesttosend, lettersent, lettersenddate, letterreturneddate, \n" +
                        "       personid_recipient\n" +
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
                notice = generateNoticeOfViolation(rs);

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

    public List<NoticeOfViolation> getNoticeOfViolationList(CECase ceCase) throws IntegrationException {
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
                al.add(getNoticeOfViolation(rs.getInt("noticeid")));
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

    private NoticeOfViolation generateNoticeOfViolation(ResultSet rs) throws SQLException, IntegrationException {

        PersonIntegrator pi = getPersonIntegrator();
        NoticeOfViolation notice = new NoticeOfViolation();

        notice.setNoticeID(rs.getInt("noticeid"));
        
        notice.setRecipient(pi.getPerson(rs.getInt("personid_recipient")));

        notice.setNoticeText(rs.getString("lettertext"));

        notice.setInsertionTimeStamp(rs.getTimestamp("insertiontimestamp").toLocalDateTime());
        notice.setInsertionTimeStampPretty(getPrettyDate(rs.getTimestamp("insertiontimestamp").toLocalDateTime()));

        notice.setDateOfRecord(rs.getTimestamp("dateofrecord").toLocalDateTime());
        notice.setDateOfRecordPretty(getPrettyDate(rs.getTimestamp("dateofrecord").toLocalDateTime()));

        notice.setRequestToSend(rs.getBoolean("requesttosend"));
        if (rs.getTimestamp("lettersenddate") != null) {
            notice.setLetterSentDate(rs.getTimestamp("lettersenddate").toLocalDateTime());
            notice.setLetterSentDatePretty(getPrettyDate(rs.getTimestamp("lettersenddate").toLocalDateTime()));
            
        } else {
            notice.setLetterSentDate(null);
        }

        if (rs.getTimestamp("letterreturneddate") != null) {
            notice.setLetterReturnedDate(rs.getTimestamp("letterreturneddate").toLocalDateTime());
            notice.setLetterSentDatePretty(getPrettyDate(rs.getTimestamp("letterreturneddate").toLocalDateTime()));
            
        } else {
            notice.setLetterReturnedDate(null);

        }


        return notice;

    }

    public void updateCodeViolation(CodeViolation v) throws IntegrationException {
        String query = "UPDATE public.codeviolation\n"
                + "   SET codesetelement_elementid=?, cecase_caseid=?, \n"
                + "       dateofrecord=?, stipulatedcompliancedate=?, \n"
                + "       penalty=?, description=?, notes=?,"
                + "       compliancetfevent=? "
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
            if(v.getCompTimeFrameComplianceEvent() != null){
                stmt.setInt(8, v.getCompTimeFrameComplianceEvent().getEventID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            stmt.setInt(9, v.getViolationID());

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
        ViolationCoordinator vc = getViolationCoordinator();
        CodeIntegrator ci = getCodeIntegrator();
        CitationIntegrator citInt = getCitationIntegrator();
        UserIntegrator ui = getUserIntegrator();

        v.setViolationID(rs.getInt("violationid"));
        v.setViolatedEnfElement(ci.getEnforcableCodeElement(rs.getInt("codesetelement_elementid")));
        v.setCeCaseID(rs.getInt("cecase_caseid"));
        v.setDateOfRecord(rs.getTimestamp("dateofrecord").toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());

        v.setEntryTimeStamp(rs.getTimestamp("entrytimestamp").toInstant()
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
        vc.configureCodeViolation(v);
        return v;
    }

    public CodeViolation getCodeViolation(int violationID) throws IntegrationException {
        String query = "SELECT violationid, codesetelement_elementid, cecase_caseid, dateofrecord, \n"
                + "       entrytimestamp, stipulatedcompliancedate, actualcompliancdate, \n"
                + "       penalty, description, notes, legacyimport, compliancetimestamp, \n" +
"       complianceuser, compliancetfevent, severity_classid \n"
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
