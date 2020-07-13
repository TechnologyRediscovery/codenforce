/*
 * Copyright (C) 2020 Technology Rediscovery LLC.
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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nathan Dietz
 */
public class PublicInfoBundleFeeAssigned extends PublicInfoBundle {

    private FeeAssigned bundledFee;
    private List<PublicInfoBundlePayment> paymentList;
    
    private int occPerAssignedFeeID;
    private int occPeriodID;
    private int occPeriodTypeID;
    private int ceCaseAssignedFeeID;
    private int caseID;
    private int codeSetElement;
    private boolean waived;

    @Override
    public String toString() {

        return this.getClass().getName() + bundledFee.getAssignedFeeID();

    }

    public FeeAssigned getBundledFee() {
        return bundledFee;
    }
    
    public void setBundledFee(FeeAssigned input) {

        switch(input.getDomain()){
            case CODE_ENFORCEMENT:
                MoneyCECaseFeeAssigned tempCE = (MoneyCECaseFeeAssigned) input;
                ceCaseAssignedFeeID = tempCE.getCeCaseAssignedFeeID();
                caseID = tempCE.getCaseID();
                codeSetElement = tempCE.getCodeSetElement();
                
                break;
            case OCCUPANCY:
                MoneyOccPeriodFeeAssigned tempOcc = (MoneyOccPeriodFeeAssigned) input;
                occPerAssignedFeeID = tempOcc.getOccPerAssignedFeeID();
                occPeriodID = tempOcc.getOccPeriodID();
                occPeriodTypeID = tempOcc.getOccPeriodTypeID();
                break;
        }
        
        input.setPaymentList(new ArrayList<Payment>());
        
        input.setAssignedBy(new User());
        
        if (input.getWaivedBy() != null && input.getWaivedBy().getUserID() != 0){
            waived = true;
        }
        
        input.setWaivedBy(new User());
        
        input.setLastModified(LocalDateTime.MIN);
        
        input.setReducedByUser(new User());
        
        input.setNotes("*****");
        
        bundledFee = input;
    }

    public int getOccPerAssignedFeeID() {
        return occPerAssignedFeeID;
    }

    public void setOccPerAssignedFeeID(int occPerAssignedFeeID) {
        this.occPerAssignedFeeID = occPerAssignedFeeID;
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

    public int getCeCaseAssignedFeeID() {
        return ceCaseAssignedFeeID;
    }

    public void setCeCaseAssignedFeeID(int ceCaseAssignedFeeID) {
        this.ceCaseAssignedFeeID = ceCaseAssignedFeeID;
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

    public List<PublicInfoBundlePayment> getPaymentList() {
        return paymentList;
    }

    public void setPaymentList(List<PublicInfoBundlePayment> paymentList) {
        this.paymentList = paymentList;
    }

    public boolean isWaived() {
        return waived;
    }

    public void setWaived(boolean waived) {
        this.waived = waived;
    }

}
