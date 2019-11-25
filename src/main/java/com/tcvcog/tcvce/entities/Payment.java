
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
/**
 *
 * @author Adam Gutonski
 */
public class Payment {
    
    private int paymentID;
    private int occupancyInspectionID;
    private PaymentType paymentType;
    private LocalDateTime dateDeposited;
    private LocalDateTime dateReceived;
    private double amount;
    private Person payer;
    private String referenceNum;
    private int checkNum;
    private boolean cleared;
    private String notes;
    private User recordedBy;
    private LocalDateTime entryTimestamp;
    private int assignedFeeID;
    private FeeAssignedType assignedTo;
    
   public Payment() {
       
       notes = " ";
       
       payer = new Person();
       
       dateReceived = LocalDateTime.now();
       
       dateDeposited = LocalDateTime.now();
       
       paymentType = new PaymentType();
       
       paymentType.setPaymentTypeId(2);
       
   }

    /**
     * @return the occupancyInspectionID
     */
    public int getOccupancyInspectionID() {
        return occupancyInspectionID;
    }

    /**
     * @param occupancyInspectionID the occupancyInspectionID to set
     */
    public void setOccupancyInspectionID(int occupancyInspectionID) {
        this.occupancyInspectionID = occupancyInspectionID;
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
     * @return the paymentPayerID
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

    public FeeAssignedType getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(FeeAssignedType assignedTo) {
        this.assignedTo = assignedTo;
    }
}
