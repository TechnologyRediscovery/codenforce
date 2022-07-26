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
import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.MunicipalityCoordinator;
import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.coordinators.WorkflowCoordinator;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CasePhaseEnum;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CitationStatusLogEntry;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.CodeViolationDisplayable;
import com.tcvcog.tcvce.entities.Icon;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.TextBlock;
import com.tcvcog.tcvce.entities.search.SearchParamsCECase;
import com.tcvcog.tcvce.entities.PrintStyle;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.CodeViolationPropCECaseHeavy;
import com.tcvcog.tcvce.entities.search.SearchParamsCodeViolation;
import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.entities.CitationCodeViolationLink;
import com.tcvcog.tcvce.entities.CitationDocketRecord;
import com.tcvcog.tcvce.entities.CitationStatus;
import com.tcvcog.tcvce.entities.IFace_transferrable;
import com.tcvcog.tcvce.entities.NoticeOfViolationType;
import com.tcvcog.tcvce.entities.Parcel;
import com.tcvcog.tcvce.entities.TextBlockCategory;
import com.tcvcog.tcvce.entities.CodeViolationStatusEnum;
import com.tcvcog.tcvce.entities.search.SearchParamsDateRule;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class CaseIntegrator extends BackingBeanUtils implements Serializable{
    
    final String CECASE_ACTIVE_FIELD = "cecase.active";
    /**
     * Creates a new instance of CaseIntegrator
     */
    public CaseIntegrator() {
    }
   
    
    /**
     * Single focal point of serach method for Code Enforcement case using a SearchParam
     * subclass. Outsiders will use runQueryCECase or runQueryCECase
     * @param params
     * @return a list of CECase IDs
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public List<Integer> searchForCECases(SearchParamsCECase params) throws IntegrationException, BObStatusException{
        SearchCoordinator sc = getSearchCoordinator();
        List<Integer> cseidlst = new ArrayList<>();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        params.appendSQL("SELECT DISTINCT caseid \n");
        params.appendSQL("FROM public.cecase \n");
        params.appendSQL("INNER JOIN public.parcel ON (cecase.parcel_parcelkey = parcel.parcelkey) \n");
        params.appendSQL("WHERE caseid IS NOT NULL ");
        
        // *******************************
        // **         BOb ID            **
        // *******************************
         if (!params.isBobID_ctl()) {

            //*******************************
            // **   MUNI,DATES,USER,ACTIVE  **
            // *******************************
            params = (SearchParamsCECase) sc.assembleBObSearchSQL_muniDatesUserActive(params, 
                                                                SearchParamsCECase.MUNI_DBFIELD,
                                                                CECASE_ACTIVE_FIELD);
            
            // *******************************
            // **       1:OPEN/CLOSED       **
            // *******************************
            if (params.isCaseOpen_ctl()) {
                if(params.isCaseOpen_val()){
                    params.appendSQL("AND closingdate IS NULL ");
                } else {
                    params.appendSQL("AND closingdate IS NOT NULL ");
                }
            }
            
            // *******************************
            // **      2:PROPERTY           **
            // *******************************
             if (params.isProperty_ctl()) {
                if(params.getProperty_val()!= null){
                    params.appendSQL("AND parcel_parcelkey=? ");
                } else {
                    params.setProperty_ctl(false);
                    params.appendToParamLog("PARCEL: no parcel object; prop filter disabled");
                }
            }
            
            // *******************************
            // **     3:PROPERTY UNIT       **
            // *******************************
             if (params.isPropertyUnit_ctl()) {
                if(params.getPropertyUnit_val()!= null){
                    params.appendSQL("AND parcelunit_unitid=? ");
                } else {
                    params.setPropertyUnit_ctl(false);
                    params.appendToParamLog("PARCEL UNIT: no parcelunit object; propunit filter disabled");
                }
            }
            
            // *******************************
            // **    4:PROP INFO CASES      **
            // *******************************
            if (params.isPropInfoCase_ctl()) {
                if (params.isPropInfoCase_val()) {
                    params.appendSQL("AND propertyinfocase = TRUE ");
                } else {
                    params.appendSQL("AND propertyinfocase = FALSE ");
                }
            }
            
            // **********************************
            // ** 5. PERSONS INFO CASES BOOL   **
            // **********************************
            if (params.isPersonInfoCase_ctl()) {
                if (params.isPersonInfoCase_val()) {
                    params.appendSQL("AND personinfocase_personid IS NOT NULL ");
                } else {
                    params.appendSQL("AND personinfocase_personid IS NULL ");
                }
            }
            
            // ********************************
            // ** 6.PERSONS INFO CASES ID    **
            // ********************************
            if (params.isPersonInfoCaseID_ctl()) {
                if(params.getPersonInfoCaseID_val() != null){
                    params.appendSQL("AND personinfocase_personid=? ");
                } else {
                    params.setPersonInfoCaseID_ctl(false);
                    params.appendToParamLog("PERSONINFO: no Person object; Person Info case filter disabled");
                }
            }
            
            // *******************************
            // **     7.BOb SOURCE          **
            // *******************************
             if (params.isSource_ctl()) {
                if(params.getSource_val() != null){
                    params.appendSQL("AND bobsource_sourceid=? ");
                } else {
                    params.setSource_ctl(false);
                    params.appendToParamLog("SOURCE: no BOb source object; source filter disabled");
                }
            }
           
            // *******************************
            // **        9. PACC            **
            // *******************************
             if (params.isPacc_ctl()) {
                if(params.isPacc_val()){
                    params.appendSQL("AND paccenabled = TRUE ");
                } else {
                    params.appendSQL("AND paccenabled = TRUE ");
                }
            }
            
            
        } else {
            params.appendSQL("caseid = ? "); // will be param 1 with ID search
        }

        int paramCounter = 0;
            
        try {
            stmt = con.prepareStatement(params.extractRawSQL());

            if (!params.isBobID_ctl()) {
                if (params.isMuni_ctl()) {
                     stmt.setInt(++paramCounter, params.getMuni_val().getMuniCode());
                }
                
                // adapted to possible use of date rule list
                if(params.isDate_startEnd_ctl()){
                    if(params.getDateRuleList() != null && !params.getDateRuleList().isEmpty()){
                        for(SearchParamsDateRule dr: params.getDateRuleList()){
                            if(dr.isDate_null_ctl()){
                                // no injection needed
                                System.out.println("CaseIntegrator.searchForCases: Found null date rule; no injection: " + params.getFilterName());
                            } else { // inject dates
                                stmt.setTimestamp(++paramCounter, java.sql.Timestamp.valueOf(dr.getDate_start_val()));
                                stmt.setTimestamp(++paramCounter, java.sql.Timestamp.valueOf(dr.getDate_end_val()));
                            }
                        }
                    } else { // legacy no date rule list
                        stmt.setTimestamp(++paramCounter, params.getDateStart_val_sql());
                        stmt.setTimestamp(++paramCounter, params.getDateEnd_val_sql());
                    }
                 }
                
                if (params.isUser_ctl()) {
                   stmt.setInt(++paramCounter, params.getUser_val().getUserID());
                }
                
                if (params.isProperty_ctl()) {
                    stmt.setInt(++paramCounter, params.getProperty_val().getParcelKey());
                }
                
                if (params.isPropertyUnit_ctl()) {
                    stmt.setInt(++paramCounter, params.getPropertyUnit_val().getUnitID());
                }
                
                 if (params.isPersonInfoCaseID_ctl()) {
                    stmt.setInt(++paramCounter, params.getPersonInfoCaseID_val().getHumanID());
                }
                 
                if(params.isSource_ctl()){
                    stmt.setInt(++paramCounter, params.getSource_val().getSourceid());
                }

            } else {
                stmt.setInt(++paramCounter, params.getBobID_val());
            }
            
            rs = stmt.executeQuery();

            int counter = 0;
            int maxResults;
            if (params.isLimitResultCount_ctl()) {
                maxResults = params.getLimitResultCount_val();
            } else {
                maxResults = Integer.MAX_VALUE;
            }
            while (rs.next() && counter < maxResults) {
                cseidlst.add(rs.getInt("caseid"));
                counter++;
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot search for code enf cases, sorry!", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return cseidlst;
    }
    
    
    /**
     * Single focal point of serach method for Code Enforcement case using a SearchParam
     * subclass. Outsiders will use runQueryCECase or runQueryCECase
     * @param params
     * @return a list of CECase IDs
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public List<CodeViolationPropCECaseHeavy> searchForCodeViolations(SearchParamsCodeViolation params) throws IntegrationException, BObStatusException{
        SearchCoordinator sc = getSearchCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        List<CodeViolationPropCECaseHeavy> cvpcehl = new ArrayList<>();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
      
params.appendSQL("SELECT DISTINCT codeviolation.violationid, cecase.caseid, cecase.casename, property.propertyid, property.address, municipality.municode, municipality.muniname ");
params.appendSQL("FROM public.codeviolation  ");
params.appendSQL("INNER JOIN public.cecase ON (cecase.caseid = codeviolation.cecase_caseid) ");
params.appendSQL("INNER JOIN public.parcel ON (cecase.parcel_parcelkey = parcel.parcelkey) ");
params.appendSQL("INNER JOIN public.municipality ON (parcel.muni_municode = municipality.municode) ");
params.appendSQL("LEFT OUTER JOIN  ");
params.appendSQL("(	SELECT codeviolation_violationid, citation.citationid, citation.dateofrecord ");
params.appendSQL("FROM public.citationviolation  ");
params.appendSQL("INNER JOIN public.citation ON (citationviolation.citation_citationid = citation.citationid) ");
params.appendSQL("INNER JOIN public.citationstatus on (citationstatus.statusid = citation.status_statusid) ");
params.appendSQL("WHERE citationstatus.editsforbidden = TRUE	 ");
params.appendSQL(") AS citv ON (codeviolation.violationid = citv.codeviolation_violationid) ");
params.appendSQL("LEFT OUTER JOIN  ");
params.appendSQL("( ");
params.appendSQL("SELECT codeviolation_violationid, sentdate ");
params.appendSQL("FROM noticeofviolationcodeviolation ");
params.appendSQL("INNER JOIN public.noticeofviolation ON (noticeofviolationcodeviolation.noticeofviolation_noticeid = noticeofviolation.noticeid) ");
params.appendSQL("WHERE noticeofviolation.sentdate IS NOT NULL ");
params.appendSQL(") AS novcv ON (codeviolation.violationid = novcv.codeviolation_violationid) ");
params.appendSQL("WHERE violationid IS NOT NULL ");
        
        // *******************************
        // **         BOb ID            **
        // *******************************
         if (!params.isBobID_ctl()) {

            //*******************************
            // **   MUNI,DATES,USER,ACTIVE  **
            // *******************************
            params = (SearchParamsCodeViolation) sc.assembleBObSearchSQL_muniDatesUserActive(params, 
                                                                SearchParamsCodeViolation.DBFIELD_MUNI,
                                                                SearchParamsCodeViolation.DBFIELD_ACTIVE);
            
            
            // *******************************
            // **     1.PROPERTY          **
            // *******************************
             if (params.isProperty_ctl()) {
                if(params.getProperty_val() != null){
                    params.appendSQL("AND property.propertyid=? ");
                } else {
                    params.setProperty_ctl(false);
                    params.appendToParamLog("PROPERTY PARAM: no property given; filter disabled");
                }
            }
            
            // *******************************
            // **     2.CECASE              **
            // *******************************
             if (params.isCecase_ctl()) {
                if(params.getCecase_val()!= null){
                    params.appendSQL("AND cecase.caseid=? ");
                } else {
                    params.setCecase_ctl(false);
                    params.appendToParamLog("CECASE: no case source object; case filter disabled");
                }
            }
             
            
            
            // *******************************
            // **     3. CITED              **
            // *******************************
             if (params.isCited_ctl()) {
                if(params.isCited_val()){
                    params.appendSQL("AND citv.citationid IS NOT NULL ");
                } else {
                    params.appendSQL("AND citv.citationid IS NULL ");
                }
            }
            
            // *******************************
            // **     4.LEGACY IMPORT       **
            // *******************************
             if (params.isLegacyImport_ctl()) {
                if(params.isLegacyImport_val()){
                    params.appendSQL("AND legacyimport=TRUE ");
                } else {
                    params.appendSQL("AND legacyimport=FALSE ");
                   
                }
            }
            
            // *******************************
            // **     5.SEVERITY            **
            // *******************************
             if (params.isSeverity_ctl()) {
                if(params.getSeverity_val() != null){
                    params.appendSQL("AND severity_classid=? ");
                } else {
                     params.setSeverity_ctl(false);
                    params.appendToParamLog("SEVERITY: no severity/intensity source object; severity filter disabled");
                }
            }
            
            // *******************************
            // **     6.BOb SOURCE          **
            // *******************************
             if (params.isSource_ctl()) {
                if(params.getSource_val() != null){
                    params.appendSQL("AND bobsource_sourceid=? ");
                } else {
                    params.setSource_ctl(false);
                    params.appendToParamLog("SOURCE: no BOb source object; source filter disabled");
                }
            }
             
            // *******************************
            // **     7.BOb SOURCE          **
            // *******************************
             if (params.isNoticeMailed_ctl()) {
                if(params.isNoticeMailed_val()){
                    params.appendSQL("AND novcv.sentdate IS NOT NULL ");
                } else {
                    params.appendSQL("AND novcv.sentdate IS NULL ");
                }
            }
           
            
            
        } else {
            params.appendSQL("violationid = ? "); // will be param 1 with ID search
        }

        int paramCounter = 0;
            
        try {
            stmt = con.prepareStatement(params.extractRawSQL());

            if (!params.isBobID_ctl()) {
                if (params.isMuni_ctl()) {
                     stmt.setInt(++paramCounter, params.getMuni_val().getMuniCode());
                }
                
                if(params.isDate_startEnd_ctl()){
                    stmt.setTimestamp(++paramCounter, params.getDateStart_val_sql());
                    stmt.setTimestamp(++paramCounter, params.getDateEnd_val_sql());
                 }
                
                if (params.isUser_ctl()) {
                   stmt.setInt(++paramCounter, params.getUser_val().getUserID());
                }
                // Violation set 1
                if (params.isProperty_ctl()) {
                    stmt.setInt(++paramCounter, params.getProperty_val().getParcelKey());
                }
                
                // violation set 2
                if (params.isCecase_ctl()) {
                    stmt.setInt(++paramCounter, params.getCecase_val().getCaseID());
                }
                // violation set 3
//                 if (params.isCited_ctl()) {
//                    stmt.setBoolean(++paramCounter, params.isCited_val());
//                }
                // violation set 4
//                if(params.isLegacyImport_ctl()){
//                    stmt.setBoolean(++paramCounter, params.isLegacyImport_val());
//                }
                
                // violation set 5
                if(params.isSeverity_ctl()){
                    stmt.setInt(++paramCounter, params.getSeverity_val().getClassID());
                }
                
                // violation set 6
                if(params.isSource_ctl()){
                    stmt.setInt(++paramCounter, params.getSource_val().getSourceid());
                }

            } else {
                stmt.setInt(++paramCounter, params.getBobID_val());
            }
            
            rs = stmt.executeQuery();

            int counter = 0;
            int maxResults;
            if (params.isLimitResultCount_ctl()) {
                maxResults = params.getLimitResultCount_val();
            } else {
                maxResults = Integer.MAX_VALUE;
            }
            while (rs.next() && counter < maxResults) {
                CodeViolationPropCECaseHeavy cvpch = new CodeViolationPropCECaseHeavy(cc.violation_getCodeViolation(rs.getInt("violationid")));
                cvpch.setCeCaseName(rs.getString("casename"));
                cvpch.setPropertyAddress(rs.getString("address"));
                cvpch.setPropertyID(rs.getInt("propertyid"));
                cvpch.setMuniCode(rs.getInt("municode"));
                cvpch.setMuniName(rs.getString("muniname"));
                
                
                cvpcehl.add(cvpch);
                
                counter++;
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot search for violations, sorry!", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return cvpcehl;
        
    }
    
    
    /**
     * Generates a CECaseDataHeavy without the big, fat lists
     * @param ceCaseID
     * @return
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public CECase getCECase(int ceCaseID) throws IntegrationException, BObStatusException{
        String query = "SELECT caseid, cecasepubliccc, parcel_parcelkey, parcelunit_unitid, \n" +
                        "       login_userid, casename, originationdate, closingdate, creationtimestamp, \n" +
                        "       notes, paccenabled, allowuplinkaccess, propertyinfocase, personinfocase_personid, \n" +
                        "       bobsource_sourceid, active, lastupdatedby_userid, lastupdatedts \n" +
                        "  FROM public.cecase WHERE caseid = ?;";
        ResultSet rs = null;
        CaseCoordinator cc = getCaseCoordinator();
        PreparedStatement stmt = null;
        Connection con = null;
        CECase c = null;
        
        try {
            
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ceCaseID);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                c = generateCECase(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get cecase by id", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return c;
    }
    
    /**
     * Gets a CECase according to a Property ID
     * @param propID
     * @return
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public List<CECase> getCECasesByProp(int propID) throws IntegrationException, BObStatusException{
        String query = "SELECT caseid, cecasepubliccc, parcel_parcelkey, parcelunit_unitid, \n" +
                        "       login_userid, casename, originationdate, closingdate, creationtimestamp, \n" +
                        "       notes, paccenabled, allowuplinkaccess, propertyinfocase, personinfocase_personid, \n" +
                        "       bobsource_sourceid, active, lastupdatedby_userid, lastupdatedts \n" +
                        "  FROM public.cecase WHERE parcel_parcelkey = ?;";
        ResultSet rs = null;
        CaseCoordinator cc = getCaseCoordinator();
        PreparedStatement stmt = null;
        Connection con = null;
        List<CECase> cList = new ArrayList<>();
        
        try {
            
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, propID);
            
            rs = stmt.executeQuery();
            
            while(rs.next()){
                cList.add(generateCECase(rs));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get cecase by id", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return cList;
    }
    
    /**
     * Internal generator for CECase objects
     * @param rs with all DB fields included
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
     private CECase generateCECase(ResultSet rs) throws SQLException, IntegrationException{
        try {
            UserCoordinator uc = getUserCoordinator();
            SystemIntegrator si = getSystemIntegrator();
            
            CECase cse = new CECase();
            
            cse.setCaseID(rs.getInt("caseid"));
            cse.setPublicControlCode(rs.getInt("cecasepubliccc"));
            
            cse.setParcelKey(rs.getInt("parcel_parcelkey"));
            cse.setPropertyUnitID(rs.getInt("parcelunit_unitid"));
            
            cse.setCaseManager(uc.user_getUser(rs.getInt("login_userid")));
            
            
            cse.setCaseName(rs.getString("casename"));
            
            if(rs.getTimestamp("originationdate") != null){
                cse.setOriginationDate(rs.getTimestamp("originationdate")
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
            
            if(rs.getTimestamp("closingdate") != null){
                cse.setClosingDate(rs.getTimestamp("closingdate")
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
            if(rs.getTimestamp("creationtimestamp") != null){
                cse.setCreationTimestamp(rs.getTimestamp("creationtimestamp")
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
            
            cse.setNotes(rs.getString("notes"));
            cse.setPaccEnabled(rs.getBoolean("paccenabled"));
            cse.setAllowForwardLinkedPublicAccess(rs.getBoolean("allowuplinkaccess"));
            cse.setPropertyInfoCase(rs.getBoolean("propertyinfocase"));
            cse.setPersonInfoPersonID(rs.getInt("personinfocase_personid"));
            
            if(rs.getInt("bobsource_sourceid") != 0){
                cse.setSource(si.getBOBSource(rs.getInt("bobsource_sourceid")));
            }
            cse.setActive(rs.getBoolean("active"));
            
            if(rs.getInt("lastupdatedby_userid") != 0){
                cse.setLastUpdatedBy(uc.user_getUser(rs.getInt("lastupdatedby_userid")));
            }
            
            if(rs.getTimestamp("lastupdatedts") != null){
                cse.setLastUpdatedTS(rs.getTimestamp("lastupdatedts").toLocalDateTime());
            }
            
            
            return cse;
        } catch (BObStatusException ex) {
            throw new IntegrationException(ex.toString());
        }
    }
    
     /**
      * First gen search method to be deprecated in Beta
      * @param pacc
      * @return
      * @throws IntegrationException
      * @throws BObStatusException 
      */
    public List<CECase> getCECasesByPACC(int pacc) throws IntegrationException, BObStatusException{
        
        ArrayList<CECase> caseList = new ArrayList();
        String query = "SELECT caseid FROM public.cecase WHERE cecasepubliccc = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, pacc);
            System.out.println("CaseIntegrator.getCECasesByPacc | sql: " + stmt.toString());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                caseList.add(getCECase(rs.getInt("caseid")));
                
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot search for cases by PACC, sorry", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return caseList;
    }
    
  /**
     * Asks the cecase table for all case id's that are property info cases for a given parcel
     * @param pcl
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public List<Integer> getParcelInfoCaseIDList(Parcel pcl) throws IntegrationException, BObStatusException {
        if(pcl == null){
            throw new BObStatusException("cannot get parcel info cases with null parcel input");
        }
        
        String query = "SELECT caseid FROM cecase WHERE propertyinfocase = TRUE AND parcel_parcelkey=?;";
        List<Integer> idl = new ArrayList<>();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, pcl.getParcelKey());
            rs = stmt.executeQuery();
            while (rs.next()) {
                idl.add(rs.getInt("caseid"));
            }
        } catch (SQLException ex) {

            System.out.println(ex);
            throw new IntegrationException("PropertyIntegrator.getPropertyUnitChange | Unable to get property unit, ", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }

        return idl;
    }
    /**
     * Insertion point for CECaseDataHeavy objects; must be called by Coordinator who checks 
     * logic before sending to the DB. This method only copies from the passed in CECaseDataHeavy
     * into the SQL INSERT
     * 
     * @param ceCase
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public int insertNewCECase(CECase ceCase) throws IntegrationException, BObStatusException{
        String query = "INSERT INTO public.cecase(\n" +
                        "            caseid, cecasepubliccc, parcel_parcelkey, parcelunit_unitid, \n" +
                        "            login_userid, casename, originationdate, closingdate, creationtimestamp, \n" +
                        "            notes, paccenabled, allowuplinkaccess, propertyinfocase, personinfocase_personid, \n" +
                        "            bobsource_sourceid, active, lastupdatedby_userid, lastupdatedts)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, now(), \n" +
                        "            ?, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, now());";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int insertedCaseID = 0;
        Connection con = null;
        
        try {
            
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ceCase.getPublicControlCode());
            stmt.setInt(2, ceCase.getParcelKey());
            if(ceCase.getPropertyUnitID() != 0) {
                stmt.setInt(3, ceCase.getPropertyUnitID());
            } else { 
                stmt.setNull(3, java.sql.Types.NULL); 
            }
            
            if(ceCase.getCaseManager() != null){
                stmt.setInt(4, ceCase.getCaseManager().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            stmt.setString(5, ceCase.getCaseName());
            
            if(ceCase.getOriginationDate() != null){
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(ceCase.getOriginationDate()));
            } else {
                stmt.setNull(6, java.sql.Types.NULL);                
            }
            // closing date
            stmt.setNull(7, java.sql.Types.NULL); 
            
            // creation TS set by database with now()
            
            stmt.setString(8, ceCase.getNotes());
            stmt.setBoolean(9, ceCase.isPaccEnabled());
            stmt.setBoolean(10, ceCase.isAllowForwardLinkedPublicAccess());
            stmt.setBoolean(11, ceCase.isPropertyInfoCase());
            if(ceCase.getPersonInfoPersonID() != 0){
                stmt.setInt(12, ceCase.getPersonInfoPersonID());
            } else {
                stmt.setNull(12, java.sql.Types.NULL); 
                
            }
            
            if(ceCase.getSource() != null){
                stmt.setInt(13, ceCase.getSource().getSourceid());
            } else {
                stmt.setNull(13, java.sql.Types.NULL); 
            }
            
            stmt.setBoolean(14, ceCase.isActive());
            
            if(ceCase.getLastUpdatedBy() != null){
                stmt.setInt(15, ceCase.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(15, java.sql.Types.NULL); 
            }
            
            stmt.execute();
            
            String retrievalQuery = "SELECT currval('cecase_caseID_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                insertedCaseID = rs.getInt(1);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Case Integrator: FATAL INSERT error; apologies.", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return insertedCaseID;
        
    }
    
    /**
     * Updates the values in the CECaseDataHeavy in the DB but does NOT
     * edit the data in connected tables, namely CodeViolation, EventCnF, and Person
     * Use calls to other add methods in this class for adding additional
     * violations, events, and people to a CE case.
     * 
     * Method will update the case's last udpated time stamp and user, if supplied
     * by the case object's fields
     * 
     * @param ceCase the case to updated, with updated member variables
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void updateCECaseMetadata(CECase ceCase) throws IntegrationException{
        String query =  "UPDATE public.cecase\n" +
                        "   SET cecasepubliccc=?, parcel_parcelkey=?, parcelunit_unitid=?, \n" + // 1-3
                        "       login_userid=?, casename=?, originationdate=?, closingdate=?, \n" + // 4-7
                        "       notes=?, paccenabled=?, allowuplinkaccess=?, \n" + // 5-7
                        "       propertyinfocase=?, personinfocase_personid=?, bobsource_sourceid=?, \n" + // 8-10
                        "       active=?, lastupdatedby_userid=?, lastupdatedts=now() \n" + // 11-12
                        "  WHERE caseid=?;";
        PreparedStatement stmt = null;
        Connection con = null;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ceCase.getPublicControlCode());
            stmt.setInt(2, ceCase.getParcelKey());
            
            if(ceCase.getPropertyUnitID() != 0){
                stmt.setInt(3, ceCase.getPropertyUnitID());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(ceCase.getCaseManager() != null){
                stmt.setInt(4, ceCase.getCaseManager().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            if(ceCase.getCaseName() != null){
                stmt.setString(5, ceCase.getCaseName());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            if(ceCase.getOriginationDate() != null){
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(ceCase.getOriginationDate()));
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            if(ceCase.getClosingDate() != null){
                stmt.setTimestamp(7, java.sql.Timestamp.valueOf(ceCase.getClosingDate()));
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            stmt.setString(8, ceCase.getNotes());
            stmt.setBoolean(9, ceCase.isPaccEnabled());
            stmt.setBoolean(10, ceCase.isAllowForwardLinkedPublicAccess());
            
            stmt.setBoolean(11, ceCase.isPropertyInfoCase());
            if(ceCase.getPersonInfoPersonID() != 0){
                stmt.setInt(12, ceCase.getPersonInfoPersonID());
            } else {
                stmt.setNull(12, java.sql.Types.NULL);
            }
            if(ceCase.getSource() != null){
                stmt.setInt(13, ceCase.getSource().getSourceid());
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }
            stmt.setBoolean(14, ceCase.isActive());
            if(ceCase.getLastUpdatedBy() != null){
                stmt.setInt(15, ceCase.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(15, java.sql.Types.NULL);
            }
            
            stmt.setInt(16, ceCase.getCaseID());
            
            stmt.executeUpdate();
            
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot udpate case due to a database storage issue", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
   
    /**
     * Updates only the notes field on cecase table
     * @param cse with the Notes field as you want it inserted 
     * 
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public void updateCECaseNotes(CECase cse) 
            throws IntegrationException, BObStatusException{
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        if(cse == null){
            throw new BObStatusException("cannot update notes on a null case");
        }
        
        try {
            String s = "UPDATE public.cecase SET notes=? WHERE caseid=?";
            stmt = con.prepareStatement(s);
            stmt.setString(1, cse.getNotes());
            stmt.setInt(2, cse.getCaseID());

            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update cecase notes", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
   
    /**
     * Updates only the notes field on citation table
     
     * 
     * @param cit
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public void updateCitationNotes(Citation cit) 
            throws IntegrationException, BObStatusException{
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        if(cit == null){
            throw new BObStatusException("cannot update notes on a null citation");
        }
        
        try {
            String s = "UPDATE public.citation SET notes=? WHERE citationid=?";
            stmt = con.prepareStatement(s);
            stmt.setString(1, cit.getNotes());
            stmt.setInt(2, cit.getCitationID());

            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update citation notes", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
   
    /**
     * Updates only the notes field on noticeofviolation table
     
     * 
     * @param nov
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public void novUpdateNotes(NoticeOfViolation nov) 
            throws IntegrationException, BObStatusException{
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        if(nov == null){
            throw new BObStatusException("cannot update notes on a null notice");
        }
        
        try {
            String s = "UPDATE public.noticeofviolation SET notes=? WHERE noticeid=?";
            stmt = con.prepareStatement(s);
            stmt.setString(1, nov.getNotes());
            stmt.setInt(2, nov.getNoticeID());

            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update NOV notes", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
   
 
    
   
    /**
     * Grabs caseids from the loginobjecthistory table
     * @param userID
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public List<Integer> getCECaseHistoryList(int userID) 
            throws IntegrationException, BObStatusException{
        List<Integer> cseidl = new ArrayList<>();
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String s = "SELECT cecase_caseid, entrytimestamp FROM loginobjecthistory "
                    + "WHERE login_userid = ? "
                    + "AND cecase_caseid IS NOT NULL "
                    + "ORDER BY entrytimestamp DESC;";
            stmt = con.prepareStatement(s);
            stmt.setInt(1, userID);

            rs = stmt.executeQuery();
            
            while (rs.next()) {
                cseidl.add(rs.getInt("cecase_caseid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate case history list", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return cseidl;
    }
      /**
     *
     * @param casephase
     * @return
     * @throws IntegrationException
     */
    public Icon getIcon(CasePhaseEnum casephase) throws IntegrationException {
        Connection con = getPostgresCon();
        SystemIntegrator si = getSystemIntegrator();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT iconid ");
        sb.append("FROM public.cecasestatusicon WHERE status=?::casephase;");
        Icon icon = null;

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setString(1, casephase.toString());
            rs = stmt.executeQuery();
            while (rs.next()) {
                icon = si.getIcon(rs.getInt("iconid"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate icon", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return icon;

    }
    
    
    
    // *************************************************************************
    // *            CODE VIOLATIONS                                            *
    // *************************************************************************
    

    /**
     * Creates a new record in the codeviolation table from a CodeViolation object
     * @param v
     * @return
     * @throws IntegrationException 
     */
    public int insertCodeViolation(CodeViolation v) throws IntegrationException {
        int lastID = 0;

        String query =  "INSERT INTO public.codeviolation(\n" +
                        "            violationid, codesetelement_elementid, cecase_caseid, dateofrecord, \n" +
                        "            entrytimestamp, stipulatedcompliancedate, actualcompliancedate, \n" +
                        "            penalty, description, notes, legacyimport, compliancetimestamp, \n" +
                        "            complianceuser, severity_classid, createdby, compliancetfexpiry_proposalid, \n" +
                        "            lastupdatedts, lastupdated_userid, active)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, \n" +
                        "            now(), ?, ?, \n" +
                        "            ?, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            now(), ?, TRUE);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);

            stmt.setInt(1, v.getViolatedEnfElement().getCodeSetElementID());
            stmt.setInt(2, v.getCeCaseID());
            if(v.getDateOfRecord() != null){
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(v.getDateOfRecord()));
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }

            // entryts stamped by PG's now()
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(v.getStipulatedComplianceDate()));
            // a smidge of business logic here! Whoopsies
            stmt.setNull(5, java.sql.Types.NULL); // actual compliance
            
            stmt.setDouble(6, v.getPenalty());
            stmt.setString(7, v.getDescription());
            stmt.setString(8, v.getNotes());
            stmt.setBoolean(9, v.isLeagacyImport());
            stmt.setNull(10, java.sql.Types.NULL); // compliance TS
            
            stmt.setNull(11, java.sql.Types.NULL); // compliance user
            if(v.getSeverityIntensity() != null){
                stmt.setInt(12, v.getSeverityIntensity().getClassID() );
            } else {
                stmt.setNull(12, java.sql.Types.NULL);
            }
            
            if(v.getCreatedBy() != null){
                stmt.setInt(13, v.getCreatedBy().getUserID());
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }

            if(v.getComplianceTFExpiryPropID() != 0){
                stmt.setInt(14, v.getComplianceTFExpiryPropID());
            } else {
                stmt.setNull(14, java.sql.Types.NULL);
            }
            
            if(v.getLastUpdatedUser() != null){
                stmt.setInt(15, v.getLastUpdatedUser().getUserID());
            } else {
                stmt.setNull(15, java.sql.Types.NULL);
            }
            
            
            
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
     * Pathway for updating CodeViolatio objects
     * @param v
     * @throws IntegrationException 
     */
    public void updateCodeViolation(CodeViolation v) throws IntegrationException {
        String query =  " UPDATE public.codeviolation\n" +
                        "   SET codesetelement_elementid=?, cecase_caseid=?, dateofrecord=?, \n" + // 1-3
                        "       stipulatedcompliancedate=?, \n" + // 4
                        "       penalty=?, description=?, legacyimport=?, \n" + // 5-7
                        "       severity_classid=?, compliancetfexpiry_proposalid=?, \n" + // 8-9
                        "       lastupdatedts=now(), lastupdated_userid=?, active=?,  nullifiedts=?, nullifiedby=? \n" + // 10-13
                        " WHERE violationid = ?;"; //14
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, v.getViolatedEnfElement().getCodeSetElementID());
            stmt.setInt(2, v.getCeCaseID());
            
            if(v.getDateOfRecord() != null){
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(v.getDateOfRecord()));
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(v.getStipulatedComplianceDate()!= null){
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(v.getStipulatedComplianceDate()));
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            stmt.setDouble(5, v.getPenalty());
            stmt.setString(6, v.getDescription());
            stmt.setBoolean(7, v.isLeagacyImport());
            
           
            if(v.getSeverityIntensity() != null){
                stmt.setInt(8, v.getSeverityIntensity().getClassID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            
            if(v.getComplianceTFExpiryProp() != null){
                stmt.setInt(9, v.getComplianceTFExpiryProp().getProposalID());
            } else if(v.getComplianceTFExpiryPropID() != 0){
                stmt.setInt(9, v.getComplianceTFExpiryPropID());
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            
            if(v.getLastUpdatedUser() != null){
                stmt.setInt(10, v.getLastUpdatedUser().getUserID());
            }else {
                stmt.setNull(10, java.sql.Types.NULL);
            }
            stmt.setBoolean(11, v.isActive());
            
            if(v.getNullifiedTS() != null){
                stmt.setTimestamp(12, java.sql.Timestamp.valueOf(v.getNullifiedTS()));
            } else {
                stmt.setNull(12, java.sql.Types.NULL);
            }
            
            if(v.getNullifiedUser() != null){
                stmt.setInt(13, v.getNullifiedUser().getUserID());
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }
            
            stmt.setInt(14, v.getViolationID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot update code violation, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }
    
    
    /**
     * Pathway for recording compliance data on a single code violation record
     * @param v
     * @throws IntegrationException 
     */
    public void updateCodeViolationCompliance(CodeViolation v) throws IntegrationException {
        String query =  " UPDATE public.codeviolation\n" +
                        "   SET actualcompliancedate=?, compliancenote=?, \n" + // 4-5
                        "       complianceuser=?, compliancetimestamp=now(), \n" + // 9-12
                        "       lastupdatedts=now(), lastupdated_userid=? \n" + // 13-14
                        " WHERE violationid = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            if(v.getActualComplianceDate() != null){
                stmt.setTimestamp(1, java.sql.Timestamp.valueOf(v.getActualComplianceDate()));
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            
            stmt.setString(2, v.getComplianceNote());
            
            if(v.getComplianceUser() != null){
                stmt.setInt(3, v.getComplianceUser().getUserID());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(v.getLastUpdatedUser() != null){
                stmt.setInt(4, v.getLastUpdatedUser().getUserID());
            }else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            stmt.setInt(5, v.getViolationID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Integration error: cannot record violation compliance, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }
    
    
    /**
     * Generator method for CodeViolation objects
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException
     */
    private CodeViolation generateCodeViolationFromRS(ResultSet rs) 
            throws SQLException, 
            IntegrationException,
            BObStatusException
            {

        CodeViolation v = new CodeViolation();
        CodeIntegrator ci = getCodeIntegrator();
        UserCoordinator uc = getUserCoordinator();
        WorkflowCoordinator wc = getWorkflowCoordinator();
        SystemIntegrator si = getSystemIntegrator();
        
        
        v.setViolationID(rs.getInt("violationid"));
        v.setViolatedEnfElement(ci.getEnforcableCodeElement(rs.getInt("codesetelement_elementid")));
        
        if(rs.getString("createdby") != null){
            v.setCreatedBy(uc.user_getUser(rs.getInt("createdby")));
        }
        
        v.setCeCaseID(rs.getInt("cecase_caseid"));
        if(rs.getTimestamp("dateofrecord") != null){
            v.setDateOfRecord(rs.getTimestamp("dateofrecord").toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
        }

        if(rs.getTimestamp("entrytimestamp") != null){
            
        v.setCreationTS(rs.getTimestamp("entrytimestamp").toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        }

        if(rs.getTimestamp("stipulatedcompliancedate") != null){
            
        v.setStipulatedComplianceDate(rs.getTimestamp("stipulatedcompliancedate").toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
                
        if (!(rs.getTimestamp("actualcompliancedate") == null)) {
            v.setActualComplianceDate(rs.getTimestamp("actualcompliancedate").toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
        }

        v.setPenalty(rs.getDouble("penalty"));
        v.setDescription(rs.getString("description"));
        v.setNotes(rs.getString("notes"));
        v.setLeagacyImport(rs.getBoolean("legacyimport"));

        if(rs.getTimestamp("compliancetimestamp") != null){
            v.setComplianceTimeStamp(rs.getTimestamp("compliancetimestamp").toLocalDateTime());
            v.setComplianceUser(uc.user_getUser(rs.getInt("complianceUser")));
        }
        
        v.setComplianceNote(rs.getString("compliancenote"));
        
        if(rs.getInt("compliancetfexpiry_proposalid") != 0){
            v.setComplianceTFExpiryPropID(rs.getInt("compliancetfexpiry_proposalid"));
            v.setComplianceTFExpiryProp(wc.getProposal(rs.getInt("compliancetfexpiry_proposalid")));
        }
        
        v.setActive(rs.getBoolean("active"));
        
        if(rs.getTimestamp("nullifiedts") != null){
            v.setNullifiedTS(rs.getTimestamp("nullifiedts").toLocalDateTime());
        } else {
            v.setNullifiedTS(null);
        }

        if(rs.getInt("nullifiedby") != 0){
            v.setNullifiedUser(uc.user_getUser(rs.getInt("nullifiedby")));
        } 
        
        if(rs.getInt("severity_classid") != 0){
            v.setSeverityIntensity(si.getIntensityClass(rs.getInt("severity_classid")));
        }
        
        List<BlobLight> blobList = new ArrayList<>();
//        try {
//            for(int id : bi.getBlobsByCECase(v.getViolationID())){
//                blobList.add(bc.getBlobLight(id));
//            }
//        } catch (BlobException ex){
//            throw new IntegrationException("An error occurred while retrieving blobs for a Code Violation", ex);
//        }
        v.setBlobList(blobList);
        
        populateTransferrableFields(v, rs);
        
        return v;
    }

    /**
     * Primary getter method for CodeViolation objects
     * @param violationID
     * @return
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public CodeViolation getCodeViolation(int violationID) 
            throws IntegrationException, BObStatusException {
        String query = "SELECT violationid, codesetelement_elementid, cecase_caseid, dateofrecord, \n" +
                        "       entrytimestamp, stipulatedcompliancedate, actualcompliancedate, \n" +
                        "       penalty, description, notes, legacyimport, compliancetimestamp, \n" +
                        "       complianceuser, severity_classid, createdby, compliancetfexpiry_proposalid, \n" +
                        "       lastupdatedts, lastupdated_userid, active, compliancenote, nullifiedts, nullifiedby,  " +
                        "       transferredts, transferredby_userid, transferredtocecase_caseid \n" +
                        "  FROM public.codeviolation WHERE violationid = ?";
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

    /**
     * Queries for all violation attached to a specific CECase
     * @param cse
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> getCodeViolations(CECase cse) throws IntegrationException, BObStatusException {
        if(cse == null){
            throw new BObStatusException("Cannot get violations by case with null case!");
            
        }
        
        String query = "SELECT violationid FROM codeviolation WHERE cecase_caseid = ?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<Integer> idl = new ArrayList();

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cse.getCaseID());
            rs = stmt.executeQuery();

            while (rs.next()) {
                idl.add(rs.getInt("violationid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("CaseInt.getCodeViolations(CECase): Cannot fetch code violations by CECase, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return idl;
       
    }
    
    /**
     * Utility adaptor method for retrieving multiple code violations given a CECaseDataHeavy
     * @param c
     * @return
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public List<Integer> getCodeViolationIDList(CECaseDataHeavy c) throws IntegrationException, BObStatusException {
        return getCodeViolations(c);
    }
    
    /**
     * TODO: NADGIT Fix the BlobList References
     * @param cv
     * @return
     * @throws IntegrationException 
     */
    public List<Blob> loadViolationPhotoList(CodeViolation cv) throws IntegrationException{
        
        List<Blob> vBlobList = new ArrayList<>();
        BlobCoordinator bc = getBlobCoordinator();
        
        String query = "SELECT photodoc_photodocid FROM public.codeviolationphotodoc WHERE codeviolation_violationid = ?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cv.getViolationID());
            rs = stmt.executeQuery();

//            while (rs.next()) {
//                vBlobList.add(bc.getBlob(rs.getInt("photodoc_photodocid")));
//            }
            
//            cv.setBlobIDList(blobList);

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot load photos on violation.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return vBlobList;
    }
       /**
     * Updates only the notes field on codeviolation table
     * 
     * @param viol
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public void updateCodeViolationNotes(CodeViolation viol) 
            throws IntegrationException, BObStatusException{
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        if(viol == null){
            throw new BObStatusException("cannot update notes on a null violation");
        }
        
        try {
            String s = "UPDATE public.codeviolation SET notes=? WHERE violationid=?";
            stmt = con.prepareStatement(s);
            stmt.setString(1, viol.getNotes());
            stmt.setInt(2, viol.getViolationID());

            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update code violation notes", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    
    /**
     * Writes the three fields specified by the IFace_transferrable interface
     * @param trable 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void updateTransferrable(IFace_transferrable trable) throws IntegrationException{
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        if(trable == null){
            throw new IntegrationException("cannot update notes a null transferrable");
        }
        
        try {

            StringBuilder sb = new StringBuilder();
            sb.append("UPDATE ");
            sb.append(trable.getTransferEnum().getTargetTableID());
            sb.append(" SET transferredts=?, transferredby_userid=?, transferredtocecase_caseid=? ");
            sb.append(" WHERE ");
            sb.append(trable.getTransferEnum().getTargetPKField());
            sb.append("=?;");
                    
            stmt = con.prepareStatement(sb.toString());
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(trable.getTransferredTS()));
            if(trable.getTransferredBy() != null && trable.getTransferredBy().getUserID() != 0){
                stmt.setInt(2, trable.getTransferredBy().getUserID());
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            if(trable.getTransferredToCECaseID() != 0){
                stmt.setInt(3, trable.getTransferredToCECaseID());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            stmt.setInt(4, trable.getDBKey());

            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update transferrable status", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
    /**
     * Populates the three fields on a transferrable object:
     * 
     * @param trable
     * @param rs
     * @throws IntegrationException
     * @throws SQLException
     * @throws BObStatusException 
     */
    public void populateTransferrableFields(IFace_transferrable trable, ResultSet rs) throws IntegrationException, SQLException, BObStatusException{
        
        if(trable == null || rs == null){
            throw new IntegrationException("Cannot populate transferrable with null transferrable or RS");
        }
        UserCoordinator uc = getUserCoordinator();
        
        if(rs.getTimestamp("transferredts") != null){
            trable.setTransferredTS(rs.getTimestamp("transferredts").toLocalDateTime());
        }
        if(rs.getInt("transferredby_userid") != 0){
            trable.setTransferredBy(uc.user_getUser(rs.getInt("transferredby_userid")));
        }
        trable.setTransferredToCECaseID(rs.getInt("transferredtocecase_caseid"));
        
    }
    
    
    // *************************************************************************
    // *            NOTICES OF VIOLATION (NOV)                                 *
    // *************************************************************************
    
    
    /**
     * Writes a new Notice of Violation to the DB
     * @param c associated CECaseDataHeavy
     * @param notice the Notice to write
     * @return the id assigned the new notice by the database
     * @throws IntegrationException 
     */
    public int novInsert(CECaseDataHeavy c, NoticeOfViolation notice) throws IntegrationException, BObStatusException {

        String query =  "INSERT INTO public.noticeofviolation(\n" +
                        "            noticeid, caseid, lettertextbeforeviolations, creationtimestamp, \n" +
                        "            dateofrecord, recipient_humanid, recipient_mailing, lettertextafterviolations, \n" +
                        "            notes, creationby, printstyle_styleid, notifyingofficer_userid, letter_typeid)\n" +
                        "    VALUES (DEFAULT, ?, ?, now(), \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, ?);";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int insertedNOVId = 0;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setInt(1, c.getCaseID());
            stmt.setString(2, notice.getNoticeTextBeforeViolations());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(notice.getDateOfRecord()));
            
            if(notice.getRecipient() != null){
                stmt.setInt(4, notice.getRecipient().getHumanID());
            } else {
                throw new BObStatusException("Cannot write notice without a person recipient");
            }
            if(notice.getRecipientMailingAddress() != null){
                stmt.setInt(5, notice.getRecipientMailingAddress().getAddressID());
            } else {
                throw new BObStatusException("Cannot write notice without an address");
            }
            stmt.setString(6, notice.getNoticeTextAfterViolations());
            stmt.setString(7, notice.getNotes());
            stmt.setInt(8, notice.getCreationBy().getUserID());
            if(notice.getStyle() != null){
                stmt.setInt(9, notice.getStyle().getStyleID());
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            
             if(notice.getNotifyingOfficer() != null){
                stmt.setInt(10, notice.getNotifyingOfficer().getUserID());
            } else {
                throw new BObStatusException("Cannot write notice without a notifying officer");
            }
                    
            
             if(notice.getNovType()!= null){
                stmt.setInt(11, notice.getNovType().getTypeID());
            } else {
                stmt.setNull(11, java.sql.Types.NULL);
            }
                    
            stmt.execute();
            
            String retrievalQuery = "SELECT currval('noticeofviolation_noticeid_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();
            while(rs.next()){
                insertedNOVId = rs.getInt(1);
            }
            
            notice.setNoticeID(insertedNOVId);
            System.out.println("CaseIntetgrator.novInsert | noticeid " + notice.getNoticeID());
            System.out.println("CaseIntegrator.novInsert | retrievedid " + insertedNOVId);
            
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
    
    /**
     * Creates link between a NOV and a CECase
     * @param nov
     * @param cv
     * @throws IntegrationException 
     */
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
    
    
    /**
     * Utility method for calling novUpdateNoticeCodeViolation on each violation
     * in a NOV
     * @param nov
     * @throws IntegrationException 
     */
    public void novUpdateAllNoticeCodeViolations(NoticeOfViolation nov) throws IntegrationException{
            Iterator<CodeViolationDisplayable> iter = nov.getViolationList().iterator();
            CodeViolationDisplayable cvd = null;
            while(iter.hasNext()){
                novUpdateNoticeCodeViolation(iter.next(), nov);
            }
    }
    
    /**
     * Updates the connection between a code violation and a NOV
     * @param cv
     * @param nov
     * @throws IntegrationException 
     */
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
    
    /**
     * Removes linkages between a NOV and its CodeViolations. We don't lose any data beyond
     * display params for the CV in the NOV
     * @param nov
     * @throws IntegrationException 
     */
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
    
    /**
     * When a NOV is locked, it cannot be edited because it's ready for mailing
     * @param nov
     * @throws IntegrationException 
     */
    public void novLockAndQueueForMailing(NoticeOfViolation nov) throws IntegrationException{
        if (nov == null){
            throw new IntegrationException("novLockAndQueueForMailing(...): Cannot lock null NOV");
        }
        
        String query =  "UPDATE public.noticeofviolation\n" +
                        "   SET lockedandqueuedformailingdate=?, lockedandqueuedformailingby=?, " + // 1-2
                        "       recipient_humanid=?, recipient_mailing=?, \n" +                          // 3-4
                        "       fixedrecipientxferts=now(), fixedrecipientname=?, fixedrecipientbldgno=?, \n" + // 5-6
                        "       fixedrecipientstreet=?, fixedrecipientcity=?, fixedrecipientstate=?, \n" +  // 7-9
                        "       fixedrecipientzip=?,  fixednotifyingofficername=?, fixednotifyingofficertitle=?, \n" +
                        "       fixednotifyingofficerphone=?, fixednotifyingofficeremail=?" + // 10
                        "   WHERE noticeid=?;"; //11
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(nov.getLockedAndqueuedTS()));
            stmt.setInt(2, nov.getLockedAndQueuedBy().getUserID());
            
            stmt.setInt(3, nov.getRecipient().getHumanID());
            stmt.setInt(4, nov.getRecipientMailingAddress().getAddressID());
            
            stmt.setString(5, nov.getFixedRecipientName());
            stmt.setString(6, nov.getFixedRecipientBldgNo());
            stmt.setString(7, nov.getFixedRecipientStreet());
            stmt.setString(8, nov.getFixedRecipientCity());
            stmt.setString(9, nov.getFixedRecipientState());
            stmt.setString(10, nov.getFixedRecipientZip());
            
            stmt.setString(11, nov.getFixedNotifyingOfficerName());
            stmt.setString(12, nov.getFixedNotifyingOfficerTitle());
            stmt.setString(13, nov.getFixedNotifyingOfficerPhone());
            stmt.setString(14, nov.getFixedNotifyingOfficerEmail());
            
            stmt.setInt(15, nov.getNoticeID());

            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("novLockAndQueueForMailing(...): Unable to write NOV lock to database", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Updates the NOV record with sender info
     * @param nov
     * @throws IntegrationException 
     */
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
    
    /**
     * Updates an NOV record to reflect the NOV being returned by the USPS
     * @param nov
     * @throws IntegrationException 
     */
    public void novRecordReturnedNotice(NoticeOfViolation nov) throws IntegrationException{
        String query =  "UPDATE public.noticeofviolation\n" +
                        "   SET returneddate=?, returnedby=? " +
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
    
    /**
     * Updates the notes field only on a NOV object
     * @param nov
     * @throws IntegrationException 
     */
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
    
    
    /**
     * Updates a subset of NOV fields
     * @param notice
     * @throws IntegrationException 
     */
    public void novUpdateNotice(NoticeOfViolation notice) throws IntegrationException {
        String query = "UPDATE public.noticeofviolation\n"
                + "   SET   lettertextbeforeviolations=?, \n"
                + "         dateofrecord=?, lettertextafterviolations=?, "
                + "         recipient_humanid=?, recipient_mailing=?, "
                + "         notifyingofficer_userid=?, notifyingofficer_humanid=?, letter_typeid=? "
                + " WHERE noticeid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setString(1, notice.getNoticeTextBeforeViolations());
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(notice.getDateOfRecord()));
            stmt.setString(3, notice.getNoticeTextAfterViolations());
            if(notice.getRecipient() != null){
                stmt.setInt(4, notice.getRecipient().getHumanID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            if(notice.getRecipientMailingAddress() != null){
                stmt.setInt(5, notice.getRecipientMailingAddress().getAddressID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            if(notice.getNotifyingOfficer() != null){
                stmt.setInt(6, notice.getNotifyingOfficer().getUserID());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            if(notice.getNotifyingOfficerPerson()!= null){
                stmt.setInt(7, notice.getNotifyingOfficerPerson().getHumanID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            if(notice.getNovType() != null){
                stmt.setInt(8, notice.getNovType().getTypeID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            
            stmt.setInt(9, notice.getNoticeID());

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

    /**
     * Actually deletes an NOV from the DB! Very rare indeed. The CaseCoordinator
     * will only allow this if the NOV has not been locked or printed
     * @param notice
     * @throws IntegrationException 
     */
    public void novDelete(NoticeOfViolation notice) throws IntegrationException {
        String query = "UPDATE noticeofviolation SET active=FALSE "
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
    
    /**
     * Updates an NOV record to reflect that no mailing actions have occurred
     * @param notice
     * @throws IntegrationException 
     */
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
    
     public void novUpdateHeaderImage(PrintStyle ps) throws BObStatusException, IntegrationException{
         if(ps == null || ps.getHeader_img_id() == 0){
             throw new BObStatusException("Cannot update header image with null print or header_img_id == 0");
                     
         }
         

        String query = "UPDATE public.printstyle\n" +
                        "   SET headerimage_photodocid=?" +
                        " WHERE styleid=?;";
        // note that original time stamp is not altered on an update

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ps.getHeader_img_id());
            stmt.setInt(2, ps.getStyleID());
            
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot clear nov of mailing data.", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
         
         
     }
     
    /**
     * Primary getter for the NOV object
     * @param noticeID
     * @return
     * @throws IntegrationException
     */
    public NoticeOfViolation novGet(int noticeID) 
            throws IntegrationException, BObStatusException, BlobException{
        String query =  "SELECT noticeid, caseid, lettertextbeforeviolations, creationtimestamp, \n" +
                        "       dateofrecord, sentdate, returneddate, personid_recipient, lettertextafterviolations, \n" +
                        "       lockedandqueuedformailingdate, lockedandqueuedformailingby, sentby, \n" +
                        "       returnedby, notes, creationby, printstyle_styleid, active, followupevent_eventid, notifyingofficer_userid, " +
                        "       recipient_humanid, recipient_mailing, \n" +
                        "       fixedrecipientxferts, fixedrecipientname, fixedrecipientbldgno, \n" +
                        "       fixedrecipientstreet, fixedrecipientcity, fixedrecipientstate, \n" +
                        "       fixedrecipientzip, fixednotifyingofficername, fixednotifyingofficertitle, \n" +
                        "       fixednotifyingofficerphone, fixednotifyingofficeremail, letter_typeid \n" +
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
            throw new IntegrationException("CaseIntegrator.netGet(): cannot fetch NoticeOfViolation by NoticeID, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return notice;
    }

    /**
     * Grabs all the NOVs for a given CECase
     * @param ceCase
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> novGetList(CECase ceCase) throws IntegrationException {
        String query = "SELECT noticeid FROM public.noticeofviolation WHERE caseid=? AND active=TRUE;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<Integer> idl = new ArrayList();

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ceCase.getCaseID());
            rs = stmt.executeQuery();

            while (rs.next()) {
                idl.add(rs.getInt("noticeid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch NOV List by CECAse, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return idl;
    }
    
    
    /**
     * Extracts the IDs of all NOV types based on muni
     * @param muni if null, all active types will be returned
     * @return the IDs of the types
     * @throws IntegrationException 
     */
    public List<Integer> novGetTypeList(Municipality muni) throws IntegrationException {
        
        StringBuilder sb  = new StringBuilder("SELECT novtypeid FROM public.noticeofviolationtype WHERE deactivatedts IS NULL ");
        if(muni != null){
            sb.append("AND muni_municode=?");
        } else {
            sb.append(";");
        }
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<Integer> idl = new ArrayList();

        try {
            stmt = con.prepareStatement(sb.toString());
            if(muni != null){
                stmt.setInt(1, muni.getMuniCode());
            }
            rs = stmt.executeQuery();

            while (rs.next()) {
                idl.add(rs.getInt("novtypeid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch NOV Type List by Muni, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return idl;
    }
    
    
    /**
     * Insertion point for NOV Types
     * @param novt
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public int novInsertNOVType(NoticeOfViolationType novt) throws IntegrationException, BObStatusException {

        if(novt == null || novt.getTypeID() != 0){
            throw new IntegrationException("Cannot insert new NOV type with null type or nonzero ID");
        }
        
        String query =  "INSERT INTO public.noticeofviolationtype(\n" +
                        "            novtypeid, title, description, eventcatsent_catid, eventcatfollowup_catid, \n" +
                        "            eventcatreturned_catid, followupwindowdays, headerimage_photodocid, \n" +
                        "            textblockcategory_catid, muni_municode, courtdocument, injectviolations, \n" +
                        "            deactivatedts, printstyle_styleid)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            NULL, ?);";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int freshID = 0;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setString(1, novt.getTitle());
            stmt.setString(2, novt.getDescription());
            if(novt.getEventCatSent() != null){
                stmt.setInt(3, novt.getEventCatSent().getCategoryID());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            if(novt.getEventCatFollowUp() != null){
                stmt.setInt(4, novt.getEventCatFollowUp().getCategoryID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            if(novt.getEventCatReturned() != null){
                stmt.setInt(5, novt.getEventCatReturned().getCategoryID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            stmt.setInt(6, novt.getFollowUpWindowDays());
            
            if(novt.getNovHeaderBlob() != null){
                stmt.setInt(7, novt.getNovHeaderBlob().getPhotoDocID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            if(novt.getTextBlockCategory() != null){
                stmt.setInt(8, novt.getTextBlockCategory().getCategoryID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            
            if(novt.getMuni() != null){
                stmt.setInt(9, novt.getMuni().getMuniCode());
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            
            stmt.setBoolean(10, novt.isCourtDocument());
            stmt.setBoolean(11, novt.isInjectViolations());
            
            
            if(novt.getPrintStyle() != null){
                stmt.setInt(12, novt.getPrintStyle().getStyleID());
            } else{
                stmt.setNull(12, java.sql.Types.NULL);
            }
            
        
            stmt.execute();
            
            String retrievalQuery = "SELECT currval('noticeofviolationtype_novtypeid_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                freshID = rs.getInt(1);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert notice of violation type due to an integration error, sorry.", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return freshID;

    } // close method
    /**
     * Update point for NOV Types
     * @param novt
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public void novUpdateNOVType(NoticeOfViolationType novt) throws IntegrationException, BObStatusException {

        if(novt == null || novt.getTypeID() == 0){
            throw new IntegrationException("Cannot update NOV type with null type or ID == 0");
        }
        
        String query =  "UPDATE public.noticeofviolationtype\n" +
                        "   SET title=?, description=?, eventcatsent_catid=?, eventcatfollowup_catid=?, \n" +
                        "       eventcatreturned_catid=?, followupwindowdays=?, headerimage_photodocid=?, \n" +
                        "       textblockcategory_catid=?, muni_municode=?, courtdocument=?, \n" +
                        "       injectviolations=?, deactivatedts=?, printstyle_styleid=? \n" +
                        " WHERE novtypeid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int freshID = 0;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setString(1, novt.getTitle());
            stmt.setString(2, novt.getDescription());
            if(novt.getEventCatSent() != null){
                stmt.setInt(3, novt.getEventCatSent().getCategoryID());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            if(novt.getEventCatFollowUp() != null){
                stmt.setInt(4, novt.getEventCatFollowUp().getCategoryID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            if(novt.getEventCatReturned() != null){
                stmt.setInt(5, novt.getEventCatReturned().getCategoryID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            stmt.setInt(6, novt.getFollowUpWindowDays());
            
            if(novt.getNovHeaderBlob() != null){
                stmt.setInt(7, novt.getNovHeaderBlob().getPhotoDocID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            if(novt.getTextBlockCategory() != null){
                stmt.setInt(8, novt.getTextBlockCategory().getCategoryID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            
            if(novt.getMuni() != null){
                stmt.setInt(9, novt.getMuni().getMuniCode());
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            
            stmt.setBoolean(10, novt.isCourtDocument());
            stmt.setBoolean(11, novt.isInjectViolations());
            if(novt.getDeactivatedTS() != null){
                stmt.setTimestamp(12, java.sql.Timestamp.valueOf(novt.getDeactivatedTS()));
            } else {
                stmt.setNull(12, java.sql.Types.NULL);
            }
            
            if(novt.getPrintStyle() != null){
                stmt.setInt(13, novt.getPrintStyle().getStyleID());
            } else{
                stmt.setNull(13, java.sql.Types.NULL);
            }
            
            stmt.setInt(14, novt.getTypeID());
            
        
            stmt.executeUpdate();
            
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert notice of violation type due to an integration error, sorry.", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

    } // close method
    
    
    /**
     * Extracts and builds an NOV type object from the DB.
     * @param tpe
     * @return
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BlobException 
     */
    public NoticeOfViolationType novGetType(int tpe) throws IntegrationException, BlobException {
        if(tpe == 0){
            throw new IntegrationException("Cannot get NOV type with ID ==0");
        }
        
        String query  = "SELECT novtypeid, title, description, eventcatsent_catid, eventcatfollowup_catid, \n" +
                        "       eventcatreturned_catid, followupwindowdays, headerimage_photodocid, \n" +
                        "       textblockcategory_catid, muni_municode, courtdocument, injectviolations, \n" +
                        "       deactivatedts, printstyle_styleid \n" +
                        "  FROM public.noticeofviolationtype WHERE novtypeid=?;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        NoticeOfViolationType novt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, tpe);
            rs = stmt.executeQuery();

            while (rs.next()) {
                novt = novGenerateType(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch NOV Type by id, sorry.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return novt;
    }
    
        
    
   /**
    * Internal generator for creating NOV type objects
    */    
    private NoticeOfViolationType novGenerateType(ResultSet rs) throws SQLException, IntegrationException, BlobException{
        NoticeOfViolationType novt = new NoticeOfViolationType();
        EventCoordinator ec = getEventCoordinator();
        MunicipalityCoordinator mc = getMuniCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        BlobCoordinator bc = getBlobCoordinator();
        
        novt.setTypeID(rs.getInt("novtypeid"));
        novt.setTitle(rs.getString("title"));
        novt.setDescription(rs.getString("description"));
        
        if(rs.getInt("eventcatsent_catid") != 0){
            novt.setEventCatSent(ec.getEventCategory(rs.getInt("eventcatsent_catid")));
        }
        if(rs.getInt("eventcatfollowup_catid") != 0){
            novt.setEventCatFollowUp(ec.getEventCategory(rs.getInt("eventcatfollowup_catid")));
        }
        if(rs.getInt("eventcatreturned_catid") != 0){
            novt.setEventCatReturned(ec.getEventCategory(rs.getInt("eventcatreturned_catid")));
        }
        
        novt.setFollowUpWindowDays(rs.getInt("followupwindowdays"));
        if(rs.getInt("headerimage_photodocid") != 0){
            novt.setNovHeaderBlob(bc.getBlobLight(rs.getInt("headerimage_photodocid")));
        }
        
        novt.setMuni(mc.getMuni(rs.getInt("muni_municode")));
        novt.setCourtDocument(rs.getBoolean("courtdocument"));
        novt.setInjectViolations(rs.getBoolean("injectviolations"));
        if(rs.getTimestamp("deactivatedts") != null){
            novt.setDeactivatedTS(rs.getTimestamp("deactivatedts").toLocalDateTime());
        }
        if(rs.getInt("textblockcategory_catid") != 0){
            novt.setTextBlockCategory(sc.getTextBlockCategory(rs.getInt("textblockcategory_catid")));
        }
        
        if(rs.getInt("printstyle_styleid") != 0){
            novt.setPrintStyle(sc.getPrintStyle(rs.getInt("printstyle_styleid")));
        }
        return novt;
        
    }
    
    
    
    
    
    /**
     * Grabs all the NOVs for a given CECase
     * @param cv
     * @return of Notice letter IDs
     * @throws IntegrationException 
     */
    public List<Integer> novGetNOVIDList(CodeViolation cv) throws IntegrationException {
        String query = "SELECT noticeofviolation_noticeid FROM public.noticeofviolationcodeviolation "
                + "WHERE codeviolation_violationid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<Integer> idl = new ArrayList<>();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cv.getViolationID());
            rs = stmt.executeQuery();

            while (rs.next()) {
                idl.add(rs.getInt("noticeofviolation_noticeid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("cannot fetch NOV LIst by CodeViolation, sorry!", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return idl;
    }

    /**
     * Internal generator method for NOVs
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException
     */
    private NoticeOfViolation novGenerate(ResultSet rs) 
            throws SQLException, 
            IntegrationException,
            BObStatusException,
            BlobException {

        UserCoordinator uc = getUserCoordinator();
        SystemIntegrator si = getSystemIntegrator();
        EventCoordinator ec = getEventCoordinator();
        PersonCoordinator pc = getPersonCoordinator();
        PropertyCoordinator propc = getPropertyCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        
        // the magical moment of notice instantiation
        NoticeOfViolation notice = new NoticeOfViolation();

        notice.setNoticeID(rs.getInt("noticeid"));
        
        notice.setDateOfRecord(rs.getTimestamp("dateofrecord").toLocalDateTime());

        notice.setNoticeTextBeforeViolations(rs.getString("lettertextbeforeviolations"));
        populateCodeViolations(notice);
        notice.setNoticeTextAfterViolations(rs.getString("lettertextafterviolations"));

        notice.setCreationTS(rs.getTimestamp("creationtimestamp").toLocalDateTime());
        notice.setCreationBy(uc.user_getUser(rs.getInt("creationby")));
        
        if (rs.getTimestamp("lockedandqueuedformailingdate") != null) {
            notice.setLockedAndqueuedTS(rs.getTimestamp("lockedandqueuedformailingdate").toLocalDateTime());
            notice.setLockedAndQueuedBy(uc.user_getUser(rs.getInt("lockedandqueuedformailingby")));
        }
        
        if (rs.getTimestamp("sentdate") != null) {
            notice.setSentTS(rs.getTimestamp("sentdate").toLocalDateTime());
            notice.setSentBy(uc.user_getUser(rs.getInt("sentby")));
        } 
        
        if (rs.getTimestamp("returneddate") != null) {
            notice.setReturnedTS(rs.getTimestamp("returneddate").toLocalDateTime());
            notice.setReturnedBy(uc.user_getUser(rs.getInt("returnedby")));
        } 
        
        notice.setStyle(si.getPrintStyle(rs.getInt("printstyle_styleid")));
        notice.setNotes(rs.getString("notes"));
        if(rs.getInt("followupevent_eventid") != 0){
            notice.setFollowupEvent(ec.getEvent(rs.getInt("followupevent_eventid")));
        }
        notice.setActive(rs.getBoolean("active"));
        
        if(rs.getInt("notifyingofficer_userid") != 0){
            notice.setNotifyingOfficer(uc.user_getUser(rs.getInt("notifyingofficer_userid")));
        }
        if(notice.getNotifyingOfficer() != null && notice.getNotifyingOfficer().getHumanID() != 0){
            notice.setNotifyingOfficerPerson(pc.getPersonByHumanID(notice.getNotifyingOfficer().getHumanID()));
        }
        
        if(rs.getInt("recipient_humanid") != 0){
            notice.setRecipient(pc.getPersonByHumanID(rs.getInt("recipient_humanid")));
        }
        if(rs.getInt("recipient_mailing") != 0){
            notice.setRecipientMailingAddress(propc.getMailingAddress(rs.getInt("recipient_mailing")));
        }
        if(rs.getTimestamp("fixedrecipientxferts") != null){
            notice.setFixedAddrXferTS(rs.getTimestamp("fixedrecipientxferts").toLocalDateTime());
        }
        
        notice.setFixedRecipientName(rs.getString("fixedrecipientname"));
        notice.setFixedRecipientBldgNo(rs.getString("fixedrecipientbldgno"));
        notice.setFixedRecipientStreet(rs.getString("fixedrecipientstreet"));
        notice.setFixedRecipientCity(rs.getString("fixedrecipientcity"));
        notice.setFixedRecipientState(rs.getString("fixedrecipientstate"));
        notice.setFixedRecipientZip(rs.getString("fixedrecipientzip"));
        
        notice.setFixedNotifyingOfficerName(rs.getString("fixednotifyingofficername"));
        notice.setFixedNotifyingOfficerTitle(rs.getString("fixednotifyingofficertitle"));
        notice.setFixedNotifyingOfficerPhone(rs.getString("fixednotifyingofficerphone"));
        notice.setFixedNotifyingOfficerEmail(rs.getString("fixednotifyingofficeremail"));
        
        if(rs.getInt("letter_typeid") != 0){
            notice.setNovType(cc.nov_getNOVType(rs.getInt("letter_typeid")));
        }

        return notice;

    }
    
    /**
     * Looks up all of the CodeViolations associated with a given NOV
     * and extracts their display properties stored in the linking DB's linking table
     * @param nov
     * @return
     * @throws IntegrationException
     */
    private NoticeOfViolation populateCodeViolations(NoticeOfViolation nov) 
            throws IntegrationException, BObStatusException{
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
            throw new IntegrationException("Cannot populate notice of violation, sorry!", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        nov.setViolationList(codeViolationList);
        
        return nov;
        
    }
    
    
    
    
     // ***********************************************************************
     // **                  TEXT BLOCKS                                      **
     // ***********************************************************************
     
   
   /**
    * Retrieves all text block categories from the DB
     * @param muni
    * @return if null, all active cats are returned
    * @throws IntegrationException 
    */
    public List<Integer> getTextBlockCategoryList(Municipality muni) throws IntegrationException{
        String query =  "SELECT categoryid FROM public.textblockcategory WHERE deactivatedts IS NULL ";
        if(muni != null){
            query = query + " AND muni_municode=?;";
        } else {
            query = query + ";";
        }
        List<Integer> idl = new ArrayList<>();
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
             if(muni != null){
                stmt.setInt(1, muni.getMuniCode());
             }
            rs = stmt.executeQuery();

            while (rs.next()) {
                idl.add(rs.getInt("categoryid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot fetch TextBlockMap, sorries!", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return idl;
    }
    
    
     /**
    * Retrieves a single text block category
     * @param id
    * @return
    * @throws IntegrationException 
    */
    public TextBlockCategory getTextBlockCategory(int id) throws IntegrationException{
        if(id == 0){
            throw new IntegrationException("Canot get a text block category if id == 0");
        }
        String query =  "SELECT categoryid, categorytitle, icon_iconid, muni_municode, \n" +
                        "       deactivatedts\n" +
                        "  FROM public.textblockcategory WHERE categoryid=?";

        TextBlockCategory tbc = null;
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, id);
            
            rs = stmt.executeQuery();

            while (rs.next()) {
                tbc = generateTextBlockCategory(rs);
                
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot fetch TextBlockCategory, sorries!", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return tbc;
    }
    
   /**
    * Retrieves all text blocks from DB
    * @return
    * @throws IntegrationException 
    * @deprecated in favor of standard coordinator model--if it wants to build a MAP, 
    *  then it can do so by itself
    */
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
            throw new IntegrationException("Cannot fetch TextBlockMap, sorries!", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return categoryMap;
    }
    
    /**
     * Builds a text block
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
    private TextBlock generateTextBlock(ResultSet rs) throws SQLException, IntegrationException{
        TextBlock tb = new TextBlock();
        MunicipalityCoordinator mc = getMuniCoordinator();
        
        tb.setBlockID(rs.getInt("blockid"));
        tb.setCategory(getTextBlockCategory(rs.getInt("blockcategory_catid")));
        tb.setMuni(mc.getMuni(rs.getInt("muni_municode")));
        tb.setTextBlockName(rs.getString("blockname"));
        tb.setTextBlockText(rs.getString("blocktext"));
        tb.setPlacementOrder(rs.getInt("placementorderdefault"));
        tb.setInjectableTemplate(rs.getBoolean("injectabletemplate"));
        if(rs.getTimestamp("deactivatedts") != null){
            tb.setDeactivatedTS(rs.getTimestamp("deactivatedts").toLocalDateTime());
        }
        
        return tb;
    }
    
    
    /**
     * Generator for text block categories;
     * @param rs
     * @return 
     */
    private TextBlockCategory generateTextBlockCategory(ResultSet rs) throws SQLException, IntegrationException{
        TextBlockCategory tbc = new TextBlockCategory();
        MunicipalityCoordinator mc = getMuniCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        
        tbc.setCategoryID(rs.getInt("categoryid"));
        tbc.setTitle(rs.getString("categorytitle"));
        if(rs.getInt("icon_iconid") != 0){
            tbc.setIcon(sc.getIcon(rs.getInt("icon_iconid")));
        }
        tbc.setMuni(mc.getMuni(rs.getInt("muni_municode")));
        if(rs.getTimestamp("deactivatedts") != null){
            tbc.setDeactivatedTS(rs.getTimestamp("deactivatedts").toLocalDateTime());
        }
        
        return tbc;
        
    }
    
    /**
     * Extracts a single text block from the DB
     * @param blockID
     * @return
     * @throws IntegrationException 
     */
     public TextBlock getTextBlock(int blockID) throws IntegrationException{
         
        String query =  "SELECT blockid, blockcategory_catid, muni_municode, blockname, blocktext, \n" +
                        "       placementorderdefault, injectabletemplate, deactivatedts\n" +
                        "  FROM public.textblock  WHERE blockid=?;";
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
     
   
     
     /**
      * Extracts all text blocks associated with a given Muni
     * @param tbc if null, blocks are returned without respect to category
      * @param m if Null, all blocks returned
      * @return
      * @throws IntegrationException 
      */
     public List<Integer> getTextBlockIDList(TextBlockCategory tbc, Municipality m) throws IntegrationException{
        StringBuilder sb = new StringBuilder(" SELECT blockid " );
                            sb.append("  FROM public.textblock WHERE deactivatedts IS NULL ");
        
        if(m != null){
            sb.append(" AND textblock.muni_municode=?");
        } 
        if(tbc != null){
            sb.append(" AND blockcategory_catid=?");
        }
        sb.append(";");
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection con = null;
        List<Integer> idl = new ArrayList();
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(sb.toString());
            int paramCount = 0;
            if(m != null){
                stmt.setInt(++paramCount, m.getMuniCode());
            }
            if(tbc != null){
                stmt.setInt(++paramCount, tbc.getCategoryID());
            }
            rs = stmt.executeQuery(); 
            while(rs.next()){
                idl.add(rs.getInt("blockid"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("CaseIntegrator: cannot retrieve text blocks by cat and/or muni", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
         return idl;
     }
     
     /**
      * Extracts all text blocks associated with a given Muni
      * @param m
      * @return
      * @throws IntegrationException 
      * @deprecated 
      */
     public ArrayList<TextBlock> getTextBlockTemplates(Municipality m) throws IntegrationException{
        String query =    "  SELECT blockid " +
                            "  FROM public.textblock INNER JOIN public.textblockcategory ON textblockcategory.categoryid=textblock.blockcategory_catid\n" +
                            "  WHERE textblock.muni_municode=? AND textblock.injectabletemplate IS TRUE;";
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
            throw new IntegrationException("Case Integrator: cannot retrive text blocks by municipality", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
         return ll;
     }
     
     /**
      * Extracts a master list of all text blocks regardless of Muni
      * @return
      * @throws IntegrationException 
      */
     public List<Integer> getAllTextBlocks() throws IntegrationException{
        String query =    "  SELECT blockid \n" +
                          "  FROM public.textblock;";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection con = null;
        List<Integer> idl = new ArrayList<>();
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            
            rs = stmt.executeQuery(); 
            
            while(rs.next()){
                idl.add((rs.getInt("blockid")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Code Violation Integrator: cannot retrive all textblocks", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
          
         return idl;
         
     }
     
     /**
      * Creates a new entry in the textblock table
      * @param tb
     * @return 
      * @throws IntegrationException 
      */
     public int insertTextBlock(TextBlock tb) throws IntegrationException{
        String query =  "INSERT INTO public.textblock(\n" +
                        " blockid, blockcategory_catid, muni_municode, blockname, "
                        + "blocktext, placementorderdefault, injectabletemplate)\n" +
                        " VALUES (DEFAULT, ?, ?, ?, ?, ?, ?);";
        PreparedStatement stmt = null;
        Connection con = null;
        ResultSet rs = null;
        int insertedBlockID = 0;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
           if(tb.getCategory() != null){
                stmt.setInt(1, tb.getCategory().getCategoryID());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            stmt.setInt(2, tb.getMuni().getMuniCode());
            stmt.setString(3, tb.getTextBlockName());
            stmt.setString(4, tb.getTextBlockText());
            stmt.setInt(5, tb.getPlacementOrder());
            stmt.setBoolean(6, tb.isInjectableTemplate());
            
            stmt.execute(); 
            
             String retrievalQuery = "SELECT currval('textblock_blockid_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                insertedBlockID = rs.getInt(1);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Text Block Integration Module: cannot insert text block into DB, sorry", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
          
         return insertedBlockID;
         
     }
     
     /**
      * Updates key fields on text blocks
      * @param tb
      * @throws IntegrationException 
      */
     public void updateTextBlock(TextBlock tb) throws IntegrationException{
        String query = "UPDATE public.textblock\n" +
                        "   SET blockcategory_catid=?, muni_municode=?, blockname=?, \n" +
                        "       blocktext=?,placementorderdefault=?, injectabletemplate=?, deactivatedts=? \n" +
                        " WHERE blockid=?;";
        PreparedStatement stmt = null;
        Connection con = null;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            if(tb.getCategory() != null){
                stmt.setInt(1, tb.getCategory().getCategoryID());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            stmt.setInt(2, tb.getMuni().getMuniCode());
            stmt.setString(3, tb.getTextBlockName());
            stmt.setString(4, tb.getTextBlockText());
            stmt.setInt(5, tb.getPlacementOrder());
            stmt.setBoolean(6, tb.isInjectableTemplate());
            if(tb.getDeactivatedTS() != null){
                stmt.setTimestamp(7, java.sql.Timestamp.valueOf(tb.getDeactivatedTS()));
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            stmt.setInt(8, tb.getBlockID());  
            
            stmt.execute(); 
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Text Block Integration Module: cannot insert text block into DB", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
          
         
     }
     
     /**
      * Creates a new entry in the textblock table
     * @param tbc
     * @return  ID of fresh category
      * @throws IntegrationException 
      */
     public int insertTextBlockCategory(TextBlockCategory tbc) throws IntegrationException{
        String query =  "INSERT INTO public.textblockcategory(\n" +
                        "            categoryid, categorytitle, icon_iconid, muni_municode, deactivatedts)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?);";
        PreparedStatement stmt = null;
        Connection con = null;
        ResultSet rs = null;
        int insertedBlockCatID = 0;
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
          
            stmt.setString(1, tbc.getTitle());
            if(tbc.getIcon() != null){
                stmt.setInt(2, tbc.getIcon().getID());
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            if(tbc.getMuni() != null){
                stmt.setInt(3, tbc.getMuni().getMuniCode());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            if(tbc.getDeactivatedTS() != null){
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(tbc.getDeactivatedTS()));
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            
            stmt.execute(); 
            
             String retrievalQuery = "SELECT currval('blockcategory_categoryid_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                insertedBlockCatID = rs.getInt(1);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Text Block Cat Integration Module: cannot insert text block cat into DB, sorry", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
          
         return insertedBlockCatID;
         
     }
     
     /**
      * Updates key fields on text blocks
     * @param tbc
      * @throws IntegrationException 
      */
     public void updateTextBlockCategory(TextBlockCategory tbc) throws IntegrationException{
        String query = "UPDATE public.textblockcategory\n" +
                        "   SET categorytitle=?, icon_iconid=?, muni_municode=?, \n" +
                        "       deactivatedts=?\n" +
                        " WHERE categoryid=?;";
        PreparedStatement stmt = null;
        Connection con = null;
        
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            
            stmt.setString(1, tbc.getTitle());
            if(tbc.getIcon() != null){
                stmt.setInt(2, tbc.getIcon().getID());
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            if(tbc.getMuni() != null){
                stmt.setInt(3, tbc.getMuni().getMuniCode());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            if(tbc.getDeactivatedTS() != null){
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(tbc.getDeactivatedTS()));
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            stmt.setInt(5, tbc.getCategoryID());
            
            
            stmt.executeUpdate(); 
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Text Block Category Integration Module: cannot update text block category in DB", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
          
         
     }
     
  
     
} // close CaseIntegrator