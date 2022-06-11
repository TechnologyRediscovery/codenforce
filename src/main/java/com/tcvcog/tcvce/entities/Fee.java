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

import com.tcvcog.tcvce.util.DateTimeUtil;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * A fee template that may be applied to an entity
 * @author Nathan Dietz
 */
public class Fee extends BOb {
    private int feeID;
    private Municipality muni;

    private String name;
    private double amount;

    private LocalDateTime effectiveDate;
    private LocalDateTime expiryDate;

    private String notes;
    private boolean autoAssigned;

    public Fee() {
        effectiveDate = LocalDateTime.now();
        expiryDate = LocalDateTime.now();
    }

    public Fee(Fee fee) {
        this.feeID = fee.getFeeID();
        this.muni = fee.getMuni();

        this.name = fee.getName();
        this.amount = fee.getAmount();

        this.effectiveDate = fee.getEffectiveDate();
        this.expiryDate = fee.getExpiryDate();

        this.notes = fee.getNotes();
        this.autoAssigned = fee.isAutoAssigned();
    }
    
    /**
     * @return the feeID
     */
    public int getFeeID() {
        return feeID;
    }

    /**
     * @param feeID the feeID to set
     */
    public void setFeeID(int feeID) {
        this.feeID = feeID;
    }

    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDateTime effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

   
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

       public boolean isAutoAssigned() {
        return autoAssigned;
    }

    public void setAutoAssigned(boolean autoAssigned) {
        this.autoAssigned = autoAssigned;
    }

}
