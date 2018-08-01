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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.TextBlock;
import com.tcvcog.tcvce.entities.Municipality;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.HashMap;

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

    public void insertCodeViolation(CodeViolation v) throws IntegrationException {

        String query = "INSERT INTO public.codeviolation(\n"
                + "            violationid, codesetelement_elementid, cecase_caseid, \n"
                + "            dateofrecord, entrytimestamp, stipulatedcompliancedate, \n"
                + "            actualcompliancdate, penalty, description, notes)\n"
                + "    VALUES (DEFAULT, ?, ?, \n"
                + "             ?, now(), ?, \n"
                + "            NULL, ?, ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            //stmt.setInt(1, v.getViolationID());
            stmt.setInt(1, v.getViolatedEnfElement().getCodeSetElementID());
            stmt.setInt(2, v.getAttachedCase().getCaseID());
            //stmt.setString(3, v.getCitationID());

            //stmt.setTimestamp(4, java.sql.Timestamp.valueOf(v.getDateOfCitation()));
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(v.getDateOfRecord()));
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(v.getStipulatedComplianceDate()));

            //stmt.setTimestamp(7, java.sql.Timestamp.valueOf(v.getActualComplianceDate()));
            stmt.setDouble(5, v.getPenalty());
            stmt.setString(6, v.getDescription());
            stmt.setString(7, v.getNotes());

            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch code violation by ID, sorry.", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }

    public void insertViolationLetter(CECase c, NoticeOfViolation notice) throws IntegrationException {

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

            System.out.println("CodeViolationIntegrator.insertViolationletter | sql: " + stmt.toString());
            
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

            System.out.println("CodeViolationIntegrator.updateNoticeOfViolation| sql: " + stmt.toString());
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

    public NoticeOfViolation getNoticeOfViolation(int violationID) throws IntegrationException {
        String query = "SELECT noticeid, caseid, lettertext, insertiontimestamp, dateofrecord, \n"
                + "       requesttosend, lettersenddate, letterreturneddate\n"
                + "  FROM public.noticeofviolation;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        NoticeOfViolation notice = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, violationID);
            System.out.println("Code.getEventCategory| sql: " + stmt.toString());
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

    public ArrayList<NoticeOfViolation> getNoticeOfViolationList(CECase ceCase) throws IntegrationException {
        String query = "SELECT noticeid, personid_recipient, caseid, lettertext, insertiontimestamp, dateofrecord, \n"
                + "       requesttosend, lettersenddate, letterreturneddate \n"
                + "  FROM public.noticeofviolation WHERE caseid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<NoticeOfViolation> al = new ArrayList();

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ceCase.getCaseID());
            rs = stmt.executeQuery();

            while (rs.next()) {
                al.add(generateNoticeOfViolation(rs));
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
                + "       dateofrecord=?, entrytimestamp=now(), stipulatedcompliancedate=?, \n"
                + "       actualcompliancdate=?, penalty=?, description=?, notes=?\n"
                + " WHERE violationid = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, v.getViolatedEnfElement().getCodeSetElementID());
            stmt.setInt(2, v.getCeCaseID());

            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(v.getDateOfRecord()));
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(v.getStipulatedComplianceDate()));

            if (v.getActualComplianceDate() != null) {
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(v.getActualComplianceDate()));
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }

            stmt.setDouble(6, v.getPenalty());
            stmt.setString((7), v.getDescription());
            stmt.setString(8, v.getNotes());
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
        CodeIntegrator ci = getCodeIntegrator();
        System.out.println("CodeViolationIntegreator.generateCodeViolationFromRS | Current RS entry: " + rs.getString("description"));

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
        return v;
    }

    public CodeViolation getCodeViolationByViolationID(int violationID) throws IntegrationException {
        String query = "SELECT violationid, codesetelement_elementid, cecase_caseid, dateofrecord, \n"
                + "       entrytimestamp, stipulatedcompliancedate, actualcompliancdate, \n"
                + "       penalty, description, notes\n"
                + "  FROM public.codeviolation WHERE violationid = ?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        CodeViolation cv = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, violationID);
            System.out.println("Code.getEventCategory| sql: " + stmt.toString());
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

    public ArrayList<CodeViolation> getCodeViolations(int caseID) throws IntegrationException {
        CECase tinyCase = new CECase();
        // this case is created to store a set of code violations
        tinyCase.setCaseID(caseID);
        return CodeViolationIntegrator.this.getCodeViolations(tinyCase);
    }

    public ArrayList<CodeViolation> getCodeViolations(CECase c) throws IntegrationException {
        String query = "SELECT violationid, codesetelement_elementid, cecase_caseid, dateofrecord, \n"
                + "       entrytimestamp, stipulatedcompliancedate, actualcompliancdate, \n"
                + "       penalty, description, notes from public.codeviolation WHERE cecase_caseid = ?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<CodeViolation> cvList = new ArrayList();

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, c.getCaseID());
            System.out.println("CodeViolationCoordinator.getCodeViolations | stmt: " + stmt.toString());
            rs = stmt.executeQuery();

            while (rs.next()) {

                cvList.add(generateCodeViolationFromRS(rs));

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
        tb.setMuni(mi.getMuniFromMuniCode(rs.getInt("muni_municode")));
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
            System.out.println("CodeViolationIntegrator.getTextBlock| sql: " + stmt.toString());
            
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
        String query =    "  SELECT blockid, blockcategory_catid, muni_municode, blockname, blocktext, categoryid, categorytitle\n" +
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
            System.out.println("CodeViolationIntegrator.getTextBlocksByMuni| sql: " + stmt.toString());
            
            rs = stmt.executeQuery(); 
            
            while(rs.next()){
                tb = generateTextBlock(rs);
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
     
     public ArrayList<TextBlock> getAllTextBlocks() throws IntegrationException{
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
            System.out.println("CodeViolationIntegrator.getTextBlocksByMuni| sql: " + stmt.toString());
            
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
            
            System.out.println("CodeViolationIntegrator.insertTextBlock| sql: " + stmt.toString());
            
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
            
            System.out.println("CodeViolationIntegrator.update text block| sql: " + stmt.toString());
            
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
            
            System.out.println("CodeViolationIntegrator.deleteTextBlock| sql: " + stmt.toString());
            
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
