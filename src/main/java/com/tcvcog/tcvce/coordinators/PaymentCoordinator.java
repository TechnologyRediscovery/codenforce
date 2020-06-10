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
import com.tcvcog.tcvce.entities.EventDomainEnum;
import com.tcvcog.tcvce.entities.Fee;
import com.tcvcog.tcvce.entities.FeeAssigned;
import com.tcvcog.tcvce.entities.MoneyCECaseFeeAssigned;
import com.tcvcog.tcvce.entities.MoneyOccPeriodFeeAssigned;
import com.tcvcog.tcvce.entities.Payment;
import com.tcvcog.tcvce.entities.PaymentType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
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

    public void insertAssignedFee(FeeAssigned input, OccPeriod currentOccPeriod, boolean waived) throws IntegrationException, BObStatusException {

        if (input.getFee() == null) {
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

        MoneyOccPeriodFeeAssigned secondSkeleton = new MoneyOccPeriodFeeAssigned(input);

        secondSkeleton.setOccPeriodID(currentOccPeriod.getPeriodID());
        secondSkeleton.setOccPeriodTypeID(currentOccPeriod.getType().getTypeID());

        PaymentIntegrator pi = getPaymentIntegrator();
        pi.insertOccPeriodFee(secondSkeleton);

    }

    public void insertAssignedFee(FeeAssigned input, CECase inputCase, CodeViolation violation, boolean waived) throws IntegrationException, BObStatusException {

        if (input.getFee() == null) {
            throw new BObStatusException("Please select a fee to assign");
        }

        PaymentIntegrator pi = getPaymentIntegrator();

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

        MoneyCECaseFeeAssigned secondSkeleton = new MoneyCECaseFeeAssigned(input);

        secondSkeleton.setCeCaseAssignedFeeID(input.getAssignedFeeID());
        secondSkeleton.setCaseID(inputCase.getCaseID());
        secondSkeleton.setCodeSetElement(violation.getCodeViolated().getCodeSetElementID());

        pi.insertCECaseFee(secondSkeleton);

    }

    public List<FeeAssigned> getOccPeriodAssignedFees(OccPeriod currentOccPeriod) throws IntegrationException {

        PaymentIntegrator pi = getPaymentIntegrator();
        ArrayList<FeeAssigned> skeletonHorde = new ArrayList<>();

        ArrayList<MoneyOccPeriodFeeAssigned> tempList = (ArrayList<MoneyOccPeriodFeeAssigned>) pi.getFeeAssigned(currentOccPeriod);

        for (MoneyOccPeriodFeeAssigned fee : tempList) {

            FeeAssigned skeleton = fee;

            skeleton.setAssignedFeeID(fee.getOccPerAssignedFeeID());
            skeleton.setDomain(EventDomainEnum.OCCUPANCY);
            skeletonHorde.add(skeleton);

        }
        /*} catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Oops! We encountered a problem trying to fetch the fee assigned list!", ""));
            System.out.println("PaymentCoordinator.getOccPeriodAssignedFees | ERROR: " + ex.toString());
        }*/

        return skeletonHorde;

    }

    public List<FeeAssigned> getCECaseAssignedFees(CECase currentCase) throws IntegrationException {
        PaymentIntegrator pi = getPaymentIntegrator();
        ArrayList<FeeAssigned> skeletonHorde = new ArrayList<>();

        List<MoneyCECaseFeeAssigned> tempList = (ArrayList<MoneyCECaseFeeAssigned>) pi.getFeeAssigned(currentCase);

        for (MoneyCECaseFeeAssigned fee : tempList) {

            FeeAssigned skeleton = fee;

            skeleton.setAssignedFeeID(fee.getCeCaseAssignedFeeID());
            skeleton.setDomain(EventDomainEnum.CODE_ENFORCEMENT);
            skeletonHorde.add(skeleton);

        }
        /*
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Oops! We encountered a problem trying to fetch the fee assigned list!", ""));
            System.out.println("PaymentCoordinator.getCECaseAssignedFees | ERROR: " + ex.toString());
        }*/

        return skeletonHorde;

    }

    public List<Fee> getFeeList() throws IntegrationException {

        PaymentIntegrator pi = getPaymentIntegrator();

        return pi.getFeeTypeList(getSessionBean().getSessMuni());

    }

    public ArrayList<Payment> getAllPayments() throws IntegrationException {
        PaymentIntegrator pi = getPaymentIntegrator();
        return pi.getPaymentList();

    }

    public void insertPayment(Payment input, FeeAssigned appliedTo) throws BObStatusException, IntegrationException {

        input.setRecordedBy(getSessionBean().getSessUser());

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

        PaymentIntegrator pi = getPaymentIntegrator();

        pi.insertPayment(input);
        input = pi.getMostRecentPayment(); //So that the join insert knows what payment to join to

        if (appliedTo.getDomain() == EventDomainEnum.OCCUPANCY) {
            MoneyOccPeriodFeeAssigned skeleton = new MoneyOccPeriodFeeAssigned(appliedTo);
            pi.insertPaymentPeriodJoin(input, skeleton);
        } else if (appliedTo.getDomain() == EventDomainEnum.CODE_ENFORCEMENT) {
            MoneyCECaseFeeAssigned skeleton = new MoneyCECaseFeeAssigned(appliedTo);
            pi.insertPaymentCaseJoin(input, skeleton);
        }

    }

    public void updatePayment(Payment input, FeeAssigned appliedTo) throws BObStatusException, IntegrationException {

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

        PaymentIntegrator paymentIntegrator = getPaymentIntegrator();

        //oif.setOccupancyInspectionFeeNotes(formOccupancyInspectionFeeNotes); TODO: ask eric what this should do
        paymentIntegrator.updatePayment(input);

    }

    public void removePayment(Payment input) throws BObStatusException, IntegrationException {

        PaymentIntegrator paymentIntegrator = getPaymentIntegrator();
        if (input != null) {
            paymentIntegrator.deletePayment(input);
        } else {
            throw new BObStatusException("Please select a payment record from the table to delete.");
        }
    }

    public String getAddressFromPropUnitID(int propUnitID) throws IntegrationException {

        PropertyIntegrator pi = getPropertyIntegrator();
        PropertyUnit unit = pi.getPropertyUnit(propUnitID);
        Property prop = pi.getProperty(unit.getPropertyID());
        return prop.getAddress();

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
