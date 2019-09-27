/*
 * Copyright (C) 2018 Adam Gutonski
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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.MoneyOccPeriodFeeAssigned;
import com.tcvcog.tcvce.occupancy.integration.PaymentIntegrator;
import com.tcvcog.tcvce.entities.Payment;
import com.tcvcog.tcvce.entities.PaymentType;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Adam Gutonski
 */
public class PaymentBB extends BackingBeanUtils implements Serializable {

    private ArrayList<Payment> paymentList;
    private Payment selectedPayment;
    private Payment formPayment;
    private ArrayList<PaymentType> paymentTypeList;
    private ArrayList<PaymentType> paymentTypeTitleList;
    private PaymentType selectedPaymentType;
    private PaymentType formPaymentType;
    private PaymentType newSelectedPaymentType;
    private PaymentType newPaymentType;

    private OccPeriod currentOccPeriod;
    private MoneyOccPeriodFeeAssigned selectedOccPeriodFee;
    private ArrayList<MoneyOccPeriodFeeAssigned> occPeriodFeeList;
    private ArrayList<MoneyOccPeriodFeeAssigned> occPeriodFilteredFeeList;

    private boolean editing;
    private String redirTo;

    public PaymentBB() {
    }

    @PostConstruct
    public void initBean() {
        PaymentIntegrator paymentIntegrator = getPaymentIntegrator();
        redirTo = getSessionBean().getPaymentRedirTo();
        if (redirTo != null) {
            currentOccPeriod = getSessionBean().getSessionOccPeriod();
            if (getSessionBean().getSessionPayment() != null) {
                paymentList = new ArrayList<>();
                paymentList.add(getSessionBean().getSessionPayment());
                try {
                    occPeriodFeeList = (ArrayList<MoneyOccPeriodFeeAssigned>) paymentIntegrator.getFeeAssigned(currentOccPeriod);
                } catch (IntegrationException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Oops! We encountered a problem trying to fetch the fee list!", ""));
                }
            } else if (currentOccPeriod != null) {
                try {
                    occPeriodFeeList = (ArrayList<MoneyOccPeriodFeeAssigned>) paymentIntegrator.getFeeAssigned(currentOccPeriod);
                    paymentList = new ArrayList<>();
                    
                    for (MoneyOccPeriodFeeAssigned skeleton : occPeriodFeeList) {
                        paymentList.addAll(skeleton.getPaymentList());
                    }
                    
                } catch (IntegrationException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Unable to load payment list (from occperiod)",
                                    "This must be corrected by the system administrator"));
                }

            }
        } else {

            try {
                paymentList = paymentIntegrator.getPaymentList();
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to load payment list",
                                "This must be corrected by the system administrator"));
            }
        }

        formPayment = new Payment();

        formPaymentType = new PaymentType();

    }

    /**
     * @return the paymentList
     */
    public ArrayList<Payment> getPaymentList() {

        if (paymentList != null) {
            return paymentList;
        } else {
            paymentList = new ArrayList();
            return paymentList;
        }
    }

    public void commitPaymentUpdates(ActionEvent e) {
        if (selectedOccPeriodFee == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a fee to assign this payment to.", " "));
            formPayment.setPayer(new Person());
        } else {
            PaymentIntegrator paymentIntegrator = getPaymentIntegrator();
            Payment payment = selectedPayment;

            payment.setPaymentType(formPayment.getPaymentType());
            payment.setOccupancyInspectionID(formPayment.getOccupancyInspectionID());
            payment.setDateDeposited(formPayment.getDateDeposited());
            payment.setDateReceived(formPayment.getDateReceived());
            payment.setAmount(formPayment.getAmount());
            payment.setPayer(formPayment.getPayer());
            payment.setReferenceNum(formPayment.getReferenceNum());
            payment.setCheckNum(formPayment.getCheckNum());
            payment.setCleared(formPayment.isCleared());
            payment.setNotes(formPayment.getNotes());
            //oif.setOccupancyInspectionFeeNotes(formOccupancyInspectionFeeNotes);
            try {
                paymentIntegrator.updatePayment(payment);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Payment record updated!", ""));
                editing = false;
                formPayment = new Payment();
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to update payment record in database.",
                                "This must be corrected by the System Administrator"));
            }
        }
    }

    public void editPayment(ActionEvent e) {
        if (getSelectedPayment() != null) {
            formPayment.setPaymentID(selectedPayment.getPaymentID());
            formPayment.setPaymentType(selectedPayment.getPaymentType());
            formPayment.setOccupancyInspectionID(selectedPayment.getOccupancyInspectionID());
            formPayment.setAmount(selectedPayment.getAmount());
            formPayment.setPayer(selectedPayment.getPayer());
            formPayment.setReferenceNum(selectedPayment.getReferenceNum());
            formPayment.setCheckNum(selectedPayment.getCheckNum());
            formPayment.setCleared(selectedPayment.isCleared());
            formPayment.setDateDeposited(selectedPayment.getDateDeposited());
            formPayment.setDateReceived(selectedPayment.getDateReceived());
            formPayment.setNotes(selectedPayment.getNotes());
            editing = true;
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a payment record to update", ""));
        }
    }

    public void initializeNewPayment(ActionEvent e) {

        editing = false;
        formPayment = new Payment();

    }

    public String finishAndRedir() {
        getSessionBean().setPaymentRedirTo(null);

        return redirTo;
    }

    public boolean editingOccPeriod() {
        return (redirTo != null && currentOccPeriod != null);
    }

    public String getOccPeriodAddress() {

        PropertyIntegrator pi = getPropertyIntegrator();

        String address = "";
        PropertyUnit unit;
        Property prop;
        try {
            unit = pi.getPropertyUnitByPropertyUnitID(currentOccPeriod.getPropertyUnitID());
            prop = pi.getProperty(unit.getPropertyID());
            address = prop.getAddress();
        } catch (IntegrationException ex) {
            System.out.println("PaymentBB had problems getting the OccPeriodAddress");
        }

        return address;

    }

    public String addPayment() {
        if (selectedOccPeriodFee == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a fee to assign this payment to.", " "));
            formPayment.setPayer(new Person());
            return "";
        }

        Payment payment = new Payment();
        PaymentIntegrator paymentIntegrator = getPaymentIntegrator();
        payment.setPaymentID(formPayment.getPaymentID());
        payment.setOccupancyInspectionID(formPayment.getOccupancyInspectionID());
        payment.setPaymentType(formPayment.getPaymentType());
        payment.setDateDeposited(formPayment.getDateDeposited());
        payment.setDateReceived(formPayment.getDateReceived());
        payment.setAmount(formPayment.getAmount());
        payment.setPayer(formPayment.getPayer());
        payment.setReferenceNum(formPayment.getReferenceNum());
        payment.setCheckNum(formPayment.getCheckNum());
        payment.setCleared(formPayment.isCleared());
        payment.setNotes(formPayment.getNotes());
        payment.setRecordedBy(getSessionBean().getSessionUser());

        if (payment.getPayer() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "The Payer's ID is not in our database, please make sure it's correct.", " "));
            formPayment.setPayer(new Person());
            return "";
        }

        if (payment.getPaymentType().getPaymentTypeId() == 1
                && (payment.getCheckNum() == 0)) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "A payment by check requires a check number.", " "));
            return "";
        }

        try {
            paymentIntegrator.insertPayment(payment);
            if (editingOccPeriod()) {
                payment = paymentIntegrator.getMostRecentPayment();
                paymentIntegrator.insertPaymentPeriodJoin(payment, selectedOccPeriodFee);
            }
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully added payment record to database!", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to add payment record to database, sorry!", "Check server print out..."));
            return "";
        }

        return "paymentManage";

    }

    public void deleteSelectedPayment(ActionEvent e) {
        PaymentIntegrator paymentIntegrator = getPaymentIntegrator();
        if (getSelectedPayment() != null) {
            try {
                paymentIntegrator.deletePayment(getSelectedPayment());
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Payment record deleted forever!", ""));
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to delete payment record--probably because it is used "
                                + "somewhere in the database. Sorry.",
                                "This category will always be with us."));
            }

        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a payment record from the table to delete", ""));
        }
    }

    /**
     * @param paymentList the paymentList to set
     */
    public void setPaymentList(ArrayList<Payment> paymentList) {
        this.paymentList = paymentList;
    }

    /**
     * @return the selectedPayment
     */
    public Payment getSelectedPayment() {
        return selectedPayment;
    }

    /**
     * @param selectedPayment the selectedPayment to set
     */
    public void setSelectedPayment(Payment selectedPayment) {
        this.selectedPayment = selectedPayment;
    }

    public Payment getFormPayment() {
        return formPayment;
    }

    public void setFormPayment(Payment formPayment) {
        this.formPayment = formPayment;
    }

    // Below are the methods to driectly access the fields of formPayment
    public int getFormPaymentOccupancyInspectionID() {
        return formPayment.getOccupancyInspectionID();
    }

    public void setFormPaymentOccupancyInspectionID(int occupancyInspectionID) {
        formPayment.setOccupancyInspectionID(occupancyInspectionID);
    }

    public Date getFormPaymentDateDeposited() {
        return Date.from(formPayment.getDateDeposited()
                .atZone(ZoneId.systemDefault()).toInstant());
    }

    public void setFormPaymentDateDeposited(Date paymentDateDeposited) {
        formPayment.setDateDeposited(LocalDateTime.ofInstant(
                paymentDateDeposited.toInstant(), ZoneId.systemDefault()));
    }

    public Date getFormPaymentDateReceived() {
        return Date.from(formPayment.getDateReceived()
                .atZone(ZoneId.systemDefault()).toInstant());
    }

    public void setFormPaymentDateReceived(Date paymentDateReceived) {

        formPayment.setDateReceived(LocalDateTime.ofInstant(
                paymentDateReceived.toInstant(), ZoneId.systemDefault()));
    }

    public double getFormPaymentAmount() {
        return formPayment.getAmount();
    }

    public void setFormPaymentAmount(double paymentAmount) {
        formPayment.setAmount(paymentAmount);
    }

    public String getFormPaymentReferenceNum() {
        return formPayment.getReferenceNum();
    }

    public void setFormPaymentReferenceNum(String paymentReferenceNum) {
        formPayment.setReferenceNum(paymentReferenceNum);
    }

    public int getFormPaymentCheckNum() {
        return formPayment.getCheckNum();
    }

    public void setFormPaymentCheckNum(int checkNum) {
        formPayment.setCheckNum(checkNum);
    }

    public boolean isFormPaymentCleared() {
        return formPayment.isCleared();
    }

    public void setFormPaymentCleared(boolean cleared) {
        formPayment.setCleared(cleared);
    }

    public int getFormPaymentID() {
        return formPayment.getPaymentID();
    }

    public void setFormPaymentID(int paymentID) {
        formPayment.setPaymentID(paymentID);
    }

    public int getFormPaymentPayer() {
        return formPayment.getPayer().getPersonID();
    }

    public void setFormPaymentPayer(int personID) {

        PersonIntegrator pi = new PersonIntegrator();

        try {
            formPayment.setPayer(pi.getPerson(personID));
        } catch (IntegrationException ex) {
            System.out.println(ex);

        }

    }

    public PaymentType getFormPaymentPaymentType() {
        System.out.println("get Form Payment Type ran! the type ID is:" + formPayment.getPaymentType().getPaymentTypeId());

        return formPayment.getPaymentType();
    }

    public void setFormPaymentPaymentType(PaymentType paymentType) {
        formPayment.setPaymentType(paymentType);
        System.out.println("Form Payment Type has been set! the type ID is:" + formPayment.getPaymentType().getPaymentTypeId() + " The type was supposed to be set to: " + paymentType.getPaymentTypeId());

    }

    public String getFormPaymentNotes() {
        return formPayment.getNotes();
    }

    public void setFormPaymentNotes(String notes) {
        formPayment.setNotes(notes);
    }

    /*METHODS IMPORTED FROM PAYMENTTYPEBB*/
    public void editPaymentType(ActionEvent e) {
        if (getSelectedPaymentType() != null) {
            formPaymentType.setPaymentTypeId(selectedPaymentType.getPaymentTypeId());
            formPaymentType.setPaymentTypeTitle(selectedPaymentType.getPaymentTypeTitle());
            editing = true;
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a payment type to update", ""));
        }
    }

    public void commitPaymentTypeUpdates(ActionEvent e) {
        PaymentIntegrator pti = getPaymentIntegrator();
        PaymentType pt = selectedPaymentType;

        pt.setPaymentTypeTitle(formPaymentType.getPaymentTypeTitle());
        //oif.setOccupancyInspectionFeeNotes(formOccupancyInspectionFeeNotes);
        try {
            pti.updatePaymentType(pt);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Payment type updated!", ""));
            editing = false;
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to update Payment type in database.",
                            "This must be corrected by the System Administrator"));
        }
    }

    public void initializeNewType(ActionEvent e) {
        editing = false;
        formPaymentType = new PaymentType();
    }

    public String addPaymentType() {
        PaymentType pt = new PaymentType();
        PaymentIntegrator pti = new PaymentIntegrator();
        pt.setPaymentTypeId(formPaymentType.getPaymentTypeId());
        pt.setPaymentTypeTitle(formPaymentType.getPaymentTypeTitle());
        try {
            pti.insertPaymentType(pt);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully added payment type to database!", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to add payment type to database, sorry!", "Check server print out..."));
            return "";
        }

        return "paymentTypeManage";
    }

    /**
     * @return the paymentTypeList
     */
    public ArrayList<PaymentType> getPaymentTypeList() {
        try {
            PaymentIntegrator pti = getPaymentIntegrator();
            paymentTypeList = pti.getPaymentTypeList();
        } catch (IntegrationException ex) {
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

    public void deleteSelectedPaymentType(ActionEvent e) {
        PaymentIntegrator pti = getPaymentIntegrator();
        if (getSelectedPaymentType() != null) {
            try {
                pti.deletePaymentType(getSelectedPaymentType());
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Payment type deleted forever!", ""));
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to delete payment type--probably because it is used "
                                + "somewhere in the database. Sorry.",
                                "This category will always be with us."));
            }

        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a payment type from the table to delete", ""));
        }
    }

    /**
     * @param paymentTypeList the paymentTypeList to set
     */
    public void setPaymentTypeList(ArrayList<PaymentType> paymentTypeList) {
        this.paymentTypeList = paymentTypeList;
    }

    /**
     * @return the selectedPaymentType
     */
    public PaymentType getSelectedPaymentType() {
        return selectedPaymentType;
    }

    /**
     * @param selectedPaymentType the selectedPaymentType to set
     */
    public void setSelectedPaymentType(PaymentType selectedPaymentType) {
        this.selectedPaymentType = selectedPaymentType;
    }

    /**
     * @return the newFormSelectedPaymentType
     */
    public PaymentType getNewSelectedPaymentType() {
        return newSelectedPaymentType;
    }

    /**
     * @param newSelectedPaymentType the newFormSelectedPaymentType to set
     */
    public void setNewSelectedPaymentType(PaymentType newSelectedPaymentType) {
        this.newSelectedPaymentType = newSelectedPaymentType;
    }

    public PaymentType getFormPaymentType() {
        return formPaymentType;
    }

    public void setFormPaymentType(PaymentType formPaymentType) {
        this.formPaymentType = formPaymentType;
    }

    //Below are the methods to directly access formPaymentType's fields
    public int getFormPaymentTypeId() {
        return formPaymentType.getPaymentTypeId();
    }

    public void setFormPaymentTypeId(int paymentTypeId) {
        formPaymentType.setPaymentTypeId(paymentTypeId);
    }

    public String getFormPaymentTypeTitle() {
        return formPaymentType.getPaymentTypeTitle();
    }

    public void setFormPaymentTypeTitle(String paymentTypeTitle) {
        formPaymentType.setPaymentTypeTitle(paymentTypeTitle);
    }

    public PaymentType getNewPaymentType() {
        return newPaymentType;
    }

    public void setNewPaymentType(PaymentType newPaymentType) {
        this.newPaymentType = newPaymentType;
    }

    /**
     * @return the paymentTypeTitleList
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public ArrayList<PaymentType> getPaymentTypeTitleList() throws IntegrationException {
        PaymentIntegrator pi = getPaymentIntegrator();
        paymentTypeTitleList = pi.getPaymentTypeList();
        return paymentTypeTitleList;
    }

    /**
     * @param paymentTypeTitleList the paymentTypeTitleList to set
     */
    public void setPaymentTypeTitleList(ArrayList<PaymentType> paymentTypeTitleList) {
        this.paymentTypeTitleList = paymentTypeTitleList;
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    public OccPeriod getCurrentOccPeriod() {
        return currentOccPeriod;
    }

    public void setCurrentOccPeriod(OccPeriod currentOccPeriod) {
        this.currentOccPeriod = currentOccPeriod;
    }

    public String getRedirTo() {
        return redirTo;
    }

    public void setRedirTo(String redirTo) {
        this.redirTo = redirTo;
    }

    public MoneyOccPeriodFeeAssigned getSelectedOccPeriodFee() {
        return selectedOccPeriodFee;
    }

    public void setSelectedOccPeriodFee(MoneyOccPeriodFeeAssigned selectedOccPeriodFee) {
        this.selectedOccPeriodFee = selectedOccPeriodFee;
    }

    public ArrayList<MoneyOccPeriodFeeAssigned> getOccPeriodFeeList() {
        return occPeriodFeeList;
    }

    public void setOccPeriodFeeList(ArrayList<MoneyOccPeriodFeeAssigned> occPeriodFeeList) {
        this.occPeriodFeeList = occPeriodFeeList;
    }

    public ArrayList<MoneyOccPeriodFeeAssigned> getOccPeriodFilteredFeeList() {
        return occPeriodFilteredFeeList;
    }

    public void setOccPeriodFilteredFeeList(ArrayList<MoneyOccPeriodFeeAssigned> occPeriodFilteredFeeList) {
        this.occPeriodFilteredFeeList = occPeriodFilteredFeeList;
    }

}
