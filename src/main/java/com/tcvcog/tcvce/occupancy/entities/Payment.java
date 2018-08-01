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
package com.tcvcog.tcvce.occupancy.entities;

import java.time.LocalDateTime;
/**
 *
 * @author Adam Gutonski
 */
public class Payment {
    
    private int paymentID;
    private int occupancyInspectionID;
    private PaymentType paymentType;
    private LocalDateTime paymentDateDeposited;
    private LocalDateTime paymentDateReceived;
    private double paymentAmount;
    private int paymentPayerID;
    private String paymentReferenceNum;
    private int checkNum;
    private boolean cleared;
    private String notes;

   

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
     * @return the paymentDateDeposited
     */
    public LocalDateTime getPaymentDateDeposited() {
        return paymentDateDeposited;
    }

    /**
     * @param paymentDateDeposited the paymentDateDeposited to set
     */
    public void setPaymentDateDeposited(LocalDateTime paymentDateDeposited) {
        this.paymentDateDeposited = paymentDateDeposited;
    }

    /**
     * @return the paymentDateReceived
     */
    public LocalDateTime getPaymentDateReceived() {
        return paymentDateReceived;
    }

    /**
     * @param paymentDateReceived the paymentDateReceived to set
     */
    public void setPaymentDateReceived(LocalDateTime paymentDateReceived) {
        this.paymentDateReceived = paymentDateReceived;
    }

    /**
     * @return the paymentAmount
     */
    public double getPaymentAmount() {
        return paymentAmount;
    }

    /**
     * @param paymentAmount the paymentAmount to set
     */
    public void setPaymentAmount(double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    /**
     * @return the paymentReferenceNum
     */
    public String getPaymentReferenceNum() {
        return paymentReferenceNum;
    }

    /**
     * @param paymentReferenceNum the paymentReferenceNum to set
     */
    public void setPaymentReferenceNum(String paymentReferenceNum) {
        this.paymentReferenceNum = paymentReferenceNum;
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
    public int getPaymentPayerID() {
        return paymentPayerID;
    }

    /**
     * @param paymentPayerID the paymentPayerID to set
     */
    public void setPaymentPayerID(int paymentPayerID) {
        this.paymentPayerID = paymentPayerID;
    }

    /**
     * unused methods (for now...)
    
    public int getPaymentPaymentTypeID() {
        return getPaymentType();
    }
    
    public void setPaymentPaymentTypeID(int paymentPaymentTypeID) {
        this.setPaymentType(paymentPaymentTypeID);
    }
    */

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
    
    
}
