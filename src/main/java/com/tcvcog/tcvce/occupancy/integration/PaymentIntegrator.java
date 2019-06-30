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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.occupancy.integration;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Payment;
import com.tcvcog.tcvce.entities.PaymentType;
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;

/**
 *
 * @author Adam Gutonski
 */
public class PaymentIntegrator extends BackingBeanUtils implements Serializable {
    
    public PaymentIntegrator() {
        
    }
    
    public void updatePayment(Payment payment) throws IntegrationException {
        String query = "UPDATE public.payment\n" +
                    "   SET occinspec_inspectionid=?, paymenttype_typeid=?, \n" +
                    "       datereceived=?, datedeposited=?, amount=?, payerid=?, referencenum=?, \n" +
                    "       checkno=?, cleared=?, notes=?\n" +
                    " WHERE paymentid=?;";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try{
            stmt = con.prepareStatement(query);
            stmt.setInt(1, payment.getOccupancyInspectionID());
            stmt.setInt(2, payment.getPaymentType().getPaymentTypeId());
            //update date received
            if(payment.getPaymentDateReceived() != null){
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(payment.getPaymentDateReceived()));
                
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            //update date deposited
            if(payment.getPaymentDateDeposited() != null){
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(payment.getPaymentDateDeposited()));
                
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            stmt.setDouble(5, payment.getPaymentAmount());
            stmt.setInt(6, payment.getPaymentPayerID());
            stmt.setString(7, payment.getPaymentReferenceNum());
            stmt.setInt(8, payment.getCheckNum());
            stmt.setBoolean(9, payment.isCleared());
            stmt.setString(10, payment.getNotes());
            stmt.setInt(11, payment.getPaymentID());
            System.out.println("PaymentBB.editPayment | sql: " + stmt.toString());
            stmt.executeUpdate();
        } catch (SQLException ex){
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update payment", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
        
    }
    
    public ArrayList<Payment> getPaymentList() throws IntegrationException {
        String query = "SELECT paymentid, occinspec_inspectionid, paymenttype_typeid, datereceived, \n" +
                    "       datedeposited, amount, payerid, referencenum, checkno, cleared, notes\n" +
                    "  FROM public.payment;";
            Connection con = getPostgresCon();
            ResultSet rs = null;
            PreparedStatement stmt = null;
            ArrayList<Payment> paymentList = new ArrayList();
        
        try {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            System.out.println("PaymentIntegrator.getPaymentList | SQL: " + stmt.toString());
            while(rs.next()){
                paymentList.add(generatePayment(rs));
            }
            
            } catch (SQLException ex) {
                System.out.println(ex.toString());
                throw new IntegrationException("Cannot get Payment List", ex);
            } finally {
                if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
                if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
                if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
            }
            return paymentList;  
    }
    
    public void insertPayment(Payment payment) throws IntegrationException {
        String query = "INSERT INTO public.payment(\n" +
                        "            paymentid, occinspec_inspectionid, paymenttype_typeid, datereceived, \n" +
                        "            datedeposited, amount, payerid, referencenum, checkno, cleared, notes)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, ?, DEFAULT, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, payment.getOccupancyInspectionID());
            stmt.setInt(2, payment.getPaymentType().getPaymentTypeId());
            if(payment.getPaymentDateReceived() != null){
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(payment.getPaymentDateReceived()));
                
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            if(payment.getPaymentDateDeposited() != null){
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(payment.getPaymentDateDeposited()));
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            stmt.setDouble(5, payment.getPaymentAmount());
            stmt.setInt(6, payment.getPaymentPayerID());
            stmt.setString(7, payment.getPaymentReferenceNum());
            stmt.setInt(8, payment.getCheckNum());
            stmt.setString(9, payment.getNotes());
            System.out.println("PaymentIntegrator.paymentIntegrator | sql: " + stmt.toString());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert payment", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    public void deletePayment(Payment payment) throws IntegrationException {
         String query = "DELETE FROM public.payment\n" +
                        " WHERE paymentid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, payment.getPaymentID());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete payment record--probably because another"
                    + "part of the database has a reference item.", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    private Payment generatePayment(ResultSet rs) throws IntegrationException {
        Payment newPayment = new Payment();
        
        try {
            newPayment.setPaymentID(rs.getInt("paymentid"));
            newPayment.setOccupancyInspectionID(rs.getInt("occinspec_inspectionid"));
            newPayment.setPaymentType(getPaymentTypeFromPaymentTypeID(rs.getInt("paymenttype_typeid")));
            java.sql.Timestamp dateReceived = rs.getTimestamp("datereceived");
            //for effective date
            if(dateReceived != null) {
                newPayment.setPaymentDateReceived(dateReceived.toLocalDateTime());
            } else {
                newPayment.setPaymentDateReceived(null);
            }
            java.sql.Timestamp dateDeposited = rs.getTimestamp("datedeposited");
            //for expiration date
            if(dateDeposited != null) {
                newPayment.setPaymentDateDeposited(dateDeposited.toLocalDateTime());
            } else {
                newPayment.setPaymentDateDeposited(null);
            }
            newPayment.setPaymentAmount(rs.getDouble("amount"));
            newPayment.setPaymentPayerID(rs.getInt("payerid"));
            newPayment.setPaymentReferenceNum(rs.getString("referencenum"));
            newPayment.setCheckNum(rs.getInt("checkno"));
            newPayment.setCleared(rs.getBoolean("cleared"));
            newPayment.setNotes(rs.getString("notes"));
            
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error generating Payment from result set", ex);
        }
        return newPayment;
    }


    public PaymentType getPaymentTypeFromPaymentTypeID(int paymentTypeID) throws IntegrationException {
        PaymentType paymentType = new PaymentType();
        PreparedStatement stmt = null;
        Connection con = null;
        // note that paymentTypeID is not returned in this query since it is specified in the WHERE
        String query = "SELECT typeid, pmttypetitle \n" +
                    "  FROM public.paymenttype" +
                    " WHERE typeid = ?;";
        ResultSet rs = null;
 
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, paymentTypeID);
            //System.out.println("MunicipalityIntegrator.getMuniFromMuniCode | query: " + stmt.toString());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                paymentType.setPaymentTypeId(rs.getInt("typeid"));
                paymentType.setPaymentTypeTitle(rs.getString("pmttypetitle"));
                          
            }
        } catch (SQLException ex) {
            System.out.println("PaymentTypeIntegrator.getPaymentTypeFromPaymentTypeID | " + ex.toString());
            throw new IntegrationException("Exception in PaymentTypeIntegrator.getPaymentTypeFromPaymentTypeID", ex);
        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return paymentType;
        
    }
    
    public void updatePaymentType(PaymentType paymentType) throws IntegrationException {
        String query = "UPDATE public.paymenttype\n" +
                        "   SET pmttypetitle=?\n" +
                        "   WHERE typeid=?;";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try{
            stmt = con.prepareStatement(query);
            stmt.setString(1, paymentType.getPaymentTypeTitle());
            stmt.setInt(2, paymentType.getPaymentTypeId());
            System.out.println("TRYING TO EXECUTE UPDATE METHOD");
            stmt.executeUpdate();
        } catch (SQLException ex){
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update payment type", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
        
    }
    
    public ArrayList<PaymentType> getPaymentTypeList() throws IntegrationException {
            String query = "SELECT typeid, pmttypetitle\n" +
                            "  FROM public.paymenttype;";
            Connection con = getPostgresCon();
            ResultSet rs = null;
            PreparedStatement stmt = null;
            ArrayList<PaymentType> paymentTypeList = new ArrayList();
        
        try {
            stmt = con.prepareStatement(query);
            System.out.println("");
            System.out.println("TRYING TO GET PAYMENT TYPE LIST");
            rs = stmt.executeQuery();
            System.out.println("PaymentTypeIntegrator.getPaymentTypeList | SQL: " + stmt.toString());
            while(rs.next()){
                paymentTypeList.add(generatePaymentType(rs));
            }
            
            } catch (SQLException ex) {
                System.out.println(ex.toString());
                throw new IntegrationException("Cannot get payment type List", ex);
            } finally {
                if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
                if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
                if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
            }
            return paymentTypeList;   
    }
    
    public void insertPaymentType(PaymentType paymentType) throws IntegrationException {
        String query = "INSERT INTO public.paymenttype(\n" +
                    "    typeid, pmttypetitle)\n" +
                    "    VALUES (DEFAULT, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            //stmt.setInt(1, paymentType.getPaymentTypeId());
            stmt.setString(1, paymentType.getPaymentTypeTitle());
            System.out.println("PaymentTypeIntegrator.paymentTypeIntegrator | sql: " + stmt.toString());
            System.out.println("TRYING TO EXECUTE INSERT METHOD");
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert Payment Type", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    
    public void deletePaymentType(PaymentType pt) throws IntegrationException{
         String query = "DELETE FROM public.paymenttype\n" +
                        " WHERE typeid = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, pt.getPaymentTypeId());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete payment type--probably because another"
                    + "part of the database has a reference item.", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    private PaymentType generatePaymentType(ResultSet rs) throws IntegrationException {
        PaymentType newPtype = new PaymentType();
        try {
            newPtype.setPaymentTypeId(rs.getInt("typeid"));
            newPtype.setPaymentTypeTitle(rs.getString("pmttypetitle"));
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error generating Payment Type from result set", ex);
        }
        return newPtype;
    }
    
    public ArrayList<PaymentType> getPaymentTypeTitleList() throws IntegrationException{
        ArrayList<PaymentType> payTypeList = new ArrayList<>();
        
        Connection con = getPostgresCon();
        String query = "SELECT typeid FROM paymenttype;";
        ResultSet rs = null;
        Statement stmt = null;
 
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while(rs.next()){
                payTypeList.add(getPaymentTypeFromPaymentTypeID(rs.getInt("typeid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Exception in PaymentTypeIntegrator.getPaymentTypeTitleList", ex);

        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return payTypeList;
    }
    
    /* I think I built this all by accident while trying to de-bug my converter...
        public void generatePaymentTypeTitleIDMap() throws IntegrationException{
        HashMap<String, Integer> payMap = new HashMap<>();
        
        Connection con = getPostgresCon();
        String query = "SELECT typeid, pmttypetitle FROM paymenttype;";
        ResultSet rs = null;
        Statement stmt = null;
 
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while(rs.next()){
                payMap.put(rs.getString("pmttypetitle"), rs.getInt("typeid"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Exception in PaymentTypeIntegrator.generatePaymentTypeTitleIDMap", ex);

        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored * } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored * } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored *} }
        } // close finally
        
        paymentTypeMap = payMap;
    }

    /**
     * @return the paymentTypeMap
     *
    public HashMap getPaymentTypeMap() throws IntegrationException {
        generatePaymentTypeTitleIDMap();
        return paymentTypeMap;
    }

    /**
     * @param paymentTypeMap the paymentTypeMap to set
     *
    public void setPaymentTypeMap(HashMap paymentTypeMap) {
        this.paymentTypeMap = paymentTypeMap;
    }
    */
    
    
}
