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
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.EventDomainEnum;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Fee;
import com.tcvcog.tcvce.entities.FeeAssigned;
import com.tcvcog.tcvce.entities.MoneyCECaseFeeAssigned;
import com.tcvcog.tcvce.entities.MoneyOccPeriodFeeAssigned;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import com.tcvcog.tcvce.occupancy.integration.PaymentIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.*;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Adam Gutonski
 */
@ViewScoped
public class FeeManagementBB extends BackingBeanUtils implements Serializable {

    //feeTypeManage.xhtml fields
    private ArrayList<Fee> existingFeeTypeList;
    private Fee selectedFeeType;
    private Municipality formMuni;

    //feeManage.xhtml fields
    private OccPeriod currentOccPeriod;
    private CECase currentCase;
    private Fee formFee;
    private Fee selectedFee;
    private ArrayList<Fee> feeList;
    private ArrayList<Fee> filteredFeeList;
    private ArrayList<CodeViolation> violationList;
    private ArrayList<CodeViolation> filteredViolationList;
    private CodeViolation selectedViolation;

    private FeeAssigned selectedAssignedFee;
    private ArrayList<FeeAssigned> feeAssignedList;
    private ArrayList<FeeAssigned> filteredFeeAssignedList;

    //feePermissions.xhtml fields
    private ArrayList<OccPeriodType> typeList;
    private ArrayList<OccPeriodType> filteredTypeList;
    private OccPeriodType selectedPeriodType;
    private OccPeriodType lockedPeriodType;

    private ArrayList<EnforcableCodeElement> elementList;
    private ArrayList<EnforcableCodeElement> filteredElementList;
    private EnforcableCodeElement selectedCodeElement;
    private EnforcableCodeElement lockedCodeElement;

    private List<Fee> existingFeeList;
    private ArrayList<Fee> workingFeeList;
    private Fee selectedWorkingFee;
    private List<Fee> allFees;

    //Generalized fields
    private EventDomainEnum currentDomain;
    private String currentMode;
    private boolean waived;
    private boolean redirected;
    private boolean currentFeeSelected;

    /**
     * Creates a new instance of NewJSFManagedBean
     */
    public FeeManagementBB() {
    }

    @PostConstruct
    public void initBean() {
        formFee = new Fee();
        formFee.setEffectiveDate(LocalDateTime.now());
        formFee.setExpiryDate(LocalDateTime.now());

        PaymentIntegrator pi = getPaymentIntegrator();

        selectedAssignedFee = new FeeAssigned();

        currentMode = "Lookup";

        //initialize default select button in list-column: false
        currentFeeSelected = false;

        if (getSessionBean().getNavStack().peekLastPage() != null) {

            refreshFeeAssignedList();

            redirected = true;

            if (allFees == null) {
                try {
                    allFees = pi.getFeeTypeList(getSessionBean().getSessMuni());
                } catch (IntegrationException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Oops! We encountered a problem trying to fetch the fee list!", ""));
                }
            }

            if (feeList == null) {
                feeList = (ArrayList<Fee>) allFees;
            }
            try {
                refreshTypesAndElements();
            } catch (BObStatusException e) {
                //TODO: make this try-catch not necessary. Maybe move alot to the coordinator?
            }
            if (currentCase != null) {
                violationList = (ArrayList<CodeViolation>) currentCase.getViolationList();
            }

        }
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
        return !("Lookup".equals(currentMode) || "Update".equals(currentMode));
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
        currentFeeSelected = false;
        //check the currentMode == null or not
        if (currentMode == null) {
            this.currentMode = tempCurMode;
        } else {
            this.currentMode = currentMode;
        }
        //create an instance object of fees if current mode == "Insert"
        if (getActiveInsertMode()) {
            selectedAssignedFee = new FeeAssigned();
            
            selectedFee = new Fee();
        }
        //show the current mode in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, this.currentMode + " Mode Selected", ""));
    }

    /**
     * Changing which fee is selected and not selected
     *
     * @param currentFee
     * @throws IntegrationException
     */
    public void onAssignedFeeSelectedButtonChange(FeeAssigned currentFee) throws IntegrationException {

        // "Select" button was selected
        if (currentFeeSelected == true) {

            //Set the correct domain and create the correct type of Assigned Fee object
            if (currentDomain == EventDomainEnum.OCCUPANCY) {

                MoneyOccPeriodFeeAssigned skeleton = (MoneyOccPeriodFeeAssigned) currentFee;

                if (selectedAssignedFee != null) {
                    skeleton.setOccPeriodID(currentOccPeriod.getPeriodID());

                } else {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Please select an assigned fee to update", ""));
                }
            } else {

                MoneyCECaseFeeAssigned skeleton = (MoneyCECaseFeeAssigned) currentFee;

                if (selectedAssignedFee != null) {

                    skeleton.setCaseID(currentCase.getCaseID());

                } else {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Please select an assigned fee to update", ""));
                }

                //set selected fee
                selectedAssignedFee = skeleton;
                //update the current selected fee list in side panel
                feeAssignedList = new ArrayList<>();
                feeAssignedList.add(currentFee);

            }

            //Message Noticefication
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Current Selected Assigned Fee: " + selectedAssignedFee.getAssignedFeeID(), ""));

            // "Select" button wasn't selected
        } else {
            //turn to default setting
            currentFeeSelected = false;

            refreshFeeAssignedList();

            //Message Noticefication
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Default Selected Fee: " + selectedAssignedFee.getAssignedFeeID(), ""));
        }

    }

    public String onInsertAssignedFeeButtonChange() {
        if (selectedAssignedFee.getFee() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a fee to assign", ""));
        }

        FeeAssigned firstSkeleton = new FeeAssigned();
        PaymentIntegrator pi = getPaymentIntegrator();

        firstSkeleton.setPaymentList(selectedAssignedFee.getPaymentList());
        firstSkeleton.setMoneyFeeAssigned(selectedAssignedFee.getMoneyFeeAssigned());
        firstSkeleton.setAssignedBy(getSessionBean().getSessUser());
        firstSkeleton.setAssigned(LocalDateTime.now());
        firstSkeleton.setLastModified(LocalDateTime.now());
        firstSkeleton.setNotes(selectedAssignedFee.getNotes());
        firstSkeleton.setFee(selectedAssignedFee.getFee());

        if (waived == true) {
            firstSkeleton.setWaivedBy(getSessionBean().getSessUser());
        } else {
            firstSkeleton.setWaivedBy(new User());
        }

        if (selectedAssignedFee.getReducedBy() != 0) {

            firstSkeleton.setReducedBy(selectedAssignedFee.getReducedBy());
            firstSkeleton.setReducedByUser(getSessionBean().getSessUser());

        } else if (selectedAssignedFee.getReducedBy() < 0) {

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "You cannot reduce a fee by a negative number", ""));

        } else {
            firstSkeleton.setReducedByUser(new User());
        }

        if (currentDomain == EventDomainEnum.OCCUPANCY) {
            MoneyOccPeriodFeeAssigned secondSkeleton = new MoneyOccPeriodFeeAssigned(firstSkeleton);
            MoneyOccPeriodFeeAssigned occPeriodFormFee = (MoneyOccPeriodFeeAssigned) selectedAssignedFee;

            secondSkeleton.setOccPerAssignedFeeID(occPeriodFormFee.getOccPerAssignedFeeID());
            secondSkeleton.setOccPeriodID(currentOccPeriod.getPeriodID());
            secondSkeleton.setOccPeriodTypeID(currentOccPeriod.getType().getTypeID());

            try {
                pi.insertOccPeriodFee(secondSkeleton);
                refreshFeeAssignedList();
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully added new fee!", ""));
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to add fee to database, sorry!", "Check server print out..."));
            }

        } else {

            MoneyCECaseFeeAssigned secondSkeleton = new MoneyCECaseFeeAssigned(firstSkeleton);
            MoneyCECaseFeeAssigned caseFormFee = new MoneyCECaseFeeAssigned(selectedAssignedFee);

            secondSkeleton.setCeCaseAssignedFeeID(caseFormFee.getCeCaseAssignedFeeID());
            secondSkeleton.setCaseID(currentCase.getCaseID());
            secondSkeleton.setCodeSetElement(selectedViolation.getCodeViolated().getCodeSetElementID());

            try {
                pi.insertCECaseFee(secondSkeleton);
                refreshFeeAssignedList();
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to add fee to database, sorry!", "Check server print out..."));
            }

        }
        return "feeManage";
    }

    public String onUpdateAssignedFeeButtonChange() {
        FeeAssigned firstSkeleton = new FeeAssigned();
        PaymentIntegrator pi = getPaymentIntegrator();

        firstSkeleton.setAssignedFeeID(selectedAssignedFee.getAssignedFeeID());
        firstSkeleton.setPaymentList(selectedAssignedFee.getPaymentList());
        firstSkeleton.setMoneyFeeAssigned(selectedAssignedFee.getMoneyFeeAssigned());
        firstSkeleton.setAssignedBy(getSessionBean().getSessUser());
        firstSkeleton.setAssigned(LocalDateTime.now());
        firstSkeleton.setLastModified(LocalDateTime.now());
        firstSkeleton.setNotes(selectedAssignedFee.getNotes());
        firstSkeleton.setFee(selectedAssignedFee.getFee());

        if (waived == true) {
            firstSkeleton.setWaivedBy(getSessionBean().getSessUser());
        } else {
            firstSkeleton.setWaivedBy(new User());
        }

        if (selectedAssignedFee.getReducedBy() < 0) {

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "You cannot reduce a fee by a negative number", ""));

            return "";
        }

        if (selectedAssignedFee.getReducedBy() != 0) {

            firstSkeleton.setReducedBy(selectedAssignedFee.getReducedBy());
            firstSkeleton.setReducedByUser(getSessionBean().getSessUser());

        } else {
            firstSkeleton.setReducedByUser(new User());
        }

        if (currentDomain == EventDomainEnum.OCCUPANCY) {
            MoneyOccPeriodFeeAssigned secondSkeleton = new MoneyOccPeriodFeeAssigned(firstSkeleton);
            MoneyOccPeriodFeeAssigned occPeriodFormFee = (MoneyOccPeriodFeeAssigned) selectedAssignedFee;

            secondSkeleton.setOccPerAssignedFeeID(occPeriodFormFee.getOccPerAssignedFeeID());
            secondSkeleton.setOccPeriodID(currentOccPeriod.getPeriodID());
            secondSkeleton.setOccPeriodTypeID(currentOccPeriod.getType().getTypeID());

            try {
                pi.updateOccPeriodFee(secondSkeleton);
                refreshFeeAssignedList();
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully updated fee!", ""));
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to update fee in database, sorry!", "Check server print out..."));
            }

        } else {

            MoneyCECaseFeeAssigned secondSkeleton = new MoneyCECaseFeeAssigned(firstSkeleton);
            MoneyCECaseFeeAssigned caseFormFee = (MoneyCECaseFeeAssigned) selectedAssignedFee;

            secondSkeleton.setCeCaseAssignedFeeID(caseFormFee.getCeCaseAssignedFeeID());
            secondSkeleton.setCaseID(currentCase.getCaseID());
            secondSkeleton.setCodeSetElement(selectedViolation.getCodeViolated().getCodeSetElementID());

            try {
                pi.updateCECaseFee(secondSkeleton);
                refreshFeeAssignedList();
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully updated fee!", ""));
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to update fee in database, sorry!", "Check server print out..."));
            }

        }
        return "muniManage";
    }

    public String onRemoveAssignedFeeButtonChange() {

        if (selectedAssignedFee == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a fee from the table to waive.", ""));

            return "";
        }

        selectedAssignedFee.setWaivedBy(getSessionBean().getSessUser());

        PaymentIntegrator pi = getPaymentIntegrator();

        if (currentDomain == EventDomainEnum.OCCUPANCY) {

            try {
                pi.updateOccPeriodFee((MoneyOccPeriodFeeAssigned) selectedAssignedFee);
                refreshFeeAssignedList();
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Fee waived!", ""));
            } catch (IntegrationException ex) {
                System.out.println(ex.toString());
            }
        }
        if (currentDomain == EventDomainEnum.CODE_ENFORCEMENT) {

            try {
                pi.updateCECaseFee((MoneyCECaseFeeAssigned) selectedAssignedFee);
                refreshFeeAssignedList();
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Fee waived!", ""));
            } catch (IntegrationException ex) {
                System.out.println(ex.toString());
            }
        }

        return "muniManage";
    }

    public String finishAndRedir() {

        return getSessionBean().getNavStack().popLastPage();
    }

    public String goToFeePermissions() {

        if (editingCECase() || editingOccPeriod()) {
            getSessionBean().getNavStack().pushCurrentPage();
        }
        return "feePermissions";
    }

    public String whatDomainFees() {
        if (currentDomain == EventDomainEnum.OCCUPANCY) {

            return "occupancy periods";

        } else {

            return "code enforcement elements";
        }

    }
    
    public void editFeeType(ActionEvent e) {
        if (getSelectedFeeType() != null) {
            formFee.setOccupancyInspectionFeeID(selectedFeeType.getOccupancyInspectionFeeID());
            formFee.setMuni(selectedFeeType.getMuni());
            formFee.setName(selectedFeeType.getName());
            formFee.setAmount(selectedFeeType.getAmount());
            formFee.setNotes(selectedFeeType.getNotes());
            
            //Have to figure out what to do w/ setting dates...
            //setFormOccupancyInspectionFeeEffDate(formFeeEffDate.toInstant()
            //        .atZone(ZoneId.systemDefault())
            //        .toLocalDateTime());
             
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select an occupancy inspection fee to update", ""));
        }
    }
    
    public void commitFeeUpdates(ActionEvent e) {
        OccupancyIntegrator oifi = getOccupancyIntegrator();
        PaymentIntegrator pi = getPaymentIntegrator();
        Fee oif = selectedFeeType;

        oif.setMuni(formFee.getMuni());
        oif.setName(formFee.getName());
        oif.setAmount(formFee.getAmount());
        oif.setEffectiveDate(formFee.getEffectiveDate());
        oif.setExpiryDate(formFee.getExpiryDate());
        oif.setNotes(formFee.getNotes());
        try {
            pi.updateOccupancyInspectionFee(oif);
        } catch (IntegrationException ex) {
        }
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Occupancy Inspection Fee updated!", ""));
    }

    
    public void initializeNewFee(ActionEvent e) {

        formFee = new Fee();
        formFee.setEffectiveDate(LocalDateTime.now());
        formFee.setExpiryDate(LocalDateTime.now());
    }

    public String saveNewFeeType() {
        PaymentIntegrator pi = getPaymentIntegrator();
        Fee oif = new Fee();
        oif.setOccupancyInspectionFeeID(formFee.getOccupancyInspectionFeeID());
        oif.setMuni(getFormMuni());
        oif.setName(formFee.getName());
        oif.setAmount(formFee.getAmount());
        oif.setEffectiveDate(formFee.getEffectiveDate());
        oif.setExpiryDate(formFee.getExpiryDate());
        oif.setNotes(formFee.getNotes());
        try {
            pi.insertOccupancyInspectionFee(oif);
        } catch (IntegrationException ex) {
        }
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Successfully added occupancy inspection fee to database!", ""));

        return "occupancyInspectionFeeManage";

    }

    public void deleteSelectedFee(ActionEvent e) {
        PaymentIntegrator pi = getPaymentIntegrator();

        if (getSelectedFeeType() != null) {
            try {
                pi.deleteOccupancyInspectionFee(getSelectedFeeType());
            } catch (IntegrationException ex) {
                System.out.println(ex.toString());
            }
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Occupancy inspection fee deleted forever!", ""));

        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select an occupancy inspection fee from the table to delete", ""));
        }
    }

    /**
     * @return the existingFeeTypeList
     */
    public ArrayList<Fee> getFeeTypeList() {
        PaymentIntegrator pi = getPaymentIntegrator();
        try {
            existingFeeTypeList = pi.getOccupancyInspectionFeeList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        if (existingFeeTypeList != null) {
            return existingFeeTypeList;
        } else {
            existingFeeTypeList = new ArrayList();
            return existingFeeTypeList;
        }
    }

    public void editOccPeriodFees() {

        try {
            lockedPeriodType = (OccPeriodType) selectedPeriodType.clone();
        } catch (CloneNotSupportedException ex) {
            System.out.println("OccPeriodType had a problem cloning. Oops!");
        }

        try {
            existingFeeList = lockedPeriodType.getPermittedFees();
            workingFeeList = new ArrayList<>(existingFeeList);
        } catch (NullPointerException e) {
            System.out.println("OccPeriodType has no existing permitted fee list, making new ArrayList...");
            workingFeeList = new ArrayList<>();
        }

    }

    public void removePermittedFee(Fee selectedFee) {
        workingFeeList.remove(selectedFee);
    }

    public void editCodeElementFees() {

        try {
            lockedCodeElement = (EnforcableCodeElement) selectedCodeElement.clone();
        } catch (CloneNotSupportedException ex) {
            System.out.println("EnforcableCodeElement had a problem cloning. Oops!");
        }

        try {
            existingFeeList = lockedCodeElement.getFeeList();
            workingFeeList = new ArrayList<>(existingFeeList);
        } catch (NullPointerException e) {
            System.out.println("EnforcableCodeElement has no existing permitted fee list, making new ArrayList...");
            workingFeeList = new ArrayList<>();
        }

    }

    public void addFeeToPermittedFees() {

        if (selectedFee == null) {

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a fee to add to the list.", ""));

        } else {

            boolean duplicate = false;

            for (Fee test : workingFeeList) {
                duplicate = test.getOccupancyInspectionFeeID() == selectedFee.getOccupancyInspectionFeeID();
                if (duplicate) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "You cannot permit the same fee twice.", ""));
                    break;
                }

            }
            if (!duplicate) {
                workingFeeList.add(selectedFee);
            }
        }
    }

    public void commitPermissionUpdates() throws BObStatusException {

        if (existingFeeList == null) {

            for (Fee workingFee : workingFeeList) {

                if (currentDomain == EventDomainEnum.OCCUPANCY) {
                    insertOrReactivateOccPeriodJoin(workingFee);
                } else if (currentDomain == EventDomainEnum.CODE_ENFORCEMENT) {
                    insertOrReactivateCodeElementJoin(workingFee);
                }
            }

        } else {

            if (currentDomain == EventDomainEnum.OCCUPANCY) {

                scanExistingOccPeriodFeeListWithDeactivate();

            } else if (currentDomain == EventDomainEnum.CODE_ENFORCEMENT) {

                scanExistingCodeElementFeeListWithDeactivate();

            }

            if (currentDomain == EventDomainEnum.OCCUPANCY) {

                scanExistingOccPeriodFeeListWithInsert();

            } else if (currentDomain == EventDomainEnum.CODE_ENFORCEMENT) {

                scanExistingCodeElementFeeListWithInsert();

            }
        }

        refreshTypesAndElements();

    }

    public void insertOrReactivateOccPeriodJoin(Fee workingFee) {

        PaymentIntegrator pi = getPaymentIntegrator();
        boolean failed = false;

        try {
            pi.insertFeePeriodTypeJoin(workingFee, lockedPeriodType);
        } catch (IntegrationException ex) {
            System.out.println("Failed inserting occperiod fee join, trying to reactivate.");
            failed = true;
        }

        if (failed) {

            try {
                pi.reactivateFeePeriodTypeJoin(workingFee, lockedPeriodType);
            } catch (IntegrationException ex) {
                System.out.println("FeeManagementBB.commitPermissionUpdates() | Error: " + ex.toString());
            }

        }

    }

    public void insertOrReactivateCodeElementJoin(Fee workingFee) {

        PaymentIntegrator pi = getPaymentIntegrator();
        boolean failed = false;

        try {
            pi.insertFeeCodeElementJoin(workingFee, lockedCodeElement);
        } catch (IntegrationException ex) {
            System.out.println("Failed inserting code element fee join, trying to reactivate.");
            failed = true;
        }

        if (failed) {

            try {
                pi.reactivateFeeCodeElementJoin(workingFee, lockedCodeElement);
            } catch (IntegrationException ex) {
                System.out.println("FeeManagementBB.commitPermissionUpdates() | Error: " + ex.toString());
            }

        }

    }

    public void scanExistingOccPeriodFeeListWithInsert() {

        PaymentIntegrator pi = getPaymentIntegrator();

        for (Fee workingFee : workingFeeList) {
            int notThisFee = 0;
            for (Fee existingFee : existingFeeList) {

                if (existingFee.getOccupancyInspectionFeeID() == workingFee.getOccupancyInspectionFeeID()) {
                    try {
                        pi.updateFeePeriodTypeJoin(workingFee, lockedPeriodType);
                    } catch (IntegrationException ex) {
                        System.out.println("FeeManagementBB.commitPermissionUpdates() | Error: " + ex.toString());
                    }
                    break;
                } else {
                    notThisFee++;
                }

            }

            if (notThisFee == existingFeeList.size()) {
                insertOrReactivateOccPeriodJoin(workingFee);

            }
        }

    }

    public void scanExistingCodeElementFeeListWithInsert() {

        PaymentIntegrator pi = getPaymentIntegrator();

        for (Fee workingFee : workingFeeList) {
            int notThisFee = 0;
            for (Fee existingFee : existingFeeList) {

                if (existingFee.getOccupancyInspectionFeeID() == workingFee.getOccupancyInspectionFeeID()) {
                    try {
                        pi.updateFeeCodeElementJoin(workingFee, lockedCodeElement);
                    } catch (IntegrationException ex) {
                        System.out.println("FeeManagementBB.commitPermissionUpdates() | Error: " + ex.toString());
                    }
                    break;
                } else {
                    notThisFee++;
                }

            }

            if (notThisFee == existingFeeList.size()) {
                insertOrReactivateCodeElementJoin(workingFee);
            }

        }

    }

    public void scanExistingOccPeriodFeeListWithDeactivate() {

        PaymentIntegrator pi = getPaymentIntegrator();

        for (Fee existingFee : existingFeeList) {
            int notThisFee = 0;
            for (Fee workingFee : workingFeeList) {
                if (existingFee.getOccupancyInspectionFeeID() != workingFee.getOccupancyInspectionFeeID()) {
                    notThisFee++;
                }

            }

            if (notThisFee == workingFeeList.size()) {

                try {
                    pi.deactivateFeePeriodTypeJoin(existingFee, lockedPeriodType);
                } catch (IntegrationException ex) {
                    System.out.println("FeeManagementBB.commitPermissionUpdates() | Error: " + ex.toString());
                }

            }

        }

    }

    public void scanExistingCodeElementFeeListWithDeactivate() {

        PaymentIntegrator pi = getPaymentIntegrator();

        for (Fee existingFee : existingFeeList) {
            int notThisFee = 0;
            for (Fee workingFee : workingFeeList) {
                if (existingFee.getOccupancyInspectionFeeID() != workingFee.getOccupancyInspectionFeeID()) {
                    notThisFee++;
                }

            }

            if (notThisFee == workingFeeList.size()) {

                try {
                    pi.deactivateFeeCodeElementJoin(existingFee, lockedCodeElement);
                } catch (IntegrationException ex) {
                    System.out.println("FeeManagementBB.commitPermissionUpdates() | Error: " + ex.toString());
                }

            }

        }

    }

    /**
     * Refreshes the lists of assigned fees, the current Domain, etc.
     */
    public void refreshFeeAssignedList() {

        feeAssignedList = new ArrayList<>();

        PaymentIntegrator pi = getPaymentIntegrator();

        currentDomain = getSessionBean().getFeeManagementDomain();

        if (currentDomain == EventDomainEnum.OCCUPANCY) {

            currentOccPeriod = getSessionBean().getFeeManagementOccPeriod();

            if (currentOccPeriod != null) {

                try {
                    ArrayList<MoneyOccPeriodFeeAssigned> tempList = (ArrayList<MoneyOccPeriodFeeAssigned>) pi.getFeeAssigned(currentOccPeriod);

                    for (MoneyOccPeriodFeeAssigned fee : tempList) {

                        FeeAssigned skeleton = fee;

                        skeleton.setAssignedFeeID(fee.getOccPerAssignedFeeID());
                        skeleton.setDomain(currentDomain);
                        feeAssignedList.add(skeleton);

                    }

                    feeList = (ArrayList<Fee>) currentOccPeriod.getType().getPermittedFees();
                } catch (IntegrationException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Oops! We encountered a problem trying to refresh the fee assigned list!", ""));
                }

            }

        } else if (currentDomain == EventDomainEnum.CODE_ENFORCEMENT) {

            currentCase = getSessionBean().getFeeManagementCeCase();

            if (currentCase != null) {

                try {

                    // TODO NADGIT is this cast okay?
                    List<MoneyCECaseFeeAssigned> tempList = (ArrayList<MoneyCECaseFeeAssigned>) pi.getFeeAssigned((CECaseDataHeavy) currentCase);

                    for (MoneyCECaseFeeAssigned fee : tempList) {

                        FeeAssigned skeleton = fee;

                        skeleton.setAssignedFeeID(fee.getCeCaseAssignedFeeID());
                        skeleton.setDomain(currentDomain);
                        feeAssignedList.add(skeleton);

                    }
                    feeList = new ArrayList<>();
                } catch (IntegrationException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Oops! We encountered a problem trying to refresh the fee assigned list!", ""));
                }
            }
        }
    }

    public void refreshTypesAndElements() throws BObStatusException {

        OccupancyIntegrator oi = getOccupancyIntegrator();
        CodeIntegrator ci = getCodeIntegrator();
        CaseIntegrator csi = getCaseIntegrator();

        try {
            typeList = (ArrayList<OccPeriodType>) oi.getOccPeriodTypeList(getSessionBean().getSessMuni().getProfile().getProfileID());
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Oops! We encountered a problem trying to fetch the OccPeriodType List!", ""));
        }

        try {
            ArrayList<CodeSet> codeSetList = ci.getCodeSets(getSessionBean().getSessMuni().getMuniCode());

            elementList = new ArrayList<>();

            for (CodeSet set : codeSetList) {

                elementList.addAll(ci.getEnforcableCodeElementList(set.getCodeSetID()));

            }

        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Oops! We encountered a problem trying to fetch the CodeSetElement List!", ""));
        }

        try {
            currentCase = csi.getCECase(currentCase.getCaseID());

        } catch (IntegrationException | BObStatusException ex) {
            System.out.println("FeeManagementBB.refreshTypesAndElements() | Error: " + ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Oops! We encountered a problem trying to refresh the currentCase!", ""));
        }

    }

    public void violationSelected(ActionEvent e) {

        feeList.clear();

        feeList.addAll(selectedViolation.getViolatedEnfElement().getFeeList());

    }

    /**
     * @param existingFeeTypeList the existingFeeTypeList to set
     */
    public void setExistingFeeTypeList(ArrayList<Fee> existingFeeTypeList) {
        this.existingFeeTypeList = existingFeeTypeList;
    }

    /**
     * @return the selectedFeeType
     */
    public Fee getSelectedFeeType() {
        return selectedFeeType;
    }

    /**
     * @param selectedFeeType the selectedFeeType to set
     */
    public void setSelectedFeeType(Fee selectedFeeType) {
        this.selectedFeeType = selectedFeeType;
    }

    /**
     * @return the formFeeID
     */
    public int getFormFeeID() {
        return formFee.getOccupancyInspectionFeeID();
    }

    /**
     * @param formFeeID the formFeeID to set
     */
    public void setFormFeeID(int formFeeID) {
        this.formFee.setOccupancyInspectionFeeID(formFeeID);
    }

    /**
     * @return the formMuni
     */
    public Municipality getFormMuni() {
        return formMuni;
    }

    /**
     * @param formMuni the formMuni to set
     */
    public void setFormMuni(Municipality formMuni) {
        this.formMuni = formMuni;
    }

    /**
     * @return the formFeeName
     */
    public String getFormFeeName() {
        return formFee.getName();
    }

    /**
     * @param formFeeName the formFeeName to set
     */
    public void setFormFeeName(String formFeeName) {
        this.formFee.setName(formFeeName);
    }

    /**
     * @return the formFeeAmount
     */
    public double getFormFeeAmount() {
        return formFee.getAmount();
    }

    /**
     * @param formFeeAmount the formFeeAmount to set
     */
    public void setFormFeeAmount(double formFeeAmount) {
        this.formFee.setAmount(formFeeAmount);
    }

    /**
     * @return the formFeeEffDate
     */
    public java.util.Date getFormFeeEffDate() {
        return java.util.Date.from(formFee.getEffectiveDate()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    /**
     * @param formFeeEffDate the formFeeEffDate to set
     */
    public void setFormFeeEffDate(java.util.Date formFeeEffDate) {
        this.formFee.setEffectiveDate(
                LocalDateTime.ofInstant(formFeeEffDate.toInstant(),
                        ZoneId.systemDefault()));
    }

    /**
     * @return the formFeeExpDate
     */
    public java.util.Date getFormFeeExpDate() {
        return java.util.Date.from(formFee.getExpiryDate()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    /**
     * @param formFeeExpDate the formFeeExpDate to set
     */
    public void setFormFeeExpDate(java.util.Date formFeeExpDate) {
        this.formFee.setExpiryDate(
                LocalDateTime.ofInstant(formFeeExpDate.toInstant(),
                        ZoneId.systemDefault()));
    }

    /**
     * @return the formFeeNotes
     */
    public String getFormFeeNotes() {
        return formFee.getNotes();
    }

    /**
     * @param formFeeNotes the formFeeNotes to set
     */
    public void setFormFeeNotes(String formFeeNotes) {
        this.formFee.setNotes(formFeeNotes);
    }

    public String getCurrentMode() {
        return currentMode;
    }

    public Fee getFormFee() {
        return formFee;
    }

    public void setFormFee(Fee formFee) {
        this.formFee = formFee;
    }

    public Fee getSelectedFee() {
        return selectedFee;
    }

    public void setSelectedFee(Fee selectedFee) {
        this.selectedFee = selectedFee;
    }

    public ArrayList<Fee> getFeeList() {
        return feeList;
    }

    public void setFeeList(ArrayList<Fee> feeList) {
        this.feeList = feeList;
    }

    public ArrayList<CodeViolation> getViolationList() {
        return violationList;
    }

    public void setViolationList(ArrayList<CodeViolation> violationList) {
        this.violationList = violationList;
    }

    public FeeAssigned getSelectedAssignedFee() {
        return selectedAssignedFee;
    }

    public void setSelectedAssignedFee(FeeAssigned selectedAssignedFee) {
        this.selectedAssignedFee = selectedAssignedFee;
    }

    public ArrayList<FeeAssigned> getFeeAssignedList() {
        return feeAssignedList;
    }

    public void setFeeAssignedList(ArrayList<FeeAssigned> feeAssignedList) {
        this.feeAssignedList = feeAssignedList;
    }

    public ArrayList<FeeAssigned> getFilteredFeeAssignedList() {
        return filteredFeeAssignedList;
    }

    public void setFilteredFeeAssignedList(ArrayList<FeeAssigned> filteredFeeAssignedList) {
        this.filteredFeeAssignedList = filteredFeeAssignedList;
    }

    public boolean isWaived() {
        return waived;
    }

    public void setWaived(boolean waived) {
        this.waived = waived;
    }

    public ArrayList<Fee> getFilteredFeeList() {
        return filteredFeeList;
    }

    public void setFilteredFeeList(ArrayList<Fee> filteredFeeList) {
        this.filteredFeeList = filteredFeeList;
    }

    public OccPeriod getCurrentOccPeriod() {
        return currentOccPeriod;
    }

    public void setCurrentOccPeriod(OccPeriod currentOccPeriod) {
        this.currentOccPeriod = currentOccPeriod;
    }

    public boolean editingOccPeriod() {
        return (getSessionBean().getNavStack().peekLastPage() != null && currentOccPeriod != null && currentDomain == EventDomainEnum.OCCUPANCY);
    }

    public boolean editingCECase() {
        return (getSessionBean().getNavStack().peekLastPage() != null && currentCase != null && currentDomain == EventDomainEnum.CODE_ENFORCEMENT);
    }

    public ArrayList<OccPeriodType> getTypeList() {
        return typeList;
    }

    public void setTypeList(ArrayList<OccPeriodType> typeList) {
        this.typeList = typeList;
    }

    public OccPeriodType getSelectedPeriodType() {
        return selectedPeriodType;
    }

    public void setSelectedPeriodType(OccPeriodType selectedPeriodType) {
        this.selectedPeriodType = selectedPeriodType;
    }

    public ArrayList<OccPeriodType> getFilteredTypeList() {
        return filteredTypeList;
    }

    public void setFilteredTypeList(ArrayList<OccPeriodType> filteredTypeList) {
        this.filteredTypeList = filteredTypeList;
    }

    public List<Fee> getExistingFeeList() {
        return existingFeeList;
    }

    public void setExistingFeeList(List<Fee> existingFeeList) {
        this.existingFeeList = existingFeeList;
    }

    public ArrayList<Fee> getWorkingFeeList() {
        return workingFeeList;
    }

    public void setWorkingFeeList(ArrayList<Fee> workingFeeList) {
        this.workingFeeList = workingFeeList;
    }

    public Fee getSelectedWorkingFee() {
        return selectedWorkingFee;
    }

    public void setSelectedWorkingFee(Fee selectedWorkingFee) {
        this.selectedWorkingFee = selectedWorkingFee;
    }

    public List<Fee> getAllFees() {
        return allFees;
    }

    public void setAllFees(List<Fee> allFees) {
        this.allFees = allFees;
    }

    public Property getOccPeriodProperty() {

        PropertyIntegrator pi = getPropertyIntegrator();

        PropertyUnit unit;
        Property prop = new Property();
        try {
            unit = pi.getPropertyUnit(currentOccPeriod.getPropertyUnitID());
            prop = pi.getProperty(unit.getPropertyID());
        } catch (IntegrationException ex) {
            System.out.println("FeeManagementBB had problems getting the OccPeriodProperty");
        }

        return prop;

    }

    public String getOccPeriodAddress() {

        return getOccPeriodProperty().getAddress();

    }

    /**
     * getter that needs to be refactored
     *
     * @return
     */
    public String getCECaseAddress() {

        /*
        String currentCECaseAddress ="";
        
        PropertyIntegrator pi = getPropertyIntegrator();
        
        Property currentProp = new Property();
        
        currentProp.setAddress("");
        
        try {
        
            currentProp = pi.getProperty(currentCase.getPropertyID());
            
        } catch (IntegrationException e) {
        
            System.out.println("FeeManagementBB had problems getting the CECase address");
            
        }
        
        return currentProp.getAddress();
         */
//        TODO: NADGIT - upgrade to case with a Property in it
// so the member variable should be a CECasePropertyUnitHeavy and you can extract
// that objecet durectly in the XHTML and ask it for its address and drop into view
        return String.valueOf(currentCase.getPropertyID());

    }

    public OccPeriodType getLockedPeriodType() {
        return lockedPeriodType;
    }

    public void setLockedPeriodType(OccPeriodType lockedPeriodType) {
        this.lockedPeriodType = lockedPeriodType;
    }

    public EventDomainEnum getCurrentDomain() {
        return currentDomain;
    }

    public void setCurrentDomain(EventDomainEnum currentDomain) {
        this.currentDomain = currentDomain;
    }

    public CECase getCurrentCase() {
        return currentCase;
    }

    public void setCurrentCase(CECase currentCase) {
        this.currentCase = currentCase;
    }

    public ArrayList<CodeViolation> getFilteredViolationList() {
        return filteredViolationList;
    }

    public void setFilteredViolationList(ArrayList<CodeViolation> filteredViolationList) {
        this.filteredViolationList = filteredViolationList;
    }

    public CodeViolation getSelectedViolation() {
        return selectedViolation;
    }

    public void setSelectedViolation(CodeViolation selectedViolation) {
        this.selectedViolation = selectedViolation;
    }

    public ArrayList<EnforcableCodeElement> getElementList() {
        return elementList;
    }

    public void setElementList(ArrayList<EnforcableCodeElement> elementList) {
        this.elementList = elementList;
    }

    public ArrayList<EnforcableCodeElement> getFilteredElementList() {
        return filteredElementList;
    }

    public void setFilteredElementList(ArrayList<EnforcableCodeElement> filteredElementList) {
        this.filteredElementList = filteredElementList;
    }

    public EnforcableCodeElement getSelectedCodeElement() {
        return selectedCodeElement;
    }

    public void setSelectedCodeElement(EnforcableCodeElement selectedCodeElement) {
        this.selectedCodeElement = selectedCodeElement;
    }

    public EnforcableCodeElement getLockedCodeElement() {
        return lockedCodeElement;
    }

    public void setLockedCodeElement(EnforcableCodeElement lockedCodeElement) {
        this.lockedCodeElement = lockedCodeElement;
    }

    public boolean isRedirected() {
        return redirected;
    }

    public void setRedirected(boolean redirected) {
        this.redirected = redirected;
    }

    public boolean isCurrentFeeSelected() {
        return currentFeeSelected;
    }

    public void setCurrentFeeSelected(boolean currentFeeSelected) {
        this.currentFeeSelected = currentFeeSelected;
    }

}
