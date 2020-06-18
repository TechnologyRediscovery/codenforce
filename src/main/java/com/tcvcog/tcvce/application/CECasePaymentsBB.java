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
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.EventDomainEnum;
import com.tcvcog.tcvce.entities.MoneyCECaseFeeAssigned;
import com.tcvcog.tcvce.entities.MoneyCECaseFeePayment;
import com.tcvcog.tcvce.entities.Payment;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;

/**
 *
 * @author sylvia
 */
public  class       CECasePaymentsBB 
        extends     BackingBeanUtils
        implements  Serializable {

    private CECaseDataHeavy currentCase;
    
    private List<MoneyCECaseFeeAssigned> filteredFeeList;
    private List<MoneyCECaseFeePayment> filteredPaymentList;
    
    @PostConstruct
    public void initBean() {
        currentCase = getSessionBean().getSessCECase();
       
    }
    /**
     * Creates a new instance of CECasePayments
     */
    public CECasePaymentsBB() {
    }

       
     public String editCECasePayments(){
         
         getSessionBean().setFeeManagementDomain(EventDomainEnum.CODE_ENFORCEMENT);
         getSessionBean().setFeeManagementCeCase(currentCase);
         getSessionBean().getNavStack().pushCurrentPage();
         
         return "payments";
     }
     
     public String editOnePayment(Payment thisPayment){
         
         getSessionBean().setFeeManagementDomain(EventDomainEnum.CODE_ENFORCEMENT);
         getSessionBean().setSessPayment(thisPayment);
         getSessionBean().getNavStack().pushCurrentPage();
         
         return "payments";
     }
     
     public String editCECaseFees(){
         
         getSessionBean().setFeeManagementDomain(EventDomainEnum.CODE_ENFORCEMENT);
         getSessionBean().setFeeManagementCeCase(currentCase);
         getSessionBean().getNavStack().pushCurrentPage();
         
         return "feeManage";
     }
     
    
    /**
     * @return the currentCase
     */
    public CECaseDataHeavy getCurrentCase() {
        return currentCase;
    }

    /**
     * @param currentCase the currentCase to set
     */
    public void setCurrentCase(CECaseDataHeavy currentCase) {
        this.currentCase = currentCase;
    }

    /**
     * @return the filteredFeeList
     */
    public List<MoneyCECaseFeeAssigned> getFilteredFeeList() {
        return filteredFeeList;
    }

    /**
     * @param filteredFeeList the filteredFeeList to set
     */
    public void setFilteredFeeList(List<MoneyCECaseFeeAssigned> filteredFeeList) {
        this.filteredFeeList = filteredFeeList;
    }

    /**
     * @return the filteredPaymentList
     */
    public List<MoneyCECaseFeePayment> getFilteredPaymentList() {
        return filteredPaymentList;
    }

    /**
     * @param filteredPaymentList the filteredPaymentList to set
     */
    public void setFilteredPaymentList(List<MoneyCECaseFeePayment> filteredPaymentList) {
        this.filteredPaymentList = filteredPaymentList;
    }
    
}
