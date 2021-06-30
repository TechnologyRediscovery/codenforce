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
public class FeeAssigned extends Fee implements Serializable {

    // NOTE: It is probably better to use the fee ID for both feeassigned and fee. I don't see any reason
    // to make them separate? So much work to do with fees!
    private int assignedFeeID;

    private DomainEnum domain;
    // For occ (apparently)
    private int occPeriodID;
    private int occPeriodTypeID;

    // For CE (apparently)
    private int caseID;
    private int codeSetElement;

    private List<Payment> paymentList; //All the payments that have been made on this fee

    private User assignedBy;
    private LocalDateTime assigned;

    private User waivedBy;

    private LocalDateTime lastModified;

    private double reducedBy; //Amount the template Fee is reduced by. Subtract this from the Fee's amount field
    private User reducedByUser;

    public FeeAssigned() {
    }

    public FeeAssigned(Fee fee) {
        super(fee);
    }

    public FeeAssigned(FeeAssigned feeAssigned) {
        super(feeAssigned);

        this.domain = feeAssigned.getDomain();

        this.occPeriodID = feeAssigned.getOccPeriodID();
        this.occPeriodTypeID = feeAssigned.getOccPeriodTypeID();

        this.caseID = feeAssigned.getCaseID();
        this.codeSetElement = feeAssigned.getCodeSetElement();

        this.paymentList = feeAssigned.getPaymentList();

        this.assignedBy = feeAssigned.getAssignedBy();
        this.assigned = feeAssigned.getAssigned();

        this.waivedBy = feeAssigned.getWaivedBy();

        this.lastModified = feeAssigned.getLastModified();

        this.reducedBy = feeAssigned.getReducedBy();
        this.reducedByUser = feeAssigned.getReducedByUser();

    }
        
    public int getAssignedFeeID() {
        return assignedFeeID;
    }

    public void setAssignedFeeID(int assignedFeeID) {
        this.assignedFeeID = assignedFeeID;
    }

    public DomainEnum getDomain() {
        return domain;
    }

    public void setDomain(DomainEnum domain) {
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

    public LocalDateTime getAssigned() {
        return assigned;
    }

    public void setAssigned(LocalDateTime assigned) {
        this.assigned = assigned;
    }

    public int getOccPeriodID() {
        return occPeriodID;
    }

    public void setOccPeriodID(int occPeriodID) {
        this.occPeriodID = occPeriodID;
    }

    public int getOccPeriodTypeID() {
        return occPeriodTypeID;
    }

    public void setOccPeriodTypeID(int occPeriodTypeID) {
        this.occPeriodTypeID = occPeriodTypeID;
    }

    public int getCaseID() {
        return caseID;
    }

    public void setCaseID(int caseID) {
        this.caseID = caseID;
    }

    public int getCodeSetElement() {
        return codeSetElement;
    }

    public void setCodeSetElement(int codeSetElement) {
        this.codeSetElement = codeSetElement;
    }

}