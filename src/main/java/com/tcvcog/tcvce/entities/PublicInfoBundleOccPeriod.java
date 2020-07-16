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

import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author Nathan Dietz
 */
public class PublicInfoBundleOccPeriod extends PublicInfoBundle {

    
    private OccPeriod bundledPeriod;
    private List<PublicInfoBundlePerson> personList;
    private List<PublicInfoBundleOccInspection> inspectionList;
    private List<PublicInfoBundleFeeAssigned> feeList;
    private List<PublicInfoBundlePayment> paymentList;

    @Override
    public String toString() {

        return this.getClass().getName() + bundledPeriod.getPeriodID();
    }

    public OccPeriod getBundledPeriod() {
        return bundledPeriod;
    }
    
    public void setBundledPeriod(OccPeriod input) {
        
        setCaseManager(input.getManager());
        input.setManager(new User());
        input.setPeriodTypeCertifiedBy(new User());
        input.setPeriodTypeCertifiedTS(LocalDateTime.MIN);
        
        input.setSource(new BOBSource());
        input.setCreatedBy(new User());
        input.setCreatedTS(LocalDateTime.MIN);
        
        input.setStartDateCertifiedTS(LocalDateTime.MIN);
        input.setStartDateCertifiedBy(new User());
        input.setEndDateCertifiedTS(LocalDateTime.MIN);
        input.setEndDateCertifiedBy(new User());
        input.setNotes("*****");
        
        bundledPeriod = input;
    }

    public List<PublicInfoBundlePerson> getPersonList() {
        return personList;
    }

    public void setPersonList(List<PublicInfoBundlePerson> personList) {
        this.personList = personList;
    }

    public List<PublicInfoBundleOccInspection> getInspectionList() {
        return inspectionList;
    }

    public void setInspectionList(List<PublicInfoBundleOccInspection> inspectionList) {
        this.inspectionList = inspectionList;
    }

    public List<PublicInfoBundleFeeAssigned> getFeeList() {
        return feeList;
    }

    public void setFeeList(List<PublicInfoBundleFeeAssigned> feeList) {
        this.feeList = feeList;
    }

    public List<PublicInfoBundlePayment> getPaymentList() {
        return paymentList;
    }

    public void setPaymentList(List<PublicInfoBundlePayment> paymentList) {
        this.paymentList = paymentList;
    }
    
    

}
