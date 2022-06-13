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
 *git 
* You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.money.integration;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.DomainEnum;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.money.coordination.MoneyCoordinator;
import com.tcvcog.tcvce.money.entities.*;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Talks to Postgres about Money Objects
 * 
 * @author Adam Gutonski & Gutted by Ellen Bascomb and rewritten
 */
public class MoneyIntegrator extends BackingBeanUtils implements Serializable {

    public MoneyIntegrator() {

    }
    
    
    
    
    
    
    /**
     * Builds a Transaction object from one record in the moneyledger table
     * @param trxid
     * @return
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public Transaction getTransaction(int trxid) throws IntegrationException, BObStatusException{
        
        String query = "SELECT transactionid, cecase_caseid, occperiod_periodid, transtype, \n" +
                        "       amount, dateofrecord, source, event_eventid, \n" +
                        "       lockedts, lockedby_userid, createdts, createdby_userid, lastupdatedts, \n" +
                        "       lastupdatedby_userid, deactivatedts, deactivatedby_userid, notes\n" +
                        "  FROM public.moneyledger WHERE transactionid=?;";
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Connection con = null;
        
        if(trxid == 0){
            throw new IntegrationException("Cannot get a new Transaction with ID of 0");
        }
        
        Transaction trx = null;
        try {
            
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, trxid);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                trx = generateTransaction(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("MoneyIntegrator.getTransaction SQL error", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return trx;
    }
    
    
    /**
     * Generator for Transaction ResultSet objects
     * @param rs
     * @return the bare bones Transaction, 
     */
    private Transaction generateTransaction(ResultSet rs) 
            throws SQLException, IntegrationException, BObStatusException{
        if(rs == null){
            throw new IntegrationException("MoneyIntegrator.generateTransaction: Cannot populate Transaction with null RS");
        }
        
        Transaction trx = new Transaction();
        SystemIntegrator si = getSystemIntegrator();
        MoneyCoordinator mc = getMoneyCoordinator();
        
        trx.setTransactionID(rs.getInt("transactionid"));
        trx.setCeCaseID(rs.getInt("cecase_caseid"));
        trx.setOccPeriodID(rs.getInt("occperiod_periodid"));
        trx.setTnxDomain(DomainEnum.valueOf(rs.getString("transtype")));
        trx.setTransactionSource(mc.getTransactionSource(rs.getInt("source_id")));
        
        trx.setAmount(rs.getDouble("amount"));
        trx.setDateOfRecord(rs.getTimestamp("dateofrecord").toLocalDateTime());
        trx.setTrackingEventID(rs.getInt("event_eventid"));
        si.populateTrackedFields(trx, rs, false);
        trx.setNotes(rs.getString("notes"));
        return trx;
        
    }
    
    /**
     * Writes a new record to the moneyledger table;
     * 
     * @param trx audited and configured for insert by coordinator
     * @return the ID of the freshly inserted ledger entry
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public int insertTransaction(Transaction trx) throws IntegrationException{
        
        if(trx == null){
            throw new IntegrationException("Cannot insert a null transaction!");
        }
        
        int freshTrxID = 0;
        
           
        String query = "INSERT INTO public.moneyledger(\n" +
                        "            transactionid, cecase_caseid, occperiod_periodid, transtype, \n" +
                        "            amount, dateofrecord, source_id, event_eventid, lockedts, lockedby_userid, \n" +
                        "            createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, \n" +
                        "            deactivatedts, deactivatedby_userid, notes)\n" +
                        "    VALUES (DEFAULT, ?, ?, CAST(? AS transactiontype), \n" +
                        "            ?, ?, ?, ?, NULL, NULL, \n" +
                        "            now(), ?, now(), ?, \n" +
                        "            ?);";
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Connection con = null;
        
        List<CECase> cList = new ArrayList<>();
        
        try {
            
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            
            if(trx.getCeCaseID() != 0){
                stmt.setInt(1, trx.getCeCaseID());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }
            
            if(trx.getOccPeriodID() != 0){
                stmt.setInt(2, trx.getOccPeriodID());
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            
            if(trx.getTnxType() != null){
                stmt.setString(3, trx.getTnxType().name());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            stmt.setDouble(4, trx.getAmount());
            
            if(trx.getDateOfRecord() != null){
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(trx.getDateOfRecord()));
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            if(trx.getTransactionSource() != null){
                stmt.setInt(6, trx.getTransactionSource().getSourceID());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            if(trx.getTrackingEventID() != 0){
                stmt.setInt(7, trx.getTrackingEventID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            if(trx.getCreatedBy() != null){
                stmt.setInt(8, trx.getCreatedBy().getUserID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            
            if(trx.getLastUpdatedBy() != null){
                stmt.setInt(9, trx.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            
            stmt.execute();
            
            String retrievalQuery = "SELECT currval('moneyledger_transactionid_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                freshTrxID = rs.getInt("currval");
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Exception??", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return freshTrxID;
        
    }
    
    
    
    /**
     * Builds a TnxSource from the db table moneytransactionsource
     * @param srcid
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public TnxSource getTransactionSource(int srcid) throws IntegrationException{
        if(srcid == 0){
            throw new IntegrationException("Cannot retrieve Tnx source with ID == 0;");
        }
        
        
        String query = "SELECT sourceid, title, descrition, notes, humanassignable, eventcatwhenposted, \n" +
                        "       applicabletype_typeid, active \n" +
                        "  FROM public.moneytransactionsource WHERE sourceid=?;";
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Connection con = null;
        
        TnxSource tnxsrc = null;
        
        try {
            
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, srcid);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                tnxsrc = generateTnxSource(rs);
                
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Failed to get a record from moneytransactionsource", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return tnxsrc;
        
    }
    
    
    /**
     * Internal generator for our lil TnxSource objects
     * @param rs
     * @return
     * @throws IntegrationException 
     */
    private TnxSource generateTnxSource(ResultSet rs) throws IntegrationException, SQLException{
        if(rs == null){
            throw new IntegrationException("MoneyIntegrator.generateTnxSource: Cannot populate TnxSource with null RS");
        }

        EventCoordinator ec = getEventCoordinator();
        
        TnxSource tnxS = new TnxSource();

        tnxS.setSourceID(rs.getInt("sourceid"));
        tnxS.setTitle(rs.getString("title"));
        tnxS.setDescription(rs.getString("description"));
        tnxS.setNotes(rs.getString("notes"));
        tnxS.setEventCategory(ec.getEventCategory(rs.getInt("eventcatwhenposted")));
        tnxS.setActive(rs.getBoolean("active"));
        
        return tnxS;
    }
    
    
    
    
    
    
    
    /**
     * Extracts a complete ID list of all records in the moneyledger table
     * that correspond with the object of ID you pass
     * @param den which will decide whether to query for cases or periods
     * @param bobID the primary key of either the occperiod or ocecase for which 
     * we're building a Ledger
     * @return a list, perhaps containing an ID number(s) of relevant 
     * Transaction objects
     * @throws IntegrationException for SQL errors or null/0 inputs
     */
    public List<Integer> getTransactionIDsOfLedger(DomainEnum den, int bobID) throws IntegrationException, BObStatusException{
        if(den == null || bobID == 0 || den == DomainEnum.UNIVERSAL){
            throw new IntegrationException("Cannot integrate out ID numbers with null doman enum or bob ID of 0");
        }
        
        
        StringBuilder sb = new StringBuilder("SELECT transactionid FROM public.moneyledger WHERE ");
        sb.append(den.getLedgerFKFieldString());
        sb.append("=?;");
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Connection con = null;
        
        List<Integer> tnxidl = new ArrayList<>();
        
        try {
            
            con = getPostgresCon();
            
            
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, bobID);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                tnxidl.add(rs.getInt("transactionid"));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Exception??", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return tnxidl;
        
    }
    
    public Transaction getTransaction(int trxid){
        
        String query = "";
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Connection con = null;
        
        List<CECase> cList = new ArrayList<>();
        
        try {
            
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, propID);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                cList.add();
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Exception??", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return cList;
        
    }
    
    
    
    
    
    
    
    
    // TEMPLATE!!!!!!!!!!!!
    
    public Transaction getTransaction(int trxid){
        
        String query = "";
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Connection con = null;
        
        List<CECase> cList = new ArrayList<>();
        
        try {
            
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, propID);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                cList.add();
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Exception??", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return cList;
        
    }

}