/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.money.entities;

import com.tcvcog.tcvce.entities.DomainEnum;
import java.util.List;


/**
 * Represents all transactions on either a Permit File (occperiod) or a CE Case (cecase)
 * @author sylvia
 */
public class MoneyLedger {
    
    private DomainEnum domain;
    
    private int caseID; 
    private int periodID;
    
    private List<Transaction> transactions;

    /**
     * @return the domain
     */
    public DomainEnum getDomain() {
        return domain;
    }

    /**
     * @return the caseID
     */
    public int getCaseID() {
        return caseID;
    }

    /**
     * @return the periodID
     */
    public int getPeriodID() {
        return periodID;
    }

    /**
     * @return the transactions
     */
    public List<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * @param domain the domain to set
     */
    public void setDomain(DomainEnum domain) {
        this.domain = domain;
    }

    /**
     * @param caseID the caseID to set
     */
    public void setCaseID(int caseID) {
        this.caseID = caseID;
    }

    /**
     * @param periodID the periodID to set
     */
    public void setPeriodID(int periodID) {
        this.periodID = periodID;
    }

    /**
     * @param transactions the transactions to set
     */
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
    
}
