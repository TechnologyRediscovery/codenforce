/*
 * Copyright (C) 2018 Technology Rediscovery, LLC.
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
package com.tcvcog.tcvce.entities;

/**
 *
 * @author Nathan Dietz
 */
public class MoneyCECaseFeePayment 
        extends Payment{

    private int moneyPaymentID;
    private int CECaseAssignedFeeID;
    
    public MoneyCECaseFeePayment() {
    }
    
    public MoneyCECaseFeePayment(Payment p){
        this.CECaseAssignedFeeID = p.getAssignedFeeID();
        this.moneyPaymentID = p.getPaymentID();
        this.paymentID = p.getPaymentID();
        this.paymentType = p.getPaymentType();
        this.dateDeposited = p.getDateDeposited();
        this.dateReceived = p.getDateReceived();
        this.amount = p.getAmount();
        this.payer = p.getPayer();
        this.referenceNum = p.getReferenceNum();
        this.checkNum = p.getCheckNum();
        this.cleared = p.isCleared();
        this.notes = p.getNotes();
        this.recordedBy = p.getRecordedBy();
        this.entryTimestamp = p.getEntryTimestamp();
        this.assignedFeeID = p.getAssignedFeeID();
        this.domain = p.getDomain();
        
    }

    public int getMoneyPaymentID() {
        return moneyPaymentID;
    }

    public void setMoneyPaymentID(int moneyPaymentID) {
        this.moneyPaymentID = moneyPaymentID;
    }

    public int getCECaseAssignedFeeID() {
        return CECaseAssignedFeeID;
    }

    public void setCECaseAssignedFeeID(int CECaseAssignedFeeID) {
        this.CECaseAssignedFeeID = CECaseAssignedFeeID;
    }
    
    
    
}
