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
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.PaymentCoordinator;
import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.NavigationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.EventDomainEnum;
import com.tcvcog.tcvce.entities.FeeAssigned;
import com.tcvcog.tcvce.entities.MoneyOccPeriodFeeAssigned;
import com.tcvcog.tcvce.entities.Payment;
import com.tcvcog.tcvce.entities.PaymentType;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 *
 * @author Adam Gutonski
 */
public class PaymentBB extends BackingBeanUtils implements Serializable {

    private ArrayList<Payment> paymentList;
    private Payment selectedPayment;
    private ArrayList<PaymentType> paymentTypeList;
    private PaymentType selectedPaymentType;
    private PaymentType newSelectedPaymentType;
    private PaymentType newPaymentType;

    private OccPeriodDataHeavy currentOccPeriod;
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

        PaymentCoordinator pc = getPaymentCoordinator();
        try {
            paymentTypeList = pc.getPaymentTypes();
        } catch (IntegrationException ex) {
            paymentTypeList = new ArrayList<>();
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Oops! We encountered a problem trying to refresh the payment type list!", ""));
        }
        currentDomain = getSessionBean().getFeeManagementDomain();

        if (currentDomain == EventDomainEnum.OCCUPANCY) {

            OccupancyCoordinator oc = getOccupancyCoordinator();

            try {
                currentOccPeriod = oc.assembleOccPeriodDataHeavy(getSessionBean().getFeeManagementOccPeriod(), getSessionBean().getSessUser().getMyCredential());
            } catch (IntegrationException | BObStatusException | SearchException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Oops! We encountered a problem trying to refresh the fee assigned list!", ""));
            }

            feeAssignedList.addAll(currentOccPeriod.getFeeList());

            //Check if we've got a payment the user wants to edit.
            if (getSessionBean().getSessionPayment() != null) {
                paymentList.add(getSessionBean().getSessionPayment());
                paymentSet = true;

                //If we don't have a payment already in mind, let's grab the list from the database
            } else if (currentOccPeriod != null) {

                for (FeeAssigned fee : feeAssignedList) {
                    paymentList.addAll(fee.getPaymentList());
                }
                paymentSet = true;

            }

        } else if (currentDomain == EventDomainEnum.CODE_ENFORCEMENT) {

            CaseCoordinator cc = getCaseCoordinator();

            try {
                currentCase = cc.cecase_assembleCECaseDataHeavy(getSessionBean().getFeeManagementCeCase(), getSessionBean().getSessUser());
            } catch (SearchException | BObStatusException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Oops! We encountered a problem trying to load the current case!", ""));
            }

            //Check if we've got a payment the user wants to edit.
            if (getSessionBean().getSessionPayment() != null) {

                paymentList.add(getSessionBean().getSessionPayment());
                paymentSet = true;
                feeAssignedList.addAll(currentCase.getFeeList());

                //If we don't have a payment already in mind, let's grab the list from the database
            } else if (currentCase != null) {

                feeAssignedList.addAll(currentCase.getFeeList());

                for (FeeAssigned fee : feeAssignedList) {

                    paymentList.addAll(fee.getPaymentList());

                }

                paymentSet = true;

            }

        }

        if (!paymentSet) {
            try {
                paymentList = pc.getAllPayments();
            } catch (IntegrationException ex) {
                paymentList = new ArrayList<>();
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to load payment list!",
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
     */
    public void onPaymentSelectedButtonChange(Payment p) {

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

        PaymentCoordinator pc = getPaymentCoordinator();

        if (selectedAssignedFee == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a fee to assign this payment to.", " "));
            selectedPayment.setPayer(new Person());
            return "";
        }

        try {
            pc.insertPayment(selectedPayment, selectedAssignedFee);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully added payment record to database!", ""));
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to add payment record to database, sorry!", "Check server print out..."));
        }

        return "payments";
    }

    public String onUpdateButtonChange() {

        PaymentCoordinator pc = getPaymentCoordinator();

        try {
            pc.updatePayment(selectedPayment, selectedAssignedFee);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Payment record updated!", ""));
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to update payment record in database.",
                            "This must be corrected by the System Administrator"));
        }

        return "payments";

    }

    public String onRemoveButtonChange() {

        PaymentCoordinator pc = getPaymentCoordinator();

        try {
            pc.removePayment(selectedPayment);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Payment record deleted forever!", ""));
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to delete payment record--probably because it is used "
                            + "somewhere in the database. Sorry.",
                            "This payment will always be with us."));
        }
        return "payments";

    }

    public String finishAndRedir() {
        try {
            return getSessionBean().getNavStack().popLastPage();
        } catch (NavigationException ex) {
            System.out.println("PaymentBB.finishAndRedir() | ERROR: " + ex);
            //We must do things a little bit different here to make sure messages are kept after the redirect.
            FacesContext context = getFacesContext();
                    context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to direct you back to the page you were on."
                            + " No changes to the database were saved. Please return to the page manually.",
                            "Do not hit the return button again but note the error."));
                    context.getExternalContext().getFlash().setKeepMessages(true);
            return "";
        }
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
     * Gets the current address the user is editing
     *
     * @return
     */
    public String getCurrentAddress() {

        String address = "";

        if (editingOccPeriod()) {

            PaymentCoordinator pc = getPaymentCoordinator();

            try {
                address = pc.getAddressFromPropUnitID(currentOccPeriod.getPropertyUnitID());
            } catch (IntegrationException ex) {
                address = "";
                System.out.println(ex.toString());
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to find property address!",
                                ""));
            }

        } else if (editingCECase()) {

            CaseCoordinator cc = getCaseCoordinator();
            try {
                address = cc.cecase_assembleCECasePropertyUnitHeavy(currentCase).getProperty().getAddress();
            } catch (IntegrationException | SearchException ex) {
                System.out.println(ex);
            }

        }

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

        PersonCoordinator pc = getPersonCoordinator();

        try {
            selectedPayment.setPayer(pc.getPerson(personID));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to find Payer with Person ID: " + personID,
                            ""));
        }

    }

    public void onSelectedPayTypeButtonChange(PaymentType type) {
        // "Select" button was selected
        if (currentPaymentSelected == true) {

            selectedPaymentType = type;

            //update the current selected type list in side panel
            paymentTypeList = new ArrayList<>();
            paymentTypeList.add(selectedPaymentType);

            //Message Noticefication
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Current Selected Payment Type: " + selectedPaymentType.getPaymentTypeTitle(), ""));

            // "Select" button wasn't selected
        } else {
            //turn to default setting
            selectedPaymentType = new PaymentType();

            currentPaymentSelected = false;

            PaymentCoordinator pc = getPaymentCoordinator();

            try {
                paymentTypeList = pc.getPaymentTypes();
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "An error occured while trying to load payment types!",
                                ""));
                paymentTypeList = new ArrayList<>();
                System.out.println(ex.toString());
            }

            //Message Noticefication
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Default Selected Payment Type: " + selectedPaymentType.getPaymentTypeTitle(), ""));
        }
    }

    public String onUpdatePayTypeButtonChange() {

        PaymentCoordinator pc = getPaymentCoordinator();

        try {
            pc.updatePaymentType(selectedPaymentType);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Payment type updated!", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to update Payment type in database.",
                            "This must be corrected by the System Administrator"));
        }

        return "paymentTypeManage";

    }

    public String onInsertPayTypeButtonChange() {

        PaymentCoordinator pc = getPaymentCoordinator();

        try {
            pc.insertPaymentType(selectedPaymentType);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully added payment type to database!", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to add payment type to database, sorry!", "Check server print out..."));
        }
        return "paymentTypeManage";

    }

    public String onRemovePayTypeButtonChange() {

        PaymentCoordinator pc = getPaymentCoordinator();

        try {
            pc.removePaymentType(selectedPaymentType);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Payment type deleted forever!", ""));
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, ex.getMessage(), ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to delete payment type--probably because it is used "
                            + "somewhere in the database. Sorry.",
                            "This payment will always be with us."));
        }

        return "paymentTypeManage";
    }

    /**
     * @return the paymentTypeList
     */
    public List<PaymentType> getPaymentTypeList() {

        return paymentTypeList;

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

    public String getCurrentMode() {
        return currentMode;
    }

    public boolean isCurrentPaymentSelected() {
        return currentPaymentSelected;
    }

    public void setCurrentPaymentSelected(boolean currentPaymentSelected) {
        this.currentPaymentSelected = currentPaymentSelected;
    }

    public OccPeriodDataHeavy getCurrentOccPeriod() {
        return currentOccPeriod;
    }

    public void setCurrentOccPeriod(OccPeriodDataHeavy currentOccPeriod) {
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
