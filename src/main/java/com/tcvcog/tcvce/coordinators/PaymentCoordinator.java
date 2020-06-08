/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
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
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.EventDomainEnum;
import com.tcvcog.tcvce.entities.Fee;
import com.tcvcog.tcvce.entities.FeeAssigned;
import com.tcvcog.tcvce.entities.MoneyCECaseFeeAssigned;
import com.tcvcog.tcvce.entities.MoneyOccPeriodFeeAssigned;
import com.tcvcog.tcvce.entities.Payment;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.occupancy.integration.PaymentIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;

/**
 * Implements business logic related to payments.
 *
 * @author Nathan Dietz and SYLVIA
 */
public class PaymentCoordinator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of PaymentCoordinator
     */
    public PaymentCoordinator() {
    }

    /*
    public OccPeriod configureOccPeriodFees(OccPeriod op){
        // look up the set of fees associated with 
    }
     */
    /**
     * Container for business logic related to an individual Payment instance
     *
     * @param pmt
     * @return
     */
    public Payment configurePayment(Payment pmt) {

        return pmt;

    }

    public void insertAutoAssignedFees(OccPeriod period) throws IntegrationException {

        PaymentIntegrator pi = getPaymentIntegrator();

        ArrayList<Fee> feeList = (ArrayList<Fee>) pi.getFeeList(period.getType());

        for (Fee fee : feeList) {

            if (fee.isAutoAssigned()) {

                MoneyOccPeriodFeeAssigned skeleton = new MoneyOccPeriodFeeAssigned();

                skeleton.setOccPeriodID(period.getPeriodID());
                skeleton.setOccPeriodTypeID(period.getType().getTypeID());
                skeleton.setMoneyFeeAssigned(fee.getOccupancyInspectionFeeID());
                skeleton.setAssignedBy(getSessionBean().getSessUser());
                skeleton.setAssigned(LocalDateTime.now());
                skeleton.setLastModified(LocalDateTime.now());
                skeleton.setNotes("Automatically assigned");
                skeleton.setFee(fee);

                pi.insertOccPeriodFee(skeleton);

            }

        }

    }

    public void insertAutoAssignedFees(CECase cse, CodeViolation violation) throws IntegrationException {

        PaymentIntegrator pi = getPaymentIntegrator();

        EnforcableCodeElement codeElement = violation.getCodeViolated();

        ArrayList<Fee> feeList = (ArrayList<Fee>) codeElement.getFeeList();

        for (Fee fee : feeList) {

            if (fee.isAutoAssigned()) {

                MoneyCECaseFeeAssigned skeleton = new MoneyCECaseFeeAssigned();

                skeleton.setCaseID(cse.getCaseID());
                skeleton.setCodeSetElement(codeElement.getCodeSetElementID());
                skeleton.setMoneyFeeAssigned(fee.getOccupancyInspectionFeeID());
                skeleton.setAssignedBy(getSessionBean().getSessUser());
                skeleton.setAssigned(LocalDateTime.now());
                skeleton.setLastModified(LocalDateTime.now());
                skeleton.setNotes("Automatically assigned");
                skeleton.setFee(fee);

                pi.insertCECaseFee(skeleton);

            }

        }

    }

    public List<FeeAssigned> getOccPeriodAssignedFees(OccPeriod currentOccPeriod) {

        PaymentIntegrator pi = getPaymentIntegrator();
        ArrayList<FeeAssigned> skeletonHorde = new ArrayList<>();

        try {
            ArrayList<MoneyOccPeriodFeeAssigned> tempList = (ArrayList<MoneyOccPeriodFeeAssigned>) pi.getFeeAssigned(currentOccPeriod);

            for (MoneyOccPeriodFeeAssigned fee : tempList) {

                FeeAssigned skeleton = fee;

                skeleton.setAssignedFeeID(fee.getOccPerAssignedFeeID());
                skeleton.setDomain(EventDomainEnum.OCCUPANCY);
                skeletonHorde.add(skeleton);

            }
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Oops! We encountered a problem trying to fetch the fee assigned list!", ""));
            System.out.println("PaymentCoordinator.getOccPeriodAssignedFees | ERROR: " + ex.toString());
        }

        return skeletonHorde;

    }

    public List<FeeAssigned> getCECaseAssignedFees(CECase currentCase) {
        PaymentIntegrator pi = getPaymentIntegrator();
        ArrayList<FeeAssigned> skeletonHorde = new ArrayList<>();

        try {
            List<MoneyCECaseFeeAssigned> tempList = (ArrayList<MoneyCECaseFeeAssigned>) pi.getFeeAssigned(currentCase);

            for (MoneyCECaseFeeAssigned fee : tempList) {

                FeeAssigned skeleton = fee;

                skeleton.setAssignedFeeID(fee.getCeCaseAssignedFeeID());
                skeleton.setDomain(EventDomainEnum.CODE_ENFORCEMENT);
                skeletonHorde.add(skeleton);

            }
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Oops! We encountered a problem trying to fetch the fee assigned list!", ""));
            System.out.println("PaymentCoordinator.getCECaseAssignedFees | ERROR: " + ex.toString());
        }

        return skeletonHorde;

    }

    public ArrayList<Payment> getAllPayments() {
        PaymentIntegrator pi = getPaymentIntegrator();
        try {
            return pi.getPaymentList();
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to load payment list",
                            "This must be corrected by the system administrator"));
            System.out.println("PaymentCoordinator.getAllPayments | ERROR: " + ex.toString());

        }
        
        return new ArrayList<>();

    }

}
