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
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

/**
 *
 * @author Nathan Dietz
 */
public class MoneyCECaseFeeAssigned extends FeeAssigned{
   
   private int ceCaseAssignedFeeID;
   private int caseID;
   private int codeSetElement;
    
    public MoneyCECaseFeeAssigned() {
    }
    
    public MoneyCECaseFeeAssigned(FeeAssigned fee) {
        
        this.ceCaseAssignedFeeID = fee.assignedFeeID;
        this.assignedFeeID = fee.assignedFeeID;
        this.domain = EventDomainEnum.CODE_ENFORCEMENT;
        this.paymentList = fee.paymentList;
        this.moneyFeeAssigned = fee.moneyFeeAssigned;
        this.assignedBy = fee.assignedBy;
        this.assigned = fee.assigned;
        this.waivedBy = fee.waivedBy;
        this.lastModified = fee.lastModified;
        this.reducedBy = fee.reducedBy;
        this.reducedByUser = fee.reducedByUser;
        this.notes = fee.notes;
        this.fee = fee.fee;
        
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

    public int getCeCaseAssignedFeeID() {
        return ceCaseAssignedFeeID;
    }

    public void setCeCaseAssignedFeeID(int ceCaseAssignedFeeID) {
        this.ceCaseAssignedFeeID = ceCaseAssignedFeeID;
    }
    
}