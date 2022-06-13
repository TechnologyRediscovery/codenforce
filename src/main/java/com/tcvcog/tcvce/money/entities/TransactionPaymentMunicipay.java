/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.money.entities;

/**
 * Represents a payment made on an account through third party Municipay
 * 
 * @author sylvia
 */
public class TransactionPaymentMunicipay
        extends TransactionPayment {
    
    private int recordID;
    private String municipayReferenceNo;
    private String municipayReplyPayload;
    
    
    
}
