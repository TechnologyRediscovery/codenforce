/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.money.entities;

import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.IFace_trackedEntityLink;
import com.tcvcog.tcvce.entities.LinkedObjectRole;
import com.tcvcog.tcvce.entities.MailingAddressLink;
import java.time.LocalDateTime;

/**
 * Concrete implementation of a TransactionPayment representing a human
 * handing a check to another person or mailing it in for application to 
 * a ledger
 * @author Ellen Bascomb of Apartment 31Y
 */
public  class       TransactionPaymentCheck
        extends     TransactionPayment {
    
    
    private int checkID;
    private int checkNo;
    private String bankName;
    private MailingAddressLink mailingAddressOnCheck;
    
   
    public TransactionPaymentCheck(){
    
    }
    
    public TransactionPaymentCheck(TransactionPayment trx){
        super(trx);
        
    }
    
    /**
     * @return the checkID
     */
    public int getCheckID() {
        return checkID;
    }

    /**
     * @return the checkNo
     */
    public int getCheckNo() {
        return checkNo;
    }

    /**
     * @return the bankName
     */
    public String getBankName() {
        return bankName;
    }

    /**
     * @return the mailingAddressOnCheck
     */
    public MailingAddressLink getMailingAddressOnCheck() {
        return mailingAddressOnCheck;
    }

    /**
     * @param checkID the checkID to set
     */
    public void setCheckID(int checkID) {
        this.checkID = checkID;
    }

    /**
     * @param checkNo the checkNo to set
     */
    public void setCheckNo(int checkNo) {
        this.checkNo = checkNo;
    }

    /**
     * @param bankName the bankName to set
     */
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    /**
     * @param mailingAddressOnCheck the mailingAddressOnCheck to set
     */
    public void setMailingAddressOnCheck(MailingAddressLink mailingAddressOnCheck) {
        this.mailingAddressOnCheck = mailingAddressOnCheck;
    }
    
    
    
    
    
}
