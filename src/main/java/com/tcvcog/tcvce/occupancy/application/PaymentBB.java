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
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.EventDomainEnum;
import com.tcvcog.tcvce.entities.FeeAssigned;
import com.tcvcog.tcvce.entities.MoneyCECaseFeeAssigned;
import com.tcvcog.tcvce.entities.MoneyOccPeriodFeeAssigned;
import com.tcvcog.tcvce.occupancy.integration.PaymentIntegrator;
import com.tcvcog.tcvce.entities.Payment;
import com.tcvcog.tcvce.entities.PaymentType;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
    private ArrayList<PaymentType> paymentTypeList;
    private ArrayList<PaymentType> paymentTypeTitleList;
    private PaymentType selectedPaymentType;
    private PaymentType newSelectedPaymentType;
    private PaymentType newPaymentType;

    private OccPeriod currentOccPeriod;
    private CECaseDataHeavy currentCase;
    private FeeAssigned selectedAssignedFee;
    private ArrayList<FeeAssigned> feeAssignedList;
    private ArrayList<MoneyOccPeriodFeeAssigned> occPeriodFilteredFeeList;

    private EventDomainEnum currentDomain;
    private String currentMode;
    private boolean redirected;
    private boolean currentPaymentSelected;

    public PaymentBB() {
    }

    @PostConstruct
    public void initBean() {
        PaymentIntegrator paymentIntegrator = getPaymentIntegrator();
        if (getSessionBean().getNavStack().peekLastPage() != null) {

            refreshFeeAssignedList();

            redirected = true;
        }

        currentMode = "Lookup";
        
        selectedPayment = new Payment();

        selectedPaymentType = new PaymentType();

        currentPaymentSelected = false;
    }

    public void refreshFeeAssignedList() {

        feeAssignedList = new ArrayList<>();

        paymentList = new ArrayList<>();

        boolean paymentSet = false;

        PaymentIntegrator pi = getPaymentIntegrator();

        currentDomain = getSessionBean().getFeeManagementDomain();

        if (currentDomain == EventDomainEnum.OCCUPANCY) {

            currentOccPeriod = getSessionBean().getSessOccPeriod();

            if (getSessionBean().getSessionPayment() != null) {
                paymentList.add(getSessionBean().getSessionPayment());
                paymentSet = true;
                try {
                    ArrayList<MoneyOccPeriodFeeAssigned> tempList = (ArrayList<MoneyOccPeriodFeeAssigned>) pi.getFeeAssigned(currentOccPeriod);

                    for (MoneyOccPeriodFeeAssigned fee : tempList) {

                        FeeAssigned skeleton = fee;

                        skeleton.setAssignedFeeID(fee.getOccPerAssignedFeeID());
                        skeleton.setDomain(currentDomain);
                        feeAssignedList.add(skeleton);

                    }
                } catch (IntegrationException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Oops! We encountered a problem trying to fetch the fee assigned list!", ""));
                }
            } else if (currentOccPeriod != null) {

                try {
                    ArrayList<MoneyOccPeriodFeeAssigned> tempList = (ArrayList<MoneyOccPeriodFeeAssigned>) pi.getFeeAssigned(currentOccPeriod);

                    for (MoneyOccPeriodFeeAssigned fee : tempList) {

                        FeeAssigned skeleton = fee;

                        skeleton.setAssignedFeeID(fee.getOccPerAssignedFeeID());
                        skeleton.setDomain(currentDomain);
                        feeAssignedList.add(skeleton);
                        paymentList.addAll(skeleton.getPaymentList());
                    }
                    paymentSet = true;
                } catch (IntegrationException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Oops! We encountered a problem trying to refresh the fee assigned list!", ""));
                }

            }

        } else if (currentDomain == EventDomainEnum.CODE_ENFORCEMENT) {

            CaseCoordinator cc = getCaseCoordinator();

            try {
                currentCase = cc.assembleCECaseDataHeavy(getSessionBean().getFeeManagementCeCase(), getSessionBean().getSessUser().getMyCredential());
            } catch (IntegrationException | BObStatusException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Oops! We encountered a problem trying to refresh the fee assigned list!", ""));
            }

            if (getSessionBean().getSessionPayment() != null) {

                paymentSet = true;
//                try {
                // TODO NADGIT rewire to use Coordinator
//                    List<MoneyCECaseFeeAssigned> tempList = (ArrayList<MoneyCECaseFeeAssigned>) pi.getFeeAssigned(currentCase);
                List<MoneyCECaseFeeAssigned> tempList = new ArrayList<>();

                for (MoneyCECaseFeeAssigned fee : tempList) {

                    FeeAssigned skeleton = fee;

                    skeleton.setAssignedFeeID(fee.getCeCaseAssignedFeeID());
                    skeleton.setDomain(currentDomain);
                    feeAssignedList.add(skeleton);

                }
//                } catch (IntegrationException ex) {
//                    getFacesContext().addMessage(null,
//                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                                    "Oops! We encountered a problem trying to fetch the fee assigned list!", ""));
//                }
            } else if (currentCase != null) {

//                try {
                // TODO NADGIT rewire to use Coordinator
//                    List<MoneyCECaseFeeAssigned> tempList = (ArrayList<MoneyCECaseFeeAssigned>) pi.getFeeAssigned(currentCase);
                feeAssignedList.addAll(currentCase.getFeeList());

                for (FeeAssigned fee : feeAssignedList) {

                    paymentList.addAll(fee.getPaymentList());

                }

                paymentSet = true;
//                } catch (IntegrationException ex) {
//                    getFacesContext().addMessage(null,
//                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                                    "Oops! We encountered a problem trying to refresh the fee assigned list!", ""));
//                }

            }

        }

        if (!paymentSet) {
            try {
                paymentList = pi.getPaymentList();
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to load payment list",
                                "This must be corrected by the system administrator"));
            }
        }

    }

    /**
     *
     * @param currentMode Lookup, Insert, Update, Remove
     * @throws IntegrationException
     */
    public void setCurrentMode(String currentMode) throws IntegrationException {

        //store currentMode into tempCurMode as a temporary value, in case the currenMode equal null
        String tempCurMode = this.currentMode;
        //reset default setting every time the Mode has been selected 
        currentPaymentSelected = false;
        //check the currentMode == null or not
        if (currentMode == null) {
            this.currentMode = tempCurMode;
        } else {
            this.currentMode = currentMode;
        }
        //create an instance object of MunicipalityDataHeavy if current mode == "Insert"
        if (getActiveInsertMode()) {
            selectedPayment = new Payment();
        }
        //show the current mode in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, this.currentMode + " Mode Selected", ""));
    }

    //check if current mode == Lookup
    public boolean getActiveLookupMode() {
        return "Lookup".equals(currentMode);
    }

    //check if current mode == Insert
    public boolean getActiveInsertMode() {
        return "Insert".equals(currentMode);
    }

    //check if current mode == Update
    public boolean getActiveUpdateMode() {
        return "Update".equals(currentMode);
    }

    //check if current mode == Remove
    public boolean getActiveRemoveMode() {
        return "Remove".equals(currentMode);
    }

    //Select button on side panel can only be used in either Lookup Mode or Update Mode
    public boolean getSelectedButtonActive() {
        return !("Lookup".equals(currentMode) || "Update".equals(currentMode) || "Remove".equals(currentMode));
    }

    /**
     * Changing of which payment is being selected and not being selected
     *
     * @param p
     * @throws IntegrationException
     */
    public void onPaymentSelectedButtonChange(Payment p) throws IntegrationException {

        // "Select" button was selected
        if (currentPaymentSelected == true) {

            //set current selected payment
            selectedPayment = p;
            //update the current selected payment list in side panel
            paymentList = new ArrayList<>();
            paymentList.add(p);

            //Message Noticefication
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Current Selected Payment: " + selectedPayment.getPaymentID(), ""));

            // "Select" button wasn't selected
        } else {
            //turn to default setting
            refreshFeeAssignedList();

            currentPaymentSelected = false;

            selectedPayment = new Payment();

            //Message Noticefication
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Default Selected Municipality: " + selectedPayment.getPaymentID(), ""));
        }

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

    public String onInsertButtonChange() {

        if (selectedAssignedFee == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a fee to assign this payment to.", " "));
            selectedPayment.setPayer(new Person());
            return "";
        }

        Payment payment = new Payment();
        PaymentIntegrator paymentIntegrator = getPaymentIntegrator();
        payment.setPaymentID(selectedPayment.getPaymentID());
        payment.setPaymentType(selectedPayment.getPaymentType());
        payment.setDateDeposited(selectedPayment.getDateDeposited());
        payment.setDateReceived(selectedPayment.getDateReceived());
        payment.setAmount(selectedPayment.getAmount());
        payment.setPayer(selectedPayment.getPayer());
        payment.setReferenceNum(selectedPayment.getReferenceNum());
        payment.setCheckNum(selectedPayment.getCheckNum());
        payment.setCleared(selectedPayment.isCleared());
        payment.setNotes(selectedPayment.getNotes());
        payment.setRecordedBy(getSessionBean().getSessUser());

        if (payment.getPayer() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "The Payer's ID is not in our database, please make sure it's correct.", " "));
            selectedPayment.setPayer(new Person());
            return "";
        }

        if (payment.getAmount() <= 0) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "The amount you entered is not valid.", " "));
            selectedPayment.setPayer(new Person());
            return "";
        }

        if (payment.getPaymentType().getPaymentTypeId() == 1
                && (payment.getReferenceNum() == null || payment.getReferenceNum().equals(""))) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "A payment by check requires a reference number", " "));
            selectedPayment.setPayer(new Person());
            return "";
        }

        if (payment.getPaymentType().getPaymentTypeId() == 1
                && (payment.getCheckNum() == 0)) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "A payment by check requires a check number.", " "));
            selectedPayment.setPayer(new Person());
            return "";
        }

        try {
            paymentIntegrator.insertPayment(payment);
            payment = paymentIntegrator.getMostRecentPayment(); //So that the join insert knows what payment to join to

            if (editingOccPeriod()) {
                MoneyOccPeriodFeeAssigned skeleton = new MoneyOccPeriodFeeAssigned(selectedAssignedFee);
                paymentIntegrator.insertPaymentPeriodJoin(payment, skeleton);
            } else if (editingCECase()) {
                MoneyCECaseFeeAssigned skeleton = new MoneyCECaseFeeAssigned(selectedAssignedFee);
                paymentIntegrator.insertPaymentCaseJoin(payment, skeleton);
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

        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successful Insert New Municipality", ""));
        return "payments";
    }

    public String onUpdateButtonChange() {

        boolean failed = false;

        Payment payment = selectedPayment;

        payment.setPaymentType(selectedPayment.getPaymentType());
        payment.setDateDeposited(selectedPayment.getDateDeposited());
        payment.setDateReceived(selectedPayment.getDateReceived());
        payment.setAmount(selectedPayment.getAmount());
        payment.setPayer(selectedPayment.getPayer());
        payment.setReferenceNum(selectedPayment.getReferenceNum());
        payment.setCheckNum(selectedPayment.getCheckNum());
        payment.setCleared(selectedPayment.isCleared());
        payment.setNotes(selectedPayment.getNotes());

        if (selectedAssignedFee == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a fee to assign this payment to.", " "));
            selectedPayment.setPayer(new Person());
            failed = true;
        }

        if (payment.getPayer() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "The Payer's ID is not in our database, please make sure it's correct.", " "));
            selectedPayment.setPayer(new Person());
            failed = true;
        }

        if (payment.getAmount() <= 0) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "The amount you entered is not valid.", " "));
            selectedPayment.setPayer(new Person());
            failed = true;
        }

        if (payment.getPaymentType().getPaymentTypeId() == 1
                && (payment.getReferenceNum() == null || payment.getReferenceNum().equals(""))) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "A payment by check requires a reference number", " "));
            selectedPayment.setPayer(new Person());
            failed = true;
        }

        if (payment.getPaymentType().getPaymentTypeId() == 1
                && (payment.getCheckNum() == 0)) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "A payment by check requires a check number.", " "));
            failed = true;
        }

        if (!failed) {
            PaymentIntegrator paymentIntegrator = getPaymentIntegrator();

            //oif.setOccupancyInspectionFeeNotes(formOccupancyInspectionFeeNotes);
            try {
                paymentIntegrator.updatePayment(payment);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Payment record updated!", ""));

            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to update payment record in database.",
                                "This must be corrected by the System Administrator"));
            }
        }

        return "payments";
    }

    public String onRemoveButtonChange() {
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
                                "This payment will always be with us."));
            }

        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a payment record from the table to delete", ""));
        }

        return "payments";
    }

    public String finishAndRedir() {

        return getSessionBean().getNavStack().popLastPage();
    }

    public String goToPaymentTypeManage() {
        getSessionBean().getNavStack().pushCurrentPage();

        return "paymentTypeManage";

    }

    public boolean editingOccPeriod() {
        return (getSessionBean().getNavStack().peekLastPage() != null && currentOccPeriod != null && currentDomain == EventDomainEnum.OCCUPANCY);
    }

    public boolean editingCECase() {
        return (getSessionBean().getNavStack().peekLastPage() != null && currentCase != null && currentDomain == EventDomainEnum.CODE_ENFORCEMENT);
    }

    /**
     * TODO NADGIT refactor to use Coordinator
     *
     * @return
     */
    public String getCurrentAddress() {

        String address = "";

//        try {
//
//            if (editingOccPeriod()) {
//                PropertyIntegrator pi = getPropertyIntegrator();
//            // TODO: NADGIT migrate to data heavy
//                PropertyUnit unit = pi.getPropertyUnitByPropertyUnitID(currentOccPeriod.getPropertyUnitID());
//                Property prop = pi.getProperty(unit.getPropertyID());
//                address = prop.getAddress();
//            } else if (editingCECase()) {
//                address = currentCase.getProperty().getAddress();
//            }
//
//        } catch (IntegrationException ex) {
//            System.out.println("PaymentBB had problems getting the currentAddress");
//        }
        return address;

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

    public int getSelectedPaymentPayer() {
        return selectedPayment.getPayer().getPersonID();
    }

    public void setSelectedPaymentPayer(int personID) {

        PersonIntegrator pi = new PersonIntegrator();

        try {
            selectedPayment.setPayer(pi.getPerson(personID));
        } catch (IntegrationException ex) {
            System.out.println(ex);

        }

    }

    public void onSelectedPayTypeButtonChange(PaymentType type) {
        // "Select" button was selected
        if (currentPaymentSelected == true) {

            selectedPaymentType = type;

            //update the current selected type list in side panel
            paymentTypeList = new ArrayList<>();
            paymentTypeList.add(type);

            //Message Noticefication
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Current Selected Payment Type: " + selectedPaymentType.getPaymentTypeTitle(), ""));

            // "Select" button wasn't selected
        } else {
            //turn to default setting
            selectedPaymentType = new PaymentType();

            currentPaymentSelected = false;

            //Message Noticefication
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Default Selected Payment Type: " + selectedPaymentType.getPaymentTypeTitle(), ""));
        }
    }

public String onUpdatePayTypeButtonChange(){
    
        PaymentIntegrator pti = getPaymentIntegrator();
        PaymentType pt = selectedPaymentType;

        try {
            pti.updatePaymentType(pt);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Payment type updated!", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to update Payment type in database.",
                            "This must be corrected by the System Administrator"));
        }

        return "paymentTypeManage";
        
    }

    public String onInsertPayTypeButtonChange(){
        PaymentType pt = new PaymentType();
        PaymentIntegrator pti = new PaymentIntegrator();
        pt.setPaymentTypeId(selectedPaymentType.getPaymentTypeId());
        pt.setPaymentTypeTitle(selectedPaymentType.getPaymentTypeTitle());
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

    public String onRemovePayTypeButtonChange(){
    
        PaymentIntegrator pti = getPaymentIntegrator();
        PaymentType pt = selectedPaymentType;

        try {
            pti.deletePaymentType(pt);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Payment type deleted!", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to delete Payment type in database.",
                            "It's probably being used somewhere in the database."));
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
                                "This payment will always be with us."));
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

    public String getCurrentMode() {
        return currentMode;
    }

    public boolean isCurrentPaymentSelected() {
        return currentPaymentSelected;
    }

    public void setCurrentPaymentSelected(boolean currentPaymentSelected) {
        this.currentPaymentSelected = currentPaymentSelected;
    }

    public OccPeriod getCurrentOccPeriod() {
        return currentOccPeriod;
    }

    public void setCurrentOccPeriod(OccPeriod currentOccPeriod) {
        this.currentOccPeriod = currentOccPeriod;
    }

    public FeeAssigned getSelectedAssignedFee() {
        return selectedAssignedFee;
    }

    public void setSelectedAssignedFee(FeeAssigned selectedAssignedFee) {
        this.selectedAssignedFee = selectedAssignedFee;
    }

    public ArrayList<FeeAssigned> getAssignedFeeList() {
        return feeAssignedList;
    }

    public void setAssignedFeeList(ArrayList<FeeAssigned> assignedFeeList) {
        this.feeAssignedList = assignedFeeList;
    }

    public ArrayList<MoneyOccPeriodFeeAssigned> getOccPeriodFilteredFeeList() {
        return occPeriodFilteredFeeList;
    }

    public void setOccPeriodFilteredFeeList(ArrayList<MoneyOccPeriodFeeAssigned> occPeriodFilteredFeeList) {
        this.occPeriodFilteredFeeList = occPeriodFilteredFeeList;
    }

    public CECaseDataHeavy getCurrentCase() {
        return currentCase;
    }

    public void setCurrentCase(CECaseDataHeavy currentCase) {
        this.currentCase = currentCase;
    }

    public EventDomainEnum getCurrentDomain() {
        return currentDomain;
    }

    public void setCurrentDomain(EventDomainEnum currentDomain) {
        this.currentDomain = currentDomain;
    }

    public boolean isRedirected() {
        return redirected;
    }

    public void setRedirected(boolean redirected) {
        this.redirected = redirected;
    }

}
