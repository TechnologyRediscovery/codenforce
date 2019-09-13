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

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author Nathan Dietz
 */
public class FeeAssigned  extends EntityUtils implements Serializable {

    private List<Payment> paymentList;
    private int moneyFeeAssigned;
    private User assignedBy;
    private LocalDateTime assigned;
    private User waivedBy;
    private LocalDateTime lastModified;
    private double reducedBy;
    private User reducedByUser;
    private String notes;
    private Fee fee;

    public List<Payment> getPaymentList() {
        return paymentList;
    }

    public void setPaymentList(List<Payment> paymentList) {
        this.paymentList = paymentList;
    }

    public int getMoneyFeeAssigned() {
        return moneyFeeAssigned;
    }

    public void setMoneyFeeAssigned(int moneyFeeAssigned) {
        this.moneyFeeAssigned = moneyFeeAssigned;
    }

    public User getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(User assignedBy) {
        this.assignedBy = assignedBy;
    }

    public User getWaivedBy() {
        return waivedBy;
    }

    public void setWaivedBy(User waivedBy) {
        this.waivedBy = waivedBy;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public double getReducedBy() {
        return reducedBy;
    }

    public void setReducedBy(double reducedBy) {
        this.reducedBy = reducedBy;
    }

    public User getReducedByUser() {
        return reducedByUser;
    }

    public void setReducedByUser(User reducedByUser) {
        this.reducedByUser = reducedByUser;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Fee getFee() {
        return fee;
    }

    public void setFee(Fee fee) {
        this.fee = fee;
    }

    public LocalDateTime getAssigned() {
        return assigned;
    }

    public void setAssigned(LocalDateTime assigned) {
        this.assigned = assigned;
    }
        
    
}
