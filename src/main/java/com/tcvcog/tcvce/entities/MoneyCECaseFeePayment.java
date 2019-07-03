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
public class MoneyCECaseFeePayment {

    private int moneyPaymentID;
    private int CECaseAssignedFeeID;
    
    public MoneyCECaseFeePayment() {
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
