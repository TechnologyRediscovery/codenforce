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
 * This BOb represents a fee that has actually been assigned to an entity.
 * @author Nathan Dietz
 */
public class FeeAssigned extends BOb implements Serializable {

    protected int assignedFeeID;
    protected EventDomainEnum domain;
    protected List<Payment> paymentList; //All the payments that have been made on this fee
    protected User assignedBy;
    protected LocalDateTime assigned;
    protected User waivedBy;
    protected LocalDateTime lastModified;
    protected double reducedBy; //Amount the template Fee is reduced by. Subtract this from the Fee's amount field
    protected User reducedByUser;
    protected String notes;
    protected Fee fee; //The fee that has been assigned

    public FeeAssigned() {
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
    
    public List<Payment> getPaymentList() {
        return paymentList;
    }

    public void setPaymentList(List<Payment> paymentList) {
        this.paymentList = paymentList;
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