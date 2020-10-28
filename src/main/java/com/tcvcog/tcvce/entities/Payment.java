
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
package com.tcvcog.tcvce.entities;

import java.time.LocalDateTime;
import java.util.Date;
/**
 *
 * @author Adam Gutonski & Nathan Dietz
 */
public class Payment extends BOb {
    
    protected int paymentID;
    protected PaymentType paymentType;
    protected LocalDateTime dateDeposited;
    protected LocalDateTime dateReceived;
    protected double amount;
    protected Person payer;
    protected String referenceNum;
    protected int checkNum;
    protected boolean cleared;
    protected String notes;
    protected User recordedBy;
    protected LocalDateTime entryTimestamp;
    protected int assignedFeeID;
    protected EventDomainEnum domain;
    
   public Payment() {
       
       notes = " ";
       
       payer = new Person();
       
       dateReceived = LocalDateTime.now();
       
       dateDeposited = LocalDateTime.now();
       
       paymentType = new PaymentType();
       
       paymentType.setPaymentTypeId(2);
       
   }
   
   public Payment(MoneyCECaseFeePayment p){
       
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
   
   public Payment(MoneyOccPeriodFeePayment p){
       
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

    /**
     * @return the dateDeposited
     */
    public LocalDateTime getDateDeposited() {
        return dateDeposited;
    }

    /**
     * @param dateDeposited the dateDeposited to set
     */
    public void setDateDeposited(LocalDateTime dateDeposited) {
        this.dateDeposited = dateDeposited;
    }

    /**
     * @return the dateReceived
     */
    public LocalDateTime getDateReceived() {
        return dateReceived;
    }

    /**
     * @param dateReceived the dateReceived to set
     */
    public void setDateReceived(LocalDateTime dateReceived) {
        this.dateReceived = dateReceived;
    }

    /**
     * @return the dateDeposited
     */
    public Date getDateDepositedUtilDate() {
        return convertUtilDate(dateDeposited);
    }

    /**
     * @param dateDeposited the dateDeposited to set
     */
    public void setDateDepositedUtilDate(Date dateDeposited) {
        this.dateDeposited = convertUtilDate(dateDeposited);
    }

    /**
     * @return the dateReceived
     */
    public Date getDateReceivedUtilDate() {
        return convertUtilDate(dateReceived);
    }

    /**
     * @param dateReceived the dateReceived to set
     */
    public void setDateReceivedUtilDate(Date dateReceived) {
        this.dateReceived = convertUtilDate(dateReceived);
    }
    
    /**
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * @return the referenceNum
     */
    public String getReferenceNum() {
        return referenceNum;
    }

    /**
     * @param referenceNum the referenceNum to set
     */
    public void setReferenceNum(String referenceNum) {
        this.referenceNum = referenceNum;
    }

    /**
     * @return the checkNum
     */
    public int getCheckNum() {
        return checkNum;
    }

    /**
     * @param checkNum the checkNum to set
     */
    public void setCheckNum(int checkNum) {
        this.checkNum = checkNum;
    }

    /**
     * @return the cleared
     */
    public boolean isCleared() {
        return cleared;
    }

    /**
     * @param cleared the cleared to set
     */
    public void setCleared(boolean cleared) {
        this.cleared = cleared;
    }

    /**
     * @return the paymentID
     */
    public int getPaymentID() {
        return paymentID;
    }

    /**
     * @param paymentID the paymentID to set
     */
    public void setPaymentID(int paymentID) {
        this.paymentID = paymentID;
    }

    /**
     * @return the payment Payer
     */
    public Person getPayer() {
        return payer;
    }

    /**
     * @param payer The person object of the payer.
     */
    public void setPayer(Person payer) {
        this.payer = payer;
    }

    /**
     * @return the paymentType
     */
    public PaymentType getPaymentType() {
        return paymentType;
    }

    /**
     * @param paymentType the paymentType to set
     */
    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public User getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(User recordedBy) {
        this.recordedBy = recordedBy;
    }

    public LocalDateTime getEntryTimestamp() {
        return entryTimestamp;
    }

    public void setEntryTimestamp(LocalDateTime entryTimestamp) {
        this.entryTimestamp = entryTimestamp;
    }

    public int getAssignedFeeID() {
        return assignedFeeID;
    }

    public void setAssignedFeeID(int assignedFeeID) {
        this.assignedFeeID = assignedFeeID;
    }

    public EventDomainEnum getDomain() {
        return domain;
    }

    public void setDomain(EventDomainEnum domain) {
        this.domain = domain;
    }
}
