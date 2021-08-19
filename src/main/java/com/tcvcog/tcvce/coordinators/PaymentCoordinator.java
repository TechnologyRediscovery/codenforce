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
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.DomainEnum;
import com.tcvcog.tcvce.entities.Fee;
import com.tcvcog.tcvce.entities.FeeAssigned;
import com.tcvcog.tcvce.entities.Payment;
import com.tcvcog.tcvce.entities.PaymentType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import com.tcvcog.tcvce.occupancy.integration.PaymentIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Container for business logic related to an individual Payment instance
     *
     * @param pmt
     * @return
     */
    public Payment configurePayment(Payment pmt) {

        return pmt;

    }

    /**
     * This method checks if any Fees associated with a certain type of OccPeriod
     * are to be automatically assigned to it. If so, it assigns them.
     * @param period
     * @throws IntegrationException 
     */
    public void insertAutoAssignedFees(OccPeriod period) throws IntegrationException {

        PaymentIntegrator pi = getPaymentIntegrator();

        ArrayList<Fee> feeList = (ArrayList<Fee>) pi.getFeeList(period.getType());

        for (Fee fee : feeList) {

            if (fee.isAutoAssigned()) {

                FeeAssigned skeleton = new FeeAssigned(fee);

                skeleton.setDomain(DomainEnum.OCCUPANCY);
                skeleton.setOccPeriodID(period.getPeriodID());
                skeleton.setOccPeriodTypeID(period.getType().getTypeID());
                skeleton.setAssignedBy(getSessionBean().getSessUser());
                skeleton.setAssigned(LocalDateTime.now());
                skeleton.setLastModified(LocalDateTime.now());
                skeleton.setNotes("Automatically assigned");

                pi.insertOccPeriodFee(skeleton);

            }

        }

    }

    /**
     * This method checks if any Fees associated with a certain type of CodeViolation
     * are to be automatically assigned to it. If so, it assigns them.
     * @param cse
     * @param violation
     * @throws IntegrationException 
     */
    public void insertAutoAssignedFees(CECase cse, CodeViolation violation) throws IntegrationException {

        PaymentIntegrator pi = getPaymentIntegrator();

        EnforcableCodeElement codeElement = violation.getCodeViolated();

        ArrayList<Fee> feeList = (ArrayList<Fee>) codeElement.getFeeList();

        for (Fee fee : feeList) {

            if (fee.isAutoAssigned()) {

                FeeAssigned skeleton = new FeeAssigned(fee);

                skeleton.setDomain(DomainEnum.CODE_ENFORCEMENT);
                skeleton.setCaseID(cse.getCaseID());
                skeleton.setCodeSetElement(codeElement.getCodeSetElementID());
                skeleton.setAssignedBy(getSessionBean().getSessUser());
                skeleton.setAssigned(LocalDateTime.now());
                skeleton.setLastModified(LocalDateTime.now());
                skeleton.setNotes("Automatically assigned");

                pi.insertCECaseFee(skeleton);

            }

        }

    }

    /**
     * Accepts a FeeAssigned object, validates the input to make sure it is
     * acceptable, sanitizes it, then tosses it back.
     *
     * @param input
     * @param waived Whether or not the fee is waived.
     * @return
     * @throws BObStatusException if the input is not acceptable.
     */
    public FeeAssigned sanitizeAssignedFee(FeeAssigned input, boolean waived) throws BObStatusException {

        if (input == null) {
            throw new BObStatusException("Please select a fee to assign");
        }

        if (waived == true) {
            input.setWaivedBy(getSessionBean().getSessUser());
        } else {
            input.setWaivedBy(new User());
        }

        if (input.getReducedBy() > 0) {
            input.setReducedByUser(getSessionBean().getSessUser());
        } else if (input.getReducedBy() < 0) {
            throw new BObStatusException("You cannot reduce a fee by a negative number.");
        } else {
            input.setReducedByUser(new User());
        }

        return input;

    }

    public void insertAssignedFee(FeeAssigned input, OccPeriod currentOccPeriod, boolean waived) throws IntegrationException, BObStatusException {

        input = sanitizeAssignedFee(input, waived);

        input.setDomain(DomainEnum.OCCUPANCY);
        input.setOccPeriodID(currentOccPeriod.getPeriodID());
        input.setOccPeriodTypeID(currentOccPeriod.getType().getTypeID());

        PaymentIntegrator pi = getPaymentIntegrator();
        pi.insertOccPeriodFee(input);

    }

    public void insertAssignedFee(FeeAssigned input, CECase inputCase, CodeViolation violation, boolean waived) throws IntegrationException, BObStatusException {

        input = sanitizeAssignedFee(input, waived);

        PaymentIntegrator pi = getPaymentIntegrator();

        input.setDomain(DomainEnum.CODE_ENFORCEMENT);
        input.setCaseID(inputCase.getCaseID());
        input.setCodeSetElement(violation.getCodeViolated().getCodeSetElementID());

        pi.insertCECaseFee(input);

    }

    public void updateAssignedFee(FeeAssigned input, CECase currentCase, CodeViolation selectedViolation, boolean waived) throws BObStatusException, IntegrationException {

        PaymentIntegrator pi = getPaymentIntegrator();

        input = sanitizeAssignedFee(input, waived);

        input.setDomain(DomainEnum.CODE_ENFORCEMENT);
        input.setCaseID(currentCase.getCaseID());
        input.setCodeSetElement(selectedViolation.getCodeViolated().getCodeSetElementID());

        pi.updateCECaseFee(input);

    }

    public void updateAssignedFee(FeeAssigned input, OccPeriod currentOccPeriod, boolean waived) throws BObStatusException, IntegrationException {

        PaymentIntegrator pi = getPaymentIntegrator();

        input = sanitizeAssignedFee(input, waived);

        input.setDomain(DomainEnum.OCCUPANCY);
        input.setOccPeriodID(currentOccPeriod.getPeriodID());
        input.setOccPeriodTypeID(currentOccPeriod.getType().getTypeID());

        pi.updateOccPeriodFee(input);

    }


    public List<FeeAssigned> getAssignedFees(OccPeriod currentOccPeriod) throws IntegrationException, BObStatusException {

        PaymentIntegrator pi = getPaymentIntegrator();
        ArrayList<FeeAssigned> skeletonHorde = new ArrayList<>();
//
//        ArrayList<FeeAssigned> tempList = (ArrayList<FeeAssigned>) pi.getFeeAssigned(currentOccPeriod);
//
//        // This code is maybe needless now? I am just doing some overview-level refactors so I apologize for
//        // much of this class's current state.
//        for (FeeAssigned fee : tempList) {
//
//            FeeAssigned skeleton = fee;
//
//            skeleton.setDomain(DomainEnum.OCCUPANCY);
//            skeletonHorde.add(skeleton);
//
//        }
//
        return skeletonHorde;

    }

    public List<FeeAssigned> getAssignedFees(CECase currentCase) throws IntegrationException, BObStatusException {
        PaymentIntegrator pi = getPaymentIntegrator();
        ArrayList<FeeAssigned> skeletonHorde = new ArrayList<>();
//
//        List<MoneyCECaseFeeAssigned> tempList = (ArrayList<FeeAssigned>) pi.getFeeAssigned(currentCase);
//
//        for (MoneyCECaseFeeAssigned fee : tempList) {
//
//            FeeAssigned skeleton = fee;
//
//            skeleton.setAssignedFeeID(fee.getCeCaseAssignedFeeID());
//            skeleton.setDomain(DomainEnum.CODE_ENFORCEMENT);
//            skeletonHorde.add(skeleton);
//
//        }

        return skeletonHorde;

    }

    public void updateFee(Fee input) throws IntegrationException {
        PaymentIntegrator pi = getPaymentIntegrator();
        pi.updateOccupancyInspectionFee(input);

    }

    public void insertFee(Fee input) throws IntegrationException {
        PaymentIntegrator pi = getPaymentIntegrator();
        pi.deleteOccupancyInspectionFee(input);
    }

    public void removeFee(Fee input) throws IntegrationException {
        PaymentIntegrator pi = getPaymentIntegrator();
    }

    public List<Fee> getFeeList() throws IntegrationException {

        PaymentIntegrator pi = getPaymentIntegrator();

        return pi.getFeeTypeList(getSessionBean().getSessMuni());

    }

    public List<Fee> getAllFeeTypes() throws IntegrationException {

        PaymentIntegrator pi = getPaymentIntegrator();

        ArrayList<Fee> output = pi.getAllFeeTypes();

        if (output == null) {
            output = new ArrayList<>();
        }

        return output;

    }

    public void activateFeeJoin(Fee input, OccPeriodType type) throws IntegrationException {

        PaymentIntegrator pi = getPaymentIntegrator();

        try {
            pi.insertFeePeriodTypeJoin(input, type);
        } catch (IntegrationException ex) {
            System.out.println("Failed inserting occperiod fee join, trying to reactivate.");
            pi.reactivateFeePeriodTypeJoin(input, type);

        }
    }

    public void activateFeeJoin(Fee input, EnforcableCodeElement element) throws IntegrationException {

        PaymentIntegrator pi = getPaymentIntegrator();

        try {
            pi.insertFeeCodeElementJoin(input, element);
        } catch (IntegrationException ex) {
            System.out.println("Failed inserting code element fee join, trying to reactivate.");
            pi.reactivateFeeCodeElementJoin(input, element);

        }
    }

    public void deactivateFeeJoin(Fee input, OccPeriodType type) throws IntegrationException {
        PaymentIntegrator pi = getPaymentIntegrator();
        pi.deactivateFeePeriodTypeJoin(input, type);

    }

    public void deactivateFeeJoin(Fee input, EnforcableCodeElement element) throws IntegrationException {
        PaymentIntegrator pi = getPaymentIntegrator();
        pi.deactivateFeeCodeElementJoin(input, element);
    }
    
    public void updateFeeJoin(Fee input, OccPeriodType type) throws IntegrationException {

        PaymentIntegrator pi = getPaymentIntegrator();
        pi.updateFeePeriodTypeJoin(input, type);

    }

    public void updateFeeJoin(Fee input, EnforcableCodeElement element) throws IntegrationException {

        PaymentIntegrator pi = getPaymentIntegrator();
        pi.updateFeeCodeElementJoin(input, element);

    }

    public ArrayList<Payment> getAllPayments() throws IntegrationException, BObStatusException {
        PaymentIntegrator pi = getPaymentIntegrator();
        return pi.getPaymentList();

    }

    /**
     * Accepts a payment, validates the input to make sure it is acceptable,
     * sanitizes it, then tosses it back.
     *
     * @param input
     * @param appliedTo the assigned fee this payment is being applied to.
     * @return
     * @throws BObStatusException if the input is not acceptable.
     */
    public Payment sanitizePayment(Payment input, FeeAssigned appliedTo) throws BObStatusException {

        if (appliedTo == null) {
            throw new BObStatusException("Please select a fee to assign this payment to.");
        }

        if (input.getPayer() == null) {
            throw new BObStatusException("The Payer's ID is not in our database, please make sure it's correct.");
        }

        if (input.getAmount() <= 0) {
            throw new BObStatusException("The amount you entered is not valid.");
        }

        if (input.getPaymentType().getPaymentTypeId() == 1
                && (input.getReferenceNum() == null || input.getReferenceNum().equals(""))) {
            throw new BObStatusException("A payment by check requires a reference number.");
        }

        if (input.getPaymentType().getPaymentTypeId() == 1
                && (input.getCheckNum() == 0)) {
            throw new BObStatusException("A payment by check requires a check number.");
        }

        return input;

    }

    public void insertPayment(Payment input, FeeAssigned appliedTo) throws BObStatusException, IntegrationException {

        input = sanitizePayment(input, appliedTo);

        input.setRecordedBy(getSessionBean().getSessUser());

        PaymentIntegrator pi = getPaymentIntegrator();

        pi.insertPayment(input);
        input = pi.getMostRecentPayment(); //So that the join insert knows what payment to join to

        // Code so interconnected with code I commented out that I also have to comment this out
//        if (appliedTo.getDomain() == DomainEnum.OCCUPANCY) {
//            MoneyOccPeriodFeeAssigned skeleton = new MoneyOccPeriodFeeAssigned(appliedTo);
//            pi.insertPaymentPeriodJoin(input, skeleton);
//        } else if (appliedTo.getDomain() == DomainEnum.CODE_ENFORCEMENT) {
//            MoneyCECaseFeeAssigned skeleton = new MoneyCECaseFeeAssigned(appliedTo);
//            pi.insertPaymentCaseJoin(input, skeleton);
//        }

    }

    public void updatePayment(Payment input, FeeAssigned appliedTo) throws BObStatusException, IntegrationException {

        input = sanitizePayment(input, appliedTo);

        PaymentIntegrator paymentIntegrator = getPaymentIntegrator();

        paymentIntegrator.updatePayment(input);

    }

    /**
     * TODO: This method should be changed to only deactivate the payment, deleting
     * financial data is bad!
     * @param input
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public void removePayment(Payment input) throws BObStatusException, IntegrationException {

        PaymentIntegrator paymentIntegrator = getPaymentIntegrator();
        if (input != null) {
            paymentIntegrator.deletePayment(input);
        } else {
            throw new BObStatusException("Please select a payment record from the table to delete.");
        }
    }

    public void updatePaymentType(PaymentType input) throws IntegrationException {

        PaymentIntegrator pti = getPaymentIntegrator();

        pti.updatePaymentType(input);

    }

    public void insertPaymentType(PaymentType input) throws IntegrationException {

        PaymentIntegrator pti = new PaymentIntegrator();

        pti.insertPaymentType(input);

    }

    public void removePaymentType(PaymentType input) throws BObStatusException, IntegrationException {

        PaymentIntegrator pti = getPaymentIntegrator();

        if (input != null) {

            pti.deletePaymentType(input);

        } else {
            throw new BObStatusException("Please select a payment type from the table to delete.");
        }

    }

    public ArrayList<PaymentType> getPaymentTypes() throws IntegrationException {

        PaymentIntegrator pti = getPaymentIntegrator();
        ArrayList<PaymentType> paymentTypeList = pti.getPaymentTypeList();
        if (paymentTypeList != null) {
            return paymentTypeList;
        } else {
            paymentTypeList = new ArrayList();
            return paymentTypeList;
        }

    }

}
