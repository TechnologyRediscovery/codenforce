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
import com.tcvcog.tcvce.entities.PaymentType;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
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

    public void insertPayment(Payment input, FeeAssigned appliedTo) {

        boolean failed = false;

        input.setRecordedBy(getSessionBean().getSessUser());
        
        if (input.getPayer() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "The Payer's ID is not in our database, please make sure it's correct.", " "));
            input.setPayer(new Person());
            failed = true;
        }

        if (input.getAmount() <= 0) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "The amount you entered is not valid.", " "));
            input.setPayer(new Person());
            failed = true;
        }

        if (input.getPaymentType().getPaymentTypeId() == 1
                && (input.getReferenceNum() == null || input.getReferenceNum().equals(""))) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "A payment by check requires a reference number", " "));
            input.setPayer(new Person());
            failed = true;
        }

        if (input.getPaymentType().getPaymentTypeId() == 1
                && (input.getCheckNum() == 0)) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "A payment by check requires a check number.", " "));
            input.setPayer(new Person());
            failed = true;
        }

        if (failed == false) {

            PaymentIntegrator pi = getPaymentIntegrator();

            try {
                pi.insertPayment(input);
                input = pi.getMostRecentPayment(); //So that the join insert knows what payment to join to

                if (appliedTo.getDomain() == EventDomainEnum.OCCUPANCY) {
                    MoneyOccPeriodFeeAssigned skeleton = new MoneyOccPeriodFeeAssigned(appliedTo);
                    pi.insertPaymentPeriodJoin(input, skeleton);
                } else if (appliedTo.getDomain() == EventDomainEnum.CODE_ENFORCEMENT) {
                    MoneyCECaseFeeAssigned skeleton = new MoneyCECaseFeeAssigned(appliedTo);
                    pi.insertPaymentCaseJoin(input, skeleton);
                }
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Successfully added payment record to database!", ""));
            } catch (IntegrationException ex) {
                System.out.println(ex.toString());
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to add payment record to database, sorry!", "Check server print out..."));
            }

        }

    }

    public void updatePayment(Payment input, FeeAssigned appliedTo) {

        boolean failed = false;

        if (appliedTo == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a fee to assign this payment to.", " "));
            input.setPayer(new Person());
            failed = true;
        }

        if (input.getPayer() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "The Payer's ID is not in our database, please make sure it's correct.", " "));
            input.setPayer(new Person());
            failed = true;
        }

        if (input.getAmount() <= 0) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "The amount you entered is not valid.", " "));
            input.setPayer(new Person());
            failed = true;
        }

        if (input.getPaymentType().getPaymentTypeId() == 1
                && (input.getReferenceNum() == null || input.getReferenceNum().equals(""))) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "A payment by check requires a reference number", " "));
            input.setPayer(new Person());
            failed = true;
        }

        if (input.getPaymentType().getPaymentTypeId() == 1
                && (input.getCheckNum() == 0)) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "A payment by check requires a check number.", " "));
            failed = true;
        }

        if (!failed) {
            PaymentIntegrator paymentIntegrator = getPaymentIntegrator();

            //oif.setOccupancyInspectionFeeNotes(formOccupancyInspectionFeeNotes); TODO: ask eric what this should do
            try {
                paymentIntegrator.updatePayment(input);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Payment record updated!", ""));

            } catch (IntegrationException ex) {
                System.out.println(ex.toString());
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to update payment record in database.",
                                "This must be corrected by the System Administrator"));
            }
        }

    }

    public void removePayment(Payment input) {

        PaymentIntegrator paymentIntegrator = getPaymentIntegrator();
        if (input != null) {
            try {
                paymentIntegrator.deletePayment(input);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Payment record deleted forever!", ""));
            } catch (IntegrationException ex) {
                System.out.println(ex.toString());
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to delete payment record--probably because it is used "
                                + "somewhere in the database. Sorry.",
                                "This payment will always be with us."));
            }

        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a payment record from the table to delete", ""));
        }

    }

    public String getAddressFromPropUnitID(int propUnitID) {

        try {
            PropertyIntegrator pi = getPropertyIntegrator();
            PropertyUnit unit = pi.getPropertyUnit(propUnitID);
            Property prop = pi.getProperty(unit.getPropertyID());
            return prop.getAddress();
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to find property address!",
                            ""));
        }

        return "";

    }

    public void updatePaymentType(PaymentType input) {

        PaymentIntegrator pti = getPaymentIntegrator();

        try {
            pti.updatePaymentType(input);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Payment type updated!", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to update Payment type in database.",
                            "This must be corrected by the System Administrator"));
        }

    }

    public void insertPaymentType(PaymentType input) {

        PaymentIntegrator pti = new PaymentIntegrator();
        try {
            pti.insertPaymentType(input);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully added payment type to database!", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to add payment type to database, sorry!", "Check server print out..."));
        }

    }

    public void removePaymentType(PaymentType input) {

        PaymentIntegrator pti = getPaymentIntegrator();

        if (input != null) {
            try {
                pti.deletePaymentType(input);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Payment type deleted forever!", ""));
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to delete payment type--probably because it is used "
                                + "somewhere in the database. Sorry.",
                                "This payment will always be with us."));
            }

        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a payment type from the table to delete", ""));
        }

    }
    
    public ArrayList<PaymentType> getPaymentTypes() {
        
        ArrayList<PaymentType> paymentTypeList = new ArrayList<>();
        
        try {
            PaymentIntegrator pti = getPaymentIntegrator();
            paymentTypeList = pti.getPaymentTypeList();
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to load Payment Type",
                            "This must be corrected by the system administrator"));
        }
        if (paymentTypeList != null) {
            return paymentTypeList;
        } else {
            paymentTypeList = new ArrayList();
            return paymentTypeList;
        }
        
    }
    
    

}
