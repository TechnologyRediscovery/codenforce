/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.application.SessionBean;
import com.tcvcog.tcvce.entities.MoneyOccPeriodFeeAssigned;
import com.tcvcog.tcvce.entities.Payment;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import java.util.List;
import javax.annotation.PostConstruct;

/**
 *
 * @author sylvia
 */
public  class   OccPeriodPaymentsBB 
        extends BackingBeanUtils{
    
    private OccPeriodDataHeavy currentOccPeriod;
    
       // payments
    private List<Payment> filteredPaymentList;
    private Payment selectedPayment;
    
    //fees
    private List<MoneyOccPeriodFeeAssigned> filteredFeeList;
    private MoneyOccPeriodFeeAssigned selectedFee;
    
     
    @PostConstruct
    public void initBean() {
        SessionBean sb = getSessionBean();
       currentOccPeriod = sb.getSessOccPeriod();
       
    }
    

    /**
     * Creates a new instance of OccPeriodPayments
     */
    public OccPeriodPaymentsBB() {
    }

    /**
     * @return the currentOccPeriod
     */
    public OccPeriodDataHeavy getCurrentOccPeriod() {
        return currentOccPeriod;
    }

    /**
     * @param currentOccPeriod the currentOccPeriod to set
     */
    public void setCurrentOccPeriod(OccPeriodDataHeavy currentOccPeriod) {
        this.currentOccPeriod = currentOccPeriod;
    }

    /**
     * @return the filteredPaymentList
     */
    public List<Payment> getFilteredPaymentList() {
        return filteredPaymentList;
    }

    /**
     * @return the selectedPayment
     */
    public Payment getSelectedPayment() {
        return selectedPayment;
    }

    /**
     * @return the filteredFeeList
     */
    public List<MoneyOccPeriodFeeAssigned> getFilteredFeeList() {
        return filteredFeeList;
    }

    /**
     * @return the selectedFee
     */
    public MoneyOccPeriodFeeAssigned getSelectedFee() {
        return selectedFee;
    }

    /**
     * @param filteredPaymentList the filteredPaymentList to set
     */
    public void setFilteredPaymentList(List<Payment> filteredPaymentList) {
        this.filteredPaymentList = filteredPaymentList;
    }

    /**
     * @param selectedPayment the selectedPayment to set
     */
    public void setSelectedPayment(Payment selectedPayment) {
        this.selectedPayment = selectedPayment;
    }

    /**
     * @param filteredFeeList the filteredFeeList to set
     */
    public void setFilteredFeeList(List<MoneyOccPeriodFeeAssigned> filteredFeeList) {
        this.filteredFeeList = filteredFeeList;
    }

    /**
     * @param selectedFee the selectedFee to set
     */
    public void setSelectedFee(MoneyOccPeriodFeeAssigned selectedFee) {
        this.selectedFee = selectedFee;
    }
    
}
